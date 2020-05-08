 /*
  * Copyright 2011 - 2013 Herb Bowie
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.powersurgepub.pspub;
 
   import com.powersurgepub.psdatalib.script.*;
   import com.powersurgepub.psdatalib.textmerge.*;
   import com.powersurgepub.psdatalib.ui.*;
   import com.powersurgepub.pstextmerge.*;
   import com.powersurgepub.psutils.*;
   import com.powersurgepub.xos2.*;
   import java.io.*;
   import java.net.*;
   import java.util.*;
   import javax.swing.*;
 
 /**
  A window that can be used to publish some source data in a different form,
  typically for browsing on the Web.
  <p>
  Initial use is for iWisdom. Also used for URL Union.
  <p>
  This class stores data about publications in multiple properties files named
  "pspub_parms.xml". One file is stored in the source folder for the data
  collection, and contains a list of the publish to locations associated with
  that data collection. Another file is tucked away in each publish to location,
  with each of these files containing the various properties specified by the
  user for that particular publication. 
  <p>
  @author Herb Bowie.
  */
 public class PublishWindow
     extends javax.swing.JFrame
       implements 
         ScriptExecutor,
         WindowToManage {
 
   public final static String PUBLISH_TO         = "publish-to";
   public final static String PUBLISH_URL        = "publish-url";
   public final static String PUBLISH_SCRIPT     = "publish-script";
   public final static String PUBLISH_WHEN       = "publish-when";
   public final static String   PUBLISH_ON_SAVE    = "On Save";
   public final static String   PUBLISH_ON_CLOSE   = "On Close";
   public final static String   PUBLISH_ON_DEMAND  = "On Demand";
   public final static String PUBLISH_TEMPLATES  = "publish-templates";
   public final static String PUBLISH_TEMPLATE   = "publish-template";
 
   public final static String INDEX_FILE_NAME
       = "index.html";
   public final static String PROPERTIES_OLD_FILE_NAME
       = "pspub_parms.xml";
   public final static String PROPERTIES_SOURCE_FILE_NAME
       = "pspub_source_parms.xml";
   public final static String PROPERTIES_PUBLICATION_FILE_NAME
       = "pspub_publication_parms.xml";
 
   public final static Object[] publishToOptions = {
                       "Add New",
                       "Replace Existing",
                       "Cancel"};
   public final static Object[] publishRemoveOptions = {
                       "Remove Existing",
                       "Cancel"};
   public final static int     UNDEFINED          = -1;
   public final static int     ADD                = 0;
   public final static int     REPLACE            = 1;
   public final static int     CANCEL             = 2;
   public final static int     REMOVE             = 3;
 
   public final static int     SAVE               = 1;
   public final static int     CLOSE              = 2;
   public final static int     DEMAND             = 3;
   
   private             int     option             = UNDEFINED;
 
   private             PublishAssistant assistant;
 
   private             File    defaultTemplatesFolder;
   private             String  defaultTemplatesFolderText;
 
   private             File             source      = null;
   private             ArrayList        pubs = new ArrayList();
   private             int              currentSelectionIndex = -1;
 
   private             int              inputSource = SYSTEM_INPUT;
   public final static int                SYSTEM_INPUT = 0;
   public final static int                USER_INPUT = 1; 
 
   private             File             publishTo = null;
   
   private             StatusBar        statusBar = null;
   
   private             Home             home = Home.getShared();
 
   /** 
    Creates new form PublishWindow. Sets up the window,
    but doesn't load any data.
    */
   public PublishWindow(PublishAssistant assistant) {
 
     this.assistant = assistant;
     initComponents();
     this.setTitle (Home.getShared().getProgramName() + " Publish");
     defaultTemplatesFolder
             = new File (Home.getShared().getAppFolder(), "templates");
     try {
       defaultTemplatesFolderText = defaultTemplatesFolder.getCanonicalPath();
     }  catch (java.io.IOException ioex) {
       defaultTemplatesFolderText = "";
     }
   }
 
   /**
    Indicate whether publish on save is a valid option.
 
    @param onSaveOption If true, then user will have the option to publish
                        on saveSource; if false, then they will not see that option.
    */
   public void setOnSaveOption (boolean onSaveOption) {
     String publishWhen = (String)publishWhenComboBox.getSelectedItem();
     publishWhenComboBox.removeAllItems();
     publishWhenComboBox.addItem(PUBLISH_ON_DEMAND);
     publishWhenComboBox.addItem(PUBLISH_ON_CLOSE);
     if (onSaveOption) {
       publishWhenComboBox.addItem(PUBLISH_ON_SAVE);
     }
     setPublishWhen (publishWhen);
   }
 
   /**
    Indicate that a new data source is being opened, and pass the folder
    containing the source data. This method will try to obtain a list of
    publications from this folder.
 
    @param source The folder containing the source data.
    */
   public void openSource(File source) {
     this.source = source;
 
     inputSource = SYSTEM_INPUT;
 
     // Load the list of all known publications for this data source
     pubs = new ArrayList();
     currentSelectionIndex = -1;
     publishToComboBox.setSelectedItem("");
     initPublicationProperties();
     String publishToText;
     if (source != null
         && source.exists()
         && source.canRead()) {
       Properties sourceProps = new Properties();
       File oldFile = new File (source, PROPERTIES_OLD_FILE_NAME);
       File inFile = new File (source, PROPERTIES_SOURCE_FILE_NAME);
       if ((inFile == null
           || (! inFile.exists()))
           && (oldFile != null
             && oldFile.exists())) {
         inFile = oldFile;
       }
       if (inFile != null
           && inFile.exists()
           && inFile.canRead()) {
         try {
           FileInputStream in = new FileInputStream (inFile);
           sourceProps.loadFromXML(in);
           int i = 0;
           publishToText = sourceProps.getProperty(
               PUBLISH_TO + "-" + String.valueOf(i));
           while (publishToText != null) {
             addPublication(publishToText);
             i++;
             publishToText = sourceProps.getProperty(
               PUBLISH_TO + "-" + String.valueOf(i));
           }
         } catch (java.io.FileNotFoundException exfnf) {
           System.out.println ("File not found");
         } catch (java.io.IOException exio) {
           System.out.println ("I/O Exception");
         }
       } // end if inFile is available
     } // end if source folder is available
     
     // Populate the publish to combo box
     populatePublishToComboBox();
 
     // Load the data for the first publish to location
     if (pubs.size() > 0) {
       setPublishTo(0);
     }
 
     inputSource = USER_INPUT;
   }
 
   /**
    Indicate that the current data source is being saved. 
    */
   public void saveSource() {
     
     // If user has requested that we publish on save, then publish now.
     checkForPublication(SAVE);
     
     // Save the list of publications for this data source
     savePubsForSource();
   }
 
   /**
    Indicate that the current data source is being closed. This method will try
    to save the current list of publications for this data source, within the
    source folder passed at open. 
    */
   public void closeSource() {
     
     // If user has requested that we publish on close, then publish now
     checkForPublication(CLOSE);
 
     // Save the list of publications for this data source
     savePubsForSource();
   }
   
   /**
    Save the list of publications for this data source.
    */
   private void savePubsForSource() {
     
     if (source != null
         && source.exists()
         && source.canWrite()
         && pubs.size() > 0) {
       Properties sourceProps = new Properties();
       for (int i = 0; i < pubs.size(); i++) {
         sourceProps.setProperty(
             PUBLISH_TO + "-" + String.valueOf(i),
             (String)pubs.get(i));
       }
       File outFile = new File (source, PROPERTIES_SOURCE_FILE_NAME);
       try {
         FileOutputStream out = new FileOutputStream (outFile);
         sourceProps.storeToXML(out,
             "com.powersurgepub.pspub.PublishWindow properties");
         File oldFile = new File (source, PROPERTIES_OLD_FILE_NAME);
         if (oldFile != null
             && oldFile.exists()) {
           oldFile.delete();
         }
       } catch (java.io.FileNotFoundException exfnf) {
         System.out.println ("File not found");
       } catch (java.io.IOException exio) {
         System.out.println ("I/O Exception");
       }
     }
   }
 
   /**
    Check each publication for this data source to see if this is an
    appropriate occasion for publication.
 
    @param occasion 1 = Save,
                    2 = Close,
                    3 = Demand.
    */
   private void checkForPublication(int occasion) {
 
     savePublicationProperties(currentSelectionIndex);
     int saveIndex = currentSelectionIndex;
     for (int i = 0; i < pubs.size(); i++) {
       setPublishTo(i);
       switch (occasion) {
         case SAVE:
           if (getPublishWhen().equalsIgnoreCase(PUBLISH_ON_SAVE)) {
             publish();
           }
           break;
         case CLOSE:
           if (getPublishWhen().equalsIgnoreCase(PUBLISH_ON_CLOSE)) {
             publish();
           }
           break;
         case DEMAND:
           if (i == saveIndex) {
             publish();
           }
           break;
         default:
           break;
       }
     }
     setPublishTo(saveIndex);
   }
   
   private void switchPublishTo(int fromIndex, int toIndex) {
     savePublicationProperties(fromIndex);
     setPublishTo(toIndex);
   }
 
   /**
    Set the publish to location, using the given index to indicate which
    of the known locations is to become the current one.
 
    @param selectedIndex An index to the pubs list.
    */
   private void setPublishTo(int toIndex) {
     if (toIndex >= 0
         && toIndex < pubs.size()) {
       if (toIndex != publishToComboBox.getSelectedIndex()) {
         publishToComboBox.setSelectedIndex(toIndex);
       }
       String publishToText = (String)pubs.get(toIndex);
       loadPublication(publishToText);
     }
     currentSelectionIndex = toIndex;
   }
 
   /**
    Load the properties for the given publishTo location.
 
    @param publishTo The folder to which the data is to be published.
    */
   private void loadPublication(String publishToText) {
 
     initPublicationProperties();
     if (publishToText != null
         && publishToText.length() > 0) {
       // setPublishTo (publishTo);
       File publishToFolder = new File(publishToText);
       if (publishToFolder.exists()
           && publishToFolder.canRead()) {
         Properties publishProps = new Properties();
         File inFile = new File 
             (publishToFolder, PROPERTIES_PUBLICATION_FILE_NAME);
         File oldFile = new File (publishToFolder, PROPERTIES_OLD_FILE_NAME);
         if ((inFile == null
               || (! inFile.exists()))
             && oldFile != null
             && oldFile.exists()) {
           inFile = oldFile;
         }
         try {
           FileInputStream in = new FileInputStream (inFile);
           publishProps.loadFromXML(in);
           setEquivalentURL(publishProps.getProperty(PUBLISH_URL, ""));
           setPublishScript(publishProps.getProperty(PUBLISH_SCRIPT, ""));
           setPublishWhen(publishProps.getProperty(PUBLISH_WHEN, ""));         
           setTemplatesFolder(publishProps.getProperty(PUBLISH_TEMPLATES,
               defaultTemplatesFolderText));
           setTemplate(publishProps.getProperty(PUBLISH_TEMPLATE, ""));
         } catch (java.io.FileNotFoundException exfnf) {
           // let's hope this doesn't happen
         } catch (java.io.IOException exio) {
           // and that this doesn't happen
         }
       } // end if publish to folder is ready to be used
     } // end if publish to folder has a value
 
   }
 
   /**
    Add a publication to the internal list of all publications for this data
    source. Ensure that no duplicates are added.
 
    @param publishToText The folder path to which the data source will be published.
 
    @return The index at which the publish to location was added, or was found
            to already exist.
    */
   private int addPublication (String publishToText) {
     int i = -1;
     if (publishToText != null
         && publishToText.length() > 0) {
       File publishToFolder = new File (publishToText);
       if (publishToFolder.exists()) {
         boolean found = false;
         i = 0;
         while (i < pubs.size() && (! found)) {
           found = publishToText.equals((String)pubs.get(i));
           if (! found) {
             i++;
           }
         }
         if (! found) {
           pubs.add(publishToText);
         }
       } // end if publish to folder exists
     } // end if publish to string isn't empty
     return i;
   }
 
   /**
    Populate the publish to Combo Box from the contents of the publications list.
    */
   private void populatePublishToComboBox() {
     publishToComboBox.removeAllItems();
     for (int i = 0; i < pubs.size(); i++) {
       publishToComboBox.addItem(pubs.get(i));
     }
   }
 
   /**
    Save the publication properties specified by the user. 
    */
   private void savePublicationProperties(int pubIndex) {
 
     if (pubIndex >= 0 && pubIndex < pubs.size()) {
       String publishToText = (String)publishToComboBox.getItemAt(pubIndex);
       if (publishToText != null
           && publishToText.length() > 0) {
         File publishToFolder = new File(publishToText);
         if (publishToFolder.exists()
             && publishToFolder.canWrite()) {
           Properties publishProps = new Properties();
           publishProps.setProperty(PUBLISH_TO,        getPublishToText());
           publishProps.setProperty(PUBLISH_URL,       getEquivalentURLText());
           publishProps.setProperty(PUBLISH_SCRIPT,    getPublishScriptText());
           publishProps.setProperty(PUBLISH_WHEN,      getPublishWhen());
           publishProps.setProperty(PUBLISH_TEMPLATES, getTemplatesFolderText());
           publishProps.setProperty(PUBLISH_TEMPLATE,   getTemplate());
           File outFile = new File 
               (publishToFolder, PROPERTIES_PUBLICATION_FILE_NAME);
           try {
             FileOutputStream out = new FileOutputStream (outFile);
             publishProps.storeToXML(out,
                 "com.powersurgepub.pspub.PublishWindow properties");
             File oldFile = new File (publishToFolder, PROPERTIES_OLD_FILE_NAME);
             if (oldFile != null
                 && oldFile.exists()) {
               oldFile.delete();
             }
           } catch (java.io.FileNotFoundException exfnf) {
             // let's hope this doesn't happen
           } catch (java.io.IOException exio) {
             // and that this doesn't happen
           }
         } // end if publish to folder is ready to be used
       } // end if publish to folder has a value
     } // end if we have a value publication index
   }
 
   /**
    Initialize the window's fields to reflect null or other default values.
    */
   private void initPublicationProperties() {
     setEquivalentURL("");
     setTemplatesFolder(defaultTemplatesFolderText);
     if (templateSelectComboBox.getItemCount() > 0) {
       templateSelectComboBox.setSelectedIndex(0);
     }
     setPublishScript("");
     setPublishWhen(PUBLISH_ON_DEMAND);
     viewWhereComboBox.setSelectedIndex(0);
   }
   
   // Getters and Setters for publish to location
 
   private File getPublishTo() {
     return new File(getPublishToText());
   }
 
   private String getPublishToText() {
     return (String)publishToComboBox.getSelectedItem();
   }
 
   // Getters and Setters for equivalent URL
 
   private void setEquivalentURL(URL equivalentURL) {
     equivalentURLText.setText(equivalentURL.toString());
   }
 
   private void setEquivalentURL(String equivalentURLText) {
     this.equivalentURLText.setText(equivalentURLText);
   }
 
   private String getEquivalentURLText() {
     return equivalentURLText.getText();
   }
 
   private URL getEquivalentURL() {
     if (equivalentURLText.getText().length() == 0) {
       return null;
     } else {
       try { 
         return new URL(equivalentURLText.getText());
       } catch (MalformedURLException e) {
         return null;
       }
     }
   }
 
   // Getters and setters for Templates folder
 
   private void setTemplatesFolder(File templatesFolder) {
     try {
       setTemplatesFolder(templatesFolder.getCanonicalPath());
     } catch (java.io.IOException e) {
       // ignore errors
     }
   }
 
   private void setTemplatesFolder(String templatesFolderText) {
     templatesText.setText(templatesFolderText);
     populateTemplates();
   }
 
   /**
    Populate the list of available template collections, based on the location
    of the templates folder.
    */
   private void populateTemplates () {
     File templatesFolder = new File (templatesText.getText());
     if (templatesFolder != null
         && templatesFolder.exists()
         && templatesFolder.isDirectory()
         && templatesFolder.canRead()) {
       File[] templates = templatesFolder.listFiles();
       if (templates != null) {
         // Bubble sort the array to get it into alphabetical order, ignoring case
         boolean swapped = true;
         while (swapped) {
           swapped = false;
           int i = 0;
           int j = 1;
           while (j < templates.length) {
             String lower = templates[i].getName();
             String higher = templates[j].getName();
             if (lower.compareToIgnoreCase(higher) > 0) {
               File hold = templates[i];
               templates[i] = templates[j];
               templates[j] = hold;
               swapped = true;
             } // end if we need to swap the two entries
             i++;
             j++;
           } // end one pass through the array of templates
         } // end while still swapping entries
 
         // Now load templates into drop-down menu
         templateSelectComboBox.removeAllItems();
         int i = 0;
         while (i < templates.length) {
           if (templates[i].getName().length() > 0
               && templates[i].getName().charAt(0) != '.'
               && templates[i].exists()
               && templates[i].canRead()
               // && templates[i].isFile()
               ) {
             templateSelectComboBox.addItem (templates[i].getName());
           } // end if folder entry looks like a usable template
           i++;
         } // end while loading combo box with templates
       } // end if the templates folder had any contentes
     } // end if we found a valid templates folder
   }
 
   private String getTemplatesFolderText() {
     return templatesText.getText();
   }
 
   private File getTemplatesFolder() {
     return new File(templatesText.getText());
   }
 
   // Getters and setters for template
 
   private void setTemplate(String template) {
     selectTemplate(template);
   }
 
   private void selectTemplate (String selectedTemplate) {
     int i = 0;
     boolean found = false;
     while ((! found) && (i < templateSelectComboBox.getItemCount())) {
       if (selectedTemplate.equalsIgnoreCase
           ((String)templateSelectComboBox.getItemAt(i))) {
         templateSelectComboBox.setSelectedIndex(i);
         found = true;
       } else {
         i++;
       }
     } // end while looking for match
   } // end method setPublishWhen
 
   private String getTemplate() {
     return (String)templateSelectComboBox.getSelectedItem();
   }
 
   // Getters and setters for publish script file
 
   private void setPublishScript(String publishScriptText) {
     this.publishScriptText.setText(publishScriptText);
   }
 
   private void setPublishScript(File publishScript) {
     try {
       publishScriptText.setText(publishScript.getCanonicalPath());
     } catch (java.io.IOException e) {
       // ignore errors
     }
   }
 
   private String getPublishScriptText() {
     return publishScriptText.getText();
   }
 
   private File getPublishScript() {
     return new File(publishScriptText.getText());
   }
 
   /**
    Apply the selected template collection, copying the contents of its
    folder to the publish to location.
    */
   private void applySelectedTemplate() {
     String selectedTemplateFolderName = getTemplate();
     if (selectedTemplateFolderName == null) {
       Trouble.getShared().report(
         this,
         "No template has been selected",
         "Template Error",
         JOptionPane.ERROR_MESSAGE);
     } else {
       File templateFolder = new File (
           getTemplatesFolderText(),
           selectedTemplateFolderName);
       if ((! templateFolder.exists())
           || (! templateFolder.canRead())) {
         Trouble.getShared().report(
           this,
           "Template folder cannot be accessed",
           "Template Error",
           JOptionPane.ERROR_MESSAGE);
       } else {
         publishTo = getPublishTo();
         if (publishTo == null
             || (! publishTo.exists()
             || (! publishTo.canWrite()))) {
           Trouble.getShared().report(
             this,
             "Publish To folder is not valid",
             "Template Error",
             JOptionPane.ERROR_MESSAGE);
         } else {
           FileUtils.copyFolder(templateFolder, publishTo);
           String[] dirEntry = publishTo.list();
           for (int i = 0; i < dirEntry.length; i++) {
             String entry = dirEntry [i];
             if (entry.endsWith(".tcz")) {
               File scriptFile = new File (publishTo, entry);
               this.setPublishScript(scriptFile);
             } // end if we have a likely script file
           } // end for each file in the publish to folder
         } // end if we have a good folder to publish to
       } // end if we have a template folder we can read
     } // end if we have a selected template folder name
   } // end method applySelectedTemplate
 
   // Getters and setters for publish when
 
   private void setPublishWhen (String publishWhen) {
     int i = 0;
     boolean found = false;
     while ((! found) && (i < publishWhenComboBox.getItemCount())) {
       if (publishWhen.equalsIgnoreCase
           ((String)publishWhenComboBox.getItemAt(i))) {
         publishWhenComboBox.setSelectedIndex(i);
         found = true;
       } else {
         i++;
       }
     } // end while looking for match
   } // end method setPublishWhen
 
   private String getPublishWhen() {
     return (String)publishWhenComboBox.getSelectedItem();
   }
   
   public void publishNow() {
     publish();
   }
 
   /**
    Perform the publication process.
    */
   private void publish() {
     
     // See if we have a good publishTo location
     if (currentSelectionIndex >= 0) {
       publishTo = new File ((String)publishToComboBox.getSelectedItem());
       Logger.getShared().recordEvent (LogEvent.NORMAL,
         "Publishing to " + publishTo.toString(), false);
       File publishScript = getPublishScript();
       Logger.getShared().recordEvent (LogEvent.NORMAL,
         "Using Script  " + publishScript.toString(), false);
       boolean ok = true;
       if (! publishTo.exists()) {
         ok = publishTo.mkdirs();
       }
       if (ok) {
         ok = publishTo.exists()
           && publishTo.canRead()
           && publishTo.canWrite();
       }
 
       if (ok) {
         ok = publishScript.exists()
           && publishScript.canRead();
       }
 
       if (ok) {
         assistant.prePub(publishTo);
         FileName publishScriptName = new FileName(publishScript);
 
         // See if script is an xslt transformer
         if (publishScriptName.getExt().equals("xml")) {
           XMLTransformer tf = new XMLTransformer();
           ok = tf.transform (publishScript, publishTo);
           if (ok) {
             Logger.getShared().recordEvent (LogEvent.NORMAL,
                 "  xslScript processed " + publishScript.toString(), false);
             announceStatus("Published");
           } else {
             Logger.getShared().recordEvent (LogEvent.MINOR,
                 "  xslScript error processing " + publishScript.toString(), false);
           }
         }
         else
         if (publishScriptName.getExt().equalsIgnoreCase(TextMergeScript.SCRIPT_EXT)) {
           PSTextMerge.execScript (
               publishScript.getAbsolutePath(),
               Logger.getShared(),
               this);
           announceStatus("Published");
         } else {
           Trouble.getShared().report(
               this,
               "File extension for publish script file not recognized",
               "Unknown Script Type",
               JOptionPane.ERROR_MESSAGE);
         }
 
         assistant.postPub(publishTo);
 
       } else {
         Trouble.getShared().report(
             this,
             "Trouble with the publication location and/or script",
             "Publish Error",
             JOptionPane.ERROR_MESSAGE);
       }
     }
   } // end of publish method
 
   /**
    A method provided to PSTextMerge 
    @param operand
    */
   public void scriptCallback(String operand) {
     if (assistant != null
         && publishTo != null) {
       assistant.pubOperation(publishTo, operand);
     }
   }
 
   /**
    Open the designated file in the user's Web browser.
 
    @param file The file to be browsed.
    */
   private boolean openURL (File file) {
     return home.openURL(file);
   }
 
   /**
    Open the designated URL in the user's Web browser.
 
    @param url The URL to be browsed.
    */
   private boolean openURL (String url) {
     return home.openURL(url);
   }
   
   /**
    The user has indicated a desire to blank out the publish to folder:
    see if they are trying to remove an existing publication.
    */
   private void askRemove() {
     if (option == UNDEFINED) {
       option = JOptionPane.showOptionDialog(
         this,
         "Remove existing location?",
         "Remove",
         JOptionPane.OK_CANCEL_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null,
         publishRemoveOptions,
         publishRemoveOptions[0]);
       if (option == 0) {
         option = REMOVE;
       } else {
         option = CANCEL;
       }
     }
   }
 
   /**
    The user has indicated a desire to modify the publish to folder:
    see if they are trying to add a new publication, or modify an
    existing one.
    */
   private void askAddOrReplace() {
     if (option == UNDEFINED) {
       option = JOptionPane.showOptionDialog(
         this,
         "Add a new location or replace existing?",
         "Add or Replace",
         JOptionPane.YES_NO_CANCEL_OPTION,
         JOptionPane.QUESTION_MESSAGE,
         null,
         publishToOptions,
         publishToOptions[0]);
     }
   }
 
   /**
    The user has specified a publish to location. Either add it as a new one,
    or update the existing one, based on the user's previously expressed
    intentions.
 
    @param publishTo The path to the folder that the user has indicated. 
    */
   private void addOrReplaceOrRemove(String publishTo) {
 
     if (option == ADD) {
       savePublicationProperties(currentSelectionIndex);
       int pubsNumber = pubs.size();
       int addedIndex = addPublication(publishTo);
       if (addedIndex >= pubsNumber) {
         publishToComboBox.addItem(publishTo);
       }
       publishToComboBox.setSelectedIndex(addedIndex);
      setPublishTo(addedIndex);
       initPublicationProperties();
     }
     else
     if (option == REPLACE) {
       int selected = publishToComboBox.getSelectedIndex();
       if (selected >= 0) {
         pubs.set(publishToComboBox.getSelectedIndex(), publishTo);
         publishToComboBox.removeItemAt(selected);
         publishToComboBox.insertItemAt(publishTo, selected);
         publishToComboBox.setSelectedIndex(selected);
         setPublishScript("");
       }
     } 
     else
     if (option == REMOVE) {
       if (currentSelectionIndex >= 0) {
         publishToComboBox.removeItemAt(currentSelectionIndex);
         pubs.remove(currentSelectionIndex);
         if (publishToComboBox.getItemCount() > 0) {
           setPublishTo(0);
         }
       }
     }
     else
     if (option == CANCEL) {
       publishToComboBox.setSelectedIndex(currentSelectionIndex);
     }
     option = UNDEFINED;
   } // end method addOrReplaceOrRemove
   
   /**
    Sets the statusBar to be used for identifying status. 
   
    @param statusBar A JLabel to be used for brief status messages. 
   */
   public void setStatusBar (StatusBar statusBar) {
     this.statusBar = statusBar;
   }
   
   private void announceStatus(String msg) {
     if (statusBar != null) {
       statusBar.setStatus(msg);
     }
   }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {
     java.awt.GridBagConstraints gridBagConstraints;
 
     publishToPanel = new javax.swing.JPanel();
     publishToLabel = new javax.swing.JLabel();
     filler2 = new javax.swing.JLabel();
     publishToBrowseButton = new javax.swing.JButton();
     equivalentURLLabel = new javax.swing.JLabel();
     equivalentURLText = new javax.swing.JTextField();
     publishToComboBox = new javax.swing.JComboBox();
     viewPanel = new javax.swing.JPanel();
     viewLabel = new javax.swing.JLabel();
     viewWhereComboBox = new javax.swing.JComboBox();
     viewNowButton = new javax.swing.JButton();
     templatePanel = new javax.swing.JPanel();
     templatesLabel = new javax.swing.JLabel();
     filler1 = new javax.swing.JLabel();
     templatesBrowseButton = new javax.swing.JButton();
     templatesText = new javax.swing.JTextField();
     templateSelectLabel = new javax.swing.JLabel();
     templateSelectComboBox = new javax.swing.JComboBox();
     templateApplyButton = new javax.swing.JButton();
     publishPanel = new javax.swing.JPanel();
     publishScriptText = new javax.swing.JTextField();
     publishScriptLabel = new javax.swing.JLabel();
     publishScriptBrowseButton = new javax.swing.JButton();
     filler3 = new javax.swing.JLabel();
     publishWhenLabel = new javax.swing.JLabel();
     publishWhenComboBox = new javax.swing.JComboBox();
     publishNowButton = new javax.swing.JButton();
 
     setTitle("Publish");
     addComponentListener(new java.awt.event.ComponentAdapter() {
       public void componentHidden(java.awt.event.ComponentEvent evt) {
         formComponentHidden(evt);
       }
     });
     getContentPane().setLayout(new java.awt.GridBagLayout());
 
     publishToPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
     publishToPanel.setLayout(new java.awt.GridBagLayout());
 
     publishToLabel.setText("Publish to:");
     publishToLabel.setMaximumSize(new java.awt.Dimension(120, 25));
     publishToLabel.setMinimumSize(new java.awt.Dimension(100, 19));
     publishToLabel.setPreferredSize(new java.awt.Dimension(100, 19));
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishToPanel.add(publishToLabel, gridBagConstraints);
 
     filler2.setText(" ");
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 1;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishToPanel.add(filler2, gridBagConstraints);
 
     publishToBrowseButton.setText("Browse...");
     publishToBrowseButton.setMaximumSize(new java.awt.Dimension(150, 35));
     publishToBrowseButton.setMinimumSize(new java.awt.Dimension(130, 29));
     publishToBrowseButton.setPreferredSize(new java.awt.Dimension(130, 29));
     publishToBrowseButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         publishToBrowseButtonActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishToPanel.add(publishToBrowseButton, gridBagConstraints);
 
     equivalentURLLabel.setText("Equivalent URL:");
     equivalentURLLabel.setMaximumSize(new java.awt.Dimension(120, 25));
     equivalentURLLabel.setMinimumSize(new java.awt.Dimension(100, 19));
     equivalentURLLabel.setPreferredSize(new java.awt.Dimension(100, 19));
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 2;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishToPanel.add(equivalentURLLabel, gridBagConstraints);
 
     equivalentURLText.setColumns(50);
     equivalentURLText.setToolTipText("Folder to which data will be published");
     equivalentURLText.addFocusListener(new java.awt.event.FocusAdapter() {
       public void focusLost(java.awt.event.FocusEvent evt) {
         equivalentURLTextFocusLost(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 3;
     gridBagConstraints.gridwidth = 3;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishToPanel.add(equivalentURLText, gridBagConstraints);
 
     publishToComboBox.setEditable(true);
     publishToComboBox.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         publishToComboBoxActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 1;
     gridBagConstraints.gridwidth = 3;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishToPanel.add(publishToComboBox, gridBagConstraints);
 
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.weighty = 0.6;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     getContentPane().add(publishToPanel, gridBagConstraints);
 
     viewPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
     viewPanel.setLayout(new java.awt.GridBagLayout());
 
     viewLabel.setText("View:");
     viewLabel.setMaximumSize(new java.awt.Dimension(120, 25));
     viewLabel.setMinimumSize(new java.awt.Dimension(100, 19));
     viewLabel.setPreferredSize(new java.awt.Dimension(100, 19));
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     viewPanel.add(viewLabel, gridBagConstraints);
 
     viewWhereComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Local File", "On Web" }));
     viewWhereComboBox.setMinimumSize(new java.awt.Dimension(169, 27));
     viewWhereComboBox.setPreferredSize(new java.awt.Dimension(169, 27));
     viewWhereComboBox.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         viewWhereComboBoxActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 1;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.weightx = 0.5;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     viewPanel.add(viewWhereComboBox, gridBagConstraints);
 
     viewNowButton.setText("View Now");
     viewNowButton.setMaximumSize(new java.awt.Dimension(150, 35));
     viewNowButton.setMinimumSize(new java.awt.Dimension(130, 29));
     viewNowButton.setPreferredSize(new java.awt.Dimension(130, 29));
     viewNowButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         viewNowButtonActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     viewPanel.add(viewNowButton, gridBagConstraints);
 
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 3;
     gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
     gridBagConstraints.weighty = 0.2;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     getContentPane().add(viewPanel, gridBagConstraints);
 
     templatePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
     templatePanel.setLayout(new java.awt.GridBagLayout());
 
     templatesLabel.setText("Templates:");
     templatesLabel.setMaximumSize(new java.awt.Dimension(120, 25));
     templatesLabel.setMinimumSize(new java.awt.Dimension(100, 19));
     templatesLabel.setPreferredSize(new java.awt.Dimension(100, 19));
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     templatePanel.add(templatesLabel, gridBagConstraints);
 
     filler1.setText(" ");
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 1;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     templatePanel.add(filler1, gridBagConstraints);
 
     templatesBrowseButton.setText("Browse...");
     templatesBrowseButton.setMaximumSize(new java.awt.Dimension(150, 35));
     templatesBrowseButton.setMinimumSize(new java.awt.Dimension(130, 29));
     templatesBrowseButton.setPreferredSize(new java.awt.Dimension(130, 29));
     templatesBrowseButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         templatesBrowseButtonActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 0;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     templatePanel.add(templatesBrowseButton, gridBagConstraints);
 
     templatesText.setColumns(50);
     templatesText.setToolTipText("Folder to which data will be published");
     templatesText.addFocusListener(new java.awt.event.FocusAdapter() {
       public void focusLost(java.awt.event.FocusEvent evt) {
         templatesTextFocusLost(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 1;
     gridBagConstraints.gridwidth = 3;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     templatePanel.add(templatesText, gridBagConstraints);
 
     templateSelectLabel.setText("Select:");
     templateSelectLabel.setMaximumSize(new java.awt.Dimension(120, 25));
     templateSelectLabel.setMinimumSize(new java.awt.Dimension(100, 19));
     templateSelectLabel.setPreferredSize(new java.awt.Dimension(100, 19));
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 2;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     templatePanel.add(templateSelectLabel, gridBagConstraints);
 
     templateSelectComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Template Folder 1", "Template Folder 2", "etc." }));
     templateSelectComboBox.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         templateSelectComboBoxActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 1;
     gridBagConstraints.gridy = 2;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.weightx = 0.5;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     templatePanel.add(templateSelectComboBox, gridBagConstraints);
 
     templateApplyButton.setText("Apply");
     templateApplyButton.setMaximumSize(new java.awt.Dimension(150, 35));
     templateApplyButton.setMinimumSize(new java.awt.Dimension(130, 29));
     templateApplyButton.setPreferredSize(new java.awt.Dimension(130, 29));
     templateApplyButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         templateApplyButtonActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 2;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     templatePanel.add(templateApplyButton, gridBagConstraints);
 
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 1;
     gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.weighty = 0.2;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     getContentPane().add(templatePanel, gridBagConstraints);
 
     publishPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
     publishPanel.setLayout(new java.awt.GridBagLayout());
 
     publishScriptText.setColumns(50);
     publishScriptText.setToolTipText("Folder to which data will be published");
     publishScriptText.addFocusListener(new java.awt.event.FocusAdapter() {
       public void focusLost(java.awt.event.FocusEvent evt) {
         publishScriptTextFocusLost(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 5;
     gridBagConstraints.gridwidth = 3;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishPanel.add(publishScriptText, gridBagConstraints);
 
     publishScriptLabel.setText("Publish Script:");
     publishScriptLabel.setMaximumSize(new java.awt.Dimension(120, 25));
     publishScriptLabel.setMinimumSize(new java.awt.Dimension(100, 19));
     publishScriptLabel.setPreferredSize(new java.awt.Dimension(100, 19));
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 4;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishPanel.add(publishScriptLabel, gridBagConstraints);
 
     publishScriptBrowseButton.setText("Browse...");
     publishScriptBrowseButton.setMaximumSize(new java.awt.Dimension(150, 35));
     publishScriptBrowseButton.setMinimumSize(new java.awt.Dimension(130, 29));
     publishScriptBrowseButton.setPreferredSize(new java.awt.Dimension(130, 29));
     publishScriptBrowseButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         publishScriptBrowseButtonActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 4;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishPanel.add(publishScriptBrowseButton, gridBagConstraints);
 
     filler3.setText(" ");
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 1;
     gridBagConstraints.gridy = 4;
     gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.weightx = 1.0;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishPanel.add(filler3, gridBagConstraints);
 
     publishWhenLabel.setText("Publish when:");
     publishWhenLabel.setMaximumSize(new java.awt.Dimension(120, 25));
     publishWhenLabel.setMinimumSize(new java.awt.Dimension(100, 19));
     publishWhenLabel.setPreferredSize(new java.awt.Dimension(100, 19));
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 6;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishPanel.add(publishWhenLabel, gridBagConstraints);
 
     publishWhenComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "On Demand", "On Close", "On Save", " ", " " }));
     publishWhenComboBox.setMinimumSize(new java.awt.Dimension(169, 27));
     publishWhenComboBox.setPreferredSize(new java.awt.Dimension(169, 27));
     publishWhenComboBox.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         publishWhenComboBoxActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 1;
     gridBagConstraints.gridy = 6;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
     gridBagConstraints.weightx = 0.5;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishPanel.add(publishWhenComboBox, gridBagConstraints);
 
     publishNowButton.setText("Publish Now");
     publishNowButton.setMaximumSize(new java.awt.Dimension(150, 35));
     publishNowButton.setMinimumSize(new java.awt.Dimension(130, 29));
     publishNowButton.setPreferredSize(new java.awt.Dimension(130, 29));
     publishNowButton.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         publishNowButtonActionPerformed(evt);
       }
     });
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 2;
     gridBagConstraints.gridy = 6;
     gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     publishPanel.add(publishNowButton, gridBagConstraints);
 
     gridBagConstraints = new java.awt.GridBagConstraints();
     gridBagConstraints.gridx = 0;
     gridBagConstraints.gridy = 2;
     gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
     gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
     getContentPane().add(publishPanel, gridBagConstraints);
 
     pack();
   }// </editor-fold>//GEN-END:initComponents
 
 private void templatesBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templatesBrowseButtonActionPerformed
   XFileChooser chooser = new XFileChooser();
   chooser.setDialogTitle("Specify Templates Folder");
   chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
   File chosen = chooser.showOpenDialog(this);
   if (chosen != null) {
     setTemplatesFolder(chosen);
   }
 
 }//GEN-LAST:event_templatesBrowseButtonActionPerformed
 
 private void templateApplyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateApplyButtonActionPerformed
   applySelectedTemplate();
 }//GEN-LAST:event_templateApplyButtonActionPerformed
 
 private void publishToBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishToBrowseButtonActionPerformed
 
   inputSource = SYSTEM_INPUT;
   if (publishToComboBox.getSelectedItem() != null
       && publishToComboBox.getSelectedItem().toString().length() > 0) {
     askAddOrReplace();
   } else {
     option = ADD;
   }
   
   if (option == ADD || option == REPLACE) {
     XFileChooser chooser = new XFileChooser();
     chooser.setDialogTitle("Specify Target Folder for Publication");
     chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
     File chosen = chooser.showOpenDialog(this);
     if (chosen != null) {
       try {
         String publishToString = chosen.getCanonicalPath();
         addOrReplaceOrRemove(publishToString);
       } catch (java.io.IOException e) {
         // Let's just pretend this whole thing never happened
       }
     } // end if user chose a folder
   } // end if user specified whether to add or replace
   inputSource = USER_INPUT;
 }//GEN-LAST:event_publishToBrowseButtonActionPerformed
 
 private void publishNowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishNowButtonActionPerformed
   savePublicationProperties(currentSelectionIndex);
   publish();
 }//GEN-LAST:event_publishNowButtonActionPerformed
 
 private void templateSelectComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_templateSelectComboBoxActionPerformed
 
 }//GEN-LAST:event_templateSelectComboBoxActionPerformed
 
 private void templatesTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_templatesTextFocusLost
 
 }//GEN-LAST:event_templatesTextFocusLost
 
 private void publishScriptBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishScriptBrowseButtonActionPerformed
   XFileChooser chooser = new XFileChooser();
   chooser.setDialogTitle("Locate Publication Script");
   chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
   File chosen = chooser.showOpenDialog(this);
   if (chosen != null) {
     setPublishScript(chosen);
   }
 }//GEN-LAST:event_publishScriptBrowseButtonActionPerformed
 
 private void publishScriptTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_publishScriptTextFocusLost
 
 }//GEN-LAST:event_publishScriptTextFocusLost
 
 private void equivalentURLTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_equivalentURLTextFocusLost
 
 }//GEN-LAST:event_equivalentURLTextFocusLost
 
 private void viewNowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewNowButtonActionPerformed
   File indexFile = new File (getPublishTo(), INDEX_FILE_NAME);
   if (indexFile.exists()
       && indexFile.canRead()
       && indexFile.isFile()) {
     if (viewWhereComboBox.getSelectedIndex() == 0) {
       openURL (indexFile);
     } else {
       String indexFileName = indexFile.getPath();
       openURL (getEquivalentURLText()
           + indexFileName.substring(getPublishToText().length()));
     }
   } // end if index file is available
 }//GEN-LAST:event_viewNowButtonActionPerformed
 
 private void viewWhereComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewWhereComboBoxActionPerformed
 // TODO add your handling code here:
 }//GEN-LAST:event_viewWhereComboBoxActionPerformed
 
 private void publishWhenComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishWhenComboBoxActionPerformed
 
 }//GEN-LAST:event_publishWhenComboBoxActionPerformed
 
 private void publishToComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_publishToComboBoxActionPerformed
   if (inputSource == USER_INPUT) {
     inputSource = SYSTEM_INPUT;
     if (publishToComboBox.getSelectedIndex() >= 0) {
       // User has selected an existing value from the drop-down menu
       switchPublishTo(
           currentSelectionIndex, 
           publishToComboBox.getSelectedIndex());
     } else {
       // User has typed in a new value
       String publishToString = (String)publishToComboBox.getSelectedItem();
       if (option == UNDEFINED || option == CANCEL) {
         if (publishToString.length() == 0) {
           askRemove();
         } else {
           askAddOrReplace();
         }
       }
       addOrReplaceOrRemove(publishToString);
     }
     inputSource = USER_INPUT;
   } // end method publishToComboBoxActionPerformed
 }//GEN-LAST:event_publishToComboBoxActionPerformed
 
 private void formComponentHidden(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentHidden
   WindowMenuManager.getShared().hide(this);
 }//GEN-LAST:event_formComponentHidden
 
 
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JLabel equivalentURLLabel;
   private javax.swing.JTextField equivalentURLText;
   private javax.swing.JLabel filler1;
   private javax.swing.JLabel filler2;
   private javax.swing.JLabel filler3;
   private javax.swing.JButton publishNowButton;
   private javax.swing.JPanel publishPanel;
   private javax.swing.JButton publishScriptBrowseButton;
   private javax.swing.JLabel publishScriptLabel;
   private javax.swing.JTextField publishScriptText;
   private javax.swing.JButton publishToBrowseButton;
   private javax.swing.JComboBox publishToComboBox;
   private javax.swing.JLabel publishToLabel;
   private javax.swing.JPanel publishToPanel;
   private javax.swing.JComboBox publishWhenComboBox;
   private javax.swing.JLabel publishWhenLabel;
   private javax.swing.JButton templateApplyButton;
   private javax.swing.JPanel templatePanel;
   private javax.swing.JComboBox templateSelectComboBox;
   private javax.swing.JLabel templateSelectLabel;
   private javax.swing.JButton templatesBrowseButton;
   private javax.swing.JLabel templatesLabel;
   private javax.swing.JTextField templatesText;
   private javax.swing.JLabel viewLabel;
   private javax.swing.JButton viewNowButton;
   private javax.swing.JPanel viewPanel;
   private javax.swing.JComboBox viewWhereComboBox;
   // End of variables declaration//GEN-END:variables
 
 }
