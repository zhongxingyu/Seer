 package biz.c24.io.fixml.sample.configuration;
 
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.ImportResource;
 
 /**
  * Created on behalf of C24 Technologies Ltd.
  *
  * @author mvickery
  * @since 03/04/2013
  */
 @ImportResource(value = {
        "classpath:META-INF/spring/c24-db-store.xml"
 })
 @Configuration
 public class C24DbConfiguration {
 
 }
