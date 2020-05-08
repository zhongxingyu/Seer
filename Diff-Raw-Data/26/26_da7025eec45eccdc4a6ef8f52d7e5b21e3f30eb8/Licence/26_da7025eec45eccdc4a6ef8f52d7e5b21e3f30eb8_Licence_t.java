 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.mgn.collabdesktop.menu.frames.about;
 
 import cz.mgn.collabdesktop.menu.MenuFrame;
 import cz.mgn.collabdesktop.utils.Utils;
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JEditorPane;
 import javax.swing.JScrollPane;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 
 /**
  *
  * @author indy
  */
 public class Licence extends MenuFrame {
 
     public Licence() {
         super();
     }
 
     @Override
     protected String getSectionName() {
         return "licence";
     }
 
     @Override
     protected void initComponents() {
        setMinimumSize(new Dimension(400, 400));
         setPreferredSize(new Dimension(400, 450));
         setSize(getPreferredSize());
         setLayout(new BorderLayout(0, 10));
 
         try {
             JEditorPane licence = new JEditorPane(Licence.class.getResource("/resources/other/gplv3.html"));
             licence.addHyperlinkListener(new HyperlinkListener() {
 
                 @Override
                 public void hyperlinkUpdate(HyperlinkEvent e) {
                     if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                         Utils.browse(e.getURL().toString());
                     }
                 }
             });
             licence.setEditable(false);
             JScrollPane licenceScrollPane = new JScrollPane(licence);
             add(licenceScrollPane, BorderLayout.CENTER);
         } catch (IOException ex) {
             Logger.getLogger(Licence.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
 }
