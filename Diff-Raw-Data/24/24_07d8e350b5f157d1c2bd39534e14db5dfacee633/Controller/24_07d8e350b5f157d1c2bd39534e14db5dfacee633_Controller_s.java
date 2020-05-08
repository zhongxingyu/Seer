 package controller;
 
 import model.Bot;
 import model.Destructor;
 import model.Flattop;
 import model.Human;
 import model.Rowboat;
 import model.Field.state;
 
 public class Controller {
 
 	private Human player;
 	private Bot bot;
 
 	public Human getPlayer() {
 		return player;
 	}
 
 	public void setPlayer(Human player) {
 		this.player = player;
 	}
 
 	public Bot getBot() {
 		return bot;
 	}
 
 	public void setBot(Bot bot) {
 		this.bot = bot;
 	}
 
 	public Controller(int fieldsize) {
 		player = new Human(fieldsize);
 		bot = new Bot(fieldsize);
 	}
 
 	// Methods for shooting each other via the controller
 	// shoot at bot
 	public void shootBot(int row, int col) {
 		player.shoot(bot.getPlayboard().getField()[row][col]);
		if (bot.getPlayboard().getField()[row][col].getStat() == state.hit) {
			if (bot.getPlayboard().getField()[row][col].getShip().getSize() == 0) {
				bot.setNumberShips(bot.getNumberShips() - 1);
			}
 		}
 	}
 
 	// shoot at human
 	public void shootHuman() {
 		int[] botshot = bot.shoot(player.getPlayboard());
 		if (player.getPlayboard().getField()[botshot[0]][botshot[1]].getShip() != null) {
 			player.setNumberShips(player.getNumberShips() - 1);
 		}
 	}
 
 	// Methods for setting the ships via the controller
 	public void setHumanRowboat(int row, int col, boolean vertikal,
 			boolean horizontal) {
 		player.getPlayboard().setShip(
 				new Rowboat(row, col, vertikal, horizontal));
 		player.setNumberShips(player.getNumberShips() + 1);
 	}
 
 	public void setHumanFlattop(int row, int col, boolean vertikal,
 			boolean horizontal) {
 		player.getPlayboard().setShip(
 				new Flattop(row, col, vertikal, horizontal));
 		player.setNumberShips(player.getNumberShips() + 1);
 	}
 
 	public void setHumanDestructor(int row, int col, boolean vertikal,
 			boolean horizontal) {
 		player.getPlayboard().setShip(
 				new Destructor(row, col, vertikal, horizontal));
 		player.setNumberShips(player.getNumberShips() + 1);
 	}
 
 	// Ships for bot
 	public void setBotRowboat(boolean vertikal, boolean horizontal) {
 		bot.setShip(new Rowboat(vertikal, horizontal));
 		bot.setNumberShips(bot.getNumberShips() + 1);
 	}
 
 	public void setBotFlattop(boolean vertikal, boolean horizontal) {
 		bot.setShip(new Flattop(vertikal, horizontal));
 		bot.setNumberShips(bot.getNumberShips() + 1);
 	}
 
 	public void setBotDestructor(boolean vertikal, boolean horizontal) {
 		bot.setShip(new Destructor(vertikal, horizontal));
 		bot.setNumberShips(bot.getNumberShips() + 1);
 	}
 }
