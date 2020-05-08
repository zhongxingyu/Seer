 package org.csgames.ai.client;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csgames.ai.client.network.NextMoveSender;
 
 import org.csgames.ai.client.AvailableMoves;
 import org.csgames.ai.client.Util.Point2D;
 
 public class AI {	
 	private static final int BRICK_DISTANCE = 10;
 	private class Action {
 		private AvailableMoves mType = AvailableMoves.None;
 		
 		public AvailableMoves getAvailableMove() {
 			return mType;
 		}
 		
 		public double getScore() {
 			double score = 0.0;
 			Util.Point2D me = mUtil.getMyLocation();
 			int x = me.x;
 			int y = me.y;
 			
 			// Update location
 			switch (mType) {
 			case Down:
 				y += 1;
 				break;
 			case Left:
 				x -= 1;
 				break;
 			case Right:
 				x += 1;
 				break;
 			case Up:
 				y -= 1;
 				break;
 			default:
 				break;
 			}
 			
 			me = new Util.Point2D(x, y);
 			
 			// Bomb based score
 			List<Point2D> bombs = mUtil.search(me.x, me.y, getBombRadius(), Util.BOMB);
 			double bomb_score = 0;
 			for (Point2D bomb : bombs) {
 				bomb_score += mUtil.distance(me.x, me.y, bomb.x, bomb.y);
 			}
 			
 			// Brick based score
 			List<Point2D> bricks = mUtil.search(me.x, me.y, BRICK_DISTANCE, Util.BRICK_WALL);
 			
 			double distance = Double.MAX_VALUE;
 			double brick_score = 0;
 			for (Point2D brick : bricks) {
 				double d = mUtil.distance(brick, me);
 				if (d < distance) {
 					distance = d;
 				}
 			}
 			if (distance < Double.MAX_VALUE) {
 				brick_score += distance;
 			}
 			
 			// Search for powerups
 			
 			// Break BLOCKS!
 			double break_score = 0.0;
 			if (mType == AvailableMoves.DropBomb) {
 				List<Point2D> blocks = mUtil.search(me.x, me.y, getBombRadius(), Util.BRICK_WALL);
 				int count = 0;
 				for (Point2D block : blocks) {
 					if (block.x == me.x || block.y == me.y) {
 						count ++;
 					}
 				}
 				
 				break_score -= count;
 			}
 			
			bomb_score *= 2;
 			brick_score *= 0.000000001;
 			break_score *= 0.0000001;
 			
 			System.out.println("Bomb:" + Double.toString(bomb_score) + " Brick:" + Double.toString(brick_score) + " Break:" + Double.toString(break_score));
 			score = bomb_score + brick_score + break_score;
 			System.out.println(mType.toString() + " " + Double.toString(score) + " " + Double.toString(distance));
 			return score;
 		}
 	}
 
 	private Util mUtil;
 	private ArrayList<Action> mPossibleActions = new ArrayList<>();
 	private Action mNextMove = null;
 	
 	private long mFirstTurn;
 	
 	public AI (Util util) {
 		mUtil = util;
 	}
 
 	public void playMove(NextMoveSender nextMoveSender) throws IOException {
 		// Set the turn time
 		if (mFirstTurn == 0) {
 			mFirstTurn = System.currentTimeMillis();
 		}
 		
 		Action bestAction = new Action();
 		AvailableMoves avoidAction = AvailableMoves.None;
 		
 		if (mNextMove != null) {
 			bestAction = mNextMove;
 			mNextMove = null;
 		} else {
 			// Clear the possible moves
 			mPossibleActions.clear();
 			
 			runFromBombs();
 			lookForPowerups();
 			breakBlocks();
 			attack();
 			
 			// Pick the best action
 			for (Action action : mPossibleActions) {
 				// If its a drop bomb action, ensure we can move away!
 				if (action.getAvailableMove() == AvailableMoves.DropBomb) {
 					AvailableMoves avoid = avoidOwnBomb();
 					if (avoid == AvailableMoves.None) {
 						continue;
 					}
 					avoidAction = avoid;
 				}
 				
 				if (action.getScore() < bestAction.getScore()) {
 					bestAction = action;
 				}
 			}
 		}
 		
 		if (avoidAction != AvailableMoves.None && bestAction.getAvailableMove() == AvailableMoves.DropBomb) {
 			mNextMove = new Action();
 			mNextMove.mType = avoidAction;
 		}
 		
 		// Execute Action
 		nextMoveSender.setMoveAndSend(bestAction.getAvailableMove());
 		
 		System.out.println("Doing:" + bestAction.mType.toString() + Double.toString(bestAction.getScore()));
 		System.out.println();
 	}
 	
 	private AvailableMoves avoidOwnBomb() {
 		Util.Point2D me = mUtil.getMyLocation();
 		Util.Point2D above = new Util.Point2D(me.x, me.y-1);
 		Util.Point2D below = new Util.Point2D(me.x, me.y+1);
 		Util.Point2D left = new Util.Point2D(me.x-1, me.y);
 		Util.Point2D right = new Util.Point2D(me.x+1, me.y);
 		AvailableMoves direction = AvailableMoves.None;
 		
 		if (mUtil.at(above).equals(Util.EMPTY) && canFleeFrom(me, above)) {
 			direction = AvailableMoves.Up;
 		} else if (mUtil.at(below).equals(Util.EMPTY) && canFleeFrom(me, below)) {
 			direction = AvailableMoves.Down;
 		} else if (mUtil.at(left).equals(Util.EMPTY) && canFleeFrom(me, left)) {
 			direction = AvailableMoves.Left;
 		} else if (mUtil.at(right).equals(Util.EMPTY) && canFleeFrom(me, right)) {
 			direction = AvailableMoves.Right;
 		}
 		
 		return direction;
 	}
 	
 	private long getElapsedTime() {
 		return System.currentTimeMillis() - mFirstTurn;
 	}
 	
 	private int getBombRadius() {
 		return 2; // TODO: make this return how many power ups we have plus one
 	}
 	
 	private void runFromBombs() {
 		Util.Point2D me = mUtil.getMyLocation();
 		Util.Point2D above = new Util.Point2D(me.x, me.y-1);
 		Util.Point2D below = new Util.Point2D(me.x, me.y+1);
 		Util.Point2D left = new Util.Point2D(me.x-1, me.y);
 		Util.Point2D right = new Util.Point2D(me.x+1, me.y);
 		
 		List<Util.Point2D> bombs = mUtil.search(me.x, me.y, getBombRadius(), Util.BOMB);
 		
 		for (Util.Point2D bomb : bombs) {
 			boolean moved = false;
 			boolean flee = false;
 			if (bomb.x == me.x) {
 				System.out.println("Bomb vertical!");
 				flee = true;
 				if (mUtil.at(above).equals(Util.EMPTY)) {
 					System.out.println("Going Up");
 					addAction(AvailableMoves.Up); // TODO: check this to see if we move in correct direction
 					moved = true;
 				}
 				if (mUtil.at(below).equals(Util.EMPTY)) {
 					System.out.println("Going Down");
 					addAction(AvailableMoves.Down);
 					moved = true;
 				}
 			}
 			if (bomb.y == me.y) {
 				System.out.println("Bomb horizontal!");
 				flee = true;
 				if (mUtil.at(left).equals(Util.EMPTY)) {
 					System.out.println("Going left");
 					addAction(AvailableMoves.Left); // TODO: check this to see if we move in correct direction
 					moved = true;
 				}
 				if (mUtil.at(right).equals(Util.EMPTY)) {
 					System.out.println("Going right");
 					addAction(AvailableMoves.Right);
 					moved = true;
 				}
 			}
 			
 			if (flee && !moved) {
 				// In range of bomb, but haven't moved
 				System.out.println("Can't move orthagonally!");
 				if (mUtil.at(above).equals(Util.EMPTY)) {
 					addAction(AvailableMoves.Up);
 				}
 				if (mUtil.at(below).equals(Util.EMPTY)) {
 					addAction(AvailableMoves.Down);
 				}
 				if (mUtil.at(right).equals(Util.EMPTY)) {
 					addAction(AvailableMoves.Right);
 				}
 				if (mUtil.at(left).equals(Util.EMPTY)) {
 					addAction(AvailableMoves.Left);
 				}
 			}
 		}
 	}
 	
 	private boolean canFleeFrom(Point2D original, Point2D me) {
 		Util.Point2D above = new Util.Point2D(me.x, me.y-1);
 		Util.Point2D below = new Util.Point2D(me.x, me.y+1);
 		Util.Point2D left = new Util.Point2D(me.x-1, me.y);
 		Util.Point2D right = new Util.Point2D(me.x+1, me.y);
 		
 		return (!above.equals(original) && mUtil.at(above).equals(Util.EMPTY))  ||
 				(!below.equals(original) && mUtil.at(below).equals(Util.EMPTY)) ||
 				(!right.equals(original) && mUtil.at(right).equals(Util.EMPTY)) ||
 				(!left.equals(original) && mUtil.at(left).equals(Util.EMPTY));
 		}
 	
 	private void breakBlocks() {
 		Util.Point2D me = mUtil.getMyLocation();
 		
 		List<Point2D> bricks = mUtil.search(me.x, me.y, getBombRadius(), Util.BRICK_WALL);
 		
 		for (Point2D brick : bricks) {
 			if (brick.x == me.x || brick.y == me.y) {
 				addAction(AvailableMoves.DropBomb);
 				break;
 			}
 		}
 	}
 	
 	private void addAction(AvailableMoves action) {
 		Action a = new Action();
 		a.mType = action;
 		mPossibleActions.add(a);
 	}
 	
 	private void lookForPowerups() {
 		Util.Point2D me = mUtil.getMyLocation();
 		Util.Point2D above = new Util.Point2D(me.x, me.y-1);
 		Util.Point2D below = new Util.Point2D(me.x, me.y+1);
 		Util.Point2D left = new Util.Point2D(me.x-1, me.y);
 		Util.Point2D right = new Util.Point2D(me.x+1, me.y);
 		
 		List<Point2D> bricks = mUtil.search(me.x, me.y, BRICK_DISTANCE, Util.BRICK_WALL);
 		
 		Point2D closest = null;
 		double distance = Double.MAX_VALUE;
 		
 		for (Point2D brick : bricks) {
 			double d = mUtil.distance(brick, me);
 			if (d < distance) {
 				closest = brick;
 				distance = d;
 				
 				int d_x = me.x - brick.x;
 				int d_y = me.y - brick.y;
 				
 				Point2D target = null;
 				AvailableMoves dir = AvailableMoves.None;
 				if (d_x < 0) {
 					// Move Right
 					target = right;
 					dir = AvailableMoves.Right;
 				} else if (d_x > 0) {
 					// Move Left
 					target = left;
 					dir = AvailableMoves.Left;
 				}
 				
 				if (target != null && mUtil.at(target).equals(Util.EMPTY)) {
 					addAction(dir);
 				}
 				
 				if (d_y < 0) {
 					// Move Down
 					target = below;
 					dir = AvailableMoves.Down;
 				} else if (d_y > 0) {
 					// Move Up
 					target = above;
 					dir = AvailableMoves.Up;
 				}
 				
 				if (target != null && mUtil.at(target).equals(Util.EMPTY)) {
 					addAction(dir);
 				}
 			}
 		}
 	}
 	
 	private void attack() {
 		
 	}
 }
