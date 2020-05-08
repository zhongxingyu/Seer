 package nb.driverobot;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletResponse;
 
 public class JSON {
   private final StringBuffer buf = new StringBuffer();
  private boolean first = true;
   
   public JSON() {
     buf.append('{');
   }
   
   public JSON append(String key, Object value) {
     if (!first ) {
       buf.append(",");
     }
     first = false;
     buf.append('"').append(key).append("\":\"").append(value).append('"');  
     return this;
   }
   
   public void marshall(HttpServletResponse response) throws IOException {
     buf.append('}');
     response.setContentType("application/json");
     response.getWriter().println(buf);
   }
 }
