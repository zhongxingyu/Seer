 package net.contextfw.web.application.internal.component;
 
 import net.contextfw.web.application.WebApplicationException;
 import net.contextfw.web.application.component.DOMBuilder;
 import net.contextfw.web.application.component.Script;
 
 import com.google.gson.Gson;
 
 class ScriptElementBuilder extends NamedBuilder {
 
     private final ComponentBuilder componentBuilder;
     private final Gson gson;
     
     protected ScriptElementBuilder(ComponentBuilder componentBuilder, Gson gson, PropertyAccess<Object> propertyAccess, String name, String accessName) {
         super(propertyAccess, name, accessName);
         this.componentBuilder = componentBuilder;
         this.gson = gson;
     }
 
     @Override
     void buildNamedValue(DOMBuilder b, String name, Object value) {
         if (value != null) {
         	if (value instanceof Script) {
         		((Script) value).build(b.descend(name), gson, componentBuilder);
         	}
         	else {
        		throw new WebApplicationException("Instance of '"+value.getClass().getName()+"' is not a subclass of Script");
         	}
         }
     }
 }
