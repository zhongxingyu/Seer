 package it.giacomos.android.osmer.pro.widgets;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 import it.giacomos.android.osmer.pro.forecastRepr.ForecastDataInterface;
 
 public class ForecastDataIdMapper {
 
 	public int get(ForecastDataInterface fdi) 
 	{
 		int startOfStripOrZone = 0;
 		ArrayList<String> areas = new ArrayList<String>();
 		areas.add("A1");
 		areas.add("A2");
 		areas.add("A3");
 		areas.add("A4");
 
 		/* suppose that the blocks of text retrieved by the server contain:
 		 * 0: Region
 		 * 1: A1
 		 * 2: A2
 		 * 3: A3
 		 * 4: A4
 		 * 
 		 * A5 to A9 do not contain useful data, so the php script on giacomos.it does not take
 		 * them into account.
 		 * 
 		 * 5: (areas.size (=4) + 1 ) F1 or Z1
 		 * 6: F2 or Z2
 		 * 7: F3 or Z3
 		 * 8: F4 or Z4
 		 * 
 		 * Pieces of information from F's and Z's are put together by the php script in 
 		 * giacomos.it/meteo.fvg/make_text.php
 		 * 
 		 */
		startOfStripOrZone = areas.size();
 
 		String id = fdi.getId();
 
 		try{
 			if(areas.contains(id))
 				return Integer.parseInt(id.replace("A", ""));
 			/* F1.. F4 or Z1... Z4 */
 			else if(id.startsWith("F"))
 				return Integer.parseInt(id.replace("F", "")) + startOfStripOrZone;
 			else if(id.startsWith("Z"))
 				return Integer.parseInt(id.replace("Z", "")) + startOfStripOrZone;
 			else if(id.compareTo("A5") == 0 || id.compareTo("A7") == 0) /* pordenone, gorizia: bassa pianura: like F3 or Z3 */
 				return startOfStripOrZone + 3;
 			else if(id.compareTo("A6") == 0) /* ud: alta pianura: like F2 or Z2 */
 				return startOfStripOrZone + 2;
 			else if(id.compareTo("A8") == 0 || id.compareTo("A9") == 0) /* lignano / trieste: costa: like F4 or Z4 */
 				return startOfStripOrZone + 4;
 			
 				
 		}
 		catch(NumberFormatException nfe)
 		{
 			Log.e("it.giacomos.android.osmer.pro.widgets.ForecastDataIdMapper", "error extracting integer from " + id);
 		}
 		return 0;
 	}
 
 }
