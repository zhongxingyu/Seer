 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package practiceUno;
 
 //~--- JDK imports ------------------------------------------------------------
 import java.io.ByteArrayInputStream;
 import java.text.MessageFormat;
 
 import java.util.Scanner;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 //~--- classes ----------------------------------------------------------------
 /**
  *
  * @author mike
  */
 public class Robot extends Player {
 
     /**
      * Field description
      */
     private static final Logger log = Logger.getLogger(Robot.class.getName());
     /**
      * Field description
      */
     private Card.cardColor[] colorValues = Card.cardColor.values();
     /**
      * Field description
      */
     private Card playingCard = null;
     private Stack<Card> wildSet = new Stack<Card>(); 
 
     /**
      *
      * @param name
      * @param pos
      */
     public Robot(String name, int pos) {
         super.name = name;
         super.playerPos = pos;
     }
 
     /**
      * Method description
      *
      *
      * @param c
      *
      * @return
      */
    public int FindCard(Card c) {
         return hand.indexOf(c);
     }
 
     /**
      * Method description
      *
      *
      * @param dex
      *
      * @return
      */
     @Override
     public Card Discard(int dex) {
         Card retC;
 
         if (dex == -1) {
             retC = null;
         } else {
             retC = hand.get(dex);
         }
 
         return retC;
     }
 
     /**
      *
      * @param d
      * @return
      */
     public boolean PlayAHand(Deck d) {
         log.entering("Play a hand", name);
 
         boolean done = false;
         boolean tried = false;
 
         log.info("Trying to decide");
 
         int state = Decide(d.TopCard());
 
         if (state == 1) {
             log.info(String.format("Decided %s %s", state, playingCard.toString()));
         } else {
             log.info(String.format("decided %s", state));
         }
 
         while (!done) {
             switch (state) {
                 case 1:
                     log.info(String.format("Trying to play a card %s %s", playingCard.toString(), d.TopCard().toString()));
 
                     if (playingCard.getClass().equals(WildCard.class))
                     {
                         state = 4;
                     }
                     else
                     {
                         d.AddDiscard(playingCard, this, null);
                         hand.remove(FindCard(playingCard));
                         state = 5;
                     }
                     break;
 
                 case 2:
                     log.info("Need to draw a card");
                     super.GetCard(d.DrawNext());
                     state = Decide(d.TopCard());
                     tried = true;
 
                     if (state == 2) {
                         state = 3;
                     }
 
                     break;
 
                 case 3:
                     log.info("Pass");
                     state = 5;
 
                     break;
 
                 case 4:
                     log.info("Time for a wild card. Choosing yellow");

                     ByteArrayInputStream in = new ByteArrayInputStream("YELLOW".getBytes());
                     System.setIn(in);
                     d.AddDiscard(playingCard, this, new Scanner(System.in));
                     hand.remove(FindCard(playingCard));
                     state = 5;
 
                     break;
 
                 case 5:
                     log.info("End turn");
                     done = true;
                     playingCard = null; 
                     break;
 
                 default:
                     log.severe("I don't know what to do");
             }
         }
 
         log.exiting("Play a Hand", name);
 
         return done;
     }
 
     /**
      * Method description
      *
      *
      * @param c
      *
      * @return
      */
     private int Decide(Card c) {
         log.entering("Decide", name);
         log.info("Entering robot decide function");
 
         int choice;
         Card discard = c;  
 
         log.info(String.format("I have %s cards. ", super.TotalCards()));
         for (int i = 0; i < hand.size(); i++)
         {
             log.info(String.format("Card %s: %s", i, hand.get(i).toString())); 
         }
 
                 if(discard.getClass().equals(NumberCard.class))
                 {
                     log.info(String.format("The top card was a number %s", discard.toString()));
                     NumberCard n = (NumberCard)discard; 
                      
                     for(Card inPlay : hand)
                     {
                         
                         if(!inPlay.getClass().equals(WildCard.class) && inPlay.GetColor().equals(discard.GetColor()))
                         {
                             playingCard = inPlay; 
                             break;
                         }
                         else if(inPlay.getClass().equals(NumberCard.class))
                         {
                             NumberCard play = (NumberCard)inPlay;
                             if(play.GetNumber() == n.GetNumber())
                             {
                                 playingCard = inPlay; 
                                 break;
                             }
                         }                        
                     }
                 }
                 else if(discard.getClass().equals(SpecialCard.class))
                 {
                     log.info(MessageFormat.format("The top card was a special card. {0}", discard.toString()));
                     SpecialCard sp = (SpecialCard)discard; 
                     
                     for(Card inPlay : hand)
                     {
                         log.info(String.format("In special for loop. Card %s", inPlay.toString()));
                         if(!inPlay.getClass().equals(WildCard.class) && inPlay.GetColor().equals(discard.GetColor()))
                         {
                             playingCard = inPlay; 
                             break;
                         }
                         else if(inPlay.getClass().equals(SpecialCard.class))
                         {
                             SpecialCard play = (SpecialCard)inPlay;
                             if(play.GetSpecial().equals(sp.GetSpecial()))
                             {
                                 playingCard = inPlay; 
                                 break;
                             }
                         }
                     }
                 }
                 else if(discard.getClass().equals(WildCard.class))
                 {
                    log.info("The top card was a wild card " + discard.toString());
                     for(Card inPlay : hand)
                     {
                        if(inPlay.GetColor().equals(discard.GetColor()))
                         {
                             playingCard = inPlay; 
                             break;
                         }
                     }
                 }
                 else
                 {
                     log.severe("This card should never exsist!");
                 }
 
         if(playingCard == null )
             PlayAWild(); 
                 
         if (playingCard != null) {
             choice = 1;
         } else {
             choice = 2;
         }
 
         log.info("Done deciding");
         log.exiting("Decide", name);
 
         return choice;
     }
     
     private void MakeWildDeck()
     {
         for(Card c : hand)
         {
             if(c.getClass().equals(WildCard.class))
             {
                 wildSet.push(c);
             }
         }
     }
     
     private void PlayAWild()
     { 
         MakeWildDeck(); 
         if(playingCard == null && !wildSet.empty() )
         {
             playingCard = wildSet.pop(); 
         }
        //wildSet.clear();
     }
 }
 
 
 
 
