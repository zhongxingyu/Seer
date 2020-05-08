 package com.RotN.acdc.logic;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import android.util.Log;
 
 import com.RotN.acdc.logic.CheckerContainer.BoardPositions;
 import com.RotN.acdc.logic.CheckerContainer.GameColor;
 
 public class AcDcAI {
 	
 	public ArrayList<Move> GetNextMove(TheGame acdc) {
 		ArrayList<BoardPositions> pertinentContainers = new ArrayList<BoardPositions>();
 		for (CheckerContainer container : acdc.containers) {
 			
 			if ( (acdc.turn == GameColor.BLACK && container.getBlackCheckerCount() > 0) ||
 					(acdc.turn == GameColor.WHITE && container.getWhiteCheckerCount() > 0) ) {
 				pertinentContainers.add(container.getPosition());
 			}  
 		}
 		AIMoves aiMoves = new AIMoves();
 		TheGame acdcCopy = new TheGame(acdc);
 		TheGameImpl acdcImplCopy = new TheGameImpl();
 		acdcImplCopy.setGammonData(acdcCopy);
 		if (acdc.acdcOrigMove) {
 			//sweet jeebus AcDc takes too long. Treat it as two moves for now
 			aiMoves = HandleAcDc(acdcImplCopy, pertinentContainers);
 		} else {
 			aiMoves = GetNextMove(acdcImplCopy, aiMoves, 1, pertinentContainers);
 		}
 		
 		logAIMove("The Move", aiMoves);
 		
 		return aiMoves.moves;
 	}
 	
 	private AIMoves HandleAcDc(TheGameImpl acdc, ArrayList<BoardPositions> pertinentContainers) {
 		AIMoves aiMove = new AIMoves();
 		
 		//do the initial move
 		acdc.getGammonData().acdcOrigMove = false;
 		aiMove = GetNextMove(acdc, aiMove, 1, pertinentContainers);
 		for (Move move : aiMove.moves) {
 			if (move.color == acdc.getTurn()) {
 				acdc.movePiece(move.origSpot, move.newSpot);
 			}
 		}
 		
 		//reset the pertinent containers
 		pertinentContainers.clear();
 		for (CheckerContainer container : acdc.getGammonData().containers) {
 			
 			if ( (acdc.getTurn() == GameColor.BLACK && container.getBlackCheckerCount() > 0) ||
 					(acdc.getTurn() == GameColor.WHITE && container.getWhiteCheckerCount() > 0) ) {
 				pertinentContainers.add(container.getPosition());
 			}  
 		}
 		
 		//you only get another move if you used all your current ones
 		if (acdc.getGammonData().movesRemaining.size() == 0) {
 			AIMoves miscDoubles = null;
 			for (int i = 1; i <= 6; i++) {
 				acdc.getGammonData().movesRemaining.add(i);
 				acdc.getGammonData().movesRemaining.add(i);
 				acdc.getGammonData().movesRemaining.add(i);
 				acdc.getGammonData().movesRemaining.add(i);
 				
 				AIMoves miscDoublesTemp = new AIMoves();
 				miscDoublesTemp = GetNextMove(acdc, miscDoublesTemp, 1, pertinentContainers);
 				if (miscDoubles == null) {
 					miscDoubles = miscDoublesTemp;
 				} else if (miscDoublesTemp.value > miscDoubles.value &&
 						miscDoublesTemp.moves.size() > 0) {
 					miscDoubles = miscDoublesTemp;
 				}
 				acdc.getGammonData().movesRemaining.clear();
 			}
 			
 			aiMove.moves.addAll(miscDoubles.moves);
 		}
 		
 		return aiMove;
 	}
 
 	private AIMoves GetNextMove(TheGameImpl acdc, AIMoves movesUsed, int depth, ArrayList<BoardPositions> pertinentContainers) {
 		String bDepth = "[" + depth + "]";
 		AIMoves aiMove = new AIMoves();
 				
 		for (BoardPositions possibleChoice : pertinentContainers) {
 			// this gives me the possible moves for a given container
 			Vector<BoardPositions> options = acdc.getPossibleMoves(possibleChoice, false);
 			
 			Log.d("AI", bDepth + "Checking container: " + possibleChoice.toString() + " it has " + options + " possible moves");
 			
 			//loops through the move options
 			for (BoardPositions move : options) {
 				//create a copy to move pieces on
 				ArrayList<BoardPositions> pertContainersCopy = new ArrayList<BoardPositions>(pertinentContainers);
 				
 				boolean doubles = movingDoubles(acdc.getGammonData().movesRemaining);
 				
 				//moves the piece on our game clone and returns everything done (important in case something went to pokey)
 				ArrayList<Move> moves = acdc.movePiece(possibleChoice, move);
 				for (Move chosenMove: moves) {
 					if (chosenMove.color == acdc.getTurn()) {
 						if (acdc.getActiveTurnCheckerCount(chosenMove.origSpot) == 0) {
 							pertContainersCopy.remove(chosenMove.origSpot);
 						}
 						
 						if (false == pertContainersCopy.contains(chosenMove.newSpot)) {
 							pertContainersCopy.add(chosenMove.newSpot);
 						}
 					}
 				}
 				Log.d("AI", bDepth + " Using move " + move.toString());
 				//make a copy of moves used to this point
 				AIMoves possible = new AIMoves(movesUsed);
 				//add the moves that we just did
 				possible.moves.addAll(moves);
 				
 				TheGame acdcData = acdc.getGammonData();
 				// if there are no more moves remaining time to check our score
				if (acdc.getGammonData().movesRemaining.size() > 0) {
 					possible = GetNextMove(acdc, possible, depth + 1, pertContainersCopy);
 				} else {
 					// returns a board value based on piece position
 					possible.value = evaluateBoard(acdcData);
 					this.logAIMove("Possible ", possible);
 				}
 
 				//undo what we did for the next check
 				for (Move movesToUndo : moves) {
 					acdcData.containers.get(movesToUndo.newSpot.getIndex()).removePiece(movesToUndo.color);
 					acdcData.containers.get(movesToUndo.origSpot.getIndex()).addPiece(movesToUndo.color);
 					if (movesToUndo.moveLength != -1) {
 						acdcData.movesRemaining.add(movesToUndo.moveLength);
 					}
 				}
 				
 				if (possible.value > aiMove.value &&
 						possible.moves.size() > 0) {
 					//Log.d("AI", bDepth + " New best value " + possible.value + " > " + aiMove.value);
 					aiMove = possible;
 				}
 				
 				if (doubles) {
 					break;
 				}
 			}
 		}
 		
 		return aiMove;
 	}
 	
 	private boolean movingDoubles(ArrayList<Integer> movesRemaining) {
 		boolean doubles = true;
 		int moveLength = -1;
 		
 		for (Integer move : movesRemaining) {
 			if (moveLength == -1) {
 				moveLength = move;
 			} else if (moveLength != move) {
 				doubles = false;
 				break;
 			}
 		}
 		
 		return doubles;
 	}
 	
 	private void logAIMove(String tag, AIMoves option) {
 		Log.d("AI", tag + " Value: " + option.value);
 		
 		for (Move move: option.moves) {
 			Log.d("AI", move.origSpot.toString() + ", " + move.newSpot.toString() + ", " + move.color.toString());
 		}
 	}
 	
 	private Integer evaluateBoard(TheGame acdc) {
 		int boardValue = 0;
 		
 		if (acdc.turn == GameColor.BLACK) {
 			boardValue = evaluateBoardBlackPerspective(acdc);
 		} else {
 			boardValue = evaluateBoardWhitePerspective(acdc);
 		}
 		
 		return boardValue;
 	}
 	
 	public Integer evaluateBoardBlackPerspective(TheGame acdc) {
 		Integer boardValue = 0;
 		
 		for (CheckerContainer container : acdc.containers) {
 			
 			int containerValue = 25 - container.getPosition().getIndex();
 			if (container.getPosition() == BoardPositions.BLACK_BUNKER ||
 					container.getPosition() == BoardPositions.POKEY) {
 				//don't count nothing these aren't important
 			} else if (container.getBlackCheckerCount() > 1) {
 				boardValue += container.getBlackCheckerCount() * (containerValue);
 			} else if (container.getBlackCheckerCount() == 1) {
 				//pieces that get hit farther along hurt more so lower the value of the open ones
 				if (containerValue > 18) {
 					containerValue = (int)(containerValue * 0.25);
 				} else if (containerValue > 12) {
 					containerValue = (int)(containerValue * .5);
 				} else if (containerValue > 6) {
 					containerValue = (int)(containerValue * .75);
 				}
 				
 				if (checkForWhitePieceWithinNineSpots(acdc, container) ) {
 					boardValue += (containerValue - 12);
 				} else {
 					boardValue += (containerValue - 6);
 				}	
 			}
 		}
 		
 		return boardValue;
 	}
 	
 	public Integer evaluateBoardWhitePerspective(TheGame acdc) {
 		Integer boardValue = 0;
 		
 		for (CheckerContainer container : acdc.containers) {
 			
 			int containerValue = container.getPosition().getIndex();
 			if (container.getPosition() == BoardPositions.WHITE_BUNKER ||
 					container.getPosition() == BoardPositions.POKEY) {
 				//don't count nothing these aren't important
 			} else if (container.getWhiteCheckerCount() > 1) {
 				boardValue += container.getWhiteCheckerCount() * containerValue;
 			} else if (container.getWhiteCheckerCount() == 1) {
 				//pieces that get hit farther along hurt more so lower the value of the open ones				
 				if (containerValue > 18) {
 					containerValue = (int)(containerValue * 0.25);
 				} else if (containerValue > 12) {
 					containerValue = (int)(containerValue * .5);
 				} else if (containerValue > 6) {
 					containerValue = (int)(containerValue * .75);
 				}
 				
 				if (checkForBlackPieceWithinNineSpots(acdc, container) ) {
 					boardValue += containerValue - 12;
 				} else {
 					boardValue += containerValue - 6;
 				}				
 			}
 		}
 		
 		return boardValue;
 	}
 	
 	private boolean checkForBlackPieceWithinNineSpots(TheGame acdc, CheckerContainer container) {
 		boolean blackFound = false;
 		
 		int containerIndex = container.getPosition().getIndex();
 		
 		for (int index = 1; index <= 9; index++) {
 			if (index + containerIndex == 25) {
 				if ( (acdc.containers.get(containerIndex + index).getBlackCheckerCount() > 0 &&
 						acdc.allBlackPiecesOut == false) ||
 						acdc.containers.get(BoardPositions.POKEY.getIndex()).getBlackCheckerCount() > 0) {
 					blackFound = true;
 					break;
 				}
 			} else if (index + containerIndex > 25) {
 				break;
 			} else if (acdc.containers.get(containerIndex + index).getBlackCheckerCount() > 0) {
 				blackFound = true;
 				break;
 			}
 		}
 		
 		return blackFound;
 	}
 	
 	private boolean checkForWhitePieceWithinNineSpots(TheGame acdc, CheckerContainer container) {
 		boolean whiteFound = false;
 		
 		int containerIndex = container.getPosition().getIndex();
 		
 		for (int index = 1; index <= 9; index++) {
 			if (containerIndex - index == 0) {
 				if ( (acdc.containers.get(containerIndex - index).getWhiteCheckerCount() > 0 &&
 						acdc.allWhitePiecesOut == false) ||
 						acdc.containers.get(BoardPositions.POKEY.getIndex()).getWhiteCheckerCount() > 0) {
 					whiteFound = true;
 					break;
 				}
 			} else if (containerIndex - index < 0) {
 				break;
 			} else if (acdc.containers.get(containerIndex - index).getWhiteCheckerCount() > 0) {
 				whiteFound = true;
 				break;
 			}
 		}
 		
 		return whiteFound;
 	}
 }
