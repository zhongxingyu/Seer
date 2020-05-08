 package es.icarto.gvsig.catastro.evaluator.actions;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.values.Value;
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.fmap.core.IFeature;
 import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
 import com.iver.cit.gvsig.fmap.layers.FLayer;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
 
 import es.icarto.gvsig.catastro.constants.ConstantManager;
 import es.icarto.gvsig.catastro.utils.Preferences;
 import es.icarto.gvsig.catastro.utils.ToggleEditing;
 
 public class UpdatePredioIDInConstrucciones implements IAction {
 
     private IFeature predio = null;
     private FLyrVect construccionesLayer = null;
 
     public UpdatePredioIDInConstrucciones(FLayer construccionesLayer,
 	    IFeature predioFusioned) {
 	this.predio = predioFusioned;
 	this.construccionesLayer = (FLyrVect) construccionesLayer;
     }
 
     @Override
     public boolean execute() {
 	IFeatureIterator iterator = getFeatureIterator();
 	Value newPredioID = getNewPredioID();
 	try {
 	    int construccionIndex = construccionesLayer.getRecordset()
 		    .getFieldIndexByName(Preferences.PREDIO_NAME_IN_DB);
 	    ToggleEditing te = new ToggleEditing();
 	    te.startEditing(construccionesLayer);
 	    while (iterator.hasNext()) {
 		IFeature construccion = iterator.next();
		int indexOfFeature = getIndexOfFeatureInRecordset(construccion);
		SelectableDataSource sds = construccionesLayer.getRecordset();
		int col = sds.getFieldIndexByName(Preferences.GID_IN_DB);
 		if (construccion.getGeometry().toJTSGeometry()
 			.coveredBy(predio.getGeometry().toJTSGeometry())) {
 		    Value[] atts = construccion.getAttributes().clone();
 		    atts[construccionIndex] = newPredioID;
 		    construccion.setAttributes(atts);
		    construccion.setID(construccionesLayer.getRecordset()
			    .getFieldValue(indexOfFeature, col).toString());
		    te.modifyFeature(indexOfFeature + 1, construccion, "_none");
 		}
 	    }
 	    return true;
 	} catch (ReadDriverException e) {
 	    e.printStackTrace();
 	    return false;
 	}
     }
 
    private int getIndexOfFeatureInRecordset(IFeature feature) {
	try {
	    SelectableDataSource sds = construccionesLayer.getRecordset();
	    int col = sds.getFieldIndexByName(Preferences.GID_IN_DB);
	    for (int row = 0; row < sds.getRowCount(); row++) {
		if (sds.getFieldValue(row, col).toString()
			.equalsIgnoreCase(feature.getAttribute(col).toString())) {
		    return row;
		}
	    }
	    return -1;
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return -1;
	}
    }

     private Value getNewPredioID() {
 	Value[] values = predio.getAttributes();
 	return values[6]; // TODO: get name from layer
     }
 
     private IFeatureIterator getFeatureIterator() {
 	try {
 	    ConstantManager constantManager = new ConstantManager();
 	    SelectableDataSource sds = construccionesLayer.getRecordset();
 	    String sqlQuery = "select * from '" + sds.getName() + "'"
 		    + " where " + Preferences.PAIS_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getPais() + " " + " and "
 		    + Preferences.ESTADO_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getEstado() + " "
 		    + " and " + Preferences.MUNICIPIO_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getMunicipio() + " "
 		    + " and " + Preferences.LIMITE_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getLimiteMunicipal() + " "
 		    + " and " + Preferences.REGION_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getRegion() + " "
 		    + " and " + Preferences.MANZANA_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getManzana() + ";";
 	    IFeatureIterator it = construccionesLayer.getSource()
 		    .getFeatureIterator(sqlQuery, null);
 	    return it;
 	} catch (ReadDriverException e) {
 	    e.printStackTrace();
 	    return null;
 	}
     }
 
     @Override
     public String getMessage() {
 	return PluginServices.getText(this,
 		"action_update_id_predio_for_construccion");
     }
 
 }
