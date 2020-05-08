 package ar.noxit.ehockey.web.pages.planilla;
 
 import ar.noxit.ehockey.model.DatosEquipoPlanilla;
 import ar.noxit.ehockey.model.Equipo;
 import ar.noxit.ehockey.model.Jugador;
 import ar.noxit.ehockey.model.Partido;
 import ar.noxit.ehockey.model.PlanillaFinal;
 import ar.noxit.ehockey.model.TarjetasPartido;
 import ar.noxit.ehockey.service.IDateTimeProvider;
 import ar.noxit.ehockey.service.IPlanillaService;
 import ar.noxit.ehockey.web.pages.base.AbstractContentPage;
import ar.noxit.ehockey.web.pages.base.MensajePage;
 import ar.noxit.ehockey.web.pages.header.IMenuItem;
 import ar.noxit.ehockey.web.pages.torneo.TorneoPage;
 import ar.noxit.exceptions.NoxitException;
 import ar.noxit.exceptions.NoxitRuntimeException;
 import ar.noxit.web.wicket.model.AdapterModel;
import java.util.Map;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
 
 public class ModificarPlanillaPage extends AbstractContentPage {
 
     private IModel<Integer> golesLocal;
     private IModel<Integer> golesVisitante;
     private IModel<EquipoInfo> infoLocal;
     private IModel<EquipoInfo> infoVisitante;
 
     @SpringBean
     private IPlanillaService planillaService;
     @SpringBean
     private IDateTimeProvider dateTimeProvider;
 
     public ModificarPlanillaPage(final IModel<Partido> partido) {
         IModel<PlanillaFinal> planillaFinal = new PlanillaFinalModel(partido, dateTimeProvider);
         infoLocal = new EquipoInfoModel(new PropertyModel<DatosEquipoPlanilla>(planillaFinal, "datosLocal"));
         infoVisitante = new EquipoInfoModel(new PropertyModel<DatosEquipoPlanilla>(planillaFinal, "datosVisitante"));
         golesLocal = new PropertyModel<Integer>(planillaFinal, "golesLocal");
         golesVisitante = new PropertyModel<Integer>(planillaFinal, "golesVisitante");
 
         add(new FeedbackPanel("feedback"));
         Form<Void> form = new Form<Void>("edicion_planilla") {
 
             @Override
             protected void onSubmit() {
                 try {
                     Integer golesLocalInt = golesLocal.getObject();
                     Integer golesVisitanteInt = golesVisitante.getObject();
                     Integer partidoId = partido.getObject().getId();
                     planillaService.updatePlanilla(partidoId, golesLocalInt, golesVisitanteInt,
                             infoLocal.getObject(), infoVisitante.getObject());
                    setResponsePage(new MensajePage("Planilla de Partido",
                            "Se completó la actualización de la planilla"));
                 } catch (NoxitException e) {
                     throw new NoxitRuntimeException(e);
                 }
             }
         };
         form.add(new PlanillaGeneralPanel("planillaGeneral", planillaFinal, golesLocal, golesVisitante));
         form.add(new PlanillaEquipoPanel("planillaLocal", new PropertyModel<Equipo>(partido, "local"), infoLocal));
         form.add(new PlanillaEquipoPanel("planillaVisitante", new PropertyModel<Equipo>(partido, "visitante"),
                 infoVisitante));
 
         this.add(form);
     }
 
     public class EquipoInfoModel extends AdapterModel<EquipoInfo, DatosEquipoPlanilla> {
 
         private boolean cargado = false;
         private IModel<EquipoInfo> equipoInfo = new Model<EquipoInfo>();
 
         public EquipoInfoModel(IModel<DatosEquipoPlanilla> delegate) {
             super(delegate);
         }
 
         @Override
         protected EquipoInfo getObject(IModel<DatosEquipoPlanilla> datosEquipo) {
             if (!cargado) {
                 EquipoInfo temp = new EquipoInfo();
 
                 DatosEquipoPlanilla object = datosEquipo.getObject();
                 temp.setArbitro(object.getArbitro());
                 temp.setDt(object.getdT());
                 temp.setGoleadores(object.getGoleadores());
                 temp.setJuezMesa(object.getJuezDeMesa());
                 temp.setMedico(object.getMedico());
                 temp.setPf(object.getpFisico());
                 temp.setCapitan(object.getCapitan());
                 for (Jugador j : object.getJugadores()) {
                     temp.getSeleccionados().add(j.getFicha());
                 }
                 for (Map.Entry<Jugador, TarjetasPartido> entry : object.getTarjetas().entrySet()) {
                     Jugador j = entry.getKey();
                     TarjetasPartido value = entry.getValue();
                     temp.getAmonestaciones().add(new AmonestacionInfo(j.getFicha(),
                                     value.getRojas(),
                                     value.getAmarillas(),
                                     value.getVerdes()));
                 }
                 this.cargado = true;
                 equipoInfo.setObject(temp);
             }
             return equipoInfo.getObject();
         }
 
         @Override
         protected void setObject(EquipoInfo equipoInfo, IModel<DatosEquipoPlanilla> arg1) {
             this.equipoInfo.setObject(equipoInfo);
             this.cargado = true;
         }
     }
 
     @Override
     public boolean shouldBeSelected(IMenuItem menuItem) {
         return menuItem.getPageLink().equals(TorneoPage.class);
     }
 }
