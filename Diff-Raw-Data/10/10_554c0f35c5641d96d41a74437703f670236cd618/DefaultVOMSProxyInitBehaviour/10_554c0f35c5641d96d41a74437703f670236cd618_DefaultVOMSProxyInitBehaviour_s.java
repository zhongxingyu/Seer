 package org.italiangrid.voms.clients.impl;
 
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
 
 import org.bouncycastle.asn1.x509.AttributeCertificate;
 import org.bouncycastle.openssl.PasswordFinder;
 import org.italiangrid.voms.VOMSError;
 import org.italiangrid.voms.VOMSValidators;
 import org.italiangrid.voms.ac.VOMSACValidator;
 import org.italiangrid.voms.ac.ValidationResultListener;
 import org.italiangrid.voms.ac.impl.DefaultVOMSValidator;
 import org.italiangrid.voms.clients.ProxyInitParams;
 import org.italiangrid.voms.clients.strategies.ProxyInitStrategy;
 import org.italiangrid.voms.clients.strategies.VOMSCommandsParsingStrategy;
 import org.italiangrid.voms.clients.util.PasswordFinders;
 import org.italiangrid.voms.clients.util.VOMSProxyPathBuilder;
 import org.italiangrid.voms.credential.CredentialsUtils;
 import org.italiangrid.voms.credential.LoadCredentialsEventListener;
 import org.italiangrid.voms.credential.LoadCredentialsStrategy;
 import org.italiangrid.voms.credential.impl.DefaultLoadCredentialsStrategy;
 import org.italiangrid.voms.request.VOMSACService;
 import org.italiangrid.voms.request.VOMSRequestListener;
 import org.italiangrid.voms.request.VOMSServerInfoStoreListener;
 import org.italiangrid.voms.request.impl.DefaultVOMSACRequest;
 import org.italiangrid.voms.request.impl.DefaultVOMSACService;
 import org.italiangrid.voms.store.VOMSTrustStoreStatusListener;
 import org.italiangrid.voms.store.impl.DefaultVOMSTrustStore;
 import org.italiangrid.voms.util.CertificateValidatorBuilder;
 
 import eu.emi.security.authn.x509.ValidationErrorListener;
 import eu.emi.security.authn.x509.ValidationResult;
 import eu.emi.security.authn.x509.X509Credential;
 import eu.emi.security.authn.x509.helpers.pkipath.AbstractValidator;
 import eu.emi.security.authn.x509.proxy.ProxyCertificate;
 import eu.emi.security.authn.x509.proxy.ProxyCertificateOptions;
 import eu.emi.security.authn.x509.proxy.ProxyGenerator;
 
 /**
  * The default VOMS proxy init behaviour.
  * 
  * @author andreaceccanti
  * 
  */
 public class DefaultVOMSProxyInitBehaviour implements ProxyInitStrategy {
 
 	private VOMSCommandsParsingStrategy commandsParser;
 	private AbstractValidator certChainValidator;
 	
 	
 	private ValidationResultListener validationResultListener;
 	private VOMSRequestListener requestListener;
 	private ProxyCreationListener proxyCreationListener;
 	private VOMSServerInfoStoreListener serverInfoStoreListener;
 	private LoadCredentialsEventListener loadCredentialsEventListener;
 	private ValidationErrorListener certChainValidationErrorListener;
 	private VOMSTrustStoreStatusListener vomsTrustStoreListener;
 	
 	
 	public DefaultVOMSProxyInitBehaviour(VOMSCommandsParsingStrategy commandsParser,
 			ValidationResultListener validationListener,
 			VOMSRequestListener requestListener,
 			ProxyCreationListener pxCreationListener,
 			VOMSServerInfoStoreListener serverInfoStoreListener,
 			LoadCredentialsEventListener loadCredEventListener,
 			ValidationErrorListener certChainListener,
 			VOMSTrustStoreStatusListener vomsTSListener)
 			{
 		
 		this.commandsParser = commandsParser;
 		this.validationResultListener = validationListener;
 		this.requestListener = requestListener;
 		this.proxyCreationListener = pxCreationListener;
 		this.serverInfoStoreListener = serverInfoStoreListener;
 		this.loadCredentialsEventListener = loadCredEventListener;
 		this.certChainValidationErrorListener = certChainListener;
 		this.vomsTrustStoreListener = vomsTSListener;
 	}
 
 	public DefaultVOMSProxyInitBehaviour(VOMSCommandsParsingStrategy commandsParser, InitListenerAdapter listenerAdapter){
 		this.commandsParser = commandsParser;
 		this.validationResultListener = listenerAdapter;
 		this.requestListener = listenerAdapter;
 		this.proxyCreationListener = listenerAdapter;
 		this.serverInfoStoreListener = listenerAdapter;
 		this.loadCredentialsEventListener = listenerAdapter;
 		this.certChainValidationErrorListener = listenerAdapter;
 		this.vomsTrustStoreListener = listenerAdapter;
 	}
 	
 	
 	
 	protected void validateUserCredential(ProxyInitParams params, X509Credential cred){
 		
 		ValidationResult result = certChainValidator.validate(cred.getCertificateChain());
 		if (!result.isValid())
 			throw new VOMSError("User credential is not valid!");
 		
 	}
 	
 	public void initProxy(ProxyInitParams params) {
 		
 		X509Credential cred = lookupCredential(params);
 		if (cred == null)
 			throw new VOMSError("No credentials found!");
 
 		initCertChainValidator(params);
 		
 		if (params.validateUserCredential())
 			validateUserCredential(params, cred);
 		
 		List<AttributeCertificate> acs = getAttributeCertificates(params, cred);
 
 		if (params.verifyAC() && !acs.isEmpty())
 			verifyACs(params, acs);
 		
 		createProxy(params, cred, acs);
 	}
 	
 	private void initCertChainValidator(ProxyInitParams params){
 		
 		if (certChainValidator == null){
 			String trustAnchorsDir = DefaultVOMSValidator.DEFAULT_TRUST_ANCHORS_DIR;
 			
 			
 			if (params.getTrustAnchorsDir()!=null)
 				trustAnchorsDir = params.getTrustAnchorsDir();
 		
 			certChainValidator = CertificateValidatorBuilder.buildCertificateValidator(trustAnchorsDir, 
 					certChainValidationErrorListener);
 		}
 	}
 	
 	private void verifyACs(ProxyInitParams params, List<AttributeCertificate> acs) {
 		
 		VOMSACValidator acValidator = VOMSValidators.newValidator(
 				new DefaultVOMSTrustStore(vomsTrustStoreListener), 
 				certChainValidator, 
 				validationResultListener);
 		
 		acValidator.validateACs(acs);
 	}
 
 	private void  createProxy(ProxyInitParams params,
 			X509Credential credential, List<AttributeCertificate> acs) {
 		
 		String proxyFilePath = VOMSProxyPathBuilder.buildProxyPath();
 		
 		if (params.getGeneratedProxyFile() != null)
 			proxyFilePath = params.getGeneratedProxyFile();
 		
 		ProxyCertificateOptions certOptions = new ProxyCertificateOptions(credential.getCertificateChain());
 		
 		certOptions.setProxyPathLimit(params.getPathLenConstraint());
 		certOptions.setLimited(params.isLimited());
 		certOptions.setLifetime(params.getProxyLifetimeInSeconds());
 		certOptions.setType(params.getProxyType());
 		certOptions.setKeyLength(params.getKeySize());
 		
 		if (acs != null && !acs.isEmpty())
 			certOptions.setAttributeCertificates(acs.toArray(new AttributeCertificate[acs.size()]));
 		
 		try {
 			
 			ProxyCertificate cert = ProxyGenerator.generate(certOptions, credential.getKey());
 			CredentialsUtils.saveCredentials(new FileOutputStream(proxyFilePath), cert.getCredential());
 			proxyCreationListener.proxyCreated(proxyFilePath, cert);
 		} catch (Throwable t) {
 			
 			throw new VOMSError("Error creating proxy certificate: "+t.getMessage(), t);
 		}
 	}
 
 	protected List<String> sortFQANsIfRequested(ProxyInitParams params, List<String> unsortedFQANs){
 		
 		if (params.getFqanOrder() != null && !params.getFqanOrder().isEmpty() && ! unsortedFQANs.isEmpty()){ 
 			List<String> result = new ArrayList<String>();
 			
 			for (String s: params.getFqanOrder()){
 				if (unsortedFQANs.contains(s))
 					result.add(s);
 			}
 			
 			for (String s: unsortedFQANs)
 				if (!result.contains(s))
 					result.add(s);
 			
 			return result;
 		}
 		
 		return unsortedFQANs;
 	}
 	
 	protected List<AttributeCertificate> getAttributeCertificates(
 			ProxyInitParams params, X509Credential cred) {
 
 		List<String> vomsCommands = params.getVomsCommands();
 
 		if (vomsCommands == null || vomsCommands.isEmpty())
 			return Collections.emptyList();
 
 		Map<String, List<String>> vomsCommandsMap = commandsParser
 				.parseCommands(params.getVomsCommands());
 
 		List<AttributeCertificate> acs = new ArrayList<AttributeCertificate>();
 
 		for (String vo : vomsCommandsMap.keySet()) {
 
 			DefaultVOMSACRequest request = new DefaultVOMSACRequest();
 
 			List<String> fqans = vomsCommandsMap.get(vo);
 			request.setVoName(vo);
 			request.setRequestedFQANs(sortFQANsIfRequested(params, fqans));
 			request.setTargets(params.getTargets());
 			request.setLifetime(params.getAcLifetimeInSeconds());
 
 			VOMSACService acService = new DefaultVOMSACService(certChainValidator,
 					requestListener, 
 					serverInfoStoreListener);
 
 			AttributeCertificate ac = acService.getVOMSAttributeCertificate(
 					cred, request);
 
 			if (ac != null)
 				acs.add(ac);
 		}
 
 		if (!vomsCommandsMap.keySet().isEmpty() && acs.isEmpty())
 			throw new VOMSError("Unable to satisfy user request!");
 		return acs;
 	}
 
 	
 	private LoadCredentialsStrategy strategyFromParams(ProxyInitParams params){
 		
 		if (params.isNoRegen())
 			return new LoadProxyCredential(loadCredentialsEventListener);
 		
 		if (params.getCertFile()!=null && params.getKeyFile()!=null)
 			return new LoadUserCredential(loadCredentialsEventListener, params.getCertFile(), params.getKeyFile());
 		
 		
 		return new DefaultLoadCredentialsStrategy(System.getProperty(DefaultLoadCredentialsStrategy.HOME_PROPERTY),
 					DefaultLoadCredentialsStrategy.TMPDIR_PROPERTY,
 					loadCredentialsEventListener);
 		
 	}
 	
 	private X509Credential lookupCredential(ProxyInitParams params) {
 
 		PasswordFinder pf = null;
 
 		if (params.isReadPasswordFromStdin())
 			pf = PasswordFinders.getNoPromptInputStreamPasswordFinder(System.in, System.out);
 		else
 			pf = PasswordFinders.getDefault();
 		
 		LoadCredentialsStrategy loadCredStrategy = strategyFromParams(params);
 		
 		return loadCredStrategy.loadCredentials(pf);
 	}
 
 }
