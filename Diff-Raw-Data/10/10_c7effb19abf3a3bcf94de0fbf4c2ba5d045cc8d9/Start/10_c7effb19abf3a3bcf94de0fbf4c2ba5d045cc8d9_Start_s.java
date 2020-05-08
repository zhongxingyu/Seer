 package Start;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.AbstractButton;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class Start extends JPanel implements ActionListener{
 
 	public Start()
 	{
 		JButton b1 = new JButton("Birth - 1997");
 		b1.setVerticalTextPosition(AbstractButton.CENTER);
 		b1.setActionCommand("1");
 		b1.setEnabled(true);
 		
 		JButton b2 = new JButton("First Birthday - 1998");
 		b2.setVerticalTextPosition(AbstractButton.CENTER);
 		b2.setActionCommand("2");
 		b2.setEnabled(true);
 		
 		JButton b3 = new JButton("Internship at CHS - 2007");
 		b3.setVerticalTextPosition(AbstractButton.CENTER);
 		b3.setActionCommand("3");
 		b3.setEnabled(true);
 		
 		JButton b4 = new JButton("First Year At SMS (and last) - 2009");
 		b4.setVerticalTextPosition(AbstractButton.CENTER);
 		b4.setActionCommand("4");
 		b4.setEnabled(true);
 		
 		JButton b5 = new JButton("Started Programming - 2009");
 		b5.setVerticalTextPosition(AbstractButton.CENTER);
 		b5.setActionCommand("5");
 		b5.setEnabled(true);
 		
 		JButton b6 = new JButton("Started attending online school - 2010");
 		b6.setVerticalTextPosition(AbstractButton.CENTER);
 		b6.setActionCommand("6");
 		b6.setEnabled(true);
 		
 		JButton b7 = new JButton("Beta tested Chromebook for Google - 2011");
 		b7.setVerticalTextPosition(AbstractButton.CENTER);
 		b7.setActionCommand("7");
 		b7.setEnabled(true);
 		
 		JButton b8 = new JButton("Peleased First Application - 2011");
 		b8.setVerticalTextPosition(AbstractButton.CENTER);
 		b8.setActionCommand("8");
 		b8.setEnabled(true);
 		
 		JButton b9 = new JButton("First year of CHS - 2012");
 		b9.setVerticalTextPosition(AbstractButton.CENTER);
 		b9.setActionCommand("9");
 		b9.setEnabled(true);
 		
 		JButton b10 = new JButton("Grandfather died - 2013");
 		b10.setVerticalTextPosition(AbstractButton.CENTER);
 		b10.setActionCommand("10");
 		b10.setEnabled(true);
 		
 		JButton b11 = new JButton("Started making user programs - 2013");
 		b11.setVerticalTextPosition(AbstractButton.CENTER);
 		b11.setActionCommand("11");
 		b11.setEnabled(true);
 		
 		JButton b12 = new JButton("Graduation - 2016");
 		b12.setVerticalTextPosition(AbstractButton.CENTER);
 		b12.setActionCommand("12");
 		b12.setEnabled(true);
 		
 		b1.addActionListener(this);
 		b2.addActionListener(this);
 		b3.addActionListener(this);
 		b4.addActionListener(this);
 		b5.addActionListener(this);
 		b6.addActionListener(this);
 		b7.addActionListener(this);
 		b8.addActionListener(this);
 		b9.addActionListener(this);
 		b10.addActionListener(this);
 		b11.addActionListener(this);
 		b12.addActionListener(this);
 		
 		add(b1);
 		add(b2);
 		add(b3);
 		add(b4);
 		add(b5);
 		add(b6);
 		add(b7);
 		add(b8);
 		add(b9);
 		add(b10);
 		add(b11);
 		add(b12);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		int i = e.getActionCommand()e.
 	}
 
 	public static JFrame MakeGUI()
 	{
 		System.out.println("Making GUI");
 		JFrame mainFrame = new JFrame("William's Timeline");
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Start buttons = new Start();
 		buttons.setOpaque(true);
 		buttons.setPreferredSize(new Dimension(440,220));
 		mainFrame.setContentPane(buttons);
 		mainFrame.pack();
 		return mainFrame;
 	}
 	
	public static JFrame MakeDisplayGUI()
 	{
 		System.out.println("Making GUI");
 		JFrame mainFrame = new JFrame("string");
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JLabel emptyLabel = new JLabel("");
 		emptyLabel.setPreferredSize(new Dimension(440,220));
 		mainFrame.getContentPane().add(emptyLabel, BorderLayout.CENTER);
 		mainFrame.pack();
 		return mainFrame;
 	}
 	
 	public static String[] getLayout(int id)
 	{
 		switch (id)
 		{
		case 1: return new String[] {"Birth","1/29/1997","I was born on this day </ br> I was born by cesarean section in Kissimmee Florida"}; 
		case 2: return new String[] {"First Birthday","1/29/1998","This was my first birthday </ br> apperently i ate the cake very carefully"}; 
		case 3: return null;
 		}
 		return null;
 	}
 	
 	public static void main(String[] args)
 	{
 		System.out.println("Starting");
 		JFrame Screen = MakeGUI();
 		Screen.setVisible(true);
 	}
 	
 }
