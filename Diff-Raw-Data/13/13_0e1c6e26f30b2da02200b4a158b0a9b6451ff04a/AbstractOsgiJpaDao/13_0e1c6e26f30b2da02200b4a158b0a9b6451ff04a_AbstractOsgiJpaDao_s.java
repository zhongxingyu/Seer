 /**
  * Copyright 2013 Maxime Bossard
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package fr.mby.utils.common.jpa;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 
 import org.eclipse.gemini.blueprint.context.BundleContextAware;
 import org.osgi.framework.BundleContext;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.Assert;
 
 /**
  * @author Maxime Bossard - 2013
  * 
  */
 public abstract class AbstractOsgiJpaDao implements BundleContextAware, InitializingBean, DisposableBean {
 
 	private BundleContext bundleContext;
 
 	private EntityManagerFactory emf;
 
 	protected abstract String getPersistenceUnitName();
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		Assert.hasText(this.getPersistenceUnitName(), "Persistence Unit name not configured !");
 		Assert.notNull(this.bundleContext, "BundleContext was not injected !");
 
 		this.emf = OsgiJpaHelper.retrieveEmfByName(this.bundleContext, this.getPersistenceUnitName());
 
 		Assert.notNull(this.emf, "Cannot retrieve the EntityManageFactory !");
 	}
 
 	@Override
 	public void destroy() throws Exception {
		this.emf.close();
		this.emf = null;
 	}
 
 	@Override
 	public void setBundleContext(final BundleContext bundleContext) {
 		this.bundleContext = bundleContext;
 	}
 
 	protected EntityManagerFactory getEmf() {
 		return this.emf;
 	}
 
 	protected EntityManager createEntityManager() {
 		return this.getEmf().createEntityManager();
 	}
 }
