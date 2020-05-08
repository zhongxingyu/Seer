 package propinquity.hardware;
 
import java.util.*;

import processing.core.*;
import processing.serial.*;
import controlP5.*;

import com.rapplogic.xbee.api.*;

import propinquity.*;

 public interface ProxEventListener {
 
 	public void proxEvent(Glove glove);
 
 }
