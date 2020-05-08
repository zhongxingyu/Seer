 package edu.gatech.oad.fullhouse.findmystuff.pres;
 
 import android.os.AsyncTask;
 import edu.gatech.oad.fullhouse.findmystuff.dao.UserAccessor;
 import edu.gatech.oad.fullhouse.findmystuff.dao.impl.ServerUserAccessorImpl;
 import edu.gatech.oad.fullhouse.findmystuff.model.User;
 import edu.gatech.oad.fullhouse.findmystuff.view.LookupUserActivity;
 
 public class LookupUserPresenter {
 	private LookupUserActivity activity;
 	private User currentLookup;
 	private UserAccessor accessor;
 	
 	public LookupUserPresenter(LookupUserActivity activ) {
 		this.activity = activ;
 		this.accessor = new ServerUserAccessorImpl();
 	}
 	
 	public void lookupUser(final String username) {
 		new AsyncTask<Void, Void, User>() {
 			
 			@Override
             protected User doInBackground(Void... params) {
                 try {
                     //try to find the user, or catch an error if there was a problem
                     return accessor.getUserByUsername(username);
                 } catch (Exception e) {
                     return null;
                 }
             }
 	        
             @Override
             protected void onPostExecute(User user) {
                 //NOTE: runs in the UI thread, so activity calls are safe
                 
                 //test the states of the user object...
                 if (user == null) {
                     activity.userNotFoundError();
                 } else {
                 	activity.setFields(user.getUsername(), user.getName(), user.getEmail(), user.isLocked(), user.isAdmin());
                 	currentLookup = user;
                 }
             }
 		}.execute();
 	}
 	
 	public void unlockUser() {
 		if (currentLookup == null) {
 			activity.userNotFoundError();
 		} else {
 			currentLookup.resetLogin();
 			activity.changeLockedField(false);
 			new AsyncTask<Void, Void, Void>() {
 				@Override
 	            protected Void doInBackground(Void... params) {
 	                accessor.updateUser(currentLookup);
 	                return null;
 	            }
 			}.execute();
 		}
 	}
 	
 	public void makeAdmin() {
 		if (currentLookup == null) {
 			activity.userNotFoundError();
 		} else {
 			currentLookup.setAdmin(true);
 			activity.changeAdminField(true);
 			new AsyncTask<Void, Void, Void>() {
 				@Override
 	            protected Void doInBackground(Void... params) {
 	                accessor.updateUser(currentLookup);
 	                return null;
 	            }
 			}.execute();
 		}
 	}
 	
 	public void deleteUser() {
 		if (currentLookup == null) {
 			activity.userNotFoundError();
 		} else {
 			activity.removeFields();
 			new AsyncTask<Void, Void, Void>() {
 				@Override
 	            protected Void doInBackground(Void... params) {
 	                accessor.deleteUser(currentLookup);
 	                return null;
 	            }
 			}.execute();
 		}
 	}
 }
