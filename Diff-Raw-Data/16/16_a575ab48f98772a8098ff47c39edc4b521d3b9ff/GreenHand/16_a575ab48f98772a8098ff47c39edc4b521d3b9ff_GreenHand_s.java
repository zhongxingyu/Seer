 package bang.bang;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class GreenHand {
 	String Card;
 	int active;
 	boolean inPlay;
 	
	public void GreenHand(String card) {
 		Card = card;
 		active = 0;
		inPlay = false;
	}
 	
 	public Boolean isPlayable(){
 		return active > 0;
 	}
 	
 	public Boolean isInPlay() {
 		return inPlay;
 	}
 	
 	public void activate() {
 		active++;
 	}
 	
 	public void play() {
 		inPlay = true;
 	}
 	
 }
