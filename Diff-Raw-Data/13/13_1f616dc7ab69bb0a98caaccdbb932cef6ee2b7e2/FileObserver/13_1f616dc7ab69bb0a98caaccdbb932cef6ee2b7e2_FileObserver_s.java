 /**
  * @author: Justin Peterson, Alex Bogart
  * @email: Jmp3833@rit.edu
  * FileObserver.java listens for all actions that are selected in the "File"
  * sub menu of the MenuBar
  */
 
 
 package Observers;
 
import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
import java.io.IOException;
 import java.util.Scanner;
 
 import javax.swing.JFileChooser;
import javax.swing.JFrame;
 import javax.swing.JOptionPane;
import javax.swing.JTextArea;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import Tag.TagCollection;
 import Text_Windows.FileReader;
 import Views.TextTabWindow;
 import Views.TextWindow;
 
 import Command.Receiver ;
 
 public class FileObserver implements Receiver{
 	
 	TextTabWindow windows;
 	FileReader fr;
 	
 	public FileObserver(TextTabWindow windows, FileReader fr){
 		this.windows = windows;
 		this.fr = fr;
 
 	}
 	
   /**
    * Checks if the file has been saved, and sends a prompt to the user 
    * asking if they would like to quit upon saving. 
    */
   public void quit(){
 	  System.exit(0);
   }
 	  
   /**
    * 
    * @param paren: the parent frame that the open box will be based from.
    * @return the text of the selected HTML file.
    */
    
   public void load(){
 	  
 	  JFileChooser fc = new JFileChooser();
 	  FileNameExtensionFilter filter = new FileNameExtensionFilter("HTM & HTML", "htm", "html");
 	  fc.setFileFilter(filter);
 	  //Shows an open prompt for a user to select a desired HTML file
 	  int returnVal = fc.showOpenDialog(null);
 	  
 	  //Grab the selected file 
 	  if(returnVal == JFileChooser.APPROVE_OPTION) {
 		  
 		  
 	      File file = fc.getSelectedFile();
 	     
 	      TagCollection t = new TagCollection();
 	    		  
 	      TextWindow newWindow = new TextWindow(file.getName(),
 	    		  windows,t);
 	      try {
 			Scanner reader = new Scanner(file);
 		    while(reader.hasNextLine()){
 		    	newWindow.append(reader.nextLine());
 		    	newWindow.append("\n");
 		    	
 		    	newWindow.requestFocusInWindow();
 		    	
 		    }
 	      } catch (FileNotFoundException e) {
 			JOptionPane.showMessageDialog(null, "File not found!");
 		}
 	  }
 	  
   }
   
   public void create(){
 	  String fileName = JOptionPane.showInputDialog(null, "Enter Filename: ", 
 				"Enter desired name of your file: ", 1);
 	  TagCollection t = new TagCollection();
 		  TextWindow newWindow = new TextWindow(fileName,
 	    		  windows,t);
   }
   
   /**
    * Write changes in the JTextArea to a file. 
    */
   public void save(){
 	  
 	  JFileChooser fc = new JFileChooser();
 	  FileNameExtensionFilter htmFilter = new FileNameExtensionFilter("HTM & HTML", "htm", "html");
 	  fc.setFileFilter(htmFilter);
 	  //Shows an open prompt for a user to select a desired HTML file
 	  int returnVal = fc.showSaveDialog(null);
 	  //Grab the selected file 
 	  if(returnVal == JFileChooser.APPROVE_OPTION) {
 		  
 		  //File file = fc.getSelectedFile();
 		  String fileName = fc.getSelectedFile().toString();
 		  int l = fileName.length();
 		  //set to save as htm
 		  if (fc.getFileFilter() == htmFilter
 				  //check if it has an extension
 				  && !fileName.substring(l-4, l).equals(".htm") && !fileName.substring(l-5, l).equals(".html")){
 			  fileName += ".htm";
 		  }
 		  String textToSave = fr.getTextFromTabWindow(windows);
 		  
 		  try{
 			  //String fileName = file.getName();
 			  FileWriter out = new FileWriter(fileName);
 	          out.write(textToSave);
 	          out.close();
 		  }catch(Exception e){
 			  //do nothing for now
 		  }
 	  }
 	  
   }
 }
