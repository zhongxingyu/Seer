 package pope.alphaBeta;
 
 import java.util.Hashtable;
 
 import pope.interfaces.IHeuristics;
 import fi.zem.aiarch.game.hierarchy.Board;
 import fi.zem.aiarch.game.hierarchy.Coord;
 import fi.zem.aiarch.game.hierarchy.Engine;
 import fi.zem.aiarch.game.hierarchy.Side;
 import fi.zem.aiarch.game.hierarchy.Situation;
 import fi.zem.aiarch.game.hierarchy.Board.Square;
 
 public class SimpleHeuristics implements IHeuristics {
 	
 	private Hashtable<WeigthOwner,Hashtable<WeightNames, Integer>> weights;
 	
 	public Side sideOfAI;
 	
 	private Board board;
 	private Engine engine;
 	
 	private int maxPiece;
 	private Coord desiredPosition;
 	
 	public SimpleHeuristics() {
 		weights = new Hashtable<WeigthOwner, Hashtable<WeightNames,Integer>>();
 	}
 
 	@Override
 	public void setSide(Side side) {
 		sideOfAI = side;
 	}
 	
 	@Override
 	public void setMode(Mode aggressive) {
 		// TODO Auto-generated method stub		
 	}
 		
 	@Override
 	public void setEngine(Engine engine) {
 		this.engine = engine;
 		this.maxPiece = engine.getMaxPiece();		
 	}
 	
 	public void init() {
 		if (engine.makeInitial().getBoard().get(0,0).getSide() == sideOfAI)
 		{
 			desiredPosition = engine.makeCoord(0, 0);			
 		}
 		else
 		{
 			desiredPosition = engine.makeCoord(engine.getBoardWidth(), engine.getBoardWidth());
 		}
 	}
 	
 	@Override
 	public void setWeights(WeigthOwner side, Hashtable<WeightNames, Integer> weigths) {
 		if (weigths == null)
 		{			
 			Hashtable<WeightNames, Integer> tmp = new Hashtable<WeightNames, Integer>();
 			tmp.put(WeightNames.positionWeight, 1);
 			tmp.put(WeightNames.firepowerWeight, 1);
 			tmp.put(WeightNames.rankWeight, 1);
 			this.weights.put(side, tmp);
 		}
 		else
 		{
 			this.weights.put(side, weigths);
 		}
 	}
 
 	@Override
 	public Integer evaluateFinnishedGame(Situation state) 
 	{
 		Integer util;
 		if (state.getWinner() == sideOfAI)
 		{
 			util = Integer.MAX_VALUE;
 		}
 		else if (state.getWinner() == sideOfAI.opposite())
 		{
 			util = Integer.MIN_VALUE;
 		}
 		else
 		{
 			util = 0; // draw.
 		}
 		return util;
 	}
 	
 	/**
 	 * Utility evaluator of Incomplete game. Value is positioned to player in turn.
 	 * 
 	 * @param state
 	 * @return
 	 */
 	@Override
 	public Integer evaluateIncompleteGame(Situation state) 
 	{
 		Integer value = 0;
 		board = state.getBoard();
 						
 		Iterable<Square> own = state.getBoard().pieces(sideOfAI);
 		Iterable<Square> enemy = state.getBoard().pieces(sideOfAI.opposite());
 
 		//OWN AS POSITIVE
 		value += weightedSum(weights.get(WeigthOwner.own), own, sideOfAI);
 		
 		//CALCULATE OWN HEAD PIECE POSITION (SPRING LIKE)
		value -= weights.get(WeigthOwner.own).get(WeightNames.maxPieceSpring)*postionOfMaxPiece(own);
 		
 		//ENEMY AS NEGATIVE
 		value -= weightedSum(weights.get(WeigthOwner.enemy), enemy, sideOfAI.opposite());
 		
 		return value;
 	}
 	
 	private Integer postionOfMaxPiece(Iterable<Square> own) {		
 		for (Square current : own) {
 			if (current.getPiece().getValue() == maxPiece)
 			{
 				return norm(current.getX(), current.getY(), desiredPosition.getX(), desiredPosition.getY());
 			}
 		}
 		return null;
 	}
 
 	private Integer norm(int x, int y, int x2, int y2) {
 		return (int) (Math.pow(x-x2, 2) + Math.pow(y-y2,2));
 	}
 
 	private int weightedSum(Hashtable<WeightNames, Integer> w, Iterable<Square> enemy, Side side)			
 	{
 			int value = 0;
 			//eval own pieces position 
 			value += w.get(WeightNames.positionWeight) * evalPiecesPosition(enemy, side);//, state.getTurn());
 		
 			//eval own pieces firepower
 			value += w.get(WeightNames.firepowerWeight) * evalPiecesFirePower(enemy, side);
 
 			//eval own pieces rank
 			value += w.get(WeightNames.rankWeight) * evalPiecesRank(enemy);
 						
 			return value;
 	}
 			
 	private Integer evalPiecesRank(Iterable<Square> own) {
 		Integer value = 0;
 		for (Square current : own) {
			int tmp = current.getPiece().getValue();
			value += (tmp > 1)? tmp/2: tmp;
 		}		
 		return value;
 	}
 
 	private Integer evalPiecesFirePower(Iterable<Square> own, Side side) {
 		Integer value = 0;
 		for (Square current : own) {
 			Integer factor = (current.getOwner() == side) ? 0 : 1;
 			value += board.firepower(side, current.getX(), current.getY()) * factor;
 		}		
 		return value;
 	}
 
 	private Integer evalPiecesPosition(Iterable<Square> own, Side side) {
 		Integer value = 0;
 		for (Square current : own) {
 			Integer factor = current.getPiece().getValue();
 			value += (current.getOwner() == side.opposite()) ? -1*factor : 0;
 			value += (current.getOwner() == Side.NONE) ? maxPiece/(2*factor) : 0;
 		}
 		return value;
 	}
 }
