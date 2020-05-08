 package tim.prune.gui;
 
 import javax.swing.AbstractListModel;
 
 import tim.prune.data.FileInfo;
 import tim.prune.data.TrackInfo;
 
 public class FileListModel extends AbstractListModel {
 
 	private FileInfo _fileInfo;
 
 	public FileListModel(FileInfo fileInfo){
 		_fileInfo = fileInfo;
 	}
 
 	@Override
 	public Object getElementAt(int inIndex) {
 		return _fileInfo.getSource(inIndex).getName();
 	}
 
 	@Override
 	public int getSize() {
 		return _fileInfo.getNumFiles();
 	}
 
 }
