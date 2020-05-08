 package edu.ucsb.cs56.projects.games.towers_of_hanoi.utility;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 import java.util.ArrayList;
 
 import edu.ucsb.cs56.projects.games.towers_of_hanoi.model.TowersOfHanoiState;
 import edu.ucsb.cs56.projects.games.towers_of_hanoi.model.TowersOfHanoiState.TowersOfHanoiIllegalMoveException;
 
 /**
  * This extends JPanel and draws the towers and disks on each tower, given a TowersOfHanoiState.  It also contains buttons to move the disks from tower to tower 
  * @author amwexler
  *
  */
 public class GamePanel extends JPanel {
 	private final int DISK_HEIGHT;//height of each disk
 	private final int DISK_OFFSET;//vertical space between each disk
 	private final int TOWER_WIDTH;//Width of the tower
 	private final int TOWER_OFFSET;//Horizontal space between towers
 	private final int INITIAL_OFFSET;//the offset from the top of the panel
 	private int sideOffset;//The horizontal space between the side towers and the side of the Panel
 	private final Color TOWER_COLOR = Color.RED;//color of towers
 	private final Color DISK_COLOR = Color.BLUE;//color of disks
 	private int maxDisk;//value of the biggest disk possible on the towers
 	private int towerHeight;//vertical height of the towers
 	private int to, from;//to: the value of the tower that a disk is being moved to; from: value of the tower that a disk is being moved from
 	private TowersOfHanoiState state;
 	
 	private ArrayList<ArrayList<Integer>> towers;
 	private HanoiTimer timer;
 
 	public GamePanel(){
 		super();
 		TOWER_WIDTH = 80;
 		DISK_HEIGHT = 10;
 		TOWER_OFFSET = 20;
 		DISK_OFFSET = 20;
 		INITIAL_OFFSET = 50;
 		sideOffset = (this.getWidth()-(3*TOWER_WIDTH) - (2 * TOWER_OFFSET))/2;
 		for(int tower = 1; tower <= 3; tower ++){//adds 3 buttons for each tower
 			JButton temp = new JButton(Integer.toString(tower));
 			temp.addActionListener(new TowerButtonListener(tower));
 			this.add(temp);
 		}
 	}
 	
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		if(towers==null){
 			return;
 		}
 		else{
 			sideOffset = (this.getWidth()-(3*TOWER_WIDTH) - (2 * TOWER_OFFSET))/2;
 			for(int tower = 1; tower <= towers.size(); tower++){
 				g.setColor(TOWER_COLOR);
 				int towerX = sideOffset + (TOWER_WIDTH+TOWER_OFFSET)*(tower-1);// X coordinate of this tower
 				g.fillRect(towerX,INITIAL_OFFSET,TOWER_WIDTH,towerHeight);
 				for(int j=towers.get(tower-1).size();j > 0;j--){
 					int offset =  DISK_OFFSET*((towers.get(tower-1).size()-j)-1);
 					int y = (towerHeight + TOWER_OFFSET) - offset;
 					int diskWidth = TOWER_WIDTH - (TOWER_WIDTH/(maxDisk+1))*(maxDisk- towers.get(tower-1).get(j-1)); //Basically, this is: (max disk width)-(ratio of width : # of disks)*(biggest disk value - this disk's value).  This makes the bar width scale with # of disks and size of tower
 					int x = towerX+((TOWER_WIDTH-diskWidth)/2);
 					g.setColor(DISK_COLOR);
 					g.fillRect(x,y,diskWidth,DISK_HEIGHT);
 				}
 			}
 		}
 	}
 
 	public void setState(TowersOfHanoiState s){
 		state = s;
 		towers=s.getTowers();
 		maxDisk=towers.get(0).size()-1;//this assigns maxBar to the # of bars - 1 which is the same as the max # that represents a bar (ie: max possible value returned by towers.get(a).get(b) )
 		towerHeight = (maxDisk + 1) * 2 * DISK_HEIGHT;
 	}
 	
 	public void setTimer(HanoiTimer timer){
 		this.timer = timer;
 	}
 	
 	private class TowerButtonListener implements ActionListener{
 		
 		int towerNum;//this is the number that represents the tower
 		
 		public TowerButtonListener(int towerNum){
 			this.towerNum = towerNum;
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if(from == 0){				
 				from = towerNum;
 				return;
 			}
 			if(to == 0){
 				to = towerNum;
 				try {
 					state.makeMove(from-1, to-1);// -1 because the game calls the towers by 1-3 while the code calls them 0-2.  This also allows for 0 to represent unassigned for this method
 				} catch (TowersOfHanoiIllegalMoveException e) {
 					System.out.println("Illegal Move");
 				}
 				GamePanel.this.repaint();
 				if(state.solved()){
 					state.handleWin();
 					if(timer!=null){
 						timer.stop();
 					}
 				}
 				to = 0;  from = 0;
 				return;
 			}
 			to = 0;  from = 0;
 			actionPerformed(arg0);
 		}
 		
 	}
 }
 
