 package com.example.wecharades.presenter;
 
 import com.example.wecharades.views.NewGameActivity;
 
 public class NewGamePresenter extends Presenter {
	private NewGameActivity activity;
 	
 	public NewGamePresenter(NewGameActivity activity) {
 		super(activity);
		this.activity = activity;
 	}
 
 	public void randomInvitation() {
		// TODO Kommunicera med dc och skicka och kolla om det finns ngra i randomInvites
 	}
 
 }
