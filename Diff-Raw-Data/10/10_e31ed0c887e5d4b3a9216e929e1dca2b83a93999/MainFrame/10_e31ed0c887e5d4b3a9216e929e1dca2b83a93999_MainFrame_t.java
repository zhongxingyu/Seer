 package uk.ac.gla.dcs.tp3.w.ui;
 
 import uk.ac.gla.dcs.tp3.w.algorithm.Algorithm;
 import uk.ac.gla.dcs.tp3.w.parser.Parser;
 import uk.ac.gla.dcs.tp3.w.print.LaTeXFile;
 import uk.ac.gla.dcs.tp3.w.gen.GenerateLeague;
 import uk.ac.gla.dcs.tp3.w.league.*;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.*;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.*;
 import javax.swing.RowSorter.SortKey;
 
 public class MainFrame extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private static final double SPACING = 1.25;
 	private HashMap<String, Division> divisions;
 	private Table table;
 	private String[] leagues = { "National", "American" };
 	private String[] divisionNames = { "West", "Central", "East" };
 	private DateTime displayDate, startDate, endDate;
 	private int numDaysToMove = 1;
 	private JLabel dateLabel = new JLabel();
 	private JPanel screenPanel;
 	private final JFileChooser fc = new JFileChooser();
 	private Parser p = new Parser();
 	private LaTeXFile LF;
 	private TableMouseListener listener;
 	@SuppressWarnings("rawtypes")
 	private JComboBox dayBox, monthBox, yearBox;
 
 	private JLabel FirstNonTriv;
 
 	// values for the comboBoxes, need them here to allow editing in inner
 	// classes
 	private Integer[] days = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14,
 			15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30 };
 	private String[] months = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
 			"Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
 	private Integer[] years;
 
 	public MainFrame(HashMap<String, Division> d) {
 		divisions = d;
 		initUI();
 		LF = new LaTeXFile();
 	}
 
 	public final void initUI() {
 		// Set up the panels with appropriate layout managers
 		screenPanel = new JPanel(new BorderLayout());
 		JPanel tablePanel = new JPanel(new BorderLayout());
 		JPanel navPanel = new JPanel(new BorderLayout());
 		JPanel topPanel = new JPanel(new BorderLayout());
 
 		// Add panels to the screen panel and then add that to the JFrame
 		screenPanel.add(topPanel, BorderLayout.PAGE_START);
 		screenPanel.add(tablePanel, BorderLayout.CENTER);
 		screenPanel.add(navPanel, BorderLayout.PAGE_END);
 		getContentPane().add(screenPanel);
 
 		// Display menu bar
 		initMenuBar();
 
 		// Add the JTable for showing league data
 		initTable(tablePanel);
 
 		// Calculate the start and end dates
 		calcStartDate();
 		calcEndDate();
 
 		// Add the panel that shows the previous/next week buttons
 		initNavPanel(navPanel);
 
 		// Update the table and date boxes
 		updateMatchesPlayed();
 
 		// Add the division and league radio buttons
 		initTopPanel(topPanel);
 
 		// Set the JFrame's attributes
 		setTitle("Team W - Algorithms for Sports Eliminations");
 		setLocation(100, 100);
 		setVisible(true);
 		pack();
 		setSize((int) (getWidth() * SPACING), getHeight());
 		setMinimumSize(new Dimension(getWidth(), getHeight()));
 		tablePanel.setSize(getWidth(), 100);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	// Set up menu bar
 	private void initMenuBar() {
 		JMenuBar menuBar;
 		JMenu menu, submenu;
 		JMenuItem menuItem;
 
 		/*
 		 * File Menu
 		 */
 		menuBar = new JMenuBar();
 		menu = new JMenu("File");
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("Open...");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int value = fc.showOpenDialog(screenPanel);
 				if (value == JFileChooser.APPROVE_OPTION) {
 					File file = fc.getSelectedFile();
 					boolean valid = false;
 					try {
 						System.out.println(file.getName());
 						valid = p.parse(file.getAbsolutePath());
 					} catch (Exception ee) {
 						JOptionPane.showMessageDialog(screenPanel,
 								"Invalid file format");
 						return;
 					}
 					if (valid) {
 						divisions = p.getDivisions();
 						table.changeDivisions(divisions);
 						listener.updateDivision(divisions);
 						// Calculate the start and end dates, set table to
 						// display the end date
 						calcStartDate();
 						calcEndDate();
 						updateMatchesPlayed();
 						dateLabel.setText("Current date: "
 								+ displayDate.toString());
 						JOptionPane.showMessageDialog(screenPanel,
 								"Valid file format");
 					} else {
 						JOptionPane.showMessageDialog(screenPanel,
 								"Invalid file format");
 					}
 				}
 			}
 		});
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Generate League...");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int value = fc.showSaveDialog(screenPanel);
 				if (value == JFileChooser.APPROVE_OPTION) {
 					String filename = fc.getSelectedFile().getAbsolutePath();
 					if (!filename.contains(".txt"))
 						filename = filename + ".txt";
 					GenerateLeague gl = new GenerateLeague(filename);
 					if (gl.generate()) {
 						JOptionPane.showMessageDialog(screenPanel,
 								"File Generated!");
 					} else {
 						JOptionPane.showMessageDialog(screenPanel,
 								"File could not be created.");
 					}
 				}
 			}
 		});
 		menu.add(menuItem);
 
 		submenu = new JMenu("Print");
 
 		menuItem = new JMenuItem("Add to print");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				LF.addDivisionFromJTable(table, displayDate);
 				JOptionPane.showMessageDialog(
 						screenPanel,
 						"Added: " + table.getCurrent() + " "
 								+ displayDate.genDate());
 			}
 		});
 
 		submenu.add(menuItem);
 
 		menuItem = new JMenuItem("View current print");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String titles = LF.getTitles();
 				if (titles == null) {
 					JOptionPane.showMessageDialog(screenPanel,
 							"Nothing in print queue");
 				} else {
 					JOptionPane.showMessageDialog(screenPanel, titles);
 				}
 			}
 		});
 
 		submenu.add(menuItem);
 
 		submenu.addSeparator();
 
 		menuItem = new JMenuItem("Clear document");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				LF.clearDocument();
 				JOptionPane.showMessageDialog(screenPanel, "Document Cleared!");
 			}
 		});
 
 		submenu.add(menuItem);
 
 		submenu.addSeparator();
 		menuItem = new JMenuItem("Print...");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (LF.getNumContents() == 0) {
 					JOptionPane.showMessageDialog(screenPanel,
 							"Nothing to be printed!");
 					return;
 				}
 				int value = fc.showSaveDialog(screenPanel);
 				if (value == JFileChooser.APPROVE_OPTION) {
 					String filename = fc.getSelectedFile().getName();
 					String directory = fc.getSelectedFile().getParentFile()
 							.getAbsolutePath();
 					System.out.println(directory);
 					System.out.println(filename);
 					if (LF.write(filename, directory))
 						JOptionPane.showMessageDialog(screenPanel,
 								"File Printed");
 					System.out.println("Print " + table.getCurrent());
 				}
 			}
 		});
 		submenu.add(menuItem);
 
 		menu.add(submenu);
 
 		menuItem = new JMenuItem("Exit");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		});
 		menu.add(menuItem);
 
 		/*
 		 * About Menu
 		 */
 		menu = new JMenu("About");
 		menuBar.add(menu);
 
 		menuItem = new JMenuItem("The Team");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(screenPanel, "We are Team W.\n\n"
 						+ "Gordon Reid: 1002536R\n" + "Ryan Wells: 1002253W\n"
 						+ "David Selkirk: 1003646S\n"
 						+ "James Gallagher: 0800899G\n"
 						+ "Kristopher Stewart: 1007175S");
 			}
 		});
 		menu.add(menuItem);
 
 		this.setJMenuBar(menuBar);
 	}
 
 	// get the start date in the fixtures
 	private void calcStartDate() {
 		Division temp = divisions.get(table.getCurrent());
 		DateTime date = temp.getFixtures().get(0).getDateTime();
 		for (Division d : divisions.values()) {
 			for (Match m : d.getFixtures()) {
 				if (m.getDateTime().before(date))
 					date = m.getDateTime();
 			}
 		}
 		startDate = new DateTime(date);
 		System.out.println("Starting date is: " + date.toString());
 	}
 
 	// get the end date in the fixtures
 	private void calcEndDate() {
 		DateTime date = divisions.get(table.getCurrent()).getFixtures().get(0)
 				.getDateTime();
 		for (Division d : divisions.values()) {
 			for (Match m : d.getFixtures()) {
 				if (!m.getDateTime().before(date))
 					date = m.getDateTime();
 			}
 		}
 		displayDate = date;
 		endDate = new DateTime(date);
 		System.out.println("End date is: " + displayDate.toString());
 	}
 
 	// loop through every game played in the current division,
 	// check if date is less than/equal to current date,
 	// if not unplay match
 	private void updateMatchesPlayed() {
 		// Check the date is in the date range, if not
 		// correct this
 		if (displayDate.before(startDate)) {
 			displayDate = new DateTime(startDate);
 		} else if (!displayDate.before(endDate)) {
 			displayDate = new DateTime(endDate);
 		}
 
 		for (Division d : divisions.values()) {
 			for (Match m : d.getFixtures()) {
 				if (m.getDateTime().before(displayDate)) {
 					m.playMatch();
 				} else {
 					m.unplayMatch();
 				}
 			}
 			for (Team t : d.getTeams()) {
 				t.setEliminated(false);
 				ArrayList<Team> teams = t.getEliminatedBy();
 				t.getEliminatedBy().removeAll(teams);
 			}
 			(new Algorithm(d)).updateDivisionElim();
 		}
 
 		// Update the date selection boxes, date label and model
 		updateComboBoxes();
 		dateLabel.setText("Current date: " + displayDate.toString());
 		table.setCurrent(table.getCurrent());
 	}
 
 	private void initTopPanel(JPanel topPanel) {
 		dateLabel = new JLabel("Current date: " + displayDate.toString(),
 				JLabel.CENTER);
 		topPanel.add(dateLabel, BorderLayout.NORTH);
 		JPanel radioPanel = new JPanel();
 		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
 		initRadioButtons(radioPanel);
 		topPanel.add(radioPanel, BorderLayout.SOUTH);
 	}
 
 	private void initRadioButtons(JPanel radioPanel) {
 		ButtonGroup leagueGroup = new ButtonGroup();
 		ButtonGroup divisionGroup = new ButtonGroup();
 
 		ActionListener leagueListener = new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				JRadioButton rb = null;
 				if (event.getSource() instanceof JRadioButton)
 					rb = (JRadioButton) event.getSource();
 				else
 					return;
 				String s = table.getCurrent();
 				String[] sa = s.split(" ");
 				sa[0] = rb.getText();
 				table.setCurrent(sa[0] + " " + sa[1]);
 				if (table.getCurrentDiv().getFirstNTTeamElim() != null) {
 					FirstNonTriv.setText("The First Team in this Division to"
 							+ " be eliminated in a non trivial manner was "
 							+ table.getCurrentDiv().getFirstNTTeamElim());
 				} else {
 					FirstNonTriv
 							.setText("This division has no non-trivial elimination.");
 				}
 			}
 		};
 		ActionListener divisionListener = new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				JRadioButton rb = null;
 				if (event.getSource() instanceof JRadioButton)
 					rb = (JRadioButton) event.getSource();
 				else
 					return;
 				String s = table.getCurrent();
 
 				String[] sa = s.split(" ");
 				sa[1] = rb.getText();
 				table.setCurrent(sa[0] + " " + sa[1]);
 				if (table.getCurrentDiv().getFirstNTTeamElim() != null) {
 					FirstNonTriv.setText("The First Team in this Division to"
 							+ " be eliminated in a non trivial manner was "
 							+ table.getCurrentDiv().getFirstNTTeamElim());
 				} else {
 					FirstNonTriv
 							.setText("This division has no non-trivial elimination.");
 				}
 			}
 		};
 
 		JPanel leaguePanel = new JPanel();
 		leaguePanel.add(new JLabel("League: "));
 		for (String s : leagues) {
 			JRadioButton rButton1 = new JRadioButton(s);
 			if (s.equals("National"))
 				rButton1.setSelected(true);
 			leagueGroup.add(rButton1);
 			leaguePanel.add(rButton1);
 			rButton1.addActionListener(leagueListener);
 		}
 		radioPanel.add(leaguePanel, BorderLayout.WEST);
 
 		JPanel divisionPanel = new JPanel();
 		divisionPanel.add(new JLabel("Division: "));
 		for (String s : divisionNames) {
 			JRadioButton rButton3 = new JRadioButton(s);
 			if (s.equals("West"))
 				rButton3.setSelected(true);
 			rButton3.addActionListener(divisionListener);
 			divisionGroup.add(rButton3);
 			divisionPanel.add(rButton3);
 		}
 		radioPanel.add(divisionPanel, BorderLayout.CENTER);
 	}
 
 	private void updateComboBoxes() {
		// first reset the day boxes model to stop errors when moving
		// between months with different numbers
		int daysInMonth = Month.daysInMonth(displayDate.getMonth(),
				displayDate.getYear());
		days = new Integer[daysInMonth];
		for (int i = 0; i < daysInMonth; i++)
			days[i] = i + 1;
		dayBox.setModel(new DefaultComboBoxModel(days));
		
		// now update each of the boxes
 		dayBox.setSelectedIndex(displayDate.getDay() - 1);
 		monthBox.setSelectedIndex(displayDate.getMonth() - 1);
 		yearBox.setSelectedIndex(displayDate.getYear() - startDate.getYear());
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private void initNavPanel(JPanel navPanel) {
 		if (table.getCurrentDiv().getFirstNTTeamElim() != null) {
 			FirstNonTriv = new JLabel("The First Team in this Division to"
 					+ " be eliminated in a non trivial manner was "
 					+ table.getCurrentDiv().getFirstNTTeamElim(), JLabel.CENTER);
 		} else {
 			FirstNonTriv = new JLabel(
 					"This division has no non-trivial elimination.",
 					JLabel.CENTER);
 		}
 		navPanel.add(FirstNonTriv, BorderLayout.NORTH);
 
 		// add comboBoxes
 		years = new Integer[(endDate.getYear() - startDate.getYear()) + 1];
 		System.out.println(endDate.getYear() + " " + startDate.getYear());
 		for (int i = 0; i <= endDate.getYear() - startDate.getYear(); i++)
 			years[i] = startDate.getYear() + i;
 		yearBox = new JComboBox(years);
 		// yearBox.setSelectedIndex(displayDate.get)
 		yearBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				displayDate.setYear((Integer) yearBox.getSelectedItem());
 				updateMatchesPlayed();
 			}
 		});
 		monthBox = new JComboBox(months);
 		monthBox.setSelectedIndex(displayDate.getMonth() - 1);
 		// need to be able to reference this in monthBox's handler
 		dayBox = new JComboBox(days);
 		monthBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				// set the month and set the day box's values to match the
 				// selected month
 				int oldDaySelected = dayBox.getSelectedIndex();
 				String m = (String) monthBox.getSelectedItem();
 				displayDate.setMonth(Month.getMonthNumber(m.toUpperCase()));
 				int daysInMonth = Month.daysInMonth(displayDate.getMonth(),
 						displayDate.getYear());
 				days = new Integer[daysInMonth];
 				for (int i = 0; i < daysInMonth; i++)
 					days[i] = i + 1;
 				dayBox.setModel(new DefaultComboBoxModel(days));
 				if (oldDaySelected >= daysInMonth) {
 					oldDaySelected = daysInMonth - 1;
 				}
 				dayBox.setSelectedIndex(oldDaySelected);
 				updateMatchesPlayed();
 			}
 		});
 
 		dayBox.setSelectedIndex(displayDate.getDay() - 1);
 		dayBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				displayDate.setDate((Integer) dayBox.getSelectedItem());
 				updateMatchesPlayed();
 			}
 		});
 
 		// now add the single day navigation buttons
 		JButton backButton = new JButton("Previous Day");
 		backButton.setToolTipText("Move to previous day of results");
 		backButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				// decrement the day
 				// then update the model
 				for (int i = 0; i < numDaysToMove; i++)
 					displayDate.decrementDate();
 				updateMatchesPlayed();
 			}
 		});
 		navPanel.add(backButton, BorderLayout.WEST);
 		JButton nextButton = new JButton("Next Day");
 		nextButton.setToolTipText("Move to next day of results");
 		nextButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				// increment the day
 				// then update the model
 				for (int i = 0; i < numDaysToMove; i++)
 					displayDate.incrementDate();
 				updateMatchesPlayed();
 			}
 		});
 		navPanel.add(nextButton, BorderLayout.EAST);
 
 		JPanel dateSelectionPanel = new JPanel();
 		dateSelectionPanel.add(dayBox);
 		dateSelectionPanel.add(monthBox);
 		dateSelectionPanel.add(yearBox);
 
 		navPanel.add(dateSelectionPanel);
 
 	}
 
 	private void initTable(JPanel tablePanel) {
 		table = new Table(divisions);
 		table.setFillsViewportHeight(true);
 		table.setAutoCreateRowSorter(true);
 		listener = new TableMouseListener(table, divisions, this);
 		table.addMouseListener(listener);
 		DefaultRowSorter<?, ?> sorter = ((DefaultRowSorter<?, ?>) table
 				.getRowSorter());
 		ArrayList<SortKey> list = new ArrayList<SortKey>();
 		list.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
 		sorter.setSortKeys(list);
 		sorter.sort();
 		JScrollPane sP = new JScrollPane(table);
 		tablePanel.add(sP);
 	}
 }
