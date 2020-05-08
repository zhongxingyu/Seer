 package views.gui.components;
 
 import models.Event;
 import utils.DateUtils;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 
 public class JEventDisplay extends JComponent{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6850617078923766896L;
 
 
 	private int rowCount=14;
 	private int startingHour = 8;
     private Calendar mondayDate = null;
 	String[] daysOfTheWeek = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
 	Iterable<EventRectangle> events = new LinkedList<>();
 	List<ActionListener> actionListeners = new LinkedList<>();
 	public JEventDisplay() {
 		// tooltip instant display
 		addMouseListener(new MouseAdapter() {
 			int defaultTooltipDelay;
 			int defaultTooltipVisible;
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				defaultTooltipDelay = ToolTipManager.sharedInstance().getInitialDelay();
 				defaultTooltipVisible = ToolTipManager.sharedInstance().getDismissDelay();
 				ToolTipManager.sharedInstance().setInitialDelay(0);
 				ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				ToolTipManager.sharedInstance().setInitialDelay(defaultTooltipDelay);
 				ToolTipManager.sharedInstance().setDismissDelay(defaultTooltipVisible);
 			}
 			
 		});
 		// event details window click
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				for(EventRectangle eventRectangle : events)
 					if(eventRectangle.contains(e.getPoint()))
 						for(ActionListener actionListener : actionListeners)
 							actionListener.actionPerformed(new ActionEvent(eventRectangle.event, ActionEvent.ACTION_FIRST+1, "display"));
 			}
 		});
 		
 	}
 	public void addActionListener(ActionListener listener){
 		actionListeners.add(listener);
 	}
 	public Iterable<Event> getEvents() {
 		List<Event> ret = new LinkedList<>();
 		for(EventRectangle eventRectangle : events)
 			ret.add(eventRectangle.getEvent());
 		return ret;
 	}
 	public void setEvents(Iterable<Event> events,Calendar mondayDate) {
 		List<EventRectangle> tmp = new LinkedList<>();
 		for(Event event : events)
 			tmp.add(new EventRectangle(event));
 		this.events = tmp;
         this.mondayDate = mondayDate;
 		repaint();
 	}
 	@Override
 	protected void paintComponent(Graphics g) {
 		int colWidth = getWidth()/8;
 		int rowHeight = getHeight()/(rowCount+1);
 		drawRowsAndColumns(colWidth, rowHeight, g);
 	}
 	
 	private void drawRowsAndColumns(int colWidth,int rowHeight,Graphics g){
 		drawWeekNameRow(colWidth,rowHeight,g);
 		drawHourColumn(colWidth,rowHeight,g);
 		g.setColor(Color.WHITE);
 		g.fillRect(colWidth,rowHeight,getWidth(),getHeight());
 		drawLines(colWidth,rowHeight,g);
 		if(events!=null){
 			drawEvents(colWidth,rowHeight,g);
 		}
 	}
 	private void drawLines(int colWidth, int rowHeight, Graphics g) {
 		drawVerticalLines(colWidth, rowHeight, g);
 		drawHorizontalLines(colWidth, rowHeight, g);
 	}
 	private void drawVerticalLines(int colWidth, int rowHeight, Graphics g){
 		g.setColor(Color.BLACK);
 		for(int i=1;i<=7;i++){
 			g.drawLine(colWidth*i,0,colWidth*i,getHeight());
 		}
 	}
 	private void drawHorizontalLines(int colWidth, int rowHeight, Graphics g){
 		g.setColor(Color.BLACK);
 		for(int i=1;i<=rowCount;i++){
 			g.drawLine(0,rowHeight*i,getWidth(),rowHeight*i);
 		}
 	}
 	private void drawHourColumn(int colWidth, int rowHeight, Graphics g) {
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRect(0,rowHeight,colWidth,getHeight());
 		g.setColor(Color.BLACK);
 		for(int i=1;i<=rowCount;i++){
 			int hour = startingHour+i-1;
 			String hourStr = (hour>=10?""+hour:"0"+hour)+":00";
 			g.drawString(hourStr, 
 					(int) (colWidth/2-g.getFontMetrics().getStringBounds(hourStr, g).getWidth()/2),
 					rowHeight*(i+1)-g.getFontMetrics().getHeight()/2);
 		}
 	}
 	private void drawWeekNameRow(int colWidth,int rowHeight,Graphics g){
 		g.setColor(Color.LIGHT_GRAY);
 		g.fillRect(0,0,getWidth(),rowHeight);
 		g.setColor(Color.BLACK);
 		for(int i=1;i<=7;i++){
             if(rowHeight<2*g.getFontMetrics().getHeight())
                 g.drawString(daysOfTheWeek[i-1],
                         colWidth*i+(int) (colWidth/2 - g.getFontMetrics().getStringBounds(daysOfTheWeek[i-1],g).getWidth()/2),
                         rowHeight-g.getFontMetrics().getHeight()/2);
             else if(mondayDate!=null){
                 g.drawString(daysOfTheWeek[i-1],
                         colWidth*i+(int) (colWidth/2 - g.getFontMetrics().getStringBounds(daysOfTheWeek[i-1],g).getWidth()/2),
                         rowHeight/2-g.getFontMetrics().getHeight()/4);
                 Calendar thisDay = (Calendar) mondayDate.clone();
                 thisDay.add(Calendar.DAY_OF_MONTH,i-1);
                 String date = DateUtils.dateDisplay(thisDay);
                 g.drawString(date,
                         colWidth*i+(int) (colWidth/2 - g.getFontMetrics().getStringBounds(date,g).getWidth()/2),
                         rowHeight-g.getFontMetrics().getHeight()/4);
             }
 		}
 	}
 	private void drawEvents(int colWidth,int rowHeight,Graphics g){
 		for(EventRectangle event : events){
 			event.draw(colWidth, rowHeight,g);
 		}
 		drawWeekNameRow(colWidth, rowHeight, g);
 		g.drawLine(0,rowHeight,getWidth(),rowHeight);
 		drawVerticalLines(colWidth, rowHeight, g);
 	}
 	private void drawEventRectangle(int x,int y,int width,int height, int arcWidth,int arcHeight, Color fill, Color border, Graphics g){
 		g.setColor(fill);
 		g.fillRoundRect(x, y, 
 				width,height,
 				arcWidth, arcHeight);
 		g.setColor(border);
 		g.drawRoundRect(x, y, 
 				width, height,
 				arcWidth, arcHeight);
 	}
 	@Override
 	public String getToolTipText(MouseEvent event) {
 		String ret = "";
 		for(EventRectangle eventRectangle : events){
 			if(eventRectangle.contains(event.getPoint())){
 				ret = eventRectangle.getTooltip();
 				break;
 			}
 		}
 		return ret;
 	}
 	
 	@Override
 	public Point getToolTipLocation(MouseEvent event) {
 		if(!getToolTipText(event).equals(""))
 			return null;
 		else return new Point(-10000,-10000);
 	}
 	
 	private class EventRectangle {
         private Event event;
 
         private Calendar getStartTime() {
             return startTime;
         }
 
         private Calendar getEndTime() {
             return endTime;
         }
 
         private Color getColor() {
             return color;
         }
 
         private String getTitle() {
             return title;
         }
 
         private Calendar startTime;
         private Calendar endTime;
         private Color color;
         private String title;
         private String comment;
         boolean isStartBeforeMonday = false;
         public Event getEvent(){
             return event;
         }
 
 		public String getTooltip() {
 			return "<html>"+event.getTitle()+"<br>"+event.getComment()+"</html>";
 		}
 		private static final int arcWidth = 10;
 		private static final int arcHeight = 10;
 		private static final int xOffset = 5; 
 		private List<Rectangle> rectangles = new LinkedList<>();
 		public EventRectangle(Event event) {
             this.event = event;
 			this.title = event.getTitle();
             this.comment = event.getComment();
             this.startTime = event.getStartTime();
             this.endTime = event.getEndTime();
             this.color = event.getColor();
 		}
 		public void draw(int colWidth, int rowHeight,Graphics g){
             if(getStartTime().before(mondayDate)){
                 startTime = (Calendar) mondayDate.clone();
                 startTime.set(Calendar.HOUR_OF_DAY, startingHour);
                 isStartBeforeMonday = true;
             }
             Calendar fridayDate = (Calendar) mondayDate.clone();
             fridayDate.add(Calendar.DAY_OF_MONTH,6);
             fridayDate.set(Calendar.HOUR_OF_DAY,23);
             fridayDate.set(Calendar.MINUTE,59);
             fridayDate.set(Calendar.MILLISECOND,59);
             if(getEndTime().after(fridayDate)){
                 endTime = (Calendar) fridayDate.clone();
                 endTime.set(Calendar.HOUR_OF_DAY,startingHour+rowCount);
             }
 			int startingDay = getStartTime().get(GregorianCalendar.DAY_OF_WEEK);
 			int endingDay = getEndTime().get(GregorianCalendar.DAY_OF_WEEK);
 			int startHour = getStartTime().get(GregorianCalendar.HOUR_OF_DAY);
 			int endHour = getEndTime().get(GregorianCalendar.HOUR_OF_DAY);
 			int startingMinute = getStartTime().get(GregorianCalendar.MINUTE);
 			int endingMinute = getEndTime().get(GregorianCalendar.MINUTE);
 			startingDay= (startingDay+5)%7;
 			endingDay=(endingDay+5)%7;
             if ( endingDay < startingDay )
                 endingDay = startingDay+1;
 			Color eventFillColor = getColor();
 			Color eventBorderColor = Color.BLACK;
 			//System.out.println(""+startingDay+" "+endingDay+"|"+startHour+" "+endHour+"|"+startingMinute+" "+endingMinute);
 			for(int day=startingDay;day<=endingDay;day++){
 				int x = xOffset+colWidth*(day+1);
 				int y = rowHeight*(startHour-startingHour+1)+(int)((float)rowHeight*((float)startingMinute/60.0f));
 				int width = colWidth-xOffset*2;
 				int height = rowHeight*(endHour-(startingDay-endingDay==0?startHour:startingHour))+(int)((float)rowHeight*((float)(endingMinute-(startingDay-endingDay==0?startingMinute:0))/60.0f));
 				Rectangle rectangle;
 				if(day==startingDay&&day==endingDay){
                     if(isStartBeforeMonday)
                         rectangle = new Rectangle(x,rowHeight-arcHeight,width,height+arcHeight);
                     else
 					    rectangle = new Rectangle(x,y,width,height);
 				}
 				else if(day==startingDay){
                     if(isStartBeforeMonday)
                         rectangle = new Rectangle(x,-10,width,getHeight()+arcHeight*2);
                     else
 					    rectangle = new Rectangle(x,y,width,getHeight()+arcHeight*2);
 				}
 				else if(day<endingDay){
                     y= 0;
 					rectangle = new Rectangle(x,0,width,getHeight()+arcHeight*2);
 				}
 				else {
                     if(endHour>startingHour)
                         y = rowHeight;
                     else y = 0;
 					rectangle = new Rectangle(x,rowHeight-arcHeight,width,height+arcHeight);
 				}
 				rectangles.add(rectangle);
 				drawEventRectangle(rectangle.x, rectangle.y, 
 						rectangle.width, rectangle.height,
 						arcWidth, arcHeight, eventFillColor, eventBorderColor, g);
 				g.setColor(Color.BLACK);
 				String eventTitleShrinked = getTitle();
 				if(g.getFontMetrics().getStringBounds(eventTitleShrinked, g).getWidth()>width-arcWidth){
 					eventTitleShrinked+="...";
 					while(g.getFontMetrics().getStringBounds(eventTitleShrinked, g).getWidth()>width-arcWidth&&eventTitleShrinked.length()>4){
 						eventTitleShrinked = eventTitleShrinked.substring(0, eventTitleShrinked.length()-4);
 						eventTitleShrinked+="...";
 					}
 				}
 				g.drawString(eventTitleShrinked, x + arcWidth/2, y + (g.getFontMetrics().getHeight()*2)/3);
 			}
 		}
 		public boolean contains(Point point){
 			for(Rectangle rectangle : rectangles){
 				if(rectangle.contains(point))
 					return true;
 			}
 			return false;
 		}
 	}
 }
