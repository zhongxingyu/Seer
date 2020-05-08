 package org.apache.myfaces.scripting.facelet.support;
 
 import javax.faces.component.UIComponent;
 import javax.faces.view.facelets.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
 * we have to reimplement the component rule class here
  * because it is declared private in the original
  * implementation
  */
 public final class ComponentRule extends MetaRule
 {
 
     final class LiteralAttributeMetadata extends Metadata
     {
         private final String _name;
         private final String _value;
 
         public LiteralAttributeMetadata(String name, String value)
         {
             _name = name;
             _value = value;
         }
 
         public void applyMetadata(FaceletContext ctx, Object instance)
         {
             ((UIComponent) instance).getAttributes().put(_name, _value);
         }
     }
 
     final static class ValueExpressionMetadata extends Metadata
     {
         private final String _name;
 
         private final TagAttribute _attr;
 
         private final Class<?> _type;
 
         public ValueExpressionMetadata(String name, Class<?> type, TagAttribute attr)
         {
             _name = name;
             _attr = attr;
             _type = type;
         }
 
         public void applyMetadata(FaceletContext ctx, Object instance)
         {
             ((UIComponent) instance).setValueExpression(_name, _attr.getValueExpression(ctx, _type));
         }
     }
 
     //private final static Logger log = Logger.getLogger("facelets.tag.component");
     private final static Logger log = Logger.getLogger(ComponentRule.class.getName());
 
     public final static ComponentRule Instance = new ComponentRule();
 
     public ComponentRule()
     {
         super();
     }
 
     public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta)
     {
         if (meta.isTargetInstanceOf(UIComponent.class))
         {
             // if component and dynamic, then must set expression
             if (!attribute.isLiteral())
             {
                 Class<?> type = meta.getPropertyType(name);
                 if (type == null)
                 {
                     type = Object.class;
                 }
 
                 return new ValueExpressionMetadata(name, type, attribute);
             }
             else if (meta.getWriteMethod(name) == null)
             {
 
                 // this was an attribute literal, but not property
                 warnAttr(attribute, meta.getTargetClass(), name);
 
                 return new LiteralAttributeMetadata(name, attribute.getValue());
             }
         }
         return null;
     }
 
     private static void warnAttr(TagAttribute attr, Class<?> type, String n)
     {
         if (log.isLoggable(Level.FINER))
         {
             log.finer(attr + " Property '" + n + "' is not on type: " + type.getName());
         }
     }
 
 }
 
