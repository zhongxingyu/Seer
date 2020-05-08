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

         gameStateImplementer.setWhoseTurn(0);
        Card[] diceDiscs = {Card.KAT, Card.GRIMREAPER, Card.NOT_A_CARD, Card.NOT_A_CARD,
                 Card.NOT_A_CARD, Card.NOT_A_CARD, Card.NOT_A_CARD};
         gameStateImplementer.setPlayerCardsOnDiscs(0, diceDiscs);
         gameStateImplementer.printStats();
 
     }
 
 
 }
