 package com.drexel.duca.frontend;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import com.drexel.duca.backend.RSAKeypair;
 import com.drexel.duca.backend.STUser;
 
 public class ChatWindow implements Runnable {
 
     private JFrame frmChatWindow;
     private JTextArea textArea;
     private JTextField textField;
     private JButton btnSend;
     private JPanel bottomPanel;
     private JScrollPane scrollPane;
     private Scanner sInput;
     private PrintStream sOutput;
     private RSAKeypair encryption;
     private RSAKeypair decryption;
 
     /**
      * Create the application.
      */
     public ChatWindow(Socket socket, ArrayList<STUser> friends) {
         try {
             String ipToLookFor = ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().toString().substring(1);
             STUser personTalking = null;
             System.out.println(ipToLookFor);
             for (STUser friend : friends) {
                 System.out.println(friend.getIp());
                 if (friend.getIp().equals(ipToLookFor)) {
                     personTalking = friend;
                     break;
                 }
             }
             if (personTalking == null) {
                 socket.close();
                 return;
             }
             sInput = new Scanner(socket.getInputStream());
             sOutput = new PrintStream(socket.getOutputStream());
             decryption = new RSAKeypair();
             sOutput.println(decryption.getE() + " " + decryption.getC());
             int e = sInput.nextInt();
             int c = sInput.nextInt();
             sInput.nextLine();// flush new line from buffer
             encryption = new RSAKeypair(e, c);
             initialize("Chat with " + personTalking.getUsername());
             new Thread(this).start();
         } catch (IOException e) {
             e.printStackTrace();
         } //STUFF TO ADDDDD catch NoSuchElementException (if they left)
     }
 
     /**
      * Initialize the contents of the frame.
      */
     private void initialize(String title) {
         frmChatWindow = new JFrame();
         frmChatWindow.setTitle(title);
         frmChatWindow.setBounds(100, 100, 450, 300);
         frmChatWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         frmChatWindow.getContentPane().setLayout(new BoxLayout(frmChatWindow.getContentPane(), BoxLayout.Y_AXIS));
 
         textArea = new JTextArea();
         textArea.setRows(15);
        textArea.setEditable(false);
         scrollPane = new JScrollPane(textArea);
         frmChatWindow.getContentPane().add(scrollPane);
 
         bottomPanel = new JPanel();
         bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
 
         textField = new JTextField();
         bottomPanel.add(textField);
         textField.addKeyListener(new KeyListener() {
             @Override
             public void keyPressed(KeyEvent e) {
                 // TODO Auto-generated method stub
             }
 
             @Override
             public void keyReleased(KeyEvent e) {
                 // TODO Auto-generated method stub
             }
 
             @Override
             public void keyTyped(KeyEvent e) {
                 if (e.getKeyChar() == 10) {
                     sendText();
                 }
             }
         });
 
         btnSend = new JButton("Send");
         btnSend.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 sendText();
             }
         });
         bottomPanel.add(btnSend);
 
         frmChatWindow.getContentPane().add(bottomPanel);
     }
 
     private void sendText() {
         String msg = textField.getText();
 
         if (!msg.isEmpty()) {
             sOutput.println(encryption.encrypt(msg));
             textArea.append(msg + "\n");
             textField.setText("");
         }
     }
 
     public JFrame getChatWindowFrame() {
         return frmChatWindow;
     }
 
     @Override
     public void run() {
         try {
             while (true) {
                 String msg = decryption.decrypt(sInput.nextLine());
                 if (!msg.isEmpty()) {
                     textArea.append(msg + "\n");
                 }
             }
         } catch (NoSuchElementException e) {
             
         }
     }
 }
