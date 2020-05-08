 package uk.ac.ebi.mdk.service.loader.data;
 
 import au.com.bytecode.opencsv.CSVReader;
 import com.google.common.base.Objects;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import org.apache.log4j.Logger;
 import uk.ac.ebi.mdk.service.index.data.ChEBIDataIndex;
 import uk.ac.ebi.mdk.service.loader.AbstractChEBILoader;
 import uk.ac.ebi.mdk.service.loader.location.ZIPRemoteLocation;
 import uk.ac.ebi.mdk.service.loader.writer.DefaultDataIndexWriter;
 import uk.ac.ebi.mdk.service.location.ResourceFileLocation;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.*;
 
 /**
  * ChEBIDataLoader - 28.02.2012 <br/>
  * <p/>
  * Class descriptions.
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$
  */
 public class ChEBIDataLoader extends AbstractChEBILoader {
 
     private static final Logger LOGGER = Logger.getLogger(ChEBIDataLoader.class);
 
     public ChEBIDataLoader() throws IOException {
         super(new ChEBIDataIndex());
 
         addRequiredResource("ChEBI Chemical Data",
                             "",
                             ResourceFileLocation.class,
                            new ZIPRemoteLocation("ftp://ftp.ebi.ac.uk/pub/databases/chebi/Flat_file_tab_delimited/chemical_data.tsv.zip"));
     }
 
     public static void main(String[] args) throws Exception{
         ChEBIDataLoader loader = new ChEBIDataLoader();
         loader.backup();
         loader.update();
     }
 
     @Override
     public void update() throws IOException {
 
         ResourceFileLocation location = getLocation("ChEBI Chemical Data");
         CSVReader csv = new CSVReader(new InputStreamReader(location.open()), '\t', '\0');
 
         List<String> header = Arrays.asList(csv.readNext());
         int idindex   = header.indexOf("COMPOUND_ID");
         int typeindex = header.indexOf("TYPE");
         int dataindex = header.indexOf("CHEMICAL_DATA");
         int sourceindex = header.indexOf("SOURCE");
 
         Multimap<String, DataValue> values = HashMultimap.create();
 
         String[] row = null;
         while((row = csv.readNext()) != null){
 
             // user cancelled
             if(isCancelled()) break;
 
             String id = getPrimaryIdentifier(row[idindex]);
             String type = row[typeindex];
             String data = row[dataindex];
             String source = row[sourceindex];
 
             values.put(id, new DataValue(type, data, source));
 
         }
 
         DefaultDataIndexWriter writer = new DefaultDataIndexWriter(getIndex());
 
         for(String accession : values.keySet()){
             Collection<DataValue> data = values.get(accession);
             if(isActive(accession)){
                 writer.write(accession, getCharge(data), getFormula(accession, data));
             }
         }
 
         writer.close();
 
         location.close();
 
 
     }
     
     public String getCharge(Collection<DataValue> values){
         Set<DataValue> charges = new HashSet<DataValue>();
         for(DataValue value : values){
             if(value.type.equals("CHARGE") && !value.value.equals(".")){
                 charges.add(value);
             }
         }
 
         if(charges.size() == 1)
             return charges.iterator().next().value;
 
         return "";
 
     }
     
     public String getFormula(String accession, Collection<DataValue> values){
 
         Set<DataValue> formulae = new HashSet<DataValue>();
         for(DataValue value : values){
             if(value.type.equals("FORMULA") && !value.value.equals(".")){
                 formulae.add(value);
             }
         }
 
         if(formulae.isEmpty())
             return "";
 
         // single entry
         if(formulae.size() == 1){
             return formulae.iterator().next().value;
         }
 
         // resolve duplicates
         Multimap<String,DataValue> sourceMap = HashMultimap.create();
         
         for(DataValue value : formulae){
             sourceMap.put(value.source, value);
         }
 
         if(sourceMap.size() != 1 && sourceMap.containsKey("ChEBI")){
             Collection<DataValue> chebiFormulae = sourceMap.get("ChEBI");
             if(chebiFormulae.size() == 1){
                 return chebiFormulae.iterator().next().value;
             } else if (chebiFormulae.size() == 2) {
                 // if there are two from ChEBI, favour the one with the R group
                 Iterator<DataValue> it = chebiFormulae.iterator();
                 DataValue first  = it.next();
                 DataValue second = it.next();
                 if(first.value.contains("R") && !second.value.contains("R")){
                     return first.value;
                 } else if(second.value.contains("R") && !first.value.contains("R")){
                     return second.value;
                 }
             }
 
         }
 
         LOGGER.warn("Could not resolve single formula for: " + accession + " : " + formulae);
 
         // just use the first one
         return formulae.iterator().next().value;
 
     }
     
     private class DataValue {
         
         private String type;
         private String value;
         private String source;
         
         public DataValue(String type, String value, String source){
             this.type = type;
             this.value = value;
             this.source = source;
         }
 
         @Override
         public String toString() {
             return value + " [" + source + "]";
         }
 
         @Override
         public int hashCode() {
             return Objects.hashCode(type, value, source);
         }
 
         @Override
         public boolean equals(Object obj) {
 
             if(!(obj instanceof DataValue))
                 return false;
 
             DataValue other = (DataValue) obj;
 
             return Objects.equal(this.type, other.type)
                     && Objects.equal(this.value, other.value)
                     && Objects.equal(this.source, other.source);
         }
     }
 }
