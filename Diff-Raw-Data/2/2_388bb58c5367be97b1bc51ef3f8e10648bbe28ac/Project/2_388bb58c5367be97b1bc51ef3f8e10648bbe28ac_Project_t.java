 package codemate.project;
 
 /**
  * Project
  *
  * This class is used to manage a project.
  *
  * @author      Li Dong <dongli@lasg.iap.ac.cn>
  */
 
 import java.io.*;
 import java.util.*;
 
 import codemate.Fortran.*;
 import codemate.ui.*;
 import codemate.utils.*;
 
 public class Project {
 	private String name;
 	private String buildScheme = "debug";
 	private File root;
 	private List<File> directories = new ArrayList<File>();
 	public List<CodeEntity> entities = new ArrayList<CodeEntity>();
     public List<String> addedMacros = new LinkedList<String>();
 
 	public Project(String dirName) {
 		setRoot(new File(dirName));
		setName(getRoot().getName());
 		collectCodeEntities(root);
 	}
 	
 	Project(String projectName, String dirName) {
 		setName(projectName);
 		setRoot(new File(dirName));
 		collectCodeEntities(root);
 	}
 
 	public void collectCodeEntities(File root) {
 		List<File> dirs = SystemUtils.getSubdirectories(root);
 		dirs.add(root);
 		for (File dir : dirs) {
 			boolean isFound = false;
 			for (String fileName : dir.list()) {
 				for (String suffix : FortranTraits.suffix)
 					if (fileName.endsWith(suffix)) {
 						isFound = true;
 						entities.add(new CodeEntity(dir+"/"+fileName));
 					}
 			}
 			// only add the directory with codes in it
 			if (isFound)
 				directories.add(dir);
 		}
 	}
 	
 	public CodeEntity searchCodeEntity(String name) {
 		for (CodeEntity entity : entities)
 			if (entity.getName().equals(name))
 				return entity;
 		return null;
 	}
 
 	public File getRoot() {
 		return root;
 	}
 
 	public void setRoot(File root) {
 		if (root.getAbsolutePath().equals(System.getenv("HOME")))
 			UI.error("codemate", "The project shouldn't locate at HOME!");
 		this.root = root.getAbsoluteFile();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		UI.notice("codemate", "Create project "+name+".");
 		this.name = name;
 	}
 
 	public String getBuildScheme() {
 		return buildScheme;
 	}
 
 	public void setBuildScheme(String buildScheme) {
 		this.buildScheme = buildScheme;
 	}
 	
 	public List<File> getDirectories() {
 		return directories;
 	}
 	
 	/**
 	 * findProject
 	 * 
 	 * This method finds the possible existing project that covers the start
 	 * point. That is find the '.codemate' directory upward, but the HOME is
 	 * excluded.
 	 * 
 	 * @param  startPoint      The start point (can be a file or a directory)
 	 * @return File            The project root directory
 	 */
 	public static File findProject(File startPoint) {
 		File parent = startPoint;
 		while (parent != null) {
 			if (parent.isDirectory() &&
 					Arrays.asList(parent.list()).contains(".codemate")) {
 				if (parent.getAbsolutePath().equals(System.getenv("HOME")))
 					parent = null;
 				break;
 			}
 			parent = parent.getParentFile();
 		}
 		if (parent != null)
 			UI.notice("codemate",
 					"Find covered project "+parent.getAbsolutePath()+".");
 		return parent;
 	}
 }
