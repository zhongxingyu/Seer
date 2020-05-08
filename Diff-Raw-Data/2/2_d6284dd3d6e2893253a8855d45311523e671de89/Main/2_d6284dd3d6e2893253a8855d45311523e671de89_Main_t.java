 /*
  * Kajona Language File Editor Core
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2, or (at your option) any
  * later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
  *
  * (c) MulchProductions, www.mulchprod.de, www.kajona.de
  *
  */
 
 package de.mulchprod.kajona.languageeditor.core;
 
 import de.mulchprod.kajona.languageeditor.core.config.Configuration.ConfigNotSetException;
 import de.mulchprod.kajona.languageeditor.core.filesystem.Filesystem.FolderNotExistingException;
 import de.mulchprod.kajona.languageeditor.core.logger.LELogger;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 
 /**
  *
  * @author sidler
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
        
         Filemanager manager = new Filemanager();
 
         System.out.println("Kajona Language Editor Core, Build "+manager.getBuildVersion());
         System.out.println("Modified version for Kajona V4 projects.\n");
         System.out.println("(c) Stefan Idler, sidler@mulchprod.de\n");
 
         if(args.length < 3) {
             System.out.println("No argument specified. Usage:");
 
            System.out.println("--formatLangfiles --projectFolder <path>");
 
 
         }
 
         else {
             //loop args and build infos
             String command = args[0];
             String folder = "";
 
             if(args[1].equals("--projectFolder"))
                 folder = args[2];
 
 
             if(command.equals("--formatLangfiles"))
                 formatLangFiles(folder);
 
             if(command.equals("--printLangfiles"))
                 printLangFiles(folder);
 
         }
         
     }
 
 
     private static void printLangFiles(String sourcePath) {
         try {
             if (sourcePath.equals("")) {
                     System.out.println("sourcepath missing");
                     return;
                 }
                 LELogger.getInstance().setLogToConsole(true);
             
                 System.out.println("Starting filemanager...");
                 Filemanager filemanager = new Filemanager();
                 System.out.println("Setting sourcepath...");
                 filemanager.setKajonaProjectPath(sourcePath);
                 System.out.println("Reading project files...");
                 filemanager.readProjectFiles();
                 
                 filemanager.printFiles();
                 
         } catch (ConfigNotSetException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         } catch (FolderNotExistingException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
             
     }
     
     
 
     private static void formatLangFiles(String sourcePath) {
         try {
             System.out.println("Formatting lang files, project-path: " + sourcePath);
             if (sourcePath.equals("")) {
                 System.out.println("sourcepath missing");
                 return;
             }
             System.out.println("Starting filemanager...");
             Filemanager filemanager = new Filemanager();
             System.out.println("Setting sourcepath...");
             filemanager.setKajonaProjectPath(sourcePath);
             System.out.println("Reading project files...");
             filemanager.readProjectFiles();
 
             System.out.println("Writing project files...");
             filemanager.writeProjectFiles(true);
 
 
         } catch (ConfigNotSetException ex) {
             System.out.println("Configuration not set: "+ex.getMessage());
         } catch (FolderNotExistingException ex) {
             System.out.println("Error reading project files: "+ex.getMessage());
         }
 
     }
 
 }
