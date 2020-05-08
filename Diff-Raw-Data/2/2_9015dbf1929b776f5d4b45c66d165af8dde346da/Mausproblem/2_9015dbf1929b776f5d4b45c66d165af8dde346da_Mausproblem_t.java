 // Copyright (c) 2007 Martin Ueding <dev@martin-ueding.de>
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 /**
  * Jede Maus hat einen Nachfolger, der ihr hinterher rennt. Da jede Maus einen
  * Nachfolger hat, entsteht ein Band, das am Ende auch sichtbar wird.
  *
  * Copyright: Martin Ueding
  */
 
 public class Mausproblem extends TimerTask {
 
 	static MausPanel feld;
 	static int iter = 0;
 	static Timer timer;
 	static JFrame f;
 	static int fb, fh;
 	static int anz;
 
 	/**
 	 * Main ließt die Bilschirmgröße ein und setzt die internen Variablen für
 	 * Breite und Höhe auf die eingelesenen Werte. Danach wird die Anzahl der
	 * Mäuse eingelesen.
 	 *
 	 * Dann wird das Fenster geöffnet und ein Spielfeld eingefügt. Zu letzt
 	 * wird ein Timer gestartet, der alle 0,02 Sekunden das Bild neu aufbaut,
 	 * damit eine flüssige Bewegung erzeugt wird.
 	 */
 
 	public static void main(String[] args) {
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 
 		fb = d.width;
 		fh = d.height - 50;
 
 		anz = 1000;
 
 		anz = Integer.parseInt(JOptionPane.showInputDialog(Spr.get("objekte")));
 
 		feld = new MausPanel(anz, fb, fh);
 		f = new JFrame("Mausproblem");
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setSize(fb, fh);
 		f.setLocation(0, 0);
 		f.add(feld);
 
 		f.setVisible(true);
 
 		timer = new Timer();
 		timer.schedule(new Mausproblem(), 500, 20);
 
 	}
 
 	/**
 	 * Diese Methode wird bei jedem Durchlauf des Timers ausgeführt, sie
 	 * zeichnet nur das Feld neu.
 	 *
 	 * Wenn alle Punkte außerhalb des Feldes sind, wird das ganze neu
 	 * gestartet.
 	 */
 
 	public void run() {
 		feld.repaint();
 		iter++;
 
 
 		if (iter % 10 == 0 && feld.innerhalb()) {
 			/* Neustart */
 			f.getContentPane().remove(feld);
 			feld = null;
 			feld = new MausPanel(anz, fb, fh);
 			f.getContentPane().add(feld);
 			f.setVisible(true);
 
 			Maus.resetSpeed();
 			iter = 0;
 		}
 	}
 }
