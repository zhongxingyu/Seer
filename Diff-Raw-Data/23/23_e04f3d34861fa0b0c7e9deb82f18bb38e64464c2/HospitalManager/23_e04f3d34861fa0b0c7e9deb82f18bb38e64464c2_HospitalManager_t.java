 
 import HMSViews.LoginView;
 
 class HospitalManager
 {
 	public static void main(String[] args) {
 		try {
 	 		LoginView loginView = new LoginView();
 	 		loginView.open();
 	 	} catch (Exception e) {
 	 		e.printStackTrace();
 	 	}
 	}
 }
