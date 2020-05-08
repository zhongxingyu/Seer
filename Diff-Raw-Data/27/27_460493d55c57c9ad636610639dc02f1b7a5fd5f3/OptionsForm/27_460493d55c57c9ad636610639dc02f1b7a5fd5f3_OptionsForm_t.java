 package edu.brown.cs32.scheddar.gui;
 
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SpringLayout;
 
 public class OptionsForm extends AbstractForm {
 	private static final long serialVersionUID = 1L;
 	
 	JTextField adminEmail;
 	JPasswordField adminPassword;
 	JTextField adminName;
 	JSpinner startDay;
 	JSpinner endDay;
 	
 	SpinnerDateModel startFieldModel;
 	SpinnerDateModel endFieldModel;
 	
 	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
 	
 	public OptionsForm(ScheddarPane s) {
 		super(s);
 		this.setTitle("Options");
 		
 		JPanel panel = new JPanel();
 		
 		JPanel adminEmailPanel = new JPanel();
 		JPanel adminPassPanel = new JPanel();
 		JPanel adminNamePanel = new JPanel();
 		JPanel startDayPanel = new JPanel();
 		JPanel endDayPanel = new JPanel();
				
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));		
 		adminEmail = new JTextField(20);
 		adminEmail.setText(_scheddar.getUsername());
 		
 		adminPassword = new JPasswordField(20);
 		adminPassword.setText(_scheddar.getPassword());
 		
 		adminName = new JTextField(20);
 		adminName.setText(_scheddar.getAdminName());
 		
 		startFieldModel = new SpinnerDateModel();
 		endFieldModel = new SpinnerDateModel();
 		
 		try {
 			startFieldModel.setValue(timeFormat.parse(_scheddar.getStartHour()));
 			endFieldModel.setValue(timeFormat.parse(_scheddar.getEndHour()));
 		} catch (ParseException e) {
 			System.out.println("parse error meetingpanel");
 		}
 		
 		startDay = new JSpinner();
 		endDay = new JSpinner();
 		
 		startFieldModel.setCalendarField(Calendar.MINUTE);
 		endFieldModel.setCalendarField(Calendar.MINUTE);
 		
 		startDay.setModel(startFieldModel);
 		endDay.setModel(endFieldModel);	
 		
 		startDay.setEditor(new JSpinner.DateEditor(startDay, "HH:mm"));
 		endDay.setEditor(new JSpinner.DateEditor(endDay, "HH:mm"));
 		
 		adminEmailPanel.add(new JLabel("Admin Email: "));
 		adminEmailPanel.add(adminEmail);
 		adminPassPanel.add(new JLabel("Admin Pass : "));
 		adminPassPanel.add(adminPassword);
 		adminNamePanel.add(new JLabel("Admin Name: "));
 		adminNamePanel.add(adminName);
 		startDayPanel.add(new JLabel("Day Start: "));
 		startDayPanel.add(startDay);
 		endDayPanel.add(new JLabel("End Start: "));
 		endDayPanel.add(endDay);
 		
 		panel.add(adminEmailPanel);
 		panel.add(adminPassPanel);
 		panel.add(adminNamePanel);
 		panel.add(startDayPanel);
 		panel.add(endDayPanel);
 		
 		// Submit Button
 		
 		JButton submit = new JButton("Submit");
 		
 		submit.addActionListener(new ActionListener() {
 			
 			@SuppressWarnings("deprecation")
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(adminName.getText().trim().length()>0) {
 					_scheddar.setAdminName(adminName.getText().trim());
 				}
 				if(adminEmail.getText().trim().length()>0){
 					_scheddar.setAdminUsername(adminEmail.getText().trim());
 				}
 				if(adminPassword.getText().trim().length()>0){
 					_scheddar.setAdminPassword(adminPassword.getText().trim());
 				}
 				
 				Calendar start,end;
 				start = Calendar.getInstance();
 				end = Calendar.getInstance();
 				start.setTime(startFieldModel.getDate());
 				end.setTime(endFieldModel.getDate());
 				
 				String newStart = start.get(Calendar.HOUR_OF_DAY) + ":" + start.get(Calendar.MINUTE);
 				String newEnd = end.get(Calendar.HOUR_OF_DAY) + ":" + end.get(Calendar.MINUTE);
 		
 
 				if(newStart.length()>0){
 					_scheddar.setStartHour(newStart);
 				}
 				
 				if(newEnd.length()>0){
 					_scheddar.setEndHour(newEnd);
 				}
 				dispose();
 			}
 		});
 		
 		panel.add(submit);
 		add(panel);
 		pack();
 		setVisible(true);
 	}
 }
