 package se.chalmers.krogkollen.detailed;
 
 import com.google.android.gms.maps.model.LatLng;
 import se.chalmers.krogkollen.IView;
 import se.chalmers.krogkollen.pub.IPub;
 
 /**
  * Interface for a DetailedView
  * 
  * @author Oskar Karrman
  * 
  */
 public interface IDetailedView extends IView {
 
 	// TODO javadoc
 	public void updateText(String pubName, String description, String openingHours, String age,
 			String price);
 
 	/**
 	 * Updates the queue indicator on the detailed view
 	 * 
 	 * @param queueTime the queue time used to update
 	 */
 	public void updateQueueIndicator(int queueTime);
 
     /**
      * Updates the vote text.
      *
      * @param upVotes
      * @param downVotes
      */
     public void showVotes(String upVotes, String downVotes);
 
     /**
      * Adds a marker to the map.
      * @param pub the pub to add a marker for.
      */
     public void addMarker(IPub pub);
 
     /**
      * Shows the location of the coordinates on the map.
      * @param latLng The location to show.
      * @param zoom The speed which the map zooms to location.
      */
     public void navigateToLocation(LatLng latLng, int zoom);
 }
