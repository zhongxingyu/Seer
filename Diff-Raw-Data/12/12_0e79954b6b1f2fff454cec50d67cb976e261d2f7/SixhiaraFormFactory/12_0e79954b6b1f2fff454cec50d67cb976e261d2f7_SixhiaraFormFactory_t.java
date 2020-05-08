 package es.icarto.gvsig.sixhiara.forms;
 
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 
 import es.icarto.gvsig.navtableforms.AbstractForm;
 import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
 import es.icarto.gvsig.navtableforms.utils.FormFactory;
 
 public class SixhiaraFormFactory extends FormFactory {
 
     private static SixhiaraFormFactory instance = null;
 
     static {
 	instance = new SixhiaraFormFactory();
     }
 
     public static void registerFormFactory() {
 	FormFactory.registerFormFactory(instance);
     }
 
     public static SixhiaraFormFactory getInstance() {
 	return instance;
     }
 
     @Override
     public AbstractForm createForm(FLyrVect layer) {
 	// TODO Auto-generated method stub
 	return null;
     }
 
     @Override
     public AbstractForm createSingletonForm(FLyrVect layer) {
 	// TODO Auto-generated method stub
 	return null;
     }
 
     @Override
     public AbstractForm createForm(String layerName) {
 	// TODO Auto-generated method stub
 	return null;
     }
 
     @Override
     public AbstractForm createSingletonForm(String layerName) {
 	// TODO Auto-generated method stub
 	return null;
     }
 
     @Override
     public AbstractSubForm createSubForm(String tableName) {
 	if (tableName != null) {
 	    if (tableName.equals(FountainsAnalyticalSubForm.TABLENAME)) {
 		return new FountainsAnalyticalSubForm();
	    } else if (tableName
		    .equals(FountainsFlowrateGaugingSubForm.TABLENAME)) {
		return new FountainsFlowrateGaugingSubForm();
 	    }
 	}
 	return null;
     }
 
     @Override
     public boolean hasMainForm(String layerName) {
 	// TODO Auto-generated method stub
 	return false;
     }
 
     @Override
     public boolean allLayersLoaded() {
 	// TODO Auto-generated method stub
 	return false;
     }
 
     @Override
     public boolean checkLayerLoaded(String layerName) {
 	// TODO Auto-generated method stub
 	return false;
     }
 
     @Override
     public boolean checkTableLoaded(String tableName) {
 	// TODO Auto-generated method stub
 	return false;
     }
 
     @Override
     public void loadLayer(String layerName) {
 	// TODO Auto-generated method stub
 
     }
 
     @Override
     public void loadTable(String tableName) {
 	// TODO Auto-generated method stub
 
     }
 
 }
