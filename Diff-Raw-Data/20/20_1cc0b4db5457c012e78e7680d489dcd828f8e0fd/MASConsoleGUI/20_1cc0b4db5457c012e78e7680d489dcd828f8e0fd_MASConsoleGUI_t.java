 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.runtime;
 
 import jason.infra.centralised.RunCentralisedMAS;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
import java.io.FileWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.LogManager;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 
 /** the GUI console to output log messages */
 public class MASConsoleGUI {
 
     private static MASConsoleGUI masConsole        = null;
     public  static String        isTabbedPropField = MASConsoleLogHandler.class.getName() + ".tabbed";
 
     private boolean              isTabbed          = false;
 
     /** for singleton pattern */
     public static MASConsoleGUI get() {
         if (masConsole == null) {
             masConsole = new MASConsoleGUI("MAS Console");
         }
         return masConsole;
     }
 
     public static boolean hasConsole() {
         return masConsole != null;
     }
 
     private Map<String, JTextArea>       agsTextArea       = new HashMap<String, JTextArea>();
     private JTabbedPane                  tabPane;
     private JFrame              frame   = null;
     private JTextArea           output;
     private JPanel              pBt     = null;
     private OutputStreamAdapter out;
     private boolean             inPause = false;
 
     private MASConsoleGUI(String title) {
         String tabbed = LogManager.getLogManager().getProperty(isTabbedPropField);
         if (tabbed != null && tabbed.equals("true")) {
             isTabbed = true;
         }
 
         frame = new JFrame(title);
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 close();
             }
         });
 
         output = new JTextArea();
         output.setEditable(false);
 
         JPanel pcenter = new JPanel(new BorderLayout());
         if (isTabbed) {
             tabPane = new JTabbedPane(JTabbedPane.LEFT);
             tabPane.add("common", new JScrollPane(output));
             pcenter.add(BorderLayout.CENTER, tabPane);
         } else {
             pcenter.add(BorderLayout.CENTER, new JScrollPane(output));
         }
 
         pBt = new JPanel();
         pBt.setLayout(new FlowLayout(FlowLayout.CENTER));
 
         frame.getContentPane().setLayout(new BorderLayout());
         frame.getContentPane().add(BorderLayout.CENTER, pcenter);
         frame.getContentPane().add(BorderLayout.SOUTH, pBt);
 
         JButton btClean = new JButton("Clean", new ImageIcon(RunCentralisedMAS.class.getResource("/images/clear.gif")));
         btClean.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 output.setText("");
             }
         });
 
         addButton(btClean);
 
         frame.setBounds(200, 20, 824, 500);
     }
 
     public void setTitle(String s) {
         frame.setTitle(s);
     }
 
     public JFrame getFrame() {
         return frame;
     }
 
     public void addButton(JButton jb) {
         pBt.add(jb);
         pBt.revalidate();
         // pack();
     }
 
     synchronized public void setPause(boolean b) {
         inPause = b;
         notifyAll();
     }
 
     synchronized void waitNotPause() {
         try {
             while (inPause) {
                 wait();
             }
         } catch (Exception e) { }
     }
 
     public boolean isPause() {
         return inPause;
     }
 
     public void append(String s) {
         append(null, s);
     }
 
     public void append(String agName, String s) {
         try {
             if (!frame.isVisible()) {
                 frame.setVisible(true);
             }
             if (inPause) {
                 waitNotPause();
             }
             if (isTabbed && agName != null) {
                 JTextArea ta = agsTextArea.get(agName);
                 if (ta == null) {
                     ta = new JTextArea();
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
                 output.append(s);
             }
         } catch (Exception e) {
             close();
             System.out.println(e); 
         }
     }
 
     public void close() {
         setPause(false);
         if (masConsole != null && masConsole.frame != null)
             masConsole.frame.setVisible(false);
         if (out != null)
             out.restoreOriginalOut();
        try {
            if (RunCentralisedMAS.getRunner() != null) {
                FileWriter f = new FileWriter(RunCentralisedMAS.stopMASFileName);
                f.write(32);
                f.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
         masConsole = null;
     }
 
     public void setAsDefaultOut() {
         out = new OutputStreamAdapter(this, null);
         out.setAsDefaultOut();
     }
 
 }
