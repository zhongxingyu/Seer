 package Hanes.testProj;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 
 public class DrinkAdmin {
 	/*
 	 * Don't use this
 	 */
 	public DrinkAdmin()
 	{
 	}
 	/* DOES NOT WORK
	 * DON't USE
 	 * 
 	 * Takes a username and a credit amount and adds them to
 	 * the user's account
 	 * 
 	 * Returns true if successful, or false if unsuccessful
 	 */
 	public static boolean addCredits(String user, int credits, Connector drinkServ, Drink_Main dMain)
 	{
 		ArrayList<String> back = drinkServ.command("addcredits "+user+" " + credits);
 		if (back.get(0).indexOf("ERR") == -1)
 		{
 			Log.d("CreditAddError","Error adding "+credits+" to user "+user +"\n"+back.get(0));
 			return false;
 		}
 		Log.d("CreditAddSuccess","Added " +credits+" to user "+user);
 		return true;
 	}
 
 
 }
