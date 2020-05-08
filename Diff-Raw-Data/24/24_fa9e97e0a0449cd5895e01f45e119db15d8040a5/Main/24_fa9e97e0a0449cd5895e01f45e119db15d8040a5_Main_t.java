 package com.earlofmarch.reach.gui;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 
 import com.earlofmarch.reach.input.BuzzerBinding;
 import com.earlofmarch.reach.model.Game;
 import com.earlofmarch.reach.model.GameIO;
 import com.earlofmarch.reach.model.Music;
 import com.earlofmarch.reach.model.Player;
 import com.earlofmarch.reach.model.PlayerIO;
 /**
  * The Main class... Manages the GroupedCells and triggers
  * @author Aly
  *
  */
 @SuppressWarnings("serial")
 public class Main extends JFrame implements ActionListener, KeyListener{
 	
 	private GroupedCell[] cells;
 	private JLabel scoreLabel;
 	private int rightScore = 0, leftScore = 0;
 	public static final int RIGHT = 1, LEFT = 2;
 	public static LinkedList<Player> players;
 	public static List<Player> teamLEFT, teamRIGHT;
 	private Game game;
 	private String packName;
 	
 	public Main(BuzzerBinding b,String name){
 		super("Reach for the Top - "+name);		
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		this.setLocation((int)(dim.getWidth()/2)-525,(int)(dim.getHeight()/2)-250);
 		setMinimumSize(new Dimension(800,480));
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
 		
 		getContentPane().setBackground(UI.colour.BACKGROUND);
 		createComponents(b);
 		
 		pack();
 		setSize(1050,500);	
 		players = PlayerIO.getAllPlayers(false);
 		if(players == null)
 			players = new LinkedList<Player>();
		for(Player p:players)
			System.out.println(p.getName());
 		
 		teamLEFT = new ArrayList<Player>();
 		teamRIGHT = new ArrayList<Player>();
 		for(int i=0;i<4;i++){
 			teamLEFT.add(null);
 			teamRIGHT.add(null);
 		}
 				
 		try {
 			this.packName = name;
 			if(GameIO.getGameNames().get(packName)==null)
 				game = null;
 			else{
 				System.out.print("Reading game...");
 				game = GameIO.getGameByName(GameIO.getGameNames().get(packName));
 				teamLEFT = game.getTeamA();				
 				teamRIGHT = game.getTeamB();
 				for(int i=0;i<8;i++)
 					cells[i].givePlayer(i<4?teamLEFT.get(i):teamRIGHT.get(i-4));
 				leftScore = game.getScoreA();
 				rightScore = game.getScoreB();
 				scoreLabel.setText(leftScore+"          "+rightScore);
 				System.out.println("Success.");
 			}
 			
 		} catch (IOException e) {}
 		
 		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 		        public void run() {		        	   					
 		        	if(game == null)
 		        		game = new Game(packName,new GregorianCalendar(),Main.teamLEFT,Main.teamRIGHT,leftScore,rightScore);
 		        	else{
 		        		game.addScore(Game.TEAMA, leftScore - game.getScoreA());		        		
 		        		game.addScore(Game.TEAMB, rightScore - game.getScoreB());
 		        		for(int i=0;i<8;i++){
 			        		if(i<4)
 			        			game.setPlayer(Game.TEAMA,i,teamLEFT.get(i));
 			        		else
 			        			game.setPlayer(Game.TEAMB,i-4,teamRIGHT.get(i-4));			        		
 			        	}
 		        	}
 		        	try{
 		        		GameIO.saveGame(game);
 		        	}catch(IOException e){
 		        		e.printStackTrace();
 		        	}
 		        	if(players!=null && players.size()!=0)
 		        		PlayerIO.updateAll(players);
 		        }				
 		    }, "Shutdown-thread"));		
 	}
 	
 	private void createComponents(BuzzerBinding b){
 		SpringLayout layout = new SpringLayout();
 		setLayout(layout);		
 		
 		scoreLabel = new JLabel("0          0");
 		scoreLabel.setFont(UI.getFont(48));
 		scoreLabel.setBackground(null);
 		scoreLabel.setHorizontalAlignment(JLabel.CENTER);
 		scoreLabel.setForeground(UI.colour.SECONDARY);	
 		
 		addKeyListener(this);
 				
 		GridLayout cellsLayout = new GridLayout(1,2,50,0);
 		JPanel cellsPanel = new JPanel(cellsLayout);
 		cellsPanel.setOpaque(false);
 		
 		GridLayout right = new GridLayout(1,4,15,0);		
 		JPanel leftPanel = new JPanel(right);
 		leftPanel.setOpaque(false);
 		
 		GridLayout left = new GridLayout(1,4,15,0);
 		JPanel rightPanel = new JPanel(left);
 		rightPanel.setOpaque(false);
 				
 		cells = new GroupedCell[8];
 		
 		for(int i=0;i<4;i++){
 			cells[i] = new GroupedCell(i,this,LEFT, b);
 			cells[i].setVisible(false);
 			cells[i+4] = new GroupedCell(i+4,this,RIGHT, b);
 			cells[i+4].setVisible(false);
 			leftPanel.add(cells[i]);
 			rightPanel.add(cells[i+4]);
 		}
 		animateCells();
 		
 		add(scoreLabel);	
 		cellsPanel.add(leftPanel);		
 		cellsPanel.add(rightPanel);
 		
 		add(cellsPanel);	
 		
 		JPanel bonusPanel = new JPanel(new FlowLayout());
 		bonusPanel.setBackground(UI.colour.SECONDARY);
 		
 		String bonuses[] = {" 5 "," 10 "," 15 "," 20 ",};
 		short numberOfPlaceHolders = 3;
 		
 		for(int i=0;i<(bonuses.length*2)+numberOfPlaceHolders;i++){
 			UIButton bonusButton = new UIButton("",true);
 			bonusButton.addActionListener(this);
 			bonusButton.invertColors();
 			if(i<4){
 				bonusButton.setText(bonuses[i]);
 				bonusButton.setActionCommand("left");
 			}else if (i > 6){
 				bonusButton.setText(bonuses[i-7]);
 				bonusButton.setActionCommand("right");
 			}else{
 				JLabel placeholder = new JLabel(i==5?"BONUS":"       ");
 				placeholder.setFont(UI.getFont(16));
 				placeholder.setForeground(UI.colour.MAIN);
 				bonusPanel.add(placeholder);
 			}
 			bonusPanel.add(bonusButton);
 		}
 		
 		add(bonusPanel);
 		
 		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, scoreLabel, 0, SpringLayout.HORIZONTAL_CENTER, getContentPane());
 		layout.putConstraint(SpringLayout.NORTH, scoreLabel, 30, SpringLayout.NORTH, getContentPane());
 		layout.putConstraint(SpringLayout.NORTH, cellsPanel, 10, SpringLayout.SOUTH, scoreLabel);
 		layout.putConstraint(SpringLayout.SOUTH, cellsPanel, -10, SpringLayout.NORTH, bonusPanel);
 		layout.putConstraint(SpringLayout.WEST, cellsPanel, 10, SpringLayout.WEST, getContentPane());
 		layout.putConstraint(SpringLayout.EAST, cellsPanel, -10, SpringLayout.EAST, getContentPane());
 		layout.putConstraint(SpringLayout.EAST, bonusPanel, 0, SpringLayout.EAST, getContentPane());
 		layout.putConstraint(SpringLayout.WEST, bonusPanel, 0, SpringLayout.WEST, getContentPane());
 		layout.putConstraint(SpringLayout.SOUTH, bonusPanel, -10, SpringLayout.SOUTH, getContentPane());		
 	}
 	
 	private void animateCells() {
 		Timer timer = new Timer();	
 		timer.scheduleAtFixedRate(new TimerTask() {
 			int time = 6;
 			public void run()
 			{					
 				if(time < 5 && time > 0){
 					//System.out.println((4-time)+" and "+(time+3));
 					cells[4-time].setVisible(true);
 					cells[time+3].setVisible(true);
 					time--;					
 				}else if(time >=5){
 					time--;
 				}else{
 					this.cancel();
 				}				
 			}
 		}, 100, 100);
 	}
 	
 	/*
 	 *  0 to 3 is the left side, 4-7 is the right side.
 	 */
 	public void trigger(int cell){
 		for(GroupedCell gc : cells)
 			if(gc.isTriggered()||gc.isCollapsing())
 				return;
 
 		Music.playMusic(getClass().getClassLoader().getResource("assets/buzz.wav"));
 		cells[cell].trigger();
 	}
 	
 	public void giveScore(int score,int side){
 		//Play music depending on score
 		if(score > 0)
 			Music.playMusic(getClass().getClassLoader().getResource("assets/good.wav"));
 		else if (score < 0)
 			Music.playMusic(getClass().getClassLoader().getResource("assets/fail.wav"));
 		
 		if(side == RIGHT)
 			rightScore += score;
 		else
 			leftScore += score;
 		
 		scoreLabel.setText(leftScore+"          "+rightScore);
 	}	
 		
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		//For bonuses only...
 		UIButton in = (UIButton)e.getSource();
 		giveScore(Integer.parseInt(in.getText().trim()),e.getActionCommand().startsWith("l")?Main.LEFT:Main.RIGHT);		
 	}
 
 	
 	public void keyTyped(KeyEvent e) {}
 	public void keyReleased(KeyEvent e) {}
 	
 
 	
 	public void keyPressed(KeyEvent e) {
 		if(e.getKeyChar()==' '){
 			int i = 0;
 			for(GroupedCell gc : cells){
 				if(gc.isTriggered()&&!gc.isCollapsing())
 					gc.giveScore(0);
 				else if(gc.isCollapsing())
 					gc.forceGone();
 				System.out.println((gc.clear()?"Cleared ":"Failed to clear ")+i);
 				i++;
 			}
 		}
 		if(e.getKeyChar()<'9' &&e.getKeyChar()>'0')
 			trigger(e.getKeyChar()-'1');		
 	}
 }
