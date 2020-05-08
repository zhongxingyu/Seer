 package com.earlofmarch.reach.gui;
 import java.awt.GridLayout;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 /**
  * Groups the {@link ScoreCell} and the {@link PlayerCell} into a single panel and adds the timer.
  * @author Aly
  *
  */
 @SuppressWarnings("serial")
 public class GroupedCell extends JPanel{
 	
 	private Timer timer;
 	private int time;
 	private final int TIMER_LENGTH = 10;
 	private Main parent;
 	private ScoreCell score;
 	private JLabel timeLabel;
 	private int playerTeam;
 	private boolean triggered = false;
 	private PlayerCell playerCell;
 	
 	public GroupedCell(Main f, int side){
 		parent = f;
 		playerTeam = side;
 		time = TIMER_LENGTH*10;
 		
 		SpringLayout layout = new SpringLayout();
 		
 		GridLayout grid = new GridLayout(2,1);
 		JPanel cells = new JPanel(grid);
 		
 		setLayout(layout);
 		setBackground(UI.colour.BACKGROUND);
 		
 		timeLabel = new JLabel();
 		timeLabel.setFont(UI.getFont(20));
 		timeLabel.setOpaque(false);
 		timeLabel.setHorizontalAlignment(JLabel.CENTER);
 		timeLabel.setVerticalAlignment(JLabel.BOTTOM);
 		timeLabel.setForeground(UI.colour.SECONDARY);
 		add(timeLabel);
 		
 		playerCell = new PlayerCell(f);
 		cells.add(playerCell);
 		
 		score = new ScoreCell(this);
 		score.setShowing(false,false);
 		cells.add(score);
 		
 		add(cells);
 		
 		layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, timeLabel, 0, SpringLayout.HORIZONTAL_CENTER, this);
 		layout.putConstraint(SpringLayout.NORTH, timeLabel, 60, SpringLayout.NORTH, this);
 		layout.putConstraint(SpringLayout.NORTH, cells, 0, SpringLayout.SOUTH, timeLabel);
 		layout.putConstraint(SpringLayout.SOUTH, cells, -10, SpringLayout.SOUTH, this);
 		layout.putConstraint(SpringLayout.WEST, cells, 0, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.EAST, cells, 0, SpringLayout.EAST, this);
 	}
 	
 	public void giveScore(int score){
 		triggered = false;
 		
 		parent.giveScore(score,playerTeam);
		Main.players.get(Main.players.indexOf(playerCell.getPlayer())).addScore(score);
 		this.score.setShowing(false,true);		
 		timeLabel.setText((score>0?"+":"")+score);
 		
 		time = TIMER_LENGTH*10;
 		timer.cancel();
 		timer = new Timer();
 		timer.scheduleAtFixedRate(new TimerTask() {
 			int labeltime = 12;
 			public void run()
 			{
 				if(labeltime > 0)
 					labeltime--;
 				else{
 					this.cancel();
 					timeLabel.setText("");
 				}				
 			}
 		}, 100, 100);
 	}
 	
 	public void trigger(){
 		triggered = true;
 		timer = new Timer();
 		score.setShowing(true,true);
 		timer.scheduleAtFixedRate(new TimerTask() {
 			public void run()
 			{
 				if(time > 0)
 					time--;
 				else
 				{
 					this.cancel();
 					time = TIMER_LENGTH*10;
 					score.setShowing(false,true);
 					triggered = false;
 				}
 				
 				if(time == (TIMER_LENGTH*10))
 					timeLabel.setText("");
 				else if(time>((TIMER_LENGTH*10)-50))
 					timeLabel.setText((time-50)/10.0 + "s");
 				else
 					timeLabel.setText("0.0s");
 			}
 		}, 100, 100);
 	}
 	
 	public boolean isTriggered(){
 		return triggered;
 	}
 }
