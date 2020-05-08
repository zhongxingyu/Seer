 package org.iplantc.de.client.sysmsgs.view;
 
 import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.sencha.gxt.widget.core.client.Composite;
 import com.sencha.gxt.widget.core.client.ListView;
 import com.sencha.gxt.widget.core.client.container.CardLayoutContainer;
import com.sencha.gxt.widget.core.client.container.ResizeContainer;
 
 /**
  * This is the default implementation of the messages view.
  * 
  * @param <M> the type of message to view
  */
 public final class DefaultMessagesView<M> extends Composite implements MessagesView<M> {
 
     interface Binder extends UiBinder<Widget, DefaultMessagesView<?>> {
 	}
 
 	private static final Binder binder = GWT.create(Binder.class);
 	
 	@UiField
     DefaultMessagesViewResources res;
 
     @UiField
     CardLayoutContainer layout;
 
     @UiField
     Widget loadingPanel;
 
     @UiField
     Widget noMessagesPanel;
 
     @UiField
    ResizeContainer messagesPanel;
 
     @UiField
 	HTML messageView;
 	
     @UiField
     Label expiryView;
 
     @UiField(provided = true)
     ListView<M, M> messageList;
 
     /**
      * @see MessagesView#init(ListView)
      */
     @Override
     public void init(final ListView<M, M> summariesView) {
         messageList = summariesView;
         initWidget(binder.createAndBindUi(this));
         res.style().ensureInjected();
     }
 
     /**
      * @see MessagesView#setExpiryMessage(String)
      */
 	@Override
     public void setExpiryMessage(final String expiryMsg) {
         expiryView.setText(expiryMsg);
 	}
 
     /**
      * @see MessagesView#setMessageBody(SafeHtml)
      */
 	@Override
 	public void setMessageBody(final SafeHtml msgBody) {
 		messageView.setHTML(msgBody);
 	}
 
     /**
      * @see MessagesView#showLoading()
      */
     @Override
     public void showLoading() {
         layout.setActiveWidget(loadingPanel);
     }
 
     /**
      * @see MessagesView#showMessages()
      */
     @Override
     public void showMessages() {
         layout.setActiveWidget(messagesPanel);
     }
 
     /**
      * @see MessagesView#showNoMessages()
      */
     @Override
     public void showNoMessages() {
         layout.setActiveWidget(noMessagesPanel);
     }
 
    /*
     * This method is overridden to force the message panel to be laid out a second time in case
     * the expiry message wrapped or unwrapped.
     */
    /**
     * @see Composite#onResize(int, int)
     */
    @Override
    protected void onResize(final int width, final int height) {
        super.onResize(width, height);
        if (layout.getActiveWidget() == messagesPanel) {
            Scheduler.get().scheduleFinally(new ScheduledCommand() {
                @Override
                public void execute() {
                    messagesPanel.forceLayout();
                }
            });
        }
    }

 }

