 /*
  * Created on Mar 26, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package cc.warlock.client.stormfront.internal;
 
 import java.io.File;
 import java.io.IOException;
 
 import cc.warlock.client.ICompass;
 import cc.warlock.client.IProperty;
 import cc.warlock.client.IWarlockStyle;
 import cc.warlock.client.internal.ClientProperty;
 import cc.warlock.client.internal.Compass;
 import cc.warlock.client.internal.WarlockClient;
 import cc.warlock.client.internal.WarlockStyle;
 import cc.warlock.client.stormfront.IStormFrontClient;
 import cc.warlock.configuration.WarlockConfiguration;
 import cc.warlock.configuration.server.ServerSettings;
 import cc.warlock.network.StormFrontConnection;
 import cc.warlock.script.IScriptCommands;
 import cc.warlock.script.internal.ScriptCommands;
 import cc.warlock.script.internal.ScriptRunner;
 import cc.warlock.stormfront.IStormFrontProtocolHandler;
 
 import com.martiansoftware.jsap.CommandLineTokenizer;
 
 /**
  * @author Marshall
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class StormFrontClient extends WarlockClient implements IStormFrontClient {
 
 	protected ICompass compass;
 	protected int lastPrompt;
 	protected ClientProperty<Integer> roundtime, health, mana, fatigue, spirit;
 	protected ClientProperty<String> leftHand, rightHand, currentSpell;
 	protected boolean isPrompting = false;
 	protected StringBuffer buffer = new StringBuffer();
 	protected IStormFrontProtocolHandler handler;
 	protected boolean isBold;
 	protected ClientProperty<String> playerId, characterName;
 	protected IWarlockStyle currentStyle = WarlockStyle.EMPTY_STYLE;
 	protected ServerSettings serverSettings;
 	protected RoundtimeRunnable rtRunnable;
 	protected ScriptCommands scriptCommands;
 	
 	public StormFrontClient() {
 		compass = new Compass(this);
 		leftHand = new ClientProperty<String>(this, "leftHand");
 		rightHand = new ClientProperty<String>(this, "rightHand");
 		currentSpell = new ClientProperty<String>(this, "currentSpell");
 		
 		roundtime = new ClientProperty<Integer>(this, "roundtime");
 		health = new ClientProperty<Integer>(this, "health");
 		mana = new ClientProperty<Integer>(this, "mana");
 		fatigue = new ClientProperty<Integer>(this, "fatigue");
 		spirit = new ClientProperty<Integer>(this, "spirit");
 		playerId = new ClientProperty<String>(this, "playerId");
 		characterName = new ClientProperty<String>(this, "characterName");
 		serverSettings = new ServerSettings(this);
 		rtRunnable = new RoundtimeRunnable();
 		scriptCommands = new ScriptCommands(this);
 	}
 
 	@Override
 	public void send(String command) {
 		if (command.startsWith(".")){
 //			runScriptCommand(command);
 		} else {
 			super.send(command);
 		}
 	}
 	
 	protected  void runScriptCommand(String command) {
 		command = command.substring(1);
		int firstSpace = command.indexOf(" ") - 1;
 		String scriptName = command.substring(0, (firstSpace < 0 ? command.length() : firstSpace));
 		String[] arguments = new String[0];
 		
 		if (firstSpace > 0)
 		{
 			String args = command.substring(firstSpace+1);
 			arguments = CommandLineTokenizer.tokenize(args);
 		}
 		
 		File scriptDirectory = WarlockConfiguration.getConfigurationDirectory("scripts", true);
 		ScriptRunner.runScriptFromFile(scriptCommands, scriptDirectory, scriptName, arguments);
 	}
 	
 	public IProperty<Integer> getRoundtime() {
 		return roundtime;
 	}
 
 	private class RoundtimeRunnable implements Runnable
 	{
 		public int roundtime;
 		
 		public synchronized void run () 
 		{
 			for (int i = 0; i < roundtime; i++)
 			{
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				updateRoundtime(StormFrontClient.this.roundtime.get() - 1);
 			}
 		}
 	}
 	
 	public void startRoundtime (int seconds)
 	{
 		roundtime.activate();
 		roundtime.set(seconds);
 		rtRunnable.roundtime = seconds;
 		
 		new Thread(rtRunnable).start();
 	}
 	
 	public void updateRoundtime (int currentRoundtime)
 	{
 		roundtime.set(currentRoundtime);
 	}
 	
 	public IProperty<Integer> getHealth() {
 		return health;
 	}
 
 	public IProperty<Integer> getMana() {
 		return mana;
 	}
 
 	public IProperty<Integer> getFatigue() {
 		return fatigue;
 	}
 
 	public IProperty<Integer> getSpirit() {
 		return spirit;
 	}
 
 	public ICompass getCompass() {
 		return compass;
 	}
 	
 	public void connect(String server, int port, String key) throws IOException {
 		try {
 			connection = new StormFrontConnection(this, key);
 			connection.connect(server, port);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void streamCleared() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void setPrompting() {
 		isPrompting = true;
 	}
 	
 	public boolean isPrompting() {
 		return isPrompting;
 	}
 	
 	public void setBold(boolean bold) {
 		isBold = bold;
 	}
 	
 	public boolean isBold() {
 		return isBold;
 	}
 
 	public IWarlockStyle getCurrentStyle() {
 		return currentStyle;
 	}
 
 	public void setCurrentStyle(IWarlockStyle currentStyle) {
 		this.currentStyle = currentStyle;
 	}
 
 	public ClientProperty<String> getPlayerId() {
 		return playerId;
 	}
 	
 	public ServerSettings getServerSettings() {
 		return serverSettings;
 	}
 	
 	public IScriptCommands getScriptCommands() {
 		return scriptCommands;
 	}
 	
 	public IProperty<String> getCharacterName() {
 		return characterName;
 	}
 	
 	public IProperty<String> getLeftHand() {
 		return leftHand;
 	}
 	
 	public IProperty<String> getRightHand() {
 		return rightHand;
 	}
 	
 	public IProperty<String> getCurrentSpell() {
 		return currentSpell;
 	}
 }
