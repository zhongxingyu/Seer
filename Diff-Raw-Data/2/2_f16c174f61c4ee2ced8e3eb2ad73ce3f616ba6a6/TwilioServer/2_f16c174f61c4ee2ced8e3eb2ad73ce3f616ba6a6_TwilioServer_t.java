 /*
  * Author: Rohit Kumar
  * Date: April 22, 2012
  * rohit.kumar@rutgers.edu
  *
  * A simple test application I wrote using Twilio's API, 
  * and deployed on Heroku, instead of doing homework.
  *
  * Currently only supports one command: decide
  * decide task1, task2, task3
  *
  * If you can't decide what to do, this will just pick a random task out 
  * of those are have given. 
  */
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.*;
 import java.util.*;
 import com.twilio.sdk.*;
 import com.twilio.sdk.resource.factory.*;
 import com.twilio.sdk.resource.instance.*;
 
 public class TwilioServer extends HttpServlet {
     private static TwilioRestClient tclient;
     private static Account mainAccount;
     private static SmsFactory smsFactory;
     private final static String ACCOUNT_SID = "your_account_sid";
     private final static String AUTH_TOKEN = "your_auth_token";
 
     /*
      * Twilio's API will issue a HTTP GET request to us with params in the
      * request defined by the API: http://www.twilio.com/docs/api/twiml/sms/twilio_request
      */
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws ServletException, IOException {
         String incomingMessage = req.getParameter("Body");
         if (incomingMessage == null) {                  //Easy check for an invalid request (like if someone
             resp.getWriter().print("Hello There!\n");   //just opens the page in the browser). Print a message
             return;
         }
         String fromNum = req.getParameter("From");
         Map <String, String> smsParams = new HashMap<String, String>();
         smsParams.put("To", fromNum);
         smsParams.put("From", "14155992671");
         String messageBody = req.getParameter("Body");
         int commandIndex = messageBody.indexOf(" ");
         String command = messageBody.substring(0, commandIndex);  //First word should be the "command (ie decide)"
         String[] args = messageBody.substring(commandIndex + 1).split("\\s*,\\s*");         //Tokenize based on commas
         String responseBody = "Error: Unknown command";
         if (command.equals("decide")) {
             responseBody = randomlyDecide(args);
         } 
         smsParams.put("Body", responseBody);
         try {
             smsFactory.create(smsParams);                   //send message
         } catch (TwilioRestException e) {
             e.printStackTrace();
         }
     }
     
     /*
      * For the "decide" command, pick a random task
      */
     private String randomlyDecide(String[] command) {
         Random generator = new Random();
        return ("You should " + command[generator.nextInt(command.length)]);
     }
     
     /*
      * Set up a simple HTTP server. 
      * See: http://blog.heroku.com/archives/2011/8/25/java/
      */
     public static void main(String[] args) throws Exception{
         Server server = new Server(Integer.valueOf(System.getenv("PORT")));
         ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         context.setContextPath("/");
         server.setHandler(context);
         context.addServlet(new ServletHolder(new TwilioServer()),"/*");
         initTwilio();
         server.start();
         server.join();   
     }
     
     /*
      * Initialize Twilio API objects
      */
     public static void initTwilio() {
         tclient = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);
         mainAccount = tclient.getAccount();
         smsFactory = mainAccount.getSmsFactory();
     }
 }
