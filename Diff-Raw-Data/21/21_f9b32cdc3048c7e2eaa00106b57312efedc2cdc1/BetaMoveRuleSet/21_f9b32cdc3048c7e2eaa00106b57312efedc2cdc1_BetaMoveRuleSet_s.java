 /*******************************************************************************
  * This files was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package strategy.game.version.beta.move;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import strategy.common.PlayerColor;
 import strategy.common.StrategyException;
 import strategy.game.common.Location;
 import strategy.game.common.MoveResult;
 import strategy.game.common.MoveResultStatus;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 import strategy.game.version.combat.CombatResult;
 import strategy.game.version.combat.CombatRuleSet;
 import strategy.game.version.move.MoveRuleSet;
 
 /**
  * Constructor for BetaMoveRuleSet
  * @author rjsmieja, jrspicola
  * @version Sept 21, 2013
  */
 public class BetaMoveRuleSet implements MoveRuleSet {
 
 	private static final int MAX_TOTAL_MOVES = 12;
 	private static final int MAX_MOVE_DISTANCE = 1;
 
 	final private Collection<PieceType> unmovablePieces;
 	final private Collection<PieceType> validPieceTypes;
 
 	final private CombatRuleSet combatRules;
 	
 	private int moveCount;
 	private PlayerColor turnPlayer;
 
 	/**
 	 * Constructor for BetaMoveRuleSet
 	 * @param combatRules ComatRuleSet to follow
 	 */
 	public BetaMoveRuleSet(CombatRuleSet combatRules){
 		unmovablePieces = new ArrayList<PieceType>();
 		unmovablePieces.add(PieceType.FLAG);
 
 		validPieceTypes = new ArrayList<PieceType>();
 		validPieceTypes.add(PieceType.MARSHAL);
 		validPieceTypes.add(PieceType.COLONEL);
 		validPieceTypes.add(PieceType.CAPTAIN);
 		validPieceTypes.add(PieceType.LIEUTENANT);
 		validPieceTypes.add(PieceType.SERGEANT);
 		validPieceTypes.add(PieceType.FLAG);
 
 		this.combatRules = combatRules;
 		
 		moveCount = 0;
 		turnPlayer = PlayerColor.RED;
 	}
 
 	@Override
 	public void reset(){
 		moveCount = 0;
 		turnPlayer = PlayerColor.RED;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public MoveResult doMove(PieceType movingType, PieceLocationDescriptor movingPiece, PieceLocationDescriptor targetPiece, Collection<PieceLocationDescriptor> firstConfiguration, Collection<PieceLocationDescriptor> secondConfiguration) throws StrategyException
 	{
		if(unmovablePieces.contains(movingPiece.getPiece().getType())){
			throw new StrategyException("Specified piece type of <" + movingPiece.getPiece().getType() + "> is not allowed to move");
		}

 		if(movingPiece.getPiece() == null){
 			throw new StrategyException("No piece at location");
 		} 
 
 		final Location from = movingPiece.getLocation();
 		final Location to = targetPiece.getLocation();
 		final int distanceToMove = to.distanceTo(from);
 		final PlayerColor pieceOwner = movingPiece.getPiece().getOwner();
 
 		//		final Piece movingPiece = getPieceAt(from);
 		//		final Piece targetPiece = getPieceAt(to);
 
 		if (movingPiece.getPiece().getType() != movingType){
 			throw new StrategyException("Piece type does not match!");
 		}
 
 		if (pieceOwner != turnPlayer){
 			throw new StrategyException("Piece does not belong to the current player's turn!");
 		}
 
 		if (distanceToMove > MAX_MOVE_DISTANCE){
 			throw new StrategyException("Moving too many spaces");
 		}
 
 		if (distanceToMove == 0){
 			throw new StrategyException("Not moving");
 		}
 
 //		boolean canMove = true;
 		
 //		while(canMove){
 //			for(int i = 0; i<=5; i++){
 //				for (int j = 0; j <= 5; j++){
 //					
 //				}
 //			}
 //		}
 		
 		//TODO: Put in board?
 //		boolean breakFlag = false;
 //
 //		for(int i = 0; i<=5;i++){
 //			if(!breakFlag){
 //				for(int j = 0; j<=5; j++){
 //					if(!breakFlag){
 //						if(getPieceAt(new Location2D(i,j)) != null){
 //							if(getPieceAt(new Location2D(i, j)).getOwner() == turnPlayer){
 //								if(getPieceAt(new Location2D(i, j)).getType() != PieceType.FLAG){
 //									breakFlag = true;
 //								}
 //							}
 //						}
 //					}
 //				}
 //			}
 //		}
 //
 //		if(!breakFlag){
 //			return new MoveResult(MoveResultStatus.DRAW, null);
 //			//			gameOver = true;
 //			//			throw new StrategyException("There are no movable pieces for the current player.");
 //		}
 
 		moveCount++;
 
 		//check 'to' spot. if no piece there, make move. else, check piece color.
 		if (targetPiece.getPiece() != null){
 			if (movingPiece.getPiece().getOwner() == targetPiece.getPiece().getOwner()){
 				throw new StrategyException("Attempting to do combat on friendly piece!");
 			}
 			//if color is friend, throw exception. if color is enemy, battle
 			if(movingPiece.getPiece().getType().equals(targetPiece.getPiece().getType())) {
 				//remove both pieces
 				removePiece(movingPiece, firstConfiguration, secondConfiguration);
 				removePiece(targetPiece, firstConfiguration, secondConfiguration);
 			} else {
 				final CombatResult combatResult = combatRules.doCombat(movingPiece, targetPiece);
 				if (combatResult.getLosingPiece().getPiece().getType() == PieceType.FLAG){
 					return new MoveResult(MoveResultStatus.valueOf(combatResult.getWinningPiece().getPiece().getOwner() + "_WINS"), combatResult.getWinningPiece());
 				}
 			}
 		} else {
 			movePiece(movingPiece, to, firstConfiguration, secondConfiguration);
 		}
 
 		if (moveCount >= MAX_TOTAL_MOVES){
			//			gameOver = true;
 			return new MoveResult(MoveResultStatus.DRAW, null);
 		}
 
 		//Next player's turn
 		if (turnPlayer == PlayerColor.RED){
 			turnPlayer = PlayerColor.BLUE;
 		} else {
 			turnPlayer = PlayerColor.RED;
 		}
 
 //		if (winner == PlayerColor.RED){
 //			return new MoveResult(MoveResultStatus.RED_WINS, piece);
 //		} else if (winner == PlayerColor.BLUE) {
 //			return new MoveResult(MoveResultStatus.BLUE_WINS, piece);
 //		}
 
		final PieceLocationDescriptor movedPiece = new PieceLocationDescriptor(movingPiece.getPiece(), to);
 		
		return new MoveResult(MoveResultStatus.OK, movedPiece);
 	}
 
 	/**
 	 * Helper to actually move a piece to an empty location
 	 * @param piece Piece to move
 	 * @param from Location to move piece from
 	 * @param to Location to move piece to
 	 */
 	private void movePiece(PieceLocationDescriptor oldPiece, Location to, Collection<PieceLocationDescriptor> redConfiguration, Collection<PieceLocationDescriptor> blueConfiguration){
		//	private void movePiece(Piece movingPiece,Location from,Location to){
		//		final PieceLocationDescriptor oldPiece = new PieceLocationDescriptor(movingPiece, from);
 		final PieceLocationDescriptor newPiece = new PieceLocationDescriptor(oldPiece.getPiece(), to);
 
 		if(oldPiece.getPiece().getOwner() == PlayerColor.RED){
 			redConfiguration.remove(oldPiece);
 			redConfiguration.add(newPiece);
 		}else if (oldPiece.getPiece().getOwner() == PlayerColor.BLUE){
 			blueConfiguration.remove(oldPiece);
 			blueConfiguration.add(newPiece);
 		}
 	}
 
 	/**
 	 * Helper to remove piece from the passed in configurations
 	 * @param piece the pieces to remove
 	 * @param configurations configurations remove the piece from
 	 */
 	@SafeVarargs
 	static private void removePiece(PieceLocationDescriptor piece, Collection<PieceLocationDescriptor>... configurations) {
 		for (Collection<PieceLocationDescriptor> configuration: configurations){
 			configuration.remove(piece);
 		}
 	}
 
 	//	/**
 	//	 * Helper to check if moves can be made
 	//	 * @throws StrategyException if a move can not be made
 	//	 */
 	//	private void canMoveCheck() throws StrategyException{
 	//		boolean breakFlag = false;
 	//
 	//		for(int i = 0; i<=5;i++){
 	//			if(!breakFlag){
 	//				for(int j = 0; j<=5; j++){
 	//					if(!breakFlag){
 	//						if(getPieceAt(new Location2D(i,j)) != null){
 	//							if(getPieceAt(new Location2D(i, j)).getOwner() == turnPlayer){
 	//								if(getPieceAt(new Location2D(i, j)).getType() != PieceType.FLAG){
 	//									breakFlag = true;
 	//								}
 	//							}
 	//						}
 	//					}
 	//				}
 	//			}
 	//		}
 	//
 	//		if(!breakFlag){
 	//			return new MoveResult(MoveStatus)
 	////			gameOver = true;
 	////			throw new StrategyException("There are no movable pieces for the current player.");
 	//		}
 	//	}
 }
