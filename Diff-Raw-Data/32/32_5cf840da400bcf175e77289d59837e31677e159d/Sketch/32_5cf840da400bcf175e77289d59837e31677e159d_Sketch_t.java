 package pokemon;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Polygon;
 import java.awt.RenderingHints;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 
 import javax.swing.JComponent;
 import javax.swing.JSlider;
 
 @SuppressWarnings("serial")
 class Sketch extends JComponent {
 	private Mode mode;
 	private Image image;
 	private Graphics2D graphics2D;
 	private int currentX, currentY, oldX, oldY;
 	private int oldWidth, oldHeight;
 	
 	private CanvasView canvasView;
 	private SceneModel sceneModel;
 	
 	Polygon selection = null;
 	public TimeLine timeLine;
 	
 	JSlider slider;
 	
 	Entity newEntity;
 
 	public Sketch() {
 		setMode(Mode.DRAW);
 		setDoubleBuffered(false);
 		
 		sceneModel = new SceneModel();
 		canvasView = new CanvasView(sceneModel, this);
 		newEntity = null;
 		
 		addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				int x = e.getX();
 				int y = e.getY();
 				
 				oldX = x;
 				oldY = y;
 				
 				if (mode == Mode.SELECT || mode == Mode.ANIMATE) {
 					if (sceneModel.pointIsInSelection(x, y)) {
 						setMode(Mode.ANIMATE);
 					} else if (!sceneModel.pointIsInSelection(x, y)) {
 						setMode(Mode.SELECT);
 					}
 				}
 				
 				if (mode == Mode.DRAW) {
 					sceneModel.clearSelection();
 					
 					newEntity = new Entity(x, y);
 					sceneModel.addEntity(newEntity);
 				} else if (mode == Mode.ERASE) {
 					sceneModel.clearSelection();
 				} else if (mode == Mode.SELECT) {
 					sceneModel.clearSelection();
 					sceneModel.beginSelection(x, y);
 				} else if (mode == Mode.ANIMATE) {
 					selection = sceneModel.selection;
 					timeLine.record(sceneModel.selectedEntities);
 				}
 			}
 		});
 		
 		addMouseMotionListener(new MouseMotionAdapter() {
 			public void mouseDragged(MouseEvent e) {
 				int x = e.getX();
 				int y = e.getY();
 				
 				currentX = x;
 				currentY = y;
 				
 				if (mode == Mode.DRAW) {
 					newEntity.addPoint(x, y);
 				} else if (mode == Mode.ERASE) {
 					sceneModel.erase(oldX, oldY, currentX, currentY);
 				} else if (mode == Mode.SELECT) {
 					sceneModel.addPointToSelection(x, y);
 				} else if (mode == Mode.ANIMATE) {
 					int dx = currentX - oldX;
 					int dy = currentY - oldY;
 					sceneModel.translateSelection(dx, dy);
 				}
 				
 				oldX = currentX;
 				oldY = currentY;
 			}
 		});
 		
 		addMouseListener(new MouseAdapter() {
             public void mouseReleased(MouseEvent e) {
             	if (mode == Mode.DRAW) {
             		newEntity = null;
 //                	sceneModel.finishEntity();
             	} else if (mode == Mode.SELECT) {
                 	sceneModel.finishSelection();
             	} else if (mode == Mode.ANIMATE) {
             		timeLine.endRecord();
             	}
             }
         });
 	}
 	
 	public void setSlider(JSlider slider) {
 		this.slider = slider;
 		timeLine = new TimeLine(canvasView, slider);
 	}
 	
 	public void redraw() {
 		repaint();
 	}
 	
 	public void playAnimation() {
 		sceneModel.clearSelection();
 		timeLine.play();
 	}
 	
 	public void pauseAnimation() {
 		timeLine.pause();
 	}
 
 	public void paintComponent(Graphics g) {
 		if (image == null || oldWidth != getSize().width || oldHeight != getSize().height) {
 			oldWidth = getSize().width;
 			oldHeight = getSize().height;
 			initImage();
 		}
 
 		canvasView.updateView();
 		g.drawImage(image, 0, 0, null);
 	}
 	
 	public void setMode(Mode mode) {
 		this.mode = mode;
 	}
 
 	public void clear() {
 		graphics2D.setPaint(Color.white);
 		graphics2D.fillRect(0, 0, getSize().width, getSize().height);
 		graphics2D.setPaint(Color.black);
 		repaint();
 	}
 	
 	private void initImage() {
 		image = createImage(getSize().width, getSize().height);
 		graphics2D = (Graphics2D) image.getGraphics();
 		graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		canvasView.setGraphics(graphics2D);
 		clear();
 	}
 }
