 package org.cagrid.dorian.service.impl;
 
 import org.cagrid.dorian.ca.impl.CertificateAuthority;
 import org.cagrid.dorian.ca.impl.CertificateAuthorityManager;
 import org.cagrid.dorian.ca.impl.CertificateAuthorityProperties;
 import org.cagrid.dorian.federation.impl.IdentityFederationProperties;
 import org.cagrid.dorian.federation.impl.TrustedIdPManager;
 import org.cagrid.dorian.idp.impl.AssertionCredentialsManager;
 import org.cagrid.dorian.idp.impl.IdentityProvider;
 import org.cagrid.dorian.idp.impl.IdentityProviderProperties;
 import org.cagrid.dorian.service.Dorian;
 import org.cagrid.dorian.service.DorianConstants;
 import org.cagrid.tools.database.Database;
 import org.cagrid.tools.events.EventManager;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.beans.factory.xml.XmlBeanFactory;
 import org.springframework.core.io.AbstractResource;
 
 public class BeanUtils {
 
 	private XmlBeanFactory factory;
 
 	public BeanUtils(AbstractResource dorianConf, AbstractResource dorianProperties) throws Exception {
 		this.factory = new XmlBeanFactory(dorianConf);
 		PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
 		cfg.setLocation(dorianProperties);
 		cfg.postProcessBeanFactory(factory);
 	}
 
 	public DorianProperties getDorianProperties() throws Exception {
 		DorianProperties props = (DorianProperties) factory.getBean(DorianConstants.DORIAN_PROPERTIES_BEAN);
 		return props;
 	}
 
 	public IdentityFederationProperties getIdentityFederationProperties() throws Exception {
 		IdentityFederationProperties props = (IdentityFederationProperties) factory.getBean(DorianConstants.IDENTITY_FEDERATION_PROPERTIES_BEAN);
 		return props;
 	}
 
 	public IdentityProvider getIdentityProvider() throws Exception {
		return (IdentityProvider) factory.getBean(DorianConstants.IDP_BEAN);
 	}
 
 	public EventManager getEventManager() throws Exception {
 		return (EventManager) factory.getBean(DorianConstants.EVENT_MANAGER_BEAN);
 	}
 
 	public TrustedIdPManager getTrustedIdPManager() throws Exception {
 		return (TrustedIdPManager) factory.getBean(DorianConstants.TRUSTED_IDP_MANAGER_BEAN);
 	}
 
 	public IdentityProviderProperties getIdentityProviderProperties() throws Exception {
 		return (IdentityProviderProperties) factory.getBean(DorianConstants.IDP_PROPERTIES_BEAN);
 	}
 
 	public AssertionCredentialsManager getAssertionCredentialsManager() throws Exception {
		return (AssertionCredentialsManager) factory.getBean(DorianConstants.IDP_ASSERTION_MANAGER_BEAN);
 	}
 
 	public org.cagrid.dorian.idp.impl.UserManager getIdPUserManager() throws Exception {
 		return (org.cagrid.dorian.idp.impl.UserManager) factory.getBean(DorianConstants.IDP_USER_MANAGER_BEAN);
 	}
 
 	public Database getDatabase() throws Exception {
 		return (Database) factory.getBean(DorianConstants.DATABASE_BEAN);
 	}
 
 	public CertificateAuthorityProperties getCertificateAuthorityProperties() throws Exception {
 		return (CertificateAuthorityProperties) factory.getBean(DorianConstants.CA_PROPERTIES_BEAN);
 
 	}
 
 	public CertificateAuthorityProperties getLegacyCertificateAuthorityProperties() throws Exception {
 		return (CertificateAuthorityProperties) factory.getBean(DorianConstants.LEGACY_CA_PROPERTIES_BEAN);
 
 	}
 
 	public CertificateAuthority getCertificateAuthority() throws Exception {
 		return (CertificateAuthority) factory.getBean(DorianConstants.CA_BEAN);
 	}
 
 	public CertificateAuthorityManager getCertificateAuthorityManager() throws Exception {
 		return (CertificateAuthorityManager) factory.getBean(DorianConstants.CA_MANAGER_BEAN);
 	}
 
 	public Dorian getDorian() throws Exception {
 		return (Dorian) factory.getBean(DorianConstants.DORIAN_BEAN);
 	}
 
 }
