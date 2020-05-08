 /*
  * Author: Peter O'Connor
  * Purpose: To implement SCADA Monitoring throughout Washington County
  * Version: 1.0a
  * 
  * Contact: avogadrosg1@gmail.com
  * 
  */
 package washcoscadaserver;
 
 import gui.PagingGUI;
 import gui.ServerGUI;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 
 public class SCADARunner
 {
 
     static final Logger log = Logger.getGlobal();
     
     static JTextArea mainArea;
     //static JFrame frame;
     static SCADAServer server;
     //static JPanel pagingHolder;
     //static modem.PageWithModem pagerServer = new modem.PageWithModem();
     //static log.LoggingSystem logServer = new log.LoggingSystem();
     static PagingGUI pagingGUI;
     //static ControlPanel controls;
     
     static ServerGUI gui;
     
     public static void main(String[] args) 
     {
         String[] verbose = {"v"};
         dispatch(verbose);
         
         server = new SCADAServer();
         //frame = new JFrame("SCADA Monitor GUI");
         //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //frame.setLayout(new BorderLayout());
         //frame.setSize(700,700);
         
         //JLabel title = new JLabel("SCADA Server");
         //JPanel main = new JPanel();
         //JPanel titlePanel = new JPanel();
         //controls = new ControlPanel(server, frame);
         //frame.add(controls, BorderLayout.EAST);
         
         //titlePanel.setPreferredSize(new Dimension(500,30));
         //main.setPreferredSize(new Dimension(500,700));
         
         mainArea = new JTextArea(30,28);
         mainArea.setText("Initializing.");
         mainArea.setEditable(false);
         /*
         JScrollPane scrollStatus = new JScrollPane(mainArea);
         scrollStatus.setPreferredSize(new Dimension(500,700));
         scrollStatus.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
         scrollStatus.setAutoscrolls(true);
         */
         
         //main.add(scrollStatus);
         //frame.add(main, BorderLayout.CENTER);
         
         //pagingHolder = new JPanel();
         //pagingHolder.setPreferredSize(new Dimension(700, 250));
         /*JLabel labelTemp = new JLabel("Paging System Inactive.");
         pagingHolder.add(labelTemp);*/
         
        
         //server.switchPaging();
         //server.pageServ.getPagingGUI().setPreferredSize(new Dimension(700,250));
         //pagingHolder.add(server.pageServ.getPagingGUI());
         //frame.add(server.pageServ.getPagingGUI(), BorderLayout.SOUTH);
         
         //frame.add(pagingHolder, BorderLayout.SOUTH);
         
         //title.setFont(Font.getFont("Calibri"));
         //title.setForeground(Color.RED);
 
         //titlePanel.add(title);
         //frame.add(titlePanel,BorderLayout.NORTH);
         gui = new ServerGUI(server.getPageServ().getPagingGUI().getPagingSystem());
         
         Timer bob = new Timer(1000, new TimerListener());
         bob.start();
         //frame.setVisible(true);
         gui.setVisible(true);
     }
     
     static void dispatch(String[] args)
     {
         for(String s: args)
         {
             s = s.replaceAll("-", "");
             char command = s.charAt(0);
             
             switch (command)
             {
                 case 'v':
                     log.setLevel(Level.ALL);
             try 
             {
                 FileHandler fh = new FileHandler("log.xml");
                 log.addHandler(fh);
                 log.info("Hai");
             } catch (IOException ex) 
             {
                 Logger.getGlobal().info(ex.toString());
             } catch (SecurityException ex) 
             {
                 Logger.getGlobal().info(ex.toString());
             }
             }
         }
     }
     
     static class TimerListener implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
            if(server.isChecking())
             {
                 mainArea.setText("Status:\n");
                 mainArea.append(server.getInformation());
                 //frame.repaint();
             }
             else
             {
                 mainArea.setText("SCADA Server Offline. Please start.");
             }
         }
 
     }
     
     public static SCADAServer getServer() {
         return server;
     }
     
     public static JTextArea getLogArea() {
         return mainArea;
     }
 }
