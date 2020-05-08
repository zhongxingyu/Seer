 package haw.ai.rn.client;
 
 public class Application {
     public static void main(String[] args) {
         System.out.println("Start client");
         
         final Controller controller = new Controller();
        controller.setView(new Chat(controller));
         controller.run();
 
         System.out.println("Stopping client");
     }
 }
