 package no.niths.services;
 
 import java.util.GregorianCalendar;
 import java.util.UUID;
 
 import no.niths.application.rest.exception.ExpiredTokenException;
 import no.niths.application.rest.exception.UnvalidTokenException;
 import no.niths.common.config.HibernateConfig;
 import no.niths.common.config.TestAppConfig;
 import no.niths.common.constants.SecurityConstants;
 import no.niths.services.auth.TokenGeneratorServiceImpl;
 
 import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = { TestAppConfig.class, HibernateConfig.class })
 public class TokenGeneratorServiceTest {
 
     private static final Logger logger = LoggerFactory
             .getLogger(TokenGeneratorServiceTest.class);
 
     @Value("${jasypt.password}")
     private String password;
     
     @Autowired
     private TokenGeneratorServiceImpl tokenService;
     
     @Test
     public void testGenerateAndVerify(){
         String token = tokenService.generateToken(new Long(21));
         tokenService.generateToken(new Long(22));
         tokenService.verifyTokenFormat(token, true);    
     }
     
     @Test(expected=ExpiredTokenException.class)
     public void testExpiredoken(){
         String unvalid = generateUnvalidToken(new Long(23));
         tokenService.verifyTokenFormat(unvalid, true);
     }
     
     @Test(expected=UnvalidTokenException.class)
     public void testUnvalidToken(){
         tokenService.verifyTokenFormat("aaaaijde876tda76fd6wafdw", true);
     }
     @Test(expected=UnvalidTokenException.class)
     public void testUnvalidToken2(){
         tokenService.verifyTokenFormat(null, true);
     }
     @Test(expected=UnvalidTokenException.class)
     public void testUnvalidToken3(){
         tokenService.generateToken(null);
     }
     
     private String generateUnvalidToken(Long userId) {
         
        long tokenIssued = new GregorianCalendar().getTimeInMillis() - (SecurityConstants.MAX_SESSION_VALID_TIME - 10);
         String generatedToken = UUID.randomUUID().toString().toUpperCase()
                 + "|" + Long.toString(userId) + "|"
                 + Long.toString(tokenIssued);
         // Encrypt the token
         StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
         jasypt.setPassword(password);
         String encryptedToked = jasypt.encrypt(generatedToken);
 
         logger.debug("Generated token before encryption: " + generatedToken);
         logger.debug("Generated token after encryption: " + encryptedToked);
 
         return encryptedToked;
     }
     
 }
