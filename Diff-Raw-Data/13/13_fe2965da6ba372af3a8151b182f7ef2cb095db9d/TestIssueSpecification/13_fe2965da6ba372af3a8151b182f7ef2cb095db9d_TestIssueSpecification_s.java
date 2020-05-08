 package org.ru.irma.api.tests.idemix;
 
 import static org.junit.Assert.*;
 
 import java.util.List;
 
 import javax.smartcardio.CardException;
 import javax.smartcardio.CardTerminal;
 import javax.smartcardio.TerminalFactory;
 
 import net.sourceforge.scuba.smartcards.CardService;
 import net.sourceforge.scuba.smartcards.CardServiceException;
 import net.sourceforge.scuba.smartcards.IResponseAPDU;
 import net.sourceforge.scuba.smartcards.TerminalCardService;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import service.IdemixService;
 import service.ProtocolCommand;
 import service.ProtocolResponses;
 
 import com.ibm.zurich.idmx.dm.Values;
 import com.ibm.zurich.idmx.issuance.IssuanceSpec;
 import com.ibm.zurich.idmx.issuance.Issuer;
 import com.ibm.zurich.idmx.issuance.Message;
 import com.ibm.zurich.idmx.key.IssuerKeyPair;
 import com.ibm.zurich.idmx.key.IssuerPrivateKey;
 import com.ibm.zurich.idmx.utils.SystemParameters;
 
 import credentials.Attributes;
 import credentials.CredentialsException;
 import credentials.idemix.IdemixCredentials;
 import credentials.idemix.IdemixPrivateKey;
 import credentials.idemix.spec.IdemixIssueSpecification;
 
 public class TestIssueSpecification {
 	
 	@BeforeClass
	public void trySetMasterSecret() throws CardException, CardServiceException {
 		CardService cs = TestSetup.getCardService();
 		
 		IdemixService service = new IdemixService(cs, TestSetup.CRED_NR);
 		
		service.open();
		service.generateMasterSecret();
		service.close();
 	}
 	
 	@Before
 	public void setupIdemixLibrary() {
     	TestSetup.setupSystem();
     	TestSetup.setupCredentialStructure();
 	}
 
 	
 	@Test
 	public void issueCredentialWithoutAPI() {
     	IssuanceSpec issuanceSpec = TestSetup.setupIssuanceSpec();
     	
 		Values values = getIssuanceValues(issuanceSpec.getPublicKey()
 				.getGroupParams().getSystemParams());
 		
 		IssuerPrivateKey isk = TestSetup.setupIssuerPrivateKey();
 
         // run the issuance protocol.
         Issuer issuer = new Issuer(new IssuerKeyPair(isk), issuanceSpec, null, null, values);
 
         IdemixService recipient = null;
         try {
             CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
             recipient = new IdemixService(new TerminalCardService(terminal),TestSetup.CRED_NR);
             recipient.open();
             recipient.sendPin(TestSetup.DEFAULT_PIN);
             recipient.setIssuanceSpecification(issuanceSpec);
             recipient.setAttributes(issuanceSpec, values);
         } catch (Exception e) {
             fail(e.getMessage()); 
             e.printStackTrace();            
         }
          
         Message msgToRecipient1 = issuer.round0();
         if (msgToRecipient1 == null) {
             fail("round0");
         }
 
         Message msgToIssuer1 = recipient.round1(msgToRecipient1);
         if (msgToIssuer1 == null) {
             fail("round1");
         }
 
         Message msgToRecipient2 = issuer.round2(msgToIssuer1);
         if (msgToRecipient2 == null) {
             fail("round2");
         }
 
         recipient.round3(msgToRecipient2);
 	}
 	
 	@Test
 	public void issueCredentialWithCardService() throws CardException, CredentialsException {
 		IdemixIssueSpecification spec = IdemixIssueSpecification
 				.fromIdemixIssuanceSpec(TestSetup.CRED_STRUCT_LOCATION, TestSetup.CRED_NR);
 
 		IdemixPrivateKey isk = IdemixPrivateKey.fromIdemixPrivateKey(TestSetup.ISSUER_SK_LOCATION);
 
 		CardService cs = TestSetup.getCardService();
 		IdemixCredentials ic = new IdemixCredentials(cs);
 		
 		Attributes attributes = getIssuanceAttributes();
 
 		ic.issue(spec, isk, attributes);
 	}
 	
 	@Test
 	public void issueCredentialAsync() throws CardException,
 			CredentialsException, CardServiceException {
 		IdemixIssueSpecification spec = IdemixIssueSpecification
 				.fromIdemixIssuanceSpec(TestSetup.CRED_STRUCT_LOCATION, TestSetup.CRED_NR);
 
 		TestSetup.setupIssuerPrivateKey();
 
 		Attributes attributes = getIssuanceAttributes();
 		IdemixCredentials ic = new IdemixCredentials();
 		
 		// Initialize the issuer
 		Issuer issuer = new Issuer(spec.getIssuerKey(), spec.getIssuanceSpec(),
 				null, null, spec.getValues(attributes));
 		
 		List<ProtocolCommand> commands = ic.requestIssueRound1Commands(spec, attributes, issuer);
 		ProtocolResponses responses = executeCommands(commands);
 		commands = ic.requestIssueRound3Commands(spec, attributes, issuer, responses);
 		responses = executeCommands(commands);
 		// Note: no processing of the commands is necessary generally, as long
 		// as errors propagate back up the change
 	}
 	
     private Values getIssuanceValues(SystemParameters syspars) {
         Values values = new Values(syspars);
         values.add("attr1", TestSetup.ATTRIBUTE_VALUE_1);
         values.add("attr2", TestSetup.ATTRIBUTE_VALUE_2);
         values.add("attr3", TestSetup.ATTRIBUTE_VALUE_3);
         values.add("attr4", TestSetup.ATTRIBUTE_VALUE_4);
         
         return values;
     }
     
     /**
      * TODO: Actually, I do not know how to make ByteEncoded attributes containing the
      * BigNumbers usually used for Idemix... Or maybe this works naturally. At least needs
      * checking.
      * 
      * @param syspars
      * @return
      */
     private Attributes getIssuanceAttributes() {
         // Return the attributes that have been revealed during the proof
         Attributes attributes = new Attributes();
 
         attributes.add("attr1", TestSetup.ATTRIBUTE_VALUE_1.toByteArray());
         attributes.add("attr2", TestSetup.ATTRIBUTE_VALUE_2.toByteArray());
         attributes.add("attr3", TestSetup.ATTRIBUTE_VALUE_3.toByteArray());
         attributes.add("attr4", TestSetup.ATTRIBUTE_VALUE_4.toByteArray());
         
         return attributes;
     }
     
 	private ProtocolResponses executeCommands(List<ProtocolCommand> commands)
 			throws CardServiceException, CardException {
 		CardService service = TestSetup.getCardService();
 
 		service.open();
 
 		ProtocolResponses responses = new ProtocolResponses();
 		for (ProtocolCommand c : commands) {
 			IResponseAPDU response = IdemixService.transmit(service, c.command);
 			responses.put(c.key, response);
 			if (response.getSW() != 0x00009000) {
 				// don't bother with the rest of the commands...
 				// TODO: get error message from global table
 				String errorMessage = c.errorMap != null
 						&& c.errorMap.containsKey(response.getSW()) ? c.errorMap
 						.get(response.getSW()) : "";
 				throw new CardServiceException(String.format(
 						"Command failed: \"%s\", SW: %04x (%s)", c.description,
 						response.getSW(), errorMessage));
 			}
 		}
 
 		service.close();
 		return responses;
 	}
 }
