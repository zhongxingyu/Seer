 package editor;
 
 import structures.*;
 import graphics.*;
 import game.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 /**
  * @author Sean Lewis
  */
 class LevelPanel extends JPanel implements ActionListener, MouseListener,
 		MouseMotionListener, ItemListener {
 
 	static final int Ball = 0;
 	static final int Body = 1;
 	static final int Moon = 2;
 	static final int Blockage = 3;
 	static final int Warp = 4;
 	static final int Goal = 5;
 
 	boolean released = false;
 	java.awt.Point p1, p2;
 	int dx = 0;
 	int dy = 0;
 
 	// Level Components
 	Level currentLevel = new Level();
 	Ball ball = new Ball();
 	ArrayList<Body> bodies = new ArrayList<Body>();
 	ArrayList<WarpPoint> warps = new ArrayList<WarpPoint>();
 	ArrayList<Blockage> blockages = new ArrayList<Blockage>();
 	ArrayList<GoalPost> goals = new ArrayList<GoalPost>();
 	double gravityStrength = 1.0;
 	double followFactor = 5.0;
 	double screenXShift, screenYShift;
 
 	// Menu Components
 	JMenuBar menuBar = new JMenuBar();
 	JMenu controlMenu = new JMenu("Control");
 	JRadioButtonMenuItem testItem = new JRadioButtonMenuItem("Test level mode");
 	JRadioButtonMenuItem editItem = new JRadioButtonMenuItem(
 			"Remove objects mode");
 	JMenuItem resetLevelItem = new JMenuItem("Reset level");
 	JMenu levelMenu = new JMenu("Level Constants");
 	JMenuItem gravityItem = new JMenuItem("Edit gravity strength factor");
 	JMenuItem followItem = new JMenuItem("Edit follow factor");
 	JMenu itemMenu = new JMenu("New Object");
 	JRadioButtonMenuItem[] objectItems = {
 			new JRadioButtonMenuItem("New ball", true),
 			new JRadioButtonMenuItem("New planet"),
 			new JRadioButtonMenuItem(
 					"New moon (will be attached to nearest planet)"),
 			new JRadioButtonMenuItem("New blockage"),
 			new JRadioButtonMenuItem("New warp point"),
 			new JRadioButtonMenuItem("New goal") };
 	ButtonGroup objectGroup = new ButtonGroup();
 	JMenu colorMenu = new JMenu("Color");
 	JRadioButtonMenuItem[] colorItems = { new JRadioButtonMenuItem("Blue"),
 			new JRadioButtonMenuItem("Cyan"),
 			new JRadioButtonMenuItem("Green"),
 			new JRadioButtonMenuItem("Magenta"),
 			new JRadioButtonMenuItem("Orange"),
 			new JRadioButtonMenuItem("Pink"),
 			new JRadioButtonMenuItem("Purple"),
 			new JRadioButtonMenuItem("Red", true),
 			new JRadioButtonMenuItem("Violet"),
 			new JRadioButtonMenuItem("Yellow") };
 	ButtonGroup colorGroup = new ButtonGroup();
 	JMenu saveObjectMenu = new JMenu("Save");
 	JMenuItem saveObjectItem = new JMenuItem("Save current object");
 	JMenuItem saveItem = new JMenuItem("Save current level");
 
 	public LevelPanel() {
 		for (int i = 0; i < objectItems.length; i++) {
 			objectGroup.add(objectItems[i]);
 			itemMenu.add(objectItems[i]);
 		}
 		objectGroup.add(testItem);
 		objectGroup.add(editItem);
 		for (int i = 0; i < colorItems.length; i++) {
 			colorGroup.add(colorItems[i]);
 			colorMenu.add(colorItems[i]);
 		}
 		levelMenu.add(gravityItem);
 		gravityItem.addActionListener(this);
 		levelMenu.add(followItem);
 		followItem.addActionListener(this);
 
 		saveObjectMenu.add(saveObjectItem);
 		saveObjectItem.addActionListener(this);
 		saveObjectMenu.add(saveItem);
 		saveItem.addActionListener(this);
 
 		controlMenu.add(testItem);
 		testItem.addActionListener(this);
 		controlMenu.add(editItem);
 		editItem.addActionListener(this);
 		controlMenu.addSeparator();
 		controlMenu.add(resetLevelItem);
 		resetLevelItem.addActionListener(this);
 
 		menuBar.add(levelMenu);
 		menuBar.add(itemMenu);
 		menuBar.add(colorMenu);
 		menuBar.add(saveObjectMenu);
 		saveObjectItem.addActionListener(this);
 		menuBar.add(controlMenu);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 	}
 
 	public Color selectedColor() {
 		for (int i = 0; i < colorItems.length; i++) {
 			if (colorItems[i].isSelected()) {
 				return DataHandler.readColor(colorItems[i].getText()
 						.toLowerCase());
 			}
 		}
 		return null;
 	}
 
 	public void paintComponent(Graphics gr) {
 		Graphics2D g = (Graphics2D) gr;
 		super.paintComponent(g);
 		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		g.setColor(Color.black);
 		g.fillRect(0, 0, 1020, 720);
 
 		double screenXShift = dx;
 		double screenYShift = dy;
 		int xShift = dx;
 		int yShift = dy;
 
 		g.setColor(selectedColor());
 		if (objectItems[Ball].isSelected() && p1 != null) {
 			g.fillOval((int) p1.getX() - 3 + xShift, (int) p1.getY() - 3
 					+ yShift, 6, 6);
 		} else if (objectItems[Body].isSelected() && p2 != null) {
 			int r = (int) p1.distance(p2);
 			g.fillOval((int) p1.getX() - r + xShift, (int) p1.getY() - r
 					+ yShift, r * 2, r * 2);
 		} else if (objectItems[Moon].isSelected() && p2 != null) {
 			int r = (int) p1.distance(p2);
 			g.fillOval((int) p1.getX() - r + xShift, (int) p1.getY() - r
 					+ yShift, r * 2, r * 2);
 
 		} else if (objectItems[Warp].isSelected() && p2 != null) {
 			g.drawImage(WarpPoint.Image, (int) p1.getX() - WarpPoint.RADIUS
 					+ xShift, (int) p1.getY() - WarpPoint.RADIUS + yShift, null);
 
 		} else if (objectItems[Blockage].isSelected() && p2 != null) {
 			int x1 = Math.min(p1.x, p2.x);
 			int x2 = Math.max(p1.x, p2.x);
 			int y1 = Math.min(p1.y, p2.y);
 			int y2 = Math.max(p1.y, p2.y);
 			g.fillRoundRect(x1 + xShift, y1 + yShift, x2 - x1, y2 - y1, 15, 15);
 
 		} else if (objectItems[Goal].isSelected() && p2 != null) {
 			g.setColor(Color.WHITE);
 			int r = (int) p1.distance(p2);
 			g.fillOval((int) p1.getX() - r + xShift, (int) p1.getY() - r
 					+ yShift, r * 2, r * 2);
 		} else if (testItem.isSelected()) { // testLevel mode
 			try {
 				screenXShift = currentLevel.getScreenXShift();
 				screenYShift = currentLevel.getScreenYShift();
 				xShift = (int) Math.round(screenXShift);
 				yShift = (int) Math.round(screenYShift);
 
 				if (p2 != null && ball != null && !ball.isLaunched()) {
 					g.setColor(Color.white);
 					Point2d initialPoint = new Point2d((int) Math.round(ball
 							.getCenter().x + screenXShift),
 							(int) Math.round(ball.getCenter().y + screenYShift));					
 					GraphicEffect.drawArrow(initialPoint, new Point2d(p2), g);
 				}
 
 				if (p2 != null && ball != null && !ball.isLaunched()
 						&& released) {
 					ball.setLaunched(true);
 					double mag = ball.getCenter().getDistance(new Point2d(p2));
 					if (mag > 300)
 						mag = 300;
 					double angle = CalcHelp.getAngle(ball.getCenter(),
 							new Point2d(p2));
 					double xComponent = Math.cos(angle) * mag / 200;
 					double yComponent = -Math.sin(angle) * mag / 200;
 					ball.setVelocity(new Vector2d(xComponent, yComponent));
 					released = false;
 				}
 
 				if (currentLevel.timeToReset() || currentLevel.inGoalPost()) {
 					currentLevel.reset();
 					ball = currentLevel.getBall();
 					p1 = null;
 					p2 = null;
 				}
 
 				currentLevel.updateLevel();
 				xShift = (int) Math.round(screenXShift);
 				yShift = (int) Math.round(screenYShift);
 
 			} catch (Exception e) {
 				System.out.println(e);
 				e.printStackTrace();
 			}
 
 		}
 
 		g.setColor(Color.WHITE);
 		for (GoalPost gp : goals) {
 			gp.draw(xShift, yShift, g);
 		}
 		for (Body b : bodies) {
 			b.draw(xShift, yShift, g);
 			for (Moon m : b.getMoons()) {
 				m.draw(xShift, yShift, g);
 			}
 		}
 		for (WarpPoint w : warps) {
 			g.drawImage(WarpPoint.Image, (int) w.getCenter().x
 					- WarpPoint.RADIUS + xShift, (int) w.getCenter().y
 					- WarpPoint.RADIUS + yShift, this);
 		}
 		for (Blockage b : blockages) {
 			b.draw(xShift, yShift, g);
 		}
 		if (ball != null) {
 			g.setColor(ball.getColor());
 			g.fillOval(
 					(int) Math.round(ball.getCenter().x - ball.getRadius()
 							+ screenXShift),
 					(int) Math.round(ball.getCenter().y - ball.getRadius()
 							+ screenYShift), ball.getDiameter(),
 					ball.getDiameter());
 		}
 
 		sleep(2);
 		repaint();
 	}
 
 	public void updateLevelComponents(Level l) {
 		currentLevel = l;
 		ball = l.getBall();
 		bodies = l.getBodies();
 		warps = l.getWarpPoints();
 		goals = l.getGoalPosts();
 		blockages = l.getBlockages();
 		followFactor = l.getFollowFactor();
 		gravityStrength = l.getGravityStrength();
 
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		if (event.getSource() == saveItem) {
 			JFileChooser chooser = new JFileChooser(
 					System.getProperty("user.dir"));
 			chooser.setApproveButtonText("Save");
 			chooser.setDialogTitle("Save");
 			chooser.setFileFilter(new FileNameExtensionFilter(".txt files",
 					"txt"));
 			int returnVal = chooser.showOpenDialog(this);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				chooser.getSelectedFile().getName();
 			}
 			try {
 				PrintWriter pw = new PrintWriter(new File(chooser
 						.getSelectedFile().getName() + ".txt"));
 				currentLevel = new Level(ball, bodies, warps, goals, blockages,
 						followFactor, gravityStrength);
				pw.println(currentLevel.toString());
 				pw.close();
 				System.out.println(currentLevel.toString());
 			} catch (Exception e) {
 				JOptionPane.showMessageDialog(null, "Error saving file",
 						"Error", JOptionPane.ERROR_MESSAGE);
 				System.out.println(e);
 				e.printStackTrace();
 			}
 		} else if (event.getSource() == gravityItem) {
 
 			String s = (String) JOptionPane.showInputDialog(null,
 					"Intput gravity strength constant:\n",
 					"Edit Gravity Strength", JOptionPane.PLAIN_MESSAGE, null,
 					null, Double.toString(gravityStrength));
 			if ((s != null) && (s.length() > 0)) {
 				try {
 					gravityStrength = Double.parseDouble(s);
 				} catch (Exception e) {
 					gravityStrength = 1.0;
 				}
 			}
 
 		} else if (event.getSource() == followItem) {
 
 			String s = (String) JOptionPane.showInputDialog(null,
 					"Intput follow factor constant:\n", "Edit Follow Factor",
 					JOptionPane.PLAIN_MESSAGE, null, null,
 					Double.toString(followFactor));
 			if ((s != null) && (s.length() > 0)) {
 				try {
 					followFactor = Double.parseDouble(s);
 				} catch (Exception e) {
 					followFactor = 2.0;
 				}
 			}
 
 		} else if (event.getSource() == saveObjectItem) {
 			saveObject();
 		} else if (event.getSource() == testItem) {
 			currentLevel = new Level(ball, bodies, warps, goals, blockages,
 					followFactor, gravityStrength);
 		} else if (event.getSource() == resetLevelItem) {
 			reset();
 		}
 		p1 = null;
 		p2 = null;
 
 	}
 
 	public void reset() {
 		currentLevel.reset();
 		ball = currentLevel.getBall();
 		p1 = null;
 		p2 = null;
 	}
 
 	public void saveObject() {
 		try {
 
 			if (objectItems[Ball].isSelected()) {
 				ball = new Ball((int) p1.getX(), (int) p1.getY(), 3,
 						selectedColor());
 			} else if (objectItems[Body].isSelected()) {
 				bodies.add(new Body((int) p1.getX(), (int) p1.getY(), (int) p1
 						.distance(p2), selectedColor()));
 			} else if (objectItems[Moon].isSelected() && bodies.size() > 0) {
 
 				int r = (int) p1.distance(p2);
 				Point2d center = new Point2d(p1);
 				int index = 0;
 				double smallestDistance = center.getDistance(bodies.get(0)
 						.getCenter()) - bodies.get(0).getRadius();
 				for (int i = 1; i < bodies.size(); i++) {
 					double d = center.getDistance(bodies.get(i).getCenter())
 							- bodies.get(i).getRadius();
 					if (d < smallestDistance) {
 						smallestDistance = d;
 						index = i;
 					}
 				}
 
 				double angle = CalcHelp.getAngle(bodies.get(index).getCenter(),
 						center) * 180.0 / Math.PI;
 				double dist = smallestDistance + bodies.get(index).getRadius();
 				bodies.get(index).addMoon(
 						new Moon((int) Math.round(angle), (int) Math
 								.round(dist), r, selectedColor(), bodies
 								.get(index)));
 
 			} else if (objectItems[Blockage].isSelected()) {
 				blockages.add(new Blockage(p1, p2, selectedColor()));
 			} else if (objectItems[Warp].isSelected()) {
 				warps.add(new WarpPoint((int) p1.getX(), (int) p1.getY()));
 			} else if (objectItems[Goal].isSelected()) {
 				goals.add(new GoalPost((int) p1.getX(), (int) p1.getY(),
 						(int) p1.distance(p2)));
 			}
 
 			currentLevel = new Level(ball, bodies, warps, goals, blockages,
 					followFactor, gravityStrength);
 			p1 = null;
 			p2 = null;
 
 		} catch (Exception e) {
 
 		}
 	}
 
 	public void sleep(int ms) {
 		try {
 			Thread.sleep(ms);
 		} catch (Exception e) {
 		}
 	}
 
 	public void itemStateChanged(ItemEvent event) {
 	}
 
 	public void mousePressed(MouseEvent event) {
 		p1 = event.getPoint();
 		if (!testItem.isSelected())
 			p1.translate(-dx, -dy);
 		p2 = null;
 		if (testItem.isSelected()) {
 			p2 = event.getPoint();
 		}
 	}
 
 	public void mouseReleased(MouseEvent event) {
 		if (testItem.isSelected()) {
 			released = true;
 		}
 		p2 = event.getPoint();
 		Point2d otherPoint = new Point2d(p2);
 		if (!testItem.isSelected())
 			p2.translate(-dx, -dy);
 		if (editItem.isSelected()) {
 			if (ball.getCenter().withinDistance(otherPoint, ball.getRadius())) {
 				ball = null;
 			}
 			for (int i = 0; i < bodies.size(); i++) {
 				if (bodies.get(i).getCenter()
 						.withinDistance(otherPoint, bodies.get(i).getRadius())) {
 					bodies.remove(i);
 					break;
 				}
 			}
 			for (int i = 0; i < warps.size(); i++) {
 				if (warps.get(i).getCenter()
 						.withinDistance(otherPoint, WarpPoint.RADIUS)) {
 					warps.remove(i);
 					break;
 				}
 			}
 			for (int i = 0; i < goals.size(); i++) {
 				if (goals.get(i).getCenter()
 						.withinDistance(otherPoint, goals.get(i).getRadius())) {
 					goals.remove(i);
 					break;
 				}
 			}
 			for (int i = 0; i < blockages.size(); i++) {
 				if (blockages.get(i).intersects(new Point2d(p2))) {
 					blockages.remove(i);
 					break;
 				}
 			}
 		}
 	}
 
 	public void mouseDragged(MouseEvent event) {
 		p2 = event.getPoint();
 		if (!testItem.isSelected())
 			p2.translate(-dx, -dy);
 	}
 
 	public void mouseClicked(MouseEvent event) {
 	}
 
 	public void mouseEntered(MouseEvent event) {
 	}
 
 	public void mouseExited(MouseEvent event) {
 	}
 
 	public void mouseMoved(MouseEvent event) {
 	}
 
 }
