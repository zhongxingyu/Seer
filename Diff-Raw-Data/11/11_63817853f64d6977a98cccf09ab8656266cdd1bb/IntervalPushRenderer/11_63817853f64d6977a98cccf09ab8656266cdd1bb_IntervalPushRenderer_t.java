 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.demo.auction.view.controllers;
 
 import org.icefaces.application.PortableRenderer;
 import org.icefaces.application.PushRenderer;
 import org.icefaces.demo.auction.view.util.FacesUtils;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.faces.bean.ApplicationScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.FacesContext;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The interval render setup up a timer task that is responsible for initiating
  * the server side push at set intervals. The interval is configurable with
  * the context parameter org.icefaces.sample.auction.interval.
  * <p/>
  * The timer will shutdown its self when there server shuts down via @PreDestroy.
  *
  * @author ICEsoft Technologies Inc.
  * @since 2.0
  */
 @ManagedBean(eager = true)
 @ApplicationScoped
 public class IntervalPushRenderer {
 
     private static Logger log = Logger.getLogger(IntervalPushRenderer.class.getName());
 
     public static final String INTERVAL_RENDER_GROUP = "auctionInterval";
 
    private int pollingInterval = 1000;
    {
         String interval = FacesUtils.getFacesParameter(
                 "org.icefaces.sample.auction.interval");
         if (interval != null) {
             try {
                pollingInterval = Integer.parseInt(interval);
             } catch (NumberFormatException e) {
                 log.log(Level.WARNING, "Error applying org.icefaces.demo.auction.interval, " +
                         "must be valid integer.", e);
             }
         }
 
     }
 
     private Timer intervalTimer;
     private TimerTask renderTask;
 
     /**
      * Constructs an new instance of the intervalTimer and sets up the timer
      * task.  The timer tasks main goal is to flush the service layer cache
      * as well as initiate a server push the render group INTERVAL_RENDER_GROUP.
      */
     public IntervalPushRenderer() {
 
         final PortableRenderer renderer = PushRenderer.getPortableRenderer();
 
         renderTask = new TimerTask() {
             public void run() {
                 try {
                     // NOTE: this thread doesn't 
 
                     // make the push call to everyone in the interval group
                     renderer.render(INTERVAL_RENDER_GROUP);
                     if (log.isLoggable(Level.FINEST)) {
                         log.finest("Render done for 'auction' using " + intervalTimer);
                     }
                 } catch (Throwable e) {
                     log.log(Level.WARNING, "Error running interval timer task.", e);
                 }
             }
         };
     }
 
     /**
      * Starts our interval timer task. Intended to keep running until this
      * Application scoped bean is shutdown by the server.
      */
     @PostConstruct
     public void initializeIntervalRender() {
         if (null != intervalTimer) {
             intervalTimer.cancel();
         }
         intervalTimer = new Timer(true);
        intervalTimer.schedule(renderTask, 0, pollingInterval);
     }
 
     /**
      * Stops the interval timer, generally only for server shutdown.
      */
     @PreDestroy
     public void cleanup() {
         if (null != intervalTimer) {
             intervalTimer.purge();
             intervalTimer.cancel();
         }
         if (log.isLoggable(Level.FINEST)) {
             log.finest("cleaning up " + intervalTimer);
         }
     }
 }
