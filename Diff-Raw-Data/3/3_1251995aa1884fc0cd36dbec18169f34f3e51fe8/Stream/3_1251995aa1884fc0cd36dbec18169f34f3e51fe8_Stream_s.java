 /*
  * Created on Jan 16, 2005
  */
 package com.arcaner.warlock.client.internal;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 
 import com.arcaner.warlock.client.IProperty;
 import com.arcaner.warlock.client.IStream;
 import com.arcaner.warlock.client.IStreamListener;
 import com.arcaner.warlock.client.IWarlockStyle;
 
 /**
  * @author Marshall
  * 
  * The internal implementation of a StormFront stream.
  */
 public class Stream implements IStream {
 	
 	private static Hashtable<String, Stream> streams = new Hashtable<String, Stream>();
 	
 	protected IProperty<String> streamName, streamTitle;
 	protected ArrayList<IStreamListener> listeners;
 	protected boolean isPrompting = false;
 	protected StyledString buffer = null;
 	
 	private Stream (String streamName) {
 		this.streamName = new Property<String>("streamName");
 		this.streamName.set(streamName);
 		this.streamTitle = new Property<String>("streamTitle");
 		
 		listeners = new ArrayList<IStreamListener>();
 		
 		streams.put(streamName, this);
 	}
 
 	public void addStreamListener(IStreamListener listener) {
 		listeners.add(listener);
 	}
 	
 	public void removeStreamListener(IStreamListener listener) {
 		listeners.remove(listener);
 	}
 
 	public void clear() {
 		for(IStreamListener listener : listeners) {
 			listener.streamCleared(this);
 		}
 	}
 	
 	public void send(String text) {
 		send (text, WarlockStyle.EMPTY_STYLE);
 	}
 	
 	private boolean isEmpty (String data)
 	{
 		for (int i = 0; i < data.length(); i++)
 		{
 			char c = data.charAt(i);
 			if (c != ' ' && c != '\n' && c != '\r')
 				return false;
 		}
 		return true;
 	}
 	
 	public void send(String data, IWarlockStyle style) {
 		
 		if (buffer == null)
 		{
 			buffer = new StyledString();
 		}
 		int currentOffset = buffer.getBuffer().length();
 		
 		buffer.getBuffer().append(data);
 		if (!buffer.getStyles().contains(style))
 		{
 			// allows styles to be relative to the string they're using
 			style.setStart(style.getStart() + currentOffset);
 			buffer.addStyle(style);
 		}
 		
 		if (buffer.readyToFlush())
 		{
 			for(IStreamListener listener : listeners) {
 				try {
 					listener.streamReceivedText(this, buffer);
 				} catch (Throwable t) {
 					// TODO Auto-generated catch block
 					t.printStackTrace();
 				}
 			}
 			
 			if (data.length() > 0 && !isEmpty(data))
 			{
 				isPrompting = false;
 			}
 			
 			buffer = null;
 		}
 	}
 
 	public void prompt(String prompt) {
 		if (!isPrompting)
 		{
 			for (IStreamListener listener : listeners)
 			{
 				listener.streamPrompted(this, prompt);
 			}
 		}
 		isPrompting = true;
 	}
 	
 	public void echo(String text) {
 		for (IStreamListener listener : listeners)
 		{
 			listener.streamEchoed(this, text);
 		}
 	}
 	
 	public IProperty<String> getName() {
 		return streamName;
 	}
 	
 	protected static Stream fromName (String name)
 	{
 		if (streams.containsKey(name))
 			return streams.get(name);
 		
 		else return new Stream(name);
 	}
 	
 	public static Collection<Stream> getStreams ()
 	{
 		return streams.values();
 	}
 
 	public IProperty<String> getTitle() {
 		return streamTitle;
 	}
 }
