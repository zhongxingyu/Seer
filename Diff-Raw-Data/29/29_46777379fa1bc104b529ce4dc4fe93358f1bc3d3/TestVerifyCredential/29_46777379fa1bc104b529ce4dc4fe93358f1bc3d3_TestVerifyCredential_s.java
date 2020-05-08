 package org.ru.irma.api.tests.idemix;
 
 import java.math.BigInteger;
 
 import javax.smartcardio.CardException;
 import javax.smartcardio.CardTerminal;
 import javax.smartcardio.TerminalFactory;
 
 import net.sourceforge.scuba.smartcards.CardService;
 import net.sourceforge.scuba.smartcards.TerminalCardService;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import service.IdemixService;
 
 import com.ibm.zurich.idmx.showproof.Proof;
 import com.ibm.zurich.idmx.showproof.ProofSpec;
 import com.ibm.zurich.idmx.showproof.Verifier;
 import com.ibm.zurich.idmx.utils.SystemParameters;
 
 import credentials.CredentialsException;
 import credentials.idemix.IdemixCredentials;
 import credentials.idemix.spec.IdemixVerifySpecification;
 
 public class TestVerifyCredential {
 	
 	@Before
 	public void setupIdemix() {
     	TestSetup.setupIssuer();
     	TestSetup.setupCredentialStructure();
     	TestSetup.setupIssuanceSpec();
 	}
 
 	@Test
 	public void verifyCredentialWithoutAPI() {
         // load the proof specification
         ProofSpec spec = TestSetup.setupProofSpec();
         System.out.println(spec.toStringPretty());
 
         SystemParameters sp = spec.getGroupParams().getSystemParams();
 
         // first get the nonce (done by the verifier)
         System.out.println("Getting nonce.");
         BigInteger nonce = Verifier.getNonce(sp);
 
         IdemixService prover = null;
         try {
             CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);
			prover = new IdemixService(new TerminalCardService(terminal));
             prover.open();
         } catch (Exception e) {
 			e.printStackTrace();
 			fail(e.getMessage());
         }
 
         // create the proof
         Proof p = prover.buildProof(nonce, spec);
 
         // now p is sent to the verifier
         Verifier verifier = new Verifier(spec, p, nonce);
         if (!verifier.verify()) {
             fail("The proof does not verify");
         } else {
             System.out.println("Proof verified");
         }
 	}
 
 	@Test
 	public void verifyCredentialWithCardService() throws CredentialsException, CardException {
 		// Setup proof spec
 		IdemixVerifySpecification vspec = IdemixVerifySpecification
 				.fromIdemixProofSpec(TestSetup.PROOF_SPEC_LOCATION);
 
 		CardService cs = TestSetup.getCardService();
 
 		IdemixCredentials ic = new IdemixCredentials(cs);
 
 		ic.verify(vspec);
 	}
 }
