 import java.awt.event.KeyEvent;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 
 public class Move2 {
 	
 	private static boolean down, left, right = false;
 	private static boolean up = true;
 	
 	JLabel winlabel = new JLabel();
     JPanel winpanel = new JPanel();
     
 	
 	public void keyboard(KeyEvent e) {
 		
 		int key = e.getKeyCode();
 		if ((key == KeyEvent.VK_A) && (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 - 1].laufen)) { // Check, ob das naechste Feld eine Wand ist
 			left = true; // setzt die zutreffende Variable auf true und die anderen auf false
 			right = false;
 			up = false;
 			down = false;
 			
 			System.out.println("links");
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].laufen = true; // setzt das bei dem Feld von dem Bomberwoman kommt laufen = true, damit es wieder zu begehen ist
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/rasen.png")); // Gras an die Stelle, von der Bomberwoman kommt
 			Spielfeld.spalte2--;
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/Bomberwomanleft2.png"));
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].laufen = false; // damit sich die Bomberwomen nicht treffen
 			
 			if (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].endpunkt) { // Endpunktcheck
 				System.out.println("Endpunkt");
 				
 				Spielfeld.panel.removeAll();
 		        winlabel.setIcon(new ImageIcon("images/gewonnen2.png"));
 		        winpanel.add(winlabel);
 		        Spielfeld.frame.add(winpanel);
 		        Spielfeld.frame.revalidate();
 			}
 		}
 		else if ((key == KeyEvent.VK_D) && (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 + 1].laufen)) { // else if, damit das Programm nicht jede if Bedingung einzeln durchgehen muss
 			left = false;
 			right = true;
 			up = false;
 			down = false;
 			
 			System.out.println("rechts");
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].laufen = true;
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/rasen.png"));
 			Spielfeld.spalte2++; // spalte wird um 1 erhöht
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/Bomberwomanright2.png"));
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].laufen = false;
 			
 			if (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].endpunkt) { // Endpunktcheck
 				System.out.println("Endpunkt");
 				
 				Spielfeld.panel.removeAll();
 		        winlabel.setIcon(new ImageIcon("images/gewonnen2.png"));
 		        winpanel.add(winlabel);
 		        Spielfeld.frame.add(winpanel);
 		        Spielfeld.frame.revalidate();
 			}
 		}
 		else if ((key == KeyEvent.VK_W) && (Spielfeld.feld[Spielfeld.zeile2 - 1][Spielfeld.spalte2].laufen)) {
 			left = false;
 			right = false;
 			up = true;
 			down = false;
 			
 			System.out.println("hoch");
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].laufen = true;
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/rasen.png"));
 			Spielfeld.zeile2--; // zeile wird um 1 verringert
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/Bomberwomanback2.png"));
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].laufen = false;
 			
 			if (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].endpunkt) { // Endpunktcheck
 				System.out.println("Endpunkt");
 				
 				Spielfeld.panel.removeAll();
 		        winlabel.setIcon(new ImageIcon("images/gewonnen2.png"));
 		        winpanel.add(winlabel);
 		        Spielfeld.frame.add(winpanel);
 		        Spielfeld.frame.revalidate();
 			}
 		}
 		else if ((key == KeyEvent.VK_S) && (Spielfeld.feld[Spielfeld.zeile2 + 1][Spielfeld.spalte2].laufen)) {
 			left = false;
 			right = false;
 			up = false;
 			down = true;
 			
 			System.out.println("runter");
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].laufen = true;
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/rasen.png"));
 			Spielfeld.zeile2++;
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].setIcon(new ImageIcon("images/Bomberwoman2.png"));
 			Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].laufen = false;
 			
 			if (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2].endpunkt) { // Endpunktcheck
 				System.out.println("Endpunkt");
 				
 				Spielfeld.panel.removeAll();
 		        winlabel.setIcon(new ImageIcon("images/gewonnen2.png"));
 		        winpanel.add(winlabel);
 		        Spielfeld.frame.add(winpanel);
 		        Spielfeld.frame.revalidate();
 			}
 		}
 		else if (key == KeyEvent.VK_Y) {
			if ((up) && (Spielfeld.feld[Spielfeld.zeile2 - 1][Spielfeld.spalte2].laufen) && (Spielfeld.feld[Spielfeld.zeile2 - 1][Spielfeld.spalte2].endpunkt == false)) { // check ob vor der Bomberwoman Rasen ist
 				System.out.println("Bombe");
 				Spielfeld.feld[Spielfeld.zeile2 - 1][Spielfeld.spalte2].setIcon(new ImageIcon("images/PropraBombe.png")); // setze Bombe auf Rasen
 				Spielfeld.feld[Spielfeld.zeile2 - 1][Spielfeld.spalte2].laufen = false; // dort wo die Bombe liegt wird laufen = false
 			}
			else if ((down) && (Spielfeld.feld[Spielfeld.zeile2 +1][Spielfeld.spalte2].laufen) && (Spielfeld.feld[Spielfeld.zeile2 + 1][Spielfeld.spalte2].endpunkt == false)) {
 					System.out.println("Bombe");
 					Spielfeld.feld[Spielfeld.zeile2 +1][Spielfeld.spalte2].setIcon(new ImageIcon("images/PropraBombe.png"));
 					Spielfeld.feld[Spielfeld.zeile2 +1][Spielfeld.spalte2].laufen = false;
 			}
			else if ((left) && (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 - 1].laufen) && (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 - 1].endpunkt == false)) {
 						System.out.println("Bombe");
 						Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 - 1].setIcon(new ImageIcon("images/PropraBombe.png"));
 						Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 - 1].laufen = false;
 			}
			else if ((right) && (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 + 1].laufen) && (Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 + 1].endpunkt == false)) {
 							System.out.println("Bombe");
 							Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 + 1].setIcon(new ImageIcon("images/PropraBombe.png"));
 							Spielfeld.feld[Spielfeld.zeile2][Spielfeld.spalte2 + 1].laufen = false;
 			}
 		}
 		else {
 			System.out.println("Wand / Spielfigur im Weg oder falsche Taste");}
 		}	
 
 	
 	}
