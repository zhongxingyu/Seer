 package ex02_02;
 
 import java.awt.*;
 import java.lang.reflect.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 class ClockProperty {
   public static final String[] fontFamily;
   public static final String[] colorFamily;
   public static final String[] fontSizes =
    {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
 
   private Font font;
   private Color fontColor;
   private Color backGroundColor;
 
   static {
     fontFamily =
         GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
     List<Field> colorFields = new ArrayList<Field>(Arrays.asList(Color.class.getFields()));
 
     Iterator<Field> it = colorFields.iterator();
     while (it.hasNext()) {
       Field field = it.next();
       if ( !(field.getType().equals(Color.class)
           && Character.isUpperCase(field.getName().charAt(0))))
         it.remove();
     }
     colorFamily = new String[colorFields.size()];
     for (int i = 0; i < colorFields.size(); ++i)
       colorFamily[i] = colorFields.get(i).getName();
   }
 
   ClockProperty(Font font, Color fontColor, Color backGroundColor) {
     this.font = font;
     this.fontColor = fontColor;
     this.backGroundColor = backGroundColor;
   }
 
   Font getFont() {
     return font;
   }
 
   void setFont(String fontName) {
     int fontStyle = font.getStyle();
     int fontSize  = font.getSize();
     font = new Font(fontName, fontStyle, fontSize);
   }
 
   void setFont(int fontSize) {
     String fontName = font.getName();
     int fontStyle = font.getStyle();
     font = new Font(fontName, fontStyle, fontSize);
   }
 
   Color getFontColor() {
     return fontColor;
   }
 
   void setFontColor(String colorName) {
     fontColor = getColorInstance(colorName);
   }
 
   void setFontColor(Color color) {
     fontColor = color;
   }
 
   Color getBackGroundColor() {
     return backGroundColor;
   }
 
   void setBackGroundColor(String colorName) {
     backGroundColor = getColorInstance(colorName);
   }
 
   void setBackGroundColor(Color color) {
     backGroundColor = color;
   }
 
   /** 文字列から Color インスタンスを返す */
   Color getColorInstance(String colorName) {
     Color ret = null;
 
     for (String colorStr : colorFamily) {
       if (colorStr.equals(colorName)) {
         Class<Color> colorClass = Color.class;
         try {
           Field filed = colorClass.getField(colorName);
           ret = (Color)filed.get(Color.WHITE);
         } catch (NoSuchFieldException e) {
           e.printStackTrace();
         } catch (IllegalAccessException e) {
           e.printStackTrace();
         }
       }
     }
 
     return ret;
   }
 }
