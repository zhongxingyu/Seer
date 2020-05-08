 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import javax.swing.JPanel;
 import java.awt.Dimension;
 import java.awt.event.KeyListener;
 import java.awt.event.KeyEvent;
 import javax.imageio.ImageIO;
 import java.awt.Rectangle;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.lang.Thread;
 import java.lang.Runnable;
 import java.lang.Math.*;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.ListIterator;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.Point;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import java.awt.Font;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import javax.swing.BoxLayout;
 import javax.swing.SwingConstants;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 
 /**
  * The clean-up game in the background whilst the popups appear
  * in the foreground
  * @author quincy
  */
 public class BackgroundGame extends JPanel implements KeyListener {
 
 	/**
 	 * The constructor. Loads all of the sprites. Creates the recycle bin
 	 * for the user to play with even before starting the game. Initializes
 	 * the list of GameObjects. 
 	 * 
 	 * Begins a game loop in a separate thread. This loop processes:
 	 * - Running time
 	 * - Removal of GameObjects marked for removal
 	 * - Calling handlers for when an object escapes its set boundaries
 	 * - Handles collisions (in separate threads)
 	 * - Calls the cycle() function of each GameObject
 	 * - Calls { gameCycle}
 	 * 
 	 * The bulk of the loop is in a synchronized block to prevent
 	 * concurrent modification and access of the list of GameObjects. 
 	 * 
 	 * Also begins a paint thread for continuous redrawing.
 	 * @param d The size of the game. 
 	 */
 	public BackgroundGame(Dimension d) {
 		super();
 		setSize(d);
 		setFocusable(true);
 		setDoubleBuffered(true);
 		setLayout(null);
 
 		//setBackground(Color.red);
 
 		GameObject.bgg = this;
 
 		sprites = new HashMap<>();
 		try {
 			loadSprites();
 		} catch (IOException e) {
 			System.err.println(e);
 			System.exit(-1);
 		}
 
 		try {
 			loadQuestions();
 		} catch (FileNotFoundException e) {
 			System.err.println(e);
 			System.err.println("The file is called QuestionBank.txt!");
 			System.exit(-1);
 		} catch (IOException e) {
 			System.err.println(e);
 			System.exit(-1);
 		}
 
 		Rectangle boundsRect = new Rectangle(d);
 
 		objects = new ArrayList<GameObject>();
 		rb = new RecycleBin(boundsRect);
 		rb.setPosition(new Point2D.Double(d.width / 2,
 				d.height - getSprites().get(rb.getSprite()).getHeight()));
 
 		objects.add(rb);
 
 		//Create a background loop
 		(new Thread(new Runnable() {
 
 			public void run() {
 				while (true) {
 					if (isPaused) {
 						/**
 						 * Offsets the time spent whilst pausing
 						 */
 						++timeGameStarted;
 					}
 					if (System.nanoTime() - lastLogicCycleTime > 1000000000 / logicFps) {
 						lastLogicCycleTime = System.nanoTime();
 
 						synchronized (lock) {
 
 							// Use an iterator for the outer loop because of
 							// for its good deletion sematnics
 							for (ListIterator<GameObject> i = objects.listIterator();
 									i.hasNext();) {
 								final GameObject g = i.next();
 
 								//Delete dead objects
 								if (g.isDead) {
 									i.remove();
 									continue;
 								}
 
 								//Execute any actions if the object goes out of bounds
 								if (!g.getBounds().contains(g.getAreaRect())) {
 									g.onOutOfBounds();
 								}
 
 
 								//Store state of kinematics variables
 								g.stashKinematicsVars();
 
 								//Handle collisions
 								Rectangle myCollRect = g.getCollRect();
 								for (ListIterator<GameObject> j = objects.listIterator();
 										j.hasNext();) {
 									final GameObject h = j.next();
 									if (g == h) {
 										continue;
 									}
 
 									if (myCollRect.intersects(h.getCollRect())) {
 										new Thread(new Runnable() {
 
 											public void run() {
 												// Reminder: collideWith calls g's 
 												// collision handlers
 												h.collideWith(g);
 											}
 										}).start();
 									}
 								}
 
 
 								// Execute cycling methdos
 								if (!isPaused && !isOver) {
 									g.cycle();
 								}
 
 							}
 							if (isStarted && !isOver && !isPaused) {
 								gameCycle();
 							}
 						}
 
 						// With actions having concluded,
 						// update the state of the object list
 						//objects.set(objectsTemp);
 					} // FPS condition ends
 				} // While loop ends
 			}
 		})).start();
 
 		// Another loop for painting
 		(new Thread(new Runnable() {
 
 			public void run() {
 				while (true) {
 					repaint();
 				}
 			}
 		})).start();
 	}
 
 	/**
 	 * Accesses the sprites member.
 	 * @return A map of string identifiers to BufferedImages
 	 */
 	public HashMap<String, BufferedImage> getSprites() {
 		return sprites;
 	}
 
 	/**
 	 * Accesses the timeGameStarted member.
 	 * @return A nano-second moment representing when the game started
 	 */
 	public long getTimeGameStarted() {
 		return timeGameStarted;
 	}
 	/**
 	 * The sole RecyclingBin object in the game
 	 */
 	private RecycleBin rb;
 	/**
 	 * A map of string identifiers to BufferedImages
 	 */
 	private HashMap<String, BufferedImage> sprites;
 	/**
 	 * A list of all of the GameObjects. Iterated through in the game loop.
 	 */
 	private ArrayList<GameObject> objects;
 
 	public void keyTyped(KeyEvent e) {
 	}
 
 	/**
 	 * Remove the acceleration from the RecycleBin when the arrow keys are
 	 * released.
 	 * @param e The KeyEvent object
 	 */
 	public void keyReleased(KeyEvent e) {
 		switch (e.getKeyCode()) {
 			case KeyEvent.VK_LEFT:
 				if (!isPaused && !isOver) {
 					rb.setAccel(new Point2D.Double(0, 0));
 				}
 				break;
 			case KeyEvent.VK_RIGHT:
 				if (!isPaused && !isOver) {
 					rb.setAccel(new Point2D.Double(0, 0));
 				}
 				break;
 		}
 	}
 
 	/**
 	 * Gives the recycle bin acceleration on depression of the left or right
 	 * arrow keys. Space pauses, and escape closes. The Windows key will
 	 * start the game too, fitting in with the Windows XP look-and-feel.
 	 * @param e The KeyEvent object
 	 */
 	public void keyPressed(KeyEvent e) {
 		switch (e.getKeyCode()) {
 			case KeyEvent.VK_LEFT:
 				if (!isPaused && !isOver) {
 					rb.setAccel(new Point2D.Double(-3, 0));
 				}
 				break;
 			case KeyEvent.VK_RIGHT:
 				if (!isPaused && !isOver) {
 					rb.setAccel(new Point2D.Double(+3, 0));
 				}
 				break;
 
 			case KeyEvent.VK_ESCAPE:
 				System.exit(0);
 				break;
 			case KeyEvent.VK_SPACE:
				if (isStarted) {
 					togglePaused();
 				}
 				break;
 			case KeyEvent.VK_WINDOWS:
 				if (!isStarted) {
 					startGame();
 				}
 				break;
 		}
 	}
 
 	/**
 	 * Loads all of the requisite images from the working directory, 7 in all.
 	 * @throws IOException 
 	 */
 	private void loadSprites() throws IOException {
 		sprites.put("fullBin", ImageIO.read(new File("user-trash-full64.png")));
 		sprites.put("emptyBin", ImageIO.read(new File("user-trash64.png")));
 		sprites.put("sysfileLarge", ImageIO.read(new File("sysfile1-48.png")));
 		sprites.put("sysfileMedium", ImageIO.read(new File("sysfile2-32.png")));
 		sprites.put("sysfileSmall", ImageIO.read(new File("sysfile3-16.png")));
 		sprites.put("junk", ImageIO.read(new File("junk.png")));
 		sprites.put("grass", ImageIO.read(new File("grass.jpg")));
 	}
 
 	/**
 	 * Draws the sprites of all of the GameObjects
 	 * @param g Graphics context
 	 */
 	public void paintComponent(Graphics g) {
 
 
 		super.paintComponent(g);
 
 
 		// The background.
 		g.drawImage(sprites.get("grass"), 0, 0, getWidth(), getHeight(), null);
 
 		if (isOver) {
 			drawGameOverScreen(g);
 			return;
 		}
 
 		if (!isStarted) {
 			drawTitleScreen(g);
 		}
 
 		synchronized (lock) {
 			for (GameObject h : objects) {
 				Point2D.Double p = h.getPosition();
 				BufferedImage s;
 				if ((s = sprites.get(h.getSprite())) != null) {
 					g.drawImage(sprites.get(h.getSprite()), (int) p.x, (int) p.y, null);
 				}
 			}
 		}
 
 	}
 	/**
 	 * A number which if exceeding 100 will cause the loss of the game
 	 */
 	private double cpuUsage = 0;
 
 	/**
 	 * Accesses the cpuUsage variable. Used to update the metre in the HUD.
 	 * @return The current CPU usage or 100, whichever is least
 	 */
 	public double getCpuUsage() {
 		if (cpuUsage > 100) {
 			return 100;
 		}
 		return cpuUsage;
 	}
 
 	//ArrayList<Question> questions;
 	/**
 	 * Create a pop-up question. Called repeatedly.
 	 */
 	private void makeDialog() {
 		final BackgroundGame thisPanel = this;
 
 		final JInternalFrame dialog = new JInternalFrame("Question!");
 		dialog.addInternalFrameListener(new InternalFrameListener() {
 
 			@Override
 			public void internalFrameOpened(InternalFrameEvent arg0) {
 			}
 
 			@Override
 			public void internalFrameClosing(InternalFrameEvent arg0) {
 			}
 
 			@Override
 			public void internalFrameClosed(InternalFrameEvent arg0) {
 				thisPanel.requestFocusInWindow();
 			}
 
 			@Override
 			public void internalFrameIconified(InternalFrameEvent arg0) {
 			}
 
 			@Override
 			public void internalFrameDeiconified(InternalFrameEvent arg0) {
 			}
 
 			@Override
 			public void internalFrameActivated(InternalFrameEvent arg0) {
 			}
 
 			@Override
 			public void internalFrameDeactivated(InternalFrameEvent arg0) {
 			}
 		});
 
 		Question randQuestion = questions.get(
 				(int) (Math.random() * questions.size()));
 
 		JLabel label = new JLabel(randQuestion.getQuestion());
 		label.setHorizontalAlignment(SwingConstants.LEFT);
 		Font font = label.getFont();
 		label.setFont(label.getFont().deriveFont(font.PLAIN, 14.0f));
 
 		JPanel a = new JPanel();
 		a.setLayout(new BoxLayout(a, BoxLayout.Y_AXIS));
 		a.add(label);
 		
 		JPanel choicesPanel = new JPanel();
 		for (int i = 1; i <= 4; ++i) {
 			String choice = randQuestion.getChoice(i);
 			JButton button = new JButton();
 			if (randQuestion.answerIs(choice)) {
 				//Remove the "--" marker
 				choice = choice.substring(0, choice.indexOf("--"));
 				
 				button.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						dialog.setVisible(false);
 						dialog.dispose();
 					}
 				});
 
 			}
 							
 			button.setText(choice);
 			choicesPanel.add(button);
 		}
 		a.add(choicesPanel);
 
 		dialog.setContentPane(a);
 
 		dialog.pack();
 		dialog.setVisible(true);
 		add(dialog);
 
 		dialog.setLocation(
 				(int) (Math.random() * (getWidth() - dialog.getWidth())),
 				(int) (Math.random() * (getHeight() - dialog.getHeight())));
 		/*
 		
 		for (max = max; max >= 1; max--) {
 		test = choices.get((int) (Math.random() * max) + min);
 		System.out.println(test);
 		System.out.println(choices.indexOf(test));
 		choices.remove(test);
 		System.out.println(max);
 		}*/
 
 		increaseCpuUsage(2);
 	}
 
 	/**
 	 * Draws the instructive title screen.
 	 * @param g The graphics context
 	 */
 	private void drawTitleScreen(Graphics g) {
 		g.setColor(new Color(0, 0x66, 0xcc, 150));
 		g.fillRect(0, 60, getWidth(), getHeight() - 120);
 		g.setColor(Color.WHITE);
 		g.setFont(new Font("Courier New", Font.BOLD, 30));
 		g.drawString("POP UP QUIZ!", 50, 100);
 		g.setFont(new Font("Courier New", Font.BOLD, 18));
 		g.drawString("Where you answer questions whilst cleaning your desktop!",
 				50, 120);
 		g.drawString("Q LAM, V TONG, A VIJAYARAGAVAN", 50, 140);
 
 		g.drawString("Use the arrow keys to move the recycle bin.", 50, 180);
 		g.drawString("<Space> pauses, and <Esc> quits.", 50, 200);
 
 		g.drawImage(sprites.get("junk"), 4, 200, null);
 		g.drawString("Polish your computer by trashing junk falling "
 				+ "from the sky!", 50, 220);
 
 		g.drawImage(sprites.get("sysfileLarge"), 4, 330, null);
 		g.drawImage(sprites.get("sysfileMedium"), 8, 390, null);
 		g.drawImage(sprites.get("sysfileSmall"), 16, 440, null);
 		g.drawString("You'll mess up your computer if you collect system files.",
 				50, 360);
 		g.drawString("Note that they fall from the sky at different speeds.",
 				50, 390);
 
 		g.drawString("The CPU gauge will tell you how well you're doing!",
 				50, 420);
 		g.drawString("If your CPU usage goes over 100%, your computer goes kaput"
 				+ " and you lose! How long can you clean?", 50, 450);
 
 		g.drawString("By the way, you'll have to answer questions from a barrage"
 				+ " of pop-up as you do this.", 50, 500);
 		g.drawString("Serves you right for not being clean!!!", 50, 530);
 
 		g.drawString("PUSH START TO BEGIN_", 50, 600);
 	}
 	/**
 	 * How many cycles of game logic to execute per second
 	 */
 	private final int logicFps = 60;
 	/**
 	 * Some number of nanoseconds representing a moment in the past 
 	 * when the logic loop was run. Used for pacing with  logicFps.
 	 */
 	private long lastLogicCycleTime = 0;
 	/**
 	 * Whether the game has been started. Note that the game goes to the
 	 * bitter end (the user's loss).
 	 */
 	private boolean isStarted = false;
 	/**
 	 * Whether the game is paused.
 	 */
 	private boolean isPaused = false;
 	/**
 	 * Whether the game has been lost;
 	 */
 	private boolean isOver = false;
 	/**
 	 * Some number of nanoseconds representing a moment in the past
 	 * when the game was started. 
 	 */
 	private long timeGameStarted;
 
 	/**
 	 * Increases the CPU usage by some amount.
 	 * @param val The amount by which to increase CPU usage.
 	 */
 	public void increaseCpuUsage(double val) {
 		cpuUsage += val;
 
 	}
 
 	/**
 	 * Routine for ending the game (showing the blue-screen).
 	 */
 	public void endGame() {
 		isOver = true;
 		timeGameEnded = System.nanoTime();
 		removeAll();
 		requestFocusInWindow();
 	}
 
 	/**
 	 * Decreases the CPU usage by some amount.
 	 * @param val The amount by which to increase CPU usage.
 	 */
 	public void decreaseCpuUsage(double val) {
 		cpuUsage -= val;
 		if (cpuUsage < 0) {
 			cpuUsage = 0;
 		}
 	}
 
 	/**
 	 * Toggles the paused state of the game.
 	 */
 	private void togglePaused() {
 		if (!isPaused) {
 			timeFirstPaused = System.nanoTime();
 		} else {
 			// Offset the time spent paused.
 			timeGameStarted += (System.nanoTime() - timeFirstPaused);
 		}
 		isPaused = !isPaused;
 	}
 	/**
 	 * A moment to be used later in offseting time paused from the time
 	 * elapsed in-game.
 	 */
 	private long timeFirstPaused;
 
 	/**
 	 * Begins the game proper!
 	 */
 	public void startGame() {
 		timeGameStarted = System.nanoTime();
 		isStarted = true;
 
 	}
 
 	/**
 	 * What to do whilst the game is running. Called in the background
 	 * loop thread. This method is strictly for things specific to each game.
 	 * e.g. Collision detection which is universal does not go here. Creation
 	 * of the junk items and popups does go here.
 	 * 
 	 * The difficulty increases exponentially as the recycling bin collects
 	 * more objects. Let n be the number of objects collected. Then the 
 	 * chance of a popup being created during a call of gameCycle is
 	 * 
 	 * (1 - 1.1<sup>-0.002n</sup>) in 1.
 	 * 
 	 * That of a large sysfile being created is
 	 * 
 	 * (0.1 + (2)3<sup>-0.2(n+20)</sup>) in 1.
 	 * 
 	 * For a medium sysfile, it's
 	 * 
 	 * (1 - 1.2<sup>-0.002n</sup>) in 1.
 	 * 
 	 * For the smallest one, it's
 	 * 
 	 * (1 - 2<sup>-0.002n</sup>) in 1.
 	 * 
 	 * Basically, smaller items are created more frequently later in the game,
 	 * whilst the large item is created less frequently and eventually vanishes.
 	 * 
 	 * Finally, junk items have a set frequency of 0.005 in 1, or about 1 in
 	 * 200 iterations.
 	 * 
 	 * All of these functions were chosen by experimentation.
 	 */
 	private void gameCycle() {
 		// The first item is the recycle bin
 		RecycleBin rb = (RecycleBin) objects.get(0);
 		double r = Math.random();
 
 		// Exponential difficulty for each item collected
 
 
 		if (Math.pow(1.1, -0.002 * rb.getAmountCollected()) < r) {
 			makeDialog();
 		}
 
 		// Large items should be created less often later
 		if ((-2 * Math.pow(3, 0.2 * -(rb.getAmountCollected() + 20)) + 0.9) < r) {
 
 			Sysfile foo = new Sysfile(getBounds(), Sysfile.Size.L);
 			foo.setPosition(new Point2D.Double(
 					(int) (Math.random() * (getWidth() - foo.getAreaRect().width)),
 					10));
 			synchronized (lock) {
 				objects.add(foo);
 			}
 		}
 
 		// Smaller sysfiles are created more often the more itmes are collected
 		if (Math.pow(1.2, -0.002 * rb.getAmountCollected()) < r) {
 
 			Sysfile foo = new Sysfile(getBounds(), Sysfile.Size.M);
 			foo.setPosition(new Point2D.Double(
 					(int) (Math.random() * (getWidth() - foo.getAreaRect().width)),
 					10));
 			synchronized (lock) {
 				objects.add(foo);
 			}
 		}
 
 		if (Math.pow(2.0, -0.002 * rb.getAmountCollected()) < r) {
 
 			Sysfile foo = new Sysfile(getBounds(), Sysfile.Size.S);
 			foo.setPosition(new Point2D.Double(
 					(int) (Math.random() * (getWidth() - foo.getAreaRect().width)),
 					10));
 			synchronized (lock) {
 				objects.add(foo);
 			}
 		}
 
 
 
 		// Junk items are created regularly
 		if (Math.random() < 0.005) {
 			Junk bar = new Junk(getBounds());
 			bar.setPosition(new Point2D.Double(
 					(int) (Math.random() * (getWidth() - bar.getAreaRect().width)),
 					10));
 			synchronized (lock) {
 				objects.add(bar);
 			}
 		}
 	}
 
 	/**
 	 * Whether the game is paused.
 	 * @return Whether the game is paused.
 	 */
 	public boolean isPaused() {
 		return isPaused;
 	}
 
 	/**
 	 * whether the game has started
 	 * @return Whether the game has started.
 	 */
 	public boolean isStarted() {
 		return isStarted;
 	}
 
 	/**
 	 * Whether the game is over
 	 * @return Whether the game is over
 	 */
 	public boolean isOver() {
 		return isOver;
 	}
 
 	/**
 	 * Draws a BSOD with game information, signifying a game over.
 	 * @param g Grahpics context
 	 */
 	private void drawGameOverScreen(Graphics g) {
 		g.setColor(Color.blue);
 		g.fillRect(0, 0, getWidth(), getHeight());
 		g.setColor(Color.white);
 		g.setFont(new Font("Courier New", Font.PLAIN, 18));
 		g.drawString("A problem has been detected and your computer", 50, 30);
 		g.drawString("has been shut down to prevent damage to your computer.", 50, 60);
 
 		g.drawString("OUT_OF_CPU_ERROR", 50, 120);
 
 		g.drawString("Diagnostics", 50, 240);
 		g.drawString("Items Junked: " + ((RecycleBin) (objects.get(0))).getAmountCollected(), 50, 300);
 		g.drawString("Time Elapsed: " + (timeGameEnded - timeGameStarted) / 1000000000.0 + " s", 50, 360);
 
 		g.drawString("Press <Esc> to power down.", 50, 400);
 	}
 	/**
 	 * A dummy object used for synchronization. Used primarily to isolate
 	 * adding GameObjects to  objects and iterating through 
 	 *  objects.
 	 */
 	private final Object lock = new Object();
 	/**
 	 * A nanosecond moment representing when the game was lost.
 	 */
 	private long timeGameEnded;
 	/**
 	 * List of questions that may appear in the pop-ups.
 	 */
 	private ArrayList<Question> questions;
 
 	/**
 	 * Loads the questions from QuestionBank.txt. The format is
 	 * 
 	 * Question
 	 * Choice
 	 * Choice
 	 * Choice
 	 * Choice
 	 * 
 	 * And the answer will be marked with two hyphens ("--"). Deviation
 	 * will cause an exception to be raised.
 	 * @throws IOException
 	 * @throws FileNotFoundException 
 	 */
 	private void loadQuestions() throws IOException, FileNotFoundException {
 		questions = new ArrayList<>();
 
 		BufferedReader br = new BufferedReader(new FileReader("QuestionBank.txt"));
 
 		String question;
 		String[] choices = new String[4];
 		int correctIndex = 0;
 
 		question = br.readLine();
 		while (question != null) {
 			for (byte i = 0; i < 4; ++i) {
 				choices[i] = br.readLine();
 				// -- marks the right answer
 				if (choices[i].indexOf("--") != -1) {
 					correctIndex = i;
 				}
 			}
 
 			questions.add(new Question(question, choices, correctIndex));
 			question = br.readLine();
 		}
 		br.close();
 	}
 }
