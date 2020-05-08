 package com.tacoid.pweek.logic;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.tacoid.pweek.Pweek;
 import com.tacoid.pweek.SoundPlayer;
 import com.tacoid.pweek.IGameService.Achievement;
 import com.tacoid.pweek.SoundPlayer.SoundType;
 
 public class GameLogic {
 
 	public enum MoveType {
 		LEFT,
 		RIGHT,
 		UP,
 		DOWN,
 		ROT_RIGHT,
 		ROT_LEFT
 	}
 
 	public final static int LINES = 12;
 	public final static int COLUMNS = 6;
 	public final int GARBAGE = 6;
 
 	private State state = State.MOVE;
 
 	int grid[][] = new int[LINES * 2][COLUMNS];
 	private Piece piece = new Piece();
 	private Piece nextPiece = new Piece();
 
 	public boolean gridFF[][];
 	private float sum = 0f;
 	private int score = 0;
 	private ArrayList<Falling> fallings;
 	private float speed = 0.4f;
 
 	private boolean first = true;
 	private ArrayList<Explosion> removes;
 	private int combo = 0;
 	private boolean isDown = false;
 	private GameLogic opponent = null;
 	private int points = 0;
 	public int garbage = 0;
 	private float leftoverNuisance = 0f;
 
 	protected boolean paused = false;
 
 	private boolean isIA = false;
 	private int n_colors = 4;
 	private boolean cheatMode = false;
 	private float initialSpeed = 0.4f;
 
 	public ArrayList<Explosion> explosions = new ArrayList<Explosion>();
 
 	public GameLogic(boolean isIA) {
 		this.isIA = isIA;
 	}
 
 	public void init() {
 		if (!isIA) {
 			Pweek.getInstance().getGameService().unlockAchievement(Achievement.FANBOY);
 		}
 		
 		fallings = null;
 		score = 0;
 		sum = 0;
 		speed = initialSpeed;
 		state = State.MOVE;
 		points = 0;
 		garbage = 0;
 		leftoverNuisance = 0f;
 
 		for (int l = 0; l < LINES * 2; l++) {
 			for (int c = 0; c < COLUMNS; c++) {
 				grid[l][c] = 0;
 			}
 		}
 		generate(); // generate current
 		generate(); // generate next
 	}
 
 	public GameLogic(GameLogic logic, boolean isIA) {
 		// copy grid.
 		for (int l = 0; l < LINES; l++) {
 			for (int c = 0; c < COLUMNS; c++) {
 				grid[l][c] = logic.grid[l][c];
 			}
 		}
 
 		this.isIA = isIA;
 
 		piece = new Piece(logic.piece, true);
 		nextPiece = new Piece(logic.nextPiece, true);
 
 		state = logic.state;
 		combo = 0;
 	}
 
 	private int cheatColor() {
 		int[] counts = new int[GARBAGE];
 		for (int c = 0; c < COLUMNS; c++) {
 			int l = LINES;
 			while (l >= 0 && grid[l][c] == 0) l--;
 			if (l >= 0 && grid[l][c] > 0 && grid[l][c] != GARBAGE) {
 				counts[grid[l][c]]++;
 			}
 		}
 
 		int max = 1;
 		for (int i = 1; i < GARBAGE; i++) {
 			if (counts[max] < counts[i]) {
 				max = i;
 			}
 		}
 		return max;
 	}
 
 	private boolean generate() {
 		// Sens de la piece :
 		// [x] [x][ ] [ ] [ ][x]
 		// [ ] [x]
 
 		piece = new Piece(nextPiece);
 
 		int coul1 = 1 + (int) (Math.random() * n_colors);
 		int coul2;
 
 		if (cheatMode) {
 			if (Math.random() < 0.5) {
 				coul2 = coul1;
 			} else {
 				coul2 = cheatColor();
 			}
 		} else {
 			coul2 = 1 + (int) (Math.random() * n_colors);
 		}
 		nextPiece.rot = (int) (Math.random() * 4);
 
 		switch (nextPiece.rot) {
 		case 0:
 			nextPiece.coords[0] = new Coord(LINES - 1, COLUMNS / 2, coul1);
 			nextPiece.coords[1] = new Coord(LINES, COLUMNS / 2, coul2);
 			break;
 		case 1:
 			nextPiece.coords[0] = new Coord(LINES, COLUMNS / 2, coul1);
 			nextPiece.coords[1] = new Coord(LINES, COLUMNS / 2 + 1, coul2);
 			break;
 		case 2:
 			nextPiece.coords[0] = new Coord(LINES, COLUMNS / 2, coul1);
 			nextPiece.coords[1] = new Coord(LINES - 1, COLUMNS / 2, coul2);
 			break;
 		case 3:
 			nextPiece.coords[0] = new Coord(LINES, COLUMNS / 2 + 1, coul1);
 			nextPiece.coords[1] = new Coord(LINES, COLUMNS / 2, coul2);
 			break;
 		}
 
 		return (piece.coords[0] == null) || (grid[(int)piece.coords[0].l][(int)piece.coords[0].c] == 0 && grid[(int)piece.coords[1].l][(int)piece.coords[1].c] == 0);
 	}
 
 	public void rotateRight() {
		if (state == State.MOVE) {
 			if(!isIA) {
 				SoundPlayer.getInstance().playSound(SoundType.ROTATE, 0.5f, true);
 			}
 			piece.rotateRight(grid);
 		}
 	}
 
 	public void rotateLeft() {
		if (state == State.MOVE) {
 			if(!isIA) {
 				SoundPlayer.getInstance().playSound(SoundType.ROTATE, 0.5f, true);
 			}
 			piece.rotateLeft(grid);
 		}
 	}
 
 	public void moveDown() {
		if (state == State.MOVE) {
 			piece.descendre(grid);
 			if(!isIA) {
 				SoundPlayer.getInstance().playSound(SoundType.MOVE, 0.2f, true);
 			}
 		}
 	}
 
 	public boolean moveLeft() {
		if (state == State.MOVE) {
 			if (piece.moveLeft(grid)) {
 				if(!isIA) {
 					SoundPlayer.getInstance().playSound(SoundType.MOVE, 0.2f, true);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean moveRight() {
		if (state == State.MOVE) {
 			if (piece.moveRight(grid)) {
 				if(!isIA) {
 					SoundPlayer.getInstance().playSound(SoundType.MOVE, 0.2f, true);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void dropPiece() {
		if (state == State.MOVE) {
 			state = State.POSE;
 		}
 	}
 
 	private ArrayList<Falling> gravity() {
 		ArrayList<Falling> boules = new ArrayList<Falling>();
 		int[] sums = new int[COLUMNS];
 		for (int l = 0; l < LINES * 2; l++) {
 			for (int c = 0; c < COLUMNS; c++) {
 				if (grid[l][c] == 0) {
 					sums[c]++;
 				} else if (sums[c] > 0) {
 					boules.add(new Falling(new Coord(l, c, grid[l][c]), sums[c]));
 					grid[l][c] = 0;
 				}
 			}
 		}
 		return boules;
 	}
 
 	public int floodfill(int l, int c, int coul, ArrayList<Coord> list) {
 		if (l < 0 || c < 0 || l >= LINES || c >= COLUMNS || grid[l][c] != coul
 				|| gridFF[l][c]) {
 			return 0;
 		}
 		gridFF[l][c] = true;
 		if (list != null) {
 			list.add(new Coord(l, c, coul));
 		}
 		return 1 + floodfill(l + 1, c, coul, list)
 				+ floodfill(l - 1, c, coul, list)
 				+ floodfill(l, c + 1, coul, list)
 				+ floodfill(l, c - 1, coul, list);
 	}
 
 	public ArrayList<Explosion> resolve() {
 		ArrayList<Explosion> remove = new ArrayList<Explosion>();
 		gridFF = new boolean[LINES][COLUMNS];
 		for (int l = 0; l < LINES; l++) {
 			for (int c = 0; c < COLUMNS; c++) {
 				if (grid[l][c] > 0 && grid[l][c] != GARBAGE) {
 					ArrayList<Coord> list = new ArrayList<Coord>();
 					if (floodfill(l, c, grid[l][c], list) >= 4) {
 						if (grid[l][c] == 5 && !isIA) {
 							Pweek.getInstance().getGameService().unlockAchievement(Achievement.NINJA);
 						}
 						remove.add(new Explosion(this, list));
 					}
 				}
 			}
 		}
 		if(!isIA && !remove.isEmpty()) {
 			SoundPlayer.getInstance().playSound(SoundType.EXPLODE, 1.0f, 0.8f + combo / 30.0f);
 		}
 		return remove;
 	}
 
 	private void pose() {
 		for (Coord p : piece.coords) {
 			grid[(int)p.l][(int)p.c] = p.coul;
 			p.coul = 0;
 		}
 	}
 
 	public void poseEtGravity() {
 		for (Coord c : piece.coords) {
 			while (c.l > 0 && grid[(int)c.l - 1][(int)c.c] == 0) {
 				c.l--;
 			}
 			grid[(int)c.l][(int)c.c] = c.coul;
 		}
 	}
 
 	public int descendreEtPose() {
 		pose();
 		points = 0;
 		combo = 0;
 		do {
 			fallings = gravity();
 			for (Falling f : fallings) {
 				grid[(int)f.getEnd().l][(int)f.getEnd().c] = f.getEnd().coul;
 			}
 
 			removes = resolve();
 			for (Explosion r : removes) {
 				r.points = r.getNbPuyos() * 10 * (r.getNbPuyos() - 3 + combo);
 				points += r.points;
 			}
 			if (combo == 0)
 				combo = 8;
 			else
 				combo *= 2;
 		} while (removes.size() > 0);
 		return points;
 	}
 
 	public void update(float delta) {
 		if(!paused) {
 			sum += delta;
 			switch (state) {
 			case GARBAGE:
 				if (garbage > 0) {
 					generateGarbage();
 					state = State.GRAVITY;
 				} else {
 					state = State.MOVE;
 				}
 				first = true;
 				break;
 			case MOVE:
 				if (first) {
 					if (!generate()) {
 						state = State.LOST;
 						if (score == 0 && !isIA) {
 							Pweek.getInstance().getGameService().unlockAchievement(Achievement.AFK);
 						}
 					} else if (!isIA && score > 0) {
 						boolean ocd = true;
 						for (int col = 0; col < COLUMNS && ocd; col++) {
 							ocd = ocd && grid[0][col] == 0;
 						}
 						if (ocd) {
 							Pweek.getInstance().getGameService().unlockAchievement(Achievement.OCD);
 						}
 					}
 					first = false;
 				}
 				if (sum > speed || (isDown && sum > 0.1f)) {
 					if (!piece.descendre(grid)) {
 						state = State.POSE;
 					}
 					sum = 0f;
 				}
 				break;
 			case POSE:
 				if (sum > speed) {
 					pose();
 					state = State.GRAVITY;
 					sum = 0f;
 					first = true;
 				}
 				break;
 			case GRAVITY:
 				if (first) {
 					fallings = gravity();
 					first = false;
 				}
 				for (Falling f : fallings) {
 					f.update(delta);
 				}
 				if (sum > 0.5) {
 					boolean playGarbage = false;
 					for (Falling f : fallings) {
 						grid[(int)f.getEnd().l][(int)f.getEnd().c] = f.getEnd().coul;
 						if(f.getEnd().coul == GARBAGE && f.getInitial().l >= LINES) {
 							playGarbage = true;
 						}
 					}
 
 					if(playGarbage == true) {
 						SoundPlayer.getInstance().playSound(SoundType.NUISANCE, 0.5f, true);
 					}
 					fallings = null;
 					first = true;
 					state = State.RESOLVE;
 					sum = 0f;
 				}
 				break;
 			case RESOLVE:
 				if (first) {
 					removes = resolve();
 					for (Explosion r : removes) {
 						if (r.getNbPuyos() >= 8) {
 							Pweek.getInstance().getGameService().unlockAchievement(Achievement.MEGA_EXPLODE);
 							if (r.getNbPuyos() >= 10) {
 								Pweek.getInstance().getGameService().unlockAchievement(Achievement.MASTERSTROKE);
 							}
 						}
 						r.points = r.getNbPuyos() * 10 * (r.getNbPuyos() - 3 + combo);
 						if (opponent != null) {
 							float nuisance = r.points / 70.0f + leftoverNuisance;
 							leftoverNuisance = nuisance - (int) nuisance;
 							opponent.sendGarbage((int) nuisance);
 						}
 						points += r.points;
 						explosions.add(r);
 					}
 					first = false;
 				}
 				if (sum > 0.3) {
 					if (removes.size() > 0) {
 						state = State.GRAVITY;
 						first = true;
 						if (combo == 0) {
 							combo = 8;
 						} else {
 							combo *= 2;
 							if (!isIA) {
 								Pweek.getInstance().getGameService().unlockAchievement(Achievement.FIRST_COMBO);
 								if (combo == 8 * 2 * 2 * 2 * 2) {
 									Pweek.getInstance().getGameService().unlockAchievement(Achievement.CHAIN);
 								}
 							}
 						}
 					} else {
 						state = State.GARBAGE;
 						score += points;
 						points = 0;
 						combo = 0;
 					}
 					sum = 0f;
 				}
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	private void generateGarbage() {
 		int l = LINES;
 
 		while (garbage >= COLUMNS) {
 			for (int c = 0; c < COLUMNS; c++) {
 				grid[l][c] = GARBAGE;
 			}
 			garbage -= COLUMNS;
 			l++;
 		}
 
 		ArrayList<Integer> cols = new ArrayList<Integer>();
 		for (int c = 0; c < COLUMNS; c++) {
 			cols.add(c);
 		}
 
 		for (int c = 0; c < garbage; c++) {
 			int i = (int)(Math.random() * cols.size());
 			grid[l][cols.get(i)] = GARBAGE;
 			cols.remove(i);
 		}
 		garbage = 0;
 	}
 
 	public int[][] getGrid() {
 		return grid;
 	}
 
 	public Piece getPiece() {
 		return piece;
 	}
 
 	public Piece getNextPiece() {
 		return nextPiece;
 	}
 
 	public ArrayList<Falling> getFallings() {
 		return fallings;
 	}
 
 	public State getState() {
 		return state;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void down() {
 		isDown = true;
 	}
 
 	public void up() {
 		isDown = false;
 	}
 
 	public void setOpponent(GameLogic opponent) {
 		this.opponent = opponent;
 	}
 
 	public void sendGarbage(int garbage) {
 		this.garbage += garbage;
 		if (this.garbage > LINES * COLUMNS) {
 			garbage = LINES * COLUMNS;
 		}
 	}
 
 	public void pause() {
 		paused = true;
 	}
 
 	public void resume() {
 		paused = false;
 	}
 
 	public boolean isPaused() {
 		return paused;
 	}
 
 	public void setInitialSpeed(float speed) {
 		this.initialSpeed = speed;
 		this.speed = speed;
 	}
 
 	public void setSpeed(float speed) {
 		this.speed = speed;
 	}
 
 	public void setNColors(int n) {
 		this.n_colors = n;
 	}
 
 	public void setCheatMode(boolean cheatMode) {
 		this.cheatMode = cheatMode;
 	}
 
 	public ArrayList<Explosion> getExplosions() {
 		long date = System.currentTimeMillis();
 		Iterator<Explosion> it = explosions.iterator();
 		while (it.hasNext()) {
 			Explosion e = it.next();
 			if (date - e.getExplosionDate() > 1000) {
 				it.remove();
 			}
 		}
 		return explosions;
 	}
 }
