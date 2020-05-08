 package sidekick.coffeescript;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import org.gjt.sp.util.Log;
 import org.gjt.sp.jedit.Buffer;
 
 import sidekick.SideKickParsedData;
 import errorlist.DefaultErrorSource;
 import errorlist.ErrorSource;
 
/** Wrapper around the actual parser, providing callbacks for TreeNode
    construction error reporting and logging
     */
 public class ParserRunner{
     private Buffer buffer;
     private DefaultErrorSource errorSource;
 
     public SideKickParsedData
     run(ICoffeeScriptParser parser, Buffer buffer, DefaultErrorSource errorSource) {
         this.buffer = buffer;
         this.errorSource = errorSource;
         String name = buffer.getName();
 
         this.showErrors = Options.getBool("showErrors");
         this.displayCodeParameters = Options.getBool("displayCodeParameters");
         this.isCakefile = name.equals("Cakefile");
 
         this.errorSource.clear();
         SideKickParsedData parsedData = new SideKickParsedData(name);
         parser.parse(this.buffer.getText(), parsedData.root, this);
         return parsedData;
     }
 
     // interface for CoffeeScriptParser.coffee
 
     public boolean showErrors;
     public boolean displayCodeParameters;
     public boolean isCakefile;
 
     public void
     logError(String message) {
         Log.log(Log.ERROR, this, message);
     }
 
     public void
     reportError(Integer line, String message) {
         if(line == null) {
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
 
     public DefaultMutableTreeNode
     makeTreeNode(String name, String type, Integer firstLine, Integer lastLine) {
         if(type.equals("hidden")) {
             name = "-" + name;
         } else if(type.equals("property")) {
             name = " " + name;
         }
         CoffeeAsset asset = new CoffeeAsset(name);
         asset.setStart(
             this.buffer.createPosition(
                 this.buffer.getLineStartOffset(firstLine)));
         asset.setEnd(
             this.buffer.createPosition(
                 this.buffer.getLineEndOffset(lastLine) - 1));
 
         return new DefaultMutableTreeNode(asset);
     }
 }
