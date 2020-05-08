 package com.github.adeshmukh.ps4j;
 
 import java.util.Collection;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 /**
  * Record contains the Measures for a single VM.
  *
  * @author adeshmukh
  */
 public class Record {
 
     /**
      * Special instance indicating that the Record does not contain any useable data.
      */
     public static final Record NOOP = new Record();
 
     private Collection<Measure<?>> measures;
 
 	private Record() {
         measures = Lists.newArrayList();
 	}
 
     /**
      * Factory method.
      *
      * @return
      */
 	public static Record create() {
 		return new Record();
 	}
 
     /**
      * Add a Collection of Measure instances to this record. Expected to be invoked by a Meter.
      *
      * @param measures
      * @return
      */
     public Record addAll(Iterable<? extends Measure<?>> measures) {
         Iterables.addAll(this.measures, measures);
         return this;
 	}
 
     /**
      * Return an ImmutableList of Measures in this record. Expected to be invoked after all Meters have finished adding
      * the measures.
      * 
      * @return
      */
     public ImmutableList<? extends Measure<?>> getMeasures() {
         return ImmutableList.copyOf(measures);
     }
 
     @Override
     public String toString() {
         return measures.toString();
     }
 }
