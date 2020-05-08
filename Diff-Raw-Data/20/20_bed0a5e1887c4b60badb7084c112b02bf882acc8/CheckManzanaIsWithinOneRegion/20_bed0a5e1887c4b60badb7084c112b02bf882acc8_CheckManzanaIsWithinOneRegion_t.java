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
 
 public class CheckManzanaIsWithinOneRegion implements IRule {
 
     IGeometry insertedGeometry;
 
     public CheckManzanaIsWithinOneRegion(IGeometry insertedGeometry) {
 	this.insertedGeometry = insertedGeometry;
     }
 
     @Override
     public boolean isObey() {
 	Geometry manzanaJTSGeom = insertedGeometry.toJTSGeometry();
 	Geometry regionJTSGeom = getRegionGeom();
	//	Geometry regionJTSToleranceBuffer = regionJTSGeom.buffer(0.5);
	//	if (!manzanaJTSGeom.coveredBy(regionJTSToleranceBuffer)) {
	if (!manzanaJTSGeom.coveredBy(regionJTSGeom)) {
 	    return false;
 	}
 	return true;
     }
 
     private Geometry getRegionGeom() {
 	TOCLayerManager tocLayerManager = new TOCLayerManager();
 	FLyrVect region = tocLayerManager
 		.getLayerByName(Preferences.REGIONES_LAYER_NAME);
 	IGeometry regionGeom = getGeomFromFLyrVect(region);
 	Geometry regionJTSGeom = regionGeom.toJTSGeometry();
 	return regionJTSGeom;
     }
 
     private IGeometry getGeomFromFLyrVect(FLyrVect layer) {
 	ConstantManager constantManager = new ConstantManager();
 	try {
 	    String sqlQuery = "select * from '" + layer.getRecordset().getName() + "'" +
 		    " where " + Preferences.PAIS_NAME_IN_DB + " = " + constantManager.getConstants().getPais() + " "+
 		    " and " + Preferences.ESTADO_NAME_IN_DB + " = " + constantManager.getConstants().getEstado() + " "+
 		    " and " + Preferences.MUNICIPIO_NAME_IN_DB + " = " + constantManager.getConstants().getMunicipio() + " "+
 		    " and " + Preferences.LIMITE_NAME_IN_DB + " = " + constantManager.getConstants().getLimiteMunicipal() + " "+
 		    " and " + Preferences.REGION_NAME_IN_DB + " = " + constantManager.getConstants().getRegion() +";";
 	    IFeatureIterator featureIterator = layer.getSource()
 		    .getFeatureIterator(sqlQuery, null);
 	    return featureIterator.next().getGeometry();
 	} catch (ReadDriverException e) {
 	    e.printStackTrace();
 	    return null;
 	}
     }
 
     @Override
     public String getMessage() {
 	return PluginServices
 		.getText(this, "rule_manzana_is_not_within_region");
     }
 
 }
