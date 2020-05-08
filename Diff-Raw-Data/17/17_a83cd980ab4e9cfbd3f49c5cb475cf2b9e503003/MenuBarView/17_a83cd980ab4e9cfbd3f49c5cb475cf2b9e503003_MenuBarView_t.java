 import java.util.ArrayList;
 import java.awt.Color;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.jms.JMSException;
 import javax.naming.NamingException;
 import javax.swing.*;
 import java.io.*;
 
 /**
  * Class which provides a Menu-type view displaying the MultiPoint Tools supplied.  Menus
  * are general hierarchical and display options via text sometimes accompanied
  * by icons.
  *
  *
  * Displays the associated buttons tools within the supplied ToolList.  The
  * ToolList itself contains the various tool controllers that will be associated
  * with this view.
  *
  * For more details, see the documentation of ToolController and ToolList.
  */
 @SuppressWarnings("serial")
 public class MenuBarView extends JMenuBar {
 
     /** The drawing canvas*/
     protected DrawingCanvas canvas;
     /** List of slides in the presentation*/
     protected ArrayList<SlideClass> slideList;
     /** Reference to the associated MultiPoint object*/
     protected MultiPoint mp;
     /** XML handler*/
     public XMLHandler xml;
 
     /**
      * < Constructor >
      *
      * Registers the tools provided in the actions list for display as menu
      * options. This constructor should not be accessed directly, but rather, the
      * factory method provided by MultiPoint should be used.
      *
      * Only ToolControllers that are enabled, i.e. the tool is not null, are added
      * to the ToolBar.
      *
      * @param actions List of tools
      * @param c Associated drawing canvas
      * @param m Associated MultiPoint
      * @return Initialized MenuBarView
      */
     MenuBarView(ToolList actions, DrawingCanvas c, MultiPoint m) {
         canvas = c;
         mp = m;
         xml = m.xmlHandler; //new XMLHandler(m.slideHandler);
         JMenu fileMenu = new JMenu("File");
         JMenu toolMenu = new JMenu("Tools");
         JMenu connectMenu = new JMenu("Connections");
         slideList = m.slideHandler.getSlideList();
 
         ToolListIterator iter = actions.iterator();
         while (iter.hasNext()) {
             Action a = (Action) iter.next();
             if (a.isEnabled()) {
                 toolMenu.add(a);
             }
         }// end tools remain
 
         JMenuItem savePresentation = new JMenuItem("Save");
         savePresentation.addActionListener(new java.awt.event.ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 xml.writeXMLSlideState();
 //                try {
 //                    mp.networkHandler.sendMessage("User","5;Pres;bob;");
 //                    mp.networkHandler.sendMessage("User","2;yelle;");
 //                } catch (JMSException ex) {
 //                    Logger.getLogger(MenuBarView.class.getName()).log(Level.SEVERE, null, ex);
 //                } catch (NamingException ex) {
 //                    Logger.getLogger(MenuBarView.class.getName()).log(Level.SEVERE, null, ex);
 //                }
             }
         });
         JMenuItem loadTemplate = new JMenuItem("Load Presentation");
         loadTemplate.addActionListener(new java.awt.event.ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 // TODO: Make this open a dialog box and have it pic from a .xml file
                 String staticFileName = "presentationProject0.xml";
                 File file = new File("presentationProject0.xml");
                 int i = 1;
                 while(file.exists()){
                     file = new File("presentationProject"+i+".xml");
                     staticFileName = "presentationProject"+i+".xml";
                     i = i+1;
                 }
                 staticFileName = "presentationProject"+(i-2)+".xml";
                 int numSlides = xml.readXMLPresentation(staticFileName);
                mp.slideListPanel.removeAll();
                 for(i = 0; i < numSlides; i++){
                     mp.slideListPanel.add(mp.updateSlideLabel(i,3));
                 }
                 mp.slidePanel.add(mp.slideListPanel);
                 mp.slideHandler.setSelectedSlide(0);
                 canvas.repaint();
             }
         });
 
         JMenuItem clearCanvas = new JMenuItem("Clear Canvas");
         clearCanvas.addActionListener(new java.awt.event.ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 canvas.clearCanvas();
             }
         });
 
         JMenuItem exit = new JMenuItem("Exit");
         exit.addActionListener(new java.awt.event.ActionListener() {
 
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 System.exit(0);
             }
         });
 
 
         fileMenu.add(savePresentation);
         fileMenu.add(loadTemplate);
         fileMenu.addSeparator();
         fileMenu.add(clearCanvas);
         fileMenu.addSeparator();
         fileMenu.add(exit);
 
         add(fileMenu);
         add(toolMenu);
         add(connectMenu);
 
     }// end __ MenuBarView( ToolList )
 }// end public class MenuBarView extends JMenuBar
