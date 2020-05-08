 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 
 public class Slidepuzzle {
 
 	private static final String infilename = "/path/to/devquiz2011/slidepuzzle/slidepannel.txt";
	private static final String mergedfile = "//path/to/devquiz2011/slidepuzzle/merged-2957.txt";
 	private static int gameCounts;
 	private static int [] moveLimits;
 	private static Deque <Game> gameQue;
 	private static HashMap <Integer, Deque<Phase>> forwardGameMap;
 	private static HashMap <Integer, Deque<Phase>> reverseGameMap;
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		int solved = 0;
 		BufferedReader in = new BufferedReader(new FileReader(infilename));
 		String header = in.readLine();
 		String limits[] = header.split(" ");
 		
 		moveLimits = new int[limits.length];
 		for(int i=0 ; i<limits.length ; i++)
 			moveLimits[i] = Integer.parseInt(limits[i]);
 		String maxCount = in.readLine();
 		gameCounts = Integer.parseInt(maxCount);
 		
 		gameQue = new LinkedList <Game> ();
 		forwardGameMap = new HashMap <Integer, Deque<Phase>> ();
 		reverseGameMap = new HashMap <Integer, Deque<Phase>> ();
 		
 		BufferedReader solvedgames = new BufferedReader(new FileReader(mergedfile));
 		
 		for (int i=0 ; i< gameCounts ; i++) {
 			Game game;
 			String sgame = in.readLine();
 			String agame[] = sgame.split(",");
 			int x = Integer.parseInt(agame[0]); 
 			int y = Integer.parseInt(agame[1]);
 			
 			String solvedanswer = solvedgames.readLine();
 			if (solvedanswer.length() != 0) {
 //				System.out.println(solvedanswer);
 //				continue;
 			}
 			
 			if (x < 8 && y < 8) {
 				gameQue = new LinkedList <Game> ();
 				forwardGameMap = new HashMap <Integer, Deque<Phase>> ();
 				reverseGameMap = new HashMap <Integer, Deque<Phase>> ();
 				game = new Game(x, y, agame[2]);
 				gameQue.offer(game);
 				game = new Game(game);
 				System.err.println(game.toString());
 				if (game.sizeX < 5 || game.sizeY < 5) {
 					game.reverse();
 					gameQue.offer(game);
 				}
 				long start = System.currentTimeMillis();
 				boolean bsolved = true;
 				while (!solver()) {
 					if (System.currentTimeMillis() - start > x*y*1000 ||
 							gameQue.size() == 0) {
 						System.out.println();
 						if (gameQue.size() == 0)
 							System.err.println("sold out");
 						else
 							System.err.println("time out");
 						bsolved = false;
 						break;
 					}
 				}
 				if (bsolved) {
 					solved++;
 					System.err.println("solved " + solved + "/" + (i+1) +
 						" in " + (System.currentTimeMillis() - start) + "msec");
 				}
 			}
 			else
 				System.out.println();
 		}
 		System.out.println("solved: " + solved);
 	}
 	
 	private static boolean solver() {
 		Game game = gameQue.poll();
 		if (game.solved() || twoway(game)) {
 			System.out.println(game.getMoves());
 			System.out.flush();
 			return true;
 		}
 		
 		if (game.isShrinkableTop() || game.isShrinkableLeft()) {
 			while (game.isShrinkableTop())
 				game.shrinkTop();
 			while (game.isShrinkableLeft())
 				game.shrinkLeft();
 			gameQue = new LinkedList <Game> ();
 			forwardGameMap = new HashMap <Integer, Deque<Phase>> ();
 			reverseGameMap = new HashMap <Integer, Deque<Phase>> ();
 			gameQue.offer(game);
 			Game r = new Game(game);
 			r.reverse();
 			gameQue.offer(r);
 			return false;
 		}
 		
 		for (int i=0 ; i<4 ; i++) {
 			Game g = new Game(game, i);
 			if (g.isvalid && !isdup(g)) {
 				if (g.manhattan < game.manhattan)
 					gameQue.offerFirst(g);
 				else
 					gameQue.offer(g);
 			}
 		}
 		return false;
 	}
 	
 	private static boolean twoway(Game game) {
 		HashMap <Integer, Deque<Phase>> m;
 		Integer key = new Integer(game.phase.problem.hashCode());
 
 		if (game.direction == game.direction_forward)
 			m = reverseGameMap;
 		else
 			m = forwardGameMap;
 
 		if (m.containsKey(key)) {
 			for (Phase s : m.get(key)) {
 				if (game.phase.problem.equals(s.problem)) {
 					String forwardmoves;
 					String reversemoves;
 					if (game.direction == game.direction_forward) {
 						forwardmoves = new String(game.phase.moves);
 						reversemoves = new String(s.moves);
 						
 					}
 					else {
 						forwardmoves = new String(s.moves);
 						reversemoves = new String(game.phase.moves);
 					}
 					for (int i=0 ; i<reversemoves.length() ; i++) {
 						switch(reversemoves.charAt(reversemoves.length() - 1 - i)) {
 						case 'U': forwardmoves += 'D'; break;
 						case 'D': forwardmoves += 'U'; break;
 						case 'L': forwardmoves += 'R'; break;
 						case 'R': forwardmoves += 'L'; break;
 						}
 					}
 					game.phase.moves = new String(forwardmoves);
 					System.err.println("twoway");
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	private static boolean isdup(Game game) {
 		Integer key = new Integer(game.phase.problem.hashCode());
 		HashMap <Integer, Deque<Phase>> m;
 		
 		if (game.direction == game.direction_forward)
 			m = forwardGameMap;
 		else
 			m = reverseGameMap;
 			
 		if (m.containsKey(key)) {
 			for (Phase s : m.get(key)) {
 				if (game.phase.problem.equals(s.problem))
 					return true;
 			}
 			m.get(key).offer(game.phase);
 		}
 		else {
 			Deque <Phase> q = new LinkedList <Phase> ();
 			q.offer(game.phase);
 			m.put(key, q);
 		}
 		return false;
 	}
 	
 	private static class Phase {
 		private String problem;
 		private String moves;
 	}
 	
 	private static class Game {
 		private final char [] chars = {'1', '2', '3', '4', '5', '6', '7', '8', '9',
 											'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 
 											'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
 											'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0'};
 		private int sizeX;
 		private int sizeY;
 		private int posZero;
 		private int direction;
 		private int step;
 		private int manhattan;
 		private boolean isvalid;
 		private Phase phase;
 		private String answer;
 		
 		private final int move_up    = 0;
 		private final int move_left  = 1;
 		private final int move_right = 2;
 		private final int move_down  = 3;
 		private final int direction_forward = 0;
 		private final int direction_reverse = 1;
 		
 		Game(Game g) {
 			phase = new Phase();
 			phase.problem = new String(g.phase.problem);
 			phase.moves   = new String(g.phase.moves);
 			sizeX     = g.sizeX;
 			sizeY     = g.sizeY;
 			answer    = new String(g.answer);
 			isvalid   = g.isvalid;
 			direction = g.direction;
 			posZero   = phase.problem.indexOf('0');
 			manhattan = g.manhattan;
 			step      = g.step;
 		}
 
 		Game(Game g, int move) {
 			phase = new Phase();
 			phase.problem = new String(g.phase.problem);
 			phase.moves   = new String(g.phase.moves);
 			sizeX     = g.sizeX;
 			sizeY     = g.sizeY;
 			answer    = new String(g.answer);
 			direction = g.direction;
 			posZero   = phase.problem.indexOf('0');
 			switch (move) {
 			case move_up:    isvalid = moveUp();    break;
 			case move_down:  isvalid = moveDown();  break;
 			case move_left:  isvalid = moveLeft();  break;
 			case move_right: isvalid = moveRight(); break;
 			}
 			step = g.step + 1;
 			manhattan = distance();
 		}
 
 		Game(int x, int y, String s) {
 			sizeX = x;
 			sizeY = y;
 			phase = new Phase();
 			phase.problem = new String(s);
 			posZero = phase.problem.indexOf('0');
 			answer = new String();
 			for (int i=0 ; i<chars.length ; i++) {
 				while (phase.problem.charAt(answer.length()) == '=')
 					answer += '=';
 				if (phase.problem.indexOf(chars[i]) != -1 )
 					answer += chars[i];
 			}
 			phase.moves = new String();
 			direction = direction_forward;
 			isvalid = true;
 			step = 0;
 			manhattan = distance();
 		}
 		
 		private boolean isShrinkableTop() {
 			if (direction == direction_reverse || sizeY < 4)
 				return false;
 			String top = phase.problem.substring(0, sizeX);
 			String ans = answer.substring(0, sizeX);
 			return top.equals(ans);
 		}
 		
 		private boolean isShrinkableLeft() {
 			if (direction == direction_reverse || sizeX < 4)
 				return false;
 			for (int i = 0 ; i < sizeY ; i++) {
 				if (phase.problem.charAt(sizeX * i) != answer.charAt(sizeX * i))
 					return false;
 			}
 			return true;
 		}
 		
 		private Game shrinkTop() {
 			System.err.println("shrink top");
 			phase.problem = phase.problem.substring(sizeX);
 			answer = answer.substring(sizeX);
 			sizeY -= 1;
 			posZero = phase.problem.indexOf('0');
 			manhattan = distance();
 			return this;
 		}
 		
 		private Game shrinkLeft() {
 			System.err.println("shrink left");
 			String p = new String();
 			String q = new String();
 			for (int i = 0 ; i<phase.problem.length() ; i++) {
 				if (i%sizeX != 0) {
 					p += phase.problem.charAt(i);
 					q += answer.charAt(i);
 				}
 			}
 			phase.problem = p;
 			answer = q;
 			sizeX -= 1;
 			posZero = phase.problem.indexOf('0');
 			manhattan = distance();
 			return this;
 		}
 		
 		private String swap0(int pos) {
 			String s = new String();
 			s = phase.problem.replace('0', ' ');
 			s = s.replace(phase.problem.charAt(pos), '0');
 			s = s.replace(' ', phase.problem.charAt(pos));
 			return s;
 		}
 		
 		private boolean moveLeft() {
 			if (posZero % sizeX == 0 ||
 				phase.problem.charAt(posZero - 1) == '=')
 				return false;
 			phase.problem = swap0((posZero - 1));
 			posZero = phase.problem.indexOf('0');
 			phase.moves += 'L';
 			return true;
 		}
 		
 		private boolean moveRight() {
 			if (posZero % sizeX == sizeX - 1 ||
 				phase.problem.charAt(posZero + 1) == '=')
 				return false;
 			phase.problem = swap0(posZero + 1);
 			posZero = phase.problem.indexOf('0');
 			phase.moves += 'R';
 			return true;
 		}
 		
 		private boolean moveUp() {
 			if (posZero < sizeX ||
 				phase.problem.charAt(posZero - sizeX) == '=')
 				return false;
 			phase.problem = swap0(posZero - sizeX);
 			posZero = phase.problem.indexOf('0');
 			phase.moves += 'U';
 			return true;
 		}
 		
 		private boolean moveDown() {
 			if (posZero >= sizeX * (sizeY - 1) ||
 				phase.problem.charAt(posZero + sizeX) == '=')
 				return false;
 			phase.problem = swap0(posZero + sizeX);
 			posZero = phase.problem.indexOf('0');
 			phase.moves+= 'D';
 			return true;
 		}
 		
 		public String toString() {
 			String s = "(" + sizeX + ", " + sizeY + ") "
 						+ phase.problem;
 			return s;
 		}
 		
 		public String getMoves() {
 			return phase.moves;
 		}
 		
 		public int goback() {
 			if (phase.moves.length() == 0)
 				return -1;
 			switch (phase.moves.charAt(phase.moves.length() - 1)) {
 			case 'U': return move_down;
 			case 'D': return move_up;
 			case 'L': return move_right;
 			case 'R': return move_left;
 			default: return -1;
 			}
 		}
 		
 		public String getStatus() {
 			return phase.problem;
 		}
 		
 		public void reverse() {
 			String s = phase.problem;
 			phase.problem = answer;
 			answer = s;
 			posZero = phase.problem.indexOf('0');
 			phase.moves = new String();
 			direction = direction_reverse;
 		}
 		
 		public boolean solved() {
 			if (answer.equals(phase.problem))
 				return true;
 			else
 				return false;
 		}
 		
 		private int distance() {
 			manhattan = 0;
 			for (int i = 0 ; i < phase.problem.length() ; i++) {
 				if (phase.problem.charAt(i) == '0')
 					continue;
 				int a = answer.indexOf(phase.problem.charAt(i));
 				if (direction == direction_forward && 
 						(i < sizeX || i % sizeX == 0))
 					manhattan += Math.abs(a - i);
 				else
 					manhattan += Math.abs(a - i) * 2;
 			}
 			manhattan += step;
 			return manhattan;
 		}
 
 		private int distance0() {
 			manhattan = 0;
 			for (int i = 0 ; i < phase.problem.length() ; i++) {
 				if (phase.problem.charAt(i) == '0')
 					continue;
 				int a = answer.indexOf(phase.problem.charAt(i));
 				manhattan += Math.abs(a - i);
 			}
 			manhattan += step;
 			return manhattan;
 		}
 }
 }
 
 /* $ cd bin
  * $ jar cvfm ../Slidepuzzle.jar ../META-INF/MANIFEST.MF *.class
  */
