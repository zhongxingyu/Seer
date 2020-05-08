 /**
  * 
  */
 package pl.dmcs.whatsupdoc.client.providers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Panel;
 
 import pl.dmcs.whatsupdoc.client.ContentManager;
 import pl.dmcs.whatsupdoc.client.services.AuthenticationService;
 import pl.dmcs.whatsupdoc.client.services.AuthenticationServiceAsync;
 
 
 /**
  * 29-10-2012
  * @author Jakub Jeleński, jjelenski90@gmail.com
  * 
  * 
  */
 public class DoctorMenuProvider extends MenuProvider {
 
 	private Map<Bar, List<Button> > subMenus;
 	private Bar currentBar;
 	//private ArrayList<String> CSS;
 	
 	/**
 	 * @param manager - ContentManager type
 	 */
 	public DoctorMenuProvider(ContentManager manager) {
 		super(manager);
 		
 		this.currentBar = Bar.HOME;
 		this.subMenus = new HashMap<Bar, List<Button>>();
 		
 		//*********************************************************** Create empty sub-menus ********************************************
 		
 		this.subMenus.put(Bar.HOME, new ArrayList<Button>());
 		
 		//*********************************************************** Create buttons for first menu *************************************
 		
 		/*Button button = new Button("Strona Główna");
 		firstLevelButton = button;
 		button.setStyleName(CSS.get(0));
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				currentBar = Bar.HOME;
 				setFirstLvlCSS((Button) event.getSource());
 				getManager().drawContent();
 			}
 		});
 		hFirstPanel.add(button);*/
 		
 		Button button = new Button("Moje konto");
 		button.setStyleName(CSS.get(1));
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				currentBar = Bar.MY_ACCOUNT;
 				Button current =(Button) event.getSource();
 				setFirstLvlCSS(current);
 				setFirstLevelBreadcrumb("Moje konto", new BodyProvider(getManager()),current);
 				getManager().drawContent();
 			}
 		});
 		hFirstPanel.add(button);
 		
 		button = new Button("Pacjenci");
 		button.setStyleName(CSS.get(1));
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				currentBar = Bar.PATIENTS;
 				Button current =(Button) event.getSource();
 				setFirstLvlCSS(current);
 				setFirstLevelBreadcrumb("Pacjenci", new BodyProvider(getManager()),current);
 				getManager().drawContent();
 			}
 		});
 		hFirstPanel.add(button);
 		
 		button = new Button("Statystyki");
 		button.setStyleName(CSS.get(1));;
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				currentBar = Bar.STATISTICS;
 				Button current =(Button) event.getSource();
 				setFirstLvlCSS(current);
 				setFirstLevelBreadcrumb("Statystyki", new BodyProvider(getManager()),current);
 				getManager().drawContent();
 			}
 		});
 		hFirstPanel.add(button);
 		
 		button = new Button("Wyloguj");
 		button.setStyleName(CSS.get(1));
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				
 				//Button current =(Button) event.getSource();
 				//setFirstLvlCSS(null);
 				//setFirstLevelBreadcrumb("Statystyki", new BodyProvider(getManager()),current);
 				final AuthenticationServiceAsync auth = GWT.create(AuthenticationService.class);
 				auth.logOut(new AsyncCallback<Void>() {
 					
 					@Override
 					public void onSuccess(Void result) {
 						getManager().getBreadcrumb().clearAll();
 						
 						BodyProvider b = new LoginProvider(getManager());
						getManager().getBreadcrumb().addField(false, "Strona logowania", b);
 						MenuProvider mp = new MenuProvider(getManager());
 						getManager().setBody(b);
 						getManager().setMenu(mp);
 						getManager().drawContent();
 						
 					}
 					
 					@Override
 					public void onFailure(Throwable caught) {
 						// TODO Auto-generated method stub
 						
 					}
 				});
 				
 			}
 		});
 		hFirstPanel.add(button);
 		
 		//******************************************************* Create buttons for My Account sub-menu *******************************
 		
 		ArrayList<Button> list = new ArrayList<Button>(2);
 		
 		button = new Button("Zmien haslo");
 		button.setStyleName(CSS.get(1));
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				Button current =(Button) event.getSource();
 				setSecondLvlCSS(current);
 				setSecondLevelBreadcrumb("Zmien haslo", new BodyProvider(getManager()), current);
 				getManager().drawContent();
 				
 			}
 		});
 		list.add(button);
 		
 		button = new Button("Zmien dane osobowe");
 		button.setStyleName(CSS.get(1));
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				Button current =(Button) event.getSource();
 				setSecondLvlCSS(current);
 				setSecondLevelBreadcrumb("Zmien dane", new BodyProvider(getManager()), current);
 				getManager().drawContent();
 				
 			}
 		});
 		list.add(button);
 		
 		subMenus.put(Bar.MY_ACCOUNT, list);
 		
 		//******************************************************** Create buttons for Patients sub-menu ****************************************
 		
 		list = new ArrayList<Button>(2);
 		
 		button = new Button("Wyszukiwarka");
 		button.setStyleName(CSS.get(1));
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				Button current =(Button) event.getSource();
 				setSecondLvlCSS(current);
 				BodyProvider b = new SearchProvider(getManager());
 				getManager().setBody(b);
 				setSecondLevelBreadcrumb("Wyszukiwarka", b, current);
 				getManager().drawContent();
 			}
 		});
 		list.add(button);
 		
 		button = new Button("Moi pacjenci");
 		button.setStyleName(CSS.get(1));
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				Button current =(Button) event.getSource();
 				setSecondLvlCSS(current);
 				setSecondLevelBreadcrumb("Moi pacjenci", new BodyProvider(getManager()), current);
 				getManager().drawContent();
 				
 			}
 		});
 		list.add(button);
 		
 		subMenus.put(Bar.PATIENTS, list);
 		
 		//******************************************************* Create buttons for Statistics sub-menu *************************************
 		
 		list = new ArrayList<Button>(2);
 		
 		button = new Button("Skuteczność leków");
 		button.setStyleName(CSS.get(1));
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				/*Button current =(Button) event.getSource();
 				setSecondLvlCSS(current);
 				setSecondLevelBreadcrumb("Skutecznosc lekow", new DrugStatisticProvider(getManager()), current);
 				getManager().drawContent();*/
 				
 				Button current =(Button) event.getSource();
 				setSecondLvlCSS(current);
 				BodyProvider b = new DrugStatisticProvider(getManager());
 				getManager().setBody(b);
 				setSecondLevelBreadcrumb("Skutecznosc lekow", b, current);
 				getManager().drawContent();
 				
 			}
 		});
 		list.add(button);
 		
 		button = new Button("Moja skuteczność");
 		button.setStyleName(CSS.get(1));
 		button.addClickHandler(new ClickHandler() {
 			
 			@Override
 			public void onClick(ClickEvent event) {
 				Button current =(Button) event.getSource();
 				setSecondLvlCSS(current);
 				setSecondLevelBreadcrumb("Moja skutecznosc", new BodyProvider(getManager()), current);
 				getManager().drawContent();
 				
 			}
 		});
 		list.add(button);
 		
 		subMenus.put(Bar.STATISTICS, list);
 	}
 	
 	/* (non-Javadoc)
 	 * @see pl.dmcs.whatsupdoc.client.providers.MenuProvider#getSecondLevel()
 	 */
 	@Override
 	public Panel getSecondLevel() {
 		hSecondPanel.clear();
 		for(Button button : subMenus.get(currentBar)){
 			hSecondPanel.add(button);
 		}
 		return hSecondPanel;
 
 	}
 
 
 
 	/**
 	 * 29-10-2012 - first menu bars
 	 * @author Jakub Jeleński, jjelenski90@gmail.com
 	 * 
 	 * 
 	 */
 	private enum Bar{
 		HOME, PATIENTS, MY_ACCOUNT, STATISTICS
 	}
 }
