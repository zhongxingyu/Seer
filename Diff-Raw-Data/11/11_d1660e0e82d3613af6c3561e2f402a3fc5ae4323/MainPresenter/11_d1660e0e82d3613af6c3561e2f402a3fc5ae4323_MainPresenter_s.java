 package be.betty.gwtp.client.presenters;
 
 import java.util.ArrayList;
 
 import be.betty.gwtp.client.Betty_gwtp;
 import be.betty.gwtp.client.Storage_access;
 import be.betty.gwtp.client.action.GetCards;
 import be.betty.gwtp.client.action.GetCardsResult;
 import be.betty.gwtp.client.model.Project;
 import be.betty.gwtp.client.place.NameTokens;
 
 import com.allen_sauer.gwt.dnd.client.PickupDragController;
 import com.allen_sauer.gwt.dnd.client.drop.AbsolutePositionDropController;
 import com.allen_sauer.gwt.dnd.client.drop.HorizontalPanelDropController;
 import com.allen_sauer.gwt.dnd.client.drop.IndexedDropController;
 import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
 import com.allen_sauer.gwt.dnd.client.drop.VerticalPanelDropController;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.storage.client.Storage;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
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
 import com.gwtplatform.mvp.client.proxy.RevealRootContentEvent;
 
 public class MainPresenter extends
 		Presenter<MainPresenter.MyView, MainPresenter.MyProxy> {
 
 	public interface MyView extends View {
 		public Label getMainLabel();
 
 		public AbsolutePanel getDndPanel();
 
 		public Image getDndImage();
 
 		Label getContent();
 
 		void setContent(Label content);
 
 		VerticalPanel getCards_panel();
 		
 		public VerticalPanel getDrop_cards_panel();
 	}
 
 	public static final Object SLOT_Card = new Object();
 	private IndirectProvider<CardPresenter> cardFactory;
 	@Inject DispatchAsync dispatcher;
 	protected ArrayList<CardPresenter> allCards = new ArrayList<CardPresenter>();
 
 	@ProxyCodeSplit
 	@NameToken(NameTokens.main)
 	public interface MyProxy extends ProxyPlace<MainPresenter> {
 	}
 
 	private Storage stockStore;
 
 	@Inject
 	public MainPresenter(final EventBus eventBus, final MyView view,
 			final MyProxy proxy, final Provider<CardPresenter> provider) {
 		super(eventBus, view, proxy);
 		cardFactory = new StandardProvider<CardPresenter>(provider);
 		stockStore = Storage.getLocalStorageIfSupported();
 	}
 
 	@Override
 	protected void revealInParent() {
 		// RevealRootContentEvent.fire(this, this);
 		RevealContentEvent.fire(this, HeaderPresenter.SLOT_CONTENT, this);
 	}
 
 	private String project_num;
 	private PickupDragController cardDragController;
 
 	@Override
 	public void prepareFromRequest(PlaceRequest request) {
 		super.prepareFromRequest(request);
 		project_num = request.getParameter("p", "-1");
 		// System.out.println("prepare from request: "+name);
 	}
 
 	@Override
 	protected void onBind() {
 		super.onBind();
 		set_dnd();
 	}
 
 	private void set_dnd() {
 		
 		// create a DragController to manage drag-n-drop actions
 		// note: This creates an implicit DropController for the boundary panel
 		cardDragController = new PickupDragController(
 				RootPanel.get(), false);
 
 
 		// dragController.makeDraggable(getView().getDndImage());
 
 		//AbsolutePositionDropController sp = new AbsolutePositionDropController(
 			//	getView().getDropPanel());
 		// IndexedDropController dropController = new
 		// IndexedDropController(getView().getDropPanel());
 
 //		dragController.registerDropController(sp);
 	//	dragController.makeDraggable(getView().getMainLabel());
 		//dragController.makeDraggable(getView().getHtml_panel());
 		//for (CardPresenter c : allCards)
 			//dragController.makeDraggable(c.getView().getWholePanel());
 		
 
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
 
 				Storage_access.setCards(project_num, result.getCards());
 				print_da_page();
 				// getView().getContent().setText(result.getActivities().toString());
 
 			}
 
 		});
 
 	}
 
 	/**
 	 * Print the cards, the board, with the information in the local storage
 	 */
 	private void print_da_page() {
 		System.out.println("**** Hell yeah, print da page");
 		writeCardWidgets();
 		
 	}
 
 	private void writeCardWidgets() {
 
 		setInSlot(SLOT_Card, null);
 		for (int i = 0; i < Storage_access.getNumberOfCard(); i++) {
 			final int myI = i;
 			cardFactory.get(new AsyncCallback<CardPresenter>() {
 
 				@Override
 				public void onSuccess(CardPresenter result) {
 					//addToSlot(SLOT_Card, result);
 				    //result.init(myI);
 				    //cardDragController.makeDraggable(result.getView().asWidget());
 				    //allCards.add(result);
 					
 					HTML widget = new HTML(Storage_access.getCard(myI));
 					widget.addStyleName("card");
 					getView().getCards_panel().add(widget);
 					cardDragController.makeDraggable(widget);
 					
					//TODO vrifier si il y a des lag en utilisant l'application sur le serveur
					//mettre en commentaire ces deux lignes
					VerticalPanelDropController dropController = new VerticalPanelDropController(getView().getDrop_cards_panel());
					cardDragController.registerDropController(dropController);
 					
 				}
 
 				@Override
 				public void onFailure(Throwable caught) {
 					// TODO Auto-generated method stub
 
 				}
 			});
 		}
 
 	}
 }
