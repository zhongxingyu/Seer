 package eu.ist.fears.client.admin;
 
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import eu.ist.fears.client.Fears;
 import eu.ist.fears.client.common.communication.Communication;
 import eu.ist.fears.client.common.exceptions.ExceptionsTreatment;
 import eu.ist.fears.client.common.views.ViewProject;
 import eu.ist.fears.client.interfaceweb.ProjectWidget;
 
 public class ListProjectsWidget extends Composite{
 
 	private Communication _com;
 	private VerticalPanel _projPanel;
 	private TextBox _newProjectName; 
 	private TextArea _newProjectDescription; 
 	private Button _createProjectButton;
 
 	public ListProjectsWidget(){
 
 		_com= new Communication("service");
 		_projPanel = new VerticalPanel();
 		_projPanel.setStyleName("projectsList");
 		_newProjectName = new TextBox();
 		_newProjectDescription= new TextArea();
 		_createProjectButton = new Button("Adicionar Projecto");
 
 		Fears.getPath().setFears();		
 
 		init();
 		initWidget(_projPanel);
 		
 
 	}
 
 	private void init(){
 		_projPanel.clear();
 	}
 
 	private void displayCreateProject(){
 		_projPanel.add(new HTML("<br><br><h2>Criar Projecto</h2>"));
 		_projPanel.add(new Label("Nome do Projecto:"));
 		_newProjectName.setText("");
 		_newProjectDescription.setText("");
 		_projPanel.add(_newProjectName);
 		_projPanel.add(new Label("Descricao do Projecto:"));
 		_newProjectDescription.setVisibleLines(7);
 		_newProjectDescription.setCharacterWidth(40);
 		_projPanel.add(_newProjectDescription);
 		_createProjectButton = new Button("Adicionar Projecto");
 		_projPanel.add(_createProjectButton);
 
 		_createProjectButton.addClickListener(new ClickListener(){
 			public void onClick(Widget sender) {
 				_com.addProject(_newProjectName.getText(),
 						_newProjectDescription.getText(), Cookies.getCookie("fears"), updateCB);
 			}
 		}); 
 	}
 
 	public void update(){
 		_com.getProjects(Cookies.getCookie("fears"), getProjectsCB);	
 	}
 	
 	protected void updateProjects(ViewProject[] projects) {
 
 		init();
 
 		if(projects==null || projects.length ==0){
 			_projPanel.add(new Label("Nao ha Projectos"));
 		}
 
 
 		for(int i=0;i< projects.length;i++){
 			_projPanel.add(new ProjectWidgetAdmin(projects[i]));
 			_projPanel.add(new HTML("<br>")); //Line Break
 		}
 		
 		displayCreateProject();
 
 	}
 	
 	public class ProjectWidgetAdmin extends ProjectWidget{
 
 		Button _removeButton;
 		Label _author;
 		DisclosurePanel _removePanel;
 
 		
 		ProjectWidgetAdmin(ViewProject p){
 			super(p);
 			_admin = new HorizontalPanel();
 			_project.add(_admin);
 			_admin.setStyleName("removeMeta");
 			_author = new Label(p.getAuthor());
 			_removeButton = new Button("Sim");
 			_removeButton.addClickListener(new RemoveButton());
 			
 			_admin.add(_author);
 			_admin.add(new HTML("&nbsp;|&nbsp;"));
 			_admin.add(new Hyperlink("Editar","Editar"));
 			_admin.add(new HTML("&nbsp;|&nbsp;"));
 			
 			
 			_removePanel = new DisclosurePanel();
 			_removePanel.setHeader(new HTML("Remover"));
 			
 			HorizontalPanel removeExpanded = new HorizontalPanel();
 			removeExpanded.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
 			removeExpanded.setStyleName("removeConfirm");
 			if(p.getNFeatures()==0){
 				removeExpanded.add(new Label("Deseja mesmo apagar este projecto?"));
 			removeExpanded.add(_removeButton);
 			Button _noButton = new Button("N&atilde;o");
 			_noButton.addClickListener(new CloseButton());
 			removeExpanded.add(_noButton);
 			}else {
 				removeExpanded.add(new HTML("N&atilde;o pode apagar projectos com sugest&otilde;es."));
 			}
 			
 			_removePanel.setContent(removeExpanded);
 			_admin.add(_removePanel);
 		}
 
 
 		private class RemoveButton implements ClickListener{
 
 			public void onClick(Widget sender) {
				_com.deleteProject(_name.getText(), Cookies.getCookie("fears"), updateCB);
 			}
 
 		}
 		
 		private class CloseButton implements ClickListener{
 			public void onClick(Widget sender) {
 				_removePanel.setOpen(false);
 			}
 
 		}
 
 	}
 	
 	AsyncCallback updateCB = new ExceptionsTreatment() {
 		public void onSuccess(Object result){ 
 			update();	
 		}		
 	};
 	
 	AsyncCallback getProjectsCB = new ExceptionsTreatment() {
 		public void onSuccess(Object result){ 
 			updateProjects((ViewProject[]) result);
 		}		
 	};
 
 }
