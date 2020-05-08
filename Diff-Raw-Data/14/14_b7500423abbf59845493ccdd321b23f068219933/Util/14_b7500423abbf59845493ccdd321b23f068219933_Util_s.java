 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.browser.util;
 
 import java.io.File;
 
 import org.generationcp.browser.exception.GermplasmStudyBrowserException;
 
 import com.vaadin.Application;
 import com.vaadin.ui.Accordion;
 import com.vaadin.ui.TabSheet;
 import com.vaadin.ui.TabSheet.Tab;
 
 public class Util{
 
     public static boolean isTabExist(TabSheet tabSheet, String tabCaption) {
 
         int countTabSheet = tabSheet.getComponentCount();
 
         for (int i = 0; i < countTabSheet; i++) {
             Tab tab = tabSheet.getTab(i);
             if (tab.getCaption().equals(tabCaption)) {
                 return true;
             }
         }
         return false;
     }
 
     public static boolean isAccordionDatasetExist(Accordion accordion, String accordionCaption) {
         int countAccordionTab = accordion.getComponentCount();
 
         for (int i = 0; i < countAccordionTab; i++) {
             Tab tab = accordion.getTab(i);
             if (tab.getCaption().equals(accordionCaption)) {
                 return true;
             }
         }
 
         return false;
     }
 
     public static Tab getTabAlreadyExist(TabSheet tabSheet, String tabCaption) {
 
         for (int i = 0; i < tabSheet.getComponentCount(); i++) {
             Tab tab = tabSheet.getTab(i);
             if (tab.getCaption().equals(tabCaption)) {
                 return tab;
             }
         }
         return null;
 
     }
     
     public static void closeAllTab(TabSheet tabSheet){
     	
     	 for (int i =  tabSheet.getComponentCount()-1; i >=0; i--) {
             tabSheet.removeTab(tabSheet.getTab(i));
           } 
     	
     }
 
 
     /**
      * Validates if an existing path is a directory
      * @param path
      * @return true if the given path is a directory
      */
     public static boolean isDirectory(String path){
         boolean isValid = true;
         File f = new File(path);
         if (!f.exists()) {  // The directory does not exist
             isValid = false;
         } else if (!f.isDirectory()) { // The path is not a directory (it is a file)
             isValid = false;
         }        
         return isValid;
     }
 
 
     /**
      * Gets the desktop directory path or the base directory of the application
      *
      * @param application
      * @return file pointing to desktop or application path
      * 
      */
     public static File getDefaultBrowseDirectory(Application application) throws GermplasmStudyBrowserException{
 
         // Initially gets the Desktop path of the user
         String desktopPath = System.getProperty("user.home") + File.separator + "Desktop";// + File.separator ;
         File file = new File(desktopPath);
         
         // If desktop path is inaccessible, get the applicaton's base directory
         if (!Util.isDirectory(desktopPath) ||
             ((file == null) || (!file.canRead())
                     || (file.getAbsolutePath() == null))){
             file = application.getContext().getBaseDirectory();
         }
         
         if ((file != null) && (file.canRead())
                 && (file.getAbsolutePath() != null)) {
             return file;
         } else {
             throw new GermplasmStudyBrowserException("No valid default directories found");
         }
 
     }
 
     /**
      * Gets the directory based on the given path string
      * 
      * @param path
      * @return file pointing to the path
      * @throws GermplasmStudyBrowserException
      */
     public static File getDefaultBrowseDirectory(String path) throws GermplasmStudyBrowserException{
         File file = new File(path);
         
         if ((file != null) && (Util.isDirectory(path)) && (file.canRead())
                 && (file.getAbsolutePath() != null)) {
             return file;
         } else {
             throw new GermplasmStudyBrowserException("Invalid path");
         }
 
     }
 
     
     /**
      * Gets one directory up the tree
      * 
      * @param path
      * @return
      */
     public static String getOneFolderUp(String path){
        String newPath = path.substring(0, path.lastIndexOf(File.separator));
         
         if (newPath.equals("")) { // already at the root directory
            newPath = File.separator; 
         }
        return newPath;
                 
         
     }
 }
