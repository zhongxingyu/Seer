 package org.jsonclipse.ui.labeling;
 
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider;
 import org.jsonclipse.json.ObjectProperty;
 import org.jsonclipse.json.Value;
 
 import com.google.inject.Inject;
 
 public class JsonLabelProvider extends DefaultEObjectLabelProvider {
     @Inject
     public JsonLabelProvider(AdapterFactoryLabelProvider delegate) {
         super(delegate);
     }
 
     public String text(Value value) {
         if (value.getObjectValue() != null) {
 
             // Look for a "name" or "id" property first, as a likely candidate
             // for a good object label
             for (ObjectProperty prop : value.getObjectValue().getProperties()) {
                 Value propValue = prop.getPropValue();
                 if (propValue != null) {
                     String stringValue = propValue.getStringValue();
                     if (stringValue != null) {
                         if ("name".equalsIgnoreCase(prop.getPropName())) {
                             return prop.getPropName() + ": " + stringValue;
                         } else if ("id".equalsIgnoreCase(prop.getPropName())) {
                             return prop.getPropName() + ": " + stringValue;
                         }
                     }
                 }
             }
 
             for (ObjectProperty prop : value.getObjectValue().getProperties()) {
                 if (prop.getPropValue() != null && prop.getPropValue().getStringValue() != null) {
                    return prop.getPropName() + ": " + prop.getPropValue().getStringValue();
                 }
             }
 
             return "Object";
         } else if (value.getArrayValue() != null) {
             return "Array";
         } else {
             return null;
         }
     }
 
     public String text(ObjectProperty prop) {
         return prop.getPropName();
     }
 
     public Object image(Value value) {
         if (value.getObjectValue() != null) {
             return "o.gif";
         } else if (value.getArrayValue() != null) {
             return "a.gif";
         } else {
             return null;
         }
     }
 
     public Object image(ObjectProperty prop) {
         if (prop.getPropValue() != null) {
             return image(prop.getPropValue());
         } else {
             return null;
         }
     }
 }
