 public class Game{
 
 public static void main(String args[]){
	
 	int numberOfPlayer = 2;
 
 	System.out.println("Initialisation du paquet de cartes ...");
 	WarDeck deck = new WarDeck("War Card Game Deck");
	deck.shuffle();
 
 	System.out.println("Création des tas de cartes pour chaque joueur et pour la bataille ...");
 	CardStack playerCardStack[] = new CardStack[2];
 	playerCardStack[0] = new CardStack();
 	playerCardStack[1] = new CardStack();
 	CardStack warBattleCardStack = new CardStack();
 
 	System.out.println("Distribution des cartes aux joueurs ...");
 	int k = 0;
 	for (int i=0; i<deck.cardsNumber(); i++){
 		playerCardStack[k].addCard(deck.at(i));
 		k++; if (k==2) k=0;
 	}
 
 	Card card0, card1;
 	System.out.println("Démarrage du jeu ...");
 	while((!playerCardStack[0].isEmpty())&&(!playerCardStack[1].isEmpty()))
 	{
 		card0 = playerCardStack[0].throwFirstCard();
 		card1 = playerCardStack[1].throwFirstCard();
 
 		warBattleCardStack.addCard(card1);
 		warBattleCardStack.addCard(card0);
 
 		System.out.println("Joueur 1 joue: "+ card0);
 		System.out.println("Joueur 2 joue: "+ card1);
 		if (deck.equals(card0, card1)){
 			System.out.println("Egalité.");
 		}
 		else {
 			if (deck.greaterThan(card0, card1))
 			{
 				System.out.println("Joueur 1 gagne la bataille.");
 				while (!warBattleCardStack.isEmpty())
 					playerCardStack[0].addCard(warBattleCardStack.throwFirstCard());
 			}
 			else
 			{
 			System.out.println("Joueur 2 gagne la bataille.");
 				while (!warBattleCardStack.isEmpty())
 					playerCardStack[1].addCard(warBattleCardStack.throwFirstCard());
 
 		}
 	}
 	System.out.println("Le joueur 1 possède "+playerCardStack[0].cardsNumber()+" cartes.");
 	System.out.println("Le joueur 2 possède "+playerCardStack[1].cardsNumber()+" cartes.");

 	}
 
 	if (playerCardStack[0].isEmpty() && playerCardStack[1].isEmpty())
 		System.out.println("Jeu nul.");
 	else {
 		if (playerCardStack[0].isEmpty())
 			System.out.println("Le joueur 2 a gagné la partie.");
 		else
 			System.out.println("Le joueur 1 a gagné la partie.");}
 }
 }
