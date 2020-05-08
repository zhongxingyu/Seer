 package propinquity;
 
 import java.io.File;
 import java.util.Vector;
 
 import org.jbox2d.common.Vec2;
 
 import processing.core.*;
 import processing.xml.*;
 
 public class LevelSelect implements PConstants, UIElement {
 
 	final String LEVEL_FOLDER = "levels/";
 
 	Propinquity parent;
 
 	Sounds sounds;
 
 	Hud hud;
 	Particle[] particles;
 
 	String[] names;
 
 	String[] playerNames;
 	Player[] players;
 
 	String[] levelFiles;
 	Level[] levels;
 	Level currentLevel;
 
 	int state, selected;
 
 	boolean isVisible;
 
 	public LevelSelect(Propinquity parent, Hud hud, Player[] players, Sounds sounds) {
 		this.parent = parent;
 		this.hud = hud;
 		this.players = players;
 		this.sounds = sounds;
 
 		isVisible = true;
 
 		levelFiles = listFileNames(parent.dataPath(LEVEL_FOLDER), "xml");
 		// load each level to know the song name and duration
 		Vector<Level> tmp_levels = new Vector<Level>();
 		for (int i = 0; i < levelFiles.length; i++) {
 			try {
 				tmp_levels.add(new Level(parent, LEVEL_FOLDER+levelFiles[i], players, sounds));
 			} catch(XMLException e) {
 				System.out.println("Level not built for file \""+levelFiles[i]+"\" because of the following XMLException");
 				System.out.println(e.getMessage());
 			}
 		}
 
 		levels = tmp_levels.toArray(new Level[0]);
 	}
 
 	// This function returns all the files in a directory as an array of Strings
 	String[] listFileNames(String dir, String ext) {
 		File file = new File(dir);
 		if (file.isDirectory()) {
 			String names[] = file.list();
 			if (ext == null)
 				return names;
 
 			// if extension is specify, parse out the rest
 			Vector<String> parsedNames = new Vector<String>();
 			for (int i = 0; i < names.length; i++) {
 				if (names[i].lastIndexOf("." + ext) == names[i].length() - 4)
 					parsedNames.add(names[i]);
 			}
 
 			String[] namesWithExt = new String[parsedNames.size()];
 			for (int i = 0; i < namesWithExt.length; i++)
 				namesWithExt[i] = parsedNames.get(i);
 
 			return namesWithExt;
 		} else {
 			// If it's not a directory
 			return null;
 		}
 	}
 
 	public Level getCurrentLevel() {
 		return currentLevel;
 	}
 
 	public void setPlayerNames(String[] playerNames) {
 		this.playerNames = playerNames;
 	}
 
 	public void reset() {
 		for (Player player : players)
 			player.setName(null);
 		stateChange(0);
 	}
 
 	void stateChange(int state) {
 		this.state = state;
 		selected = 0;
 
 		killParticles();
 
 		if (state < playerNames.length) {
 			while (nameTaken(playerNames[selected]))
 				selected = (selected + 1) % playerNames.length;
 			createParticles(playerNames.length, players[state].getColor());
 		} else if (state == playerNames.length) {
 			createParticles(levels.length, PlayerConstants.NEUTRAL_COLOR);
 		} else if (state == playerNames.length + 1) {
 			parent.changeGameState(GameState.Play);
 		} else {
 			System.out.println("Unknown Level Select State");
 		}
 	}
 
 	void createParticles(int num, Color color) {
 		int radius = parent.height / 2 - Hud.WIDTH * 2;
 
 		particles = new Particle[num];
 
 		for (int i = 0; i < num; i++) {
 			Particle p = new Particle(parent, new Vec2(PApplet.cos(PApplet.TWO_PI / particles.length * i) * radius,
 					PApplet.sin(PApplet.TWO_PI / particles.length * i) * radius), color, true);
 			p.scale = 1f;
 			particles[i] = p;
 		}
 	}
 
 	void killParticles() {
 		if (particles == null)
 			return;
 		for (Particle particle : particles)
 			particle.kill();
 	}
 
 	public void show() {
 		isVisible = true;
 	}
 
 	public void hide() {
 		isVisible = false;
 	}
 
 	public boolean isVisible() {
 		return isVisible;
 	}
 
 	public void draw() {
 		if (!isVisible)
 			return;
 
 		hud.drawInnerBoundary();
 		hud.drawOuterBoundary();
 		
 		drawParticles();
 
 		if (state < playerNames.length) {
 			hud.drawCenterText("Select Player " + (state + 1));
 			hud.drawBannerCenter(playerNames[selected], players[state].getColor(), PApplet.TWO_PI / playerNames.length
 					* selected);
 		} else if (state == playerNames.length) {
 			hud.drawCenterText("Select Song");
 			hud.drawBannerCenter(levels[selected].getName(), PlayerConstants.NEUTRAL_COLOR, PApplet.TWO_PI/levels.length*selected);
 		}
 
 	}
 
 	void drawParticles() {
 		if(particles == null) return;
 
 		parent.pushMatrix();
 		parent.translate(parent.width / 2, parent.height / 2);
 		
 		for (int i = 0; i < particles.length; i++) {
 			particles[i].draw();
 		}
 		
 		parent.popMatrix();
 	}
 
 	/**
 	 * Receive a keyPressed event.
 	 * 
 	 * @param key the char of the keyPressed event.
 	 * @param keycode the keycode of the keyPressed event.
 	 */
 	public void keyPressed(char key, int keycode) {
 		switch (keycode) {
 		case BACKSPACE: {
 			if (state == 0)
 				parent.changeGameState(GameState.PlayerList);
 			reset();
 			break;
 		}
 		case LEFT: {
 			left();
 			break;
 		}
 		case RIGHT: {
 			right();
 			break;
 		}
 		case ENTER:
 		case ' ': {
 			select();
 			break;
 		}
 		}
 	}
 
 	boolean nameTaken(String name) {
 		for (Player player : players) {
 			if (player.getName() == playerNames[selected])
 				return true; // Use == not .equals
 		}
 		return false;
 	}
 
 	public void left() {
 		if (state < playerNames.length) {
 			do {
 				selected = (selected + playerNames.length - 1) % playerNames.length;
 			} while (nameTaken(playerNames[selected]));
 		} else if (state == playerNames.length) {
 			selected = (selected + levels.length - 1) % levels.length;
 		}
 	}
 
 	public void right() {
 		if (state < playerNames.length) {
 			do {
 				selected = (selected + 1) % playerNames.length;
 			} while (nameTaken(playerNames[selected]));
 		} else if (state == playerNames.length) {
 			selected = (selected + 1) % levels.length;
 		}
 	}
 
 	public void select() {
 		if (state < playerNames.length) {
 			players[state].setName(playerNames[selected]);
 		} else if (state == playerNames.length) {
 			currentLevel = levels[selected];
 		}
 
 		stateChange(state + 1);
 	}
 }
