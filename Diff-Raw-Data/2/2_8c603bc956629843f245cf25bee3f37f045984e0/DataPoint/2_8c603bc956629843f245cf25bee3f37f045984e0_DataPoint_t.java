 package nl.sense_os.commonsense.client.common.json.overlays;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 import java.util.Date;
 
 public class DataPoint extends JavaScriptObject {
 
     protected DataPoint() {
         // empty protected constructor
     }
 
     protected final native double getRawDate() /*-{
 		return this.date;
     }-*/;
 
     public final native String getRawValue() /*-{
 		return '' + this.value;
     }-*/;
 
     public final Date getTimestamp() {
        return new Date(Math.round(this.getRawDate()*1000));
     }
 }
