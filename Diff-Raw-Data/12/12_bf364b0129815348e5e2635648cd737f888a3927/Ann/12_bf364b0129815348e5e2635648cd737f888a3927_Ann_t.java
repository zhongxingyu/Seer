 package mechanics;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class Ann implements ArtificialIntilligence {
 
 	private static int CLEVER = 1;
 	private static int STUPID = 2;
 
 	private Move bestMove;
 
 	private void searchForLines(Board b, Cell c, List<Group> l, Direction d,
 			int side) {
 		Cell neighbour = c.shift(d);
 		if (b.getState(neighbour) == side) {
 			l.add(new Group(c, neighbour));
 			neighbour = neighbour.shift(d);
 			if (b.getState(neighbour) == side)
 				l.add(new Group(c, neighbour));
 		}
 	}
 
 	public List<Group> getAllGroups(Board b, byte side) {
 		List<Group> list = new ArrayList<Group>();
 		for (Cell c : b.getSideMarbles(side)) {
 			list.add(new Group(c));
 			searchForLines(b, c, list, Direction.East, side);
 			searchForLines(b, c, list, Direction.SouthEast, side);
 			searchForLines(b, c, list, Direction.South, side);
 		}
 		return list;
 	}
 
 	public List<Move> getAllPossibleMoves(Board b, byte side) {
 		List<Move> list = new ArrayList<Move>();
 		for (Group group : getAllGroups(b, side)) {
 			for (Direction d : Direction.values()) {
 				Move m = new Move(group, d, side);
 				if (b.getMoveType(m).getResult() != MoveType.NOMOVE) {
 					list.add(m);
 				}
 			}
 		}
 		return list;
 	}
 
 	private double evaluatePosition(Board b, byte side, int steps,
 			double alphabeta, int moveType) {
 		byte oppSide = Board.oppositeSide(side);
 		Cell center = Cell.get(5, 5);
 		byte[][] f = b.getField();
 		if (steps == 0) {
 			if (moveType == CLEVER) {
 				double sum = 4
 						* (b.getMarblesCaptured(Board.oppositeSide(side)) - b
 								.getMarblesCaptured(side)) + Math.random()
 						* 0.000001;
 				for (int i = 1; i <= 9; i++)
 					for (int j = Board.getMinColumn(i); j <= Board
 							.getMaxColumn(i); j++) {
 						if (f[i][j] == side)
 							sum += 1 / (Cell.get(i, j).findDistance(center) + 1.0);
 						else if (f[i][j] == oppSide)
 							sum -= 1 / (Cell.get(i, j).findDistance(center) + 1.0);
 					}
 				return sum;
 			} else {
 				double sum = 0;
 				for (int i = 1; i <= 9; i++)
 					for (int j = Board.getMinColumn(i); j <= Board
 							.getMaxColumn(i); j++) {
 						if (f[i][j] == side) {
							if (f[i-1][j] == side) sum--;
							if (f[i+1][j] == side) sum--;
							if (f[i][j-1] == side) sum--;
							if (f[i][j+1] == side) sum--;
							if (f[i-1][j-1] == side) sum--;
							if (f[i+1][j+1] == side) sum--;
 						}
 					}
 				return sum;
 			}
 		} else {
 			Board futureBoard;
 			Move bestMove = null, m = null;
 			double currValue, bestValue = Double.POSITIVE_INFINITY, ab = alphabeta;
 			Cell original, shifted1, shifted2, shifted3, shifted4;
 			byte state1, state2, state3, state4, state5;
 			// byte oppSide = Board.oppositeSide(side);
 			// byte[][] f = b.getField();
 			flag: for (int i = 1; i <= 9; i++)
 				for (int j = Board.getMinColumn(i); j <= Board.getMaxColumn(i); j++) {
 					if (f[i][j] == side) {
 						for (Direction d : Direction.getSecondary()) {
 							original = Cell.get(i, j);
 							shifted1 = original.shift(d);
 							state1 = b.getState(shifted1);
 							if (state1 == Layout.E) {
 								futureBoard = b.clone();
 								m = new Move(new Group(original), d, side);
 								futureBoard.makeMove(m);
 								currValue = evaluatePosition(futureBoard,
 										oppSide, steps - 1,
 										ab == Double.NEGATIVE_INFINITY ? ab
 												: -ab, moveType);
 								if (currValue < bestValue) {
 									bestValue = currValue;
 									bestMove = m;
 									if (alphabeta > bestValue)
 										break flag;
 									ab = bestValue;
 								}
 							} else if (state1 == side) {
 								shifted2 = shifted1.shift(d);
 								state2 = b.getState(shifted2);
 								shifted3 = shifted2.shift(d);
 								state3 = b.getState(shifted3);
 								if (state2 == Layout.E
 										|| (state2 == oppSide && (state3 == Layout.N || state3 == Layout.E))) {
 									futureBoard = b.clone();
 									m = new Move(new Group(original, shifted1),
 											d, side);
 									futureBoard.makeMove(m);
 									currValue = evaluatePosition(futureBoard,
 											oppSide, steps - 1,
 											ab == Double.NEGATIVE_INFINITY ? ab
 													: -ab, moveType);
 									if (currValue < bestValue) {
 										bestValue = currValue;
 										bestMove = m;
 										if (alphabeta > bestValue)
 											break flag;
 										ab = bestValue;
 									}
 								} else if (state2 == side) {
 									shifted4 = shifted3.shift(d);
 									state4 = b.getState(shifted4);
 									state5 = b.getState(shifted4.shift(d));
 									if (state3 == Layout.E
 											|| (state3 == oppSide
 													&& state4 == oppSide && (state5 == Layout.N || state5 == Layout.E))) {
 										futureBoard = b.clone();
 										m = new Move(new Group(original,
 												shifted2), d, side);
 										futureBoard.makeMove(m);
 										currValue = evaluatePosition(
 												futureBoard,
 												Board.oppositeSide(side),
 												steps - 1,
 												ab == Double.NEGATIVE_INFINITY ? ab
 														: -ab, moveType);
 										if (currValue < bestValue) {
 											bestValue = currValue;
 											bestMove = m;
 											if (alphabeta > bestValue)
 												break flag;
 											ab = bestValue;
 										}
 									}
 								}
 							}
 						}
 						for (Direction d : Direction.getPrimary()) {
 							original = Cell.get(i, j);
 							shifted1 = original.shift(d);
 							state1 = b.getState(shifted1);
 							if (state1 == Layout.E) {
 								futureBoard = b.clone();
 								m = new Move(new Group(original), d, side);
 								futureBoard.makeMove(m);
 								currValue = evaluatePosition(futureBoard,
 										oppSide, steps - 1,
 										ab == Double.NEGATIVE_INFINITY ? ab
 												: -ab, moveType);
 								if (currValue < bestValue) {
 									bestValue = currValue;
 									bestMove = m;
 									if (alphabeta > bestValue)
 										break flag;
 									ab = bestValue;
 								}
 							} else if (state1 == side) {
 								shifted2 = shifted1.shift(d);
 								state2 = b.getState(shifted2);
 								shifted3 = shifted2.shift(d);
 								state3 = b.getState(shifted3);
 								if (state2 == Layout.E
 										|| (state2 == oppSide && (state3 == Layout.N || state3 == Layout.E))) {
 									futureBoard = b.clone();
 									m = new Move(new Group(original, shifted1),
 											d, side);
 									futureBoard.makeMove(m);
 									currValue = evaluatePosition(futureBoard,
 											oppSide, steps - 1,
 											ab == Double.NEGATIVE_INFINITY ? ab
 													: -ab, moveType);
 									if (currValue < bestValue) {
 										bestValue = currValue;
 										bestMove = m;
 										if (alphabeta > bestValue)
 											break flag;
 										ab = bestValue;
 									}
 								} else if (state2 == side) {
 									shifted4 = shifted3.shift(d);
 									state4 = b.getState(shifted4);
 									state5 = b.getState(shifted4.shift(d));
 									if (state3 == Layout.E
 											|| (state3 == oppSide
 													&& state4 == oppSide && (state5 == Layout.N || state5 == Layout.E))) {
 										futureBoard = b.clone();
 										m = new Move(new Group(original,
 												shifted2), d, side);
 										futureBoard.makeMove(m);
 										currValue = evaluatePosition(
 												futureBoard,
 												Board.oppositeSide(side),
 												steps - 1,
 												ab == Double.NEGATIVE_INFINITY ? ab
 														: -ab, moveType);
 										if (currValue < bestValue) {
 											bestValue = currValue;
 											bestMove = m;
 											if (alphabeta > bestValue)
 												break flag;
 											ab = bestValue;
 										}
 									}
 									for (Direction md : Direction
 											.getNotDirection(d)) {
 										if (b.getState(original.shift(md)) == Layout.E
 												&& b.getState(shifted1
 														.shift(md)) == Layout.E
 												&& b.getState(shifted2
 														.shift(md)) == Layout.E) {
 											futureBoard = b.clone();
 											m = new Move(new Group(original,
 													shifted2), md, side);
 											futureBoard.makeMove(m);
 											currValue = evaluatePosition(
 													futureBoard,
 													Board.oppositeSide(side),
 													steps - 1,
 													ab == Double.NEGATIVE_INFINITY ? ab
 															: -ab, moveType);
 											if (currValue < bestValue) {
 												bestValue = currValue;
 												bestMove = m;
 												if (alphabeta > bestValue)
 													break flag;
 												ab = bestValue;
 											}
 										}
 									}
 								}
 								for (Direction md : Direction
 										.getNotDirection(d)) {
 									if (b.getState(original.shift(md)) == Layout.E
 											&& b.getState(shifted1.shift(md)) == Layout.E) {
 										futureBoard = b.clone();
 										m = new Move(new Group(original,
 												shifted1), md, side);
 										futureBoard.makeMove(m);
 										currValue = evaluatePosition(
 												futureBoard,
 												Board.oppositeSide(side),
 												steps - 1,
 												ab == Double.NEGATIVE_INFINITY ? ab
 														: -ab, moveType);
 										if (currValue < bestValue) {
 											bestValue = currValue;
 											bestMove = m;
 											if (alphabeta > bestValue)
 												break flag;
 											ab = bestValue;
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 
 			this.bestMove = bestMove;
 			return -bestValue;
 		}
 	}
 
 	public Move findNextMove(Board b, byte side, int steps) {
 		Random r = new Random();
 		if (r.nextDouble() > 0.25)
 			evaluatePosition(b, side, steps, Double.NEGATIVE_INFINITY, CLEVER);
 		else
 			evaluatePosition(b, side, steps, Double.NEGATIVE_INFINITY, STUPID);
 		return bestMove;
 	}
 
 	public Move requestMove(Game g) {
 		return findNextMove(g.getBoard(), g.getSide(), 2);
 	}
 }
