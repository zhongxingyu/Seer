 package gov.usgs.cida.watersmart.parse.column;
 
 import com.google.common.collect.*;
 import gov.usgs.cida.netcdf.dsg.Observation;
 import gov.usgs.cida.netcdf.dsg.RecordType;
 import gov.usgs.cida.netcdf.dsg.Station;
 import gov.usgs.cida.netcdf.dsg.Variable;
 import gov.usgs.cida.netcdf.jna.NCUtil;
 import gov.usgs.cida.watersmart.parse.StationLookup;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.xml.stream.XMLStreamException;
import org.apache.commons.lang.StringUtils;
 import org.joda.time.Instant;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public class AFINCHParser extends StationPerColumnDSGParser {
     
     private static final Logger LOG = LoggerFactory.getLogger(AFINCHParser.class);
     
     private static final Pattern propertyPattern = Pattern.compile("# (\\w+)");
     private static final Pattern stationLinePattern = Pattern.compile("^\\s+((?:\\d+\\s+)+)$");
     private static final Pattern headerLinePattern = Pattern.compile("^TIMESTEP\\s+((?:[^\\s\\(]+\\([^\\)]+\\)\\s*)+)$");
     private static final Pattern dataLinePattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z)\\s+((?:[^\\s]+\\s*)+)$");
     
     private static final Pattern statisticAndUnitsPattern = Pattern.compile("([^\\(]+)\\(([^\\)]+)\\)");
 
     public static final DateTimeFormatter inputDateFormatter = ISODateTimeFormat.dateTimeParser();
     
     // Create a hashmap for each station, collect Observations
     private ListMultimap<Station, Observation> allData;
     private Iterator<Station> stationIterator;
     private Iterator<Observation> observationIterator;
     private RecordType record;
     
     public AFINCHParser(InputStream input, StationLookup lookup) throws IOException, XMLStreamException {
         super(input, lookup);
         allData = LinkedListMultimap.create();
         stationIterator = null;
         observationIterator = null;
         record = null;
     }
     
     @Override
     public RecordType parse() throws IOException {
         String line = null;
         String property = null;
         String[] stations = null;
         String[] statistics = null;
         while (null != (line = reader.readLine())) {
             Matcher matcher = null;
             matcher = getDataLinePattern().matcher(line);
             if (matcher.matches()) { // actually happens last, put first as common case
                 Instant timestep = Instant.parse(matcher.group(1), getInputDateFormatter());
                 String observations = matcher.group(2);
                 String [] columns = observations.split("\\s+");
                 // add values to list for station, when done, step through and create observations, adding to allData
                 ListMultimap<String,Float> observationBuilderMap = LinkedListMultimap.create();
                 for (int i=0; i<columns.length; i++) {
                     String coli = columns[i];
                     if (StringUtils.isBlank(coli)) continue;
                     Float value = Float.valueOf(coli);
                     String station = stations[i];
                     observationBuilderMap.put(station, value);
                 }
                 for (String station : observationBuilderMap.keySet()) {
                     Station stationObj = stationLookup.get(station);
                     if (stationObj == null) {
                         LOG.debug("station doesn't match one from associated sites layer");
                         continue; // skip this one
                     }
                     List<Float> vals = observationBuilderMap.get(station);
                     Observation ob = new Observation(calculateTimeOffset(timestep),
                             stationObj.index,
                             vals.toArray());
                     allData.put(stationObj, ob);
                 }
                 
             }
             else {
                 matcher = propertyPattern.matcher(line);
                 if (matcher.matches()) {
                     property = matcher.group(1);
                     continue;
                 }
                 matcher = stationLinePattern.matcher(line);
                 if (matcher.matches()) {
                     stations = matcher.group(1).split("\\s+");
                     continue;
                 }
                 matcher = getHeaderLinePattern().matcher(line);
                 if (matcher.matches()) {
                     statistics = matcher.group(1).split("\\s+");
                     continue;
                 }
             }
         }
        
         record = new RecordType("days since " + baseDate.toString());
         // order matters
         Set<String> uniqueStats = Sets.newLinkedHashSet(Lists.newArrayList(statistics));
         
         for (String stat : uniqueStats) {
             Matcher statMatcher = statisticAndUnitsPattern.matcher(stat);
             if (statMatcher.matches()) {
                 String statname = statMatcher.group(1);
                 String units = statMatcher.group(2);
                 String longname = statname + " " + property;
                 Map<String, Object> attrs = Maps.newHashMap();
                 attrs.put("long_name", longname);
                 attrs.put("units", units);
                 Variable statVar = new Variable(statname, NCUtil.XType.NC_FLOAT, attrs);
                 record.addType(statVar);
             }
         }
         // everything set up, start iterator
         //allData.entries().iterator();
         stationIterator = allData.keySet().iterator();
         return record;
     }
     
     @Override
     public boolean hasNext() {
         if (stationIterator == null) {
             throw new RuntimeException("Must parse before iterating data");
         }
         return stationIterator.hasNext() || observationIterator.hasNext();
     }
     
     @Override
     public Observation next() {
         if (stationIterator == null) {
             throw new RuntimeException("Must parse before iterating data");
         }
         while (observationIterator == null || !observationIterator.hasNext()) {
             observationIterator = allData.get(stationIterator.next()).iterator();
         }
         return observationIterator.next();
     }
     
     @Override
     protected Pattern getDataLinePattern() {
         return dataLinePattern;
     }
     
     @Override
     protected Pattern getHeaderLinePattern() {
         return headerLinePattern;
     }
 
     @Override
     protected final DateTimeFormatter getInputDateFormatter() {
         return inputDateFormatter;
     }  
 }
