 package gui;
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 
 import org.joda.time.DateTime;
 
 public class AddEventDialog{
 
 	private JFrame _parent;
 	private JPanel _calendar;
 	private JPanel _calPane;
 	private JLabel _monthAndYear;
 	private JTextField _eventName;
 	private JButton _okButton;
 	private JButton _cancelButton;
 	private JDialog _dialog;
 	private CalendarGui _gui;
 	private DateTime _today;
 	private DateTime _firstOfMonth;
 	private ArrayList<DateTime> _selected = new ArrayList<DateTime>();
 	private JComboBox _startHour;
 	private JComboBox _endHour;
 
 
 	public AddEventDialog(CalendarGui gui){
 		_today = new DateTime();
 		_calendar = new JPanel();
 		_gui = gui;
 
 		_calPane = new JPanel();
 		_firstOfMonth = _today.minusDays(_today.getDayOfMonth()-1);
 		_dialog = new JDialog();
 		_parent = new JFrame();
 
 		_monthAndYear = new JLabel(_firstOfMonth.monthOfYear().getAsText() + " " + _firstOfMonth.getYear());
 
 		_calendar.setLayout(new GridLayout(0, 7, 0, 0));
 		buildCalendar();
 
 		JButton prev = new JButton("<");
 		prev.setActionCommand("<");
 		JButton next = new JButton(">");
 		next.setActionCommand(">");
 		NextPrevListener npl = new NextPrevListener();
 		prev.addActionListener(npl);
 		next.addActionListener(npl);
 
 		JPanel monthYearPane = new JPanel();
 		monthYearPane.add(_monthAndYear);
 
 		_calPane.add(prev, BorderLayout.EAST);
 		_calPane.add(_calendar, BorderLayout.CENTER);
 		_calPane.add(next, BorderLayout.WEST);
 		_calPane.revalidate();
 
 		_eventName = new JTextField();
 		_eventName.setText("New Event Name");
 		_eventName.addFocusListener(new myFocusListener());
 		JPanel namePanel = new JPanel();
 		namePanel.setLayout(new GridLayout (0,3));
 		namePanel.add(new JPanel());
 		namePanel.add(_eventName);
 
 		String[] strings = new String[25];
 		for (int i=0; i<=24; i++){
 			if (i==0 || i ==24){
 				strings[i] = "midnight";
 			}
 			else if (i ==12){
 				strings[i]="noon";
 			}
 			else {
 				strings[i] = new String (Integer.toString(i) + ":00");
 			}
 		}
 
 		_startHour = new JComboBox(strings);
 		_startHour.setSelectedIndex(10);
 
 		_endHour = new JComboBox(strings);
 		_endHour.setSelectedIndex(20);
 
 		JPanel times = new JPanel();
 		times.setLayout(new GridLayout(2,2));
 		times.add(new JLabel("From:"));
 		times.add(_startHour);
 		times.add(new JLabel("To:"));
 		times.add(_endHour);
 
 		_dialog = new JDialog(_parent, "Choose Dates");
 		_dialog.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 
 		_okButton = new JButton ("OK");
 		_okButton.setActionCommand("OK");
 		_cancelButton = new JButton ("Cancel");
 		_cancelButton.setActionCommand ("Cancel");
 		ExitListener el = new ExitListener();
 		_okButton.addActionListener(el);
 		_cancelButton.addActionListener(el);
 		JPanel submitPanel = new JPanel();
 		submitPanel.add(_okButton);
 		submitPanel.add(_cancelButton);
 
 		c.insets = new Insets(10, 0, 5, 0);
 
 		c.weighty = 0.0;
 		c.weightx = 1.0;	
 		c.gridx = 0;
 		c.gridy = 0;
 		_dialog.add(new JLabel("What is the name of the event you are planning?"), c);
 
 		c.weighty = 0.0;
 		c.weightx = 1.0;	
 		c.gridx = 0;
 		c.gridy = 1;
 		_dialog.add(namePanel, c);
 
 		c.weighty = 0.0;
 		c.weightx = 1.0;	
 		c.gridx = 0;
 		c.gridy = 2;
 		c.ipady = 0;
 		_dialog.add(new JLabel ("What dates might work?"), c);
 
 		c.weighty = 0.0;
 		c.weightx = 1.0;	
 		c.gridx = 0;
 		c.gridy = 3;
 		c.ipady = 0;
 		_dialog.add(monthYearPane, c);
 
 		c.weighty = 0.0;
 		c.weightx = 1.0;	
 		c.gridx = 0;
 		c.gridy = 4;
 		_dialog.add(_calPane, c);
 
 		c.weighty = 0.0;
 		c.weightx = 1.0;	
 		c.gridx = 0;
 		c.gridy = 5;
 		c.ipady = 0;
 		_dialog.add(new JLabel ("What Times?"), c);		
 
 		c.weighty = 0.0;
 		c.weightx = 1.0;
 		c.gridx = 0;
 		c.gridy = 6;
 		c.ipady=0;
 		_dialog.add(times, c);
 
 		c.weighty = 0.0;
 		c.weightx = 1.0;
 		c.gridx = 0;
 		c.gridy = 7;
 		_dialog.add(submitPanel, c);
 
 		_dialog.pack();
 		_dialog.setLocationRelativeTo(null);
 		_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		_dialog.setResizable(false);
 		_dialog.setVisible(true);
 	}
 
 	private void buildCalendar(){
 		_calendar.removeAll();
 		_monthAndYear.setText(_firstOfMonth.monthOfYear().getAsText() + " " + _firstOfMonth.getYear());
 
 		for (int i=1; i<_firstOfMonth.getDayOfWeek(); i++){
 			_calendar.add(new JPanel());
 		}
 
		System.out.println(_firstOfMonth.dayOfMonth().getMaximumValue());
 		int maxDay = _firstOfMonth.dayOfMonth().getMaximumValue();
 		for (int i = 0; i<maxDay; i++){
 
 			DateButton button = new DateButton(Integer.toString(_firstOfMonth.plusDays(i).getDayOfMonth()),
 					_firstOfMonth.plusDays(i));
 			button.addActionListener(new DateListener());
 			if (button.getDate().isBefore(_today)){
 				button.setEnabled(false);
 			}
 			for (DateTime date: _selected){
 				if (button.getDate().equals(date))
 					button.setSelected(true);
 			}
 			_calendar.add(button);
 		}
 
 		_dialog.invalidate();
 		_dialog.validate();
 		_dialog.pack();
 		_calendar.revalidate();
 	}
 
 	private class DateListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 
 			if (((DateButton) e.getSource()).isSelected()==true)
 				_selected.add(((DateButton) e.getSource()).getDate());
 			else {
 				_selected.remove(((DateButton) e.getSource()).getDate());
 			}

			for (DateTime t: _selected){
				System.out.println(t);
			}
			System.out.println("");
 		}
 
 	}
 
 	private class NextPrevListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getActionCommand().equals("<")){
 				_firstOfMonth = _firstOfMonth.minusMonths(1);
 				_firstOfMonth = _firstOfMonth.minusDays(_firstOfMonth.getDayOfMonth()-1);
 				buildCalendar();
 			}
 			else if (e.getActionCommand().equals(">")){
 				_firstOfMonth = _firstOfMonth.plusMonths(1);
 				_firstOfMonth = _firstOfMonth.plusDays(_firstOfMonth.getDayOfMonth()-1);
 				buildCalendar();
 			}
 		}
 	}	
 
 
 	private class myFocusListener implements FocusListener{
 
 		@Override
 		public void focusGained(FocusEvent e) {
 			if (((JTextField) e.getSource()).getText().equals("New Event Name")){
 				((JTextField) e.getSource()).setText("");
 			}
 		}
 
 		@Override
 		public void focusLost(FocusEvent e) {
 			if (((JTextField) e.getSource()).getText().equals("")){
 				((JTextField) e.getSource()).setText("New Event Name");
 			}
 		}
 	}	
 
 
 	private class ExitListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getActionCommand().equals("OK")){
 				_dialog.dispose();
				_gui.CreateEvent(_selected, _startHour.getSelectedIndex(), _endHour.getSelectedIndex());
 			}
 			else if (e.getActionCommand().equals("Cancel")){
 				_dialog.dispose();
 			}
 		}
 
 	}
 
 
 	private class DateButton extends JToggleButton{
 
 		private DateTime _date;
 
 		public DateButton (String s, DateTime date){
 			super(s);
 			_date = date;
 		}
 
 		public DateTime getDate(){
 			return _date;
 		}
 	}
 
 
 }
