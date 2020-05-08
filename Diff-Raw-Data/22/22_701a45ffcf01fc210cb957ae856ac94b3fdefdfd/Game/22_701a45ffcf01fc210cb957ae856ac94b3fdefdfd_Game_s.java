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
		return currentCard;
 	}
 
 	public void drawCard(){ 
 		currentCard = new Karte();
 	}
 
 	public void putCard(int position){ 
		sentence.add(position);	
 	}
 
 	public void deleteCard(int position){
 		sentence.remove(position);
 	}
 
 	public void printInfo(){
 		System.out.println("--------------------------------------------------------------------");
 		System.out.println("                          Word Scrabble");
 		System.out.println("--------------------------------------------------------------------");
		System.out.println("Aktueller Satz: " +  printSentence();
		System.out.println("Aktuelle Wortanzahl: " + satz.size());
 		System.out.println("Aktuelle Punktzahl: " + getScore());
		System.out.println("Aktuelle Karte: " showCurrentCard + '\n');
 	}
 
 	public void printSentence(){
		for (Karte k : satz) {
			System.out.print(k.getWort())
 		}
 	}
 
 	public int getScore(){
 		int score = 0;
		for (Karte k : satz) {
 			score += k.getWert();
 		}
 		return score;
 	}
 }
