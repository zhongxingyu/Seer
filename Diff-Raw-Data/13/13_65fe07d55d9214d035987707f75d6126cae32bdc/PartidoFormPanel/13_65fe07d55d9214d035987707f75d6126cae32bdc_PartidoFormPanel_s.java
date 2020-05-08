 package ar.noxit.ehockey.web.pages.torneo;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.datetime.PatternDateConverter;
 import org.apache.wicket.datetime.markup.html.form.DateTextField;
 import org.apache.wicket.extensions.yui.calendar.DateTimeField;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.joda.time.LocalDateTime;
 
import ar.noxit.ehockey.service.IEquipoService;
import ar.noxit.ehockey.service.transfer.PartidoInfo;
import ar.noxit.ehockey.web.pages.models.EquipoModel;
import ar.noxit.web.wicket.model.Date2LocalDateTimeAdapterModel;

 public abstract class PartidoFormPanel extends Panel {
 
     @SpringBean
    private IEquipoService equiposService;
     @SuppressWarnings("unused")
     private String botonSubmit;
     private boolean localActivo = true;
     private boolean visitanteActivo = true;
     private boolean numeroFechaActivo = true;
     private boolean fechaActivo = true;
     private boolean ruedaActivo = true;
 
     public PartidoFormPanel(String id, final IModel<PartidoInfo> partido) {
         super(id);
 
         final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
         feedbackPanel.setOutputMarkupId(true);
         add(feedbackPanel);
 
         Form<Void> form = new Form<Void>("form");
 
         form.add(new Label("local", new EquipoModel(new PropertyModel<Integer>(partido, "equipoLocalId"), equiposService)).setRenderBodyOnly(true));
         form.add(new Label("visitante", new EquipoModel(new PropertyModel<Integer>(partido, "equipoVisitanteId"), equiposService)).setRenderBodyOnly(true));
 
         form.add(new RequiredTextField<Integer>("numero_fecha",
                 new PropertyModel<Integer>(partido, "numeroFecha"), Integer.class) {
 
             @Override
             public boolean isEnabled() {
                 return numeroFechaActivo;
             }
         });
 
         form.add(new RequiredTextField<Integer>("rueda",
                 new PropertyModel<Integer>(partido, "rueda"), Integer.class) {
 
             @Override
             public boolean isEnabled() {
                 return ruedaActivo;
             }
         });
 
         form.add(new DateTimeField("fecha", new Date2LocalDateTimeAdapterModel(new PropertyModel<LocalDateTime>(
                 partido, "fecha"))) {
 
             @SuppressWarnings("unchecked")
             @Override
             protected DateTextField newDateTextField(String id, PropertyModel dateFieldModel) {
                 return new DateTextField(id, dateFieldModel, new PatternDateConverter("dd/MM/yyyy", false));
             }
 
             @Override
             public boolean isEnabled() {
                 return fechaActivo;
             }
         }.setRequired(true));
 
         form.add(new AjaxButton("submit", new PropertyModel<String>(this, "botonSubmit")) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 target.addComponent(feedbackPanel);
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
 
     public PartidoFormPanel setRuedaActivo(boolean ruedaActivo) {
         this.ruedaActivo = ruedaActivo;
         return this;
     }
 
     protected abstract void onSubmit(AjaxRequestTarget target, IModel<PartidoInfo> partido);
 }
