 /*************************************************************************
  * 
  *  Copyright 2009 by Giuseppe Castagno beppec56@openoffice.org
  *  
  *  The Contents of this file are made available subject to
  *  the terms of European Union Public License (EUPL) version 1.1
  *  as published by the European Community.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the EUPL.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  EUPL for more details.
  *
  *  You should have received a copy of the EUPL along with this
  *  program.  If not, see:
  *  https://www.osor.eu/eupl, http://ec.europa.eu/idabc/eupl.
  *
  ************************************************************************/
 
 package it.plio.ext.oxsit.ooo.ui;
 
 import it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor.TreeNodeType;
 
 import java.util.HashMap;
 
 import com.sun.star.lang.XEventListener;
 
 /**
  * contains the certificate data, common for
  * some of the tree nodes.
  * In this implementation we have only some fake strings, not the
  * actual certificate data.
  * 
  * @author beppe
  *
  */
 public class CertificateData {
 	/**
 	 * defines the state of the node, for display purposes
 	 * at every state is associated a graphic element for display
 	 * or none, if this is the case  
 	 * @author beppec56
 	 *
 	 */
 	public enum ElementState {
 		ELEMENT_NONE, //no graphic indication needed
 		ELEMENT_OK,
 		ELEMENT_WARNING,
 		ELEMENT_ERROR,
 		ELEMENT_BROKEN
 	};
 
 
 	public static final int m_nTITLE			= 0;
 	public static final int m_nEMPTY_FIELD_01	= 1;
 	public static final int m_nINFO_LINE1	= 2;
 	public static final int m_nINFO_LINE2	= 3;
 	public static final int m_nTITLE_SCOPE		= 4;
 	public static final int m_nSCOPE_LINE1	= 5;
 	public static final int m_nSCOPE_LINE2	= 6;
 	public static final int m_nSCOPE_LINE3	= 7;
 	public static final int m_nSCOPE_LINE4	= 8;
 	public static final int m_nCERT_VALIDITY_STATE	= 9;
 	public static final int m_nCERT_VALIDITY_ERROR	= 10;
 	public static final int m_nCERT_VALIDITY_VERIFICATION_CONDITIONS	= 11;
 	public static final int m_nDATE_VALID_FROM	= 12;
 	public static final int m_nDATE_VALID_TO	= 13;
 
 	public static final int m_nMAXIMUM_FIELDS 				= 14;
 	
 	//strings for certificate status
 	
 	public static final String m_sCERTIFICATE_VERIFIED = "bCertificato verificato con CRL";
 	
 	//state of the certificate control
 //	public static final int m_nCERTIFICATE_VERIFIED 
 	
 	public final String[] m_sCertificateValidityState = {
 		"bCertificato verificato e valido, CA accreditata.",
 		"bCERTIFICATO NON VALIDO.",
 		"bCERTIFICATO SCADUTO.",
 	};
 
 	public final String[] m_sCertificateValidityVerificationConditions = {
 			"bVERIFICA CRL NON ATTIVA.",
 			"bVERIFICA CRL ATTIVA MA NON ACCESSIBILE.",
 			"bVERIFICA CRL ATTIVA E FUNZIONANTE."
 		};
 	
 	public final String[] m_sCertificateValidityStatus = {
 			"bVERIFICA CRL NON ATTIVA.",
 			"bCertificato non verificabile con CRL."
 		};
 	
 	public final String m_sEmptyField = "r(this field is empty)";
 	                    
 	// contains the certificate strings definitions
 	private HashMap<TreeNodeDescriptor.TreeNodeType, String[]>	aCertStrings;
 
 	/**
 	 * this class will fill the certificate data with all the information needed
 	 * 
 	 * 
 	 */
 	public CertificateData() {
 		// TODO Auto-generated constructor stub
 		aCertStrings = new HashMap<TreeNodeDescriptor.TreeNodeType, String[]>(15);
 		// now init the certificate with dummy data
 		// for every string meant for the multi fixed text display
 		// devise a method to change the character font strike
 		// the first character in the string marks the stroke type
 		// (see function: it.plio.ext.oxsit.ooo.ui.TreeNodeDescriptor.EnableDisplay(boolean) for
 		// for information on how they are interpreted):
 		// b	the string will be in bold
 		// B
 		// r
 		// s    the string will striken out regular
 		// S    the string will striken out bold
 		//	
 		{
 			String[] saDummy = new String[m_nMAXIMUM_FIELDS];			
 
 			saDummy[m_nTITLE] = "bInformazioni sul certificato";
 			saDummy[m_nEMPTY_FIELD_01] = "";
 			saDummy[m_nINFO_LINE1] = "rIl certificato è adatto per la firma digitale.";
 			saDummy[m_nINFO_LINE2] = "r(non ripudio attivato)";
 			saDummy[m_nTITLE_SCOPE] = "bIl certificato è stato rilasciato per i seguenti scopi:";
 			saDummy[m_nSCOPE_LINE1] = "...";
 			saDummy[m_nSCOPE_LINE2] = "...";
 			saDummy[m_nSCOPE_LINE3] = "...";
 			saDummy[m_nSCOPE_LINE4] = "";
 			saDummy[m_nCERT_VALIDITY_STATE] = "bCertificato verificato con CRL";
 			saDummy[m_nCERT_VALIDITY_ERROR] = "";
 			saDummy[m_nCERT_VALIDITY_VERIFICATION_CONDITIONS] = "";
 			saDummy[m_nDATE_VALID_FROM] = "bValido dal 10/04/2006 16:06:04";
 			saDummy[m_nDATE_VALID_TO] = "bal 10/04/2009 16:06:04";
 			setCertString(TreeNodeType.CERTIFICATE, saDummy);
 		}
 		//subject TreeNodeType.SUBJECT inserted by the derived classes
 		{
 			String[] saDummy = {"V3"};
 			setCertString(TreeNodeType.VERSION, saDummy);
 		}
 		{
 			String[] saDummy = {"15 BA 35"};
 			setCertString(TreeNodeType.SERIAL_NUMBER, saDummy);
 		}
 		{
 			String[] saDummy = {
 					"CN=The Certification Authority\nOU=The Certification Authority\nserialNumber=02313821007\nO=The Certification Wizards\nC=IT"
 					};
 			setCertString(TreeNodeType.ISSUER,saDummy);
 		}
 		{
 			String[] saDummy = {
 					"10/04/2006 16:06:04",
 			};
 			setCertString(TreeNodeDescriptor.TreeNodeType.VALID_FROM,saDummy);
 		}
 		{
 			String[] saDummy = {
 					"10/04/2009 16:06:04",
 			};
 			setCertString(TreeNodeDescriptor.TreeNodeType.VALID_TO,saDummy);
 		}
 		{
 			String[] saDummy = {
 				"dnQualifier=2006111605A1528\nSN=GIACOMO\ngivenName=VERDI\nE=giacomo.verdi@acme.it\nserialNumber=GCMVRD01D12210Y\nCN=\"GCMVRD01D12210Y/7420091000049623.fmIW78mgkUVdmdQuXCrZbDsW9oQ=\"\nOU=C.C.I.A.A. DI TORINO\nO=Non Dichiarato\nC=IT",
 			};
 			setCertString(TreeNodeDescriptor.TreeNodeType.SUBJECT,saDummy);
 		}
 		{
 			String[] saDummy = {
 				"PKCS #1 RSA Encryption (rsaEncryption)",
 			};
 			setCertString(TreeNodeDescriptor.TreeNodeType.SUBJECT_ALGORITHM,saDummy);
 		}
 		{
 			String[] saDummy = {
 				"30 82 01 0A 02 82 01 01 00 C4 1C 77 1D AD 89 18\nB1 6E 88 20 49 61 E9 AD 1E 3F 7B 9B 2B 39 A3 D8\nBF F1 42 E0 81 F0 03 E8 16 26 33 1A B1 DC 99 97\n4C 5D E2 A6 9A B8 D4 9A 68 DF 87 79 0A 98 75 F8\n33 54 61 71 40 9E 49 00 00 06 51 42 13 33 5C 6C\n34 AA FD 6C FA C2 7C 93 43 DD 8D 6F 75 0D 51 99\n83 F2 8F 4E 86 3A 42 22 05 36 3F 3C B6 D5 4A 8E\nDE A5 DC 2E CA 7B 90 F0 2B E9 3B 1E 02 94 7C 00\n8C 11 A9 B6 92 21 99 B6 3A 0B E6 82 71 E1 7E C2\nF5 1C BD D9 06 65 0E 69 42 C5 00 5E 3F 34 3D 33\n2F 20 DD FF 3C 51 48 6B F6 74 F3 A5 62 48 C9 A8\nC9 73 1C 8D 40 85 D4 78 AF 5F 87 49 4B CD 42 08\n5B C7 A4 D1 80 03 83 01 A9 AD C2 E3 63 87 62 09\nFE 98 CC D9 82 1A CB AD 41 72 48 02 D5 8A 76 C0\nD5 59 A9 FF CA 3C B5 0C 1E 04 F9 16 DB AB DE 01\nF7 A0 BE CF 94 2A 53 A4 DD C8 67 0C A9 AF 60 5F\n53 3A E1 F0 71 7C D7 36 AB 02 03 01 00 01",
 			};
 			setCertString(TreeNodeDescriptor.TreeNodeType.PUBLIC_KEY,saDummy);
 		}
 		{
 			String[] saDummy = {
 				"PKCS #1 SHA-1 With RSA Encryption (sha1WithRSAEncryption)",
 			};
 			setCertString(TreeNodeDescriptor.TreeNodeType.SIGNATURE_ALGORITHM,saDummy);
 		}		
 		{
 			String[] saDummy = {	"6F 59 05 59 E6 FB 45 8F D4 C3 2D CB 8C 4C 55 02\nDB 5A 42 39 ",
 			};
 			setCertString(TreeNodeType.THUMBPRINT_SHA1,saDummy);
 		}
 		{
 			String[] saDummy = {	"6F B2 8C 96 83 3C 65 26 6F 7D CF 74 3F E7 E4 AD",
 			};
 			setCertString(TreeNodeType.THUMBPRINT_MD5,saDummy);
 		}
 
 		{
 			String[] saDummy = {	"Non Repudiation",
 			};
 			setCertString(TreeNodeType.X509V3_KEY_USAGE,saDummy);
 		}
 		{
 			String[] saDummy = {
 					"Certificato qualificato (O.I.D. 0.4.0.1862.1.1)\nPeriodo conservazione informazioni relative alla emissione del cerificato qualificato: 20 anni (O.I.D. 0.4.0.1862.1.3)\nDispositivo sicuro (O.I.D. 0.4.0.1862.1.4)"
 					};
 			setCertString(TreeNodeType.QC_STATEMENTS,saDummy);
 		}
 
 		{
 			String[] saDummy = {
 					"OCSP - URI:http://ocsp.infocert.it/OCSPServer_ICE/OCSPServlet"
 					};
 			setCertString(TreeNodeType.AUTHORITY_INFORMATION_ACCESS,saDummy);
 		}
 
 		{
 			String[] saDummy = {
 					"Policy: 1.3.76.14.1.1.1\nCPS: http://www.card.infocamere.it/firma/cps/cps.htm"
 					};
 			setCertString(TreeNodeType.X509V3_CERTIFICATE_POLICIES,saDummy);
 		}
 		{
 			String[] saDummy = {
 					"19560415000000Z"
 					};
 			setCertString(TreeNodeType.X509V3_SUBJECT_DIRECTORY_ATTRIBUTES,saDummy);
 		}
 
 		{
 			String[] saDummy = {
 					"URI:ldap://ldap.infocamere.it/cn%3DInfoCamere%20Firma%20Digitale%2Cou%3DCertificati%20di%20Firma%2Co%3DInfoCamere%20SCpA%2Cc%3DIT?cacertificate, email:firma"
 					};
 			setCertString(TreeNodeType.X509V3_ISSUER_ALTERNATIVE_NAME,saDummy);
 		}
 		
 		{
 			String[] saDummy = {
 					"keyid:9C:6F:E1:76:68:27:42:9C:C0:80:40:70:A0:0F:08:E9:D1:12:FF:A4"
 					};
 			setCertString(TreeNodeType.X509V3_AUTHORITY_KEY_IDENTIFIER,saDummy);
 		}
 		{
 			String[] saDummy = {
 					"URI:ldap://ldap.infocamere.it/cn%3DInfoCamere%20Firma%20Digitale%2Cou%3DCertificati%20di%20Firma%2Co%3DInfoCamere%20SCpA%2Cc%3DIT?certificaterevocationlist"
 					};
 			setCertString(TreeNodeType.X509V3_CRL_DISTRIBUTION_POINTS,saDummy);
 		}
 		{
 			String[] saDummy = {
 					"9C:6F:E1:76:68:27:42:9C:C0:80:40:70:A0:0F:08:E9:D1:12:FF:A4"
 					};
 			setCertString(TreeNodeType.X509V3_SUBJECT_KEY_IDENTIFIER,saDummy);
 		}
 	}
 
 	public void removeCertString(TreeNodeDescriptor.TreeNodeType aType){
 		aCertStrings.remove(aType);
 	}
 
 	public String[] getCertStrings(TreeNodeDescriptor.TreeNodeType aType) {
 		return aCertStrings.get(aType);
 	}
 
 	public void setCertString(TreeNodeDescriptor.TreeNodeType aType, String[] aStringArr) {
 		aCertStrings.put(aType, aStringArr);
 	}
 
 	public boolean isCertificateValid() {
 		return true;
 	}	
 }
