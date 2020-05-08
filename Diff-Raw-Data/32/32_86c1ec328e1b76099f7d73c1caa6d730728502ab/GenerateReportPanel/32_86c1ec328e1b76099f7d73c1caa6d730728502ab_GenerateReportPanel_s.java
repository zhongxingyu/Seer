 package GUI;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.DefaultTableModel;
 
 import main.Log;
 import main.LogEntry;
 import main.Statistics;
 import main.SystemAdministrator;
 
 public class GenerateReportPanel extends ContentPanel {
 
 	private JComboBox<String> parameters;
 	private ButtonListener buttonListener;
 	private ComboBoxListener comboBoxListener;
 	private JButton generateButton;
 	private JPanel dataEntryPanel;
 	private JPanel resultsPanel;
 	private JScrollPane scroller1;
 	private JScrollPane scroller2;
 	private JTable table;
 	private JTabbedPane tabs;
 	
 	JTextField startField;
 	JTextField endField;
 	JTextField cwidField;
 	JTextField toolNameField;
 	JTextField machineNameField;
 	
 	Calendar start;
 	Calendar end;
 	
 	JComboBox<String> startMonths;
 	JComboBox<String> endMonths;
 	JComboBox<String> startDays;
 	JComboBox<String> endDays;
 	JComboBox<String> startYears;
 	JComboBox<String> endYears;
 	
 	int sMonth, sDay, sYear, eMonth, eDay, eYear;
 	
 	private String currentParameter;
 	
 	public GenerateReportPanel() {
 		
 		super("Generate a Report");
 		buttonListener = new ButtonListener();
 		comboBoxListener = new ComboBoxListener();
 		
 		parameters = new JComboBox<String>();
 		JLabel parameterLabel = new JLabel("Report Parameter:");
 		
 		parameters.addItem("Date");
 		parameters.addItem("User");
 		parameters.addItem("Tool");
 		parameters.addItem("Machine");
 		parameters.addActionListener(comboBoxListener);
 		
 		currentParameter = "Date";
 		
 		setDefaultDate();
 		
 		parameterLabel.setFont(buttonFont);
 		parameters.setFont(textFont);
 		
 		generateButton = new JButton("Generate");
 		generateButton.setFont(buttonFont);
 		generateButton.addActionListener(buttonListener);
 		
 		JPanel parameterPanel = new JPanel(new GridLayout(1, 2));
 		
 		parameterPanel.add(parameterLabel);
 		parameterPanel.add(parameters);
 		
 		dataEntryPanel = new JPanel(new GridBagLayout());
 		dataEntryPanel.setBorder(new TitledBorder("Parameters"));
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 1;
 		dataEntryPanel.add(new DatePanel(), c);
 		
 		resultsPanel = new JPanel();
 		resultsPanel.setBorder(new TitledBorder("Results"));
 		resultsPanel.setLayout(new GridLayout(0, 1));
 		
 		scroller2 = new JScrollPane(resultsPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		
 		scroller1 = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		tabs = new JTabbedPane(); 
 		
 		tabs.addTab("Statistics", scroller2);
 		tabs.addTab("Results", scroller1);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.1;
 		c.gridx = 0;
 		c.gridy = 0;
 		add(new JPanel(), c);
 		
 		c.fill = GridBagConstraints.NONE;
 		c.weightx = 0.8;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 0;
 		add(title, c);
 		
 		c.fill = GridBagConstraints.NONE;
 		c.weightx = 0.1;
 		c.gridx = 2;
 		c.gridy = 0;
 		add(new JPanel(), c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 1;
 		add(parameterPanel, c);
 		
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 2;
 		add(dataEntryPanel, c);
 		
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.1;
 		c.gridx = 1;
 		c.gridy = 3;
 		add(generateButton, c);
 		
 		c.fill = GridBagConstraints.BOTH;
 		c.weighty = 0.5;
 		c.gridx = 1;
 		c.gridy = 4;
 		add(tabs, c);
 		
 		c.weighty = 0.1;
 		c.gridy = 5;
 		add(new JPanel(), c);
 		
 	}
 	
 	private void setDefaultDate() {
 		Calendar today = Calendar.getInstance();
 		sYear = today.get(Calendar.YEAR);
 		eYear = today.get(Calendar.YEAR);
 		sMonth = today.get(Calendar.MONTH);
 		eMonth = today.get(Calendar.MONTH);
 		sDay = today.get(Calendar.DATE);
 		eDay = today.get(Calendar.DATE) ;
 	}
 	
 	private class ComboBoxListener implements ActionListener {
 		
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource() == parameters) {
 				String parameter = parameters.getSelectedItem().toString();
 				c.fill = GridBagConstraints.HORIZONTAL;
 				c.weightx = 1;
 				if (parameter == "Date") {
 					currentParameter = "Date";
 					dataEntryPanel.removeAll();
 					dataEntryPanel.add(new DatePanel(), c);
 				} else if (parameter == "User") {
 					currentParameter = "User";
 					dataEntryPanel.removeAll();
 					dataEntryPanel.add(new UserPanel(), c);
 				} else if (parameter == "Tool") {
 					currentParameter = "Tool";
 					dataEntryPanel.removeAll();
 					dataEntryPanel.add(new ToolPanel(), c);
 				} else if (parameter == "Machine") {
 					currentParameter = "Machine";
 					dataEntryPanel.removeAll();
 					dataEntryPanel.add(new MachinePanel(), c);
 				}
 			} else if (e.getSource() == startMonths) {
 				sMonth = Integer.parseInt(startMonths.getSelectedItem().toString()) - 1;
 			} else if (e.getSource() == startDays) {
 				sDay = Integer.parseInt(startDays.getSelectedItem().toString());
 			} else if (e.getSource() == startYears) {
 				sYear = Integer.parseInt(startYears.getSelectedItem().toString());
 			} else if (e.getSource() == endMonths) {
 				eMonth = Integer.parseInt(endMonths.getSelectedItem().toString()) - 1;
 			} else if (e.getSource() == endDays) {
 				eDay = Integer.parseInt(endDays.getSelectedItem().toString());
 			} else if (e.getSource() == endYears) {
 				eYear =	Integer.parseInt(endYears.getSelectedItem().toString());
 			}
 		}
 	}
 	
 	private void showResults() {
 		String[] columns = {"Entry ID", "User", "Time In", "Time Out", "Machines Used", 
 		                    "Tools Checked Out", "Tools Returned"};
		String data[][] = new String[Log.getResults().size()][7];
		if (Log.getResults().size() > 0) {
			for (LogEntry entry : Log.getResults()) {	
				data[Log.getResults().indexOf(entry)][0] = Integer.toString(entry.getID());
				data[Log.getResults().indexOf(entry)][1] = entry.getCwid();
				data[Log.getResults().indexOf(entry)][2] = entry.getTimeIn().toString();
 				
 				String timeOut = "";
 				if (entry.getTimeOut() == null) {
					data[Log.getResults().indexOf(entry)][3] = timeOut;
 				} else {
					data[Log.getResults().indexOf(entry)][3] = entry.getTimeOut().toString();
 				}
 				
				data[Log.getResults().indexOf(entry)][4] = entry.getMachinesUsed().toString();
				data[Log.getResults().indexOf(entry)][5] = entry.getToolsCheckedOut().toString();
				data[Log.getResults().indexOf(entry)][6] = entry.getToolsReturned().toString();
 			}
 		}
 		tabs.removeTabAt(1);
 		table = new JTable(data, columns);
 		scroller1 = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 		tabs.addTab("Results", scroller1);
 	}
 	
 	private void showStatistics() {
 		Statistics stats = new Statistics();
 		stats.run();
 		JLabel numEntries = new JLabel("Number of Log Entries: " + Integer.toString(stats.getNumEntries()));
 		JLabel numUsers = new JLabel("Number of Users: " + Integer.toString(stats.getNumUsers()));
 		JLabel numTools = new JLabel("Number of Tools Used: " + Integer.toString(stats.getNumTools()));
 		JLabel numMachines = new JLabel("Number of Machines Used: " + Integer.toString(stats.getNumMachines()));
 		
 		numEntries.setFont(resultsFont);
 		numUsers.setFont(resultsFont);
 		numTools.setFont(resultsFont);
 		numMachines.setFont(resultsFont);
 		
 		resultsPanel.add(numEntries);
 		resultsPanel.add(numUsers);
 		resultsPanel.add(numTools);
 		resultsPanel.add(numMachines);
 		
 		JLabel machineFreqTitle = new JLabel("Machine Frequencies");
 		machineFreqTitle.setFont(titleFont);
 		resultsPanel.add(machineFreqTitle);
 		for (Map.Entry entry : stats.getMachineFrequencies().entrySet()) {
 			JLabel machineFreq = new JLabel(entry.getKey() + ": " + entry.getValue());
 			machineFreq.setFont(resultsFont);
 			resultsPanel.add(machineFreq);
 		}
 		
 		JLabel toolFreqTitle = new JLabel("Tool Frequencies");
 		toolFreqTitle.setFont(titleFont);
 		resultsPanel.add(toolFreqTitle);
 		for (Map.Entry entry : stats.getToolsFrequencies().entrySet()) {
 			JLabel toolFreq = new JLabel(entry.getKey() + ": " + entry.getValue());
 			toolFreq.setFont(resultsFont);
 			resultsPanel.add(toolFreq);
 		}
 		
 		int seconds = (int) (stats.getAvgTimeLoggedIn() / 1000) % 60;
		int minutes = (int) (stats.getAvgTimeLoggedIn() / (1000*60)) % 60;
		int hours = (int) (stats.getAvgTimeLoggedIn() / (1000*60*60)) % 24;
 		
 		System.out.println(stats.getAvgTimeLoggedIn());
 		System.out.println(hours + " " + minutes + " " + seconds);
 		
 		String avg = String.format("%d Hours, %d Minutes, %d Seconds", hours, minutes, seconds);
 		JLabel avgTime = new JLabel("Avg Time Logged In: " + avg);
 		avgTime.setFont(resultsFont);
 		resultsPanel.add(avgTime);
 	
 	}
 	
 	private void showReport() {
 		showResults();
 		showStatistics();
 	}
 	
 	private void setDates() {
 		start = Calendar.getInstance();
 		end = Calendar.getInstance();
 		start.set(sYear, sMonth, sDay, 0, 0, 0);
 		end.set(eYear, eMonth, eDay, 23, 59, 59);
 	}
 	
 	private class ButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource() == generateButton) {
 				if (currentParameter.equals("User")) {
 					resultsPanel.removeAll();
 					setDates();
 					String cwid = cwidField.getText();
 					Log.extractLog(start.getTime(), end.getTime(), true);
 					Log.extractResultsByUser(cwid);
 					showReport();
 				} else if (currentParameter.equals("Date")) {
 					resultsPanel.removeAll();		
 					setDates();
 					Log.extractLog(start.getTime(), end.getTime(), true);
 					showReport();
 				} else if (currentParameter.equals("Tool")) {
 					resultsPanel.removeAll();
 					setDates();
 					String tool = toolNameField.getText();
 					Log.extractLog(start.getTime(), end.getTime(), true);
 					Log.extractResultsByTool(tool);
 					showReport();
 				} else if (currentParameter.equals("Machine")) {
 					resultsPanel.removeAll();
 					setDates();
 					String machine = machineNameField.getText();
 					Log.extractLog(start.getTime(), end.getTime(), true);
 					Log.extractResultsByMachine(machine);
 					showReport();
 				}
 			}
 		}
 	}
 	
 	private class DatePanel extends JPanel {
 		
 		DatePanel() {
 			
 			Font smallFont = new Font("SansSerif", Font.BOLD, 18);
 			
 			setLayout(new GridLayout(2, 2));
 			
 			JLabel startMonth = new JLabel("START: Month ");
 			JLabel endMonth = new JLabel("END: Month ");
 			JLabel startDay = new JLabel("Day ");
 			JLabel endDay = new JLabel("Day ");
 			JLabel startYear = new JLabel("Year ");
 			JLabel endYear = new JLabel("Year ");
 			
 			startMonth.setFont(smallFont);
 			endMonth.setFont(smallFont);
 			startDay.setFont(smallFont);
 			endDay.setFont(smallFont);
 			startYear.setFont(smallFont);
 			endYear.setFont(smallFont);
 			
 			startMonth.setHorizontalAlignment(JLabel.RIGHT);
 			endMonth.setHorizontalAlignment(JLabel.RIGHT);
 			startDay.setHorizontalAlignment(JLabel.RIGHT);
 			endDay.setHorizontalAlignment(JLabel.RIGHT);
 			startYear.setHorizontalAlignment(JLabel.RIGHT);
 			endYear.setHorizontalAlignment(JLabel.RIGHT);
 			
 			
 			final String[] MONTHS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
 			
 			final String[] DAYS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", 
                 "12", "13", "14", "15", "16", "17", "18", "19", "20", 
                 "21", "22", "23", "24", "25", "26", "27", "28", "29", 
                 "30", "31"};
 			
 			final String[] YEARS = {"2013", "2014", "2015", "2016", "2017", "2018", "2019", 
 					"2020", "2021", "2022", "2023", "2024", "2025", "2026", "2027", "2028",
 					"2029", "2030", "2031", "2032", "2033", "2034", "2035", "2036", "2037",
 					"2038", "2039", "2040", "2041", "2042", "2043", "2044", "2045", "2046", 
 					"2047", "2048", "2049", "2050"};
 			
 			startMonths = new JComboBox<String>();
 			for (String i : MONTHS) {
 				startMonths.addItem(i);
 			}
 			startMonths.addActionListener(new ComboBoxListener());
 			
 			startMonths.setSelectedItem(Integer.toString(sMonth + 1));
 			
 			endMonths = new JComboBox<String>();
 			for (String i : MONTHS) {
 				endMonths.addItem(i);
 			}
 			endMonths.addActionListener(new ComboBoxListener());
 			
 			endMonths.setSelectedItem(Integer.toString(eMonth + 1));
 			
 			startDays = new JComboBox<String>();
 			for (String i : DAYS) {
 				startDays.addItem(i);
 			}
 			startDays.addActionListener(new ComboBoxListener());
 			
 			startDays.setSelectedItem(Integer.toString(sDay));
 			
 			endDays = new JComboBox<String>();
 			for (String i : DAYS) {
 				endDays.addItem(i);
 			}
 			endDays.addActionListener(new ComboBoxListener());
 			
 			endDays.setSelectedItem(Integer.toString(eDay));
 			
 			startYears = new JComboBox<String>();
 			for (String i : YEARS) {
 				startYears.addItem(i);
 			}
 			startYears.addActionListener(new ComboBoxListener());
 			
 			startYears.setSelectedItem(Integer.toString(sYear));
 			
 			endYears = new JComboBox<String>();
 			for (String i : YEARS) {
 				endYears.addItem(i);
 			}
 			endYears.addActionListener(new ComboBoxListener());
 			
 			endYears.setSelectedItem(Integer.toString(eYear));
 			
 			add(startMonth);
 			add(startMonths);
 			add(startDay);
 			add(startDays);
 			add(startYear);
 			add(startYears);
 			add(endMonth);
 			add(endMonths);
 			add(endDay);
 			add(endDays);
 			add(endYear);
 			add(endYears);
 						
 		}
 		
 	}
 	
 	private class UserPanel extends JPanel {
 		
 		UserPanel() {
 			setLayout(new GridBagLayout());
 			
 			JPanel cwid = new JPanel();
 			cwid.setLayout(new GridLayout(1, 2));
 			
 			JLabel cwidLabel = new JLabel("Enter User's CWID:");
 			cwidLabel.setHorizontalAlignment(JLabel.CENTER);
 			
 			cwidField = new JTextField();
 			cwidLabel.setFont(buttonFont);
 			cwidField.setFont(textFont);
 			
 			cwid.add(cwidLabel);
 			cwid.add(cwidField);
 	
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.weightx = 0.1;
 			c.weighty = 0.5;
 			c.gridx = 0;
 			c.gridy = 0;
 			c.ipady = 10;
 			add(cwid, c);
 			
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.weightx = 0.1;
 			c.weighty = 0.5;
 			c.gridx = 0;
 			c.gridy = 2;
 			add(new DatePanel(), c);
 		}
 		
 	}
 
 	private class ToolPanel extends JPanel {
 		ToolPanel() {
 			setLayout(new GridBagLayout());
 			
 			JPanel tool = new JPanel();
 			tool.setLayout(new GridLayout(1, 2));
 			
 			JLabel toolNameLabel = new JLabel("Enter Name of Tool:");
 			toolNameLabel.setHorizontalAlignment(JLabel.CENTER);
 			
 			toolNameField = new JTextField();
 			toolNameLabel.setFont(buttonFont);
 			toolNameField.setFont(textFont);
 			
 			tool.add(toolNameLabel);
 			tool.add(toolNameField);
 
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.weightx = 0.1;
 			c.weighty = 0.5;
 			c.gridx = 0;
 			c.gridy = 0;
 			c.ipady = 10;
 			add(tool, c);
 			
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.weightx = 0.1;
 			c.weighty = 0.5;
 			c.gridx = 0;
 			c.gridy = 2;
 			add(new DatePanel(), c);
 		}
 	}
 	
 	private class MachinePanel extends JPanel {
 		MachinePanel() {
 			setLayout(new GridBagLayout());
 			
 			JPanel machine = new JPanel();
 			machine.setLayout(new GridLayout(1, 2));
 			
 			JLabel machineNameLabel = new JLabel("Enter Name of Machine:");
 			machineNameLabel.setHorizontalAlignment(JLabel.CENTER);
 			
 			machineNameField = new JTextField();
 			machineNameLabel.setFont(buttonFont);
 			machineNameField.setFont(textFont);
 			
 			machine.add(machineNameLabel);
 			machine.add(machineNameField);
 			
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.weightx = 0.1;
 			c.weighty = 0.5;
 			c.gridx = 0;
 			c.gridy = 0;
 			c.ipady = 10;
 			add(machine, c);
 			
 			c.fill = GridBagConstraints.HORIZONTAL;
 			c.weightx = 0.1;
 			c.weighty = 0.5;
 			c.gridx = 0;
 			c.gridy = 2;
 			add(new DatePanel(), c);
 		}
 	}
 	
 }
