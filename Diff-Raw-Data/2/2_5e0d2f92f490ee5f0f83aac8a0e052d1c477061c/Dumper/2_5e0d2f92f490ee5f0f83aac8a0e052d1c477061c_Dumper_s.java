 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.math.BigInteger;
 import java.util.Map;
 import java.util.TreeMap;
 
 import com.staktrace.util.conv.json.JsonArray;
 import com.staktrace.util.conv.json.JsonObject;
 import com.staktrace.util.conv.json.JsonReader;
 
 public class Dumper {
     private final Map<String, Long> _data;
 
     Dumper( Reader in ) throws Exception {
         JsonObject data = new JsonReader( in ).readObject();
         _data = toMap( data );
         _data.put( "explicit/heap-unclassified", calculateHeapUnclassified( data ) );
     }
 
     private void add( Map<String, Long> map, String path, long value ) {
         Long old = map.get( path );
         if (old == null) {
             map.put( path, value );
         } else {
             map.put( path, value + old );
         }
     }
 
     private Map<String, Long> toMap( JsonObject memDump ) {
         Map<String, Long> map = new TreeMap<String, Long>();
         JsonArray reports = (JsonArray)memDump.getValue( "reports" );
         for (int i = reports.size() - 1; i >= 0; i--) {
             JsonObject report = (JsonObject)reports.getValue( i );
             if (( (BigInteger)report.getValue( "units" ) ).intValue() != 0) {
                 continue;
             }
             String path = (String)report.getValue( "path" );
             long value = ( (BigInteger)report.getValue( "amount" ) ).longValue();
             add( map, path, value );
         }
         return map;
     }
 
     private long calculateHeapUnclassified( JsonObject memDump ) {
         long knownHeap = 0;
         JsonArray reports = (JsonArray)memDump.getValue( "reports" );
         for (int i = reports.size() - 1; i >= 0; i--) {
             JsonObject report = (JsonObject)reports.getValue( i );
             String path = (String)report.getValue( "path" );
             if (path.startsWith( "explicit/" )
                 && ( (BigInteger)report.getValue( "units" ) ).intValue() == 0
                 && ( (BigInteger)report.getValue( "kind" ) ).intValue() == 1)
             {
                knownHeap += _data.get( path );
             }
         }
         return _data.get( "heap-allocated" ) - knownHeap;
     }
 
     public void dumpData( String prefix ) {
         for (String path : _data.keySet()) {
             System.out.println( prefix + path );
             System.out.println( _data.get( path ) );
         }
     }
 
     public static void main( String[] args ) throws Exception {
         new Dumper( new InputStreamReader( System.in ) ).dumpData( args[0] );
     }
 }
