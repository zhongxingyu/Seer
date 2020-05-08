 import java.util.*;
 import java.util.concurrent.*;
 
 public class Game {
 
 	private static final Game instance = new Game();
 	protected List<Ball> balls;
 	protected List<Joint> joints;
 	private Vektor pointer;
 	private Physics physics;
 	public Ball active;
 
 	/* game settings and magic numbers below here */
 
 	private double fieldSize = 320;
 	public int width = 600;
 	public int height = 400;
 	private float ballsize = 15;
 	private int refreshInterval = 50;
 	private int ctprogress;
 
 	private Timer calcTimer;
 	private TimerTask calculate = new TimerTask() {
 		public void run() {
 			refresh();
 		}
 	};
 
 	private Timer generateTimer;
 	private TimerTask generatePairs = new TimerTask() {
 		public void run() {
 			randomPair();
 		}
 	};
 
 	private Game() {
 		balls = new CopyOnWriteArrayList<Ball>();
 		joints = new CopyOnWriteArrayList<Joint>();
 		pointer = getCenter();
 		physics = new Physics(this);
 
 		calcTimer = new Timer();
 		calcTimer.schedule(calculate, 0, refreshInterval);
 		generateTimer = new Timer();
 		// generateTimer.schedule(generatePairs, 1000, 4 * 1000);
 
 	}
 
 	private static Random rand = new Random();
 
 	public static Game instance() {
 		return instance;
 	}
 
 	/* game logic below here */
 	public void refresh() {
 		physics.physik();
 		cycleTest();
 	}
 
 	public void restart() {
 		balls.clear();
 		joints.clear();
 	}
 
 	public void randomPair() {
 		Vektor randPos;
 		// TODO randomPair() generiert keine "unten rechts" Baelle
 		double xMargin = height / 2 - fieldSize / 2;
 		double yMargin = width / 2 - fieldSize / 2;
 		switch (rand.nextInt(3)) {
 		case 0:
 			randPos = new Vektor(rand.nextDouble() * xMargin, rand.nextDouble() * yMargin);
 			break;
 		case 1:
 			randPos = new Vektor(width - rand.nextDouble() * xMargin, rand.nextDouble() * yMargin);
 			break;
 		case 2:
 			randPos = new Vektor(rand.nextDouble() * xMargin, height - rand.nextDouble() * yMargin);
 			break;
 		case 3:
 			randPos = new Vektor(width - rand.nextDouble() * xMargin, height - rand.nextDouble() * yMargin);
 			System.out.println("unten rechts");
 			break;
 		default:
 			randPos = new Vektor(width - rand.nextDouble() * xMargin, height - rand.nextDouble() * yMargin);
 			System.out.println("unten rechts");
 			break;
 		}
 		createPair(randPos);
 	}
 
 	public void cycleTest() {
 		for (Ball b : balls) {
 			b.ctNumber = 0;
 			b.ctCheck = 0;
 		}
 		ctprogress = 1;
 		for (Ball b : balls) {
 			if (b.ctNumber == 0) {
 				cycleTest(b, null);
 			}
 		}
 	}
 
 	public void cycleTest(Ball v, Ball pre) {
 		v.ctNumber = ctprogress;
 		ctprogress++;
 		v.ctCheck = 1;
 
 		List<Ball> post = v.jointBalls();
 		if (pre != null)
 			post.remove(pre);
 		post.remove(v);
 
 		for (Ball w : post) {
 			if (w.ctNumber == 0)
 				cycleTest(w, v);
 			if (w.ctCheck == 1) {
 				/* handle cycle */
 				for (Ball b : balls) {
 					if (b.ctCheck == 1) {
 						/* TODO sometimes removes too many balls */
 						// b.color = new Color(0, 0, 0, "black");
 						removeBall(b);
 					}
 				}
 				// path(v, w);
 				unJoin(v, w);
 			}
 		}
 		v.ctCheck = 2;
 	}
 
 	public void path(Ball a, Ball b) {
 		List<Ball> todo = new CopyOnWriteArrayList<Ball>();
 		for (Ball jp : a.jointBalls()) {
 			if (jp == b) {
 				removeBall(a);
 				removeBall(b);
 			}
 
 			todo.add(jp);
 		}
 		for (Ball t : todo) {
 			for (Ball jp : t.jointBalls()) {
 				if (jp == b) {
 					removeBall(b);
 					path(a, t);
 					return;
 				} else if (!todo.contains(jp)) {
 					todo.add(jp);
 				}
 			}
 		}
 	}
 
 	/* balls below here */
 
 	private Ball createBall(Vektor v) {
 		Ball ball = new Ball(v, ballsize);
 		ball.color = Color.random();
 		balls.add(ball);
 		return ball;
 	}
 
 	private void createPair(Vektor v) {
		Vektor pair = new Vektor(1, 0).mul(Joint.defaultLength * 0.6);
 		pair.setAngle(rand.nextDouble() * Math.PI * 2);
 		Vektor pos1 = v.add(pair);
 		Vektor pos2 = v.sub(pair);
 		Ball ball1 = createBall(pos1);
 		Ball ball2 = createBall(pos2);
 		joints.add(join(ball1, ball2));
 	}
 
 	/* nur benutzen wenn zwei neue Baelle gejoint werden */
 	protected Joint join(Ball a, Ball b) {
 		Joint joint = new Joint(a, b);
 		a.addJoint(joint);
 		b.addJoint(joint);
 		return joint;
 	}
 
 	private void unJoin(Ball a, Ball b) {
 		for (Joint j : a.jointsWith(b)) {
 			joints.remove(j);
 			a.removeJoint(j);
 			b.removeJoint(j);
 		}
 	}
 
 	public void replaceBall(Ball a, Ball b) {
 		/*
 		 * TODO joining with a single Ball should not delete a Ball (single
 		 * balls may occure after eliminating a cycle ) 
 		 */
 		if (!a.isJointWith(b)) { // a haengt nicht an b
 			for (Ball jb : b.jointBalls()) { // alle anhaenger an b
 				if (!a.isJointWith(jb)) { // anhaenger haengt nicht bereits an a
 					joints.add(join(a, jb));
 				}
 			}
 			removeBall(b);
 			active = null;
 		}
 	}
 
 	private Ball collidingBall(Vektor v) {
 		for (Ball b : balls)
 			if (b.isHit(v))
 				return b; // TODO more than one ball is clicked?
 		return null;
 	}
 
 	private void removeBall(Ball b) {
 		joints.removeAll(b.getJoints());
 		for(Ball jp: b.jointBalls()) {
 			jp.getJoints().removeAll(jp.jointsWith(b));
 		}
 		balls.remove(b);
 	}
 
 	/* mouseinteraction below here */
 	public void mouseMoved(Vektor v) {
 		setPointer(v);
 	}
 	
 	public void mousePressedLeft(Vektor v) {
 		Ball b = collidingBall(v);
 		if (b != null) {
 			active = b;
 		}
 	}
 
 	public void mousePressedRight(Vektor v) {
 		createPair(v);
 	}
 
 	public void mouseReleasedLeft(Vektor v) {
 		active = null;
 	}
 
 	public void mouseReleasedRight(Vektor v) {
 	}
 
 	/* getter/setter below here */
 	public List<Ball> getBalls() {
 		return balls;
 	}
 
 	public List<Joint> getJoints() {
 		return joints;
 	}
 
 	public double getFieldSize() {
 		return fieldSize;
 	}
 
 	public int getNumberOfJoints() {
 		return joints.size();
 	}
 
 	protected Vektor getPointer() {
 		return pointer;
 	}
 
 	public Vektor getCenter() {
 		return new Vektor(width / 2, height / 2);
 	}
 
 	public void setPointer(Vektor pointer) {
 		this.pointer = pointer;
 	}
 }
