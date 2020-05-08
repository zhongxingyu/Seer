 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans.module.atom;
 
 import org.mule.ibeans.api.application.Receive;
 import org.mule.ibeans.api.application.Schedule;
 import org.mule.ibeans.channels.ATOM;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.inject.Singleton;
 
 import org.apache.abdera.model.Entry;
 
 @Singleton
 public class AtomSplitFeedWithLastUpdate
 {
     private AtomicInteger count = new AtomicInteger(0);
 
    @Schedule(interval = 2000)
     @Receive(uri = "atom:http://rossmason.blogspot.com/feeds/posts/default", properties = ATOM.LAST_UPDATE_DATE + "=2009-03-01")
     public void readFeed(Entry entry) throws Exception
     {
         count.getAndIncrement();
     }
 
     public int getCount()
     {
         return count.get();
     }
 }
