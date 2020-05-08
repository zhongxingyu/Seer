 /**
  * 
  */
 package com.dart.archive.image.search.site;
 
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ConfigurableApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 import com.github.nhuray.dropwizard.spring.SpringBundle;
 import com.yammer.dropwizard.Service;
 import com.yammer.dropwizard.config.Bootstrap;
 import com.yammer.dropwizard.config.Environment;
 
 /**
  * @author Massimiliano.Gerardi
  *
  */
 public class ImageSearchApp extends Service<ImageSearchConfiguration> {
 
 	public static void main(String[] args) throws Exception {
         new ImageSearchApp().run(args);
     }
 
 	@Override
 	public void initialize(Bootstrap<ImageSearchConfiguration> bootstrap) {
 		bootstrap.setName("image-search");
 		bootstrap.addBundle(new SpringBundle<ImageSearchConfiguration>(applicationContext(), true, true));
 	}
 
 	@Override
 	public void run(ImageSearchConfiguration configuration, Environment environment) throws Exception {
 	}
 
     private ConfigurableApplicationContext applicationContext() throws BeansException {
     	AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
     	context.scan("com.dart.archive.image.search.site");
 //    	ApplicationContext classpathContext = new ClassPathXmlApplicationContext("classpath*:/applicationContext.xml");
     	ApplicationContext classpathContext = new FileSystemXmlApplicationContext("applicationContext.xml");
     	context.setParent(classpathContext);
     	return context;
       }
 
 }
