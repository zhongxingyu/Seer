 package carrental;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.ArrayList;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.text.SimpleDateFormat;
 
 /**
  * GraphicAlternate displays an overview of vehicles and their respective
  * reservations and maintenances.
  * @author CNN
  * @version 2011-12-14
  */
 public class GraphicAlternate extends JComponent {
     public static final int
             VIEW_DAYS = 0,
             VIEW_MONTH = 3;
     public static final int
             S_IN_DAY = 86400,
             S_IN_WEEK = 604800;
     private int unit;
     private int width = 800, height = 600,
             collumnWidth, rowHeight = 20,
             numberOfCollumns, numberOfRows,
             pointerX = 0, pointerY = 0,
             textSpace = 120, textHeight = 15,
             display = VIEW_DAYS;
     private ArrayList<Vehicle> vehicles = new ArrayList<>();
     private ArrayList<ArrayList<Booking>> vehicle_bookings = new ArrayList<>(),
             reference = new ArrayList<>();
     private ArrayList<Timestamp> timestamps = new ArrayList<>();
     private ArrayList<String> dateString;
     private Calendar calendar;
     private Timestamp first_date;
     private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy"),
             month = new SimpleDateFormat("MM");
     
     public GraphicAlternate() {
         calendar = Calendar.getInstance();
         
         addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 int x = e.getX();
                 int y = e.getY();
                 if(x > 710 && y < 15) {
                     display = VIEW_DAYS;
                     refreshDataAndPaint();
                 }
                 else if(x > 710 && y < 30 && y > 15) {
                     display = VIEW_MONTH;
                     refreshDataAndPaint();
                 }
                 else {
                     x = ((x - textSpace) / collumnWidth);
                     y = ((y - 5)/ rowHeight);
                     if(reference.size() > y && reference.get(y) != null) {
                         if(reference.get(y).size() > x && reference.get(y).get(x) != null) {
                             if(reference.get(y).get(x).isMaintenance()) {
                                 CarRental.getView().getMaintenancePanel().setMaintenanceToView(CarRental.getInstance().requestMaintenance(reference.get(y).get(x).getID()));
                                 CarRental.getView().getMaintenancePanel().showViewEntityPanel();
                                 CarRental.getView().viewMaintenance();
                             }
                             else {
                                 CarRental.getView().getReservationPanel().setReservationToView(CarRental.getInstance().requestReservation(reference.get(y).get(x).getID()));
                                 CarRental.getView().getReservationPanel().showViewEntityPanel();
                                 CarRental.getView().viewReservation();
                             }
                         }
                     }
                 }
             }
         });
         refreshDataAndPaint();
     }
     
     /**
      * This method is used to refresh all the data used (gets new lists from
      * model) and repaint the whole visual aspect based on this new data.
      */
     public final void refreshDataAndPaint() {
         reference = new ArrayList<>();
         vehicles = CarRental.getInstance().requestVehicles();
         if(!vehicles.isEmpty()) numberOfRows = vehicles.size();
         else numberOfRows = 0;
         
         first_date = new Timestamp(calendar.getTimeInMillis() * 1000);
         vehicle_bookings = new ArrayList<>();
         for(Vehicle v : vehicles) {
             ArrayList<Booking> bs = CarRental.getInstance().requestBookingsByVehicle(v.getID());
             if(!bs.isEmpty()) {
                 for(Booking b : bs) {
                     if(b.getTStart().before(first_date) && b.getTEnd().after(new Timestamp(calendar.getTimeInMillis())))
                         first_date = b.getTStart();
                 }
                 if(first_date.before(new Timestamp(calendar.getTimeInMillis())))
                     first_date = new Timestamp(calendar.getTimeInMillis());
             }
             vehicle_bookings.add(bs);
         }
         
         switch(display) {
             case(VIEW_DAYS):
                 unit = S_IN_DAY;
                 numberOfCollumns = 7;
                 break;
             case(VIEW_MONTH):
                 unit = S_IN_DAY;
                 numberOfCollumns = 30;
                 break;
             default:
                 unit = 0;
                 numberOfCollumns = 0;
                 break;
         }
         
         if(numberOfCollumns != 0) collumnWidth = width / numberOfCollumns;
         else collumnWidth = width;
         
         timestamps = new ArrayList<>();
         for(int i = 0; i < numberOfCollumns; i++) {
             timestamps.add(new Timestamp(first_date.getTime() - (first_date.getTime() % (unit * 1000)) + (i * unit * 1000)));
         }
         
         if(timestamps.isEmpty()) numberOfCollumns = 0;
         else numberOfCollumns = timestamps.size();
         if(numberOfCollumns > 0) collumnWidth = (width - textSpace) / numberOfCollumns;
         else collumnWidth = (width - textSpace);
         
         dateString = new ArrayList<>();
         for (Timestamp timestamp : timestamps) {
             dateString.add(dateFormat.format(timestamp));
         }
         
         requestFocusInWindow();
         repaint();
     }
 
     /**
      * Moves a marker used to paint the visuals a certain amount down.
      */
     private void movePointerY() {
         if (pointerY >= (height - textHeight)) {
             pointerY = 0;
         } else {
             pointerY += rowHeight;
         }
     }
 
     /**
      * Moves a marker used to paint the visuals a certain amount to the right.
      */
     private void movePointerX() {
         if (pointerX >= (width - collumnWidth)) {
             pointerX = textSpace;
         } else {
             pointerX += collumnWidth;
         }
     }
 
     /**
      * Paints the whole visual. Called through repaint().
      * @param g Graphics element used to paint, supplied automatically when
      * using repaint().
      */
     public void paint(Graphics g) {
         pointerX = textSpace;
         pointerY = 0;
         
         for(int y = 0; y < numberOfRows; y++) {
             if(y % 2 == 0) {
                 g.setColor(new Color(220,220,220));
                 g.fillRect(0, y*rowHeight + 5, width, rowHeight);
             }
             reference.add(new ArrayList<Booking>());
             for(int x = 0; x < numberOfCollumns; x++) {
                 boolean booked = false;
                 Booking bkng = null;
                 for(Booking b : vehicle_bookings.get(y)) {
                     if(b.getTStart().before(timestamps.get(x)) && b.getTEnd().after(timestamps.get(x))) {
                         booked = true;
                         bkng = b;
                         if(b.isMaintenance()) g.setColor(Color.RED);
                         else g.setColor(Color.BLUE);
                     }
                 }
                 if(booked) {
                     g.fillRect(x * collumnWidth + textSpace, y * rowHeight + 6, collumnWidth, rowHeight - 2);
                 }
                 reference.get(y).add(bkng);
                 movePointerX();
             }
             movePointerY();
             pointerX = 0;
         }
         pointerY = rowHeight;
         
         for(Vehicle v : vehicles) {
             g.setColor(Color.black);
             String desc = "";
             if(v.getID() > -1 && !v.getDescription().isEmpty()) {
                 desc = v.getDescription();
                 if(desc.length() > 18) desc = desc.substring(0,16)+"...";
             }
             else desc = "Unknown Vehicle";
             g.drawString(desc, 0, pointerY);
             movePointerY();
         }
         
         pointerX = textSpace;
         int textpointer = height - textHeight;
 
         g.setColor(Color.LIGHT_GRAY);
         g.drawLine(0, height - 3*textHeight, width, height - 3*textHeight);
 
         for (int x = 0; x < numberOfCollumns; x++) {
             boolean draw_this = false, draw_line = false;
             g.setColor(Color.GRAY);
             g.drawLine(pointerX, 0, pointerX, height - 3*textHeight);
             g.setColor(Color.black);
             
             if(numberOfCollumns < 10) draw_this = true;
             if(numberOfCollumns < 20 && numberOfCollumns >= 10 && x % 2 == 0) draw_this = true;
             if(numberOfCollumns < 35 && numberOfCollumns >= 20 && x % 3 == 0) {
                 draw_this = true;
                 draw_line = true;
             }
             if(draw_this) g.drawString(dateString.get(x), (int)(pointerX + (0.5 * (collumnWidth - 60))), textpointer); //60, collumnWidth
             if(draw_line) g.drawLine((int)(pointerX + (0.5 * (collumnWidth - 60)) + 30), textpointer - 12, (int)(pointerX + (0.5 * (collumnWidth - 60)) + 30), height - 3*textHeight);
             movePointerX();
             if (textpointer == height - textHeight) {
                 textpointer -= textHeight;
             } else {
                 textpointer += textHeight;
             }
         }
         
         g.setColor(new Color(230,230,230));
         g.fillRect(705, 0, 80, 36);
         g.setColor(Color.BLACK);
         g.drawRect(705,0,80,36);
         String str = "";
         if(display == VIEW_DAYS) str = "> ";
         g.drawString(str+"View week", 710, 15);
         str = "";
         if(display == VIEW_MONTH) str = "> ";
         g.drawString(str+"View month", 710, 30);
         
         g.setColor(Color.BLUE);
         g.fillRect(4, height - 42, 11, 11);
         g.setColor(Color.RED);
         g.fillRect(4, height - 27, 11, 11);
         g.setColor(Color.BLACK);
         g.drawRect(4, height - 42, 11, 11);
         g.drawRect(4, height - 27, 11, 11);
         g.drawString("Reservation", 20, height - 32);
         g.drawString("Maintenance", 20, height - 16);
     }
 }
