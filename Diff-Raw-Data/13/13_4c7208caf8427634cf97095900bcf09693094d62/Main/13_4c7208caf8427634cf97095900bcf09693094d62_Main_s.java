 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package omnibot;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledDocument;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  *
  * @author pavel
  */
 public class Main extends JFrame implements ActionListener {
 
     WikiApi api;
     List<WikiPage> allPages;
     List<WikiTalkPage> talkpages;
     List<WikiPage> wikipages;
     List<WikiPage> foundPages;
 
     private boolean editTalkPages = false;
 
     private JButton nactiSoubor;
     private JFileChooser fileChooser;
 
     private JComboBox apiURLCombo;
     private JTextField loginName;
     private JPasswordField password;
     private JButton login;
 
     private JButton importCriteriaButton;
     private JButton exportCriteriaButton;
     JCheckBox searchWhileParsing;
     JComboBox searchLimitCombo;
     JSpinner searchOffsetSpinner;
     JCheckBox stringTitleSearchCheckBox;
     JTextField stringTitleSearchTextField;
     JCheckBox stringTitleNegationCheckBox;
     JTextField stringTitleNegationTextField;
     JCheckBox stringSearchCheckBox;
     JTextField stringSearchTextField;
     JCheckBox stringNegationCheckBox;
     JTextField stringNegationTextField;
     JCheckBox regexTitleSearchCheckBox;
     JTextField regexTitleSearchTextField;
     JCheckBox regexTitleNegationCheckBox;
     JTextField regexTitleNegationTextField;
     JCheckBox regexSearchCheckBox;
     JTextField regexSearchTextField;
     JCheckBox regexNegationCheckBox;
     JTextField regexNegationTextField;
     JCheckBox replaceCheckBox;
     JTextArea replaceTextField;
     JCheckBox talkPageNamespaceCheckBox;
     JTextField talkPageNamespaceTextField;
     JCheckBox talkPageCheckBox;
     JCheckBox talkPageMissingCheckBox;
     JCheckBox talkPageStringSearchCheckBox;
     JTextField talkPageStringSearchTextField;
     JCheckBox talkPageStringNegationCheckBox;
     JTextField talkPageStringNegationTextField;
     JCheckBox talkPageRegexSearchCheckBox;
     JTextField talkPageRegexSearchTextField;
     JCheckBox talkPageRegexNegationCheckBox;
     JTextField talkPageRegexNegationTextField;
     JCheckBox talkPageReplaceCheckBox;
     JTextArea talkPageReplaceTextField;
     JCheckBox talkPageCreateCheckBox;
     JTextArea talkPageCreateTextField;
 
     private JButton searchButton;
 
     private JButton pagesSelectAllButton;
     private JButton pagesDeselectAllButton;
     private JScrollPane scrollPane;
     CheckBoxList pageList;
     private JButton exportTitleNamesButton;
     private JButton exportURLLinksButton; //TODO
     private JButton exportResultsButton;
     private JButton exportTalkPageResultsButton;
     private JButton saveDumpButton;
     JTextField summaryTextField;
     JCheckBox minorCheckBox;
     JCheckBox botCheckBox;
     private JButton runButton;
     private JButton runTalkPageButton;
 
     private JFrame confirmEditFrame;
     private JTextField confirmEditTitleTextField;
     private JScrollPane oldScrollPane;
     private JTextPane oldTextPane;
     private JScrollPane newScrollPane;
     private JTextPane newTextPane;
     private JTextField confirmEditSummaryTextField;
     private JButton confirmEditButton;
     private JButton confirmEditCancelButton;
     private JCheckBox confirmEditMinorCheckBox;
     private JCheckBox confirmEditBotCheckBox;
 
     private int confirmEditIndex = 0;
 
     private JFrame exportFrame;
     List<JCheckBox> checkBoxes;
 
     private WikiPage pageToEdit;
 
 
     Main() {
         super("OmniBot");
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
         }
         SwingUtilities.updateComponentTreeUI(this);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setLayout(new GridLayout(1, 2));
         JPanel leftPanel = new JPanel(new GridLayout(29, 1));
 
         //LEFT PANEL
         allPages = new ArrayList<WikiPage>();
         foundPages = new ArrayList<WikiPage>();
 
         JPanel topPanel = new JPanel(new GridLayout(1,2));
         JLabel nactiSouborLabel = new JLabel("Načti databázový dump:");
         topPanel.add(nactiSouborLabel);
         nactiSoubor = new JButton("…");
         nactiSoubor.addActionListener(this);
         nactiSoubor.setActionCommand("dbDumpOpen");
         topPanel.add(nactiSoubor);
         leftPanel.add(topPanel);                                                 //+row in GridLayout
 
         leftPanel.add(new JSeparator(JSeparator.HORIZONTAL));                    //+row in GridLayout
 
         fileChooser = new JFileChooser();
 
         String[] apis = {
             "http://www.wikiskripta.eu/api.php",
             "http://www.wikilectures.eu/api.php",
             "http://cs.wikipedia.org/w/api.php",
             "http://en.wikipedia.org/w/api.php",
             ""
         };
         apiURLCombo = new JComboBox(apis);
         leftPanel.add(apiURLCombo);                                              //+row in GridLayout
         apiURLCombo.setEditable(true);
         JPanel loginPanel = new JPanel(new GridLayout(1,2));
         JLabel loginNameLabel = new JLabel("Uživatelské jméno:");
         loginName = new JTextField(20);
         loginPanel.add(loginNameLabel);
         loginPanel.add(loginName);
         leftPanel.add(loginPanel);                                               //+row in GridLayout
         JPanel passwordPanel = new JPanel(new GridLayout(1,2));
         JLabel passwordLabel = new JLabel("Heslo:");
         password = new JPasswordField(20);
         passwordPanel.add(passwordLabel);
         passwordPanel.add(password);
         leftPanel.add(passwordPanel);                                            //+row in GridLayout
         login = new JButton("Přihlásit se");
         login.addActionListener(this);
         login.setActionCommand("login");
         leftPanel.add(login);                                                    //+row in GridLayout
         leftPanel.add(new JSeparator(JSeparator.HORIZONTAL));                    //+row in GridLayout
 
         //SEARCH
         JPanel importCriteriaPanel = new JPanel(new GridLayout(1, 2));
         importCriteriaButton = new JButton("Importovat kritéria");
         importCriteriaButton.addActionListener(this);
         importCriteriaButton.setActionCommand("importCriteria");
         importCriteriaPanel.add(importCriteriaButton);
         exportCriteriaButton = new JButton("Exportovat kritéria");
         exportCriteriaButton.addActionListener(this);
         exportCriteriaButton.setActionCommand("exportCriteria");
         importCriteriaPanel.add(exportCriteriaButton);
         leftPanel.add(importCriteriaPanel);                                      //+row in GridLayout
 
         searchWhileParsing = new JCheckBox("Hledat již během parsování (pro objemné DB dumpy, např. wikipedie)");
         searchWhileParsing.addActionListener(this);
         searchWhileParsing.setActionCommand("toggleSearchWhileParsing");
         leftPanel.add(searchWhileParsing);                                       //+row in GridLayout
         
         JPanel searchLimitPanel = new JPanel(new GridLayout(1,2));
         JLabel searchLimitLabel = new JLabel("Limit hledaných výsledků:");
         String[] limits = { "10", "100", "200", "500", "1000" };
         searchLimitCombo = new JComboBox(limits);
         searchLimitCombo.setEnabled(false);
         searchLimitPanel.add(searchLimitLabel);
         searchLimitPanel.add(searchLimitCombo);
         leftPanel.add(searchLimitPanel);                                         //+row in GridLayout
 
         JPanel searchOffsetPanel = new JPanel(new GridLayout(1,2));
         JLabel searchOffsetLabel = new JLabel("Přeskočit nalezených výsledků:");
         SpinnerNumberModel offsetModel = new SpinnerNumberModel(0, 0, 1000, 50);
         searchOffsetSpinner = new JSpinner(offsetModel);
         searchOffsetSpinner.setEnabled(false);
         searchOffsetPanel.add(searchOffsetLabel);
         searchOffsetPanel.add(searchOffsetSpinner);
         leftPanel.add(searchOffsetPanel);                                        //+row in GridLayout
 
         JPanel stringTitleSearchPanel = new JPanel(new GridLayout(1, 2));
         stringTitleSearchCheckBox = new JCheckBox("Název obsahuje:");
         stringTitleSearchCheckBox.addActionListener(this);
         stringTitleSearchCheckBox.setActionCommand("stringTitleSearch");
         stringTitleSearchTextField = new JTextField(20);
         stringTitleSearchTextField.setEnabled(false);
         stringTitleSearchPanel.add(stringTitleSearchCheckBox);
         stringTitleSearchPanel.add(stringTitleSearchTextField);
         leftPanel.add(stringTitleSearchPanel);                                   //+row in GridLayout
 
         JPanel stringTitleNegationPanel = new JPanel(new GridLayout(1, 2));
         stringTitleNegationCheckBox = new JCheckBox("Název neobsahuje:");
         stringTitleNegationCheckBox.addActionListener(this);
         stringTitleNegationCheckBox.setActionCommand("stringTitleNegation");
         stringTitleNegationTextField = new JTextField(20);
         stringTitleNegationTextField.setEnabled(false);
         stringTitleNegationPanel.add(stringTitleNegationCheckBox);
         stringTitleNegationPanel.add(stringTitleNegationTextField);
         leftPanel.add(stringTitleNegationPanel);                                 //+row in GridLayout
 
         JPanel regexTitleSearchPanel = new JPanel(new GridLayout(1, 2));
         regexTitleSearchCheckBox = new JCheckBox("Hledaný regulární výraz v názvu:");
         regexTitleSearchCheckBox.addActionListener(this);
         regexTitleSearchCheckBox.setActionCommand("regexTitleSearch");
         regexTitleSearchTextField = new JTextField(20);
         regexTitleSearchTextField.setEnabled(false);
         regexTitleSearchPanel.add(regexTitleSearchCheckBox);
         regexTitleSearchPanel.add(regexTitleSearchTextField);
         leftPanel.add(regexTitleSearchPanel);                                    //+row in GridLayout
 
         JPanel regexTitleNegationPanel = new JPanel(new GridLayout(1, 2));
         regexTitleNegationCheckBox = new JCheckBox("Negativní regulární výraz v názvu:");
         regexTitleNegationCheckBox.addActionListener(this);
         regexTitleNegationCheckBox.setActionCommand("regexTitleNegation");
         regexTitleNegationTextField = new JTextField(20);
         regexTitleNegationTextField.setEnabled(false);
         regexTitleNegationPanel.add(regexTitleNegationCheckBox);
         regexTitleNegationPanel.add(regexTitleNegationTextField);
         leftPanel.add(regexTitleNegationPanel);                                  //+row in GridLayout
 
         JPanel stringSearchPanel = new JPanel(new GridLayout(1, 2));
         stringSearchCheckBox = new JCheckBox("Text článku obsahuje:");
         stringSearchCheckBox.addActionListener(this);
         stringSearchCheckBox.setActionCommand("stringSearch");
         stringSearchTextField = new JTextField(20);
         stringSearchTextField.setEnabled(false);
         stringSearchPanel.add(stringSearchCheckBox);
         stringSearchPanel.add(stringSearchTextField);
         leftPanel.add(stringSearchPanel);                                        //+row in GridLayout
 
         JPanel stringNegationPanel = new JPanel(new GridLayout(1, 2));
         stringNegationCheckBox = new JCheckBox("Text článku neobsahuje:");
         stringNegationCheckBox.addActionListener(this);
         stringNegationCheckBox.setActionCommand("stringNegation");
         stringNegationTextField = new JTextField(20);
         stringNegationTextField.setEnabled(false);
         stringNegationPanel.add(stringNegationCheckBox);
         stringNegationPanel.add(stringNegationTextField);
         leftPanel.add(stringNegationPanel);                                      //+row in GridLayout
 
 
         JPanel regexSearchPanel = new JPanel(new GridLayout(1, 2));
         regexSearchCheckBox = new JCheckBox("Hledaný regulární výraz v textu:");
         regexSearchCheckBox.addActionListener(this);
         regexSearchCheckBox.setActionCommand("regexSearch");
         regexSearchTextField = new JTextField(20);
         regexSearchTextField.setEnabled(false);
         regexSearchPanel.add(regexSearchCheckBox);
         regexSearchPanel.add(regexSearchTextField);
         leftPanel.add(regexSearchPanel);                                         //+row in GridLayout
 
         JPanel regexNegationPanel = new JPanel(new GridLayout(1, 2));
         regexNegationCheckBox = new JCheckBox("Negativní regulární výraz v textu:");
         regexNegationCheckBox.addActionListener(this);
         regexNegationCheckBox.setActionCommand("regexNegation");
         regexNegationTextField = new JTextField(20);
         regexNegationTextField.setEnabled(false);
         regexNegationPanel.add(regexNegationCheckBox);
         regexNegationPanel.add(regexNegationTextField);
         leftPanel.add(regexNegationPanel);                                       //+row in GridLayout
 
         JPanel replacePanel = new JPanel(new GridLayout(1,2));
         replaceCheckBox = new JCheckBox("Zaměnit za:");
         replaceCheckBox.addActionListener(this);
         replaceCheckBox.setActionCommand("replace");
         replaceCheckBox.setEnabled(false);
         replaceTextField = new JTextArea(1, 20);
         replaceTextField.setEnabled(false);
         JScrollPane replaceScrollPane = new JScrollPane(replaceTextField);
         replacePanel.add(replaceCheckBox);
         replacePanel.add(replaceScrollPane);
         leftPanel.add(replacePanel);                                             //+row in GridLayout
 
         JPanel talkPageNamespacePanel = new JPanel(new GridLayout(1, 2));
         talkPageNamespaceCheckBox = new JCheckBox("Oddělovat diskusní stránky ve jmenném prostoru");
         talkPageNamespaceCheckBox.addActionListener(this);
         talkPageNamespaceCheckBox.setActionCommand("talkPageNamespace");
         talkPageNamespaceTextField= new JTextField("Diskuse");
         talkPageNamespaceTextField.setEnabled(false);
         talkPageNamespacePanel.add(talkPageNamespaceCheckBox);
         talkPageNamespacePanel.add(talkPageNamespaceTextField);
         leftPanel.add(talkPageNamespacePanel);                                   //+row in GridLayout
 
         JPanel talkPagePanel = new JPanel(new GridLayout(1, 2));
         talkPageMissingCheckBox = new JCheckBox("Nemá stránku v diskusním jmenném prostoru");
         talkPageMissingCheckBox.addActionListener(this);
         talkPageMissingCheckBox.setActionCommand("talkPageMissing");
         talkPageMissingCheckBox.setEnabled(false);
         talkPageCheckBox = new JCheckBox("Má stránku diskusním jménném prostoru");
         talkPageCheckBox.addActionListener(this);
         talkPageCheckBox.setActionCommand("talkPage");
         talkPageCheckBox.setEnabled(false);
         talkPagePanel.add(talkPageMissingCheckBox);
         talkPagePanel.add(talkPageCheckBox);
         leftPanel.add(talkPagePanel);                                            //+row in GridLayout
 
         JPanel talkPageStringSearchPanel = new JPanel(new GridLayout(1, 2));
         talkPageStringSearchCheckBox = new JCheckBox("Která obsahuje text:");
         talkPageStringSearchCheckBox.addActionListener(this);
         talkPageStringSearchCheckBox.setActionCommand("talkPageStringSearch");
         talkPageStringSearchCheckBox.setEnabled(false);
         talkPageStringSearchTextField = new JTextField(20);
         talkPageStringSearchTextField.setEnabled(false);
         talkPageStringSearchPanel.add(talkPageStringSearchCheckBox);
         talkPageStringSearchPanel.add(talkPageStringSearchTextField);
         leftPanel.add(talkPageStringSearchPanel);                                //+row in GridLayout
 
         JPanel talkPageStringNegationPanel = new JPanel(new GridLayout(1, 2));
         talkPageStringNegationCheckBox = new JCheckBox("Která neobsahuje text:");
         talkPageStringNegationCheckBox.addActionListener(this);
         talkPageStringNegationCheckBox.setActionCommand("talkPageStringNegation");
         talkPageStringNegationCheckBox.setEnabled(false);
         talkPageStringNegationTextField = new JTextField(20);
         talkPageStringNegationTextField.setEnabled(false);
         talkPageStringNegationPanel.add(talkPageStringNegationCheckBox);
         talkPageStringNegationPanel.add(talkPageStringNegationTextField);
         leftPanel.add(talkPageStringNegationPanel);                              //+row in GridLayout
 
         JPanel talkPageRegexSearchPanel = new JPanel(new GridLayout(1, 2));
         talkPageRegexSearchCheckBox = new JCheckBox("Jejíž text vyhovuje regulárnímu výrazu:");
         talkPageRegexSearchCheckBox.addActionListener(this);
         talkPageRegexSearchCheckBox.setActionCommand("talkPageRegexSearch");
         talkPageRegexSearchCheckBox.setEnabled(false);
         talkPageRegexSearchTextField = new JTextField(20);
         talkPageRegexSearchTextField.setEnabled(false);
         talkPageRegexSearchPanel.add(talkPageRegexSearchCheckBox);
         talkPageRegexSearchPanel.add(talkPageRegexSearchTextField);
         leftPanel.add(talkPageRegexSearchPanel);                                 //+row in GridLayout
 
         JPanel talkPageRegexNegationPanel = new JPanel(new GridLayout(1, 2));
         talkPageRegexNegationCheckBox = new JCheckBox("Jejíž text nevyhovuje regulárnímu výrazu:");
         talkPageRegexNegationCheckBox.addActionListener(this);
         talkPageRegexNegationCheckBox.setActionCommand("talkPageRegexNegation");
         talkPageRegexNegationCheckBox.setEnabled(false);
         talkPageRegexNegationTextField = new JTextField(20);
         talkPageRegexNegationTextField.setEnabled(false);
         talkPageRegexNegationPanel.add(talkPageRegexNegationCheckBox);
         talkPageRegexNegationPanel.add(talkPageRegexNegationTextField);
         leftPanel.add(talkPageRegexNegationPanel);                               //+row in GridLayout
 
         JPanel talkPageReplacePanel = new JPanel(new GridLayout(1, 2));
         talkPageReplaceCheckBox = new JCheckBox("Zaměnit za:");
         talkPageReplaceCheckBox.addActionListener(this);
         talkPageReplaceCheckBox.setActionCommand("talkPageReplace");
         talkPageReplaceCheckBox.setEnabled(false);
         talkPageReplaceTextField = new JTextArea(1, 20);
         talkPageReplaceTextField.setEnabled(false);
         JScrollPane talkPageReplaceScrollPane = new JScrollPane(talkPageReplaceTextField);
         talkPageReplacePanel.add(talkPageReplaceCheckBox);
         talkPageReplacePanel.add(talkPageReplaceScrollPane);
         leftPanel.add(talkPageReplacePanel);                                     //+row in GridLayout
 
         JPanel talkPageCreatePanel = new JPanel(new GridLayout(1, 2));
         talkPageCreateCheckBox = new JCheckBox("Vytvořit diskusní stránku s textem:");
         talkPageCreateCheckBox.addActionListener(this);
         talkPageCreateCheckBox.setActionCommand("talkPageCreate");
         talkPageCreateCheckBox.setEnabled(false);
         talkPageCreateTextField = new JTextArea(1, 20);
         talkPageCreateTextField.setEnabled(false);
         JScrollPane talkPageCreateScrollPane = new JScrollPane(talkPageCreateTextField);
         talkPageCreatePanel.add(talkPageCreateCheckBox);
         talkPageCreatePanel.add(talkPageCreateScrollPane);
         leftPanel.add(talkPageCreatePanel);                                      //+row in GridLayout
 
         searchButton = new JButton("Vyhledej");
         searchButton.addActionListener(this);
         searchButton.setActionCommand("search");
         leftPanel.add(searchButton);                                             //+row in GridLayout
 
         add(leftPanel);
 
         //RIGHT PANEL
         JPanel rightPanel = new JPanel();
         rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
 
         JPanel pagesSelectionPanel = new JPanel(new GridLayout(1, 2));
         pagesSelectAllButton = new JButton("Označ všechny");
         pagesSelectAllButton.addActionListener(this);
         pagesSelectAllButton.setActionCommand("pagesSelectAll");
         pagesDeselectAllButton = new JButton("Zruš označení");
         pagesDeselectAllButton.addActionListener(this);
         pagesDeselectAllButton.setActionCommand("pagesDeselectAll");
         pagesSelectionPanel.add(pagesSelectAllButton);
         pagesSelectionPanel.add(pagesDeselectAllButton);
         rightPanel.add(pagesSelectionPanel);
 
         JPanel pagesPanel = new JPanel(new GridLayout(1,1));
         pageList = new CheckBoxList();
         scrollPane = new JScrollPane(pageList);
         pagesPanel.add(scrollPane);
         pagesPanel.setPreferredSize(new Dimension(600, 700));
         rightPanel.add(pagesPanel);
 
         JPanel saveDumpPanel = new JPanel(new GridLayout(1,2));
         JLabel saveDumpLabel = new JLabel("Ulož tyto stránky do dumpu:");
         saveDumpButton = new JButton("…");
         saveDumpButton.addActionListener(this);
         saveDumpButton.setActionCommand("saveDump");
         saveDumpPanel.add(saveDumpLabel);
         saveDumpPanel.add(saveDumpButton);
         rightPanel.add(saveDumpPanel);
 
         JPanel exportPanel = new JPanel(new GridLayout(1, 2));
         exportURLLinksButton = new JButton("Seznam URL");
         exportURLLinksButton.addActionListener(this);
         exportURLLinksButton.setActionCommand("exportURL");
         exportPanel.add(exportURLLinksButton);
         exportTitleNamesButton = new JButton("Seznam názvů stránek");
         exportTitleNamesButton.addActionListener(this);
         exportTitleNamesButton.setActionCommand("exportTitleNames");
         exportPanel.add(exportTitleNamesButton);
         rightPanel.add(exportPanel);
 
         JPanel exportResultsPanel = new JPanel(new GridLayout(1,2));
         exportResultsButton = new JButton("Export výsledků");
         exportResultsButton.addActionListener(this);
         exportResultsButton.setActionCommand("exportResults");
         exportResultsButton.setEnabled(false);
         exportTalkPageResultsButton = new JButton("Export výsledků z diskuzí");
         exportTalkPageResultsButton.addActionListener(this);
         exportTalkPageResultsButton.setActionCommand("exportTalkPageResults");
         exportTalkPageResultsButton.setEnabled(false);
         exportResultsPanel.add(exportResultsButton);
         exportResultsPanel.add(exportTalkPageResultsButton);
         rightPanel.add(exportResultsPanel);
 
 
 
         JPanel bottomPanel = new JPanel(new GridLayout(3,1));
         JPanel summaryPanel = new JPanel(new GridLayout(1,2));
         JLabel summaryLabel = new JLabel("Souhrn editace:");
         summaryTextField = new JTextField(" (via OmniBot)");
         summaryPanel.add(summaryLabel);
         summaryPanel.add(summaryTextField);
         JPanel minorPanel = new JPanel(new GridLayout(1, 2));
         minorCheckBox = new JCheckBox("Malá editace");
         minorCheckBox.setSelected(true);
         minorPanel.add(minorCheckBox);
         botCheckBox = new JCheckBox("Editovat jako bot");
         botCheckBox.setSelected(true);
         bottomPanel.add(minorPanel);
         bottomPanel.add(botCheckBox);
         JPanel runPanel = new JPanel(new GridLayout(1, 2));
         runButton = new JButton("Spustit");
         runButton.addActionListener(this);
         runButton.setActionCommand("run");
         runButton.setSize(searchButton.getSize());
         runTalkPageButton = new JButton("Spustit pro diskusní stránky");
         runTalkPageButton.addActionListener(this);
         runTalkPageButton.setActionCommand("runTalkPage");
         runPanel.add(runButton);
         runPanel.add(runTalkPageButton);
         bottomPanel.add(summaryPanel);
         bottomPanel.add(runPanel);
         rightPanel.add(bottomPanel);
         add(rightPanel);
 
         setSize(1200, 800);
     }
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Main frame = new Main();
         frame.setVisible(true);
     }
 
     boolean doAllCriteriaApply(WikiPage wikipage) {
 
         //criteria:
         boolean allCriteriaApply = false;
         boolean titleDoesNotContain, titleRegexDoesNotContain;
         boolean titleContains, titleRegexContains;
         boolean textContains, textDoesNotContain, textRegexContains, textRegexDoesNotContain;
         boolean hasTalkPage, doesNotHaveTalkPage;
         boolean talkPageContains, talkPageDoesNotContain, talkPageRegexContains, talkPageRegexDoesNotContain;
 
 
         //which criteria to apply:
         if (stringSearchTitleApplied()) {
             titleContains = wikipage.titleContains(stringTitleSearchTextField.getText());
         } else {
             titleContains = true; //criterion not specified, do not apply
         }
         if (regexSearchTitleApplied()) {
             titleRegexContains = wikipage.titleRegexContains(regexTitleSearchTextField.getText());
         } else {
             titleRegexContains = true; //criterion not specified, do not apply
         }
         if ( stringTitleNegationTextField.isEnabled() && (!stringTitleNegationTextField.getText().isEmpty()) ) {
             titleDoesNotContain = wikipage.titleDoesNotContain(stringTitleNegationTextField.getText());
         } else {
             titleDoesNotContain = true; //criterion not specified, do not apply
         }
         if ( regexTitleNegationTextField.isEnabled() && (!regexTitleNegationTextField.getText().isEmpty()) ) {
             titleRegexDoesNotContain = wikipage.titleRegexDoesNotContain(regexTitleNegationTextField.getText());
         } else {
             titleRegexDoesNotContain = true; //criterion not specified, do not apply
         }
         if (stringSearchTextApplied()) {
             textContains = wikipage.contains(stringSearchTextField.getText());
         } else {
             textContains = true; //criterion not specified, do not apply
         }
         if ( stringNegationTextField.isEnabled() && (!stringNegationTextField.getText().isEmpty()) ) {
             textDoesNotContain = wikipage.doesNotContain(stringNegationTextField.getText());
         } else {
             textDoesNotContain = true; //criterion not specified, do not apply
         }
         if (regexSearchTextApplied()) {
             textRegexContains = wikipage.regexContains(regexSearchTextField.getText());
         } else {
             textRegexContains = true; //criterion not specified, do not apply
         }
         if ( regexNegationTextField.isEnabled() && (!regexNegationTextField.getText().isEmpty()) ) {
             textRegexDoesNotContain = wikipage.regexDoesNotContain(regexNegationTextField.getText());
         } else {
             textRegexDoesNotContain = true; //criterion not specified, do not apply
         }
         if (talkPageCheckBox.isSelected()) {
             hasTalkPage = wikipage.hasTalkPage();
         } else {
             hasTalkPage = true; //criterion not specified, do not apply
         }
         if (talkPageMissingCheckBox.isSelected()) {
             doesNotHaveTalkPage = (!wikipage.hasTalkPage());
         } else {
             doesNotHaveTalkPage = true; //criterion not specified, do not apply
         }
         if ( talkPageStringSearchTextField.isEnabled() && (!talkPageStringSearchTextField.getText().isEmpty()) ) {
             talkPageContains = wikipage.talkPageContains(talkPageStringSearchTextField.getText());
         } else {
             talkPageContains = true; //criterion not specified, do not apply
         }
         if ( talkPageStringNegationTextField.isEnabled() && (!talkPageStringNegationTextField.getText().isEmpty()) ) {
             talkPageDoesNotContain = wikipage.talkPageDoesNotContain(talkPageStringNegationTextField.getText());
         } else {
             talkPageDoesNotContain = true; //criterion not specified, do not apply
         }
         if ( talkPageRegexSearchTextField.isEnabled() && (!talkPageRegexSearchTextField.getText().isEmpty()) ) {
             talkPageRegexContains = wikipage.talkPageRegexContain(talkPageRegexSearchTextField.getText());
         } else {
             talkPageRegexContains = true; //criterion not specified, do not apply
         }
         if ( talkPageRegexNegationTextField.isEnabled() && (!talkPageRegexNegationTextField.getText().isEmpty()) ) {
             talkPageRegexDoesNotContain = wikipage.talkPageRegexDoesNotContain(talkPageRegexNegationTextField.getText());
         } else {
             talkPageRegexDoesNotContain = true; //criterion not specified, do not apply
         }
 
         //try if all criteria are set to true:
         allCriteriaApply = (titleContains && titleRegexContains &&
                 titleDoesNotContain && titleRegexDoesNotContain &&
                 textContains && textDoesNotContain && textRegexContains && textRegexDoesNotContain &&
                 hasTalkPage && doesNotHaveTalkPage &&
                 talkPageContains && talkPageDoesNotContain && talkPageRegexContains && talkPageDoesNotContain);
 
         return allCriteriaApply;
     }
     
     private void selectAllPages() {
         for (int i = 0; i < pageList.getModel().getSize(); i++) {
             JCheckBox cb = (JCheckBox) pageList.getModel().getElementAt(i);
             cb.setSelected(true);
         }
         pageList.repaint();
     }
     private void deselectAllPages() {
         for (int i = 0; i < pageList.getModel().getSize(); i++) {
             JCheckBox cb = (JCheckBox) pageList.getModel().getElementAt(i);
             cb.setSelected(false);
         }
         pageList.repaint();
     }
     void showError(Exception e) {
         JOptionPane.showMessageDialog(this, e.getLocalizedMessage(), "Chyba!", JOptionPane.ERROR_MESSAGE);
     }
     private void exportCriteria(File f) {
         try {
             DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
             Document doc = docBuilder.newDocument();
             Element root = doc.createElement("criteria");
             doc.appendChild(root);
 
             Element el;
             el = doc.createElement("searchWhileParsing");
             if (searchWhileParsing.isSelected()) {
                 el.setAttribute("apply", "yes");
                 Element searchLimit = doc.createElement("searchLimit");
                 String searchLimitValue = (String) searchLimitCombo.getModel().getSelectedItem();
                 searchLimit.appendChild(doc.createTextNode(searchLimitValue));
                 el.appendChild(searchLimit);
                 Element searchOffset = doc.createElement("searchOffset");
                 Integer searchOffsetValue = (Integer) searchOffsetSpinner.getModel().getValue();
                 searchOffset.appendChild(doc.createTextNode(searchOffsetValue.toString()));
                 el.appendChild(searchOffset);
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("titleString");
             if (stringTitleSearchCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(stringTitleSearchTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("titleNegationString");
             if (stringTitleNegationCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(stringTitleNegationTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("titleRegex");
             if (regexTitleSearchCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(regexTitleSearchTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("titleNegationRegex");
             if (regexTitleNegationCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(regexTitleNegationTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("textString");
             if (stringSearchCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(stringSearchTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             el = doc.createElement("textNegationString");
             if (stringNegationCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(stringNegationTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             root.appendChild(el);
             el = doc.createElement("textRegex");
             if (regexSearchCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(regexSearchTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("textNegationRegex");
             if (regexNegationCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(regexNegationTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("textReplace");
             if (replaceCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(replaceTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("talkpageNamespace");
             if (talkPageNamespaceCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(talkPageNamespaceTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("talkpageDoesNotExist");
             if (talkPageMissingCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("talkpageExists");
             if (talkPageCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("talkpageString");
             if (talkPageStringSearchCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(talkPageStringSearchTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("talkpageNegationString");
             if (talkPageStringNegationCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(talkPageStringNegationTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("talkpageRegex");
             if (talkPageRegexSearchCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(talkPageRegexSearchTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("talkpageNegationRegex");
             if (talkPageRegexNegationCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(talkPageRegexNegationTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("talkpageReplace");
             if (talkPageReplaceCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(talkPageReplaceTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("talkpageCreate");
             if (talkPageCreateCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
                 el.appendChild(doc.createTextNode(talkPageCreateTextField.getText()));
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
             el = doc.createElement("summary");
             el.appendChild(doc.createTextNode(summaryTextField.getText()));
             root.appendChild(el);
             el = doc.createElement("minor");
             if (minorCheckBox.isSelected()) {
                 el.setAttribute("apply", "yes");
             } else {
                 el.setAttribute("apply", "no");
             }
             root.appendChild(el);
 
 
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
             DOMSource source = new DOMSource(doc);
             StreamResult streamResult = new StreamResult(f);
             //StreamResult streamResult = new StreamResult(System.out); //for testing only
             transformer.transform(source, streamResult);
 
             JOptionPane.showMessageDialog(this, "Kritéria uložena!", "OK", JOptionPane.INFORMATION_MESSAGE);
         } catch (Exception e) {
             showError(e);
         }
     }
     private void saveWSDump(File f) {
         try {
             DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
             Document doc = docBuilder.newDocument();
             Element rootElement = doc.createElement("mediawiki");
             doc.appendChild(rootElement);
 
             rootElement.setAttribute("xmlns", "http://www.mediawiki.org/xml/export-0.5/");
             rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
             rootElement.setAttribute("xsi:schemaLocation", "http://www.mediawiki.org/xml/export-0.5/ http://www.mediawiki.org/xml/export-0.5.xsd");
             rootElement.setAttribute("version", "0.5");
             rootElement.setAttribute("xml:lang", "cs");
 
             Element siteinfo = doc.createElement("siteinfo");
             rootElement.appendChild(siteinfo);
             Element sitename = doc.createElement("sitename");
             sitename.appendChild(doc.createTextNode("WikiSkripta"));
             siteinfo.appendChild(sitename);
             Element base = doc.createElement("base");
             base.appendChild(doc.createTextNode("http://www.wikiskripta.eu/index.php/Home"));
             siteinfo.appendChild(base);
             Element caseElement = doc.createElement("case");
             caseElement.appendChild(doc.createTextNode("first-letter"));
             siteinfo.appendChild(caseElement);
 
             Element namespaces = doc.createElement("namespaces");
             Element namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "-2");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Média"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "-1");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Speciální"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "0");
             namespace.setAttribute("case", "first-letter");
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "1");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Diskuse"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "2");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Uživatel"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "3");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Diskuse s uživatelem"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "4");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("WikiSkripta"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "5");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Diskuse k WikiSkriptům"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "6");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Soubor"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "7");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Diskuse k souboru"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "8");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("MediaWiki"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "9");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Diskuse k MediaWiki"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "10");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Šablona"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "11");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Diskuse k šabloně"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "12");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Nápověda"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "13");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Diskuse k nápovědě"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "14");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Kategorie"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "15");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Diskuse ke kategorii"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "100");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Portál"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "101");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Portál diskuse"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "102");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Fórum"));
             namespaces.appendChild(namespace);
 
             namespace = doc.createElement("namespace");
             namespace.setAttribute("key", "103");
             namespace.setAttribute("case", "first-letter");
             namespace.appendChild(doc.createTextNode("Fórum diskuse"));
             namespaces.appendChild(namespace);
             siteinfo.appendChild(namespaces);
 
             WikiPage wikipage;
             Element page, title, id, revision, revisionId, revisionTimestamp,
                     revisionContributor, contributorUsername, contributorId,
                     minor, comment, text;
             for (Iterator<WikiPage> it = foundPages.iterator(); it.hasNext(); ) {
                 wikipage = it.next();
                 if (isPageSelected(wikipage.getTitle())) {
                     page = doc.createElement("page");
                     title = doc.createElement("title");
                     title.appendChild(doc.createTextNode(wikipage.getTitle()));
                     page.appendChild(title);
                     id = doc.createElement("id");
                     id.appendChild(doc.createTextNode(wikipage.getPageId()));
                     page.appendChild(id);
                     revision = doc.createElement("revision");
                     revisionId = doc.createElement("id");
                     revisionId.appendChild(doc.createTextNode(wikipage.getRevisionId()));
                     revision.appendChild(revisionId);
                     revisionTimestamp = doc.createElement("timestamp");
                     revisionTimestamp.appendChild(doc.createTextNode(wikipage.getRevisionTimestamp()));
                     revision.appendChild(revisionTimestamp);
                     revisionContributor = doc.createElement("contributor");
                     contributorUsername = doc.createElement("username");
                     contributorUsername.appendChild(doc.createTextNode(wikipage.getRevisionContributor()));
                     revisionContributor.appendChild(contributorUsername);
                     contributorId = doc.createElement("id");
                     contributorId.appendChild(doc.createTextNode(wikipage.getRevisionContributorId()));
                     revisionContributor.appendChild(contributorId);
                     revision.appendChild(revisionContributor);
                     if (wikipage.isMinor()) {
                         minor = doc.createElement("minor");
                         revision.appendChild(minor);
                     }
                     comment = doc.createElement("comment");
                     comment.appendChild(doc.createTextNode(wikipage.getRevisionComment()));
                     revision.appendChild(comment);
                     text = doc.createElement("text");
                     text.appendChild(doc.createTextNode(wikipage.getPageText()));
                     revision.appendChild(text);
                     page.appendChild(revision);
                     rootElement.appendChild(page);
                 }
             }
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
             DOMSource source = new DOMSource(doc);
             StreamResult streamResult = new StreamResult(f);
             //StreamResult streamResult = new StreamResult(System.out); //for testing only
             transformer.transform(source, streamResult);
 
             JOptionPane.showMessageDialog(this, "Dump uložen!", "OK", JOptionPane.INFORMATION_MESSAGE);
 
 
         } catch (Exception e) {
             showError(e);;
         }
 
 
     }
     private boolean isPageSelected(String pageTitle) {
         JCheckBox checkbox;
 
         for (int i = 0; i < pageList.getModel().getSize(); i++) {
             checkbox = (JCheckBox) pageList.getModel().getElementAt(i);
             if (checkbox.getText().equals(pageTitle)) {
                 return checkbox.isSelected();
             }
         }
         return false;
     }
     private void confirmEdit() {
         pageToEdit = getNextSelectedPage();
         WikiTalkPage talkpage;
         String pageText = "";
         String title = "";
         String timestamp = "";
         String talkpageNamespace = talkPageNamespaceTextField.getText();
         if (!talkpageNamespace.contains(":")) {
             talkpageNamespace = talkpageNamespace + ":";
         }
 
         if (pageToEdit != null) {
             if (editTalkPages) {
                 talkpage = pageToEdit.getTalkpage();
                 if (talkpage != null) {
                     title = talkpage.getTitle();
                     pageText = talkpage.getPageText();
                 } else {
                     title = talkpageNamespace + pageToEdit.getTitle();
                     pageText = "";
                 }
             } else {
                 title = pageToEdit.getTitle();
                 pageText = pageToEdit.getPageText();
             }
             if (api != null) {
                 WikiPage apiPage = api.revision(pageToEdit.getTitle());
                 if (!apiPage.equalsTo(pageToEdit)) {
                     //TODO
                     JOptionPane.showMessageDialog(this, "Stránka byla od vytvoření databázového dumpu změněna. Ruším editaci.", "Pozor!", JOptionPane.INFORMATION_MESSAGE);
                     confirmEditFrame.setVisible(false);
                     return;
                 }
             } else {
                 JOptionPane.showMessageDialog(this, "Nejste přihlášeni k API! Pracujete offline!", "Pozor!", JOptionPane.ERROR_MESSAGE);
             }
 
             confirmEditFrame = new JFrame("Potvrďte editaci…");
             confirmEditFrame.setSize(1200, 800);
             confirmEditFrame.setLayout(new BoxLayout(confirmEditFrame.getContentPane(), BoxLayout.PAGE_AXIS));
             JPanel confirmEditTitlePanel = new JPanel(new GridLayout(1, 2));
             JLabel confirmEditTitleLabel = new JLabel("Název:");
             confirmEditTitleTextField = new JTextField(title);
             confirmEditTitleTextField.setEditable(false);
             confirmEditTitlePanel.add(confirmEditTitleLabel);
             confirmEditTitlePanel.add(confirmEditTitleTextField);
             confirmEditFrame.add(confirmEditTitlePanel);
 
             JPanel timestampPanel = new JPanel(new GridLayout(1, 2));
             JLabel timestampLabel = new JLabel("Timestamp článku:");
             JTextField timestampTextField = new JTextField(pageToEdit.getRevisionTimestamp());
             timestampTextField.setEditable(false);
             timestampPanel.add(timestampLabel);
             timestampPanel.add(timestampTextField);
             confirmEditFrame.add(timestampPanel);
 
             if (editTalkPages) {
                 JPanel talkpageTimestampPanel = new JPanel(new GridLayout(1, 2));
                 JLabel talkpageTimestampLabel = new JLabel("Timestamp diskusní stránky:");
                 JTextField talkpageTimestampTextField = new JTextField();
                 if (pageToEdit.getTalkpage() != null) {
                     talkpageTimestampTextField.setText(pageToEdit.getTalkpage().getRevisionTimestamp());
                 } else {
                     talkpageTimestampTextField.setText("-");
                 }
                 talkpageTimestampTextField.setEditable(false);
                 talkpageTimestampPanel.add(talkpageTimestampLabel);
                 talkpageTimestampPanel.add(talkpageTimestampTextField);
                 confirmEditFrame.add(talkpageTimestampPanel);
             }
 
             JPanel confirmEditChangesPanel = new JPanel(new GridLayout(1, 1));
             JLabel confirmEditChangesLabel = new JLabel("Změny");
             confirmEditChangesPanel.add(confirmEditChangesLabel);
             confirmEditFrame.add(confirmEditChangesPanel);
 
             JPanel confirmEditTextPanePanel = new JPanel(new GridLayout(1, 2));
             confirmEditTextPanePanel.setSize(1200, 600);
             oldTextPane = new JTextPane();
             oldTextPane.setEditable(false);
 
 
             StyledDocument oldDoc = oldTextPane.getStyledDocument();
             Style regular = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
             Style highlighted = oldDoc.addStyle("highlighted", regular);
             StyleConstants.setBackground(highlighted, Color.yellow);
             StyleConstants.setForeground(highlighted, Color.red);
             try {
                 StyledTexts styledTexts = null;
                 if (editTalkPages && talkPageStringSearchApplied()) {
                     styledTexts = styledTextsFromStringSearch(pageText, talkPageStringSearchTextField.getText());
                 } else if (editTalkPages && talkPageRegexSearchApplied()) {
                     styledTexts = StyledTextsFromRegexSearch(pageText, talkPageRegexSearchTextField.getText());
                 } else if ((!editTalkPages) && stringSearchTextApplied()) {
                     styledTexts = styledTextsFromStringSearch(pageText, stringSearchTextField.getText());
                 } else if ((!editTalkPages) && regexSearchTextApplied()) {
                     styledTexts = StyledTextsFromRegexSearch(pageText, regexSearchTextField.getText());
                 } else {
                     oldDoc.insertString(oldDoc.getLength(), pageText, regular);                        
                 }
                 if (styledTexts != null) {
                     StyledText styledText = styledTexts.getStyledText();
                     while (styledText != null) {
                         oldDoc.insertString(oldDoc.getLength(), styledText.getText(), oldTextPane.getStyle(styledText.getStyleName()));
                         styledText = styledTexts.getStyledText();
                     }
                 }
             } catch (Exception e) {
                 showError(e);
             }
             newTextPane = new JTextPane();
             newTextPane.setEditable(true);
             StyledDocument newDoc = newTextPane.getStyledDocument();
             try {
                 String searchString = "";
                 String replacement = "";
                 if (editTalkPages && talkPageReplaceTextApplied()) {
                     if (talkPageStringSearchTextField.isEnabled() && (!talkPageStringSearchTextField.getText().isEmpty()) ) {
                         searchString = talkPageStringSearchTextField.getText();
                         replacement = talkPageReplaceTextField.getText();
                     } else if (talkPageRegexSearchTextField.isEnabled() && (!talkPageRegexSearchTextField.getText().isEmpty()) ) {
                         searchString = talkPageRegexSearchTextField.getText();
                         replacement = talkPageReplaceTextField.getText();
                     } else {
                         JOptionPane.showMessageDialog(this, "Musíte vybrat, který text má být nahrazen!", "Pozor!", JOptionPane.ERROR_MESSAGE);
                     }
                 } else if (editTalkPages && talkPageCreateApplied()) {
                     pageText += talkPageCreateTextField.getText();
                 } else if (replaceTextApplied()) {
                     if (stringSearchTextField.isEnabled() && (!stringSearchTextField.getText().isEmpty()) ) {
                         searchString = stringSearchTextField.getText();
                         replacement = replaceTextField.getText();
                     } else if (regexSearchTextField.isEnabled() && (!regexSearchTextField.getText().isEmpty()) ) {
                         searchString = regexSearchTextField.getText();
                         replacement = replaceTextField.getText();
                     } else {
                         JOptionPane.showMessageDialog(this, "Musíte vybrat, který text má být nahrazen!", "Pozor!", JOptionPane.ERROR_MESSAGE);
                     }
                 }
                 pageText = pageText.replaceAll(searchString, replacement);
                 newDoc.insertString(newDoc.getLength(), pageText, StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE));
 
             } catch (Exception e) {
                 showError(e);
             }
             oldScrollPane = new JScrollPane(oldTextPane);
             newScrollPane = new JScrollPane(newTextPane);
             confirmEditTextPanePanel.add(oldScrollPane);
             confirmEditTextPanePanel.add(newScrollPane);
             confirmEditFrame.add(confirmEditTextPanePanel);
 
             JPanel confirmEditSummaryPanel = new JPanel(new GridLayout(1, 2));
             JLabel confirmEditSummaryLabel = new JLabel("Souhrn editace");
             confirmEditSummaryTextField = new JTextField(summaryTextField.getText());
             confirmEditSummaryPanel.add(confirmEditSummaryLabel);
             confirmEditSummaryPanel.add(confirmEditSummaryTextField);
             confirmEditFrame.add(confirmEditSummaryPanel);
 
             JPanel confirmEditMinorPanel = new JPanel(new GridLayout(1, 2));
             confirmEditMinorCheckBox = new JCheckBox("Malá editace");
             confirmEditMinorCheckBox.setSelected(minorCheckBox.isSelected());
             confirmEditMinorPanel.add(confirmEditMinorCheckBox);
             confirmEditBotCheckBox = new JCheckBox("Editovat jako bot");
             confirmEditBotCheckBox.setSelected(botCheckBox.isSelected());
             confirmEditMinorPanel.add(confirmEditBotCheckBox);
             confirmEditFrame.add(confirmEditMinorPanel); //TODO checkMinor on main Frame
 
             JPanel confirmEditButtonPanel = new JPanel(new GridLayout(1, 2));
             confirmEditButton = new JButton("OK");
             confirmEditButton.addActionListener(this);
             confirmEditButton.setActionCommand("editConfirmed");
             confirmEditCancelButton = new JButton("Zrušit");
             confirmEditCancelButton.addActionListener(this);
             confirmEditCancelButton.setActionCommand("editCanceled");
             confirmEditButtonPanel.add(confirmEditButton);
             confirmEditButtonPanel.add(confirmEditCancelButton);
             confirmEditFrame.add(confirmEditButtonPanel);
 
 
             confirmEditFrame.setVisible(true);
         } else {
                 JOptionPane.showMessageDialog(this, "Není další stránka, která by měla být editována.", "Konec!", JOptionPane.INFORMATION_MESSAGE);
         }
         
     }
 
     private boolean stringSearchTextApplied() {
         if (stringSearchTextField.isEnabled() && (!stringSearchTextField.getText().isEmpty()) ) {
             return true;
         } else {
             return false;
         }
     }
     private boolean regexSearchTextApplied() {
         if (regexSearchTextField.isEnabled() && (!regexSearchTextField.getText().isEmpty()) ) {
             return true;
         } else {
             return false;
         }
     }
     private boolean replaceTextApplied() {
         if (replaceTextField.isEnabled()) {
             //not testing emptiness of the textField, because you can make blank replacement
             return true;
         } else {
             return false;
         }
     }
     private boolean stringSearchTitleApplied() {
         if ( stringTitleSearchTextField.isEnabled() && (!stringTitleSearchTextField.getText().isEmpty()) ) {
             return true;
         } else {
             return false;
         }
     }
     private boolean regexSearchTitleApplied() {
         if ( regexTitleSearchTextField.isEnabled() && (!regexTitleSearchTextField.getText().isEmpty())) {
             return true;
         } else {
             return false;
         }
     }
     private boolean talkPageStringSearchApplied() {
         if (talkPageStringSearchTextField.isEnabled() && (!talkPageStringSearchTextField.getText().isEmpty()) ) {
             return true;
         } else {
             return false;
         }
     }
     private boolean talkPageRegexSearchApplied() {
         if (talkPageRegexSearchTextField.isEnabled() && (!talkPageRegexSearchTextField.getText().isEmpty()) ) {
             return true;
         } else {
             return false;
         }
     }
     private boolean talkPageReplaceTextApplied() {
         if (talkPageReplaceCheckBox.isEnabled()) {
             //not testing emptiness of the textField, because you can make blank replacement
             return true;
         } else {
             return false;
         }
     }
     private boolean talkPageCreateApplied(){
         if (talkPageCreateTextField.isEnabled() && (!talkPageCreateTextField.getText().isEmpty())) {
             return true;
         } else {
             return false;
         }
     }
 
     private void enableTalkPageRegexSearchSettings() {
         talkPageRegexSearchTextField.setEnabled(true);
         talkPageReplaceCheckBox.setEnabled(true);
         exportTalkPageResultsButton.setEnabled(true);
         talkPageStringSearchCheckBox.setEnabled(false);
         talkPageStringSearchTextField.setEnabled(false);
         //talkPageStringNegationCheckBox.setEnabled(false);
         //talkPageStringNegationTextField.setEnabled(false);
     }
     private void disableTalkPageRegexSearchSettings() {
         talkPageRegexSearchTextField.setEnabled(false);
         exportTalkPageResultsButton.setEnabled(false);
         talkPageStringSearchCheckBox.setEnabled(true);
         //talkPageStringNegationCheckBox.setEnabled(true);
         talkPageReplaceCheckBox.setEnabled(false);
     }
     private void enableTalkPageStringSearchSettings() {
         talkPageStringSearchTextField.setEnabled(true);
         talkPageReplaceCheckBox.setEnabled(true);
         talkPageRegexSearchCheckBox.setEnabled(false);
         talkPageRegexSearchTextField.setEnabled(false);
         //talkPageRegexNegationCheckBox.setEnabled(false);
         //talkPageRegexNegationTextField.setEnabled(false);
     }
     private void disableTalkPageStringSearchSettings() {
         talkPageStringSearchTextField.setEnabled(false);
         talkPageRegexSearchCheckBox.setEnabled(true);
         //talkPageRegexNegationCheckBox.setEnabled(true);
         talkPageReplaceCheckBox.setEnabled(false);
     }
     private void enableSearchWhileParsingSettings() {
         searchLimitCombo.setEnabled(true);
         searchOffsetSpinner.setEnabled(true);
         talkPageNamespaceCheckBox.setEnabled(false);
         talkPageNamespaceTextField.setEnabled(false);
         talkPageMissingCheckBox.setSelected(false);
         talkPageMissingCheckBox.setEnabled(false);
         talkPageCheckBox.setSelected(false);
         talkPageCheckBox.setEnabled(false);
     }
     private void enableStringTextSearchSettings() {
         stringSearchTextField.setEnabled(true);
         regexSearchCheckBox.setEnabled(false);
         regexSearchTextField.setEnabled(false);
         regexNegationCheckBox.setEnabled(false);
         regexNegationTextField.setEnabled(false);
         replaceCheckBox.setEnabled(true);
     }
     private void disableStringTextSearchSettings() {
         stringSearchTextField.setEnabled(false);
         regexSearchCheckBox.setEnabled(true);
         regexSearchTextField.setEnabled(true);
         regexNegationCheckBox.setEnabled(true);
         regexNegationTextField.setEnabled(true);
         replaceCheckBox.setEnabled(false);
         replaceTextField.setEnabled(false);
     }
     private void enableRegexTextSearchSettings() {
         regexSearchTextField.setEnabled(true);
         exportResultsButton.setEnabled(true);
         stringSearchCheckBox.setEnabled(false);
         stringSearchTextField.setEnabled(false);
         stringNegationCheckBox.setEnabled(false);
         stringNegationTextField.setEnabled(false);
         replaceCheckBox.setEnabled(true);
     }
     private void disableRegexTextSearchSettings() {
         regexSearchTextField.setEnabled(false);
         exportResultsButton.setEnabled(false);
         stringSearchCheckBox.setEnabled(true);
         stringNegationCheckBox.setEnabled(true);
         replaceCheckBox.setEnabled(false);
         replaceTextField.setEnabled(false);
     }
     private void enableTalkPageNamespaceSettings() {
         talkPageNamespaceTextField.setEnabled(true);
         talkPageCheckBox.setEnabled(true);
         talkPageMissingCheckBox.setEnabled(true);
     }
     private void disableTalkPageNamespaceSettings() {
         talkPageNamespaceTextField.setEnabled(false);
         talkPageCheckBox.setEnabled(false);
         talkPageMissingCheckBox.setEnabled(false);
         disableTalkPageSettings();
     }
     private void enableTalkPageSettings() {
         talkPageStringSearchCheckBox.setEnabled(true);
         talkPageStringNegationCheckBox.setEnabled(true);
         talkPageRegexSearchCheckBox.setEnabled(true);
         talkPageRegexNegationCheckBox.setEnabled(true);
         talkPageReplaceCheckBox.setEnabled(true);
     }
     private void disableTalkPageSettings() {
         talkPageStringSearchCheckBox.setEnabled(false);
         talkPageStringSearchTextField.setEnabled(false);
         talkPageStringNegationCheckBox.setEnabled(false);
         talkPageStringNegationTextField.setEnabled(false);
         talkPageRegexSearchCheckBox.setEnabled(false);
         talkPageRegexSearchTextField.setEnabled(false);
         talkPageRegexNegationCheckBox.setEnabled(false);
         talkPageRegexNegationTextField.setEnabled(false);
         talkPageReplaceCheckBox.setEnabled(false);
         talkPageReplaceTextField.setEnabled(false);
         talkPageCreateCheckBox.setEnabled(false);
         talkPageCreateTextField.setEnabled(false);
     }
 
     private StyledTexts styledTextsFromStringSearch (String pageText, String searchString) {
                 String[] textParts = pageText.split(searchString);
                 StyledTexts styledTexts = new StyledTexts();
                 if (pageText.startsWith(searchString)) {
                     //the text starts with the searchString, add the styled searchString to the begging
                     styledTexts.addStyledText(searchString, "highlighted");
                 }
                 //after every textpart added add styled searchString also
                 for (int i = 0; i < textParts.length; i++) {
                     styledTexts.addStyledText(textParts[i], "regular");
 
                     if (i < (textParts.length - 1)) { //otherwise it's the last piece added, no need to add highlighted searchString at the end
                         styledTexts.addStyledText(searchString, "highlighted");
                     }
                 }
 
                 if (pageText.endsWith(searchString)) {
                     //the text ends with the searchString, add the styled searchString to the end
                     styledTexts.addStyledText(searchString, "highlighted");
                 }
                 return styledTexts;
     }
     private StyledTexts StyledTextsFromRegexSearch (String pageText, String regex) {
         String stringPart;
         Pattern pattern = Pattern.compile(regex);
         Matcher matcher = pattern.matcher(pageText);
         StyledTexts styledTexts = new StyledTexts();
         int index, previousIndex = 0;
         while (matcher.find()) {
             index = matcher.start();
             stringPart = pageText.substring(previousIndex, index);
             styledTexts.addStyledText(stringPart, "regular");
             styledTexts.addStyledText(matcher.group(), "highlighted");
             previousIndex = matcher.end();
         }
         //add text following the last match
         stringPart = pageText.substring(previousIndex);
         styledTexts.addStyledText(stringPart, "regular");
         return styledTexts;
     }
     private void makeEdit() {
         api.editPage(
                 pageToEdit,
                 newTextPane.getText(),
                "Pokus: Blablabla",
                 confirmEditMinorCheckBox.isSelected(),
                 confirmEditBotCheckBox.isSelected()
         );
         JCheckBox checkBox = (JCheckBox) pageList.getModel().getElementAt(confirmEditIndex);
         checkBox.setSelected(false);
     }
     private WikiPage getPage(String pageTitle) {
         WikiPage page;
         for (Iterator<WikiPage> it = foundPages.iterator(); it.hasNext(); ) {
             page = it.next();
             if (page.getTitle().equals(pageTitle)) {
                 return page;
             }
         }
         return null;
     }
     private WikiPage getNextSelectedPage() {
         int max = pageList.getModel().getSize();
         if (confirmEditIndex >= max) {
             //every element of the list has been already returned
             confirmEditIndex = 0;
             return null;
         }
 
         JCheckBox checkbox = (JCheckBox) pageList.getModel().getElementAt(confirmEditIndex);
         while (!checkbox.isSelected()) {
             confirmEditIndex++;
             if (confirmEditIndex >= max) {
                 //every element of the list has been already returned
                 confirmEditIndex = 0;
                 return null;
             }
             checkbox = (JCheckBox) pageList.getModel().getElementAt(confirmEditIndex);
         }
         return getPage(checkbox.getText());
     }
     public void exportURL() {
         String wsUrls = "";
         String wlUrls = "";
         String twUrls = "";
         String enUrls = "";
         String csUrls = "";
         JCheckBox checkBox;
         for (int i = 0; i < pageList.getModel().getSize(); i++) {
             checkBox = (JCheckBox) pageList.getModel().getElementAt(i);
             if (checkBox.isSelected()) {
                 wsUrls += "http://www.wikiskripta.eu/index.php/" + checkBox.getText().replaceAll(" ", "_") + "\n";
                 wlUrls += "http://www.wikilectures.eu/index.php/" + checkBox.getText().replaceAll(" ", "_") + "\n";
                 twUrls += "http://test-wiki2.lf1.cuni.cz/index.php/" + checkBox.getText().replaceAll(" ", "_") + "\n";
                 enUrls += "http://en.wikipedia.org/wiki/" + checkBox.getText().replaceAll(" ", "_") + "\n";
                 csUrls += "http://cs.wikipedia.org/wiki/" + checkBox.getText().replaceAll(" ", "_") + "\n";
             }
         }
         String[] texts = {wsUrls, wlUrls, twUrls, enUrls, csUrls};
         showExport(texts);
     }
     public void exportTitleNames() {
         String wikilinks = "";
         String pagenames = "";
         JCheckBox checkBox;
         for (int i = 0; i < pageList.getModel().getSize(); i++) {
             checkBox = (JCheckBox) pageList.getModel().getElementAt(i);
             if (checkBox.isSelected()) {
                 if (pagenames.isEmpty()) {
                     pagenames = checkBox.getText();
                 } else {
                     pagenames += "|" + checkBox.getText();
                 }
                 wikilinks += "* [[" + checkBox.getText() + "]]\n";
             }
         }
         String[] texts = {wikilinks, pagenames};
         showExport(texts);
     }
     private boolean isTalkpage(WikiPage page) {
         String title = page.getTitle();
         String talkpageNamespace = talkPageNamespaceTextField.getText();
         if (!talkpageNamespace.contains(":")) {
             talkpageNamespace += ":";
         }
         if (title.startsWith(talkpageNamespace)) {
             return true;
         } else {
             return false;
         }
     }
     private WikiTalkPage getTalkpage(String title) {
         String talkpageNamespace = talkPageNamespaceTextField.getText();
         if (!talkpageNamespace.contains(":")) {
             talkpageNamespace += ":";
         }
         WikiTalkPage talkpage;
         for (Iterator<WikiTalkPage> it = talkpages.iterator(); it.hasNext(); ) {
             talkpage = it.next();
             if (talkpage.getTitle().equals(talkpageNamespace + title)) {
                 return talkpage;
             }
         }
         return null;
     }
     private void exportResults() {
         String results = "";
         String regexResults = "";
         JCheckBox checkBox;
         WikiPage page;
         String result;
         String regex = regexSearchTextField.getText();
         for (int i = 0; i < pageList.getModel().getSize(); i++) {
             checkBox = (JCheckBox) pageList.getModel().getElementAt(i);
             if (checkBox.isSelected()) {
                 page = getPage(checkBox.getText());
                 result = page.getRegexGroup(regex);
                 if (result != null && results.isEmpty()) {
                     results = result;
                     regexResults = result;
                 }
                 if (result != null && (!results.isEmpty())) {
                     results += "\n" + result;
                     regexResults = "|" + result;
                 }
 
             }
         }
         String[] texts = {results, regexResults};
         showExport(texts);
     }
     private void exportTalkPageResults() {
         String results = "";
         String regexResults = "";
         JCheckBox checkBox;
         WikiPage page;
         String result;
         String regex = regexSearchTextField.getText();
         for (int i = 0; i < pageList.getModel().getSize(); i++) {
             checkBox = (JCheckBox) pageList.getModel().getElementAt(i);
             if (checkBox.isSelected()) {
                 page = getPage(checkBox.getText());
                 result = page.getRegexGroup(regex);
                 if (result != null && results.isEmpty()) {
                     results = result;
                     regexResults = result;
                 }
                 if (result != null && (!results.isEmpty())) {
                     results += "\n" + result;
                     regexResults = "|" + result;
                 }
 
             }
         }
         String[] texts = {results, regexResults};
         showExport(texts);
     }
     private void showExport(String[] texts) {
         exportFrame = new JFrame("Export");
         exportFrame.setLayout(new BoxLayout(exportFrame.getContentPane(), BoxLayout.PAGE_AXIS));
         JTextArea area;
         JScrollPane scrollPane;
         for (int i = 0; i < texts.length; i++) {
             area = new JTextArea(texts[i]);
             scrollPane = new JScrollPane(area);
             exportFrame.add(scrollPane);
         }
         JButton zavrit = new JButton("Zavřít");
         zavrit.addActionListener(this);
         zavrit.setActionCommand("zavritExport");
         exportFrame.add(zavrit);
         exportFrame.setSize(1200, 800);
         exportFrame.setVisible(true);
 
     }
 
     private void sortPages() {
         wikipages = new ArrayList<WikiPage>();
         talkpages = new ArrayList<WikiTalkPage>();
 
         WikiPage page;
         for (Iterator<WikiPage> it = allPages.iterator(); it.hasNext(); ) {
             page = it.next();
             if (isTalkpage(page)) {
                 talkpages.add(new WikiTalkPage(page));
             } else {
                 wikipages.add(page);
             }
         }
 
         for (Iterator<WikiPage> it = wikipages.iterator(); it.hasNext(); ) {
             page = it.next();
             page.setTalkpage(getTalkpage(page.getTitle()));
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         try {
             if (e.getActionCommand().equals("dbDumpOpen")) {
                 int returnVal = fileChooser.showOpenDialog(this);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     File file = fileChooser.getSelectedFile();
                     this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                     if (searchWhileParsing.isSelected()) {
                         String searchLimitValue = (String) searchLimitCombo.getSelectedItem();
                         int searchLimit = Integer.parseInt(searchLimitValue);
                         int searchOffset = ((Integer) searchOffsetSpinner.getModel().getValue()).intValue();
                         DBDumpSAX dumpParser = new DBDumpSAX(file.getPath(), this, true, searchLimit, searchOffset);
                     } else {
                         DBDumpSAX dumpParser = new DBDumpSAX(file.getPath(), this);
                     }
                     this.setCursor(Cursor.getDefaultCursor());
                 }
             }
             if (e.getActionCommand().equals("importCriteria")) {
                 int returnVal = fileChooser.showOpenDialog(this);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     File file = fileChooser.getSelectedFile();
                     CriteriaParser cp = new CriteriaParser(file.getPath(), this);
                     cp.applyCriteria();
                 }
             }
             if (e.getActionCommand().equals("exportCriteria")) {
                 int returnVal = fileChooser.showSaveDialog(this);
                 if (returnVal == JFileChooser.APPROVE_OPTION) {
                     File file = fileChooser.getSelectedFile();
                     exportCriteria(file);
                 }
             }
             if (e.getActionCommand().equals("saveDump")) {
 
             String selectedApiUrl = (String) apiURLCombo.getSelectedItem();
                 if (selectedApiUrl.equals("http://www.wikiskripta.eu/api.php")) {
                     int returnVal = fileChooser.showSaveDialog(this);
                     if (returnVal == JFileChooser.APPROVE_OPTION) {
                         File file = fileChooser.getSelectedFile();
                         saveWSDump(file);
                     }
                 } else {
                     //TODO ukládání dumpu z jiných api
                     JOptionPane.showMessageDialog(this, "Ukládání dumpů zatím pouze WikiSkript, jiné projekty nejsou dostupné.", "Chyba!", JOptionPane.ERROR_MESSAGE);
                 }
             }
             if (e.getActionCommand().equals("exportURL")) {
                 exportURL();
             }
             if (e.getActionCommand().equals("exportTitleNames")) {
                 exportTitleNames();
             }
             if (e.getActionCommand().equals("pagesSelectAll")) {
                 selectAllPages();
             }
             if (e.getActionCommand().equals("pagesDeselectAll")) {
                 deselectAllPages();
             }
 
             if (e.getActionCommand().equals("login")) {
                 api = new WikiApi((String) apiURLCombo.getSelectedItem(), this);
                 api.login(loginName.getText(), password.getPassword());
             }
             if (e.getActionCommand().equals("talkPageCreate")) {
                 if (talkPageCreateCheckBox.isSelected()) {
                     talkPageCreateTextField.setEnabled(true);
                 } else {
                     talkPageCreateTextField.setEnabled(false);
                 }
             }
             if (e.getActionCommand().equals("talkPageReplace")) {
                 if (talkPageReplaceCheckBox.isSelected()) {
                     talkPageReplaceTextField.setEnabled(true);
                 } else {
                     talkPageReplaceTextField.setEnabled(false);
                 }
             }
             if (e.getActionCommand().equals("talkPageRegexNegation")) {
                 if (talkPageRegexNegationCheckBox.isSelected()) {
                     talkPageStringNegationCheckBox.setEnabled(false);
                     talkPageStringNegationTextField.setEnabled(false);
                     talkPageRegexNegationTextField.setEnabled(true);
                 } else {
                     talkPageRegexNegationTextField.setEnabled(false);
                     talkPageStringNegationCheckBox.setEnabled(true);
                 }
             }
             if (e.getActionCommand().equals("talkPageRegexSearch")) {
                 if (talkPageRegexSearchCheckBox.isSelected()) {
                     enableTalkPageRegexSearchSettings();
                 } else {
                     disableTalkPageRegexSearchSettings();
                 }
             }
 
             if (e.getActionCommand().equals("talkPageStringNegation")) {
                 if (talkPageStringNegationCheckBox.isSelected()) {
                     talkPageStringNegationTextField.setEnabled(true);
                     talkPageRegexNegationCheckBox.setEnabled(false);
                     talkPageRegexNegationTextField.setEnabled(false);
                 } else {
                     talkPageRegexNegationCheckBox.setEnabled(true);
                     talkPageStringNegationTextField.setEnabled(false);
                 }
             }
             if (e.getActionCommand().equals("talkPageStringSearch")) {
                 if (talkPageStringSearchCheckBox.isSelected()) {
                     enableTalkPageStringSearchSettings();
                 } else {
                     disableTalkPageStringSearchSettings();
                 }
             }
             if (e.getActionCommand().equals("talkPageNamespace")) {
                 if (talkPageNamespaceCheckBox.isSelected()) {
                     enableTalkPageNamespaceSettings();
                 } else {
                     disableTalkPageNamespaceSettings();
                 }
             }
             if (e.getActionCommand().equals("talkPageMissing")) {
                 if (talkPageMissingCheckBox.isSelected()) {
                     talkPageCreateCheckBox.setEnabled(true);
                     talkPageCheckBox.setEnabled(false);
                 } else {
                     talkPageCreateCheckBox.setEnabled(false);
                     talkPageCreateTextField.setEnabled(false);
                     talkPageCheckBox.setEnabled(true);
                 }
             }
             if (e.getActionCommand().equals("talkPage")) {
                 if (talkPageCheckBox.isSelected()) {
                     enableTalkPageSettings();
                     talkPageMissingCheckBox.setEnabled(false);
 
                 } else {
                     disableTalkPageSettings();
                     talkPageMissingCheckBox.setEnabled(true);
                 }
             }
             if (e.getActionCommand().equals("stringNegation")) {
                 if (stringNegationCheckBox.isSelected()) {
                     stringNegationTextField.setEnabled(true);
                     if (!regexSearchCheckBox.isSelected() && !stringSearchCheckBox.isSelected()) {
                         replaceTextField.setEnabled(false);
                         replaceCheckBox.setEnabled(false);
                     }
                 } else {
                     stringNegationTextField.setEnabled(false);
                     if (regexSearchCheckBox.isSelected() || stringSearchCheckBox.isSelected()) {
                         replaceCheckBox.setEnabled(true);
                     }
                 }
             }
             if (e.getActionCommand().equals("regexNegation")) {
                 if (regexNegationCheckBox.isSelected()) {
                     regexNegationTextField.setEnabled(true);
                     if (!regexSearchCheckBox.isSelected() && !stringSearchCheckBox.isSelected()){
                         replaceTextField.setEnabled(false);
                         replaceCheckBox.setEnabled(false);
                     }
                 } else {
                     regexNegationTextField.setEnabled(false);
                     if (regexSearchCheckBox.isSelected() || stringSearchCheckBox.isSelected()) {
                         replaceCheckBox.setEnabled(true);
                     }
                 }
             }
             if (e.getActionCommand().equals("stringSearch")) {
                 if (stringSearchCheckBox.isSelected()) {
                     enableStringTextSearchSettings();
                 } else {
                     disableStringTextSearchSettings();
                 }
             }
             if (e.getActionCommand().equals("regexSearch")) {
                 if (regexSearchCheckBox.isSelected()) {
                     enableRegexTextSearchSettings();
                 } else {
                     disableRegexTextSearchSettings();
                 }
             }
             if (e.getActionCommand().equals("regexTitleNegation")) {
                 if (regexTitleNegationCheckBox.isSelected()) {
                     regexTitleNegationTextField.setEnabled(true);
                     stringTitleNegationTextField.setEnabled(false);
                     stringTitleNegationTextField.setEnabled(false);
                 } else {
                     regexTitleNegationTextField.setEnabled(false);
                     stringTitleNegationCheckBox.setEnabled(true);
                 }
             }
             if (e.getActionCommand().equals("stringTitleNegation")) {
                 if (stringTitleNegationCheckBox.isSelected()) {
                     stringTitleNegationTextField.setEnabled(true);
                     regexTitleNegationTextField.setEnabled(false);
                     regexTitleNegationCheckBox.setEnabled(false);
                 } else {
                     stringTitleNegationTextField.setEnabled(false);
                     regexTitleNegationCheckBox.setEnabled(true);
                 }
             }
             if (e.getActionCommand().equals("stringTitleSearch")) {
                 if (stringTitleSearchCheckBox.isSelected()) {
                     stringTitleSearchTextField.setEnabled(true);
                     regexTitleSearchTextField.setEnabled(false);
                     regexTitleSearchCheckBox.setEnabled(false);
                 } else {
                     stringTitleSearchTextField.setEnabled(false);
                     regexTitleSearchCheckBox.setEnabled(true);
                 }
             }
             if (e.getActionCommand().equals("regexTitleSearch")) {
                 if (regexTitleSearchCheckBox.isSelected()) {
                     regexTitleSearchTextField.setEnabled(true);
                     stringTitleSearchCheckBox.setEnabled(false);
                     stringTitleSearchTextField.setEnabled(false);
                 } else {
                     regexTitleSearchTextField.setEnabled(false);
                     stringTitleSearchCheckBox.setEnabled(true);
                 }
             }
             if (e.getActionCommand().equals("toggleSearchWhileParsing")) {
                 if (searchWhileParsing.isSelected()) {
                     enableSearchWhileParsingSettings();
                 } else {
                     searchLimitCombo.setEnabled(false);
                     searchOffsetSpinner.setEnabled(false);
                     talkPageNamespaceCheckBox.setEnabled(true);
                 }
 
             }
             if (e.getActionCommand().equals("replace")) {
                 if (replaceCheckBox.isSelected()) {
                     replaceTextField.setEnabled(true);
                 } else {
                     replaceTextField.setEnabled(false);
                 }
             }
             if (e.getActionCommand().equals("search")) {
                 this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
                 checkBoxes = new ArrayList<JCheckBox>();
 
                 WikiPage page;
                 Iterator<WikiPage> it;
 
                 if (talkPageNamespaceCheckBox.isSelected()) {
                     sortPages();
                     it = wikipages.iterator();
                 } else {
                     it = allPages.iterator();
                 }
                 for (; it.hasNext();) {
                     page = it.next();
 
                     if (doAllCriteriaApply(page)) {
                         checkBoxes.add(new JCheckBox(page.getTitle()));
                         foundPages.add(page);
                     }
                 }
                 pageList.setListData(checkBoxes.toArray());
                 this.setCursor(Cursor.getDefaultCursor());
             }
             if (e.getActionCommand().equals("editConfirmed")) {
                 makeEdit();
                 confirmEditFrame.setVisible(false);
                 confirmEdit();
             }
             if (e.getActionCommand().equals("editCanceled")) {
                 confirmEditFrame.setVisible(false);
                 JOptionPane.showMessageDialog(this, "Editace byla zrušena.", "Editace zrušena!", JOptionPane.INFORMATION_MESSAGE);
                 confirmEdit();
             }
             if (e.getActionCommand().equals("run")) {
                 editTalkPages = false;
                 confirmEdit();
             }
             if (e.getActionCommand().equals("runTalkPage")) {
                 editTalkPages = true;
                 confirmEdit();
             }
             if (e.getActionCommand().equals("zavritExport")) {
                 exportFrame.setVisible(false);
             }
             if (e.getActionCommand().equals("exportResults")) {
                 exportResults();
             }
             if (e.getActionCommand().equals("exportTalkPageResults")) {
                 exportTalkPageResults();
             }
         } catch (Exception excp) {
             showError(excp);
             setCursor(Cursor.getDefaultCursor());
         }
     }
 }
 
 class StyledTexts {
     List<StyledText> styledTexts;
     int styledTextsIndex = 0;
     StyledTexts() {
         styledTexts = new ArrayList<StyledText>();
     }
     void addStyledText(String text, String styleName) {
         styledTexts.add(new StyledText(text, styleName));
     }
     StyledText getStyledText() {
         if (styledTextsIndex >= styledTexts.size()) {
             return null;
         }
         StyledText st = styledTexts.get(styledTextsIndex);
         styledTextsIndex++;
         return st;
     }
 
 }
 class StyledText {
     String text;
     String styleName;
 
     StyledText (String text, String styleName) {
         this.text = text;
         this.styleName = styleName;
     }
     public String getText() {
         return this.text;
     }
     public String getStyleName() {
         return this.styleName;
     }
 
 }
