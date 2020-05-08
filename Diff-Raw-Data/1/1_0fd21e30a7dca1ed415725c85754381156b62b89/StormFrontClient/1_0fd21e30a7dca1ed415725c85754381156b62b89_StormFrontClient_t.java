 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package cc.warlock.core.stormfront.client.internal;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import cc.warlock.core.client.ICharacterStatus;
 import cc.warlock.core.client.ICommand;
 import cc.warlock.core.client.IProperty;
 import cc.warlock.core.client.IRoomListener;
 import cc.warlock.core.client.IStream;
 import cc.warlock.core.client.IWarlockSkin;
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.WarlockClientRegistry;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.internal.CharacterStatus;
 import cc.warlock.core.client.internal.ClientProperty;
 import cc.warlock.core.client.internal.WarlockClient;
 import cc.warlock.core.client.internal.WarlockStyle;
 import cc.warlock.core.client.settings.IClientSettings;
 import cc.warlock.core.configuration.ConfigurationUtil;
 import cc.warlock.core.script.IScript;
 import cc.warlock.core.script.IScriptListener;
 import cc.warlock.core.script.ScriptEngineRegistry;
 import cc.warlock.core.script.configuration.ScriptConfiguration;
 import cc.warlock.core.stormfront.IStormFrontProtocolHandler;
 import cc.warlock.core.stormfront.client.BarStatus;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.network.StormFrontConnection;
 import cc.warlock.core.stormfront.settings.IStormFrontClientSettings;
 import cc.warlock.core.stormfront.settings.StormFrontServerSettings;
 import cc.warlock.core.stormfront.settings.internal.StormFrontClientSettings;
 import cc.warlock.core.stormfront.settings.skin.DefaultSkin;
 import cc.warlock.core.stormfront.settings.skin.IStormFrontSkin;
 import cc.warlock.core.stormfront.xml.StormFrontDocument;
 import cc.warlock.core.stormfront.xml.StormFrontElement;
 
 import com.martiansoftware.jsap.CommandLineTokenizer;
 
 /**
  * @author Marshall
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class StormFrontClient extends WarlockClient implements IStormFrontClient, IScriptListener, IRoomListener {
 
 	protected ICharacterStatus status;
 	protected ClientProperty<Integer> roundtime, monsterCount;
 	protected ClientProperty<BarStatus> health, mana, fatigue, spirit;
 	protected ClientProperty<String> leftHand, rightHand, currentSpell;
 	protected StringBuffer buffer = new StringBuffer();
 	protected IStormFrontProtocolHandler handler;
 	protected ClientProperty<String> playerId, characterName, gameCode, roomDescription;
 	protected StormFrontClientSettings clientSettings;
 	protected StormFrontServerSettings serverSettings;
 	protected long timeDelta;
 	protected Integer roundtimeEnd;
 	protected Thread roundtimeThread = null;
 	protected ArrayList<IScript> runningScripts;
 	protected ArrayList<IScriptListener> scriptListeners;
 	protected DefaultSkin skin;
 	protected HashMap<String, ClientProperty<String>> components = new HashMap<String, ClientProperty<String>>();
 	protected HashMap<String, IStream> componentStreams = new HashMap<String, IStream>();
 	protected ClientProperty<GameMode> mode;
 	protected HashMap<String, String> commands;
 	
 	public StormFrontClient() {
 		super();
 		
 		status = new CharacterStatus(this);
 		leftHand = new ClientProperty<String>(this, "leftHand", null);
 		rightHand = new ClientProperty<String>(this, "rightHand", null);
 		currentSpell = new ClientProperty<String>(this, "currentSpell", null);
 		
 		roundtime = new ClientProperty<Integer>(this, "roundtime", 0);
 		health = new ClientProperty<BarStatus>(this, "health", null);
 		mana = new ClientProperty<BarStatus>(this, "mana", null);
 		fatigue = new ClientProperty<BarStatus>(this, "fatigue", null);
 		spirit = new ClientProperty<BarStatus>(this, "spirit", null);
 		playerId = new ClientProperty<String>(this, "playerId", null);
 		characterName = new ClientProperty<String>(this, "characterName", null);
		gameCode = new ClientProperty<String>(this, "gameCode", null);
 		roomDescription = new ClientProperty<String>(this, "roomDescription", null);
 		monsterCount = new ClientProperty<Integer>(this, "monsterCount", null);
 		mode = new ClientProperty<GameMode>(this, "gameMode", GameMode.Game);
 
 		roundtimeEnd = null;
 		runningScripts = new ArrayList<IScript>();
 		scriptListeners = new ArrayList<IScriptListener>();
 		
 		WarlockClientRegistry.activateClient(this);
 	}
 	
 	@Override
 	protected IClientSettings createClientSettings() {
 		clientSettings = new StormFrontClientSettings(this);
 
 		skin = new DefaultSkin(clientSettings);
 		skin.loadDefaultStyles(clientSettings.getHighlightConfigurationProvider());
 		
 		serverSettings = new StormFrontServerSettings();
 		clientSettings.addChildProvider(serverSettings);
 		
 		return clientSettings;
 	}
 	
 	@Override
 	public void send(ICommand command) {
 		String scriptPrefix = ScriptConfiguration.instance().getScriptPrefix();
 		
 		if (command.getCommand().startsWith(scriptPrefix)){
 			runScript(command.getCommand().substring(scriptPrefix.length()));
 		} else {
 			super.send(command);
 		}
 	}
 	
 	public void runScript(String command) {
 		command = command.replaceAll("[\\r\\n]", "");
 		
 		int firstSpace = command.indexOf(" ");
 		String scriptName = command.substring(0, (firstSpace < 0 ? command.length() : firstSpace));
 		String[] arguments = new String[0];
 		
 		if (firstSpace > 0)
 		{
 			String args = command.substring(firstSpace+1);
 			arguments = CommandLineTokenizer.tokenize(args);
 		}
 		
 		IScript script = ScriptEngineRegistry.startScript(scriptName, this, arguments);
 		if (script != null)
 		{
 			script.addScriptListener(this);
 			for (IScriptListener listener : scriptListeners) listener.scriptStarted(script);
 			runningScripts.add(script);
 		}
 	}
 	
 	public void scriptAdded(IScript script) {
 		for (IScriptListener listener : scriptListeners) listener.scriptAdded(script);
 	}
 	
 	public void scriptRemoved(IScript script) {
 		for (IScriptListener listener : scriptListeners) listener.scriptRemoved(script);
 	}
 	
 	public void scriptPaused(IScript script) {
 		for (IScriptListener listener : scriptListeners) listener.scriptPaused(script);
 	}
 	
 	public void scriptResumed(IScript script) {
 		for (IScriptListener listener : scriptListeners) listener.scriptResumed(script);
 	}
 	
 	public void scriptStarted(IScript script) {
 		for (IScriptListener listener : scriptListeners) listener.scriptStarted(script);
 	}
 	
 	public void scriptStopped(IScript script, boolean userStopped) {
 		runningScripts.remove(script);
 		
 		for (IScriptListener listener : scriptListeners) listener.scriptStopped(script, userStopped);
 	}
 	
 	public IProperty<Integer> getRoundtime() {
 		return roundtime;
 	}
 	
 	public IProperty<Integer> getMonsterCount() {
 		return monsterCount;
 	}
 
 	private class RoundtimeThread extends Thread
 	{		
 		public void run()
 		{
 			for (;;) {
 				long now = System.currentTimeMillis();
 				long roundTime = 0;
 				
 				// Synchronize with external roundtime updates
 				synchronized(StormFrontClient.this) {
 					if (roundtimeEnd != null)
 						roundTime = roundtimeEnd * 1000L + timeDelta - now;
 					
 					if (roundTime <= 0) {
 						roundtimeThread = null;
 						roundtimeEnd = null;
 						roundtime.set(0);
 						StormFrontClient.this.notifyAll();
 						return;
 					}
 				}
 				
 				// Update the world with the new roundtime
 				// Avoid flicker caused by redundant updates
 				int rt = (int)((roundTime + 999) / 1000);
 				if (roundtime.get() != rt)
 					roundtime.set(rt);
 				
 				// Compute how long until next roundtime update
 				long waitTime = roundTime % 1000;
 				if (waitTime == 0) waitTime = 1000;
 				
 				try {
 					Thread.sleep(waitTime);
 				} catch (InterruptedException e) {
 					// This is not supposed to happen.
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public synchronized void setupRoundtime(Integer end)
 	{
 		roundtimeEnd = end;
 	}
 	
 	public synchronized void syncTime(Integer now)
 	{
 		if (roundtimeEnd == null) return;
 		
 		long newTimeDelta = System.currentTimeMillis() - now * 1000L;
 		if (roundtimeThread != null) {
 			// Don't decrease timeDelta while roundtimes are active.
 			if (newTimeDelta > timeDelta) timeDelta = newTimeDelta;
 			return;
 		}
 		timeDelta = newTimeDelta;
 		
 		roundtime.activate(); // FIXME Do we need this?
 		if (roundtimeEnd > now) {
 			// We need to do this now due to scheduling delays in the thread
 			roundtime.set(roundtimeEnd - now);
 			roundtimeThread = new RoundtimeThread();
 			roundtimeThread.start();
 		} else {
 			roundtime.set(0);
 			roundtimeEnd = null;
 		}
 	}
 	
 	public synchronized void waitForRoundtime(double delay) throws InterruptedException {
 		if (roundtimeEnd == null)
 			return;
 		while (roundtimeEnd != null) {
 			wait();
 			Thread.sleep((long)(delay * 1000));
 		}
 	}
 	
 	public IProperty<BarStatus> getHealth() {
 		return health;
 	}
 
 	public IProperty<BarStatus> getMana() {
 		return mana;
 	}
 
 	public IProperty<BarStatus> getFatigue() {
 		return fatigue;
 	}
 
 	public IProperty<BarStatus> getSpirit() {
 		return spirit;
 	}
 
 	public void connect(String server, int port, String key) throws IOException {
 		connection = new StormFrontConnection(this, key);
 		connection.connect(server, port);
 		
 		WarlockClientRegistry.clientConnected(this);
 	}
 	
 	public void streamCleared() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public ClientProperty<String> getPlayerId() {
 		return playerId;
 	}
 	
 	public IStormFrontClientSettings getStormFrontClientSettings() {
 		return clientSettings;
 	}
 	
 	public IProperty<String> getCharacterName() {
 		return characterName;
 	}
 	
 	public IProperty<String> getGameCode() {
 		return gameCode;
 	}
 	
 	public IProperty<String> getClientId() {
 		return playerId;
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
 	
 	public ICharacterStatus getCharacterStatus() {
 		return status;
 	}
 	
 	public Collection<IScript> getRunningScripts() {
 		return runningScripts;
 	}
 	
 	public void addScriptListener(IScriptListener listener)
 	{
 		scriptListeners.add(listener);
 	}
 	
 	public void removeScriptListener (IScriptListener listener)
 	{
 		if (scriptListeners.contains(listener))
 			scriptListeners.remove(listener);
 	}
 	
 	public IWarlockSkin getSkin() {
 		return skin;
 	}
 	
 	public IStormFrontSkin getStormFrontSkin() {
 		return skin;
 	}
 	
 	@Override
 	public IStream getStream(String streamName) {
 		return StormFrontStream.fromNameAndClient(this, streamPrefix + streamName);
 	}
 	
 	public IStream getThoughtsStream() {
 		return getStream(THOUGHTS_STREAM_NAME);
 	}
 	
 	public IStream getInventoryStream() {
 		return getStream(INVENTORY_STREAM_NAME);
 	}
 	
 	public IStream getDeathsStream() {
 		return getStream(DEATH_STREAM_NAME);
 	}
 	
 	public IStream getRoomStream() {
 		return getStream(ROOM_STREAM_NAME);
 	}
 	
 	@Override
 	public IStream getDefaultStream() {
 		return getStream(DEFAULT_STREAM_NAME);
 	}
 
 	public IStream getFamiliarStream() {
 		return getStream(FAMILIAR_STREAM_NAME);
 	}
 	
 	public void setComponent (String name, String value, IStream stream)
 	{
 		if (!components.containsKey(name)) {
 			components.put(name, new ClientProperty<String>(this, name, value));
 		} else {
 			components.get(name).set(value);
 		}
 		componentStreams.put(name, stream);
 		//stream.addComponent(name);
 	}
 	
 	public void updateComponent(String name, WarlockString value) {
 		components.get(name).set(value.toString());
 		componentStreams.get(name).updateComponent(name, value);
 	}
 	
 	public IProperty<String> getComponent(String componentName) {
 		return components.get(componentName);
 	}
 	
 	@Override
 	protected void finalize() throws Throwable {
 		WarlockClientRegistry.removeClient(this);
 		super.finalize();
 	}
 	
 	public IWarlockStyle getCommandStyle() {
 		IWarlockStyle style = clientSettings.getNamedStyle(StormFrontClientSettings.PRESET_COMMAND);
 		if (style == null) {
 			return new WarlockStyle();
 		}
 		return style;
 	}
 	
 	public void loadCmdlist()
 	{
 		try {
 			commands  = new HashMap<String, String>();
 			FileInputStream stream = new FileInputStream(ConfigurationUtil.getConfigurationFile("cmdlist1.xml"));
 			StormFrontDocument document = new StormFrontDocument(stream);
 			stream.close();
 			
 			StormFrontElement cmdlist = document.getRootElement();
 			for (StormFrontElement cliElement : cmdlist.elements())
 			{
 				if(cliElement.getName().equals("cli")) {
 					String coord = cliElement.attributeValue("coord");
 					String command = cliElement.attributeValue("command");
 					
 					if(coord != null && command != null)
 						commands.put(coord, command);
 				}
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public String getCommand(String coord) {
 		if(commands == null) return null;
 		return commands.get(coord);
 	}
 	/* Internal only.. meant for importing/exporting stormfront's savings */
 	public StormFrontServerSettings getServerSettings() {
 		return serverSettings;
 	}
 }
