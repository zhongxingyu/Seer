 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.awt.Font;
 import java.util.List;
 import java.util.Stack;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 /**
  * A table to put cards on.
  * 
  * @author Felipe I. Anfurrutia
  * @version 29/10/2011
  */
 public class TableGame extends World
 {
     /**
      * Enumerates de positions of the players on the table
      */
     public enum Position {
         SOUTH(0), EAST(270), NORTH(180), WEST(90);
         int rotation;
         Position(int rotation){
             this.rotation = rotation;
         }
         
         public int getRotation(){
             return rotation;
         }
     }
     
     /** Determines the max. number of players */
     private static int NUM_PLAYERS = 4;
     /** Determines the number of rows */
     private static int NUM_ROWS = 4;
     /** The players of the game */
     private Player[] players;
     /** The rows to be completed in the game */
     private CardRow[] rows;
     /** The deck of the game */
     private Deck deck;
     /** The card show to deal manually */
     private Card showCard;
     /** The controller of the player's turn */
     private TableTurn<Player> turno;
     /** Controls if the all cards are dealed */
     private boolean allCardsDealed;
 
     /**
      * Create a new table game for '6ko urrean' (seises)
      */
     public TableGame()
     {    
         super(750, 750, 1);
         Greenfoot.setSpeed(50);
        
         addDeck();           
         addPlayers();
         addRows();
         turno = new TableTurn<Player>(players);
         addObject(turno, 650, 50);
         allCardsDealed = false;
  
        // Greenfoot.start();        
     }
     
     /**
      * Crea y aade una baraja a la mesa
      */
     private void addDeck(){
         deck = new Deck(Card.Colour.BLUE, false, 1, 1, 1, 1);
         deck.fill();
         deck.shuffle();
         addObject(deck, 40, 50);
     }
     
     /**
      * Crea y aade 4 jugadores a la mesa
      */
     private void addPlayers(){
         players = new Player[NUM_PLAYERS];
         players[0] = new Player("player1", "images/ppl1.png", Position.SOUTH.getRotation());
         players[1] = new Player("player5", "images/ppl2.png", Position.EAST.getRotation());
         players[2] = new Player("player3", "images/ppl3.png", Position.NORTH.getRotation());
         players[3] = new Player("player4", "images/ppl4.png", Position.WEST.getRotation());
         addObject(players[0], 375, 650);
         addObject(players[1], 675, 350);
         addObject(players[2], 375, 50);
         addObject(players[3], 50, 350);
     }
     
     /**
      * Crea y aade 4 filas a la mesa
      */
     private void addRows(){
         //To-DO: aadir filas, empezando la primera fila en la posicin (400,200). El inicio de las siguientes filas se diferencian en 100 de unas a otras. 
         rows = new CardRow[NUM_ROWS];
         for (int i=0; i<rows.length; i++){
             rows[i] = new CardRow();
             addObject(rows[i], 400, 200 + i * 100);
         }
     }
     
     /**
      * If the deck is clicked on, show a card o deal all cards
      */
     public void act()
     {
         if (isFinished() ) {
             showTheWinner();
             Greenfoot.stop();
         }
 
         MouseInfo mouse = Greenfoot.getMouseInfo();
         if(! (mouse!=null && (mouse.getActor()==deck))) return; //If the mouse isn't used on the deck, don't do anything
         
         if (Greenfoot.mouseClicked(deck)) {
             if(showCard != null && showCard.getAccepter()==null) {
                 removeObject(showCard);
                 deck.addCard(showCard);
             }
             if (mouse.getButton() == 1 && deck.getSize()>0) { //If the clicked button is the left
                 showACard();
                 if (deck.getSize()==0)
                     allCardsDealed = true;
             }
             else if (mouse.getButton() == 3){ //If the clicked button is the right
                 dealAllCards();
             }
         }
     }
 
     /**
      * Determina si el juego ha terminado o no
      * @return True si el juego ha terminado y False, en caso contrario
      */
     private boolean isFinished(){
         //To-DO:
        //return rowsCompleted() || playersEmpty();
        return false;
     }
     
     private boolean rowsCompleted() {
         boolean completas = true;
         for (CardRow row: rows)
             if (!row.isCompleted()) 
                 completas = false;
        System.out.println("rows");
        System.out.println(completas);
         return completas;
     }
     
     private boolean playersEmpty(){
         boolean vacios = true;
         for (Player player: players)
             if (player.hasCards())
                 vacios = false;
        System.out.println("players");
        System.out.println(vacios);
         return vacios;
     }
     
     /**
      * Muestra la carta que est encima de la baraja y la coloca encima de la mesa boca arriba
      */
     private void showACard(){
         showCard = deck.drawCard();
         Greenfoot.playSound("sounds/card.wav");
         addObject(showCard, 120, 50);
         showCard.setDraggable(true);
     }
     
     /**
      * Reparte todas las cartas entre los jugadores, respetando el turno de cada uno de ellos
      */
     public void dealAllCards(){
         //To-DO:
         while(deck.getSize() != 0)
         {
             //Obtenemos la carta de la baraja y la eliminamos
             Card carta = deck.drawCard();
             //Dibujamos la carta en el tablero
             addObject(carta,120,50);
             carta.draw();
             //obtenemos el jugador del turno correspondiente
             Player player = turno.next();
             //le damos la carta a dicho jugador
             player.addCard(carta);
         }
         //hemos repartido toda la baraja
         allCardsDealed = true;
     }
     
     /**
      * Muestra en pantalla el ganador del juego
      */
     private void showTheWinner(){
         String name = getTheWinner();
         BigText text = new BigText();
         addObject(text, 200, 300);
         text.setText("Game complete! The winner is " + name);
     }
     
     /**
      * Determina el ganador del juego
      * @return el nombre del jugador que ha ganado el juego
      */
     private String getTheWinner(){
         //TO-DO:
         return "";
     }
     
     /**
      * Determina si todas las cartas estn repartidas
      * @return True si todas las cartas estn repartidas y False, en caso contrario
      */
     public boolean areAllCardsDealed(){
         return allCardsDealed;
     }
     
     /**
      * Determina si es el turno del jugador 'player' 
      * @param player un jugador
      * @return True si el turno es del jugador 'player' y False, en caso contrario
      */
     public boolean isMyturn(Player player){
         return turno.isMyturn(player);
     }
     
     /**
      * Pasa al siguiente turno
      */
     public void next(){
         turno.next();
     }
 }
