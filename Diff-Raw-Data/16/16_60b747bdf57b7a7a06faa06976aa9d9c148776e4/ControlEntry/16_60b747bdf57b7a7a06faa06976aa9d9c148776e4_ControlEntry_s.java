 /**
  * This file is part of FileControl application (check README).
  * Copyright (C) 2013  Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package filecontrol;
 
 /**
  * Control entry define path for control and 
  * other options;
  * @author Stanislav Nepochatov
  */
 public class ControlEntry extends Generic.CsvElder {
     
     /**
      * Name of entry
      */
     public String EntryName;
     
     /**
      * Path to search location;
      */
     public String Path;
     
     /**
      * File mask to search or single filename;
      */
     public String Mask;
     
     /**
      * Ignore file with this mask in search;
      */
     public String IgnoreMask;
     
     /**
      * Status of entry
      */
     public EntryStatus Status;
     
     /**
      * String representation of entry's status;
      */
     public String StrStatus;
     
     /**
      * <p>Treat condition satisfaction as error. 
      * If <code>ErrorCondition</code> will equal to False 
      * then this control entry will return error while <code>Status</code> 
      * refer to OK (condition not satisfied).<p>
      */
     public Boolean ErrorCondition;
     
     /**
      * Turn on case sensative in scaning;
      */
     public Boolean CaseSensetive;
     
     /**
      * Ignore folder while scan;
      */
     public Boolean IgnoreFolder;
     
     /**
      * Current filename filter;
      */
     private ControlFilter CurrFilter;
     
     /**
      * Total filename hits;
      */
     private Integer CurrHits;
     
     /**
      * Cell render object;
      */
     public java.awt.Color EntryColor = new java.awt.Color(165, 255, 145);
     
     /**
      * Empty constructor;
      */
     ControlEntry() {
         this.baseCount = 7;
         this.currentFormat = Generic.CsvElder.csvFormatType.SimpleCsv;
         this.CurrFilter = new ControlFilter();
     }
     
     /**
      * Default constructor from csv line;
      * @param givenCsv given csv string; 
      */
     ControlEntry(String givenCsv) {
         this();
         java.util.ArrayList<String[]> parsedStruct = Generic.CsvFormat.fromCsv(this, givenCsv);
         String[] baseArray = parsedStruct.get(0);
         this.EntryName = baseArray[0];
         this.Path = baseArray[1];
         this.Mask = baseArray[2];
         this.IgnoreMask = baseArray[3];
         this.CaseSensetive = baseArray[4].equals("1") ? true : false;
         this.ErrorCondition = baseArray[5].equals("1") ? true : false;
         this.IgnoreFolder = baseArray[6].equals("1") ? true : false;
     }
     
     /**
      * Parametrick constructor.
      * @param givenName name of entry;
      * @param givenPath path to scan;
      * @param givenMask filename mask;
      * @param givenIgnoreMask filename mask which will be ignored;
      * @param givenSensetive make search masks case sensative;
      * @param givenErrorCond treat condition satisfation as error;
      */
     ControlEntry(String givenName, String givenPath, String givenMask, String givenIgnoreMask, Boolean givenSensetive, Boolean givenErrorCond, Boolean givenIgnFolder) {
         this();
         this.EntryName = givenName;
         this.Path = givenPath;
         this.Mask = givenMask;
         this.IgnoreMask = givenIgnoreMask;
         this.CaseSensetive = givenSensetive;
         this.ErrorCondition = givenErrorCond;
         this.IgnoreFolder = givenIgnFolder;
     }
     
     /**
      * Set <code>StrStatus</code> proper text message according to control entry status;
      */
     private void renderStrStatus() {
         switch (Status) {
             case OK:
                 if (ErrorCondition) {
                     this.StrStatus = "OK";
                     this.EntryColor = new java.awt.Color(165, 255, 145);
                 } else {
                    this.StrStatus = "Файли не знайдено!";
                     this.EntryColor = new java.awt.Color(255, 69, 69);
                     FileControl.log(2, "[" + this.EntryName + "] >> " + this.StrStatus);
                     FileControl.conditionHandle(false, this);
                 }
                 break;
             case IOEXCEPTION_OCURRED:
                this.StrStatus = "Помилка читання!";
                 this.EntryColor = new java.awt.Color(255, 147, 69);
                 FileControl.log(1, "Ошибка чтения (" + Path + ")!");
                 break;
             case CONDITION_SATISFIED:
                 if (!ErrorCondition) {
                     this.StrStatus = "OK(" + CurrHits + ")";
                     this.EntryColor = new java.awt.Color(165, 255, 145);
                 } else {
                    this.StrStatus = "ЗНАЙДЕНО(" + CurrHits + ")";
                     this.EntryColor = new java.awt.Color(255, 69, 69);
                     FileControl.log(2, "[" + this.EntryName + "] >> " + this.StrStatus);
                     FileControl.conditionHandle(true, this);
                 }
                 break;
         }
     }
 
     @Override
     public String toCsv() {
         return "{" + this.EntryName + "},{" + this.Path + "},{"
                 + this.Mask + "},{" + this.IgnoreMask + "},"
                 + (this.CaseSensetive ? "1" : "0") + ","
                 + (this.ErrorCondition ? "1" : "0") + ","
                 + (this.IgnoreFolder ? "1" : "0");
     }
     
     /**
      * File filter with mask support;
      */
     private class ControlFilter implements java.io.FilenameFilter {
 
         @Override
         public boolean accept(java.io.File dir, String name) {
             if (new java.io.File(dir.getAbsolutePath() + "/" +  name).isDirectory() && IgnoreFolder) {
                 return false;
             }
             if (MaskCalculate(name, Mask)) {
                 if (!IgnoreMask.isEmpty()) {
                     return !MaskCalculate(name, IgnoreMask);
                 } else {
                     return true;
                 }
             } else {
                 return false;
             }
         }
         
         // Mask calculation section
         
         /**
          * Current mask to calculate
          */
         private String curr_mask;
         
         /**
          * Current position of parser cursor;
          */
         private Integer curr_position;
         
         /**
          * Current char in parser;
          */
         private char curr_char;
         
         /**
          * Next char in parser (may be null);
          */
         private char next_char;
         
         /**
          * Main mask parser method;
          * @param name name of file;
          * @param mask mask to check;
          * @return true if filename equal to mask;
          */
         private Boolean MaskCalculate(String name, String mask) {
             if (mask.equals("*")) {
                 return true;
             } else {
                 if (!CaseSensetive) {
                     curr_mask = mask.toUpperCase();
                     name = name.toUpperCase();
                 } else {
                     curr_mask = mask;
                 }
                 curr_position = 0;
                 curr_char = curr_mask.charAt(0);
                 next_char = curr_mask.charAt(1);
                 for (Integer index=0; index < name.length(); index++) {
                     if (index == (name.length() - 1)) {
                         if (curr_char == '*' && curr_position == curr_mask.length() - 1) {
                             return true;
                         } else if (name.charAt(name.length() - 1) == curr_char && curr_position == curr_mask.length() - 1) {
                             return true;
                         } else {
                             return false;
                         }
                     }
                     char curr_nchar = name.charAt(index);
                     char next_nchar = name.charAt(index +1);
                     if (curr_char == '*') {
                         if (next_nchar == next_char) {
                             MoveForward();
                         } else {
                             continue;
                         }
                     } else if (curr_char == '?') {
                         MoveForward();
                     } else if (curr_nchar == curr_char) {
                         MoveForward();
                     } else {
                         return false;
                     }
                 }
                 return true;
             }
         }
         
         /**
          * Move parser stack forward;
          */
         private void MoveForward() {
             curr_char = curr_mask.charAt(++curr_position);
             if (curr_position == curr_mask.length() - 1) {
                 next_char = '\0';
             } else {
                 next_char = curr_mask.charAt(curr_position + 1);
             }
         }
         
     }
     
     /**
      * Check condition;
      */
     public void check() {
         try {
             java.io.File pathFile = new java.io.File(Path);
             String[] FilterResult = pathFile.list(CurrFilter);
             if (FilterResult.length == 0) {
                 this.Status = EntryStatus.OK;
             } else {
                 this.Status = EntryStatus.CONDITION_SATISFIED;
                 this.CurrHits = FilterResult.length;
             }
         } catch (Exception ex) {
             this.Status = EntryStatus.IOEXCEPTION_OCURRED;
             ex.printStackTrace();
         }
         this.renderStrStatus();
     }
 }
