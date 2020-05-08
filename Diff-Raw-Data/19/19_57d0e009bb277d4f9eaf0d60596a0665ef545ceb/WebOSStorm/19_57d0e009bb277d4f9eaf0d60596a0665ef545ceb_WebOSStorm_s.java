 package com.mojojungle.webosstorm;
 
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.SystemInfo;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiDirectory;
 
 import javax.swing.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class WebOSStorm {
 
 	public static final Icon ICON = new ImageIcon(WebOSStorm.class.getResource("icons/webos.png"));
 	public static final Icon ADD_ICON = new ImageIcon(WebOSStorm.class.getResource("icons/new_app.png"));
 	public static final Icon NATURE_ICON = new ImageIcon(WebOSStorm.class.getResource("icons/webos_nature.png"));
 	
 	public static final String PLUGIN_ID = "com.mojojungle.webosstorm";
 
 	public static List<VirtualFile> getWebOSAppsInProject(Project project) {
 		ArrayList<VirtualFile> files = new ArrayList<VirtualFile>();
 		VirtualFile baseDir = project.getBaseDir();
 		if (baseDir != null) {
 			if (WebOSStorm.isWebOSAppDir(baseDir))
 				files.add(baseDir);
 			VirtualFile[] children = baseDir.getChildren();
 			for (VirtualFile child : children) {
 				if (child.isDirectory()) {
 					if (WebOSStorm.isWebOSAppDir(child)) {
 						files.add(child);
 					}
 				}
 			}
 		}
 		return files;
 	}
 
 	public static VirtualFile getAppDirForFile(VirtualFile file) {
 		while (file != null) {
 			if (WebOSStorm.isWebOSAppDir(file)) {
 				return file;
 			}
 			file = file.getParent();
 		}
 		return null;
 	}
 	
 	public static boolean isWebOSAppDir(PsiDirectory dir) {
 		return isWebOSAppDir(dir.getVirtualFile());
 	}
 
 	public static boolean isWebOSAppDir(VirtualFile file) {
 		if(!file.isDirectory())
 			return false;
 		boolean hasAppinfo = file.findChild("appinfo.json") != null;
 //		boolean hasSources = file.findChild("sources.json") != null;
 		boolean parentIsResources = false;
 		VirtualFile parent = file.getParent();
 		if(parent != null)
 			parentIsResources = parent.getName().equals("resources");
 		return hasAppinfo && !parentIsResources;
 	}
 
 	public static String getPalmSDKPath() {
 		if(SystemInfo.isWindows)
 			return System.getenv("PalmSDK");
		else if(SystemInfo.isMac)
 			return "/opt/PalmSDK/Current";
		else if(SystemInfo.isLinux)
			return "/opt/PalmSDK";
 		return "/";
 	}
 
 	public static String getWebOSToolsJarPath() {
 		return getPalmSDKPath()+"/share/jars/webos-tools.jar";
 	}
 }
