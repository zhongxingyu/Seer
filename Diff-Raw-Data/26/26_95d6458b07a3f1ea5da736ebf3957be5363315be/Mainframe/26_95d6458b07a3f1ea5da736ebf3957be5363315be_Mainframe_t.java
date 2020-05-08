 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 
 import calendar.Days;
 import calendar.Months;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 public class Mainframe extends JFrame { 
 	
 	private JButton jbtUpdateDay;
 	private JButton jbtUpdateMonth;
 	private JButton jbtNext;
 	private JTextArea jtaDays;
 	private JTextArea jtaMonths;
 	
 	private static final long serialVersionUID = 7746814428039297989L;
 
 	public Mainframe() {
 	JPanel p1 = new JPanel();
 	JPanel p2 = new JPanel();
 	JPanel p3 = new JPanel();
 	
	jbtUpdateDay = new JButton("Change day");
	jbtUpdateMonth = new JButton("Change month");	
	jbtNext = new JButton("Next day");
	
	jtaDays = new JTextArea(1, 1);
 	jtaDays.setEditable(false);
 	jtaDays.setFont(new Font("Verdana", Font.BOLD, 50));
 	jtaDays.setText(Days.days.digitToString());
	
	jtaMonths = new JTextArea(1, 1);
 	jtaMonths.setEditable(false);
 	jtaMonths.setFont(new Font("Verdana", Font.BOLD, 50));
	jtaMonths.setText(Months.months.digitToString());
 	
 	jbtNext.addActionListener(new NextListener());
 	
 	p1.add(jbtNext);
 	add(p1, BorderLayout.SOUTH);
 	
 	p2.add(jbtUpdateDay);
 	p2.add(jbtUpdateMonth);
 	add(p2);
 	
 	p3.add(jtaDays);
 	p3.add(jtaMonths);
 	add(p3, BorderLayout.NORTH);
 	
 
 	this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 	
 	
 	
 	}
 	
 	class NextListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			Days.days.Increase();
 			System.out.println(jtaDays.getText());
 		}
 		
 	}
 	
	class UpdateMonthsListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if ()
		}
		
	}
	
 	
 	public static void main(String[] args) {
 		JFrame frame = new Mainframe();
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 }
