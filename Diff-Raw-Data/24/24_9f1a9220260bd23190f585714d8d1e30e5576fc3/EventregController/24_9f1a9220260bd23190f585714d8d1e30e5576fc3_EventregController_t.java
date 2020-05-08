 /* ********************************************************************
 Licensed to Jasig under one or more contributor license
 agreements. See the NOTICE file distributed with this work
 for additional information regarding copyright ownership.
 Jasig licenses this file to you under the Apache License,
 Version 2.0 (the "License"); you may not use this file
 except in compliance with the License. You may obtain a
 copy of the License at:
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on
 an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied. See the License for the
 specific language governing permissions and limitations
 under the License.
  */
 
 package org.bedework.eventreg.web;
 
 import org.bedework.eventreg.bus.SessionManager;
 import org.bedework.eventreg.db.Event;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author douglm
  *
  */
 public class EventregController implements Controller {
   protected final Log logger = LogFactory.getLog(getClass());
   private SessionManager sessMan;
 
   @Override
   public ModelAndView handleRequest(final HttpServletRequest request,
                                     final HttpServletResponse response) throws Exception {
     try {
       sessMan.setMessage("");
       Event currEvent = sessMan.getCurrEvent();
 
       int maxTicketsAllowed = currEvent.getMaxTickets();
      if (maxTicketsAllowed < 0) {
        sessMan.setMessage("Cannot register for this event.");
        Map myModel = new HashMap();
        myModel.put("sessMan", sessMan);

        return new ModelAndView("error", myModel);
      }

      int numTicketsRequested = sessMan.getTicketsRequested();
       long currentTicketCount = sessMan.getTicketCount();
 
       logger.debug("event registration start");
 
       if ((numTicketsRequested + currentTicketCount) > maxTicketsAllowed) {
         sessMan.setRegistrationFull(true);
       }
 
       DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       Date deadline = formatter.parse(sessMan.getCurrEvent().getRegistrationEnd());
       Date now = new Date();
 
       if (now.before(deadline)) {
         sessMan.setDeadlinePassed(false);
       } else {
         sessMan.setDeadlinePassed(true);
       }
 
       if (sessMan.getRegistrationFull()) {
         logger.debug("event registration stop - registration is full");
         Map myModel = new HashMap();
         myModel.put("sessMan", sessMan);
 
         return new ModelAndView("init", myModel);
       }
 
       if (sessMan.getDeadlinePassed()) {
         logger.debug("event registration stop - deadline has passed");
         Map myModel = new HashMap();
         myModel.put("sessMan", sessMan);
 
         return new ModelAndView("init", myModel);
       }
 
       if ((numTicketsRequested > currEvent.getMaxTicketsPerUser()) &&
           !sessMan.getSuperUser()) {
         sessMan.setMessage("Number of tickets requested exceeds number of tickets allowed.");
         logger.debug("Number of tickets requested exceeds number of tickets allowed.");
         Map myModel = new HashMap();
         myModel.put("sessMan", sessMan);
 
         return new ModelAndView("error", myModel);
       }
 
       String comment = request.getParameter("comment");
       String email = request.getParameter("email");
       String regType = request.getParameter("regType");
 
       logger.debug("event registration  - number of tickets requested: " + numTicketsRequested);
       logger.debug("event registration  - superuser: " + sessMan.getSuperUser());
 
       sessMan.registerUserInEvent(numTicketsRequested,
                                   email,
                                   comment,
                                   regType,
                                   sessMan.getSuperUser());
       Map myModel = new HashMap();
 
       myModel.put("sessMan", sessMan);
 
       return new ModelAndView("eventreg", myModel);
     } catch (Exception e) {
       logger.error(this, e);
       throw e;
     } catch (Throwable t) {
       logger.info(t);
       sessMan.setMessage(t.getMessage());
       throw new Exception(t);
     } finally {
       sessMan.closeDb();
     }
   }
 
   /**
    * @param sm
    */
   public void setSessionManager(final SessionManager sm) {
     sessMan = sm;
   }
 }
