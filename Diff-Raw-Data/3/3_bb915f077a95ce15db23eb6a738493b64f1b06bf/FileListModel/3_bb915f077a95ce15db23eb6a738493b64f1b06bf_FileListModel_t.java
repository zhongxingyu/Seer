 package cz.zcu.kiv.kc.shell;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Comparator;
 
 import javax.swing.AbstractListModel;
 //import javax.swing.event.ListDataEvent;
 //import javax.swing.event.ListDataListener;
 
 public class FileListModel extends AbstractListModel<File> {
 
 	private static final long serialVersionUID = 1616095720949603826L;
 
 	private String dirPath = "/";
 	
 	private File[] files = new File[0];
 
 	private File parentDir = null;
 	
 	private Comparator<File> filenameComparator = new Comparator<File>() {
 		@Override
 		public int compare(File arg0, File arg1) {
 			int ret;
 			if (arg0.isDirectory() && !arg1.isDirectory()) {
 				ret = -1;
 			} else if (!arg0.isDirectory() && arg1.isDirectory()) {
 				ret = 1;
 			} else if (arg0.isDirectory() && arg1.isDirectory()) {
 				ret = arg0.getName()
 						.compareToIgnoreCase(arg1.getName());
 			} else {
 				ret = arg0.getName()
 						.compareToIgnoreCase(arg1.getName());
 			}
 			return ret;
 		}
 	};
 	
 	@Override
 	public File getElementAt(int arg0) {
 		if (this.parentDir != null && arg0 == 0)
 		{
 			return new FirstFile(this.parentDir.getAbsolutePath());
 		}
		
		return arg0 > files.length ? null: files[arg0 - (this.parentDir != null ? 1 : 0)];
 	}
 
 	@Override
 	public int getSize() {
 		return files.length + (this.parentDir != null ? 1 : 0);
 	}
 
 	public void refresh() {
 		int origFilesCount = this.files.length;
 		File dir = new File(dirPath);
 		this.parentDir = dir.getParentFile();
 		this.files = dir.listFiles();
 		if (this.files == null)
 		{
 			this.files = new File[0];
 		}
 		
 		Arrays.sort(this.files, this.filenameComparator);
 		
 		this.fireIntervalRemoved(this, 0, origFilesCount);
 		this.fireIntervalAdded(this, 0, this.files.length + (this.parentDir != null ? 1 : 0));
 		/*String parentFile = dir.getParent();
 		if (parentFile != null) {
 			files = new File[childFilesLength + 1];
 			files[0] = new FirstFile(parentFile);
 		} else {
 			files = new File[childFilesLength];
 		}
 		if (childFiles != null) {
 			Arrays.sort(childFiles, new Comparator<File>() {
 				@Override
 				public int compare(File arg0, File arg1) {
 					int ret;
 					if (arg0.isDirectory() && !arg1.isDirectory()) {
 						ret = -1;
 					} else if (!arg0.isDirectory() && arg1.isDirectory()) {
 						ret = 1;
 					} else if (arg0.isDirectory() && arg1.isDirectory()) {
 						ret = arg0.getName()
 								.compareToIgnoreCase(arg1.getName());
 					} else {
 						ret = arg0.getName()
 								.compareToIgnoreCase(arg1.getName());
 					}
 					return ret;
 				}
 			});
 			if (parentFile != null) {
 				for (int i = 1; i < childFilesLength + 1; i++) {
 					files[i] = childFiles[i - 1];
 				}
 			} else {
 				for (int i = 0; i < childFilesLength; i++) {
 					files[i] = childFiles[i];
 				}
 			}
 		}
 		ListDataEvent event = new ListDataEvent(this,
 				ListDataEvent.CONTENTS_CHANGED, 0, files.length);
 		for (ListDataListener listener : listenerList
 				.getListeners(ListDataListener.class)) {
 			listener.contentsChanged(event);
 		}
 		ListDataEvent event2 = new ListDataEvent(this,
 				ListDataEvent.INTERVAL_REMOVED, 0, files.length);
 		for (ListDataListener listener : listenerList
 				.getListeners(ListDataListener.class)) {
 			listener.contentsChanged(event2);
 		}*/
 	}
 
 	public void setDirPath(String dirPath) {
 		this.dirPath = dirPath;
 		refresh();
 	}
 }
