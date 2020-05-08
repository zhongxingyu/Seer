 package com.gmail.at.zhuikov.aleksandr.root;
 
 import static com.gmail.at.zhuikov.aleksandr.root.domain.GrantedAuthority.USER;
 
 import java.security.cert.X509Certificate;
 
 import javax.inject.Inject;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
 import org.springframework.stereotype.Component;
 
 import com.gmail.at.zhuikov.aleksandr.root.domain.User;
 import com.gmail.at.zhuikov.aleksandr.root.repository.UserRepository;
 
 @Component("certificateUserService")
 public class CertificateUserService implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
 
 	private static final Logger LOG = LoggerFactory.getLogger(CertificateUserService.class);
 	
 	@Inject
 	private UserRepository userRepository;
 	
 	@Inject
 	private OcspGateway ocspClient;
 	
 	@Override
 	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token)
 			throws UsernameNotFoundException {
 		
 		String personalIdCode = (String) token.getPrincipal();
 		LOG.info("User with personal ID code: " + personalIdCode);
 		
 		X509Certificate certificate = (X509Certificate) token.getCredentials();
 //		LOG.debug("User with certificate: " + certificate);
 		
		boolean valid = ocspClient.isValidCertificate(certificate);
 		LOG.info("Certificate is " + (valid ? "" : "NOT ") + "valid");
 
         User user = userRepository.findOne(personalIdCode);
 		
         if (user == null) {
         	user = new User(personalIdCode);
         	user.getAuthorities().add(USER);
         }
 
         userRepository.save(user);
 
         return user;
 	}
 }
