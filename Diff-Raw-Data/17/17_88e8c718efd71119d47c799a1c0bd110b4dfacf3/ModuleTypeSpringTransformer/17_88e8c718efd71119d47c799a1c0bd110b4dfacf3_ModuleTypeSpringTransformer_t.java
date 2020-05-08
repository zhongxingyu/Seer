 package com.atlassian.plugin.osgi.factory.transform;
 
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentRegistration;
 import com.atlassian.plugin.ModuleDescriptorFactory;
 
 import java.util.List;
 import java.util.jar.Manifest;
 import java.io.File;
 
 import org.dom4j.Document;
 import org.dom4j.Element;
 
 /**
  * Transforms module-type plugin modules into Spring configuration
  * @since 2.1
  */
 public class ModuleTypeSpringTransformer implements SpringTransformer
 {
     public void transform(File pluginJar, Manifest mf, List<HostComponentRegistration> regs, Document pluginDoc, Document springDoc)
     {
         Element root = springDoc.getRootElement();
         List<Element> elements = pluginDoc.getRootElement().elements("module-type");
         for (Element e : elements)
         {
             Element bean = root.addElement("beans:bean");
             bean.addAttribute("id", getBeanId(e));
            bean.addAttribute("class", "com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory");
 
             Element arg = bean.addElement("beans:constructor-arg");
             arg.addAttribute("index", "0");
             Element value = arg.addElement("beans:value");
             value.setText(e.attributeValue("key"));
             Element arg2 = bean.addElement("beans:constructor-arg");
             arg2.addAttribute("index", "1");
             Element value2 = arg2.addElement("beans:value");
             value2.setText(e.attributeValue("class"));
 
             Element osgiService = root.addElement("osgi:service");
             osgiService.addAttribute("id", getBeanId(e) +"_osgiService");
             osgiService.addAttribute("ref", getBeanId(e));
             osgiService.addAttribute("interface", ModuleDescriptorFactory.class.getName());
         }
     }
 
     private String getBeanId(Element e)
     {
         return "moduleType-"+e.attributeValue("key");
     }
 }
