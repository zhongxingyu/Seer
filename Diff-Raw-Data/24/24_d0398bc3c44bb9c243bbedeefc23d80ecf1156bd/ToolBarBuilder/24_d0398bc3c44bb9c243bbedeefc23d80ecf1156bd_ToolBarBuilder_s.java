 /**
  * Junk - ToolBarBuilder.java
  * Copyright 2011 Dave Huffman (dave dot huffman at me dot com).
  * TODO license info.
  */
 package dmh.swing.huxley;
 
 import java.awt.Color;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.util.Arrays;
 import java.util.Collection;
 
import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JEditorPane;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.text.StyledEditorKit;
 import javax.swing.text.html.HTMLEditorKit;
 
 import dmh.kuebiko.Main;
 import dmh.swing.enumselect.EnumSelectable;
 import dmh.swing.enumselect.MasterEnumSelectAction;
 import dmh.swing.html.constants.ParagraphType;
 import dmh.swing.huxley.constants.TextAction;
 
 /**
  * Builder object for the Huxley UI tool bar.
  *
  * @author davehuffman
  */
 class ToolBarBuilder {
     static JToolBar build(JEditorPane textArea) {
         return new ToolBarBuilder(textArea).buildToolBar();
     }
     
     private final JEditorPane instrumentedTextArea;
     
     private ToolBarBuilder(JEditorPane instrumentedTextArea) {
         this.instrumentedTextArea = instrumentedTextArea;
     }
 
     private JToolBar buildToolBar() {
         JToolBar editToolBar = new JToolBar();
         editToolBar.setFocusable(false);
         editToolBar.setFloatable(false);
         editToolBar.setRollover(true);
         editToolBar.setMargin(new Insets(0, 0, 0, 0));
         
 //        editToolBar.setBorder(BorderFactory.createLineBorder(Color.MAGENTA));
 
         
         // Font.
         if (Main.POST_0_1_RELEASE) {
             editToolBar.add(new JComboBox(new String[] { "Font" })); // TODO implement.
             editToolBar.add(new JComboBox(new String[] { "Size" })); // TODO implement.
             editToolBar.add(newBtn("FONT_SIZE_DOWN", TextAction.FONT_SIZE_SMALLER));
             editToolBar.add(newBtn("FONT_SIZE_UP", TextAction.FONT_SIZE_BIGGER));
             JButton colorButton = newBtn("COLOR", TextAction.FONT_COLOR);
             colorButton.setAction(new StyledEditorKit.ForegroundAction(HTMLEditorKit.COLOR_ACTION, Color.GREEN));
             editToolBar.add(colorButton);
             editToolBar.addSeparator();
         }
         
         // Style.
         editToolBar.add(newTogBtn("bold", TextAction.STYLE_BOLD));
         editToolBar.add(newTogBtn("italic", TextAction.STYLE_ITALIC));
         editToolBar.add(newTogBtn("underline", TextAction.STYLE_UNDERLINE));
         if (Main.POST_0_1_RELEASE) {
             editToolBar.add(newTogBtn("strikethrough", TextAction.STYLE_STRIKETHROUGH)); // TODO implement.
             editToolBar.addSeparator();
         }
         
         // Alignment.
         if (Main.POST_0_1_RELEASE) {
             Collection<JToggleButton> alignButtons = Arrays.asList(
                     newTogBtn("align-left", TextAction.ALIGN_LEFT),
                     newTogBtn("align-center", TextAction.ALIGN_CENTER),
                     newTogBtn("align-right", TextAction.ALIGN_RIGHT));
             ButtonGroup alignButtonGroup = new ButtonGroup();
             for (JToggleButton alignButton: alignButtons) {
                 alignButtonGroup.add(alignButton);
                 editToolBar.add(alignButton);
                 
             }
             alignButtons.add(newTogBtn("align-justity", TextAction.ALIGN_JUSTIFY));
         }
         
         // Objects.
         if (Main.POST_0_1_RELEASE) {
             editToolBar.add(newBtn("link", TextAction.INSERT_LINK));
 //            editToolBar.add(new JButton(new ImageAction()));
             //editToolBar.add(newBtn("IMAGE", TextAction.INSERT_IMAGE));
             editToolBar.add(newBtn("break", TextAction.INSERT_BREAK));
             editToolBar.add(newBtn("horizontal-rule", TextAction.INSERT_SEPERATOR));
             editToolBar.add(newBtn("list-numbers", TextAction.INSERT_LIST_NUMBERED));
             editToolBar.add(newBtn("list-numbers", TextAction.INSERT_LIST_NUMBERED_ITEM));
             editToolBar.add(newBtn("list-bullets", TextAction.INSERT_LIST_BULLETTED));
             editToolBar.add(newBtn("list-bullets", TextAction.INSERT_LIST_BULLETTED_ITEM));
             editToolBar.add(newBtn("table", TextAction.INSERT_TABLE));
             editToolBar.add(newBtn("table", TextAction.INSERT_TABLE_COL));
             editToolBar.add(newBtn("table", TextAction.INSERT_TABLE_ROW));
             editToolBar.addSeparator();
         }
         
         // Paragraph.
         final ParagraphStyleAction psa = new ParagraphStyleAction(instrumentedTextArea);
         MasterEnumSelectAction<ParagraphType> masterParagraphAction = 
                 new MasterEnumSelectAction<ParagraphType>(ParagraphType.class, 
                         new EnumSelectable<ParagraphType>() {
                             @Override
                             public void onEnumSelect(ParagraphType enumValue,
                                     ActionEvent e) {
                                 psa.actionPerformed(e);
                             }
                         });
         instrumentedTextArea.getActionMap().put("huxley-paragraph", masterParagraphAction);
         final JComboBox paragraphComboBox = masterParagraphAction.buildComboBox();
         paragraphComboBox.setFont(paragraphComboBox.getFont().deriveFont(11f));
         paragraphComboBox.setMaximumSize(paragraphComboBox.getPreferredSize());
         editToolBar.add(paragraphComboBox);
         
         editToolBar.add(Box.createHorizontalGlue());
         
         return editToolBar;
     }
     
     private JButton newBtn(String image, TextAction action) {
         JButton button = new JButton(
                 instrumentedTextArea.getActionMap().get(action.actionName));
         button.setIcon(new ImageIcon(ImageManager.get().getImage(image)));
         button.setText(null);
 //        button.setPreferredSize(new Dimension(16, 16));
         button.setFocusable(false);
         return button;
     }
     
     /**
      * Helper method. Instantiate and configure a toggle button for use in the 
      * note edit tool bar.
      * @param image Identifier for the button's image.
      * @param action The action to be associated with the button.
      * @return A newly created toggle button.
      */
     private JToggleButton newTogBtn(String image, final TextAction action) {
         JToggleButton button = new JToggleButton(
                 instrumentedTextArea.getActionMap().get(action.actionName));
         button.setIcon(new ImageIcon(ImageManager.get().getImage(image)));
         button.setText(null);
 //        button.setMaximumSize(new Dimension(16, 16));
         button.setMargin(new Insets(0,0,0,0));
         button.setFocusable(false);
         return button;
     }
     
     private void describeSelection() {
         String selectedText = instrumentedTextArea.getSelectedText();
         if (selectedText != null) {
             selectedText = selectedText.replaceAll("\n", "\\\\n");
         }
         
         System.out.printf("%d-%d:[%s]%n", 
                 instrumentedTextArea.getSelectionStart(),
                 instrumentedTextArea.getSelectionEnd(),
                 selectedText);
         
     }
 }
