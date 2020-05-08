 package org.irmacard.web.restapi;
 
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import org.irmacard.web.restapi.resources.VerificationProtocolResource;
 
 import com.ibm.zurich.credsystem.utils.Locations;
 import com.ibm.zurich.idmx.issuance.IssuanceSpec;
 import com.ibm.zurich.idmx.key.IssuerKeyPair;
 import com.ibm.zurich.idmx.key.IssuerPrivateKey;
 
 
 /**
  * A place for storing idemix parameters and references to idemix-related files.
  * Mostly copied from TestSetup.java in the credentials_api repository.
  */
 public class IRMASetup {
 	
 	public static URI BASE_LOCATION = null;
     
     /** Actual location of the public issuer-related files. */
     public static URI ISSUER_LOCATION = null;
 	
     /** URIs and locations for issuer */
     public static URI ISSUER_SK_LOCATION = null;
     public static URI ISSUER_PK_LOCATION = null;
     
     /** Credential location */
     public static final String CRED_STRUCT_NAME = "CredStructCard4";
     public static URI CRED_STRUCT_LOCATION = null;
     
     /** Proof specification location */
     public static URI PROOF_SPEC_LOCATION = null;
 
 	static {
 		try {
 			BASE_LOCATION = VerificationProtocolResource.class.getClassLoader().getResource("/resources/parameter/").toURI();
 			ISSUER_LOCATION = BASE_LOCATION.resolve("../issuerData/");
 			ISSUER_SK_LOCATION = BASE_LOCATION.resolve("../private/isk.xml");
 			ISSUER_PK_LOCATION = ISSUER_LOCATION.resolve("ipk.xml");
 			CRED_STRUCT_LOCATION = BASE_LOCATION
 		            .resolve("../issuerData/" + CRED_STRUCT_NAME + ".xml");
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
             BASE_ID = new URI("http://www.zurich.ibm.com/security/idmx/v2/");
             ISSUER_ID = new URI("http://www.issuer.com/");
             CRED_STRUCT_ID = new URI("http://www.ngo.org/" + CRED_STRUCT_NAME + ".xml");
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
         setupIssuer();
         setupCredentialStructure();
 //        setupIssuanceSpec();
     }
     
     /** The identifier of the credential on the smartcard */
     public static short CRED_NR = (short) 4;
 
     /** Attribute values */
     public static final BigInteger ATTRIBUTE_VALUE_1 = BigInteger.valueOf(1313);
     public static final BigInteger ATTRIBUTE_VALUE_2 = BigInteger.valueOf(1314);
     public static final BigInteger ATTRIBUTE_VALUE_3 = BigInteger.valueOf(1315);
     public static final BigInteger ATTRIBUTE_VALUE_4 = BigInteger.valueOf(1316);
 
     /**
 	 * Setup the system including private key
 	 * 
	 * For use with the credentials-api it is not advisable to use initIssuer.
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
 
     /** Setup the issuer's private key */
     public static IssuerPrivateKey setupIssuerPrivateKey() {
     	IssuerKeyPair ikp = (IssuerKeyPair) Locations.init(ISSUER_SK_LOCATION);
     	return ikp.getPrivateKey();
     }
 }
