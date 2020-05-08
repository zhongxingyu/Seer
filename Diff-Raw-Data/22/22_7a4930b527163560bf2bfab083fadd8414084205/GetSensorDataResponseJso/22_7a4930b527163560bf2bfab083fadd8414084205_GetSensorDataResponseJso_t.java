 package nl.sense_os.commonsense.client.viz.data;
 
 import nl.sense_os.commonsense.client.viz.data.timeseries.BackEndDataPoint;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.core.client.JsArray;
 
 public class GetSensorDataResponseJso extends JavaScriptObject {
 
     protected GetSensorDataResponseJso() {
         // empty protected constructor
     }
 
     /**
      * Creates a GetSensorDataResponseJso from the JSON response from CommonSense. Also converts any
      * "embedded" JSON-disguised-as-String objects, so that e.g.
      * <code>{"foo":"{\"bar\":\"baz\"}"}</code> will get completely converted.
      * 
      * @param source
      *            Raw response from CommonSense.
      * @return JavaScriptObject representing the response.
      */
     public final static native GetSensorDataResponseJso create(String source) /*-{
         function stripslashes(str) {
             return (str + '').replace(/\\(.?)/g, function(s, n1) {
                 switch (n1) {
                 case '\\':
                     return '\\';
                 case '0':
                     return '\u0000';
                 case '':
                     return '';
                 default:
                     return n1;
                 }
             });
         }
         var stripped = stripslashes(source);
         var jsonFixed = stripped.replace(/:\"{/g, ':{').replace(/}\"/g, '}');
         return eval('(' + jsonFixed + ')');
     }-*/;
 
    /**
     * @return The 'data' property of the response, containing an array with sensor data. If 'data'
     *         is undefined, an empty array is returned.
     */
     public final native JsArray<BackEndDataPoint> getData() /*-{
        if (undefined != this.data) {
            return this.data;
        } else {
            return [];
        }
     }-*/;
 
    /**
     * @return The 'total' property of the response. If 'total' is undefined, -1 is returned.
     */
     public final native int getTotal() /*-{
        if (undefined != this.total) {
            return this.total;
        } else {
            return -1;
        }
     }-*/;
 }
