 import java.io.File;
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JList;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 public class ItemSelectionListener implements ListSelectionListener {
 	private CustomSVGCanvas svgCanvas;
 	private DefaultListModel fileListmodel = null;
 	private ClipListModel clipListModel = null;
 	private JList clipList = null;
 	private boolean enabledPreview = true;
 
 	public ItemSelectionListener(CustomSVGCanvas svgCanvas, DefaultListModel fileListModel, ClipListModel clipListModel, JList clipList) {
 		this.svgCanvas = svgCanvas;
 		this.fileListmodel = fileListModel;
 		this.clipListModel = clipListModel;
 		this.clipList = clipList;
 	}
 	
 	public void setEnabledPreview(boolean b) {
 		this.enabledPreview = b;
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 		if (e.getValueIsAdjusting()) return;
 		int index = ((JList)e.getSource()).getSelectedIndex();
 		
 		updateItem(index);
 	}
 
 	public void updateItem(int index) {
 		updateClipListModel(index);
 
 		if (index < 0) {
 			clipList.clearSelection();
 		} else {
 			ListItem listItem = (ListItem)fileListmodel.get(index);
 			int clipIndex = listItem.getSelectedClipIndex();
 			clipListModel.setUpdating(true);
 			clipList.setSelectedIndex(clipIndex);
 			clipListModel.setUpdating(false);
 		}
 
 		updatePreviewImage(index);
 	}
 	
 	private void updateClipListModel(int index) {
 		if (index < 0) {
 			clipListModel.clear();
 			return;
 		}
 		
 		ListItem listItem = (ListItem)fileListmodel.get(index);
 		int selectedClipIndex = listItem.getSelectedClipIndex();
 		clipListModel.setUpdating(true);
 		clipListModel.clear();
 
 		ArrayList<ClipListItem> list = listItem.getClipList();
 		for (ClipListItem item : list) {
 			clipListModel.addElement(item);
 		}
 		clipListModel.setUpdating(false);
 
 		listItem.setSelectedClipIndex(selectedClipIndex);
 	}
 	
 	private void updatePreviewImage(int index) {
 		if (index < 0) {
 			svgCanvas.setURI(null);
 			return;
 		}
 		ListItem listItem = (ListItem)fileListmodel.get(index);
 		IFile item = listItem;
 		String filename = item.getFilename();
 		if (enabledPreview && PathUtil.isRasterFile(filename) && listItem.isSelected()) {
 			File file = null;
 			if (listItem.getSvgFile() != null) {
 				file = listItem.getSvgFile();
 				item = new FileItem(file, listItem.getClipRect());
 			} else {
 				File svgFile = Epub.convertToSvgFromImage(item);
 				if (svgFile != null) {
 					listItem.setSvgFile(svgFile);
 					item = new FileItem(svgFile, listItem.getClipRect());
 				}
 			}
 		}
 
 		svgCanvas.setListItem(listItem);
 		svgCanvas.setPreview(enabledPreview);
		if (PathUtil.isImageFile(item.getFilename())) {
 			svgCanvas.setImage(item);
 		} else {
 			svgCanvas.setSvg(item);
 		}
 	}
 }
