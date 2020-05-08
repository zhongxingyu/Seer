 package com.earlofmarch.reach.gui;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JPanel;
 import javax.swing.SpringLayout;
 /**
  * A grid of score buttons for each player
  * @author Aly
  *
  */
 @SuppressWarnings("serial")
 public class ScoreCell extends JPanel implements ActionListener{
 
 	private UIButton[] button;
 	private GroupedCell parent;
 	private JPanel mainPanel;
 	private boolean collapsing, forcedGone;
 	
 	public ScoreCell(GroupedCell g){
 		super();
 		setBackground(UI.colour.BACKGROUND);	
 		
 		mainPanel = new JPanel();
 		mainPanel.setBackground(null);
 				
 		int[] scores = {5,10,20,40,-5,-10,-20,-40};
 		button = new UIButton[scores.length];
 		
 		GridLayout grid = new GridLayout(scores.length/2,2,0,0);
 		mainPanel.setLayout(grid);
 		
 		for(int i=0;i<button.length;i++){
 			button[i] = new UIButton("",false);
 //			button[i].invertColors();
 			button[i].setText((scores[i]>0?"+":"")+scores[i]);
 			button[i].setActionCommand(scores[i]+"");
 			button[i].addActionListener(this);
 			button[i].setFont(new Font("Verdana",Font.PLAIN,14));
 			mainPanel.add(button[i]);			
 		}
 		
 		parent = g;
 		
 		SpringLayout layout = new SpringLayout();
 		layout.putConstraint(SpringLayout.EAST, mainPanel, 0, SpringLayout.EAST, this);
 		layout.putConstraint(SpringLayout.NORTH, mainPanel, 0, SpringLayout.NORTH, this);
 		layout.putConstraint(SpringLayout.WEST, mainPanel, 0, SpringLayout.WEST, this);
 		layout.putConstraint(SpringLayout.SOUTH, mainPanel, 0, SpringLayout.SOUTH, this);
 		setLayout(layout);
 		
 		add(mainPanel);
 		
 	}
 	
 	public void actionPerformed(ActionEvent e) {
 		parent.giveScore(Integer.parseInt(e.getActionCommand()));
 	}
 	
 	public void setShowing(final boolean visible, boolean animate){	
 		collapsing = true;
 		if(animate){
 			//System.out.println("Animate");
 			Timer timer = new Timer();			
 			timer.scheduleAtFixedRate(new TimerTask() {
 				int time = 4;
 				public void run()
 				{	
 					if(forcedGone){
 						for(int i=0;i<button.length;i++)
 							button[i].setVisible(false);
						collapsing = false;
 						time = 0;
						this.cancel();		
 						forcedGone = false;
 					}
 					
 					if(time > 0){
 						//System.out.println(visible?(4-time)+" set and "+(4-(time-1))+" set":(time-1)+" set");
 						if(visible){
 							button[(8-(time*2))].setVisible(visible);
 							button[(8-(time*2))+1].setVisible(visible);
 						}else{
 							button[(time*2)-1].setVisible(visible);
 							button[(time*2)-2].setVisible(visible);
 						}
 						time--;					
 					}else{
 						this.cancel();
 						collapsing = false;
 					}				
 				}
 			}, 100, 100);				
 		}else{
 			for(int i=0;i<button.length;i++)
 				button[i].setVisible(visible);
 			collapsing = false;
 		}
 	}
 	
 	public boolean isCollapsing(){
 		return collapsing;
 	}
 
 	public void forceGone() {
 		forcedGone = true;
 	}
 
 }
