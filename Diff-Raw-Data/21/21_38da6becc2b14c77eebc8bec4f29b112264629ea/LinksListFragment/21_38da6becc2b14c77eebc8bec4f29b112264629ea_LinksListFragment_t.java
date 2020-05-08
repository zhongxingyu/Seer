 package com.app.example.bookmarksWallet.fragments;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import com.actionbarsherlock.ActionBarSherlock;
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.ActionMode;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.widget.SearchView;
 import com.actionbarsherlock.widget.ShareActionProvider;
 import com.app.example.bookmarksWallet.FragmentChangeActivity;
 import com.app.example.bookmarksWallet.R;
 import com.app.example.bookmarksWallet.models.Link;
 import com.app.example.common.lib.SharedData;
 import com.app.example.db.lib.ActionLogDbAdapter;
 import com.app.example.db.lib.DatabaseAdapter;
 import com.app.example.db.lib.DatabaseConnectionCommon;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 @SuppressLint("NewApi")
 public class LinksListFragment extends SherlockFragment {
 	private static final String TAG = "LinksListFragment_TAG";
 	ActionBarSherlock mSherlock=ActionBarSherlock.wrap(getActivity());
 	public DatabaseAdapter db;
 	public ActionLogDbAdapter actionLogDb;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.links_list_layout, null);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		db=new DatabaseAdapter(getActivity());
 		actionLogDb=new ActionLogDbAdapter(getActivity());
 		
 		createLayout(db);
 	}
 	
     public void createLayout(DatabaseAdapter db){
     	final ListView linksListView = (ListView)getActivity().findViewById(R.id.linksListId);
 //    	ArrayList<Link> linksDataList=SharedData.getLinksListStatic();
     	
     	ArrayList<Link> linksDataList = DatabaseConnectionCommon.getLinksWrappLocalDb(db);
     	if(linksDataList==null){
     		Log.d(TAG,"set list from JSON data");
     		//start dialog
     		LoadingDialog m = new LoadingDialog();
             m.show(getFragmentManager(), "EditLinkDialog");
     		try{
 	    		//TODO change iconPath on DB
 				linksDataList = DatabaseConnectionCommon.getLinksListFromJSONData();
 				for(Link link:linksDataList)
 					DatabaseConnectionCommon.insertLinkWrappLocalDb(db,actionLogDb,link);
 				
 				//TODO fix it - clear all Log
				DatabaseConnectionCommon.deleteAllActionLogWrappLocalDb(actionLogDb);
				
 				//close dialog
 	    	}catch(Exception e){
 	    		Log.e(TAG,"error - " + e);
 	    	}
 			m.dismiss();
     	}
     	//TEST - empty list
 //    	if(linksDataList==null){
 //    		Log.d(TAG,"set list TEST data");
 //    		linksDataList=testLinksList();
 //    	}
 
 //    	if(linksDataList!=null && linksDataList.size()==0){
     	if(linksDataList==null){
     		//TODO handle empty - need to be refreshed
 //    		((TextView)getActivity().findViewById(R.id.linkList_empty_label)).setText("Empty List");
     		toastMessageWrapper("empty List - img");
 
     		//TEST
     		Link emptyLink=new Link(SharedData.EMPTY_LINKID,SharedData.EMPTY_STRING,"Empty list - sorry",SharedData.EMPTY_STRING,SharedData.EMPTY_LINKID,SharedData.EMPTY_STRING,false);
     		linksDataList=new ArrayList<Link>();
     		linksDataList.add(emptyLink);
     	}
     	
     	Collections.reverse((List<Link>)linksDataList);
     	ArrayAdapter<Link> adapter=new LinkCustomAdapter(getActivity(), R.layout.link_row, linksDataList);
 		linksListView.setAdapter(adapter);
 //		linksListView.setFocusableInTouchMode(true);
     	SharedData.setLinksList(linksDataList);
     	getSherlockActivity().registerForContextMenu(linksListView);
     }
 	/**OPEN LINK***/
 	public void openLinkOnBrowser(String linkUrl){
 		toastMessageWrapper("get link on Browser - "+linkUrl);
 		try{
     		//TEST - print out the text of obj selected
     		toastMessageWrapper("URL selected "+linkUrl);
     		//check if linkUrl is right parsed :D
     		if(checkURL(linkUrl)){
         		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
         		getActivity().startActivity(browserIntent);
     		}else
         		toastMessageWrapper("your URL is wrong "+linkUrl);
 
 		}catch(Exception e){
     		Log.e(TAG,"error - " + e);
     		toastMessageWrapper("I cant load your URL "+ e);
 		}
 	}
     //TODO TO BE IMPLEMENTED !!
     public boolean checkURL(String linkUrl){
     	//check URL with regex
     	return true;
     }
 	/**DELETE LINK*/
 	public boolean deleteLink(Link linkObj, ListView linksListView,boolean isNetworkAvailable){
 		/*if(!isNetworkAvailable){
 			NetworkNotAvailableDialog m = new NetworkNotAvailableDialog();
 	        m.show(getFragmentManager(), "NetworkNotAvailableDialog");
 			return false;
 		}*/
 		DeleteLinkDialog m = new DeleteLinkDialog();
         m.show(getFragmentManager(), "DeleteLinkDialog");
 		return true;
 	}
 	/**DELETE LINK*/
 	public boolean deleteAllLinks(){
 		DeleteAllLinksDialog m = new DeleteAllLinksDialog();
         m.show(getFragmentManager(), "DeleteLinkDialog");
 		return true;
 	}
 	/**EDIT LINK***/
 	public boolean editLink(){
 		EditLinkDialog m = new EditLinkDialog();
         m.show(getFragmentManager(), "EditLinkDialog");
 		return true;
 	}
 	/**SHARE LINK***/
 	public boolean shareLink(Link linkObj,MenuItem item){
 		//TODO need to be fixed - it doesnt work
         ShareActionProvider actionProvider = (ShareActionProvider) item.getActionProvider();
         if(actionProvider==null){
         	Log.d(TAG, "providerNull");
         	return false;
         }
 //        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
         // Note that you can set/change the intent any time,
         // say when the user has selected an image.
         actionProvider.setShareIntent(createShareIntent(linkObj.getLinkUrl()));
 		return true;
 	}
 	/*** Creates a sharing {@link Intent}.*/
     private Intent createShareIntent(String uri) {
         Intent shareIntent = new Intent(Intent.ACTION_SEND);
 //        shareIntent.setType("image/*");
 //        Uri uri = Uri.fromFile(getFileStreamPath("shared.png"));
         shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
         return shareIntent;
     }
 	
 //    public void onBackPressed(){
 //    	super.onBackPressed();
 //    	Log.v("MY_TAG","back_pressed");
 //    	return;
 //    }    
 	/**TOAST MESSAGGE WRAPPER**/
 	private void toastMessageWrapper(String message){
 		Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
 	}
     /**CUSTOM LAYOUT CLASS**/
 	public class LinkCustomAdapter extends ArrayAdapter<Link> {
 		public LinkCustomAdapter(Context context, int linkViewResourceId,
                 ArrayList<Link> items) {
 			super(context, linkViewResourceId, items);
 		}
 		
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			if (convertView == null) 
 				convertView = LayoutInflater.from(getContext()).inflate(R.layout.link_row, null);
 			
 			TextView linkTitle = (TextView)convertView.findViewById(R.id.link_title_id);
 			linkTitle.setText(getItem(position).getLinkName());
 
 			//attach event to actionLayout and preview layout
 			convertView.findViewById(R.id.link_action_layout_id).setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View view) {
 					toastMessageWrapper("get links action bottom menu");
 //					view.setBackgroundColor(Color.parseColor("#ff0000"));
 					Activity activity = getActivity();
 					if(activity instanceof FragmentChangeActivity) {
 						Log.i(TAG, "TextView clicked on row " + position);
 						SharedData.setLinkPosition(position);
 				    	getSherlockActivity().startActionMode(new linkOverActionBar());
 					}
 				}
 			});
 			//attach event to actionLayout and preview layout
 			final String linkUrlFinal=getItem(position).getLinkUrl();
 			convertView.findViewById(R.id.link_preview_layout_id).setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					openLinkOnBrowser(linkUrlFinal);
 				}
 			});
 			//checkBox
 //			convertView.setOnLongClickListener(new View.OnLongClickListener() {
 //				@Override
 //				public boolean onLongClick(View arg0) {
 //					// TODO Auto-generated method stub
 //					toastMessageWrapper("show checkbox mode");
 //					return false;
 //				}
 //			});
 
 			return convertView;
 		}
 	}
 
 	/**OVER ACTION BAR impl**/
 	public final class linkOverActionBar implements ActionMode.Callback {
 	    private static final String TAG ="linkOverActionBar_TAG";
 
 		@Override
 	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 	        //Used to put dark icons on light action bar
 	        int index=0;
 	        int order=0;
 	        
 	        menu.add(0,index++,order++,"Share")
 	            .setIcon(android.R.drawable.ic_menu_share)
 	            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
 	        menu.add(0,index++,order++,"Edit")
 	            .setIcon(android.R.drawable.ic_menu_edit)
 	            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
 	        menu.add(0,index++,order++,"Delete")
 	            .setIcon(android.R.drawable.ic_menu_delete)
 	            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 	        menu.add(0,index++,order++,"Delete All")
 	            .setIcon(android.R.drawable.ic_menu_manage)
 	            .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 
 	        return true;
 	    }
 
 	    @Override
 	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
 	        return false;
 	    }
 
 	    @Override
 	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 	    	ListView linksListView = (ListView)getActivity().findViewById(R.id.linksListId);
 	    	if(SharedData.getLinkPosition()!=SharedData.LINK_NOT_IN_LIST){
 		    	Link linkObj = (Link) linksListView.getAdapter().getItem(SharedData.getLinkPosition());
 		    	switch(item.getItemId()){
 		    	case 0:
 		    		//SHARE opt
 //		    		toastMessageWrapper("Share your link on ....");
 		    		if(linkObj!=null)
 		    			if(!shareLink(linkObj,item))
 		    				toastMessageWrapper("Item sharing Failed- "+linkObj.getLinkName());
 		    		break;
 		    	case 1:
 		    		//EDIT opt
 //		    		toastMessageWrapper("Edit your link");
 		    		if(linkObj!=null)
 		    			if(!editLink())
 		    				toastMessageWrapper("Item edit Failed- "+linkObj.getLinkName());
 		    		
 //    				toastMessageWrapper("Edit on menu - Link not found on ListView");
 		    		break;
 		    	case 2:
 		    		//DEL opt
 //		    		toastMessageWrapper("Delete this link");
 		    		if(linkObj!=null)
 		    			if(!deleteLink(linkObj,linksListView, SharedData.isNetworkAvailable(getActivity())))
 		    				toastMessageWrapper("Item del Failed- "+linkObj.getLinkName());
 //    				toastMessageWrapper("Delete on menu - Link not found on ListView");
 		    		break;
 		    	case 3:
 		    		//DEL opt
 //		    		toastMessageWrapper("Delete this link");
 		    		if(linkObj!=null)
 		    			if(!deleteAllLinks())
 		    				toastMessageWrapper("Item del Failed- "+linkObj.getLinkName());
 //    				toastMessageWrapper("Delete on menu - Link not found on ListView");
 		    		break;
 
 		    	}
 	    	}
 	    	Log.d(TAG, ""+ item.getTitle());
 	    	mode.finish();
 	        return true;
 	    }
 
 		@Override
 		public void onDestroyActionMode(ActionMode mode) {
 			// TODO Auto-generated method stub
 			
 		}
 
 	}
     /***NO NETWORK DIALOG FRAGMENT***/
     public class NetworkNotAvailableDialog extends SherlockDialogFragment{
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
         	AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
         	
             builder.setTitle("Info");
             builder.setMessage("Internet not available, Cross check your internet connectivity and try again");
             builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
             	   dialog.cancel();
 
                }
             });
             
             return builder.create();
         }
     }
     /***NO NETWORK DIALOG FRAGMENT***/
     public class LoadingDialog extends SherlockDialogFragment{
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
         	AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
         	
             builder.setTitle("Info");
             builder.setMessage("...... loading data");
             return builder.create();
         }
     }
     /***EDIT DIALOG FRAGMENT***/
     public class EditLinkDialog extends SherlockDialogFragment{
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
         	AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
         	
             LayoutInflater inflater = getActivity().getLayoutInflater();
             final View view = inflater.inflate(R.layout.edit_link_dialog_layout, null);
             builder.setView(view);
             builder.setMessage("Change your Link title")
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                     	   	try{
 	                    	   //get new link title
 								ListView linksListView = (ListView)getActivity().findViewById(R.id.linksListId);
 								Link linkObj = (Link) linksListView.getAdapter().getItem(SharedData.getLinkPosition());
 								String linkNameMod=((EditText) view.findViewById(R.id.editLink_title_editText_dialog_id)).getText().toString();
 								linkObj.setLinkName(linkNameMod);
 								DatabaseConnectionCommon.updateLinkByIdWrappLocalDb(db,actionLogDb, linkObj,getActivity().getSharedPreferences(SharedData.LOG_DB, 0));
 								linksListView.refreshDrawableState();
 								Log.d(TAG,linkNameMod);
                     	   	}catch(Exception e){
                     	   		toastMessageWrapper("failed edit Link - editLinkDialog");
                     	   		Log.d(TAG,"failed edit Link");
                     	   		dialog.cancel();
                     	   	}
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                     	   dialog.cancel();
                        }
                    });
             
             return builder.create();
         }
     }
     /***DELETE DIALOG FRAGMENT***/
     public class DeleteLinkDialog extends SherlockDialogFragment{
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             // Use the Builder class for convenient dialog construction
             AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
             // Create the AlertDialog object and return it
             builder.setMessage("Sure to delete Link?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
 	                   	   try{
 	                    	   //get new link title
 								ListView linksListView = (ListView)getActivity().findViewById(R.id.linksListId);
 								Link linkObj = (Link) linksListView.getAdapter().getItem(SharedData.getLinkPosition());
 								if(DatabaseConnectionCommon.deleteLinkByIdWrappLocalDb(db,actionLogDb,linkObj.getLinkId(),getActivity().getSharedPreferences(SharedData.LOG_DB, 0))){
 									((LinkCustomAdapter) linksListView.getAdapter()).remove(linkObj);
 									linksListView.refreshDrawableState();
 	    							//SharedData.removeLink(linkObj);
 									//delete from local db
 									toastMessageWrapper("Item deletedx - "+linkObj.getLinkName());
 								}
 	                   	   }catch(Exception e){
 	                   	   		toastMessageWrapper("failed edit Link - editLinkDialog");
 	                   	   		Log.d(TAG,"failed delete Link");
 	                   	   		dialog.cancel();
 	                   	   }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                     	   dialog.cancel();
                        }
                    });
             
             return builder.create();
         }
 
     }
     /***DELETE DIALOG FRAGMENT***/
     public class DeleteAllLinksDialog extends SherlockDialogFragment{
 
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             // Use the Builder class for convenient dialog construction
             AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
             // Create the AlertDialog object and return it
             builder.setMessage("Sure to delete Link?")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
 	                   	   try{
 								ListView linksListView = (ListView)getActivity().findViewById(R.id.linksListId);
 								if(DatabaseConnectionCommon.deleteLinksWrappLocalDb(db)){
 									for(int i=0;i<linksListView.getCount();i++){
 										Link linkObj = (Link) linksListView.getAdapter().getItem(i);
 										((LinkCustomAdapter) linksListView.getAdapter()).remove(linkObj);
 									}
 									linksListView.refreshDrawableState();
 	    							//SharedData.removeLink(linkObj);
 									//delete from local db
 									toastMessageWrapper("All items deleted - ");
 								}
 	                   	   }catch(Exception e){
 	                   	   		toastMessageWrapper("failed edit Link - editLinkDialog");
 	                   	   		Log.d(TAG,"failed delete Link");
 	                   	   		dialog.cancel();
 	                   	   }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                     	   dialog.cancel();
                        }
                    });
             
             return builder.create();
         }
 
     }
 
     
 	/**CONTEXT MENU**/
 /*    @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         //This uses the imported MenuItem from ActionBarSherlock
         Toast.makeText(getActivity(), "Got click: " + item.toString(), Toast.LENGTH_SHORT).show();
         return true;
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
         menu.add("One");
         menu.add("Two");
         menu.add("Three");
         menu.add("Four");
     }
 
     @Override
     public boolean onContextItemSelected(android.view.MenuItem item) {
         //Note how this callback is using the fully-qualified class name
         Toast.makeText(getActivity(), "Got click: " + item.toString(), Toast.LENGTH_SHORT).show();
         return true;
     }*/
 
 	/**BOTTOM static menu**/
     public boolean onCreateOptionsMenu(android.view.Menu menu) {
 		return mSherlock.dispatchCreateOptionsMenu(menu);
     }
 
     @Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		 int index=0,order=0;
 		 
 		 
         //Create the search view
         SearchView searchView = new SearchView(getActivity().getActionBar().getThemedContext());
         searchView.setQueryHint("Search for countries");
 
 		 menu.add(0,(index++),order++,"Refresh")
 		 	.setIcon(android.R.drawable.ic_menu_rotate)
 		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM );
 
 		 menu.add(0,(index++),order++,"Search")
 		 	.setIcon(android.R.drawable.ic_menu_search)
             .setActionView(searchView)
 		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM );
 
 //		 menu.add(0,index++,order++,"Save")
 //		 .setIcon(android.R.drawable.ic_menu_add)
 //		 .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 		
 //		 menu.add(0,index++,order++,"Delete FOOTER")
 //		    .setIcon( android.R.drawable.ic_menu_delete)
 //		    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
 	 }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 //		 AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 //		 final ListView linksListView = (ListView)getActivity().findViewById(R.id.linksListId);
 //		 ListAdapter linksListAdapter = linksListView.getAdapter();
 //		 // Retrieve the item that was clicked on
 //		 Link linkObj = (Link) linksListAdapter.getItem(info.position);
     	
     	
     	switch(item.getItemId()){
 	    	case 0:
 	    		toastMessageWrapper("Refresh list action");
 	    		break;
 //	    	case 1:
 //	    		toastMessageWrapper("Delete action");
 //	    		deleteLink(null);
 //	    		break;
     	}
     	return true;
     }
 /*
     OnMenuItemClickListener deleteLinkClickListener = new OnMenuItemClickListener() {
         @Override
         public boolean onMenuItemClick(MenuItem item) {  
 //        	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 //	   		final ListView linksListView = (ListView)getActivity().findViewById(R.id.linksListId);
 //	   		ListAdapter linksListAdapter = linksListView.getAdapter();
 	   		// Retrieve the item that was clicked on
 //	   		if(linksListAdapter!=null){
 //	   			Link linkObj = (Link) linksListAdapter.getItem(info.position);
 //	   			deleteLink(linkObj,linksListView);
 //	   		}
             return false;
         }
     };*/
 
     /**list view actions**/
 /*
 	convertView.findViewById(R.id.link_action_layout_id).setOnLongClickListener(new View.OnLongClickListener() {
 	@Override
 	public boolean onLongClick(View view) {
 		// TODO Auto-generated method stub
 		registerForContextMenu(view);
 		view.showContextMenu();
 		return true;
 	}
 });
 
 // set the listview not scrollable - lock the touch on 
 convertView.findViewById(R.id.link_action_layout_id).setOnTouchListener(new View.OnTouchListener() {
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
         int action = event.getAction();
         switch (action) {
         case MotionEvent.ACTION_DOWN:
             // Disallow ScrollView to intercept touch events.
             v.getParent().requestDisallowInterceptTouchEvent(true);
             break;
 
         case MotionEvent.ACTION_UP:
             // Allow ScrollView to intercept touch events.
             v.getParent().requestDisallowInterceptTouchEvent(false);
             break;
         }
         // Handle ListView touch events.
         v.onTouchEvent(event);
         return true;
 	}
 });
 */			    
 
 	/**TEST population*/
     public ArrayList<Link> testLinksList(){
     	ArrayList<Link> linksDataList = new ArrayList<Link>();
     	ArrayList<String> linksUrlArray= new ArrayList<String>();
 		boolean deletedLinkFlag=false;
 
     	linksUrlArray.add("heavy metal1");
 		linksUrlArray.add("pop1");
 		linksUrlArray.add("underground");
 		linksUrlArray.add("heavy metal");
 		linksUrlArray.add("underground");
 		linksUrlArray.add("hey_ure_fkin_my_shitty_dog_are_u_sure_u_want_to_cose_ure_crazy");
 		linksUrlArray.add("bla1");
 		linksUrlArray.add("link2");
     	String linkUrl="http://www.google.it";
     	int userId=0;
     	for(int i=0;i<linksUrlArray.size();i++)
        		linksDataList.add(new Link(i,"ic_launcher", linksUrlArray.get(i),linkUrl,userId,"del_icon",deletedLinkFlag));
     	
     	return linksDataList;
     }
     
 }
