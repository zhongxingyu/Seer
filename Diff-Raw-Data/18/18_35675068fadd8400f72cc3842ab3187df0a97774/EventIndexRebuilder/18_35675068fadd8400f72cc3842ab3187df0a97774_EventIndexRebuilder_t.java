 /*****************************************************************************
  * 
  * Copyright (C) Zenoss, Inc. 2011, all rights reserved.
  * 
  * This content is made available according to terms specified in
  * License.zenoss under the directory where your Zenoss product is installed.
  * 
  ****************************************************************************/
 
 
 package org.zenoss.zep.index;
 
 /**
  * Interface used to trigger a rebuild of the event index when configuration changes.
  */
 public interface EventIndexRebuilder {
     /**
     * Initializes the EventIndexRebuilder.
     */
    public void init();

    /**
      * Shuts down the index rebuilder.
      *
      * @throws InterruptedException If the rebuilder is interrupted while shutting down.
      */
     public void shutdown() throws InterruptedException;
 }
