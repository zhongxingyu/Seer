 package monbulk.MethodBuilder.client.presenter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 
 
 
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.ui.HasWidgets;
 
 import monbulk.MethodBuilder.client.PreviewWindow;
 import monbulk.MethodBuilder.client.PreviewWindow.SupportedFormats;
 import monbulk.MethodBuilder.client.event.ChangeWindowEvent;
 import monbulk.MethodBuilder.client.event.ChangeWindowEventHandler;
 import monbulk.MethodBuilder.client.model.MethodCompleteModel;
 import monbulk.MethodBuilder.client.view.AppletStateNavigation;
 import monbulk.MethodBuilder.client.view.MethodDetailsView;
 import monbulk.MethodBuilder.client.view.MethodForm;
 import monbulk.MethodBuilder.client.view.StepForm;
 import monbulk.MethodBuilder.client.view.SubjectPropertiesForm;
 
 import monbulk.client.desktop.Desktop;
 import monbulk.client.event.WindowEvent;
 
 import monbulk.shared.Architecture.IPresenter.FormPresenter;
 import monbulk.shared.Architecture.IView;
 import monbulk.shared.Architecture.IView.IDockView;
 import monbulk.shared.Architecture.IView.IDraggable;
 import monbulk.shared.Architecture.IView.IFormView;
 import monbulk.shared.Events.DragEvent;
 import monbulk.shared.Form.FormBuilder;
 import monbulk.shared.Model.IPojo;
 import monbulk.shared.Model.pojo.pojoMetaData;
 import monbulk.shared.Model.pojo.pojoMethod;
 import monbulk.shared.Model.pojo.pojoStepDetails;
 import monbulk.shared.Model.pojo.pojoStudy;
 import monbulk.shared.Model.pojo.pojoSubjectProperties;
 import monbulk.shared.util.HtmlFormatter;
 import monbulk.shared.util.MonbulkEnums;
 import monbulk.shared.util.MonbulkEnums.viewTypes;
 import monbulk.shared.widgets.Window.OkCancelWindow.OkCancelHandler;
 import monbulk.shared.widgets.Window.appletWindow;
 
 import monbulk.MethodBuilder.shared.IMethodsView;
 import monbulk.MethodBuilder.shared.iMBModel;
 
 ///I think we can change this just to a view state with a main model that can be queried on each state
 
 public class MethodCreatorPresenter implements FormPresenter,OkCancelHandler{
 
 	private int countLoads;
 	//private IView vwMethodCreator;
 	//private IDockView vwDockPanel;
 	//private ArrayList<PresenterState> AllStates;
 	private MethodCreatorStates CurrentState;
 	private IDockView ImplementedDockView;
 	private IMethodsView ImplementedMethodView;
 	private IView NavigationView;
 	public enum MethodCreatorStates{
 		METHOD_DETAILS,SUBJECT_PROPERTIES,STEP_DETAILS,COMPLETE, INIT
 	};
 	public enum SupportedMethodCommands{
 		NEW,EDIT,NEXT,PREV,ADD_STEP,REMOVE_STEP,PREVIEW,SAVE,PUBLISH,DELETE_METHOD,CANCEL,EXPAND
 	}
 	
 	public final HashMap<String,MethodCreatorStates> stateSelector;
 	public final HashMap<MethodCreatorStates,IFormView> _AllStates;
 	private final MethodCompleteModel mainModel; 
 	//private MethodForm methodForm;
 	//private MethodModel MethodModel;
 	private final HandlerManager eventBus; 
 	//private SubjectPropertiesModel SubjectModel;
 	private appletWindow tclBox;
 	public MethodCreatorPresenter(HandlerManager evtBus)
 	{
 		stateSelector = new HashMap<String,MethodCreatorStates>();
 		_AllStates = new HashMap<MethodCreatorStates,IFormView>();
 		this.eventBus = evtBus;
 		mainModel = new MethodCompleteModel();
 		mainModel.setPresenter(this);
 		Construct("");
 	
 	}
 	/**
 	 * Generalised constructor
 	 * @param ID
 	 */
 	public void Construct(String ID)
 	{	
 		//METHOD_DETAILS,SUBJECT_PROPERTIES,STEP_DETAILS,COMPLETE, INIT
 		stateSelector.put("METHOD_DETAILS", MethodCreatorStates.METHOD_DETAILS);
 		stateSelector.put("SUBJECT_PROPERTIES", MethodCreatorStates.SUBJECT_PROPERTIES);
 		stateSelector.put("STEP_DETAILS", MethodCreatorStates.STEP_DETAILS);
 		stateSelector.put("COMPLETE", MethodCreatorStates.COMPLETE);
 		stateSelector.put("INIT", MethodCreatorStates.INIT);
 		this.ImplementedMethodView = new MethodDetailsView();		
 		
 		//Not supported
 		//this.ImplementedDockView = new metaDataListIntegrated();		
 		//this.ImplementedDockView.setPresenter(this);
 		countLoads = 0;
 		//AllStates = new ArrayList<PresenterState>();
 		//tclBox = new appletWindow("TCL Viewer", "TCLViewer", this.eventBus, null);
 		
 		SetStates(ID);
 		bind();
 	}
 	public MethodCreatorPresenter(HandlerManager evtBus, String ID)
 	{
 		//Load Method From ID;
 		stateSelector = new HashMap<String,MethodCreatorStates>();
 		_AllStates = new HashMap<MethodCreatorStates,IFormView>();
 		this.eventBus = evtBus;
 		mainModel = new MethodCompleteModel(ID,this);
 		Construct(ID);
 	}
 	@Override
 	public ArrayList<String> GetDocks() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	private void SetStates(String ID)
 	{
 			try
 			{
 			//GWT.log(mainModel.getFormData().toString());
 			MethodForm tmpForm = new MethodForm(this);
 			SubjectPropertiesForm tmpForm2 = new SubjectPropertiesForm(this);
 			StepForm tmpForm3 = new StepForm(this);
 			
 			this._AllStates.put(MethodCreatorStates.METHOD_DETAILS, tmpForm);
 			this._AllStates.put(MethodCreatorStates.SUBJECT_PROPERTIES, tmpForm2);
 			this._AllStates.put(MethodCreatorStates.STEP_DETAILS, tmpForm3);
 			
 			
 			}
 			catch(Exception ex)
 			{
 				GWT.log("Error Occurs @ MethodCreatorPresentor.SetStates" + ex.getMessage());
 			}
 	//
 		
 		
 	}
 	/**
 	 * 
 	 * @param newState
 	 */
 	private void ChangeState(MethodCreatorStates newState)
 	{
 			if(this.CurrentState != null)
 			{
 				try
 				{			
 					IFormView tmpView = this._AllStates.get(newState);
 					
 					if(tmpView!=null)
 					{	
 						this.ImplementedMethodView.clearChild();
 						GWT.log("State change 1");
 						if(!this.mainModel.isLoaded())
 						{
 								GWT.log("State change 2");
 								FormBuilder tmpForm = this.mainModel.getFormData(newState.toString());
 								if(tmpForm!=null)
 								{
 									GWT.log("State change 3");			
 									//this.ImplementedMethodView.setData(tmpForm);
 									tmpView.LoadForm(tmpForm);
 									
 								}
 								else
 								{
 									GWT.log("@MethodCreatorPresenter.ChangeState: Could not find a form for:" + newState);
 								}
 							
 							//countLoads++;
 							GWT.log("Loaded:" + newState);
 						}
 						
 						//GWT.log("Pre-SetChild" + this.CurrentState);
 						this.ImplementedMethodView.setChild(tmpView.asWidget());
 						//GWT.log("Post-SetChild" + newState);
 						this.ImplementedMethodView.setPresenter(this);
 						this.CurrentState=newState;
 								//GWT.log("4");
 								//this.ImplementedDockView.setTabData(tmpState.getModel().getMetaDataCategories(), "MetaData");
 								//GWT.log(tmpState.presenterState.toString());
 						GWT.log("cHILD wIDGET:" + tmpView.getClass());
 						GWT.log("cHILD wIDGET:" + tmpView.asWidget().toString());
 						return;
 					}
 							
 							
 						
 					}
 					catch(Exception ex)
 					{
 						GWT.log("Error Occurs @ MethodCreatorPresenter.ChangeState" + ex.getMessage());
 					}
 			
 		}
 		else
 		{
 			
 			this.CurrentState = MethodCreatorStates.INIT;
 			this.ChangeState(MethodCreatorStates.METHOD_DETAILS);
 		}
 		
 	}
 	
 	///TODO We have to bind custom events for Changing local state
 	private void bind()
 	{
 		 eventBus.addHandler(ChangeWindowEvent.TYPE, 
 		    		new ChangeWindowEventHandler(){
 		    			public void onChangeWindow(ChangeWindowEvent event){
 		    				doChangeWindow(event.getId(),event.getChange());
 		    			}   	
 		    		});
 		return;
 	}
 	private void doChangeWindow(String WindowName,String Change)
 	{
 		this.tclBox.hide();
 	}
 	/**
 	 * go(HasWidgets Container) - loads a single panel
 	 */
 	@Override
 	public void go(HasWidgets container) {
 		// TODO Auto-generated method stub
 		container.add(this.ImplementedMethodView.asWidget());
 	}
 
 	/**
 	 * Loads two panels
 	 * NB: Dock Panel not currently supported
 	 * This is called by a parent class which loads panels into a layout 
 	 */
 	@Override
 	public void go(HasWidgets bodyContainer, HasWidgets dockContainer) {
 		// TODO Auto-generated method stub
 		
 		//Dock container not supported
 		//dockContainer.add(this.ImplementedDockView.asWidget());
 		bodyContainer.add(this.ImplementedMethodView.asWidget());
 
 		//Load initial state
 		this.CurrentState = MethodCreatorStates.INIT;
 		this.ChangeState(MethodCreatorStates.METHOD_DETAILS);
 		
 	}
 	
 
 	@Override
 	public void FormComplete(String FormName,String Command) {
 		IFormView currentView =this._AllStates.get(CurrentState);
 		iMBModel tmpModel = this.mainModel;
 		if(Command=="Next")
 		{
 		//	com.google.gwt.user.client.Window.alert("Unable to Save Form.");
 			GWT.log("Next and CurrentState is:" + CurrentState + "New State is: " + FormName);
 			
 			//FIX: Validation fails for some formFields
 			//String Validation = tmpModel.ValidateForm(FormName);
 			
 			
 			//FIX: For SOme reason the StepDetails form is loading too many items - need to check
 			/*if(!Validation.equals(""))
 			{
 				com.google.gwt.user.client.Window.alert("Unable to Save Form." + Validation);
 				return;
 			}*/
 			
 			if(FormName==pojoMethod.FormName)
 			{
 				//TODO Must ensure next, prev and complete states are submitted to any view/form
 				
 				this.ChangeState(MethodCreatorStates.SUBJECT_PROPERTIES);
 		
 				this.ImplementedMethodView.SetMenuIndex(MethodCreatorStates.SUBJECT_PROPERTIES.toString());
 				GWT.log("complete Load");
 			}
 			else if(FormName==pojoSubjectProperties.FormName)
 			{
 				IFormView tmpView = this._AllStates.get(MethodCreatorStates.STEP_DETAILS);
 				this.ImplementedMethodView.clearChild();
 				pojoStepDetails tmpPojo1 = this.mainModel.getFirstStep();
 				if(tmpPojo1!=null)
 				{
 					FormBuilder tmpForm  = tmpPojo1.getFormStructure();
 					if(tmpForm!=null)
 					{
 						GWT.log("State change 3");			
 						//this.ImplementedMethodView.setData(tmpForm);
 						tmpView.LoadForm(tmpForm);
 						this.ImplementedMethodView.setData(tmpForm);
 						this.ImplementedMethodView.setChild(tmpView.asWidget());
 					}
 				}
 				
 				
 				//this.ImplementedMethodView.setData(this.mainModel.getFormData(this.mainModel.get);
 			
 			}
 			else if(FormName.contains(pojoStepDetails.FormName))
 			{
 				//com.google.gwt.user.client.Window.alert("New Step");
 				IFormView tmpView = this._AllStates.get(MethodCreatorStates.STEP_DETAILS);
 				this.ImplementedMethodView.clearChild();
 				pojoStepDetails tmpPojo = this.mainModel.getNextStep(FormName); 
 				tmpView.ClearForm();
 				if(tmpPojo!=null)
 				{
 					FormBuilder tmpForm  = tmpPojo.getFormStructure();
 					if(tmpForm!=null)
 					{
 						GWT.log("State change 3");			
 						//this.ImplementedMethodView.setData(tmpForm);
 						tmpView.LoadForm(tmpForm);
 						this.ImplementedMethodView.setData(tmpForm);
 						
 						this.ImplementedMethodView.setChild(tmpView.asWidget());
 					}
 				}
 				//this.ImplementedMethodView.clearChild();
 		
 			}
 		}
 		else if(Command=="Prev")
 		{
 			if(FormName==pojoSubjectProperties.FormName)
 			{
 				//DONE Must ensure next, prev and complete states are submitted to any view/form
 				this.ChangeState(MethodCreatorStates.METHOD_DETAILS);
 				this.ImplementedMethodView.SetMenuIndex(MethodCreatorStates.METHOD_DETAILS.toString());
 			}	
 			else if(FormName.contains(pojoStepDetails.FormName))
 			{
 				//DONE Must ensure next, prev and complete states are submitted to any view/form
 				this.ChangeState(MethodCreatorStates.SUBJECT_PROPERTIES);
 				this.ImplementedMethodView.SetMenuIndex(MethodCreatorStates.SUBJECT_PROPERTIES.toString());
 			}	
 		}
 		else if(Command=="AddAnother")
 		{
 			//Current Form Reset
 			//revisedModel.AddNewStep();
 			currentView.ClearForm();
 			//Current Model - Set New Step
 		}
 		else if(Command=="Edit")
 		{
 			if(FormName.contains(pojoStepDetails.FormName))
 			{
 				IFormView tmpView = this._AllStates.get(MethodCreatorStates.STEP_DETAILS);
 				this.ImplementedMethodView.clearChild();
 				pojoStepDetails tmpPojo = this.mainModel.getStep(FormName); 
 				tmpView.ClearForm();
 				if(tmpPojo!=null)
 				{
 					FormBuilder tmpForm  = tmpPojo.getFormStructure();
 					//com.google.gwt.user.client.Window.alert(tmpPojo.getFormStructure().getFormDetails().toString() + tmpPojo.getFormStructure().getFormDetails().size());
 					//tmpPojo.getFormStructure().getFormDetails()
 					if(tmpForm!=null)
 					{
 						GWT.log("State change 3");			
 						//this.ImplementedMethodView.setData(tmpForm);
 						tmpView.LoadForm(tmpForm);
 						if(tmpView.getViewType()==MonbulkEnums.viewTypes.DRAGDROP)
 						{
 							IDraggable vw = (IDraggable)tmpView;
 							
 							vw.BuildList(tmpPojo.getMetaData(pojoStepDetails.SubjectMetaDataField), pojoStepDetails.SubjectMetaDataField);
 							vw.BuildList(tmpPojo.getMetaData(pojoStudy.STUDY_METADATA), pojoStudy.STUDY_METADATA);
 						}
 						this.ImplementedMethodView.setData(tmpForm);						
 						this.ImplementedMethodView.setChild(tmpView.asWidget());
 					}
 				}
 				
 				
 			}
 			else
 			{
 				GWT.log("Editing" + this.stateSelector.get(FormName));
 				FormBuilder tmpForm = this.mainModel.getFormData(FormName);
 				this.ImplementedMethodView.clearChild();
 				IFormView tmpView = this._AllStates.get(this.stateSelector.get(FormName));
 				//tmpView.LoadForm(tmpForm);				
 				//GWT.log("Pre-SetChild" + this.CurrentState);
 				this.ImplementedMethodView.setChild(tmpView.asWidget());
 				//GWT.log("Post-SetChild" + newState);
 				this.ImplementedMethodView.setPresenter(this);
 				this.CurrentState=this.stateSelector.get(FormName);
 			}
 				
 		}
 		else if(Command=="Delete")
 		{
 			//StepModel revisedModel =(StepModel)tmpModel;
 			//revisedModel.RemoveStepForFormName(FormName);
 			MethodCompleteModel delModel = this.mainModel;
 			delModel.removeStep(FormName);
 			this.ImplementedMethodView.RemoveListItem(FormName);
 			this.ImplementedMethodView.clearChild();
 			this.ChangeState(MethodCreatorStates.SUBJECT_PROPERTIES);
 		}
 		else if(Command=="Save")
 		{
			
 		}
 		else if(Command=="Publish")
 		{
 			StringBuilder strTCL = new StringBuilder();
			strTCL = strTCL.append(HtmlFormatter.GetHTMLUtilityScript("TCL"));
			
			strTCL = strTCL.append(this.mainModel.getStringRpresentation("TCL"));
			
 			//Should just launch a Window with a Pojo
 			Desktop d = Desktop.get();
 			final PreviewWindow m = (PreviewWindow )d.getWindow("MethodPreviewWindow");
 			m.loadPreview(strTCL, SupportedFormats.TCL);
 			//d.
 			//_newWin.loadPreview(strTCL, SupportedFormats.TCL);
 		    //_newWin.setOkCancelHandler(this);
 		    d.show(m, true);
 		    //_newWin.
 				//WindowManager tmpManager = WindowManager.manager();
 			//	tmpManager.add(_win);
 				//tmpManager.setDefaultParentWidget(defaultPar)
 				//_win.show();
 				
 	
 		}
 		else if(Command=="Cancel")
 		{
 			// HACK: This shouldn't be specifying "MethodBuilder" here.  It should
 			// know what window it's in, or the type name should be a static that
 			// all windows implement.
 			eventBus.fireEvent(new WindowEvent("MethodBuilder", WindowEvent.EventType.CloseWindow));
 		}
 	
 		}
 	
 	@Override
 	public void FireDragEvent(DragEvent e) {
 		
 		
 		
 		
 		
 		//RelatedModel.
 		
 		String DataObjectType = e.getName();
 		String DataFieldName = e.getId();
 		int Action = e.getIndex();
 		//0 = Add
 		//1= remove
 		IPojo relatedPOJO = e.getPojo();
 		
 			if(Action ==0)
 			{
 				if(DataObjectType==pojoMetaData.FormName)
 				{
 					this.mainModel.loadMetaData(DataFieldName, (pojoMetaData) relatedPOJO);
 					///this.ImplementedMethodView.setData(anyBuilder)
 				}
 			}
 			if(Action==1)
 			{
 				if(DataObjectType==pojoMetaData.FormName)
 				{
 					this.mainModel.removeMetaData(DataFieldName, (pojoMetaData) relatedPOJO);
 					//this.mainModel.loadMetaData(DataFieldName, relatedPOJO);
 				}
 			}
 		
 		
 		//this.mainModel.
 		
 	}
 	
 	@Override
 	public void go(HasWidgets bodyContainer, HasWidgets dockContainer,HasWidgets navContainer) {
 			this.NavigationView = new AppletStateNavigation();
 			NavigationView.setPresenter(this);
 			navContainer.clear();
 			navContainer.add(this.NavigationView.asWidget());
 			this.go(bodyContainer, dockContainer);
 		
 	}
 	@Override
 	public void ModelUpdate(String ServiceName) {
 		
 	//	Window.alert("Help");//
 		if(ServiceName=="GetMethod")
 		{
 			
 			Iterator<Entry<MethodCreatorStates,IFormView>> i = this._AllStates.entrySet().iterator();
 			
 			while(i.hasNext())
 			{
 				Entry<MethodCreatorStates, IFormView> tmpView = i.next();
 				String formName = tmpView.getKey().toString();
 				if(tmpView!=null)
 				{
 					
 					if(tmpView.getKey().toString().contains(pojoStepDetails.FormName))
 					{
 						int j=0;
 						while(j<this.mainModel.getStepCount())
 						{
 							String tmpformName = tmpView.getKey().toString() + j;
 							
 							FormBuilder tmpBuilder = this.mainModel.getFormData(tmpformName);
 							if(tmpBuilder!=null)
 							{
 								this.ImplementedMethodView.setData(tmpBuilder);
 							}
 							j++;
 						}
 					}
 					else
 					{
 						FormBuilder tmpBuilder = this.mainModel.getFormData(formName);
 						if(tmpView!=null)
 						{
 							
 							this.ImplementedMethodView.setData(tmpBuilder);
 							tmpView.getValue().LoadForm(tmpBuilder);
 							if(tmpView.getValue().getViewType()==viewTypes.DRAGDROP)
 							{
 								IDraggable vw = (IDraggable)tmpView.getValue();
 								
 								vw.BuildList(this.mainModel.getMetaDataList(pojoSubjectProperties.SubjectMetaDataField), pojoSubjectProperties.SubjectMetaDataField);
 							}
 							
 						}
 					}
 				}
 			}
 			//this.getCurrentPresenterState().getView().LoadForm(this.mainModel.getFormData(this.getCurrentPresenterState().presenterState.toString()));
 			GWT.log("We get here...MU");
 		}
 	}
 	@Override
 	public String UpdateValue(FormBuilder someFormData) {
 		
 		this.mainModel.Update(someFormData);
 		FormBuilder tmpBuilder = this.mainModel.getFormData(this.CurrentState.toString());
 		//GWT.log(someFormDa)
 		IFormView tmpView = this._AllStates.get(this.CurrentState);
 		
 		if(tmpView!=null)
 		{
 			GWT.log("Not null");
 			this.ImplementedMethodView.setData(someFormData);
 			//tmpView.LoadForm(tmpBuilder);
 			
 			//FIX: Not updating model
 			
 		}
 		else
 		{
 			return "Form not found";
 		}
 		return "";
 		
 	}
 	@Override
 	public void onOkCancelClicked(Event eventType) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 
 }
 
