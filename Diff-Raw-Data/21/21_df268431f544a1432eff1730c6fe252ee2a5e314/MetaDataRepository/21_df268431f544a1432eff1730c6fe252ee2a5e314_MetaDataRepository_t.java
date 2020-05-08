 package com.servicenow.syseng.metadata;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.io.IOException;
 import java.util.List;
 
 import com.servicenow.syseng.datamodel.CanonicalMetrics;
 import com.servicenow.syseng.datamodel.Reading;
 import com.servicenow.syseng.datamodel.ReadingType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MetaDataRepository {
 
 
     public static final String REPO_MAP_NAME = "repository";
     private static final Logger logger = LoggerFactory.getLogger(MetaDataRepository.class);
 
     // repositoryMap stores the map for all NamedPersistentMap including itself
     private static NamedPersistentMap repositoryMap;
 
     static { // init repository
         try {
             // make sure hbase has the schema
             HbaseAccess.init();
             AsynchbaseAccess.init();
             repositoryMap = new NamedPersistentMap(REPO_MAP_NAME);
         } catch (Exception e) {
             logger.error("Cannot initialize MetaDataRepository");
         }
     }
 
     // get the NamedPersistentMap called name
     // if not exists yet, create one and return
     public static NamedPersistentMap getNamedPersistentMap(String name) throws IOException {
         if (repositoryMap.getValue(name).isEmpty()) { // add to repositoryMap if not exists
             createNamedPersistentMap(name);
         }
         return new NamedPersistentMap(name);
     }
 
 
     // get all NamedPersistentMap names
     public static Collection<String> getAllNamedPersistentMapNames() throws IOException {
 	return repositoryMap.keys();
     }
 
 
     // create a new NamedPersistentMap called name
     private static final void createNamedPersistentMap(String name) throws IOException {
 	    repositoryMap.addValue(name,name);
     }
 
 
     // add to a NamedPersistentMap a new key-value pair (extacted from cm via extractor)
     // if the NamedPersistentMap does not exist, create one first
     public static final void addMetaData(MetaDataExtractor extractor, CanonicalMetrics cm) throws IOException {
         MetaDataExtractor.KeyValuePair pair = extractor.extract(cm);
         if (!extractor.exists(pair)) {   // add to persistent map if not yet done
             NamedPersistentMap map = getNamedPersistentMap(extractor.getMapName());
             map.addValue(pair.key,pair.value);
         }
     }
 
 
     public static final void main(String[] args) {
         try {
             long startTime = System.currentTimeMillis();
 
             NamedPersistentMap hostMetricsMap = new NamedPersistentMap("host-metrics");
             hostMetricsMap.addValue("host1","xmlstats.transaction.count");
             hostMetricsMap.addValue("host1","xmlstats.transaction.avg");
             Collection<String> metrics = hostMetricsMap.getValue("host1");
             System.out.println("Host host1 has metrics:");
             for (String m:metrics) {
                 System.out.print(m+" ");
             }
             System.out.println();
 
             NamedPersistentMap dataCenterHostsMap = new NamedPersistentMap("datacenter-hosts");
            dataCenterHostsMap.addValue("iad2","host1");
            dataCenterHostsMap.addValue("iad2","host2");
             Collection<String> hosts = dataCenterHostsMap.getValue("iad1");
             System.out.println("Data center iad1 has hosts:");
             for (String h:hosts) {
                 System.out.print(h+" ");
             }
             System.out.println();
 
             long endTime = System.currentTimeMillis();
             System.out.println("Elapsed time = "+ (endTime-startTime) + " ms");
 
             // build canonical metrics object
             List<Reading> readings = new ArrayList<Reading>();
             readings.add(new Reading("shortterm", ReadingType.GAUGE, 0.1));
             readings.add(new Reading("midterm",ReadingType.GAUGE,0.2));
             readings.add(new Reading("longterm", ReadingType.GAUGE, 0.3));
             CanonicalMetrics cm = new CanonicalMetrics(
                     System.currentTimeMillis(),"42",
                    "jenkins02.sea2.service-now.com","xmlstats.linux.load.load",
                     null,"1.0","1.1",readings);
             // test extractor
             MetaDataExtractor extractor = new TestHostMetricsExtractor();
             // extact and add meta data
             addMetaData(extractor,cm);
 
            System.exit(0);

         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
 }
