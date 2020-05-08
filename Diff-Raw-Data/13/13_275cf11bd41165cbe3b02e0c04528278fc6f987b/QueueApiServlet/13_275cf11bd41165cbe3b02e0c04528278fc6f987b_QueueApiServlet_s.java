 package sonique.bango.servlet;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.sun.istack.internal.Nullable;
 import org.codehaus.jackson.map.ObjectMapper;
 import sonique.bango.store.QueueStore;
 import sonique.bango.store.ServiceProblemStore;
 
 import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
 import java.util.Collection;
 
 import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.*;
 
 public class QueueApiServlet extends PretentiousServlet {
     private final ServiceProblemStore serviceProblemStore;
     private final QueueStore queueStore;
 
     public QueueApiServlet(ObjectMapper objectMapper, ServiceProblemStore serviceProblemStore, QueueStore queueStore) {
         super("/api/queue/*", objectMapper);
         this.serviceProblemStore = serviceProblemStore;
         this.queueStore = queueStore;
     }
 
     protected String getResponse(HttpServletRequest request) {
         String pathInfo = request.getPathInfo();
         if (pathInfo.equals("/list")) {
             return writeJson(serviceProblemStore.serviceProblemsForQueueId(parseInt(request.getParameter("queueId"))));
         } else if (pathInfo.equals("/all")) {
             return writeJson(queueStore.allQueues());
        } else if(pathInfo.equals("/bulkTransfer")) {
             Collection<Integer> serviceProblemIds = Collections2.transform(newArrayList(request.getParameterValues("serviceProblemIds")), new Function<String, Integer>() {
                 public Integer apply(String input) {
                     return parseInt(input);
                 }
             });
             serviceProblemStore.bulkTransfer(parseInt(request.getParameter("destinationQueueId")), serviceProblemIds);
 
             return writeJson(serviceProblemStore.serviceProblemsForQueueId(parseInt(request.getParameter("originalQueueId"))));
        } else if(pathInfo.equals("/bulkClear")) {
             Collection<Integer> serviceProblemIds = Collections2.transform(newArrayList(request.getParameterValues("serviceProblemIds")), new Function<String, Integer>() {
                 public Integer apply(String input) {
                     return parseInt(input);
                 }
             });
             serviceProblemStore.bulkClear(serviceProblemIds);
 
             return writeJson(serviceProblemStore.serviceProblemsForQueueId(parseInt(request.getParameter("originalQueueId"))));
         }
 
         throw new GTFOException(404, "Unknown Api!");
     }
 }
