 package se.chalmers.h_sektionen.utils;
 
 import java.util.ArrayList;
 
 import se.chalmers.h_sektionen.R;
 
 import android.content.Context;
 import android.content.res.Resources;
 
 /**
  * Populate a static list of items. The list is used to build the menu.
  *
  */
 public class MenuModel {
 
 	 public static ArrayList<MenuItem> Items;
 	 	/**
 	 	 * Constructor - Populates a list containing the items for the menu
 	 	 * @param con - Context of the calling activity
 	 	 */
 	    public static void LoadModel(Context con) {
 
 	    	//menu_titles
 	    	//con.getResources().getXml(R.xml.samplexml);
 	    	
 	    	Resources res = con.getResources();
 	    	String[] planets = res.getStringArray(R.array.menu_titles);
 	    	
 	        Items = new ArrayList<MenuItem>();
<<<<<<< HEAD
 	        Items.add(new MenuItem(1, "ic_news.png", planets[0]));
 	        Items.add(new MenuItem(2, "ic_events.png", planets[1]));
 	        Items.add(new MenuItem(3, "ic_lunch.png", planets[2]));
 	        Items.add(new MenuItem(4, "ic_11an.png", planets[3]));
 	        Items.add(new MenuItem(5, "ic_info.png", planets[4]));
 	        Items.add(new MenuItem(6, "ic_suggest.png", planets[5]));
 	        Items.add(new MenuItem(7, "ic_faultreport.png", planets[6]));
=======
	        Items.add(new MenuItem(1, "ic_news.png", "Nyheter"));
	        Items.add(new MenuItem(2, "ic_events.png", "Events"));
	        Items.add(new MenuItem(3, "ic_lunch.png", "Dagens Lunch"));
	        Items.add(new MenuItem(4, "ic_11an.png", "11:an"));
	        Items.add(new MenuItem(5, "ic_info.png", "Information"));
	        Items.add(new MenuItem(6, "ic_suggest.png", "Förslagslådan"));
	        Items.add(new MenuItem(7, "ic_faultreport.png", "Felanmälan"));
>>>>>>> d3407637371c3f252643330733e364aee7e95dfd
 
 	    }
 
 	    /**
 	     * Return MenuItem corresponding to a Id
 	     * 
 	     * @param id - identifier of the MenuItem
 	     * @return MenuItem - returns a MenuItem
 	     */
 	    public static MenuItem GetbyId(int id){
 
 	        for(MenuItem item : Items) {
 	            if (item.Id == id) {
 	                return item;
 	            }
 	        }
 	        return null;
 	    }
 }
