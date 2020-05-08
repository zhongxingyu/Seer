 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://www.3wks.com.au/thundr
  * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.threewks.thundr.jpa;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.util.Map;
 
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.servlet.ServletContext;
 
 import com.threewks.thundr.jpa.intercept.Transactional;
 import com.threewks.thundr.jpa.intercept.TransactionalActionInterceptor;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Matchers;
 import org.mockito.Mockito;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import com.threewks.thundr.action.method.ActionInterceptorRegistry;
 import com.threewks.thundr.injection.InjectionContextImpl;
 import com.threewks.thundr.injection.UpdatableInjectionContext;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(Persistence.class)
 public class JpaInjectionConfigurationTest {
 	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();
 
 	@Before
 	public void before() {
 		PowerMockito.mockStatic(Persistence.class);
 		when(Persistence.createEntityManagerFactory(Mockito.anyString())).thenReturn(mock(EntityManagerFactory.class));
 
 		injectionContext.inject(mock(ServletContext.class)).as(ServletContext.class);
 		injectionContext.inject("default:local").named(JpaInjectionConfiguration.PersistenceManagersConfigName).as(String.class);
 
 		ActionInterceptorRegistry actionInterceptorRegistry = mock(ActionInterceptorRegistry.class);
 		injectionContext.inject(actionInterceptorRegistry).as(ActionInterceptorRegistry.class);
 
 		new JpaInjectionConfiguration().configure(injectionContext);
 	}
 
 	@Test
 	public void shouldInjectTransactionalActionInterceptor() {
 		ActionInterceptorRegistry registry = injectionContext.get(ActionInterceptorRegistry.class);
 		verify(registry).registerInterceptor(Matchers.eq(Transactional.class), Matchers.any(TransactionalActionInterceptor.class));
 	}
 
 	@Test
 	public void shouldInjectPersistenceManagerRegistry() {
 		PersistenceManagerRegistry registry = injectionContext.get(PersistenceManagerRegistry.class);
 		assertThat(registry, is(notNullValue()));
 		assertThat(registry.get("default"), is(notNullValue()));
 	}
 
 	@Test
 	public void shouldReturnDefaultPersistenceManagerAndPersistenceUnitNameIfNoneSet() {
 		PowerMockito.mockStatic(Persistence.class);
 		when(Persistence.createEntityManagerFactory(Mockito.anyString())).thenReturn(mock(EntityManagerFactory.class));
 
 		injectionContext = new InjectionContextImpl();
 		injectionContext.inject(mock(ServletContext.class)).as(ServletContext.class);
 
 		ActionInterceptorRegistry actionInterceptorRegistry = mock(ActionInterceptorRegistry.class);
 		injectionContext.inject(actionInterceptorRegistry).as(ActionInterceptorRegistry.class);
 
 		JpaInjectionConfiguration config = new JpaInjectionConfiguration();
 		Map<String, String> results = config.getPersistenceUnitNames(injectionContext);
 		assertThat(results.size(), is(1));
 		assertThat(results, hasEntry("default", "default"));
 	}
 }
