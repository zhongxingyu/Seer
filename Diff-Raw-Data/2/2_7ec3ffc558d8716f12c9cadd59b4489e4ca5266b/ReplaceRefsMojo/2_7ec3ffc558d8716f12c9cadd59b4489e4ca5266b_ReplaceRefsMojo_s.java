 package org.maring.maven.plugins;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.shared.model.fileset.FileSet;
 import org.codehaus.plexus.util.FileUtils;
 
 /**
  * 
  * @goal replaceRefs
  * @phase process-resources
  */
 public class ReplaceRefsMojo extends AbstractMojo
 {
 	
 	private final Log log = this.getLog();
 
 	/**
 	 * 
 	 * @parameter
 	 *       default-value="lib/js/yui2"
 	 */
 	private Object newRelativePath;
 	
 
 	/**
 	 * @parameter
 	 * @required yes
 	 */
 	private Object outputDir;
 	
 	
 	
 	/**
 	 * @parameter
 	 * @required yes
 	 */
 	private FileSet fileSet;
 	
 	
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		
 		File sourceDir = new File(this.fileSet.getDirectory());
 		if (!sourceDir.exists()) {
 			log.info("skipping " + this.fileSet.getDirectory() + " ...");
 			return;
 		}
 		
 		File outputDir = new File((String)this.getOutputDir());
 		if (!outputDir.exists()) {
 			outputDir.mkdir();
 		}
 		
 		if (this.fileSet != null) {
 			this.processFileSet(this.fileSet);
 		} else {
 			log.error("your plugin <configuration> needs a <fileset>");
 		}
 
 	}
 	
 	
 
 
 	@SuppressWarnings("unchecked")
 	private void processFileSet(FileSet fileSet) {
 		File directory = new File(fileSet.getDirectory());
         String includes = toString(fileSet.getIncludes());
         String excludes = toString(fileSet.getExcludes());
         List<File> files;
         try {
 			files = FileUtils.getFiles(directory, includes, excludes);
 		} catch (IOException e) {
 			log.warn("error loading files",e);
 			return;
 		}
 		for ( File file : files ) {
 			this.replaceRef(file);
 		}
 	}
 	
 	
 
 
 	private static String toString(List<String> strings) {
         StringBuilder sb = new StringBuilder();
         for (String string : strings) {
                 if (sb.length() > 0)
                         sb.append(", ");
                 sb.append(string);
         }
         return sb.toString();
 	}
 
 	
 	private void replaceRef(File file) {
 
 		String separator;
 		if (System.getProperty("file.separator").equals("\\")) {
 			separator = "\\\\";
 		} else {
 			separator = "/";
 		}
 		
 		String refererPath = file.getPath().substring(
 				file.getPath().indexOf(this.fileSet.getDirectory())+this.fileSet.getDirectory().length()+2,
 				file.getPath().length());
 		log.debug("resource: " + refererPath);
 	
 		String[] sourceDirElements;
 		if (this.fileSet.getDirectory().contains("/")) {
 			sourceDirElements = this.fileSet.getDirectory().split("/");
 		} else {
 			sourceDirElements = this.fileSet.getDirectory().split("\\");
 		}
 		
 		String projectDir = sourceDirElements[sourceDirElements.length - 1];
 		
 		ArrayList<String> refererPathStack = new ArrayList<String>();
 		
 		String newRelativePathBase = (String) this.getNewRelativePath();
 		String[] newRelativePathBaseElements;
 		if (newRelativePathBase.contains("/")) {
 			newRelativePathBaseElements = newRelativePathBase.split("/");
 		} else {
 			newRelativePathBaseElements = newRelativePathBase.split("\\");
 		}
 		for (int i = 0; i < newRelativePathBaseElements.length; i++) {
 			refererPathStack.add(newRelativePathBaseElements[i]);
 		}
 		refererPathStack.add(projectDir);
 		String[] refererPathElements = refererPath.split(separator);
 		for (int i = 0; i < refererPathElements.length; i++) {
 			refererPathStack.add(refererPathElements[i]);
 		}
 		
 		StringBuffer fileData = new StringBuffer();
 		FileReader fileReader = null;
 		try {
 			fileReader = new FileReader(file);
 		} catch (FileNotFoundException fnfe) {
 			log.warn("file " + file + " was not found");
 			return;
 		}
         BufferedReader reader = new BufferedReader(fileReader);
         boolean fileDataModified = false;
         String strLine;
         try {
 			while ((strLine = reader.readLine()) != null)   {
 				
 				if (strLine.contains("url(")) {
 
 					StringBuffer newLine = new StringBuffer();
 					int indexPos = 0;
 					int curIndexPos = 0;
 					String postPathText = "";
 					
 					while ((indexPos = strLine.indexOf("url(",curIndexPos)) != -1) {
 
 						String prePathText = strLine.substring(curIndexPos,indexPos+4);
 						newLine.append(prePathText);
 						
 						int endPathPos = strLine.indexOf(")",indexPos+4);
 						
 						String oldRef = strLine.substring(indexPos+4,endPathPos);
 						log.debug("     changing url '" + oldRef + "'");
 						
 						if (oldRef.startsWith("\'") || oldRef.startsWith("\"")) {
 							oldRef = oldRef.substring(1,oldRef.length()-1);
 						}
 	
 						
 						String[] oldPathElements = oldRef.split("/");
 						ArrayList<String> oldPathStack = new ArrayList<String>();
 						for (int i = 0; i < oldPathElements.length; i++) {
 							oldPathStack.add(oldPathElements[i]);
 						}
 						int parentCount = 0;
 						for (String oldPathElement: oldPathStack) {
 							if (oldPathElement.equals("..")) {
 								parentCount++;
 							}
 						}
 						
 						ArrayList<String> newPathStack = new ArrayList<String>();
 						if (parentCount > 0) {
							for (int i = 0; i < refererPathStack.size() - parentCount; i++) {
 								newPathStack.add(refererPathStack.get(i));
 							}
 						} else {
 							for (int i = 0; i < refererPathStack.size() - 1; i++) {
 								newPathStack.add(refererPathStack.get(i));
 							}
 						}
 
 						for (int i = parentCount; i < oldPathStack.size(); i++) {
 							newPathStack.add(oldPathStack.get(i));
 						}
 						
 						StringBuffer newRef = new StringBuffer();
 						for (int i = 0; i < newPathStack.size(); i++) {
 							newRef.append(newPathStack.get(i));
 							if (i < newPathStack.size()-1) {
 								newRef.append("/");
 							}
 						}
 						log.debug("               to '" + newRef + "'");
 						
 						
 						
 						newLine.append(newRef);
 						
 						postPathText = strLine.substring(endPathPos, strLine.length());
 						curIndexPos = endPathPos;
 					}
 					newLine.append(postPathText);
 					
 					fileData.append(newLine + "\n");
 					fileDataModified = true;
 				} else {
 					fileData.append(strLine + "\n");
 				}
 			
 			}
 			reader.close();
 			
 			if (fileDataModified) {
 				log.debug("writing modified file ...");
 				try {
 					File modifiedFile = new File(this.getOutputDir() + separator + refererPath);
 					modifiedFile.getParentFile().mkdirs();
 					FileWriter fileWriter = new FileWriter(modifiedFile);
 					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
 					bufferedWriter.write(fileData.toString());
 					bufferedWriter.close();
 					fileWriter.close();
 				} catch (IOException e) {
 					log.warn("error writing file",e);
 				}
 			}
 			
 		} catch (IOException e) {
 			log.warn("error reading file",e);
 			return;
 		}
         	
         
 		
 		
 		
 	}
 
 
 
 	public FileSet getFileset() {
 		return fileSet;
 	}
 
 
 	public void setFileset(FileSet fileSet) {
 		this.fileSet = fileSet;
 	}
 	
 	
 	
 	public Object getNewRelativePath() {
 		return newRelativePath;
 	}
 
 	public void setNewRelativePath(Object newRelativePath) {
 		this.newRelativePath = newRelativePath;
 	}
 
 	public Object getOutputDir() {
 		return outputDir;
 	}
 
 	public void setOutputDir(Object outputDir) {
 		this.outputDir = outputDir;
 	}
 	
 	
 }
