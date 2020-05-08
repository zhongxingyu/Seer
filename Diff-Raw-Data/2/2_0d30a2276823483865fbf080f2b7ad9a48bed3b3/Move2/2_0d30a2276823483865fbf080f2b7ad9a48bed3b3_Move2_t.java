 import java.awt.event.KeyEvent;
 import javax.swing.ImageIcon;
 import java.util.*;
 
 public class Move2 {
 	Timer timer = new Timer();
 
 	private static boolean up = false;
 	private static boolean left = false;
 	private static boolean right = false;
 	private static boolean down = true;
 
 	public void keyboard(KeyEvent e) {
 		int key = e.getKeyCode();
 
 		if((key == KeyEvent.VK_A) && (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 - 1].laufen)) {
 			if(left) {
 				Blockeigenschaft.rasen(Spielfeld.zeile2, Spielfeld.spalte2);
 				Spielfeld.spalte2--;
 				if(Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].explosion == true) { 
 					Blockeigenschaft.explosion(Spielfeld.zeile2, Spielfeld.spalte2);
 					Funktion.gewinner1();
 				}
 				else Blockeigenschaft.spieler2(Spielfeld.zeile2, Spielfeld.spalte2, "left");
 
 				if(Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].endpunkt) Funktion.gewinner2();
 			}
 
 			else {
 				left = true;
 				right = false;
 				up = false;
 				down = false;
 				Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/Bomberwomanleft2.png"));
 			}
 		}
 
 		else if((key == KeyEvent.VK_D) && (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 + 1].laufen)) {
 			if(right) {
 				Blockeigenschaft.rasen(Spielfeld.zeile2, Spielfeld.spalte2);
 				Spielfeld.spalte2++;
 				if(Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].explosion == true) { 
 					Blockeigenschaft.explosion(Spielfeld.zeile2, Spielfeld.spalte2);
 					Funktion.gewinner1();
 				}
 				else Blockeigenschaft.spieler2(Spielfeld.zeile2, Spielfeld.spalte2, "right");
 
 				if(Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].endpunkt) Funktion.gewinner2();
 			}
 
 			else {
 				left = false;
 				right = true;
 				up = false;
 				down = false;
				Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/Bomberwomanright2.png"));
 			}
 		}
 
 		else if((key == KeyEvent.VK_W) && (Spielfeld.feld[Spielfeld.zeile2 - 1][Spielfeld.spalte2].laufen)) {
 			if(up) {
 				Blockeigenschaft.rasen(Spielfeld.zeile2, Spielfeld.spalte2);
 				Spielfeld.zeile2--;
 				if(Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].explosion == true) { 
 					Blockeigenschaft.explosion(Spielfeld.zeile2, Spielfeld.spalte2);
 					Funktion.gewinner1();
 				}
 				else Blockeigenschaft.spieler2(Spielfeld.zeile2, Spielfeld.spalte2, "up");
 
 				if(Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].endpunkt) Funktion.gewinner2();
 			}
 
 			else {
 				left = false;
 				right = false;
 				up = true;
 				down = false;
 				Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/Bomberwomanback2.png"));
 			}
 		}
 
 		else if((key == KeyEvent.VK_S) && (Spielfeld.feld[Spielfeld.zeile2 + 1][Spielfeld.spalte2].laufen)) {
 			if(down) {
 				Blockeigenschaft.rasen(Spielfeld.zeile2, Spielfeld.spalte2);
 				Spielfeld.zeile2++;
 				if(Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].explosion == true) { 
 					Blockeigenschaft.explosion(Spielfeld.zeile2, Spielfeld.spalte2);
 					Funktion.gewinner1();
 				}
 				else Blockeigenschaft.spieler2(Spielfeld.zeile2, Spielfeld.spalte2, "down");
 
 				if(Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].endpunkt) Funktion.gewinner2();
 			}
 
 			else {
 				left = false;
 				right = false;
 				up = false;
 				down = true;
 				Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/Bomberwoman2.png"));
 			}
 		}
 
 		else if(key == KeyEvent.VK_Y) {
 			if((up) && (Spielfeld.feld[Spielfeld.zeile2 - 1][Spielfeld.spalte2].laufen)
 					&& (Spielfeld.feld[Spielfeld.zeile2 - 1][Spielfeld.spalte2].endpunkt == false)) {
 				Blockeigenschaft.bombe2(Spielfeld.zeile2 - 1, Spielfeld.spalte2);
 				timer.schedule(new BombeTask(3, Spielfeld.zeile2 - 1, Spielfeld.spalte2), 2000);
 			}
 			else if((down) && (Spielfeld.feld[Spielfeld.zeile2 + 1][Spielfeld.spalte2].laufen)
 					&& (Spielfeld.feld[Spielfeld.zeile2 + 1][Spielfeld.spalte2].endpunkt == false)) {
 				Blockeigenschaft.bombe2(Spielfeld.zeile2 + 1, Spielfeld.spalte2);
 				timer.schedule(new BombeTask(3, Spielfeld.zeile2 + 1, Spielfeld.spalte2), 2000);
 			}
 			else if((left) && (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 - 1].laufen)
 					&& (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 - 1].endpunkt == false)) {
 				Blockeigenschaft.bombe2(Spielfeld.zeile2, Spielfeld.spalte2 - 1);
 				timer.schedule(new BombeTask(3, Spielfeld.zeile2, Spielfeld.spalte2 - 1), 2000);
 			}
 			else if((right) && (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 + 1].laufen)
 					&& (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 + 1].endpunkt == false)) {
 				Blockeigenschaft.bombe2(Spielfeld.zeile2, Spielfeld.spalte2 + 1);
 				timer.schedule(new BombeTask(3, Spielfeld.zeile2, Spielfeld.spalte2 + 1), 2000);
 			}
 		}
 	}
 }
