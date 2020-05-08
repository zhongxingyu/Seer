 package monbulk.MethodBuilder.client.view;
 
 /*Java Util Imports */
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 
 /*GWT Miscellaneous Imports */
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.event.shared.HandlerManager;
 
 /*GWT Binder Imports */
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 
 /*GWT User Imports */
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HasWidgets;
 import com.google.gwt.user.client.ui.Hidden;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 /*Monbulk Imports */
 import monbulk.MethodBuilder.client.event.MenuChangeEvent;
 import monbulk.shared.Architecture.IPresenter;
 import monbulk.shared.Architecture.ISearchController;
 import monbulk.shared.Events.DragEvent;
 import monbulk.shared.Model.IPojo;
 import monbulk.shared.Model.pojo.pojoMethod;
 import monbulk.shared.Model.pojo.pojoMethodComplete;
 import monbulk.shared.Services.MethodService;
 import monbulk.shared.Services.ServiceRegistry;
 import monbulk.shared.util.MonbulkEnums;
 import monbulk.shared.view.iMenuWidget;
 
 /**
  * This Class instantiates the MethodList Service and converts the corresponding 
  * list of POJO data classes (pojoMethod) into stacks in a stack panel.
  * NB: Each Stack has a MethodMenuItem widget which allows for Editing and Cloning of Methods
  * NB2: A DragEvent is used to alert a presenter that data has been selected
  * 
  * <p>This class is a Member of the MethodBuilder package which in included in the Monbulk project
  *  </p>
  *  
  *  <pThis class implements Interfaces for iMenuWidget and MethodServiceHandler which are both
  * 	found in the Monbulk Shared package 
  * </p>
  * 
  *  @author Andrew Glenn
  *  @version 183
  *  @since 183
  *  @see MethodMenuItem
  *  @see monbulk.shared.Model.pojo.pojoMethod
  *  @see monbulk.shared.Services.MethodService
  *  @see monbulk.shared.view.iMenuWidget;
  *  @see monbulk.shared.Architecture.IPresenter
  *  @category View
  *  
  */
 public class MethodList extends Composite implements iMenuWidget, MethodService.MethodServiceHandler,ISearchController {
 
 	private static MethodListUiBinder uiBinder = GWT
 			.create(MethodListUiBinder.class);
 
 	interface MethodListUiBinder extends UiBinder<Widget, MethodList> {
 	}
 	
 	private final HandlerManager eventBus;
 	private String ActiveClassName;
 	private String PassiveClassName;
 	private HashMap<String,pojoMethod> methodList;
 	
 	@UiField FlexTable _MenuStack;
 	@UiField ScrollPanel _pnlScroll; 
 	@UiField PushButton _Newbutton;
 	@UiField PushButton _Refresh;
 	@UiField HorizontalPanel _searchPanel;
 	 
 	private IPresenter _presenter;
 	/**
 	 * Contsructor for MethodList 
 	 * @param eBus	requires the singleton eventBus from GWT.event.shared
 	 * @throws ServiceNotFoundException		If the service has not been registered
 	 */
 	public MethodList(HandlerManager eBus) {
 		
 		initWidget(uiBinder.createAndBindUi(this));
 		this.eventBus = eBus;
 		this._pnlScroll.setAlwaysShowScrollBars(true);
 		
 		methodList = new HashMap<String,pojoMethod>();
 		try
 		{
 			MethodService service = (MethodService)ServiceRegistry.getService(MonbulkEnums.ServiceNames.Methods);
 			service.getMethodList(this);
 		}
 		catch (ServiceRegistry.ServiceNotFoundException e)
 		{
 			GWT.log("Couldn't find Method service");
 		}
 		_Newbutton.setText("Add New Method");
 	}
 	/**
 	 * In case you want to set the StylenNames for the menu - you can construct as follows
 	 * 
 	 * @param tmpEvent
 	 * @param menuClassName
 	 * @param activeClassName
 	 * @param pClassName
 	 * @deprecated We no longer use the Menu class but a stack instead
 	 */
 	public MethodList(HandlerManager tmpEvent, String menuClassName, String activeClassName, String pClassName)
 	{
 		initWidget(uiBinder.createAndBindUi(this));
 		this.eventBus = tmpEvent;
 		//this._MethodList.setStyleName(menuClassName);
 		this.ActiveClassName = activeClassName;
 		this.PassiveClassName = pClassName;
 		try
 		{
 			MethodService service = (MethodService)ServiceRegistry.getService(MonbulkEnums.ServiceNames.Methods);
 			service.getMethodList(this);
 		}
 		catch (ServiceRegistry.ServiceNotFoundException e)
 		{
 			GWT.log("Couldn't find metadata service");
 		}
 		//this._MethodList.setSize("137px", "400px");
 		
 		
 	}
 	public void refreshList()
 	{
 		
 		try
 		{
 			MethodService service = (MethodService)ServiceRegistry.getService(MonbulkEnums.ServiceNames.Methods);
 			this._searchPanel.clear();
 			this._MenuStack.clear();
 			this._MenuStack.removeAllRows();
 			service.getMethodList(this);
 		}
 		catch (ServiceRegistry.ServiceNotFoundException e)
 		{
 			GWT.log("Couldn't find Method service");
 		}
 	}
 	/**
 	 * If we Click "Add New" We fire a drag Event in the presenter with no attached data 
 	 * 
 	 * @param e If you add a new button
 	 */
 	@UiHandler("_Newbutton")
 	void onClick(ClickEvent e) {
 		//FIXChange to MenuChangeEvent("NewMethod")
 		eventBus.fireEvent(new DragEvent("NewMethod","NewMethod",0,null));
 	}
 	@UiHandler("_Refresh")
 	void onClick1(ClickEvent e) {
 		//FIXChange to MenuChangeEvent("NewMethod")
 		eventBus.fireEvent(new MenuChangeEvent("Refresh",""));
 	}
 	
 	public void setText(String text) {
 		_Newbutton.setText(text);
 	}
 
 	public String getText() {
 		return _Newbutton.getText();
 	}
 	@Override
 	public void Filter(String Name)
 	{
 		this.BuildList(Name);
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see monbulk.shared.view.iMenuWidget#getBaseWidget()
 	 */
 	@Override
 	public HasWidgets getBaseWidget() {
 		// TODO Auto-generated method stub
 		return this._MenuStack;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see monbulk.shared.view.iMenuWidget#setActiveMenu(java.lang.String)
 	 * 
 	 * 
 	 */
 	@Override
 	public final void setActiveMenu(String activeItem) {
 		int count = this._MenuStack.getRowCount();
 		int i=0;
 //		GWT.log("SetActiveMenu Called" + activeItem + count);
 		while(i<count)
 		{
 			Hidden tmpItem = (Hidden)this._MenuStack.getWidget(i, 1);
 ///			GWT.log("ItemFound:" + tmpItem.getText() + activeItem + tmpItem.getText().length() + activeItem.length());
 			GWT.log("Item collected is: " + tmpItem.getValue());
 			if(tmpItem!=null)
 			{
 				if(tmpItem.getValue().contains(activeItem)&& tmpItem.getValue().length()==activeItem.length())
 				{
 					_MenuStack.getFlexCellFormatter().getElement(i, 0).setAttribute("style", "font-weight:bold;color:#345484;");
 					//_MenuStack.getFlexCellFormatter().getElement(i, 1).setAttribute("style", "padding-left:5px;background-color:#345484;");
 				//d	_MenuStack.getFlexCellFormatter().getElement(i, 2).setAttribute("style", "background-color:#345484;");
 					GWT.log("ItemFound:" + activeItem);
 				}
 				else
 				{
 					GWT.log("Item reset is " + tmpItem.getValue());
 					_MenuStack.getFlexCellFormatter().getElement(i, 0).setAttribute("style", "font-weight:normal;");
 					//_MenuStack.getFlexCellFormatter().getElement(i, 1).setAttribute("style", "padding-left:5px;");
 					//_MenuStack.getFlexCellFormatter().getElement(i, 2).setAttribute("style", "");	
 				}
 			}
 			//tmpItem.setActive(activeItem);
 			i++;
 		}
 		
 		
 	}
 	private void BuildList(String Filter)
 	{
 		
 			Iterator<Entry<String,pojoMethod>> it = this.methodList.entrySet().iterator();
 			
 			int i=0;
 			this._MenuStack.clear();
 			this._MenuStack.removeAllRows();
 			
 			//this._MenuStack.setHeight(height)
 			//We need to set Height of the parent panel dynamically based on the number of items
 			//We need to add a hidden field with a unique id
 			while(it.hasNext())
 			{
 				//Use of final to allow access to it in the clickHandlers
 				final int index = i;  //This keeps track of which item we are looking at
 				Entry<String,pojoMethod> tmpEntry = it.next();
 				final pojoMethod tmpMethod = tmpEntry.getValue();
 				//
 				String comp1 =tmpMethod.getMethodName().toLowerCase();
 				String comp2 = Filter.toLowerCase();
 				if(comp1.contains(comp2))
 				{
 					final Label titleLabel = new Label();
 					titleLabel.setText(tmpMethod.getMethodName());
 					titleLabel.addStyleName("menuMethodName");
 					final int _i = i;
 					//_edit.setStyleName("btnEditMethod");
 					titleLabel.addClickHandler(new ClickHandler()
 					{
 	
 						@Override
 						public void onClick(ClickEvent event) {
 							
 							//set selected style/should shuffle as well
							setActiveMenu(tmpMethod.getMethodID());
 							eventBus.fireEvent(new DragEvent(titleLabel.getText(),"EditMethod",index,(IPojo)tmpMethod));
 							
 						}
 						
 					});
 					
 					//this._MenuStack.setWidget(i,0,tmpItem.asWidget());
 					this._MenuStack.setWidget(i,0,titleLabel);
 					//this._MenuStack.setWidget(i,1,_edit);
 					Hidden tmpHidden = new Hidden();
 					tmpHidden.setValue(tmpMethod.getMethodID());
 					this._MenuStack.setWidget(i,1,tmpHidden);
 					_MenuStack.getFlexCellFormatter().getElement(i, 0).setAttribute("style", "border-right:Solid 1px #ccc;");
 					//_MenuStack.getFlexCellFormatter().getElement(i, 1).setAttribute("style", "padding-left:5px;");
 					i++;
 				}
 			}
 			
 			_MenuStack.setCellPadding(0);
 			_MenuStack.setCellSpacing(0);
 			int newHeight = _MenuStack.getRowCount() * 30;
 			this._MenuStack.setHeight(newHeight + "px");
 		
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see monbulk.shared.Services.MethodService.MethodServiceHandler#onReadMethodList(java.util.ArrayList)
 	 */
 	@Override
 	public void onReadMethodList(ArrayList<pojoMethod> arrMethods) {
 		try
 		{
 			Iterator<pojoMethod> it = arrMethods.iterator();
 			SearchWidget _SearchWidget = new SearchWidget(this);
 			this._searchPanel.add(_SearchWidget.getHeaderWidget());
 			int i=0;
 			
 			while(it.hasNext())
 			{
 				//Use of final to allow access to it in the clickHandlers
 				final int index = i;  //This keeps track of which item we are looking at
 				final pojoMethod tmpMethod = it.next();
 				this.methodList.put(tmpMethod.getMethodID(), tmpMethod);
 				
 				final Label titleLabel = new Label();
 				titleLabel.setText(tmpMethod.getMethodName());
 				titleLabel.addStyleName("menuMethodName");
 				//titleLabel.setWidth("150px");
 				
 			//	PushButton _clone = new PushButton();
 			//	_clone.setStyleName("btnCloneMethod");
 			//	_clone.addClickHandler(new ClickHandler()
 			//	{
 
 			//		@Override
 			//		public void onClick(ClickEvent event) {
 			//			setActiveMenu(titleLabel.getText());
 			//			eventBus.fireEvent(new DragEvent(titleLabel.getText(),"CloneMethod",index,(IPojo)tmpMethod));
 			//			
 			//		}
 			//		
 			//	});
 				
 				///PushButton _edit = new PushButton();
 				//_edit.setStyleName("btnEditMethod");
 				titleLabel.addClickHandler(new ClickHandler()
 				{
 
 					@Override
 					public void onClick(ClickEvent event) {
 						
 						//set selected style/should shuffle as well
 						setActiveMenu(tmpMethod.getMethodID());
 						eventBus.fireEvent(new DragEvent(titleLabel.getText(),"EditMethod",index,(IPojo)tmpMethod));
 						
 					}
 					
 				});
 				//this._MenuStack.setWidget(i,0,tmpItem.asWidget());
 				this._MenuStack.setWidget(i,0,titleLabel);
 				
 				//this._MenuStack.setWidget(i,1,_edit);
 				Hidden tmpHidden = new Hidden();
 				tmpHidden.setValue(tmpMethod.getMethodID());
 				this._MenuStack.setWidget(i,1,tmpHidden);
 				if(tmpMethod.getMethodName().length()>38)
 				{
 					_MenuStack.getFlexCellFormatter().setStyleName(i, 0, "long");	
 				}
 				_MenuStack.getFlexCellFormatter().getElement(i, 0).setAttribute("style", "");
 				//_MenuStack.getFlexCellFormatter().getElement(i, 1).setAttribute("style", "padding-left:5px;");
 				i++;
 			}
 			
 			_MenuStack.setCellPadding(0);
 			_MenuStack.setCellSpacing(0);
 		}
 		catch(Exception ex)
 		{
 			GWT.log("Error occurs @ MethodList.onReadMethodList" + ex.getMessage() + ex.getStackTrace().toString());
 		}
 		
 	}
 	@Override
 	public void onReadMethod(pojoMethodComplete method) {
 		return;
 		
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see monbulk.shared.view.iMenuWidget#setPresenter(monbulk.shared.Architecture.IPresenter)
 	 */
 	@Override
 	public void setPresenter(IPresenter tmpPresenter) {
 		this._presenter = tmpPresenter;
 		
 	}
 	@Override
 	public void populateItems(List<String> tmpArray) {
 		return;
 		
 	}
 	@Override
 	public void onMethodInUse(Boolean isInUse) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
