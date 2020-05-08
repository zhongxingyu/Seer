 package Client;
 import Common.Encryptor;
 import java.awt.*;
 import java.awt.event.*;
 import java.net.Socket;
 import javax.swing.*;
 
 public class ChatClient extends JFrame implements ActionListener {
     private String name;
     private String pass;
     private boolean encrypted;
 
     
     private JTextArea  enteredText = new JTextArea(10, 32);
     private JTextField typedText   = new JTextField(32);
   
     
     private AudioClient audioClient = null; //the audio chat client
 
     // socket for connection to chat server
     private Socket socket;
 
     // for writing to and reading from the server
     private Out out;
     private In in;
 
     public ChatClient(String hostName, String port) 
     {
         System.setProperty("Apple.laf.usedScreenMenuBar", "true");
         
         // connect to server
         try {
             socket = new Socket(hostName, Integer.parseInt(port));
             out    = new Out(socket);
             in     = new In(socket); //IF SERVER SENDS A SINGLE MESSAGE W/ RIGHT CURLY BRACE, 
         }
         catch (Exception ex) { ex.printStackTrace(); }
 
         // close output stream  - this will cause listen() to stop and exit
         addWindowListener(
             new WindowAdapter() {
                 public void windowClosing(WindowEvent e) {
                     out.close();
                     
                     
                    // in.close();
                     //try                   { socket.close();        }
                     //catch (Exception ioe) { ioe.printStackTrace(); }
                 }
             }
         );
 
 
         // create GUI stuff
         enteredText.setEditable(false);
         enteredText.setBackground(Color.BLACK);
         enteredText.setForeground(Color.GREEN);
         typedText.setForeground(Color.GREEN);
         typedText.setBackground(Color.BLACK);
         typedText.addActionListener(this);
         
         
         JOptionPane getName = new JOptionPane();
         String nicky = getName.showInputDialog("Please input your nickname.");//gets name
         System.out.println("Name: "+nicky);
         out.println(nicky);
         name = nicky;
         
         if (!socket.isConnected())
         {
             JOptionPane getNameAgain = new JOptionPane();
             String nik = getNameAgain.showInputDialog("That is not a valid nickname.  Please input your nickname.");//gets name
             System.out.println("Name: "+nik);
             out.println(nik);
             name = nik;
         }
         
         JOptionPane getPass = new JOptionPane();
         String pizzle = getPass.showInputDialog("Please input your password.");//gets name
         out.println(pizzle);
         pass= pizzle;
         
         if (!socket.isConnected())
         {
             JOptionPane getPassAgain = new JOptionPane();
             String paa = getPassAgain.showInputDialog("That is not a valid password.  Please input your password.");//gets name
             
             out.println(paa);
             pass = paa;
         }
         
         
         
         
         
         
         Container content = getContentPane();
         content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
         content.add(typedText, BorderLayout.SOUTH);
         JMenuBar menubar = new JMenuBar();
         JMenu menu = new JMenu();
         JMenuItem leave = new JMenuItem("Exit chat room");
         leave.addActionListener(this);
         menubar.setVisible(true);
         this.setJMenuBar(menubar);
         // menubar.validate();
         //content.add(menubar);
         content.validate();
         //content.add(menubar);
         
         
         //  Create jframe for audio w/ controls
         
         
         
         
         
         
         
         
         
         
         //listen();
         
         
         
         
         // to encrypt, use /encrypt & encryption class
         //put button 
         //every time message is sent while encryption is on, put {slash in front of it
         // display the window, with focus on typing box
         setTitle("Chat Client 9000!!!!: [" + hostName + ":" + port + "]");
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         pack();
         typedText.requestFocusInWindow();
         setVisible(true);
 
         listen();
     }
 
     
     
     
     
     // process TextField after user hits Enter
     public void actionPerformed(ActionEvent e) 
     {
         if (e.getSource() instanceof JMenuItem)
         {
             JOptionPane finalMess = new JOptionPane();
             String fina = finalMess.showInputDialog("Any final words, coward?");
             out.println(fina);
             out.println("/disconnect");
         }
         else if (e.getSource() instanceof JTextField)
         {
             String outy = typedText.getText();
             out.println(outy);
             typedText.setText("");
             typedText.requestFocusInWindow();
         }
         //else if 
         
                
     }
     
     
     // listen to socket and print everything that server broadcasts
     public void listen() 
     {
         String s;
         while ((s = in.readLine()) != null) 
         {
             if (s.equals("/accept"))
             {
                 try
                 {
                     System.out.println("Client making new audio chat socket on port: "+(socket.getPort()+1));
                 if (audioClient==null)
                 { audioClient = new AudioClient(new Socket(socket.getInetAddress(), socket.getPort()+1)); audioClient.start(); }
                 
                 else 
                     System.out.println("Duplicate audio chat clients attempted...?");
                 } catch (Exception e){ e.printStackTrace(); }
             }
             
             else if (s.equals("/decline"))
             {
                 if (audioClient!=null){ audioClient.stopRunning(); audioClient = null; }
             }
             
             else
             {
             enteredText.insert(s + "\n", enteredText.getText().length());
             enteredText.setCaretPosition(enteredText.getText().length());   
             
             }
         }
         out.close();
         in.close();
         try                 { socket.close();      }
         catch (Exception e) { e.printStackTrace(); }
         System.err.println("Closed client socket");
     }
 
     public static void main(String[] args)  
     {
        ChatClient client2 = new ChatClient("zadikian.info","9000");
        ChatClient client3 = new ChatClient("zadikian.info","9000");
     } 
 }
