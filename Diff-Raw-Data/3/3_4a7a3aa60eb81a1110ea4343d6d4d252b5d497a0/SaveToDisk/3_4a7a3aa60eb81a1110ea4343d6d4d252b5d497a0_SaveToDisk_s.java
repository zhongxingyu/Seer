 /*
  * Copyright 2013 Charles Amey
  *
  * This file is part of Trashbiller.
  *
  * Trashbiller is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Trashbiller is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Trashbiller.  If not, see<http://www.gnu.org/licenses/>.
 */
 
 // import javax.swing.AbstractAction;
 // import javax.swing.ImageIcon;
 // import javax.swing.KeyStroke;
 // import java.awt.event.ActionEvent;
 import javax.swing.JFileChooser;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.io.PrintWriter;
 
 import java.util.ArrayList;
 
 public class SaveToDisk
 {
      public SaveToDisk(MainWindow p,ArrayList<ArrayList<String>> d)
      {
           parent = p;
           data = d;
           save();
 //           super(text,icon); //text is the actual name
 //           myTreeClass = t;
 //           putValue(ACTION_COMMAND_KEY,actionCmd);     //action command
 //           putValue(SHORT_DESCRIPTION, toolTip); //used for tooltip text
 //           putValue(MNEMONIC_KEY, mnemonic);
 //           putValue(ACCELERATOR_KEY,accelerator);
      }//end constructor
      
      protected void save()
      {
           System.out.println("show the file picker");
           JFileChooser filePicker = new JFileChooser();
           
 //           filePicker.setFileHidingEnabled(false);
 //           filePicker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
           
           int returnValue = filePicker.showSaveDialog(parent);
           if (returnValue == filePicker.APPROVE_OPTION)
           {
                File f = filePicker.getSelectedFile();
                String split[] = f.getName().split("\\.");
                String newname = "";
                if (split.length == 1)
                {
 //                     System.out.println("no extension, so put one");
                     newname = split[0] + ".csv";
                }
                else if(!split[1].equals("csv") )
                {
 //                     System.out.println("not csv, so put it");
                     newname = split[0] + ".csv";
                }
                else
                     newname = f.getName();
                
                newname = f.getParentFile() + System.getProperty("file.separator") + newname;
                System.out.println("new name: " + newname);
                //make a new file
                
                f = new File(newname);
                try 
                {
                     FileWriter writer = new FileWriter(f);
                     BufferedWriter bufferedWriter = new BufferedWriter(writer);
                     PrintWriter out = new PrintWriter(bufferedWriter);
                     //loop through the tree
                     Csv combine = new Csv();
                     for(int i=0;i<data.size();i++)
                     {
                         String s = combine.combine(data.at(i));
                          out.println(s);
                     }
                     out.close();
                     bufferedWriter.close();
                     writer.close();
                     
                     //out.println("some string");
 //                     traverse();
 //                     out.close();
 //                     bufferedWriter.close();
 //                     writer.close();
 //                     System.out.println("everything is closed");
 //                     System.out.println("already set the title " + theFile);
 //                     myTreeClass.getParentWindow().setTitle("Scratchpad - " + theFile);
                     
                }
                catch (Exception fe)
                {
                }
 
           }
 
 //           if (myTreeClass.getFileSaved() == 0)     //the file is not saved
 //           {
 //                if (myTreeClass.getFileName() == "")
 //                {
 //                     System.out.print("show the file chooser dialog to get a file first");
 //                     JFileChooser filePicker = new JFileChooser();
 //                     int returnValue = filePicker.showSaveDialog(myTreeClass.getParentWindow());
 //                     if (returnValue == filePicker.APPROVE_OPTION)
 //                     {
 //                          System.out.println("they clicked ok");
 //                          File f = filePicker.getSelectedFile();
 //                          System.out.println(f.getName());
 //                          
 //                          //create a new SaveToDisk object
 //                          SaveToDisk saveme = new SaveToDisk(myTreeClass,f);
 //                          saveme.write();
 //                          myTreeClass.setFileName(f.getPath());
 //                          myTreeClass.setFileSaved(1);
 //                          //saveAction.setEnabled(false);
 //                          setEnabled(false);
 //                     }//they clicked ok
 //                }
 //                else
 //                {
 //                     System.out.println(myTreeClass.getFileName());
 //                     //create a new SaveToDisk object
 //                     File f = new File(myTreeClass.getFileName());
 //                     SaveToDisk saveme = new SaveToDisk(myTreeClass,f);
 //                     saveme.write();
 // 
 //                     myTreeClass.setFileSaved(1);
 //                     //saveAction.setEnabled(false);
 //                     setEnabled(false);
 //                }
 //           }//end file was not saved
      }//end actionPerformed
      
 //      private tree myTreeClass;
      private ArrayList<ArrayList<String>> data;
      private MainWindow parent;
 }//end SaveAction
 
