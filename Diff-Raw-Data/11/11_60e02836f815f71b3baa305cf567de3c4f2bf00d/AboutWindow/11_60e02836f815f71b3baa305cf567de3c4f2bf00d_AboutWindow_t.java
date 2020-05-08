 package ua.kharkov.kture.ot.view.swing.additional.about;
 
 import ua.kharkov.kture.ot.common.localization.MessageBundle;
 import ua.kharkov.kture.ot.view.swing.additional.AbstractAdditionalWindow;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.swing.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.List;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 public class AboutWindow extends AbstractAdditionalWindow {
 
     MessageBundle bundle;
 
     @Inject
     public void setBundle(@Named("about") MessageBundle bundle) {
         this.bundle = bundle;
     }
 
     @Override
     protected void go() {
         List<String> names = newArrayList(
                 bundle.getMessage("link"),
                 bundle.getMessage("st.kurilin"),
                 bundle.getMessage("akril"));
         setTitle("About");
         setSize(500, 150);
         setResizable(false);
         StringBuffer b = new StringBuffer();
         b.append(bundle.getMessage("developers")).append("\n");
         while (names.size() > 0) {
             int c = (int) Math.floor(Math.random() * names.size());
             b.append("    ").append(names.get(c)).append("\n");
             names.remove(c);
         }
         b.append(bundle.getMessage("kurator")).append("\n");
        b.append("    ").append(bundle.getMessage("management"));
         b.append("\n\n\n\n\n\n\n\n\n\n\n\nWe are realy sorry :(");
 
         JTextArea text = new JTextArea(b.toString());
         text.setEditable(false);
         getContentPane().add(text);
         text.addKeyListener(new KeyListener() {
 
             @Override
             public void keyTyped(KeyEvent e) {
                 // TODO Auto-generated method stub
 
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
                 // TODO Auto-generated method stub
 
             }
 
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_P && e.isAltDown() && e.isShiftDown() && !e.isControlDown()) {
                     setResizable(true);
                 }
 
             }
         });
     }
 }
