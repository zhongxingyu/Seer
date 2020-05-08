 package org.omships.omships;
 
 import java.io.BufferedInputStream;
 import java.net.URL;
 import java.util.List;
 
 import org.omships.omships.datatypes.Ship;
 import org.omships.omships.parse.SettingsReader;
 
 import android.content.res.Resources;
 import android.os.AsyncTask;
 
 /**
  * Singleton style class for holding ship configurations.
  */
 public class Settings {
 	private static int selected=0;
 	private static List<Ship> ships=null;
 	
 	static class FetchConfig extends AsyncTask<String,Integer,Integer>{
 
 		@Override
 		protected Integer doInBackground(String... urls) {
 			try {
 				SettingsReader reader=new SettingsReader(
 						new BufferedInputStream(new URL(urls[0]).openStream()));
 				ships = reader.getItems();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return 0;
 		}//end doInBackground
 		
 	}//end class FetchConfig
 	
 	/**
 	 * Loads the configuration
 	 * @param r
 	 * There is a default config.xml, and we load an updated version
 	 *  from the web when we get the chance. 
 	 */
	public static void loadConfig(Resources r){
 		if(ships==null){
 			SettingsReader reader = new SettingsReader(r.openRawResource(R.raw.config));
 			try {
 				ships = reader.getItems();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}//end try-catch
 			new FetchConfig().execute("http://danielrothfus.com/config.xml");
 		}//end if
 	}//end loadConfig
 	
 	/**
 	 * Gets a list of all known ships.
 	 * @return
 	 */
 	public static List<Ship> getShips(){
 		return ships;
 	}
 	
 	/**
 	 * Gets the currently selected ship.
 	 * @return
 	 */
 	public static Ship getShip(){
 		return ships.get(selected);
 	}
 }//end Settings
