 package gfx;
 
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import blueMaggot.Game;
 
 public class MenuOptions extends Menu {
 	private GridBagConstraints c = new GridBagConstraints();
 	private Dimension btnSize = new Dimension(200, 20);
 
 	private MenuField fieldPlayerTwo;
 	private MenuField fieldPlayerOne;
 	private MenuField fieldIp;
 
 	private MenuButton btnApply;
 	private MenuButton btnReturn;
 
 	private MenuLabel playerOne;
 	private MenuLabel playerTwo;
 	private MenuLabel connectIp;
 	private MenuLabel isHost;
 
 	private MenuCheckBox boxIsHost;
 
 	public MenuOptions(Game game) {
 		super();
 
		// setVisible(false);
 		super.border = 5;
 
 		fieldIp = new MenuField(20);
 		fieldPlayerOne = new MenuField(20);
 		fieldPlayerTwo = new MenuField(20);
 
 		btnApply = new MenuButton("apply", this, game, btnSize);
 		btnReturn = new MenuButton("return", this, game, btnSize);
 
 		playerOne = new MenuLabel("playerOne");
 		playerTwo = new MenuLabel("playerTwo");
 		connectIp = new MenuLabel("connectIp");
 		isHost = new MenuLabel("isHost");
 
 		boxIsHost = new MenuCheckBox();
 
 		// left column
 		add(playerOne, new GBC(0, 0, "right"));
 		add(playerTwo, new GBC(0, 1, "right"));
 		add(connectIp, new GBC(0, 2, "right"));
 		add(isHost, new GBC(0, 3, "right"));
 		add(boxIsHost, new GBC(1, 3, "left"));
 
 		// right column
 		add(fieldPlayerOne, new GBC(1, 0, "left"));
 		add(fieldPlayerTwo, new GBC(1, 1, "left"));
 		add(fieldIp, new GBC(1, 2, "left"));
 
 		add(btnApply, new GBC(0, 4, "right"));
 		add(btnReturn, new GBC(1, 4, "left"));
 	}
 
 	public void apply(Game game) {
 		game.isHost = boxIsHost.getState();
 		game.nickPlayerOne = fieldPlayerOne.msg;
 		game.nickPlayerTwo = fieldPlayerTwo.msg;
 		game.hostIp = fieldIp.msg;
 	}
 }
