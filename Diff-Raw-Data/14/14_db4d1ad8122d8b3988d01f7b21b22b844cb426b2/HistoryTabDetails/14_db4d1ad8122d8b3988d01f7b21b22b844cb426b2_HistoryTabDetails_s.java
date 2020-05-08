 package edu.upenn.cis573;
 
 import java.util.Date;
 
 import edu.upenn.cis573.database.DBManager;
 import edu.upenn.cis573.datastructure.Room;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.DialogInterface.OnClickListener;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class HistoryTabDetails extends Fragment {
     private StudySpace o;
     private EditText showNoteText;
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.historyviewtabdetails, container, false);
         return view;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         Intent i = getActivity().getIntent();
         o = (StudySpace) i.getSerializableExtra("STUDYSPACE");
 
         TextView tt = (TextView) getView().findViewById(R.id.spacename_history);
         tt.setText(o.getBuildingName());
 
         TextView rt = (TextView) getView().findViewById(R.id.roomtype_history);
         rt.setText(o.getSpaceName());
 
         TextView rn = (TextView) getView().findViewById(R.id.roomnumbers_history);
 
         rn.setText(o.getRoomNames());
 
         TextView mo = (TextView) getView().findViewById(R.id.maxoccupancy_history);
         mo.setText("Maximum occupancy: "+o.getMaximumOccupancy());
 
         TextView pi = (TextView) getView().findViewById(R.id.privacy_history);
         ImageView private_icon = (ImageView) getView().findViewById(R.id.private_icon_history);
         if(o.getPrivacy().equals("S")){
             pi.setText("This study space is a common Space");
             if(private_icon!=null){
                 Resources resource = getResources();
                 int resID = resource.getIdentifier("icon_no_private", "drawable", getActivity().getPackageName() );
                 private_icon.setImageResource(resID);
             }
         }else{
             pi.setText("Private space");
             if(private_icon!=null){
                 Resources resource = getResources();
                 int resID = resource.getIdentifier("icon_private", "drawable", getActivity().getPackageName() );
                 private_icon.setImageResource(resID);
             }
         }
 
         TextView res = (TextView) getView().findViewById(R.id.reservetype_history);
         if(o.getReserveType().equals("N")){
             res.setText("This study space is non-reservable.");
         }else{ 
             res.setText("This study space can be reserved.");
         }
         TextView wb = (TextView) getView().findViewById(R.id.whiteboard_history);
         ImageView wb_icon = (ImageView) getView().findViewById(R.id.whiteboard_icon_history);
         if(o.hasWhiteboard()){
             if(wb_icon!=null){
                 Resources resource = getResources();
                 int resID = resource.getIdentifier("icon_whiteboard", "drawable", getActivity().getPackageName() );
                 wb_icon.setImageResource(resID);
             }
             wb.setText("This study space has a whiteboard.");
         }else{
             wb.setText("This study space does not have a whiteboard.");
             if(wb_icon!=null){
                 Resources resource = getResources();
                 int resID = resource.getIdentifier("icon_no_whiteboard", "drawable", getActivity().getPackageName() );
                 wb_icon.setImageResource(resID);
             }
         }
 
         TextView com = (TextView) getView().findViewById(R.id.computer_history);
         ImageView com_icon = (ImageView) getView().findViewById(R.id.computer_icon_history);
         if(o.hasComputer()){
             com.setText("This study space has a computer.");
             if(com_icon!=null){
                 Resources resource = getResources();
                 int resID = resource.getIdentifier("icon_computer", "drawable", getActivity().getPackageName() );
                 com_icon.setImageResource(resID);
             }
         }else{
             com.setText("This study space does not have computers.");
             if(com_icon!=null){
                 Resources resource = getResources();
                 int resID = resource.getIdentifier("icon_no_computer", "drawable", getActivity().getPackageName() );
                 com_icon.setImageResource(resID);
             }
         }
 
         TextView proj = (TextView) getView().findViewById(R.id.projector_history);
         ImageView proj_icon = (ImageView) getView().findViewById(R.id.projector_icon_history);
         if(o.has_big_screen()){
             proj.setText("This study space has a big screen.");
             if(proj_icon!=null){
                 Resources resource = getResources();
                 int resID = resource.getIdentifier("icon_projector", "drawable", getActivity().getPackageName() );
                 proj_icon.setImageResource(resID);
             }
         }else{
             proj.setText("This study space does not have a big screen.");
             if(proj_icon!=null){
                 Resources resource = getResources();
                 int resID = resource.getIdentifier("icon_no_projector", "drawable", getActivity().getPackageName() );
                 proj_icon.setImageResource(resID);
             }
         }
 
         View an = (View) getView().findViewById(R.id.availablenow_history);
         if(an != null){
             boolean availableNow = false;
             for(Room r : o.getRooms()){
                 try {
                     if(r.availableNow())
                         availableNow = true;
                 } catch (Exception e) {
                     availableNow = false;   //Calendar crashes
                 }
             }
             if(availableNow)
                 an.setVisibility(View.VISIBLE);
             else
                 an.setVisibility(View.GONE);
         }
 
         //displays date of last reservation
         TextView lastReservation = (TextView) getView().findViewById(R.id.last_reservation_time);
         lastReservation.setText(new Date(o.getStartDate()).toString());
 
         showNoteText = (EditText)getView().findViewById(R.id.note);
         showNoteText.setText(o.getNote());
 
         Button saveButton = (Button)getView().findViewById(R.id.savenote1);
         saveButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 String noteText = showNoteText.getText().toString();
 
                 Log.i("In history details page building name is: ", o.getBuildingName());
                 if(DBManager.updateDbWithSpecficEntry(o, noteText) == -1) {
                     showClearHistoryDialog();
                 }else {
                     showSaveDialog();
                 }
             }
         });
     }
 
     public void showSaveDialog(){
         AlertDialog.Builder builder = new Builder(getActivity());
         builder.setMessage("Your note has been saved!");
         builder.setTitle("Confirmation Dialog");
 
         builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();                           
             }
         });     
         builder.create().show();
     }
 
     public void showClearHistoryDialog() {
         final Context currContext = this.getActivity();
         new AlertDialog.Builder(currContext)
         .setMessage("The database if full, would you like to clear history? (This action is unrecoverable)")
         .setTitle("Database Full")
         .setPositiveButton("Yes", new OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 if (DBManager.isEmpty()) {
                     new AlertDialog.Builder(currContext)
                     .setTitle("No History")
                     .setMessage("Current history is empty!")
                     .show();
                 }else {
                     DBManager.clearDB();
                     ((Activity)currContext).finish();
                 }
             }
         })
         .show();
     }
 
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
     }
 
 }
