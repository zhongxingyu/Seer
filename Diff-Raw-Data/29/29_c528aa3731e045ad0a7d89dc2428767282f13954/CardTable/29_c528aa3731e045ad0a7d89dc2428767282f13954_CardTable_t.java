 import java.awt.*;
 //import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;      
 import java.lang.reflect.*;       
 import java.awt.event.*;
 /**
  *  This class implements a graphical canvas in which card 
  *  piles are placed.  It will also contain a nested listener class
  *  to respond to and handle mouse events.
  *
  *  The canvas is large enough to contain five rows of cards.
  *  Each row has its associated fixed CardPile.  When initialized,
  *  all the cards are in the top pile and the others are empty.
  *
  *  CardTable should implement the following behavior:
  *  - When the user doubleclicks on a card, that card and all those
  *    on top of it on the pile should be flipped over
  *  - When the user drags a card, that card and all those on top of it
  *    on the pile should be removed from the pile they are on and
  *    follow the mouse around.
  *  - When the user releases the mouse while dragging a pile of cards,
  *    the pile should be inserted into some fixed pile according to
  *    where the mouse was released. 
  *  
  *  @author Zach Arthurs and Pratistha Bhattarai
  *  @version CSC 212, 20 February 2013
  */
 public class CardTable extends JComponent {
     /** Gives the number of piles available */
     public static final int NPILE = 5;
 
     /** gives the width of the canvas */
     public static final int WIDTH = 800;
 
     /** gives the height of the canvas */
     public static final int HEIGHT = 500;
 
     /** Storage for each of the piles available */
     CardPile pile[] = new CardPile[NPILE];
 
     /** Storage for pile that is in motion */
     CardPile movingPile;
 
     /** Records card under last mouse press */
     Card cardUnderMouse;
 
     /** Records index of pile under last mouse press */
     CardPile pileUnderMouse;
 
     /** Initialize a table with a deck of cards in the first slot */
     public CardTable() {
 	pile[0] = new CardPile(Card.newDeck(),2,2);
 	pile[1] = new CardPile(2,102);
 	pile[2] = new CardPile(2,202);
 	pile[3] = new CardPile(2,302);
 	pile[4] = new CardPile(2,402);
 	movingPile = null;
 	
 	/** Add MouseListener and MouseMotionListener to GUI */
 	this.addMouseListener(new ClickListener());
 	this.addMouseListener(new DraggedListener());
 	this.addMouseMotionListener(new DraggedListener());
     }
 
     /**
      *  Returns the requested card pile
      *
      *  @param i  The index of the pile requested
      *  @return   The requested pile, or null if the pile is empty
      */
     public CardPile getPile(int i) {
 	CardPile pile;
 	if ((i >= 0)&&(i < NPILE)) {
 	    pile = this.pile[i];
 	} else {
 	    pile = null;
 	}
 	return pile;
     }
 
     /**
      *  Attaches the specified cards to the specified pile.
      *  The location of the pile is set to a fixed location.
      *
      *  @param i  ID of the pile to use
      *  @param pile  Cards to put there
      */
     public void setPile(int i, CardPile pile) {
 	if ((i >= 0)&&(i < NPILE)) {
             pile.setX(2);
             pile.setY(2+100*i);
 	    this.pile[i] = pile;
 	}
     }
 
     /**
      *  Draws the table and the cards upon it
      *
      *  @param g The graphics object to draw into
      */
     public void paintComponent(Graphics g) {
 	g.setColor(Color.green.darker().darker());
 	g.fillRect(0,0,WIDTH,HEIGHT);
 	g.setColor(Color.black);
 	for (int i = 0; i < pile.length; i++) {
 	    g.drawRect(2,2+100*i,72,96);
 	    pile[i].draw(g);
 	}
 	if (movingPile != null) {
 	    movingPile.draw(g);
 	}
     }
 
     /**
      *  The component will look bad if it is sized smaller than this
      *
      *  @return The minimum dimension
      */
     public Dimension getMinimumSize() {
 	return new Dimension(WIDTH,HEIGHT);
     }
 
     /**
      *  The component will look best at this size
      *
      *  @return The preferred dimension
      */
     public Dimension getPreferredSize() {
 	return new Dimension(WIDTH,HEIGHT);
     }
 
     /**
      *  For debugging.  Runs validation tests on all piles.
      */
     public void validatePiles() {
 	for (int i = 0; i < NPILE; i++) {
 	    System.out.print("Pile "+i+":  ");
             System.out.print("Loction:  ("+pile[i].getX()+","+
                              pile[i].getY()+");  Length:  ");
             System.out.print(pile[i].size()+";  Status:  ");
             System.out.println("Valid.");
 	}
 	System.out.print("Moving pile:  ");
         System.out.print("Location:  ("+movingPile.getX()+","+
                          movingPile.getY()+");  Length:  ");
         System.out.print(movingPile.size()+";  Status:  ");
         System.out.println("Valid.");
     }
 
     /**
      *  Locates the pile clicked on, if any.
      *
      *  @param x,y  Coordinates of mouse click
      *  @return  CardPile  holding clicked card
      */
     private CardPile locatePile(int x, int y) {
         int index = y/100;
         if (index < 0) {
             index = 0;
         } else if (index>=NPILE) {
             index = NPILE-1;
         }
 	return pile[index];
     }
 
     /**
      *  Locates the card clicked on, if any.
      *
      *  @param x,y  Coordinates of mouse click
      *  @return  Card  holding clicked card
      */
     public Card locateCard(int x, int y) {
 	return locatePile(x,y).locateCard(x,y);
     }
     
 	/** Event handler for mouse double click on game window */
     class ClickListener extends MouseAdapter {
 		/** Flips over the cards */
 		public void mouseClicked(MouseEvent e)  {
 			if (e.getClickCount() == 2) {
 				pileUnderMouse = locatePile((int)e.getX(), (int)e.getY());
 			    cardUnderMouse = locateCard((int)e.getX(), (int)e.getY());
 				ListIterator<Card> i;
 			    i = pileUnderMouse.listIterator(pileUnderMouse.size());
 				if (cardUnderMouse != null) {
 					Card n = i.previous();
 					n.flipCard();
 				    while (n != cardUnderMouse){
 					    n = i.previous();
 						n.flipCard();
 					}
 				} else {
 					for (i = pileUnderMouse.listIterator(); i.hasNext();){
 						Card n = i.next();
 						n.flipCard();
 					}	
 				}
 			    repaint();
 			} else {
 				
 			}
 		}
 			    
     }
 	
 	/** Event handler for dragging in game window */
 	class DraggedListener extends MouseAdapter {
 		/** starting mouse drag x coordinate */
 		private double firstX;
 
 		/** starting mouse drag y coordinate */
 		private double firstY;
 		
 		/** Gets the location of first click and the pile and card under the mouse */
 		public void mousePressed(MouseEvent e) {
 			pileUnderMouse = locatePile((int)e.getX(), (int)e.getY());
 		    cardUnderMouse = locateCard((int)e.getX(), (int)e.getY());
 			firstX = e.getX();
 			firstY = e.getY();
 		}
 		
 		/** Drags the cards across the screen */
		int count = 0;
 		public void mouseDragged(MouseEvent e) {
			if (pileUnderMouse.size() != 0 | count == 0) {	
				if (movingPile == null)	{
					if (cardUnderMouse == null) {
						movingPile = new CardPile((int)firstX, (int)firstY);
						movingPile.addAll(pileUnderMouse.split(pileUnderMouse.get(0)));
					} else {
						movingPile = new CardPile((int)firstX, (int)firstY);
						movingPile.addAll(pileUnderMouse.split(cardUnderMouse));
					}
					count += 1;
				}	
 			double secondX = e.getX();
 			double secondY = e.getY();
 			double deltaX = secondX - firstX;
 			double deltaY = secondY - firstY;
 			movingPile.setX((int)movingPile.getX() + (int)deltaX);
 			movingPile.setY((int)movingPile.getY() + (int)deltaY);
 			firstX = secondX;
 			firstY = secondY;
 			repaint();
			}	
 		}
 		
 		/** Releases the cards into the correct location */
 		public void mouseReleased(MouseEvent e) {
 			double releasedX = e.getX();
 			double releasedY = e.getY();
 			CardPile releasedPile = locatePile((int)releasedX, (int)releasedY);
 			Card releasedCard = locateCard((int)releasedX, (int)releasedY);
 			if (movingPile != null) {	
 				if (releasedCard == null){
 					releasedPile.addAll(movingPile);
 					movingPile = null;
					count = 0;
 					} else {
 					releasedPile.insertAfter(movingPile, releasedCard);
 					movingPile = null;
					count = 0;
 				}
 				repaint();
 			}
 		}
 	}
 
 }
 
 
