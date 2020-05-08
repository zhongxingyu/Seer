 package com.arcaner.warlock.rcp.views;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Hashtable;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.LineBackgroundEvent;
 import org.eclipse.swt.custom.LineBackgroundListener;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 
 import com.arcaner.warlock.client.IProperty;
 import com.arcaner.warlock.client.IStream;
 import com.arcaner.warlock.client.IStreamListener;
 import com.arcaner.warlock.client.IStyledString;
 import com.arcaner.warlock.client.IWarlockStyle;
 import com.arcaner.warlock.client.PropertyListener;
 import com.arcaner.warlock.client.stormfront.IStormFrontClient;
 import com.arcaner.warlock.client.stormfront.WarlockColor;
 import com.arcaner.warlock.configuration.server.HighlightString;
 import com.arcaner.warlock.configuration.server.ServerSettings;
 import com.arcaner.warlock.configuration.skin.IWarlockSkin;
 import com.arcaner.warlock.rcp.ui.StyleRangeWithData;
 import com.arcaner.warlock.rcp.ui.WarlockText;
 import com.arcaner.warlock.rcp.ui.client.SWTPropertyListener;
 import com.arcaner.warlock.rcp.ui.client.SWTStreamListener;
 import com.arcaner.warlock.rcp.ui.style.StyleMappings;
 
 public class StreamView extends ViewPart implements IStreamListener, LineBackgroundListener {
 	
 	public static final String STREAM_VIEW_PREFIX = "com.arcaner.warlock.rcp.views.stream.";
 	public static final String DEATH_VIEW_ID =  STREAM_VIEW_PREFIX + IStormFrontClient.DEATH_STREAM_NAME;
 	public static final String INVENTORY_VIEW_ID = STREAM_VIEW_PREFIX  + IStormFrontClient.INVENTORY_STREAM_NAME;
 	public static final String THOUGHTS_VIEW_ID = STREAM_VIEW_PREFIX + IStormFrontClient.THOUGHTS_STREAM_NAME ;
 	
 	protected static ArrayList<StreamView> openViews = new ArrayList<StreamView>();
 	
 	protected IStream mainStream;
 	protected ArrayList<IStream> streams;
 	protected IStormFrontClient client;
 	protected WarlockText text;
 	protected Hashtable<Integer, Color> lineBackgrounds = new Hashtable<Integer,Color>();
 	protected Hashtable<Integer, Color> lineForegrounds = new Hashtable<Integer,Color>();
 	protected Composite mainComposite;
 	// This name is the 'suffix' part of the stream... so we will install listeners for each client
 	protected String mainStreamName;
 	protected SWTStreamListener streamListenerWrapper;
 	protected SWTPropertyListener<String> propertyListenerWrapper;
 	protected boolean appendNewlines = false;
 	
 	public StreamView() {
 		openViews.add(this);
 		streamListenerWrapper = new SWTStreamListener(this);
 		streams = new ArrayList<IStream>();
 	}
 
 	public static StreamView getViewForStream (String streamName) {
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
 			StreamView nextInstance = (StreamView) page.showView(STREAM_VIEW_PREFIX + streamName);
 			nextInstance.setStreamName(streamName);
 			
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
 		
 		text = new WarlockText(mainComposite, SWT.V_SCROLL);
 		text.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		text.setEditable(false);
 		text.setWordWrap(true);
 		text.setBackground(new Color(text.getDisplay(), 25, 25, 50));
 		text.setForeground(new Color(text.getDisplay(), 240, 240, 255));
 		text.addLineBackgroundListener(this);
 		
 		GameView currentGameView = GameView.getViewInFocus();
 		if (currentGameView != null && currentGameView.getStormFrontClient() != null)
 		{
 			setPartName(currentGameView.getStormFrontClient().getStream(mainStreamName).getTitle().get());
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
 	}
 	
 	public void addStream (IStream stream) {
 		stream.addStreamListener(streamListenerWrapper);
 		streams.add(stream);
 	}
 	
 	public void streamCleared(IStream stream) {
 		if (this.mainStream.equals(stream))
 			text.setText("");
 	}
 	
 	private void scrollToBottom ()
 	{
 		int length = this.text.getContent().getCharCount();		
 		if (this.text.getCaretOffset() < length) {
 			this.text.setCaretOffset(length);
 			this.text.showSelection();
 		}
 	}
 	
 	private void applyUserHighlights (StyleRange parentStyle, String text, int start, int lineIndex)
 	{
 		Font font = this.text.getFont();
 		if (parentStyle != null)
 		{
 			if (parentStyle.font != null)
 				font = parentStyle.font;
 		}
 		
 		for (HighlightString highlight : client.getServerSettings().getHighlightStrings())
 		{
 			int highlightLength = highlight.getText().length();
 			int index = text.indexOf(highlight.getText());
 			while (index > -1)
 			{
 				StyleRangeWithData range = new StyleRangeWithData();
 				range.background = createColor(highlight.getBackgroundColor());
 				range.foreground = createColor(highlight.getForegroundColor());
 				range.start = start + index;
 				range.length = highlightLength;
 				range.font = font;
 				
 				if (highlight.isFillEntireLine())
 				{
 					lineBackgrounds.put(lineIndex, range.background);
 					lineForegrounds.put(lineIndex, range.foreground);
 				}
 				
 				this.text.setStyleRange(range);
 				index = text.indexOf(highlight.getText(), index+1);
 			}
 		}
 	}
 	
 	public void streamReceivedText(IStream stream, IStyledString string) {
 		if (this.mainStream.equals(stream) || this.streams.contains(stream))
 		{
 			String streamText = string.getBuffer().toString();
 			
 			if (appendNewlines)
 				streamText += "\n";
 			
 			int charCount = this.text.getCharCount();
 			StyleRangeWithData ranges[] = new StyleRangeWithData[string.getStyles().size()];
 			int i = 0;
 			for (IWarlockStyle style : string.getStyles())
 			{
 				ranges[i] = StyleMappings.getStyle(client.getServerSettings(), style, charCount + style.getStart(), style.getLength());
 				if (style.getStyleTypes().contains(IWarlockStyle.StyleType.LINK))
 				{
 					ranges[i].data.put("link.url", style.getLinkAddress().toString());
 					// skip any leading spaces
 					int j = 0;
 					while (string.getBuffer().charAt(j) == ' ') j++;
 					ranges[i].start = ranges[i].start + j;
 				}
 				i++;
 			}
 			
 			this.text.append(streamText);
 			
			boolean userHighlightsApplied = false;
			
 			for (StyleRangeWithData range : ranges)
 			{
 				if (range != null) {
 					int lineIndex = this.text.getLineAtOffset(range.start);
 					this.text.setStyleRange(range);
 					if (range.data.containsKey(StyleMappings.FILL_ENTIRE_LINE))
 					{
 						lineBackgrounds.put(lineIndex, range.background);
 						if (range.foreground != null)
 							lineForegrounds.put(lineIndex, range.foreground);
 					}
					
 					applyUserHighlights(range, streamText, range.start, lineIndex);
					userHighlightsApplied = true;
 				}
 			}
 			
			if (!userHighlightsApplied) {
				applyUserHighlights(null, streamText, charCount, this.text.getLineAtOffset(charCount));
			}
			
 			scrollToBottom();
 		}
 	}
 	
 	public void lineGetBackground(LineBackgroundEvent event) {
 		int lineIndex = this.text.getLineAtOffset(event.lineOffset);
 		boolean hasBackground = lineBackgrounds.containsKey(lineIndex);
 		boolean hasForeground = lineForegrounds.containsKey(lineIndex);
 		
 		if (hasBackground)
 		{
 			event.lineBackground = lineBackgrounds.get(lineIndex);
 		}
 		
 //		if (hasForeground)
 //		{
 //			StyleRange lineRange = new StyleRange();
 //			lineRange.start = event.lineOffset;
 //			lineRange.length = event.lineText.length();
 //			lineRange.foreground = lineForegrounds.get(lineIndex);
 //			
 //			this.text.setStyleRange(lineRange);
 //		}
 	}
 	
 	public void streamEchoed(IStream stream, String text) {
 		if (this.mainStream.equals(stream) || this.streams.contains(stream))
 		{
 			StyleRange echoStyle = StyleMappings.getEchoStyle(client.getServerSettings(), this.text.getCharCount(), text.length());
 			this.text.append(text + "\n");
 			this.text.setStyleRange(echoStyle);
 			
 			scrollToBottom();
 		}
 	}
 	
 	public void streamPrompted(IStream stream, String prompt) {
 		if (this.mainStream.equals(stream) || this.streams.contains(stream))
 		{
 			this.text.append(prompt);
 			scrollToBottom();
 		}
 	}
 
 	public static Collection<StreamView> getOpenViews ()
 	{
 		return openViews;
 	}
 	
 	private Color createColor (WarlockColor color)
 	{
 		return new Color(getSite().getShell().getDisplay(), color.getRed(), color.getGreen(), color.getBlue());
 	}
 	
 	public void setClient (IStormFrontClient client)
 	{
 		this.client = client;
 		
 		setMainStream(client.getStream(mainStreamName));
 	}
 	
 	public void loadServerSettings (ServerSettings settings)
 	{
 		// just inherit from the main window for now
 		WarlockColor bg = settings.getColorSetting(IWarlockSkin.ColorType.MainWindow_Background);
 		WarlockColor fg = settings.getColorSetting(IWarlockSkin.ColorType.MainWindow_Foreground);
 		String fontFace = settings.getFontFaceSetting(IWarlockSkin.FontFaceType.MainWindow_FontFace);
 		int fontSize = settings.getFontSizeSetting(IWarlockSkin.FontSizeType.MainWindow_FontSize);
 		
 		text.setBackground(createColor(bg));
 		text.setForeground(createColor(fg));
 		
 		Font normalFont = new Font(getSite().getShell().getDisplay(), fontFace, fontSize, SWT.NONE);
 		text.setFont(normalFont);
 	}
 	
 	@Override
 	public void dispose() {
 		if (mainStream != null) {
 			mainStream.removeStreamListener(streamListenerWrapper);
 			mainStream.getTitle().removeListener(propertyListenerWrapper);
 		}
 		
 		for (IStream stream : streams)
 		{
 			stream.removeStreamListener(streamListenerWrapper);
 		}
 		
 		super.dispose();
 	}
 
 	@Override
 	public Object getAdapter(Class adapter) {
 		if (IStormFrontClient.class.equals(adapter))
 		{
 			return client;
 		}
 		return super.getAdapter(adapter);
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
 }
