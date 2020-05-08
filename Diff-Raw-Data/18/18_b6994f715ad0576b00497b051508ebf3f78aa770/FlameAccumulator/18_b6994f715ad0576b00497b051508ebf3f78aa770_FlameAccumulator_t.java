 /**
  * @author Hadrien Milano <Sciper : 224340>
  * @author Christophe Tafani-Dereeper <Sciper : 223529>
  */
 
 package ch.epfl.flamemaker.flame;
 
 import ch.epfl.flamemaker.color.Palette;
 import ch.epfl.flamemaker.color.Color;
 import ch.epfl.flamemaker.geometry2d.AffineTransformation;
 import ch.epfl.flamemaker.geometry2d.Point;
 import ch.epfl.flamemaker.geometry2d.Rectangle;
 import ch.epfl.flamemaker.geometry2d.Transformation;
 
 /**
  * Classe modélisant un accumulateur pour une fractale Flame
  */
 public class FlameAccumulator {
 
 	/**
 	 * Le nombre de points de chaque case de l'accumulateur
 	 */
 	private int[][] m_hitCount;
 
 	/**
 	 * La somme des index de couleur de l'accumulateur
 	 */
 	private double[][] m_colorIndexes;
 
 	/**
 	 * Le dénominateur utilisé pour le calcul de l'intensité d'une case
 	 */
 	private double m_denominator;
 
 	/**
 	 * Construit un accumulateur à partir d'un tableau contenant le nombre de
 	 * points de chaque case de l'accumulateur et de la somme des index de
 	 * couleur de chaque case
 	 * 
 	 * @param hitCount
 	 *            Le nombre de points contenu par chaque case
 	 * @param colorIndexSum
 	 *            La somme des index de couleur de chaque case
 	 */
 	private FlameAccumulator(int[][] hitCount, double[][] colorIndexSum) {
 
 		int maxHit = 0;
 		int width = hitCount.length;
 		int height = hitCount[0].length;
 
 		m_hitCount = new int[width][];
 		m_colorIndexes = new double[width][];
 
 		for (int i = 0; i < width; i++) {
 			m_hitCount[i] = new int[height];
 			m_colorIndexes[i] = new double[height];
 
 			for (int j = 0; j < height; j++) {
 				m_hitCount[i][j] = hitCount[i][j];
 				m_colorIndexes[i][j] = colorIndexSum[i][j];
 
 				if (maxHit < hitCount[i][j]) {
 					maxHit = hitCount[i][j];
 				}
 
 			}
 		}
 
 		m_denominator = Math.log(maxHit + 1);
 	}
 
 	/**
 	 * @return La largeur de l'accumulateur
 	 */
 	public int width() {
 		return m_hitCount.length;
 	}
 
 	/**
 	 * @return La hauteur de l'accumulateur
 	 */
 	public int height() {
 		return m_hitCount[0].length;
 	}
 
 	/**
 	 * @param x
 	 * @param y
 	 * @return L'intensité de la case de coordonnées x, y de l'accumulateur
 	 */
 	public double intensity(int x, int y) {
 		if (x < 0 || y < 0 || x > m_hitCount.length || y > m_hitCount[0].length) {
 			throw new IndexOutOfBoundsException();
 		}
 
 		return Math.log(m_hitCount[x][y] + 1) / m_denominator;
 	}
 
 	/**
 	 * Permet de calculer la couleur d'un point donné de l'accumulateur, à
 	 * partir d'une palette et d'une couleur de fond
 	 * 
 	 * @param palette
 	 *            La palette à utiliser
 	 * @param background
 	 *            La couleur de fond à utiliser
 	 * @param x
 	 *            Coordonnée x du point
 	 * @param y
 	 *            Coordonnée y du point
 	 * @return La couleur du point de l'accumulateur demandé
 	 * @throws IndexOutOfBoundsException
 	 *             Si les coordonnées du point sont invalides
 	 */
 	public Color color(Palette palette, Color background, int x, int y) {
 		if (x < 0 || y < 0 || x > m_colorIndexes.length
 				|| y > m_colorIndexes[0].length) {
 			throw new IndexOutOfBoundsException();
 		}
 
 		return palette.colorForIndex(m_colorIndexes[x][y]).mixWith(background,
 				intensity(x, y));
 	}
 
 	/**
 	 * Classe modélisant un bâtisseur d'un accumulateur pour une fractale Flame
 	 */
 	public static class Builder {
 
 		/**
 		 * Le nombre de points de chaque case du futur accumulateur qui sera
 		 * construit
 		 */
 		private int[][] m_grid;
 
 		/**
 		 * La somme des index de couleur de chaque case du futur accumulateur
 		 * qui sera construit
 		 */
 		private double[][] m_colors;
 
 		/**
 		 * La transformation permettant d'associer un point du plan à un point
 		 * de l'accumulateur
 		 */
 		private Transformation m_transform;
 
 		/**
 		 * Construit un nouveau bâtisseur pour un accumulateur de largeur et
 		 * hauteurs spécifiés, pour la région du plan frame
 		 * 
 		 * @param frame
 		 *            La région du plan visée
 		 * @param width
 		 *            La largeur de l'accumulateur
 		 * @param height
 		 *            La hauteur de l'accumulateur
 		 * @throws IllegalArgumentException
 		 *             Si la largeur ou la hauteur est invalide
 		 */
 		public Builder(Rectangle frame, int width, int height) {
 			if (width <= 0 || height <= 0) {
 				throw new IllegalArgumentException(
 						"width and height must be positive");
 			}
 
 			m_grid = new int[width][height];
 			m_colors = new double[width][height];
 
 			// On crée la transformation qui passe d'un point du plan à un point
 			// de l'accumulateur
 			m_transform = AffineTransformation.newScaling(
 					(double) width / frame.width(),
 					(double) height / frame.height()).composeWith(
 					AffineTransformation.newTranslation(-frame.left(),
 							-frame.bottom()));
 		}
 
 		/**
 		 * Signale la présence d'un nouveau point p à partir de la position
 		 * définie par ce point et de sa couleur
 		 * 
 		 * @param p
 		 *            Point correspondant à position du nouveau point dans le
 		 *            plan
 		 * @param colorIndex
 		 *            La couleur du point calculé
 		 */
 		public void hit(Point p, double colorIndex) {
 			Point coord = m_transform.transformPoint(p);
 
 			int x = (int) Math.floor(coord.x());
 			int y = (int) Math.floor(coord.y());
 
			m_colors[x][y] = (colorIndex + m_colors[x][y] * m_grid[x][y])
					/ (m_grid[x][y] + 1);
			m_grid[x][y]++;
 		}
 
 		/**
 		 * Construit un accumulateur avec les données récoltées
 		 */
 		public FlameAccumulator build() {
 			return new FlameAccumulator(m_grid, m_colors);
 		}
 	}
 }
