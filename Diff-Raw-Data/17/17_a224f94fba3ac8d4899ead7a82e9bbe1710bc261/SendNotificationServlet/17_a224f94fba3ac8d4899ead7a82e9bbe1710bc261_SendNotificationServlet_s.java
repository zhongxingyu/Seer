 /**
  * Copyright 2012 Google Inc. All Rights Reserved. 
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.sheepdog.mashmesh.tasks;
 
 import com.google.api.client.util.Preconditions;
 import com.google.appengine.api.datastore.*;
 import com.googlecode.objectify.ObjectifyService;
 import com.sheepdog.mashmesh.PickupNotification;
 import com.sheepdog.mashmesh.Itinerary;
 import com.sheepdog.mashmesh.geo.GeocodeFailedException;
 import com.sheepdog.mashmesh.geo.GeocodeNotFoundException;
 import com.sheepdog.mashmesh.models.OfyService;
 import com.sheepdog.mashmesh.models.RideRecord;
 import com.sheepdog.mashmesh.models.UserProfile;
 import com.sheepdog.mashmesh.models.VolunteerProfile;
 import com.sheepdog.mashmesh.geo.GeoUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import javax.mail.MessagingException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class SendNotificationServlet extends HttpServlet {
     private static final DateTimeFormatter iso8601Parser = ISODateTimeFormat.dateTimeParser().withOffsetParsed();
 
     // TODO: Pull apart and refactor this method.
 
     @Override
     public void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws IOException {
         String patientEmail = req.getParameter("patientEmail");
         String appointmentAddress = req.getParameter("appointmentAddress");
         String appointmentTimeString = req.getParameter("appointmentTime");
         DateTime appointmentTime = iso8601Parser.parseDateTime(appointmentTimeString);
 
        GeoPt appointmentGeoPt = null;
         try {
             appointmentGeoPt = GeoUtils.geocode(appointmentAddress);
         } catch (GeocodeFailedException e) {
            e.printStackTrace();  // TODO
         } catch (GeocodeNotFoundException e) {
            e.printStackTrace();  // TODO
         }
 
         UserProfile patientProfile = UserProfile.getByEmail(patientEmail);
 
         Preconditions.checkNotNull(patientProfile);
 
         VolunteerProfile volunteerProfile = VolunteerProfile.getEligibleVolunteer(
                 patientProfile.getLocation(), appointmentGeoPt, appointmentTime);
 
         try {
             resp.setStatus(200);
             resp.setContentType("text/html");
 
             if (volunteerProfile == null) {
                 PickupNotification.sendFailureNotification(patientProfile, appointmentAddress, appointmentTime);
                 String html = PickupNotification.renderFailureNotification(patientProfile, appointmentAddress,
                         appointmentTime);
                 resp.getWriter().write(html);
                 return;
             }
 
             UserProfile volunteerUserProfile = volunteerProfile.getUserProfile();
             Itinerary itinerary = Itinerary.fetch(volunteerUserProfile.getAddress(), appointmentAddress,
                     patientProfile.getAddress(), appointmentTime.minusMinutes(5));
             PickupNotification pickupNotification = new PickupNotification(patientProfile, volunteerUserProfile,
                     itinerary, appointmentAddress);
 
             // TODO: Handle data races
             volunteerProfile.addAppointmentTime(itinerary.getDepartureTime(), itinerary.getArrivalTime());
             OfyService.ofy().put(volunteerProfile);
 
             pickupNotification.send();
 
             RideRecord rideRecord = new RideRecord();
             rideRecord.setVolunteerUserProfile(volunteerUserProfile.getKey());
             rideRecord.setVolunteerLocation(volunteerUserProfile.getLocation());
             rideRecord.setDepartureTime(itinerary.getDepartureTime());
 
             rideRecord.setPatientProfile(patientProfile.getKey());
             rideRecord.setPatientLocation(patientProfile.getLocation());
             rideRecord.setPickupTime(itinerary.getPickupTime());
 
             rideRecord.setArrivalTime(itinerary.getArrivalTime());
 
             rideRecord.setAppointmentAddress(appointmentAddress);
             rideRecord.setAppointmentLocation(appointmentGeoPt);
             rideRecord.setAppointmentTime(appointmentTime);
 
             rideRecord.setDistanceMiles(itinerary.getDistanceMiles());
 
             OfyService.ofy().put(rideRecord);
 
             String html = pickupNotification.renderTemplate(PickupNotification.VOLUNTEER_NOTIFICATION_TEMPLATE_PATH);
             resp.getWriter().write(html);
         } catch (URISyntaxException e) {
             e.printStackTrace(); // TODO
         } catch (MessagingException e) {
             e.printStackTrace(); // TODO
         }
     }
 }
