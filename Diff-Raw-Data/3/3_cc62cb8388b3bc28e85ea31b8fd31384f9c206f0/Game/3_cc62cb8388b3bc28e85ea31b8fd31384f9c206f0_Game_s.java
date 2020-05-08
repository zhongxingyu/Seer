 import java.util.*;
 import java.util.concurrent.*;
 
 public class Game {
 
 	private static final Game instance = new Game();
 	protected List<Ball> balls;
 	protected List<Joint> joints;
 	private Vektor pointer;
 	private Physics physics;
 	public Ball active;
 	private boolean enablePhysics = true;
 	public boolean useGenerateTimer = false;
 
 	/* game settings and magic numbers below here */
 
 	private double fieldSize = 320;
 	public int width = 600;
 	public int height = 400;
 	private float ballsize = 15;
 	private int refreshInterval = 50;
 
 	private Random rand = new Random();
 	private Spammer spammer = new Spammer(this);
 
 	private Timer calcTimer;
 	private TimerTask calculate = new TimerTask() {
 		public void run() {
 			refresh();
 		}
 	};
 
 	private Timer generateTimer;
 	private TimerTask generatePairs = new TimerTask() {
 		public void run() {
 			if (useGenerateTimer)
 				spammer.randomPair();
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
 		generateTimer.schedule(generatePairs, 1000, 3 * 1000);
 	}
 
 	public static Game instance() {
 		return instance;
 	}
 
 	/* game logic below here */
 	public void refresh() {
 		if (enablePhysics)
 			physics.physik();
 	}
 
 	public void restart() {
 		balls.clear();
 		joints.clear();
 
 	}
 
 	/* balls below here */
 
 	private Ball createBall(Vektor v) {
 		Ball ball = new Ball(v, ballsize);
 		// ball.color = Color.random();
 		balls.add(ball);
 		return ball;
 	}
 
 	protected void createPair(Vektor v) {
 		Vektor pair = new Vektor(1, 0).mul(Joint.defaultLength * 0.6);
 		pair.setAngle(rand.nextDouble() * Math.PI * 2);
 		Vektor pos1 = v.add(pair);
 		Vektor pos2 = v.sub(pair);
 		Ball ball1 = createBall(pos1);
 		Ball ball2 = createBall(pos2);
 		// if (balls.size() % 4 == 0)
 		joints.add(join(ball1, ball2));
 	}
 
 	/* nur benutzen wenn zwei neue Baelle gejoint werden */
 	protected Joint join(Ball a, Ball b) {
 		Joint joint = new Joint(a, b);
 		a.addJoint(joint);
 		b.addJoint(joint);
 		return joint;
 	}
 
 	protected void unJoin(Ball a, Ball b) {
 		for (Joint j : a.jointsWith(b)) {
 			joints.remove(j);
 			a.removeJoint(j);
 			b.removeJoint(j);
 		}
 	}
 
 	public void attachBalls(Collision c) {
 		Ball a = c.a;
 		Ball b = c.b;
 		if ((a == active || b == active)) {
 			if (a.getJoints().size() > 0 && b.getJoints().size() > 0) {
 				if (a.equals(b)) {
 					replaceBall(a, b);
 				}
 			} else {
 				joints.add(join(a, b));
 			}
 		}
 		handleCycles();
 	}
 
 	private void replaceBall(Ball a, Ball b) {
 		boolean shareJointBall = false;
 		for (Ball jp : a.jointBalls()) {
 			if (jp.isJointWith(b))
 				shareJointBall = true;
 		}
 		if (!shareJointBall && !a.isJointWith(b)) {
 			for (Ball jb : b.jointBalls()) {
 				if (!a.isJointWith(jb)) {
 					joints.add(join(a, jb));
 				}
 			}
 			removeBall(b);
 			active = null;
 		}
 
 	}
 
 	private void handleCycles() {
 		List<List<Ball>> cycles = CycleTest.cycleTest(balls);
 		for (List<Ball> cycle : cycles)
 			for (Ball b : cycle)
 				removeBall(b);
 	}
 
 	private Ball collidingBall(Vektor v) {
 		for (Ball b : balls)
 			if (b.isHit(v))
 				return b; // TODO more than one ball is clicked?
 		return null;
 	}
 
 	protected void removeBall(Ball b) {
 		joints.removeAll(b.getJoints());
 		for (Ball jp : b.jointBalls()) {
 			jp.getJoints().removeAll(jp.jointsWith(b));
 		}
 		balls.remove(b);
 	}
 
 	/* interaction below here */
 	public void keyPressed(int key) {
 		switch (key) {
 		case 0:
 			spammer.randomPair();
 			break;
 		case 1:
 			restart();
 			break;
 		case 2:
 			//
 		case 3:
 			enablePhysics = !enablePhysics;
 		case 4:
 			useGenerateTimer = !useGenerateTimer;
 		}
 	}
 
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
