 /*
  * This file is part of ALOE.
  *
  * ALOE is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * ALOE is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with ALOE.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2012 SCCL, University of Washington (http://depts.washington.edu/sccl)
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
  * Represents a collection of segments. Knows how to transform itself into Weka
  * Instances with basic feature information.
  *
  * @author Michael Brooks <mjbrooks@uw.edu>
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
 
     /**
      * Get the underlying list of segments.
      *
      * @return
      */
     public List<Segment> getSegments() {
         return segments;
     }
 
     /**
      * Set the underlying list of segments.
      *
      * @param segments
      */
     public void setSegments(List<Segment> segments) {
         this.segments = segments;
     }
 
     /**
      * Convert the segment set into an ExampleSet (ready for feature
      * extraction). The returned example set includes an id attribute, the
      * message text, a label attribute, and several basic features extracted
      * from the segment.
      *
      * @return
      */
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
         Attribute durationAttr = instances.attribute(DURATION_ATTR_NAME);
         Attribute lengthAttr = instances.attribute(LENGTH_ATTR_NAME);
         Attribute cpsAttr = instances.attribute(CPS_ATTR_NAME);
         Attribute rateAttr = instances.attribute(RATE_ATTR_NAME);
 
 
         for (int i = 0; i < size(); i++) {
             Segment segment = get(i);
             Instance instance = new DenseInstance(instances.numAttributes());
 
             String messageStr = segment.concatMessages();
 
             instance.setValue(idAttr, segment.getId());
             instance.setValue(messageAttr, messageStr);
             if (segment.hasTrueLabel()) {
                 instance.setValue(labelAttr, segment.getTrueLabel() ? "true" : "false");
             }
 
             computeRateValues(segment, instance, messageStr, durationAttr, lengthAttr, cpsAttr, rateAttr);
 
             instances.add(instance);
         }
 
         return new ExampleSet(instances);
     }
 
     /**
      * Get the ith segment.
      *
      * @param i
      * @return
      */
     public Segment get(int i) {
         return this.segments.get(i);
     }
 
     /**
      * Return a new segment set containing only the labeled segments.
      *
      * @return
      */
     public SegmentSet onlyLabeled() {
         SegmentSet labeled = new SegmentSet();
         for (Segment segment : segments) {
             if (segment.hasTrueLabel()) {
                 labeled.add(segment);
             }
         }
         return labeled;
     }
 
     /**
      * Computes the basic timing-related features about a segment and applies
      * them to the given instance.
      *
      * @param segment
      * @param instance
      * @param messageStr
      * @param durationAttr
      * @param lengthAttr
      * @param cpsAttr
      * @param rateAttr
      */
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
 
     /**
      * Counts the number of segments that have the given label (true, false, or
      * null).
      *
      * @param label
      * @return
      */
     public int getCountWithTrueLabel(Boolean label) {
         int count = 0;
         for (Segment segment : segments) {
             if (segment.getTrueLabel() == label) {
                 count++;
             }
         }
         return count;
     }
 }
