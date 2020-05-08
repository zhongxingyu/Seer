 import java.io.FileReader;
 import java.io.Reader;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.TreeMap;
 
 import com.staktrace.util.conv.json.JsonArray;
 import com.staktrace.util.conv.json.JsonObject;
 import com.staktrace.util.conv.json.JsonReader;
 
 public class Differ {
     private final Map<String, Long> _base;
     private final Map<String, Long> _modified;
 
     Differ( Reader base, Reader modified ) throws Exception {
         _base = toMap( new JsonReader( base ).readObject() );
         _modified = toMap( new JsonReader( modified ).readObject() );
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
             String path = (String)report.getValue( "path" );
             long value = ( (BigInteger)report.getValue( "amount" ) ).longValue();
             add( map, path, value );
             while (path.lastIndexOf( '/' ) >= 0) {
                 path = path.substring( 0, path.lastIndexOf( '/' ) );
                 add( map, path, value );
             }
         }
         return map;
     }
 
     public void dumpDiff() {
         Map<String, DiffEntry> diffs = new HashMap<String, DiffEntry>();
         for (String key : _base.keySet()) {
             Long baseVal = _base.get( key );
             Long modVal = _modified.remove( key );
             DiffEntry diff = null;
             if (modVal == null) {
                 diff = new DiffEntry(key + " (removed)", 0 - baseVal);
             } else if (! baseVal.equals(modVal)) {
                 diff = new DiffEntry(key, modVal - baseVal);
             }
             if (diff != null) {
                 diffs.put(key, diff);
             }
         }
         for (String key : _modified.keySet()) {
             Long modVal = _modified.get( key );
             DiffEntry diff = new DiffEntry(key + " (added)", modVal);
             diffs.put(key, diff);
         }
         List<DiffEntry> roots = new ArrayList<DiffEntry>();
         for (String key : diffs.keySet()) {
             int lastIx = key.lastIndexOf( '/' );
             if (lastIx >= 0) {
                 String parent = key.substring( 0, lastIx );
                 if (diffs.containsKey( parent )) {
                     diffs.get( parent ).addChild( diffs.get( key ) );
                     continue;
                 }
             }
             roots.add( diffs.get( key ) );
         }
         for (DiffEntry diff : diffs.values()) {
             diff.sort();
         }
         Collections.sort(roots);
 
         dumpTree(roots, new StringBuffer());
     }
 
     private void dumpTree(List<DiffEntry> roots, StringBuffer prefix) {
         int prefixLength = prefix.length();
         for (DiffEntry diff : roots) {
             System.out.println( prefix.toString() + diff._delta + " " + diff._path );
             prefix.append( "  " );
             dumpTree( diff._children, prefix );
             prefix.setLength( prefixLength );
         }
     }
 
     private static class DiffEntry implements Comparable<DiffEntry> {
         public final long _delta;
         public final String _path;
         public final List<DiffEntry> _children;
 
         DiffEntry( String path, long delta ) {
             _delta = delta;
             _path = path;
             _children = new ArrayList<DiffEntry>();
         }
 
         void addChild( DiffEntry child ) {
             _children.add( child );
         }
 
         void sort() {
             Collections.sort( _children );
         }
 
         public int compareTo( DiffEntry other ) {
            if (other._delta == this._delta) {
                return _path.compareTo( other._path );
            }
             return (int)(other._delta - this._delta);
         }
     }
 
     public static void main( String[] args ) throws Exception {
         if (args.length < 2) {
             System.err.println( "Usage: java Differ <file1.json> <file2.json>" );
             return;
         }
 
         new Differ( new FileReader( args[0] ), new FileReader( args[1] ) ).dumpDiff();
     }
 }
