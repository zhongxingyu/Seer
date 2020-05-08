 package client.gui.panels;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import javax.swing.*;
 import client.model.MeetingModel;
 
public class MeetingModelRenderer extends DefaultListCellRenderer {
 	
 	JLabel label3, label2;
 	MeetingModel model;
 	
 	public MeetingModelRenderer(){
 		label2 = new JLabel();
 		this.add(label2);
 	}
 
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value,
 			int index, boolean isSelected, boolean cellHasFocus) {
 		MeetingModel model = (MeetingModel)value;
 		
 		label2 = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 		
 		if(model != null)
 			setModel(model);
 		
 		setModel((MeetingModel)value);
 		
 		return this;
 	}
 	
 	public void setModel(MeetingModel model){
 		
 		this.model = model;
 		String timeFrom;
 		String timeTo = now();
 		timeFrom = now();
 		label2.setText(timeFrom + " - " + timeTo +"      "+ this.model.getName());
 		label2.setPreferredSize(new Dimension(150,30));
 		
 		//label.setBorder(BorderFactory.createEtchedBorder(Color.black, Color.white));
 		//label2.setBorder(BorderFactory.createLineBorder(Color.black));
 	}
 	
 	public static final String DATE_FORMAT_NOW = "HH:mm";
 	
 	public static String now() {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
 		
 		return sdf.format(cal.getTime());}
 }
 
 
