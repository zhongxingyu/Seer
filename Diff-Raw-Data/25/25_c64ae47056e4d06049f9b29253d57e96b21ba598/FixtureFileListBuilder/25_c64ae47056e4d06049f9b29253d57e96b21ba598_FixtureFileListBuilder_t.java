 package de.cologneintelligence.fitgoodies.file.builder;
 
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 public class FixtureFileListBuilder {
 	private String currentDir;
 	private String seperator;
 	private String baseDir;
 	private List<String> files;
 
 	public FixtureFileListBuilder(String baseDir){
 		this.seperator = File.separator;
 		files = new ArrayList<String>();
 		this.baseDir = new File(baseDir).getPath();
 		currentDir = "\\";
		File fullFilePath = new File(this.baseDir);
		if(!fullFilePath.getPath().endsWith(seperator)){
			fullFilePath = new File(fullFilePath, seperator);
		}
		addIfExists(fullFilePath, "setup.html");
 
 	}
 
 	/**
 	 * Adds the selected files and all corresponding setup and teardown files to a list and then calls {@link #returnFiles()} to return the list.
 	 * @param file	- The filename received from the runner
 	 */
 	public void addFile(String file){
 		if(!file.startsWith(seperator)){
 			file = seperator + file;
 		}
 		String[] baseDirArray = currentDir.split(Pattern.quote(seperator), -1);
 		String parent = new File(file).getParent();
 		String[] pathArray = parent.split(Pattern.quote(seperator));
 		int indexOfPrefix = 0;
 		indexOfPrefix = getCommonIndex(baseDirArray, pathArray, indexOfPrefix);
 
 		for (int i = baseDirArray.length-1; i >= indexOfPrefix ; i--) {
 			File fullFilePath = createDirectoryToFile(baseDirArray, i);
			if(!fullFilePath.getPath().endsWith(seperator)){
				fullFilePath = new File(fullFilePath, seperator);
			}
			addIfExists(fullFilePath, "teardown.html");
 		}
 
 		for(int i = indexOfPrefix; i<pathArray.length; i++){
 			File fullFilePath = createDirectoryToFile(pathArray, i);
			if(!fullFilePath.getPath().endsWith(seperator)){
				fullFilePath = new File(fullFilePath, seperator);
			}
			addIfExists(fullFilePath, "setup.html");
 		}
 
 		File fullFilePath = new File(baseDir);
 		addIfExists(fullFilePath, file);
 		currentDir = parent;
 	}
 
 	/**
 	 * @param fullFilePath	- Path where to add file
 	 * @param fileName		- filename
 	 */
 	private void addIfExists(File fullFilePath, String fileName) {
 		if (new File(fullFilePath, fileName).exists()) {
 			String diff = StringUtils.difference(baseDir, fullFilePath.getPath());
 			files.add(diff + fileName);
 		}
 	}
 
 	/**
 	 * Before returning the list it needs to iterate from current directory to the base directory and add the missing tear down files.
 	 * @return returns a list with all the file names, including the relevant setup and teardown files.
 	 */
 	public List<String> returnFiles(){
 		File currentPath = new File(baseDir, currentDir);
 		while(!currentPath.equals(new File(baseDir))){
 			currentPath = currentPath.getParentFile();
			addIfExists(currentPath, "teardown.html");
 		}
 		return files;
 	}
 
 	/**
 	 * @param baseDirArray	- the path to the base directory, split by File.seperator
 	 * @param pathArray		- the path to the selected file, split by File.seperator
 	 * @param indexOfPrefix	- the position of the suffix of the comparison between base and path directory.
 	 * @return
 	 */
 	private int getCommonIndex(String[] baseDirArray, String[] pathArray,
 			int indexOfPrefix) {
 		for(;indexOfPrefix<Math.min(pathArray.length, baseDirArray.length); indexOfPrefix++){
 			if (!pathArray[indexOfPrefix].equals(baseDirArray[indexOfPrefix])){
 				break;
 			}
 		}
 		return indexOfPrefix;
 	}
 
 	/**
 	 * @param dirArray	- the path to the base directory, split by File.seperator
 	 * @param pathIndex  	- index of current directory iteration
 	 * @return a file containing the full path
 	 */
 	private File createDirectoryToFile(String[] dirArray, int pathIndex) {
 		File f = new File(baseDir);
 		for(int i=0; i<=pathIndex; i++){
 			f = new File(f, dirArray[i]);
 		}
 		return f;
 	}
 }
