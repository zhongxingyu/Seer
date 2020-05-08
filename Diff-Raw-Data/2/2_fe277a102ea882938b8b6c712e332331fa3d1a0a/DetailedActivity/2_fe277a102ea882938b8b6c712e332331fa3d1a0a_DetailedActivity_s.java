 package se.chalmers.krogkollen.detailed;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.backend.NoBackendAccessException;
 import se.chalmers.krogkollen.backend.NotFoundInBackendException;
 import se.chalmers.krogkollen.map.MapActivity;
 
 /*
  * This file is part of Krogkollen.
  *
  * Krogkollen is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Krogkollen is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  *
  * You should have received a copy of the GNU General Public License
  * along with Krogkollen.  If not, see <http://www.gnu.org/licenses/>.
  *
 <<<<<<< Updated upstream
  */
 
 /**
  * This class represent the activity for the detailed view
  */
 public class DetailedActivity extends Activity implements IDetailedView {
 
     /** The presenter connected to the detailed view */
 	private IDetailedPresenter presenter;
 
     /** A bunch of view elements*/
     private TextView pubTextView, descriptionTextView,openingHoursTextView,
             ageRestrictionTextView, entranceFeeTextView, votesUpTextView, votesDownTextView;
     private ImageButton thumbsUpButton, thumbsDownButton;
     private ImageView queueIndicator;
     private MenuItem favoriteStar;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_detailed);
         presenter = new DetailedPresenter();
         presenter.setView(this);
         try {
 			presenter.setPub(getIntent().getStringExtra(MapActivity.MARKER_PUB_ID));
 		} catch (NotFoundInBackendException e) {
 			this.showErrorMessage(e.getMessage());
 		} catch (NoBackendAccessException e) {
 			this.showErrorMessage(e.getMessage());
 		}
 
         addThumbsUpButtonListener();
         addThumbsDownButtonListener();
 
         ScrollView scroll = (ScrollView) findViewById(R.id.scrollView);
         scroll.setFadingEdgeLength(100);
         pubTextView= (TextView) findViewById(R.id.pub_name);
         descriptionTextView = (TextView) findViewById(R.id.description);
         openingHoursTextView = (TextView) findViewById(R.id.opening_hours);
         ageRestrictionTextView = (TextView) findViewById(R.id.age);
         entranceFeeTextView = (TextView) findViewById(R.id.entrance_fee);
         queueIndicator = (ImageView) findViewById(R.id.queueIndicator);
         votesUpTextView = (TextView) findViewById(R.id.thumbsUpTextView);
         votesDownTextView = (TextView) findViewById(R.id.thumbsDownTextView);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {

        System.out.println("FUCKEEEEER" + R.id.favorite_star);
         super.onCreateOptionsMenu(menu);
 
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.detailed, menu);
         favoriteStar = menu.findItem(R.id.favorite_star);
 
         refresh();
         return true;
     }
 
 	@Override
 	public void navigate(Class<?> destination) {
         Intent navigateBack = new Intent(this, destination);
         startActivity(navigateBack);
 
 	}
 
 	@Override
 	public void showErrorMessage(String message) {
 		// TODO This method should create a toast or some kind of window showing the error message
 	}
 
 	@Override
 	public void updateText(String pubName, String description, String openingHours, String age, String price) {
         pubTextView.setText(pubName);
         descriptionTextView.setText(description);
         openingHoursTextView.setText(openingHours);
         ageRestrictionTextView.setText(age);
         entranceFeeTextView.setText(price);
 	}
 
 	@Override
 	public void updateQueueIndicator(int queueTime) {
         switch(queueTime) {
             case 1:
                 queueIndicator.setBackgroundResource(R.drawable.detailed_queue_green);
                 break;
             case 2:
                 queueIndicator.setBackgroundResource(R.drawable.detailed_queue_yellow);
                 break;
             case 3:
                 queueIndicator.setBackgroundResource(R.drawable.detailed_queue_red);
                 break;
             default:
                 queueIndicator.setBackgroundResource(R.drawable.detailed_queue_gray);
                 break;
         }
 	}
 
 	@Override
 	public void navigate(Class<?> destination, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void refresh() {
         presenter.getQueueTime();
         presenter.getText();
         presenter.getThumbs();
         presenter.getFavoriteStar();
 
         try {
             presenter.getVotes();
         } catch (NoBackendAccessException e) {
             this.showErrorMessage(e.getMessage());
         } catch (NotFoundInBackendException e) {
             this.showErrorMessage(e.getMessage());
         }
     }
 
     /**
      * Adds listener to thumb up button.
      */
     public void addThumbsUpButtonListener(){
         thumbsUpButton = (ImageButton) findViewById(R.id.thumbsUpButton);
         thumbsUpButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view){
                 try {
 
                     presenter.ratingChanged(1);
                     presenter.getVotes();
 
                 } catch (NotFoundInBackendException e) {
                     showErrorMessage(e.getMessage());
                 } catch (NoBackendAccessException e) {
                     showErrorMessage(e.getMessage());
                 }
             }
         });
     }
 
     /**
      * Adds listener to thumb down button.
      */
     public void addThumbsDownButtonListener(){
         thumbsDownButton = (ImageButton) findViewById(R.id.thumbsDownButton);
         thumbsDownButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 try {
                     presenter.ratingChanged(-1);
                     presenter.getVotes();
                 } catch (NotFoundInBackendException e) {
                     showErrorMessage(e.getMessage());
                 } catch (NoBackendAccessException e) {
                 	showErrorMessage(e.getMessage());
                 }
             }
         });
     }
 
     /**
      * Updates the thumb button pictures.
      *
      * @param thumb Represents thumb up, thumb down or neither with 1, -1 or 0.
      */
     public void setThumbs(int thumb){
         switch (thumb){
             case -1:
                 thumbsDownButton.setBackgroundResource(R.drawable.thumb_down_selected);
                 thumbsUpButton.setBackgroundResource(R.drawable.thumb_up);
                 break;
             case 1:
                 thumbsUpButton.setBackgroundResource(R.drawable.thumb_up_selected);
                 thumbsDownButton.setBackgroundResource(R.drawable.thumb_down);
                 break;
             default:
                 thumbsDownButton.setBackgroundResource(R.drawable.thumb_down);
                 thumbsUpButton.setBackgroundResource(R.drawable.thumb_up);
                 break;
         }
     }
 
     /**
      * Updates the text showing number of votes.
      *
      * @param upVotes Number of up votes.
      * @param downVotes Number of down votes.
      */
     public void updateVotes(String upVotes, String downVotes){
         votesUpTextView.setText(upVotes);
         votesDownTextView.setText(downVotes);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem menuItem){
         switch(menuItem.getItemId()){
 
             case R.id.favorite_star:
                 presenter.saveFavoriteState();
                 presenter.getFavoriteStar();
 
             case R.id.refresh_info:
                 try {
                     presenter.updateInfo();
                 } catch (NoBackendAccessException e) {
                     this.showErrorMessage(e.getMessage());
                 } catch (NotFoundInBackendException e) {
                     this.showErrorMessage(e.getMessage());
                 }
                 refresh();
 
             case R.id.action_settings:
         }
         return true;
     }
 
     /**
      * Updates the star.
      * @param isStarFilled Represents if the star is filled or not.
      */
     public void updateStar(boolean isStarFilled){
 
         if(isStarFilled){
             favoriteStar.setIcon(R.drawable.star_not_filled);
         }
         else{
             favoriteStar.setIcon(R.drawable.star_filled);
         }
     }
 }
