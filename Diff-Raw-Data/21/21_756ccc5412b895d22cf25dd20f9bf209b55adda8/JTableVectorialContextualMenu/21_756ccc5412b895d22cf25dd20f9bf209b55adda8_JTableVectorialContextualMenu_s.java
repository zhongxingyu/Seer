 package es.icarto.gvsig.navtableforms.gui.tables;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JTable;
 
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
import es.icarto.gvisg.navtableforms.BasicAbstractForm;
 import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
 
 public class JTableVectorialContextualMenu implements MouseListener {
 
     private String layerName;
     private JTable table;
     private TableFormFactory factory;
 
     public JTableVectorialContextualMenu(String layerName,
 	    TableFormFactory factory) {
 	this.layerName = layerName;
 	this.factory = factory;
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
 	table = (JTable) e.getComponent();
 	if (doubleOrRightClickOnARow(e)) {
 	    TOCLayerManager toc = new TOCLayerManager();
 	    FLyrVect layer = toc.getLayerByName(layerName);
 	    BasicAbstractForm form = factory.createForm(layer);
 	    form.init();
 	    form.setPosition(table.convertRowIndexToModel(table
 		    .getSelectedRow()));
 	    PluginServices.getMDIManager().addWindow(form);
 	}
     }
 
     private boolean doubleOrRightClickOnARow(MouseEvent e) {
 	return (table.getSelectedRow() > -1)
 		&& ((e.getClickCount() == 2) || (e.getButton() == MouseEvent.BUTTON3));
 
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
 }
