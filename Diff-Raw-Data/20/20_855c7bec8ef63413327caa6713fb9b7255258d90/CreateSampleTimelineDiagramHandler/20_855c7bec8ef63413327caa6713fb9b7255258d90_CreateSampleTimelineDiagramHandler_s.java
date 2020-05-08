 package pl.edu.agh.iisg.timeline.integration.handler;
 
import java.text.ParseException;

 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import pl.edu.agh.iisg.timeline.editor.TimelineEditor;
 import pl.edu.agh.iisg.timeline.editor.TimelineEditorInput;
 import pl.edu.agh.iisg.timeline.integration.DataGenerator;
 
 public class CreateSampleTimelineDiagramHandler extends AbstractHandler {
 
     private static final String AXES_CNT = "pl.edu.agh.iisg.timeline.create.sample.axesCnt"; //$NON-NLS-1$
 
     private static final String ELEMENTS_CNT = "pl.edu.agh.iisg.timeline.create.sample.elementsCnt"; //$NON-NLS-1$
 
     @Override
     public Object execute(ExecutionEvent event) throws ExecutionException {
         IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
         IWorkbenchPage page = window.getActivePage();
 
         int axesCnt = Integer.parseInt(event.getParameter(AXES_CNT));
         long elementsCont = Long.parseLong(event.getParameter(ELEMENTS_CNT));
 
         try {
             page.openEditor(createTimelineEditorInput(axesCnt, elementsCont), TimelineEditor.ID);
         } catch (PartInitException e) {
             e.printStackTrace();
         }
         return null;
 
     }
 
     protected TimelineEditorInput createTimelineEditorInput(int axesCnt, long elementsCount) {
         TimelineEditorInput editorInput = new TimelineEditorInput();
        try {
            editorInput.setTimelineDiagram(DataGenerator.createRealDataDiagram(axesCnt, elementsCount, false));
        } catch (ParseException e) {
            e.printStackTrace();
        }
         return editorInput;
     }
 }
