 package se.chalmers.krogkollen.detailed;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.map.MapActivity;
 import se.chalmers.krogkollen.pub.IPub;
 import se.chalmers.krogkollen.pub.Pub;
 import se.chalmers.krogkollen.pub.PubUtilities;
 
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
  */
 
 /**
  * An activity for the detailed view.
  */
 public class DetailedActivity extends Activity implements IDetailedView {
 	
 	private IDetailedPresenter presenter;
     private IPub pub;
     private TextView pubTextView, descriptionTextView,openingHoursTextView,
             ageRestrictionTextView, entranceFeeTextView;
     private ImageButton thumbsUpButton;
     private ImageButton thumbsDownButton;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_detailed);
         String pubID = getIntent().getStringExtra(MapActivity.MARKER_PUB_ID);
         pub = PubUtilities.getInstance().getPub(pubID);
         updateText();
 
         presenter = new DetailedPresenter();
         presenter.setView(this);
 
         addThumbsUpButtonListener();
         addThumbsDownButtonListener();
 
         setThumbs(getSharedPreferences(pub.getID(), 0).getInt(pub.getID(), 0));
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
 		// TODO I denna ska ni l�gga koden som skickar tillbaka anv�ndaren till MapActivity
 		
 	}
 
 	@Override
 	public void showErrorMessage(String message) {
 		// TODO Auto-generated method stub
 		
 	}
 
     /**
      * Sets the pubs information into the detailed view
      */
 
 	@Override
 	public void updateText() {
         pubTextView= (TextView) findViewById(R.id.pub_name);
         pubTextView.setText(pub.getName());
         descriptionTextView = (TextView) findViewById(R.id.description);
         descriptionTextView.setText(pub.getDescription());
         openingHoursTextView = (TextView) findViewById(R.id.opening_hours);
         openingHoursTextView.setText((""+pub.getTodaysOpeningHour())+"-"+pub.getTodaysClosingHour());
         ageRestrictionTextView = (TextView) findViewById(R.id.age);
         ageRestrictionTextView.setText(""+pub.getAgeRestriction());
         entranceFeeTextView = (TextView) findViewById(R.id.entrance_fee);
         entranceFeeTextView.setText(""+pub.getEntranceFee());
 		
 	}
 
 	@Override
 	public void updateRating() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void updateQueueIndicator() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void navigate(Class<?> destination, Bundle extras) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void refresh() {
 		// TODO Auto-generated method stub
 		
 	}
 
     public void addThumbsUpButtonListener(){
         thumbsUpButton = (ImageButton) findViewById(R.id.thumbsUpButton);
         thumbsUpButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (getSharedPreferences(pub.getID(), 0).getInt(pub.getID(), 0)==1){
                     setThumbs(0);
                 }else{
                     setThumbs(1);
                 }
             }
         });
     }
 
     public void addThumbsDownButtonListener(){
         thumbsDownButton = (ImageButton) findViewById(R.id.thumbsDownButton);
         thumbsDownButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 presenter.ratingChanged(pub, -1);
                 if (getSharedPreferences(pub.getID(), 0).getInt(pub.getID(), 0)==-1){
                     setThumbs(0);
                 }else{
                     setThumbs(-1);
                 }
             }
         });
     }
 
     public void setThumbs(int thumb){
         saveThumbState(thumb);
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
 
     public void saveThumbState(int thumb){
         SharedPreferences.Editor editor = getSharedPreferences(pub.getID(), 0).edit();
         editor.putInt(pub.getID(), thumb);
         editor.commit();
     }
 

 }
