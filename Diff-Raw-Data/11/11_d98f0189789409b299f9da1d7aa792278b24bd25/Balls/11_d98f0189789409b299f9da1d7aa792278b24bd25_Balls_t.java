 import java.util.ArrayList;
 
 public class Balls implements Runnable {
 	ArrayList<Ball> ballList;
 	Game gui;
 	final static int MAXIMUM_ANGLE = 75;
 	public Balls(Game game) {
 		// TODO Auto-generated constructor stub
 		ballList = new ArrayList<Ball>();
 		gui = game;
 	}
 
 	public boolean isEmpty() {
 		return ballList.isEmpty();
 	}
 
 	public ArrayList<Ball> getList() {
 		return ballList;
 	}
 
 	public void add(double x, double y, double dx, double dy, double radius) {
 		ballList.add(new Ball(x, y, dx, dy, radius));
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		Ball b;
 		while (true) {
 			for (int i = 0; i < ballList.size(); i++) {
 				b = ballList.get(i);
 				b.setX(b.getX() + b.getDX());
 				b.setY(b.getY() + b.getDY());
 				// TODO Remove X Bounce
 				/*
 				 *  X Bounce
 				 */
 				if (b.getX() - b.getRadius() < 0
 						|| b.getX() + b.getRadius() > gui.getGUIWidth()) {
 					b.setDX(-b.getDX());
 				}
 				/*
 				 *  Y Bounce
 				 */
				if (b.getY() - b.getRadius() < 0 && b.getDY() < 0
						|| b.getY() + b.getRadius() > gui.getGUIHeight() && b.getDY() > 0) {
 					b.setDY(-b.getDY());
 				}
 				/*
 				 *  Paddle1 Bounce
 				 */
 				Paddle paddle1 = gui.getPaddle1();
 				if (b.getX() - b.getRadius() <= paddle1.getX()
 						+ paddle1.getThick()
 						&& b.getX() - b.getRadius() > paddle1.getX()
 								- paddle1.getThick()
 						&& paddle1.isRangeY(b.getY()) && b.getDX() < 0) {
 					double x = paddle1.getLength() / Math.tan(Math.toRadians(MAXIMUM_ANGLE)) / 2;
 					double theta = Math.atan((b.getY() - paddle1.getY()) / x);
 					double phi = -Math.atan(b.getDY() / b.getDX());
 					double phi2;
 //					System.out.println("theta:"+Math.toDegrees(theta)+" phi:"+Math.toDegrees(phi));
 					if(theta == 0)
 						phi2 = phi;
 					else if(theta > 0)
 					{
 						if(phi >= theta)
 							phi2  = (Math.toRadians(90) + phi)/2;
 						else
 							phi2 = (phi+theta)/2;
 					}
 					else
 					{
 						if(phi <= theta)
 							phi2 = (Math.toRadians(-90) + phi)/2;
 						else
 							phi2 = (phi+theta)/2;
 					}
					if(phi > Math.toRadians(MAXIMUM_ANGLE))
						phi = Math.toRadians(MAXIMUM_ANGLE);
					if(phi < Math.toRadians(-MAXIMUM_ANGLE))
						phi = Math.toRadians(-MAXIMUM_ANGLE);
					if(Math.abs(phi) < Math.toRadians(5))
						phi = Math.random()*10-5;
 //					System.out.println("phi2: "+Math.toDegrees(phi2));
 					double v = Math.sqrt(Math.pow(b.getDX(), 2)+Math.pow(b.getDY(), 2));
 					b.setDX(Math.cos(phi2)*v);
 					b.setDY(Math.sin(phi2)*v);
 				}
 				
 				/*
 				 *  Paddle2 Bounce
 				 */
 				Paddle paddle2 = gui.getPaddle2();
 				if (b.getX() + b.getRadius() <= paddle2.getX()
 						+ paddle2.getThick()
 						&& b.getX() + b.getRadius() > paddle2.getX()
 								- paddle2.getThick()
 						&& paddle2.isRangeY(b.getY()) && b.getDX() > 0) {
 					double x = paddle2.getLength() / Math.tan(Math.toRadians(MAXIMUM_ANGLE)) / 2;
 					double theta = Math.atan((b.getY() - paddle2.getY()) / x);
 					double phi = -Math.atan(b.getDY() / b.getDX());
 					double phi2;
 //					System.out.println("theta:"+Math.toDegrees(theta)+" phi:"+Math.toDegrees(phi));
 					if(theta == 0)
 						phi2 = phi;
 					else if(theta > 0)
 					{
 						if(phi >= theta)
 							phi2  = phi;
 						else
 							phi2 = (phi+theta)/2;
 					}
 					else
 					{
 						if(phi <= theta)
 							phi2 = phi;
 						else
 							phi2 = (phi+theta)/2;
 					}
 //					System.out.println("phi2: "+Math.toDegrees(phi2));
 					double v = Math.sqrt(Math.pow(b.getDX(), 2)+Math.pow(b.getDY(), 2));
 					b.setDX(-Math.cos(phi2)*v);
 					b.setDY(Math.sin(phi2)*v);
 				}
 				
 			}
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
