 package ar.noxit.ehockey.web.pages.torneo;
 
 import ar.noxit.ehockey.model.Partido;
 import ar.noxit.ehockey.service.IDateTimeProvider;
 import ar.noxit.ehockey.web.pages.authentication.IRenderable;
 import org.apache.wicket.Page;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.joda.time.LocalDateTime;
 
 public abstract class PlanillaBaseLink extends Link<Partido> implements IRenderable {
 
     @SpringBean
     private IDateTimeProvider dateTimeProvider;
 
     public PlanillaBaseLink(String id, IModel<Partido> model) {
         super(id, model);
     }
 
     @Override
     public final boolean isEnabled() {
         LocalDateTime localDateTime = dateTimeProvider.getLocalDateTime();
         Partido partido = getModelObject();
         return isEnabled(partido, localDateTime);
     }
 
     @Override
     public final void onClick() {
         setResponsePage(createNewPage(getModel()));
     }
 
     protected abstract boolean isEnabled(Partido partido, LocalDateTime localDateTime);
 
     protected abstract Page createNewPage(IModel<Partido> model);
 }
