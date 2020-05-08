 package se.knifhen.javarebel.plugin;
 
 
 import org.zeroturnaround.javarebel.ClassResourceSource;
 import org.zeroturnaround.javarebel.IntegrationFactory;
 import org.zeroturnaround.javarebel.Plugin;
 
 /**
  * Javarebel plugin that makes any changes to Stripes ActionBeans load without restarting the application server. 
  * @author Andreas Knifh
  */
 public class StripesPlugin implements Plugin {
 	
 	/**
 	 * Adds the IntegrationProcessors.
 	 */
 	public void preinit() {
 		IntegrationFactory.getInstance().addIntegrationProcessor("net.sourceforge.stripes.controller.NameBasedActionResolver", new NameBasedActionResolverBytecodeProcessor());
 		IntegrationFactory.getInstance().addIntegrationProcessor("net.sourceforge.stripes.controller.UrlBindingFactory", new UrlBindingFactoryBytecodeProcessor());
 		IntegrationFactory.getInstance().addIntegrationProcessor("net.sourceforge.stripes.integration.spring.SpringHelper", new SpringHelperBytecodeProcessor());
 	}
 
 	public boolean checkDependencies(ClassLoader cl, ClassResourceSource crs) {
 		return crs.getClassResource("net.sourceforge.stripes.controller.NameBasedActionResolver") != null;
 	}
 
 	public String getDescription() {
 		return "Adds reloading of Stripes ActionBeans.";
 	}
 
 	public String getId() {
 		return "stripes_plugin";
 	}
 
 	public String getName() {
		return "Stripes plugin 1.0.6";
 	}
 	
 	public String getAuthor() {
 		return "Andreas Knifh";
 	}
 	
 	public String getWebsite() {
 		return "http://github.com/knifhen/javarebel-stripes-plugin/tree/master";
 	}
 
 }
