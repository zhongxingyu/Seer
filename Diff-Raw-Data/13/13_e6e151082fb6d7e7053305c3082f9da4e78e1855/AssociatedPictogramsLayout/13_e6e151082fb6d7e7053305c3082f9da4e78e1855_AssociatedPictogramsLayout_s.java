 package dk.aau.cs.giraf.train.profile;
 
 import java.util.ArrayList;
 
 import dk.aau.cs.giraf.train.R;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.LinearLayout;
 
 /**
  * A layout containing the pictograms associated to the station.
  * @author Jesper Riemer Andersen
  *
  */
 public class AssociatedPictogramsLayout extends LinearLayout implements PictogramReceiver {
     
     private StationConfiguration station;
     private ArrayList<PictogramButton> pictogramButtons = new ArrayList<PictogramButton>();
     private CustomiseLinearLayout customiseLinearLayout;
     
     
     public AssociatedPictogramsLayout(Context context, AttributeSet attrs) {
         super(context, attrs);
         this.customiseLinearLayout = (CustomiseLinearLayout) ((ProfileActivity) super.getContext()).findViewById(R.id.customiseLinearLayout);
     }
     
     public int getPictogramCount() {
         return this.pictogramButtons.size();
     }
     
     public void bindStation(StationConfiguration station) {
         this.station = station;
        for (long id : station.getAcceptPictograms()) {
            this.addPictogram(id);
         }
     }
     
    public void addPictogram(long pictogramId) {
         PictogramButton pictogramButton = new PictogramButton(this.getContext());
         pictogramButton.setPictogram(pictogramId);
         pictogramButton.setRemovable(true);
         LayoutParams params = new LayoutParams(75, 75);
         params.setMargins(0, 0, 1, 0);
         pictogramButton.setLayoutParams(params);
         
         this.pictogramButtons.add(pictogramButton);
         super.addView(pictogramButton);
         
         this.bindPictograms(); //Update the station with the new pictogram
     }
     
     private void bindPictograms() {
         this.station.clearAcceptPictograms();
         
         for (PictogramButton pictogramButton : this.pictogramButtons) {
             this.station.addAcceptPictogram(pictogramButton.getPictogram().getPictogramID());
         }
     }
     
     @Override
     public void removeView(View view) {
         super.removeView(view);
         this.pictogramButtons.remove(view);
         this.station.removeAccepPictogram(((PictogramButton) view).getPictogramId());
         
         if(this.customiseLinearLayout.getTotalPictogramSize() < ProfileActivity.ALLOWED_PICTOGRAMS) {
             this.customiseLinearLayout.setVisibilityPictogramButtons(true);
         }
     }
     
     @Override
     public void receivePictograms(long[] pictogramIds, int requestCode) {
         for (long id : pictogramIds) {
             this.addPictogram(id);
             if(this.customiseLinearLayout.getTotalPictogramSize() >= ProfileActivity.ALLOWED_PICTOGRAMS) {
                 this.customiseLinearLayout.setVisibilityPictogramButtons(false);
                 break;
             }
         }
     }
 }
