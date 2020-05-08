 package main;
 
 import image.ImageDealer;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractCellEditor;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowFilter;
 import javax.swing.SpinnerListModel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.Border;
 import javax.swing.event.CellEditorListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.TableCellEditor;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableRowSorter;
 import javax.swing.undo.UndoManager;
 
 import main.file.FileDealer;
 import main.ftp.FTPManager;
 import main.utils.LevenshteinDistance;
 import main.view.tables.ComboTableCellRenderer;
 import main.view.tables.DocsTableRenderer;
 import main.view.tables.MoviesTableRenderer;
 import main.view.tables.MusicTableRenderer;
 import main.view.tables.VideosTableRenderer;
 import movies.db.Movie;
 import movies.web.WebMoviesInfoExtractor;
 import music.db.Disc;
 import music.db.NewDiscTabMod;
 import music.dealfiles.DealMusicFiles;
 import music.lyrics.LyricsFrame;
 import music.mp3Player.MP3Player;
 import music.mp3Player.MP3PlayerWindow;
 import music.mp3Player.TabModelPlayList;
 import music.web.WebMusicInfoExtractor;
 import musicmovies.db.Video;
 import web.WebReader;
 import db.CSV.CSV;
 import docs.db.Doc;
 import docs.db.DocTheme;
 
 public class MultiDB extends JFrame {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	//CONSTRAINTS
 
     public final Dimension COVERS_DIM = new Dimension(400,400);
     public final Dimension MAX_COVERS_DIM = new Dimension(1200,800);
     public static final Dimension PLAYER_DIM = new Dimension(1000,600);
     public final Dimension LYRICS_DIM = new Dimension(500,700);
     public final Dimension MAIN_DIM = new Dimension(1230, 700);
     public final Dimension PANE_DIM = new Dimension(1200, 600);
     public final String COVERS_NOT_FOUND_MSG = new String("Covers not available");
     public final String COVERS_NOT_NAMED_PROP_MSG = new String("Covers found but not named properly");
     public final int EQ_NUM_BANDS = 10;
     public final String configFileName="multiDB.config";
     public final String LYR_PLAYER_NAME="playerLyricsMenu";
     public final String LYR_MENU_NAME="menuViewLyrics"; 
     public final int IND_MUSIC_TAB = 0;
     public final int IND_VIDEOS_TAB = 1;
     public final int IND_MOVIES_TAB = 2;
     public final int IND_DOCS_TAB = 3;
     public final int SAVE_REVIEW=0;
     public final int PASTE_IN_TABLE=1;
     public final int SAVE_COMMENTS=2;
     public final int SAVE_VIDEO_REVIEW=3;
     //VARS
     
     //STATIC VARS
     public static int port;
     public static String host;
     public static String user;
     public static String pass;
     public static String musicDatabase;
     public static String moviesDatabase;
     public static String mysqlPath;
     public static String webEMSearch;
     public static String webEMBand;
     public static String webEMRelease;
     public static String webEMLyrics;
     public static String webMBSearch;
     public static String webMBBand;
     public static String webBing1;
     public static String webBing2;
     public static String webBingAccountKey;
     public static String ftpUser;
     public static String ftpPassword;
     public static String ftpHost;
     public static String ftpDirectory;
     public static String blogUser;
     public static String blogPswd;
     public static String musicUpload;
     public static String videosUpload;
     public static String moviesUpload;
     public static String docsUpload;
     public static String defaultMusicPath;
     public static String logPath=null;
     public static String musicTable="music";
     public static String musicMoviesTable="videos";
     public static String musicFavoritesTable="favorites";
     public static String moviesTable="movies";
     public static String docsTable="docs";
     public static boolean isMusicInCSV=false;
     public static boolean isMoviesInCSV=false;
     public static boolean isDocsInCSV=false;
     public static boolean isVideosInCSV=false;
     public static boolean isdb=false;
     
    
     
     /////MULTIVARS
     
     ///common
     public int selectedViewColumn = -1,selectedModelColumn=-1;
     public int selectedViewRow = -1,lastSelectedViewRow = -1,selectedModelRow=-1,lastSelectedModelRow=-1;
     public int selectedViewRowPlayer = -1,selectedModelRowPlayer=-1;  
     public FTPManager ftpManager;
     public db.CSV.CSV dbCSV;
     public List<Integer> selectedView = new LinkedList<Integer>();//selected Rows in table (View)
     public List<Integer> selectedModel = new LinkedList<Integer>();//selected Rows in table (Model)   
     public TimerThread timerThread;
     //public RandomPlayThread randomPlayThread;
     public Clipboard sysClipboard;
     public long lastTime;
     public int currentCharPos;
     public main.Errors errors;
     
     ///n-tuplas
     public music.db.TabMod musicTabModel;
     public music.db.DataBaseTable musicDataBase;
     public TableRowSorter<music.db.TabMod> musicTableSorter; //trouble with tablerowsorter, used only for filtering words
     
     public movies.db.TabMod moviesTabModel;     
     public movies.db.DataBaseTable moviesDataBase;
     public TableRowSorter<movies.db.TabMod> moviesTableSorter;
     
     public docs.db.TabMod docsTabModel;     
     public docs.db.DataBaseTable docsDataBase;
     public TableRowSorter<docs.db.TabMod> docsTableSorter;
     
     public musicmovies.db.TabMod videosTabModel;     
     public musicmovies.db.DataBaseTable videosDataBase;
     public TableRowSorter<musicmovies.db.TabMod> videosTableSorter;
     
     public music.db.DataBaseFavorites favoritesDataBase;
     
   
     //MUSIC VARS
    
     public List<Disc> disCover = new LinkedList<Disc>();
     public UndoManager undoManager = new UndoManager();  //undo/redo manager
     public WebMusicInfoExtractor webMusicInfoExtractor;
     public WebMoviesInfoExtractor webMoviesInfoExtractor;
     public NewDiscTabMod newDiscsTabMod;
     public boolean musicFolderConnected = false,currentFrontCover = false,playFirstTime = true;
     public SpinnerListModel spinnerCoversM;
     public File backUpPath,lyricsFile,auxPath;
     public Dimension bigCoverDim;
 
     //IMAGE ELEMENTS
     public ImageDealer imageDealer;
     //MAIN VIEWING ELEMENTS
     
     //common
     public MultiDB f; //main frame
     public JMenuBar menuBar;
     //menu items
     public JMenu menuDataBase;
     public JMenuItem menuRelDBBU,menuLoadTempFolder,menuAddItem,menuDelItem,menuMakeBUP,menuRestoreBUP,menuAddBUDB, menuUploadBackup,menuUploadAllBackup;
     public JMenuItem menuOpenCSVDB, menuSaveCSVDB;
     public JMenu menuEdit;
     public JMenuItem menuUndo,menuRedo,menuPaste,menuFilter;
     public JMenu menuMusicOptions;
     public JMenu menuMoviesOptions;
     public JMenuItem menuOpcionesCovers,menuOpcionesGuardar,menuOpcionesCopiarPortadas,menuOpcionesCoverBackup,menuCopyReviews2BUP;
     public JMenu menuViewNewDiscsViaWeb;  
     public JMenuItem menuLoadGroupData,menuPlayRandom,menuViaEM,menuViaMB;
     public JTabbedPane multiPane;
     //popupmenus
     public JPopupMenu popupTable,popupComments;
     public JMenu menuDownloadCover;
     public JMenuItem menuPlay,menuDownloadCoverViaBing,menuDownloadCoverViaCoverParadies,menuViewLyrics,menuLoadFilmData; 
     public MusicTableRenderer coloredTableRenderer;    
         
     //n-tuplas
     public JTable musicJTable,moviesJTable,docsJTable,videosJTable;           //main tables
     public JScrollPane musicTableSp,moviesTableSp,docsTableSp,videosTableSp;
     public JSplitPane musicSplit,docsSplit,videosSplit;
         
     //music specific
     public JLabel coversView,selectCoversView,bigCoversView;  //covers labels
     public JTextArea reviewView,infoText;
     public JScrollPane spRev,splitLeft,newDiscsSp,bigCoversScroll, infoScroll;
     public JSplitPane splitRight;
     public ImageIcon origIcon, scaledIcon, bigIcon;
     public JPopupMenu popupCover,popupReview,popupVideoReview,popupCommentsr;
     public JFrame selectCoverFrame,bigCoversFrame,infoFrame,newDiscsFrame;   
     public JSpinner spinnerCovers;
     public JButton spinnerCoversButton;
     public Image imagen;
     public JTable newDiscsTab;        //table for discographies searched in the internet
     
     //videos specific
     public JTextArea videosReviewView;
     public JScrollPane spVideosReview;
     
     //docs specific
     public JTextArea docsReviewView;
     public JScrollPane spDocsReview;
       
     //PLAYER ELEMENTS
     public MP3Player mp3Player;
     public TabModelPlayList playList;
     public JFrame playerFrame;
     public MP3PlayerWindow mp3PlayerWindow; 
     //public JButton playButton;
     public JButton pauseResumeButton,stopButton,resetEqButton;
     public JTable playListTable;
     public JSlider songSlider;
     public JSlider[] equalizer;
     public JTextField songInformation;
     public JSplitPane splitEq;
     public JInternalFrame playerIntFrame,equalizerFrame;
     public JSplitPane splitPlayer;
     public JScrollPane spPlay,spLyrics;
     public JPopupMenu popupSong;
     public PlayerTableRenderer playerTableRenderer;
        
         ///////////////////////////////////HERE WE GO!!!!\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     	///////////////////////////////////HERE WE GO!!!!\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     	///////////////////////////////////HERE WE GO!!!!\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     
     public MultiDB() {
         super("MultiDB");
         f=this;
     }
 
     public static void main(String[] args) {
         MultiDB aplicacion = new MultiDB();
         aplicacion.initApi();  
     }
 
   
 //////////////////////METODOS/////////////////////////////////////////////////////////////
 //////////////////////METODOS/////////////////////////////////////////////////////////////
 //////////////////////METODOS/////////////////////////////////////////////////////////////
 
 
 ///////////////////////////////////////////////////////////////////////////////////
 //////////////////////VIEWING METHODS/////////////////////////////////////////////////////////////
 ///////////////////////////////////////////////////////////////////////////////////
     
     public void initApi(){
     	
     	Errors.setLogging();
     	this.setIconImage(Toolkit.getDefaultToolkit().getImage("static\thrash8Icon.jpg")); 
     	//initializing vars
     	this.setSize(MAIN_DIM);
     	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     	Toolkit.getDefaultToolkit().setDynamicLayout(true);
     	System.setProperty("sun.awt.noerasebackground", "true");
     	JFrame.setDefaultLookAndFeelDecorated(true);
     	JDialog.setDefaultLookAndFeelDecorated(true);
     	try {
 			UIManager.setLookAndFeel("com.easynth.lookandfeel.EaSynthLookAndFeel");
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 	
         processConfig(configFileName);  //reading parameters from config file
         
         //creating main objects
         
         dbCSV = new CSV();
         mp3Player = new MP3Player();
         imageDealer = new ImageDealer();
         mp3PlayerWindow = new MP3PlayerWindow();
         
         //common
         webMusicInfoExtractor = new WebMusicInfoExtractor();
         webMoviesInfoExtractor = new WebMoviesInfoExtractor();
         ftpManager = new FTPManager();
         Errors.f=f;
         //n-tuplas
         musicTabModel = new music.db.TabMod();
         videosTabModel = new musicmovies.db.TabMod();
         moviesTabModel = new movies.db.TabMod();
         docsTabModel = new docs.db.TabMod(); 
         videosTabModel = new musicmovies.db.TabMod(); 
         if (musicTabModel.getRowCount()>0){ //no database connected or empty 
         	isdb=true;
         	musicDataBase = new music.db.DataBaseTable();
             musicDataBase.setTabModel(musicTabModel);
             videosDataBase = new musicmovies.db.DataBaseTable();
             videosDataBase.setTabModel(videosTabModel);
         	moviesDataBase = new movies.db.DataBaseTable();
             moviesDataBase.setTabModel(moviesTabModel);
         	docsDataBase = new docs.db.DataBaseTable();
             docsDataBase.setTabModel(docsTabModel);
             favoritesDataBase = new music.db.DataBaseFavorites(musicDataBase);
             favoritesDataBase.getSong(0);
         }else isdb=false;
         
         sysClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
         lastTime=System.currentTimeMillis();
         currentCharPos=0;
     	
         /////////////////////////////////////////////creating menu
         menuBar = new JMenuBar();
         this.setJMenuBar(menuBar);
         
         //menu DATA BASE
         menuDataBase = new JMenu("Data Base ");
         menuDataBase.setMnemonic('D');
         //item REL DATABASE AND BACKUP
         menuRelDBBU = new JMenuItem("Relate DataBase and Backup");
         menuRelDBBU.setMnemonic('r');
         RelDBBUHandler relHandler = new RelDBBUHandler();
         menuRelDBBU.addActionListener(relHandler);
         menuDataBase.add(menuRelDBBU);
         //item LOAD TEMP FOLDER
         menuLoadTempFolder = new JMenuItem("Load temp music folder");
         menuLoadTempFolder.setMnemonic('l');
         LoadFolderTempHandler loadHandler = new LoadFolderTempHandler();
         menuLoadTempFolder.addActionListener(loadHandler);
         menuDataBase.add(menuLoadTempFolder);
         
         //item ADD SEVERAL ITEMS TO THE DATABASE
         menuAddItem = new JMenuItem("Add items to the data base");
         AddMItemHandler addMItemHandler = new AddMItemHandler();
         menuAddItem.addActionListener(addMItemHandler);
         menuDataBase.add(menuAddItem);
         //item DELETE ITEM FROM DATABASE
         menuDelItem = new JMenuItem("Delete items from data base");
         menuDelItem.setMnemonic('d');
         DelItemHandler delItemHandler = new DelItemHandler();
         menuDelItem.addActionListener(delItemHandler);
         menuDataBase.add(menuDelItem);
         //item ADD DISCS TO BACKUP AND DATABASE
         menuAddBUDB = new JMenuItem("Add discs from folder to backup and database");
         menuDelItem.setMnemonic('a');
         AddBUDBHandler addBUDBHandler = new AddBUDBHandler();
         menuAddBUDB.addActionListener(addBUDBHandler);
         menuDataBase.add(menuAddBUDB); 
           //item MAKE BACKUP OF DATABASE
         menuMakeBUP = new JMenuItem("Make backup of database");
         menuMakeBUP.setMnemonic('m');
         DBBUPHandler makeBUPHandler = new DBBUPHandler();
         menuMakeBUP.addActionListener(makeBUPHandler);
         menuDataBase.add(menuMakeBUP);
         //item RESTORE BACKUP OF DATABASE
         menuRestoreBUP = new JMenuItem("Restore database");
         menuRestoreBUP.setMnemonic('r');
         RestoreDBBUPHandler restoreBUPHandler = new RestoreDBBUPHandler();
         menuRestoreBUP.addActionListener(restoreBUPHandler);
         menuDataBase.add(menuRestoreBUP);
         //item UPLOAD BACKUP TO WEB
         menuUploadBackup = new JMenuItem("Upload this backup to Webdatabase");
         menuUploadBackup.setMnemonic('u');
         UploadBUPHandler uploadBUPHandler = new UploadBUPHandler();
         menuUploadBackup.addActionListener(uploadBUPHandler);
         menuDataBase.add(menuUploadBackup);
         //item UPLOAD BACKUP TO WEB
         menuUploadAllBackup = new JMenuItem("Upload all backups to Webdatabase");
         menuUploadAllBackup.setMnemonic('u');
         UploadAllBUPHandler uploadAllBUPHandler = new UploadAllBUPHandler();
         menuUploadAllBackup.addActionListener(uploadAllBUPHandler);
         menuDataBase.add(menuUploadAllBackup);
         //item OPEN CSV DATABASE
         menuOpenCSVDB = new JMenuItem("Open CSV database");
         menuOpenCSVDB.setMnemonic('u');
         OpenCSVDBHandler openCSVDBHandler = new OpenCSVDBHandler();
         menuOpenCSVDB.addActionListener(openCSVDBHandler);
         menuDataBase.add(menuOpenCSVDB);
         //item SAVE CSV DATABASE
         menuSaveCSVDB = new JMenuItem("Save CSV database");
         menuSaveCSVDB.setMnemonic('u');
         SaveCSVDBHandler saveCSVDBHandler = new SaveCSVDBHandler();
         menuSaveCSVDB.addActionListener(saveCSVDBHandler);
         menuDataBase.add(menuSaveCSVDB);    
         
         //menu EDIT
         menuEdit = new JMenu("Edit ");
         menuEdit.setMnemonic('E');
         //item UNDO   
         menuUndo = new JMenuItem("Undo");
         menuUndo.setMnemonic('u');
         menuUndo.setAccelerator(KeyStroke.getKeyStroke('Z',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
         MenuUndoHandler menuUndoHandler = new MenuUndoHandler();
         menuUndo.addActionListener(menuUndoHandler);
         menuEdit.add(menuUndo);
         //item REDO
         menuRedo = new JMenuItem("Redo");
         menuRedo.setMnemonic('r');
         MenuRedoHandler menuRedoHandler = new MenuRedoHandler();
         menuRedo.addActionListener(menuRedoHandler);
         menuEdit.add(menuRedo);
         //item PASTE
         menuPaste = new JMenuItem("Paste");
         menuPaste.setMnemonic('v');
         MenuPasteHandler menuPasteHandler = new MenuPasteHandler();
         menuPaste.setAccelerator(KeyStroke.getKeyStroke('V',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
         menuPaste.addActionListener(menuPasteHandler);
         menuEdit.add(menuPaste);
         //item FILTER
         menuFilter = new JMenuItem("Search word");
         menuRedo.setMnemonic('f');
         menuFilter.setAccelerator(KeyStroke.getKeyStroke('F',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
         MenuFilterHandler menuFilterHandler = new MenuFilterHandler();
         menuFilter.addActionListener(menuFilterHandler);
         menuEdit.add(menuFilter);
         
         //MENU OPCIONES
         menuMusicOptions = new JMenu("Music DB Options ");
         menuMusicOptions.setMnemonic('O');
         //item VER LISTA COVERS
         menuOpcionesCovers = new JMenuItem("Open found covers list");
         menuOpcionesCovers.setMnemonic('c');
         CoverHandler coverHandler = new CoverHandler();
         menuOpcionesCovers.addActionListener(coverHandler);
         menuOpcionesCovers.setEnabled(false);
         menuMusicOptions.add(menuOpcionesCovers);
         //item GUARDAR LISTA DISCOS SIN COVER
         menuOpcionesGuardar = new JMenuItem("Save list of discs without cover");
         menuOpcionesGuardar.setMnemonic('g');
         NoCoverDiscHandler listaHandler = new NoCoverDiscHandler();
         menuOpcionesGuardar.addActionListener(listaHandler);
         menuMusicOptions.add(menuOpcionesGuardar);
         //item COPIAR PORTADAS A DESTINO
         menuOpcionesCopiarPortadas = new JMenuItem("Move covers to backup or music folder");
         menuOpcionesCopiarPortadas.setMnemonic('p');
         MoveCoversHandler copiarPortadasHandler = new MoveCoversHandler();
         menuOpcionesCopiarPortadas.addActionListener(copiarPortadasHandler);
         menuMusicOptions.add(menuOpcionesCopiarPortadas);
         //item COVER BACKUP
         menuOpcionesCoverBackup = new JMenuItem("Cover backup");
         menuOpcionesCoverBackup.setMnemonic('b');
         CoverBackupHandler coverBackupHandler = new CoverBackupHandler();
         menuOpcionesCoverBackup.addActionListener(coverBackupHandler);
         menuOpcionesCoverBackup.setEnabled(false);
         menuMusicOptions.add(menuOpcionesCoverBackup);
         //item COPYREVIEW2BUP
         menuCopyReviews2BUP = new JMenuItem("Copy reviews to database");
         menuCopyReviews2BUP.setMnemonic('m');
         CopyReviewsHandler copyReviewsHandler = new CopyReviewsHandler();
         menuCopyReviews2BUP.addActionListener(copyReviewsHandler);
         menuMusicOptions.add(menuCopyReviews2BUP);
         //item VIEWNEWDISCS
         menuViewNewDiscsViaWeb = new JMenu("Search new discs via web");
         menuViewNewDiscsViaWeb.setMnemonic('v');
         ViewNewDiscsHandler viewNewDiscsHandlerEM = new ViewNewDiscsHandler("webEM");
         ViewNewDiscsHandler viewNewDiscsHandlerMB = new ViewNewDiscsHandler("webMB");
         menuViaEM = new JMenuItem("Via Encyclopedia Metallum");
         menuViaEM.addActionListener(viewNewDiscsHandlerEM);
         menuViewNewDiscsViaWeb.add(menuViaEM);
         menuViaMB = new JMenuItem("Via Musicbrainz");
         menuViaMB.addActionListener(viewNewDiscsHandlerMB);
         menuViewNewDiscsViaWeb.add(menuViaMB);
         menuMusicOptions.add(menuViewNewDiscsViaWeb);
         //item PLAYRANDOM
         menuLoadGroupData = new JMenuItem("Load group data");
         MenuLoadGroupDataHandler menuLoadGroupDataHandler = new MenuLoadGroupDataHandler();
         menuLoadGroupData.addActionListener(menuLoadGroupDataHandler);
         menuLoadGroupData.setMnemonic('l');
         menuMusicOptions.add(menuLoadGroupData);
         //item PLAYRANDOM
         menuPlayRandom = new JMenuItem("Play files at random");
         menuPlayRandom.setMnemonic('p');
         PlayRandomHandler playRandomHandler = new PlayRandomHandler();
         menuPlayRandom.addActionListener(playRandomHandler);
         menuPlayRandom.setEnabled(false);
         menuMusicOptions.add(menuPlayRandom);
         
         menuMoviesOptions = new JMenu("Movies DB Options ");
         menuMoviesOptions.setMnemonic('O');
         //item LOAD MOVIE DATA
         menuLoadFilmData = new JMenuItem("Load movie data");
 		MenuLoadFilmDataHandler menuLoadFilmDataHandler = new MenuLoadFilmDataHandler();
 		menuLoadFilmData.addActionListener(menuLoadFilmDataHandler);
 		menuLoadFilmData.setMnemonic('v');
 		menuMoviesOptions.add(menuLoadFilmData);
             
 
         menuBar.add(menuDataBase);
         menuBar.add(menuEdit);
         menuBar.add(menuMusicOptions);
         menuBar.add(menuMoviesOptions);
               
 
 ////////////////////////////////////popupmenus//////////////////////////////////
 
 		popupTable = new JPopupMenu();
 		JMenuItem menuDefaultOrder = new JMenuItem("Default sorting");
 		PopupMenuSortDefaultHandler popupSortMenuDefaultHandler = new PopupMenuSortDefaultHandler();
 		menuDefaultOrder.addActionListener(popupSortMenuDefaultHandler);
 		JMenuItem menuOrderByField = new JMenuItem("Sort by this field");
 		PopupMenuSortByFieldHandler popupMenuSortByFieldHandler = new PopupMenuSortByFieldHandler();
 		menuOrderByField.addActionListener(popupMenuSortByFieldHandler);
 		menuPlay = new JMenuItem("Play this item");
 		PopupMenuPlayHandler popupMenuPlayHandler = new PopupMenuPlayHandler();
 		menuPlay.addActionListener(popupMenuPlayHandler);
 		menuPlay.setEnabled(false);
 		menuViewLyrics = new JMenuItem("View Lyrics");
 		PopupMenuViewLyricsHandler popupMenuViewLyrics = new PopupMenuViewLyricsHandler();
 		menuViewLyrics.addActionListener(popupMenuViewLyrics);	
 		menuViewLyrics.setEnabled(false);
 		menuViewLyrics.setName(LYR_MENU_NAME);
 		menuDownloadCover = new JMenu("Download cover");		
 		menuDownloadCoverViaBing = new JMenuItem("Download cover via Bing");
 		menuDownloadCoverViaBing.setName("Bing");
 		PopupMenuDownloadCover popupMenuDownloadCoverViaBing = new PopupMenuDownloadCover();
 		menuDownloadCoverViaBing.addActionListener(popupMenuDownloadCoverViaBing);
 		menuDownloadCoverViaCoverParadies = new JMenuItem("Download cover via CoverParadies");
 		menuDownloadCoverViaCoverParadies.setName("Paradies");
 		PopupMenuDownloadCover popupMenuDownloadCoverViaCoverParadies = new PopupMenuDownloadCover();
 		menuDownloadCoverViaCoverParadies.addActionListener(popupMenuDownloadCoverViaCoverParadies);
 		menuDownloadCover.add(menuDownloadCoverViaBing);
 		menuDownloadCover.add(menuDownloadCoverViaCoverParadies);
 		
 		JMenuItem menuPastePopup = new JMenuItem("Paste");
 		PopupMenuPasteHandler popupMenuPasteHandler = new PopupMenuPasteHandler();
 		menuPastePopup.addActionListener(popupMenuPasteHandler);
 		menuPastePopup.setMnemonic('v');
 		menuPastePopup.setAccelerator(KeyStroke.getKeyStroke('V',Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
 				
 		popupTable.add(menuDefaultOrder);
 		popupTable.add(menuOrderByField);
 		popupTable.add(menuPlay);
 		popupTable.add(menuPastePopup);
 		popupTable.add(menuViewLyrics);
 		popupTable.add(menuDownloadCover);	
 		
 		popupCover = new JPopupMenu();
 		JMenuItem menuViewBigCover = new JMenuItem("View cover in bigger frame");
 		PopupMenuShowBigCoverHandler showBigCoverHandler = new PopupMenuShowBigCoverHandler();
 		menuViewBigCover.addActionListener(showBigCoverHandler);
 		JMenuItem menuOpenCovers = new JMenuItem("Open images from folder");
 		PopupMenuOpenCovers menuHandlerOpenCovers = new PopupMenuOpenCovers();
 		menuOpenCovers.addActionListener(menuHandlerOpenCovers);
 
 		popupCover.add(menuViewBigCover);
 		popupCover.add(menuOpenCovers);
 			
 		popupReview = new JPopupMenu();
         JMenuItem menuSaveReview = new JMenuItem("Save review in database");
         menuSaveReview.setMnemonic('s');
         SaveReviewHandler saveReviewHandler = new SaveReviewHandler();
         menuSaveReview.addActionListener(saveReviewHandler);
         
 		popupReview.add(menuSaveReview);
 		
 		popupVideoReview = new JPopupMenu();
         JMenuItem menuSaveVideoReview = new JMenuItem("Save video review in database");
         menuSaveVideoReview.setMnemonic('s');
         SaveVideoReviewHandler saveVideoReviewHandler = new SaveVideoReviewHandler();
         menuSaveVideoReview.addActionListener(saveVideoReviewHandler);
         
 		popupVideoReview.add(menuSaveVideoReview);
 	
 		popupComments = new JPopupMenu();
         JMenuItem menuSaveComments = new JMenuItem("Save comments in database");
         menuSaveComments.setMnemonic('s');
         SaveCommentsHandler saveCommentsHandler = new SaveCommentsHandler();
         menuSaveComments.addActionListener(saveCommentsHandler);
         
         popupComments.add(menuSaveComments);
 		
         menuLoadFilmData.setEnabled(false);	
         if (!isdb){
         	menuSaveReview.setEnabled(false);
         	menuSaveVideoReview.setEnabled(false);
         	menuSaveComments.setEnabled(false);
         	menuAddBUDB.setEnabled(true);
         	menuMakeBUP.setEnabled(false);
         	menuRestoreBUP.setEnabled(false);
         	menuDownloadCover.setEnabled(false);
         }
 		
 		////////////////////////////////TABLES LAYOUT\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
 		////////////////////////////////TABLES LAYOUT\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
 		////////////////////////////////TABLES LAYOUT\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
 		
 	////////////////////////////////////////music table layout\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
         
         musicJTable = new JTable(musicTabModel);
         musicJTable.setCellSelectionEnabled(true); //no se puede seleccionar solo una celda
         musicJTable.setColumnSelectionAllowed(true); //se pueden seleccionar columnas
         musicJTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
       
         musicTableSorter = new TableRowSorter<music.db.TabMod>(musicTabModel);
         musicJTable.setRowSorter(musicTableSorter);
         for (int i=0;i<musicTabModel.getColumnCount();i++){ 
         	musicTableSorter.setSortable(i,false); //issue with TableRowSorter, disabling sorting, using only for filtering
         }
         
         MusicTableRenderer musicTableRenderer = new MusicTableRenderer();
         musicJTable.setDefaultRenderer(Object.class,musicTableRenderer);
         
         //sizing cols
         TableColumn col = musicJTable.getColumn("groupName");
         col.setMinWidth(130);
         col.setPreferredWidth(130);
         col = musicJTable.getColumn("title");
         col.setMinWidth(180);
         col.setPreferredWidth(180);
         col = musicJTable.getColumn("style");
         col.setMinWidth(120);
         col.setPreferredWidth(120);
         col = musicJTable.getColumn("year");
         col.setMinWidth(40);
         col.setPreferredWidth(40);
         col = musicJTable.getColumn("loc");
         col.setMinWidth(80);
         col.setPreferredWidth(80);
         col = musicJTable.getColumn("copy");
         col.setMinWidth(60);
         col.setPreferredWidth(60);
         col = musicJTable.getColumn("type");
         col.setMinWidth(40);
         col.setPreferredWidth(40);
         col = musicJTable.getColumn("mark");
         col.setMinWidth(30);
         col.setPreferredWidth(30);
         col = musicJTable.getColumn("present");
         col.setMinWidth(30);
         col.setPreferredWidth(30);
         //removing cols not needed
         col = musicJTable.getColumn("review");
         musicJTable.removeColumn(col);
         col = musicJTable.getColumn("path");
         musicJTable.removeColumn(col);
         col = musicJTable.getColumn("Id");
         musicJTable.removeColumn(col);
         
         
 	////////////////////////////////////////videos table layout\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
         
         videosJTable = new JTable(videosTabModel);
         videosJTable.setCellSelectionEnabled(true); //no se puede seleccionar solo una celda
         videosJTable.setColumnSelectionAllowed(true); //no se pueden seleccionar columnas
         videosJTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
       
         videosTableSorter = new TableRowSorter<musicmovies.db.TabMod>(videosTabModel);
         videosJTable.setRowSorter(videosTableSorter);
         for (int i=0;i<videosTabModel.getColumnCount();i++){ 
         	videosTableSorter.setSortable(i,false); //issue with TableRowSorter, disabling sorting, using only for filtering
         }
         
         VideosTableRenderer videosTableRenderer = new VideosTableRenderer();
         videosJTable.setDefaultRenderer(Object.class,videosTableRenderer);
         
         //sizing cols
         col = videosJTable.getColumn("groupName");
         col.setMinWidth(130);
         col.setPreferredWidth(130);
         col = videosJTable.getColumn("title");
         col.setMinWidth(180);
         col.setPreferredWidth(180);
         col = videosJTable.getColumn("style");
         col.setMinWidth(120);
         col.setPreferredWidth(120);
         col = videosJTable.getColumn("year");
         col.setMinWidth(40);
         col.setPreferredWidth(40);
         col = videosJTable.getColumn("loc");
         col.setMinWidth(80);
         col.setPreferredWidth(80);
         col = videosJTable.getColumn("copy");
         col.setMinWidth(60);
         col.setPreferredWidth(60);
         col = videosJTable.getColumn("type");
         col.setMinWidth(40);
         col.setPreferredWidth(40);
         col = videosJTable.getColumn("mark");
         col.setMinWidth(30);
         col.setPreferredWidth(30);
         //removing cols not needed
         col = videosJTable.getColumn("Id");
         videosJTable.removeColumn(col);
         col = videosJTable.getColumn("review");
         videosJTable.removeColumn(col);
         
     	videosReviewView = new JTextArea();
 	    Font font = new Font("Verdana", Font.BOLD, 11);
 	    videosReviewView.setFont(font);
 	    videosReviewView.setForeground(Color.BLACK);
 	    videosReviewView.setBackground(Color.CYAN);
 	    videosReviewView.setLineWrap(true);
 	    videosReviewView.setWrapStyleWord(true);
 	    
 	    //creating splits
 	    splitLeft = new JScrollPane(videosJTable);
 	    spVideosReview = new JScrollPane(videosReviewView);  
 	    //scrollbar policies
 	    spVideosReview.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 	    spVideosReview.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 	    videosSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitLeft,spVideosReview );
 	    videosSplit.setContinuousLayout(true);
 	    videosSplit.setOneTouchExpandable(true);
 	    videosSplit.setDividerLocation(800);
 	    videosSplit.setPreferredSize(PANE_DIM);
 	    videosTableSp = new JScrollPane(videosSplit);
         
         
 	////////////////////////////////////////movies table layout\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
         
         moviesJTable = new JTable(moviesTabModel);
         moviesJTable.setCellSelectionEnabled(true); //no se puede seleccionar solo una celda
         moviesJTable.setColumnSelectionAllowed(true); //no se pueden seleccionar columnas
         moviesJTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
       
         moviesTableSorter = new TableRowSorter<movies.db.TabMod>(moviesTabModel);
         moviesJTable.setRowSorter(moviesTableSorter);
         for (int i=0;i<moviesTabModel.getColumnCount();i++){ 
         	moviesTableSorter.setSortable(i,false); //issue with TableRowSorter, disabling sorting, using only for filtering
         }
         
         MoviesTableRenderer moviesTableRenderer = new MoviesTableRenderer();
         moviesJTable.setDefaultRenderer(Object.class,moviesTableRenderer);
         
         //sizing cols
         col = moviesJTable.getColumn("title");
         col.setMinWidth(180);
         col.setPreferredWidth(180);
         col = moviesJTable.getColumn("director");
         col.setMinWidth(120);
         col.setPreferredWidth(120);
         col = moviesJTable.getColumn("year");
         col.setMinWidth(40);
         col.setPreferredWidth(40);
         col = moviesJTable.getColumn("loc");
         col.setMinWidth(80);
         col.setPreferredWidth(80);
         col = moviesJTable.getColumn("other");
         col.setMinWidth(60);
         col.setPreferredWidth(60);
       
         //removing cols not needed
      
         col = moviesJTable.getColumn("Id");
         moviesJTable.removeColumn(col);
         col = moviesJTable.getColumn("path");
         moviesJTable.removeColumn(col);
         col = moviesJTable.getColumn("present");
         moviesJTable.removeColumn(col);
         moviesTableSp = new JScrollPane(moviesJTable);
                
 	////////////////////////////////////////docs table layout\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
         
         docsJTable = new JTable(docsTabModel);
         docsJTable.setCellSelectionEnabled(true); //no se puede seleccionar solo una celda
         docsJTable.setColumnSelectionAllowed(true); //no se pueden seleccionar columnas
         docsJTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
       
         docsTableSorter = new TableRowSorter<docs.db.TabMod>(docsTabModel);
         docsJTable.setRowSorter(docsTableSorter);
         for (int i=0;i<docsTabModel.getColumnCount();i++){ 
         	docsTableSorter.setSortable(i,false); //issue with TableRowSorter, disabling sorting, using only for filtering
         }
         
         DocsTableRenderer docsTableRenderer = new DocsTableRenderer();
         docsJTable.setDefaultRenderer(Object.class,docsTableRenderer);
         
         //sizing cols
         col = docsJTable.getColumn("title");
         col.setMinWidth(500);
         col.setPreferredWidth(500);
         col = docsJTable.getColumn("theme");
         col.setMinWidth(60);
         col.setPreferredWidth(60);
         col = docsJTable.getColumn("loc");
         col.setMinWidth(40);
         col.setPreferredWidth(40);
       
         col = docsJTable.getColumn("id");
         docsJTable.removeColumn(col);
         col = docsJTable.getColumn("comments");
         docsJTable.removeColumn(col);
         
         //adding combobox for theme column		
 		ComboCellEditor comboCellEditor = new ComboCellEditor();
 		docsJTable.setDefaultEditor(DocTheme.class, comboCellEditor);
 		ComboTableCellRenderer comboCellRenderer = new ComboTableCellRenderer();
 		docsJTable.setDefaultRenderer(DocTheme.class,comboCellRenderer);
 		
 		docsReviewView = new JTextArea();
 	    font = new Font("Verdana", Font.BOLD, 11);
 	    docsReviewView.setFont(font);
 	    docsReviewView.setForeground(Color.BLACK);
 	    docsReviewView.setBackground(Color.CYAN);
 	    docsReviewView.setLineWrap(true);
 	    docsReviewView.setWrapStyleWord(true);
 	              
 	       
 	    
 	    //creating splits
 	    splitLeft = new JScrollPane(docsJTable);
 	    spDocsReview = new JScrollPane(docsReviewView);  
 	    //scrollbar policies
 	    spDocsReview.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 	    spDocsReview.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 	    docsSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitLeft,spDocsReview );
 	    docsSplit.setContinuousLayout(true);
 	    docsSplit.setOneTouchExpandable(true);
 	    docsSplit.setDividerLocation(800);
 	    docsSplit.setPreferredSize(PANE_DIM);
 	    docsTableSp = new JScrollPane(docsSplit);
 		
         
        
  
 //////////////////////////////////tab music splits layout\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\  
         
         coversView = new JLabel();
         //dimensions for covers
         coversView.setMinimumSize(COVERS_DIM);
         reviewView = new JTextArea();
         font = new Font("Verdana", Font.BOLD, 11);
         reviewView.setFont(font);
         reviewView.setForeground(Color.BLACK);
         reviewView.setBackground(Color.CYAN);
         reviewView.setLineWrap(true);
         reviewView.setWrapStyleWord(true);
                
         
         spRev = new JScrollPane(reviewView);
         //scrollbar policies
         spRev.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         spRev.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         //creating splits
         splitLeft = new JScrollPane(musicJTable);
         splitRight = new JSplitPane(JSplitPane.VERTICAL_SPLIT,coversView,spRev);        
         splitRight.setContinuousLayout(true);
         splitRight.setOneTouchExpandable(true);
         splitRight.setDividerLocation(400);
         musicSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitLeft,splitRight );
         musicSplit.setContinuousLayout(true);
         musicSplit.setOneTouchExpandable(true);
         musicSplit.setDividerLocation(800);
         musicSplit.setPreferredSize(PANE_DIM);
         musicTableSp = new JScrollPane(musicSplit);
         
 
         /////////////////////////////////adding tabs to tabbedpane\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
         multiPane = new JTabbedPane();
         ///being careful with the orders, they must be the same as in constants IND_MUSIC_TAB,IND_VIDEOS_TAB,IND_MOVIES_TAB,IND_DOCS_TAB
         multiPane.addTab("music",musicTableSp);
         multiPane.addTab("music videos",videosTableSp);
         multiPane.addTab("movies",moviesTableSp);
         multiPane.addTab("documentaries",docsTableSp); 
         TabbedPaneListener tabbedPaneListener = new TabbedPaneListener();
         multiPane.addChangeListener(tabbedPaneListener);
         f.add(multiPane);
         
         
         ///////////setting icons for pictures
         origIcon= new ImageIcon();
         scaledIcon = new ImageIcon();
         bigIcon = new ImageIcon();
         
         if (isdb){
 	        musicDataBase.setReviewView(reviewView);
 	        moviesDataBase.setReviewView(videosReviewView);
 	        docsDataBase.setReviewView(docsReviewView);
         }
 
         
         
 ////////////////////////////////table new Discs layout////////////////////////////
 		newDiscsTab = new JTable();        
 		newDiscsTab.setCellSelectionEnabled(true); //no se puede seleccionar solo una celda
 		newDiscsTab.setColumnSelectionAllowed(true); //no se pueden seleccionar columnas
 		newDiscsTab.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); 
 		
 		coloredTableRenderer = new MusicTableRenderer();
 		newDiscsTab.setDefaultRenderer(Object.class,coloredTableRenderer);
 		
 		newDiscsTabMod = new NewDiscTabMod();   
 		newDiscsTab.setModel(newDiscsTabMod);
 		//sizing cols
 		col = newDiscsTab.getColumn("Group");
 		col.setMinWidth(130);
 		col.setPreferredWidth(130);
 		col = newDiscsTab.getColumn("Title");
 		col.setMinWidth(180);
 		col.setPreferredWidth(180);
 		col = newDiscsTab.getColumn("Style");
 		col.setMinWidth(120);
 		col.setPreferredWidth(120);
 		col = newDiscsTab.getColumn("Year");
 		col.setMinWidth(40);
 		col.setPreferredWidth(40);
 		col = newDiscsTab.getColumn("Location");
 		col.setMinWidth(220);
 		col.setPreferredWidth(220);
 		col = newDiscsTab.getColumn("Type");
 		col.setMinWidth(80);
 		col.setPreferredWidth(80);
 		col = newDiscsTab.getColumn("Id");
 		newDiscsTab.removeColumn(col);
 		newDiscsSp = new JScrollPane(newDiscsTab);  
         
 ///////////////////////////////Frames layout///////////////////////////////////////////////
         
         //bigcover frame
         bigCoversFrame=new JFrame("Cover");
         bigCoversView = new JLabel();
         bigCoversScroll = new JScrollPane(bigCoversView);
         bigCoversFrame.add(bigCoversScroll);
          
         //copyinginfo frame
 		infoFrame = new JFrame("Info");
 		infoFrame.setSize(500, 300);
 		infoText = new JTextArea();
 		infoText.setWrapStyleWord(true);
 		infoText.setLineWrap(true);
 		infoScroll = new JScrollPane(infoText);
 		infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		infoFrame.add(infoScroll);
 		
 		//newDiscs Frame
 		newDiscsFrame = new JFrame("New Discs");
         newDiscsFrame.add(newDiscsSp);
         
         ///////////////////////////for every table/////////////////////////////////////
         //n-tuplas where needed
 
         //selecting row, shows review and covers in case of BackUpConnected
         SelectItemHandler selectHandler = new SelectItemHandler();
         SelectColumnHandler selectColumnHandler = new SelectColumnHandler();
         //if a row is selected and column selection changes on the same row we need another handler regarding the columnModel
         musicJTable.getSelectionModel().addListSelectionListener(selectHandler);
         musicJTable.getColumnModel().getSelectionModel().addListSelectionListener(selectColumnHandler);
         moviesJTable.getSelectionModel().addListSelectionListener(selectHandler);
         moviesJTable.getColumnModel().getSelectionModel().addListSelectionListener(selectColumnHandler);
         docsJTable.getSelectionModel().addListSelectionListener(selectHandler);
         docsJTable.getColumnModel().getSelectionModel().addListSelectionListener(selectColumnHandler);
         videosJTable.getSelectionModel().addListSelectionListener(selectHandler);
         videosJTable.getColumnModel().getSelectionModel().addListSelectionListener(selectColumnHandler);
         
         //to edit cells and save in the database
         CellEditorHandler edHandler = new CellEditorHandler();
         musicJTable.getDefaultEditor(Object.class).addCellEditorListener(edHandler);
         moviesJTable.getDefaultEditor(Object.class).addCellEditorListener(edHandler);
         docsJTable.getDefaultEditor(Object.class).addCellEditorListener(edHandler);
         videosJTable.getDefaultEditor(Object.class).addCellEditorListener(edHandler);
         
         //popuptablelistener
         PopupTableListener popupTableListener = new PopupTableListener();
         musicJTable.addMouseListener(popupTableListener);
         moviesJTable.addMouseListener(popupTableListener);
         docsJTable.addMouseListener(popupTableListener); 
         videosJTable.addMouseListener(popupTableListener); 
         
         //key listener to select row by letter
         TableKeyListener tableKeyListener = new TableKeyListener();
         musicJTable.addKeyListener(tableKeyListener);
         moviesJTable.addKeyListener(tableKeyListener);
         docsJTable.addKeyListener(tableKeyListener);
         videosJTable.addKeyListener(tableKeyListener);
    
         //pastelisteners Ctrl+V
         musicJTable.getInputMap().put(KeyStroke.getKeyStroke("control V"), "pasteInTable");
         moviesJTable.getInputMap().put(KeyStroke.getKeyStroke("control V"), "pasteInTable");
         docsJTable.getInputMap().put(KeyStroke.getKeyStroke("control V"), "pasteInTable");
         videosJTable.getInputMap().put(KeyStroke.getKeyStroke("control V"), "pasteInTable");
         
         // Add the action to the component
         AbstractActionsHandler pasteHandler = new AbstractActionsHandler(PASTE_IN_TABLE);
         musicJTable.getActionMap().put("pasteInTable",pasteHandler);
         moviesJTable.getActionMap().put("pasteInTable",pasteHandler);
         docsJTable.getActionMap().put("pasteInTable",pasteHandler);
         videosJTable.getActionMap().put("pasteInTable",pasteHandler);
         
         
         ////////////////////specific handlers for docs\\\\\\\\\\\\\\\\\\\\\\\\\
         
         //popucommentslistener
         PopupCommentsListener popupCommentsListener = new PopupCommentsListener();
         docsReviewView.addMouseListener(popupCommentsListener);
         
         //diferent method to add keystroke to the reviewView due to the fact that the other method doesn't work if the popup isn't showed 
         docsReviewView.getInputMap().put(KeyStroke.getKeyStroke("control S"), "saveComments");
         AbstractActionsHandler saveCommentsKHandler = new AbstractActionsHandler(SAVE_COMMENTS);
         // Add the action to the component
         docsReviewView.getActionMap().put("saveComments",saveCommentsKHandler);
         
         ////////////////////specific handlers for videos\\\\\\\\\\\\\\\\\\\\\\\\\
         
         //popucommentslistener
         PopupVideoReviewListener popupRevVideosListener = new PopupVideoReviewListener();
         videosReviewView.addMouseListener(popupRevVideosListener);
         
         //diferent method to add keystroke to the reviewView due to the fact that the other method doesn't work if the popup isn't showed 
         videosReviewView.getInputMap().put(KeyStroke.getKeyStroke("control S"), "saveVideoReview");
         AbstractActionsHandler saveVideoRevKHandler = new AbstractActionsHandler(SAVE_VIDEO_REVIEW);
         // Add the action to the component
         videosReviewView.getActionMap().put("saveVideoReview",saveVideoRevKHandler);
         
         ////////////////////specific handlers for music\\\\\\\\\\\\\\\\\\\\\\\\\
 
         //popupreviewlistener
         PopupReviewListener popupReviewListener = new PopupReviewListener();
         reviewView.addMouseListener(popupReviewListener);
         
         //diferent method to add keystroke to the reviewView due to the fact that the other method doesn't work if the popup isn't showed 
         reviewView.getInputMap().put(KeyStroke.getKeyStroke("control S"), "saveReview");
         AbstractActionsHandler saveReviewKHandler = new AbstractActionsHandler(SAVE_REVIEW);
         // Add the action to the component
         reviewView.getActionMap().put("saveReview",saveReviewKHandler);
         
      
         /////////////////////////////covers//////////////////////////////////////////
         
         //mouselistener to change the coverview for the backcoverview
         ChangeCoverListener changeCoverListener = new ChangeCoverListener();
         coversView.addMouseListener(changeCoverListener);
         
         //////////////////////////////////////////////////////////////////////
         
         if (!isdb){
         	File file=FileDealer.selectFile(f,"Please select file for music database");
    	        if (file != null) {              //para todos los grupos de la carpeta
    	            RetrieveCSVThread retThread = new RetrieveCSVThread();
    	            retThread.setDaemon(true);
    	            retThread.fileName=file.getAbsolutePath();
    	            retThread.start();
    		    }
         }
         this.setVisible(true);       
     }
 
 /////////////////////////////////////////////////////////////////////////////////////////////////////
 //////////////////////////////CONFIGURATION FILE PROCESSING METHOD///////////////////////////////////
 /////////////////////////////////////////////////////////////////////////////////////////////////////
 
   
     //m�todo que procesa las variables de configuraci�n
     public void processConfig(String fileName) {
    	String appPath = System.getProperties().getProperty("user.dir"); 
     	File fi=new File(appPath+File.separator+fileName);
 		if (fi!=null){
  		   try{
  			   FileReader fr = new FileReader(fi);
  			   BufferedReader bf = new BufferedReader(fr);
  			   String cad;
  			   String param;
  			   int pos;
  			   while ((cad=bf.readLine())!=null) {
  				   pos=0;
  				   if ((pos=cad.indexOf(">"))>-1){
  					   param=cad.substring(pos+1).trim();
 	 				   if (cad.indexOf("<port>")>-1){
 	 					   port=Integer.parseInt(param);
 	 				   }else if (cad.indexOf("<host>")>-1){
 	 					   host=param;
 	 				   }else if (cad.indexOf("<user>")>-1){
 	 					   user=param;
 	 				   }else if (cad.indexOf("<password>")>-1){
 	 					   pass=param;
 	 				   }else if (cad.indexOf("<musicDatabase>")>-1){
 	 					   musicDatabase=param;
 	 				   }else if (cad.indexOf("<moviesDatabase>")>-1){
 	 					   moviesDatabase=param;
 	 				   }else if (cad.indexOf("<mysqlPath>")>-1){
 	 					   mysqlPath=param;
 	 				   }else if (cad.indexOf("<webEMSearch>")>-1){
 	 					   webEMSearch=param;
 	 				   }else if (cad.indexOf("<webEMBand>")>-1){
 	 					   webEMBand=param;
 	 				   }else if (cad.indexOf("<webEMRelease>")>-1){
 	 					   webEMRelease=param;
 	 				   }else if (cad.indexOf("<webEMLyrics>")>-1){
 	 					   webEMLyrics=param;
 	 				   }else if (cad.indexOf("<webMBSearch>")>-1){
 	 					   webMBSearch=param;
 	 				   }else if (cad.indexOf("<webMBBand>")>-1){
 	 					   webMBBand=param;
 	 				   }else if (cad.indexOf("<bingSearch1>")>-1){
 	 					  webBing1=param;
 	 				   }else if (cad.indexOf("<bingSearch2>")>-1){
 	 					  webBing2=param;
 	 				   }else if (cad.indexOf("<bingAccountKey>")>-1){
 	 					  webBingAccountKey=param;
 	 				   }else if (cad.indexOf("<ftpUser>")>-1){
 	 					   ftpUser=param;
 				   	   }else if (cad.indexOf("<ftpPswd>")>-1){
 	 					   ftpPassword=param;
 				   	   }else if (cad.indexOf("<ftpHost>")>-1){
 				   		   ftpHost=param;
  				   	   }else if (cad.indexOf("<ftpDirectory>")>-1){
 				   		   ftpDirectory=param;
  				   	   }else if (cad.indexOf("<blogUser>")>-1){
  				   		   blogUser=param;
  				   	   }else if (cad.indexOf("<blogPswd>")>-1){
  				   		   blogPswd=param;
  				   	   }else if (cad.indexOf("<musicUpload>")>-1){
  				   		   musicUpload=param;
  				   	   }else if (cad.indexOf("<videosUpload>")>-1){
  				   		   videosUpload=param;
  				   	   }else if (cad.indexOf("<moviesUpload>")>-1){
  				   		   moviesUpload=param;
  				   	   }else if (cad.indexOf("<docsUpload>")>-1){
  				   		   docsUpload=param;
  				   	   }else if (cad.indexOf("<defaultMusicPath>")>-1){
  				   		   defaultMusicPath=param;
  				   	   }else if (cad.indexOf("<logPath>")>-1){
  				   		   logPath=param;
  				   	   }
  				   }
  			   }  			  
  			   bf.close();
  			   fr.close();
  		   }catch(Exception ex){
  			   ex.printStackTrace();
  			   Errors.showError(Errors.FILE_NOT_FOUND,"File not found: "+ fileName);
  			   //errorFileNF(fileName,f);
  		   }
  	    }
     }
 
 
 ////////////////////////////////////////other methods////////////////////////////////////////////////////
 ////////////////////////////////////////other methods////////////////////////////////////////////////////    
 ////////////////////////////////////////other methods////////////////////////////////////////////////////
     
     ////////method to paste in multiple cells in the table
     public void multiPasteInTable(){
 		String data;
 		try {
 			data = (String) sysClipboard.getData(DataFlavor.stringFlavor);
 			selectedModel=transformViewSelectedToModel(selectedView);
 			for (int i=0;i<selectedModel.size();i++){
 				switch(multiPane.getSelectedIndex()){
 					case IND_MUSIC_TAB:
 						musicTabModel.setValueAt(data, selectedModel.get(i), selectedModelColumn);
 						Disc newDisc=musicTabModel.getDiscAtRow(selectedModel.get(i));
 						Disc previousDisc=musicDataBase.getDisc(newDisc.id);
 				        //adding the undo/redo effect object
 				        undoManager.addEdit(new music.db.UnReUpdate(musicDataBase,newDisc,previousDisc,selectedModel.get(i)));		        
 				        musicDataBase.updateDiscWithoutReview(newDisc,selectedModel.get(i));	
 						break;
 					case IND_VIDEOS_TAB:
 						videosTabModel.setValueAt(data, selectedModel.get(i), selectedModelColumn);
 						Video newVid=videosTabModel.getVideoAtRow(selectedModel.get(i));
 						Video previousVid=videosDataBase.getVideo(newVid.id.intValue());
 				        //adding the undo/redo effect object
 				        undoManager.addEdit(new musicmovies.db.UnReUpdate(videosDataBase,newVid,previousVid,selectedModel.get(i)));		        
 				        videosDataBase.updateVideo(newVid,selectedModel.get(i));	
 						break;
 					case IND_MOVIES_TAB:
 						moviesTabModel.setValueAt(data, selectedModel.get(i), selectedModelColumn);
 						Movie newMovie=moviesTabModel.getMovieAtRow(selectedModel.get(i));
 				        Movie previousMovie=moviesDataBase.getMovie(newMovie.id);
 				        //adding the undo/redo effect object 
 				        undoManager.addEdit(new movies.db.UnReUpdate(moviesDataBase,newMovie,previousMovie,selectedModel.get(i)));		        
 				        moviesDataBase.updateMovie(newMovie,selectedModel.get(i));	
 						break;
 					case IND_DOCS_TAB:
 						docsTabModel.setValueAt(data, selectedModel.get(i), selectedModelColumn);
 						Doc newDoc=docsTabModel.getDocAtRow(selectedModel.get(i));
 				        Doc previousDoc=docsDataBase.getDoc(newDoc.id);
 				        //adding the undo/redo effect object 
 				        undoManager.addEdit(new docs.db.UnReUpdate(docsDataBase,newDoc,previousDoc,selectedModel.get(i)));		        
 				        docsDataBase.updateDoc(newDoc,selectedModel.get(i));	
 						break;
 				}
 
 			}
 		} catch (UnsupportedFlavorException e) {
 			Errors.showError(Errors.GENERIC_ERROR,e.toString());
 		} catch (IOException e) {
 			Errors.showError(Errors.GENERIC_ERROR,e.toString());
 		}
     }
     
     public void showCover(int type){
         boolean present=false;
         File pathDisc;
 
         if (((String) musicTabModel.getValueAt(selectedModelRow, Disc.COL_PRESENT)).compareTo("YES") == 0) present = true;
 
 		if (musicFolderConnected && present) {
 			pathDisc = (File) musicTabModel.getValueAt(selectedModelRow, Disc.COL_PATH);
 			if (!imageDealer.showImage(pathDisc, coversView,type)){
 				coversView.setIcon(null);
 				coversView.setText(COVERS_NOT_FOUND_MSG);
 			}
 		} else {
 			coversView.setText(COVERS_NOT_FOUND_MSG);
 		}
 	}
     
 //method to save current review in the database
     public void saveCurrentReview(){
     	String review = reviewView.getText();
     	review=review.replace("\"","\\\"");
     	musicTabModel.setValueAt(review,selectedModelRow,Disc.COL_REVIEW);
     	musicDataBase.updateReviewOnly(musicTabModel.getDiscAtRow(selectedModelRow));
     }
     
     public void saveCurrentVideoReview(){
     	String review = videosReviewView.getText();
     	review=review.replace("\"","\\\"");
     	videosTabModel.setValueAt(review,selectedModelRow,Disc.COL_REVIEW);
     	videosDataBase.updateReviewOnly(videosTabModel.getVideoAtRow(selectedModelRow));
     }
     
   //method to save current commments in the database
     public void saveCurrentComments(){
     	String comments = docsReviewView.getText();
     	comments=comments.replace("\"","\\\"");
     	docsTabModel.setValueAt(comments,selectedModelRow,Doc.COL_COMMENTS);
     	docsDataBase.updateCommentsOnly(docsTabModel.getDocAtRow(selectedModelRow));
     }
 
     //method to transform Rows selected in View to Model
     public List<Integer> transformViewSelectedToModel(List<Integer> selectedView){
     	selectedModel.clear();
     	for (int i=0;i<selectedView.size();i++){
     		switch(multiPane.getSelectedIndex()){
 			case IND_MUSIC_TAB:
 				selectedModel.add(new Integer(musicJTable.convertRowIndexToModel(selectedView.get(i))));
 				break;
 			case IND_VIDEOS_TAB:
 				selectedModel.add(new Integer(videosJTable.convertRowIndexToModel(selectedView.get(i))));
 				break;
 			case IND_MOVIES_TAB:
 				selectedModel.add(new Integer(moviesJTable.convertRowIndexToModel(selectedView.get(i))));
 				break;
 			case IND_DOCS_TAB:
 				selectedModel.add(new Integer(docsJTable.convertRowIndexToModel(selectedView.get(i))));
 				break;	
     		}
     	}	
     	return selectedModel;
     }
    
    public int uploadBUP(int db,File fbup){
    	
  	   	int ret=0;
     	switch(db){
     		case IND_MUSIC_TAB:
     			ret=musicDataBase.makeBackup(fbup.getAbsolutePath());
     			break;
     		case IND_VIDEOS_TAB:
     			ret=videosDataBase.makeBackup(fbup.getAbsolutePath());
     			break;
     		case IND_MOVIES_TAB:
     			ret=moviesDataBase.makeBackup(fbup.getAbsolutePath());
     			break;
     	   	case IND_DOCS_TAB:
     	   		ret=docsDataBase.makeBackup(fbup.getAbsolutePath());
     	   		break;	
     	}
     	if (ret>-1) {
     	     try{         	            
     	     	ftpManager.upload(fbup);
     	      	ret = 0;
           	    switch(db){
          			case IND_MUSIC_TAB:
         				ret=WebReader.uploadBackup(fbup.getName(),musicUpload);
           				break;
           			case IND_VIDEOS_TAB:
           				ret=WebReader.uploadBackup(fbup.getName(),videosUpload);
           				break;
           			case IND_MOVIES_TAB:
           				ret=WebReader.uploadBackup(fbup.getName(),moviesUpload);
           				break;
           			case IND_DOCS_TAB:
           				ret=WebReader.uploadBackup(fbup.getName(),docsUpload);
           				break;	
           	    }
     	        }catch(IOException e){
     	        	Errors.showError(Errors.UPLOADING_BUP,e.toString());
     	        }
     	        fbup.delete();
     	        
     		}else Errors.showError(ret);         		
        return ret;
    }
    
 
     ///////////////////////////////////HANDLERS///////////////////////////////////////////
     ///////////////////////////////////HANDLERS///////////////////////////////////////////
     ///////////////////////////////////HANDLERS///////////////////////////////////////////
 
     ///////////////////////////////////ACTION HANDLERS///////////////////////////////////////////
  
     ////////////////////////////////MENU DATABASE HANDLERS///////////////////////////////////////////////
     ////////////////////////////////MENU DATABASE HANDLERS///////////////////////////////////////////////
     
    private class SaveReviewHandler implements ActionListener {
 
        public void actionPerformed(ActionEvent event) {
     	   saveCurrentReview();
        }
    } //FIN HANDLER SAVEREVIEW
    
    private class SaveVideoReviewHandler implements ActionListener {
 
        public void actionPerformed(ActionEvent event) {
     	   saveCurrentVideoReview();
        }
    } //FIN HANDLER SAVEREVIEW
    
    private class SaveCommentsHandler implements ActionListener {
 
        public void actionPerformed(ActionEvent event) {
     	   saveCurrentComments();
        }
    } //FIN HANDLER SAVEREVIEW
 
 
 
   private class DelItemHandler implements ActionListener {
 
        public void actionPerformed(ActionEvent evento) {
     	   Disc disc;
     	   Movie movie;
     	   Doc doc;
     	   Video vid;
            if (selectedModelRow>-1) {
         	   int size = selectedModel.size();
         	   for (int row=0;row < size;row++){
 	        	   switch(multiPane.getSelectedIndex()){
 	   					case IND_MUSIC_TAB:	
 	   		        	    disc=musicTabModel.getDiscAtRow(selectedModel.get(0));
 	   		                undoManager.addEdit(new music.db.UnReDelete(musicDataBase,disc,musicTabModel));
 	   		                if (isdb){
 	   		                	musicDataBase.deleteDisc(selectedModelRow);
 	   		                }else{
 	   		                	musicTabModel.deleteDisc(selectedModelRow);
 	   		                }
 	   					break;
 	   					case IND_VIDEOS_TAB:	
 	   		        	    vid=videosTabModel.getVideoAtRow(selectedModel.get(0));
 	   		                undoManager.addEdit(new musicmovies.db.UnReDelete(videosDataBase,vid,videosTabModel));
 	   		                if (isdb){
 	   		                	videosDataBase.deleteVideo(selectedModelRow);
 	   		                }else{
 	   		                	videosTabModel.deleteVideo(selectedModelRow);
 	   		                }
 	   					break;
 	   					case IND_MOVIES_TAB:
 	   						movie=moviesTabModel.getMovieAtRow(selectedModel.get(0));
 	   						undoManager.addEdit(new movies.db.UnReDelete(moviesDataBase,movie,moviesTabModel));
 	   						if (isdb){
 	   							moviesDataBase.deleteMovie(selectedModelRow);
 	   		                }else{
 	   		                	moviesTabModel.deleteMovie(selectedModelRow);
 	   		                }
 	   						
 	   					break;	
 	   					case IND_DOCS_TAB:
 	   						doc=docsTabModel.getDocAtRow(selectedModel.get(0));
 	   						undoManager.addEdit(new docs.db.UnReDelete(docsDataBase,doc,docsTabModel));
 	   						if (isdb){
 	   							docsDataBase.deleteDoc(selectedModelRow);
 	   		                }else{
 	   		                	docsTabModel.deleteDoc(selectedModelRow);
 	   		                }
 	   		        	    
 	   					break;	
 	       			}
         	   }
            }
            else JOptionPane.showMessageDialog(f,"Please select an item\n");
        }
    } //FIN HANDLER DELITEM
 
  
   
   private class AddMItemHandler implements ActionListener {
 	  private Boolean number;
 	  private Integer num;
 	  private String snum;
 	  
       public void actionPerformed(ActionEvent evento) {
     	  number=false;
     	  while (!number){
           	snum = JOptionPane.showInputDialog("Please insert the number of items to add");
           	try{
           		num=Integer.valueOf(snum);
           		number=true;
           	}catch(NumberFormatException ex){	 
           		if (snum==null) break;
           		JOptionPane.showMessageDialog(f,"Must be an integer");
           	}
       	  }
     	  if (snum!=null){
 	    	  String loc = JOptionPane.showInputDialog("Please insert the name of Copy/Loc field for database");
 	    	  if (loc!=null){
 		    	  switch(multiPane.getSelectedIndex()){
 					case IND_MUSIC_TAB:
 						Disc disc; 
 				        for (int i=1;i<=num;i++){
 				        	disc = new Disc();
 					        disc.reset();
 					        disc.copy=loc;
 					        if (isdb){
 					        	musicDataBase.insertNewDisc(disc);
 					        }else{
 					        	 int row = musicTabModel.addDisc(disc);
 					        	 musicTabModel.setValueAt("?", row, Disc.COL_PRESENT);
 					        	 musicTabModel.setValueAt(disc.path, row, Disc.COL_PATH);
 					        }
 					
 				        }
 						break;
 					case IND_VIDEOS_TAB:
 						Video vid; 
 				        for (int i=1;i<=num;i++){
 				        	vid = new Video();
 					        vid.reset();
 					        vid.copy=loc;
 					        if (isdb){
 					        	videosDataBase.insertNewVideo(vid);
 					        }else{
 					        	videosTabModel.addVideo(vid);
 					        }
 					
 				        }
 						break;
 					case IND_MOVIES_TAB:
 						Movie movie; 
 				        for (int i=1;i<=num;i++){
 				        	movie = new Movie();
 							movie.reset();
 							movie.loc=loc;
 							if (isdb){
 								moviesDataBase.insertNewMovie(movie);
 						    }else{
 						      	moviesTabModel.addMovie(movie);
 						    }
 				        	
 				        }
 						break;
 					case IND_DOCS_TAB:
 						Doc doc; 
 				        for (int i=1;i<=num;i++){
 				        	doc = new Doc();
 							doc.reset();
 							doc.loc=loc;
 				        	if (isdb){
 					        	docsDataBase.insertNewDoc(doc);
 						    }else{
 						      	docsTabModel.addDoc(doc);
 						    }
 				        }
 						break;	
 		  		}          
 	    	  }
     	  }
       }
   } //FIN HANDLER ADDDITEMS
   
   
    private class RelDBBUHandler implements ActionListener {
 	   private int select=JOptionPane.OK_OPTION;	   
 	   
        public void actionPerformed(ActionEvent evento) {
     	   Object[] options = {"Yes","No"};
            select = JOptionPane.showOptionDialog(f,"Would you like to use default path?","Question",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,
            		null,     //do not use a custom Icon
            		options,  //the titles of buttons
            		options[0]); //default button title
            if (select!=JOptionPane.CLOSED_OPTION){
 	           	if (select==JOptionPane.OK_OPTION) backUpPath = new File(defaultMusicPath);
 	           	else backUpPath=FileDealer.selectPath(f,"Select path for music");
 	           	if (backUpPath!=null) {
     	           String[] grupos = backUpPath.list();
     	           Integer tam = grupos.length;
     	           if (tam == 0) {
     	               Errors.showWarning(Errors.WRONG_DIRECTORY,backUpPath.toString());
     	           } else{              //para todos los grupos de la carpeta
     	        	    RelateDBBUPThread relThread = new RelateDBBUPThread();
     	        	    relThread.setDaemon(true);
     	        	    relThread.start();
     		       	}
 	    	    }
            }
     	   
     	   
     	   
        }
    } //FIN HANDLER RELDBBU
    
    private class LoadFolderTempHandler implements ActionListener {
 	      
        public void actionPerformed(ActionEvent evento) {
     	   File tempFolder=FileDealer.selectPath(f,"Select path for music");
 	    	if (tempFolder!=null){
 	           String[] grupos = tempFolder.list();
 	           Integer tam = grupos.length;
 	           if (tam == 0) {
 	               Errors.showWarning(Errors.WRONG_DIRECTORY,tempFolder.toString());
 	           } else{              //para todos los grupos de la carpeta
 	        	    LoadFolderTempThread loadThread = new LoadFolderTempThread();
 	        	    loadThread.musicPath=tempFolder;
 	        	    loadThread.setDaemon(true);
 	        	    loadThread.start();
 		       	}
 	       }
        }
    } //FIN HANDLER RELDBBU
    
 
    private class AddBUDBHandler implements ActionListener {
 
 	   Integer tam=0;
        public void actionPerformed(ActionEvent evento) {
     	   auxPath=FileDealer.selectPath(f,"Path for folder with discs");
     	   reviewView.setText("");
     	   if (auxPath!=null) {
 	            String[] grupos = auxPath.list();
 	            if (grupos!=null) tam = grupos.length;
 	            if (tam == 0) {
 	                Errors.showWarning(Errors.WRONG_DIRECTORY,auxPath.getAbsolutePath());
 	            } else {
 	            	CopyThread copyThread = new CopyThread();
 	            	copyThread.path=auxPath.getAbsolutePath();
 	                copyThread.folders=grupos;
 	                copyThread.size=tam;
 	                copyThread.start();
 	            }
            }
        }
    } //FIN HANDLER ADDBUDB
 
 
     private class DBBUPHandler implements ActionListener {
 
        public void actionPerformed(ActionEvent evento) {  
     	   File auxPath=FileDealer.selectPath(f, "Path for Backup destination");
     	   if (auxPath!=null){
         	   int ret=0;
         	   switch(multiPane.getSelectedIndex()){
     	   		case IND_MUSIC_TAB:
     	   			ret=musicDataBase.makeBackup(auxPath.getAbsolutePath());
     	   			break;
     	   		case IND_VIDEOS_TAB:
     	   			ret=videosDataBase.makeBackup(auxPath.getAbsolutePath());
     	   			break;
     	   		case IND_MOVIES_TAB:
     	   			ret=moviesDataBase.makeBackup(auxPath.getAbsolutePath());
     	   			break;
     	   		case IND_DOCS_TAB:
     	   			ret=docsDataBase.makeBackup(auxPath.getAbsolutePath());
     	   			break;	
         	   }
         	   if (ret>-1) JOptionPane.showMessageDialog(f, "File created succesfully: "+auxPath.getAbsolutePath());
         	   else Errors.showError(ret);
     	   }
        }
    }  //FIN HANDLER DBBUP
     
   
     private class UploadBUPHandler implements ActionListener {
 
         public void actionPerformed(ActionEvent evento) {  
         	File fbup=FileDealer.selectFile(f, "Backup file to upload");
       	    if (fbup!=null){
          	   int ret=uploadBUP(multiPane.getSelectedIndex(),fbup);
 	           if (ret==0){
 	        	   JOptionPane.showMessageDialog(f, "Backup Upload succesful");
 	           }else Errors.showError(ret);
             }
         }
     }  //FIN HANDLER UPLPOADBUP
     
     
     private class UploadAllBUPHandler implements ActionListener {
 
         public void actionPerformed(ActionEvent evento) {  
         	
         	File fbup=FileDealer.selectFile(f, "Backup file to upload");
       	    if (fbup!=null){  
                ProgressBarWindow pw = new ProgressBarWindow();
                pw.setFrameSize(pw.dimRelate);
 	   	       if (pw.startProgBar(4)<0) {
 		        	Errors.showError(Errors.GENERIC_ERROR);
 		        	return;
 		       }
          	   int ret=uploadBUP(IND_MUSIC_TAB,fbup);
          	   pw.setPer(1, "Database Music");
          	   if (ret!=0) Errors.showError(ret,"Music DB");
          	   uploadBUP(IND_MOVIES_TAB,fbup);
          	   pw.setPer(2, "Database Movies");
          	   if (ret!=0) Errors.showError(ret,"Movies DB");
          	   uploadBUP(IND_VIDEOS_TAB,fbup);
          	   pw.setPer(3, "Database Videos");
          	   if (ret!=0) Errors.showError(ret,"Videos DB");
          	   uploadBUP(IND_DOCS_TAB,fbup);
          	   pw.setPer(4, "Database Docs");
          	   if (ret!=0) Errors.showError(ret,"Docs DB");
         	   JOptionPane.showMessageDialog(f, "Done");
             }
         }
     }  //FIN HANDLER UPLPOADBUP
 
  private class RestoreDBBUPHandler implements ActionListener {
 
        File fileIn;
 
      public void actionPerformed(ActionEvent evento) {
 
     	 fileIn=FileDealer.selectPath(f, "Backup file to upload");
    	     if (fileIn!=null){
              int ret=0;
 	      	   switch(multiPane.getSelectedIndex()){
 	  	   		case IND_MUSIC_TAB:
 	  	   			ret=musicDataBase.restoreBackup(fileIn.getAbsolutePath());
 	  	   			break;
 		  	   	case IND_VIDEOS_TAB:
 	  	   			ret=videosDataBase.restoreBackup(fileIn.getAbsolutePath());
   	   			break;
 	  	   		case IND_MOVIES_TAB:
 	  	   			ret=moviesDataBase.restoreBackup(fileIn.getAbsolutePath());
 	  	   			break;
 	  	   		case IND_DOCS_TAB:
 	  	   			ret=docsDataBase.restoreBackup(fileIn.getAbsolutePath());
 	  	   			break;	
 	      	   }
 	      	   if (ret>-1) JOptionPane.showMessageDialog(f, "File restored succesfully: "+fileIn.getAbsolutePath());
 	      	   else Errors.showError(ret);
          }
      }
    }  //FIN HANDLER DBRESTORE
  
  private class OpenCSVDBHandler implements ActionListener {
 	   
      public void actionPerformed(ActionEvent evento) {
     	 File file=FileDealer.selectFile(f,"Please select CSV file to open");
 	     if (file != null) {            
 	        RetrieveCSVThread retThread = new RetrieveCSVThread();
 	        retThread.setDaemon(true);
 	        retThread.fileName=file.getAbsolutePath();
 	        retThread.start();
 		 }
      }
  } //FIN HANDLER OPENCSVHANDLER  
  
  private class SaveCSVDBHandler implements ActionListener {
 	   
      public void actionPerformed(ActionEvent evento) {
     	 File file=FileDealer.selectFile(f,"Please select CSV file to save");
 	     if (file != null) {             
 	         StoreCSVThread storeThread = new  StoreCSVThread();
 	         storeThread.setDaemon(true);
 	         storeThread.fileName=file.getAbsolutePath();
 	         storeThread.start();
          }
      }
  } //FIN HANDLER SAVECSVHANDLER  
  
 
 
 ///////////////////////////////////////////////MENU EDIT HANDLERS\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
 ///////////////////////////////////////////////MENU EDIT HANDLERS\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     
     private class MenuUndoHandler implements ActionListener {
 
        public void actionPerformed(ActionEvent e) {
            if(undoManager.canUndo()){
                undoManager.undo();
            }else JOptionPane.showMessageDialog(f,"Nothing to undo");
        }
 
     }//MenuUndo Handler END
 
   private class MenuRedoHandler implements ActionListener {
 
        public void actionPerformed(ActionEvent e) {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }else JOptionPane.showMessageDialog(f,"Nothing to redo");
        }
    }//MenuUndo Handler END
   
   
   private class MenuFilterHandler implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			String word = JOptionPane.showInputDialog(f,"Write the word you want to filter");
 			if (word!=null){
 				switch(multiPane.getSelectedIndex()){
 	  	   		case IND_MUSIC_TAB:
 	  	   			musicTableSorter.setRowFilter(RowFilter.regexFilter("(?i)"+word));
 	  	   			break;
 	  	   		case IND_VIDEOS_TAB:
 		   			videosTableSorter.setRowFilter(RowFilter.regexFilter("(?i)"+word));
 		   			break;
 	  	   		case IND_MOVIES_TAB:
 	  	   			moviesTableSorter.setRowFilter(RowFilter.regexFilter("(?i)"+word));
 	  	   			break;
 	  	   		case IND_DOCS_TAB:
 	  	   			docsTableSorter.setRowFilter(RowFilter.regexFilter("(?i)"+word));
 	  	   			break;	
 				}
 			}			
 		}
 	}//END OF FILTER HANDLER
   
   private class MenuPasteHandler implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {	
 			multiPasteInTable();
 		}
 	}//END OF PASTE HANDLER
 
   
 ///////////////////////////////////////////COVER MENU HANDLERS///////////////////////////////
 ///////////////////////////////////////////COVER MENU HANDLERS///////////////////////////////
 ///////////////////////////////////////////COVER MENU HANDLERS///////////////////////////////
   
     private class NoCoverDiscHandler implements ActionListener {
 
        int posGuion = -1, longNombre = -1, numberFiles = 0, found = 0;
        String[] files;
        String sep = File.separator;
 
        public void actionPerformed(ActionEvent evento) {
 
     	   auxPath=FileDealer.selectPath(f, "Path for music files");
     	   if (auxPath!=null){
 	           String name = JOptionPane.showInputDialog(f, "File name for text with list without cover");
 	           if (name!=null){
 		           try {
 		               FileWriter fout = new FileWriter(name + ".txt");
 		               BufferedWriter buffer = new BufferedWriter(fout);
 		               PrintWriter printer = new PrintWriter(buffer);
 		
 		               if (auxPath.isDirectory() == false) {
 		                   Errors.showWarning(Errors.WRONG_DIRECTORY,auxPath.getAbsolutePath());
 		               } else {
 		                   String[] grupos = auxPath.list();
 		                   Integer tam = grupos.length;
 		                   if (tam == 0) {
 		                       Errors.showWarning(Errors.WRONG_DIRECTORY,auxPath.getAbsolutePath());
 		                   } else {
 		
 		                       //para todos los grupos de la carpeta
 		                       for (int j = 0; j < tam; j++) {
 		                           String nombreGrupo = grupos[j];
 		
 		                           File discosGrupoF = new File(auxPath.getAbsolutePath() + sep + nombreGrupo);
 		                           if (discosGrupoF.isDirectory() == false) {
 		                               Errors.errorSint(auxPath.getAbsolutePath() + sep + nombreGrupo);
 		                           } else {
 		                               String[] discosGrupo = discosGrupoF.list();
 		                               Integer numeroDiscos = discosGrupo.length;
 		
 		                               //para todos los discos de este grupo
 		
 		                               for (int k = 0; k < numeroDiscos; k++) {
 		                                   File discoF = new File(auxPath.getAbsolutePath() + sep + nombreGrupo + sep + discosGrupo[k]);
 		                                   if (discoF.isDirectory() == false) {
 		                                       Errors.errorSint(auxPath.getAbsolutePath() + sep+ nombreGrupo + sep + discosGrupo[k]);
 		                                   } else {
 		                                       posGuion = discosGrupo[k].indexOf("-");
 		
 		                                       if (posGuion < 0) {
 		                                           Errors.errorSint(auxPath.getAbsolutePath() + sep + nombreGrupo + sep + discosGrupo[k]);
 		                                       } else {
 		                                           longNombre = discosGrupo[k].length();
 		                                           String nombreDisco = discosGrupo[k].substring(posGuion + 1, longNombre);
 		                                           files = discoF.list();
 		                                           numberFiles = files.length;
 		                                           found = 0;
 		                                           for (int i = 0; i < numberFiles; i++) {
 		                                               files[i] = files[i].toLowerCase();
 		                                               if ((((files[i].indexOf(".jpg") > -1) || (files[i].indexOf(".gif")) > -1)) && (files[i].indexOf("front") > -1)) {
 		                                                   found = 1;
 		                                                   break;
 		                                               }
 		                                           }
 		                                           if (found == 0) {
 		                                               printer.println(nombreGrupo + " - " + nombreDisco);
 		                                           }
 		                                       }
 		                                   }
 		                               }
 		                           }
 		                       }
 		                   }
 		               }
 		               printer.close();
 		               buffer.close();
 		               fout.close();
 		           } catch (IOException e) {
 		               Errors.showError(Errors.COPYING_IOERROR,"Error writing to no found cover file");
 		           }
 	           }
            }
        }
    }  //FIN HANDLER LISTA COVERS
    
    private class CoverHandler implements ActionListener {
 
 
        String[][] listaCover;
        int numCovers = 0;
 
 
        public void actionPerformed(ActionEvent evento) {
            if (disCover.size() == 0) {
                JOptionPane.showMessageDialog(f, "No cover list loaded");
            } else {
                numCovers = disCover.size();
                listaCover = new String[numCovers][2];
                for (int i = 0; i < numCovers; i++) {
                    listaCover[i][0] = disCover.get(i).group;
                    listaCover[i][1] = disCover.get(i).title;
                }
                String[] columnas = {"Disco", "Anho"};
                JTable table = new JTable(listaCover, columnas);
                JScrollPane scrollPane = new JScrollPane(table);
                splitRight.setTopComponent(scrollPane);
            }
        }
    } //FIN HANDLER COVER
 
    private class MoveCoversHandler implements ActionListener {
 
        int posGuion1 = -1, posGuion2 = -1;
        String[] portadas;
        String dirCovers, dirDisc, nombreGrupo, nombreDisco, pathDisc;
        String sep=File.separator;
 
        public void actionPerformed(ActionEvent evento) {
 
     	   auxPath=FileDealer.selectPath(f,"Path for music files");
     	   if (auxPath!=null){
 	           dirDisc=auxPath.getAbsolutePath();
 	           auxPath=FileDealer.selectPath(f,"Path for cover files");
 	           if (auxPath!=null){
 		           dirCovers=auxPath.getAbsolutePath();		           
 		        
 		           portadas = auxPath.list();
 		           int tam = portadas.length;
 		           if (tam == 0) {
 		                Errors.showWarning(Errors.WRONG_DIRECTORY,dirCovers);
 		           } else { //para todas las portadas
 		                   for (int i = 0; i < tam; i++) {
 		                       File currentCover = new File(dirCovers + sep + portadas[i]);
 		                       //portadas[i]=portadas[i].substring(portadas[i].lastIndexOf("."));
 		                       posGuion1 = portadas[i].indexOf("-");
 		                       if (posGuion1 < 0) {
 		                           Errors.errorSint(dirCovers + sep + portadas[i]);
 		                       } else {
 		                           nombreGrupo = portadas[i].substring(0, posGuion1);
 		                           nombreGrupo = nombreGrupo.trim();
 		                           String left = portadas[i].substring(posGuion1+1);
 		                           posGuion2 = left.indexOf("-");
 		                           if (posGuion2 < 0) {
 		                               Errors.errorSint(dirCovers + sep + portadas[i]);
 		                           } else {
 		                        	   nombreDisco = left.substring(0,posGuion2);
 		                               nombreDisco = nombreDisco.trim();
 		                               pathDisc = DealMusicFiles.buscarDisco(nombreGrupo, nombreDisco, dirDisc);
 		                               if (pathDisc.compareTo("") != 0) {
 		                                   if (currentCover.canWrite()) {
 		                                       if (currentCover.renameTo(new File(pathDisc + sep + portadas[i]))) {
 		                                           reviewView.append("File moved succesfully: " + portadas[i]+"\n");
 		                                       } else {
 		                                           reviewView.append("Can write but not rename file: " + portadas[i]+ "to "+pathDisc + sep + portadas[i]+"\n");
 		                                       }
 		                                   } else {
 		                                       reviewView.append("Could not rename file: " + portadas[i]+"\n");
 		                                   }
 		                               }else reviewView.append(nombreGrupo+"//"+nombreDisco+" not found on folder "+dirDisc+"\n");
 		                           }
 		                       }
 		
 		                   }
 		               }
 		           }
 		        }
            
        }
    }  //FIN HANDLER COPIARPORTADAS
    
 
    private class CoverBackupHandler implements ActionListener {
 
        int tam = 0, posGuion;
        String dirDest, dirDisc;
        String sep=File.separator;
 
        public void actionPerformed(ActionEvent evento) {
 
     	   auxPath=FileDealer.selectPath(f, "Path for backup destination");
     	   if (auxPath!=null){
 	           dirDest=auxPath.getAbsolutePath();
 	           auxPath=FileDealer.selectPath(f, "Path for cover discs");
 	           if (auxPath!=null){
 		           dirDisc=auxPath.getAbsolutePath();
 		           String[] grupos = auxPath.list();
 		           tam = grupos.length;
 		           if (tam == 0) {
 		               Errors.showWarning(Errors.WRONG_DIRECTORY,dirDisc);
 		           } else { //para todos los grupos de la carpeta
 		                   for (int j = 0; j < tam; j++) {
 		                       String nombreGrupo = grupos[j];
 		                       File discosGrupoF = new File(dirDisc + sep + nombreGrupo);
 		                       if (discosGrupoF.isDirectory() == false) {
 		                           Errors.errorSint(dirDisc + sep + nombreGrupo);
 		                       } else {
 		                           String[] discosGrupo = discosGrupoF.list();
 		                           int numeroDiscos = discosGrupo.length; //para todos los discos de este grupo
 		                           for (int k = 0; k < numeroDiscos; k++) {
 		                               File discoF = new File(dirDisc + sep + nombreGrupo + sep + discosGrupo[k]);
 		                               if (discoF.isDirectory() == false) {
 		                                   Errors.errorSint(dirDisc + sep + nombreGrupo + sep + discosGrupo[k]);
 		                               } else {
 		                                   posGuion = discosGrupo[k].indexOf("-");
 		                                   if (posGuion < 0) {
 		                                       Errors.errorSint(dirDisc + sep + nombreGrupo + sep + discosGrupo[k]);
 		                                   } else {
 		                                       String[] listaArchivos = discoF.list();
 		                                       int numArchivos = listaArchivos.length;
 		                                       for (int i = 0; i < numArchivos; i++) {
 		                                           listaArchivos[i] = listaArchivos[i].toLowerCase();
 		                                           if (((listaArchivos[i].indexOf(".jpg") > -1) || (listaArchivos[i].indexOf(".gif")) > -1)) {
 		                                               if (listaArchivos[i].indexOf("front") > -1) {
 		                                                   //front cover found!!
 		                                                   try {
 		                                                       //copiamos portada a directorio destino
 		                                                       File fsrc = new File(dirDisc + sep + nombreGrupo + sep + discosGrupo[k] + sep + listaArchivos[i]);
 		                                                       File fdest = new File(dirDest + sep + nombreGrupo + " - " + discosGrupo[k] + " - front.jpg");
 		                                                       FileDealer.fileCopy(fsrc,fdest);
 		                                                       
 		                                                   } catch (Exception e) {
 		                                                       Errors.errorSint(dirDisc + sep + nombreGrupo + sep + discosGrupo[k]);
 		                                                   }
 		                                                   break;
 		                                               } else {
 		                                                   //covers found but not named front cover
 		                                               }
 		                                           //covers not found
 		                                           }
 		                                       }
 		                                   }
 		                               }
 		                           }
 		                       }
 		                   }
 		               }
 		       }
 	       }
        }
    }  //FIN HANDLER COVERBACKUP
    
 ///////////////////////////////////////////OTHER OPTION HANDLERS///////////////////////////////
 ///////////////////////////////////////////OTHER OPTION HANDLERS///////////////////////////////
 ///////////////////////////////////////////OTHER OPTION HANDLERS///////////////////////////////  
    
    private class CopyReviewsHandler implements ActionListener {
 
        int posGuion1 = -1, posGuion2 = -1, row=0;
        String[] reviews;
        String dirReviews, nombreGrupo, nombreDisco, nota, review;
        File currentReview;
 
        @Override
        public void actionPerformed(ActionEvent evento) {
 
     	   auxPath=FileDealer.selectPath(f, "Path for review files");
     	   if (auxPath!=null){
 	           dirReviews=auxPath.getAbsolutePath();
 	
 	           if (auxPath.isDirectory() == false) {
 	               Errors.showWarning(Errors.WRONG_DIRECTORY,dirReviews);
 	           } else {
 	               reviews = auxPath.list();
 	               int tam = reviews.length;
 	               if (tam == 0) {
 	                   Errors.showWarning(Errors.WRONG_DIRECTORY,dirReviews);
 	               } else { //para todas las reviews
 	                   for (int i = 0; i < tam; i++) {
 	                	   //System.out.println(i);
 	                	   if (reviews[i].indexOf(".txt") > -1){
 		                       currentReview = new File(dirReviews + File.separator + reviews[i]);
 		                       posGuion1 = reviews[i].indexOf("-");
 		                       if (posGuion1 < 0) {
 		                           Errors.errorSint(dirReviews + File.separator + reviews[i]);
 		                       } else {
 		                           nombreGrupo = reviews[i].substring(0, posGuion1);
 		                           nombreGrupo = nombreGrupo.trim();
 		                           posGuion2 = reviews[i].lastIndexOf("-");
 		                           if (posGuion1 >= posGuion2) {
 		                               Errors.errorSint(dirReviews + File.separator + reviews[i]);
 		                           } else {
 		                               nombreDisco = reviews[i].substring(posGuion1 + 1, posGuion2);
 		                               nombreDisco = nombreDisco.trim();
 		                               nota = reviews[i].substring(posGuion2 + 1, reviews[i].lastIndexOf("."));
 		                               nota = nota.trim();
 		                               review="";
 		                        	   try{
 		                        	       FileReader fr = new FileReader(currentReview);
 		                        		   BufferedReader bf = new BufferedReader(fr);
 		                        		   String cad;
 		                        		   while ((cad=bf.readLine())!=null) {
 		                        			   review=review+cad+"\n";
 		                        		   } 
 		                        		   bf.close();
 		                        		   fr.close();
 		                        		   Disc disc= new Disc();
 			                        	   disc=musicDataBase.getDiscByDG(nombreGrupo,nombreDisco);
 			                        	   if (disc!=null){
 				                        	   row = musicTabModel.searchDisc(disc.id);
 				                        	   review=review.replace("\"","\\\"");
 				                        	   disc.review=review;
 				                        	   disc.mark=nota;
 				                        	   if (disc.id!=0) musicDataBase.updateDisc(disc, row);
 				                        	   else Errors.writeError(Errors.SAVING_REVIEW,"Failed to save review "+nombreGrupo+ " "+nombreDisco+"\n");
 			                        	   }
 		                        		}catch(Exception ex){
 		                        		   Errors.writeError(Errors.SAVING_REVIEW,"Failed to save review "+nombreGrupo+ " "+nombreDisco+"\n"+ex.toString());
 		                        	   }
 		                           }
 		                       }
 	                   		}
 	                   }
 	               }
 	           }
 
            }
        }
    }  //FIN HANDLER COPYRREVIEWS
    
    private class ViewNewDiscsHandler implements ActionListener {
 	   
 	private Look4NewDiscsThread lookThread;
 	private String web;
 	
 	ViewNewDiscsHandler(String web){
 		   this.web=web;
 	   }
 	@Override
 	public void actionPerformed(ActionEvent arg0) { 
 		lookThread = new Look4NewDiscsThread();
 		lookThread.web=this.web;
 		lookThread.start();
 	}
    
    }
    
   
 ///////////////////////////////////////POPUPMENUS ACTIONLISTENERS\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
 ///////////////////////////////////////POPUPMENUS ACTIONLISTENERS\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    
    private class PopupMenuSortDefaultHandler implements ActionListener{
 
         public void actionPerformed(ActionEvent e) {
 			switch(multiPane.getSelectedIndex()){
   	   		case IND_MUSIC_TAB:
 	  	   		Disc.caseCompare=0;
 	            musicTabModel.sort();         
   	   			break;
   	   		case IND_VIDEOS_TAB:
 	  	   		Video.caseCompare=0;
 	            videosTabModel.sort();         
   	   			break;
   	   		case IND_MOVIES_TAB:
   	   			Movie.caseCompare=0;
   	   			moviesTabModel.sort(); 
   	   			break;
   	   		case IND_DOCS_TAB:
   	   			Doc.caseCompare=0;
   	   			docsTabModel.sort(); 
   	   			break;
 			}
                
         }
     } //ORDERING MENU HANDLER END
    
    private class PopupMenuSortByFieldHandler implements ActionListener{
 
         public void actionPerformed(ActionEvent e) {
 			switch(multiPane.getSelectedIndex()){
   	   		case IND_MUSIC_TAB:
   	            Disc.caseCompare=selectedModelColumn;
   	            musicTabModel.sort();       
   	   			break;
   	   		case IND_VIDEOS_TAB:
 	            Video.caseCompare=selectedModelColumn;
 	            videosTabModel.sort();       
 	   			break;
   	   		case IND_MOVIES_TAB:
   	   			Movie.caseCompare=selectedModelColumn;
 	            moviesTabModel.sort(); 
   	   			break;
   	   		case IND_DOCS_TAB:
   	   			Doc.caseCompare=selectedModelColumn;
 	            docsTabModel.sort(); 
   	   			break;
 			}
 
         }
     } //ORDERING MENU HANDLER END
 
    private class PopupMenuPlayHandler implements ActionListener{
 
        File pathDisc;
         public void actionPerformed(ActionEvent e) {
              pathDisc = (File) musicTabModel.getValueAt(selectedModelRow,Disc.COL_PATH);
              mp3PlayerWindow.setMusicTabModel(musicTabModel);
              mp3PlayerWindow.setPlayer(mp3Player);
              
              mp3PlayerWindow.openAndStartPlaying(pathDisc,(String)musicTabModel.getValueAt(selectedModelRow,Disc.COL_GROUP),(String)musicTabModel.getValueAt(selectedModelRow,Disc.COL_TITLE));
            
         }
     } //Playing disc
    
    private class PlayRandomHandler implements ActionListener{
 
        private Double mark=-1.0;
        private int select=JOptionPane.OK_OPTION;
 
         public void actionPerformed(ActionEvent e) {
             	mp3Player.randomPlay=true;
             	while ((mark<=0)||(mark>=10)){
 	            	String sMark = JOptionPane.showInputDialog("Please insert the minimum mark of discs which to play");
 	            	try{
 	            		if (sMark!=null){
 		            		mark=Double.valueOf(sMark).doubleValue();
 		            		if ((mark<=0)||(mark>=10)) JOptionPane.showMessageDialog(f,"Mark must be between 0 and 10");
 	            		} else break;
 	            	}catch(NumberFormatException ex){
 	            		if (mark==null) break;
 	            		JOptionPane.showMessageDialog(f,"Mark must be between 0 and 10");
 	            	}	            	
             	}
             	if ((mark!=null)&&(mark>=0)){
 		            Object[] options = {"Yes, please","No way!"};
 		            select = JOptionPane.showOptionDialog(f,"Would you like to seek in favourites songs?","Question",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,
 		            		null,     //do not use a custom Icon
 		            		options,  //the titles of buttons
 		            		options[0]); //default button title
 		            if (select!=JOptionPane.CLOSED_OPTION){
 		            	boolean fav = true;
 		            	if (select==JOptionPane.OK_OPTION) fav=true;
 		            	else fav=false;
 		            	mp3PlayerWindow.setMusicTabModel(musicTabModel);
 		                mp3PlayerWindow.setPlayer(mp3Player);
 		            	mp3PlayerWindow.openAndStartPlaying(mark, fav);
 		            }
 	            }            	
         }
     } //Playing disc
    
    private class PopupMenuPasteHandler implements ActionListener{
 
         public void actionPerformed(ActionEvent e) {
         	multiPasteInTable();
         }      
     } 
       
    private class PopupMenuViewLyricsHandler implements ActionListener{
 	   
        public void actionPerformed(ActionEvent e) {
     	   File lyricsPath=new File("");
     	   String lyricsGroup="";
     	   String lyricsAlbum="";
     	   
     	   if (((JMenuItem)e.getSource()).getName().compareTo(LYR_PLAYER_NAME)==0) {
     	    	lyricsPath = playList.getSongAtRow(selectedModelRowPlayer).discPath;
     	    	lyricsGroup = playList.getSongAtRow(selectedModelRowPlayer).group;
     	    	lyricsAlbum = playList.getSongAtRow(selectedModelRowPlayer).album;
     	   }
     	   else if (((JMenuItem)e.getSource()).getName().compareTo(LYR_MENU_NAME)==0)  {
     	    	lyricsPath = musicTabModel.getDiscAtRow(selectedModelRow).path;
     	    	lyricsGroup=musicTabModel.getDiscAtRow(selectedModelRow).group;
     	    	lyricsAlbum=musicTabModel.getDiscAtRow(selectedModelRow).title; 	    	
     	   }
     	   LyricsFrame lyricsFrame = new LyricsFrame();
     	   lyricsFrame.open(lyricsPath, lyricsGroup, lyricsAlbum);
        }
    }
    
    private class MenuLoadFilmDataHandler implements ActionListener{
 	   
 	   public void actionPerformed(ActionEvent e) {
     	   GetFilmDataThread getFilmDataThread = new GetFilmDataThread();
 	       getFilmDataThread.start();
        }
    }
    
    private class MenuLoadGroupDataHandler implements ActionListener{
 	   
 	   public void actionPerformed(ActionEvent e) {
     	   GetGroupDataThread getGroupDataThread = new GetGroupDataThread();
 	       getGroupDataThread.start();
        }
    }
    
 
    private class PopupMenuDownloadCover implements ActionListener{
   	   private String group,title,searchString;
   	   
          public void actionPerformed(ActionEvent e) {
       	   	group=musicTabModel.getDiscAtRow(selectedModelRow).group;
       	   	title=musicTabModel.getDiscAtRow(selectedModelRow).title;
       	   	searchString=group+" "+title;
       	   	imageDealer.setDisc(musicTabModel.getDiscAtRow(selectedModelRow));
       	   	imageDealer.searchImage(searchString,((JMenuItem)e.getSource()).getName());
          }
      }
    
    
    
    private class PopupMenuShowBigCoverHandler implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {	
 	        boolean present=false;
 
 	        if (((String) musicTabModel.getValueAt(selectedModelRow, Disc.COL_PRESENT)).compareTo("YES") == 0) present = true;
 
 			if (musicFolderConnected && present) {
 				Dimension dim =imageDealer.showCurrentImageInLabel(bigCoversView);
 				bigCoversFrame.setSize(dim.width+10,dim.height+50);
 				bigCoversFrame.setVisible(true);
 			}
 	   }
    }//END OF PopupMenuShowBigCoverHandler
    
    
    private class PopupMenuOpenCovers implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {	
 	        boolean present=false;
 
 	        if (((String) musicTabModel.getValueAt(selectedModelRow, Disc.COL_PRESENT)).compareTo("YES") == 0) present = true;
 
 			if (musicFolderConnected && present) {
 				imageDealer.showImages((File)musicTabModel.getValueAt(selectedModelRow,Disc.COL_PATH));
 			}
 	   }
    }//END OF PopupMenuShowBigCoverHandler
    
    
       
    ////// OTHER EVENT HANDLERS ///////////////////////////////////////////////////////////////////////////////
    ////// OTHER EVENT HANDLERS ///////////////////////////////////////////////////////////////////////////////
    ////// OTHER EVENT HANDLERS ///////////////////////////////////////////////////////////////////////////////
  
    
    private class CellEditorHandler implements CellEditorListener {
 
      
        public void editingStopped(ChangeEvent e) {
     	   switch(multiPane.getSelectedIndex()){
  	   		case IND_MUSIC_TAB:
  	   			Disc newDisc=musicTabModel.getDiscAtRow(selectedModelRow);
  	   			Disc previousDisc=musicDataBase.getDisc(newDisc.id);
  	   			//adding the undo/redo effect object
  	   			if (!newDisc.review.contains("\\\""))	newDisc.review=newDisc.review.replace("\"","\\\"");
  	   			undoManager.addEdit(new music.db.UnReUpdate(musicDataBase,newDisc,previousDisc,selectedModelRow));
  	   			musicDataBase.updateDisc(newDisc,selectedModelRow);       
  	   			break;
  	   		case IND_VIDEOS_TAB:
 	   			Video newVid=videosTabModel.getVideoAtRow(selectedModelRow);
 	   			Video previousVid=videosDataBase.getVideo(newVid.id.intValue());
 	   			//adding the undo/redo effect object
 	   			undoManager.addEdit(new musicmovies.db.UnReUpdate(videosDataBase,newVid,previousVid,selectedModelRow));
 	   			videosDataBase.updateVideo(newVid,selectedModelRow);
 	   			break;		
  	   		case IND_MOVIES_TAB:
  	   			Movie newMovie=moviesTabModel.getMovieAtRow(selectedModelRow);
  	   			Movie previousMovie=moviesDataBase.getMovie(newMovie.id);
  	   			//adding the undo/redo effect object
  	   			undoManager.addEdit(new movies.db.UnReUpdate(moviesDataBase,newMovie,previousMovie,selectedModelRow));
  	   			moviesDataBase.updateMovie(newMovie,selectedModelRow);
  	   			break;		
  	   		case IND_DOCS_TAB:
  	   			Doc newDoc=docsTabModel.getDocAtRow(selectedModelRow);
  	   			Doc previousDoc=docsDataBase.getDoc(newDoc.id);
  	   			//adding the undo/redo effect object
  	   			undoManager.addEdit(new docs.db.UnReUpdate(docsDataBase,newDoc,previousDoc,selectedModelRow));
  	   			docsDataBase.updateDoc(newDoc,selectedModelRow);
  	   			break;	
 			}
            
        }
 
        public void editingCanceled(ChangeEvent e) {}
    }//END OF CELLEDITOR HANDLER
    
   
    //EDITOR HANDLER FOR COMBOBOX IN DOCSTABLE 
 
   public class ComboCellEditor extends AbstractCellEditor implements TableCellEditor{
 
 	private static final long serialVersionUID = 1L;
 	JComboBox combo;
 		
 	   public ComboCellEditor() {
 		   this.combo = new JComboBox();
 		   for (DocTheme docTheme : DocTheme.values()){
 			   combo.addItem(docTheme);
 		   }
 		   ComboActionListener comboAction = new ComboActionListener();
 		   combo.addItemListener(comboAction);
 	   }	
 	   @Override
 	   public Object getCellEditorValue() {
 		   return combo.getSelectedItem();
 	   }
 	   
 	  
 	@Override
 	public Component getTableCellEditorComponent(JTable table, Object value,
 			boolean isSelected, int row, int column) {
 		selectedViewRow = row;       
         lastSelectedViewRow = row; 
         selectedViewColumn = column;
         selectedView.clear();
  		selectedModelRow = table.convertRowIndexToModel(selectedViewRow);
  		lastSelectedModelRow = table.convertRowIndexToModel(lastSelectedViewRow);
  		selectedModelColumn = table.convertColumnIndexToModel(selectedViewColumn);
 		combo.setSelectedItem(value);
 		return combo;
 	}
 	   
    }
 
    //EVENT STATE HANDLER FOR COMBOBOX IN DOCSTABLE
   private class ComboActionListener implements ItemListener{
 
 		@Override
 	public void itemStateChanged(ItemEvent e) {
 		DocTheme docTheme;
 		docTheme=(DocTheme)((JComboBox)e.getSource()).getSelectedItem();
 		if (e.getStateChange()==ItemEvent.SELECTED){
 			Doc newDoc=docsTabModel.getDocAtRow(selectedModelRow);		
 			newDoc.setTheme(docTheme);
 		   	Doc previousDoc=docsDataBase.getDoc(newDoc.id);
 		   	//adding the undo/redo effect object
 		   	undoManager.addEdit(new docs.db.UnReUpdate(docsDataBase,newDoc,previousDoc,selectedModelRow));
 		   	docsDataBase.updateDoc(newDoc,selectedModelRow);
 		}	
 		
 	}
 
    }
      
   //SELECTING CELLS IN EACH TABLE
    private class SelectItemHandler implements ListSelectionListener {
 
        public void valueChanged(ListSelectionEvent e) {
           if(e.getValueIsAdjusting()){return;} //to avoid double triggering when deselecting/selecting cells
     
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (!lsm.isSelectionEmpty()){
                selectedViewRow = lsm.getMinSelectionIndex();       
                lastSelectedViewRow = lsm.getMaxSelectionIndex(); 
                
                JTable currentTable=null;
                switch(multiPane.getSelectedIndex()){
 	               	case IND_MUSIC_TAB:
 	    	   			currentTable=musicJTable;
 	    	   			break;
 	               	case IND_VIDEOS_TAB:
 	    	   			currentTable=videosJTable;
 	    	   			break;
 	    	   		case IND_MOVIES_TAB:
 	    	   			currentTable=moviesJTable;
 	    	   			break;
 	    	   		case IND_DOCS_TAB:
 	    	   			currentTable=docsJTable;
 	    	   			break;
    				}
                selectedViewColumn = currentTable.getSelectedColumn();
                selectedView.clear();
                selectedModel.clear();
 	   		   selectedModelRow = currentTable.convertRowIndexToModel(selectedViewRow);
 	   		   lastSelectedModelRow = currentTable.convertRowIndexToModel(lastSelectedViewRow);
 	   		   selectedModelColumn = currentTable.convertColumnIndexToModel(selectedViewColumn);
              //only way to find out which rows are selected in multiple selection mode (for a DefaultListSelectionModel)
 	   		   
                for (int i = selectedViewRow; i <= lastSelectedViewRow; i++) { 
                    if (lsm.isSelectedIndex(i)) {
                 	   selectedView.add(new Integer(i));
                 	   selectedModel.add(currentTable.convertRowIndexToModel(new Integer(i)));
                    }
                }
                if (multiPane.getSelectedIndex()==IND_MUSIC_TAB){
             	   String review=(String)musicTabModel.getValueAt(selectedModelRow, Disc.COL_REVIEW);
             	   review=review.replace("\\\"","\"");
             	   reviewView.setText(review);
             	   imageDealer.setDisc(musicTabModel.getDiscAtRow(selectedModelRow));
             	   if (musicFolderConnected) showCover(ImageDealer.FRONT_COVER);
                }
                if (multiPane.getSelectedIndex()==IND_VIDEOS_TAB){
             	   String comment=(String)videosTabModel.getValueAt(selectedModelRow, Video.COL_REVIEW);
             	   comment=comment.replace("\\\"","\"");
             	   videosReviewView.setText(comment);
                }
                if (multiPane.getSelectedIndex()==IND_DOCS_TAB){
             	   String comment=(String)docsTabModel.getValueAt(selectedModelRow, Doc.COL_COMMENTS);
             	   comment=comment.replace("\\\"","\"");
             	   docsReviewView.setText(comment);
                }
            }   
        }      
    } //FIN HANDLER SELECCION DE DISCO
    
    
    private class SelectColumnHandler implements ListSelectionListener {
 
        public void valueChanged(ListSelectionEvent e) {
           if(e.getValueIsAdjusting()){return;} //to avoid double triggering when deselecting/selecting cells
     
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (!lsm.isSelectionEmpty()){
                JTable currentTable=null;
                switch(multiPane.getSelectedIndex()){
 	               	case IND_MUSIC_TAB:
 	    	   			currentTable=musicJTable;
 	    	   			break;
 	               	case IND_VIDEOS_TAB:
 	    	   			currentTable=videosJTable;
 	    	   			break;
 	    	   		case IND_MOVIES_TAB:
 	    	   			currentTable=moviesJTable;
 	    	   			break;
 	    	   		case IND_DOCS_TAB:
 	    	   			currentTable=docsJTable;
 	    	   			break;
    				}
                selectedViewColumn = currentTable.getSelectedColumn();
 	   		   selectedModelColumn = currentTable.convertColumnIndexToModel(selectedViewColumn);
            }   
        }      
    } //FIN HANDLER SELECCION DE DISCO
    
   //////////////////////////////////KEYBOARD LISTENERS///////////////////////////////////////
    
    //LISTENER FOR KEYBOARD USED FOR SEARCHING 
    private class TableKeyListener extends KeyAdapter {
 
        char letter;
        long currentTime;
        int startingRow;
        
        @Override
        public void keyTyped(KeyEvent e) {
            //this function selects the first cell (in the selected column) that starts with the key typed
     	   letter=e.getKeyChar();
     	   currentTime=System.currentTimeMillis();
     	   if (currentTime-lastTime<1500){
     		   currentCharPos++;
     		   startingRow=selectedModelRow;
     	   }
     	   else{ 
     		   currentCharPos=0;
     		   startingRow=0;
     	   }
     	   lastTime=currentTime;
     	   if ((letter >= 'a' && letter <= 'z') || (letter>='A' && letter<='Z')||(letter==' ')) {
     		   switch(multiPane.getSelectedIndex()){
 	   	   		case IND_MUSIC_TAB:
 		   	   		if (musicJTable.getCellEditor()!=null) {
 	    		   		musicJTable.getCellEditor().cancelCellEditing();
 						int row = musicTabModel.searchFirstElementWithLetter(letter,selectedModelColumn,currentCharPos,startingRow);
 						if (selectedViewColumn < 0) selectedViewColumn = 0;
 						if (row>-1) musicJTable.changeSelection(musicJTable.convertRowIndexToView(row),selectedViewColumn, false, false);
 	    		   	}
 	   	   			break;
 	   	   		case IND_VIDEOS_TAB:
 		   	   		if (videosJTable.getCellEditor()!=null) {
 	    		   		videosJTable.getCellEditor().cancelCellEditing();
 						int row = videosTabModel.searchFirstElementWithLetter(letter,selectedModelColumn,currentCharPos,startingRow);
 						if (selectedViewColumn < 0) selectedViewColumn = 0;
 						if (row>-1) videosJTable.changeSelection(videosJTable.convertRowIndexToView(row),selectedViewColumn, false, false);
 	    		   	}
 	   	   			break;
 	   	   		case IND_MOVIES_TAB:
 		   	   		if (moviesJTable.getCellEditor()!=null) {
 	    		   		moviesJTable.getCellEditor().cancelCellEditing();
 						int row = moviesTabModel.searchFirstElementWithLetter(letter,selectedModelColumn,currentCharPos,startingRow);
 						if (selectedViewColumn < 0) selectedViewColumn = 0;
 						if (row>-1) moviesJTable.changeSelection(moviesJTable.convertRowIndexToView(row),selectedViewColumn, false, false);
 	    		   	}
 	   	   			break;
 	   	   		case IND_DOCS_TAB:
 		   	   		if (docsJTable.getCellEditor()!=null) {
 	    		   		docsJTable.getCellEditor().cancelCellEditing();
 						int row = docsTabModel.searchFirstElementWithLetter(letter,selectedModelColumn,currentCharPos,startingRow);
 						if (selectedViewColumn < 0) selectedViewColumn = 0;
 						if (row>-1) docsJTable.changeSelection(docsJTable.convertRowIndexToView(row),selectedViewColumn, false, false);
 	    		   	}
 	   	   			break;	
 	  			}
     		   	
     		}
        }
    } //TABLEKEYLISTENERHANDLER END
    
      
    private class TabbedPaneListener implements ChangeListener{
 
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		
 		switch(((JTabbedPane)e.getSource()).getSelectedIndex()){
 				case IND_MUSIC_TAB:
 					menuMusicOptions.setEnabled(true);
 	   	   			menuRelDBBU.setEnabled(true);
 	   	   			menuAddBUDB.setEnabled(true);
 	   	   			menuLoadFilmData.setEnabled(false);	
 	   	   			if (musicFolderConnected) menuPlay.setEnabled(true);
 	   	   		    if (musicFolderConnected) menuViewLyrics.setEnabled(true);
 	   	   		    if (musicFolderConnected) menuPlayRandom.setEnabled(true);
 	   	   		    if (musicFolderConnected) menuOpcionesCoverBackup.setEnabled(true);
 	   	   		    if (musicFolderConnected) menuOpcionesCovers.setEnabled(true);
 	   	   		    if (musicFolderConnected) menuDownloadCover.setEnabled(true);
 	   	   			break;
 	   	   		case IND_MOVIES_TAB:
 	   	   			menuLoadFilmData.setEnabled(true);	
 	   	   			break;
 	   	   		default:
 	   	   			menuMusicOptions.setEnabled(false);
 	   	   			menuRelDBBU.setEnabled(false);
 	   	   			menuAddBUDB.setEnabled(false);
 	   	   			menuPlay.setEnabled(false);
 	   	   			menuViewLyrics.setEnabled(false);
 	   	   		    menuPlayRandom.setEnabled(false);
 	   	   		    menuLoadFilmData.setEnabled(false);
 	   	   		    menuDownloadCover.setEnabled(false);
 	   	   			break;
 	  			}
 	}
 	   
    }
 
    
    public class AbstractActionsHandler extends AbstractAction{
 
 		private static final long serialVersionUID = 1L;
 		private int mode=-1;
 		
 		public AbstractActionsHandler(int mode){
 			this.mode=mode;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			switch (mode){
 				case PASTE_IN_TABLE:     //PASTE IN TABLE
 					multiPasteInTable();
 					break;
 				case SAVE_REVIEW:     //SAVE REVIEW
 					saveCurrentReview();
 					break;
 				case SAVE_VIDEO_REVIEW:     //SAVE VIDEO REVIEW
 					saveCurrentVideoReview();
 					break;
 				case SAVE_COMMENTS:   //SAVE COMMENTS
 					saveCurrentComments();
 				default:
 					break;							
 			}			
 		}
    	
    }
 
    /////////////////////POPUPS LISTENERS/////////////////////////////////////////////////////////////////////
    
    ///////////////////main table popup/////////////////////////////////
    private class PopupTableListener extends MouseAdapter {
 
        @Override
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e)) {
                Point p = e.getPoint();// get the coordinates of the mouse click
                JTable currentTable = null;
                switch(multiPane.getSelectedIndex()){
 		   	   		case IND_MUSIC_TAB:
 		   	   			currentTable=musicJTable;
 		   	   			break;
 			   	   	case IND_VIDEOS_TAB:
 		   	   			currentTable=videosJTable;
 		   	   			break;
 		   	   		case IND_MOVIES_TAB:
 		   	   			currentTable=moviesJTable;
 		   	   			break;
 		   	   		case IND_DOCS_TAB:
 		   	   			currentTable=docsJTable;
 		   	   			break;	
 	  			}
                 selectedViewRow = currentTable.rowAtPoint(p);
   	   			selectedModelRow = currentTable.convertRowIndexToModel(selectedViewRow);
   	   			selectedViewColumn = currentTable.columnAtPoint(p);
   	   			selectedModelColumn = currentTable.convertColumnIndexToModel(selectedViewColumn);
   	   			currentTable.changeSelection(selectedViewRow, selectedViewColumn, false, false);              
            }
            if (e.isPopupTrigger()) {
                popupTable.show(e.getComponent(),e.getX(), e.getY());
            }
        }
 
    } //POPUPLISTENER HANDLER END
 
    ////////////////////////////////review popup////////////////////////////////
    private class PopupReviewListener extends MouseAdapter{
        @Override
       public void mousePressed(MouseEvent e) {
     	   if (SwingUtilities.isRightMouseButton(e)){
           	popupReview.show(e.getComponent(),e.getX(), e.getY());         	
           }
       }
   }
    
    ////////////////////////////////review popup////////////////////////////////
    private class PopupVideoReviewListener extends MouseAdapter{
        @Override
       public void mousePressed(MouseEvent e) {
     	   if (SwingUtilities.isRightMouseButton(e)){
           	popupVideoReview.show(e.getComponent(),e.getX(), e.getY());         	
           }
       }
   }
    
   ////////////////////////////////comments popup////////////////////////////////
    private class PopupCommentsListener extends MouseAdapter{
        @Override
       public void mousePressed(MouseEvent e) {
     	   if (SwingUtilities.isRightMouseButton(e)){
           	popupComments.show(e.getComponent(),e.getX(), e.getY());         	
           }
       }
   }
    
    //this class shows the popup for showing the cover in big frame, it also changes the cover to front and back
    private class ChangeCoverListener extends MouseAdapter{
        @Override
       public void mousePressed(MouseEvent e) {
           if (SwingUtilities.isLeftMouseButton(e)) {
              if (ImageDealer.frontCover) {
                  if (musicFolderConnected) showCover(ImageDealer.BACK_COVER);
              }
              else {
             	 if (musicFolderConnected) showCover(ImageDealer.FRONT_COVER);
              }           
           } else if (SwingUtilities.isRightMouseButton(e)){
           	popupCover.show(e.getComponent(),e.getX(), e.getY());         	
           }
       }
   }
    
    
 //////////////////////////////////////END OF HANDLERS/////////////////////////////////////////////////////////////// 
    
    
    public class PlayerTableRenderer extends JLabel implements TableCellRenderer {
 	   
 		private static final long serialVersionUID = 1L;
 
 		public PlayerTableRenderer() {
 			super();
 		}
 
 		public PlayerTableRenderer(String arg0) {
 			super(arg0);
 		}
 
 		@Override
 		public Component getTableCellRendererComponent(JTable table, Object value,
 				boolean isSelected, boolean isFocused, int row, int col) {
 			
 			Font font;
 			Border loweredBevel = BorderFactory.createLoweredBevelBorder();
 			//Border raisedBevel = BorderFactory.createRaisedBevelBorder();
 			//Border border = BorderFactory.createCompoundBorder(loweredBevel,raisedBevel);
 			int modelRow=playListTable.convertRowIndexToModel(row);
 			if ((Boolean)playList.getValueAt(modelRow,TabModelPlayList.COL_CURRENT_SONG)){
 				this.setBackground(Color.CYAN);
 				font= new Font("Arial",Font.BOLD,12);
 				this.setFont(font);
 			} else {
 				this.setBackground(Color.YELLOW);
 				font= new Font("Arial",Font.PLAIN,12);
 				this.setFont(font);
 			}
 			
 			if (isSelected){
 				this.setBorder(loweredBevel); 
 				this.setBackground(Color.getHSBColor(50,147,1635));
 			}
 			else this.setBorder(BorderFactory.createEmptyBorder());
 			this.setOpaque(true);
 			this.setText((String)value);
 			return this;
 		} 
 		   
 	   }
    
    
    
    /////////////////////////////THREADS\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    /////////////////////////////THREADS\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\   
    /////////////////////////////THREADS\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    
    
   //RELATE DATABASE BACKUP/////////////////////////////////////////////////////////////////////////
    public class RelateDBBUPThread extends Thread {
 	   int posGuion = -1, longNombre = -1, numberFiles = 0;
        String[] files;
        String discsNF = "";
        String sep = File.separator;
        
 		public RelateDBBUPThread() {
 			super();
 		}
 
 		@Override
 		public void run() {
      	   String[] grupos = backUpPath.list();
            Integer tam = grupos.length;
            ProgressBarWindow pw = new ProgressBarWindow();
            pw.setFrameSize(pw.dimRelate);
 	       if (pw.startProgBar(tam)<0) {
 	        	Errors.showError(Errors.GENERIC_ERROR);
 	        	return;
 	       }
            for (int j = 0; j < tam; j++) {
                String nombreGrupo = grupos[j];
         	   pw.setPer(j+1, "Loking for titles of "+nombreGrupo);
                infoText.setText("Loking for titles of "+nombreGrupo);
                File discosGrupoF = new File(backUpPath + sep + nombreGrupo);               
                if (discosGrupoF.isDirectory() == false) {
                	   Errors.errorSint(backUpPath + sep + nombreGrupo);
                } else {
                    String[] discosGrupo = discosGrupoF.list();
                    Integer numeroDiscos = discosGrupo.length;
 
                    //para todos los discos de este grupo
 
                    for (int k = 0; k < numeroDiscos; k++) {
                        File discoF = new File(backUpPath + sep + nombreGrupo +sep + discosGrupo[k]);
                        if (discoF.isDirectory() == false) {
                        } else {
                            posGuion = discosGrupo[k].indexOf("-");
 
                            if (posGuion < 0) {
                                Errors.errorSint(backUpPath + sep + nombreGrupo + sep + discosGrupo[k]);
                            } else {
                                Disc disco = new Disc();
                                String anho = discosGrupo[k].substring(0, posGuion);
                                anho = anho.trim();
                                try {
                                    //Long anhoLong = Long.decode(anho);
                                    longNombre = discosGrupo[k].length();
                                    String nombreDisco = discosGrupo[k].substring(posGuion + 1, longNombre);
                                    //creamos nuevo disco con los datos leidos
                                    disco.title = nombreDisco.trim();
                                    disco.group = nombreGrupo;
                                    disco.year = anho;
                                    disco.path = discoF;
                                    //OPCIONAL!!
                                    //busqueda de carpetas llamadas cover para avisar que las portadas estan ahi
                                    /*files = discoF.list();
                                    numberFiles = files.length;
                                    for (int i = 0; i < numberFiles; i++) {
                                        File fileArchivo = new File(backUpPath + sep + nombreGrupo + sep + discosGrupo[k] + "//" + files[i]);
                                        if (fileArchivo.isDirectory()) {
                                            files[i] = files[i].toLowerCase();
 
                                            if (files[i].indexOf("cover") > -1) {
                                                disCover.add(disco);
                                            }
                                        }
                                    }*/
                                    int pos;
                                    if((pos=musicTabModel.searchDisc(disco.group,disco.title))!=-1){
                                        musicTabModel.setValueAt("YES",pos,Disc.COL_PRESENT);
                                        musicTabModel.setValueAt(disco.path, pos,Disc.COL_PATH);
                                    }else{
                                        //reviewView.append("Disc not found in Database:"+nombreGrupo+"::"+discosGrupo[k]+"\n");
                                        discsNF=discsNF+"Disc not found in Database:"+nombreGrupo+"::"+discosGrupo[k]+"\n";
                                    }
                                    musicFolderConnected=true;
                                    menuPlay.setEnabled(true);
                                    menuPlayRandom.setEnabled(true);
                                    menuViewLyrics.setEnabled(true);
                                    menuOpcionesCovers.setEnabled(true);
                                    menuOpcionesCopiarPortadas.setEnabled(true);
                                    menuOpcionesCoverBackup.setEnabled(true);
                                    menuDownloadCover.setEnabled(true);
                                    
                                } catch (NumberFormatException e) {
                                    Errors.errorSint(backUpPath + sep + nombreGrupo + sep + discosGrupo[k]);
                                }
 
                              }
                           }
                        }
                    infoText.setText(discsNF);
                    }
                }
       	   infoFrame.setVisible(true);
 		}		
   }
    
    
    
    //LOAD FOLDER TEMP /////////////////////////////////////////////////////////////////////////
    public class LoadFolderTempThread extends Thread {
 	   private int posGuion = -1, longNombre = -1;
 	   private String discsNF = "";
 	   private String sep = File.separator;
        public File musicPath;
        
 		public LoadFolderTempThread() {
 			super();
 		}
 
 		@Override
 		public void run() {
      	   String[] grupos = musicPath.list();
            Integer tam = grupos.length;
            ProgressBarWindow pw = new ProgressBarWindow();
            pw.setFrameSize(pw.dimRelate);
 	       if (pw.startProgBar(tam)<0) {
 	        	Errors.showError(Errors.GENERIC_ERROR);
 	        	return;
 	       }
            musicTabModel.clearData();
            for (int j = 0; j < tam; j++) {
                String nombreGrupo = grupos[j];
                File discosGrupoF = new File(musicPath + sep + nombreGrupo);   
                pw.setPer(j,"Discs of "+nombreGrupo);
                if (discosGrupoF.isDirectory() == false) {
                	   Errors.errorSint(musicPath + sep + nombreGrupo);
                } else {
                    String[] discosGrupo = discosGrupoF.list();
                    Integer numeroDiscos = discosGrupo.length;
 
                    //para todos los discos de este grupo
 
                    for (int k = 0; k < numeroDiscos; k++) {
                        File discoF = new File(musicPath + sep + nombreGrupo +sep + discosGrupo[k]);
                        if (discoF.isDirectory() == false) {
                        } else {
                            posGuion = discosGrupo[k].indexOf("-");
 
                            if (posGuion < 0) {
                                Errors.errorSint(musicPath + sep + nombreGrupo + sep + discosGrupo[k]);
                            } else {
                                Disc disco = new Disc();
                                String anho = discosGrupo[k].substring(0, posGuion);
                                anho = anho.trim();
                                try {
                                    //Long anhoLong = Long.decode(anho);
                                    longNombre = discosGrupo[k].length();
                                    String nombreDisco = discosGrupo[k].substring(posGuion + 1, longNombre);
                                    //creamos nuevo disco con los datos leidos
                                    disco.reset();
                                    disco.title = nombreDisco.trim();
                                    disco.group = nombreGrupo;
                                    disco.year = anho;
                                    disco.path = discoF;
                                    disco.present="YES";
                                    musicTabModel.addDisc(disco);
 
                                    musicFolderConnected=true;
                                    menuPlay.setEnabled(true);
                                    menuPlayRandom.setEnabled(true);
                                    menuViewLyrics.setEnabled(true);
                                    menuOpcionesCovers.setEnabled(true);
                                    menuOpcionesCopiarPortadas.setEnabled(true);
                                    menuOpcionesCoverBackup.setEnabled(true);
                                    menuDownloadCover.setEnabled(true);
                                    
                                } catch (NumberFormatException e) {
                                    Errors.errorSint(musicPath + sep + nombreGrupo + sep + discosGrupo[k]);
                                }
 
                              }
                           }
                        }
                    infoText.setText(discsNF);
                    }
                }
            pw.closeProgBar();
       	   if (infoText.getText().length()>0) infoFrame.setVisible(true);
 		}		
   }
    
    
    //RETRIEVE CSV DATABASE/////////////////////////////////////////////////////////////////////////  
    public class RetrieveCSVThread extends Thread {
 	   String fileName;
        
 		public RetrieveCSVThread() {
 			super();
 		}
 
 		@Override
 		public void run() {
 			switch(multiPane.getSelectedIndex()){
 	   	   		case IND_MUSIC_TAB:
 	   	   			musicTabModel.setAllDataString(dbCSV.readFile(fileName,music.db.DataBaseTable.columns));
 	   	   			isMusicInCSV=true;
 	   	   			break;
 	   	   		case IND_VIDEOS_TAB:
 		   	   		videosTabModel.setAllDataString(dbCSV.readFile(fileName,musicmovies.db.DataBaseTable.columns));
 	   	   			isVideosInCSV=true;
 	   	   			break;
 	   	   		case IND_MOVIES_TAB:
 		   	   		moviesTabModel.setAllDataString(dbCSV.readFile(fileName,movies.db.DataBaseTable.columns));
 	   	   			isMoviesInCSV=true;
 	   	   			break;
 	   	   		case IND_DOCS_TAB:
 		   	   		docsTabModel.setAllDataString(dbCSV.readFile(fileName,docs.db.DataBaseTable.columns));
 	   	   			isDocsInCSV=true;
 	   	   			break;	
 			}
 
 	   		isdb=false;
         }	
   }
    
    //STORE CSV DATABASE/////////////////////////////////////////////////////////////////////////  
    public class StoreCSVThread extends Thread {
 	   String fileName;
        
 		public StoreCSVThread() {
 			super();
 		}
 
 		@Override
 		public void run() {
 			int ret=0;
 			switch(multiPane.getSelectedIndex()){
 	   	   		case IND_MUSIC_TAB:
 	   	   			ret=musicTabModel.saveToCSV(fileName);
 	   	   			isMusicInCSV=true;
 	   	   			break;
 		   	   	case IND_VIDEOS_TAB:
 	   	   			ret=videosTabModel.saveToCSV(fileName);
 	   	   			isVideosInCSV=true;
 	   	   			break;
 	   	   		case IND_MOVIES_TAB:
 		   	   		ret=moviesTabModel.saveToCSV(fileName);
 	   	   			isMoviesInCSV=true;
 	   	   			break;
 	   	   		case IND_DOCS_TAB:
 		   	   		ret=docsTabModel.saveToCSV(fileName);
 	   	   			isDocsInCSV=true;
 	   	   			break;	
 			}
 			if (ret>-1) JOptionPane.showMessageDialog(f, "File created succesfully: "+fileName);
      	    else Errors.showError(ret);
         }
 		
   }
    
    
    //TIMER/////////////////////////////////////////////////////////////////////////    
    public class TimerThread extends Thread {
 
 	   public boolean closed = false;
 	   public boolean paused = false;
 
 		public TimerThread() {
 			super();
 		}
 
 		@Override
 		public void run() {
 			try {
 				if (mp3Player != null) {
 					do {
 						if (!paused) {
 							songSlider.setValue((int)mp3Player.getPosition()/1000);
 							Long sec = mp3Player.getPosition()/1000;
 				            Long min = sec / 60;
 				            sec = sec % 60;
 				            String timeSt;
 				            if (sec<10) timeSt= Long.toString(min) + ":0" + Long.toString(sec);
 				            else timeSt= Long.toString(min) + ":" + Long.toString(sec);
 				            if (mp3Player.currentSong<mp3Player.list.getRowCount()){
 				            	String titleShown;
 				              	if (mp3Player.list.getSongAtRow(mp3Player.currentSong).tagTitle==null)
 				              			titleShown=mp3Player.list.getSongAtRow(mp3Player.currentSong).name;
 				            	else {
 				              		if (mp3Player.list.getSongAtRow(mp3Player.currentSong).tagTitle.compareTo("")==0)
 				              			titleShown=mp3Player.list.getSongAtRow(mp3Player.currentSong).name;
 				              		else titleShown=mp3Player.list.getSongAtRow(mp3Player.currentSong).tagTitle;
 				            	}
 				            	songInformation.setText("Playing: "+ titleShown+"  "+ timeSt);
 				            }
 						}
 						Thread.sleep(1000);
 					} while (!closed);
 				}
 			} catch (Exception e) {
 				Errors.showError(Errors.GENERIC_ERROR,e.toString());
 			}
 		}
 
 	/*	public void waitThis(){
        	try {
        		synchronized(this){
            		wait();
            	}
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
        }*/
    }
   
    //COPY FOLDERS TO BACKUP/////////////////////////////////////////////////////////////////////////  
    public class CopyThread extends Thread {
 
 	   public boolean end = false;
 	   public String[] folders;
 	   public String path;
 	   public int size;
        int posGuion = -1, longNombre = -1;
        String[] files;
        File currentDisc;
        String sep = File.separator; 
        boolean exists;
 
 		public CopyThread() {
 			super();
 		}
 
 		@Override
 		public void run() {
 
 			 ProgressBarWindow pw = new ProgressBarWindow(true,false);
 	         pw.setFrameSize(pw.dimRelate);
 		     if (pw.startProgBar(size)<0) {
 		       	Errors.showError(Errors.GENERIC_ERROR);
 		       	return;
 		     }
      
 			 for (int j = 0; j < size; j++) {
 				 if (pw.aborted) {
                 	 pw.closeProgBar();
                 	 break;
                  }
                  String nombreGrupo = folders[j];                
                  pw.setPer(j+1, "Adding discs of "+nombreGrupo);
                  File discosGrupoF = new File(path + sep + nombreGrupo);
                  if (discosGrupoF.isDirectory() == false) {
                       Errors.errorSint(path + sep + nombreGrupo);
                  } else {
                      String[] discosGrupo = discosGrupoF.list();
                      Integer numeroDiscos = discosGrupo.length;
                      File groupBUpPath=DealMusicFiles.buscarGrupo(nombreGrupo,backUpPath);
                      exists=false;
                      if (groupBUpPath!=null){
                         //group folder already exists in backup
                          exists=FileDealer.copyFiles(discosGrupoF.getAbsoluteFile(),groupBUpPath.getAbsoluteFile());
                          if (!exists){
                         	 reviewView.append("Added folder "+discosGrupoF.getAbsolutePath()+" to "+groupBUpPath.getAbsolutePath()+"\n");
                          }
                      }else {
                          exists=FileDealer.copyFolder(discosGrupoF.getAbsoluteFile(),backUpPath.getAbsoluteFile());
                          if (!exists){
                         	 reviewView.append("Added folder "+discosGrupoF.getAbsolutePath()+" to "+backUpPath.getAbsolutePath()+"\n");
                          }
                      }
                      reviewView.revalidate();
                     //for every disc in the group folder we insert an entry in the database
 
                      if (!exists) {
 							for (int k = 0; k < numeroDiscos; k++) {
 								currentDisc = new File(path + sep+ nombreGrupo + sep+ discosGrupo[k]);
 								if (currentDisc.isDirectory() == false) {
 									Errors.errorSint(currentDisc.getAbsolutePath());
 								} else {
 									posGuion = discosGrupo[k].indexOf("-");
 									if (posGuion < 0) {
 										Errors.errorSint(currentDisc.getAbsolutePath());
 									} else {
 										Disc disco = new Disc();
 										String anho = discosGrupo[k].substring(0, posGuion);
 										anho = anho.trim();
 										try {
 											//Long anhoLong = Long.decode(anho);
 											longNombre = discosGrupo[k].length();
 											String nombreDisco = discosGrupo[k].substring(posGuion + 1,longNombre);
 											// creamos nuevo disco con los datos leidos
 											disco.reset();
 											disco.title = nombreDisco.trim();
 											disco.group = nombreGrupo;
 											disco.year = anho;
 											disco.review=" ";
 											disco.path = currentDisc;
 											if (groupBUpPath!=null){
 												//copiamos la procedencia del grupo de algun elemento ya existente de ese grupo
 												disco.loc=musicDataBase.getDiscByGroupName(nombreGrupo).loc;												
 											}
 											// anhadimos el disco tanto al backup como a la base de datos
 											musicDataBase.insertNewDisc(disco);
 										} catch (NumberFormatException e) {
 											Errors.errorSint(currentDisc.getAbsolutePath());
 										}
 									}
 								}
 							}
 						}else{							
 			                 if (Errors.confirmDialog("Existing file or some error while copying, continue with the rest?")==JOptionPane.NO_OPTION){
 			                    break;
 			                 }			                    	
 			             }
 					}
              }
 			 JOptionPane.showMessageDialog(f, "Finished operation");
 			 pw.closeProgBar();
 		}
 		
    }
   
    //NEW DISCS OF GROUPS/////////////////////////////////////////////////////////////////////////  
    public class Look4NewDiscsThread extends Thread {
 
 	   public String web="webEM";
 	   public String groupName;	   
 	   private Disc discDB=new Disc();
 	   private Disc discWeb=new Disc();
 	   private boolean found;
 	   private ArrayList<Disc> discListWeb = new ArrayList<Disc>(),discListDB = new ArrayList<Disc>();
 	   private ArrayList<Disc> finalList = new ArrayList<Disc>();
 	   private ArrayList<Integer> indexes = new ArrayList<Integer>();
 	   private Set<String> groupList= new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
 	   private int already=0;  //number of discs already in db
 	   
 		public Look4NewDiscsThread() {
 			super();
 		}
 
 		@Override
 		public void run() {
 
 			infoFrame.setVisible(true);
 			selectedModel=transformViewSelectedToModel(selectedView);	 
 			discListWeb.clear();
 			finalList.clear();
 			infoText.setText("");
 			
 			indexes.addAll(selectedModel);
 			ProgressBarWindow pw = new ProgressBarWindow();
 	        pw.setFrameSize(pw.dimRelate);
 		    if (pw.startProgBar(indexes.size())<0) {
 			   	Errors.showError(Errors.GENERIC_ERROR);
 			   	return;
 			}
 	     
      
 			
 			for (int i=0;i<indexes.size();i++){
 				groupName=musicTabModel.getDiscAtRow(indexes.get(i)).group;
 				pw.setPer(i+1, "Looking for info of "+groupName);
 				if (!groupList.contains(groupName)){
 					groupList.add(groupName);
 					discListDB.clear();
 					discListDB = musicDataBase.getDiscsOfGroup(groupName);
 					infoFrame.setSize(500, 400);
 					infoFrame.setVisible(true);						
 					infoText.append("Looking for new discs of "+groupName+"\n");
 					discListWeb=webMusicInfoExtractor.getGroupInfo(groupName,web, musicTabModel.getDiscAtRow(indexes.get(i)).loc,musicTabModel.getDiscAtRow(indexes.get(i)).style);
 					if ((discListWeb!=null)&&(discListWeb.size()!=0)){
 						already=0;
 						for (int indWeb=0;indWeb<discListWeb.size();indWeb++){
 							//System.out.println(discList.get(disc).title);
 							found=false;							
 							for (int indDB=0;indDB<discListDB.size();indDB++){
 								discDB=discListDB.get(indDB);
 								discWeb=discListWeb.get(indWeb);
 								if (String.CASE_INSENSITIVE_ORDER.compare(discWeb.title,discDB.title)==0){
 									found=true;
 									already++;
 									break;
 								}									
 							}
 							if (!found){
 								finalList.add(discWeb);
 							}
 						}
 						infoText.append("Found "+discListWeb.size()+" albums in their discography\n");
 						if (already>0) infoText.append(already+" albums already on list\n");
 					}else infoText.append("Group not found or whithout album releases\n");
 					infoText.append("-----------------------------------------\n");
 				}				
 			}
 			newDiscsTabMod.setData(finalList);
 			if (finalList.size()>0) {
 				newDiscsFrame.setSize(700,finalList.size()*18+60);
 				newDiscsFrame.setMaximumSize(new Dimension(700,1000));
 				newDiscsFrame.setVisible(true);
 			} else infoText.append("No new discs found\n");
 			infoText.append("Done!");	
 		}
    	}
 
    //GET FILM DATA/////////////////////////////////////////////////////////////////////////  
    public class GetFilmDataThread extends Thread {
 	   
 	   private String title;
 	   private Movie newmovie=null, currentmovie=null;
 	   
 	   public GetFilmDataThread() {
 			super();
 		}
 
 		@Override
 		public void run() {
 			
 			ProgressBarWindow pw = new ProgressBarWindow();
 	        pw.setFrameSize(pw.dimRelate);
 	        if (pw.startProgBar(selectedModel.size())<0) {
 	        	Errors.showError(Errors.GENERIC_ERROR);
 	        	return;
 	        }
 			
 			for (int i=0;i<selectedModel.size();i++){
 		    	title = moviesTabModel.getMovieAtRow(selectedModel.get(i)).title;  
 		    	newmovie=webMoviesInfoExtractor.getMovieInfo(title);
 		    	pw.setPer(i+1, "Looking for info of "+title);
 				if (newmovie==null) Errors.showError(Errors.MOVIE_NOT_FOUND);
 				else{
 					currentmovie=moviesTabModel.getMovieAtRow(selectedModel.get(i));
 					currentmovie.setDirector(newmovie.getDirector());
 					currentmovie.setYear(newmovie.getYear());
 					moviesDataBase.updateMovie(currentmovie, selectedModel.get(i));
 				}
 	    	}
 			
 		
 		}
    }
    
    //GET GROUP DATA/////////////////////////////////////////////////////////////////////////  
    public class GetGroupDataThread extends Thread {
 	   
 	   private Disc newdisc=null, currentdisc=null;
 	   private ArrayList<Disc> discList;
 	   private LevenshteinDistance dist;
 	   
 	   public GetGroupDataThread() {
 			super();
 		}
 
 		@Override
 		public void run() {
 			
 			ProgressBarWindow pw = new ProgressBarWindow();
 	        pw.setFrameSize(pw.dimRelate);
 	        if (pw.startProgBar(selectedModel.size())<0) {
 	        	Errors.showError(Errors.GENERIC_ERROR);
 	        	return;
 	        }
 	        dist=new LevenshteinDistance();
 	        dist.setThreshold(LevenshteinDistance.MED_THRESHOLD);
 			
 			for (int i=0;i<selectedModel.size();i++){
 				currentdisc = musicTabModel.getDiscAtRow(selectedModel.get(i));  
 		    	discList=webMusicInfoExtractor.getGroupInfo(currentdisc.group,"webEM",currentdisc.loc,currentdisc.style);
 		    	pw.setPer(i+1, "Looking for info of "+currentdisc.group);
 		    	
 		    	if ((discList==null)||(discList.size()==0)) Errors.showError(Errors.GROUP_NOT_FOUND);
 				else{
 					for (int indWeb=0;indWeb<discList.size();indWeb++){
 						//System.out.println(discList.get(disc).title);
 						if (dist.compare(currentdisc.title,discList.get(indWeb).title)){
 							newdisc=discList.get(indWeb);
 							currentdisc.setStyle(newdisc.getStyle());
 							currentdisc.setLoc(newdisc.getLoc());
 							if (newdisc.getType().compareTo("Full-length")==0) currentdisc.setType("LP");
 							else currentdisc.setType(newdisc.getType());
 							musicDataBase.updateDisc(currentdisc, selectedModel.get(i));
 							break;
 						}									
 						
 					}
 				}
 			}
 	
 		}
    }
    
 }
 	
