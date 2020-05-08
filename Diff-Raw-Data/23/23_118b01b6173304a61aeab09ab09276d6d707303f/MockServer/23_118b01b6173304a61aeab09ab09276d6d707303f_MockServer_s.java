 package server;
 
 import exception.AlreadyConnectedException;
 import exception.EmptyDeckException;
 import exception.GameException;
 import exception.TurnException;
 import game.Bet;
 import game.Game;
 import game.Hand;
 import game.Player;
 import game.Turn;
 import game.card.Card;
 import game.card.Deck;
 import game.enumeration.Suit;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import client.Client;
 
 public class MockServer extends Server {
 
 	final static public int MAX_TURNS = 10;
 	final static public int MAX_PLAYERS = 4;
 	final static public int MIN_BET = 6;
 	final static public int NB_CARDS_NEW_HAND = 6;
 
 
 	private Deck deck;
 	private Client client = null;
 	private Player player = null;
 	private Player[] players = null;
 	private Game game = null;
 	private int nbTurn = 0;
 	private Turn turn = null;
 
 	public MockServer() {
		/*
 		this.game = new Game();
 		this.deck = new Deck();
		*/
 		this.deck.mixCards();
 	}
 
 	public void setClient( Client client ) {
 		this.client = client;
 		this.player = client.getPlayer();
 	}
 
 	private void connectPlayers() throws RemoteException {
 
 		this.players = new Player[]{ this.player, new Player("bot2"), new Player("bot3"), new Player("bot4") };
 
 		try {
 			this.client.connect();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		//Connections
 		this.game.setPlayers( this.players );
 
 		//Start i and 1 to skip the first player since it's us
 		for( int i = 1; i < this.players.length; i++ ) {
 			client.notifyPlayerConnect( this.players[i] );
 		}
 
 	}
 
 	private Bet createHands() throws EmptyDeckException {
 
 		Hand hand = new Hand( deck );
 		Bet bet = this.client.notifyBettingTime( hand );
 
 		System.out.println("SERVER: Player " + this.player.getName() + " has bet " + bet );
 
 		//Bot hands
 		this.players[1].setHand( new Hand( this.deck ) );
 		this.players[2].setHand( new Hand( this.deck ) );
 		this.players[3].setHand( new Hand( this.deck ) );
 
 
 		return bet;
 
 	}
 
 	private void createAndManageBets() throws EmptyDeckException {
 
 		//Client bet
 		Bet bet = this.createHands();
		this.game.setBet( bet, this.player );
 
 		//Bot bets		
 		for( int i = 1; i < MockServer.MAX_PLAYERS; i++ ) {
 
 			ArrayList<Bet> botBets = this.game.getPlayableBets( this.players[i] );
 			Bet botBet = null;
 
 			if( botBets.size() > 0 ) {
 				botBet = botBets.get(0);
 			}
 
			this.game.setBet( botBet, this.players[i] );
 			this.client.notifyPlayerBet( this.players[i], botBet );
 
 		}
 
 		Player betWinner = this.game.getBestPlayerBet();
 		Suit gameSuit = this.game.getGameSuit();
 		this.client.notifyBetWinner( betWinner, gameSuit );
 
 		for( Player p : this.players ) {
 			p.getHand().setGameSuit( gameSuit );
 		}
 
 		if( betWinner == this.player ) {
 
 			Card newCards[] = new Card[ MockServer.NB_CARDS_NEW_HAND ];
 
 			for( int i = 0; i < 6; i++ ) {
 				newCards[i] = deck.takeCard();
 			}
 
 			this.client.notifyChangeCardsAfterBet( newCards );
 		}
 
 	}
 
 	private void showWinner() throws RemoteException {
 
 		int indexPlayer = 0;
 		for(int i = 0; i < this.players.length; ++i) {
 			if(players[i].equals(this.game.getBestPlayerBet())) {
 				indexPlayer = i;
 				break;
 			}
 		}
 
 		int otherTeammate1;
 		int otherTeammate2;
 
 		int teammate = 0;
 		if(indexPlayer == 0 || indexPlayer == 2) {
 			indexPlayer = 0;
 			teammate = 2;
 			otherTeammate1 = 1;
 			otherTeammate2 = 3;
 		}
 		else { // if(indexPlayer == 1 || indexPlayer == 3) {
 			indexPlayer = 1;
 			teammate = 3;
 			otherTeammate1 = 0;
 			otherTeammate2 = 2;
 		}
 
 		if(this.players[indexPlayer].getTurnWin() + this.players[teammate].getTurnWin() >= this.game.getBestPlayerBet().getOriginalBet().getNbRounds()) {
 			this.client.notifyWinner(this.players[indexPlayer], this.players[teammate]);
 		}
 		else {
 			this.client.notifyWinner( players[otherTeammate1], players[otherTeammate2] );
 		}
 	}
 
 	private Player[] playingOrder( Player winner ) {
 
 		Player[] order = new Player[ MockServer.MAX_PLAYERS ];
 		int orderPos = 1;
 
 		int pos = -1;
 		for( int i = 0; i < this.players.length && pos < 0; i++ ) {
 			if( this.players[i] == winner ) {
 				pos = i;
 			}
 		}
 
 		order[0] = this.players[pos];
 
 		for( int i = pos + 1; i < this.players.length; i++ ) {
 			order[orderPos] = this.players[i];
 			orderPos++;
 		}
 
 		for( int i = 0; i < pos; i++ ) {
 			order[orderPos] = this.players[i];
 			orderPos++;
 		}
 
 		return order;
 
 	}
 
 	public void startGame() throws RemoteException, GameException {
 
 		this.connectPlayers();
 		this.createAndManageBets();
 
 		ArrayList<Player> playingOrder = new ArrayList<Player>(Arrays.asList( players ));
 		this.client.notifyStartNewGame( playingOrder );
 
 		this.turn = new Turn( this.game.getGameSuit() );
 		Player[] turnOrder = this.playingOrder( this.game.getBestPlayerBet() );
 
 		while( this.nbTurn < MockServer.MAX_TURNS ) {
 
 			for( Player currentPlayer : turnOrder ) {
 
 				if( currentPlayer == this.player ) {
 
 					Card card = this.client.notifyYourTurn( this.turn.getTurnSuit() );
 					this.turn.addCard( currentPlayer , card );
 
 				} else {

 					Card botCard = currentPlayer.getHand().getPlayableCard( this.turn.getTurnSuit() ).get(0);				
 					this.turn.addCard( currentPlayer, botCard );
 					currentPlayer.getHand().playCard( botCard );
 					this.client.notifyPlayerTurn( currentPlayer, botCard );
 				}
 
 			}
 
 			Player turnWinner = this.turn.getWinner();
 			this.client.notifyTurnWinner( turnWinner );
 
 			turnOrder = this.playingOrder(  turnWinner );
 			this.turn = new Turn( this.game.getGameSuit() );
 			this.nbTurn++;
 
 
 		}
 
 		this.showWinner();
 
 	}
 
 	@Override
 	public boolean connectClient(Client client, Player player) {
 
 		System.out.println("SERVER: Client with player " + player.getName() + " has connected.");
 		this.player = player;
 		return true;
 
 	}
 
 	@Override
 	public void playCard(Client client, Card card) throws RemoteException {
 
 		System.out.println("SERVER: player " + client.getPlayer().toString() + " has played card " + card.toString() );
 
 		try {
 			this.turn.addCard( client.getPlayer() , card );
 		} catch (TurnException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public void disconnectClient(Client client) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
	public void setBetForPlayer(Client client, Bet bet) {
 	}
 
 	@Override
 	public Player getCurrentPlayer() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public int getScore() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
	public void setNewHandAfterBet(Client client, ArrayList<Card> cards) {
 
 		System.out.println("SERVER: Player " + client.getPlayer().toString() + " has new hand " + cards.toString() );
 		client.getPlayer().getHand().setCards( cards );
 
 	}
 
 }
