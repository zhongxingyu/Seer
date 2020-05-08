 package com.atlassian.plugin.osgi.factory.transform.model;
 
 import org.dom4j.Element;
 import org.apache.commons.lang.Validate;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import static com.atlassian.plugin.util.validation.ValidationPattern.createPattern;
 import static com.atlassian.plugin.util.validation.ValidationPattern.test;
 import com.atlassian.plugin.PluginParseException;
 
 /**
  * Represents the data in a component-import tag in the plugin descriptor
  *
  * @since 2.2.0
  */
 public class ComponentImport
 {
     private final String key;
     private final Set<String> interfaces;
     private final Element source;
 
     public ComponentImport(Element element) throws PluginParseException
     {
         Validate.notNull(element);
         createPattern().
                 rule(
                         test("@key").withError("The key is required"),
                        test("(@interface and string-length(@interface) > 0) or (interface and string-length(interface[1]) > 0)")
                                 .withError("The interface must be specified either via the 'interface'" +
                                     "attribute or child 'interface' elements")).
                 evaluate(element);
 
         this.source = element;
         this.key = element.attributeValue("key");
         this.interfaces = new HashSet<String>();
         if (element.attribute("interface") != null)
         {
             interfaces.add(element.attributeValue("interface"));
         }
         else
         {
             List<Element> compInterfaces = element.elements("interface");
             for (Element inf : compInterfaces)
             {
                 interfaces.add(inf.getTextTrim());
             }
         }
     }
 
     public String getKey()
     {
         return key;
     }
 
     public Set<String> getInterfaces()
     {
         return interfaces;
     }
 
     public Element getSource()
     {
         return source;
     }
 }
