 package com.mbl111.ld26;
 
 import java.applet.Applet;
 import java.awt.BorderLayout;
import java.awt.Dimension;
 
 public class GameApplet extends Applet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4358947612137457039L;
 	public Game game = new Game();
 
 	@Override
 	public void start() {
 		game.start();
		setLayout(new BorderLayout());
		setSize(Game.WIDTH * Game.SCALE, Game.HEIGHT * Game.SCALE);
		setMaximumSize(new Dimension(Game.WIDTH * Game.SCALE, Game.HEIGHT * Game.SCALE));
		add(game);
 		super.start();
 	}
 
 	@Override
 	public void stop() {
 		super.stop();
 	}
 
 	public void init() {

 	};
 
 }
