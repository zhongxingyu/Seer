 package pl.psnc.dl.wf4ever.portal.modals;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.markup.head.IHeaderResponse;
 import org.apache.wicket.markup.head.JavaScriptHeaderItem;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 
 import pl.psnc.dl.wf4ever.portal.components.EventPanel;
 import pl.psnc.dl.wf4ever.portal.components.feedback.MyFeedbackPanel;
 import pl.psnc.dl.wf4ever.portal.components.form.AjaxEventButton;
 import pl.psnc.dl.wf4ever.portal.events.CancelClickedEvent;
 import pl.psnc.dl.wf4ever.portal.events.OkClickedEvent;
 
 import com.google.common.eventbus.EventBus;
 
 /**
  * A modal for adding resources to the RO.
  * 
  * @author piotrekhol
  * 
  */
 public abstract class AbstractModal extends EventPanel {
 
     /** id. */
     private static final long serialVersionUID = -278196023603190873L;
 
     /** Feedback panel. */
     protected MyFeedbackPanel feedbackPanel;
 
     /** Form for all input fields and buttons. */
     protected Form<?> form;
 
     /** Internal event bus for button clicks. */
     protected IModel<EventBus> internalEventBusModel;
 
     /** Modal id in HTML for JavaScript hiding and showing. */
     private String modalId;
 
    /** The div with a different ID for each modal. */
     protected WebMarkupContainer modal;
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param eventBusModel
      *            event bus
      * @param modalId
      *            modal HTML id, without any '#'
      * @param title
      *            modal title
      */
     public AbstractModal(String id, IModel<EventBus> eventBusModel, String modalId, String title) {
         this(id, null, eventBusModel, modalId, title);
     }
 
 
     /**
      * Constructor.
      * 
      * @param id
      *            wicket id
      * @param model
      *            panel model, may be null
      * @param eventBusModel
      *            event bus
      * @param modalId
      *            modal HTML id, without any '#'
      * @param title
      *            modal title
      */
     public AbstractModal(String id, IModel<?> model, final IModel<EventBus> eventBusModel, String modalId, String title) {
         super(id, model, eventBusModel);
         setOutputMarkupId(true);
         this.modalId = modalId;
         form = new Form<Void>("form");
         add(form);
 
         modal = new WebMarkupContainer("modal");
         modal.add(AttributeAppender.replace("id", modalId));
         form.add(modal);
 
         feedbackPanel = new MyFeedbackPanel("feedbackPanel");
         feedbackPanel.setOutputMarkupId(true);
         modal.add(feedbackPanel);
 
         modal.add(new Label("title", title));
 
         internalEventBusModel = new LoadableDetachableModel<EventBus>() {
 
             /** id. */
             private static final long serialVersionUID = 5225667860067218852L;
 
 
             @Override
             protected EventBus load() {
                 return new EventBus();
             }
         };
         internalEventBusModel.getObject().register(this);
 
         AjaxEventButton ok = new AjaxEventButton("ok", form, internalEventBusModel, OkClickedEvent.class);
         form.setDefaultButton(ok);
         modal.add(ok);
         modal.add(new AjaxEventButton("cancel", form, internalEventBusModel, CancelClickedEvent.class)
                 .setDefaultFormProcessing(false));
         modal.add(new AjaxEventButton("close", form, internalEventBusModel, CancelClickedEvent.class)
                 .setDefaultFormProcessing(false));
     }
 
 
     @Override
     public void renderHead(IHeaderResponse response) {
         super.renderHead(response);
         response.render(JavaScriptHeaderItem.forScript("$(document).ready(function() {\n" + "    $('#" + modalId
                 + "').modal({\n" + "        backdrop : 'static',\n" + "        show : false\n" + "    });\n" + "});\n"
                 + "", "init-modal"));
     }
 
 
     /**
      * Show itself.
      * 
      * @param target
      *            AJAX target
      */
     public void show(AjaxRequestTarget target) {
         target.appendJavaScript("$('#" + modalId + "').modal('show')");
     }
 
 
     /**
      * Hide.
      * 
      * @param target
      *            AJAX target
      */
     public void hide(AjaxRequestTarget target) {
         target.prependJavaScript("$('#" + modalId + "').modal('hide')");
         target.add(feedbackPanel);
     }
 
 }
