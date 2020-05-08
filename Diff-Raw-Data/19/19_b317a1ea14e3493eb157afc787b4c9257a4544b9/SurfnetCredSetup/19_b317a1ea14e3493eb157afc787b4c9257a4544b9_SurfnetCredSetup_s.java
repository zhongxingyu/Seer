 package org.irmacard.web.restapi.resources;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.irmacard.web.restapi.resources.VerificationProtocolResource;
 
 import com.ibm.zurich.credsystem.utils.Locations;
 import com.ibm.zurich.idmx.issuance.IssuanceSpec;
 import com.ibm.zurich.idmx.key.IssuerKeyPair;
 
 
 /**
  * A place for storing idemix parameters and references to idemix-related files.
  * Mostly copied from TestSetup.java in the credentials_api repository.
  */
 public class SurfnetCredSetup {
 	/** Actual setup */
     private static String CRED_STRUCT_NAME = "root";
     private static String ISSUER_NAME = "Surfnet";
    private static String ISSUER_BASE_URL = "http://www.irmacard.org/org.irmacard.credentials/phase1/surfnet/";
     
 	
 	public static URI BASE_LOCATION = null;
 	
     /** URIs and locations for issuer */
     public static URI ISSUER_SK_LOCATION = null;
     public static URI ISSUER_PK_LOCATION = null;
     
     /** Credential location */
 
     public static URI CRED_STRUCT_LOCATION = null;
     
     /** Proof specification location */
     public static URI PROOF_SPEC_LOCATION = null;
 
 	static {
 		try {
 			BASE_LOCATION = VerificationProtocolResource.class.getClassLoader()
 					.getResource("/resources/" + ISSUER_NAME + "/").toURI();
 			//ISSUER_SK_LOCATION = BASE_LOCATION.resolve("private/isk.xml");
 			ISSUER_PK_LOCATION = BASE_LOCATION.resolve("ipk.xml");
 			CRED_STRUCT_LOCATION = BASE_LOCATION
 		            .resolve("Issues/" + CRED_STRUCT_NAME + "/structure.xml");
 			PROOF_SPEC_LOCATION = BASE_LOCATION
                     .resolve("../proofSpecifications/ProofSpecCard4.xml");
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
     /** Ids used within the test files to identify the elements. */
     public static URI BASE_ID = null;
     public static URI ISSUER_ID = null;
     public static URI CRED_STRUCT_ID = null;
     static {
         try {
             BASE_ID = new URI(ISSUER_BASE_URL);
             ISSUER_ID = BASE_ID;
             CRED_STRUCT_ID = BASE_ID.resolve(CRED_STRUCT_NAME + "structure.xml");
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
         setupIssuer();
         setupCredentialStructure();
     }
     
     /** The identifier of the credential on the smartcard */
     public static short CRED_NR = (short) 1;
 
     /**
 	 * Setup the system including private key
 	 * 
	 * For use with the org.irmacard.credentials-api it is not advisable to use initIssuer.
 	 * Using the seperate functions for setting up the material gives a bit more
 	 * control.
 	 */
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
 }
