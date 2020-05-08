 package org.kohsuke.stapler.nvd3;
 
 import org.kohsuke.stapler.HttpResponse;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 import org.kohsuke.stapler.export.DataWriter;
 import org.kohsuke.stapler.export.Flavor;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * {@link HttpResponse} representation of the data model NVD3 wants.
  *
  * This allows you to write a web method like the following
  * to serve data for charting:
  *
  * <pre>
  * public Data&lt;XYValue> doData() {
  *     return new Data().with(new Series("abc") ..);
  * }
  *
  * @author Kohsuke Kawaguchi
  */
 public class Data<V extends Value> implements HttpResponse {
     public final List<Series<V>> data = new ArrayList<Series<V>>();
 
     public Data(Series<V>... data) {
         with(data);
     }
 
    public Data<V> with(Series<V>... data) {
         this.data.addAll(Arrays.asList(data));
         return this;
     }
 
    public Data<V> with(Series<V> s) {
         this.data.add(s);
         return this;
     }
 
     public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object o) throws IOException, ServletException {
         DataWriter w = Flavor.JSON.createDataWriter(this, rsp);
         w.startArray();
         for (Series<V> d : data) {
             d.writeTo(w);
         }
         w.endArray();
     }
 }
