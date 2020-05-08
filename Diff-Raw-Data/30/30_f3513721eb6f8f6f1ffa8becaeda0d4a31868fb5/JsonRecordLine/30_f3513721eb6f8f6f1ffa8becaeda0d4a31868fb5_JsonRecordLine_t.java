 package com.shopify.schemas;
 
 
 import cascading.flow.FlowProcess;
 import cascading.scheme.SinkCall;
 import cascading.scheme.SourceCall;
 import cascading.scheme.hadoop.TextLine;
 import cascading.tuple.Fields;
 import cascading.tuple.Tuple;
 import com.google.gson.stream.JsonReader;
 import com.google.gson.stream.JsonWriter;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapred.JobConf;
 import org.apache.hadoop.mapred.OutputCollector;
 import org.apache.hadoop.mapred.RecordReader;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.nio.charset.Charset;
 import java.util.HashSet;
 import java.util.Set;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: tobi
  * Date: 3/15/13
  * Time: 10:53 AM
  */
 public class JsonRecordLine extends TextLine {

    public JsonRecordLine(Fields f) {
        super(f);
     }
 
     private Set<String> exportedFields = null;
 
     private Set<String> getExportedFields() {
 
         if(exportedFields == null) {
             Fields fields = getSourceFields();
             Set<String> set = new HashSet<String>(fields.size());
 
             for (Comparable comparable : fields) {
                 set.add(comparable.toString());
             }
             exportedFields = set;
         }
         return exportedFields;
     }
 
     @Override
     public void sink(FlowProcess<JobConf> flowProcess, SinkCall<Object[], OutputCollector> sinkCall) throws IOException {
         Text text = (Text) sinkCall.getContext()[ 0 ];
        //Charset charset = (Charset) sinkCall.getContext()[ 1 ];
 
         StringWriter content = new StringWriter();
         JsonWriter writer = new JsonWriter(content);
 
         writer.beginObject();
 
         Tuple tuple = sinkCall.getOutgoingEntry().getTuple();
        Fields fields = sinkCall.getOutgoingEntry().getFields();
 
 
         int index = 0;
        for (Comparable field: fields) {
            writer.name(field.toString());
            writer.value(tuple.getString(index++));
         }
 
         writer.endObject();
         writer.close();
 
         text.set( content.toString() );
 
         // it's ok to use NULL here so the collector does not write anything
         sinkCall.getOutput().collect( null, text );
     }
 


     @Override
     protected void sourceHandleInput(SourceCall<Object[], RecordReader> sourceCall) {
 
         Tuple tuple = sourceCall.getIncomingEntry().getTuple();
 
         Object[] context = sourceCall.getContext();
 
         Text text = (Text) context[ 1 ];
 
         // the new String likely creates a new copy of the string in memory which would be great
         // to avoid. All we need is a java.io.Readable here so maybe there is a more direct line to
         // that instead of going through a StringReader
         String json = new String( text.getBytes(), 0, text.getLength(), (Charset) context[ 2 ] );
 
         JsonReader reader = new JsonReader( new StringReader( json ));
 
         Fields fields = getSourceFields();
         Set<String> exports = getExportedFields();
 
         try {
             reader.beginObject();
 
             while (reader.hasNext()) {
                 String property = reader.nextName();
 
                 if(exports.contains(property)) {
                     String value = reader.nextString();
                     tuple.set(fields.getPos(property), value);
                 } else {
                     reader.skipValue();
                 }
 
             }
             reader.endObject();
             reader.close();
         } catch (IOException e) {
             throw new RuntimeException(e.getMessage());
         }
     }


 }
