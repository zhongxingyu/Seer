 package com.github.joakimpersson.tda367.gui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import com.github.joakimpersson.tda367.model.PyromaniacModel;
 import com.github.joakimpersson.tda367.model.IPyromaniacModel;
 import com.github.joakimpersson.tda367.model.player.Player;
 
 /**
 * An container view for PlayerInfoviews
  * 
  * @author joakimpersson
  * 
  */
 public class PlayerInfoContainerView implements IView {
 	private int startX;
 	private int startY;
	private int panelWidth;
	private int panelHeight;
 	private IPyromaniacModel model = null;
 	private List<PlayerInfoView> playersInfo = null;
 
 	/**
 	 * Create a new container view for holding playerinfoviews
 	 * 
 	 * @param startX
 	 *            The starting coordinate in the x-axis
 	 * @param startY
 	 *            The starting coordinate in the y-axis
 	 */
 	public PlayerInfoContainerView(int startX, int startY) {
 		this.startX = startX;
 		this.startY = startY;
		this.panelWidth = 200;
		this.panelHeight = 102;
 		init();
 	}
 
 	/**
 	 * Responsible for fetching instances ,info from the model and init fonts
 	 * etc
 	 */
 	private void init() {
 		model = PyromaniacModel.getInstance();
 	}
 
 	@Override
 	public void enter() {
 		playersInfo = new ArrayList<PlayerInfoView>();
 		List<Player> players = model.getPlayers();
 		int x = 0;
 		int y = 0;
 		for (Player p : players) {
 			PlayerInfoView view = new PlayerInfoView(p, x, y, panelWidth,
 					panelHeight);
 			playersInfo.add(view);
 			y += panelHeight;
 		}
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 		g.setColor(Color.black);
 		g.fillRect(startX, startY, panelWidth, container.getHeight());
 		for (PlayerInfoView view : playersInfo) {
 			view.render(container, g);
 		}
 	}
 }
