 package js;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.text.DateFormatSymbols;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 
 /**
  * JSDateEntry provides a number of different ways for the user to enter a date.
  * <code>DROPDOWN_VIEW</code> provides three dropdown lists, one each for date, month and year.
  * <code>MULTI_TEXTFIELD_VIEW</code> provides three text fields for the user to type in the date, month and year.
  * <code>CALENDAR_BUTTON_VIEW</code> provides an uneditable text field and a button to open a popup calendar, where the user can click a date to select it.<br><br>
  * 
  * A JSDateEntry component can be given an initial date, and a limit can be set on the number of years to show in the past and future from the current date.
  * 
  * @author Josh Sunshine
  * @version 1.0
  */
 public class JSDateEntry extends JComponent implements KeyListener, FocusListener, ActionListener {
 	
 	private static final long serialVersionUID = 1013338297375582600L;
 	public static final int DROPDOWN_VIEW = 0;
 	public static final int REVERSE_DROPDOWN_VIEW = 1;
 	public static final int MULTI_TEXTFIELD_VIEW = 2;
 	public static final int CALENDAR_BUTTON_VIEW = 3;
 	
 	private int viewType;
 	private Calendar date;
 	private int yearsAhead;
 	private int yearsBehind;
 	private Calendar currentDate;
 	
 	private JComboBox dayBox, monthBox, yearBox;
 	private JTextField textField, dayField, monthField, yearField;
 	private JButton calendarButton;
 	
 	private JDialog calendarDialog;
 	private JSGridPanel calendarPanel;
 	private JButton[] dayList;
 	private JButton leftArrow, rightArrow, todayButton, closeCalendarButton;
 //	private JComboBox monthSelection, yearSelection;
 	private int dialogMonth, dialogYear;
 	private JLabel calendarTitle;
 	private boolean todayShowing;
 	
	private boolean isMacOS;
	
 	/**
 	 * Creates a new JSDateEntry using <code>DROPDOWN_VIEW</code> and no initial date.
 	 * The year dropdown will contain 25 past years and 5 future years. 
 	 */
 	public JSDateEntry() {
 		this(DROPDOWN_VIEW, null, 25, 5);
 	}
 	
 	/**
 	 * Creates a new JSDateEntry with the specified view type and no initial date.
 	 * If <code>DROPDOWN_VIEW</code> or <code>CALENDAR_BUTTON_VIEW</code> are used, they will display 25
 	 * past years and 5 future years.
 	 * 
 	 * @param viewType an integer representing the type of entry to use for this JSDateEntry.
 	 */
 	public JSDateEntry(int viewType) {
 		this(viewType, null, 25, 5);
 	}
 	
 	/**
 	 * Creates a new JSDateEntry using <code>DROPDOWN_VIEW</code> and the specified initial date.
 	 * The year dropdown will contain 25 past years and 5 future years. 
 	 * 
 	 * @param initialDate the date to set this JSDateEntry to when initialising.
 	 */
 	public JSDateEntry(Calendar initialDate) {
 		this(DROPDOWN_VIEW, initialDate, 25, 5);
 	}
 	
 	/**
 	 * Creates a new JSDateEntry with the specified view type and the specified initial date.
 	 * If <code>DROPDOWN_VIEW</code> or <code>CALENDAR_BUTTON_VIEW</code> are used, they will display 25
 	 * past years and 5 future years.
 	 * 
 	 * @param viewType an integer representing the type of entry to use for this JSDateEntry.
 	 * @param initialDate the date to set this JSDateEntry to when initialising.
 	 */
 	public JSDateEntry(int viewType, Calendar initialDate) {
 		this(viewType, initialDate, 25, 5);
 	}
 	
 	/**
 	 * Creates a new JSDateEntry with the specified view type and the specified initial date.
 	 * If <code>DROPDOWN_VIEW</code> or <code>CALENDAR_BUTTON_VIEW</code> are used, they will display the specified
 	 * number of years in the past and 5 years in the future.
 	 * 
 	 * @param viewType an integer representing the type of entry to use for this JSDateEntry.
 	 * @param initialDate the date to set this JSDateEntry to when initialising.
 	 * @param yearsBehind an integer number of years to display in this JSDateEntry in the past from the current year.
 	 */
 	public JSDateEntry(int viewType, Calendar initialDate, int yearsBehind) {
 		this(viewType, initialDate, yearsBehind, 5);
 	}
 	
 	
 	/**
 	 * Creates a new JSDateEntry with the specified view type and the specified initial date.
 	 * If <code>DROPDOWN_VIEW</code> or <code>CALENDAR_BUTTON_VIEW</code> are used, they will display the specified
 	 * number of years in the past and the future.
 	 * 
 	 * @param viewType an integer representing the type of entry to use for this JSDateEntry.
 	 * @param initialDate initialDate the date to set this JSDateEntry to when initialising.
 	 * @param yearsBehind an integer number of years to display in this JSDateEntry in the past from the current year.
 	 * @param yearsAhead an integer number of years to display in this JSDateEntry in the future from the current year.
 	 */
 	public JSDateEntry(int viewType, Calendar initialDate, int yearsBehind, int yearsAhead) {
 		
		isMacOS = System.getProperty("os.name").contains("Mac");
		
 		this.viewType = viewType;
 		this.date = initialDate;
 		this.yearsBehind = yearsBehind;
 		this.yearsAhead = yearsAhead;
 		
 		currentDate = initialDate;
 		
 		Calendar today = Calendar.getInstance();
 		
 		setLayout(null);
 		
 		switch (this.viewType) {
 		case DROPDOWN_VIEW:
 			dayBox = new JComboBox();
 			dayBox.addItem("DD");
 			for (int i = 1; i <= 31; i ++) {
 				if (i >= 10)
 					dayBox.addItem(i);
 				else
 					dayBox.addItem("0" + i);
 			}
 			dayBox.setBounds(0, 0, 80, 25);
 			
 			monthBox = new JComboBox();
 			monthBox.addItem("MM");
 			for (int i = 1; i <= 12; i ++) {
 				if (i >= 10)
 					monthBox.addItem(i);
 				else
 					monthBox.addItem("0" + i);
 			}
 			monthBox.setBounds(85, 0, 80, 25);
 			
 			yearBox = new JComboBox();
 			yearBox.addItem("YYYY");
 			for (int i = today.get(Calendar.YEAR) - yearsBehind; i <= today.get(Calendar.YEAR) + yearsAhead; i ++) {
 				yearBox.addItem(i);
 			}
 			yearBox.setBounds(170, 0, 90, 25);
 			
 			add(dayBox);
 			add(monthBox);
 			add(yearBox);
 			
 			if (this.date != null) {
 				dayBox.setSelectedIndex(date.get(Calendar.DATE));
 				monthBox.setSelectedIndex(date.get(Calendar.MONTH));
 				yearBox.setSelectedItem(date.get(Calendar.YEAR));
 			}
 			
 			break;
 			
 		case REVERSE_DROPDOWN_VIEW:
 			dayBox = new JComboBox();
 			dayBox.addItem("DD");
 			for (int i = 1; i <= 31; i ++) {
 				if (i >= 10)
 					dayBox.addItem(i);
 				else
 					dayBox.addItem("0" + i);
 			}
 			dayBox.setBounds(180, 0, 80, 25);
 			
 			monthBox = new JComboBox();
 			monthBox.addItem("MM");
 			for (int i = 1; i <= 12; i ++) {
 				if (i >= 10)
 					monthBox.addItem(i);
 				else
 					monthBox.addItem("0" + i);
 			}
 			monthBox.setBounds(95, 0, 80, 25);
 			
 			yearBox = new JComboBox();
 			yearBox.addItem("YYYY");
 			for (int i = today.get(Calendar.YEAR) - yearsBehind; i <= today.get(Calendar.YEAR) + yearsAhead; i ++) {
 				yearBox.addItem(i);
 			}
 			yearBox.setBounds(0, 0, 90, 25);
 			
 			add(dayBox);
 			add(monthBox);
 			add(yearBox);
 			
 			if (this.date != null) {
 				dayBox.setSelectedIndex(date.get(Calendar.DATE));
 				monthBox.setSelectedIndex(date.get(Calendar.MONTH) + 1);
 				yearBox.setSelectedItem(date.get(Calendar.YEAR));
 			}
 			
 			break;
 			
 		case MULTI_TEXTFIELD_VIEW:
 			
 			dayField = new JTextField();
 			dayField.setBounds(0, 0, 50, 30);
 			dayField.setHorizontalAlignment(JTextField.CENTER);
 			dayField.addFocusListener(this);
 			dayField.addKeyListener(this);
 			add(dayField);
 			
 			monthField = new JTextField();
 			monthField.setBounds(55, 0, 50, 30);
 			monthField.setHorizontalAlignment(JTextField.CENTER);
 			monthField.addFocusListener(this);
 			monthField.addKeyListener(this);
 			add(monthField);
 			
 			yearField = new JTextField();
 			yearField.setBounds(110, 0, 70, 30);
 			yearField.setHorizontalAlignment(JTextField.CENTER);
 			yearField.addFocusListener(this);
 			yearField.addKeyListener(this);
 			add(yearField);
 			
 			if (this.date != null) {
 				yearField.setText(Integer.toString(date.get(Calendar.YEAR)));
 				if (date.get(Calendar.MONTH) > 9)
 					monthField.setText(Integer.toString(date.get(Calendar.MONTH)));
 				else
 					monthField.setText("0" + Integer.toString(date.get(Calendar.MONTH)));
 				if (date.get(Calendar.DATE) > 9)
 					dayField.setText(Integer.toString(date.get(Calendar.DATE)));
 				else
 					dayField.setText("0" + Integer.toString(date.get(Calendar.DATE)));
 			} else {
 				yearField.setText("YYYY");
 				monthField.setText("MM");
 				dayField.setText("DD");
 			}
 			
 			break;
 			
 		case CALENDAR_BUTTON_VIEW:
 			
 			textField = new JTextField();
 			textField.setEditable(false);
 			textField.setBounds(0, 0, 120, 30);
 			if (this.date != null) {
 				int day = this.date.get(Calendar.DATE);
 				String dayString = "";
 				if (day > 9)
 					dayString = Integer.toString(day);
 				else
 					dayString = "0" + Integer.toString(day);
 				int month = this.date.get(Calendar.MONTH) + 1;
 				String monthString = "";
 				if (month > 9)
 					monthString = Integer.toString(month);
 				else
 					monthString = "0" + Integer.toString(month);
 				textField.setText(dayString + "/" + monthString + "/" + Integer.toString(this.date.get(Calendar.YEAR)));
 			} else
 				textField.setText("- - -");
 			textField.setFont(this.getFont());
 			textField.setForeground(this.getForeground());
 			textField.setHorizontalAlignment(JTextField.CENTER);
 			add(textField);
 			
 			ImageIcon calendarButtonIcon = new ImageIcon("images/DownArrow.png");
 			calendarButton = new JButton(calendarButtonIcon);
 			calendarButton.setBounds(119, 0, 30, 30);
 			calendarButton.addActionListener(this);
 			calendarButton.setBackground(this.getBackground());
 			add(calendarButton);
 			
 			break;
 			
 		}
 	}
 	
 	/**
 	 * Returns the current date from this JSDateEntry.
 	 * 
 	 * @return a Calendar object representing the date entered into this JSDate entry, or <code>null<code> if one has not been entered.
 	 */
 	public Calendar getDate() {
 		Calendar date = Calendar.getInstance();
 		
 		switch (viewType) {
 		case DROPDOWN_VIEW:
 			try {
 				date.set((Integer) yearBox.getSelectedItem(), monthBox.getSelectedIndex(), dayBox.getSelectedIndex());
 			} catch (ClassCastException e) {
 				date = null;
 			}
 			break;
 			
 		case REVERSE_DROPDOWN_VIEW:
 			try {
 				date.set((Integer) yearBox.getSelectedItem(), monthBox.getSelectedIndex(), dayBox.getSelectedIndex());
 			} catch (ClassCastException e) {
 				date = null;
 			}
 			break;
 			
 		case MULTI_TEXTFIELD_VIEW:
 			try {
 				date.set(Integer.parseInt(yearField.getText()), Integer.parseInt(monthField.getText()), Integer.parseInt(dayField.getText()));
 			} catch (NumberFormatException e) {
 				date = null;
 			}
 			break;
 			
 		case CALENDAR_BUTTON_VIEW:
 			date = currentDate;
 			break;
 		}
 		
 		return date;
 	}
 	
 	/**
 	 * Sets the background color of the calendar button in <code>CALENDAR_BUTTON_VIEW</code> to the specified color.
 	 * 
 	 * @param color the color to set the background of the calendar button to 
 	 */
 	public void setButtonColor(Color color) {
 		if (viewType == CALENDAR_BUTTON_VIEW)
 			calendarButton.setBackground(color);
 	}
 	
 	public void setFont(Font font) {
 		if (viewType == MULTI_TEXTFIELD_VIEW) {
 			dayField.setFont(font);
 			monthField.setFont(font);
 			yearField.setFont(font);
 		} else if (viewType == CALENDAR_BUTTON_VIEW) {
 			textField.setFont(font);
 		}
 	}
 	
 	private void updateCalendar(int month, int year) {
 	
 		if (calendarDialog != null) {
 			calendarDialog.setVisible(false);
 			calendarDialog.dispose();
 		}
 		
 		JSGridPanel dayLabelPanel = new JSGridPanel(1, 7);
 		dayLabelPanel.setBounds(0, 35, 252, 30);
 		
 		String initials[] = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
 		JLabel dayLabels[] = new JLabel[7];
 		for (int i = 0; i < 7; i ++) {
 			dayLabels[i] = new JLabel(initials[i], JLabel.CENTER);
 			dayLabelPanel.addComponent(dayLabels[i]);
 		}
 		
 		calendarDialog = new JDialog();
 		calendarDialog.setLayout(null);
 		calendarDialog.setModal(true);
 		calendarDialog.setSize(252, 270);
 		calendarDialog.setLocation(calendarButton.getLocationOnScreen().x, calendarButton.getLocationOnScreen().y);
 		calendarDialog.setUndecorated(true);
 		calendarDialog.add(dayLabelPanel);
 		
 		if (calendarTitle != null) {
 			calendarDialog.remove(calendarTitle);
 			calendarDialog.remove(leftArrow);
 			calendarDialog.remove(rightArrow);
 			calendarDialog.remove(closeCalendarButton);
 			calendarDialog.remove(todayButton);
 			calendarDialog.remove(calendarPanel);
 			calendarDialog.repaint();
 		}
 		
 		dialogMonth = month;
 		dialogYear = year;
 		
 		calendarPanel = new JSGridPanel(6, 7);
 		calendarPanel.setBounds(0, 65, 252, 162);
 		calendarPanel.setBackgroundColor(new Color(200, 222, 255));
 		dayList = new JButton[getDaysInMonth(month, year)];
 		JPanel empty[] = new JPanel[21];
 		for (int i = 0; i < 21; i ++) {
 			empty[i] = new JPanel();
 			empty[i].setBackground(new Color(210, 210, 210));
 			empty[i].setOpaque(true);
 		}
 		
 		GregorianCalendar date = new GregorianCalendar();
 		date.set(year, month, 1);
 		int firstOfMonth = date.get(Calendar.DAY_OF_WEEK);
 		date.set(year, month, getDaysInMonth(month, year));
 		
 		todayShowing = false;
 		GregorianCalendar today = new GregorianCalendar();
 		if (month == today.get(Calendar.MONTH) && year == today.get(Calendar.YEAR))
 			todayShowing = true;
 		
 		if (firstOfMonth > 1) {
 			for (int i = 0; i < firstOfMonth - 2; i ++) {
 				calendarPanel.addComponent(empty[i]);
 			}
 		} else if (firstOfMonth == 1) {
 			for (int i = 0; i < 6; i ++) {
 				calendarPanel.addComponent(empty[i]);
 			}
 		}
 		
 		dayList = new JButton[getDaysInMonth(month, year)];
 		for (int i = 0; i < getDaysInMonth(month, year); i ++) {
 			dayList[i] = new JButton(Integer.toString(i + 1));
 			if (i + 1 == today.get(Calendar.DATE) && todayShowing) {
 				dayList[i].setForeground(new Color(72, 139, 245));
				if (isMacOS)
					dayList[i].setFont(dayList[i].getFont().deriveFont(Font.BOLD));
				else
					dayList[i].setFont(dayList[i].getFont().deriveFont(Font.BOLD).deriveFont(10));
 			}
 			dayList[i].addActionListener(this);
 			calendarPanel.addComponent(dayList[i]);
 		}
 		
 		int i = 7;
 		while (calendarPanel.moreEmptyCells()) {
 			calendarPanel.addComponent(empty[i]);
 			i ++;
 		}
 		
 		if (! (today.get(Calendar.YEAR) - this.yearsBehind == year && month == 0)) {
 			leftArrow = new JButton("");
 			leftArrow.setBounds(5, 5, 30, 30);
 			leftArrow.setFont(leftArrow.getFont().deriveFont(16f));
 			leftArrow.addActionListener(this);
 			calendarDialog.add(leftArrow);
 		}
 		
 		if (! (today.get(Calendar.YEAR) + this.yearsAhead == year && month == 11)) {
 			rightArrow = new JButton("");
 			rightArrow.setBounds(217, 5, 30, 30);
 			rightArrow.setFont(rightArrow.getFont().deriveFont(16f));
 			rightArrow.addActionListener(this);
 			calendarDialog.add(rightArrow);
 		}
 		
 		calendarTitle = new JLabel(getMonthForInt(month) + " " + Integer.toString(year), JLabel.CENTER);
 		calendarTitle.setBounds(0, 5, 252, 30);
 		calendarDialog.add(calendarTitle);
 		
 		closeCalendarButton = new JButton("Cancel");
 		closeCalendarButton.setBounds(10, 235, 100, 30);
 		closeCalendarButton.addActionListener(this);
 		calendarDialog.add(closeCalendarButton);
 		
 		todayButton = new JButton("Today");
 		todayButton.setBounds(142, 235, 100, 30);
 		todayButton.addActionListener(this);
 		calendarDialog.add(todayButton);
 		
 		calendarDialog.add(calendarPanel);
 		calendarDialog.setVisible(true);
 	}
 	
 	private void updateField(int day, int month, int year) {
 		currentDate = new GregorianCalendar();
 		currentDate.set(year, month, day);
 		String dayString = "";
 		if (day > 9)
 			dayString = Integer.toString(day);
 		else
 			dayString = "0" + Integer.toString(day);
 		String monthString = "";
 		if (month > 9)
 			monthString = Integer.toString(month);
 		else
 			monthString = "0" + Integer.toString(month);
 		textField.setText(dayString + "/" + monthString + "/" + Integer.toString(year));
 		calendarDialog.setVisible(false);
 		calendarDialog.dispose();
 	}
 	
 	private int getDaysInMonth(int month, int year) {
 		if (month == 0 || month == 2 || month == 4 || month == 6 || month == 7 || month == 9 || month == 11)
 			return 31;
 		else if (month != 1)
 			return 30;
 		else if (year % 4 == 0)
 			return 29;
 		else
 			return 28;
 	}
 	
 	private String getMonthForInt(int m) {
 	    String month = "---";
 	    DateFormatSymbols dfs = new DateFormatSymbols();
 	    String[] months = dfs.getMonths();
 	    if (m >= 0 && m <= 11 ) {
 	        month = months[m];
 	    }
 	    
 	    return month;
 	}
 
 	public void focusGained(FocusEvent event) {
 			if (event.getSource() == dayField && dayField.getText().equals("DD"))
 				dayField.setText("");
 			else if (event.getSource() == monthField && monthField.getText().equals("MM"))
 				monthField.setText("");
 			else if (event.getSource() == yearField && yearField.getText().equals("YYYY"))
 				yearField.setText("");
 	}
 
 	public void focusLost(FocusEvent event) {
 		if (viewType == MULTI_TEXTFIELD_VIEW) {
 			if (event.getSource() == dayField && dayField.getText().equals(""))
 				dayField.setText("DD");
 			else if (event.getSource() == monthField && monthField.getText().equals(""))
 				monthField.setText("MM");
 			else if (event.getSource() == yearField && yearField.getText().equals(""))
 				yearField.setText("YYYY");
 			
 			if (! dayField.getText().equals("DD")) {
 				if (event.getSource() == dayField && (dayField.getText().equals("0") || dayField.getText().equals("00")))
 					dayField.setText("01");
 				else if (event.getSource() == dayField && (Integer.parseInt(dayField.getText()) > 31))
 					dayField.setText("31");
 				else if (event.getSource() == dayField && dayField.getText().length() == 1)
 					dayField.setText("0" + dayField.getText());
 			}
 			
 			
 			if (! monthField.getText().equals("MM")) {
 				if (event.getSource() == monthField && (monthField.getText().equals("0") || monthField.getText().equals("00")))
 					monthField.setText("01");
 				else if (event.getSource() == monthField && (Integer.parseInt(monthField.getText()) > 12))
 					monthField.setText("12");
 				else if (event.getSource() == monthField && monthField.getText().length() == 1)
 					monthField.setText("0" + monthField.getText());
 			}
 			
 			
 			if (! yearField.getText().equals("YYYY")) {
 				if (event.getSource() == yearField) {
 					if (yearField.getText().length() == 1)
 						yearField.setText("200" + yearField.getText());
 					else if (yearField.getText().length() == 2)
 						yearField.setText("20" + yearField.getText());
 					else if (yearField.getText().length() == 3)
 						yearField.setText("1" + yearField.getText());
 				}
 			}
 		}
 	}
 
 	public void keyPressed(KeyEvent event) {
 		
 	}
 
 	public void keyReleased(KeyEvent event) {
 		
 	}
 
 	public void keyTyped(KeyEvent event) {
 		if (viewType == MULTI_TEXTFIELD_VIEW) {
 			char typed = event.getKeyChar();
 			if (! Character.isDigit(typed))
 				event.consume();
 			else if (event.getSource() != yearField) {
 				JTextField field = (JTextField) event.getSource();
 				String text = field.getText();
 				if (text.length() == 2 && (field.getSelectedText() == null || field.getSelectedText().length() != 2))
 					event.consume();
 			} else {
 				String text = yearField.getText();
 				if (text.length() == 4 && (yearField.getSelectedText() == null || yearField.getSelectedText().length() != 4))
 					event.consume();
 			}
 		}
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		if (event.getSource() == calendarButton) {
 			if (this.date != null)
 				updateCalendar(this.date.get(Calendar.MONTH), this.date.get(Calendar.YEAR));
 			else {
 				GregorianCalendar today = new GregorianCalendar();
 				updateCalendar(today.get(Calendar.MONTH), today.get(Calendar.YEAR));
 			}
 			calendarDialog.setLocation(calendarButton.getLocationOnScreen().x, calendarButton.getLocationOnScreen().y);
 		}
 		
 		else if (event.getSource() == closeCalendarButton) {
 			calendarDialog.setVisible(false);
 			calendarDialog.dispose();
 		}
 		
 		else if (event.getSource() == leftArrow) {
 			if (dialogMonth == 0) {
 				updateCalendar(11, dialogYear - 1);
 			} else {
 				updateCalendar(dialogMonth - 1, dialogYear);
 			}
 		}
 		
 		else if (event.getSource() == rightArrow) {
 			if (dialogMonth == 11) {
 				updateCalendar(0, dialogYear + 1);
 			} else {
 				updateCalendar(dialogMonth + 1, dialogYear);
 			}
 		}
 		
 		else if (event.getSource() == todayButton) {
 			GregorianCalendar today = new GregorianCalendar();
 			updateField(today.get(Calendar.DATE), today.get(Calendar.MONTH) + 1, today.get(Calendar.YEAR));
 		}
 		
 		else {
 			updateField(Integer.parseInt(((JButton) event.getSource()).getText()), dialogMonth + 1, dialogYear);
 		}
 			
 	}
 	
 }
