 package com.teamBasics.CollegeTD;
 
 import java.util.ArrayList;
 
 import com.teamBasics.framework.Graphics;
 
 public class EnemyGroup {
 	//enemies is an arrayList of enemies
 	private ArrayList<Enemy> group;
 
 	//delay in terms of number of updates before starting to spawn this group
 	private int delay;	
 		
 	//delay in terms of number of updates between enemy spawns within a group
 	private int delay_within;	
 	
 	//counter for each group which will deal with the internal delays of the class
 	//keeps a relative count dependent on time when last enemy was spawned
 	private int internal_count;
 	
 	private boolean allDead;
 	
 	public EnemyGroup(int delay, int delay_within) {
 		group = new ArrayList<Enemy>();
 		this.delay = delay;
 		this.delay_within = delay_within;
 		internal_count = 0;
 		allDead = true;
 	}
 
 	public void addEnemy(Enemy enemy) {
 		group.add(enemy);
 		allDead = false;		
 	}
 	
 	public void update(int count) {
 		boolean reset_internal_count = false;
 		allDead = true;
 		
 		if(delay <= count) {
 			for(int j=0; j<group.size(); j++) {
 				if(group.get(j).isVisible()) {
 					group.get(j).update();						
 				}
 				else if(internal_count >= delay_within) {
 					group.get(j).update();
 					internal_count = 0;
 					reset_internal_count = true;
 				}
 				if(!(group.get(j).isDead())) {
 					allDead=false;
 				}
 			}
 			if(reset_internal_count == false) {
 				internal_count++;					
 			}
 		}
 		
 	}
 	
 	public int draw(Graphics g) {
 		int lives_lost = 0;
 		for(int j=0; j<group.size(); j++) {
 			Enemy e = group.get(j);
 			if(e.isVisible()) {
 				e.draw(g);	
 			}
 			else if(e.isKamakazi()) {
 				lives_lost++;
 				e.setKamakazi(false);
 			}
 		}
 		return lives_lost;
 	}
 
 	public ArrayList<Enemy> getGroup() {
 		return group;
 	}
 
 	public void setGroup(ArrayList<Enemy> group) {
 		this.group = group;
 	}
 
 	public int getDelay() {
 		return delay;
 	}
 
 	public void setDelay(int delay) {
 		this.delay = delay;
 	}
 
 	public int getDelay_within() {
 		return delay_within;
 	}
 
 	public void setDelay_within(int delay_within) {
 		this.delay_within = delay_within;
 	}
 
 	public int getInternal_count() {
 		return internal_count;
 	}
 
 	public void setInternal_count(int internal_count) {
 		this.internal_count = internal_count;
 	}
 
 	public boolean isAllDead() {
 		return allDead;
 	}
 
 	public void setAllDead(boolean allDead) {
 		this.allDead = allDead;
 	}
 }
