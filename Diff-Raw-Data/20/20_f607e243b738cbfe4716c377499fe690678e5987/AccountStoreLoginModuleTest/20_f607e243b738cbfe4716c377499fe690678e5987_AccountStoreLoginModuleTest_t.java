/**
 * Copyright 2010 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
 
 package org.waveprotocol.wave.examples.fedone.authentication;
 
 import junit.framework.TestCase;
 
 import org.waveprotocol.wave.examples.fedone.account.HumanAccountDataImpl;
 import org.waveprotocol.wave.examples.fedone.persistence.AccountStore;
 import org.waveprotocol.wave.examples.fedone.persistence.memory.MemoryStore;
 
 import javax.security.auth.Subject;
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.callback.UnsupportedCallbackException;
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 
 /**
 * @author josephg@gmail.com (Joseph Gentle)
  *
  */
 public class AccountStoreLoginModuleTest extends TestCase {
   @Override
   protected void setUp() {
     AccountStore store = new MemoryStore();
     store.putAccount(new HumanAccountDataImpl("haspwd@example.com", "pwd".toCharArray()));
     store.putAccount(new HumanAccountDataImpl("nopwd@example.com"));
     AccountStoreHolder.init(store);
   }
 
   @Override
   protected void tearDown() {
     AccountStoreHolder.clearAccountStore();
   }
 
   private class FakeCallbackHandler implements CallbackHandler {
     final String address, password;
 
     public FakeCallbackHandler(String address, String password) {
       this.address = address;
       this.password = password;
     }
 
     @Override
     public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
       for (Callback c : callbacks) {
         if (c instanceof NameCallback) {
           ((NameCallback) c).setName(address);
         } else if (c instanceof PasswordCallback) {
           ((PasswordCallback) c).setPassword(password.toCharArray());
         } else {
           throw new UnsupportedCallbackException(c);
         }
       }
     }
   }
 
   LoginContext makeLoginContext(String address, String password) throws LoginException {
     return new LoginContext(ConfigurationProvider.CONTEXT_NAME, new Subject(),
         new FakeCallbackHandler(address, password), MockConfigurationProvider.make());
   }
 
   private void assertLoginFails(LoginContext context) {
     boolean exceptionThrown = false;
     try {
       context.login();
     } catch (LoginException e) {
       // Expected behaviour.
       exceptionThrown = true;
     }
     assertTrue(exceptionThrown);
   }
 
   public void testIncorrectPasswordThrowsLoginException() throws Exception {
     LoginContext context = makeLoginContext("haspwd@example.com", "wrongpassword");
     assertLoginFails(context);
 
     // Make sure the subject doesn't have any principals set.
     assertEquals(0, context.getSubject().getPrincipals(AccountStorePrincipal.class).size());
   }
 
   public void testCorrectPasswordConfiguresSubject() throws Exception {
     LoginContext context = makeLoginContext("haspwd@example.com", "pwd");
     context.login();
     Subject subject = context.getSubject();
     boolean hasPrincipal = false;
     for (AccountStorePrincipal p : subject.getPrincipals(AccountStorePrincipal.class)) {
       assertEquals("haspwd@example.com", p.getName());
       hasPrincipal = true;
     }
     assertTrue(hasPrincipal);
 
     context.logout();
     assertEquals(0, subject.getPrincipals(AccountStorePrincipal.class).size());
   }
 
   public void testUserWithNoPasswordCannotLogin() throws Exception {
     LoginContext context = makeLoginContext("nopwd@example.com", "");
     assertLoginFails(context);
     assertEquals(0, context.getSubject().getPrincipals(AccountStorePrincipal.class).size());
   }
 }
