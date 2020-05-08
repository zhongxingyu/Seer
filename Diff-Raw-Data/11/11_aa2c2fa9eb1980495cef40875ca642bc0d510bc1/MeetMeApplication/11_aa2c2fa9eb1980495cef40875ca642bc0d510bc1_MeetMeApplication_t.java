 package io.meetme;
 
 import io.meetme.database.DatabaseManager;
 import io.meetme.database.DatabaseManager.OnUserAddedListener;
 import io.meetme.database.User;
 import io.meetme.restclient.RestClient;
 import io.meetme.restclient.RestClient.RequestMethod;
 import io.meetme.restclient.RestClient.Response;
import io.meetme.util.AndroidUtils;
 import android.app.Application;
 import android.os.SystemClock;
 import android.util.Log;
 
 public class MeetMeApplication extends Application implements
 	OnUserAddedListener {
 
    public static MeetMeApplication instance;

     @Override
     public void onCreate() {
 	super.onCreate();
 
	instance = this;

 	DatabaseManager.init(this, R.xml.database);
 	DatabaseManager.setOnUserAddedListener(this);
 	DatabaseManager databaseManager = DatabaseManager.getInstance();
 
 	databaseManager.add(User.meetUser("123", "Test"));
 
 	Log.d(this.toString(), "POINTS: " + databaseManager.getPoints());
     }
 
     @Override
     public void onAdded(User user) {
 	// TODO test it when server will be up :D
 	new Thread(new StatSender()).start();
     }
 
     public static class StatSender implements Runnable {
 
 	@Override
 	public void run() {
 
 	    // Please do not cheat :D and hack XD
 	    RestClient restClient = new RestClient(
 		    "http://googleio.vador.mydevil.net/saverank/",
 		    RequestMethod.POST);
 
	    restClient.AddParam("id",
		    AndroidUtils.getUUID(MeetMeApplication.instance));
 	    restClient.AddParam("points",
 		    String.valueOf(DatabaseManager.getInstance().getPoints()));
 	    restClient.AddParam("username",
 		    String.valueOf(DatabaseManager.getInstance().getPoints()));
 
 	    try {
 		restClient.buildRequest();
 	    } catch (Exception e) {
 		e.printStackTrace();
 	    }
 	    restClient.executeRequest();
 
 	    Response response = restClient.getResponse();
 
 	    System.out.println("Response: " + response.content + " code "
 		    + response.httpCode + " err " + response.httpCodeMessage);
 
 	    if (response.httpCode != 200) {
 		// Go sleep and retry when connection fails
 		SystemClock.sleep(10000);
 		// Pseudo recursive call :D
 		StatSender statSender = new StatSender();
 		statSender.run();
 	    }
 	}
     }
 }
