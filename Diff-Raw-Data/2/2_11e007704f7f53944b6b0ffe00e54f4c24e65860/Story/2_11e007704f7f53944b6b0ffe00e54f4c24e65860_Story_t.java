 package com.youpony.amuse;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 
 import com.origamilabs.library.views.StaggeredGridView;
 import com.youpony.amuse.adapters.ItemsAdapter;
 
 /*
  * fragment containing Story.
  * default landing page for the app.
  */
 public class Story extends Fragment {
 	
 	
 //	private ListView listViewLeft;
 //	private ListView listViewRight;
 	
 	//call Story.<leftorright>Adapter.notifyDataChanged() from outer classes when item list is modified
 //	public static ItemsAdapter leftAdapter;
 //	public static ItemsAdapter rightAdapter;
 
 //	int[] leftViewsHeights;
 //	int[] rightViewsHeights;
 	
 //	ListView lv1;
 	//public static ArrayAdapter<Item> files;
 	public static int id_mostra;
 	public static boolean start = true;
 	int current = 0;
 	int removable;
 	
 	public static String[] debugArray;
 	public static Button send;
 	private StaggeredGridView pinterest;
 	public static ItemsAdapter pinterestAdapter;
 	public static RelativeLayout buttonLayout;
 	public static ImageView tutorial;
 	
 	private View sView;
 	
 	public Story(){
 		
 		
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
 
 		//DEBUGGING PURPOSES
		debugArray = new String[]{"0&4"};//, "0&6", "0&7", "0&4", "0&1", "0&3"};//,"0&33","0&41","0&27","0&14","0&13","0&26","0&28","0&29","0&17","0&15","0&16","0&18","0&9"};
 
 
 		sView = inflater.inflate(R.layout.activity_story, container, false);
 		pinterest = (StaggeredGridView) sView.findViewById(R.id.staggeredGridView);
 		send = (Button) sView.findViewById(R.id.send_button);
 //		line = (View) sView.findViewById(R.id.lineaStory);
 		Story.send.setVisibility(View.INVISIBLE);
 		
 		buttonLayout = (RelativeLayout) sView.findViewById(R.id.buttonLine);
 //		Story.line.setVisibility(View.INVISIBLE);
 		
 		for(int i=0; i<debugArray.length; i++){
 			Intent qrResult = new Intent(PageViewer.getAppContext(), QrResult.class);
 	  		String resultString = debugArray[i];
 	  		qrResult.putExtra(PageViewer.EXTRA_MESSAGE, resultString);
 	  		startActivity(qrResult);
 		}
 		
 		tutorial = (ImageView) sView.findViewById(R.id.tutorial);
 		
 		pinterest.setOnItemClickListener(onItemClick);
 		pinterest.setOnItemLongClickListener(longListener);
 		
 		send.setText("Send story");
 		
 		send.setOnClickListener(sendListener);
 		
 		loadItems();
 
 		
 		return sView;
 	}
 	
 	//on click listener (accessing object infos)
 		StaggeredGridView.OnItemClickListener onItemClick = new StaggeredGridView.OnItemClickListener(){
 
 			@Override
 			public void onItemClick(StaggeredGridView parent, View view,
 					int position, long id) {
 					Intent info = new Intent(PageViewer.getAppContext(), ItemInfo.class);
 					info.putExtra("pos", position);
 					startActivity(info);
 			}
 
 			
 		
 		};
 	
 	
 	//SEND STORY LISTENER
 	OnClickListener sendListener = new OnClickListener(){
 
 		@Override
 		public void onClick(View v) {
 			if(PageViewer.values.size() != 0 ){
 				Intent sendStory = new Intent(PageViewer.getAppContext(), SendStory.class);
 				startActivity(sendStory);
 			}
 		}
 	
 	};
 	
 	
 	//Listener for deleting object from list
 	StaggeredGridView.OnItemLongClickListener longListener = new StaggeredGridView.OnItemLongClickListener() {
 		
 		@Override
 		public boolean onItemLongClick(StaggeredGridView parent, View view,
 				int position, long id) {
 			removable = position;
 			
 			AlertDialog.Builder adb=new AlertDialog.Builder(getActivity());
             adb.setTitle("Delete?");
             
             
             adb.setMessage("Are you sure you want to delete " + position);
             
             adb.setNegativeButton("Cancel", null);
             adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                 public void onClick(DialogInterface dialog, int which) {
                 	
                 	
                 	PageViewer.values.remove(removable);
                 	PageViewer.pinterestItems.remove(removable);
                 	pinterestAdapter.notifyDataSetChanged();
                 	if(PageViewer.values.size() == 0){
                 		start = true;
                 		Story.send.setVisibility(View.INVISIBLE);
 //                		Story.line.setVisibility(View.INVISIBLE);
 
                 	}
                 	Log.i("orrudebug", "cancellato l'oggetto " + removable);
                 }});
                 
             adb.show();
 			return false;
 		}
 	};    
 	
 	
 	
 	private void loadItems(){
 		
 		pinterestAdapter = new ItemsAdapter(this.getActivity(), R.layout.item, PageViewer.pinterestItems );
 		pinterest.setAdapter(pinterestAdapter);	
 	}
 	
 	public static void setButtonInvisible(){
 		send.setText("no invio");
 	}
     
     public static void setButtonVisible(){
     	send.setText("si invio");
     }
 	
 	
 	public static boolean findName(String name) {
 		//Log.i("orrudebug", "size : " + PageViewer.values.size());
 		for(int i=0; i<PageViewer.values.size(); i++){
 			//Log.i("orrudebug", name + " " + PageViewer.values.get(i));
 			if(PageViewer.values.get(i).name.equals(name)){
 				//Log.i("orrudebug", "sto iterando, ho trovato");
 				
 				return true;
 			}
 		}
 		return false;
 	}
 	public static int findPos(String name){
 		for(int i=0; i<PageViewer.values.size(); i++){
 			//Log.i("orrudebug", name + " " + PageViewer.values.get(i));
 			if(PageViewer.values.get(i).name.equals(name)){
 				return i;
 			}
 		}
 		return 0;
 	}
 
 	
 }
