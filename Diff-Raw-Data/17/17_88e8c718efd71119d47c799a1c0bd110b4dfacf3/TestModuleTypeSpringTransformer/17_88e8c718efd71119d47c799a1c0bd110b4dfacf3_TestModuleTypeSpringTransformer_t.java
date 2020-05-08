 package com.atlassian.plugin.osgi.factory.transform;
 
 import junit.framework.TestCase;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 import java.io.IOException;
 
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;

 public class TestModuleTypeSpringTransformer extends TestCase
 {
     public void testTransform() throws IOException
     {
         Element pluginRoot = DocumentHelper.createDocument().addElement("atlassian-plugin");
         Element moduleType = pluginRoot.addElement("module-type");
         moduleType.addAttribute("key", "foo");
         moduleType.addAttribute("class", "my.FooDescriptor");
 
        SpringTransformerTestHelper.transform(new ModuleTypeSpringTransformer(), pluginRoot,
                "beans:bean[@id='moduleType-foo' and @class='"+ SingleModuleDescriptorFactory.class.getName()+"']",
                "osgi:service[@id='moduleType-foo_osgiService']");
     }
 
 }
