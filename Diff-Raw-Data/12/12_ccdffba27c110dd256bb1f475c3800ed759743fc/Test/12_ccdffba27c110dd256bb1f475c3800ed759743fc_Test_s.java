 import java.util.TimerTask;
 import java.util.Timer;
 
 public class Test extends TimerTask {
 	private static final long PROGTIME = 9500; // 10 Seconds minus some time for cleaning up/output
	private static final long NUMGAMES = 3;
 	private static Timer timer;
 	/*
 	Standard Konstruktor f�r 4x4 Labyrinth;
 	boolean [][][]lab = {
 		{{false, false}, {false, false}, {false, false}, {false, false}},
 		{{false, false}, {false, false}, {false, false}, {false, false}},
 		{{false, false}, {false, false}, {false, false}, {false, false}},
 		{{false, false}, {false, false}, {false, false}, {false, false}},
 	};
 	*/
 	private boolean [][][]lab = {
 			{{true , true }, {true , false}, {true , false}, {true , true }},
 			{{false, false}, {true , true }, {false, false}, {false, false}},
 			{{true , false}, {false, false}, {true , false}, {false, true }},
 			{{false, true }, {false, false}, {false, false}, {true , false}},
 		};
 	boolean [][][]lab2 = {
 			{{true , true }, {true , false}, {true , true }},
 			{{false, true }, {false, true }, {false, false}},
 			{{false, false}, {false, true }, {false, true }},
 		};
 	boolean [][][]lab3 = {
 			{{true , true }, {true , false}, {true , false}, {true , false}, {true , false}},
 			{{false, false}, {false, true }, {false, false}, {false, true }, {false, true }},
 			{{true , true }, {false, false}, {true , false}, {false, true }, {true , true }},
 			{{false, false}, {false, false}, {true , false}, {false, false}, {false, true }},
 			{{false, false}, {true , false}, {true , false}, {true , true }, {false, false}},
 		};
 	private Game g1;
 	private Game g2;
 	private Game g3;
 	private int deadgames = 0;
 	
 	
 	public static void main(String[] args) {
 		Test t = new Test();
 		timer = new Timer();
 		// Starting timer
 		timer.schedule(t, PROGTIME);
 		
 		t.start1();
 		
 		while (t.isRunning()) {
 			try {
 				Thread.sleep(50);
 			} catch (InterruptedException e) {
 				// Continue
 			}
 		}
 	}
 	
 	public static void test1() {
 		
 	}
 	
 	public Test() {
 		// Game 1
		g1 = new Game(lab, 25);;
 		g1.createHunter(0, 0, "Ben");
 		g1.createHunter(1, 1, "Sep");
 		g1.createHunter(2, 0, "Moni");
 		g1.createGhost(3, 3);
 		g1.createGhost(2, 2);
 		
 		// Game 2
 		g2 = new Game(lab, 25);;
 		g2.createHunter(1, 1, "Ben");
 		g2.createGhost(0, 0);
 		
 		// Game 3
 		g2 = new Game(lab, 25);
 		g1.createHunter(0, 0, "Sep");
 		g1.createHunter(4, 4, "Moni");
 		g1.createGhost(3, 3);
 		g2.createGhost(1, 0);
 	}
 
 	public void start1() {
 		System.out.println("Starting Game1.");
 		g1.startGame();
 	}
 
 	public void start2() {
 		System.out.println("Starting Game2.");
 		g2.startGame();
 	}
 
 	public void start3() {
 		System.out.println("Starting Game3.");
 		g3.startGame();
 	}
 	
 	public boolean isRunning() {
 		if (g1.getState() == Game.State.FINISHED) {
 			deadgames++;
 			g2.startGame();
 		}else if(g2.getState() == Game.State.FINISHED) {
 			deadgames++;
 			g3.startGame();
 		}else if (g3.getState() == Game.State.FINISHED) {
 			deadgames++;
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public void run() {
 		g1.endGame();
 		g2.endGame();
 		g3.endGame();
 		System.exit(0);
 	}
 }
