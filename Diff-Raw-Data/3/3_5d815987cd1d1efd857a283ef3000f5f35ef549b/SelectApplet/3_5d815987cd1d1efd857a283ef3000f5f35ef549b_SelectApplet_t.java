 package org.softwarehelps.learncs;
 
 import java.awt.Button;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.InputStream;
 import java.util.Scanner;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 
 /**
  * Allow user to select Explorations Applet by clicking on button in
  * page that describes each Applet organized by Lab number.
  * 
  * @author Peter Dobson
  */
 public class SelectApplet extends JPanel {
     
     SelectApplet() {
         
         start();
         
         //addTitle("Explorations in Computer Science");
         
         addLab("Lab 1: Introduction to the Labs");
         addApplet("Introduction", "INTRO", 
                 "tell us about yourself and practice taking screenshots");
                         
         addLab("Lab 2: Exploring Number Systems");
         addApplet("Number systems", "NUMSYS", 
                 "conversions between base 10 and some other bases");
         addApplet("Binary addition", "BINARYADD", 
                 "convert numbers to binary and add them");
         
         addLab("Lab 3A: Representing Numbers");
         addApplet("Negative binary numbers", "NEGNUMS", 
                 "sign-magnitude form and 2's complement form");
         addApplet("Real number representations", "REALS", 
                 "scientific notation in decimal and in binary");
         
         addLab("Lab 3B: Colorful Characters");
         addApplet("Character codes (ASCII and Unicode)", "CHARCODES", 
                 "show the corresponding ASCII or Unicode values for "
                 + "\ncharacters (up to 255)");
         addApplet("Text translator into ASCII", "TEXTCODES", 
                 "display a list of the Unicode values, and allow some "
                 + "\ntext to be converted to numeric codes");
         addApplet("Color maker", "COLORS", 
                 "display colors in RGB and HSB formats");
         
         addLab("Lab 3C: Compressing Text");
         addApplet("Text compression using keywords", "COMPRESSOR", 
                 "compress and decompress text using a keyword table");
         addApplet("Text compression using Huffman encoding", "COMPRESSOR2", 
                 "compress and decompress text using a Huffman encoding");
         
         addLab("Lab 4: Logic Circuits");
         addApplet("LogicGates", "LOGICGATES", 
                 "A logic gate circuit simulator");
         
         
         addLab("Lab 5: Computer Cycling");
         addApplet("Super Simple CPU", "CPU", 
                 "a complete, working computer that illustrates the "
                 + "\nfetch/decode/execute cycle.");
         
         addLab("Lab 7: Low-Level Languages");
         addApplet("Super Simple CPU", "CPU", 
                 "the same as used in Lab 5 (above)");
         
         addLab("Lab 8: Using Algorithms for Painting");
         addApplet("Palgo", "PALGO", 
                 "\"painting algorithmically\", a simple paint environment "
                 + "\nwhere a program directs the paintbrush. Also allows "
                 + "\npurely textual programming.");
         
         addLab("Lab 9A: Searching for the Right Sort");
         addApplet("Stacks and queues", "STACKQUEUE", 
                 "difference between stacks and queues");
         addApplet("Trees", "TREES", "see how binary trees work");
         
         addLab("Lab 9B: Searching for the Right Sort");
         addApplet("Sorting", "SORT", 
                 "selection sort, bubble sort, quick sort");
         addApplet("Searching", "SEARCH", 
                 "sequential versus binary search");
         
         addLab("Lab 10: Operating Systems");
         addApplet("Placement of jobs in memory", "MEMFITTING", 
                 "contiguous allocation of memory for jobs, using various "
                 + "\nfitting algorithms");
         addApplet("Scheduling of jobs", "SCHEDULING", 
                 "using FSCS, SJF and Round Robin");
         
         addLab("Lab 11: Disk Scheduling");
         addApplet("Disk Scheduling", "DISKSCHED", 
                 "FSCS, SSTF and SCAN (elevator)");
         
         addLab("Lab 12B: Databases");
         addApplet("Simple SQL", "SIMPLESQL", 
                 "A tiny relational database program that processes "
                 + "\nSQL queries");
         
         addLab("Lab 13: Artificial Intelligence");
         addApplet("Semantic networks", "SEMNET", "logic deduction");
         addApplet("Eliza therapist", "ELIZA", 
                 "conversational computer program using some simple rules "
                 + "\nfor textual transformation");
         
         addLab("Lab 14: Simulating Life and Heat");
         addApplet("Game of Life", "GAMEOFLIFE", 
                 "the classic cellular automaton");
         addApplet("Heat transfer", "HEAT", 
                 "colorful animation of dissipation of heat");
         
         addLab("Lab 15: Networking");
         addApplet("TCP/IP", "TCPIP", 
                 "reliable connection, ensured delivery of packets between "
                 + "\ntwo nodes in a network, allows user to damage or "
                 + "\ndestroy packets");
         addApplet("Network router", "NETWORK", 
                 "illustrates how routing decisions are made");
         
         addLab("Lab 17: Limits of Computing");
         addApplet("Comparison of several functions", "FUNCGROWTH", 
                 "shows how f(N) gets very large");
         addApplet("Plotter", "PLOTTER", 
                 "a way to visualize the growth rates of functions");
         addApplet("Traveling Salesperson Problem", "TSP", 
                 "run the algorithm on various graphs to find the shortest "
                 + "\ncomplete route (if there is one). Beware! If you animate "
                 + "\nthe search and the graph is reasonably large, the applet "
                 + "\nwill take a very long time to finish!");
 
         end();
     }
     
     GridBagConstraints cOuter;
     //JList labsList;
     //DefaultListModel labsListModel;
     JTabbedPane labsPane;
     JComponent lab;    
     GridBagConstraints cLab;
     
     final void start() {
         
         setLayout(new GridBagLayout());        
         cOuter = new GridBagConstraints();
         
         cOuter.anchor = GridBagConstraints.CENTER;
         cOuter.ipadx = 15; cOuter.ipady = 15;
         
         labsPane = new JTabbedPane(JTabbedPane.TOP, 
                 JTabbedPane.WRAP_TAB_LAYOUT);        
         add(labsPane,cOuter);
         cOuter.gridy++;
     }
     
     final void end() {
         endLab();
         cOuter.weighty = 1.0;
         add(new JLabel(" "),cOuter);
         cOuter.weighty = 0.0;
     }
     
     final void addLab(String title) {
                 
         String[] titleParts = title.split(":",2);
         String labName = titleParts[0];
         String labTitle = titleParts[1].trim();
 
         if (lab != null) {
             endLab();            
         }
         
         lab = new JPanel();
         labsPane.addTab(labName, lab);
         lab.setLayout(new GridBagLayout());
         
         if (cLab == null) {
             cLab = new GridBagConstraints();
             cLab.anchor = GridBagConstraints.FIRST_LINE_START;                
             cLab.ipadx = 15; cLab.ipady = 5;
         }
         cLab.gridx = 0; cLab.gridy = 0;        
         lab.add(new JLabel(" "), cLab);
         cLab.gridx++;
         cLab.gridy++;
         cLab.gridwidth = 3;
         lab.add(new JLabel(title), cLab);
         cLab.gridwidth = 1;
         cLab.gridx = 1;
         cLab.gridy++;
     }
     
     final void endLab() {
         
         cLab.weightx = 1.0;
         cLab.weighty = 1.0;
         cLab.gridx = 4;
         lab.add(new JLabel(" "), cLab);        
         cLab.weightx = 0.0;
         cLab.weighty = 0.0;
     }
     
     final void addApplet(String title, final String folder, String description) {
         
         final AppletLauncher ea = makeExpressionsApplet(folder);
         if (ea == null) {
             // don't show applets that aren't installed correctly
             return;
         }
 
         lab.add(new JLabel(" "), cLab);
         cLab.gridy++;
         Button button = new Button(title);
         button.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 startApplet(ea);
             }
         });
         cLab.fill = GridBagConstraints.HORIZONTAL;        
         lab.add(button, cLab);
         cLab.gridx++;
         
         lab.add(new JLabel(" "),cLab);
         cLab.gridx++;
         
         JTextArea textArea = new JTextArea();  
         textArea.setColumns(35);        
         textArea.setBackground(this.getBackground());
         textArea.setText(description);
         textArea.setAlignmentY(JTextArea.CENTER_ALIGNMENT);
         cLab.anchor = GridBagConstraints.WEST;
         cLab.fill = GridBagConstraints.NONE;
         lab.add(textArea, cLab);
         cLab.gridx = 1;
         cLab.gridy++;                
     }        
     
     public AppletLauncher makeExpressionsApplet(String folderName) {
         
         String appletPackageName = getClass().getPackage().getName() 
                 + "." + folderName;        
         String resourcePath = folderName+"/core.htm";
         InputStream is = getClass().getResourceAsStream(resourcePath);
         Scanner scanner = new Scanner(is);
         scanner.useDelimiter("\\Z");
         String text = scanner.next();
         AppletLauncher ea = new AppletLauncher();
         ea.setPackageName(appletPackageName);
         ea.parseAppletTag(text);
         try {
             ea.getAppletClass();
         } catch (ClassNotFoundException ex) {
             // just leave out this applet from SelectApplet page
             return null;
         }
         
         return ea;
     }
     
     public void startApplet(AppletLauncher ae) {
 
         try {
             ae.launch();
         } catch (IllegalAccessException ex) {
             JOptionPane.showMessageDialog(this, "Illegal Access: "
                     + ae.className, "Error", JOptionPane.ERROR_MESSAGE);
         } catch (InstantiationException ex) {
             JOptionPane.showMessageDialog(this, "Instantiation Exception: "
                     + ae.className, "Error", JOptionPane.ERROR_MESSAGE);
         } catch (ClassNotFoundException ex) {
             JOptionPane.showMessageDialog(this, "Class Not Found: "
                     + ae.className, "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", 
                    JOptionPane.ERROR_MESSAGE);
         }
     }
 }
