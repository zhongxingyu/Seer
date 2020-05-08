 package br.org.indt.ndg.lwuit.ui;
 
 import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
 import com.sun.lwuit.Display;
 import com.sun.lwuit.Font;
 import com.sun.lwuit.TextArea;
 
 
 public class UIUtils {
 
     private static final int NUMBER_OF_COLUMNS = 20;
 
     static public TextArea createTextArea( String aText, Font aFont ) {
         TextArea item = new TextArea();
         item.getStyle().setFont(aFont);
         item.getUnselectedStyle().setFont(aFont);
         item.setEditable(false);
         item.setFocusable(false);
         item.setColumns(NUMBER_OF_COLUMNS);
         item.setRows(1);
         item.setGrowByContent(false);
        item.setText(aText);
 
         int pw = Display.getInstance().getDisplayWidth() - 10;// magic number, it should rather be set to width of margins+borders+padding
         int w = aFont.stringWidth(aText);
         if (w >= pw) {
             item.setGrowByContent(true);
             item.setRows(2);
         } else {
             item.setGrowByContent(false);
             item.setRows(1);
         }
         return item;
     }
 
     static public TextArea createTextArea( String aText, Font aFont, int aColor ) {
         TextArea item = createTextArea(aText, aFont);
         item.getStyle().setFgColor(aColor);
         item.getSelectedStyle().setFgColor(aColor);
         return item;
     }
 
     static public TextArea createQuestionName( String aText ) {
         return createTextArea( aText, NDGStyleToolbox.fontSmall );
     }
 }
