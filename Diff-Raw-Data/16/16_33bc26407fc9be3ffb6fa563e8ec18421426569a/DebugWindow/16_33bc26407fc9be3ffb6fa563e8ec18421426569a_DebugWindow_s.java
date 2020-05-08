 package com.btbb.hoiley;
 
 import javax.swing.JInternalFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 
 import org.lateralgm.main.LGM;
 
 public class DebugWindow {
 
     private static DebugWindow window;
     private JTextPane pane;
     private JScrollPane scroll;
     private JInternalFrame f;
 
     private DebugWindow()
         {
             f = new JInternalFrame("Runero");
             f.setMaximizable(true);
             f.setClosable(true);
             f.setResizable(true);
             f.setSize(640, 480);
             pane = new JTextPane();
             pane.setEditable(false);
             scroll = new JScrollPane(pane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
             f.add(scroll);
             LGM.mdi.add(f);
         }
 
     public void log(Exception e) {
         String ex = "";
         for (StackTraceElement st : e.getStackTrace())
             ex += st + "\n";
         pane.setText(pane.getText() + e.getClass().getCanonicalName() + "\n" + ex);
     }
 
     public void log(String msg) {
         pane.setText(pane.getText() + msg + "\n");
     }
     
     public void reset() {
         pane.setText("");
     }
 
     public static DebugWindow getWindow() {
         if (window == null)
             window = new DebugWindow();
         window.f.setVisible(true);
         window.f.toFront();
         return window;
     }
 }
