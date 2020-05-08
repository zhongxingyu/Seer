 package main.java;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 public class Control implements KeyListener {
 	PlayerAction playeraction;
 
 	Control(Game game) {
 		this.playeraction = game.playeraction;
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		int keyCode = e.getKeyCode();
 
 		// Check for KeyCode after event
 		switch (keyCode) {
 		case KeyEvent.VK_ESCAPE:
 			System.out.print("pressed: ESC - System.Exit");
 			System.exit(0);
 			break;
 		// Movement
 		case KeyEvent.VK_UP:
 			// System.out.println("pressed: UP");
 			this.playeraction.move(1);
 			break;
 		case KeyEvent.VK_RIGHT:
 			// System.out.println("pressed: RIGHT");
 			this.playeraction.move(2);
 			break;
 		case KeyEvent.VK_DOWN:
 			// System.out.println("pressed: DOWN");
 			this.playeraction.move(3);
 			break;
 		case KeyEvent.VK_LEFT:
 			// System.out.println("pressed: LEFT");
 			this.playeraction.move(4);
 			break;
 		// Spells
 		case KeyEvent.VK_Q:
 			System.out.println("pressed: Q - Spell 1");
 			break;
 		case KeyEvent.VK_W:
 			System.out.println("pressed: W - Spell 2");
 			break;
 		case KeyEvent.VK_E:
 			System.out.println("pressed: E - Spell 3");
 			break;
 		case KeyEvent.VK_R:
 			System.out.println("pressed: R - Spell 4");
 			break;
 		case KeyEvent.VK_D:
 			System.out.println("pressed: D - Spell 5");
 			break;
 		case KeyEvent.VK_F:
 			System.out.println("pressed: F - Spell 6");
 			break;
 
 		case KeyEvent.VK_X:
 			//System.out.println("pressed: X - Item aufgenommen!");
 			this.playeraction.pickUpItem();
 			break;
 		case KeyEvent.VK_B:
			//System.out.println("pressed: B - Zeige Inventory");
 			this.playeraction.openInventory();
 			break;
 		default:
 			System.out.println("pressed");
 			break;
 		}
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// no code for this function yet.
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// no code for this function yet.
 	}
 }
