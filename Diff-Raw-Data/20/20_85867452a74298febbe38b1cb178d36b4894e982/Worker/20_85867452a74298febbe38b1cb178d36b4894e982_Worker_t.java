 import com.google.gson.Gson;
 import org.jeromq.ZMQ;
 
 import java.util.UUID;
 import java.util.concurrent.ConcurrentSkipListMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import static org.jeromq.ZMQ.*;
 
 public class Worker
 {
     public void run()
     {
         try
         {
             ExecutorService executorService = Executors.newSingleThreadExecutor();
             executorService.execute(new WorkerProccessor());
 
         } catch (Exception e)
         {
             e.printStackTrace();
         }
     }
 
     public class WorkerProccessor implements Runnable
     {
         private Gson gson = new Gson();
 
         @Override
         public void run()
         {
 
             Context context = context(1);
 
             //  Socket to receive messages on
             Socket subscriber = context.socket(SUB);
             subscriber.connect("tcp://localhost:5557");
             subscriber.subscribe("");
             //  Socket to send messages to
             Socket sender = context.socket(PUSH);
             sender.connect("tcp://*:5558");
 
             try
             {
                 while (true)
                 {
                     String string = new String(subscriber.recv(0)).trim();
                     TaskRequest request = gson.fromJson(string, TaskRequest.class);
 
                     System.out.println(String.format(" [x] Worker received %s", request));
                    Thread.sleep(5000);
                     TaskResponse response = new TaskResponse(request.A + request.B, request.Id);
                     sender.send(gson.toJson(response).getBytes(), 0);
                     System.out.println(String.format(" [x] Worker sent %s", request));
 
                 }
             } catch (Exception e)
             {
                 e.printStackTrace();
             }
             sender.close();
             subscriber.close();
             context.term();
         }
     }
 }
