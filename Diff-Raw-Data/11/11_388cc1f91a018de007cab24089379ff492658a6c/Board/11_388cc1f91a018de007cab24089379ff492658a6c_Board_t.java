 import java.awt.Color;
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 
 public class Board extends JPanel {
 	private static final long serialVersionUID = 1L;
 	
 	Data data;
 	Menu menu;
 	
 	double border;
 
 	long startTime;
 	
 	public Board(Menu m) {
 		this.setBackground(Color.white);
 		menu = m;
 		init();
 	}
 	
 	public void init() {
         AttrMeta.reset();
         Clas.num = 0;
         
 		data = new Data("datasets/" + Main.filename);
 		
 		border = Math.max(data.entity.length * 4, 500);
		data.randomPosition(border);
		menu.mass = startSpeed() / 10;
 		
 		menu.setMenu(data);
						
 		startTime = System.nanoTime() / (int)1E9;		
 	}
 	
 	private double startSpeed() {
 		double str = 0;
 		for (Conn con : data.conn) {
			str += Math.pow(con.strength - data.avgConn, 3) / Math.sqrt(con.dist() / 2);
 		}
 		return str;
 	}
 	
 	private void step() {
 		for (Conn con : data.conn) {
 			double d = con.dist();
 			if (d < 8 && d > 0) {
 				crash(con);
 			}
 			force(con);
 		}
 		
 		move();
 		shrinkUniverse();
 	}
 	
 	private void move() {
 		for (Entity ent : data.entity) {
 			double x = ent.x, y = ent.y;
 			double d1 = Math.sqrt(Math.pow(x - border, 2) + Math.pow(y - border, 2));
 			if (d1 > border) {
 				x += ent.speedX;
 				y += ent.speedY;
 				double d2 = Math.sqrt(Math.pow(x - border, 2) + Math.pow(y - border, 2));
 				if (d2 > d1) {
 					ent.speedX *= -0.3;
 					ent.speedY *= -0.3;	
 				}
 			}
 			ent.move();
 		}
 	}
 	
 	private void shrinkUniverse() {
 		for (Entity en : data.entity) {
 			double k = menu.shrinkSpeed;
 			en.x = (en.x - border) * k + border;
 			en.y = (en.y - border) * k + border;
 		}
 	}
 	
 	private void crash(Conn con) {
 		//if (!con.e1.clasHidden && !con.e2.clasHidden && con.e1.clas == con.e2.clas) {
 		//	glue(con);
 		//} else {
 		
 			double g;
 			if (con.strength > menu.avgConn) {
 				g = ((con.strength - menu.avgConn) / (con.strength + menu.avgConn) + 3) / 4;
 			} else {
 				g = ((menu.avgConn - con.strength) / (con.strength + menu.avgConn)) / 2 + 1;
 			}
 	
 			double sx = con.e1.speedX * g;
 			double sy = con.e1.speedY * g;
 			con.e1.speedX = con.e2.speedX * g;
 			con.e1.speedY = con.e2.speedY * g;
 			con.e2.speedX = sx;
 			con.e2.speedY = sy;
 		
 			con.moveFor(-(10 - con.dist()));
 		//}
 	}
 	/*
 	private void glue(Conn con) {
 		double g = 0.5;
 		double g1 = 1 - g;
 		
 		double k = 1;
 		
 		double sx = (con.e1.speedX * g + con.e2.speedX * g1) * k;
 		double sy = (con.e1.speedY * g + con.e2.speedY * g1) * k;
 		con.e1.speedX = (con.e2.speedX * g + con.e1.speedX * g1) * k;
 		con.e1.speedY = (con.e2.speedY * g + con.e1.speedY * g1) * k;
 		con.e2.speedX = sx;
 		con.e2.speedY = sy;
 	}
 	*/
 	private void force(Conn con) {
 		double m = Math.pow(con.strength - menu.avgConn, 3);
 		double d = Math.max(con.dist(), 30);
 
 		if (m > 0) {
 		//	d = Math.min(d, 600);
 		} else {
 			m *= 1 + 1E5 / (d * d);
 		}
 
 		double force = (m / Math.sqrt(d));
 
 		force /= menu.mass;
 
 		if (force > 20) force = 20;
 		if (Double.isNaN(force) || Double.isInfinite(force)) force = 0;
 		
 		con.changeSpeed(force);
 	}
 
 	private void reMove() {
 		try {
 			Thread.sleep(10);
 			step();
 			repaint();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		if (System.nanoTime() / (int)1E9 - startTime > 5) {
 			menu.newDistPredict();
 			startTime = System.nanoTime() / (int)1E9;
 		}
 	}
 	
 	public void paint(Graphics g) {
 		super.paintComponent(g);
 		
 		double zoom = ((double)getHeight() / 2) / (border + 50);		
 		g.setColor(new Color(100, 100, 100));
 		double rc = border * zoom * 2;
 		int xc = (int)((double)getWidth() / 2 - rc / 2);
 		int yc = (int)((double)getHeight() / 2 - rc / 2);
 		g.drawOval(xc, yc, (int)rc, (int)rc);
 		
 		int moveX = (int)((border - getWidth() / 2) * zoom);
 		int moveY = (int)((border - getHeight() / 2) * zoom);
 		
 		for (Entity ent : data.entity) {
 			int x = (int)Math.round(ent.x * zoom + ((1 - zoom) * getWidth() / 2));
 			int y = (int)Math.round(ent.y * zoom + ((1 - zoom) * getHeight() / 2));
 							
 			int r = (int)Math.ceil(5 * zoom);
 			g.setColor(ent.color());
 			g.fillOval(x - r - moveX, y - r - moveY, 2 * r, 2 * r);
 		}
 
 		//printColors(g);
 		reMove();
 	}
 	
 }
