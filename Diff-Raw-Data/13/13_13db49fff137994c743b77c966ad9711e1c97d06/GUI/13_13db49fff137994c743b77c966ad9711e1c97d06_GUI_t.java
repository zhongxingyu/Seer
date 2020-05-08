 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import javax.swing.*;
 
 public class GUI
 {
     private JFrame window;
     private JPanel sendFrame;
     private JPanel readFrame;
     private JTabbedPane actionPane;
     private JProgressBar waiting;
     
     private JTextField sendIP;
     private JSpinner sendPort;
     private JFileChooser sendChooser;
     private JTextField sendFilename;
     private JButton sendChoose;
     private JButton sendDo;
     
     private JSpinner readPort;
     private JFileChooser readChooser;
     private JTextField readFilename;
     private JButton readChoose;
     private JButton readDo;
 
     final private Runnable networksuccess;
     final private Runnable networkfail;
     
     public static void main(String[] args)
     {
         GUI x = new GUI();
     }
     
     public GUI()
     {
         networksuccess = (new Runnable() {
             public void run()
             {
                 waiting.setIndeterminate(false);
                waiting.setString("");
             }
         });
 
         networkfail = (new Runnable() {
             public void run()
             {
                 waiting.setIndeterminate(false);
                waiting.setString("");
                 JOptionPane.showMessageDialog(null, "Network operation failed", "Error", JOptionPane.ERROR_MESSAGE);
             }
         });
 
         window = new JFrame("FileCat GUI");
         sendFrame = new JPanel();
         readFrame = new JPanel();
         actionPane = new JTabbedPane();
         waiting = new JProgressBar(0, 100);
         
         sendIP = new JTextField();
         sendPort = new JSpinner(new SpinnerNumberModel(2555, 0, 60000, 1));
         sendChoose = new JButton("Open...");
         sendChooser = new JFileChooser();
         sendFilename = new JTextField();
         sendDo = new JButton("Send File");
         
         readPort = new JSpinner(new SpinnerNumberModel(2555, 0, 60000, 1));
         readChoose = new JButton("Save...");
         readChooser = new JFileChooser();
         readFilename = new JTextField();
         readDo = new JButton("Read File");
 
         configGUI();
         
         window.pack();
         window.setVisible(true);
     }
     
     /*
     Takes a list of components and packs each component in a new row.
      */
     private void packColumn(Container container, JComponent[] components)
     {
         for (JComponent component : components)
         {
             container.add(component);
         }
     }
     
     /*
     High level GUI configuration and initialization.
      */
     private void configGUI()
     {
         window.getContentPane().setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS));
         window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);        
         window.getContentPane().add(actionPane);
         window.getContentPane().add(waiting);
         
         actionPane.addTab("Send File", sendFrame);
         actionPane.addTab("Read File", readFrame);
 
         sendFrame.setLayout(new GridLayout(4, 2));
         readFrame.setLayout(new GridLayout(3, 2));
         
         JComponent[] sendComponents = {
                 (JComponent)new JLabel("IP Address"),
                 sendIP,
                 (JComponent)new JLabel("Port #"),
                 sendPort,
                 sendChoose,
                 sendFilename,
                 sendDo,
         };
         packColumn(sendFrame, sendComponents);
         
         JComponent[] readComponents = {
                 (JComponent)new JLabel("Port #"),
                 readPort,
                 readChoose,
                 readFilename,
                 readDo,
         };
         packColumn(readFrame, readComponents);
         
         sendDo.addActionListener(new ActionListener() {
            @Override
             public void actionPerformed(ActionEvent e)
             {
                 waiting.setIndeterminate(true);
                waiting.setString("Waiting for network...");
 
                 final String ip = sendIP.getText();
                 final Integer port = (Integer)sendPort.getValue();
                 final String filename = sendFilename.getText();
 
                 new Thread(new Runnable() {
                     public void run()
                     {
                         boolean failed = false;
                         try
                         {
                             Networking.do_write(ip, port, filename);
                         }
                         catch (IOException ex)
                         {
                             failed = true;
                         }
 
                         try
                         {
                             if (failed)
                                 SwingUtilities.invokeLater(networkfail);
                             else
                                 SwingUtilities.invokeLater(networksuccess);
                         }
                         catch (Exception ex)
                         {
                         }
                     }
                }).start();
             }
         });
         
         readDo.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) 
             {
                 waiting.setIndeterminate(true);
                waiting.setString("Waiting for network...");
 
                 final Integer port = (Integer)readPort.getValue();
                 final String filename = readFilename.getText();
 
                 new Thread(new Runnable() {
                     public void run()
                     {
                         boolean failed = false;
                         try
                         {
                             Networking.do_read(port, filename);
                         }
                         catch (IOException ex)
                         {
                             failed = true;
                         }
 
                         try
                         {
                             if (failed)
                                 SwingUtilities.invokeLater(networkfail);
                             else
                                 SwingUtilities.invokeLater(networksuccess);
                         }
                         catch (Exception ex)
                         {
                         }
                     }
                }).start();
             }
         });
         
         readChoose.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) 
             {
                 int status = readChooser.showSaveDialog(window);
                 if (status == JFileChooser.APPROVE_OPTION)
                 {
                     readFilename.setText(readChooser.getSelectedFile().getAbsolutePath());   
                 }
             }
         });
 
         sendChoose.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 int status = sendChooser.showSaveDialog(window);
                 if (status == JFileChooser.APPROVE_OPTION)
                 {
                     sendFilename.setText(sendChooser.getSelectedFile().getAbsolutePath());
                 }
             }
         });
     }
 }
