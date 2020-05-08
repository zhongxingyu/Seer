 package com.sun.identity.admin.model;
 
 import com.sun.identity.admin.handler.StaticAttributesHandler;
 import com.sun.identity.entitlement.ResourceAttribute;
 import com.sun.identity.entitlement.StaticAttributes;
import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import static com.sun.identity.admin.model.AttributesBean.AttributeType.*;
 
 public class StaticAttributesBean extends AttributesBean {
 
     public StaticAttributesBean() {
         super();
     }
 
     public ViewAttribute newViewAttribute() {
         return new StaticViewAttribute();
     }
 
     public AttributeType getAttributeType() {
         return STATIC;
     }
 
     public StaticAttributesBean(Set<ResourceAttribute> resourceAttributes) {
         this();
 
         for (ResourceAttribute ras: resourceAttributes) {
             if (!(ras instanceof StaticAttributes)) {
                 continue;
             }
 
             String key = ras.getPropertyName();
             for (String val : ras.getPropertyValues()) {
                 StaticViewAttribute sva = new StaticViewAttribute();
                 sva.setName(key);
                 sva.setValue(val);
                 getViewAttributes().add(sva);
             }
         }
     }
 
     public Set<ResourceAttribute> toResourceAttributesSet() {
        Set<ResourceAttribute> resAttributes = new HashSet<ResourceAttribute>();
 
         for (ViewAttribute va: getViewAttributes()) {
             if (!(va instanceof StaticViewAttribute)) {
                 continue;
             }
             StaticViewAttribute sva = (StaticViewAttribute)va;
            StaticAttributes sas = new StaticAttributes();
            
            sas.setPropertyName(sva.getName());
            sas.setPropertyValues(Collections.singleton(sva.getValue()));
            resAttributes.add(sas);
         }
 
        return resAttributes;
     }
 
     @Override
     public void reset() {
         super.reset();
         setAttributesHandler(new StaticAttributesHandler(this));
     }
 }
