 package forma;
 
 // import java.beans.ConstructorProperties;
 
 import forma.WholeFileInputFormat;
 import cascading.scheme.Scheme;
 import cascading.tap.Tap;
 import cascading.tuple.Fields;
 import cascading.tuple.Tuple;
 import cascading.tuple.TupleEntry;
 import java.io.IOException;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.OutputCollector;
 
 public class WholeFile extends Scheme {
     // @ConstructorProperties({"fields"})
     public WholeFile( Fields fields ) {
         super(fields);
     }
 
     @Override
     public void sourceInit( Tap tap, JobConf conf ) {
        conf.qsetInputFormat( WholeFileInputFormat.class );
     }
     
     @Override
     public void sinkInit(Tap tap, JobConf conf) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     @Override
     public Tuple source( Object key, Object value )
     {
         Tuple tuple = new Tuple();
         tuple.add(value);
         return tuple;
     }
     
     @Override
     public void sink(TupleEntry te, OutputCollector oc) throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 } 
