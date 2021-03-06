 //----------------------------------------------------------------------------
 // $Id$
 //----------------------------------------------------------------------------
 
 package net.sf.gogui.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.Frame;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusAdapter;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.ArrayList;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import net.sf.gogui.go.ConstPointList;
 import net.sf.gogui.go.GoColor;
 import net.sf.gogui.go.GoPoint;
 import net.sf.gogui.go.PointList;
 import net.sf.gogui.gtp.GtpError;
 import net.sf.gogui.gtp.GtpUtil;
 import net.sf.gogui.gui.GuiUtil;
 import net.sf.gogui.util.PrefUtil;
 
 /** Dialog for selecting an AnalyzeCommand. */
 public final class AnalyzeDialog
     extends JDialog
     implements ActionListener, ListSelectionListener
 {
     /** Callback for actions generated by AnalyzeDialog. */
     public interface Listener
     {
         void actionClearAnalyzeCommand();
         
         void actionSetAnalyzeCommand(AnalyzeCommand command, boolean autoRun,
                                      boolean clearBoard, boolean oneRunOnly);
     }
 
     public AnalyzeDialog(Frame owner, Listener listener,
                          ArrayList supportedCommands,
                          String programAnalyzeCommands, GuiGtpClient gtp)
     {
         super(owner, "Analyze");
         m_gtp = gtp;
         m_supportedCommands = supportedCommands;
         m_programAnalyzeCommands = programAnalyzeCommands;
         m_listener = listener;
         Container contentPane = getContentPane();
         JPanel commandPanel = createCommandPanel();
         contentPane.add(commandPanel, BorderLayout.CENTER);
         comboBoxChanged();
         //int minWidth = commandPanel.getPreferredSize().width;
         // not supported in Java 1.4
         //setMinimumSize(new Dimension(minWidth, 192));
         pack();
         addWindowListener(new WindowAdapter() {
                 public void windowActivated(WindowEvent e) {
                     m_comboBoxHistory.requestFocusInWindow();
                 }
             });
     }
 
     public void actionPerformed(ActionEvent event)
     {
         String command = event.getActionCommand();
         if (command.equals("clear"))
             clearCommand();
         else if (command.equals("comboBoxChanged"))
             comboBoxChanged();
         else if (command.equals("run"))
             runCommand();
         else
             assert(false);
     }
 
     public void dispose()
     {
         if (! m_autoRun.isSelected())
             clearCommand();
         saveRecent();
         super.dispose();
     }
 
     public GoColor getSelectedColor()
     {
         if (m_black.isSelected())
             return GoColor.BLACK;
         else
             return GoColor.WHITE;
     }
 
     public void saveRecent()
     {
         final int maxRecent = 100;
         ArrayList recent = new ArrayList(maxRecent);
         int start = (m_firstIsTemp ? 1 : 0);
         for (int i = start; i < start + m_numberNewRecent; ++i)
             recent.add(getComboBoxItem(i));
         for (int i = 0; i < m_fullRecentList.size(); ++i)
         {
             if (recent.size() == maxRecent)
                 break;
             String name = (String)m_fullRecentList.get(i);
             if (recent.indexOf(name) < 0)
                 recent.add(name);
         }
         PrefUtil.putList("net/sf/gogui/gui/analyzedialog/recentcommands",
                          recent);
     }
 
     /** Set board size.
         Need for verifying responses to initial value for EPLIST commands.
         Default is 19.
     */
     public void setBoardSize(int boardSize)
     {
         m_boardSize = boardSize;
     }
 
     public void setSelectedColor(GoColor color)
     {
         m_selectedColor = color;
         selectColor();
     }
 
     public void valueChanged(ListSelectionEvent e)
     {
         int index = m_list.getSelectedIndex();
         if (index >= 0)
             selectCommand(index);
     }
 
     /** Wrapper object for JComboBox items.
         JComboBox can have focus and keyboard navigation problems if
         duplicate String objects are added.
         See JDK 1.4 doc for JComboBox.addItem.
     */
     private static class WrapperObject
     {
         WrapperObject(String item)
         {
             m_item = item;
         }
 
         public String toString()
         {
             return m_item;
         }
 
         private final String m_item;
     }
 
     /** Is the first item in the history combo box a temporary item?
         Avoids that the first item in the history combo box is treated
         as a real history command, if it was not run.
     */
     private boolean m_firstIsTemp;
 
     private int m_boardSize = GoPoint.DEFAULT_SIZE;
 
     /** Serial version to suppress compiler warning.
         Contains a marker comment for serialver.sourceforge.net
     */
     private static final long serialVersionUID = 0L; // SUID
 
     private int m_numberNewRecent;
 
     private ArrayList m_fullRecentList;
 
     private GoColor m_selectedColor = GoColor.EMPTY;
 
     private final GuiGtpClient m_gtp;
 
     private JButton m_clearButton;
 
     private JButton m_runButton;
 
     private JCheckBox m_autoRun;
 
     private JCheckBox m_clearBoard;
 
     private JComboBox m_comboBoxHistory;
 
     private JList m_list;
 
     private Box m_colorBox;
 
     private JRadioButton m_black;
 
     private JRadioButton m_white;
 
     private final ArrayList m_commands = new ArrayList(128);
 
     private final ArrayList m_supportedCommands;
 
     private final ArrayList m_labels = new ArrayList(128);
 
     private final Listener m_listener;
 
     private final String m_programAnalyzeCommands;
 
     private void clearCommand()
     {
         m_listener.actionClearAnalyzeCommand();
         m_autoRun.setSelected(false);
     }
 
     private void comboBoxChanged()
     {
         Object item = m_comboBoxHistory.getSelectedItem();
         if (item == null)
         {
             m_list.clearSelection();
             return;
         }
         String label = item.toString();
         String selectedValue = (String)m_list.getSelectedValue();
         if (selectedValue != null && ! selectedValue.equals(label))
             m_list.clearSelection();
     }
 
     private JPanel createButtons()
     {
         JPanel innerPanel = new JPanel(new GridLayout(1, 0, GuiUtil.PAD, 0));
         m_runButton = new JButton("Run");
         m_runButton.setToolTipText("Run command");
         m_runButton.setActionCommand("run");
         m_runButton.addActionListener(this);
         m_runButton.setMnemonic(KeyEvent.VK_R);
         m_runButton.setEnabled(false);
         getRootPane().setDefaultButton(m_runButton);
         innerPanel.add(m_runButton);
         m_clearButton = new JButton("Clear");
         m_clearButton.setToolTipText("Clear board and cancel auto run");
         m_clearButton.setActionCommand("clear");
         m_clearButton.addActionListener(this);
         m_clearButton.setMnemonic(KeyEvent.VK_C);
         innerPanel.add(m_clearButton);
         JPanel outerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         outerPanel.add(innerPanel);
         return outerPanel;
     }
 
     private JComponent createColorPanel()
     {
         m_colorBox = Box.createVerticalBox();
         ButtonGroup group = new ButtonGroup();
         m_black = new JRadioButton("Black");
         m_black.setToolTipText("Run selected command for color Black");
         m_black.setEnabled(false);
         group.add(m_black);
         m_colorBox.add(m_black);
         m_white = new JRadioButton("White");
         m_white.setToolTipText("Run selected command for color White");
         m_white.setEnabled(false);
         group.add(m_white);
         m_colorBox.add(m_white);
         return m_colorBox;
     }
 
     private JPanel createCommandPanel()
     {
         JPanel panel = new JPanel(new BorderLayout());
         m_list = new JList();
         m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         m_list.setVisibleRowCount(25);
         m_list.addMouseListener(new MouseAdapter() {
                 public void mouseClicked(MouseEvent e) {
                     int modifiers = e.getModifiers();
                     int mask = ActionEvent.ALT_MASK;
                     if (e.getClickCount() == 2
                         || ((modifiers & mask) != 0))
                     {
                         //int index =
                         //   m_list.locationToIndex(event.getPoint());
                         runCommand();
                     }
                 }
             });
         m_list.addFocusListener(new FocusAdapter() {
                 public void focusGained(FocusEvent e) {
                     int index = getSelectedCommand();
                     if (index >= 0)
                         m_list.setSelectedIndex(index);
                 }
             });
         m_list.addListSelectionListener(this);
         JScrollPane scrollPane = new JScrollPane(m_list);
         panel.add(scrollPane, BorderLayout.CENTER);
         panel.add(createLowerPanel(), BorderLayout.SOUTH);
         reload();
         loadRecent();
         return panel;
     }
 
     private JPanel createLowerPanel()
     {
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         panel.add(GuiUtil.createFiller());
         m_comboBoxHistory = new JComboBox();
         panel.add(m_comboBoxHistory);
         JPanel lowerPanel = new JPanel();
         lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));
         lowerPanel.setBorder(GuiUtil.createEmptyBorder());
         panel.add(lowerPanel);
         JPanel optionsPanel
             = new JPanel(new GridLayout(0, 2, GuiUtil.PAD, 0));
         lowerPanel.add(optionsPanel);
         JPanel leftPanel = new JPanel();
         optionsPanel.add(leftPanel);
         Box leftBox = Box.createVerticalBox();
         leftPanel.add(leftBox);
         m_autoRun = new JCheckBox("Auto run");
         m_autoRun.addItemListener(new ItemListener() {
                 public void itemStateChanged(ItemEvent e) {
                     if (! m_autoRun.isSelected())
                         m_listener.actionClearAnalyzeCommand();
                 }
             });
         m_autoRun.setToolTipText("Automatically run after changes on board");
         m_autoRun.setEnabled(false);
         leftBox.add(m_autoRun);
         m_clearBoard = new JCheckBox("Clear board");
         m_clearBoard.setToolTipText("Clear board before displaying result");
         m_clearBoard.setEnabled(false);
         leftBox.add(m_clearBoard);
         m_clearBoard.setSelected(true);
         JPanel rightPanel = new JPanel();
         rightPanel.add(createColorPanel());
         optionsPanel.add(rightPanel);
         lowerPanel.add(createButtons());
         m_comboBoxHistory.addActionListener(this);
         return panel;
     }
 
     private String getComboBoxItem(int i)
     {
         return m_comboBoxHistory.getItemAt(i).toString();
     }
 
     private int getComboBoxItemCount()
     {
         return m_comboBoxHistory.getItemCount();
     }
 
     private int getSelectedCommand()
     {
         Object item = m_comboBoxHistory.getSelectedItem();
         if (item == null)
             return -1;
         return m_labels.indexOf(item.toString());
     }
 
     private void insertComboBoxItem(String label, int index)
     {
         m_comboBoxHistory.insertItemAt(new WrapperObject(label), index);
     }
 
     private void loadRecent()
     {
         m_comboBoxHistory.removeAllItems();
         m_fullRecentList =
             PrefUtil.getList("net/sf/gogui/gui/analyzedialog/recentcommands");
         m_numberNewRecent = 0;
         for (int i = 0; i < m_fullRecentList.size(); ++i)
         {
             String name = (String)m_fullRecentList.get(i);
             if (m_labels.indexOf(name) >= 0)
                 m_comboBoxHistory.addItem(new WrapperObject(name));
             if (m_comboBoxHistory.getItemCount() > 20)
                 break;
         }
         int index = getSelectedCommand();
         if (index >= 0)
             selectCommand(index);
         m_firstIsTemp = false;
     }
 
     private void reload()
     {
         try
         {
             ArrayList supportedCommands = m_supportedCommands;
             AnalyzeCommand.read(m_commands, m_labels, supportedCommands,
                                 m_programAnalyzeCommands);
             m_list.setListData(m_labels.toArray());
             comboBoxChanged();
         }
         catch (Exception e)
         {            
             showError(e.getMessage());
         }
     }
 
     private void runCommand()
     {
         if (m_gtp.isCommandInProgress())
         {
             showError("Cannot execute while computer is thinking", false);
             return;
         }
         int index = getSelectedCommand();
         if (index < 0)
         {
             showError("Command not supported by " + m_gtp.getProgramName(),
                       false);
             return;
         }
         updateRecent(index);
        ++m_numberNewRecent;
         String analyzeCommand = (String)m_commands.get(index);
         AnalyzeCommand command = new AnalyzeCommand(analyzeCommand);
         if (command.needsColorArg())
             command.setColorArg(getSelectedColor());
         String label = command.getResultTitle();        
         if (command.needsStringArg())
         {
             String stringArg = JOptionPane.showInputDialog(this, label);
             if (stringArg == null)
                 return;
             command.setStringArg(stringArg);
         }
         if (command.needsOptStringArg())
         {
             try
             {
                 command.setOptStringArg("");
                 String commandWithoutArg =
                     command.replaceWildCards(m_selectedColor);
                 String value = m_gtp.send(commandWithoutArg);
                 String optStringArg =
                     JOptionPane.showInputDialog(this, label, value);
                 if (optStringArg == null || optStringArg.equals(value))
                     return;
                 command.setOptStringArg(optStringArg);
             }
             catch (GtpError e)
             {
                 showError(e.getMessage());
                 return;
             }
         }
         if (command.getType() == AnalyzeCommand.EPLIST)
         {
             try
             {
                 command.setPointListArg(new PointList());
                 String commandWithoutArg =
                     command.replaceWildCards(m_selectedColor) + " show";
                 String response = m_gtp.send(commandWithoutArg);
                 ConstPointList pointList =
                     GtpUtil.parsePointList(response, m_boardSize);
                 command.setPointListArg(pointList);
             }
             catch (GtpError e)
             {
                 showError(e.getMessage());
                 return;
             }
         }
         if (command.needsFileArg())
         {
             File fileArg = SimpleDialogs.showSelectFile(this, label);
             if (fileArg == null)
                 return;
             command.setFileArg(fileArg);
         }
         if (command.needsFileOpenArg())
         {
             File fileArg = SimpleDialogs.showOpen(this, label);
             if (fileArg == null)
                 return;
             command.setFileOpenArg(fileArg);
         }
         if (command.needsFileSaveArg())
         {
             File fileArg = SimpleDialogs.showSave(this, label);
             if (fileArg == null)
                 return;
             command.setFileSaveArg(fileArg);
         }
         if (command.needsColorArg())
             command.setColorArg(getSelectedColor());
         boolean autoRun = m_autoRun.isEnabled() && m_autoRun.isSelected();
         boolean clearBoard =
             m_clearBoard.isEnabled() && m_clearBoard.isSelected();
         m_listener.actionSetAnalyzeCommand(command, autoRun, clearBoard,
                                            false);
     }
 
     private void selectCommand(int index)
     {
         AnalyzeCommand command =
             new AnalyzeCommand((String)m_commands.get(index));
         boolean needsColorArg = command.needsColorArg();
         m_black.setEnabled(needsColorArg);
         m_white.setEnabled(needsColorArg);
         m_autoRun.setEnabled(command.getType() != AnalyzeCommand.PARAM);
         m_clearBoard.setEnabled(command.getType() != AnalyzeCommand.PARAM);
         m_runButton.setEnabled(true);
         String label = (String)m_labels.get(index);
         m_comboBoxHistory.removeActionListener(this);
         if (m_firstIsTemp && getComboBoxItemCount() > 0)
             m_comboBoxHistory.removeItemAt(0);
         if (getComboBoxItemCount() == 0 || ! getComboBoxItem(0).equals(label))
         {
             insertComboBoxItem(label, 0);
             m_firstIsTemp = true;
             m_comboBoxHistory.setSelectedIndex(0);
         }
         m_comboBoxHistory.addActionListener(this);
     }
 
     private void selectColor()
     {
         if (m_selectedColor == GoColor.BLACK)
             m_black.setSelected(true);
         else if (m_selectedColor == GoColor.WHITE)
             m_white.setSelected(true);
     }
 
     private void showError(String message)
     {
         showError(message, true);
     }
 
     private void showError(String message, boolean isSignificant)
     {
         SimpleDialogs.showError(this, message, isSignificant);
     }
 
     private void updateRecent(int index)
     {
         String label = (String)m_labels.get(index);
        if (getComboBoxItemCount() == 0 || ! getComboBoxItem(0).equals(label))
            insertComboBoxItem(label, 0);
         for (int i = 1; i < getComboBoxItemCount(); ++i)
             if (getComboBoxItem(i).equals(label))
                 m_comboBoxHistory.removeItemAt(i);
         m_comboBoxHistory.setSelectedIndex(0);
         m_firstIsTemp = false;        
     }
 }
