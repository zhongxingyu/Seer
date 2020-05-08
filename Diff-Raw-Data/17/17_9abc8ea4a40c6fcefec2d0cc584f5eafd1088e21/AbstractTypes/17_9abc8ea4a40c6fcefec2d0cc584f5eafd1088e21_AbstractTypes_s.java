 /**
  * @(#)AbstractTypes.java
  */
 
 package aurora;
 
 /**
  * Basic type constants used by Aurora.
  * @author Alex Kurzhanskiy
  * @version $Id: AbstractTypes.java 38 2010-02-08 22:59:00Z akurzhan $
  */
 public abstract class AbstractTypes {
 	public final static int MASK_NETWORK = 1073741824;
 	public final static int MASK_NODE = 536870912;
 	public final static int MASK_LINK = 268435456;
 	public final static int MASK_MONITOR = 134217728;
 	public final static int MASK_MONITOR_CONTROLLER = MASK_MONITOR | 67108864;
 	public final static int MASK_MONITOR_EVENT = MASK_MONITOR | 33554432;
 	public final static int MASK_MONITOR_ZIPPER = MASK_MONITOR | 16777216;
 	public final static int MASK_SENSOR  = 8388608;
 	
 }
