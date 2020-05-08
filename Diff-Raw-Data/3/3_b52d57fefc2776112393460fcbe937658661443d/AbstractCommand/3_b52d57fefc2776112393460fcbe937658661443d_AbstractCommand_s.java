 package com.cloudbees.sdk;
 
 import org.kohsuke.args4j.ClassParser;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 import javax.inject.Inject;
 import java.lang.reflect.Field;
 import java.util.List;
 
 /**
  * Partial implementation of {@link ICommand} that uses args4j.
  * @author Kohsuke Kawaguchi
  */
 public abstract class AbstractCommand extends ICommand {
     @Inject
     Verbose verbose;
     
     public abstract int main() throws Exception;
 
     @Override
     public int run(List<String> args) throws Exception {
         CmdLineParser p = createParser();
         try {
             p.parseArgument(args.subList(1,args.size()));
             return main();
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             p.printUsage(System.err);
             return 1;
         }
     }
 
     protected CmdLineParser createParser() {
         CmdLineParser p = new CmdLineParser(this);
 
         // if any injected component define options, include them
         for (Class c = getClass(); c!=null; c=c.getSuperclass()) {
             for (Field f : c.getDeclaredFields()) {
                 if (f.isAnnotationPresent(Inject.class)) {
                     try {
                         f.setAccessible(true);
                         new ClassParser().parse(f.get(this), p);
                     } catch (IllegalAccessException e) {
                         throw new Error(e);
                     }
                 }
             }
         }
         return p;
     }
 
     @Override
     public void printHelp(List<String> args) {
         CmdLineParser p = createParser();
         p.printUsage(System.err);
     }
 }
