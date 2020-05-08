 package strategy.game.version.beta.combat;
 
 import strategy.common.StrategyException;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.version.combat.CombatResult;
 import strategy.game.version.combat.CombatRuleSet;
 
 public class BetaCombatRuleSet implements CombatRuleSet {
 
 	/**
 	 * Calculates the result of combat between two pieces and returns the winning piece
 	 * @param attackingPiece the attacking piece
 	 * @param defendingPiece the piece being attacked
 	 * @return piece that won combat
 	 * @throws StrategyException if an invalid pieceType is not passed in or if a Flag tries to attack
 	 */
 	@Override
 	public CombatResult doCombat(PieceLocationDescriptor attackingPiece, PieceLocationDescriptor defendingPiece) throws StrategyException{
 		switch(attackingPiece.getPiece().getType()) {
 		case MARSHAL:
 			switch(defendingPiece.getPiece().getType()) {
 			case COLONEL:
 				return new CombatResult(attackingPiece, defendingPiece);
 			case CAPTAIN:
 				return new CombatResult(attackingPiece, defendingPiece);
 			case LIEUTENANT:
 				return new CombatResult(attackingPiece, defendingPiece);
 			case SERGEANT:
 				return new CombatResult(attackingPiece, defendingPiece);
 			case FLAG:
 				return new CombatResult(attackingPiece, defendingPiece);
 			default:
 				throw new StrategyException("Piece is not meant for Beta");
 			}
 		case COLONEL:
 			switch(defendingPiece.getPiece().getType()) {
 			case MARSHAL:
 				return new CombatResult(defendingPiece, attackingPiece);
 			case CAPTAIN:
 				return new CombatResult(attackingPiece, defendingPiece);
 			case LIEUTENANT:
 				return new CombatResult(attackingPiece, defendingPiece);
 			case SERGEANT:
 				return new CombatResult(attackingPiece, defendingPiece);
 			case FLAG:
 				return new CombatResult(attackingPiece, defendingPiece);
 			default:
 				throw new StrategyException("Piece is not meant for Beta");
 			}
 		case CAPTAIN:
 			switch(defendingPiece.getPiece().getType()) {
 			case MARSHAL:
 				return new CombatResult(defendingPiece, attackingPiece);
 			case COLONEL:
 				return new CombatResult(defendingPiece, attackingPiece);
 			case LIEUTENANT:
 				return new CombatResult(attackingPiece, defendingPiece);
 			case SERGEANT:
 				return new CombatResult(attackingPiece, defendingPiece);
 			case FLAG:
 				return new CombatResult(attackingPiece, defendingPiece);
 			default:
 				throw new StrategyException("Piece is not meant for Beta");
 			}
 		case LIEUTENANT:
 			switch(defendingPiece.getPiece().getType()) {
 			case MARSHAL:
 				return new CombatResult(defendingPiece, attackingPiece);
 			case COLONEL:
 				return new CombatResult(defendingPiece, attackingPiece);
 			case CAPTAIN:
 				return new CombatResult(defendingPiece, attackingPiece);
 			case SERGEANT:
 				return new CombatResult(attackingPiece, defendingPiece);
 			case FLAG:
				return new CombatResult(defendingPiece, attackingPiece);
 
 			default:
 				throw new StrategyException("Piece is not meant for Beta");
 			}
 		case SERGEANT:
 			switch(defendingPiece.getPiece().getType()) {
 			case MARSHAL:
 				return new CombatResult(defendingPiece, attackingPiece);
 			case COLONEL:
 				return new CombatResult(defendingPiece, attackingPiece);
 			case CAPTAIN:
 				return new CombatResult(defendingPiece, attackingPiece);
 			case LIEUTENANT:
 				return new CombatResult(defendingPiece, attackingPiece);
 			case FLAG:
 				return new CombatResult(attackingPiece, defendingPiece);
 			default:
 				throw new StrategyException("Piece is not meant for Beta");
 			}
 		default:
 			throw new StrategyException("Piece is not meant for Beta");
 		}
 	}
 }
