 package ar.noxit.ehockey.web.pages.torneo;
 
 import ar.noxit.ehockey.model.Jugador;
 import ar.noxit.ehockey.service.IJugadorService;
 import ar.noxit.ehockey.web.pages.base.AbstractColorBasePage;
 import ar.noxit.ehockey.web.pages.models.IdJugadorModel;
 import ar.noxit.ehockey.web.pages.planilla.AmonestacionInfo;
 import ar.noxit.ehockey.web.pages.renderers.JugadorRenderer;
 import java.util.List;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 public abstract class NuevaAmonestacionPage extends AbstractColorBasePage {
 
     private AmonestacionInfo amonestacionInfo = new AmonestacionInfo(null, 0, 0, 0);
     @SpringBean
     private IJugadorService jugadorService;
 
     public NuevaAmonestacionPage(IModel<List<Jugador>> jugadores, final ModalWindow modal) {
         Form<Void> form = new Form<Void>("form");
         add(form);
 
         final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
         feedbackPanel.setOutputMarkupId(true);
         form.add(feedbackPanel);
 
         form.add(new DropDownChoice<Jugador>("jugadores",
                 new IdJugadorModel(new PropertyModel<Integer>(amonestacionInfo, "jugadorId"), jugadorService),
                 jugadores,
                 JugadorRenderer.get())
                 .setRequired(true));
 
         form.add(new RequiredTextField<Integer>("rojas",
                new PropertyModel<Integer>(amonestacionInfo, "rojas"), Integer.class));
         form.add(new RequiredTextField<Integer>("amarillas",
                new PropertyModel<Integer>(amonestacionInfo, "amarillas"), Integer.class));
         form.add(new RequiredTextField<Integer>("verdes",
                new PropertyModel<Integer>(amonestacionInfo, "verdes"), Integer.class));
 
         form.add(new AjaxButton("submit") {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 target.addComponent(feedbackPanel);
                 NuevaAmonestacionPage.this.onSubmit(amonestacionInfo);
                 modal.close(target);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.addComponent(feedbackPanel);
             }
         });
     }
 
     protected abstract void onSubmit(AmonestacionInfo amonestacionInfo);
 }
