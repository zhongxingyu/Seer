 /*
  *  GeoBatch - Open Source geospatial batch processing system
  *  http://code.google.com/p/geobatch/
  *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package it.geosolutions.geobatch.figis.intersection;
 
 import it.geosolutions.figis.model.Config;
 import it.geosolutions.figis.model.Geoserver;
 import it.geosolutions.figis.model.Intersection;
 import it.geosolutions.figis.model.Intersection.Status;
 import it.geosolutions.figis.persistence.dao.util.PwEncoder;
 import it.geosolutions.figis.requester.requester.dao.IEConfigDAO;
 import it.geosolutions.geobatch.flow.event.action.ActionException;
 import it.geosolutions.geobatch.flow.event.action.BaseAction;
 import it.geosolutions.geoserver.rest.GeoServerRESTReader;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.EventObject;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FileDataStore;
 import org.geotools.data.shapefile.ShapefileDataStore;
 import org.geotools.data.simple.SimpleFeatureCollection;
 import org.geotools.data.simple.SimpleFeatureIterator;
 import org.geotools.process.feature.gs.ClipProcess;
 import org.geotools.process.feature.gs.IntersectionFeatureCollection;
 import org.geotools.process.feature.gs.IntersectionFeatureCollection.IntersectionMode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.thoughtworks.xstream.InitializationException;
 import com.vividsolutions.jts.geom.Geometry;
 
 /**
  * 
  * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
  * 
  */
 public class IntersectionAction extends BaseAction<EventObject> {
 	
 	private static final Logger LOGGER = LoggerFactory.getLogger(IntersectionAction.class);
 	
 	private final List<ShapefileDataStore> shapeFileStores= new ArrayList<ShapefileDataStore>();
 	
 	private int itemsPerPage = 50;
 	private IEConfigDAO ieConfigDAO = null;
 	private GeoServerRESTReader gsRestReader = null;
 	private Geoserver geoserver = null;
 
 	/**
 	 * configuration
 	 */
 	private final IntersectionConfiguration conf;
 	private String host = "http://localhost:9999";
 	private final String TMP_DIR_NAME = "figis";
 
 	/** Username ie-service */
 	private String ieServiceUsername = null;
 
 	/** Password ie-service */
 	private String ieServicePassword = null;
 
 	public IntersectionAction(IntersectionConfiguration configuration) throws MalformedURLException {
 		super(configuration);
 		conf = configuration;
 		host = conf.getPersistencyHost();
 		itemsPerPage = conf.getItemsPerPages();
 		ieServiceUsername = conf.getIeServiceUsername();
 		ieServicePassword = conf.getIeServicePassword();
 	}
 
 	/******
 	 * this method takes a zip file name and return its SimpleFeatureCollection
 	 * 
 	 * @param filename
 	 *            the full name of the shape file
 	 * @return the simple feature collection
 	 */
 	private SimpleFeatureCollection simpleFeatureCollectionByShp(String filename) {
 		if (filename == null) {
 			LOGGER.error("Shapefile name is null!");
 			return null;
 		}
 		File shpfile = new File(filename);
 		if (LOGGER.isTraceEnabled()) {
 			LOGGER.trace("ZSR: SimpleFeatureCollection: shpfile: " + shpfile);
 			LOGGER.trace("ZSR: SimpleFeatureCollection: shpfile.getAbsolutePath(): " + shpfile.getAbsolutePath());
 			LOGGER.trace("ZSR: SimpleFeatureCollection: shpfile.getName(): " + shpfile.getName());
 		}
 		if (!shpfile.exists() || !shpfile.canRead()) {
 			return null;
 		}
 
 		try {
 			ShapefileDataStore store = new ShapefileDataStore(shpfile.toURI().toURL());
 			shapeFileStores.add(store);
 			FeatureSource fs = store.getFeatureSource();
 			return (SimpleFeatureCollection) fs.getFeatures();
 		} catch (Exception e1) {
 			LOGGER.trace("ZSR: error:", e1.getLocalizedMessage(), e1);
 			return null;
 		}
 
 	}
 
 	/**********
 	 * download a zip file from geoserver, save it in the tmp directory,
 	 * uncompress it and return the name of the containe shape file
 	 * 
 	 * @param layername
 	 *            the layer to download
 	 * @param tmpDir
 	 *            the folder where saving and uncompress the downloaded zip file
 	 * @return the name of the shape file downloaded
 	 */
 	private String downloadFromGeoserver(String layername, String tmpDir) {
 		String urlCollection = "";
 		try {
 			LOGGER.info("Downloading " + layername + " ");
 
 			String name = Utilities.getName(layername);
 			if (geoserver != null) {
 				LOGGER.info("Georserverl URL " + geoserver.getGeoserverUrl());
 				// build the url to download the zip file by wfs
 				urlCollection = geoserver.getGeoserverUrl()
 						+ "/wfs?outputFormat=SHAPE-ZIP&request=GetFeature&version=1.1.1&typeName="
 						+ name + "&srs=EPSG:4326";
 			} else {
 				LOGGER.error("Geoserver is null, check the data");
 
 				return null;
 			}
 
 			// uncompress the downloaded file and return the name of the shape
 			// file
 			return Utilities.getShapeFileFromURLbyZIP(urlCollection,tmpDir, name);
 		} catch (Exception e) {
 			LOGGER.error("Failed to download the " + urlCollection + " layer",e);
 
 			return null;
 		}
 	}
 
 	/***********
 	 * perform the ie-intersection action
 	 * 
 	 * @param intersection
 	 *            the data on which to perform the intersection
 	 * @return true in case no intersection is performed because any reason
 	 */
 
 	private SimpleFeatureCollection intersect(Intersection intersection, String tmpDir) {
 
 		// initialize variables
 		String srcLayer = intersection.getSrcLayer();
 		String trgLayer = intersection.getTrgLayer();
 		String srcCodeField = intersection.getSrcCodeField();
 		String trgCodeField = intersection.getTrgCodeField();
 		String maskLayer = intersection.getMaskLayer();
 
 		boolean isMasked = intersection.isMask();
 
 		IntersectionMode mode = IntersectionMode.INTERSECTION;
 		if (!intersection.isPreserveTrgGeom()) {
 			mode = IntersectionMode.SECOND;
 		}
 
 		// load feature collections from geoserver
 		SimpleFeatureCollection srcCollection = null;
 		SimpleFeatureCollection trgCollection = null;
 		SimpleFeatureCollection maskCollection = null;
 		String srcfilename = null;
 		String trgfilename = null;
 		String maskfilename = null;
 		try {
 			// try to load src collection
 			LOGGER.info("download first geometry " + srcLayer + " " + srcCodeField);
 			srcfilename = downloadFromGeoserver(srcLayer, tmpDir);
 			LOGGER.info("finish download first geometry");
 			srcCollection = simpleFeatureCollectionByShp(srcfilename);
 
 			// check if the src attribute exists and srcCollection is not empty
 			if ((srcCollection == null) || (srcCollection.getSchema().getDescriptor(srcCodeField) == null)) {
 				
 				LOGGER.error("Error downloading " + srcLayer + " or the " + srcCodeField + " attribute does not exist");
 
 				return null;
 			}
 
 			// try to load trg collection
 			LOGGER.info("download second geometry " + trgLayer + " " + trgCodeField);
 			trgfilename = downloadFromGeoserver(trgLayer, tmpDir);
 			LOGGER.info("finish download second geometry");
 			trgCollection = simpleFeatureCollectionByShp(trgfilename);
 			
 			// check if the src attribute exists and trgCollection is not empty
 			if ((trgCollection == null) || (trgCollection.getSchema().getDescriptor(trgCodeField) == null)) {
 				LOGGER.error("Error downloading " + trgLayer + " or the " + trgCodeField + " attribute does not exist");
 
 				return null;
 			}
 
 			// try to load mask collection
 			if (isMasked) {
 				if(LOGGER.isTraceEnabled()){
 					LOGGER.trace("download mask layer " + Utilities.getName(maskLayer));
 				}
 				maskfilename = downloadFromGeoserver(Utilities.getName(maskLayer), tmpDir);
 				if(LOGGER.isTraceEnabled()){
 					LOGGER.trace("mask layer " + maskLayer + " downloaded");
 				}
 				maskCollection = simpleFeatureCollectionByShp(maskfilename);
 
 				if (maskCollection == null) {
 					LOGGER.error("Error downloading " + maskLayer);
 
 					return null;
 				}
 			}
 
 			// check if intersection requires masking
 			if (isMasked) {
 				if(LOGGER.isTraceEnabled()){
 					LOGGER.trace("Mask flag is set on true");
 				}
 				//
 				// generate the union of the mask geometries
 				//
 				SimpleFeatureIterator sfi = null;
 				Geometry maskGeometry = null;
 				try {
 					sfi = maskCollection.features();
 					if(LOGGER.isTraceEnabled()){
 						LOGGER.trace("Generating the union geometry");
 					}
 					if (sfi.hasNext()) {
 						maskGeometry = (Geometry) sfi.next().getDefaultGeometry();
 					}
 					while (sfi.hasNext()) {
 						Geometry geometry = (Geometry) sfi.next().getDefaultGeometry();
 						// maskGeometry = maskGeometry.union(geometry);
 						maskGeometry = Utilities.union(maskGeometry, geometry);
 					}
 				} catch (Exception e) {
 					// FIXME for the moment I throw na exception and exit. It
 					// might be enough to continue without masking
 					LOGGER.error(e.getLocalizedMessage(), e);
 					return null;
 				} finally {
 					if (sfi != null) {
 						try {
 							sfi.close();
 						} catch (Exception e) {
							LOGGER.trace(e.getLocalizedMessage(), e);
 						}
 					}
 				}
 
 				//
 				// clip the src Collection using the mask collection
 				//
 				final ClipProcess clipProcess = new ClipProcess();
 				if(LOGGER.isTraceEnabled()){
 					LOGGER.trace("clipping the first layer");
 				}
 				try {
 					srcCollection = clipProcess.execute(srcCollection,maskGeometry);
 				} catch (Exception e) {
 					LOGGER.error("Exception when clipping the first layer", e);
 					return null;
 				}
 				if(LOGGER.isTraceEnabled()){
 					LOGGER.trace("clipping the second layer");
 				}
 				
 				try {
 					// clip the trg Collection using the mask collection
 					trgCollection = clipProcess.execute(trgCollection,maskGeometry);
 				} catch (Exception e) {
 					LOGGER.error("Exception when clipping the second layer", e);
 					return null;
 				}
 
 			}
 
 			// setup for the IntersectionFeatureCollectionProcess
 			List<String> srcAttributes = new ArrayList<String>();
 			srcAttributes.add(srcCodeField);
 
 			List<String> trgAttributes = new ArrayList<String>();
 			trgAttributes.add(trgCodeField);
 
 			// perform the IntersectionFeatureCollection process
 			IntersectionFeatureCollection intersectionProcess = new IntersectionFeatureCollection();
 			SimpleFeatureCollection result2 = null;
 			try {
 				result2 = intersectionProcess.execute(srcCollection,trgCollection, srcAttributes, trgAttributes, mode,true, true);
 			} catch (Exception e) {
 				LOGGER.error("Exception when performing the intersection", e);
 				return null;
 			}
 			if(LOGGER.isTraceEnabled()){
 				LOGGER.trace("Intersection process prepared");
 			}
 			return result2;
 		} catch (Throwable e) {
 			LOGGER.error("Failed to load some layers", e);
 
 			return null;
 		} finally {
 			if (srcCollection != null) {
 				try {
 					srcCollection.clear();
 				} catch (Exception e) {
 					if(LOGGER.isErrorEnabled()){
 						LOGGER.error("Failed to clear srcCollection", e);
 					}
 				}
 			}
 			if (trgCollection != null) {
 				try {
 					trgCollection.clear();
 				} catch (Exception e) {
					if(LOGGER.isErrorEnabled()){
						LOGGER.error("Failed to clear srcCollection", e);
					}
 				}
 			}
 			if (maskCollection != null) {
 				try {
 					maskCollection.clear();
 				} catch (Exception e) {
					if(LOGGER.isErrorEnabled()){
						LOGGER.error("Failed to clear srcCollection", e);
					}
 				}
 			}
 		}
 	}
 
 	boolean initConnections(Geoserver geoserver) {
 		try {
 			// check if this control works as expected
 			String url = geoserver.getGeoserverUrl();
 			LOGGER.trace(url + " USER " + geoserver.getGeoserverUsername() + ", PWD(enc) " + geoserver.getGeoserverPassword());
 			gsRestReader = new GeoServerRESTReader(url, geoserver
 					.getGeoserverUsername(), PwEncoder.decode(geoserver.getGeoserverPassword()));
 
 			if (!gsRestReader.existGeoserver()) {
 				return false;
 			}
 
 		} catch (Exception e) {
 			LOGGER.error("Exception when initializing the connection", e);
 
 			return false;
 		}
 		LOGGER.info("Connection successfully initialized");
 
 		return true;
 	}
 
 	/************
 	 * execute the intersections and update their status on the basis of the
 	 * content od the intersections table if status is DELETE then the
 	 * intersection will be removed if status is TOCOMPUTE then the intersection
 	 * will be executed, stored and published and then the STATUS will be
 	 * updated to COMPUTED otherwise no changes are applied to the intersection
 	 * 
 	 * @param host
 	 *            where address the request of delete, update
 	 * @param intersections
 	 *            the intersections to parse
 	 * @return true everything goes well
 	 * @throws Exception
 	 */
 	boolean executeIntersectionStatements(String host, Config config, boolean simulate, String tmpdir) throws InitializationException {
 		
 		OracleDataStoreManager dataStoreOracle = null;
 		// check if this control works as expected
 		if (config.intersections == null) {
 			LOGGER.error("The list of the intersections is null, cannot continue");
 
 			return false;
 		}
 		LOGGER.info("Updating intersections");
 
 		// init of the DB connectio to the ORACLE datastore
 		String dbHost = config.getGlobal().getDb().getHost();
 		String schema = config.getGlobal().getDb().getSchema();
 		String db = config.getGlobal().getDb().getDatabase();
 		String user = config.getGlobal().getDb().getUser();
 		String pwd = PwEncoder.decode(config.getGlobal().getDb().getPassword());
 		int port = Integer.parseInt(config.getGlobal().getDb().getPort());
 
 		try {
 			dataStoreOracle = new OracleDataStoreManager(dbHost, port, db, schema, user, pwd);
 		} catch (Exception e) {
 			throw new InitializationException("Problems creating the ORACLE datastore instance, check the parameters", e);
 		}
 
 		for (Intersection intersection : config.intersections) {
 
 			Status status = intersection.getStatus();
 			String srcLayer = intersection.getSrcLayer();
 			String trgLayer = intersection.getTrgLayer();
 			String srcCode = intersection.getSrcCodeField();
 			String trgCode = intersection.getTrgCodeField();
 			long id = intersection.getId();
 
 			// in case the intersection has been scheduled to be deleted,
 			// delete the intersection from the list
 			// and its intersection from the DB
 			LOGGER.info(" Performing intersection command " + status);
 			LOGGER.info("COMMAND: " + intersection);
 			if (status == Status.TODELETE) {
 				try {
 					dataStoreOracle.deleteAll(Utilities.getName(srcLayer), Utilities.getName(trgLayer), srcCode, trgCode);
 					ieConfigDAO.deleteIntersectionById(host, id, ieServiceUsername, ieServicePassword);
 				} catch (Exception e) {
 					LOGGER.error("Problem deleting intersection from the database identified by " + srcLayer + "," + trgLayer, e);
 				}
 				// still to implement
 			}
 			if (status == Status.TOCOMPUTE) // if the intersection should be
 			// computed
 			{
 				intersection.setStatus(Status.COMPUTING);
 				ieConfigDAO.updateIntersectionById(host, id, intersection, ieServiceUsername, ieServicePassword);
 
 				SimpleFeatureCollection resultFeatureCollection = null;
 				try {
 					resultFeatureCollection = intersect(intersection, tmpdir);
 
 					String geometryType = "unknown";
 
 					if (resultFeatureCollection != null) {
 						geometryType = resultFeatureCollection.getSchema()
 								.getGeometryDescriptor().getType().getName().getLocalPart();
 
 						// the intersection can be updated on the db only if the
 						// intersection generate a reuslt and it is Multipolygon
 						// typed
 						// else it must be deleted by both the db and by the
 						// intersection list
 						if (geometryType.equals("MultiPolygon")) {
 							// set the intersection to Computing. This is to
 							// avoid
 							// that a concurrent
 							// compute again the intersection
 							intersection.setStatus(Status.COMPUTING);
 							ieConfigDAO.updateIntersectionById(host, id, intersection, ieServiceUsername, ieServicePassword);
 
 							LOGGER.info("Trying to store intersection result in ORACLE "
 											+ schema
 											+ ":"
 											+ db
 											+ " on "
 											+ dbHost
 											+ ":"
 											+ port
 											+ "("
 											+ user
 											+ ",*****) ");
 							dataStoreOracle.saveAll(resultFeatureCollection,
 									Utilities.getName(srcLayer), Utilities
 											.getName(trgLayer), srcCode,
 									trgCode, itemsPerPage);
 							intersection.setStatus(Status.COMPUTED);
 							ieConfigDAO.updateIntersectionById(host, id, intersection, ieServiceUsername, ieServicePassword);
 							LOGGER.info("Store operation successfully computed");
 						} else {
 							LOGGER.error("Skipping intersection between "
 											+ srcLayer
 											+ " and "
 											+ trgLayer
 											+ " because the intersection cannot be computed");
 							LOGGER.error("Intersections will be deleted");
 							// Request.deleteIntersectionById(host,
 							// id,ieServiceUsername,ieServicePassword);
 							intersection.setStatus(Status.FAILED);
 							ieConfigDAO.updateIntersectionById(host, id, intersection, ieServiceUsername, ieServicePassword);
 						}
 					} else {
 						intersection.setStatus(Status.FAILED);
 						ieConfigDAO.updateIntersectionById(host, id, intersection, ieServiceUsername, ieServicePassword);
 					}
 				} catch (Exception e) {
 					LOGGER.error("Problem performing Intersection on "
 							+ srcLayer + "," + trgLayer + "," + srcCode + ","
 							+ trgCode, e);
 					intersection.setStatus(Status.FAILED);
 					ieConfigDAO.updateIntersectionById(host, id, intersection, ieServiceUsername, ieServicePassword);
 					throw new RuntimeException("Exception caught while computing intersections.", e);
 				} finally {
 					if (resultFeatureCollection != null) {
 						try{
 							resultFeatureCollection.clear();
 						} catch (Exception e){
 							LOGGER.error("ERROR on cleaning resultFeatureCollection: ", e);
 						}
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	Config checkConfiguration(Config config) {
 		// check if this control works as expected
 		if (config == null) {
 			LOGGER.error("Problems to find config information. ");
 
 			return null;
 		}
 		LOGGER.info("Config information correctly read. Trying to connect to Geoserver on "
 						+ config.getGlobal().getGeoserver().getGeoserverUrl());
 		// check the datastore and REST manager geoserver connections
 		if (!initConnections(config.getGlobal().getGeoserver())) {
 			LOGGER.error("Problems to find Geoserver. ");
 
 			return null;
 		}
 		LOGGER.info("Geoserver found");
 
 		return config;
 	}
 
 	/**
 	 * Removes TemplateModelEvents from the queue and put
 	 */
 	public Queue<EventObject> execute(Queue<EventObject> events) throws ActionException {
 		host = conf.getPersistencyHost();
 		itemsPerPage = conf.getItemsPerPages();
 		ieServiceUsername = conf.getIeServiceUsername();
 		ieServicePassword = conf.getIeServicePassword();
 
 		LOGGER.info("*** Injected Setting *****");
 		LOGGER.info("Persistence host " + host);
 		LOGGER.info("Items Per Page " + itemsPerPage);
 		LOGGER.info("ieServiceUsername: " + this.ieServiceUsername);
 		LOGGER.info("ieServicePassword: " + this.ieServicePassword);
 		LOGGER.info("**************************");
 
 		// return
 		final Queue<EventObject> ret = new LinkedList<EventObject>();
 
 		while (events.size() > 0) {
 			final EventObject ev;
 			try {
 				if ((ev = events.remove()) != null) {
 					if (LOGGER.isTraceEnabled()) {
 						LOGGER.trace("ConfigAction.execute(): working on incoming event: " + ev.getSource());
 					}
 
 					// perform basic checks and return the current config in the
 					// DB
 					LOGGER.info("Trying to connect to " + host + " and retrieve config data");
 
 					if (ieConfigDAO == null) {
 						throw new ActionException(this, "ieConfigDAO was null!");
 					}
 
 					Config config = checkConfiguration(ieConfigDAO.loadConfg(host, ieServiceUsername, ieServicePassword));
 					if (config != null) {
 						LOGGER.info("Config data correctly read");
 						geoserver = config.getGlobal().getGeoserver();
 						// create the figis temporary dir
 						LOGGER.info("Creating the temporary dir: " + TMP_DIR_NAME);
 
 						File tmpDir = Utilities.createTmpDir(TMP_DIR_NAME + "/" + System.nanoTime());
 						
 						LOGGER.info(TMP_DIR_NAME + " successfully created");
 
 						// update the status of the intersections on the basis
 						// of the new input
 						boolean areIntersectionsUpdated = executeIntersectionStatements(host, config, false, tmpDir.getAbsolutePath());
 						if (!areIntersectionsUpdated) {
 							LOGGER.error("Problems occurred during the execution, check the log to see the reason");
 						}
 						// delete the tmpDir after execution
 						LOGGER.trace("Deleting the temporary dir: " + TMP_DIR_NAME);
 						
 						Utilities.deleteDir(tmpDir);
 						
 						LOGGER.trace(TMP_DIR_NAME + " successfully deleted");
 					} else {
 						LOGGER.error("Cannot read the configuration " + config + ". Skip execution");
 					}
 				} else {
 					if (LOGGER.isErrorEnabled()) {
 						LOGGER.error("ConfigAction.execute(): Encountered a NULL event: SKIPPING...");
 					}
 					continue;
 				}
 			} catch (Exception ioe) {
 				final String message = "ConfigAction.execute(): Unable to produce the output: " + ioe.getLocalizedMessage();
 				throw new ActionException(this, message, ioe);
 				
 			} finally {
 				
 				// dispose all the shapefile resources
 				for(ShapefileDataStore st:shapeFileStores){
 					if(st!=null){
 						try{
 							st.dispose();
 						} catch (Exception e) {
 							LOGGER.trace(e.getLocalizedMessage(),e);
 						}
 					}
 				}
 			}
 		}
 
 		return ret;
 	}
 
 	/**
 	 * @param ieConfigDAO
 	 *            the ieConfigDAO to set
 	 */
 	public void setIeConfigDAO(IEConfigDAO ieConfigDAO) {
 		this.ieConfigDAO = ieConfigDAO;
 	}
 
 	public void setGeoserver(Geoserver geoserver) {
 		this.geoserver = geoserver;
 	}
 
 	public void setHost(String host) {
 		this.host = host;
 	}
 
 	public void setIeServiceUsername(String ieServiceUsername) {
 		this.ieServiceUsername = ieServiceUsername;
 	}
 
 	public void setIeServicePassword(String ieServicePassword) {
 		this.ieServicePassword = ieServicePassword;
 	}
 
 }
