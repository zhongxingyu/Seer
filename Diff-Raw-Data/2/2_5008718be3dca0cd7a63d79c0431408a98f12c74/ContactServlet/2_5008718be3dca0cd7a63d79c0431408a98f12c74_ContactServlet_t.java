 package com.bitmechanic.contact;
 
 import com.bitmechanic.barrister.Contract;
 import com.bitmechanic.barrister.Server;
 import com.bitmechanic.barrister.JacksonSerializer;
 
import com.bitmechanic.contact.generated.ContactService;

 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletRequest;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 public class ContactServlet extends HttpServlet {
 
     private Contract contract;
     private Server server;
     private JacksonSerializer serializer;
 
     public ContactServlet() {
         // Serialize requests/responses as JSON using Jackson
         serializer = new JacksonSerializer();
     }
 
     public void init(ServletConfig config) throws ServletException {
         try {
             // Load the contract from the IDL JSON file
             contract = Contract.load(getClass().getResourceAsStream("/contact.json"));
             server = new Server(contract);
 
             // Register our service implementation
             server.addHandler(ContactService.class, new ContactServiceImpl());
         }
         catch (Exception e) {
             throw new ServletException(e);
         }
     }
 
     public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
         try {
             InputStream is = req.getInputStream();
             OutputStream os = resp.getOutputStream();
             resp.addHeader("Content-Type", "application/json");
 
             // This will deserialize the request and invoke
             // our ContactServiceImpl code based on the method and params
             // specified in the request. The result, including any
             // RpcException (if thrown), will be serialized to the OutputStream
             server.call(serializer, is, os);
 
             is.close();
             os.close();
         }
         catch (Exception e) {
             throw new ServletException(e);
         }
     }
 
 }
