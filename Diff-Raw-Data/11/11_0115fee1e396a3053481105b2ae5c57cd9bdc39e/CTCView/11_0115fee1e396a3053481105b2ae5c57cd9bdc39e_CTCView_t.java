 /*
 *	Program Name:	CTCView.java
 *	Lead Programmer:	Zachary Sweigart
 *	Date Modified:	4/19/12
 */
 
 package ctc;
 
 import simulator.Simulator;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.FocusEvent;
 import java.text.ParseException;
 import javax.swing.*;
 import javax.swing.text.MaskFormatter;
 import global.*;
 import trackmodel.*;
 import traincontroller.*;
 import trainmodel.*;
 
 /**
  * This file contains the specification for the CTCView object which defines the
  *  main GUI for the program
  * 
  * @author Zachary Sweigart
  */
 public class CTCView extends JFrame
 {
     private static boolean debugMode;   // used to determine if the system is in debug mode
     private String dispatcherID = "";   // used to hold the id for the dispatcher currently logged in
     private Dispatcher dispatcherLoggedin = null;
     private int lineSelectedIndex = 0; // used to hold the location of the currently selected train line
     private int controllerSelectedIndex = 0; // used to hold the location of the currently selected wayside
     private int blockSelectedIndex = 0; // used to hold the location of the currently selected train block
     private int trainSelectedIndex = 0; // used to hold the location of the currently selected train
     private int operatorTrainSelectedIndex = 0;
     private int dispatcherSelectedIndex = 0;    // used to hold the location of the currently selected train block on the dipatcher screen
     //private int dispatcherSelectedTrainIndex = 0;
     private int clockSelectedIndex = 8;
     private static CTCModel model = new CTCModel(); // references the model of the system 
     private static CTCControl control = new CTCControl(model);  // references the control that passes messages to the system
     private static Simulator sim;   // references the simulator for the system
     private boolean isOpen = true;  // used to determine if the main GUI window is open
     private int clockRate = 60; // used to set the clock rate for the system
     private boolean demoMode = false;   // used to determine if demo mode is active
     private MainPanel mainPanel;    // used to create the dispaly for the main screen
     private DispatcherPanel dispatcherPanel;    // used to create the dispaly for the dispatcher screen
     private OperatorPanel operatorPanel;  // used to create the dispaly for the operator screen
     private TrainPanel trainPanel;  // used to create the dispaly for the add/modify train screen
     private TrackPanel trackPanel;  // used to create the dispaly for the view/modify track screen
     private MetricsPanel metricsPanel;  // used to create the dispaly for the view metrics screen
     private SplashPanel splashPanel = new SplashPanel();    // used to create the dispaly for the splash screen
     private Line selectedLine = Line.GREEN;
     private ID selectedWaysideID;
     private ID selectedDispatcherTrainID;
     private Line dispatcherLine;
     private ID dispatcherWaysideID;
     private Dispatcher [] dispatchers = new Dispatcher [10];
     
     /**
      * creates a new CTCView object and initializes debug mode to false
      */
     public CTCView()
     {
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setLayout(new BorderLayout());
         this.add(splashPanel);
         this.setSize(800,600);
         this.setVisible(true);
         intialize();
         debugMode = false;
         control.setDebugMode(debugMode);
         model.setDebugMode(debugMode);
     }
     
     /**
      * creates a new CTCView object with provided debug mode and simulator
      * 
      * @param d boolean which sets the debug mode flag
      * @param s Simulator object which the runs the system on clock ticks
      */
     public CTCView(boolean d, Simulator s)
     {
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setLayout(new BorderLayout());
         this.add(splashPanel);
         this.setSize(800,600);
         this.setVisible(true);
         sim = s;
         intialize();
         debugMode = d;
         control.setDebugMode(debugMode);
         control.setSimulator(s);
         model.setDebugMode(debugMode);
     }
 
     private void intialize()
     {
         JMenuBar menuBar = new JMenuBar();  // used to create the main menu bar for the GUI
         JMenu fileMenu = new JMenu("File"); // used to create the file menu for the main menu bar
         JMenuItem fileClose = new JMenuItem("Close");   // used to create the Close option in the file menu
         JMenu viewMenu = new JMenu("View"); // used to create the view menu for the main menu bar
         JMenuItem viewMain = new JMenuItem("Main"); // used to create the Main option in the view menu
         JMenuItem viewDispatcher = new JMenuItem("Dispatcher"); // used to create the Dispatcher option in the view menu
         JMenuItem viewOperator = new JMenuItem("Operator"); // used to create the Operator option in the view menu
         JMenuItem viewTrain = new JMenuItem("Add/Modify Train");    // used to create the Add/Modify Train option in the view menu
         JMenuItem viewTrack = new JMenuItem("View/Modify Track");   // used to create the View/Modify Track option in the view menu
         JMenuItem viewMetrics = new JMenuItem("Metrics");   // used to create the Metrics option in the view menu
         JMenu runMenu = new JMenu("Run");   // used to create the run menu for the main menu bar
         JMenuItem runDemo = new JMenuItem("Run Demo");  // used to create the Run Demo option in the run menu
         JMenuItem runCancel = new JMenuItem("Cancel Demo"); // used to create the Cancel Demo option in the run menu
         
         menuBar.add(fileMenu);
         menuBar.add(viewMenu);
         menuBar.add(runMenu);
         fileMenu.add(fileClose);
         viewMenu.add(viewMain);
         viewMenu.add(viewDispatcher);
         viewMenu.add(viewOperator);
         viewMenu.add(viewTrain);
         viewMenu.add(viewTrack);
         viewMenu.add(viewMetrics);
         runMenu.add(runDemo);
         runMenu.add(runCancel);
         setJMenuBar(menuBar);
         
         mainPanel = new MainPanel(8);
         dispatcherPanel = new DispatcherPanel();
         trainPanel= new TrainPanel();
         trackPanel = new TrackPanel();
         metricsPanel = new MetricsPanel();
         operatorPanel = new OperatorPanel();
         
         fileClose.addActionListener(new MenuActionClose());
         viewMain.addActionListener(new MenuOpenScreen(mainPanel));
         viewDispatcher.addActionListener(new MenuDialogLogin(this));
         viewOperator.addActionListener(new MenuOpenScreen(operatorPanel));
         viewTrain.addActionListener(new MenuOpenScreen(trainPanel));
         viewTrack.addActionListener(new MenuOpenScreen(trackPanel));
         viewMetrics.addActionListener(new MenuOpenScreen(metricsPanel));
         runDemo.addActionListener(new MenuActionRunDemo());
         runCancel.addActionListener(new MenuActionCancelDemo());
         
         dispatchers[0] = (new Dispatcher("sweigartz", "password", Line.GREEN, (new ID(Line.GREEN, 'A', -1))));
         dispatchers[1] = (new Dispatcher("dgreenb", "password", Line.GREEN, (new ID(Line.GREEN, 'B', -1))));
         dispatchers[2] = (new Dispatcher("dgreenc", "password", Line.GREEN, (new ID(Line.GREEN, 'C', -1))));
         dispatchers[3] = (new Dispatcher("dgreend", "password", Line.GREEN, (new ID(Line.GREEN, 'D', -1))));
         dispatchers[4] = (new Dispatcher("dgreene", "password", Line.GREEN, (new ID(Line.GREEN, 'E', -1))));
         dispatchers[5] = (new Dispatcher("dgreenf", "password", Line.GREEN, (new ID(Line.GREEN, 'F', -1))));
         dispatchers[6] = (new Dispatcher("dgreeng", "password", Line.GREEN, (new ID(Line.GREEN, 'G', -1))));
         dispatchers[7] = (new Dispatcher("dgreenh", "password", Line.GREEN, (new ID(Line.GREEN, 'H', -1))));
         dispatchers[8] = (new Dispatcher("dgreeni", "password", Line.GREEN, (new ID(Line.GREEN, 'I', -1))));
         dispatchers[9] = (new Dispatcher("marinic", "password", Line.GREEN, (new ID(Line.GREEN, 'J', -1))));
     }
     
     private class MenuActionRunDemo implements ActionListener
     {
         private MenuActionRunDemo() {}
         
         @Override
         public void actionPerformed(ActionEvent e) 
         {
             if(debugMode)
             {
                 System.out.println("CTC View: Run Demo menu item clicked");
             }
             demoMode = true;
         }
     }
     
     private class MenuActionCancelDemo implements ActionListener
     {
         private MenuActionCancelDemo() {}
         
         @Override
         public void actionPerformed(ActionEvent e) 
         {
             if(debugMode)
             {
                 System.out.println("CTC View: Cancel Demo menu item clicked");
             }
             demoMode = false;
         }
     }
     
     private class MenuActionClose implements ActionListener 
     {
         private MenuActionClose() {}
         
         @Override
         public void actionPerformed(ActionEvent e) 
         {
             if(debugMode)
             {
                 System.out.println("CTC View: Close menu item clicked");
             }
             isOpen = false;
             dispose();
         }
 
     }
     
     private class MenuDialogLogin implements ActionListener
     {
         private JFrame frame;
         
         private MenuDialogLogin(JFrame f) 
         {
             this.frame = f;
         }
         @Override
         public void actionPerformed(ActionEvent e) 
         {
             LogInDialog login = new LogInDialog(frame, debugMode, dispatchers);
             login.setVisible(true);
             if(login.isSucceeded())
             {
                 dispatcherLoggedin = login.getVerifiedDispatcher();
                 dispatcherPanel.initialize();
                 changePanel(dispatcherPanel);
             }
             else
             {
                 changePanel(mainPanel);
             }
                         
         }
     }
     
     private class MenuOpenScreen implements ActionListener 
     {
         private JPanel panel;
         
         private MenuOpenScreen(JPanel pnl) 
         {
             this.panel = pnl;
         }
         
         @Override
         public void actionPerformed(ActionEvent e) 
         {
             changePanel(panel);
         }
 
     }
     
     private void changePanel(JPanel panel) 
     {
         if(panel instanceof MainPanel)
         {
             if(debugMode)
             {
                 System.out.println("CTC View: View Main menu item clicked");
             }
             //((MainPanel)panel).initialize();
         }
         else
         {
             if(panel instanceof DispatcherPanel)
             {
                 if(debugMode)
                 {
                     System.out.println("CTC View: View Dispatcher menu item clicked");
                 }
                 //((DispatcherPanel)panel).initialize();
             }
             else
             {
                 if(panel instanceof OperatorPanel)
                 {
                     if(debugMode)
                     {
                         System.out.println("CTC View: View Operator menu item clicked");
                     }
                     ((OperatorPanel)panel).initialize();
                 }  
                 else
                 {
                     if(panel instanceof TrainPanel)
                     {
                         if(debugMode)
                         {
                             System.out.println("CTC View: View Add/Modify Train menu item clicked");
                         }
                         ((TrainPanel)panel).initialize();
                     }
                     else
                     {
                         if(panel instanceof TrackPanel)
                         {
                             if(debugMode)
                             {
                                 System.out.println("CTC View: View View/Modify Track menu item clicked");
                             }
                             //((TrackPanel)panel).initialize();
                         } 
                         else
                         {
                             if(panel instanceof MetricsPanel)
                             {
                                 if(debugMode)
                                 {
                                     System.out.println("CTC View: View Metrics menu item clicked");
                                 }
                                 ((MetricsPanel)panel).initialize();
                             }   
                         }
                     }
                 }
             }
         }
         getContentPane().removeAll();
         getContentPane().add(panel);
         getContentPane().validate();
         update(getGraphics());
     }
     
     /**
      * return the state of the debug mode flag
      * 
      * @return a boolean representing the state of the debug mode flag
      */
     public static boolean getDebugMode()
     {
         return debugMode;
     }
     
     /**
      * return the model
      * 
      * @return a CTCModel 
      */
     public static CTCModel getModel()
     {
         return model;
     }
     
     private class MainPanel extends JPanel
     {
         private MapPanel map = new MapPanel(model, ""); // references the panel that paints the system
         private JLabel clockLabel = new JLabel("Clock Rate (1 hr = )"); // labels the dropdown for selecting clock rate
         private final String clockRates [] = {"5 minutes", "6 minutes", 
             "10 minutes", "15 minutes", "20 minutes", "30 minutes", 
             "40 minutes", "45 minutes", "60 minutes", "90 minutes", "120 minutes"};
         /*
          * Holds the possible options for setting the clock rate
          */
         private JComboBox clockCombo= new JComboBox(clockRates);    // used to create a dropdown which holds the options for setting the clock rate 
         private JLabel trackSectionLabel = new JLabel("Display track secion: ");    // labels the dropdown for selecting the section to paint
         private final String trackSections [] = {"", "Green Line ", "Green A", "Green B", "Green C", 
             "Green D", "Green E", "Green F", "Green G", "Green H", "Green I", "Green J", "Red Line "};
         /*
          * Holds the possible options for displaying the system
          */
         private JComboBox trackCombo = new JComboBox(trackSections);  // used to create a dropdown which holds the options for setting the clock rate
         private Insets insets = new Insets(0,10,0,0);   // sets the insets for all of the displayed objects except the map
         private Insets insets2 = new Insets(0,0,0,0);   // sets the insets for the map panel
 
         MainPanel()
         {
             this.setLayout(new GridBagLayout());
             initialize();
         }
         
         MainPanel(int index)
         {
             clockSelectedIndex = index;
             this.setLayout(new GridBagLayout());
             initialize();
         }
 
         private void initialize()
         {                        
             if(clockCombo.getActionListeners().length <= 0)
             {
                 clockCombo.addActionListener(clockComboListener);
             }
             if(trackCombo.getActionListeners().length <= 0)
             {
                 trackCombo.addActionListener(trackComboListener);
             }
             clockCombo.setSelectedIndex(clockSelectedIndex);
             
             addComponent(this, map, 0, 0, 4, 1, 1.0, 1.0, insets2, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
             addComponent(this, clockLabel, 0, 1, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, clockCombo, 1, 1, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, trackSectionLabel, 2, 1, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, trackCombo, 3, 1, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
         }
         
         
         ActionListener clockComboListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JComboBox cb = (JComboBox)event.getSource();
                 int clockSelectedIndex = (int)cb.getSelectedIndex();
                 switch(clockSelectedIndex)
                 {
                     case 0:
                         clockRate = 5;
                         break;
                     case 1:
                         clockRate = 6;
                         break;
                     case 2:
                         clockRate = 10;
                         break;
                     case 3:
                         clockRate = 15;
                         break;
                     case 4:
                         clockRate = 20;
                         break;
                     case 5:
                         clockRate = 30;
                         break;
                     case 6:
                         clockRate = 40;
                         break;
                     case 7:
                         clockRate = 45;  
                         break;
                     case 8:
                         clockRate = 60;  
                         break;
                     case 9:
                         clockRate = 90;  
                         break;
                     default:
                         clockRate = 120;
                 }
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: Clock rate set to : " + clockRate);
                 }
                 sim.setClockRate(clockRate);
             }
         };
         
         ActionListener trackComboListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: track section selected: " + trackCombo.getSelectedItem().toString());
                 }
                 map.setDebugMode(CTCView.getDebugMode());
                 map.setTrackSection(trackCombo.getSelectedItem().toString());
                 map.repaint();
             }
             
         };
         
         public void paintComponent(Graphics g)
         {
             int width = getWidth();
             int height = getHeight();
             g.setColor(Color.WHITE);
             g.fillRect(0,0, width, height);
         }
     }
     
     private class DispatcherPanel extends JPanel
     {
         private MapPanel map = new MapPanel(model); // references the panel that paints the map
         private JLabel dispatcherIDLabel;   // displays the userid of the dispatcher that is logged in
         //private JLabel trainID = new JLabel("Train ID");  //
         //private JComboBox trains = new JComboBox();   //
         private JLabel trackID = new JLabel("Track ID");    // lables the track id combo box
         private JComboBox track = new JComboBox();  // holds the track blocks available to the dispatcher
         private JLabel setpointLabel = new JLabel("Setpoint");  // labels  the setpoint input field
         private JTextField setpoint = new JTextField(); // used to enter the desired setpoint
         private JLabel authorityLabel = new JLabel("Authority");    // labels the authority input field
         private JTextField authority = new JTextField();    // used to enter the desired authority
         private JButton sendSetpoint;   // sends the value in the setpoint to the block
         private JButton sendAuthority;  // sends the value in the authority to the block
         private Insets insets = new Insets(0,0,0,0);    // insets for the map
         private Insets insets2 = new Insets(0,0,0,10);  // insets for all other display components
         private String [] trackIDs; // holds the trackIDs available to the dispatcher
         private String [] trainIDs; //
 
         DispatcherPanel()
         {
             this.setLayout(new GridBagLayout());
             initialize();
         }
 
         private void initialize()
         {           
             map.setDebugMode(debugMode);
             
             if(dispatcherLoggedin != null)
             {
                 map.setTrackSection(dispatcherLoggedin.getWayside());
             }
             
             if(dispatcherLoggedin != null)
             {
                 trackIDs = model.getTrackIDs(dispatcherLoggedin.getWayside());
             }
             else
             {
                 trackIDs = new String[0];
             }
             
 //            trainIDs = sim.getTrainIDs();
             
             track.removeAllItems();
             track.addItem("");
             for(int i = 0; i < trackIDs.length; i++)
             {
                 track.addItem(trackIDs[i]);
             }
             track.setSelectedIndex(dispatcherSelectedIndex);
             
 //            trains = new JComboBox();
 //            trains.addItem("");
 //            for(int i = 0; i < trainIDs.length; i++)
 //            {
 //            //System.out.println(trainIDs[i]);
 //                trains.addItem(trainIDs[i]);
 //            }
 //            trains.setSelectedIndex(dispatcherSelectedTrainIndex);
             
             if(CTCView.getDebugMode())
             {
                 System.out.println("CTC View: Dispatcher ID: " + dispatcherLoggedin.getUserName());
             }
            
             sendSetpoint = new JButton("Send Setpoint"); 
             sendAuthority = new JButton("Send Authority"); 
             
             dispatcherIDLabel = new JLabel(dispatcherID);
             setpoint.setColumns(10);
             authority.setColumns(10);
             
             sendSetpoint.addActionListener(buttonClick);
             sendAuthority.addActionListener(buttonClick);
             track.addActionListener(trackComboListener);
             //trains.addActionListener(trainsComboListener);
             
             addComponent(this, map, 0, 0, 6, 1, 1.0, 1.0, insets, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
             addComponent(this, dispatcherIDLabel, 0, 1, 1, 1, 0, 0, insets2, GridBagConstraints.EAST, GridBagConstraints.NONE);
             //addComponent(this, trainID, 0, 2, 1, 1, 0, 0, insets2, GridBagConstraints.EAST, GridBagConstraints.NONE);
             //addComponent(this, trains, 1, 2, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.BOTH);
             addComponent(this, setpointLabel, 0, 3, 1, 1, 0, 0, insets2, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, setpoint, 1, 3, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.NONE);      
             addComponent(this, sendSetpoint, 2, 3, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, trackID, 0, 2, 1, 1, 0, 0, insets2, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, track, 1, 2, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.BOTH);
             addComponent(this, authorityLabel, 3, 3, 1, 1, 0, 0, insets2, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, authority, 4, 3, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, sendAuthority, 5, 3, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.NONE);
         }
 
         ActionListener buttonClick = new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 if(e.getSource().toString().equals(sendSetpoint.toString()))
                 {
                     try
                     {
                         int set = Integer.parseInt(setpoint.getText());
                         if(set < 0 || set > 70)
                         {
                             JOptionPane.showMessageDialog(DispatcherPanel.this, "Invalid Setpoint value, please enter a number between 0 and 70", "Input Error", JOptionPane.ERROR_MESSAGE);
                         }
                         else
                         {
                             if(dispatcherSelectedIndex == 0)
                             {
                                JOptionPane.showMessageDialog(DispatcherPanel.this, "Invalid track selection, please select the track to send the setpoint", "Input Error", JOptionPane.ERROR_MESSAGE); 
                             }
                             else
                             {
                                 if(CTCView.getDebugMode() && dispatcherSelectedIndex > 0)
                                 {
                                     System.out.println("CTC View: Setpoint value: " + set + " sent to " + trackIDs[dispatcherSelectedIndex - 1]);
                                 }
                                 control.setDispatcherSpeed(set, dispatcherLoggedin.getLine(), dispatcherLoggedin.getWayside().getSection(), Integer.parseInt(trackIDs[dispatcherSelectedIndex - 1].substring(1,2)));
                             }
                         }
                     }
                     catch(NumberFormatException p)
                     {
                         JOptionPane.showMessageDialog(DispatcherPanel.this, "Invalid Setpoint value, please enter a number between 0 and 70", "Input Error", JOptionPane.ERROR_MESSAGE);
                     }
                 }
                 if(e.getSource().toString().equals(sendAuthority.toString()))
                 {
                     try
                     {
                         int auth = Integer.parseInt(authority.getText());
                         String trackID = track.getSelectedItem().toString();
                         if(CTCView.getDebugMode() && dispatcherSelectedIndex > 0)
                         {
                             System.out.println("CTC View: Authority value: " + auth + " sent to " + trackIDs[dispatcherSelectedIndex - 1]);
                         }
                         if(auth < 0 || auth > 70)
                         {
                             JOptionPane.showMessageDialog(DispatcherPanel.this, "Invalid Authority value, please enter a number between 0 and 70", "Input Error", JOptionPane.ERROR_MESSAGE);
                         }
                         else
                         {
                             if(dispatcherSelectedIndex == 0)
                             {
                                JOptionPane.showMessageDialog(DispatcherPanel.this, "Invalid track selection, please select the track to send the authority", "Input Error", JOptionPane.ERROR_MESSAGE); 
                             }
                             else
                             {
                                 control.setDispatcherAuthority(auth, dispatcherLoggedin.getLine(), dispatcherLoggedin.getWayside().getSection(), Integer.parseInt(trackIDs[dispatcherSelectedIndex - 1].substring(1,2)));
                             }
                         }
                     }
                     catch(NumberFormatException p)
                     {
                         JOptionPane.showMessageDialog(DispatcherPanel.this, "Invalid Authority value, please enter a number between 0 and 70", "Input Error", JOptionPane.ERROR_MESSAGE);
                     }
                 }
             }
         };
         
         ActionListener trackComboListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JComboBox cb = (JComboBox)event.getSource();
                 dispatcherSelectedIndex = (int)cb.getSelectedIndex();
                 String id = (String)cb.getSelectedItem();
                 //selectedDispatcherTrainID = 
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("Track " + id + " selected");
                 }
             }
         };
         
 //        ActionListener trainsComboListener = new ActionListener()
 //        {
 //            public void actionPerformed(ActionEvent event)
 //            {
 //                JComboBox cb = (JComboBox)event.getSource();
 //                dispatcherSelectedTrainIndex = (int)cb.getSelectedIndex();
 //                String id = (String)cb.getSelectedItem();
 //                //Track t = model.getTrack(id);
 //                //get all track properties
 //                //initialize();
 //                if(CTCView.getDebugMode())
 //                {
 //                    System.out.println("Track " + id + " selected");
 //                }
 //            }
 //        };
         
         public void paintComponent(Graphics g)
         {
             int width = getWidth();
             int height = getHeight();
             g.setColor(Color.WHITE);
             g.fillRect(0,0, width, height);
         }
     }
     
     private class OperatorPanel extends JPanel
     {
         private MapPanel map = new MapPanel(model); // references the panel that paints the display
         private JLabel trainID = new JLabel("Train ID: ");  // labels the train id combo box
         private JComboBox trains = new JComboBox(); // holds the train ids currently in the system
         private JLabel speedLabel = new JLabel("Speed: ");  // labels the speed input box
         private JTextField operatorSpeed = new JTextField();    // input field for the operator speed
         private JLabel brakeLabel = new JLabel ("Emergency Brake: ");   // labels the brake check box
         private JCheckBox brake = new JCheckBox("");    // allows the brake to be set on or off
         private JButton send;   // sends the operator commands to the system
         private Insets insets = new Insets(0,0,0,0);    // insets for all components except the map panel
         private Insets insets2 = new Insets(0,0,0,10);  // insets for the map panel
         private JLabel trackSectionLabel = new JLabel("Display track secion: ");
         /*
          * labels the track combo box select
          */
         private final String trackSections [] = {"", "Green Line ", "Green A", "Green B", "Green C", 
             "Green D", "Green E", "Green F", "Green G", "Green H", "Green I", "Green J", "Red Line "};
         /*
          * holds all of the sections that can be displayed
          */
         private JComboBox trackCombo = new JComboBox(trackSections);    // displays all the track sections that can be displayed  
         private String [] trainIDs;
         
         OperatorPanel()
         {
             this.setLayout(new GridBagLayout());
             initialize();
         }
 
         private void initialize()
         {
             trainIDs = sim.getTrainIDs();
             
             if(trainIDs.length != 0)
             {
                 trains.removeAllItems();
             }
             trains.addItem("");
             for(int i = 0; i < trainIDs.length; i++)
             {
                 trains.addItem(trainIDs[i]);
             }
             trains.setSelectedIndex(trainSelectedIndex);
             
             map.setDebugMode(debugMode);
             map.setTrackSection("");
             send = new JButton("Send");
             operatorSpeed.setColumns(10);
             
             if(send.getActionListeners().length <= 0)
             {
                 send.addActionListener(buttonClick);
             }
             if(trackCombo.getActionListeners().length <= 0)
             {
                 trackCombo.addActionListener(trackComboListener);
             }
             if(trains.getActionListeners().length <= 0)
             {
                 trains.addActionListener(trainComboListener);
             }
             
             addComponent(this, map, 0, 0, 6, 1, 1.0, 1.0, insets, GridBagConstraints.CENTER, GridBagConstraints.BOTH);
             addComponent(this, trainID, 0, 1, 1, 1, 0, 0, insets2, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, trains, 1, 1, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.BOTH);
             addComponent(this, speedLabel, 0, 2, 1, 1, 0, 0, insets2, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, operatorSpeed, 1, 2, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, brakeLabel, 3, 2, 1, 1, 0, 0, insets2, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, brake, 4, 2, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, trackSectionLabel, 3, 1, 1, 1, 0, 0, insets2, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, trackCombo, 4, 1, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, send, 5, 2, 1, 1, 0, 0, insets2, GridBagConstraints.WEST, GridBagConstraints.NONE);
         }
 
         ActionListener buttonClick = new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 if(e.getSource().toString().equals(send.toString()))
                 {
                     try
                     {
                         int set = Integer.parseInt(operatorSpeed.getText());
                         if(CTCView.getDebugMode())
                         {   
                             System.out.println("CTC View: Speed Entered: " + set);
                         }
                         if(set < 0 || set > 100)
                         {
                             JOptionPane.showMessageDialog(OperatorPanel.this, "Invalid Speed value, please enter a number between 0 and 100", "Input Error", JOptionPane.ERROR_MESSAGE);
                         }
                         else
                         {
                             if(trains.getSelectedIndex() <= 0)
                             {
                                     JOptionPane.showMessageDialog(OperatorPanel.this, "Please select a train", "Input Error", JOptionPane.ERROR_MESSAGE);
                             }
                             else
                             {
                                 sendOperatorCommands();
                             }
                         }
                     }
                     
                     catch(NumberFormatException p)
                     {
                         JOptionPane.showMessageDialog(OperatorPanel.this, "Invalid Speed value, please enter a number between 0 and 100", "Input Error", JOptionPane.ERROR_MESSAGE);
                     }
                 }
             }
         };
         
         ActionListener trackComboListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: track section selected: " + trackCombo.getSelectedItem().toString());
                 }
                 map.setTrackSection(trackCombo.getSelectedItem().toString());
                 map.repaint();
             }
             
         };
         
         ActionListener trainComboListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JComboBox cb = (JComboBox)event.getSource();
                 String id = (String)cb.getSelectedItem();
                 operatorTrainSelectedIndex = (int)cb.getSelectedIndex();
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: Train selected " + id);
                 }
             }
         };
 
         public void sendOperatorCommands()
         {
             if(CTCView.getDebugMode())
             {
                 System.out.println("CTC View: Commands sent, speed: " + operatorSpeed.getText() + " , brake: " + brake.isSelected());
             }
             control.setOperatorCommands(Integer.parseInt(operatorSpeed.getText()), brake.isSelected(), trainIDs[operatorTrainSelectedIndex - 1]);
         }
         
         public void paintComponent(Graphics g)
         {
             int width = getWidth();
             int height = getHeight();
             g.setColor(Color.WHITE);
             g.fillRect(0,0, width, height);
         }
     }
     
     private class TrainPanel extends JPanel
     {
         private String [] lines = {"Green", "Red"}; // holds the different lines in the system
         //private String [] trainYards = {"", "Train Yard 1"};    // holds the train yards in the system
         private JLabel currentTrainsLabel = new JLabel("Current Trains");   // labels the combo box for the train ids
        // private JLabel trainYardSelectLabel = new JLabel("Add new train from train yard: ");
         /*
          * labels the train yard selection combo box
          */
         private JLabel lineLabel = new JLabel("Line");  // labels the line combo
         private JLabel heightLabel = new JLabel("Height");  // labels the height field
         private JLabel widthLabel = new JLabel("Width");    // labels the width field
         private JLabel numCarsLabel = new JLabel("Number of Cars"); // labels the number of cars field
         private JLabel lengthLabel = new JLabel("Length");  // labels the length field
         private JLabel massLabel = new JLabel("Mass");  // labels the mass field
         private JLabel crewCountLabel = new JLabel("Crew Count");   // labels the crew count field
         private JLabel passengerCountLabel = new JLabel("Passenger Count"); // labels the passenger count field
         private JLabel currentSpeedLabel = new JLabel("Current Speed"); // labels the current speed field
         private JLabel currentAccelerationLabel = new JLabel("Current Acceleration");   // labels the current acceleration field
         private JLabel messageLabel = new JLabel("Message Displayed");  // labels the message field
         private JLabel headlightLabel = new JLabel("Headlights on");    // labels the hedlights check box
         private JLabel cabinLightLabel = new JLabel("Cabin Lights on"); // labels the cabin lights check box
         private JLabel doorLabel = new JLabel("Doors Open");    // labels the door check box
         private JLabel brakeFailLabel = new JLabel("Brake Failure");    // labels the brake failure check box
         private JLabel engineFailLabel = new JLabel("Engine Failure");  // labels the engine failure check box
         private JLabel signalPickupFailLabel = new JLabel("Signal Pickup Failure"); // labels the signal pickup failure check box
         private JLabel trainIDLabel = new JLabel("Train ID");   // labels the train ID dropdown 
         private JComboBox currentTrains = new JComboBox();;    // displays the ids of the trains in the system
         //private JComboBox trainYard = new JComboBox(trainYards);    // displays the train yard ids in the system
         private JComboBox line = new JComboBox(lines);  // displays the lines in the system
         private JTextField heightField = new JTextField();  // displays the height of the train selected
         private JTextField widthField = new JTextField();   // displays the width of the train selected
         private JTextField numCarsField = new JTextField(); // displays the number of cars of the train selected
         private JTextField lengthField = new JTextField();  // displays the length of the train selected
         private JTextField massField = new JTextField();    // displays the mass of the train selected
         private JFormattedTextField crewCountField;   // displays the crew count of the train selected
         private JTextField passengerCountField = new JTextField();  // displays the passenger count of the train selected
         private JTextField currentSpeedField = new JTextField();    // displays the current speed of the train selected
         private JTextField currentAccelerationField = new JTextField(); // displays the current acceleration of the train selected
         private JTextField messageField = new JTextField(); // displays the message of the train selected
         private JFormattedTextField trainIDField;   // input field for a new train's id
         private JCheckBox headlightCheck = new JCheckBox(); // displays the headlight state of the train selected
         private JCheckBox cabinLightCheck = new JCheckBox();    // displays the cabin light state of the train selected
         private JCheckBox doorCheck = new JCheckBox();  // displays the door state of the train selected
         private JCheckBox brakeFailCheck = new JCheckBox();   // displays the brake failure state of the train selected
         private JCheckBox engineFailCheck = new JCheckBox();  // displays the engine failure state of the train selected
         private JCheckBox signalPickupFailCheck = new JCheckBox();    // displays the signal pickup state of the train selected
         private JButton emergencyBrakeButton;   // sends emergency brake signal to the current train
         private JButton addTrainButton; // creates and adds a new train to the system
         private JButton removeTrainButton;  // removes selected train from the system
         private Insets insets = new Insets(5,20,0,20);  // insets for all components in the display
         private double height; // holds the height of the selected train
         private double width;  // holds the width of the selected train
         private int numCars;    // holds the number of cars of the selected train
         private double length; // holds the length of the selected train
         private double mass;   // holds the mass of the selected train
         private int crewCount;  // holds the crew count of the selected train
         private int passCount;  // holds the passenger count of the selected train
         private double currentSpeed;   // holds the current speed of the selected train
         private double currentAcceleration;    // holds the current acceleration of the selected train
         private String message; // holds the message of the selected train
         private boolean cabinLights;    // holds the cabin light state of the selected train
         private boolean headlights; // holds the headlight state of the selected train
         private boolean doors;  // holds the door state of the selected train
         private boolean brakeFailure;   // holds the brake failure state of the selected train
         private boolean engineFailure;  // holds the engine failure state of the selected train
         private boolean signalFailure;  // holds the signal pickup state of the selected train
         private int lineIndex;  // holds the index of the selected line
         private MaskFormatter format;   // formats the train id field to only accept 10 characters
         private String trainIDs []; // holds the train IDs currently in the system
         private String enteredTrainID;  // holds the train id entered by the user
         
         TrainPanel()
         {
             this.setLayout(new GridBagLayout());
             initialize();
         }
 
         private void initialize()
         {
             trainIDs = sim.getTrainIDs();
             
             if(trainIDs.length != 0)
             {
                 currentTrains.removeAllItems();
             }
             currentTrains.addItem("");
             for(int i = 0; i < trainIDs.length; i++)
             {
                 currentTrains.addItem(trainIDs[i]);
             }
             currentTrains.setSelectedIndex(trainSelectedIndex);
             line.setSelectedIndex(lineIndex);
             
             try
             {
                 format = new MaskFormatter("**********");
                 trainIDField = new JFormattedTextField(format);
             }
             catch(ParseException e)
             {
                 System.err.println("Unable to add format");
             }
             
             try
             {
                 format = new MaskFormatter("#");
                 crewCountField = new JFormattedTextField(format);
             }
             catch(ParseException e)
             {
                 System.err.println("Unable to add format");
             }
                         
             emergencyBrakeButton = new JButton("Emergency Brake");
             addTrainButton = new JButton("Add Train");
             removeTrainButton = new JButton("Remove Train");
                         
             if(emergencyBrakeButton.getActionListeners().length <= 0)
             {
                 emergencyBrakeButton.addActionListener(emergencyBrakeButtonListener);
             }
             if(addTrainButton.getActionListeners().length <= 0)
             {
                 addTrainButton.addActionListener(addTrainButtonListener);
             }
             if(removeTrainButton.getActionListeners().length <= 0)
             {
                 removeTrainButton.addActionListener(removeTrainButtonListener);
             }
             if(brakeFailCheck.getActionListeners().length <= 0)
             {
                 brakeFailCheck.addActionListener(brakeFailCheckListener);
             }
             if(engineFailCheck.getActionListeners().length <= 0)
             {
                 engineFailCheck.addActionListener(engineFailCheckListener);
             }
             if(signalPickupFailCheck.getActionListeners().length <= 0)
             {
                 signalPickupFailCheck.addActionListener(signalPickupFailCheckListener);
             }
             if(currentTrains.getActionListeners().length <= 0)
             {
                 currentTrains.addActionListener(trainComboListener);
             }
             if(trainIDField.getActionListeners().length <= 0)
             {
                 trainIDField.addActionListener(trainIDFieldListener);
             }
             if(trainIDField.getFocusListeners().length <= 2)
             {
                 trainIDField.addFocusListener(trainIDFocusListener);
             }
             if(trainIDField.getKeyListeners().length <= 0)
             {
                 trainIDField.addKeyListener(trainIDKeyListener);
             }
             if(crewCountField.getActionListeners().length <= 0)
             {
                 crewCountField.addActionListener(crewFieldListener);
             }
             if(crewCountField.getFocusListeners().length <= 2)
             {
                 crewCountField.addFocusListener(crewFocusListener);
             }
             if(crewCountField.getKeyListeners().length <= 0)
             {
                 crewCountField.addKeyListener(crewKeyListener);
             }
             
             heightField.setColumns(10);
             widthField.setColumns(10);
             numCarsField.setColumns(10);
             lengthField.setColumns(10);
             massField.setColumns(10);
             crewCountField.setColumns(10);
             passengerCountField.setColumns(10);
             currentSpeedField.setColumns(10);
             currentAccelerationField.setColumns(10);
             messageField.setColumns(10);
             trainIDField.setColumns(10);
             
             heightField.setEditable(false);
             widthField.setEditable(false);
             numCarsField.setEditable(false);
             lengthField.setEditable(false);
             massField.setEditable(false);
             crewCountField.setEditable(true);
             passengerCountField.setEditable(false);
             currentSpeedField.setEditable(false);
             currentAccelerationField.setEditable(false);
             messageField.setEditable(false);
             trainIDField.setEditable(true);
             cabinLightCheck.setEnabled(false);
             headlightCheck.setEnabled(false);
             doorCheck.setEnabled(false);
             
             heightField.setText("" + height);
             widthField.setText("" + width);
             numCarsField.setText("" + numCars);
             lengthField.setText("" + length);
             massField.setText("" + mass);
             passengerCountField.setText("" + passCount);
             currentSpeedField.setText("" + currentSpeed);
             currentAccelerationField.setText("" + currentAcceleration);
             crewCountField.setText("" + crewCount);
             trainIDField.setText("");
             
             if(message == null)
             {
                 messageField.setText("");
             }
             else
             {
                 messageField.setText("" + message);
             }
            
             cabinLightCheck.setSelected(cabinLights);
             headlightCheck.setSelected(headlights);
             doorCheck.setSelected(doors);
             engineFailCheck.setSelected(engineFailure);
             brakeFailCheck.setSelected(brakeFailure);
             signalPickupFailCheck.setSelected(signalFailure);
             /*if((engineFailCheck.isSelected() && !engineFailure) || (!engineFailCheck.isSelected() && engineFailure))
             {
                 engineFailCheck.doClick();
             }
             if((brakeFailCheck.isSelected() && !brakeFailure) || (!brakeFailCheck.isSelected() && brakeFailure))
             {
                 brakeFailCheck.doClick();
             }
             if((signalPickupFailCheck.isSelected() && !signalFailure) || (!signalPickupFailCheck.isSelected() && signalFailure))
             {
                 signalPickupFailCheck.doClick();
             }*/
                        
             addComponent(this, currentTrainsLabel, 0, 0, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, currentTrains, 1, 0, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.BOTH);
             //addComponent(this, trainYardSelectLabel, 2, 0, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             //addComponent(this, trainYard, 3, 0, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.BOTH);
             addComponent(this, trainIDLabel, 2, 1, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, trainIDField, 3, 1, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, lineLabel, 0, 2, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, line, 1, 2, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.BOTH);
             addComponent(this, heightLabel, 0, 3, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, heightField, 1, 3, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, widthLabel, 0, 4, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, widthField, 1, 4, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, numCarsLabel, 0, 5, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, numCarsField, 1, 5, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, lengthLabel, 0, 6, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, lengthField, 1, 6, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, massLabel, 0, 7, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, massField, 1, 7, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, crewCountLabel, 0, 8, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, crewCountField, 1, 8, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, passengerCountLabel, 0, 9, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, passengerCountField, 1, 9, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, currentSpeedLabel, 0, 10, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, currentSpeedField, 1, 10, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, currentAccelerationLabel, 0, 11, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, currentAccelerationField, 1, 11, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, messageLabel, 0, 12, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, messageField, 1, 12, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, cabinLightLabel, 0, 13, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, cabinLightCheck, 1, 13, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, headlightLabel, 0, 14, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, headlightCheck, 1, 14, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, doorLabel, 0, 15, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, doorCheck, 1, 15, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, brakeFailLabel, 0, 16, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, brakeFailCheck, 1, 16, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, engineFailLabel, 0, 17, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, engineFailCheck, 1, 17, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, signalPickupFailLabel, 0, 18, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, signalPickupFailCheck, 1, 18, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, emergencyBrakeButton, 1, 19, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, addTrainButton, 2, 19, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, removeTrainButton, 3, 19, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             
         }
         
         FocusListener trainIDFocusListener = new FocusListener()
         {
             public void focusLost(FocusEvent event)
             {
                 
             }
             public void focusGained(FocusEvent event)
             {
                 trainIDField.setText("          ");
                 enteredTrainID = "";
                 try
                 {
                     trainIDField.commitEdit();
                     trainIDField.grabFocus();
                 }
                 catch(Exception e)
                 {
 
                 }
             }
         };
         
         ActionListener trainIDFieldListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JFormattedTextField jft = (JFormattedTextField)event.getSource();
                 enteredTrainID = (String)jft.getValue();
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: Train ID entered " + enteredTrainID);
                 }
             }
         };
         
         KeyListener trainIDKeyListener = new KeyListener()
         {
             public void keyReleased(KeyEvent event)
             {
                 JFormattedTextField jft = (JFormattedTextField)event.getSource();
                 enteredTrainID = (String)jft.getText();
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: Train ID entered " + enteredTrainID);
                 }
             }
             public void keyPressed(KeyEvent event)
             {
                 
             }
             public void keyTyped(KeyEvent event)
             {
                 
             }
         };
         
         FocusListener crewFocusListener = new FocusListener()
         {
             public void focusLost(FocusEvent event)
             {
                 
             }
             public void focusGained(FocusEvent event)
             {
                 crewCountField.setText("0");
                 crewCount = 0;
                 try
                 {
                     crewCountField.commitEdit();
                     crewCountField.grabFocus();
                 }
                 catch(Exception e)
                 {
 
                 }
             }
         };
         
         ActionListener crewFieldListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JFormattedTextField jft = (JFormattedTextField)event.getSource();
                 try
                 {
                     crewCount = Integer.parseInt(jft.getValue().toString()); 
                 }
                 catch(Exception e)
                 {
                     crewCount = 0;
                 }
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: Crew Count entered " + crewCount);
                 }
             }
         };
         
         KeyListener crewKeyListener = new KeyListener()
         {
             public void keyReleased(KeyEvent event)
             {
                 JFormattedTextField jft = (JFormattedTextField)event.getSource();
                 try
                 {
                     crewCount = Integer.parseInt(jft.getText().toString());
                 }
                 catch(Exception e)
                 {
                     crewCount = 0;
                 }
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: Crew Count entered " + crewCount);
                 }
             }
             public void keyPressed(KeyEvent event)
             {
                 
             }
             public void keyTyped(KeyEvent event)
             {
                 
             }
         };
         
         ActionListener trainComboListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JComboBox cb = (JComboBox)event.getSource();
                 String id = (String)cb.getSelectedItem();
                 trainSelectedIndex = (int)cb.getSelectedIndex();
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: Train selected " + id);
                 }
                 TrainController tc = sim.getTrainController(id);
                 if(tc != null)
                 {
                     Train t = tc.getTrain();
                     height = t.getHeight();
                     width = t.getWidth();
                     numCars = 1;
                     length = t.getLength();
                     mass = t.getMass();
                     crewCount = t.getCrew();
                     passCount = t.getOccupancy();
                     currentSpeed = t.calcVelocity();
                     currentAcceleration = t.calcAcceleration();
                     message = tc.getMessage();
                     cabinLights = t.getCabinLights();
                     headlights = t.getHeadLights();
                     doors = t.getDoors();
                     brakeFailure = t.getFailures()[0];
                     engineFailure = t.getFailures()[1];
                     signalFailure = t.getFailures()[2];
                     initialize();
                 }
                 else
                 {
                     height = 0;
                     width = 0;
                     numCars = 0;
                     length = 0;
                     mass = 0;
                     crewCount = 0;
                     passCount = 0;
                     currentSpeed = 0;
                     currentAcceleration = 0;
                     message = "";
                     cabinLights = false;
                     headlights = false;
                     doors = false;
                     brakeFailure = false;
                     engineFailure = false;
                     signalFailure = false;
                     initialize();
                 }
             }
         };
         
         ActionListener emergencyBrakeButtonListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 String id = trainIDs[trainSelectedIndex - 1];
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: Emergency Brake Signal sent to Train: " + id);
                 }
                 TrainController t = sim.getTrainController(id);
                 t.setBraking(true);
                 initialize();
             }
         };
                 
         ActionListener addTrainButtonListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: Train created: " + enteredTrainID);
                 }
                 Line l = Line.GREEN;
                 if(line.getSelectedIndex() == 0)
                 {
                     l = Line.GREEN;
                 }
                 else
                 {
                     if(line.getSelectedIndex() == 1)
                     {
                         l = Line.RED;
                     }
                 }
                 if(enteredTrainID != null)
                 {
                     if(sim.getTrainController(enteredTrainID) == null)
                     {
                         sim.createTrain(l, crewCount, clockRate, enteredTrainID);
                         Train t = sim.getTrainController(enteredTrainID).getTrain();
                         crewCount = 0;
                         initialize();
                     }
                     else
                     {
                         JOptionPane.showMessageDialog(TrainPanel.this, "Train ID already in use", "Input Error", JOptionPane.ERROR_MESSAGE);
                     } 
                 }
                 else
                 {
                     JOptionPane.showMessageDialog(TrainPanel.this, "Train ID error, please enter or re-enter the Train ID", "Input Error", JOptionPane.ERROR_MESSAGE);
                 }
             }
         };
                         
         ActionListener removeTrainButtonListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 String id = trainIDs[trainSelectedIndex - 1];
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("CTC View: Train: " + id + " sent remove command");
                 }
                 TrainController tc = sim.getTrainController(id);
                 tc.remove();
                 initialize();
             }
         };
         
         ActionListener brakeFailCheckListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JCheckBox cb = (JCheckBox)event.getSource();
                 if(trainSelectedIndex > 0)
                 {
                     String id = trainIDs[trainSelectedIndex - 1];
                     TrainController tc = sim.getTrainController(id);
                     if(tc != null)
                     {
                         Train t = tc.getTrain();
                         if(cb.isSelected())
                         {
                             t.setFailure(0);
                             if(CTCView.getDebugMode())
                             {
                                 System.out.println("CTC View: Train: " + id + " sent brake failure command");
                             } 
                         }
                         else
                         {
                             t.fixFailure(0);
                             if(CTCView.getDebugMode())
                             {
                                 System.out.println("CTC View: Train: " + id + " sent fix brake failure command");
                             } 
                         }
                     }
                 }
             }
         };
         
         ActionListener engineFailCheckListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JCheckBox cb = (JCheckBox)event.getSource();
                 if(trainSelectedIndex > 0)
                 {
                     String id = trainIDs[trainSelectedIndex - 1];
                     TrainController tc = sim.getTrainController(id);
                     if(tc != null)
                     {
                         Train t = tc.getTrain();
                         if(cb.isSelected())
                         {
                             t.setFailure(1);
                             if(CTCView.getDebugMode())
                             {
                                 System.out.println("CTC View: Train: " + id + " sent brake failure command");
                             } 
                         }
                         else
                         {
                             t.fixFailure(1);
                             if(CTCView.getDebugMode())
                             {
                                 System.out.println("CTC View: Train: " + id + " sent fix brake failure command");
                             } 
                         }
                     }
                 }
             }
         };
                 
         ActionListener signalPickupFailCheckListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JCheckBox cb = (JCheckBox)event.getSource();
                 if(trainSelectedIndex > 0)
                 {
                     String id = trainIDs[trainSelectedIndex - 1];
                     TrainController tc = sim.getTrainController(id);
                     if(tc != null)
                     {
                         Train t = tc.getTrain();
                         if(cb.isSelected())
                         {
                             t.setFailure(2);
                             if(CTCView.getDebugMode())
                             {
                                 System.out.println("CTC View: Train: " + id + " sent brake failure command");
                             } 
                         }
                         else
                         {
                             t.fixFailure(2);
                             if(CTCView.getDebugMode())
                             {
                                 System.out.println("CTC View: Train: " + id + " sent fix brake failure command");
                             } 
                         }
                     }
                 }
             }
         };
         
         public void paintComponent(Graphics g)
         {
             int w = getWidth();
             int h = getHeight();
             g.setColor(Color.WHITE);
             g.fillRect(0,0, w, h);
         }
     }
     
     private class TrackPanel extends JPanel
     {
         private String [] failureTypes = {"Broken Rail", "Power Failure", "Circuit Failure"};
         /*
          * holds the types of track failures 
          */
         private String [] lines = {"Green", "Red"}; // holds the lines in the system
         private JLabel lineLabel = new JLabel("Select Line");   // lables the line combo
         private JLabel controllerLabel = new JLabel("Select Wayside");  // lables the wayside id combo
         private JLabel trackLabel = new JLabel("Select Track"); // labels the track id combo
         private JLabel speedLimitLabel = new JLabel("Speed Limit"); // labels the speed limit for the selected track block
         private JLabel elevationLabel = new JLabel("Elevation");    // labels the elevation for the selected track block
         private JLabel gradeLabel = new JLabel("Grade");    // labels the grade for the selected track block
         private JLabel blockSizeLabel = new JLabel("Block Size");   // labels the block size for the selected track block
         private JLabel trackTypeLabel = new JLabel("Track Type");   // labels the track type for the selected track block
         //private JLabel passengersBoardingLabel = new JLabel("Passengers Boarding");
         /*
          * labels the number of passengers boarding if the bloc is a station
          */
         //private JLabel passengersDisembarkingLabel = new JLabel("Passengers Disembarking");
         /*
          * labels the number of passengers disembarking if the block is a station
          */
         private JLabel openStateLabel = new JLabel("Open State");
         private JLabel failureStateLabel = new JLabel("Failure State");
         private JComboBox line; // holds the lines to be selected
         private JComboBox controller = new JComboBox();   // holds the waysides to be selected
         private JComboBox track = new JComboBox();    // holds the track blocks to be selected
         private JComboBox failType = new JComboBox(failureTypes);   // displays the track block failure types
         private JTextField speedLimitField = new JTextField();  // displays the speed limit for the selected block
         private JTextField elevationField = new JTextField();   // displays the elevation for the selected block
         private JTextField gradeField = new JTextField();   // displays the grade for the selected block
         private JTextField blockSizeField = new JTextField();   // displays the block size for the selected block
         private JTextField trackTypeField = new JTextField();   // displays the track type for the selected block
         //private JTextField passengersBoardingField = new JTextField();  // displays the passengers boarding for the selected block
         //private JTextField passengersDisembarkingField = new JTextField();  // displays the passengers disembarking for the selected block
         private JTextField openStateField = new JTextField();
         private JTextField failureStateField = new JTextField();
         private JButton closeButton = new JButton("Close"); // sends close signal to the selected track block
         private JButton openButton = new JButton("Open");   // sends open signal to the selected track block
         private JButton breakButton = new JButton("Break"); // sends break signal to the selected track block
         private JButton fixButton = new JButton("Fix"); // sends fix signal to the selected track block
         private Insets insets = new Insets(5,20,0,20);  // insets for all display components
         private int speedLimit; // holds the speed limit of the selected track block
         private double elevation;  // holds the elevation of the selected track block
         private double grade;  // holds the grade of the selected track block
         private double blockSize;  // holds the block size of the selected track block
         private String trackType;  // holds the track type of the selected track block
         private String openState; 
         private String failureState; 
         //private int passengersBoarding; // holds the number of passengers boarding of the selected track block
         //private int passengersDisembarking; // holds the number of passengers disembarking of the selected track block
         private String trackIDs[] = new String [0];  // holds the ids of the track blocks in the system
         private String controllerIDs [] = new String [0];    // holds the ids of the controllers in the system
                 
         TrackPanel()
         {
             this.setLayout(new GridBagLayout());
             initialize();
         }
 
         private void initialize()
         {
             int controllerTemp = controllerSelectedIndex;
             int blockTemp = blockSelectedIndex;
             line = new JComboBox();
             Line l;
             
             closeButton.setEnabled(false);
             openButton.setEnabled(false);
             breakButton.setEnabled(false);
             fixButton.setEnabled(false);
             
             for(int i = 0; i < lines.length; i++)
             {
                 line.addItem(lines[i]);
             }
             line.setSelectedIndex(lineSelectedIndex);
             
             switch(lineSelectedIndex)
             {
                 case 0:
                     l = Line.GREEN;
                     break;
                 default:
                     l = Line.RED;
             }
             
             controllerIDs = model.getWaysides(l);
             if(controllerIDs.length != 0)
             {
                 controller.removeAllItems();
             }
             controller.addItem("");
             for(int i = 0; i < controllerIDs.length; i++)
             {
                 controller.addItem(controllerIDs[i]);
             }
             controllerSelectedIndex = controllerTemp;
             controller.setSelectedIndex(controllerSelectedIndex);
              
             trackIDs = model.getTrackIDs(selectedWaysideID);
             if(trackIDs.length != 0)
             {
                 track.removeAllItems();
             }
             for(int i = 0; i < trackIDs.length; i++)
             {
                 track.addItem(trackIDs[i]);
             }
             if(trackIDs.length != 0)
             {
                 blockSelectedIndex = blockTemp;
                 track.setSelectedIndex(blockSelectedIndex);
                 closeButton.setEnabled(true);
                 openButton.setEnabled(true);
                 breakButton.setEnabled(true);
                 fixButton.setEnabled(true);
             }
             
             if(track.getActionListeners().length == 0)
             {
                 track.addActionListener(trackComboListener);
             }
             if(closeButton.getActionListeners().length == 0)
             {
                 closeButton.addActionListener(closeButtonListener);
             }
             if(openButton.getActionListeners().length == 0)
             {
                 openButton.addActionListener(openButtonListener);
             }
             if(breakButton.getActionListeners().length == 0)
             {
                 breakButton.addActionListener(breakButtonListener);
             }
             if(fixButton.getActionListeners().length == 0)
             {
                 fixButton.addActionListener(fixButtonListener);
             }
             if(line.getActionListeners().length == 0)
             {
                 line.addActionListener(lineComboListener);
             }
             if(controller.getActionListeners().length == 0)
             {
                 controller.addActionListener(controllerComboListener);
             }
                         
             speedLimitField.setColumns(10);
             elevationField.setColumns(10);
             gradeField.setColumns(10);
             blockSizeField.setColumns(10);
             trackTypeField.setColumns(10);
             openStateField.setColumns(10);
             failureStateField.setColumns(10);
             //passengersBoardingField.setColumns(10);
             //passengersDisembarkingField.setColumns(10);
             
             speedLimitField.setText(speedLimit + "");
             elevationField.setText(elevation + "");
             gradeField.setText(grade + "");
             blockSizeField.setText(blockSize + "");
             trackTypeField.setText(trackType);
             openStateField.setText(openState + "");
             failureStateField.setText(failureState + "");
             
             speedLimitField.setEditable(false);
             elevationField.setEditable(false);
             gradeField.setEditable(false);
             blockSizeField.setEditable(false);
             trackTypeField.setEditable(false);
             openStateField.setEditable(false);
             failureStateField.setEditable(false);
             //passengersBoardingField.setEditable(false);
             //passengersDisembarkingField.setEditable(false);
             track.setEditable(false);
                         
             addComponent(this, lineLabel, 0, 0, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, line, 1, 0, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.BOTH);
             addComponent(this, controllerLabel, 0, 1, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, controller, 1, 1, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.BOTH);
             addComponent(this, trackLabel, 0, 2, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, track, 1, 2, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.BOTH);
             addComponent(this, speedLimitLabel, 0, 3, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, speedLimitField, 1, 3, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, elevationLabel, 0, 4, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, elevationField, 1, 4, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, gradeLabel, 0, 5, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, gradeField, 1, 5, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, blockSizeLabel, 0, 6, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, blockSizeField, 1, 6, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, trackTypeLabel, 0, 7, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, trackTypeField, 1, 7, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             //addComponent(this, passengersBoardingLabel, 0, 8, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             //addComponent(this, passengersBoardingField, 1, 8, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             //addComponent(this, passengersDisembarkingLabel, 0, 9, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             //addComponent(this, passengersDisembarkingField, 1, 9, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, openStateLabel, 0, 8, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, openStateField, 1, 8, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, failureStateLabel, 0, 9, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, failureStateField, 1, 9, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, closeButton, 0, 10, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, openButton, 0, 11, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, breakButton, 0, 12, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, failType, 1, 12, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.BOTH);
             addComponent(this, fixButton, 0, 13, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
         }
         
         ActionListener lineComboListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JComboBox cb = (JComboBox)event.getSource();
                 lineSelectedIndex = (int)cb.getSelectedIndex();
                 controllerSelectedIndex = 0;
                 blockSelectedIndex = 0;
                 String id = (String)cb.getSelectedItem();
                 if(id.equals("Green Line "))
                 {
                     selectedLine = Line.GREEN;
                 }
                 else
                 {
                     if(id.equals("Red Line"))
                     {
                         selectedLine = Line.RED;
                     }
                 }
                 initialize();
                 if(CTCView.getDebugMode())
                 {
                     System.out.println("Line:  " + id + " selected");
                 }
             }
         };
                 
         ActionListener controllerComboListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JComboBox cb = (JComboBox)event.getSource();
                 controllerSelectedIndex = (int)cb.getSelectedIndex();
                 blockSelectedIndex = 0;
                 if(controllerSelectedIndex > 0)
                 {
                     char id = cb.getSelectedItem().toString().charAt(0);
                     selectedWaysideID = new ID(selectedLine, id, -1);
                     initialize();
                     if(CTCView.getDebugMode())
                     {
                         System.out.println("Track section " + id + " selected");
                     }
                 }
                 else
                 {
                     selectedWaysideID = new ID(selectedLine, ' ', -1);
                     track.removeAllItems();
                     track.addItem("");
                     track.setSelectedIndex(0);
                     trackIDs = new String[0];
                     blockSelectedIndex = 0;
                     initialize();
                 }
             }
         };
         
         ActionListener trackComboListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                 JComboBox cb = (JComboBox)event.getSource();
                 blockSelectedIndex = (int)cb.getSelectedIndex();
                 int trackIDVal = blockSelectedIndex;
                 ID trackselectedID = new ID(selectedLine, selectedWaysideID.getSection(), trackIDVal);
                 Track t = model.getTrack(trackselectedID);
                 if(t != null)
                 {
                     speedLimit = t.getSpeedLimit();
                     elevation = t.getElevation();
                     grade = t.getGrade();  
                     blockSize = t.getBlockLength();
 
                     if(t.isOpen())
                     {
                         openState = "Open";
                     }
                     else
                     {
                         openState = "Closed";
                     }
 
                     switch(t.isFailed())
                     {
                         case BROKEN:
                             failureState = "Broken Rail";
                             break;
                         case POWER:
                             failureState = "Power Failure";
                             break;
                         case CIRCUIT:
                             failureState = "Circuit Failure";
                         default:
                             failureState = "N/A";
                             break;
                     }
 
                     if(t instanceof Station)
                     { 
                         trackType = "Station";  
                     }
                     else
                     {
                         if(t instanceof Crossing)
                         {
                             trackType = "Crossing";
                         }
                         else
                         {
                             if(t instanceof Switch)
                             {
                                 trackType = "Switch";
                             }
                             else
                             {
                                 if(t instanceof Tunnel)
                                 {
                                     trackType = "Tunnel";
                                 }
                                 else
                                 {
                                     trackType = "Track Block";
                                 }
                             }
                         }
                     }
                     initialize();
                     if(CTCView.getDebugMode())
                     {
                         System.out.println("Track " + trackselectedID.getSection() + trackselectedID.getUnit() + " selected");
                     }
                 }
                 else
                 {
                     speedLimit = 0;
                     elevation = 0;
                     grade = 0;
                     blockSize = 0;
                     openState = null;
                     failureState = null;
                 }
             }
             
         };
         
         ActionListener closeButtonListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                if(blockSelectedIndex >= 0)
                 {
                     ID id;
                     if(track.getSelectedItem().toString().length() == 2)
                     {
                         id = new ID(selectedLine, controller.getSelectedItem().toString().charAt(0), Integer.parseInt(track.getSelectedItem().toString().substring(1,2)));
                     }
                     else
                     {
                         id = new ID(selectedLine, controller.getSelectedItem().toString().charAt(0), Integer.parseInt(track.getSelectedItem().toString().substring(1,3)));
                     }
                     Track t = model.getTrack(id);
                     t.setOpen(false);
                     initialize();
                     if(CTCView.getDebugMode())
                     {
                         System.out.println("Track " + id.toString() + " closed");
                     }
                 }
             }
         };
         
         ActionListener openButtonListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                if(blockSelectedIndex >= 0)
                 {
                     ID id;
                     if(track.getSelectedItem().toString().length() == 2)
                     {
                         id = new ID(selectedLine, controller.getSelectedItem().toString().charAt(0), Integer.parseInt(track.getSelectedItem().toString().substring(1,2)));
                     }
                     else
                     {
                         id = new ID(selectedLine, controller.getSelectedItem().toString().charAt(0), Integer.parseInt(track.getSelectedItem().toString().substring(1,3)));
                     }
                     Track t = model.getTrack(id);
                     t.setOpen(true);
                     initialize();
                     if(CTCView.getDebugMode())
                     {
                         System.out.println("Track " + id + " opened");
                     }
                 }
             }
         };
         
         ActionListener breakButtonListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                if(blockSelectedIndex >= 0)
                 {
                     int type = failType.getSelectedIndex();
                     ID id;
                     if(track.getSelectedItem().toString().length() == 2)
                     {
                         id = new ID(selectedLine, controller.getSelectedItem().toString().charAt(0), Integer.parseInt(track.getSelectedItem().toString().substring(1,2)));
                     }
                     else
                     {
                         id = new ID(selectedLine, controller.getSelectedItem().toString().charAt(0), Integer.parseInt(track.getSelectedItem().toString().substring(1,3)));
                     }
                     Track t = model.getTrack(id);
                     switch(type)
                     {
                         case 0:
                             t.setFailure(TrackFault.BROKEN);
                             break;
                         case 1:
                             t.setFailure(TrackFault.POWER);
                             break;
                         case 2:
                             t.setFailure(TrackFault.CIRCUIT);
                             break;
                         default:
                             t.setFailure(TrackFault.NONE);
                     }
                     initialize();
                     if(CTCView.getDebugMode())
                     {
                         System.out.println("Track " + id + " broken: " + type);
                     }
                 }
             }
         };
         
         ActionListener fixButtonListener = new ActionListener()
         {
             public void actionPerformed(ActionEvent event)
             {
                if(blockSelectedIndex >= 0)
                 {
                     ID id;
                     if(track.getSelectedItem().toString().length() == 2)
                     {
                         id = new ID(selectedLine, controller.getSelectedItem().toString().charAt(0), Integer.parseInt(track.getSelectedItem().toString().substring(1,2)));
                     }
                     else
                     {
                         id = new ID(selectedLine, controller.getSelectedItem().toString().charAt(0), Integer.parseInt(track.getSelectedItem().toString().substring(1,3)));
                     }
                     Track t = model.getTrack(id);
                     t.setFix();
                     initialize();
                     if(CTCView.getDebugMode())
                     {
                         System.out.println("Track " + id + " fixed");
                     }
                 }
             }
         };
         
         public void paintComponent(Graphics g)
         {
             int width = getWidth();
             int height = getHeight();
             g.setColor(Color.WHITE);
             g.fillRect(0,0, width, height);
         }
     }
     
     private class MetricsPanel extends JPanel
     {
         private JLabel throughputLabel = new JLabel("Throughput");  // labels the throughput field
         private JLabel capacityLabel = new JLabel("Capacity");  // labels the throughput field
         private JLabel occupancyLabel = new JLabel("Occupancy");    // labels the throughput field
         private JTextField throughputField = new JTextField();  // displays system throughput
         private JTextField capacityField = new JTextField();    // displays system capacity
         private JTextField occupancyField = new JTextField();   // displays the system occupancy
         private Insets insets = new Insets(5,20,0,20);  // insets for all display components
         
         MetricsPanel()
         {
             this.setLayout(new GridBagLayout());
             initialize();
         }
 
         private void initialize()
         {
             throughputField.setColumns(10);
             capacityField.setColumns(10);
             occupancyField.setColumns(10);
             
             throughputField.setEditable(false);
             capacityField.setEditable(false);
             occupancyField.setEditable(false);
             
             throughputField.setText("" + sim.getThroughput());
             capacityField.setText("" + sim.getCapacity());
             occupancyField.setText("" + sim.getOccupancy());
             
             addComponent(this, throughputLabel, 0, 0, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, throughputField, 1, 0, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, capacityLabel, 0, 1, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, capacityField, 1, 1, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
             addComponent(this, occupancyLabel, 0, 2, 1, 1, 0, 0, insets, GridBagConstraints.EAST, GridBagConstraints.NONE);
             addComponent(this, occupancyField, 1, 2, 1, 1, 0, 0, insets, GridBagConstraints.WEST, GridBagConstraints.NONE);
         }
         
         public void paintComponent(Graphics g)
         {
             int width = getWidth();
             int height = getHeight();
             g.setColor(Color.WHITE);
             g.fillRect(0,0, width, height);
         }
     }
     
     private class SplashPanel extends JPanel
     {
         
         public void paintComponent(Graphics g)
         {
             int width = getWidth();
             int height = getHeight();
             Font f = new Font(Font.MONOSPACED, Font.BOLD, 30);
             g.setColor(Color.WHITE);
             g.fillRect(0,0, width, height);
             g.setColor(Color.BLACK);
             g.setFont(f);
             g.drawString("Alpha Train Simulator", width/4, height/3);
         }
     }
     
     private void addComponent(Container c, Component comp, int gridx, int gridy, int gridwidth, int gridheight, double weightx, double weighty, Insets i, int anchor, int fill)
     {
         GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, i, 0, 0);
         c.add(comp, gbc);
     }
     
     /**
      * returns the clock rate
      * 
      * @return integer clock rate
      */
     public int getClockRate()
     {
         return clockRate;
     }
     
     /**
      * returns demo mode flag
      * @return boolean demo mode flag
      */
     public boolean getDemo()
     {
         return demoMode;
     }
 
     /**
      * returns GUI open flag
      * 
      * @return boolean GUI open flag
      */
     public boolean getIsOpen()
     {
         return isOpen;
     }
     
     /**
      * sets reference for GUI's simulator
      * 
      * @param s Simulator object referenced by the GUI
      */
     public void setSimulator(Simulator s)
     {
         sim = s;
     }
     
 }
