 /*
  * Copyright (c) 2011: Edmund Wagner, Wolfram Weidel
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * * Neither the name of the jeconfig nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jeconfig.client.annotation.configclass;
 
 import javassist.util.proxy.ProxyObject;
 import junit.framework.Assert;
 
 import org.jeconfig.api.annotation.ConfigClass;
 import org.jeconfig.api.annotation.ConfigSimpleProperty;
 import org.jeconfig.api.scope.GlobalScopeDescriptor;
 import org.jeconfig.api.scope.ScopePathBuilderFactory;
 import org.jeconfig.client.AbstractConfigServiceTest;
 import org.jeconfig.client.annotation.map.SimpleTypeMapPropertyTestConfiguration;
 import org.jeconfig.client.annotation.map.UseParentMergingStrategyTestConfiguration;
 import org.junit.Test;
 
 @SuppressWarnings("nls")
 public class ConfigClassTest extends AbstractConfigServiceTest {
 
 	@Test
 	public void testDefaultFactory() {
 		final TestConfiguration cfg = getConfigService().load(TestConfiguration.class);
 		Assert.assertEquals("f1", cfg.getField1());
 		Assert.assertEquals(Integer.valueOf(2), cfg.getField2());
 		Assert.assertEquals(Integer.valueOf(3), cfg.getSimpleList().get(0));
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testSaveUnKnownScope() {
 		final UnknownScopeTestConfiguration config = getConfigService().load(UnknownScopeTestConfiguration.class);
 		config.setCounter(5);
 		config.setUser("Lukas");
 		getConfigService().save(config);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testMissingConfigClassAnnotation() {
 		final MissingConfigAnnoConfiguration config = getConfigService().load(MissingConfigAnnoConfiguration.class);
 		config.setCounter(-2);
 		config.setUser("Wolfram");
 		getConfigService().save(config);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testMissingDefaultConstructor() {
 		final MissingDefaultConstructorConfiguration config = getConfigService().load(
 				MissingDefaultConstructorConfiguration.class);
 		config.setCounter(-2);
 		config.setUser("Wolfram");
 		getConfigService().save(config);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testScopePathClassScope() {
 		final ClassScopeTestConfiguration config = getConfigService().load(ClassScopeTestConfiguration.class);
 		getConfigService().save(config);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testRedundantScope() {
 		final RedundantScopeTestConfiguration config = getConfigService().load(RedundantScopeTestConfiguration.class);
 		getConfigService().save(config);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testScopePathCodeDefaultScope() {
 		final CodeDefaultTestConfiguration config = getConfigService().load(CodeDefaultTestConfiguration.class);
 		getConfigService().save(config);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testSaveToScopePathWrongConfigClassInScopePath() {
 		final ScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(
 				SimpleTypeMapPropertyTestConfiguration.class);
 		final UseParentMergingStrategyTestConfiguration parentConfig = getConfigService().load(
 				UseParentMergingStrategyTestConfiguration.class);
 		getConfigService().copyToScopePath(parentConfig, factory.annotatedPathUntil(GlobalScopeDescriptor.NAME).create());
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testSaveToScopePathWrongConfigClassInScopePathWithProxy() {
 		UseParentMergingStrategyTestConfiguration parentConfig = getConfigService().load(
 				UseParentMergingStrategyTestConfiguration.class);
 		final ScopePathBuilderFactory factory = getConfigService().getScopePathBuilderFactory(
 				SimpleTypeMapPropertyTestConfiguration.class);
 		getConfigService().save(parentConfig);
 		parentConfig = getConfigService().load(UseParentMergingStrategyTestConfiguration.class);
 		Assert.assertTrue(ProxyObject.class.isAssignableFrom(parentConfig.getClass()));
 		getConfigService().copyToScopePath(parentConfig, factory.annotatedPath().create());
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testComplexAndSimplePropertyAnnoOnGetter() {
 		final ComplexAndSimplePropertyAnnoOnGetterConfiguration config = getConfigService().load(
 				ComplexAndSimplePropertyAnnoOnGetterConfiguration.class);
 		config.setI(9);
 		getConfigService().save(config);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testInlineConfigClass() {
		final TestConfiguration config = getConfigService().load(new TestConfiguration() {
			private static final long serialVersionUID = 1L;
		}.getClass());
 		getConfigService().save(config);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testInnerPrivateConfigClass() {
 		final InnerPrivateClass privateClassConfig = getConfigService().load(InnerPrivateClass.class);
 		privateClassConfig.setI(10);
 		getConfigService().save(privateClassConfig);
 	}
 
 	@Test
 	public void testInnerStaticPublicConfigClass() {
 		final InnerStaticPublicClass innerPublicClassConfig = getConfigService().load(InnerStaticPublicClass.class);
 		innerPublicClassConfig.setI(10);
 		getConfigService().save(innerPublicClassConfig);
 		final InnerStaticPublicClass loadedConfig = getConfigService().load(InnerStaticPublicClass.class);
 		Assert.assertEquals(innerPublicClassConfig, loadedConfig);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testInnerNonStaticPublicConfigClass() {
 		final InnerNonStaticPublicClass innerNonStaticPublicClassConfig = getConfigService().load(InnerNonStaticPublicClass.class);
 		innerNonStaticPublicClassConfig.setI(10);
 		getConfigService().save(innerNonStaticPublicClassConfig);
 	}
 
 	@ConfigClass(scopePath = GlobalScopeDescriptor.NAME)
 	private static class InnerPrivateClass {
 		private int i;
 
 		@SuppressWarnings("unused")
 		@ConfigSimpleProperty
 		public int getI() {
 			return i;
 		}
 
 		public void setI(final int i) {
 			this.i = i;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + i;
 			return result;
 		}
 
 		@Override
 		public boolean equals(final Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (!(obj instanceof InnerPrivateClass)) {
 				return false;
 			}
 			final InnerPrivateClass other = (InnerPrivateClass) obj;
 			if (i != other.i) {
 				return false;
 			}
 			return true;
 		}
 	}
 
 	@ConfigClass(scopePath = GlobalScopeDescriptor.NAME)
 	public static class InnerStaticPublicClass {
 		private int i;
 
 		public InnerStaticPublicClass() {}
 
 		@ConfigSimpleProperty
 		public int getI() {
 			return i;
 		}
 
 		public void setI(final int i) {
 			this.i = i;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + i;
 			return result;
 		}
 
 		@Override
 		public boolean equals(final Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (!(obj instanceof InnerStaticPublicClass)) {
 				return false;
 			}
 			final InnerStaticPublicClass other = (InnerStaticPublicClass) obj;
 			if (i != other.i) {
 				return false;
 			}
 			return true;
 		}
 	}
 
 	@ConfigClass(scopePath = GlobalScopeDescriptor.NAME)
 	public class InnerNonStaticPublicClass {
		private static final long serialVersionUID = 1L;
 		private int i;
 
 		public InnerNonStaticPublicClass() {}
 
 		@ConfigSimpleProperty
 		public int getI() {
 			return i;
 		}
 
 		public void setI(final int i) {
 			this.i = i;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + i;
 			return result;
 		}
 
 		@Override
 		public boolean equals(final Object obj) {
 			if (this == obj) {
 				return true;
 			}
 			if (obj == null) {
 				return false;
 			}
 			if (!(obj instanceof InnerNonStaticPublicClass)) {
 				return false;
 			}
 			final InnerNonStaticPublicClass other = (InnerNonStaticPublicClass) obj;
 			if (i != other.i) {
 				return false;
 			}
 			return true;
 		}
 	}
 }
