 package com.flat502.rox.demo;
 
 import java.util.Date;
 
 /**
  * An example of a simple "struct" class used by some of the demo's.
  * <P>
  * This class is used to illustrate RoX's ability to marshal POJOs
  * directly.
  */
 public class TimeInfo {
	public Date today;
	public String info;
	public TimeInfo[] array;
 	public String toString() {
 		return "TimeInfo[today=" + today + ", info=" + info + "]";
 	}
 }
