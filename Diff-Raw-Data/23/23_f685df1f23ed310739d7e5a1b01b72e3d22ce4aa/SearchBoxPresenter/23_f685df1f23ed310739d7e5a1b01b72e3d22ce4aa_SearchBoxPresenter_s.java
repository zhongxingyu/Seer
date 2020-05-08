 package net.thesocialos.client.presenter;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import net.thesocialos.client.CacheLayer;
 import net.thesocialos.client.desktop.DesktopUnit;
 import net.thesocialos.client.desktop.DesktopUnit.TypeUnit;
 import net.thesocialos.client.view.PopUpInfoContact;
 import net.thesocialos.shared.model.Group;
 import net.thesocialos.shared.model.User;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ContextMenuEvent;
 import com.google.gwt.event.dom.client.ContextMenuHandler;
 import com.google.gwt.event.dom.client.DomEvent;
 import com.google.gwt.event.dom.client.DomEvent.Type;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 
 public class SearchBoxPresenter extends DesktopUnit {
 	
 	SingleSelectionModel<User> selectionModel;
 	ListDataProvider<User> dataProvider;
 	/*
 	 * Los modelos de la cajas de seleccion de los usuarios
 	 */
 	ProvidesKey<User> KEY_USERS_PROVIDER;
 	List<User> usersList = new ArrayList<User>();
 	
 	/*
 	 * Los modelos de la cajas de seleccion de los grupos
 	 */
 	ProvidesKey<Object> KEY_GROUPS_PROVIDER;
 	List<Object> groupsList = new ArrayList<Object>();
 	
 	Display display;
 	
 	public interface Display{
 		
 		Widget asWidget();
 		
 		Label getLabelFriends();
 		
 		Label getLabelGroups();
 		
 		Label getLabelInfo();
 		
 		Label getLabelInvite();
 		
 		Image getAvatarIMG();
 		
 		CellList<User> getComponentsList();
 		
 		VerticalPanel getSearchBoxPanel();
 		
 		void setComponentsList(CellList<User> cellList);
 	}
 	
 	public SearchBoxPresenter(Display display){
 		programID = "002";
 		typeUnit = TypeUnit.INFO;
 		this.display = display;
 		KEY_USERS_PROVIDER = new ProvidesKey<User>() {
 			public Object getKey(User item) {
 				return item == null ? null :  item.getEmail();
 			}
 		};
 		selectionModel = new SingleSelectionModel<User>(KEY_USERS_PROVIDER);
 		handlers();
 	}
 	/*
 	 * Eventos de la interfaz
 	 */
 	private void handlers(){
 		
 		display.getLabelFriends().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				//display.setComponentsList(new CellList<User>(usersCell()));
 				
 			}
 		});
 		
 		display.getLabelGroups().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		display.getLabelInvite().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				User contactUser;
 				if ((contactUser= selectionModel.getSelectedObject())!=null){
 					addPetitionContact(contactUser);
 				}
 				
 			}
 		});
 		display.getLabelInfo().addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				User contactUser;
 				if ((contactUser= selectionModel.getSelectedObject())!=null){
 					PopUpInfoContact contactInfoPopup  = new PopUpInfoContact(contactUser.getEmail(), contactUser.getName(), contactUser.getLastName());
 					contactInfoPopup.setGlassEnabled(true);
 					contactInfoPopup.center();
 					contactInfoPopup.show();
 				}
 				
 				
 			}
 		});
 	}
 	private void addPetitionContact(User contactUser){
 		CacheLayer.ContactCalls.addPetitionContact(contactUser, new AsyncCallback<Boolean>() {
 
 			@Override
 			public void onFailure(Throwable caught) {
 				System.out.println(caught.getMessage());
 				
 			}
 
 			@Override
 			public void onSuccess(Boolean result) {
 				System.out.println(result);
 				
 			}
 		});
 	}
 	/**
 	 * Obtiene todos los usuarios del servidor
 	 */
 	private void getUsers(){
 		
		display.getComponentsList().setSelectionModel(selectionModel);
		dataProvider = new ListDataProvider<User>(usersList);
 		
 		CacheLayer.ContactCalls.getUsers(true, new AsyncCallback<Map<String,User>>() {
 			
 			@Override
 			public void onSuccess(Map<String, User> result) {
 				
 				//display.setComponentsList(new CellList<User>(usersCell()));
				usersList.clear();
 				usersList.addAll(result.values());
		
				dataProvider.addDataDisplay(display.getComponentsList());
 				
 				
 			}
 			
 			@Override
 			public void onFailure(Throwable caught) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			public void onSelectionChange(SelectionChangeEvent event) {
 			    System.out.println(selectionModel.getSelectedObject().getName());
 			    System.out.println(selectionModel.getSelectedObject().getLastName());
 			}
 		});
 		display.getComponentsList().addDomHandler(new ContextMenuHandler() {
 			
 			@Override
 			public void onContextMenu(ContextMenuEvent event) {
 				System.out.println("boton derecho pulsado");
 				
 				DomEvent.fireNativeEvent(Document.get().createClickEvent(0, event.getNativeEvent().getScreenX(),
 						event.getNativeEvent().getScreenY(), event.getNativeEvent().getClientX(), 
 						event.getNativeEvent().getClientY(), false, false, false, false), display.getComponentsList());
 				
 			}
 		}, ContextMenuEvent.getType());
 		
 	}
 	/**
 	 * Obtiene todos los grupos del servidor
 	 */
 	private void getGroups(){
 		
 	}
 
 
 	@Override
 	public int getZposition() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	@Override
 	public void close(AbsolutePanel absolutePanel) {
 		absolutePanel.remove(display.asWidget());
 		
 	}
 	@Override
 	public void open(AbsolutePanel absolutePanel) {
 		absolutePanel.add(display.asWidget(),x,y);
 		display.asWidget().setVisible(true);
 		getUsers();
 		
 		
 	}
 	@Override
 	public void minimize() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void maximize() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void isMinimized() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void getID() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void toFront() {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void toZPosition(int position) {
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void setPosition(int x, int y) {
 		System.out.println(display.asWidget().getOffsetHeight());
 		
 		this.x = x;
 		this.y = y;
 		
 	}
 	@Override
 	public int getXPosition() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 	@Override
 	public int getYPosition() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 }
