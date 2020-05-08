 package com.googlecode.whatswrong;
 
 import javax.swing.*;
 import java.awt.*;
 import static java.awt.GridBagConstraints.EAST;
 import static java.awt.GridBagConstraints.NONE;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.*;
 import java.util.Properties;
 
 /**
  * The WhatsWrongWithMyNLP class serves as main application class. It sets up the canvas and all controllers and
  * configures the position of the corresponding dialogs. It also loads all stored parameters from the "~/.whatswrong"
  * file.
  *
  * @author Sebastian Riedel
  */
 public class WhatsWrongWithMyNLP extends JPanel {
 
     private NLPCanvas nlpCanvas = new NLPCanvas();
     private JScrollPane nlpScrollPane;
    public final static String VERSION = "0.2.2";
     private final static Properties properties = new Properties();
     private static final boolean USE_SYSTEM_UI = true;
 
     public static Properties getProperties() {
         return properties;
     }
 
     /**
      * Sets some ui properties and loads oroperties from .whatswrong
      */
     static {
         System.setProperty("apple.laf.useScreenMenuBar", "true");
 
         try {
             File file = new File(System.getProperty("user.home") + "/.whatswrong");
             if (file.exists()) {
                 properties.load(new FileInputStream(file));
             } else {
                 properties.setProperty("whatswrong.golddir", System.getProperty("user.dir"));
                 properties.setProperty("whatswrong.guessdir", System.getProperty("user.dir"));
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Create a new WhatsWrongWithMyNLP application.
      */
     public WhatsWrongWithMyNLP() {
         super(new BorderLayout());
 
         nlpScrollPane = new JScrollPane(nlpCanvas);
         nlpScrollPane.setPreferredSize(new Dimension(300, 200));
 
         add(nlpScrollPane, BorderLayout.CENTER);
 
 
         setMinimumSize(new Dimension(700, 600));
     }
 
     public NLPCanvas getNlpCanvas() {
         return nlpCanvas;
     }
 
     public void scrollToBottom() {
         nlpCanvas.scrollRectToVisible(new Rectangle(
             nlpScrollPane.getViewport().getX(),
             nlpCanvas.getHeight() - nlpScrollPane.getViewport().getHeight(),
             nlpScrollPane.getViewport().getWidth(),
             nlpScrollPane.getViewport().getHeight()));
     }
 
     private static class WindowMenuItem extends JCheckBoxMenuItem {
         public WindowMenuItem(final JWindow window, String text) {
             super(text, window.isVisible());
             addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     window.setVisible(!window.isVisible());
                 }
             });
         }
 
 
         public WindowMenuItem(final Dialog window) {
             this(window, window.getTitle());
         }
 
         public WindowMenuItem(final Window window, String title) {
             super(title, window.isVisible());
             addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     //window.pack();
                     window.setVisible(!window.isVisible());
                 }
             });
 
             window.addWindowListener(new WindowAdapter() {
 
                 public void windowClosing(WindowEvent e) {
                     WindowMenuItem.this.setSelected(false);
                 }
 
 
                 public void windowOpened(WindowEvent e) {
                     WindowMenuItem.this.setSelected(USE_SYSTEM_UI);
                 }
             });
         }
     }
 
     private static class ControllerDialog extends JDialog {
 
 
         public ControllerDialog(Frame owner, String title, boolean resizable) throws HeadlessException {
             super(owner, title, false);
             pack();
             setResizable(resizable);
         }
 
         public ControllerDialog(String title, boolean resizable) {
             this(null, title, resizable);
         }
 
     }
 
     private static void changeUI(boolean system) {
         try {
             // Set System L&F
 //      UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
             UIManager.setLookAndFeel(system ?
                 UIManager.getSystemLookAndFeelClassName() :
                 UIManager.getCrossPlatformLookAndFeelClassName());
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 
     public static void main(String[] args) {
         System.setProperty("com.apple.mrj.application.apple.menu.about.name", "What's Wrong ...");
         changeUI(USE_SYSTEM_UI);
 
         final NLPCanvas canvas = new NLPCanvas();
 
         //create the filter pipeline
         EdgeTokenFilter edgeTokenFilter = new EdgeTokenFilter();
         EdgeLabelFilter edgeLabelFilter = new EdgeLabelFilter();
         TokenFilter tokenFilter = new TokenFilter();
         EdgeTypeFilter edgeTypeFilter = new EdgeTypeFilter();
         FilterPipeline filterPipeline = new FilterPipeline(
             tokenFilter, edgeTypeFilter, edgeLabelFilter, edgeTokenFilter);
 
         //set filter of canvas to be the pipeline
         canvas.setFilter(filterPipeline);
 
         int canvasWidth = 900;
         int canvasHeight = 300;
         int canvasX = 50;
         int canvasY = 50;
         int canvasBottom = canvasHeight + canvasY;
 
         final CorpusLoader gold = new CorpusLoader("Select Gold");
         final CorpusLoader guess = new CorpusLoader("Select Guess");
         gold.loadProperties(properties);
         guess.loadProperties(properties);
 
         //Menu
         JMenuBar menuBar = new JMenuBar();
         JMenu file = new JMenu("File");
         JMenuItem exportEps = new JMenuItem("Export EPS");
         final JFileChooser fc = new JFileChooser();
         exportEps.setAccelerator(KeyStroke.getKeyStroke('E', java.awt.event.InputEvent.ALT_MASK));
         exportEps.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 int returnVal = fc.showSaveDialog(canvas);
                 if (returnVal == JFileChooser.APPROVE_OPTION)
                     try {
                         canvas.exportToEPS(fc.getSelectedFile());
                     } catch (IOException e1) {
                         e1.printStackTrace();
                     }
 
             }
         });
         file.add(exportEps);
         file.setMnemonic('F');
 
         JMenuItem quit = new JMenuItem("Quit");
         //quit.setMnemonic('Q');
         file.add(quit);
         quit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 System.exit(0);
             }
         });
 
 
         JMenu window = new JMenu("Window");
 
         menuBar.add(file);
         menuBar.add(window);
 
         //Toolbar
         JToolBar toolBar = new JToolBar("Blub");
         toolBar.add(new JButton("Test"));
 
         //dummy Frame
         //JFrame dummy = new JFrame();
         //dummy.setVisible(false);
 
         //canvas frame
         JFrame canvasFrame = new JFrame("What's Wrong With My NLP?");
         canvasFrame.setSize(canvasWidth, canvasHeight);
         canvasFrame.getContentPane().setLayout(new BorderLayout());
         canvasFrame.getContentPane().add(new JScrollPane(canvas), BorderLayout.CENTER);
         canvasFrame.setJMenuBar(menuBar);
         //canvasFrame.getContentPane().add(toolBar, BorderLayout.NORTH);
         canvasFrame.setLocation(canvasX, canvasY);
         canvasFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         //window.add(new WindowMenuItem(canvasFrame,"Canvas"));
         //desktop.add(canvasFrame);
 
         //file selection frame
         final ControllerDialog fileWindow = new ControllerDialog("File Selection", USE_SYSTEM_UI);
         fileWindow.getContentPane().setLayout(new BoxLayout(fileWindow.getContentPane(), BoxLayout.Y_AXIS));
         fileWindow.getContentPane().add(gold);
         fileWindow.getContentPane().add(new JSeparator());
         fileWindow.getContentPane().add(guess);
         fileWindow.setLocation(canvasX + 20, canvasBottom + 20);
         fileWindow.pack();
         fileWindow.setVisible(USE_SYSTEM_UI);
         //fileWindow.toBack();
         window.add(new WindowMenuItem(fileWindow));
         //fileFrame.setResizable(false);
         //desktop.add(fileFrame);
 
         //filter frame
         ControllerDialog filterWindow = new ControllerDialog("Edge Filters", false);
         filterWindow.getContentPane().setLayout(new BoxLayout(filterWindow.getContentPane(), BoxLayout.Y_AXIS));
         filterWindow.getContentPane().add(new EdgeTypeFilterPanel(canvas, edgeTypeFilter));
         filterWindow.getContentPane().add(new JSeparator());
         filterWindow.getContentPane().add(new DependencyFilterPanel(canvas, edgeLabelFilter, edgeTokenFilter));
         filterWindow.pack();
         filterWindow.setLocation(canvasX + 250, canvasBottom + 15);
         filterWindow.setVisible(USE_SYSTEM_UI);
         window.add(new WindowMenuItem(filterWindow));
 
         //token filter frame
         ControllerDialog tokenFilterWindow = new ControllerDialog("Token Filters", false);
         tokenFilterWindow.getContentPane().setLayout(new BoxLayout(tokenFilterWindow.getContentPane(), BoxLayout.Y_AXIS));
         tokenFilterWindow.getContentPane().add(new TokenFilterPanel(canvas, tokenFilter));
         tokenFilterWindow.pack();
         tokenFilterWindow.setLocation(canvasX + 360, canvasBottom + 230);
         tokenFilterWindow.setVisible(USE_SYSTEM_UI);
         window.add(new WindowMenuItem(tokenFilterWindow));
 
         //appearance
         ControllerDialog appearance = new ControllerDialog("Appearance", false);
         appearance.getContentPane().setLayout(new BoxLayout(appearance.getContentPane(), BoxLayout.Y_AXIS));
         appearance.getContentPane().add(new AppearancePanel(canvas));
         appearance.pack();
         appearance.setLocation(canvasX + 500, canvasBottom + 25);
         appearance.setVisible(USE_SYSTEM_UI);
         window.add(new WindowMenuItem(appearance));
 
         //navigator
         ControllerDialog navigatorWindow = new ControllerDialog("Search Corpus", USE_SYSTEM_UI);
         navigatorWindow.getContentPane().setLayout(new BoxLayout(navigatorWindow.getContentPane(), BoxLayout.Y_AXIS));
         CorpusNavigator navigator = new CorpusNavigator(canvas, gold, guess, edgeTypeFilter);
         navigatorWindow.getContentPane().add(navigator);
         navigatorWindow.pack();
         navigatorWindow.setMinimumSize(navigatorWindow.getSize());
         navigatorWindow.setLocation(canvasX + 800, canvasBottom + 20);
         navigatorWindow.setVisible(USE_SYSTEM_UI);
         window.add(new WindowMenuItem(navigatorWindow, "Navigator"));
 
         //statusbar
         JPanel statusBar = new JPanel();
         JLabel status = new JLabel("What's Wrong With My NLP version " + VERSION);
         status.setForeground(Color.LIGHT_GRAY);
         statusBar.setLayout(new GridBagLayout());
         statusBar.setBorder(BorderFactory.createEmptyBorder(1, 10, 1, 10));
         statusBar.add(status);
         statusBar.add(navigator.getSpinnerPanel(), new SimpleGridBagConstraints(0, USE_SYSTEM_UI));
         statusBar.add(navigator.getSpinnerPanel(), new SimpleGridBagConstraints(1, 0, 1.0, 0.0, EAST, NONE));
 
         //final preparation of canvas
         canvasFrame.getContentPane().add(statusBar, BorderLayout.SOUTH);
         canvasFrame.setVisible(USE_SYSTEM_UI);
         canvasFrame.requestFocus();
         //canvasFrame.requestFocusInWindow();
 
         Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
             public void run() {
                 gold.saveProperties(properties);
                 guess.saveProperties(properties);
                 try {
                     properties.store(new FileOutputStream(System.getProperty("user.home") + "/.whatswrong"),
                         "Whats wrong with you NLP properties");
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }));
 
 
     }
 
 }
