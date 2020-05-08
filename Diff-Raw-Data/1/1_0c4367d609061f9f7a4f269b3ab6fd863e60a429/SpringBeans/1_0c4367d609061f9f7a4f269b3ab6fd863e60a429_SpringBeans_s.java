 package com.github.pchudzik.gae.test.config;
 
 import org.datanucleus.api.jpa.PersistenceProviderImpl;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.EnableLoadTimeWeaving;
 import org.springframework.instrument.classloading.LoadTimeWeaver;
 import org.springframework.instrument.classloading.SimpleLoadTimeWeaver;
 import org.springframework.orm.jpa.JpaTransactionManager;
 import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
 import org.springframework.transaction.PlatformTransactionManager;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import javax.persistence.EntityManagerFactory;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * User: pawel
  * Date: 20.07.13
  * Time: 11:15
  */
 @Configuration
 @EnableTransactionManagement
 @EnableLoadTimeWeaving
 public class SpringBeans {
 	@Bean
 	public LoadTimeWeaver loadTimeWeaver() {
 		return new SimpleLoadTimeWeaver();
 	}
 
 	@Bean(name = "entityManagerFactory")
 	public EntityManagerFactory getEntityManagerFactorySpringWay() {
 		Map<String, String> jpaProperties = new HashMap<String, String>(){{
 			put("datanucleus.NontransactionalRead", "true");
 			put("datanucleus.NontransactionalWrite", "true");
 			put("datanucleus.ConnectionURL", "appengine");
 			put("datanucleus.singletonEMFForName", "true");
 			put("datanucleus.metadata.allowLoadAtRuntime", "true");	//magic property not mentioned in gea docs but required when working with maven
 
 		}};
 		LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
 		entityManagerFactoryBean.setPersistenceProviderClass(PersistenceProviderImpl.class);
 		entityManagerFactoryBean.setPackagesToScan("com.github.pchudzik.gae.test.domain");
 		entityManagerFactoryBean.setLoadTimeWeaver(loadTimeWeaver());
 		entityManagerFactoryBean.afterPropertiesSet();
 		EntityManagerFactory result = entityManagerFactoryBean.getObject();
 		return result;
 	}
 
 	@Bean @Autowired
 	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
 		JpaTransactionManager txManager = new JpaTransactionManager();
 		txManager.setEntityManagerFactory(entityManagerFactory);
 		return txManager;
 	}
 }
