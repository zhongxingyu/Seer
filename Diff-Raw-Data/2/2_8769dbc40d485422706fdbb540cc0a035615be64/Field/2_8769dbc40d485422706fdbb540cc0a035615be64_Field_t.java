 package ucbang.gui;
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 import ucbang.core.Card;
 import ucbang.core.Player;
 import ucbang.core.Deck.CardName;
 
 import ucbang.network.Client;
 
 public class Field implements MouseListener, MouseMotionListener{
 	Client client;
 	public BSHashMap<Card, cardSpace> cards = new BSHashMap<Card, cardSpace>();
 	CardDisplayer cd;
         Point pointOnCard;
         cardSpace movingCard;
         Card clicked;
         ArrayList<Card> pick;
         
 	public Field(CardDisplayer cd, Client c) {
 		this.cd=cd;
                 client = c;
 	}
 	public void add(Card card, int x, int y){
 		cards.put(card, new cardSpace(card, new Rectangle(x,y,55,85)));
 	}
 	public void paint(Graphics2D graphics){
 		Iterator<cardSpace> iter = cards.values().iterator();
 		while(iter.hasNext()){
 			cardSpace temp= iter.next();
 			cd.paint(temp.card.name ,graphics, temp.rect.x, temp.rect.y);
 		}
 	}
         public cardSpace binarySearchCardAtPoint(Point ep){
             //bsearch method
              int start;
              int end;
 
              ArrayList<cardSpace> al = cards.values(); //search the values arrayList for...
 
 
              int a = 0, b = al.size(), index = al.size() / 2;
 
              while (a != b) {
                  if (ep.y > al.get(index).rect.y + 85) { // the "start" is the value of the card whose bottom is closest to the cursor (and on the cursor)
                      a = index + 1;
                  } else {
                      b = index;
                  }
                  index = a + (b - a) / 2;
              }
              start = a;
              a = 0;
              b = al.size();
              index = al.size() / 2;
              while (a != b) {
                  if (ep.y > al.get(index).rect.y) { // the "end" is the value of the card whose top is closest to the cursor (and on the cursor)
                      a = index + 1;
                  } else {
                      b = index;
                  }
                  index = a + (b - a) / 2;
              }
              end = a - 1;
              for (int n = end; n>= start; n--) {
                  cardSpace s = al.get(n);
                  if (s.rect.contains(ep.x, ep.y)) {
                      return al.get(n);
                  }
              }
              return null;
         }
         public void clear(){
             Point pointOnCard = null;
             cardSpace movingCard = null;
             cards.clear();
         }
         
 	public void mouseClicked(MouseEvent e) {
 		Point ep=e.getPoint();
                 cardSpace cs = binarySearchCardAtPoint(ep);
                 if(cs != null)
                     System.out.println("Clicked on "+cs.card.name);
                else
                    return;
                 if(client.prompting && pick.contains(cs.card)){
                     System.out.println("sending prompt...");
                     if(cs.card.type==1){
                         client.outMsgs.add("Prompt:"+ pick.indexOf(cs.card));
                         client.player.hand.clear(); //you just picked a character card
                         clear();
                     }
                     else{
                         client.outMsgs.add("Prompt:"+ pick.indexOf(cs.card));
                     }
                     client.prompting = false;
                 }
                 else{ //TODO: debug stuff
                     if(client.prompting){
                         System.out.println("i was prompting");
                         if(!client.player.hand.contains(cs.card)){
                             System.out.println("but the arraylist didn't contain the card i was looking for!");
                             System.out.println(cs.card+" "+client.player.hand);
                         }
                     }
                 }
 	}
 	public void mouseEntered(MouseEvent e) {
 	}
 	public void mouseExited(MouseEvent e) {
 	}
 	public void mousePressed(MouseEvent e) {
 	    movingCard = binarySearchCardAtPoint(e.getPoint());
             if(movingCard!=null){
                 pointOnCard = new Point(e.getPoint().x-movingCard.rect.x, e.getPoint().y-movingCard.rect.y);
                 //System.out.println("picked up card");
             }
 	}
 	public void mouseReleased(MouseEvent e) {
             if(movingCard!=null){
                 //System.out.println("card dropped");
             }
             movingCard = null;
 	}
 
         public void mouseDragged(MouseEvent e) {
             //System.out.println("dragging");
             if(movingCard!=null){
                 movingCard.rect.setLocation(Math.max(0, Math.min(e.getPoint().x-pointOnCard.x,745)),Math.max(0, Math.min(e.getPoint().y-pointOnCard.y,515))); //replace boundaries with width()/height() of frame?
             }
             else{
                 //System.out.println("not dragging");
             }
         }
 
         public void mouseMoved(MouseEvent e) {
         }
 
     public class BSHashMap<K,V> extends HashMap<K,V>{
         ArrayList<V> occupied = new ArrayList<V>();
         
         public V put(K key, V value){
             occupied.add(value);
             return super.put(key, value);
         }
         
         public ArrayList<V> values(){
             ArrayList<V> al = new ArrayList<V>();
             Collections.sort(occupied, new Comparator(){
                         public int compare(Object o1, Object o2) {
                             return ((Comparable)o1).compareTo(o2);
                         }
                     });
             al.addAll(occupied);
             return al;
         }
         public void clear(){
             occupied.clear();
             super.clear();
         }
         public V remove(Object o){
             occupied.remove(get(o));
             V oo = super.remove(o);
             return oo;
         }
     }
         
         /*
          * Contains a card and a rectangle
          */
     private class cardSpace implements Comparable{
         public Rectangle rect;
         public Card card;
         public int location; //position of card on field or in hand
         
         public cardSpace(Card c, Rectangle r){
             card = c;
             rect = r;
         }
         
         public int compareTo(Object o) {
             if(((cardSpace)o).rect.getLocation().y!=rect.getLocation().y)
                 return ((Integer)rect.getLocation().y).compareTo(((cardSpace)o).rect.getLocation().y);
             else
                 return ((Integer)rect.getLocation().x).compareTo(((cardSpace)o).rect.getLocation().x);
         }
     }
 }
