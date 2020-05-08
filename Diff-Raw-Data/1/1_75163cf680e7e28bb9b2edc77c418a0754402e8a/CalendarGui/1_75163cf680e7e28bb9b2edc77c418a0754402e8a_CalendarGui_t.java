 package gui;
 
 import static gui.GuiConstants.FRAME_HEIGHT;
 import static gui.GuiConstants.FRAME_WIDTH;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 import org.joda.time.DateTime;
 
 import cal_master.Communicator;
 import calendar.CalendarGroup;
 import calendar.CalendarResponses;
 import calendar.CalendarSlots;
 
 public class CalendarGui {
 
 	private CalendarGroup<CalendarResponses> _responseGroup;
 	private CalendarGroup<CalendarSlots> _slotGroup;
 	private int _startHour = 0;
 	private int _endHour = 24;
 	private JFrame _frame;
 	private JButton _switch;
 	private CalPanel _myCal;
 	private ReplyPanel _when2MeetCal;
 	private JPanel _dayOfWeekLabels;
 	private JPanel _hourOfDayLabels;
 	private Communicator _communicator = new Communicator();
 	private EventPanel _eventPanel = new EventPanel(_communicator, this);
 
 	public static enum DaysOfWeek {Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday};
 
 	// Represents the monday of the current week
 	private DateTime _thisMonday;
 
 	public CalendarGui(){
 		_thisMonday = new DateTime();
 		_myCal = new MyPanel(_thisMonday, _responseGroup);
 		_when2MeetCal = new ReplyPanel(_thisMonday);
 		_eventPanel.addEvent(new EventLabel("TESTING TESTING", "1234", _communicator, this));
 		buildFrame();
 	}
 
 	public CalendarGui(CalendarGroup<CalendarResponses> responseGroup, CalendarGroup<CalendarSlots> slotGroup){
 		_slotGroup=slotGroup;
 		_responseGroup=responseGroup;
 		_thisMonday = _slotGroup.getStartTime().minusDays(_slotGroup.getStartTime().getDayOfWeek()-1);
 
 		_myCal = new MyPanel(_thisMonday, _responseGroup);
 		_when2MeetCal = new ReplyPanel(_thisMonday, _responseGroup, _slotGroup);
 
 		_startHour = slotGroup.getStartTime().getHourOfDay();
 		_endHour = slotGroup.getEndTime().getHourOfDay();
 
 		_eventPanel.addEvent(new EventLabel("TESTING TESTING", "1234", _communicator, this));
		//_communicator.
 		
 		makeDayLabels();
 		makeHourLabels();
 		buildFrame();
 	}
 
 	public void setSlots(CalendarGroup<CalendarSlots> slotGroup){
 		_slotGroup= slotGroup;
 		_when2MeetCal.setSlots(_slotGroup);
 	}
 	
 	public void makeDayLabels(){
 
 		_dayOfWeekLabels = new JPanel();
 		_dayOfWeekLabels.setBackground(GuiConstants.LINE_COLOR);
 		_dayOfWeekLabels.setLayout(new GridLayout(1, 7, GuiConstants.LINE_SPACING, 0));
 
 		for (DaysOfWeek d: DaysOfWeek.values()){
 			JPanel dayLabel = new JPanel();
 			dayLabel.add(new JLabel(d.name(), SwingConstants.CENTER));
 			dayLabel.setBackground(GuiConstants.LABEL_COLOR);
 			_dayOfWeekLabels.add(dayLabel);
 		}
 	}
 
 	public void makeHourLabels(){
 		_hourOfDayLabels = new JPanel();
 		_hourOfDayLabels.setBackground(GuiConstants.LINE_COLOR);
 		_hourOfDayLabels.setLayout(new GridLayout(_endHour - _startHour, 1, 0, GuiConstants.LINE_SPACING));
 
 		for (int i=_startHour; i<_endHour; i++){
 			JPanel hourLabel = new JPanel();
 			hourLabel.add(new JLabel(i+ ":00", SwingConstants.CENTER), SwingConstants.CENTER);
 			hourLabel.setBackground(GuiConstants.LABEL_COLOR);
 			_hourOfDayLabels.add(hourLabel);
 		}
 	}
 
 
 
 	public void buildFrame(){
 		_frame = new JFrame("Kairos");
 		_frame.add(_dayOfWeekLabels, BorderLayout.NORTH);
 		_frame.add(_hourOfDayLabels, BorderLayout.WEST);
 		_frame.add(_when2MeetCal, BorderLayout.CENTER);
 		_frame.add(_eventPanel, BorderLayout.EAST);
 		_switch = new JButton("SWITCH");
 		_switch.addActionListener(new MainListener());
 		//		_frame.add(_switch, BorderLayout.EAST);
 		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		_frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
 		_frame.setVisible(true);
 	}
 
 	public void nextWeek(){
 		_thisMonday = _thisMonday.plusDays(7);
 		_myCal.nextWeek();
 		_when2MeetCal.nextWeek();
 	}
 
 	public void lastWeek(){
 		_thisMonday = _thisMonday.minusDays(7);
 		_myCal.lastWeek();
 		_when2MeetCal.lastWeek();
 	}
 
 	public void myView(){
 		_frame.getContentPane().remove(_when2MeetCal);
 		_frame.getContentPane().remove(_myCal);
 		_frame.add(_myCal, BorderLayout.CENTER);
 
 		this.repaint();
 	}
 
 	public void replyView(){
 		_frame.remove(_when2MeetCal);
 		_frame.remove(_myCal);
 		_frame.add(_when2MeetCal);
 		this.repaint();
 	}
 
 
 	public void repaint(){
 		_frame.invalidate();
 		_frame.validate();
 	}
 
 	class MainListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			myView();
 		}
 
 	}
 
 }
