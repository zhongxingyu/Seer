 package org.installer;
 
 
 public class InstallerFactory {
 
 	
 	
 	public static boolean isInstallAvailable(){
 		if(NautilusInstaller.isUsingNautilus() && !NautilusInstaller.isInstalledOnNautilus()){
 			return true;
 		}
 		if(WindowsInstaller.isUsingWindows() && !WindowsInstaller.isInstalledOnWindows()){
 			return true;
 		}
 		return false;
 	}
 	
 	public static Installer getInstaller(){
 		if(NautilusInstaller.isUsingNautilus() ){
 			return new NautilusInstaller();
 		}
 		return null;
 	}
 	
 }
