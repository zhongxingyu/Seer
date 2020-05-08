 package game;
 
 import bots.BotRepository;
 
 /**
  * A GameDescription just holds (serializable) setup-data about a certain
  * game type.
  * This abstract baseclass just holds the botnames and initial bankroll, 
  * subclasses will provide more info about cash-games our tournament (or
  * other variants). 
  *
  */
 public abstract class AbstractGameDescription {
 
 	private String[] botNames = new String[0];
 	private String[] inGameNames = new String[0];
 	private double initialBankRoll;
 	private boolean nolimit;
 
 	public AbstractGameDescription() {
 		super();
 	}
 
 	public String[] getBotNames() {
 		return botNames;
 	}
 
 	public String[] getInGameNames() {
 		return inGameNames;
 	}
 
 	public double getInitialBankRoll() {
 		return initialBankRoll;
 	}
 
 	public int getNumSeats() {
 		return inGameNames.length;
 	}
 
 	/**
 	 * sets the (technical) names of the bots to play.<br>
 	 * These bots are later retrieved from the {@link BotRepository}<br>
 	 * This method also generates the {@link #setInGameNames(String[])}, if you
 	 * don't like them, call {@link #setInGameNames(String[])} manually
 	 * 
 	 * @param botNames
 	 */
 	public void setBotNames(String[] botNames) {
 		this.botNames = botNames;
 		this.inGameNames = new String[botNames.length];
 		for (int i = 0; i < botNames.length; i++) {
			this.inGameNames[i] = "Agt#" + (i + 1) + " (" + botNames[i] + ")";
 		}
 	}
 
 	public void setInGameNames(String[] inGameNames) {
 		this.inGameNames = inGameNames;
 	}
 
 	public void setInitialBankRoll(double initialBankRoll) {
 		this.initialBankRoll = initialBankRoll;
 	}
 
 	public boolean isNolimit() {
 		return nolimit;
 	}
 
 	public void setNolimit(boolean nolimit) {
 		this.nolimit = nolimit;
 	}
 
 	/**
 	 * factory method to create an implementation of a GameRunner, being able
 	 * to run a game based in this GameDescription
 	 */
 	public abstract GameRunner createGameRunner();
 }
