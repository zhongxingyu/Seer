 package gui;
 
 import controller.IntStringMap;
 import controller.LevelHandler;
 import controller.SqlCommunicator;
 import controller.TUGInformations;
 import static controller.TUGInformations.*;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.HashMap;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import javax.swing.JLabel;
 
 import javax.swing.JFrame;
 
 
 /*
  * JFrame
  *
  * Changed on 24.09. 2005, 15:53
  */
 /**
  * The Top level class
  *
  * @author user
  */
 public class TUGLabyrinth extends JFrame {
 
     private JFrame mainframe_ = this; //?????
     private static final int height = 580;
     private static final int width = 950;
     private RuleTreePanel rule_tree_panel_;
     private JScrollPane rule_tree_scrollpane_;
     private MenuePanel menue_panel_;
     private MenueExercisePanel menue_exercise_panel_;
     private MenueExamPanel menue_exam_panel_;
     private HashMap brick_images_;
     private StatusPanel status_message_;
     private AktiveRulePanel aktive_rule_panel_;
     private JButton level_button_;
     private SpeedPanel speed_panel_;
     private StatisticsPanel statistics_panel_;
     private PrintLabyrinth print_labyrinth_;
 
     private static BrickImages images_;
     private static LevelHandler level_handler_;
     private IntStringMap mark_array_, state_array_;
     private String typ_, cookie_, file_name_, version_;
 
     private ControllerGuiCommunicator controller_gui_communicator_;
     private ControlSimulation control_simulation_;
     private ControlLevel control_level_;
     /**
      * ADDED FROM PrintGame
      * *********************************************************
      */
     // derzeit nï¿œig
     //number of cells in a row and a col
     private final static int ROWS = 20;
     private final static int COLS = 20;
 
     public TUGLabyrinth() {
         try {
             // RUPTHO: TODO
             // very very VERY quick hack
             // should maybe? replace it with classLoader.getResource()
             // Not done yet because we have to research, 
             // how the final folder structure for the jnlp file has to look like
 
             // this is the source folder where the TUGLabyrinth class is in
             URL url = TUGLabyrinth.class.getResource("/resources/");
 
             setDocumentBase(url);
             setCodeBase(url);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         // RUPHTO: will close the window and terminate ALL threads
         addWindowListener(new WindowAdapter() {
             @Override
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             }
         });
 
         init();
         setLocationRelativeTo(null);
         setResizable(false);
        setSize(950, 600);
         setVisible(true);
     }
 
     /**
      * Initializes the applet JApplet Draws the applet and some of its buttons.
      * Adds action listeners. One of the object trees which may help you to
      * understand the source is:
      * Applet->LevelHandler->LoadLabyrinth->LabyrinthField
      */
     public final void init() {
         // BEGIN OF WINDOW SETUP ****************************************************************
       /*  
          RUPTHO: I dont think this is really useful or needed..........
         
          SqlCommunicator.add_log("Starting Applet", 1);
          int log_level = 0;
          if (getParameter("logging") != null) {
          try {
          log_level = Integer.parseInt(getParameter("logging"));
          } catch (NumberFormatException ex) {
          System.err.println(" Please insert int values for parameter logging");
          }
          if (log_level > 0) {
          SqlCommunicator.activate(true);
          System.out.println("logging with level " + log_level);
          } else {
          System.out.println("logging == false");
          }
          } else {
          System.out.println("Parameter logging nicht angegeben -> setze logging: false");
          }
 
          String version = "unknown version";
          if (getParameter("version") != null) {
          version = getParameter("version");
          }
 
          System.out.println("Version:" + version);
 
          if (getParameter("typ") != null) {
          if (getParameter("typ").equals("exam")) {
          typ_ = "exam";
          System.out.println("exam: ");
          } else if (getParameter("typ").equals("exercise")) {
          System.out.println("exercise: ");
          typ_ = "exercise";
          } else {
          System.out.println("normal: ");
          typ_ = "normal";
          }
          } else {
          System.out.println("Parameter typ nicht angegeben -> setze typ: normal");
          }
 
          String abs_filename = getParameter("abs_filename");*/
         setTitle("Neptun Roboter Simulation");
         String abs_filename = null;
         if (abs_filename == null) {
             abs_filename = getDocumentBase().getFile() + "/config/games/Game.xml";
             System.out.println("Parameter abs_filename nicht angegeben -> setze default file: " + abs_filename);
         } else {
             System.out.println("take file: " + abs_filename);
         }
 
         URL new_url = null;
         try {
             new_url = new URL(getDocumentBase(), abs_filename);
             TUGInformations.setGameDocumentBase(new_url);
         } catch (MalformedURLException ex) {
             System.err.println(ex.getMessage());
         }
         file_name_ = new_url.toString(); // "Game.xml", Level-Info
 
         TUGInformations.setDocumentBase(getDocumentBase());
         TUGInformations.setCodeBase(getCodeBase());
         TUGInformations.setAppletContext(getAppletContext());
         TUGInformations.addGameFileName(file_name_);
 
 //        try {
 //            cookie_ = (String) JSObject.getWindow(this).eval("document.cookie");
 //            //System.out.println("Cookie == " + cookie_);
 //        } catch (Exception ex) {
 //            System.out.println("This browser may not support Java to Javascript communication");
 //        }
 //        URL logger_url;
 //
 //        String user_id = "";
 //        if (getParameter("userid") != null) {
 //            user_id = getParameter("userid");
 //        }
 //        try {
 //            logger_url = new URL(getCodeBase(), "logger.php");
 //            SqlCommunicator.init(cookie_, logger_url, user_id, log_level);
 //        } catch (Exception e) {
 //            System.out.println("Log writing failed! (Could not get code base)");
 //        }
 //        SqlCommunicator.add_log("Appleteinstellungen: BenutzerID= " + user_id + " Labyrinthmodus= " + typ_ + " verwende " + abs_filename + " Loglevel= " + log_level + " Version= " + version, 1);
 //
         String line_separator = System.getProperty("line.separator");
         String java_vendor = System.getProperty("java.vendor");
         String java_version = System.getProperty("java.version");
         String os_arch = System.getProperty("os.arch");
         String os_name = System.getProperty("os.name");
         String os_version = System.getProperty("os.version");
 
         SqlCommunicator.add_log("java_vendor=" + java_vendor + " java_version=" + java_version + " os_name=" + os_name + " os_version=" + os_version + " os_arch=" + os_arch, 1);
 
         mark_array_ = new IntStringMap();
         state_array_ = new IntStringMap();
         mark_array_.setString("0");
         mark_array_.setString("1");
         mark_array_.setString("2");
         mark_array_.setString("3");
 //         state_array_.setString("NONE");
         state_array_.setString("grün");
         state_array_.setString("blau");
         state_array_.setString("rot");
         state_array_.setString("gelb");
 
         level_handler_ = new LevelHandler();
         level_handler_.setDocumentBase(getDocumentBase());
         level_handler_.setMarkArray(mark_array_);
         level_handler_.loadGameXmlFile(file_name_);
 
         // Images -> HashMap "brick_images_"
         images_ = new BrickImages(getCodeBase());
         brick_images_ = images_.loadImages();
 
         status_message_ = new StatusPanel();
         aktive_rule_panel_ = new AktiveRulePanel(brick_images_, mark_array_, state_array_);
         rule_tree_panel_ = new RuleTreePanel(brick_images_, mark_array_, state_array_, new Dimension(230, height - 40 + 2));
         rule_tree_scrollpane_ = new JScrollPane(rule_tree_panel_);
         print_labyrinth_ = new PrintLabyrinth(brick_images_, mark_array_, state_array_);
         statistics_panel_ = new StatisticsPanel();
         speed_panel_ = new SpeedPanel();
         level_button_ = new JButton();
 
         getContentPane().setLayout(null);
         this.setSize(width, height);
         getContentPane().add(rule_tree_scrollpane_);
         getContentPane().add(aktive_rule_panel_);
         getContentPane().add(print_labyrinth_);
         getContentPane().add(status_message_);
         getContentPane().add(statistics_panel_);
         getContentPane().add(speed_panel_);
         getContentPane().add(level_button_);
 
         level_button_.setBackground(new Color(127, 127, 127));
         level_button_.setForeground(Color.ORANGE);
         print_labyrinth_.setBackground(Color.ORANGE);
         status_message_.setBackground(Color.ORANGE);
         status_message_.setVisible(true);
         this.getContentPane().setBackground(Color.ORANGE);
 
         controller_gui_communicator_ = new ControllerGuiCommunicator(rule_tree_panel_, aktive_rule_panel_, brick_images_, mark_array_, state_array_, getCodeBase());
         control_level_ = new ControlLevel(print_labyrinth_, statistics_panel_, speed_panel_, level_button_, status_message_, level_handler_, mark_array_);
 
         //TODO: what is this
         if (typ_ == "exam") {
             rule_tree_scrollpane_.setBounds(5, 35, 248, height - 40 + 1);
             level_button_.setBounds(730, 510, 150, 30);
             print_labyrinth_.setBounds(265, 70, 400, 400);
             status_message_.setBounds(265, 490, 400, 70);
             statistics_panel_.setBounds(677, 62, 265, 150);
             speed_panel_.setBounds(677, 225, 265, 100);
             aktive_rule_panel_.setBounds(677, 338, 265, 135);
             menue_exam_panel_ = new MenueExamPanel(level_handler_);
             menue_exam_panel_.setBounds(0, 0, width, 30);
             getContentPane().add(menue_exam_panel_);
             control_simulation_ = new ControlSimulation(menue_exam_panel_, level_handler_, mark_array_, this);
             control_level_.drawNewLabyrinth(menue_exam_panel_.getCurrentSelected());
         } else if (typ_ == "exercise") {
             rule_tree_scrollpane_.setBounds(5, 35, 248, height - 40 + 1);
             level_button_.setBounds(730, 510, 150, 30);
             print_labyrinth_.setBounds(265, 70, 400, 400);
             status_message_.setBounds(265, 490, 400, 70);
             statistics_panel_.setBounds(677, 62, 265, 150);
             speed_panel_.setBounds(677, 225, 265, 100);
             aktive_rule_panel_.setBounds(677, 338, 265, 135);
             menue_exercise_panel_ = new MenueExercisePanel(level_handler_);
             menue_exercise_panel_.setBounds(0, 0, width, 30);
             getContentPane().add(menue_exercise_panel_);
             control_simulation_ = new ControlSimulation(menue_exercise_panel_, level_handler_, mark_array_);
             control_level_.drawNewLabyrinth(1);
         } else {
             rule_tree_scrollpane_.setBounds(5, 10, 248, height - 40 + 1); // mit menue
             level_button_.setBounds(730, 480, 150, 30);
             print_labyrinth_.setBounds(265, 40, 400, 400);
             status_message_.setBounds(265, 460, 400, 70);
             statistics_panel_.setBounds(677, 32, 265, 150);
             speed_panel_.setBounds(677, 195, 265, 100);
             aktive_rule_panel_.setBounds(677, 308, 265, 135);
             menue_panel_ = new MenuePanel(getCodeBase(), level_handler_, this);
             control_simulation_ = new ControlSimulation(menue_panel_, level_handler_, mark_array_);
             this.setJMenuBar(menue_panel_.getMenueBar());
             control_level_.drawNewLabyrinth(1);
         }
 
         controller_gui_communicator_.setCommunicationElements(control_level_, control_simulation_);
         control_level_.setCommunicationElements(controller_gui_communicator_, control_simulation_);
         control_simulation_.setCommunicationElements(controller_gui_communicator_, control_level_);
         // END OF WINDOW SETUP ******************************************************************
 
     }
 
     public void drawFinish() {
         JLabel finish = new JLabel();
         finish.setOpaque(true);
         finish.setBackground(new Color(127, 127, 127));
         finish.setForeground(Color.ORANGE);
         finish.setText("                             Alle Labyrinthe bearbeitet...");
         finish.setBounds(265, 70, 400, 400);
         getContentPane().remove(print_labyrinth_);
         getContentPane().remove(level_button_);
         getContentPane().add(finish);
         aktive_rule_panel_.removeAktiveRule();
         statistics_panel_.clearPanel();
 
         status_message_.clear();
         status_message_.setNewFirstLine("   Danke für Ihre Teilnahme!");
         rule_tree_panel_.deleteAllRules();
         repaint();
     }
 
     public static void main(String[] args) {
         new TUGLabyrinth();
     }
 
 }
