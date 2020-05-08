 package se.chalmers.krogkollen.detailed;
 
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.view.View;
 import com.google.android.gms.maps.model.LatLng;
 import se.chalmers.krogkollen.IView;
 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.backend.BackendHandler;
 import se.chalmers.krogkollen.backend.BackendNotInitializedException;
 import se.chalmers.krogkollen.backend.NoBackendAccessException;
 import se.chalmers.krogkollen.backend.NotFoundInBackendException;
 import se.chalmers.krogkollen.map.UserLocation;
 import se.chalmers.krogkollen.pub.IPub;
 import se.chalmers.krogkollen.pub.PubUtilities;
 
 /**
  * A presenter class for the detailed view of a pub
  */
 public class DetailedPresenter implements IDetailedPresenter {
 
 	/** The view connected with the Presenter */
 	private DetailedActivity	view;
 
 	/** The pub that the Presenter holds */
 	private IPub				pub;
 
 	@Override
 	public void setView(IView view) {
 		this.view = (DetailedActivity) view;
 	}
 
 	@Override
 	public void setPub(String pubID) throws NoBackendAccessException, NotFoundInBackendException,
 			BackendNotInitializedException {
 		pub = PubUtilities.getInstance().getPub(pubID);
 		BackendHandler.getInstance().updatePubLocally(pub);
 	}
 
 	// TODO Can this be refactored?
 	@Override
 	public void ratingChanged(int rating) throws NotFoundInBackendException,
 			NoBackendAccessException, BackendNotInitializedException {
 
 		if (rating == 1) {
 			if (view.getSharedPreferences(pub.getID(), 0).getInt("thumb", 0) == 1) {
                 view.setThumbs(0);
 
 				BackendHandler.getInstance().removeRatingVote(pub, 1);
 				pub.setPositiveRating(pub.getPositiveRating() - 1);
 
 				saveThumbState(0);
 
 			} else if (view.getSharedPreferences(pub.getID(), 0).getInt("thumb", 0) == -1) {
                 view.setThumbs(1);
 
 				BackendHandler.getInstance().removeRatingVote(pub, -1);
 				pub.setNegativeRating(pub.getNegativeRating() - 1);
 
 				BackendHandler.getInstance().addRatingVote(pub, 1);
 				pub.setPositiveRating(pub.getPositiveRating() + 1);
 
 				saveThumbState(1);
 			} else {
                 view.setThumbs(1);
 
 				BackendHandler.getInstance().addRatingVote(pub, 1);
 				pub.setPositiveRating(pub.getPositiveRating() + 1);
 
 				saveThumbState(1);
 			}
 		}
 
 		else if (rating == -1) {
 			if (view.getSharedPreferences(pub.getID(), 0).getInt("thumb", 0) == -1) {
 				view.setThumbs(0);
 
 				BackendHandler.getInstance().removeRatingVote(pub, -1);
 				pub.setNegativeRating(pub.getNegativeRating() - 1);
 
 				saveThumbState(0);
 
 			} else if (view.getSharedPreferences(pub.getID(), 0).getInt("thumb", 0) == 1) {
 				view.setThumbs(-1);
 
 				BackendHandler.getInstance().removeRatingVote(pub, 1);
 				pub.setPositiveRating(pub.getPositiveRating() - 1);
 				BackendHandler.getInstance().addRatingVote(pub, -1);
 				pub.setNegativeRating(pub.getNegativeRating() + 1);
 
 				saveThumbState(-1);
 			} else {
 				view.setThumbs(-1);
 
 				BackendHandler.getInstance().addRatingVote(pub, -1);
 				pub.setNegativeRating(pub.getNegativeRating() + 1);
 
 				saveThumbState(-1);
 			}
 		} else {
 			view.setThumbs(rating);
 
 			BackendHandler.getInstance().addRatingVote(pub, rating);
 			if (rating > 0) {
 				pub.setPositiveRating(pub.getPositiveRating() + 1);
 			} else {
 				pub.setNegativeRating(pub.getNegativeRating() + 1);
 			}
 			saveThumbState(rating);
 		}
 
         updateVotes();
 	}
 
     /**
      * Saves the state of the favorite locally
      */
     public void saveFavoriteState(){
         SharedPreferences.Editor editor = view.getSharedPreferences(pub.getID(), 0).edit();
         editor.putBoolean("star", (!view.getSharedPreferences(pub.getID(), 0).getBoolean("star", true)));
         editor.commit();
     }
 
     /**
      * Saves the state of the thumb because a user is only allowed to vote 1 time.
      * @param thumb
      */
 	private void saveThumbState(int thumb){
 		SharedPreferences.Editor editor = view.getSharedPreferences(pub.getID(), 0).edit();
 		editor.putInt("thumb", thumb);
 		editor.commit();
 	}
 
     /**
      * Converts the hour string
      * @param hour
      * @return
      */
 	private String convertOpeningHours(int hour){
 		if(hour / 10 ==0){
 			return "0"+hour;
 		}
 		return ""+hour;
 	}
 
     /**
      * Updates the info of a pub
      * @throws NoBackendAccessException
      * @throws NotFoundInBackendException
      * @throws BackendNotInitializedException 
      */
     public void updateInfo() throws NoBackendAccessException, NotFoundInBackendException{
         new UpdateTask().execute();
     }
 
     // TODO what does this method do?
     private class UpdateTask extends AsyncTask<Void, Void, Void>{
         protected void onPreExecute(){
             view.showProgressDialog();
         }
 
         protected Void doInBackground(Void... voids){
 
             try {
                 BackendHandler.getInstance().updatePubLocally(pub);
             } catch (NoBackendAccessException e) {
                 view.showErrorMessage(e.getMessage());
             } catch (NotFoundInBackendException e) {
             	view.showErrorMessage(e.getMessage());
             } catch (BackendNotInitializedException e){
             	view.showErrorMessage(e.getMessage());
             }
             return null;
         }
 
         protected void onPostExecute(Void result){
             view.hideProgressDialog();
             updateMain();
         }
     }
 
 
     @Override
     public void onClick(View view) {
         if(view.getId() == R.id.thumbsDownLayout){
             try {
                 ratingChanged(-1);
                 //updateThumbs();
             } catch (NotFoundInBackendException e) {
             	this.view.showErrorMessage(e.getMessage());
             } catch (NoBackendAccessException e) {
                 this.view.showErrorMessage(e.getMessage());
             } catch (BackendNotInitializedException e){
             	this.view.showErrorMessage(e.getMessage());
             }
         }
         else if(view.getId() == R.id.thumbsUpLayout){
             try {
                 ratingChanged(1);
                 //updateThumbs();
             } catch (NotFoundInBackendException e) {
             	this.view.showErrorMessage(e.getMessage());
             } catch (NoBackendAccessException e) {
             	this.view.showErrorMessage(e.getMessage());
             } catch (BackendNotInitializedException e){
             	this.view.showErrorMessage(e.getMessage());
             }
         } else if (view.getId() == R.id.navigate) {
             Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr="
                     + UserLocation.getInstance().getCurrentLatLng().latitude + ","
                     + UserLocation.getInstance().getCurrentLatLng().longitude + "&daddr="
                     + pub.getLatitude() + "," + pub.getLongitude()));
             this.view.startActivity(i);
         }
     }
 
     //Sends the new information to the view for displaying.
     private void updateMain(){
         view.updateQueueIndicator(pub.getQueueTime());
         view.updateText(pub.getName(), pub.getDescription(), convertOpeningHours(pub.getTodaysOpeningHour()) + " - " +
                 (convertOpeningHours(pub.getTodaysClosingHour())), ""+pub.getAgeRestriction() + " år", ""+pub.getEntranceFee()
                 + ":-"); // TODO put "år" in xml
         view.addMarker(pub);
         view.navigateToLocation(new LatLng(pub.getLatitude(), pub.getLongitude()), 14);
         view.showStar(view.getSharedPreferences(pub.getID(), 0).getBoolean("star", true));
         view.setThumbs(view.getSharedPreferences(pub.getID(), 0).getInt("thumb", 0));
         view.removeMarker();
         view.addMarker(pub);
         updateVotes();
     }
 
     @Override
     public void updateStar(){
         saveFavoriteState();
         view.showStar(view.getSharedPreferences(pub.getID(), 0).getBoolean("star", true));
     }
 
    // TODO what does this do?
     private void updateVotes(){
         view.showVotes("" + pub.getPositiveRating(), "" + pub.getNegativeRating());
     }
 }
