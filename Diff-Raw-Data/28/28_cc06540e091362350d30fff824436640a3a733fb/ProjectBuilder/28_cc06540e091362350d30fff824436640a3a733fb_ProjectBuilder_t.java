 package com.mutation.report.generator;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mutation.report.source.om.Project;
 import com.mutation.report.source.om.SourceFile;
 
 public class ProjectBuilder {
 
 	public Project buildProject(List<String> sourceFolders) {
 
 		Project project = new Project();
 
 		for (String folderName : sourceFolders) {
 
 			try {
 				List<String> sourceFileNames = getSourceFiles(folderName);
 
 				for (String sourceFileName : sourceFileNames) {
 					try {
 						SourceFile sourceFile = new SourceFile();
 						sourceFile.setFileName(sourceFileName);
 						sourceFile.setPackageName(extractPackageName(folderName, sourceFileName));
 						sourceFile.setClassName(extractClassName(sourceFileName));
 						sourceFile.setSourceText(extractFileContent(sourceFileName));
 						project.addSourceFile(sourceFile);
 					} catch (IOException e) {
 						System.err.println("Source folder " + folderName + " is invalid. Skipping ...");
 						continue;
 					}
 
 				}
 			} catch (IOException e) {
 				System.err.println("Source folder " + folderName + " is invalid. Skipping ...");
 				continue;
 			}
 
 		}
 
 		return project;
 
 	}
 
 	private String extractPackageName(String folderName, String sourceFileName) {
 		String nameWithoutSourceFolder = sourceFileName.substring(folderName.length() );
 
 		String packageName = "";
 
		int lastSlashIndex = nameWithoutSourceFolder.lastIndexOf(System.getProperty("file.separator"));
 
 		if (lastSlashIndex > 0) {
 			packageName = nameWithoutSourceFolder.substring(0, lastSlashIndex);
 		}
 
		String javaPackageName = packageName.replaceAll(System.getProperty("file.separator"), ".");
 		
 		if ( javaPackageName.startsWith(".")) {
 			javaPackageName = javaPackageName.substring(1);
 		}
 		
 		return javaPackageName;
 	}
 
 	private String extractClassName(String sourceFileNameWithPath) {
 
 		String fileNameWithoutPath;
 
		int lastSlashIndex = sourceFileNameWithPath.lastIndexOf(System.getProperty("file.separator"));
 
 		if (lastSlashIndex > 0) {
 
 			fileNameWithoutPath = sourceFileNameWithPath.substring(lastSlashIndex + 1, sourceFileNameWithPath.length());
 
 		} else
 			fileNameWithoutPath = sourceFileNameWithPath;
 
 		// Strip ".java" before returning result
 		return fileNameWithoutPath.substring(0, fileNameWithoutPath.length() - 5);
 	}
 
 	private List<String> extractFileContent(String sourceFileName) throws IOException {
 
 		List<String> sourceText = new ArrayList<String>();
 
 		BufferedReader reader = new BufferedReader(new FileReader(new File(sourceFileName)));
 
 		String lineRead = null;
 		while ((lineRead = reader.readLine()) != null) {
 			sourceText.add(lineRead + "\n");
 		}
 
 		return sourceText;
 
 	}
 
 	private List<String> getSourceFiles(String folderName) throws IOException {
 
 		// TODO: verify whether symbolic links can lead to infinite loops
 
 		List<String> result = new ArrayList<String>();
 
 		File folder = new File(folderName);
 
 		File[] javaFiles = folder.listFiles(new FilenameFilter() {
 
 			public boolean accept(File dir, String name) {
 				return name.endsWith(".java");
 			}
 
 		});
 
 		if (javaFiles != null) {
 			for (int fileCount = 0; fileCount < javaFiles.length; fileCount++) {
 
 				result.add(javaFiles[fileCount].getPath());
 			}
 		}
 
 		File[] directories = folder.listFiles(new FilenameFilter() {
 
 			public boolean accept(File dir, String name) {
 				return dir.isDirectory();
 			}
 
 		});
 
 		if (directories != null) {
 			for (int dirCount = 0; dirCount < directories.length; dirCount++) {
 
 				result.addAll(getSourceFiles(directories[dirCount].getPath()));
 			}
 		}
 
 		return result;
 
 	}
 }
