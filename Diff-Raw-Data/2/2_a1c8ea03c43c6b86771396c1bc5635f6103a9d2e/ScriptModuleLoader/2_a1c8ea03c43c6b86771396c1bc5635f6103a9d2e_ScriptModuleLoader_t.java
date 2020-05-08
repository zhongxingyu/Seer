 package org.freecode.irc.votebot;
 
 import org.freecode.irc.votebot.api.ExternalModule;
 import org.freecode.irc.votebot.modules.admin.LoadModules;
 import org.python.core.PyObject;
 import org.python.util.PythonInterpreter;
 
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 /**
  * Loads scripted modules for FreeVoteBot.
  * Modules may be scripted in Jython
  *
  * @author Shivam Mistry
  */
 
 public class ScriptModuleLoader {
 
 
     private final FreeVoteBot fvb;
     private final PythonInterpreter interpreter;
 
 
     /**
      * Initialises a ScriptModuleLoader
      *
      * @param fvb the instance of {@link FreeVoteBot} where the modules should be loaded to
      */
     public ScriptModuleLoader(FreeVoteBot fvb) {
         this.fvb = fvb;
         Properties props = new Properties();
        props.setProperty("python.path", System.getProperty("java.class.path") + ":" + LoadModules.MODULES_DIR.getAbsolutePath());
         PythonInterpreter.initialize(System.getProperties(), props,
                 new String[]{""});
         interpreter = new PythonInterpreter();
     }
 
 
     /**
      * Loads a module from a Python file that contains a class with the <b>SAME</b> name as the file.
      * The class must extend {@link ExternalModule}.
      *
      * @param file the file to load the {@link org.freecode.irc.votebot.api.ExternalModule} from
      * @return the {@link ExternalModule} loaded, or <tt>null</tt>
      * @throws IOException     if the file was not found
      * @throws ScriptException if the script failed to load
      */
     public ExternalModule loadFromFile(final InputStream file, final String name) throws IOException, ScriptException {
         if (file == null) {
             throw new IOException("Stream is null!");
         }
         if (name.endsWith(".py")) {
 
             String clzName = name.replace(".py", "");
             interpreter.exec(String.format("from %s import %s", clzName, clzName));
             //interpreter.execfile(file);
             PyObject object = interpreter.get(clzName);
             PyObject buildObject = object.__call__();
             ExternalModule ext = (ExternalModule) buildObject.__tojava__(ExternalModule.class);
             ext.setFvb(fvb);
             ext.setExtName(clzName);
             /*interpreter.execfile(file);
             Object o = interpreter.get("module", ExternalModule.class);
             ExternalModule ext = (ExternalModule) o;
             ext.setFvb(fvb);                                         */
             return ext;
         }
         return null;
 
     }
 }
