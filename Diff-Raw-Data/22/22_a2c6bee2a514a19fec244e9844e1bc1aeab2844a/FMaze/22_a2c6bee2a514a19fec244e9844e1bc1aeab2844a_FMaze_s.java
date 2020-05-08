 package maze;
 
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 
 
 public class FMaze {
     public int life     = 3;
     public int lifeRPS  = 0;
     public int lifeRPS2 = 0;
     boolean    partner  = false;
     long       start    = System.currentTimeMillis();
 
     public static void main(String[] args) {
         FMaze Maze = new FMaze();
         Maze.intro();
     }
 
     void intro() {
         System.out.println(
             "Welcome to the maze.  How nice of you to join us.  " +
                 "As you see, there are three doorways in front of you.  " +
                 "Pick a door.  Find the treasure.");
         System.out.println("You have three lives");
         this.start();
     }
 
     void start() {
         System.out.println("Which hallway do you choose?  Hallway 1, 2, or 3");
 
         Scanner myScan       = new Scanner(System.in);
         int     PlayerChoice = myScan.nextInt();
 
         switch (PlayerChoice) {
 
             case 1:
                 this.hallway1();
                 /* fall-through */
 
             case 2:
                 this.hallway2();
                 /* fall-through */
 
             case 3:
                 this.hallway3();
                 /* fall-through */
 
             default:
                 System.out.println("Invalid! Please reenter");
         }
     }
 
     void guard() {
         Scanner TS = new Scanner(System.in);
         System.out.println(
             "As you walk down the hallway, you happen upon a guard.  " +
                 "Do you take a \"stance\" or do you \"talk\"?");
 
         String choice = TS.nextLine();
         choice = choice.toLowerCase();
 
         switch (choice) {
 
             case "stance": {
                 System.out.println(
                     "The gaurd interprets your stance as a challenge to fight.  " +
                         "He attacks you and you die.");
                 JOptionPane.showMessageDialog(null, "Game over.  You ran out of lives.");
                 System.exit(1);
             }
 
             case "talk": {
                 System.out.println(
                     "The gaurd walks up to you.  " +
                         "You find out that he actually is a prisoner here too " +
                         "and he would like to escape also.");
                 System.out.println(
                     "Type \"leave\" to leave him behind.  " +
                         "Type \"continue\" to allow him to join.");
                 /* fall-through */
             }
         }
 
         String choice2 = TS.nextLine();
         choice2 = choice2.toLowerCase();
 
         switch (choice2) {
 
             case "continue": {
                 System.out.println(
                     "You let him join you on your quest to escape and to find the treasure.");
                 partner = true;
 
                 break;
             }
 
             case "leave": {
                 System.out.println("You chose to leave the guard behind.");
                 /* fall-through */
             }
         }
 
         this.dragonroom();
     }
 
     void dragonroom() {
         System.out.println(
             "You stumble upon a room with a dragon in it.  " +
                 "You quickly decide that there are two plausible actions to take.  " +
                 "Choose either \"hide\" to crawl in the shadows around the dragon " +
                 "or type \"bow\" to pick up a nearby bow " +
                 "and attempt to shoot the dragon through the eye and kill it.");
 
         Scanner TS     = new Scanner(System.in);
         String  choice = TS.nextLine();
         choice = choice.toLowerCase();
 
         if (choice.equals("hide") && (partner == true)) {
             System.out.println(
                 "You crawled in the shadows around the dragon " +
                     "and managed to go to the next room unnoticed.  " +
                     "However, your partner the guard, who went the other way around, " +
                     "did get noticed and was eaten.");
         }
 
         if (choice.equals("hide") && (partner == false)) {
             System.out.println(
                 "You crawled in the shadows around the dragon " +
                     "and managed to go to the next room unnoticed.");
         }
 
         if (choice.equals("bow")) {
             System.out.println(
                 "You shot the bow at the dragon.  " +
                     "However, you missed and it glanced off its scales.  " +
                     "The dragon now noticed you and is now breathing fire on you.");
 
             for (int BurnChance = 0; BurnChance < 2; BurnChance++) {
                 int ChanceToBurn = (int) (Math.random() * 3);
 
                 switch (ChanceToBurn) {
 
                     case 1: {
                         life = life - 1;
                         System.out.println("You lost a life from burns.  You have " + life +
                                 " lives left");
 
                         break;
                     }
                 }
 
                 switch (life) {
 
                     case 0: {
                         JOptionPane.showMessageDialog(null,
                             "Game over.  You ran out of lives.  " +
                                 "The birds killed you nearly instantly.");
                         System.exit(1);
                     }
 
                     default:
                         System.out.println("You now advance to the final room.");
 
                         break;
                 }
             }
         }
 
         this.FinalRoom();
     }
 
     void desert() {
         Scanner choiceScan = new Scanner(System.in);
         System.out.println(
             "As you walk in, the heat blasts you instantly.  " +
                 "\"The voice was definetly right when it said WARM, SUNNY days.  " +
                 "This place is a desert!\", you think.");
         System.out.println(
             "You start to grow thirsty as you slowly trek to the other side of the desert, " +
                 "to a far-off door you see.");
         System.out.println(
             "Eventually, you realize you need to get out faster.  " +
                 "You spot an eagle in a nest, sleeping.  " +
                 "Maybe it can carry you faster.  " +
                 "Type \"eagle\" to try to grab onto the eagle and hope it carries you to the door.");
         System.out.println("Type \"walk\" to continue walking along.");
 
         String choice = choiceScan.nextLine();
         choice = choice.toLowerCase();
 
         if (choice.equals("eagle")) {
             System.out.println(
                 "you walk over to the eagle and it wakes up and claws your face off.");
             life = 0;
 
             switch (life) {
 
                 case 0: {
                     JOptionPane.showMessageDialog(null,
                         "Game over.  You ran out of lives.  " +
                             "The birds killed you nearly instantly.");
                     System.exit(1);
                 }
 
                 default:
                     System.exit(1);
             }
         }
 
         if (choice.equals("walk")) {
             System.out.println(
                 "You continue walking and see a water fountain behind the next cactus.  " +
                     "You refresh yourself and finish walking the final distance to the door " +
                     "and continue into the hallway beyond.");
             this.dragonroom();
         }
     }
 
     void snakes() {
         System.out.println(
             "Now do you see?  This room is full of Pythons.  " +
                 "They slowly kill you until you think you have found a way out.");
         System.out.println(
             "Your first option is to pick up a nearby rusty dagger " +
                 "and try to slice off the snakes, " +
                 "although there is a chance you will slice yourself.");
         System.out.println(
             "The other option: off in the distance you see a bunch of birds flying in a room.  " +
                 "You know snakes are afraid of birds.");
         System.out.println("Which option do you choose? Option 1 or Option 2?");
 
         Scanner myScan       = new Scanner(System.in);
         int     PlayerChoice = myScan.nextInt();
 
         if (PlayerChoice == 1) {
             this.dagger();
         }
 
         if (PlayerChoice == 2) {
             this.birds();
         }
     }
 
     void dagger() {
         System.out.println("You grab the dagger and start slicing off the snakes.");
 
         int ChanceToDie = (int) (Math.random() * 3);
 
         if (ChanceToDie == 2) {
             life = life - 1;
             System.out.println("You lost a life from accidentaly hurting yourself.  You have " +
                     life + " lives left");
         }
 
         switch (life) {
 
             case 0: {
                 JOptionPane.showMessageDialog(null, "You are dead!");
                 System.exit(1);
             }
 
             default:
                 System.out.println("You continue to the next room, free of snakes.");
                 this.FinalRoom();
                 /* fall-through */
         }
 
     }
 
     void birds() {
         System.out.println(
             "As you walk into the room, the snakes instantly shrink away " +
                 "back to the previous.  You turn around...");
         System.out.println(
             "and breath a sigh of relief.  Although the birds seem slightly agitated, " +
                 "you think nothing of it.");
         System.out.println(
             "You turn and walk away into another hallway with a light at the end.  " +
                 "As you approach the light, you sense something coming...");
 
         this.FinalRoom();
     }
 
     void FinalRoom() {
         System.out.println(
             "As you walk in the room, you immediately notice a golden glow " +
                 "coming from a fountain in the center.");
         System.out.println(
             "As you step towards it, a flood of Cassoworries " +
                 "swarms out of the hallway from the bird room.  " +
                 "You take the defensive, knowing these birds can kill.");
         System.out.println(
             "You can either sing to the birds to calm them" +
                 " or grab a loaded shotgun from a nearby statue's hands.  " +
                 "Type \"sing\" or \"shotgun\"");
 
         Scanner myScan   = new Scanner(System.in);
         String  Decision = myScan.nextLine();
         Decision = Decision.toLowerCase();
 
         switch (Decision) {
 
             case "sing": {
                 this.sing();
             }
 
             case "shotgun": {
                 this.shotgun();
             }
 
             default: {
                 System.out.println("Invalid.  Please reenter");
             }
         }
 
     }
 
     void sing() {
 
         System.out.println(
             "You attempt to soothe the birds.  They listen to you.  " +
                 "However, after about a minute, " +
                 "they grow tired of listning and attack you.");
         life = life - 3;
 
         switch (life) {
 
             case 0: {
                 JOptionPane.showMessageDialog(null,
                     "Game over.  You ran out of lives.  " +
                         "The birds killed you nearly instantly.");
                 System.exit(1);
             }
         }
     }
 
     void shotgun() {
         System.out.println(
             "As soon as you pick it up, the birds shy away.  They seem afraid of it.  " +
                 "You wave it at them and they shriek and run away.  " +
                 "You continue to the golden fountain and find the fabled Holy Grail.");
         System.out.println(
             "As soon as you grab the Holy Grail, " +
                 "a door opens in the wall next to the fountain.  " +
                 "You walk up to it.");
         this.doorcombo();
         ;
     }
 
     void doorcombo() {
         Scanner comboguess = new Scanner(System.in);
         System.out.println("This door has a combination lock on it.  " +
                 "Guess the one digit number");
 
         while (true) {
             int CG = comboguess.nextInt();
 
             switch (CG) {
 
                 case 3: {
                     System.out.println("Correct!");
                     this.boss();
                 }
 
                 default:
                     System.out.println("Incorrect.  Guess again!");
             }
         }
     }
 
     void boss() {
         System.out.println("As you enter the room, you see a guard standing there.");
         System.out.println(
             "\"You may not leave yet.  " +
                 "In order to leave, you must beat me in Rock, Paper, Scissors!  " +
                 "And you NEVER WILL!\"");
 
         while (true) {
 
             // Creates scanner
             Scanner myScan = new Scanner(System.in);
 
             // Whatever player types in is stored as a string named Player
             String Player = myScan.nextLine();
 
             // Transforms all of player's inputs to lower case input
             Player = Player.toLowerCase();
 
             // Generates random number
             int computerGuessNumber = (int) (Math.random() * 3);
 
             // Turns random number into output.  Basically randomly chooses
             // rock paper or scissors
             switch (computerGuessNumber) {
 
                 case 0:
                     System.out.println("I play Rock!");
                     /* fall-through */
 
                 case 1:
                     System.out.println("I play Paper!");
                     /* fall-through */
 
                 case 2:
                     System.out.println("I play Scissors!");
                     /* fall-through */
             }
 
             // Creates situations
             if (Player.equals("rock") && (computerGuessNumber == 0)) {
                 System.out.println("Draw!");
             }
 
             if (Player.equals("rock") && (computerGuessNumber == 1)) {
                 System.out.println("Gaurd got one point!");
                 lifeRPS2 = lifeRPS2 + 1;
             }
 
             if (Player.equals("rock") && (computerGuessNumber == 2)) {
                 System.out.println("You get one point!");
                 lifeRPS = lifeRPS + 1;
             }
 
             if (Player.equals("scissors") && (computerGuessNumber == 0)) {
                 System.out.println("Guard got one point!");
                 lifeRPS2 = lifeRPS2 + 1;
             }
 
             if (Player.equals("scissors") && (computerGuessNumber == 1)) {
                 System.out.println("You get one point!");
                 lifeRPS = lifeRPS + 1;
             }
 
             if (Player.equals("scissors") && (computerGuessNumber == 2)) {
                 System.out.println("Draw!");
             }
 
             if (Player.equals("paper") && (computerGuessNumber == 0)) {
                 System.out.println("You get one point!");
                 lifeRPS = lifeRPS + 1;
             }
 
             if (Player.equals("paper") && (computerGuessNumber == 1)) {
                 System.out.println("Draw!");
             }
 
             if (Player.equals("paper") && (computerGuessNumber == 2)) {
                 System.out.println("Guard got one point!");
                 lifeRPS2 = lifeRPS2 + 1;
             }
 
             // If player types "end", terminate game
             if (Player.equals("end")) {
                 System.out.println("The game is now ending.  Goodbye.  See you later!");
 
                 break;
             }
 
             // secondary way of instant win/ending game
             if (Player.equals("shoot")) {
                 System.out.println("GAAAH!  OK YOU WIN YOU WIN!");
                 lifeRPS = lifeRPS + 3;
             }
 
             if (lifeRPS == 3) {
                 this.end();
                 System.out.println("You won!");
                 JOptionPane.showMessageDialog(null, "You escaped from the maze!");
             }
 
             if (lifeRPS2 == 3) {
                 System.out.println("\"MWAHAHA I WIN YOU LOSE! GOODBYE!!\"");
                 JOptionPane.showMessageDialog(null,
                     "Game over.  You ran out of lives because the guard beat you.");
 
                 break;
             }
         }
     }
 
     protected void end() {
         Scanner TS = new Scanner(System.in);
         System.out.println("You walk out into sunshine and live the rest of your life in peace.");
 
         long end  = System.currentTimeMillis();
         long time = end - start;
         time = time / 1000;
         System.out.println("It took you " + time + " seconds to complete the maze.");
 
         String again = TS.nextLine();
         again = again.toLowerCase();
         System.out.println("To play again, type \"Play again\"");
 
         if (again.equals("play again"))
             this.intro();
     }
 }
