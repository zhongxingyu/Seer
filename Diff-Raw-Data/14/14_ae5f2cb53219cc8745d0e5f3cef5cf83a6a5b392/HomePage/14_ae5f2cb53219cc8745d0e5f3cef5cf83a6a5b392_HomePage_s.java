 package com.UI;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;

 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 
 public class HomePage extends JPanel{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -386551712892064216L;
 
 	
 	//Singleton Pattern
 	private static HomePage instance;
 	private JCheckBox[] checkboxes;
 	private boolean player2;
 	private int numOfBoxes;
 	JComboBox<Object> gridList;
 	
 	//Private Constructor
 	private HomePage(){
 		super();
 		this.setLayout(new GridBagLayout());
 		checkboxes = new JCheckBox[2];
 		addImage();
 		addCheckBoxes();
 		addText();
 		addDropDown();		
 		addButton();
 	}
 	
 	
 	public static HomePage getInstance(){
 		if(instance == null){
 			instance = new HomePage();
 		}
 		return instance;
 	}
 	
 	private void addImage(){
<<<<<<< HEAD
 		JLabel image = new JLabel(new ImageIcon("board.png"));
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		this.add(image, c);
		
=======
 		URL resource = getClass().getResource("/board.png");
 		System.out.println(resource);
 		JLabel image = new JLabel(new ImageIcon(resource));
 		this.add(image);
>>>>>>> refs/remotes/origin/master
 	}
 	
 	private void addCheckBoxes(){
 		Dimension d = new Dimension(200,40);
 		String[] texts = {"Player Vs Player", "Player Vs AI"};
 		ButtonGroup bg1 = new ButtonGroup( );
 		GridBagConstraints c = new GridBagConstraints();
 		int i = 0;
 		int x = 1;
 		for(String text : texts){
 			checkboxes[i] = new JCheckBox(text);
 			checkboxes[i].setSize(d);
 			checkboxes[i].setMinimumSize(d);
 			checkboxes[i].setMaximumSize(d);
 			checkboxes[i].setPreferredSize(d);
 			c.gridx = 0;
 			c.gridy = x;
 			this.add(checkboxes[i], c);
 			bg1.add(checkboxes[i]);
 			x++;
 			i++;	
 		}
 	}
 	
 	private void addText(){
 		Dimension d = new Dimension(200,40);
 		JLabel textField = new JLabel("Select the grid for the board: "); 
 		textField.setSize(d);
 		textField.setMinimumSize(d);
 		textField.setMaximumSize(d);
 		textField.setPreferredSize(d);
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 3;
 		this.add(textField, c);
 	}
 	
 	private void addDropDown(){
 		Dimension d = new Dimension(200,40);
 		String[] Grid = {"","2x2", "3x3" , "4x4", "5x5", "6x6", "7x7", "8x8", "9x9","10x10"};
 		gridList = new JComboBox<Object>(Grid);
 		gridList.setSelectedIndex(0);
 		gridList.setVisible(true);
 		gridList.setSize(d);
 		gridList.setMinimumSize(new Dimension(150,20));
 		gridList.setMaximumSize(d);
 		gridList.setPreferredSize(d);
 		
 		//  Center algin the content of the comboBox 
 		DefaultListCellRenderer dlcr = new DefaultListCellRenderer(); 
 		dlcr.setHorizontalAlignment(DefaultListCellRenderer.CENTER); 
 		gridList.setRenderer(dlcr); 
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 4;
 		this.add(gridList, c);		
 	}
 	
 	private void addButton(){
 		Dimension d = new Dimension(200,40);
 		JButton start = new JButton("START");
 		start.setSize(d);
 		start.setMinimumSize(d);
 		start.setMaximumSize(d);
 		start.setPreferredSize(d);
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 5;
 		this.add(start, c);
 				
 		class ButtonListener implements ActionListener{
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (e.getActionCommand().equals ("START")){
 					player2 = checkboxes[0].isSelected();
 					String num = ((String)gridList.getSelectedItem()).split("x")[0];
 					numOfBoxes = Integer.parseInt(num);
 					
 					Runner.getGame(numOfBoxes, player2);
 					
 				}
 			}
 			
 		}
 		start.addActionListener(new ButtonListener());
 	}
 	
 }
 
