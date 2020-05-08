 package org.treblefrei.kedr.gui.qt;
 
 import com.trolltech.qt.QVariant;
 import com.trolltech.qt.core.QModelIndex;
 import com.trolltech.qt.core.Qt;
 import com.trolltech.qt.gui.QAbstractButton;
 import com.trolltech.qt.gui.QAbstractTableModel;
 import com.trolltech.qt.gui.QTableView;
 import com.trolltech.qt.gui.QWidget;
 import org.treblefrei.kedr.core.Updatable;
 import org.treblefrei.kedr.model.Album;
 import org.treblefrei.kedr.model.Track;
 
 public class QKedrAlbumWindow extends QWidget implements Updatable {
     private class QTrackListModel extends QAbstractTableModel {
         private Album album;
 
         public void setAlbum(Album album) {
             this.album = album;
         }
 
         @Override
         public Object data(QModelIndex index, int role) {
             if (index == null || album == null)
                return new QVariant();
 
             if (index.row() > album.getTracks().size() || index.row() < 0)
                return new QVariant();
 
             if (role == Qt.ItemDataRole.DisplayRole) {
                 Track track = album.getTracks().get(index.row());
 
                 switch(index.column()) {
                     case 0:
                         return track.getArtist();
 
                     case 1:
                         return track.getTitle();
                 }
             }
 
            return new QVariant();
         }
 
         @Override
         public Object headerData(int section, Qt.Orientation orientation, int role) {
             if (role != Qt.ItemDataRole.DisplayRole)
                return new QVariant();

           System.err.println("QTrackListModel.headerData");
 
             if (orientation == Qt.Orientation.Horizontal) {
 
                 switch(section) {
                     case 0:
                         return tr("Artist");
                     case 1:
                         return tr("Title");
 
                     default:
                        return new QVariant();
                 }
             }
 
            return new QVariant();
         }
 
         @Override
         public int columnCount(QModelIndex qModelIndex) {
             return 2;
         }
         @Override
         public int rowCount(QModelIndex qModelIndex) {
             if (album == null)
                 return 0;
             return  album.getTracks().size();
         }
     }
 
 	private QAbstractButton queryButton;
     private QTableView trackList;
     private QTrackListModel trackListModel;
 
 	private QKedrMainWindow qKedrMainWindow;
 	 
 	private QAbstractButton qAbstractButton;
     private Album selectedAlbum;
 
     public QKedrAlbumWindow() {
         trackList = new QTableView(this);
         trackListModel = new QTrackListModel();
 
         trackList.setModel(trackListModel);
         trackList.horizontalHeader().show();
     }
 
     public void setAlbum(Album album) {
         System.err.println("QKedrAlbumWindow.setAlbum("+album+")");
 
         if (album != null)
             album.removeUpdatable(this);
 
         selectedAlbum = album;
         trackListModel.setAlbum(album);
         trackList.update();
 	    album.addUpdatable(this);
 
 
 	}
 	 
 	private void fetchAlbumInfo() {
 	 
 	}
 
     //private void update
 	 
 	/**
 	 * @see org.treblefrei.kedr.core.Updatable#perfomed()
 	 */
 	public boolean perfomed() {
 		return false;
 	}
 
 }
  
