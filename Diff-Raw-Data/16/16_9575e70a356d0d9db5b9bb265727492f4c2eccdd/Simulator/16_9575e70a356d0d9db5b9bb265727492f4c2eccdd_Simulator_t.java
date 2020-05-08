 package balle.simulator;
 
 import java.util.Random;
 
 import javax.swing.JFrame;
 import javax.swing.UIManager;
 
 import org.jbox2d.collision.Collision;
 import org.jbox2d.collision.shapes.CircleShape;
 import org.jbox2d.collision.shapes.PolygonShape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.FixtureDef;
 import org.jbox2d.dynamics.World;
 import org.jbox2d.dynamics.joints.FrictionJointDef;
 import org.jbox2d.dynamics.joints.WeldJointDef;
 import org.jbox2d.testbed.framework.TestbedFrame;
 import org.jbox2d.testbed.framework.TestbedModel;
 import org.jbox2d.testbed.framework.TestbedPanel;
 import org.jbox2d.testbed.framework.TestbedTest;
 import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
 
 import balle.io.listener.Listener;
 import balle.io.reader.AbstractVisionReader;
 import balle.io.reader.Reader;
 import balle.misc.Globals;
 
 public class Simulator extends TestbedTest implements AbstractVisionReader {
 
 	// Increases sizes, but keeps real-world scale; jbox2d acts unrealistically
 	// at a small scale
 
 	private static boolean noisy;
 
 	protected final float scale = 10;
 
 	private Body ground;
 	protected Body ball;
 
 	protected Robot blue;
 	protected Robot yellow;
 
 	private final SoftBot blueSoft = new SoftBot();
 	private final SoftBot yellowSoft = new SoftBot();
 
 	private long startTime;
 
 	private CircleShape ballShape;
 	private final Random rand = new Random();
 
 	private long lastFrameTime;
 
 	public SoftBot getBlueSoft() {
 		return blueSoft;
 	}
 
 	public SoftBot getYellowSoft() {
 		return yellowSoft;
 	}
 
 	@Override
 	public String getTestName() {
 		return "Super Cool Simulator";
 	}
 
 	public boolean isNoisy(boolean noise) {
 		return noisy = noise;
 	}
 
 	private void addEdge(float x1, float y1, float x2, float y2) {
 		World w = getWorld();
 		PolygonShape r = new PolygonShape();
 		r.setAsEdge(new Vec2(x1, y1), new Vec2(x2, y2));
 		FixtureDef f = new FixtureDef();
 		f.shape = r;
 		// TODO find real restitution
 		f.restitution = 0.4f;
 		BodyDef b = new BodyDef();
 		b.type = BodyType.STATIC;
 		// b.position.set(10f,10f);
 		w.createBody(b).createFixture(f);
 
 	}
 
 	@Override
 	public void initTest(boolean arg0) {
 
 		startTime = System.currentTimeMillis();
 
 		// centre the camera
 		setCamera(new Vec2(1.22f * scale, 1.22f * scale),
 				getCachedCameraScale());
 		// No Gravity
 		World w = getWorld();
 		w.setGravity(new Vec2(0, 0));
 
 		// create ground
 		PolygonShape groundShape = new PolygonShape();
 		groundShape.setAsBox(2.64f * scale, 1.42f * scale);
 		BodyDef groundBodyDef = new BodyDef();
 		groundBodyDef.type = BodyType.STATIC;
 		groundBodyDef.position.set(1.22f * scale, 0.61f * scale);
 		ground = w.createBody(groundBodyDef);
 		FixtureDef groundF = new FixtureDef();
 		groundF.shape = groundShape;
 		groundF.density = (1f / 0.36f) / scale;
 		groundF.filter.maskBits = 0;
 		ground.createFixture(groundF);
 
 		// Create edge of field
 		// Main Pitch
 		addEdge(0f * scale, 0f * scale, 2.44f * scale, 0f * scale);
 		addEdge(0f * scale, 1.22f * scale, 2.44f * scale, 1.22f * scale);
 		addEdge(0f * scale, 0f * scale, 0f * scale, 0.31f * scale);
 		addEdge(2.44f * scale, 0f * scale, 2.44f * scale, 0.31f * scale);
 		addEdge(0f * scale, 1.22f * scale, 0f * scale, 0.91f * scale);
 		addEdge(2.44f * scale, 1.22f * scale, 2.44f * scale, 0.91f * scale);
 		// Left-hand goal area
 		addEdge(0f * scale, 0.31f * scale, -0.1f * scale, 0.31f * scale);
 		addEdge(-0.1f * scale, 0.31f * scale, -0.1f * scale, 0.91f * scale);
 		addEdge(2.44f * scale, 0.31f * scale, 2.54f * scale, 0.31f * scale);
 		// Right-hand goal area
 		addEdge(2.44f * scale, 0.91f * scale, 2.54f * scale, 0.91f * scale);
 		addEdge(0f * scale, 0.91f * scale, -0.1f * scale, 0.91f * scale);
 		addEdge(2.54f * scale, 0.31f * scale, 2.54f * scale, 0.91f * scale);
 
 		// Create ball
 		resetBallPosition();
 
 		// create robots at either end of pitch
 		resetRobotPositions(true);
 
 		// Send the size of the pitch to the world
 		this.reader.propagatePitchSize();
 
 	}
 
 	@Override
 	public void keyPressed(char c, int keyCode) {
 	}
 
 	@Override
 	public void update() {
 		blue.updateRobot(blueSoft);
 		yellow.updateRobot(yellowSoft);
 		super.update();
 
 		// Update world with new information, throtteling the frame rate
 		if (System.currentTimeMillis() - lastFrameTime > 1000f / Globals.SIMULATED_VISON_FRAMERATE) {
 			lastFrameTime = System.currentTimeMillis();
 			this.reader.update();
 		}
 	}
 
 	/**
 	 * Empty constructor, to make private. For constructor use createSimulator()
 	 */
 	protected Simulator(boolean noisy) { /*
 										 * James: Do not use. Use initTest()
 										 * instead.
 										 */
 		this.noisy = noisy;
 	}
 
 	public static Simulator createSimulator() {
 		return createSimulator(noisy);
 	}
 
 	/**
 	 * Equivalent to a constructor.
 	 * 
 	 * @return A new simulator object.
 	 */
 	public static Simulator createSimulator(boolean noisy) {
 		try {
 			UIManager
 					.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
 		} catch (Exception e) {
 			System.out
 					.println("Could not set the look and feel to nimbus.  "
 							+ "Hopefully you're on a mac so the window isn't ugly as crap.");
 		}
 		TestbedModel model = new TestbedModel();
 		TestbedPanel panel = new TestPanelJ2D(model);
 		model.addCategory("Buggy");
 		Simulator pgui = new Simulator(noisy);
 		model.addTest(pgui);
 		JFrame testbed = new TestbedFrame(model, panel);
 		testbed.setTitle("Simulator");
 		testbed.setVisible(true);
 		testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		return pgui;
 	}
 
 	private static int robotNo = 0;
 
 	protected class Robot {
 
 		private final Body kicker;
 
 		private final Body robot;
 
 		private final float robotWidth = Globals.ROBOT_WIDTH * scale / 2;
 		private final float robotLength = Globals.ROBOT_LENGTH * scale / 2;
 
 		private final float kickerWidth = Globals.ROBOT_WIDTH * scale / 2;
 		private final float kickerLength = Globals.ROBOT_POSSESS_DISTANCE
 				* scale / 2;
 		private Vec2 kickPos = new Vec2((robotLength + kickerLength), 0);
 		private final PolygonShape kickerShape;
 		private static final float kickForce = 20f;
 
 		// wheel power (-1 for lock and 0 for float and (0,100] for power )
 
 		public Robot(Vec2 startingPos, float angle) {
 			World w = getWorld();
 			int robotIndex = robotNo++;
 			BodyDef robotBodyDef = new BodyDef();
 			// BodyDef wheelBodyDef = new BodyDef();
 			BodyDef kickerBodyDef = new BodyDef();
 
 			robotBodyDef.angle = 0.0174532925f * angle;
 			kickerBodyDef.angle = 0.0174532925f * angle;
 			float x = 0.12f * scale;
 			float y = 0f;
 			float newx = x * (float) Math.cos(0.0174532925f * angle) - y
 					* (float) Math.sin(0.0174532925f * angle);
 			float newy = x * (float) Math.sin(0.0174532925f * angle) + y
 					* (float) Math.cos(0.0174532925f * angle);
 
 			kickPos = new Vec2(newx, newy);
 
 			// Create Robot body, set position from Constructor
 			PolygonShape robotShape = new PolygonShape();
 			robotShape.setAsBox(robotLength, robotWidth);
 			// BodyDef robotBodyDef = new BodyDef();
 			robotBodyDef.type = BodyType.DYNAMIC;
 			robotBodyDef.position.set(startingPos);
 			robotBodyDef.angularDamping = 0;
 			robot = w.createBody(robotBodyDef);
 			FixtureDef robotF = new FixtureDef();
 			robotF.shape = robotShape;
 			robotF.density = (1f / 0.36f) / scale;
 			// Stops wheels and body colliding
 			robotF.filter.groupIndex = -robotIndex - 20;
 			robot.createFixture(robotF);
 
 			kickerShape = new PolygonShape();
 			kickerShape.setAsBox(kickerLength, kickerWidth);
 			// BodyDef kickerBodyDef = new BodyDef();
 			kickerBodyDef.type = BodyType.DYNAMIC;
 			FixtureDef kickF = new FixtureDef();
 			kickF.filter.maskBits = 0x0;
 			kickF.filter.groupIndex = -robotIndex - 20;
 			kickF.density = (1f / 0.36f) / scale;
 			kickF.shape = kickerShape;
 
 			kickerBodyDef.position.set(robot.getWorldCenter().clone()
 					.add(kickPos));
 			kicker = w.createBody(kickerBodyDef);
 			kicker.createFixture(kickF);
 
 			WeldJointDef wjd = new WeldJointDef();
 			wjd.initialize(robot, kicker, kicker.getWorldCenter());
 			w.createJoint(wjd);
 		}
 
 		public boolean ballInRange() {
 			Collision c = getWorld().getPool().getCollision();
 			return c.testOverlap(ballShape, kickerShape, ball.getTransform(),
 					kicker.getTransform());
 		}
 
 		public void updateRobot(SoftBot bot) {
 
 			double blueAng = robot.getAngle();
 
 			// if we are turning
 			if (bot.isRotating()) {
 				boolean turningLeft = bot.isTurningLeft();
 				// get current angle - desired angle
 				float dDA = bot.deltaDesiredAngle();
 				if ((!turningLeft && (0 <= dDA && dDA <= (Math.PI / 4))) || // turning
 																			// right
 						(turningLeft && (0 >= dDA && dDA >= -(Math.PI / 4)))) { // turning
 																				// left
 					// stop
 					bot.stop();
 				}
 			}
 
 			float vl = linearVelocity(bot) * scale; // linear velocity
 			float va = angularVelocityRadians(bot); // angular velocity
 			robot.setLinearVelocity(new Vec2((float) (vl * Math.cos(blueAng)),
 					(float) (vl * Math.sin(blueAng))));
 			;
 			robot.setAngularVelocity(va);
 			// System.out.println(va);
 
 			if (bot.isKicking() && ballInRange()) {
                bot.stopKicking();
 				// Kick
 				float xF, yF;
 				xF = (float) (kickForce * Math.cos(blueAng));
 				yF = (float) (kickForce * Math.sin(blueAng));
 				ball.applyForce(new Vec2(xF, yF), ball.getWorldCenter());
 			}
 		}
 
 		public Body getBody() {
 			return robot;
 		}
 
 		private float angularVelocityRadians(SoftBot bot) {
 			float vl = Globals.powerToVelocity(bot.getLeftWheelSpeed());
 			float vr = Globals.powerToVelocity(bot.getRightWheelSpeed());
 			float w = Globals.ROBOT_TRACK_WIDTH;
 			return (vr - vl) / w;
 		}
 
 		private float linearVelocity(SoftBot bot) {
 			float vl = Globals.powerToVelocity(bot.getLeftWheelSpeed());
 			float vr = Globals.powerToVelocity(bot.getRightWheelSpeed());
 			return (vl + vr) / 2f;
 		}
 	}
 
 	SimulatorReader reader = new SimulatorReader();
 
 	/*
 	 * Output to World: All code refering to the output from the simulator goes
 	 * here.
 	 */
 	class SimulatorReader extends Reader {
 
 		private long getTimeStamp() {
 			return System.currentTimeMillis() - startTime;
 		}
 
 		private float convAngle(float a) {
 			return (180f / (float) Math.PI) * a;
 		}
 
 		private Vec2 convPos(Vec2 a) {
 			Vec2 output = new Vec2();
 			output.x = a.x / scale;
 			output.y = a.y / scale;
 			return output;
 		}
 
 		public synchronized String getWorldData() {
 			Vec2 yellowPos, bluePos;
 			yellowPos = convPos(yellow.robot.getPosition());
 			bluePos = convPos(blue.robot.getPosition());
 
 			float yellowAng, blueAng;
 			yellowAng = convAngle(yellow.robot.getAngle());
 			blueAng = convAngle(blue.robot.getAngle());
 			return yellowPos.x + " " + yellowPos.y + " " + yellowAng + " "
 					+ bluePos.x + " " + bluePos.y + " " + blueAng + " "
 					+ getTimeStamp();
 		}
 
 		public void update() {
 			float yPosX, yPosY, yRad, bPosX, bPosY, bRad, ballPosX, ballPosY;
 
 			Vec2 yPos = convPos(yellow.robot.getPosition());
 			yPosX = yPos.x;
 			yPosY = yPos.y;
 			yRad = convAngle(yellow.robot.getAngle());
 
 			Vec2 bPos = convPos(blue.robot.getPosition());
 			bPosX = bPos.x;
 			bPosY = bPos.y;
 			bRad = convAngle(blue.robot.getAngle());
 
 			Vec2 ballPos = convPos(ball.getPosition());
 			ballPosX = ballPos.x;
 			ballPosY = ballPos.y;
 
 			long timestamp = getTimeStamp();
 
 			// if (true) {
 			// // Simulate Height/Observed_Position distortion.
 			// Coord yCoord, bCoord, worldCenter;
 			// Vec2 worldVec = convPos(ground.getPosition());
 			// worldCenter = new Coord(worldVec.x, worldVec.y);
 			// yCoord = new Coord(yPosX, yPosY);
 			// bCoord = new Coord(bPosX, bPosY);
 			//
 			// double distortion = Globals.CAMERA_HEIGHT /
 			// (Globals.CAMERA_HEIGHT - Globals.ROBOT_HEIGHT);
 			// yCoord =
 			// yCoord.sub(worldCenter).mult(distortion).add(worldCenter);
 			// bCoord =
 			// bCoord.sub(worldCenter).mult(distortion).add(worldCenter);
 			//
 			// yPosX = (float) yCoord.getX();
 			// yPosY = (float) yCoord.getY();
 			//
 			// bPosX = (float) bCoord.getX();
 			// bPosY = (float) bCoord.getY();
 			// }
 
 			if (noisy) {
 				// set standard deviations
 				float posSd = Globals.VISION_COORD_NOISE_SD;
 				float angSd = Globals.VISION_ANGLE_NOISE_SD;
 
 				// add noise to positions
 				if (rand.nextDouble() < 0.02) {
 					yPosX = -1;
 					yPosY = -1;
 				} else {
 					yPosX = genRand(posSd, yPosX);
 					yPosY = genRand(posSd, yPosY);
 				}
 
 				if (rand.nextDouble() < 0.02) {
 					bPosX = -1;
 					bPosY = -1;
 				} else {
 					bPosX = genRand(posSd, bPosX);
 					bPosY = genRand(posSd, bPosY);
 				}
 
 				if (rand.nextDouble() < 0.02) {
 					ballPosX = -1;
 					ballPosY = -1;
 				} else {
 					ballPosX = genRand(posSd, ballPosX);
 					ballPosY = genRand(posSd, ballPosY);
 				}
 
 				// add noise to angles
 				yRad = genRandDeg(angSd, yRad);
 				bRad = genRandDeg(angSd, bRad);
 			}
 
 			super.propagate(yPosX, yPosY, yRad, bPosX, bPosY, bRad, ballPosX,
 					ballPosY, timestamp);
 		}
 
 		private float genRand(float sd, float mean) {
 			return ((float) rand.nextGaussian() * sd) + mean;
 		}
 
 		private float genRandDeg(float sd, float mean) {
 			return genRand(sd, mean) % 360;
 		}
 
 		public void propagatePitchSize() {
 			super.propagatePitchSize(Globals.PITCH_WIDTH, Globals.PITCH_HEIGHT);
 		}
 
 		public void propagateGoals() {
 			super.propagateGoals(0, Globals.PITCH_WIDTH, 0, 0);
 		}
 	}
 
 	/**
 	 * Marks a world to be updated of any changes
 	 * 
 	 * @param listener
 	 *            World to be updated.
 	 */
 	@Override
 	public void addListener(Listener listener) {
 		reader.addListener(listener);
 	}
 
 	public void randomiseBallPosition() {
 		World w = getWorld();
 		w.destroyBody(ball);
 
 		ballShape = new CircleShape();
 		ballShape.m_radius = Globals.BALL_RADIUS * scale;
 		FixtureDef f = new FixtureDef();
 		f.shape = ballShape;
 		f.density = (1f / 0.36f) / scale;
 		BodyDef bd = new BodyDef();
 		bd.type = BodyType.DYNAMIC;
 		bd.position.set((float) (Math.random() * Globals.PITCH_WIDTH * scale),
 				(float) (Math.random() * Globals.PITCH_HEIGHT * scale));
 		bd.bullet = true;
 		ball = w.createBody(bd);
 		ball.createFixture(f);
 		ball.setLinearDamping(0);
 	}
 
 	public void randomiseRobotPositions() {
 		World w = getWorld();
 
 		// float random1 = (Globals.ROBOT_LENGTH/2) + (float)Math.random() * (
 		// Globals.PITCH_WIDTH - Globals.ROBOT_LENGTH);
 		// float random2 = (Globals.ROBOT_LENGTH/2) + (float)Math.random() * (
 		// Globals.PITCH_WIDTH - Globals.ROBOT_LENGTH);
 		w.destroyBody(blue.getBody());
 		w.destroyBody(blue.kicker);
 		// blue = new Robot(new Vec2((float) ((random1 * (Globals.PITCH_WIDTH +
 		// Globals.ROBOT_LENGTH)) * scale), (float) ((Math.random() *
 		// (Globals.PITCH_HEIGHT - Globals.ROBOT_LENGTH)) * scale)), 0f);
 		blue = new Robot(new Vec2(
 				(float) ((Math.random() * Globals.PITCH_WIDTH) * scale),
 				(float) ((Math.random() * Globals.PITCH_HEIGHT) * scale)),
 				(float) (Math.random() * 360));
 		blueSoft.setBody(blue.getBody());
 
 		w.destroyBody(yellow.getBody());
 		w.destroyBody(yellow.kicker);
 		// yellow = new Robot(new Vec2((float) ((random2 * (Globals.PITCH_WIDTH
 		// + Globals.ROBOT_LENGTH)) * scale), (float) ((Math.random() *
 		// (Globals.PITCH_HEIGHT - Globals.ROBOT_LENGTH)) * scale)), 0f);
 		yellow = new Robot(new Vec2(
 				(float) ((Math.random() * Globals.PITCH_WIDTH) * scale),
 				(float) ((Math.random() * Globals.PITCH_HEIGHT) * scale)),
 				(float) (Math.random() * 360));
 		yellowSoft.setBody(yellow.getBody());
 	}
 
 	public void resetBallPosition() {
 		World w = getWorld();
 
 		if (ball != null) {
 			w.destroyBody(ball);
 		}
 
 		ballShape = new CircleShape();
 		ballShape.m_radius = Globals.BALL_RADIUS * scale;
 		FixtureDef f = new FixtureDef();
 		f.shape = ballShape;
 		f.density = (1f / 0.36f) / scale;
 		BodyDef bd = new BodyDef();
 		bd.type = BodyType.DYNAMIC;
 		bd.position.set(1.22f * scale, 0.61f * scale);
 		bd.bullet = true;
 		ball = w.createBody(bd);
 		ball.createFixture(f);
 		ball.setLinearDamping(0);
 
 		// TODO correct friction
 		FrictionJointDef ballFriction = new FrictionJointDef();
 		ballFriction.initialize(ball, ground, ball.getWorldCenter());
 		ballFriction.maxForce = 0.1f;
 		ballFriction.maxTorque = 0.001f;
 		w.createJoint(ballFriction);
 	}
 
 	public void resetRobotPositions(boolean robot) {
 		World w = getWorld();
 
 		// If called when simulator is first created, just makes robots
 		// else, destroys old robots and makes new ones
 
 		if (blue != null) {
 			w.destroyBody(blue.getBody());
 			w.destroyBody(blue.kicker);
 		}
 
 		if (yellow != null) {
 			w.destroyBody(yellow.getBody());
 			w.destroyBody(yellow.kicker);
 		}
 
 		if (robot) {
 			blue = new Robot(new Vec2((0.1f * scale), (float) (0.61 * scale)),
 					0f);
 			yellow = new Robot(new Vec2((float) (2.34 * scale),
 					(float) (0.61 * scale)), 180f);
 		} else {
 			blue = new Robot(new Vec2((float) (2.34 * scale),
 					(float) (0.61 * scale)), 180f);
 			yellow = new Robot(
 					new Vec2((0.1f * scale), (float) (0.61 * scale)), 0f);
 		}
 
 		blueSoft.setBody(blue.getBody());
 		yellowSoft.setBody(yellow.getBody());
 	}
 }
