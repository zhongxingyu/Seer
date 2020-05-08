 /*
  * Copyright (c) 2013 Aritzh (Aritz Lopez)
  *
  * This game is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This game is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  * PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this
  * game. If not, see http://www.gnu.org/licenses/.
  */
 
 package aritzh.waywia.core.states;
 
 import aritzh.waywia.core.Game;
 import aritzh.waywia.gui.components.GUI;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * @author Aritz Lopez
  * @license Lesser GNU Public License v3 (http://www.gnu.org/licenses/lgpl.html)
  */
 public abstract class WaywiaState extends BasicGameState {
 
 	protected GUI currGui;
 	protected Game game;
 	protected final String name;
 
 	public WaywiaState(Game game, String name) {
 		this.game = game;
 		this.name = name;
 	}
 
 	public GUI getCurrentGui() {
 		return currGui;
 	}
 
 	public void openGUI(GUI gui) {
 		this.currGui = gui;
 	}
 
 	public boolean isGuiOpen() {
 		return currGui != null;
 	}
 
 	public Game getGame() {
 		return game;
 	}
 
 	@Override
 	public String toString() {
 		return this.name;
 	}
 
 	@Override
 	public final void init(GameContainer container, StateBasedGame game) throws SlickException {
 		this.game = (Game) game;
 		try {
 			this.init();
 		} catch (Throwable t) {
 			this.game.enterState(this.game.errorState.setError("init", t));
 		}
 	}
 
 	@Override
 	public final void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException {
 		try {
			if (!this.isGuiOpen() || this.currGui.hasTransparentBackGround()) this.render(g);
 			if (this.isGuiOpen()) this.currGui.render(g);
 		} catch (Throwable t) {
 			this.game.enterState(this.game.errorState.setError("render", t));
 		}
 	}
 
 	@Override
 	public final void update(GameContainer container, StateBasedGame game, int delta) throws SlickException {
 		try {
 			if (!this.isGuiOpen() || !this.currGui.stopsGame()) this.update(delta);
 		} catch (Throwable t) {
 			this.game.enterState(this.game.errorState.setError("update", t));
 		}
 	}
 
 	public final void closing() {
 		try {
 			this.onClosing();
 		} catch (Throwable t) {
 			this.game.enterState(this.game.errorState.setError("update", t));
 		}
 	}
 
 	public abstract void init();
 
 	public abstract void render(Graphics g);
 
 	public abstract void update(int delta);
 
 	public void onClosing() {
 	}
 
 	// Input handling
 
 	@Override
 	public void mouseClicked(int button, int x, int y, int clickCount) {
 		if (this.isGuiOpen()) this.currGui.mouseClicked(button, x, y, clickCount);
 	}
 
 	@Override
 	public void mouseDragged(int oldx, int oldy, int newx, int newy) {
 		if (this.isGuiOpen()) this.currGui.mouseDragged(oldx, oldy, newx, newy);
 	}
 
 	@Override
 	public void mouseMoved(int oldx, int oldy, int newx, int newy) {
 		if (this.isGuiOpen()) this.currGui.mouseMoved(oldx, oldy, newx, newy);
 	}
 
 	@Override
 	public void mousePressed(int button, int x, int y) {
 		if (this.isGuiOpen()) this.currGui.mousePressed(button, x, y);
 	}
 
 	@Override
 	public void mouseReleased(int button, int x, int y) {
 		if (this.isGuiOpen()) this.currGui.mouseReleased(button, x, y);
 	}
 
 	@Override
 	public void mouseWheelMoved(int newValue) {
 		if (this.isGuiOpen()) this.currGui.mouseWheelMoved(newValue);
 	}
 
 	@Override
 	public void keyPressed(int key, char c) {
 		if (this.isGuiOpen()) this.currGui.keyPressed(key, c);
 	}
 
 	@Override
 	public void keyReleased(int key, char c) {
 		if (this.isGuiOpen()) this.currGui.keyReleased(key, c);
 	}
 }
