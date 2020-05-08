 /*
  * Created on Sep 17, 2004
  */
 package cc.warlock.rcp.views;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Random;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Caret;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 
 import cc.warlock.client.ICommand;
 import cc.warlock.client.IWarlockClient;
 import cc.warlock.client.internal.Command;
 import cc.warlock.client.stormfront.IStormFrontClient;
 import cc.warlock.client.stormfront.IStormFrontClientViewer;
 import cc.warlock.client.stormfront.WarlockColor;
 import cc.warlock.configuration.Profile;
 import cc.warlock.configuration.SavedProfiles;
 import cc.warlock.configuration.server.ServerSettings;
 import cc.warlock.configuration.skin.IWarlockSkin;
 import cc.warlock.rcp.actions.ProfileConnectAction;
 import cc.warlock.rcp.application.WarlockApplication;
 import cc.warlock.rcp.plugin.Warlock2Plugin;
 import cc.warlock.rcp.ui.WarlockCompass;
 import cc.warlock.rcp.ui.WarlockText;
 import cc.warlock.rcp.ui.client.SWTStormFrontClientViewer;
 import cc.warlock.rcp.ui.macros.IMacro;
 import cc.warlock.rcp.ui.macros.MacroRegistry;
 import cc.warlock.rcp.ui.macros.internal.SystemMacros;
 import cc.warlock.rcp.ui.style.CompassThemes;
 import cc.warlock.rcp.util.ColorUtil;
 
 /**
  * @author marshall
  */
 public class GameView extends StreamView implements KeyListener, IStormFrontClientViewer {
 
 	public static final String VIEW_ID = "cc.warlock.rcp.views.gameView";
 	
 	private static GameView firstInstance;
 	private static boolean firstInstanceIsUsed = false;
 	private static ArrayList<GameView> openViews = new ArrayList<GameView>();
 	private static GameView viewInFocus;
 	
 	protected StyledText entry;
 	protected WarlockCompass compass;
 	protected ICommand currentCommand;
 	
 	protected SWTStormFrontClientViewer wrapper;
 //	protected ViewEvents viewer;
 	private Font normalFont = JFaceResources.getDefaultFont();
 	
 	public GameView () {
 		if (firstInstance == null) {
 			firstInstance = this;
 		}
 		
 		currentCommand = new Command("", new Date());
 		wrapper = new SWTStormFrontClientViewer(this);
 //		viewer = new ViewEvents(client);
 		openViews.add(this);
 		
 		setStreamName(IWarlockClient.DEFAULT_STREAM_NAME);
 		
 	}
 	
 	@Override
 	public void init(IViewSite site) throws PartInitException {
 		super.init(site);
 		
 		if (WarlockApplication.instance().getStartWithProfile() != null)
 		{
 			autoConnect();
 		}
 	}
 	
 	protected void autoConnect ()
 	{
 		Profile connectToProfile = null;
 		
 		for (Profile profile : SavedProfiles.getAllProfiles())
 		{
 			if (WarlockApplication.instance().getStartWithProfile().equals(profile.getCharacterName()))
 			{
 				connectToProfile = profile;
 			}
 		}
 		
 		if (connectToProfile == null) /* TODO show a warning */ return;
 		
 		ProfileConnectAction action = new ProfileConnectAction(connectToProfile);
 		action.setGameView(this);
 		
 		action.run();
 	}
 	
 	public static Collection<GameView> getOpenGameViews ()
 	{
 		return openViews;
 	}
 	
 	public static GameView getViewInFocus ()
 	{
 		return viewInFocus;
 	}
 	
 	public static GameView createNext () {
 		if (firstInstance != null)	
 		{
 			if (!firstInstanceIsUsed)
 			{
 				firstInstanceIsUsed = true;
 				viewInFocus = firstInstance;
 				return firstInstance;
 			}
 			
 			else {
 				IWorkbenchPage page = firstInstance.getSite().getPage();
 				try {
 					GameView nextInstance = (GameView) page.showView(VIEW_ID, generateUniqueId(), IWorkbenchPage.VIEW_ACTIVATE);
 					viewInFocus = nextInstance;
 					return nextInstance;
 				} catch (PartInitException e) {
 					e.printStackTrace();
 				}	
 			}
 		}	
 		
 		return null;
 	}
 	
 	private static String generateUniqueId () {
 		return new Random().nextInt() + "";
 	}
 	
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 		
 		text.setLineLimit(2000);
 		text.setScrollDirection(SWT.DOWN);
 		text.addKeyListener(this);
 		
 //		text.addFocusListener(new FocusListener () { 
 //			public void focusGained(FocusEvent e) {
 //				entry.setFocus();
 //			}
 //			
 //			public void focusLost(FocusEvent e) {}
 //		});
 		
 		text.append(
 			"Hello, and welcome to Warlock 2!\n" + 
 			"To get started, you can connect to your play.net account by opening the \"Connect\" menu, and pressing \"New Connection\".\n" +
 			"Good luck, and happy hunting!\n\n" +
 			"Copyright (c) 2001-2007 " + WarlockText.OBJECT_HOLDER +
 			"\n\n");
 		
 		text.addLink("http://warlock.cc", "The Warlock Team");
 		
 		entry = new StyledText(mainComposite, SWT.BORDER | SWT.SINGLE);
 		entry.setLayoutData(new GridData(GridData.FILL, GridData.VERTICAL_ALIGN_END, true, false, 1, 1));
 		entry.setEditable(true);
 		entry.setLineSpacing(5);
 		entry.setIndent(5);
 		
 		compass = new WarlockCompass(text, CompassThemes.getCompassTheme("small"));
 		text.setBackgroundMode(SWT.INHERIT_DEFAULT);
 		
 		entry.addModifyListener(new ModifyListener () {
 			public void modifyText(ModifyEvent e) {
 				((Command)currentCommand).setCommand(entry.getText());
 			}
 		});
 		entry.addKeyListener(this);
 		
 		this.client = Warlock2Plugin.getDefault().getCurrentClient();
 	}
 	
 	public void setFocus() {
 		entry.setFocus();
 		
 		viewInFocus = this;
 	}
 		
 	private ProgressMonitorDialog settingsProgressDialog;
 	public void startDownloadingServerSettings() {
 		settingsProgressDialog = new ProgressMonitorDialog(getSite().getShell());
 		settingsProgressDialog.setBlockOnOpen(false);
 		settingsProgressDialog.open();
 		
 		IProgressMonitor monitor = settingsProgressDialog.getProgressMonitor();
 		monitor.beginTask("Downloading server settings...", SettingType.values().length);
 	}
 	
 	public void receivedServerSetting(SettingType settingType)
 	{
 		IProgressMonitor monitor = settingsProgressDialog.getProgressMonitor();
 		monitor.subTask("Downloading " + settingType.toString() + "...");
 		
 		monitor.worked(1);
 	}
 	
 	public void finishedDownloadingServerSettings() {
 		IProgressMonitor monitor = settingsProgressDialog.getProgressMonitor();
 		monitor.done();
 		settingsProgressDialog.close();
 	}
 	
 	private Image createCaretImage (int width, Color foreground)
 	{
 		PaletteData caretPalette = new PaletteData(new RGB[] {
 				new RGB(0, 0, 0), new RGB(255, 255, 255) });
 		
 		int widthOffset = width - 1;
 		ImageData imageData = new ImageData(4 + widthOffset, entry
 				.getLineHeight(), 1, caretPalette);
 		Display display = entry.getDisplay();
 		Image bracketImage = new Image(display, imageData);
 		GC gc = new GC(bracketImage);
 		gc.setForeground(foreground);
 		gc.setLineWidth(1);
 		// gap between two bars of one third of the height
 		// draw boxes using lines as drawing a line of a certain width produces
 		// rounded corners.
 		for (int i = 0; i < width; i++) {
 			gc.drawLine(i, 0, i, imageData.height - 1);
 		}
 
 		gc.dispose();
 
 		return bracketImage;
 	}
 
 	private Caret createCaret (int width, Color foreground) {
 		Caret caret = new Caret(entry, SWT.NULL);
 		Image image = createCaretImage(width, foreground);
 		
 		if (image != null)
 			caret.setImage(image);
 		else
 			caret.setSize(width, entry.getLineHeight());
 
 		caret.setFont(entry.getFont());
 
 		return caret;
 	}
 	
 	public void loadServerSettings (final ServerSettings settings)
 	{
 		WarlockColor bg = settings.getColorSetting(IWarlockSkin.ColorType.MainWindow_Background);
 		WarlockColor fg = settings.getColorSetting(IWarlockSkin.ColorType.MainWindow_Foreground);
 		
 		String fontFace = settings.getFontFaceSetting(IWarlockSkin.FontFaceType.MainWindow_FontFace);
 		int fontSize = settings.getFontSizeSetting(IWarlockSkin.FontSizeType.MainWindow_FontSize);
 		
 		normalFont = fontFace == null ? JFaceResources.getDefaultFont() : new Font(getSite().getShell().getDisplay(), fontFace, fontSize, SWT.NONE);
 		text.setFont(normalFont);
 		
 		WarlockColor entryBG = settings.getColorSetting(IWarlockSkin.ColorType.CommandLine_Background);
 		WarlockColor entryFG = settings.getColorSetting(IWarlockSkin.ColorType.CommandLine_Foreground);
 		entry.setForeground(ColorUtil.warlockColorToColor(entryFG.equals(WarlockColor.DEFAULT_COLOR) ? fg  : entryFG));
 		entry.setBackground(ColorUtil.warlockColorToColor(entryBG.equals(WarlockColor.DEFAULT_COLOR) ? bg : entryBG));
 		
 		Caret newCaret = createCaret(1, ColorUtil.warlockColorToColor(settings.getColorSetting(IWarlockSkin.ColorType.CommandLine_BarColor)));
 		entry.setCaret(newCaret);
 		
 		text.setBackground(ColorUtil.warlockColorToColor(bg));
 		text.setForeground(ColorUtil.warlockColorToColor(fg));
 		
 		SystemMacros.addMacrosFromServerSettings(settings);
 		
 		if (HandsView.getDefault() != null)
 		{
 			HandsView.getDefault().loadServerSettings(settings);
 		}
 		if (StatusView.getDefault() != null)
 		{
 			StatusView.getDefault().loadServerSettings(settings);
 		}
 	}
 	
 	public void setViewerTitle(String title) {
 		setPartName(title);
 	}
 	
 	public ICommand getCurrentCommand ()
 	{
 		return GameView.this.currentCommand;
 	}
 	
 	public void setCurrentCommand(ICommand command) {
 		GameView.this.currentCommand = command;
 		GameView.this.entry.setText(command.getCommand());
 		GameView.this.entry.setSelection(command.getCommand().length());
 	}
 	
 	public void setStormFrontClient(IStormFrontClient client) {
 		this.client = client;
 		
 		setMainStream(client.getDefaultStream());
 		addStream(client.getStream(IStormFrontClient.DEATH_STREAM_NAME));
 		
 		client.addViewer(wrapper);
 		
 //		text.setText("");
 		
 		compass.setCompass(client.getCompass());
 		
 		if (HandsView.getDefault() != null)
 			HandsView.getDefault().setClient(client);
 		
 		if (BarsView.getDefault() != null)
 			BarsView.getDefault().init(client);
 		
 		for (StreamView streamView : StreamView.getOpenViews())
 		{
 			if (streamView != this) {
 				streamView.setClient(client);
 			}
 		}
 	}
 	
 	public IWarlockClient getWarlockClient() {
 		return client;
 	}
 	
 	public IStormFrontClient getStormFrontClient() {
 		return client;
 	}
 
 	public void keyPressed(KeyEvent e) {
 		Collection<IMacro> macros = MacroRegistry.instance().getMacros();
 		e.doit = true;
 		
 		for (IMacro macro : macros)
 		{
 			if (macro.getKeyCode() == e.keyCode && macro.getModifiers() == e.stateMask)
 			{
 				 try {
 					macro.execute(wrapper);
 				} catch (Exception ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 				
 				e.doit = false;
 				break;
 			}
 		}
 	}
 	
	public void keyReleased(KeyEvent e) {	}
 }
