 package no.niths.services;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotSame;
 import static org.mockito.Mockito.when;
 
 import java.util.GregorianCalendar;
 import java.util.UUID;
 
 import no.niths.application.rest.exception.UnvalidTokenException;
 import no.niths.domain.Student;
 import no.niths.security.SessionToken;
 import no.niths.security.User;
 import no.niths.services.auth.AuthenticationServiceImpl;
 import no.niths.services.auth.interfaces.GoogleAuthenticationService;
 import no.niths.services.interfaces.StudentService;
 
 import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class AuthenticationServiceTest {
 
 	@Mock
 	private StudentService studRepo;
 	
 	@Mock
 	private GoogleAuthenticationService authService;
 	
 	@InjectMocks
 	private AuthenticationServiceImpl service = new AuthenticationServiceImpl();
 	
 	@Test(expected=UnvalidTokenException.class)
 	public void testAuthenticate(){
		service.setCryptionPassword("password");
 		String token = getNormalToken();
 		when(studRepo.getStudentBySessionToken(token)).thenReturn(new Student("mail@nith.no", System.currentTimeMillis()));
 		
 		User u = service.authenticateSessionToken(token);
 		assertEquals(1, u.getAuthorities().size());
 		
 		token = getExpiredToken();
 		when(studRepo.getStudentBySessionToken(token)).thenReturn(new Student("mail@nith.no", System.currentTimeMillis()));
 		
 		u = service.authenticateSessionToken(token);
 		assertEquals(1, u.getAuthorities().size());
 		
 		u = service.authenticateSessionToken(null);
 		assertEquals(1, u.getAuthorities().size());
 
 	}
 	
 	
 	@Test
 	public void testLogin(){
 		Student s = new Student("mail@nith.no");
 		s.setId(new Long(1));
 		service.setCryptionPassword("password");
 
 		//Valid email
 		when(authService.authenticateAndGetEmail("token")).thenReturn("mail@nith.no");
 		when(studRepo.getStudentByEmail("mail@nith.no")).thenReturn(s);
 				
 		SessionToken genToken = service.authenticateAtGoogle("token");
 		assertNotSame("Not a valid token provided", genToken.getToken());
 		
 		//Not a registrated user
 		s.setEmail("mail@nith.no");
 		when(authService.authenticateAndGetEmail("token")).thenReturn("mail@nith.no");
 		when(studRepo.getStudentByEmail("mail@nith.no")).thenReturn(null);
 		when(studRepo.create(new Student("mail@nith.no"))).thenReturn(new Long(1));
 		
 		genToken = service.authenticateAtGoogle("token");
 		assertNotSame("Not a valid token provided", genToken.getToken());
 	}
 	
 	private String getExpiredToken(){
 		long tokenIssued = new GregorianCalendar().getTimeInMillis() - 60000;
 		String generatedToken = UUID.randomUUID().toString().toUpperCase()
 				+ "|" + Long.toString(1) + "|" + Long.toString(tokenIssued);
 		//Encrypt the token
 		StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
		jasypt.setPassword("password");
 		return jasypt.encrypt(generatedToken);		
 	}
 	
 	private String getNormalToken(){
 		long tokenIssued = new GregorianCalendar().getTimeInMillis();
 		String generatedToken = UUID.randomUUID().toString().toUpperCase()
 				+ "|" + Long.toString(1) + "|" + Long.toString(tokenIssued);
 		//Encrypt the token
 		StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
		jasypt.setPassword("password");
 		return jasypt.encrypt(generatedToken);
 	}
 }
