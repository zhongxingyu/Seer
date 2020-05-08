 package org.iplantc.de.client.sysmsgs.view;
 
 import org.iplantc.de.client.sysmsgs.view.DefaultMessagesViewResources.Style;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.MouseDownEvent;
import com.sencha.gxt.core.client.Style.SelectionMode;
 import com.sencha.gxt.widget.core.client.ListViewSelectionModel;
 import com.sencha.gxt.widget.core.client.event.XEvent;
 
 /**
  * The summary selection model. This model prevents a summary from being selected when the user
  * clicks on its dismiss button.
 * 
 * @param the type of message to select
  */
final class SelectionModel<M> extends ListViewSelectionModel<M> {
 
 	private static final Style CSS;
 
     static {
         CSS = GWT.<DefaultMessagesViewResources> create(DefaultMessagesViewResources.class).style();
     	CSS.ensureInjected();
     }
  
     /**
     * the constructor
     * 
     * @param selectionMode the selection mode
     */
    SelectionModel(final SelectionMode selectionMode) {
        setSelectionMode(selectionMode);
    }

    /**
      * @see ListViewSelectionModel<T>#handleMouseDown(MouseDownEvent)
      */
 	@Override
 	protected void handleMouseDown(final MouseDownEvent mouseEvent) {
 	    final XEvent event = mouseEvent.getNativeEvent().<XEvent> cast();
 	    if (!event.getEventTargetEl().hasClassName(CSS.dismiss())) {
 	    	super.handleMouseDown(mouseEvent);
 	    }
 	}
 	
 }
 
