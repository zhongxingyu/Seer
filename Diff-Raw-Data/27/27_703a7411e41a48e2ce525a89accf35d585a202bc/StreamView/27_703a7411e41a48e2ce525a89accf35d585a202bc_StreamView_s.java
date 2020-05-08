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
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
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
 
 import cc.warlock.core.client.ICommand;
 import cc.warlock.core.client.IProperty;
 import cc.warlock.core.client.IStream;
 import cc.warlock.core.client.IStreamListener;
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.IWarlockClientListener;
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.PropertyListener;
 import cc.warlock.core.client.WarlockClientRegistry;
 import cc.warlock.core.client.WarlockFont;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.settings.IHighlightString;
 import cc.warlock.core.client.settings.IWindowSettings;
 import cc.warlock.rcp.configuration.GameViewConfiguration;
 import cc.warlock.rcp.ui.WarlockText;
 import cc.warlock.rcp.ui.client.SWTPropertyListener;
 import cc.warlock.rcp.ui.client.SWTStreamListener;
 import cc.warlock.rcp.ui.client.SWTWarlockClientListener;
 import cc.warlock.rcp.ui.style.DefaultStyleProvider;
 import cc.warlock.rcp.ui.style.StyleProviders;
 import cc.warlock.rcp.util.ColorUtil;
 import cc.warlock.rcp.util.FontUtil;
 import cc.warlock.rcp.util.SoundPlayer;
 
 public class StreamView extends ViewPart implements IStreamListener, IGameViewFocusListener, IWarlockClientListener {
 	
 	public static final String STREAM_VIEW_PREFIX = "cc.warlock.rcp.views.stream.";
 	
 	public static final String RIGHT_STREAM_PREFIX = "rightStream.";
 	public static final String TOP_STREAM_PREFIX = "topStream.";
 	
 	protected static ArrayList<StreamView> openViews = new ArrayList<StreamView>();
 	protected static StreamView viewInFocus;
 	
 	protected ArrayList<IStream> streams;
 	protected WarlockText currentText;
 	protected Composite mainComposite;
 	protected PageBook book;
 	protected HashMap<IWarlockClient, WarlockText> clientStreams = new HashMap<IWarlockClient, WarlockText>();
 	
 	// This name is the 'suffix' part of the stream... so we will install listeners for each client
 	protected String mainStreamName;
 	protected SWTStreamListener streamListenerWrapper;
 	protected SWTPropertyListener<String> propertyListenerWrapper;
 	protected boolean appendNewlines = false;
 	protected boolean isPrompting = false;
 	protected String prompt = null;
 	protected boolean streamTitled = true;
 	protected boolean bufferOnPrompt = true;
 	
 	public void setBufferOnPrompt(boolean bufferOnPrompt) {
 		this.bufferOnPrompt = bufferOnPrompt;
 	}
 
 	private HashMap<IWarlockClient, WarlockString> textBuffers =
 		new HashMap<IWarlockClient, WarlockString>();
 	
 	public StreamView() {
 		openViews.add(this);
 		streamListenerWrapper = new SWTStreamListener(this);
 		streams = new ArrayList<IStream>();
 		
 		if (!(this instanceof GameView))
 		{
 			GameView.addGameViewFocusListener(this);
 			WarlockClientRegistry.addWarlockClientListener(new SWTWarlockClientListener(this));
 		}
 	}
 	
 	public void setStreamTitled (boolean enabled) {
 		streamTitled = enabled;
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
 			
 			return nextInstance;
 		} catch (PartInitException e) {
 			e.printStackTrace();
 		}	
 		return null;
 	}
 	
 	protected void createMainComposite(Composite parent)
 	{
 		mainComposite = new Composite (parent, SWT.NONE);
 		GridLayout layout = new GridLayout(1, false);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		mainComposite.setLayout(layout);
 	}
 	
 	protected void createPageBook ()
 	{
 		book = new PageBook(mainComposite, SWT.NONE);
 		book.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 
 		if (!(this instanceof GameView))
 		{
 			String streamName = getViewSite().getId().substring(getViewSite().getId().lastIndexOf('.')+1);
 			setStreamName(streamName);
 		}
 	}
 	
 	@Override
 	public void createPartControl(Composite parent) {
 		createMainComposite(parent);
 		createPageBook();
 		if (!(this instanceof GameView)) {
 			scanClients();
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
 			GameView game;
 			if (this instanceof GameView) { // Only setup this
 				game = (GameView) this;
 			} else {
 				game = GameView.getGameViewForClient(client);
 			}
 			if (game == null) {
 				System.out.println("Couldn't find a gameview for this client! This view won't be setup to send keys over.");
 			} else {
 				text.getTextWidget().addVerifyKeyListener(game.getWarlockEntry().new KeyVerifier());
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().addKeyListener(game.getWarlockEntry().new KeyEventListener());
 			}
 			
 			Color background = ColorUtil.warlockColorToColor(client.getSkin().getMainBackground());
 			Color foreground = ColorUtil.warlockColorToColor(client.getSkin().getMainForeground());
 			WarlockFont font = client.getClientSettings().getMainWindowSettings().getFont();
 			if (!mainStreamName.equals(IWarlockClient.DEFAULT_STREAM_NAME)) {
 				IWindowSettings settings = client.getClientSettings().getWindowSettings(mainStreamName);
 				if (settings != null) {
 					if (!settings.getBackgroundColor().isDefault())
 						background = ColorUtil.warlockColorToColor(settings.getBackgroundColor());
 					if (!settings.getForegroundColor().isDefault())
 						foreground = ColorUtil.warlockColorToColor(settings.getForegroundColor());
 					if (!settings.getFont().isDefaultFont())
 						font = settings.getFont();
 				}
 			}
 			text.setBackground(background);
 			text.setForeground(foreground);
 			
 			String defaultFontFace = GameViewConfiguration.instance().getDefaultFontFace();
 			int defaultFontSize = GameViewConfiguration.instance().getDefaultFontSize();
 			
 			if (font.isDefaultFont()) {
 				text.setFont(new Font(Display.getDefault(), defaultFontFace, defaultFontSize, SWT.NORMAL));
 			} else {
 				text.setFont(FontUtil.warlockFontToFont(font));
 			}
 			
 			text.setScrollDirection(SWT.DOWN);
 			
 			clientStreams.put(client, text);
 			
 			addStream(client.getStream(mainStreamName));
 
 			return text;
 		}
 		else return clientStreams.get(client);
 	}
 
 	public void gameViewFocused(GameView gameView) {
 		if (gameView.getWarlockClient() != null)
 		{
 			setClient(gameView.getWarlockClient());
 		}
 	}
 	
 	@Override
 	public synchronized void setFocus() {
 		// Set View in Focus
 		viewInFocus = this;
 	}
 	
 	public static StreamView getViewInFocus() {
 		return viewInFocus;
 	}
 	
 	public synchronized void addStream (IStream stream) {
 		streams.add(stream);
 		stream.addStreamListener(streamListenerWrapper);
 		if (streamTitled) {
 			if (propertyListenerWrapper == null) {
 				propertyListenerWrapper = new SWTPropertyListener<String>(new PropertyListener<String>() {
 					@Override
 					public void propertyChanged(IProperty<String> property, String oldValue) {
 						if (property.getName().equals("streamTitle"))
 						{
 							setPartName(property.get());
 						}
 					}
 				});
 			}
 			stream.getTitle().addListener(propertyListenerWrapper);
 		}
 		stream.setView(true);
 	}
 	
 	public synchronized void removeStream (IStream stream) {
 		stream.removeStreamListener(streamListenerWrapper);
 		if (streamTitled)
 			stream.getTitle().removeListener(propertyListenerWrapper);
 		streams.remove(stream);
 		stream.setView(false);
 	}
 	
 	public synchronized void streamCleared(IStream stream) {
 		if (streams.contains(stream))
 		{
			clientStreams.get(stream.getClient()).setText("");
 		}
 	}
 	
 	protected synchronized void bufferText (IWarlockClient client, WarlockString string)
 	{
 		WarlockString bufferedText = textBuffers.get(client);
 		if(bufferedText == null) {
 			bufferedText = new WarlockString();
 			textBuffers.put(client, bufferedText);
 		}
 
 		bufferedText.append(string);
 	}
 	
 	protected synchronized void appendText(IWarlockClient client, WarlockString string) {
 		WarlockText text = getTextForClient(client);
 		highlightText(client, string);
 		text.append(string);
 	}
 	
 	protected void highlightText (IWarlockClient client, WarlockString text)
 	{	
 		for (IHighlightString highlight : client.getClientSettings().getAllHighlightStrings())
 		{
 			Pattern p;
 			try {
 				p = highlight.getPattern();
 			} catch(PatternSyntaxException e) {
 				continue;
 			}
 			if(p == null)
 				continue;
 			Matcher matcher = p.matcher(text.toString());
 			
 			while (matcher.find())
 			{
 				MatchResult result = matcher.toMatchResult();
 				int start = result.start();
 				int length = result.end() - start;
 				
 				IWarlockStyle style = highlight.getStyle();
 				text.addStyle(start, length, style);
 				
 				try{
 					if (style.getSound() != null && !style.getSound().equals("")){
 						//System.out.println("Playing sound " + style.getSound());
 						SoundPlayer.getInstance().play(style.getSound(), false);
 						//InputStream soundStream = new FileInputStream(style.getSound());
 						//RCPUtil.playSound(soundStream);
 					}
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public synchronized void streamReceivedText(IStream stream, WarlockString text) {
 		if (this.streams.contains(stream))
 		{
 			WarlockString string = new WarlockString();
 			
 			if (bufferOnPrompt) {
 			
 				if (isPrompting) {
 					string.append("\n");
 					isPrompting = false;
 				}
 				
 				string.append(text);
 				
 				if (appendNewlines)
 					string.append("\n");
 				
 				bufferText(stream.getClient(), string);
 			}
 			else {
 				getTextForClient(stream.getClient()).append(text);
 			}
 		}
 	}
 	
 	public synchronized void streamEchoed(IStream stream, String text) {
 		if (this.streams.contains(stream))
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
 	
 	public synchronized void streamReceivedCommand(IStream stream, ICommand command) {
 		if (this.streams.contains(stream))
 		{
 			IWarlockClient client = stream.getClient();
 			WarlockString string = new WarlockString(command.getText());
 			
 			string.addStyle(0, string.length(), client.getCommandStyle());
 			
 			if (bufferOnPrompt) {
 				if(!isPrompting && prompt != null)
 					appendText(client, new WarlockString(prompt));
 				else
 					isPrompting = false;
 			}
 			
 			appendText(client, string);
 		}
 	}
 	
 	public synchronized void streamPrompted(IStream stream, String prompt) {
 		if (!this.streams.contains(stream))
 			return;
 		
 		if(!isPrompting)
 		{
 			this.prompt = prompt;
 			
 			IWarlockClient client = stream.getClient();
 			
 			WarlockString text = new WarlockString();
 			
 			WarlockString bufferedText = textBuffers.get(client);
 			if (bufferedText != null)
 			{
 				text.append(bufferedText);
 				bufferedText.clear();
 				textBuffers.remove(client);
 			}
 			
 			if (!GameViewConfiguration.instance().getSuppressPrompt()) {
 				// FIXME: Shouldn't this be set unconditionally?
 				isPrompting = true;
 				
 				if(prompt != null)
 					text.append(prompt);
 			}
 			appendText(client, text);
 		} else {
 			// if the new prompt is the same as the old one, do nothing.
 			// if the new prompt is null, just print the newline.
 			if(prompt == null) {
 				if(this.prompt != null) {
 					appendText(stream.getClient(), new WarlockString("\n"));
 					this.prompt = prompt;
 				}
 			} else if(this.prompt == null || !this.prompt.equals(prompt)) {
 				appendText(stream.getClient(), new WarlockString("\n" + prompt));
 				this.prompt = prompt;
 			}	
 		}
 	}
 
 	public static Collection<StreamView> getOpenViews ()
 	{
 		return openViews;
 	}
 	
 	public synchronized void setClient (IWarlockClient client)
 	{
 		currentText = getTextForClient(client);
 		book.showPage(currentText.getTextWidget());
 		
 		if (StyleProviders.getStyleProvider(client) == null)
 			StyleProviders.setStyleProvider(client, DefaultStyleProvider.instance());
 	}
 	
 	@Override
 	public void dispose() {
 		
 		for (IStream stream : streams)
 		{
 			stream.removeStreamListener(streamListenerWrapper);
 			stream.getTitle().removeListener(propertyListenerWrapper);
 			stream.setView(false);
 		}
 		
 		clientStreams.clear();
 		GameView.removeGameViewFocusListener(this);
 		
 		if (openViews.contains(this)) {
 			openViews.remove(this);
 		}
 		
 		if (viewInFocus == this) {
 			viewInFocus = null;
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
 	
 	@Override
 	public void setPartName(String partName) {
 		super.setPartName(partName);
 	}
 	
 	public synchronized void streamFlush(IStream stream) {
 		WarlockString bufferedText = textBuffers.get(stream.getClient());
 		if(bufferedText != null) {
 			appendText(stream.getClient(), bufferedText);
 			bufferedText.clear();
 			textBuffers.remove(stream.getClient());
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
 	
 	public void pageUp() {
 		currentText.pageUp();
 	}
 	
 	public void pageDown() {
 		currentText.pageDown();
 	}
 	
 	public void componentUpdated(IStream stream, String id, WarlockString value) {
 		WarlockText text = getTextForClient(stream.getClient());
 		text.replaceMarker(id, value);
 	}
 	
 	public void scanClients() {
 		for (IWarlockClient client : WarlockClientRegistry.getActiveClients()) {
 			clientActivated(client);
 			//if (client.getConnection() == null) continue;
 			//if (client.getConnection().isConnected())
 			//	clientConnected(client);
 		}
 	}
 
 	public void clientActivated(IWarlockClient client) {
 		// TODO Auto-generated method stub
 		if (!(this instanceof GameView)) {
 			setClient(client);
 		}
 	}
 
 	public void clientConnected(IWarlockClient client) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void clientDisconnected(IWarlockClient client) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void clientRemoved(IWarlockClient client) {
 		// TODO Auto-generated method stub
 		
 	}
 }
