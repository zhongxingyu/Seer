 /**
  * SignalPanel.java created 31.05.2012
  */
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.swing.JPanel;
 
 /**
  * The panel containing all signalgraphs and the controls to resize them. Implemented as Singleton.
  * @author Enrico Grunitz
  * @version 0.1 (31.05.2012)
  */
 public class SignalPanel extends JPanel {
 
 	/** serialization ID */						private static final long serialVersionUID = 1L;
 	/** the singleton instance */				private static final SignalPanel myself = new SignalPanel();
 	
 	/** collection of the signalgraphs */		private Collection<SignalView> graphs;
 	/** collection of resize controls */		private Collection controls;
 	
 	/**
 	 * Only used constructor.
 	 */
 	private SignalPanel() {
 		super();
		this.addComponentListener(new SignalPanelComponentAdapter());
 		this.setLayout(new SignalPanelLayoutManager());
 		graphs = new ArrayList<SignalView>(12);
		// DEBUG this button only serves debug purposes
 		Sidebar.getInstance().addDbgButtonAL(new ActionListener() {
 												public void actionPerformed(ActionEvent ae) {
 													SignalView sv = graphs.iterator().next();
 													Dimension d = sv.getPreferredSize();
 													d.height += 20;
 													sv.setPreferredSize(d);
													doLayout();
 												}
 											});
 		return;
 	}
 	
 	/**
 	 * Returns the singleton instance of this class.
 	 * @return instance of SignalPanel
 	 */
 	public static SignalPanel getInstance() {
 		return myself;
 	}
 	
 	/**
 	 * Adds the given ChartPanel to the display.
 	 * @param element the ChartPanel to be added
 	 */
 	public void addSignal(SignalView element) {
 		if(element != null) {
 			graphs.add(element);
 			element.setPreferredSize(new Dimension(this.getWidth(), this.getHeight() / graphs.size()));
 			this.add(element);
 			this.validate();
 			// DEBUG console message for adding signals to signalpanel 
 			System.out.println("Added signal - preffered size: " + element.getPreferredSize());
 		}
 	}
 	
 	/**
 	 * Removes the given ChartPanel from the diplay.
 	 * @param element ChartPanel to be removed.
 	 * @return true if element was removed, false if the given element was not displayed (and so cannot be removed)
 	 */
 	public boolean removeSignal(SignalView element) {
 		if(graphs.contains(element)) {
 			graphs.remove(element);
 			this.remove(element);
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Sets for all graphs the width of preferred sizes to the given width.
 	 * @param width the new preferred size width
 	 */
 	private void adjustGraphWidths(int width) {
 		Iterator<SignalView> it = graphs.iterator();
 		// DEBUG adjustingGraphWidths
 		System.out.println("Adjusting widths to " + width);
 		while(it.hasNext()) {
 			SignalView sv = it.next();
 			Dimension dim = sv.getPreferredSize();
 			dim.width = width;
 			sv.setPreferredSize(dim);
			// DEBUG adjustingGraphWidths too
 			System.out.println(dim);
 		}
 		return;
 	}
 
 	/**
 	 * ComponentAdapter to save new size of panel after resizing.
 	 * @author Enrico Grunitz
 	 * @version 0.2 (01.06.2012)
 	 */
 	private class SignalPanelComponentAdapter extends ComponentAdapter {
 		public void componentResized(ComponentEvent event) {
 			int newHeight = getHeight();
 			int newWidth = getWidth();
			/* FIXME doesn't adjusts graphs after maximizing application
			 * 		 seems like LayoutManager is informed before ComponentAdapter
			 * 		 doLayout() is a workaround
			 */
 			// DEBUG system message for resizing signalpanel 
 			System.out.println("new size: " + newWidth + "x" + newHeight);
 			if(newWidth > 0 && newHeight > 0 && !graphs.isEmpty()) {
 				adjustGraphWidths(newWidth);
				doLayout();
 			}
 		}
 	}
 	
 	/** 
 	 * LayoutManager for the SignalPanel. It arranges the Components in a vertical arrangement. It tries to use preferred sizes.
 	 * If this fails it resizes all elements scaling with the preferred size.
 	 * @version 0.2.1 (01.06.2012)
 	 * @author Enrico Grunitz
 	 */
 	public class SignalPanelLayoutManager implements LayoutManager {
 		/**
 		 * Needed by LayoutManager. Does nothing.
 		 * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
 		 */
 		public void addLayoutComponent(String str, Component c) {
 			return;	// no need for strings for our components
 		}
 
 		/**
 		 * Needed by LayoutManager. Lays out the given Container.
 		 * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
 		 */
 		public void layoutContainer(Container parent) {
 			if(parent != null) {
 				Insets ins = parent.getInsets();
 				Dimension targetDim = removeInsets(parent.getSize(), ins);
 				Dimension prefDim = removeInsets(this.preferredLayoutSize(parent), ins);
 				int curY = ins.top;
 				Rectangle rect = new Rectangle();
 				Component[] comps = parent.getComponents();
 				double yScaleFactor = 1.0;
 				if(prefDim.height > targetDim.height) {
 					yScaleFactor = (double)targetDim.height / (double)prefDim.height; 
 				}
 				for(int i = 0; i < comps.length; i++) {
 					rect.x = ins.left;
 					rect.y = curY;
 					if(comps[i].getPreferredSize().getWidth() <= targetDim.width) {
 						rect.width = comps[i].getPreferredSize().width; 
 					} else {
 						rect.width = targetDim.width;
 					}
 					rect.height = (int)Math.round( (double)comps[i].getPreferredSize().getHeight() * yScaleFactor);
 					comps[i].setBounds(rect);
 					curY += rect.height;
 				}
 			}
 		}
 
 		/**
 		 * Needed by LayoutManager. Returns the minimum size of the given container with this Layout.
 		 * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
 		 */
 		public Dimension minimumLayoutSize(Container parent) {
 			Dimension minDim = new Dimension(0, 0);
 			if(parent != null) {
 				// getting sizes of components
 				Component[] comps = parent.getComponents();
 				for(int i = 0; i < comps.length; i++) {
 					minDim.height += comps[i].getMinimumSize().height;
 					minDim.width = Math.max(minDim.width, comps[i].getMinimumSize().width);
 				}
 				// adding sizes of insets
 				minDim = addInsets(minDim, parent.getInsets());
 			}
 			return minDim;
 		}
 
 		/**
 		 * Needed by LayoutManager. Returns the preferred size of the given container with this Layout.
 		 * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
 		 */
 		public Dimension preferredLayoutSize(Container parent) {
 			Dimension dim = new Dimension(0, 0);
 			if(parent != null) {
 				// getting sizes of components
 				Component[] comps = parent.getComponents();
 				for(int i = 0; i < comps.length; i++) {
 					dim.height += comps[i].getPreferredSize().height;
 					dim.width = Math.max(dim.width, comps[i].getPreferredSize().width);
 				}
 				// adding inset sizes
 				dim = addInsets(dim, parent.getInsets());
 			}
 			return dim;
 		}
 
 		/**
 		 * Needed by LayoutManager. Does nothing.
 		 * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
 		 */
 		public void removeLayoutComponent(Component arg0) {
 			return;	// nothing to do here - yay holidays
 		}
 		
 		/**
 		 * Adds the insets to the dimension.
 		 * @param dim Dimension to add to
 		 * @param ins Insets to be added
 		 * @return the new calculated Dimension
 		 */
 		private Dimension addInsets(Dimension dim, Insets ins) {
 			return new Dimension(dim.width + ins.left + ins.right, dim.height + ins.top + ins.bottom);
 		}
 		
 		/**
 		 * Subtracts the insets from the dimension.
 		 * @param dim the Dimension
 		 * @param ins Insets to be subtracted
 		 * @return the new calculated Dimension
 		 */
 		private Dimension removeInsets(Dimension dim, Insets ins) {
 			return new Dimension(dim.width - ins.left - ins.right, dim.height - ins.top - ins.bottom);
 		}
 	}
 }
