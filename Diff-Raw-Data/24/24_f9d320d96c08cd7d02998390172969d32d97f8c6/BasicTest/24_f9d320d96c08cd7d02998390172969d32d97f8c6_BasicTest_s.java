 package org.acme;
 
 import static org.junit.Assert.*;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 
 import org.jglue.cdiunit.AdditionalClasses;
 import org.jglue.cdiunit.CdiRunner;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 @RunWith(CdiRunner.class)
 @AdditionalClasses(BasicTest.Dep.class)
 public class BasicTest {
 
 	@Inject
	Bean bean;
 	
 	@Test
 	public void cdiUnitJustWorks() {
 
 		assertNotNull(bean);
 
 		//shows it's a proxy
 		System.out.println(bean.getClass());
 		
 		//first-access causes injection and lifecycle callbacks
 		System.out.println(bean);
 	}
 	
 	
 	@ApplicationScoped //proxied scope
	static class Bean {
 		
 		//this is not a proxy
 		Dep dep;
 		
 		//must have a no=arg constructor for proxying to occur
		Bean(){};
 		
 		@Inject
		Bean(Dep foo) {
 			this.dep=foo;
 		}
 		
 		@PostConstruct
 		public void init() {
 			System.out.println(dep);
 		}
 		
 		Dep dep() {
 			return dep;
 		}
 		
 	}
 	
 	//dependent scope, hence unproxied
 	static class Dep {}
 	
 	
 }
