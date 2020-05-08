 package com.atlassian.sal.ctk.test;
 
 import java.util.Collection;
 
 import org.springframework.stereotype.Component;
 
 import com.atlassian.plugin.PluginController;
 import com.atlassian.sal.api.component.ComponentLocator;
 import com.atlassian.sal.ctk.CtkTest;
 import com.atlassian.sal.ctk.CtkTestResults;
 import com.atlassian.sal.spi.HostContextAccessor;
 
 @Component
 public class ComponentLocatorTest implements CtkTest
 {
 
     public void execute(final CtkTestResults results)
     {
         try
         {
             final PluginController mgr = ComponentLocator.getComponent(PluginController.class);
             results.assertTrue("PluginController accessible in ComponentLocator", mgr != null);
 
             final Collection<PluginController> c = ComponentLocator.getComponents(PluginController.class);
             results.assertTrue("Should be one PluginController found", c != null && !c.isEmpty());
            results.assertTrueOrWarn("There should be only one PluginController", c.size()==1);
 
             results.assertTrueOrWarn("HostContextAccessor should be accessible in ComponentLocator unless the application provides its own ComponentLocator and ", ComponentLocator.getComponent(HostContextAccessor.class) != null);
         } catch (final UnsupportedOperationException ex)
         {
             results.fail("ComponentLocator operations should be supported");
             ex.printStackTrace();
         }
     }
 }
