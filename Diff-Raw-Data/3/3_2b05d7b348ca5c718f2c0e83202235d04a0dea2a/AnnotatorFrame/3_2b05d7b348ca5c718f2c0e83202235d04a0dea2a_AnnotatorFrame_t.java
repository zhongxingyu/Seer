 package eu.kyotoproject.kafannotator;
 
 import eu.kyotoproject.kaf.*;
 import eu.kyotoproject.kafannotator.io.*;
 import eu.kyotoproject.kafannotator.objects.Lexicon;
 import eu.kyotoproject.kafannotator.objects.WordTag;
 import eu.kyotoproject.kafannotator.sensetags.MostCommonSubsumer;
 import eu.kyotoproject.kafannotator.tableware.AnnotationTable;
 import eu.kyotoproject.kafannotator.tableware.AnnotationTableModel;
 import eu.kyotoproject.kafannotator.tableware.CacheData;
 import eu.kyotoproject.kafannotator.tableware.TableSettings;
 import eu.kyotoproject.kafannotator.triple.TagToTriples;
 import eu.kyotoproject.kafannotator.triple.TripleConfig;
 import eu.kyotoproject.kafannotator.util.Colors;
 import eu.kyotoproject.kafannotator.util.Util;
 import vu.tripleevaluation.objects.Triple;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Created by IntelliJ IDEA.
  * User: kyoto
  * Date: Aug 5, 2010
  * Time: 4:51:57 PM
  * To change this template use File | Settings | File Templates.
  * This file is part of KafAnnotator.
 
     KafAnnotator is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     KafAnnotator is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with KafAnnotator.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 
 
 
 public class AnnotatorFrame extends JFrame
 {
     static public AnnotatorFrame instance;
 
     static boolean OPINION = false;
     static boolean SYNSET = false;
     public static String RESOURCESFOLDER = "../resources";
     public static String TripleCONFIGFILE = RESOURCESFOLDER+"/triple.cfg";
     public static String TAGSETFILE = RESOURCESFOLDER+"/tagset.txt";
     public static String LEXICONFILE = RESOURCESFOLDER+"/taglexicon.xml";
     public static String LOCATIONFILE = RESOURCESFOLDER+"/locations.xml";
     public static String THETAGFILE = "";
     public static ArrayList<String> theTag1Set;
     public static ArrayList<String> theTag2Set;
     public static ArrayList<String> theTag3Set;
     public static ArrayList<String> theTag4Set;
     public static ArrayList<String> theTag5Set;
     public static ArrayList<String> theTag6Set;
     public static ArrayList<String> theTag7Set;
     public static ArrayList<String> theTag8Set;
     public static Lexicon tagLexicon;
     public static TableSettings tableSettings;
     static public String inputName;
     ArrayList<WordTag> taggedWordList;
     public KafSaxParser parser;
     ArrayList<CacheData> cache;
     ArrayList<CacheData> cacheBu;
     TripleConfig TripleConfig;
     public String clipboardTag;
     public Integer clipboardTagId;
 
     JMenuBar menuButtons;
 
     JMenu fileMenu;
     JMenuItem openMenuItem;
     JMenuItem readTagFileMenuItem;
     JMenuItem readTagSetMenuItem;
     JMenuItem readLexiconMenuItem;
     JMenu saveAsMenu;
     JMenuItem saveLexiconAsMenuItem;
     JMenuItem saveTagMenuItem;
     JMenuItem saveTagAsMenuItem;
     JMenuItem saveTrainMenuItem;
     JMenuItem convertTagsToTriples;
     JMenuItem outputMostCommonSubsumer;
     JMenuItem quitMenuItem;
 
     JMenu tag1Menu;
     JMenu tag1TokenMenu;
     JMenu tag1LemmaMenu;
     JMenu tag1ConstituentMenu;
     JMenu tag1SentenceMenu;
     JMenuItem newTag1MenuItem;
 
     JMenu tag2Menu;
     JMenu tag2TokenMenu;
     JMenu tag2LemmaMenu;
     JMenu tag2ConstituentMenu;
     JMenu tag2SentenceMenu;
     JMenuItem newTag2MenuItem;
 
     JMenu tag3Menu;
     JMenu tag3TokenMenu;
     JMenu tag3LemmaMenu;
     JMenu tag3ConstituentMenu;
     JMenu tag3SentenceMenu;
     JMenuItem newTag3MenuItem;
 
     JMenu tag4Menu;
     JMenu tag4TokenMenu;
     JMenu tag4LemmaMenu;
     JMenu tag4ConstituentMenu;
     JMenu tag4SentenceMenu;
     JMenuItem newTag4MenuItem;
 
     JMenu tag5Menu;
     JMenu tag5TokenMenu;
     JMenu tag5LemmaMenu;
     JMenu tag5ConstituentMenu;
     JMenu tag5SentenceMenu;
     JMenuItem newTag5MenuItem;
 
     JMenu tag6Menu;
     JMenu tag6TokenMenu;
     JMenu tag6LemmaMenu;
     JMenu tag6ConstituentMenu;
     JMenu tag6SentenceMenu;
     JMenuItem newTag6MenuItem;
 
     JMenu tag7Menu;
     JMenu tag7TokenMenu;
     JMenu tag7LemmaMenu;
     JMenu tag7ConstituentMenu;
     JMenu tag7SentenceMenu;
     JMenuItem newTag7MenuItem;
 
     JMenu tag8Menu;
     JMenu tag8TokenMenu;
     JMenu tag8LemmaMenu;
     JMenu tag8ConstituentMenu;
     JMenu tag8SentenceMenu;
     JMenuItem newTag8MenuItem;
 
     JMenu lexiconMenu;
     JMenuItem updateLexiconMenuItem;
     JMenuItem saveLexiconMenuItem;
 
     JMenu otherMenu;
     JMenuItem confirmMenuItem;
     JMenuItem unconfirmMenuItem;
     JMenuItem undoMenuItem;
     JMenuItem removeAllTagsMenuItem;
     JMenu searchMenu;
     JMenuItem searchWordMenuItem;
     JMenuItem searchTagMenuItem;
     JMenuItem searchLastTagMenuItem;
     JMenuItem searchWordAgainMenuItem;
     JMenuItem searchTagAgainMenuItem;
     JMenuItem searchNotDoneMenuItem;
     String lastTag = "";
     String lastWord = "word";
 
     JPanel contentPanel;
     AnnotationTable table;
 
     JPanel messagePanel;
     JTextArea fullTextField;
     JTextField kafFileField;
     JTextField tagFileField;
     JTextField tagSetFileField;
     JTextField messageField;
     JLabel fullTextLabel;
     JLabel kafFileLabel;
     JLabel tagFileLabel;
     JLabel tagSetFileLabel;
     JLabel messageLabel;
 
 
     static public AnnotatorFrame getInstance(TableSettings settings) {
         if (instance == null) {
             instance = new AnnotatorFrame(settings);
         }
         return instance;
     }
 
     public AnnotatorFrame (TableSettings settings) {
         File resource = new File (RESOURCESFOLDER);
         if (!resource.exists()) {
             resource.mkdir();
         }
         File locations = new File (LOCATIONFILE);
         if (!locations.exists()) {
             try {
                 locations.createNewFile();
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
         tableSettings = settings;
         cache = new ArrayList<CacheData>();
         cacheBu = new ArrayList<CacheData>();
         parser = new KafSaxParser();
         tagLexicon = new Lexicon();
         tagLexicon.parseFile(LEXICONFILE);
         TripleConfig = new TripleConfig(TripleCONFIGFILE);
         theTag1Set = new ArrayList<String>();
         theTag2Set = new ArrayList<String>();
         theTag3Set = new ArrayList<String>();
         theTag4Set = new ArrayList<String>();
         theTag5Set = new ArrayList<String>();
         theTag6Set = new ArrayList<String>();
         theTag7Set = new ArrayList<String>();
         theTag8Set = new ArrayList<String>();
         clipboardTag = "";
         clipboardTagId = -1;
         inputName = "";
 
         /// Message field
         messagePanel = new JPanel();
         messagePanel.setMinimumSize(new Dimension(400, 320));
         messagePanel.setPreferredSize(new Dimension(400, 320));
         messagePanel.setMaximumSize(new Dimension(400, 320));
 
         messageLabel = new JLabel("Messages:");
         messageLabel.setMinimumSize(new Dimension(80, 25));
         messageLabel.setPreferredSize(new Dimension(80, 25));
 
         messageField = new JTextField();
         messageField.setEditable(false);
         messageField.setMinimumSize(new Dimension(300, 25));
         messageField.setPreferredSize(new Dimension(300, 25));
         messageField.setMaximumSize(new Dimension(400, 30));
 
 
         fullTextLabel = new JLabel("Text:");
         fullTextLabel.setMinimumSize(new Dimension(80, 25));
         fullTextLabel.setPreferredSize(new Dimension(80, 25));
         fullTextField = new JTextArea();
         fullTextField.setEditable(false);
         fullTextField.setBackground(Colors.BackGroundColor);
/*
         fullTextField.setMinimumSize(new Dimension(300, 200));
         fullTextField.setPreferredSize(new Dimension(300, 200));
         fullTextField.setMaximumSize(new Dimension(400, 200));
*/
         fullTextField.setLineWrap(true);
         fullTextField.setWrapStyleWord(true);
         fullTextField.addMouseListener(new MouseAdapter() {
             public void mousePressed(MouseEvent e) {
                 switch(e.getModifiers()) {
                     case InputEvent.BUTTON3_MASK: {
                         String word = fullTextField.getSelectedText();
                         table.searchForString(AnnotationTableModel.ROWWORDTOKEN, word);
                     }
                 }
             }
         });
 
 
         JScrollPane scrollableTextArea = new JScrollPane (fullTextField,
                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 
         scrollableTextArea.setMinimumSize(new Dimension(300, 200));
         scrollableTextArea.setPreferredSize(new Dimension(300, 200));
         scrollableTextArea.setMaximumSize(new Dimension(400, 200));
 
 
         kafFileLabel = new JLabel("KAF file:");
         kafFileLabel.setMinimumSize(new Dimension(80, 25));
         kafFileLabel.setPreferredSize(new Dimension(80, 25));
 
         kafFileField = new JTextField();
         kafFileField.setEditable(false);
         kafFileField.setBackground(Colors.BackGroundColor);
         kafFileField.setMinimumSize(new Dimension(300, 25));
         kafFileField.setPreferredSize(new Dimension(300, 25));
         kafFileField.setMaximumSize(new Dimension(400, 30));
 
         tagFileLabel = new JLabel("TAG file:");
         tagFileLabel.setMinimumSize(new Dimension(80, 25));
         tagFileLabel.setPreferredSize(new Dimension(80, 25));
 
         tagFileField = new JTextField();
         tagFileField.setEditable(false);
         tagFileField.setBackground(Colors.BackGroundColor);
         tagFileField.setMinimumSize(new Dimension(300, 25));
         tagFileField.setPreferredSize(new Dimension(300, 25));
         tagFileField.setMaximumSize(new Dimension(400, 30));
 
         tagSetFileLabel = new JLabel("TAG set:");
         tagSetFileLabel.setMinimumSize(new Dimension(80, 25));
         tagSetFileLabel.setPreferredSize(new Dimension(80, 25));
 
         tagSetFileField = new JTextField();
         tagSetFileField.setEditable(false);
         tagSetFileField.setBackground(Colors.BackGroundColor);
         tagSetFileField.setMinimumSize(new Dimension(300, 25));
         tagSetFileField.setPreferredSize(new Dimension(300, 25));
         tagSetFileField.setMaximumSize(new Dimension(400, 30));
 
         messagePanel.setLayout(new GridBagLayout());
         messagePanel.add(fullTextLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0
                 ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(10, 1, 1, 1), 0, 0));
         messagePanel.add(scrollableTextArea, new GridBagConstraints(1, 0, 1, 1, 0.3, 0.3
                 , GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(10, 1, 1, 1), 0, 0));
         messagePanel.add(messageLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0
                 ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(10, 1, 1, 1), 0, 0));
         messagePanel.add(messageField, new GridBagConstraints(1, 1, 1, 1, 0.3, 0.3
                 , GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(10, 1, 1, 1), 0, 0));
         messagePanel.add(kafFileLabel, new GridBagConstraints(0, 2, 1, 1, 0, 0
                 ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
         messagePanel.add(kafFileField, new GridBagConstraints(1, 2, 1, 1, 0.3, 0.3
                 , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
         messagePanel.add(tagFileLabel, new GridBagConstraints(0, 3, 1, 1, 0, 0
                 ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
         messagePanel.add(tagFileField, new GridBagConstraints(1, 3, 1, 1, 0.3, 0.3
                 , GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
         messagePanel.add(tagSetFileLabel, new GridBagConstraints(0, 4, 1, 1, 0, 0
                 ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(1, 1, 1, 1), 0, 0));
         messagePanel.add(tagSetFileField, new GridBagConstraints(1, 4, 1, 1, 0.3, 0.3
                             ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(1, 1, 1, 1), 0, 0));
 
         /// Menus
         menuButtons = new JMenuBar();
         fileMenu = new JMenu("File");
         fileMenu.setMnemonic('f');
 
         ////
         openMenuItem = new JMenuItem("Open KAF file",'o');
         openMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_loadKafFile();
               setCursor(current_cursor);
            }
         });
         fileMenu.add(openMenuItem);
 
         readTagFileMenuItem = new JMenuItem("Load TAG file",'l');
         readTagFileMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_loadTagFile();
               setCursor(current_cursor);
            }
         });
         fileMenu.add(readTagFileMenuItem);
 
         readTagSetMenuItem = new JMenuItem("Load TAG set", 't');
         readTagSetMenuItem.setMnemonic('t');
 
         readTagSetMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_readTagSet();
               setCursor(current_cursor);
            }
         });
         fileMenu.add(readTagSetMenuItem);
 
         readLexiconMenuItem = new JMenuItem("Load lexicon file", 'x');                
         readLexiconMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_readLexicon();
               setCursor(current_cursor);
            }
         });
         fileMenu.add(readLexiconMenuItem);
 
         ////
         saveTagMenuItem = new JMenuItem("Save tagging",'s');
         saveTagMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               int ok = -1;
               ok = DO_saveTagFile();
               DO_saveLexiconFile();
               if (ok==0) {
                   messageField.setText("Tagging data saved");
               }
               else {
                   messageField.setText("Warning! Tagging data NOT saved");
               }
               setCursor(current_cursor);
            }
         });
         fileMenu.add(saveTagMenuItem);
 
 
         ////////////////////////////////////////
 
 
         saveAsMenu = new JMenu("Save as");
         saveAsMenu.setMnemonic('a');
 
         saveTagAsMenuItem = new JMenuItem("Tagging",'t');
         saveTagAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_saveTagFileAs();
               setCursor(current_cursor);
            }
         });
         saveAsMenu.add(saveTagAsMenuItem);
 
         saveLexiconAsMenuItem = new JMenuItem("Lexicon",'l');
         saveLexiconAsMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_saveLexiconFileAs();
               setCursor(current_cursor);
            }
         });
         saveAsMenu.add(saveLexiconAsMenuItem);
 
         fileMenu.add(saveAsMenu);
 
 ////
         saveTrainMenuItem = new JMenuItem("Export tags to train format",'e');
         saveTrainMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_saveTrainFile();
               setCursor(current_cursor);
            }
         });
         fileMenu.add(saveTrainMenuItem);
 
         convertTagsToTriples = new JMenuItem("Export to Triples",'e');
         convertTagsToTriples.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_exportToTriples();
               setCursor(current_cursor);
            }
         });
         fileMenu.add(convertTagsToTriples);
 
         outputMostCommonSubsumer = new JMenuItem("Export wordnet classes",'w');
         outputMostCommonSubsumer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_exportMostCommonSubsumers();
               setCursor(current_cursor);
            }
         });
         fileMenu.add(outputMostCommonSubsumer);
 
         quitMenuItem = new JMenuItem("Quit",'q');
         quitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DO_checkSave()==2) {
                     System.exit(0);
                }
            }
         });
         fileMenu.add(quitMenuItem);
 
         menuButtons.add(fileMenu);
 
 
         ////////////////////////////////////////
 
         searchMenu = new JMenu("Search");
         searchMenu.setMnemonic('s');
         ////
         searchWordMenuItem = new JMenuItem("Word",'w');
         searchWordMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_searchWord();
               setCursor(current_cursor);
            }
         });
         searchMenu.add(searchWordMenuItem);
         ////
         searchLastTagMenuItem = new JMenuItem("Last tag",'l');
         searchLastTagMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_searchLastTag();
               setCursor(current_cursor);
            }
         });
         searchMenu.add(searchLastTagMenuItem);
 
         searchTagMenuItem = new JMenuItem("Tag",'t');
         searchTagMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_searchTag();
               setCursor(current_cursor);
            }
         });
         searchMenu.add(searchTagMenuItem);
 
         searchWordAgainMenuItem = new JMenuItem("Next Word",'n');
         searchWordAgainMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_searchWordAgain();
               setCursor(current_cursor);
            }
         });
         searchMenu.add(searchWordAgainMenuItem);
         ////
         searchTagAgainMenuItem = new JMenuItem("Next Tag",'x');
         searchTagAgainMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_searchTagAgain();
               setCursor(current_cursor);
            }
         });
         searchMenu.add(searchTagAgainMenuItem);
 
         ////
         searchNotDoneMenuItem = new JMenuItem("No Confirm",'c');
         searchNotDoneMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_notDone();
               setCursor(current_cursor);
            }
         });
         searchMenu.add(searchNotDoneMenuItem);
 
         menuButtons.add(searchMenu);
 
 /////////////////////////////////////////////////
         otherMenu = new JMenu("Other");
         otherMenu.setMnemonic('o');
         ////
         confirmMenuItem = new JMenuItem("Do Confirm",'d');
         confirmMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Cursor current_cursor = getCursor();
                 setCursor(new Cursor(Cursor.WAIT_CURSOR));
                 DO_Selected();
                 setCursor(current_cursor);
             }
         });
         otherMenu.add(confirmMenuItem);
         ////
         unconfirmMenuItem = new JMenuItem("Undo Confirm",'u');
         unconfirmMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_Unselected();
               setCursor(current_cursor);
            }
         });
         otherMenu.add(unconfirmMenuItem);
 
         removeAllTagsMenuItem = new JMenuItem("Remove all annotations",'r');
         removeAllTagsMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Cursor current_cursor = getCursor();
                 setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                 DO_unTagAllTokens();
                 setCursor(current_cursor);
             }
         });
         otherMenu.add(removeAllTagsMenuItem);
 
         undoMenuItem = new JMenuItem("Undo",'u');
        // undoMenuItem.setMaximumSize(new Dimension(70, 150));
         //  undoMenuItem.setBorder(BorderFactory.createLineBorder(Color.gray));
         undoMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 Cursor current_cursor = getCursor();
                 setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                 DO_undo();
                 setCursor(current_cursor);
             }
         });
         otherMenu.add(undoMenuItem);
 
         menuButtons.add(otherMenu);
 ////////////////////////////////////////////////////////
 
         ////
 
         /////////////////////
 
         tag1Menu = new JMenu("Tag level 1");
         tag1Menu.setMnemonic('1');
 
         tag1TokenMenu = new JMenu("Tag tokens");
         tag1TokenMenu.setMnemonic('t');
         tag1Menu.add(tag1TokenMenu);
  
         tag1LemmaMenu = new JMenu("Tag types");
         tag1LemmaMenu.setMnemonic('y');
         tag1Menu.add(tag1LemmaMenu);
 
         tag1ConstituentMenu = new JMenu("Tag constituent");
         tag1ConstituentMenu.setMnemonic('c');
         tag1Menu.add(tag1ConstituentMenu);
 
         tag1SentenceMenu = new JMenu("Tag sentence");
         tag1SentenceMenu.setMnemonic('s');
         tag1Menu.add(tag1SentenceMenu);
        // popup.add(tag1Menu);              // just trying
 
         JMenuItem untagItem = new JMenuItem ("UNTAG", 'u');
         untagItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_unTagTokens(1);
               setCursor(current_cursor);
            }
         });
         tag1Menu.add(untagItem);
 
         JMenuItem mergeTagIdsItem = new JMenuItem ("Merge Tag Ids", 'm');
         mergeTagIdsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_mergeTagIds(1);
               setCursor(current_cursor);
            }
         });
         tag1Menu.add(mergeTagIdsItem);
 
         JMenuItem separateTagIdsItem = new JMenuItem ("Separate Tag Ids", 's');
         separateTagIdsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_separateTagIds(1);
               setCursor(current_cursor);
            }
         });
         tag1Menu.add(separateTagIdsItem);
 
         JMenuItem editTagIdsItem = new JMenuItem ("Edit Tag Id", 'e');
         editTagIdsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_editTagIds(1);
               setCursor(current_cursor);
            }
         });
         tag1Menu.add(editTagIdsItem);
 
         JMenuItem copyTagItem = new JMenuItem ("Copy Tag & Id", 'c');
         copyTagItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_copyTag(1);
               setCursor(current_cursor);
            }
         });
         tag1Menu.add(copyTagItem);
 
         JMenuItem pastTagItem = new JMenuItem ("Past Tag & Id", 'p');
         pastTagItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_pastTag(1);
               setCursor(current_cursor);
            }
         });
         tag1Menu.add(pastTagItem);
 
         JMenuItem selectLexTagMenuItem = new JMenuItem("Dominant Tag",'l');
         selectLexTagMenuItem.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                    Cursor current_cursor = getCursor();
                    setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                    DO_selectLexicalTag();
                    setCursor(current_cursor);
                 }
         });
         tag1Menu.add(selectLexTagMenuItem);
 
         newTag1MenuItem = new JMenuItem("Add Tag",'a');
         newTag1MenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_makeNewTag1();
               setCursor(current_cursor);
            }
         });
         tag1Menu.add(newTag1MenuItem);
 
         menuButtons.add(tag1Menu);
 
         //////////////////////////////////
 
         tag2Menu = new JMenu("Tag level 2");
         tag2Menu.setMnemonic('2');
 
         tag2TokenMenu = new JMenu("Tag tokens");
         tag2TokenMenu.setMnemonic('t');
         tag2Menu.add(tag2TokenMenu);
 
         tag2LemmaMenu = new JMenu("Tag types");
         tag2LemmaMenu.setMnemonic('y');
         tag2Menu.add(tag2LemmaMenu);
 
         tag2ConstituentMenu = new JMenu("Tag constituent");
         tag2ConstituentMenu.setMnemonic('c');
         tag2Menu.add(tag2ConstituentMenu);
 
         tag2SentenceMenu = new JMenu("Tag sentence");
         tag2SentenceMenu.setMnemonic('s');
         tag2Menu.add(tag2SentenceMenu);
 
         JMenuItem untagItem2 = new JMenuItem ("UNTAG", 'u');
         untagItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_unTagTokens(2);
               setCursor(current_cursor);
            }
         });
         tag2Menu.add(untagItem2);
 
         JMenuItem mergeTagIdsItem2 = new JMenuItem ("Merge Tag Ids", 'm');
         mergeTagIdsItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_mergeTagIds(2);
               setCursor(current_cursor);
            }
         });
         tag2Menu.add(mergeTagIdsItem2);
 
         JMenuItem separateTagIdsItem2 = new JMenuItem ("Separate Tag Ids", 's');
         separateTagIdsItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_separateTagIds(2);
               setCursor(current_cursor);
            }
         });
         tag2Menu.add(separateTagIdsItem2);
 
         JMenuItem editTagIdsItem2 = new JMenuItem ("Edit Tag Id", 'e');
         editTagIdsItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_editTagIds(2);
               setCursor(current_cursor);
            }
         });
         tag2Menu.add(editTagIdsItem2);
 
         JMenuItem copyTagItem2 = new JMenuItem ("Copy Tag & Id", 'c');
         copyTagItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_copyTag(2);
               setCursor(current_cursor);
            }
         });
         tag2Menu.add(copyTagItem2);
 
         JMenuItem pastTagItem2 = new JMenuItem ("Past Tag & Id", 'p');
         pastTagItem2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_pastTag(2);
               setCursor(current_cursor);
            }
         });
         tag2Menu.add(pastTagItem2);
 
         newTag2MenuItem = new JMenuItem("Add Tag",'a');
         newTag2MenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_makeNewTag2();
               setCursor(current_cursor);
            }
         });
         tag2Menu.add(newTag2MenuItem);
 
         if (!tableSettings.hideTag2) {
             menuButtons.add(tag2Menu);
         }
 
         /////////////////#################
 
         tag3Menu = new JMenu("Tag level 3");
         tag3Menu.setMnemonic('3');
 
         tag3TokenMenu = new JMenu("Tag tokens");
         tag3Menu.add(tag3TokenMenu);
 
         tag3LemmaMenu = new JMenu("Tag types");
         tag3LemmaMenu.setMnemonic('y');
         tag3Menu.add(tag3LemmaMenu);
 
         tag3ConstituentMenu = new JMenu("Tag constituent");
         tag3ConstituentMenu.setMnemonic('c');
         tag3Menu.add(tag3ConstituentMenu);
 
         tag3SentenceMenu = new JMenu("Tag sentence");
         tag3SentenceMenu.setMnemonic('s');
         tag3Menu.add(tag3SentenceMenu);
 
         JMenuItem untagItem3 = new JMenuItem ("UNTAG", 'u');
         untagItem3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_unTagTokens(3);
               setCursor(current_cursor);
            }
         });
         tag3Menu.add(untagItem3);
 
         JMenuItem mergeTagIdsItem3 = new JMenuItem ("Merge Tag Ids", 'm');
         mergeTagIdsItem3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_mergeTagIds(3);
               setCursor(current_cursor);
            }
         });
         tag3Menu.add(mergeTagIdsItem3);
 
         JMenuItem separateTagIdsItem3 = new JMenuItem ("Separate Tag Ids", 's');
         separateTagIdsItem3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_separateTagIds(3);
               setCursor(current_cursor);
            }
         });
         tag3Menu.add(separateTagIdsItem3);
 
         JMenuItem editTagIdsItem3 = new JMenuItem ("Edit Tag Id", 'e');
         editTagIdsItem3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_editTagIds(3);
               setCursor(current_cursor);
            }
         });
         tag3Menu.add(editTagIdsItem3);
 
         JMenuItem copyTagItem3 = new JMenuItem ("Copy Tag & Id", 'c');
         copyTagItem3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_copyTag(3);
               setCursor(current_cursor);
            }
         });
         tag3Menu.add(copyTagItem3);
 
         JMenuItem pastTagItem3 = new JMenuItem ("Past Tag & Id", 'p');
         pastTagItem3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_pastTag(3);
               setCursor(current_cursor);
            }
         });
         tag3Menu.add(pastTagItem3);
 
         newTag3MenuItem = new JMenuItem("Add Tag",'a');
         newTag3MenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_makeNewTag3();
               setCursor(current_cursor);
            }
         });
         tag3Menu.add(newTag3MenuItem);
 
         if (!tableSettings.hideTag3){
             menuButtons.add(tag3Menu);
         }
 
         /////////////////////
         tag4Menu = new JMenu("Tag level 4");
         tag4Menu.setMnemonic('4');
 
         tag4TokenMenu = new JMenu("Tag tokens");
         tag4Menu.add(tag4TokenMenu);
 
         tag4LemmaMenu = new JMenu("Tag types");
         tag4LemmaMenu.setMnemonic('y');
         tag4Menu.add(tag4LemmaMenu);
 
         tag4ConstituentMenu = new JMenu("Tag constituent");
         tag4ConstituentMenu.setMnemonic('c');
         tag4Menu.add(tag4ConstituentMenu);
 
         tag4SentenceMenu = new JMenu("Tag sentence");
         tag4SentenceMenu.setMnemonic('s');
         tag4Menu.add(tag4SentenceMenu);
 
         JMenuItem untagItem4 = new JMenuItem ("UNTAG", 'u');
         untagItem4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_unTagTokens(4);
               setCursor(current_cursor);
            }
         });
         tag4Menu.add(untagItem4);
 
         JMenuItem mergeTagIdsItem4 = new JMenuItem ("Merge Tag Ids", 'm');
         mergeTagIdsItem4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_mergeTagIds(4);
               setCursor(current_cursor);
            }
         });
         tag4Menu.add(mergeTagIdsItem4);
 
         JMenuItem separateTagIdsItem4 = new JMenuItem ("Separate Tag Ids", 's');
         separateTagIdsItem4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_separateTagIds(4);
               setCursor(current_cursor);
            }
         });
         tag4Menu.add(separateTagIdsItem4);
 
         JMenuItem editTagIdsItem4 = new JMenuItem ("Edit Tag Id", 'e');
         editTagIdsItem4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_editTagIds(4);
               setCursor(current_cursor);
            }
         });
         tag4Menu.add(editTagIdsItem4);
 
         JMenuItem copyTagItem4 = new JMenuItem ("Copy Tag & Id", 'c');
         copyTagItem4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_copyTag(4);
               setCursor(current_cursor);
            }
         });
         tag4Menu.add(copyTagItem4);
 
         JMenuItem pastTagItem4 = new JMenuItem ("Past Tag & Id", 'p');
         pastTagItem4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_pastTag(4);
               setCursor(current_cursor);
            }
         });
         tag4Menu.add(pastTagItem4);
 
         newTag4MenuItem = new JMenuItem("Add Tag",'a');
         newTag4MenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_makeNewTag4();
               setCursor(current_cursor);
            }
         });
         tag4Menu.add(newTag4MenuItem);
 
         if (!tableSettings.hideTag4) {
             menuButtons.add(tag4Menu);
         }
 
         /////////////////#################
 
         tag5Menu = new JMenu("Tag level 5");
         tag5Menu.setMnemonic('5');
 
         tag5TokenMenu = new JMenu("Tag tokens");
         tag5Menu.add(tag5TokenMenu);
 
         tag5LemmaMenu = new JMenu("Tag types");
         tag5LemmaMenu.setMnemonic('y');
         tag5Menu.add(tag5LemmaMenu);
 
         tag5ConstituentMenu = new JMenu("Tag constituent");
         tag5ConstituentMenu.setMnemonic('c');
         tag5Menu.add(tag5ConstituentMenu);
 
         tag5SentenceMenu = new JMenu("Tag sentence");
         tag5SentenceMenu.setMnemonic('s');
         tag5Menu.add(tag5SentenceMenu);
 
         JMenuItem untagItem5 = new JMenuItem ("UNTAG", 'u');
         untagItem5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_unTagTokens(5);
               setCursor(current_cursor);
            }
         });
         tag5Menu.add(untagItem5);
 
         JMenuItem mergeTagIdsItem5 = new JMenuItem ("Merge Tag Ids", 'm');
         mergeTagIdsItem5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_mergeTagIds(5);
               setCursor(current_cursor);
            }
         });
         tag5Menu.add(mergeTagIdsItem5);
 
         JMenuItem separateTagIdsItem5 = new JMenuItem ("Separate Tag Ids", 's');
         separateTagIdsItem5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_separateTagIds(5);
               setCursor(current_cursor);
            }
         });
         tag5Menu.add(separateTagIdsItem5);
 
         JMenuItem editTagIdsItem5 = new JMenuItem ("Edit Tag Id", 'e');
         editTagIdsItem5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_editTagIds(5);
               setCursor(current_cursor);
            }
         });
         tag5Menu.add(editTagIdsItem5);
 
         JMenuItem copyTagItem5 = new JMenuItem ("Copy Tag & Id", 'c');
         copyTagItem5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_copyTag(5);
               setCursor(current_cursor);
            }
         });
         tag5Menu.add(copyTagItem5);
 
         JMenuItem pastTagItem5 = new JMenuItem ("Past Tag & Id", 'p');
         pastTagItem5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_pastTag(5);
               setCursor(current_cursor);
            }
         });
         tag5Menu.add(pastTagItem5);
 
         newTag5MenuItem = new JMenuItem("Add Tag",'a');
         newTag5MenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_makeNewTag5();
               setCursor(current_cursor);
            }
         });
         tag5Menu.add(newTag5MenuItem);
 
         if (!tableSettings.hideTag5) {
             menuButtons.add(tag5Menu);
         }
 
         /////////////////#################
 
         tag6Menu = new JMenu("Tag level 6");
         tag6Menu.setMnemonic('6');
 
         tag6TokenMenu = new JMenu("Tag tokens");
         tag6Menu.add(tag6TokenMenu);
 
         tag6LemmaMenu = new JMenu("Tag types");
         tag6LemmaMenu.setMnemonic('y');
         tag6Menu.add(tag6LemmaMenu);
 
         tag6ConstituentMenu = new JMenu("Tag constituent");
         tag6ConstituentMenu.setMnemonic('c');
         tag6Menu.add(tag6ConstituentMenu);
 
         tag6SentenceMenu = new JMenu("Tag sentence");
         tag6SentenceMenu.setMnemonic('s');
         tag6Menu.add(tag6SentenceMenu);
 
         JMenuItem untagItem6 = new JMenuItem ("UNTAG", 'u');
         untagItem6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_unTagTokens(6);
               setCursor(current_cursor);
            }
         });
         tag6Menu.add(untagItem6);
 
         JMenuItem mergeTagIdsItem6 = new JMenuItem ("Merge Tag Ids", 'm');
         mergeTagIdsItem6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_mergeTagIds(6);
               setCursor(current_cursor);
            }
         });
         tag6Menu.add(mergeTagIdsItem6);
 
         JMenuItem separateTagIdsItem6 = new JMenuItem ("Separate Tag Ids", 's');
         separateTagIdsItem6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_separateTagIds(6);
               setCursor(current_cursor);
            }
         });
         tag6Menu.add(separateTagIdsItem6);
 
         JMenuItem editTagIdsItem6 = new JMenuItem ("Edit Tag Id", 'e');
         editTagIdsItem6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_editTagIds(6);
               setCursor(current_cursor);
            }
         });
         tag6Menu.add(editTagIdsItem6);
 
         JMenuItem copyTagItem6 = new JMenuItem ("Copy Tag & Id", 'c');
         copyTagItem6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_copyTag(6);
               setCursor(current_cursor);
            }
         });
         tag6Menu.add(copyTagItem6);
 
         JMenuItem pastTagItem6 = new JMenuItem ("Past Tag & Id", 'p');
         pastTagItem6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_pastTag(6);
               setCursor(current_cursor);
            }
         });
         tag6Menu.add(pastTagItem6);
 
         newTag6MenuItem = new JMenuItem("Add Tag",'a');
         newTag6MenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_makeNewTag6();
               setCursor(current_cursor);
            }
         });
         tag6Menu.add(newTag6MenuItem);
 
         if (!tableSettings.hideTag6) {
             menuButtons.add(tag6Menu);
         }
 
         /////////////////#################
 
         tag7Menu = new JMenu("Tag level 7");
         tag7Menu.setMnemonic('7');
 
         tag7TokenMenu = new JMenu("Tag tokens");
         tag7Menu.add(tag7TokenMenu);
 
         tag7LemmaMenu = new JMenu("Tag types");
         tag7LemmaMenu.setMnemonic('y');
         tag7Menu.add(tag7LemmaMenu);
 
         tag7ConstituentMenu = new JMenu("Tag constituent");
         tag7ConstituentMenu.setMnemonic('c');
         tag7Menu.add(tag7ConstituentMenu);
 
         tag7SentenceMenu = new JMenu("Tag sentence");
         tag7SentenceMenu.setMnemonic('s');
         tag7Menu.add(tag7SentenceMenu);
 
         JMenuItem untagItem7 = new JMenuItem ("UNTAG", 'u');
         untagItem6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_unTagTokens(7);
               setCursor(current_cursor);
            }
         });
         tag7Menu.add(untagItem7);
 
         JMenuItem mergeTagIdsItem7 = new JMenuItem ("Merge Tag Ids", 'm');
         mergeTagIdsItem6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_mergeTagIds(7);
               setCursor(current_cursor);
            }
         });
         tag7Menu.add(mergeTagIdsItem7);
 
         JMenuItem separateTagIdsItem7 = new JMenuItem ("Separate Tag Ids", 's');
         separateTagIdsItem6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_separateTagIds(7);
               setCursor(current_cursor);
            }
         });
         tag7Menu.add(separateTagIdsItem7);
 
         JMenuItem editTagIdsItem7 = new JMenuItem ("Edit Tag Id", 'e');
         editTagIdsItem7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_editTagIds(7);
               setCursor(current_cursor);
            }
         });
         tag7Menu.add(editTagIdsItem7);
 
         JMenuItem copyTagItem7 = new JMenuItem ("Copy Tag & Id", 'c');
         copyTagItem7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_copyTag(7);
               setCursor(current_cursor);
            }
         });
         tag7Menu.add(copyTagItem7);
 
         JMenuItem pastTagItem7 = new JMenuItem ("Past Tag & Id", 'p');
         pastTagItem7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_pastTag(7);
               setCursor(current_cursor);
            }
         });
         tag7Menu.add(pastTagItem7);
 
         newTag7MenuItem = new JMenuItem("Add Tag",'a');
         newTag7MenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_makeNewTag7();
               setCursor(current_cursor);
            }
         });
         tag7Menu.add(newTag7MenuItem);
 
         if (!tableSettings.hideTag7) {
             menuButtons.add(tag7Menu);
         }
 
         /////////////////#################
 
         tag8Menu = new JMenu("Tag level 8");
         tag8Menu.setMnemonic('8');
 
         tag8TokenMenu = new JMenu("Tag tokens");
         tag8Menu.add(tag8TokenMenu);
 
         tag8LemmaMenu = new JMenu("Tag types");
         tag8LemmaMenu.setMnemonic('y');
         tag8Menu.add(tag8LemmaMenu);
 
         tag8ConstituentMenu = new JMenu("Tag constituent");
         tag8ConstituentMenu.setMnemonic('c');
         tag8Menu.add(tag8ConstituentMenu);
 
         tag8SentenceMenu = new JMenu("Tag sentence");
         tag8SentenceMenu.setMnemonic('s');
         tag8Menu.add(tag8SentenceMenu);
 
         JMenuItem untagItem8 = new JMenuItem ("UNTAG", 'u');
         untagItem8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_unTagTokens(8);
               setCursor(current_cursor);
            }
         });
         tag8Menu.add(untagItem8);
 
         JMenuItem mergeTagIdsItem8 = new JMenuItem ("Merge Tag Ids", 'm');
         mergeTagIdsItem8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_mergeTagIds(8);
               setCursor(current_cursor);
            }
         });
         tag8Menu.add(mergeTagIdsItem8);
 
         JMenuItem separateTagIdsItem8 = new JMenuItem ("Separate Tag Ids", 's');
         separateTagIdsItem8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_separateTagIds(8);
               setCursor(current_cursor);
            }
         });
         tag8Menu.add(separateTagIdsItem8);
 
         JMenuItem editTagIdsItem8 = new JMenuItem ("Edit Tag Id", 'e');
         editTagIdsItem8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_editTagIds(8);
               setCursor(current_cursor);
            }
         });
         tag8Menu.add(editTagIdsItem8);
 
         JMenuItem copyTagItem8 = new JMenuItem ("Copy Tag & Id", 'c');
         copyTagItem8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_copyTag(8);
               setCursor(current_cursor);
            }
         });
         tag8Menu.add(copyTagItem8);
 
         JMenuItem pastTagItem8 = new JMenuItem ("Past Tag & Id", 'p');
         pastTagItem8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_pastTag(8);
               setCursor(current_cursor);
            }
         });
         tag8Menu.add(pastTagItem8);
 
         newTag8MenuItem = new JMenuItem("Add Tag",'a');
         newTag8MenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Cursor current_cursor = getCursor();
               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
               DO_makeNewTag8();
               setCursor(current_cursor);
            }
         });
         tag8Menu.add(newTag8MenuItem);
 
         if (!tableSettings.hideTag8) {
             menuButtons.add(tag8Menu);
         }
 
 
         /// Text Area
 
         table = new AnnotationTable(tableSettings);
 
         ///////////////////////
         contentPanel = new JPanel();
         setContentPane(contentPanel);
         contentPanel.setLayout(new GridBagLayout());
         contentPanel.add(menuButtons, new GridBagConstraints(0, 0, 1, 1, 0, 0
                 ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 15, 15));
         contentPanel.add(table, new GridBagConstraints(0, 1, 1, 1, 0.3, 0.3
                             ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
         contentPanel.add(messagePanel, new GridBagConstraints(0, 3, 1, 1, 0.3, 0.3
                             ,GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(10, 10, 10, 10), 0, 0));
     }
 
     JMenu getMenu(String c, JMenu p) {
         for (int i = 0; i < p.getMenuComponentCount(); i++) {
             Component comp = p.getMenuComponent(i);
           //  System.out.println("comp.getClass() = " + comp.getClass().getSimpleName());
             if (comp.getClass().getSimpleName().equals("JMenu")) {
                 JMenu pItem = (JMenu) comp;
                // System.out.println("pItem.getText() = " + pItem.getText());
                 if (pItem.getText().equals(c)) {
                     return pItem;
                 }
             }
 
         }
         JMenu item = new JMenu (c);
         item.setMnemonic(c.charAt(0));
         p.add(item);
         return item;
     }
 
     void addTagsAsTokenMenu (JMenu jmenu, ArrayList<String> tagSet, final int level) {
         jmenu.removeAll();
         for (int i = 0; i < tagSet.size(); i++) {
             String menu = tagSet.get(i);
             String [] fields = menu.split("#");
             if (fields.length>1) {
                 JMenu tagGroupItem = new JMenu();
                 for (int j = 0; j < fields.length; j++) {
                     String tagString = fields[j].trim();
                     if (j==0) {
                         //// first level added to jmenu
                         tagGroupItem = getMenu(tagString, jmenu);
                     }
                     else if (j<fields.length-1) {
                         //// intermediate level added to previous
                         tagGroupItem = getMenu(tagString, tagGroupItem);                                                
                     }
                     else {
                         //// final level becomes a JMenuItem
                         final String s = fields[j].trim();
                         JMenuItem tagMenuItem = new JMenuItem (s, s.charAt(0));
                         tagMenuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                               Cursor current_cursor = getCursor();
                               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                               DO_applyTagToTokens(s, level);
                               setCursor(current_cursor);
                            }
                         });
                         tagGroupItem.add(tagMenuItem);
                     }
                 }
             }
             else {
                 final String s = menu;
                 JMenuItem tagItem = new JMenuItem (s, s.charAt(0));
                 tagItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                       Cursor current_cursor = getCursor();
                       setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                       DO_applyTagToTokens(s, level);
                       setCursor(current_cursor);
                    }
                 });
                 jmenu.add(tagItem);
             }
         }
 
     }
     void addTagsAsTypeMenu (JMenu jmenu, ArrayList<String> tagSet, final int level) {
         jmenu.removeAll();
         for (int i = 0; i < tagSet.size(); i++) {
             String menu = tagSet.get(i);
             String [] fields = menu.split("#");
             if (fields.length>1) {
                 JMenu tagGroupItem = new JMenu();
                 for (int j = 0; j < fields.length; j++) {
                     String tagString = fields[j].trim();
                     if (j==0) {
                         //// first level added to jmenu
                         tagGroupItem = getMenu(tagString, jmenu);
                     }
                     else if (j<fields.length-1) {
                         //// intermediate level added to previous
                         tagGroupItem = getMenu(tagString, tagGroupItem);
                     }
                     else {
                         //// final level becomes a JMenuItem
                         final String s = fields[j].trim();
                         JMenuItem tagMenuItem = new JMenuItem (s, s.charAt(0));
                         tagMenuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                               Cursor current_cursor = getCursor();
                               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                               DO_applyTagToTypes(s, level);
                               setCursor(current_cursor);
                            }
                         });
                         tagGroupItem.add(tagMenuItem);
                     }
                 }
             }
             else {
                 final String s = menu;
                 JMenuItem tagItem = new JMenuItem (s, s.charAt(0));
                 tagItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                       Cursor current_cursor = getCursor();
                       setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                       DO_applyTagToTypes(s, level);
                       setCursor(current_cursor);
                    }
                 });
                 jmenu.add(tagItem);
             }
         }
 
     }
     void addTagsAsConstituentMenu (JMenu jmenu, ArrayList<String> tagSet, final int level) {
         jmenu.removeAll();
         for (int i = 0; i < tagSet.size(); i++) {
             String menu = tagSet.get(i);
             String [] fields = menu.split("#");
             if (fields.length>1) {
                 JMenu tagGroupItem = new JMenu();
                 for (int j = 0; j < fields.length; j++) {
                     String tagString = fields[j].trim();
                     if (j==0) {
                         //// first level added to jmenu
                         tagGroupItem = getMenu(tagString, jmenu);
                     }
                     else if (j<fields.length-1) {
                         //// intermediate level added to previous
                         tagGroupItem = getMenu(tagString, tagGroupItem);
                     }
                     else {
                         //// final level becomes a JMenuItem
                         final String s = fields[j].trim();
                         JMenuItem tagMenuItem = new JMenuItem (s, s.charAt(0));
                         tagMenuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                               Cursor current_cursor = getCursor();
                               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                               DO_applyTagToConstituents(s, level);
                               setCursor(current_cursor);
                            }
                         });
                         tagGroupItem.add(tagMenuItem);
                     }
                 }
             }
             else {
                 final String s = menu;
                 JMenuItem tagItem = new JMenuItem (s, s.charAt(0));
                 tagItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                       Cursor current_cursor = getCursor();
                       setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                       DO_applyTagToConstituents(s, level);
                       setCursor(current_cursor);
                    }
                 });
                 jmenu.add(tagItem);
             }
         }
 
     }
     void addTagsAsSentenceMenu (JMenu jmenu, ArrayList<String> tagSet, final int level) {
         jmenu.removeAll();
         for (int i = 0; i < tagSet.size(); i++) {
             String menu = tagSet.get(i);
             String [] fields = menu.split("#");
             if (fields.length>1) {
                 JMenu tagGroupItem = new JMenu();
                 for (int j = 0; j < fields.length; j++) {
                     String tagString = fields[j].trim();
                     if (j==0) {
                         //// first level added to jmenu
                         tagGroupItem = getMenu(tagString, jmenu);
                     }
                     else if (j<fields.length-1) {
                         //// intermediate level added to previous
                         tagGroupItem = getMenu(tagString, tagGroupItem);
                     }
                     else {
                         //// final level becomes a JMenuItem
                         final String s = fields[j].trim();
                         JMenuItem tagMenuItem = new JMenuItem (s, s.charAt(0));
                         tagMenuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                               Cursor current_cursor = getCursor();
                               setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                               DO_applyTagToSentences(s, level);
                               setCursor(current_cursor);
                            }
                         });
                         tagGroupItem.add(tagMenuItem);
                     }
                 }
             }
             else {
                 final String s = menu;
                 JMenuItem tagItem = new JMenuItem (s, s.charAt(0));
                 tagItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                       Cursor current_cursor = getCursor();
                       setCursor(new Cursor ( Cursor.WAIT_CURSOR ));
                       DO_applyTagToSentences(s, level);
                       setCursor(current_cursor);
                    }
                 });
                 jmenu.add(tagItem);
             }
         }
 
     }
 
     void makeTagSet1Menus () {
         addTagsAsTokenMenu(tag1TokenMenu,theTag1Set, 1);
         addTagsAsTypeMenu(tag1LemmaMenu,theTag1Set, 1);
         addTagsAsConstituentMenu(tag1ConstituentMenu,theTag1Set, 1);
         addTagsAsSentenceMenu(tag1SentenceMenu,theTag1Set, 1);
     }
 
     void makeTagSet2Menus () {
         addTagsAsTokenMenu(tag2TokenMenu,theTag2Set, 2);
         addTagsAsTypeMenu(tag2LemmaMenu,theTag2Set, 2);
         addTagsAsConstituentMenu(tag2ConstituentMenu,theTag2Set, 2);
         addTagsAsSentenceMenu(tag2SentenceMenu,theTag2Set, 2);
     }
 
     void makeTagSet3Menus () {
         addTagsAsTokenMenu(tag3TokenMenu,theTag3Set, 3);
         addTagsAsTypeMenu(tag3LemmaMenu,theTag3Set, 3);
         addTagsAsConstituentMenu(tag3ConstituentMenu,theTag3Set, 3);
         addTagsAsSentenceMenu(tag3SentenceMenu,theTag3Set, 3);
     }
 
     void makeTagSet4Menus () {
         addTagsAsTokenMenu(tag4TokenMenu,theTag4Set, 4);
         addTagsAsTypeMenu(tag4LemmaMenu,theTag4Set, 4);
         addTagsAsConstituentMenu(tag4ConstituentMenu,theTag4Set, 4);
         addTagsAsSentenceMenu(tag4SentenceMenu,theTag4Set, 4);
     }
 
     void makeTagSet5Menus () {
         addTagsAsTokenMenu(tag5TokenMenu,theTag5Set, 5);
         addTagsAsTypeMenu(tag5LemmaMenu,theTag5Set, 5);
         addTagsAsConstituentMenu(tag5ConstituentMenu,theTag5Set, 5);
         addTagsAsSentenceMenu(tag5SentenceMenu,theTag5Set, 5);
     }
 
     void makeTagSet6Menus () {
         addTagsAsTokenMenu(tag6TokenMenu,theTag6Set, 6);
         addTagsAsTypeMenu(tag6LemmaMenu,theTag6Set, 6);
         addTagsAsConstituentMenu(tag6ConstituentMenu,theTag6Set, 6);
         addTagsAsSentenceMenu(tag6SentenceMenu,theTag6Set, 6);
     }
 
     void makeTagSet7Menus () {
         addTagsAsTokenMenu(tag7TokenMenu,theTag7Set, 7);
         addTagsAsTypeMenu(tag7LemmaMenu,theTag7Set, 7);
         addTagsAsConstituentMenu(tag7ConstituentMenu,theTag7Set, 7);
         addTagsAsSentenceMenu(tag7SentenceMenu,theTag7Set, 7);
     }
 
     void makeTagSet8Menus () {
         addTagsAsTokenMenu(tag8TokenMenu,theTag8Set, 8);
         addTagsAsTypeMenu(tag8LemmaMenu,theTag8Set, 8);
         addTagsAsConstituentMenu(tag8ConstituentMenu,theTag8Set, 8);
         addTagsAsSentenceMenu(tag8SentenceMenu,theTag8Set, 8);
     }
 
     void DO_loadKafFile() {
             String kafFile = selectInputFileDialog(this, ".kaf");
             DO_loadKafFile(kafFile);
     }
 
     public void DO_loadKafFile(String inputFile) {
             messageField.setText("");
             contentPanel.remove(table);
             inputName = inputFile;
             if (inputName!=null && inputName.length()>0) {
                 kafFileField.setText(inputName);
                 THETAGFILE = "";
                 //// WE ASSUME THIS IS A KAF FILE
                 parser.parseFile(inputName);
                 fullTextField.setText(parser.getFullText());
                 taggedWordList= new ArrayList<WordTag>();
                 for (int i = 0; i < parser.kafWordFormList.size(); i++) {
                     KafWordForm nextWord =  parser.kafWordFormList.get(i);
                     WordTag wordTag = null;
                     KafTerm kafTerm = null;
                     if (parser.WordFormToTerm.containsKey(nextWord.getWid())) {
                         String termId = parser.WordFormToTerm.get(nextWord.getWid());
                         //System.out.println("termId = " + termId);
                         kafTerm = parser.getTerm(termId);
                         //System.out.println("kafTerm = " + kafTerm.getTid());
                     }
                     if (kafTerm != null) {
                         String synset = "";
                         if (SYNSET) {
                             if (kafTerm.getSenseTags().size()>=1) {
                                 KafSense sense = kafTerm.getSenseTags().get(0);
                                 synset =  sense.getSensecode()+":"+sense.getConfidence();
                             }
                         }
                         else if (OPINION) {
                             ArrayList<String> opinions = parser.TermToOpinions.get(kafTerm.getTid());
 
                             if (opinions!=null && opinions.size()>0) {
                                 for (int j = 0; j < opinions.size(); j++) {
                                     String oid = opinions.get(j);
                                     KafOpinion kafOpinion = parser.getOpinion(oid);
                                     for (int k = 0; k < kafOpinion.getSpansOpinionTarget().size(); k++) {
                                         String termId = kafOpinion.getSpansOpinionTarget().get(k);
                                         if (termId.equals(kafTerm.getTid())) {
                                             synset = oid+":O-TARGET";
                                             break;
                                         }
                                     }
                                     for (int k = 0; k < kafOpinion.getSpansOpinionHolder().size(); k++) {
                                         String termId = kafOpinion.getSpansOpinionHolder().get(k);
                                         if (termId.equals(kafTerm.getTid())) {
                                             synset = oid+":O-HOLDER";
                                             break;
                                         }
                                     }
                                     for (int k = 0; k < kafOpinion.getSpansOpinionExpression().size(); k++) {
                                         String termId = kafOpinion.getSpansOpinionExpression().get(k);
                                         if (termId.equals(kafTerm.getTid())) {
                                             synset = oid+":O-"+kafOpinion.getOpinionSentiment().getPolarity();
                                             break;
                                         }
                                     }
                                 }
                             }
                         }
                         Integer sentenceId = Util.makeInt(nextWord.getSent());
                         wordTag = new WordTag(sentenceId, nextWord.getWid(), nextWord.getWf(), kafTerm.getLemma(), kafTerm.getPos(), synset, i);
                     }
                     else {
                         wordTag = new WordTag(0, nextWord.getWid(),nextWord.getWf(),"","","",i);
                         wordTag.setMark(true);
                     }
                     if (wordTag!=null) {
                         taggedWordList.add(wordTag);
                     }
                 }
                 table = new AnnotationTable(taggedWordList, tableSettings);
                 table.setBackground();
                 contentPanel.add(table, new GridBagConstraints(0, 1, 1, 1, 0.9, 0.9
                                         ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
      //           table.theTable.printDebugData();
 //                table.setColumnSize();
                 contentPanel.repaint();
                 contentPanel.setVisible(true);
                 setContentPane(contentPanel);
                 this.repaint();
                 messageField.setText("Loaded from:"+inputName+". Nr of wordtokens="+taggedWordList.size());
                 table.READFILE = false;
                 int idx = inputName.lastIndexOf(".");
                 String tagFileForKafFile = inputName;
                 if (idx>-1) {
                     tagFileForKafFile = inputName.substring(0, idx)+".tag";
                 }
                 else {
                     tagFileForKafFile += ".tag";
                 }
                 DO_loadTagFile(tagFileForKafFile);
             }
         this.repaint();
     }
 
     void DO_loadTagFile() {
         messageField.setText("");
         if (THETAGFILE.length()==0) {
             int idx = inputName.lastIndexOf(".");
             if (idx>-1) {
                 THETAGFILE = inputName.substring(0, idx)+".tag";
                 THETAGFILE = selectInputFileDialog(this, THETAGFILE, ".tag");
             }
             else {
                 THETAGFILE = selectInputFileDialog(this, ".tag");
             }
         }
         else {
             THETAGFILE = selectInputFileDialog(this, THETAGFILE, ".tag");
         }
         DO_loadTagFile(THETAGFILE);
     }
 
     public void DO_loadTagFile(String tagFile) {
 
         //table.init();
         this.DO_unTagAllTokens();
         messageField.setText("");
         THETAGFILE = tagFile;
         if (THETAGFILE.length()>0) {
             tagFileField.setText(THETAGFILE);
             int nAnnos = 0;
             if (new File (THETAGFILE).exists()) {
                 table.READFILE = true;
                 if ((taggedWordList!=null) && (taggedWordList.size()>0)) {
                     nAnnos = table.theTable.addAnnotations(THETAGFILE);
                 }
                 else {
                     nAnnos = table.theTable.addJustAnnotations(THETAGFILE);
                 }
                 messageField.setText("Loaded from:"+THETAGFILE+". Nr of annotated tokens="+nAnnos);
                 table.READFILE = false;
             }
             else {
                 System.out.println("NO TAG FILE");
             }
         }
         else {
             messageField.setText("No tag file loaded!");
         }
         //table.table.setRowSorter(table.rowSorter);
         //table.hideUntaggedRows();
         this.repaint();
     }
 
     void DO_readLexicon() {
         messageField.setText("");
         LEXICONFILE = selectInputFileDialog(this,RESOURCESFOLDER, ".xml");
         if (LEXICONFILE.length()>0) {
             if (new File(LEXICONFILE).exists()) {
                 tagLexicon.parseFile(LEXICONFILE);
                 String txt = "Load lexicon from:"+LEXICONFILE+". Nr of entries="+tagLexicon.data.size();
                 messageField.setText(txt);
             }
         }
         else {
            messageField.setText("No lexicon file loaded!");
         }
     }
 
     void DO_readTagSet() {
         TAGSETFILE = selectInputFileDialog(this,RESOURCESFOLDER, ".txt");
         DO_readTagSet(TAGSETFILE);
     }
 
     public void DO_readTagSet(String inputFile) {
         messageField.setText("");
         String error = "";
         String level = "";
         String tag = "";
         int nTags = 0;
         TAGSETFILE = inputFile;
         if (TAGSETFILE.length()>0) {
             tagSetFileField.setText(TAGSETFILE);
             if (!new File(TAGSETFILE).exists()) {
                 theTag1Set = new ArrayList<String>();
                 theTag2Set = new ArrayList<String>();
                 theTag3Set = new ArrayList<String>();
                 theTag4Set = new ArrayList<String>();
                 theTag5Set = new ArrayList<String>();
                 theTag6Set = new ArrayList<String>();
                 theTag7Set = new ArrayList<String>();
                 theTag8Set = new ArrayList<String>();
                 String txt = "The file does not exist:"+TAGSETFILE+". Tag menus have been cleared. Please add tags manually";
                 messageField.setText(txt);
                 makeTagSet1Menus();
                 makeTagSet2Menus();
                 makeTagSet3Menus();
                 makeTagSet4Menus();
                 makeTagSet5Menus();
                 makeTagSet6Menus();
                 makeTagSet7Menus();
                 makeTagSet8Menus();
             }
             else {
                 try {
                   theTag1Set = new ArrayList<String>();
                   theTag2Set = new ArrayList<String>();
                   theTag3Set = new ArrayList<String>();
                   theTag4Set = new ArrayList<String>();
                   theTag5Set = new ArrayList<String>();
                   theTag6Set = new ArrayList<String>();
                   theTag7Set = new ArrayList<String>();
                   theTag8Set = new ArrayList<String>();
                   FileInputStream fis = new FileInputStream(TAGSETFILE);
                   InputStreamReader isr = new InputStreamReader(fis);
                   BufferedReader in = new BufferedReader(isr);
                   String inputLine;
                   while ((inputLine = in.readLine()) != null) {
                       String[] fields = inputLine.trim().split(";");
                       if (fields.length==2) {
                           nTags++;
                           level = fields[0];
                           tag = fields[1];
                           if (tag.length()>0) {
                               if (level.equals("1")) {
                                   if (!theTag1Set.contains(tag)) {
                                       theTag1Set.add(tag);
                                   }
                               }
                               else if (level.equals("2")) {
                                   if (!theTag2Set.contains(tag)) {
                                       theTag2Set.add(tag);
                                   }
                               }
                               else if (level.equals("3")) {
                                   if (!theTag3Set.contains(tag)) {
                                       theTag3Set.add(tag);
                                   }
                               }
                               else if (level.equals("4")) {
                                   if (!theTag4Set.contains(tag)) {
                                       theTag4Set.add(tag);
                                   }
                               }
                               else if (level.equals("5")) {
                                   if (!theTag5Set.contains(tag)) {
                                       theTag5Set.add(tag);
                                   }
                               }
                               else if (level.equals("6")) {
                                   if (!theTag6Set.contains(tag)) {
                                       theTag6Set.add(tag);
                                   }
                               }
                               else if (level.equals("7")) {
                                   if (!theTag7Set.contains(tag)) {
                                       theTag7Set.add(tag);
                                   }
                               }
                               else if (level.equals("8")) {
                                   if (!theTag8Set.contains(tag)) {
                                       theTag8Set.add(tag);
                                   }
                               }
                           }
                       }
                   }
                   in.close();
                   String txt = "Loaded tags from:"+TAGSETFILE+". Nr of tags="+nTags;
                   messageField.setText(txt);
                   makeTagSet1Menus();
                   makeTagSet2Menus();
                   makeTagSet3Menus();
                   makeTagSet4Menus();
                   makeTagSet5Menus();
                   makeTagSet6Menus();
                   makeTagSet7Menus();
                   makeTagSet8Menus();
                 }
                 catch (Exception eee) {
                       error += "\nException --"+eee.getMessage();
                   System.out.println(error);
                 }
             }
         }
         else {
             messageField.setText("No tags loaded for level 1!");
         }
     }
 
     void DO_makeNewTag1() {
         GetStringDialog dia = new GetStringDialog("TAG", "");
 		dia.setSize(250,200);
 		dia.pack();
 		dia.setVisible(true);
 		final String tag = dia.field.getText().trim();
         if (tag.length()>0) {
             if (!theTag1Set.contains(tag)) {
                 theTag1Set.add(tag);
                 DO_saveTagSetFile(TAGSETFILE);
                 //DO_saveTagSetFile(TAGSET1FILE, theTag1Set);
                 //table.initTagCombo();
                 makeTagSet1Menus();
             }
         }
 
     }
 
     void DO_makeNewTag2() {
         GetStringDialog dia = new GetStringDialog("TAG", "");
 		dia.setSize(250,200);
 		dia.pack();
 		dia.setVisible(true);
 		final String tag = dia.field.getText().trim();
         if (tag.length()>0) {
             if (!theTag2Set.contains(tag)) {
                 theTag2Set.add(tag);
                 DO_saveTagSetFile(TAGSETFILE);
                 //DO_saveTagSetFile(TAGSET2FILE, theTag2Set);
                 //table.initTagCombo();
                 makeTagSet2Menus();
             }
         }
 
     }
 
     void DO_makeNewTag3() {
         GetStringDialog dia = new GetStringDialog("TAG", "");
 		dia.setSize(250,200);
 		dia.pack();
 		dia.setVisible(true);
 		final String tag = dia.field.getText().trim();
         if (tag.length()>0) {
             if (!theTag3Set.contains(tag)) {
                 theTag3Set.add(tag);
                 DO_saveTagSetFile(TAGSETFILE);
                 //DO_saveTagSetFile(TAGSET3FILE, theTag3Set);
                 //table.initTagCombo();
                 makeTagSet3Menus();
             }
         }
     }
 
     void DO_makeNewTag4() {
         GetStringDialog dia = new GetStringDialog("TAG", "");
 		dia.setSize(250,200);
 		dia.pack();
 		dia.setVisible(true);
 		final String tag = dia.field.getText().trim();
         if (tag.length()>0) {
             if (!theTag4Set.contains(tag)) {
                 theTag4Set.add(tag);
                 DO_saveTagSetFile(TAGSETFILE);
                 //DO_saveTagSetFile(TAGSET4FILE, theTag4Set);
                 //table.initTagCombo();
                 makeTagSet4Menus();
             }
         }
     }
 
     void DO_makeNewTag5() {
         GetStringDialog dia = new GetStringDialog("TAG", "");
 		dia.setSize(250,200);
 		dia.pack();
 		dia.setVisible(true);
 		final String tag = dia.field.getText().trim();
         if (tag.length()>0) {
             if (!theTag5Set.contains(tag)) {
                 theTag5Set.add(tag);
                 DO_saveTagSetFile(TAGSETFILE);
                 //DO_saveTagSetFile(TAGSET5FILE, theTag5Set);
                 //table.initTagCombo();
                 makeTagSet5Menus();
             }
         }
     }
 
     void DO_makeNewTag6() {
         GetStringDialog dia = new GetStringDialog("TAG", "");
 		dia.setSize(250,200);
 		dia.pack();
 		dia.setVisible(true);
 		final String tag = dia.field.getText().trim();
         if (tag.length()>0) {
             if (!theTag6Set.contains(tag)) {
                 theTag6Set.add(tag);
                 DO_saveTagSetFile(TAGSETFILE);
                 //DO_saveTagSetFile(TAGSET6FILE, theTag6Set);
                 //table.initTagCombo();
                 makeTagSet6Menus();
             }
         }
     }
 
     void DO_makeNewTag7() {
         GetStringDialog dia = new GetStringDialog("TAG", "");
 		dia.setSize(250,200);
 		dia.pack();
 		dia.setVisible(true);
 		final String tag = dia.field.getText().trim();
         if (tag.length()>0) {
             if (!theTag7Set.contains(tag)) {
                 theTag7Set.add(tag);
                 DO_saveTagSetFile(TAGSETFILE);
                 //DO_saveTagSetFile(TAGSET7FILE, theTag7Set);
                 //table.initTagCombo();
                 makeTagSet7Menus();
             }
         }
     }
 
     void DO_makeNewTag8() {
         GetStringDialog dia = new GetStringDialog("TAG", "");
 		dia.setSize(250,200);
 		dia.pack();
 		dia.setVisible(true);
 		final String tag = dia.field.getText().trim();
         if (tag.length()>0) {
             if (!theTag8Set.contains(tag)) {
                 theTag8Set.add(tag);
                 DO_saveTagSetFile(TAGSETFILE);
                 //DO_saveTagSetFile(TAGSET8FILE, theTag8Set);
                 //table.initTagCombo();
                 makeTagSet8Menus();
             }
         }
     }
 
 
     String selectInputFileDialog(JFrame parent, final String extension) {
          String theFile = "";
          Locations adjLoc = new Locations(LOCATIONFILE);
          LocationsParser parser = new LocationsParser(LOCATIONFILE);
          adjLoc.inputs = parser.input;
          String inputPath = "";
          if (adjLoc.inputs.size()>0) {
              inputPath = ((String)adjLoc.inputs.elementAt(0)).trim();
          }
          JFileChooser fc = new JFileChooser(inputPath);
          fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
              public boolean accept(File f) {
                return
                      (
                      f.getName().toLowerCase().endsWith(extension) ||
                      f.isDirectory()
                      );
              }
              public String getDescription() {
                  return extension;
              }
          });
          int returnVal = fc.showDialog(parent, "Select a file");
          if (returnVal == JFileChooser.APPROVE_OPTION) {
              File file = fc.getSelectedFile();
              theFile = file.getPath();
              if (new File (LOCATIONFILE).exists()) {
                  adjLoc.inputs.insertElementAt(theFile,0);
                  //serializations of the databases
 
                  try{
                    adjLoc.serialization(LOCATIONFILE);
                  }
                  catch(IOException io){
                    io.printStackTrace();
                  }
              }
          }
          return theFile;
       }
 
       String selectInputFileDialog(JFrame parent, String inputPath, final String extension) {
           File inputFile = new File (inputPath);
           String theFile = "";
           JFileChooser fc = new JFileChooser();
           if (inputFile.isDirectory()) {
              fc = new JFileChooser(inputFile);
           }
           else {
                fc = new JFileChooser(inputFile.getParent());
           }           fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
              public boolean accept(File f) {
                return
                      (
                      f.getName().toLowerCase().endsWith(extension) ||
                      f.isDirectory()
                      );
              }
              public String getDescription() {
                  return "Input file";
              }
              });
              int returnVal = fc.showDialog(parent, "Select a file");
              if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File file = fc.getSelectedFile();
                   theFile = file.getPath();
              }
              return theFile;
       }
 
       String selectOutputFileDialog(JFrame parent, String inputPath, final String extension, final String textLabel) {
           File inputFile = new File (inputPath);
           String outputPath = "";
           JFileChooser fc = new JFileChooser();
           if (inputFile.isDirectory()) {
              fc = new JFileChooser(inputFile);
           }
           else {
                fc = new JFileChooser(inputFile.getParent());
           }
           fc.setDialogTitle(textLabel);
           fc.setDialogType(JFileChooser.SAVE_DIALOG);
           fc.setFileFilter(new javax.swing.filechooser.FileFilter() {
               public boolean accept(File f) {
                 return (f.getName().toLowerCase().endsWith(extension) ||
                       f.isDirectory());
               }
               public String getDescription() {
                 return (extension);
               }
           });
           int returnVal = fc.showSaveDialog(parent);
           if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File file = fc.getSelectedFile();
                 outputPath = file.getAbsolutePath();
           }
           fc.setVisible(false);
           return outputPath;
     }
 
     int DO_saveTagFile() {
         messageField.setText("");
             if (THETAGFILE.length()==0) {
                 int idx = inputName.lastIndexOf(".");
                 if (idx>-1) {
                     THETAGFILE = inputName.substring(0, idx)+".tag";
                     THETAGFILE = selectOutputFileDialog(this, THETAGFILE, ".tag", "Save KAF tags in file");
                 }
                 else {
                     THETAGFILE = selectOutputFileDialog(this, inputName, ".tag", "Save KAF tags in file");
                 }
             }
             if (THETAGFILE.length()>0) {
                 tagFileField.setText(THETAGFILE);
                 table.theTable.writeTableToTagFile(THETAGFILE);
                 messageField.setText("Saved to:"+THETAGFILE);
                 return 0;
             }
         return -1;
     }
 
     int DO_saveTagFileAs() {
         messageField.setText("");
             if (THETAGFILE.length()==0) {
                 int idx = inputName.lastIndexOf(".");
                 if (idx>-1) {
                     THETAGFILE = inputName.substring(0, idx)+".tag";
                     THETAGFILE = selectOutputFileDialog(this, THETAGFILE, ".tag", "Save KAF tags in file");
                 }
                 else {
                     THETAGFILE = selectOutputFileDialog(this, inputName, ".tag", "Save KAF tags in file");
                 }
             }
             else {
                 THETAGFILE = selectOutputFileDialog(this, THETAGFILE, ".tag","Save KAF tags in file");
             }
             if (THETAGFILE.length()>0) {
                 tagFileField.setText(THETAGFILE);
                 table.theTable.writeTableToTagFile(THETAGFILE);
                 messageField.setText("Saved to:"+THETAGFILE);
                 return 0;
             }
         return -1;
     }
 
     int DO_saveLexiconFile() {
         messageField.setText("");
         if (LEXICONFILE.length()==0) {
             LEXICONFILE = selectOutputFileDialog(this, RESOURCESFOLDER, ".xml","Save lexicon with tags in file");
         }
         if (LEXICONFILE.length()>0) {
             instance.tagLexicon.saveLexicon(LEXICONFILE);
             String txt = "Saved to lexicon file:"+LEXICONFILE+". Nr of entries="+instance.tagLexicon.data.size();
             messageField.setText(txt);
             return 0;
         }
         return -1;
     }
 
     int DO_saveLexiconFileAs() {
         messageField.setText("");
         if (LEXICONFILE.length()==0) {
             LEXICONFILE = selectOutputFileDialog(this, RESOURCESFOLDER, ".xml","Save lexicon with tags in file");
         }
         else {
             LEXICONFILE = selectOutputFileDialog(this, LEXICONFILE, ".xml", "Save lexicon with tags in file");
         }
         if (LEXICONFILE.length()>0) {
             instance.tagLexicon.saveLexicon(LEXICONFILE);
             String txt = "Saved to lexicon file:"+LEXICONFILE+". Nr of entries="+instance.tagLexicon.data.size();
             messageField.setText(txt);
             return 0;
         }
         return -1;
     }
 
 
     void DO_saveTagSetFile(String outputFile, ArrayList<String> theTagSet) {
         try {
           File theFile = new File (outputFile);
           if (theFile.exists()) {
              String outputPath = outputFile+".bu";
              File input = new File(outputFile);
              input.renameTo(new File(outputPath));
           }
           FileOutputStream fos = new FileOutputStream(outputFile);
           for (int i = 0; i < theTagSet.size(); i++) {
               String s = theTagSet.get(i)+"\n";
              // System.out.println("s = " + s);
               fos.write(s.getBytes());
           }
           fos.close();
         }
         catch (Exception e){ e.printStackTrace();}
     }
 
     void DO_saveTagSetFile(String outputFile) {
         try {
           File theFile = new File (outputFile);
           if (theFile.exists()) {
              String outputPath = outputFile+".bu";
              File input = new File(outputFile);
              input.renameTo(new File(outputPath));
           }
           FileOutputStream fos = new FileOutputStream(outputFile);
 
           for (int i = 0; i < theTag1Set.size(); i++) {
               String s = "1;"+theTag1Set.get(i)+"\n";
               fos.write(s.getBytes());
           }
           for (int i = 0; i < theTag2Set.size(); i++) {
               String s = "2;"+theTag2Set.get(i)+"\n";
               fos.write(s.getBytes());
           }
           for (int i = 0; i < theTag3Set.size(); i++) {
               String s = "3;"+theTag3Set.get(i)+"\n";
               fos.write(s.getBytes());
           }
           for (int i = 0; i < theTag4Set.size(); i++) {
               String s = "4;"+theTag4Set.get(i)+"\n";
               fos.write(s.getBytes());
           }
           for (int i = 0; i < theTag5Set.size(); i++) {
               String s = "5;"+theTag5Set.get(i)+"\n";
               fos.write(s.getBytes());
           }
           for (int i = 0; i < theTag6Set.size(); i++) {
               String s = "6;"+theTag6Set.get(i)+"\n";
               fos.write(s.getBytes());
           }
           for (int i = 0; i < theTag7Set.size(); i++) {
               String s = "7;"+theTag7Set.get(i)+"\n";
               fos.write(s.getBytes());
           }
           for (int i = 0; i < theTag8Set.size(); i++) {
               String s = "8;"+theTag8Set.get(i)+"\n";
               fos.write(s.getBytes());
           }
           fos.close();
         }
         catch (Exception e){ e.printStackTrace();}
     }
 
     void DO_saveTrainFile() {
             messageField.setText("");
             int idx = inputName.lastIndexOf(".");
             String outputName = inputName.substring(0,idx)+".train";
             table.theTable.writeTableToTrainFile(outputName);
             messageField.setText("Saved to:"+outputName);
     }
 
 
     void DO_searchWord() {
         GetStringDialog dia = new GetStringDialog("Word", lastWord);
 		dia.setSize(250,200);
 		dia.pack();
 		dia.setVisible(true);
 		lastWord = dia.field.getText();
         messageField.setText(table.searchForString(AnnotationTableModel.ROWWORDTOKEN,lastWord));
     }
 
     void DO_searchWordAgain() {
         messageField.setText(table.searchForString(AnnotationTableModel.ROWWORDTOKEN,lastWord));
     }
 
     void DO_searchLastTag() {
         messageField.setText(table.searchForLastString(AnnotationTableModel.TAGROWS));
     }
 
     void DO_searchTag() {
         GetStringDialog dia = new GetStringDialog("TAG", lastTag);
 		dia.setSize(250,200);
 		dia.pack();
 		dia.setVisible(true);
 		lastTag = dia.field.getText();
 
         messageField.setText(table.searchForString(AnnotationTableModel.TAGROWS,lastTag));
     }
 
     void DO_searchTagAgain() {
          messageField.setText(table.searchForString(AnnotationTableModel.TAGROWS,lastTag));
      }
 
 
     void DO_notDone() {
         messageField.setText(table.searchForBoolean(AnnotationTableModel.ROWSTATUS,false));
     }
 
 
     void DO_applyTagToTokens(String tag, int level) {
         messageField.setText("");
         Integer tagId = table.theTable.generateTagId();
         int [] rows =   table.table.getSelectedRows();
         if (rows.length>0) {
 /*
             for (int i = 0; i < cacheBu.size(); i++) {
                 CacheData cacheData = cacheBu.get(i);
                 System.out.println("BU cacheData.toString() = " + cacheData.toString());
             }
 */
             cacheBu = cache;
 /*
             for (int i = 0; i < cacheBu.size(); i++) {
                 CacheData cacheData = cacheBu.get(i);
                 System.out.println("BU cacheData.toString() = " + cacheData.toString());
             }
 */
 
             cache = new ArrayList<CacheData>();
             for (int i = 0; i<rows.length;i++) {
               int theRow = rows[i];
               addRowToCache(theRow, level);
               if (level==1) {
                   table.sorter.setValueAt(tag, theRow, AnnotationTableModel.ROWTAG1);
                   table.sorter.setValueAt(tagId, theRow, AnnotationTableModel.ROWTAGID1);
 
               }
               else if (level==2) {
                   table.sorter.setValueAt(tag, theRow, AnnotationTableModel.ROWTAG2);
                   table.sorter.setValueAt(tagId, theRow, AnnotationTableModel.ROWTAGID2);
               }
               else if (level==3) {
                   table.sorter.setValueAt(tag, theRow, AnnotationTableModel.ROWTAG3);
                   table.sorter.setValueAt(tagId, theRow, AnnotationTableModel.ROWTAGID3);
               }
               else if (level==4) {
                   table.sorter.setValueAt(tag, theRow, AnnotationTableModel.ROWTAG4);
                   table.sorter.setValueAt(tagId, theRow, AnnotationTableModel.ROWTAGID4);
               }
               else if (level==5) {
                   table.sorter.setValueAt(tag, theRow, AnnotationTableModel.ROWTAG5);
                   table.sorter.setValueAt(tagId, theRow, AnnotationTableModel.ROWTAGID5);
               }
               else if (level==6) {
                   table.sorter.setValueAt(tag, theRow, AnnotationTableModel.ROWTAG6);
                   table.sorter.setValueAt(tagId, theRow, AnnotationTableModel.ROWTAGID6);
               }
               else if (level==7) {
                   table.sorter.setValueAt(tag, theRow, AnnotationTableModel.ROWTAG7);
                   table.sorter.setValueAt(tagId, theRow, AnnotationTableModel.ROWTAGID7);
               }
               else if (level==8) {
                   table.sorter.setValueAt(tag, theRow, AnnotationTableModel.ROWTAG8);
                   table.sorter.setValueAt(tagId, theRow, AnnotationTableModel.ROWTAGID8);
               }
             }
         }
 /*
         for (int i = 0; i < cache.size(); i++) {
             CacheData cacheData = cache.get(i);
             System.out.println("cache cacheData.toString() = " + cacheData.toString());
         }
 */
         this.repaint();
         if (cache.size()==0) {
             cache = cacheBu;
         }
     }
 
     void DO_unTagTokens(int level) {
         messageField.setText("");
         int [] rows =   table.table.getSelectedRows();
         if (rows.length>0) {
             cacheBu = cache;
             cache = new ArrayList<CacheData>();
             for (int i = 0; i<rows.length;i++) {
               int theRow = rows[i];
               String wordForm = (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWWORDTOKEN);
               String tag = (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAG1);
               addRowToCache(theRow, level);
               if (level==1) {
                   tagLexicon.decrementEntry(wordForm, tag);
                   table.sorter.setValueAt("", theRow, AnnotationTableModel.ROWTAG1);
                   table.sorter.setValueAt(0, theRow, AnnotationTableModel.ROWTAGID1);
                   if (table.theTable.hasTagValue(theRow)) {
                     table.sorter.setValueAt(new Boolean(false), theRow, AnnotationTableModel.ROWSTATUS);
                   }
               }
               else if (level==2) {
                   table.sorter.setValueAt("", theRow, AnnotationTableModel.ROWTAG2);
                   table.sorter.setValueAt(0, theRow, AnnotationTableModel.ROWTAGID2);
                   if (table.theTable.hasTagValue(theRow)) {
                       table.sorter.setValueAt(new Boolean(false), theRow, AnnotationTableModel.ROWSTATUS);
                   }
               }
               else if (level==3) {
                   table.sorter.setValueAt("", theRow, AnnotationTableModel.ROWTAG3);
                   table.sorter.setValueAt(0, theRow, AnnotationTableModel.ROWTAGID3);
                   if (table.theTable.hasTagValue(theRow)) {
                       table.sorter.setValueAt(new Boolean(false), theRow, AnnotationTableModel.ROWSTATUS);
                   }
               }
               else if (level==4) {
                   table.sorter.setValueAt("", theRow, AnnotationTableModel.ROWTAG4);
                   table.sorter.setValueAt(0, theRow, AnnotationTableModel.ROWTAGID4);
                   if (table.theTable.hasTagValue(theRow)) {
                       table.sorter.setValueAt(new Boolean(false), theRow, AnnotationTableModel.ROWSTATUS);
                   }
               }
               else if (level==5) {
                   table.sorter.setValueAt(0, theRow, AnnotationTableModel.ROWTAGID5);
                   table.sorter.setValueAt("", theRow, AnnotationTableModel.ROWTAG5);
                   if (table.theTable.hasTagValue(theRow)) {
                       table.sorter.setValueAt(new Boolean(false), theRow, AnnotationTableModel.ROWSTATUS);
                   }
               }
               else if (level==6) {
                   table.sorter.setValueAt("", theRow, AnnotationTableModel.ROWTAG6);
                   table.sorter.setValueAt(0, theRow, AnnotationTableModel.ROWTAGID6);
                   if (table.theTable.hasTagValue(theRow)) {
                       table.sorter.setValueAt(new Boolean(false), theRow, AnnotationTableModel.ROWSTATUS);
                   }
               }
               else if (level==7) {
                   table.sorter.setValueAt("", theRow, AnnotationTableModel.ROWTAG7);
                   table.sorter.setValueAt(0, theRow, AnnotationTableModel.ROWTAGID7);
                   if (table.theTable.hasTagValue(theRow)) {
                       table.sorter.setValueAt(new Boolean(false), theRow, AnnotationTableModel.ROWSTATUS);
                   }
               }
               else if (level==8) {
                   table.sorter.setValueAt("", theRow, AnnotationTableModel.ROWTAG8);
                   table.sorter.setValueAt(0, theRow, AnnotationTableModel.ROWTAGID8);
                   if (table.theTable.hasTagValue(theRow)) {
                       table.sorter.setValueAt(new Boolean(false), theRow, AnnotationTableModel.ROWSTATUS);
                   }
               }
             }
             this.repaint();
             if (cache.size()==0) {
                 cache = cacheBu;
             }
         }
     }
 
     void DO_unTagAllTokens() {
         messageField.setText("");
         cacheBu = cache;
         table.removeAllTags();
         this.repaint();
     }
 
     void DO_mergeTagIds(int level) {
         messageField.setText("");
         int [] rows =   table.table.getSelectedRows();
         if (rows.length>0) {
             cacheBu = cache;
             cache = new ArrayList<CacheData>();
             int theRow = rows[0];
             Integer id = table.theTable.generateTagId();
             if (level==1) {
                 id = (Integer) table.sorter.getValueAt(theRow,AnnotationTableModel.ROWTAGID1);
             }
             if (level==2) {
                 id = (Integer) table.sorter.getValueAt(theRow,AnnotationTableModel.ROWTAGID2);
             }
             if (level==3) {
                 id = (Integer) table.sorter.getValueAt(theRow,AnnotationTableModel.ROWTAGID3);
             }
             if (level==4) {
                 id = (Integer) table.sorter.getValueAt(theRow,AnnotationTableModel.ROWTAGID4);
             }
             if (level==5) {
                 id = (Integer) table.sorter.getValueAt(theRow,AnnotationTableModel.ROWTAGID5);
             }
             if (level==6) {
                 id = (Integer) table.sorter.getValueAt(theRow,AnnotationTableModel.ROWTAGID6);
             }
             if (level==7) {
                 id = (Integer) table.sorter.getValueAt(theRow,AnnotationTableModel.ROWTAGID7);
             }
             if (level==8) {
                 id = (Integer) table.sorter.getValueAt(theRow,AnnotationTableModel.ROWTAGID8);
             }
             for (int i = 0; i<rows.length;i++) {
               theRow = rows[i];
               addRowToCache(theRow, level);
               if (level==1) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID1);
               }
               else if (level==2) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID2);
               }
               else if (level==3) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID3);
               }
               else if (level==4) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID4);
               }
               else if (level==5) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID5);
               }
               else if (level==6) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID6);
               }
               else if (level==7) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID7);
               }
               else if (level==8) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID8);
               }
             }
             this.repaint();
             if (cache.size()==0) {
                 cache = cacheBu;
             }
         }
     }
 
     void DO_separateTagIds(int level) {
         messageField.setText("");
         int [] rows =   table.table.getSelectedRows();
         if (rows.length>0) {
             cacheBu = cache;
             cache = new ArrayList<CacheData>();
             for (int i = 0; i<rows.length;i++) {
               int theRow = rows[i];
               Integer id = table.theTable.generateTagId();
               addRowToCache(theRow, level);
               if (level==1) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID1);
               }
               else if (level==2) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID2);
               }
               else if (level==3) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID3);
               }
               else if (level==4) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID4);
               }
               else if (level==5) {
                 table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID5);
               }
               else if (level==6) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID6);
               }
               else if (level==7) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID7);
               }
               else if (level==8) {
                   table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID8);
               }
             }
             this.repaint();
         }
     }
 
     void DO_editTagIds(int level) {
         messageField.setText("");
         int [] rows =   table.table.getSelectedRows();
         if (rows.length>0) {
             GetIntegerDialog dia = new GetIntegerDialog("Id", "0");
             dia.setSize(250,200);
             dia.pack();
             dia.setVisible(true);
             if (dia.DOIT) {
                 Integer id = null;
                 try {
                     id = Integer.parseInt(dia.field.getText());
                 } catch (NumberFormatException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
                 cacheBu = cache;
                 cache = new ArrayList<CacheData>();
                 for (int i = 0; i<rows.length;i++) {
                   int theRow = rows[i];
                   addRowToCache(theRow, level);
                   if (level==1) {
                       table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID1);
                   }
                   else if (level==2) {
                       table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID2);
                   }
                   else if (level==3) {
                       table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID3);
                   }
                   else if (level==4) {
                       table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID4);
                   }
                   else if (level==5) {
                     table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID5);
                   }
                   else if (level==6) {
                       table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID6);
                   }
                   else if (level==7) {
                       table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID7);
                   }
                   else if (level==8) {
                       table.sorter.setValueAt(id, theRow, AnnotationTableModel.ROWTAGID8);
                   }
                 }
                 this.repaint();
             }
             dia.dispose();
         }
     }
 
     void DO_copyTag(int level) {
         messageField.setText("");
         int [] rows =   table.table.getSelectedRows();
         if (rows.length==1) {
               if (level==1) {
                   this.clipboardTag = (String) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAG1);
                   this.clipboardTagId = (Integer) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAGID1);
                //   System.out.println("this.clipboardTag = " + this.clipboardTag);
                //   System.out.println("this.clipboardTagId = " + this.clipboardTagId);
               }
               else if (level==2) {
                   this.clipboardTag = (String) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAG2);
                   this.clipboardTagId = (Integer) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAGID2);
               }
               else if (level==3) {
                   this.clipboardTag = (String) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAG3);
                   this.clipboardTagId = (Integer) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAGID3);
               }
               else if (level==4) {
                   this.clipboardTag = (String) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAG4);
                   this.clipboardTagId = (Integer) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAGID4);
               }
               else if (level==5) {
                   this.clipboardTag = (String) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAG5);
                   this.clipboardTagId = (Integer) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAGID5);
               }
               else if (level==6) {
                   this.clipboardTag = (String) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAG6);
                   this.clipboardTagId = (Integer) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAGID6);
               }
               else if (level==7) {
                   this.clipboardTag = (String) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAG7);
                   this.clipboardTagId = (Integer) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAGID7);
               }
               else if (level==8) {
                   this.clipboardTag = (String) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAG8);
                   this.clipboardTagId = (Integer) table.sorter.getValueAt(rows[0], AnnotationTableModel.ROWTAGID8);
               }
         }
        // System.out.println("rows.length = " + rows.length);
     }
 
     void DO_pastTag(int level) {
         messageField.setText("");
         if (this.clipboardTag.length()>0) {
             int [] rows =   table.table.getSelectedRows();
             if (rows.length>0) {
                 cacheBu = cache;
                 cache = new ArrayList<CacheData>();
                 for (int i = 0; i<rows.length;i++) {
                   int theRow = rows[i];
                   addRowToCache(theRow, level);
                   if (level==1) {
                       table.sorter.setValueAt(this.clipboardTag, theRow, AnnotationTableModel.ROWTAG1);
                       table.sorter.setValueAt(this.clipboardTagId, theRow, AnnotationTableModel.ROWTAGID1);
                   }
                   else if (level==2) {
                       table.sorter.setValueAt(this.clipboardTag, theRow, AnnotationTableModel.ROWTAG2);
                       table.sorter.setValueAt(this.clipboardTagId, theRow, AnnotationTableModel.ROWTAGID2);
                   }
                   else if (level==3) {
                       table.sorter.setValueAt(this.clipboardTag, theRow, AnnotationTableModel.ROWTAG3);
                       table.sorter.setValueAt(this.clipboardTagId, theRow, AnnotationTableModel.ROWTAGID3);
                   }
                   else if (level==4) {
                       table.sorter.setValueAt(this.clipboardTag, theRow, AnnotationTableModel.ROWTAG4);
                       table.sorter.setValueAt(this.clipboardTagId, theRow, AnnotationTableModel.ROWTAGID4);
                   }
                   else if (level==5) {
                     table.sorter.setValueAt(this.clipboardTag, theRow, AnnotationTableModel.ROWTAG5);
                     table.sorter.setValueAt(this.clipboardTagId, theRow, AnnotationTableModel.ROWTAGID5);
                   }
                   else if (level==6) {
                       table.sorter.setValueAt(this.clipboardTag, theRow, AnnotationTableModel.ROWTAG6);
                       table.sorter.setValueAt(this.clipboardTagId, theRow, AnnotationTableModel.ROWTAGID6);
                   }
                   else if (level==7) {
                       table.sorter.setValueAt(this.clipboardTag, theRow, AnnotationTableModel.ROWTAG7);
                       table.sorter.setValueAt(this.clipboardTagId, theRow, AnnotationTableModel.ROWTAGID7);
                   }
                   else if (level==8) {
                       table.sorter.setValueAt(this.clipboardTag, theRow, AnnotationTableModel.ROWTAG8);
                       table.sorter.setValueAt(this.clipboardTagId, theRow, AnnotationTableModel.ROWTAGID8);
                   }
                 }
                 this.repaint();
             }
         }
     }
 
     void DO_applyTagToTypes(String tag, int level) {
         messageField.setText("");
         int [] rows =   table.table.getSelectedRows();
         Integer tagId = table.theTable.generateTagId();
         ArrayList<String> taggedTokens = new ArrayList<String>();
         if (rows.length>0) {
             cache = new ArrayList<CacheData>();
             for (int i = 0; i<rows.length;i++) {
               int theRow = rows[i];
               String tokenId = (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID);
               KafTerm term = null;
               if (parser.WordFormToTerm.containsKey(tokenId)) {
                   String termId = parser.WordFormToTerm.get(tokenId);
                   term = parser.getTerm(termId);
               }
               if (term!=null) {
                   ArrayList<String> spans = term.getSpans();
                   for (int j = 0; j < spans.size(); j++) {
                       String s = spans.get(j);
                       if (!taggedTokens.contains(s)) {
                        //   System.out.println("s = " + s);
                           table.searchForString(AnnotationTableModel.ROWID,s);
                           int currentRow = table.table.getSelectedRow();
                        //   System.out.println("currentRow = " + currentRow);
                           addRowToCache(currentRow, level);
                           if (level==1) {
                               table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG1);
                               table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID1);
                           }
                           else if (level==2) {
                               table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG2);
                               table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID2);
                           }
                           else if (level==3) {
                               table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG3);
                               table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID3);
                           }
                           else if (level==4) {
                               table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG4);
                               table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID4);
                           }
                           else if (level==5) {
                               table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG5);
                               table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID5);
                           }
                           else if (level==6) {
                               table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG6);
                               table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID6);
                           }
                           else if (level==7) {
                               table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG7);
                               table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID7);
                           }
                           else if (level==8) {
                               table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG8);
                               table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID8);
                           }
                           taggedTokens.add(s);
                       }
                   }
               }
               else {
                   messageField.setText("Could not fine the type. Please annotate at the token level");
               }
             }
             this.repaint();
             if (cache.size()==0) {
                 cache = cacheBu;
             }
         }
     }
 
     void addChunk (ArrayList<KafChunk> chunks, KafChunk chunk) {
         if (chunks.size()==0) {
             chunks.add(chunk);
         }
         else {
             ArrayList<String> termIds = chunk.getSpans();
             for (int j = 0; j < termIds.size(); j++) {
                 String s = termIds.get(j);
                 for (int i = 0; i < chunks.size(); i++) {
                     KafChunk kafChunk = chunks.get(i);
                     if (kafChunk.getSpans().contains(s)) {
                         if (chunk.getSpans().size()<kafChunk.getSpans().size()) {
                             if (!chunks.contains(chunk)) {
                                 // we swap the chunks
                                 chunks.set(i, chunk);
                              //   System.out.println("substituting kafChunk.getPhrase() = " + kafChunk.getPhrase());
                              //   System.out.println("for chunk.getPhrase() = " + chunk.getPhrase());
                                 return;
                             }
                         }
                         else {
                             // we are done since there is already a smaller chunk
                          //   System.out.println("Keeping kafChunk.getPhrase() = " + kafChunk.getPhrase());
                          //   System.out.println("instead of chunk.getPhrase() = " + chunk.getPhrase());
                             return;
                         }
                     }
                 }
             }
         }
     }
 
     void DO_applyTagToConstituents(String tag, int level) {
         messageField.setText("");
         int [] rows =   table.table.getSelectedRows();
         if (rows.length==0) {
             messageField.setText("No rows selected");
             return;
         }
         else {
             cacheBu = cache;
             cache = new ArrayList<CacheData>();
             Integer tagId = table.theTable.generateTagId();
             ArrayList<String> taggedTokens = new ArrayList<String>();
             for (int i = 0; i<rows.length;i++) {
               int theRow = rows[i];
               String tokenId = (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID);
               ArrayList<KafTerm> terms = new ArrayList<KafTerm>();
               if (parser.WordFormToTerm.containsKey(tokenId)) {
                   String termId = parser.WordFormToTerm.get(tokenId);
                   if (parser.TermToChunk.containsKey(termId)) {
                       ArrayList<KafChunk> theChunks = new ArrayList<KafChunk>();
                       ArrayList<String> chunkIds = parser.TermToChunk.get(termId);
                       for (int j = 0; j < chunkIds.size(); j++) {
                           String cid = chunkIds.get(j);
                          // System.out.println("cid = " + cid);
                           KafChunk chunk = parser.getChunks(cid);
                          // System.out.println("theChunks before adding = " + theChunks.size());
                           addChunk (theChunks, chunk);
                          // System.out.println("theChunks after adding = " + theChunks.size());
                       }
                       for (int j = 0; j < theChunks.size(); j++) {
                           KafChunk kafChunk = theChunks.get(j);
                           ArrayList<String> termdIds = kafChunk.getSpans();
                           for (int k = 0; k < termdIds.size(); k++) {
                               String tid = termdIds.get(k);
                               KafTerm term = parser.getTerm(tid);
                               if (term!=null) {
                                 terms.add(term);
                               }
                           }
 
                       }
                   }
                   else {
                       messageField.setText("There is no constituent for this token. Please annotate at the token level");
                       return;
                   }
               }
               if (terms.size()>0) {
                   for (int t = 0; t < terms.size(); t++) {
                       KafTerm kafTerm = terms.get(t);
                     //  System.out.println("kafTerm.getTid() = " + kafTerm.getTid());
                       ArrayList<String> spans = kafTerm.getSpans();
                       for (int j = 0; j < spans.size(); j++) {
                           String s = spans.get(j);
                           if (!taggedTokens.contains(s)) {
                            //   System.out.println("s = " + s);
                               table.searchForString(AnnotationTableModel.ROWID,s);
                               int currentRow = table.table.getSelectedRow();
                            //   System.out.println("currentRow = " + currentRow);
                               addRowToCache(currentRow, level);
                               if (level==1) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG1);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID1);
                               }
                               else if (level==2) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG2);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID2);
                               }
                               else if (level==3) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG3);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID3);
                               }
                               else if (level==4) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG4);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID4);
                               }
                               else if (level==5) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG5);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID5);
                               }
                               else if (level==6) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG6);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID6);
                               }
                               else if (level==7) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG7);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID7);
                               }
                               else if (level==8) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG8);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID8);
                               }
                               taggedTokens.add(s);
                           }
                       }
                   }
               }
               else {
                   messageField.setText("Could not fine the constituent terms. Please annotate at the token level");
               }
             }
             this.repaint();
             if (cache.size()==0) {
                 cache = cacheBu;
             }
         }
     }
 
     void DO_applyTagToAlllConstituents(String tag, int level) {
         messageField.setText("");
         int [] rows =   table.table.getSelectedRows();
         if (rows.length==0) {
             messageField.setText("No rows selected");
             return;
         }
         else {
             cacheBu = cache;
             cache = new ArrayList<CacheData>();
             Integer tagId = table.theTable.generateTagId();
             ArrayList<String> taggedTokens = new ArrayList<String>();
             for (int i = 0; i<rows.length;i++) {
               int theRow = rows[i];
               String tokenId = (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID);
               ArrayList<KafTerm> terms = new ArrayList<KafTerm>();
               if (parser.WordFormToTerm.containsKey(tokenId)) {
                   String termId = parser.WordFormToTerm.get(tokenId);
                   if (parser.TermToChunk.containsKey(termId)) {
                       ArrayList<String> chunkIds = parser.TermToChunk.get(termId);
                       for (int j = 0; j < chunkIds.size(); j++) {
                           String cid = chunkIds.get(j);
                          // System.out.println("cid = " + cid);
                           KafChunk chunk = parser.getChunks(cid);
                           ArrayList<String> termdIds = chunk.getSpans();
                           for (int k = 0; k < termdIds.size(); k++) {
                               String tid = termdIds.get(k);
                               KafTerm term = parser.getTerm(tid);
                               if (term!=null) {
                                 terms.add(term);
                               }
                           }
                       }
                   }
                   else {
                       messageField.setText("There is no constituent for this token. Please annotate at the token level");
                       return;
                   }
               }
               if (terms.size()>0) {
                   for (int t = 0; t < terms.size(); t++) {
                       KafTerm kafTerm = terms.get(t);
                     //  System.out.println("kafTerm.getTid() = " + kafTerm.getTid());
                       ArrayList<String> spans = kafTerm.getSpans();
                       for (int j = 0; j < spans.size(); j++) {
                           String s = spans.get(j);
                           if (!taggedTokens.contains(s)) {
                            //   System.out.println("s = " + s);
                               table.searchForString(AnnotationTableModel.ROWID,s);
                               int currentRow = table.table.getSelectedRow();
                            //   System.out.println("currentRow = " + currentRow);
                               addRowToCache(currentRow, level);
                               if (level==1) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG1);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID1);
                               }
                               else if (level==2) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG2);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID2);
                               }
                               else if (level==3) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG3);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID3);
                               }
                               else if (level==4) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG4);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID4);
                               }
                               else if (level==5) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG5);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID5);
                               }
                               else if (level==6) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG6);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID6);
                               }
                               else if (level==7) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG7);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID7);
                               }
                               else if (level==8) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG8);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID8);
                               }
                               taggedTokens.add(s);
                           }
                       }
                   }
               }
               else {
                   messageField.setText("Could not fine the constituent terms. Please annotate at the token level");
               }
             }
             this.repaint();
             if (cache.size()==0) {
                 cache = cacheBu;
             }
         }
     }
 
     void DO_applyTagToSentences(String tag, int level) {
         messageField.setText("");
         int [] rows =   table.table.getSelectedRows();
         Integer tagId = table.theTable.generateTagId();
         ArrayList<String> taggedTokens = new ArrayList<String>();
         if (rows.length>0) {
             cacheBu = cache;
             cache = new ArrayList<CacheData>();
             for (int i = 0; i<rows.length;i++) {
               int theRow = rows[i];
               String tokenId = (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID);
               KafWordForm wf = parser.getWordForm(tokenId);
               if (wf!=null) {
                   String sentence = wf.getSent();
                   if (parser.SentenceToWord.containsKey(sentence)) {
                       ArrayList<String> tokenIds = parser.SentenceToWord.get(sentence);
                       if (tokenIds.size()==0) {
                           messageField.setText("Could not fine tokens for the sentence: "+sentence+". Please annotate at the token level");
                           return;
                       }
                       for (int j = 0; j < tokenIds.size(); j++) {
                           String s = tokenIds.get(j);
                           if (!taggedTokens.contains(s)) {
                         //      System.out.println("s = " + s);
                               table.searchForString(AnnotationTableModel.ROWID,s);
                               int currentRow = table.table.getSelectedRow();
                               addRowToCache (currentRow, level);
                         //      System.out.println("currentRow = " + currentRow);
                               if (level==1) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG1);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID1);
                               }
                               else if (level==2) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG2);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID2);
                               }
                               else if (level==3) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG3);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID3);
                               }
                               else if (level==4) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG4);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID4);
                               }
                               else if (level==5) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG5);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID5);
                               }
                               else if (level==6) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG6);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID6);
                               }
                               else if (level==7) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG7);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID7);
                               }
                               else if (level==8) {
                                   table.sorter.setValueAt(tag, currentRow, AnnotationTableModel.ROWTAG8);
                                   table.sorter.setValueAt(tagId, currentRow, AnnotationTableModel.ROWTAGID8);
                               }
                               taggedTokens.add(s);
                           }
                       }
                   }
                   else {
                       messageField.setText("Could not fine the sentence. Please annotate at the token level");
                   }
                 }
             }
             this.repaint();
             if (cache.size()==0) {
                 cache = cacheBu;
             }
         }
     }
 
 
     void DO_selectLexicalTag() {
         messageField.setText("");
         //// GETS THE MOST FREQUENT TAG FROM THE TAG LEXICON
         String tag = "";
         int [] rows =   table.table.getSelectedRows();
     //    System.out.println("rows.length = " + rows.length);
         Integer tagId = table.theTable.generateTagId();
         if (rows.length>0) {
             cacheBu = cache;
             cache = new ArrayList<CacheData>();
             for (int i = 0; i<rows.length;i++) {
               int theRow = rows[i];
               addRowToCache(theRow, 1); ///level 1
               String word = (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWWORDTOKEN);
         //      System.out.println("word = " + word);
               tag = tagLexicon.getTag(word);
               if (tag.length()>0) {
                   table.sorter.setValueAt(tag, theRow, AnnotationTableModel.ROWTAG1);
                   table.sorter.setValueAt(tagId, theRow, AnnotationTableModel.ROWTAGID1);
          //         System.out.println("tag = " + tag);
               }
               else {
                   messageField.setText("This word has not been tagged yet:"+word);
               }
             }
             this.repaint();
             if (cache.size()==0) {
                 cache = cacheBu;
             }
         }
     }
 
     public void DO_Selected() {
       messageField.setText("");
       int [] rows =   table.table.getSelectedRows();
       if (rows.length>0) {
           cacheBu = cache;
           cache = new ArrayList<CacheData> ();
           for (int i = 0; i<rows.length;i++) {
             int theRow = rows[i];
             addRowToCache(theRow, -1);
             table.sorter.setValueAt(new Boolean(true),theRow,AnnotationTableModel.ROWSTATUS);
           }
           table.repaint();
           this.repaint();
           if (cache.size()==0) {
               cache = cacheBu;
           }
           this.repaint();
       }
     }
 
     public void DO_Unselected() {
       messageField.setText("");
       int [] rows =   table.table.getSelectedRows();
         if (rows.length>0) {
             cacheBu = cache;
             cache = new ArrayList<CacheData> ();
             for (int i = 0; i<rows.length;i++) {
               int theRow = rows[i];
               addRowToCache(theRow, -1);
               table.sorter.setValueAt(new Boolean(false),theRow,AnnotationTableModel.ROWSTATUS);
             }
             table.repaint();
             this.repaint();
             if (cache.size()==0) {
                 cache = cacheBu;
             }
             this.repaint();
         }
     }
 
     public void DO_undo(){
         messageField.setText("");
         if (cache.size()>0) {
             for (int i = 0; i < cache.size(); i++) {
                 CacheData cacheData = cache.get(i);
                 if (cacheData.getTagLevel()==-1) {
                     table.sorter.setValueAt(new Boolean(cacheData.isStatus()),cacheData.getnRow(),AnnotationTableModel.ROWSTATUS);
                 }
                 else if (cacheData.getTagLevel()==1) {
                     table.sorter.setValueAt(cacheData.getTag(),cacheData.getnRow(),AnnotationTableModel.ROWTAG1);
                     table.sorter.setValueAt(cacheData.getTagId(),cacheData.getnRow(),AnnotationTableModel.ROWTAGID1);
                 }
                 else if (cacheData.getTagLevel()==2) {
                     table.sorter.setValueAt(cacheData.getTag(),cacheData.getnRow(),AnnotationTableModel.ROWTAG2);
                     table.sorter.setValueAt(cacheData.getTagId(),cacheData.getnRow(),AnnotationTableModel.ROWTAGID2);
                 }
                 else if (cacheData.getTagLevel()==3) {
                     table.sorter.setValueAt(cacheData.getTag(),cacheData.getnRow(),AnnotationTableModel.ROWTAG3);
                     table.sorter.setValueAt(cacheData.getTagId(),cacheData.getnRow(),AnnotationTableModel.ROWTAGID3);
                 }
                 else if (cacheData.getTagLevel()==4) {
                     table.sorter.setValueAt(cacheData.getTag(),cacheData.getnRow(),AnnotationTableModel.ROWTAG4);
                     table.sorter.setValueAt(cacheData.getTagId(),cacheData.getnRow(),AnnotationTableModel.ROWTAGID4);
                 }
                 else if (cacheData.getTagLevel()==5) {
                     table.sorter.setValueAt(cacheData.getTag(),cacheData.getnRow(),AnnotationTableModel.ROWTAG5);
                     table.sorter.setValueAt(cacheData.getTagId(),cacheData.getnRow(),AnnotationTableModel.ROWTAGID5);
                 }
                 else if (cacheData.getTagLevel()==6) {
                     table.sorter.setValueAt(cacheData.getTag(),cacheData.getnRow(),AnnotationTableModel.ROWTAG6);
                     table.sorter.setValueAt(cacheData.getTagId(),cacheData.getnRow(),AnnotationTableModel.ROWTAGID6);
                 }
                 else if (cacheData.getTagLevel()==7) {
                     table.sorter.setValueAt(cacheData.getTag(),cacheData.getnRow(),AnnotationTableModel.ROWTAG7);
                     table.sorter.setValueAt(cacheData.getTagId(),cacheData.getnRow(),AnnotationTableModel.ROWTAGID7);
                 }
                 else if (cacheData.getTagLevel()==8) {
                     table.sorter.setValueAt(cacheData.getTag(),cacheData.getnRow(),AnnotationTableModel.ROWTAG8);
                     table.sorter.setValueAt(cacheData.getTagId(),cacheData.getnRow(),AnnotationTableModel.ROWTAGID8);
                 }
             }
             cache = cacheBu;
             cacheBu = new ArrayList<CacheData>();
             this.repaint();
         }
         else {
             messageField.setText("Cannot UNDO tagging!!!!");
         }
     }
     
 
     void addRowToCache (int theRow, int level) {
         CacheData data = null;
         if (level==-1) {
             data = new CacheData(theRow, (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID),
                     (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAG1),
                     (Integer) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAGID1),
                     (Boolean) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWSTATUS),
                     level);            
         }
         else if (level==1) {
             data = new CacheData(theRow, (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID),
                     (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAG1),
                     (Integer) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAGID1),
                     (Boolean) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWSTATUS),
                     level);
         }
         else if (level==2) {
             data = new CacheData(theRow, (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID),
                     (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAG2),
                     (Integer) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAGID2),
                     (Boolean) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWSTATUS),
                     level);
         }
         else if (level==3) {
             data = new CacheData(theRow, (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID),
                     (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAG3),
                     (Integer) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAGID3),
                     (Boolean) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWSTATUS),
                     level);
         }
         else if (level==4) {
             data = new CacheData(theRow, (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID),
                     (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAG4),
                     (Integer) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAGID4),
                     (Boolean) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWSTATUS),
                     level);
         }
         else if (level==5) {
             data = new CacheData(theRow, (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID),
                     (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAG5),
                     (Integer) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAGID5),
                     (Boolean) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWSTATUS),
                     level);
         }
         else if (level==6) {
             data = new CacheData(theRow, (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID),
                     (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAG6),
                     (Integer) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAGID6),
                     (Boolean) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWSTATUS),
                     level);
         }
         else if (level==7) {
             data = new CacheData(theRow, (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID),
                     (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAG7),
                     (Integer) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAGID7),
                     (Boolean) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWSTATUS),
                     level);
         }
         else if (level==8) {
             data = new CacheData(theRow, (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWID),
                     (String) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAG8),
                     (Integer) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWTAGID8),
                     (Boolean) table.sorter.getValueAt(theRow, AnnotationTableModel.ROWSTATUS),
                     level);
         }
         if (data!=null) {
             cache.add(data);
         }
     }
 
     public int DO_checkSave () {
         messageField.setText("");
         ConfirmDialog dia = new ConfirmDialog(this, "Do you want to save tagging before quiting?", "");
         if (dia.DOIT==0) {
           int ok = -1;
           ok = DO_saveTagFile();
           DO_saveLexiconFile();
           if (ok==0) {
               messageField.setText("Tagging data saved");
           }
           else {
               messageField.setText("Warning! Tagging data NOT saved");
           }
         }
         return dia.DOIT;
     }
 
     public void DO_exportToTriples() {
          if (THETAGFILE.length()==0) {
              THETAGFILE = selectInputFileDialog(this, ".tag");
          }
          else {
             DO_saveTagFile();
          }
          ArrayList<Triple> Triples = null;
          if (TripleConfig.pairs.size()==0) {
           //  Triples = TagToTriples.loadAnnotationFile(THETAGFILE);
              messageField.setText("No Triple.cfg file in: "+RESOURCESFOLDER+":"+TripleCONFIGFILE);
 
          }
          else {
             Triples = TagToTriples.loadAnnotationFile(TripleConfig, THETAGFILE);
          }
         if (Triples!=null) {
              String TripleFile = selectOutputFileDialog(this, THETAGFILE, ".trp", "Specify file for storing Triples");
              if ((TripleFile.length()>0)) {
                  TagToTriples.saveTriplesToFile(TripleFile, Triples, TripleConfig);
              }
              ArrayList<String> sentenceTokens = new ArrayList<String> ();
              ArrayList<String> eventTokens = new ArrayList<String> ();
              for (int i = 0; i < Triples.size(); i++) {
                 Triple Triple = Triples.get(i);
                 for (int j = 0; j < Triple.getElementFirstIds().size(); j++) {
                     String s = Triple.getElementFirstIds().get(j);
                     if (!sentenceTokens.contains(s)) {
                         sentenceTokens.add(s);
                     }
                     if (!eventTokens.contains(s)) {
                         eventTokens.add(s);
                     }
                 }
                 for (int j = 0; j < Triple.getElementSecondIds().size(); j++) {
                     String s = Triple.getElementSecondIds().get(j);
                     if (!sentenceTokens.contains(s)) {
                         sentenceTokens.add(s);
                     }
                 }
              }
 
             ///// NEXT WE OUTPUT THE TOKEN SCOPE FOR THE SENTENCES THAT WERE ANNOTATED
             ArrayList<String> sentences = new ArrayList<String>();
          //   System.out.println("sentenceTokens.size() = " + sentenceTokens.size());
             for (int i = 0; i < sentenceTokens.size(); i++) {
                 String s = sentenceTokens.get(i);
                 if (parser.wordFormMap.containsKey(s)) {
                 //if (parser.kafWordFormList.contains(s)) {
                     //KafWordForm wf = parser.kafWordFormList.get(parser.kafWordFormList.indexOf(s));
                     KafWordForm wf = parser.wordFormMap.get(s);
                     String sentence = wf.getSent();
                  //   System.out.println("sentence = " + sentence);
                     if (sentence.length()>0) {
                         if (!sentences.contains(sentence)) {
                             sentences.add(sentence);
                         }
                     }
                 }
             }
             try {
              //   System.out.println("sentences.size() = " + sentences.size());
              //   System.out.println("parser.SentenceToWord.size() = " + parser.SentenceToWord.size());
                 FileOutputStream fos = new FileOutputStream (TripleFile+".sentence-token-scope");
                 for (int i = 0; i < sentences.size(); i++) {
                     String s = sentences.get(i);
                     if (parser.SentenceToWord.containsKey(s)) {
                         ArrayList<String> wfs = parser.SentenceToWord.get(s);
                         for (int j = 0; j < wfs.size(); j++) {
                             String s1 = wfs.get(j)+"\n";
                             fos.write(s1.getBytes());
                         }
                     }
                 }
                 fos.close();
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
 
             //////// WE OUTPUT THE TOKEN SCOPE FOR JUST THE EVENTS (MORE STRICT)
             ArrayList<String> events = new ArrayList<String>();
          //   System.out.println("sentenceTokens.size() = " + sentenceTokens.size());
             for (int i = 0; i < eventTokens.size(); i++) {
                 String s = eventTokens.get(i);
                 if (parser.wordFormMap.containsKey(s)) {
                     KafWordForm wf = parser.wordFormMap.get(s);
                     if (!events.contains(wf.getWid())) {
                         events.add(wf.getWid());
                     }
                 }
             }
             try {
                 FileOutputStream fos = new FileOutputStream (TripleFile+".first-element-token-scope");
                 for (int i = 0; i < events.size(); i++) {
                     String s = events.get(i)+"\n";
                     fos.write(s.getBytes());
                 }
                 fos.close();
 
             } catch (IOException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
     }
     public void DO_exportMostCommonSubsumers() {
          if (THETAGFILE.length()==0) {
              THETAGFILE = selectInputFileDialog(this, ".tag");
          }
          else {
             DO_saveTagFile();
          }
         try {
             FileOutputStream fos = new FileOutputStream (THETAGFILE+".mcs");
             HashMap<String, ArrayList<String>> tagSynsetMap = new HashMap<String, ArrayList<String>>();
             MostCommonSubsumer.loadAnnotationFile(tagSynsetMap, THETAGFILE);
             MostCommonSubsumer.getMostCommonSubsumer(tagSynsetMap, fos);
             fos.close();
 
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public static void main(String[] args) {
         String pathToKafFile = "";
         String pathToTagFile = "";
         String pathToTagSetFile = "";
         TableSettings tableSettings = new TableSettings();
         for (int i = 0; i < args.length; i++) {
             String arg = args[i];
             if ((arg.equalsIgnoreCase("--kaf-file")) && args.length>(i+1)) {
                 pathToKafFile = args[i+1];
             }
             else if ((arg.equalsIgnoreCase("--tag-file")) && args.length>(i+1)) {
                 pathToTagFile = args[i+1];
             }
             else if ((arg.equalsIgnoreCase("--tag-set")) && args.length>(i+1)) {
                 pathToTagSetFile = args[i+1];
             }
             else if (arg.equalsIgnoreCase("--opinion")) {
                OPINION = true;
             }
             else if (arg.equalsIgnoreCase("--synset")) {
                SYNSET = true;
             }
             else if (arg.equalsIgnoreCase("--hide-label")) {
                tableSettings.hideLabel = true;
             }
             else if (arg.equalsIgnoreCase("--hide-pos")) {
                tableSettings.hidePos = true;
             }
             else if (arg.equalsIgnoreCase("--hide-status")) {
                tableSettings.hideStatus = true;
             }
             else if (arg.equalsIgnoreCase("--hide-order")) {
                tableSettings.hideOrder = true;
             }
             else if (arg.equalsIgnoreCase("--hide-term")) {
                tableSettings.hideTerms = true;
             }
             else if (arg.equalsIgnoreCase("--hide-tagid")) {
                tableSettings.hideTagIds = true;
             }
             else if (arg.equalsIgnoreCase("--hide-8")) {
                tableSettings.hideTag8 = true;
             }
             else if (arg.equalsIgnoreCase("--hide-7")) {
                tableSettings.hideTag7 = true;
             }
             else if (arg.equalsIgnoreCase("--hide-6")) {
                tableSettings.hideTag6 = true;
             }
             else if (arg.equalsIgnoreCase("--hide-5")) {
                tableSettings.hideTag5 = true;
             }
             else if (arg.equalsIgnoreCase("--hide-4")) {
                tableSettings.hideTag4 = true;
             }
             else if (arg.equalsIgnoreCase("--hide-3")) {
                tableSettings.hideTag3 = true;
             }
             else if (arg.equalsIgnoreCase("--hide-2")) {
                tableSettings.hideTag2 = true;
             }
         }
         final AnnotatorFrame frame = AnnotatorFrame.getInstance(tableSettings);
 
         if (!pathToKafFile.isEmpty()) {
             System.out.println("pathToKafFile = " + pathToKafFile);
             frame.DO_loadKafFile(pathToKafFile);
         }
         if (!pathToTagFile.isEmpty()) {
             System.out.println("pathToTagFile = " + pathToTagFile);
             frame.DO_loadTagFile(pathToTagFile);
         }
         if (!pathToTagSetFile.isEmpty()) {
             System.out.println("pathToTagSetFile = " + pathToTagSetFile);
             frame.DO_readTagSet(pathToTagSetFile);
         }
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 if (frame.DO_checkSave()==2) {
                     System.exit(0);
                 }
             }
         });
         frame.setIconImage(new ImageIcon("images/kyoto.gif").getImage());        
         frame.setTitle("KAF Annotation Tool");
         frame.pack();
         frame.setVisible(true);
     }
 }
