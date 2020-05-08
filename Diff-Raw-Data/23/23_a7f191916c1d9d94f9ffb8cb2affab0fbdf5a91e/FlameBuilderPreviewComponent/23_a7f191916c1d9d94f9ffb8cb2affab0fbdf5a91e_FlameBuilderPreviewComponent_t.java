 /**
  * @author Hadrien Milano <Sciper : 224340>
  * @author Christophe Tafani-Dereeper <Sciper : 223529>
  */
 
 package ch.epfl.flamemaker.gui;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JComponent;
 
 import ch.epfl.flamemaker.color.Color;
 import ch.epfl.flamemaker.color.Palette;
 import ch.epfl.flamemaker.flame.FlameAccumulator;
 import ch.epfl.flamemaker.flame.ObservableFlameBuilder;
 import ch.epfl.flamemaker.flame.ObservableFlameBuilder.Listener;
 import ch.epfl.flamemaker.geometry2d.Rectangle;
 
 /**
  * Ce component dessine la fractale définie par les paramètres du GUI
  */
 @SuppressWarnings("serial")
 public class FlameBuilderPreviewComponent extends JComponent implements
 Listener {
 
 	// Palette de couleur avec laquelle dessiner la fractale
 	private Palette m_palette;
 
 	// Couleur de fond
 	private Color m_bgColor;
 
 	// Constructeur de la fractale à dessiner
 	private ObservableFlameBuilder m_builder;
 
 	// Cadre de la fractale à dessiner
 	private Rectangle m_frame;
 
 	// Densité du dessin
 	private int m_density;
 
 	/**
 	 * Constructeur, initialise les arguments.
 	 * 
 	 * @param builder
 	 *            Constructeur de la fractale à dessiner
 	 * @param backgroundColor
 	 *            Couleur de fond
 	 * @param palette
 	 *            Palette de couleur avec laquelle dessiner la fractale
 	 * @param frame
 	 *            Cadre de la fractale à dessiner
 	 * @param density
 	 *            Densité du dessin
 	 */
 	public FlameBuilderPreviewComponent(ObservableFlameBuilder builder,
 			Color backgroundColor, Palette palette, Rectangle frame, int density) {
 
 		m_bgColor = backgroundColor;
 		m_palette = palette;
 		m_builder = builder;
 		m_frame = frame;
 		m_density = density;
 
 		m_builder.addListener(this);
 	}
 
 	/**
	 * Méthode appelée pour rafraîchir le dessin
 	 */
 	@Override
 	protected void paintComponent(Graphics g) {
 		// On calcule le vrai cadre de la fractale basé sur le ratio de taille
 		// du component
 		Rectangle realFrame = m_frame.expandToAspectRatio((double) this
 				.getWidth() / this.getHeight());
 
 		// On peut maintenant calculer la fractale avec les paramètres de taille
 		FlameAccumulator accumulator = m_builder.build().compute(realFrame,
 				this.getWidth(), this.getHeight(), m_density);
 
 		// Crée l'image sur laquelle on va rendre la fractale
 		BufferedImage image = new BufferedImage(accumulator.width(),
 				accumulator.height(), BufferedImage.TYPE_INT_RGB);
 
 		for (int x = 0; x < accumulator.width(); x++) {
 			for (int y = 0; y < accumulator.height(); y++) {
 				// On met à jour la couleur du pixel courant
 				image.setRGB(x, accumulator.height() - y - 1, accumulator
 						.color(m_palette, m_bgColor, x, y).asPackedRGB());
 			}
 		}
 
 		// Et on dessine l'image sur l'objet de type Graphics passé en paramètre
 		g.drawImage(image, 0, 0, null);
 	}
 
 	/**
 	 * Retourne la taille préférée (par défaut : 200x100)
 	 */
 	@Override
 	public Dimension getPreferredSize() {
 		return new Dimension(200, 100);
 	}
 
 	@Override
 	public void onFlameBuilderChange(ObservableFlameBuilder b) {
 		repaint();
 	}
 }
