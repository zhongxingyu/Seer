 /**
  * IRMASmartcard.java
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
  * Copyright (C) Pim Vullers, Radboud University Nijmegen, June 2011.
  */
 
 package org.irmacard.idemix;
 
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateEncodingException;
 import java.security.interfaces.RSAPrivateKey;
 import java.security.interfaces.RSAPublicKey;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.TreeMap;
 import java.util.Vector;
 
 import org.irmacard.idemix.util.IdemixFlags;
 
 import net.sourceforge.scuba.smartcards.CommandAPDU;
 import net.sourceforge.scuba.smartcards.ISO7816;
 import net.sourceforge.scuba.smartcards.ProtocolCommand;
 import net.sourceforge.scuba.smartcards.ProtocolCommands;
 import net.sourceforge.scuba.smartcards.ProtocolErrors;
 import net.sourceforge.scuba.smartcards.ProtocolResponses;
 
 import com.ibm.zurich.idmx.dm.Values;
 import com.ibm.zurich.idmx.dm.structure.AttributeStructure;
 import com.ibm.zurich.idmx.dm.structure.CredentialStructure;
 import com.ibm.zurich.idmx.issuance.IssuanceSpec;
 import com.ibm.zurich.idmx.issuance.Message;
 import com.ibm.zurich.idmx.issuance.Message.IssuanceProtocolValues;
 import com.ibm.zurich.idmx.key.IssuerPublicKey;
 import com.ibm.zurich.idmx.showproof.Identifier;
 import com.ibm.zurich.idmx.showproof.Proof;
 import com.ibm.zurich.idmx.showproof.ProofSpec;
 import com.ibm.zurich.idmx.showproof.predicates.CLPredicate;
 import com.ibm.zurich.idmx.showproof.predicates.Predicate;
 import com.ibm.zurich.idmx.showproof.predicates.Predicate.PredicateType;
 import com.ibm.zurich.idmx.showproof.sval.SValue;
 import com.ibm.zurich.idmx.showproof.sval.SValuesProveCL;
 import com.ibm.zurich.idmx.utils.StructureStore;
 import com.ibm.zurich.idmx.utils.SystemParameters;
 
 /**
  * Idemix Smart Card Interface based on a SCUBA Card Service.
  *
  * @author Pim Vullers
  * @version $Revision: 554 $ by $Author: pim $
  *          $LastChangedDate: 2011-04-28 16:31:47 +0200 (Thu, 28 Apr 2011) $
  */
 public class IdemixSmartcard {
 
     /**
      * AID of the IRMAcard application: ASCII encoding of "IRMAcard".
      */
     private static final byte[] AID = {(byte) 0xF8, 0x49, 0x52, 0x4D, 0x41, 0x63, 0x61, 0x72, 0x64};    
     private static final byte[] AID_0_7 = {0x49, 0x52, 0x4D, 0x41, 0x63, 0x61, 0x72, 0x64};
 
     /**
      * INStruction to select an application.
      */
     private static final byte INS_SELECT_APPLICATION = (byte) 0xA4;
 
     /**
      * P1 parameter for select by name.
      */
     private static final byte P1_SELECT_BY_NAME = 0x04;
 
     
     /**
      * CLAss to be used for IRMA APDUs.
      */
     private static final byte CLA_IRMACARD = (byte) 0x80;
 
     /**
      * CLAss mask to indicate command chaining.
      */
     private static final byte CLA_COMMAND_CHAINING = 0x10;
 
     /**
      * INStruction to generate the master secret on the card.
      */
     private static final byte INS_GENERATE_SECRET = 0x01;
 
     /**
      * INStruction to generate the master secret on the card.
      */
     private static final byte INS_AUTHENTICATION_SECRET = 0x02;
     
     /**
      * INStruction to start issuing a credential (and to set the corresponding
      * context and issuance information).
      */
     private static final byte INS_ISSUE_CREDENTIAL = 0x10;
 
     /**
      * INStruction to issue the the issuer public key.
      */
     private static final byte INS_ISSUE_PUBLIC_KEY = 0x11;
 
     /**
      * INStruction to issue the attributes.
      */
     private static final byte INS_ISSUE_ATTRIBUTES = 0x12;
 
     /**
      * combined hidden attributes (U).
      */
     private static final byte INS_ISSUE_COMMITMENT = 0x1A;
 
     /**
      * INStruction to receive the zero-knowledge proof for correct construction
      * of U (c, v^', s_A).
      */
     private static final byte INS_ISSUE_COMMITMENT_PROOF = 0x1B;
 
     /**
      * INStruction to receive the second nonce (n_2).
      */
     private static final byte INS_ISSUE_CHALLENGE = 0x1C;
 
     /**
      * INStruction to send the blind signature (A, e, v'').
      */
     private static final byte INS_ISSUE_SIGNATURE = 0x1D;
 
     /**
      * INStruction to verify the signature and the zero-knowledge proof.
      */
     private static final byte INS_ISSUE_VERIFY = 0x1F;
 
     /**
      * INStruction to start proving attributes from a credential (and to set
      * the corresponding context).
      */
     private static final byte INS_PROVE_CREDENTIAL = 0x20;
 
     /**
      * INStruction to send the challenge (m) to be signed in the proof and
      * receive the commitment for the proof (a).
      */
     private static final byte INS_PROVE_COMMITMENT = 0x2A;
 
     /**
      * INStruction to receive the values A', e^ and v^.
      */
     private static final byte INS_PROVE_SIGNATURE = 0x2B;
 
     /**
      * INStruction to receive the disclosed attributes (A_i).
      */
     private static final byte INS_PROVE_ATTRIBUTE = 0x2C;
 
     /**
      * INStruction to select a credential on the card.
      */
 	private static final byte INS_ADMIN_CREDENTIAL = 0x30;
 
     /**
      * INStruction to remove a credential from the card.
      */
     private static final byte INS_ADMIN_REMOVE = 0x31;
 
     /**
      * INStruction to get an attribute from the current selected credential.
      */
     private static final byte INS_ADMIN_ATTRIBUTE = 0x32;
 
     /**
      * INStruction to get the flags of a credential.
      */
     private static final byte INS_ADMIN_FLAGS = 0x33;
 
     /**
      * INStruction to get a list of credentials stored on the card.
      */
     private static final byte INS_ADMIN_CREDENTIALS = 0x3A;
 
     /**
      * INStruction to get the transaction log from the card.
      */
     private static final byte INS_ADMIN_LOG = 0x3B;
 
 
     /**
      * P1 parameter for the n value from the issuer public key.
      */
     private static final byte P1_PUBLIC_KEY_N = 0x00;
 
     /**
      * P1 parameter for the s value from the issuer public key.
      */
     private static final byte P1_PUBLIC_KEY_S = 0x01;
 
     /**
      * P1 parameter for the z value from the issuer public key.
      */
     private static final byte P1_PUBLIC_KEY_Z = 0x02;
 
     /**
      * P1 parameter for the R values from the issuer public key.
      */
     private static final byte P1_PUBLIC_KEY_R = 0x03;
 
     /**
      * P1 parameter for the A value from a signature.
      */
     private static final byte P1_SIGNATURE_A = 0x01;
 
     /**
      * P1 parameter for the e value from a signature.
      */
     private static final byte P1_SIGNATURE_E = 0x02;
 
     /**
      * P1 parameter for the v value from a signature.
      */
     private static final byte P1_SIGNATURE_V = 0x03;
 
     /**
      * P1 parameter for the challenge of a proof.
      */
     private static final byte P1_SIGNATURE_PROOF_C = 0x04;
 
     /**
      * P1 parameter for the s_e response of a proof.
      */
     private static final byte P1_SIGNATURE_PROOF_S_E = 0x05;
 
     /**
      * P1 parameter for the challenge of a proof.
      */
     private static final byte P1_PROOF_C = 0x01;
     
     /**
      * P1 parameter for the vPrimeHat response of a proof.
      */
     private static final byte P1_PROOF_VPRIMEHAT = 0x02;
     
     /**
      * P1 parameter for the sHat response of a proof.
      */
     private static final byte P1_PROOF_SHAT = 0x03;
 
     /**
      * P1 parameter for the modulus of an RSA key.
      */
     private static final byte P1_RSA_MODULUS = 0x00;
 
     /**
      * P1 parameter for the exponent of an RSA key.
      */
     private static final byte P1_RSA_EXPONENT = 0x01;
 
     /**
      * P2 parameter for the attribute PIN.
      */
     public static final byte P2_PIN_ATTRIBUTE = 0x00;
 
     /**
      * P2 parameter for the administrative PIN.
      */
     public static final byte P2_PIN_ADMIN = 0x01;
 
     /**
      * Produces an unsigned byte-array representation of a BigInteger.
      *
      * <p>BigInteger adds an extra sign bit to the beginning of its byte
      * array representation.  In some cases this will cause the size
      * of the byte array to increase, which may be unacceptable for some
      * applications. This function returns a minimal byte array representing
      * the BigInteger without extra sign bits.
      *
      * <p>This method is taken from the Network Security Services for Java (JSS)
      * currently maintained by the Mozilla Foundation and originally developed
      * by the Netscape Communications Corporation.
      *
      * @return unsigned big-endian byte array representation of a BigInteger.
      */
     public static byte[] BigIntegerToUnsignedByteArray(BigInteger big) {
         byte[] ret;
 
         // big must not be negative
         assert(big.signum() != -1);
 
         // bitLength is the size of the data without the sign bit.  If
         // it exactly fills an integral number of bytes, that means a whole
         // new byte will have to be added to accommodate the sign bit. In
         // this case we need to remove the first byte.
         if(big.bitLength() % 8 == 0) {
             byte[] array = big.toByteArray();
             // The first byte should just be sign bits
             assert( array[0] == 0 );
             ret = new byte[array.length-1];
             System.arraycopy(array, 1, ret, 0, ret.length);
         } else {
             ret = big.toByteArray();
         }
         return ret;
     }
 
     /**
      * Fix the length of array representation of BigIntegers put into the APDUs.
      *
      * @param integer of which the length needs to be fixed.
      * @param the new length of the integer in bits
      * @return an array with a fixed length.
      */
     public static byte[] fixLength(BigInteger integer, int length_in_bits) {
         byte[] array = BigIntegerToUnsignedByteArray(integer);
         int length;
 
         length = length_in_bits/8;
         if (length_in_bits % 8 != 0){
         	length++;
         }
 
         assert (array.length <= length);
 
         int padding = length - array.length;
         byte[] fixed = new byte[length];
         Arrays.fill(fixed, (byte) 0x00);
         System.arraycopy(array, 0, fixed, padding, array.length);
         return fixed;
     }
 
     private static byte[] addTimeStamp(byte[] argument) {
     	int time = (int) ((new Date()).getTime() / 1000);
 		return ByteBuffer.allocate(argument.length + 4).put(argument)
 				.putInt(time).array();
 	}
 
     /**************************************************************************/
     /* IRMAcard Smart Card commands                                           */
     /**************************************************************************/
 
     public static ProtocolCommand selectApplicationCommand =
     		new ProtocolCommand(
     				"selectapplet",
     				"Select IRMAcard application",
     				 new CommandAPDU(ISO7816.CLA_ISO7816,
     			                INS_SELECT_APPLICATION, P1_SELECT_BY_NAME, 0x00, AID, 256)); // LE == 0 is required.
     public static ProtocolCommand selectApplicationCommand_0_7 =
     		new ProtocolCommand(
     				"selectapplet",
     				"Select IRMAcard application",
     				 new CommandAPDU(ISO7816.CLA_ISO7816,
     			                INS_SELECT_APPLICATION, P1_SELECT_BY_NAME, 0x00, AID_0_7, 256)); // LE == 0 is required.
 
     /**
      * Get the APDU commands for setting the specification of
      * a certificate issuance:
      * <ul>
      *   <li> issuer public key, and
      *   <li> context.
      * </ul>
      *
      * @param spec the specification to be set
      * @param id
      * @return
      */
     public static ProtocolCommands setIssuanceSpecificationCommands(IssuanceSpec spec, short id) {
     	ProtocolCommands commands = new ProtocolCommands();
 
     	commands.add(startIssuanceCommand(spec, id));
 
     	commands.addAll(setPublicKeyCommands(
     				spec.getPublicKey(),
     				spec.getCredentialStructure().getAttributeStructs().size() + 1));
     	return commands;
     }
 
     /**
      * Get the APDU commands for setting the specification of
      * a certificate issuance:
      * <ul>
      *   <li> issuer public key, and
      *   <li> context.
      * </ul>
      *
      * @param spec the specification to be set
      * @param id
      * @return
      */
     public static ProtocolCommands setIssuanceSpecificationCommands_0_7(IssuanceSpec spec, short id) {
     	ProtocolCommands commands = new ProtocolCommands();
 
     	commands.add(startIssuanceCommand_0_7(spec, id));
 
     	commands.addAll(setPublicKeyCommands(
     				spec.getPublicKey(),
     				spec.getCredentialStructure().getAttributeStructs().size() + 1));
     	return commands;
     }
 
     /**
      * Get the APDU commands for setting the public key on
      * the card.
      *
      * @param spec Issuance spec to get the public key from.
      * @return
      */
     public static ProtocolCommands setPublicKeyCommands(IssuerPublicKey pubKey, int pubKeyElements) {
     	int l_n = pubKey.getGroupParams().getSystemParams().getL_n();
 
     	ProtocolCommands commands = new ProtocolCommands();
     	commands.add(
     			new ProtocolCommand(
     					"publickey_n",
     					"Set public key (n)",
     					new CommandAPDU(
     			        		CLA_IRMACARD, INS_ISSUE_PUBLIC_KEY, P1_PUBLIC_KEY_N, 0x00,
     			                fixLength(pubKey.getN(), l_n))));
 
     	commands.add(
     			new ProtocolCommand(
     					"publickey_z",
     					"Set public key (Z)",
     					new CommandAPDU(
     			        		CLA_IRMACARD, INS_ISSUE_PUBLIC_KEY, P1_PUBLIC_KEY_Z, 0x00,
     			                fixLength(pubKey.getCapZ(), l_n))));
 
     	commands.add(
     			new ProtocolCommand(
     					"publickey_s",
     					"Set public key (S)",
     					new CommandAPDU(
     							CLA_IRMACARD, INS_ISSUE_PUBLIC_KEY, P1_PUBLIC_KEY_S, 0x00,
     							fixLength(pubKey.getCapS(), l_n))));
 
     	BigInteger[] pubKeyElement = pubKey.getCapR();
     	for (int i = 0; i < pubKeyElements; i++) {
         	commands.add(
         			new ProtocolCommand(
         					"publickey_element" + i,
         					"Set public key element (R@index " + i + ")",
         					new CommandAPDU(
         		            		CLA_IRMACARD, INS_ISSUE_PUBLIC_KEY, P1_PUBLIC_KEY_R, i,
         		            		fixLength(pubKeyElement[i], l_n))));
     	}
 
     	return commands;
     }
 
 
     /**
      * Get the APDU commands to start issuance.
      *
      * @param spec Issuance specification
      * @param id id of credential
      * @return
      */
     public static ProtocolCommand startIssuanceCommand(IssuanceSpec spec, short id) {
     	int l_H = spec.getPublicKey().getGroupParams().getSystemParams().getL_H();
 
     	// FIXME: flags set to 0 for now
         IdemixFlags flags = new IdemixFlags();
         byte[] flagBytes = flags.getFlagBytes();
 
         byte[] data = new byte[4 + flagBytes.length + l_H/8];
         data[0] = (byte) (id >> 8);
         data[1] = (byte) (id & 0x00ff);
         data[2] = (byte) (spec.getCredentialStructure().getAttributeStructs().size() >> 8);
         data[3] = (byte) (spec.getCredentialStructure().getAttributeStructs().size() & 0xff);
         System.arraycopy(flagBytes, 0, data, 4, flagBytes.length);
         System.arraycopy(fixLength(spec.getContext(), l_H), 0, data, 4 + flagBytes.length, l_H/8);
         data = addTimeStamp(data);
 
     	return new ProtocolCommand(
                                 "start_issuance",
                                 "Start credential issuance.",
                                 new CommandAPDU(
                                     CLA_IRMACARD, INS_ISSUE_CREDENTIAL, 0x00, 0x00, data),
     			        new ProtocolErrors(
                                     0x00006986,"Credential already issued."));
     }
 
     /**
      * Get the APDU commands to start issuance. (0.7 version)
      *
      * @param spec Issuance specification
      * @param id id of credential
      * @return
      */
     public static ProtocolCommand startIssuanceCommand_0_7(IssuanceSpec spec, short id) {
     	int l_H = spec.getPublicKey().getGroupParams().getSystemParams().getL_H();
 
     	// FIXME: flags set to 0 for now
     	IdemixFlags flags = new IdemixFlags();
     	byte[] flagBytes = flags.getFlagBytes();
 
     	byte[] data = new byte[2 + l_H/8 + 2 + flagBytes.length];
     	data[0] = (byte) (id >> 8);
     	data[1] = (byte) (id & 0xff);
     	System.arraycopy(fixLength(spec.getContext(), l_H), 0, data, 2, l_H/8);
     	data[l_H/8 + 2] = (byte) (spec.getCredentialStructure().getAttributeStructs().size() >> 8);
     	data[l_H/8 + 3] = (byte) (spec.getCredentialStructure().getAttributeStructs().size() & 0xff);
     	System.arraycopy(flagBytes, 0, data, l_H/8 + 4, flagBytes.length);
 
     	return new ProtocolCommand(
     					"start_issuance",
     					"Start credential issuance.",
     					new CommandAPDU(
     			        		CLA_IRMACARD, INS_ISSUE_CREDENTIAL, 0x00, 0x00, addTimeStamp(data)),
     			        new ProtocolErrors(
     			        		0x00006986,"Credential already issued."));
     }
 
     /**
      * Get the APDU commands to start proof.
      *
      * @param spec Proof specification
      * @param id id of credential
      * @return
      */
     public static ProtocolCommand startProofCommand(ProofSpec spec, short id, short D) {
     	int l_H = spec.getGroupParams().getSystemParams().getL_H();
 
         byte[] data = new byte[4 + l_H/8];
         data[0] = (byte) (id >> 8);
         data[1] = (byte) (id & 0xff);
         data[2] = (byte) (D >> 8);
         data[3] = (byte) (D & 0xff);
         System.arraycopy(fixLength(spec.getContext(), l_H), 0, data, 4, l_H/8);
         data = addTimeStamp(data);
 
         return new ProtocolCommand(
                                 "startprove",
                                 "Start credential proof.",
                                 new CommandAPDU(
                                     CLA_IRMACARD, INS_PROVE_CREDENTIAL, 0x00, 0x00, data),
     			        new ProtocolErrors(
                                     0x00006A88,"Credential not found."));
     }
 
     /**
      * Get the APDU commands to start proof. (0.7 version)
      *
      * @param spec Proof specification
      * @param id id of credential
      * @return
      */
     public static ProtocolCommand startProofCommand_0_7(ProofSpec spec, short id, short D) {
     	int l_H = spec.getGroupParams().getSystemParams().getL_H();
 
     	byte[] data = new byte[2 + l_H/8 + 2];
     	data[0] = (byte) (id >> 8);
     	data[1] = (byte) (id & 0xff);
     	System.arraycopy(fixLength(spec.getContext(), l_H), 0, data, 2, l_H/8);
     	data[l_H/8 + 2] = (byte) (D >> 8);
     	data[l_H/8 + 3] = (byte) (D & 0xff);
     	 
     	return
     			new ProtocolCommand(
     					"startprove",
     					"Start credential proof.",
     					new CommandAPDU(
     			        		CLA_IRMACARD, INS_PROVE_CREDENTIAL, 0x00, 0x00, addTimeStamp(data)),
     			        new ProtocolErrors(
     			        		0x00006A88,"Credential not found."));
     }
 
     public static ProtocolCommand generateMasterSecretCommand =
     			new ProtocolCommand(
     					"generatesecret",
     					"Generate master secret",
     					new CommandAPDU(
     			        		CLA_IRMACARD, INS_GENERATE_SECRET, 0x00, 0x00),
     			        new ProtocolErrors(
     			        		0x00006986,"Master secret already set."));
 
     public static ProtocolCommands initialiseAuthenticationKey(RSAPrivateKey key) {
     	ProtocolCommands commands = new ProtocolCommands();
     	
     	commands.add(new ProtocolCommand(
     			"initauthmod",
     			"Initialise the RSA modulus of the authentication key",
     			new CommandAPDU(
     					CLA_IRMACARD, INS_AUTHENTICATION_SECRET, P1_RSA_MODULUS, 0x00,
     					fixLength(key.getModulus(), 128))
     			));
     	
     	commands.add(new ProtocolCommand(
     			"initauthexp",
     			"Initialise the RSA exponent of the authentication key",
     			new CommandAPDU(
     					CLA_IRMACARD, INS_AUTHENTICATION_SECRET, P1_RSA_EXPONENT, 0x00,
     					fixLength(key.getPrivateExponent(), 128))
     			));
     	
     	return commands;
     }
 
     public static ProtocolCommand sendPinCommand(byte pinID, byte[] pin) {
     	byte[] pinBytes = new byte[8];
     	System.arraycopy(pin, 0, pinBytes, 0, pin.length);
     	
     	return
     			new ProtocolCommand(
     					"sendpin",
     					"Authorize using PIN",
     					new CommandAPDU(
     			        		ISO7816.CLA_ISO7816, ISO7816.INS_VERIFY, 0x00, pinID, pinBytes)
     					);
     }
 
     public static ProtocolCommand queryPinCommand(byte pinID) {
     	return
     			new ProtocolCommand(
     					"querypin",
     					"Query PIN verification status",
     					new CommandAPDU(
     			        		ISO7816.CLA_ISO7816, ISO7816.INS_VERIFY, 0x00, pinID)
     					);
     }
 
 	public static ProtocolCommand updatePinCommand(byte pinID, byte[] oldPin, byte[] newPin) {
		byte[] pinBytes = new byte[16];
		System.arraycopy(oldPin, 0, pinBytes, 0, oldPin.length);
		System.arraycopy(newPin, 0, pinBytes, 8, newPin.length);
 		
     	return
     			new ProtocolCommand(
     					"updatepin",
     					"Update current PIN",
     					new CommandAPDU(
     			        		ISO7816.CLA_ISO7816, ISO7816.INS_CHANGE_CHV, 0x00, pinID, pinBytes)
     					);
 	}
 
     /**
      * Get the APDU commands for setting the attributes:
      *
      * <pre>
      *   m_1, ..., m_l
      * </pre>
      *
      * @param spec the issuance specification for the ordering of the values.
      * @param values the attributes to be set.
      * @return
      */
     public static ProtocolCommands setAttributesCommands(IssuanceSpec spec, Values values) {
     	ProtocolCommands commands = new ProtocolCommands();
         Vector<AttributeStructure> structs = spec.getCredentialStructure().getAttributeStructs();
         int L_m = spec.getPublicKey().getGroupParams().getSystemParams().getL_m();
         int i = 1;
         for (AttributeStructure struct : structs) {
         	BigInteger attr = (BigInteger) values.get(struct.getName()).getContent();
         	commands.add(
         			new ProtocolCommand(
         					"setattr"+i,
         					"Set attribute (m@index" + i + ")",
         					new CommandAPDU(
         		            		CLA_IRMACARD, INS_ISSUE_ATTRIBUTES, i, 0x00,
         		                    fixLength(attr, L_m))));
         	i += 1;
         }
     	return commands;
     }
 
 
     public static ProtocolCommands round1Commands(IssuanceSpec spec, final Message msg) {
     	ProtocolCommands commands = new ProtocolCommands();
         BigInteger theNonce1 = msg.getIssuanceElement(
                 IssuanceProtocolValues.nonce);
         int L_Phi = spec.getPublicKey().getGroupParams().getSystemParams().getL_Phi();
     	commands.add(
     			new ProtocolCommand(
     					"nonce_n1",
     					"Issue nonce n1",
     					new CommandAPDU(
     		            		CLA_IRMACARD, INS_ISSUE_COMMITMENT, 0x00, 0x00,
     		                    fixLength(theNonce1,L_Phi))));
     	commands.add(
     			new ProtocolCommand(
     					"proof_c",
     					"Issue proof c",
     					new CommandAPDU(
     		            		CLA_IRMACARD, INS_ISSUE_COMMITMENT_PROOF, P1_PROOF_C, 0x00)));
 
     	commands.add(
     			new ProtocolCommand(
     					"vHatPrime",
     					"Issue proof v^'",
     					new CommandAPDU(
     		            		CLA_IRMACARD, INS_ISSUE_COMMITMENT_PROOF, P1_PROOF_VPRIMEHAT, 0x00)));
     	commands.add(
     			new ProtocolCommand(
     					"proof_s_A",
     					"Issue proof s_A",
     					new CommandAPDU(
     		            		CLA_IRMACARD, INS_ISSUE_COMMITMENT_PROOF, P1_PROOF_SHAT, 0x00)));
     	commands.add(
     			new ProtocolCommand(
     					"nonce_n2",
     					"Issue nonce n2",
     					new CommandAPDU(
     		            		CLA_IRMACARD, INS_ISSUE_CHALLENGE, 0x00, 0x00)));
     	return commands;
     }
 
     public static Message processRound1Responses(ProtocolResponses responses) {
     	HashMap<IssuanceProtocolValues, BigInteger> issuanceProtocolValues =
                 new HashMap<IssuanceProtocolValues, BigInteger>();
         TreeMap<String, BigInteger> additionalValues =
                 new TreeMap<String, BigInteger>();
         HashMap<String, SValue> sValues = new HashMap<String, SValue>();
 
     	issuanceProtocolValues.put(IssuanceProtocolValues.capU,
                 new BigInteger(1, responses.get("nonce_n1").getData()));
 
     	BigInteger challenge = new BigInteger(1, responses.get("proof_c").getData());
 
         additionalValues.put(IssuanceSpec.vHatPrime,
                 new BigInteger(1, responses.get("vHatPrime").getData()));
 
         sValues.put(IssuanceSpec.MASTER_SECRET_NAME,
                 new SValue(new BigInteger(1, responses.get("proof_s_A").getData())));
 
         issuanceProtocolValues.put(IssuanceProtocolValues.nonce,
                 new BigInteger(1, responses.get("nonce_n2").getData()));
 
         // Return the next protocol message
         return new Message(issuanceProtocolValues,
                 new Proof(challenge, sValues, additionalValues));
     }
 
 
     public static ProtocolCommands round3Commands(IssuanceSpec spec, final Message msg) {
     	ProtocolCommands commands = new ProtocolCommands();
     	SystemParameters sysPars = spec.getPublicKey().getGroupParams().getSystemParams();
 
     	BigInteger A = msg.getIssuanceElement(IssuanceProtocolValues.capA);
     	commands.add(
     			new ProtocolCommand(
     					"signature_A",
     					"Issue signature A",
     					new CommandAPDU(
     		            		CLA_IRMACARD, INS_ISSUE_SIGNATURE, P1_SIGNATURE_A, 0x00,
     		                    fixLength(A, sysPars.getL_n()))));
     	BigInteger e = msg.getIssuanceElement(IssuanceProtocolValues.e);
     	commands.add(
     			new ProtocolCommand(
     					"signature_e",
     					"Issue signature e",
     					new CommandAPDU(
     		            		CLA_IRMACARD, INS_ISSUE_SIGNATURE, P1_SIGNATURE_E, 0x00,
     		                    fixLength(e, sysPars.getL_e()))));
     	BigInteger v = msg.getIssuanceElement(IssuanceProtocolValues.vPrimePrime);
     	commands.add(
     			new ProtocolCommand(
     					"vPrimePrime",
     					"Issue signature v''",
     					new CommandAPDU(
     							CLA_IRMACARD, INS_ISSUE_SIGNATURE, P1_SIGNATURE_V, 0x00,
     		                    fixLength(v, sysPars.getL_v()))));
     	BigInteger c = msg.getProof().getChallenge();
     	commands.add(
     			new ProtocolCommand(
     					"proof_c",
     					"Issue proof c'",
     					new CommandAPDU(
                                 CLA_IRMACARD, INS_ISSUE_SIGNATURE, P1_SIGNATURE_PROOF_C, 0x00,
     		                    fixLength(c, sysPars.getL_H()))));
     	BigInteger s_e =
         		(BigInteger) msg.getProof().getSValue(IssuanceSpec.s_e).getValue();
     	commands.add(
     			new ProtocolCommand(
     					"proof_s_e",
     					"Issue proof s_e",
     					new CommandAPDU(
                                 CLA_IRMACARD, INS_ISSUE_SIGNATURE, P1_SIGNATURE_PROOF_S_E, 0x00,
     		                    fixLength(s_e, sysPars.getL_n()))));
     	commands.add(
     			new ProtocolCommand(
     					"issue_verify",
     					"Verify issuance results (signature & proof)",
     					new CommandAPDU(
     		            		CLA_IRMACARD, INS_ISSUE_VERIFY, 0x00, 0x00)));
     	return commands;
     }
 
 
 
     public static ProtocolCommands buildProofCommands(final BigInteger nonce, final ProofSpec spec, short id) {
     	ProtocolCommands commands = new ProtocolCommands();
         // Set the system parameters
     	SystemParameters sysPars = spec.getGroupParams().getSystemParams();
 
         Predicate predicate = spec.getPredicates().firstElement();
         if (predicate.getPredicateType() != PredicateType.CL) {
             throw new RuntimeException("Unimplemented predicate.");
         }
         CLPredicate pred = ((CLPredicate) predicate);
         CredentialStructure cred = (CredentialStructure) 
         		StructureStore.getInstance().get(pred.getCredStructLocation());
 
         // Determine the disclosure selection bitmask
         short D = 0;
         for (AttributeStructure attribute : cred.getAttributeStructs()) {
             Identifier identifier = pred.getIdentifier(attribute.getName());
             if (identifier.isRevealed()) {
             	D |= 1 << attribute.getKeyIndex();
             }
         }
 
         commands.add(
         		startProofCommand(spec, id, D));
         commands.add(
         		new ProtocolCommand(
         				"challenge_c",
         				"Send challenge n1",
         				new CommandAPDU(CLA_IRMACARD, INS_PROVE_COMMITMENT, 0x00, 0x00,
         	                    fixLength(nonce, sysPars.getL_Phi()))));
         commands.add(
         		new ProtocolCommand(
         				"signature_A",
         				"Get random signature A",
         				new CommandAPDU(CLA_IRMACARD, INS_PROVE_SIGNATURE, P1_SIGNATURE_A, 0x00)));
         commands.add(
         		new ProtocolCommand(
         				"signature_e",
         				"Get random signature e^",
         				new CommandAPDU(CLA_IRMACARD, INS_PROVE_SIGNATURE, P1_SIGNATURE_E, 0x00)));
         commands.add(
         		new ProtocolCommand(
         				"signature_v",
         				"Get random signature v^",
             				new CommandAPDU(CLA_IRMACARD, INS_PROVE_SIGNATURE, P1_SIGNATURE_V, 0x00)));
         commands.add(
         		new ProtocolCommand(
         				"master",
         				"Get random value (@index 0).",
         				new CommandAPDU(CLA_IRMACARD, INS_PROVE_ATTRIBUTE, 0x00, 0x00)));
 
         // iterate over all the identifiers
         for (AttributeStructure attribute : cred.getAttributeStructs()) {
             String attName = attribute.getName();
             Identifier identifier = pred.getIdentifier(attName);
             int i = attribute.getKeyIndex();
         	commands.add(
             		new ProtocolCommand(
             				"attr_"+attName,
             				(identifier.isRevealed() ? "Get disclosed attribute" : "Get random value") + " (@index " + i + ").",
             				new CommandAPDU(CLA_IRMACARD, INS_PROVE_ATTRIBUTE, i, 0x00)));
         }
     	return commands;
     }
 
     public static ProtocolCommands buildProofCommands_0_7(final BigInteger nonce, final ProofSpec spec, short id) {
     	ProtocolCommands commands = new ProtocolCommands();
         // Set the system parameters
     	SystemParameters sysPars = spec.getGroupParams().getSystemParams();
 
         Predicate predicate = spec.getPredicates().firstElement();
         if (predicate.getPredicateType() != PredicateType.CL) {
             throw new RuntimeException("Unimplemented predicate.");
         }
         CLPredicate pred = ((CLPredicate) predicate);
         CredentialStructure cred = (CredentialStructure) 
         		StructureStore.getInstance().get(pred.getCredStructLocation());
 
         // Determine the disclosure selection bitmask
         short D = 0;
         for (AttributeStructure attribute : cred.getAttributeStructs()) {
             Identifier identifier = pred.getIdentifier(attribute.getName());
             if (identifier.isRevealed()) {
             	D |= 1 << attribute.getKeyIndex();
             }
         }
 
         commands.add(
         		startProofCommand_0_7(spec, id, D));
         commands.add(
         		new ProtocolCommand(
         				"challenge_c",
         				"Send challenge n1",
         				new CommandAPDU(CLA_IRMACARD, INS_PROVE_COMMITMENT, 0x00, 0x00,
         	                    fixLength(nonce, sysPars.getL_Phi()))));
         commands.add(
         		new ProtocolCommand(
         				"signature_A",
         				"Get random signature A",
         				new CommandAPDU(CLA_IRMACARD, INS_PROVE_SIGNATURE, P1_SIGNATURE_A, 0x00)));
         commands.add(
         		new ProtocolCommand(
         				"signature_e",
         				"Get random signature e^",
         				new CommandAPDU(CLA_IRMACARD, INS_PROVE_SIGNATURE, P1_SIGNATURE_E, 0x00)));
         commands.add(
         		new ProtocolCommand(
         				"signature_v",
         				"Get random signature v^",
             				new CommandAPDU(CLA_IRMACARD, INS_PROVE_SIGNATURE, P1_SIGNATURE_V, 0x00)));
         commands.add(
         		new ProtocolCommand(
         				"master",
         				"Get random value (@index 0).",
         				new CommandAPDU(CLA_IRMACARD, INS_PROVE_ATTRIBUTE, 0x00, 0x00)));
 
         // iterate over all the identifiers
         for (AttributeStructure attribute : cred.getAttributeStructs()) {
             String attName = attribute.getName();
             Identifier identifier = pred.getIdentifier(attName);
             int i = attribute.getKeyIndex();
         	commands.add(
             		new ProtocolCommand(
             				"attr_"+attName,
             				(identifier.isRevealed() ? "Get disclosed attribute" : "Get random value") + " (@index " + i + ").",
             				new CommandAPDU(CLA_IRMACARD, INS_PROVE_ATTRIBUTE, i, 0x00)));
         }
     	return commands;
     }
 
     public static Proof processBuildProofResponses(ProtocolResponses responses, final ProofSpec spec) {
         HashMap<String, SValue> sValues = new HashMap<String, SValue>();
         TreeMap<String, BigInteger> commonList = new TreeMap<String, BigInteger>();
 
         Predicate predicate = spec.getPredicates().firstElement();
         if (predicate.getPredicateType() != PredicateType.CL) {
             throw new RuntimeException("Unimplemented predicate.");
         }
         CLPredicate pred = ((CLPredicate) predicate);
         StructureStore store = StructureStore.getInstance();
         CredentialStructure cred = (CredentialStructure) store.get(
                pred.getCredStructLocation());
 
 
         BigInteger challenge = new BigInteger(1, responses.get("challenge_c").getData());
 
         commonList.put(pred.getTempCredName(),
         				new BigInteger(1, responses.get("signature_A").getData()));
 
         sValues.put(pred.getTempCredName(),
         		new SValue(
         				new SValuesProveCL(
         						new BigInteger(1, responses.get("signature_e").getData()),
         						new BigInteger(1, responses.get("signature_v").getData())
         						)));
 
         sValues.put(IssuanceSpec.MASTER_SECRET_NAME,
         		new SValue(new BigInteger(1, responses.get("master").getData())));
 
         for (AttributeStructure attribute : cred.getAttributeStructs()) {
         	String attName = attribute.getName();
             Identifier identifier = pred.getIdentifier(attName);
             sValues.put(identifier.getName(),
             		new SValue(new BigInteger(1, responses.get("attr_" + attName).getData())));
         }
 
         // Return the generated proof, based on the proof specification
         return new Proof(challenge, sValues, commonList);
     }
     
 	public static ProtocolCommand getCredentialsCommand() {
 		return new ProtocolCommand(
 			"getcredentials", 
 			"Get list of credentials",
 			new CommandAPDU(CLA_IRMACARD, INS_ADMIN_CREDENTIALS, 0x00, 0x00));
 	}
 
 	public static ProtocolCommand selectCredentialCommand(short id) {
         byte[] data = new byte[2];
         data[0] = (byte) (id >> 8);
         data[1] = (byte) (id & 0xff);
 
 		return new ProtocolCommand(
 				"selectcredential",
 				"Select a credential for further modifications",
 				new CommandAPDU(CLA_IRMACARD, INS_ADMIN_CREDENTIAL, 0, 0, data));
 	}
 
 	public static ProtocolCommands getAttributesCommands(IssuanceSpec spec) {
 		ProtocolCommands commands = new ProtocolCommands();
 		for (AttributeStructure attribute : spec.getCredentialStructure()
 				.getAttributeStructs()) {
 			String attName = attribute.getName();
 			int i = attribute.getKeyIndex();
 			commands.add(new ProtocolCommand(
 				"attr_" + attName,
 				"Get attribute (@index " + i + ")", 
 				new CommandAPDU(CLA_IRMACARD, INS_ADMIN_ATTRIBUTE, i, 0x00)));
 		}
 		return commands;
 	}
 
 	public static ProtocolCommand removeCredentialCommand(short id) {
 		byte[] empty = {};
 		return new ProtocolCommand(
 			"removecredential", 
 			"Remove credential (id " + id + ")", 
 			new CommandAPDU(CLA_IRMACARD, INS_ADMIN_REMOVE, 0, 0, addTimeStamp(empty)));
 	}
 
 	public static ProtocolCommand getCredentialFlagsCommand() {
 		return new ProtocolCommand(
 			"getcredflags", 
 			"Get credential flags",
 			new CommandAPDU(CLA_IRMACARD, INS_ADMIN_FLAGS, 0, 0));
 	}
 
 	public static ProtocolCommand getLogCommand(byte idx) {
 		return new ProtocolCommand(
 			"getlog",
 			"Get logs",
 			new CommandAPDU(CLA_IRMACARD, INS_ADMIN_LOG, idx, 0));
 	}
 
 	public static ProtocolCommand setCredentialFlagsCommand(IdemixFlags flags) {
 		return new ProtocolCommand(
 			"setcredflags", 
 			"Set credential flags (" + flags + ")", 
 			new CommandAPDU(CLA_IRMACARD, INS_ADMIN_FLAGS, 0,0, flags.getFlagBytes()));
 	}
 
 	public static ProtocolCommands verifyCertificateCommands(Certificate cert) throws CertificateEncodingException {
 		ProtocolCommands commands = new ProtocolCommands();
 		byte[] certBytes = cert.getEncoded();
 
 		for (int offset = 0; offset < certBytes.length - 1; offset += 255) {
 			commands.add(new ProtocolCommand(
 				"cert_" + offset,
 				"Verify certificate (@offset " + offset + ")",
 				new CommandAPDU(ISO7816.CLA_ISO7816 | ((offset + 255 < certBytes.length - 1) ? CLA_COMMAND_CHAINING : 0x00), ISO7816.INS_PSO, 0x00, 0xBE, Arrays.copyOfRange(certBytes, offset, Math.min(offset + 255, certBytes.length)))));
 		}
 
 		return commands;
 	}
 
 	public static ProtocolCommands setCAKeyCommands(RSAPublicKey caKey) {
 		ProtocolCommands commands = new ProtocolCommands();
 
 		commands.add(new ProtocolCommand(
 				"caExp",
 				"Set CA public key exponent",
 				new CommandAPDU(CLA_IRMACARD, INS_AUTHENTICATION_SECRET, 2, 0, fixLength(caKey.getPublicExponent(), 1024))));
 
 		commands.add(new ProtocolCommand(
 				"caMod",
 				"Set CA public key modulus",
 				new CommandAPDU(CLA_IRMACARD, INS_AUTHENTICATION_SECRET, 3, 0, fixLength(caKey.getModulus(), 1024))));
 
 		return commands;
 	}
 }
