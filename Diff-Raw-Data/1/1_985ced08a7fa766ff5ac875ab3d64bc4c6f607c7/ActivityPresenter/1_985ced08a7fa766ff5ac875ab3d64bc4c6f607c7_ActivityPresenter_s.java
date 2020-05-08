 package com.sfeir.common.gwt.client.mvp;
 
 import com.google.gwt.activity.shared.AbstractActivity;
 import com.google.gwt.place.shared.Place;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.sfeir.common.gwt.client.events.ErrorMessageEvent;
 import com.sfeir.common.gwt.client.events.LoadingEvent;
 import com.sfeir.common.gwt.client.events.SetTitleEvent;
 
 /**
  * Super class of all Activities
  * 
  * Allow to access to the ClientFactory and the Place
  * 
  * Add also helper methods
  * 
  * @param <P>
  *            The place that launch this activity
  */
 public abstract class ActivityPresenter<P extends Place> extends AbstractActivity {
 	ClientFactory clientFactory;
 	P place;
 	Place oldPlace;
 	private boolean isLive = true;
 	private boolean isTop = true;
 	
 	public ActivityPresenter() {
     }
 	
 	public ActivityPresenter(P place) {
     }
 
 	/**
 	 * Get the global ClientFactory
 	 * 
 	 * @return
 	 */
 	protected ClientFactory getClientFactory() {
 		return clientFactory;
 	}
 
 	/**
 	 * Get the place used to launch this activity
 	 * 
 	 * @return The Place of type
 	 *         <P>
 	 */
 	protected P getPlace() {
 		return place;
 	}
 
 	/**
 	 * Shortcut to Go To a new place
 	 * 
 	 * @param newPlace
 	 */
 	protected void goTo(Place newPlace) {
 		clientFactory.getPlaceController().goTo(newPlace);
 	}
 	
 	/**
 	 * Replace the last Place
 	 * 
 	 * @param newPlace
 	 */
 	protected void replacePlace(Place newPlace) {
 		clientFactory.getPlaceController().replace(newPlace);
 	}
 
 	/**
 	 * Display an errorMessage
 	 * 
 	 * If errorMessage is null, hide the error message
 	 * 
 	 * @param errorMessage
 	 */
 	protected void setErrorMessage(String errorMessage) {
 		clientFactory.getEventBus().fireEvent(new ErrorMessageEvent(errorMessage));
 	}
 	
 	/**
 	 * Set the title of the page (Send to the layout who is responsible to display it
 	 * 
 	 * If the place extend the DialogBoxPlace, the title is used has the Caption of the dialogbox
 	 * 
 	 * @param title
 	 */
 	protected void setTitle(String title) {
 		clientFactory.getEventBus().fireEvent(new SetTitleEvent(title));
 	}
 
 	/**
 	 * Display or hide a loading dialog
 	 * 
 	 * @param loading
 	 */
 	protected void setLoading(boolean loading) {
 		clientFactory.getEventBus().fireEvent(new LoadingEvent(loading));
 	}
 	
 	/**
 	 * Check if the Activity is allready live
 	 * @return
 	 */
 	public boolean isLive() {
 		return isLive;
 	}
 	
 	/**
 	 * Return false if the activity is a subactivity of an other activity (in activity group, with the attachActivity)
 	 * @return
 	 */
 	public boolean isTop() {
 		return isTop;
 	}
 	
 	void setTop(boolean isTop) {
 		this.isTop = isTop;
 	}
 
 	/**
 	 * Wrap the async callback to a callback which manage not login exception
 	 * Is the onStop or onCancel is called in this activity, the AsyncCallBack is canceled
 	 * 
 	 * @param async
 	 * @return
 	 */
 	protected <T> AsyncCallback<T> createAsync(AsyncCallback<T> async) {
 		return new ServiceCallback<T>(this, async);
 	}
 
 	/* Private method used by the ActivityMapper */
 
 	void setClientFactory(ClientFactory clientFactory) {
 		this.clientFactory = clientFactory;
 	}
 
 	void setPlace(P place) {
 		this.place = place;
 	}
 	
 	@Override
 	public void onStop() {
 		super.onStop();
 		this.isLive = false;
 	}
 	
 	@Override
 	public void onCancel() {
 		super.onCancel();
 		this.isLive = false;
 	}
 	
 	public void setOldPlace(Place oldPlace) {
         this.oldPlace = oldPlace;
     }
 	
 	protected void returnLastPlace() {
 	    goTo(oldPlace);
 	}
 }
