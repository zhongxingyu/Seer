 package me.simplex.pluginkickstarter.generator;
 
 import me.simplex.pluginkickstarter.PluginKickstarter;
 import me.simplex.pluginkickstarter.storage.CommandContainer;
 
 public class GenPlugin extends Generator {
 
 	public GenPlugin(PluginKickstarter main) {
 		super(main);
 	}
 	
 	public String buildMainClass(){
		return main.getData().getPackage()+Generator.StringToClassName(main.getData().getPluginname());
 	}
 	
 	public String buildVersion(){
 		return main.getData().getVersion();
 	}
 	
 	public String buildDescription(){
 		return main.getData().getDescription();
 	}
 	
 	public String buildDepends(){
 		if (main.getData().getDepends().trim().length() > 0) {
 			return "depends: "+main.getData().getDepends().trim();
 		}
 		return "";
 	}
 	
 	public String buildSoftdepends(){
 		if (main.getData().getSoftdepends().trim().length() > 0) {
 			return "softdepends: "+main.getData().getSoftdepends().trim();
 		}
 		return "";
 	}
 	
 	public String buildCommands(){
 		if (main.getData().getCommands().size() > 0) {
 			String ret="commands:\n";
 			for (CommandContainer cmd : main.getData().getCommands()) {
 				ret=ret+cmd.buildPluginfileEntry();
 			}
 			return ret; //TODO
 		}
 		else {
 			return"";
 		}
 	}
 
 	@Override
 	public String buildClassname() {
 		return null;
 	}
 }
