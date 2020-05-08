 package jason.runtime;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 
 /**
  * A quick implementation of a TextPane with default coloring for Jason.
 
  * @author Felipe Meneguzzi
  */
 public class MASConsoleColorGUI extends MASConsoleGUI {
     private Map<String, MASColorTextPane>     agsTextArea       = new HashMap<String, MASColorTextPane>();
     private Hashtable<String, Color>          agsColours        = new Hashtable<String, Color>();
     private MASColorTextPane                  output;
     
     private MASConsoleColorGUI(String title) {
         super(title);
     }
     
     /** for singleton pattern */
     public static MASConsoleGUI get() {
         if (masConsole == null) {
             masConsole = new MASConsoleColorGUI("MAS Console");
         }
         return masConsole;
     }
 
     @Override
     protected void initOutput() {
         output = new MASColorTextPane(Color.black);
         output.setEditable(false);
         if (isTabbed()) {
             tabPane.add("common", new JScrollPane(output));
         } else {
             pcenter.add(BorderLayout.CENTER, new JScrollPane(output));
         }
     }
 
     @Override
     public void append(String agName, String s) {
         try {
             Color c = null;
             if (agName != null) {
                 c = agsColours.get(agName);
                 if (c == null) {
                     c = MASColorTextPane.getNextAvailableColor();
                     agsColours.put(agName, c);
                 }
             }          
             
             if (!frame.isVisible()) {
                 frame.setVisible(true);
             }
             if (inPause) {
                 waitNotPause();
             }
             if (isTabbed() && agName != null) {
                 MASColorTextPane ta = agsTextArea.get(agName);
                 if (ta == null) {
                     ta = new MASColorTextPane(c);
                     ta.setEditable(false);
                     agsTextArea.put(agName, ta);
                     tabPane.add(agName, new JScrollPane(ta));
                 }
                 if (ta != null) { // no new TA was created
                     // print out
                     int l = ta.getDocument().getLength();
                     if (l > 100000) {
                         ta.setText("");
                         // l = output.getDocument().getLength();
                     }
                     ta.append(s);
                     // output.setCaretPosition(l);
                 }
             }
 
             // print in output
             int l = output.getDocument().getLength();
             if (l > 60000) {
                 output.setText("");
                 // l = output.getDocument().getLength();
             }
             synchronized (this) {
                 output.append(c, s);
             }
         } catch (Exception e) {
             close();
             System.out.println(e);
             e.printStackTrace();
         }
     }
 
 }
 
 class MASColorTextPane extends JTextPane {
     protected static final Color seq[] = {//Color.black,
                                           Color.blue, 
                                           Color.red,
                                           Color.gray, 
                                           Color.cyan,
                                           Color.magenta,
                                          Color.orange,
                                          Color.pink,
                                          Color.green,
                                          Color.yellow};
     protected static int change = 0;
     protected static int lastColor = 0;
     
     public static Color getNextAvailableColor() {
         if(change > 0) {
             seq[lastColor] = (change%2 == 1)?seq[lastColor].brighter():seq[lastColor].darker();
         }
         Color c = seq[lastColor];
         lastColor = (lastColor+1)%seq.length;
         if(lastColor == 0) {
             change++;
         }
         return c;
     }
     
     protected Color defaultColor;
     
     public MASColorTextPane(Color defaultColor) {
         this.defaultColor = defaultColor;
     }
     
     public void append(String s) {
         append(defaultColor, s);
     }
     
     public void append(Color c, String s) {
         if (c == null)
             c = defaultColor;
         StyleContext sc = StyleContext.getDefaultStyleContext();
         AttributeSet as = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
         try {
             getDocument().insertString(getDocument().getLength(), s, as);
         } catch (BadLocationException e) {
             e.printStackTrace();
         } 
     }
 }
 
