 package ru.jcorp.m2parse.gui;
 
 import org.apache.commons.lang3.StringUtils;
 import ru.jcorp.m2parse.analyze.Modula2Analyzer;
 import ru.jcorp.m2parse.analyze.RPNExecutor;
 import ru.jcorp.m2parse.analyze.SemanticResult;
 import ru.jcorp.m2parse.exceptions.AnalyzeException;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.io.*;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 public class MainFrame extends JFrame {
 
     private static final long serialVersionUID = -5412079065963817486L;
 
     private Action newAction;
     private Action openAction;
     private Action saveAction;
     private Action checkAction;
     private Action contentsAction;
     private Action errorsAction;
 
     private Action aboutAction;
 
     private ResourceBundle resourceBundle;
 
     private JTextArea sourceCodeTextBox;
 
     private JTable variablesTable;
     private JTable constantsTable;
 
     private JLabel cycleCountLabel;
     private JLabel resultLabel;
 
     private JLabel errorLabel;
 
     public MainFrame() {
         Locale locale = Locale.getDefault();
 
         resourceBundle = Utf8ResourceBundle.getBundle("locale.messages", locale);
 
         setTitle(getMessage("mainFrame.title"));
         setMinimumSize(new Dimension(320, 240));
 
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setLayout(new BorderLayout());
         setIconImage(getIcon("android.png").getImage());
 
         createActions();
         createMenuBar();
         createToolBar();
         createComponents();
     }
 
     private void createActions() {
         openAction = new AbstractAction(getMessage("actions.Open"), getIcon("open.png")) {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFileChooser fileChooser = new JFileChooser();
                 if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                     File f = fileChooser.getSelectedFile();
                     try {
                         BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                         StringBuilder builder = new StringBuilder();
                         String line;
                         while ((line = reader.readLine()) != null)
                             builder.append(line).append("\n");
                         sourceCodeTextBox.setText(builder.toString());
                     } catch (FileNotFoundException ignored) {
                     } catch (IOException ignored) {
                     }
                 }
             }
         };
 
         saveAction = new AbstractAction(getMessage("actions.Save"), getIcon("save.png")) {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFileChooser fileChooser = new JFileChooser();
                 if (fileChooser.showSaveDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION) {
                     try {
                         FileOutputStream outputStream = new FileOutputStream(fileChooser.getSelectedFile());
                         PrintWriter writer = new PrintWriter(outputStream);
                         writer.print(sourceCodeTextBox.getText());
                         writer.close();
                         outputStream.close();
                     } catch (FileNotFoundException ignored) {
                     } catch (IOException ignored) {
                     }
                 }
             }
         };
 
         newAction = new AbstractAction(getMessage("actions.New"), getIcon("new.png")) {
             @Override
             public void actionPerformed(ActionEvent e) {
                 sourceCodeTextBox.setText("");
                 resultLabel.setText(getMessage("parser.Success"));
                 errorLabel.setText(" ");
                 constantsTable.setModel(new DefaultTableModel(new Object[0][0], new Object[]{getMessage("parser.Constants")}));
                 variablesTable.setModel(new DefaultTableModel(new Object[0][0], new Object[]{getMessage("parser.Variables")}));
                 cycleCountLabel.setText("");
             }
         };
 
         checkAction = new AbstractAction(getMessage("actions.Check"), getIcon("tick.png")) {
 
             @Override
             public void actionPerformed(ActionEvent e) {
                 String text = sourceCodeTextBox.getText();
 
                 try {
                     Modula2Analyzer analyzer = new Modula2Analyzer();
                     SemanticResult result = analyzer.analyze(text);
 
                     Object[][] variablesData = new Object[result.getVariables().size()][1];
                     Object[] varArray = result.getVariables().toArray();
                     for (int i = 0; i < varArray.length; i++) {
                         variablesData[i][0] = varArray[i];
                     }
                     DefaultTableModel varModel = new DefaultTableModel(variablesData, new Object[]{getMessage("parser.Variables")}) {
                         @Override
                         public boolean isCellEditable(int row, int column) {
                             return false;
                         }
                     };
                     variablesTable.setModel(varModel);
 
                     Object[][] constData = new Object[result.getConstants().size()][1];
                     Object[] constArray = result.getConstants().toArray();
                     for (int i = 0; i < constArray.length; i++) {
                         constData[i][0] = constArray[i];
                     }
                     DefaultTableModel constModel = new DefaultTableModel(constData, new Object[]{getMessage("parser.Constants")}) {
                         @Override
                         public boolean isCellEditable(int row, int column) {
                             return false;
                         }
                     };
                     constantsTable.setModel(constModel);
 
                     resultLabel.setText(getMessage("parser.Success"));
                     errorLabel.setText(" ");
 
                     RPNExecutor executor = new RPNExecutor();
 
                     try {
                         int fromValue = executor.calculate(result.getBeginValue());
                         int toValue = executor.calculate(result.getEndValue());
 
                         int defaultByValue = toValue < fromValue ? -1 : 1;
                         int byValue = StringUtils.isEmpty(result.getDeltaValue()) ? defaultByValue : executor.calculate(result.getDeltaValue());
 
                         if (byValue != 0) {
                             int res = (toValue - fromValue + defaultByValue) / byValue;
                             cycleCountLabel.setText(String.valueOf(res));
                         }
 
                     } catch (AnalyzeException ignored) {
                         cycleCountLabel.setText("-");
                     }
 
                 } catch (AnalyzeException ex) {
                     resultLabel.setText(getMessage("parser.Error"));
 
                     constantsTable.setModel(new DefaultTableModel(new Object[0][0], new Object[]{getMessage("parser.Constants")}));
                     variablesTable.setModel(new DefaultTableModel(new Object[0][0], new Object[]{getMessage("parser.Variables")}));
 
                     String message = getMessage(ex.getMessage());
                     Object[] params = ex.getParams();
                     for (int i = 0; i < params.length; i++) {
                         message = message.replace("{" + String.valueOf(i) + "}", String.valueOf(params[i]));
                     }
                     errorLabel.setText(message);
 
                     cycleCountLabel.setText("");
                 }
             }
         };
 
         aboutAction = new AbstractAction(getMessage("actions.About"), getIcon("question.png")) {
             @Override
             public void actionPerformed(ActionEvent e) {
                 AboutFrame aboutFrame = new AboutFrame();
                 aboutFrame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                 aboutFrame.setModal(true);
                 aboutFrame.setLocationByPlatform(true);
                 aboutFrame.setVisible(true);
             }
         };
 
         contentsAction = new AbstractAction(getMessage("aboutFrame.description"), getIcon("contents.png")) {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ContentsFrame aboutFrame = new ContentsFrame();
                 aboutFrame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                 aboutFrame.setModal(true);
                 aboutFrame.setLocationByPlatform(true);
                 aboutFrame.setVisible(true);
             }
         };
 
         errorsAction = new AbstractAction(getMessage("aboutFrame.errors"), getIcon("bug.png")) {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ErrorsFrame aboutFrame = new ErrorsFrame();
                 aboutFrame.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
                 aboutFrame.setModal(true);
                 aboutFrame.setLocationByPlatform(true);
                 aboutFrame.setVisible(true);
             }
         };
     }
 
     private void createToolBar() {
         JToolBar toolBar = new JToolBar();
         toolBar.setFloatable(false);
 
         toolBar.add(createToolbarButton(openAction));
         toolBar.add(createToolbarButton(saveAction));
         toolBar.add(createToolbarButton(checkAction));
 
         JPanel toolbarPane = new JPanel();
         toolbarPane.setLayout(new BorderLayout());
         toolbarPane.setBorder(new EmptyBorder(0, 3, 0, 3));
         toolbarPane.add(toolBar, BorderLayout.CENTER);
 
         add(toolbarPane, BorderLayout.PAGE_START);
     }
 
     private void createComponents() {
         JPanel mainPane = new JPanel();
         mainPane.setBorder(new EmptyBorder(0, 5, 5, 5));
         mainPane.setLayout(new BorderLayout());
 
         sourceCodeTextBox = new JTextArea();
         sourceCodeTextBox.setBorder(new LineBorder(Color.GRAY, 1));
         sourceCodeTextBox.setFont(new Font("Monospace", Font.BOLD, 14));
         sourceCodeTextBox.setForeground(Color.DARK_GRAY);
         sourceCodeTextBox.setMinimumSize(new Dimension(200, 0));
         sourceCodeTextBox.setPreferredSize(new Dimension(200, 480));
 
         mainPane.add(sourceCodeTextBox, BorderLayout.CENTER);
 
         errorLabel = new JLabel(" ");
         mainPane.add(errorLabel, BorderLayout.PAGE_END);
 
         JPanel resultsPanel = new JPanel();
         resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
         resultsPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
         resultsPanel.setMinimumSize(new Dimension(150, -1));
         resultsPanel.setPreferredSize(new Dimension(200, 200));
 
         resultLabel = new JLabel(getMessage("parser.Success"));
         resultLabel.setForeground(new Color(0x00590E));
         resultLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
 
         resultsPanel.add(resultLabel);
 
         JPanel cycleCountPanel = new JPanel();
         BoxLayout cycleCountLayout = new BoxLayout(cycleCountPanel, BoxLayout.X_AXIS);
         cycleCountPanel.setLayout(cycleCountLayout);
 
         cycleCountLabel = new JLabel();
 
         cycleCountPanel.add(new JLabel(getMessage("parser.cycleCount")));
         cycleCountPanel.add(new JLabel(" "));
         cycleCountPanel.add(cycleCountLabel);
 
         resultsPanel.add(cycleCountPanel);
 
         resultsPanel.add(new JLabel(" "));
 
         variablesTable = new JTable(new Object[0][0], new Object[]{getMessage("parser.Variables")});
         variablesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         JScrollPane variablesPane = new JScrollPane(variablesTable);
         resultsPanel.add(variablesPane);
 
         resultsPanel.add(new JLabel(" "));
 
         constantsTable = new JTable(new Object[0][0], new Object[]{getMessage("parser.Constants")});
         constantsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         JScrollPane constantsPane = new JScrollPane(constantsTable);
         resultsPanel.add(constantsPane);
 
         JScrollPane resultsScroll = new JScrollPane(resultsPanel);
         resultsScroll.setMinimumSize(new Dimension(200, -1));
         resultsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 
         JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
         splitPane.setResizeWeight(0.85);
         splitPane.add(mainPane);
         splitPane.add(resultsScroll);
 
         add(splitPane, BorderLayout.CENTER);
     }
 
     private void createMenuBar() {
         JMenuBar menuBar = new JMenuBar();
 
         JMenu fileMenu = new JMenu(getMessage("actions.File"));
         fileMenu.setMnemonic(getMessage("mnemonic.File").charAt(0));
         fileMenu.setDisplayedMnemonicIndex(0);
 
         JMenuItem newItem = new JMenuItem(newAction);
 
         JMenuItem openItem = new JMenuItem(openAction);
 
         JMenuItem saveItem = new JMenuItem(saveAction);
 
         JMenuItem exitItem = new JMenuItem(new AbstractAction(getMessage("actions.Exit"),
                 getIcon("door.png")) {
             private static final long serialVersionUID = -10251481958599430L;
 
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 MainFrame.this.dispose();
             }
         });
 
         fileMenu.add(newItem);
         fileMenu.add(openItem);
         fileMenu.add(saveItem);
         fileMenu.addSeparator();
         fileMenu.add(exitItem);
         menuBar.add(fileMenu);
 
         JMenu analyzeMenu = new JMenu(getMessage("actions.Analyze"));
         analyzeMenu.setMnemonic(getMessage("mnemonic.Analyze").charAt(0));
         analyzeMenu.setDisplayedMnemonicIndex(0);
 
         JMenuItem checkItem = new JMenuItem(checkAction);
 
         analyzeMenu.add(checkItem);
         menuBar.add(analyzeMenu);
 
         JMenu helpMenu = new JMenu(getMessage("actions.Help"));
         helpMenu.setMnemonic(getMessage("mnemonic.Help").charAt(0));
         helpMenu.setDisplayedMnemonicIndex(0);
 
         JMenuItem aboutItem = new JMenuItem(aboutAction);
         JMenuItem contentsItem = new JMenuItem(contentsAction);
         JMenuItem errorsItem = new JMenuItem(errorsAction);
 
         helpMenu.add(aboutItem);
         helpMenu.add(contentsItem);
         helpMenu.add(errorsItem);
         menuBar.add(helpMenu);
 
         setJMenuBar(menuBar);
     }
 
     private JButton createToolbarButton(Action action) {
         JButton button = new JButton(action);
         button.setHideActionText(true);
         button.setToolTipText((String) action.getValue(Action.NAME));
         return button;
     }
 
     private String getMessage(String key) {
         return resourceBundle.getString(key);
     }
 
     private ImageIcon getIcon(String name) {
         return new ImageIcon(getClass().getResource("/icons/" + name));
     }
 }
