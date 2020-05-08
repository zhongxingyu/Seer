 package com.pieno.jeu;
 
 import com.pieno.jeu.Monster;
 import com.pieno.jeu.Ladder;
 import com.pieno.jeu.Platform;
 import com.pieno.jeu.Wall;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.text.DecimalFormat;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import com.esotericsoftware.kryonet.Connection;
 import com.esotericsoftware.kryonet.Listener;
 import com.esotericsoftware.kryonet.Server;
 import com.esotericsoftware.minlog.Log;
 import com.pieno.jeu.Network.*;
 
 public class Server2D extends JFrame implements WindowListener {
 	Server server;
 	PhysicsThread PT;
 	SendThread ST;
 	String ip;
 	Vector<CharacterConnection> characterConnections = new Vector<CharacterConnection>();
 	Vector<Character> loggedIn = new Vector<Character>();
 	Vector<Map> maps = new Vector<Map>();
 	HashMap<String, Vector<Projectile>> projectiles = new HashMap<String, Vector<Projectile>>();
 	HashMap<String, Vector<Effect>> effects = new HashMap<String, Vector<Effect>>();
 	HashMap<String, Vector<Drop>> drops = new HashMap<String, Vector<Drop>>();
 	Vector<String> loadedMaps = new Vector<String>();
 
 	public JPanel pane1, pane2, pane3, content;
 
 	public JButton dc_btn;
 	public JList loggedIn_l;
 	public DefaultListModel connectionsList = new DefaultListModel();
 
 	public static void main(String[] args) throws IOException {
 		Log.set(Log.LEVEL_INFO);
 		new Server2D();
 	}
 
 	public Server2D() throws IOException {
 		server = new Server() {
 			protected Connection newConnection() {
 				CharacterConnection c = new CharacterConnection();
 				characterConnections.add(c);
 				connectionsList.addElement(c);
 				return c;
 			}
 		};
 
 		// For consistency, the classes to be sent over the network are
 		// registered by the same method for both the client and server.
 		Network.register(server);
 
 		server.addListener(new Listener.ThreadedListener(new Listener()) {
 			public void received(Connection c, Object object) {
 				// We know all connections for this server are actually
 				// CharacterConnections.
 				CharacterConnection connection = (CharacterConnection) c;
 				Character character = connection.character;
 
 				if (object instanceof Authentication) {
 					Authentication auth = (Authentication) object;
 					AuthResponse r = new AuthResponse();
 					;
 
 					File rootDir = new File("C:/");
 					if (isMac())
 						rootDir = new File(System.getProperty("user.home") + "/Documents");
 					File path = new File(rootDir, "/jeu/" + auth.name);
 					if (!path.exists()) {
 						new File(path, auth.pass).mkdirs();
 					}
 					if (new File(path, auth.pass).exists()) {
 						r.accepted = true;
 						connection.sendTCP(r);
 					} else {
 						r.accepted = false;
 						connection.sendTCP(r);
 					}
 					return;
 				}
 
 				if (object instanceof Login) {
 					// Ignore if already logged in.
 					if (character != null)
 						return;
 
 					Login login = (Login) object;
 
 					// Reject if the name is invalid.
 					String name = login.name;
 					if (!isValid(name)) {
 						c.close();
 						return;
 					}
 
 					// Reject if already logged in.
 					for (Character other : loggedIn) {
 						if (other.name.equals(name)) {
 							c.close();
 							return;
 						}
 					}
 					// Reject if wrong password
 					File rootDir = new File("C:/");
 					if (isMac())
 						rootDir = new File(System.getProperty("user.home") + "/Documents");
 					if (!new File(rootDir, "/jeu/" + login.name + "/" + login.pass).exists()) {
 						c.close();
 						return;
 					}
 
 					character = loadCharacter(name, login.saveSlot);
 
 					// Reject if couldn't load character.
 					if (character == null) {
 						c.sendTCP(new RegistrationRequired());
 						return;
 					}
 
 					character.setInvincible(1000);
 					loggedIn(connection, character);
 
 					Stats update = new Stats();
 					update.atts = character.atts;
 					update.attStats = character.attStats;
 					server.sendToTCP(character.connection, update);
 
 					Skills updates = new Skills();
 					updates.skillLvls = character.skillLvls;
 					updates.passiveLvls = character.passiveLvls;
 					updates.skillStats = character.skillStats;
 					server.sendToTCP(character.connection, updates);
 					sendStats(character);
 					return;
 				}
 
 				if (object instanceof RequestStats) {
 					sendStats(character);
 					return;
 				}
 
 				if (object instanceof RemoveCharacter) {
 					saveCharacter(connection.character);
 					loggedIn.remove(connection.character);
 
 					RemoveCharacter removeCharacter = new RemoveCharacter();
 					removeCharacter.id = connection.character.id;
 					server.sendToAllTCP(removeCharacter);
 					connection.character = null;
 					return;
 				}
 
 				if (object instanceof SwitchItems) {
 					Random rand = new Random();
 					SwitchItems switchItems = (SwitchItems) object;
 					Item previousItem;
 					if (switchItems.add) {
 						Item bob = character.inventory.getEquip(switchItems.previousI);
 						character.inventory.delete(switchItems.previousI);
 						character.inventory.add(bob);
 
 					} else if (switchItems.previousJ == -1) {
 						previousItem = character.inventory.getEquip(switchItems.previousI);
 						if (switchItems.i == -1) {
 							add(new Drop(previousItem, new Point(character.getX() + rand.nextInt(70), character.getY() + 150), character.id), character.map);
 							character.inventory.delete(switchItems.previousI);
 						} else if (character.inventory.getItem(switchItems.i, switchItems.j) == null
 								|| (character.inventory.getItem(switchItems.i, switchItems.j).getSlot() == switchItems.previousI && character.inventory.getItem(
 										switchItems.i, switchItems.j).getLvl() <= character.lvl)) {
 							character.inventory.setEquip(character.inventory.getItem(switchItems.i, switchItems.j), switchItems.previousI);
 							character.inventory.setItem(previousItem, switchItems.i, switchItems.j);
 						}
 					} else {
 						previousItem = character.inventory.getItem(switchItems.previousI, switchItems.previousJ);
 						if (switchItems.i == -1) {
 							add(new Drop(previousItem, new Point(character.getX() + rand.nextInt(70), character.getY() + 150), character.id), character.map);
 							character.inventory.delete(switchItems.previousI, switchItems.previousJ);
 						} else if (switchItems.j == -1) {
 							if (previousItem == null || (previousItem.getSlot() == switchItems.i && previousItem.getLvl() <= character.lvl)) {
 								character.inventory.setItem(character.inventory.getEquip(switchItems.i), switchItems.previousI, switchItems.previousJ);
 								character.inventory.setEquip(previousItem, switchItems.i);
 							}
 						} else {
 							character.inventory.setItem(character.inventory.getItem(switchItems.i, switchItems.j), switchItems.previousI, switchItems.previousJ);
 							character.inventory.setItem(previousItem, switchItems.i, switchItems.j);
 						}
 					}
 					updateInventory(character);
 				}
 
 				if (object instanceof Pickup) {
 					Pickup pickup = (Pickup) object;
 					for (int i = 0; i < drops.get(character.map).size(); i++) {
 						if (drops.get(character.map).get(i).getArea().intersects(character.getArea())
 								&& (drops.get(character.map).get(i).ownerID == -1 || drops.get(character.map).get(i).ownerID == character.id)) {
 							character.inventory.add(drops.get(character.map).get(i).item);
 							RemoveDrop update = new RemoveDrop();
 							update.id = drops.get(character.map).get(i).id;
 							sendToAllOnMap(character.map, update, false);
 							drops.get(character.map).remove(i);
 							updateInventory(character);
 							return;
 						}
 					}
 					return;
 				}
 
 				if (object instanceof Lvlstat) {
 					Lvlstat lvlstat = (Lvlstat) object;
 					if (lvlstat.up && character.attStats > 0) {
 						character.baseAtts[lvlstat.stat]++;
 						character.attStats--;
 						character.loadStats();
 					} else if (!lvlstat.up) {
 						character.baseAtts[lvlstat.stat]--;
 						character.attStats++;
 						character.loadStats();
 					}
 					Stats update = new Stats();
 					update.atts = character.atts;
 					update.attStats = character.attStats;
 					connection.sendTCP(update);
 					sendStats(character);
 					return;
 				}
 
 				if (object instanceof Lvlskill) {
 					Lvlskill lvlskill = (Lvlskill) object;
 					if (lvlskill.passive) {
 						if (lvlskill.up)
 							character.passives[lvlskill.skill].addLvl();
 						else
 							character.passives[lvlskill.skill].removeLvl();
 					} else {
 						if (lvlskill.up)
 							character.skills[lvlskill.skill].addLvl();
 						else
 							character.skills[lvlskill.skill].removeLvl();
 					}
 					Skills update = new Skills();
 					update.skillLvls = character.skillLvls;
 					update.passiveLvls = character.passiveLvls;
 					update.skillStats = character.skillStats;
 					connection.sendTCP(update);
 
 					sendStats(character);
 					return;
 				}
 
 				if (object instanceof Register) {
 					// Ignore if already logged in.
 					if (character != null)
 						return;
 
 					Register register = (Register) object;
 
 					// Reject if the login is invalid.
 					if (!isValid(register.name)) {
 						c.close();
 						return;
 					}
 
 					// Reject if character alread exists.
 					if (loadCharacter(register.name, register.saveSlot) != null) {
 						c.close();
 						return;
 					}
 
 					character = new Character();
 					character.classe = register.classe;
 					character.name = register.name;
 					character.saveSlot = register.saveSlot;
 					character.x = 0;
 					character.y = 0;
 					if (!saveCharacter(character)) {
 						c.close();
 						return;
 					}
 					character = loadCharacter(character.name, character.saveSlot);
 					loggedIn(connection, character);
 
 					Stats update = new Stats();
 					update.atts = character.atts;
 					update.attStats = character.attStats;
 					connection.sendTCP(update);
 
 					Skills updates = new Skills();
 					updates.skillLvls = character.skillLvls;
 					updates.passiveLvls = character.passiveLvls;
 					updates.skillStats = character.skillStats;
 					connection.sendTCP(updates);
 
 					sendStats(character);
 
 					return;
 				}
 
 				if (object instanceof RequestSaves) {
 					sendSavesData(connection.getID(), ((RequestSaves) object).name);
 					return;
 				}
 
 				if (object instanceof DeleteSave) {
 					DeleteSave delete = (DeleteSave) object;
 					File rootDir = new File("C:/");
 					if (isMac())
 						rootDir = new File(System.getProperty("user.home") + "/Documents");
 					File file = new File(rootDir, "/jeu/" + delete.name + "/save" + delete.slot + ".sav");
 					File backup = new File(rootDir, "/jeu/" + delete.name + "/backup" + delete.slot);
 					if (backup.exists())
 						backup.delete();
 					file.renameTo(backup);
 					sendSavesData(connection.getID(), delete.name);
 
 					return;
 				}
 
 				if (object instanceof PressedKeys) {
 					// Ignore if not logged in.
 					if (character == null)
 						return;
 
 					PressedKeys msg = (PressedKeys) object;
 
 					character.moveKeys = msg.moveKeys;
 					character.setSkill(msg.skill);
 					return;
 				}
 			}
 
 			private void sendSavesData(int id, String name) {
 				SavesInfo saves = new SavesInfo();
 				Character ch;
 				for (int i = 0; i < 4; i++) {
 					saves.saves[i] = new SaveInfo();
 					if ((ch = loadCharacter(name, i)) != null) {
 						saves.saves[i].classe = ch.classe;
 						saves.saves[i].lvl = ch.lvl;
 					}
 				}
 				server.sendToTCP(id, saves);
 			}
 
 			private boolean isValid(String value) {
 				if (value == null)
 					return false;
 				value = value.trim();
 				if (value.length() == 0)
 					return false;
 				return true;
 			}
 
 			public void disconnected(Connection c) {
 				connectionsList.removeElement(c);
 				CharacterConnection connection = (CharacterConnection) c;
 				if (connection.character != null) {
 					saveCharacter(connection.character);
 					loggedIn.remove(connection.character);
 					RemoveCharacter removeCharacter = new RemoveCharacter();
 					removeCharacter.id = connection.character.id;
 					server.sendToAllTCP(removeCharacter);
 				}
 				characterConnections.remove(c);
 				connectionsList.removeElement(c);
 			}
 		});
 		ip = InetAddress.getLocalHost().getHostAddress();
 		server.bind(new InetSocketAddress(InetAddress.getLocalHost(), Network.port), new InetSocketAddress(InetAddress.getLocalHost(), Network.port + 1));
 		server.start();
 		PT = new PhysicsThread();
 		PT.start();
 		ST = new SendThread();
 		ST.start();
 
 		initWindow();
 	}
 
 	void initWindow() {
 
 		setTitle("Server - " + ip);
 
 		dc_btn = new JButton();
 		dc_btn.setText("Disconnect");
 		dc_btn.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int selected = loggedIn_l.getSelectedIndex();
 
 				if (selected > -1 && characterConnections.size() > selected) {
 
 					if (characterConnections.get(selected).character != null) {
 						saveCharacter(characterConnections.get(selected).character);
 
 						RemoveCharacter removeCharacter = new RemoveCharacter();
 						removeCharacter.id = characterConnections.get(selected).character.id;
 						loggedIn.remove(characterConnections.get(selected).character);
 						server.sendToAllTCP(removeCharacter);
 
 					}
 
 					characterConnections.get(selected).close();
 
 				}
 
 			}
 
 		});
 		loggedIn_l = new JList(connectionsList);
 
 		pane1 = new JPanel();
 		pane1.setLayout(new GridLayout(1, 1, 1, 1));
 		pane1.add(dc_btn);
 
 		pane2 = new JPanel();
 		pane2.add(new JLabel(ip));
 
 		pane3 = new JPanel();
 		pane3.setLayout(new BorderLayout(1, 1));
 		pane3.add(pane1, BorderLayout.NORTH);
 		pane3.add(new JScrollPane(loggedIn_l), BorderLayout.CENTER);
 		pane3.add(pane2, BorderLayout.SOUTH);
 
 		content = new JPanel();
 		content.setLayout(new GridLayout(1, 1, 1, 1));
 		content.add(pane3);
 
 		content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 
 		setContentPane(content);
 		pack();
 		setSize(400, 400);
 		setResizable(false);
 		setLocationRelativeTo(null);
 		addWindowListener(this);
 		setVisible(true);
 
 	}
 
 	void updateInventory(Character c) {
 		c.loadStats();
 		Equips equip = new Equips();
 		equip.equips = c.inventory.equip;
 		server.sendToTCP(c.connection, equip);
 		Items items = new Items();
 		items.items = c.inventory.items;
 		server.sendToTCP(c.connection, items);
 		sendStats(c);
 	}
 
 	synchronized void sendToAllOnMap(String map, Object update, boolean UDP) {
 		for (int i = 0; i < loggedIn.size(); i++) {
 			if (loggedIn.get(i).map.equals(map)) {
 				if (UDP)
 					server.sendToUDP(loggedIn.get(i).connection, update);
 				else
 					server.sendToTCP(loggedIn.get(i).connection, update);
 			}
 		}
 	}
 
 	void loggedIn(CharacterConnection c, Character character) {
 		c.character = character;
 		character.connection = c.getID();
 
 		// Add existing characters to new logged in connection.
 		for (Character other : loggedIn) {
 			AddCharacter addCharacter = new AddCharacter();
 			addCharacter.character = other;
 			c.sendTCP(addCharacter);
 		}
 
 		loggedIn.add(character);
 
 		// Add logged in character to all connections.
 		AddCharacter addCharacter = new AddCharacter();
 		addCharacter.character = character;
 		server.sendToAllTCP(addCharacter);
 		updateInventory(addCharacter.character);
 	}
 
 	public static boolean isMac() {
 
 		String os = System.getProperty("os.name").toLowerCase();
 		return (!(os.indexOf("win") >= 0));
 
 	}
 
 	boolean saveCharacter(Character character) {
 		File rootDir = new File("C:/");
 		if (isMac())
 			rootDir = new File(System.getProperty("user.home") + "/Documents");
 
 		File file = new File(rootDir, "jeu/" + character.name + "/save" + character.saveSlot + ".sav");
 		file.getParentFile().mkdirs();
 
 		if (character.id == 0) {
 			String[] children = file.getParentFile().list();
 			if (children == null)
 				return false;
 			character.id = children.length + 1;
 		}
 		if (!character.alive)
 			character.respawn();
 
 		ObjectOutputStream output = null;
 		try {
 			output = new ObjectOutputStream(new FileOutputStream(file));
 			output.writeInt(character.classe);
 			output.writeInt(character.id);
 			output.writeInt(character.lvl);
 			output.writeInt(character.exp);
 			output.writeInt(character.getX());
 			output.writeInt(character.getY());
 			output.writeInt(character.life);
 			output.writeInt(character.mana);
 			output.writeInt(character.skillStats);
 			output.writeInt(character.attStats);
 			output.writeObject(character.map);
 			output.writeObject(character.baseAtts);
 			output.writeObject(character.skillLvls);
 			output.writeObject(character.passiveLvls);
 			output.writeObject(character.inventory);
 			return true;
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			return false;
 		} finally {
 			try {
 				output.close();
 			} catch (IOException ignored) {
 			}
 		}
 	}
 
 	Character loadCharacter(String name, int saveSlot) {
 		File rootDir = new File("C:/");
 		if (isMac())
 			rootDir = new File(System.getProperty("user.home") + "/Documents");
 		File file = new File(rootDir, "jeu/" + name + "/save" + saveSlot + ".sav");
 		if (!file.exists())
 			return null;
 		ObjectInputStream input = null;
 		try {
 			input = new ObjectInputStream(new FileInputStream(file));
 			Character character = new Character();
 			character.saveSlot = saveSlot;
 			character.classe = input.readInt();
 			for (int i = 0; i < 3; i++) {
 				character.skills[i] = new Skill(character, i);
 			}
 			for (int i = 0; i < 1; i++) {
 				character.passives[i] = new PassiveSkill(i, character);
 			}
 			character.id = input.readInt();
 			character.lvl = input.readInt();
 			character.exp = input.readInt();
 			character.name = name;
 			character.x = input.readInt();
 			character.y = input.readInt();
 			character.life = input.readInt();
 			character.mana = input.readInt();
 			character.skillStats = input.readInt();
 			character.attStats = input.readInt();
 			character.map = (String) input.readObject();
 			character.baseAtts = (int[]) input.readObject();
 			character.skillLvls = (int[]) input.readObject();
 			character.passiveLvls = (int[]) input.readObject();
 			character.inventory = (Inventory) input.readObject();
 			input.close();
 			character.loadStats();
 			character.setInvincible(1000);
 			return character;
 		} catch (IOException ex) {
 			ex.printStackTrace();
 			return null;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			try {
 				if (input != null)
 					input.close();
 			} catch (IOException ignored) {
 			}
 		}
 	}
 
 	// This holds per connection state.
 	static class CharacterConnection extends Connection {
 		public Character character;
 
 		public String toString() {
 			try {
 				InetAddress address = getRemoteAddressTCP().getAddress();
 				if (character != null) {
 					return character.toString() + " connection " + getID() + ", " + address.getHostAddress();
 				}
 				return "No character (" + address.getHostName() + ") connection " + getID() + ", " + address.getHostAddress();
 			} catch (Exception e) {
 				return "Connection " + getID();
 			}
 		}
 	}
 
 	public void sendStats(Character c) {
 		CharStats stats = new CharStats();
 		stats.atts = c.atts;
 		stats.crit = c.critChance;
 		stats.critd = c.critDamage;
 		stats.mastery = c.mastery;
 		stats.watk = c.wATK;
 		stats.def = c.defense;
 		stats.rarec = c.RF;
 		stats.itemf = c.MF;
 		server.sendToTCP(c.connection, stats);
 
 	}
 
 	public void add(Projectile projectile) {
 		AddProjectile update = new AddProjectile();
 		projectile.id = projectiles.get(projectile.map).size() - 1;
 		projectiles.get(projectile.map).add(projectile);
 		update.id = projectile.id;
 		update.type = projectile.type;
 		update.x = (int) projectile.x;
 		update.y = (int) projectile.y;
 		update.vx = projectile.vx;
 		update.vy = projectile.vy;
 		sendToAllOnMap(projectile.map, update, false);
 	}
 
 	public void add(Effect effect) {
 		AddEffect update = new AddEffect();
 		effects.get(effect.map).add(effect);
 		update.id = effects.get(effect.map).size() - 1;
 		update.type = effect.type;
 		update.x = (int) effect.x;
 		update.y = (int) effect.y;
 		if (effect.c != null)
 			update.cid = effect.c.id;
 		sendToAllOnMap(effect.map, update, false);
 	}
 
 	public class SendThread extends Thread {
 		public void run() {
 			while (true) {
 				if (!loggedIn.isEmpty()) {
 					sendCharsData();
 					try {
 						Thread.sleep(6);
 					} catch (Exception e) {
 					}
 					sendMonstersData();
 					try {
 						Thread.sleep(6);
 					} catch (Exception e) {
 					}
 				} else {
 					try {
 						Thread.sleep(100);
 					} catch (Exception e) {
 					}
 				}
 			}
 		}
 
 		void sendMonstersData() {
 			Vector<Map> tempMaps = new Vector<Map>(maps);
 			for (Map map : tempMaps) {
 				for (int i = 0; i < map.getMonsters().size(); i++) {
 					Monster m = map.getMonsters().get(i);
 					sendMonsterData(m, map.map, i);
 					if (m.summonType != -1)
 						for (int y = 0; y < m.summons.size(); y++) {
 							sendMonsterData(m.summons.get(y), map.map, 100 + i * 20 + y);
 						}
 				}
 			}
 		}
 
 		void sendMonsterData(Monster m, String map, int id) {
 			UpdateMonster update = new UpdateMonster();
 			update.x = m.getX();
 			update.y = m.getY();
 			update.id = id;
 			update.canMove = m.canMove;
 			update.facingLeft = m.isFacingLeft();
 			update.type = m.type;
 			update.alive = m.isAlive();
 			update.lifePercentage = (int) (1000 * m.life / m.getMaxLife());
 			update.eliteType = m.eliteT;
 			update.lvl = m.getLevel();
 			sendToAllOnMap(map, update, true);
 		}
 
 		void sendCharsData() {
 			// send all updated chars
 			for (int i = 0; i < loggedIn.size(); i++) {
 				Character c = loggedIn.get(i);
 				UpdateCharacter update = new UpdateCharacter();
 				update.id = c.id;
 				update.x = c.getX();
 				update.y = c.getY();
 				update.life = c.life;
 				update.maxLife = c.maxLife;
 				update.mana = c.mana;
 				update.maxMana = c.maxMana;
 				update.invincible = c.invincible;
 				if (c.currentSkill == -1)
 					update.usingSkill = false;
 				// Animation
 				if (c.onLadder != null) {
 					if (c.vy == 0 && c.vx == 0)
 						update.currentAnim = Network.onladder;
 					else if (c.vy == 0)
 						update.currentAnim = Network.climbSide;
 					else
 						update.currentAnim = Network.climb;
 				} else {
 					if (c.vy == 0) {
 						if (c.vx > 0)
 							update.currentAnim = Network.walkR;
 						else if (c.vx < 0)
 							update.currentAnim = Network.walkL;
 						else if (c.facingLeft)
 							update.currentAnim = Network.standL;
 						else
 							update.currentAnim = Network.standR;
 					} else {
 						if (c.facingLeft)
 							update.currentAnim = Network.jumpL;
 						else
 							update.currentAnim = Network.jumpR;
 					}
 				}
 				sendToAllOnMap(c.map, update, true);
 
 			}
 		}
 	}
 
 	public class PhysicsThread extends Thread {
 		long currentTime = System.currentTimeMillis();
 		long timePassed = 0;
 
 		@Override
 		public void run() {
 			while (true) {
 				if (loggedIn.isEmpty()) {
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 					}
 					currentTime = System.currentTimeMillis();
 					if (!maps.isEmpty())
 						loadmaps();
 				} else {
 
 					loadmaps();
 
 					timePassed = System.currentTimeMillis() - currentTime;
 					currentTime += timePassed;
 					if (timePassed > 20)
 						timePassed = 20;
 					update(timePassed);
 
 					try {
 						Thread.sleep(5);
 					} catch (InterruptedException e) {
 					}
 				}
 			}
 		}
 
 		synchronized void loadmaps() {
 			Vector<String> usedMaps = new Vector<String>();
 			for (int i = 0; i < loggedIn.size(); i++) {
 				if (!usedMaps.contains(loggedIn.get(i).map))
 					usedMaps.add(loggedIn.get(i).map);
 			}
 
 			for (String map : usedMaps) {
 				if (!loadedMaps.contains(map)) {
 					loadedMaps.add(map);
 					maps.add(new Map(map));
 					projectiles.put(map, new Vector<Projectile>());
 					effects.put(map, new Vector<Effect>());
 					drops.put(map, new Vector<Drop>());
 				}
 			}
 			Vector<Map> tempMaps = new Vector<Map>(maps);
 			for (Map map : tempMaps) {
 				if (!usedMaps.contains(map.map)) {
 					loadedMaps.remove(map.map);
 					maps.remove(map);
 					projectiles.remove(map.map);
 					effects.remove(map.map);
 					drops.remove(map.map);
 				}
 			}
 		}
 
 		synchronized void update(long timePassed) {
 			for (int i = 0; i < loggedIn.size(); i++) {
 				Character c = loggedIn.get(i);
 				if (c.moveKeys[Network.LEFT])
 					c.dir = -1;
 				else if (c.moveKeys[Network.RIGHT])
 					c.dir = 1;
 				else
 					c.dir = 0;
 				if (c.moveKeys[Network.JUMP] && (c.vy == 0 || c.onLadder != null)) {
 					c.jump();
 					c.onLadder = null;
 				}
 				collisions(c, c.map);
 				c.update(timePassed);
 				if (c.lastSave > 60000 && c.alive) {
 					saveCharacter(c);
 					c.lastSave = 0;
 				}
 			}
 			Vector<Map> tempMaps = new Vector<Map>(maps);
 			for (Map map : tempMaps) {
 				monsterCollisions(map);
 				for (Monster m : map.getMonsters()) {
 					m.update(timePassed);
 					if (m.summonType != -1) {
 						for (Monster summon : m.summons) {
 							summon.update(timePassed);
 						}
 					}
 				}
 
 				for (int i = 0; i < projectiles.get(map.map).size(); i++) {
 					Projectile projectile = projectiles.get(map.map).get(i);
 					projectile.update(timePassed);
 					if (projectile.active)
 						projectileCollisions(projectile, map);
 					else {
 						RemoveProjectile update = new RemoveProjectile();
 						update.id = projectile.id;
 						sendToAllOnMap(map.map, update, false);
 						projectiles.get(map.map).remove(i);
 					}
 				}
 				Vector<Effect> tempEffects = new Vector<Effect>(effects.get(map.map));
 				for (Effect effect : tempEffects)
 					effect.update(timePassed);
 				Vector<Drop> tempDrops = new Vector<Drop>(drops.get(map.map));
 				for (Drop drop : tempDrops)
 					drop.update(timePassed);
 
 			}
 
 		}
 
 		void projectileCollisions(Projectile proj, Map map) {
 			Vector<Monster> monsters = new Vector<Monster>(map.getMonsters());
 			for (int i = 0; i < monsters.size(); i++) {
 				Monster monster = monsters.get(i);
 				if (monster.isAlive() && proj.isActive() && proj.getArea().intersects(monster.getArea())) {
 					if (proj.skill.skill == Skill.ExplosiveArrow) {
 						int x = (int) proj.x + 140;
 						if (proj.skill.cLeft)
 							x = (int) proj.x - 160;
 						add(proj.skill.skillEffect = new Effect(new Point(x, (int) proj.y - 15), Network.EXPLOARROW, 200, map.map));
 						proj.skill.hit(0);
 					} else
 						damageMonster(proj.skill.c, monster, proj.skill.c.getDamage(monster, proj.skill.skillData.dmgMult[proj.number]),
 								proj.skill.skillData.KBSpeed[proj.number]);
 					RemoveProjectile update = new RemoveProjectile();
 					update.id = proj.id;
 					sendToAllOnMap(map.map, update, false);
 					proj.delete();
 					return;
 				}
 			}
 			Vector<Wall> walls = new Vector<Wall>(map.getWalls());
 			for (Wall wall : walls) {
 				if (proj.isActive() && proj.getArea().intersects(wall.getArea())) {
 					RemoveProjectile update = new RemoveProjectile();
 					update.id = proj.id;
 					sendToAllOnMap(map.map, update, false);
 					proj.delete();
 					return;
 				}
 			}
 		}
 
 		void monsterCollisions(Map map) {
 
 			float multiPlayer = 0.301f;
 			for (int i = 0; i < loggedIn.size(); i++) {
 				if (loggedIn.get(i).map.equals(map.map))
 					multiPlayer += 0.7f;
 			}
 
 			for (Monster monster : map.getMonsters()) {
 				monsterCollision(map, monster, multiPlayer);
 				if (monster.summonType != -1)
 					for (Monster summon : monster.summons) {
 						monsterCollision(map, summon, multiPlayer);
 					}
 			}
 
 		}
 
 		void monsterCollision(Map map, Monster monster, float multiPlayer) {
 			if (monster.isAlive()) {
 
 				monster.setLifeMultiplier(multiPlayer);
 
 				boolean canFall = true;
 
 				for (Wall wall : map.getWalls())
 					if (wall != null)
 						if (wall.getTop().intersects(monster.getBase()) && monster.vy >= 0) {
 							monster.y = wall.getTopY() - monster.getHeight();
 							monster.vy = 0;
 							canFall = false;
 						}
 
 				for (Platform platform : map.getPlatforms())
 					if (platform != null)
 						if (platform.getTop().intersects(monster.getBase()) && monster.vy >= 0) {
 							monster.y = platform.getTopY() - monster.getHeight();
 							monster.vy = 0;
 							canFall = false;
 						}
 
 				turnMonster(monster, map);
 
 				if (monster.y < 0 && monster.vy < 0) {
 					monster.vy = 0;
 					monster.y = 0;
 				}
 				if (monster.y + monster.getHeight() > map.getYLimit() && monster.vy > 0) {
 					monster.vy = 0;
 					monster.y = map.getYLimit() - monster.getHeight();
 					canFall = false;
 				}
 
 				if (monster.x < 0 && monster.vx < 0) {
 					monster.x = 0;
 					monster.vx = -monster.getSpeed();
 				} else if (monster.x + monster.getWidth() > map.getXLimit() && monster.vx > 0) {
 					monster.x = map.getXLimit() - monster.getWidth();
 					monster.vx = monster.getSpeed();
 				}
 
 				if (canFall)
 					monster.fall(timePassed);
 			}
 		}
 
 		public void turnMonster(Monster m, Map map) {
 
 			boolean turn, facingwall = false;
 			if (m.vy == 0)
 				turn = true;
 			else
 				turn = false;
 
 			for (Wall wall : map.getWalls())
 				if (wall != null)
 					if (wall.getTop().intersects(m.getNextFloor()))
 						turn = false;
 			for (Platform platform : map.getPlatforms())
 				if (platform != null)
 					if (platform.getTop().intersects(m.getNextFloor()))
 						turn = false;
 
 			for (Wall wall : map.getWalls())
 				if (wall != null)
 					if (wall.getSide().intersects(m.getSide())) {
 						turn = true;
 						facingwall = true;
 					}
 
 			if (turn) {
 				if (m.canMove()) {
 					if (m.isAggro != null && isFacingChar(m) && !facingwall && m.vy == 0) {
 						m.jump();
 					} else {
 						m.vx = -m.vx;
 						m.setFacingLeft(!m.isFacingLeft());
 					}
 
 				} else
 					m.vx = 0;
 			}
 
 			if (m.isAggro != null)
 				if (m.canMove() && (m.vx != m.getSpeed() && m.vx != -m.getSpeed())) {
 					if (m.isAggro.getX() + 60 > m.x + m.getWidth() / 2) {
 						m.vx = m.getSpeed();
 						m.setFacingLeft(false);
 					} else {
 						m.vx = -m.getSpeed();
 						m.setFacingLeft(true);
 					}
 				}
 
 		}
 
 		public boolean isFacingChar(Monster m) {
 			if (m.isAggro.y + 240 > m.y) {
 				if ((m.vx > 0 && m.isAggro.getX() > m.x) || (m.vx < 0 && m.isAggro.getX() < m.x))
 					return true;
 			}
 			return false;
 		}
 
 		// frappe un personnage
 		public void hit(Monster m, Character c) {
 			int dmg = getDamage(m, c);
 
 			c.damageChar(dmg);
 			if (dmg >= (c.maxLife * 5 / 100)) {
 				double vx = 0.35d;
 				if ((m.x + m.getWidth() / 2) > (c.getX() + c.getWidth() / 2))
 					vx = -vx;
 				c.vx = vx;
 				c.vy = -0.6d;
 				c.canMove = false;
 			}
 			if(!c.alive) {
 				AddExp exp = new AddExp();
 				exp.exp = c.exp;
 				exp.lvl = c.lvl;
 				server.sendToTCP(c.connection, exp);
 			} else
 			c.setInvincible(1000);
 			
 			DamageText update = new DamageText();
 			update.dmg = dmg;
 			update.x = c.getX() + c.getWidth() / 3;
 			update.y = c.getY() + c.getHeight() / 2;
 			update.crit = false;
 			sendToAllOnMap(c.map, update, true);
 		}
 
 		// retourne les dgats si le monstre frappe un personnage
 		public int getDamage(Monster m, Character c) {
 			Random rand = new Random();
 			int dmast = rand.nextInt(100 - m.getMastery()) + m.getMastery();
 			int dmg = m.getAtk();
 			dmg = dmg * dmast / 100;
 			dmg = (int) (dmg * (1 - c.defense / (c.defense + 22 * Math.pow(1.1, m.getLevel()))));
 			if (dmg <= 0)
 				dmg = 1;
 			return dmg;
 		}
 
 		void collisions(Character c, String m) {
 			if (c.alive) {
 				boolean canFall = true;
 				Map map = null;
 				Vector<Map> tempMaps = new Vector<Map>(maps);
 				for (Map bob : tempMaps) {
 					if (bob.map.equals(m)) {
 						map = bob;
 						break;
 					}
 				}
 				if (map == null)
 					return;
 
 				if (!c.invincible) {
 					for (Monster monster : map.getMonsters()) {
 						if (monster.isAlive())
 							if (monster.getArea().intersects(c.getArea()))
 								hit(monster, c);
 						if (monster.summonType != -1)
 							for (Monster summon : monster.summons) {
 								if (summon.isAlive())
 									if (summon.getArea().intersects(c.getArea()))
 										hit(summon, c);
 							}
 					}
 				}
 
 				for (Platform platform : map.getPlatforms()) {
 					if (c.getBase().intersects(platform.getTop())) {
 
 						if (!c.canMove && c.vy >= 0) {
 							c.vx = 0;
 							c.canMove = true;
 						}
 						if (c.vy >= 0 && !c.moveKeys[Network.DOWN]) {
 							canFall = false;
 							c.vy = 0;
 							c.y = platform.getTopY() - c.getHeight();
 						} else if (c.vy >= 0 && c.onLadder != null) {
 							boolean touchesLadder = false;
 							for (Ladder ladder : map.getLadders())
 								if (platform.getTop().intersects(ladder.getTop()) || platform.getTop().intersects(ladder))
 									touchesLadder = true;
 							if (!touchesLadder) {
 								c.onLadder = null;
 								c.vy = 0;
 								c.y = platform.getTopY() - c.getHeight();
 							}
 						}
 					}
 				}
 
 				for (Wall wall : map.getWalls()) {
 					if (c.vx < 0 && c.getLeftSide().intersects(wall.getSide())) {
 						c.x = wall.getX() + wall.getWidth() - 21;
 						c.vx = 0;
 						if (c.dir < 0)
 							c.dir = 0;
 						if (c.xspeed < 0)
 							c.xspeed = 0;
 
 					} else if (c.vx > 0 && c.getRightSide().intersects(wall.getSide())) {
 						c.x = wall.getX() - c.getWidth() + 21;
 						c.vx = 0;
 						if (c.dir > 0)
 							c.dir = 0;
 						if (c.xspeed > 0)
 							c.xspeed = 0;
 					}
 
 					if (c.getBase().intersects(wall.getTop())) {
 
 						if (c.vy >= 0) {
 							c.vy = 0;
 							c.y = wall.getTopY() - c.getHeight();
 							c.onLadder = null;
 							canFall = false;
 							if (!c.canMove) {
 								c.vx = 0;
 								c.canMove = true;
 							}
 						}
 					}
 
 					if (c.getTop().intersects(wall.getBot())) {
 						if (c.vy < 0) {
 							c.vy = 0;
 							c.y = wall.getBotY() + 2;
 						}
 					}
 				}
 
 				if (c.onLadder != null)
 					canFall = false;
 
 				if (c.y <= 0 && c.vy <= 0) {
 					c.vy = 0;
 					c.y = 0;
 				}
 				if (c.y + 200 >= map.getYLimit() && c.vy >= 0) {
 					c.vy = 0;
 					c.y = map.getYLimit() - 200;
 					canFall = false;
 				}
 				if (c.x <= 0 && c.vx <= 0) {
 					c.x = 0;
 					c.vx = 0;
 					if (c.dir < 0)
 						c.dir = 0;
 					if (c.xspeed < 0)
 						c.xspeed = 0;
 				} else if (c.x + 120 >= map.getXLimit() && c.vx >= 0) {
 					c.x = map.getXLimit() - 120;
 					c.vx = 0;
 					if (c.dir > 0)
 						c.dir = 0;
 					if (c.xspeed > 0)
 						c.xspeed = 0;
 				}
 
 				if (canFall)
 					c.fall(timePassed);
 
 				if (c.moveKeys[Network.UP] && c.changedMap && c.currentSkill == -1)
 					for (Spot spot : map.getSpots()) {
 						if (spot.getArea().intersects(c.getArea())) {
 							c.map = spot.getNextMap();
 							c.x = spot.getSpawn().x;
 							c.y = spot.getSpawn().y;
 							c.changedMap = false;
 							ChangedMap update = new ChangedMap();
 							update.id = c.id;
 							update.map = c.map;
 							server.sendToAllTCP(update);
 						}
 					}
 
 				if (!c.moveKeys[Network.UP])
 					c.changedMap = true;
 
 				int i = 0;
 				if (c.moveKeys[Network.UP])
 					i = 1;
 				else if (c.moveKeys[Network.DOWN])
 					i = -1;
 				boolean onladder = false;
 				for (Ladder ladder : map.getLadders())
 					if (ladder != null)
 						if (c.getArea().intersects(ladder)) {
 							if (c.canMove && i != 0)
 								c.onLadder = ladder;
 							onladder = true;
 						}
 
 				if (!onladder)
 					c.onLadder = null;
 				if (c.onLadder != null)
 					c.setClimbing(i);
 			}
 		}
 	}
 
 	public class PassiveSkill {
 
 		public static final int WandMastery = 0, MopMastery = 1, BowMastery = 2;
 
 		public int skill, classe, skillSlot;
 		transient public Character c;
 		public PassiveData passiveData;
 
 		public PassiveSkill(int skillSlot, Character c) {
 			switch (c.classe) {
 			case Character.MAGE:
 				switch (skillSlot) {
 				case 0:
 					skill = WandMastery;
 					break;
 				}
 				break;
 			case Character.ARCHER:
 				switch (skillSlot) {
 				case 0:
 					skill = BowMastery;
 					break;
 				}
 				break;
 			case Character.FIGHTER:
 				switch (skillSlot) {
 				case 0:
 					skill = MopMastery;
 					break;
 				}
 				break;
 			}
 			this.c = c;
 		}
 
 		public int getLvl() {
 			return c.passiveLvls[skillSlot];
 		}
 
 		public void skillStats() {
 			passiveData = new PassiveData(skill, c.passiveLvls[skillSlot]);
 		}
 
 		public int getSkillStat(int i) {
 			return passiveData.statBonus[i];
 		}
 
 		public void removeLvl() {
 			if (getLvl() > 0) {
 				c.skillStats++;
 				c.passiveLvls[skillSlot]--;
 				c.loadStats();
 			}
 		}
 
 		public void addLvl() {
 			if (getLvl() < 10)
 				if (c.skillStats > 0) {
 					c.skillStats--;
 					c.passiveLvls[skillSlot]++;
 					c.loadStats();
 				}
 		}
 
 	}
 
 	public class Projectile {
 		int id;
 		int width, height;
 		String map;
 		float x, y;
 		long timer = 0;
 		float vx, vy;
 		boolean active = true;
 		public Skill skill;
 		public int number = 0, type;
 
 		public Projectile(int type) {
 			this.type = type;
 			switch (type) {
 			case Network.ARROW:
 			case Network.ARROW + 1:
 				width = 100;
 				height = 50;
 				break;
 			case Network.ENERGY:
 				width = 40;
 				height = 38;
 			case Network.FIRE:
 				width = 50;
 				height = 40;
 				break;
 			}
 		}
 
 		public Projectile(int type, Skill skill) {
 			this(type);
 			this.skill = skill;
 		}
 
 		public Projectile(int type, Skill skill, int number) {
 			this(type, skill);
 			this.number = number;
 		}
 
 		public Rectangle getArea() {
 			return new Rectangle((int) x, (int) y, width, height);
 		}
 
 		public boolean isActive() {
 			return active;
 		}
 
 		public void activate() {
 			active = true;
 			timer = 0;
 		}
 
 		public void delete() {
 			active = false;
 		}
 
 		public void update(long timePassed) {
 			x += vx * timePassed;
 			y += vy * timePassed;
 			timer += timePassed;
 			if (timer >= 400)
 				delete();
 
 		}
 
 	}
 
 	public class Skill {
 
 		public static final int ATTACK = 1, Arrow = 2, EnergyBall = 3, Smash = 4, DoubleArrow = 5, ExplosiveArrow = 6, FireBall = 7, MultiHit = 8, Explosion = 9;
 
 		int skill;
 
 		int skillSlot;
 
 		private boolean active = false;
 		private long totalTime = 0;
 		private boolean[] hit = new boolean[6];
 		boolean cLeft;
 		SkillData skillData;
 		private Projectile[] skillProjectiles = new Projectile[8];
 		private int shots = 0;
 		Character c;
 		Effect skillEffect;
 
 		public Skill(Character c, int skillSlot) {
 			this.skillSlot = skillSlot;
 			this.c = c;
 			switch (c.classe) {
 			case Character.MAGE:
 				switch (skillSlot) {
 				case 0:
 					skill = EnergyBall;
 					break;
 				case 1:
 					skill = FireBall;
 					break;
 				case 2:
 					skill = Explosion;
 					break;
 				}
 				break;
 
 			case Character.ARCHER:
 				switch (skillSlot) {
 				case 0:
 					skill = Arrow;
 					break;
 				case 1:
 					skill = DoubleArrow;
 					break;
 				case 2:
 					skill = ExplosiveArrow;
 					break;
 				}
 				break;
 			case Character.FIGHTER:
 				switch (skillSlot) {
 				case 0:
 					skill = ATTACK;
 					break;
 				case 1:
 					skill = MultiHit;
 					break;
 				case 2:
 					skill = Smash;
 					break;
 				}
 				break;
 			}
 			skillStats();
 		}
 
 		public void skillStats() {
 			skillData = new SkillData(skill, c.skillLvls[skillSlot]);
 		}
 
 		public int getMaxHits() {
 			for (int i = 0; i < hit.length; i++) {
 				if (skillData.hitTime[i] == 0)
 					return i;
 			}
 			return 0;
 		}
 
 		public synchronized void addLvl() {
 			if (getLvl() < 10)
 				if (c.skillStats > 0) {
 					c.skillStats--;
 					c.skillLvls[skillSlot]++;
 					skillStats();
 				}
 		}
 
 		public void removeLvl() {
 			if (getLvl() > 0) {
 				c.skillStats++;
 				c.skillLvls[skillSlot]--;
 				skillStats();
 			}
 		}
 
 		public int getLvl() {
 			return c.skillLvls[skillSlot];
 		}
 
 		// update le sort
 		public void update(long timePassed) {
 			totalTime += timePassed;
 			updateSkill();
 			if (skillData.skillTime < totalTime) {
 				active = false;
 			}
 		}
 
 		// retourne si le sort est encore actif
 		public boolean isActive() {
 			return active;
 		}
 
 		// update le sort selon son numro
 		public void updateSkill() {
 			switch (skill) {
 			default:
 				attack();
 				break;
 
 			case Arrow:
 			case DoubleArrow:
 			case ExplosiveArrow:
 			case EnergyBall:
 			case FireBall:
 				Projectiles();
 				break;
 			}
 
 		}
 
 		public void Projectiles() {
 			for (int i = 0; i < hit.length; i++) {
 
 				if (skillData.hitTime[i] != 0)
 					if (totalTime >= skillData.hitTime[i] && shots == i) {
 						skillProjectiles[i] = null;
 						skillProjectiles[i] = new Projectile(projectileType(), this, i);
 						skillProjectiles[i].y = getY() + getHeight() / 2;
 						if (cLeft) {
 							skillProjectiles[i].x = getX() + getWidth() / 3 - skillProjectiles[i].width;
 							skillProjectiles[i].vx = -1;
 						} else {
 							skillProjectiles[i].x = getX() + 2 * getWidth() / 3;
 							skillProjectiles[i].vx = 1;
 						}
 						Point target = getTarget(getAimArea(), new Point(c.getX() + c.getWidth() / 2, c.getY() + c.getHeight() / 2));
 						if (target != null)
 							skillProjectiles[i].vy = (float) ((target.getY() - (skillProjectiles[i].y + skillProjectiles[i].height)) / Math.abs(target.getX()
 									- (skillProjectiles[i].x + skillProjectiles[i].width / 2)));
 						if (skillProjectiles[i].vy > 0.6f)
 							skillProjectiles[i].vy = 0.6f;
 						if (skillProjectiles[i].vy < -0.6f)
 							skillProjectiles[i].vy = -0.6f;
 						skillProjectiles[i].map = c.map;
 						skillProjectiles[i].activate();
 						add(skillProjectiles[i]);
 						shots++;
 					}
 			}
 		}
 
 		public Point getTarget(Rectangle area, Point start) {
 			Vector<Monster> monsters = new Vector<Monster>(maps.get(loadedMaps.indexOf(c.map)).getMonsters());
 			Vector<Point> targets = new Vector<Point>();
 			double lastDistance = 1000;
 			Point target = null;
 			for (Monster monster : monsters) {
 				if (monster.isAlive() && monster.getArea().intersects(area))
 					targets.add(new Point(monster.getX() + monster.width / 2, monster.getY() + monster.height / 2));
 			}
 
 			for (Point p : targets) {
 				if (start.distance(p) < lastDistance) {
 					lastDistance = start.distance(p);
 					target = p;
 				}
 			}
 
 			return target;
 		}
 
 		public int projectileType() {
 			switch (skill) {
 			case ExplosiveArrow:
 			case Arrow:
 			case DoubleArrow:
 				if (cLeft)
 					return Network.ARROW;
 				else
 					return Network.ARROW + 1;
 			case EnergyBall:
 				return Network.ENERGY;
 			case FireBall:
 				return Network.FIRE;
 			default:
 				return 0;
 			}
 
 		}
 
 		// retourne le multiplicateur de dgat du sort
 		public float getDmgMult(int hit) {
 			return skillData.dmgMult[hit];
 		}
 
 		// retourne la vitesse de recul du monstre lorsqu'il est frapp par le
 		// sort
 		public float getKBSpeed(int hit) {
 			return skillData.KBSpeed[hit];
 		}
 
 		// retourne le nombre de monstres que le sort peut tapper
 		public int getMaxEnemiesHit() {
 			return skillData.maxEnemiesHit;
 		}
 
 		// attaque de base
 		public void attack() {
 
 			for (int i = 0; i < hit.length; i++) {
 				if (skillData.hitTime[i] != 0)
 					if ((skillData.hitTime[i] <= totalTime) && (hit[i] == false)) {
 						hit[i] = true;
 						hit(i);
 					}
 			}
 		}
 
 		// retourne la zone du sort
 		public Rectangle getArea() {
 			switch (skill) {
 			case ExplosiveArrow:
 			case Explosion:
 				return skillEffect.getArea();
 			default:
 				if (cLeft)
 					return (new Rectangle(getX() + 10, getY(), getWidth() - 70, getHeight()));
 				else
 					return new Rectangle(getX() + 60, getY(), getWidth() - 70, getHeight());
 			}
 		}
 
 		public Rectangle getAimArea() {
 			switch (skill) {
 			default:
 				if (cLeft) {
 					return new Rectangle(getX() - 450, getY() - 20, 450, getHeight() + 50);
 				} else {
 					return new Rectangle(getX() + getWidth(), getY() - 20, 450, getHeight() + 50);
 				}
 			}
 		}
 
 		public int getX() {
 			if (cLeft)
 				return c.getX() - getWidth() + c.getWidth();
 			else
 				return c.getX();
 		}
 
 		public int getY() {
 			return c.getY() - c.getHeight() + getHeight();
 		}
 
 		public int getHeight() {
 			return skillData.height;
 		}
 
 		public int getWidth() {
 			return skillData.width;
 		}
 
 		// active le sort si le personnage  assez de mana
 		public void activate() {
 			int mana = skillData.manaUsed;
 			if (mana < 0)
 				mana = 0;
 
 			if (mana <= c.mana) {
 				c.currentSkill = skillSlot;
 				c.mana -= mana;
 				cLeft = c.facingLeft;
 				totalTime = 0;
 				for (int i = 0; i < hit.length; i++)
 					hit[i] = false;
 				shots = 0;
 				active = true;
 
 				skillEffects();
 				UpdateSkill update = new UpdateSkill();
 				update.skill = this.skill;
 				update.id = c.id;
 				update.facingLeft = cLeft;
 				sendToAllOnMap(c.map, update, false);
 			} else {
 				c.setSkill(0);
 			}
 
 		}
 
 		private void skillEffects() {
 			switch (skill) {
 			case Explosion:
 				skillEffect = new Effect(new Point(c.getX() + c.getWidth() / 2 - 250, c.getY() - 80), Network.EXPLOSION, 500, c);
 				skillEffect.map = c.map;
 				add(skillEffect);
 				break;
 			}
 		}
 
 		public void hit(int hit) {
 			int hits = 1;
 			for (int i = 0; i < maps.get(loadedMaps.indexOf(c.map)).getMonsters().size() && hits <= getMaxEnemiesHit(); i++) {
 				Monster m = maps.get(loadedMaps.indexOf(c.map)).getMonsters().get(i);
 				if (hitMonster(m, hit))
 					hits++;
 				if (m.summonType != -1)
 					for (Monster summon : m.summons)
 						if (hitMonster(summon, hit))
 							hits++;
 			}
 
 		}
 
 		public boolean hitMonster(Monster m, int hit) {
 			if (m.isAlive())
 				if (m.getArea().intersects(getArea())) {
 					damageMonster(c, m, c.getDamage(m, getDmgMult(hit)), getKBSpeed(hit));
 
 					if (skillData.manaUsed < 0) {
 						int manaU = skillData.manaUsed * (c.atts[Network.SPIRIT] + 100) / 100;
 						if (c.mana - manaU > c.maxMana)
 							c.mana = c.maxMana;
 						else
 							c.mana -= manaU;
 					}
 					return true;
 				}
 			return false;
 		}
 
 	}
 
 	public void drop(Monster m, String map) {
 		Random rand = new Random();
 		int rarity = rand.nextInt(100);
 
 		Vector<Character> charlist = new Vector<Character>();
 		for (int i = 0; i < loggedIn.size(); i++)
 			if (loggedIn.get(i).map.equals(map))
 				if (new Point((int) loggedIn.get(i).x, (int) loggedIn.get(i).y).distance(new Point((int) m.x, (int) m.y)) <= 700)
 					charlist.add(loggedIn.get(i));
 		if (charlist.size() <= 0)
 			for (int i = 0; i < loggedIn.size(); i++)
 				if (loggedIn.get(i).map.equals(map))
 					charlist.add(loggedIn.get(i));
 
 		Character c = charlist.get(rand.nextInt(charlist.size()));
 
 		if ((rand.nextInt(100)) < m.getdropchance() * (100 + c.getStat(Network.IF)) / 100) {
 			int id = c.id;
 
 			if (rarity < m.getrarechance() * (c.getStat(Network.RF) + 100) / 100)
 				rarity = Item.RARE;
 			else if (rarity < (int) (((m.getrarechance() * (c.getStat(Network.RF) + 100) / 100) * 4.5)))
 				rarity = Item.MAGIC;
 			else
 				rarity = Item.COMMON;
 
 			int itemChoices = 8;
 			if (rarity == Item.COMMON)
 				itemChoices = 6;
 
 			int dropLvl = rand.nextInt(10);
 			if (dropLvl <= 3)
 				dropLvl = m.getLevel();
 			else if (dropLvl <= 5)
 				dropLvl = m.getLevel() + 1;
 			else
 				dropLvl = m.getLevel() - 1;
 
 			if (dropLvl < 1)
 				dropLvl = 1;
 
 			add(new Drop(new Item(dropLvl, rand.nextInt(itemChoices), rarity), new Point(m.getX() + rand.nextInt(m.getWidth() >= 50 ? m.getWidth() - 50 : 1),
 					m.getY() + m.getHeight() - 50), id), map);
 		}
 
 	}
 
 	public synchronized void add(Drop drop, String map) {
 		drops.get(map).add(drop);
 		drop.id = drops.get(map).size() - 1;
 		AddDrop update = new AddDrop();
 		update.id = drop.id;
 		update.rarity = drop.item.getRarity();
 		update.type = drop.item.getSlot();
 		update.x = (int) drop.x;
 		update.y = (int) drop.y;
 		sendToAllOnMap(map, update, false);
 	}
 
 	public void damageMonster(Character c, Monster m, int dmg, float speed) {
 
 		if (m.isAlive()) {
 			boolean crit = false;
 
 			if (dmg < 1)
 				dmg = 1;
 
 			Random rand = new Random(System.currentTimeMillis());
 			if ((rand.nextInt(100)) < c.critChance) {
 				dmg = dmg * (c.critDamage + 100) / 100;
 				crit = true;
 			}
 
 			if (m.avoid + m.getLevel() - c.lvl - c.atts[Network.AGI] > 0)
 				if (rand.nextInt(100) < m.avoid + m.getLevel() - c.lvl - c.atts[Network.AGI])
 					dmg = 0;
 
 			if (dmg > 0) {
 				m.life -= dmg;
 				if (m.life <= 0) {
 					m.die();
 					for (int j = 0; j < loggedIn.size(); j++) {
 						if (loggedIn.get(j).map.equals(c.map)) {
 							if (loggedIn.get(j).lvl <= m.getLevel() + 5 && loggedIn.get(j).lvl > m.getLevel() - 10) {
 
 								if (loggedIn.get(j).lvl > m.getLevel())
 									loggedIn.get(j).exp += m.getExp() * (1 - 0.15f * (loggedIn.get(j).lvl - m.getLevel()));
 								else
 									loggedIn.get(j).exp += m.getExp();
 
 								if (c.exp >= Character.expToLvl(c.lvl)) {
 									c.lvlup();
 									add(new Effect(new Point(c.getX() - 40, c.getY() - 100), Network.LVLUP, 1260, c));
 									Stats update = new Stats();
 									update.atts = c.atts;
 									update.attStats = c.attStats;
 									server.sendToTCP(c.connection, update);
 
 									Skills updates = new Skills();
 									updates.skillLvls = c.skillLvls;
 									updates.passiveLvls = c.passiveLvls;
 									updates.skillStats = c.skillStats;
 									server.sendToTCP(c.connection, updates);
 								}
 
 								AddExp exp = new AddExp();
 								exp.exp = loggedIn.get(j).exp;
 								exp.lvl = loggedIn.get(j).lvl;
 								server.sendToTCP(loggedIn.get(j).connection, exp);
 							}
 						}
 					}
 
 					for (int j = 0; j < m.getdropamount(); j++)
 						drop(m, c.map);
 				}
 			}
 
 			DamageText update = new DamageText();
 			update.dmg = dmg;
 			update.crit = crit;
 			update.x = m.getX() + m.width / 3;
 			update.y = m.getY() + m.height / 2;
 			sendToAllOnMap(c.map, update, false);
 
 			m.isAggro = c;
 			m.aggroTimer = 5000;
 
 			if (dmg > m.getMaxLife() / 50 && m.canMove && m.eliteT != Monster.SLOW) {
 				m.canMove = false;
 				m.cantMoveTime = (long) (105 + speed * 1100);
 				m.vy = -(speed * 3);
 				if ((c.x + c.getWidth() / 2) > (m.x + m.getWidth() / 2))
 					speed = -speed;
 				m.vx = speed;
 			} else {
 				m.vx = m.vx * 1.01f;
 			}
 		}
 	}
 
 	public class Effect {
 		public int x, y;
 		String map;
 		private boolean active;
 		private int previousCX, previousCY;
 		int type;
 		Character c;
 
 		public Effect(Point p, int type, long totalTime, String map) {
 			this.type = type;
 			x = p.x;
 			y = p.y;
 			this.map = map;
 			active = true;
 
 		}
 
 		public Effect(Point p, int type, long totalTime, Character c) {
 			this.type = type;
 			this.c = c;
 			x = p.x;
 			y = p.y;
 			active = true;
 			map = c.map;
 			previousCX = c.getX();
 			previousCY = c.getY();
 
 		}
 
 		public void update(long timePassed) {
 
 			if (c != null) {
 				x += c.getX() - previousCX;
 				previousCX = c.getX();
 				y += c.getY() - previousCY;
 				previousCY = c.getY();
 			}
 
 		}
 
 		public Rectangle getArea() {
 			return new Rectangle(x, y, getWidth(), getHeight());
 		}
 
 		public int getWidth() {
 			switch (type) {
 			case Network.EXPLOSION:
 				return 500;
 			case Network.EXPLOARROW:
 				return 200;
 			}
 			return 0;
 		}
 
 		public int getHeight() {
 			switch (type) {
 			case Network.EXPLOSION:
 				return 300;
 			case Network.EXPLOARROW:
 				return 100;
 			}
 			return 0;
 		}
 
 		public boolean isActive() {
 			return active;
 		}
 
 	}
 
 	@Override
 	public void windowActivated(WindowEvent arg0) {
 	}
 
 	@Override
 	public void windowClosed(WindowEvent arg0) {
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		for (int i = 0; i < loggedIn.size(); i++) {
 			saveCharacter(loggedIn.get(i));
 		}
 		server.close();
 		System.exit(0);
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent arg0) {
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent arg0) {
 	}
 
 	@Override
 	public void windowIconified(WindowEvent arg0) {
 	}
 
 	@Override
 	public void windowOpened(WindowEvent arg0) {
 	}
 }
