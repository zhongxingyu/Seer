 package ar.noxit.ehockey.web.pages.torneo;
 
 import ar.noxit.ehockey.model.Equipo;
 import ar.noxit.ehockey.service.IEquiposService;
 import ar.noxit.ehockey.service.transfer.PartidoInfo;
 import ar.noxit.ehockey.web.pages.models.SelectedEquipoModel;
 import ar.noxit.ehockey.web.pages.models.TodosEquiposModel;
 import ar.noxit.ehockey.web.pages.renderers.EquipoRenderer;
 import ar.noxit.web.wicket.model.Date2LocalDateTimeAdapterModel;
 import java.util.List;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.datetime.PatternDateConverter;
 import org.apache.wicket.datetime.markup.html.form.DateTextField;
 import org.apache.wicket.extensions.yui.calendar.DateTimeField;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.joda.time.LocalDateTime;
 
 public abstract class PartidoFormPanel extends Panel {
 
     @SpringBean
     private IEquiposService equiposService;
     @SuppressWarnings("unused")
     private String botonSubmit;
     private boolean localActivo = true;
     private boolean visitanteActivo = true;
     private boolean numeroFechaActivo = true;
     private boolean fechaActivo = true;
 
     public PartidoFormPanel(String id, final IModel<PartidoInfo> partido) {
         super(id);
 
         final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
         feedbackPanel.setOutputMarkupId(true);
         add(feedbackPanel);
 
         Form<Void> form = new Form<Void>("form");
 
         // choices
         IModel<List<Equipo>> choices = new TodosEquiposModel(equiposService);
         // renderer
         IChoiceRenderer<Equipo> renderer = EquipoRenderer.get();
 
         form.add(new DropDownChoice<Equipo>("local",
                 new SelectedEquipoModel(new PropertyModel<Integer>(partido, "equipoLocalId"), equiposService),
                 choices,
                 renderer) {
 
             @Override
             public boolean isEnabled() {
                 return localActivo;
             }
 
         }.setRequired(true));
 
         form.add(new DropDownChoice<Equipo>("visitante",
                 new SelectedEquipoModel(new PropertyModel<Integer>(partido, "equipoVisitanteId"), equiposService),
                 choices,
                 renderer) {
 
             @Override
             public boolean isEnabled() {
                 return visitanteActivo;
             }
 
         }.setRequired(true));
 
         form.add(new RequiredTextField<Integer>("numero_fecha",
                 new PropertyModel<Integer>(partido, "numeroFecha"), Integer.class) {
 
             @Override
             public boolean isEnabled() {
                 return numeroFechaActivo;
             }
         });
 
         form.add(new DateTimeField("fecha", new Date2LocalDateTimeAdapterModel(new PropertyModel<LocalDateTime>(
                 partido, "fecha"))) {
 
             @SuppressWarnings("unchecked")
             @Override
             protected DateTextField newDateTextField(String id, PropertyModel dateFieldModel) {
                 return new DateTextField(id, dateFieldModel, new PatternDateConverter("dd/MM/yy", false));
             }
 
             @Override
             public boolean isEnabled() {
                 return fechaActivo;
             }
         }.setRequired(true));
 
         form.add(new AjaxButton("submit", new PropertyModel<String>(this, "botonSubmit")) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 PartidoFormPanel.this.onSubmit(target, partido);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.addComponent(feedbackPanel);
             }
         });
 
         add(form);
     }
 
     public PartidoFormPanel setSubmitLabel(String label) {
         this.botonSubmit = label;
         return this;
     }
 
     public PartidoFormPanel setLocalActivo(boolean localActivo) {
         this.localActivo = localActivo;
         return this;
     }
 
     public PartidoFormPanel setVisitanteActivo(boolean visitanteActivo) {
         this.visitanteActivo = visitanteActivo;
         return this;
     }
 
     public PartidoFormPanel setNumeroFechaActivo(boolean numeroFechaActivo) {
         this.numeroFechaActivo = numeroFechaActivo;
         return this;
     }
 
     public PartidoFormPanel setFechaActivo(boolean fechaActivo) {
         this.fechaActivo = fechaActivo;
         return this;
     }
 
     protected abstract void onSubmit(AjaxRequestTarget target, IModel<PartidoInfo> partido);
 }
