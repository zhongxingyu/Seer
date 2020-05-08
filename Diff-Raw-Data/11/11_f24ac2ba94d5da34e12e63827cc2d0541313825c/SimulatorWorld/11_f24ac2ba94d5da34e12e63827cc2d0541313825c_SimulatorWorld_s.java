 package balle.simulator;
 
 import java.util.LinkedList;
 import java.util.Random;
 
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
 
 import balle.io.reader.Reader;
 import balle.misc.Globals;
 import balle.world.AngularVelocity;
 import balle.world.Coord;
 import balle.world.Orientation;
 import balle.world.Snapshot;
 import balle.world.Velocity;
 import balle.world.objects.Ball;
 import balle.world.objects.Goal;
 import balle.world.objects.Pitch;
 
 public class SimulatorWorld {
 
 	private World world;
 
 	private static int robotNo = 0;
 	private long startTime;
 
 	private Body ground;
 	protected Body ball;
 
 	public Robot blue;
 	protected Robot yellow;
 
 	private final SoftBot blueSoft = new SoftBot();
 	private final SoftBot yellowSoft = new SoftBot();
 
 	private SimulatorReader reader = new SimulatorReader();
 
 	private CircleShape ballShape;
 	private final Random rand = new Random();
 
 	private boolean noisy;
 
 	private long visionDelay = Globals.SIMULATED_VISON_DELAY;
 
 	// Increases sizes, but keeps real-world SCALE; jbox2d acts unrealistically
 	// at a small SCALE
 	protected final static float SCALE = 10;
 
 	public SimulatorWorld(boolean noisy) {
 		this.noisy = noisy;
 	}
 
 	public void setWorld(World w) {
 		world = w;
 	}
 
 	public World getWorld() {
 		return world;
 	}
 
 	public void setStartTime(long t) {
 		startTime = t;
 	}
 
 
 	protected boolean isNoisy() {
 		return noisy;
 	}
 
 	protected void setNoisy(boolean noisy) {
 		this.noisy = noisy;
 	}
 
 	public SoftBot getBlueSoft() {
 		return blueSoft;
 	}
 
 	public SoftBot getYellowSoft() {
 		return yellowSoft;
 	}
 
 	public SimulatorReader getReader() {
 		return reader;
 	}
 
 	public void update(long dt) {
 		blue.updateRobot(blueSoft, dt);
 		yellow.updateRobot(yellowSoft, dt);
 	}
 
 	public void initWorld() {
 		// No Gravity
 		world.setGravity(new Vec2(0, 0));
 
 		// create ground
 		PolygonShape groundShape = new PolygonShape();
 		groundShape.setAsBox(2.64f * SCALE, 1.42f * SCALE);
 		BodyDef groundBodyDef = new BodyDef();
 		groundBodyDef.type = BodyType.STATIC;
 		groundBodyDef.position.set(1.22f * SCALE, 0.61f * SCALE);
 		ground = world.createBody(groundBodyDef);
 		FixtureDef groundF = new FixtureDef();
 		groundF.shape = groundShape;
 		groundF.density = (1f / 0.36f) / SCALE;
 		groundF.filter.maskBits = 0;
 		ground.createFixture(groundF);
 
 		// Create edge of field
 		// Main Pitch
 		addEdge(0f * SCALE, 0f * SCALE, 2.44f * SCALE, 0f * SCALE);
 		addEdge(0f * SCALE, 1.22f * SCALE, 2.44f * SCALE, 1.22f * SCALE);
 		addEdge(0f * SCALE, 0f * SCALE, 0f * SCALE, 0.31f * SCALE);
 		addEdge(2.44f * SCALE, 0f * SCALE, 2.44f * SCALE, 0.31f * SCALE);
 		addEdge(0f * SCALE, 1.22f * SCALE, 0f * SCALE, 0.91f * SCALE);
 		addEdge(2.44f * SCALE, 1.22f * SCALE, 2.44f * SCALE, 0.91f * SCALE);
 		// Left-hand goal area
 		addEdge(0f * SCALE, 0.31f * SCALE, -0.1f * SCALE, 0.31f * SCALE);
 		addEdge(-0.1f * SCALE, 0.31f * SCALE, -0.1f * SCALE, 0.91f * SCALE);
 		addEdge(2.44f * SCALE, 0.31f * SCALE, 2.54f * SCALE, 0.31f * SCALE);
 		// Right-hand goal area
 		addEdge(2.44f * SCALE, 0.91f * SCALE, 2.54f * SCALE, 0.91f * SCALE);
 		addEdge(0f * SCALE, 0.91f * SCALE, -0.1f * SCALE, 0.91f * SCALE);
 		addEdge(2.54f * SCALE, 0.31f * SCALE, 2.54f * SCALE, 0.91f * SCALE);
 
 		// Create ball
 		resetBallPosition();
 
 		// create robots at either end of pitch
 		resetRobotPositions();
 	}
 
 	private void addEdge(float x1, float y1, float x2, float y2) {
 		PolygonShape r = new PolygonShape();
 		r.setAsEdge(new Vec2(x1, y1), new Vec2(x2, y2));
 		FixtureDef f = new FixtureDef();
 		f.shape = r;
 		// TODO find real restitution
 		f.restitution = 0.4f;
 		BodyDef b = new BodyDef();
 		b.type = BodyType.STATIC;
 		// b.position.set(10f,10f);
 		world.createBody(b).createFixture(f);
 
 	}
 
 	protected void setBallPosition(Coord pos) {
 		world.destroyBody(ball);
 
 		ballShape = new CircleShape();
 		ballShape.m_radius = Globals.BALL_RADIUS * SCALE;
 		FixtureDef f = new FixtureDef();
 		f.shape = ballShape;
 		f.density = (1f / 0.36f) / SCALE;
 		BodyDef bd = new BodyDef();
 		bd.type = BodyType.DYNAMIC;
 		bd.position.set((float) pos.getX(), (float) pos.getY());
 		bd.bullet = true;
 		ball = world.createBody(bd);
 		ball.createFixture(f);
 		ball.setLinearDamping(0);
 	}
 
 	public void randomiseBallPosition() {
 		setBallPosition(new Coord(Math.random() * Globals.PITCH_WIDTH * SCALE,
 				Math.random() * Globals.PITCH_HEIGHT * SCALE));
 	}
 
 	public void setBlueRobotPosition(Coord c, Orientation o) {
 		world.destroyBody(blue.getBody());
 		world.destroyBody(blue.kicker);
 		blue = new Robot(new Vec2((float) c.getX(), (float) c.getY()),
 				(float) o.degrees());
 		blueSoft.setBody(blue.getBody());
 	}
 
 	public void setYellowRobotPosition(Coord c, Orientation o) {
 		world.destroyBody(yellow.getBody());
 		world.destroyBody(yellow.kicker);
 		yellow = new Robot(new Vec2((float) c.getX(), (float) c.getY()),
 				(float) o.degrees());
 		yellowSoft.setBody(yellow.getBody());
 	}
 
 	public void randomiseRobotPositions() {
 		setBlueRobotPosition( new Coord( (Math.random() * Globals.PITCH_WIDTH) * SCALE, (Math.random() * Globals.PITCH_HEIGHT) * SCALE), new Orientation(Math.random() * 360, false) );
 		setYellowRobotPosition( new Coord( (Math.random() * Globals.PITCH_WIDTH) * SCALE, (Math.random() * Globals.PITCH_HEIGHT) * SCALE), new Orientation(Math.random() * 360, false) );
 	}
 
 	public void resetBallPosition() {
 
 		if (ball != null) {
 			world.destroyBody(ball);
 		}
 
 		ballShape = new CircleShape();
 		ballShape.m_radius = Globals.BALL_RADIUS * SCALE;
 		FixtureDef f = new FixtureDef();
 		f.shape = ballShape;
 		f.density = (1f / 0.36f) / SCALE;
 		BodyDef bd = new BodyDef();
 		bd.type = BodyType.DYNAMIC;
 		bd.position.set(1.22f * SCALE, 0.61f * SCALE);
 		bd.bullet = true;
 		ball = world.createBody(bd);
 		ball.createFixture(f);
 		ball.setLinearDamping(0);
 
 		// TODO correct friction
 		FrictionJointDef ballFriction = new FrictionJointDef();
 		ballFriction.initialize(ball, ground, ball.getWorldCenter());
 		ballFriction.maxForce = 0.1f;
 		ballFriction.maxTorque = 0.001f;
 		world.createJoint(ballFriction);
 	}
 
 	public void resetRobotPositions() {
 		// If called when simulator is first created, just makes robots
 		// else, destroys old robots and makes new ones
 
 		if (blue != null) {
 			world.destroyBody(blue.getBody());
 			world.destroyBody(blue.kicker);
 		}
 
 		if (yellow != null) {
 			world.destroyBody(yellow.getBody());
 			world.destroyBody(yellow.kicker);
 		}
 
 		blue = new Robot(new Vec2((0.1f * SCALE), (float) (0.61 * SCALE)), 0f);
 		yellow = new Robot(new Vec2((float) (2.34 * SCALE),
 				(float) (0.61 * SCALE)), 180f);
 
 		blueSoft.setBody(blue.getBody());
 		yellowSoft.setBody(yellow.getBody());
 	}
 
 	/*
 	 * Output to World: All code refering to the output from the simulator goes
 	 * here.
 	 */
 	public class SimulatorReader extends Reader {
 		LinkedList<VisionPackage> history = new LinkedList<VisionPackage>();
 
 		private long getTimeStamp() {
 			// Make sure relative time is not needed
 			return System.currentTimeMillis(); // - startTime;
 		}
 
 		private float convAngle(float a) {
 			return (180f / (float) Math.PI) * a;
 		}
 
 		private Vec2 convPos(Vec2 a) {
 			Vec2 output = new Vec2();
 			output.x = a.x / SimulatorWorld.SCALE;
 			output.y = a.y / SimulatorWorld.SCALE;
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
 
 		/**
 		 * propagate using the history and the vision delay
 		 */
 		public void propagate() {
 			long targetTs = getTimeStamp();
 			if (noisy) {
 				targetTs -= visionDelay;
 			}
 			VisionPackage vp, next;
 			do {
 				vp = history.poll();
 				next = history.peek();
 			} while (vp != null && next != null
 					&& next.getTimestamp() < targetTs);
 
 			// default package
 			if (vp == null) {
 				vp = new VisionPackage(-1, -1, -1, -1, -1, -1, -1, -1, targetTs);
 			}
 
 			super.propagate(vp.getYPosX(), vp.getYPosY(), vp.getYRad(),
 					vp.getBPosX(), vp.getBPosY(), vp.getBRad(),
 					vp.getBallPosX(), vp.getBallPosY(), vp.getTimestamp());
 		}
 
 		/**
 		 * update the reader with the latest info
 		 */
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
 
 			VisionPackage vp = new VisionPackage(yPosX, yPosY, yRad, bPosX,
 					bPosY, bRad, ballPosX, ballPosY, timestamp);
 			history.add(vp);
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
 
 	public long getVisionDelay() {
 		return visionDelay;
 	}
 
 	public void setVisionDelay(long visionDelay) {
 		this.visionDelay = visionDelay;
 	}
 
 	public class Robot {
 
 		private final Body kicker;
 
 		private final Body robot;
 		private final float robotWidth = Globals.ROBOT_WIDTH
 				* SimulatorWorld.SCALE / 2;
 		private final float robotLength = Globals.ROBOT_LENGTH
 				* SimulatorWorld.SCALE / 2;
 
 		private final float kickerWidth = Globals.ROBOT_WIDTH
 				* SimulatorWorld.SCALE / 2;
 		private final float kickerLength = Globals.ROBOT_POSSESS_DISTANCE
 				* SimulatorWorld.SCALE / 2;
 		private Vec2 kickPos = new Vec2((robotLength + kickerLength), 0);
 		private final PolygonShape kickerShape;
 		private static final float kickForce = 20f;
 
 		private float lastRPower;
 		private float lastLPower;
 
 		// wheel power (-1 for lock and 0 for float and (0,100] for power )
 
 		public Robot(Vec2 startingPos, float angle) {
 			int robotIndex = robotNo++;
 			BodyDef robotBodyDef = new BodyDef();
 			// BodyDef wheelBodyDef = new BodyDef();
 			BodyDef kickerBodyDef = new BodyDef();
 
 			robotBodyDef.angle = 0.0174532925f * angle;
 			kickerBodyDef.angle = 0.0174532925f * angle;
 			float x = 0.12f * SimulatorWorld.SCALE;
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
 			robot = world.createBody(robotBodyDef);
 			FixtureDef robotF = new FixtureDef();
 			robotF.shape = robotShape;
 			robotF.density = (1f / 0.36f) / SimulatorWorld.SCALE;
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
 			kickF.density = (1f / 0.36f) / SimulatorWorld.SCALE;
 			kickF.shape = kickerShape;
 
 			kickerBodyDef.position.set(robot.getWorldCenter().clone()
 					.add(kickPos));
 			kicker = world.createBody(kickerBodyDef);
 			kicker.createFixture(kickF);
 
 			WeldJointDef wjd = new WeldJointDef();
 			wjd.initialize(robot, kicker, kicker.getWorldCenter());
 			world.createJoint(wjd);
 		}
 
 		public boolean ballInRange() {
 			Collision c = world.getPool().getCollision();
 			return c.testOverlap(ballShape, kickerShape, ball.getTransform(),
 					kicker.getTransform());
 		}
 
 		public void updateRobot(SoftBot bot, long dt) {
 
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
 
 			float vl = linearVelocity(bot, dt) * SimulatorWorld.SCALE; // linear
 																		// velocity
 			float va = angularVelocityRadians(bot, dt); // angular velocity
 			robot.setLinearVelocity(new Vec2((float) (vl * Math.cos(blueAng)),
 					(float) (vl * Math.sin(blueAng))));
 			;
 			robot.setAngularVelocity(va);
 			// System.out.println(va);
 
 			if (bot.isKicking()) {
 				if (ballInRange()) {
 					// Kick
 					float xF, yF;
 					xF = (float) (kickForce * Math.cos(blueAng));
 					yF = (float) (kickForce * Math.sin(blueAng));
 					ball.applyForce(new Vec2(xF, yF), ball.getWorldCenter());
 				}
 
 				bot.stopKicking();
 			}
 		}
 
 		public Body getBody() {
 			return robot;
 		}
 
 		private float getSlipableWheelVelocity(SoftBot bot,
 				float nonSlipVelocity, Vec2 wheelPos, long dt) {
 			// current velocity of wheel
 			Vec2 unitForward = new Vec2((float) Math.cos(bot.getBody()
 					.getAngle()), (float) Math.sin(bot.getBody().getAngle()));
 			float vi = Vec2.dot(
 					bot.getBody().getLinearVelocityFromLocalPoint(wheelPos),
 					unitForward) / SimulatorWorld.SCALE;
 
 			// no-slip velocity
 			float vf = nonSlipVelocity;
 
 			// find acceleration of wheel
 			// NOTE: must convert dt to seconds (td/1000)
 			float dts = dt / 1000f;
 			float a = (vf - vi) / dts;
 			// Account for slip
 			// if above maximum (Globals.MaxWheelAccel)
 			if (Math.abs(a) > Globals.MaxWheelAccel) {
 				// use Globals.WheelSlipAccel to calculate velocities
 				float slipAccel = Globals.SlipWheelAccel;
 				if (a < 0)
 					slipAccel = -slipAccel;
 				vf = vi + (dts * slipAccel);
 			}
 
 			return vf;
 		}
 
 		private float getLeftWheelVelocity(SoftBot bot, long dt) {
 			float p = bot.getLeftWheelSpeed();
 			float a = (p - lastLPower) / dt;
 			float maxA = Globals.MAX_MOTOR_POWER_ACCEL;
 			// System.out.println("lw a:" + a + "lw p: " + p);
 			if (Math.abs(a) > maxA) {
 				if (a > 0) {
 					p = lastLPower + (maxA * dt);
 				} else {
 					p = lastLPower - (maxA * dt);
 				}
 			}
 			return getSlipableWheelVelocity(bot, Globals.powerToVelocity(p),
 					Globals.ROBOT_RIGHT_WHEEL_POS, dt);
 		}
 
 		private float getRightWheelVelocity(SoftBot bot, long dt) {
 			float p = bot.getRightWheelSpeed();
 			float a = (p - lastRPower) / dt;
 			float maxA = Globals.MAX_MOTOR_POWER_ACCEL;
 			if (Math.abs(a) > maxA) {
 				if (a > 0) {
 					p = lastRPower + (maxA * dt);
 				} else {
 					p = lastRPower - (maxA * dt);
 				}
 			}
 			return getSlipableWheelVelocity(bot, Globals.powerToVelocity(p),
 					Globals.ROBOT_RIGHT_WHEEL_POS, dt);
 		}
 
 		private float angularVelocityRadians(SoftBot bot, long dt) {
 			float vl = getLeftWheelVelocity(bot, dt);
 			float vr = getRightWheelVelocity(bot, dt);
 			float w = Globals.ROBOT_TRACK_WIDTH;
 			float avf = (vr - vl) / w; // final (new) angualar velocity
 			// cap accleration
 			float maxA = Globals.MAX_ROBOT_ANG_ACCEL;
 			float avi = bot.getBody().getAngularVelocity(); // current angular
 															// velocity
 			float a = (avf - avi) / dt; // angular acceleration
 			// if (Math.abs(a) > Globals.MAX_ROBOT_ANG_ACCEL
 			// && (Math.abs(avi) > Math.abs(avf))) {
 			// System.out.println("Ang Accel above max. Accel: " + a);
 			// if (a > 0) {
 			// avf = avi + (maxA * dt);
 			// } else {
 			// avf = avi - (maxA * dt);
 			// }
 			// }
 			return avf;
 		}
 
 		private float linearVelocity(SoftBot bot, long dt) {
 			float vl = getLeftWheelVelocity(bot, dt);
 			float vr = getRightWheelVelocity(bot, dt);
 			float vlf = (vl + vr) / 2f;
 			// cap accleration
 			float maxA = Globals.MAX_ROBOT_ANG_ACCEL;
 			float vli = bot.getBody().getAngularVelocity(); // current angular
 															// velocity
 															// float a = (vlf -
 															// vli) / dt; //
 															// angular
 															// acceleration
 			// if (Math.abs(a) > Globals.MAX_ROBOT_LINEAR_ACCEL) {
 			// if (a > 0) {
 			// vlf = vli + (maxA * dt);
 			// } else {
 			// vlf = vli - (maxA * dt);
 			// }
 			// }
 			return vlf;
 		}
 	}
 
 	/**
 	 * sets the robot positions and linear velocities from a given snapshot
 	 * (ball is taken off the field)
 	 */
 	public void setRobotStatesFromSnapshot(Snapshot s, boolean balleIsBlue,
 			float lastLPower, float lastRPower) {
 
 		// TODO set ball position?
 
 		ball.getPosition().set(-100f, -100f);
 		Robot balle = balleIsBlue ? blue : yellow;
 		Robot opponent = balleIsBlue ? yellow : blue;
 		balle.lastLPower = lastLPower;
 		balle.lastLPower = lastRPower;
 
 		balle.world.objects.Robot b = s.getBalle();
 		Coord bp = b.getPosition().mult(SCALE);
 		Coord bv = b.getVelocity().mult(SCALE * 1000);
 		balle.getBody().setLinearVelocity(
 				new Vec2((float) bv.getX(), (float) bv.getY()));
 		balle.getBody().setTransform(
 				new Vec2((float) bp.getX(), (float) bp.getY()),
 				(float) b.getOrientation().radians());
 
 		balle.world.objects.Robot o = s.getOpponent();
 		Coord op = o.getPosition().mult(SCALE);
 		Coord ov = o.getVelocity().mult(SCALE * 1000);
 		opponent.getBody().setLinearVelocity(
 				new Vec2((float) ov.getX(), (float) ov.getY()));
 		opponent.getBody().setTransform(
 				new Vec2((float) op.getX(), (float) op.getY()),
 				(float) o.getOrientation().radians());
 	}
 
 	private Coord v2C(Vec2 v) {
 		return new Coord(v.x, v.y);
 	}
 
 	public Snapshot getSnapshot(long timeStamp, Pitch p, Goal opponentsGoal, Goal ownGoal) {
 		Body o = yellow.getBody();
 		Body me = blue.getBody();
 		Body b = ball;
 		return new Snapshot(
 				new balle.world.objects.Robot(v2C(o.getPosition())
				.mult(1.0 / SCALE), new Velocity(v2C(b.getLinearVelocity())
				.mult(1.0 / (SCALE)), 1000), new AngularVelocity(0,1),  new Orientation(b.getAngle())),
 				
 				new balle.world.objects.Robot(v2C(me.getPosition()).mult(1.0 / SCALE),
 				new Velocity(v2C(me.getLinearVelocity()).mult(1.0 / (SCALE)),
						1000), new AngularVelocity(0, 1), new Orientation(
 						me.getAngle())),
 
 		new Ball(v2C(
 				b.getPosition()).mult(1.0 / SCALE), new Velocity(v2C(
 				b.getLinearVelocity()).mult(1.0 / (SCALE)), 1000)),
 
 		opponentsGoal, ownGoal, p, timeStamp);
 	}
 
 	public void setWithSnapshot(Snapshot snapshot, boolean balleIsBlue) {
 		balle.world.objects.Robot yRobot, bRobot;
 		if (balleIsBlue) {
 			yRobot = snapshot.getOpponent();
 			bRobot = snapshot.getBalle();
 		} else {
 			yRobot = snapshot.getBalle();
 			bRobot = snapshot.getOpponent();
 		}
 
 		setBlueRobotPosition(bRobot.getPosition(), bRobot.getOrientation());
 		setYellowRobotPosition(yRobot.getPosition(), yRobot.getOrientation());
 		setBallPosition(snapshot.getBall().getPosition());
 		
 		blue.getBody().setLinearVelocity(bRobot.getVelocity().vec2());
 		yellow.getBody().setLinearVelocity(yRobot.getVelocity().vec2());
 	}
 
 }
