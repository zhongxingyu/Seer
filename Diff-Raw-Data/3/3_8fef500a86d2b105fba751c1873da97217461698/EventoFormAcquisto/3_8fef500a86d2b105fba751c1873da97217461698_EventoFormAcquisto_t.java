 package it.agilis.mens.azzeroCO2.client.forms.evento;
 
 import com.extjs.gxt.ui.client.Style;
 import com.extjs.gxt.ui.client.binding.FormBinding;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.util.Padding;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.VerticalPanel;
 import com.extjs.gxt.ui.client.widget.button.ToolButton;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.LabelField;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.layout.*;
 import com.google.gwt.user.client.Element;
 import it.agilis.mens.azzeroCO2.shared.model.RiepilogoModel;
 import it.agilis.mens.azzeroCO2.shared.model.amministrazione.ProgettoDiCompensazioneModel;
 import it.agilis.mens.azzeroCO2.shared.model.evento.DettaglioModel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: giovannilt
  * Date: 6/19/11
  * Time: 5:19 PM
  * To change this template use File | Settings | File Templates.
  */
 public class EventoFormAcquisto extends LayoutContainer {
     private ContentPanel east = new ContentPanel();
     private ContentPanel centre = new ContentPanel();
     private ListStore<ProgettoDiCompensazioneModel> store = new ListStore<ProgettoDiCompensazioneModel>();
     private FormBinding binding = null;
 
 
     private double totaleKC02 = 0;
     private LabelField titoloEvento = new LabelField("Titolo Evento ");
     private LabelField kcO2Evento = new LabelField("Kg C02");
     private final LabelField titoloProgettoScelto = new LabelField("TitoloProgettoScelto");
     private final LabelField euroPerKCo2Progetto = new LabelField(" 0.00");
     private final LabelField totale = new LabelField(" 0.00");
 
     @Override
     protected void onRender(Element parent, int index) {
         super.onRender(parent, index);
 
         BorderLayout layout = new BorderLayout();
         setLayout(layout);
         layout.setEnableState(false);
         setStyleAttribute("padding", "0px");
 
         Grid<ProgettoDiCompensazioneModel> grid = createGrid();
         FormPanel form = createForm();
 
         binding = new FormBinding(form, true);
         binding.setStore(grid.getStore());
 
         VerticalPanel vp = new VerticalPanel();
         vp.setHeight(493);
         vp.add(form);
         east.add(vp);
 
         grid.getSelectionModel().addListener(Events.SelectionChange,
                 new Listener<SelectionChangedEvent<ProgettoDiCompensazioneModel>>() {
                     public void handleEvent(SelectionChangedEvent<ProgettoDiCompensazioneModel> be) {
                         if (be.getSelection().size() > 0) {
                             titoloProgettoScelto.setText(be.getSelection().get(0).getNome());
                             //euroPerKCo2Progetto.setText((be.getSelection().get(0).getKgCO2() * be.getSelection().get(0).getPrezzo()) + "");
                             //totale.setText((be.getSelection().get(0).getKgCO2() * be.getSelection().get(0).getPrezzo() * totaleKC02) + "");
                             binding.bind(be.getSelection().get(0));
                         } else {
                             titoloProgettoScelto.setText("TitoloProgettoScelto");
                             euroPerKCo2Progetto.setText("0.0");
                             totale.setText("0.0");
                             binding.unbind();
                         }
                     }
                 });
         east.setHeading("Acquisto");
         BorderLayoutData westData = new BorderLayoutData(Style.LayoutRegion.EAST, 300);
         east.getHeader().addTool(new ToolButton("x-tool-help"));
         east.getHeader().addTool(new ToolButton("x-tool-refresh"));
         westData.setMargins(new Margins(0));
         east.setAutoHeight(true);
         add(east, westData);
 
         centre.setLayout(new RowLayout(Style.Orientation.HORIZONTAL));
         centre.add(grid, new RowData(1, 1));
         centre.setHeading("Progetti Di Compensazione");
         centre.setHeight(520);
 
         BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
         centerData.setMargins(new Margins(0));
         add(centre, centerData);
 
     }
 
     private FormPanel createForm() {
         FormPanel panel = new FormPanel();
         panel.setFrame(true);
         panel.setHeaderVisible(false);
         panel.setSize(300, -1);
         panel.setLabelAlign(FormPanel.LabelAlign.LEFT);
         HBoxLayoutData flex = new HBoxLayoutData(new Margins(0, 2, 0, 0));
         {
             LayoutContainer c = new LayoutContainer();
             HBoxLayout layout = new HBoxLayout();
             layout.setPadding(new Padding(1));
             layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.BOTTOM);
             c.setLayout(layout);
             LabelField label = new LabelField("Evento: ");
             label.setStyleAttribute("font-size", "16px");
             c.add(label, flex);
             panel.add(c);
         }
         {
             {
                 LayoutContainer c = new LayoutContainer();
                 HBoxLayout layout = new HBoxLayout();
                 layout.setPadding(new Padding(2));
                 layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.BOTTOM);
                 c.setLayout(layout);
                 titoloEvento.setWidth(220);
                 c.add(titoloEvento);
                 panel.add(c, new FormData("100%"));
             }
             {
                 LayoutContainer c = new LayoutContainer();
                 HBoxLayout layout = new HBoxLayout();
                 layout.setPadding(new Padding(2));
                 layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.BOTTOM);
                 c.setLayout(layout);
                 LabelField label = new LabelField("Kg/CO2");
                 label.setWidth(190);
                 c.add(label);
                 c.add(kcO2Evento, flex);
 
                 panel.add(c, new FormData("100%"));
             }
             {    // PROGETTO SCELTO
                 LayoutContainer c = new LayoutContainer();
                 HBoxLayout layout = new HBoxLayout();
                 layout.setPadding(new Padding(1));
                 layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.BOTTOM);
                 c.setLayout(layout);
 
                 LabelField label = new LabelField("Progetto:");
                 label.setStyleAttribute("font-size", "16px");
                 label.setWidth(220);
                 c.add(label);
 
                 panel.add(c, new FormData("100%"));
             }
             {
                 LayoutContainer c = new LayoutContainer();
                 HBoxLayout layout = new HBoxLayout();
                 layout.setPadding(new Padding(2));
                 layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.BOTTOM);
                 c.setLayout(layout);
 
                 titoloProgettoScelto.setWidth(220);
                 c.add(titoloProgettoScelto);
 
                 panel.add(c, new FormData("100%"));
             }
             {
                 LayoutContainer c = new LayoutContainer();
                 HBoxLayout layout = new HBoxLayout();
                 layout.setPadding(new Padding(5));
                 layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.BOTTOM);
                 c.setLayout(layout);
 
                 LabelField label = new LabelField("€ x Kg/CO2 ");
                 label.setWidth(180);
                 c.add(label);
                 c.add(euroPerKCo2Progetto);
 
                 panel.add(c, new FormData("100%"));
             }
             { // TOTALE
                 LayoutContainer c = new LayoutContainer();
                 HBoxLayout layout = new HBoxLayout();
                 layout.setPadding(new Padding(5));
                 layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.BOTTOM);
                 c.setLayout(layout);
 
                 LabelField label = new LabelField("Totale € ");
                 label.setStyleAttribute("color", "#FF9933");
                 label.setStyleAttribute("font-size", "16px");
                 label.setWidth(180);
                 c.add(label);
 
                 totale.setStyleAttribute("color", "#FF9933");
                 totale.setStyleAttribute("font-size", "16px");
                 c.add(totale, flex);
 
                 panel.add(c, new FormData("100%"));
             }
             { // Coupon
                 LayoutContainer c = new LayoutContainer();
                 HBoxLayout layout = new HBoxLayout();
                 layout.setPadding(new Padding(5));
                 layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.BOTTOM);
                 c.setLayout(layout);
 
                 LabelField label = new LabelField("Hai Un Coupon? ");
                 label.setWidth(100);
                 c.add(label);
                 TextField<String> coupon = new TextField<String>();
                 coupon.setWidth(150);
                 c.add(coupon, flex);
                 panel.add(c, new FormData("100%"));
             }
             {
                 LayoutContainer c = new LayoutContainer();
                 c.setHeight(250);
                 c.setWidth(290);
                 c.setStyleAttribute("background-color", "#FF9933");
                 HBoxLayout layout = new HBoxLayout();
                 layout.setPadding(new Padding(10));
                 layout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
                 c.setLayout(layout);
 
                 LabelField label = new LabelField("AzzeroCO2 puo' offrirti <br>consulenza per<br>la riduzione delle emissioni <br>Chiamaci !!");
                 label.setStyleAttribute("font-size", "20px");
                 label.setWidth(300);
                 c.add(label);
                 panel.add(c, new FormData("100%"));
             }
         }
         return panel;
     }
 
     private Grid<ProgettoDiCompensazioneModel> createGrid() {
         List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
 
         ColumnConfig column = new ColumnConfig("nome", "Progetto", 310);
         configs.add(column);
 
 
         column = new ColumnConfig("prezzo", "Euro", 60);
         column.setAlignment(Style.HorizontalAlignment.RIGHT);
         configs.add(column);
 
         ColumnModel cm = new ColumnModel(configs);
 
         Grid<ProgettoDiCompensazioneModel> grid = new Grid<ProgettoDiCompensazioneModel>(store, cm);
         grid.setBorders(true);
         //grid.setHideHeaders(true);
         grid.setHeight(525);
 
         return grid;
     }
 
     public void setInStore(List<ProgettoDiCompensazioneModel> progettoDiCompensazioneModel) {
         this.store.removeAll();
         this.store.add(progettoDiCompensazioneModel);
     }
 
     public void clear() {
     }
 
     public void setRiepilogo(List<RiepilogoModel> eventoRiepilogoModels, DettaglioModel riepilogo) {
         double totale = 0;
         for (RiepilogoModel r : eventoRiepilogoModels) {
             totale += r.getKgCO2();
         }
         this.totaleKC02 = totale;
         kcO2Evento.setText(totale + "");
         titoloEvento.setText(riepilogo.getNome());
     }
 }
 
