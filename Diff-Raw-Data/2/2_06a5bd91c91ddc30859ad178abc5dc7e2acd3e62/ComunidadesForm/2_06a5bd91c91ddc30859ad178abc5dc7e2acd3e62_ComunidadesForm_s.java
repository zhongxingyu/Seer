 package es.udc.cartolab.gvsig.fonsagua.forms;
 
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
 import es.icarto.gvsig.navtableforms.gui.tables.AlphanumericTableLoader;
 import es.icarto.gvsig.navtableforms.gui.tables.TableHandler;
 import es.icarto.gvsig.navtableforms.utils.TOCTableManager;
 import es.udc.cartolab.gvsig.fonsagua.croquis.ui.CroquisButtons;
 import es.udc.cartolab.gvsig.navtable.listeners.PositionEvent;
 
 @SuppressWarnings("serial")
 public class ComunidadesForm extends BasicAbstractForm {
 
     public static final String NAME = "comunidades";
     private TableHandler adescosHandler;
 
     public ComunidadesForm(FLyrVect layer) {
 	super(layer);
 	viewInfo.setTitle("Comunidades");
 	try {
 	    TOCTableManager toc = new TOCTableManager();
 	    if (toc.getTableByName(AdescosForm.NAME) == null) {
 		AlphanumericTableLoader.loadTable(AdescosForm.NAME);
 	    }
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
 	adescosHandler = new TableHandler(AdescosForm.NAME,
 		getWidgetComponents(), "cod_comunidad", AdescosForm.colNames,
 		AdescosForm.colAlias);
     }
 
     @Override
     protected void fillSpecificValues() {
 	JPanel actionsToolBar = this.getActionsToolBar();
 	String comunidadId = ((JTextField) getFormBody().getComponentByName(
 		"cod_comunidad")).getText();
 	actionsToolBar.add(new CroquisButtons(comunidadId)
 		.getAddCroquisButton());
 	actionsToolBar.add(new CroquisButtons(comunidadId)
 		.getShowCroquisButton());
 	adescosHandler
 		.fillValues(getFormController().getValue("cod_comunidad"));
     }
 
     @Override
     protected void setListeners() {
 	super.setListeners();
	adescosHandler.reload(new EntrevistadosForm());
     }
 
     @Override
     protected void removeListeners() {
 	super.removeListeners();
 	adescosHandler.removeListeners();
     }
 
     @Override
     public void onPositionChange(PositionEvent e) {
 	super.onPositionChange(e);
 	adescosHandler
 		.fillValues(getFormController().getValue("cod_comunidad"));
 	this.repaint(); // will force embedded tables to refresh
     }
 
     @Override
     protected String getBasicName() {
 	return NAME;
     }
 
 }
