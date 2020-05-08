 package pegasus.esp;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.joda.time.DateTime;
 
 import pegasus.esp.data.ValueStreams;
 import pegasus.eventbus.client.Envelope;
 
 import com.espertech.esper.client.EventBean;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class EnvelopeCounter extends EventMonitor {
 
     public interface EnvelopeRetriever {
         public String retrieve(Envelope e);
         public String key();
     }
 
     Collection<EnvelopeRetriever> counters = Lists.newArrayList();
     private Map<String, ValueStreams> dc = Maps.newHashMap();
 
     public EnvelopeCounter() {
 
         counters.add(new EnvelopeRetriever() {
             public String retrieve(Envelope e) { return e.getEventType(); }
             public String key() { return "EventType"; }
         });
 
         counters.add(new EnvelopeRetriever() {
             public String retrieve(Envelope e) { return e.getTopic(); }
             public String key() { return "Topic"; }
         });
 
         for (EnvelopeRetriever envelopeRetriever : counters) {
             String type = envelopeRetriever.key();
             dc.put(type, new ValueStreams(type));
         }
     }
 
     public void dumpFreqs() {
         for (EnvelopeRetriever counter : counters) {
             String type = counter.key();
             dumpFreq(type);
         }
     }
 
     public void dumpFreq(String type) {
         System.out.println(type + ":");
         ValueStreams stream = dc.get(type);
         for (String cat : stream.getActiveRanges()) {
             for (String item : stream.getValues()) {
                 System.out.println("  " + cat + "(" + item + ")=" + stream.getActiveRange(cat, item).getTotal());
             }
         }
         System.out.println();
     }
 
     @Override
     public InferredEvent receive(EventBean eventBean) {
         Envelope env = (Envelope) eventBean.get("env");
         recordOccurrences(env);
         return null;
     }
 
     private void recordOccurrences(Envelope env) {
         for (EnvelopeRetriever counter : counters) {
             String type = counter.key();
             String item = counter.retrieve(env);
            dc.get(type).addValue(item, env.getTimestamp().getTime(), 1);
         }
     }
 
     @Override
     public void registerPatterns(EventStreamProcessor esp) {
         esp.monitor(true, getPattern(), this);
     }
 
     private String getPattern() {
         return "select env from Envelope as env";
     }
 }
