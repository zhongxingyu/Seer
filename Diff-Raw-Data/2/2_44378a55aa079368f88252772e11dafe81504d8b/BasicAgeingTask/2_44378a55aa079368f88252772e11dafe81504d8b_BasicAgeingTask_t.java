 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.process.task;
 
 import com.sapienter.jbilling.server.invoice.InvoiceBL;
 import com.sapienter.jbilling.server.invoice.db.InvoiceDAS;
 import com.sapienter.jbilling.server.invoice.db.InvoiceDTO;
 import com.sapienter.jbilling.server.notification.INotificationSessionBean;
 import com.sapienter.jbilling.server.notification.MessageDTO;
 import com.sapienter.jbilling.server.notification.NotificationBL;
 import com.sapienter.jbilling.server.notification.NotificationNotFoundException;
 import com.sapienter.jbilling.server.order.OrderBL;
 import com.sapienter.jbilling.server.order.db.OrderDAS;
 import com.sapienter.jbilling.server.order.db.OrderDTO;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
 import com.sapienter.jbilling.server.process.AgeingDTOEx;
 import com.sapienter.jbilling.server.process.db.AgeingEntityStepDAS;
 import com.sapienter.jbilling.server.process.db.AgeingEntityStepDTO;
 import com.sapienter.jbilling.server.process.event.NewUserStatusEvent;
 import com.sapienter.jbilling.server.system.event.EventManager;
 import com.sapienter.jbilling.server.user.UserBL;
 import com.sapienter.jbilling.server.user.UserDTOEx;
 import com.sapienter.jbilling.server.user.db.UserDAS;
 import com.sapienter.jbilling.server.user.db.UserDTO;
 import com.sapienter.jbilling.server.user.db.UserStatusDAS;
 import com.sapienter.jbilling.server.user.db.UserStatusDTO;
 import com.sapienter.jbilling.server.util.Constants;
 import com.sapienter.jbilling.server.util.Context;
 import com.sapienter.jbilling.server.util.PreferenceBL;
 import com.sapienter.jbilling.server.util.audit.EventLogger;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.NameValuePair;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.log4j.Logger;
 import org.hibernate.ScrollableResults;
 import org.springframework.dao.EmptyResultDataAccessException;
 
 import javax.naming.NamingException;
 import javax.sql.rowset.CachedRowSet;
 import java.sql.SQLException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * BasicAgeingTask
  *
  * @author Brian Cowdery
  * @since 28/04/11
  */
 public class BasicAgeingTask extends PluggableTask implements IAgeingTask {
 
     private static final Logger LOG = Logger.getLogger(BasicAgeingTask.class);
     private final EventLogger eLogger = EventLogger.getInstance();
 
     private static Calendar calendar = GregorianCalendar.getInstance();
     static {
         calendar.clear();
     }
 
     private Map<Integer, Integer> gracePeriodCache = new HashMap<Integer, Integer>();
 
     protected int getGracePeriod(Integer entityId) {
         if (!gracePeriodCache.containsKey(entityId)) {
             PreferenceBL preference = new PreferenceBL();
             preference.set(entityId, Constants.PREFERENCE_GRACE_PERIOD);
             gracePeriodCache.put(entityId, preference.getInt());
         }
 
         return gracePeriodCache.get(entityId);
     }
 
     /**
      * Review all users for the given day, and age those that have outstanding invoices over
      * the set number of days for an ageing step.
      *
      * @param steps ageing steps
      * @param today today's date
      * @param executorId executor id
      */
     public void reviewAllUsers(Integer entityId, Set<AgeingEntityStepDTO> steps, Date today, Integer executorId) {
         LOG.debug("Reviewing users for entity " + entityId + " ...");
 
         // go over all the users already in the ageing system
         for (UserDTO user : new UserDAS().findAgeing(entityId)) {
             ageUser(steps, user, today, executorId);
         }
 
         // go over the active users with payable invoices
         try {
             UserDAS userDas = new UserDAS();
             InvoiceDAS invoiceDas = new InvoiceDAS();
 
             CachedRowSet users = new UserBL().findActiveWithOpenInvoices(entityId);
 
             while (users.next()) {
                 Integer userId = users.getInt(1);
                 UserDTO user = userDas.find(userId);
                 int gracePeriod = getGracePeriod(entityId);
 
                 LOG.debug("Reviewing invoices for user " + user.getId()
                           + " using a grace period of " + gracePeriod + " days.");
 
                 for (InvoiceDTO invoice : invoiceDas.findProccesableByUser(user)) {
                     if (isInvoiceOverdue(invoice, user, gracePeriod, today)) {
                         ageUser(steps, user, today, executorId);
                         break;
                     }
                 }
             }
 
         } catch (SQLException e) {
             LOG.error("Failed to fetch users with payable invoices.", e);
 
         } catch (NamingException e) {
             LOG.error("Exception fetching users with payable invoices.", e);
         }
     }
 
     /**
      * Moves a user one step forward in the ageing process (move from active -> suspended etc.). The
      * user will only be moved if they have spent long enough in their present status.
      *
      * @param steps ageing steps
      * @param user user to age
      * @param today today's date
      * @return the resulting ageing step for the user after ageing
      */
     public AgeingEntityStepDTO ageUser(Set<AgeingEntityStepDTO> steps, UserDTO user, Date today, Integer executorId) {
         LOG.debug("Ageing user " + user.getId());
 
         Integer currentStatusId = user.getStatus().getId();
         UserStatusDTO nextStatus = null;
         AgeingEntityStepDTO ageingStep = null;
 
         if (currentStatusId.equals(UserDTOEx.STATUS_ACTIVE)) {
             // send welcome message (initial step after active).
             nextStatus = getNextAgeingStep(steps, UserDTOEx.STATUS_ACTIVE);
 
         } else {
             // user already in the ageing process
             ageingStep = new AgeingEntityStepDAS().findStep(user.getEntity().getId(), currentStatusId);
 
             if (ageingStep != null) {
                 // determine the next ageing step
                 if (isAgeingRequired(user, ageingStep, today)) {
                     nextStatus = getNextAgeingStep(steps, currentStatusId);
                     LOG.debug("User " + user.getId() + " needs to be aged to '" + getStatusDescription(nextStatus) + "'");
                 }
 
             } else {
                 // User is in a non-existent ageing status... Either the status was removed or
                 // the data is bad. As a workaround, just move to the next status.
                 nextStatus =  getNextAgeingStep(steps, currentStatusId);
                 LOG.warn("User " + user.getId() + " is in an invalid ageing step. Moving to '" + getStatusDescription(nextStatus) + "'");
             }
         }
 
         // set status
         if (nextStatus != null) {
             setUserStatus(user, nextStatus, today, null);
 
         } else {
             LOG.debug("Next status is null, no further ageing steps are available.");
             eLogger.warning(user.getEntity().getId(),
                             user.getUserId(),
                             user.getUserId(),
                             EventLogger.MODULE_USER_MAINTENANCE,
                             EventLogger.NO_FURTHER_STEP,
                             Constants.TABLE_BASE_USER);
         }
 
         return ageingStep;
     }
 
     /**
      * Returns true if the given invoice is overdue.
      *
      * @param invoice invoice to check
      * @param user user owning the invoice
      * @param gracePeriod company wide grace period
      * @param today today's date
      * @return true if invoice is overdue, false if not
      */
     public boolean isInvoiceOverdue(InvoiceDTO invoice, UserDTO user, Integer gracePeriod, Date today) {
         calendar.clear();
         calendar.setTime(invoice.getDueDate());
         calendar.add(Calendar.DATE, gracePeriod);
 
         if (calendar.getTime().before(today)) {
             LOG.debug("Invoice is overdue (due date " + invoice.getDueDate() + " + "
                       + gracePeriod + " days grace, is before today " + today + ")");
             return true;
         }
 
         LOG.debug("Invoice is NOT overdue (due date " + invoice.getDueDate() + " + "
                   + gracePeriod + " days grace is after today " + today + ")");
         return false;
     }
 
     /**
      * Returns true if the user requires ageing.
      *
      * @param user user being reviewed
      * @param currentStep current ageing step of the user
      * @param today today's date
      * @return true if user requires ageing, false if not
      */
     public boolean isAgeingRequired(UserDTO user, AgeingEntityStepDTO currentStep, Date today) {
         Date lastStatusChange = user.getLastStatusChange() != null
                                 ? user.getLastStatusChange()
                                 : user.getCreateDatetime();
 
         calendar.clear();
         calendar.setTime(lastStatusChange);
         calendar.add(Calendar.DATE, currentStep.getDays());
 
         if (calendar.getTime().equals(today) || calendar.getTime().before(today)) {
             LOG.debug("User status has expired (last change " + lastStatusChange + " + "
                       + currentStep.getDays() + " days is before today " + today + ")");
             return true;
         }
 
         LOG.debug("User does not need to be aged (last change " + lastStatusChange + " + "
                   + currentStep.getDays() + " days is after today " + today + ")");
         return false;
     }
 
     /**
      * Removes a user from the ageing process (makes them active), ONLY if they do not
      * still have overdue invoices.
      *
      * @param user user to make active
      * @param excludedInvoiceId invoice id to ignore when determining if the user CAN be made active
      * @param executorId executor id
      */
     public void removeUser(UserDTO user, Integer executorId, Integer excludedInvoiceId) {
         Date now = new Date();
 
         // validate that the user actually needs a status change
        if (UserDTOEx.STATUS_ACTIVE.equals(user.getStatus().getId())) {
             LOG.debug("User " + user.getId() + " is already active, no need to remove from ageing.");
             return;
         }
 
         // validate that the user does not still have overdue invoices
         try {
             if (new InvoiceBL().isUserWithOverdueInvoices(user.getUserId(), now, excludedInvoiceId)) {
                 LOG.debug("User " + user.getId() + " still has overdue invoices, cannot remove from ageing.");
                 return;
             }
         } catch (SQLException e) {
             LOG.error("Exception occurred checking for overdue invoices.", e);
             return;
         }
 
         // make the status change.
         LOG.debug("Removing user " + user.getUserId() + " from ageing (making active).");
         UserStatusDTO status = new UserStatusDAS().find(UserDTOEx.STATUS_ACTIVE);
         setUserStatus(user, status, now, null);
     }
 
     /**
      * Sets the user status to the given "aged" status. If the user status is already set to the aged status
      * no changes will be made. This method also performs an HTTP callback and sends a notification
      * message when a status change is made.
      *
      * If the user becomes suspended and can no longer log-in to the system, all of their active orders will
      * be automatically suspended.
      *
      * If the user WAS suspended and becomes active (and can now log-in to the system), any automatically
      * suspended orders will be re-activated.
      *
      * @param user user
      * @param status status to set
      * @param today today's date
      * @param executorId executor id
      */
     public void setUserStatus(UserDTO user, UserStatusDTO status, Date today, Integer executorId) {
         // only set status if the new "aged" status is different from the users current status
         if (status.getId() == user.getStatus().getId()) {
             return;
         }
 
         LOG.debug("Setting user " + user.getId() + " status to '" + getStatusDescription(status) + "'");
 
         if (executorId != null) {
             // this came from the gui
             eLogger.audit(executorId,
                           user.getId(),
                           Constants.TABLE_BASE_USER,
                           user.getId(),
                           EventLogger.MODULE_USER_MAINTENANCE,
                           EventLogger.STATUS_CHANGE,
                           user.getStatus().getId(), null, null);
         } else {
             // this is from a process, no executor involved
             eLogger.auditBySystem(user.getCompany().getId(),
                                   user.getId(),
                                   Constants.TABLE_BASE_USER,
                                   user.getId(),
                                   EventLogger.MODULE_USER_MAINTENANCE,
                                   EventLogger.STATUS_CHANGE,
                                   user.getStatus().getId(), null, null);
         }
 
         // make the change
         boolean couldLogin = user.getStatus().getCanLogin() == 1;
         UserStatusDTO oldStatus = user.getStatus();
 
         user.setUserStatus(status);
         user.setLastStatusChange(today);
 
         // status changed to deleted, remove user
         if (status.getId() == UserDTOEx.STATUS_DELETED) {
             LOG.debug("Deleting user " + user.getId());
             new UserBL(user.getId()).delete(executorId);
             return;
         }
 
         // status changed from active to suspended
         // suspend customer orders
         if (couldLogin && status.getCanLogin() == 0) {
             LOG.debug("User " + user.getId() + " cannot log-in to the system. Suspending active orders.");
 
             OrderBL orderBL = new OrderBL();
             ScrollableResults orders = new OrderDAS().findByUser_Status(user.getId(), Constants.ORDER_STATUS_ACTIVE);
 
             while (orders.next()) {
                 OrderDTO order = (OrderDTO) orders.get()[0];
                 orderBL.set(order);
                 orderBL.setStatus(executorId, Constants.ORDER_STATUS_SUSPENDED_AGEING);
             }
 
             orders.close();
         }
 
         // status changed from suspended to active
         // re-active suspended customer orders
         if (!couldLogin && status.getCanLogin() == 1) {
             LOG.debug("User " + user.getId() + " can now log-in to the system. Activating previously suspended orders.");
 
             OrderBL orderBL = new OrderBL();
             ScrollableResults orders = new OrderDAS().findByUser_Status(user.getId(), Constants.ORDER_STATUS_SUSPENDED_AGEING);
 
             while (orders.next()) {
                 OrderDTO order = (OrderDTO) orders.get()[0];
                 orderBL.set(order);
                 orderBL.setStatus(executorId, Constants.ORDER_STATUS_ACTIVE);
             }
 
             orders.close();
         }
 
         // perform callbacks and notifications
         performAgeingCallback(user, oldStatus, status);
         sendAgeingNotification(user, oldStatus, status);
 
         // emit NewUserStatusEvent
         NewUserStatusEvent event = new NewUserStatusEvent(user.getCompany().getId(), user.getId(), oldStatus.getId(), status.getId());
         EventManager.process(event);
     }
 
 
     protected boolean performAgeingCallback(UserDTO user, UserStatusDTO oldStatus, UserStatusDTO newStatus) {
         String url = null;
         try {
             PreferenceBL pref = new PreferenceBL();
             pref.set(user.getEntity().getId(), Constants.PREFERENCE_URL_CALLBACK);
             url = pref.getString();
 
         } catch (EmptyResultDataAccessException e) {
             /* ignore, no callback preference configured */
         }
 
         if (url != null && url.length() > 0) {
             try {
                 LOG.debug("Performing ageing HTTP callback for URL: " + url);
 
                 // cook the parameters to be sent
                 NameValuePair[] data = new NameValuePair[6];
                 data[0] = new NameValuePair("cmd", "ageing_update");
                 data[1] = new NameValuePair("user_id", String.valueOf(user.getId()));
                 data[2] = new NameValuePair("login_name", user.getUserName());
                 data[3] = new NameValuePair("from_status", String.valueOf(oldStatus.getId()));
                 data[4] = new NameValuePair("to_status", String.valueOf(newStatus.getId()));
                 data[5] = new NameValuePair("can_login", String.valueOf(newStatus.getCanLogin()));
 
                 // make the call
                 HttpClient client = new HttpClient();
                 client.setConnectionTimeout(30000);
                 PostMethod post = new PostMethod(url);
                 post.setRequestBody(data);
                 client.executeMethod(post);
 
             } catch (Exception e) {
                 LOG.error("Exception occurred posting ageing HTTP callback for URL: " + url, e);
                 return false;
             }
         }
         return true;
     }
 
     protected boolean sendAgeingNotification(UserDTO user, UserStatusDTO oldStatus, UserStatusDTO newStatus) {
         try {
             MessageDTO message = new NotificationBL().getAgeingMessage(user.getEntity().getId(),
                                                                user.getLanguage().getId(),
                                                                newStatus.getId(),
                                                                user.getId());
 
             INotificationSessionBean notification = (INotificationSessionBean) Context.getBean(Context.Name.NOTIFICATION_SESSION);
             notification.notify(user, message);
 
         } catch (NotificationNotFoundException e) {
             LOG.warn("Failed to send ageing notification. Entity " + user.getEntity().getId()
                      + " does not have an ageing message configured for status '" + getStatusDescription(newStatus) + "'.");
             return false;
         }
         return true;
     }
 
     /**
      * Get the status for the next step in the ageing process, based on the users
      * current status.
      *
      * @param steps configured ageing steps
      * @param currentStatusId the current user status
      */
     public UserStatusDTO getNextAgeingStep(Set<AgeingEntityStepDTO> steps, Integer currentStatusId) {
         for (AgeingEntityStepDTO step : steps) {
             Integer stepStatusId = step.getUserStatus().getId();
             if (stepStatusId.compareTo(currentStatusId) > 0) {
                 return step.getUserStatus();
             }
         }
 
         return null;
     }
 
     /**
      * Null safe convenience method to return the status description.
      *
      * @param status user status
      * @return description
      */
     private String getStatusDescription(UserStatusDTO status) {
         if (status != null) {
             return status.getDescription();
         }
         return null;
     }
 }
