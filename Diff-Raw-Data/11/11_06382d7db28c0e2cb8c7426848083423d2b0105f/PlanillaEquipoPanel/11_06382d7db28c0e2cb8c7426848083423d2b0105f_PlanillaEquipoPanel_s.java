 package ar.noxit.ehockey.web.pages.planilla;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.Page;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.PageCreator;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow.WindowClosedCallback;
 import org.apache.wicket.extensions.markup.html.form.palette.Palette;
 import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
 import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import ar.noxit.ehockey.model.Equipo;
 import ar.noxit.ehockey.model.Jugador;
 import ar.noxit.ehockey.service.IClubService;
 import ar.noxit.ehockey.service.IJugadorService;
 import ar.noxit.ehockey.web.pages.jugadores.JugadorModalPage;
 import ar.noxit.ehockey.web.pages.models.JugadorModel;
 import ar.noxit.ehockey.web.pages.models.JugadoresParaEquipoListModel;
 import ar.noxit.ehockey.web.pages.renderers.JugadorRenderer;
 import ar.noxit.ehockey.web.pages.torneo.NuevaAmonestacionPage;
 import ar.noxit.exceptions.NoxitException;
 import ar.noxit.exceptions.NoxitRuntimeException;
 import ar.noxit.web.wicket.column.AbstractLabelColumn;
 import ar.noxit.web.wicket.model.AdapterModel;
 
 public class PlanillaEquipoPanel extends Panel {
 
     @SpringBean
     private IClubService clubService;
     @SpringBean
     private IJugadorService jugadorService;
 
     public PlanillaEquipoPanel(String id, final IModel<Equipo> equipo, final IModel<EquipoInfo> info) {
         super(id);
 
         // VENTANA MODAL
         final ModalWindow modal = new ModalWindow("modal");
         modal.setPageMapName("amonestaciones");
         modal.setCookieName("amonestaciones");
         add(modal);
 
         // LISTA DE JUGADORES MODEL
         final IModel<List<Jugador>> jugadoresModel = new JugadoresParaEquipoListModel(
                 new PropertyModel<Integer>(equipo, "club.id"), clubService);
 
         // PALETA DE JUGADORES
         final Component palette = new Palette<Jugador>("palette",
                 new JugadoresSeleccionadosModel(info, equipo),
                 jugadoresModel,
                 JugadorRenderer.get(),
                 10,
                 false) {
 
             @Override
             protected Recorder<Jugador> newRecorderComponent() {
                 Recorder<Jugador> recorder = super.newRecorderComponent();
                 recorder.add(new AjaxFormComponentUpdatingBehavior("onchange") {
 
                     @Override
                     protected void onUpdate(AjaxRequestTarget target) {
                     }
                 });
                 return recorder;
             }
         };
         palette.setOutputMarkupId(true);
         add(palette);
 
         add(new AjaxLink<Void>("nuevo_jugador") {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                 modal.setPageCreator(new NuevoJugadorPC(modal, equipo));
                 modal.setWindowClosedCallback(new NuevoJugadorWCB(palette));
                 modal.show(target);
             }
         });
 
         // DATOS DEL PARTIDO
         add(new TextField<String>("goleadores", new PropertyModel<String>(info, "goleadores")));
         add(new TextField<String>("dt", new PropertyModel<String>(info, "dt")));
         add(new TextField<String>("pf", new PropertyModel<String>(info, "pf")));
         add(new TextField<String>("medico", new PropertyModel<String>(info, "medico")));
         add(new TextField<String>("capitan", new PropertyModel<String>(info, "capitan")));
         add(new TextField<String>("juezmesa", new PropertyModel<String>(info, "juezMesa")));
         add(new TextField<String>("arbitro", new PropertyModel<String>(info, "arbitro")));
 
         // AMONESTACIONES
         List<IColumn<AmonestacionInfo>> columns = new ArrayList<IColumn<AmonestacionInfo>>();
         columns.add(new AbstractLabelColumn<AmonestacionInfo>(Model.of("Nombre y Apellido")) {
 
             @Override
             protected IModel<String> createDisplayModel(IModel<AmonestacionInfo> rowModel) {
                 IModel<Integer> id = new PropertyModel<Integer>(rowModel, "jugadorId");
                return new PropertyModel<String>(new JugadorModel(id, jugadorService), "apellido");
             }
         });
         columns.add(new PropertyColumn<AmonestacionInfo>(Model.of("Rojas"), "rojas"));
         columns.add(new PropertyColumn<AmonestacionInfo>(Model.of("Amarillas"), "amarillas"));
         columns.add(new PropertyColumn<AmonestacionInfo>(Model.of("Verdes"), "verdes"));
         final DefaultDataTable<AmonestacionInfo> amonestacionesTable = new DefaultDataTable<AmonestacionInfo>(
                 "amonestaciones",
                         columns,
                         new ListDataProvider(info),
                         25);
         amonestacionesTable.setOutputMarkupId(true);
         add(amonestacionesTable);
 
         add(new AjaxLink<Void>("nueva_amonestacion") {
 
             @Override
             public void onClick(AjaxRequestTarget target) {
                 modal.setPageCreator(new AmonestacionPC(jugadoresModel, modal, info));
                 modal.setWindowClosedCallback(new AmonestacionWCB(amonestacionesTable));
                 modal.show(target);
             }
         });
     }
 
     private final class NuevoJugadorPC implements PageCreator {
 
         private ModalWindow modal;
         private IModel<Equipo> equipo;
 
         public NuevoJugadorPC(ModalWindow modal, IModel<Equipo> equipo) {
             this.modal = modal;
             this.equipo = equipo;
         }
 
         @Override
         public Page createPage() {
             IModel<Integer> clubId = new PropertyModel<Integer>(equipo, "club.id");
             IModel<Integer> divisionId = new PropertyModel<Integer>(equipo, "division.id");
             IModel<Integer> sectorId = new PropertyModel<Integer>(equipo, "sector.id");
             return new JugadorModalPage(modal, clubId, divisionId, sectorId);
         }
     }
 
     private final class NuevoJugadorWCB implements WindowClosedCallback {
 
         private Component palette;
 
         public NuevoJugadorWCB(Component palette) {
             this.palette = palette;
         }
 
         @Override
         public void onClose(AjaxRequestTarget target) {
             target.addComponent(palette);
         }
     }
 
     private final class AmonestacionPC implements PageCreator {
 
         private final IModel<List<Jugador>> jugadoresModel;
         private final ModalWindow modal;
         private final IModel<EquipoInfo> info;
 
         private AmonestacionPC(IModel<List<Jugador>> jugadoresModel, ModalWindow modal, IModel<EquipoInfo> info) {
             this.jugadoresModel = jugadoresModel;
             this.modal = modal;
             this.info = info;
         }
 
         @Override
         public Page createPage() {
             return new NuevaAmonestacionPage(jugadoresModel, modal) {
 
                 @Override
                 protected void onSubmit(AmonestacionInfo amonestacionInfo) {
                     info.getObject().getAmonestaciones().add(amonestacionInfo);
                 }
             };
         }
     }
 
     private final class AmonestacionWCB implements WindowClosedCallback {
 
         private final DefaultDataTable<AmonestacionInfo> amonestacionesTable;
 
         private AmonestacionWCB(DefaultDataTable<AmonestacionInfo> amonestacionesTable) {
             this.amonestacionesTable = amonestacionesTable;
         }
 
         @Override
         public void onClose(AjaxRequestTarget target) {
             target.addComponent(amonestacionesTable);
         }
     }
 
     private class JugadoresSeleccionadosModel extends AdapterModel<List<Jugador>, EquipoInfo> {
 
         private Integer clubId;
 
         public JugadoresSeleccionadosModel(IModel<EquipoInfo> delegate, IModel<Equipo> equipo) {
             super(delegate);
             this.clubId = equipo.getObject().getClub().getId();
         }
 
         @Override
         protected List<Jugador> getObject(IModel<EquipoInfo> equipoInfo) {
             try {
                 return clubService.getJugadoresPorClub(this.clubId, equipoInfo.getObject().getSeleccionados());
             } catch (NoxitException e) {
                 throw new NoxitRuntimeException(e);
             }
         }
 
         @Override
         protected void setObject(List<Jugador> jugadores, IModel<EquipoInfo> equipoInfo) {
             List<Integer> seleccionados = equipoInfo.getObject().getSeleccionados();
             seleccionados.clear();
             for (Jugador j : jugadores) {
                 seleccionados.add(j.getFicha());
             }
         }
     }
 
     private class ListDataProvider extends SortableDataProvider<AmonestacionInfo> {
 
         private IModel<EquipoInfo> info;
 
         public ListDataProvider(IModel<EquipoInfo> info) {
             this.info = info;
         }
 
         @Override
         public Iterator<? extends AmonestacionInfo> iterator(int first, int count) {
             List<AmonestacionInfo> amonestaciones = getAmonestaciones();
 
             int toIndex = first + count;
             if (toIndex > amonestaciones.size()) {
                 toIndex = amonestaciones.size();
             }
             return amonestaciones.subList(first, toIndex).listIterator();
         }
 
         private List<AmonestacionInfo> getAmonestaciones() {
             List<AmonestacionInfo> amonestaciones = info.getObject().getAmonestaciones();
             return amonestaciones;
         }
 
         @Override
         public IModel<AmonestacionInfo> model(AmonestacionInfo object) {
             return Model.of(object);
         }
 
         @Override
         public int size() {
             return getAmonestaciones().size();
         }
     }
 }
