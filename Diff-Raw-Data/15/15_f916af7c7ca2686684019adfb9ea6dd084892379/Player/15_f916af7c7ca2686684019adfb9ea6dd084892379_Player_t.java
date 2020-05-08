 package com.augustalso.bd;
 
 public class Player {
 	
 	private int gold = 3;
 
 	public int getGold() {
 		return this.gold;
 	}
 
 	public void addGold(int gold) {
 		this.gold += gold;
 	}
 
 	public void subtractGold(int gold) {
 		if (gold > this.gold){
 			throw new NotEnoughGoldException();
 		}
		else {
			this.gold -= gold;
		}
 	}
 
 	
 }
