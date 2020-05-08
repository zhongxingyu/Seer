 package nehsics;
 import nehsics.math.*;
 import static nehsics.math.Util.*;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.Color;
 import java.awt.geom.AffineTransform;
 import java.util.*;
 import java.awt.geom.*;
 public abstract class Body {
 	// tmp forces will be cleared after each step!
 	protected Set<Vector2d> tmp = new HashSet<Vector2d>();
 	protected Set<Vector2d> forces = new HashSet<Vector2d>();
 	protected double mass; // kg
 	protected double charge; // coulombs
 	protected double radius;
     protected List<Body> alreadyHit = new LinkedList<Body>();
 	protected Vector2d position; // m (pixels)
 	protected Vector2d velocity; // m/s
 	protected Shape shape;
 	protected boolean visible = true;
 	protected boolean temperatureColor;
 	protected Color color = Color.black;
 
 	public Body(Vector2d pos, Vector2d vel, double m, Shape s) {
 		position = pos;
 		velocity = vel;
 		mass = m;
 		shape = s;
 	}
 	public boolean intersectsRectangle(Rectangle2D r)
 	{
 		AffineTransform af = AffineTransform.getTranslateInstance(
 			position.getX()-radius, position.getY()-radius);
 		Shape transformed = af.createTransformedShape(shape);
 		return transformed.intersects(r);
 	}
 	public void setVisible(boolean v) {
 		visible = v;
 	}
 
 	public void setRadius(double r) {
 		radius = r;
 	}
 	public void setTemperatureColor(boolean t)
 	{
 		temperatureColor = t;
 	}
 	public void step(double dt) { // seconds
 		Vector2d accel = v(), disp = v();
 		for (Vector2d f : forces)
 			accel = add(accel, scale(f, 1/mass));
 		for (Vector2d f : tmp)
 			accel = add(accel, scale(f, 1/mass));
 		velocity = add(velocity, scale(accel, dt));
 		position = add(position, scale(velocity, dt));
 		alreadyHit.clear();
 		tmp.clear();
 	}
 
 	public void applyForce(Vector2d f) {
 		tmp.add(f);
 	}
 
 	public void addForce(Vector2d f) {
 		forces.add(f);
 	}
 
 	public void setShape(Shape shape) {
 		this.shape = shape;
 	}
 
 	public double getMass() {
 		return mass;
 	}
 
 	public void setMass(double m) {
 		mass = m;
 	}
 
 	public double getCharge() {
 		return charge;
 	}
 
 	public void setCharge(double c) {
 		charge = c;
 	}
 
 	public Vector2d getPosition() {
 		return position;
 	}
 
 	public Vector2d getVelocity() {
 		return velocity;
 	}
 
 	public void setVelocity(Vector2d v) {
 		velocity = v;
 	}
 
 	public void setPosition(Vector2d position) {
 		this.position = position;
 	}
 	public Color getColor(int iter, int maxiter)
 	{
 		double r = Math.abs(Math.sin(iter*Math.PI*2/maxiter)*255);
 		double g = Math.abs(Math.cos(iter*Math.PI*2/maxiter)*255);
 		double b = Math.abs(Math.sin((iter*Math.PI*2/maxiter)+.7)*255);
 		return new Color((int)r,(int)g,(int)b);
 	}
 	public Color calculateColor()
 	{
 		Color c;
 		double maxK = 8000000;
 		double k = .5*mass*Math.pow(velocity.length(),2);
 		double fraction = k/maxK;
 		if(fraction>1)
 			c = new Color(255,0,0);
 		else if(fraction<1.0/2.0)
 			c = new Color((int)(255*2*fraction),(int)(255*2*fraction),(int)(255-(255*2*fraction)));
 		else
 			c = new Color((int)(255),(int)(255-(255*2*(fraction-.5))),(int)(0));
 		return c;
 		//return getColor((int)k,(int)maxK);
 	}
 	public void paint(Graphics2D g2d) {
 		if(!visible)
 			return;
 		if(temperatureColor)
 			color = calculateColor();
 		g2d.setColor(color);
 		AffineTransform af = AffineTransform.getTranslateInstance(
 			position.getX()-radius, position.getY()-radius);
 		Shape transformed = af.createTransformedShape(shape);
 		g2d.fill(transformed);
 	}
	public void setColor(Color c)
	{
		color = c;
	}
 	public Set<Vector2d> getForces() {
 		return forces;
 	}
 }
