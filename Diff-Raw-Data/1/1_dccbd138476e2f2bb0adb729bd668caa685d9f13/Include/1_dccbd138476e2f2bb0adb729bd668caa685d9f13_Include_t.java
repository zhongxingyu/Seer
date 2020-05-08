 package jason.asSyntax.directives;
 
 import jason.asSemantics.Agent;
 import jason.asSyntax.Pred;
 import jason.asSyntax.StringTerm;
 import jason.asSyntax.parser.as2j;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /** Implementation of the <code>include</code> directive. */
 public class Include implements Directive {
 
     static Logger logger = Logger.getLogger(Include.class.getName());
     public static final String CRPrefix = "ClassResource:";
 
     private List<String> aslSourcePath = null;
     
     public Agent process(Pred directive, Agent outerContent, Agent innerContent) {
         if (outerContent == null)
             return null;
         String file = ((StringTerm)directive.getTerm(0)).getString().replaceAll("\\\\", "/");
         try {
             String outerPrefix = outerContent.getASLSrc(); // the source file that has the include directive
             InputStream in;
             if (outerContent != null && outerPrefix != null) {
                 // check if the outer is URL
                 if (outerPrefix.startsWith("jar")) {
                     outerPrefix = outerPrefix.substring(0,outerPrefix.indexOf("!")+1) + "/";
                     file = checkPathAndFixWithSourcePath(file, aslSourcePath, outerPrefix);
                     in = new URL(file).openStream();
                     
                 } if (outerPrefix.startsWith(CRPrefix)) {
                     // outer is loaded from a resource ("application".jar) file, used for java web start
                     int posSlash = outerPrefix.lastIndexOf("/");
 
                     List<String> newpath = new ArrayList<String>();
                     if (outerPrefix.indexOf("/") != posSlash) { // has only one slash
                         newpath.add(outerPrefix.substring(CRPrefix.length()+1,posSlash));
                     }
                     newpath.addAll(aslSourcePath);
                     
                     file = checkPathAndFixWithSourcePath(file, newpath, CRPrefix+"/");
                     in = Agent.class.getResource(file.substring(CRPrefix.length())).openStream();
                 } else {
                     // get the directory of the source of the outer agent and 
                     // try to find the included source in the same directory
                     // or in the source paths
                     List<String> newpath = new ArrayList<String>();
                     newpath.add(new File(outerPrefix).getAbsoluteFile().getParent());
                     if (aslSourcePath != null)
                         newpath.addAll(aslSourcePath);
                     file = checkPathAndFixWithSourcePath(file, newpath, null);
                     in = new FileInputStream(file);
                 }
             } else {
                 in = new FileInputStream(checkPathAndFixWithSourcePath(file, aslSourcePath, null));         
             }
 
             Agent ag = new Agent();
            ag.initAg();
             ag.setASLSrc(file);
             as2j parser = new as2j(in); 
             parser.agent(ag);
             logger.fine("as2j: AgentSpeak program '"+file+"' parsed successfully!");
             return ag;
         } catch (FileNotFoundException e) {
             logger.log(Level.SEVERE,"The included file '"+file+"' was not found! (it is being included in the agent '"+outerContent.getASLSrc()+"')");
         } catch (Exception e) {
             logger.log(Level.SEVERE,"as2j: error parsing \"" + file + "\"", e);
         }
         return null;
     }
 
     public void setSourcePath(List<String> sp) {
         aslSourcePath = sp;
     }
     
     /** fix path of the asl code based on aslSourcePath, also considers code from a jar file (if urlPrefix is not null) */
     public static String checkPathAndFixWithSourcePath(String f, List<String> srcpath, String urlPrefix) {
         if (urlPrefix == null || urlPrefix.length() == 0) {
             if (new File(f).exists()) {
                 return f;
             } else if (srcpath != null) {
                 for (String path: srcpath) {
                     try {
                         File newname = new File(path + "/" + f.toString());
                         if (newname.exists()) {
                             return newname.getCanonicalFile().toString();
                         }
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         } else {
             if (testURLSrc(urlPrefix + f)) {
                 return urlPrefix + f;
             } else if (srcpath != null) {
                 for (String path: srcpath) {
                     String newname = urlPrefix + path + "/" + f;
                     if (testURLSrc(newname)) {
                         return newname;
                     }
                 }
             }
         }
         return f;
     }
     
     private static boolean testURLSrc(String asSrc) {
         try {
             if (asSrc.startsWith(CRPrefix)) {
                 Agent.class.getResource(asSrc.substring(CRPrefix.length())).openStream();
                 return true;
             } else {
                 Agent.class.getResource(asSrc).openStream();
                 return true;                
             }
         } catch (Exception e) {}
         return false;
     }    
 
 }
