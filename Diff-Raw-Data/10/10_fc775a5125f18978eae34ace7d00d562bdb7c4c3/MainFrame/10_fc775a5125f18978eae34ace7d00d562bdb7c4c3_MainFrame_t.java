 import java.util.Map;
 import java.util.HashMap;
 import javax.swing.*;
 import java.awt.event.*;
 import java.awt.*;
 
 /**
  * Main class for GUI
  * 
  * @author cmcl
  * @version 1.0
  */
 public class MainFrame extends JFrame implements ActionListener {
     private static final long serialVersionUID = 1L;
     private BaseView[] views;
     private JButton currentButton;
     private Map<JButton, BaseView> viewMap;
     private JPanel viewPanel;
     private JLabel viewLabel;
     private JScrollPane scrollPane;
     private EventDialog eventDialog;
     private CalendarModel model;
     private final CalendarOperation calOp;
     
     public MainFrame() {
         super();
         model = new CalendarModel();
         calOp = new CalendarOperation(model);
     }
 
     public void setupGui() {
         setTitle("IS3 Calendar");
         setLocation(100, 100);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         calOp.loadCalendar("Calendar.cal");
         
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent we) {
                 calOp.saveCalendar("Calendar.cal");
             }
         });
         
         
         eventDialog = new EventDialog(this, "Add an Event", model);
         eventDialog.setVisible(false);
         eventDialog.pack();
         setJMenuBar(createMenuBar());
         
         
         views = new BaseView[4];
 
         viewMap = new HashMap<JButton, BaseView>();
         viewLabel = new JLabel();
         
         createViews();
         
         JPanel mainPanel = new JPanel(new BorderLayout());
         JPanel upperPanel = new JPanel(new BorderLayout());
         JPanel labelPanel = new JPanel(new BorderLayout());
         viewPanel = new JPanel(new BorderLayout());
         
         upperPanel.add(labelPanel, BorderLayout.WEST);
         upperPanel.add(viewPanel, BorderLayout.EAST);
         mainPanel.add(upperPanel, BorderLayout.NORTH);
 
         for (BaseView v : views)
             v.setupGui();
 
         scrollPane = new JScrollPane(views[2]);
         viewLabel.setText(views[2].toString());
         
         mainPanel.add(scrollPane, BorderLayout.CENTER);
         
         labelPanel.add(viewLabel, BorderLayout.WEST);
         createButtons();
         
         getContentPane().add(mainPanel);
         // Create buttons
         pack();
         setVisible(true);
     }
 
     public JMenuBar createMenuBar() {
         JMenuBar menuBar = new JMenuBar();
         // File menu
         JMenu fileMenu = new JMenu("File");
         // File -> Exit
         JMenuItem exitItem = new JMenuItem("Exit");
         exitItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 calOp.saveCalendar("Calendar.cal");
                 System.exit(0);
             }
         });
         fileMenu.add(exitItem);
         menuBar.add(fileMenu);
 
         // Event menu
         JMenu eventMenu = new JMenu("Event");
         // Event -> New event
         JMenuItem newEventItem = new JMenuItem("New event");
         newEventItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 eventDialog.createEvent();
                 eventDialog.setVisible(true);
             }
         });
         eventMenu.add(newEventItem);
         // Event -> Edit event
         JMenuItem editEventItem = new JMenuItem("Edit event");
         eventMenu.add(editEventItem);
         editEventItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 eventDialog.editEvent();
                 eventDialog.setVisible(true);
             }
         });
         // Event -> Delete event
         JMenuItem deleteEventItem = new JMenuItem("Delete event");
         eventMenu.add(deleteEventItem);
         deleteEventItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 eventDialog.deleteEvent();
             }
         });
         // Event menu finished.
         menuBar.add(eventMenu);
 
         // Category menu
         JMenu categoryMenu = new JMenu("Category");
         // Category -> View categories
         JMenuItem viewCategoriesItem = new JMenuItem("View categories");
         categoryMenu.add(viewCategoriesItem);
         // Category menu finished.
         menuBar.add(categoryMenu);
 
         return menuBar;
     }
 
     public void actionPerformed(ActionEvent e) {
         JButton source = (JButton) e.getSource();
         BaseView v = viewMap.get(source);
         if (v != null) {
             // Update the view.
             scrollPane.setViewportView(v);
             // Update the view label
             viewLabel.setText(v.toString());
             
             // Update enabled/disabled buttons.
             currentButton.setEnabled(true);
             source.setEnabled(false);
             currentButton = source;
         }
     }
 
     private void createButtons() {
         // Create view buttons.
        JButton day, week, month, year;
        viewMap.put(day = new JButton("Day"), views[0]);
         viewMap.put(week = new JButton("Week"), views[1]);
         viewMap.put(month = new JButton("Month"), views[2]);
         viewMap.put(year = new JButton("Year"), views[3]);
 
         // Add to views
        viewPanel.add(day, BorderLayout.WEST);
         viewPanel.add(week, BorderLayout.EAST);
         viewPanel.add(month, BorderLayout.NORTH);
         viewPanel.add(year, BorderLayout.SOUTH);
         for (JButton b : viewMap.keySet())
             b.addActionListener(this);
 
         // Default is month view
         currentButton = month;
         currentButton.setEnabled(false);
     }
 
     private void createViews() {
	    views[0] = new DayView(model, viewLabel);
         views[1] = new WeekView(model, viewLabel);
         views[2] = new MonthView(model, viewLabel);
         views[3] = new YearView(model, viewLabel);
     }
 }
