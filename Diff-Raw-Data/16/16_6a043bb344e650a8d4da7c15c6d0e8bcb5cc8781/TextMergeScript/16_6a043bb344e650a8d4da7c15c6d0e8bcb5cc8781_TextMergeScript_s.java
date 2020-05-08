 /*
  * Copyright 1999 - 2013 Herb Bowie
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
 
 package com.powersurgepub.psdatalib.textmerge;
 
   import com.powersurgepub.psdatalib.pslist.*;
   import com.powersurgepub.psdatalib.script.*;
   import com.powersurgepub.psdatalib.ui.*;
   import com.powersurgepub.psfiles.*;
   import com.powersurgepub.psutils.*;
   import java.awt.*;
   import java.awt.event.*;
   import java.io.*;
   import java.net.*;
   import javax.swing.*;
   import javax.swing.border.*;
 
 /**
   Module to record script actions. 
 
   @author Herb Bowie
  */
 public class TextMergeScript 
     implements FileSpecOpener {
   
   /** Default file extension for script files. */
   public		static	final String  SCRIPT_EXT       = "tcz";
   
   public    static  final String  AUTOPLAY         = "autoplay";
   
   private     boolean             quietMode = true;
   private     boolean             tabSet = false;
   private     boolean             menuSet = false;
   
   private     ScriptExecutor      scriptExecutor = null;
   
   private     PSList              psList = null;
   
   private     File                templateLibrary = null;
   
   private     TextMergeInput      inputModule = null;
   private     TextMergeFilter     filterModule = null;
   private     TextMergeSort       sortModule = null;
   private     TextMergeTemplate   templateModule = null;
   private     TextMergeOutput     outputModule = null;
   
   private     JTabbedPane         tabs = null;
   private     JMenuBar            menus = null;
   
   private     File                currentDirectory = null;
   private			String							normalizerPath = "";
   
   // Fields used for the User Interface
   private     Border              raisedBevel;
   private     Border              etched;
   private     GridBagger          gb = new GridBagger();
   
   // Script panel objects
   private     JPanel              scriptPanel;
   private     JButton             scriptRecordButton  = new JButton();
   private     JButton             scriptStopButton    = new JButton();
   private     JButton             scriptPlayButton    = new JButton();
   private     JButton             scriptReplayButton  = new JButton();
   private     JButton             scriptAutoPlayButton = new JButton();
   private     JButton             scriptEasyPlayButton = new JButton();
   private     JLabel							scriptPlaceHolder;
   private     JScrollPane         scriptTextScrollPane;
   private     JTextArea           scriptText = new JTextArea("");
   
 	private     boolean             scriptRecording = false;
   
 	private     ScriptFile          outScript;
   private     File                outScriptFile;
   
 	private     ScriptAction        outAction;
   
   private			JMenu								scriptMenu = null;
   private			JMenuItem						scriptRecord = null;
   private			JMenuItem						scriptEndRecording = null;
   private			JMenuItem						scriptPlay = null;
   private			JMenuItem						scriptReplay = null;
   private     JMenuItem           scriptClear = null;
   private     JMenuItem           scriptAutoPlay = null;
   private     JMenuItem           scriptEasyPlay = null;
   private     JMenu               recentScriptsMenu = null;
   
   // Easy Play panel objects
   private     JPanel              easyPlayTab = null;
   
 	/*
 	   Scripting stuff
 	 */
   private     File                scriptDirectory = null;
 	private     URL                 scriptURL;
 	private     ScriptFile          inScript = null;
 	private     File                inScriptFile = null;
 	private     ScriptAction        inAction;
 	private     String              inActionModule;
 	private     String              inActionAction;
 	private     String              inActionModifier;
 	private     String              inActionObject;
 	private     String              inActionValue;
   private			int									inActionValueAsInt;
   private			boolean							inActionValueValidInt;
   private			String							inputObject = "";
 	private     boolean             scriptPlaying = false; 
   private     RecentFiles         recentScripts = null; 
   private     boolean             autoplayAllowed = true;
   private     String              autoPlay = "";
   private     String              easyPlay = "";
   private     File                easyPlayFolder = null;
   
   public TextMergeScript (PSList psList) {
     this.psList = psList;
     setListOptions();
   }
   
   public void allowAutoplay (boolean autoplayAllowed) {
     this.autoplayAllowed = autoplayAllowed;
   }
   
   /**
    If the user has specified a script file to play at startup, then play it
    when requested. 
   
    @return True if a script was automatically played, false otherwise. 
   */
   public boolean checkAutoPlay() {
     boolean played = false;
     if (autoplayAllowed) {
       autoPlay = UserPrefs.getShared().getPref(AUTOPLAY, "");
       if (autoPlay.length() > 0) {
         File autoPlayFile = new File (autoPlay);
         if (autoPlayFile.exists() && autoPlayFile.canRead()) {
           inScriptFile = autoPlayFile;
           inScript = new ScriptFile (inScriptFile, getTemplateLibrary().toString());
           playScript();
           played = true;
         } // end if input script file is available
       } // end if autoplay was specified
     }
     return played;
   }
   
   public void setPSList (PSList psList) {
     this.psList = psList;
     setListOptions();
   }
   
   public void setInputModule (TextMergeInput inputModule) {
     this.inputModule = inputModule;
   }
   
   public void setFilterModule (TextMergeFilter filterModule) {
     this.filterModule = filterModule;
   }
   
   public void setSortModule (TextMergeSort sortModule) {
     this.sortModule = sortModule;
   }
   
   public void setTemplateModule (TextMergeTemplate templateModule) {
     this.templateModule = templateModule;
   }
   
   private File getTemplateLibrary() {
     if (templateModule == null) {
       return templateLibrary;
     } else {
       return templateModule.getTemplateLibrary();
     }
   }
   
   public void setOutputModule (TextMergeOutput outputModule) {
     this.outputModule = outputModule;
   }
   
   /**
    Set a class to be used for callbacks. 
   
    @param scriptExecutor The class to be used for callbacks.
   */
   public void setScriptExecutor(ScriptExecutor scriptExecutor) {
     this.scriptExecutor = scriptExecutor;
   }
   
   public void setCurrentDirectory (File currentDirectory) {
     this.currentDirectory = currentDirectory;
   }
   
   public void setCurrentDirectoryFromFile (File currentFile) {
     this.currentDirectory = currentFile.getParentFile();
   }
   
   public boolean hasCurrentDirectory() {
     return (currentDirectory != null);
   }
   
   public File getCurrentDirectory () {
     return currentDirectory;
   }
   
   public void setNormalizerPath (String normalizerPath) {
     this.normalizerPath = normalizerPath;
   }
   
   public String getNormalizerPath() {
     return normalizerPath;
   }
   
   public void setMenus(JMenuBar menus, String menuText) {
     
     quietMode = false;
     menuSet = true;
     
     this.menus = menus;
     scriptMenu = new JMenu(menuText);
     menus.add (scriptMenu);
     
     // Equivalent Menu Item to Record a Script
     scriptRecord = new JMenuItem ("Record...");
     scriptRecord.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_R,
         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     scriptMenu.add (scriptRecord);
     scriptRecord.addActionListener (new ActionListener() 
       {
         public void actionPerformed (ActionEvent event) {
           selectTab();
           startScriptRecording();		    
         } // end ActionPerformed method
       } // end action listener
     );
     
     // Equivalent Menu Item to Stop Recording of a Script
     scriptEndRecording = new JMenuItem ("End Recording");
     scriptEndRecording.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_E,
         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     scriptMenu.add (scriptEndRecording);
     scriptEndRecording.addActionListener (new ActionListener() 
       {
         public void actionPerformed (ActionEvent event) {
           selectTab();
           stopScriptRecordingUI();		    
         } // end ActionPerformed method
       } // end action listener
     );
     scriptEndRecording.setEnabled (false);
     
     // Equivalent Menu Item to Play a Script
     scriptPlay = new JMenuItem ("Play");
     scriptPlay.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_P,
         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     scriptMenu.add (scriptPlay);
     scriptPlay.addActionListener (new ActionListener() 
       {
         public void actionPerformed (ActionEvent event) {
           selectTab();
           startScriptPlaying();		    
         } // end ActionPerformed method
       } // end action listener
     );
     
     // Equivalent Menu Item to Replay a Script
     scriptReplay = new JMenuItem ("Play Again");
     scriptReplay.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_A,
         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
     scriptMenu.add (scriptReplay);
     scriptReplay.addActionListener (new ActionListener() 
       {
         public void actionPerformed (ActionEvent event) {
           selectTab();
           startScriptPlayingAgain();		    
         } // end ActionPerformed method
       } // end action listener
     );
     
     // Menu Item to Clear Sort and Filter settings
     scriptClear = new JMenuItem ("Clear");
     scriptClear.setToolTipText("Clear sort and filter settings");
     scriptMenu.add (scriptClear);
     scriptClear.addActionListener (new ActionListener() 
       {
         public void actionPerformed (ActionEvent event) {
           clearSortAndFilterSettings();	    
         } // end ActionPerformed method
       } // end action listener
     );
     
     // Equivalent Menu Item to AutoPlay a Script
     if (autoplayAllowed) {
       if (autoPlay.length() == 0) {
         scriptAutoPlay = new JMenuItem ("Turn Autoplay On");
       } else {
         scriptAutoPlay = new JMenuItem("Turn Autoplay Off");
       }
       scriptMenu.add (scriptAutoPlay);
       scriptAutoPlay.addActionListener (new ActionListener() 
         {
           public void actionPerformed (ActionEvent event) {
             toggleAutoPlay();		    
           } // end ActionPerformed method
         } // end action listener
       );
     }
     
     // Equivalent Menu Item to EasyPlay a Script
     if (easyPlay.length() == 0) {
       scriptEasyPlay = new JMenuItem ("Turn Easy Play On");
     } else {
       scriptEasyPlay = new JMenuItem("Turn Easy Play Off");
     }
     scriptMenu.add (scriptEasyPlay);
     scriptEasyPlay.addActionListener (new ActionListener() 
       {
         public void actionPerformed (ActionEvent event) {
           toggleEasyPlay();		    
         } // end ActionPerformed method
       } // end action listener
     );
     
     // Initialize Recent Scripts
     recentScriptsMenu = new JMenu ("Play Recent Script");
     scriptMenu.add(recentScriptsMenu);
     recentScripts = new RecentFiles("recentscript");
     recentScripts.loadFromPrefs();
     recentScripts.setRecentFilesMax(10);
     recentScripts.registerMenu(recentScriptsMenu, this);
     
   } // end method setMenus
   
   public void setTabs(JTabbedPane tabs) {
     quietMode = false;
     tabSet = true;
     
     this.tabs = tabs;
     
 		scriptPanel = new JPanel();
 				
 		// Button to record a script
 		scriptRecordButton.setText("Record");
 		scriptRecordButton.setVisible(true);
 		scriptRecordButton.setToolTipText("Start recording your actions");
 		scriptRecordButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           startScriptRecording();
 		    } // end ActionPerformed method
 		  } // end action listener for script record button
 		);
 
     // Button to stop recording
 		scriptStopButton.setText("Stop");
 		scriptStopButton.setVisible(true);
 		scriptStopButton.setToolTipText("Stop recording");
 		scriptStopButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
 		      stopScriptRecordingUI();
 		    } // end ActionPerformed method
 		  } // end action listener for script stop button
 		);
     
     // Set Off Initially
     scriptStopButton.setEnabled (false);
 
     // Button to playback a script that has already been recorded
 		scriptPlayButton.setText("Play");
 		scriptPlayButton.setVisible(true);
 		scriptPlayButton.setToolTipText("Play back a previously recorded script");
 		scriptPlayButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           startScriptPlaying();
 		    } // end ActionPerformed method
 		  } // end action listener for script play button
 		); 
 
     // Button to replay the script just played/recorded
 		scriptReplayButton.setText("Play Again");
 		scriptReplayButton.setVisible(true);
 		scriptReplayButton.setToolTipText("Replay the last script");
 		scriptReplayButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           startScriptPlayingAgain();
 		    } // end ActionPerformed method
 		  } // end action listener for script play button
 		); 
     
     // Set Off Initially
     setScriptReplayControls();
     
     // Button to set an auto play script 
     if (autoplayAllowed) {
       scriptAutoPlayButton.setVisible(true);
       scriptAutoPlayButton.setToolTipText("Select a script to automatically play at startup");
       scriptAutoPlayButton.addActionListener (new ActionListener()
         {
           public void actionPerformed (ActionEvent event) {
             toggleAutoPlay();
           } // end ActionPerformed method
         } // end action listener for script play button
       ); 
     }
     
     // Button to set an easy play folder 
 		scriptEasyPlayButton.setVisible(true);
 		scriptEasyPlayButton.setToolTipText
         ("Select a folder of scripts to be invoked via buttons");
 		scriptEasyPlayButton.addActionListener (new ActionListener()
 		  {
 		    public void actionPerformed (ActionEvent event) {
           toggleEasyPlay();
 		    } // end ActionPerformed method
 		  } // end action listener for script play button
 		); 
     
     // Create place holder for third column
     scriptPlaceHolder       
         = new JLabel ("                            ", JLabel.CENTER);
 				
 		scriptText.setLineWrap (true);
 		scriptText.setEditable (false);
 		scriptText.setWrapStyleWord (true);
 		scriptText.setVisible (true);
     
     scriptTextScrollPane = new JScrollPane(scriptText, 
 		  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
 		  JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scriptTextScrollPane.setVisible(true);
 
 		gb.startLayout (scriptPanel, 3, 3);
 		gb.setDefaultRowWeight (0.0);
 		
 		gb.setAllInsets (1);
 		
 		gb.add (scriptRecordButton);
 		gb.add (scriptPlayButton);
     if (autoplayAllowed) {
       gb.add (scriptAutoPlayButton);
     }
     
     gb.add (scriptStopButton);
 		gb.add (scriptReplayButton);
     gb.add (scriptEasyPlayButton);
     // gb.add (scriptPlaceHolder);
 		
 		gb.nextRow ();
 		
 		gb.setWidth (3);
 		gb.setRowWeight (1.0);
 		gb.add (scriptTextScrollPane);
     
     tabs.add("Script", scriptPanel);
   }
   
   private void setListOptions() {
     
     currentDirectory = null;
     scriptDirectory = null;
     templateLibrary = null;
     easyPlay = "";
     if (tabs != null && easyPlayTab != null) {
       removeEasyPlayTab();
     }
     
     if (psList == null) {
       // System.out.println ("  psList is null");
     } else {
       
       // Set current directory
       FileSpec source = psList.getSource();
       if (source != null) {
         currentDirectory = source.getFolder();
       } else {
         // System.out.println ("  source is null");
       }
       
       // Set script directory
       String scriptDirectoryPath = null;
       if (source != null) {
         scriptDirectoryPath = source.getScriptsFolder();
       }
       if (scriptDirectoryPath != null
           && scriptDirectoryPath.length() > 0) {
         scriptDirectory = new File (scriptDirectoryPath);
         if (! scriptDirectory.exists()) {
           scriptDirectory = null;
         }
       }
       
       // Set template library
       String templateLibraryPath = null;
       if (source != null) {
         templateLibraryPath = source.getTemplatesFolder();
       }
       if (templateLibraryPath != null
           && templateLibraryPath.length() > 0) {
         templateLibrary = new File (templateLibraryPath);
         if (! templateLibrary.exists()) {
           templateLibrary = null;
         }
       } 
       if (templateLibrary == null) {
         templateLibrary = new File (
             Home.getShared().getAppFolder().getPath(),  
             "templates");
       }
       
       // Set easyplay options
       if (source != null) {
         easyPlay = source.getEasyPlay();
       }
       if (tabSet) {
         if (easyPlay.length() == 0) { 
           scriptEasyPlayButton.setText("Turn Easy Play On");
         } else {
           scriptEasyPlayButton.setText("Turn Easy Play Off");
           addEasyPlayTab(easyPlay);
        }
      }
     }
   }
   
   /**
      Start recording of a script.
    */
   private void  startScriptRecording() {
     if (! scriptRecording) {
       JFileChooser fileChooser = new JFileChooser();
       if (scriptDirectory != null) {
         fileChooser.setCurrentDirectory(scriptDirectory);
       }
       else
       if (currentDirectory != null) {
         fileChooser.setCurrentDirectory (currentDirectory);
       } 
       fileChooser.setDialogTitle ("Create Output File to Store Script");
       int fileChooserReturn 
         = fileChooser.showSaveDialog (scriptRecordButton);
       if (fileChooserReturn 
         == JFileChooser.APPROVE_OPTION) {
         outScriptFile = fileChooser.getSelectedFile();
         FileName outScriptFileName 
             = new FileName (outScriptFile, FileName.FILE_TYPE);
         if (outScriptFileName.getExt().trim().equals ("")) {
           outScriptFile 
               = new File (outScriptFileName.getPath(), 
                   outScriptFileName.replaceExt (SCRIPT_EXT));
         }
         outScript = new ScriptFile (outScriptFile, getTemplateLibrary().toString());
         // setCurrentDirectoryFromFile (outScriptFile);
         setScriptDirectoryFromFile (outScriptFile);
         normalizerPath = scriptDirectory.getPath();
         outScript.setLog (Logger.getShared());
         outScript.openForOutput();
         scriptRecording = true;
         scriptText.append ("Recording new script "
           + outScript.getFileName() + GlobalConstants.LINE_FEED_STRING);
           
         if (tabSet) {
           scriptRecordButton.setEnabled (false);
           scriptStopButton.setEnabled (true);
           scriptStopButton.requestFocus();
           scriptPlayButton.setEnabled (false);
           scriptReplayButton.setEnabled (false);
         }
         if (menuSet) {
           scriptRecord.setEnabled (false);
           scriptEndRecording.setEnabled (true);
           scriptPlay.setEnabled (false);
           scriptReplay.setEnabled (false);
         }
       } // end if file approved
     } // end if not already recording
   }
   
   /**
      Stop recording of a script.
    */
   private void stopScriptRecordingUI () {
     
     stopScriptRecording();
     
     if (tabSet) {
       scriptRecordButton.setEnabled (true);
       scriptStopButton.setEnabled (false);
       scriptPlayButton.setEnabled (true);
     }
     if (menuSet) {
       scriptRecord.setEnabled (true);
       scriptEndRecording.setEnabled (false);
       scriptPlay.setEnabled (true);
     }
     setScriptReplayControls();
   }
   
   /**
      Start the playback of a script.
    */
   private void startScriptPlaying() {
     JFileChooser fileChooser = new JFileChooser();
     if (scriptDirectory != null) {
       fileChooser.setCurrentDirectory(scriptDirectory);
     }
     else
     if (currentDirectory != null) {
       fileChooser.setCurrentDirectory (currentDirectory);
     } 
     fileChooser.setDialogTitle ("Select Pre-Recorded Script to Play Back");
     int fileChooserReturn 
       = fileChooser.showOpenDialog (scriptPlayButton);
     if (fileChooserReturn 
         == JFileChooser.APPROVE_OPTION) {
       inScriptFile = fileChooser.getSelectedFile();
       // setCurrentDirectoryFromFile (inScriptFile);
       setScriptDirectoryFromFile (inScriptFile);
       inScript = new ScriptFile (inScriptFile, getTemplateLibrary().toString());
       playScript();
     } // end if file approved
   }
   
   /**
      Start the replay of the last script.
    */          
   private void startScriptPlayingAgain() {
     if (inScript != null) {
       playScript();
     } // end if script file defined
   }
   
   /**
      Toggle the script autoplay.
    */
   private void toggleAutoPlay() {
     if (autoPlay.length() > 0) {
       autoPlay = "";
       saveAutoPlay();
       if (menuSet) {
         scriptAutoPlay.setText("Turn Autoplay On");
       }
       if (tabSet) {
         scriptAutoPlayButton.setText("Turn Autoplay On");
       }
       // removeEasyPlayTab();
     } else {
       JFileChooser fileChooser = new JFileChooser();
       if (scriptDirectory != null) {
         fileChooser.setCurrentDirectory (scriptDirectory);
       }
       else
       if (currentDirectory != null) {
         fileChooser.setCurrentDirectory (currentDirectory);
       } 
       fileChooser.setDialogTitle ("Select Autoplay Script");
       fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
       int fileChooserReturn 
         = fileChooser.showOpenDialog (scriptAutoPlayButton);
       if (fileChooserReturn 
           == JFileChooser.APPROVE_OPTION) {
         File autoPlayFile = fileChooser.getSelectedFile();
           setScriptDirectoryFromFile(autoPlayFile);
           autoPlay = autoPlayFile.getPath();
           saveAutoPlay();
           if (menuSet) {
             scriptAutoPlay.setText("Turn Autoplay Off");
           }
           if (tabSet) {
             scriptAutoPlayButton.setText("Turn Autoplay Off");
           }
 
       } // end if file approved
     } // End if turning autoplay on
   }
   
   private void saveAutoPlay() {
     UserPrefs.getShared().setPref(AUTOPLAY, autoPlay);
   }
   
   /**
      Toggle the Easy play folder.
    */
   private void toggleEasyPlay() {
     if (easyPlay.length() > 0) {
       easyPlay = "";
       saveEasyPlay();
       if (menuSet) {
         scriptEasyPlay.setText("Turn Easy Play On");
       }
       if (tabSet) {
         scriptEasyPlayButton.setText("Turn Easy Play On");
       }
       removeEasyPlayTab();
     } else {
       JFileChooser fileChooser = new JFileChooser();
       if (scriptDirectory != null) {
         fileChooser.setCurrentDirectory(scriptDirectory);
       }
       else
       if (currentDirectory != null) {
         fileChooser.setCurrentDirectory (currentDirectory);
       } 
       fileChooser.setDialogTitle ("Select Easy Play Folder");
       fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
       int fileChooserReturn 
         = fileChooser.showOpenDialog (scriptEasyPlayButton);
       if (fileChooserReturn 
           == JFileChooser.APPROVE_OPTION) {
         easyPlayFolder = fileChooser.getSelectedFile();
         setScriptDirectoryFromDir(easyPlayFolder);
         easyPlay = easyPlayFolder.getPath();
         saveEasyPlay();
         if (menuSet) {
           scriptEasyPlay.setText("Turn Easy Play Off");
         }
         if (tabSet) {
           scriptEasyPlayButton.setText("Turn Easy Play Off");
         }
         addEasyPlayTab(easyPlay);
         selectEasyTab();
       } // end if file approved
     } // End if turning easy play on
   }
   
   private void saveEasyPlay() {
     if (psList != null) {
       FileSpec fileSpec = psList.getSource();
       if (fileSpec != null) {
         fileSpec.setEasyPlay(easyPlay);
       }
     }
   }
   
 	/**
 	   Add the Easy Play tab to the interface. 
 	 */
 	private void addEasyPlayTab (String easyPlay) {
 		
     easyPlayTab = new JPanel();
     
     gb.startLayout (easyPlayTab, 3, 6);
 		gb.setDefaultRowWeight (0.0);
 		gb.setAllInsets (1);
     
     Dimension minButtonSize = new Dimension(200, 28);
     
     easyPlayFolder = new File (easyPlay);
     String[] scripts = easyPlayFolder.list();
     for (int i = 0; i < scripts.length; i++) {
       File scriptFile = new File (easyPlayFolder, scripts[i]);
       FileName scriptName = new FileName(scriptFile);
       if (scriptName.getExt().equalsIgnoreCase(SCRIPT_EXT)) {
         JButton easyPlayButton = new JButton(scriptName.getBase());
         easyPlayButton.setActionCommand(scriptName.getBase());
         easyPlayButton.setToolTipText
             ("Play " + scriptName.getBase() + "." + SCRIPT_EXT
             + " from " + easyPlay);
         easyPlayButton.setMinimumSize(minButtonSize);
         easyPlayButton.setVisible(true);
         easyPlayButton.addActionListener(new ActionListener()
         {
           public void actionPerformed (ActionEvent event) {
             inScriptFile = new File
                 (easyPlayFolder, event.getActionCommand() + "." + SCRIPT_EXT);
             inScript = new ScriptFile (inScriptFile, getTemplateLibrary().toString());
             playScript();
           }
         });
         gb.add(easyPlayButton);
       } // end if directory entry ends with script extension
     } // end for each directory entry
 		
 		tabs.insertTab("Easy", null, easyPlayTab, "Easily play scripts from the Easy Play ", 0);
 
 	} // end method addEasyPlayTab
   
   /**
    Set the script replay controls to either be disabled if no script is 
    available, or to be enabled if a script is available. 
   */
   private void setScriptReplayControls() {
     if (inScript == null
         || inScriptFile == null) {
       if (scriptReplayButton != null) {
         scriptReplayButton.setEnabled (false);
         scriptReplayButton.setToolTipText("Replay the last script");
       }
       if (scriptReplay != null) {
         scriptReplay.setEnabled (false);
       }
       
     } else {
       if (scriptReplayButton != null) {
         scriptReplayButton.setEnabled (true);
         scriptReplayButton.setToolTipText("Replay script " + inScriptFile.getPath());
       }
       if (scriptReplay != null) {
         scriptReplay.setEnabled (true);
       }
     }
   }
   
 	public void recordScriptAction (String module, String action, String modifier, 
 	    String object, String value) {
 	  if (scriptRecording) {
   	  outAction = new ScriptAction (module, action, modifier, object, value);
   	  outScript.nextRecordOut (outAction);
   	  scriptText.append (outAction.toString() + GlobalConstants.LINE_FEED_STRING);
 	  }
 	} // end recordScriptAction method
   
   /**
    Select the easy play tab, or the first tab, if the easy play tab is not 
    active. 
    */
   public void selectEasyTab() {
     if (tabs != null) {
       tabs.setSelectedIndex(0);
     }
   }
   
   /**
    Select the tab for this panel. 
   */
   public void selectTab() {
     if (tabs != null) {
       tabs.setSelectedComponent (scriptPanel);
     }
   }
   
   private void setScriptDirectoryFromFile (File inFile) {
     scriptDirectory = new File(inFile.getParent());
     saveScriptDirectory();
   }
   
   private void setScriptDirectoryFromDir (File inFile) {
     scriptDirectory = inFile;
     saveScriptDirectory();
   }
   
   private void saveScriptDirectory() {
     
     if (psList != null) {
       FileSpec source = psList.getSource();
       if (source != null) {
         source.setScriptsFolder(scriptDirectory);
       } else {
         // System.out.println ("TextMergeScript.saveScriptDirectory source is null");
       }
     } else {
       // System.out.println ("TextMergeScript.saveScriptDirectory psList is null");
     }
   }
 	
   /**
    If we are currently recording a script, then let's close it. 
   
   */
 	public void stopScriptRecording() {
     if (scriptRecording) {
       outScript.close();
       scriptRecording = false;
       scriptText.append ("Recording stopped" + GlobalConstants.LINE_FEED_STRING);
       inScript = outScript;
       inScriptFile = outScriptFile;
       setScriptReplayControls();
     } // end if script recording
   } // end stopScriptRecording method
   
   /**
    Save user preferences before shutting down. 
   */
   public void savePrefs () {
     if (recentScripts != null) {
       recentScripts.savePrefs();
     }
   }
   
   public void clearSortAndFilterSettings() {
     if (sortModule != null) {
       sortModule.playSortModule(
           ScriptConstants.CLEAR_ACTION, 
           ScriptConstants.NO_MODIFIER, 
           ScriptConstants.NO_OBJECT);
       sortModule.playSortModule(
           ScriptConstants.SET_ACTION, 
           ScriptConstants.NO_MODIFIER, 
           ScriptConstants.PARAMS_OBJECT);
     }
     if (filterModule != null) {
       filterModule.playScript(
           ScriptConstants.CLEAR_ACTION, 
           ScriptConstants.NO_MODIFIER, 
           ScriptConstants.NO_OBJECT,
           ScriptConstants.NO_VALUE);
       filterModule.playScript(
           ScriptConstants.SET_ACTION, 
           ScriptConstants.NO_MODIFIER, 
           ScriptConstants.PARAMS_OBJECT,
           ScriptConstants.NO_VALUE);
     }
   }
   
   /**      
     Standard way to respond to a request to open a file.
    
     @param file File to be opened by this application.
    */
   public void handleOpenFile (FileSpec fileSpec) {
     playScript (fileSpec.getFile());
   }
   
   public void playScript (File sFile) {
     inScriptFile = sFile;
     // setCurrentDirectoryFromFile (inScriptFile);
     setScriptDirectoryFromFile (inScriptFile);
     inScript = new ScriptFile (inScriptFile, getTemplateLibrary().toString());
     playScript();
   }
   
   /**
      Plays back a script file that has already been recorded.
    */
   private void playScript() {
     Logger.getShared().recordEvent (LogEvent.NORMAL,
         "Playing script " + inScript.toString(),
         false);
     if (hasCurrentDirectory()) {
       normalizerPath = currentDirectory.getPath();
     }
     inScript.setLog (Logger.getShared());
     try {
       inScript.openForInput();
     } catch (IOException e) {
       if (quietMode) {
         Logger.getShared().recordEvent (LogEvent.MEDIUM, 
           "MSG003 " + inScript.toString() + " could not be opened as a valid Script File",
           true);
       } else {
         JOptionPane.showMessageDialog (tabs, 
           "Script File could not be opened successfully",
           "Script File Error",
           JOptionPane.ERROR_MESSAGE);
       }
     } 
     scriptPlaying = true;
     scriptText.append ("Playing script "
       + inScript.getFileName() + GlobalConstants.LINE_FEED_STRING);
     if (! quietMode && recentScripts != null) {
       recentScripts.addRecentFile (inScriptFile);
     }
     while (! inScript.isAtEnd()) {
       try {
         inAction = inScript.nextRecordIn();
       } catch (IOException e) {
         inAction = null;
       } 
       if (inAction != null) {
         scriptText.append ("Playing action " + 
           inAction.toString() + 
           GlobalConstants.LINE_FEED_STRING);
         inActionModule = inAction.getModule();
         inActionAction = inAction.getAction();
         inActionModifier = inAction.getModifier();
         inActionObject = inAction.getObject();
         inActionValue = inAction.getValue();
         try {
           inActionValueAsInt = Integer.parseInt (inActionValue);
           inActionValueValidInt = true;
         } catch (NumberFormatException e) {
           inActionValueAsInt = 0;
           inActionValueValidInt = false;
         }
         if (inActionModule.startsWith("<!--")) {
           Logger.getShared().recordEvent(LogEvent.NORMAL, inActionModule, false);
         }
         else
         if (inActionModule.equals (ScriptConstants.INPUT_MODULE)) {
           playInputModule();
         } 
         else
         if (inActionModule.equals (ScriptConstants.SORT_MODULE)) {
           playSortModule();
         } 
         else
         if (inActionModule.equals (ScriptConstants.COMBINE_MODULE)) {
           playCombineModule();
         } 
         else
         if (inActionModule.equals (ScriptConstants.FILTER_MODULE)) {
           playFilterModule();
         }
         else
         if (inActionModule.equals (ScriptConstants.OUTPUT_MODULE)) {
           playOutputModule();
         }
         else
         if (inActionModule.equals (ScriptConstants.TEMPLATE_MODULE)) {
           playTemplateModule();
         }
         else
         if (inActionModule.equals (ScriptConstants.CALLBACK_MODULE)) {
           playCallbackModule();
         }
         else {
           Logger.getShared().recordEvent (LogEvent.MEDIUM, 
             inActionModule + " is not a valid Scripting Module",
             true);
         } // end else unrecognized module
       } // end if inAction not null
     } // end while more script commands
     inScript.close();
     scriptPlaying = false;
     scriptText.append ("Playback stopped" + GlobalConstants.LINE_FEED_STRING);
     resetOptions();
     setScriptReplayControls();
     selectTab();
   } // end method playScript
   
   private void removeEasyPlayTab() {
     tabs.remove(easyPlayTab);
   }
   
   /**
      Play one recorded action in the Input module.
    */
   private void playInputModule () {
    if (inputModule == null) {
       Logger.getShared().recordEvent(LogEvent.MEDIUM, 
           "Input module not available to play scripted input action", false);
     } else {
       inputModule.playScript(
           inActionAction, 
           inActionModifier, 
           inActionObject, 
           inActionValue,
           inActionValueAsInt,
           inActionValueValidInt);
     }
   } // end playInputModule method
   
   /**
      Play one recorded action in the Sort module.
    */
   private void playSortModule () {
     if (sortModule == null) {
       Logger.getShared().recordEvent(LogEvent.MEDIUM, 
           "Sort module not available to play scripted sort action", false);
     } else {
       sortModule.playSortModule(
           inActionAction, 
           inActionModifier, 
           inActionObject);
     }
   } // end playSortModule method
   
   /**
      Play one recorded action in the Combine module.
    */
   private void playCombineModule () {
     if (sortModule == null
         || (! sortModule.isCombineAllowed())) {
       Logger.getShared().recordEvent(LogEvent.MEDIUM, 
           "Sort module not available to play scripted combine action", false);
     } else {
       sortModule.playCombineModule(
           inActionAction, 
           inActionModifier, 
           inActionObject, 
           inActionValue,
           inActionValueAsInt,
           inActionValueValidInt);
     }
   } // end playCombineModule method
   
   /**
      Play one recorded action in the Filter module.
    */
   private void playFilterModule () {
     if (filterModule == null) {
       Logger.getShared().recordEvent(LogEvent.MEDIUM, 
           "Filter module not available to play scripted filter action", false);
     } else {
       filterModule.playScript(
           inActionAction, 
           inActionModifier, 
           inActionObject,
           inActionValue);
     }
   }
   
   /**
      Play one recorded action in the Output module.
    */
   private void playOutputModule () {
   /*
     if (inActionAction.equals (ScriptConstants.SET_ACTION)
       && inActionObject.equals (ScriptConstants.USING_DICTIONARY_OBJECT)) {
       usingDictionary = Boolean.valueOf(inActionValue).booleanValue();
       setDictionaryImplications();
     }
     else
     if (inActionAction.equals (ScriptConstants.OPEN_ACTION)) {
       chosenOutputFile = new File (inActionValue);
       if (chosenOutputFile == null) {
         Logger.getShared().recordEvent (LogEvent.MEDIUM, 
           inActionValue + " is not a valid file name for an Output Open Action",
           true);
       }
       else {
         createOutput();
       } // end file existence selector
     } // end valid action
     else {
       Logger.getShared().recordEvent (LogEvent.MEDIUM, 
         inActionAction + " is not a valid Scripting Action for the Output Module",
         true);
     } // end Action selector
    */
   } // end playOutputModule method
   
   /**
      Play one recorded action in the Template module.
    */
   private void playTemplateModule () {
     if (templateModule == null) {
       Logger.getShared().recordEvent(LogEvent.MEDIUM, 
           "Template module not available to play scripted template action", false);
     } else {
       templateModule.playTemplateModule(
           inActionAction, 
           inActionModifier, 
           inActionObject,
           inActionValue);
     }
   } // end playTemplateModule method
 
   private void playCallbackModule() {
   
     Logger.getShared().recordEvent(LogEvent.NORMAL, 
         "Playing callback for " + inActionAction + " action", false);
     if (scriptExecutor == null) {
       Logger.getShared().recordEvent(LogEvent.MEDIUM, 
           "Script callback executor not available", false);
     }
     if (scriptExecutor != null) {
       scriptExecutor.scriptCallback(inActionAction);
     }
    
   }
   
   /**
     Reset all the input options that might have been modified by a script
     that was played.
    */
   private void resetOptions() {
     
     // initInputModules();
     
     // usingDictionary = false;
     // setDictionaryImplications();
     
     // merge = 0;
     // setMergeImplications();
     
     // dirMaxDepth = 0;
     
     // normalType = 0;
     // setNormalTypeImplications();
     
     if (! quietMode) {
       // inputTypeBox.setSelectedIndex (0);
       // inputDictionaryCkBox.setSelected (false);
       // inputMergeNoButton.setSelected (true);
       // inputDirMaxDepthValue.setText (String.valueOf(dirMaxDepth));
       // if (normalization) {
       //   inputNormalBox.setSelectedIndex (0);
       // }
     }
   }
 
 }
