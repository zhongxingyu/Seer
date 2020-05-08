 package se.chalmers.krogkollen.detailed;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.*;
 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.backend.NoBackendAccessException;
 import se.chalmers.krogkollen.backend.NotFoundInBackendException;
 import se.chalmers.krogkollen.help.HelpActivity;
 import se.chalmers.krogkollen.map.MapActivity;
 import se.chalmers.krogkollen.pub.IPub;
 import se.chalmers.krogkollen.pub.PubUtilities;
 import se.chalmers.krogkollen.utils.EQueueIndicator;
 
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
  * You should have received a copy of the GNU General Public License
  * along with Krogkollen.  If not, see <http://www.gnu.org/licenses/>.
  *
 <<<<<<< Updated upstream
  */
 
 /**
  * An activity for the detailed view.
  */
 public class DetailedActivity extends Activity implements IDetailedView {
 	
 	private IDetailedPresenter presenter;
     private TextView pubTextView, descriptionTextView,openingHoursTextView,
             ageRestrictionTextView, entranceFeeTextView, votesUpTextView, votesDownTextView;
     private ImageButton thumbsUpButton, thumbsDownButton;
     private ImageView queueIndicator;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_detailed);
         presenter = new DetailedPresenter();
         presenter.setView(this);
        try {
			presenter.setPub(getIntent().getStringExtra(MapActivity.MARKER_PUB_ID));
		} catch (NotFoundInBackendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoBackendAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
 
         refresh();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
 
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.detailed, menu);
 
         return true;
     }
 
 	@Override
 	public void navigate(Class<?> destination) {
         Intent navigateBack = new Intent(this, destination);
         startActivity(navigateBack);
 
 	}
 
 	@Override
 	public void showErrorMessage(String message) {
 		// TODO Auto-generated method stub
 		
 	}
 
     /**
      * Sets the pubs information into the detailed view
      */
 
 	@Override
 	public void updateText(String pubName, String description, String openingHours, String age, String price) {
         pubTextView.setText(pubName);
         descriptionTextView.setText(description);
         openingHoursTextView.setText(openingHours);
         ageRestrictionTextView.setText(age);
         entranceFeeTextView.setText(price);
 	}
 
 	@Override
 	public void updateRating() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void updateQueueIndicator(EQueueIndicator queueTime) {
         switch(queueTime) {
             case GREEN:
                 queueIndicator.setBackgroundResource(R.drawable.detailed_queue_green);
                 break;
             case YELLOW:
                 queueIndicator.setBackgroundResource(R.drawable.detailed_queue_yellow);
                 break;
             case RED:
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
         try {
             presenter.getVotes();
         } catch (NoBackendAccessException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (NotFoundInBackendException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public void addThumbsUpButtonListener(){
         thumbsUpButton = (ImageButton) findViewById(R.id.thumbsUpButton);
         thumbsUpButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view){
                 try {
 
                     presenter.ratingChanged(1);
                     presenter.getVotes();
 
                 } catch (NotFoundInBackendException e) {
                     e.printStackTrace();
                 } catch (NoBackendAccessException e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     public void addThumbsDownButtonListener(){
         thumbsDownButton = (ImageButton) findViewById(R.id.thumbsDownButton);
         thumbsDownButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 try {
 
                     presenter.ratingChanged(-1);
                     presenter.getVotes();
 
                 } catch (NotFoundInBackendException e) {
                     e.printStackTrace();
                 } catch (NoBackendAccessException e) {
                     e.printStackTrace();
                 }
 
             }
         });
     }
 
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
 
     public void updateVotes(String upVotes, String downVotes){
         votesUpTextView.setText(upVotes);
         votesDownTextView.setText(downVotes);
     }
 }
