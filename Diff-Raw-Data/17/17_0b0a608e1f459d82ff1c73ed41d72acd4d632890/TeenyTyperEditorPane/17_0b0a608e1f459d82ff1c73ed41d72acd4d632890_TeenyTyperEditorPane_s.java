 /**
  * TeenyTyper by Lindsay Bradford is licensed under a 
  * Creative Commons Attribution-ShareAlike 3.0 Unported License.
  *
  * Year: 2012 
  *
  */
 
 package blacksmyth.teenytyper;
 
 import java.awt.Color;
 
 import java.awt.Cursor;
 import java.awt.Font;
 
 import javax.swing.JTextPane;
 import javax.swing.text.Style;
 import javax.swing.text.StyleConstants;
 
 /**
  *  An extension to JTextPane that allows for the creation of an editor pane
  *  of a given font, capable of switching between the supplied colour list, and
  *  defaulting to the given defaultColour. 
 *
  */
 public class TeenyTyperEditorPane extends JTextPane {
 
   private static final long serialVersionUID = 1L;
   
   public TeenyTyperEditorPane(Font editorFont, Color[] colourList, Color defaultColour) {
     super();
     
     this.setFont(editorFont);
     
     this.setCursor(
         Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
     );
     
     for(Color editorColour : colourList) {
       Style colourStyle = this.addStyle(editorColour.toString(), null);
       StyleConstants.setForeground(colourStyle, editorColour);
     }
     
     setTextColour(defaultColour);
   }
 
   /**
    * Sets the style of the editor to generate any new text entered using the 
    * supplied colour as the new forgound colour.
    * pre: colour is one already supplied in the list of valid colours at 
    *      tine of instantiation.
    * @param colour
    */
   public void setTextColour(Color colour) {
     String colorText = colour.toString();
     this.setCharacterAttributes(
         this.getStyle(colorText), false
     );
   }
 }
