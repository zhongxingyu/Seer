 package be.betty.gwtp.client.presenters;
 
 import java.util.ArrayList;
 
 import be.betty.gwtp.client.CardHandler;
 import be.betty.gwtp.client.CellDropControler;
 import be.betty.gwtp.client.Filter_kind;
 import be.betty.gwtp.client.Storage_access;
 import be.betty.gwtp.client.action.GetActivityStateAction;
 import be.betty.gwtp.client.action.GetActivityStateActionResult;
 import be.betty.gwtp.client.action.GetCards;
 import be.betty.gwtp.client.action.GetCardsResult;
 import be.betty.gwtp.client.action.SaveCardDropAction;
 import be.betty.gwtp.client.action.SaveCardDropActionResult;
 import be.betty.gwtp.client.event.BoardViewChangedEvent;
 import be.betty.gwtp.client.event.CardFilterEvent;
 import be.betty.gwtp.client.event.DropCardEvent;
 import be.betty.gwtp.client.event.CardFilterEvent.CardFilterHandler;
 import be.betty.gwtp.client.event.DropCardEvent.DropCardHandler;
 import be.betty.gwtp.client.event.ProjectListModifyEvent;
 import be.betty.gwtp.client.place.NameTokens;
 import be.betty.gwtp.shared.dto.ActivityState_dto;
 
 import com.allen_sauer.gwt.dnd.client.PickupDragController;
 import com.allen_sauer.gwt.dnd.client.drop.VerticalPanelDropController;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import com.gwtplatform.common.client.IndirectProvider;
 import com.gwtplatform.common.client.StandardProvider;
 import com.gwtplatform.dispatch.shared.DispatchAsync;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.ProxyPlace;
 import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
 
 public class MainPresenter extends
 		Presenter<MainPresenter.MyView, MainPresenter.MyProxy> {
 
 	public interface MyView extends View {
 		public Label getMainLabel();
 
 		public AbsolutePanel getDndPanel();
 
 		Label getContent();
 
 		void setContent(Label content);
 
 		VerticalPanel getCards_panel();
 
 		void constructFlex(PickupDragController cardDragController);
 
 		ListBox getComboInstance();
 
 		Label getCurrentInstance();
 
 		ListBox getCombo_viewChoice1();
 
 		ListBox getCombo_viewChoice2();
 	}
 
 	public static final Object SLOT_Card = new Object();
 	public static final Object SLOT_BOARD = new Object();
 	public static final Object SLOT_OPTION_SELECION = new Object();
 	
 	@Inject CardSelectionOptionPresenter cardSelectionOptionPresenter;
 	@Inject BoardPresenter boardPresenter;
 	
 	private IndirectProvider<SingleCardPresenter> cardFactory;
 	@Inject DispatchAsync dispatcher;
 
 	public static ArrayList<SingleCardPresenter> allCards;
 
 	@ProxyCodeSplit
 	@NameToken(NameTokens.main)
 	public interface MyProxy extends ProxyPlace<MainPresenter> {
 	}
 	
 	
 	private Storage stockStore;
 	private EventBus eventBus;
 	
 
 	@Inject
 	public MainPresenter(final EventBus eventBus, final MyView view,
 			final MyProxy proxy, final Provider<SingleCardPresenter> provider) {
 		super(eventBus, view, proxy);
 		cardFactory = new StandardProvider<SingleCardPresenter>(provider);
 		stockStore = Storage.getLocalStorageIfSupported();
 		this.eventBus = eventBus;
 		allCards = new ArrayList<SingleCardPresenter>();
 	}
 
 	@Override
 	protected void revealInParent() {
 		// RevealRootContentEvent.fire(this, this);
 		RevealContentEvent.fire(this, HeaderPresenter.SLOT_CONTENT, this);
 	}
 
 	private String project_num;
 	public static PickupDragController cardDragController;
 	public static VerticalPanelDropController cardDropPanel;
 	private CardFilterHandler filterHandler = new CardFilterHandler() {
 
 		@Override
 		public void onCardFilter(CardFilterEvent event) {
 
 			// TODO : attention, si on decide de faire 2 combobox, les id venan
 			// de la combox et
 			// du local storage ne seront plus les mêmes... et donc on pourra pas
 			// faire ainsi.
 
 			switch (event.getFilterType()) {
 			case TEACHER:
 				writeCardsFromSelector(Filter_kind.TEACHER,
 						Storage_access.getTeacher(event.getFilterObjId()));
 				break;
 			case GROUP:
 				writeCardsFromSelector(Filter_kind.GROUP,
 						Storage_access.getGroup(event.getFilterObjId()));
 			}
 		}
 	};
 	
 	private DropCardHandler dropCardHandler = new DropCardHandler() {
 		@Override public void onDropCard(DropCardEvent event) {
 			System.out.println("$$$$$ Catch event.. day="+event.getDay()+" and period= "+event.getPeriod()+" cardid="+event.getCardID());
 			int activity_bddId = Storage_access.getBddIdCard(Storage_access.getCard(event.getCardID()));
 			int projectInstance = Storage_access.getCurrentProjectInstanceBDDID();
 			System.out.println("Actual project instance= "+projectInstance);
 			
 			// "first", save to bdd (it's asynchronous)
 			dispatcher.execute(new SaveCardDropAction(event.getDay(), event.getPeriod(), activity_bddId, event.getRoom(), projectInstance),
 					new AsyncCallback<SaveCardDropActionResult>() {
 						@Override public void onFailure(Throwable arg0) {
 							System.out.println("save 'dropped card' failed !!");
 							System.out.println("tostring"+arg0);
 							System.out.println("get message:"+arg0.getMessage());
 
 						}
 
 						@Override public void onSuccess(SaveCardDropActionResult result) {
 							System.out.println("save card drop action sucess!!!");
 
 						}
 					});
 
 			// Then save in local Storage
 			if (event.getDay() != 0) {
 				//System.out.println("tiiiittllee"+allCards.get(event.getCardID()).getWidget().getTitle());
 				//System.out.println(allCards.size());
 				allCards.get(event.getCardID()).getWidget().addStyleName("cardPlaced");
 				Storage_access.setSlotCard(event.getCardID(), event.getDay(), event.getPeriod());
 				//TODO: faut aussi l'envoyer � la bdd, ou un truc du genre
 			}
 			else {
 				allCards.get(event.getCardID()).getWidget().addStyleName("card");
 				Storage_access.revoveFromSlot(event.getCardID());
 				// faut aussi l'envoyer � la bdd, ou un truc du genre
 			}
 		}
 	};
 	
 
 	@Override
 	public void prepareFromRequest(PlaceRequest request) {
 		super.prepareFromRequest(request);
 		project_num = request.getParameter("p", "-1");
 		// System.out.println("prepare from request: "+name);
 	}
 
 	@Override
 	protected void onBind() {
 		super.onBind();
 		
 		getView().getComboInstance().addChangeHandler(new ChangeHandler() {
 			@Override public void onChange(ChangeEvent arg0) {
 				int selectedIndex = getView().getComboInstance().getSelectedIndex();
 				
 				getView().getCurrentInstance().setText(
 						""+Storage_access.getInstanceLocalNum(selectedIndex)
 						);
 				
				Storage_access.setCurrentProjectInstanceBddId(selectedIndex);
 				
 				reDrowStatusCard();
 			}});
 		
 		getView().getCombo_viewChoice1().addChangeHandler(new ChangeHandler() {
 			@Override public void onChange(ChangeEvent arg0) {
 				printSecondComboBxView(getView().getCombo_viewChoice1().getSelectedIndex());
 				
 			}
 
 		});
 		
 		getView().getCombo_viewChoice2().addChangeHandler(new ChangeHandler() {
 			@Override public void onChange(ChangeEvent arg0) {
 				reDrowStatusCard();
 			}
 
 		});
 		
 		set_dnd();
 		registerHandler(getEventBus().addHandler(CardFilterEvent.getType(),
 				filterHandler));
 		
 		registerHandler(getEventBus().addHandler(
 				DropCardEvent.getType(), dropCardHandler));
 
 	}
 
 	private void set_dnd() {
 
 		// create a DragController to manage drag-n-drop actions
 		// note: This creates an implicit DropController for the boundary panel
 		cardDragController = new PickupDragController(RootPanel.get(), false);
 		cardDragController.addDragHandler(new CardHandler());
 		cardDropPanel = new VerticalPanelDropController(getView().getCards_panel());
 
 		// TODO v�rifier si il y a des lag en utilisant l'application sur le
 		// serveur
 		// mettre en commentaire ces deux lignes
 
 		// VerticalPanelDropController dropController = new
 		// VerticalPanelDropController(getView().getDrop_cards_panel());
 		// cardDragController.registerDropController(dropController);
 
 		// dragController.makeDraggable(getView().getDndImage());
 
 		// AbsolutePositionDropController sp = new
 		// AbsolutePositionDropController(
 		// getView().getDropPanel());
 		// IndexedDropController dropController = new
 		// IndexedDropController(getView().getDropPanel());
 
 		// dragController.registerDropController(sp);
 		// dragController.makeDraggable(getView().getMainLabel());
 		// dragController.makeDraggable(getView().getHtml_panel());
 		// for (CardPresenter c : allCards)
 		// dragController.makeDraggable(c.getView().getWholePanel());
 
 	}
 
 	@Override
 	protected void onReset() {
 		super.onReset();
 
 		String login = "";
 		String sess = "";
 		if (stockStore != null) {
 			sess = stockStore.getItem("session_id");
 			login = stockStore.getItem("login");
 		}
 		if (sess == null) {
 			getView().getMainLabel().setText("Please (re)log first");
 			return;
 		}
 
 		getView().getMainLabel().setText(
 				"Welcome " + login + " *****  Projet num " + project_num);
 
 	//	final boolean[] DONT_REPEAT_YOURSELF = {true};  //marche po :(
 		GetCards action = new GetCards(project_num);
 		dispatcher.execute(action, new AsyncCallback<GetCardsResult>() {
 
 			@Override
 			public void onFailure(Throwable arg0) {
 				// TODO Auto-generated method stub
 				// arg0.printStackTrace();
 				System.err.println("***failure:" + arg0);
 
 			}
 
 			@Override
 			public void onSuccess(GetCardsResult result) {
 //				if (DONT_REPEAT_YOURSELF[0])
 //					DONT_REPEAT_YOURSELF[0]=false;
 //				else return;
 
 				Storage_access.populateStorage(project_num,result);
 				
 				
 				
 				print_da_page();
 				// getView().getContent().setText(result.getActivities().toString());
 				//Storage_access.printStorage();
 
 			}
 
 		});
 
 	}
 
 	/**
 	 * PRE: local storage Must (already) be filled with the right info 
 	 * POST: The page is drawed on screen
 	 * 
 	 * Print the cards, the board, with the information in the local storage
 	 */
 	private void print_da_page() {
 		System.out.println("**** Hell yeah, print da page");
 		// des Assert ici pour verifier qq truc sur le local storage serait p-e
 		// bien..
 
 		setInSlot(SLOT_OPTION_SELECION, cardSelectionOptionPresenter);
 		cardSelectionOptionPresenter.init();
 		
 		setInSlot(SLOT_BOARD, boardPresenter);
 
 		cardDragController.registerDropController(cardDropPanel);
 		
 		setStaticFirstComboView();
 		writeInstancePanel();
 		Storage_access.setCurrentProjectInstanceBddId(0);
 		//Storage_access.setCurrentProjectInstance(Storage_access.getInstanceBddId(Storage_access.getCurrentProjectInstance()));
 		
 		reDrowStatusCard();
 		
 		this.boardPresenter.redrawBoard(0,0); //TODO n'enregistrerons nous pas la "vue par default"? ou la derniere ?
 		
 		writeCardWidgetsFirstTime();
 		//getView().constructFlex(cardDragController);
 	
 		
 		
 		//CellDropControler dropController = new CellDropControler(simplePanel);
 	   //	cardDragController.registerDropController(dropController);
 
 	}
 
 	/**
 	 * This method will construct the first combobox involved in selecting a view (for the card board)
 	 * It's "static" data, so one should change this with care, as it's used by others (it's handler)
 	 */
 	private void setStaticFirstComboView() {
 		getView().getCombo_viewChoice1().clear();
 		getView().getCombo_viewChoice1().addItem("Professeur");
 		getView().getCombo_viewChoice1().addItem("Local");
 		getView().getCombo_viewChoice1().addItem("Classe");
 	
 	}
 	
 
 	private void printSecondComboBxView(int selectedIndex) {
 		assert selectedIndex >=0 && selectedIndex<=2;
 		getView().getCombo_viewChoice2().clear();
 		
 		switch (selectedIndex) {
 		case 0: 
 			for (int i=0; i< Storage_access.getNumberOfTeacher(); i++){
 				getView().getCombo_viewChoice2().addItem(Storage_access.getTeacher(i));
 			}
 			break;
 		case 1: 
 			
 			break;
 		case 2: 
 			for (int i=0; i< Storage_access.getNumberOfGroup(); i++){
 				getView().getCombo_viewChoice2().addItem(Storage_access.getGroup(i)	);
 			}
 			break;
 		default: return;
 		}
 	}
 
 	private void writeInstancePanel() {
 		
 		//System.out.println("***mm**  number of instance="+Storage_access.getNumberOfInstance());
 		//System.out.println("***mm**  first Instance="+Storage_access.getInstance(0));
 		getView().getComboInstance().clear();
 		for (int i = 0; i<Storage_access.getNumberOfInstance(); i++)
 			getView().getComboInstance().addItem(""+Storage_access.getInstanceLocalNum(i)+": "+Storage_access.getInstanceDesc(i));
 		//Storage_access.printStorage();
 		
 	}
 
 	/**
 	 *  PRE: local storage must be full-filled
 	 */
 	private void writeCardWidgetsFirstTime() {
 		setInSlot(SLOT_Card, null);
 		allCards.clear();
 		for (int i = 0; i < Storage_access.getNumberOfCard(); i++) {
 			final int myI = i;
 			cardFactory.get(new AsyncCallback<SingleCardPresenter>() {
 
 				@Override
 				public void onSuccess (SingleCardPresenter result) {
 					addToSlot(SLOT_Card, result);
 					result.init(myI);
 					cardDragController.makeDraggable(result.getWidget(), result.getView().getCourse());
 					cardDragController.makeDraggable(result.getWidget(), result.getView().getTeacher());
 					allCards.add(result);
 
 				}
 
 				@Override
 				public void onFailure(Throwable caught) {
 					// TODO Auto-generated method stub
 
 				}
 			});
 		}
 
 	}
 
 	/**
 	 * 
 	 * le principe est d'appliquer "un filtre" au cartons,
 	 * pour en faire "disparaitre" ou "reaparaitre"
 	 * Faut voire si c'est le plus performant/bug free
 	 * ou sinon, on fait comme la methode d'apres..
 	 * 
 	 * @param filter_kind
 	 * @param toFilter
 	 */
 	private void writeCardsFromSelector(Filter_kind filter_kind, String toFilter) {
 
 		for (SingleCardPresenter c : allCards) {
 			//System.out.println(c.getView().getHeader());
 			 if(c.getKindString(filter_kind).equals(toFilter))
 				 c.getWidget().setVisible(false);
 		}
 
 	}
 	
 	/**
 	 *  ( devrait pe bien etre fusionne avec le precedent, ms fonctionne pas de la mm maniere, 
 	 *  et surtt, celle ci a besoin d'une requete server.. ms bon, on peu s'arranger)
 	 *  
 	 *  Le principe est d'appeller cette methode a chaque fois que le
 	 *  statu des cartes est modifie (changement de vues, (de mode?), d'instance, (de filtres ?) )
 	 *  
 	 *  Pour le moment elle n'est (et ne doit etre) utilise lors d'un changemnt d'instances
 	 *  
 	 */
 	private void reDrowStatusCard() {
 		int currentInstance= Storage_access.getCurrentProjectInstanceBDDID() ;
 		
 		dispatcher.execute(new GetActivityStateAction(currentInstance), new AsyncCallback<GetActivityStateActionResult>() {
 
 	
 
 			@Override public void onFailure(Throwable arg0) {
 				System.out.println("!!!!!!!!!!!!!!!!!!!!!****  failed to get activities status");
 
 			}
 
 			@Override public void onSuccess(GetActivityStateActionResult result) {
 				for (int i=0; i< Storage_access.getNumberOfCard(); i++) {
 					String card = Storage_access.getCard(i);
 					ActivityState_dto a = result.getActivitiesState().get(""+Storage_access.getBddIdCard(card));
 					if (a == null) 
 						Storage_access.revoveFromSlot(i);	
 					else 
 						Storage_access.setSlotCard(i, a.getDay(), a.getPeriod());	
 					
 				}
 				eventBus.fireEvent( 
 						new BoardViewChangedEvent(getView().getCombo_viewChoice1().getSelectedIndex(),
 												  getView().getCombo_viewChoice2().getSelectedIndex())
 						);
 				//Storage_access.printStorage();
 			}
 
 			});
 
 		
 	}
 
 	
 
 }
