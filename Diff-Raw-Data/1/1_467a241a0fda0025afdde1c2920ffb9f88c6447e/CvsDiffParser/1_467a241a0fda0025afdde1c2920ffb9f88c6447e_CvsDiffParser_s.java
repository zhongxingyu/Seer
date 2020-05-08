 /*
  * Created on Dec 9, 2003
  * 
  */
 package org.eclipse.releng;
 
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.BuildException;
 
 import java.util.Vector;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 import java.io.FileInputStream;
 import java.util.StringTokenizer;
 import java.util.Enumeration;
 
 /**
  * @author kmoir
  * 
  * To change the template for this generated type comment go to Window -
  * Preferences - Java - Code Generation - Code and Comments
  */
 public class CvsDiffParser extends Task {
 
 	private String mapDiffFile;
 	private String mapOwnerProperties;
 	private Vector updatedMaps;
 	
 	/**
 	 *  
 	 */
 	public CvsDiffParser() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	public static void main(String[] args) {
 
 		CvsDiffParser parser = new CvsDiffParser();
 		parser.setMapDiffFile("d:/junk/cvsDiff.txt");
 		parser.mapOwnerProperties="d:/junk/component.properties";
 		parser.execute();		
 	}
 
 	public void execute() throws BuildException {
 		parseMapDiffFile();
 		notify(getComponentsFromDiff());
 	}
 
 	/**
 	 * @return Returns the mapDiffFile.
 	 */
 	public String getMapDiffFile() {
 		return mapDiffFile;
 	}
 
 	/**
 	 * @param mapDiffFile
 	 *            The mapDiffFile to set.
 	 */
 	public void setMapDiffFile(String mapDiffFile) {
 		this.mapDiffFile = mapDiffFile;
 	}
 
 	/**
 	 * @return Returns the mapOwnerProperties.
 	 */
 	public String getMapOwnerProperties() {
 		return mapOwnerProperties;
 	}
 
 	/**
 	 * @param mapOwnerProperties
 	 *            The mapOwnerProperties to set.
 	 */
 	public void setMapOwnerProperties(String mapOwnerProperties) {
 		this.mapOwnerProperties = mapOwnerProperties;
 	}
 
 	private void parseMapDiffFile() {
 		updatedMaps = new Vector();
 
 		//read the contents of the Diff file, and return contents as a String
 		if (mapDiffFile.length() == 0)
 			updatedMaps=null;
 
 		BufferedReader in = null;
 		String aLine;
 		String contents = "";
 
 		try {
 			in = new BufferedReader(new FileReader(mapDiffFile));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			while ((aLine = in.readLine()) != null) {
 				if (aLine.startsWith("RCS file")) {
 					String mapPath =
 						(aLine
 							.substring(aLine.indexOf(":"), aLine.indexOf(",")))
 							.trim();
 					
 					//verification for actual changes in tags
 					while ((aLine = in.readLine()) != null && !aLine.startsWith("===")){
 						if (aLine.startsWith("< plugin")||aLine.startsWith("< fragment")||aLine.startsWith("< feature")){
 							updatedMaps.add(new File(mapPath).getName());
 							break;
 						}
 					}
 		
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	
 	private Component[] getComponents() {
 
 		Properties componentProperties;
 
 		componentProperties = new Properties();
 		try {
 			componentProperties.load(
 				new FileInputStream(new File(mapOwnerProperties)));
 		} catch (IOException e) {
 		}
 
 		Component[] components = new Component[componentProperties.size()];
 
 		Enumeration propKeys = componentProperties.keys();
 		int i = 0;
 
 		while (propKeys.hasMoreElements()) {
 			String key=propKeys.nextElement().toString();
 			components[i++] =
 				getComponent(
 					key,
 					componentProperties.getProperty(
 						key));
 		}
 
 		return components;
 
 	}
 
 	private Component getComponent(String componentName, String mapList) {
 
 		Component component = new Component();
 		component.setComponentName(componentName);
 
 		// Create a vector of map names from the map list //
 		StringTokenizer str = new StringTokenizer(mapList, ",");
 		while (str.hasMoreTokens()) {
 			component.getMaps().add(str.nextToken());
 		}
 		return component;
 	}
 
 	private Vector getComponentsFromDiff(){
 		Vector componentNames=new Vector();
 		
 		Component [] components = getComponents();
 		
 		if (updatedMaps==null){
 			notify(null);
 		}
 
 		for (int i=0; i<updatedMaps.size(); i++){
 
 			for (int j=0; j<components.length; j++){
 				Component component= components[j];
 				if (component.ownsMap((String)updatedMaps.elementAt(i)))
 					if (!componentNames.contains(component.getComponentName()))
 						componentNames.add(component.getComponentName());
 			}
 		}
 		return componentNames;
 	}
 	
 	private void notify(Vector componentNames){
 		
 		if (componentNames==null || componentNames.size()==0){
 			throw new BuildException("Build cancelled - map files unchanged.");
 		} 
 		
 		Mailer mailer = new Mailer();
 		
 		String subject="updated map file listing";
 		String message ="these map files have been updated for the build:\n\n";
 		
 		for (int i=0; i<updatedMaps.size();i++){
 			message=message.concat(updatedMaps.elementAt(i).toString()+"\n");
 		}
 		
 		message=message.concat("\nThe following teams will need to validate this build:\n\n");	
 		
 		for (int i=0; i<componentNames.size();i++){
 	
 			message=message.concat(componentNames.elementAt(i).toString()+"\n");
 		}
 		
 		try {
 			mailer.sendMessage(subject,message);
 		} catch (NoClassDefFoundError e){
 			System.out.println(message);
 		}		
 	}
 }
