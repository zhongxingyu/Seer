 package org.treblefrei.kedr.gui.qt;
 
 import com.trolltech.qt.core.QModelIndex;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.gui.*;
 import org.treblefrei.kedr.core.Updatable;
 import org.treblefrei.kedr.model.Album;
 import org.treblefrei.kedr.model.Track;
 
 public class QKedrAlbumWindow extends QWidget implements Updatable {
     private class QTrackListModel extends QAbstractTableModel {
         private Album album;
 
         public void setAlbum(Album album) {
             if (this.album != null) {
                 beginRemoveRows(null, 0, this.album.getTracks().size()-1);
                 this.album = null;
                 endRemoveRows();
                 }
             
             beginInsertRows(null, 0, album.getTracks().size()-1);
             this.album = album;
             endInsertRows();
            reset();
         }
         @Override
         public Object data(QModelIndex index, int role) {
             if (index == null || album == null)
                 return null;
 
             if (index.row() > album.getTracks().size() || index.row() < 0)
                 return null;
 
             if (role == Qt.ItemDataRole.DisplayRole) {
                 Track track = album.getTracks().get(index.row());
 
                 switch(index.column()) {
                     case 0:
                         return String.valueOf(track.getTrackNumber());
                     case 1:
                         return track.getArtist();
                     case 2:
                         return track.getAlbum();
                     case 3:
                         return track.getTitle();
                     case 4:
                         return track.getGenre();
                     case 5:
                         return track.getYear();
                     case 6:
                         return String.format("%02d:%02d", (int)(track.getDuration() / 60 / 1000),
                             (int)(track.getDuration() / 1000)%60);
 
                     default:
                         return null;
                 }
             }
 
             return null;
         }
         @Override
         public Object headerData(int section, Qt.Orientation orientation, int role) {
 
             if (role != Qt.ItemDataRole.DisplayRole)
                 return null;
 
             if (orientation == Qt.Orientation.Horizontal) {
 
                 switch(section) {
                     case 0:
                         return tr("Track No");
                     case 1:
                         return tr("Artist");
                     case 2:
                         return tr("Album");
                     case 3:
                         return tr("Title");
                     case 4:
                         return tr("Genre");
                     case 5:
                         return tr("Year");
                     case 6:
                         return tr("Duration");
 
                     default:
                         return null;
                 }
             }
 
             return null;
         }
         @Override
         public int columnCount(QModelIndex qModelIndex) {
             return 7;
         }
         @Override
         public int rowCount(QModelIndex qModelIndex) {
             if (album == null)
                 return 0;
             return  album.getTracks().size();
         }
     }
 
 	private QAbstractButton queryButton = new QPushButton(tr("Fetch tags"));
 	private QAbstractButton saveTagsButton = new QPushButton(tr("Save tags"));
 
 
     private QTableView trackList;
     private QTrackListModel trackListModel;
 
 	private QKedrMainWindow qKedrMainWindow;
 	 
 	private QAbstractButton qAbstractButton;
     private Album selectedAlbum;
     private QHBoxLayout trackListLayout = new QHBoxLayout();
     private QVBoxLayout verticalLayout = new QVBoxLayout();
     private QHBoxLayout buttonLayout = new QHBoxLayout();
 
     public Signal1<Album> fetchAlbum = new Signal1<Album>();
     public Signal1<Album> saveTags = new Signal1<Album>();
 
     public QKedrAlbumWindow() {
         trackList = new QTableView(this);
         trackListModel = new QTrackListModel();
 
         trackListLayout.addWidget(trackList);
         buttonLayout.addWidget(queryButton);                      
         buttonLayout.addWidget(saveTagsButton);
 
         verticalLayout.addLayout(buttonLayout);
         verticalLayout.addLayout(trackListLayout);
 
         setLayout(verticalLayout);
 
         trackList.setModel(trackListModel);
         trackList.horizontalHeader().show();
         trackList.resizeColumnsToContents();
 
         queryButton.pressed.connect(this, "fetchAlbumInfo()");
         saveTagsButton.pressed.connect(this, "saveAlbumTags()");
     }
 
     public void setAlbum(Album album) {
         if (album != null)
             album.removeUpdatable(this);
 
         selectedAlbum = album;
         trackListModel.setAlbum(album);
         trackList.resizeColumnsToContents();
 	    album.addUpdatable(this);
 	}
 	 
 	private void fetchAlbumInfo() {
         if (null != selectedAlbum) {
             fetchAlbum.emit(selectedAlbum);
         }
 	}
 
     private void saveAlbumTags() {
         if (selectedAlbum != null) {
             saveTags.emit(selectedAlbum);
         }
     }
 
     //private void update
 	 
 	/**
 	 * @see org.treblefrei.kedr.core.Updatable#perfomed()
 	 */
 	public boolean perfomed() {
         System.err.println("performed()");
         trackListModel.setAlbum(selectedAlbum);
 		return true;
 	}
 
 }
  
