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
 
 package de.mulchprod.kajona.languageeditor.core.filesystem;
 
 import de.mulchprod.kajona.languageeditor.core.Filemanager;
 import de.mulchprod.kajona.languageeditor.core.logger.LELogger;
 import de.mulchprod.kajona.languageeditor.core.textfile.LanguageFileSet;
 import de.mulchprod.kajona.languageeditor.core.textfile.Textentry;
 import de.mulchprod.kajona.languageeditor.core.textfile.Textfile;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 /**
  *
  * @author sidler
  */
 public class Filewriter {
 
     public boolean writeTextfilesToFiles(ArrayList<LanguageFileSet> filesets, String baseFolder, boolean forceWrite) {
 
         for(LanguageFileSet singleFileSet : filesets) {
             LELogger.getInstance().logInfo("Investigating set "+singleFileSet.getModule()+"/"+singleFileSet.getModulePart());
 
             for(Textfile singleFile : singleFileSet.getFileMap().values()) {
 
                 LELogger.getInstance().logInfo("File "+singleFile.getFilename()+" contains "+singleFile.getTextEntries().size()+" entries");
                 LELogger.getInstance().logInfo("Original Path: "+singleFile.getSourcePath());
 
                 if(singleFile.isFileHasChanged() || forceWrite) {
                     this.writeFile(singleFile);
                     singleFile.setFileHasChanged(false);
                 }
                 
                 //if file was saved, reinit again
                 Filereader reader = new Filereader();
                 singleFile = reader.generateTextfileFromFile(new File(singleFile.getSourcePath()));
             }
 
         }
         
         
         return false;
     }
 
     public boolean writeTextfilesToFiles(ArrayList<LanguageFileSet> filesets, String baseFolder) {
         return writeTextfilesToFiles(filesets, baseFolder, false);
     }
 
 
     private boolean writeFile(Textfile file) {
         try {
 
             StringBuffer fileContent = new StringBuffer();
             fileContent.append("<?php\n");
             fileContent.append("/*\"******************************************************************************************************\n");
             fileContent.append("*   (c) 2004-2006 by MulchProductions, www.mulchprod.de                                                 *\n");
             fileContent.append("*   (c) 2007-"+Calendar.getInstance().get(Calendar.YEAR)+" by Kajona, www.kajona.de                                                              *\n");
             fileContent.append("*       Published under the GNU LGPL v2.1, see /system/licence_lgpl.txt                                 *\n");
             fileContent.append("*-------------------------------------------------------------------------------------------------------*\n");
             fileContent.append("*	$Id$					    *\n");
             fileContent.append("********************************************************************************************************/");
 
             fileContent.append("\n//Edited with Kajona Language Editor GUI, see www.kajona.de and www.mulchprod.de for more information\n");
             fileContent.append("//Kajona Language Editor Core Build "+new Filemanager().getBuildVersion()+"\n");
 
             
 
             if(file.getTextEntries().size() > 0) {
                 fileContent.append("\n//editable entries\n");
 
                 for(Textentry entry : file.getTextEntries().values()) {
                     fileContent.append(entry.getEntryAsString(true));
                 }
             }
 
             
             if(file.getNonEditableTextEntries().size() > 0) {
                 fileContent.append("\n//non-editable entries\n");
 
                 for(Textentry entry : file.getNonEditableTextEntries().values()) {
                     fileContent.append(entry.getEntryAsString(true));
                 }
             }
 
            fileContent.append("");
 
             //System.out.println(fileContent.toString());
 
             
             //write back to file
             BufferedWriter filewriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.getSourcePath()), "UTF8"));
             filewriter.write(fileContent.toString());
             filewriter.flush();
             filewriter.close();
             
             return true;
         } catch (FileNotFoundException ex) {
             LELogger.getInstance().logInfo(ex+"");
         } catch (IOException ex) {
             LELogger.getInstance().logInfo(ex+"");
         }
 
         return false;
     }
 
    
     
 }
