 package de.test;
 
 import org.apache.commons.lang.SerializationUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.WindowScoped;
 import org.apache.myfaces.extensions.cdi.core.impl.projectstage.ProjectStageProducer;
 import org.apache.myfaces.extensions.cdi.jsf.impl.scope.conversation.spi.EditableWindowContext;
 import org.apache.myfaces.extensions.cdi.jsf2.impl.request.DefaultRequestTypeResolver;
 
 import javax.ejb.EJB;
 import javax.inject.Named;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 
 @Named
 @WindowScoped
 public class PassivationCapableBean implements Serializable {
 
     private String stringValue;
     private Integer intValue;
 
     @EJB
     private EjbBeanIntf ejbBean;
 
     public String getStringValue() {
         return stringValue;
     }
 
     public void setStringValue(String stringValue) {
         this.stringValue = stringValue;
     }
 
     public Integer getIntValue() {
         return intValue;
     }
 
     public void setIntValue(Integer intValue) {
         this.intValue = intValue;
     }
 
     public String submit(){
        System.out.println("submit");
         return null;
     }
 }
