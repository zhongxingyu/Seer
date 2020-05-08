 package hud;
 
 import hud.HeavyHudPanel.HudMotionListener;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Path2D;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.Point2D.Double;
 
 import javax.swing.JPanel;
 
 import projections.LambertAzimuthalProjection;
 import projections.Projection;
 import projections.PerspectiveProjection;
 
 public class HeavyHudPanel extends JPanel {
 
 	private double pitch; // pitch
 	private double yaw; // yaw
 	private double roll; // roll
 
 	private Path2D horizon;
 	private Path2D northMeridian;
 	private Path2D southMeridian;
 	private Path2D eastMeridian;
 
 	private Path2D parallelp45;
 	private Path2D parallelm45;
 	private Path2D meridianp45;
 	private Path2D meridianm45;
 
 
 	private Point2D target;
 
 
 	private Projection projection;
 
 	private static Color GROUND_COLOR = new Color(200, 113, 55);
 	private static Color SKY_COLOR = new Color(0, 102, 255);
 
 	public HeavyHudPanel() {
 		super();
 		this.pitch = Math.toRadians(0);
 		this.yaw = Math.toRadians(0);
 		this.roll = Math.toRadians(0);
 
 		this.setTarget(new Point2D.Double(34., 52.));
 
 		this.horizon = new Path2D.Double();
 		this.northMeridian = new Path2D.Double();
 		this.southMeridian = new Path2D.Double();
 		this.eastMeridian = new Path2D.Double();
 		this.parallelp45 = new Path2D.Double();
 		this.parallelm45 = new Path2D.Double();
 		this.meridianp45 = new Path2D.Double();
 		this.meridianm45 = new Path2D.Double();
 
 		this.horizon.moveTo(0., -1. * Math.PI);
 		this.northMeridian.moveTo(Math.PI / 2., 0.);
 		this.southMeridian.moveTo(Math.PI / 2., 0.);
 		this.eastMeridian.moveTo(Math.PI / 2., Math.PI / 2.);
 		this.parallelp45.moveTo(Math.PI / 4.,  -1. * Math.PI);
 		this.parallelm45.moveTo(-1.* Math.PI / 4., -1. * Math.PI);
 		this.meridianp45.moveTo(Math.PI / 2., Math.PI / 4.);
 		this.meridianm45.moveTo(Math.PI / 2., -1.*Math.PI / 4.);
 
 		int nbpts = 1800;
 		double pos = 0.;
 		for (int i = 0; i < nbpts; i++) {
 			pos = i / (1. * nbpts);
 			this.horizon.lineTo(0., -1. * Math.PI + pos * Math.PI);
 			this.northMeridian.lineTo(Math.PI / 2. - pos * Math.PI, 0.);
 			this.southMeridian.lineTo(Math.PI / 2. - pos * Math.PI,Math.PI);
 			this.eastMeridian.lineTo(Math.PI / 2. - pos * Math.PI, Math.PI / 2.);
 
 			this.parallelp45.lineTo(Math.PI / 4.,-1. * Math.PI + pos * Math.PI);
 			this.parallelm45.lineTo(-1.* Math.PI / 4., -1. * Math.PI + pos * Math.PI);
 			this.meridianp45.lineTo(Math.PI / 2. - pos * Math.PI, Math.PI / 4.);
 			this.meridianm45.lineTo(Math.PI / 2. - pos * Math.PI, -1.*Math.PI / 4.);
 
 		}
 		for (int i = 0; i < nbpts; i++) {
 			pos = i / (1. * nbpts);
 			this.horizon.lineTo(0., pos * Math.PI);
 
 			this.eastMeridian.lineTo(-1. * Math.PI / 2. + pos * Math.PI, -1.	* Math.PI / 2.);
 
 			this.parallelp45.lineTo(Math.PI / 4., pos * Math.PI);
 			this.parallelm45.lineTo(-1.* Math.PI / 4.,pos * Math.PI);
 			this.meridianp45.lineTo(-1. * Math.PI / 2. + pos * Math.PI, Math.PI+Math.PI / 4.);
 			this.meridianm45.lineTo(-1. * Math.PI / 2. + pos * Math.PI, Math.PI-1.*Math.PI / 4.);
 
 		}
 
 		this.projection = new LambertAzimuthalProjection();
 		//this.projection = PerspectiveProjection.STEREOGRAPHIC_PROJECTION;
 		// this.projection = PerspectiveProjection.GNOMONIC_PROJECTION;
 		// this.projection = PerspectiveProjection.ORTHOGRAPHIC_PROJECTION;
 
 		this.setMinimumSize(new Dimension(320, 240));
 		this.setPreferredSize(new Dimension(640, 480));
 
 		HudMotionListener listener = new HudMotionListener(this);
 
 		this.addMouseListener(listener);
 		this.addMouseMotionListener(listener);
 		this.addKeyListener(listener);
 		this.setFocusable(true);
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		// TODO Auto-generated method stub
 		super.paint(g);
 		Graphics2D g2 = (Graphics2D) g.create();
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 
 		this.projection.setLambda(this.yaw);
 		this.projection.setPhi(this.pitch);
 
 		double scale = 0.25 * Math.min(this.getSize().getWidth(), this
 				.getSize().getHeight());
 		AffineTransform transformer = new AffineTransform();
 		transformer.concatenate(AffineTransform.getRotateInstance(this.roll,0.5 * this
 				.getSize().getWidth(), 0.5 * this.getSize().getHeight()));
 		transformer.concatenate(AffineTransform.getTranslateInstance(0.5 * this
 				.getSize().getWidth(), 0.5 * this.getSize().getHeight()));
 		transformer.concatenate(AffineTransform.getScaleInstance(scale, scale));
 
 		Path2D horizonT = projection.project(horizon);
 		Path2D northMeridianT = projection.project(northMeridian);
 		Path2D southMeridianT = projection.project(southMeridian);
 		Path2D eastMeridianT = projection.project(eastMeridian);
 		Path2D parallelp45T = projection.project(parallelp45);
 		Path2D parallelm45T = projection.project(parallelm45);
 		Path2D meridianp45T = projection.project(meridianp45);
 		Path2D meridianm45T = projection.project(meridianm45);
 
 		horizonT.transform(transformer);
 		northMeridianT.transform(transformer);
 		southMeridianT.transform(transformer);
 		eastMeridianT.transform(transformer);
 		parallelp45T.transform(transformer);
 		parallelm45T.transform(transformer);
 		meridianp45T.transform(transformer);
 		meridianm45T.transform(transformer);
 
 		if (Math.sin(this.pitch) >= 0) {
 			g2.setBackground(SKY_COLOR);
 			g2.clearRect(0, 0, this.getWidth(), this.getHeight());
 			g2.setPaint(GROUND_COLOR);
 			g2.fill(horizonT);
 
 		} else {
 			g2.setBackground(GROUND_COLOR);
 			g2.clearRect(0, 0, this.getWidth(), this.getHeight());
 			g2.setPaint(SKY_COLOR);
 			g2.fill(horizonT);
 
 		}
 		g2.setPaint(Color.WHITE);
 		g2.setStroke(new BasicStroke(1));
 		g2.draw(horizonT);
 
 		g2.setPaint(Color.WHITE);
 		g2.setStroke(new BasicStroke(3));
 		g2.draw(northMeridianT);
 
 		g2.setPaint(Color.WHITE);
 		g2.setStroke(new BasicStroke(1));
 		g2.draw(southMeridianT);
 
 		g2.setPaint(Color.WHITE);
 		float[] dashPattern = { 5, 10 };
 		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND,
 				BasicStroke.JOIN_ROUND, 10, dashPattern, 0));
 		g2.draw(eastMeridianT);
 
 		g2.setPaint(new Color(255,255,255,100));
 		float[] dashPattern2 = { 2, 4 };
 		g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND,
 				BasicStroke.JOIN_ROUND, 10, dashPattern2, 0));
 		g2.draw(parallelp45T);
 		g2.draw(parallelm45T);
 		g2.draw(meridianp45T);
 		g2.draw(meridianm45T);
 
 
 
 		double size=Math.min(this.getBounds().getWidth(),this.getBounds().getHeight());
 
 		Shape bound = new Ellipse2D.Double((this.getBounds().getWidth()-size)/2, (this.getBounds().getHeight()-size)/2,size,size);
 		Path2D mask = new Path2D.Double(this.getBounds());
 		mask.append(bound, false);
 		mask.setWindingRule(Path2D.WIND_EVEN_ODD);
 
 		g2.setStroke(new BasicStroke(2));
 		g2.setPaint(Color.BLACK);
 		g2.fill(mask);
 		g2.setPaint(Color.WHITE);
 		g2.draw(bound);
 
		Shape center = new Ellipse2D.Double(0.5 * this.getBounds().getWidth()
				- 6, 0.5
				* this.getBounds().getHeight()- 6, 12, 12);
 
 		g2.setPaint(Color.WHITE);
 		g2.draw(center);
 
 
 
 		Point2D targetT=projection.project(this.getTarget());
 		transformer.transform(targetT, targetT);
 		Path2D targetPath = new Path2D.Double();
 		targetPath.moveTo(targetT.getX()-6, targetT.getY()-6);
 		targetPath.lineTo(targetT.getX()+6, targetT.getY()+6);
 		targetPath.moveTo(targetT.getX()-6, targetT.getY()+6);
 		targetPath.lineTo(targetT.getX()+6, targetT.getY()-6);
 		g2.setPaint(Color.YELLOW);
 		g2.draw(targetPath);
 
 		g2.dispose();
 
 	}
 
 	public double getLambda() {
 		return yaw;
 	}
 
 	public void setLambda(double lambda) {
 
 		this.yaw = ((lambda+Math.PI)%(2.*Math.PI))-Math.PI;
 	}
 
 	public double getPhi() {
 		return pitch;
 	}
 
 	public void setPhi(double phi) {
 		this.pitch = ((phi+Math.PI/2.)%Math.PI)-Math.PI/2.;
 	}
 
 	public double getPsi() {
 		return roll ;
 	}
 
 	public void setPsi(double psi) {
 		this.roll = ((psi+Math.PI/2.)%Math.PI)-Math.PI/2.;
 	}
 
 	public Point2D getTarget() {
 		return target;
 	}
 
 	public void setTarget(Point2D target) {
 		this.target = target;
 	}
 
 	public class HudMotionListener implements MouseListener,
 	MouseMotionListener, KeyListener {
 
 		private HeavyHudPanel hud;
 
 		private Point2D lastPoint;
 		private Point2D lastPos;
 
 		public HudMotionListener(HeavyHudPanel hud) {
 			super();
 			this.hud = hud;
 		}
 
 		public Point2D getRelativePosition(Point2D position) {
 			double size=Math.min(hud.getBounds().getWidth(),hud.getBounds().getHeight());
 
 			double x=(position.getX()-(1.*hud.getBounds().getWidth()-size)/2.)/size;
 			double y=(position.getY()-(1.*hud.getBounds().getHeight()-size)/2.)/size;
 
 			x=x*4.-2.;
 			y=y*4.-2.;
 
 			return new Point2D.Double(x,y);
 
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			if(arg0.getButton()==MouseEvent.BUTTON1) {
 				Point2D relativePos=this.getRelativePosition(arg0.getPoint());
 				if(relativePos.distance(0., 0.)<2) {
 					this.hud.setTarget(this.hud.projection.inverse(relativePos));
 				}
 			}
 			else {
 				Point2D relativePos=this.getRelativePosition(arg0.getPoint());
 				if(relativePos.distance(0., 0.)<2) {
 					this.hud.setPhi(this.hud.projection.inverse(relativePos).getX());
 					this.hud.setLambda(this.hud.projection.inverse(relativePos).getY());
 					this.hud.setPsi(0.);
 				}
 				
 			}
 
 			this.hud.repaint();
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent arg0) {
 
 		}
 
 		@Override
 		public void mouseExited(MouseEvent arg0) {
 
 		}
 
 		@Override
 		public void mousePressed(MouseEvent arg0) {
 			Point2D relativePos=this.getRelativePosition(arg0.getPoint());
 			if(relativePos.distance(0., 0.)<2) {
 				this.lastPoint=this.hud.projection.inverse(relativePos);
 				this.lastPos=new Point2D.Double(this.hud.getPhi(),this.hud.getLambda());
 			}
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent arg0) {
 
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent arg0) {
 
 			Point2D relativePos=this.getRelativePosition(arg0.getPoint());
 			if(relativePos.distance(0., 0.)<2) {
 				Point2D arg=this.hud.projection.inverse(relativePos);
 
 				double x = arg.getX() - this.lastPoint.getX();
 				double y = arg.getY() - this.lastPoint.getY();
 
				System.out.println("---------------------------\n" + arg + "\n" + this.lastPoint);
				
 				this.hud.setPhi(this.lastPos.getX() -x);
 				this.hud.setLambda( this.lastPos.getY() -y );
		
 
				this.lastPoint=this.hud.projection.inverse(relativePos);
 				this.lastPos=new Point2D.Double(this.hud.getPhi(),this.hud.getLambda());
 				this.hud.repaint();
 			}
 
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent arg0) {
 
 		}
 
 		@Override
 		public void keyPressed(KeyEvent arg0) {
 
 			switch (arg0.getKeyCode()) {
 			case KeyEvent.VK_LEFT:
 				this.hud.setPsi(this.hud.getPsi() + Math.toRadians(1));
 				break;
 			case KeyEvent.VK_RIGHT:
 				this.hud.setPsi(this.hud.getPsi() + Math.toRadians(-1));
 				break;
 			case KeyEvent.VK_UP:
 				this.hud.setPhi(this.hud.getPhi() + Math.toRadians(1)*Math.cos(this.hud.getPsi()));
 				this.hud.setLambda(this.hud.getLambda() + Math.toRadians(-1)*Math.sin(this.hud.getPsi()));
 				break;
 			case KeyEvent.VK_DOWN:
 				this.hud.setPhi(this.hud.getPhi() + Math.toRadians(-1)*Math.cos(this.hud.getPsi()));
 				this.hud.setLambda(this.hud.getLambda() + Math.toRadians(+1)*Math.sin(this.hud.getPsi()));
 				break;
 			default:
 				break;
 			}
 			this.hud.repaint();
 
 		}
 
 		@Override
 		public void keyReleased(KeyEvent arg0) {
 
 		}
 
 		@Override
 		public void keyTyped(KeyEvent arg0) {
 
 		}
 	}
 }
