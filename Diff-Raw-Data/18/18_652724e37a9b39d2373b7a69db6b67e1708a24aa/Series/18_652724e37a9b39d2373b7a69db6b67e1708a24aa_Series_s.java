 package org.kohsuke.stapler.nvd3;
 
 import org.kohsuke.stapler.export.DataWriter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * A single sequence of data points.
  *
  * @author Kohsuke Kawaguchi
  */
 public class Series<V extends Value> {
     /**
      * Human readable title of this data series.
      */
     public String key;
 
     /**
      * Optional color.
      */
     public String color;
 
     public final List<V> values = new ArrayList<V>();
 
     public Series(String key) {
         this.key = key;
     }
     
    public Series with(V... data) {
         this.values.addAll(Arrays.asList(data));
         return this;
     }
 
    public Series color(String c) {
         this.color = c;
         return this;
     }
 
    public Series with(V s) {
         this.values.add(s);
         return this;
     }
 
     protected void writeTo(DataWriter w) throws IOException {
         w.startObject();
         w.name("key");
         w.value(key);
 
         if (color!=null) {
             w.name("color");
             w.value(color   );
         }
 
         w.name("values");
         w.startArray();
         for (V v : values) {
             v.writeTo(w);
         }
         w.endArray();
         w.endObject();
     }
 }
