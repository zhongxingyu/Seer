 /**
  * Copyright IBM Corporation 2009-2011.
  */
 package com.ibm.zurich.idmx.tests.idmx;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.smartcardio.CardTerminal;
 import javax.smartcardio.TerminalFactory;
 
import net.sourceforge.scuba.smartcards.CardServiceException;
 import net.sourceforge.scuba.smartcards.TerminalCardService;
 
 import service.IdemixService;
 
 import junit.framework.TestCase;
 
 import com.ibm.zurich.credsystem.utils.Locations;
 import com.ibm.zurich.idmx.dm.Values;
 import com.ibm.zurich.idmx.issuance.IssuanceSpec;
 import com.ibm.zurich.idmx.issuance.Issuer;
 import com.ibm.zurich.idmx.issuance.Message;
 import com.ibm.zurich.idmx.key.IssuerKeyPair;
 import com.ibm.zurich.idmx.showproof.Proof;
 import com.ibm.zurich.idmx.showproof.ProofSpec;
 import com.ibm.zurich.idmx.showproof.Verifier;
 import com.ibm.zurich.idmx.utils.Constants;
 import com.ibm.zurich.idmx.utils.Parser;
 import com.ibm.zurich.idmx.utils.StructureStore;
 import com.ibm.zurich.idmx.utils.SystemParameters;
 import com.ibm.zurich.idmx.utils.Utils;
 import com.ibm.zurich.idmx.utils.XMLSerializer;
 
 /**
  * Test cases to cover issuance of credentials.
  */
 public class Test4Attributes extends TestCase {
 
     /** Actual location of the files. */
     public static final URI BASE_LOCATION = new File(
             System.getProperty("user.dir")).toURI().resolve("files/parameter/");
 
     /** Id that is used within the test files to identify the elements. */
     public static URI BASE_ID = null;
     
     /** Id that is used within the test files to identify the elements. */
     public static URI ISSUER_ID = null;
     static {
         try {
             BASE_ID = new URI("http://www.zurich.ibm.com/security/idmx/v2/");
             ISSUER_ID = new URI("http://www.issuer.com/");
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
     }
     
     /** Actual location of the public issuer-related files. */
     public static final URI ISSUER_LOCATION = BASE_LOCATION
             .resolve("../issuerData/");
 
     /** Attribute value 1313. */
     public static final BigInteger ATTRIBUTE_VALUE_1 = BigInteger.valueOf(1313);
     /** Attribute value 1314. */
     public static final BigInteger ATTRIBUTE_VALUE_2 = BigInteger.valueOf(1314);
     /** Attribute value 1315. */
     public static final BigInteger ATTRIBUTE_VALUE_3 = BigInteger.valueOf(1315);
     /** Attribute value 1316. */
     public static final BigInteger ATTRIBUTE_VALUE_4 = BigInteger.valueOf(1316);
     /** Attribute value 1317. */
     public static final BigInteger ATTRIBUTE_VALUE_5 = BigInteger.valueOf(1317);
 
     /**
      * Credential structure.
      * <ol>
      * <li>attr1: known: int</li>
      * <li>attr2: known: int</li>
      * <li>attr3: known: int</li>
      * <li>attr4: known: int</li>
      * <li>attr5: known: int</li>
      * </ol>
      */
     public static final String CRED_STRUCT_CARD = "CredStructCard4";
 
     /**
      * Credential.<br/>
      * <ol>
      * <li>attr1:1313/ATTRIBUTE_VALUE_1</li>
      * <li>attr2:1314/ATTRIBUTE_VALUE_2</li>
      * <li>attr3:1315/ATTRIBUTE_VALUE_3</li>
      * <li>attr4:1316/ATTRIBUTE_VALUE_4</li>
      * <li>attr5:1317/ATTRIBUTE_VALUE_5</li>
      * </ol>
      * 
      * @see Test4Attributes#CRED_STRUCT_CARD
      */
     public static final String CREDCARD_FN = "Credential_card";    
 
     /** Key pair of the issuer. */
     private IssuerKeyPair issuerKey = null;
 
     /** Names of the Proof and Nonce objects. */
     private static final String CL_CARD = "clCardValues";
 
     public final static String ENDING = "\n "
             + "============================================================\n";
     public final static String PROOF_VERIFIED = "Proof Verified." + ENDING;
     
     /**
      * Default PIN of card.
      */
     private static final byte[] DEFAULT_PIN = {0x30, 0x30, 0x30, 0x30};
 
     /**
      * Setup of the test environment.
      */
     protected final void setUp() {
         
         // URIs and locations for issuer
         URI iskLocation = BASE_LOCATION.resolve("../private/isk.xml");
         URI ipkLocation = ISSUER_LOCATION.resolve("ipk.xml");
 
         issuerKey = Locations.initIssuer(BASE_LOCATION, BASE_ID.toString(),
                 iskLocation, ipkLocation, ISSUER_ID.resolve("ipk.xml"));
 
         Locations.initSystem(BASE_LOCATION, BASE_ID.toString());
 
         // loading issuer public key
         Locations.init(ISSUER_ID.resolve("ipk.xml"), ISSUER_LOCATION.resolve("ipk.xml"));
 
         // loading credential structures
         loadCredStruct(CRED_STRUCT_CARD);
     }
 
     /**
      * Executed upon finishing the test run.
      */
     protected final void tearDown() {}
 
     /**
      * Test: Reads the library version and fails if the version is not the
      * expected version.
      */
     public final void testVersion() {
         if (Constants.getVersion() != "2.3.4") {
             fail("wrong version");
         }
     }
 
     /**
      * Test: Issues a credential.
      * 
      * @see Test4Attributes#CRED_STRUCT_CARD
      * @see Test4Attributes#CREDCARD_FN
      */
     public final void testIssuance_CredCard() {
         String credStruct = Test4Attributes.CRED_STRUCT_CARD;
 
         // URIs and locations for recipient
         URI credStructLocation = null, credStructId = null;
         try {
             credStructLocation = BASE_LOCATION.resolve("../issuerData/"
                     + credStruct + ".xml");
             credStructId = new URI("http://www.ngo.org/" + credStruct + ".xml");
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
 
         // loading credential structure linked to a URI
         Locations.init(credStructId, credStructLocation);
 
         // create the issuance specification
         IssuanceSpec issuanceSpec = new IssuanceSpec(
                 ISSUER_ID.resolve("ipk.xml"), credStructId);
 
         // get the values - NOTE: the values are KNOWN to both parties (as
         // specified in the credential structure)
         Values values = new Values(issuerKey.getPublicKey().getGroupParams()
                 .getSystemParams());
         values.add("attr1", ATTRIBUTE_VALUE_1);
         values.add("attr2", ATTRIBUTE_VALUE_2);
         values.add("attr3", ATTRIBUTE_VALUE_3);
         values.add("attr4", ATTRIBUTE_VALUE_4);
 
         // run the issuance protocol.
         Issuer issuer = new Issuer(issuerKey, issuanceSpec, null, null, values);
 
         IdemixService recipient = null;
         try {
             CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
             recipient = new IdemixService(new TerminalCardService(terminal),(short)4);
             recipient.open();
            try {
            	recipient.generateMasterSecret();
            } catch (CardServiceException e) {
            	System.out.println("Could not set master secret again, ignoring.");
            	e.printStackTrace();
            }

             recipient.sendPin(DEFAULT_PIN);
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
 
     private static final void loadCredStruct(String credStructName) {
         URI credStructLocation = null, credStructId = null;
         try {
             credStructLocation = BASE_LOCATION
                     .resolve("../issuerData/" + credStructName + ".xml");
             credStructId = new URI("http://www.ngo.org/" + credStructName
                     + ".xml");
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
 
         // loading credential structure linked to a URI
         Locations.init(credStructId, credStructLocation);
     }
 
     /**
      * Convenience method.
      */
     private final String getProofLocation(String name) {
         return "../send/" + name + "_proof.xml";
     }
 
     /**
      * Convenience method.
      */
     private final String getNonceLocation(String name) {
         return "../send/" + name + "_nonce.xml";
     }
 
     /**
      * Convenience method.
      */
     private void serializeElements(String name, Proof p, BigInteger nonce) {
         // save the proof
         XMLSerializer.getInstance().serialize(p,
                 BASE_LOCATION.resolve(getProofLocation(name)));
         // save the nonce for the verification test case
         XMLSerializer.getInstance().serialize(nonce,
                 BASE_LOCATION.resolve(getNonceLocation(name)));
     }
 
     /**
      * Test: Builds a proof according to the specification.
      */
     public final void testProve_CredCard() {
 
         // load the proof specification
         ProofSpec spec = (ProofSpec) StructureStore.getInstance().get(
                 BASE_LOCATION
                         .resolve("../proofSpecifications/ProofSpecCard4.xml"));
         System.out.println(spec.toStringPretty());
 
         SystemParameters sp = spec.getGroupParams().getSystemParams();
 
         // first get the nonce (done by the verifier)
         System.out.println("Getting nonce.");
         BigInteger nonce = Verifier.getNonce(sp);
 
         IdemixService prover = null;
         try {
             CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);            
             prover = new IdemixService(new TerminalCardService(terminal), (short)4);
             prover.open();
         } catch (Exception e) {
             fail(e.getMessage()); 
             e.printStackTrace();            
         }
 
         // create the proof
         Proof p = prover.buildProof(nonce, spec);
         System.out.println("Proof Created.");
 
         serializeElements(CL_CARD, p, nonce);
     }
 
     /**
      * Test: Verifies the proof according to the specification.
      * 
      * @see TestProofCard#testProve_CredCard()
      */
     public final void testVerify_CredCard() {
 
         // load the proof specification
         ProofSpec spec = (ProofSpec) StructureStore.getInstance().get(
                 BASE_LOCATION.resolve("../proofSpecifications/ProofSpecCard4.xml"));
         System.out.println(spec.toStringPretty());
 
         // load the proof
         Proof p = (Proof) Parser.getInstance().parse(
                 BASE_LOCATION.resolve(getProofLocation(CL_CARD)));
         BigInteger nonce = (BigInteger) Parser.getInstance().parse(
                 BASE_LOCATION.resolve(getNonceLocation(CL_CARD)));
 
         // now p is sent to the verifier
         Verifier verifier = new Verifier(spec, p, nonce);
         if (!verifier.verify()) {
             fail("The proof does not verify");
         } else {
             System.out.println(PROOF_VERIFIED);
         }
 
         // shows the values that have been revealed during the proof
         HashMap<String, BigInteger> revealedValues = verifier
                 .getRevealedValues();
         outputRevealedValues(revealedValues);
     }
 
     /**
      * @param revealedValues
      */
     private static final void outputRevealedValues(
             HashMap<String, BigInteger> revealedValues) {
         Iterator<String> it = revealedValues.keySet().iterator();
         System.out.println("Revealed values...");
         while (it.hasNext()) {
             String key = it.next();
             System.out.println("\t" + key + "\t"
                     + Utils.logBigInt(revealedValues.get(key)));
         }
         System.out.println(ENDING);
     }
 }
