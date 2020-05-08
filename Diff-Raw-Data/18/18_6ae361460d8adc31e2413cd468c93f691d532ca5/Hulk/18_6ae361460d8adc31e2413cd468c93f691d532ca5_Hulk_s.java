 /**
  * Speichert die Position der Spielfigur im Spielfeld und Grafik
  * 
  * @author Tobias Korfmacher
  * 
  */
 
 public class Hulk {
 
 	// Deklaration & Initialisierung:
 	public int x; // x-Koordinate (horizontale Position)
 	public int y; // y-Koordinate (vertikale Position)
 	public int[] startPos = new int[2];
 	public int icon;
 	public int max_bomben = 1;
 	private int bomben_radius = 2;
 	static String spielerName = "Spieler 1";
 
 	/* Konstruktor: */
 	/**
 	 * @param x
 	 *            Horizontal-Koordinate der Figur
 	 * @param y
 	 *            Vertikal-Koordinate der Figur
 	 * @param Icon
 	 *            Grafik der Spielfigur
 	 */
 	public Hulk(int x, int y, int Icon) {
 		this.x = x;
 		this.y = y;
 		startPos[0] = x;
 		startPos[1] = y;
 		icon = Icon;
 	}
 
 	/* METHODEN: */
 
 	/* setter & getter: */
 
 	// get_x-Methode:
 	/**
 	 * @return aktuelle X-Koordinate im Map-Array
 	 */
 	public int get_x() {
 		return x;
 	}
 
 	// set_x-Methode:
 	/**
 	 * @param x
 	 *            aktualisiert X-Koordinate
 	 */
 	public void set_x(int x) {
 		this.x = x;
 	}
 
 	// get_y-Methode:
 	/**
 	 * @return aktuelle y-Koordinate im Map-Array
 	 */
 	public int get_y() {
 		return y;
 	}
 
 	// set_y-Methode:
 	/**
 	 * @param y
 	 *            aktualisiert Y-Koordinate
 	 */
 	public void set_y(int y) {
 		this.y = y;
 	}
 
 	// get_startX-Methode:
 	public int get_startX() {
 		return startPos[0];
 	}
 
 	// get_startY-Methode:
 	public int get_startY() {
 		return startPos[1];
 	}
 
 	// get_Icon-Methode:
 	/**
 	 * @return Icon-ID fuer Grafikarray
 	 */
 	public int get_Icon() {
 		return icon;
 	}
 
 	// set Spielername Methode
 	public void set_Spielername(String name) {
 		spielerName = name;
 	}
 
 	// get Spielername Methode
 	public String get_Spielername() {
 		return spielerName;
 	}
 
 	// get_max_bomben-Methode:
 	public int get_max_bomben() {
 		return max_bomben;
 	}
 
 	// set_max_bomben-Methode:
 	public void set_max_bomben(int max_bomben) {
 		this.max_bomben = max_bomben;
 	}
 
 	// get_bomben_radius-Methode:
 	public int get_bomben_radius() {
 		return bomben_radius;
 	}
 
 	// set_bomben_radius-Methode:
 	public void set_bomben_radius(int bomben_radius) {
 		this.bomben_radius = bomben_radius;
 	}
 
 }
