 package net.clonecomputers.lab.todo;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.EtchedBorder;
 
 public class AlarmSettingsPanel extends JPanel {
 
 	private static final long serialVersionUID = -8619450133970057390L;
 	
 	private JTextField nameField;
 	private DatePanel yearPanel;
 	private DatePanel monthPanel;
 	private DatePanel dayPanel;
 	private DatePanel hourPanel;
 	private DatePanel minutePanel;
 	private JButton saveButton;
 	private JButton cancelButton;
 	
 	private final Todo main;
 	
 	private AlarmListItem origAlarm;
 	
 	public AlarmSettingsPanel(Todo todo) {
 		super();
 		main = todo;
 		setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		add(getNameSettingsPanel());
 		yearPanel = new DatePanel("Year");
 		monthPanel = new DatePanel("Month");
 		dayPanel = new DatePanel("Day");
 		hourPanel = new DatePanel("Hour");
 		minutePanel = new DatePanel("Minute");
 		add(yearPanel);
 		add(monthPanel);
 		add(dayPanel);
 		add(hourPanel);
 		add(minutePanel);
 		add(getSaveCancelButtons());
 		this.setVisible(false);
 	}
 	
 	public void show(AlarmListItem alarm) {
 		origAlarm = alarm;
 		nameField.setText(alarm.getName());
 		ScheduledAlarm alarmData = alarm.getAlarm();
 		yearPanel.setValue(Integer.toString(alarmData.getYear()));
 		monthPanel.setValue(Integer.toString(alarmData.getMonth()));
 		dayPanel.setValue(Integer.toString(alarmData.getDay()));
 		hourPanel.setValue(Integer.toString(alarmData.getHour()));
 		minutePanel.setValue(Integer.toString(alarmData.getMinute()));
 		this.setVisible(true);
 	}
 	
	public void hide() {
 		this.setVisible(false);
 		origAlarm = null;
 	}
 	
 	public boolean isSaved() {
 		return this.toAlarmListItem().equals(origAlarm);
 	}
 	
 	private JPanel getNameSettingsPanel() {
 		JPanel namePanel = new JPanel();
 		//namePanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
 		namePanel.setBorder(new OurEtchedBorder(OurEtchedBorder.RAISED, OurEtchedBorder.BOTTOM));
 		namePanel.add(new JLabel("Alarm Name:"));
 		nameField = new JTextField(10);
 		namePanel.add(nameField);
 		namePanel.setAlignmentY(0.5F);
 		return namePanel;
 	}
 	
 	private JPanel getSaveCancelButtons() {
 		JPanel outsidePanel = new JPanel();
 		outsidePanel.setLayout(new BorderLayout());
 		outsidePanel.setBorder(new OurEtchedBorder(OurEtchedBorder.RAISED, OurEtchedBorder.TOP));
 		JPanel buttonPanel = new JPanel(new BorderLayout());
 		buttonPanel.setBorder(BorderFactory.createEmptyBorder(3, 9, 0, 9));
 		saveButton = new JButton("Save");
 		cancelButton = new JButton("Cancel");
 		saveButton.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				AlarmListItem alarm = toAlarmListItem();
 				if(!(alarm == null)) {
 					origAlarm.setFields(alarm);
 					main.saveAlarm(origAlarm);
 				}
 			}
 		});
 		cancelButton.addActionListener(new ActionListener() {
 			
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		buttonPanel.add(cancelButton, BorderLayout.LINE_START);
 		buttonPanel.add(saveButton, BorderLayout.LINE_END);
 		outsidePanel.add(buttonPanel);
 		return outsidePanel;
 	}
 	
 	public AlarmListItem toAlarmListItem() {
 		String name = nameField.getText().trim();
 		if(name.isEmpty()) {
 			JOptionPane.showMessageDialog(null, "You must name the alarm", "Error", JOptionPane.WARNING_MESSAGE);
 			return null;
 		}
 		int year = ScheduledAlarm.NO_VALUE;
 		int month = ScheduledAlarm.NO_VALUE;
 		int day = ScheduledAlarm.NO_VALUE;
 		int hour = ScheduledAlarm.NO_VALUE;
 		int minute = ScheduledAlarm.NO_VALUE;
 		String errors = new String();
 		try {
 			year = Integer.parseInt(yearPanel.getValue());
 		} catch(NumberFormatException e) {
 			errors += ", years";
 		}
 		try {
 			month = Integer.parseInt(monthPanel.getValue());
 		} catch(NumberFormatException e) {
 			errors += ", months";
 		}
 		try {
 			day = Integer.parseInt(dayPanel.getValue());
 		} catch(NumberFormatException e) {
 			errors += ", days";
 		}
 		try {
 			hour = Integer.parseInt(hourPanel.getValue());
 		} catch(NumberFormatException e) {
 			errors += ", hours";
 		}
 		try {
 			minute = Integer.parseInt(minutePanel.getValue());
 		} catch(NumberFormatException e) {
 			errors += ", minutes";
 		}
 		if(!errors.isEmpty()) {
 			JOptionPane.showMessageDialog(null, "Inappropriate value for" + errors.substring(1), "Error", JOptionPane.WARNING_MESSAGE);
 			return null;
 		}
 		ScheduledAlarm alarm = new ScheduledAlarm(year, month, day, hour, minute);
 		if(!alarm.isValid()) {
 			JOptionPane.showMessageDialog(null, "Alarm will never go off", "Error", JOptionPane.WARNING_MESSAGE);
 			return null;
 		}
 		return new AlarmListItem(name, alarm);
 		
 	}
 	
 	class DatePanel extends JPanel {
 		
 		private static final long serialVersionUID = -2399677665800558851L;
 		
 		private final JTextField valueField;
 		private final JCheckBox wildcardBtn;
 		
 		public DatePanel(String name) {
 			super();
 			add(new JLabel(name + ": "));
 			valueField = new JTextField(10);
 			wildcardBtn = new JCheckBox("Wildcard");
 			wildcardBtn.addItemListener(new ItemListener() {
 				
 				public void itemStateChanged(ItemEvent ie) {
 					valueField.setEnabled(!wildcardBtn.isSelected());
 				}
 			});
 			add(valueField);
 			add(wildcardBtn);
 		}
 		
 		public DatePanel(String name, String value, boolean isWildcard) {
 			this(name);
 			valueField.setText(value);
 			wildcardBtn.setSelected(isWildcard);
 		}
 
 		public String getValue() {
 			return valueField.getText();
 		}
 
 		public void setValue(String value) {
 			valueField.setText(value);
 		}
 
 		public boolean isWildcard() {
 			return wildcardBtn.isSelected();
 		}
 
 		public void setWildcard(boolean wildcard) {
 			wildcardBtn.setSelected(wildcard);
 		}
 		
 	}
 }
