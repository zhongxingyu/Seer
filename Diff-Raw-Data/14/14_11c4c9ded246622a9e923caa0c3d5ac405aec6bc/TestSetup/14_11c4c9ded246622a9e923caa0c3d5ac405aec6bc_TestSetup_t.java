 /**
  * TestSetup.java
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright (C) Wouter Lueks, Radboud University Nijmegen, July 2012.
  */
 
 package org.irmacard.credentials.idemix.test;
 
 import java.io.File;
 import java.math.BigInteger;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.smartcardio.CardException;
 import javax.smartcardio.CardTerminal;
 import javax.smartcardio.TerminalFactory;
 
 import service.IdemixService;
 
 import net.sourceforge.scuba.smartcards.TerminalCardService;
 
 import com.ibm.zurich.credsystem.utils.Locations;
 import com.ibm.zurich.idmx.issuance.IssuanceSpec;
 import com.ibm.zurich.idmx.key.IssuerKeyPair;
 import com.ibm.zurich.idmx.key.IssuerPrivateKey;
 import com.ibm.zurich.idmx.showproof.ProofSpec;
 import com.ibm.zurich.idmx.utils.StructureStore;
 
 public class TestSetup {
     /** Actual location of the files. */
 	/** TODO: keep this in mind, do we need BASE_LOCATION to point to .../parameter/
 	 *  to keep idemix-library happy, i.e. so that it can find gp.xml and sp.xml?
 	 */
     public static final URI BASE_LOCATION = new File(
             System.getProperty("user.dir")).toURI().resolve("irma_configuration/RU/");
     
     /** Actual location of the public issuer-related files. */
     public static final URI ISSUER_LOCATION = BASE_LOCATION;
 	
     /** URIs and locations for issuer */
     public static final URI ISSUER_SK_LOCATION = ISSUER_LOCATION.resolve("private/isk.xml");
     public static final URI ISSUER_PK_LOCATION = ISSUER_LOCATION.resolve("ipk.xml");
     
     /** Credential location */
     public static final String CRED_STRUCT_NAME = "studentCard";
     public static final URI CRED_STRUCT_LOCATION = BASE_LOCATION
             .resolve("Issues/studentCard/structure.xml");
     
     /** Proof specification location */
     public static final URI PROOF_SPEC_LOCATION = BASE_LOCATION
                             .resolve("Verifies/studentCardAll/specification.xml");
     
     /** Ids used within the test files to identify the elements. */
     public static URI BASE_ID = null;
     public static URI ISSUER_ID = null;
     public static URI CRED_STRUCT_ID = null;
     static {
         try {
             BASE_ID = new URI("http://www.irmacard.org/credentials/phase1/RU/");
             ISSUER_ID = new URI("http://www.issuer.com/");
             CRED_STRUCT_ID = new URI("http://www.ngo.org/" + CRED_STRUCT_NAME + ".xml");
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
     }
     
     /** The identifier of the credential on the smartcard */
     public static short CRED_NR = (short) 4;
 
     /** Attribute values */
     public static final BigInteger ATTRIBUTE_VALUE_1 = BigInteger.valueOf(1313);
     public static final BigInteger ATTRIBUTE_VALUE_2 = BigInteger.valueOf(1314);
     public static final BigInteger ATTRIBUTE_VALUE_3 = BigInteger.valueOf(1315);
     public static final BigInteger ATTRIBUTE_VALUE_4 = BigInteger.valueOf(1316);
     public static final BigInteger ATTRIBUTE_VALUE_5 = BigInteger.valueOf(1317);
 
     /**
      * Default PIN of card.
      */
     public static final byte[] DEFAULT_PIN = {0x30, 0x30, 0x30, 0x30};
    public static final byte[] DEFAULT_MASTER_PIN = {0x30, 0x30, 0x30, 0x30, 0x30, 0x30};
 
     /** This one also sets up the system, but now it doesn't know the private key */
     public static void setupSystem() {
         Locations.initSystem(BASE_LOCATION, BASE_ID.toString());
 
         // loading issuer public key
         Locations.init(ISSUER_ID.resolve("ipk.xml"), ISSUER_LOCATION.resolve("ipk.xml"));
     }
     
     /** Setup the issuer's private key */
     public static IssuerPrivateKey setupIssuerPrivateKey() {
     	IssuerKeyPair ikp = (IssuerKeyPair) Locations.init(ISSUER_SK_LOCATION);
     	return ikp.getPrivateKey();
     }
 
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
     
     public static TerminalCardService getCardService() throws CardException {
 		CardTerminal terminal = TerminalFactory.getDefault().terminals().list().get(0);
 		return new TerminalCardService(terminal);
     }
     
     public static IdemixService getIdemixService() throws CardException {
     	return new IdemixService(getCardService());
     }
 
     // load the proof specification
     public static ProofSpec setupProofSpec() {
     	return (ProofSpec) StructureStore.getInstance().get(PROOF_SPEC_LOCATION);
     }
 }
