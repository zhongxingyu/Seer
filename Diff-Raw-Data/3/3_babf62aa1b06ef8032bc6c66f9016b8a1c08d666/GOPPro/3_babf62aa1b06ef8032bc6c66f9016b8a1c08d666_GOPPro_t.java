 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 
 import org.rsbot.event.events.MessageEvent;
 import org.rsbot.event.listeners.MessageListener;
 import org.rsbot.event.listeners.PaintListener;
 import org.rsbot.script.Script;
 import org.rsbot.script.ScriptManifest;
 import org.rsbot.script.methods.Game;
 import org.rsbot.script.methods.Skills;
 import org.rsbot.script.util.Filter;
 import org.rsbot.script.wrappers.RSArea;
 import org.rsbot.script.wrappers.RSItem;
 import org.rsbot.script.wrappers.RSNPC;
 import org.rsbot.script.wrappers.RSObject;
 import org.rsbot.script.wrappers.RSTile;
 
 /**
  * @author Ramy
  * @category MiniGame
  * @version Beta 0.92
  */
 
 @ScriptManifest(authors = { "Ramy" }, keywords = { "Miningames",
 		"Grea Orb Project", "Ramy" }, name = "GOPPro", version = 0.92, description = "Playing the Great Orb Project game and craft runes!")
 public class GOPPro extends Script implements PaintListener, MessageListener,
 		MouseListener {
 
 	Team team = Team.Green;
 	JoinTeam Jointeam;
 	Style style;
 
 	public enum Team {
 		Yellow(0, 8021, 38377, 13648, 13644, 13646), Green(1, 8025, 38378,
 				13647, 13643, 13645);
 
 		int ID = 0;
 
 		int OrbID = 0;
 		int BarriedID = 0;
 		int BarriesID = 0;
 		int RepellerID = 0;
 		int AttrackerID = 0;
 
 		Team(int ID, int OrbID, int BarriesID, int BarriedID, int RepellerID,
 				int AttrackerID) {
 			this.RepellerID = RepellerID;
 			this.AttrackerID = AttrackerID;
 			this.BarriesID = BarriesID;
 			this.BarriedID = BarriedID;
 			this.OrbID = OrbID;
 		}
 
 		public int getRepellerID() {
 			return BarriesID;
 		}
 
 		public int getAttrackerID() {
 			return BarriesID;
 		}
 
 		public int getBarriesID() {
 			return BarriesID;
 		}
 
 		public int getBarriedID() {
 			return BarriedID;
 		}
 
 		public int getOrbID() {
 			return OrbID;
 		}
 
 		public int getID() {
 			return ID;
 		}
 	}
 
 	public enum State {
 		join, offend, defend, craftRunes, exit, Walk2Alter, hold, Destroy
 	};
 
 	public enum Style {
 		Repel, Attrack, Hold
 	}
 
 	public enum JoinTeam {
 		Green(8031), Yellow(8030), Randomly(8038, 8039, 8033, 8040);
 
 		int[] WizardID;
 
 		JoinTeam(int... WizardID) {
 			this.WizardID = WizardID;
 		}
 
 		public int[] getWizardID() {
 			return WizardID;
 		}
 	}
 
 	RSNPC Orb = null;
 
 	protected GOPGui GOPGUI;
 
 	public long LastKicked = 0;
 	public int Min2Wait = 0;
 
 	public String Status = "";
 
 	public long startTime = 0;
 	public long millis = 0;
 	public long hours = 0;
 	public long minutes = 0;
 	public long seconds = 0;
 
 	public long last = 0;
 	public boolean START = false;
 
 	public boolean GUILoaded = false;
 	public boolean hide = false;
 
 	public int START_TOKENS = 0;
 	public int START_XP = 0;
 	public int START_LEVEL = 0;
 	public int CURRENT_TOKENS = 0;
 	public int CURRENT_LEVEL = 0;
 	public int GAINED_TOKENS = 0;
 	public int GAINED_XP = 0;
 	public int GAINED_LEVEL = 0;
 
 	public final int WIZARD_ID[] = { 8038, 8039, 8033, 8040 };
 	public final int GUILD_PORTAL_ID = 8019;
 	public final int ALTER_PORTAL_ID = 8020;
 
 	public final int ANIMATION = 10132;
 	public final int[] ESS = { 1436, 7936 };
 	public final int MIND_ALTER_ID = 2479;
 	public final int TOKENS = 13650;
 
 	public final int[] ALTERS_ID = { 2478, 2479, 2480, 2481, 2482, 2483, 2487,
 			2486 };
 
 	public final int[] GUILD_AREA = { 1688, 5460, 1704, 5476 };
 
 	public final static RSArea[] MIND_UNREACH_ABLE_ORBS = {
 			new RSArea(new RSTile(9191, 5682, 0), new RSTile(9190, 5684, 0)),
 			new RSArea(new RSTile(9197, 5678, 0), new RSTile(9195, 5680, 0)),
 			new RSArea(new RSTile(9198, 5674, 0), new RSTile(9199, 5675, 0)),
 			new RSArea(new RSTile(9200, 5671, 0), new RSTile(9201, 5670, 0)) };
 
 	public int loop() {
 		try {
 			switch (getState()) {
 			case join:
 				Join();
 				break;
 			case Destroy:
 				DestroyBarrier();
 				break;
 			case offend:
 				Offend();
 				break;
 			case hold:
 				Hold();
 				break;
 			case defend:
 				Defend();
 				break;
 			case craftRunes:
 				CraftRunes();
 				break;
 			case exit:
 				Exit();
 				break;
 			case Walk2Alter:
 				Walk2Alter();
 				break;
 			}
 		} catch (Exception e) {
 		}
 		return 80;
 	}
 
 	public void Walk2Alter() {
 		RSObject alter = objects.getNearest(ALTERS_ID);
 		if (alter != null && walking.walkTileMM(alter.getLocation())) {
 			Status = "Walking to " + alter.getName() + "...";
 			sleep(400, 500);
 		}
 	}
 
 	public Filter<RSNPC> Filter = new Filter<RSNPC>() {
 		public boolean accept(RSNPC n) {
 			if (style != Style.Hold) {
 				return n.getID() == team.getOrbID() && Check(n)
 						&& (calc.distanceTo(n.getLocation()) > 2);
 			}
 			return n.getID() == team.getOrbID() && Check(n);
 		}
 	};
 
 	public boolean Check(final RSNPC o) {
 		if (objects.getNearest(MIND_ALTER_ID) == null) {
 			return true;
 		}
 		for (int i = 0; i < MIND_UNREACH_ABLE_ORBS.length; i++) {
 			if (MIND_UNREACH_ABLE_ORBS[i].contains(o.getLocation())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void Exit() {
 		Status = "Entering Portal...";
 		RSNPC exitPortal = npcs.getNearest(8020);
 		if (inventory.getCount(ESS) < 5) {
 			if (exitPortal != null) {
 				if (exitPortal.isOnScreen() && exitPortal.interact("Enter")) {
 					sleep(1500, 2000);
 				} else {
 					if (walking.walkTileMM(exitPortal.getLocation())) {
 						sleep(600, 700);
 					}
 				}
 
 			}
 		}
 	}
 
 	public void Hold() {
 		if (getMyPlayer().getAnimation() != ANIMATION) {
 			RSNPC g = npcs.getNearest(Filter);
 
 			if (g != null) {
 				if (g.isOnScreen()) {
 					g.interact("attrack");
 					sleep(500);
 				} else {
 					if (walking.walkTileMM(g.getLocation())) {
 						sleep(200, 500);
 					}
 				}
 			}
 		} else {
 			moveCameraRandomly();
 		}
 	}
 
 	public void Join() {
 		if (interfaces.getComponent(243, 4).getText().toLowerCase()
 				.contains("i never should have put my hopes in")) {
 			interfaces.getComponent(243, 7).doClick();
 			sleep(400, 600);
 		} else if (interfaces.getComponent(242, 4).getText().toLowerCase()
 				.contains("you left the last game")) {
 			int a = Integer.parseInt(interfaces.getComponent(242, 5).getText()
 					.toLowerCase().replaceAll(" ", "")
 					.replaceAll("newgamefor", "").replaceAll("minutes.", ""));
 			Min2Wait = a / (1000 * 60);
 			Status = "Waiting " + Min2Wait + " minutes..";
 			LastKicked = System.currentTimeMillis();
 			AntiBan();
 			return;
 		} else if (System.currentTimeMillis() + Min2Wait < LastKicked) {
 			AntiBan();
 			return;
 		} else if (interfaces.getComponent(242, 4).getText().toLowerCase()
 				.contains("you failed")) {
 			interfaces.getComponent(242, 6).doClick();
 			sleep(400, 600);
 		} else if (interfaces.getComponent(210, 6).isValid()) {
 			interfaces.getComponent(210, 2).doClick();
 			sleep(400, 600);
 		}
 
 		RSNPC wizard = npcs.getNearest(Jointeam.getWizardID());
 
 		if (inventory.getCount(new int[] { Team.Green.getBarriedID(),
 				Team.Yellow.getBarriedID() }) < 1) {
 			if (interfaces.getComponent(228, 2).isValid()) {
 				interfaces.getComponent(228, 2).doClick(true);
 				sleep(743, 812);
 			} else {
 				if (wizard != null) {
 					if (wizard.isOnScreen()) {
 						Status = "Joining Game...";
 						if (wizard.interact("Join")) {
 							sleep(1000, 1500);
 							moveCameraRandomly();
 						}
 					} else {
 						if (walking.walkTileMM(wizard.getLocation())) {
 							sleep(200, 500);
 						}
 
 					}
 				}
 			}
 		} else {
 			if (inventory.getCount(Team.Yellow.getBarriedID()) > 0) {
 				team = Team.Yellow;
 			}
 			if (inventory.getCount(Team.Green.getBarriedID()) > 0) {
 				team = Team.Green;
 			}
 			RSItem r = inventory.getItem(team.getRepellerID());
 			if (r != null && style == Style.Repel && r.interact("Wield")) {
 				sleep(2000);
 			}
 			CURRENT_TOKENS = inventory.getCount(true, TOKENS);
 			RSNPC port = npcs.getNearest(GUILD_PORTAL_ID);
 			if (port != null) {
 				if (port.isOnScreen()) {
 					port.interact("Enter");
 					sleep(500, 1000);
 				} else {
 					walking.walkTileMM(port.getLocation());
 				}
 			} else {
 				AntiBan();
 			}
 		}
 
 	}
 
 	public void CraftRunes() {
 		RSObject alter = objects.getNearest(ALTERS_ID);
 		if (inventory.getCount(ESS) >= 5) {
 			if (alter != null) {
 				if (alter.isOnScreen()) {
 					if (interfaces.getComponent(228, 2).isValid()) {
 						interfaces.getComponent(228, 2).doClick();
 						sleep(500, 1000);
 					} else {
 						Status = "Crafting Runes...";
 						if (TrunCameraTo(alter) && alter.interact("Craft-rune")) {
 							sleep(850, 1250);
 						}
 					}
 				} else {
 					Walk2Alter();
 				}
 			}
 		}
 	}
 
 	public void Defend() {
 		Status = "Repelling orb...";
 		RSNPC orb = npcs.getNearest(Filter);
 		if (style == Style.Repel) {
 			if (orb != null) {
 				RSItem r = inventory.getItem(team.getRepellerID());
 				if (r != null && r.interact("Wield")) {
 					sleep(500, 800);
 				}
 				if (TrunCameraTo(orb) && orb.interact("Repel")) {
 					sleep(1000, 1500);
 				}
 			} else {
 				moveCameraRandomly();
 			}
 		}
 	}
 
 	public void Offend() {
 		//Aza ana az3'ar ohwe az3'ar
 		RSObject alter = objects.getNearest(ALTERS_ID);
 		RSNPC orb = npcs.getNearest(Filter);
 		if (orb != null) {
 			if (getMyPlayer().getAnimation() == ANIMATION && alter.isOnScreen()) {
 				moveMouseOffScreen(random(200, 1200));
 			} else if (calc.distanceTo(alter.getLocation()) > 3) {
 				Walk2Alter();
 			} else {
 					RSItem a = inventory.getItem(team.getAttrackerID());
 					if (a != null && a.interact("Wield")) {
 						sleep(200, 500);
 					} else {
 						Status = "Attracking orb...";
 						if(TrunCameraTo(orb) && orb.interact("attract")){
 						Orb = orb;
 							sleep(400, 800);
 						}
 					}
 			}
 		} else {
 			if(Orb != null){
 			}
 			moveCameraRandomly();
 		}
 	}
 
 	boolean a(RSNPC Orb, RSObject alter) {
 		return Orb.getLocation().getX() > getMyPlayer().getLocation().getX();
 	}
 
 	public void DestroyBarrier() {
 		Status = "Destroying Barriers..";
 		RSObject BARRIER = objects.getNearest(team.getBarriesID());
 		if (BARRIER != null && TrunCameraTo(BARRIER)
 				&& BARRIER.interact("Destroy")) {
 			sleep(1000, 1720);
 		}
 	}
 
 	/**
 	 * @param orb
 	 *            the npc to turn to
 	 */
 	public boolean TrunCameraTo(RSNPC orb) {
 		return TrunCameraTo(orb.getLocation());
 	}
 
 	/**
 	 * @param orb
 	 *            the object to turn to
 	 */
 	public boolean TrunCameraTo(RSObject orb) {
 		return TrunCameraTo(orb.getLocation());
 	}
 
 	/**
 	 * @param orb
 	 *            the tile to turn to
 	 */
 	public boolean TrunCameraTo(RSTile t) {
 		if (calc.distanceTo(t) > 5) {
 			camera.setPitch(random(20, 40));
 		} else if (calc.distanceTo(t) > 10) {
 			camera.setPitch(random(15, 35));
 		} else if (calc.distanceTo(t) > 15) {
 			camera.setPitch(random(10, 25));
 		} else if (calc.distanceTo(t) > 20) {
 			camera.setPitch(random(5, 15));
 		} else if (calc.distanceTo(t) > 25) {
 			camera.setPitch(random(0, 5));
 		}
 		camera.turnTo(t);
 		sleep(200, 500);
 		return calc.tileOnScreen(t);
 	}
 
 	Rectangle r = new Rectangle(522, 468, 29, 33);
 
 	public void mouseClicked(MouseEvent mouse) {
 		Point p = mouse.getPoint();
 		if (r.contains(p)) {
 			hide = !hide;
 		}
 	}
 
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	public void mouseExited(MouseEvent e) {
 	}
 
 	public void mousePressed(MouseEvent e) {
 	}
 
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	public void messageReceived(final MessageEvent evt) {
 		final String serverString = evt.getMessage();
 		if (evt.getID() == MessageEvent.MESSAGE_SERVER
 				&& serverString.contains("is getting low")) {
 			if (inventory.getCount(team.getBarriedID()) > 0) {
 				CreatBarrier(inventory.getItem(team.getBarriedID()),
 						random(3, 6));
 			}
 		}
 	}
 
 	/**
 	 * @param i
 	 *            barrier maker item
 	 * @param t
 	 *            times to make barries.
 	 */
 	public void CreatBarrier(RSItem i, int t) {
 		RSObject a = objects.getNearest(ALTERS_ID);
 		while (calc.distanceTo(a) < 15) {
 			walking.walkTileMM(new RSTile(a.getLocation().getX()
 					+ random(15, 20), a.getLocation().getY()));
 			sleep(200, 500);
 			while (getMyPlayer().isMoving()) {
 				sleep(200, 500);
 			}
 		}
 		for (int k = 0; k < t; k++) {
 			if (i.interact("Make-barrier")) {
 				sleep(1000, 1500);
 				DestroyBarrier();
 			}
 		}
 	}
 
 	/**
 	 * @param url
 	 *            the link to image
 	 * @return Image
 	 */
 	private Image getImage(String url) {
 		try {
 			return ImageIO.read(new URL(url));
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 	private final Color color1 = new Color(255, 0, 0);
 	private final Color color2 = new Color(0, 153, 0);
 	private final Color color3 = new Color(255, 255, 0);
 
 	private final Font font1 = new Font("Arial", 1, 23);
 	private final Font font2 = new Font("Arial", 1, 21);
 
 	private final Image img1 = getImage("http://img197.imageshack.us/img197/6111/greeatorbpaint2.png");
 
 	public void onRepaint(Graphics g1) {
 		Graphics2D g = (Graphics2D) g1;
 		if (!hide) {
 			g.setColor(new Color(255, 0, 51, 187));
 			g.fillRect(522, 468, 29, 33);
 
 			GAINED_TOKENS = CURRENT_TOKENS - START_TOKENS;
 			GAINED_XP = skills.getCurrentExp(Skills.RUNECRAFTING) - START_XP;
 			GAINED_LEVEL = skills.getCurrentLevel(Skills.RUNECRAFTING)
 					- START_LEVEL;
 			CURRENT_LEVEL = skills.getCurrentLevel(Skills.RUNECRAFTING);
 
 			millis = System.currentTimeMillis() - startTime;
 			hours = millis / (1000 * 60 * 60);
 			millis -= hours * (1000 * 60 * 60);
 			minutes = millis / (1000 * 60);
 			millis -= minutes * (1000 * 60);
 			seconds = millis / 1000;
 
 			g.drawImage(img1, -29, 206, null);
 			g.setFont(font1);
 			g.setColor(color1);
 			g.drawString("Tokens: " + CURRENT_TOKENS + "(" + GAINED_TOKENS
 					+ ")", 16, 385);
 			g.drawString("XP: " + GAINED_XP, 14, 407);
 			g.setFont(font2);
 			g.drawString("Level: " + CURRENT_LEVEL + "(" + GAINED_LEVEL + ")",
 					14, 430);
 			g.setColor(color2);
 			g.drawString("Runtime: " + hours + ":" + minutes + ":" + seconds,
 					9, 35);
 			g.setColor(color1);
 			g.drawString("Status: " + Status, 17, 458);
 			g.setColor(color3);
 			g.drawString("GOP", 159, 337);
 			g.setColor(color2);
 			g.drawString("Pro", 205, 338);
 		} else {
 			g.setColor(new Color(102, 255, 0, 187));
 			g.fillRect(522, 468, 29, 33);
 		}
 	}
 
 	public void onFinish() {
 		log(Color.blue, "Thank you for using GOPPro!");
 	}
 
 	public boolean onStart() {
 		log(Color.ORANGE, "Welcome!");
 		startTime = System.currentTimeMillis();
 		START_XP = skills.getCurrentExp(Skills.RUNECRAFTING);
 		START_TOKENS = inventory.getCount(true, TOKENS);
 		START_LEVEL = skills.getCurrentLevel(Skills.RUNECRAFTING);
 		GOPGUI = new GOPGui();
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				new GOPGui().setVisible(true);
 			}
 		});
 		while (!GUILoaded) {
 			sleep(random(200, 300));
 		}
 		return true;
 	}
 
 	/**
 	 * @return the status.
 	 */
 	public State getState() {
 		RSNPC exitPortal = npcs.getNearest(8020);
 		RSObject alter = objects.getNearest(ALTERS_ID);
 		RSObject BARRIER = objects.getNearest(team.getBarriesID());
 
 		if (playerInArea(GUILD_AREA)) {
 			return State.join;
 		} else if (alter != null) {
 			if (alter.isOnScreen()) {
 				if (exitPortal != null) {
 					if (inventory.getCount(ESS) >= 5) {
 						return State.craftRunes;
 					}
 					return State.exit;
 
 				} else {
 					if (BARRIER != null && calc.distanceTo(BARRIER) <= 10) {
 						return State.Destroy;
 					} else {
 						if (style == Style.Attrack) {
 							return State.offend;
 						} else {
 							return State.defend;
 						}
 					}
 				}
 			} else {
 				if (calc.distanceTo(alter.getLocation()) > 50) {
 					return State.Walk2Alter;
 				} else {
 					TrunCameraTo(alter);
 				}
 			}
 		}
 		return null;
 	}
 
 	/** -----------------------GUI----------------------- */
 	@SuppressWarnings("serial")
 	public class GOPGui extends javax.swing.JFrame {
 
 		/**
 		 * @author Ramy
 		 */
 		public GOPGui() {
 			initComponents();
 		}
 
 		private void initComponents() {
 
 			jLabel1 = new javax.swing.JLabel();
 			green = new javax.swing.JRadioButton();
 			yellow = new javax.swing.JRadioButton();
 			jLabel2 = new javax.swing.JLabel();
 			jLabel3 = new javax.swing.JLabel();
 			jLabel4 = new javax.swing.JLabel();
 			jLabel5 = new javax.swing.JLabel();
 			jLabel6 = new javax.swing.JLabel();
 			jLabel7 = new javax.swing.JLabel();
 			defend = new javax.swing.JRadioButton();
 			offend = new javax.swing.JRadioButton();
 			jLabel8 = new javax.swing.JLabel();
 			jLabel9 = new javax.swing.JLabel();
 			jButton1 = new javax.swing.JButton();
 
 			setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
 			jLabel1.setIcon(new javax.swing.JLabel() {
 				public javax.swing.Icon getIcon() {
 					try {
 						return new javax.swing.ImageIcon(new java.net.URL(
 								"http://i51.tinypic.com/2cqll6o_th.jpg"));
 					} catch (java.net.MalformedURLException e) {
 					}
 					return null;
 				}
 			}.getIcon());
 
 			green.setSelected(true);
 			green.setText("Join Green.");
 
 			yellow.setSelected(true);
 			yellow.setText("Join Yellow.");
 
 			jLabel2.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
 			jLabel2.setText("Teams");
 
 			jLabel3.setIcon(new javax.swing.JLabel() {
 				public javax.swing.Icon getIcon() {
 					try {
 						return new javax.swing.ImageIcon(
 								new java.net.URL(
 										"http://png.findicons.com/files/icons/1226/agua_extras_vol_1/16/blank_badge_green.png"));
 					} catch (java.net.MalformedURLException e) {
 					}
 					return null;
 				}
 			}.getIcon());
 
 			jLabel4.setIcon(new javax.swing.JLabel() {
 				public javax.swing.Icon getIcon() {
 					try {
 						return new javax.swing.ImageIcon(
 								new java.net.URL(
 										"http://png.findicons.com/files/icons/811/developer_kit/16/ball_yellow.png"));
 					} catch (java.net.MalformedURLException e) {
 					}
 					return null;
 				}
 			}.getIcon());
 
 			jLabel5.setIcon(new javax.swing.JLabel() {
 				public javax.swing.Icon getIcon() {
 					try {
 						return new javax.swing.ImageIcon(
 								new java.net.URL(
 										"http://img36.imageshack.us/img36/7241/kaida23sigrct.png"));
 					} catch (java.net.MalformedURLException e) {
 					}
 					return null;
 				}
 			}.getIcon());
 
 			jLabel6.setIcon(new javax.swing.JLabel() {
 				public javax.swing.Icon getIcon() {
 					try {
 						return new javax.swing.ImageIcon(
 								new java.net.URL(
 										"http://images4.wikia.nocookie.net/__cb20100320123411/runescape/images/1/12/Green_orb.png"));
 					} catch (java.net.MalformedURLException e) {
 					}
 					return null;
 				}
 			}.getIcon());
 
 			jLabel7.setFont(new java.awt.Font("Tahoma", 0, 18)); // NOI18N
 			jLabel7.setText("Play Style");
 
 			defend.setSelected(true);
 			defend.setText("Defend.");
 			defend.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					DefendActionPerformed(evt);
 				}
 			});
 
 			offend.setText("Offend.");
 			offend.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					OffendActionPerformed(evt);
 				}
 			});
 
 			jLabel8.setIcon(new javax.swing.JLabel() {
 				public javax.swing.Icon getIcon() {
 					try {
 						return new javax.swing.ImageIcon(
 								new java.net.URL(
 										"http://www.runescape.com/img/main/kbase/minigames/rcguild/attractor_wand.gif"));
 					} catch (java.net.MalformedURLException e) {
 					}
 					return null;
 				}
 			}.getIcon());
 
 			jLabel9.setIcon(new javax.swing.JLabel() {
 				public javax.swing.Icon getIcon() {
 					try {
 						return new javax.swing.ImageIcon(
 								new java.net.URL(
 										"http://runescape.salmoneus.net/assets/images/activities/great_orb_project/repellerwand.png"));
 					} catch (java.net.MalformedURLException e) {
 					}
 					return null;
 				}
 			}.getIcon());
 
 			jButton1.setText("Start");
 			jButton1.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					jButton1ActionPerformed(evt);
 				}
 			});
 
 			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
 					getContentPane());
 			getContentPane().setLayout(layout);
 			layout.setHorizontalGroup(layout
 					.createParallelGroup(
 							javax.swing.GroupLayout.Alignment.LEADING)
 					.addGroup(
 							layout.createSequentialGroup()
 									.addGroup(
 											layout.createParallelGroup(
 													javax.swing.GroupLayout.Alignment.LEADING)
 													.addGroup(
 															layout.createSequentialGroup()
 																	.addGap(72,
 																			72,
 																			72)
 																	.addComponent(
 																			jLabel2))
 													.addGroup(
 															layout.createSequentialGroup()
 																	.addGap(54,
 																			54,
 																			54)
 																	.addGroup(
 																			layout.createParallelGroup(
 																					javax.swing.GroupLayout.Alignment.LEADING)
 																					.addGroup(
 																							layout.createSequentialGroup()
 																									.addComponent(
 																											green)
 																									.addPreferredGap(
 																											javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 																									.addComponent(
 																											jLabel3))
 																					.addGroup(
 																							layout.createSequentialGroup()
 																									.addComponent(
 																											defend)
 																									.addPreferredGap(
 																											javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 																									.addComponent(
 																											jLabel8))
 																					.addGroup(
 																							layout.createSequentialGroup()
 																									.addComponent(
 																											offend)
 																									.addPreferredGap(
 																											javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 																									.addComponent(
 																											jLabel9))
 																					.addGroup(
 																							layout.createSequentialGroup()
 																									.addGroup(
 																											layout.createParallelGroup(
 																													javax.swing.GroupLayout.Alignment.TRAILING)
 																													.addComponent(
 																															jLabel7)
 																													.addComponent(
 																															yellow))
 																									.addPreferredGap(
 																											javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																									.addComponent(
 																											jLabel4)))
 																	.addGap(77,
 																			77,
 																			77))
 													.addGroup(
 															layout.createSequentialGroup()
 																	.addGap(30,
 																			30,
 																			30)
 																	.addComponent(
 																			jLabel1))
 													.addGroup(
 															javax.swing.GroupLayout.Alignment.TRAILING,
 															layout.createSequentialGroup()
 																	.addContainerGap()
 																	.addComponent(
 																			jLabel6)
 																	.addPreferredGap(
 																			javax.swing.LayoutStyle.ComponentPlacement.RELATED,
 																			41,
 																			Short.MAX_VALUE)
 																	.addComponent(
 																			jLabel5))
 													.addGroup(
 															layout.createSequentialGroup()
 																	.addGap(38,
 																			38,
 																			38)
 																	.addComponent(
 																			jButton1,
 																			javax.swing.GroupLayout.PREFERRED_SIZE,
 																			134,
 																			javax.swing.GroupLayout.PREFERRED_SIZE)))
 									.addContainerGap()));
 			layout.setVerticalGroup(layout
 					.createParallelGroup(
 							javax.swing.GroupLayout.Alignment.LEADING)
 					.addGroup(
 							layout.createSequentialGroup()
 									.addContainerGap()
 									.addComponent(jLabel1)
 									.addGap(40, 40, 40)
 									.addComponent(jLabel2)
 									.addPreferredGap(
 											javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 									.addGroup(
 											layout.createParallelGroup(
 													javax.swing.GroupLayout.Alignment.LEADING)
 													.addComponent(green)
 													.addComponent(jLabel3))
 									.addPreferredGap(
 											javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 									.addGroup(
 											layout.createParallelGroup(
 													javax.swing.GroupLayout.Alignment.LEADING)
 													.addComponent(yellow)
 													.addComponent(jLabel4))
 									.addGap(29, 29, 29)
 									.addComponent(jLabel7)
 									.addGap(11, 11, 11)
 									.addGroup(
 											layout.createParallelGroup(
 													javax.swing.GroupLayout.Alignment.TRAILING)
 													.addComponent(defend)
 													.addComponent(jLabel8))
 									.addPreferredGap(
 											javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 									.addGroup(
 											layout.createParallelGroup(
 													javax.swing.GroupLayout.Alignment.TRAILING)
 													.addComponent(jLabel9)
 													.addComponent(offend))
 									.addGap(18, 18, 18)
 									.addComponent(
 											jButton1,
 											javax.swing.GroupLayout.DEFAULT_SIZE,
 											39, Short.MAX_VALUE)
 									.addPreferredGap(
 											javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 									.addGroup(
 											layout.createParallelGroup(
 													javax.swing.GroupLayout.Alignment.LEADING)
 													.addGroup(
 															javax.swing.GroupLayout.Alignment.TRAILING,
 															layout.createSequentialGroup()
 																	.addComponent(
 																			jLabel5)
 																	.addGap(34,
 																			34,
 																			34))
 													.addGroup(
 															javax.swing.GroupLayout.Alignment.TRAILING,
 															layout.createSequentialGroup()
 																	.addComponent(
 																			jLabel6)
 																	.addContainerGap()))));
 
 			pack();
 		}
 
 		private void DefendActionPerformed(java.awt.event.ActionEvent evt) {
 			offend.setSelected(!defend.isSelected());
 		}
 
 		private void OffendActionPerformed(java.awt.event.ActionEvent evt) {
 			defend.setSelected(!offend.isSelected());
 		}
 
 		private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
 			if (defend.isSelected() || offend.isSelected()) {
 				if (defend.isSelected()) {
 					style = Style.Attrack;
 				} else {
 					style = Style.Repel;
 				}
 				if (yellow.isSelected() && green.isSelected()) {
 					Jointeam = JoinTeam.Randomly;
 				} else if (yellow.isSelected()) {
 					Jointeam = JoinTeam.Yellow;
 				} else {
 					Jointeam = JoinTeam.Green;
 				}
 				GUILoaded = true;
 				GOPGUI.setVisible(false);
 				GOPGUI.dispose();
 			} else {
 				stopScript();
 			}
 		}
 
 		private javax.swing.JRadioButton defend;
 		private javax.swing.JRadioButton green;
 		private javax.swing.JRadioButton offend;
 		private javax.swing.JRadioButton yellow;
 		private javax.swing.JButton jButton1;
 		private javax.swing.JLabel jLabel1;
 		private javax.swing.JLabel jLabel2;
 		private javax.swing.JLabel jLabel3;
 		private javax.swing.JLabel jLabel4;
 		private javax.swing.JLabel jLabel5;
 		private javax.swing.JLabel jLabel6;
 		private javax.swing.JLabel jLabel7;
 		private javax.swing.JLabel jLabel8;
 		private javax.swing.JLabel jLabel9;
 		// End of variables declaration
 	}
 
 	/**
 	 * 
 	 * @param area
 	 * @return true if player in area
 	 */
 	public boolean playerInArea(int[] area) {
 		int x = getMyPlayer().getLocation().getX();
 		int y = getMyPlayer().getLocation().getY();
 		if (x >= area[0] && y >= area[1] && x <= area[2] && y <= area[3])
 			return true;
 		return false;
 	}
 
 	private void AntiBan() {
 		switch (random(0, 3)) {
 		case 0:
 			checkXP(random(0, 24));
 		case 1:
 			moveMouseOffScreen(random(400, 600));
 		case 2:
 			moveCameraRandomly();
 		}
 	}
 
 	/**
 	 * @param Skill
 	 *            the skill number to check
 	 */
 	private void checkXP(int Skill) {
 		if (game.getTab() != Game.Tab.STATS) {
 			game.openTab(Game.Tab.STATS);
 			skills.doHover(Skill);
 		} else {
 			skills.doHover(Skill);
 		}
 		sleep(random(1000, 1500));
 	}
 
 	/**
 	 * @param Time
 	 *            The time to sleep in milliseconds.
 	 */
 	private void moveMouseOffScreen(int Time) {
 		mouse.moveOffScreen();
 		sleep(Time);
 	}
 
 	private void moveCameraRandomly() {
 		switch (random(0, 3)) {
 		case 0:
 			moveCameraToLeft(750, 1000);
 			camera.setPitch(random(30, 120));
 			sleep(50, 100);
 			break;
 
 		case 1:
 			moveCameraToRight(1000, 2500);
 			camera.setPitch(random(30, 120));
 			sleep(50, 100);
 			break;
 
 		}
 
 	}
 
 	/**
 	 * @param minTime
 	 *            The minimum time to sleep in milliseconds.
 	 * @param maxTime
 	 *            The maximum time to sleep in milliseconds.
 	 */
 	private void moveCameraToLeft(int minTime, int maxTime) {
 
 		keyboard.pressKey((char) KeyEvent.VK_LEFT);
 		sleep(random(minTime, maxTime));
 		keyboard.releaseKey((char) KeyEvent.VK_LEFT);
 	}
 
 	/**
 	 * @param minTime
 	 *            The minimum time to sleep in milliseconds.
 	 * @param maxTime
 	 *            The maximum time to sleep in milliseconds.
 	 */
 	private void moveCameraToRight(int minTime, int maxTime) {
 		keyboard.pressKey((char) KeyEvent.VK_RIGHT);
 		sleep(random(minTime, maxTime));
 		keyboard.releaseKey((char) KeyEvent.VK_RIGHT);
 
 	}
 
 	public class AdvancedOrb {
 
 		X X = new X();
 		Y Y = new Y();
 		
 		int MX = 0;
 		int MY = 0;
 
 		int OX = 0;
 		int OY = 0;
 
 		int AX = 0;
 		int AY = 0;
 
 		RSNPC orb;
 		RSObject alter;
 
 		AdvancedOrb(RSNPC orb, RSObject alter) {
 			if (orb != null && alter != null) {
 				this.orb = orb;
 				this.alter = alter;
 				MX = getMyPlayer().getLocation().getX();
 				MY = getMyPlayer().getLocation().getY();
 
 				OX = orb.getLocation().getX();
 				OY = orb.getLocation().getX();
 
 				AX = alter.getLocation().getX();
 				AY = alter.getLocation().getX();
 			}
 		}
 
 		public class X {
 
 			public boolean AX() {
 				return OX > AX && MX > AX;
 			}
 
 			public boolean BX() {
 				return OX > AX && MX < AX;
 			}
 
 			public boolean N2M() {
 				return AX() || BX();
 			}
 		}
 
 		public class Y {
 
 			public boolean AY() {
 				return OY > MY && MY < AY;
 			}
 
 			public boolean BY() {
 				return OY < AY && MY > AY;
 			}
 
 			public boolean N2M() {
 				return AY() || BY();
 			}
 		}
 		public boolean N2M(){
 			return X.N2M() || Y.N2M();
 		}
 	}
 }
