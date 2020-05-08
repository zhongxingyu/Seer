 package lab2;
 
 /**
  *
  * @author Scott Roethle
  */
 public class SuperMarioBrothersGame {
     private int levelNumber=1;
     private int numberOfLevels = 20;
     private int numberPoints=0;
    MarioGameCharacter mario = new MarioGameCharacter();
     GUI marioGUI = new GUI();
     
    
     public void runGame(){
         while (levelNumber < 21 && mario.getRemainingNumLives() >= 0) {
            SuperMarioBrothersLevel level1 =
                    new SuperMarioBrothersLevel(50, 600, 5, 3);
             //Code to play the game
             //once the level is completed the code will 
         }
         endGame();
     }
     
     private void endGame() {
         //code to end the game here
     }
     
     private void changeLevel(){
         levelNumber++;
     }
     
     private void loseOneLife(){
         mario.setRemainingNumLives(mario.getRemainingNumLives()-1);
         if (mario.getRemainingNumLives() == 0) {
             endGame();
         }
     }
 }
