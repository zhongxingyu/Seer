 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.Socket;
 import javax.swing.BoxLayout;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 /**
  * Client.java
  *
  * Uppgift 4.1.1 (Stream-sockets: klient) i kursen Internetprogrammering 1
  * GUI- och sändardel av klienten.
  *
  * @author André Karlsson
  *
  */
 public class Client extends JFrame {
 
     private JPanel content;
     private JScrollPane scrollPane;
     private JTextField messageBox;
     private JTextArea chatBox;
     private static String host = "0";
     private static String port = "0";
     Socket s;
     PrintWriter out;
     BufferedReader in;
 
     public static void main(String[] args) throws MalformedURLException, IOException {
 
         System.out.println(args.length);
         switch (args.length) {
             case 0: {
                 host = "127.0.0.1";
                 port = "2000";
                 break;
             }
             case 1: {
                 host = args[0];
                 port = "2000";
                 break;
             }
             case 2: {
                 host = args[0];
                 port = args[1];
                 break;
             }
         }
 
         new Client().setVisible(true);
 
 
     }
 
     public Client() throws IOException {
         setSize(640, 480);
         setDefaultCloseOperation(EXIT_ON_CLOSE);
 
         content = new JPanel();
         messageBox = new JTextField();
         chatBox = new JTextArea();
         scrollPane = new JScrollPane(chatBox);
 
         content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
         chatBox.setMinimumSize(new Dimension(640, 460));
         messageBox.setMaximumSize(new Dimension(640, 25));
 
         messageBox.addActionListener(new EnterListener()); //Lyssna efter enter
 
         content.add(scrollPane);
         content.add(messageBox);
 
         chatBox.setEditable(false);
 
         getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
         getContentPane().add(content);
 
         /*Ge meddelanderutan fokus från start*/
         addWindowListener(new WindowAdapter() {
 
             @Override
             public void windowOpened(WindowEvent e) {
                 messageBox.requestFocus();
             }
         });
 
         /*Skapa socket och lyssningstråd*/
         createSocket();
 
     }
 
     /**
      * Metod för att initiera socket och in- och utströmmar samt starta en lyssnartråd.
      *
      */
     private void createSocket() {
 
         try {
             s = new Socket(host, Integer.valueOf(port)); //skapa ny socket
 
             setTitle(host + ":" + port); //sätt titelrad i fönster
             in = new BufferedReader(new InputStreamReader(s.getInputStream()));
             out = new PrintWriter(s.getOutputStream(), true);
             ClientThread listen = new ClientThread(in, chatBox); //skapa lyssnartråd
             listen.start(); //starta tråd
         } catch (IOException ex) {
             System.err.println("Någonting gick fel, kanske fel Host eller port?");
             System.exit(1);
         }
     }
 
     class EnterListener implements ActionListener {
 
         public void actionPerformed(ActionEvent e) {
             out.println(messageBox.getText()); //skicka text till server
            chatBox.append(messageBox.getText() + "\n");
             messageBox.setText("");
         }
     }
 }
