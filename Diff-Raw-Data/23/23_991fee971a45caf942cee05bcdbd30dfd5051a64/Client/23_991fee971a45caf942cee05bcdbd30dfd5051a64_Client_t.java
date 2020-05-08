 public class Client {
     public static void main(String[] args) {
         TaskInfo info = new TaskInfo();
        info.cmd = "/bin/sleep";
         info.arguments = "60";
         info.framework_name = "Binos";
         info.need_cpu = 0.2;
         info.need_memory = 300;
         String address = "10.5.0.174:9997";
         long id = Cello.Submit(address, info);
         System.out.println("submit success.task id is " + id);
     }
 }
