 package com.example.wecharades.presenter;
 
 import com.example.wecharades.views.NewGameActivity;
 
 public class NewGamePresenter extends Presenter {
 	
 	public NewGamePresenter(NewGameActivity activity) {
 		super(activity);
 	}
 
 	public void randomInvitation() {
		dc.putInRandomQueue();
 	}
 
 }
