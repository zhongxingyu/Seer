 package mbs2.pr02;
 
 import java.rmi.Naming;
 import java.rmi.RMISecurityManager;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 
 public class ComputeServer extends UnicastRemoteObject implements Compute {
 
   public ComputeServer() throws RemoteException {
     super();
   }
   
   public Object executeTask(Task t) {
     return t.execute();
   }
   
   public static void main(String[] args) {
     setRmiCodebase();
     System.setSecurityManager(new RMISecurityManager());
     try {
       Compute engine = new ComputeServer();
       Naming.rebind("//localhost:1099/Compute", engine);
       System.out.println("ComputeServer started.");
     } catch (Exception ex) {
       ex.printStackTrace();
     }
   }
 
   /**
    * RMI registry mora da zna gde ce pronaci klase koje cine server. Ta lokacija
    * se definise sistemskim propertijem pod nazivom java.rmi.server.codebase.
    * Serverski program se moze pozvati tako da se ovaj properti definise u
    * komandnoj liniji:
    * 
    * java -Djava.rmi.server.codebase=file://path-to-dir-or-jar class-name
    * 
    * Ako se kao lokacija navodi direktorijum, putanja se mora zavrsiti kosom 
    * crtom (/).
    * 
    * Ova metoda sluzi da definise properti ako on nije definisan prilikom poziva
    * programa iz komandne linije. Pri tome se gleda da li se program pokrece iz
    * jar fajla ili je raspakovan u fajl sistemu.
    */
   private static void setRmiCodebase() {
     String codebase = System.getProperty("java.rmi.server.codebase");
     if (codebase != null)
       return;
     
     String testFile = "/" + ComputeServer.class.getName().replace('.', '/') + ".class";
     String url = ComputeServer.class.getResource(testFile).toString();
     if (url.startsWith("jar:")) {
       int pos = url.indexOf(".jar");
       int lastSlash = url.lastIndexOf('/', pos);
       codebase = "file://" + url.substring(9, pos + 4);
       codebase += " file://" + url.substring(9, lastSlash+1) + "Client.jar";
     } else if (url.startsWith("file:")) {
       int pos = url.indexOf("bin");
       codebase = "file://" + url.substring(5, pos+4);
     } else // WTF
       return;
     System.err.println("Setting RMI codebase to " + codebase);
     System.setProperty("java.rmi.server.codebase", codebase);
     System.setProperty("java.rmi.server.useCodebaseOnly", "true");
   }

  private static final long serialVersionUID = 7399672500745174821L;
 }
