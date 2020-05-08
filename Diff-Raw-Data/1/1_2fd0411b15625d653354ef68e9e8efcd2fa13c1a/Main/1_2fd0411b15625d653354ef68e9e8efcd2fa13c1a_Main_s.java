 package task2.graphviz;
 
 import java.io.FileWriter;
 import java.io.Writer;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 
 import task1.FSM;
 
 public class Main {
   public static void main(String[] args) {
     if (args.length != 2) {
       System.out.println("Usage: input-prolog-filename output-filename");
       System.exit(0);
     }
     try {
       Velocity.init();
       VelocityContext context = new VelocityContext();
       FSM fsm = new FSM(args[0]);
 
       context.put("nodes", fsm.nodes);
 
       context.put("edges", fsm.edges);
 
       Template template = null;
 
       template = Velocity.getTemplate("task2-fsm.vm");
       Writer writer = new FileWriter(args[1]+".dot");
       context.put("filename", args[1]);
 
       template.merge(context, writer);
       writer.close();
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 }
