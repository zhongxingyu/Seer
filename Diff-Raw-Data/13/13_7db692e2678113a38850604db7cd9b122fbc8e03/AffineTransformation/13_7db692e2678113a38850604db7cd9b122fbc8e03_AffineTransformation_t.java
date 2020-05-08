 package ch.epfl.flamemaker.geometry2d;
 
 import java.io.Serializable;
 
 /**
  * Classe modélisant une transformation affine
  */
 @SuppressWarnings("serial")
 public class AffineTransformation implements Transformation, Serializable {
 
 	/**
 	 * La transformation identité
 	 */
 	final static public AffineTransformation IDENTITY = new AffineTransformation(
 			1, 0, 0, 0, 1, 0);
 	/**
 	 * Les 6 nombres représentant les deux premières lignes de la matrice
 	 * homogène de la transformation (a b c) (d e f) (0 0 1)
 	 */
 	final private double m_a, m_b, m_c, m_d, m_e, m_f;
 
 	/**
 	 * Crée une transformation affine à partir des valeurs des deux premières
 	 * lignes de sa matrice homogène
 	 * 
 	 * @param a
 	 *            , b, c, d, e, f Les valeurs de chaque élément des deux
 	 *            premières lignes de la matrice homogène
 	 */
 	public AffineTransformation(double a, double b, double c, double d,
 			double e, double f) {
 		m_a = a;
 		m_b = b;
 		m_c = c;
 		m_d = d;
 		m_e = e;
 		m_f = f;
 	}
 
 	/**
 	 * Crée une transformation affine à partir de la transformation passée en
 	 * paramètre
 	 * 
 	 * @param from
 	 *            La transformation à copier
 	 */
 	public AffineTransformation(AffineTransformation from) {
 		m_a = from.m_a;
 		m_b = from.m_b;
 		m_c = from.m_c;
 		m_d = from.m_d;
 		m_e = from.m_e;
 		m_f = from.m_f;
 	}
 
 	@Override
 	public Point transformPoint(Point p) {
 		return new Point(m_a * p.x() + m_b * p.y() + m_c, m_d * p.x() + m_e
 				* p.y() + m_f);
	}	
 
 	/**
 	 * Compose la transformation actuelle avec celle passée en paramètre, en
 	 * multipliant leurs matrices homogènes
 	 * 
 	 * @param other
 	 *            La transformation à composer avec l'actuelle
 	 * @return La composition de ces deux transformations
 	 */
 	public AffineTransformation composeWith(AffineTransformation other) {
 		return new AffineTransformation(m_a * other.m_a + m_b * other.m_d, m_a
 				* other.m_b + m_b * other.m_e, m_a * other.m_c + m_b
 				* other.m_f + m_c, m_d * other.m_a + m_e * other.m_d, m_d
 				* other.m_b + m_e * other.m_e, m_d * other.m_c + m_e
 				* other.m_f + m_f);
 	}
 
 	/**
 	 * @return La composante horizontale de la transformation
 	 */
 	public double translationX() {
 		return m_c;
 	}
 
 	/**
 	 * @return La composante verticale de la transformation
 	 */
 	public double translationY() {
 		return m_f;
 	}
 
 	/**
 	 * @param dx
 	 *            La composante horizontale de la translation
 	 * @param dy
 	 *            La composante verticale de la translation
 	 * @return Une translation de composante horizontale <i>dx</i> et de
 	 *         composante verticale <i>dy</i>
 	 */
 	public static AffineTransformation newTranslation(double dx, double dy) {
 		return new AffineTransformation(1, 0, dx, 0, 1, dy);
 	}
 
 	/**
 	 * @param teta
 	 *            L'angle de la rotation
 	 * @return Une rotation d'angle <i>teta</i>
 	 */
 	public static AffineTransformation newRotation(double teta) {
 		return new AffineTransformation(Math.cos(teta), -Math.sin(teta), 0,
 				Math.sin(teta), Math.cos(teta), 0);
 	}
 
 	/**
 	 * @param sx
 	 *            Facteur de dilatation horizontal
 	 * @param sy
 	 *            Facteur de dilatation vertical
 	 * @return Une dilatation de facteur horizontal <i>sx</i> et de facteur
 	 *         vertical <i>sy</i>
 	 */
 	public static AffineTransformation newScaling(double sx, double sy) {
 		return new AffineTransformation(sx, 0, 0, 0, sy, 0);
 	}
 
 	/**
 	 * @param sx
 	 *            Facteur de transvection horizontal
 	 * @return Une transvection de facteur <i>sx</i> parallèlement à l'axe des
 	 *         abscisses
 	 */
 	public static AffineTransformation newShearX(double sx) {
 		return new AffineTransformation(1, sx, 0, 0, 1, 0);
 	}
 
 	/**
 	 * @param sy
 	 *            Facteur de transvection horizontal
 	 * @return Une transvection de facteur <i>sy</i> parallèlement à l'axe des
 	 *         ordonnées
 	 */
 	public static AffineTransformation newShearY(double sy) {
 		return new AffineTransformation(1, 0, 0, sy, 1, 0);
 	}
 }
