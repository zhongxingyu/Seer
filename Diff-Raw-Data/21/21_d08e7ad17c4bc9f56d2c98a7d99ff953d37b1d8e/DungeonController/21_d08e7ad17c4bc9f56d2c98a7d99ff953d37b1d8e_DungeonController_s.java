 package dungeonGame;
 
import java.util.ArrayList;

import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 import dungeonGame.DungeonGame.Compass;
 
 public class DungeonController implements KeyListener {
 
 	private DungeonGame game;
 	private DungeonView view;
	private Shell shell;
 	
	public DungeonController(Shell shell) {
		this.shell = shell;
	}	
 	
 	public void addModel(DungeonGame g) {
 		this.game = g;
 	}
 
 	public void addView(DungeonView v) {
 		this.view = v;		
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		
 		if(e.keyCode == 97) {
 			game.desiredMove(Compass.WEST, game.getHero());
 		}
 		else if(e.keyCode == 100) {
 			game.desiredMove(Compass.EAST, game.getHero());
 		}
 		else if(e.keyCode == 119) {
 			game.desiredMove(Compass.NORTH, game.getHero());
 		}
 		else if(e.keyCode == 115) {
 			game.desiredMove(Compass.SOUTH, game.getHero());
 		}
 		else if(e.keyCode == 122) { // Z
 			System.out.println("attacking");
 			game.attack();
 		}
 		else if(e.keyCode == 120) { // X
 			System.out.println(e.keyCode);
 		}
 		else if(e.keyCode == 99) { // C
 			System.out.println(e.keyCode);
 		}
 	}
 	@Override
 	public void keyReleased(KeyEvent e) {}
 	
 }
