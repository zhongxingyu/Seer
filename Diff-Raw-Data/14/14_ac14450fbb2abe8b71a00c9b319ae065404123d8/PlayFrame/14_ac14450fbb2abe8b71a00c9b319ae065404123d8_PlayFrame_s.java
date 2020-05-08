 package gui;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.LinkedList;
 
 import javax.swing.*;
 
 import reachForTheTop.Player;
 import reachForTheTop.PlayerIO;
 
 /**
  * @author Aly
  *
  */
 public class PlayFrame extends DefaultFrame implements ActionListener{
 	
 	private AlyButton scoreButtonRight[][],scoreButtonLeft[][], plusButtonRight[], plusButtonLeft[];
 	private GridLayout mainLayout = new GridLayout(6,1);
 	private JLabel playerNamesR[], playerNamesL[];
 	private JPanel rightSuperSubPanel[], leftSuperSubPanel[];
 	
 	public PlayFrame(){
 		super("Game Time!");
 		setLayout(new GridLayout(1,3));
 		createComponents();
 		pack();
 		repaint();	
 		
 	}
 
 	private void createComponents() {
 		
 		createLeftPanel();
 		
 		JPanel centerPanel = new JPanel();
 		centerPanel.setBackground(ColorScheme.DEFAULT_MAIN);
 		add(centerPanel);
 		
 		/*
 		 * 
 		 * LOOK AWAY! PLEASE!
 		 * THIS WAS HAO'S IDEA!
 		 * 
 		 */
 		createRightPanel();
 		
 	}
 	
 	private void createRightPanel() {
 		JPanel rightMainPanel = new JPanel();
 		rightMainPanel.setLayout(mainLayout);
 		add(rightMainPanel);
 		
 		JPanel rightSubPanel[] = new JPanel[mainLayout.getRows()];
 		SpringLayout rightSubLayout[] = new SpringLayout[mainLayout.getRows()];
 		for(int i=0;i<rightSubPanel.length;i++){
 			rightSubPanel[i] = new JPanel();
 			rightSubLayout[i] = new SpringLayout();
 			
 			rightSubPanel[i].setBackground(ColorScheme.DEFAULT_MAIN);			
 			rightSubPanel[i].setLayout(rightSubLayout[i]);
 			rightMainPanel.add(rightSubPanel[i]);
 		}
 		
 		JLabel teamTwoLabel = new JLabel("TEAM TWO!");
 		teamTwoLabel.setBackground(ColorScheme.DEFAULT_SECONDARY);
 		teamTwoLabel.setForeground(ColorScheme.DEFAULT_MAIN);
 		teamTwoLabel.setFont(new Font("Mangal",Font.BOLD,24));
 		teamTwoLabel.setHorizontalAlignment(JLabel.CENTER);
 		teamTwoLabel.setVerticalAlignment(JLabel.CENTER);
 		teamTwoLabel.setOpaque(true);
 		
 		rightSubLayout[0].putConstraint(SpringLayout.WEST, teamTwoLabel,
 				15, SpringLayout.WEST,rightSubPanel[0]);
 		rightSubLayout[0].putConstraint(SpringLayout.NORTH, teamTwoLabel,
 				0, SpringLayout.NORTH,rightSubPanel[0]);
 		rightSubLayout[0].putConstraint(SpringLayout.EAST, teamTwoLabel,
 				-15, SpringLayout.EAST,rightSubPanel[0]);
 		rightSubLayout[0].putConstraint(SpringLayout.SOUTH, teamTwoLabel,
 				-10, SpringLayout.SOUTH,rightSubPanel[0]);
 	
 		rightSubPanel[0].add(teamTwoLabel);
 		
 		scoreButtonRight = new AlyButton[4][4];
 		playerNamesL = new JLabel[4];
 		rightSuperSubPanel = new JPanel[4];		
 		
 		for(int i=0;i<scoreButtonRight.length;i++){
 						
 			playerNamesL[i] = new JLabel("   Player");
 			playerNamesL[i].setBackground(ColorScheme.DEFAULT_SECONDARY);
 			playerNamesL[i].setForeground(ColorScheme.DEFAULT_MAIN);
 			playerNamesL[i].setOpaque(true);
 			playerNamesL[i].setVisible(false);
 			playerNamesL[i].setVerticalAlignment(JLabel.TOP);
 			playerNamesL[i].setFont(new Font("Mangal",Font.PLAIN,16));
 			
 			rightSubLayout[i+1].putConstraint(SpringLayout.NORTH, playerNamesL[i],
 					0, SpringLayout.NORTH,rightSubPanel[i+1]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.WEST, playerNamesL[i],
 					15, SpringLayout.WEST,rightSubPanel[i+1]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.EAST, playerNamesL[i],
 					-15, SpringLayout.EAST,rightSubPanel[i+1]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.SOUTH, playerNamesL[i],
 					0, SpringLayout.SOUTH,rightSubPanel[i+1]);
 						
 			
 			rightSuperSubPanel[i] = new JPanel();
 			GridLayout g = new GridLayout(1,4);
 			g.setHgap(5);
 			rightSuperSubPanel[i].setLayout(g);
 			rightSuperSubPanel[i].setVisible(false);
 			rightSuperSubPanel[i].setBackground(ColorScheme.DEFAULT_SECONDARY);						
 			
 			rightSubLayout[i+1].putConstraint(SpringLayout.NORTH, rightSuperSubPanel[i],
 				30, SpringLayout.NORTH,rightSubPanel[i+1]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.SOUTH, rightSuperSubPanel[i],
 					-15, SpringLayout.SOUTH,rightSubPanel[i+1]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.WEST, rightSuperSubPanel[i],
 					10, SpringLayout.WEST,playerNamesL[i]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.EAST, rightSuperSubPanel[i],
 					-10, SpringLayout.EAST,playerNamesL[i]);
 			
 			rightSubPanel[i+1].add(rightSuperSubPanel[i]);
 			rightSubPanel[i+1].add(playerNamesL[i]);
 		}
 
 		
 		for(int i=0;i<scoreButtonRight.length;i++)
 			for(int j=0;j<scoreButtonRight[0].length;j++){
 				scoreButtonRight[i][j] = new AlyButton((j+1)*10+"");
 				scoreButtonRight[i][j].addActionListener(this);
 				scoreButtonRight[i][j].setActionCommand("right");
 				rightSuperSubPanel[i].add(scoreButtonRight[i][j]);
 			}			
 		
 		//add the "+" buttons
 		plusButtonRight = new AlyButton[4];
 		for(int i=0;i<plusButtonRight.length;i++){
 			 plusButtonRight[i] = new AlyButton("+");
 			 plusButtonRight[i].flipColors();
 			 plusButtonRight[i].addActionListener(this);
 			 plusButtonRight[i].setFont(new Font("Mangal",Font.PLAIN,40));
 			 
 			 rightSubLayout[i+1].putConstraint(SpringLayout.NORTH, plusButtonRight[i],
 					0, SpringLayout.NORTH,rightSubPanel[i+1]);
 			 rightSubLayout[i+1].putConstraint(SpringLayout.WEST, plusButtonRight[i],
 					15, SpringLayout.WEST,rightSubPanel[i+1]);
 			 rightSubLayout[i+1].putConstraint(SpringLayout.EAST, plusButtonRight[i],
 					-15, SpringLayout.EAST,rightSubPanel[i+1]);
 			 rightSubLayout[i+1].putConstraint(SpringLayout.SOUTH,plusButtonRight[i],
 					0, SpringLayout.SOUTH,rightSubPanel[i+1]);
 			 
 			 rightSubPanel[i+1].add(plusButtonRight[i]);
 		}
 	}
 
 	private void createLeftPanel(){
 		JPanel leftMainPanel = new JPanel();
 		leftMainPanel.setLayout(mainLayout);
 		add(leftMainPanel);
 		
 		JPanel leftSubPanel[] = new JPanel[mainLayout.getRows()];
 		SpringLayout leftSubLayout[] = new SpringLayout[mainLayout.getRows()];
 		for(int i=0;i<leftSubPanel.length;i++){
 			leftSubPanel[i] = new JPanel();
 			leftSubLayout[i] = new SpringLayout();
 			
 			leftSubPanel[i].setBackground(ColorScheme.DEFAULT_MAIN);			
 			leftSubPanel[i].setLayout(leftSubLayout[i]);
 			leftMainPanel.add(leftSubPanel[i]);
 		}
 		
 		JLabel teamOneLabel = new JLabel("TEAM ONE!");
 		teamOneLabel.setBackground(ColorScheme.DEFAULT_SECONDARY);
 		teamOneLabel.setForeground(ColorScheme.DEFAULT_MAIN);
 		teamOneLabel.setFont(new Font("Mangal",Font.BOLD,24));
 		teamOneLabel.setHorizontalAlignment(JLabel.CENTER);
 		teamOneLabel.setVerticalAlignment(JLabel.CENTER);
 		teamOneLabel.setOpaque(true);
 		
 		leftSubLayout[0].putConstraint(SpringLayout.WEST, teamOneLabel,
 				15, SpringLayout.WEST,leftSubPanel[0]);
 		leftSubLayout[0].putConstraint(SpringLayout.NORTH, teamOneLabel,
 				0, SpringLayout.NORTH,leftSubPanel[0]);
 		leftSubLayout[0].putConstraint(SpringLayout.EAST, teamOneLabel,
 				-15, SpringLayout.EAST,leftSubPanel[0]);
 		leftSubLayout[0].putConstraint(SpringLayout.SOUTH, teamOneLabel,
 				-10, SpringLayout.SOUTH,leftSubPanel[0]);
 	
 		leftSubPanel[0].add(teamOneLabel);
 		
 		scoreButtonLeft = new AlyButton[4][4];
 		playerNamesR = new JLabel[4];
 		leftSuperSubPanel = new JPanel[4];		
 		
 		for(int i=0;i<scoreButtonLeft.length;i++){
 						
 			playerNamesR[i] = new JLabel("   Player");
 			playerNamesR[i].setBackground(ColorScheme.DEFAULT_SECONDARY);
 			playerNamesR[i].setForeground(ColorScheme.DEFAULT_MAIN);
 			playerNamesR[i].setOpaque(true);
 			playerNamesR[i].setVisible(false);
 			playerNamesR[i].setVerticalAlignment(JLabel.TOP);
 			playerNamesR[i].setFont(new Font("Mangal",Font.PLAIN,16));
 			
 			leftSubLayout[i+1].putConstraint(SpringLayout.NORTH, playerNamesR[i],
 					0, SpringLayout.NORTH,leftSubPanel[i+1]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.WEST, playerNamesR[i],
 					15, SpringLayout.WEST,leftSubPanel[i+1]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.EAST, playerNamesR[i],
 					-15, SpringLayout.EAST,leftSubPanel[i+1]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.SOUTH, playerNamesR[i],
 					0, SpringLayout.SOUTH,leftSubPanel[i+1]);
 						
 			
 			leftSuperSubPanel[i] = new JPanel();
 			GridLayout g = new GridLayout(1,4);
 			g.setHgap(5);
 			leftSuperSubPanel[i].setLayout(g);
 			leftSuperSubPanel[i].setVisible(false);
 			leftSuperSubPanel[i].setBackground(ColorScheme.DEFAULT_SECONDARY);						
 			
 			leftSubLayout[i+1].putConstraint(SpringLayout.NORTH, leftSuperSubPanel[i],
 				30, SpringLayout.NORTH,leftSubPanel[i+1]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.SOUTH, leftSuperSubPanel[i],
 					-15, SpringLayout.SOUTH,leftSubPanel[i+1]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.WEST, leftSuperSubPanel[i],
 					10, SpringLayout.WEST,playerNamesR[i]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.EAST, leftSuperSubPanel[i],
 					-10, SpringLayout.EAST,playerNamesR[i]);
 			
 			leftSubPanel[i+1].add(leftSuperSubPanel[i]);
 			leftSubPanel[i+1].add(playerNamesR[i]);
 		}
 
 		
 		for(int i=0;i<scoreButtonLeft.length;i++)
 			for(int j=0;j<scoreButtonLeft[0].length;j++){
 				scoreButtonLeft[i][j] = new AlyButton((j+1)*10+"");				
 				scoreButtonLeft[i][j].addActionListener(this);
 				scoreButtonLeft[i][j].setActionCommand("right");
 				leftSuperSubPanel[i].add(scoreButtonLeft[i][j]);
 			}	
 		
 		//add the "+" buttons
 		plusButtonRight = new AlyButton[4];
 		for(int i=0;i<plusButtonRight.length;i++){
 			 plusButtonRight[i] = new AlyButton("+");
 			 plusButtonRight[i].flipColors();
 			 plusButtonRight[i].addActionListener(this);
 			 plusButtonRight[i].setFont(new Font("Mangal",Font.PLAIN,40));
 			 
 			 leftSubLayout[i+1].putConstraint(SpringLayout.NORTH, plusButtonRight[i],
 					0, SpringLayout.NORTH,leftSubPanel[i+1]);
 			 leftSubLayout[i+1].putConstraint(SpringLayout.WEST, plusButtonRight[i],
 					15, SpringLayout.WEST,leftSubPanel[i+1]);
 			 leftSubLayout[i+1].putConstraint(SpringLayout.EAST, plusButtonRight[i],
 					-15, SpringLayout.EAST,leftSubPanel[i+1]);
 			 leftSubLayout[i+1].putConstraint(SpringLayout.SOUTH,plusButtonRight[i],
 					0, SpringLayout.SOUTH,leftSubPanel[i+1]);
 			 
 			 leftSubPanel[i+1].add(plusButtonRight[i]);
 		}
 		
 	}
 
 	public void actionPerformed(ActionEvent ev) {
 		AlyButton pressed = new AlyButton();
 		if(ev.getSource() instanceof AlyButton)
 			pressed = (AlyButton) ev.getSource();
 		
		if(pressed.getText().equals("+"))
			System.out.println(chosenPlayer().getScore());			
 	}
 
 	private Player chosenPlayer() {
 		LinkedList<String> temp = PlayerIO.getAllPlayers();
 		String list[];
 		
 		list = new String[temp.size()+1];
 		for(int i=0;i<list.length-1;i++)
 			list[i] = " "+temp.get(i);
 		list[list.length-1] = "zzzzzzzzzzzzzzzz";
 		Arrays.sort(list);
 		list[list.length-1] = " Create a new player!"; 
 									
 		String result = (String) JOptionPane.showInputDialog(this.getContentPane(),null,"Choose Player",JOptionPane.PLAIN_MESSAGE,null,list,0);
		
 		int chosen = Arrays.binarySearch(list, result);
 				
 		if(chosen < 0){
 			
 			JLabel label = new JLabel("Who's the new player?");
 			label.setFont(new Font("Mangal",Font.BOLD,16));
 			label.setHorizontalAlignment(JLabel.CENTER);
 			
 			String s = (String) JOptionPane.showInputDialog(this.getContentPane(),label,"Add Player",JOptionPane.PLAIN_MESSAGE,null,null,null);
 			int check = Arrays.binarySearch(list, " "+s);
 	
 			if(check>=0){
 				label.setText("Already Exists");
 				JOptionPane.showMessageDialog(this.getContentPane(),label,"Not so fast",JOptionPane.PLAIN_MESSAGE,null);
 				return PlayerIO.getPlayer(list[check].trim());	
 			}
 			PlayerIO.addPlayer(s);
 			return PlayerIO.getPlayer(s);
 		}else
 			return PlayerIO.getPlayer(list[chosen].trim());			
 	}
 
 }
