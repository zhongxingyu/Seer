 /**
  *  -----------------------------------------------------------------------------
  *  
  *  Holo-Edit, spatial sound trajectories editor, part of Holophon
  *  Copyright (C) 2006 GMEM
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
  *  
  *  -----------------------------------------------------------------------------
  */
 package holoedit;
 
 import holoedit.data.HoloRecentFile;
 import holoedit.fileio.HoloSettings;
 import holoedit.gui.AboutDialog;
 import holoedit.gui.CounterPanel;
 import holoedit.gui.ConnectionPanel;
 import holoedit.gui.GestionPistes;
 import holoedit.gui.HoloMenuBar;
 import holoedit.gui.PrefPane;
 import holoedit.gui.Room3DGUI;
 import holoedit.gui.RoomGUI;
 import holoedit.gui.ScoreGUI;
 import holoedit.gui.SoundPoolGUI;
 import holoedit.gui.TimeEditorGUI;
 import holoedit.gui.TransportPanel;
 import holoedit.gui.HelpWindow;
 import holoedit.rt.Connection;
 import holoedit.rt.OSCConnection;
 import holoedit.util.Ut;
 import java.awt.Dimension;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.Vector;
 import javax.media.opengl.GL;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLCapabilities;
 import javax.media.opengl.GLDrawableFactory;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.GLCanvas;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import com.cycling74.max.MaxObject;
 import com.sun.opengl.util.FPSAnimator;
 
 // FEATURE MAIN IMPORT/EXPORT AS SIMPLE POINT LIST (EXCEL TREATMENTS)
 // FEATURE MAIN SAUVEGARDE AUTOMATIQUE
 // FEATURE MAIN HISTORIQUE ANNULATION
 public class HoloEdit implements UncaughtExceptionHandler
 {
 	public static String buildDate = "01/02/2011";
 	public static Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
 	
 	/* standalone ?? */
 	boolean standalone;
 	
 	/* zone de gestion des pistes */
 	public GestionPistes gestionPistes;
 	/* compteurs */
 	public CounterPanel counterPanel;
 	public ConnectionPanel connectionPanel;
 	/* Transport */
 	public TransportPanel transport;
 	/* editeur temporel */
 	public TimeEditorGUI timeEditor;
 	/* sound pool */
 	public SoundPoolGUI soundPoolGui;
 	/* Room */
 	public RoomGUI room;
 	/* 3D room */
 	public Room3DGUI room3d;
 	/* Score */
 	public ScoreGUI score;
 	/* Fenetre d'aide */
 	public HelpWindow helpWindow;
 	/* OSC */
 	public Connection connection;
 	/* Menu Window for WINDOWS */
 	public JFrame mainFrame;
 	public AboutDialog aboutBox;
 	public PrefPane prefs;
 	
 	public HoloSettings settings;
 	// look and feel
 	public int laf = 1;
 	// choix de coordonnees (cartesiennes/polaires)
 	public int coordinates = 0;
 	// nombre maximum de fichier recents
 	public int maxRecentFiles = 10;
 	// vecteur de fichiers recents
 	public Vector<HoloRecentFile> recentFiles = new Vector<HoloRecentFile>();
 	// dernier fichier lus avec la derniere fermeture de l'appli
 	public HoloRecentFile last;
 	// dernier script ouvert dans l'editeur d'algo groovy
 	public java.io.File lastScriptFile = new java.io.File("");
 	public String openFileName = null;
 	public boolean readyToOpen = false;
 	// booleen qui charge automatique le dernier fichier lu a la prochaine ouverture de l'appli
 	public boolean openLastOnLoad = false;
 	// boolean qui memorise la visibilite des haut-parleurs
 	public boolean viewSpeakers = true;
 	// boolean qui memorise la possibilit� de voir les options
 	// avanc�e lors de l'importation de SDIF
 	public boolean sdifExpert = false;
 	// boolean de sauvegarde (permet de savoir si une modification a ete effectuee)
 	protected boolean savedState = true;
 	// sensibilite du scroll %
 	public float scrollSpeed = 100;
 //	Position & Sizes
 	public int padW = 10;
 	public int padH = 10;
 	// Preferences box
 	public int wsPrefH = 450;
 	public int wsPrefW = 400;
 	public int wlPrefX = (screenSize.width - wsPrefW) / 2;
 	public int wlPrefY = (screenSize.height - wsPrefH) / 2;
 	// About box
 	public int wsAboutW = 420;
 	public int wsAboutH = 600;
 	public int wlAboutX = (screenSize.width - wsAboutW) / 2;
 	public int wlAboutY = (screenSize.height - wsAboutH) / 2;
 	// Soundpool
 	public boolean wbSoundPool = false;
 	public int wsSoundPoolW = 700;
 	public int wsSoundPoolH = 400;
 	public int wlSoundPoolX = (screenSize.width - wsSoundPoolW) / 2;
 	public int wlSoundPoolY = (screenSize.height - wsSoundPoolH) / 2;
 	// Room
 	public boolean wbRoom = true;
 	public int wsRoomW = 580;
 	public int wsRoomH = 641;
 	public int wlRoomX = (screenSize.width - wsRoomW) / 2;
 	public int wlRoomY = (screenSize.height - wsRoomH) / 2;
 	// Room3D
 	public boolean wbRoom3D = false;
 	public int wsRoomW3D = 309;
 	public int wsRoomH3D = 325;
 	public int wlRoomX3D = wlRoomX + wsRoomW + padW;
 	public int wlRoomY3D = screenSize.height - wlRoomY - wsRoomH3D;
 	// Score
 	public boolean wbScore = false;
 	public int wsScoreW = 794;
 	public int wsScoreH = 643 ;
 	public int wlScoreX = wlRoomX;
 	public int wlScoreY = wlRoomY;
 	// TrackSelector / Counters / Transport
 	public boolean wbTrackSel = true;
 	public int wsTrackSelW = 163;
 	public int wsTrackSelH = 324;
 	public boolean wbTrans = true;
 	public int wsTransW = 195;
 	public int wsTransH = 223;
 	public int wlTrackSelX = wlRoomX - padW - wsTrackSelW;
 	public int wlTrackSelY = wlRoomY;
 	public int wlTransX = wlRoomX - padW - wsTransW;
 	public int wlTransY = screenSize.height - (screenSize.height - wsRoomH) / 2 - wsTransH;
 	// Time Editor
 	public boolean wbTime = false;
 	public int wsTimeW = 794;
 	public int wsTimeH = 643;
 	public int wlTimeX = wlRoomX;
 	public int wlTimeY = wlRoomY;
 	// HelpWindow
 	public boolean helpWindowOpened = false;
 	public int helpWindowW = 780;
 	public int helpWindowH = 450;
 	public int helpWindowX = wlRoomX;
 	public int helpWindowY = wlRoomY;
 	
 	// TESTIN
 	//public GLPbuffer glpb;
 	public GLCanvas glpb;
 	public GLCapabilities glcap;
 	public static boolean SMOOTH = true;
 	public FPSAnimator rtDisplay;
 	public int	displayFPS = 10;
 	public boolean hpEditMode;
 	public boolean allTrackActive = false;
 	public boolean shortViewMode;
 	public boolean viewOnlyEditablePoints = false;
 	public boolean bonjour = false;
 	public boolean bonjourInstalled = false;
 	
 	public HoloEdit(boolean standalone)
 	{
 		this.standalone = standalone;
 		
 		Thread.currentThread().setUncaughtExceptionHandler(this);
 		Thread.currentThread().setName("Holo-Edit");
 		// JOGL INIT
 		glcap = new GLCapabilities();
 		// TESTIN
 		//glpb = GLDrawableFactory.getFactory().createGLPbuffer(glcap, null, 1, 1, null);
 		glpb = new GLCanvas(glcap);
 		
 		rtDisplay = new FPSAnimator(displayFPS);
 		rtDisplay.setIgnoreExceptions(false);
 		rtDisplay.setPrintExceptions(true);
 		glpb.addGLEventListener(new GLEventListener()
 		{
 			public void init(GLAutoDrawable arg0)
 			{
 				GL gl = arg0.getGL();
 				String vendor = gl.glGetString(GL.GL_VENDOR);
 				System.out.println("Vendor: " + vendor);
 				String renderer = gl.glGetString(GL.GL_RENDERER);
 				System.out.println("Renderer: " + renderer);
 			}
 
 			public void display(GLAutoDrawable arg0) {}
 
 			public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {}
 
 			public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {}
 			
 		});	
 		glpb.display();
 		try
 		{
 			// On recupere le nom du system d'exploitation et on affecte les variables en fonction
 			Ut.APP_PATH = System.getProperty("user.dir");
 			Ut.hv = "Holo-Edit v" + Ut.version;
 			Ut.print( ".. GO on system "+System.getProperty("os.name"));
 			Ut.MAC = System.getProperty("os.name").indexOf("Mac") >= 0;
 			Ut.LINUX = System.getProperty("os.name").indexOf("Linux") >= 0;
 			Ut.WIN = !Ut.MAC && !Ut.LINUX;
 			
 			if (Ut.MAC)
 			{
 				Ut.dirCar = "/";
				Ut.numCar = '�';
				Ut.numCar2 = '�';
 				
 			} else if (Ut.LINUX)
 			{
 				Ut.dirCar = "/";
				Ut.numCar = '°';
				Ut.numCar2 = '�';
 				
 			}else {
 				Ut.dirCar = "\\";
				Ut.numCar = '�';
				Ut.numCar2 = '�';
 			}
 //				if(bonjour && !standalone)
 //				{
 //					try{
 //						ClassLoader.getSystemClassLoader().loadClass("com.apple.dnssd.DNSSD");
 //						connection = (Connection)ClassLoader.getSystemClassLoader().loadClass("holoedit.rt.BonjourConnection").getConstructor(new Class[]{this.getClass()}).newInstance(new Object[]{this});
 //					} catch(ClassNotFoundException cnfe) {
 //						Ut.alert("Error","Bonjour has not been installed on this computer, see documentation.");
 //						connection = new OSCConnection(this);
 //						bonjourInstalled = false;
 //						bonjour = false;
 //					}
 //				}else
 				{
 					connection = new OSCConnection(this);
 					bonjourInstalled = false;
 					bonjour = false;
 				}
 				
 			counterPanel = new CounterPanel(this);
 			connectionPanel = new ConnectionPanel(this);
 			timeEditor = new TimeEditorGUI(this);
 			transport = new TransportPanel(this);
 			gestionPistes = new GestionPistes(this);
 			//Ut.post("preGUI");
 			soundPoolGui = new SoundPoolGUI(this);
 			
 			score = new ScoreGUI(this);
 			room = new RoomGUI(this);
 			room3d = new Room3DGUI(this);
 			helpWindow = new HelpWindow(this);
 			if(standalone) Ut.barMenu = new HoloMenuBar(this,null);
 			gestionPistes.selectTrack(0);
 			
 			Ut.post("preconfigg");
 			// Lecture de la configuraton
 			settings = new HoloSettings(this);
 			settings.load();
 			if (wbRoom)
 				room.open();
 			if (wbTrackSel)
 				gestionPistes.ts.open();
 			if (wbTrans)
 				transport.open();
 			if (wbSoundPool)
 				soundPoolGui.open();
 			if (wbRoom3D)
 				room3d.open();
 			if (wbScore)
 				score.open();
 			if (wbTime)
 				timeEditor.open();
 			readyToOpen = true;
 			if(openFileName != null)
 				gestionPistes.readDroppedFile(openFileName);
 			else 
 				openLast();
 			if(settings.focus.equalsIgnoreCase("room")) {
 				room.toFront();
 				room.requestFocus();
 			} else if(settings.focus.equalsIgnoreCase("room3d")) {
 				room3d.toFront();
 				room3d.requestFocus();
 			} else if(settings.focus.equalsIgnoreCase("score")) {
 				score.toFront();
 				score.requestFocus();
 			} else if(settings.focus.equalsIgnoreCase("time")) {
 				timeEditor.toFront();
 				timeEditor.requestFocus();
 			}
 			if(!Ut.MAC)
 			{
 				mainFrame = new JFrame(Ut.hv+" - Menu");
 				mainFrame.setSize(room.getWidth(),50);
 				mainFrame.setLocation(room.getX(), room.getY()-55);
 				mainFrame.setJMenuBar(Ut.barMenu);
 				mainFrame.setAlwaysOnTop(true);
 				mainFrame.setVisible(true);
 				mainFrame.setResizable(false);
 				mainFrame.toFront();
 				mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 				mainFrame.addWindowListener(new WindowAdapter()
 				{
 					public void windowClosing(WindowEvent e)
 					{
 						close();
 					}
 					public void windowActivated(WindowEvent e)
 					{
 						mainFrame.setJMenuBar(Ut.barMenu);
 					}
 				});
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			// Superbe message d'erreur qui permet a l'utilisateur de savoir exactement ce qui c'est passe.
 			JOptionPane.showMessageDialog(null, "Holo-Edit encountered an error,\nplease see the console\n(Application/Utilities/Console.app)\nfor more informations.", "Error !", JOptionPane.ERROR_MESSAGE);
 			System.exit(1);
 		}
 	}
 
 	private void openLast()
 	{
 		// Si l'option de chargement automatique du dernier fichier lu est
 		// active dans le fichier de configuration
 		// alors on le lit
 		if (openLastOnLoad)
 		{
 			if(last != null)
 				try
 				{
 					// suivant le type du fichier
 					switch (last.ft)
 					{
 					case 0: // mid
 						gestionPistes.readFile14b(Ut.dir(last.fd) + last.fn);
 						gestionPistes.holoFilename = null;
 						break;
 					case 1: // midi7
 						gestionPistes.readFile7bp(Ut.dir(last.fd) + last.fn);
 						gestionPistes.holoFilename = null;
 						break;
 					case 2: // txt
 						gestionPistes.readTextFile2(Ut.dir(last.fd) + last.fn);
 						gestionPistes.holoFilename = null;
 						break;
 					case 3: // seq~
 						gestionPistes.readSeqFileDirect(Ut.dir(last.fd) + last.fn);
 						gestionPistes.holoFilename = null;
 						break;
 					case 4: // holo
 						gestionPistes.readHoloFile(Ut.dir(last.fd) + last.fn);
 						gestionPistes.holoFilename = last.fn;
 						gestionPistes.holoDirectory = last.fd;
 					default:
 						break;
 					}
 					setTitle(last.fn);
 				gestionPistes.setDirty(true);
 				}
 				catch (Exception e)
 				{
 					Ut.alert("Error","Error while trying to load last opened file.");
 				}
 		} else {
 			counterPanel.setCompteur(1,0);
 			counterPanel.setCompteur(2,100);
 		}
 	}
 
 	static public void main(String[] args)
 	{
 		System.out.println("GO GO GO Holo-Edit !!!");
 		try
 		{
 			System.loadLibrary("jogl");
 		} catch (UnsatisfiedLinkError ule)
 		{
 			Ut.alert("Jogl Error", "Jogl ");
 			
 			System.out.println("� No jogl in library path. Update LD_LIBRARY_PATH.");
 			System.out.println(" Exiting ...");
 			System.exit(1);
 		}
 		splash();
 		new HoloEdit(true);
 		
 	}
 
 	private static void splash()
 	{
 		Thread t = new Thread()
 		{
 			public void run()
 			{
 				ImageIcon holoSplash = new ImageIcon("./images/Holo-Edit.gif");
 				int h = holoSplash.getIconHeight();
 				int w = holoSplash.getIconWidth();
 
 				JButton b = new JButton(holoSplash);
 				b.setSize(w,h);
 				b.setVisible(true);
 				b.setBorder(null);
 				b.setLocation(0,0);
 
 				JFrame splash = new JFrame();
 				splash.getContentPane().add(b);
 				splash.setUndecorated(true);
 				splash.setResizable(false);
 				splash.setSize(w,h);
 				splash.setLocationRelativeTo(null);
 				splash.setVisible(true);
 				splash.toFront();
 				int c = 0;
 				try
 				{
 					while(c < 250)
 					{
 						Thread.sleep(10);
 						splash.toFront();
 						c+=1;
 					}
 					splash.dispose();
 				}
 				catch (InterruptedException e)
 				{
 					splash.dispose();
 					e.printStackTrace();
 				}
 			}
 		};
 		t.start();
 	}
 
 	public void createAbout()
 	{
 		aboutBox = new AboutDialog(this, "About Holo-Edit");
 	}
 
 	public void about()
 	{
 		if (aboutBox == null)
 			createAbout();
 		aboutBox.setResizable(false);
 		aboutBox.setVisible(true);
 	}
 
 	public void openPrefs()
 	{
 		prefs = new PrefPane(this);
 		prefs.open();
 	}
 
 	public void updateUI()
 	{
 		SwingUtilities.updateComponentTreeUI(gestionPistes.ts);
 		SwingUtilities.updateComponentTreeUI(counterPanel);
 		SwingUtilities.updateComponentTreeUI(transport);
 		SwingUtilities.updateComponentTreeUI(room);
 		SwingUtilities.updateComponentTreeUI(room3d);
 		SwingUtilities.updateComponentTreeUI(score);
 		SwingUtilities.updateComponentTreeUI(soundPoolGui);
 		SwingUtilities.updateComponentTreeUI(timeEditor);
 		SwingUtilities.updateComponentTreeUI(Ut.barMenu);
 	}
 
 	public void checkWindows()
 	{
 		if(!(gestionPistes.ts.visible || transport.visible || room.visible || room3d.visible || score.visible || soundPoolGui.visible || timeEditor.visible))
 			room.open();
 	}
 	
 	// ----------------- QUAND ON QUITTE ------------------------/
 	// Fonction generique pour fermeture de l'application.
 	public boolean close()
 	{
 		boolean OK = askForSave();
 		if (OK)
 		{
 			gestionPistes.saveAlgos();
 			settings.save();
 			connection.close();
 			if(rtDisplay.isAnimating())
 				rtDisplay.stop();
 			System.exit(0);
 			return true;
 		}
 		System.out.println("Exit Canceled");
 		return false;
 	}
 
 	// Demander confirmation a l'utilisateur avant de quitter si le fichier a ete modifie depuis sa derniere sauvegarde
 	public boolean askForSave()
 	{
 		int result;
 		boolean out = false;
 		try
 		{
 			if (isSaved())
 				out = true;
 			else
 			{
 				result = JOptionPane.showConfirmDialog(null, "Do you want to save the changes", "Save ?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
 				if (result == 0)
 				{
 					gestionPistes.writeHoloFile();
 					out = true;
 				} else if (result == 1)
 					out = true;
 			}
 		}
 		catch (NullPointerException npe)
 		{}
 		return out;
 	}
 
 	// pour ajouter un fichier recent (appele chaque fois qu'un fichier est lu)
 	public void insertRecent(HoloRecentFile rf)
 	{
 		// On test s'il n'est pas deja dans la liste
 		if (!recentFiles.contains(rf))
 		{
 			Ut.barMenu.openRecentFilesMenu.setEnabled(true);
 			Ut.barMenu.openRecentFilesMenu.insert(rf, 0);
 			recentFiles.insertElementAt(rf, 0);
 		}
 		// on memorise le dernier fichier lu (pour le chargement automatique a la prochaine ouverture)
 		last = rf;
 		Ut.barMenu.update();
 	}
 	
 	public void addRecent(HoloRecentFile rf)
 	{
 		if(rf.exists())
 		{
 			// On test s'il n'est pas deja dans la liste
 			if (!recentFiles.contains(rf))
 			{
 				Ut.barMenu.openRecentFilesMenu.setEnabled(true);
 				Ut.barMenu.openRecentFilesMenu.add(rf);
 				recentFiles.add(rf);
 			}
 			Ut.barMenu.update();
 		}
 	}
 	
 	public void setTitle(String title)
 	{
 		room.setTitle(title);
 		score.setTitle(title);
 		timeEditor.setTitle(title);
 		if(!Ut.MAC)
 			mainFrame.setTitle(Ut.hv+" - Menu - "+title);
 	}
 
 	public void setSaved(boolean b)
 	{
 		savedState = b;
 		room.setStarred(!b);
 		score.setStarred(!b);
 		timeEditor.setStarred(!b);
 		if(!Ut.MAC)
 			mainFrameSetStarred(!b);
 	}
 
 	public boolean isSaved()
 	{
 		return savedState;
 	}
 
 	public void save()
 	{
 		room.setStarred(false);
 		score.setStarred(false);
 		timeEditor.setStarred(false);
 		if(!Ut.MAC)
 			mainFrameSetStarred(false);
 		savedState = true;
 	}
 
 	public void modify()
 	{
 		setSaved(false);
 	}
 
 	public String getTitle()
 	{
 		return room.getTitle();
 	}
 	
 	public static boolean smooth()
 	{
 		return SMOOTH;
 	}
 
 	public void uncaughtException(Thread t, Throwable e)
 	{
 		System.err.println("Tread : "+t.getName()+" Exception : "+e.getClass());
 		e.printStackTrace();
 		
 	}
 	
 	public void mainFrameSetStarred(boolean b)
 	{
 		String title = mainFrame.getTitle();
 		if(b) {
 			if(!title.endsWith("*"))
 				title += "*";
 		} else {
 			if(title.endsWith("*"))
 				title = title.substring(0,title.length() - 1);
 		}
 		mainFrame.setTitle(title);
 	}
 	
 	public void mainFrameOpen()
 	{
 		mainFrame.setVisible(true);
 		mainFrame.toFront();
 	}
 }
