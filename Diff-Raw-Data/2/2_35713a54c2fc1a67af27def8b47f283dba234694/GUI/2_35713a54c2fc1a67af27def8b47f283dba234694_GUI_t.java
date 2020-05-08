 /*
  * TODO: Courseview, addcourseview, fix weekView and
  * week scrolling add save-method and add-event method
  * 
  *
  */
 package dealwithcalendar;
 
 import javax.swing.*;
 import javax.imageio.ImageIO;
 
 import java.io.*;
 import java.util.*;
 
 
 import java.awt.event.*;
 import java.awt.Component.*;
 import java.awt.GridLayout;
 import java.awt.FlowLayout;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Insets;
 import java.awt.Font;
 import java.awt.FileDialog;
 import java.awt.Dimension;
 
 public class GUI extends JFrame
                             implements ActionListener {
 
      Main m; // main program the GUI uses
      HashMap<Integer, Course> crs; // courses the GUI uses
      ArrayList<Event> weekEvents; // week events kept updated by curWeek and curYear
 
      // current event shown in mainRight's eventProperties panel
      private Event curEvent = null;
      // current event shown in eventWindow and to be added by clicking addEvent
      private Event addE = null;
      // Year in which the week view is atm.
      private int curYear = Calendar.getInstance().get(Calendar.YEAR);
      // week in which the week view is atm
      private int curWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
 
      private static final int WEEKDAYS = 7;
      private static final int HOURS = 24;
      private static final int MONTHS = 12;
      private static final Color THEME_COLOR_VDARKBLUE = new Color(100,125,150);
      private static final Color THEME_COLOR_DARKBLUE = new Color(100,125,150, 200);
      private static final Color THEME_COLOR_BLUE = new Color(100,125,150, 100);
      private static final Color THEME_COLOR_LIGHTBLUE = new Color(100,125,150, 50);
      private static final Color THEME_COLOR_VLIGHTBLUE = new Color(100,125,150, 0);
      private static final Color THEME_COLOR_OTHER = new Color(150,125,100, 150);
      private static final Color THEME_COLOR_COTHER = new Color(200,125, 50, 150);
      private static final Color THEME_COLOR_CTEST = new Color(200,125,150, 200);
      private static final Color THEME_COLOR_CSTUDYG = new Color(100,150,150, 100);
      private static final Color THEME_COLOR_CLECTURE = new Color(50,125,125, 150);
      private static final Color THEME_COLOR_CGUIDG = new Color(25,175,100, 100);
      private static final Font THEME_FONT_SMALL = new Font("sansserif", Font.BOLD, 12);
 
      // calendar view's hour buttons for each day
      private JButton[][] calendarButtons;
      /*  map calendarButton's indexes to weekEvent's indexes
       *  ie. calendarButton[i][j] refers to weekEvents.get(calendarEvents[i][j])
       * if calendarButton[i][j] doesn't represent any event then calendarEvents[i][j] = -1. */
      private int[][] calendarEvents;
 
      // general button for constructing calendarButtons
      private JButton b;
 
      // buttons for scrolling weeks
      private JButton bPrev = new JButton("<<");
      private JButton bNext = new JButton(">>"); 
 
      // General menubar
      private JMenuBar menuBar = new JMenuBar();
 
      // calendar menu and it's items
      private JMenu Menu = new JMenu("Valikko");
      private JMenuItem weekView = new JMenuItem("Viikkonäkymään");
      private JMenuItem monthView = new JMenuItem("Kuukausinäkymään");
      private JMenuItem eventView = new JMenuItem("Lisää tapahtuma");
      private JMenuItem saveWeek = new JMenuItem("Tallenna viikko");
      private JMenuItem quit = new JMenuItem("Poistu");
         // courses is submenu in main menu
      private JMenu courses = new JMenu("Kurssit");
      private JMenuItem CoursesView = new JMenuItem("Selaa kursseja");
      private JMenuItem addCourseView = new JMenuItem("Lisää kurssi");
 
      
      // weekNumber indicator
      JTextField weekNumber = new JTextField(curYear + "VIIKKO " + curWeek);
      
      String[] dc; // Course names for pickCourse combobox
      private int[] comboToCourseID; // maps pickCourse combobox's indexes to courseID's;
 
 
      // some hard coded shit (list's) down here
 
 
      private String eventEmpty ="\t \n" +
                                 "Päivämäärä:   \t \n" +
                                 "Kello: - \n" +
                                 "Paikka: \n" +
                                 "Omat merkinnät:";
 
      // list for JComboBox courseEventDays
      private String[] dayNames = {  "Sunnuntai",
                                     "Maanantai",
                                     "Tiistai",
                                     "Keskiviikko",
                                     "Torstai",
                                     "Perjantai",
                                     "Lauantai"};
 
      // list for JComboBox courseEventSTime and courseEventETime
      // also for eeTime and esTime
      private String[] hrs = {   "0:00", "1:00", "2:00", "3:00", "4:00", "5:00",
                                 "6:00", "7:00", "8:00", "9:00", "10:00", "11:00",
                                 "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
                                 "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
 
      // list for JComboBox courseEventType
      private String[] crsEvents = {"Luento", "Laskuharjoitus", "Ohjausryhmä", "Muu"};
 
      // list for JComboBox courseEventTestMonths
      private String[] months = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
 
      // list for JComboBox courseEventTestDays
      private String[] monthdays = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                                    "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                                    "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
 
      // list for JComboBox courseEventTestYears
      private String[] years = new String[2];
 
 
    
     
      // DIFFERENT PANEL'S, BOXES ETC. FOR CONSTRUCTING UI
 
      // week view
 
      // text area and panel for showing event info details in week view's mainRight
      private JButton saveEventMarkings = new JButton("Tallenna merkinnät");
      private JTextArea eventProperties = new JTextArea(eventEmpty, 5, 30);
      private JTextArea eventOwnMarkings = new JTextArea("", 7, 30);
      private JPanel eventInfo = new JPanel(new BorderLayout());
 
      // event buttons in week view's mainRight
      private JButton addEvent = new JButton("Lisää / muuta tapahtuman tiedot");
      private JButton removeEvent = new JButton("Poista tapahtuma");
      private JButton alterEvent = new JButton("Muokkaa tapahtumaa");
      private JPanel eventButtons = new JPanel(new GridLayout(3,1));
 
      // whole calendar to store calendarScrollPane and weekScroll
      private JPanel calendarWhole;
      private Insets margins = new Insets(0,0,0,0); // insets for calendarButtons
      JScrollPane calendarScrollPane;
      private JPanel weekScroll = new JPanel(new BorderLayout(1,3));
 
      // main UI contianers
      private JPanel upperLeftUI = new JPanel(new BorderLayout());     
      private JPanel mainLeft = new JPanel(new BorderLayout());
      private JPanel rightUIPanel = new JPanel(new BorderLayout());
      private JPanel menuPanel = new JPanel(new BorderLayout());
      private JPanel mainUI = new JPanel(new FlowLayout());
      private JPanel mainRight = new JPanel(new BorderLayout());
      private JPanel wholeGUI = new JPanel(new BorderLayout());
 
      // courses view
      private JComboBox pickDay = new JComboBox(dayNames);
      private JComboBox pickHour = new JComboBox(hrs);
      private JComboBox pickCourseEvent = new JComboBox(crsEvents);
      private JTextField insertPlace = new JTextField("lisää paikka");
      private JCheckBox insertToCalendar;
      private JComboBox pickTestDay = new JComboBox(monthdays);
      private JComboBox pickTestMonth = new JComboBox(months);
      private JComboBox pickTestYear = new JComboBox(years);
      private JComboBox pickCourse = new JComboBox();
      private JButton addSelectedEvents = new JButton ("Lisää valitut tapahtumat kalenteriin");
      private JPanel courseViewMain = new JPanel(new BorderLayout());
      private JPanel courseViewUpper = new JPanel(new FlowLayout());
      private JPanel courseInfo = new JPanel(new BorderLayout());
      private JTextField courseNickname = new JTextField("");
      private JTextField cn = new JTextField("Anna kurssille nikki: ");
      private JTextField selectedCourseInfo = new JTextField("");
      private JPanel courseEventGrid = new JPanel(new GridLayout(8,1));
      private JPanel courseEvent = new JPanel(new FlowLayout());
      private JPanel courseViewLower = new JPanel(new BorderLayout());
      private JPanel courseNick = new JPanel(new FlowLayout());
      private JComboBox[] courseEventType = new JComboBox[8];
      private JComboBox[] courseEventDays = new JComboBox[6];
      private JComboBox[] courseEventTestDays = new JComboBox[2];
      private JComboBox[] courseEventTestMonths = new JComboBox[2];
      private JComboBox[] courseEventTestYears = new JComboBox[2];
      private JComboBox[] courseEventSTime = new JComboBox[8];
      private JComboBox[] courseEventETime = new JComboBox[8];
      private JTextField[] courseEventLoc = new JTextField[8];
      private JCheckBox[] courseEventSel = new JCheckBox[8];
 
      // add / change event view
      JFrame eventWindow;
      JPanel Event;
     JTextField eName;
     JTextField ePlace;
     JTextArea eOM;
     JComboBox eYear;
     JComboBox eMonth;
     JComboBox eDay;
     JComboBox eeTime;
     JComboBox esTime;
 
     // add course event view
     JFrame addCourseWindow;
     JPanel Course;
     JTextField crsName;
     JComboBox crsSYear;
     JComboBox crsSMonth;
     JComboBox crsSDay;
     JComboBox crsEYear;
     JComboBox crsEMonth;
     JComboBox crsEDay;
     JButton addCourse = new JButton("Lisää kurssi");
 
     
     /**
      * Constructor to create GUI for calendar
      */
     public GUI(Main main) {
 
         for (int i = 0; i < 2; i++) {
             years[i] = String.valueOf(curYear +i);
         }
         
         m = main;
         crs = m.getCourses();
         mapCourses();
         weekEvents = m.getWeek(curYear, curWeek);
 
         addEvent.addActionListener(this);
         addCourse.addActionListener(this);
 
         UIManager.put("Button.disabledText", Color.WHITE);
 
         // CONSTRUCT STANDARD WEEK VIEW
 
         // construct weekNumber marker and calendarButtons for standard week view
         weekNumber.setFont(new Font("sansserif", Font.BOLD, 25));
         weekNumber.setBackground(THEME_COLOR_DARKBLUE);
         weekNumber.setHorizontalAlignment(0);
         weekNumber.setEditable(false);
         bPrev.setFont(new Font("sansserif", Font.BOLD, 25));
         bPrev.setBackground(THEME_COLOR_DARKBLUE);
         bPrev.addActionListener(this);
         bNext.setFont(new Font("sansserif", Font.BOLD, 25));
         bNext.setBackground(THEME_COLOR_DARKBLUE);
         bNext.addActionListener(this);
         
         calendarWhole = new JPanel(new GridLayout(HOURS,WEEKDAYS));
         calendarButtons = new JButton[HOURS][WEEKDAYS];
         calendarEvents = new int[HOURS][WEEKDAYS];
         // make calendar view scrollable (not necessary?)
         calendarScrollPane = new JScrollPane(calendarWhole);
         calendarScrollPane.setPreferredSize(new Dimension(650, 400));
 
         weekScroll.add("West", bPrev);
         weekScroll.add(weekNumber);
         weekScroll.add("East", bNext);
         upperLeftUI.add("North", weekScroll);
 
         // create standard week view, map JButtons and events into arrays
         for (int i = 0; i < HOURS; i++) {
             for (int j = 0; j < WEEKDAYS; j++) {
                b = new JButton("   ");
                b.setBackground(THEME_COLOR_VLIGHTBLUE);
                b.setFont(new Font("sansserif", Font.PLAIN, 10));
                b.setMargin(margins);
                b.setPreferredSize(new Dimension(80,15));
                // Testing a fix for button highlighting!
                b.setRolloverEnabled(false);
                b.addActionListener(this);
 
                calendarEvents[i][j] = -1;
                calendarButtons[i][j] = b;
                calendarWhole.add(b);
             }
         }
        
 
         // add week view as default view into GUI.mainLeft
         mainLeft.add("North", upperLeftUI);
         mainLeft.add("South", calendarScrollPane);
 
         // construct container for event info's
         eventProperties.setFont(THEME_FONT_SMALL);
         eventProperties.setBackground(THEME_COLOR_BLUE);
         eventProperties.setForeground(new Color(0,0,0));
         eventProperties.setEditable(false);
         eventProperties.setLineWrap(true);
         eventProperties.setWrapStyleWord(true);
 
         saveEventMarkings.setFont(new Font("sansserif", Font.BOLD, 15));
         saveEventMarkings.setBackground(THEME_COLOR_VDARKBLUE);
         saveEventMarkings.setForeground(new Color(0,0,0));
         saveEventMarkings.addActionListener(this);
 
         removeEvent.setFont(new Font("sansserif", Font.BOLD, 15));
         removeEvent.setBackground(THEME_COLOR_VDARKBLUE);
         removeEvent.setForeground(new Color(0,0,0));
         removeEvent.addActionListener(this);
 
         alterEvent.setFont(new Font("sansserif", Font.BOLD, 15));
         alterEvent.setBackground(THEME_COLOR_VDARKBLUE);
         alterEvent.setForeground(new Color(0,0,0));
         alterEvent.addActionListener(this);
 
         eventButtons.add(saveEventMarkings);
         eventButtons.add(alterEvent);
         eventButtons.add(removeEvent);
 
         eventInfo.add("North", eventProperties);
         eventInfo.add(eventOwnMarkings);
         eventInfo.add("South", eventButtons);
 
         // construct default view of right side of UI
         rightUIPanel.add("North", eventInfo);
         mainRight.add("North", rightUIPanel);
 
         mainUI.add(mainLeft);
         mainUI.add(mainRight);
 
         // CONSTRUCT COURSES VIEW
 
         // FIXME: get all courses in here map them to some array
         pickCourse = new JComboBox(dc);
         pickCourse.setFont(THEME_FONT_SMALL);
         pickCourse.setBackground(THEME_COLOR_VDARKBLUE);
         pickCourse.setForeground(new Color(0,0,0));
         pickCourse.addActionListener(this);
 
         // generate general course info into courseinfo JPanel here
         
         selectedCourseInfo.setText("");
         selectedCourseInfo.setEditable(false);
         selectedCourseInfo.setPreferredSize(new Dimension(200, 60));
         selectedCourseInfo.setFont(THEME_FONT_SMALL);
         selectedCourseInfo.setBackground(THEME_COLOR_VDARKBLUE);
         selectedCourseInfo.setForeground(new Color(0,0,0));
         
         courseNickname.setText("");
         courseNickname.setEditable(true);
         courseNickname.setPreferredSize(new Dimension(200, 20));
         cn.setFont(THEME_FONT_SMALL);
         cn.setBackground(THEME_COLOR_VLIGHTBLUE);
         cn.setEditable(false);
         courseNick.add(cn);
         courseNick.add(courseNickname);
 
         courseInfo.add("North", selectedCourseInfo);
         courseInfo.add("South", courseNick);
 
         // generate course event properties' adding 
         for (int i=0; i < 6; i++) {
             courseEvent = new JPanel(new FlowLayout());
             courseEvent.setFont(THEME_FONT_SMALL);
             courseEvent.setBackground(THEME_COLOR_BLUE);
             courseEvent.setForeground(new Color(0,0,0));
 
             pickCourseEvent = new JComboBox(crsEvents);
             pickCourseEvent.setPreferredSize(new Dimension(120,20));
             pickCourseEvent.setFont(THEME_FONT_SMALL);
             pickCourseEvent.setBackground(THEME_COLOR_VDARKBLUE);
             pickCourseEvent.setForeground(new Color(0,0,0));
             courseEventType[i] = pickCourseEvent;
             courseEvent.add(pickCourseEvent);
 
             pickDay = new JComboBox(dayNames);
             pickDay.setPreferredSize(new Dimension(110,20));
             pickDay.setFont(THEME_FONT_SMALL);
             pickDay.setBackground(THEME_COLOR_VDARKBLUE);
             pickDay.setForeground(new Color(0,0,0));
             pickDay.setSelectedIndex(1);
             courseEventDays[i] = pickDay;
             courseEvent.add(pickDay);
 
             pickHour = new JComboBox(hrs);
             pickHour.setPreferredSize(new Dimension(70,20));
             pickHour.setFont(THEME_FONT_SMALL);
             pickHour.setBackground(THEME_COLOR_VDARKBLUE);
             pickHour.setForeground(new Color(0,0,0));
             pickHour.setSelectedIndex(12);
             courseEventSTime[i] = pickHour;
             courseEvent.add(pickHour);
 
             pickHour = new JComboBox(hrs);
             pickHour.setPreferredSize(new Dimension(70,20));
             pickHour.setFont(THEME_FONT_SMALL);
             pickHour.setBackground(THEME_COLOR_VDARKBLUE);
             pickHour.setForeground(new Color(0,0,0));
             pickHour.setSelectedIndex(12);
             courseEventETime[i] = pickHour;
             courseEvent.add(pickHour);
 
             insertPlace = new JTextField("lisää paikka");
             insertPlace.setPreferredSize(new Dimension(120,20));
             insertPlace.setFont(new Font("sansserif", Font.PLAIN, 12));
             insertPlace.setForeground(new Color(0,0,0));
             insertPlace.setEditable(true);
             courseEventLoc[i] = insertPlace;
             courseEvent.add(insertPlace);
 
             insertToCalendar = new JCheckBox("kalenteriin");
             insertToCalendar.setPreferredSize(new Dimension(130,20));
             insertToCalendar.setFont(new Font("sansserif", Font.PLAIN, 12));
             insertToCalendar.setBackground(new Color(100,125,150,0));
             insertToCalendar.setOpaque(true);
             insertToCalendar.setForeground(new Color(0,0,0));
             courseEventSel[i] = insertToCalendar;
             courseEvent.add(insertToCalendar);
 
             courseEventGrid.add(courseEvent);
         }
 
         for (int i=0; i < 2; i++) {
             courseEvent = new JPanel(new FlowLayout());
             courseEvent.setFont(THEME_FONT_SMALL);
             courseEvent.setBackground(THEME_COLOR_BLUE);
             courseEvent.setForeground(new Color(0,0,0));
 
             insertPlace = new JTextField("Tentti");
             insertPlace.setFont(THEME_FONT_SMALL);
             insertPlace.setBackground(THEME_COLOR_VLIGHTBLUE);
             insertPlace.setEditable(false);
             courseEvent.add(insertPlace);
 
             pickTestDay = new JComboBox(monthdays);
             pickTestDay.setPreferredSize(new Dimension(50,20));
             pickTestDay.setFont(THEME_FONT_SMALL);
             pickTestDay.setBackground(THEME_COLOR_VDARKBLUE);
             pickTestDay.setForeground(new Color(0,0,0));
             courseEventTestDays[i] = pickTestDay;
             courseEvent.add(pickTestDay);
 
             pickTestMonth = new JComboBox(months);
             pickTestMonth.setPreferredSize(new Dimension(50,20));
             pickTestMonth.setFont(THEME_FONT_SMALL);
             pickTestMonth.setBackground(THEME_COLOR_VDARKBLUE);
             pickTestMonth.setForeground(new Color(0,0,0));
             courseEventTestMonths[i] = pickTestMonth;
             courseEvent.add(pickTestMonth);
 
             pickTestYear = new JComboBox(years);
             pickTestYear.setPreferredSize(new Dimension(70,20));
             pickTestYear.setFont(THEME_FONT_SMALL);
             pickTestYear.setBackground(THEME_COLOR_VDARKBLUE);
             pickTestYear.setForeground(new Color(0,0,0));
             courseEventTestYears[i] = pickTestYear;
             courseEvent.add(pickTestYear);
 
             pickHour = new JComboBox(hrs);
             pickHour.setPreferredSize(new Dimension(70,20));
             pickHour.setFont(THEME_FONT_SMALL);
             pickHour.setBackground(THEME_COLOR_VDARKBLUE);
             pickHour.setForeground(new Color(0,0,0));
             pickHour.setSelectedIndex(12);
             courseEventSTime[i+6] = pickHour;
             courseEvent.add(pickHour);
 
             pickHour = new JComboBox(hrs);
             pickHour.setPreferredSize(new Dimension(70,20));
             pickHour.setFont(THEME_FONT_SMALL);
             pickHour.setBackground(THEME_COLOR_VDARKBLUE);
             pickHour.setForeground(new Color(0,0,0));
             pickHour.setSelectedIndex(12);
             courseEventETime[i+6] = pickHour;
             courseEvent.add(pickHour);
 
             insertPlace = new JTextField("lisää paikka");
             insertPlace.setPreferredSize(new Dimension(120,20));
             insertPlace.setFont(new Font("sansserif", Font.PLAIN, 12));
             insertPlace.setBackground(new Color(255,255,255));
             insertPlace.setForeground(new Color(0,0,0));
             insertPlace.setEditable(true);
             courseEventLoc[i+6] = insertPlace;
             courseEvent.add(insertPlace);
 
             insertToCalendar = new JCheckBox("kalenteriin");
             insertToCalendar.setPreferredSize(new Dimension(125,20));
             insertToCalendar.setFont(new Font("sansserif", Font.PLAIN, 12));
             insertToCalendar.setBackground(new Color(100,125,150,0));
             insertToCalendar.setOpaque(true);
             insertToCalendar.setForeground(new Color(0,0,0));
             courseEventSel[i+6] = insertToCalendar;
             courseEvent.add(insertToCalendar);
 
             courseEventGrid.add(courseEvent);
         }
       
         addSelectedEvents.setFont(new Font("sansserif", Font.PLAIN, 20));
         addSelectedEvents.setBackground(THEME_COLOR_VDARKBLUE);
         addSelectedEvents.setOpaque(true);
         addSelectedEvents.setForeground(new Color(0,0,0));
         addSelectedEvents.addActionListener(this);
 
         courseViewUpper.add("West", pickCourse);
         courseViewLower.add("North", courseInfo);
         courseViewLower.add(courseEventGrid);
         courseViewLower.add("South", addSelectedEvents);
         courseViewMain.add(courseViewLower);
         courseViewMain.add("North", courseViewUpper);
         //courseViewMain.setPreferredSize(new Dimension(650, 400));
         courseViewMain.setBackground(THEME_COLOR_VLIGHTBLUE);
         courseViewMain.setFont(new Font("sansserif", Font.PLAIN, 13));
 
        
         // GENERATE JMENUBAR AND IT'S MENUS        
         
         menuBar.setBackground(THEME_COLOR_BLUE);
         menuBar.add(Menu);
         
         // game option menu
 
         Menu.setFont(new Font("sansserif", Font.BOLD, 13));
         Menu.setMnemonic(KeyEvent.VK_V);
         Menu.setForeground(THEME_COLOR_VDARKBLUE);
 
         courses.setBackground(THEME_COLOR_VDARKBLUE);
         courses.setForeground(THEME_COLOR_VDARKBLUE);
         courses.setMnemonic(KeyEvent.VK_K);
         Menu.add(courses);
 
             // courses submenu
 
             CoursesView.setBackground(THEME_COLOR_VDARKBLUE);
             CoursesView.setForeground(Color.DARK_GRAY);
             CoursesView.addActionListener(this);
             courses.add(CoursesView);
 
             addCourseView.setBackground(THEME_COLOR_VDARKBLUE);
             addCourseView.setForeground(Color.DARK_GRAY);
             addCourseView.addActionListener(this);
             courses.add(addCourseView);
 
         weekView.setBackground(THEME_COLOR_VDARKBLUE);
         weekView.setForeground(Color.DARK_GRAY);
         weekView.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_V, ActionEvent.CTRL_MASK));
         weekView.addActionListener(this);
         Menu.add(weekView);
 
         monthView.setBackground(THEME_COLOR_VDARKBLUE);
         monthView.setForeground(Color.DARK_GRAY);
         monthView.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_K, ActionEvent.CTRL_MASK));
         monthView.addActionListener(this);
         Menu.add(monthView);
 
         eventView.setBackground(THEME_COLOR_VDARKBLUE);
         eventView.setForeground(Color.DARK_GRAY);
         eventView.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_E, ActionEvent.CTRL_MASK));
         eventView.addActionListener(this);
         Menu.add(eventView);
 
         saveWeek.setBackground(THEME_COLOR_VDARKBLUE);
         saveWeek.setForeground(Color.DARK_GRAY);
         saveWeek.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_S, ActionEvent.CTRL_MASK));
         saveWeek.addActionListener(this);
         Menu.add(saveWeek);
 
         quit.setBackground(THEME_COLOR_VDARKBLUE);
         quit.setForeground(Color.DARK_GRAY);
         quit.setAccelerator(KeyStroke.getKeyStroke(
         KeyEvent.VK_P, ActionEvent.CTRL_MASK));
         quit.addActionListener(this);
         Menu.add(quit);
 
         // GENERATE WHOLE GUI
 
         // create standard week view in here, get current week's events
         createWeekView(curYear, curWeek);
 
         menuPanel.add("North", menuBar);
 
         add(wholeGUI);
         wholeGUI.add("North", menuPanel);
         wholeGUI.add("South", mainUI);
 
         setTitle("OpKuppa");
         setResizable(false);
         //setPreferredSize(new Dimension(900, 500));
         pack();
         this.setLocationRelativeTo(null); //Center the window
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         try
 		{	//set the logo as icon
 			this.setIconImage(ImageIO.read(new File("icon.jpg")));
 		} catch (IOException ex)
 		{	//advanced error handling
 			System.err.println("No icon found, deal with it");
 		}
 
         setVisible(true);
 
     }
 
    
 
 
     /**
     * Action listener for GUI. Handles all actions.
     *
     * @param act ActionEvent to which reaction is needed
     */ 
    public void actionPerformed(ActionEvent act) {
 
         Object source = act.getSource();
 
         // menu and main functions
         if (source == quit) exit();
         if (source == weekView) createWeekView(curYear, curWeek);
         if (source == monthView) openSaveFileDialog();
         if (source == eventView) openEventWindow(0, 0);
         if (source == CoursesView) createCoursesView();
         if (source == addCourseView) createAddCourseWindow();
         if (source == saveWeek) openSaveFileDialog();
 
         // week view
         if (source == bPrev) createWeekView(curYear, curWeek -1) ;
         if (source == bNext) createWeekView(curYear, curWeek +1);
         if (source == saveEventMarkings) saveOwnMarkings();
         if (source == alterEvent) alterEvent();
         if (source == removeEvent) removeEvent();
                 
         for (byte i = 0; i < HOURS ; i++) {
             for (byte j = 0; j < WEEKDAYS; j++) {
                 if (source == calendarButtons[i][j]) {
                     if (calendarEvents[i][j] >= 0)
                         updateEventInfo(weekEvents.get(calendarEvents[i][j]));
                     else { openEventWindow(i, j); }
                 }
             }
         }
 
         // course view
         if (source == addSelectedEvents) addCourseEvents();
         if (source == pickCourse) updateCourseInformation();
 
         // eventWindow
         if (source==addEvent) addEventToCalendar();
 
         // addCourseWindow
         if (source==addCourse) addCourseToCourses();
 
     }
 
     public void saveOwnMarkings() {
         if (curEvent != null)
             m.changeEventOwnMarkings(curEvent, eventOwnMarkings.getText());
     }
 
     public void mapCourses() {
         dc = new String[crs.size()];
         comboToCourseID = new int[crs.size()];
         int i = 0;
 
         Iterator<Course> ci = crs.values().iterator();
 
         while (ci.hasNext()) {
             Course c = ci.next();
             dc[i] = c.getName();
             comboToCourseID[i] = c.getId();
             i++;
         }
     }
 
     public void createWeekView(int cy, int cw) {
         if (cw == 0) {
             cw = 53;
             cy--;
         }
         if (cw == 54) {
             cw = 1;
             cy++;
         }
         curYear = cy;
         curWeek = cw;
         weekEvents = m.getWeek(cy,cw);
 
         mainLeft.removeAll();
         mainRight.removeAll();
         weekNumber.setText(cy + " VIIKKO " + cw);
         int eventDay = 0;
         int eventStart = 0;
         int eventEnd = 0;
 
         for (int i = 0; i < HOURS; i++) {
             for (int j = 0; j < WEEKDAYS; j++) {
                calendarButtons[i][j].setText("    ");
                calendarButtons[i][j].setBackground(THEME_COLOR_VLIGHTBLUE);
                calendarEvents[i][j] = -1;
             }
         }
 
         if (weekEvents != null) {
             System.out.println(weekEvents.size());
 
             for (int i = 0; i < weekEvents.size(); i++) {
                 Event e = weekEvents.get(i);
                 eventDay = e.getStarttime().get(Calendar.DAY_OF_WEEK);
                 if (eventDay == 1) eventDay = 6;
                 else eventDay = eventDay-2;
                 eventStart = e.getStarttime().get(Calendar.HOUR_OF_DAY);
                 eventEnd = e.getEndtime().get(Calendar.HOUR_OF_DAY);
 
                 for (int j = eventStart; j < eventEnd; j++) {
                     calendarButtons[j][eventDay].setText(e.getName() + " : " + e.getLocation());
                     setButtonBg(j, eventDay, e);
                     calendarEvents[j][eventDay] = i;
                 }
             }
         }
         upperLeftUI.removeAll();
         upperLeftUI.add("North", weekScroll);
         mainRight.add("North", rightUIPanel);
         mainLeft.add("North", upperLeftUI);
         mainLeft.add("South", calendarScrollPane);
         repaint();
     }
 
     /* Changes mainLeft container's content to basic course view where one
      * can change course's lecture, studygroup and test times.
      */
     public void createCoursesView() {
         mainRight.removeAll();
         mainLeft.removeAll();
         mainLeft.add(courseViewMain);
 
         mainRight.validate();
         mainLeft.validate();
         repaint();
     }
 
     public void setCourseEventGridToDefault() {
 
         for (int i = 0; i < courseEventType.length-2; i++) {
             ((JComboBox)courseEventType[i]).setSelectedIndex(0);
             ((JComboBox)courseEventDays[i]).setSelectedIndex(0);
             ((JComboBox)courseEventSTime[i]).setSelectedIndex(12);
             ((JComboBox)courseEventETime[i]).setSelectedIndex(12);
             ((JTextField)courseEventLoc[i]).setText("lisää paikka");
             ((JCheckBox)courseEventSel[i]).setSelected(false);
         }
         for (int i = 0; i < 2; i++) {
             ((JComboBox)courseEventTestDays[i]).setSelectedIndex(0);
             ((JComboBox)courseEventTestMonths[i]).setSelectedIndex(0);
             ((JComboBox)courseEventTestYears[i]).setSelectedIndex(0);
             ((JComboBox)courseEventSTime[i]).setSelectedIndex(12);
             ((JComboBox)courseEventETime[i]).setSelectedIndex(12);
             ((JTextField)courseEventLoc[i]).setText("lisää paikka");
             ((JCheckBox)courseEventSel[i]).setSelected(false);
         }
     }
 
     public void updateCourseInformation() {
         int courseid = comboToCourseID[pickCourse.getSelectedIndex()];
         updateCourseGeneralInfo(courseid);
         updateCourseEvents(courseid);
     }
 
     public String parseDate(Calendar c) {
         String d = c.get(Calendar.DAY_OF_MONTH) + "." +
                    (c.get(Calendar.MONTH)+1) + "." +
                    c.get(Calendar.YEAR);
 
         return d;
     }
 
     public void updateCourseGeneralInfo(int courseid) {
         Course c = m.getACourse(courseid);
         String info = c.getName() + "\n " +
                       parseDate(c.getStart()) + " - " +
                       parseDate(c.getEnd()) + "\n";
 
         selectedCourseInfo.setText(info);
         selectedCourseInfo.validate();
         courseNickname.setText(c.getNickname());
         courseInfo.validate();
         courseInfo.repaint();
     }
 
     public void updateCourseEvents(int courseid) {
         // display all course events of selected course
         // in courseEventGrid
 
         setCourseEventGridToDefault();
 
         ArrayList<courseEvent> ce = m.getACourse(courseid).getCourseEvents();
         int tn = 0;
 
         for (int i = 0; i < ce.size(); i++) {
             courseEvent e = ce.get(i);
             if (e.getType() != 3) {
                 courseEventType[i].setSelectedIndex(e.getType());
                 courseEventDays[i].setSelectedIndex(e.getWeekday()-1);
                 courseEventSTime[i].setSelectedIndex(e.getTime().get(Calendar.HOUR_OF_DAY));
                 courseEventETime[i].setSelectedIndex(e.getTime().get(Calendar.HOUR_OF_DAY) + (ce.get(i).getDuration()/60));
                 courseEventLoc[i].setText(e.getLocation());
                 courseEventSel[i].setSelected(true);
             }
             else {
                 courseEventTestDays[tn].setSelectedIndex(e.getTime().get(Calendar.DAY_OF_MONTH) -1);
                 courseEventTestMonths[tn].setSelectedIndex(e.getTime().get(Calendar.MONTH));
                 courseEventTestYears[tn].setSelectedIndex(e.getTime().get(Calendar.YEAR)- Calendar.getInstance().get(Calendar.YEAR));
                 courseEventSTime[6+tn].setSelectedIndex(e.getTime().get(Calendar.HOUR_OF_DAY));
                 courseEventETime[6+tn].setSelectedIndex(e.getTime().get(Calendar.HOUR_OF_DAY) + (ce.get(i).getDuration()/60));
                 courseEventLoc[6+tn].setText(e.getLocation());
                 courseEventSel[6+tn].setSelected(true);
                 tn++;
             }
         }
         courseEventGrid.validate();
         courseEventGrid.repaint();
     }
 
     public void alterEvent() {
         if (curEvent == null)
             return;
         else createEventWindow(curEvent);
     }
 
     public void removeEvent() {
         m.removeEvent(curEvent);
         curEvent = null;
 
         createWeekView(curYear, curWeek);
     }
 
     public void addCourseEvents() {
         int courseid = comboToCourseID[pickCourse.getSelectedIndex()];
         if (courseid < 0) return;
 
         m.deleteCourseEvents(courseid);
         // remove all previous course events from this course
         // remove all events from this course from calendar
 
         m.getACourse(courseid).setNickname(courseNickname.getText());
 
         for (int i = 0; i < courseEventType.length-2; i++) {
             
             if (courseEventSel[i].isSelected()) {
 
                 // create course event properties
                 int st = courseEventSTime[i].getSelectedIndex();
                 int et = courseEventETime[i].getSelectedIndex();
                 if (st < et) { // add only those with starttime < endtime
                     int type = courseEventType[i].getSelectedIndex();
                     if (type == 3) type = 4;
                     int d = ((JComboBox)courseEventDays[i]).getSelectedIndex() +1;
                     String loc = courseEventLoc[i].getText();
                     if (loc.equals("lisää paikka")) loc = "";
                     int dur = (et-st)*60;
                     Calendar c = Calendar.getInstance();
                     c.set(0, 0, 0, st, 0, 0);
                     m.addCourseEvent(m.getACourse(courseid), type, c, d, dur, loc);
                 }
             }
         }
         for (int i = 0; i < 2; i++) {
 
             if (courseEventSel[i+6].isSelected()) {
 
                 // create course event properties
                 int st = courseEventSTime[i+6].getSelectedIndex();
                 int et = courseEventETime[i+6].getSelectedIndex();
                 if (st < et) { // add only those with starttime < endtime
                     int td = courseEventTestDays[i].getSelectedIndex() +1;
                     int tm = courseEventTestMonths[i].getSelectedIndex();
                     int ty = Calendar.getInstance().get(Calendar.YEAR) + courseEventTestYears[i].getSelectedIndex();
                     String loc = courseEventLoc[i+6].getText();
                     if (loc.equals("lisää paikka")) loc = "";
                     int dur = (et-st)*60;
                     Calendar c = Calendar.getInstance();
                     c.set(ty, tm, td, st, 0, 0);
                     m.addCourseExam(m.getACourse(courseid), c, loc, dur);
                     System.out.println(td + "." + tm + "." + ty);
                 }
             }
         }
         m.createEventsToCalendar(courseid);
 
         System.out.println(m.getCalendar().size());
         try {
                 m.writeData();
             }
             catch (IOException e) {}
     }
 
     public void exit() {
 
         try {
                 m.writeData();
             }
             catch (IOException e) {}
 
         System.exit(0);
 
     }
 
     public void addEventToCalendar() {
         if (esTime.getSelectedIndex() >= eeTime.getSelectedIndex())
             return;
         if (eName.getText().equals("nimi"))
             return;
         else addE.setName(eName.getText());
 
         eventWindow.setVisible(false);
 
         int y = Calendar.getInstance().get(Calendar.YEAR) + eYear.getSelectedIndex();
         int mo = eMonth.getSelectedIndex();
         int d = eDay.getSelectedIndex() +1;
         int sh = esTime.getSelectedIndex();
         int eh = eeTime.getSelectedIndex();
         
         Calendar st = Calendar.getInstance();
         Calendar et = Calendar.getInstance();
 
         st.set(y,mo,d,sh,0,0);
         et.set(y,mo,d,eh,0,0);
 
         addE.setStarttime(st);
         addE.setEndtime(et);
         
         if (ePlace.getText().equals("paikka"))
             addE.setLocation("");
         else addE.setLocation(ePlace.getText());
 
         if (eOM.getText().equals("lisämerkinnät"))
             addE.setOwnMarkings("");
         else addE.setOwnMarkings(eOM.getText());
 
         m.removeEvent(addE);
         addE.setType(4);
         m.addEvent(addE);
 
         try {
             m.writeData();
         }
         catch (IOException e) {}
 
         createWeekView(curYear, curWeek);
         repaint();
 
 
     }
 
     public void addCourseToCourses() {
         if (crsName.getText().equals("nimi")) return;
         if (crsName.getText().equals("")) return;
 
         System.out.println(crs.size());
 
         String crsn = crsName.getText();
         int sy = Calendar.getInstance().get(Calendar.YEAR) + crsSYear.getSelectedIndex();
         int ey = Calendar.getInstance().get(Calendar.YEAR) + crsEYear.getSelectedIndex();
         int sm = crsSMonth.getSelectedIndex();
         int em = crsEMonth.getSelectedIndex();
         int sd = crsSDay.getSelectedIndex() +1;
         int ed = crsEMonth.getSelectedIndex() +1;
 
         if (sy >= ey) { // add only courses with atleast one day length
             if (sm >= em) {
                 if (sd >= ed)
                     return;
             }
         }
 
         addCourseWindow.hide();
 
         Calendar sc = Calendar.getInstance();
         Calendar ec = Calendar.getInstance();
         sc.set(sy,sm,sd, 0,0, 0);
         ec.set(ey,em,ed, 23, 59, 59);
 
         m.addCourse(sc, ec, crsn, 0);
 
         System.out.println(crs.size());
 
         // update courses
         mapCourses();
     }
 
     
     /**
      * Sets given slot {x, y} background color
      * to Color. in calendarGrid. Used for AI testing.
      *
      * @param x row of slot to be altered
      * @param y column of slot to be altered
      */
     public void setButtonBg(int x, int y, Event e) {
         if (e.getCourseID() < 0) {
             calendarButtons[x][y].setBackground(THEME_COLOR_OTHER);
             return;
         }
         if (e.getType() == 0) {
             calendarButtons[x][y].setBackground(THEME_COLOR_CLECTURE);
             return;
         }
         if (e.getType() == 1) {
             calendarButtons[x][y].setBackground(THEME_COLOR_CSTUDYG);
             return;
         }
         if (e.getType() == 2) {
             calendarButtons[x][y].setBackground(THEME_COLOR_CGUIDG);
             return;
         }
         if (e.getType() == 3) {
             calendarButtons[x][y].setBackground(THEME_COLOR_CTEST);
             return;
         }
         if (e.getType() == 4) {
             calendarButtons[x][y].setBackground(THEME_COLOR_COTHER);
             return;
         }
 
     }
 
 
     /**
      * Disables given slot {x, y} in calendarGrid, sets it's text
      * to 'mover' (ie. X or O) and background color depending on 'mover'.
      *
      * @param mover char to be set as text in button
      * @param x row of slot to be altered
      * @param y column of slot to be altered
      */
     public void setBoardSquareLabel(char mover, int x, int y) {
     /*
         calendarGrid[x][y].setText(String.valueOf(mover));
         calendarGrid[x][y].setEnabled(false);
             if (mover == 'X') {
                 calendarGrid[x][y].setBackground(Color.DARK_GRAY);
             }
             else {
                 calendarGrid[x][y].setBackground(Color.LIGHT_GRAY);
             }
      *
      */
     }
 
    /**
      * Used for loading and saving problems and setThreatInformer().
      *
      * @param message String to be set in 'errorMessages' text area.
      */
     public void setErrorMessage(String message) {
       
     }
     
     /**
      * Opens FileDialog for loading a game. Game
      * is unplayable while FileDialog is open.
      */
     public void openLoadFileDialog() {
         FileDialog loadWindow = new FileDialog(this, "Choose a file to load ", FileDialog.LOAD);
         loadWindow.setFile("*.XOS");
         loadWindow.setDirectory("Saves");
         loadWindow.setLocation(getLocationOnScreen());
         loadWindow.setSize(400, 500);
         loadWindow.show();
         String filePath = loadWindow.getDirectory() +
            System.getProperty("file.separator") + loadWindow.getFile();
         /*
         if (loadWindow.getFile() != null) game.loadGame(filePath);
          *
          */
     }
 
     /**
      * Opens FileDialog for saving the game. Game
      * is unplayable while FileDialog is open.
      */
     public void openSaveFileDialog() {
         FileDialog saveWindow = new FileDialog(this, "Choose a file name to save", FileDialog.SAVE);
         saveWindow.setFile("*.txt");
         saveWindow.setLocation(getLocationOnScreen());
         saveWindow.setSize(400, 500);
         saveWindow.show();
         String filePath = saveWindow.getDirectory() +
            System.getProperty("file.separator") + saveWindow.getFile();
         
         if (saveWindow.getFile() != null) m.writeWeekEventList(curYear, curWeek, filePath);
         
          
     }
 
     /**
      * Clears calendarGrid, ie. sets all buttons label to "   ",
      * enables them and sets background color back to Color.WHITE.
      */
     private void clearGUIgrid() {
          for (int i = 0; i < HOURS ; i++) {
            for (int j = 0; j < WEEKDAYS; j++) {
                b = calendarButtons[i][j];
                b.setText("   ");
                b.setEnabled(true);
                b.setBackground(Color.WHITE);
            }
          }
         
     }
 
   
     public void createStudent() {
 
         FileDialog saveWindow = new FileDialog(this, "Choose a name for student", FileDialog.SAVE);
         saveWindow.setFile("*.stu");
         saveWindow.setDirectory("Students");
         saveWindow.setSize(400, 500);
         saveWindow.setLocation(getLocationOnScreen());
         saveWindow.show();
         String filePath = saveWindow.getDirectory() +
            System.getProperty("file.separator") + saveWindow.getFile();
 
         /*
         if (saveWindow.getFile() != null) game.createPlayer(XO, filePath);
          *
          */
 
     }
 
     /**
      * Updates playerSummaries in GUI to represent current game's player and
      * that players current statistics.
      *
      */
     public void updateEventInfo(Event e) {
         curEvent = e;
         String type = "";
 
 
         if (e.getType() == 0) type = ", luento";
         if (e.getType() == 1) type = ", laskuharjoitus";
         if (e.getType() == 2) type = ", ohjausryhmä";
         if (e.getType() == 3) type = ", tentti";
         if (e.getType() == 4) type = ", muu";
         if (e.getCourseID() < 0) type = "";
 
 
         eventProperties.setText(e.getName() + type + "\n" +
                                 "Päivämäärä: " + parseDate(e.getStarttime()) + "\n" +
                                 "Kello: "+ e.getStarttime().get(Calendar.HOUR_OF_DAY) +" - "
                                          + e.getEndtime().get(Calendar.HOUR_OF_DAY) + "\n" +
                                 "Paikka: "+ e.getLocation() +" \n" +
                                 "Omat merkinnät:");
         eventOwnMarkings.setText(e.getOwnMarkings());
         repaint();
     }
 
     /**
      * Opens new JFrame for general info about the game,
      * it's developer and version number. Game is playable
      * while JFrame is open.
      */
     public void openEventWindow(int i, int j) {
         repaint();
 
         Calendar st = Calendar.getInstance();
         Calendar et = Calendar.getInstance();
         int wd = j;
         if (wd == 6) wd = 1;
         else wd = wd+2;
         
         st.set(Calendar.YEAR, curYear);
         st.set(Calendar.WEEK_OF_YEAR, curWeek);
         st.set(Calendar.DAY_OF_WEEK, wd);
         st.set(Calendar.HOUR_OF_DAY, i);
         st.set(Calendar.MINUTE, 1);
         st.set(Calendar.SECOND, 0);
         st.getTime();
 
         if (i == 23) { 
             i = 0;
             wd++;
             if (wd == 8)
                 wd = 1;
         }
 
         et.set(Calendar.YEAR, curYear);
         et.set(Calendar.WEEK_OF_YEAR, curWeek);
         et.set(Calendar.DAY_OF_WEEK, wd);
         et.set(Calendar.HOUR_OF_DAY, i+1);
         et.set(Calendar.MINUTE, 1);
         et.set(Calendar.SECOND, 0);
         et.getTime();
 
         Event e = new Event(st, et, "", "", -1);
         e.setOwnMarkings("");
       
         createEventWindow(e);
     }
 
     public void createEventWindow(Event e) {
         if (eventWindow != null)
             eventWindow.hide();
         
         eventWindow = new JFrame("Lisää/muuta tapahtuman tietoja");
 
         eName = new JTextField("");
         ePlace = new JTextField("");
         eOM = new JTextArea(5, 30);
         eYear = new JComboBox(years);
         eMonth = new JComboBox(months);
         eDay = new JComboBox(monthdays);
         eeTime = new JComboBox(hrs);
         esTime = new JComboBox(hrs);
         
         eeTime.setSelectedIndex(12);
         esTime.setSelectedIndex(12);
 
         addE = e;
        
         if (e.getName().equals(""))
             eName.setText("nimi");
         else eName.setText(e.getName());
 
         if (e.getLocation().equals(""))
             ePlace.setText("paikka");
         else ePlace.setText(e.getLocation());
 
         if (e.getOwnMarkings().equals(""))
             eOM.setText("lisämerkinnät");
         else eOM.setText(e.getOwnMarkings());
 
         eDay.setSelectedIndex(e.getStarttime().get(Calendar.DAY_OF_MONTH) -1);
         eMonth.setSelectedIndex(e.getStarttime().get(Calendar.MONTH));
         eYear.setSelectedIndex(e.getStarttime().get(Calendar.YEAR) - Calendar.getInstance().get(Calendar.YEAR));
         eeTime.setSelectedIndex(e.getEndtime().get(Calendar.HOUR_OF_DAY));
         esTime.setSelectedIndex(e.getStarttime().get(Calendar.HOUR_OF_DAY));
        
 
         Event = new JPanel(new FlowLayout());
         Event.setFont(THEME_FONT_SMALL);
         Event.setBackground(THEME_COLOR_BLUE);
         Event.setForeground(new Color(0,0,0));
 
         eName.setPreferredSize(new Dimension(120,20));
         eName.setFont(THEME_FONT_SMALL);
         eName.setEditable(true);
         Event.add(eName);
 
         eDay.setPreferredSize(new Dimension(50,20));
         eDay.setFont(THEME_FONT_SMALL);
         eDay.setBackground(THEME_COLOR_VDARKBLUE);
         eDay.setForeground(new Color(0,0,0));
         Event.add(eDay);
 
         eMonth.setPreferredSize(new Dimension(50,20));
         eMonth.setFont(THEME_FONT_SMALL);
         eMonth.setBackground(THEME_COLOR_VDARKBLUE);
         eMonth.setForeground(new Color(0,0,0));
         Event.add(eMonth);
 
         eYear.setPreferredSize(new Dimension(70,20));
         eYear.setFont(THEME_FONT_SMALL);
         eYear.setBackground(THEME_COLOR_VDARKBLUE);
         eYear.setForeground(new Color(0,0,0));
         Event.add(eYear);
 
         esTime.setPreferredSize(new Dimension(70,20));
         esTime.setFont(THEME_FONT_SMALL);
         esTime.setBackground(THEME_COLOR_VDARKBLUE);
         esTime.setForeground(new Color(0,0,0));
         Event.add(esTime);
 
         eeTime.setPreferredSize(new Dimension(70,20));
         eeTime.setFont(THEME_FONT_SMALL);
         eeTime.setBackground(THEME_COLOR_VDARKBLUE);
         eeTime.setForeground(new Color(0,0,0));
         Event.add(eeTime);
 
         ePlace.setPreferredSize(new Dimension(120,20));
         ePlace.setFont(new Font("sansserif", Font.PLAIN, 12));
         ePlace.setBackground(new Color(255,255,255));
         ePlace.setForeground(new Color(0,0,0));
         ePlace.setEditable(true);
         Event.add(ePlace);
 
         addEvent.setPreferredSize(new Dimension(120,20));
         addEvent.setFont(THEME_FONT_SMALL);
         addEvent.setBackground(THEME_COLOR_VDARKBLUE);
 
         JPanel layout = new JPanel(new BorderLayout());
         JPanel ev = new JPanel(new BorderLayout());
 
         ev.add("North", Event);
         ev.add("South", eOM);
         layout.add("North", ev);
         layout.add("South", addEvent);
 
         Event.validate();
 
         eventWindow.add(layout);
         eventWindow.pack();
         eventWindow.setResizable(false);
         eventWindow.setLocation(getLocationOnScreen());
         eventWindow.validate();
         eventWindow.show();
         eventWindow.toFront();
         eventWindow.setVisible(true);
 
 
     }
 
     public void createAddCourseWindow() {
         if (addCourseWindow != null)
             addCourseWindow.hide();
 
         addCourseWindow = new JFrame("Lisää kurssi");
         crsName = new JTextField("nimi");
         crsSDay = new JComboBox(monthdays);
         crsSMonth = new JComboBox(months);
         crsSYear = new JComboBox(years);
         crsEDay = new JComboBox(monthdays);
         crsEMonth = new JComboBox(months);
         crsEYear = new JComboBox(years);
 
         Course = new JPanel(new GridLayout(4,1));
         Course.setFont(THEME_FONT_SMALL);
         Course.setBackground(THEME_COLOR_BLUE);
         Course.setForeground(new Color(0,0,0));
 
         JPanel crsRow = new JPanel(new FlowLayout());
         crsName.setText("nimi");
         crsName.setPreferredSize(new Dimension(250,20));
         crsName.setFont(THEME_FONT_SMALL);
         crsName.setEditable(true);
         crsRow.add(crsName);
         Course.add(crsRow);
         
         crsRow = new JPanel(new FlowLayout());
         JTextField cs = new JTextField("alkaa");
         cs.setPreferredSize(new Dimension(100,20));
         cs.setEditable(false);
         cs.setFont(THEME_FONT_SMALL);
         cs.setBackground(THEME_COLOR_VDARKBLUE);
         cs.setForeground(new Color(0,0,0));
         crsRow.add(cs);
 
         crsSDay.setPreferredSize(new Dimension(50,20));
         crsSDay.setFont(THEME_FONT_SMALL);
         crsSDay.setBackground(THEME_COLOR_VDARKBLUE);
         crsSDay.setForeground(new Color(0,0,0));
         crsRow.add(crsSDay);
         
         crsSMonth.setPreferredSize(new Dimension(50,20));
         crsSMonth.setFont(THEME_FONT_SMALL);
         crsSMonth.setBackground(THEME_COLOR_VDARKBLUE);
         crsSMonth.setForeground(new Color(0,0,0));
         crsRow.add(crsSMonth);
         
         crsSYear.setPreferredSize(new Dimension(70,20));
         crsSYear.setFont(THEME_FONT_SMALL);
         crsSYear.setBackground(THEME_COLOR_VDARKBLUE);
         crsSYear.setForeground(new Color(0,0,0));
         crsRow.add(crsSYear);
         Course.add(crsRow);
 
         crsRow = new JPanel(new FlowLayout());
         cs = new JTextField("päättyy");
         cs.setPreferredSize(new Dimension(100,20));
         cs.setEditable(false);
         cs.setFont(THEME_FONT_SMALL);
         cs.setBackground(THEME_COLOR_VDARKBLUE);
         cs.setForeground(new Color(0,0,0));
         crsRow.add(cs);
 
         crsEDay.setPreferredSize(new Dimension(50,20));
         crsEDay.setFont(THEME_FONT_SMALL);
         crsEDay.setBackground(THEME_COLOR_VDARKBLUE);
         crsEDay.setForeground(new Color(0,0,0));
         crsRow.add(crsEDay);
 
         crsEMonth.setPreferredSize(new Dimension(50,20));
         crsEMonth.setFont(THEME_FONT_SMALL);
         crsEMonth.setBackground(THEME_COLOR_VDARKBLUE);
         crsEMonth.setForeground(new Color(0,0,0));
         crsRow.add(crsEMonth);
 
         crsEYear.setPreferredSize(new Dimension(70,20));
         crsEYear.setFont(THEME_FONT_SMALL);
         crsEYear.setBackground(THEME_COLOR_VDARKBLUE);
         crsEYear.setForeground(new Color(0,0,0));
         crsRow.add(crsEYear);
         Course.add(crsRow);
 
         addCourse.setPreferredSize(new Dimension(120,20));
         addCourse.setFont(THEME_FONT_SMALL);
         addCourse.setBackground(THEME_COLOR_VDARKBLUE);
 
         Course.add(addCourse);
         Course.validate();
 
         addCourseWindow.add(Course);
         addCourseWindow.pack();
         addCourseWindow.setResizable(false);
         addCourseWindow.setLocation(getLocationOnScreen());
         addCourseWindow.validate();
         addCourseWindow.show();
         addCourseWindow.toFront();
         addCourseWindow.setVisible(true);
 
 
 
 
 
 
 
     }
 
 
 }
 
 
             
 
         
 
     
 
 
