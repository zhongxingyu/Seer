 package project.senior.app.pormmo;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.ScrollPane;
 import java.awt.Toolkit;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Scanner;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import uk.co.caprica.vlcj.player.MediaPlayer;
 
 /**
  * @author John Fisher
  */
 public class UserInterfaceFrame extends JFrame
 {
 
   public float TOLERANCE;
   private ScrollPane tPane;
   protected HSBPanel hsbPanel;
   protected OutputPanel outputPanel;
   private File selectedInputFile = null;
   private PlayerControlPanel pcp;
   private ScrollPane sPane;
   private JFrame me;
   protected BufferedImage snapshot, displayShot;
   private String fileDirectory;
   protected MediaPlayer mPlayer;
   protected boolean userSelectingLocation = false;
   private GSR gSR;
   private BufferedWrapper BW;
   private Dimension screenSize;
   protected ImageContainer ic;
 
   public UserInterfaceFrame()
   {
     TOLERANCE = 0.5f;
     initFrame();
     initMenu();
     initControlPanel();
     initOutputPanel();
     initDisplay();
     showFrame();
   }
 
   private void initFrame()
   {
     screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
     setTitle("PorMMo");
     setPreferredSize(
             new Dimension((int) screenSize.getWidth() / 2
             , (int) screenSize.getHeight() - 50));
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setLayout(new BorderLayout());
     this.addComponentListener(new FrameListener());
     me = this;
     gSR = new GSR();
   }
 
   private void showFrame()
   {
     pack();
     setVisible(true);
   }
 
   private void initDisplay()
   {
     JPanel[] tools = new JPanel[4];
     hsbPanel = new HSBPanel(this);
    hsbPanel.setPreferredSize(new Dimension(100, 150));
     tools[0] = hsbPanel;
 
     TolerancePanel tp = new TolerancePanel(outputPanel);
     tp.setMinimumSize(new Dimension(100, 100));
     tools[1] = tp;
 
     FilterPanel fp = new FilterPanel(this);
     fp.setMinimumSize(new Dimension(100, 200));
     tools[2] = fp;
 
     StartEndPanel sep = new StartEndPanel(mPlayer);
     sep.setMinimumSize(new Dimension(100, 200));
     tools[3] = sep;
 
//    SequenceGrabber sg = new SequenceGrabber(this);
//    tools[4] = sg;
    
     tPane = new ToolsPanel(tools);
 
     sPane = new ScrollPane();
     sPane.add(outputPanel);
     sPane.setPreferredSize(this.getPreferredSize());
 
     add(pcp, BorderLayout.NORTH);
     add(sPane, BorderLayout.CENTER);
     add(tPane, BorderLayout.WEST);
     fileDirectory = "";    
   }
 
   private void initMenu()
   {
     AppMenuBar amb = new AppMenuBar(this);
     UserInterfaceFrameMenuListener listener =
             new UserInterfaceFrameMenuListener();
 
     JMenu jMenu = amb.AddMenuItem("File", listener);
     amb.AddSubMenuItem("Open", jMenu, listener);
     amb.AddSubMenuItem("Exit", jMenu, listener);
   }
 
   private void initOutputPanel()
   {
     outputPanel = new OutputPanel(this);
     outputPanel.setPreferredSize(new Dimension(500, 500));
   }
 
   private void initControlPanel()
   {
     ic = new ImageContainer();
     pcp = new PlayerControlPanel(this, mPlayer);
 
     pcp.AddJButton("Play", 0, 0, 1);
     pcp.AddJButton("Stop", 1, 0, 1);
     pcp.AddJButton("Forward", 2, 0, 1);
     pcp.AddJButton("Rewind", 3, 0, 1);
     pcp.AddJButton("Snapshot", 4, 0, 1);
     pcp.AddJButton("Save", 5, 0, 1);
     pcp.AddJButton("Reset Image", 6, 0, 1);
     pcp.AddJSlider("PlayPosition", 0, 1, 7);
   }
 
   /**
    * Check and do settings. 0 = Check for the Settings Directory.
    *
    * 1 = Check for FileSelect directories and last location. ##~If there is no
    * last one, it will create the file.
    *
    * 2 = Save latest location for FileSelect
    *
    * @param setting The setting you wish to check/do.
    */
   private void doSettings(int setting)
   {
     switch (setting)
     {
       case 0:
         File SettingsDirectory = new File("Settings/");
         if (!SettingsDirectory.exists())
         {
           SettingsDirectory.mkdir();
         }
         break;
       //------------------------------------------------------------------------
       case 1:
         File SavedDirectory = new File("Settings/SD.pmo");
         if (SavedDirectory.exists())
         {
           try
           {
             Scanner scan = new Scanner(SavedDirectory);
             fileDirectory = scan.nextLine();
             scan.close();
           }
           catch (Exception e)
           {
             //#DEBUG
             System.out.println("Insufficient Access to file! setting = 1");
           }
         }//::End if(SavedDirectory)
         break;
       //------------------------------------------------------------------------
       case 2:
         try
         {
           BufferedWriter bw =
                   new BufferedWriter(new FileWriter("Settings/SD.pmo"));
           bw.write(fileDirectory);
           bw.close();
         }
         catch (Exception e)
         {
           //#DEBUG
           System.out.println("Can't write to file! setting = 2");
         }
         break;
     }
   }
 
   private void showFileSelect()
   {
     doSettings(0);
     doSettings(1);
 
     JFileChooser jFC = new JFileChooser();
     jFC.setSelectedFile(new File(fileDirectory));
     jFC.setAcceptAllFileFilterUsed(false);
     jFC.setFileFilter(new ExtFileFilter(new String[]
             {
               ".png", ".jpg"
             }));
     jFC.setFileFilter(new ExtFileFilter(new String[]
             {
               ".mp4", ".mpg", ".avi"
             }));
 
     jFC.showDialog(this, "Open");
     selectedInputFile = jFC.getSelectedFile();
     if (selectedInputFile != null)
     {
       fileDirectory = jFC.getSelectedFile().getPath();
       doSettings(2);
       OpenSource();
     }
   }
 
   private void OpenSource()
   {
     if (selectedInputFile.canRead())
     {
       try
       {
         pcp.SetInputFile(selectedInputFile);
       }
       catch (java.lang.NoClassDefFoundError e)
       {
         System.out.println("Error: " + e.getMessage());
       }
     }
   }
 
 //  public void processSnapshot(BufferedImage givenSnapshot)
 //  {
 //    if (givenSnapshot != null)
 //    {
 //      outputPanel.DrawBufferedImage(givenSnapshot);
 //      outputPanel.setPreferredSize(new Dimension(me.getWidth(), me.getHeight() - pcp.getHeight()));
 //      hsbPanel.setImage(givenSnapshot);
 //    }
 //  }
 
   private class UserInterfaceFrameMenuListener extends MouseAdapter
   {
 
     @Override
     public void mouseReleased(MouseEvent e)
     {
 
       switch (((JMenuItem) e.getSource()).getText().toLowerCase())
       {
         case "open":
           showFileSelect();
           break;
         case "exit":
           System.exit(0);
           break;
       }
     }
   }
 
   private class FrameListener implements ComponentListener
   {
 
     @Override
     public void componentResized(ComponentEvent e)
     {
       pcp.setPreferredSize(new Dimension(me.getWidth(), pcp.getHeight()));
       outputPanel.setPreferredSize(new Dimension(me.getWidth(), outputPanel.getHeight() - pcp.getHeight()));
       me.validate();
       me.repaint();
     }
 
     @Override
     public void componentMoved(ComponentEvent e)
     {
     }
 
     @Override
     public void componentShown(ComponentEvent e)
     {
     }
 
     @Override
     public void componentHidden(ComponentEvent e)
     {
     }
   }
 }
