 import java.util.*;
 public abstract class Deck implements Comparable, Shufflable{
 	private Symbol symbols[];
 	private Card cards [];
 
 	private Hauteurs hauteurs;
 
 	public Hauteurs getHauteurs(){
 		return hauteurs;
 	}
 	public Card[] getCards(){
 		return cards;
 	}
 
 	public Card at(int i){
 		return cards[i];
 	}
 
 	public int cardsNumber(){
 		return cards.length;
 	}
 
 	public String toString(){
 		String cardsContents="";
		for (int i = 0; i < this.cardsNumber(); i++)
 			cardsContents = cardsContents
 							+ ", "
 							+ cards[i].toString();
 		return "Deck(cartes={"
 			+cardsContents
 			+"})";
 	}
 	public void setSymbols(Symbol symbols[]){
 		this.symbols = symbols;}
 
 	public void setHauteurs(Hauteurs hauteurs){
 		this.hauteurs = hauteurs;}
 
 	public void shuffle(){
		Random generator = new Random(10);
		for (int i = 0; i < cards.length; i++) {
 			int randomPosition = generator.nextInt(cards.length);
     		Card temp = cards[i];
     		cards[i] = cards[randomPosition];
     		cards[randomPosition] = temp;
     	}
     }
 
 	public void cut(){
		Random generator = new Random(10);
 		int cutPosition = generator.nextInt(cards.length);
 		Card tmp[] = new Card[cards.length];
		for (int i = cutPosition; i < cards.length; i++)
 			tmp[i-cutPosition] = cards[i];
		for (int i = 0; i < cutPosition; i++)
 			tmp[i-cutPosition+cards.length] = cards[i];
		cards = tmp;
 	}
 
 	public void buildDeck(Hauteurs hauteurs, Symbol symbols[]){
 		this.setHauteurs(hauteurs);
 		this.setSymbols(symbols);
 		int numberOfCards = hauteurs.size()*symbols.length;
 		cards = new Card[numberOfCards];
 		int k = 0;
 		for (int i=0; i< hauteurs.size(); i++)
 			for (int j=0; j < symbols.length; j++){
 				cards[k] = new Card(hauteurs.at(i), symbols[j]);
 				k++;
 			}
 	}	
 }
