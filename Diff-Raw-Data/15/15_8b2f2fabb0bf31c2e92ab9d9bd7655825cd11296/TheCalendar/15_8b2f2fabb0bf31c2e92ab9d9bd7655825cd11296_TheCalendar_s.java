 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.table.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.*;
 
 public class TheCalendar {
 
 	static JLabel month;
 	static JLabel year;
 	static JButton prev;
 	static JButton next;
 	static JTable Calendar;
 	@SuppressWarnings("rawtypes")
 	static JComboBox yearBox;
 	static JFrame mainFrame;
 	static Container thePane;
 	static DefaultTableModel calendarTable;
 	static JScrollPane calendarScroll;
 	static JPanel calendarPanel;
 	static int theDay;
 	static int theMonth;
 	static int theYear;
 	static int otherMonth;
 	static int otherYear;
 
 	@SuppressWarnings("unchecked")
 	TheCalendar(){
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (ClassNotFoundException e) {
 
 		}
 		catch (InstantiationException e) {
 
 		}
 		catch (IllegalAccessException e) {
 
 		}
 		catch (UnsupportedLookAndFeelException e) {
 
 		}
 
 		mainFrame = new JFrame("Month View");
 		month = new JLabel("January");
 		year = new JLabel("Change Year:");
 		yearBox = new JComboBox();
 		prev = new JButton("<-");
 		next = new JButton("->");
 		calendarTable = new DefaultTableModel()
 		{
 			public boolean isCellEditable(int rowIndex, int mColIndex)
 			{
 				return false;
 			}
 		};
 		Calendar = new JTable(calendarTable);
 		calendarScroll = new JScrollPane(Calendar);
 		calendarPanel = new JPanel(null);
 
 		mainFrame.setSize(660,750);
 		thePane = mainFrame.getContentPane();
 		thePane.setLayout(null);
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		calendarPanel.setBorder(BorderFactory.createTitledBorder("Calendar"));
 
 		thePane.add(calendarPanel);
 		calendarPanel.add(month);
 		calendarPanel.add(year);
 		calendarPanel.add(yearBox);
 		calendarPanel.add(prev);
 		calendarPanel.add(next);
 		calendarPanel.add(calendarScroll);
 
 		calendarPanel.setBounds(0, 0, 640, 670);
 		month.setBounds(320-month.getPreferredSize().width/2, 50, 200, 50);
 		year.setBounds(20, 610, 160, 40);
 		yearBox.setBounds(460, 610, 160, 40);
 		prev.setBounds(20, 50, 100, 50);
 		next.setBounds(520, 50, 100, 50);
 		calendarScroll.setBounds(20, 100, 600, 500);
 
 		mainFrame.setResizable(false);
 		mainFrame.setVisible(true);
 
 		GregorianCalendar gregCal = new GregorianCalendar();
 		theDay = gregCal.get(GregorianCalendar.DAY_OF_MONTH);
 		theMonth = gregCal.get(GregorianCalendar.MONTH);
 		theYear = gregCal.get(GregorianCalendar.YEAR);
 		otherMonth = theMonth;
 		otherYear = theYear;
 
 		String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
 		for (int i = 0; i < 7; i++){
 			calendarTable.addColumn(days[i]);
 		}
 
 		Calendar.getParent().setBackground(Calendar.getBackground());
 		Calendar.getTableHeader().setResizingAllowed(false);
 		Calendar.getTableHeader().setReorderingAllowed(false);
 		Calendar.setColumnSelectionAllowed(true);
 		Calendar.setRowSelectionAllowed(true);
 		Calendar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		Calendar.setRowHeight(76);
 		calendarTable.setColumnCount(7);
 		calendarTable.setRowCount(6);
 
 		for (int i = theYear-100; i < theYear+100; i++){
 			yearBox.addItem(String.valueOf(i));
 		}
 
 		prev.addActionListener(new prevMonth());
 		next.addActionListener(new nextMonth());
 		yearBox.addActionListener(new changeYear());
 
 		updateCalendar(theMonth, theYear);
 	}
 
 	public static void updateCalendar(int aMonth, int aYear){
 		String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
 		int days;
 		int startOfMonth; 
 
 		prev.setEnabled(true); 
 		next.setEnabled(true);
 
 		if (aMonth == 0 && aYear <= theYear-10){
 			prev.setEnabled(false);
 		} 
 		if (aMonth == 11 && aYear >= theYear+100){
 			next.setEnabled(false);
 		} 
 		month.setText(months[aMonth]); 
 		month.setBounds(320-month.getPreferredSize().width/2, 50, 360, 50); 
 		yearBox.setSelectedItem(String.valueOf(aYear)); 
 
 		GregorianCalendar gregCal = new GregorianCalendar(aYear, aMonth, 1);
 		days = gregCal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
 		startOfMonth = gregCal.get(GregorianCalendar.DAY_OF_WEEK);
 
 		for (int i = 0; i < 6; i++){
 			for (int j = 0; j < 7; j++){
 				calendarTable.setValueAt(null, i, j);
 			}
 		}
<<<<<<< HEAD
 
		for (int i = 1; i <= days; i++){
			int row = new Integer((i+startOfMonth-2)/7);
			int column = (i+startOfMonth-2)%7;
			String template = "<html>%s<br>Event<br>Event<br>Event<html>";
			String stringA = String.valueOf(i);
			String put = String.format(template, stringA);
			calendarTable.setValueAt(put, row, column);
		}
=======
		
 		generateDays(days, startOfMonth);
>>>>>>> 74b58aa6a12093eec0e0c68d22b499c0a5eb1fbf
 
 		DefaultTableCellRenderer render = new DefaultTableCellRenderer();
 		render.setVerticalAlignment(JLabel.TOP);
 
 		for (int i = 0; i < 7; i++){
 			Calendar.getColumnModel().getColumn(i).setCellRenderer(render);
 		}
 	}
 
 	private static void generateDays(int days, int startOfMonth) {
 		System.out.println("------------ MySQL JDBC Connection Testing ------------");
 
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 		} catch (ClassNotFoundException e) {
 			System.out.println("No MySQL JDBC Driver?");
 			e.printStackTrace();
 			return;
 		}
 
 		System.out.println("MySQL JDBC Driver Registered");
 		Connection connection = null;
 
 		try {
 			connection = DriverManager
 					.getConnection("jdbc:mysql://orion.csl.mtu.edu/ajbrowne","ajbrowne", "ajZ4VikY/tnI.");
 
 		} catch (SQLException e) {
 			System.out.println("Connection Failed!");
 			((Throwable) e).printStackTrace();
 			return;
 		}
 
 		if (connection != null) {
 			System.out.println("Now Connected, so please stay and look around!");
 
 
 		} else {
 			System.out.println("Failed to make a connection!");
 		}
 		
 		SendToDB getData = new SendToDB();
 
 		for (int i = 1; i <= days; i++){
 			StoreData data = new StoreData();
 			int row = new Integer((i+startOfMonth-2)/7);
 			int column = (i+startOfMonth-2)%7;
 			theMonth ++;
 			if(i < 10){
 				data.setDate(""+theMonth+"-0"+i+"-"+theYear);
 			}else{
 				data.setDate(""+theMonth+"-"+i+"-"+theYear);
 			}
 			theMonth--;
 			getData.getSpecificData(connection, data);
 			String template = "<html>%s<br>%s<br>%s<br>%s<html>";
 			String stringA = String.valueOf(i);
 			ArrayList<String> theNames = data.getNames();
 			if(theNames == null){
 				continue;
 			}else if(theNames.size() == 1){
 				String put = String.format(template, stringA, theNames.get(0), "","");
 				calendarTable.setValueAt(put, row, column);
 			}else if(theNames.size() == 2){
 				String put = String.format(template, stringA, theNames.get(0), theNames.get(1),"");
 				calendarTable.setValueAt(put, row, column);
 			}else if(theNames.size() == 3){
 				String put = String.format(template, stringA, theNames.get(0), theNames.get(1), theNames.get(2));
 				calendarTable.setValueAt(put, row, column);
 			}else{
 				String put = String.format(template, stringA,"","","");
 				calendarTable.setValueAt(put, row, column);
 			}
 		}
 		try {
 			connection.close();
 		} catch (SQLException e) {
 			System.out.println("The connection was not closed.....Run away now!!!!");
 			e.printStackTrace();
 		}
 	}
 
 	static class prevMonth implements ActionListener{
 		public void actionPerformed (ActionEvent e){
 			if (otherMonth == 0){ 
 				otherMonth = 11;
 				otherYear -= 1;
 				theMonth=11;
 			}
 			else{ 
 				otherMonth -= 1;
 				theMonth --;
 			}
 			updateCalendar(otherMonth, otherYear);
 		}
 	}
 	static class nextMonth implements ActionListener{
 		public void actionPerformed (ActionEvent e){
 			if (otherMonth == 11){ 
 				otherMonth = 0;
 				otherYear += 1;
 				theMonth = 0;
 			}
 			else{ 
 				otherMonth += 1;
 				theMonth ++;
 			}
 			updateCalendar(otherMonth, otherYear);
 		}
 	}
 	static class changeYear implements ActionListener{
 		public void actionPerformed (ActionEvent e){
 			if (yearBox.getSelectedItem() != null){
 				String b = yearBox.getSelectedItem().toString();
 				otherYear = Integer.parseInt(b);
 				theYear = otherYear;
 				updateCalendar(otherMonth, otherYear);
 			}
 		}
 	}
 
 }
