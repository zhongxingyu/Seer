 package gui;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.GroupLayout.ParallelGroup;
 import javax.swing.GroupLayout.SequentialGroup;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import connection.Client;
 
 import Models.Event;
 
 @SuppressWarnings("serial")
 public class Calendar extends JPanel {
 	
 	private Client client;
 	
 	public void setCalendarEvent(Event event, Color color) {
 		// Find JPanel
 		int day = event.getStartTime().getDay();
		if (day == 0){ day = 7; }
 		int startHour = event.getStartTime().getHours();
 		int endHour = event.getEndTime().getHours();
 		int totalHours = endHour - startHour;
 		
 		int width = 99;
     	int height = 60;
 		
 		for (int i = startHour; i < endHour; i++){
 			JPanel panel = hourPanels.get(day).get(i);
 		
 			// Define layout for the panel
 			GroupLayout layout = new GroupLayout(panel);
 			panel.setLayout(layout);
 		
 			ParallelGroup vertical = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
 	    	SequentialGroup horizontal = layout.createSequentialGroup();
 	    	
 	    	JTextArea textArea;
 	    	if (i == startHour){
 	    		textArea = new JTextArea(event.getTitle());
 	    	} else {
 	    		textArea = new JTextArea("");
 	    	}
 	    	textArea.setEditable(false);
 			textArea.setLineWrap(true);
 			textArea.setBackground(color);
 			textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
 //			if (endHour == startHour){
 //				textArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, darkGreen), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
 //			} else if (i == startHour){
 //				textArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(2, 2, 0, 2, darkGreen), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
 //			} else if (i == (endHour - 1)){
 //				textArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 2, 2, 2, darkGreen), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
 //			} else { 
 //				textArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 2, 0, 2, darkGreen), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
 //			}
 			
 			// Add event to the panels layout
 			vertical.addComponent(textArea, height, height, height);
 			horizontal.addComponent(textArea, width, width, width);
 
 	    	layout.setHorizontalGroup(horizontal);
 	    	layout.setVerticalGroup(vertical);
 		}
 		JPanel lastPanel = hourPanels.get(day).get(endHour);
 		GroupLayout layout = new GroupLayout(lastPanel);
 		lastPanel.setLayout(layout);
 	
 		ParallelGroup vertical = layout.createParallelGroup(GroupLayout.Alignment.LEADING);
     	SequentialGroup horizontal = layout.createSequentialGroup();
 		
     	JTextArea textArea;
     	if (startHour == endHour){
     		textArea = new JTextArea(event.getTitle());
     	} else {
     		textArea = new JTextArea("");
     	}
     	textArea.setEditable(false);
 		textArea.setLineWrap(true);
 		textArea.setBackground(color);
 		
 		height = event.getEndTime().getMinutes();
 		vertical.addComponent(textArea, height, height, height);
 		horizontal.addComponent(textArea, width, width, width);
 
     	layout.setHorizontalGroup(horizontal);
     	layout.setVerticalGroup(vertical);
 	}
 	
 	public Calendar() {
 		dayPanels = new ArrayList<JPanel>();
 		hourPanels = new ArrayList<ArrayList<JPanel>>();
 		layouts = new ArrayList<GroupLayout>();
         
         for (int i = 0; i < 8; i++){
         	dayPanels.add(new JPanel());
         	hourPanels.add(new ArrayList<JPanel>());
         	layouts.add(new GroupLayout(dayPanels.get(i)));
         	for (int j = 0; j < 24; j++){
         		JPanel temp = new JPanel();
         		if (i == 0){
         		}
         		else {
 	        		if (j % 2 == 0) {
 	        			temp.setBackground(Color.white);
 	        		} else {
 	        			temp.setBackground(new Color(240, 240, 240));
 	        		}
         		}
         		temp.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.black));
         		hourPanels.get(i).add(temp);
         	}
         }
         
         for (int i = 0; i < hourPanels.get(0).size(); i++){
         	hourPanels.get(0).get(i).add(new JLabel(i + ":00"));
         }
         
         for (int i = 0; i < dayPanels.size(); i++){
         	dayPanels.get(i).setLayout(layouts.get(i));
         	ParallelGroup horizontal = layouts.get(i).createParallelGroup(GroupLayout.Alignment.LEADING);
         	SequentialGroup vertical = layouts.get(i).createSequentialGroup();
         	for (int j = 0; j < hourPanels.get(i).size(); j++){
         		horizontal.addComponent(hourPanels.get(i).get(j), GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE);
         		vertical.addComponent(hourPanels.get(i).get(j), GroupLayout.PREFERRED_SIZE, 60, GroupLayout.PREFERRED_SIZE);
         	}
         	layouts.get(i).setHorizontalGroup(horizontal);
         	layouts.get(i).setVerticalGroup(vertical);
         }
 
 		GroupLayout calendarPanelLayout = new GroupLayout(this);
         this.setLayout(calendarPanelLayout);
         
         SequentialGroup calendarHorizontal = calendarPanelLayout.createSequentialGroup();
         ParallelGroup calendarVertical = calendarPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING);
         for (int i = 0; i < dayPanels.size(); i++){
         	calendarHorizontal.addComponent(dayPanels.get(i), GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE);
         	calendarVertical.addComponent(dayPanels.get(i), GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE);
         }
         
         calendarPanelLayout.setHorizontalGroup(calendarHorizontal);
         calendarPanelLayout.setVerticalGroup(calendarVertical);
         
 	}
 	JScrollPane scrollex = new JScrollPane(new JPanel());
 	private ArrayList<JPanel> dayPanels;
 	private ArrayList<ArrayList<JPanel>> hourPanels;
 	private ArrayList<GroupLayout> layouts;
 	
 	public void addListener(Client client) {
 		this.client = client;
 		
 	}
 }
