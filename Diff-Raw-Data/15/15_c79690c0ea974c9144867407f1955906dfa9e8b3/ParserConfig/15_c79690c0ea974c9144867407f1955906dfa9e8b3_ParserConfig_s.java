 package sidekick.coffeescript;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import org.gjt.sp.util.Log;
 import org.gjt.sp.jedit.Buffer;
 
 import errorlist.DefaultErrorSource;
 import errorlist.ErrorSource;
 
 /**
  * Configuration object for the parser, providing options and callbacks for
  * TreeNode construction, error reporting and logging.
  */
 public class ParserConfig {
     public boolean showErrors;
     public boolean displayCodeParameters = false;;
     public boolean isCakefile = false;
     public int line = 0;
 
     private final Buffer buffer;
     private final DefaultErrorSource errorSource;
 
     private
     ParserConfig(Buffer buffer, DefaultErrorSource errorSource) {
         this.buffer = buffer;
         this.errorSource = errorSource;
     }
 
     /**
      * Build config for parsing.
      */
     static ParserConfig
     forParsing(Buffer buffer, DefaultErrorSource errorSource) {
         ParserConfig config = new ParserConfig(buffer, errorSource);
         config.showErrors = Options.getBool("showErrors");
         config.displayCodeParameters = Options.getBool("displayCodeParameters");
         config.isCakefile = buffer.getName().equals("Cakefile");
         return config;
     }
 
     /**
      * Build config for compiling.
      */
     static ParserConfig
     forCompiling(Buffer buffer, DefaultErrorSource errorSource) {
         ParserConfig config = new ParserConfig(buffer, errorSource);
         config.showErrors = true;
         return config;
     }
 
     /**
      * Logger function for the CoffeeScript parser.
     */
     public void
     logError(String message) {
         Log.log(Log.ERROR, CoffeeScriptSideKickParser.class, message);
     }
 
     /**
      * Reporter function for the CoffeeScript parser.
     */
     public void
    reportError(Integer line, String message) {
         if (showErrors) {
             if (line == null) {
                 line = Integer.MAX_VALUE;
             }
             this.errorSource.addError(
                 new DefaultErrorSource.DefaultError(this.errorSource,
                                                     ErrorSource.ERROR,
                                                     this.buffer.getPath(),
                                                     line,
                                                    0, 0,
                                                     message));
         }
     }
 
     /**
      * TreeNode factory for the CoffeeScript parser.
     */
     public DefaultMutableTreeNode
     makeTreeNode(String name, String type, String qualifier,
                     int firstLine, int lastLine) {
         CoffeeAsset asset = new CoffeeAsset(name, type, qualifier);
         asset.setStart(
             this.buffer.createPosition(
                 this.buffer.getLineStartOffset(firstLine)));
         asset.setEnd(
             this.buffer.createPosition(
                 this.buffer.getLineEndOffset(lastLine) - 1));
 
         return new DefaultMutableTreeNode(asset);
     }
 }
