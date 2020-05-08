 package be.betty.gwtp.client.presenters;
 
 import be.betty.gwtp.client.CellDropControler;
 
 import com.allen_sauer.gwt.dnd.client.drop.DropController;
 import com.allen_sauer.gwt.dnd.client.drop.VerticalPanelDropController;
 import com.gwtplatform.mvp.client.PresenterWidget;
 import com.gwtplatform.mvp.client.PopupView;
 import com.google.inject.Inject;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.widget.client.TextButton;
 
 public class DeleteCardPopupPresenter extends
 		PresenterWidget<DeleteCardPopupPresenter.MyView> {
 
 	public interface MyView extends PopupView {
 		
 		public TextButton getCloseButton();
 		
 		public TextButton getWindowCloseButton();
 
 		public VerticalPanel getCardVerticalPanel();
 
 		public VerticalPanel getDeleteVerticalPanel();
 
 		public TextButton getDeleteCardButton();
 		
 	}
 
 	@Inject
 	public DeleteCardPopupPresenter(final EventBus eventBus, final MyView view) {
 		super(eventBus, view);
 	}
 
 	@Override
 	protected void onBind() {
 		super.onBind();
		VerticalPanelDropController trash = new VerticalPanelDropController(getView().getDeleteVerticalPanel());
		MainPresenter.cardDragController.registerDropController(trash);
		
		getView().getCloseButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				getView().hide();
				
			}
		});
		
		getView().getWindowCloseButton().addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				getView().hide();
			}
		});
 		
 	}
 	
 	@Override
 	protected void onReveal() {	
 		super.onReveal();
 		//populateCardPanel();
 	};
 	
 	public void populateCardPanel() {
 		for (int i = 0; i < MainPresenter.allCards.size(); i++) {
 			getView().getCardVerticalPanel().add(MainPresenter.allCards.get(""+i));
 			MainPresenter.allCards.get(""+i).addStyleName("white");
 		}
 	}
 }
