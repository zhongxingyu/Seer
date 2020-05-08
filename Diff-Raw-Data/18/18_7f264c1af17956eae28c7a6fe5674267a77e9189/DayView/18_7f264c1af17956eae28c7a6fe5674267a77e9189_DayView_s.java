 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 public class DayView
 {
 	private JLabel tasklabel, eventlabel, date, day;
 	private JList tasks, events;
 	private JFrame frmMain;
 	private Container pane;
 	private JPanel panel;
 	private JScrollPane taskscroll, eventscroll;
 	
 	
 	public DayView(final int iday,final int imonth,final int idate,final int iyear)
 	{
 		//Look and feel
 		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
 		catch (ClassNotFoundException e) {}
 		catch (InstantiationException e) {}
 		catch (IllegalAccessException e) {}
 		catch (UnsupportedLookAndFeelException e) {}
 		
 		//instantiate component
 
 		tasklabel = new JLabel( "Tasks" );
 		eventlabel = new JLabel( "Events" );
 		
 		String[] days = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
 		day = new JLabel( days[iday]+"" );
 		String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
 		date = new JLabel( months[imonth]+" "+idate+", "+iyear ); // <---connect to calendar or something
 
 		ArrayList<Task> t = null;
 		ArrayList<Event> ev = null;
 		//t = DatabaseRW.queryDayTasks(iyear, imonth, idate);
 		try
 		{
 			t = DatabaseRW.queryDayTasks(iyear, (imonth+1), idate);
 			//System.out.println("Successfully Queried Day Task: " + iyear + " " + (imonth+1) + " " + idate);
 			//if(t == null) System.out.println("No ArrayList returned.");
 			//else System.out.println("ArrayList size: " + t.size());
 
 			ev = DatabaseRW.queryDayEvents(iyear, (imonth+1), idate);
 			//System.out.println("Successfully Queried Day Event: " + iyear + " " + (imonth+1) + " " + idate);
 			//if(ev == null) System.out.println("No ArrayList returned.");
 			//else System.out.println("ArrayList size: " + ev.size());
 		} catch (Exception e) {}
 
 		String[] taskNames = new String[1];
 		//String[] taskDescs = new String[1];
 		//String[] taskTstamp = new String[1];
 		taskNames[0] = "No tasks found";
 		
 		String[] eventNames = new String[1];
 		//String[] eventDescs = new String[1];
 		//String[] eventTstamp = new String[1];
 		eventNames[0] = "No tasks found";
 		boolean clickable = false;
 		if(t != null && t.size() > 0)
 		{
 			//System.out.println("Returned Task List size > 0");
 			taskNames = new String[t.size()];
 			for(int i = 0; i < t.size(); i++)
 			{
 				taskNames[i] = t.get(i).name;
 				//taskDescs[i] = t.get(i).desc;
 				//taskTstamp[i] = t.get(i).startTime;
 			}
 			clickable = true;
 		}
 		if(ev != null && ev.size() > 0)
 		{
 			//System.out.println("Returned Event List size > 0");
 			eventNames = new String[ev.size()];
 			for(int i = 0; i < ev.size(); i++)
 			{
 				eventNames[i] = ev.get(i).name;
				System.out.println( ev.get(i).name);
 				//eventDescs[i] = ev.get(i).descs;
 				//eventTstamp[i] = ev.get(i).startTime;
 			}
 			clickable = true;
 		}
 
 
 		tasks = new JList(taskNames);
 		//tasks = new JList();
 		events = new JList(eventNames);
 		panel = new JPanel(new GridLayout(2, 1));
 		taskscroll = new JScrollPane(tasks);
 		eventscroll = new JScrollPane(events);
 
 		//if JList item has been clicked
 		tasks.addMouseListener(new MouseAdapter() {
 		    public void mouseClicked(MouseEvent evt) {
 		    	JList list = (JList)evt.getSource();
 		    	Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
 		    	if (r != null && r.contains(evt.getPoint())) {
 		     		int index = list.locationToIndex(evt.getPoint()); 
 			    	//System.out.println(index);
 			    	try{
 				    	ArrayList<Task> t =
 				    	DatabaseRW.queryDayTasks(iyear, (imonth+1), idate);
 				    	Object[] options = {"Edit","Delete","Back"};
 				    	int choice = JOptionPane.showOptionDialog(frmMain,
 				    	t.get(index).desc,
 				    	t.get(index).name,
 				    	JOptionPane.YES_NO_CANCEL_OPTION,
 				    	JOptionPane.INFORMATION_MESSAGE,
     					null,
     					options,
     					options[2]);
     					if(choice==0){
     						frmMain.dispose();
 							Calendar.addEntryOpen++;
     						//open the edit frame
     						//EditTask edit = new EditTask(t.get(index));
     						EditTask.createAndShowGUI(t.get(index));
     					} else if(choice == 1){
 				    		Object[] delOpts = {"Back","Delete"};
     						int delChoice = JOptionPane.showOptionDialog(frmMain,
     					 	"Delete "+t.get(index).name+"?","Confirm Delete",
     						JOptionPane.YES_NO_CANCEL_OPTION,
     						JOptionPane.QUESTION_MESSAGE,
     						null,
     						delOpts,
     						delOpts[1]);
 							if(delChoice == 1){
 								DatabaseRW.deleteTask(t.get(index).id);
 								frmMain.dispose();
 								Calendar.addEntryOpen++;
 							}
     					}
 			    	}catch(Exception e){}
 		 		}
 		    }
 		});
 
 		events.addMouseListener(new MouseAdapter() {
 		    public void mouseClicked(MouseEvent evt) {
 		    	JList list = (JList)evt.getSource();
 		    	Rectangle r = list.getCellBounds(0, list.getLastVisibleIndex());
 		    	if (r != null && r.contains(evt.getPoint())) {
 		     		int index = list.locationToIndex(evt.getPoint()); 
 			    	//System.out.println(index);
 			    	try{
 				    	ArrayList<Event> t =
 				    	DatabaseRW.queryDayEvents(iyear, (imonth+1), idate);
 				    	Object[] options = {"Edit","Delete","Back"};
 				    	int choice = JOptionPane.showOptionDialog(frmMain,
 				    	t.get(index).desc,
 				    	t.get(index).name,
 				    	JOptionPane.YES_NO_CANCEL_OPTION,
 				    	JOptionPane.INFORMATION_MESSAGE,
     					null,
     					options,
     					options[2]);
     					if(choice==0){
     						frmMain.dispose();
 							Calendar.addEntryOpen++;
     						//open the edit frame
     						EditEvent.createAndShowGUI(t.get(index));
     					} else if(choice == 1){
 				    		Object[] delOpts = {"Back","Delete"};
     						int delChoice = JOptionPane.showOptionDialog(frmMain,
     					 	"Delete "+t.get(index).name+"?","Confirm Delete",
     						JOptionPane.YES_NO_CANCEL_OPTION,
     						JOptionPane.QUESTION_MESSAGE,
     						null,
     						delOpts,
     						delOpts[1]);
 							if(delChoice == 1){
 								DatabaseRW.deleteEvent(t.get(index).id);
 								frmMain.dispose();
 								Calendar.addEntryOpen++;
 							}
     					}
 			    	}catch(Exception e){}
 		 		}
 		    }
 		});
 
 		//frame stuff
 		frmMain = new JFrame( months[imonth]+" "+idate+", "+iyear );
 		frmMain.setSize( 325, 450 );
 		pane = frmMain.getContentPane();
 		pane.setLayout(new BorderLayout());
 		frmMain.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frmMain.setResizable(false);
 		frmMain.setVisible(true);
 		frmMain.setAlwaysOnTop(true);
 		
 		//add contents to pane
 		pane.add(panel, BorderLayout.CENTER);
 		JPanel taskPanel = new JPanel(new BorderLayout());
 			JPanel taskLabelPanel = new JPanel(new GridLayout(2,1));
 				taskLabelPanel.add(new JLabel(" "));
 				taskLabelPanel.add(tasklabel);
 			taskPanel.add(taskLabelPanel, BorderLayout.PAGE_START);
 			taskPanel.add(taskscroll, BorderLayout.CENTER);
 		panel.add(taskPanel);
 		JPanel eventPanel = new JPanel(new BorderLayout());
 			JPanel eventLabelPanel = new JPanel(new GridLayout(2, 1));
 				eventLabelPanel.add(new JLabel(" "));
 				eventLabelPanel.add(eventlabel);
 			eventPanel.add(eventLabelPanel, BorderLayout.PAGE_START);
 			eventPanel.add(eventscroll, BorderLayout.CENTER);
 		//panel.add(eventlabel);
 		//panel.add(eventscroll);
 		panel.add(eventPanel);
 		JPanel heading = new JPanel( new GridLayout(2, 1) );
 			heading.add(new JLabel(" "));
 			JPanel dayDatePanel = new JPanel(new FlowLayout());
 				dayDatePanel.add(day);
 				dayDatePanel.add(date);
 			heading.add(dayDatePanel);
 		pane.add(heading, BorderLayout.PAGE_START);
 		pane.add(new JLabel(" "), BorderLayout.PAGE_END);
 		pane.add(new JLabel("   "), BorderLayout.LINE_END);
 		pane.add(new JLabel("   "), BorderLayout.LINE_START);
 		
 		//set bounds
 		panel.setBounds(0, 0, 500, 500);
 		day.setBounds(10, 10, 80,20);
 		date.setBounds(70, 10, 150,20);
 		//tasklabel.setBounds(10, 40, 80, 20);                                  
 		//taskscroll.setBounds(10, 60, 300, 150);
 		//eventlabel.setBounds(10, 220, 80, 20);
 		//eventscroll.setBounds(10, 240, 300, 150);
 	}
 }
