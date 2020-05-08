 package Play;
 
 import World.World;
 import Faction.*;
 import HTNP.*;
 
 public class Play {
 	public static void main(String[] args) {
 		Faction f = new TestFaction();
 		Faction f2 = new TestFaction();
 		World targetWorld = new World();
 		targetWorld.setControllingFaction(f2);
 		f2.setWorld(targetWorld);
 		f.addKnownWorldLocation(targetWorld);
 		f2.setNumArmies(15);
 		f.setEnemy(f2);
 		f.replan();
 	
 		while(true) {
 			System.out.println(f);
			System.out.println(f.timeToReplan);
 			f.doTurn();
 			if(f.didWin())
 				break;
			
 		}
 		System.out.println(f.didWin());
 	}
 }
