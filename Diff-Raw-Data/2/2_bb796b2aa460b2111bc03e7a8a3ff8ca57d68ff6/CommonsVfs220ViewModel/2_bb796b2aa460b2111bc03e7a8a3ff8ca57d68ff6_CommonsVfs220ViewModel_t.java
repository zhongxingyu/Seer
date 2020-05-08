 package org.github.simbo1905.zktreemvvm;
 
 import org.apache.commons.vfs2.FileObject;
 import org.apache.commons.vfs2.FileSystemException;
 import org.apache.commons.vfs2.FileSystemManager;
 import org.apache.commons.vfs2.VFS;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zkoss.zul.TreeModel;
 
 public class CommonsVfs220ViewModel {
 	
 	final private static Logger log = LoggerFactory.getLogger(CommonsVfs220ViewModel.class);
 	
 	/*
 	 * could be something like "file:///tmp/" but that would be a security risk
 	 */
 	private static final String FILE_SYSTEM_URI = "jar:http://repo1.maven.org/maven2/org/apache/commons/commons-vfs2/2.0/commons-vfs2-2.0.jar";
 	
 	TreeModel<FileObject> treeModel = null;
 	
 	public TreeModel<FileObject> getTreeModel() {
 		if (treeModel == null) {
 			try {
 				FileSystemManager fsManager = VFS.getManager();
 				FileObject fo = fsManager.resolveFile( FILE_SYSTEM_URI );
 				treeModel = new CachingVfsTreeModel(fo);
 			} catch (FileSystemException e) {
				throw new IllegalArgumentException(String.format("Could not open VFS uri: %s",FILE_SYSTEM_URI),e);
 			}
 			}
 		return treeModel;
 	}
 	
 	private FileObject pickedItem = null;
 
 	public FileObject getPickedItem() {
 		return pickedItem;
 	}
 
 	public void setPickedItem(FileObject pickedItem) {
 		log.info(String.format("setPickedItem: %s", pickedItem));
 		this.pickedItem = pickedItem;
 	}
 }
