 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.examples.aut.adder.swing.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.JTree;
 import javax.swing.SwingConstants;
 
 /**
  * The frame of the Application Under Test.
  * 
  * @author BREDEX GmbH
  * @created 20.07.2004
  */
 public class AutFrame extends JFrame {
     /**
      * <code>BLACK_JACK_MODE</code> ... somewhat special related to black jack 
      */
     public static final int BLACK_JACK_MODE = 0;
     /**
      * <code>DEEP_THOUGHT_MODE</code> ... thinking about 42
      */
     public static final int DEEP_THOUGHT_MODE = 1;
     /**
      * <code>STRICT_CALC_MODE</code>
      */
     public static final int STRICT_CALC_MODE = 2;
     
     /**
      * <code>m_calculationMode</code>
      */
     private int m_calculationMode = BLACK_JACK_MODE;
     
     /**
      * Calculator panel
      */
     private AbstractCalculatorPanel m_calculaorPanel = null;
     
     /**
      * CheckBox for switching between integer and float mode
      */
     private JCheckBox m_floatCheckBox = null;
     /**
      * The options tree.
      */
     private JTree m_optionsTree;
     /**
      * The options tree scrollpane.
      */
     private JScrollPane m_optionsTreePane;
     /**
      * The options table.
      */
     private JTable m_optionsTable;
     /**
      * The options table scrollpane.
      */
     private JScrollPane m_optionsTablePane;
     /**
      * The precision-combobox.
      */
     private JComboBox m_precisionComboBox;
 
     /**
      * GridBagLayout
      */
     private GridBagLayout m_gbl = null;
 
     /**
      * container
      */
     private Container m_cont = null;
     /**
      * Menu item to quit the application.
      */
     private JMenuItem m_quit = new JMenuItem("quit"); //$NON-NLS-1$
     /**
      * Menu item to reset the GUI.
      */
     private JMenuItem m_reset = new JMenuItem("reset"); //$NON-NLS-1$
     /**
      * Menu item to show the about dialog.
      */
     private JMenuItem m_about = new JMenuItem("about"); //$NON-NLS-1$
 
     /**
      * when set to false, AUT is started in Simple Mode
      * otherwise in Advanced Mode
      */
     private boolean m_complexMode;
 
     /**
      * Constructor.
      * @param hasAlternativeLayout <code>true</code> to create a frame with the
      * alternative Layout
      */
     public AutFrame(boolean hasAlternativeLayout) {
         initialize(hasAlternativeLayout);
     }
     /**
      * Constructor of AutFrame Sets the title of the frame and initialize the
      * gui.
      * 
      * @param title
      *            A <code>String</code> value.
      * @param mode
      *            A <code>boolean</code> value.
      * @param hasAlternativeLayout <code>true</code> to create a frame with the
      * alternative Layout
      */
     public AutFrame(String title, boolean mode, boolean hasAlternativeLayout) {
         super(title);
         m_complexMode = mode;
         initialize(hasAlternativeLayout);
     }
 
     /**
      * 
      * @return Returns the floatCheckBox.
      */
     public JCheckBox getFloatCheckBox() {
         if (m_floatCheckBox == null) {
             createFloatCheckBox();
         }
         return m_floatCheckBox;
     }
     /**
      * initialize1 - split of initialize for checkstyle reasons
      */
    private void initialize1() {
         setBackground(Color.white);
        //Layout setzen und Komponenten hinzuf�gen
         m_gbl = new GridBagLayout();
         m_cont = getContentPane();
         m_cont.setLayout(m_gbl);
         GridBagConstraints gbc;
         createMenu();
 
         // Calculator panel
         gbc = LayoutUtil.makegbc(0, 3, 3, 1);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         m_cont.add(m_calculaorPanel, gbc);
         if (m_complexMode) {
             // Separator
             JSeparator sep2 = new JSeparator();
             sep2.setOrientation(SwingConstants.HORIZONTAL);
             gbc = LayoutUtil.makegbc(0, 4, 3, 1);
             gbc.weightx = 100;
             gbc.fill = GridBagConstraints.HORIZONTAL;
             m_gbl.setConstraints(sep2, gbc);
             m_cont.add(sep2);
 
             // float CheckBox
             gbc = LayoutUtil.makegbc(0, 5, 1, 1);
             gbc.fill = GridBagConstraints.HORIZONTAL;
             JCheckBox checkbox = getFloatCheckBox();
             m_gbl.setConstraints(checkbox, gbc);
             m_cont.add(checkbox);
 
             // Precision ComboBox
             gbc = LayoutUtil.makegbc(1, 5, 1, 1);
             gbc.weightx = 0.0;
             gbc.fill = GridBagConstraints.NONE;
             gbc.anchor = GridBagConstraints.WEST;
             m_gbl.setConstraints(getPrecisionComboBox(), gbc);
             m_cont.add(getPrecisionComboBox());
             
             gbc = LayoutUtil.makegbc(2, 5, 1, 1);
             gbc.weightx = 1.0;
             gbc.fill = GridBagConstraints.HORIZONTAL;
             m_cont.add(new JPanel(), gbc);
         
             // Options tree
             gbc = LayoutUtil.makegbc(0, 6, 3, 1);
             gbc.weightx = 1.0;
             gbc.weighty = 3.0;
             gbc.fill = GridBagConstraints.BOTH;
             m_gbl.setConstraints(getOptionsTreePane(), gbc);
             m_cont.add(getOptionsTreePane());
         
             // Options table
             gbc = LayoutUtil.makegbc(0, 7, 3, 1);
             gbc.weightx = 1.0;
             gbc.weighty = 3.0;
             gbc.fill = GridBagConstraints.BOTH;
             m_gbl.setConstraints(getOptionsTablePane(), gbc);
             m_cont.add(getOptionsTablePane());
         }
     }
     /**
      * initialize2 -- split of initialize only for checkstyle reasons
      */
    private void initialize2() {
         GridBagConstraints gbc;
         
         // Radio buttons controlling the calculation mode
         gbc = LayoutUtil.makegbc(0, 8, 3, 1);
         gbc.weightx = 1.0;
         gbc.weighty = 1.0;
         gbc.fill = GridBagConstraints.BOTH;
         Component radioButtonPane = createRadioButtonsPane();
         m_gbl.setConstraints(radioButtonPane, gbc);
         m_cont.add(radioButtonPane);
     }
     
     /**
     * Initialize the Gui
     * @param hasAlternativeLayout <code>true</code> to create a frame with the
     * alternative Layout
      */
     private void initialize(boolean hasAlternativeLayout) {
         if (hasAlternativeLayout) {
             m_calculaorPanel = new AlternativeCalculatorPanel();
         } else {
             m_calculaorPanel = new CalculatorPanel();
         }
        // "Maximum Method Length: Methode ist 80 Zeilen lang, erlaubt sind
        //  h�chstens 75."
        initialize1();
         if (m_complexMode) {
            initialize2();
         }
     }
 
     /**
      * Creates the JMenu
      */
     private void createMenu() {
         JMenuBar menu = new JMenuBar();
         menu.setName("menuBar"); //$NON-NLS-1$
         JMenu fileMenu = new JMenu("File"); //$NON-NLS-1$
         JMenu helpMenu = new JMenu("Help"); //$NON-NLS-1$
 
         helpMenu.add(m_about);
         fileMenu.add(m_reset);
         fileMenu.add(m_quit);
 
         menu.add(fileMenu);
         menu.add(helpMenu);
 
         setJMenuBar(menu);
     }
     
     /**
      * create the FloatCheckBox
      */
     private void createFloatCheckBox() {
         if (m_floatCheckBox == null) {
             m_floatCheckBox = new JCheckBox("float", false); //$NON-NLS-1$
             m_floatCheckBox.setName("float"); //$NON-NLS-1$
             m_floatCheckBox.setMnemonic(KeyEvent.VK_F);
         }
     }
 
     /**
      * ActionListener-Generator (very small factory method(?)) 
      * returning an ActionListener that sets the mode given as parameter.
      * 
      * @param mode mode to set for this action
      * @return an appropriate ActionListener 
      */
     private ActionListener modeSettingActionListener(int mode) {
         final int useMode = mode;
         return new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 m_calculationMode = useMode;
             }
         };
     }
     /**
      * @return a Pannel containing some radio buttons :-)
      */
     private JPanel createRadioButtonsPane() {
         
         JRadioButton blackJackButton = new JRadioButton("BlackJackMode"); //$NON-NLS-1$
         blackJackButton.setText("Black Jack Mode"); //$NON-NLS-1$
         blackJackButton.setMnemonic(KeyEvent.VK_B);
         JRadioButton deepThoughtButton = new JRadioButton("DeepThoughtMode"); //$NON-NLS-1$
         deepThoughtButton.setText("Deep Thought Mode"); //$NON-NLS-1$
         deepThoughtButton.setMnemonic(KeyEvent.VK_D);
         JRadioButton strictCalcButton = new JRadioButton("StrictCalcMode"); //$NON-NLS-1$
         strictCalcButton.setText("Strict Calculator Mode"); //$NON-NLS-1$
         strictCalcButton.setMnemonic(KeyEvent.VK_S);
         
         blackJackButton.addActionListener(
             modeSettingActionListener(BLACK_JACK_MODE));
         deepThoughtButton.addActionListener(
             modeSettingActionListener(DEEP_THOUGHT_MODE));
         strictCalcButton.addActionListener(
             modeSettingActionListener(STRICT_CALC_MODE));
         
         ButtonGroup buttonGroup = new ButtonGroup();
         buttonGroup.add(blackJackButton);
         buttonGroup.add(deepThoughtButton);
         buttonGroup.add(strictCalcButton);
         
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         panel.add(blackJackButton);
         panel.add(deepThoughtButton);
         panel.add(strictCalcButton);
         
         return panel;
     }
     /**
      * @return The options tree.
      */
     public JTree getOptionsTree() {
         if (m_optionsTree == null) {
             m_optionsTree = new JTree();
             m_optionsTree.setName("OptionsTree"); //$NON-NLS-1$
         }
         return m_optionsTree;
     }
     /**
      * @return The options tree scrollpane.
      */
     private JScrollPane getOptionsTreePane() {
         if (m_optionsTreePane == null) {
             m_optionsTreePane = new JScrollPane(getOptionsTree());
         }
         return m_optionsTreePane;
     }
     /**
      * @return The options table.
      */
     public JTable getOptionsTable() {
         if (m_optionsTable == null) {
             m_optionsTable = new JTable();
             m_optionsTable.setName("OptionsTable"); //$NON-NLS-1$
         }
         return m_optionsTable;
     }
     /**
      * @return The options table scrollpane.
      */
     public JScrollPane getOptionsTablePane() {
         if (m_optionsTablePane == null) {
             m_optionsTablePane = new JScrollPane(getOptionsTable()) {
                 public Dimension getPreferredSize() {
                     Dimension tableSize = getOptionsTable().getPreferredSize();
                     int sbWidth = getHorizontalScrollBar().getSize().width;
                     int sbHeight = getVerticalScrollBar().getSize().height;
                     return new Dimension(tableSize.width + sbWidth,
                             tableSize.height + sbHeight);
                 }
             };
         }
         return m_optionsTablePane;
     }
     /**
      * @return The precision-combobox.
      */
     public JComboBox getPrecisionComboBox() {
         if (m_precisionComboBox == null) {
             m_precisionComboBox = new JComboBox();
             m_precisionComboBox.setName("combobox"); //$NON-NLS-1$
         }
         return m_precisionComboBox;
     }
     /**
      * @return Returns the calculationMode.
      */
     public int getCalculationMode() {
         return m_calculationMode;
     }
     /**
      * @return Menu item to show the about dialog.
      */
     public JMenuItem getAbout() {
         return m_about;
     }
     /**
      * @return Menu item to quit the application.
      */
     public JMenuItem getQuit() {
         return m_quit;
     }
     /**
      * @return Menu item to reset the GUI.
      */
     public JMenuItem getReset() {
         return m_reset;
     }
 
     /**
      * Calculator panel
      * @return Calculator Panel
      */
     public AbstractCalculatorPanel getCalculatorPanel() {
         return m_calculaorPanel;
     }
 }
