 package org.dyndns.schuschu.xmms2client.view.window;
 
 import org.dyndns.schuschu.xmms2client.action.FooPluginActionFilter;
 import org.dyndns.schuschu.xmms2client.action.FooPluginActionPlaylist;
 import org.dyndns.schuschu.xmms2client.backend.FooPluginBackendMedia;
 import org.dyndns.schuschu.xmms2client.backend.FooPluginBackendMediaPlaylist;
 import org.dyndns.schuschu.xmms2client.backend.FooPluginBackendPlaylist;
 import org.dyndns.schuschu.xmms2client.view.element.FooCombo;
 import org.dyndns.schuschu.xmms2client.view.element.FooList;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 
 import se.fnord.xmms2.client.Client;
 
 public class FooWindowSWT {
 
 	final static int WIDTH = 600;
 	final static int HEIGHT = 400;
 
 	Client client;
 
 	private Display display = new Display();
 
 	boolean visible = false;
 
 	private Shell sShell = null;
 	private SashForm sashFormMain = null;
 	private SashForm sashFormSubLeft = null;
 	private SashForm sashFormSubRight = null;
 	private Composite compositePlaylist = null;
 	private FooList listArtist = null;
 	private FooList listAlbum = null;
 	private FooList listTrack = null;
 	private FooList listPlaylist = null;
 	private FooCombo comboPlaylist = null;
 
 	/**
 	 * This method initializes sShell
 	 */
 
 	public FooWindowSWT(Client client) {
 		this.client = client;
 		initalize();
 	}
 
 	public void setVisible(boolean visible) {
 		this.visible = visible;
 		if (visible) {
 			getsShell().open();
 		} else {
 			getsShell().close();
 		}
 	}
 
 	public void toggleVisible() {
 		visible = !visible;
 		setVisible(visible);
 	}
 
 	public void initalize() {
 		createSShell();
 
 		// here starts the magic (content chaining)
 		listArtist.getBackend().setToAll();
 		listAlbum.getBackend().setContentProvider(listArtist.getBackend());
 		listTrack.getBackend().setContentProvider(listAlbum.getBackend());
 
 		listPlaylist.getBackend().setContentProvider(comboPlaylist.getBackend());
 		// TODO: remove workaround to load content on startup;
		comboPlaylist.getBackend().refresh();
 	}
 
 	private void createSShell() {
 		setsShell(new Shell(getDisplay()));
 		getsShell().setText("fooXmms2");
 		getsShell().setSize(new Point(WIDTH, HEIGHT));
 		createSashFormMain();
 		getsShell().setLayout(new FillLayout());
 	}
 
 	private void createSashFormMain() {
 		sashFormMain = new SashForm(getsShell(), SWT.NONE);
 		createSashFormSubLeft();
 		createSashFormSubRight();
 	}
 
 	private void createSashFormSubLeft() {
 		sashFormSubLeft = new SashForm(sashFormMain, SWT.NONE);
 
 		listArtist = new FooList(sashFormSubLeft, SWT.BORDER | SWT.MULTI
 				| SWT.V_SCROLL);
 		FooPluginBackendMedia artist_backend = new FooPluginBackendMedia(
 				"%artist%", "artist", client, listArtist);
 		listArtist.setBackend(artist_backend);
 		FooPluginActionFilter artist_action = new FooPluginActionFilter(
 				artist_backend);
 		artist_action.addListeners();
 
 		listAlbum = new FooList(sashFormSubLeft, SWT.BORDER | SWT.MULTI
 				| SWT.V_SCROLL);
 		// I still prefer album (date) but i don't realy care that much
 		FooPluginBackendMedia album_backend = new FooPluginBackendMedia(
 				"%date% - %album%", "album", client, listAlbum);
 		listAlbum.setBackend(album_backend);
 		FooPluginActionFilter album_action = new FooPluginActionFilter(
 				album_backend);
 		album_action.addListeners();
 	}
 
 	private void createSashFormSubRight() {
 		sashFormSubRight = new SashForm(sashFormMain, SWT.NONE);
 
 		listTrack = new FooList(sashFormSubRight, SWT.BORDER | SWT.MULTI
 				| SWT.V_SCROLL);
 
 		FooPluginBackendMedia track_backend = new FooPluginBackendMedia(
 				"%title%", "title", client, listTrack);
 		listTrack.setBackend(track_backend);
 
 		FooPluginActionFilter action = new FooPluginActionFilter(track_backend);
 		action.addListeners();
 
 		createCompositePlaylist();
 	}
 
 	private void createCompositePlaylist() {
 		compositePlaylist = new Composite(sashFormSubRight, SWT.NONE);
 		FormLayout layout = new FormLayout();
 		compositePlaylist.setLayout(layout);
 
 		comboPlaylist = new FooCombo(compositePlaylist, SWT.READ_ONLY);
 		FormData comboData = new FormData();
 		comboData.top = new FormAttachment(0, 0);
 		comboData.left = new FormAttachment(0, 0);
 		comboData.right = new FormAttachment(100, 0);
 		comboPlaylist.setLayoutData(comboData);
 		
 		 FooPluginBackendPlaylist backend = new FooPluginBackendPlaylist(
 				 client, comboPlaylist);
 		 comboPlaylist.setBackend(backend);
 
 		listPlaylist = new FooList(compositePlaylist, SWT.BORDER | SWT.MULTI
 				| SWT.V_SCROLL);
 		FormData listData = new FormData();
 		listData.top = new FormAttachment(comboPlaylist.getCombo(), 0);
 		listData.left = new FormAttachment(0, 0);
 		listData.right = new FormAttachment(100, 0);
 		listData.bottom = new FormAttachment(100, 0);
 		listPlaylist.setLayoutData(listData);
 
 		FooPluginBackendMediaPlaylist list_backend = new FooPluginBackendMediaPlaylist(
 				"%artist% - %title%", "title", client, listPlaylist);
 
 		listPlaylist.setBackend(list_backend);
 
 		FooPluginActionPlaylist list_action = new FooPluginActionPlaylist(
 				list_backend);
 		list_action.addListeners();
 	}
 
 	public void setsShell(Shell sShell) {
 		this.sShell = sShell;
 	}
 
 	public Shell getsShell() {
 		return sShell;
 	}
 
 	public void setDisplay(Display display) {
 		this.display = display;
 	}
 
 	public Display getDisplay() {
 		return display;
 	}
 }
