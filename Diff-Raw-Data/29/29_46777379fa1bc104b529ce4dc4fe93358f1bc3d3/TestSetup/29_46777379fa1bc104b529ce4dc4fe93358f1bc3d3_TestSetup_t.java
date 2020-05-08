 package org.ru.irma.api.tests.idemix;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.smartcardio.CardException;
 import javax.smartcardio.CardTerminal;
 import javax.smartcardio.TerminalFactory;
 
 import net.sourceforge.scuba.smartcards.TerminalCardService;
 
 import com.ibm.zurich.credsystem.utils.Locations;
 import com.ibm.zurich.idmx.issuance.IssuanceSpec;
 import com.ibm.zurich.idmx.key.IssuerKeyPair;
 import com.ibm.zurich.idmx.showproof.ProofSpec;
 import com.ibm.zurich.idmx.utils.StructureStore;
 
 public class TestSetup {
     /** Actual location of the files. */
 	/** TODO: keep this in mind, do we need BASE_LOCATION to point to .../parameter/
 	 *  to keep idemix-library happy, i.e. so that it can find gp.xml and sp.xml?
 	 */
     public static final URI BASE_LOCATION = new File(
             System.getProperty("user.dir")).toURI().resolve("files/parameter/");
     
     /** Actual location of the public issuer-related files. */
     public static final URI ISSUER_LOCATION = BASE_LOCATION
             .resolve("../issuerData/");
 	
     /** URIs and locations for issuer */
     public static final URI ISSUER_SK_LOCATION = BASE_LOCATION.resolve("../private/isk.xml");
     public static final URI ISSUER_PK_LOCATION = ISSUER_LOCATION.resolve("ipk.xml");
     
     /** Credential location */
     public static final String CRED_STRUCT_NAME = "CredStructCard4";
     public static final URI CRED_STRUCT_LOCATION = BASE_LOCATION
             .resolve("../issuerData/" + CRED_STRUCT_NAME + ".xml");
     
     /** Proof specification location */
     public static final URI PROOF_SPEC_LOCATION = BASE_LOCATION
                             .resolve("../proofSpecifications/ProofSpecCard4.xml");
     
     /** Ids used within the test files to identify the elements. */
     public static URI BASE_ID = null;
     public static URI ISSUER_ID = null;
     public static URI CRED_STRUCT_ID = null;
     static {
         try {
             BASE_ID = new URI("http://www.zurich.ibm.com/security/idmx/v2/");
             ISSUER_ID = new URI("http://www.issuer.com/");
             CRED_STRUCT_ID = new URI("http://www.ngo.org/" + CRED_STRUCT_NAME + ".xml");
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
     }
     
    /** The identifier of the credential on the smartcard */
    public static short CRED_NR = (short) 4;

     /** This one also sets up the system, but now it doesn't know the private key */
     public static void setupSystem() {
         Locations.initSystem(BASE_LOCATION, BASE_ID.toString());
 
         // loading issuer public key
         Locations.init(ISSUER_ID.resolve("ipk.xml"), ISSUER_LOCATION.resolve("ipk.xml"));
     }
     
     /** Setup the system including private key */
     public static IssuerKeyPair setupIssuer() {
     	return Locations.initIssuer(BASE_LOCATION, BASE_ID.toString(),
     			ISSUER_SK_LOCATION, ISSUER_PK_LOCATION, ISSUER_ID.resolve("ipk.xml"));
     }
     
     public static void setupCredentialStructure() {
     	Locations.init(CRED_STRUCT_ID, CRED_STRUCT_LOCATION);
     }
     
     public static IssuanceSpec setupIssuanceSpec() {
         // create the issuance specification
         return new IssuanceSpec(ISSUER_ID.resolve("ipk.xml"), CRED_STRUCT_ID);
     }
     
     public static TerminalCardService getCardService() throws CardException {
 		CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);
 		return new TerminalCardService(terminal);
     }
 
     // load the proof specification
     public static ProofSpec setupProofSpec() {
     	return (ProofSpec) StructureStore.getInstance().get(PROOF_SPEC_LOCATION);
     }
 }
