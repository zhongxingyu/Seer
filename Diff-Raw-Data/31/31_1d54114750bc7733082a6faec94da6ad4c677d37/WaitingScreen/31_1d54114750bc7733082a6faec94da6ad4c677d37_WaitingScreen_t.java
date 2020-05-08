 package br.org.indt.ndg.lwuit.ui;
 
 import br.org.indt.ndg.lwuit.ui.style.NDGStyleToolbox;
 import com.sun.lwuit.Component;
 import com.sun.lwuit.Container;
 import com.sun.lwuit.Dialog;
 import com.sun.lwuit.Display;
 import com.sun.lwuit.Image;
 import com.sun.lwuit.Label;
 import com.sun.lwuit.TextArea;
 import com.sun.lwuit.layouts.BorderLayout;
 
 /**
  *
  * @author mluz
  */
 public class WaitingScreen {
 
     private static WaitingScreen instance;
     private Dialog dialog;
 
     public static void show(String status) {
         NDGLookAndFeel.removeDefaultDialogTransitionInAndOut();
         instance = new WaitingScreen(status);
         instance.showModeless();
     }
 
     public static boolean isShowed() {
         if (instance != null)
             return instance.dialog.isVisible();
         else
             return false;
     }
 
     public static void dispose() {
         instance.dialog.dispose();
         NDGLookAndFeel.setDefaultDialogTransitionInAndOut();
     }
 
     private void showModeless() {
         Container cont1 = dialog.getContentPane();
         int hi = 0;
         int wi = cont1.getPreferredW();
 
         for( int i = 0 ; i< dialog.getComponentCount() ; i ++ ){
             hi += dialog.getComponentAt(i).getPreferredH();
         }
 
         int disH = Display.getInstance().getDisplayHeight();
         int disW = Display.getInstance().getDisplayWidth();
 
        int marginH = (disH - hi)/2;
        if(marginH < 0){
            marginH = 0;
        }
        int marginW = (disW - wi)/2;
        if(marginW < 0){
            marginW = 0;
        }

        dialog.show( marginH, marginH, marginW, marginW, false, false );


         //dialog.showPacked( BorderLayout.CENTER, false);
     }
 
 
     private WaitingScreen(String status) {
         dialog = new Dialog();
         dialog.setDialogStyle( NDGStyleToolbox.getInstance().menuStyle.getBaseStyle() );
         dialog.getDialogStyle().setBgColor( NDGStyleToolbox.getInstance().menuStyle.bgUnselectedColor );
         dialog.setLayout(new BorderLayout());
         Image image = Screen.getRes().getImage("wait");
 
         Label label = new Label(image);
         label.setAlignment(Component.CENTER);
 
         TextArea msg = new TextArea( status );
         msg.setSingleLineTextArea(true);
         msg.setAlignment( Component.CENTER );
         msg.getSelectedStyle().setFgColor( NDGStyleToolbox.getInstance().menuStyle.unselectedFontColor );
         msg.getSelectedStyle().setFont( NDGStyleToolbox.getInstance().menuStyle.unselectedFont );
         msg.getSelectedStyle().setBgTransparency(0x00);
         msg.setEditable(false);
         msg.setPreferredW(msg.getSelectedStyle().getFont().stringWidth( status ) + 5);
         msg.setPreferredH(msg.getSelectedStyle().getFont().getHeight() + 3);
         msg.setIsScrollVisible(false);
 
         dialog.addComponent(BorderLayout.NORTH ,label);
         dialog.addComponent(BorderLayout.CENTER, msg);
     }
 }
