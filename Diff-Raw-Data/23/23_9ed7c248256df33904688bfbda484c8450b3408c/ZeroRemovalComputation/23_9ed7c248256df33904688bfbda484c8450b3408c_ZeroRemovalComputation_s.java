 /*
  *  Copyright (C) 2007-2012 GeoSolutions S.A.S.
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
 package it.geosolutions.geobatch.destination.zeroremoval;
 
 import it.geosolutions.geobatch.destination.common.InputObject;
 import it.geosolutions.geobatch.destination.common.OutputObject;
 import it.geosolutions.geobatch.destination.ingestion.MetadataIngestionHandler;
 import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.FeatureStore;
 import org.geotools.data.Transaction;
 import org.geotools.jdbc.JDBCDataStore;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.filter.Filter;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class implements the zero removal computation.
  * It uses as source and target the same table, one of the arc geometry tables (siig_geo_ln_arco_X) 
  * It is responsible for compute the NR_INCIDENTI_ELAB field starting from the values taken from NR_INCIDENTI
  * The process entry point is the method removeZeros that is called by the groovy script.
  * 
  * Due to some problems that the total risk formula has to manage arcs with a number of incident equals to 0
  * we want to calculate the field NR_INCIDENTI_ELAB from NR_INCIDENTI "spreading" the records with NR_INCIDENTI > 0 on those that have that value = 0
  * The total risk formula will use NR_INCIDENTI_ELAB instead of NR_INCIDENTI
  * 
  * The algorithm is this:
  * 
  *      given a road
  *              foreach arcs in road
  *                      if NR_INCIDENTI = 0
  *                              NR_INCIDENTI_ELAB = NR_INCIDENTI + (kInc * weightedAverage)
  *                      if NR_INCIDENTI > 0
  *                              NR_INCIDENTI_ELAB = NR_INCIDENTI - (kInc * weightedAverage * n / m)
  *      
  *       where:
  *                         kInc = a global correction factor (default is KINCR_DEFAULT_VALUE)
  *              weightedAverage = average of incidents weighted on arcs length
  *                            n = sum of the length of arcs with NR_INCIDENTI = 0
  *                            m = sum of the length of arcs with NR_INCIDENTI > 0                              
  * 
  * Due to lack on algorithm formula may happens that the incidents vale of non zero arcs become negative.
  * To avoid this the process is iterated with kInc decremented by KINCR_DECR_STEP at each iteration until negative values are absent.
  * 
  * @author DamianoG
  * 
  */
 public class ZeroRemovalComputation extends InputObject {
 
 	private final static Logger LOGGER = LoggerFactory.getLogger(ZeroRemovalComputation.class);
 
 	private static Pattern TYPE_NAME_PARTS = Pattern
 			.compile("^([A-Z]{2})_([A-Z]{1})_([A-Za-z]+)_([0-9]{8})(_ORIG)?$");
 
 	public static String GEO_TYPE_NAME = "siig_geo_ln_arco_X";
 	//private static final String NR_INCIDENTI = "nr_incidenti";
 	private static final String LUNGHEZZA = "lunghezza";
 	private static final String GEOID = "id_geo_arco";
 	private static final String ID_ORIGIN = "id_origine";
 	private static final String PARTNER_FIELD = "fk_partner";
 	private static final double KINCR_DEFAULT_VALUE = .2;
 	private DecimalFormat df = new DecimalFormat("0.00");
     String codicePartner;
     int partner;
 	
 	/**
 	 * A value that multiply all weightedAverage in order to avoid negative results
 	 */
 	private double kInc;
 
 	/**
 	 * 
 	 * @param kIncr
 	 * @param inputTypeName
 	 * @param listenerForwarder
 	 */
 	public ZeroRemovalComputation(double kIncr, String inputTypeName,
 			ProgressListenerForwarder listenerForwarder,
 			MetadataIngestionHandler metadataHandler, DataStore dataStore) {
 		super(inputTypeName, listenerForwarder, metadataHandler, dataStore);
 		this.kInc = kIncr;
 	}
 	
 	@Override
 	protected String getInputTypeName(String inputTypeName) {
 		return inputTypeName.replace("_ORIG", "");
 	}
 
 	/**
 	 * This constructor set the default value KINCR_DEFAULT_VALUE to kIncr field 
 	 * 
 	 * @param inputTypeName
 	 * @param listenerForwarder
 	 */
 	public ZeroRemovalComputation(String inputTypeName,
 			ProgressListenerForwarder listenerForwarder,
 			MetadataIngestionHandler metadataHandler, DataStore dataStore) {
 		super(inputTypeName, listenerForwarder, metadataHandler, dataStore);
 		this.kInc = KINCR_DEFAULT_VALUE;
 	}
 
 	@Override
 	protected boolean parseTypeName(String typeName) {
 		Matcher m = TYPE_NAME_PARTS.matcher(typeName);
 		if(m.matches()) {
 			// partner alphanumerical abbreviation (from siig_t_partner)
 			codicePartner = m.group(1);
 			// partner numerical id (from siig_t_partner)
 			partner = Integer.parseInt(partners.get(codicePartner).toString());			
 			
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * @param typeName
 	 * @param aggregationLevel
 	 * @return
 	 */
 	private String getTypeName(String typeName, int aggregationLevel) {
 		return typeName.replace("X", aggregationLevel + "");
 	}
 
 	/**
 	 * 
 	 * This method implements the zero removal startegy. see the class javadoc.
 	 * 
 	 * @param datastoreParams
 	 * @param crs
 	 * @param aggregationLevel
 	 * @param onGrid
 	 * @param partnerId
 	 * @throws IOException
 	 */
 	public void removeZeros(CoordinateReferenceSystem crs, int aggregationLevel, String closePhase)
 					throws IOException {
 		reset();
 
 		if (isValid()) {
 
 			crs = checkCrs(crs);
 
 			int process = -1;
 			int trace = -1;
 
 			int errors = 0;
 			int startErrors = 0;
 			// existing process
 			MetadataIngestionHandler.Process importData = getProcessData();
 			if (importData != null) {
 				process = importData.getId();
 				trace = importData.getMaxTrace();
 				errors = importData.getMaxError();
 				startErrors = errors;
 			}
 
 			if (metadataHandler != null && process == -1) {
 				LOGGER.error("Cannot find process for input file");
 				throw new IOException("Cannot find process for input file");
 			}
 
 			// First iteration on NR_INCIDENTI -> NR_INCIDENTI_ELAB
 			LOGGER.debug("Start first iteration");
 			errors = this.iterativeProcess("nr_incidenti", "nr_incidenti_elab", true,
 					aggregationLevel, startErrors, errors, trace, process,
 					closePhase);
 			// Check for zero nr_incidenti_elab
 
 			// Second iteration on NR_INCIDENTI_ELAB -> NR_INCIDENTI_ELAB
 			if (this.existsZeroInElab(aggregationLevel, "nr_incidenti_elab")) {
 				LOGGER.debug("Start second iteration");
 				this.iterativeProcess("nr_incidenti_elab", "nr_incidenti_elab",
						false, aggregationLevel, startErrors, errors, trace,
 						process, closePhase);
 			}
 
 		}
 
 	}	
 
 	private Boolean existsZeroInElab(int aggregationLevel, String field){
 		Boolean nElabZero = false;
 		try{
 			//Check if exists almost one arc for partner with NR_INCIDENTI_ELAB = 0
 			String geoName = getTypeName(GEO_TYPE_NAME, aggregationLevel);
 			createInputReader(dataStore,  Transaction.AUTO_COMMIT, geoName);
 			setInputFilter(
 					filterFactory.and(
 							filterFactory.equals(filterFactory.property(PARTNER_FIELD),filterFactory.literal(partner)),
 							filterFactory.equals(filterFactory.property(field),filterFactory.literal(0))
 							)
 					);
 			nElabZero = (getImportCount() > 0);
 		} catch (Exception e) {
 			LOGGER.error(e.getMessage(), e);
 		}
 		return nElabZero;
 	}
 
         private int iterativeProcess(String inputField, String outputField, Boolean streetAggregation,
                 int aggregationLevel, int startErrors, int errors, int trace, int process,
                 String closePhase) throws IOException {
             Transaction transaction = new DefaultTransaction("ZeroRemoval");
             try {
                 // setup geo input / output object
                 String geoName = getTypeName(GEO_TYPE_NAME, aggregationLevel);
     
                 // setup input reader
                 createInputReader(dataStore, transaction, geoName);
     
                 OutputObject geoObject = new OutputObject(dataStore, transaction, geoName, GEOID);
     
                 setInputFilter(filterFactory.equals(filterFactory.property(PARTNER_FIELD),
                         filterFactory.literal(partner)));
                 
                 // get unique aggregation values in order to identify the roads
                 Set<BigDecimal> aggregationValues = null;
                 if (streetAggregation) {
                     aggregationValues = getAggregationBigValues(ID_ORIGIN);
                 } else {
                     aggregationValues = new HashSet<BigDecimal>(Arrays.asList(new BigDecimal(-1)));
                 }
                 int total = aggregationValues.size();
                 int aggregationCount = 0;
                 for (BigDecimal aggregationValue : aggregationValues) {
                 	aggregationCount++;
                     try {
                         //
                         // First of all filter all the arcs to a specified road and partner
                         //
                         Filter filter = filterFactory.equals(filterFactory.property(PARTNER_FIELD),
                                 filterFactory.literal(partner));
                         if (streetAggregation) {
                             filter = filterFactory.and(
                                     filter,
                                     filterFactory.equals(filterFactory.property(ID_ORIGIN),
                                             filterFactory.literal(aggregationValue)));
                         }
                         setInputFilter(filter);
                         // int arcs = getImportCount();
                         Double incidenti = (Double) getSumOnInput(inputField, new Double(0)).doubleValue();
                         if (incidenti != 0.0) {
                             Long lunghezzaTotale = (Long) getSumOnInput(LUNGHEZZA, new Long(0)).longValue();
                             DecIncManager decIncManager = new DecIncManager(inputField, kInc,
                                     lunghezzaTotale);
         
                             //
                             // Calculate elaborated incident
                             // In each iteration decrease the kInc and check if no negative values are computed
                             //
                             //
                             decIncManager.computeNextIteration();
                             while (decIncManager.computeZeros()) {                                
                                 decIncManager.computeNextIteration();                                
                             }
         
                             // Update features
                            int count = 0;
                             FeatureStore<SimpleFeatureType, SimpleFeature> writer = geoObject.getWriter();
                             writer.setTransaction(transaction);
                             for (SimpleFeature inputFeature : decIncManager.getElabIncident().keySet()) {
                                 // updateImportProgress(total, errors - startErrors, "Update feature N. incidenti elaborati = " +
                                 // decIncManager.getElabIncident().get(inputFeature));
                                 // LOGGER.debug("Update feature N. incidenti elaborati = " + decIncManager.getElabIncident().get(inputFeature));
                                 updateIncidentalita(outputField, writer, geoObject, inputFeature,
                                        decIncManager.getElabIncident().get(inputFeature));
                                if (count % 50 == 0) {
                                    transaction.commit();
                                }
                                count++;
                             }
                             transaction.commit();
                         }
                         updateImportProgress(aggregationCount, total, errors - startErrors, "Spreading zeros data in " + geoObject.getName());
                     } catch (IllegalArgumentException e) {
                         LOGGER.error(e.getMessage(), e);
                         transaction.rollback();
                         errors++;
                         if (metadataHandler != null) {
                            metadataHandler.logError(trace, errors, "Error importing data",
                                    getError(e), 0);
                         }
                     }
                 }
                importFinished(aggregationCount, errors - startErrors, "Accident data updated in " + geoName);
                 return errors;
             } catch (Exception e) {
                 LOGGER.error(e.getMessage(), e);
                 transaction.rollback();
                 errors++;
                 if (metadataHandler != null) {
                     metadataHandler.logError(trace, errors, "Error importing data", getError(e), 0);
                 }
                 throw new IOException(e);
             } finally {
                 if (process != -1 && closePhase != null) {
                     // close current process phase
                     if (metadataHandler != null) {
                         metadataHandler.closeProcessPhase(process, closePhase);
                     }
                 }
                 closeInputReader();    
                 transaction.close();
                 
             }
         }
 
 	/**
 	 * @param writer 
 	 * @param geoObject
 	 * @param inputFeature
 	 * @param newIncidenti
 	 * @throws IOException
 	 */
 	private void updateIncidentalita(String outputField, FeatureStore<SimpleFeatureType, SimpleFeature> writer, OutputObject geoObject, SimpleFeature inputFeature,
 			double newIncidenti) throws IOException {
 		Filter updateFilter = filterFactory.equals(filterFactory.property(GEOID),
 				filterFactory.literal(inputFeature.getAttribute(GEOID)));
 		if(Double.isNaN(newIncidenti) || Double.isInfinite(newIncidenti)) {
 			newIncidenti = 0.0;
 		}
 		writer.modifyFeatures(
 				geoObject.getSchema().getDescriptor(outputField).getName(), newIncidenti,
 				updateFilter);
 	}
 	
 	/**
 	 * @param e
 	 * @return
 	 */
 	private class DecIncManager{
 
 		private Map<SimpleFeature,Double> elabIncident = new HashMap<SimpleFeature,Double>();
 		private static final double KINCR_DECR_STEP = .01;
 		private double kInc;
 		private double dec;
 		private double inc;
 		private long lunghezzaTotale;
 		private String inputField;
 
 		public Map<SimpleFeature, Double> getElabIncident() {
 			return elabIncident;
 		}
 
 		public DecIncManager(String inputField, double kIncStart, long lunghezzaTotale) throws IOException {
 			super();
 			this.kInc = kIncStart + KINCR_DECR_STEP;
 			this.lunghezzaTotale = lunghezzaTotale;
 			this.inputField = inputField;
 
 			SimpleFeature in;
 			while((in = readInput()) != null){
 				elabIncident.put(in, -1d);
 			}
 
 		}
 
 		public boolean computeZeros(){
 			boolean noValidValueFound = false;
 			try {
 				for (SimpleFeature inputFeature : this.elabIncident.keySet() ) {
 					Object value = inputFeature.getAttribute(this.inputField);
 					double nrIncidenti = 0;
 					if(value instanceof Double){
 						nrIncidenti = ((Double) inputFeature.getAttribute(this.inputField)).doubleValue();
 					}
 					if(value instanceof BigDecimal){
 						nrIncidenti = ((BigDecimal) inputFeature.getAttribute(this.inputField)).doubleValue();
 					}
 					if(nrIncidenti < 0) {
 						nrIncidenti = 0;
 					}
 					double newIncidenti = (double) nrIncidenti;
 					if (newIncidenti == 0) {
 						newIncidenti += inc;
 					} else {
 						newIncidenti -= dec;
 						if(newIncidenti <= 0){
 							noValidValueFound = true;
 							LOGGER.debug("No valid value (less than 0 for non zero ARC) found for feature kInc = " + df.format(this.kInc));
 							break;
 						}
 					}
 					elabIncident.put(inputFeature, newIncidenti);
 				}
 			} catch(Exception e){
 				LOGGER.error(e.getMessage(), e);
 			}
 			return noValidValueFound;
 		}
 
 		public void computeNextIteration() throws IllegalArgumentException{
 			this.kInc = this.kInc - KINCR_DECR_STEP;
 			if(this.kInc <= 0){
 				throw new IllegalArgumentException("No valid kInc found!");
 			}
 			Double weightedSum = 0.0;
 			int n = 0;
 			int m = 0;
 
 			try {
 				for (SimpleFeature inputFeature : this.elabIncident.keySet() ) {
 					double nrIncidenti = 0;
 					Object value = inputFeature.getAttribute(this.inputField);
 					if(value instanceof Double){
 						nrIncidenti = ((Double) inputFeature.getAttribute(this.inputField)).doubleValue();
 					}
 					if(value instanceof BigDecimal){
 						nrIncidenti = ((BigDecimal) inputFeature.getAttribute(this.inputField)).doubleValue();
 					}					
 					int lunghezza = ((BigDecimal) inputFeature.getAttribute(LUNGHEZZA))
 							.intValue();
 					if (nrIncidenti == 0) {
 						n += lunghezza;
 					} else {
 						m += lunghezza;
 					}
 					weightedSum += (double) (nrIncidenti * lunghezza);
 				}
 				Double weightedAvg = weightedSum / lunghezzaTotale;
 
 				this.inc = kInc * weightedAvg;
 				this.dec = inc * n / m;
 
 
 			} catch(Exception e){
 				LOGGER.error(e.getMessage(), e);
 			}
 
 		}
 	}
 }
