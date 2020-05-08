 package ar.noxit.ehockey.web.pages.equipos;
 
 import ar.noxit.ehockey.model.Club;
 import ar.noxit.ehockey.model.Division;
 import ar.noxit.ehockey.model.Sector;
 import ar.noxit.ehockey.service.IClubService;
 import ar.noxit.ehockey.service.IDivisionService;
 import ar.noxit.ehockey.service.ISectorService;
 import ar.noxit.ehockey.service.transfer.EquipoPlano;
 import ar.noxit.ehockey.web.pages.components.HybridSingleAndMultipleChoicePanel;
 import ar.noxit.ehockey.web.pages.components.RequiredHybridSingleAndMultipleChoicePanel;
 import ar.noxit.ehockey.web.pages.models.ClubModel;
 import ar.noxit.ehockey.web.pages.models.ClubesListModel;
 import ar.noxit.ehockey.web.pages.models.DivisionListModel;
 import ar.noxit.ehockey.web.pages.models.DivisionModel;
 import ar.noxit.ehockey.web.pages.models.SectorModel;
 import ar.noxit.ehockey.web.pages.models.SectoresListModel;
 import ar.noxit.ehockey.web.pages.renderers.ClubRenderer;
 import ar.noxit.ehockey.web.pages.renderers.DivisionRenderer;
 import ar.noxit.ehockey.web.pages.renderers.SectorRenderer;
 import java.util.List;
 import org.apache.commons.lang.Validate;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 public abstract class EquipoFormPanel extends Panel {
 
     @SpringBean
     private IClubService clubService;
     @SpringBean
     private IDivisionService divisionService;
     @SpringBean
     private ISectorService sectorService;
     private boolean clubActivo = true;
     private boolean divisionActivo = true;
     private boolean sectorActivo = true;
 
     public EquipoFormPanel(String id, IModel<EquipoPlano> equipo) {
         super(id);
         Validate.notNull(equipo);
 
         add(new FeedbackPanel("feedback"));
 
         Form<EquipoPlano> form = new Form<EquipoPlano>("form", equipo) {
 
             @Override
             protected void onSubmit() {
                 EquipoFormPanel.this.onSubmit(getModelObject());
             }
         };
 
         // nombre del equipo
         form.add(crearNombreTextField("nombre", equipo));
 
         // club
         form.add(crearHybridClubChoice("club", equipo));
 
         // division
         form.add(crearHybridDivisionChoice("division", equipo));
 
         // sector
         form.add(crearHybridSectorChoice("sector", equipo));
 
         add(form);
     }
 
     protected RequiredTextField<String> crearNombreTextField(String id, IModel<EquipoPlano> equipo) {
         return new RequiredTextField<String>(id, new PropertyModel<String>(equipo, "nombre"));
     }
 
     protected HybridSingleAndMultipleChoicePanel<Club> crearHybridClubChoice(String id, IModel<EquipoPlano> equipo) {
         IModel<Integer> clubId = new PropertyModel<Integer>(equipo, "clubId");
         IModel<Club> club = new ClubModel(clubId, clubService);
         IModel<List<Club>> clubes = new ClubesListModel(clubService);
         IChoiceRenderer<Club> clubRenderer = new ClubRenderer();
 
        return new RequiredHybridSingleAndMultipleChoicePanel<Club>(id, club, clubes, clubRenderer) {
 
             @Override
             public boolean isEnabled() {
                 return clubActivo;
             }
         };
     }
 
     protected HybridSingleAndMultipleChoicePanel<Division> crearHybridDivisionChoice(
             String id, IModel<EquipoPlano> equipo) {
 
         IModel<Integer> divisionId = new PropertyModel<Integer>(equipo, "divisionId");
         IModel<Division> division = new DivisionModel(divisionId, divisionService);
         IModel<List<Division>> divisiones = new DivisionListModel(divisionService);
         IChoiceRenderer<Division> divisionRenderer = new DivisionRenderer();
 
        return new RequiredHybridSingleAndMultipleChoicePanel<Division>(id, division, divisiones, divisionRenderer) {
 
             @Override
             public boolean isEnabled() {
                 return divisionActivo;
             }
         };
     }
 
     protected HybridSingleAndMultipleChoicePanel<Sector> crearHybridSectorChoice(String id, IModel<EquipoPlano> equipo) {
         IModel<Integer> sectorId = new PropertyModel<Integer>(equipo, "sectorId");
         IModel<Sector> sector = new SectorModel(sectorId, sectorService);
         IModel<List<Sector>> divisiones = new SectoresListModel(sectorService);
         IChoiceRenderer<Sector> divisionRenderer = new SectorRenderer();
 
        return new RequiredHybridSingleAndMultipleChoicePanel<Sector>(id, sector, divisiones, divisionRenderer) {
 
             @Override
             public boolean isEnabled() {
                 return sectorActivo;
             }
         };
     }
 
     public EquipoFormPanel setClubActivo(boolean clubActivo) {
         this.clubActivo = clubActivo;
         return this;
     }
 
     public EquipoFormPanel setDivisionActivo(boolean divisionActivo) {
         this.divisionActivo = divisionActivo;
         return this;
     }
 
     public EquipoFormPanel setSectorActivo(boolean sectorActivo) {
         this.sectorActivo = sectorActivo;
         return this;
     }
 
     protected abstract void onSubmit(EquipoPlano modelObject);
 }
