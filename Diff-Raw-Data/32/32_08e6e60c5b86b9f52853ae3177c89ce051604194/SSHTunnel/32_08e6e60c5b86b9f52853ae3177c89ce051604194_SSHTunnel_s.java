 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.tunnel22;
 
 import com.jcraft.jsch.*;
 import java.awt.*;
 import javax.swing.*;
 
 /**
  *
  * @author Hugo Ritter Vazquez
  */
 public class SSHTunnel {
     private Session session;
     public Session getSession(){
         return session;
     }
 
     public boolean openL(String uname, String rhost, int lport, int rport) {
         try{
           JSch jsch=new JSch();
           session=jsch.getSession(uname, rhost, 22);
 
           //String foo=JOptionPane.showInputDialog("Enter -L port:host:hostport",
           //                                       "port:host:hostport");
           //lport=Integer.parseInt(foo.substring(0, foo.indexOf(':')));
           //foo=foo.substring(foo.indexOf(':')+1);
           //rhost=foo.substring(0, foo.indexOf(':'));
           //rport=Integer.parseInt(foo.substring(foo.indexOf(':')+1));
 
           // username and password will be given via UserInfo interface.
           UserInfo ui=new MyUserInfo();
           session.setUserInfo(ui);
           session.connect();
 
           //Channel channel=session.openChannel("shell");
           //channel.connect();
 
           int assinged_port=session.setPortForwardingL(lport, "127.0.0.1", rport);
           System.out.println("localhost:"+assinged_port+" -> "+rhost+":"+rport);
         }
         catch(Exception e){
           System.out.println(e);
           return false;
         }
         return true;
     }
 }
 
 class MyUserInfo implements UserInfo, UIKeyboardInteractive{
     private String passwd;
 
     //public MyUserInfo(String passwd){
     //    this.passwd=passwd;
     //}
     
     public String getPassword(){ return passwd; }
 
     public boolean promptYesNo(String str){
       Object[] options={ "yes", "no" };
       int foo=JOptionPane.showOptionDialog(null,
              str,
              "Warning",
              JOptionPane.DEFAULT_OPTION,
              JOptionPane.WARNING_MESSAGE,
              null, options, options[0]);
        return foo==0;
     }
 
     public String getPassphrase(){ return null; }
     public boolean promptPassphrase(String message){ return true; }
     public boolean promptPassword(String message){ return true; }
     public void showMessage(String message){
       JOptionPane.showMessageDialog(null, message);
     }
     final GridBagConstraints gbc =
       new GridBagConstraints(0,0,1,1,1,1,
                              GridBagConstraints.NORTHWEST,
                              GridBagConstraints.NONE,
                              new Insets(0,0,0,0),0,0);
     private Container panel;
     public String[] promptKeyboardInteractive(String destination,
                                               String name,
                                               String instruction,
                                               String[] prompt,
                                               boolean[] echo){
       panel = new JPanel();
       panel.setLayout(new GridBagLayout());
 
       gbc.weightx = 1.0;
       gbc.gridwidth = GridBagConstraints.REMAINDER;
       gbc.gridx = 0;
       panel.add(new JLabel(instruction), gbc);
       gbc.gridy++;
 
       gbc.gridwidth = GridBagConstraints.RELATIVE;
 
       JTextField[] texts=new JTextField[prompt.length];
       for(int i=0; i<prompt.length; i++){
         gbc.fill = GridBagConstraints.NONE;
         gbc.gridx = 0;
         gbc.weightx = 1;
         panel.add(new JLabel(prompt[i]),gbc);
 
         gbc.gridx = 1;
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.weighty = 1;
         if(echo[i]){
           texts[i]=new JTextField(20);
         }
         else{
           texts[i]=new JPasswordField(20);
         }
         panel.add(texts[i], gbc);
         gbc.gridy++;
       }
 
       if(JOptionPane.showConfirmDialog(null, panel,
                                        destination+": "+name,
                                        JOptionPane.OK_CANCEL_OPTION,
                                        JOptionPane.QUESTION_MESSAGE)
          ==JOptionPane.OK_OPTION){
         String[] response=new String[prompt.length];
         for(int i=0; i<prompt.length; i++){
           response[i]=texts[i].getText();
         }
 	return response;
       }
       else{
         return null;  // cancel
       }
    }
 
 
 }
