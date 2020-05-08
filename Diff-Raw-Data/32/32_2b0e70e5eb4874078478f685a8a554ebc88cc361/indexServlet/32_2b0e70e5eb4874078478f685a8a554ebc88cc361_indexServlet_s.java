 package net.ggtools.codestory;
 
 import com.google.common.io.ByteStreams;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletInputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: Christophe Labouisse
  * Date: 15/01/13
  * Time: 13:39
  */
 public class indexServlet extends javax.servlet.http.HttpServlet {
 
     private List<Resolver> resolvers = new ArrayList<Resolver>();
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         resolvers.add(new QuestionResolver());
         resolvers.add(new ScalaskelResolver());
         resolvers.add(new CalculatorResolver());
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         ServletInputStream inputStream = req.getInputStream();
         byte[] bytes = ByteStreams.toByteArray(inputStream);
         log(new String(bytes));
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         logRequest(request);
         String value = "Unknown request";
         try {
             for (Resolver resolver : resolvers) {
                 log("Trying " + resolver);
                 value = resolver.solve(request);
                 if (value != null) {
                     break;
                 }
             }
             response.getOutputStream().println(value);
         } catch (ResolverException e) {
             log("Cannot solve", e);
         }
     }
 
     private void logRequest(HttpServletRequest request) {
         StringBuilder stringBuilder = new StringBuilder("Dispatch: '").append(request.getServletPath()).append("', params = {");
         Map<String, String[]> parameterMap = request.getParameterMap();
         for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
             stringBuilder.append(entry.getKey()).append(" = ").append(Arrays.toString(entry.getValue()));
         }
         stringBuilder.append("}");
         log(stringBuilder.toString());
     }
 }
