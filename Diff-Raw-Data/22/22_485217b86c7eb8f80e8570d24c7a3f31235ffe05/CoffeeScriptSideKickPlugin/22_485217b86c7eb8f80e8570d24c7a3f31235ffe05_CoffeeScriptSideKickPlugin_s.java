 package sidekick.coffeescript;
 
 import java.util.Map;
 import java.util.HashMap;
 
 import org.gjt.sp.util.StringList;
 import org.gjt.sp.jedit.jEdit;
 import org.gjt.sp.jedit.Buffer;
 import org.gjt.sp.jedit.View;
 import org.gjt.sp.jedit.textarea.TextArea;
 import org.gjt.sp.jedit.textarea.Selection;
 import org.gjt.sp.jedit.EditPlugin;
 
 import errorlist.ErrorSource;
 import errorlist.DefaultErrorSource;
 
 public class CoffeeScriptSideKickPlugin extends EditPlugin {
     public static final String NAME = "sidekick.coffeescript";
     public static final String OPTION_PREFIX = "options.coffeescript.";
 
     private static final Map<View, DefaultErrorSource>
     errorSources = new HashMap<View, DefaultErrorSource>();
 
     private static DefaultErrorSource getErrorSource(View view) {
         if (errorSources.containsKey(view)) {
             return errorSources.get(view);
         } else {
             DefaultErrorSource errorSource =
                     new DefaultErrorSource("CoffeeScript", view);
             ErrorSource.registerErrorSource(errorSource);
             errorSources.put(view, errorSource);
             return errorSource;
         }
     }
 
     @Override
     public void stop() {
         for (ErrorSource errorSource : errorSources.values()) {
             ErrorSource.unregisterErrorSource(errorSource);
         }
         errorSources.clear();
     }
 
     /**
      * Compiles text of selection(s) into a new buffer.
      *
      * The text of each selection or or the whole buffer (if no selection
      * exists) is compiled with CoffeeScript. If successful the result will be
      * opened in a new buffer. Errors will be forwarded to ErrorList.
      *
      * @param   view    the current View
      */
     public static void compileSelection(View view) {
         StringList results = new StringList();
         ICoffeeScriptParser parser = new CoffeeScriptParser();
         TextArea textArea = view.getTextArea();
         DefaultErrorSource errorSource = getErrorSource(view);
 
         ParserConfig config =
                ParserConfig.forCompiling(view.getBuffer(), errorSource);
         errorSource.clear();
 
         if (textArea.getSelectionCount() == 0) {
             results.add(parser.compile(textArea.getText(), config));
         } else {
             for (Selection sel : textArea.getSelection()) {
                 config.line = sel.getStartLine();
                 results.add(
                     parser.compile(textArea.getSelectedText(sel), config));
             }
         }
         if (errorSource.getErrorCount() == 0) {
            Buffer buffer = jEdit.newFile(view.getEditPane());
            buffer.insert(0, results.join("\n").trim());
            buffer.setMode("javascript");
            buffer.setDirty(false);
         }
     }
 }
