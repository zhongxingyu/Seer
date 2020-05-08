 package com.dsklyut.virgo.osgi.javanaming.bridge.test.bundle;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.util.Assert;
 
 import javax.annotation.PostConstruct;
 
 /**
  * User: dsklyut
  * Date: Oct 8, 2010
  * Time: 1:57:48 PM
  */
public class Validator implements org.springframework.beans.factory.InitializingBean {
 
     private final Logger logger = LoggerFactory.getLogger(Validator.class);
 
     private Object simple;
     private Object full;
     private Object simpleFqn;
     private Object fullFqn;
     private Object osgiFqn;
     private Object osgiFilter;
 
     public Object getSimple() {
         return simple;
     }
 
     public void setSimple(Object simple) {
         this.simple = simple;
     }
 
     public Object getFull() {
         return full;
     }
 
     public void setFull(Object full) {
         this.full = full;
     }
 
     public Object getSimpleFqn() {
         return simpleFqn;
     }
 
     public void setSimpleFqn(Object simpleFqn) {
         this.simpleFqn = simpleFqn;
     }
 
     public Object getFullFqn() {
         return fullFqn;
     }
 
     public void setFullFqn(Object fullFqn) {
         this.fullFqn = fullFqn;
     }
 
	@Override
     public void afterPropertiesSet() throws Exception {
 
         boolean result = validate("simple", this.simple);
         result = validate("simpleFqn", this.simpleFqn) || result;
         result = validate("full", this.full) || result;
         result = validate("fullFqn", this.fullFqn) || result;
         result = validate("osgiFqn", this.osgiFqn) || result;
         result = validate("osgiFilter", this.osgiFilter) || result;
 
         Assert.state(!result, "There where errors, check logs");
     }
 
     private boolean validate(String name, Object obj) {
         if (this.simple == null) {
             logger.error("{} is null", name);
			System.err.println(String.format("%s is null", name ));
             return true;
         }
        logger.error("{} was set and is type of {} and toString {}", new Object[]{name, obj.getClass(), obj.toString()});
		System.err.println(String.format("%s was set and is type of %s and toString %s", name, obj.getClass().toString(), obj.toString()));
         return false;
     }
 
     public void setOsgiFqn(Object osgiFqn) {
         this.osgiFqn = osgiFqn;
     }
 
     public Object getOsgiFqn() {
         return osgiFqn;
     }
 
     public void setOsgiFilter(Object osgiFilter) {
         this.osgiFilter = osgiFilter;
     }
 
     public Object getOsgiFilter() {
         return osgiFilter;
     }
 }
