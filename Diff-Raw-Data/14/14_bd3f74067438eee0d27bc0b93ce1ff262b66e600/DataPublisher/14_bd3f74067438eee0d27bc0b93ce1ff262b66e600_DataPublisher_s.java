 package org.fao.fi.gis.metadata.publisher;
 
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
 import it.geosolutions.geoserver.rest.GeoServerRESTReader;
 import it.geosolutions.geoserver.rest.HTTPUtils;
 import it.geosolutions.geoserver.rest.GeoServerRESTPublisher.UploadMethod;
 import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder;
 import it.geosolutions.geoserver.rest.encoder.GSLayerEncoder21;
 import it.geosolutions.geoserver.rest.encoder.GSResourceEncoder.ProjectionPolicy;
 import it.geosolutions.geoserver.rest.encoder.authorityurl.GSAuthorityURLInfoEncoder;
 import it.geosolutions.geoserver.rest.encoder.feature.GSFeatureTypeEncoder;
 import it.geosolutions.geoserver.rest.encoder.identifier.GSIdentifierInfoEncoder;
 import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.GSVirtualTableEncoder;
 import it.geosolutions.geoserver.rest.encoder.metadata.virtualtable.VTGeometryEncoder;
 import it.geosolutions.geoserver.rest.encoder.metadatalink.GSMetadataLinkInfoEncoder;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.io.FileUtils;
 import org.fao.fi.gis.metadata.association.GeographicMetaObject;
 import org.fao.fi.gis.metadata.association.GeographicMetaObjectProperty;
 import org.fao.fi.gis.metadata.model.settings.GeographicServerSettings;
 import org.fao.fi.gis.metadata.model.settings.MetadataCatalogueSettings;
 import org.fao.fi.gis.metadata.util.Utils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * DataPublisher
  * 
  * @author eblondel (FAO)
  * 
  */
 public class DataPublisher {
 
 	private static Logger LOGGER = LoggerFactory.getLogger(DataPublisher.class);
 	
 	private static final String crs = "GEOGCS[\"WGS 84\", \n  DATUM[\"World Geodetic System 1984\", \n    SPHEROID[\"WGS 84\", 6378137.0, 298.257223563, AUTHORITY[\"EPSG\",\"7030\"]], \n    AUTHORITY[\"EPSG\",\"6326\"]], \n  PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \n  UNIT[\"degree\", 0.017453292519943295], \n  AXIS[\"Geodetic longitude\", EAST], \n  AXIS[\"Geodetic latitude\", NORTH], \n  AUTHORITY[\"EPSG\",\"4326\"]]";
 
 	String geoserverBaseURL;
 	String gsUser;
 	String gsPwd;
 
 	public GeoServerRESTReader GSReader;
 	public GeoServerRESTPublisher GSPublisher;
 	
 	String srcLayer;
 	String srcAttribute;
 	String trgWorkspace;
 	String trgDatastore;
 	String trgLayerPrefix;
 	
 	String geonetworkBaseURL;
 
 
 	/**
 	 * Data publisher
 	 * 
 	 * @param settings
 	 * @param catalogueSettings
 	 * @throws MalformedURLException
 	 */
 	public DataPublisher(GeographicServerSettings settings, MetadataCatalogueSettings catalogueSettings)
 			throws MalformedURLException {
 
 		this.geoserverBaseURL = settings.getUrl();
 		this.gsUser = settings.getUser();
 		this.gsPwd = settings.getPassword();
 
 		this.GSReader = new GeoServerRESTReader(geoserverBaseURL, gsUser, gsPwd);
 		this.GSPublisher = new GeoServerRESTPublisher(geoserverBaseURL, gsUser, gsPwd);
 
 		this.srcLayer = settings.getSourceLayer();
 		this.srcAttribute = settings.getSourceAttribute();
 		this.trgWorkspace = settings.getTargetWorkspace();
 		this.trgDatastore = settings.getTargetDatastore();
 		this.trgLayerPrefix = settings.getTargetLayerPrefix();
 
 		this.geonetworkBaseURL = catalogueSettings.getUrl();
 		
 	}
 
 
 	/**
 	 * Check layer existence
 	 * 
 	 * 
 	 * @param code
 	 * @return
 	 * @throws Exception
 	 */
 	public boolean checkLayerExistence(String code) {
 		String layername = this.trgLayerPrefix + "_" + code;
 
 		// using geoserver-manager
 		List<String> layers = GSReader.getLayers().getNames();
 		return layers.contains(layername);
 	}
 
 	/**
 	 * Delete the layer
 	 * 
 	 * @param object
 	 * @throws Exception
 	 */
 	public boolean deleteLayer(GeographicMetaObject object) {
 		String layername = object.getTargetLayerName();
 
 		// using geoserver-manager
 		return GSPublisher.unpublishFeatureType(trgWorkspace, trgDatastore,
 				layername);
 	}
 
 	public boolean deleteOnlyFeatureType(GeographicMetaObject object)
 			throws MalformedURLException {
 		URL deleteFtUrl = new URL(this.geoserverBaseURL + "/rest/workspaces/"
 				+ this.trgWorkspace + "/datastores/" + this.trgDatastore
 				+ "/featuretypes/" + object.getTargetLayerName());
 
 		boolean ftDeleted = HTTPUtils.delete(deleteFtUrl.toExternalForm(),
 				this.gsUser, this.gsPwd);
 		return ftDeleted;
 	}
 
 	/**
 	 * Publish a layer (as GeoServer SQL View layer)
 	 * 
 	 * @param object
 	 * @throws IOException 
 	 */
 	public boolean publishLayer(GeographicMetaObject object, String style, PublicationMethod method, String shapefile) throws IOException {
 
 		
 		// Using geoserver-manager
 		// -----------------------
 		final GSFeatureTypeEncoder fte = new GSFeatureTypeEncoder();
 		fte.setProjectionPolicy(ProjectionPolicy.REPROJECT_TO_DECLARED);
 		fte.setNativeName(object.getTargetLayerName());
 		fte.setName(object.getTargetLayerName());
 		fte.setTitle(object.getMetaTitle());
 		fte.setSRS("EPSG:4326");
 		fte.setNativeCRS("EPSG:4326");
 		fte.setEnabled(true);
 
 		for (Entry<GeographicMetaObjectProperty, List<String>> entry : object
 				.getSpecificProperties().entrySet()) {
 			if (entry.getKey().isThesaurus()) {
 				if (!entry.getKey().containsURIs()) {
 					for (String kw : entry.getValue()) {
 						String keyword = kw + " ("
 								+ entry.getKey().authority().name() + ")";
 						fte.addKeyword(keyword);
 					}
 				}
 			}
 		}
 
 		Envelope bbox = object.getBBOX();
 		if (bbox != null) {
 			fte.setNativeBoundingBox(bbox.getMinX(), bbox.getMinY(),
 					bbox.getMaxX(), bbox.getMaxY(), "EPSG:4326");
 			fte.setLatLonBoundingBox(bbox.getMinX(), bbox.getMinY(),
 					bbox.getMaxX(), bbox.getMaxY(), "EPSG:4326");
 		} else {
 			fte.setNativeBoundingBox(-180.0, -90.0, 180.0, 90.0, "EPSG:4326");
 			fte.setLatLonBoundingBox(-180.0, -90.0, 180.0, 90.0, "EPSG:4326");
 		}

 		// virtual table (sql view)
 		if(method == PublicationMethod.SQLVIEW){
			VTGeometryEncoder gte = new VTGeometryEncoder("THE_GEOM",
 					"MultiPolygon", "4326");
 			String sql = "SELECT * FROM " + this.srcLayer + " WHERE "
 					+ this.srcAttribute + " = '" + object.getCode() + "'";
 			GSVirtualTableEncoder vte = new GSVirtualTableEncoder(
 					object.getTargetLayerName(), sql, null, Arrays.asList(gte),
 					null);
 			fte.setMetadataVirtualTable(vte);
 		}
 			
 		// metadata
 		final GSMetadataLinkInfoEncoder mde1 = new GSMetadataLinkInfoEncoder(
 				"text/xml", "ISO19115:2003", Utils.getXMLMetadataURL(
 						this.geonetworkBaseURL, object.getMetaIdentifier()));
 		final GSMetadataLinkInfoEncoder mde2 = new GSMetadataLinkInfoEncoder(
 				"text/html", "ISO19115:2003", Utils.getHTMLMetadataURL(
 						this.geonetworkBaseURL, object.getMetaIdentifier()));
 		fte.addMetadataLinkInfo(mde1);
 		fte.addMetadataLinkInfo(mde2);
 
 		// layer
 		final GSLayerEncoder layerEncoder = new GSLayerEncoder21();
 		layerEncoder.setDefaultStyle(style);
 
 		// add authorityURL & identifiers
 		for (Entry<GeographicMetaObjectProperty, List<String>> entry : object
 				.getSpecificProperties().entrySet()) {
 			if (entry.getKey().isThesaurus()) {
 				if (entry.getKey().containsURIs()) {
 					layerEncoder.addAuthorityURL(new GSAuthorityURLInfoEncoder(
 							entry.getKey().authority().name(), entry.getKey()
 									.authority().href()));
 					for (String identifier : entry.getValue()) {
 						layerEncoder.addIdentifier(new GSIdentifierInfoEncoder(
 								entry.getKey().authority().name(), identifier));
 								LOGGER.debug("add "+entry.getKey().authority().name()+" identifier: "+identifier);
 					}
 				}
 			}
 		}
 		
 		// publication
 		boolean publish = false;
 		if(method == PublicationMethod.SQLVIEW){
 			publish = GSPublisher.publishDBLayer(trgWorkspace, trgDatastore, fte, layerEncoder);
 			
 		}else if(method == PublicationMethod.SHAPEFILE){
 			
 			//prepare shapefile
 			URL url = new URL(shapefile);
 			String tDir = System.getProperty("java.io.tmpdir");
 			String inputpath = tDir + "tmp" + ".zip";
 			File file = new File(inputpath);
 			file.deleteOnExit();
 			FileUtils.copyURLToFile(url, file);
 			
 			publish = GSPublisher.publishShp(trgWorkspace, trgDatastore,
 					new NameValuePair[0], object.getTargetLayerName(),
 					UploadMethod.FILE, file.toURI(), fte, layerEncoder);
 		}
 		return publish;
 
 	}
 }
