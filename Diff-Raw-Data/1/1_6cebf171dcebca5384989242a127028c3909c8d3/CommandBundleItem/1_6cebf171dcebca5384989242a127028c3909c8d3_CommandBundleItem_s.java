 package kkckkc.jsourcepad.model.bundle;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Function;
 import com.google.common.base.Strings;
 import com.google.common.io.Files;
 import kkckkc.jsourcepad.model.*;
 import kkckkc.jsourcepad.model.bundle.snippet.Snippet;
 import kkckkc.jsourcepad.util.Config;
 import kkckkc.jsourcepad.util.io.NullWriter;
 import kkckkc.jsourcepad.util.io.ScriptExecutor;
 import kkckkc.jsourcepad.util.io.ScriptExecutor.Execution;
 import kkckkc.jsourcepad.util.io.TeeWriter;
 import kkckkc.jsourcepad.util.io.UISupportCallback;
 import kkckkc.syntaxpane.model.Interval;
 import kkckkc.utils.Os;
 import kkckkc.utils.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.swing.*;
 import javax.swing.text.BadLocationException;
 import java.io.*;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 @SuppressWarnings("restriction")
 public class CommandBundleItem implements BundleItem<Void> {
 	private static Logger logger = LoggerFactory.getLogger(CommandBundleItem.class);
 
 	public static final String OUTPUT_SHOW_AS_HTML = "showAsHTML";
 	public static final String OUTPUT_DISCARD = "discard";
 	public static final String OUTPUT_REPLACE_SELECTED_TEXT = "replaceSelectedText";
 	public static final String OUTPUT_SHOW_AS_TOOLTIP = "showAsTooltip";
 	public static final String OUTPUT_INSERT_AS_SNIPPET = "insertAsSnippet";
 	public static final String OUTPUT_AFTER_SELECTED_TEXT = "afterSelectedText";
     public static final String OUTPUT_CREATE_NEW_DOCUMENT = "openAsNewDocument";
     public static final String OUTPUT_REPLACE_DOCUMENT = "replaceDocument";
 
 	public static final String INPUT_NONE = "none";
 	public static final String INPUT_DOCUMENT = "document";
 	public static final String INPUT_SELECTION = "selection";
     public static final String INPUT_CHARACTER = "character";
     public static final String INPUT_LINE = "line";
     public static final String INPUT_WORD = "word";
     public static final String INPUT_SCOPE = "scope";
 
     public static final String BEFORE_SAVE_ALL = "saveModifiedFiles";
     public static final String BEFORE_SAVE_ACTIVE = "saveActiveFile";
 
 	private String output;
 	private String command;
 	private String input;
 	private String fallbackInput;
     private String beforeRunning;
 	private BundleItemSupplier bundleItemSupplier;
 	private Interval virtualSelection;
 
     public CommandBundleItem(BundleItemSupplier bundleItemSupplier, String command, String input, String fallbackInput, String output, String beforeRunning) {
 		this.bundleItemSupplier = bundleItemSupplier;
 		this.command = command;
 		this.input = input;
 		this.fallbackInput = fallbackInput;
 		this.output = output;
         this.beforeRunning = beforeRunning;
 	}
 	
 	public static CommandBundleItem create(BundleItemSupplier bundleItemSupplier, Map<?, ?> props) {
 	    return new CommandBundleItem(bundleItemSupplier,
 	    		(String) props.get("command"),
 	    		(String) props.get("input"),
 	    		(String) props.get("fallbackInput"),
 	    		(String) props.get("output"),
                 (String) props.get("beforeRunningCommand"));
     }
 
 	
 	public interface ExecutionMethod {
         Writer getWriter();
 
         void preExecute();
         void postExecute();
 
         void processResult(String s);
     }
 	
 	
 	public void execute(final Window window, Void context) throws Exception {
         if (! Os.isMac()) {
             fixShebang();
         }
 
         beforeRunning(window);
 
 		ScriptExecutor scriptExecutor = new ScriptExecutor(command, Application.get().getThreadPool());
         scriptExecutor.setDirectory(bundleItemSupplier.getFile().getParentFile().getParentFile());
 
 		final WindowManager wm = Application.get().getWindowManager();
 
         String inputText = getInput(window);
 
         if (inputText == null) return;
 
         final StringWriter stdoutWriter = new StringWriter();
 
 		final ExecutionMethod executionMethod = createExecutionMethod(window, wm, output);
         executionMethod.preExecute();
 
         UISupportCallback callback = new UISupportCallback(window) {
             @Override
             public void onAfterDone(Execution execution) {
                 super.onAfterDone(execution);
                 executionMethod.postExecute();
             }
 
             @Override
             public void onAfterSuccess(Execution execution) {
                 if (execution.getExitCode() >= 200 && execution.getExitCode() <= 207) {
                     String out = null;
                     switch (execution.getExitCode()) {
                         case 200:
                             out = OUTPUT_DISCARD;
                             break;
                         case 201:
                             out = OUTPUT_REPLACE_SELECTED_TEXT;
                             break;
                         case 202:
                             out = OUTPUT_REPLACE_DOCUMENT;
                             break;
                         case 203:
                             out = OUTPUT_AFTER_SELECTED_TEXT;
                             break;
                         case 204:
                             out = OUTPUT_INSERT_AS_SNIPPET;
                             break;
                         case 205:
                             out = OUTPUT_SHOW_AS_HTML;
                             break;
                         case 206:
                             out = OUTPUT_SHOW_AS_TOOLTIP;
                             break;
                         case 207:
                             out = OUTPUT_CREATE_NEW_DOCUMENT;
                             break;
                     }
 
                     String stdoutAsString = stdoutWriter.toString();
 
                     ExecutionMethod delegatedExecutionMethod = createExecutionMethod(window, wm, out);
                     delegatedExecutionMethod.preExecute();
                     try {
                         delegatedExecutionMethod.getWriter().write(stdoutAsString);
                         delegatedExecutionMethod.getWriter().flush();
                         delegatedExecutionMethod.getWriter().close();
                     } catch (IOException e) {
                         throw new RuntimeException(e);
                     }
                     delegatedExecutionMethod.postExecute();
                     delegatedExecutionMethod.processResult(stdoutAsString);
                 } else {
                     executionMethod.processResult(stdoutWriter.toString());
                 }
             }
         };
 
         scriptExecutor.execute(callback,
                 new StringReader(inputText),
                 new TeeWriter(stdoutWriter, executionMethod.getWriter()),
                 EnvironmentProvider.getEnvironment(window, bundleItemSupplier));
 	}
 
     private void fixShebang() throws IOException {
         File bundleFolder = bundleItemSupplier.getFile().getParentFile().getParentFile();
 
         File supportFolder = new File(bundleFolder, "Support");
         File supportCacheFolder = new File(Config.getTempFolder(), "Bundles/" + bundleFolder.getName() + "/Support");
 
         // Get newest timestamp of the Support folder
         long timestamp = 0;
         List<File> files = FileUtils.recurse(supportFolder);
         for (File f : files) {
             timestamp = Math.max(timestamp, f.lastModified());
         }
 
         // Get the timestamp of the modified Support folder
         long timestampOfSupportCache = 0;
         File timestampfile = new File(supportCacheFolder, ".timestamp");
         if (! timestampfile.exists()) {
             timestampfile.getParentFile().mkdirs();
         } else {
             timestampOfSupportCache = Long.parseLong(Files.toString(timestampfile, Charsets.UTF_8));
         }
 
         if (timestamp > timestampOfSupportCache) {
             logger.info("Applying shebang fix for " + bundleFolder.getName() + "/Support");
             for (File f : files) {
                 if (f.isDirectory()) continue;
 
                 byte[] contents = FileUtils.readBytes(f);
 
                 if (contents[0] == (byte) '#' && contents[1] == (byte) '!') {
                     StringBuilder shebangLine = new StringBuilder();
                     for (int i = 2; i < contents.length; i++) {
                         if (contents[i] == '\n') break;
                         shebangLine.append((char) contents[i]);
                     }
 
                     File wrapper = new File(supportCacheFolder,
                             f.getCanonicalPath().substring(supportFolder.getCanonicalPath().length()));
 
                     File to = new File(supportCacheFolder,
                             f.getCanonicalPath().substring(supportFolder.getCanonicalPath().length()) + ".real");
 
                     to.getParentFile().mkdirs();
 
                     // TODO: Need to check different script types
                     if (f.getCanonicalPath().endsWith(".rb")) {
                         Files.write(
                                 "#!/usr/bin/env ruby\n" +
                                 "exec %Q!" + shebangLine + " \"#{ENV['TM_BUNDLE_SUPPORT']}/" + f.getName() + ".real\" #{ARGV.join(\" \")}!",
                                 wrapper,
                                 Charsets.US_ASCII);
 
                     } else {
                         Files.write(
                                 "#!/bin/bash\n" +
                                 shebangLine + " \"$TM_BUNDLE_SUPPORT/" + f.getName() + ".real\" $*",
                                 wrapper,
                                 Charsets.US_ASCII);
                     }
 
                     Files.write(contents, to);
                 } else {
                     File to = new File(supportCacheFolder,
                             f.getCanonicalPath().substring(supportFolder.getCanonicalPath().length()));
                     to.getParentFile().mkdirs();
                     Files.write(contents, to);
                 }
             }
         }
 
         Files.write(Long.toString(timestamp), timestampfile, Charsets.UTF_8);
     }
 
     @Override
     public BundleStructure.Type getType() {
         return BundleStructure.Type.COMMAND;
     }
 
     private void beforeRunning(Window window) {
         if (beforeRunning == null || "nop".equals(beforeRunning)) return;
 
         if (BEFORE_SAVE_ALL.equals(beforeRunning)) {
             for (Doc doc : window.getDocList().getDocs()) {
                 doc.save();
             }
         } else if (BEFORE_SAVE_ACTIVE.equals(beforeRunning)) {
             window.getDocList().getActiveDoc().save();
         } else {
             throw new RuntimeException("Unsupported 'beforeRunningCommand'-value: '" + beforeRunning + "'");
         }
     }
 
     private ExecutionMethod createExecutionMethod(Window window, WindowManager wm, String output) {
 	    ExecutionMethod outputMethod;
 	    if (OUTPUT_SHOW_AS_HTML.equals(output)) {
 	    	outputMethod = new HtmlExecutionMethod(window, wm);
 	    } else {
 	    	outputMethod = new DefaultExecutionMethod(output, virtualSelection, window, wm);
 	    }
 	    return outputMethod;
     }
 
 	private String getInput(Window window) throws BadLocationException {
 		String text;
 		if (! INPUT_NONE.equals(input)) {
 			text = getTextForInput(input, window);
 			if (text == null || "".equals(text)) {
 				text = getTextForInput(fallbackInput == null ? INPUT_DOCUMENT : fallbackInput, window);
 			}
 		} else {
 			text = "";
 		}
 		
 		return text;
     }
 
 	private String getTextForInput(String type, Window window) {
         virtualSelection = null;
 
 		Buffer buffer = window.getDocList().getActiveDoc().getActiveBuffer();
 		if (INPUT_SELECTION.equals(type)) {
 			return buffer.getText(buffer.getSelection());
 		} else if (INPUT_DOCUMENT.equals(type)) {
 			return buffer.getText(buffer.getCompleteDocument());
         } else if (INPUT_CHARACTER.equals(type)) {
             Interval iv = Interval.createWithLength(buffer.getInsertionPoint().getPosition(), 1);
             if (iv.getEnd() >= buffer.getLength()) return null;
             
             virtualSelection = iv;
             return buffer.getText(iv);
         } else if (INPUT_LINE.equals(type)) {
             virtualSelection = buffer.getCurrentLine();
             return buffer.getText(virtualSelection);
         } else if (INPUT_WORD.equals(type)) {
             virtualSelection = buffer.getCurrentWord();
             return buffer.getText(virtualSelection);
         } else if (INPUT_SCOPE.equals(type)) {
             virtualSelection = buffer.getCurrentScope();
             return buffer.getText(virtualSelection);
 
 		} else if (type == null) {
 			return null;
 		} else {
 			throw new RuntimeException("Unsupported input type " + type);
 		}
 	}
 	
 
 	
 	
 
 	public static class HtmlExecutionMethod implements ExecutionMethod {
 		private Window window;
         private Writer writer;
         private BlockingQueue<Object> outputQueue;
         private String path;
 
         private static Logger logger = LoggerFactory.getLogger(HtmlExecutionMethod.class);
         private Writer processWriter;
 
 
         private Object SENTINEL = new Object();
 
         public HtmlExecutionMethod(Window window, WindowManager wm) {
 			this.window = window;
             outputQueue = new LinkedBlockingQueue<Object>();
         }
 
         @Override
         public Writer getWriter() {
             final StringBuilder buffer = new StringBuilder();
             final Function<String, String> transformation = new Function<String, String>() {
                 public String apply(String s) {
                     s = s.replaceAll("file://(?!localhost)", "http://localhost:" + Config.getHttpPort() + "/files");
                     return s;
                 }
             };
 
             processWriter = new Writer() {
                 @Override
                 public void write(char[] cbuf, int off, int len) throws IOException {
                     for (int i = off; i < (off + len); i++) {
                         buffer.append(cbuf[i]);
                         if (cbuf[i] == '\n') {
                            flushBuffer();
                         }
                     }
                 }
 
                 private void flushBuffer() {
                     String transformedResult = transformation.apply(buffer.toString());
                     outputQueue.add(transformedResult);
                     buffer.setLength(0);
                 }
 
                 @Override
                 public void flush() throws IOException {
                     flushBuffer();
                 }
 
                 @Override
                 public void close() throws IOException {
                     flushBuffer();
                     outputQueue.add(SENTINEL);
                 }
             };
             return processWriter;
         }
 
         @Override
         public void preExecute() {
             CommandBundleServer server = Application.get().getBeanFactory().getBean(CommandBundleServer.class);
             long id = server.register(new CommandBundleServer.Handler() {
                 public void handle(HttpServletResponse resp) throws IOException {
                     resp.setContentType("text/html");
 
                     writer = resp.getWriter();
                     writer.write(
                             "<script>" +
                             "TextMate = {}; " +
                             "TextMate.port = " + Config.getHttpPort() + "; " +
                             "TextMate.windowId = " + window.getId() + "; " +
                             "TextMate.system = function (cmd, handler) { " +
                             "    if (handler == null) { " +
                             "        xhr = new XMLHttpRequest(); " +
                             "        xhr.open('GET', 'http://localhost:' + TextMate.port + '/cmd/exec?cmd=' + escape(cmd), false); " +
                             "        xhr.send(null); " +
                             "        return { " +
                             "            outputString: xhr.responseText, " +
                             "            errorString: null, " +
                             "            status: xhr.getResponseHeader('X-ResponseCode') " +
                             "        }; " +
                             "    } else { " +
                             "        xhr = new XMLHttpRequest(); " +
                             "        xhr.open('GET', 'http://localhost:' + TextMate.port + '/cmd/exec?cmd=' + escape(cmd), false); " +
                             "        xhr.onreadystatechange = function() { " +
                             "            if (xhr.readyState == 4 && xhr.status != 404) { " +
                             "                handler({" +
                             "                    outputString: xhr.responseText, " +
                             "                    errorString: null, " +
                             "                    status: xhr.getResponseHeader('X-ResponseCode') " +
                             "                }); " +
                             "            } " +
                             "        };" +
                             "        xhr.send(null); " +
                             "        return {};" +
                             "    } " +
                             "}; " +
                             "</script>");
 
                     Object line;
                     try {
                         while ((line = outputQueue.take()) != SENTINEL) {
                             writer.write(line.toString());
                         }
                     } catch (InterruptedException e) {
                         throw new RuntimeException(e);
                     }
 
                     writer.write(
                             "<script>\n" +
                             "document.body.onclick = function(e) { " +
                             "  var target = e.target || e.srcElement; " +
                             "  if (target.tagName.toLowerCase() == 'a') { " +
                             "    if (target.href.match(/^txmt:\\/\\/open/)) { " +
                             "      location.href = target.href.replace(/txmt:\\/\\/open\\/?\\?([^'\" \\t\\n\\f\\r]+)/, 'http://localhost:' + TextMate.port + '/cmd/open?windowId=' + TextMate.windowId + '&$1');" +
                             "      return false;\n" +
                             "    } " +
                             "  }" +
                             "}" +
                             "</script>");
                     writer.close();
                 }
             });
 
             path = "/command/" + id;
 
             try {
                 Application.get().getBrowser().show(new URI("http://localhost:" + Config.getHttpPort() + path), false);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             } catch (URISyntaxException e) {
                 throw new RuntimeException(e);
             }
         }
 
         @Override
         public void postExecute() {
             try {
                 processWriter.close();
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
 
         @Override
         public void processResult(String s) {
         }
     }
 
 	public static class DefaultExecutionMethod implements ExecutionMethod {
 		private String output;
 		private Window window;
 		private WindowManager wm;
         private Interval virtualSelection;
 
         public DefaultExecutionMethod(String output, Interval virtualSelection, Window window, WindowManager wm) {
 			this.output = output;
             this.virtualSelection = virtualSelection;
 			this.window = window;
 			this.wm = wm;
 		}
 		
         @Override
         public Writer getWriter() {
             return new NullWriter();
         }
 
         @Override
         public void preExecute() {
         }
 
         @Override
         public void postExecute() {
         }
 
         @Override
         public void processResult(String s) {
             if (s == null) s = "";
 
             if (OUTPUT_SHOW_AS_TOOLTIP.equals(output)) {
                 if (! Strings.isNullOrEmpty(s.trim())) {
                     JOptionPane.showMessageDialog(window.getContainer(), s);
                 }
             } else if (OUTPUT_REPLACE_SELECTED_TEXT.equals(output)) {
                 Buffer buffer = window.getDocList().getActiveDoc().getActiveBuffer();
                 Interval selection = buffer.getSelection();
                 if (selection == null || selection.isEmpty()) {
                     if (virtualSelection != null) {
                         selection = virtualSelection;
                     } else {
                         selection = new Interval(0, buffer.getLength());
                     }
                 }
 
                 buffer.replaceText(selection, s, null);
             } else if (OUTPUT_AFTER_SELECTED_TEXT.equals(output)) {
                 Buffer buffer = window.getDocList().getActiveDoc().getActiveBuffer();
                 Interval selection = buffer.getSelection();
                 if (selection == null || selection.isEmpty()) {
                     if (virtualSelection != null) {
                         selection = virtualSelection;
                     } else {
                         selection = new Interval(0, buffer.getLength());
                     }
                 }
 
                 buffer.insertText(selection.getEnd(), s, null);
 
             } else if (OUTPUT_REPLACE_DOCUMENT.equals(output)) {
                 Buffer buffer = window.getDocList().getActiveDoc().getActiveBuffer();
                 Interval selection = buffer.getCompleteDocument();
                 buffer.replaceText(selection, s, null);
 
             } else if (OUTPUT_CREATE_NEW_DOCUMENT.equals(output)) {
                 Doc doc = window.getDocList().create();
                 doc.getActiveBuffer().insertText(0, s, null);
 
             } else if (OUTPUT_DISCARD.equals(output)) {
                 // Do nothing
 
             } else if (OUTPUT_INSERT_AS_SNIPPET.equals(output)) {
                 Buffer activeBuffer = window.getDocList().getActiveDoc().getActiveBuffer();
 
                 Interval selection = activeBuffer.getSelection();
                 if (selection == null || selection.isEmpty()) {
                     if (virtualSelection != null) {
                         selection = virtualSelection;
                     } else {
                         selection = new Interval(0, activeBuffer.getLength());
                     }
                 }
 
                 activeBuffer.remove(selection);
 
                 Snippet snippet = new Snippet(s, null);
                 snippet.insert(window, activeBuffer);
 
             } else {
                 throw new RuntimeException("Unsupported output " + output);
             }
         }
     }
 }
