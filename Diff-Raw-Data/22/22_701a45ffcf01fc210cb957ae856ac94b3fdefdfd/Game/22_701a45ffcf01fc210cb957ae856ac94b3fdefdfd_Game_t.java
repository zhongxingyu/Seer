 import java.util.LinkedList;
 public class Game{
 	private LinkedList<Karte> sentence;
 	private Karte currentCard;
 	private int drawnCards, deletedCards;
 
 	public Game(){
 	}
 
 	public void debugFillSentence(){
 		for(int i=0; i<10; i++){
 			sentence.add(new Karte());
 		}
 	}
 
 	public void showCurrentCard(){
	    System.out.print(currentCard.getWort());
 	}
 
 	public void drawCard(){ 
 		currentCard = new Karte();
 	}
 
 	public void putCard(int position){ 
		sentence.add(position, currentCard);	
 	}
 
 	public void deleteCard(int position){
 		sentence.remove(position);
 	}
 
 	public void printInfo(){
 		System.out.println("--------------------------------------------------------------------");
 		System.out.println("                          Word Scrabble");
 		System.out.println("--------------------------------------------------------------------");
		System.out.println("Aktueller Satz: ");
		printSentence();
		System.out.println("Aktuelle Wortanzahl: " + sentence.size());
 		System.out.println("Aktuelle Punktzahl: " + getScore());
		System.out.println("Aktuelle Karte: " + currentCard.getWort());
 	}
 
 	public void printSentence(){
		for (Karte k : sentence) {
			System.out.println(k.getWort());
 		}
 	}
 
 	public int getScore(){
 		int score = 0;
		for (Karte k : sentence) {
 			score += k.getWert();
 		}
 		return score;
 	}
 }
