 import java.util.ArrayList;
 
 public class Hunter extends Character {
 	private static final int MAXSTEPS = 1000;
 	private String name;
 	private int booty, posx, posy, steps, lastpos;
 	private Game game;
 	private Field pos;
 
 	/**
 	 *  (precondition) x and y have to be in the bounds of the games labyrinth, game must be a valid game instance
 	 *  (not null), time has to be bigger than 0
 	 *  (postcondition) If all parameters were valid, a new instance of Hunter has been created, ready to be started,
 	 *  if the start-coordinates were invalid, this Hunter will be terminated and removed.
 	 */
 	public Hunter(int time, int x, int y, String name, Game game) {
 		super(time, game);
 		this.name = name;
 		this.booty = 0;
 		this.posx = x;
 		this.posy = y;
 		this.steps = 0;
 		this.lastpos = -1;
 		this.game = game;
 		try {
 			pos = game.getLabyrith().getField(x, y);
 		} catch (IndexOutOfBoundsException ex) {
 			System.err.println("Hunter "+name+" in game "+game+" spawned on invalid field! Hunter will die now.");
 			this.die();
 		}
 	}
 
 	/**
 	 * (precondition) Hunter is up and running, game is a valid game, the Hunter is on a valid position on
 	 * the field and able to move (not blocked by walls)
 	 * (postcondition) After this method was called, with all the preconditions fulfilled, the Hunter will
 	 * have moved one square, eventually being killed or winning the game in the process. The step counter
 	 * will be incremented.
 	 *  If the conditions were not met, the Hunter will be terminated and removed
 	 */
 	@Override
 	protected void move() {
 		// List all directions
 		ArrayList<Integer> directions = new ArrayList<Integer>(4);
 		directions.add(Field.NORTH); // N
 		directions.add(Field.EAST); // E
 		directions.add(Field.SOUTH); // S
 		directions.add(Field.WEST); // W
 		
 		Labyrinth lab  = game.getLabyrith();
 		// Remove directions blocked by wall 
 		if (lab.hasWall(pos, Field.NORTH)) {
 			directions.remove(new Integer(Field.NORTH));
 		} else if (lab.hasWall(pos, Field.EAST)) {
 			directions.remove(new Integer(Field.EAST));
 		} else if (lab.hasWall(pos, Field.SOUTH)) {
 			directions.remove(new Integer(Field.SOUTH));
 		} else if (lab.hasWall(pos, Field.WEST)) {
 			directions.remove(new Integer(Field.WEST));
 		}
 		
 		// If list is empty and there's no where to go, die
 		if (directions.size() == 0) {
 			this.die();
 			return;
 		}
 		
 		// If list size > 1 and lastpos defined, don't go there
 		if (directions.size() > 1 && lastpos >= 0 && lastpos < 4) {
 			directions.remove(lastpos);
 		}
 
 		// Pick random direction from list
		int direction = directions.get( (int) Math.round(Math.random() * (directions.size()-1)) );
 		int newx, newy;
 		newx = posx;
 		newy = posy;
 		if (direction == Field.NORTH) {
 			newy--;
 		} else if (direction == Field.EAST) {
 			newx++;
 		} else if (direction == Field.SOUTH) {
 			newy++;
 		} else if (direction == Field.WEST) {
 			newx--;
 		}
 		
 		// if win field, game is over
 		if (game.getLabyrith().onWinField(newx, newy)) {
 			game.hunterWin(); 
 		} else {
 			pos.leave(this); // Leave field
 			try {
 				pos = game.getLabyrith().getField(newx, newy);
 			} catch (IndexOutOfBoundsException ex) {
 				// This cannot happen if this program is correct
 				System.err.println("ERRO: Hunter "+name+" made illegal move! This should not happen, terminating program.");
 				System.exit(1);
 			}
 			pos.enter(this);
 			
 			// Increment step count and end game if maxstep reached
 			if (++steps >= MAXSTEPS) game.hunterWin();
 			
 			// Update lastpos and coordinates
 			if (direction == Field.NORTH) {
 				lastpos = Field.SOUTH;
 			} else if (direction == Field.EAST) {
 				lastpos = Field.WEST;
 			} else if (direction == Field.SOUTH) {
 				lastpos = Field.NORTH;
 			} else if (direction == Field.WEST) {
 				lastpos = Field.EAST;
 			}
 			posx = newx;
 			posy = newy;
 		}
 	}
 	
 	// (invariant) Name of this Hunter will be returned and not altered
 	public String getName() {
 		return name;
 	}
 
 	// (invariant) Value of collected treasures of this Hunter will be returned and not altered
 	public int getBooty() {
 		return booty;
 	}
 	
 	// (postcondition) The Thread of this Hunter will stop at some point, it will be removed from
 	//  the game and not move anymore
 	public void die() {
 		this.stopThread();
 		game.killHunter(this);
 	}
 }
