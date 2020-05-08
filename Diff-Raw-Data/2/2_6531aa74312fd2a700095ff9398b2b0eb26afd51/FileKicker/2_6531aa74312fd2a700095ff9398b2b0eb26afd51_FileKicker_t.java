 package com.captureplay.maven.plugin.buildprofile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 
 public class FileKicker {
 
 	public static void configureForEnv(String env, String outDir) throws MojoExecutionException {
 		String[] extensions = null;
 		Collection<File> classpathFiles = FileUtils.listFiles(new File(outDir), extensions, false);
 //		getLog().info("Location: " + outDir);
 		for (File file : classpathFiles) {
 			if (!file.isDirectory()) {
 				if (file.getName().contains(env)) {
 //					getLog().info(file.getName() + " contains " + env);
 //					getLog().info(file.getAbsolutePath());
 					String[] nameExtension = file.getName().split("\\.");
 					String[] newNameArray = nameExtension[0].split("-");
 					String newName = newNameArray[0] + "." + nameExtension[1];
 //					getLog().info("original: " + file.getName() + ", new: " + newName);
					File newFile = new File(outDir+ "/" + newName);
 					try {
 						FileUtils.deleteQuietly(newFile);
 						FileUtils.moveFile(file, newFile);
 					} catch (IOException e) {
 						throw new MojoExecutionException("failed to move file", e);
 					}
 				}
 			}
 		}
 	}
 }
