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
 package cc.warlock.rcp.views;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.PageBook;
 import org.eclipse.ui.part.ViewPart;
 
 import cc.warlock.core.client.IHighlightString;
 import cc.warlock.core.client.IProperty;
 import cc.warlock.core.client.IStream;
 import cc.warlock.core.client.IStreamListener;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.PropertyListener;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.rcp.configuration.GameViewConfiguration;
 import cc.warlock.rcp.ui.WarlockText;
 import cc.warlock.rcp.ui.client.SWTPropertyListener;
 import cc.warlock.rcp.ui.client.SWTStreamListener;
 import cc.warlock.rcp.ui.style.DefaultStyleProvider;
 import cc.warlock.rcp.ui.style.StyleProviders;
 import cc.warlock.rcp.util.ColorUtil;
 
 public class StreamView extends ViewPart implements IStreamListener, IGameViewFocusListener {
 	
 	public static final String STREAM_VIEW_PREFIX = "cc.warlock.rcp.views.stream.";
 	
 	public static final String RIGHT_STREAM_PREFIX = "rightStream.";
 	public static final String TOP_STREAM_PREFIX = "topStream.";
 	
 	protected static ArrayList<StreamView> openViews = new ArrayList<StreamView>();
 	
 	protected IStream mainStream;
 	protected ArrayList<IStream> streams;
 	protected IWarlockClient client;
 	protected Composite mainComposite;
 	protected PageBook book;
 	protected Hashtable<IWarlockClient, WarlockText> clientStreams = new Hashtable<IWarlockClient, WarlockText>();
 	
 	// This name is the 'suffix' part of the stream... so we will install listeners for each client
 	protected String mainStreamName;
 	protected SWTStreamListener streamListenerWrapper;
 	protected SWTPropertyListener<String> propertyListenerWrapper;
 	protected boolean appendNewlines = false;
 	protected boolean isPrompting = false;
 	protected boolean multiClient = false;
 	
 	protected HashMap<IWarlockClient, WarlockString> textBuffers = new HashMap<IWarlockClient, WarlockString>();
 	
 	public StreamView() {
 		openViews.add(this);
 		streamListenerWrapper = new SWTStreamListener(this);
 		streams = new ArrayList<IStream>();
 		
 		if (!(this instanceof GameView))
 		{
 			GameView.addGameViewFocusListener(this);
 			this.multiClient = true;
 		}
 	}
 	
 	public void setMultiClient (boolean multiClient)
 	{
 		this.multiClient = multiClient;
 	}
 
 	public static StreamView getViewForStream (String prefix, String streamName) {
 		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 		
 		for (StreamView view : openViews)
 		{
 			if (view.getStreamName().equals(streamName))
 			{
 				page.activate(view);
 				return view;
 			}
 		}
 		
 		// none of the already created views match, create a new one
 		try {
 			StreamView nextInstance = (StreamView) page.showView(STREAM_VIEW_PREFIX + prefix + streamName);
 			nextInstance.setStreamName(streamName);
 			nextInstance.setMultiClient(true);
 			
 			return nextInstance;
 		} catch (PartInitException e) {
 			e.printStackTrace();
 		}	
 		return null;
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		mainComposite = new Composite (parent, SWT.NONE);
 		GridLayout layout = new GridLayout(1, false);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		mainComposite.setLayout(layout);
 		
 		book = new PageBook(mainComposite, SWT.NONE);
 		book.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 
 		if (!(this instanceof GameView))
 		{
 			String streamName = getViewSite().getId().substring(getViewSite().getId().lastIndexOf('.')+1);
 			setStreamName(streamName);
 		}
 	}
 	
 	protected WarlockText getTextForClient (IWarlockClient client)
 	{
 		if (!clientStreams.containsKey(client))
 		{
 			// TODO move this section into WarlockText
 			WarlockText text = new WarlockText(book, SWT.V_SCROLL, client);
 			GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
 			text.setLayoutData(data);
 			text.setEditable(false);
 			text.setWordWrap(true);
 			text.getTextWidget().setIndent(1);
			GameView game = GameView.getGameViewForClient(client);
			if (game == null) {
				System.out.println("Couldn't find a gameview for this client! This view won't be setup to send keys over.");
			} else {
				// if the getWarlockEntry is null, then we're prolly already in the gameview which means we'll be setup by GameView
				// TODO: Stop the GameView from doing this for us.. and make it create the WarlockEntry before the View
				if (game.getWarlockEntry() != null)
					text.getTextWidget().addVerifyKeyListener(game.getWarlockEntry());
			}
 			
 			Color background = ColorUtil.warlockColorToColor(GameViewConfiguration.instance().getDefaultBackground());
 			Color foreground = ColorUtil.warlockColorToColor(GameViewConfiguration.instance().getDefaultForeground());
 			
 			text.setBackground(background);
 			text.setForeground(foreground);
 			
 			String fontFace = GameViewConfiguration.instance().getDefaultFontFace();
 			int fontSize = GameViewConfiguration.instance().getDefaultFontSize();
 			
 			text.setFont(new Font(Display.getDefault(), fontFace, fontSize, SWT.NORMAL));
 			text.setScrollDirection(SWT.DOWN);
 			
 			clientStreams.put(client, text);
 			return text;
 		}
 		else return clientStreams.get(client);
 	}
 
 	public void gameViewFocused(GameView gameView) {
 		if (multiClient)
 		{
 			if (gameView.getWarlockClient() != null)
 			{
 				setClient(gameView.getWarlockClient());
 			}
 		}
 	}
 	
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public IStream getMainStream() {
 		return mainStream;
 	}
 	
 	public void setMainStream(IStream stream) {
 		this.mainStream = stream;
 		
 		stream.addStreamListener(streamListenerWrapper);
 		propertyListenerWrapper = new SWTPropertyListener<String>(new PropertyListener<String>() {
 			@Override
 			public void propertyChanged(IProperty<String> property, String oldValue) {
 				if (property.getName().equals("streamTitle"))
 				{
 					setPartName(property.get());
 				}
 			}
 		});
 		stream.getTitle().addListener(propertyListenerWrapper);
 		stream.setView(true);
 	}
 	
 	public void removeMainStream ()
 	{
 		mainStream.removeStreamListener(streamListenerWrapper);
 		mainStream.getTitle().removeListener(propertyListenerWrapper);
 		mainStream.setView(false);
 	}
 	
 	public void addStream (IStream stream) {
 		stream.addStreamListener(streamListenerWrapper);
 		streams.add(stream);
 		stream.setView(true);
 	}
 	
 	public void removeStream (IStream stream) {
 		stream.removeStreamListener(streamListenerWrapper);
 		streams.remove(stream);
 		stream.setView(false);
 	}
 	
 	public void streamCleared(IStream stream) {
 		if (this.mainStream.equals(stream) || streams.contains(stream))
 		{
 			clientStreams.get(stream.getClient()).setText("");
 		}
 	}
 	
 	protected void bufferText (IWarlockClient client, WarlockString string)
 	{
 		WarlockString bufferedText = textBuffers.get(client);
 		if(bufferedText == null) {
 			bufferedText = new WarlockString();
 			textBuffers.put(client, bufferedText);
 		}
 
 		bufferedText.append(string);
 	}
 	
 	protected void appendText(IWarlockClient client, WarlockString string) {
 		WarlockText text = getTextForClient(client);
 		highlightText(client, string);
 		text.append(string);
 	}
 	
 	protected void highlightText (IWarlockClient client, WarlockString text)
 	{	
 		for (IHighlightString hstring : client.getHighlightStrings())
 		{
 			findHighlight(hstring, text);
 		}
 	}
 	
 	protected void findHighlight (IHighlightString highlight, WarlockString text)
 	{
 		Pattern p = highlight.getPattern();
 		if(p == null)
 			return;
 		Matcher matcher = p.matcher(text.toString());
 		
 		while (matcher.find())
 		{
 			MatchResult result = matcher.toMatchResult();
 			int start = result.start();
 			int length = result.end() - start;
 			
 			IWarlockStyle style = highlight.getStyle();
 			text.addStyle(start, length, style);
 		}
 	}
 	
 	public void streamReceivedText(IStream stream, WarlockString text) {
 		if (this.mainStream.equals(stream) || this.streams.contains(stream))
 		{
 			WarlockString string = new WarlockString();
 			
 			if (isPrompting) {
 				string.append("\n");
 				isPrompting = false;
 			}
 			
 			string.append(text);
 			
 			if (appendNewlines)
 				string.append("\n");
 			
 			bufferText(stream.getClient(), string);
 		}
 	}
 	
 	public void streamEchoed(IStream stream, String text) {
 		if (this.mainStream.equals(stream) || this.streams.contains(stream))
 		{
 			IWarlockClient client = stream.getClient();
 			WarlockString string = new WarlockString();
 			if(isPrompting) {
 				string.append("\n");
 				isPrompting = false;
 			}
 			int styleStart = string.length();
 			string.append(text);
 			// TODO: make a different style for client messages
 			string.addStyle(styleStart, text.length(), client.getCommandStyle());
 			
 			appendText(client, string);
 		}
 	}
 	
 	public void streamReceivedCommand(IStream stream, String text) {
 		if (this.mainStream.equals(stream) || this.streams.contains(stream))
 		{
 			IWarlockClient client = stream.getClient();
 			WarlockString string = new WarlockString(text);
 			
 			string.addStyle(0, text.length(), client.getCommandStyle());
 			
 			isPrompting = false;
 			appendText(client, string);
 		}
 	}
 	
 	public void streamPrompted(IStream stream, String prompt) {
 		if ((!isPrompting) &&
 				(this.mainStream.equals(stream) || this.streams.contains(stream)))
 		{
 			IWarlockClient client = stream.getClient();
 			
 			WarlockString text = new WarlockString();
 			WarlockString bufferedText = textBuffers.get(client);
 			
 			if (bufferedText != null)
 			{
 				text.append(bufferedText);
 				textBuffers.remove(client);
 			}
 			
 			if(isPrompting) {
 				text.append("\n");
 			} else {
 				isPrompting = true;
 			}
 				
 			text.append(prompt);
 			appendText(client, text);
 		}
 	}
 
 	public static Collection<StreamView> getOpenViews ()
 	{
 		return openViews;
 	}
 	
 	public void setClient (IWarlockClient client)
 	{
 		this.client = client;
 		book.showPage(getTextForClient(client).getTextWidget());
 		
 		if (mainStream == null)
 			setMainStream(client.getStream(mainStreamName));
 
 		if (StyleProviders.getStyleProvider(client) == null)
 			StyleProviders.setStyleProvider(client, DefaultStyleProvider.instance());
 	}
 	
 	@Override
 	public void dispose() {
 		if (mainStream != null) {
 			mainStream.removeStreamListener(streamListenerWrapper);
 			mainStream.getTitle().removeListener(propertyListenerWrapper);
 			mainStream.setView(false);
 		}
 		
 		for (IStream stream : streams)
 		{
 			stream.removeStreamListener(streamListenerWrapper);
 			stream.setView(false);
 		}
 		
 		clientStreams.clear();
 		GameView.removeGameViewFocusListener(this);
 		
 		if (openViews.contains(this)) {
 			openViews.remove(this);
 		}
 		
 		super.dispose();
 	}
 	
 	public String getStreamName() {
 		return mainStreamName;
 	}
 
 	public void setStreamName(String streamName) {
 		this.mainStreamName = streamName;
 	}
 
 	public void setAppendNewlines(boolean appendNewlines) {
 		this.appendNewlines = appendNewlines;
 	}
 	
 	public void setViewTitle (String title)
 	{
 		setPartName(title);
 	}
 	
 	public void streamFlush(IStream stream) {
 		WarlockString bufferedText = textBuffers.get(stream.getClient());
 		if(bufferedText != null) {
 			appendText(stream.getClient(), bufferedText);
 			textBuffers.remove(client);
 		}
 	}
 	
 	public void setForeground (IWarlockClient client, Color foreground)
 	{
 		getTextForClient(client).setForeground(foreground);
 	}
 	
 	public void setBackground (IWarlockClient client, Color background)
 	{
 		getTextForClient(client).setBackground(background);
 	}
 }
