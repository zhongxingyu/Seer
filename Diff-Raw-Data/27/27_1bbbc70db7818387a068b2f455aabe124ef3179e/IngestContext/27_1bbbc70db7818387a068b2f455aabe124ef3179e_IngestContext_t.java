 package dk.statsbiblioteket.digitv.youseeingester;
 
/**
 * @author jrg
 */
 public class IngestContext {
     private String filename;
     private String starttime;
     private String stoptime;
     private String channelid;
 
     public IngestContext() {
     }
 
     public void setFilename(String filename) {
         this.filename = filename;
     }
 
     public void setStarttime(String starttime) {
         this.starttime = starttime;
     }
 
     public void setStoptime(String stoptime) {
         this.stoptime = stoptime;
     }
 
     public void setChannelid(String channelid) {
         this.channelid = channelid;
     }
 
     public String getFilename() {
         return filename;
     }
 
     public String getStarttime() {
         return starttime;
     }
 
     public String getStoptime() {
         return stoptime;
     }
 
     public String getChannelid() {
         return channelid;
     }
 }
