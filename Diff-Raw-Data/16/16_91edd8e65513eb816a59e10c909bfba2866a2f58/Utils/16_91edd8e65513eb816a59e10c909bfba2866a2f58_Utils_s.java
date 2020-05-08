 package org.cagrid.gaards.dorian.test;
 
 import gov.nih.nci.cagrid.opensaml.SAMLAssertion;
 import gov.nih.nci.cagrid.opensaml.SAMLAttribute;
 import gov.nih.nci.cagrid.opensaml.SAMLAttributeStatement;
 
 import java.util.Iterator;
 
 import org.cagrid.dorian.ca.impl.CertificateAuthority;
 import org.cagrid.dorian.ca.impl.CertificateAuthorityManager;
 import org.cagrid.dorian.ca.impl.CertificateAuthorityProperties;
 import org.cagrid.dorian.federation.impl.AutoApprovalPolicy;
 import org.cagrid.dorian.federation.impl.IdentityFederationProperties;
 import org.cagrid.dorian.federation.impl.ManualApprovalPolicy;
 import org.cagrid.dorian.federation.impl.UserManager;
 import org.cagrid.dorian.idp.impl.AssertionCredentialsManager;
 import org.cagrid.dorian.idp.impl.IdentityProvider;
 import org.cagrid.dorian.idp.impl.IdentityProviderProperties;
 import org.cagrid.dorian.ifs.GridUserPolicy;
 import org.cagrid.dorian.ifs.TrustedIdP;
 import org.cagrid.dorian.service.impl.BeanUtils;
 import org.cagrid.dorian.service.impl.DorianProperties;
 import org.cagrid.tools.database.Database;
 import org.cagrid.tools.events.EventManager;
 import org.springframework.core.io.AbstractResource;
 import org.springframework.core.io.ClassPathResource;
 
 public class Utils {
 
 	public static String CA_SUBJECT_PREFIX = null;
 
 	public static String CA_SUBJECT_DN = null;
 
 	public static String CA_SUBJECT = null;
 
 	private static Database db = null;
 
 	public static BeanUtils getBeanUtils() throws Exception {
 		ClassPathResource properties = new ClassPathResource(Constants.DORIAN_PROPERTIES);
 		return getBeanUtils(properties);
 	}
 
 	public static BeanUtils getBeanUtils(AbstractResource properties) throws Exception {
 		ClassPathResource cpr = new ClassPathResource(Constants.DORIAN_CONFIGURATION);
 		return new BeanUtils(cpr, properties);
 	}
 
 	public static DorianProperties getDorianProperties() throws Exception {
 		return getBeanUtils().getDorianProperties();
 	}
 
 	public static EventManager getEventManager() throws Exception {
 		return getBeanUtils().getEventManager();
 	}
/*
	public static DorianProperties getExpiringDorianProperties() throws Exception {
		return getBeanUtils(new ClassPathResource(Constants.DORIAN_PROPERTIES_EXPIRING_CREDENTIALS)).getDorianProperties();
	}
	*/
 
 	public static IdentityFederationProperties getIdentityFederationProperties() throws Exception {
 		return getBeanUtils().getIdentityFederationProperties();
 	}
 
 	public static IdentityProvider getIdentityProvider() throws Exception {
 		return getBeanUtils().getIdentityProvider();
 	}
 
 	public static IdentityProviderProperties getIdentityProviderProperties() throws Exception {
 		return getBeanUtils().getIdentityProviderProperties();
 	}
 
 	public static AssertionCredentialsManager getAssertionCredentialsManager() throws Exception {
 		return getBeanUtils().getAssertionCredentialsManager();
 	}
 
 	public static org.cagrid.dorian.idp.impl.UserManager getIdPUserManager() throws Exception {
 		return getBeanUtils().getIdPUserManager();
 	}
 
 	public static Database getDB() throws Exception {
 		if (db == null) {
 			db = getBeanUtils().getDatabase();
 			db.createDatabaseIfNeeded();
 		}
 		return db;
 	}
 
 	public static String getDorianIdPUserId(String policy, String idpName, String caSubject, String uid) throws Exception {
 		TrustedIdP idp = new TrustedIdP();
 		idp.setId(1);
 		idp.setName(idpName);
 		return UserManager.getUserSubject(policy, caSubject, idp, uid);
 	}
 
 	public static String getDorianIdPUserId(String policy, String caSubject, String uid) throws Exception {
 		return getDorianIdPUserId(policy, "Dorian IdP", caSubject, uid);
 	}
 
 	public static GridUserPolicy[] getUserPolicies() {
 		GridUserPolicy[] policies = new GridUserPolicy[2];
 		policies[0] = new GridUserPolicy();
 		policies[0].setClassName(ManualApprovalPolicy.class.getName());
 		policies[0].setName("");
 		policies[1] = new GridUserPolicy();
 		policies[1].setClassName(AutoApprovalPolicy.class.getName());
 		policies[1].setName("");
 		return policies;
 	}
 
 	public static String getCASubject() throws Exception {
 		if (CA_SUBJECT == null) {
 			return getCASubject(getCAProperties());
 		}
 		return CA_SUBJECT;
 	}
 
 	public static String getCASubject(CertificateAuthorityProperties conf) throws Exception {
 		if (CA_SUBJECT == null) {
 			CA_SUBJECT = conf.getCreationPolicy().getSubject();
 			int index = CA_SUBJECT.lastIndexOf(",");
 			CA_SUBJECT_PREFIX = CA_SUBJECT.substring(0, index);
 			index = CA_SUBJECT.indexOf("CN=");
 			CA_SUBJECT_DN = CA_SUBJECT.substring(index + 3);
 		}
 		return CA_SUBJECT;
 	}
 
 	public static CertificateAuthorityProperties getCAProperties() throws Exception {
 		return getBeanUtils().getCertificateAuthorityProperties();
 	}
 	
 	public static CertificateAuthorityManager getCertificateAuthorityManager() throws Exception{
 		getCA();
 		CertificateAuthorityManager cm = getBeanUtils().getCertificateAuthorityManager();
 		return cm;
 	}
 
 	public static CertificateAuthority getCA() throws Exception {
 		getCASubject(getCAProperties());
 		CertificateAuthority ca = getBeanUtils().getCertificateAuthority();
 		ca.clearCertificateAuthority();
 		return ca;
 	}
 
 	public static String getAttribute(SAMLAssertion saml, String namespace, String name) {
 		Iterator itr = saml.getStatements();
 		while (itr.hasNext()) {
 			Object o = itr.next();
 			if (o instanceof SAMLAttributeStatement) {
 				SAMLAttributeStatement att = (SAMLAttributeStatement) o;
 				Iterator attItr = att.getAttributes();
 				while (attItr.hasNext()) {
 					SAMLAttribute a = (SAMLAttribute) attItr.next();
 					if ((a.getNamespace().equals(namespace)) && (a.getName().equals(name))) {
 						Iterator vals = a.getValues();
 						while (vals.hasNext()) {
 							String val = gov.nih.nci.cagrid.common.Utils.clean((String) vals.next());
 							if (val != null) {
 								return val;
 							}
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 }
