 package org.treblefrei.kedr.gui.qt;
 
 import com.trolltech.qt.QSignalEmitter;
 import com.trolltech.qt.core.QSize;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.gui.*;
 import org.treblefrei.kedr.core.Updatable;
 import org.treblefrei.kedr.gui.WorkspaceItem;
 import org.treblefrei.kedr.model.Album;
 import org.treblefrei.kedr.model.Workspace;
 
 import java.util.List;
 
 public class QKedrWorkspace extends QDialog implements Updatable  {
  
 	public QSignalEmitter.Signal1<Album> selectedAlbumChanged = new Signal1<Album>();
 	 
 	private QKedrMainWindow mainWindow;
 
 	private WorkspaceItem workspaceItem;
     private QListWidget albumList;
     private QHBoxLayout horizontalLayout; 
     private QVBoxLayout verticalLayout;
     private Workspace workspace;
 
     public QKedrWorkspace(Workspace workspace) {
         this.workspace = workspace;
 
         horizontalLayout = new QHBoxLayout();
         verticalLayout = new QVBoxLayout();
 
         setLayout(verticalLayout);
         verticalLayout.addLayout(horizontalLayout);
 
         albumList = new QListWidget(this);
         albumList.setViewMode(QListView.ViewMode.IconMode);
         albumList.setIconSize(new QSize(176, 160));
         albumList.setMovement(QListView.Movement.Static);
         albumList.setMinimumWidth(200);
         albumList.setSpacing(12);
         albumList.currentItemChanged.connect(this, "changeAlbum(QListWidgetItem , QListWidgetItem)");
 
         horizontalLayout.addWidget(albumList);
         workspace.addUpdatableWidget(this);
     }
 
     private void changeAlbum(QListWidgetItem current, QListWidgetItem previous) {
        if (current != null) {
            Album album = (Album) current.data(Qt.ItemDataRole.DisplayPropertyRole);
            if (album != null)
                selectedAlbumChanged.emit(album);
            }
     }
 
 	/**
 	 * @see org.treblefrei.kedr.core.Updatable#perfomed()
 	 */
 	public boolean perfomed() {
         albumList.clear();
         List<Album> albums = workspace.getAlbums();
 
         for (Album album : albums) {
             QListWidgetItem item = new QListWidgetItem();
             item.setData(Qt.ItemDataRole.DisplayPropertyRole, album);
             item.setText(album.getTitle());
             
             // TODO: Place black square or smth like that
             //item.setIcon(new QIcon("classpath:org/treblefrei/kedr/resources/icons/unknown.svg"));
             // coz Windows cant view svg's :\
             item.setIcon(new QIcon("classpath:org/treblefrei/kedr/resources/icons/unknown.png"));
             albumList.addItem(item);
             
         }
 
 		return true;
 	}
 
 }
  
