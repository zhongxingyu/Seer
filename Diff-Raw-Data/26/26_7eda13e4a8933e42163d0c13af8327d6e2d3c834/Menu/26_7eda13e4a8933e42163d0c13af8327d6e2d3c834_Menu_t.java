 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 
 
 public class Menu implements ActionListener {
 
 	public Menu() {
 		start.add(startl);
 		start.setSize(50, 30);
 		start.addActionListener(this);
 
 		JButton options = new JButton();
 		options.add(optionsl);
 		options.setSize(50, 30);
 
 		exit.add(exitl);
 		exit.setSize(50, 30);
 		exit.addActionListener(this);
 
 		JPanel panel = new JPanel();
 
 		panel.add(label);
 		panel.add(start);
		//panel.add(options);
 		panel.add(exit);
 
 		frame.setSize(550, 550);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.add(panel);
 		frame.setVisible(true);
 	}
 
 	JButton start = new JButton();
 	JButton exit = new JButton();
 
 	JFrame frame = new JFrame("BOMBERWOMAN | Menu");
 
 	JLabel label = new JLabel("Bomberwoman Version 1.00");
 	JLabel startl = new JLabel("Start");
 	JLabel optionsl = new JLabel("Options");
 	JLabel exitl = new JLabel("Exit");
 	
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == start) new Spielfeld();
 		if (e.getSource() == exit) frame.dispose();
 	}
 }
