 package com.alcatel_lucent.jenkins.plugins;
 
 import jenkins.model.Jenkins;
 import hudson.Extension;
 import hudson.Plugin;
 import hudson.model.ManagementLink;
 import hudson.model.RootAction;
 
 public class SlaveInformations extends Plugin {
 
 	
 	private String name;
 	@Extension
 	public static class MyLink extends ManagementLink implements RootAction{
 
		@Override
		public String getIconFileName() {
 
			return "/plugin/infos/icon.jpg";
 		}
 		
		public String getDisplayName() {
 
			return "Slave Informations";
 		}
 		@Override
 		public String getUrlName() {
 
			return Jenkins.getInstance().getRootUrl()+ "plugin/infos/";
 		}
 
 		
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 
 }
