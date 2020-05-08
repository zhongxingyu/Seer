 import play.*;
 import play.mvc.*;
 import play.mvc.Http.Request;
 import play.mvc.Http.Context;
 import java.lang.reflect.Method;
import com.alvazan.play2.NoSqlForPlay2;
 
 public class Global extends GlobalSettings {
 
    @Override
    public Action onRequest(Request request, Method actionMethod) {
       Context.current().args.put("em", NoSqlForPlay2.getEntityManagerFactory().createEntityManager());
       return super.onRequest(request, actionMethod);  
    }
 }
