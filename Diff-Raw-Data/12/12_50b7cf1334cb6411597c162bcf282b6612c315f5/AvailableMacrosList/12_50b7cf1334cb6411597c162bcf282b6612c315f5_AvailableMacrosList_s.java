 /*
  * Copyright 2006 Sony Computer Entertainment Inc.
  *
  * Licensed under the SCEA Shared Source License, Version 1.0 (the "License"); you may not use this 
  * file except in compliance with the License. You may obtain a copy of the License at:
  * http://research.scea.com/scea_shared_source_license.html
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License 
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing permissions and limitations under the 
  * License. 
  */
 package refinery;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 /** 
  *  A <code>AvailableMacrosList</code> is a list of available macros, created from
  *  macro files found in the specified directory.
  */
 public class AvailableMacrosList {
 	final String MACRO_EXTENSION = "pip";
 	private Vector<MacroTemplate> availableMacrosList;
 	
 	public AvailableMacrosList(){
 		availableMacrosList = new Vector<MacroTemplate>();
 	}
 	
 	/**
 	 * loadFromDirectory scans the directory given in argument and tries to load the potential macro files
 	 * founds. It also scans the first level of directories and set the category parameter to the name of the directory 
 	 * in which the file was found. This makes an easy way to make categories of macros, the user just have to
 	 * create a directory of the name he wants and put the macro file inside. Macros found in the root
 	 * directory are put in the "No Category" category. 
 	 * @param externalLibs
 	 * @param dir Directory to look for
 	 */
 	public void loadFromDirectory(String dir){
 		
 		File macroDirectory = new File(dir);
 		
 		// If the directory does not exitm, just create it
 		if(!macroDirectory.exists()){
 			macroDirectory.mkdirs();
 			return;
 		}
 		String [] macroList = macroDirectory.list();
 		
 		//Refinery.instance.addDebugMessage("Macro list:");
 		for(int i=0; i<macroList.length; i++){
			File file = new File(dir+"\\"+macroList[i]);
 			if(file.isFile() && file.getName().matches(".*\\."+MACRO_EXTENSION)){
 				try {
 					addFromFile(file.getCanonicalPath(), "No category");
 				} catch (Exception e) {
					Refinery.instance.addDebugMessage("Macro "+dir+"\\"+macroList[i]+ " could not be loaded :\n"+e);
 				}
 			} else if (file.isDirectory()) {	// Scan the first directory level
 				File [] macroList2 = file.listFiles(new MacroFileNameFilter());
 				for (int j=0; j<macroList2.length; j++){
 					try{
 						addFromFile(macroList2[j].getCanonicalPath(), file.getName());
 					} catch (Exception e){
						Refinery.instance.addDebugMessage("Macro "+dir+"\\"+macroList2[j]+ " could not be loaded :\n"+e);
 					}
 				}
 			}
 		}
 		
 		//TODO check there is no circular call between macros
 		
 	}
 	
 	private void addFromFile(String filename, String category) throws ParserConfigurationException, IOException, SAXException{
 		MacroTemplate mb = new MacroTemplate(new File(filename));
 		if (getMacroBaseFromLibName(mb.getIDName()) == null){
 			mb.setCategory(category);
 			add(mb);
 			//Refinery.instance.addDebugMessage(mb.getIDName());
 		} else {
 			Refinery.instance.errorAndWarnings.addWarning("A macro is trying to register with the name \""+ mb.getIDName() + "\" which is already used by another macro. Only one of them can be used. Please fix this problem.");
 		}
 	}
 	
 	public void add(MacroTemplate mb){
 		availableMacrosList.add(mb);
 	}
 	
 	public Iterator<MacroTemplate> iterator(){
 		return availableMacrosList.iterator();
 	}
 	
 	public MacroTemplate getMacroBaseFromLibName(String libname){
 		Iterator<MacroTemplate> mbIt = availableMacrosList.iterator();
 		while (mbIt.hasNext()){
 			MacroTemplate mb = mbIt.next();
 			if (mb.getIDName().equals(libname)){
 				return mb;
 			}
 		}
 		return null;
 	}
 	
 	public void printList(){
 		System.out.println("Macros found :");
 		Iterator<MacroTemplate> mIt = availableMacrosList.iterator();
 		while(mIt.hasNext()){
 			System.out.println(" " + mIt.next().getIDName());
 		}
 	}
 	
 	private class MacroFileNameFilter implements FilenameFilter{
 
 		public boolean accept(File dir, String name) {
 			return name.matches("[a-zA-Z][a-zA-Z0-9\\-]*\\."+MACRO_EXTENSION);
 		}
 		
 	}
 
 }
