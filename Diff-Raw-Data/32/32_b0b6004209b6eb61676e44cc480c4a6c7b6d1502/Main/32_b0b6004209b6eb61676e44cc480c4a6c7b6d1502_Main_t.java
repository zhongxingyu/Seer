 /*
  * Author: Dabo Ross
  * Website: www.daboross.net
  * Email: daboross@daboross.net
  */
 package net.daboross.mavenbukkitcreator;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import static java.lang.System.out;
 
 /**
  *
  * @author daboross
  */
 public class Main {
 
     private static BufferedReader in;
 
     private static void init() {
         out.println("Welcome to Maven Bukkit Project Creator");
         Reader r = new InputStreamReader(System.in);
         in = new BufferedReader(r);
     }
 
     private static String ask(String question) throws IOException {
         out.printf(question + "\n> ");
         return in.readLine();
     }
 
     private static boolean askBoolean(String question) throws IOException {
         out.printf(question + "\n> ");
         while (true) {
             String line = in.readLine();
             if (line.equalsIgnoreCase("yes")) {
                 return true;
             } else if (line.equalsIgnoreCase("no")) {
                 return false;
             } else {
                 out.printf("'" + line + "' isn't a valid answer.\n(yes or no)> ");
             }
         }
     }
 
     public static void main(String[] args) {
         try {
             init();
             makeProject();
         } catch (IOException ioe) {
             ioe.printStackTrace(System.err);
        } catch (InterruptedException ie) {
            ie.printStackTrace(System.err);
         }
     }
 
    private static void makeProject() throws IOException, InterruptedException {
        String name, desc, gitOriginName;
         while (true) {
             name = ask("What should the project be called?");
             if (askBoolean("Project will be called '" + name + "'. Is this OK?")) {
                 break;
             }
         }
         while (true) {
             desc = ask("What should the project's description be?");
             if (askBoolean("Project will be described as '" + desc + "'. Is this OK?")) {
                 break;
             }
         }
        while (true) {
            gitOriginName = ask("What is the remote git repository?");
            if (askBoolean("Remote git repository is '" + gitOriginName + "'. Is this OK?")) {
                break;
            }
        }
        new ProjectCreator(name, desc, gitOriginName).create();
     }
 }
