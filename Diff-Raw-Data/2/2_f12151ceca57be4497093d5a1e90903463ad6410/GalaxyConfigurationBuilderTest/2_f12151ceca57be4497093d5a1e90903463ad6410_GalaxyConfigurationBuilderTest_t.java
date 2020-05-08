 package org.mule.galaxy.mule2;
 
 
 import org.mule.api.MuleContext;
 import org.mule.api.model.Model;
 import org.mule.context.DefaultMuleContextFactory;
 import org.mule.galaxy.mule2.config.GalaxyConfigurationBuilder;
 import org.mule.galaxy.test.AbstractAtomTest;
 import org.mule.util.IOUtils;
 
 import java.util.Properties;
 
 public class GalaxyConfigurationBuilderTest extends AbstractAtomTest
 {
     private MuleContext context;
 
     public void testMuleConfig() throws Exception
     {
        String configURL = "http://admin:admin@localhost:9002/api/registry?q=select artifact where mule2.model = 'helloSample'";
 
         GalaxyConfigurationBuilder builder = new GalaxyConfigurationBuilder(configURL);
         context = new DefaultMuleContextFactory().createMuleContext(builder);
 
         //Assert components
         Model model = context.getRegistry().lookupModel("helloSample");
         assertNotNull(model);
         assertNotNull(context.getRegistry().lookupService("GreeterUMO"));
         assertNotNull(context.getRegistry().lookupService("ChitChatUMO"));
 
     }
 
     public void testMuleConfigWithProperties() throws Exception
     {
 
         String configURL = "http://localhost:9002/api/registry";
 
         Properties properties = new Properties();
         properties.load(IOUtils.getResourceAsStream("credentials.properties", getClass(), true, false));
         GalaxyConfigurationBuilder builder = new GalaxyConfigurationBuilder(configURL, properties);
 
         context = new DefaultMuleContextFactory().createMuleContext(builder);
 
         //Assert components
         Model model = context.getRegistry().lookupModel("helloSample");
         assertNotNull(model);
         assertNotNull(context.getRegistry().lookupService("GreeterUMO"));
         assertNotNull(context.getRegistry().lookupService("ChitChatUMO"));
 
     }
 
 
     @Override
     protected void tearDown() throws Exception
     {
         try
         {
             if (context != null)
             {
                 context.dispose();
             }
         }
         finally
         {
             super.tearDown();
         }
     }
 }
