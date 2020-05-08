 /**
  * @author Hadrien Milano <Sciper : 224340>
  * @author Christophe Tafani-Dereeper <Sciper : 223529>
  */
 
 package ch.epfl.flamemaker.flame;
 
 
 import ch.epfl.flamemaker.geometry2d.*;
 
 /**
  * Classe modélisant une transformation de type Flame
  */
 
 public class FlameTransformation implements Transformation {
 
 	/**
 	 * La composante affine de la transformation
 	 */
 	private final AffineTransformation m_affineTransfo;
 
 	/**
 	 * Un tableau contenant les poids des différentes variations
 	 */
 	private final double[] m_weight;
 
 	/**
 	 * Construit une transformation de type Flame à partir de sa composante
 	 * affine et des poids des différentes variations
 	 * 
 	 * @param affineTransformation
 	 *            La composante affine
 	 * @param variationWeight
 	 *            Les poids des différentes variations
 	 * @throws IllegalArgumentException
 	 *             Si le tableau des poids des différentes variations ne
 	 *             contient pas autant d'éléments qu'il y a de variations
 	 */
 	public FlameTransformation(AffineTransformation affineTransformation,
 			double[] variationWeight) {
 		if (variationWeight.length != Variation.ALL_VARIATIONS.size()) {
 			throw new IllegalArgumentException(
 					"variationWeight must have length "
 							+ Variation.ALL_VARIATIONS.size());
 		}
 
 		m_affineTransfo = affineTransformation;
 		m_weight = variationWeight.clone();
 	}
 
 	/**
 	 * @see ch.epfl.flamemaker.geometry2d.Transformation#transformPoint(ch.epfl.flamemaker.geometry2d.Point)
 	 */
 	@Override
 	public Point transformPoint(Point p) {
 
 		Point tmp, result = new Point(0, 0);
 
 		// On applique la transformation affine au point
 		p = m_affineTransfo.transformPoint(p);
 		for (int i = 0; i < Variation.ALL_VARIATIONS.size(); i++) {
 			if (m_weight[i] != 0) {
 				tmp = Variation.ALL_VARIATIONS.get(i).transformPoint(p);
 				result = new Point(result.x() + m_weight[i] * tmp.x(),
 						result.y() + m_weight[i] * tmp.y());
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Retourne le poids de la variation concernée pour la transformation
 	 * courante
 	 * 
 	 * @param variation
 	 *            La variation dont on veut connaître le poids
 	 * @return Le poids de la variation passée en paramètre
 	 * @throws IllegalArgumentException
 	 *             Si la variation a un index invalide
 	 */
 	public double weight(Variation variation) {
 		int index = variation.index();
		if (index < 0 || index >= m_weight.length) {
 			throw new IllegalArgumentException("invalid index given");
 		}
 		return m_weight[index];
 	}
 
 	/**
 	 * @return Le tableau des poids des différentes variations
 	 * @see ch.epfl.flamemaker.gui.WeightsModificationComponent
 	 */
 	public double[] weights() {
 		return m_weight.clone();
 	}
 
 	/**
 	 * Classe modélisant un bâtisseur pour une transformation flame
 	 */
 	public static class Builder {
 
 		/**
 		 * La composante affine de la transformation flame qui sera construite
 		 */
 		private AffineTransformation m_affineTransfo;
 
 		/**
 		 * Les poids des différentes variations de la transformation flame qui
 		 * sera construite
 		 */
 		private double[] m_weights;
 
 		/**
 		 * Construit un bâtisseur de transformation flame à partir d'une telle
 		 * transformation
 		 * 
 		 * @param transformation
 		 */
 		public Builder(FlameTransformation transformation) {
 			m_affineTransfo = transformation.m_affineTransfo;
 			m_weights = transformation.m_weight;
 		}
 
 		/**
 		 * Modifie la composante affine de la transformation flame
 		 * 
 		 * @param newAffineTransfo
 		 *            La nouvelle composante affine
 		 */
 		public void setAffineTransformation(
 				AffineTransformation newAffineTransfo) {
 			m_affineTransfo = newAffineTransfo;
 		}
 
 		/**
 		 * @return La composante affine de la transformation flame
 		 */
 		public AffineTransformation affineTransformation() {
 			return m_affineTransfo;
 		}
 
 		/**
 		 * Modifie le poids de la variation d'index <i>index</i>
 		 * 
 		 * @param index
 		 *            L'index de la variation
 		 * @param newWeight
 		 *            Le nouveau poids à attribuer à cette variation
 		 * @throws IllegalArgumentException
 		 *             si l'index de la variation est invalide
 		 */
 		public void setWeight(int index, double newWeight) {
 			checkWeightIndex(index);
 			m_weights[index] = newWeight;
 		}
 
 		/**
 		 * @param variationIndex
 		 * @return Le poids de la variation d'index passé en paramètre
 		 * @throws IllegalArgumentException
 		 *             si l'index de la variation est invalide
 		 */
 		public double weight(int variationIndex) {
 			checkWeightIndex(variationIndex);
 
 			return m_weights[variationIndex];
 		}
 
 		/**
 		 * Construite la transformation flame à partir des données récoltées
 		 * 
 		 * @return La transformation construite
 		 */
 		public FlameTransformation build() {
 			return new FlameTransformation(m_affineTransfo, m_weights);
 		}
 
 		/**
 		 * Vérifie si l'index passé en paramètre est valide pour le tableau des
 		 * poids
 		 * 
 		 * @param index
 		 *            L'index à vérifier
 		 */
 		public void checkWeightIndex(int index) {
 			if (index < 0 || index >= m_weights.length) {
 				throw new IllegalArgumentException();
 			}
 		}
 	}
 }
