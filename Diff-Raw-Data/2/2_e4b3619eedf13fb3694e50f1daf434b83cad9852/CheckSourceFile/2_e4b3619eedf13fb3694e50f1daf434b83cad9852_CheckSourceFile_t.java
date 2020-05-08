 package org.kisst.gft.action;
 
 import org.kisst.gft.filetransfer.FileTransferData;
 import org.kisst.gft.task.Task;
 
 public class CheckSourceFile implements Action {
 	public boolean safeToRetry() { return true; }
 
 	public Object execute(Task task) {
 		FileTransferData ft= (FileTransferData) task.getData();
 		// TODO: remember filesize
 		if (! ft.channel.src.fileExists(ft.channel.srcdir, ft.srcpath))
				throw new RuntimeException("Source file "+ft.channel.srcdir+"/"+ft.srcpath+" does not exist or is not accessible");
 		return null;
 	}
 
 }
