 package snomed.visualization.vaadin.ui;
 
 import java.util.Iterator;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.xtext.nodemodel.INode;
 
 import snomed.visualization.vaadin.VisualizationDsl;
 import snomed.visualization.vaadin.VisualizationDslModifyEvent;
 import snomed.visualization.vaadin.listener.IVisualizationDslModifyListener;
 import snomed.visualization.vaadin.util.VisualizationDslUtil;
 
 import com.vaadin.server.Page;
 import com.vaadin.shared.Position;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Notification.Type;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * View to show the dsl representation of the expression. 
  * 
  * @author rporcio
  */
 public class VisualizationDslView extends VerticalLayout implements IVisualizationDslModifyListener {
 
 	private static final long serialVersionUID = -2580308559244447759L;
 	private VisualizationDsl visualizationDsl;
 	private VisualizationDslUtil dslUtil;
	private Notification errorNotification;
 	private VisualizationView visualizationView;
 	private boolean containsErrors;
 	
 	public VisualizationDslView(VisualizationView visualizationView) {
 		this.visualizationView = visualizationView;
 		setSizeFull();
 		visualizationDsl = new VisualizationDsl();
 		
 		dslUtil = new VisualizationDslUtil();
 		
 		visualizationDsl.setSizeFull();
 		visualizationDsl.addModifyListener(this);
 		visualizationDsl.setImmediate(false);
 
 		addComponent(visualizationDsl);
 		
		errorNotification = new Notification("Errors in the expression", "", Type.WARNING_MESSAGE);
		errorNotification.setPosition(Position.BOTTOM_RIGHT);
 	}
 	
 	/**
 	 * Visualize the grammar in text format.
 	 */
 	public void visualizeGrammar() {
 		visualizationDsl.setState(dslUtil.addHtmlFormatters(dslUtil.convertToPresentation(visualizationView.getExpression())));
 	}
 
 	/**
 	 * Tells if the grammar contains errors or not.
 	 * 
 	 * @return <code>true</code> if there are errors in the grammar.
 	 */
 	public boolean isContainsErrors() {
 		return containsErrors;
 	}
 
 	@Override
 	public void handleModify(VisualizationDslModifyEvent event) {
 		// TODO add schedule because of gwt-richtextarea bug(?)
 		
 		String dsl = dslUtil.removeHtmlFormatters(event.getDsl());
 		
 		if (dslUtil.isValid(dsl)) {
 			containsErrors = false;
 
 			visualizationView.updateDiagram(dslUtil.updatePreviousExpression(visualizationView.getExpression(), dsl));
 			visualizationDsl.setState(dslUtil.addHtmlFormatters(dsl));
 		} else {
 			containsErrors = true;
 			
 			String errorMarkedDsl = "";
 			final String errorMarkerOpen = "<font color=\"red\"><u>";
 			final String errorMarkerClose = "</u></font>";
 			final StringBuilder builder = new StringBuilder();
 			
 			int start = 0;
 			Iterator<INode> syntaxErrors = dslUtil.getSyntaxErrors(dsl).iterator();
 			
 			while (syntaxErrors.hasNext()) {
 				INode syntaxError = syntaxErrors.next();
 				int startOffset = syntaxError.getTotalOffset();
 				int endOffset = syntaxError.getTotalEndOffset();
 				
 				errorMarkedDsl = errorMarkedDsl.concat(dsl.substring(start, startOffset));
 				String errorPart = dsl.substring(startOffset, endOffset);
 				errorMarkedDsl = errorMarkedDsl.concat(errorMarkerOpen).concat(errorPart).concat(errorMarkerClose);
 				
 				start = endOffset;
 				
 				builder.append("\n").append(StringUtils.capitalize(syntaxError.getSyntaxErrorMessage().getMessage())).append(".");
 				
 				if (!syntaxErrors.hasNext()) {
 					errorMarkedDsl = errorMarkedDsl.concat(dsl.substring(endOffset));
 				}
 			}
 			
 			visualizationDsl.getState().setText(dslUtil.addHtmlFormatters(errorMarkedDsl));
 			
			errorNotification.setDescription(builder.toString());
			errorNotification.show(Page.getCurrent());
			errorNotification.setStyleName("error-notification-style");
 		}
 	}
 
 }
