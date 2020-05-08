 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client.ui.assetpanel;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.Transparency;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.ImagePanel;
 import net.rptools.lib.swing.SelectionListener;
 import net.rptools.lib.swing.ImagePanel.SelectionMode;
 import net.rptools.lib.swing.preference.SplitPanePreferences;
 import net.rptools.lib.swing.preference.TreePreferences;
 import net.rptools.maptool.client.AppConstants;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.TransferableAsset;
 import net.rptools.maptool.model.Asset;
 import net.rptools.maptool.util.ImageManager;
 
 public class AssetPanel extends JComponent {
 
 	private static final ImageIcon FILTER_IMAGE = new ImageIcon(AssetPanel.class.getClassLoader().getResource("net/rptools/maptool/client/image/zoom.png"));
 	
 	private AssetTree assetTree;
 	private ImagePanel imagePanel;
 	private JTextField filterTextField;
 	private Asset assetBeingTransferred;
     
     private AssetPanelModel assetPanelModel;
 
     private Timer updateFilterTimer;
     
     public AssetPanel(String controlName) {
         this(controlName, new AssetPanelModel());
     }
     public AssetPanel(String controlName, AssetPanelModel model) {
     	this(controlName, model, JSplitPane.VERTICAL_SPLIT);
     }
     public AssetPanel(String controlName, AssetPanelModel model, int splitPaneDirection) {
 		
         assetPanelModel = model;
         model.addImageUpdateObserver(this);
 
         assetTree = new AssetTree(this);
 
         createImagePanel();
         
 		JSplitPane splitPane = new JSplitPane(splitPaneDirection);
         splitPane.setContinuousLayout(true);
         
 		splitPane.setTopComponent(new JScrollPane(assetTree));
 		splitPane.setBottomComponent(createSouthPanel());
 		splitPane.setDividerLocation(100);
 		
         new SplitPanePreferences(AppConstants.APP_NAME, controlName, splitPane);
         new TreePreferences(AppConstants.APP_NAME, controlName, assetTree);
         
 		setLayout(new GridLayout());
 		add(splitPane);
 	}
 
     private void createImagePanel() {
		imagePanel = new ImagePanel();/* {
 			@Override
 			public void dragGestureRecognized(DragGestureEvent dge) {
 				super.dragGestureRecognized(dge);
 
 				MapTool.getFrame().getDragImageGlassPane().setImage(ImageManager.getImageAndWait(assetBeingTransferred));
 			}
 			@Override
 			public void dragMouseMoved(DragSourceDragEvent dsde) {
 				super.dragMouseMoved(dsde);
 
 				Point p = new Point(dsde.getLocation());
 				SwingUtilities.convertPointFromScreen(p, MapTool.getFrame().getDragImageGlassPane());
 
 				MapTool.getFrame().getDragImageGlassPane().setImagePosition(p);
 			}
 			@Override
 			public void dragDropEnd(DragSourceDropEvent dsde) {
 				super.dragDropEnd(dsde);
 
 				MapTool.getFrame().getDragImageGlassPane().setImage(null);
 			}
 			@Override
 			protected Cursor getDragCursor() {
 				return Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(1, 1, Transparency.BITMASK), new Point (0,0), "");
 			}
		};*/
 		
 		imagePanel.setShowCaptions(true);
 		imagePanel.setSelectionMode(SelectionMode.SINGLE);
 		imagePanel.setFont(new Font("Helvetica", 0, 10));
     }
     
     public void setThumbSize(int size) {
     	imagePanel.setGridSize(size);
     }
     
     private JPanel createSouthPanel() {
     	JPanel panel = new JPanel(new BorderLayout());
     	
     	panel.add(BorderLayout.NORTH, createFilterPanel());
     	panel.add(BorderLayout.CENTER, new JScrollPane(imagePanel));
     	
     	return panel;
     }
 
     private JPanel createFilterPanel() {
     	JPanel panel = new JPanel(new BorderLayout());
     	panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
     	
     	panel.add(BorderLayout.CENTER, getFilterTextField());
     	panel.add(BorderLayout.WEST, new JLabel(" ", FILTER_IMAGE, JLabel.LEFT));
     	
     	return panel;
     }
     
     public void addImageSelectionListener(SelectionListener listener) {
     	imagePanel.addSelectionListener(listener);
     }
     
     public void removeImageSelectionListener(SelectionListener listener) {
     	imagePanel.removeSelectionListener(listener);
     }
 
     public List<Object> getSelectedIds() {
         return imagePanel.getSelectedIds();
     }
     
     public void showImagePanelPopup(JPopupMenu menu, int x, int y) {
         
         menu.show(imagePanel, x, y);
     }
     
     public JTextField getFilterTextField() {
     	if (filterTextField == null) {
     		filterTextField = new JTextField();
     		filterTextField.getDocument().addDocumentListener(new DocumentListener(){
     			public void changedUpdate(DocumentEvent e) {
     				// no op
     			}
     			public void insertUpdate(DocumentEvent e) {
     				updateFilter();
     			}
     			public void removeUpdate(DocumentEvent e) {
     				updateFilter();
     			}
     		});
     	}
     	return filterTextField;
     }
 
     private synchronized void updateFilter() {
     	
     	if (updateFilterTimer == null) {
     		updateFilterTimer = new Timer(500, new ActionListener(){
     			public void actionPerformed(ActionEvent e) {
 
     		    	ImageFileImagePanelModel model = (ImageFileImagePanelModel) imagePanel.getModel();
     		    	if (model == null) {
     		    		return;
     		    	}
     		    	
     		    	model.setFilter(filterTextField.getText());
     		    	// TODO: This should be event based
     		    	imagePanel.revalidate();
     		    	imagePanel.repaint();
     		    	
     		    	updateFilterTimer.stop();
     		    	updateFilterTimer = null;
     			}
     		});
     		updateFilterTimer.start();
     	} else {
     		updateFilterTimer.restart();
     	}
     	
     }
     
     // TODO: Find a way around this, it's ugly
     public Asset getAsset(int index) {
         return ((ImageFileImagePanelModel)imagePanel.getModel()).getAsset(index);        
     }
     
     public void addImagePanelMouseListener(MouseListener listener) {
         imagePanel.addMouseListener(listener);
     }
     
     public void removeImagePanelMouseListener(MouseListener listener) {
         imagePanel.removeMouseListener(listener);
     }
     
     public AssetPanelModel getModel() {
         return assetPanelModel;
     }
     
     public boolean isAssetRoot(Directory dir) {
         return ((ImageFileTreeModel) assetTree.getModel()).isRootGroup(dir);
     }
     
     public void removeAssetRoot(Directory dir) {
         assetPanelModel.removeRootGroup(dir);
     }
     
     public Directory getSelectedAssetRoot() {
         return assetTree.getSelectedAssetGroup();
     }
     
 	public void addAssetRoot(Directory dir) {
 		
 		assetPanelModel.addRootGroup(dir);
 	}
 	
 	public void setDirectory(Directory dir) {
 		imagePanel.setModel(new ImageFileImagePanelModel(dir) {
 			@Override
 			public Transferable getTransferable(int index) {
//				TransferableAsset t = (TransferableAsset) super.getTransferable(index);
//				assetBeingTransferred = t.getAsset();
//				return t;
				return super.getTransferable(index);
 			}
 		});
 		updateFilter();
 	}
   
     public AssetTree getAssetTree() {
       return assetTree;
     }
     
     
 }
