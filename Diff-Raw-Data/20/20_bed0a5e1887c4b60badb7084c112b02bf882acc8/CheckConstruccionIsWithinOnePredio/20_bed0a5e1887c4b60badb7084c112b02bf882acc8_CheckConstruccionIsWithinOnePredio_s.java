 package es.icarto.gvsig.catastro.evaluator.rules;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.andami.PluginServices;
 import com.iver.cit.gvsig.fmap.core.IGeometry;
 import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
 import com.iver.cit.gvsig.fmap.layers.FLyrVect;
 import com.vividsolutions.jts.geom.Geometry;
 
 import es.icarto.gvsig.catastro.constants.ConstantManager;
 import es.icarto.gvsig.catastro.utils.Preferences;
 import es.icarto.gvsig.catastro.utils.TOCLayerManager;
 
 public class CheckConstruccionIsWithinOnePredio implements IRule {
 
     IGeometry insertedGeometry;
 
     public CheckConstruccionIsWithinOnePredio(IGeometry insertedGeometry) {
 	this.insertedGeometry = insertedGeometry;
     }
 
     @Override
     public boolean isObey() {
 	Geometry construccionJTSGeom = insertedGeometry.toJTSGeometry();
 	Geometry predioJTSGeom = getPredioGeom();
	Geometry predioJTSGeomWithBuffer = predioJTSGeom.buffer(0.5);
	if (!construccionJTSGeom.coveredBy(predioJTSGeomWithBuffer)) {
	    return false;
 	}
	return true;
     }
 
     @Override
     public String getMessage() {
 	return PluginServices.getText(this,
 		"rule_construccion_is_not_within_predio");
     }
 
     private Geometry getPredioGeom() {
 	TOCLayerManager tocLayerManager = new TOCLayerManager();
 	FLyrVect predio = tocLayerManager
 		.getLayerByName(Preferences.PREDIOS_LAYER_NAME);
 	IGeometry predioGeom = getGeomFromFLyrVect(predio);
 	Geometry predioJTSGeom = predioGeom.toJTSGeometry();
 	return predioJTSGeom;
     }
 
     private IGeometry getGeomFromFLyrVect(FLyrVect layer) {
 	ConstantManager constantManager = new ConstantManager();
 	try {
 	    String sqlQuery = "select * from '"
 		    + layer.getRecordset().getName() + "'" + " where "
 		    + Preferences.PAIS_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getPais() + " " + " and "
 		    + Preferences.ESTADO_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getEstado() + " "
 		    + " and " + Preferences.MUNICIPIO_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getMunicipio() + " "
 		    + " and " + Preferences.LIMITE_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getLimiteMunicipal() + " "
 		    + " and " + Preferences.PREDIO_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getPredio() + " "
 		    + " and " + Preferences.MANZANA_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getManzana() + " "
 		    + " and " + Preferences.REGION_NAME_IN_DB + " = "
 		    + constantManager.getConstants().getRegion() + ";";
 	    IFeatureIterator featureIterator = layer.getSource()
 		    .getFeatureIterator(sqlQuery, null);
 	    return featureIterator.next().getGeometry();
 	} catch (ReadDriverException e) {
 	    e.printStackTrace();
 	    return null;
 	}
     }
 }
