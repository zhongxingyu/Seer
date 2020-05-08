 package game.client.Game;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import game.client.Entity.Player;
 import game.client.Entity.Character;
 import game.client.Entity.ProjectileSpell;
 import game.client.Entity.Spell;
 import game.client.Login.LoginScreen;
 import game.client.Map.Map;
 import game.client.Resource.ResourceManager;
 import game.util.IO.InputState;
 import game.util.IO.Net.GameClientListeners;
 import game.util.IO.Net.Network;
 import game.util.IO.Net.Network.CharacterInfo;
 import game.util.IO.Net.Network.GameServerInfo;
 import game.util.IO.Net.Network.PlayerInfo;
 import game.util.IO.Net.Network.UpdatePlayer;
 import game.util.UI.SpellButton;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.Game;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 import com.esotericsoftware.kryonet.Client;
 
 public class MainGame implements Game {
 
 	public AppGameContainer apc;
 	private LoginScreen ls;
 	private String playerID;
 	private GameServerInfo gsi;
 	public static Player player;
 	public static java.util.Map<String, Character> players;
 	public static java.util.Set<Spell> spells;
 	private ProjectileSpell s;
 	private SpellButton spellbtn;
 	public static Map map;
 	public static Client client;
 	public MainGame() {
 		ls = new LoginScreen();
 	}
 	
 	@Override
 	public boolean closeRequested() {
 		if (ls != null)
 			return true;
 		if (client.isConnected())
 			client.stop();
 		return true;
 	}
 
 	@Override
 	public String getTitle() {
 		if (ls != null)
 			return ls.getTitle();
 		return "MainGameWindow";
 	}
 	
 	@Override
 	public void init(GameContainer container) throws SlickException {
 		if (ls != null) {
 			ls.init(container);
 			System.out.println("LoginScreen Init");
 			return;
 		}
 		System.out.println("Main Game Init");
 		ResourceManager.Manager().init();
 		CharacterInfo ci = new CharacterInfo();
 		ci.speed = 128;
 		ci.imageID = "GameAssets:Player:player.bmp";
 		ci.x = 62;
 		ci.y = 62;
 		PlayerInfo pi = new PlayerInfo();
 		pi.characterInfo = ci;
 		pi.player = playerID;
 		player = new Player(pi);
 		map = ResourceManager.Manager().getMap("bonnyMap2:testmap.tmx");
 		players = Collections.synchronizedMap(new HashMap<String, Character>());
 		spells = Collections.synchronizedSet(new HashSet<Spell>());
 		s = new ProjectileSpell();
 		s.setSpeed(512.0f);
 		spellbtn = new SpellButton(s);
 		spellbtn.key = Input.KEY_1;
 		client = new Client();
 		client.start();
 		Network.register(client);
 		GameClientListeners.createListeners();
 		try {
 			client.connect(5000, gsi.ip, gsi.port, gsi.port + 1);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		if (ls != null) {
 			ls.render(container, g);
 			return;
 		}
 		map.drawBG(player.getPlayerX() - GameInfo.Width / 2, player.getPlayerY() - GameInfo.Height / 2, GameInfo.Width, GameInfo.Height);
 		synchronized (players) {
 			for (Character c : players.values()) {
 				c.draw(player.getPlayerX(), player.getPlayerY());
 			}
 		}
 		synchronized (spells) {
 			for (Spell s : spells) {
 				s.draw(player.getPlayerX(), player.getPlayerY());
 			}
 		}
 		player.Draw();
 		map.drawFG(player.getPlayerX() - GameInfo.Width / 2, player.getPlayerY() - GameInfo.Height / 2, GameInfo.Width, GameInfo.Height);
 	}
 	long time = System.currentTimeMillis();
 	boolean ensureStop = true;
 	@Override
 	public void update(GameContainer container, int delta)
 			throws SlickException {
 		if (ls != null) {
 			ls.update(container, delta);
 			if (ls.gsi != null) {
 				playerID = ls.un;
 				gsi = ls.gsi;
 				ls = null;
 				apc.setDisplayMode(GameInfo.Width, GameInfo.Height, false);
 				container.reinit();
 			}
 			return;
 		}
 		InputState.Update(container);
 		
 		spellbtn.update(container);
 		s.update(delta);
 
 		synchronized (players) {
 			for (Character c : players.values()) {
 				c.update(delta);
 			}
 		}
 		synchronized (spells) {
 			Iterator<Spell> it = spells.iterator();
 			while (it.hasNext()) {
 				Spell s = it.next();
 				s.update(delta);
 				if (s.isDead())
 					it.remove();
 			}
 		}
 		
 		player.update(delta);
 		if (System.currentTimeMillis() - time > 100) {
 			if (player.hasChanged()) {
 				ensureStop = true;
 				UpdatePlayer up = new UpdatePlayer();
 				up.playerInfo = player.getPlayerInfo();
 				client.sendUDP(up);
 			} else if (ensureStop) {
 				ensureStop = false;
 				UpdatePlayer up = new UpdatePlayer();
 				up.playerInfo = player.getPlayerInfo();
 				client.sendTCP(up);
 			} 
 		}
 	}
 }
