 package pl.psnc.dl.wf4ever.portal.components.form;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.attributes.AjaxCallListener;
 import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.event.Broadcast;
 import org.apache.wicket.event.IEventSink;
 import org.apache.wicket.markup.html.form.Form;
 
 import pl.psnc.dl.wf4ever.portal.events.AbstractAjaxEvent;
 import pl.psnc.dl.wf4ever.portal.events.AbstractClickAjaxEvent;
 import pl.psnc.dl.wf4ever.portal.events.ErrorEvent;
 
 /**
  * A button the creates an AJAX Event when clicked.
  * 
  * @author piotrekhol
  * 
  */
 public class AjaxEventButton extends AjaxButton {
 
     /** id. */
     private static final long serialVersionUID = -2527416440222820413L;
 
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(AjaxEventButton.class);
 
     /** the root of the DOM subtree that will be notified. */
     private IEventSink component;
 
     /** the class of the event to post. */
     protected Class<? extends AbstractClickAjaxEvent> eventClass;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket ID
      * @param form
      *            for which will be validated
      * @param component
      *            the root of the DOM subtree that will be notified
      * @param eventClass
      *            the class of the event to post
      */
     public AjaxEventButton(String id, Form<?> form, IEventSink component,
             Class<? extends AbstractClickAjaxEvent> eventClass) {
         super(id, form);
         this.component = component;
         this.eventClass = eventClass;
     }
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket ID
      * @param component
      *            the root of the DOM subtree that will be notified
      * @param eventClass
      *            the class of the event to post
      */
     public AjaxEventButton(String id, IEventSink component, Class<? extends AbstractClickAjaxEvent> eventClass) {
         this(id, null, component, eventClass);
     }
 
 
     @Override
     protected final void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
         target.appendJavaScript("hideBusy()");
         AbstractAjaxEvent event = newEvent(target);
         if (component == null) {
             component = getPage();
         }
         if (event != null) {
             send(component, Broadcast.BREADTH, event);
         }
     }
 
 
     /**
      * Create a new event.
      * 
      * @param target
      *            AJAX request target
      * @return an event or null
      */
     protected AbstractAjaxEvent newEvent(AjaxRequestTarget target) {
         try {
             return (AbstractAjaxEvent) eventClass.getConstructor(AjaxRequestTarget.class).newInstance(target);
         } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException e) {
             LOG.error("Can't create the default event", e);
             return null;
         }
     }
 
 
     @Override
     protected void onError(AjaxRequestTarget target, Form<?> form) {
         target.appendJavaScript("hideBusy()");
         LOG.error("Error when submitting the button");
         if (component == null) {
             component = getPage();
         }
        send(component, Broadcast.BREADTH, new ErrorEvent(target));
     }
 
 
     @Override
     protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
         super.updateAjaxAttributes(attributes);
 
         AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
 
             /** id. */
             private static final long serialVersionUID = -5008615244332637745L;
 
 
             @Override
             public CharSequence getBeforeHandler(Component component) {
                 return "showBusy();";
             }
         };
         attributes.getAjaxCallListeners().add(myAjaxCallListener);
     }
 
 }
