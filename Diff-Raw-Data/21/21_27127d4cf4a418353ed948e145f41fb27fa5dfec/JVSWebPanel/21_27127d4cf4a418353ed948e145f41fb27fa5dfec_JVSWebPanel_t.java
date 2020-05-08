 /**************************************************************
  * Philippe VIENNE, Copyright (C) 2011.  All rights reserved. *
  **************************************************************/
 package org.javascool.gui;
 
 import java.awt.BorderLayout;
 import java.net.MalformedURLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JPanel;
 import org.lobobrowser.html.gui.HtmlPanel;
 import org.lobobrowser.html.test.SimpleHtmlRendererContext;
 import org.lobobrowser.html.test.SimpleUserAgentContext;
 
 /** Make a web navigation Panel
  * Open url, website in a JPanel
  * @author Philippe VIENNE
  */
 public class JVSWebPanel extends JPanel{
     
     private HtmlPanel webPanel;
     
     public JVSWebPanel(){
         this.setupPanel();
     }
     
     private void setupPanel(){
         this.setLayout(new BorderLayout());
         this.setupWebPanel();
         this.setVisible(true);
     }
     
     private void setupWebPanel(){
         webPanel=new HtmlPanel();
         try {
             new SimpleHtmlRendererContext(webPanel, new SimpleUserAgentContext())
            .prompt("hello", "");
        } catch (Exception ex) {
             Logger.getLogger(JVSWebPanel.class.getName()).log(Level.SEVERE, null, ex);
         }
         this.add(webPanel,BorderLayout.CENTER);
     }
     
 }
