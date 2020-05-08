  package no.ntnu.fp.common.model;
 
 
 import no.ntnu.fp.client.gui.GuiConstants;
 import no.ntnu.fp.common.Util;
 import no.ntnu.fp.common.handlers.EventHandler;
 import no.ntnu.fp.server.storage.db.RoomHandler;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 public class Event extends EventHandler implements Model, Comparable<Event> {
 	
 	public static final int TITLE_LENGTH = 40;
 	
 	public static final String ADDED_NEW_PARTICIPANT = "new Participant";
 	public static final String DESCRIPTION_CHANGED = "description changed";
 	public static final String ROOM_CHANGED = "room changed";
 	public static final String DATETO_CHANGED = "dateTo changed";
 	public static final String DATEFROM_CHANGED = "dateFrom changed";
 	public static final String TITLE_CHANGED = "title changed";
 	public static final String EVENT_SAVED = "event saved";
 	
     private int ID;	
     private String title;
     private Calendar dateFrom;
     private Calendar dateTo;
     private Room room;
     private String description;
     private boolean isCanceled;
     private PropertyChangeSupport pcs;
     private ArrayList<Employee> participants;
     private Employee admin;
     //used to paint the event to the calendarDayBox
     private Color eventColor = GuiConstants.EVENT_PENDING;
     private Color eventColorBorder = GuiConstants.EVENT_PENDING_BORDER;
     private Color textColor = GuiConstants.EVENT_TEXT_COLOR;
     private int fromPx = -1;
     private int toPx = -1;
     private int width;
 
     private Event(int id) {
         super(id);
     	pcs = new PropertyChangeSupport(this);
     	participants = new ArrayList<Employee>();
     	dateFrom = Calendar.getInstance();
     	dateTo = Calendar.getInstance();
     }
     public Event(Employee admin){
         this(0);
         ID = 0;
         this.admin = admin;
         participants.add(this.admin);
     }
     public Event(int fromPx, int toPx, Employee admin) {
     	this(0);
     	this.admin = admin;
     	if(toPx-fromPx < GuiConstants.HOUR_HEIGHT/2)
 			toPx += GuiConstants.HOUR_HEIGHT/2 - (toPx-fromPx);
 		this.fromPx = calculatePixelLocation(fromPx);
 		this.toPx = calculatePixelLocation(toPx);
         participants.add(this.admin);
     }
     public Event(int id, String title, Date dateFrom, Date dateTo, Employee admin){
         this(id);
         ID = id;
         setTitle(title);
         setDateFrom(dateFrom);
         setDateTo(dateTo);
         this.admin = admin;
         fromPx = calculatePixelLocation(this.dateFrom);
         toPx = calculatePixelLocation(this.dateTo);
     }
 
     public Event(JSONObject object, boolean is_server) throws JSONException, SQLException {
         this(
                 object.getInt("id"),
                 object.getString("title"),
                 Util.dateTimeFromString(object.getString("date_from")),
                 Util.dateTimeFromString(object.getString("date_to")),
                 new Employee(object.getJSONObject("admin"))
         );
         Util.print("Creating event from JSONObject: " + object);
         setDescription(object.getString("description"));
         if(is_server)
             setRoom(RoomHandler.getRoom(object.getInt("room_id")));
         else
             setRoom(Room.getRoom(object.getInt("room_id")));
 
         JSONArray participantsArray = object.getJSONArray("participants");
         for(int i = 0; i<participantsArray.length(); i++){
             Employee employee = new Employee(participantsArray.getJSONObject(i));
             participants.add(employee);
         }
 
     }
 
     public int getID()               { return ID; }
     public String getTitle()         { return title; }
     public int getFromPixel()        { return fromPx; }
     public int getToPixel()          { return toPx; }
     public Color getEventColor()     { return eventColor; }
     public Color getTextColor()      { return textColor; }
     public Date getDateFrom()        { return dateFrom.getTime(); }
     public Date getDateTo()          { return dateTo.getTime(); }
     public Room getRoom()            { return room; }
     public String getDescription()   { return description == null ? "" : description; }
     public Employee getAdmin()       { return admin; }
 
     public static Event getDummyEvent(String title) {
     	Event evt = new Event(new Employee("Lol Lolsson", "lol@super.com", Calendar.getInstance().getTime(), Employee.Gender.MALE));
     	evt.setDateFrom(Calendar.getInstance().getTime());
     	evt.setDateTo(Calendar.getInstance().getTime());
     	evt.setRoom(new Room(1,"Sebra", "P-15", 10));
     	return evt;
     }
 
 	public static int[] getTimeFromPixel(int px) {
 		int[] hourAndMin = new int[2];
 		hourAndMin[0] = px / GuiConstants.HOUR_HEIGHT;
 		hourAndMin[1] = px % GuiConstants.HOUR_HEIGHT;
 		return hourAndMin;
 	}
 	
 	public Rectangle getBounds() {
 		return new Rectangle(0, fromPx, width, toPx);
 	}
 	
 	private int calculatePixelLocation(Calendar cal) {
 		int hour = cal.get(Calendar.HOUR);
 		int min = cal.get(Calendar.MINUTE);
 		Util.localPrint("calculatePixelLocation  hour: " + " " + hour + ", min: " + min);
 		return hour*GuiConstants.HOUR_HEIGHT + min%GuiConstants.HOUR_HEIGHT;
 	}
 	
 	private int calculatePixelLocation(int px) {
 		int hour = px / GuiConstants.HOUR_HEIGHT;
 		int minute = px % GuiConstants.HOUR_HEIGHT;
 		int quarter = (minute / (GuiConstants.HOUR_HEIGHT / 4));
 		if(minute % (GuiConstants.HOUR_HEIGHT/4) > GuiConstants.HOUR_HEIGHT/8)
 			quarter++;
 		return hour* GuiConstants.HOUR_HEIGHT + quarter * (GuiConstants.HOUR_HEIGHT/4);
 	}
 	
 	public void drawEvent(Graphics g, int overlap) {
 		width = GuiConstants.CANVAS_WIDTH-10*overlap-1;
 		g.setColor(getEventColor());
 		g.fillRect(0, getFromPixel(), width, getToPixel()-getFromPixel());
 		g.setColor(getEventColorBorder());
 		g.drawRect(0, getFromPixel(), width, getToPixel()-getFromPixel());
 		g.setColor(getTextColor());
 		getStringRepresentation(g);
 	}
 	
 //	A hack-ish method that draws the representative string for a meeting
 	public void getStringRepresentation(Graphics g) {
 		if(title == null)
 			return;
 		g.setFont(GuiConstants.EVENT_LABEL_TITLE_FONT);
 		int maxLines = (toPx - fromPx) / (GuiConstants.HOUR_HEIGHT / 4);
 		int maxChars = (int)(width/7.5);
 		String[] titleWords = getTitle().split(" ");
 		int line = 0;
 		String text = "";
 		for(int i = 0; i < titleWords.length; i++) {
 			if(text.length() + titleWords[i].length() >= maxChars) {
 				if(++line > maxLines)
 					return;
 				g.drawString(text, 0, getFromPixel()+line*13);
 				text = "";
 				i--;
 			} else {
 				text += titleWords[i]+ " ";
 			}
 		}
 		if(maxLines != 2)
 			g.drawString(text, 0, getFromPixel()+(++line)*13);
 		if(++line > maxLines)
 			return;
 		if(room == null)
 			return;
 		g.setFont(GuiConstants.EVENT_LABEL_ROOM_FONT);
 		String[] room = {getRoom() != null ? getRoom().toString() : ""}; //check if room is null
 		if(room[0].length() >= maxChars) {
 			room = room[0].split(", ");
 			g.drawString(room[0]+",", 0, getFromPixel()+line*13);
 			g.drawString(room[1], 0, getFromPixel()+(line)*13+9);
 		} else
 			g.drawString(room[0], 0, getFromPixel()+line*13);
 	}
 	
 	public void setFromAndToPixel(int fromPx, int toPx) {
 		if(toPx-fromPx < GuiConstants.HOUR_HEIGHT/2)
 			toPx += GuiConstants.HOUR_HEIGHT/2 - (toPx-fromPx);
 		this.fromPx = calculatePixelLocation(fromPx);
 		this.toPx = calculatePixelLocation(toPx);
 		Util.localPrint("from: " + fromPx + ", to : " + toPx);
 	}
 	
 	public ArrayList<Employee> getParticipants(){
 		return participants;
 	}
 
 
 	public void setFromPixel(int px) { this.fromPx = px; }
 	public void setToPixel(int px)   { this.toPx = px; }
 
 	public void setEventColor(Color eventColor) {
 		this.eventColor = eventColor;
 	}
 
 	public void setTextColor(Color textColor) {
 		this.textColor = textColor;
 	}
 
     public void setTitle(String title) {
     	if(title.length() <= TITLE_LENGTH){
     		String oldTitle = this.title;
     		this.title = title;
     		pcs.firePropertyChange(TITLE_CHANGED, oldTitle, this.title);
     	}
     }
 
     public void setDateFrom(Date dateFrom) {
     	Date oldDateFrom = this.dateFrom.getTime();
         this.dateFrom.setTime(dateFrom);
         pcs.firePropertyChange(DATEFROM_CHANGED, oldDateFrom, this.dateFrom);
 //        if(!getDateFrom().before(getDateTo())) {
 ////        	TODO: virker dette?
 //        	Calendar c = Calendar.getInstance();
 //        	c.setTime(getDateFrom());
 //        	setDateTo(c.getTime());
 //        }
     }
 
 
 
     public void setDateTo(Date dateTo) {
         if(getDateFrom() != null && getDateFrom().before(dateTo)){
         	Date oldDateTo = this.dateTo.getTime();
             this.dateTo.setTime(dateTo);
             pcs.firePropertyChange(DATETO_CHANGED, oldDateTo, this.dateTo);
         }
     }
 	
 
     
     /**
      * sets a new room for the given event
      * @param room
      */
     public void setRoom(Room room) {
     	Room oldRoom = this.room;
         this.room = room;
         pcs.firePropertyChange(ROOM_CHANGED, oldRoom, this.room);
     }
     
 
     public void setDescription(String description) {
     	String oldDescription = this.description;
         this.description = description;
         pcs.firePropertyChange(DESCRIPTION_CHANGED, oldDescription, description);
     }
     
     public void addParticipants (Employee employee){
     	participants.add(employee);
     	pcs.firePropertyChange(ADDED_NEW_PARTICIPANT, employee, participants);
     }
     
     public void setParticipants(ArrayList<Employee> participants){
     	this.participants = participants;
     }
 
     public boolean save(){
         updateEvent(this);
         Event newValue = this;
         pcs.firePropertyChange(EVENT_SAVED, null, newValue);
         return true;
     }
 
     @Override
     public void addPropertyChangeListener(PropertyChangeListener listener) {
         pcs.addPropertyChangeListener(listener);
     }
 
     public int getIsCanceledAsInt() {
         if(isCanceled) return 1;
         return 0;
     }
     
     @Override
     public String toString() {
     	return this.title;
     }
 
 
     
     public JSONObject toJson() throws JSONException {
         Util.print(String.format("Making json of event: %s(%d)",getTitle(),getID()));
         JSONObject object = new JSONObject();
         object.put("id", getID());
         object.put("title", getTitle());
         object.put("date_from", Util.dateTimeToString(getDateFrom()));
         object.put("date_to", Util.dateTimeToString(getDateTo()));
         object.put("room_id", getRoom().getId());
         object.put("description", getDescription());
         object.put("admin", getAdmin().toJson());
         object.put("participants", getParticipantsAsJsonArray());
         return object;
     }
 	@Override
 	public int compareTo(Event e) {
 		if(this.getDateFrom().before(e.getDateFrom()))
 			return -1;
 		else if(this.getDateFrom().after(e.getDateTo()))
 			return 1;
 		else
 			return 0;
 	}
 	public Color getEventColorBorder() {
 		return eventColorBorder;
 	}
 	public void setEventColorBorder(Color eventColorBorder) {
 		this.eventColorBorder = eventColorBorder;
 	}
 	public int getWidth() {
 		return width;
 	}
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
     public JSONArray getParticipantsAsJsonArray() throws JSONException {
         JSONArray array = new JSONArray();
         for(Employee employee:getParticipants()){
             array.put(employee.toJson());
         }
         return array;
     }
 
 
 }
