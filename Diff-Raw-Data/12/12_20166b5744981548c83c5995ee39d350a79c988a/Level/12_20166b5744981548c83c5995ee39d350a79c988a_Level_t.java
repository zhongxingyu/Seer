 package model.game;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import model.exception.IllegalMovementException;
 
 public class Level {
 	private ArrayList<ArrayList<Box>> map = new ArrayList<ArrayList<Box>>();
 	private Player p = null;
 	private boolean finished = false;
 	private String name;
 
 	/**
 	 * Initiate a level.
 	 * 
 	 * @param pathname
 	 *            URI to the level map file.
 	 * @throws IOException
 	 */
 	public Level(String pathname) throws IOException {
 		File in = new File(pathname);
 		BufferedReader br = new BufferedReader(new FileReader(in));
 		String line;
 		int j = 0;
 
 		name = in.getName();
 
 		while ((line = br.readLine()) != null) {
 			ArrayList<Box> row = new ArrayList<Box>();
 			map.add(row);
 			++j;
 
 			for (int i = 0; i < line.length(); ++i) {
 				char c = line.charAt(i);
 
 				switch (c) {
     				case '+': row.add(Box.GROUND); break;
     				case '*': row.add(Box.BLOCK); break;
     				case '!': row.add(Box.DOOR); break;
     				case '@': p = new Player(i, j - 1); row.add(Box.PLAYER); break;
     				case ' ': row.add(Box.EMPTY); break;
     				default: throw new IOException("Invalid character in the map file.");
 				}
 			}
 		}
 
 		br.close();
 
 		if (p == null) {
 			throw new IllegalArgumentException("The map file is invalid.");
 		}
 	}
 
 	public void toggle() throws IllegalMovementException {
 		if (map.get(p.getY() - 1).get(p.getX()) == Box.BLOCK) {
 			down();
 			return;
 		}
 		
 		up();
 	}
 
     public void action(Action a) throws Exception {
 		if (!finished) {
 			switch (a) {
 				case TOGGLE: toggle(); break;
     			case LEFT: deplacement(-1); break;
     			case RIGHT: deplacement(1); break;
 			}
 		} else {
 			throw new IllegalMovementException();
 		}
 	}
 
 	private void up() throws IllegalMovementException {
 		int x = p.getX(), y = p.getY();
 		if (map.get(y).get(x + p.getDirection()) == Box.BLOCK
 				&& map.get(y - 1).get(x + p.getDirection()) == Box.EMPTY
 				&& map.get(y - 1).get(x) == Box.EMPTY) {
 			swap(x + p.getDirection(), y, x, y - 1);
 		} else {
 			throw new IllegalMovementException();
 		}
 	}
 
 	private void down() throws IllegalMovementException {
 		if (map.get(p.getY() - 1).get(p.getX() + p.getDirection()) == Box.EMPTY
 				&& map.get(p.getY() - 1).get(p.getX()) == Box.BLOCK) {
 			int y = yChute(p.getX() + p.getDirection(), p.getY() - 1);
 			swap(p.getX(), p.getY() - 1, p.getX() + p.getDirection(), y);
 		} else {
 			throw new IllegalMovementException();
 		}
 	}
 
 	private void deplacement(int d) throws IllegalMovementException,
 			IndexOutOfBoundsException {
 		if (d != p.getDirection()) { // changement direction
 			p.setDirection(d);
 		} else { // deplacement
 			int y = p.getY(), x = p.getX() + p.getDirection();
 			if ((map.get(y - 1).get(x) == Box.EMPTY || map.get(y - 1).get(x) == Box.DOOR)
 					&& (map.get(y - 1).get(p.getX()) == Box.EMPTY || map.get(
 							y - 1).get(p.getX()) == Box.BLOCK)) { // peut monter
 																	// ?
 				--y;
 			} else if (map.get(y).get(x) != Box.EMPTY
 					&& map.get(y).get(x) != Box.DOOR) { // peut à coté ?
 				throw new IllegalMovementException();
 			}
 			y = yChute(x, y); // effecue déplacement
 			if (map.get(y).get(x) == Box.DOOR) {
 				finished = true;
 				map.get(y).set(x, Box.PLAYER_ON_DOOR);
 				map.get(p.getY()).set(p.getX(), Box.EMPTY);
 			} else {
 				swap(p.getX(), p.getY(), x, y);
 
 				if (map.get(p.getY() - 1).get(p.getX()) == Box.BLOCK) { // avait
 																		// un
 																		// bloc
 																		// ?
 					/*if (map.get(p.getY() - 1).get(x) == Box.EMPTY) { // caisse
 																		// peut
 																		// se
 																		// déplacer
 						swap(p.getX(), p.getY() - 1, x, y - 1);*/
 					if(map.get(y - 1).get(x) == Box.EMPTY) {
 						swap(x, y - 1, p.getX(), p.getY() - 1);
 					} else { // caisse coincée donc tombe a la place du player
 						swap(p.getX(), p.getY() - 1, p.getX(), p.getY());
 					}
 				}
 			}
 			p.setMoves(p.getMoves() + 1);
 			p.setX(x);
 			p.setY(y);
 		}
 	}
 
 	private void swap(int x1, int y1, int x2, int y2) {
 		Box temporaire = map.get(y1).get(x1);
 		map.get(y1).set(x1, map.get(y2).get(x2));
 		map.get(y2).set(x2, temporaire);
 	}
 
 	private int yChute(int x, int y) {
 		while (map.get(y + 1).get(x) == Box.EMPTY) {
 			++y;
 		}
		
		if(map.get(y + 1).get(x) == Box.DOOR) {
			++y;
		}
		
 		return y;
 	}
 
 	public boolean isFinished() {
 		return finished;
 	}
 
 	public int getNbMoves() {
 		return p.getMoves();
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public Player getPlayer(){
 		return p;
 	}
 
 	public String toString() {
 		StringBuilder s = new StringBuilder("x : " + p.getX() + ", y:" + p.getY() + "\n");
 		for (ArrayList<Box> bs : map) {
 			for (Box b : bs) {
 				char c = ' ';
 				switch (b) {
     				case BLOCK: c = '*'; break;
     				case PLAYER: c = '@'; break;
     				case EMPTY: c = ' '; break;
     				case PLAYER_ON_DOOR: c = '%'; break;
     				case DOOR: c = '!'; break;
     				case GROUND: c = '+'; break;
 				}
 				s.append(c);
 			}
 			s.append('\n');
 		}
 		return s.toString();
 	}
 
 	public ArrayList<ArrayList<Box>> getMap() {
 		return map;
 	}
 
 }
