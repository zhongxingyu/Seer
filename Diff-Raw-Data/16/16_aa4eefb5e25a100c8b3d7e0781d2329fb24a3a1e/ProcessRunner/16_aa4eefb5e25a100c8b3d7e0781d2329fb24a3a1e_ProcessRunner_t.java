 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.StringTokenizer;
 
 /**
  * ProcessRunner is a separated thread for running command process
  * 
  * @author Guanyu Wang
  * */
public class ProcessRunner extends Thread{ 
   
   private boolean debug;
   private String command;
   private RunningProcessTable process_table;
   private MigratableProcess mp;
   private String commandarg;
   
   public ProcessRunner(String commandline, RunningProcessTable process_table) throws Exception {
     this.command = commandline;
     this.process_table = process_table;
     this.mp = null;
   }
   
   
   public ProcessRunner(MigratableProcess mp, String command, RunningProcessTable process_table) throws Exception {
     this.mp = mp;
     this.commandarg = command;
     this.process_table = process_table;
   }
   
   public void printDebugInfo(String s){
     if(debug)
       System.out.println("PrcessRunner: " + s);
   }
   
   public void run() {
     
     if(this.mp == null){
     
       StringTokenizer stoken = new StringTokenizer(command);
       String cmd = stoken.nextToken();
       String arguments = new String();
       while (stoken.hasMoreTokens())
         arguments += (stoken.nextToken() + " ");
   
       try {
         Class<?> ProcessClass = Class.forName(cmd);
         Constructor[] ProcessCtor = ProcessClass.getConstructors();
         Object Cmdprocess = ProcessCtor[0].newInstance((Object) command.split(" "));
         
         // TODO: how to decide cmdprocess is a (MigratableProcess) or not???
         
         Thread t = new Thread((MigratableProcess)Cmdprocess);
         if (process_table.containsKey((MigratableProcess)Cmdprocess))
           process_table.removeprocess((MigratableProcess)Cmdprocess);
         process_table.putprocess((MigratableProcess)Cmdprocess, cmd + " " + arguments);
         t.start();
         t.join();
         printDebugInfo("finished join");
         if(process_table.containsKey((MigratableProcess)Cmdprocess)){
           System.out.println("");
           System.out.println("Process " + process_table.get((MigratableProcess)Cmdprocess) + "was terminated");
           System.out.print("==> ");
           process_table.removeprocess((MigratableProcess)Cmdprocess);
         }
         else{
           System.out.println("Process " + command + " was moved");
           // TODO: suspend case, no need to output termination info.
 
         }
       } catch (ClassNotFoundException e) {
         System.out.println("");
         System.out.println("Cannot find the input command.");
       } catch (InvocationTargetException e) {
         System.out.println("");
         System.out.println("Invalid arguments for input command.");
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
     else {
       try {
         Thread t = new Thread(mp);
         if (process_table.containsKey(mp))
           process_table.removeprocess(mp);
         process_table.putprocess(mp, commandarg);
         t.start();
         t.join();
         printDebugInfo("finished join");
         if(process_table.containsKey(mp)){
           System.out.println("");
           System.out.println("Process " + process_table.get(mp) + "was terminated");
           System.out.print("==> ");
           process_table.removeprocess(mp);
         }
         else{
           System.out.println("Process " + process_table.get(mp) + " was moved");
           // TODO: suspend case, no need to output termination info.
           
         }
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
     
   }
 }
