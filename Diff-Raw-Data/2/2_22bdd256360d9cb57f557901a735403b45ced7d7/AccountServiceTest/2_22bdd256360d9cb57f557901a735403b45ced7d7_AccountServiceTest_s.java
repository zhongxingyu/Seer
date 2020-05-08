 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.    
  */
 package jp.dip.komusubi.lunch.service;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.util.Date;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 
 import jp.dip.komusubi.lunch.MockBootstrap;
 import jp.dip.komusubi.lunch.model.User;
 import jp.dip.komusubi.lunch.module.DefaultNonce;
 import jp.dip.komusubi.lunch.module.dao.UserDao;
 import jp.dip.komusubi.lunch.module.resolver.DateResolver;
 import jp.dip.komusubi.lunch.module.resolver.DigestResolver;
 import jp.dip.komusubi.lunch.util.Nonce;
 import junitx.util.PrivateAccessor;
 
 import org.apache.wicket.protocol.http.mock.MockServletContext;
 import org.junit.Before;
 import org.junit.Test;
 import org.komusubi.common.protocol.smtp.MailMessage;
 import org.komusubi.common.util.Resolver;
 import org.mockito.Mockito;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.TypeLiteral;
 import com.google.inject.name.Names;
 
 public class AccountServiceTest {
 //	private UserDao mock;
 	
 	@Before
 	public void prepare() {
 		ServletContext servletContext = new MockServletContext(null, null);
 		MockBootstrap boot = new MockBootstrap() {
 			@Override
 			protected Injector getInjector() {
 				return Guice.createInjector(new AbstractModule() {
 
 					@Override
 					protected void configure() {
 						bind(Nonce.class).to(DefaultNonce.class);
 						bind(new TypeLiteral<Resolver<Date>>(){})
 							.annotatedWith(Names.named("date")).toInstance(new DateResolver());
 					}
 					
 				});
 			}
 		};
 		boot.contextInitialized(new ServletContextEvent(servletContext));
 	}
 	
 	@Test
 	public void signInSuccess() {
 		Integer id = 1;
 		String email = "komusubi@email.com";
 		DigestResolver resolver = new DigestResolver();
 		String plainPassword = "password";
 		String password = resolver.resolve(plainPassword);
 		User komusubi = new User(id)
 		                    .setEmail(email)
 		                    .setPassword(password);
 		komusubi.getHealth().setActive(true);
 		
 		UserDao mock = Mockito.mock(UserDao.class);
 		when(mock.findByEmail(email)).thenReturn(komusubi);
 
 		AccountService target = new AccountService(mock, 
 									new DigestResolver(), 
 									new MockSmtpServer(),
 									new DateResolver()) {
 			private static final long serialVersionUID = -2096190310264111713L;
 
 			@Override
 			protected void temporarySupply() {
 				// nothing to do
 			}
 		};
 		assertTrue(target.signIn(email, plainPassword));
 		
 		verify(mock).findByEmail(email);
 	}
 	
 	@Test
 	public void applyNormal() throws Throwable {
 		AccountService target = new AccountService(null, new DigestResolver(), new MockSmtpServer());
 		
 		User user = new User(1)
 						.setName("こむすび")
 						.setEmail("komusubi@email.com");
 		
		Nonce nonce = target.apply(user, "http://www.domain.com/base");
 
 		MockSmtpServer server = (MockSmtpServer) PrivateAccessor.getField(target, "smtp");
 		MailMessage message = server.getMailMessage();
 		User admin = new User()
 						.setName("Lunchat")
 						.setEmail("noreply@lunchat.jp");
 		assertEquals(admin.toString(), message.getFrom().toString());
 		assertEquals("確認メール", message.getContent().getSubject());
 	}
 }
