 package vooga.towerdefense.model;
 
import java.awt.Graphics2D;
 
 
 public class Game {
 	private final int TICKS_PER_SECOND = 50;
 	private final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
 	private final int MAX_FRAMESKIP = 10;
 
 	private boolean game_is_running = false;
 	private GameModel myModel;
	private Graphics2D myPen;
 
 	public Game() {
 		// TODO: functions to construct model from file. Probably put that in
 		// GameModel constructor.
		myModel = new GameModel(null, null);
 	}
 
	public void start(Graphics2D pen) {
		myPen = pen;
 		setRunning(true);
 		run();
 	}
 
 	public void run() {
 		// this game loop will update the game at up to TICKS_PER_SECOND, and
 		// repaint the screen as fast as possible.
 		long next_game_tick = System.currentTimeMillis();
 
 		int loops;
 
 		while (game_is_running) {
 			loops = 0;
 			while (System.currentTimeMillis() > next_game_tick
 					&& loops < MAX_FRAMESKIP) {
 				myModel.update(System.currentTimeMillis() - next_game_tick);
 				next_game_tick += SKIP_TICKS;
 				loops++;
 			}
			myModel.display(myPen);
 		}
 
 	}
 
 	public void setRunning(boolean isRunning) {
 		game_is_running = isRunning;
 		run();
 	}
 }
