 package gui;
 
 import static gui.GuiConstants.LINE_SPACING;
 import static gui.GuiConstants.DAY_SPACING;
 import static gui.GuiConstants.LINE_COLOR;
 
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 
 import calendar.Availability;
 import calendar.CalGroupType;
 import calendar.CalendarGroup;
 import calendar.CalendarResponses;
 import calendar.CalendarSlots;
 import calendar.Event;
 import calendar.Response;
 import calendar.When2MeetEvent;
 
 public class ReplyPanel extends CalPanel{
 
 	private CalendarGroup<CalendarSlots> _slotCals;
 	private CalendarGroup<CalendarResponses> _respCals;
 	private CalendarGroup<CalendarSlots> _clicks;
 	private Day[] _bigDays;
 
 
 	public ReplyPanel() {
 		super();
 		this.setLayout(new GridLayout(1,7,DAY_SPACING,0));
 		this.setBackground(LINE_COLOR);
 	}
 	public ReplyPanel(CalendarGroup<CalendarResponses> respCals,
 			CalendarGroup<CalendarSlots> slotCals) {
 		super();
 
 		this.setBackground(LINE_COLOR);
 
 		_slotCals = slotCals;
 		_respCals = respCals;
 
 		setViewDate();
 
 		_startHour = _slotCals.getStartTime().getHourOfDay();
 		_endHour = _slotCals.getEndTime().getHourOfDay();
 		_numHours = _slotCals.getCalendars().get(0).getNumHours();
 
 		configDays();
 	}
 
 	public CalendarSlots getClicks(){
 		return _clicks.getCalendars().get(0);
 	}
 
 	public void setResps(CalendarGroup<CalendarResponses> respCals){
 		_respCals = respCals;
 		configDays();
 	}
 
 	public int getWeekDayPanelHeight(){
 		return _bigDays[0].getLabelHeight();
 	}
 
 
 	public void setSlots(CalendarGroup<CalendarSlots> slotCals){
 		_slotCals = slotCals;
 
 		_startHour = _slotCals.getStartTime().getHourOfDay();
 		_endHour = _slotCals.getEndTime().getHourOfDay();
 		_numHours = _slotCals.getCalendars().get(0).getNumHours();
 
 		//		_thisMonday = _slotCals.getStartTime().minusDays(_slotCals.getStartTime().getDayOfWeek()-1);
 
 		setViewDate();
 		configDays();
 	}
 
 	public void setBestTimes(CalendarResponses bestTimes){
 		for (Day d: _bigDays){
 			d.setBestTimes(bestTimes);
 		}
 	}
 //CalendarSlots.getDaysBetween(_endDay, _slotCals.getEndTime()) != 0 && 
 	public void nextWeek(){
 		if (_endDay.isBefore(_slotCals.getEndTime())){
 			_startDay = _startDay.plusDays(7);
 			_endDay = _endDay.plusDays(7);
 			if (_endDay.isAfter(_slotCals.getEndTime())){
 				_endDay = _slotCals.getEndTime();
 			}
 		}
		System.out.println("AFTER NEXT: " + _startDay.getDayOfMonth() + " " +_endDay.getDayOfMonth());
 		configDays();
 	}
 
 	public void prevWeek(){
 		if (!(_startDay.getYear()==_slotCals.getStartTime().getYear()
 				&& _startDay.getDayOfYear()==_slotCals.getStartTime().getDayOfYear())){
 			_startDay = _startDay.minusDays(7);
 			_endDay = _endDay.minusDays(7);
 		}
 		configDays();
 	}
 
 	//	public ArrayList<ArrayList<Response>> getDayResps(int dayOfWeek, CalendarGroup<CalendarResponses> respCals){
 	//
 	//		ArrayList<ArrayList<Response>> responses = new ArrayList<ArrayList<Response>>();
 	//		for (CalendarResponses resp: respCals.getCalendars()){
 	//			ArrayList<Response> resps = new ArrayList<Response>();
 	//			for (Response r: resp.getResponses()){
 	//				if (Days.daysBetween(_thisMonday.plusDays(dayOfWeek), r.getStartTime()).getDays()==0){
 	//					resps.add(r);
 	//				}
 	//			}
 	//			responses.add(resps);
 	//		}
 	//		return responses;
 	//
 	//	}
 
 	//	public ArrayList<CalendarSlots> getDaySlots(int dayOfWeek, CalendarGroup<CalendarSlots> slotCals){
 	//
 	//		ArrayList<CalendarSlots> slots = new ArrayList<CalendarSlots>();
 	//		for (CalendarSlots s: slotCals.getCalendars()){
 	//			Availability[][] oneDayAvail = {s.getAvail()[Days.daysBetween(s.getStartTime(), _thisMonday.plusDays(dayOfWeek)).getDays()]};
 	//			CalendarSlots oneDayCal = new CalendarSlots(s.getStartTime(), s.getEndTime(), s.getOwner(), s.getMinInSlot(), oneDayAvail);
 	//			slots.add(oneDayCal);	
 	//		}
 	//		return slots;
 	//	}
 
 	public void setViewDate(){
 		_startDay =  _slotCals.getStartTime();
 		if (_slotCals!=null){
 			if (CalendarSlots.getDaysBetween(_slotCals.getStartTime(), _slotCals.getEndTime()) < 7)
 				_endDay = _slotCals.getEndTime();
 			else
 				_endDay = _slotCals.getStartTime().plusDays(6);	
 		}
 	}
 
 
 	@Override
 	public void makeDays() {
 
 		_bigDays=new Day[7];
 
 		for (int i=0; i<7; i++){
 			_bigDays[i]=new Day(new ClickableDayPanel(), new DayPanel(), new DateTime());
 			this.add(_bigDays[i]);
 		}	
 
 		//		_days = new DayPanel[14];
 		//		for (int i=0; i<14; i+=2){
 		//			_days[i]=new ClickableDayPanel();
 		//			_days[i+1]=new DayPanel();
 		//			this.add(_days[i]);
 		//			this.add(_days[i+1]);
 		//		}		
 	}
 
 	public void configDays(){
 
 		int numDays = CalendarSlots.getDaysBetween(_startDay, _endDay) +1;
		System.out.println("NUMDAYS: " + numDays);
 		this.setLayout(new GridLayout(1,numDays,DAY_SPACING,0));
 		int ctr = 0;
 
 		for (int i=0; i<7; i++){
			System.out.println("Boop");
 			_bigDays[i].setStartHour(_startHour);
 			_bigDays[i].setNumHours(_numHours);
 			_bigDays[i].setDay(_startDay.plusDays(i));
			if (i>=numDays){
 				_bigDays[i].setActive(false);
 				//				//=======
 				//				//		for (int i=0; i<14; i+=2){
 				//				//			_days[i].setStartHour(_startHour);
 				//				//			_days[i+1].setStartHour(_startHour);
 				//				//			
 				//				//			_days[i].setNumHours(_numHours);
 				//				//			_days[i+1].setNumHours(_numHours);
 				//				//			
 				//				//			_days[i].setDay(_thisMonday.plusDays(i/2));
 				//				//			_days[i+1].setDay(_thisMonday.plusDays(i/2));
 				//				//			if (_thisMonday.plusDays(i/2).isAfter(_slotCals.getEndTime())
 				//				//					|| _thisMonday.plusDays(i/2).isBefore(_slotCals.getStartTime())){
 				//				//				_days[i].setActive(false);
 				//				//				_days[i+1].setActive(false);
 				//				//>>>>>>> f50a65162b0b592e3d316e52054b9160553b511f
 			} else {
 				_bigDays[i].setActive(true);
 				_bigDays[i].getClickableDay().setResponses(_respCals);
 				_bigDays[i].getDay().setEvent((Event) _slotCals, ctr);
 
 				_clicks = new CalendarGroup<CalendarSlots>(_slotCals.getStartTime(), _slotCals.getEndTime(), CalGroupType.When2MeetEvent);
 
 				if(((When2MeetEvent) _slotCals).getUserResponse() != null) {
 					_clicks.addCalendar(((When2MeetEvent) _slotCals).getUserResponse());
 				}
 				else {
 					_clicks.addCalendar(new CalendarSlots(_slotCals.getStartTime(),
 							_slotCals.getEndTime(),
 							_slotCals.getCalendars().get(0).getMinInSlot(),
 							Availability.busy));
 				}
 				_bigDays[i].getClickableDay().setSlots(_clicks);
 				ctr++;
 			}
 
 
 			//			for (int i=0; i<14; i+=2){
 			//			_days[i].setStartHour(_startHour);
 			//			_days[i+1].setStartHour(_startHour);
 			//			_days[i].setNumHours(_numHours);
 			//			_days[i+1].setNumHours(_numHours);
 			//			_days[i].setDay(_thisMonday.plusDays(i/2));
 			//			_days[i+1].setDay(_thisMonday.plusDays(i/2));
 			//			if (_thisMonday.plusDays(i/2).isAfter(_slotCals.getEndTime())
 			//					|| _thisMonday.plusDays(i/2).isBefore(_slotCals.getStartTime())){
 			//				_days[i].setActive(false);
 			//				_days[i+1].setActive(false);
 			//			} else {
 			//				_days[i].setActive(true);
 			//				_days[i].setResponses(_respCals);
 			//				_days[i+1].setActive(true);
 			//				// TODO to change back to when2meet style display, change to setSlots and get rid of set Event
 			//				_days[i+1].setEvent((Event) _slotCals, ctr);
 			//				
 			//
 			//				_clicks = new CalendarGroup<CalendarSlots>(_slotCals.getStartTime(), _slotCals.getEndTime(), CalGroupType.When2MeetEvent);
 			//
 			//				if(((When2MeetEvent) _slotCals).getUserResponse() != null) {
 			//					_clicks.addCalendar(((When2MeetEvent) _slotCals).getUserResponse());
 			//				}
 			//				else {
 			//					_clicks.addCalendar(new CalendarSlots(_slotCals.getStartTime(),
 			//					_slotCals.getEndTime(),
 			//					_slotCals.getCalendars().get(0).getMinInSlot(),
 			//					Availability.busy));
 			//				}
 			//				_days[i].setSlots(_clicks);
 			//				ctr++;
 			//			}
 		}		
 //		repaint();
 	}
 
 
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		for(Day day: _bigDays){
 			day.repaint();
 		}
 	}
 
 }
