 package no.ntnu.fp.gui;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import no.ntnu.fp.gui.timepicker.DateModel;
 
 /**
  * Shows a grid representing 7days (cols) and 24 hour (rows) with CalendarEnties
  * placed on the sheet with x, y and height corresponding to their weekday, startTime and duration
  * 
  * @author andrephilipp
  * 
  */
 
 public class WeekSheet extends JPanel implements PropertyChangeListener{
 
 	final Color GRID_COLOR = Color.LIGHT_GRAY;
 	final Color HOURLABELS_COLOR = Color.LIGHT_GRAY;
 	private ArrayList<JLabel> hours = new ArrayList<JLabel>();
 	
 	public ArrayList<CalendarEntryView> events = new ArrayList<CalendarEntryView>();
 	
 	private WeekSheetAdapter adapter;
 
 	private int cellHeight;
 	private int cellWidth;
 	private int hourColWidth;
 	private JPanel weekHeader;
 
 	public WeekSheet(WeekSheetAdapter adapter) {
 		this.adapter = adapter;
 		setBackground(Color.GRAY);
 		addHourLabels();
 		addEvents();
 		setPreferredSize(new Dimension(600, 1500));
 	}
 	
 	private void addEvents() {
 		for(CalendarEntryView cev: adapter){
 			events.add(cev);
 			add(cev);
 		}
 	}
 	
 	public void updateSheet() {
 		events.clear();
 		removeAll();
 		addEvents();
		addHourLabels();
 		paint(getGraphics());
 	}
 	
 	public int getCellHeight() {
 		return cellHeight;
 	}
 	
 	public int getCellWidth() {
 		return cellWidth;
 	}
 	
 	private void addHourLabels() {
 		JLabel hour;
 		for (int i = 0; i < 24; i++) {
 			hour = new JLabel(i + ":00");
 			hour.setForeground(HOURLABELS_COLOR);
 			hour.setBounds(0, i * cellHeight, hour.getPreferredSize().width,
 					hour.getPreferredSize().height);
 			add(hour);
 			hours.add(hour);
 			hourColWidth = hour.getWidth() + 2;
 		}
 	}
 
 	public void paint(Graphics g) {
 		cellHeight = getHeight() / 24;
 		cellWidth = (getWidth() - hourColWidth) / 7;
 
 		paintGrid(g);
 		paintEvents();
 		paintHours();
 
 		super.paintComponents(g);
 	}
 
 	private void paintHours() {
 		int i = 0;
 		for (JLabel hour : hours) {
 			hour.setBounds(0, i * cellHeight, hour.getPreferredSize().width,
 					hour.getPreferredSize().height);
 			i++;
 		}
 	}
 
 	private void paintEvents() {
 		int x, y, width, height;
 		for (CalendarEntryView e : events) {
 			x = hourColWidth + (e.getModel().getDayOfWeek() - 1) * cellWidth;
 			y = (e.getModel().getTimeOfDay() * cellHeight) / 60;
 			width = cellWidth;
 			height = (int) (e.getModel().getDuration() * cellHeight) / 60;
 
 			e.setBounds(x, y, width, height);
 		}
 	}
 
 	/**
 	 * Paints a 24x7 grid representing 24 hours and 7 days
 	 * 
 	 * @param Graphic
 	 *            object to paint to
 	 */
 	private void paintGrid(Graphics g) {
 		g.setColor(Color.WHITE);
 		g.drawRect(0, 0, getWidth(), getHeight());
 		g.setColor(GRID_COLOR);
 		for (int i = 0; i < 24; i++) {
 			g.drawLine(0, i * cellHeight, getWidth(), i * cellHeight);
 		}
 
 		for (int i = 0; i < 7; i++) {
 			g.drawLine(i * cellWidth + hourColWidth, 0, i * cellWidth
 					+ hourColWidth, getHeight());
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent arg0) {
 		updateSheet();
 	}
 }
