 package jig.ironLegends.core;
 
 import java.io.File;
 import java.net.URL;
 
 public class InstallInfo {
 
 	static public String getInstallDir(String className, String jarName) {
 		String sInstallDir = "/Temp";
 
 		URL classURL = InstallInfo.class.getResource(className);
 		String s = classURL.getFile();
 		File installDir = new File(s.substring(0, s.lastIndexOf("/")));
 
 		sInstallDir = installDir.toString();
 		if (sInstallDir.startsWith("file:"))
			sInstallDir = installDir.toString().substring(6);
 
 		int bangIdx = sInstallDir.indexOf("!");
 		if (bangIdx > 0) {
 			sInstallDir = sInstallDir.substring(0, bangIdx);
 			String sPackage = jarName;
 			sInstallDir = sInstallDir.substring(0, sInstallDir.length()
 					- sPackage.length());
			sInstallDir += "/";
 		} else {
 			sInstallDir += "/";
 			Package pkg = InstallInfo.class.getPackage();
 			if (pkg != null) {
 				String sPackage = pkg.getName();
 				sInstallDir = sInstallDir.substring(0, sInstallDir.length()
 						- sPackage.length());
 			}
 		}
 
 		return sInstallDir;
 	}
 }
