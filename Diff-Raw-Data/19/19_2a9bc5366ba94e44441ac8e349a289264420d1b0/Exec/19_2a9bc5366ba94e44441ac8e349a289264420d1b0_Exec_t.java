 package gotchas;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 
 class CommandRunner {
     private BufferedReader stdout;
     private BufferedReader stderror;
 
     public int execute(String command) {
         try {
             Process p = Runtime.getRuntime().exec(command);
  
             stdout = new BufferedReader(new 
                  InputStreamReader(p.getInputStream()));
 
             stderror = new BufferedReader(new 
                  InputStreamReader(p.getErrorStream()));
             
             return 0;
         } catch (IOException e) {
             e.printStackTrace();
             return 1;
         } finally {
         }
     }
 
     public String stdout() {
         StringBuilder lines = new StringBuilder();
         try {
             String line;
             while ((line = stdout.readLine()) != null) {
                 lines.append(line);
             }
         } catch (IOException e) {
         } finally {
         }
         return lines.toString();
     }
 
    public String stderr() {
         StringBuilder lines = new StringBuilder();
         try {
             String line;
             while ((line = stderror.readLine()) != null) {
                 lines.append(line);
             }
         } catch (IOException e) {
         } finally {
         }
         return lines.toString();
     }
 }
 
 
 interface SystemCommand {
     public String command();
 }
 
 class EchoCommand implements SystemCommand {
     private static final String COMMAND_NAME = "echo";
     private static final String COMMAND_OPTARGS = "-n";
     private static final String COMMAND_ARGS = "does it work";
     private List<String> command = null;
 
     public EchoCommand() {
         build();
     }
 
     private void build() {
         command = new ArrayList<String>();
         command.add(COMMAND_NAME);
         command.add(COMMAND_OPTARGS);
         command.add(COMMAND_ARGS);
     }
 
     public String command() {
         String commandLine = "";
         for (String word : command) {
             commandLine += " " + word;
         }
         return commandLine;
     }
 }
 
 
 class Exec {
 
     private CommandRunner exec = null;
 
     public int execute(SystemCommand command) {
         exec = new CommandRunner();
         int result = exec.execute(command.command());
         return result;
     }
 
     public String stdout() {
        return exec.stdout();
     }
 
     public String stderr() {
        return exec.stderr();
     }
 }
 
