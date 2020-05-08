 package Testers;
 
 import Implementers.GameStateImplementer;
 import framework.cards.Card;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 19/05/12
  * Desc:
  */
 public class TestingCat {
 
     public static void main(String []args){
         GameStateImplementer gameStateImplementer = new GameStateImplementer();
        int[] num = {1,1,1};
         gameStateImplementer.setWhoseTurn(0);
        gameStateImplementer.setActionDice(num);
        Card[] diceDiscs = {Card.KAT, Card.CONSUL, Card.NOT_A_CARD, Card.NOT_A_CARD,
                 Card.NOT_A_CARD, Card.NOT_A_CARD, Card.NOT_A_CARD};
         gameStateImplementer.setPlayerCardsOnDiscs(0, diceDiscs);
         gameStateImplementer.printStats();
        gameStateImplementer.runGame();
 
     }
 
 
 }
