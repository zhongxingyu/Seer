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
 package it.geosolutions.geobatch.destination.vulnerability;
 
 import it.geosolutions.destination.utils.Formula;
 import it.geosolutions.destination.utils.FormulaUtils;
 import it.geosolutions.geobatch.destination.common.InputObject;
 import it.geosolutions.geobatch.destination.common.OutputObject;
 import it.geosolutions.geobatch.flow.event.ProgressListenerForwarder;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.geotools.data.DefaultTransaction;
 import org.geotools.data.Transaction;
 import org.geotools.feature.NameImpl;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
 import org.geotools.jdbc.JDBCDataStore;
 import org.geotools.jdbc.JDBCDataStoreFactory;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class is the entry point for the vulnerability process (method
  * computeVulnerability) and it implements the computation's main loop
  * 
  * @author Alessio Fabiani - <alessio.fabiani at geo-solutions.it>
  * 
  */
 public class RiskComputation extends InputObject {
 
 	private final static Logger LOGGER = LoggerFactory
 			.getLogger(RiskComputation.class);
 
 	private static Pattern TYPE_NAME_PARTS = Pattern.compile("^([a-z]{4})_([a-z]{3})_([a-z]{2})_([a-z]{4})_([1-3]{1})");
 
 	public static String ARC_INPUT_TYPE_NAME_LN = "siig_geo_ln_arco_X";
 	public static String ARC_INPUT_TYPE_NAME_PL = "siig_geo_pl_arco_X";
 
 	public static String RISK_OUTPUT_TYPE_NAME = "siig_t_elab_standard_X";
 	public static String GEOID = "id_geo_arco";
 
 	/**
 	 * @param inputTypeName
 	 * @param listenerForwarder
 	 */
 	public RiskComputation(int aggregationLevel,
 			ProgressListenerForwarder listenerForwarder) {
 		super(getTypeName(aggregationLevel < 3 ? ARC_INPUT_TYPE_NAME_LN : ARC_INPUT_TYPE_NAME_PL, aggregationLevel), listenerForwarder);
 		// default area
 	}
 
 	@Override
 	protected boolean parseTypeName(String typeName) {
 		Matcher m = TYPE_NAME_PARTS.matcher(typeName);
 		if (m.matches()) {
 			return true;
 		}
 		return false;
 	}
 
 	private static String getTypeName(String typeName, int aggregationLevel) {
 		return typeName.replace("X", aggregationLevel + "");
 	}
 
     private Long startOriginId; 
     private Long endOriginId;
     private Long totPages; 
     private Long pageNumber;
     
     /**
      * @param startOriginId the startOriginId to set
      */
     public void setStartOriginId(Long startOriginId) {
         this.startOriginId = startOriginId;
     }
 
     /**
      * @param endOriginId the endOriginId to set
      */
     public void setEndOriginId(Long endOriginId) {
         this.endOriginId = endOriginId;
     }
 
     /**
      * @param totPages the totPages to set
      */
     public void setTotPages(Long totPages) {
         this.totPages = totPages;
     }
 
     /**
      * @param pageNumber the pageNumber to set
      */
     public void setPageNumber(Long pageNumber) {
         this.pageNumber = pageNumber;
     }
     
 	/**
 	 * Pre-computes the risks and stores the outcomes on the DB.
 	 * 
 	 * @param datastoreParams
 	 * @param batch
 	 *            batch calculus size
 	 * @param precision
 	 *            output value precision (decimals)
 	 * @param aggregationLevel
 	 *            input arcs level
 	 * @param formula
 	 *            id of the formula to calculate
 	 * @param target
 	 *            id of the target/s to use in calculation
 	 * @param materials
 	 *            ids of the materials to use in calculation
 	 * @param scenarios
 	 *            ids of the scenarios to use in calculation
 	 * @param entities
 	 *            ids of the entities to use in calculation
 	 * @param severeness
 	 *            ids of the severeness to use in calculation
 	 * @throws IOException
 	 */
 	public void prefetchRiskAtLevel(Map<String, Serializable> datastoreParams,
			Integer precision, int aggregationLevel, int formula, int target, String materials, String scenarios,
			String entities, String severeness, int id_partner, String writeMode) throws IOException {
 
 		reset();
 		if (isValid()) {
 			if (precision == null) {
 				precision = 3;
 			}
 
 			JDBCDataStore dataStore = null;
 
 			// read input features
 			String outputFeatureName = getTypeName(RISK_OUTPUT_TYPE_NAME, aggregationLevel);
 
 			int trace = -1;
             int errors = 0;
 			long otherErrors = 0;
 
 			if(datastoreParams.get("expose primary keys") == null && datastoreParams.get("expose_primary_keys") == null)
 			{
 				datastoreParams.put(JDBCDataStoreFactory.EXPOSE_PK.key, Boolean.TRUE);
 			}
 			dataStore = connectToDataStore(datastoreParams);
 			createInputReader(dataStore, null, null);
 
 			// calculates total objects to import
 			int total = getImportCount();
 			
 			// ------------------ TODO
             //Setup the OutputObject (in that case the "elab" table) remove old values for the partner and detect the max id
             Transaction transaction = new DefaultTransaction();
             OutputObject riskObj = new OutputObject(dataStore, transaction, outputFeatureName, GEOID);
 
             //
             // Load the concrete operation (Insert or PurgeInsert or Update)
             //
             VulnerabilityOperation concreteOperation = VulnerabilityOperation.instantiateWriterFromString(writeMode);
             LOGGER.info("Using writer " + VulnerabilityOperation.class);
             concreteOperation.initFeature(riskObj, id_partner);
             // Setup filtering
             concreteOperation.setStartOriginId(startOriginId);
             concreteOperation.setEndOriginId(endOriginId);
             concreteOperation.setPageNumber(pageNumber);
             concreteOperation.setTotPages(totPages);
 			
 			//
             // Start the risk Computation
             //
 			try {
 				// we will calculate risk in batch of arcs
 				// we store each feature of the batch in a map
 				// indexed by id					
 				// ids will store the list of id of each batch
 				// used to build risk query
                 SimpleFeature inputFeature = null;
                 setInputFilter(concreteOperation.buildOriginFilter(id_partner, total));
                 LOGGER.info("Start computation: ThreadName: " + Thread.currentThread().getName() + " - startOriginId: " + startOriginId + " - endOriginId: " + endOriginId);
 
              // output FeatureType (risk)
     			//  - id_geo_arco
     			//  - geometria
     			//  - rischio1
     			//  - rischio2
     			SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();									
     			tb.add("id_geo_arco", riskObj.getSchema().getDescriptor("id_geo_arco").getType().getBinding());
     			//tb.add("geometria", MultiLineString.class,riskObj.getSchema().getGeometryDescriptor().getCoordinateReferenceSystem());
     	        tb.add("rischio1", Double.class);			
     	        tb.add("rischio2", Double.class);
     	        // fake layer name (risk) used for WPS output. Layer risk must be defined in GeoServer
     	        // catalog
     	        tb.setName(new NameImpl(riskObj.getSchema().getName().getNamespaceURI(), "risk"));	        
     	        SimpleFeatureType ft = tb.buildFeatureType();
     	        // result builder
 		        SimpleFeatureBuilder fb = new SimpleFeatureBuilder(ft);
 		        
                 //---- TODO
             	Connection conn = null;
                 try{
                 	conn = dataStore.getConnection(transaction);
                	Formula formulaDescriptor = Formula.load(conn, formula, target);
                 	if ((!formulaDescriptor.hasGrid() && aggregationLevel == 3)
         					|| (!formulaDescriptor.hasNoGrid() && aggregationLevel < 3)) {
         	        	LOGGER.info("Formula not supported on this level, returning empty collection");
                 	}
                 	else
                 	{
                         // iterate source features
                 		Map<String, Double> statsMap = new HashMap<String, Double>();
                 		//int cnt = 0;
                         while ((inputFeature = readInput()) != null /*&& cnt < 100*/) {
                         	//cnt++;
                         	Double[] risk = new Double[] {0.0, 0.0};
                         	Number id = (Number)inputFeature.getAttribute("id_geo_arco");
                         	final long flg_lieve   = 1;
                         	final long id_geo_arco = id.longValue();
                         	final long id_distanza = 1;
                         	final long id_scenario = 1;
                         	final long id_sostanza = 9;
                         	
                         	final String fid = flg_lieve+"."+id_geo_arco+"."+id_distanza+"."+id_scenario+"."+id_sostanza;
 
                         	Map<Number, SimpleFeature> temp = new HashMap<Number, SimpleFeature>();
 							// calculate risk here only if it depends from arcs
                         	temp.put(id.intValue(), fb.buildFeature(fid));
                         	//LOGGER.info("Calculated " + count + " values");
                        	FormulaUtils.calculateFormulaValues(conn, aggregationLevel, formulaDescriptor, id_geo_arco + "", materials, scenarios, entities, severeness, target, temp, precision);
                         	
                         	if (temp != null && !temp.isEmpty())
                         	{
                         		statsMap.put("flg_lieve",   new Double(flg_lieve));
                         		statsMap.put("id_geo_arco", new Double(id_geo_arco));
                         		statsMap.put("id_distanza", new Double(id_distanza));
                         		statsMap.put("id_scenario", new Double(id_scenario));
                         		statsMap.put("id_sostanza", new Double(id_sostanza));
 
                         		statsMap.put("fk_partner", new Double(id_partner));
 
                         		risk[0] = (Double) (temp.get(id.intValue()).getAttribute("rischio1") != null ? temp.get(id.intValue()).getAttribute("rischio1") : risk[0]);
                         		risk[1] = (Double) (temp.get(id.intValue()).getAttribute("rischio2") != null ? temp.get(id.intValue()).getAttribute("rischio2") : risk[1]);
 
                         		statsMap.put("calc_formula_soc", risk[0]);
                         		statsMap.put("calc_formula_amb", risk[1]);
                         	}
                         	
                         	if(!statsMap.isEmpty()){
                         		errors = concreteOperation.writeOutputObjects(trace, dataStore, riskObj, total, errors, outputFeatureName, inputFeature, fid, statsMap, id_partner);
                         		LOGGER.info("Computed Risk for output feature [" + outputFeatureName + "]:" + fid + " - Risk values [" + risk[0] + ";" + risk[1] + "]");
                         		statsMap.clear();
                         	}
                         }
 
                 	}
                 }
                 catch(Exception e){
                     otherErrors++;
                     LOGGER.error(e.getMessage(), e);
                 }
                 finally
                 {
         			transaction.close();
         			if(conn != null) {
         				try {
 							conn.close();
 						} catch (SQLException e) {
 							LOGGER.warn("SQL Exception while closing connection to the DB.", e);
 						}
         			}
                 }
                     
                 importFinished(total, "Data imported in " + outputFeatureName);
             } finally {
                 closeInputReader();
                 LOGGER.info("Write errors: " + errors + " - other errors: " + otherErrors);
             }
 			
 		}
 	}
 }
