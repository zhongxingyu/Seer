 package edu.brown.cs32.scheddar.gui;
 
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerDateModel;
 import javax.swing.SpinnerListModel;
 import javax.swing.SwingConstants;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.Document;
 
 import edu.brown.cs32.scheddar.*;
 
 
 /**
  * @author sdemane
  * 
  * In-frame panel for meeting creation and editing. Should
  * be paired with a DayPanel object.
  *
  */
 public class MeetingPane extends ScheddarSubPane {
 	private static final long serialVersionUID = 1L;
 	
 	Meeting meeting;
 	
 	JTextField name;
 	Document nameDoc;
 	JTextField description;
 	Document descripDoc;
 	
 	JList<String> unaddedGroups;
 	JList<String> addedGroups;
 	
 	JComboBox<String> nameComboBox;
 	JComboBox<String> impComboBox;
 	
 	JSpinner durationField = new JSpinner();
 	JSpinner dateField = new JSpinner();
 	JSpinner fromTime = new JSpinner();
 	JSpinner toTime = new JSpinner();
 	
 	SpinnerDateModel dateFieldModel;
 	SpinnerDateModel fromFieldModel;
 	SpinnerDateModel toFieldModel;
 	
 	HashMap<ScheddarTime,Double> timeSlots = new HashMap<ScheddarTime,Double>();
 	JList<String> selectSlots;
 	
 	JList<String> proposedTimes;
 	JButton emailAvailability;
 	
 	
 	
 	
 	
 	SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
 	
 	public MeetingPane(ScheddarPane s, Meeting m) {
 		super(s);
 		
 		meeting = m;
 		if (meeting == null)
 			meeting = new Meeting();
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 		panel.add(Box.createVerticalStrut(20));
 		
 		if (meeting.isDecided()) {
 			
 			JTextArea info = new JTextArea();
 			info.append("Name: " + meeting.getName() + "\n");
 			info.append("Description: " + meeting.getDescription() + "\n");
 			info.append("\n");
 			info.append("This meeting has been scheduled for "+meeting.getFinalTime().timeRangeToString() + "\n");
 			info.append("\n");
 			info.append("Invitees:\n");
 			for (String n : meeting.getAllNames()) {
 				info.append(n + "\n");
 			}
 			
 			panel.add(info);
 			
 			
 		} else {
 		
 		
 			// setting names and description fields
 			
 			name = new JTextField(meeting.getName(),20);
 			nameDoc = name.getDocument();
 			nameDoc.addDocumentListener(new DocumentListener() {
 
 				@Override
 				public void changedUpdate(DocumentEvent arg0) {
 					meeting.setName(name.getText());
 				}
 
 				@Override
 				public void insertUpdate(DocumentEvent arg0) {
 					meeting.setName(name.getText());
 				}
 
 				@Override
 				public void removeUpdate(DocumentEvent arg0) {
 					meeting.setName(name.getText());
 				}
 
 			});
 			
 			description = new JTextField(meeting.getDescription(),40);
 			descripDoc = description.getDocument();
 			descripDoc.addDocumentListener(new DocumentListener() {
 	
 				@Override
 				public void changedUpdate(DocumentEvent arg0) {
 					meeting.setDescription(description.getText());
 				}
 	
 				@Override
 				public void insertUpdate(DocumentEvent arg0) {
 					meeting.setDescription(description.getText());
 				}
 	
 				@Override
 				public void removeUpdate(DocumentEvent arg0) {
 					meeting.setDescription(description.getText());
 				}
 				
 			});
 			
 			
 			JPanel namePanel = new JPanel();
 			//namePanel.setLayout(new BoxLayout(namePanel,BoxLayout.X_AXIS));
 			if (meeting.getName().equals("")) {
 				namePanel.add(new JLabel("Meeting Title:"));
 				//namePanel.add(Box.createHorizontalStrut(5));
 				namePanel.add(name);
 			} else {
 				namePanel.add(new JLabel("Meeting Title: " + meeting.getName()));
 			}
 			namePanel.setAlignmentX(RIGHT_ALIGNMENT);
 			panel.add(namePanel);
 			
 			panel.add(Box.createVerticalStrut(10));
 			
 			JPanel descriPanel = new JPanel();
 			//descriPanel.setLayout(new BoxLayout(descriPanel,BoxLayout.X_AXIS));
 			descriPanel.add(new JLabel("Description:"));
 			//descriPanel.add(Box.createHorizontalStrut(5));
 			descriPanel.add(description);
 			descriPanel.setAlignmentX(RIGHT_ALIGNMENT);
 			panel.add(descriPanel);
 			panel.add(Box.createVerticalStrut(20));
 			
 			
 			
 			
 			
 			JPanel groupPanel = new JPanel(new GridLayout(1,3,10,0));
 			
 			addedGroups = new JList<String>();
 			addedGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			unaddedGroups = new JList<String>();
 			unaddedGroups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			selectSlots = new JList<String>();
 			selectSlots.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			proposedTimes = new JList<String>();
 			proposedTimes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			
 			
 			
 			// TODO: add labels above scrollpanes
 			JScrollPane unaddedGroupsPane = new JScrollPane(unaddedGroups);
 			JScrollPane addedGroupsPane = new JScrollPane(addedGroups);
 			
 			
 			
 			JPanel groupButtonsPanel = new JPanel(new GridLayout(4,1,0,3));
 			
 			JButton addGroup = new JButton("Add to Meeting -->");
 			addGroup.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					if (unaddedGroups.getSelectedValue() != null) {
 						meeting.addGroupInvolved(_scheddar.getGroupFromName(unaddedGroups.getSelectedValue()));
 						updateLists();
 					}
 				}
 			});
 			
 			JButton removeGroup = new JButton("<-- Remove from Meeting");
 			removeGroup.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					Person p;
 					Group g;
 					if (addedGroups.getSelectedValue() != null) {
 						if ((g = _scheddar.getGroupFromName(addedGroups.getSelectedValue())) != null) {
 							meeting.removeGroupInvolved(g);
 						} else if ((p = _scheddar.getPersonFromName(addedGroups.getSelectedValue())) != null) {
 							meeting.removeExtraPerson(p);
 						}
 						updateLists();
 					}
 				}
 			});
 			
 			groupButtonsPanel.add(new JPanel());
 			groupButtonsPanel.add(addGroup);
 			groupButtonsPanel.add(removeGroup);
 			groupButtonsPanel.add(new JPanel());
 			
 			groupPanel.add(unaddedGroupsPane);
 			groupPanel.add(groupButtonsPanel);
 			groupPanel.add(addedGroupsPane);
 			
 			groupPanel.setAlignmentX(RIGHT_ALIGNMENT);
 			panel.add(groupPanel);
 			panel.add(Box.createVerticalStrut(20));
 			
 			JPanel individualPanel = new JPanel(new GridLayout(1,3,8,0));
 			nameComboBox = new JComboBox<String>();
 			nameComboBox.setEditable(true);
 			impComboBox = new JComboBox<String>(Scheddar.importanceArrayLabels);
 			impComboBox.setSelectedIndex(2);
 			JButton addPersonButton = new JButton("Add to Meeting ^^");
 			addPersonButton.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					Person p = _scheddar.getPersonFromName((String)nameComboBox.getSelectedItem());
 					if (p != null) {
 						meeting.addExtraPerson(p, Scheddar.importanceArrayValues[impComboBox.getSelectedIndex()]);
 						updateLists();
 					} else {
 						PopUps.popUpPersonNotFound();
 					}
 				}
 			});
 			
 			individualPanel.add(nameComboBox);
 			individualPanel.add(impComboBox);
 			individualPanel.add(addPersonButton);
 			individualPanel.setAlignmentX(RIGHT_ALIGNMENT);
 			panel.add(individualPanel);
 			panel.add(Box.createVerticalStrut(20));
 			
 		
 		
 			// submitting time ranges
 			JPanel schedulePanel = new JPanel();
 			schedulePanel.setLayout(new BoxLayout(schedulePanel,BoxLayout.Y_AXIS));
 			JPanel durationPanel = new JPanel();
 			String[] durations = new String[12];
 			for (int i = 0; i < 6; i++) {
 				durations[i*2] = "0" + i + ":30";
 				durations[i*2+1] = "0" + new Integer(i+1) + ":00";
 			}
 			durationField.setModel(new SpinnerListModel(durations));
 			durationField.setValue("01:00");
 			durationPanel.add(new JLabel("Required meeting duration (HH:MM):"));
 			durationPanel.add(durationField);
 			durationPanel.setAlignmentX(RIGHT_ALIGNMENT);
 			schedulePanel.add(durationPanel);
 			schedulePanel.add(Box.createVerticalStrut(10));
 			
 			
 			
 			JLabel meetingLabel = new JLabel("Select range of possible meeting times:");
 			meetingLabel.setAlignmentX(RIGHT_ALIGNMENT);
 			schedulePanel.add(meetingLabel);
 			schedulePanel.add(Box.createVerticalStrut(10));
 			JPanel timeRangePanel = new JPanel();
 			timeRangePanel.setLayout(new GridLayout(4,2,5,2));
 			dateFieldModel = new SpinnerDateModel();
 			dateFieldModel.setValue(new Date());
 			dateFieldModel.setCalendarField(Calendar.DAY_OF_YEAR);			
 			
 			fromFieldModel = new SpinnerDateModel();
 			toFieldModel = new SpinnerDateModel();
 			try {
 				fromFieldModel.setValue(timeFormat.parse(_scheddar.getStartHour()));
 				toFieldModel.setValue(timeFormat.parse(_scheddar.getEndHour()));
 			} catch (ParseException e) {
 				System.out.println("parse error meetingpanel");
 			}
 			
 			fromFieldModel.setCalendarField(Calendar.MINUTE);
 			toFieldModel.setCalendarField(Calendar.MINUTE);
 			
 			dateField.setModel(dateFieldModel);
 			fromTime.setModel(fromFieldModel);
 			toTime.setModel(toFieldModel);
 			
 			
 			dateField.setEditor(new JSpinner.DateEditor(dateField, "MM/dd/yy"));
 			fromTime.setEditor(new JSpinner.DateEditor(fromTime, "HH:mm"));
 			toTime.setEditor(new JSpinner.DateEditor(toTime, "HH:mm"));
 
 			
 			
 			JButton addRange = new JButton("Calculate meeting times");
 			addRange.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					Calendar date,from,to,difference;
 					date = Calendar.getInstance();
 					from = Calendar.getInstance();
 					to = Calendar.getInstance();
 					difference = Calendar.getInstance();
 					
 					
 					date.setTime(dateFieldModel.getDate());
 					from.setTime(fromFieldModel.getDate());
 					to.setTime(toFieldModel.getDate());
 					difference.setTimeInMillis(to.getTimeInMillis() - from.getTimeInMillis());
 		//			System.out.println("Day of Month : " + date.get(Calendar.DAY_OF_MONTH));
 //					System.out.println("Start Hour : " + from.get(Calendar.HOUR_OF_DAY));
 //					System.out.println("Start Minute : " + from.get(Calendar.MINUTE));
 //					System.out.println("End Hour : " + to.get(Calendar.HOUR_OF_DAY));
 //					System.out.println("End Minutes : " + to.get(Calendar.MINUTE));
 //					
 //					System.out.println("Difference Minutes : " + difference.get(Calendar.MINUTE));
 					//System.out.println("Difference Hours : " + difference.get(Calendar.HOUR_OF_DAY));
 					
 					int diffHours = difference.get(Calendar.HOUR_OF_DAY) + 5;
 					if(diffHours>=24){
 						diffHours = diffHours - 24;
 					}
 
 					
					ScheddarTime range = new ScheddarTime(from.get(Calendar.HOUR_OF_DAY),from.get(Calendar.MINUTE), difference.get(Calendar.MINUTE) + diffHours*60,date.get(Calendar.DAY_OF_WEEK)-1,date.get(Calendar.DAY_OF_MONTH),date.get(Calendar.MONTH)+1,date.get(Calendar.YEAR),false);
 					
 					Calendar d = new GregorianCalendar();
 					try {
 						d.setTime(timeFormat.parse(durationField.getValue().toString()));
 						int duration = d.get(Calendar.HOUR_OF_DAY) * 60 + d.get(Calendar.MINUTE);
 						
 						timeSlots = meeting.recommendMeetingTimes(range, duration);
 					} catch (ParseException e) {
 						// do nothing
 						System.out.println("parse exception D:");
 					}
 					updateLists();
 					
 				}
 			});
 			
 			timeRangePanel.add(new JLabel("Date:",SwingConstants.RIGHT));
 			timeRangePanel.add(dateField);
 			timeRangePanel.add(new JLabel("Earliest start time:",SwingConstants.RIGHT));
 			timeRangePanel.add(fromTime);
 			timeRangePanel.add(new JLabel("Latest end time:",SwingConstants.RIGHT));
 			timeRangePanel.add(toTime);
 			timeRangePanel.add(new JPanel());
 			timeRangePanel.add(addRange);
 			
 			
 			// making panel for choosing time slots
 			
 			
 			JScrollPane slotListPane = new JScrollPane(selectSlots);			
 			
 			JPanel slotSelectionPanel = new JPanel(new GridLayout(1,2,5,0));
 			slotSelectionPanel.add(timeRangePanel);
 			slotSelectionPanel.add(slotListPane);
 			
 			slotSelectionPanel.setAlignmentX(RIGHT_ALIGNMENT);
 			schedulePanel.add(slotSelectionPanel);
 			schedulePanel.add(Box.createVerticalStrut(5));
 			
 			JPanel updatePanel = new JPanel();
 			updatePanel.setLayout(new BoxLayout(updatePanel,BoxLayout.X_AXIS));
 			JButton pickSlots = new JButton("Select time slot");
 			pickSlots.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					String slot = selectSlots.getSelectedValue();
 					ScheddarTime time = null;
 					if (slot != null) {
 						for (ScheddarTime t : timeSlots.keySet()) {
 							if (slot.startsWith(t.timeRangeToString())) {
 								meeting.addProposedTime(t);
 								time = t;
 							}
 						}
 					}
 					updateLists();
 					_scheddar.addMeeting(meeting);
 					_scheddarPane._calendar.switchToMeeting(time, meeting);
 				}
 			});
 			updatePanel.add(Box.createHorizontalGlue());
 			updatePanel.add(pickSlots);
 			
 			updatePanel.setAlignmentX(RIGHT_ALIGNMENT);
 			schedulePanel.add(updatePanel);
 			schedulePanel.add(Box.createVerticalStrut(20));
 			
 			
 			
 			
 			// readout of selected time slots
 			
 			JPanel proposedTimePanel = new JPanel(new GridLayout(1,2));
 			
 			proposedTimePanel.add(new JPanel());
 			proposedTimePanel.add(new JScrollPane(proposedTimes));
 			
 			JPanel optionsPanel = new JPanel();
 			
 			JButton removeTimeButton = new JButton("Remove proposed time");
 			removeTimeButton.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					ScheddarTime time = null;
 					for (ScheddarTime t : meeting.getProposedTimes()) {
 						if (proposedTimes.getSelectedValue().equals(t.timeRangeToString())) {
 							time = t;
 							break;
 						}
 					}
 					meeting.removeProposedTime(time);
 					updateLists();
 				}
 			});
 			
 			emailAvailability = new JButton("Request Availabilities");
 			emailAvailability.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					for (String name : meeting.getAllNames()) {
 						Person p = _scheddar.getPersonFromName(name);
 						_scheddar.sendMeetingRequestEmail(p.getEmail(), name, meeting);
 						emailAvailability.setEnabled(false);
 					}
 					
 				}
 			});
 			
 			JButton scheduleNow = new JButton("Schedule Now");
 			scheduleNow.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					String proposedTime = proposedTimes.getSelectedValue();
 					if (proposedTime != null) {
 						int retval = JOptionPane.showConfirmDialog(MeetingPane.this, "Would you like to finalize this meeting for "+proposedTime+"?");
 						if (retval == JOptionPane.OK_OPTION) {
 							for (ScheddarTime t : meeting.getProposedTimes()) {
 							//	System.out.println(proposedTimes.getSelectedValue());
 							//	System.out.println(t.timeRangeToString());
 								if (proposedTimes.getSelectedValue().equals(t.timeRangeToString())) {
 									meeting.user = _scheddar.getUsername();
 									meeting.password = _scheddar.getPassword();
 //									meeting.setFinalTime(new ScheddarTime(t.getStartHour(),t.getStartMinutes(),t.getDuration(),t.getDayOfWeek(),t.getDay(),t.getMonth()-1,t.getYear(),t.isRecurring()));
 									meeting.setFinalTime(t);
 									break;
 								}
 							}
 
 //							MeetingPane.this.removeAll();
 //							JPanel panel = new JPanel();
 //							panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 //							panel.add(Box.createVerticalStrut(20));
 //							
 //							JTextArea info = new JTextArea();
 //							info.append("Name: " + meeting.getName() + "\n");
 //							info.append("Description: " + meeting.getDescription() + "\n");
 //							info.append("\n");
 //							info.append("This meeting has been scheduled for "+meeting.getFinalTime().timeRangeToString() + "\n");
 //							info.append("\n");
 //							info.append("Invitees:\n");
 //							for (String n : meeting.getAllNames()) {
 //								info.append(n + "\n");
 //							}
 //							
 //							panel.add(info);
 //							MeetingPane.this.add(panel);
 //							MeetingPane.this.revalidate();
 							_scheddar.addMeeting(meeting);
 							_scheddarPane._calendar.switchToMeeting(meeting.getFinalTime(), meeting);
 
 						}
 					}
 				}
 			});
 			
 			optionsPanel.add(removeTimeButton);
 			optionsPanel.add(emailAvailability);
 			optionsPanel.add(scheduleNow);
 			
 			proposedTimePanel.setAlignmentX(RIGHT_ALIGNMENT);
 			optionsPanel.setAlignmentX(RIGHT_ALIGNMENT);
 			
 			schedulePanel.add(proposedTimePanel);
 			schedulePanel.add(optionsPanel);
 		
 			schedulePanel.setAlignmentX(RIGHT_ALIGNMENT);
 			panel.add(schedulePanel);
 			panel.add(Box.createVerticalStrut(20));
 		}
 		
 		
 		updateLists();
 		
 		
 		add(panel);
 	}
 	
 	
 	
 	
 	private void updateLists() {
 		DefaultListModel<String> addedList = new DefaultListModel<String>();
 		DefaultListModel<String> unaddedList = new DefaultListModel<String>();
 		DefaultComboBoxModel<String> peopleList = new DefaultComboBoxModel<String>();
 		DefaultListModel<String> timeslotList = new DefaultListModel<String>();
 		DefaultListModel<String> proposedTimesList = new DefaultListModel<String>();
 		
 		for (Group g : meeting.getGroupsInvolved()) {
 			addedList.addElement(g.getName());
 		}
 		
 		for (Person p : meeting.getExtraPeopleToImportance().keySet()) {
 			addedList.addElement(p.getFullName());
 		}
 		
 		for (String n : _scheddar.getGroups().keySet()) {
 			if (!addedList.contains(n)) {
 				unaddedList.addElement(n);
 			}
 		}
 		
 		for (String n : _scheddar.getPersons().keySet()) {
 			if (!addedList.contains(n)) {
 				peopleList.addElement(n);
 			}
 		}
 		
 		HashSet<String> proposedTimeStrings = new HashSet<String>();
 		
 		for (int i = 0; i < meeting.getProposedTimes().size(); i++) {
 			ScheddarTime t = meeting.getProposedTimes().get(i);
 			proposedTimeStrings.add(t.timeRangeToString());
 			proposedTimesList.addElement(t.timeRangeToString());
 		}
 		
 		
 		
 		
 		
 		LinkedList<Map.Entry<ScheddarTime,Double>> orderedTimes = new LinkedList<Map.Entry<ScheddarTime,Double>>();
 		orderedTimes.addAll(timeSlots.entrySet());
 		Comparator<Map.Entry<ScheddarTime,Double>> c = new Comparator<Map.Entry<ScheddarTime,Double>>() {
 			@Override
 			public int compare(Entry<ScheddarTime, Double> o1,
 					Entry<ScheddarTime, Double> o2) {
 				double v1 = o1.getValue();
 				double v2 = o2.getValue();
 				
 				if (v1 > v2) {
 					return -1;
 				} else if (v1 < v2) {
 					return 1;
 				} else {
 					return 0;
 				}
 			}
 		};
 		
 		Collections.sort(orderedTimes, c);
 		
 		while (!orderedTimes.isEmpty()) {
 			Map.Entry<ScheddarTime, Double> e = orderedTimes.pollLast();
 			if (!proposedTimeStrings.contains(e.getKey().timeRangeToString()))
 				timeslotList.addElement(e.getKey().timeRangeToString() + "    ["+e.getValue()+"]");
 		}
 		
 		
 		
 		addedGroups.setModel(addedList);
 		unaddedGroups.setModel(unaddedList);
 		nameComboBox.setModel(peopleList);
 		selectSlots.setModel(timeslotList);
 		proposedTimes.setModel(proposedTimesList);
 	}
 	
 	
 	public Dimension getPreferredSize() {
 		Dimension d = _scheddarPane.getPreferredSize();
 		return new Dimension(d.width * 3/4 - 25, d.height);
 	}
 	
 	
 }
