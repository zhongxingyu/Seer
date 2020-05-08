 package gov.nysenate.billbuzz.controller;
 
 import gov.nysenate.billbuzz.model.BillBuzzConfirmation;
 import gov.nysenate.billbuzz.model.BillBuzzSubscription;
 import gov.nysenate.billbuzz.model.BillBuzzUser;
 import gov.nysenate.billbuzz.util.BillBuzzDAO;
 import gov.nysenate.billbuzz.util.FormProcessor;
 
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 /**
  * Handles /BillBuzz/update/form requests.
  *
  * @author GraylinKim
  *
  */
 @SuppressWarnings("serial")
 public class UpdateForm extends HttpServlet
 {
     private final Logger logger = Logger.getLogger(UpdateForm.class);
 
     /**
      * Get requests to update form need to have a valid confirmation code.
      */
     public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         try {
             String message = "";
             BillBuzzDAO dao = new BillBuzzDAO();
             List<BillBuzzSubscription> subscriptions;
             BillBuzzConfirmation confirmation = FormProcessor.getConfirmation(request);
 
             if (confirmation != null) {
                message = "instruction";
                 subscriptions = dao.loadSubscriptions(confirmation.getUserId());
             }
             else {
                 message = "invalid";
                 subscriptions = new ArrayList<BillBuzzSubscription>();
             }
 
             request.setAttribute("user", confirmation.getUser());
             request.setAttribute("message", message);
             request.setAttribute("confirmation", confirmation);
             request.setAttribute("senators", dao.getSenators(dao.getSession()));
             request.setAttribute("subscriptions", FormProcessor.getSubscriptionMap(subscriptions));
             request.getRequestDispatcher("/WEB-INF/pages/update_form.jsp").forward(request, response);
         }
         catch (SQLException e) {
             logger.error(e.getMessage(), e);
             throw new ServletException(e.getMessage(), e);
         }
     }
 
     /**
      * Processes the updated form and updates the user's record in the database.
      * @throws IOException
      */
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         try {
             String message = "";
             Date createdAt = new Date();
             BillBuzzUser user = null;
             BillBuzzDAO dao = new BillBuzzDAO();
             List<BillBuzzSubscription> subscriptions = new ArrayList<BillBuzzSubscription>();
             BillBuzzConfirmation confirmation = FormProcessor.getConfirmation(request);
             BillBuzzUser formUser = FormProcessor.processSubscriptionForm(request, createdAt);
             if (confirmation == null) {
                 message = "invalid";
             }
             else if (formUser == null) {
                 message = "missing_userinfo";
             }
             else {
                 // Overwrite stored values with form values
                 user = confirmation.getUser();
                 user.setFirstName(formUser.getFirstName());
                 user.setLastName(formUser.getLastName());
                 user.setEmail(formUser.getEmail());
                 user.setSubscriptions(formUser.getSubscriptions());
 
                 if (user.getSubscriptions().isEmpty()) {
                     message = "missing_subscription";
                 }
                 else if (user.getConfirmedAt() == null) {
                     // Treat this update like a sign-up
                     message = "signup_required";
                     request.getRequestDispatcher("/signup/form").forward(request, response);
                 }
                 else {
                     // Update their goods.
                     dao.saveUser(user);
                     subscriptions = user.getSubscriptions();
                     dao.replaceSubscriptions(subscriptions, user.getId());
                     confirmation.setUsedAt(createdAt);
                     dao.saveConfirmation(confirmation);
                     message = "success";
                 }
             }
 
             request.setAttribute("user", user);
             request.setAttribute("message", message);
             request.setAttribute("confirmation", confirmation);
             request.setAttribute("senators", dao.getSenators(dao.getSession()));
             request.setAttribute("subscriptions", FormProcessor.getSubscriptionMap(subscriptions));
             request.getRequestDispatcher("/WEB-INF/pages/update_form.jsp").forward(request, response);
         }
         catch (SQLException e) {
             logger.error(e.getMessage(), e);
             throw new ServletException(e.getMessage(), e);
         }
     }
 }
