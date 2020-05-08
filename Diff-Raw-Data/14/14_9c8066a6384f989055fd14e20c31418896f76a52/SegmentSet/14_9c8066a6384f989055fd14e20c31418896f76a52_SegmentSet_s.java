 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package etc.aloe.data;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import weka.core.Attribute;
 import weka.core.DenseInstance;
 import weka.core.Instance;
 import weka.core.Instances;
 
 /**
  *
  * @author michael
  */
 public class SegmentSet {
     public static final String DURATION_ATTR_NAME = "duration";
     public static final String LENGTH_ATTR_NAME = "length";
     public static final String CPS_ATTR_NAME = "cps";
     public static final String RATE_ATTR_NAME = "rate";
 
     private List<Segment> segments = new ArrayList<Segment>();
 
     /**
      * Add a segment to the set.
      *
      * @param segment
      */
     public void add(Segment segment) {
         this.segments.add(segment);
     }
 
     /**
      * Get the size of the segment set.
      *
      * @return
      */
     public int size() {
         return this.segments.size();
     }
 
     public List<Segment> getSegments() {
         return segments;
     }
 
     public void setSegments(List<Segment> segments) {
         this.segments = segments;
     }
 
     public ExampleSet getBasicExamples() {
         ArrayList<Attribute> attributes = new ArrayList<Attribute>();
 
         attributes.add(new Attribute(ExampleSet.ID_ATTR_NAME));
         attributes.add(new Attribute(ExampleSet.MESSAGE_ATTR_NAME, (List<String>) null));
         attributes.add(new Attribute(ExampleSet.LABEL_ATTR_NAME, Arrays.asList(new String[]{"false", "true"})));
         attributes.add(new Attribute(DURATION_ATTR_NAME));
         attributes.add(new Attribute(LENGTH_ATTR_NAME));
         attributes.add(new Attribute(CPS_ATTR_NAME));
         attributes.add(new Attribute(RATE_ATTR_NAME));
 
         Instances instances = new Instances("BasicExamples", attributes, 0);
         instances.setClassIndex(2);
 
         Attribute idAttr = instances.attribute(ExampleSet.ID_ATTR_NAME);
         Attribute messageAttr = instances.attribute(ExampleSet.MESSAGE_ATTR_NAME);
         Attribute labelAttr = instances.attribute(ExampleSet.LABEL_ATTR_NAME);
        Attribute duration = instances.attribute(DURATION_ATTR_NAME);
        Attribute length = instances.attribute(LENGTH_ATTR_NAME);
        Attribute cps = instances.attribute(CPS_ATTR_NAME);
        Attribute rate = instances.attribute(RATE_ATTR_NAME);
 
 
         for (int i = 0; i < size(); i++) {
             Segment segment = get(i);
            Instance instance = new DenseInstance(3);
 
             String messageStr = segment.concatMessages();
 
             instance.setValue(idAttr, segment.getId());
             instance.setValue(messageAttr, messageStr);
             if (segment.hasTrueLabel()) {
                 instance.setValue(labelAttr, segment.getTrueLabel() ? "true" : "false");
             }
 
            computeRateValues(segment, instance, messageStr, duration, length, cps, rate);
 
             instances.add(instance);
         }
 
         return new ExampleSet(instances);
     }
 
     public Segment get(int i) {
         return this.segments.get(i);
     }
 
     public SegmentSet onlyLabeled() {
         SegmentSet labeled = new SegmentSet();
         for (Segment segment : segments) {
             if (segment.hasTrueLabel()) {
                 labeled.add(segment);
             }
         }
         return labeled;
     }
 
     private void computeRateValues(Segment segment, Instance instance, String messageStr, Attribute durationAttr, Attribute lengthAttr, Attribute cpsAttr, Attribute rateAttr) {
         double duration = segment.getDurationInSeconds();
         double length = segment.getMessages().size();
 
         //If the length is 1, then we correct the duration.
         //Assume average typing speed (35 words per minute, 5 char/word)
         if (length <= 1) {
             double averageCharPerSecond = 35.0 * 5.0 / 60.0;
             //[seconds] = [chars] / ([chars]/[seconds])
             duration = messageStr.length() / averageCharPerSecond;
         }
 
         if (duration > 100000) {
             System.err.println("Wacky segment id: " + segment.getId() + " has duration: " + duration);
         }
 
         double cps = messageStr.length() / duration;
         double rate = segment.getMessages().size() / duration;
 
         instance.setValue(durationAttr, duration);
         instance.setValue(lengthAttr, length);
         instance.setValue(cpsAttr, cps);
         instance.setValue(rateAttr, rate);
     }
 }
