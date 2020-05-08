 import java.awt.event.KeyEvent;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 
 public class Move {
 	
 	private static boolean up, left, right = false;
 	private static boolean down = true;
 
 	JLabel winlabel = new JLabel();
     JPanel winpanel = new JPanel();
     
     
 	public void keyboard(KeyEvent e) {
 		
 		int key = e.getKeyCode();
 		if ((key == KeyEvent.VK_LEFT) && (Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte - 1].laufen)) { // Check, ob das naechste Feld eine Wand ist
 			left = true; // setzt die zutreffende Variable auf true und die anderen auf false
 			right = false;
 			up = false;
 			down = false;
 			
 			System.out.println("links");
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].laufen = true; // setzt das bei dem Feld von dem Bomberwoman kommt laufen = true, damit es wieder zu begehen ist
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].setIcon(new ImageIcon("images/rasen.png")); // Gras an die Stelle, von der Bomberwoman kommt
 			Spielfeld.spalte--;
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].setIcon(new ImageIcon("images/Bomberwomanleft.png"));
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].laufen = false; // damit sich die Bomberwomen nicht treffen
 			
 			if (Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].endpunkt) { // Endpunktcheck
 				System.out.println("Endpunkt");
 				
 				Spielfeld.panel.removeAll();
 		        winlabel.setIcon(new ImageIcon("images/gewonnen1.png"));
 		        winpanel.add(winlabel);
 		        Spielfeld.frame.add(winpanel);
 		        Spielfeld.frame.revalidate();
 			}
 		}
 		else if ((key == KeyEvent.VK_RIGHT) && (Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte + 1].laufen)) { // else if, damit das Programm nicht jede if Bedingung einzeln durchgehen muss
 			left = false;
 			right = true;
 			up = false;
 			down = false;
 			
 			System.out.println("rechts");
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].laufen = true;
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].setIcon(new ImageIcon("images/rasen.png"));
 			Spielfeld.spalte++; // spalte wird um 1 erhöht
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].setIcon(new ImageIcon("images/Bomberwomanright.png"));
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].laufen = false;
 			
 			if (Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].endpunkt) { // Endpunktcheck
 				System.out.println("Endpunkt");
 				
 				Spielfeld.panel.removeAll();
 		        winlabel.setIcon(new ImageIcon("images/gewonnen1.png"));
 		        winpanel.add(winlabel);
 		        Spielfeld.frame.add(winpanel);
 		        Spielfeld.frame.revalidate();
 			}
 		}
 		else if ((key == KeyEvent.VK_UP) && (Spielfeld.feld[Spielfeld.zeile - 1][Spielfeld.spalte].laufen)) {
 			left = false;
 			right = false;
 			up = true;
 			down = false;
 			
 			System.out.println("hoch");
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].laufen = true;
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].setIcon(new ImageIcon("images/rasen.png"));
 			Spielfeld.zeile--; // zeile wird um 1 verringert
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].setIcon(new ImageIcon("images/Bomberwomanback.png"));
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].laufen = false;
 			
 			if (Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].endpunkt) { // Endpunktcheck
 				System.out.println("Endpunkt");
 				
 				Spielfeld.panel.removeAll();
 		        winlabel.setIcon(new ImageIcon("images/gewonnen1.png"));
 		        winpanel.add(winlabel);
 		        Spielfeld.frame.add(winpanel);
 		        Spielfeld.frame.revalidate();      
 			}
 		}
 		else if ((key == KeyEvent.VK_DOWN) && (Spielfeld.feld[Spielfeld.zeile + 1][Spielfeld.spalte].laufen)) {
 			left = false;
 			right = false;
 			up = false;
 			down = true;
 			
 			System.out.println("runter");
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].laufen = true;
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].setIcon(new ImageIcon("images/rasen.png"));
 			Spielfeld.zeile++;
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].setIcon(new ImageIcon("images/Bomberwoman.png"));
 			Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].laufen = false;
 			
 			if (Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte].endpunkt) { // Endpunktcheck
 				System.out.println("Endpunkt");
 				
 				Spielfeld.panel.removeAll();
 		        winlabel.setIcon(new ImageIcon("images/gewonnen1.png"));
 		        winpanel.add(winlabel);
 		        Spielfeld.frame.add(winpanel);
 		        Spielfeld.frame.revalidate();
 			}
 		}
 		else if (key == 32) {
			if ((up) && (Spielfeld.feld[Spielfeld.zeile - 1][Spielfeld.spalte].laufen) && (Spielfeld.feld[Spielfeld.zeile - 1][Spielfeld.spalte].endpunkt == false)) {
 				System.out.println("Bombe");
 				Spielfeld.feld[Spielfeld.zeile - 1][Spielfeld.spalte].setIcon(new ImageIcon("images/PropraBombe.png"));
 				Spielfeld.feld[Spielfeld.zeile - 1][Spielfeld.spalte].laufen = false;
 			}
			else if ((down) && (Spielfeld.feld[Spielfeld.zeile +1][Spielfeld.spalte].laufen) && (Spielfeld.feld[Spielfeld.zeile + 1][Spielfeld.spalte].endpunkt == false)) {
 					System.out.println("Bombe");
 					Spielfeld.feld[Spielfeld.zeile +1][Spielfeld.spalte].setIcon(new ImageIcon("images/PropraBombe.png"));
 					Spielfeld.feld[Spielfeld.zeile +1][Spielfeld.spalte].laufen = false;
 			}
			else if ((left) && (Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte - 1].laufen) && (Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte - 1].endpunkt == false)) {
 						System.out.println("Bombe");
 						Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte - 1].setIcon(new ImageIcon("images/PropraBombe.png"));
 						Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte - 1].laufen = false;
 			}
			else if ((right) && (Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte + 1].laufen) && (Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte + 1].endpunkt == false)) {
 							System.out.println("Bombe");
 							Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte + 1].setIcon(new ImageIcon("images/PropraBombe.png"));
 							Spielfeld.feld[Spielfeld.zeile][Spielfeld.spalte + 1].laufen = false;
 			}
 		}
 		else {
 			System.out.println("Wand / Spielfigur im Weg oder falsche Taste");}
 		}	
 
 	
 	}
