 package cytoscape.actions;
 
 import java.awt.event.ActionEvent;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.event.MenuEvent;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.util.CytoscapeAction;
 import cytoscape.view.CyNetworkView;
 
 public class ApplyVisualStyleAction extends CytoscapeAction {
 
 	private static final long serialVersionUID = 2435881456685787138L;
 	
 	private String styleName;
 	
 	public ApplyVisualStyleAction(String styleName) {
 		super(styleName);
 		this.styleName = styleName;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		final List<CyNetwork> selected = Cytoscape.getSelectedNetworks();
 		final Set<CyNetworkView> views = new HashSet<CyNetworkView>();
 		
 		for (final CyNetwork network: selected) {
 			final CyNetworkView targetView = Cytoscape.getNetworkView(network.getIdentifier());
 			if (targetView != Cytoscape.getNullNetworkView()) {
 				views.add(targetView);
 			}
 		}
 		
 		applyStyle(views);
 	}
 	
 	private void applyStyle(final Set<CyNetworkView> targetViews) {
 		Cytoscape.getVisualMappingManager().setVisualStyle(styleName);
 		for (CyNetworkView view: targetViews) {
 			System.out.println("Apply Visual Style: " + view.getNetwork().getTitle());
 			view.setVisualStyle(styleName);
 			view.redrawGraph(false, true);
 		}
 		
 	}
 	
 	public void menuSelected(MenuEvent e) {
 		this.enableForNetworkAndView();
 	}
 
 }
