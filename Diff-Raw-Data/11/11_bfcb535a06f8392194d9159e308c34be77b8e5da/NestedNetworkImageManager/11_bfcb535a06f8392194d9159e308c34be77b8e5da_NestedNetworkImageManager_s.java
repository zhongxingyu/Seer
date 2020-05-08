 package cytoscape.util;
 
 import giny.model.GraphPerspective;
 
 import java.awt.Graphics;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.HeadlessException;
 import java.awt.Image;
 import java.awt.TexturePaint;
 import java.awt.Transparency;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.PixelGrabber;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.render.stateful.NodeDetails;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CytoscapeDesktop;
 import ding.view.DGraphView;
 import ding.view.DNodeView;
 
 
 public class NestedNetworkImageManager implements PropertyChangeListener {
 	private final Image DEF_IMAGE;
 	
 	private static final int DEF_WIDTH = 100;
 	private static final int DEF_HEIGHT = 100;
 	
 	private static NestedNetworkImageManager networkImageGenerator;
 	
 	private final Map<CyNetwork, ImageAndReferenceCount> networkToImageMap;
 	
 	static {
 		try {
 			networkImageGenerator = new NestedNetworkImageManager();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	
 	public static NestedNetworkImageManager getNetworkImageGenerator() {
 		return networkImageGenerator;
 	}
 
 
 	private NestedNetworkImageManager() throws IOException {
 		DEF_IMAGE = ImageIO.read(Cytoscape.class.getResource("/cytoscape/images/default_network.png"));
 		networkToImageMap = new HashMap<CyNetwork, ImageAndReferenceCount>();
 		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(this);
 		
 	}
 	
 	
 	public Image getImage(final CyNetwork network) {
 		
 		System.out.println("!!!!!! Image map size = " + this.networkToImageMap.size() + 
 				", network = " + network + ", val = " + networkToImageMap.get(network));
		
 		
 		if (networkToImageMap.get(network) == null) {
 			return null;
 		} else {
 			return networkToImageMap.get(network).getImage();
 		}
 	}
 	
 	
 	public int getImageCount() {
 		return this.networkToImageMap.size();
 	}
 
 
 	public void propertyChange(final PropertyChangeEvent evt) {		
 		if (evt.getPropertyName().equals(Cytoscape.NESTED_NETWORK_CREATED)) {
 			final CyNetwork network = (CyNetwork) evt.getNewValue();
 			if (this.networkToImageMap.containsKey(network)) {
 				this.networkToImageMap.get(network).incRefCount();
 				return;
 			}
 			updateImage(network, Cytoscape.getNetworkView(network.getIdentifier()));
 		} else if (evt.getPropertyName().equals(Cytoscape.NESTED_NETWORK_DESTROYED)) {
 			final CyNetwork network = (CyNetwork) evt.getNewValue();
 			final ImageAndReferenceCount imageAndRefCount = networkToImageMap.get(network);
 			imageAndRefCount.decRefCount();
 			if (imageAndRefCount.getRefCount() == 0) {
 				this.networkToImageMap.remove(network);
 			}
 		} else if (CytoscapeDesktop.NETWORK_VIEW_CREATED.equals(evt.getPropertyName())) {
 			//TODO: Need to sync. image update timing.
 			final CyNetworkView view = (CyNetworkView)evt.getNewValue();
 			final CyNetwork viewNetwork = view.getNetwork();
 			if (networkToImageMap.containsKey(viewNetwork)) {
 				updateImage(viewNetwork, view);
 				refreshViews(viewNetwork);
 			}
 			
 			boolean updateView = false;
 			for (final CyNode node: (List<CyNode>)viewNetwork.nodesList()) {
 				final CyNetwork nestedNetwork = (CyNetwork) node.getNestedNetwork();
 				if (nestedNetwork != null) {
 					updateView = true;
					addCustomGraphics(viewNetwork, view, node);
 				}
 			}
 			if (updateView) {
 				view.updateView();
 			}
 			
 			System.out.println("**** updating network: " + viewNetwork.getTitle());			
			refreshViews(viewNetwork);
 		}
 	}
 	
 	
 	private void refreshViews(final CyNetwork nestedNetwork) {
 		final List<CyNode> nodes = Cytoscape.getRootGraph().nodesList();
 		final Set<CyNetworkView> updateViews = new HashSet<CyNetworkView>();
 		for (final CyNode node: nodes) {
 			if (node.getNestedNetwork() == nestedNetwork) {
 				for (final CyNetworkView view: Cytoscape.getNetworkViewMap().values()) {
 					if (view.getNodeView(node) != null) {
 						updateViews.add(view);
 					}
 				}
 			}
 		}
 		
 		for (final CyNetworkView view: updateViews) {
 			view.updateView();
 		}
 	}
 
 	
 	private void updateImage(CyNetwork network, CyNetworkView view) {
 		if (view == Cytoscape.getNullNetworkView()) {
 			// View does not exist
 			System.out.println("*************View does not exist for: " + network.getTitle() +" == " + view.getNetwork().getTitle());
 			networkToImageMap.put(network, new ImageAndReferenceCount(DEF_IMAGE));
 		} else {
 			// Create image from this view.
 			final DGraphView dView = (DGraphView) view;
 			final Image image = dView.createImage(DEF_WIDTH, DEF_HEIGHT, 1.0);
 			System.out.println("*************View FOUND for: " + network.getTitle() +" == " + view.getNetwork().getTitle() + image);
 			networkToImageMap.put(network, new ImageAndReferenceCount(image));
 		}
 	}
 
 	
 	private void addCustomGraphics(final CyNetwork network, final CyNetworkView dView, final CyNode parentNode) {
 		System.out.println("*** Adding custom graphics: Count = " + dView.getNodeViewCount() + " node = " + parentNode.getIdentifier());
 		Image networkImage = getImage(network);
 		final DNodeView nodeView = (DNodeView)dView.getNodeView(parentNode);
 		final Rectangle2D rect = new Rectangle2D.Double(-50.0, -50.0, 50.0, 50.0);
 		
 		System.out.println("*** ADD CUSTOM: " + nodeView + ", IMG = " + networkImage +", RECT = " + rect);
 		if (nodeView != null) {
 			if (networkImage == null) {
 				networkImage = DEF_IMAGE;
 			}
 			nodeView.addCustomGraphic(rect, new TexturePaint((BufferedImage) networkImage, rect), NodeDetails.ANCHOR_CENTER);
 		}
 	}
 	
 	
 	private static class ImageAndReferenceCount {
 		private Image image;
 		private int refCount;
 		
 		
 		public ImageAndReferenceCount(final Image image) {
 			this.image = image;
 			this.refCount = 1;
 		}
 		
 		
 		public void incRefCount() {
 			this.refCount++;
 		}
 		
 		
 		public void decRefCount() {
 			this.refCount--;
 		}
 		
 		
 		public int getRefCount() {
 			return refCount;
 		}
 		
 		
 		public Image getImage() {
 			return image;
 		}
 	}
 	
 
 }
