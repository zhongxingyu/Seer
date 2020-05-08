 package allreg.logs;
 
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * Formats a lead source tally into an URL suitable for sending to the google
  * chart service.
  * 
  * @author orion
  */
 public class GoogleBarChartURLLeadSourceTallySink implements LeadSourceTallySink {
 
   private final GoogleChartConfig cfg;
   private String chartURL;
   
   public GoogleBarChartURLLeadSourceTallySink(GoogleChartConfig cfg) {
     this.cfg = cfg;
   }
 
   public void finalLeadSourceTally(Set<LeadSourceDescriptor> descriptors) {
    String dataString = "t:";
     String legendString = "";
     String colorString = "";
     
     Set<LeadSourceDescriptor> sortedDescriptors = new TreeSet<LeadSourceDescriptor>(new Comparator<LeadSourceDescriptor>() {
       
       public int compare(LeadSourceDescriptor lsd1, LeadSourceDescriptor lsd2) {
         // XXX: I'm sure there's a more concise way to express this.
         if (lsd1.getCount() < lsd2.getCount()) {
           return 1;
         } else if (lsd1.getCount() > lsd2.getCount()) {
           return -1;
         } else {
           return 0;
         }
       }
     });
     sortedDescriptors.addAll(descriptors);
     for (Iterator<LeadSourceDescriptor> i=sortedDescriptors.iterator(); i.hasNext();) {
       LeadSourceDescriptor descriptor = i.next();
       legendString += descriptor.getLabel();
       dataString += descriptor.getCount();
       colorString += descriptor.getColor();
       if (i.hasNext()) {
         dataString += ",";
         legendString += "|";
         colorString += "|";
       }
     }
     
         
     chartURL = GoogleChartConfig.BASE_URL;
     // set the dimensions
     chartURL += "?cht=bvg&chs=" + cfg.dimensions;
     // set the chart title
     chartURL += "&chtt=Lead%20Sources";
     // turn on range labels
     chartURL += "&chxt=y";
     // set the legends
     chartURL += "&chdl=" + legendString;
     // set the series colors
     chartURL += "&chco=" + colorString;
     // set the series data
    chartURL += "&chd="+dataString; 
   }
 
   public String getChartURL() {
     return chartURL;
   }
   
 }
