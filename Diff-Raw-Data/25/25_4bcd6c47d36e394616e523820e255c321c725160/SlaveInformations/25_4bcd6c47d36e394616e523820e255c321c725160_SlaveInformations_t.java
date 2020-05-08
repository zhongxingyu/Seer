 package com.alcatel_lucent.jenkins.plugins;
 
 import jenkins.model.Jenkins;
 import hudson.Extension;
 import hudson.Plugin;
 import hudson.model.ManagementLink;
 import hudson.model.RootAction;
 

/**
 * @author Olivier Ferreira <br />
 * 	olivier.ferreira@alcatel-lucent.com 
 */

 public class SlaveInformations extends Plugin {
 
 	
 	private String name;
 	@Extension
 	public static class MyLink extends ManagementLink implements RootAction{
 
		public String getDisplayName() {
 
			return "Slave Information";
 		}
 		
		@Override
		public String getIconFileName() {
 
			return "/plugin/slave/icon.jpg";
 		}
	
 		@Override
 		public String getUrlName() {
 
			return Jenkins.getInstance().getRootUrl()+ "plugin/slave/";
 		}
 
 		
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 
 }
