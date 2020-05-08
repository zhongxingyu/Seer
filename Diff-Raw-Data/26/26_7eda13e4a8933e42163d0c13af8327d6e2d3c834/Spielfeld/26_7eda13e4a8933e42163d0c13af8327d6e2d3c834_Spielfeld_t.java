 import java.awt.GridLayout;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 
 public class Spielfeld extends JFrame implements KeyListener {	
 	
     static int zeile = 1, spalte = 1, zeile2 = 9, spalte2 = 9;
     JPanel panel = new JPanel(new GridLayout(11, 11, 0, 0)); // anlegen vom Gridlayout
     public static Feld[][] feld = new Feld[11][11]; // 11 mal 11 JLabel
     
         public Spielfeld() {
         	
             	for (int x = 0; x < 11; x++) { // zweidimensionale Schleife
     				for (int y = 0; y < 11; y++) {
     					 if (x == 0 || x == 10 || y == 0 || y == 10 || (x % 2 == 0 && y % 2 == 0)) { // Koordinaten für die Waende
                              feld[x][y] = new Feld (new ImageIcon("images/wand2.png")); // feld (JLabel) an der Stelle x, y bekommt dieses Bild zugewiesen
                              panel.add(feld[x][y]); // einfuegen von JLabel in das JPanel
                              }
     					 else if ((x == 3 && y == 3) || (x == 3 && y == 7) || (x == 7 && y == 3) || (x == 7 && y == 7)) { // Koordinaten für die zerstoerbaren Waende
     						 feld[x][y] = new Feld (new ImageIcon("images/wandbreak.png"));
                              feld[x][y].zerbrechen = true;
                              panel.add(feld[x][y]);
     					 }
     					 else { // ansonten Rasen
     						 feld[x][y] = new Feld (new ImageIcon("images/rasen.png"));
     						 feld[x][y].laufen = true;
     						 panel.add(feld[x][y]);
     					 }
     				}
             	}
             	
             feld[zeile][spalte].setIcon(new ImageIcon("images/Bomberwoman.png")); // Spielfigur
             feld[zeile2][spalte2].setIcon(new ImageIcon("images/Bomberwoman2.png"));
            feld[zeile][spalte].laufen = false; // laufen auf false setzen, damit die Bomberwomen sich nicht umlaufen koennen
            feld[zeile2][spalte2].laufen = false;
             feld[5][5].setIcon(new ImageIcon("images/endpunkt.png")); // Endpunkt
             feld[5][5].endpunkt = true;
             
             setContentPane(panel); // hinzufuegen des fertigen Gridlayout- + Bilderkonstruktes
             setTitle("BOMBERWOMAN"); // Fenstertitel
             setSize(550, 550); // Fenstergroesse
             setLocationRelativeTo(null);
             setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             setResizable(false); // maximieren verbieten, damit das Spiel nicht unschoen aussieht
             setVisible(true); // Sichtbarkeit auf true setzen
             
             addKeyListener(this); // einfuegen des KeyListeners
         }
         
         public void keyPressed(KeyEvent e) {
         	
         	Move move = new Move();
         	move.keyboard(e);
         	Move2 move2 = new Move2();
         	move2.keyboard(e);
         }
 
 		public void keyReleased(KeyEvent e) { // muss implementiert sein, kann aber leer bleiben, da keine Verwendung besteht
 
 		}
 		
 		public void keyTyped(KeyEvent e) {
 			
 		}
 }
