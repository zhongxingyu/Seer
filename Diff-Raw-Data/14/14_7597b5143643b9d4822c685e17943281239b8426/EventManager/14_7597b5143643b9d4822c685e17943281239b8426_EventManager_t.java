 package views.gui;
 
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.border.EmptyBorder;
 
 import controllers.EventManagerController;
 import models.Event;
 import models.EventGroup;
 import models.EventPriority;
 import models.Organizer;
 import models.Resource;
 import models.User;
 import controllers.DatePickerController;
 import utils.DateUtils;
 import views.gui.components.LimitedDocument;
 
 public class EventManager extends JFrame {
 
 	private static final long serialVersionUID = -1113743261857828270L;
 	
 	private JPanel contentPane;
 	private JTextField txtfldTitle;
 	private JTextArea txtrComment;
 	private java.util.Calendar endCalendar;
 	private java.util.Calendar startCalendar;
     private java.util.Calendar dateUntilCalendar;
 	private JComboBox<String> startTimeBox;
 	private JComboBox<String> endTimeBox;
 	private JComboBox<EventPriority> importanceBox;
     private User currentUser;
     private JList<Resource> list = null;
     private Event event = null;
 
 
     public JCheckBox getChckbxSun() {
         return chckbxSun;
     }
 
     private JCheckBox chckbxMonday;
     private JCheckBox chckbxTuesday;
     private JCheckBox chckbxWed;
     private JCheckBox chckbxTh;
     private JCheckBox chckbxFri;
     private JCheckBox chckbxSat;
     private JCheckBox chckbxSun;
 
     /**
 	 * For testing purposes.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					EventManager frame = new EventManager(new User("asdf", "asdf".toCharArray()),null);
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
      * @param currentUser
      *
      * @param event If null then new event will be added. Otherwise given event will be edited;
      */
 	public EventManager(User currentUser, Event event) {
         this.currentUser = currentUser;
         // there is similar if at the end of the constructor because some values (calendars) had to be set before
         // creating components and some (text areas, list) after.
         if(event==null) {
             endCalendar = Organizer.getInstance().getCurrentUser().getUserProfile().getState().getFirstDay();
             startCalendar = Organizer.getInstance().getCurrentUser().getUserProfile().getState().getFirstDay();
         }
         else {
             endCalendar = event.getEndTime();
             startCalendar = event.getStartTime();
             this.event = event;
         }
         dateUntilCalendar = Organizer.getInstance().getCurrentUser().getUserProfile().getState().getFirstDay();
         setResizable(false);
 		setBounds(100, 100, 397, 529);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		JLabel lblTitle = new JLabel("Title: ");
 		lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		txtfldTitle = new JTextField(new LimitedDocument(EventGroup.getMaxTitleLength()), "", 10);
 		
 		JLabel lblComment = new JLabel("Comment:");
 		lblComment.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		JPanel panel = new JPanel();
 		
 		JPanel panel_3 = new JPanel();
 		
 		JLabel lblImportance = new JLabel("Importance:");
 		lblImportance.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		importanceBox = new JComboBox<>(new DefaultComboBoxModel<EventPriority>(EventPriority.values()));
 		if(isEditing())
 			importanceBox.setSelectedItem(event.getPriorityObject());
 		
 		JPanel panel_4 = new JPanel();
 		
 		JPanel panel_5 = new JPanel();
 		
 		txtrComment = new JTextArea();
 		txtrComment.setWrapStyleWord(true);
 		txtrComment.setLineWrap(true);
 		txtrComment.setFont(new Font("Monospaced", Font.PLAIN, 12));
 		JScrollPane commentHolder = new JScrollPane(txtrComment);
 		
 		GroupLayout gl_contentPane = new GroupLayout(contentPane);
 		gl_contentPane.setHorizontalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addComponent(lblTitle)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(txtfldTitle, GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE))
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addComponent(lblImportance)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(importanceBox, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap())
 				.addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
 				.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
 				.addComponent(panel_5, GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
 				.addComponent(commentHolder, GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
 				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addComponent(lblComment, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
 					.addContainerGap(295, Short.MAX_VALUE))
 		);
 		gl_contentPane.setVerticalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblTitle)
 						.addComponent(txtfldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(lblComment)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(commentHolder, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel_3, GroupLayout.PREFERRED_SIZE, 51, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblImportance)
 						.addComponent(importanceBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel_5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 		);
 		
 		JButton btnAddEvent = new JButton(isEditing()?"Save":"Add Event");
 		btnAddEvent.addActionListener(new EventManagerController(this));
 		
 		JButton btnClose = new JButton("Close");
 		btnClose.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				dispose();
 			}
 		});
 		GroupLayout gl_panel_5 = new GroupLayout(panel_5);
 		gl_panel_5.setHorizontalGroup(
 			gl_panel_5.createParallelGroup(Alignment.LEADING)
 				.addGroup(Alignment.TRAILING, gl_panel_5.createSequentialGroup()
 					.addComponent(btnAddEvent)
 					.addPreferredGap(ComponentPlacement.RELATED, 189, Short.MAX_VALUE)
 					.addComponent(btnClose))
 		);
 		gl_panel_5.setVerticalGroup(
 			gl_panel_5.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_5.createSequentialGroup()
 					.addGroup(gl_panel_5.createParallelGroup(Alignment.BASELINE)
 						.addComponent(btnClose)
 						.addComponent(btnAddEvent))
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		panel_5.setLayout(gl_panel_5);
 		
 		JButton btnAddResource = new JButton("Add resource");
 		btnAddResource.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				new AddResourceDialog(getResourcesJList()).setVisible(true);
 			}
 		});
 		
 		JButton btnRemoveresource = new JButton("Remove resource");
         btnRemoveresource.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 ((DefaultListModel<Resource>)list.getModel()).removeElement(list.getSelectedValue());
             }
         });
 		list = new JList<>(new DefaultListModel<Resource>());
 		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
 		gl_panel_4.setHorizontalGroup(
 			gl_panel_4.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_4.createSequentialGroup()
 					.addComponent(btnAddResource)
 					.addPreferredGap(ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
 					.addComponent(btnRemoveresource))
 				.addComponent(list, GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
 		);
 		gl_panel_4.setVerticalGroup(
 			gl_panel_4.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_4.createSequentialGroup()
 					.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
 						.addComponent(btnAddResource)
 						.addComponent(btnRemoveresource))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(list, GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
 		);
 		panel_4.setLayout(gl_panel_4);
 
         if(isEditing()==false){
             JLabel lblRepeatUntil = new JLabel("Repeat until:");
             lblRepeatUntil.setFont(new Font("Tahoma", Font.PLAIN, 13));
 
             chckbxMonday = new JCheckBox("Mon");
 
             chckbxTuesday = new JCheckBox("Tue");
 
             chckbxWed = new JCheckBox("Wed");
 
             chckbxTh = new JCheckBox("Thu");
 
             chckbxFri = new JCheckBox("Fri");
 
             chckbxSat = new JCheckBox("Sat");
 
             chckbxSun = new JCheckBox("Sun");
 
             final JTextPane repeatUntilDate = JDateFactory.JDate(dateUntilCalendar);
             repeatUntilDate.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent arg0) {
                     DatePicker.getInstance(new DatePickerController() {
 
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             dateUntilCalendar = getGregorianCalendar();
                             repeatUntilDate.setText(DateUtils.dateDisplay(dateUntilCalendar));
                             repeatUntilDate.insertIcon(new ImageIcon(Calendar.SRC_MAIN_IMAGES_DATE_PICKER_ICON_GIF));
                             super.actionPerformed(e);
                         }
                     }, EventManager.this.currentUser).setVisible(true);
                 }
             });
 
             GroupLayout gl_panel_3 = new GroupLayout(panel_3);
             gl_panel_3.setHorizontalGroup(
                 gl_panel_3.createParallelGroup(Alignment.LEADING)
                     .addGroup(gl_panel_3.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
                             .addGroup(gl_panel_3.createSequentialGroup()
                                 .addComponent(lblRepeatUntil)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(repeatUntilDate, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                             .addGroup(gl_panel_3.createSequentialGroup()
                                 .addComponent(chckbxMonday)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(chckbxTuesday)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(chckbxWed)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(chckbxTh)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(chckbxFri)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(chckbxSat)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(chckbxSun)))
                         .addContainerGap(72, Short.MAX_VALUE))
             );
             gl_panel_3.setVerticalGroup(
                 gl_panel_3.createParallelGroup(Alignment.LEADING)
                     .addGroup(gl_panel_3.createSequentialGroup()
                         .addGroup(gl_panel_3.createParallelGroup(Alignment.TRAILING)
                             .addComponent(lblRepeatUntil)
                             .addComponent(repeatUntilDate, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
                             .addComponent(chckbxMonday)
                             .addComponent(chckbxTuesday)
                             .addComponent(chckbxWed)
                             .addComponent(chckbxTh)
                             .addComponent(chckbxFri)
                             .addComponent(chckbxSat)
                             .addComponent(chckbxSun))
                         .addContainerGap(22, Short.MAX_VALUE))
             );
             panel_3.setLayout(gl_panel_3);
         }
 		JPanel panel_1 = new JPanel();
 		
 		JPanel panel_2 = new JPanel();
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
 		);
 		gl_panel.setVerticalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
 				.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
 		);
 		
 		JLabel lblEnd = new JLabel("End:");
 		lblEnd.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		final JTextPane endDatePicker = JDateFactory.JDate(endCalendar);
 		endDatePicker.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				DatePicker.getInstance(new DatePickerController() {
 
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						endCalendar = getGregorianCalendar();
 						endDatePicker.setText(DateUtils.dateDisplay(endCalendar));
 						endDatePicker.insertIcon(new ImageIcon(Calendar.SRC_MAIN_IMAGES_DATE_PICKER_ICON_GIF));
 						super.actionPerformed(e);
 					}
 				}, EventManager.this.currentUser).setVisible(true);
 			}
 		});
 		
 		endTimeBox = new JComboBox<>();
 		endTimeBox.setModel(new DefaultComboBoxModel<>(new String[] {"00:00", "00:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30", "04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00", "21:30", "22:00", "22:30", "23:00", "23:30"}));
 		endTimeBox.setSelectedIndex(17);
 		endTimeBox.setEditable(true);
 		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
 		gl_panel_2.setHorizontalGroup(
 			gl_panel_2.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_2.createSequentialGroup()
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblEnd)
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addComponent(endDatePicker, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(endTimeBox, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)))
 					.addContainerGap(51, Short.MAX_VALUE))
 		);
 		gl_panel_2.setVerticalGroup(
 			gl_panel_2.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_2.createSequentialGroup()
 					.addComponent(lblEnd)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addComponent(endTimeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(endDatePicker, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		panel_2.setLayout(gl_panel_2);
 		
 		JLabel lblStart = new JLabel("Start:");
 		lblStart.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		final JTextPane startDatePicker = JDateFactory.JDate(startCalendar);
 		startDatePicker.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				DatePicker.getInstance(new DatePickerController() {
 					
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						startCalendar = getGregorianCalendar();
 						startDatePicker.setText(DateUtils.dateDisplay(startCalendar));
 						startDatePicker.insertIcon(new ImageIcon(Calendar.SRC_MAIN_IMAGES_DATE_PICKER_ICON_GIF));
 						super.actionPerformed(e);
 					}
 				}, EventManager.this.currentUser).setVisible(true);
 			}
 		});
 		
 		startTimeBox = new JComboBox<>();
 		startTimeBox.setEditable(true);
 		startTimeBox.setModel(new DefaultComboBoxModel<>(new String[] {"00:00", "00:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30", "04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00", "21:30", "22:00", "22:30", "23:00", "23:30"}));
 		startTimeBox.setSelectedIndex(16);
 		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
 		gl_panel_1.setHorizontalGroup(
 			gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_1.createSequentialGroup()
 					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblStart)
 						.addGroup(gl_panel_1.createSequentialGroup()
 							.addComponent(startDatePicker, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(startTimeBox, GroupLayout.PREFERRED_SIZE, 62, GroupLayout.PREFERRED_SIZE)))
 					.addContainerGap(52, Short.MAX_VALUE))
 		);
 		gl_panel_1.setVerticalGroup(
 			gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_1.createSequentialGroup()
 					.addComponent(lblStart)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
 						.addComponent(startTimeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(startDatePicker, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		panel_1.setLayout(gl_panel_1);
 		panel.setLayout(gl_panel);
 		contentPane.setLayout(gl_contentPane);
         if(event!=null){
             txtfldTitle.setText(event.getTitle());
             txtrComment.setText(event.getComment());
             for(Resource resource : event.getResourceList())
                 ((DefaultListModel<Resource>)list.getModel()).addElement(resource);
             int h = startCalendar.get(java.util.Calendar.HOUR_OF_DAY);
             int m = startCalendar.get(java.util.Calendar.MINUTE);
             startTimeBox.setSelectedItem(
                    (h<=9?"0":"")+h+":"+
                             (m<=9?"0":"")+m);
             h = endCalendar.get(java.util.Calendar.HOUR_OF_DAY);
             m = endCalendar.get(java.util.Calendar.MINUTE);
             endTimeBox.setSelectedItem(
                    (h<=9?"0":"")+h+":"+
                             (m<=9?"0":"")+m);
         }
 		setVisible(true);
 	}
 	public String getEventTitle() {
 		return txtfldTitle.getText();
 	}
 	public String getEventComment() {
 		return txtrComment.getText();
 	}
 	public EventPriority getEventPriority() {
 		return (EventPriority) importanceBox.getSelectedItem();
 	}
 	private boolean checkIsTimeIsValid(String time){
 		if(time.length()==5&&time.charAt(2)==':'){
 			String hour = time.substring(0,2);
 			String minutes = time.substring(3,5);
 			int h,m;
 			try{
 				h = Integer.parseInt(hour);
 				m = Integer.parseInt(minutes);	
 			} catch(NumberFormatException exception){
 				return false;
 			}
 			if(h>=0&&h<=24&&m>=0&&m<=59)
 				return true;
 		}
 		return false;
 	}
 	public java.util.Calendar getStartCalendar() {
 		String time = (String) getStartTimeBox().getSelectedItem();
 		if(checkIsTimeIsValid(time)){
 			int h = Integer.parseInt(time.substring(0,2));
 			int m = Integer.parseInt(time.substring(3,5));
 			startCalendar.set(java.util.Calendar.HOUR_OF_DAY, h);
 			startCalendar.set(java.util.Calendar.MINUTE, m);
 		}
         else
             return null;
 		return (java.util.Calendar) startCalendar.clone();
 	}
 	public java.util.Calendar getEndCalendar() {
 		String time = (String) getEndTimeBox().getSelectedItem();
 		if(checkIsTimeIsValid(time)){
 			int h = Integer.parseInt(time.substring(0, 2));
 			int m = Integer.parseInt(time.substring(3, 5));
 			endCalendar.set(java.util.Calendar.HOUR_OF_DAY, h);
 			endCalendar.set(java.util.Calendar.MINUTE, m);
 		}
         else
             return null;
 		return (java.util.Calendar) endCalendar.clone();
 	}
 
     public java.util.Calendar getDateUntilCalendar() {
         dateUntilCalendar.set(java.util.Calendar.HOUR,23);
         dateUntilCalendar.set(java.util.Calendar.MINUTE,59);
         dateUntilCalendar.set(java.util.Calendar.SECOND,59);
         return (java.util.Calendar) dateUntilCalendar.clone();
     }
 
     protected JComboBox<String> getStartTimeBox() {
 		return startTimeBox;
 	}
 	protected JComboBox<String> getEndTimeBox() {
 		return endTimeBox;
 	}
 	protected JList<Resource> getResourcesJList() {
 		return list;
 	}
 	public List<Resource> getResources() {
 		ListModel<Resource> model = getResourcesJList().getModel();
         List<Resource> ret = new LinkedList<>();
         for(int i=0;i<model.getSize();i++)
             ret.add(model.getElementAt(i));
         return ret;
 	}
     public boolean isEditing(){
         return event!=null;
     }
     public Event getEvent(){
         return event;
     }
     public boolean isMondayRepeat() {
         return chckbxMonday!=null&&chckbxMonday.isSelected();
     }
 
     public boolean isTuesdayRepeat() {
         return chckbxTuesday!=null&&chckbxTuesday.isSelected();
     }
 
     public boolean isWednesdayRepeat() {
         return chckbxWed!=null&&chckbxWed.isSelected();
     }
 
     public boolean isThursdayRepeat() {
         return chckbxTh!=null&&chckbxTh.isSelected();
     }
 
     public boolean isFridayRepeat() {
         return chckbxFri!=null&&chckbxFri.isSelected();
     }
 
     public boolean isSaturdayRepeat() {
         return chckbxSat!=null&&chckbxSat.isSelected();
     }
 
     public boolean isSundayRepeat() {
         return chckbxSun!=null&&chckbxSun.isSelected();
     }
 
     public boolean isRepeating(){
         return isMondayRepeat()||isTuesdayRepeat()||isWednesdayRepeat()
                 ||isThursdayRepeat()||isFridayRepeat()||isSaturdayRepeat()||isSundayRepeat();
     }
 }
