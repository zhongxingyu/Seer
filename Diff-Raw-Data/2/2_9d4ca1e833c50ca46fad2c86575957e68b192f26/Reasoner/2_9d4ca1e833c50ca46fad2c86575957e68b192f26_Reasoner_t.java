 package com.xmleditor.util;
 
 import java.io.File;
 import java.util.LinkedList;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import com.xmleditor.io.XMLWriter;
 
 import skillsplanner.beans.Skill;
 import skillsplanner.beans.SkillRequirement;
 import skillsplanner.io.IOHandler;
 import skillsplanner.resources.ClassManager;
 import skillsplanner.resources.SkillManager;
 import skillsplanner.utils.jdom.Handler;
 
 
 public class Reasoner {
 
 	private final static String LIBS_PATH = "../../libs";
 	
 	/**
 	 * Goes through all the skills and looks at the requirements. Checks for
 	 * any misbehaviors, such as skillreq for a skill that requires higher character
 	 * level than the skill
 	 */
 	public static void ReasonThroughSkillReqs(){
 		LinkedList<Skill> changed = new LinkedList<Skill>();
 		for(Skill sk : SkillManager.getInstance().getAllSkills().values()){
 			for(Object obj : sk.getSkillRequirements().toArray()){
 				SkillRequirement sr = (SkillRequirement) obj;
 				Skill s = SkillManager.getInstance().getSkill(sr.getName().trim());
 				if(s == null){
 					System.out.println(sr.getName() + " null");
 					continue;
 				}
 				if(s.getRequiredlevel() > sk.getRequiredlevel()){
 					System.out.println(s.getName() + " reqlvl "+s.getRequiredlevel() +" : " +sk.getName() + " reqlvl " + sk.getRequiredlevel());
 					sk.removeSkillRequirement(s.getName());
 					changed.add(sk);
 				}
 			}
 		}
 		
 		//update the xml files
 		for(Skill skill : changed){
 			System.out.println("Updating "+skill.getUniqueName());
 			XMLWriter.getInstance().skillToXML(skill, skill.getFileTree());
 		}
 		
 		
 	}
 	
 	/**
 	 * This just attempts to load all the xml files without multithreading in order to detect problematic files easier.
 	 */
 	public static void verifyXMLFiles(){
 		File f = new File(IOHandler.getSkillsDir());
 		if(!f.exists()){
 			return;
 		}
 		
 		System.out.println("Rummaging through "+f.getAbsolutePath());
 		loopThroughDirForXMLFiles(f);
 		
 	}
 	
 	/**
 	 * Read the method name prick
 	 */
 	private static void loopThroughDirForXMLFiles(File dir){
 		if(dir.isDirectory()){
 			for(File f : dir.listFiles()){
 				loopThroughDirForXMLFiles(f);
 			}
 		}
 		else{
 			if(dir.getName().endsWith(".xml")){
 				//attempt to load the xml using SAXBuilder
//				System.out.println("Opening file "+dir.getName());
 				Handler.openXMLFile(dir);
 			}
 		}
 	}
 	
 	private static void chooseLibsDir(){
 		//make sure to never consider the library as in a jar
 		IOHandler.JAROVERRIDE = false;
 		File f = new File(LIBS_PATH);
 		if(f.exists()){
 			IOHandler.setClassDir(f.getAbsolutePath().replaceAll("\\\\", "/")+"/classes");
 			IOHandler.setSkillsDir(f.getAbsolutePath().replaceAll("\\\\", "/")+"/skills");
 			
 			SkillManager.getInstance().getSkill("Something that doesn't exist");
 			ClassManager.getInstance().getClassByName("Something that doesn't exist");
 			return;
 		}
 		JFileChooser fc = new JFileChooser(".");
 		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		JOptionPane.showMessageDialog(null, "Please select location of the libs directory");
 		int ret = fc.showOpenDialog(null);
 		
 		if(ret == fc.APPROVE_OPTION){
 			f = fc.getSelectedFile();
 			IOHandler.setClassDir(f.getAbsolutePath().replaceAll("\\\\", "/")+"/classes");
 			IOHandler.setSkillsDir(f.getAbsolutePath().replaceAll("\\\\", "/")+"/skills");
 			
 			//Make sure the threads finish beforel oading
 			SkillManager.getInstance().getSkill("Something that doesn't exist");
 			ClassManager.getInstance().getClassByName("Something that doesn't exist");
 		}
 		
 	}
 	
 	public static void main(String args[]){
 		Reasoner.chooseLibsDir();
 //		Reasoner.ReasonThroughSkillReqs();
 		Reasoner.verifyXMLFiles();
 	}
 
 }
