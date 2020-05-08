 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.sql.Timestamp;
 
 public class EditTask extends JPanel
 {
 	JTextField name;
 	JTextArea desc;
 	JComboBox month, day, year, hour, minute, ampm, status, priority;
 	public static JFrame frame;
 	JScrollPane dscScroll;
 	final int id;
 
 	public EditTask(Task tsk)
 	{
 		super(new GridLayout(1, 1));
 
 		id = tsk.id;
 		name = new JTextField();
 		name.setText(tsk.name);
 		desc = new JTextArea();
 		desc.setLineWrap(true);
 		desc.setText(tsk.desc);
 		dscScroll = new JScrollPane(desc);
 
 		String tempDate = tsk.startTime.toString();
 		int tempMonth = Integer.parseInt(tempDate.substring(5,7))-2;
 		int tempDay = Integer.parseInt(tempDate.substring(8, 10))-1;
 		int tempYear = Integer.parseInt(tempDate.substring(0,4));
 		int tempHours = Integer.parseInt(tempDate.substring(11,13));
 
 		String[] months = { "January", "February", "March","April","May","June","July","August","September","October","November","December"};
 		month = new JComboBox(months);
 		month.setSelectedIndex(tempMonth);
 
 		Integer[] days = new Integer[31];
 		for( int i=0; i<=30; i++ )
 			days[i] = i+1;
 		day = new JComboBox(days);
		day.setSelectedIndex(tempDay-1);
 
 		Integer[] years = new Integer[100];
 		for( int i=0; i<100; i++ )
 			years[i] = 2012+i;
 		year = new JComboBox(years);
 		//year.setSelectedIndex(tempYear);
 
 		Integer[] hours = new Integer[12];
 		for( int i=0; i<12; i++)
 			hours[i] = i+1;
 		hour = new JComboBox(hours);
 
 		boolean pmcheck = false;
 		if(tempHours!= 0 && tempHours > 12){
 			tempHours -= 12;
 			pmcheck = true;
 		}
 		//System.out.println(">>>>>>>>>>>>>>>"+tempHours);
 		hour.setSelectedIndex(tempHours-1);
 
 		String[] minutes = new String[60];
 		for( int i=0; i<60; i++ )
 		{	if( i > 9 )
 			minutes[i] = Integer.toString(i);
 			else
 			minutes[i] = "0"+Integer.toString(i);
 		}
 		minute = new JComboBox(minutes);
 		//minute.setSelectedIndex(tempMins);
 
 		String[] time = { "AM", "PM"};
 		ampm = new JComboBox(time);
 		if(pmcheck)
 			ampm.setSelectedIndex(1);
 		
 		String[] status0 = { "To Do", "Doing", "Done" };
 		status = new JComboBox(status0);
 		status.setSelectedIndex(tsk.status);
 		//System.out.println("status:::::::::::::"+tsk.status);
 
 		String[] priority0 = { "Very High", "High", "Normal", "Low", "Very Low" };
 		priority = new JComboBox(priority0);
 		priority.setSelectedIndex(tsk.priority);
 
 		JTabbedPane tabbedPane = new JTabbedPane();
         ImageIcon icon = createImageIcon("tray.jpg");
         
         JComponent panel1 = makeTextPanel();
 		panel1.setLayout(new GridLayout(1,1));
         tabbedPane.addTab("Tasks", icon, panel1,
                 "Tasks");
 		JPanel tasksPanelMain = new JPanel(new BorderLayout());
 			tasksPanelMain.add(new JLabel(" "), BorderLayout.PAGE_START);
 			tasksPanelMain.add(new JLabel(" "), BorderLayout.PAGE_END);
 			tasksPanelMain.add(new JLabel("   "), BorderLayout.LINE_START);
 			tasksPanelMain.add(new JLabel("   "), BorderLayout.LINE_END);
 			JPanel taskPane = new JPanel(new BorderLayout());
 				JPanel taskHead = new JPanel(new GridLayout(1,1));
 					JPanel taskNameLabel = new JPanel(new BorderLayout());
 						taskNameLabel.add(new JLabel("Task Name:   \t"), BorderLayout.LINE_START);
 						taskNameLabel.add(name, BorderLayout.CENTER);
 					taskHead.add(taskNameLabel);
 				taskPane.add(taskHead, BorderLayout.PAGE_START);
 				JPanel taskBody = new JPanel(new GridLayout(2, 1));
 					JPanel taskDescMain = new JPanel(new BorderLayout());
 						taskDescMain.add(new JLabel(" "), BorderLayout.PAGE_START);
 						JPanel taskDescLabel = new JPanel(new FlowLayout());
 						taskDescMain.add(new JLabel("Description:   \t"), BorderLayout.LINE_START);
 						taskDescMain.add(dscScroll, BorderLayout.CENTER);
 					taskBody.add(taskDescMain);
 					JPanel taskProp = new JPanel(new GridLayout(2,1));
 						JPanel taskDeadlineMain = new JPanel(new BorderLayout());
 							taskDeadlineMain.add(new JLabel("Deadline: \t"), BorderLayout.LINE_START);
 							JPanel taskDeadlineInfo = new JPanel(new GridLayout(2,1));
 								JPanel taskDate = new JPanel(new FlowLayout()); //DATE
 									taskDate.add(month);
 									taskDate.add(day);
 									taskDate.add(year);
 								taskDeadlineInfo.add(taskDate);
 								JPanel taskTime = new JPanel(new FlowLayout()); //TIME
 									taskTime.add(hour);
 									taskTime.add(minute);
 									taskTime.add(ampm);
 								taskDeadlineInfo.add(taskTime);
 							taskDeadlineMain.add(taskDeadlineInfo, BorderLayout.CENTER);
 						taskProp.add(taskDeadlineMain);
 						JPanel taskButtons = new JPanel(new GridLayout(3,1));
 							JPanel taskStatusMain = new JPanel( new BorderLayout());
 								taskStatusMain.add(new JLabel("Status:   \t"), BorderLayout.LINE_START);
 								taskStatusMain.add(status, BorderLayout.CENTER);
 							taskButtons.add(taskStatusMain);
 							JPanel taskPriorityMain = new JPanel( new BorderLayout());
 								taskPriorityMain.add(new JLabel("Priority:  \t"), BorderLayout.LINE_START);
 								taskPriorityMain.add(priority, BorderLayout.CENTER);
 							taskButtons.add(taskPriorityMain);
 							JButton save = new JButton("Save Changes");
 							save.addActionListener(new saveChanges_Action());
 							taskButtons.add(save);
 						taskProp.add(taskButtons);
 					taskBody.add(taskProp);
 				taskPane.add(taskBody);
 			tasksPanelMain.add(taskPane, BorderLayout.CENTER);
 		panel1.add(tasksPanelMain);
 
 	add(tabbedPane);
         
     //The following line enables to use scrolling tabs.
     tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 
 	}
 
 	 protected JComponent makeTextPanel() {
         JPanel panel = new JPanel(false);
         panel.setLayout(new FlowLayout());
         return panel;
     }
     
     /** Returns an ImageIcon, or null if the path was invalid. */
     protected static ImageIcon createImageIcon(String path) {
         java.net.URL imgURL = EditTask.class.getResource(path);
         if (imgURL != null) {
             return new ImageIcon(imgURL);
         } else {
             System.err.println("Couldn't find file: " + path);
             return null;
         }
     }
     
     /**
      * Create the GUI and show it.  For thread safety,
      * this method should be invoked from
      * the event dispatch thread.
      */
     public static void createAndShowGUI(Task tsk) {
         //Create and set up the window.
         frame = new JFrame("Edit Task");
         
         //Add content to the window.
         //Task e = null;
         frame.add(new EditTask(tsk), BorderLayout.CENTER);
         
         //Display the window.
         frame.pack();
         frame.setVisible(true);
 		frame.setSize(325,450);
 		frame.setResizable(false);
     }
     
   //   public static void main(String[] args) {
   //       //Schedule a job for the event dispatch thread:
   //       //creating and showing this application's GUI.
   //       SwingUtilities.invokeLater(new Runnable() {
   //           public void run() {
   //               //Turn off metal's use of bold fonts
 		// UIManager.put("swing.boldMetal", Boolean.FALSE);
 		// createAndShowGUI();
   //           }
   //       });
   //   }
 
     class saveChanges_Action implements ActionListener
     {
     	public void actionPerformed(ActionEvent ae)
     	{
     		String n = name.getText();
 			String d = desc.getText();
 			int m = ((int)month.getSelectedIndex()); //January = 0; December = 11;
 			int da = (int)day.getSelectedIndex()+1;
 			int y = Integer.parseInt(year.getSelectedItem()+""); //returns exact year
 			int s = (int)status.getSelectedIndex(); // to-do = 0; doing = 1; done = 2;
 			int h = (int)hour.getSelectedIndex()+1; //returns exact hour
 			int mi = (int)minute.getSelectedIndex(); //returns exact minutes
 			int p = (int)priority.getSelectedIndex();
 			int ap = (int)ampm.getSelectedIndex(); // am = 0; pm = 1;
 
 			if( ap == 1 )
 			{
 				h = h + 12;
 			}			
 
 			//System.out.println(">>>>>>>>>>>>>>>>>"+s);
     		DatabaseRW.updateTask(id, n, d, y, m+1, da, h, mi, s, p);
     		frame.dispose();
     		frame.setVisible(false);
     	}
     }
 }
