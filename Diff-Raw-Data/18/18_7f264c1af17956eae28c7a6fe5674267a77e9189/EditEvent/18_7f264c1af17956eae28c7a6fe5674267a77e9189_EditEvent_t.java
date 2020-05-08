 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 public class EditEvent extends JPanel
 {
 	JTextField name;
 	JTextArea desc;
 	JComboBox month, day, year, hour, minute, ampm, status, priority, repeating;
 	public static JFrame frame;
 	public JRadioButtonMenuItem allDay, timeSelection;
 	JLabel repeatingLabel;
 	JScrollPane dscScroll;
 	final int id;
 
 	public EditEvent(Event eve)
 	{
 		super(new GridLayout(1, 1));
 		id = eve.id;
 		name = new JTextField();
 		name.setText(eve.name);
 		desc = new JTextArea();
 		desc.setLineWrap(true);
 		desc.setText(eve.desc);
 		dscScroll = new JScrollPane(desc);
 
 		String tempDate = eve.startTime.toString();
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
		if(tempHours!=0)
			day.setSelectedIndex(tempDay);
 
 		Integer[] years = new Integer[100];
 		for( int i=0; i<100; i++ )
 			years[i] = 2012+i;
 		year = new JComboBox(years);
 		//year.setSelectedIndex(tempYear);
 
 		boolean pmcheck = false;
 		if(tempHours!= 0 && tempHours > 12){
 			tempHours -= 12;
 			pmcheck = true;
 		}
 
 		Integer[] hours = new Integer[12];
 		for( int i=0; i<12; i++)
 			hours[i] = i+1;
 		hour = new JComboBox(hours);
 		hour.setSelectedIndex(tempHours-1);
 
 		String[] minutes = new String[60];
 		for( int i=0; i<60; i++ )
 		{	if( i > 9 )
 			minutes[i] = Integer.toString(i);
 			else
 			minutes[i] = "0"+Integer.toString(i);
 		}
 		minute = new JComboBox(minutes);
 
 		String[] time = { "AM", "PM"};
 		ampm = new JComboBox(time);
 		if(pmcheck)
 			ampm.setSelectedIndex(1);
 		
 		String[] repeating0 = { "None","Daily", "Weekly", "Monthly", "Yearly" };
 		repeating = new JComboBox(repeating0);
 		repeating.setSelectedIndex(eve.repeating);
 		repeating.setEnabled(true);
 
 		JTabbedPane tabbedPane = new JTabbedPane();
         ImageIcon icon = createImageIcon("tray.jpg");
         
       JComponent panel2 = makeTextPanel();
 		panel2.setLayout(new GridLayout(1,1));
         tabbedPane.addTab("Events", icon, panel2,
                 "Events");
 		JPanel eventsPanelMain = new JPanel(new BorderLayout());
 			eventsPanelMain.add(new JLabel(" "), BorderLayout.PAGE_START);
 			eventsPanelMain.add(new JLabel(" "), BorderLayout.PAGE_END);
 			eventsPanelMain.add(new JLabel("   "), BorderLayout.LINE_START);
 			eventsPanelMain.add(new JLabel("   "), BorderLayout.LINE_END);
 			JPanel eventPane = new JPanel(new BorderLayout());
 				JPanel eventHead = new JPanel(new GridLayout(1,1));
 					JPanel eventNameLabel = new JPanel(new BorderLayout());
 						eventNameLabel.add(new JLabel("Event Name:  \t"), BorderLayout.LINE_START);
 						eventNameLabel.add(name, BorderLayout.CENTER);
 					eventHead.add(eventNameLabel);
 				eventPane.add(eventHead, BorderLayout.PAGE_START);
 				JPanel eventBody = new JPanel(new GridLayout(2, 1));
 					JPanel eventDescMain = new JPanel(new BorderLayout());
 						eventDescMain.add(new JLabel(" "), BorderLayout.PAGE_START);
 						JPanel eventDescLabel = new JPanel(new FlowLayout());
 						eventDescMain.add(new JLabel("Description:   \t"), BorderLayout.LINE_START);
 						eventDescMain.add(dscScroll, BorderLayout.CENTER);
 					eventBody.add(eventDescMain);
 					JPanel eventProp = new JPanel(new GridLayout(5,1));
 						JPanel eventDeadlineMain = new JPanel(new BorderLayout());
 							eventDeadlineMain.add(new JLabel("Start Date: \t"), BorderLayout.LINE_START);
 							JPanel eventDate = new JPanel(new FlowLayout()); //DATE
 									eventDate.add(month);
 									eventDate.add(day);
 									eventDate.add(year);
 							eventDeadlineMain.add(eventDate, BorderLayout.CENTER);
 						eventProp.add(eventDeadlineMain);
 							JPanel eventDeadlineInfo = new JPanel(new BorderLayout());	
 								timeSelection = new JRadioButtonMenuItem("Time",true);
 								eventDeadlineInfo.add(timeSelection, BorderLayout.LINE_START);
 								JPanel eventTime = new JPanel(new FlowLayout()); //TIME
 									eventTime.add(hour);
 									eventTime.add(minute);
 									eventTime.add(ampm);
 								eventDeadlineInfo.add(eventTime, BorderLayout.CENTER);
 						allDay = new JRadioButtonMenuItem("All Day",false);
 						ButtonGroup bg = new ButtonGroup();
 							bg.add(timeSelection);
 							bg.add(allDay);
 							timeSelection.addItemListener( new time_Select() );
 							allDay.addItemListener( new allDay_Select() );
 							
 						eventProp.add(eventDeadlineInfo);
 						eventProp.add(allDay);
 							JPanel isRepeating = new JPanel(new BorderLayout());	
 								repeatingLabel = new JLabel("Repeat Event?");
 								isRepeating.add(repeatingLabel, BorderLayout.LINE_START);
 								isRepeating.add(repeating, BorderLayout.CENTER);
 						eventProp.add(isRepeating);
 						JButton save = new JButton("Save Changes");
 						save.addActionListener(new save_Action());
 						eventProp.add(save);
 					eventBody.add(eventProp);
 				eventPane.add(eventBody);
 			eventsPanelMain.add(eventPane, BorderLayout.CENTER);
 		panel2.add(eventsPanelMain);
         
         //Add the tabbed pane to this panel.
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
         java.net.URL imgURL = EditEvent.class.getResource(path);
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
     public static void createAndShowGUI(Event eve) {
         //Create and set up the window.
         frame = new JFrame("Edit Event");
         
         //Add content to the window.
         frame.add(new EditEvent(eve), BorderLayout.CENTER);
         
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
 
     class time_Select implements ItemListener
 	{
 		public void itemStateChanged( ItemEvent e)
 		{
 
 			if( timeSelection.isSelected() )
 			{
 				hour.setEnabled(true);
 				minute.setEnabled(true);
 				ampm.setEnabled(true);
 			}
 		}
 	}
 	
 	class allDay_Select implements ItemListener
 	{
 		public void itemStateChanged( ItemEvent e)
 		{
 			if( allDay.isSelected() )
 			{	
 				hour.setEnabled(false);
 				minute.setEnabled(false);
 				ampm.setEnabled(false);
 			}
 		}
 	}
 
 	class save_Action implements ActionListener
 	{
 		public void actionPerformed(ActionEvent ae)
 		{
 			String n = name.getText();
 			String de = desc.getText();
 			int mo = (int)month.getSelectedIndex(); //January = 0; December = 11;
 			int da = (int)day.getSelectedIndex()+1;
 			int y = Integer.parseInt(year.getSelectedItem()+""); //returns exact year
 			int h = (int)hour.getSelectedIndex()+1; //returns exact hour
 			int mi = (int)minute.getSelectedIndex(); //returns exact minutes
 			int ap = (int)ampm.getSelectedIndex(); // am = 0; pm = 1;
 			int r = (int)repeating.getSelectedIndex();
 
 			if(ap == 1)
 			{
 				h = h + 12;
 			}
 			//System.out.println(">>>>>>>>>>>>>>>>>"+id);
 			DatabaseRW.updateRepeatingEvent(id, n, de, y, mo+1, da, h, mi, r);
     		frame.dispose();
     		frame.setVisible(false);
 		}
 	}
 }
