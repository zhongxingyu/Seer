 /**
  * The MIT License
  *
  * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is furnished to do
  * so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package dk.nsi.minlog.export.config;
 
 import static java.lang.System.getProperty;
 
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.core.io.Resource;
 import org.springframework.scheduling.annotation.EnableScheduling;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 /**
  * Setup of the application
  *  
  * @author kpi
  * 
  */
 @Configuration
 @EnableScheduling
 @EnableTransactionManagement
@ComponentScan({"dk.nsi.minlog.export.web"})
 public class ApplicationRootConfig {	
     @Bean
     public static PropertyPlaceholderConfigurer configuration() {
         final PropertyPlaceholderConfigurer props = new PropertyPlaceholderConfigurer();
         props.setLocations(new Resource[]{
                 new ClassPathResource("default.properties"),
                 new FileSystemResource(getProperty("jboss.server.config.url") + "minlog." + getProperty("user.name") + ".properties"),
                 new ClassPathResource("minlog." + getProperty("user.name") + ".properties"),
                 new ClassPathResource("jdbc.default.properties"),
                 new FileSystemResource(getProperty("jboss.server.config.url") + "jdbc." + getProperty("user.name") + ".properties"),
                 new ClassPathResource("jdbc." + getProperty("user.name") + ".properties"),
                 new FileSystemResource(getProperty("user.home") + "/.minlog/passwords.properties")                
         });
         props.setIgnoreResourceNotFound(true);
         props.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
         
         return props;
     }
 }
