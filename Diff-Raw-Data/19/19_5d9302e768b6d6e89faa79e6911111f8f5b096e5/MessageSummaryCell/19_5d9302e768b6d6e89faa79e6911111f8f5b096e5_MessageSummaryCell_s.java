 package org.iplantc.de.client.sysmsgs.view;
 
 import java.util.Date;
 
 import org.iplantc.de.client.sysmsgs.model.Message;
 import org.iplantc.de.client.sysmsgs.view.DefaultMessagesViewResources.Style;
 
 import com.google.gwt.cell.client.ValueUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.BrowserEvents;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.safehtml.shared.SafeHtml;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.client.Event;
 import com.sencha.gxt.cell.core.client.AbstractEventCell;
 import com.sencha.gxt.core.client.XTemplates;
 import com.sencha.gxt.widget.core.client.event.XEvent;
 
 /**
  * This is the cell used to render message summaries.
  */
 final class MessageSummaryCell extends AbstractEventCell<Message> {
 	
 	interface Templates extends XTemplates {
         @XTemplate(source = "MessageSummary.html")
         SafeHtml make(Message msg, String activationTimeMsg, Style style);
 	}
 	
 	private static final Style CSS;
     private static final Templates FACTORY;
     private static final long DAYS_PER_WEEK;
     private static final long MS_PER_DAY;
 
     static {
         FACTORY = GWT.create(Templates.class);
         CSS = GWT.<DefaultMessagesViewResources> create(DefaultMessagesViewResources.class).style();
     	CSS.ensureInjected();
         DAYS_PER_WEEK = 7;
         MS_PER_DAY = 86400000;
     }
  
     private final DateTimeFormat activationTimeFormat;
 
     /**
      * the constructor
      */
     MessageSummaryCell() {
         super(BrowserEvents.CLICK);
         activationTimeFormat = DateTimeFormat.getFormat("dd MMMM yyyy");
 	}
 
     /**
      * @see AbstractEventCell<T>#onBrowserEvent(com.google.gwt.cell.client.Cell.Context, Element, T,
      *      NativeEvent, ValueUpdater)
      */
 	@Override
 	public void onBrowserEvent(final Context context, final Element parent, final Message message, 
 			final NativeEvent nativeEvent, final ValueUpdater<Message> updater) {
 		final XEvent event = nativeEvent.<XEvent>cast();
         if (event.getTypeInt() == Event.ONCLICK) {
             if (event.getEventTargetEl().hasClassName(CSS.dismiss())) {
                 fireEvent(new DismissMessageEvent(message));
             }
         }
     }
 	
     /**
      * @see AbstractEventCell<T>#render(com.google.gwt.cell.client.Cell.Context, T, SafeHtmlBuilder)
      */
 	@Override
 	public void render(final Context context, final Message message, 
 			final SafeHtmlBuilder builder) {
 	    final Date actTime = message.getActivationTime();
         final Date now = new Date();
         String actMsg = "";
         if (sameDay(now, actTime)) {
            actMsg = "Today";
         } else if (withinPreviousWeek(now, actTime)) {
             actMsg = DateTimeFormat.getFormat("cccc").format(actTime);
         } else {
             actMsg = activationTimeFormat.format(actTime);
         }
         builder.append(FACTORY.make(message, actMsg, CSS));
     }
 	
     private boolean sameDay(final Date lhs, final Date rhs) {
         return activationTimeFormat.format(lhs).equals(activationTimeFormat.format(rhs));
     }
 
     private boolean withinPreviousWeek(final Date successor, final Date predecessor) {
         if (predecessor.after(successor)) {
             return false;
         }
         final Date succDay = activationTimeFormat.parseStrict(activationTimeFormat.format(successor));
         final Date predDay = activationTimeFormat.parseStrict(activationTimeFormat.format(predecessor));
         if (predDay.getTime() + (DAYS_PER_WEEK - 1) * MS_PER_DAY <= succDay.getTime()) {
             return false;
         }
         return true;
     }
 
 }
