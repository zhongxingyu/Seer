 package org.geworkbench.events;
 
 import java.util.List;
 
 import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
 import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
 import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
 import org.geworkbench.engine.config.events.Event;
 
 /**
  * <p>Event thrown when a row is selected on the Sequence Discovery Panel</p>
  * <p>This event is thrown when a row is selected on the patternTable by
  * the SequenceDiscoveryViewAppComponent</p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: Columbia Genomics Center</p>
  *
  * @author Saroja Hanasoge
  * @version $Id$
  */
@Deprecated // no one publishes this event anymore
 public class SequenceDiscoveryTableEvent extends Event {
 
     public SequenceDiscoveryTableEvent(List<DSMatchedPattern<DSSequence, CSSeqRegistration>> patternMatches) {
         super(null);
         this.patternMatches = patternMatches;
     }
 
     private List<DSMatchedPattern<DSSequence, CSSeqRegistration>> patternMatches = null;
 
     public List<DSMatchedPattern<DSSequence, CSSeqRegistration>> getPatternMatchCollection() {
         return patternMatches;
     }
 
 }
