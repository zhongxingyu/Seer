 package com.iver.cit.gvsig.project.documents.view.toc.actions;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.ui.mdiManager.IWindow;
 import com.iver.cit.gvsig.StartEditing;
 import com.iver.cit.gvsig.fmap.layers.FLayer;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.project.documents.view.gui.View;
 import com.iver.cit.gvsig.project.documents.view.toc.AbstractTocContextMenuAction;
 import com.iver.cit.gvsig.project.documents.view.toc.ITocItem;
 
 /**
  * Comienza la edicin de la capa seleccionada.
  *
  * @author Vicente Caballero Navarro
  */
 public class StartEditingTocMenuEntry extends AbstractTocContextMenuAction {
 	public String getGroup() {
 		return "edition";
 	}
 
 	public int getGroupOrder() {
 		return 1;
 	}
 
 	public int getOrder() {
 		return 1;
 	}
 
 	public String getText() {
 		return PluginServices.getText(this, "start_edition");
 	}
 
 	public boolean isEnabled(ITocItem item, FLayer[] selectedItems) {
		return true;
 	}
 
 	public boolean isVisible(ITocItem item, FLayer[] selectedItems) {
		IWindow window=PluginServices.getMDIManager().getActiveWindow();
		if (window instanceof View){
			return (isTocItemBranch(item)) && (selectedItems.length == 1 && selectedItems[0].isAvailable() && selectedItems[0] instanceof FLyrVect) && !((FLyrVect)selectedItems[0]).isEditing();
		}
		return false;
 	}
 
 	public void execute(ITocItem item, FLayer[] selectedItems) {
 		StartEditing startEditind=(StartEditing)PluginServices.getExtension(StartEditing.class);
 		startEditind.execute("STARTEDITING");
 		PluginServices.getMainFrame().enableControls();
    }
 }
