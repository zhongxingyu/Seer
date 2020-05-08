 package ru.javatalks.checkers.gui;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import ru.javatalks.checkers.gui.actions.*;
 import ru.javatalks.checkers.gui.language.L10nBundle;
 import ru.javatalks.checkers.gui.language.LangMenuItems;
 import ru.javatalks.checkers.gui.language.Language;
 import ru.javatalks.checkers.logic.ChessBoardModel;
 import ru.javatalks.checkers.model.Player;
 
 import javax.annotation.PostConstruct;
 import javax.swing.*;
 import java.awt.*;
 
 import static java.lang.System.exit;
 
 /**
  * This class provides GUI
  * 
  * @author Kapellan
  */
 @Component
 public class Dialog {
 
     @Autowired
     private L10nBundle bundle;
 
     @Autowired
     private ChessBoardModel boardModel;
 
     @Autowired
     private ChessBoardPanel chessBoardPanel;
 
     @Autowired
     private CheckBoardMouseListener act;
 
     @Autowired
     private NewGameAction newGameAction;
 
     @Autowired
     private ExitAction exitAction;
 
     @Autowired
     private RulesAction rulesAction;
 
     @Autowired
     private AboutAction aboutAction;
 
     @Autowired
     private ChessBoardModel chessBoardModel;
 
     @Autowired
     private GameFlowController gameFlowController;
 
     @Autowired
     private StepLogger stepLogger;
 
     private LangMenuItems itemLanguage;
 
     private JTextArea tArea;
 
     private JFrame frame;
     private JMenuBar menuBar;
     private JMenu menuGame;
 
     private JMenu menuSettings;
  
     private JMenu menuHelp;
     private JMenuItem itemNewGame;
     private JMenuItem itemExit;
     private JMenuItem itemRules;
 
     private JMenuItem itemAbout;
     private JLabel labelComp;
     private JLabel labelUser;
     private JPanel resultPanel;
 
 
     @PostConstruct
     public void runDialog() {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception ignore) {
         }
 
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 initializeComponent();
             }
         });
     }
 
     private void initializeComponent() {
         tArea = new JTextArea(26, 12);
 
         frame = new JFrame();
         menuBar = new JMenuBar();
 
         menuGame = new JMenu();
         menuSettings = new JMenu();
 
         menuHelp = new JMenu();
 
         labelComp = new JLabel();
         labelUser = new JLabel();
         JScrollPane scrollPane = new JScrollPane(tArea);
         resultPanel = new JPanel();
         JPanel mainPanel = new JPanel(new FlowLayout());
 
         tArea.append(bundle.getString("stepUserText") + '\n');
 
         chessBoardPanel.addMouseListener(act);
 
         itemLanguage = new LangMenuItems(bundle, this);
         menuSettings.add(itemLanguage);
 
         itemNewGame = new JMenuItem(newGameAction);
         itemExit = new JMenuItem(exitAction);
 
         menuGame.add(itemNewGame);
         menuGame.add(itemExit);
 
         itemRules = new JMenuItem(rulesAction);
         itemAbout = new JMenuItem(aboutAction);
 
         menuHelp.add(itemRules);
         menuHelp.add(itemAbout);
 
         menuBar.add(menuGame);
         menuBar.add(menuSettings);
         menuBar.add(menuHelp);
 
         tArea.setFont(new Font("Dialog", Font.PLAIN, 12));
         tArea.setLineWrap(true);
         tArea.setWrapStyleWord(true);
         tArea.setEditable(false);
 
         labelUser.setText(bundle.getString("labelUserTitle") + boardModel.getUserCheckerNumber());
         labelComp.setText(bundle.getString("labelCompTitle") + boardModel.getCompCheckerNumber());
         scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
         scrollPane.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
         labelUser.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
         labelComp.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
 
         LayoutManager boxLayout = new BoxLayout(resultPanel, BoxLayout.Y_AXIS);
         resultPanel.setLayout(boxLayout);
         resultPanel.add(labelUser);
         resultPanel.add(labelComp);
         resultPanel.add(Box.createVerticalStrut(10));
         resultPanel.add(scrollPane);
         resultPanel.add(Box.createVerticalStrut(20));
 
         mainPanel.add(chessBoardPanel);
         mainPanel.add(resultPanel);
 
         setLanguage(bundle.getCurrentLanguage());
         frame.setFocusable(true);
         frame.getRootPane().setOpaque(true);
         frame.getContentPane().setLayout(new BorderLayout());
         frame.getContentPane().add(menuBar, BorderLayout.NORTH);
         frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         frame.setMinimumSize(new Dimension(700, 500));
         frame.pack();
         frame.setLocationRelativeTo(null);
 
         frame.setVisible(true);
     }
 
     public void setLanguage(Language lang) {
         bundle.setLanguage(lang);
 
         frame.setTitle(bundle.getString("frameTitle"));
         menuGame.setText(bundle.getString("gameTitle"));
         menuSettings.setText(bundle.getString("settingsTitle"));
         itemLanguage.setText(bundle.getString("languageTitle"));
         menuHelp.setText(bundle.getString("helpTitle"));
 
         itemNewGame.setText(bundle.getString("newGameTitle"));
         itemExit.setText(bundle.getString("exitTitle"));
         itemRules.setText(bundle.getString("rulesTitle"));
         itemAbout.setText(bundle.getString("aboutTitle"));
         labelComp.setText(bundle.getString("labelCompTitle") + boardModel.getCompCheckerNumber());
         labelUser.setText(bundle.getString("labelUserTitle") + boardModel.getUserCheckerNumber());
     }
 
     void checkGameStatus() {
         labelUser.setText(bundle.getString("labelUserTitle") + boardModel.getUserCheckerNumber());
         labelComp.setText(bundle.getString("labelCompTitle") + boardModel.getCompCheckerNumber());
 
         String[] optionsDialog = {bundle.getString("dialogNewGame"), bundle.getString("dialogExit")};
 
         if (!boardModel.hasCheckerOf(Player.OPPONENT)) {
             notifyAboutGameEnd(optionsDialog, "userWon", "noCompCheckersText");
             return;
         }
 
         if (!boardModel.hasCheckerOf(Player.USER)) {
             notifyAboutGameEnd(optionsDialog, "userLost", "noUserCheckersText");
             return;
         }
 
         if (!boardModel.canStep(Player.OPPONENT)) {
             notifyAboutGameEnd(optionsDialog, "userWon", "compIsBlockedText");
             return;
         }
 
         if (!boardModel.canStep(Player.USER)) {
             notifyAboutGameEnd(optionsDialog, "userLost", "userIsBlockedText");
             return;
         }
     }
 
     private void notifyAboutGameEnd(String[] optionsDialog, String titleKey, String textKey) {
         gameFlowController.stopThread();
         int userChoice = JOptionPane.showOptionDialog(null,
                 bundle.getString(textKey),
                 bundle.getString(titleKey),
                 JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, optionsDialog, optionsDialog[0]);
         
         if (userChoice == JOptionPane.YES_OPTION) {
             restartGame();
         }
         else {
             exit(0);
         }
     }
 
     public void restartGame() {
         chessBoardModel.setInitialState();
         stepLogger.clear();
     }
 
     /**
      * Thread safe set log content
      * @param text
      */
     public void setText(final String text) {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 tArea.setText(text);
             }
         });
     }
 }
