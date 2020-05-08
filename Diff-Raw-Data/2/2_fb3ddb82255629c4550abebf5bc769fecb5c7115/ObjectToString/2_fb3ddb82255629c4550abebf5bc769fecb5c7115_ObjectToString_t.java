 //----------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //----------------------------------------------------------------------------
package cytoscape.visual.parsers;
 //----------------------------------------------------------------------------
 import java.awt.Color;
 import java.awt.Font;
 
 import y.view.LineType;
 import y.view.Arrow;
 
 import cytoscape.util.Misc;
 //----------------------------------------------------------------------------
 /**
  * This class contains a method that does the reverse of the various parsing
  * classes in this package, i.e. turns a Object back into a String representation.
  * Most cases either use the corresponding methods in cytoscape.util.Misc or
  * use the default toString() method of the object.
  */
 public class ObjectToString {
     
     /**
      * Constructs and returns a String representation of the given Object.
      */
     public String getStringValue(Object o) {
         if (o instanceof Color) {
             return Misc.getRGBText((Color)o);
         } else if (o instanceof LineType) {
             return Misc.getLineTypeText((LineType)o);
         } else if (o instanceof Byte) {
             return Misc.getNodeShapeText( ((Byte)o).byteValue() );
         } else if (o instanceof Arrow) {
             return Misc.getArrowText((Arrow)o);
         } else if (o instanceof Font) {
             return getStringValue((Font)o);
         } else if (o instanceof Number) {
             //just trust the default String representation for numbers
             return o.toString();
         } else {
             //default: use the toString() method
             return o.toString();
         }
     }
     
     public String getStringValue(Font f) {
         String name = f.getName();
         int style = f.getStyle();
         String styleString = "plain";
         if (style == Font.BOLD) {
             styleString = "bold";
         } else if (style == Font.ITALIC) {
             styleString = "italic";
         } else if ( style == (Font.BOLD|Font.ITALIC) ) {
             styleString = "bold|italic";
         }
         int size = f.getSize();
         String sizeString = Integer.toString(size);
         
         return name + "," + styleString + "," + sizeString;
     }
 }
 
