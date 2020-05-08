 package com.odea;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.extensions.markup.html.form.palette.Palette;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.IChoiceRenderer;
 import org.apache.wicket.markup.html.form.ListMultipleChoice;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.ResourceModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import com.odea.behavior.focusOnLoad.FocusOnLoadBehavior;
 import com.odea.domain.Actividad;
 import com.odea.domain.Proyecto;
 import com.odea.services.DAOService;
 
 public class EditarProyectosPage extends BasePage {
 
 	@SpringBean
 	public transient DAOService daoService;
 	private IModel<Proyecto> proyectoModel;
 	public ListMultipleChoice<Actividad> originals;
 	public ListMultipleChoice<Actividad> destinations;
 	
 	public EditarProyectosPage(){
 		
 		this.proyectoModel = new CompoundPropertyModel<Proyecto>(
 				new LoadableDetachableModel<Proyecto>() {
 					@Override
 					protected Proyecto load() {
 						return new Proyecto(0, "",true);
 					}
 				});
 
 		this.preparePage();
 
 	}
 
 	public EditarProyectosPage(final PageParameters parameters) {
 
 		Subject subject = SecurityUtils.getSubject();
 		if (!subject.isAuthenticated()) {
 			this.redirectToInterceptPage(new LoginPage());
 		}
 
 		this.proyectoModel = new CompoundPropertyModel<Proyecto>(
 				new LoadableDetachableModel<Proyecto>() {
 					@Override
 					protected Proyecto load() {
 						return new Proyecto(parameters.get("proyectoId").toInt(),
 								parameters.get("proyectoNombre").toString(),
 								parameters.get("proyectoHabilitado").toBoolean());
 					}
 				});
 
 		this.preparePage();
 
 	}
 
 	private void preparePage() {
 		add(new BookmarkablePageLink<ProyectosPage>("link", ProyectosPage.class));
 		add(new FeedbackPanel("feedback"));
 
 		EditForm form = new EditForm("form", proyectoModel) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, EditForm form) {
 				setResponsePage(ProyectosPage.class);
 			}
 
 		};
 
 		form.setOutputMarkupId(true);
 
 		add(form);
 	}
 
 	public abstract class EditForm extends Form<Proyecto> {
 
 		public List<Actividad> selectedOriginals;
 		public List<Actividad> selectedDestinations;
 		public List<Actividad> listDestination;
 		public List<Actividad> listOriginals;
 //		public DualMultipleChoice<Actividad> dualMultiple;
 
 		public EditForm(String id, IModel<Proyecto> proyecto) {
 
 			super(id, proyecto);
 
 
			RequiredTextField<String> nombre = new RequiredTextField<String>("nombreLogin");
 			nombre.add(new FocusOnLoadBehavior());
 			
 			
 	        LoadableDetachableModel todosModel = new LoadableDetachableModel() {
 				@Override
 				protected Object load() {
 					return daoService.getActividades();
 				}							
 	        };
 	        
 	        LoadableDetachableModel seleccionadosModel = new LoadableDetachableModel() {
 				@Override
 				protected Object load() {
 					return EditForm.this.obtenerListaDestino();
 				}							
 	        };
 	        final Palette<Actividad> pal = new Palette<Actividad>("dual", seleccionadosModel, todosModel, new IChoiceRenderer<Actividad>() {
 	        	
 	        	
 	        	
 				@Override
 				public Object getDisplayValue(Actividad object) {
 					return object.getNombre();
 				}
 
 				@Override
 				public String getIdValue(Actividad object, int index) {
 					return Integer.toString(object.getIdActividad());
 				}
 			}, 8, false){
 	        	
 	        	@Override
 	        	protected Component newAvailableHeader(final String componentId)
 	        	{
 	        		return new Label(componentId, new ResourceModel("palette.available", "Actividades Disponibles"));
 	        	}
 	        	
 	        	@Override
 	        	protected Component newSelectedHeader(final String componentId)
 	        	{
 	        		return new Label(componentId, new ResourceModel("palette.selected", "Actividades Elegidas"));
 	        	}
 	        };
 	        						
 			
 			AjaxButton submit = new AjaxButton("submit") {
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form form) {
 					EditForm.this.onSubmit(target, (EditForm) form);
 					daoService.agregarProyecto(EditForm.this.getModelObject(),
 							(Collection<Actividad>) pal.getDefaultModelObject());
 				}
 			};
 
 			
 			add(nombre);
 			add(pal);
 			add(submit);
 		}
 
 
 		private List<Actividad> obtenerListaDestino() {
 			if (proyectoModel.getObject().getIdProyecto() > 0) {
 				return daoService.getActividades(proyectoModel.getObject());
 			}
 			else{
 				return new ArrayList<Actividad>();
 			}
 		}
 
 						
 		private List<Actividad> obtenerListaOrigen() {
 			if (proyectoModel.getObject().getIdProyecto() > 0) {
 				return daoService.actividadesOrigen(proyectoModel.getObject());
 			} else {
 				return daoService.getActividades();
 			}
 		}		
 		
 		
 		protected abstract void onSubmit(AjaxRequestTarget target, EditForm form);
 
 	}
 }
