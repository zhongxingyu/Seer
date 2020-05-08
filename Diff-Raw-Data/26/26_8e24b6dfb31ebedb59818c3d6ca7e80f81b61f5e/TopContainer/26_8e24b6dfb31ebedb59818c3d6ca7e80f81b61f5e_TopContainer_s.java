 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package rhythmwheels;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.plaf.basic.BasicCheckBoxMenuItemUI;
 
 /**
  *
  * @author Varun Madiath <vamega@gmail.com>
  */
 public class TopContainer extends JPanel
 {
 
     JSplitPane panes;
     public RhythmWheel rw;
     public JEditorPane htmlContent;
     public ZipFile bundle;
     public int currentPageNumber = 1;
     public int numPages = 0;
     
     private JButton previous, next;
 
     public TopContainer()
     {
         this(null);
     }
 
     public TopContainer(URL docbase)
     {
         panes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
         rw = new RhythmWheel(docbase);
         panes.setRightComponent(rw);
 
         htmlContent = new JEditorPane();
         htmlContent.setContentType("text/html");
         try
         {
             htmlContent.setPage(new File("test.html").toURI().toURL());
         }
         catch (MalformedURLException ex)
         {
             Logger.getLogger(TopContainer.class.getName()).log(Level.SEVERE, null, ex);
         }
         catch (IOException ex)
         {
             Logger.getLogger(TopContainer.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         JScrollPane htmlScrollPane = new JScrollPane(htmlContent);
 
         JPanel leftPanel = new JPanel();
         BoxLayout leftLayout = new BoxLayout(leftPanel, BoxLayout.Y_AXIS);
         leftPanel.setLayout(leftLayout);
 
         JPanel navigationPanel = new JPanel();
         previous = new JButton();
         next = new JButton();

         previous.setToolTipText("Previous View");
         previous.setText("Previous");
         previous.addActionListener(new ActionListener()
         {
 
             public void actionPerformed(ActionEvent e)
             {
                 ZipEntry page = bundle.getEntry(String.format("page%s.html", --currentPageNumber));
                 System.out.println("Page #: " + currentPageNumber);
                 try
                 {
                     htmlContent.read(bundle.getInputStream(page), page);
                 }
                 catch (IOException ex)
                 {
                     Logger.getLogger(TopContainer.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
                 if (currentPageNumber == 1)
                 {
                     ((JButton) e.getSource()).setEnabled(false);
                }
                 next.setEnabled(true);
             }
         });
         navigationPanel.add(previous);
         
         next.setToolTipText("Next View");
         next.setText("Next");
         next.addActionListener(new ActionListener()
         {
 
             public void actionPerformed(ActionEvent e)
             {
                 ZipEntry page = bundle.getEntry(String.format("page%s.html", ++currentPageNumber));
                 try
                 {
                     htmlContent.read(bundle.getInputStream(page), page);
                 }
                 catch (IOException ex)
                 {
                     Logger.getLogger(TopContainer.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
                 if (currentPageNumber == numPages)
                 {
                     ((JButton) e.getSource()).setEnabled(false);
                 }
                 previous.setEnabled(true);
             }
         });
        navigationPanel.add(next);
         
         setUpButtons();
 
         navigationPanel.setMinimumSize(new Dimension(100,200));
         navigationPanel.setSize(new Dimension(100,200));
         
         leftPanel.add(htmlScrollPane);
         leftPanel.add(navigationPanel);
         
         panes.setLeftComponent(leftPanel);
         htmlContent.setPreferredSize(new Dimension(300, 1000));
 
     }
 
     public void setUpButtons()
     {
        if(numPages == 1)
         {
             previous.setEnabled(false);
             next.setEnabled(false);
         }
         
         else if(currentPageNumber == 1)
         {
             previous.setEnabled(false);
         }
     }
 }
