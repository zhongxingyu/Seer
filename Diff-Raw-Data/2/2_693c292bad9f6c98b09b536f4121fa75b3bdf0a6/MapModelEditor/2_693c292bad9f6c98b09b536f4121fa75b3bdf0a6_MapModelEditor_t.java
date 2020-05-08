 package org.toj.dnd.irctoolkit.ui.map.modelpane;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.ListCellRenderer;
 import javax.swing.SwingConstants;
 import javax.swing.WindowConstants;
 import javax.swing.border.EmptyBorder;
 
 import org.toj.dnd.irctoolkit.engine.ToolkitEngine;
 import org.toj.dnd.irctoolkit.engine.command.map.AddOrUpdateModelCommand;
 import org.toj.dnd.irctoolkit.map.MapModel;
 import org.toj.dnd.irctoolkit.token.Color;
 import org.toj.dnd.irctoolkit.ui.StyleConstants;
 
 public class MapModelEditor extends JDialog {
 
     private static final String ICON_ID = "ͼId";
     private static final String ICON = "ͼ";
     private static final String ICON_DESC = "";
     private static final String BLOCK_LOS = "赲";
     private static final String BLOCK_LOE = "赲Ч";
 
     private static final long serialVersionUID = -5864200205663505539L;
 
     private JPanel contentPane;
     private JTextField tfId;
     private JTextField tfIcon;
     private JTextField tfDesc;
     private ColorTypeRadioGroup rColorType;
     private JComboBox listColor;
     private JCheckBox checkLoS;
     private JCheckBox checkLoE;
     private MapModel model;
     private int index;
 
     public MapModelEditor() {
         setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
         setResizable(false);
         contentPane = new JPanel();
         contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
         setContentPane(contentPane);
         contentPane.setLayout(null);
 
         /* ------------------------------ row 1 ------------------------------ */
         // 2 elements in row 1: icon id label(lId) and icon id input box(tfId)
         int internalMargin = (StyleConstants.MODEL_EDITOR_SIZE.width - 2
                 * StyleConstants.DIALOG_HORIZONTAL_MARGIN
                 - StyleConstants.ICON_ID_LABEL_SIZE.width - StyleConstants.ICON_ID_INPUT_SIZE.width);
         int startingWidth = StyleConstants.DIALOG_HORIZONTAL_MARGIN;
         int startingHeight = StyleConstants.DIALOG_VERTICAL_MARGIN;
         JLabel lId = new JLabel(ICON_ID);
         lId.setVerticalAlignment(SwingConstants.CENTER);
         lId.setBounds(startingWidth, startingHeight,
                 StyleConstants.ICON_ID_LABEL_SIZE.width,
                 StyleConstants.ICON_ID_LABEL_SIZE.height);
         contentPane.add(lId);
 
         startingWidth += StyleConstants.ICON_ID_LABEL_SIZE.width
                 + internalMargin;
         tfId = new JTextField();
         tfId.setBounds(startingWidth, startingHeight,
                 StyleConstants.ICON_ID_INPUT_SIZE.width,
                 StyleConstants.ICON_ID_INPUT_SIZE.height);
         tfId.setEditable(false);
         tfId.setBackground(java.awt.Color.WHITE);
         tfId.setHorizontalAlignment(SwingConstants.RIGHT);
         contentPane.add(tfId);
 
         /* ------------------------------ row 2 ------------------------------ */
         // 4 elements in row 2: icon label(lIcon), icon input box(tfIcon), desc
         // label(lDesc) and desc input box(tfDesc)
         internalMargin = (StyleConstants.MODEL_EDITOR_SIZE.width - 2
                 * StyleConstants.DIALOG_HORIZONTAL_MARGIN
                 - StyleConstants.ICON_LABEL_SIZE.width
                 - StyleConstants.ICON_INPUT_SIZE.width
                 - StyleConstants.DESC_LABEL_SIZE.width - StyleConstants.DESC_INPUT_SIZE.width) / 3;
         startingWidth = StyleConstants.DIALOG_HORIZONTAL_MARGIN;
         startingHeight += StyleConstants.INPUT_HEIGHT
                 + StyleConstants.DIALOG_VERTICAL_MARGIN;
         JLabel lIcon = new JLabel(ICON);
         lIcon.setBounds(startingWidth, startingHeight,
                 StyleConstants.ICON_LABEL_SIZE.width,
                 StyleConstants.ICON_LABEL_SIZE.height);
         contentPane.add(lIcon);
 
         startingWidth += StyleConstants.ICON_LABEL_SIZE.width + internalMargin;
         tfIcon = new JTextField();
         tfIcon.setBounds(startingWidth, startingHeight,
                 StyleConstants.ICON_INPUT_SIZE.width,
                 StyleConstants.ICON_INPUT_SIZE.height);
         tfIcon.setFont(StyleConstants.ICON_FONT);
         contentPane.add(tfIcon);
         tfIcon.setColumns(10);
 
         startingWidth += StyleConstants.ICON_INPUT_SIZE.width + internalMargin;
         JLabel lDesc = new JLabel(ICON_DESC);
         lDesc.setHorizontalAlignment(SwingConstants.CENTER);
         lDesc.setBounds(startingWidth, startingHeight,
                 StyleConstants.DESC_LABEL_SIZE.width,
                 StyleConstants.DESC_LABEL_SIZE.height);
         contentPane.add(lDesc);
 
         startingWidth += StyleConstants.DESC_LABEL_SIZE.width + internalMargin;
         tfDesc = new JTextField();
         tfDesc.setBounds(startingWidth, startingHeight,
                 StyleConstants.DESC_INPUT_SIZE.width,
                 StyleConstants.DESC_INPUT_SIZE.height);
         contentPane.add(tfDesc);
         tfDesc.setColumns(10);
 
         /* ------------------------------ row 3 ------------------------------ */
         // 3 elements in row 3: radio button
         // forecolor(rColorType.getRadioFore()), radio button
         // backcolor(rColorType.getRadioBack()) and color picker
         // dropdown(listColor)
         internalMargin = (StyleConstants.MODEL_EDITOR_SIZE.width - 2
                 * StyleConstants.DIALOG_HORIZONTAL_MARGIN
                 - StyleConstants.COLOR_DEPTH_RADIO_BUTTON_SIZE.width * 2 - StyleConstants.COLOR_DROPDOWN_SIZE.width) / 2;
         startingWidth = StyleConstants.DIALOG_HORIZONTAL_MARGIN;
         startingHeight += StyleConstants.INPUT_HEIGHT
                 + StyleConstants.DIALOG_VERTICAL_MARGIN;
 
         rColorType = new ColorTypeRadioGroup();
 
         rColorType.getRadioFore().setBounds(startingWidth, startingHeight,
                 StyleConstants.COLOR_DEPTH_RADIO_BUTTON_SIZE.width,
                 StyleConstants.COLOR_DEPTH_RADIO_BUTTON_SIZE.height);
         contentPane.add(rColorType.getRadioFore());
 
         startingWidth += StyleConstants.COLOR_DEPTH_RADIO_BUTTON_SIZE.width
                 + internalMargin;
         rColorType.getRadioBack().setBounds(startingWidth, startingHeight,
                 StyleConstants.COLOR_DEPTH_RADIO_BUTTON_SIZE.width,
                 StyleConstants.COLOR_DEPTH_RADIO_BUTTON_SIZE.height);
         contentPane.add(rColorType.getRadioBack());
 
         startingWidth += StyleConstants.COLOR_DEPTH_RADIO_BUTTON_SIZE.width
                 + internalMargin;
         listColor = new JComboBox(new DefaultComboBoxModel(Color.values()));
         listColor.setBounds(startingWidth, startingHeight,
                 StyleConstants.COLOR_DROPDOWN_SIZE.width,
                 StyleConstants.COLOR_DROPDOWN_SIZE.height);
         listColor.setMaximumRowCount(16);
         listColor.setOpaque(true);
         listColor.setRenderer(new ListCellRenderer() {
 
             private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
 
             @Override
             public Component getListCellRendererComponent(JList list,
                     Object value, int index, boolean isSelected,
                     boolean cellHasFocus) {
                 // super.getListCellRendererComponent(list, value, index,
                 // isSelected, cellHasFocus);
                 Color color = (Color) value;
 
                 JLabel label = (JLabel) defaultRenderer
                         .getListCellRendererComponent(list, value, index,
                                 isSelected, cellHasFocus);
                 label.setText(color.getName());
                 label.setHorizontalAlignment(SwingConstants.CENTER);
                 label.setVerticalAlignment(SwingConstants.CENTER);
                 label.setOpaque(true);
                 label.setBackground(color.getColor());
                 label.setForeground(isDark(color.getColor()) ? java.awt.Color.white
                         : java.awt.Color.black);
                 Dimension size = getPreferredSize();
                 size.height = StyleConstants.INPUT_HEIGHT;
                 label.setPreferredSize(size);
                 if (isSelected) {
                     label.setBorder(BorderFactory
                             .createLineBorder(java.awt.Color.black));
                 }
                 return label;
             }
 
             private boolean isDark(java.awt.Color color) {
                 return (color.getRed() + color.getGreen() + color.getBlue()) < 255 * 3 / 2;
             }
         });
         contentPane.add(listColor);
 
         /* ------------------------------ row 4 ------------------------------ */
         // 3 elements in row 4: checkbox blockLoS(checkLoS), checkbox
         // blockLoE(checkLoE) and button ok(bOk)
         internalMargin = (StyleConstants.MODEL_EDITOR_SIZE.width - 2
                 * StyleConstants.DIALOG_HORIZONTAL_MARGIN
                 - StyleConstants.BLOCK_LOE_CHECKBOX_SIZE.width * 2 - StyleConstants.BUTTON_SIZE_SMALL.width) / 2;
         startingWidth = StyleConstants.DIALOG_HORIZONTAL_MARGIN;
         startingHeight += StyleConstants.INPUT_HEIGHT
                 + StyleConstants.DIALOG_VERTICAL_MARGIN;
 
         checkLoS = new JCheckBox(BLOCK_LOS);
         checkLoS.setBounds(startingWidth, startingHeight,
                 StyleConstants.BLOCK_LOE_CHECKBOX_SIZE.width,
                 StyleConstants.BLOCK_LOE_CHECKBOX_SIZE.height);
         contentPane.add(checkLoS);
 
         startingWidth += StyleConstants.BLOCK_LOE_CHECKBOX_SIZE.width
                 + internalMargin;
         checkLoE = new JCheckBox(BLOCK_LOE);
         checkLoE.setBounds(startingWidth, startingHeight,
                 StyleConstants.BLOCK_LOE_CHECKBOX_SIZE.width,
                 StyleConstants.BLOCK_LOE_CHECKBOX_SIZE.height);
         contentPane.add(checkLoE);
 
         startingWidth += StyleConstants.BLOCK_LOE_CHECKBOX_SIZE.width
                 + internalMargin;
         BtnOk bOk = new BtnOk(this);
         bOk.setBounds(startingWidth, startingHeight,
                 StyleConstants.BUTTON_SIZE_SMALL.width,
                 StyleConstants.BUTTON_SIZE_SMALL.height);
         contentPane.add(bOk);
     }
 
     public void initWithModel(MapModel initModel, int index) {
         this.model = initModel;
         if (initModel == null) {
             this.model = new MapModel();
         }
 
         this.index = index;
 
         tfId.setText(model.getId());
         tfIcon.setText(model.getCh());
         tfDesc.setText(model.getDesc());
 
         if (model.getForeground() != null) {
             rColorType.getRadioFore().setSelected(true);
             listColor.setSelectedItem(model.getForeground());
         } else {
             rColorType.getRadioBack().setSelected(true);
             listColor.setSelectedItem(model.getBackground());
         }
 
         checkLoS.setSelected(model.isBlocksLineOfSight());
         checkLoE.setSelected(model.isBlocksLineOfEffect());
     }
 
     void addOrUpdateModel() {
 
         if (ColorTypeRadioGroup.FOREGROUND.equals(rColorType.getSelected())) {
             model.setForeground((Color) listColor.getSelectedItem());
             model.setBackground(null);
         }
 
         if (ColorTypeRadioGroup.BACKGROUND.equals(rColorType.getSelected())) {
             model.setBackground((Color) listColor.getSelectedItem());
             model.setForeground(null);
             model.setCh("");
         }
 
         if (model.getForeground() != null) {
             String ic = tfIcon.getText();
             if (ic == null || ic.trim().isEmpty()) {
                ToolkitEngine.getEngine().fireErrorMsgWindow("ݰͼ¡");
                 throw new IllegalArgumentException();
             }
             ic = ic.trim();
             if (isDoubleByteCharacter(ic.substring(0, 1))) {
                 ic = ic.substring(0, 1);
             } else {
                 if (ic.length() > 2) {
                     ic = ic.substring(0, 2);
                 }
                 if (ic.length() == 1) {
                     ic = ic + " ";
                 }
             }
             model.setCh(ic);
         }
 
         model.setDesc(tfDesc.getText());
 
         model.setBlocksLineOfSight(checkLoS.isSelected());
         model.setBlocksLineOfEffect(checkLoE.isSelected());
 
         ToolkitEngine.getEngine().queueCommand(
                 new AddOrUpdateModelCommand(model, index));
     }
 
     private boolean isDoubleByteCharacter(String ch) {
         return ch.getBytes().length > 1;
     }
 
     public void adjustBounds() {
         Point pCenter = java.awt.MouseInfo.getPointerInfo().getLocation();
         Rectangle bounds = new Rectangle();
         bounds.x = pCenter.x - getSize().width / 2;
         if (bounds.x < 0) {
             bounds.x = 0;
         }
         bounds.y = pCenter.y - getSize().height / 2;
         if (bounds.y < 0) {
             bounds.y = 0;
         }
         bounds.width = StyleConstants.MODEL_EDITOR_SIZE.width;
         bounds.height = StyleConstants.MODEL_EDITOR_SIZE.height;
 
         setBounds(bounds);
     }
 }
