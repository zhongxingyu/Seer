 /**
  * Copyright (C) 2011-2013 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.util.value.impl;
 
 import static com.barchart.util.value.impl.ValueBuilder.*;
 
 import com.barchart.util.value.api.Time;
 import com.barchart.util.value.api.TimeInterval;
 
 public class DefTimeInterval implements TimeInterval {
 
 	final long start;
 	final long stop;
 
 	DefTimeInterval(final Time start, final Time stop) {
 		this.start = start.millisecond();
 		this.stop = stop.millisecond();
 	}
 
 	DefTimeInterval(final long start, final long stop) {
 		this.start = start;
 		this.stop = stop;
 	}
 
 	@Override
 	public Time start() {
 		return newTime(start);
 	}
 
 	@Override
 	public Time stop() {
 		return newTime(stop);
 	}
 
 	@Override
 	public boolean equals(final Object o) {
 
 		if (o == null) {
 			return false;
 		}
 
 		if (this == o) {
 			return true;
 		}
 
 		if (!(o instanceof TimeInterval)) {
 			return false;
 		}
 
 		final TimeInterval tI = (TimeInterval) o;
 
		return (start() == tI.start()) && (stop() == tI.stop());
 
 	}
 
 	@Override
 	public TimeInterval copy() {
 		return this;
 	}
 
 }
