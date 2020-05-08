 package de.thischwa.pmcmstest.spring;
 
 import org.apache.log4j.BasicConfigurator;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 
 public class SpringExampleAnnotationsOnly {
 	
 	public static void main(String[] args) {
 		BasicConfigurator.configure();
 		
 		// init spring;
 		try {
			BeanFactory beanFactory = new AnnotationConfigApplicationContext("de.thischwa.pmcms.spring");
 			
 			Masterbean mb = (Masterbean) beanFactory.getBean("master");
 			System.out.println(mb.getInnerMessage());
 		} catch (Exception ioe) {
 			throw new RuntimeException(ioe);
 		}
 	}
 }
