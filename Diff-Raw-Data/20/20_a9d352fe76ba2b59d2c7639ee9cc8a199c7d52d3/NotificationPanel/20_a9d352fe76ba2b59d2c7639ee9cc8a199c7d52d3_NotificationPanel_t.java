 package de.flower.rmt.ui.page.event.manager.notification;
 
 import com.google.common.annotations.VisibleForTesting;
import de.flower.common.ui.ajax.event.AjaxEventSender;
import de.flower.common.ui.feedback.AlertMessage;
 import de.flower.common.ui.js.JQuery;
 import de.flower.rmt.model.event.Event;
 import de.flower.rmt.model.type.Notification;
 import de.flower.rmt.service.IEventManager;
 import de.flower.rmt.service.mail.INotificationService;
 import de.flower.rmt.ui.app.Links;
 import de.flower.rmt.ui.markup.html.form.EntityForm;
 import de.flower.rmt.ui.markup.html.form.field.*;
 import de.flower.rmt.ui.panel.BasePanel;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.Markup;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.FormComponentPanel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import javax.mail.internet.InternetAddress;
 import java.util.List;
 
 /**
  * @author flowerrrr
  */
 // TODO (flowerrrr - 04.04.12) make this panel undependend from event and template service.
 public class NotificationPanel extends BasePanel {
 
     @SpringBean
     private IEventManager eventManager;
 
     @SpringBean
     private INotificationService notificationService;
 
     public NotificationPanel(String id, final IModel<Event> model) {
         super(id, model);
 
         final IModel<Notification> notificationModel = Model.of(new Notification());
         final EntityForm form = new EntityForm<Notification>("form", notificationModel) {
             @Override
             protected void onSubmit(final AjaxRequestTarget target, final Form<Notification> form) {
                 eventManager.sendInvitationMail(model.getObject().getId(), form.getModelObject());
                 notificationModel.setObject(new Notification());
                 target.appendJavaScript(JQuery.scrollToTop("slow"));
                // fix for RMT-537 - no invitation sent alert not hidden after email is sent
                AjaxEventSender.send(this, AlertMessage.class);
             }
         };
         add(form);
 
         RecipientListFormComponent fc = new RecipientListFormComponent(new PropertyModel(notificationModel, "recipients"), model);
         form.add(new FormFieldPanel("recipientList", fc) {
             @Override
             protected boolean isInstantValidationEnabled() {
                 // disable needless rendering of javascript onchange handler
                 return false;
             }
 
         });
         final TextFieldPanel subject;
         form.add(subject = new TextFieldPanel("subject"));
         final TextAreaPanel body = new BodyTextAreaPanel("body");
         // disabling word wraping in firefox cannot be done by only using css. need wrap attribute on element as well
         body.getFormComponent().add(AttributeModifier.replace("wrap", "off"));
         body.add(new SelectTemplatePanel() {
             @Override
             protected void onUpdate(final AjaxRequestTarget target, final Template template) {
                 Notification notification = newNotification(model.getObject());
                 notificationModel.getObject().setSubject(notification.getSubject());
                 notificationModel.getObject().setBody(notification.getBody());
                 // form.modelChanged(); // does not propagate changes down to components
                 subject.getFormComponent().modelChanged();
                 target.add(subject);
                 body.getFormComponent().modelChanged();
                 target.add(body);
             }
         });
         form.add(body);
         form.add(new CheckBoxPanel("bccMySelf"));
     }
 
     private Notification newNotification(Event event) {
         return notificationService.newEventNotification(event, Links.deepLinkEvent(event.getId()));
     }
 
     /**
      * Class making the wrapped panel act like a form component. Allows to integrate panel
      * into existing form field architecture.
      */
     @VisibleForTesting
     protected static class RecipientListFormComponent extends FormComponentPanel<List<InternetAddress>> {
 
         public RecipientListFormComponent(final IModel<List<InternetAddress>> model, final IModel<Event> eventModel) {
             super("input", model);
             add(new RecipientListPanel(model, eventModel) {
                 @Override
                 protected void onChange(final AjaxRequestTarget target) {
                     // trigger instant validation, simulate AjaxFormComponentUpdatingBehavior
                     processInput();
                     target.add(this.findParent(AbstractFormFieldPanel.class));
                 }
             });
         }
 
         @Override
         protected void convertInput() {
             setConvertedInput(getModelObject());
         }
 
         @Override
         public Markup getAssociatedMarkup() {
             String markup = "<wicket:panel><div wicket:id='recipientListPanel'/></wicket:panel>";
             return Markup.of(markup);
         }
     }
 }
