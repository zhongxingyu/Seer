 package com.trivadis.camel.security.test;
 
 import org.apache.camel.CamelExecutionException;
 import org.apache.camel.component.shiro.security.ShiroSecurityToken;
 import org.apache.camel.component.shiro.security.ShiroSecurityTokenInjector;
 import org.apache.camel.test.junit4.CamelSpringTestSupport;
 import org.apache.shiro.authc.IncorrectCredentialsException;
 import org.apache.shiro.authc.UnknownAccountException;
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.context.support.AbstractApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class ShiroSecurityTest extends CamelSpringTestSupport {
     private final byte[] passPhrase = "CamelSecureRoute".getBytes();
 
     @Test
     public void testShiroRouteWithValidUser() throws Exception {
         ShiroSecurityToken shiroSecurityToken = new ShiroSecurityToken("userEditor", "secret2");
 
         ShiroSecurityTokenInjector shiroSecurityTokenInjector =
                 new ShiroSecurityTokenInjector(shiroSecurityToken, passPhrase);
        template.sendBodyAndHeader("direct:findUserDataShiro", 1234567890, "SHIRO_SECURITY_TOKEN",
                shiroSecurityTokenInjector.encrypt());
     }
 
     @Test
     public void testShiroRouteWithInvalidUser() throws Exception {
         try {
             ShiroSecurityToken shiroSecurityToken = new ShiroSecurityToken("userAgent", "secret");
 
             ShiroSecurityTokenInjector shiroSecurityTokenInjector =
                     new ShiroSecurityTokenInjector(shiroSecurityToken, passPhrase);
             template.sendBodyAndHeader("direct:findUserDataShiro", 1234567890, "SHIRO_SECURITY_TOKEN",
                     shiroSecurityTokenInjector.encrypt());
         } catch (CamelExecutionException ex) {
             if (ex.getCause() instanceof IncorrectCredentialsException) {
                 // OK
             } else {
                 Assert.fail(ex.getMessage());
             }
         }
     }
 
     @Test(expected = CamelExecutionException.class)
     public void testShiroRouteWithoutToken() throws Exception {
         template.sendBody("direct:findUserDataShiro", 1234567890);
     }
 
     @Test
     public void testShiroRouteWithUnknownUser() throws Exception {
         try {
             ShiroSecurityToken shiroSecurityToken = new ShiroSecurityToken("myUser", "mySecret");
 
             ShiroSecurityTokenInjector shiroSecurityTokenInjector =
                     new ShiroSecurityTokenInjector(shiroSecurityToken, passPhrase);
             template.sendBodyAndHeader("direct:findUserDataShiro", 1234567890, "SHIRO_SECURITY_TOKEN",
                     shiroSecurityTokenInjector.encrypt());
         } catch (CamelExecutionException ex) {
             if (ex.getCause() instanceof UnknownAccountException) {
                 // OK
             } else {
                 Assert.fail(ex.getMessage());
             }
         }
     }
 
     @Override
     protected AbstractApplicationContext createApplicationContext() {
         return new ClassPathXmlApplicationContext("spring/camel-context.xml");
     }
 }
