 package com.baas.client.presenter;
 
 import com.baas.client.place.PlaceTokens;
 import com.baas.shared.GetStoryAction;
 import com.baas.shared.GetStoryResult;
 import com.baas.shared.UpdateStoryAction;
 import com.baas.shared.UpdateStoryResult;
 import com.baas.shared.core.UserStory;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.shared.EventBus;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.inject.Inject;
 import com.gwtplatform.dispatch.client.DispatchAsync;
 import com.gwtplatform.mvp.client.Presenter;
 import com.gwtplatform.mvp.client.View;
 import com.gwtplatform.mvp.client.annotations.NameToken;
 import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
 import com.gwtplatform.mvp.client.proxy.PlaceManager;
 import com.gwtplatform.mvp.client.proxy.PlaceRequest;
 import com.gwtplatform.mvp.client.proxy.ProxyPlace;
 import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
 
 public class UserStoryPresenter extends Presenter<UserStoryPresenter.MyView, UserStoryPresenter.MyProxy> {
 
 	public interface MyView extends View {
 		public void setStory(UserStory story);
 		public UserStory getStory();
 		public Button getCancelButton();
 		public Button getConfirmButton();
 	}
 
 	@ProxyCodeSplit
 	@NameToken(PlaceTokens.STORY)
 	public interface MyProxy extends ProxyPlace<UserStoryPresenter>{}
 	
 	private PlaceManager placeManager;
 	private DispatchAsync dispatcher;
 
 	@Inject
 	public UserStoryPresenter(EventBus eventBus, MyView view, MyProxy proxy, PlaceManager placeManager, DispatchAsync dispatcher) {
 		super(eventBus, view, proxy);
 		
 		this.placeManager = placeManager;
 		this.dispatcher = dispatcher;
 	}
 
 	@Override
 	protected void revealInParent() {
 		RevealContentEvent.fire(this, MainPagePresenter.TYPE_SetMainContent, this);
 	}
 	
 	@Override
 	protected void onBind() {
 		super.onBind();
 		getView().getCancelButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				placeManager.revealRelativePlace(-1);
 			}
 		});
 		
 		getView().getConfirmButton().addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				UserStory story = getView().getStory();
 				
 				updateStory(story);
 			}
 		});
 	}
 	
 	@Override
 	public void prepareFromRequest(PlaceRequest request) {
 		super.prepareFromRequest(request);
 		String selectedStory = request.getParameter("storyId", null);
 		String selectedBacklogId = placeManager.getCurrentPlaceHierarchy().get(0).getParameter("backlogId", null);
 		
 		String action = request.getParameter(PlaceTokens.ACTION_PARAM_KEY, PlaceTokens.LIST_STORIES_ACTION_PARAM_KEY);
 		if(action.equals(PlaceTokens.EDIT_ACTION_PARAM_KEY) && selectedStory != null){
 			editStory(Long.parseLong(selectedStory));
 		}else if(action.equals(PlaceTokens.NEW_ACTION_PARAM_KEY)){
 			newStory(selectedBacklogId);
 		}
 	}
 
 	private void newStory(String backlogId) {
 		if(backlogId == null ){
 			Window.alert("Erreur lors de la récupération du backlog dans lequel créer la story");
 			return;
 		}
 			
 		UserStory userStory = new UserStory();
 		userStory.setBacklogId(Long.parseLong(backlogId));
 		getView().setStory(userStory);
 	}
 
 	private void editStory(long selectedStory) {
 		dispatcher.execute(new GetStoryAction(selectedStory), new AsyncCallback<GetStoryResult>() {
 			@Override
 			public void onFailure(Throwable caught) {
 			}
 
 			@Override
 			public void onSuccess(GetStoryResult result) {
 				getView().setStory(result.getStory());
 			}
 		});
 	}
 	
 	private void updateStory(UserStory story) {
 		dispatcher.execute(new UpdateStoryAction(story), new AsyncCallback<UpdateStoryResult>() {
 			@Override
 			public void onFailure(Throwable caught) {
 			}
 
 			@Override
 			public void onSuccess(UpdateStoryResult result) {
 				placeManager.revealRelativePlace(-1);
				Window.alert("La story " + result.getStory().getName() + " a été mise jour avec succès.");
 			}
 		});
 	}
 }
