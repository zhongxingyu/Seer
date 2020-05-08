 package gov.nysenate.billbuzz.controller;
 
 import gov.nysenate.billbuzz.model.BillBuzzConfirmation;
 import gov.nysenate.billbuzz.model.BillBuzzUser;
 import gov.nysenate.billbuzz.util.BillBuzzDAO;
 import gov.nysenate.billbuzz.util.FormProcessor;
 import gov.nysenate.billbuzz.util.Mailer;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import javax.mail.MessagingException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.velocity.VelocityContext;
 
 @SuppressWarnings("serial")
 public class UpdateRequest extends HttpServlet
 {
     /**
      * Serve up the simple request form.
      */
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         request.setAttribute("message", "instruction");
         request.getRequestDispatcher("/WEB-INF/pages/update_request.jsp").forward(request, response);
     }
 
     /**
      * Create confirmation code and send email with valid link to the update form
      * @throws IOException
      */
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         try {
             String message = "";
             BillBuzzUser user = FormProcessor.processRequestForm(request);
             if (user == null) {
                 message = "invalid_email";
             }
             else {
                 message = "success";
                 BillBuzzConfirmation confirmation = new BillBuzzDAO().getOrCreateConfirmation("update", user.getId(), true, null);
 
                 VelocityContext context = new VelocityContext();
                 context.put("user", user);
                 context.put("confirmation", confirmation);
                 Mailer.send("update_request", user, context);
             }
 
             request.setAttribute("user", user);
             request.setAttribute("message", message);
             request.getRequestDispatcher("/WEB-INF/pages/update_request.jsp").forward(request, response);
         }
         catch (SQLException e) {
             throw new ServletException(e.getMessage(), e);
         }
         catch (EmailException e) {
             throw new ServletException(e.getMessage(), e);
         }
         catch (MessagingException e) {
             throw new ServletException(e.getMessage(), e);
         }
     }
 }
