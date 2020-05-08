 package view.state;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.SwingConstants;
 
 import model.Activity;
 import model.ActivityType;
 import model.Project;
 import model.TimeEntry;
 import model.gui.ActivityListRenderer;
 import model.gui.ActivityTypeListRenderer;
 import model.gui.GenericComboBoxModel;
 import model.gui.ProjectListRenderer;
 import utils.Dialog;
 import view.ViewContainer;
 
 @SuppressWarnings("serial")
 public class CalendarViewState extends AbstractViewState {
 
 	private JButton btnBack;
 	private JButton btnRegister;
 	private JPanel toolbar;
 	private JPanel legend;
 	private JPanel day1;
 	private JPanel day2;
 	private JPanel day3;
 	private JPanel day4;
 	private JPanel day5;
 	private JPanel day6;
 	private JPanel day7;
 	private JPanel panel_1;
 	private JPanel panel_2;
 	private JPanel panel_4;
 	private JPanel panel_5;
 	private JPanel panel_6;
 	private JPanel panel_7;
 	private JPanel panel_8;
 	private JLabel lblDay1;
 	private JLabel lblDay2;
 	private JLabel lblDay3;
 	private JLabel lblDay4;
 	private JLabel lblDay5;
 	private JLabel lblDay6;
 	private JLabel lblDay7;
 	private JPanel pnlDateInput;
 	private JLabel lblNewLabel;
 	private JLabel label;
 	private JPanel pnlInputActivity;
 	private JLabel lblDeveloperName;
 	private JLabel lblProject;
 	private JLabel lblActivity;
 	private JLabel lblDate;
 	private JTextField txtDate;
 	private JTextField txtEnd;
 	private JTextField txtStart;
 	private JComboBox<Project> cmbProject;
 	private JComboBox<Activity> cmbActivity;
 	private JLabel[] dayLabels;
 	private JPanel[] dayPanels;
 	private JButton btnPrevious;
 	private JButton btnNext;
 	private JLabel lblChangeDay;
 	private JPanel panel;
 	private JLabel lblTime1;
 	private JLabel lblTime2;
 	private JLabel lblKl;
 	private JLabel lblKl_1;
 	private JLabel lblKl_2;
 	private JLabel lblKl_3;
 	private JLabel lblKl_4;
 	private JLabel lblKl_5;
 	private JLabel lblKl_6;
 	private JLabel lblKl_7;
 	private JLabel lblKl_8;
 	private JLabel lblKl_9;
 	private JButton btnPreviousDeveloper;
 	private JButton btnNextDeveloper;
 	private JPanel pnlAddButtons;
 	private JButton btnReserve;
 	private JToggleButton tglIsFixed;
 	private JPanel pnlFixed;
 	private JLabel lblActivityType;
 	private JComboBox<ActivityType> cmbActivityType;
 	private JPanel panel_3;
 	private JLabel lblKl_10;
 	private JPanel panel_9;
 	private JLabel lblDato;
 	private HashMap<TimeEntry, JPanel> timeEntryPanels;
 
 	public CalendarViewState() {
 		setMinimumSize(new Dimension(ViewContainer.WINDOW_WIDTH, ViewContainer.WINDOW_HEIGHT));
 		setMaximumSize(new Dimension(ViewContainer.WINDOW_WIDTH, ViewContainer.WINDOW_HEIGHT));
 						setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 						
 						JPanel topPanel = new JPanel();
 						add(topPanel);
 						topPanel.setLayout(new GridLayout(0, 1, 0, 0));
 						
 								toolbar = new JPanel();
 								topPanel.add(toolbar);
 								toolbar.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 								this.btnBack = new JButton("Back to menu");
 								toolbar.add(btnBack);
 								
 								JPanel header = new JPanel();
 								topPanel.add(header);
 								header.setBounds(new Rectangle(0, 0, ViewContainer.WINDOW_WIDTH, 0));
 								header.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 								
 								btnPreviousDeveloper = new JButton("<");
 								header.add(btnPreviousDeveloper);
 								this.lblDeveloperName = new JLabel();
 								lblDeveloperName.setFont(new Font("DejaVu Sans", Font.PLAIN, 20));
 								header.add(lblDeveloperName);
 								lblDeveloperName.setText("Test");
 								
 								btnNextDeveloper = new JButton(">");
 								header.add(btnNextDeveloper);
 				
 						JPanel weekPanel = new JPanel();
 						weekPanel.setPreferredSize(new Dimension(ViewContainer.WINDOW_WIDTH, 300));
 						weekPanel.setMinimumSize(new Dimension(ViewContainer.WINDOW_WIDTH, 300));
 						weekPanel.setMaximumSize(new Dimension(ViewContainer.WINDOW_WIDTH, 300));
 						weekPanel.setLayout(new GridLayout(0, 8, 1, 0));
 						
 						panel = new JPanel();
 						panel.setPreferredSize(new Dimension(40, 300));
 						panel.setMinimumSize(new Dimension(40, 300));
 						panel.setMaximumSize(new Dimension(40, 300));
 						panel.setBackground(Color.LIGHT_GRAY);
 						weekPanel.add(panel);
 						panel.setLayout(new GridLayout(13, 1, 0, 0));
 						
 						lblTime1 = new JLabel("Kl. 00");
 						lblTime1.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblTime1);
 						
 						lblTime2 = new JLabel("Kl. 02");
 						lblTime2.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblTime2);
 						
 						lblKl = new JLabel("Kl. 04");
 						lblKl.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl);
 						
 						lblKl_1 = new JLabel("Kl. 06");
 						lblKl_1.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl_1);
 						
 						lblKl_2 = new JLabel("Kl. 08");
 						lblKl_2.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl_2);
 						
 						lblKl_3 = new JLabel("Kl. 10");
 						lblKl_3.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl_3);
 						
 						lblKl_4 = new JLabel("Kl. 12");
 						lblKl_4.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl_4);
 						
 						lblKl_5 = new JLabel("Kl. 14");
 						lblKl_5.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl_5);
 						
 						lblKl_6 = new JLabel("Kl. 16");
 						lblKl_6.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl_6);
 						
 						lblKl_7 = new JLabel("Kl. 18");
 						lblKl_7.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl_7);
 						
 						lblKl_8 = new JLabel("Kl. 20");
 						lblKl_8.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl_8);
 						
 						lblKl_9 = new JLabel("Kl. 22");
 						lblKl_9.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl_9);
 						
 						lblKl_10 = new JLabel("Kl. 24");
 						lblKl_10.setHorizontalAlignment(SwingConstants.CENTER);
 						panel.add(lblKl_10);
 						
 								day1 = new JPanel();
 								day1.setMaximumSize(new Dimension(80, 300));
 								day1.setBackground(Color.GRAY);
 								day1.setPreferredSize(new Dimension(80, 300));
 								day1.setMinimumSize(new Dimension(80, 300));
 								day1.setLayout(null);
 								weekPanel.add(day1);
 								
 								day2 = new JPanel();
 								day2.setBackground(Color.GRAY);
 								day2.setPreferredSize(new Dimension(70, 300));
 								day2.setMinimumSize(new Dimension(70, 300));
 								day2.setLayout(null);
 								weekPanel.add(day2);
 								
 										day3 = new JPanel();
 										day3.setBackground(Color.GRAY);
 										day3.setPreferredSize(new Dimension(70, 300));
 										day3.setMinimumSize(new Dimension(70, 300));
 										day3.setLayout(null);
 										weekPanel.add(day3);
 										
 												day4 = new JPanel();
 												day4.setBackground(Color.GRAY);
 												day4.setPreferredSize(new Dimension(70, 300));
 												day4.setMinimumSize(new Dimension(70, 300));
 												day4.setLayout(null);
 												weekPanel.add(day4);
 												
 														day5 = new JPanel();
 														day5.setBackground(Color.GRAY);
 														day5.setPreferredSize(new Dimension(70, 300));
 														day5.setMinimumSize(new Dimension(70, 300));
 														day5.setLayout(null);
 														weekPanel.add(day5);
 														
 																day6 = new JPanel();
 																day6.setBackground(Color.GRAY);
 																day6.setPreferredSize(new Dimension(70, 300));
 																day6.setMinimumSize(new Dimension(70, 300));
 																day6.setLayout(null);
 																weekPanel.add(day6);
 																
 																		day7 = new JPanel();
 																		day7.setBackground(Color.GRAY);
 																		day7.setPreferredSize(new Dimension(70, 300));
 																		day7.setMinimumSize(new Dimension(70, 300));
 																		day7.setLayout(null);
 																		weekPanel.add(day7);
 																		add(weekPanel);
 				
 				legend = new JPanel();
 				legend.setPreferredSize(new Dimension(ViewContainer.WINDOW_WIDTH, 20));
 				legend.setMinimumSize(new Dimension(ViewContainer.WINDOW_WIDTH, 20));
 				legend.setMaximumSize(new Dimension(ViewContainer.WINDOW_WIDTH, 20));
 				add(legend);
 				legend.setLayout(new GridLayout(0, 8, 0, 0));
 				
 				panel_9 = new JPanel();
 				panel_9.setPreferredSize(new Dimension(70, 70));
 				panel_9.setMinimumSize(new Dimension(70, 70));
 				panel_9.setMaximumSize(new Dimension(70, 70));
 				legend.add(panel_9);
 				panel_9.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 				
 				lblDato = new JLabel("Dato:");
 				lblDato.setFont(new Font("DejaVu Sans", Font.BOLD, 12));
 				lblDato.setHorizontalAlignment(SwingConstants.CENTER);
 				lblDato.setAlignmentX(0.5f);
 				panel_9.add(lblDato);
 				
 				panel_1 = new JPanel();
 				panel_1.setMaximumSize(new Dimension(70, 70));
 				panel_1.setPreferredSize(new Dimension(70, 70));
 				panel_1.setMinimumSize(new Dimension(70, 70));
 				legend.add(panel_1);
 				panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 				
 				lblDay1 = new JLabel("Mon 13/4");
 				lblDay1.setHorizontalAlignment(SwingConstants.CENTER);
 				lblDay1.setAlignmentX(Component.CENTER_ALIGNMENT);
 				panel_1.add(lblDay1);
 				
 				panel_2 = new JPanel();
 				panel_2.setMaximumSize(new Dimension(70, 70));
 				panel_2.setPreferredSize(new Dimension(70, 70));
 				panel_2.setMinimumSize(new Dimension(70, 70));
 				legend.add(panel_2);
 				panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 				
 				lblDay2 = new JLabel("Mon 13/4");
 				lblDay2.setHorizontalAlignment(SwingConstants.CENTER);
 				lblDay2.setAlignmentX(0.5f);
 				panel_2.add(lblDay2);
 				
 				panel_4 = new JPanel();
 				panel_4.setMaximumSize(new Dimension(70, 70));
 				panel_4.setPreferredSize(new Dimension(70, 70));
 				panel_4.setMinimumSize(new Dimension(70, 70));
 				legend.add(panel_4);
 				panel_4.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 				
 				lblDay3 = new JLabel("Mon 13/4");
 				lblDay3.setHorizontalAlignment(SwingConstants.CENTER);
 				lblDay3.setAlignmentX(0.5f);
 				panel_4.add(lblDay3);
 				
 				panel_5 = new JPanel();
 				panel_5.setMaximumSize(new Dimension(70, 70));
 				panel_5.setPreferredSize(new Dimension(70, 70));
 				panel_5.setMinimumSize(new Dimension(70, 70));
 				legend.add(panel_5);
 				panel_5.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 				
 				lblDay4 = new JLabel("Mon 13/4");
 				lblDay4.setHorizontalAlignment(SwingConstants.CENTER);
 				lblDay4.setAlignmentX(0.5f);
 				panel_5.add(lblDay4);
 				
 				panel_6 = new JPanel();
 				panel_6.setMaximumSize(new Dimension(70, 70));
 				panel_6.setPreferredSize(new Dimension(70, 70));
 				panel_6.setMinimumSize(new Dimension(70, 70));
 				legend.add(panel_6);
 				panel_6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 				
 				lblDay5 = new JLabel("Mon 13/4");
 				lblDay5.setHorizontalAlignment(SwingConstants.CENTER);
 				lblDay5.setAlignmentX(0.5f);
 				panel_6.add(lblDay5);
 				
 				panel_7 = new JPanel();
 				panel_7.setMaximumSize(new Dimension(70, 70));
 				panel_7.setPreferredSize(new Dimension(70, 70));
 				panel_7.setMinimumSize(new Dimension(70, 70));
 				legend.add(panel_7);
 				panel_7.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 				
 				lblDay6 = new JLabel("Mon 13/4");
 				lblDay6.setHorizontalAlignment(SwingConstants.CENTER);
 				lblDay6.setAlignmentX(0.5f);
 				panel_7.add(lblDay6);
 				
 				panel_8 = new JPanel();
 				panel_8.setMaximumSize(new Dimension(70, 70));
 				panel_8.setPreferredSize(new Dimension(70, 70));
 				panel_8.setMinimumSize(new Dimension(70, 70));
 				legend.add(panel_8);
 				panel_8.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 				
 				lblDay7 = new JLabel("Mon 13/4");
 				lblDay7.setHorizontalAlignment(SwingConstants.CENTER);
 				lblDay7.setAlignmentX(0.5f);
 				panel_8.add(lblDay7);
 		
 		// Create day label list
 		this.dayLabels = new JLabel[] {this.lblDay1, this.lblDay2, this.lblDay3, this.lblDay4, this.lblDay5, this.lblDay6, this.lblDay7};
 		this.dayPanels = new JPanel[] {this.day1, this.day2, this.day3, this.day4, this.day5, this.day6, this.day7};
 		
 		panel_3 = new JPanel();
 		add(panel_3);
 		panel_3.setLayout(new GridLayout(5, 1, 0, 0));
 		
 		JPanel controls = new JPanel();
 		panel_3.add(controls);
 		
 		btnPrevious = new JButton("<");
 		controls.add(btnPrevious);
 		
 		lblChangeDay = new JLabel("Change day");
 		lblChangeDay.setFont(new Font("DejaVu Sans", Font.BOLD, 12));
 		controls.add(lblChangeDay);
 		
 		btnNext = new JButton(">");
 		btnNext.setFont(new Font("DejaVu Sans", Font.PLAIN, 12));
 		controls.add(btnNext);
 		
 		pnlDateInput = new JPanel();
 		panel_3.add(pnlDateInput);
 		
 		lblDate = new JLabel("Date");
 		lblDate.setFont(new Font("DejaVu Sans", Font.BOLD, 12));
 		pnlDateInput.add(lblDate);
 		
 		txtDate = new JTextField();
 		txtDate.setColumns(7);
 		pnlDateInput.add(txtDate);
 		
 		label = new JLabel("Start time");
 		label.setFont(new Font("DejaVu Sans", Font.BOLD, 12));
 		pnlDateInput.add(label);
 		
 		txtStart = new JTextField();
 		txtStart.setColumns(5);
 		pnlDateInput.add(txtStart);
 		
 		lblNewLabel = new JLabel("End time");
 		lblNewLabel.setFont(new Font("DejaVu Sans", Font.BOLD, 12));
 		pnlDateInput.add(lblNewLabel);
 		
 		txtEnd = new JTextField();
 		pnlDateInput.add(txtEnd);
 		txtEnd.setColumns(5);
 		
 		pnlFixed = new JPanel();
 		panel_3.add(pnlFixed);
 		
 		tglIsFixed = new JToggleButton("Fixed");
 		pnlFixed.add(tglIsFixed);
 		
 		lblActivityType = new JLabel("Type");
 		lblActivityType.setFont(new Font("DejaVu Sans", Font.BOLD, 12));
 		pnlFixed.add(lblActivityType);
 		
 		cmbActivityType = new JComboBox<ActivityType>();
 		cmbActivityType.setMinimumSize(new Dimension(80, 25));
 		cmbActivityType.setMaximumSize(new Dimension(200, 25));
 		pnlFixed.add(cmbActivityType);
 		
 		pnlInputActivity = new JPanel();
 		panel_3.add(pnlInputActivity);
 		
 		cmbProject = new JComboBox<Project>();
 		cmbProject.setMinimumSize(new Dimension(80, 25));
 		cmbProject.setMaximumSize(new Dimension(200, 25));
 		
 		lblProject = new JLabel("Project");
 		lblProject.setFont(new Font("DejaVu Sans", Font.BOLD, 12));
 		pnlInputActivity.add(lblProject);
 		pnlInputActivity.add(cmbProject);
 		
 		lblActivity = new JLabel("Activity");
 		lblActivity.setFont(new Font("DejaVu Sans", Font.BOLD, 12));
 		pnlInputActivity.add(lblActivity);
 		
 		cmbActivity = new JComboBox<Activity>();
 		cmbActivity.setMaximumSize(new Dimension(150, 25));
 		cmbActivity.setMinimumSize(new Dimension(80, 25));
 		cmbActivity.setBounds(new Rectangle(0, 0, 80, 0));
 		pnlInputActivity.add(cmbActivity);
 		
 		pnlAddButtons = new JPanel();
 		panel_3.add(pnlAddButtons);
 		
 		btnRegister = new JButton("Register");
 		pnlAddButtons.add(btnRegister);
 		
 		btnReserve = new JButton("Reserve");
 		pnlAddButtons.add(btnReserve);
 		
 		this.timeEntryPanels = new HashMap<TimeEntry, JPanel>();
 	}
 	
 	// Add an entry to the calendar view
 	public void addTimeEntry(final TimeEntry timeEntry, int day, Color color) {
 		float minuteHeight = Float.valueOf(this.dayPanels[day].getHeight()) / (24f * 60f);
 		
		int fromMinute = timeEntry.getStartDate().get(Calendar.HOUR_OF_DAY) * 60 + timeEntry.getStartDate().get(Calendar.MINUTE);

 		JPanel timeEntryPanel = new JPanel();
 		
 		this.timeEntryPanels.put(timeEntry, timeEntryPanel);
 		
 		timeEntryPanel.setBackground(color);
 		int durationInMinutes = timeEntry.getDurationInMinutes();
 		
		timeEntryPanel.setBounds(0, (int)(minuteHeight * fromMinute), this.dayPanels[day].getWidth(), (int)(minuteHeight * durationInMinutes));
 		
 		timeEntryPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
 
 		DateFormat format = new SimpleDateFormat("HH:mm");
 		String projectName = null;
 		if (timeEntry.getActivity().isFixed()) {
 			projectName = timeEntry.getActivity().getType().name();
 		}
 		else {
 			projectName = timeEntry.getActivity().getProject().getName();
 		}
 		JLabel projectLabel = new JLabel(String.format("<html>%s<br>%s - %s<br>%d mins</html>",
 				projectName,
 				format.format(timeEntry.getStartDate().getTime()),
 				format.format(timeEntry.getEndDate().getTime()),
 				timeEntry.getDurationInMinutes()));
 		timeEntryPanel.add(projectLabel);
 		
 		this.dayPanels[day].add(timeEntryPanel);
 	}
 
 	public void clearTimeEntries() {
 		
 		for (JPanel dayPanel : this.dayPanels) {
 			dayPanel.removeAll();
 			dayPanel.revalidate();
 			dayPanel.repaint();
 			this.timeEntryPanels.remove(dayPanel);
 		}
 
 		this.timeEntryPanels = new HashMap<TimeEntry, JPanel>();
 	}
 	
 	public void setFixedEnabled(boolean fixed) {
 		this.cmbActivity.setEnabled(!fixed);
 		this.cmbProject.setEnabled(!fixed);
 		this.btnReserve.setEnabled(!fixed);
 
 		this.cmbActivityType.setEnabled(fixed);
 	}
 	
 	// Component getters
 	public HashMap<TimeEntry, JPanel> getTimeEntryPanels() {
 		return this.timeEntryPanels;
 	}
 	
 	public JComboBox<Project> getProjectComboBox() {
 		return this.cmbProject;
 	}
 	
 	public JComboBox<ActivityType> getActivityTypeComboBox() {
 		return this.cmbActivityType;
 	}
 
 	public JLabel[] getDayLabels() {
 		return this.dayLabels;
 	}
 
 	public JButton getBackButton() {
 		return this.btnBack;
 	}
 
 	public JButton getRegisterButton() {
 		return this.btnRegister;
 	}
 
 	public JButton getReserveButton() {
 		return this.btnReserve;
 	}
 
 	public JButton getNextButton() {
 		return this.btnNext;
 	}
 
 	public JButton getPrevButton() {
 		return this.btnPrevious;
 	}
 
 	public JButton getNextDeveloperButton() {
 		return this.btnNextDeveloper;
 	}
 
 	public JButton getPrevDeveloperButton() {
 		return this.btnPreviousDeveloper;
 	}
 	
 	public JToggleButton getFixedToggleButton() {
 		return this.tglIsFixed;
 	}
 	
 	// Model setters
 	public void setProjects(List<Project> projects) {
 		this.cmbProject.setModel(new GenericComboBoxModel<Project>(projects));
 		this.cmbProject.setRenderer(new ProjectListRenderer());
 	}
 
 	public void setActivities(List<Activity> activities) {
 		this.cmbActivity.setModel(new GenericComboBoxModel<Activity>(activities));
 		this.cmbActivity.setRenderer(new ActivityListRenderer());
 	}
 
 	public void setActivityTypes() {
 		this.cmbActivityType.setModel(new DefaultComboBoxModel<ActivityType>(ActivityType.values()));
 		this.cmbActivityType.setRenderer(new ActivityTypeListRenderer());
 	}
 
 	public void setDeveloperName(String name) {
 		this.lblDeveloperName.setText(name);
 	}
 
 	public void setDateString(String dateString) {
 		this.txtDate.setText(dateString);
 	}
 
 	public void setStartTime(String startTime) {
 		this.txtStart.setText(startTime);
 	}
 
 	public void setEndTime(String endTime) {
 		this.txtEnd.setText(endTime);
 	}
 
 	// Model getters
 	public Project getSelectedProject() {
 		return (Project) this.cmbProject.getSelectedItem();
 	}
 	
 	public Activity getSelectedActivity() {
 		return (Activity) this.cmbActivity.getSelectedItem();
 	}
 
 	public String getDateString() {
 		return this.txtDate.getText();
 	}
 
 	public String getStartTimeString() {
 		return this.txtStart.getText();
 	}
 
 	public String getEndTimeString() {
 		return this.txtEnd.getText();
 	}
 
 	public boolean getFixedState() {
 		return this.getFixedToggleButton().getModel().isSelected();
 	}
 
 	public ActivityType getSelectedActivityType() {
 		return (ActivityType) this.cmbActivityType.getSelectedItem();
 	}
 }
