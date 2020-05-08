 package gui.main;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.UIManager;
 
 import data.Date;
 import data.Day;
 import data.DayLoader;
 import data.DaySaver;
 
 public class MainWindow extends JFrame {
 	
 	private AppointmentPanel ap;
 	private DayPanel dp;
 	private Day day;
 	private DayLoader dl;
 	private DatePicker cp;
 	private JPanel sidePanel;
 	private MonthPanel mp;
 	private WaitListPane wlp;
 	private JSplitPane pane;
 	private JScrollPane sidePane;
 	
 	private boolean inMonthView = false;
 	private boolean showingWaitList = false;
 	
 	//The code (virtually) starts here
 	public MainWindow(String name) {
 		/*MainWindow is divided in two major parts which are initialised by initComponents()
 		 * 1. Side Panel
 		 * 2. Appointment Panel
 		 */
 		
 		this.setTitle(name);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//set the window look to default for the system
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {}
 		
 		cp = new DatePicker(this);
 		
 		dl = new DayLoader();
 		day = dl.loadDay(cp.getDate());
 		if (day == null) {
 			day = new Day(cp.getDate());
 		}
 		dp = new DayPanel(day, this);
 		setLayout(new BorderLayout());
 		initComponents(dp);
 		setSize(new Dimension(800,600));
 		setJMenuBar(new Menu(this));
 		setExtendedState(MAXIMIZED_BOTH);
 		setVisible(true);
 		
 	}
 	
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
 	
 	
 	//Loads and displays the month view data 
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
 	
 	public void toggleWaitList() {
 		if (showingWaitList) hideWaitList();
 		else showWaitList();
 		repaint();
 		validate();
 	}
 	
 	/**
 	 * resets the entire appointment panel, such as when changing time slots
 	 */
 	public void resetAppointmentPanel() {
 		remove(ap);
 		ap = new AppointmentPanel(dp);
 		add(ap, BorderLayout.CENTER);
 		repaint();
 		validate();
 	}
 	
 	public boolean inMonthView() {
 		return inMonthView;
 	}
 	
 	public boolean showingWaitList() {
 		return showingWaitList;
 	}
 	
 	public Day getCurrentDay() {
 		return dp.getDay();
 	}
 	
 	public void refresh() {
 		setDate(dp.getDay().getDate());
 	}
 	
 	public void switchView() {	
 		if (inMonthView) {
 			if (showingWaitList) {
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
 			
 		} else {
 			if (showingWaitList) {
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
 		}
 		inMonthView = !inMonthView;
 	}
 	
 	public void setDate(Date date) {
 		new DaySaver().storeDay(day);
 		day = dl.loadDay(date);
 		if (day == null) {
 			day = new Day(date);
 			new DaySaver().storeDay(day);
 		}
 		dp = new DayPanel(day, this);
 		
 		if (inMonthView) {
 			if (showingWaitList) {
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
 			if (showingWaitList) {
 				sidePanel.remove(dp);
 				sidePanel.add(dp, BorderLayout.NORTH);
 				sidePanel.repaint();
 				sidePanel.validate();
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
 		//Added by Aakash
 		dp.isMonthViewValidate();
 	}
 	
 	public void setDay(Day day) {
 		this.day = day;
 	}
 	
 	public AppointmentPanel getAppPanel() {
 		return ap;
 	}
 
 	public MonthPanel getMonthPanel() {
 		return mp;
 	}
 	
 	
 }
