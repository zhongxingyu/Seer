 package ar.noxit.ehockey.web.pages.torneo;
 
 import static java.util.Collections.sort;
 
 import ar.noxit.ehockey.exception.PlanillaNoDisponibleException;
 import ar.noxit.ehockey.model.Administrador;
 import ar.noxit.ehockey.model.Partido;
 import ar.noxit.ehockey.model.PartidosComparator;
 import ar.noxit.ehockey.model.PlanillaFinal;
 import ar.noxit.ehockey.model.Torneo;
 import ar.noxit.ehockey.model.Usuario;
 import ar.noxit.ehockey.service.IDateTimeProvider;
 import ar.noxit.ehockey.service.IPartidoService;
 import ar.noxit.ehockey.web.pages.authentication.AuthSession;
 import ar.noxit.ehockey.web.pages.base.AbstractHeaderPage;
 import ar.noxit.ehockey.web.pages.header.IMenuItem;
 import ar.noxit.ehockey.web.pages.models.PartidoModel;
 import ar.noxit.ehockey.web.pages.planilla.PlanillaFinalModel;
 import ar.noxit.utils.Collections;
 import ar.noxit.web.wicket.column.AbstractLabelColumn;
 import ar.noxit.web.wicket.model.AdapterModel;
 import ar.noxit.web.wicket.model.LocalDateTimeFormatModel;
 import ar.noxit.web.wicket.provider.DataProvider;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.commons.lang.Validate;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
 import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.markup.repeater.Item;
 import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.joda.time.LocalDateTime;
 
 public class VerPartidosPage extends AbstractHeaderPage {
 
     @SpringBean
     private IPartidoService partidoService;
     private DefaultDataTable<Partido> dataTable;
     @SpringBean
     private IDateTimeProvider dateTimeProvider;
     private final static ResourceReference CHECK = new ResourceReference(VerPartidosPage.class, "check.png");
     private final static ResourceReference CALENDAR = new ResourceReference(VerPartidosPage.class, "calendar.png");
 
     public VerPartidosPage(IModel<Torneo> torneo) {
         Validate.notNull(torneo, "torneo no puede ser null");
 
         List<IColumn<Partido>> columns = new ArrayList<IColumn<Partido>>();
         columns.add(new PropertyColumn<Partido>(Model.of("Local"), "local.nombre"));
         columns.add(new PropertyColumn<Partido>(Model.of("Visitante"), "visitante.nombre"));
         columns.add(new PropertyColumn<Partido>(Model.of("R"), "rueda"));
         columns.add(new PropertyColumn<Partido>(Model.of("F"), "fechaDelTorneo"));
         columns.add(new PropertyColumn<Partido>(Model.of("P"), "partido"));
         columns.add(new AbstractLabelColumn<Partido>(Model.of("Inicio")) {
 
             @Override
             protected IModel<String> createDisplayModel(IModel<Partido> rowModel) {
                 return new LocalDateTimeFormatModel(new PropertyModel<LocalDateTime>(rowModel, "inicio"));
             }
         });
         columns.add(new AbstractColumn<Partido>(Model.of("Jugado")) {
 
             @Override
             public void populateItem(Item<ICellPopulator<Partido>> cellItem, String componentId,
                     IModel<Partido> rowModel) {
                 cellItem.add(new PartidoJugadoFragment(componentId, "jugado", getPage(), rowModel));
             }
         });
         columns.add(new AbstractColumn<Partido>(Model.of("Planillas")) {
 
             @Override
             public void populateItem(Item<ICellPopulator<Partido>> cellItem, String componentId,
                     IModel<Partido> rowModel) {
                 cellItem.add(new PlanillasFragment(componentId, "planillas", getPage(), rowModel));
             }
         });
         columns.add(new AbstractColumn<Partido>(Model.of("Estado Planilla")) {
 
             @Override
             public void populateItem(Item<ICellPopulator<Partido>> cellItem, String componentId,
                     IModel<Partido> rowModel) {
 
                 IModel<PlanillaFinal> planillaFinalModel = new PlanillaFinalModel(rowModel, dateTimeProvider);
                 cellItem.add(new Label(componentId, new EstadoPlanillaAdapterModel(planillaFinalModel))
                         .setRenderBodyOnly(true));
             }
 
         });
         agregarColumnaAcciones(columns);
 
         this.dataTable = new DefaultDataTable<Partido>("partidos", columns, new PartidosFromTorneoDataProvider(torneo),
                 20) {
 
             @Override
             protected Item<Partido> newRowItem(String id, int index, final IModel<Partido> model) {
                 return new Item<Partido>(id, index, model) {
 
                     @Override
                     protected void onComponentTag(ComponentTag tag) {
                         super.onComponentTag(tag);
                         Partido partido = model.getObject();
                         tag.put("class", (partido.getRueda() + partido.getFechaDelTorneo()) % 2 == 0 ? "even" : "odd");
                     }
                 };
             }
         };
         this.dataTable.setItemReuseStrategy(new ReuseIfModelsEqualStrategy());
         this.dataTable.setOutputMarkupId(true);
         add(dataTable);
     }
 
     private void agregarColumnaAcciones(List<IColumn<Partido>> columns) {
         Usuario userLogged = AuthSession.get().getUserLogged();
         if (userLogged instanceof Administrador) {
             columns.add(new AbstractColumn<Partido>(Model.of("Acciones")) {
 
                 @Override
                 public void populateItem(Item<ICellPopulator<Partido>> cellItem, String componentId,
                         IModel<Partido> rowModel) {
                     cellItem.add(new AccionesFragment(componentId, "acciones", getPage(), rowModel));
                 }
             });
         }
     }
 
     private class EstadoPlanillaAdapterModel extends AdapterModel<String, PlanillaFinal> {
 
         public EstadoPlanillaAdapterModel(IModel<PlanillaFinal> delegate) {
             super(delegate);
         }
 
         @Override
         protected String getObject(IModel<PlanillaFinal> delegate) {
             try {
                 PlanillaFinal object = delegate.getObject();
                 return object.getEstadoReducido();
             } catch (Exception e) {
                 return "No disponible.";
             }
         }
 
         @Override
         protected void setObject(String object, IModel<PlanillaFinal> delegate) {
         }
     }
 
     private final class PartidosFromTorneoDataProvider extends DataProvider<Partido> {
 
         private IModel<Torneo> torneo;
 
         public PartidosFromTorneoDataProvider(IModel<Torneo> torneo) {
             Validate.notNull(torneo);
             this.torneo = torneo;
         }
 
         @Override
         protected List<Partido> loadList() {
             Torneo t = torneo.getObject();
             List<Partido> list = Collections.toList(t.iteradorPartidos());
             sort(list, PartidosComparator.comparatorPorRuedaFechaYPartido());
             return list;
         }
 
         @Override
         public IModel<Partido> model(Partido object) {
             return new PartidoModel(Model.of(object.getId()), partidoService);
         }
     }
 
     private class PlanillasFragment extends Fragment {
 
         public PlanillasFragment(String id,
                 String markupId,
                 MarkupContainer markupProvider,
                 final IModel<Partido> rowModel) {
 
             super(id, markupId, markupProvider);
 
             final PlanillaPrecargadaLink planillaPrecargadaLink = new PlanillaPrecargadaLink("precargada", rowModel);
             add(planillaPrecargadaLink);
 
             final PlanillaFinalLink planillaFinalLink = new PlanillaFinalLink("final", rowModel);
             planillaFinalLink.add(new Label("desc_final", new AbstractReadOnlyModel<String>() {
 
                 @Override
                 public String getObject() {
                     try {
                         Partido partido = rowModel.getObject();
                         PlanillaFinal planilla = partido.getPlanilla(dateTimeProvider.getLocalDateTime());
                         Usuario userLogged = AuthSession.get().getUserLogged();
 
                         if (userLogged instanceof Administrador) {
                             if (planilla.isFinalizada()) {
                                 return "Final";
                             }
                             if (!planilla.isVencida()) {
                                 return "Ver parcial...";
                             }
                             if (planilla.isVencida()) {
                                 return "Finalizar...";
                             }
                         } else {
                             // local
                             if (userLogged.puedeVer(partido.getLocal().getClub())) {
                                 if (planilla.isRechazada()) {
                                     return "Publicar...";
                                 }
                                 if (planilla.isEditable()) {
                                     return "Publicar...";
                                 }
                                 if (planilla.isFinalizada()) {
                                     return "Final";
                                 }
                                 if (planilla.isPublicada()) {
                                     return "Ver parcial...";
                                 }
                                 if (planilla.isVencida()) {
                                     return "Ver parcial...";
                                 }
                             } else {
                                 if (planilla.isRechazada()) {
                                     return "Ver parcial...";
                                 }
                                 if (planilla.isEditable()) {
                                     return "Vista parcial...";
                                 }
                                 if (planilla.isFinalizada()) {
                                     return "Final";
                                 }
                                 if (planilla.isPublicada()) {
                                     return "Validar...";
                                 }
                                 if (planilla.isVencida()) {
                                     return "Ver parcial...";
                                 }
                             }
                         }
                     } catch (PlanillaNoDisponibleException e) {
                         return "Final";
                     }
                     return null;
                 }
             }));
             add(planillaFinalLink);
 
             add(new WebMarkupContainer("separator") {
 
                 @Override
                 public boolean isVisible() {
                     return planillaFinalLink.determineVisibility() && planillaPrecargadaLink.determineVisibility();
                 }
             }.setRenderBodyOnly(true));
 
             add(new WebMarkupContainer("no_acciones") {
 
                 @Override
                 public boolean isVisible() {
                     return !planillaFinalLink.determineVisibility() && !planillaPrecargadaLink.determineVisibility();
                 }
             }.setRenderBodyOnly(true));
         }
     }
 
     private class AccionesFragment extends Fragment {
 
         public AccionesFragment(String id,
                 String markupId,
                 MarkupContainer markupProvider,
                 final IModel<Partido> partido) {
 
             super(id, markupId, markupProvider);
 
             // Reprogramación
             ModalWindow modal = new ModalWindow("modal");
             modal.setPageMapName("modal-1");
             modal.setCookieName("modal-1");
             add(modal);
 
             final AjaxLink<Void> reprogramar = new ReprogramarPartidoLink("reprogramar", modal, partido, dataTable);
             reprogramar.add(new Image("reprogramar", CALENDAR));
             add(reprogramar);
 
             final TerminarPartidoLink terminar = new TerminarPartidoLink("terminar", partido);
             terminar.add(new Image("terminar", CHECK));
             add(terminar);
 
             add(new WebMarkupContainer("no_acciones") {
 
                 @Override
                 public boolean isVisible() {
                     return !(reprogramar.determineVisibility() || terminar.determineVisibility());
                 }
             }.setRenderBodyOnly(true));
         }
     }
 
     private class PartidoJugadoFragment extends Fragment {
 
         public PartidoJugadoFragment(String id,
                 String markupId,
                 MarkupContainer markupProvider,
                 final IModel<Partido> partido) {
 
             super(id, markupId, markupProvider);
 
             add(new Label("jugado", new AbstractReadOnlyModel<String>() {
 
                 @Override
                 public String getObject() {
                     return partido.getObject().isJugado() ? "Si" : "No";
                 }
             }));
         }
     }
 
     @Override
     public boolean shouldBeSelected(IMenuItem menuItem) {
         return menuItem.getPageLink().equals(TorneoPage.class);
     }
 }
