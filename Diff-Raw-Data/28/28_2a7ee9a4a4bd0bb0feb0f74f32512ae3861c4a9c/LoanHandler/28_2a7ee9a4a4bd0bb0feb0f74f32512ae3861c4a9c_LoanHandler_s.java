 package com.ssem.loan;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 
 import com.google.gson.Gson;
 
 public class LoanHandler extends AbstractHandler {
     private static final String TICKET_ID = "ticketId";
 
     @Override
     public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         response.setContentType("application/json;charset=utf-8");
         response.setStatus(HttpServletResponse.SC_OK);
         baseRequest.setHandled(true);
         PrintWriter writer = response.getWriter();
        if (isApplication(request)) {
            Application application = new Application(getNextId());
            LoanRepository.store(application);
            String json = new Gson().toJson(application);
            writer.println(json);
        } else if (isStatusRequest(request) && idSpecified(request)) {
            String application = fetchLoanInfo(request.getParameter(TICKET_ID));
            writer.println(application);
        } else {
            writer.println("Incorrect parameters provided");
         }
     }
 
     private boolean idSpecified(HttpServletRequest request) {
         return request.getParameter(TICKET_ID) != null && validId(request) != 0;
     }
 
     private long validId(HttpServletRequest request) {
         String ticketId = request.getParameter(TICKET_ID);
         try {
             return Long.parseLong(ticketId);
         } catch (NumberFormatException e) {
             return 0L;
         }
     }
 
     private long getNextId() {
         return LoanRepository.getNextId();
     }
 
     private boolean isStatusRequest(HttpServletRequest request) {
         return request.getParameter("fetch") != null;
     }
 
     private boolean isApplication(HttpServletRequest request) {
         return request.getParameter("apply") != null;
     }
 
     private String fetchLoanInfo(String ticketId) {
         Application formerApplication = LoanRepository.fetch(ticketId);
         return new Gson().toJson(formerApplication);
     }
 }
