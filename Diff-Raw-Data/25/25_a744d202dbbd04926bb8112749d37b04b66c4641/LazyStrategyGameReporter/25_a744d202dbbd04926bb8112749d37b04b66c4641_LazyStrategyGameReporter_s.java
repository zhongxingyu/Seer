 /*******************************************************************************
  * This files was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package strategy.game.reporter;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import strategy.common.StrategyException;
 import strategy.game.common.Location;
 import strategy.game.common.MoveResult;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 import strategy.game.common.StrategyGameObserver;
 
 /** This is a lazy reporter that simply prints what ever it is given
  *  whenever it is notified about events. 
  * 
  * @author Dabrowski
  * @version $Revision: 6.9 $
  */
 public class LazyStrategyGameReporter implements StrategyGameObserver {
 
 	/* (non-Javadoc)
 	 * @see strategy.game.common.StrategyGameObserver#gameStart(java.util.Collection, java.util.Collection)
 	 */
 	@Override
 	public void gameStart(Collection<PieceLocationDescriptor> redConfiguration,
 			Collection<PieceLocationDescriptor> blueConfiguration) {
 		System.out.println("A new game has started!");
 		printConfig(redConfiguration);
 		printConfig(blueConfiguration);
 		System.out.println(" ");
 	}
 
 	/** Prints the given configuration
 	 * @param config the configuration to print
 	 */
 	private void printConfig(
 			Collection<PieceLocationDescriptor> config) {
 		final Iterator<PieceLocationDescriptor> configIter =   config.iterator();
 		while (configIter.hasNext()){
 			PieceLocationDescriptor next = configIter.next();
 			System.out.println("Piece: " + next.getPiece().toString() + "  At: " + next.getLocation().toString());			
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see strategy.game.common.StrategyGameObserver#moveHappened(strategy.game.common.PieceType, strategy.game.common.Location, strategy.game.common.Location, strategy.game.common.MoveResult, strategy.common.StrategyException)
 	 */
 	@Override
 	public void moveHappened(PieceType piece, Location from, Location to,
 			MoveResult result, StrategyException fault) {
 		System.out.println("A move was attempted: ");
		System.out.println("Piece: " + piece.toString());
		System.out.println("From:  " + from.toString());
		System.out.println("To:    " + to.toString());
 		if (result != null){
 			System.out.print("Result:");
 			if (result.getBattleWinner() != null){
 				if (result.getBattleWinner().getPiece() != null){
 					System.out.print(result.getBattleWinner().getPiece().toString() + " won ");
 				}
 				if (result.getBattleWinner().getLocation() != null){
 					System.out.print(" at " + result.getBattleWinner().getLocation().toString());	
 				}
 			} else {
 				System.out.print("the pieces killed eachother lol");
 			}
 			System.out.println( ", Game Status: " + result.getStatus().toString());
 		}
 		if (fault != null)	System.out.println("Fault :" + fault.getMessage());
 		System.out.println(" ");
 	}
 }
