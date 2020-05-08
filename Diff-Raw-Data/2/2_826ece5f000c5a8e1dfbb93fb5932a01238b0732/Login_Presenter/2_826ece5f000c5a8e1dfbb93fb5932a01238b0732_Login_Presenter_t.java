 package com.example.wheresmystuff.Presenter;
 
 import com.example.wheresmystuff.Model.IModel;
 import com.example.wheresmystuff.Model.User;
 import com.example.wheresmystuff.View.ILoginView;
 import com.example.wheresmystuff.View.MainActivity;
 import com.example.wheresmystuff.View.mainUserScreen;
 import com.example.wheresmystuff.validation.ValidatePassword;
 
 public class Login_Presenter {
 
 	
 	private final IModel myModel;
 	private final ILoginView myView;
 	
 	public Login_Presenter(ILoginView v, IModel m){
 		myModel = m;
 		myView = v;
 	}
 	
 	
 	public void validate(String name, String password){
 		
 		//search the model. From that determine if legit or not 
 		
 		myModel.open();
 		
 		//try to find user
 		if(myModel.find_uid(name) ){
 			
 			//found user; now try to find password
 			int login_attempts = myModel.getLoginAttempts(name);
 			if(login_attempts >= 3){
 				myView.notify_of_error("Account Locked");
 				myModel.setLocked(name);
 				myView.call_intent(MainActivity.class);
 			}else{
 				if(!myModel.find_password(password,name)){
 				//found user but not password
 				//get the login_attempts
 				//error checking
 					if(!("admin".compareTo(name) == 0)){
 						myModel.increase_login_attempts(++login_attempts, name);
 						myView.notify_of_error("Unknow user and/or invalid password");
 					}
					myView.notify_of_error("Unknow user and/or invalid password");
 				}
 				else{
 					myView.call_intent(mainUserScreen.class);
 					myModel.setCurUser(name);
 				}
 				
 			}
 		}else{
 			myView.notify_of_error("Unknow user and/or invalid password");
 		}
 		//close the db
 		
 		myModel.close();
 		//if(!found_user) myView.notify_of_error("Unknow user and/or invalid password")	
 	}
 }
