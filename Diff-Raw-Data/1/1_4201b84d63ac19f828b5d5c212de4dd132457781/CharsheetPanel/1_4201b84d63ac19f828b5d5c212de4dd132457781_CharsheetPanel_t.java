 package net.rptools.maptool.client.ui.token;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DropTarget;
 import java.awt.dnd.DropTargetDragEvent;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.dnd.DropTargetEvent;
 import java.awt.dnd.DropTargetListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.TransferableHelper;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.util.ImageManager;
 
 public class CharsheetPanel extends JPanel implements DropTargetListener {
 
 	private MD5Key sheetAssetId;
 
 	public CharsheetPanel() {
 		new DropTarget(this, this);
 	}
 	
 	public MD5Key getSheetAssetId() {
 		return sheetAssetId;
 	}
 	
 	public void setSheetAssetId(MD5Key sheetAssetId) {
 		this.sheetAssetId = sheetAssetId;
 		
 		if (sheetAssetId == null) {
			removeAll();
 			JLabel label = new JLabel("<html><body>Drop charsheet image here</body></html>", JLabel.CENTER);
 			label.setForeground(Color.white);
 			add(label);
 		} else {
 			removeAll();
 		}
 		repaint();
 	}
 	
 	@Override
 	protected void paintComponent(Graphics g) {
 		
 		Dimension size = getSize();
 		g.setColor(Color.black);
 		g.fillRect(0, 0, size.width, size.height);
 		
 		if (sheetAssetId == null) {
 			return;
 		}
 		
 		BufferedImage image = ImageManager.getImage(AssetManager.getAsset(sheetAssetId), this);
 		
 		Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
 		SwingUtil.constrainTo(imgSize, size.width-8, size.height-8);
 		
 		g.drawImage(image, (size.width - imgSize.width)/2, (size.height - imgSize.height)/2, imgSize.width, imgSize.height, this);
 	}
 	
 	////
 	// DROP TARGET LISTENER
 	public void dragEnter(DropTargetDragEvent dtde) {
 	}
 	public void dragExit(DropTargetEvent dte) {
 	}
 	public void dragOver(DropTargetDragEvent dtde) {
 	}
 	public void drop(DropTargetDropEvent dtde) {
 		
         Transferable t = dtde.getTransferable();
         if (!(TransferableHelper.isSupportedAssetFlavor(t)
                 || TransferableHelper.isSupportedTokenFlavor(t))
                 || (dtde.getDropAction () & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
             dtde.rejectDrop(); // Not a supported flavor or not a copy/move
             System.out.println(" Couldn't figure out the drop");
             return;
         }
         dtde.acceptDrop(dtde.getDropAction());
         
         List<Asset> assets = TransferableHelper.getAsset(dtde);
 
         if (assets == null || assets.size() == 0) {
         	return;
         }
         
         setSheetAssetId(assets.get(0).getId());
 	}
 	public void dropActionChanged(DropTargetDragEvent dtde) {
 	}
 	
 }
