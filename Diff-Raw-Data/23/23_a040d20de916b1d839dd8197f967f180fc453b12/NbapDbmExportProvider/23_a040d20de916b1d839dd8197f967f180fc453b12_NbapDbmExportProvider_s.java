 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.neighbours.gpeh;
 
 import java.util.Calendar;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.amanzi.awe.neighbours.gpeh.Calculator3GPPdBm.ValueType;
 import org.amanzi.awe.statistic.CallTimePeriods;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.apache.log4j.Logger;
 import org.hsqldb.lib.StringUtil;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.index.lucene.LuceneIndexService;
 
 /**
  * <p>
  * Export provider for Nbap reports in Dbm
  * </p>
  * 
  * @author TsAr
  * @since 1.0.0
  */
 public class NbapDbmExportProvider extends ExportProvider3GPP {
     private  static final Logger LOGGER = Logger.getLogger(NbapDbmExportProvider.class);
     private Integer power;
 
     /**
      * Instantiates a new nbap dbm export provider.
      * 
      * @param dataset the dataset
      * @param network the network
      * @param service the service
      * @param value3gpp the value3gpp
      * @param statRelation the stat relation
      * @param period the period
      * @param dataname the dataname
      * @param luceneService the lucene service
      */
     public NbapDbmExportProvider(Node dataset, Node network, GraphDatabaseService service, ValueType value3gpp, RelationshipType statRelation, CallTimePeriods period,
             String dataname, LuceneIndexService luceneService) {
         super(dataset, network, service, value3gpp, statRelation, period, dataname, luceneService);
     }
 
     @Override
     protected void createArrayHeader() {
         for (int i = 20; i <= 50; i++) {
             headers.add(String.valueOf(i));
         }
     }
 
     @Override
     public List<Object> getNextLine() {
         Pair<Node, int[]> values = model.next();
         List<Object> result = new LinkedList<Object>();
         String name = (String)values.getLeft().getProperty("userLabel", "");
         if (StringUtil.isEmpty(name)) {
             name = NeoUtils.getNodeName(values.getLeft(), service);
         }
         result.add(name);
         power = (Integer)values.getLeft().getProperty("maximumTransmissionPower", null);
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(computeTime);
         result.add(dateFormat.format(calendar.getTime()));
         result.add(dateFormat2.format(calendar.getTime()));
         result.add(period.getId());
         int[] array = values.getRight();
         processArray(result, array);
         return result;
     }
 
     @Override
     protected void processArray(List<Object> result, int[] array) {
         int startElem = result.size();
         for (int i = 20; i <= 50; i++) {
             result.add(0);
         }
         if (power == null) {
             return;
         }
        double maxTrPowWatt = Math.pow(10, -3) * Math.pow(10, power / 100);
         for (int i = value3gpp.getMin3GPP(); i <= value3gpp.getMax3GPP(); i++) {
            double txpowerdbm = 10 * Math.log10(Math.ceil(maxTrPowWatt * value3gpp.getRightBound(i) / 1000) / 0.001);// (txpower=TxPower*10
             // i - 0-1000
             // i/1000);
             if (txpowerdbm < 0 || txpowerdbm > 50) {
                 LOGGER.error(String.format("Cell %s. Wrong TxPower %s", "", txpowerdbm));
                 continue;
             }
             int value = array[i];
             if (value != 0) {
                 int ind = 0;
                 txpowerdbm = Math.ceil(txpowerdbm);
                 ind = startElem + (int)txpowerdbm - 20;
                result.set(ind, (Integer)result.get(ind) + (Integer)value);
             }
 
         }
     }
 }
