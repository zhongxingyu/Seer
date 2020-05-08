 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 /*
  * Created on Mar 26, 2004
  */
 package com.sapienter.jbilling.server.process;
 
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 
 import javax.naming.NamingException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.log4j.Logger;
 
 import sun.jdbc.rowset.CachedRowSet;
 
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.server.invoice.InvoiceBL;
 import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
 import com.sapienter.jbilling.server.notification.INotificationSessionBean;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.notification.NotificationBL;
 import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
 import com.sapienter.jbilling.server.order.OrderBL;
 import com.sapienter.jbilling.server.order.db.OrderDAS;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.process.db.AgeingEntityStepDAS;
 import com.sapienter.jbilling.server.process.db.AgeingEntityStepDTO;
 import com.sapienter.jbilling.server.process.event.NewUserStatusEvent;
 import com.sapienter.jbilling.server.system.event.EventManager;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.user.db.CompanyDTO;
 import com.sapienter.jbilling.server.user.db.UserDAS;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 import com.sapienter.jbilling.server.user.db.UserStatusDAS;
 import com.sapienter.jbilling.server.user.db.UserStatusDTO;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.Context;
 import com.sapienter.jbilling.server.util.PreferenceBL;
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 import org.springframework.dao.EmptyResultDataAccessException;
 
 /**
  * @author Emil
  */
 public class AgeingBL {
 
     private AgeingEntityStepDAS ageingDas = null;
     private AgeingEntityStepDTO ageing = null;
     private static final Logger LOG = Logger.getLogger(AgeingBL.class);
     private EventLogger eLogger = null;
 
     public AgeingBL(Integer ageingId) {
         init();
         set(ageingId);
     }
 
     public AgeingBL() {
         init();
     }
 
     private void init() {
         eLogger = EventLogger.getInstance();        
         ageingDas = new AgeingEntityStepDAS();
 
     }
 
     public AgeingEntityStepDTO getEntity() {
         return ageing;
     }
     
     public void set(Integer id) {
         ageing = ageingDas.find(id);
     }
 
     public void setUserStatus(Integer executorId, Integer userId, 
             Integer statusId, Date today) {
         // find out if this user is not already in the required status
         UserBL user = new UserBL(userId);
         Integer originalStatusId = user.getEntity().getStatus().getId();
         if (originalStatusId.equals(statusId)) {
             return;
         }
          
         LOG.debug("Setting user " + userId + " status to " + statusId);
         // see if this guy could login in her present status
         boolean couldLogin = user.getEntity().getStatus().getCanLogin() == 1;
         
         // log an event
         if (executorId != null) {
             // this came from the gui
             eLogger.audit(executorId, userId, Constants.TABLE_BASE_USER, 
                     user.getEntity().getUserId(), 
                     EventLogger.MODULE_USER_MAINTENANCE, 
                     EventLogger.STATUS_CHANGE, 
                     user.getEntity().getStatus().getId(), null, null);
         } else {
             // this is from a process, no executor involved
             eLogger.auditBySystem(user.getEntity().getEntity().getId(), userId,
                     Constants.TABLE_BASE_USER, 
                     user.getEntity().getUserId(), 
                     EventLogger.MODULE_USER_MAINTENANCE, 
                     EventLogger.STATUS_CHANGE, 
                     user.getEntity().getStatus().getId(), null, null);
         }
         
         // make the notification
         NotificationBL notification = new NotificationBL();
         try {
             MessageDTO message = notification.getAgeingMessage(
                     user.getEntity().getEntity().getId(), 
                     user.getEntity().getLanguageIdField(), statusId, userId);
      
             INotificationSessionBean notificationSess = 
                     (INotificationSessionBean) Context.getBean(
                     Context.Name.NOTIFICATION_SESSION);
             notificationSess.notify(user.getEntity(), message);
         } catch (NotificationNotFoundException e) {
             LOG.warn("Changeing the satus of a user. An ageing notification " +
                     "should be " +
                     "sent to the user, but the entity doesn't have it. " +
                     "entity " + user.getEntity().getEntity().getId());
         }
  
 
         // make the change
         UserStatusDTO status = new UserStatusDAS().find(
                 statusId);
         user.getEntity().setUserStatus(status);
         user.getEntity().setLastStatusChange(today);
         if (status.getId() == UserDTOEx.STATUS_DELETED) {
             // yikes, it's out
             user.delete(executorId);
             return; // her orders were deleted, no need for any change in status
         }
         
         // see if this new status is suspended
         if (couldLogin && status.getCanLogin() == 0) {
             // all the current orders have to be suspended
             OrderDAS orderDas = new OrderDAS();
             OrderBL order = new OrderBL();
             for (Iterator it = orderDas.findByUser_Status(userId, 
                     Constants.ORDER_STATUS_ACTIVE).iterator(); 
                     it.hasNext();) {
                 OrderDTO orderRow = (OrderDTO) it.next();
                 order.set(orderRow);
                 order.setStatus(executorId, 
                         Constants.ORDER_STATUS_SUSPENDED_AGEING);
             }               
         } else if (!couldLogin && status.getCanLogin() == 1) {
             // the oposite, it is getting out of the ageing process
             // all the suspended orders have to be reactivated
             OrderDAS orderDas = new OrderDAS();
             OrderBL order = new OrderBL();
             for (Iterator it = orderDas.findByUser_Status(userId, 
                     Constants.ORDER_STATUS_SUSPENDED_AGEING).iterator(); 
                     it.hasNext();) {
                 OrderDTO orderRow = (OrderDTO) it.next();
                 order.set(orderRow);
                 order.setStatus(executorId, Constants.ORDER_STATUS_ACTIVE);
             }               
         }
         
         // make the http call back
         String url = null;
         try {
             PreferenceBL pref = new PreferenceBL();
             pref.set(user.getEntity().getEntity().getId(), 
                     Constants.PREFERENCE_URL_CALLBACK);
             url = pref.getString();
         } catch (EmptyResultDataAccessException e2) {
             // no call then
         }
         
         if (url != null && url.length() > 0) {
             // get the url connection
             try {
                 LOG.debug("Making callback to " + url);
                 
                 // cook the parameters to be sent
                 NameValuePair[] data = new NameValuePair[6];
                 data[0] = new NameValuePair("cmd", "ageing_update");
                 data[1] = new NameValuePair("user_id", userId.toString());
                 data[2] = new NameValuePair("login_name", user.getEntity().getUserName());
                 data[3] = new NameValuePair("from_status", originalStatusId.toString());
                 data[4] = new NameValuePair("to_status", statusId.toString());
                 data[5] = new NameValuePair("can_login", String.valueOf(status.getCanLogin()));
                 
                 // make the call
                 HttpClient client = new HttpClient();
                 client.setConnectionTimeout(30000);
                 PostMethod post = new PostMethod(url);
                 post.setRequestBody(data);
                 client.executeMethod(post);
             } catch (Exception e1) {
                 LOG.info("Could not make call back. url = " + url + 
                         " Message:" + e1.getMessage());
             }
 
         }
 
         // trigger NewUserStatusEvent
         EventManager.process(new NewUserStatusEvent(
                 user.getDto().getCompany().getId(), userId, originalStatusId,
                 statusId));
     }
     
     /**
      * Takes a user out of the ageing system -> back to status active
      * but only if she doesn't have any outstanding invoices.
      * @param user
      */
     public void out(UserDTO user, Integer excludigInvoiceId) 
             throws SQLException {
         // if the user is in the ageing process
         LOG.debug("Taking user " + user.getUserId() + " out of ageing");
         if (user.getStatus().getId() != UserDTOEx.STATUS_ACTIVE) {
             InvoiceBL invoices = new InvoiceBL();
             // only if the user doesn't have any more invoices that are overdue
             if (!invoices.isUserWithOverdueInvoices(user.getUserId(),
                     Calendar.getInstance().getTime(),
                     excludigInvoiceId).booleanValue()) {
                 // good, no processable invoices for this guy
                 setUserStatus(null, user.getUserId(),  UserDTOEx.STATUS_ACTIVE,
                         Calendar.getInstance().getTime());
             } else {
                 LOG.debug("User with overdue invoices");
             }
         } else {
             LOG.debug("User already active");
         }
 
     }
     
     /**
      * Will move the user one step forward in the ageing proces ONLY IF
      * the user has been long enough in the present status. (for a user
      * in active status, it always moves it to the first ageing step).
      * @param userId
      * @throws NamingException
      * @throws SessionInternalError
      */
     public void age(UserDTO user, Date today) 
             throws NamingException, SessionInternalError {
         LOG.debug("Ageing user:" + user.getUserId());
         Integer status = user.getStatus().getId();
         Integer nextStatus = null;
         if (status.equals(UserDTOEx.STATUS_ACTIVE)) {
             // welcome to the ageing process
             nextStatus = getNextStep(user.getEntity(),
                     UserDTOEx.STATUS_ACTIVE);
         } else {
             LOG.debug("she's already in the ageing");
             // this guy is already in the ageing
             AgeingEntityStepDTO step = new AgeingEntityStepDAS().findStep(
                     user.getEntity().getId(), status);
             if (step != null) {    
                 ageing = ageingDas.find(step.getId());
             
                 // verify if it is time for another notch
                 GregorianCalendar cal = new GregorianCalendar();
                 Date lastChange = user.getLastStatusChange();
                 if (lastChange == null) {
                     lastChange = user.getCreateDatetime();
                 }
                 cal.setTime(lastChange);
                 cal.add(Calendar.DATE, ageing.getDays());
                 LOG.debug("last time + days=" + cal.getTime() + " today " + today
                         + "compare=" + cal.getTime().compareTo(today));
                 if (cal.getTime().compareTo(today) <= 0) {
                     nextStatus =  getNextStep(user.
                             getEntity(),user.getStatus().getId());
                 } else {
                     return;
                 }
             } else {
                 // this user is an ageing status that has been removed.
                 // may be this is a bug, and a currently-in-use status
                 // should not be removable.
                 // Now it will simple jump to the next status.
                 nextStatus =  getNextStep(user.
                         getEntity(),user.getStatus().getId());
             }         
         }
         if (nextStatus != null) {
             setUserStatus(null, user.getUserId(), nextStatus, today);
         } else {
             eLogger.warning(user.getEntity().getId(), user.getUserId(), 
                     user.getUserId(), EventLogger.MODULE_USER_MAINTENANCE, 
                     EventLogger.NO_FURTHER_STEP,
                     Constants.TABLE_BASE_USER);
         }
     }
     
     /**
      * Give a current step, finds the next one. If there's none next, then
      * it returns null. The current step can be missing (for the case that
      * it has been deleted by the admin).
      * @param entity
      * @param statusId
      * @return
      * @throws SessionInternalError
      */
     private Integer getNextStep(CompanyDTO entity, Integer statusId) 
             throws SessionInternalError {
         // this will return the next step, even if statusId doesn
         // exists in the current set of steps.
         // The steps are returned order by status id.
         for (AgeingEntityStepDTO step : entity.getAgeingEntitySteps()) {
             Integer stepId = step.getUserStatus().getId();
             if (stepId.compareTo(statusId) > 0) {
                 return stepId;
             }
         }
         
         return null;
     }
     
     public static boolean isAgeingInUse(CompanyDTO entity) {
         return entity.getAgeingEntitySteps().size() > 1;
     }
     
     /**
      * Goes over all the users that are not active, and calls age on them.
      * This doesn't discriminate over entities.
      */
     public void reviewAll(Date today) 
             throws NamingException, SessionInternalError, SQLException {
         // go over all the users already in the ageing system
         for (UserDTO userRow : new UserDAS().findAgeing()) {
             age(userRow, today);
         }
         
         // now go over the active users with payable invoices
         UserBL user = new UserBL();
         CachedRowSet usersSql;
         try {
             usersSql = user.findActiveWithOpenInvoices();
         } catch (Exception e) {
             // avoid further problems
             LOG.error("Exception finding users to age", e);
             return;
         }
         
         InvoiceBL invoiceBL = new InvoiceBL();
         
         while (usersSql.next()) {
             Integer userId = new Integer(usersSql.getInt(1));
             user.set(userId);
             UserDTO userRow = user.getEntity();
             // get the grace period for the entity of this user
             PreferenceBL prefs = new PreferenceBL();
             prefs.set(userRow.getEntity().getId(), 
                     Constants.PREFERENCE_GRACE_PERIOD);
             int gracePeriod = prefs.getInt();
             LOG.debug("Reviewing invoices of user:" + userRow.getUserId() +
                     " grace: " + gracePeriod);
             // now go over this user's pending invoices
             for (Iterator it2 = invoiceBL.getHome().findProccesableByUser(
                     userRow).iterator(); it2.hasNext(); ) {
                 InvoiceDTO invoice = (InvoiceDTO) it2.next();
                 GregorianCalendar cal = new GregorianCalendar();
                 cal.setTime(invoice.getDueDate());
                 if (gracePeriod > 0) {
                     cal.add(Calendar.DATE, gracePeriod);
                 }
                 
                 if (userRow.getUserId().intValue() == 17) {
                     LOG.debug("invoice " + invoice.getId() + " due+grace=" + cal.getTime() +
                         " today=" + today + " compare=" + (cal.getTime().compareTo(today)));
                 }
                     
                 if (cal.getTime().compareTo(today) < 0) {
                     // ok, this user has an overdue invoice
                     age(userRow, today);        
                     break;
                 }
             }  
         }
     }
     
     public String getWelcome(Integer entityId, Integer languageId, 
             Integer statusId) throws NamingException {
         AgeingEntityStepDTO step = new AgeingEntityStepDAS().findStep(
             entityId, statusId);
         ageing = ageingDas.find(step.getId());
         return ageing.getWelcomeMessage(languageId);
     }
     
 	public AgeingDTOEx[] getSteps(Integer entityId, 
             Integer executorLanguageId, Integer languageId) 
             throws NamingException {
         AgeingDTOEx[] result  = new AgeingDTOEx[
                 UserDTOEx.STATUS_DELETED.intValue()];
         
         // go over all the steps
         for (int step = UserDTOEx.STATUS_ACTIVE.intValue(); 
                 step <= UserDTOEx.STATUS_DELETED.intValue(); step++) {
             AgeingDTOEx newStep = new AgeingDTOEx();
             newStep.setStatusId(new Integer(step));
             UserStatusDTO statusRow = new UserStatusDAS().find(step);
             newStep.setStatusStr(statusRow.getDescription(
                         executorLanguageId));
             newStep.setCanLogin(statusRow.getCanLogin());
             AgeingEntityStepDTO myStep = new AgeingEntityStepDAS().findStep(
                     entityId, new Integer(step));
             if (myStep != null) { // it doesn't have to be there
                 ageing = ageingDas.find(myStep.getId());
 
                 newStep.setDays(ageing.getDays());
                 newStep.setFailedLoginMessage(ageing.getFailedLoginMessage(
                         languageId));
                 newStep.setInUse(new Boolean(true));
                 newStep.setWelcomeMessage(ageing.getWelcomeMessage(
                         languageId));
             } else {
                 newStep.setInUse(new Boolean(false));
             }
             result[step-1] = newStep;
         }
         
         return result;
     }
 	
 	public AgeingDTOEx[] validate(AgeingDTOEx[] steps) throws SessionInternalError { 
 		
         int lastSelected = 0;
         for (int f = 1; f < steps.length; f++) {
             AgeingDTOEx line = steps[f];
             if (line.getInUse().booleanValue()) {
                 lastSelected = f;
             }
         }
         for (int f = 0; f < steps.length; f++) {
         	//Active Step cannot be set to not-in-use
 	        if (steps[f].getStatusId().equals(UserDTOEx.STATUS_ACTIVE)) {
 	            steps[f].setInUse(new Boolean(true));
 	        }
 	        if (steps[f].getInUse().booleanValue()) {
 	        	//if the Step is not deleted, welcome message may not be null
                 if (!steps[f].getStatusId().equals(UserDTOEx.STATUS_DELETED) && 
                         steps[f].getWelcomeMessage() == null ) {
                	SessionInternalError exception = new SessionInternalError("Validation of new plug-in");
                 	exception.setErrorMessages(new String[] {
                         	"AgeingWS,welcomeMessage,config.ageing.error.null.message," + null});
                 	throw exception;
                 }
                 //for inUse steps (NOT ACTIVE or DELETE Step) , days may not be zero
                 if ( ! ( steps[f].getStatusId().equals(UserDTOEx.STATUS_ACTIVE) ||
                 		steps[f].getStatusId().equals(UserDTOEx.STATUS_DELETED) ) 
                 		&& f != lastSelected ) {
                 	
                 	if (steps[f].getDays() <= 0 ) {
                 		SessionInternalError exception = new SessionInternalError("Days cannot be zero for an 'in use' step");
                     	exception.setErrorMessages(new String[] {
                             	"AgeingWS,days,config.ageing.error.zero.days," + 0});
                     	throw exception;
                 	}
                 }
                 //set days to zero by default for the last Selected Step
                 if (f == lastSelected ) {
                 	if (steps[f].getDays() > 0) {
 	                	SessionInternalError exception = new SessionInternalError("The days for the last selected step has to be 0");
 	                	exception.setErrorMessages(new String[] {
 	                        	"AgeingWS,days,config.ageing.error.lastDay," + steps[f].getDays()});
 	                	throw exception;
                 	}
                 	else {steps[f].setDays(0);}
                 }
 	        }
         }
         return steps;
 	}
     
     public void setSteps(Integer entityId, Integer languageId, 
             AgeingDTOEx[] steps) throws NamingException {
         LOG.debug("Setting a total of " + steps.length + " steps");
         for (int f = 0; f < steps.length; f++) {
             // get the existing data for this step
             LOG.debug("Processing step[" + f + "]:" + steps[f].getStatusId());
             AgeingEntityStepDTO myStep = new AgeingEntityStepDAS().findStep(
                     entityId, steps[f].getStatusId());
             if (myStep != null) {
                 ageing = ageingDas.find(myStep.getId());
 
                 LOG.debug("step present");
             } else {
                 LOG.debug("step not present");
                 ageing = null;
             }
             if (!steps[f].getInUse().booleanValue()) {
                 // delete if now is not wanted
                 if (ageing != null) {
                     LOG.debug("Removig step.");
                     ageingDas.delete(ageing);
                 } 
             } else {
                 // it is wanted
                 LOG.debug("welcome = " + steps[f].getWelcomeMessage());
                 if (ageing == null) {
                     // create
                     LOG.debug("Creating step.");
                     ageingDas.create(entityId, steps[f].getStatusId(), 
                             steps[f].getWelcomeMessage(), 
                             steps[f].getFailedLoginMessage(), 
                             languageId, steps[f].getDays());
                 } else {
                     // update
                     LOG.debug("Updating step.");
                     ageing.setDays(steps[f].getDays());
                     ageing.setFailedLoginMessage(languageId,
                             steps[f].getFailedLoginMessage());
                     ageing.setWelcomeMessage(languageId, 
                             steps[f].getWelcomeMessage());
                 }
             }
         }
     }
 
     public AgeingWS getWS(AgeingDTOEx dto) { 
     	return null == dto ? null : new AgeingWS(dto);
     }
     
     public AgeingDTOEx getDTOEx(AgeingWS ws) {
     	AgeingDTOEx dto= new AgeingDTOEx();
     	dto.setStatusId(ws.getStatusId());
     	dto.setStatusStr(ws.getStatusStr());
     	dto.setInUse(null == ws.getInUse() ? Boolean.FALSE : ws.getInUse());
     	dto.setDays(null == ws.getDays() ? 0 : ws.getDays().intValue());
     	dto.setWelcomeMessage(ws.getWelcomeMessage());
 	    dto.setFailedLoginMessage(ws.getFailedLoginMessage());
 	    return dto;
     }
 }
