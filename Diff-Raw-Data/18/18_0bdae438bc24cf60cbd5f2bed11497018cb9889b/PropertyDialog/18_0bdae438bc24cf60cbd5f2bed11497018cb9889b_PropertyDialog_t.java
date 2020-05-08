 package ex02_02;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 class PropertyDialog extends JDialog {
   private static final long serialVersionUID = -8600365985749587342L;
   private static final Font labelCommonFont = new Font("Arial", Font.BOLD, 20);
 
   private DigitalClock owner;
   private ClockProperty property;
 
   private JLabel fontLabel;
   private DefaultComboBoxModel<String> fontModel;
   private JComboBox<String> fontBox;
 
   private JLabel fontSizeLabel;
   private DefaultComboBoxModel<String> fontSizeModel;
   private JComboBox<String> fontSizeBox;
 
   private JLabel fontColorLabel;
   private DefaultTableModel fontColorModel;
   private JTable fontColorTable;
   private JScrollPane fontColorScroll;
 
   private JLabel backGroundLabel;
   private DefaultTableModel backGroundModel;
   private JTable backGroundTable;
   private JScrollPane backGroundScroll;
 
   private JButton okButton;
 
   private GridBagLayout layout;
   private GridBagConstraints constraints;
 
   PropertyDialog(DigitalClock owner, ClockProperty property) {
     super(owner);
     this.owner = owner;
     this.property = property;
     setModal(true);
     setTitle("Property");
     setSize(400,270);
     setResizable(false);
     setLocationRelativeTo(null);
 
    fontLabel = new JLabel("Font");
     fontLabel.setFont(labelCommonFont);
     fontModel = new DefaultComboBoxModel<String>(ClockProperty.fontFamily);
     fontBox   = new JComboBox<String>(fontModel);
 
    fontSizeLabel = new JLabel("Font Size");
     fontSizeLabel.setFont(labelCommonFont);
     fontSizeModel = new DefaultComboBoxModel<String>(ClockProperty.fontSizes);
     fontSizeBox   = new JComboBox<String>(fontSizeModel);
 
     /* 色情報 */
     int colorNum = ClockProperty.colorFamily.length;
     String[][] tableData = new String[colorNum][2];
     for (int i = 0; i < colorNum; i++) {
       tableData[i][0] = "";
       tableData[i][1] = ClockProperty.colorFamily[i];
     }
 
    fontColorLabel = new JLabel("Font Color");
     fontColorLabel.setFont(labelCommonFont);
     fontColorModel = new DefaultTableModel(tableData, new String[]{"", ""});
     fontColorTable = new JTable(fontColorModel);
     fontColorTable.setTableHeader(null);
     fontColorTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
     fontColorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     fontColorTable.getSelectionModel().addListSelectionListener(new FontColorSelectionListener());
     fontColorTable.getColumnModel().getColumn(0).setCellRenderer(new ColorCellRenderer());
     fontColorTable.getColumnModel().getColumn(0).setPreferredWidth(50);
     fontColorTable.setDefaultEditor(Object.class, null);
     fontColorScroll = new JScrollPane(fontColorTable);
     fontColorScroll.setPreferredSize(new Dimension(200, 50));
 
    backGroundLabel = new JLabel("Background Color");
     backGroundLabel.setFont(labelCommonFont);
     backGroundModel = new DefaultTableModel(tableData, new String[]{"", ""});
     backGroundTable = new JTable(backGroundModel);
     backGroundTable.setTableHeader(null);
     backGroundTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
     backGroundTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
     backGroundTable.getSelectionModel().addListSelectionListener(new BackgroundSelectionListener());
     backGroundTable.getColumnModel().getColumn(0).setCellRenderer(new ColorCellRenderer());
     backGroundTable.getColumnModel().getColumn(0).setPreferredWidth(50);
     backGroundTable.setDefaultEditor(Object.class, null);
     backGroundScroll = new JScrollPane(backGroundTable);
     backGroundScroll.setPreferredSize(new Dimension(200, 50));
 
     okButton = new JButton("OK");
 
     layout = new GridBagLayout();
     setLayout(layout);
     constraints = new GridBagConstraints();
     constraints.insets = new Insets(5, 5, 5, 5);
 
     constraints.anchor = GridBagConstraints.EAST;
     addComponent(fontLabel, 0, 0);
     constraints.anchor = GridBagConstraints.WEST;
     addComponent(fontBox, 1, 0);
     constraints.anchor = GridBagConstraints.EAST;
     addComponent(fontSizeLabel, 0, 1);
     constraints.anchor = GridBagConstraints.WEST;
     addComponent(fontSizeBox, 1, 1);
     constraints.anchor = GridBagConstraints.EAST;
     addComponent(fontColorLabel, 0, 2);
     constraints.anchor = GridBagConstraints.WEST;
     addComponent(fontColorScroll, 1, 2);
     constraints.anchor = GridBagConstraints.EAST;
     addComponent(backGroundLabel, 0, 3);
     constraints.anchor = GridBagConstraints.WEST;
     addComponent(backGroundScroll, 1, 3);
     constraints.anchor = GridBagConstraints.EAST;
     addComponent(okButton, 1, 4);
 
     PropertyActionListener listener = new PropertyActionListener();
     fontBox.addActionListener(listener);
     fontSizeBox.addActionListener(listener);
     okButton.addActionListener(listener);
   }
 
   private void addComponent(Component com, int x, int y) {
     constraints.gridx = x;
     constraints.gridy = y;
     layout.setConstraints(com, constraints);
     add(com);
   }
 
   public class ColorCellRenderer extends DefaultTableCellRenderer {
     private static final long serialVersionUID = 1601402918980924648L;
 
     @Override
     public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
       JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
       Color color = property.getColorInstance(ClockProperty.colorFamily[row]);
       label.setBackground(color);
 
       return label;
     }
   }
 
   private class PropertyActionListener implements ActionListener {
     @Override
     public void actionPerformed(ActionEvent e) {
       Object source = e.getSource();
       if (source == fontBox) {
         String fontName = (String)fontBox.getSelectedItem();
         property.setFont(fontName);
         owner.changeFont();
       } else if (source == fontSizeBox) {
         int fontSize = Integer.valueOf((String)fontSizeBox.getSelectedItem());
         property.setFont(fontSize);
         owner.changeFont();
       } else if (source == okButton) {
         PropertyDialog.this.setVisible(false);
       }
     }
   }
 
   private class FontColorSelectionListener implements ListSelectionListener {
     @Override
     public void valueChanged(ListSelectionEvent e) {
       int selectedRow = fontColorTable.convertRowIndexToModel(fontColorTable.getSelectedRow());
       String colorName = (String)fontColorModel.getValueAt(selectedRow, 1);
       property.setFontColor(colorName);
     }
   }
 
   private class BackgroundSelectionListener implements ListSelectionListener {
     @Override
     public void valueChanged(ListSelectionEvent e) {
       int selectedRow = backGroundTable.convertRowIndexToModel(backGroundTable.getSelectedRow());
       String colorName = (String)backGroundModel.getValueAt(selectedRow, 1);
       property.setBackGroundColor(colorName);
     }
   }
 }
