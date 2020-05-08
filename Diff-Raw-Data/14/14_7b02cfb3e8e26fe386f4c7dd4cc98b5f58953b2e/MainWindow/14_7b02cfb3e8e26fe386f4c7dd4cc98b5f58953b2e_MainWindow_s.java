 package gui.main;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.UIManager;
 
 import backend.DataService.DataServiceImpl;
 import backend.DataTransferObjects.DayDto;
 
 import java.sql.Date;
 
 
 /**
  * MainWindow combines all GUI components to produce the final application.
  */
 @SuppressWarnings("serial")
 public class MainWindow extends JFrame {
 	
 	private AppointmentPanel ap;
 	private DayPanel dp;
 	private DayDto day;
 	private DatePicker cp;
 	private JPanel sidePanel;
 	private MonthPanel mp;
 	private SearchPane sp;
 	private AppointmentConfirmationPane acp;
 	private WaitListPane wlp;
 	private JSplitPane pane;
 	private JScrollPane sidePane;
 	
 	private boolean inMonthView = false;
 	private boolean showingSearch = false;
 	private boolean showingApptConfirmation = false;
 	private boolean showingWaitList = false;
 
 	
 	//The code (virtually) starts here
 	/**
 	 * MainWindow is divided in two major parts which are initialized by initComponents()
 	 * 1. Side Panel
 	 * 2. Appointment Panel
 	 */
 	public MainWindow(String name) {
 		this.setTitle(name);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//set the window look to default for the system
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {}
 		
 		cp = new DatePicker(this);
 		day = DataServiceImpl.GLOBAL_DATA_INSTANCE.getOrCreateDay(cp.getDate());
 		dp = new DayPanel(day, this);
 		setLayout(new BorderLayout());
 		
 		initComponents(dp);
 		
 		setSize(new Dimension(800,600));
 		setJMenuBar(new Menu(this));
 		setExtendedState(MAXIMIZED_BOTH);
 		setVisible(true);
 		
 	}
 	
 	/**
 	 * Initializes the day view for the scheduler.
 	 * 
 	 * @param dp - a DayPanel that displays the left side panel containing the mini calendar and the buttons of the GUI
 	 */
 	private void initComponents(DayPanel dp) {
 		sidePanel = new JPanel(new BorderLayout());
 		JPanel test = new JPanel(new BorderLayout());
 		test.add(cp, BorderLayout.NORTH);
 		sidePanel.add(test,BorderLayout.NORTH);
 		
 		sidePanel.add(dp, BorderLayout.CENTER);
 
 		sidePane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		sidePane.setViewportView(sidePanel);
 		
 		//Adds the Sidepanel to main window
 		add(sidePane,BorderLayout.WEST);
 		
 		//Adds the appointment panel to main window
 		ap = new AppointmentPanel(dp);
 		add(ap, BorderLayout.CENTER);
 	}
 	
 	
 	/**
 	 * Loads and displays the month view data. 
 	 * 
 	 * @param dp - a DayPanel that displays the left side panel containing the mini calendar and the buttons of the GUI
 	 */
 	private void initMVComponents(DayPanel dp) {
 		sidePanel = new JPanel(new BorderLayout());
 		//Add the calendar on the left to sidepanel
 		JPanel test = new JPanel(new BorderLayout());
 		test.add(cp, BorderLayout.NORTH);
 		sidePanel.add(test,BorderLayout.NORTH);
 		
 		//Add the toolbox daypanel to sidepanel
 		sidePanel.add(dp, BorderLayout.CENTER);
 	
 		//Add above created sidepanel to sidepane
 		sidePane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		sidePane.setViewportView(sidePanel);
 		add(sidePane,BorderLayout.WEST);
 		
 		//Load the month's appointment information using daypanel(to get selected date and month)
 		mp = new MonthPanel(dp);
 		add(mp, BorderLayout.CENTER);
 	}
 
 	/**
 	 * Splits the screen and displays the search pane when the "Search" button is pressed.
 	 */
 	private void showSearch() {
 		sp = new SearchPane(this);
 		if (inMonthView) {
 			remove(mp);
 			pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mp, sp);
 			pane.setResizeWeight(.75);
 			add(pane, BorderLayout.CENTER);
 		} else {
 			remove(ap);
 			pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, ap, sp);
 			pane.setResizeWeight(.75);
 			add(pane, BorderLayout.CENTER);
 		}
 		showingSearch = true;
 	}
 	
 	/**
 	 * Removes the search pane when the "Hide Search" button is pressed.
 	 */
 	private void hideSearch() {
 		if (inMonthView) {
 			remove(pane);
 			add(mp, BorderLayout.CENTER);
 		} else {
 			remove(pane);
 			add(ap, BorderLayout.CENTER);
 		}
 		showingSearch = false;
 	}
 	
 	/**
 	 * Allows the user to switch between showing and hiding the search pane.
 	 * If a different side pane is open (e.g. the appointment confirmation pane or the waitlist pane), 
 	 * that pane will be closed before opening the search pane.
 	 */
 	public void toggleSearch() {
 		if (showingSearch) {
 			hideSearch();
 		} else {
 			if (showingApptConfirmation) {
 				hideApptConfirmation();
 			}
 			if (showingWaitList) {
 				hideWaitList();
 			}
 			showSearch();
 		}
 		repaint();
 		validate();
 	}
 	
 	/**
 	 *  Splits the screen and displays the appointment confirmation pane when the "Appointment Confirmation" button is pressed.
 	 */
 	private void showApptConfirmation() {
 		acp = new AppointmentConfirmationPane(this, dp);
 		if (inMonthView) {
 			remove(mp);
 			pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mp, acp);
 			pane.setResizeWeight(.75);
 			add(pane, BorderLayout.CENTER);
 		} else {
 			remove(ap);
 			pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, ap, acp);
 			pane.setResizeWeight(.75);
 			add(pane, BorderLayout.CENTER);
 		}
 		showingApptConfirmation = true;
 	}
 	
 	/**
 	 * Removes the appointment confirmation pane when the "Hide Appointment Confirmation" button is pressed.
 	 */
 	private void hideApptConfirmation() {
 		if (inMonthView) {
 			remove(pane);
 			add(mp, BorderLayout.CENTER);
 		} else {
 			remove(pane);
 			add(ap, BorderLayout.CENTER);
 		}
 		showingApptConfirmation = false;
 	}
 	
 	/**
 	 * Allows the user to switch between showing and hiding the appointment confirmation pane. 
 	 * If a different side pane is open (e.g. the search pane or the waitlist pane), 
 	 * that pane will be closed before opening the search pane.
 	 */
 	public void toggleApptConfirmation() {
 		if (showingApptConfirmation) {
 			hideApptConfirmation();
 		} else {
 			if (showingSearch) {
 				hideSearch();
 			}
 			if (showingWaitList) {
 				hideWaitList();
 			}
 			showApptConfirmation();	
 		}
 		repaint();
 		validate();
 	}
 	
 	/**
 	 *  Splits the screen and displays the wait list pane when the "Wait List" button is pressed.
 	 */
 	private void showWaitList() {
 		wlp = new WaitListPane(this);
 		if (inMonthView) {
 			remove(mp);
 			pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, mp, wlp);
 			pane.setResizeWeight(.75);
 			add(pane, BorderLayout.CENTER);
 		} else {
 			remove(ap);
 			pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, ap, wlp);
 			pane.setResizeWeight(.75);
 			add(pane, BorderLayout.CENTER);
 		}
 		showingWaitList = true;
 	}
 	
 	/**
 	 * Removes the wait list pane when the "Hide Wait List" button is pressed.
 	 */
 	private void hideWaitList() {
 		if (inMonthView) {
 			remove(pane);
 			add(mp, BorderLayout.CENTER);
 		} else {
 			remove(pane);
 			add(ap, BorderLayout.CENTER);
 		}
 		showingWaitList = false;
 	}
 	
 	/**
 	 * toggleWaitList() allows the user to switch between showing and hiding the wait list pane. 
 	 * If a different side pane is open (e.g. the search pane or the appointment confirmation pane), 
 	 * that pane will be closed before opening the search pane.
 	 */
 	public void toggleWaitList() {
 		if (showingWaitList) {
 			hideWaitList();
 		}
 		else {
 			if (showingSearch) {
 				hideSearch();
 			}
 			if (showingApptConfirmation) {
 				hideApptConfirmation();
 			}
 			showWaitList();
 		}
 		repaint();
 		validate();
 	}
 	
 	/**
 	 * Resets the entire appointment panel, such as when changing time slots.
 	 */
 	public void resetAppointmentPanel() {
 		remove(ap);
 		ap = new AppointmentPanel(dp);
 		add(ap, BorderLayout.CENTER);
 		repaint();
 		validate();
 	}
 	
 	/**
 	 * Determines if user is in day or month view.
 	 * 
 	 * @return true if in month view, false otherwise.
 	 */
 	public boolean inMonthView() {
 		return inMonthView;
 	}
 	
 	/**
 	 * Determines if the search pane is open.
 	 * 
 	 * @return true is search pane is open, false otherwise.
 	 */
 	public boolean showingSearch() {
 		return showingSearch;
 	}
 	
 	/**
 	 * Determines if the appointment confirmation pane is open.
 	 * 
 	 * @return true if the appointment confirmation pane is open, false otherwise.
 	 */
 	public boolean showingApptConfirmation() {
 		return showingApptConfirmation;
 	}
 	
 	/**
 	 * Determines if the wait list pane is open.
 	 * 
 	 * @return true if the wait list pane is open, false otherwise.
 	 */
 	public boolean showingWaitList() {
 		return showingWaitList;
 	}
 	
 	/**
 	 * Returns an instance of the Day class to retrieve a representation of a day (including date and time slot).
 	 * 
 	 * @return the day from the DayPanel
 	 * @see data.Day
 	 */
 	public DayDto getCurrentDay() {
 		return dp.getDay();
 	}
 	
 	/**
 	 * Resets the date of the current day.
 	 */
 	public void refresh() {
 		DataServiceImpl.GLOBAL_DATA_INSTANCE.getOrCreateDay(dp.getDay().getDate());
 	}
 	
 	/** 
 	 * Allows the user to switch between day and month view and resets all panes accordingly,
 	 * especially if a split pane is already open (e.g. the search, appointment confirmation, or wait list panes). 
 	 */
 	public void switchView() {	
 		if (inMonthView) {
 			if (showingWaitList || showingSearch || showingApptConfirmation) {
 				ap = new AppointmentPanel(dp);
 				pane.setLeftComponent(ap);
 				pane.repaint();
 				pane.validate();
 			} else {
 				remove(mp);
 				remove(sidePane);
 				initComponents(dp);
 				repaint();
 				validate();
 			}
 			inMonthView= false;
 			
 		} else {
 			if (showingWaitList || showingSearch || showingApptConfirmation) {
 				mp = new MonthPanel(dp);
 				pane.setLeftComponent(mp);
 				pane.repaint();
 				pane.validate();
 			} else {
 				remove(ap);
 				remove(sidePane);
 				initMVComponents(dp);
 				repaint();
 				validate();
 			}
 			inMonthView= true;
 		}
 		//inMonthView = !inMonthView;
 		dp.isMonthViewValidate();
 	}
 	
 	/**
 	 * Sets the date.
 	 * 
 	 * @param date - an instance of the Date class
 	 * @see data.Date
 	 */
 	public void setDate(Date date) {
 
 		day = DataServiceImpl.GLOBAL_DATA_INSTANCE.getOrCreateDay(date);

 		dp = new DayPanel(day, this);
 		
 		if (inMonthView) {
 			if (showingWaitList || showingSearch || showingApptConfirmation) {
 				mp = new MonthPanel(dp);
 				pane.setLeftComponent(mp);
 				pane.repaint();
 				pane.validate();
 			} else {			
 				remove(mp);
 				remove(sidePane);
 				initMVComponents(dp);
 				repaint();
 				validate();
 			}
 		} else {			
 			
 			if (showingWaitList || showingSearch || showingApptConfirmation) {
 				//sidePanel.remove(dp);
 				//sidePanel.add(dp, BorderLayout.CENTER);
 				//sidePanel.repaint();
 				//sidePanel.validate();
				
 				dp.showingWaitList();
 				ap = new AppointmentPanel(dp);
 				pane.setLeftComponent(ap);
 				pane.repaint();
 				pane.validate();
 			} else {
 				remove(ap);
 				remove(sidePane);
 				initComponents(dp);
 				repaint();
 				validate();
 			}
 		}
 		
 		dp.isMonthViewValidate();		
 		
 	}
 	
 	/**
 	 * Sets the day.
 	 * 
 	 * @param day - an instance of the Day class that represents a particular date.
 	 * @see data.day
 	 */
 	public void setDay(DayDto day) {
 		this.day = day;
 	}
 	
 	/**
 	 * Gets the appointment panel.
 	 * 
 	 * @return An instance of AppointmentPanel
 	 * @see gui.main.AppointmentPanel
 	 */
 	public AppointmentPanel getAppPanel() {
 		return ap;
 	}
 
 	/**
 	 * Gets the month panel.
 	 * 
 	 * @return An instance of the Month Panel
 	 * @see gui.main.MonthPanel
 	 */
 	public MonthPanel getMonthPanel() {
 		return mp;
 	}
 	
 	
 	public DayPanel getDayPanel() {
 		return dp;
 	}
 	
 	
 }
