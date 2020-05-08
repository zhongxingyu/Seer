 package org.mindinformatics.gwt.domeo.client.ui.annotation.actions;
 
 import org.mindinformatics.gwt.domeo.client.IDomeo;
 import org.mindinformatics.gwt.domeo.client.ui.content.AnnotationFrameWrapper;
 import org.mindinformatics.gwt.domeo.model.MAnnotation;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Window;
 
 public class ActionDeleteAnnotation {
 
 	public static final ClickHandler getClickHandler(final IDomeo domeo, final Object clazz, final MAnnotation annotation) {
 		ClickHandler ch = new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				boolean deleteFlag = Window.confirm("Do you really want to delete the annotation?");
 				if(deleteFlag) {
 					domeo.getProgressPanelContainer().setProgressMessage("Deleting annotation item...");
 					domeo.getLogger().command(AnnotationFrameWrapper.LOG_CATEGORY_DELETE_ANNOTATION, clazz, "Item " + annotation.getClass().getName() + "-"+annotation.getLocalId());
 					// TODO manage deletion and undo???
 					domeo.getContentPanel().getAnnotationFrameWrapper().removeAnnotation(annotation, false);
					domeo.getProgressPanelContainer().setCompletionMessage("Annotation deleted!");
 				}
 			}
 		};
 		return ch;
 	}
 }
