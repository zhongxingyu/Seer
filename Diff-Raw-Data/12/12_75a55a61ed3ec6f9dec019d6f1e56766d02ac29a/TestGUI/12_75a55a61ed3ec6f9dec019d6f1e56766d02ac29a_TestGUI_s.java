 package com.novel.stdobj.gui;
 
 import com.novel.odisp.common.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 /** ,   GUI-,     (?)
 *  ( ).
* @author Andrew A. Porohin
 * @author  . 
 * @author (C) 2003  "-"
* @version $Id: TestGUI.java,v 1.3 2003/10/14 09:53:23 valeks Exp $
 */
 public class TestGUI extends CallbackODObject {
    private JTextField txt1 = new JTextField(20);
    private JFrame mainFrame;
    public class ButtonOkListener implements ActionListener {
       public void actionPerformed(ActionEvent e) {
          String name = ((JButton)e.getSource()).getText();
          txt1.setText(name);
       }
    }
    /**  ,   .
    */
    protected void registerHandlers() {
      addHandler("od_cleanup",
                 new MessageHandler() {
                   public void messageReceived(Message msg) {
                     txt1.setText("Ops... Shutdown!");
                     log("messageReceived", "");
                     cleanUp(((Integer)msg.getField(0)).intValue());
                   }
                 });
    }
 
    /** 
    */
    public int cleanUp(int type) {
       mainFrame.hide();
      // wait(1000);
       mainFrame.dispose();
       return 0;
    }
 
    /** 
    */
    public TestGUI(Integer id) {
      super("testGUI"+id);
      JButton buttonOk; /*    */
      ButtonOkListener bol = new ButtonOkListener();
 
      buttonOk = new JButton("Ok :-)");
      buttonOk.addActionListener(bol);
 
      mainFrame = new JFrame("TestGUI::mainFrame");
      // temporarily...
      mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      Container cn = mainFrame.getContentPane();
      cn.setLayout(new FlowLayout());
      cn.add(buttonOk);
      cn.add(txt1);
      mainFrame.setSize(400, 400);
      mainFrame.setVisible(true);
    }
    public String[] getProviding(){
 	String[] res = {"testGUI"};
 	return res;
    }
    public String[] getDepends(){
 	String[] res = {"stddispatcher"};
 	return res;
    }
 }
