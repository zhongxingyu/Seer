 package no.ntnu.fp.gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.swing.*;
 
 import no.ntnu.fp.Client;
 import no.ntnu.fp.model.Appointment;
 
 public class DayView extends JPanel {
 	private String dayName;
 	private GregorianCalendar date;
 
 	public void setDate(GregorianCalendar d) {
 		date = d;
 	}
 
 	public GregorianCalendar getDate() {
 		return date;
 	}
 
 	// private ArrayList<AppointmentWidget> appointmentWidgets;
 
 	public void paintComponent(Graphics g) {
 		if (date != null) {
 			super.paintComponent(g);
 			g.drawString(dayName, 0, 50);
 			for (int i = 0; i < getPreferredSize().getHeight()
 					/ GUI_CONSTANTS.hourHeight; i++) {
 				g.drawLine(0, i * GUI_CONSTANTS.hourHeight,
 						(int) getPreferredSize().getWidth(), i
 								* GUI_CONSTANTS.hourHeight);
 			}
 			if (Client.appointments.containsKey(Client.dateToString(date))) {
 				for (Appointment a : Client.appointments.get(Client
 						.dateToString(date))) {
 					renderAppointment(g, a);
 				}
 			}
 		}
 	}
 
 	public void renderAppointment(Graphics g, Appointment a) {
 		int x = 0;
 		int y = a.getStartDate().get(GregorianCalendar.HOUR) * GUI_CONSTANTS.hourHeight;
		System.out.println(y);
		int width = getWidth();
 		int height = 100;
 		int paddingX = 5;
 		int paddingY = 5;
 		String title = a.getTitle();
 		g.setColor(Color.RED);
 		// g.drawRoundRect(getBounds().x, getBounds().y, getBounds().width,
 		// getBounds().height, 5, 5);
 		g.fillRoundRect(x, y, width + 2 * paddingX, height + 2 * paddingY,
 				(int) (paddingX), (int) (paddingY));
 		g.setColor(Color.WHITE);
 		g.drawString(title, x + paddingX, y + height + paddingY);
 	}
 
 	public void paintBorder(Graphics g) {
 		super.paintBorder(g);
 
 	}
 
 	public DayView(String dayName) {
 		// appointmentWidgets = new ArrayList<AppointmentWidget>();
 		this.setPreferredSize(GUI_CONSTANTS.dayViewDimension);
 		this.dayName = dayName;
 	}
 
 	/*
 	 * public void addAppointment(Appointment a){ AppointmentWidget ap = new
 	 * AppointmentWidget(a); appointmentWidgets.add(ap); }
 	 */
 
 }
