 package com.innoq.sample;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.swing.*;
 
 import com.google.gson.Gson;
 class JavaSwing1{
 	
 	public String getHTML(String urlToRead) {
 	      URL url;
 	      HttpURLConnection conn;
 	      BufferedReader rd;
 	      String line;
 	      String result = "";
 	      try {
 	         url = new URL(urlToRead);
 	         conn = (HttpURLConnection) url.openConnection();
 	         conn.setRequestMethod("GET");
 	         rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 	         while ((line = rd.readLine()) != null) {
 	            result += line;
 	         }
 	         rd.close();
 	      } catch (Exception e) {
 	         e.printStackTrace();
 	      }
 	      return result;
 	   }
 	
 	public void home(JFrame jfrm, JFrame jfrm2){
 			jfrm.setVisible(true);
 	    	jfrm2.setVisible(false);
 	}
 	
 	public void showEvent(String eventID, JFrame oldjfrm, JFrame newjfrm){
 		JList auswahl = listEvent(eventID);
         newjfrm.getContentPane().add(auswahl);
         oldjfrm.setVisible(false);
     	newjfrm.setVisible(true);
 	}
 	
 	public JList listEvent(String eventID){
 		String e = getHTML("http://localhost:8080/myJPAProject/" + eventID);
 		if (e.startsWith("{",0)) {
 			Event event = new Gson().fromJson(e, Event.class);
 			String[] eventdetails = new String[7];
 			eventdetails[0] = "ID: " + event.getID().toString();
 			eventdetails[1] = "Titel: " + event.getTitle();
 			eventdetails[2] = "Date: " + event.getDate();
 			eventdetails[3] = "Details: " + event.getDetails();
 			eventdetails[4] = "Slots: " + event.getSlots();
 			eventdetails[5] = "Vegetarian: " + event.isVegetarian_opt();
 			String[] bookings = new String[50];
 			for (int i = 0; i < event.getBookings().size(); i++) {
 				bookings[i] = event.getBookings().get(i).getUser().getShortname();
 			}
 			eventdetails[6] = "Bookings: " + bookings.toString();
 			JList auswahl = new JList(eventdetails);
 			return auswahl;
 		}
 		else {
 			String[] err = {"Event nicht vorhanden"};
 			JList auswahl = new JList(err);
 			return auswahl;
 		}
 	}
 	
 	public void display(){
 		
 		
 		final JFrame jfrm=new JFrame("Mittach Application");
         final JFrame jfrm2=new JFrame("Mittach Application");
 
 		//set size
 		jfrm.setSize(400,400);
 
 		//wen closed?
 		jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		//set the layout
 		jfrm.setLayout(new FlowLayout());
 
 		//set visible
 		jfrm.setVisible(true);
 
 		final JTextField eventIDField = new JTextField();
		eventIDField.setSize(50, 20);
         JButton eventButton = new JButton("Zeige Event");
         ActionListener eventlist = new ActionListener() {
             public void actionPerformed( ActionEvent e ) {
             	showEvent(eventIDField.getText(), jfrm, jfrm2);
             }
           };
           eventButton.addActionListener( eventlist );
           
           JButton homeButton = new JButton("Zurck zu Home");
           ActionListener home = new ActionListener() {
               public void actionPerformed( ActionEvent e ) {
               	home(jfrm, jfrm2);
               }
             };
           homeButton.addActionListener( home );
         
           jfrm.getContentPane().add(eventIDField);
         jfrm.getContentPane().add(eventButton);
 
		//set size
 		jfrm2.setSize(400,400);
 
		//wen closed?
 		jfrm2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
		//set the layout
 		jfrm2.setLayout(new FlowLayout());
 
		//set visible
 		jfrm2.setVisible(false);
 		
 		jfrm2.getContentPane().add(homeButton);
 
 	}
 	public static void main(String j[]){
 		//creating thread
 		SwingUtilities.invokeLater(new Runnable(){
 			public void run(){
 				JavaSwing1 obj=new JavaSwing1();
 				obj.display();
 			}
 		});
 	}
 }
 
 
