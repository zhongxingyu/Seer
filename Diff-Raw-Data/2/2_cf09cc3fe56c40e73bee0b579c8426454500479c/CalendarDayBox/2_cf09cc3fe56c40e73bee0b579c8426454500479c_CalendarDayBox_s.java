 package no.ntnu.fp.client.gui.objects;
 
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 import no.ntnu.fp.client.controller.ClientApplication;
 import no.ntnu.fp.client.gui.CalendarPanel;
 import no.ntnu.fp.client.gui.GuiConstants;
 import no.ntnu.fp.common.Util;
 import no.ntnu.fp.common.model.Day;
 import no.ntnu.fp.common.model.Event;
 
 @SuppressWarnings("serial")
 public class CalendarDayBox extends JPanel implements MouseListener, MouseMotionListener, PropertyChangeListener{
 
 	private int y, dy;
 	private Calendar date; 
 	private CalendarCanvas canvas;
 	private Day day;
 	private CalendarPanel parent;
 
     public CalendarDayBox(int reprDay, Calendar date) {
     	this.date = date;
 //    	Util.print("Date in CalendarDayBox: " + date.getTime());
     	day = new Day(date.getTime());
 		switch(reprDay) {
 		case 0: 
 			setBorder(BorderFactory.createEmptyBorder(-5, 0, -5, -5));
 		case 6:
 			setBorder(BorderFactory.createEmptyBorder(-5, -5, -5, 0));
 		default:
 			setBorder(BorderFactory.createEmptyBorder(-5, -5, -5, -5));
 		}
 		initCanvas();
 	}
     
     public void setParent(CalendarPanel parent) {
     	this.parent = parent;
     }
     
     public void addEvent(Event event) {
     	day.add(event);
     	canvas.repaint();
     }
 
 	private void initCanvas() {
 		canvas = new CalendarCanvas();
 		canvas.addMouseListener(this);
 		canvas.addMouseMotionListener(this);
 		add(canvas);
 	}
 
     public void setModel(Day day) {
         this.day = day;
         canvas.repaint();
     }
 
 	public Date getDate() {
 		return day.getDate();
 	}
     
 	public void setDate(Calendar c) {
 		this.date = c;
 		this.day.setDate(c);
 	}
 	
 	public void changeDay(Calendar cal) {
 		this.day.setDate(cal);
 	}
 	
 	public void paintEvents() {
 		canvas.repaint();
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		canvas.mouseIsPressed = true;
 		y = e.getY();
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		dy = e.getY() > y ? e.getY() : y;
 		canvas.repaint();
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		dy = e.getY();
 		Util.print("Mouse released, y: " + y + ", dy: " + dy);
 		canvas.mouseIsPressed = false;
 		createNewEvent(e);
 		canvas.repaint();
 	}
 	
 	public void mouseClicked(MouseEvent e) {
 		int x = e.getX();
 		int y = e.getY();
 		for(Event event : day) {
 			Util.print("Bounds: " + event.getBounds());
 			if(event.getBounds().contains(x, y)) {
 				ClientApplication.getEventViewController().showEvent(event);
 			}
 		}
 	}
 	
 	private void createNewEvent(MouseEvent e) {
 		Event event = new Event(y, dy, ClientApplication.getCurrentUser());
 		int[] from = Event.getTimeFromPixel(event.getFromPixel());
 		int[] to = Event.getTimeFromPixel(event.getToPixel());
 		Calendar calFrom = fixTime(from);
 		Calendar calTo = fixTime(to);
 		event.setDateFrom(calFrom.getTime());
 		event.setDateTo(calTo.getTime());
 		Util.print("Event created: " + event.getDateFrom() + " : " + event.getDateTo());
 		if(y != dy || numOccupations(y, dy) == 0) {
 			ClientApplication.getEventViewController().showEvent(event);
 			event.addPropertyChangeListener(this);
 		}
 	}
 	
 	private int numOccupations(int y, int dy) {
 		int numOccupations = 0;
 		for(Event ev : day) {
			if(ev.getFromPixel() <= y || ev.getToPixel() >= dy) {
 				numOccupations++;
 			}
 		}
 		return numOccupations;
 	}
 	
 	private Calendar fixTime(int[] hourAndMin) {
 		Calendar cal = (Calendar)date.clone();
 		cal.set(Calendar.HOUR_OF_DAY, hourAndMin[0]);
 		cal.set(Calendar.MINUTE, hourAndMin[1]);
 		return cal;
 	}
 	public void propertyChange(PropertyChangeEvent evt) {
 		Event e;
 		String s = evt.getPropertyName();
 		Util.localPrint(s);
 		if(s.equals(Event.EVENT_SAVED)) {
 			e = (Event)(evt.getNewValue());
 			Util.print("Event " + e + " saved to day: " + day.getDate());
 			day.add(e);
 			Util.localPrint(day);
 		}
 		canvas.repaint();
 	}
 	
 	public void mouseEntered(MouseEvent e) {	}
 	public void mouseExited(MouseEvent e) {	}
 	public void mouseMoved(MouseEvent e) {	}
 //	------------INNER CLASS--------------------------------------------------------------
 	private class CalendarCanvas extends JPanel {
 		boolean mouseIsPressed = false;
 		Color foreground = GuiConstants.DRAG_NEW_EVENT;
 		
 		public CalendarCanvas() {
 			setBorder(BorderFactory.createEmptyBorder(-5, -5, -5, -5));
 			setPreferredSize(new Dimension(GuiConstants.CANVAS_WIDTH, GuiConstants.CANVAS_HEIGHT));
 			setForeground(foreground);
 			setBackground(GuiConstants.STD_BACKGROUND);
 		}
 				
 		@Override
 		public void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			drawForegroundLines(g);
 			paintEvents(g);
 			g.setColor(foreground);
 			if(mouseIsPressed) {
 				g.fillRect(0, y, GuiConstants.CANVAS_WIDTH, dy-y);
 				g.setColor(Color.BLUE);
 				g.drawRect(0, y, GuiConstants.CANVAS_WIDTH-1, dy-y);
 			}
 		}
 
 		private void paintEvents(Graphics g) {
 			for(Event e : day) {
 				Util.localPrint("numOccupied: " + numOccupations(y, dy));
 				e.drawEvent(g, numOccupations(y, dy));
 //				g.setColor(e.getEventColor());
 //				g.fillRect(0, e.getFromPixel(), GuiConstants.CANVAS_WIDTH-10, e.getToPixel()-e.getFromPixel());
 //				g.setColor(e.getEventColorBorder());
 //				g.drawRect(0, e.getFromPixel(), GuiConstants.CANVAS_WIDTH-10, e.getToPixel()-e.getFromPixel());
 //				g.setColor(e.getTextColor());
 //				e.getStringRepresentation(g);
 			}
 		}
 		private void drawForegroundLines(Graphics g) {
 			g.setColor(GuiConstants.STD_FOREGROUND);
 			for(int i = 1; i < GuiConstants.HOURS; i++) {
 				g.drawLine(0, i* GuiConstants.HOUR_HEIGHT, GuiConstants.CANVAS_WIDTH, i* GuiConstants.HOUR_HEIGHT);
 			}
 		}
 		
 	}
 
 
 }
