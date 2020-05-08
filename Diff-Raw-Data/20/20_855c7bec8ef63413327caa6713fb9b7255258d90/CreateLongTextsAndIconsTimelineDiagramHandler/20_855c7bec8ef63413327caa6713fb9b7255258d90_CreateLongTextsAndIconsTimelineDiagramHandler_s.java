 package pl.edu.agh.iisg.timeline.integration.handler;
 
 import java.text.ParseException;
 
 import pl.edu.agh.iisg.timeline.editor.TimelineEditorInput;
 import pl.edu.agh.iisg.timeline.integration.DataGenerator;
 
 public class CreateLongTextsAndIconsTimelineDiagramHandler extends CreateSampleTimelineDiagramHandler {
 
 	@Override
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
