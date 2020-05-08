 package ch.cern.atlas.apvs.client.places;
 
 import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasTreeItems;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.Tree;
 import com.google.gwt.user.client.ui.TreeItem;
 import com.google.gwt.user.client.ui.Widget;
 
 
 public class Procedures extends MenuPlace {
 
 	private static final long serialVersionUID = 1985380912819050341L;
 	
 	public int getIndex() {
 		return 1;
 	}
 
 	@Override
 	public Widget getHeader() {
 		return new HTML("Procedures");
 	}
 
 	@Override
 	public Widget getWidget() {
 		Tree tree = new Tree();
 
 		TreeItem tile = new TreeItem("Tile Calo Drawer Extraction");
 		tree.addItem(tile);
 
 		tile.addItem("Step 1");
 		tile.addItem("Step 2");
 		tile.addItem("Step 3");
 		tile.addItem("Step 4");
 		tile.addItem("Step 5");
 		tile.addItem("Step 6");
 		tile.addItem("Step 7");
 		tile.addItem("Step 8");
 
 		TreeItem ibl = new TreeItem("IBL Installation");
 		tree.addItem(ibl);
 
 		ibl.addItem("Step A");
 		ibl.addItem("Step B");
 		ibl.addItem("Step C");
 		ibl.addItem("Step D");
 
 		ScrollPanel panel = new ScrollPanel();
 		panel.add(tree);
 		return panel;
 	}
 
 }
