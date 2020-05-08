 package calendar;
 
 import gui.DayPanel;
 import gui.GuiConstants;
 
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.joda.time.DateTime;
 
 public class Event extends CalendarGroup<CalendarSlots> {
 	private ArrayList<java.awt.Color> _colors = new ArrayList<java.awt.Color>(); 
 	private ArrayList<EventUpdate> _updates = new ArrayList<EventUpdate>();
 	private CalendarSlots _userResponse = null;
 	private boolean _userHasSubmitted = false;
 	private boolean _hasUpdates = false;
 	protected String _name, _url;
 	protected int _id;
 	
 	
 	
 	public Event(DateTime start, DateTime end, CalGroupType type){
 		super(start, end, type);
 		initColors();
 	}
 	
 	public Event(DateTime start, DateTime end, Collection<CalendarSlots> cals, CalGroupType type){
 		super(start, end, cals, type);
 		initColors();
 	}
 	
 	
 	public ArrayList<String> getCalOwnerNames() {
 		ArrayList<String> names = new ArrayList<String>();
 		if(_userHasSubmitted)
 			names.add(_userResponse.getOwner().getName());
 		
 		for(CalendarSlots cal : this.getCalendars())
 			names.add(cal.getOwner().getName());
 		
 		return names;
 	}
 	public void setID(int id) { _id = id; };
 	public void setURL(String url) { _url = url; }
 	public void setName(String name) {_name = name; }
 	
 	
 	public void setUserResponse(CalendarSlots cal) { 
		System.out.println("SETTING USER RESPONSE");
 		_userResponse = cal; 
		System.out.println("Num cals before : " + this.getCalendars().size());
 		this.removeCalendar(cal);
		System.out.println("Num cals after : " + this.getCalendars().size());
 		String names = "";
 		for(CalendarSlots c : this.getCalendars()){
 			names += c.getOwner().getName() + ", ";
 		}
		System.out.println(names);
 	}
 	public void setUserSubmitted(boolean b) {_userHasSubmitted = b; }
 	
 	public CalendarSlots getCalByName(String name) {
 		CalendarSlots cal = null;
 		for(int i = 0; i < this.getCalendars().size(); i++) { 
 			if(this.getCalendars().get(i).getOwner().getName().equalsIgnoreCase(name)) {
 				cal = this.getCalendars().get(i);
 				break;
 			}
 		}
 		return cal;
 	}
 	
 	private void initColors() {
 		_colors.add(GuiConstants.PALE_YELLOW);
 		_colors.add(GuiConstants.AQUAMARINE);
 		_colors.add(GuiConstants.PALE_GREEN);
 		_colors.add(GuiConstants.BRIGHT_ORANGE);
 		_colors.add(GuiConstants.BABY_BLUE);
 		//_colors.add(GuiConstants.BLUEBERRY);
 		//_colors.add(GuiConstants.DARK_BROWN);
 		_colors.add(GuiConstants.PEACH);
 		
 		
 	}
 	
 	public String getURL(){ return _url; }
 	public int getID(){ return _id; }
 	public String getName(){ return _name; }
 	public CalendarSlots getUserResponse() { return _userResponse; }
 	
 	
 	public boolean hasUpdates() { return _hasUpdates; }
 	public ArrayList<EventUpdate> getUpdates() { return _updates; }
 	public void addUpdates(ArrayList<EventUpdate> newUpdates) {
 		assert newUpdates != null;
 		assert _updates != null;
 		
 		if(newUpdates.size() == 0)
 			return;
 		_hasUpdates = true;
 		_updates.addAll(0,  newUpdates);
 	}
 	
 	public void updatesViewed() { _hasUpdates = false;}
 	
 	
 	public boolean userHasSubmitted(){
 		return _userHasSubmitted;
 	}
 
 	// 10 pts
 	// 0 - 9
 	// 0 -> 0
 	// 9 -> height
 	// index / (numPts - 1) * height
 	
 	private int endPtToHeight(int index, int numPts, int height) {
 		double indDbl = (double) index;
 		double numPtsDbl = (double) numPts;
 		double heightDbl = (double) height;
 		return (int) (indDbl * heightDbl / (numPtsDbl - 1.0));
 		//return (int) ((1.0 * index) / (1.0 * numPts - 1.0) * height);
 	}
 	
 	private int slotHeight(int numPts, int height) {
 		return height / numPts;
 	}
 	
 	private int availWidth(int totalAvail, int width) {
 		return width / totalAvail;
 	}
 
 	public void paint(Graphics2D brush, DayPanel d, int day){
 		_colors.clear();
 		initColors();
 
 		ArrayList<CalendarSlots> cals = this.getCalendars();
 		int numSlotsInDay = cals.get(0).getSlotsInDay();
 		int days = cals.get(0).getDays();
 
 		// 0 1 2 3
 		// goes from left, right edge left, left edge right, right edge right
 		int[][] endpts = new int[numSlotsInDay*2+1][4];
 		for(int i = 0; i < endpts.length; i++) {
 			endpts[i][0] = 0;
 			endpts[i][1] = 0;
 			endpts[i][2] = 0;
 			endpts[i][3] = 0;
 		}
 
 		for(int i = 0; i < cals.size(); i++) {
 			// Left is 0, right is 1
 			int outside = 0;
 			int inside = 1;
 			if(i % 2 == 1) {
 				outside = 3;
 				inside = 2;
 			}
 
 			CalendarSlots cal = cals.get(i);
 			brush.setColor(_colors.get(i % _colors.size()));
 			// Fill in availabilities
 			for(int slot = 0; slot < numSlotsInDay; slot++) {
 				int s = slot*2+1;
 				if(cal.getAvail(day, slot) == Availability.free)
 					endpts[s][outside] = endpts[s][inside] + 1;
 				else
 					endpts[s][outside] = endpts[s][inside];
 			}
 
 			// Connect the dots
 			for(int endPtSlot = 2; endPtSlot < endpts.length - 1; endPtSlot = endPtSlot + 2) {
 				int above = endpts[endPtSlot - 1][outside];
 				int below = endpts[endPtSlot + 1][outside];
 				if(above == below)
 					endpts[endPtSlot][outside] = above;
 				else if(above < below)
 					endpts[endPtSlot][outside] = above;
 				else
 					endpts[endPtSlot][outside] = below;
 			}
 
 			// Draw this calendar
 			int pHeight = d.getHeight();
 			int numEndPts = endpts.length;
 			int availWidth = availWidth(cals.size(), d.getWidth());
 			for(int slot = 0; slot < numSlotsInDay; slot++) {
 				Polygon poly = new Polygon();
 				int endPtAbove = slot * 2;
 				int endPtHere = slot * 2 + 1;
 				int endPtBelow = slot * 2 + 2;
 				int topY = endPtToHeight(endPtAbove, numEndPts, pHeight);
 				int bottomY = endPtToHeight(endPtBelow, numEndPts, pHeight);
 				int centerX = d.getWidth() / 2;
 				int centerY = (topY + bottomY) / 2;
 
 				// If we're on the left, want to subtract from center
 				// Otherwise want to add to center
 				int dir = -1;
 				if(outside == 3)
 					dir = 1;
 
 				int outsideXMiddle = centerX + endpts[endPtHere][outside] * dir * availWidth;
 				int insideXMiddle = centerX + endpts[endPtHere][inside] * dir * availWidth;
 				int outsideXTop = centerX + endpts[endPtAbove][outside] * dir * availWidth;
 				int insideXTop = centerX + endpts[endPtAbove][inside] * dir * availWidth;
 				int outsideXBot = centerX + endpts[endPtBelow][outside] * dir * availWidth;
 				int insideXBot = centerX + endpts[endPtBelow][inside] * dir * availWidth; 
 
 				// Don't want to add top points
 				if(! (outsideXTop == insideXTop && outsideXMiddle == insideXMiddle)) {
 					poly.addPoint(outsideXTop, topY);
 					poly.addPoint(insideXTop, topY);
 				}
 				
 				if(! (outsideXMiddle == insideXMiddle))
 					poly.addPoint(insideXMiddle, centerY);
 				
 				if(!(outsideXBot == insideXBot && outsideXMiddle == insideXMiddle)) {
 					poly.addPoint(insideXBot, bottomY);
 					poly.addPoint(outsideXBot, bottomY);		
 				}
 				if(! (outsideXMiddle == insideXMiddle))
 					poly.addPoint(outsideXMiddle, centerY);
 
 				brush.draw(poly);	
 				brush.fill(poly);
 			}
 
 
 			// Move over endpts
 			for(int j = 0; j < endpts.length; j++)
 				endpts[j][inside] = endpts[j][outside];
 		}
 	}
 	
 
 	
 }
