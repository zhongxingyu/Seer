 package sonique.bango.store;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import sonique.bango.domain.Queue;
 import sonique.bango.domain.ServiceProblem;
 import sonique.bango.domain.WorkItem;
 
 import java.util.Collection;
 import java.util.List;
 
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.Lists.newArrayList;
 
 public class ServiceProblemStore {
 
     private final List<ServiceProblem> serviceProblems = newArrayList();
     private final QueueStore queueStore;
 
     public ServiceProblemStore(QueueStore queueStore) {
         this.queueStore = queueStore;
         for(int index=0; index<100; index++) {
            int queueId = (index % 10) + 1;
             serviceProblems.add(new ServiceProblem(index, "Open", new WorkItem(index+10, "Unassigned"), queueStore.queueById(queueId)));
         }
     }
 
     public List<ServiceProblem> serviceProblemsForQueueId(final Integer queueId) {
         return newArrayList(filter(serviceProblems, new Predicate<ServiceProblem>() {
             public boolean apply(ServiceProblem serviceProblem) {
                 return serviceProblem.queue().id().equals(queueId);
             }
         }));
     }
 
     public void transfer() {
 
     }
 
     public void bulkTransfer(int destinationQueueId, final Collection<Integer> serviceProblemIds) {
         Collection<ServiceProblem> serviceProblemsToTransfer = Collections2.filter(serviceProblems, new Predicate<ServiceProblem>() {
             public boolean apply(ServiceProblem serviceProblem) {
                 return serviceProblemIds.contains(serviceProblem.serviceProblemId());
             }
         });
         Queue destinationQueue = queueStore.queueById(destinationQueueId);
         for (ServiceProblem serviceProblem : serviceProblemsToTransfer) {
             serviceProblem.setQueue(destinationQueue);
         }
     }
 }
