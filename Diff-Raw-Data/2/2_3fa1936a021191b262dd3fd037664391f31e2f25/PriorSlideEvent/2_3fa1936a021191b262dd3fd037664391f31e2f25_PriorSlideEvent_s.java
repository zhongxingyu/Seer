 /**
  * 
  */
 package org.hypothesis.application.collector.events;
 
 import org.hypothesis.entity.Slide;
 
 /**
  * @author kamil
  *
  */
 @SuppressWarnings("serial")
 public class PriorSlideEvent extends AbstractRunningEvent implements HasName {
 
 	public PriorSlideEvent(Slide slide) {
 		super(slide);
 	}
 
 	public String getName() {
		return ProcessEvents.NextSlide;
 	}
 
 	public Slide getSlide() {
 		return (Slide) getSource();
 	}
 
 }
