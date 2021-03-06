 package sidekick.haxe;
 
 import static sidekick.haxe.HaXeSideKickPlugin.trace;
 
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.gjt.sp.jedit.Buffer;
 import org.gjt.sp.jedit.Mode;
 import org.gjt.sp.jedit.View;
 import org.gjt.sp.jedit.jEdit;
 import org.gjt.sp.jedit.textarea.TextArea;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import completion.service.CompletionCandidate;
 import completion.service.CompletionProvider;
 import completion.util.CodeCompletionField;
 import completion.util.CodeCompletionMethod;
 import completion.util.CompletionUtil;
 import completion.util.CtagsCompletionCandidate;
 
 import ctagsinterface.index.TagIndex;
 import ctagsinterface.main.CtagsInterfacePlugin;
 import ctagsinterface.main.Tag;
 
 /**
  * Service for the Completion plugin.
  */
 public class HaxeCodeCompletion
     implements CompletionProvider
 {
     public HaxeCodeCompletion ()
     {
         super();
         haxeMode = new HashSet<Mode>();
         haxeMode.add(jEdit.getMode("haxe"));
     }
 
     /**
      * The haxe compiler is only called when the most recent character is a dot.  Calling the
      * compiler every other time would be to costly.
      */
     public List<CompletionCandidate> getCompletionCandidates (View view)
     {
         List<CompletionCandidate> codeCompletions = new ArrayList<CompletionCandidate>();
 
         getImportCompletions(view, codeCompletions);
         getEnumSwitchCompletions(view, codeCompletions);
         getHaxeCompilerCompletions(view, codeCompletions);
         getCtagsCompletions(view, codeCompletions);
 
         return codeCompletions;
     }
 
     public Set<Mode> restrictToModes ()
     {
         return haxeMode;
     }
 
     /**
      * Get local members and Haxe classes.
      */
     protected void getCtagsCompletions (View view, List<CompletionCandidate> candidates)
     {
         //Ctags cannot current complete dot completion, save that for the compiler.
         if (CompletionUtil.isDotCompletion(view)) {
             return;
         }
 
         List<CompletionCandidate> localCandidates = new ArrayList<CompletionCandidate>();
         String prefix = CompletionUtil.getCompletionPrefix(view);
         prefix = prefix == null ? "" : prefix;
 
         //If the prefix is all lower case, ignore case
         boolean islowercase = prefix.toLowerCase().equals(prefix);
         //Local members
         String q = (islowercase ? TagIndex._NAME_LOWERCASE_FLD : TagIndex._NAME_FLD) + ":" + prefix + "*"
             + " AND " + TagIndex._PATH_FLD + ":" + view.getBuffer().getPath() + " AND (kind:function OR kind:variable)";
 
         Vector<Tag> tags = CtagsInterfacePlugin.runScopedQuery(view, q);
 
         for (Tag t : tags) {
             if (t.getName().length() > 1) {
                 localCandidates.add(new CtagsCompletionCandidate(t));
             }
         }
 
 
         TextArea ta = view.getTextArea();
         //If we're not dot-completing, look for classes
         if (prefix.length() > 0 && !ta.getText(ta.getCaretPosition() - 1 - prefix.length(), 1).equals(".")) {
             q = (islowercase ? TagIndex._NAME_LOWERCASE_FLD : TagIndex._NAME_FLD) + ":" + prefix + "* AND (kind:class OR kind:enum)" +
                 " AND " + TagIndex._PATH_FLD + ":*.hx";
             tags = CtagsInterfacePlugin.runScopedQuery(view, q);
             Set<String> classes = new HashSet<String>();
             for (Tag t : tags) {
                 if (!classes.contains(t.getName()) && !prefix.equals(t.getName())) {
                     localCandidates.add(new CtagsCompletionCandidate(t));
                     classes.add(t.getName());
                 }
             }
         }
 
         Collections.sort(localCandidates);
         candidates.addAll(localCandidates);
 
     }
 
     protected void getHaxeCompilerCompletions (View view, List<CompletionCandidate> candidates)
     {
         TextArea ta = view.getTextArea();
         int dotPosition = ta.getCaretPosition();
         if (!ta.getText(dotPosition - 1, 1).equals(".")) {
             return;
         }
 
         // If the caret is at a ".", use the Haxe compiler to provide completion hints
         Buffer buffer = view.getBuffer();
         // Save the file if dirty
         if (buffer.isDirty()) {
             buffer.save(view, null, false, true);
             // Wait a bit to allow the save notifications to go through and not
             // bork the reload/popup
             try {
                 Thread.sleep(50);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
 
         HaxeCompilerOutput output = HaXeSideKickPlugin.getHaxeBuildOutput(view.getEditPane(), dotPosition, true);
 
         if (output == null || output.output == null || output.output.errors == null) {
             trace("  haxe build error, no completion candidates");
             return;
         }
 
         String completionXMLString = output.output.errors.trim();
 
         if (completionXMLString == null || completionXMLString.equals("")
             || !completionXMLString.startsWith("<")) {
             return;
         }
 
         List<CompletionCandidate> localCandidates = new ArrayList<CompletionCandidate>();
 
         try {
             // Example see http://www.rgagnon.com/javadetails/java-0573.html
             DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             InputSource is = new InputSource();
             is.setCharacterStream(new StringReader(completionXMLString));
 
             Document doc = db.parse(is);
             NodeList insertions = doc.getElementsByTagName("i");
 
             // iterate the insertions
             for (int i = 0; i < insertions.getLength(); i++) {
                 Element element = (Element)insertions.item(i);
                 if (element.getNodeName().equals("i")) {
                     // Insertion
                     String codeName = element.getAttribute("n");
                     String argString = ((Element)element.getElementsByTagName("t").item(0)).getTextContent();
                     String[] methodTokens = argString.split("->");
                     String returns = methodTokens[methodTokens.length - 1];
                     if (methodTokens.length == 1) {
                         localCandidates.add(new CodeCompletionField(codeName, returns));
                     } else {
                         CodeCompletionMethod cc = new CodeCompletionMethod(codeName, returns);
                         if (methodTokens.length > 1 && !methodTokens[0].trim().equals("Void")) {
                             List<String> args = new ArrayList<String>(methodTokens.length - 1);
                             List<String> argsTypes = new ArrayList<String>(
                                 methodTokens.length - 1);
                             for (int jj = 0; jj < methodTokens.length - 1; ++jj) {
                                 String[] argTokens = methodTokens[jj].split(":");
                                 args.add(argTokens[0]);
                                 if (argTokens.length > 1) {
                                     argsTypes.add(argTokens[1]);
                                 }
                             }
                             cc.arguments = args;
                             cc.argumentTypes = argsTypes;
                         }
                         localCandidates.add(cc);
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
 
 
         Collections.sort(localCandidates);
         candidates.addAll(localCandidates);
     }
 
     protected void getEnumSwitchCompletions (View view, List<CompletionCandidate> candidates)
     {
         String prefix = CompletionUtil.getCompletionPrefix(view);
         prefix = prefix == null ? "" : prefix;
 
         if (!prefix.equals("switch")) {
             return;
         }
 
         //Global enums
         String q = "kind:enum AND " +  TagIndex._PATH_FLD + ":*.hx ";
         Vector<Tag> tags = CtagsInterfacePlugin.runScopedQuery(view, q);
 
         List<CompletionCandidate> localCandidates = new ArrayList<CompletionCandidate>();
 
         for (Tag t : tags) {
             if (t.getName().length() > 1) {
                 localCandidates.add(new EnumSwitchCompletionCandidate(t));
             }
         }
 
         Collections.sort(localCandidates);
         candidates.addAll(localCandidates);
     }
 
     protected void getImportCompletions (View view, List<CompletionCandidate> candidates)
     {
         String prefix = CompletionUtil.getCompletionPrefix(view);
         //Don't bother for dot completion
        if (prefix.endsWith(".")) {
             return;
         }
        prefix = prefix == null ? "" : prefix;
 
        for (String fullPackageName : ImportManager.getFullClassNames(prefix)) {
            candidates.add(new CompletionCandidateFullPackageName(fullPackageName));
         }
     }
 
     private Set<Mode> haxeMode;
 }
