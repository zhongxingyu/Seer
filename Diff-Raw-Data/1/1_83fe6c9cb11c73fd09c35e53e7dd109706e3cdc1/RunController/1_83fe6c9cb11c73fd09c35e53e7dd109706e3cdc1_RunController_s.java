 package sd.controler;
 
 import java.rmi.Naming;
 
 import sd.interfaces.InterfaceControlador;
 
 public class RunController {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
         InterfaceControlador ic;
         
         System.out.println("Server object created");
         try
         {
         	ic = new Controller();
             System.out.println("Binding controller " + ic);
             Naming.rebind("rmi://localhost/Controller", ic);
             System.out.println("Controller Binded!");
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         System.out.println("Controller Running!");
     }
 }
