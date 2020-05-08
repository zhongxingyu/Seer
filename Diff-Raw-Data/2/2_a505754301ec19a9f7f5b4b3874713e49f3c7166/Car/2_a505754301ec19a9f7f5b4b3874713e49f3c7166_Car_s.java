 package racing;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 
 public class Car extends JComponent {
 
 	private static final long serialVersionUID = -8147987130820080891L;
 	public static final double SLOW_SPEED = 1;
 	public static final double MAX_FORWARD_SPEED = 6;
 	public static final double MAX_BACKWARD_SPEED = -2;
 	private double x = 0, y = 0, oldX = 0, oldY = 0;
 	private float angle = 0;
 	private double speed = 0.0;
 
 	public Car() {
 	}
 
 	@Override
 	public void move(final int x, final int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public void move(final double x, final double y) {
 		oldX = this.x;
 		oldY = this.y;
 		this.x = x;
 		this.y = y;
 	}
 
 	public void move() {
 		final double newX = x + Math.sin(angle) * speed;
 		final double newY = y - Math.cos(angle) * speed;
 		move(newX, newY);
 	}
 
 	@Override
 	protected void paintComponent(final Graphics g) {
 		super.paintComponent(g);
 		final Graphics2D g2d = (Graphics2D) g;
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		/*
 		 * Verschieben des Zeichenrasters um x und y drehen der Fl�che um den
 		 * Winkle angle
 		 */
 		g2d.translate(x, y);
 		g2d.rotate(angle);
 
 		// Beispielzeichnung ersetzten durch ihre eigene
 		g2d.setPaint(Color.red);
 		g2d.fillRect(-15, -20, 30, 40);
 		g2d.setPaint(new Color(0x727272));
 		g2d.fillRect(-20, -18, 5, 10);
 		g2d.fillRect(15, -18, 5, 10);
 		g2d.fillRect(-20, 8, 5, 10);
 		g2d.fillRect(15, 8, 5, 10);
 
 		g2d.setPaint(Color.yellow);
 		g2d.fillRect(-12, 10, 24, 5);
 
 		g2d.setPaint(new Color(0xBFD7FF));
 		g2d.fillPolygon(new int[] { 0, -10, 10 }, new int[] { -17, 5, 5 }, 3);
 
 	}
 
 	private int doubleToInt(final double doubleValue) {
 		final int baseInt = (int) doubleValue;
 		if (doubleValue - baseInt >= 0.5) {
 			return baseInt + 1;
 		} else {
 			return baseInt;
 		}
 	}
 
 	public void accelerate() {
 		if (speed <= MAX_FORWARD_SPEED) {
 			speed += 0.1;
 		}
 	}
 
 	public void deccelerate() {
 		if (speed >= MAX_BACKWARD_SPEED) {
 			speed -= 0.1;
 		}
 	}
 
 	public void slowdown() {
 		slowdown(1);
 	}
 
 	public void slowdown(final double factor) {
 		speed -= 0.2 * factor;
 		if (speed < 0) {
 			speed = 0;
 		}
 	}
 
 	public void slowdownBackward(final double factor) {
 		speed += 0.2 * factor;
 		if (speed > 0) {
 			speed = 0;
 		}
 	}
 
 	public void setSlowSpeed() {
 		if (speed > SLOW_SPEED) {
 			slowdown(2.8);
 		}
 	}
 
 	public void setMinSlowSpeed() {
 		if (speed < -SLOW_SPEED) {
 			speed = -SLOW_SPEED;
 		}
 
 	}
 
 	public void stop() {
 		speed = 0;
 	}
 
 	public void turnRight() {
 		angle += Math.PI / 40;
 	}
 
 	public void turnLeft() {
 		angle -= Math.PI / 40;
 	}
 
 	@Override
 	public int getX() {
 		return doubleToInt(x);
 	}
 
 	@Override
 	public int getY() {
 		return doubleToInt(y);
 	}
 
 	public int getOldY() {
 		return doubleToInt(oldY);
 	}
 
 	public int getOldX() {
 		return doubleToInt(oldX);
 	}
 
 	public float getAngle() {
 		return angle;
 	}
 
 	public double getSpeed() {
 		return speed;
 	}
 
 	public void setAngle(final float angle) {
 		this.angle = angle;
 	}
 
 	public List<Dimension> getCollisionModel() {
 		final List<Dimension> collisionModel = new ArrayList<>();
 
 		collisionModel.add(calculateMatrix(x - 20, y + 20));
 		collisionModel.add(calculateMatrix(x + 20, y + 20));
 		collisionModel.add(calculateMatrix(x - 20, y - 20));
 		collisionModel.add(calculateMatrix(x + 20, y - 20));
 		collisionModel.add(calculateMatrix(x, y + 20));
 		collisionModel.add(calculateMatrix(x, y - 20));
 		collisionModel.add(calculateMatrix(x + 20, y));
 		collisionModel.add(calculateMatrix(x - 20, y));
 		collisionModel.add(calculateMatrix(x, y));
 		return collisionModel;
 	}
 
 	private Dimension calculateMatrix(double absoluteX, double absoluteY) {
 		absoluteX -= x;
 		absoluteY -= y;
 		final double rotatedX = (Math.cos(angle) * absoluteX)
 				- (absoluteY * Math.sin(angle));
 		final double rotatedY = (Math.sin(angle) * absoluteX)
 				+ (absoluteY * Math.cos(angle));
 		final Dimension d = new Dimension();
 		d.setSize(rotatedX + x, rotatedY + y);
 
 		return d;
 	}
 
 	public static void main(final String[] args) {
 		final JFrame f = new JFrame("Racing");
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		final Car c = new Car();
 		c.move(50, 50);
 		f.add(c);
 		f.setSize(200, 240);
 		f.setResizable(false);
 		f.setVisible(true);
 
 	}
 
 	public void bumpBack() {
 		if (speed >= 0) {
			speed = -10;
 		} else {
 			speed = 3;
 		}
 
 	}
 
 }
