 import java.util.ArrayList;
 
 public class DotComBust {
 	
 	GameHelper helper = new GameHelper();
	private ArrayList<DotCom> dotComsList;
 	int numOfGuesses = 0;
 
 	private void setUpGame() {
 		// initialize DotCom objects with names and locations
 		DotCom dotCom1 = new DotCom();
 		dotCom1.setName("Pets.com");
 		DotCom dotCom2 = new DotCom();
 		dotCom2.setName("toys.com");
 		DotCom dotCom3 = new DotCom();
 		dotCom3.setName("boo.com");
 		
 		dotComsList.add(dotCom1);
 		dotComsList.add(dotCom2);
 		dotComsList.add(dotCom3);
 		
 		for (DotCom dotComToSet : dotComsList) {
 			ArrayList<String> newLocation = helper.placeDotCom(3);
 			dotComToSet.setLocationsCells(newLocation);
 		}
 		
 		//Display instructions
 		System.out.println("Guess location to destroy a DotCom");
 	}
 	
 	private void startPlaying(){
 		while (!dotComsList.isEmpty()) {
 			String userInput = helper.getUserInput("What's your guess?");
 			checkUserGuess(userInput);
 		}
 		finishGame();
 	}
 	
 	private void checkUserGuess(String userGuess){
 		numOfGuesses++;
 		String result = "miss";
 		for (DotCom DotComToCheck : dotComsList) {
 			result = DotComToCheck.checkYourself(userGuess);
			if (result.equals("miss")){
 				break;
 			} else if (result.equals("kill")) {
 				dotComsList.remove(DotComToCheck);
 				break;
 			}
 		}
		System.out.println(result);
 	}
 
 	private void finishGame() {
 		System.out.println("Game Over!");
 		System.out.println("Good job! It only took you " + numOfGuesses +" to take down all DotComs.");
 	}
 	
 	public static void main(String[] args) {
		// TODO Auto-generated method stub
 		DotComBust game = new DotComBust();
 		game.setUpGame();
 		game.startPlaying();
 
 	}
 
 }
