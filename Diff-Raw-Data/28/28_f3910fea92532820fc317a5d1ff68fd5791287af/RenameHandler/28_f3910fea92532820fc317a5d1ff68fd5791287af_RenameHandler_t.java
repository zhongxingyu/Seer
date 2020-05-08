 package dk.brics.lightrefactor.eclipse.handlers;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.ltk.core.refactoring.CompositeChange;
 import org.eclipse.ltk.core.refactoring.DocumentChange;
 import org.eclipse.ltk.core.refactoring.TextChange;
 import org.eclipse.ltk.core.refactoring.TextFileChange;
 import org.eclipse.swt.SWT;
 import org.eclipse.text.edits.MultiTextEdit;
 import org.eclipse.text.edits.ReplaceEdit;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.mozilla.javascript.ast.AstNode;
 
 import dk.brics.jshtml.Html;
 import dk.brics.jshtml.HtmlJs;
 import dk.brics.jshtml.InlineJs;
 import dk.brics.lightrefactor.Asts;
 import dk.brics.lightrefactor.ISource;
 import dk.brics.lightrefactor.Loader;
 import dk.brics.lightrefactor.NameRef;
 import dk.brics.lightrefactor.NodeFinder;
 import dk.brics.lightrefactor.Renaming;
 import dk.brics.lightrefactor.eclipse.FileSource;
 import dk.brics.lightrefactor.eclipse.RefactoringScope;
 import dk.brics.lightrefactor.eclipse.ui.EnterNameDialog;
 import dk.brics.lightrefactor.eclipse.ui.RefactoringUtil;
 
 /**
  * Semi-automatic rename refactoring for JavaScript based on same-type inference.
  */
 public class RenameHandler extends AbstractHandler {
   // TODO: Possible performance issue: AstNode.getAbsolutePosition() and Asts.source() costs are linear in tree depth
   // XXX: Undo label will be "Typing" if only modifying a single file
   // TODO: closed automatically opened editors afterwards?
   
   class JavaScriptResources {
     List<IFile> javascriptFiles = new ArrayList<IFile>();
     List<IFile> htmlFiles = new ArrayList<IFile>();
   }
   
   private boolean isJavaScript(IFile file) {
     return "js".equalsIgnoreCase(file.getFileExtension());
   }
   private boolean isHtml(IFile file) {
     return "html".equalsIgnoreCase(file.getFileExtension()) || "htm".equalsIgnoreCase(file.getFileExtension());
   }
   
   /**
    * Finds all JavaScript resources in the given project, folder, or workspace.
    * A JavaScript resource is a resource with the file extension "<tt>js</tt>" or "<tt>html</tt>" or "<tt>htm</tt>".
    * @param container resource container, such as a project, folder, or workspace
    * @return new list
    */
   private JavaScriptResources getJavaScriptResources(IContainer container) {
     final JavaScriptResources result = new JavaScriptResources();
     try {
       container.accept(new IResourceVisitor() {
         @Override
         public boolean visit(IResource resource) throws CoreException {
           IFile file = (IFile)resource.getAdapter(IFile.class);
           if (file == null)
             return true;
           if (isJavaScript(file)) {
             result.javascriptFiles.add(file);
           }
           else if (isHtml(file)) {
             result.htmlFiles.add(file);
           }
           return true;
         }
       });
     } catch (CoreException e1) {
       throw new RuntimeException(e1);
     }
     return result;
   }
   
   /**
    * Every dirty editor that edits one of the given resources is saved.
    * Must be called in the UI thread.
    * @param window the workbench window whose editors should be saved
    * @param resources only editors that edit one of these resources will be saved
    */
   private void saveOpenResources(IWorkbenchWindow window, Set<? extends IResource> resources) throws InterruptedException {
     final List<IEditorPart> editorsToSave = new ArrayList<IEditorPart>();
     
     for (IWorkbenchPage page : window.getPages()) {
       for (IEditorReference ref : page.getEditorReferences()) {
         IEditorPart editor = ref.getEditor(false);
         if (editor == null)
           continue;
         if (!editor.isDirty())
           continue;
         IResource editorResource = (IResource) editor.getEditorInput().getAdapter(IResource.class);
         if (editorResource == null)
           continue;
         if (!resources.contains(editorResource))
           continue;
         editorsToSave.add(editor);
       }
     }
     
     if (editorsToSave.size() == 0)
       return;
     
     try {
       window.run(false, true, new IRunnableWithProgress() {
         @Override
         public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
           monitor.beginTask("Saving open files", editorsToSave.size());
           for (IEditorPart editor : editorsToSave) {
             monitor.subTask(editor.getTitle());
             editor.doSave(monitor);
             monitor.worked(1);
             if (monitor.isCanceled()) {
               throw new InterruptedException();
             }
           }
           monitor.done();
         }
       });
     } catch (InvocationTargetException e) {
       throw new RuntimeException(e);
     }
   }
   
   /**
    * Map elements in a list to their index.
    */
   private <E> Map<E,Integer> indexMap(List<E> list) {
     int i=0;
     Map<E,Integer> map = new HashMap<E,Integer>();
     for (E x : list) {
       map.put(x, i++);
     }
     return map;
   }
   
 	public Object execute(ExecutionEvent event) throws ExecutionException {
     final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
     ITextEditor currentEditor = (ITextEditor) HandlerUtil.getActiveEditor(event);
     executeWith(window,currentEditor);
     return null;
 	}
 	
 	public void executeWith(final IWorkbenchWindow window, ITextEditor currentEditor) throws ExecutionException {
 	  // Pull some useful objects out of the void
     ITextSelection sel = (ITextSelection) currentEditor.getEditorSite().getSelectionProvider().getSelection();
     IDocument doc = currentEditor.getDocumentProvider().getDocument(currentEditor.getEditorInput());
     IFile currentResource = (IFile) currentEditor.getEditorInput().getAdapter(IFile.class);
     
     // ===================== CLASSIFY REFACTORING REQUEST =================
     // Load the AST
     FileSource currentSource;
     final Loader loader = new Loader();
     if (isJavaScript(currentResource)) {
       currentSource = new FileSource(currentResource);
       loader.addSource(currentSource, doc.get());
     }
     else if (isHtml(currentResource)) {
       currentSource = null;
       List<HtmlJs> frags = Html.extract(doc.get());
       for (HtmlJs frag : frags) {
         if (frag instanceof InlineJs) {
           InlineJs inl = (InlineJs) frag;
           FileSource src = new FileSource(currentResource, inl.getOffset());
           loader.addSource(src, inl.getCode());
           if (sel.getOffset() >= inl.getOffset() && sel.getOffset() <= inl.getOffset() + inl.getCode().length()) {
             currentSource = src;
           }
         }
       }
       if (currentSource == null) {
         return; // not inside JavaScript fragment
       }
     }
     else {
       return; // does not apply to this file type
     }
     final Asts asts = loader.getAsts();
     
     // Find the selected name token
     AstNode targetName = NodeFinder.find(asts.get(currentSource), sel.getOffset() - currentSource.getOffset());
     String originalName = NameRef.name(targetName);
     
     boolean showScope;
     String thingType;
     if (NameRef.isPrty(targetName)) {
       thingType = "property";
       showScope = true;
     } else if (NameRef.isGlobalVar(targetName)) {
       thingType = "variable";
       showScope = true;
     } else if (NameRef.isVar(targetName)) {
       thingType = "variable";
       showScope = false;
     } else if (NameRef.isLabel(targetName)) {
       thingType = "label";
       showScope = false;
     } else {
       return; // cannot rename this type of thing
     }
 
     
     // ===================== INITIAL USER PROMPT =================
     
     // Prompt the new name
     currentEditor.selectAndReveal(currentSource.getOffset() + NameRef.startPos(targetName), originalName.length());
     EnterNameDialog newNameDialog = new EnterNameDialog(window.getShell(), thingType, originalName);
     newNameDialog.setShowScope(showScope);
     newNameDialog.open();
     if (newNameDialog.getReturnCode() != Window.OK) {
       return; // we got cancelled
     }
     String newName = newNameDialog.getNewName();
     RefactoringScope scope = showScope ? newNameDialog.getSelectedScope() :  RefactoringScope.File;
 
     
     // ===================== FIND/SAVE AFFECTED RESOURCES =================
     
     // Find affected resources and save them if they are open in a dirty editor
     final List<IFile> affectedResources = new ArrayList<IFile>();
     affectedResources.add(currentResource);
     switch (scope) {
       case File:
         break;
       case Project:
       case Workspace:
         IContainer container = scope == RefactoringScope.Project ? currentResource.getProject() : currentResource.getWorkspace().getRoot();
         JavaScriptResources resources = getJavaScriptResources(container);
         resources.javascriptFiles.remove(currentResource);
         resources.htmlFiles.remove(currentResource);
         affectedResources.addAll(resources.javascriptFiles);
         affectedResources.addAll(resources.htmlFiles);
         try {
           saveOpenResources(window, new HashSet<IFile>(affectedResources));
         } catch (InterruptedException e1) {
           return; // operation was cancelled
         }
         break;
     }
     
     // Load all ASTs
     for (IFile file : affectedResources) {
       if (file.equals(currentResource))
         continue;
 
       try {
         InputStream stream = file.getContents();
         try {
           Reader reader = new InputStreamReader(stream, file.getCharset());
           if (isJavaScript(file)) {
             loader.addSource(new FileSource(file), reader);
           } else if (isHtml(file)) {
             List<HtmlJs> frags = Html.extract(reader);
             for (HtmlJs frag : frags) {
               if (frag instanceof InlineJs) {
                 InlineJs inl = (InlineJs)frag;
                 loader.addSource(new FileSource(file, inl.getOffset()), inl.getCode());
               }
             }
           } else {
             // ??
           }
         } finally {
           stream.close();
         }
       } catch (CoreException e) {
         throw new ExecutionException("Could not load file: " + file, e);
       } catch (IOException e) {
         throw new ExecutionException("Could not load file: " + file, e);
       }
     }
 
     // ===================== COMPUTE RENAMING INFO =====================
     
    Renaming renaming = new Renaming(asts);
    List<ArrayList<AstNode>> questions = renaming.renameNode(targetName);
     
     
     // ===================== ORDERING OF QUESTIONS =====================
     
     // Order questions such that all questions are visited in a forward manner (no jumping back and forth).
     // Also ensure that the current file is visited first (note: it is always the first entry in affectedResources)
     final Map<IFile,Integer> file2index = indexMap(affectedResources);
     final Comparator<ISource> sourceOrd = new Comparator<ISource>() {
       @Override
       public int compare(ISource o1, ISource o2) {
         FileSource fs1 = (FileSource)o1;
         FileSource fs2 = (FileSource)o2;
         int deltaIndex = file2index.get(fs1.getFileResource()) - file2index.get(fs2.getFileResource());
         if (deltaIndex != 0)
           return deltaIndex;
         // if same file, compare by offset in that file
         return fs1.getOffset() - fs2.getOffset();
       }
     };
     final Comparator<AstNode> tokenOrd = new Comparator<AstNode>() {
       @Override
       public int compare(AstNode o1, AstNode o2) {
         int src = sourceOrd.compare(asts.source(o1), asts.source(o2));
         if (src != 0)
           return src;
         return o1.getAbsolutePosition() - o2.getAbsolutePosition();
       }
     };
     
     // Order questions within each group
    for (List<AstNode> list : questions) {
       Collections.sort(list, tokenOrd);
     }
     
     // Order questions by their first token
    Collections.sort(questions, new Comparator<ArrayList<AstNode>>() {
       @Override
       public int compare(ArrayList<AstNode> o1, ArrayList<AstNode> o2) {
         return tokenOrd.compare(o1.get(0), o2.get(0));
       }
     });
 
     
     // ===================== ASK EVERY QUESTION =====================
     
     // Prompt user for each group of tokens
     List<AstNode> autoTokens = new ArrayList<AstNode>(); // automatically selected tokens
     
     // Automatically answer the question concerning the selected token
     // (Handling this here makes it easier to implement the back button in the question loop)
    for (int i=0; i<questions.size(); i++) {
      if (questions.get(i).contains(targetName)) {
        autoTokens.addAll(questions.get(i));
        questions.remove(i);
         break;
       }
     }
     
     // Ask the questions
     int questionIndex = 0;
     List<Integer> yesQuestions = new ArrayList<Integer>();
    while (questionIndex < questions.size()) {
      List<AstNode> names = questions.get(questionIndex);
       // select the first token in the editor
       AstNode name = names.get(0);
       FileSource src = (FileSource) asts.source(name);
       IFile file = src.getFileResource();
       ITextEditor editor;
       try {
         // TODO: what if result is not an ITextEditor?
         editor = (ITextEditor) IDE.openEditor(currentEditor.getEditorSite().getPage(), file);
       } catch (PartInitException e) {
         throw new ExecutionException("Could not initialize editor", e);
       }
       
       // reveal token in the editor
       editor.selectAndReveal(src.getOffset() + NameRef.startPos(name), originalName.length());
       
       // if more than one token is in this group, let the user know somehow
       String msgSuffix = "";
       if (names.size() == 2) {
         msgSuffix = " (and 1 other)";
       } else if (names.size() > 2) {
         msgSuffix = " (and " + (names.size()-1) + " others)";
       }
       
       // prompt the user: should this token be renamed?
       int BACK; // index of the BACK button in the string array below
       int YES; // index of the YES button in the string array below
       String[] btns;
       if (questionIndex > 0) {
         BACK = 0;
         YES = 2;
         btns = new String[] {"< Back", "No", "Yes" };
       } else {
         BACK = 3;
         YES = 1;
         btns = new String[] {"No", "Yes" };
       }
       MessageDialog msg = new MessageDialog(
           window.getShell(), 
           "Rename " + thingType, 
           null,                             // no title image
           "Rename this token?" + msgSuffix, 
           MessageDialog.NONE,               // no image type 
           btns, 
           YES);
       int dialogReturnCode = msg.open(); // can be DEFAULT, BACK, YES, or NO
       
       // if the dialog was called (e.g. user pressed ESC) then cancel the entire refactoring 
       if (dialogReturnCode == SWT.DEFAULT)
         return;
       
       if (dialogReturnCode == BACK) {
         // undo last answer if it was yes
         if (yesQuestions.size() > 0 && yesQuestions.get(yesQuestions.size()-1) == questionIndex-1) {
           yesQuestions.remove(yesQuestions.size()-1);
         }
         questionIndex--; // back to previous question
         continue;
       }
       
       if (dialogReturnCode == YES) {
         yesQuestions.add(questionIndex);
       }
       questionIndex++; // advance to next question
     }
 
     Set<IFile> modifiedFiles = new HashSet<IFile>();
     List<AstNode> tokensToRename = new ArrayList<AstNode>();
     tokensToRename.addAll(autoTokens);
     modifiedFiles.add(currentResource);
     for (int q : yesQuestions) {
      tokensToRename.addAll(questions.get(q));
      for (AstNode tok : questions.get(q)) {
         FileSource rsrc = (FileSource)asts.source(tok);
         modifiedFiles.add(rsrc.getFileResource());
       }
     }
     
     // ===================== CLEAN UP UI =====================
     
     // jump back to where the refactoring started
     // hack: we do this by selecting the original token again
     // do this before performing the changes, so the offset is still correct
     currentEditor.getEditorSite().getPage().activate(currentEditor);
     currentEditor.selectAndReveal(currentSource.getOffset() + NameRef.startPos(targetName), originalName.length());
     
     
     // ===================== PERFORM CHANGES =====================
 
     // Prepare the change set
     // In the following be aware of the following differences between TextFileChange and DocumentChange:
     // TextFileChange: changes are saved automatically, open editors will reload
     // DocumentChange: changes are not automatically saved, open editors will be marked dirty
     CompositeChange changeSet = new CompositeChange("Rename " + originalName + " to " + newName);
     Map<IFile,TextChange> file2changes = new HashMap<IFile,TextChange>();
     if (modifiedFiles.size() == 1) {
       TextChange change = new DocumentChange(changeSet.getName(), doc);
       change.setEdit(new MultiTextEdit());
       file2changes.put(currentResource, change);
       changeSet.add(change);
     }
     Collections.sort(tokensToRename);
     for (AstNode name : tokensToRename) {
       FileSource src = (FileSource) asts.source(name);
       IFile file = src.getFileResource();
       TextChange change = file2changes.get(file);
       if (change == null) {
         change = new TextFileChange(changeSet.getName(), file);
         change.setEdit(new MultiTextEdit());
         file2changes.put(file, change);
         changeSet.add(change);
       }
       change.addEdit(new ReplaceEdit(src.getOffset() + NameRef.startPos(name), originalName.length(), newName));
     }
     
     // Execute the change
     if (newNameDialog.isPreviewSelected()) {
       RefactoringUtil.previewAndPerform(changeSet, changeSet.getName(), window.getShell(), changeSet.getName());
     } else {
       RefactoringUtil.perform(changeSet, window, modifiedFiles.size() > 1);
     }
 	}
 
 }
