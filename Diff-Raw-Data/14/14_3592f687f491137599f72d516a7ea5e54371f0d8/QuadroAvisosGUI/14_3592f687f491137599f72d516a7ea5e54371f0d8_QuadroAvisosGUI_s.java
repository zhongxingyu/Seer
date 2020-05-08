 package com.hstefan.distrib.t2;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.rmi.RemoteException;
 import javax.swing.*;
 
 /**
  *
  * @author yuriks, hstefan
  */
 public class QuadroAvisosGUI {
 
     private final JFrame frame;
     private final JButton groupButton;
     private final JTextField textField;
     private final DefaultListModel<String> messageListModel;
     private QuadroAvisos bboard;
 
     /**
      * Launch the application.
      */
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException e) {
         } catch (InstantiationException e) {
         } catch (IllegalAccessException e) {
         } catch (UnsupportedLookAndFeelException e) {
         }
 
         QuadroAvisosGUI window = new QuadroAvisosGUI();
         window.frame.setVisible(true);
     }
 
     /**
      * Create the application.
      */
     public QuadroAvisosGUI() {
         frame = new JFrame();
         frame.setBounds(100, 100, 450, 300);
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 
         frame.addWindowListener(new WindowAdapter() {
 
             @Override
             public void windowClosing(WindowEvent windowEvent) {
                 leaveGroup();
             }
         });
 
         Container content_pane = frame.getContentPane();
 
         messageListModel = new DefaultListModel<String>();
         JList<String> messageList = new JList<String>(messageListModel);
         content_pane.add(messageList, BorderLayout.CENTER);
 
         JPanel input_section = new JPanel();
         input_section.setLayout(
                 new BoxLayout(input_section, BoxLayout.LINE_AXIS));
         content_pane.add(input_section, BorderLayout.PAGE_END);
 
         textField = new JTextField();
         input_section.add(textField);
 
         JButton sendButton = new JButton("Enviar");
         input_section.add(sendButton);
         frame.getRootPane().setDefaultButton(sendButton);
         sendButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 sendMessage(textField.getText());
                 textField.setText(null);
             }
         });
 
         groupButton = new JButton("Entrar");
         input_section.add(groupButton);
         groupButton.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 if (bboard == null) {
                     joinGroup();
                 } else {
                     leaveGroup();
                 }
             }
         });
     }
 
     void joinGroup() {
         if (bboard != null) {
             leaveGroup();
         }
 
         HostDialog hostDlg = new HostDialog(frame, true);
         hostDlg.setVisible(true);
 
         if (!hostDlg.getSucess()) {
             return;
         }
 
         try {
             bboard = new QuadroAvisos(this, hostDlg.getHost(), hostDlg.getPort());
         } catch (RemoteException ex) {
             JOptionPane.showMessageDialog(frame, "Failed to export object: " + ex.getMessage());
             return;
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(frame, "I/O Error: " + ex.getMessage());
             return;
         }
 
         receiveMessage("--- Joined group ---");
         groupButton.setText("Sair");
     }
 
     void leaveGroup() {
         if (bboard == null) {
             return;
         }
 
         bboard.closing();
         bboard = null;
 
         receiveMessage("--- Left group ---");
         groupButton.setText("Entrar");
     }
 
     void receiveMessage(String mensagem) {
         messageListModel.insertElementAt(mensagem, 0);
     }
 
     void sendMessage(String message) {
        try {
            bboard.broadcast(message);
        } catch (RemoteException ex) {
            receiveMessage("Failed to send message!");
            receiveMessage(ex.getMessage());
         }
     }
 }
