 package fr.iutvalence.java.mp.epicbattle;
 
 /**
  * This class represents a new game. It is compound a table of two players. //
  * TODO (fixed) next line is useless Player is a class which will be described in
  * Player.java
  * 
  * @author GERLAND KAYRAK
  * 
  */
 
 public class Battle
 {
     // Temporally in the Class battle.
     // Constants
     
     // Spells
     /**
      * Yell is the spell of Conan. It inflicts damage to the opponent.
      */
     public final static Effect YELL = new Destruction("Yell", 5);
     // Passives
     /**
      * The barbarian's karma is a passive which inflicts damages to the enemy warrior each turn.
      */
     public final static Effect BARBARIANKARMA = new Destruction("barbarian karma", 1);
     // Heroes
     /**
      * Conan is an offensive hero.
      */
     public final static Heros CONAN = new Heros("Conan", YELL, BARBARIANKARMA); 
     // Attacks
     /**
      * Ax Blow is the basic attack of the axeman.
      */
     public final static Effect AXBLOW = new Destruction("Ax Blow",10);   
     // Warriors
     /**
      * Axeman is a barbarian warrior.
      */
     public final static Warrior AXEMAN = new Warrior("Axeman", 10, 30, AXBLOW);
     
     
     
     
     // TODO (think about it) consider separating the heroes with which the player
     // plays and how he plays (i.e how he interacts with the game).
     
     /**
      * The players which will play the battle.
      */
     // TODO (fixed) rename field (just players)
     private Player[] players;   
     
     /**
      * This number represents how much turn are occurred.
      */
     private int turnNb;
     
     /**
      * The player which plays while the turn.
      */
     private Player activePlayer;
     
     /**
      * The player which doesn't play while the turn.
      */
     private Player passivePlayer;
 
     
     // TODO (fixed) do not say how it works but what it does
      /** 
      * This constructor creates a battle between two players. 
      * It also assigns a hero to each players.
      * @param name1 Name of the first player
      * @param hero1 His hero
      * @param name2 Name of the second player        
      * @param hero2 His heros
     * 
      */
 
     public Battle(String name1, Heros hero1, String name2, Heros hero2)
     {
         this.players = new Player[2];
         this.players[1] = new Player(name1, hero1);
         this.players[2] = new Player(name2, hero2);
         this.turnNb = 1;
         this.activePlayer = this.players[1];
         this.passivePlayer = this.players[2];
     }
     
     /**
      * The next turn is initialized.
      * 
      */
     private void nextTurn()
     {
         this.turnNb = this.turnNb+1;
         /**
          * If the turn is even :
          * The player 2 plays / the player 1 doesn't play
          * If the turn is uneven :
          * The player 2 doesn't play / the player 2 plays.
          */
         this.activePlayer = this.players[(this.turnNb+1)%2 + 1];
         this.passivePlayer = this.players[this.turnNb%2 + 1];
     }
     /**
      * A warrior attacks an other warrior. 
      * @param attacker The warrior who attacks
      * @param target The warrior who is attacked
      */
     public void attack(Warrior attacker, Warrior target)
     {
         attacker.attack(target);
         nextTurn();
     }
     
     /**
      * We use a spell
      * @param hero the hero using a spell
      * @param target the warrior focused
      */
     public void useSpell(Heros hero, Warrior target)
     {
         hero.useSpell(target);
         nextTurn();
     }
     
     
     
 
 }
