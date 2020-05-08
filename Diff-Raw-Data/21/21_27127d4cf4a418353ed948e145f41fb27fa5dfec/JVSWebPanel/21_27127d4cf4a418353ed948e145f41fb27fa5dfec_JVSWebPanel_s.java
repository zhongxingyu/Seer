 /**************************************************************
  * Philippe VIENNE, Copyright (C) 2011.  All rights reserved. *
  **************************************************************/
 package org.javascool.gui;
 
 import java.awt.BorderLayout;
import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JPanel;
 import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
 import org.lobobrowser.html.test.SimpleHtmlRendererContext;
 import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
 
 /** Make a web navigation Panel
  * Open url, website in a JPanel
  * @author Philippe VIENNE
 * @todo Write this class
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
            .navigate("http://www.javascool.fr");
        } catch (MalformedURLException ex) {
             Logger.getLogger(JVSWebPanel.class.getName()).log(Level.SEVERE, null, ex);
         }
         this.add(webPanel,BorderLayout.CENTER);
     }
     
 }
