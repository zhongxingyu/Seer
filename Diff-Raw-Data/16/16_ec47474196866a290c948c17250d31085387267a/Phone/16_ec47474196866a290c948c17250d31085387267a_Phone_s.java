 package phone;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 
 @SuppressWarnings("serial")
 public class Phone extends JFrame{
 
 	String[] optionKeys = {"Menu", "Back", "Messages", "C"};
 	
 	String[] numbKeys = {"1", "2 ABC", "3 DEF", "4 GHI", "5 JKL", 
 			"6 MNO", "7 PQRS", "8 TUV", "9 WXYZ", "*   a/A", "0   +", "#"}; 
 	
 	JButton up = new JButton("^"); 
 	JButton down = new JButton("v");
 	JButton left = new JButton("<");
 	JButton right = new JButton(">");
 	JButton select = new JButton("O");
	Menu currentMenu = new Menu(Color.GRAY, Color.WHITE);
	Menu newMenu = new Menu(Color.PINK, Color.GREEN);
 	String brand = "Sony Ericsson"; 
 	
 	public Phone() {
 		super("Phone"); 
 		
 		JLabel lblBrand = new JLabel(this.brand);
 		lblBrand.setHorizontalAlignment(SwingConstants.CENTER); 
		this.add(lblBrand, BorderLayout.NORTH); 
 		
 		JTextArea taScreen = new JTextArea(); 
 		taScreen.setEditable(false); 
		this.add(currentMenu, BorderLayout.CENTER); 
 		
 		this.add(new Buttons(), BorderLayout.SOUTH); 
 		
 		this.setSize(320, 570); 
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE); 
 		this.setLocationRelativeTo(null); 
 		this.setResizable(false); 
 		this.setVisible(true); 
 
 	}
 	
 	public class Buttons extends JPanel {
 		
 		public Buttons() {
 			super(new BorderLayout()); 
 
 			EventHandler handler = new EventHandler();
 			//Option keys 
 			JPanel pnlOptionKeys = new JPanel(new BorderLayout()); 
 			
 			Dimension dimension = new Dimension(90, 50); 
 
 			JPanel pnlLeftKeys = new JPanel(new GridLayout(2, 1)); 
 			
 			for (int i = 0; i < 2; i++) {
 				JButton btn = new JButton(optionKeys[i]);
 				btn.addActionListener(handler);
 				pnlLeftKeys.add(btn); 
 			}
 						
 			pnlLeftKeys.setPreferredSize(dimension); 
 			
 			JPanel pnlRightKeys = new JPanel(new GridLayout(2,1)); 
 			
 			for (int i = 0; i < 2; i++) {
 				JButton btn = new JButton(optionKeys[i + 2]);
 				btn.addActionListener(handler);
 				pnlRightKeys.add(btn); 
 			}
 			
 			pnlRightKeys.setPreferredSize(dimension); 
 			
 			JPanel pnlArrowKeys = new JPanel(); 
 			pnlArrowKeys.setLayout(new BorderLayout());
 						
 			Dimension arrowDimension = new Dimension(25, 20); 
 			
 			//select.setPreferredSize(new Dimension(40, 40)); 
 			
 			up.setPreferredSize(arrowDimension); 
 			down.setPreferredSize(arrowDimension); 
 			left.setPreferredSize(arrowDimension); 
 			right.setPreferredSize(arrowDimension); 
 			
 			up.addActionListener(handler);
 			down.addActionListener(handler);
 			left.addActionListener(handler);
 			right.addActionListener(handler);
 			select.addActionListener(handler);
 			
 			pnlArrowKeys.add(up, BorderLayout.NORTH); 
 			pnlArrowKeys.add(down, BorderLayout.SOUTH); 
 			pnlArrowKeys.add(left, BorderLayout.WEST); 
 			pnlArrowKeys.add(right, BorderLayout.EAST); 
 			pnlArrowKeys.add(select, BorderLayout.CENTER);
 			
 			pnlOptionKeys.add(pnlLeftKeys, BorderLayout.WEST); 
 			pnlOptionKeys.add(pnlArrowKeys, BorderLayout.CENTER);
 			pnlOptionKeys.add(pnlRightKeys, BorderLayout.EAST); 
 			
 			this.add(pnlOptionKeys, BorderLayout.NORTH); 
 			
 			//Number keys 
 			
 			JPanel pnlNumPad = new JPanel(new GridLayout(4, 3)); 
 			
 			for(String currentLabel : numbKeys) {
 				JButton btn = new JButton(currentLabel);
 				btn.addActionListener(handler);
 				pnlNumPad.add(btn); 
 			}
 			
 			this.add(pnlNumPad, BorderLayout.CENTER); 
 		}
 	}
 	
 	
 	public class EventHandler implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			
 			//Actions when buttons are pressed
 			String data = e.getActionCommand();
			if(data.equals("^")) currentMenu.buttonUp();
			if(data.equals("v")) currentMenu.buttonDown();
 		}
 	}
 	
 	public static void main(String[] args) {
 		new Phone(); 
 	}
 }
