 package com.teamgrau.altourism.util.data;
 
 import java.util.List;
 
 import com.google.android.gms.maps.model.LatLng;
 import com.teamgrau.altourism.util.data.model.POI;
 
/**
 * Testklasse für einen Storyprovider, der 6 verschiedene POI's liefert
 * 
 * @author simon
 *
 */
 public class StoryProviderHardcoded implements StoryProvider {
 
	private List<POI> geschichten;
	
 	@Override
 	public List<POI> listPOIs(LatLng position, double radius) {
		return geschichten;
	}
	
	public StoryProviderHardcoded(){
		geschichten.add(new POI("Gendarmenmarkt", new LatLng(52.513609,13.392119)));
		geschichten.add(new POI("Humboldt Uni", new LatLng(52.517396, 13.394394)));
		geschichten.add(new POI("Altes Museum", new LatLng(52.519772, 13.398385)));
		geschichten.add(new POI("Deutscher Dom", new LatLng(52.518806, 13.400531)));
		geschichten.add(new POI("Hackescher Markt", new LatLng(52.524159, 13.402376)));
		geschichten.add(new POI("Alexanderplatz", new LatLng(52.522201,13.412848)));
 	}
 
 }
