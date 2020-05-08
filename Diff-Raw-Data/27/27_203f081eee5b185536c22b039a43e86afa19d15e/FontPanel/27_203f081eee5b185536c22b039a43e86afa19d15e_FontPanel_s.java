 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ch9k.chat.gui.components;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
 import javax.swing.Action;
 import javax.swing.GroupLayout;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 import javax.swing.JToggleButton;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
 import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 import javax.swing.text.StyledEditorKit;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
 
 /**
  *
  * @author Bruno
  */
 public class FontPanel extends JPanel implements ActionListener, CaretListener {
     /**
      * The textpanel that we want to style
      */
     private JTextPane editor;
 
     /**
      * The actions used to style the text
      */
     private Action boldAction = new StyledEditorKit.BoldAction();
 
     private Action underlineAction = new StyledEditorKit.UnderlineAction();
 
     private Action italicAction = new StyledEditorKit.ItalicAction();
 
     /**
      * The toggle buttons that will use the actions above
      */
     private JToggleButton bold;
 
     private JToggleButton italic;
 
     private JToggleButton underline;
 
     /**
      * Arrays with the available options for the comboboxes
      */
     private String[] fontTypes = { "Serif", "SansSerif", "Monospaced" };
 
     private String[] fontSizes = { "10", "12", "14", "16", "18", "20" };
 
     private Color[] colors = { Color.BLACK, Color.GRAY, Color.BLUE, Color.RED,
                                Color.GREEN, Color.ORANGE, Color.CYAN, Color.MAGENTA,
                                Color.YELLOW, Color.PINK };
 
     /**
      * Comboxes in which the user wil be able to select above options
      */
     private JComboBox fontType;
 
     private JComboBox fontSize;
 
     private JComboBox colorBox;
 
 
     public FontPanel(JTextPane textpane) {
         editor = textpane;
 
         /* Add a caretListener for the toggle buttons */
         editor.addCaretListener(this);
 
         initComponents();
         initLayout();
     }
 
     private void initComponents() {
         /* Initialize all the buttons */
         bold = new JToggleButton(boldAction);
         bold.putClientProperty("JButton.buttonType", "gradient");
         bold.setMargin(new Insets(2, 5, 2, 5));
         bold.setText("B");
         bold.setFont(bold.getFont().deriveFont(Font.BOLD));
 
         italic = new JToggleButton(italicAction);
         italic.putClientProperty("JButton.buttonType", "gradient");
         italic.setMargin(new Insets(2, 5, 2, 5));
         italic.setText("I");
         italic.setFont(italic.getFont().deriveFont(Font.ITALIC));
 
         underline = new JToggleButton(underlineAction);
         underline.putClientProperty("JButton.buttonType", "gradient");
         underline.setMargin(new Insets(2, 5, 2, 5));
         underline.setText("<html><u>U</u>");
         // make sure we use the button font (is incorrect on windows)
         underline.setFont(UIManager.getFont("Button.font"));
 
         /* Initialize the comboxes and add them to the panel */
         fontType = new JComboBox(fontTypes);
         fontType.addActionListener(this);
         fontType.setSelectedItem("SansSerif");
 
         fontSize = new JComboBox(fontSizes);
         fontSize.addActionListener(this);
         fontSize.setSelectedItem("14");
 
         colorBox = new JComboBox(colors);
         colorBox.addActionListener(this);
         ColorRenderer renderer = new ColorRenderer();
         renderer.setSize(30, 20);
         colorBox.setRenderer(renderer);
 
     }
 
     private void initLayout() {
         GroupLayout layout = new GroupLayout(this);
         setLayout(layout);
 
         layout.setHorizontalGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(bold, 40, 40, 40).addComponent(italic, 40, 40, 40).addComponent(underline, 40, 40, 40).addComponent(fontType).addComponent(fontSize).addComponent(colorBox).addGap(0, 0, Short.MAX_VALUE));
 
         layout.setVerticalGroup(layout.createParallelGroup().addComponent(bold).addComponent(italic).addComponent(underline).addComponent(fontType).addComponent(fontSize).addComponent(colorBox));
     }
 
     public void actionPerformed(ActionEvent e) {
         /* All the text in the editor should change by this action, so get the attributes */
         MutableAttributeSet attrs = editor.getInputAttributes();
 
         /* Check which of the comboboxes fired this event */
         if(e.getSource() == fontType) {
             int index = fontType.getSelectedIndex();
             StyleConstants.setFontFamily(attrs, fontTypes[index]);
         } else if(e.getSource() == fontSize) {
             int index = fontSize.getSelectedIndex();
             StyleConstants.setFontSize(attrs, Integer.parseInt(fontSizes[index]));
         } else if(e.getSource() == colorBox) {
             int index = colorBox.getSelectedIndex();
             StyleConstants.setForeground(attrs, colors[index]);
         }
 
         /* Change the attributes to the ones we changed above */
         StyledDocument doc = editor.getStyledDocument();
         doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
 
     }
 
     @Override
     public void caretUpdate(CaretEvent e) {
         /* For some reason, it's better to only update our buttons
         a little bit later */
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 MutableAttributeSet attr = editor.getInputAttributes();
 
                 /* Update the toggle buttons if the user moves the caret */
                 bold.setSelected(StyleConstants.isBold(attr));
                 italic.setSelected(StyleConstants.isItalic(attr));
                 underline.setSelected(StyleConstants.isUnderline(attr));
             }
         });
     }
 }
 
