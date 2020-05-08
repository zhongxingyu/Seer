 /**
  * Creates the headings in the small calendar.
  */
 
 package gui.main;
 
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
import data.Date;
 
 
 public class TinyMonthHeadingPanel extends JPanel {
 	
 	final DatePicker dp;
 	
 	public TinyMonthHeadingPanel(String month, String year, boolean current, DatePicker dp) {
 		this.dp = dp;
 		//setPreferredSize(new Dimension(200, 0));
 		setLayout(new BorderLayout());
 		JPanel monthHeading = new JPanel(new BorderLayout());
 		JLabel label = new JLabel(month + " " + year, JLabel.CENTER);
 		label.setFont(new Font("Tahoma",Font.BOLD,16));
 		monthHeading.add(label, BorderLayout.CENTER);
 /*		
 		if (current) {
 			JButton prevButton = new JButton("<");
 			prevButton.setMargin(new java.awt.Insets(0,5,0,5));
 			prevButton.setAction(prevMonthAction);
 			monthHeading.add(prevButton, BorderLayout.WEST);
 			JButton nextButton = new JButton(">");
 			nextButton.setMargin(new java.awt.Insets(0,5,0,5));
 			nextButton.setAction(nextMonthAction);
 			monthHeading.add(nextButton, BorderLayout.EAST);
 		}
 	*/	
 		monthHeading.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
 		monthHeading.setBackground(new Color(215,215,255));
 		add(monthHeading);
 		
 		//-------------------------------
 		JPanel buttonPanel = new JPanel(new GridLayout(1,0));
 		//JLabel label1 = new JLabel("  ", JLabel.CENTER);
 		//JLabel label2 = new JLabel("  ", JLabel.CENTER);
 		//text.setFont(new Font("Tahoma",Font.PLAIN,11));
 		
 		if (current) {
 			JButton prevButton = new JButton("<");
 			prevButton.setMargin(new java.awt.Insets(0,5,0,5));
 			prevButton.setAction(prevMonthAction);
 			buttonPanel.add(prevButton, BorderLayout.WEST);
 			
 			JButton todayButton = new JButton("  TODAY  ");
 			todayButton.setMargin(new java.awt.Insets(0,5,0,5));
 			todayButton.setAction(todayAction);
 			//buttonPanel.add(label1, BorderLayout.CENTER);
 			buttonPanel.add(todayButton, BorderLayout.CENTER);
 			//buttonPanel.add(label2, BorderLayout.CENTER);
 			
 			JButton nextButton = new JButton(">");
 			nextButton.setMargin(new java.awt.Insets(0,5,0,5));
 			nextButton.setAction(nextMonthAction);
 			buttonPanel.add(nextButton, BorderLayout.EAST);
 		}
 		
 		
 		add(buttonPanel, BorderLayout.CENTER);
 		//-------------------------------
 		JPanel dayHeading = new JPanel(new GridLayout(1,0));
 		for (int i = 0; i < 7; i++) {
 			String day;
 			if (i==0) day = "Sun";
 			else if (i==1) day = "Mon";
 			else if (i==2) day = "Tue";
 			else if (i==3) day = "Wed";
 			else if (i==4) day = "Thu";
 			else if (i==5) day = "Fri";
 			else day = "Sat";
 			
 			label = new JLabel(" " + day + " ");
 			//text.setFont(new Font("Tahoma",Font.PLAIN,11));
 			
 			JPanel panel = new JPanel(new BorderLayout());
 			panel.add(label, BorderLayout.CENTER);
 			panel.setBackground(new Color(255,215,215));
 			panel.setBorder(BorderFactory.createMatteBorder(0,0,1,0,Color.BLACK));
 			dayHeading.add(panel);
 			
 		}		
 		
 		add(monthHeading, BorderLayout.NORTH);
 		add(dayHeading, BorderLayout.SOUTH);
 			
 	}
 	
 	private final AbstractAction nextMonthAction = new AbstractAction(">") {
 		public void actionPerformed(ActionEvent e) {
 			dp.nextMonth();
 		}
 	};
 	
 	private final AbstractAction todayAction = new AbstractAction("  Today  ") {
 		public void actionPerformed(ActionEvent e) {
 			GregorianCalendar cal = new GregorianCalendar();
 			cal.setTimeInMillis(System.currentTimeMillis());
			Date currentDate = new Date(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.YEAR));
			
 			dp.setDate(currentDate);
 		}
 	};
 	
 	private final AbstractAction prevMonthAction = new AbstractAction("<") {
 		public void actionPerformed(ActionEvent e) {
 			dp.prevMonth();
 		}
 	};
 }
