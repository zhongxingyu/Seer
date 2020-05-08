 //----------------------------------------------------------------------------
 // $Id$
 //----------------------------------------------------------------------------
 
 package hexgui.gui;
 
 import hexgui.hex.*;
 import hexgui.htp.HtpController;
 
 import javax.swing.*;          
 import javax.swing.text.*;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.ListSelectionEvent;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import java.util.*;
 
 /** Non-modal dialog displaying list of htp commands of the connected
     HTP compatible program. */
 public class AnalyzeDialog 
     extends JDialog implements ActionListener, ListSelectionListener
 {
 
     public interface Callback 
     {
 	void analyzeCommand(String str);
     }
 
     public AnalyzeDialog(JFrame owner, Callback callback)
     {
 	super(owner, "HexGui: Analyze");
 
         m_callback = callback;
 
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 
         m_list = new JList();
         m_list.addListSelectionListener(this);
         m_list.setDragEnabled(false);
         m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 	m_scrollpane = new JScrollPane(m_list);
 	m_scrollpane.setVerticalScrollBarPolicy(
 		     JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
         JPanel runpanel = new JPanel();
         m_run = new JButton("Run");
         m_run.addActionListener(this);
         m_run.setActionCommand("run");
 
         runpanel.add(m_run);
 
         panel.add(m_scrollpane);
         panel.add(runpanel);
         add(panel);
 
         pack();
 
 
 	Dimension size = owner.getSize();
 	setLocation(size.width, size.height);
         
         setVisible(true);
     }
 
     public void setCommands(Vector<String> commands)
     {
         m_commands = commands;
         m_list.setListData(commands);
     }
 
     public void valueChanged(ListSelectionEvent e)
     {
        int index = m_list.getSelectedIndex();
     }
 
     public void actionPerformed(ActionEvent e)
     {
         String what = e.getActionCommand();
         if (what.equals("run")) {
             int index = m_list.getSelectedIndex();
             if (index != -1) {
                 String cmd = m_commands.get(index) + "\n";
                 m_callback.analyzeCommand(cmd);
             }
         }
     }
 
     Callback m_callback;
     
     JList m_list;
     JScrollPane m_scrollpane;
     Vector<String> m_commands;
    
     JButton m_run;
 }
