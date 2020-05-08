 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.dfci.cccb.mev.web.configuration.container;
 
 import static org.springframework.context.annotation.FilterType.ANNOTATION;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.ComponentScan.Filter;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.RequestToViewNameTranslator;
 import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
 
 import edu.dfci.cccb.mev.dataset.client.contract.AnnotatedClassViewRegistrar;
 import edu.dfci.cccb.mev.dataset.client.contract.JavascriptInjectorRegistry;
 import edu.dfci.cccb.mev.dataset.client.prototype.MevClientConfigurerAdapter;
 import edu.dfci.cccb.mev.web.domain.reflection.Reflector;
 import edu.dfci.cccb.mev.web.domain.reflection.spring.SpringReflector;
 
 /**
  * @author levk
  * 
  */
 @Configuration
 @ComponentScan (basePackages = "edu.dfci.cccb.mev.web",
                 excludeFilters = @Filter (type = ANNOTATION, value = Configuration.class),
                 includeFilters = @Filter (type = ANNOTATION, value = { Controller.class, ControllerAdvice.class }))
 public class ContainerConfigurations extends MevClientConfigurerAdapter {
 
   @Bean
   public Reflector reflection () {
     return new SpringReflector ();
   }
 
   /* (non-Javadoc)
    * @see
    * edu.dfci.cccb.mev.dataset.client.prototype.MevClientConfigurerAdapter#
    * registerAnnotatedClassViews
    * (edu.dfci.cccb.mev.dataset.client.contract.AnnotatedClassViewRegistrar) */
   @Override
   public void registerAnnotatedClassViews (AnnotatedClassViewRegistrar annotatedClassViewRegistrar) {
     annotatedClassViewRegistrar.register (Views.class);
   }
 
   /* (non-Javadoc)
    * @see
    * edu.dfci.cccb.mev.dataset.client.prototype.MevClientConfigurerAdapter#
    * registerJavascriptInjectors
    * (edu.dfci.cccb.mev.dataset.client.contract.JavascriptInjectorRegistry) */
   @Override
   public void registerJavascriptInjectors (JavascriptInjectorRegistry registry) {
     registry.register ("/container/javascript/main.js");
   }
 
   /* (non-Javadoc)
    * @see
    * org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
    * #addResourceHandlers
    * (org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry) */
   @Override
   public void addResourceHandlers (ResourceHandlerRegistry registry) {
     registry.addResourceHandler ("/container/javascript/**")
             .addResourceLocations ("classpath:/edu/dfci/cccb/mev/web/javascript/");
     registry.addResourceHandler ("/container/style/**")
             .addResourceLocations ("classpath:/edu/dfci/cccb/mev/web/style/");
   }
   
   @Bean (name="viewNameTranslator")
   public RequestToViewNameTranslator viewNameTranslator(){
     return new MevRequestToViewNameTranslator ();
   }
 }
