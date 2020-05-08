 package org.jhotdraw.draw.action;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 
import org.jhotdraw.samples.svg.SVGPanels;
 import org.jhotdraw.samples.svg.figures.SVGImage;
 
 import com.lorent.whiteboard.client.Client;
 
 public class DefaultDrawingViewDropTargetListener implements DropTargetListener {
 
 	private static final String[] supports={".jpg", ".bmp", ".jpeg", ".png"};
 	@Override
 	public void dragEnter(DropTargetDragEvent dtde) {
		if(!SVGPanels.getInstance().isWritable()){
			dtde.rejectDrag();
			return;
		}
 		boolean r = dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
 		if(!r){
 			dtde.rejectDrag();
 			return;
 		}
 		
 		Transferable transferable = dtde.getTransferable();
 		try {
 			List<File> data = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
 			if(data.isEmpty())
 				return;
 			File file = data.get(0);
 			for(String n:supports){
 				if(file.getName().toLowerCase().endsWith(n)){
 					dtde.acceptDrag(DnDConstants.ACTION_LINK);
 					return;
 				}
 			}
 			dtde.rejectDrag();
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "操作失败！文件格式不支持或正被使用！");
 		}
 	}
 
 	@Override
 	public void dragOver(DropTargetDragEvent dtde) {
 	}
 
 	@Override
 	public void dropActionChanged(DropTargetDragEvent dtde) {
 	}
 
 	@Override
 	public void dragExit(DropTargetEvent dte) {
 	}
 
 	@Override
 	public void drop(DropTargetDropEvent dtde) {
		if(!SVGPanels.getInstance().isWritable()){
			dtde.dropComplete(false);
			return;
		}
 		boolean r = dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
 		if(!r)
 			return;
 		dtde.acceptDrop(DnDConstants.ACTION_LINK);
 		Transferable transferable = dtde.getTransferable();
 		try {
 			List<File> data = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
 			if(data.isEmpty())
 				return;
 			File f = data.get(0);
 			BufferedImage read = ImageIO.read(f);
 			String name = f.getName();
 			int i = name.lastIndexOf('.');
 			Client.getInstance().broadcast(new SVGImage(read,new String(name.substring(i+1))));
 			dtde.dropComplete(true);
 			return;
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(null, "操作失败！文件格式不支持或正被使用！");
 		}
 		
 		dtde.dropComplete(false);
 	}
 
 }
