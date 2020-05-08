 /**
  * Sophia Anopa/Alex Yang
  * APCSSec01Yl12
  * Blackjack: Game
  * Java 1.7, MacOSX10.8
  * May 14-21, 2013
  */
 import java.util.*;
 public class Game
 {
     private ArrayList<Player> players;
     private int pnum;
     private ArrayList<Card> deck;
     
     /** Default Constructor **/
     public Game() {
         Scanner reader = new Scanner(System.in);
         System.out.print("Enter number of players: ");
         pnum = reader.nextInt();
         reader.close();
         deck = new ArrayList<Card>();
         players = new ArrayList<Player>();
         createDeck();
         createPlayers();
     }
     
     /** Accessors **/
     public String showResult() {
         String winner = "\n\n--------------=Results=----------------\n";
         int dev = 21;
         ArrayList<Player> winners = new ArrayList<Player>();
         for (Player p: players) {
             winner += p.getName() + " " + p.getValue() + "\n";
             if (p.getValue() == 21) {
                 winners.add(p);
             }
             else if (Math.abs(p.getValue() - 21) < dev && p.getValue() < 21) {
                 dev = Math.abs(p.getValue() - 21);
             }
             else if (Math.abs(p.getValue() - 21) < dev) {
                 dev = Math.abs(p.getValue() - 21);
             }
         }
         if (winners.size() == 0) {
             for (Player p: players) {
                if (p.getValue() == dev) {
                     winners.add(p);
                 }
             }
         }
         winner += "\n\nThe winner(s):\n";
         for (int i = 0; i < winners.size(); i++) {
             winner += winners.get(i).toString();
         }
         return winner;
     }
     
     /** Other methods **/
     public void play() {
         Scanner reader = new Scanner(System.in);
         int stands = 0;
         while (stands < pnum-1) {
             stands = 0;
             for (Player p : players) {
                 if (p.getStand()) {
                     stands++;
                 } else if (p instanceof Dealer) {
                     p.draw(deck);
                 } else {
                     System.out.println("--------------=Next Player=----------------");
                     System.out.println(p);
                     System.out.print("Draw (1) or Stand (2)? ");
                     int choice = checkChoice.checkValidity(reader.next());
                     while (choice == -1 || (choice != 1 && choice != 2)) {
                         System.out.println("Please enter a valid number.");
                         System.out.print("Draw (1) or Stand (2)? ");
                         choice = checkChoice.checkValidity(reader.next());
                     }
                     if (choice == 1) {
                         p.draw(deck);
                         System.out.println("\nNew Hand");
                         System.out.println(p);
                     } else {
                         p.stand();
                     }
                 }
             }
         }
         System.out.println(showResult());
         reader.close();
     }
     
     public void createDeck() {
         for (Card.Suit suit : Card.Suit.values()) {
             for (Card.Rank rank : Card.Rank.values())
                 deck.add(new Card(rank, suit));
         }
     }
     
     public void createPlayers() {
         Scanner reader = new Scanner(System.in);
         for (int i = 1; i <= pnum; i++) {
             System.out.print("Please enter Player " + i + "'s name: ");
             String name = reader.next();
             Player p = new Player(name);
             p.draw(deck);
             p.draw(deck);
             players.add(p);
         }
         reader.close();
         Dealer d = new Dealer();    
         d.draw(deck);
         d.draw(deck);
         players.add(d);
         System.out.println("\n-------------------------------------------");
     }
 }
