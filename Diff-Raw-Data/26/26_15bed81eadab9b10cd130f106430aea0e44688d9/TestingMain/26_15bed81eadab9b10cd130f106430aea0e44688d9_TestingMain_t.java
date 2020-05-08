 package rhythmwheels;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 import javax.script.ScriptException;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import rhythmwheels.scripting.Events;
 import rhythmwheels.scripting.ScriptManager;
 
 /**
  * This class runs the RhythmWheels program as an application.
  * This makes it useful for debugging, as it allows log files to be made.
  * @author Varun Madiath
  */
 public class TestingMain implements ActionListener
 {
 
     private static JFrame topFrame;
     TopContainer root;
     ScriptManager scriptManager;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args)
     {
 //        SwingUtilities.invokeLater(new Runnable()
 //        {
 //
 //            public void run()
 //            {
 //
 //                JFrame topFrame = new JFrame("Rhythm Wheels");
 //                JMenuBar menuBar = new JMenuBar();
 //                JMenu menu = new JMenu("Script");
 //                menuBar.add(menu);
 //
 //                JMenuItem menuItem = new JMenuItem("Load Script");
 //                menu.add(menuItem);
 //
 //                topFrame.setJMenuBar(menuBar);
 //                topFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 //
 //                TopContainer root = new TopContainer();
 //                topFrame.add(root.panes);
 //
 //                menuItem.addActionListener(new ActionListener()
 //                {
 //
 //                    public void actionPerformed(ActionEvent e)
 //                    {
 //                        JFileChooser fc = new JFileChooser();
 //                        int returnVal = fc.showOpenDialog(root);
 //
 //                        if (returnVal == JFileChooser.APPROVE_OPTION)
 //                        {
 //                            File selectedFile = fc.getSelectedFile();
 //                            TopContainer.r
 //                        }
 //                    }
 //                });
 //
 //                topFrame.setSize(1100, 800);
 //                topFrame.setVisible(true);
 //            }
 //        });
 //        SwingUtilities.invokeLater(new Runnable()
 //        {
 //
 //            public void run()
 //            {
 //
 //                JFrame topFrame = new JFrame("Rhythm Wheels");
 //                JMenuBar menuBar = new JMenuBar();
 //                JMenu menu = new JMenu("Script");
 //                menuBar.add(menu);
 //
 //                JMenuItem menuItem = new JMenuItem("Load Script");
 //                menu.add(menuItem);
 //
 //                topFrame.setJMenuBar(menuBar);
 //                topFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 //
 //                TopContainer root = new TopContainer();
 //                topFrame.add(root.panes);
 //
 //                menuItem.addActionListener(new ActionListener()
 //                {
 //
 //                    public void actionPerformed(ActionEvent e)
 //                    {
 //                        JFileChooser fc = new JFileChooser();
 //                        int returnVal = fc.showOpenDialog(root);
 //
 //                        if (returnVal == JFileChooser.APPROVE_OPTION)
 //                        {
 //                            File selectedFile = fc.getSelectedFile();
 //                            TopContainer.r
 //                        }
 //                    }
 //                });
 //
 //                topFrame.setSize(1100, 800);
 //                topFrame.setVisible(true);
 //            }
 //        });
         TestingMain m = new TestingMain();
     }
 
     public TestingMain()
     {
         topFrame = new JFrame("Rhythm Wheels");
         JMenuBar menuBar = new JMenuBar();
         JMenu menu = new JMenu("Script");
         menuBar.add(menu);
 
         JMenuItem menuItem = new JMenuItem("Load Script");
         menu.add(menuItem);
 
         topFrame.setJMenuBar(menuBar);
         topFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         root = new TopContainer();
         topFrame.add(root.panes);
 
         menuItem.addActionListener(this);
 
         topFrame.setSize(1100, 800);
         topFrame.setVisible(true);
         scriptManager = new ScriptManager(root.rw);
     }
 
     public static JFrame getAppFrame()
     {
         return topFrame;
     }
 
     public void actionPerformed(ActionEvent e)
     {
         JFileChooser fc = new JFileChooser();
         int returnVal = fc.showOpenDialog(root.rw);
 
         if (returnVal == JFileChooser.APPROVE_OPTION)
         {
             File selectedFile = fc.getSelectedFile();
             try
             {
                 scriptManager.fireEvent(Events.ON_UNLOAD);
 //                scriptManager.loadScript(selectedFile);
 
                 ZipFile s;
                 s = new ZipFile(selectedFile);
                root.numPages = 0;
                 for (Enumeration<ZipEntry> x = (Enumeration<ZipEntry>) s.entries(); x.hasMoreElements();)
                 {
                     ZipEntry z = x.nextElement();
                     if (z.getName().endsWith("html"))
                     {
                         root.numPages++;
                     }
                     if (z.getName().endsWith("js"))
                     {
                         scriptManager.loadScript(
                                 new BufferedReader(new InputStreamReader(s.getInputStream(z))));
                     }
                 }
                 
                 ZipEntry page1 = s.getEntry("page1.html");
                 root.htmlContent.read(s.getInputStream(page1), page1);
                 scriptManager.fireEvent(Events.ON_LOAD);
                 root.bundle = s;
                root.currentPageNumber = 1;
                 root.setUpButtons();
             }
             catch (ZipException ex)
             {
                 Logger.getLogger(TestingMain.class.getName()).log(Level.SEVERE, null, ex);
             }
             catch (IOException ex)
             {
                 Logger.getLogger(TestingMain.class.getName()).log(Level.SEVERE, null, ex);
             }
             catch (ScriptException ex)
             {
                 Logger.getLogger(TestingMain.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
