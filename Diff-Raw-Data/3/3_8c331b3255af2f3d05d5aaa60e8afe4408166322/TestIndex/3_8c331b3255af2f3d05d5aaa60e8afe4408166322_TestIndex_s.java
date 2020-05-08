 package it.com.atlassian.plugin.refimpl;
 
 import com.atlassian.webdriver.refapp.page.RefappPluginIndexPage;
 import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 
import javax.annotation.Nullable;
 import java.util.Set;
 
 public class TestIndex extends AbstractRefappTestCase
 {
     public void testIndex()
     {
         RefappPluginIndexPage pluginIndexPage = PRODUCT.visit(RefappPluginIndexPage.class);
         Set<String> pluginKeys = pluginIndexPage.getPluginKeys();
         pluginKeys.contains("com.atlassian.plugin.osgi.bridge");
 
 
         Set<RefappPluginIndexPage.Bundle> bundles = pluginIndexPage.getBundles();
         assertFalse("none of the plugins should be just 'Installed'", Iterables.any(bundles, new Predicate<RefappPluginIndexPage.Bundle>()
         {
             public boolean apply(RefappPluginIndexPage.Bundle bundle)
             {
                 return bundle.getState().equals("Installed");
             }
         }));
     }
 }
