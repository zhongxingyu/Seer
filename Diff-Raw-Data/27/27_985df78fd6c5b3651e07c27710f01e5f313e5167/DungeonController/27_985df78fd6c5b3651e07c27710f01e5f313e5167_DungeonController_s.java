 package dungeonGame;
 
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import dungeonGame.DungeonGame.Compass;
 
 public class DungeonController implements KeyListener {
 	
 	private DungeonGame game;
 	@SuppressWarnings("unused")
 	private DungeonView view;
 	private Runnable timer;
 	private final int DELAY = 50;
 	private Shell shell;
 	
 	public DungeonController(Shell s) {
 		shell = s;
 	}
 	
 	public void addModel(DungeonGame g) {
 		this.game = g;
 	}
 
 	public void addView(DungeonView v) {
 		this.view = v;
 	}
 
 	public void startGame() {
 		
 		timer = new Runnable() {
 			public void run() {
 				if(shell.isDisposed()) {
 					System.out.println("Runnable: shell is disposed!");
 					return;
 				}
 				
 				game.runGame();
 				shell.getDisplay().timerExec(DELAY, timer);
 			}
 		};
 		Display.getDefault().timerExec(DELAY, timer);
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		switch(e.keyCode) {
 		case 97:	game.keyFlagsHelper(Compass.WEST, true);
 					break;
 		case 100:	game.keyFlagsHelper(Compass.EAST, true);
 					break;
 		case 119:	game.keyFlagsHelper(Compass.NORTH, true);
 					break;
 		case 115:	game.keyFlagsHelper(Compass.SOUTH, true);
 					break;
 		case 122:	game.attack();
 					break;
 		case 120:
 			for(GameObject obj : game.getFloor().getObjects()) {
 				if(obj instanceof AnimateObject) {
 					System.out.print(obj.getClass().getSimpleName() + " ");
 				}
 			}
 			System.out.println();
 			break;
 		case 99:	System.out.println(e.keyCode);
 					break;
 		default:
 		}
		
 	}
 	@Override
 	public void keyReleased(KeyEvent e) {
 		switch(e.keyCode){
 		case 97:	game.keyFlagsHelper(Compass.WEST, false);
 					break;
 		case 100:	game.keyFlagsHelper(Compass.EAST, false);
 					break;
 		case 119:	game.keyFlagsHelper(Compass.NORTH, false);
 					break;
 		case 115:	game.keyFlagsHelper(Compass.SOUTH, false);
 					break;
 		default:
 		}
 	}
 	
 	
 	
 }
