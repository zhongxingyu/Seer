 package com.gmail.at.zhuikov.aleksandr.root;
 
 import static com.gmail.at.zhuikov.aleksandr.root.domain.GrantedAuthority.USER;
 import static java.lang.Boolean.FALSE;
 import static java.lang.Boolean.TRUE;
 import static junit.framework.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 import java.security.cert.X509Certificate;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
 
 import com.gmail.at.zhuikov.aleksandr.root.domain.User;
 import com.gmail.at.zhuikov.aleksandr.root.repository.UserRepository;
 
 public class CertificateUserServiceTest {
 
 	private @Mock
 	UserRepository userRepository;
 	private @Mock
 	OcspGateway ocspGateway;
 	private @InjectMocks
 	CertificateUserService service = new CertificateUserService();
 
 	@Before
 	public void injectMocks() {
 		initMocks(this);
 	}
 
 	@Test
 	public void setsDefaultRoleToUser() {
 		PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(
 				"personalIdCode", mock(X509Certificate.class));
 
 		when(userRepository.findOne("identity")).thenReturn(null);
 
 		User user = (User) service.loadUserDetails(token);
 
 		assertTrue(user.getAuthorities().contains(USER));
 		verify(userRepository).save(user);
 	}
 
 	@Test
 	public void validCertificate() {
 		X509Certificate cert = mock(X509Certificate.class);
 		PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(
 				"personalIdCode", cert);
 
 		when(userRepository.findOne("identity")).thenReturn(null);
		when(ocspGateway.isValidCertificate(cert)).thenReturn(TRUE);
 
 		User user = (User) service.loadUserDetails(token);
 
 		assertTrue(user.getAuthorities().contains(USER));
 		verify(userRepository).save(user);
 	}
 
 	@Test
 	public void invalidCertificate() {
 		X509Certificate cert = mock(X509Certificate.class);
 		PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(
 				"personalIdCode", cert);
 
 		when(userRepository.findOne("identity")).thenReturn(null);
		when(ocspGateway.isValidCertificate(cert)).thenReturn(FALSE);
 
 		User user = (User) service.loadUserDetails(token);
 
 		assertTrue(user.getAuthorities().contains(USER));
 		verify(userRepository).save(user);
 	}
 }
