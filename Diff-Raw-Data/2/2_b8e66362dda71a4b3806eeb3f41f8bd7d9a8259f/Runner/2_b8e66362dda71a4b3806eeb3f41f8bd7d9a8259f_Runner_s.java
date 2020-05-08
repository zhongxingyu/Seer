 import com.xenojoshua.xjf.netty.client.XjfNettyClient;
 import com.xenojoshua.xjf.netty.server.XjfNettyServer;
 import com.xenojoshua.xjf.system.XjfSystem;
 import com.xenojoshua.xjf.util.XjfValidator;
 
 import java.util.ArrayList;
 
 public class Runner {
 
     public static void main(String[] args) throws Exception {
 
         if (args.length != 4) {
             Runner.printUsage();return;
         }
 
         // initialize params
         boolean isIDE = false;
         String  mode = "server";
         String  host = "127.0.0.1";
         int     port = 8080;
 
         // isIDE: validation args[0]
         if (!XjfValidator.isNumeric(args[0])) {
             Runner.printUsage();return;
         }
         int ideParam = Integer.parseInt(args[0]);
         if (ideParam != 0 && ideParam != 1) {
             Runner.printUsage();return;
         } else if (ideParam == 1) {
             isIDE = true;
         }
         // mode: validation args[1]
         if (!args[1].equals("client") && !args[1].equals("server")) {
             Runner.printUsage();return;
         } else {
             mode = args[1];
         }
         // host: validation args[2]
         if (!XjfValidator.isIP(args[2])) {
             Runner.printUsage();return;
         } else {
             host = args[2];
         }
         // port: validation args[3]
         if (!XjfValidator.isNumeric(args[3])) {
             Runner.printUsage();return;
         }
         int portParam = Integer.parseInt(args[3]);
         if (!XjfValidator.isPort(portParam)) {
             Runner.printUsage();return;
         } else {
             port = portParam;
         }
 
         System.out.println(
             String.format(
                 "[xjf-netty] start with: %s %s %s %s ...",
                    args
             )
         );
 
         String jarFilePath = Runner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
         jarFilePath = jarFilePath.substring(0, jarFilePath.lastIndexOf("/") + 1);
 
         if (isIDE && jarFilePath.contains("classes")) { // means run in IDE("IntelliJ IDEA"), remove the tailing "classes"
             jarFilePath = jarFilePath.substring(0, jarFilePath.lastIndexOf("classes"));
         }
 
         XjfSystem.init(jarFilePath);
 
         if (mode.equals("server")) {
 
             new XjfNettyServer(host, port).run();
 
         } else if (mode.equals("client")) {
 
             XjfNettyClient client = new XjfNettyClient(host, port);
 
             client.send("CLIENT_MSG_TST_001");
             client.send("CLIENT_MSG_TST_002");
             client.send("CLIENT_MSG_TST_003");
             client.send("CLIENT_MSG_TST_004");
 
             client.run();
 
         }
     }
 
     /**
      * Print the usage information.
      */
     private static void printUsage() {
         System.err.println(
             String.format(
                 "Usage: %s <ide:1|0> <mode:server|client> <host> <port>",
                 Runner.class.getSimpleName()
             )
         );
     }
 }
