 package villebasse.gamelogic;
 
 import java.awt.Color;
 import java.util.Vector;
 
 
 public class VilleBasseEngine
 {
	public static enum EngineState {
 		INITIALIZATION,
 		DECKEMPTY,
 		INGAME,
 		INGAME_PUT_PIECE,
 		INGAME_PLACE_MEEPLE,
 		INGAME_PROFIT,
 		/*
 		put piece
 		place a meeple
 		remove meeples
 		profit
 		*/
 	};
 
 	private Board board;
 	private Deck deck;
 	private EngineState state;
 	private int turn;
 	private Piece curPiece;
 	private Player curPlayer;
 	private Vector<Piece> piecesOnBoard;
 	private Vector<Player> players;
 
 
 	public VilleBasseEngine()
 	{
 		this(new DefaultDeckWithoutCloisters());
 	}
 
 	public VilleBasseEngine(Deck deck)
 	{
 		try {
 			this.board = new Board(deck);
 		} catch (Exception e) {
 			// TODO
 			System.err.println(e);
 		}
 
 		this.deck = deck;
 		this.piecesOnBoard = new Vector<Piece>(deck.size());
 		this.players = new Vector<Player>(6);
 		this.state = EngineState.INITIALIZATION;
 		this.turn = 0;
 	}
 
 	public boolean addPlayer(String name, Color color)
 	{
 		return this.addPlayer(new Player(name, color));
 	}
 
 	public boolean addPlayer(Player player)
 	{
 		if (this.state != EngineState.INITIALIZATION || player == null)
 			return false;
 
 		this.players.add(player);
 		return true;
 	}
 
 	public Board getBoard()
 	{
 		return this.board;
 	}
 
 	public Deck getDeck()
 	{
 		return this.deck;
 	}
 
 	public Piece getCurrentPiece()
 	{
 		return this.curPiece;
 	}
 
 	public int getRoundNumber()
 	{
 		if (this.turn == 0)
 			return 0;
 		return (this.turn - 1) / this.players.size() + 1;
 	}
 
	public EngineState getState()
	{
		return this.state;
	}

 	public int getTurnNumber()
 	{
 		return this.turn;
 	}
 
 	public boolean nextTurn()
 	{
 		//if (this.state != INGAME)
		if (this.turn > 0 && this.state.ordinal() <= EngineState.INGAME_PUT_PIECE.ordinal())
 			return false;
 
 		try {
 			this.curPiece = this.deck.draw();
 		} catch (Exception e) { // deck empty
 			System.err.println(e);
 			this.curPiece = null;
 			this.state = EngineState.DECKEMPTY;
 			return false;
 		}
 
 		this.curPlayer = this.players.get(this.turn % this.players.size());
 		this.turn++;
 		this.state = EngineState.INGAME_PUT_PIECE;
 
 		return true;
 	}
 
 	public boolean placeMeeple(double x, double y)
 	{
 		if (this.state != EngineState.INGAME_PLACE_MEEPLE || this.curPiece == null)
 			return false;
 
 		Meeple m = this.curPlayer.takeMeeple();
 		if (m == null) // should throw exception
 			return false;
 
 		this.curPiece.placeMeeple(x, y, m);
 		this.state = EngineState.INGAME_PROFIT;
 
 		return true;
 	}
 
 	public boolean putPiece(int x, int y)
 	{
 		if (this.state != EngineState.INGAME_PUT_PIECE || this.curPiece == null)
 			return false;
 
 		try {
 			this.board.putPieceRelative(x, y, this.curPiece);
 		} catch (Exception e) {
 			System.err.println(e);
 			return false;
 		}
 
 		this.piecesOnBoard.add(this.curPiece);
 		this.state = EngineState.INGAME_PLACE_MEEPLE;
 
 		return true;
 	}
 
 	/*
 	public boolean placePieceAbsolute(int x, int y) throws Exception
 	{
 		if (this.piece == null)
 			return false;
 
 		this.board.putPieceAbsolute(int x, int y, this.curPiece);
 		return this.putPiece();
 	}
 
 	public boolean placePieceRelative(int x, int y) throws Exception
 	{
 		if (this.piece == null)
 			return false;
 
 		this.board.putPieceRelative(int x, int y, this.curPiece);
 		return this.putPiece();
 	}
 	*/
 
 	public int piecesLeft()
 	{
 		return this.deck.size();
 	}
 
 	public boolean removeMeeple(int pieceX, int pieceY, double meepleX, double meepleY, int points)
 	{
 		if (this.state.ordinal() < EngineState.INGAME_PLACE_MEEPLE.ordinal())
 			return false;
 
 		Piece piece = this.board.getPieceRelative(pieceX, pieceY);
 		if (piece == null)
 			return false;
 
 		Meeple meeple = piece.removeMeeple(meepleX, meepleY);
 		if (meeple == null)
 			return false;
 
 		meeple.getPlayer().returnMeeple(points);
 
 		return true;
 	}
 
 	public boolean replaceCurrentPiece()
 	{
 		if (this.state != EngineState.INGAME_PUT_PIECE)
 			return false;
 
 		Piece newPiece = null;
 		try {
 			newPiece = this.deck.draw();
 		} catch (Exception e) {
 			// deck empty
 			System.err.println(e);
 			this.state = EngineState.DECKEMPTY;
 			this.curPiece = null;
 			return false;
 		}
 
 		this.deck.putBack(this.curPiece);
 		this.curPiece = newPiece;
 
 		return true;
 	}
 
 	public boolean startGame()
 	{
 		if (this.state != EngineState.INITIALIZATION || this.players.size() == 0)
 			return false;
 
 		this.state = EngineState.INGAME;
 		this.nextTurn();
 		return true;
 	}
 
 
 	/*
 	private boolean putPiece()
 	{
 		this.piecesOnBoard.add(this.piece);
 		//this.piece = null;
 		this.state = INGAME_PLACE_MEEPLE;
 
 		return true;
 	}
 	*/
 
 	/*
 	private class LoopingIterator<E> extends ListIterator<E>
 	{
 		private List<E> list;
 		private int pos, size;
 
 
 		public LoopingIterator(List<E> list)
 		{
 			this.list = list;
 			this.pos = 0;
 
 			if (list != null)
 				this.size = list.size();
 		}
 
 		public void add(E e) {}
 		public void remove() {}
 		public void set(E e) {}
 
 		public boolean hasNext()
 		{
 			return this.list != null;
 		}
 
 		public boolean hasPrevious()
 		{
 			return this.list != null;
 		}
 
 		public E next()
 		{
 			if (!this.hasNext())
 				return null;
 
 			this.pos = (this.pos + 1) % this.size;
 			return this.list.get(this.previousIndex());
 		}
 
 		public int nextIndex()
 		{
 			return this.pos;
 		}
 
 		public E previous()
 		{
 			if (!this.hasPrevious())
 				return null;
 
 			this.pos = this.previousIndex();
 			return this.list.get(this.pos);
 		}
 
 		public previousIndex()
 		{
 			return (this.pos - 1 + this.size) % this.size;
 		}
 	}
 	*/
 }
