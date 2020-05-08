 /*
  * Created on Mar 26, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package com.arcaner.warlock.client.stormfront.internal;
 
 import java.io.IOException;
 
 import com.arcaner.warlock.client.ICompass;
 import com.arcaner.warlock.client.IProperty;
 import com.arcaner.warlock.client.IWarlockClientViewer;
 import com.arcaner.warlock.client.internal.ClientProperty;
 import com.arcaner.warlock.client.internal.Compass;
 import com.arcaner.warlock.client.internal.WarlockClient;
 import com.arcaner.warlock.client.stormfront.IStormFrontClient;
 import com.arcaner.warlock.client.stormfront.IStormFrontClientViewer;
 import com.arcaner.warlock.client.stormfront.IStormFrontStyle;
 import com.arcaner.warlock.configuration.ServerSettings;
 import com.arcaner.warlock.network.StormFrontConnection;
 import com.arcaner.warlock.stormfront.IStormFrontProtocolHandler;
 import com.arcaner.warlock.stormfront.IStreamListener;
 import com.arcaner.warlock.stormfront.internal.Stream;
 
 /**
  * @author Marshall
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class StormFrontClient extends WarlockClient implements IStormFrontClient, IStreamListener {
 
 	protected ICompass compass;
 	protected int lastPrompt;
 	protected ClientProperty<Integer> roundtime, health, mana, fatigue, spirit;
 	protected boolean isPrompting = false;
 	protected StringBuffer buffer = new StringBuffer();
 	protected IStormFrontProtocolHandler handler;
 	protected boolean isBold;
 	protected ClientProperty<String> playerId;
 	protected IStormFrontStyle currentStyle = StormFrontStyle.EMPTY_STYLE;
 	protected ServerSettings serverSettings;
 	
 	public StormFrontClient() {
 		compass = new Compass(this);
 		
 		roundtime = new ClientProperty<Integer>(this, "roundtime");
 		health = new ClientProperty<Integer>(this, "health");
 		mana = new ClientProperty<Integer>(this, "mana");
 		fatigue = new ClientProperty<Integer>(this, "fatigue");
 		spirit = new ClientProperty<Integer>(this, "spirit");
 		playerId = new ClientProperty<String>(this, "playerId");
 		serverSettings = new ServerSettings(this);
 		Stream.DEFAULT_STREAM.addStreamListener(this);
 	}
 	
 	public void append (String viewName, String text)
 	{	
 		if(DEFAULT_VIEW.equals(viewName))
 		{
 			append(viewName, text, currentStyle);
 			isPrompting = false;
 			
 //			boolean flush = false;
 //			
 //			// if we've already shown the prompt newline, don't do it again
 //			if(isPrompting) {
 //				if(text.charAt(0) == '\n') {
 //					text = text.substring(1);
 //					flush = true;
 //				} else if(text.startsWith("\r\n")) {
 //					text = text.substring(2);
 //					flush = true;
 //				}
 //			}
 //			
 //			// search for a newline
 //			int end = text.lastIndexOf('\r');
 //			if(end < 0) end = text.lastIndexOf('\n');
 //			if(end >= 0) {
 //				flush = true;
 //			} else {
 //				// if there was no newline, the end is the start
 //				end = 0;
 //			}
 //			
 //			/*
 //			 * if there was a newline, output the existing buffer, and then the
 //			 * text up to that new line
 //			 */
 //			if(flush) {
 //				// if there is text to output, add it to the buffer
 //				if(end > 0) {
 //					// when we finish prompting, prepend a newline
 //					if(isPrompting) {
 //						isPrompting = false;
 //						buffer.append("\r\n");
 //					}
 //					buffer.append(text, 0, end);
 //				}
 //				
 //				/*
 //				 * it's possible that our end was the first character, so the
 //				 * buffer might still be empty, we need to check it.
 //				 */
 //				// if there is something in the buffer, output and clear it
 //				if(buffer.length() > 0) {
 //					String bufferText = buffer.toString();
 //					
 //					append(viewName, bufferText, currentStyle);
 //					buffer.delete(0, buffer.length());
 //					bufferText = null;
 //				}
 //			}
 //			
 //			// append the rest of the text to the buffer
 //			if(text.length() > end) {
 //				// when we finish prompting, prepend a newline
 //				if(isPrompting) {
 //					isPrompting = false;
 //					buffer.append("\r\n");
 //				}
 //				buffer.append(text, end, text.length());
 //			}
 		} else {
 			System.out.println("Got a stream not shown of id: " + viewName);
 		}
 	}
 	
 	public IProperty<Integer> getRoundtime() {
 		return roundtime;
 	}
 
 	private void startRoundtime (int seconds)
 	{
 		roundtime.set(seconds);
 	}
 	
 	private void updateRoundtimes (int currentRoundtime)
 	{
 		roundtime.set(currentRoundtime);
 	}
 	
 	public void startRoundtime (final int seconds, String label) {
 		new Thread (new Runnable () {
 			private int elapsed = 0;
 			
 			public void run() {
 				try {
 					startRoundtime(seconds);
 					
 					while (elapsed < seconds)
 					{
 						updateRoundtimes(seconds - elapsed);
 						
 						elapsed++;
 						Thread.sleep((long)1000);
 					}
 					
 					updateRoundtimes(0);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
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
 	
 	public void streamReceivedText(String text, IStormFrontStyle style) {
 		for (IWarlockClientViewer viewer : viewers)
 		{
 			if (viewer instanceof IStormFrontClientViewer)
 			{
 				IStormFrontClientViewer sfViewer = (IStormFrontClientViewer) viewer;
 				sfViewer.append(Stream.DEFAULT_STREAM, text, style);
 			}
 			else {
 				viewer.append(Stream.DEFAULT_STREAM.getName(), text);
 			}
 		}
 	}
 	
 	public void append(String viewName, String text, IStormFrontStyle style) {
 		streamReceivedText(text, style);
 	}
 	
 	public void echo(String viewName, String text, IStormFrontStyle style) {
 		for (IWarlockClientViewer viewer : viewers)
 		{
 			if (viewer instanceof IStormFrontClientViewer)
 			{
 				IStormFrontClientViewer sfViewer = (IStormFrontClientViewer) viewer;
 				sfViewer.echo(Stream.fromName(viewName), text, style);
 			}
 			else {
 				viewer.echo(viewName, text);
 			}
 		}
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
 
 	public IStormFrontStyle getCurrentStyle() {
 		return currentStyle;
 	}
 
 	public void setCurrentStyle(IStormFrontStyle currentStyle) {
 		this.currentStyle = currentStyle;
 	}
 
 	public ClientProperty<String> getPlayerId() {
 		return playerId;
 	}
 	
 	public ServerSettings getServerSettings() {
 		return serverSettings;
 	}
 }
