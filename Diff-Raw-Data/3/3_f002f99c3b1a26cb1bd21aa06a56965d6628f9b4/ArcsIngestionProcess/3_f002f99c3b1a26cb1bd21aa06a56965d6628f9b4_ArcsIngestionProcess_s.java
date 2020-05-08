 /*
  *    GeoTools - The Open Source Java GIS Toolkit
  *    http://geotools.org
  *
  *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
  *
  *    This library is free software; you can redistribute it and/or
  *    modify it under the terms of the GNU Lesser General Public
  *    License as published by the Free Software Foundation;
  *    version 2.1 of the License.
  *
  *    This library is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *    Lesser General Public License for more details.
  */
 package it.geosolutions.geobatch.destination.ingestion;
 
 import it.geosolutions.geobatch.destination.common.InputObject;
 import it.geosolutions.geobatch.destination.common.OutputObject;
 import it.geosolutions.geobatch.destination.common.utils.FeatureLoaderUtils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataUtilities;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Transaction;
 import org.geotools.factory.Hints;
 import org.geotools.feature.FeatureIterator;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.jdbc.JDBCDataStore;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.filter.Filter;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.vividsolutions.jts.geom.Geometry;
 import it.geosolutions.geobatch.flow.event.ProgressListener;
 
 /**
  * @author "Mauro Bartolomeoli - mauro.bartolomeoli@geo-solutions.it"
  *
  */
 public class ArcsIngestionProcess extends InputObject {
 	
 	private final static Logger LOGGER = LoggerFactory.getLogger(ArcsIngestionProcess.class);
 		
 	private static Pattern typeNameParts = Pattern
 			.compile("^([A-Z]{2})_([A-Z]{1})_([A-Za-z]+)_([0-9]{8})$");
 	
 	private int partner;
 	private String codicePartner;
 	private String date;
 	
 	public static Properties aggregation = new Properties();
 	public static Properties bersaglio = new Properties();	
 		
 	private String gridTypeName = "siig_geo_grid";
 	
 	private String geoTypeName = "siig_geo_ln_arco_X";
 	private String geoTypeNamePl = "siig_geo_pl_arco_X";
 	
 	private String geoId = "id_geo_arco";
 	
 	private String byVehicleTypeName = "siig_r_tipovei_geoarcoX";
 	private String dissestoTypeName = "siig_r_arco_X_dissesto";
 	private String tipobersTypeName = "siig_r_arco_X_scen_tipobers";
 	private String sostanzaTypeName = "siig_t_sostanza";
 	private String sostanzaArcoTypeName = "siig_r_arco_X_sostanza";
 	
 	private static float PADDR_WORKAROUTD_VALUE = .5f;
 	
 	private static Map attributeMappings = null;		
 	
 	static {	
 		// load mappings from resources				
 		attributeMappings = (Map) readResourceFromXML("/roadarcs.xml");	
 		
 		InputStream aggregationStream = null;
                 InputStream bersaglioStream = null;
 		try {
 		        aggregationStream = ArcsIngestionProcess.class.getResourceAsStream("/aggregation.properties");
 		        bersaglioStream = ArcsIngestionProcess.class.getResourceAsStream("/bersaglio.properties");
 			aggregation.load(aggregationStream);
 			bersaglio.load(bersaglioStream);
 		} catch (IOException e) {
 			LOGGER.error("Unable to load configuration: "+e.getMessage(), e);
 		} finally{
 		    try {
                         if(bersaglioStream != null){
 		            bersaglioStream.close();
                         }
                     } catch (IOException e) {
                         LOGGER.error(e.getMessage(), e);
                     }
 		    try {
 		        if(aggregationStream != null){
                             aggregationStream.close();
 		        }
                     } catch (IOException e) {
                         LOGGER.error(e.getMessage(), e);
                     }
 		}
 	}
 	
 	/**
 	 * Initializes a VectorTarget handler for the given input feature.
 	 * 
 	 * @param inputTypeName
 	 */
 	public ArcsIngestionProcess(String inputTypeName,
 			ProgressListener listenerForwarder,
 			MetadataIngestionHandler metadataHandler, JDBCDataStore dataStore) {
 		super(inputTypeName, listenerForwarder, metadataHandler, dataStore);		
 	}
 	
 	/**
 	 * Parse input feature typeName and extract useful information from it. 
 	 */
 	protected boolean parseTypeName(String typeName) {
 		Matcher m = typeNameParts.matcher(typeName);
 		if(m.matches()) {
 			// partner alphanumerical abbreviation (from siig_t_partner)
 			codicePartner = m.group(1);
 			// partner numerical id (from siig_t_partner)
 			partner = Integer.parseInt(partners.get(codicePartner).toString());
 			// target macro type (bu or bnu)			
 			// file date identifier
 			date = m.group(4);			
 			
 			// TODO: add other validity checks
 			
 			return true;
 		}
 		return false;
 	}
 	
 	public int getPartner(){
 	    return partner;
 	}
 		
 	/**
 	 * @param geoTypeName2
 	 * @param aggregationLevel
 	 * @return
 	 */
 	private String getTypeName(String typeName, int aggregationLevel) {		
 		return typeName.replace("X", aggregationLevel+"");
 	}
 
 	/**
 	 * Imports the arcs feature from the original Feature into on of the SIIG
 	 * arcs tables (in staging).
 	 * 
 	 * @param datastoreParams
 	 * @param crs
 	 * @param aggregationLevel level to import (1, 2, 3)
 	 * @param onGrid aggregate on cells or not (for level 3)
 	 * @param dropInput drop input table after import
 	 * @param closePhase phase to close at the end of the import (A, B or C; null means no closing)
 	 * @throws IOException
 	 */
 	public void importArcs(CoordinateReferenceSystem crs, int aggregationLevel,
 			boolean onGrid, boolean dropInput, String closePhase)
 			throws IOException {
 		reset();
 		if(isValid()) {								
 			
 			
 			crs = checkCrs(crs);			
 			
 			int process = -1;
 			int trace = -1;
 			int errors = 0;
 			
 			
 			String processPhase = closePhase;
 			
 			try {												
 								
 				// create or retrieve metadata for ingestion
 				if(aggregationLevel == 1) {
 					// new process
 					process = createProcess();
 					// write log for the imported file
 					trace = logFile(process, NO_TARGET,
 							partner, codicePartner, date, false);
 				} else {
 					// existing process
 					MetadataIngestionHandler.Process importData = getProcessData();
 					process = importData.getId();
 					trace = importData.getMaxTrace();
 					errors = importData.getMaxError();
 				}	
 				int startErrors = errors;
 				
 				// setup input reader								
 				createInputReader(dataStore, null, onGrid ? gridTypeName : null);						
 				
 				Transaction transaction = new DefaultTransaction();
 				
 				// setup the MAIN geo output object
 				// The mainGeoObject is that one is used for compute also the other outputObjects
 				// For aggregation level 1 and 2 is that one related to table siig_geo_ln_arco_X but for aggregation 3 on grid is siig_geo_pl_arco_X 
 				String geoName = getTypeName((onGrid ? geoTypeNamePl : geoTypeName), aggregationLevel);
 				OutputObject mainGeoObject = new OutputObject(dataStore, transaction, geoName, geoId);
 				
 				// setup vehicle output object
 				String vehicleName = getTypeName(byVehicleTypeName, aggregationLevel);
 				OutputObject vehicleObject = new OutputObject(dataStore, transaction, vehicleName, "");
 								
 				// setup dissesto output object
 				String dissestoName = getTypeName(dissestoTypeName, aggregationLevel);
 				OutputObject dissestoObject = new OutputObject(dataStore, transaction, dissestoName, "");
 				
 				// setup CFF output object
 				String tipobersName = getTypeName(tipobersTypeName, aggregationLevel);
 				OutputObject tipobersObject = new OutputObject(dataStore, transaction, tipobersName, "");
 
 				// setup sostanza output object
 				String tiposostName = getTypeName(sostanzaArcoTypeName, aggregationLevel);
 				OutputObject tiposostObject = new OutputObject(dataStore, transaction, tiposostName, "");
                                 
 				// list of all the output objects
 				OutputObject[] outputObjects = new OutputObject[] {vehicleObject,
 						dissestoObject, tipobersObject, tiposostObject, mainGeoObject};
 								
 				try {
 					// remove previous data for the given partner
 					Filter removeFilter = filterFactory.equals(
 						filterFactory.property("fk_partner"), filterFactory.literal(partner)
 					);
 					if(aggregationLevel == 3 && !onGrid) {
 						// remove only geo data for ln_3
 						removeObjects(new OutputObject[] {mainGeoObject}, removeFilter);
 					} else {
 						removeObjects(outputObjects, removeFilter);
 					}
 					
 					transaction.commit();	
 				} catch (IOException e) {
 					LOGGER.error(e.getMessage(),e);
 					errors++;					
 					metadataHandler.logError(trace, errors, "Error removing old data", getError(e), 0);					
 					transaction.rollback();					
 					throw e;
 				} finally {
 					transaction.close();
 				}
 									
 				// calculates total objects to import				
 				int total = getImportCount();												
 				
 				if(onGrid) {
 					//aggregate arcs on grid and compute also the other tables with that aggregation
 					errors = aggregateArcsOnGrid(trace, dataStore, outputObjects, total, errors, startErrors, geoName, aggregationLevel);					
 				} else if(aggregationLevel == 1) {
 					// no aggregation
 					errors = importWithoutAggregation(trace, dataStore,
 							outputObjects, total, errors, geoName);
 				} else {
 					// aggregation on input field
 					errors = aggregateArcs(trace, dataStore, outputObjects,
 							total, errors, startErrors, geoName,
 							aggregationLevel, aggregationLevel == 3);
 				}
 				metadataHandler.updateLogFile(trace, total, errors, aggregationLevel == 1);
 			} catch (IOException e) {
 				LOGGER.error(e.getMessage(),e);
 				errors++;				
 				metadataHandler.logError(trace, errors, "Error importing data", getError(e), 0);				
 				throw e;
 			} finally {
 				if(dropInput) {
 					dropInputFeature(dataStore);
 				}
 				
 				if(process != -1 && processPhase != null) {
 					// close current process phase
 					metadataHandler.closeProcessPhase(process, processPhase);
 				}				
 							
 			}
 		}
 	}
 
 
 
 	/**
 	 * @param trace
 	 * @param dataStore
 	 * @param outputObjects
 	 * @param intValue
 	 * @param total
 	 * @param errors
 	 * @param geoName
 	 * @param aggregationLevel
 	 * @return
 	 * @throws IOException 
 	 */
 	private int aggregateArcsOnGrid(int trace, DataStore dataStore,
 			OutputObject[] outputObjects, int total, int errors, int startErrors,
 			String outputName, int aggregationLevel) throws IOException {
 		try {
 			String inputGeometryName = getInputGeometryName(dataStore);
 			
 			SimpleFeature gridFeature = null;			
 			while( (gridFeature = readInput()) != null) {
 				
 				int id = nextId();							
 				int idTematico = ((BigDecimal)gridFeature.getAttribute("gid")).intValue();
 				
 				Geometry cell = (Geometry)gridFeature.getDefaultGeometry();
 				
 				FeatureSource<SimpleFeatureType, SimpleFeature> reader = createInputReader(
 						dataStore, null, null);				
 				
 				FeatureIterator<SimpleFeature> iterator = reader.getFeatures(filterFactory.intersects(
 						filterFactory.property(inputGeometryName), 
 						filterFactory.literal(cell)
 				)).features();
 				
 				try  {
 					errors = aggregateStep(trace, dataStore, outputObjects, total,
 							errors, startErrors, outputName, id, idTematico, iterator, cell, false);
 				} finally {
 					iterator.close();
 				}
 				
 																				
 			}
 			importFinished(total, errors - startErrors, "Data imported in "+ outputName);
 			
 		} finally {
 			closeInputReader();
 		}				
 																				
 		
 		return errors;
 	}
 
 	/**
 	 * @throws IOException 
 	 * 
 	 */
 	private int aggregateArcs(int trace, DataStore dataStore,
 			OutputObject[] outputObjects, int total, int errors, int startErrors,
 			String outputName, int aggregationLevel, boolean computeOnlyGeoFeature) throws IOException {
 		String aggregationAttribute = aggregation.getProperty(aggregationLevel + "");
 		// get unique aggregation values		
 		Set<Number> aggregationValues = getAggregationValues(aggregationAttribute);		
 		
 		for(Number aggregationValue : aggregationValues) {						
 			
 			int id = nextId();			
 			int idTematico = aggregationValue.intValue();						
 			
 			setInputFilter(filterFactory.equals(
 				filterFactory.property(aggregationAttribute),
 				filterFactory.literal(aggregationValue)
 			));
 			try {
 				errors = aggregateStep(trace, dataStore, outputObjects, total,
 						errors, startErrors, outputName, id, idTematico, null, null, computeOnlyGeoFeature);
 			} finally {
 				closeInputReader();
 			}	
 			
 		}
 		importFinished(total, errors - startErrors, "Data imported in "+ outputName);
 		return errors;		
 	}
 
 	/**
 	 * Execute an aggregation step.
 	 * Aggregates all feature from iterator into a single output record.
 	 * 
 	 * @param trace
 	 * @param dataStore
 	 * @param outputObjects output objects descriptors 
 	 * @param total total input objects
 	 * @param errors current errors count
 	 * @param startErrors initial error values (errors can sum up in various phases)
 	 * @param outputName main output table name
 	 * @param id main output table id value
 	 * @param idTematico original object id value
 	 * @param iterator list of objects to aggregate
 	 * @param aggregateGeo optional alternative aggregate geo (for grid cells)
 	 * @param computeOnlyGeoFeature write only main output table
 	 * @return
 	 * @throws IOException
 	 */
 	private int aggregateStep(int trace, DataStore dataStore,
 			OutputObject[] outputObjects, int total, int errors,
 			int startErrors, String outputName, int id, int idTematico,
 			FeatureIterator<SimpleFeature> iterator, Geometry aggregateGeo,
 			boolean computeOnlyGeoFeature) throws IOException {
 		
 		SimpleFeature inputFeature;
 		Geometry geo = null;
 		int lunghezza = 0;
 		int incidenti = 0;
 		int corsie = 0;
 		int[] tgm = new int[] {0, 0};
 		int[] velocita = new int[] {0, 0};
 		double[] cff = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		double[] padr = new double[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		ElementsCounter flgTgmCounter = new ElementsCounter();
 		ElementsCounter flgVelocCounter = new ElementsCounter();
 		ElementsCounter flgCorsieCounter = new ElementsCounter();
 		ElementsCounter flgIncidentiCounter = new ElementsCounter();
 		Set<Integer> pterr = new HashSet<Integer>();
 		int idOrigin = -1;
 		while( (inputFeature = readInput(iterator)) != null) {	
 			try {
 				if(aggregateGeo == null) {								
 					if(geo == null) {
 						geo = (Geometry)inputFeature.getDefaultGeometry();
 					} else if(inputFeature.getDefaultGeometry() != null){
 						geo = geo.union((Geometry)inputFeature.getDefaultGeometry());
 					}
 				} else {
 					geo = aggregateGeo;
 				}
 				idOrigin = (idOrigin == -1)?((Number)getMapping(inputFeature, attributeMappings, "id_origine")).intValue():idOrigin;
  
 				Number currentLunghezza = (Number) getMapping(inputFeature,
 						attributeMappings, "lunghezza");
 				if (currentLunghezza != null) {
 					lunghezza += currentLunghezza.intValue();
 				}
 				Number currentIncidenti = (Number) getMapping(inputFeature,
 						attributeMappings, "nr_incidenti");
 				if (currentIncidenti != null) {
 					incidenti += Math.max(0, currentIncidenti.intValue());
 				}
 				Number currentCorsie = (Number) getMapping(inputFeature,
 						attributeMappings, "nr_corsie");
 				if (currentCorsie != null && currentLunghezza != null) {
 					corsie += currentCorsie.intValue()
 							* currentLunghezza.intValue();
 				}
 				
 				String currentFlgCorsie = (String)getMapping(inputFeature,
 						attributeMappings, "flg_nr_corsie");
 													 
 				String currentFlgIncidenti = (String)getMapping(inputFeature,
 						attributeMappings, "flg_nr_incidenti");
 				flgCorsieCounter.addElement(currentFlgCorsie);
 				flgIncidentiCounter.addElement(currentFlgIncidenti);
 				if(!computeOnlyGeoFeature){		
 					// by vehicle
 					int[] tgms = extractMultipleValues(inputFeature, "TGM");
 					int[] velocitas = extractMultipleValues(inputFeature,
 							"VELOCITA");
 					for (int i = 0; i < tgms.length; i++) {
 						if (currentLunghezza != null) {
 							tgm[i] += tgms[i] * currentLunghezza.intValue();
 							velocita[i] += velocitas[i]
 									* currentLunghezza.intValue();
 						}
 					}
 					String currentFlgTGM = (String)getMapping(inputFeature,
 							attributeMappings, "flg_densita_veicolare");
 														 
 					String currentFlgVeloc = (String)getMapping(inputFeature,
 							attributeMappings, "flg_velocita");
 					flgTgmCounter.addElement(currentFlgTGM);
 					flgVelocCounter.addElement(currentFlgVeloc);
 
 					// dissesto
 					String[] pterrs = inputFeature.getAttribute("PTERR") == null ? new String[0]
 							: inputFeature.getAttribute("PTERR").toString()
 									.split("\\|");
 					for (int j = 0; j < pterrs.length; j++) {
 						try {
 							int dissesto = Integer.parseInt(pterrs[j]);
 							pterr.add(dissesto);
 						} catch (NumberFormatException e) {
 
 						}
 					}
 
 					// cff
 					double[] cffs = extractMultipleValuesDouble(inputFeature,
 							"CFF", cff.length);
 					for (int i = 0; i < cff.length; i++) {
 						cff[i] += cffs[i] * currentLunghezza.intValue();
 					}
 					// padr
 					double[] padrs = extractMultipleValuesDouble(inputFeature,
 							"PADR", padr.length);
 					for (int i = 0; i < padrs.length; i++) {
 						padr[i] += padrs[i] * currentLunghezza.intValue();
 					}
 				}
 				
 			} catch(Exception e) {
 				LOGGER.error(e.getMessage(),e);
 				errors++;
 				metadataHandler.logError(trace, errors,
 						"Error writing output feature", getError(e),
 						idTematico);
 			}
 		}
 		if(geo != null) {
 			Transaction rowTransaction = new DefaultTransaction();
 			setTransaction(outputObjects, rowTransaction);			
 			
 			try {							
 				addAggregateGeoFeature(outputObjects[4], id, idTematico, geo,
 						lunghezza, corsie, incidenti, inputFeature, idOrigin, 
 						flgCorsieCounter.getMax(), flgIncidentiCounter.getMax());						
 				if(!computeOnlyGeoFeature){
 					addAggregateVehicleFeature(outputObjects[0], id, lunghezza,
 							tgm, velocita, flgTgmCounter.getMax(),
 							flgVelocCounter.getMax(), inputFeature);
 					addAggregateDissestoFeature(outputObjects[1], id,
 							lunghezza, pterr, inputFeature);
 					addAggregateCFFFeature(outputObjects[2], id, lunghezza, cff,
 							inputFeature);
 					addAggregatePADRFeature(outputObjects[3], id, lunghezza,
 							padr, inputFeature);
 					
 				}
 				rowTransaction.commit();
 				
 				updateImportProgress(total, errors - startErrors, "Importing data in " + outputName);
 			} catch(Exception e) {
 				LOGGER.error(e.getMessage(),e);
 				errors++;								
 				rowTransaction.rollback();
 				metadataHandler.logError(trace, errors,
 						"Error writing output feature", getError(e),
 						idTematico);
 			} finally {	
 				rowTransaction.close();							
 			}
 		}
 		return errors;
 	}
 
 	private void addAggregatePADRFeature(OutputObject padrObject, int id,
 			int lunghezza, double padr[], SimpleFeature inputFeature)
 			throws IOException {
 		
 		SimpleFeatureBuilder featureBuilder = padrObject.getBuilder();
 		for (int count = 0; count < padr.length; count++) {
 			try {
 				double padrElement = padr[count];
 				featureBuilder.reset();
 				// compiles the attributes from target and read feature data,
 				// using mappings
 				// to match input attributes with output ones
 				for (AttributeDescriptor attr : padrObject.getSchema()
 						.getAttributeDescriptors()) {
 					if (attr.getLocalName().equals("padr")) {
 						// compute the aritmetic average
 						featureBuilder.add(padrElement / lunghezza);
 					} else if (attr.getLocalName().equals("fk_partner")) {
 						featureBuilder.add(partner + "");
 					} else {
 						featureBuilder.add(null);
 					}
 				}
 
 				String idSostanza = (count+1)+"";
 				String featureid = id + "." + idSostanza;
 				SimpleFeature feature = featureBuilder.buildFeature(featureid);
 				feature.getUserData().put(Hints.USE_PROVIDED_FID, true);
 
 				padrObject.getWriter().addFeatures(
 						DataUtilities.collection(feature));
 			} catch (NumberFormatException e) {
 
 			}
 		}
 	}
 	
 	/**
 	 * @param outputObject
 	 * @param id
 	 * @param lunghezza
 	 * @param pterr
 	 * @param inputFeature
 	 * @throws IOException 
 	 */
 	private void addAggregateDissestoFeature(OutputObject outputObject, int id,
 			int lunghezza, Set<Integer> pterr, SimpleFeature inputFeature) throws IOException {
 		SimpleFeatureBuilder dissestoFeatureBuilder = outputObject.getBuilder();
 		
 		for(int dissesto : pterr) {			
 			// compiles the attributes from target and read feature data, using mappings
 			// to match input attributes with output ones
 			for(AttributeDescriptor attr : outputObject.getSchema().getAttributeDescriptors()) {
 				if(attr.getLocalName().equals(geoId)) {
 					dissestoFeatureBuilder.add(id);
 				} else if(attr.getLocalName().equals("id_dissesto")) {
 					dissestoFeatureBuilder.add(dissesto);
 				} else if(attr.getLocalName().equals("fk_partner")) {
 					dissestoFeatureBuilder.add(partner+"");
 				} else {
 					dissestoFeatureBuilder.add(null);
 				}
 			}
 			String featureid = dissesto + "." + id;
 			SimpleFeature feature = dissestoFeatureBuilder.buildFeature(featureid);
 			feature.getUserData().put(Hints.USE_PROVIDED_FID, true);			
 			
 			outputObject.getWriter().addFeatures(DataUtilities
 					.collection(feature));
 		}
 	}
 
 	/**
 	 * @param outputObject
 	 * @param id
 	 * @param idTematico
 	 * @param tgm
 	 * @param velocita
 	 * @param inputFeature
 	 * @throws IOException 
 	 */
 	private void addAggregateVehicleFeature(OutputObject outputObject, int id,
 			int lunghezza, int[] tgm, int[] velocita, String flgTgm, String flgVeloc,
 			SimpleFeature inputFeature) throws IOException {
 		SimpleFeatureBuilder byvehicleFeatureBuilder = outputObject.getBuilder();
 
 		for(int type = 0; type <= 1;type++) {
 			for(AttributeDescriptor attr : outputObject.getSchema().getAttributeDescriptors()) {
 				if(attr.getLocalName().equals(geoId)) {
 					byvehicleFeatureBuilder.add(id);
 				} else if(attr.getLocalName().equals("densita_veicolare")) {
 					if(lunghezza == 0) {
 						byvehicleFeatureBuilder.add(0);
 					} else {
 						byvehicleFeatureBuilder.add(tgm[type] / lunghezza);
 					}
 				} else if(attr.getLocalName().equals("id_tipo_veicolo")) {
 					byvehicleFeatureBuilder.add(type + 1);
 				} else if(attr.getLocalName().equals("flg_velocita")) {
 				        byvehicleFeatureBuilder.add(flgVeloc);
                                 } else if(attr.getLocalName().equals("flg_densita_veicolare")) {
                                         byvehicleFeatureBuilder.add(flgTgm);    
                                 }
 				else if(attr.getLocalName().equals("velocita_media")) {
 					if(lunghezza == 0) {
 						byvehicleFeatureBuilder.add(0);
 					} else {
 						byvehicleFeatureBuilder.add(velocita[type] / lunghezza);
 					}
 				} else if(attr.getLocalName().equals("fk_partner")) {
 					byvehicleFeatureBuilder.add(partner+"");
 				} else {
 					byvehicleFeatureBuilder.add(null);
 				}
 			}
 			String featureid = (type + 1) + "." + id;
 			SimpleFeature feature = byvehicleFeatureBuilder.buildFeature(featureid);
 			feature.getUserData().put(Hints.USE_PROVIDED_FID, true);			
 			
 			outputObject.getWriter().addFeatures(DataUtilities
 					.collection(feature));
 		}
 	}
 
 	/**
 	 * @param outputObject
 	 * @param id
 	 * @param geo
 	 * @param lunghezza
 	 * @param corsie
 	 * @param inputFeature
 	 * @throws IOException 
 	 */
 	private void addAggregateGeoFeature(OutputObject outputObject, int id, int idTematico,
 			Geometry geo, int lunghezza, int corsie, int incidenti, SimpleFeature inputFeature, 
 			int idOrigin, String flgCorsie, String flgIncidenti) throws IOException {
 		SimpleFeatureBuilder geoFeatureBuilder = outputObject.getBuilder();
 		for(AttributeDescriptor attr : outputObject.getSchema().getAttributeDescriptors()) {
 			if(attr.getLocalName().equals(geoId)) {
 				geoFeatureBuilder.add(id);
 			} else if(attr.getLocalName().equals("fk_partner")) {
 				geoFeatureBuilder.add(partner+"");
 			} else if(attr.getLocalName().equals("id_tematico_shape")) {
 				geoFeatureBuilder.add(idTematico+"");
 			} else if(attr.getLocalName().equals("geometria")) {
 				geoFeatureBuilder.add(geo);
 			} else if(attr.getLocalName().equals("lunghezza")) {
 				geoFeatureBuilder.add(lunghezza);
 			} else if(attr.getLocalName().equals("nr_incidenti")) {
 				geoFeatureBuilder.add(incidenti);
 			} else if(attr.getLocalName().equals("nr_incidenti_elab")) {
 				geoFeatureBuilder.add(incidenti);
 			} else if(attr.getLocalName().equals("flg_nr_corsie")) {
 				geoFeatureBuilder.add(flgCorsie);
 			} else if(attr.getLocalName().equals("flg_nr_incidenti")) {
 				geoFeatureBuilder.add(flgIncidenti);
 			} else if(attr.getLocalName().equals("id_origine")) {
 			        geoFeatureBuilder.add(idOrigin);
 			} else if(attr.getLocalName().equals("nr_corsie")) {
 				if(lunghezza == 0) {
 					geoFeatureBuilder.add(0);
 				} else {
 					geoFeatureBuilder.add(corsie / lunghezza);
 				}
 			} else {
 				geoFeatureBuilder.add(null);
 			}
 		}
 		SimpleFeature geoFeature = geoFeatureBuilder.buildFeature("" + id);
 		geoFeature.getUserData().put(Hints.USE_PROVIDED_FID, true);
 		outputObject.getWriter().addFeatures(DataUtilities
 				.collection(geoFeature));
 	}
 	
 	private void addAggregateCFFFeature(OutputObject cffObject,
                 int id, int lunghezza, double cff[], SimpleFeature inputFeature) throws IOException {
             
             SimpleFeatureBuilder featureBuilder = cffObject.getBuilder();
             for(int count=0; count < cff.length; count++) {
                     try {
                             double cffElement = cff[count];
                             featureBuilder.reset();
                             // compiles the attributes from target and read feature data, using mappings
                             // to match input attributes with output ones
                             for(AttributeDescriptor attr : cffObject.getSchema().getAttributeDescriptors()) {
                                     if(attr.getLocalName().equals("cff")) {
                                             // compute the aritmetic average
                                             featureBuilder.add(cffElement/lunghezza);
                                     }else if(attr.getLocalName().equals("fk_partner")) {
                                             featureBuilder.add(partner+"");
                                     } else {
                                             featureBuilder.add(null);
                                     }
                             }
                             String idBersaglio = bersaglio.getProperty(Integer.toString(count+1));
                             String featureid = id + "." + idBersaglio;
                             SimpleFeature feature = featureBuilder.buildFeature(featureid);
                             feature.getUserData().put(Hints.USE_PROVIDED_FID, true);                        
                             
                             cffObject.getWriter().addFeatures(DataUtilities
                                             .collection(feature));
                     } catch(NumberFormatException e) {
                             
                     }
             }
         }
 	
 
 	/**
 	 * @param trace
 	 * @param dataStore
 	 * @param outputObjects
 	 * @param total
 	 * @param errors
 	 * @param outputName
 	 * @param inputFeature
 	 * @param id
 	 * @param idTematico
 	 * @return
 	 * @throws IOException
 	 */
 	private int writeOutputObjects(int trace, DataStore dataStore,
 			OutputObject[] outputObjects, int total, int errors,
 			String outputName, SimpleFeature inputFeature, int id,
 			int idTematico) throws IOException {
 		Transaction rowTransaction = new DefaultTransaction();
 		setTransaction(outputObjects, rowTransaction);
 		
 		try {							
 			addGeoFeature(outputObjects[4], id, inputFeature);						
 			addVehicleFeature(outputObjects[0], id, inputFeature);
 			addDissestoFeature(outputObjects[1], id, inputFeature);
 			addCFFFeature(outputObjects[2], id, inputFeature);
 			addSostanzaFeature(outputObjects[3], id, inputFeature, dataStore);
 			
 			rowTransaction.commit();
 			
 			updateImportProgress(total, errors, "Importing data in " + outputName);
 		} catch(Exception e) {
 			LOGGER.error(e.getMessage(),e);
 			errors++;			
 			rowTransaction.rollback();
 			metadataHandler.logError(trace, errors,
 					"Error writing output feature", getError(e),
 					idTematico);
 		} finally {				
 			rowTransaction.close();							
 		}
 		return errors;
 	}
 	
 
 	/**
 	 * @throws IOException 
 	 * 
 	 */
 	private int importWithoutAggregation(int trace, DataStore dataStore,
 			OutputObject[] outputObjects, int total, int errors,
 			String outputName) throws IOException {
 		try {
 			SimpleFeature inputFeature = null;
 			while( (inputFeature = readInput()) != null) {
 								
 				int id = nextId();							
 				int idTematico = getIdTematico(inputFeature, attributeMappings);
 				
 				errors = writeOutputObjects(trace, dataStore, outputObjects,
 						total, errors, outputName, inputFeature, id, idTematico);						
 			}
 			importFinished(total, errors, "Data imported in "+ outputName);
 			
 		} finally {
 			closeInputReader();
 		}				
 																				
 		
 		return errors;
 	}
 
 	/**
 	 * Adds arc - vehicletype data feature.
 	 * 
 	 */
 	private void addVehicleFeature(OutputObject vehicleObject,
 			int id, SimpleFeature inputFeature)
 			throws IOException {
 		
 		SimpleFeatureBuilder featureBuilder = vehicleObject.getBuilder();
 		
 		int[] tgm = extractMultipleValues(inputFeature, "TGM");
 		int[] velocita = extractMultipleValues(inputFeature, "VELOCITA");
 		
 		
 		for(int type = 0; type <= 1; type++) {
 			featureBuilder.reset();
 			// compiles the attributes from target and read feature data, using mappings
 			// to match input attributes with output ones
 			for(AttributeDescriptor attr : vehicleObject.getSchema().getAttributeDescriptors()) {
 				if(attr.getLocalName().equals(geoId)) {
 					featureBuilder.add(id);
 				} else if(attr.getLocalName().equals("densita_veicolare")) {
 					featureBuilder.add(tgm[type]);
 				} else if(attr.getLocalName().equals("id_tipo_veicolo")) {
 					featureBuilder.add(type + 1);
 				} else if(attr.getLocalName().equals("velocita_media")) {
 					featureBuilder.add(velocita[type]);
 				} else if(attr.getLocalName().equals("fk_partner")) {
 					featureBuilder.add(partner+"");
 				}else if(attributeMappings.containsKey(attr.getLocalName())) {
 	                                featureBuilder.add(getMapping(inputFeature,attributeMappings, attr.getLocalName()));
 				} else {
 					featureBuilder.add(null);
 				}
 			}
 			String featureid = (type + 1) + "." + id;
 			SimpleFeature feature = featureBuilder.buildFeature(featureid);
 			feature.getUserData().put(Hints.USE_PROVIDED_FID, true);			
 			
 			vehicleObject.getWriter().addFeatures(DataUtilities
 					.collection(feature));
 		}		
 	}
 
 	/**
 	 * @param inputFeature
 	 * @return
 	 */
 	private int[] extractMultipleValues(SimpleFeature inputFeature, String attributeName) {
 		if(inputFeature.getAttribute(attributeName) == null) {
     		return new int[0];
     	}
 		String[] svalues = inputFeature.getAttribute(attributeName).toString().split("\\|");				
 		int[] values = new int[] {0,0};		
 		
 		for(int count=0; count < svalues.length; count++) {
 			try {
 				values[count] = Integer.parseInt(svalues[count]);
 			} catch(NumberFormatException e) {
 				
 			}
 		}
 		return values;
 	}
 	
 	/**
          * @param inputFeature
          * @return
          */
         private double[] extractMultipleValuesDouble(SimpleFeature inputFeature, String attributeName, int valueNumber) {
         	if(inputFeature.getAttribute(attributeName) == null) {
         		return new double[valueNumber];
         	}
             String[] svalues = inputFeature.getAttribute(attributeName).toString().split("\\|");                            
             double[] values = new double[valueNumber];
             
             for(int count=0; count < svalues.length; count++) {
                     try {
                             String el = svalues[count].replace(",", ".");
                             values[count] = Double.parseDouble(el);
                     } catch(NumberFormatException e) {
                             
                     }
             }
             return values;
         }
 	
 	/**
 	 * Adds arc - dissesto data feature.
 	 * 
 	 */
 	private void addDissestoFeature(OutputObject dissestoObject,
 			int id, SimpleFeature inputFeature) throws IOException {
 		
 		SimpleFeatureBuilder featureBuilder = dissestoObject.getBuilder();
 		
 		String[] pterrs = inputFeature.getAttribute("PTERR") == null ? null : inputFeature.getAttribute("PTERR").toString().split("\\|");					
 		
 		for(int count=0; count < pterrs.length; count++) {
 			try {
 				int dissesto = Integer.parseInt(pterrs[count]);
 				featureBuilder.reset();
 				// compiles the attributes from target and read feature data, using mappings
 				// to match input attributes with output ones
 				for(AttributeDescriptor attr : dissestoObject.getSchema().getAttributeDescriptors()) {
 					if(attr.getLocalName().equals(geoId)) {
 						featureBuilder.add(id);
 					} else if(attr.getLocalName().equals("id_dissesto")) {
 						featureBuilder.add(dissesto);
 					} else if(attr.getLocalName().equals("fk_partner")) {
 						featureBuilder.add(partner+"");
 					} else {
 						featureBuilder.add(null);
 					}
 				}
 				String featureid = id + "." + dissesto;
 				SimpleFeature feature = featureBuilder.buildFeature(featureid);
 				feature.getUserData().put(Hints.USE_PROVIDED_FID, true);			
 				
 				dissestoObject.getWriter().addFeatures(DataUtilities
 						.collection(feature));
 			} catch(NumberFormatException e) {
 				
 			}
 		}
 				
 	}
 	
 	private void addCFFFeature(OutputObject cffObject,
                 int id, SimpleFeature inputFeature) throws IOException {
 	    
 	    SimpleFeatureBuilder featureBuilder = cffObject.getBuilder();
 	    Object cffAttribute = inputFeature.getAttribute("CFF");	    
             String[] cffAttributeSplitted =  cffAttribute == null ? new String[] {} : cffAttribute.toString().split("\\|");                                       
             
             for(int count=0; count < cffAttributeSplitted.length; count++) {
                     try {
                             String el = cffAttributeSplitted[count].replace(",", ".");
                             double cffElement = Double.parseDouble(el);
                             featureBuilder.reset();
                             
                             String idBersaglio = bersaglio.getProperty(Integer.toString(count+1));
                             
                             // compiles the attributes from target and read feature data, using mappings
                             // to match input attributes with output ones
                             for(AttributeDescriptor attr : cffObject.getSchema().getAttributeDescriptors()) {
                                     if(attr.getLocalName().equals(geoId)) {
                                             featureBuilder.add(id);
                                     } else if(attr.getLocalName().equals("cff")) {
                                             featureBuilder.add(cffElement);
                                     } else if(attr.getLocalName().equals("id_bersaglio")) {
                                             featureBuilder.add(idBersaglio);
                                     } else if(attr.getLocalName().equals("fk_partner")) {
                                             featureBuilder.add(partner+"");
                                     } else {
                                             featureBuilder.add(null);
                                     }
                             }
 //                            String fid2 = el.replaceAll("\\,", "");
 //                            fid2 = el.replaceAll("\\.", "");
                             String featureid = id + "." + idBersaglio;
                             SimpleFeature feature = featureBuilder.buildFeature(featureid);
                             feature.getUserData().put(Hints.USE_PROVIDED_FID, true);                        
                             
                             cffObject.getWriter().addFeatures(DataUtilities
                                             .collection(feature));
                     } catch(NumberFormatException e) {
                             
                     }
             }
 	}
 	
 	private void addSostanzaFeature(OutputObject sostanzaObject, int id,
 			SimpleFeature inputFeature,
 			DataStore datastore) throws IOException {
 
 		SimpleFeatureBuilder featureBuilder = sostanzaObject.getBuilder();
 		Object padrAttribute = inputFeature.getAttribute("PADR");
 		String[] padrAttributeSplitted = padrAttribute == null ? new String[] {}
 				: padrAttribute.toString().split("\\|");
 
 		for (int count = 0; count < padrAttributeSplitted.length; count++) {
 			try {
 				String el = padrAttributeSplitted[count].replace(",", ".");
 				double padrElement = Double.parseDouble(el);
 				featureBuilder.reset();
 				String id_sostanza = (count+1)+"";
 				// for (String id_sostanza : sostanze) {
 				for (AttributeDescriptor attr : sostanzaObject.getSchema()
 						.getAttributeDescriptors()) {
 					if (attr.getLocalName().equals(geoId)) {
 						featureBuilder.add(id);
 					} else if (attr.getLocalName().equals("id_sostanza")) {
 						featureBuilder.add(id_sostanza);
 					} else if (attr.getLocalName().equals("padr")) {
 						featureBuilder.add(padrElement);
 					} else if (attr.getLocalName().equals("fk_partner")) {
 						featureBuilder.add(partner + "");
 					} else {
 						featureBuilder.add(null);
 					}
 				}
 				String featureid = id + "." + id_sostanza;
 				SimpleFeature feature = featureBuilder.buildFeature(featureid);
 				feature.getUserData().put(Hints.USE_PROVIDED_FID, true);
 
 				sostanzaObject.getWriter().addFeatures(
 						DataUtilities.collection(feature));
 				// }
 			} catch (NumberFormatException e) {
 
 			}
 		}
 	}
 	
 	
 	
 	/**
 	 * Adds a new geo arc feature.
 	 * 
 	 * @param geoSchema
 	 * @param geoFeatureBuilder
 	 * @param geoFeatureWriter
 	 * @param inputFeature
 	 * @param id
 	 * @throws IOException
 	 */
 	private void addGeoFeature(OutputObject geoObject,
 			int id,  SimpleFeature inputFeature) throws IOException {				
 		SimpleFeatureBuilder geoFeatureBuilder = geoObject.getBuilder();
 		// compiles the attributes from target and read feature data
 		for(AttributeDescriptor attr : geoObject.getSchema().getAttributeDescriptors()) {
 			if(attr.getLocalName().equals(geoId)) {
 				geoFeatureBuilder.add(id);
 			} else if(attr.getLocalName().equals("fk_partner")) {
 				geoFeatureBuilder.add(partner+"");
 			} else if(attr.getLocalName().equals("geometria")) {
 				geoFeatureBuilder.add(inputFeature.getDefaultGeometry());
 			} else if(attributeMappings.containsKey(attr.getLocalName())) {
 				geoFeatureBuilder.add(getMapping(inputFeature,attributeMappings, attr.getLocalName()));
 			} else {
 				geoFeatureBuilder.add(null);
 			}
 						
 		}
 		
 		SimpleFeature geoFeature = geoFeatureBuilder.buildFeature("" + id);		
 		geoFeature.getUserData().put(Hints.USE_PROVIDED_FID, true);
 		geoObject.getWriter().addFeatures(DataUtilities
 				.collection(geoFeature));
 	}
 	
 	 
 	
 }
