 package client.gui.week;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.SwingConstants;
 
 import client.ClientMain;
 import client.gui.avtale.AppointmentPanel;
 import client.model.CalendarModel;
 import client.model.MeetingModel;
 import client.model.UserModel;
 
 
 /**
  * @author Magne
  */
 public class WeekView extends JPanel {
 	
 	private static final long serialVersionUID = -8533878088518459485L;
 	
	public static final String WEEKCLICK = "";
 	
 	public static final int 
 		HOURHEIGHT = 50,
 		HOURWIDTH = 100,
 		SHOWHOURS = 12;
 	
 	
 	private final CalendarModel calModel;
 	
 	private final Calendar date;
 	private final JScrollPane weekScroll;
 	private final JLabel weekLabel;
 	private final JButton prevWeekButton, todayButton, nextWeekButton;
 	private ArrayList<AppointmentPanel>  appointments;
 	private JLayeredPane AppointmentLayer;
 	private PropertyChangeSupport pcs;
 	private JPanel northPanel, dayPanelWithPadding;
 	
 	
 	public WeekView() {
 		
 		calModel = ClientMain.getActiveUser().getCalendarModel();
 		
 		appointments = new ArrayList<AppointmentPanel>();
 
 		date = Calendar.getInstance(); //Sets the default week to view as the current week
 		
 		this.setLayout(new BorderLayout());
 		
 		//North
 		northPanel = new JPanel(new BorderLayout());
 		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
 		weekLabel = new JLabel("Uke " + date.get(date.WEEK_OF_YEAR) + ", " + date.get(date.YEAR),SwingConstants.CENTER);
 		weekLabel.setFont(new Font("Times New Roman", Font.BOLD,20));
 		
 		prevWeekButton = new JButton("<<");
 		prevWeekButton.addActionListener(new prewWeekAction());
 		nextWeekButton = new JButton(">>");
 		nextWeekButton.addActionListener(new nextWeekAction());
 		todayButton = new JButton("I dag");
 		todayButton.addActionListener(new todayAction());
 		buttonPanel.add(prevWeekButton);
 		buttonPanel.add(todayButton);
 		buttonPanel.add(nextWeekButton);
 		
 		northPanel.add(weekLabel, BorderLayout.CENTER);
 		northPanel.add(buttonPanel, BorderLayout.EAST);
 		JPanel testPanel = createDayPanel(date);
 		dayPanelWithPadding = new JPanel(); //Because of the field on the left side that contains the times, e.g. "13:00", we need some extra padding.
 		dayPanelWithPadding.add(Box.createHorizontalStrut(12));
 		dayPanelWithPadding.add(testPanel);
 		northPanel.add(dayPanelWithPadding, BorderLayout.SOUTH);
 		
 		//Center
 		JPanel centerPanel = new JPanel(new BorderLayout());
 		JPanel wvi = createWeekViewInternal();
 		centerPanel.add(wvi, BorderLayout.CENTER);		
 		centerPanel.add(wvi);
 	
 		this.add(northPanel, BorderLayout.NORTH);
 
 		
 		//Legger CenterPanel i et JLayeredPane s jeg kan plasser avtaler over CenterPanel
 		AppointmentLayer = new JLayeredPane();
 		AppointmentLayer.setPreferredSize(new Dimension(HOURWIDTH*7+30,HOURHEIGHT*24));
 		//Legger til centerpanel p 1.layer
 		AppointmentLayer.add(centerPanel, 1, 0);
 		centerPanel.setBounds(0,0,HOURWIDTH*7+50,HOURHEIGHT*25);
 		
 		weekScroll = new JScrollPane(AppointmentLayer);
 		weekScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		weekScroll.setPreferredSize(new Dimension(HOURWIDTH*7 + 50,HOURHEIGHT*SHOWHOURS));
 		this.add(weekScroll, BorderLayout.CENTER);
 		
 		addAllAppointments();
 		
 		pcs = new PropertyChangeSupport(this);
 	}
 	
 	
 	/**
 	 * 
 	 * @param hour The hour it should focus on, between 0 and 23 inclusive.
 	 */
 	public void focusOnHour(int hour) {
 		if (hour > 23 || hour < 0) return;
 		
 		final JScrollBar vs = weekScroll.getVerticalScrollBar();
 		
 		if (hour < SHOWHOURS/2) hour = SHOWHOURS/2;
 		if (hour > (23-SHOWHOURS/2)) hour = (23-SHOWHOURS/2);
 		hour -= SHOWHOURS/2;
 		
 		//System.out.println(hour);
 		
 //		It seems like getMaximum returns only half of the maximum. Dividing by two to compensate
 		vs.setValue(
 				hour * vs.getMaximum() / 2 / SHOWHOURS
 				);
 	}
 	
 	/**
 	 * Creates a JPanel containing 7 labels, writing out the days of the week given as input. e.g "Monday 9.jan Tuesday 10.jan ..."
 	 * @param date A date which you want the JPanel to focus around. Note only the input week is relevant. Tuesday the 6th as input is the same as Thursday the 8th of the same week.
 	 * @return
 	 */
 	private JPanel createDayPanel(Calendar date) {
 		SimpleDateFormat sdf = new SimpleDateFormat("EEE dd.MMM");
 		JPanel p = new JPanel();
 		p.setLayout(new GridLayout(1,8));
 				
 		//Setter dayOfWeek til  vre datoen den frste dagen i uken, bruker den til  loope og skrive dato
 		Calendar dayOfWeek = (Calendar)date.clone();
 		dayOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
 		
 		for (int i = 0; i < 7; i++) {
 			JLabel label = new JLabel(sdf.format(dayOfWeek.getTime()), JLabel.LEFT);
 			
 			JPanel pLabel = new JPanel();
 			pLabel.add(label);
 			pLabel.setPreferredSize(new Dimension(HOURWIDTH,HOURHEIGHT/2));
 			
 			dayOfWeek.add(Calendar.DAY_OF_WEEK, 1);
 			p.add(pLabel);
 		}
 		
 		return p;
 	}
 
 	
 	
 	/**
 	 * Creates a JPanel filled with HourCells and timestamps to the left.
 	 * @return
 	 */
 	private JPanel createWeekViewInternal() {
 		JPanel weekViewInternal = new JPanel();
 		
 		weekViewInternal.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
 		
 		JPanel hourColumn = new JPanel(new GridLayout(24,1));
 		
 		for (int hour = 0; hour < 24; hour++) {
 			String txt = (hour < 10 ? "0" : "") + Integer.toString(hour) + ":00";
 			JLabel label = new JLabel(txt,JLabel.CENTER);
 			JPanel p = new JPanel(new BorderLayout());
 			p.setPreferredSize(new Dimension(label.getPreferredSize().width,HOURHEIGHT));
 			p.add(label, BorderLayout.NORTH);
 			hourColumn.add(p);
 		}
 		
 		JPanel hourCellPanel = new JPanel();
 		hourCellPanel.setLayout(new GridLayout(24,8));
 		
 		for (int i = 0; i < 24*7; i++) {
 			HourCell hc = new HourCell(i / 7, HOURWIDTH,HOURHEIGHT);
 			hc.addMouseListener(new MouseClickListener(i));
 			hourCellPanel.add(hc);
 		}
 		
 		weekViewInternal.add(hourColumn);
 		weekViewInternal.add(hourCellPanel);
 		
 		return weekViewInternal;
 	}
 	
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		pcs.addPropertyChangeListener(listener);
 	}
 	
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		pcs.removePropertyChangeListener(listener);
 	}
 	
 	class MouseClickListener implements MouseListener {
 		long timestamp;
 		boolean secondClick;
 		int day, hour;
 		
 		public MouseClickListener(int index) {
 			// Calculate which day and hour this listener is for
 			hour = index / 7;
 			day = index % 7 + 1;
 			// Create a timestamp;
 			timestamp = System.currentTimeMillis();
 			secondClick = false;
 		}
 		
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			// Clock the click and calculate delta time since last click
 			long now = System.currentTimeMillis();
 			long delta = now - timestamp;
 			timestamp = now;
 			// Only care about double clicks with delta < 500 ms
 			if (delta < 500 && !secondClick) {
 				// Propagate event to all listeners
 				pcs.firePropertyChange(WEEKCLICK, null, new int[]{day, hour});
 				secondClick = true;
 			} else {
 				secondClick = false;
 			}
 		}
 
 		/*
 		 * Unused methods
 		 */
 		@Override
 		public void mouseEntered(MouseEvent arg0) {}
 		@Override
 		public void mouseExited(MouseEvent arg0) {}
 		@Override
 		public void mousePressed(MouseEvent arg0) {}
 		@Override
 		public void mouseReleased(MouseEvent arg0) {}
 	}
 	
 	
 	/**
 	 * Tegner alle avtaler som ligger i appointments arraylist
 	 */
 	private void drawAppointments() {	
 		for (AppointmentPanel AP : appointments){
 			AppointmentLayer.add(AP,2, 0);
 			AP.setOpaque(true);
 			AP.setBounds(AP.getX(), AP.getY(), AP.getWidth(),AP.getLength());
 		}	
 	}
 	
 	/**
 	 * Fjerner Alle avtaler fra panelet og arraylist med removeAllAppointments()
 	 * Legger til alle avtaler for denne uken
 	 * Tegner alle disse avtalene med drawAppintments
 	 */
 	private void addAllAppointments(){
 		removeAllAppointments();
 		for (MeetingModel MM : calModel.getMeetingsInWeek(date)){
 			AppointmentPanel avtale = new AppointmentPanel(MM);
 			appointments.add(avtale);
 		}
 		drawAppointments();
 	}
 	
 	/**
 	 * Fjerner alle avtaler fra Panelet for  s fjerne alle fra arraylist.
 	 */
 	private void removeAllAppointments(){
 		for (AppointmentPanel AP : appointments){
 			AppointmentLayer.remove(AP);
 		}
 		appointments.clear();
 		AppointmentLayer.repaint();
 	}
 	
 	private void setDateLabels(){
 
 		//Setter datofeltene til riktig verdi
 		dayPanelWithPadding.removeAll();
 		JPanel testPanel = createDayPanel(date);
 		dayPanelWithPadding.add(Box.createHorizontalStrut(12));
 		dayPanelWithPadding.add(testPanel);
 		
 		//Setter uke og r
 		weekLabel.setText("Uke " + date.get(date.WEEK_OF_YEAR) + ", " + date.get(date.YEAR));
 		
 	}
 	
 	class  nextWeekAction implements ActionListener { 
         public void actionPerformed(ActionEvent e) { 
         	date.add(Calendar.WEEK_OF_YEAR, 1);
         	setDateLabels();
         	addAllAppointments();
         } 
     }
 	
 	class prewWeekAction implements ActionListener { 
         public void actionPerformed(ActionEvent e) { 
         	date.add(Calendar.WEEK_OF_YEAR, -1);
         	setDateLabels();
         	addAllAppointments();
         } 
     }
 	
 	class  todayAction implements ActionListener { 
         public void actionPerformed(ActionEvent e) { 
         	date.setTime(Calendar.getInstance().getTime());
         	setDateLabels();
         	addAllAppointments();
         } 
     }
 	
 //	public static void main (String args[]) { 
 //        JFrame frame = new JFrame("");
 //        Calendar from =  Calendar.getInstance();
 //        from.set(2012, 3, 21, 13, 0);
 //        Calendar to = Calendar.getInstance();
 //        to.set(2012, 3, 21, 14, 0);
 //        UserModel testPerson = new UserModel("Olano", "ola@hotmail.com", "Ola Nordmann");
 //        MeetingModel  MM = new MeetingModel(from, to, testPerson);
 //        MM.setName("Viktig avtale");
 //        MM.setLocation("spisesalen");
 //        MM.setActive(true);
 //        
 //        Calendar from2 =  Calendar.getInstance();
 //        from2.set(2012, 3, 23, 13, 0);
 //        Calendar to2 = Calendar.getInstance();
 //        to2.set(2012, 3, 23, 14, 0);
 //        UserModel testPerson2 = new UserModel("Hansern", "hans@hotmail.com", "Hans Hansen");
 //        MeetingModel  MM2 = new MeetingModel(from2, to2, testPerson2);
 //        MM2.setName("Verksted");
 //        MM2.setLocation("Fjordgata 2");
 //        MM2.addAttendee(testPerson);
 //        MM2.setActive(true);
 //        
 //        
 //        
 //        WeekView WV = new WeekView();
 //        WV.setOpaque(true);
 //        WV.addAppointment(MM);
 //        WV.addAppointment(MM2);
 //        frame.getContentPane().add(WV); 
 //        frame.pack();  
 //        frame.setVisible(true);   
 //        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 //    }
 }
