 package com.example.wheresmystuff.Presenter;
 
 import com.example.wheresmystuff.Model.IModel;
 import com.example.wheresmystuff.View.LockOrUnlock;
 
 public class UnlockUser {
 
 	private final IModel myModel;
 	private final LockOrUnlock myView;
 
 	public UnlockUser(LockOrUnlock v, IModel m) {
 
 		myModel = m;
 		myView = v;
 
 	}
 
 	public void unlockUser(String username) {
 
 		myModel.open();
 		
 		if (myModel.find_uid(username)) {
			myModel.unlockAccount(username);
 			myView.notify_of_error("Unlocked " + username);
 
 		}else{
 			myView.notify_of_error("User not Found");
 		}
 		myModel.close();
 
 	}
 }
 
