 package gov.usgs.cida.coastalhazards.wps;
 
 import gov.usgs.cida.coastalhazards.util.GeoserverUtils;
 import gov.usgs.cida.coastalhazards.util.LayerImportUtil;
 import java.io.IOException;
import java.util.Set;
 import org.apache.commons.lang.StringUtils;
 import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
 import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.ResourceInfo;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.feature.ReprojectingFeatureCollection;
 import org.geoserver.wps.gs.GeoServerProcess;
 import org.geoserver.wps.gs.ImportProcess;
 import org.geotools.data.DataAccess;
 import org.geotools.data.DataUtilities;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.factory.GeoTools;
 import org.geotools.factory.Hints;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.SchemaException;
 import org.geotools.process.ProcessException;
 import org.geotools.process.factory.DescribeParameter;
 import org.geotools.process.factory.DescribeProcess;
 import org.geotools.process.factory.DescribeResult;
 import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.factory.epsg.DirectEpsgFactory;
 import org.geotools.referencing.factory.epsg.ThreadedEpsgFactory;
 import org.geotools.referencing.factory.epsg.ThreadedHsqlEpsgFactory;
 import org.opengis.feature.Feature;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.FeatureType;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
 
 /**
  *
  * @author isuftin
  */
 @DescribeProcess(
 		title = "Publish Resource",
 		description = "Publishes one or more resources by moving the resource into the workspace that is used for published resources",
 		version = "1.0.0")
 public class CopyLayer implements GeoServerProcess {
 
 	private Catalog catalog;
 	private ImportProcess importProc;
 
 	public CopyLayer(Catalog catalog, ImportProcess importProc) {
 		this.catalog = catalog;
 		this.importProc = importProc;
 	}
 
 	@DescribeResult(name = "String", description = "New resource as workspace:name")
 	public String execute(
 			@DescribeParameter(name = "source-workspace", min = 1, max = 1, description = "Workspace in which store resides") String sourceWorkspace,
 			@DescribeParameter(name = "source-store", min = 1, max = 1, description = "Store in which layer resides") String sourceStore,
 			@DescribeParameter(name = "source-layer", min = 1, max = -1, description = "Layer(s) to be published") String sourceLayer,
 			@DescribeParameter(name = "target-workspace", min = 1, max = 1, description = "Workspace to copy layer to") String targetWorkspace,
 			@DescribeParameter(name = "target-store", min = 1, max = 1, description = "Store to copy layer to") String targetStore,
 			@DescribeParameter(name = "declared-srs", min = 0, max = 1, description = "Set the layer's declared SRS") String declaredSRS)
 			throws ProcessException, IOException, NoSuchAuthorityCodeException, FactoryException, SchemaException {
 
 		GeoserverUtils gUtils = new GeoserverUtils(this.catalog);
 
 		WorkspaceInfo sourceWorkspaceInfo = gUtils.getWorkspaceByName(sourceWorkspace);
 		DataStoreInfo sourceStoreInfo = gUtils.getDataStoreByName(sourceWorkspaceInfo.getName(), sourceStore);
 		DataAccess<? extends FeatureType, ? extends Feature> sourceDataAccess = gUtils.getDataAccess(sourceStoreInfo, null);
 		FeatureSource<? extends FeatureType, ? extends Feature> sourceFeatureSource = gUtils.getFeatureSource(sourceDataAccess, sourceLayer);
 		FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = (FeatureCollection<SimpleFeatureType, SimpleFeature>) gUtils.getFeatureCollection(sourceFeatureSource);
 		SimpleFeatureCollection simpleFeatureCollection = (SimpleFeatureCollection) DataUtilities.collection(featureCollection);
 		
 		Hints hints = GeoTools.getDefaultHints().clone();
 		ReferencingFactoryFinder.scanForPlugins();
 		ThreadedEpsgFactory epsgFactory = new ThreadedHsqlEpsgFactory(hints);
		FeatureCollection<SimpleFeatureType, SimpleFeature> fc;
		
 		if (StringUtils.isNotBlank(declaredSRS)) {
			fc = new ReprojectingFeatureCollection(simpleFeatureCollection, epsgFactory.createCoordinateReferenceSystem(declaredSRS));
 		} else {
			fc = (FeatureCollection<SimpleFeatureType, SimpleFeature>) gUtils.getFeatureCollection(sourceFeatureSource);
 		}
		
 		LayerImportUtil importer = new LayerImportUtil(catalog, importProc);
		String response = importer.importLayer((SimpleFeatureCollection) DataUtilities.collection(fc), targetWorkspace, targetStore, sourceLayer, fc.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem(), ProjectionPolicy.REPROJECT_TO_DECLARED);
 
 		return response;
 	}
 }
