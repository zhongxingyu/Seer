 package graphics;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.Shape;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.LinkedList;
 
 import javax.swing.JPanel;
 
 import ui.FractalPanel;
 import ui.UIFrame;
 import collect.Curves;
 import collect.FractalComponent;
 import collect.Polygon2D;
 import collect.Triangle;
 
 //A fraktálokat alkotó háromszögeket kirajzoló és megjelenítő felület
 public class JCanvas extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private int width;
 	private int height;
 	private double offsetX;
 	private double offsetY;
 	private double scale;
 	private float scalex;
 	private float scaley;
 	private boolean fVisible;
 	private boolean gVisible;
 	private boolean controlsVisible;
 	private boolean curvesVisible;
 	private BufferedImage img;
 	private AffineTransform transform;
 	private LinkedList<FractalComponent> fComponentList;
 	private LinkedList<FractalComponent> gComponentList;
 	private LinkedList<FractalComponent> hComponentList;
 	private LinkedList<Curves> cList;
 	private FractalPanel fFractal;
 	private FractalPanel gFractal;
 	private Triangle defTriang;
 	
 	private int mouseX;
 	private int mouseY;
 	private FractalComponent changeComponent;
 	
 	private float draggableX;
 	private float draggableY;
 	private boolean draggable;
 	
 	private boolean resizable;
 	private Point2D.Float resizePoint;
 	private int radius;
 
 	public JCanvas(FractalPanel fFract, FractalPanel gFract) {
 		width = 700;
 		height = 600;
 		scale = 1.0f;
 		fVisible = true;
 		gVisible = true;
 		controlsVisible = false;
 		curvesVisible = true;
 		defTriang = UIFrame.defTriangle;
 		hComponentList = null;
 		
 		radius = 10;
 		
 		scalex = width / 10;
 		scaley = -scalex;
 		
 		this.fFractal = fFract;
 		this.gFractal = gFract;
 
 		cList = new LinkedList<Curves>();
 		transform = new AffineTransform();
 		setPreferredSize(new Dimension(width, height));
 
 		this.addMouseWheelListener(new MouseWheelListener() {
 			// Scroll esetén skálázást végez el
 			@Override
 			public void mouseWheelMoved(MouseWheelEvent e) {
 				float mod = e.getUnitsToScroll() / 3;
 				mod = mod / 10;
 
 				if (((scale + mod) <= 12.0f) && ((scale + mod) >= 1.0f)) {
 					scale += mod;
 					offsetX = e.getX() * (1.0 - scale) / scale;
 					offsetY = e.getY() * (1.0 - scale) / scale;
 					offsetX = (offsetX < 0) ? offsetX : 0;
 					offsetY = (offsetY < 0) ? offsetY : 0;
 
 					repaint();
 				}
 			}
 		});
 		
 		this.addMouseListener(new MouseAdapter(){
 			@Override
 			public void mousePressed(MouseEvent e) {
				mouseX = (int) (e.getX() / scale - offsetX);
				mouseY = (int) (e.getY() / scale - offsetY);
				
				System.out.println(e.getX() + " " + offsetX + " " + " " + scale + " " + mouseX);
				System.out.println(e.getY() + " " + offsetY + " " + " " + scale + " " + mouseY);
 				
 				if (isInsideTriang(mouseX,mouseY)) {
 					draggable = true;
 				}
 				
 				if (isInsideOval(mouseX,mouseY)) {
 					resizable = true;
 				}
 			}
 			
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				draggable = false;
 				resizable = false;
 			}			
 		});
 		
 		this.addMouseMotionListener(new MouseMotionAdapter() {
 			
 			public void mouseDragged(MouseEvent e) {
 				
 				//Ha valamelyik háromszög csúcspontját mozgatjuk
 				if (resizable) {
					resizePoint.x = (float)((e.getX() / scale - offsetX - width / 2) / scalex);
					resizePoint.y = (float)((e.getY() /scale - offsetY - height / 2) / scaley);
 					
 					changeComponent.transform = changeComponent.triang.toTransform();
 					
 					fFractal.setFractalComponentList(fComponentList);
 					gFractal.setFractalComponentList(gComponentList);
 					
 					repaint();					
 				}
 				else {
 					//Ha az egész háromszöget mozgatjuk
 					if (draggable) {
						changeComponent.triang.A.x = (float)((e.getX() / scale - offsetX - width / 2 - draggableX) / scalex);
						changeComponent.triang.A.y = (float)((e.getY() / scale - offsetY - height / 2 - draggableY) / scaley);
 						
 						changeComponent.triang.B.x = changeComponent.triang.A.x + changeComponent.transform.getValue(0,1);
 						changeComponent.triang.B.y = changeComponent.triang.A.y + changeComponent.transform.getValue(1,1);
 						
 						changeComponent.triang.C.x = changeComponent.triang.A.x + changeComponent.transform.getValue(0,0);
 						changeComponent.triang.C.y = changeComponent.triang.A.y + changeComponent.transform.getValue(1,0);	
 						
 						changeComponent.transform = changeComponent.triang.toTransform();
 						
 						fFractal.setFractalComponentList(fComponentList);
 						gFractal.setFractalComponentList(gComponentList);
 						repaint();
 					}
 				}	
 			}
 			
 		});
 	}
 
 	public void init() {
 		Graphics gr;
 		// Egy image canvasra rajzol
 		img = new BufferedImage(getWidth(), getHeight(),
 				BufferedImage.TYPE_INT_ARGB);
 		gr = img.getGraphics();
 
 		// Háttérszín beállítása
 		gr.setColor(Color.DARK_GRAY);
 		gr.fillRect(0, 0, width, height);
 
 		// Függőleges vonalak
 		gr.setColor(Color.gray);
 		for (int i = 0; i < width; i = i + 10) {
 			gr.drawLine(i, 0, i, height);
 		}
 
 		// Vízszintes vonalak
 		for (int j = 0; j < width; j = j + 10) {
 			gr.drawLine(0, j, width, j);
 		}
 
 		// Függőleges középvonal
 		gr.setColor(Color.BLUE);
 		gr.drawLine(width / 2, 0, width / 2, height);
 
 		// Vízszintes középvonal
 		gr.setColor(Color.RED);
 		gr.drawLine(0, height / 2, width, height / 2);
 	}
 
 	//Kiralyzolom a háromszögek pontjaihoz a megfelelő betűket
 	public void drawTexts(Graphics2D g, Triangle triang, String label) {
 		float x3;
 		float y3;
 
 		g.drawString("A", (float) (triang.A.x * scalex - 10),
 				(float) (triang.A.y * scaley + 10));
 		g.drawString("B", (float) (triang.B.x * scalex + 10),
 				(float) (triang.B.y * scaley + 10));
 		g.drawString("C", (float) (triang.C.x * scalex - 10),
 				(float) (triang.C.y * scaley - 5));
 
 		// Háromszög súlypontja
 		x3 = ((triang.A.x * scalex + triang.B.x * scalex + triang.C.x * scalex) / 3);
 		y3 = ((triang.A.y * scaley + triang.B.y * scaley + triang.C.y * scaley) / 3);
 
 		g.drawString(label, x3, y3);
 	}
 
 	//Kirajzolom a háromszögekhez tartozó köröket
 	public void drawOvals(Graphics2D g, Triangle triang) {
 		g.drawOval((int) (triang.A.x * scalex - radius / 2), (int) (triang.A.y * scaley - radius / 2), radius, radius);
 		g.drawOval((int) (triang.B.x * scalex - radius / 2), (int) (triang.B.y * scaley - radius / 2), radius, radius);
 		g.drawOval((int) (triang.C.x * scalex - radius / 2), (int) (triang.C.y * scaley - radius / 2), radius, radius);
 	}
 	
 	//Kirajzolom a háromszögeket
 	public void drawTriangles(Graphics2D g) {
 		Polygon2D p;
 
 		g.translate(width / 2, height / 2);
 
 		//Alap háromszög kirajzolása
 		defTriang = UIFrame.defTriangle;
 		p = defTriang.getPolygon(scalex, scaley);
 		g.setColor(Color.WHITE);
 		g.draw(p);
 
 		//Az f fraktálhoz tartozó háromszögek kirajzolása
 		if (!fComponentList.isEmpty() && fVisible) {
 			for (FractalComponent component : fComponentList) {
 				g.setColor(component.getColor());
 				drawTexts(g, component.getTriang(), "F");
 				drawOvals(g, component.getTriang());				
 				p = component.getTriang().getPolygon(scalex, scaley);
 				g.draw(p);
 			}
 		}
 
 		//Az g fraktálhoz tartozó háromszögek kirajzolása		
 		if (!gComponentList.isEmpty() && gVisible) {
 
 			// A megadott háromszögek kirajzolása
 			for (FractalComponent component : gComponentList) {
 				g.setColor(component.getColor());
 				drawTexts(g, component.getTriang(), "G");
 				drawOvals(g, component.getTriang());				
 				p = component.getTriang().getPolygon(scalex, scaley);
 				g.draw(p);
 			}
 		}
 		
 		//A h köztes fraktált alkotó háromszög kirajzolása
 		if ((hComponentList != null) && (!hComponentList.isEmpty())) {
 		
 			for (FractalComponent component : hComponentList) {
 				g.setColor(component.getColor());
 				drawTexts(g, component.getTriang(), "H");
 				p = component.getTriang().getPolygon(scalex, scaley);
 				g.draw(p);
 			}
 		}		
 	}
 
 	//Görbék kirajzolása
 	public void drawCurve(Graphics2D g, Point2D.Float[] points) {
 		Polygon poly = new Polygon();
 		Shape shape;
 
 		//Kontrollpont megjelenítése
 		if (controlsVisible) {
 			for (Point2D.Float point : points) {
 				shape = new Ellipse2D.Float((float) (point.x * scalex - 3),
 						(float) (point.y * scaley - 3), 6f, 6f);
 				g.draw(shape);
 			}
 		}
 
 		Point2D.Float point;
 
 		if (curvesVisible) {
 			//Egy görbét 100 pont hataroz meg
 			for (int i = 1; i <= 100; i++) {
 				point = Curves.getCurvePoint((float) i / 100, points);  //Kiszámítja a görbe pontjat egy adott t időpillanatban
 				poly.addPoint((int) Math.round(point.x * scalex),		//Hozzáadom a poligonhoz a kiszamított pontot
 						(int) Math.round((float) (-point.y * scalex)));
 			}
 	
 			g.drawPolyline(poly.xpoints, poly.ypoints, poly.npoints);
 		}
 	}
 
 	//Kirajzolom a görbéket
 	public void drawCurves(Graphics2D g) {
 		Point2D.Float[] points;
 
 		if (!cList.isEmpty()) {
 			
 			for (Curves c : cList) {
 				
 				g.setColor(c.getColor());
 				
 				points = c.getaCurve();
 				drawCurve(g, points);
 				
 				points = c.getbCurve();
 				drawCurve(g, points);
 				
 				points = c.getcCurve();
 				drawCurve(g, points);
 							
 			}
 		}
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		if (img == null) {
 			init();
 		}
 
 		Graphics2D gr2D = (Graphics2D) g;
 		transform = gr2D.getTransform();
 		transform.scale(scale, scale);
 		transform.translate(offsetX, offsetY);
 		gr2D.setTransform(transform);
 
 		gr2D.drawImage(img, 0, 0, null);
 		drawTriangles(gr2D);
 		drawCurves(gr2D);
 	}
 	
 	//Ellenörzi, hogy az egér a háromszögen belül található-e
 	public boolean isInsideTriangle(FractalComponent component,Point2D.Float P) {
 		
 		//A háromszög pontjai
 		Point2D.Float A = component.triang.A;
 		Point2D.Float B = component.triang.B;
 		Point2D.Float C = component.triang.C;
 		
 		//Vektorok
 		Point2D.Float v0 = new Point2D.Float((float)((C.x-A.x)*scalex),(float)((C.y-A.y)*scaley));
 		Point2D.Float v1 = new Point2D.Float((float)((B.x-A.x)*scalex),(float)((B.y-A.y)*scaley));
 		Point2D.Float v2 = new Point2D.Float((float)(P.x-A.x*scalex),(float)(P.y-A.y*scaley));
 		
 		double invDenom = 1 / (v0.x * v1.y - v0.y * v1.x);
 		double u = (v2.x * v1.y - v1.x * v2.y) * invDenom;
 		double v = (v0.x * v2.y - v2.x * v0.y) * invDenom;
 		
 		return (u >= 0) && (v >= 0) && (u + v < 1);
 		
 	}
 	
 	//Meghívja az ellenörzést a komponens család minden komponensére
 	public boolean checkIfInsideTriang(LinkedList <FractalComponent> fComponentList, Point2D.Float p) {
 		
 		for (FractalComponent component : fComponentList) {
 			if (isInsideTriangle(component,p)) {
 				draggableX = p.x - component.triang.A.x * scalex;
 				draggableY = p.y - component.triang.A.y * scaley;
 				changeComponent = component;
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	//Ellenörzöm, hogy az egér poziciója valamelyik háromszögen belül van-e
 	public boolean isInsideTriang(int mouseX,int mouseY) {
 		
 		float x = (float)(mouseX - width / 2);
 		float y = (float)(mouseY - height / 2);
 		
 		Point2D.Float p = new Point2D.Float(x,y);
 		
 		//Ellenörzöm mind a két fraktál komponensei esetében
 		return (checkIfInsideTriang(fComponentList,p) || checkIfInsideTriang(gComponentList,p) );
 	}
 	
 	//Ez az algoritmus hajtsa végre az ellenörzést
 	public boolean checkIfInsideOval(LinkedList <FractalComponent> fComponentList, Point2D.Float p) {
 		
 		//Bejárom az összes komponenst és ellenörzöm mind a három csúcspont esetében, hogy az egér a csúcspontra mutat-e
 		for (FractalComponent component : fComponentList) {			
 			
 			if (Math.pow(component.triang.A.x * scalex - p.x,2) + Math.pow(component.triang.A.y * scaley - p.y,2) < Math.pow(radius,2)) {
 				resizePoint = component.triang.A;
 				changeComponent = component;				
 				return true;
 			}
 			
 			if (Math.pow(component.triang.B.x * scalex - p.x,2) + Math.pow(component.triang.B.y * scaley - p.y,2) < Math.pow(radius,2)) {
 				resizePoint = component.triang.B;
 				changeComponent = component;				
 				return true;
 			}
 			
 			if (Math.pow(component.triang.C.x * scalex - p.x,2) + Math.pow(component.triang.C.y * scaley - p.y,2) < Math.pow(radius,2)) {
 				resizePoint = component.triang.C;
 				changeComponent = component;				
 				return true;
 			}			
 		}
 		
 		return false;
 	}
 	
 	//Ellenörzöm, hogy az egér poziciója a köröcskén belül van-e
 	public boolean isInsideOval(int mouseX, int mouseY) {
 		
 		float x = (float)(mouseX - width / 2);
 		float y = (float)(mouseY - height / 2);
 		
 		Point2D.Float p = new Point2D.Float(x,y);		
 		
 		//Ellenörzöm mind a két fraktál összes komponensére
 		return (checkIfInsideOval(fComponentList,p) || checkIfInsideOval(gComponentList,p));
 	}
 	
 	//Frissíti a canvas F fraktál komponenseit tartalmazó listájat
 	public void setFComponentList(LinkedList<FractalComponent> fComponentList) {
 		this.fComponentList = fComponentList;
 		repaint();
 	}
 
 	//Frissíti a canvas G fraktál komponenseit tartalmazó listájat
 	public void setGComponentList(LinkedList<FractalComponent> gComponentList) {
 		this.gComponentList = gComponentList;
 		repaint();
 	}
 
 	public boolean isfVisible() {
 		return fVisible;
 	}
 
 	public void setfVisible(boolean fVisible) {
 		this.fVisible = fVisible;
 		repaint();
 	}
 	
 	public boolean isgVisible() {
 		return gVisible;
 	}	
 
 	public void setgVisible(boolean gVisible) {
 		this.gVisible = gVisible;
 		repaint();
 	}
 
 	public void setCurves(LinkedList<Curves> cList) {
 		this.cList = cList;
 		repaint();
 	}
 
 	public boolean isControlsVisible() {
 		return controlsVisible;
 	}
 
 	public void setControlsVisible(boolean cVisible) {
 		this.controlsVisible = cVisible;
 	}
 
 	public boolean isCurvesVisible() {
 		return curvesVisible;
 	}
 
 	public void setCurvesVisible(boolean curvVisible) {
 		this.curvesVisible = curvVisible;
 	}
 
 	public LinkedList<FractalComponent> gethComponentList() {
 		return hComponentList;
 	}
 
 	public void sethComponentList(LinkedList<FractalComponent> hComponentList) {
 		this.hComponentList = hComponentList;
 	}	
 	
 }
