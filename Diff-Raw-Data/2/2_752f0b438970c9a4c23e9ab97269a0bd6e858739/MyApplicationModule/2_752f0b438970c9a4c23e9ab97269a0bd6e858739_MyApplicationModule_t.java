 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package};
 
 import ${package}.providers.WebApplicationLocaleProvider;
 import ${package}.util.WebApplicationLocale;
 import net.contextfw.web.application.ModuleConfiguration;
 import net.contextfw.web.application.WebApplicationModule;
 import net.contextfw.web.application.WebApplicationServletModule;
 
 import org.guiceyfruit.jsr250.Jsr250Module;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.name.Names;
 
 public class MyApplicationModule extends AbstractModule {
 
     public MyApplicationModule() {}
 
     @Override
     protected void configure() {
 
         ModuleConfiguration config = new ModuleConfiguration()
             .attributeHandlerClass(MyAttributeHandler.class)
            .addResourcePaths("${package}")
             .initializerRootPackages("${package}.views")
             .debugMode(true);
         
         config.setXmlParamName("xml");
         config.setLogXML(true);
         
         bind(WebApplicationLocale.class).toProvider(WebApplicationLocaleProvider.class);
         
         install(new WebApplicationModule(config));
         install(new Jsr250Module());
         install(new WebApplicationServletModule(config));
     }
 }
