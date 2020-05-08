 package se.chalmers.TowerDefence;
 
 import java.awt.List;
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.newdawn.slick.SlickException;
 
 public class Wave implements Observer{
	private ArrayList <Monster> monsterWave;
 	private Road road;
 	
 	
 	public Wave(int nbrOfMonsters, Road road) throws SlickException {
 		this.road = road;
 		createMonsters(nbrOfMonsters);
 	}
 	
 	public void move() {
 		for (int i=0; i>monsterWave.size(); i++) {
 			if(monsterWave != null){
 				monsterWave.get(i).move();
 			}
 			
 		}
 	}
 	public void createMonsters(int nbrOfMonsters) throws SlickException {
 		for (int i=0; i<nbrOfMonsters; i++) {
 //			Monster monster = new Monster(road);
 			monsterWave.add(new Monster(road));
 			monsterWave.get(0).addObserver(this);
 		}
 	}
 
 	public void update(Observable arg0, Object arg1) {
 		// TODO Auto-generated method stub
 		
 	}
 
 //	public void update(Observable o, Object arg) {
 //		if(arg == false) {
 //			for (int i=0; i<monsterWave.size(); i++) {
 //				if (o.equals(monsteWave.get(i)) {
 // 					remove(monsterWave.get(i));
 //				}
 //			}
 //			
 //		}else {
 //			
 //			
 //			
 //		}
 		
 		
 }
 	
