 package dungeonGame;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 public class DungeonMVC {
 
 	private Shell shell;
 	public DungeonMVC(Display display) {
 		//DungeonGame game = new DungeonGame(); // inside DungeonView Constructor
 		
 		shell = new Shell(display);
 		shell.setText("Dungeon");
 		
 		DungeonView view = new DungeonView(shell);
 		
 		//game.addObserver(view); // inside DungeonView Constructor
 		
		DungeonController controller = new DungeonController(shell);
 		controller.addModel(view.getGame());
 		controller.addView(view);
 		//game.updateGame(); // inside DungeonView Constructor
 		
 		view.addController(controller);
 		
 		shell.pack();
 		shell.open();
 		
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) display.sleep();
 		}
 	}
 	
 	public static void main(String[] args) {
 		Display display = new Display();
 		new DungeonMVC(display);
 		display.dispose();
 	}
 
 }
