 package br.org.indt.ndg.lwuit.ui;
 
 import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
 import br.org.indt.ndg.mobile.AppMIDlet;
 import br.org.indt.ndg.mobile.Resources;
 import br.org.indt.ndg.mobile.Utils;
 import br.org.indt.ndg.mobile.logging.Logger;
 import br.org.indt.ndg.mobile.settings.LocationHandler;
 import com.sun.lwuit.Display;
 import com.sun.lwuit.Font;
 import com.sun.lwuit.Graphics;
 import com.sun.lwuit.Image;
 import com.sun.lwuit.Painter;
 import com.sun.lwuit.geom.Rectangle;
 import java.util.Vector;
 
 /**
  *
  * @author mluz
  */
 public class TitleBar implements Painter {
 
     private static final Image hr = Screen.getRes().getImage("bottom");
     private static final Image imageGPS = Screen.getRes().getImage("gps");
     private static final int gpsWidth = imageGPS.getWidth();
     private static final int TEXT_PADDING = 2;
     private static int logoWidth;
     private static int logoHeight;
 
     int textOffsetHorizontal;
     private String title1;
     private String title2;
     private Font titleFont = null;
     private int currentScreenWidth = 0;
 
 
     TitleBar(String title1, String title2) {
         this.title1 = title1;
         this.title2 = title2;
         logoWidth = Resources.logo.getWidth();
         logoHeight = Resources.logo.getHeight();
         textOffsetHorizontal = TEXT_PADDING + gpsWidth + TEXT_PADDING + logoWidth + TEXT_PADDING;
     }
 
     private Font getTitleFont(){
         int newScreenWidth = Display.getInstance().getDisplayWidth();
 
         if(titleFont == null || newScreenWidth != currentScreenWidth){
             currentScreenWidth = newScreenWidth;
             titleFont = prepereFont(title1);
         }
         return titleFont;
     }
 
     public int getPrefferedH() {
        if( title1.length() == 0 && title2.length() == 0 )
            return 0;
 
        int fontsHight = 3*TEXT_PADDING + getTitleFont().getHeight() + getTitleFont().getHeight();//title1 height and title2 height
        int logoHight = 2*TEXT_PADDING + logoHeight;
        return fontsHight < logoHight ? logoHight : fontsHight;
     }
 
     public void paint(Graphics g, Rectangle rect) {
         int width = rect.getSize().getWidth();
         int height = rect.getSize().getHeight();
 
         g.fillLinearGradient(0xffffff, 0xe1e1e1, rect.getX(), rect.getY(), width, height-3, false);
 
         g.drawImage( hr.scaled( Display.getInstance().getDisplayWidth(),
                                 hr.getHeight()),
                                 rect.getX(), rect.getY() + height - 2 );
 
         g.drawImage( Resources.logo, TEXT_PADDING + gpsWidth + TEXT_PADDING, ( height - logoHeight )>>1 );
 
         int spacingVertical =  ( height - getTitleFont().getHeight() - getTitleFont().getHeight() )/3;
 
         g.setFont( getTitleFont() );
         g.setColor( 0x007b7b7b );
         g.drawString( title1, textOffsetHorizontal, spacingVertical );
         g.drawString( title2, textOffsetHorizontal, spacingVertical +  getTitleFont().getHeight() + spacingVertical );
         LocationHandler locationHandler = AppMIDlet.getInstance().getLocationHandler();
         if ( locationHandler != null && locationHandler.locationObtained() ) {
             g.drawImage(imageGPS, TEXT_PADDING, 22);
         }
     }
 
     private Font prepereFont(String title){
         Font font = NDGStyleToolbox.fontSmall;
         int dWidth = Display.getInstance().getDisplayWidth();
 
         if(Utils.isS40()){
             Vector s40Fonts = NDGStyleToolbox.getDefaultFontList();
             Font f = null;
             for(int idx = 0; idx < s40Fonts.size(); idx++){
                 f = (Font)s40Fonts.elementAt(idx);
                if(f.stringWidth(title) + textOffsetHorizontal < dWidth){
                     font = f;
                     break;
                 }
             }
             return font;
         }
 
         Vector fontVect = NDGStyleToolbox.getAvailableFontSizes();
         Font f = null;
         for(int idx = fontVect.size() - 1; idx >= 0; idx--){
             f = Screen.getFontRes().getFont( (String)fontVect.elementAt(idx) );
            if(f.stringWidth(title) + textOffsetHorizontal < dWidth){
                 font = f;
                 break;
             }
         }
         return font;
     }
 
     public void setTitle1(String title1) {
         this.title1 = title1;
     }
 
     public void setTitle2(String title2) {
         this.title2 = title2;
     }
 }
