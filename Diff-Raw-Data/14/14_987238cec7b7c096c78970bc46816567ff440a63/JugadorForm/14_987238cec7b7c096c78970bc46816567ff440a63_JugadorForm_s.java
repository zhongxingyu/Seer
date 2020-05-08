 package ar.noxit.ehockey.web.pages.jugadores;
 
 import java.util.Date;
 
 import org.apache.commons.lang.Validate;
 import org.apache.wicket.extensions.markup.html.form.DateTextField;
 import org.apache.wicket.extensions.yui.calendar.DatePicker;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.joda.time.LocalDate;
 
 import ar.noxit.ehockey.model.Club;
 import ar.noxit.ehockey.model.Division;
 import ar.noxit.ehockey.model.Sector;
 import ar.noxit.ehockey.service.IClubService;
 import ar.noxit.ehockey.service.IDivisionService;
 import ar.noxit.ehockey.service.ISectorService;
 import ar.noxit.ehockey.web.pages.models.ClubListModel;
 import ar.noxit.ehockey.web.pages.models.DivisionListModel;
 import ar.noxit.ehockey.web.pages.models.IdClubModel;
 import ar.noxit.ehockey.web.pages.models.IdDivisionModel;
 import ar.noxit.ehockey.web.pages.models.IdSectorModel;
 import ar.noxit.ehockey.web.pages.models.SectorListModel;
 import ar.noxit.ehockey.web.pages.renderers.ClubRenderer;
 import ar.noxit.ehockey.web.pages.renderers.DivisionRenderer;
 import ar.noxit.ehockey.web.pages.renderers.SectorRenderer;
 import ar.noxit.web.wicket.model.AdapterModel;
 
 public abstract class JugadorForm extends Panel {
 
     @SpringBean
     private IClubService clubService;
     @SpringBean
     private IDivisionService divisionService;
     @SpringBean
     private ISectorService sectorService;
 
     public JugadorForm(String id, final IModel<JugadorPlano> jugador) {
         super(id);
         Validate.notNull(jugador);
 
         Form<JugadorPlano> form = new Form<JugadorPlano>("agregar_jugador",
                 jugador) {
 
             @Override
             protected void onSubmit() {
                 JugadorForm.this.onSubmit(jugador);
             }
         };
 
         form.add(new RequiredTextField<String>("nombre",
                 new PropertyModel<String>(jugador, "nombre")));
         form.add(new RequiredTextField<String>("apellido",
                 new PropertyModel<String>(jugador, "apellido")));
        form.add(new TextField<String>("tipodoc", new PropertyModel<String>(
                jugador, "tipoDocumento")));
         form.add(new TextField<String>("numerodoc", new PropertyModel<String>(
                 jugador, "numeroDocumento")));
 
         PropertyModel<LocalDate> modelFechaNacimiento = new PropertyModel<LocalDate>(
                 jugador, "fechaNacimiento");
         DateTextField fechaEvento = new DateTextField("fechanac",
                 new DateAdapter(modelFechaNacimiento), "dd/MM/yyyy");
         fechaEvento.add(new DatePicker());
         form.add(fechaEvento);
 
         form.add(new TextField<String>("telefono", new PropertyModel<String>(
                 jugador, "telefono")));
 
         form.add(new TextField<String>("fechaalta", new Model<String>(
                 new LocalDate().toString("dd/MM/yyyy"))).setEnabled(false));
 
         form.add(new DropDownChoice<Club>("club", new IdClubModel(
                 new PropertyModel<Integer>(jugador, "clubId"), clubService),
                 new ClubListModel(clubService), new ClubRenderer())
                 .setRequired(true));
 
         form.add(new DropDownChoice<Division>("division", new IdDivisionModel(
                 new PropertyModel<Integer>(jugador, "divisionId"),
                 divisionService), new DivisionListModel(divisionService),
                 new DivisionRenderer()).setRequired(true));
 
         form
                 .add(new DropDownChoice<Sector>("sector", new IdSectorModel(
                         new PropertyModel<Integer>(jugador, "sectorId"),
                         sectorService), new SectorListModel(sectorService),
                         new SectorRenderer()).setRequired(true));
         form.add(new TextField<String>("letra", new PropertyModel<String>(
                 jugador, "letraJugador")));
         add(form);
         add(new FeedbackPanel("feedback"));
     }
 
     protected abstract void onSubmit(IModel<JugadorPlano> jugador);
 
     private class DateAdapter extends AdapterModel<Date, LocalDate> {
 
         public DateAdapter(IModel<LocalDate> delegate) {
             super(delegate);
         }
 
         @SuppressWarnings("deprecation")
         @Override
         protected Date getObject(IModel<LocalDate> delegate) {
             LocalDate fecha = delegate.getObject();
             if (fecha == null)
                 return null;
             return new Date(fecha.getYear(), fecha.getMonthOfYear(), fecha
                     .getDayOfMonth());
         }
 
         @Override
         protected void setObject(Date object, IModel<LocalDate> delegate) {
             LocalDate temp = delegate.getObject();
             delegate.setObject(new LocalDate(object));
         }
     }
 }
