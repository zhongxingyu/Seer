 package jDistsim.core.simulation.event.ui;
 
import jDistsim.core.simulation.event.description.IEventDescription;

 import java.awt.*;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 6.10.12
  * Time: 19:40
  */
 public interface UIEventPreview {
 
     public void display(Graphics2D graphics2D, int width, int height);
 
     public IEventDescription getEventDescription();
 }
