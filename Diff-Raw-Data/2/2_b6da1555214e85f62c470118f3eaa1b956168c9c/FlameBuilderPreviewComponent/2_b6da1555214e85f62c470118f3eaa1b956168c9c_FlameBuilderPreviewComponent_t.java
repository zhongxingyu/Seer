 package ch.epfl.flamemaker.gui;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JComponent;
 
 import ch.epfl.flamemaker.color.Color;
 import ch.epfl.flamemaker.color.Palette;
 import ch.epfl.flamemaker.concurrent.Flame;
 import ch.epfl.flamemaker.concurrent.ObservableFlameBuilder;
 import ch.epfl.flamemaker.concurrent.ObservableFlameBuilder.Listener;
 import ch.epfl.flamemaker.flame.FlameAccumulator;
 import ch.epfl.flamemaker.geometry2d.ObservableRectangle;
 import ch.epfl.flamemaker.geometry2d.Point;
 import ch.epfl.flamemaker.geometry2d.Rectangle;
 
 /**
  * Ce component dessine la fractale définie par les paramètres du GUI
  */
 @SuppressWarnings("serial")
 public class FlameBuilderPreviewComponent extends JComponent implements Listener, MouseListener, ObservableRectangle.Listener, MouseMotionListener, MouseWheelListener{
 	
 	static public final double ZOOM_FACTOR = 1.1;
 	
 	// Palette de couleur avec laquelle dessiner la fractale
 	private Palette m_palette;
 	
 	// Couleur de fond
 	private Color m_bgColor;
 	
 	// Constructeur de la fractale à dessiner
 	private ObservableFlameBuilder m_builder;
 	
 	// Cadre de la fractale à dessiner
 	private ObservableRectangle m_frame;
 	
 	private Rectangle m_lastFrame;
 	
 	// Cadre redimentionné pour atteindre le ratio du composant
 	private Rectangle m_realFrame;
 	
 	private Rectangle m_drawingRect;
 	
 	private boolean m_dragging;
 	private boolean m_displayProgress;
 	
 	private Integer m_progress = -1;
 	
 	// Densité du dessin
 	private int m_density;
 	
 	private Flame m_flame;
 	
 	private boolean m_preventRecompute;
 	
 	private FlameAccumulator m_accu;
 	
 	private BufferedImage m_image;
 	
 	private int m_lastHeight = 0,
 				m_lastWidth = 0;
 	
 	// Coordonnées du mousePressed
 	private int m_mouseX;
 	private int m_mouseY;
 
 	/**
 	 * Constructeur, initialise les arguments.
 	 * @param builder Constructeur de la fractale à dessiner
 	 * @param backgroundColor Couleur de fond
 	 * @param palette Palette de couleur avec laquelle dessiner la fractale
 	 * @param frame Cadre de la fractale à dessiner
 	 * @param density Densité du dessin
 	 */
 	public FlameBuilderPreviewComponent(
 			ObservableFlameBuilder builder,
 			Color backgroundColor,
 			Palette palette,
 			ObservableRectangle frame,
 			int density){
 		
 		m_bgColor = backgroundColor;
 		m_palette = palette;
 		m_builder = builder;
 		m_frame = frame;
 		m_density = density;
 		
 		m_lastFrame = m_frame.toRectangle();
 		
 		m_builder.addListener(this);
 		
 		addMouseListener(this);
 		addMouseWheelListener(this);
 		this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
 		
 		m_frame.addListener(this);
 	}
 
 	
 	/**
 	 * Méthode appellée pour rafraichir le dessin. Si le dessin stocké n'est pas à jour, elle demande un nouveau 
 	 * calcul avec recompute() et redimentionne l'image pour un affichage temporaire.
 	 */
 	@Override
 	protected void paintComponent(final Graphics g){
 		super.paintComponent(g);
 		
 		// L'utilisateur a modifié ou modifie le cadre de vue
 		if(m_dragging){
 			int newWidth = (int)(m_lastFrame.width()/m_frame.width()*this.getWidth());
 			int newHeight = (int)(m_lastFrame.height()/m_frame.height()*this.getHeight());
 			
 			/* Evite d'afficher la couleur par défaut dans les zones pas couvertes par l'image */
 			g.setColor(java.awt.Color.BLACK);
 			g.fillRect(0, 0, this.getWidth(), this.getHeight());
 			
 			m_drawingRect = new Rectangle(new Point(
 					(m_lastFrame.center().x() - m_frame.center().x())*(this.getWidth()/m_realFrame.width()) + getWidth()/2, 
 					(m_frame.center().y() - m_lastFrame.center().y())*(this.getHeight()/m_realFrame.height()) + getHeight()/2),
 					newWidth,
 					newHeight);
 			
 			g.drawImage(m_image, (int)m_drawingRect.left(), (int)m_drawingRect.bottom()
 					, (int)m_drawingRect.width(), (int)m_drawingRect.height(), null);
 			
 			// Il a fini de modifier la vue
 			if(!m_preventRecompute) {
 				m_dragging = false;
 				recompute();
 			}
 		// le composant a été redimentionné
 		} else if(m_lastHeight != this.getHeight() || m_lastWidth != this.getWidth()){
 			recompute();
 			if(m_image != null){
 				
 				Rectangle rect = new Rectangle(new Point(0,0), this.getWidth(), this.getHeight())
 						.expandToAspectRatio((double)m_image.getWidth()/m_image.getHeight());
 						
 				m_drawingRect = new Rectangle(new Point(
 						(this.getWidth())/2,
 						(this.getHeight())/2),
 						rect.width(), rect.height());
 
 				g.drawImage(m_image, (int)m_drawingRect.left(), (int)m_drawingRect.bottom()
 						, (int)m_drawingRect.width(), (int)m_drawingRect.height(), null);
 			}
 		// Sinon, c'est qu'on a fini un recompute, on génère l'image résultante
 		} else if(m_accu != null){
 			m_lastFrame = m_frame.toRectangle();
 			
 			m_image = new BufferedImage(m_accu.width(), m_accu.height(), BufferedImage.TYPE_INT_RGB);
 			
 			for(int x = 0 ; x < m_accu.width() ; x++){
 				for(int y = 0 ; y < m_accu.height() ; y++){
 					// On met à jour la couleur du pixel courant
 					m_image.setRGB(x, m_accu.height() - y -1, m_accu.color(m_palette, m_bgColor, x, y).asPackedRGB());
 				}
 			}
 			
 			//Et on dessine l'image sur l'objet de type Graphics passé en paramètre
 			g.drawImage(m_image, 0, 0, null);
 			
 			m_drawingRect = new Rectangle(new Point(getWidth()/2, getHeight()/2), getWidth(), getHeight());
 			m_accu = null;
 			
 		} else if(m_displayProgress) {
 			g.setColor(java.awt.Color.BLACK);
 			g.fillRect(0, 0, this.getWidth(), this.getHeight());
 			
 			if(m_drawingRect != null){
 				g.drawImage(m_image, (int)m_drawingRect.left(), (int)m_drawingRect.bottom()
 						, (int)m_drawingRect.width(), (int)m_drawingRect.height(), null);
 			}
 			
 			synchronized(m_progress){
 				drawProgressBar(g);
 			}
 		} else if(m_image != null) {
 			g.drawImage(m_image, (int)m_drawingRect.left(), (int)m_drawingRect.bottom()
 					, (int)m_drawingRect.width(), (int)m_drawingRect.height(), null);
 		}
 		
 		// Sinon, on ne sais pas pourquoi il fallait repeindre
 	}
 	
 	private void drawProgressBar(Graphics g){
 		g.setColor(java.awt.Color.WHITE);
 		
 		int barWidth = getWidth() - 10 - 50;
 		int fillWidth = (m_progress == 0) ? 0 : (barWidth-4)*m_progress/100;
 		int barY = getHeight() - 10 - 10;
 		
 		g.drawString(m_progress+"%", getWidth() - 40, barY + 10);
 		g.drawRect(10, barY, barWidth, 10);
 		g.fillRect(12, barY+2, fillWidth, 7);
 	}
 	
 	/**
 	 * Demande un nouveau calcul de la fractale. 
 	 * Cette méthode demande à son tour un nouveau dessin du composant quand le calcul est terminé.
 	 */
 	private void recompute(){
 		
 		// Protege contre des calculs inutiles
 		if(this.getWidth() == 0 || this.getHeight() == 0)
 			return;
 		
 		if(m_flame != null){
 			m_flame.destroy();
 			m_flame = null;
 		}
 		
 		
 		// On peut maintenant calculer la fractale avec les paramètres de taille
 		m_flame = m_builder.build();
 		m_flame.addListener(new Flame.Listener() {
 			
 			@Override
 			public void onComputeProgress(int percent) {
 				synchronized(m_progress){
 					m_progress = percent;
 					m_displayProgress = true;
 					repaint();
 				}
 			}
 			
 			@Override
 			public void onComputeDone(FlameAccumulator accumulator) {
 				synchronized(m_progress){
 					m_accu = accumulator;
 					m_displayProgress = false;
 					repaint();
 				}
 			}
 		});
 	
 		m_realFrame = m_frame.expandToAspectRatio((double)this.getWidth()/this.getHeight());
 		
 		m_lastHeight = this.getHeight();
 		m_lastWidth = this.getWidth();
 		
 		m_flame.compute(m_realFrame, this.getWidth(), this.getHeight(), m_density);
 		
 		synchronized(m_progress){
 			m_displayProgress = true;
 			m_progress = 0;
 			repaint();
 		}
 	}
 	
 	/**
 	 * Retourne la taille préférée (par défaut : 200x100)
 	 */
 	@Override
 	public Dimension getPreferredSize(){
 		return new Dimension(200, 100);
 	}
 
 
 	@Override
 	public void onFlameBuilderChange(ObservableFlameBuilder b) {
 		recompute();
 	}
 
 
 	/* Interaction avec la souris */
 	
 	// Les trois méthodes suivantes ne nous intéressent pas
 	@Override
 	public void mouseClicked(MouseEvent arg0) {}
 	@Override
 	public void mouseEntered(MouseEvent arg0) {}
 	@Override
 	public void mouseExited(MouseEvent arg0) {}
 
 
 	@Override
 	public void mousePressed(MouseEvent evt) {
 		m_mouseX = evt.getX();
 		m_mouseY = evt.getY();
 		m_preventRecompute = true;
 		m_dragging = true;
 		
 		addMouseMotionListener(this);
 	}
 
 
 	@Override
 	public void mouseReleased(MouseEvent evt) {
 		m_preventRecompute = false;
 		
 		m_frame.setCenter(new Point(
 				m_frame.center().x() + (m_mouseX - evt.getX())*m_realFrame.width()/this.getWidth(),
 				m_frame.center().y() - (m_mouseY - evt.getY())*m_realFrame.height()/this.getHeight()
 		));
 		
 		removeMouseMotionListener(this);
 	}
 
 
 	@Override
 	public void onRectangleChange(ObservableRectangle rect) {
 		repaint();
 	}
 
 
 	@Override
 	public void mouseDragged(MouseEvent evt) {
 		m_frame.setCenter(new Point(
 				m_frame.center().x() + (m_mouseX - evt.getX())*m_realFrame.width()/this.getWidth(),
 				m_frame.center().y() - (m_mouseY - evt.getY())*m_realFrame.height()/this.getHeight()
 		));
 		
 		m_mouseX = evt.getX();
 		m_mouseY = evt.getY();
 	}
 
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {}
 
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent evt) {
 		if(evt.getWheelRotation() == 0) return;
 		
 		
 		
		double factor = (evt.getWheelRotation() < 0) ? 1.0/ZOOM_FACTOR : ZOOM_FACTOR;
 		
 		m_dragging = true;
 		m_frame.setSize(m_frame.width()*factor, m_frame.height()*factor);
 	}
 }
