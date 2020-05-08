 package ar.noxit.ehockey.web.pages.partido;
 
 import java.util.List;
 
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import ar.noxit.ehockey.model.Partido;
 import ar.noxit.ehockey.model.Planilla;
 import ar.noxit.ehockey.service.IPartidoService;
 import ar.noxit.ehockey.web.pages.base.AbstractContentPage;
 import ar.noxit.ehockey.web.pages.planilla.ModificarPlanillaPage;
 import ar.noxit.ehockey.web.pages.planilla.PlanillaPage;
 import ar.noxit.exceptions.NoxitException;
 import ar.noxit.web.wicket.model.LDM;
 
 public class PartidoPage extends AbstractContentPage {
 
     private final IChoiceRenderer<Partido> PARTIDORENDERER = new PartidoRenderer();
     @SpringBean
     private IPartidoService partidoService;
 
     public PartidoPage() {
         add(new FeedbackPanel("feedback"));
 
         final IModel<Partido> partido = new PartidoModel(new Model<Integer>(), partidoService);
         Form<Void> formPartidos = new Form<Void>("partidos");

         formPartidos.add(new DropDownChoice<Partido>("partidos", partido, new PartidosListModel(), PARTIDORENDERER)
                 .setRequired(true));

         formPartidos.add(new Button("ver") {
 
             @Override
             public void onSubmit() {
                 setResponsePage(new PlanillaPage(partido));
             }
         });
 
         formPartidos.add(new Button("editar") {
 
             @Override
             public void onSubmit() {
                 setResponsePage(new ModificarPlanillaPage(partido));
             }
         });
 
         add(formPartidos);
     }
 
     private class PartidosListModel extends LDM<List<Partido>> {
 
         @Override
         protected List<Partido> doLoad() throws NoxitException {
             return partidoService.getAll();
         }
     }
 
     private class PartidoRenderer implements IChoiceRenderer<Partido> {
 
         private PartidoRenderer() {
         }
 
         @Override
         public Object getDisplayValue(Partido object) {
            return String.format("%s vs %s (Fecha: %s, Rueda %s)",
                    object.getLocal().getNombre(),
                    object.getVisitante().getNombre(),
                    object.getFechaDelTorneo(),
                    object.getRueda());
         }
 
         @Override
         public String getIdValue(Partido object, int index) {
             return object.getId().toString();
         }
     }
 }
