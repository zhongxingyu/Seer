 package net.caprazzi.giddone.worker;
 
 import com.google.common.base.Optional;
 import com.google.inject.Inject;
 import com.google.inject.name.Named;
 import net.caprazzi.giddone.RandomStringGenerator;
 import net.caprazzi.giddone.deploy.DeployService;
 import net.caprazzi.giddone.deploy.PresentationService;
 import net.caprazzi.giddone.hook.HookQueueClient;
 import net.caprazzi.giddone.hook.PostReceiveHook;
 import net.caprazzi.giddone.hook.QueueElement;
 import net.caprazzi.giddone.parsing.Todo;
 import net.caprazzi.giddone.parsing.TodoRecord;
 import net.caprazzi.giddone.parsing.TodoSnapshot;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.MDC;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class HookQueueExecutor {
 
     private static final Logger Log = LoggerFactory.getLogger(HookQueueExecutor.class);
 
     private final ExecutorService executor = Executors.newSingleThreadExecutor();
     private final HookQueueClient client;
     private final long pollDelay;
     private final RepositoryParser repositoryParser;
     private final DeployService deployService;
     private final PresentationService presentationService;
 
     private final String executorId = RandomStringGenerator.randomString();
     private long jobCount = 0;
 
     @Inject
     public HookQueueExecutor(HookQueueClient client, @Named("hook-worker-polling") long pollDelay, RepositoryParser repositoryParser, DeployService deployService, PresentationService presentationService) {
         this.client = client;
         this.pollDelay = pollDelay;
         this.repositoryParser = repositoryParser;
         this.deployService = deployService;
         this.presentationService = presentationService;
     }
 
     private Optional<QueueElement> next() {
         try {
             return client.headValue();
         } catch (IOException e) {
             return Optional.absent();
         }
     }
 
     public void start() {
 
         executor.submit(new Runnable() {
             @Override
             public void run() {
                 MDC.put("executorId", "executor=" + executorId + " ");
                 while(!executor.isShutdown()) {
 
                     Optional<QueueElement> value = next();
                     if (!value.isPresent()) {
                         sleep(pollDelay);
                         continue;
                     }
 
                     MDC.put("jobId", "job=" + jobCount++ + " ");
                     MDC.put("elementId", "el=" + value.get().getId() + " ");
                    MDC.put("repoId", "repo=" + value.get().getValue().getRepository().getCloneUrl());
 
                     try {
                         process(value.get().getValue());
                         try {
                             client.success(value.get().getId());
                         }
                         catch (Exception ex) {
                             Log.error("Error while reporting success to queue: {}", ex);
                         }
                     }
                     catch (Exception ex) {
                         Log.error("Error while processing {}: {}", value.get(), ex);
                         ex.printStackTrace();
                         try {
                             client.error(value.get().getId(), ex);
                         } catch (IOException e) {
                             Log.error("Error while reporting error to queue: {}", ex);
                         }
                     }
                     finally {
                         MDC.remove("jobId");
                         MDC.remove("elementId");
                         MDC.remove("repoId");
                     }
                 }
                 MDC.remove("executorId");
             }
         });
     }
 
     // TODO: move the processing part to a Worker class
     private void process(PostReceiveHook hook) throws Exception {
         Iterable<Todo> todos = repositoryParser.parse(hook.getRepository().getCloneUrl(), hook.getBranch());
 
         // TODO: would be interesting to try and keep the iterable abstraction
         LinkedList<TodoRecord> records = new LinkedList<TodoRecord>();
         for(Todo todo : todos) {
             System.out.println("\t" + todo.getComment().getSource().getFile().getFileName() + ":\t" + todo.getLabel() + ": " + todo.getTodo());
             records.add(TodoRecord.from(todo));
         }
 
         TodoSnapshot snapshot = new TodoSnapshot(new Date(), hook, records);
 
         String html = presentationService.asHtml(snapshot);
 
         deployService.deployHtmlPage(snapshot, html);
 
         // TODO: cleanup old repos if there are no errors
     }
 
     private void sleep(long pollDelay) {
         try {
             Thread.sleep(pollDelay);
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
     }
 }
