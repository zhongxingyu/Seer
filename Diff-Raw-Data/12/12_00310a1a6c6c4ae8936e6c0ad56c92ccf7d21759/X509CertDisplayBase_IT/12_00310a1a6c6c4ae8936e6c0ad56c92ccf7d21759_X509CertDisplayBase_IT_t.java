 /* ***** BEGIN LICENSE BLOCK ********************************************
  * Version: EUPL 1.1/GPL 3.0
  * 
  * The contents of this file are subject to the EUPL, Version 1.1 or 
  * - as soon they will be approved by the European Commission - 
  * subsequent versions of the EUPL (the "Licence");
  * you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is /oxsit-custom_it/src/com/yacme/ext/oxsit/cust_it/comp/security/cert/X509CertDisplayBase_IT.java.
  *
  * The Initial Developer of the Original Code is
  * Giuseppe Castagno giuseppe.castagno@acca-esse.it
  * 
  * Portions created by the Initial Developer are Copyright (C) 2009-2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 3 or later (the "GPL")
  * in which case the provisions of the GPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of the GPL, and not to allow others to
  * use your version of this file under the terms of the EUPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the EUPL, or the GPL.
  *
  * ***** END LICENSE BLOCK ******************************************** */
 
 package com.yacme.ext.oxsit.cust_it.comp.security.cert;
 
 import com.yacme.ext.oxsit.security.cert.CertificateElementID;
 import com.yacme.ext.oxsit.security.cert.XOX_CertificateExtension;
 import com.yacme.ext.oxsit.security.cert.XOX_X509Certificate;
 import com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Set;
 import java.util.TimeZone;
 
 import org.bouncycastle.asn1.ASN1InputStream;
 import org.bouncycastle.asn1.ASN1Sequence;
 import org.bouncycastle.asn1.DERObject;
 import org.bouncycastle.asn1.DERObjectIdentifier;
 import org.bouncycastle.asn1.DEROutputStream;
 import org.bouncycastle.asn1.x509.X509CertificateStructure;
 import org.bouncycastle.asn1.x509.X509Extension;
 import org.bouncycastle.asn1.x509.X509Extensions;
 import org.bouncycastle.crypto.digests.MD5Digest;
 import org.bouncycastle.crypto.digests.SHA1Digest;
 
 import com.sun.star.awt.Size;
 import com.sun.star.beans.PropertyValue;
 import com.sun.star.beans.XPropertySet;
 import com.sun.star.container.XNameAccess;
 import com.sun.star.frame.XComponentLoader;
 import com.sun.star.frame.XController;
 import com.sun.star.frame.XFrame;
 import com.sun.star.frame.XModel;
 import com.sun.star.lang.IllegalArgumentException;
 import com.sun.star.lang.XComponent;
 import com.sun.star.lang.XEventListener;
 import com.sun.star.lang.XInitialization;
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.lang.XMultiServiceFactory;
 import com.sun.star.lib.uno.helper.ComponentBase;
 import com.sun.star.style.XStyle;
 import com.sun.star.style.XStyleFamiliesSupplier;
 import com.sun.star.text.XText;
 import com.sun.star.text.XTextCursor;
 import com.sun.star.text.XTextDocument;
 import com.sun.star.text.XTextRange;
 import com.sun.star.text.XTextTable;
 import com.sun.star.text.XTextViewCursor;
 import com.sun.star.text.XTextViewCursorSupplier;
 import com.sun.star.uno.AnyConverter;
 import com.sun.star.uno.Exception;
 import com.sun.star.uno.Type;
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 import com.yacme.ext.oxsit.Helpers;
 import com.yacme.ext.oxsit.comp.security.cert.CertificateExtensionDisplayHelper;
 import com.yacme.ext.oxsit.logging.DynamicLogger;
 import com.yacme.ext.oxsit.logging.IDynamicLogger;
 import com.yacme.ext.oxsit.ooo.registry.MessageConfigurationAccess;
 
 /**
  *  This service implements the X509Certificate service.
  * receives the doc information from the task  
  *  
  * This objects has properties, they are set by the calling UNO objects.
  * 
  * The service is initialized with URL and XStorage of the document under test
  * Information about the certificates, number of certificates, status of every signature
  * ca be retrieved through properties 
  * 
  * @author beppec56
  *
  */
 public abstract class X509CertDisplayBase_IT extends ComponentBase //help class, implements XTypeProvider, XInterface, XWeak
 			implements 
 			XInitialization,
 			XOX_X509CertificateDisplay
 			 {
 
 	private XComponentContext m_xContext;
 	private XMultiComponentFactory m_xMCF;
 
 	protected String m_sTimeLocaleString = "id_validity_time_locale";//"%1$td %1$tB %1$tY %1$tH:%1$tM:%1$tS (%1$tZ)";
 	protected String m_sLocaleLanguage = "id_iso_lang_code"; //"it";
 	protected String m_sLocaleDateOfBirth = "id_date_of_birth_locale"; //specific locale, special for italy;
 	//Strings used for certificate elements
 
 	protected IDynamicLogger m_aLogger;
 
 //	private int m_nCertificateState;
 //	private int m_nCertificateStateConditions;
 	
 	protected boolean	m_bDisplayOID;
 
 	//the certificate representation
 	protected X509CertificateStructure m_aX509;
 
 	protected String m_sSubjectDisplayName;	
 	protected String m_sSubjectName = "";
 	private String				m_sLabelSubject = "id_cert_subject";
 	public	static final String		m_CERT_SBJ_NAME = "Subject";
 
 	private String m_sVersion = "";
 	private String				m_sLabelVersion = "id_cert_version";
 
 	private String m_sSerialNumber = "";
 	private String				m_sLabelSerialNumer = "id_cert_ser_numb";
 
 	protected String m_sIssuerDisplayName = "";
 	private String				m_sLabelIssuer = "id_cert_issuer";
 	protected String m_sIssuerName = "";
 	protected String m_sIssuerCommonName = "";
 
 	private String m_sNotValidAfter = "";
 	private String				m_sLabelNotValidAfter = "id_cert_valid_to";
 
 	private String m_sNotValidBefore = "";
 	private String				m_sLabelNotValidBefore = "id_cert_valid_from";
 
 	private String m_sSubjectPublicKeyAlgorithm = "";
 	private String				m_sLabelSubjAlgor = "id_cert_sign_alg";
 
 	private String m_sSubjectPublicKeyValue = "";
 	private String 				m_sLabelSubjectPublicKey = "id_cert_pub_key";
 
 	private String m_sSignatureAlgorithm = "";
 	private String 				m_sLabelThumbAlgor = "id_cert_thumbp";
 
 	private String m_sMD5Thumbprint = "";
 	private String 				m_sLabelThumbMDA5 = "id_cert_mda5_thumbp";
 
 	private String m_sSHA1Thumbprint = "";
 	private String 				m_sLabelThumbSHA1 = "id_cert_sha1_thumbp";
 
 	private Locale m_lTheLocale;
 
 	private String 				m_sLabelCritExtension = "id_cert_crit_ext";
 	private String 				m_sLabelNotCritExtension = "id_cert_notcrit_ext";
 	private String 				m_sLabelCertPath = "id_cert_certif_path";
 
 	//the hash map of all the extensions
 	//the String is the OID,
 	private HashMap<String,X509Extension>	m_aExtensions = new HashMap<String, X509Extension>(20);
 	private HashMap<CertificateElementID,String>	m_aElementLocalizedNames = new HashMap<CertificateElementID, String>(15);
 	private HashMap<CertificateElementID,String>	m_aElementComments = new HashMap<CertificateElementID, String>(15);
 	private HashMap<String,String>			m_aExtensionLocalizedNames = new HashMap<String, String>(50);
 	private HashMap<String,String>			m_aExtensionDisplayValues = new HashMap<String, String>(20);
 	private HashMap<String,String>			m_aElementAdditionalNotes = new HashMap<String, String>(20);
 	//the hash map of all the critical extensions
 	private HashMap<String,X509Extension>	m_aCriticalExtensions = new HashMap<String, X509Extension>(20);
 	//the hash map of all the non critical extensions
 	private HashMap<String,X509Extension>	m_aNotCriticalExtensions = new HashMap<String, X509Extension>(20);
 
 	private XOX_X509Certificate m_xQc;
 
 	/**
 	 * 
 	 * 
 	 * @param _ctx
 	 */
 	public X509CertDisplayBase_IT(XComponentContext _ctx) {
 		m_aLogger = new DynamicLogger(this, _ctx);
 //		m_aLogger = new DynamicLazyLogger();
 //		m_aLogger.enableLogging();
     	m_aLogger.ctor();
     	m_xContext = _ctx;
     	m_xMCF = m_xContext.getServiceManager();
     	m_bDisplayOID = false;
 //    	m_bIsFromUI = false;
     	//grab the locale strings, we'll use the interface language as a locale
     	//e.g. if interface language is Italian, the locale will be Italy, Italian
 		fillLocalizedString();
 
 		//locale of the extension
 		m_lTheLocale = new Locale(m_sLocaleLanguage);
 	}
 
 	/**
 	 * prepare the strings for the dialogs
 	 */
 	protected void fillLocalizedString() {
 		MessageConfigurationAccess m_aRegAcc = null;
 		m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);
 
 		try {
 			m_sTimeLocaleString = m_aRegAcc.getStringFromRegistry( m_sTimeLocaleString );			
 			m_sLocaleLanguage = m_aRegAcc.getStringFromRegistry( m_sLocaleLanguage );
 			m_sLocaleDateOfBirth = m_aRegAcc.getStringFromRegistry( m_sLocaleDateOfBirth );
 
 //strings for certificate tree control display
 			//directly inserted into a Hashmap
 			m_aElementLocalizedNames.put(CertificateElementID.VERSION, m_aRegAcc.getStringFromRegistry( m_sLabelVersion ) );
 			m_aElementLocalizedNames.put(CertificateElementID.SERIAL_NUMBER, m_aRegAcc.getStringFromRegistry( m_sLabelSerialNumer ));
 			m_aElementLocalizedNames.put(CertificateElementID.ISSUER, m_aRegAcc.getStringFromRegistry( m_sLabelIssuer ) );
 			m_aElementLocalizedNames.put(CertificateElementID.NOT_BEFORE, m_aRegAcc.getStringFromRegistry( m_sLabelNotValidBefore ));
 			m_aElementLocalizedNames.put(CertificateElementID.NOT_AFTER, m_aRegAcc.getStringFromRegistry( m_sLabelNotValidAfter ));
 			m_aElementLocalizedNames.put(CertificateElementID.SUBJECT, m_aRegAcc.getStringFromRegistry( m_sLabelSubject ));
 			m_aElementLocalizedNames.put(CertificateElementID.SUBJECT_ALGORITHM, m_aRegAcc.getStringFromRegistry( m_sLabelSubjAlgor ));
 			m_aElementLocalizedNames.put(CertificateElementID.SUBJECT_PUBLIC_KEY, m_aRegAcc.getStringFromRegistry( m_sLabelSubjectPublicKey ));
 			m_aElementLocalizedNames.put(CertificateElementID.THUMBPRINT_SIGNATURE_ALGORITHM, m_aRegAcc.getStringFromRegistry( m_sLabelThumbAlgor ));
 			m_aElementLocalizedNames.put(CertificateElementID.CERTIFICATE_SHA1_THUMBPRINT, m_aRegAcc.getStringFromRegistry( m_sLabelThumbSHA1 ));
 			m_aElementLocalizedNames.put(CertificateElementID.CERTIFICATE_MD5_THUMBPRINT, m_aRegAcc.getStringFromRegistry( m_sLabelThumbMDA5 ));
 			m_aElementLocalizedNames.put(CertificateElementID.CERTIFICATION_PATH, m_aRegAcc.getStringFromRegistry( m_sLabelCertPath ));
 			m_aElementLocalizedNames.put(CertificateElementID.CRITICAL_EXTENSION, m_aRegAcc.getStringFromRegistry( m_sLabelCritExtension ));
 			m_aElementLocalizedNames.put(CertificateElementID.NOT_CRITICAL_EXTENSION, m_aRegAcc.getStringFromRegistry( m_sLabelNotCritExtension ));
 		} catch (com.sun.star.uno.Exception e) {
 			m_aLogger.severe("fillLocalizedString", e);
 		}
 		m_aRegAcc.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XInitialization#initialize(java.lang.Object[])
 	 * _arg[0] 
 	 */
 	@Override
 	public void initialize(Object[] _arg) throws Exception {
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getSubjectDisplayName()
 	 */
 	@Override
 	public String getSubjectDisplayName() {
 		return m_sSubjectDisplayName;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getVersion()
 	 */
 	@Override
 	public String getVersion() {
 		return m_sVersion;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getNotValidAfter()
 	 */
 	@Override
 	public String getNotValidAfter() {
 		return m_sNotValidAfter;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getNotValidBefore()
 	 */
 	@Override
 	public String getNotValidBefore() {
 		return m_sNotValidBefore;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getIssuerDisplayName()
 	 */
 	@Override
 	public String getIssuerDisplayName() {
 		return m_sIssuerDisplayName;
 	}
 
 	/* (non-Javadoc)
 
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getIssuerName()
 	 */
 	@Override
 	public String getIssuerName() {
 		return m_sIssuerName;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getIssuerCommonName()
 	 */
 	@Override
 	public String getIssuerCommonName() {
 		return m_sIssuerCommonName;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getMD5Thumbprint()
 	 */
 	@Override
 	public String getMD5Thumbprint() {
 		return m_sMD5Thumbprint;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getSHA1Thumbprint()
 	 */
 	@Override
 	public String getSHA1Thumbprint() {
 		return m_sSHA1Thumbprint;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getSerialNumber()
 	 */
 	@Override
 	public String getSerialNumber() {
 		return m_sSerialNumber;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getSignatureAlgorithm()
 	 */
 	@Override
 	public String getSignatureAlgorithm() {
 		return m_sSignatureAlgorithm;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getSubjectName()
 	 */
 	@Override
 	public String getSubjectName() {
 		return m_sSubjectName;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getSubjectPublicKeyAlgorithm()
 	 */
 	@Override
 	public String getSubjectPublicKeyAlgorithm() {
 		return m_sSubjectPublicKeyAlgorithm;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getSubjectPublicKeyValue()
 	 */
 	@Override
 	public String getSubjectPublicKeyValue() {
 		return m_sSubjectPublicKeyValue;
 	}
 
 	protected void initThumbPrints() {
 		//obtain a byte block of the entire certificate data
 		ByteArrayOutputStream   bOut = new ByteArrayOutputStream();
 		DEROutputStream         dOut = new DEROutputStream(bOut);
 		try {
 			dOut.writeObject(m_aX509);
 			byte[] certBlock = bOut.toByteArray();
 
 			//now compute the certificate SHA1 & MD5 digest
 			SHA1Digest digsha1 = new SHA1Digest();
 			digsha1.update(certBlock, 0, certBlock.length);
 			byte[] hashsha1 = new byte[digsha1.getDigestSize()];
 			digsha1.doFinal(hashsha1, 0);
 			m_sSHA1Thumbprint = Helpers.printHexBytes(hashsha1);
 			MD5Digest  digmd5 = new MD5Digest();
 			digmd5.update(certBlock, 0, certBlock.length);
 			byte[] hashmd5 = new byte[digmd5.getDigestSize()];
 			digmd5.doFinal(hashmd5, 0);
 			m_sMD5Thumbprint = Helpers.printHexBytes(hashmd5);
 		} catch (IOException e) {
 			m_aLogger.severe("initThumbPrints", e);
 		}
 	}
 
 	protected String initSignatureAlgorithm() {
 		DERObjectIdentifier oi = m_aX509.getSignatureAlgorithm().getObjectId();
 		return new String(""+(
 				(oi.equals(X509CertificateStructure.sha1WithRSAEncryption)) ? 
 				"pkcs-1 sha1WithRSAEncryption" : oi.toString())
 				);
 	}
 
 	protected String initPublicKeyData() {
 		byte[] sbjkd = m_aX509.getSubjectPublicKeyInfo().getPublicKeyData().getBytes();
 		return Helpers.printHexBytes(sbjkd);
 	}
 
 	protected String initPublicKeyAlgorithm() {
 //		AlgorithmIdentifier aid = m_aX509.getSignatureAlgorithm();
 		DERObjectIdentifier oi = m_aX509.getSubjectPublicKeyInfo().getAlgorithmId().getObjectId();
 		return new String(""+(
 				(oi.equals(X509CertificateStructure.rsaEncryption)) ?
 					"pkcs-1 rsaEncryption" : oi.getId())
 					);
 	}
 
 	protected String initCertDate(Date _aTime) {
 		//force UTC time
 		TimeZone gmt = TimeZone.getTimeZone("UTC");
 		GregorianCalendar calendar = new GregorianCalendar(gmt,m_lTheLocale);
 		calendar.setTime(_aTime);	
 //string with time only
 //the locale should be the one of the extension not the Java one.
 		String time = String.format(m_lTheLocale,m_sTimeLocaleString, calendar);
 		return time;
 	}	
 
 	abstract void initSubjectName();
 
 	abstract void initIssuerName();
 	
 	//////////////////////////////////////////////////////////////////
 	///////////////// area for extension display management
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCertificateExtensionStringValue(java.lang.String)
 	 */
 	@Override
 	public String getCertificateExtensionValueString(String _oid) {
 		String ret = m_aExtensionDisplayValues.get(_oid);
 		return (ret == null) ? "": ret;
 	}
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCertificateExtensionOIDs()
 	 */
 	@Override
 	public String[] getCertificateExtensionOIDs() {
 		if(m_aExtensions.isEmpty())
 			return null;
 		Set<String> aTheOIDs = m_aExtensions.keySet();
 		String[] ret = new String[aTheOIDs.size()]; 
 		return aTheOIDs.toArray(ret);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCriticalCertificateExtensionOIDs()
 	 */
 	@Override
 	public String[] getCriticalCertificateExtensionOIDs() {
 		if(m_aCriticalExtensions.isEmpty())
 			return null;
 		Set<String> aTheOIDs = m_aCriticalExtensions.keySet();
 		String[] ret = new String[aTheOIDs.size()]; 
 		return aTheOIDs.toArray(ret);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getNotCriticalCertificateExtensionOIDs()
 	 */
 	@Override
 	public String[] getNotCriticalCertificateExtensionOIDs() {
 		if(m_aNotCriticalExtensions.isEmpty())
 			return null;
 		Set<String> aTheOIDs = m_aNotCriticalExtensions.keySet();
 		String[] ret = new String[aTheOIDs.size()]; 
 		return aTheOIDs.toArray(ret);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCriticalExtensions()
 	 */
 	@Override
 	public XOX_CertificateExtension[] getCriticalExtensions() {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getNotCriticalExtensions()
 	 */
 	@Override
 	public XOX_CertificateExtension[] getNotCriticalExtensions() {
 		return null;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCertificateExtensionName(java.lang.String)
 	 */
 	@Override
 	public String getCertificateExtensionLocalizedName(String _oid) {
 		String ret = m_aExtensionLocalizedNames.get(_oid);
 		return (ret == null) ? "": ret;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCertificateElementLocalizedName(com.yacme.ext.oxsit.security.cert.CertificateElementID)
 	 */
 	@Override
 	public String getCertificateElementLocalizedName(CertificateElementID arg0) {
 		String ret = m_aElementLocalizedNames.get(arg0);
 		return (ret == null) ? "" : ret;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCertificateElementCommentString(com.yacme.ext.oxsit.security.cert.CertificateElementID)
 	 */
 	@Override
 	public String getCertificateElementCommentString(CertificateElementID arg0) {
 		String ret = m_aElementComments.get(arg0);
 		return (ret == null) ? "" : ret;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#setCertificateElementCommentString(com.yacme.ext.oxsit.security.cert.CertificateElementID, java.lang.String)
 	 */
 	@Override
 	public void setCertificateElementCommentString(CertificateElementID arg0,
 			String arg1) {
 		m_aElementComments.put(arg0, arg1);
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#prepareDisplayStrings(com.sun.star.frame.XFrame, com.sun.star.lang.XComponent)
 	 */
 	@Override
 	public void prepareDisplayStrings(XFrame _xFrame, XComponent _xComp)
 			throws IllegalArgumentException, Exception {
 		m_xQc = (XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, _xComp);
 		if(m_xQc == null)
 			throw (new IllegalArgumentException("com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#prepareDisplayStrings wrong argument"));
 
 		//
 		m_aX509 = null; //remove old certificate
 						//remove old data from HashMaps
 		m_aExtensions.clear();
 		m_aExtensionLocalizedNames.clear();
 		m_aExtensionDisplayValues.clear();
 		m_aCriticalExtensions.clear();
 		m_aNotCriticalExtensions.clear();
 
 		ByteArrayInputStream as = new ByteArrayInputStream(m_xQc.getCertificateAttributes().getDEREncoded()); 
 		ASN1InputStream aderin = new ASN1InputStream(as);
 		DERObject ado;
 		try {
 			ado = aderin.readObject();
 			m_aX509 = new X509CertificateStructure((ASN1Sequence) ado);
 //initializes the certificate display information
 			initSubjectName();
 			m_sVersion = String.format("V%d", m_aX509.getVersion());
 			m_sSerialNumber = new String(""+m_aX509.getSerialNumber().getValue());
 			initIssuerName();
 			m_sNotValidBefore = initCertDate(m_aX509.getStartDate().getDate());
 			m_sNotValidAfter =  initCertDate(m_aX509.getEndDate().getDate());
 			m_sSubjectPublicKeyAlgorithm = initPublicKeyAlgorithm();
 			m_sSubjectPublicKeyValue = initPublicKeyData();
 			m_sSignatureAlgorithm = initSignatureAlgorithm();
 			initThumbPrints();
 			//now initializes the Extension listing			
 			X509Extensions aX509Exts = m_aX509.getTBSCertificate().getExtensions();
 			//fill the internal extension HashMaps
 			//at the same time we'll get the extension localized name from resources and
 			//fill the display data
 			MessageConfigurationAccess m_aRegAcc = null;
 			m_aRegAcc = new MessageConfigurationAccess(m_xContext, m_xMCF);
 //FIXME: may be we need to adapt this to the context: the following is valid ONLY if this
 			//object is instantiated from within a dialog, is not true if instantiated from a not UI method (e.g. from basic for example).
 			IDynamicLogger aDlgH = null;
 			CertificateExtensionDisplayHelper aHelper = new CertificateExtensionDisplayHelper(m_xContext, m_lTheLocale,
 										m_sTimeLocaleString,m_sLocaleDateOfBirth, m_bDisplayOID, m_aLogger);
 
 			for(Enumeration<DERObjectIdentifier> enume = aX509Exts.oids(); enume.hasMoreElements();) {
 				DERObjectIdentifier aDERId = enume.nextElement();
 				String aTheOID = aDERId.getId();
 				X509Extension aext = aX509Exts.getExtension(aDERId);
 				m_aExtensions.put(aTheOID, aext);
 				//now grab the localized description
 				try {
 					m_aExtensionLocalizedNames.put(aTheOID, m_aRegAcc.getStringFromRegistry( aTheOID )+
 							((m_bDisplayOID) ? (" (OID: "+aTheOID.toString()+")" ): ""));
 				} catch (com.sun.star.uno.Exception e) {
 					m_aLogger.severe("setDEREncoded", e);
 					m_aExtensionLocalizedNames.put(aTheOID, aTheOID);
 				}
 				//and decode this extension
 				m_aExtensionDisplayValues.put(aTheOID, aHelper.examineExtension(aext, aDERId, this));
 
 				if(aext.isCritical())
 					m_aCriticalExtensions.put(aTheOID, aext);
 				else
 					m_aNotCriticalExtensions.put(aTheOID, aext);					
 			}
 			m_aRegAcc.dispose();
 		} catch (IOException e) {
 			m_aLogger.severe("setDEREncoded", e);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#setCertificateElementCommentString(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void setCertificateExtensionCommentString(String _Name, String _Comment) {
 		m_aElementAdditionalNotes.put(_Name, _Comment);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#getCertificateElementCommentString(java.lang.String)
 	 */
 	@Override
 	public String getCertificateExtensionCommentString(String _Name) {
 		String ret = m_aElementAdditionalNotes.get(_Name);
 		return (ret == null) ? "": ret;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#generateReport()
 	 */
 	@Override
 	public void generateCertificateReport(XComponent _xComp)
 	throws IllegalArgumentException, Exception  {
 		//Create a writer document with certificate element description
 		m_xQc = (XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, _xComp);
 		if(m_xQc == null)
 			throw (new IllegalArgumentException("com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#generateCertificateReport wrong argument"));
 
 		try {
 			//create a writer empty document
 			Object oDesktop = m_xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
 			//insert a title, H1
 			XComponentLoader aCompLd = (XComponentLoader)UnoRuntime.queryInterface(XComponentLoader.class, oDesktop);
 
 			// define load properties according to com.sun.star.document.MediaDescriptor
 
 			/* or simply create an empty array of com.sun.star.beans.PropertyValue structs:
 			    PropertyValue[] loadProps = new PropertyValue[0]
 			 */
 
 			// the boolean property Hidden tells the office to open a file in hidden mode
 			PropertyValue[] loadProps = new PropertyValue[2];
 			loadProps[0] = new PropertyValue();
 			loadProps[0].Name = "DocumentTitle";
 			loadProps[0].Value = new String("Stato del certificato"); 
 			loadProps[1] = new PropertyValue();
 			loadProps[1].Name = "Author";
 			loadProps[1].Value = new String("OXSIT signature extension"); 
 			// load
 			XComponent aDocComp = aCompLd.loadComponentFromURL("private:factory/swriter", "_blank", 0, loadProps); 
 			XTextDocument aTextDocument = (com.sun.star.text.XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, aDocComp);
 			addCertificateReport(aTextDocument, _xComp);
 			//insert a Header
 			prepareAHeader(aTextDocument, "Stato del certificato");			
 		} catch (Throwable e) {
 			m_aLogger.severe(e);
 		}
 	}
 
 	/** This method sets the text colour of the cell refered to by sCellName to white and inserts
     the string sText in it
 	 */
 	private void insertIntoCell(String sCellName, String sText, XTextTable xTable) {
 		// Access the XText interface of the cell referred to by sCellName
 		XText xCellText = (XText) UnoRuntime.queryInterface(
 				XText.class, xTable.getCellByName(sCellName));
 
 		// create a text cursor from the cells XText interface
 /*		XTextCursor xCellCursor = xCellText.createTextCursor();
 
 		// Get the property set of the cell's TextCursor
 		XPropertySet xCellCursorProps = (XPropertySet)UnoRuntime.queryInterface(
 				XPropertySet.class, xCellCursor);
 
 		    try {
         // Set the color of the text to white
         xCellCursorProps.setPropertyValue("CharColor", new Integer(16777215));
     } catch (Exception e) {
         e.printStackTrace(System.out);
     }*/
 
 		// Set the text in the cell to sText
 		xCellText.setString(sText);
 	}
 
 
 	/** This method shows how to create and insert a text table, as well as insert text and formulae
    into the cells of the table
 	 * @param xTxCurs 
 	 */
 	protected XTextTable insertTable(XTextDocument xDoc, XTextCursor xTxCurs, int row, int col)
 	{
 		try 
 		{
 			XMultiServiceFactory xMSF = (XMultiServiceFactory)UnoRuntime.queryInterface(XMultiServiceFactory.class, xDoc);
 
 			//       Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xCC);
 			// get the remote service manager
 			// query its XDesktop interface, we need the current component
 			//       XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop);
 
 			//       XComponent xWriterComponent = xDesktop.getCurrentComponent();
 
 			//       XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xDoc);
 
 			//       XController xController = xModel.getCurrentController();
 			// the controller gives us the TextViewCursor
 			//       XTextViewCursorSupplier xViewCursorSupplier =
 			//       (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController);
 			//       XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();
 			//get the main text document
 			XText mxDocText = xDoc.getText();      
 
 			// Create a new table from the document's factory
 			XTextTable xTable = (XTextTable) UnoRuntime.queryInterface( 
 					XTextTable.class, 
 					xMSF.createInstance(
 							"com.sun.star.text.TextTable" ) );
 
 			// Specify that we want the table to have 4 rows and 4 columns
 			xTable.initialize( row	, col );
 
 			XTextRange xPos = xTxCurs.getStart();//xViewCursor.getStart();		       
 			mxDocText.insertTextContent( xPos, xTable, false);
 
 			// Insert the table into the document
 			// Get an XIndexAccess of the table rows
 
 			// Access the property set of the first row (properties listed in service description:
 			// com.sun.star.text.TextTableRow)
 			//       XPropertySet xRow = (XPropertySet) UnoRuntime.queryInterface( 
 			//           XPropertySet.class, xRows.getByIndex ( 0 ) );
 			// If BackTransparant is false, then the background color is visible
 			//       xRow.setPropertyValue( "BackTransparent", new Boolean(false));
 			// Specify the color of the background to be dark blue
 			//       xRow.setPropertyValue( "BackColor", new Integer(6710932));
 
 			// Access the property set of the whole table
 			       XPropertySet xTableProps = (XPropertySet)UnoRuntime.queryInterface( 
 			           XPropertySet.class, xTable );
 
 //			       Utilities.showProperties(xTable, xTableProps);
 			 //set table
 			// We want visible background colors
 			       xTableProps.setPropertyValue( "RepeatHeadline", new Boolean(true));
 			       xTableProps.setPropertyValue( "HeaderRowCount", new Integer(1));
 			       xTableProps.setPropertyValue( "HoriOrient", new Short(com.sun.star.text.HoriOrientation.LEFT));
 			       xTableProps.setPropertyValue( "RelativeWidth", new Short((short)100));
 //WRONG !			       xTableProps.setPropertyValue( "IsWidthRelative", new Boolean(true));
 
 //			       Utilities.showProperties(xTable, xTableProps);
 			// Set the background colour to light blue
 			//       xTableProps.setPropertyValue( "BackColor", new Integer(13421823));
 
 			// set the text (and text colour) of all the cells in the first row of the table
 			//insert some titles
 			insertIntoCell( "A1", "Element", xTable );
 			insertIntoCell( "B1", "Value", xTable );
 			insertIntoCell( "C1", "Notes", xTable );
 
 			/*       // Insert random numbers into the first this three cells of each
        // remaining row
        xTable.getCellByName( "A2" ).setValue( getRandomDouble() );
        xTable.getCellByName( "B2" ).setValue( getRandomDouble() );
        xTable.getCellByName( "C2" ).setValue( getRandomDouble() );
 
        xTable.getCellByName( "A3" ).setValue( getRandomDouble() );
        xTable.getCellByName( "B3" ).setValue( getRandomDouble() );
        xTable.getCellByName( "C3" ).setValue( getRandomDouble() );
 
        xTable.getCellByName( "A4" ).setValue( getRandomDouble() );
        xTable.getCellByName( "B4" ).setValue( getRandomDouble() );
        xTable.getCellByName( "C4" ).setValue( getRandomDouble() );*/
 
 			// Set the last cell in each row to be a formula that calculates
 			// the sum of the first three cells
 			/*        xTable.getCellByName( "D2" ).setFormula( "sum <A2:C2>" );
        xTable.getCellByName( "D3" ).setFormula( "sum <A3:C3>" );
        xTable.getCellByName( "D4" ).setFormula( "sum <A4:C4>" );*/
 			return xTable;
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace ( System.out );
 		}
 		return null;
 	}
 
 	private void trimStandardPageStyle(XTextDocument _xaDoc) {
 		XStyleFamiliesSupplier StyleFam = (XStyleFamiliesSupplier) UnoRuntime.queryInterface(XStyleFamiliesSupplier.class, _xaDoc);
 		XNameAccess StyleFamNames = StyleFam.getStyleFamilies();
 
 		XStyle StdStyle = null;
 		try {
 			XNameAccess PageStyles = (XNameAccess) AnyConverter.toObject(new Type(XNameAccess.class),StyleFamNames.getByName("PageStyles"));
 			StdStyle = (XStyle) AnyConverter.toObject(new Type(XStyle.class),
 					PageStyles.getByName("Standard"));
 			XPropertySet PropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, StdStyle);
 			// changing/getting some properties
 			PropSet.setPropertyValue("IsLandscape", new Boolean(true));
 			Size aSz = new Size();
 			aSz.Height = 21000;
 			aSz.Width = 29700;
 			PropSet.setPropertyValue("Size", aSz);
 			PropSet.setPropertyValue("LeftMargin", new Integer(1500));
 			PropSet.setPropertyValue("RightMargin", new Integer(1500));
 			PropSet.setPropertyValue("TopMargin", new Integer(1500));
 			PropSet.setPropertyValue("BottomMargin", new Integer(1500));
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		} 
 		
 	}
 
 	/**
 	 * @param textDocument
 	 * @param string
 	 */
 	private void insertAHeading(XTextDocument textDocument, XTextViewCursor xViewCursor, String string, String style) {
 		//from view cursor get properties
 		try {
 			XText xDocumentText = xViewCursor.getText();
 		     
 		     // the text creates a model cursor from the viewcursor
 		     XTextCursor xModelCursor = xDocumentText.createTextCursorByRange(xViewCursor.getStart());
 	
 		     // query its XPropertySet interface, we want to set character and paragraph properties
 		     XPropertySet xCursorPropertySet = (XPropertySet)UnoRuntime.queryInterface(
 		         XPropertySet.class, xViewCursor);
 		     String sParSt = (String) xCursorPropertySet.getPropertyValue("ParaStyleName");
 		     xCursorPropertySet.setPropertyValue("ParaStyleName", new String(style) );
 		     xViewCursor.setString(string+"\r");
 		     xViewCursor.collapseToEnd();
 /*		     xViewCursor.setString("\r");
 		     xViewCursor.collapseToEnd();*/
 		     xCursorPropertySet.setPropertyValue("ParaStyleName", sParSt );
 
 		     /*		     
 		     if(xCursorPropertySet != null) {
 		    	 XPropertySetInfo xf = xCursorPropertySet.getPropertySetInfo();
 		    	 Property[] pr = xf.getProperties();
 		    	 for(int i = 0; i < pr.length;i++)
 		    		 trace(""+pr[i].Name);
 //			     Utilities.showProperties(xViewCursor, xCursorPropertySet);		    	 
 		     }*/
 /*		     xCursorPropertySet = (XPropertySet)UnoRuntime.queryInterface(
 			         XPropertySet.class, xModelCursor);	     
 //		     Utilities.showProperties(xModelCursor, xCursorPropertySet);
 		     sParSt = (String) xCursorPropertySet.getPropertyValue("ParaStyleName");
 		     xCursorPropertySet.setPropertyValue("ParaStyleName", new String("Heading 1") );
 		     xModelCursor.setString(string+"\r");
 		     xModelCursor.collapseToEnd();
 		     xModelCursor.setString("\r");
 		     xCursorPropertySet.setPropertyValue("ParaStyleName", sParSt );*/
 		     
 		} catch (Throwable e) {
 			m_aLogger.severe(e);
 		}		
 	}
 
 	private void prepareAHeader(XTextDocument _xaDoc, String _TheHeader) {
 		XText xText = (com.sun.star.text.XText) _xaDoc.getText();
 
 		XStyleFamiliesSupplier StyleFam = (XStyleFamiliesSupplier) UnoRuntime.queryInterface(XStyleFamiliesSupplier.class, _xaDoc);
 		XNameAccess StyleFamNames = StyleFam.getStyleFamilies();
 
 		XStyle StdStyle = null;
 		try {
 			XNameAccess PageStyles = (XNameAccess) AnyConverter.toObject(new Type(XNameAccess.class),StyleFamNames.getByName("PageStyles"));
 			StdStyle = (XStyle) AnyConverter.toObject(new Type(XStyle.class),PageStyles.getByName("Standard"));
 		}
 		catch (Exception e) {}
 
 		XPropertySet PropSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, StdStyle);
 
 		// changing/getting some properties
 		XText HeaderText = null;
 
 		try {
 			PropSet.setPropertyValue("HeaderIsOn", new Boolean(true));
 			PropSet.setPropertyValue("FooterIsOn", new Boolean(true));
 			HeaderText = (XText) UnoRuntime.queryInterface(XText.class, PropSet.getPropertyValue("HeaderText"));
 			XTextCursor xTextCursor = (XTextCursor) _xaDoc.getText().createTextCursor();
 			HeaderText.setString(_TheHeader);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		} 
 		
 		
 	}
 	
 	private void addASingleCertificateReport(XTextDocument _aTextDocument, XOX_X509Certificate _xCertif, int topLevel) {
 		
 		try {
 			trimStandardPageStyle(_aTextDocument);
 			XOX_X509CertificateDisplay xCeDisp = (XOX_X509CertificateDisplay)UnoRuntime.queryInterface(XOX_X509CertificateDisplay.class, _xCertif);
 
 			// get the XModel interface from the component
 			XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, _aTextDocument);
 
 			// the model knows its controller
 			XController xController = xModel.getCurrentController();
 
 			// the controller gives us the TextViewCursor
 			// query the viewcursor supplier interface 
 			XTextViewCursorSupplier xViewCursorSupplier = 
 				(XTextViewCursorSupplier)UnoRuntime.queryInterface(
 						XTextViewCursorSupplier.class, xController);
 
 			// get the cursor 
 			XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();
 
 			//General certificate section H1
 			//insert a title, Heading level 1
 			insertAHeading(_aTextDocument,xViewCursor, "Stato del certificato","Heading "+topLevel);
 			xViewCursor.collapseToEnd();
 			xViewCursor.setString(xCeDisp.getCertificateElementCommentString(CertificateElementID.GENERAL_CERTIFICATE_ABSTRACT)+
 			"\r");
 			xViewCursor.collapseToEnd();
 
 			//core certificate element H2
			insertAHeading(_aTextDocument,xViewCursor, "Elementi principali del certificato","Heading "+(topLevel+1));
 
 			//compute all the extensions + 11 other elements
 
 			//table with element, 3 columns: name, value, notes
 			XTextTable xTable = insertTable(_aTextDocument, xViewCursor, 13, 3);			
 			xViewCursor.gotoEnd(false);
 
 			int nRow = 2;
 			insertIntoCell("A"+nRow, xCeDisp.getSubjectDisplayName(), xTable);
 			nRow++;
 			CertificateElementID iCertEl = CertificateElementID.VERSION;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getVersion(), xTable);
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 
 			nRow++;
 			iCertEl = CertificateElementID.SERIAL_NUMBER;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getSerialNumber(), xTable);
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 			nRow++;
 			iCertEl = CertificateElementID.ISSUER;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getIssuerName(), xTable);
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 			nRow++;
 			iCertEl = CertificateElementID.NOT_BEFORE;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getNotValidBefore(), xTable);
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 			nRow++;
 			iCertEl = CertificateElementID.NOT_AFTER;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getNotValidAfter(), xTable);			
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 			nRow++;
 			iCertEl = CertificateElementID.SUBJECT;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getSubjectName(), xTable);
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 			nRow++;
 			iCertEl = CertificateElementID.SUBJECT_ALGORITHM;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getSubjectPublicKeyAlgorithm(), xTable);
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 			nRow++;
 			iCertEl = CertificateElementID.SUBJECT_PUBLIC_KEY;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getSubjectPublicKeyValue(), xTable);
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 			nRow++;
 			iCertEl = CertificateElementID.THUMBPRINT_SIGNATURE_ALGORITHM;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getSignatureAlgorithm(), xTable);
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 			nRow++;
 			iCertEl = CertificateElementID.CERTIFICATE_SHA1_THUMBPRINT;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getSHA1Thumbprint(), xTable);
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 			nRow++;
 			iCertEl = CertificateElementID.CERTIFICATE_MD5_THUMBPRINT;
 			insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 			insertIntoCell("B"+nRow, xCeDisp.getMD5Thumbprint(), xTable);
 			insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 			//			insertIntoCell("D"+nRow, ""+nRow, xTable);
 			//certificate critical extensions H2
 
 			xViewCursor.gotoEnd(false);
 
 			nRow = 2;
 			int nRows = 2;
 			String[] sCritExt = xCeDisp.getCriticalCertificateExtensionOIDs();
 			if(sCritExt != null) {
 				nRows += sCritExt.length;
 
 				iCertEl = CertificateElementID.CRITICAL_EXTENSION;
 				//Not certificate critical extensions H2
				insertAHeading(_aTextDocument,xViewCursor, xCeDisp.getCertificateElementLocalizedName(iCertEl),"Heading "+(topLevel+2));
 
 				xTable = insertTable(_aTextDocument, xViewCursor, nRows, 3);
 				xViewCursor.gotoEnd(false);
 				nRow = 2;
 				insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 				insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 				//				insertIntoCell("D"+nRow, ""+nRow, xTable);
 
 				//then there are extension marked critical
 				//add the main node
 				//the root node for extensions should see for the aggregate state of all
 				//the critical extensions
 				for(int i=0; i<sCritExt.length;i++) {
 					nRow++;
 					insertIntoCell("A"+nRow, xCeDisp.getCertificateExtensionLocalizedName(sCritExt[i]), xTable);
 					insertIntoCell("B"+nRow, xCeDisp.getCertificateExtensionValueString(sCritExt[i]), xTable);
 					insertIntoCell("C"+nRow, xCeDisp.getCertificateExtensionCommentString(sCritExt[i]), xTable);
 					//					insertIntoCell("D"+nRow, ""+nRow, xTable);
 				}
 			}
 
 			nRows = 2;
 			String[] sExt = xCeDisp.getNotCriticalCertificateExtensionOIDs();
 			if(sExt != null) {
 				nRows += sExt.length;
 
 				//Not certificate critical extensions H2
 				nRow = 2;
 				iCertEl = CertificateElementID.NOT_CRITICAL_EXTENSION;
				insertAHeading(_aTextDocument,xViewCursor, xCeDisp.getCertificateElementLocalizedName(iCertEl),"Heading "+(topLevel+2));
 
 				xTable = insertTable(_aTextDocument, xViewCursor, nRows, 3);
 				xViewCursor.gotoEnd(false);
 
 				insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 				insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 				//					insertIntoCell("D"+nRow, ""+nRow, xTable);
 
 				//then there are extension marked critical
 				//add the main node
 				//the root node for extensions should see for the aggregate state of all
 				//the critical extensions
 				for(int i=0; i<sExt.length;i++) {
 					nRow++;
 					insertIntoCell("A"+nRow, xCeDisp.getCertificateExtensionLocalizedName(sExt[i]), xTable);
 					insertIntoCell("B"+nRow, xCeDisp.getCertificateExtensionValueString(sExt[i]), xTable);
 					insertIntoCell("C"+nRow, xCeDisp.getCertificateExtensionCommentString(sExt[i]), xTable);
 					//						insertIntoCell("D"+nRow, ""+nRow, xTable);
 				}
 			}
 			//add the certification path
 			XOX_X509Certificate xCPath = _xCertif.getCertificationPath();
 			if(xCPath != null) {
 				iCertEl = CertificateElementID.CERTIFICATION_PATH;
 				insertAHeading(_aTextDocument,xViewCursor, xCeDisp.getCertificateElementLocalizedName(iCertEl),"Heading "+(topLevel+1));
 				xTable = insertTable(_aTextDocument, xViewCursor, 2, 3);
 				xViewCursor.gotoEnd(false);
 				nRow = 2;
 				insertIntoCell("A"+nRow, xCeDisp.getCertificateElementLocalizedName(iCertEl), xTable);
 				insertIntoCell("C"+nRow, xCeDisp.getCertificateElementCommentString(iCertEl), xTable);
 				//get the issuer certificate and print it
 				addASingleCertificateReport(_aTextDocument, xCPath,topLevel+1);
 				//exit, leave the document opened and unsaved, that's up to the user
 			}
 		} catch (Throwable e) {
 			m_aLogger.severe(e);
 		}
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#addCertificateReport(com.sun.star.text.XTextDocument)
 	 */
 	@Override
 	public void addCertificateReport(XTextDocument _aTextDocument, XComponent _xComp)
 	throws IllegalArgumentException, Exception {
 		m_xQc = (XOX_X509Certificate)UnoRuntime.queryInterface(XOX_X509Certificate.class, _xComp);
 		if(m_xQc == null)
 			throw (new IllegalArgumentException("com.yacme.ext.oxsit.security.cert.XOX_X509CertificateDisplay#addCertificateReport wrong argument"));
 
 		addASingleCertificateReport(_aTextDocument,m_xQc,1);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XComponent#addEventListener(com.sun.star.lang.XEventListener)
 	 */
 	@Override
 	public void addEventListener(XEventListener arg0) {
 		super.addEventListener(arg0);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XComponent#dispose()
 	 */
 	@Override
 	public void dispose() {
 		// FIXME need to check if this element is referenced somewhere before deallocating it
 		m_aLogger.entering("dispose");
 /*		if(m_xExt != null) {
 			for(int i=0; i < m_xExt.length; i++) {
 				XOX_CertificateExtension xExt = m_xExt[i];
 				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, xExt);
 				if(xComp != null)
 					xComp.dispose();
 			}
 		}
 		if(m_xCritExt != null) {
 			for(int i=0; i < m_xCritExt.length; i++) {
 				XOX_CertificateExtension xExt = m_xCritExt[i];
 				XComponent xComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, xExt);
 				if(xComp != null)
 					xComp.dispose();
 			}			
 		}
 		super.dispose();*/
 	}
 
 	/* (non-Javadoc)
 	 * @see com.sun.star.lang.XComponent#removeEventListener(com.sun.star.lang.XEventListener)
 	 */
 	@Override
 	public void removeEventListener(XEventListener arg0) {
 		super.removeEventListener(arg0);
 	}	
 }
