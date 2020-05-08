 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package coq;
 
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.util.Stack;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JOptionPane;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 import org.netbeans.api.editor.settings.AttributesUtilities;
 import org.netbeans.core.spi.multiview.MultiViewElement;
 import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
 import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
 import org.openide.awt.ActionID;
 import org.openide.awt.ActionReference;
 import org.openide.awt.ActionReferences;
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.MIMEResolver;
 import org.openide.loaders.DataObject;
 import org.openide.loaders.DataObjectExistsException;
 import org.openide.loaders.MultiDataObject;
 import org.openide.loaders.MultiFileLoader;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle.Messages;
 import org.openide.util.RequestProcessor;
 import org.openide.windows.TopComponent;
 
 @Messages({
     "LBL_cq_LOADER=Files of cq"
 })
 @MIMEResolver.ExtensionRegistration(
         displayName = "#LBL_cq_LOADER",
         mimeType = "text/coq",
         extension = {"v"})
 @DataObject.Registration(
         mimeType = "text/coq",
         iconBase = "coq/1372847281_ok_16x16.gif",
         displayName = "#LBL_cq_LOADER",
         position = 300)
 @ActionReferences({
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
             position = 100,
             separatorAfter = 200),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
             position = 300),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
             position = 400,
             separatorAfter = 500),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
             position = 600),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
             position = 700,
             separatorAfter = 800),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
             position = 900,
             separatorAfter = 1000),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
             position = 1100,
             separatorAfter = 1200),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
             position = 1300),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
             position = 1400)
 })
public class cqDataObject extends MultiDataObject implements KeyListener{
 
     /**
      * @return the compiledOffset
      */
     public int getCompiledOffset() {
         return compiledOffset.intValue();
     }
 
     /**
      * @return the uiWindow
      */
     public ProofError getUiWindow() {
         return uiWindow;
     }
 
     /**
      * @param uiWindow the uiWindow to set
      */
     public void setUiWindow(ProofError uiWindow) {
         this.uiWindow = uiWindow;
     }
 
     /**
      * @return the dbugcontents
      */
     public String getDbugcontents() {
         return dbugcontents;
     }
 
     /**
      * @param dbugcontents the dbugcontents to set
      */
     public void setDbugcontents(String dbugcontents) {
         this.dbugcontents = dbugcontents;
     }
 
     /**
      * @return the editor
      */
     public EditorCookie getEditor() {
         if(!initialized)
             initialize();
         return editor;
     }
 
     /**
      * @return the goal
      */
     public nu.xom.Document getGoal() {
         return goal;
     }
 
     /**
      * @param goal the goal to set
      */
     public void setGoal(nu.xom.Document goal) {
         this.goal = goal;
     }
 
     @Override
     public void keyTyped(KeyEvent ke) {
         //JOptionPane.showMessageDialog(null, "NSA you edited!");
               //  highlighter.setHighlight(0, getCompiledOffset());
 
     }
 
     @Override
     public void keyPressed(KeyEvent ke) {
         //keyboard shortcuts?
     }
 
     @Override
     public void keyReleased(KeyEvent ke) {
     }
 
     /**
      * @return the retb
      */
     public OffsetsBag getRetb() {
         return retb;
     }
 
     /**
      * @param retb the retb to set
      */
     public void setRetb(OffsetsBag retb) {
         this.retb = retb;
     }
 
         public void setHighlight(int start, int end)
     {
         retb.clear();
         retb.addHighlight(start, end, compiledCodeAttr);
     }
 
     class BatchCompile implements Runnable{
         private AtomicInteger targetOffset;
         private AtomicInteger pendingSteps;
         private AtomicBoolean stopRequest; // flag to request it to stop
         
         
         public void resetPendingSteps()
         {
           //  pendingSteps=0;
             pendingSteps.set(0);
         }
 
         void decrementPendingSteps(int n)
         {
             pendingSteps.addAndGet(0-n);
         }
         
         public void incrementPendingSteps()
         {
             pendingSteps.incrementAndGet();
         }
                 
         public void requestStopping()
         {
             stopRequest.set(true);
         }
 
         public BatchCompile(int targetOffset) {
             pendingSteps=new AtomicInteger(0);
             this.targetOffset=new AtomicInteger(targetOffset);
             stopRequest=new AtomicBoolean(false);
         }
         
         @Override
         public void run() {
             boolean change;
             if(getCompiledOffset()<targetOffset.intValue())
                 change=handleCompileToTargetPos();
             else
                 change=handleSteps();
             
             getUiWindow().enableCompileButtons();
             if(change)
             {
                 if(uiWindow.isShowGoalChecked())
                 {
                     updateGoal();
                     uiWindow.showGoal();                    
                 }
             }
         }
 
         boolean handleSteps()
         {
             if(pendingSteps.intValue()<0)
                 return handleRewind(0-pendingSteps.intValue());
             
                 boolean change=false;
             while(pendingSteps.intValue()>0)
             {
                 if(compileStep())
                 {
                    pendingSteps.decrementAndGet();
                    change=true;
                 }
                 else
                 {
                     pendingSteps.set(0);
                     break;
                 }
             }
             return change;
         }
         
         boolean handleRewind(int nofSteps)
         {
             
              int rewSteps=(rewindCoqtop(nofSteps));
              resetPendingSteps();
              return rewSteps>0;
               
         }
         
         boolean handleCompileToTargetPos()
         {
             boolean change=false;
             while(getCompiledOffset()<targetOffset.intValue()&&(!stopRequest.get()))
             {
                 if(compileStep())
                 {
                     change=true;
                 }
                 else
                 {
                     break;
                 }
             }
             stopRequest.set(false);
             targetOffset.set(0);
             return change;
         }
         
         public synchronized void setTargetOffset(int targetOffset) {
             this.targetOffset.set(targetOffset);
         }
     }
     
      private RequestProcessor.Task batchCompileTask;
     private CoqTopXMLIO coqtop;
     private String dbugcontents;
     private AtomicInteger compiledOffset;
     private EditorCookie editor;
     private boolean initialized;   
     //private CoqHighlighter highlighter;
     private final RequestProcessor rp;
     private static final Pattern coqCommandEnd=Pattern.compile("(\\.[\\s])");
     private static final Pattern coqComment=Pattern.compile("(\\(\\*)|(\\*\\))");
     private BatchCompile batchCompile;
     private ProofError uiWindow;
     private nu.xom.Document goal;
     private Stack<Integer> offsets;
     private OffsetsBag retb;
     private static final AttributeSet compiledCodeAttr =
             AttributesUtilities.createImmutable(StyleConstants.Background,
             new Color(200, 255, 200));
 
     public cqDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
         super(pf, loader); 
         initialized=false;
         registerEditor("text/coq", true);
         coqtop=new CoqTopXMLIO(pf.getParent());
         compiledOffset=new AtomicInteger(0);
         rp = new RequestProcessor(cqDataObject.class);
         batchCompile=new BatchCompile(0);
         batchCompileTask=rp.create(batchCompile, true);
         offsets=new Stack<Integer>();
         retb=null;
     //    initialize();
     }
 
     void scheduleCompilation()
     {
         batchCompileTask.schedule(10);
     }
 
     void handleCompileOffsetChange()
     {
         setHighlight(0, getCompiledOffset());        
     }
     /**
      * this should only be called after coq compilation/rewind.
      * else it can cause inconsistency between coqtop's and editor's state
      * @param change 
      */
     synchronized void addToCompiledOffset(int change)
     {
         assert(change>0);
         offsets.push(compiledOffset.intValue());
         compiledOffset.addAndGet(change);
         handleCompileOffsetChange();
     }
     
     synchronized void unwindOffsets(int n)
     {
         int newOffset=compiledOffset.intValue();
         for(int i=0;i<n;i++)
         {
             newOffset=offsets.pop();
         }
         compiledOffset.set(newOffset);
         handleCompileOffsetChange();
     }
     
     void jumpToCompileOffest()
     {        
          getEditor().getOpenedPanes()[0].getCaret().setDot(compiledOffset.intValue());
     }    
     
     final void initialize()
     {
         initialized=true;
         assignCookie();
 //        assert(highlighter!=null);
         
      }
     
     private StyledDocument getDocument()
     {
         return getEditor().getDocument();
     }
     synchronized void updateGoal()
     {
         setGoal(coqtop.getGoal());
     }
     
     int getOffsetToSend() {
 
         int offset = 0;
         int endPos = getDocument().getEndPosition().getOffset();
         String code="";
         try {
             code = getDocument().getText(getCompiledOffset(), endPos - getCompiledOffset());
         } catch (BadLocationException ex) {
             Exceptions.printStackTrace(ex);
             assert(false);
         }
         int unmatchedComLeft = 0;
         //int unmatchedStrLift = 0;
         Matcher commandEndMatcher = coqCommandEnd.matcher(code);
         int start=0;
         while (commandEndMatcher.find()) {
             Matcher comments = coqComment.matcher(code.substring(start, commandEndMatcher.end()));
             start=commandEndMatcher.end();
             while (comments.find()) {
                 if (comments.group().equals("*)")) {
                     unmatchedComLeft = unmatchedComLeft - 1;
                 }
 
                 if (comments.group().equals("(*")) {
                     unmatchedComLeft = unmatchedComLeft + 1;
                 }
             }
             if (unmatchedComLeft == 0) {
                 offset = commandEndMatcher.end() - 1;
                 // code[offset]='.'
                 break;
             }
         }
 
 
 
 
         return offset;
 
 
     }
     
     synchronized boolean  compileStep() {
         if(!initialized)
             initialize();
         
         int dotOffset=getOffsetToSend();
         String sendtocoq="";
         try {
             sendtocoq = getDocument().getText(getCompiledOffset(), dotOffset);
         } catch (BadLocationException ex) {
             Exceptions.printStackTrace(ex);
             assert(false);
         }
         
         CoqTopXMLIO.CoqRecMesg rec=coqtop.interpret(sendtocoq);
         
         setDbugcontents("sent: "+sendtocoq+" received "+rec.nuDoc.toXML());
         
         if(rec.success)
         {
                 addToCompiledOffset (dotOffset+1);
         }  
         return rec.success;
     }
 
     void handleDownButton()
     {
         batchCompile.incrementPendingSteps();
         scheduleCompilation();
     }
     
     void handleDownToCursor()
     {
         int curPos=getEditor().getOpenedPanes()[0].getCaretPosition();
         batchCompile.setTargetOffset(curPos);
         scheduleCompilation();
     }
     
     void handleUpButton()
     {
         batchCompile.decrementPendingSteps(1);
         scheduleCompilation();
     }
     void handleUppButton()
     {
         batchCompile.decrementPendingSteps(offsets.size());
         scheduleCompilation();
     }
     /**
      * 
      * @param nofSteps
      * @return the number of steps actually rewound
      *  INCLUDING the ones asked for(and extra steps)
      */
     int rewindCoqtop (int nofSteps)
     {
         CoqTopXMLIO.CoqRecMesg rec=coqtop.rewind(nofSteps);
         if(rec.success)
         {
             int actualSteps= rec.getExtraRewoudSteps()+nofSteps;
             unwindOffsets(actualSteps);
             return actualSteps;
                     
         }
         else
             return 0;
     }
     /**
      * final because it is called in the constructor
      */
     final void assignCookie()
     {
         editor=getLookup().lookup(EditorCookie.class);
         assert(getEditor()!=null);
         assert(getEditor().getDocument()!=null);
         //getEditor().getOpenedPanes()[0].addKeyListener(this);
     }
     
     @Override
     protected int associateLookup() {
         return 1;
     }
 
     void getContents() {
         
             setDbugcontents("successfully started CoqTop version: \n" +coqtop.getVersion());
             
         
     }
     @MultiViewElement.Registration(
             displayName = "#LBL_cq_EDITOR",
             iconBase = "coq/1372847281_ok_16x16.gif",
             mimeType = "text/coq",
             persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
             preferredID = "cq",
             position = 1000)
     @Messages("LBL_cq_EDITOR=Source")
     public static MultiViewEditorElement createEditor(Lookup lkp) {
         return new MultiViewEditorElement(lkp);
     }
 
     /**
      * @param highlighter the highlighter to set
      */
 //    public void setHighlighter(CoqHighlighter highlighter) {
 //        this.highlighter = highlighter;
 //    }
 
 }
