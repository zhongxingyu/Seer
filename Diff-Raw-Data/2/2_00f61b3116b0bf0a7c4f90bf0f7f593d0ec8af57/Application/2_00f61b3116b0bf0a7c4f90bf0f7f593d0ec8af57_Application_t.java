 package paulenka.aleh.pm.appletree;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 
 public class Application {
 
     private final static String COMMAND_GROW = "grow";
     private final static String COMMAND_SHAKE = "shake";
     private final static String COMMAND_EXIT = "exit";
 
     private AppleTree appleTree;
 
     public Application(String[] args) {
         appleTree = new AppleTree();
     }
 
     public static void main(String[] args) {
         Application application = new Application(args);
         application.execute();
     }
 
     protected AppleTree getAppleTree() {
         return appleTree;
     }
 
     protected void execute() {
         try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
             System.out.println("Apple tree is ready!");
             String command;
             String[] args;
             do {
                 String line = in.readLine();
                 command = getCommand(line);
                 args = getArgs(line);
                 switch (command) {
                     case COMMAND_GROW:
                         executeGrow(args);
                         break;
                     case COMMAND_SHAKE:
                         executeShake(args);
                         break;
                     case COMMAND_EXIT:
                         executeExit();
                         break;
                     default:
                         executeError(command);
                         break;
                 }
             } while (!COMMAND_EXIT.equals(command));
         } catch (IOException ex) {
             ex.printStackTrace();
         }
     }
 
     protected String getCommand(String line) {
         String[] parts = line.split("\\s");
         return parts.length > 0 ? parts[0] : "";
     }
 
     protected String[] getArgs(String line) {
         String[] parts = line.split("\\s");
         return parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[]{};
     }
 
     protected void executeGrow(String[] args) {
         if (args.length > 0) {
             for (String arg : args) {
                 try {
                     int fallen = getAppleTree().grow(Integer.parseInt(arg));
                     System.out.println("You wait " + arg + " days.");
                    describeGrowResult(fallen);
                 } catch (NumberFormatException ex) {
                     System.out.println("Can't wait for \"" + arg + "\" days, it's not a number!");
                 }
             }
         } else {
             int fallen = getAppleTree().grow();
             System.out.println("You wait for a long time...");
             describeGrowResult(fallen);
         }
     }
 
     protected void describeGrowResult(int fallen) {
         switch (fallen) {
             case 0:
                 System.out.println("Unfortunately apple tree seemns to be empty.");
                 break;
             case 1:
                 System.out.println("Only one apple is grown on the tree.");
                 break;
             default:
                 System.out.println(fallen + " apples are grown on the tree.");
                 break;
         }
     }
 
     protected void executeShake(String[] args) {
         if (args.length > 0) {
             for (String arg : args) {
                 try {
                     int fallen = getAppleTree().shake(Integer.parseInt(arg));
                     System.out.println("You've shaken the apple tree with full force for " + arg + " seconds.");
                     describeShakeResult(fallen);
                 } catch (NumberFormatException ex) {
                     System.out.println("Can't shake with for \"" + arg + "\" seconds, it's not a number!");
                 }
             }
         } else {
             int fallen = getAppleTree().shake();
             System.out.println("You've shaken the apple tree with full force.");
             describeShakeResult(fallen);
         }
     }
 
     protected void describeShakeResult(int fallen) {
         switch (fallen) {
             case 0:
                 System.out.println("Unfortunately nothing happens.");
                 break;
             case 1:
                 System.out.println("Only one apple fall down");
                 break;
             default:
                 System.out.println(fallen + " apples fall down.");
                 break;
         }
     }
 
     protected void executeExit() {
         System.out.println("You've just cut your wonderful apple tree down...");
     }
 
     protected void executeError(String command) {
         System.out.println("\"" + command + "\" is an incorrect command.");
         System.out.println("Available commands: " + COMMAND_GROW + ", " + COMMAND_SHAKE + ", " + COMMAND_EXIT);
     }
 }
