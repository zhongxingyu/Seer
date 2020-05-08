 package com.odea;
 
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.form.TextArea;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.validator.PatternValidator;
 
 import com.odea.behavior.noInput.NoInputBehavior;
 import com.odea.behavior.numberComma.NumberCommaBehavior;
 import com.odea.behavior.onlyNumber.OnlyNumberBehavior;
 import com.odea.components.datepicker.DatePickerBehavior;
 import com.odea.domain.Actividad;
 import com.odea.domain.Entrada;
 import com.odea.domain.Proyecto;
 import com.odea.domain.Usuario;
 import com.odea.services.DAOService;
 import com.odea.validators.duracion.DurationValidator;
 import com.odea.validators.ticketExterno.TicketExternoValidator;
 
 
 public class AgregarEntradasPage extends BasePage {
 	
 	private static final long serialVersionUID = 1088210443697851501L;
 
 	@SpringBean
 	private transient DAOService daoService;
 	
 	private Usuario usuario;
 	
 	IModel<List<Entrada>> lstEntradasModel;
 	IModel<Integer> horasSemanalesModel;
 	ListView<Entrada> entradasListView;
 	DropDownChoice<String> selectorTiempo;
 	Label mensajeProyecto;
 	Label mensajeActividad;
 	
 	WebMarkupContainer listViewContainer;
 	
 	public AgregarEntradasPage() {
 		final Subject subject = SecurityUtils.getSubject();
 		
 		if(!subject.isAuthenticated()){
 			this.redirectToInterceptPage(new LoginPage());
 		}
 		
 		this.usuario = this.daoService.getUsuario(subject.getPrincipal().toString());
 		
 		this.lstEntradasModel = new LoadableDetachableModel<List<Entrada>>() { 
             @Override
             protected List<Entrada> load() {
             	return daoService.getEntradasSemanales(AgregarEntradasPage.this.usuario);
             }
         };
         
         this.horasSemanalesModel = new LoadableDetachableModel<Integer>() {
     		@Override
     		protected Integer load() {
     			return daoService.getHorasSemanales(usuario);
     		}
     		
     	}; 
 
 		if(usuario == null){
 			this.setResponsePage(LoginPage.class);
 		}
 
 		this.listViewContainer = new WebMarkupContainer("listViewContainer");
 		this.listViewContainer.setOutputMarkupId(true);
 		
 		EntradaForm form = new EntradaForm("form"){
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, EntradaForm form) {
 				daoService.agregarEntrada(form.getModelObject(), usuario);
 				target.add(listViewContainer);
 				target.add(form);
 			}
 		};
 		
 		
 		
 		
 		entradasListView = new ListView<Entrada>("entradas", this.lstEntradasModel) {
             @Override
             protected void populateItem(ListItem<Entrada> item) {
             	Entrada entrada = item.getModel().getObject();   
             	if((item.getIndex() % 2) == 0){
             		item.add(new AttributeModifier("class","odd"));
             	}
             	item.add(new Label("fecha_entrada", new Model<Date>(entrada.getFecha())));
                 item.add(new Label("proyecto_entrada", entrada.getProyecto().getNombre()));
                 item.add(new Label("actividad_entrada", entrada.getActividad().getNombre()));
                 item.add(new Label("duracion_entrada", new Model<String>(entrada.getDuracion())));
                 item.add(new Label("ticketBZ_entrada", new Model<Integer>(entrada.getTicketBZ())));
 
                 item.add(new AjaxLink<Entrada>("deleteLink",new Model<Entrada>(entrada)) {
                     @Override
                     public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                         daoService.borrarEntrada(getModelObject());
                         ajaxRequestTarget.add(getPage().get("listViewContainer"));
                     }
 
                 });
                 
                 
                 PageParameters parametros = new PageParameters().add("id", entrada.getIdEntrada().getTime());
                 item.add(new BookmarkablePageLink<EditEntradasPage>("modifyLink",EditEntradasPage.class, parametros));
 
             }
         };
         
         
 		ArrayList<String> opcionesTiempo = new ArrayList<String>();
 		opcionesTiempo.add("Dia");
 		opcionesTiempo.add("Semana");
 		opcionesTiempo.add("Mes");
 		
 		
 		IModel<String> modelTiempo = new Model<String>();
 		
 		selectorTiempo = new DropDownChoice<String>("selectorTiempo", modelTiempo, opcionesTiempo);
 		
 		selectorTiempo.add(new AjaxFormComponentUpdatingBehavior("onchange"){
 			@Override
 			protected void onUpdate(AjaxRequestTarget target) {
 				
 				if (selectorTiempo.getConvertedInput().equals("Mes")) {
 					entradasListView.setModel(new LoadableDetachableModel<List<Entrada>>() {
 
 						@Override
 						protected List<Entrada> load() {
 							return daoService.getEntradasMensuales(AgregarEntradasPage.this.usuario);
 						}
 						
 					});
 				}
 				if (selectorTiempo.getConvertedInput().equals("Semana")) {
 					entradasListView.setModel(new LoadableDetachableModel<List<Entrada>>() {
 						
 						@Override
 						protected List<Entrada> load() {
 							return daoService.getEntradasSemanales(AgregarEntradasPage.this.usuario);
 						}
 						
 					});
 				}
 				if (selectorTiempo.getConvertedInput().equals("Dia")) {
 					entradasListView.setModel(new LoadableDetachableModel<List<Entrada>>() {
 						
 						@Override
 						protected List<Entrada> load() {
 							return daoService.getEntradasDia(AgregarEntradasPage.this.usuario);
 						}
 						
 					});
 				}
 				
 				
 				target.add(listViewContainer);
 				
 			}
 		});
         
         
 		Label horasAcumuladas = new Label("horasAcumuladas", this.horasSemanalesModel);
 
 		listViewContainer.add(selectorTiempo);
         listViewContainer.setOutputMarkupId(true);
 		listViewContainer.add(entradasListView);
 		listViewContainer.add(horasAcumuladas);
 		add(listViewContainer);
 		add(form);	
 	
 	}
 
 	public abstract class EntradaForm extends Form<Entrada> {
 		public IModel<Entrada> entradaModel = new CompoundPropertyModel<Entrada>(new Entrada());
 		public DropDownChoice<Actividad> comboActividad;
 		public DropDownChoice<Proyecto> comboProyecto; 	
 		public TextField<String> ticketExt;
 		public DropDownChoice<String> sistemaExterno;
 		public TextField<String> duracion;
 		public TextField<String> ticketBZ;
 		public TextField<Date> fecha;
 		
 		public EntradaForm(String id) {
 			super(id);
 			this.setDefaultModel(this.entradaModel);
 			
			
 			ArrayList<String> sistExt = new ArrayList<String>();
 			sistExt.add("Sistema de Incidencias de YPF");
 			sistExt.add("Sistema Geminis de YPF");
 			
 			
 			
 			this.comboProyecto = new DropDownChoice<Proyecto>("proyecto", daoService.getProyectos(),new IChoiceRenderer<Proyecto>() {
 				@Override
 				public Object getDisplayValue(Proyecto object) {
 					return object.getNombre();
 				}
 
 				@Override
 				public String getIdValue(Proyecto object, int index) {
 					return Integer.toString(object.getIdProyecto());
 				}
 				
 			});
 			this.comboProyecto.setOutputMarkupId(true);	
 			this.comboProyecto.setRequired(true);
 			this.comboProyecto.setLabel(Model.of("Proyecto"));
 			
 			this.comboProyecto.add(new AjaxFormComponentUpdatingBehavior("onchange"){
 				@Override
 				protected void onUpdate(AjaxRequestTarget target) {
 					EntradaForm.this.comboActividad.setChoices(daoService.getActividades(EntradaForm.this.comboProyecto.getModelObject()));
 					target.add(EntradaForm.this.comboActividad);
 				}
 			});
 			
 			mensajeProyecto = new Label("mensajeProyecto","Campo necesario");
 			mensajeProyecto.add(new AttributeModifier("style", new Model("display:none")));
 			mensajeProyecto.setOutputMarkupId(true);
 			
 			
 			this.comboActividad = new DropDownChoice<Actividad>("actividad",new ArrayList<Actividad>(),new IChoiceRenderer<Actividad>() {
 				@Override
 				public Object getDisplayValue(Actividad object) {
 					return object.getNombre();
 				}
 
 				@Override
 				public String getIdValue(Actividad object, int index) {
 					return Integer.toString(object.getIdActividad());
 				}
 				
 			});
 			this.comboActividad.setOutputMarkupId(true);
 			this.comboActividad.setRequired(true);
 			this.comboActividad.setLabel(Model.of("Actividad"));
 			
 			mensajeActividad = new Label("mensajeActividad","Campo Necesario");
 			mensajeActividad.add(new AttributeModifier("style", new Model("display:none")));
 			mensajeActividad.setOutputMarkupId(true);
 			
 			sistemaExterno = new DropDownChoice<String>("sistemaExterno", sistExt);
 			sistemaExterno.setLabel(Model.of("Sistema Externo"));
 			sistemaExterno.setOutputMarkupId(true);
 
 			
 			TextArea<String> nota = new TextArea<String>("nota");
 			
 			duracion = new TextField<String>("duracion");
 			duracion.setRequired(true);
 			duracion.setOutputMarkupId(true);
 			duracion.setLabel(Model.of("Duracion"));
 			duracion.add(new NumberCommaBehavior(duracion.getMarkupId()));
 			duracion.add(new DurationValidator());
 			
 			
 			ticketBZ = new TextField<String>("ticketBZ");
 			ticketBZ.setRequired(true);
 			ticketBZ.setOutputMarkupId(true);
 			ticketBZ.setLabel(Model.of("Ticket Bugzilla"));
 			ticketBZ.add(new OnlyNumberBehavior(ticketBZ.getMarkupId()));
 			
 			
 			ticketExt = new TextField<String>("ticketExterno");
 			ticketExt.setLabel(Model.of("ID Ticket Externo"));
 			ticketExt.setOutputMarkupId(true);
 			ticketExt.add(new PatternValidator("^[a-z0-9_-]{1,15}$"));
 			
 			
 			
 			
 			fecha = new TextField<Date>("fecha");
 			fecha.setRequired(true);
 			fecha.add(new DatePickerBehavior(fecha.getMarkupId()));
 			fecha.setOutputMarkupId(true);
 			fecha.setLabel(Model.of("Fecha"));
 			fecha.add(new NoInputBehavior());
 			
 			final FeedbackPanel feedBackPanel = new FeedbackPanel("feedBackPanel");
 			feedBackPanel.setOutputMarkupId(true);
 			
 			
 			AjaxButton submit = new AjaxButton("submit", this) {
 			
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 
 					EntradaForm.this.onSubmit(target, (EntradaForm)form);								
 					target.add(feedBackPanel);
 					target.add(listViewContainer);
 					EntradaForm.this.setModelObject(new Entrada());
 					
 					if (duracion.isValid()) {
 						duracion.add(new AttributeModifier("style", new Model("border-color:none")));
 					}else{
 						duracion.add(new AttributeModifier("style", new Model("border-style:solid; border-color:red;")));
 					}
 					
 					if (ticketExt.isValid()) {
 						ticketExt.add(new AttributeModifier("style", new Model("border-color:none")));
 					}else{
 						ticketExt.add(new AttributeModifier("style", new Model("border-style:solid; border-color:red;")));
 					}
 					
 					if (fecha.isValid()) {
 						fecha.add(new AttributeModifier("style", new Model("border-color:none")));
 					}else{
 						fecha.add(new AttributeModifier("style", new Model("border-style:solid; border-color:red;")));
 					}
 					
 					if (comboProyecto.isValid()) {
 						mensajeProyecto.add(new AttributeModifier("style", new Model("display:none")));
 					}else{
 					}
 					
 					if (comboActividad.isValid()) {
 						mensajeActividad.add(new AttributeModifier("style", new Model("display:none")));
 					}else{
 					}
 					
 					target.add(EntradaForm.this);
 				}
 
 				@Override
 				protected void onError(AjaxRequestTarget target, Form<?> form) {
 					
 					if (!duracion.isValid()) {
 						duracion.add(new AttributeModifier("style", new Model("border-style:solid; border-color:red;")));
 					}else{
 						duracion.add(new AttributeModifier("style", new Model("border-color:none")));
 					}
 					
 					if (!ticketExt.isValid()) {
 						ticketExt.add(new AttributeModifier("style", new Model("border-style:solid; border-color:red;")));
 					}else{
 						ticketExt.add(new AttributeModifier("style", new Model("border-color:none")));
 					}					
 					
 					if (!fecha.isValid()) {
 						fecha.add(new AttributeModifier("style", new Model("border-style:solid; border-color:red;")));
 					}else{
 						fecha.add(new AttributeModifier("style", new Model("border-color:none")));
 					}
 					
 					if (comboProyecto.isValid()) {
 						mensajeProyecto.add(new AttributeModifier("style", new Model("display:none")));
 					}else{
 						System.out.println("else");
						mensajeProyecto.add(new AttributeModifier("style", new Model("font-weight:bold;color:red")));
 					}
 					
 					if (comboActividad.isValid()) {
 						mensajeActividad.add(new AttributeModifier("style", new Model("display:none")));
 					}else{
						mensajeActividad.add(new AttributeModifier("style", new Model("font-weight:bold;color:red")));
 					}
 					
 					target.add(feedBackPanel);
 					target.add(fecha);
 					target.add(duracion);
 					target.add(ticketExt);
 					target.add(mensajeProyecto);
 					target.add(mensajeActividad);
 				}
 				
 			};
 
 			
 		
 
 			
 			
 			add(mensajeProyecto);
 			add(mensajeActividad);
 			add(comboProyecto);
 			add(comboActividad);
 			add(duracion);
 			add(fecha);
 			add(nota);
 			add(ticketBZ);
 			add(sistemaExterno);
 			add(ticketExt);
 			add(feedBackPanel);
 			this.add(new TicketExternoValidator(sistemaExterno ,ticketExt));
 			add(submit);
 			
 			this.setOutputMarkupId(true);
 
 		}
 
 		
 		protected abstract void onSubmit(AjaxRequestTarget target, EntradaForm form);
 
 	}	
 }
 
 
