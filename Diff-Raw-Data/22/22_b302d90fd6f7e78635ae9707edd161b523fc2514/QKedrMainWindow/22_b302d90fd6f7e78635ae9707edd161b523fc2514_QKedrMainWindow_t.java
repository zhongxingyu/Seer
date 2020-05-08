 package org.treblefrei.kedr.gui.qt;
 
 import com.trolltech.qt.QSignalEmitter;
 import com.trolltech.qt.core.QDir;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.gui.*;
 import org.treblefrei.kedr.database.AlbumInfoFetcher;
 import org.treblefrei.kedr.filesystem.AlbumLoader;
 import org.treblefrei.kedr.filesystem.AlbumSaver;
 import org.treblefrei.kedr.model.Album;
 import org.treblefrei.kedr.model.Workspace;
 
 import javax.swing.filechooser.FileSystemView;
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
public class    QKedrMainWindow extends QMainWindow {
 
 	private QKedrWorkspace workspaceWidget;
 	private QKedrAlbumWindow albumWindow;
 	private QKedrAboutDialog aboutDialog;
 	private QKedrSettingsDialog settingsDialog;
 
     private QAction aboutAction;
     private QAction exitAction;
     private QAction addNewAlbumAction;
 
     private QDir directory;
     private QDockWidget dockWidget;
     private Workspace workspace;
 
     public void setSelectedAlbum(Album album) {
         albumWindow.setAlbum(album);
 	}
 
 	public void offerNewAlbumAdding() {
         String path = QFileDialog.getExistingDirectory(this, tr("Directory"), directory.path());
 
         if (!path.isEmpty()) {
             directory.setPath(path);
             Album album = AlbumLoader.getAlbum(path);
             if (album != null) {
                 workspace.addAlbum(album);
                 albumWindow.setAlbum(album);
                 }
         }
 	}
 
     private static class InfoFetcherRunnable extends QSignalEmitter implements Runnable {
         private Album album;
         private Album filledAlbum;
 
         public Signal2<Album, Album> albumFilled = new Signal2<Album, Album>();
 
         InfoFetcherRunnable(Album album) {
             this.album = album;
             this.filledAlbum = null;
         }
 
         public void run() {
             try {
                 filledAlbum = AlbumInfoFetcher.fetchAlbumInfo(album);
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 //album.sync(filledAlbum);
                 albumFilled.emit(album, filledAlbum);
                 albumFilled.disconnect();
             }
         }
 
     }
 
     public void syncAlbum(Album album, Album filledAlbum) {
         // This MUST be done in the same thread where all the QObjects live :( :(
         System.out.println("are we here?");
         album.sync(filledAlbum);
     }
 
 	public void fetchAlbumInfo(Album album) {
         if (null != album) {
             ExecutorService executor = Executors.newCachedThreadPool();
             InfoFetcherRunnable fetcher = new InfoFetcherRunnable(album);
             fetcher.albumFilled.connect(this, "syncAlbum(Album, Album)");
             executor.execute(fetcher);
             executor.shutdown();
         }
 	}
 
     public void saveTags(Album album) {
         AlbumSaver.saveAlbum(album);
     }
 
     public QKedrMainWindow() {
         setMenuBar(new QMenuBar());
         setMinimumSize(800, 600);
         
         initWorkspace();
 
         createActions();
         createMenu();
         createWorkspaceWidget();
         createAlbumWindow();
 
         aboutDialog = null;
 
         // TODO: Make something with this crap in future versions
         String defaultPath = FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath();
         directory = new QDir(defaultPath);
         this.setWindowTitle("KEDR");
         setWindowIcon(new QIcon("classpath:org/treblefrei/kedr/resources/icons/icon.png"));
     }
 
     private boolean initWorkspace() {
         workspace = new Workspace();
 
         return true;
     }
     private boolean createAlbumWindow() {
         albumWindow = new QKedrAlbumWindow();
         setCentralWidget(albumWindow);
 
         albumWindow.saveTags.connect(this, "saveTags(Album)");
         albumWindow.fetchAlbum.connect(this, "fetchAlbumInfo(Album)");
 
         return true;
     }
     private boolean createWorkspaceWidget() {
         dockWidget = new QDockWidget(tr("Workspace"), this);
         dockWidget.setAllowedAreas(new Qt.DockWidgetAreas(
                 Qt.DockWidgetArea.RightDockWidgetArea.value() |
                 Qt.DockWidgetArea.LeftDockWidgetArea.value()
         ));
 
         workspaceWidget = new QKedrWorkspace(workspace);
         workspaceWidget.selectedAlbumChanged.connect(this, "setSelectedAlbum(Album)");
         dockWidget.setWidget(workspaceWidget);
         dockWidget.setMinimumWidth(200);
         QDockWidget.DockWidgetFeatures dockFeatures = new QDockWidget.DockWidgetFeatures(
                 QDockWidget.DockWidgetFeature.DockWidgetFloatable.value() |
                 QDockWidget.DockWidgetFeature.DockWidgetMovable.value()
         );
         dockWidget.setFeatures(dockFeatures);
 
         addDockWidget(Qt.DockWidgetArea.LeftDockWidgetArea, dockWidget);
 
         return true;
     }
     private boolean createActions() {
         aboutAction = new QAction(null, tr("&About"), this);
         aboutAction.setStatusTip(tr("Show KEDR's about dialog"));
         aboutAction.triggered.connect(this, "showAboutDialog()");
 
         addNewAlbumAction = new QAction(null, tr("Add &New album directory"), this);
         addNewAlbumAction.setShortcut(new QKeySequence(tr("Ctrl+N")));
         addNewAlbumAction.setStatusTip(tr("Add new album directory to workspace"));
         addNewAlbumAction.triggered.connect(this, "offerNewAlbumAdding()");
 
         exitAction = new QAction(null, tr("&Exit"), this);
         exitAction.setShortcut(new QKeySequence(tr("Ctrl+Q")));
         exitAction.setStatusTip(tr("Exit the application"));
 
         return true;
     }
     private boolean createMenu() {
         QMenu fileMenu = menuBar().addMenu(tr("&File"));
         QMenu helpMenu = menuBar().addMenu(tr("&Help"));
 
         fileMenu.addAction(addNewAlbumAction);
         fileMenu.addSeparator();
         fileMenu.addAction(exitAction);
         helpMenu.addAction(aboutAction);
 
         return true;
     }
 
     private boolean showAboutDialog() {
         if (null == aboutDialog)
             aboutDialog = new QKedrAboutDialog(this);
         aboutDialog.show();
         
         return true;
     }
 
     public void run(String []args) {
         show();
     }
     public static void main(String []args) {
         QApplication.initialize(args);
         Logger.getLogger("org.jaudiotagger.audio").setLevel(Level.OFF); // just stfu
         QKedrMainWindow mainWindow = new QKedrMainWindow();
 
         mainWindow.show();
         QApplication.exec();
     }
 
 }
 
 
