 package com.bluesky.visualprogramming.ui;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 
 import com.bluesky.visualprogramming.core._Object;
 import com.bluesky.visualprogramming.vm.VirtualMachine;
 
 /**
 * used for clip board
  * 
  * @author jack
  * 
  */
 public class FieldSelectionClipboardOwner implements ClipboardOwner {
 	private boolean lostOwnership;
 
 	public void setObject(_Object owner, String fieldName) {
 		StringSelection stringSelection = new StringSelection(owner.getPath()
 				+ "|" + fieldName);
 
 		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		clipboard.setContents(stringSelection, this);
 
 		lostOwnership = false;
 	}
 
 	public FieldSelection getObject() {
 		if (lostOwnership)
 			return null;
 		
 		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 		// odd: the Object param of getContents is not currently used
 		Transferable contents = clipboard.getContents(null);
 		boolean hasTransferableText = (contents != null)
 				&& contents.isDataFlavorSupported(DataFlavor.stringFlavor);
 		if (hasTransferableText) {
 			try {
 				String str = (String) contents
 						.getTransferData(DataFlavor.stringFlavor);
 				String[] ss = str.split("\\|");
 				String path = ss[0];
 				String fieldName = ss[1];
 				
 				VirtualMachine vm = VirtualMachine.getInstance();
 				_Object obj = vm.getObjectRepository().getObjectByPath(path);
 
 				FieldSelection fs = new FieldSelection(obj, fieldName);
 				return fs;
 			} catch (Exception ex) {
 				System.out.println(ex);
 				ex.printStackTrace();
 			}
 
 		}
 		return null;
 	}
 
 	@Override
 	public void lostOwnership(Clipboard arg0, Transferable arg1) {
 		this.lostOwnership = true;
 
 	}
 
 }
