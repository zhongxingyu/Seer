 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class AllKeys {
 	
 	//main JPanel for the Main page
 	JPanel content = new JPanel();
 	
 	//constructor creates buttons, add action listener to the buttons
 	//sets font, size, and colors
 	//creates the left and right panels and adds all the content to each
 	public AllKeys() {
 		
 		//Create the label and buttons for major keys
 		JLabel major = new JLabel("  MAJOR");
 		major.setForeground(Color.RED);
 		major.setFont(new Font("Serif", Font.PLAIN, 25));
 		
 		JButton aButton = new JButton("A");
 		JButton asButton = new JButton("A#");
 		JButton bButton = new JButton("B");
 		JButton cButton = new JButton("C");	
 		JButton csButton = new JButton("C#");
 		JButton dButton = new JButton("D");
 		JButton dsButton = new JButton("D#");
 		JButton eButton = new JButton("E");
 		JButton fButton = new JButton("F");
 		JButton fsButton = new JButton("F#");
 		JButton gButton = new JButton("G");
 		JButton gsButton = new JButton("G#");
 				
 		//Create the label and buttons for minor keys
 		JLabel minor = new JLabel("  MINOR");
 		minor.setForeground(Color.RED);
 		minor.setFont(new Font("Serif", Font.PLAIN, 25));
 		
		JButton amButton = new JButton(" Am ");
 		JButton asmButton = new JButton("A#m");
		JButton bmButton = new JButton(" Bm ");
		JButton cmButton = new JButton(" Cm ");
 		JButton csmButton = new JButton("C#m");
		JButton dmButton = new JButton(" Dm ");
 		JButton dsmButton = new JButton("D#m");
		JButton emButton = new JButton("  Em ");
		JButton fmButton = new JButton("  Fm ");
		JButton fsmButton = new JButton(" F#m");
		JButton gmButton = new JButton(" Gm ");
 		JButton gsmButton = new JButton("G#m");
 				
 		//create the action listener
 		GoToKeyInfo listener = new GoToKeyInfo();
 				
 		//add the action listener to all the buttons
 		aButton.addActionListener(listener);
 		asButton.addActionListener(listener);
 		bButton.addActionListener(listener);
 		cButton.addActionListener(listener);
 		csButton.addActionListener(listener);
 		dButton.addActionListener(listener);
 		dsButton.addActionListener(listener);
 		eButton.addActionListener(listener);
 		fButton.addActionListener(listener);
 		fsButton.addActionListener(listener);
 		gButton.addActionListener(listener);
 		gsButton.addActionListener(listener);
 				
 		amButton.addActionListener(listener);
 		asmButton.addActionListener(listener);
 		bmButton.addActionListener(listener);
 		cmButton.addActionListener(listener);
 		csmButton.addActionListener(listener);
 		dmButton.addActionListener(listener);
 		dsmButton.addActionListener(listener);
 		emButton.addActionListener(listener);
 		fmButton.addActionListener(listener);
 		fsmButton.addActionListener(listener);
 		gmButton.addActionListener(listener);
 		gsmButton.addActionListener(listener);
 			
 		//set the font and size for all the buttons
 		aButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		asButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		bButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		cButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		csButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		dButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		dsButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		eButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		fButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		fsButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		gButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		gsButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		
 		amButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		asmButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		bmButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		cmButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		csmButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		dmButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		dsmButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		emButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		fmButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		fsmButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		gmButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		gsmButton.setFont(new Font("Serif", Font.PLAIN, 25));
 		
 		//left is JPanel for the major keys
 		JPanel left = new JPanel();
 		left.setBackground(Color.BLACK);
 		left.setPreferredSize(new Dimension(200, 250));
 				
 		//right JPanel is for the minor keys
 		JPanel right = new JPanel();
 		right.setBackground(Color.BLACK);
 		right.setPreferredSize(new Dimension(200, 250));
 				
 		//use a Border Layout for the main window display to have the parts arranged side by side
 		content.setLayout(new BorderLayout());
 				
 		//add the major key (left) panel to the West side of the border layout
 		content.add(left, BorderLayout.WEST);
 				
 		//add the minor key (right) panel to the East side of the border layout
 		content.add(right, BorderLayout.EAST);
 		
 		//left and right panels are set up with Box Layout to have the content displayed one on top of the other
 				
 		//add the title and buttons to the panel for major keys
 		left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
 		left.add(major);
 		left.add(aButton);
 		left.add(asButton);
 		left.add(bButton);
 		left.add(cButton);
 		left.add(csButton);
 		left.add(dButton);
 		left.add(dsButton);
 		left.add(eButton);
 		left.add(fButton);
 		left.add(fsButton);
 		left.add(gButton);
 		left.add(gsButton);
 
 		//add the title and buttons to the panel for minor keys
 		right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
 		right.add(minor);
 		right.add(amButton);
 		right.add(asmButton);
 		right.add(bmButton);
 		right.add(cmButton);
 		right.add(csmButton);
 		right.add(dmButton);
 		right.add(dsmButton);
 		right.add(emButton);
 		right.add(fmButton);
 		right.add(fsmButton);
 		right.add(gmButton);
 		right.add(gsmButton);
 	}
 }
