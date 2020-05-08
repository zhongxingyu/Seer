 package kingOfT;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 
 public class Game {
 public ArrayList<Monster> monsters;
 public Monster inTokyo;
 public Monster curMon;
 public void cleanUp() {
 	for(Monster m: monsters){
 		if (m.getHealth()<=0){
 			lose(m);
 		}
 		if (m.getHealth() >= 20){
 			win(m);
 		}
 	}
 	int nextMon = (monsters.indexOf(curMon) + 1) % (monsters.size() + 1);
 	curMon = monsters.get(nextMon);
 }
 private void win(Monster m) {
 	end();
 	
 }
 private void lose(Monster m) {
 	monsters.remove(m);
 	if(inTokyo == m) {
 		if(curMon == inTokyo){
 			inTokyo = null;
 		}else{
 			inTokyo = curMon;
 		}
 	}
 }
 public void takeTurn() {
	diceSet dice = new diceSet(3);
 	while dice.
 }
 public Game (int num_of_monsters, String[] names) {
 	inTokyo = null;
 	for (int i= 0; i <num_of_monsters; i++){
 		monsters.add(new Monster(names[i]));
 	}
 	Collections.shuffle(monsters);
 	curMon = monsters.get(0);
 }
 }
