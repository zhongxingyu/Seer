 package dk.aau.cs.giraf.train.profile;
 
 import java.util.ArrayList;
 
 import dk.aau.cs.giraf.train.R;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 /**
  * CustomiseLinearLayout is the class that handles and shows the list of stations ({@link Station}) in the customisation window.
  * @see StationConfiguration
  * @author Nicklas Andersen
  */
 public class CustomiseLinearLayout extends LinearLayout {
     
     private ArrayList<StationConfiguration> stations = new ArrayList<StationConfiguration>();
     private ArrayList<ImageButton> addPictogramButtons = new ArrayList<ImageButton>();
     private ArrayList<AssociatedPictogramsLayout> associatedPictogramsLayouts = new ArrayList<AssociatedPictogramsLayout>();
     
     public CustomiseLinearLayout(Context context, AttributeSet attrs) {
         super(context, attrs);
     }
     
     /**
      * Adds a station to the list.
      * @param station The station to add to the list.
      */
     public void addStation(StationConfiguration station) {
         this.stations.add(station);
         this.preventStationOverflow();
         
         LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View stationListItem = layoutInflater.inflate(R.layout.station_list_item, null);
         
         PictogramButton categoryPictogramButton = (PictogramButton) stationListItem.findViewById(R.id.list_category);
         categoryPictogramButton.bindStationAsCategory(station);
         
         AssociatedPictogramsLayout associatedPictogramsLayout = (AssociatedPictogramsLayout) stationListItem.findViewById(R.id.associatedPictograms);
         associatedPictogramsLayout.bindStation(station);
         this.associatedPictogramsLayouts.add(associatedPictogramsLayout);
         
         ImageButton addPictogramsButton = (ImageButton) stationListItem.findViewById(R.id.addPictogramButton);
         addPictogramsButton.setOnClickListener(new AddPictogramsClickListener(associatedPictogramsLayout));
         this.addPictogramButtons.add(addPictogramsButton);
         
         ImageView deleteButton = (ImageView) stationListItem.findViewById(R.id.deleteRowButton);
         deleteButton.setOnClickListener(new RemoveClickListener(station));
         
         super.addView(stationListItem);
     }
     
     public ArrayList<StationConfiguration> getStations() {
        return this.stations;
     }
     
     public void setStationConfigurations(ArrayList<StationConfiguration> stationConfigurations) {
         this.stations = new ArrayList<StationConfiguration>();
         super.removeAllViews();
         
         for (StationConfiguration station : stationConfigurations) {
             this.addStation(station);
         }
     }
     
     private void preventStationOverflow() {
         Button addStationButton = (Button) ((ProfileActivity) super.getContext()).findViewById(R.id.addStationButton);
         if(this.stations.size() >= ProfileActivity.ALLOWED_STATIONS) {
             addStationButton.setEnabled(false);
         } else {
             addStationButton.setEnabled(true);
         }
     }
     
     public int getTotalPictogramSize() {
         int result = 0;
         for (AssociatedPictogramsLayout pictogramLayout : this.associatedPictogramsLayouts) {
             result += pictogramLayout.getPictogramCount();
         }
         return result;
     }
     
     public void setVisibilityPictogramButtons(boolean visible) {
         for (ImageButton imageButton : this.addPictogramButtons) {
             if(visible) {
                 imageButton.setVisibility(ImageButton.VISIBLE);
             } else {
                 imageButton.setVisibility(ImageButton.INVISIBLE);
             }
             imageButton.setEnabled(visible);
         }
     }
     
     /**
      * Removes {@link StationConfiguration} at the specified index from the list.
      * @param index The index of the {@link StationConfiguration} in the list to remove.
      */
     public void removeStation(int index) {
         if(index > this.stations.size() - 1) {
             return;
         }
         this.removeViewAt(index);
         this.stations.remove(index);
         this.addPictogramButtons.remove(index);
         this.associatedPictogramsLayouts.remove(index);
         this.preventStationOverflow();
     }
     
     /**
      * Removes {@link Station} from the list.
      * @param station The {@link Station} that should be removed from the list.
      */
     public void removeStation(StationConfiguration station) {
         this.removeStation(this.stations.indexOf(station));
     }
     
     private final class AddPictogramsClickListener implements OnClickListener {
         private AssociatedPictogramsLayout associatedPictogramsLayout;
         
         public AddPictogramsClickListener(AssociatedPictogramsLayout associatedPictogramsLayout) {
             this.associatedPictogramsLayout = associatedPictogramsLayout;
         }
         
         @Override
         public void onClick(View v) {
             ((ProfileActivity) CustomiseLinearLayout.this.getContext()).startPictoAdmin(ProfileActivity.RECEIVE_SINGLE, this.associatedPictogramsLayout);
         }
     }
     
     private final class RemoveClickListener implements OnClickListener {
         
         private StationConfiguration station;
         
         public RemoveClickListener(StationConfiguration station) {
             this.station = station;
         }
         
         @Override
         public void onClick(View view) {
             CustomiseLinearLayout.this.removeStation(this.station);
             if(CustomiseLinearLayout.this.getTotalPictogramSize() < ProfileActivity.ALLOWED_PICTOGRAMS) {
                 CustomiseLinearLayout.this.setVisibilityPictogramButtons(true);
             }
         }
     }
 }
