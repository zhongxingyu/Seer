 /*
  * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.pixmob.freemobile.netstat.gae.web.v1;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.pixmob.freemobile.netstat.gae.repo.DeviceNotFoundException;
 import org.pixmob.freemobile.netstat.gae.repo.DeviceStatRepository;
 
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.ibm.icu.util.Calendar;

 @Service
 /**
  * REST service handling daily device statistics.
  * @author Pixmob
  */
 public class DailyDeviceStatService {
     private final Logger logger = Logger.getLogger(DeviceService.class.getName());
     private final DeviceStatRepository dsr;
 
     @Inject
     DailyDeviceStatService(final DeviceStatRepository dsr) {
         this.dsr = dsr;
     }
 
     @Post
     public Reply<?> storeStats(Request req, @Named("id") String deviceId, @Named("date") String date) {
         logger.fine("Trying to store device statistics");
 
         final DateFormat df = new SimpleDateFormat("yyyyMMdd");
         final long d;
         try {
             d = df.parse(date).getTime();
         } catch (ParseException e) {
             logger.log(Level.WARNING, "Invalid date: " + date, e);
             return Reply.with("Invalid date: " + date).status(HttpServletResponse.SC_BAD_REQUEST);
         }
 
         final Stat s = req.read(Stat.class).as(Json.class);
         logger.fine("Received device statistics: " + s);
 
         final long total = s.timeOnFreeMobile + s.timeOnOrange;
 
         final Calendar cal = Calendar.getInstance();
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         cal.add(Calendar.DATE, -7);
         final long minAge = cal.getTimeInMillis();
 
         cal.setTimeInMillis(System.currentTimeMillis());
         cal.set(Calendar.HOUR_OF_DAY, 0);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
         cal.add(Calendar.DATE, 1);
         final long maxAge = cal.getTimeInMillis();
 
         if (logger.isLoggable(Level.FINE)) {
             logger.fine("total=" + total + ", minAge=" + minAge + ", maxAge=" + maxAge + ", d=" + d);
         }
         if (d < minAge) {
             logger.info("Discard old device statistics: " + s);
             return Reply.saying().ok();
         }
         if (total < 0 || total > 86400 * 1000 || d > maxAge) {
             logger.warning("Invalid daily device statistics: " + s);
             return Reply.saying().status(HttpServletResponse.SC_BAD_REQUEST);
         }
 
         try {
             dsr.update(deviceId, d, s.timeOnOrange, s.timeOnFreeMobile);
         } catch (DeviceNotFoundException e) {
             logger.log(Level.WARNING, "Failed to store device statistics", e);
             return Reply.with(e.getMessage()).notFound();
         }
 
         logger.info("Statistics stored");
 
         return Reply.saying().ok();
     }
 
     /**
      * JSON request parameters for device statistics.
      * @author Pixmob
      */
     private static class Stat {
         public long timeOnOrange;
         public long timeOnFreeMobile;
 
         @Override
         public String toString() {
             return "timeOnOrange=" + timeOnOrange + ", timeOnFreeMobile=" + timeOnFreeMobile;
         }
     }
 }
