 package com.gmail.at.zhuikov.aleksandr.root;
 
 import java.io.IOException;
 import java.security.Security;
 import java.security.cert.X509Certificate;
 import java.util.Properties;
 
 import javax.annotation.PostConstruct;
 
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.stereotype.Component;
 
 import ee.sk.digidoc.DigiDocException;
 import ee.sk.digidoc.SignedDoc;
 import ee.sk.digidoc.factory.NotaryFactory;
 import ee.sk.utils.ConfigManager;
 
 @Component
 public class OcspGateway {
 
 	private static final Logger LOG = LoggerFactory.getLogger(OcspGateway.class);
 
 	private static final String JDIGIDOC_PROPERTIES_FILENAME = "/jdigidoc.properties";
 
 	private NotaryFactory notaryFactory;
 
 	@PostConstruct
 	public void initializeJDigiDoc() {
 		Security.addProvider(new BouncyCastleProvider());
 
 		loadJDigiDocConfiguration();
 		initializeJDigiDocFactories();
 	}
 
 	private void loadJDigiDocConfiguration() {
 		try {
 			ClassPathResource resource = new ClassPathResource(
 					JDIGIDOC_PROPERTIES_FILENAME, getClass());
 
 			LOG.info("Loading jDigiDoc config from "
 					+ JDIGIDOC_PROPERTIES_FILENAME);
 			Properties properties = new Properties();
 			properties.load(resource.getInputStream());
 
 			ConfigManager.init(properties);
 		} catch (IOException e) {
 			LOG.error("Failed to load " + JDIGIDOC_PROPERTIES_FILENAME, e);
 			throw new RuntimeException("Failed to load "
 					+ JDIGIDOC_PROPERTIES_FILENAME, e);
 		}
 	}
 
 	private void initializeJDigiDocFactories() {
 		try {
 			ConfigManager.instance().getDigiDocFactory();
 			notaryFactory = ConfigManager.instance().getNotaryFactory();
 		} catch (DigiDocException e) {
 			LOG.error("Failed to initialize factories", e);
 			throw new RuntimeException("Failed to initialize factories", e);
 		}
 	}
 
	public boolean isValidVertificate(X509Certificate cert) {
 		try {
 			LOG.debug("Check cert: " + cert.getSubjectDN().getName());
 			LOG.debug("CA for cert: "
 					+ SignedDoc.getCommonName(cert.getIssuerX500Principal()
 							.getName("RFC1779")));
 			notaryFactory.checkCertificate(cert);
 		} catch (DigiDocException e) {
 			LOG.warn("Certificate verification failed", e);
 			return false;
 		}
 
 		return true;
 	}
 }
