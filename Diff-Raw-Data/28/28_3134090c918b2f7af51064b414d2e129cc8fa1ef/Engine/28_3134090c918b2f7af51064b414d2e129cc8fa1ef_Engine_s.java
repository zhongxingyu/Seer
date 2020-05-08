 package me.timellis.quack.engine;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Robot;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import me.timellis.quack.network.ClientReceiver;
 import me.timellis.quack.network.ClientSender;
 import me.timellis.quack.network.MultiplayerData;
 import me.timellis.quack.util.EnvironmentGenerator;
 import me.timellis.quack.util.MathsOperations;
 import me.timellis.quack.util.Messages;
 import me.timellis.quack.util.ModelGenerator;
 
 public class Engine {
 	private static final int KEY_TURN_SPEED = 10;
 	Frame frame;
 	ArrayList<Model> models = new ArrayList<Model>();
 	Polygon poly = new Polygon();
 	final int DELAY = 0;
 	final double d = 500;
 	double[][] pov = new double[][] { { 0 }, { 0 }, { 500 } };
 	Camera camera;
 	Robot r;
 	boolean isRecentering = false;
 	boolean wireframe = false;
 	boolean mLook = true;
 	boolean gameRunning = true;
 
 	int offsetX;
 	int offsetY;
 
 	boolean keyForwardPressed = false;
 	boolean keyBackwardPressed = false;
 	boolean keyLeftPressed = false;
 	boolean keyRightPressed = false;
 	boolean keyStrafeLeftPressed = false;
 	boolean keyStrafeRightPressed = false;
 	boolean keyDownPressed = false;
 	boolean keyUpPressed = false;
 	boolean keyYawLeftPressed = false;
 	boolean keyYawRightPressed = false;
 	boolean terrain = false;
 	boolean firing = false;
 	boolean sprinting = false;
 
 	// double[][] lightSource = new double[][] {{2*Math.random()-1},
 	// {2*Math.random()-1}, {2*Math.random()-1},{0}};
 	double[][] lightSource = new double[][] { { 2 * Math.random() - 1 },
 			{ 2 * Math.random() - 1 }, { 2 * Math.random() - 1 }, { 0 } };
 
 	long timeForFPS = System.currentTimeMillis();
 	long frameCount = 0;
 
 	DecimalFormat twoDForm = new DecimalFormat("#.##");
 
 	// clipping frame
 	int xMIN = 0;
 	int xMAX = 600;
 	int yMIN = 0;
 	int yMAX = 600;
 
 	final int LEFT = 1;
 	final int RIGHT = 2;
 	final int TOP = 3;
 	final int BOTTOM = 4;
 
 	// dustfield settings
 	Model dustField = new Model();
 	final int dustNo = 1000;
 
 	// rendering resolution (pixelation)
 	int noOfRenderingLines = 500;
 	boolean renderingWithLines = false;
 
 	final double WALKING_SPEED = .3;
 	final double SPRINTING_SPEED = .3;
 	double[] walkingVector = new double[2];
 	final double MOUSE_SPEED = 1;
 
 	// SpaceClient sc;
 
 	final String SERVER_ADDRESS = "localhost";
 	final int SERVER_PORT = 8189;
 
 	ArrayList<MultiplayerData> otherPlayers = new ArrayList<MultiplayerData>();
 	// ArrayList<Model> otherPlayerModels = new ArrayList();
 
 	GraphicsDevice gd;
 
 	public Engine() {
 
 		lightSource = MathsOperations.normalise(lightSource);
 		// System.out.println(lightSource[0][0] + ", " + lightSource[1][0] +
 		// ", " + lightSource[2][0] );
 
 		for (int i = 0; i < dustNo; i++) {
 			Vertex v = new Vertex(0, 0, 0, i);
 			v.setWorldPosition(new double[] {
 					(int) (Math.random() * 100000 - 50000),
 					(int) (Math.random() * 100000 - 50000),
 					(int) (Math.random() * 100000 - 50000) });
 			dustField.vertices.add(v);
 		}
 
 		try {
 			r = new Robot();
 		} catch (Exception e) {
 			System.out.println("Sorry, couldn't get mouse..");
 			System.exit(0);
 		}
 		camera = new Camera(
 				new double[][] { { 100 }, { 100 }, { 1900 }, { 0 } });
 		camera.resetCameraRoundingErrors();
 
 		EnvironmentGenerator.generateTestRoom(models);
 		// EnvironmentGenerator.generateRenderTestRoom(models);
 
 		gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
 				.getDefaultScreenDevice();
 		frame = new Frame(gd.getDefaultConfiguration());
 
 		frame.setUndecorated(true);
 		// fr.setResizable(false);
 		frame.setIgnoreRepaint(true);
 		// frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
 
 		frame.setVisible(true);
 
 		Messages.addMessage(
 				2,
 				"WSAD to move, arrows to aim, space or right-click to jump, hold shift to sprint, ctrl to shoot");
 		Messages.addMessage(
 				3,
 				"Press (n) to toggle wireframe/node view (useful for debugging), (t) for terrain mode");
 		Messages.addMessage(4, "Press (m) for mouse look, left-click to shoot");
 		Messages.addMessage(12, "Press (r) to switch rendering styles");
 
 		try {
 			System.out.println("Connecting to server...");
 			Socket s = new Socket(SERVER_ADDRESS, SERVER_PORT);
 			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
 			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
 			ClientReceiver scr = new ClientReceiver(ois, otherPlayers);
 			ClientSender scs = new ClientSender(camera, oos);
 			System.out.println("Connected to " + SERVER_ADDRESS + " on port "
 					+ SERVER_PORT);
 			scr.start();
 			scs.start();
 		} catch (Exception e) {
 			System.out.println("Unable to connect to server.");
 		}
 	}
 
 	public void run() {
 		try {
 			gd.setFullScreenWindow(frame);
 
 			offsetX = (int) (frame.getWidth() / 2);
 			offsetY = (int) (frame.getHeight() / 2);
 
 			GameKeys gk = new GameKeys();
 			MouseLook mml = new MouseLook();
 			MouseClicker ml = new MouseClicker();
 
 			frame.addKeyListener(gk);
 			frame.addMouseMotionListener(mml);
 			frame.addMouseListener(ml);
 
 			r.mouseMove(offsetX, offsetY);
 
 			frame.createBufferStrategy(2);
 			BufferStrategy bs = frame.getBufferStrategy();
 			long currentTime = System.currentTimeMillis();
 			while (gameRunning) {
 				Graphics g = bs.getDrawGraphics();
 				long elapsedTime = System.currentTimeMillis() - currentTime;
 				currentTime += elapsedTime;
 				gameLoop(g, elapsedTime);
 				g.dispose();
 				bs.show();
 				try {
 					Thread.sleep(DELAY);
 				} catch (InterruptedException e) {
 				}
 			}
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		} finally {
 			gd.setFullScreenWindow(null);
 			frame.dispose();
 			System.exit(0);
 		}
 
 	}
 
 	private void toggleTerrainMode() {
 		models = new ArrayList<Model>();
 		if (terrain) {
 			EnvironmentGenerator.generateTestRoom(models);
 			terrain = false;
 		} else {
 			EnvironmentGenerator.generateTestTerrain(models);
 			terrain = true;
 		}
 	}
 
 	class MouseClicker extends MouseAdapter {
 		public void mousePressed(MouseEvent e) {
 			if (e.getButton() == MouseEvent.BUTTON1)
 				firing = true;
 			if (e.getButton() == MouseEvent.BUTTON3)
 				camera.jump();
 		}
 
 		public void mouseReleased(MouseEvent e) {
 			if (e.getButton() == MouseEvent.BUTTON1)
 				firing = false;
 		}
 	}
 
 	class MouseLook extends MouseAdapter {
 
 		double prevX = offsetX;
 		double prevY = offsetY;
 
 		public void mouseMoved(MouseEvent e) {
 			doMouseStuff(e);
 		}
 
 		public void mouseDragged(MouseEvent e) {
 			doMouseStuff(e);
 		}
 
 		public void doMouseStuff(MouseEvent e) {
 
 			if (mLook) {
 				if (isRecentering && offsetX == e.getX() && offsetY == e.getY()) {
 					// if(offsetX == e.getX() && offsetY==e.getY())
 					isRecentering = false;
 					// System.out.println(offsetX + "-" + e.getX() +", " +
 					// offsetY + "-" + e.getY());
 
 					// prevX = e.getX();
 					// prevY = e.getY();
 				} else {
 					double xDelta = prevX - e.getX();
 					double yDelta = prevY - e.getY();
 					if (e.getX() != prevX)
 						camera.turnRight(xDelta * MOUSE_SPEED);
 					if (e.getY() != prevY)
 						camera.lookUp(-(yDelta) * MOUSE_SPEED);
 					// if(e.getX()==prevX && e.getY()==prevY) {
 					// prevX = e.getX();
 					// prevY = e.getY();
 					// }
 					// if(xDelta>100 or )
 					isRecentering = true;
 					// r.mouseMove(frame.getX() + offsetX, frame.getY() +
 					// offsetY);
 					r.mouseMove(offsetX, offsetY);
 				}
 				prevX = e.getX();
 				prevY = e.getY();
 			}
 
 		}
 	}
 
 	class GameKeys extends KeyAdapter {
 		private static final int KEY_DOWN = 40;
 		private static final int KEY_UP = 38;
 		private static final int KEY_CTRL = 17;
 		private static final int KEY_LEFT = 39;
 		private static final int KEY_RIGHT = 37;
 		private static final int KEY_ESC = 27;
 		private static final int KEY_SPACE = 32;
 		private static final int KEY_SHIFT = 16;
 		private static final int KEY_M = 77;
 		private static final int KEY_E = 69;
 		private static final int KEY_Q = 81;
 		private static final int KEY_N = 78;
 		private static final int KEY_T = 84;
 		private static final int KEY_D = 68;
 		private static final int KEY_A = 65;
 		private static final int KEY_S = 83;
 		private static final int KEY_W = 87;
 		private static final int KEY_GT = 46;
 		private static final int KEY_LT = 44;
 		private static final int KEY_R = 82;
 
 		public void keyPressed(KeyEvent e) {
 			// System.out.println(e.getKeyCode());
 			if (e.getKeyCode() == KEY_W) {
 				keyForwardPressed = true;
 			}
 			if (e.getKeyCode() == KEY_S) {
 				keyBackwardPressed = true;
 				camera.speedUp = 1;// cancel bunny hop speed boost
 			}
 			if (e.getKeyCode() == KEY_GT)
 				if (noOfRenderingLines < 1600)
 					noOfRenderingLines += 50;
 			if (e.getKeyCode() == KEY_LT)
 				if (noOfRenderingLines > 50)
 					noOfRenderingLines -= 50;
 			if (e.getKeyCode() == KEY_R) {
 				renderingWithLines = !renderingWithLines;
 			}
 			// if(e.getKeyCode()==37) {keyLeftPressed=true;}
 			if (e.getKeyCode() == KEY_RIGHT) {
 				keyLeftPressed = true;
 			}
 			if (e.getKeyCode() == KEY_LEFT) {
 				keyRightPressed = true;
 			}
 
 			if (e.getKeyCode() == KEY_A) {
 				keyStrafeLeftPressed = true;
 			}
 			if (e.getKeyCode() == KEY_D) {
 				keyStrafeRightPressed = true;
 			}
 			if (e.getKeyCode() == KEY_UP) {
 				keyUpPressed = true;
 			}
 			if (e.getKeyCode() == KEY_DOWN) {
 				keyDownPressed = true;
 			}
 			if (e.getKeyCode() == KEY_SPACE) {
 				camera.jump();
 			}
 			if (e.getKeyCode() == KEY_T) {
 				toggleTerrainMode();
 			}
 			if (e.getKeyCode() == KEY_N) {
 				wireframe = !wireframe;
 			}
 			// if(e.getKeyCode()==KEY_Q) {keyYawLeftPressed=true;}
 			// if(e.getKeyCode()==KEY_E) {keyYawRightPressed=true;}
 			if (e.getKeyCode() == KEY_M) {
 				mLook = !mLook;
 			}
 			// if(e.getKeyCode()==KEY_SPACE) {firing=true;}
 			if (e.getKeyCode() == KEY_ESC) {
 				gameRunning = false;
 			}
 			if (e.getKeyCode() == KEY_SHIFT) {
 				sprinting = true;
 			}
 			if (e.getKeyCode() == KEY_CTRL) {
 				firing = true;
 			}
 		}
 
 		public void keyReleased(KeyEvent e) {
 			if (e.getKeyCode() == KEY_W) {
 				keyForwardPressed = false;
 				// Messages.addMessage(15 , "w released");
 				// camera.speedUp = 1;//cancel bunny hop speed boost
 			}
 			if (e.getKeyCode() == KEY_S) {
 				keyBackwardPressed = false;
 			}
 			if (e.getKeyCode() == KEY_RIGHT) {
 				keyLeftPressed = false;
 			}
 			if (e.getKeyCode() == KEY_LEFT) {
 				keyRightPressed = false;
 			}
 			if (e.getKeyCode() == KEY_A) {
 				keyStrafeLeftPressed = false;
 			}
 			if (e.getKeyCode() == KEY_D) {
 				keyStrafeRightPressed = false;
 			}
 			if (e.getKeyCode() == KEY_DOWN) {
 				keyDownPressed = false;
 			}
 			if (e.getKeyCode() == KEY_UP) {
 				keyUpPressed = false;
 			}
 			// if(e.getKeyCode()==KEY_Q) {keyYawLeftPressed=false;}
 			// if(e.getKeyCode()==KEY_E) {keyYawRightPressed=false;}
 			// if(e.getKeyCode()==KEY_SPACE) {firing=false;}
 			if (e.getKeyCode() == KEY_SHIFT) {
 				sprinting = false;
 			}
 			if (e.getKeyCode() == KEY_CTRL) {
 				firing = false;
 			}
 		}
 	}
 
 	public void gameLoop(Graphics g, long elapsedTime) {
 		if (mLook)
 			frame.setCursor(frame.getToolkit().createCustomCursor(
 					new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB),
 					new Point(0, 0), "null"));
 		else
 			frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
 		offsetX = (int) (frame.getWidth() / 2);
 		offsetY = (int) (frame.getHeight() / 2);
 
 		xMIN = (int) (0);
 		xMAX = (int) (frame.getWidth());
 		yMIN = (int) (0);
 		yMAX = (int) (frame.getHeight());
 
 		g.setColor(new Color(0, 0, 0));
 		g.fillRect(0, 0, frame.getSize().width, frame.getSize().height);
 		camera.setCamera();
 
 		Collections.sort(models);
 
 		camera.updateView(dustField);
 
 		ArrayList<Vertex> dustVert = dustField.getVertices();
 
 		// ArrayList<Vect> dustVects = new ArrayList();
 		for (int i = 0; i < dustVert.size(); i++) {
 			if (dustVert.get(i).getCxyz()[2][0] < 0) {
 				double cX = dustVert.get(i).getCxyz()[0][0];
 				double cY = dustVert.get(i).getCxyz()[1][0];
 				double cZ = dustVert.get(i).getCxyz()[2][0];
 
 				double pX = cX * (-500 / cZ);
 				double pY = cY * (-500 / cZ);
 
 				int dustColour = (int) (255 - Math.abs(cZ) / 50000 * 255);
 				if (dustColour > 255)
 					dustColour = 255;
 				if (dustColour < 0)
 					dustColour = 0;
 
 				int dustSize = (int) (dustColour / 255.0 * 5);
 
 				// System.out.println(cZ + ", " + dustColour);
 				g.setColor(new Color(dustColour, dustColour, dustColour));
 				g.fillOval((int) (pX + offsetX),
 						(int) (frame.getHeight() - pY - offsetY), dustSize,
 						dustSize);
 				if (wireframe) {
 					g.setColor(Color.CYAN);
 					g.drawString("" + dustVert.get(i).index,
 							(int) (pX + offsetX) + 3, (int) (frame.getHeight()
 									- pY - offsetY) - 3);
 					// vects.add(new Vect(pX+offsetX, getHeight()-pY-offsetY,
 					// dustVert.get(i).index));
 				}
 			}
 		}
 
 		for (int i = 0; i < models.size(); i++) {
 			Model m = models.get(i);
 			camera.updateView(m);
 			m.sortByDepth();
 			for (int j = 0; j < m.getTriangleCount(); j++) {
 				Triangle t = m.getTriangle(j);
 
 				if (t.onePointVisible() && t.displayFace(pov)) {
 					ArrayList<Vect> vects1 = t.getVects();
 					ArrayList<Vect> vects = new ArrayList<Vect>();
 
 					for (int k = 0; k < vects1.size(); k++) {
 						// calculate the perspective and add the offset
 						double pX = vects1.get(k).x * (-500 / vects1.get(k).z);
 						double pY = vects1.get(k).y * (-500 / vects1.get(k).z);
 						vects.add(new Vect(pX + offsetX, frame.getHeight() - pY
 								- offsetY, vects1.get(k).index));
 					}
 
 					vects = doClipping(vects);
 
 					for (int k = 0; k < vects.size(); k++) {
 						poly.addPoint((int) vects.get(k).x,
 								(int) vects.get(k).y);
 					}
 
 					if (!wireframe) {
 						String mName = m.getModelName();
 						if (mName.equals("starfury")
 								|| mName.equals("roidMine")
 								|| mName.equals("largeRoid")
 								|| mName.equals("roid2")) {
 							int brightness = (int) (t
 									.getBrightness(lightSource) * -127 + 127);
 							// System.out.println(brightness);
 							g.setColor(new Color(brightness, brightness,
 									brightness));
 						} else {
 							g.setColor(t.getColor());
 							if (m.getModelName().equals("bullet"))
 								g.setColor(Color.YELLOW);
 						}
 						if (renderingWithLines) {
 							Messages.addMessage(11,
 									"Rendering style is scanline");
 							if (vects.size() > 0)
 								this.scanLineRender(vects, g);
 						} else {
 							Messages.addMessage(11,
 									"Rendering style is polygon");
 							Messages.addMessage(13, "");
 							Messages.addMessage(14, "");
 							g.fillPolygon(poly);
 						}
 					}
 					if (t.wf || wireframe) {
 						g.setColor(Color.blue);
 						g.drawPolygon(poly);
 					}
 					if (wireframe) {
 						g.setColor(Color.RED);
 						for (int k = 0; k < vects.size(); k++) {
 							g.fillRect((int) vects.get(k).x - 2,
 									(int) vects.get(k).y - 2, 4, 4);
 							// if(vects.get(k).index != 0)
 							// g.drawString(""+vects.get(k).index,
 							// (int)vects.get(k).x+3, (int)vects.get(k).y-3);
 						}
 					}
 					poly.reset();
 				}
 			}
 			if (m.getModelName().equals("roid2")
 					|| m.getModelName().equals("largeRoid"))
 			// if(m.getModelName().equals("cube"))
 			{
 				m.rotateX(0.01);
 				m.rotateY(0.01);
 				m.rotateZ(0.01);
 			}
 			if (m.getModelName().equals("bullet")) {
 				m.lifeSpan--;
 				// if(m.lifeSpan<0 || m.getAverageY()<-50) {
 				if (m.lifeSpan < 0) {
 					models.remove(m);
 					i--;
 				} else {
 					m.moveByTraj();
 				}
 			}
 
 		}
 		if (keyForwardPressed)
 			walkingVector[1] = 1;
 		else if (keyBackwardPressed)
 			walkingVector[1] = -1;
 		else
 			walkingVector[1] = 0;
 		if (keyLeftPressed)
 			camera.turnRight(KEY_TURN_SPEED);
 		if (keyRightPressed)
 			camera.turnRight(-KEY_TURN_SPEED);
 		if (keyStrafeLeftPressed)
 			walkingVector[0] = -1;
 		else if (keyStrafeRightPressed)
 			walkingVector[0] = 1;
 		else
 			walkingVector[0] = 0;
 		if (keyDownPressed)
 			camera.lookUp(KEY_TURN_SPEED);
 		if (keyUpPressed)
 			camera.lookUp(-KEY_TURN_SPEED);
 		// if(keyYawLeftPressed){}
 		// if(keyYawRightPressed){}
 		if (firing) {
 			models.add(ModelGenerator.createBullet(camera.VRPxyz[0][0]
 					+ camera.N[0][0] * 100, camera.VRPxyz[1][0]
 					+ Camera.PLAYER_HEIGHT + camera.N[1][0] * 100,
 					camera.VRPxyz[2][0] + camera.N[2][0] * 100, camera.N));
 		}
 
 		for (int i = 0; i < Messages.max(); i++) {
 			g.setColor(Color.red);
 			g.drawString(Messages.getMessage(i), 10, 10 + i * 20);
 		}
 
 		for (int i = 0; i < otherPlayers.size(); i++) {
 			boolean found = false;
 			int fIndex = 0;
 			MultiplayerData md = otherPlayers.get(i);
 			// System.out.println(md.playerNo);
 			for (int j = 0; j < models.size(); j++) {
 				if (models.get(j).playerNo == md.getPlayerNo()) {
 					found = true;
 					fIndex = j;
 					break;
 				}
 			}
 			if (!found) {
 				// System.out.println("not found");
 				// String filename, double scale, boolean wf, boolean twoSided,
 				// Color c, double[] worldPosition, String modelName)
 				Model newPlayer = ModelGenerator
						.importFromBlender("models/viperElite.obj", 10, false,
 								false, Color.blue, new double[] { md.getxPos(),
 										md.getyPos(), md.getzPos() }, "viper"
 										+ md.getPlayerNo(), true);
 				newPlayer.playerNo = md.getPlayerNo();
 				models.add(newPlayer);
 			} else {
 				models.get(fIndex)
 						.setWorldPosition(
 								new double[] { md.getxPos(), md.getyPos(),
 										md.getzPos() });
 			}
 		}
 
 		// camera.look();
 		// if(!keyUpPressed && !keyDownPressed) camera.slowPitch(mLook);
 		// if(!keyLeftPressed && !keyRightPressed) camera.slowRoll(mLook);
 
 		// normalise walkingvector
 		// System.out.println("before:"+walkingVector[0] + ", " +
 		// walkingVector[1]);
 		double mag = Math.sqrt(Math.pow(walkingVector[0], 2)
 				+ Math.pow(walkingVector[1], 2));
 		if (mag != 0) {
 			walkingVector[0] = (walkingVector[0] / mag)
 					* (sprinting ? SPRINTING_SPEED : WALKING_SPEED)
 					* elapsedTime;
 			walkingVector[1] = (walkingVector[1] / mag)
 					* (sprinting ? SPRINTING_SPEED : WALKING_SPEED)
 					* elapsedTime;
 		}
 		// System.out.println("after:"+walkingVector[0] + ", " +
 		// walkingVector[1]);
 		camera.move(models, walkingVector);
 		camera.updateHeight(models);
 
 		drawCrossHair(g);
 		Messages.addMessage(20,
 				"" + Double.valueOf(twoDForm.format(camera.speedUp)));
 		/*
 		 * long elapsed = System.currentTimeMillis() - camera.lastJumpTime;
 		 * System.out.println(elapsed); if(!camera.jumping && (elapsed >
 		 * camera.BUNNY_HOP_DELAY)) { System.out.println("setting to 1");
 		 * camera.speedUp = 1; }
 		 */
 		// repaint();
 		frameCount++;
 		if (System.currentTimeMillis() - timeForFPS >= 1000) {
 			Messages.addMessage(16, frameCount + " fps");
 			frameCount = 0;
 			timeForFPS = System.currentTimeMillis();
 		}
 		// System.out.println(camera.VRPxyz[1][0]);
 		if (camera.VRPxyz[1][0] < -10000) {
 			camera = new Camera(new double[][] { { 100 }, { 100 }, { 1900 },
 					{ 0 } });
 			camera.resetCameraRoundingErrors();
 		}
 
 	}
 
 	private void drawCrossHair(Graphics g) {
 		int width = frame.getWidth() / 2;
 		int height = frame.getHeight() / 2;
 
 		g.setColor(Color.WHITE);
 		g.drawLine(width - 5, height, width + 5, height);
 		g.drawLine(width, height - 5, width, height + 5);
 	}
 
 	public ArrayList<Vect> doClipping(ArrayList<Vect> vects) {
 		for (int b = 1; b <= 4; b++) {
 			ArrayList<Vect> vectsNew = new ArrayList<Vect>();
 
 			for (int i = 0; i < vects.size(); i++) {
 				Vect v1 = vects.get(i);
 				int i2 = (i == vects.size() - 1 ? 0 : i + 1);
 				Vect v2 = vects.get(i2);
 				boolean v1inside = isInside(v1.x, v1.y, b);
 				boolean v2inside = isInside(v2.x, v2.y, b);
 
 				if (v1inside && v2inside) {
 					vectsNew.add(v1);
 				} else if (v1inside && !v2inside) {
 					vectsNew.add(v1);
 					vectsNew.add(intersection(v1.x, v1.y, v2.x, v2.y, b));
 				} else if (!v1inside && v2inside) {
 					vectsNew.add(intersection(v1.x, v1.y, v2.x, v2.y, b));
 					vectsNew.add(v2);
 				}
 			}
 			vects = new ArrayList<Vect>();
 			for (int i = 0; i < vectsNew.size(); i++) {
 				vects.add(new Vect(vectsNew.get(i).x, vectsNew.get(i).y,
 						vectsNew.get(i).index));
 
 			}
 
 		}
 		return vects;
 	}
 
 	public Vect intersection(double x1, double y1, double x2, double y2,
 			int boundary) {
 		double a1, b1, c1;
 		double a2, b2, c2;
 
 		double x, y;
 
 		switch (boundary) {
 		case 1:
 			a1 = 1;
 			b1 = 0;
 			c1 = xMIN;
 			break;
 		case 2:
 			a1 = 1;
 			b1 = 0;
 			c1 = xMAX;
 			break;
 		case 3:
 			a1 = 0;
 			b1 = 1;
 			c1 = yMAX;
 			break;
 		case 4:
 			a1 = 0;
 			b1 = 1;
 			c1 = yMIN;
 			break;
 		default:
 			a1 = 0;
 			b1 = 0;
 			c1 = 0;
 			System.out.println("oo la la");
 			break;
 		}
 
 		a2 = y2 - y1;
 		b2 = x1 - x2;
 		c2 = a2 * x1 + b2 * y1;
 
 		double det = a1 * b2 - a2 * b1;
 		if (det == 0) {
 			x = 0;
 			y = 0;
 			System.out.println("parallel lines :S");
 		} else {
 			x = (b2 * c1 - b1 * c2) / det;
 			y = (a1 * c2 - a2 * c1) / det;
 		}
 		return new Vect(x, y);
 	}
 
 	public boolean isInside(double x, double y, int boundary) {
 		switch (boundary) {
 		case 1:
 			return x >= xMIN;
 		case 2:
 			return x <= xMAX;
 		case 3:
 			return y <= yMAX;
 		case 4:
 			return y >= yMIN;
 		default:
 			return false;
 		}
 	}
 
 	private void scanLineRender(ArrayList<Vect> vects, Graphics g) {
 		// determine the resolution to render the squares
 		float screenRatio = (float) yMAX / xMAX;
 		int numberOfScreenCols = (int) (noOfRenderingLines * screenRatio);
 		double sizeOfEachLine = yMAX / (double) noOfRenderingLines;
 		double sizeOfEachCol = xMAX / (double) numberOfScreenCols;
 
 		Messages.addMessage(13, "Current rendering resolution: ("
 				+ noOfRenderingLines + ", " + numberOfScreenCols + ")");
 		Messages.addMessage(14, "Press '>' and '<' to increase/decrease");
 
 		// first find the top and bottom for the scan
 		double yMin = vects.get(0).y;
 		double yMax = vects.get(0).y;
 		for (int i = 1; i < vects.size(); i++) {
 			if (vects.get(i).y < yMin)
 				yMin = vects.get(i).y;
 			if (vects.get(i).y > yMax)
 				yMax = vects.get(i).y;
 		}
 
 		// now we know the size how our scan array (i.e how many lines will
 		// there be?)
 		int top = (int) yMin;
 		int bottom = (int) yMax;
 		int numberOfLines = Math
 				.max((int) ((bottom - top) / sizeOfEachLine), 1);
 		// int numberOfLines =(int)((bottom - top)/sizeOfEachLine);
 		Scan scanLines = new Scan(numberOfLines);
 
 		// now go through each line and calculate a pair of x coordinates for
 		// each one
 		for (int i = 0; i < numberOfLines; i++) {
 			int y = (int) (top + i * sizeOfEachLine);
 			for (int j = 0; j < vects.size(); j++) {
 				double y1 = vects.get(j).y;
 				double y2 = vects.get((j + 1) < vects.size() ? j + 1 : 0).y;
 				double x1 = vects.get(j).x;
 				double x2 = vects.get((j + 1) < vects.size() ? j + 1 : 0).x;
 				double gradient = (y2 - y1) / (x2 - x1);
 				if (gradient != 0) {
 					if (y >= (Math.min(y1, y2)) && y <= (Math.max(y1, y2))) {
 						if ((y2 - y1) != 0) {
 							double x = x1 + (y - y1) / gradient;
 							x = (int) (x / sizeOfEachCol) * sizeOfEachCol;
 
 							// double x = (y-y1)*(x2-x1)/(y2-y1)+x1;
 							// System.out.println("vect "+ j +
 							// "("+x1+", "+y1+")-("+x2+", "+y2+")(y = " + y +
 							// ", x = " + x + " gradient = " + gradient);
 							// scanLines.addX(i, Math.max(xMIN, Math.max((int)x,
 							// xMAX)), y);
 							scanLines.addX(i, (int) x, y);
 						}
 					} else {
 						// System.out.println("y=" + y + "(" + y1 + ", " + y2 +
 						// ")");
 					}
 				}
 
 			}
 		}
 
 		// now cycle through the scanLines and draw the horizontal lines to
 		// graphics
 		for (int i = 0; i < scanLines.numberOfLines; i++) {
 			int y = scanLines.xAndY[i][0];
 			int x1 = Math.min(scanLines.xAndY[i][1], scanLines.xAndY[i][2]);
 			int x2 = Math.max(scanLines.xAndY[i][1], scanLines.xAndY[i][2]);
 			// System.out.println(y + ", " + x1 + ", " + x2);
 			// g.drawLine(x1, y, x2, y);
 			// if(x1==x2)x2 = x1+1;
 			// System.out.println(x1 + ", " + x2);
 			g.fillRect(x1, y - 10, x2 - x1, (int) (sizeOfEachLine + 10));
 		}
 	}
 
 	public static void main(String[] args) {
 		System.out.println("TimEngine - tech demo");
 		Engine e = new Engine();
 		e.run();
 	}
 
 }
