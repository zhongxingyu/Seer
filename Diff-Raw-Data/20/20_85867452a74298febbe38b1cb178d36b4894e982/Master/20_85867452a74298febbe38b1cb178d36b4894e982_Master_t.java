 import com.google.gson.Gson;
 import org.jeromq.ZMQ;
 
 import java.util.Random;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentSkipListMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import static org.jeromq.ZMQ.*;
 
 
 public class Master
 {
     public void run()
     {
         try
         {
             ExecutorService executorService = Executors.newFixedThreadPool(2);
             ConcurrentSkipListMap<UUID, TaskRequest> tasks = new ConcurrentSkipListMap<UUID, TaskRequest>();
             executorService.execute(new MasterSender(tasks));
             executorService.execute(new MasterReceiver(tasks));
 
         } catch (Exception e)
         {
             System.err.println(e);
         }
     }
 
     public class MasterSender implements Runnable
     {
         private Gson gson = new Gson();
         private Random random = new Random();
         private ConcurrentSkipListMap<UUID, TaskRequest> tasks;
 
         public MasterSender(ConcurrentSkipListMap<UUID, TaskRequest> tasks)
         {
             this.tasks = tasks;
         }
 
         @Override
         public void run()
         {
             try
             {
 
 
                 Context context = context(1);
 
                 //  Socket to receive messages on
                 Socket publisher = context.socket(PUB);
                 publisher.bind("tcp://*:5557");
 
                 try
                 {
                     while (true)
                     {
                         TaskRequest taskRequest = new TaskRequest(random.nextInt(), random.nextInt(), UUID.randomUUID());
                         publisher.send(gson.toJson(taskRequest).getBytes(), 0);
                         tasks.putIfAbsent(taskRequest.Id, taskRequest);
                         System.out.println(" [x] Master sent '" + taskRequest + "'");
                     }
                 } catch (Exception e)
                 {
                     e.printStackTrace();
                 }
                 publisher.close();
                 publisher.close();
                 context.term();
             } catch (Exception e)
             {
                 e.printStackTrace();
             }
         }
     }
 
     public class MasterReceiver implements Runnable
     {
 
         private Gson gson = new Gson();
 
         private ConcurrentSkipListMap<UUID, TaskRequest> tasks;
 
         public MasterReceiver(ConcurrentSkipListMap<UUID, TaskRequest> tasks)
         {
             this.tasks = tasks;
         }
 
         @Override
         public void run()
         {
 
             Context context = context(1);
 
             //  Socket to send messages to
             Socket receiver = context.socket(PULL);
             receiver.bind("tcp://*:5558");
 
             try
             {
 
                 while (true)
                 {
 
                     String string = new String(receiver.recv(0)).trim();
                     TaskResponse response = gson.fromJson(string, TaskResponse.class);
 
                     TaskRequest request = tasks.get(response.Id);
                     if (request == null)
                     {
                         System.err.println(String.format(" [x] Master received a bad response with id=%s", response.Id));
                     } else
                     {
                         System.out.println(" [x] Master received '" + response + "' for " + request);
                     }
 
                 }
             } catch (Exception e)
             {
                 System.err.println(e.getStackTrace());
             }
             receiver.close();
             receiver.close();
             context.term();
         }
     }
 }
