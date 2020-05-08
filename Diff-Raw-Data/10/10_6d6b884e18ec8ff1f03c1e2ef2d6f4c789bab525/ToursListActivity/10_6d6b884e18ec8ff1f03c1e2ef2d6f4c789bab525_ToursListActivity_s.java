 package com.iCompute.tour;
 
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.Application;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.AdapterView.OnItemClickListener;
 import com.iCompute.tour.ToursList;
 
 public class ToursListActivity extends ListActivity implements OnClickListener{ //OnItemClickListener{
 	//TODO need to make it so that changes made when updating a tour are visible when returning to the tours list
 	
 	
 	private ListView list;
 	private TourListAdapter adapter;
 	private boolean isSearch;
 	private int tourID;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tours_list_layout);
 		Intent intent=getIntent();
 		isSearch=!intent.getBooleanExtra("myTours", false);
 		if(isSearch)
 		{
 			//TODO search query from intent
 			CharSequence title="Search Query";
 			((TextView)findViewById(R.id.searchTitleToursListTextView)).setText(title);
 		}
 		else
 		{
 			((TextView)findViewById(R.id.searchTitleToursListTextView)).setText("My Tours");
 		}
 		
 
 		list=getListView();
 		
 		//list.setOnItemClickListener(this);
 		adapter=new TourListAdapter(this, ((TourApplication)getApplication()).tours, isSearch);
 		list.setAdapter(adapter);
 		
 	}
 	
 	
 	
 	//@Override
 	/*public void onItemClick(AdapterView<?> parent, View v, int position, long id){
 		switch(v.getId())
 		{
 		case R.id.delToursListItemImgButton:
 			tourToDelete=id;
 			showDialog(0);
 			break;
 		case R.id.editToursListItemButton:
 			editTour();
 			break;
 		case R.id.tourInfoToursListItemLL:
 			viewTour();
 			break;
 		}
 	}*/
 	
 	@Override
 	public void onResume(){
 		super.onResume();
 		//doesn't work as intended...
 		adapter.notifyDataSetChanged();
 		//maybe this will?
 		list=getListView();
 	}
 	
 	@Override
 	public void onClick(View v)
 	{
 		tourID=list.getPositionForView((View)v.getParent());
 		switch(v.getId())
 		{
 		case R.id.tourInfoToursListItemLL:
 			viewTour();
 			break;
 		case R.id.editToursListItemButton:
 			editTour();
 			break;
 		case R.id.delToursListItemImgButton:
 			showDialog(0);
 			break;
 		}
 	}
 	
 	@Override
 	public Dialog onCreateDialog(int i)
 	{
 		Dialog d;
 		switch(i)
 		{
 		case 0:
 			d=createDeleteConfirmDialog();
 			break;
 		default:
 			d=null;
 			break;
 		}
 		return d;
 	}
 	
 	private void viewTour()
 	{
 		Intent i=new Intent(ToursListActivity.this, ViewTourActivity.class);
 		i.putExtra("isLocal", !isSearch);
 		startActivity(i);
 	}
 	
 	private void editTour()
 	{
 		Intent i=new Intent(ToursListActivity.this, EditTourActivity.class);
 		i.putExtra("isUpdate", true);
 		i.putExtra("tourID", tourID);
 		startActivity(i);
 	}
 	
 	private void confirmDeleteTour()
 	{
 		adapter.removeItem(tourID);
 		tourID=-1;
 	}
 	private void cancelDeleteTour()
 	{
 		tourID=-1;
 	}
 	
 	private Dialog createDeleteConfirmDialog()
 	{
 		AlertDialog.Builder builder=new AlertDialog.Builder(this);
 		builder.setTitle("Delete Tour");
 		builder.setMessage("Are you sure you would like to delete this tour from your device?");
 		builder.setPositiveButton("Delete", new Dialog.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				confirmDeleteTour();
 				dialog.dismiss();
 			}
 		});
 		builder.setNegativeButton("Cancel", new Dialog.OnClickListener(){
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				cancelDeleteTour();
 				dialog.dismiss();
 			}
 		});
 		return builder.create();
 	}
 	
	//TODO modify this to display the list and bind the buttons correctly
 	private class TourListAdapter extends BaseAdapter implements ListAdapter{
 
 		private LayoutInflater mInflater;
 		private ToursList mToursList;
 		private boolean mIsSearch;
 		public TourListAdapter(Context context, ToursList tours, boolean isSearch){
 			mInflater= LayoutInflater.from(context);
 			mToursList = tours;
 			mIsSearch=isSearch;
 		}
 		
 		public void removeItem(int i)
 		{
 			if(i>=0&&i < mToursList.size())
 			{
 				mToursList.remove(i);
 				this.notifyDataSetChanged();
 			}
 		}
 		
 				
 		public ToursList getItems()
 		{
 			return mToursList;
 		}
 		
 		@Override
 		public int getCount() {
 			return mToursList.size();
 		}
 
 		@Override
 		public Tour getItem(int i) {
 			
 			return mToursList.get(i);
 		}
 
 		@Override //not sure what purpose this serves
 		public long getItemId(int i) {		
 			return i;
 		}
 		
 		
 		@Override
 		public View getView(int i, View v, ViewGroup group) {
 			ViewHolder holder; //create a new view holder
 			if(v==null||v.getTag()==null){ //if view is null
 				v=mInflater.inflate(R.layout.tour_list_item, null);
 				//holder contains references to the widgets, this is to minimize calls to findViewById
 				holder = new ViewHolder();
 				holder.mTitle=(TextView)v.findViewById(R.id.tourNameTourListItemTextView);
 				holder.mItem = (LinearLayout)v.findViewById(R.id.tourInfoToursListItemLL);
 				holder.mStopCount = (TextView)v.findViewById(R.id.stopCountTourListItemTextView);
 				holder.mHStopCount = (TextView)v.findViewById(R.id.handicapStopsTourListItemTextView); 
 				holder.mEditButton = (Button)v.findViewById(R.id.editToursListItemButton);
 				holder.mRemoveButton = (ImageButton)v.findViewById(R.id.delToursListItemImgButton);
 				
 				//add onClickListeners to each widget
 				holder.mItem.setOnClickListener(ToursListActivity.this);
 				holder.mEditButton.setOnClickListener(ToursListActivity.this);
 				holder.mRemoveButton.setOnClickListener(ToursListActivity.this);
 				
 				//set the title based on the tour
 				//TODO set other fields here
 				holder.mTitle.setText(getItem(i).getTitle().toString());
 				v.setTag(holder);
 			}else{
 				holder=(ViewHolder)v.getTag();
 			}
 			
 			/*if(mIsSearch)
 			{
 				view.findViewById(R.id.delToursListItemImgButton).setVisibility(View.GONE);
 				view.findViewById(R.id.editToursListItemButton).setVisibility(View.GONE);
 			}*/
 			//else// if tour is downloaded
 			//{
 				//findViewById(R.id.editToursListItemButton).setVisibility(View.GONE);
 			//}
 	
 			
 			return v;
 		}
 		
 		public class ViewHolder{
 			LinearLayout mItem;
 			TextView mTitle;
 			TextView mStopCount;
 			TextView mHStopCount;
 			Button mEditButton;
 			ImageButton mRemoveButton;
 		}
 	}
 
 }
