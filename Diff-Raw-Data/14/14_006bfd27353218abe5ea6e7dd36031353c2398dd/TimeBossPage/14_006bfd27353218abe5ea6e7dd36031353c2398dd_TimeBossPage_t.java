 package hu.schmidtsoft.timeboss.standalone;
 
 import hu.schmidtsoft.timeboss.server.Activity;
 import hu.schmidtsoft.timeboss.server.ITimeBossServer;
 import hu.schmidtsoft.timeboss.server.TimeBossInstance;
 import hu.schmidtsoft.timeboss.util.UtilTime;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class TimeBossPage {
 	TimeBossInstance instance;
 	public TimeBossPage(TimeBossInstance instance) {
 		super();
 		this.instance = instance;
 	}
 	class SaveActivity implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			try
 			{
 				if(title.getSelectedItem().toString().length()<1)
 				{
 					throw new RuntimeException("title must be set!");
 				}
 				long lengthL=Long.parseLong(length.getText());
 				long preL=Long.parseLong(pre.getText());
 				if(lengthL<0)
 				{
 					throw new NumberFormatException("Time can not be negative");
 				}
 				if(preL<0)
 				{
 					throw new NumberFormatException("Time can not be negative");
 				}
 				Activity toSave=new Activity(true,
 						project.getSelectedItem().toString(),
 						title.getSelectedItem().toString(),
 						description.getText(),
 						issue.getSelectedItem().toString(),
 						System.currentTimeMillis(),
 						lengthL,
 						preL
 						);

				// Save the project, activity and issue to the config file.
 				instance.getServer().saveActivity(toSave);
 				instance.getConfiguration().addProject(project.getSelectedItem().toString());
 				instance.getConfiguration().addActivityTitle(title.getSelectedItem().toString());
 				instance.getConfiguration().addIssue(issue.getSelectedItem().toString());
 				instance.getServer().setPreferences(instance.getConfiguration().toXml());
       
        // Add project, activity and issue to the drop-down list if required.
        if (project.getSelectedIndex() == -1) {
          project.addItem(project.getSelectedItem());
        }
        if (title.getSelectedIndex() == -1) {
          title.addItem(title.getSelectedItem());
        }
        if (issue.getSelectedIndex() == -1) {
          issue.addItem(issue.getSelectedItem());
        }
 			}catch(Exception ex)
 			{
 				JOptionPane.showMessageDialog(parent,
 						"Invalid data, cannot save activity: "+ex.getMessage());
 			}
 			updateControls();
 		}
 	}
 	class Reload implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			updateControls();
 		}
 	}
 	class SaveActivityNothing implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			try
 			{
 				long time=Long.parseLong(notWorkingTime.getText());
 				if(time<0)
 				{
 					throw new NumberFormatException("Time can not be negative");
 				}
 				instance.getServer().saveActivity(new Activity(
 						false,
 						"",
 						"","",
 						"",System.currentTimeMillis(), time,
 						0));
 			}catch(Exception ex)
 			{
 				JOptionPane.showMessageDialog(parent,
 						"Invalid data, cannot save activity: "+ex.getMessage());
 			}
 			updateControls();
 		}
 	}
 	JLabel time;
 	JLabel finishTime;
 	JLabel lastActivity;
 	JTextField notWorkingTime;
 	JComboBox title;
 	JTextArea description;
 	JTextField length;
 	JTextField pre;
 	JComboBox project;
 	JComboBox issue;
 	JFrame parent;
 	int gridX=0;
 	int gridY=0;
 	public Component createControl(JFrame parent)
 	{
 		this.parent=parent;
 		JPanel panel=new JPanel();
 		panel.setLayout(new GridBagLayout());
 		parent.add(panel);
 		JPanel ret=panel;
 		createLabel(ret, "Last activity title:");
 		lastActivity=createLabel(ret, "");
 		createSeparator(ret);
 		createLabel(ret, "Last activity saved at:");
 		time=createLabel(ret, "");
 		
 		
 		
 		createLabel(ret, "Last activity lasts until:");
 		finishTime=createLabel(ret, "");
 		createLabel(ret, "project:");
 		project=createComboBox(ret, instance.getConfiguration().getProjects().toArray());
 		createLabel(ret, "Activity Title:");
 		title=createComboBox(ret, instance.getConfiguration().getActivityTitles().toArray());
 		createLabel(ret, "Issue:");
 		issue=createComboBox(ret, instance.getConfiguration().getIssues().toArray());
 		createLabel(ret, "Elapsed Time (up to now in minutes):");
 		pre=createText(ret, "0");
 		createLabel(ret, "Remaining Time (from now in minutes):");
 		length=createText(ret, "");
 		createLabel(ret, "Activity Description:");
 		description=createTextBig(ret, "");
 		createEmpty(ret);
 		createButton(ret, "Save activity log")
 			.addActionListener(new SaveActivity());
 		createEmpty(ret);
 		createButton(ret, "Reload last activity's data")
 			.addActionListener(new Reload());
 		createButton(ret, "Not working. Don't bother me for minutes: ")
 			.addActionListener(new SaveActivityNothing());
 		notWorkingTime=createText(ret, ""+instance.getConfiguration().getTimeoutForActivityCheck());
 //		Point p=ret.computeSize(0, 0);
 		updateControls();
 		return panel;
 	}
 	private void createSeparator(JPanel ret) {
 		JLabel toAdd=new JLabel();
 		ret.add(toAdd);
 	}
 	public void updateControls() {
 		ITimeBossServer server=instance.getServer();
 		Activity last=server.getLastRealActivity();
 		Activity lastAny=server.getLastActivity();
 		time.setText(""+UtilTime.timeToString(
 				lastAny.getStartTime()));
 		lastActivity.setText(""+lastAny.getTitle());
 		if(!lastAny.getWork())
 		{
 			lastActivity.setText("REST");
 		}
 		finishTime.setText(""+UtilTime.timeToString(
 				lastAny.getStartTime()<0?-1:
 				(lastAny.getStartTime()+
 				UtilTime.minToMilli(lastAny.getLength()))));
 		description.setText(last.getDescription());
 		title.setSelectedItem(last.getTitle());
 		project.setSelectedItem(last.getProject());
 		issue.setSelectedItem(last.getIssue());
 		pre.setText("0");
 		if(last.getLength()<0)
 		{
 			length.setText(""+instance.getConfiguration().getTimeoutForActivityCheck());
 		}else
 		{
 			length.setText(""+last.getLength());
 		}
 	}
 	private void createEmpty(JComponent c)
 	{
 		createLabel(c, "");
 	}
 	private JTextField createText(JComponent c, String string) {
 		JTextField ret=new JTextField(string);
 		createGridConstraint(c, ret);
 		c.add(ret);
 		return ret;
 	}
 
 	private JComboBox createComboBox(JComponent c, Object[] strings) {
 		JComboBox ret=new JComboBox(strings);
 		ret.setEditable(true);
 		createGridConstraint(c, ret);
 		c.add(ret);
 		return ret;
 	}
 
 	private JTextArea createTextBig(JComponent c, String string) {
 
 		JTextArea ret=new JTextArea(string);
 		ret.setRows(5);
 //		ret.setAutoscrolls(true);
 //		ret.setBorder(new LineBorder(Color.BLACK));
 		JScrollPane scroller = new JScrollPane(ret);
 		createGridConstraint(c, scroller);
 		c.add(scroller);
 		return ret;
 	}
 
 	private JLabel createLabel(JComponent ret, String string) {
 		JLabel l=new JLabel(string);
 		createGridConstraint(ret, l);
 		ret.add(l);
 		return l;
 	}
 
 	private void createGridConstraint(JComponent ret, JComponent l) {
 		GridBagConstraints c=new GridBagConstraints();
 		c.gridx=gridX;
 		c.gridy=gridY;
 		c.anchor=GridBagConstraints.FIRST_LINE_START;
 		c.fill=GridBagConstraints.HORIZONTAL;
 		gridX++;
 		if(gridX>1)
 		{
 			gridX=0;
 			gridY++;
 		}
 		((GridBagLayout)ret.getLayout()).setConstraints(l, 
 				c);
 	}
 
 	private JButton createButton(JComponent ret, String string) {
 		JButton b=new JButton(string);
 		createGridConstraint(ret, b);
 		ret.add(b);
 		return b;
 	}
 	
 }
