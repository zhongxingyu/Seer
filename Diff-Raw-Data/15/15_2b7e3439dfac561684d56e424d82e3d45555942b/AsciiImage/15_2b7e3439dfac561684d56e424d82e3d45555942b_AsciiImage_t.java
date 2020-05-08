 import java.util.ArrayList;
 
 /**
  * @author Thomas Rieder
  * @matrikelnummer 1125403
  * @date 2012-01-19
  * @description 10. bungsbeispiel
  * 
  */
 
 public class AsciiImage {
 	// Bild
 	private char[][] image;
 
 	// Breite und Hhe
 	private int width, height;
 
 	// gueltige Zeichen
 	private String charset;
 
 	/**
 	 * Konstruktur mit der Gre des Bildes als Parameter
 	 * 
 	 * @param width
 	 *            Breite des Bildes
 	 * @param height
 	 *            Hhe des Bildes
 	 */
 	public AsciiImage(int width, int height, String charset) {
 		// auf 0 prfen
 		if (width <= 0 || height <= 0 || charset.length() == 0) {
 			throw new IllegalArgumentException();
 		}
 
 		// auf doppelte Zeichen prfen
 		for (int i = 0; i < charset.length(); i++) {
 			for (int j = 0; j < charset.length(); j++) {
 				if (charset.charAt(i) == charset.charAt(j) && i != j) {
 					throw new IllegalArgumentException();
 				}
 			}
 		}
 
 		this.charset = charset;
 		this.width = width;
 		this.height = height;
 		char background = charset.charAt(charset.length() - 1);
 
 		image = new char[width][height];
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				image[i][j] = background;
 			}
 		}
 	}
 
 	/**
 	 * Kopierkonstruktor mit Deepcopy
 	 * 
 	 * @param img
 	 *            AsciiImage das kopiert wird
 	 */
 	public AsciiImage(AsciiImage img) {
 		this.charset = img.charset;
 		this.width = img.width;
 		this.height = img.height;
 
 		image = new char[width][height];
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				image[i][j] = img.getPixel(i, j);
 			}
 		}
 	}
 
 	/**
 	 * @return liefert das charset zurck
 	 */
 	public String getCharset() {
 		return charset;
 	}
 
 	/**
 	 * @return liefert die Breite zurck
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * @return liefert die Hhe zurck
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * Ausgabe mittels berladener toString-Methode
 	 * 
 	 * @return Ausgabe des Bildes
 	 */
 	public String toString() {
 		String output = "";
 		for (int i = 0; i < height; i++) {
 			for (int j = 0; j < width; j++) {
 				output += image[j][i];
 			}
 			output += '\n';
 		}
 		return output;
 	}
 
 	/**
 	 * liefert den Pixel an der Stelle (x, y)
 	 * 
 	 * @param x
 	 *            x-Koordinate
 	 * @param y
 	 *            y-Koordinate
 	 * @return character (x, y)
 	 */
 	public char getPixel(int x, int y) {
 		if (x >= width || y >= height) {
 			throw new IndexOutOfBoundsException();
 		}
 		return image[x][y];
 	}
 
 	/**
 	 * Setzt den Pixel an der Stelle (x, y)
 	 * 
 	 * @param x
 	 *            x-Koordinate
 	 * @param y
 	 *            y-Koordinate
 	 * @param c
 	 *            Replacement-Character
 	 */
 	public void setPixel(int x, int y, char c) {
 		if (x >= width || y >= height || charset.indexOf(c) == -1) {
 			throw new IndexOutOfBoundsException();
 		}
 		image[x][y] = c;
 	}
 
 	/**
 	 * Liefert den Wert des AsciiPoints p zurck
 	 * 
 	 * @param p
 	 *            AsciiPoint/Koordinaten
 	 * @return Wert als char
 	 */
 	public char getPixel(AsciiPoint p) {
 		return getPixel(p.getX(), p.getY());
 	}
 
 	/**
 	 * Erzeugt eine Liste von AsciiPoints mit dem Zeichen c
 	 * 
 	 * @param c
 	 *            Zeichen von dem die Liste erstellt wird
 	 * @return Liste alle AsciiPoints die das Zeichen @param c besitzen
 	 */
 	public ArrayList<AsciiPoint> getPointList(char c) {
 		ArrayList<AsciiPoint> ret = new ArrayList<AsciiPoint>();
 		for (int i = 0; i < height; i++) {
 			for (int j = 0; j < width; j++) {
 				if (image[j][i] == c) {
 					ret.add(new AsciiPoint(j, i));
 				}
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * Weist einem Pixel einen neuen Wert zu
 	 * 
 	 * @param p
 	 *            Pixel
 	 * @param c
 	 *            Zeichen
 	 */
 	public void setPixel(AsciiPoint p, char c) {
 		setPixel(p.getX(), p.getY(), c);
 	}
 
 	/**
 	 * Vergleicht zwei Bilder miteinander
 	 * 
 	 * @param c
 	 *            Bild mit dem verglichen wird
 	 * @return true falls gleich
 	 */
 	public boolean equals(Object c) {
		if (c == null) {
			return false;
		}

		if (c == this) {
			return true;
		}

		if (c.getClass() != getClass()) {
			return false;
		}

 		AsciiImage comp = (AsciiImage) c;
 		boolean equal = (comp.getWidth() == width)
		&& (comp.getHeight() == height);
 
 		for (int i = 0; i < width && equal == true; i++) {
 			for (int j = 0; j < height && equal == true; j++) {
 				equal = image[i][j] == comp.image[i][j];
 			}
 		}
 
 		return equal;
 	}
 
 	/**
 	 * Liefert den hashCode eines Bildes zurck (Summe der Ascii-Werte aller
 	 * Zeichen)
 	 * 
 	 * @return hashCode des Bildes
 	 */
 	public int hashCode() {
 		int sum = 0;
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				sum += image[i][j];
 			}
 		}
 		return sum;
 	}
 
 	/**
 	 * Liefert die Anzahl der einzigartigen Zeichen im Bild zurck
 	 * 
 	 * @return Anzahl einzigartige Zeichen
 	 */
 	public int getUniqueChars() {
 		int cnt = 0;
 		String unique = "";
 
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				if (unique.indexOf(image[i][j]) == -1) {
 					unique += image[i][j];
 					cnt++;
 				}
 			}
 		}
 		return cnt;
 	}
 }
