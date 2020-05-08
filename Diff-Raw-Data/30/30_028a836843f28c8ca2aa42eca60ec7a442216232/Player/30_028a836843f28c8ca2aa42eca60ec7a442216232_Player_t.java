 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 
 import java.util.Stack;
 import java.util.Queue;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  * Write a description of class Player here.
  * 
  * @author Felipe I. Anfurrutia
  * @version 29/10/2011
  */
 public class Player  extends Actor implements Accepter
 {
     /** The player's name */
     private String name;
     /** The player's image */
     private GreenfootImage playerImage;
     /** The orientation of the player */
     private int rotation;
     /** The cards in the hands */
     private Queue<Card> cards;
     /** The counter to know how many times the player couldn't put a card on the table */
     private int failures;
     /** The player can only place the first card */
     private boolean onlyFirst = true;
     /** Show the first card completed */
     private boolean showFirstCompleted = false;
     
     /**
      * Create a new player 
      */
     public Player(String name, String imageFilename, int rotation){
         this.name = name;
         playerImage = new GreenfootImage(imageFilename);
         this.rotation = rotation;
         cards = new LinkedList<Card>();
         failures = 0;
     }
     
     /**
      * Adds the player without cards
      */
     public void addedToWorld(World world){
         setImage("images/cards/empty.png");
 
         int expand = 200;
         int width = playerImage.getWidth() + getImage().getWidth() + expand;
         int height = getImage().getHeight();
         GreenfootImage image = new GreenfootImage(width, height);
         setImage(image);
         setRotation(rotation);
         showPlayerInfo();
     }
 
     /**
      * Visualiza el texto del jugador
      */
     private void showPlayerInfo(){
         GreenfootImage image = getImage();
         image.clear();
         image.drawImage(playerImage, 0, 0);
         image.drawImage(new GreenfootImage("images/cards/empty.png"), playerImage.getWidth(), 0);
         image.drawString(name, 0, playerImage.getHeight()+10);
         image.drawString("f:"+failures, 10, playerImage.getHeight()+30);
         image.drawRect( 0, 0, image.getWidth()-1, image.getHeight()-1);
     }
     
     /**
      * Determine whether the player has cards in hand or not
      * @return True if the player has cards on hand and False, otherwise
      */
     public boolean hasCards(){
         //To-DO:
        return cards.size() != 0;
     }
     
     /**
      * Add a card if it's legal.
      * @return True if the card has been added and False if it's been rejected.
      */
     public boolean addCard(Card card)
     {
         //To-DO:
        //comprobamos reglas
        if (true) {
            add(card);
            return true;
        }
         return false;
     }
     
     /**
      * Adds a card on the structure of data and shows on the table
      */
     private void add(Card card){
         //To-DO: aadir carta a la cola
         card.setAccepter(this);
         card.setDraggable(true);
         placeCard(card);
     }
 
     /**
      * Coloca la ltima carta en la mesa adecuando la posicin y rotacin de la carta
      */
     private void placeCard(Card card){
         int x = getX4card(cards.size());
         int y = getY4card(cards.size());
         card.setLocation(x, y);      
         card.setRotation(rotation);
     }
     
     /**
      * Calcula y devuelve la coordenada x para las cartas del jugador
      * @return la coordenada x
      */
     private int getX4card(int i){
         int x = getX();
         if (rotation == 0 )       
             x = x - (getImage().getWidth() / 2) + playerImage.getWidth() + getOffset(i); 
         else if (rotation == 180)
             x = x + (getImage().getWidth() / 2) - playerImage.getWidth() - getOffset(i); 
         return x;
     }
     
     /**
      * Calcula y devuelve la coordenada y para las cartas del jugador
      * @return la coordenada y
      */
     private int getY4card(int i){
         int y = getY();
         if (rotation == 90)
             y = y - (getImage().getWidth()/2) + playerImage.getWidth() + getOffset(i); 
         else if (rotation == 270)
             y = y + (getImage().getWidth()/2) - playerImage.getWidth() - getOffset(i);
         return y;
     }
     
     /**
      * Calcula la diferencia de una carta a otra
      */
     private int getOffset(int i){
         int offset = 20 + 15 * i;
         return offset;
     }
    
     /**
      * Calcula y devuelve el nmero de cartas que tiene el jugador en la mano
      * @return el nmero de cartas que tiene el jugador en la mano
      */
     public int numberOfCards(){
         //To-DO:
         return cards.size();
     }
     
     /**
      * Elimina la carta 'card'
      */
     public void remove(Card card){
         //To-DO: eliminar la carta correspondiente
         cards.remove(card);
         placeAllCards();
     }
 
     /**
      * Determina si puede seleccionar la carta 'card' para ponerla encima de la mesa
      * @return True si puede selecionarla y False, en caso contrario
      */
     public boolean canSelect(Card card){
         //To-DO:
        return cards.peek() == card;
     }
     
     /**
      * Calcula las cartas que estan encima de la carta 'card'
      * @return una lista con las cartas que estan encima de la carta 'card'
      */
     public List<Card> getCardsOn(Card card){
         //To-DO:
         return null;
     
     }
     
     /**
      * Recoloca todas las cartas que tiene el jugador en la mano
      */
     public void placeAllCards(){
         //To-DO:
     }
 
     /**
      * Realiza paso y mueve la carta a otro sitio para el siguiente turno
      */
     public void incrementFailures(Card card){
         //To-DO:
         failures++;
     }
     
     
     /**
      * Coloca la primera carta al final de todas y la muestra completamente
      */
     private void moveFirst2Last(){
         //To-DO:
     }
 
     /**
      * Redibuja todas las cartas de la mano para que la primera carta se vea complentamente
      */
     private void showFirstCompleted(Card card){
         //To-DO:
     }
     
     /**
      * Act - do whatever the Player wants to do. This method is called whenever
      * the 'Act' or 'Run' button gets pressed in the environment.
      */
     public void act() 
     {
         // Add your action code here.
     }    
 }
