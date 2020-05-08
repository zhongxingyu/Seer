 package net.krks.android.roidcast;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 
 import net.krks.android.roidcast.Podcast.PodcastItem;
 import android.app.ExpandableListActivity;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Gravity;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AbsListView;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
 
 @SuppressWarnings("unused")
 public class Roidcast extends ExpandableListActivity  implements View.OnClickListener{
 	public static final String TAG = "Roidcast";
 	ExpandableListAdapter mAdapter;
 	
 	ArrayList<Podcast> loadData;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState){
     	Log.i(TAG,"onCreate @@@ roidast");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
        ImageButton b = (ImageButton)findViewById(R.id.RecrawlButton);
         //RoidcastClickLisner roidcastClickLisner = new RoidcastClickLisner();
         b.setOnClickListener(this);
         
        registerForContextMenu(getExpandableListView());
        
         // TODO 起動時に自動でrecrawlする(すべきでないのでは？と考えとりあえず実装しない)
         //loadData = doLoad();
     }
     
     @Override
 	protected void onPause() {
 		super.onPause();
 		try {
 			doSave();
 		} catch (IOException e) {
 			new RoidcatUtil().eLog(e);
 		}
 	}
 
 	@Override
 	protected void onStart() {
 		// TODO Auto-generated method stub
 		super.onStart();
 		    
         doDraw();
 	}
 	
 	protected void doDraw() {
 		loadData = doLoad();
 		//RoidcastEVLAdapter roidcastEVLAdapter = new RoidcastEVLAdapter(getApplicationContext());
         RoidcastEVLAdapter roidcastEVLAdapter = new RoidcastEVLAdapter();
         roidcastEVLAdapter.setPodcastList(loadData);
         
         // ExpandListViewを登録する時のお決まりの文法
         mAdapter = roidcastEVLAdapter;
         setListAdapter(mAdapter);
       //!?
       // registerForContextMenu(getExpandableListView());
         
 	}
 	
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.setHeaderTitle(R.string.roidcast_context_menu_header);
 		menu.add(0, 0, 0, R.string.roidcast_context_menu_delete);
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		
 		ExpandableListContextMenuInfo menuinfo = (ExpandableListContextMenuInfo)item.getMenuInfo();
 		
 		int type = ExpandableListView.getPackedPositionType(menuinfo.packedPosition);
 		if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
 			// super.onContextItemSelected(item);
 			// 親要素の時だけ処理する
 			int groupPosition = ExpandableListView.getPackedPositionGroup(menuinfo.packedPosition);
 			Podcast podcast = (Podcast)mAdapter.getGroup(groupPosition);
 			String title = podcast.getTitle();
 			loadData.remove(groupPosition);
 			try {
 				doSave();
 			} catch (IOException e) {
 				new RoidcatUtil().eLog(e);
 			}
 			doDraw();
 			Toast.makeText(getApplicationContext()
 					, title + " " + getString(R.string.roidcast_context_menu_delete_done)
 					, Toast.LENGTH_SHORT).show();
 		}
 		
 		return true;
 	}
 
 	/*
 	protected class RoidcastClickLisner implements OnClickListener{
     	@Override
     	public void onClick(View v) {
     		RoidcastFileIo r = new RoidcastFileIo(getApplicationContext());
     		loadData= r.doLoad();
     		
     		for(Podcast p:loadData) {
     			p.reCrawl();
     		}
     		try {
 				r.doSave(loadData);
 			} catch (IOException e) {
 				new RoidcatUtil().eLog(e);
 			}
 			
     	}
     }
     */
 	
 	
     public void doSave() throws IOException{
     	RoidcastFileIo r = new RoidcastFileIo(getApplicationContext());
     	r.doSave(loadData);
 	}
     
     /**
      * 子要素をタップまたはカーソルで選んでトラックボールのボタンを押したとき
      */
 	@Override
 	public boolean onChildClick(ExpandableListView parent, View v,
 			int groupPosition, int childPosition, long id) {
 		if (v instanceof RoidcastTextView) {
 			RoidcastTextView rv = (RoidcastTextView) v;
 			
 			Intent i = new Intent();
 			i.setAction(Intent.ACTION_VIEW);
 			i.setDataAndType(Uri.parse(rv.getAudioUri()), rv.getMediaType());
 			try {
 				// 最後に再生した時間を保存する
 				loadData.get(groupPosition).getItems().get(childPosition).setLastPlayedDate(new Date());
 				doSave();
 				startActivity(i);
 			} catch (ActivityNotFoundException e) {
 				/*
 				 * TODO mp4を独自再生する
 				VideoView video = new VideoView(getApplicationContext());
 				video.setMediaController(new MediaController(getApplicationContext()));
 				video.setVideoURI(Uri.parse(tv.getAudioUri()));
 				video.requestFocus();
 				*/
 				
 				Toast.makeText(getApplicationContext(), R.string.activity_notfound, Toast.LENGTH_SHORT).show();
 				new RoidcatUtil().eLog(e);
 			} catch (IOException e) {
 				new RoidcatUtil().eLog(e);
 			}
 			return true;
 		}
 		return false;
 		//return super.onChildClick(parent, v, groupPosition, childPosition, id);
 	}
 
 	public ArrayList<Podcast> doLoad(){
 		RoidcastFileIo r = new RoidcastFileIo(getApplicationContext());
     	return r.doLoad();
 	}
 	
 	
 	public class RoidcastEVLAdapter extends BaseExpandableListAdapter {
 		private ArrayList<Podcast> podcastList = new ArrayList<Podcast>();
 		
 		public void setPodcastList(ArrayList<Podcast> podcastList) {
 			this.podcastList = podcastList;
 		}
 
 		@Override
 		public Object getChild(int groupPosition, int childPosition) {
 			return podcastList.get(groupPosition).getItems().get(childPosition);
 		}
 
 		@Override
 		public long getChildId(int groupPosition, int childPosition) {
 			return childPosition;
 		}
 		
 		/** 
 		 * expandviewの親をクリックした後開かれる子要素のViewクラスを返す 
 		 * */
 		@Override
 		public View getChildView(int groupPosition, int childPosition,
 				boolean isLastChild, View convertView, ViewGroup parent) {
         	// TODO
 			RoidcastTextView textView = new RoidcastTextView(Roidcast.this);
 			setTextViewParams(textView);
 			
 			PodcastItem podcatItem = (PodcastItem)getChild(groupPosition, childPosition);
 			/* 子要素の表示はここで決まる */
             textView.setText(podcatItem.getTitle());
 			textView.setAudioUri(podcatItem.getAudioUri());
 			textView.setMediaType(podcatItem.getMediaType());
 			// 再生していないものは色変更
 			if(null == podcatItem.getLastPlayedDate()) {
 				textView.setTextColor(Color.CYAN);
 			}
 				
 			
 			return textView;
 		}
 		
 		@Override
 		public int getChildrenCount(int groupPosition) {
 			return podcastList.get(groupPosition).getItems().size();
 		}
 
 		@Override
 		public Object getGroup(int groupPosition) {
 			return podcastList.get(groupPosition);
 		}
 
 		@Override
 		public int getGroupCount() {
 			return podcastList.size();
 		}
 
 		@Override
 		public long getGroupId(int groupPosition) {
 			return groupPosition;
 		}
 
 		@Override
 		public View getGroupView(int groupPosition, boolean isExpanded,
 				View convertView, ViewGroup parent) {
 
             TextView textView = new TextView(Roidcast.this);
             setTextViewParams(textView);
 			
 			Podcast p = (Podcast) getGroup(groupPosition);
 			//CharSequence c =  getResources().getText((R.string.last_publish_date));
             textView.setText(p.getTitle() + "\n" + 
             		p.getLatestItemDate());
             
             // 子要素に、再生していないものがあれば色を変更する
             Podcast podcast = (Podcast)getGroup(groupPosition);
             boolean hasNotPlayItem = false;
             for(PodcastItem item:podcast.getItems()) {
             	if(item.getLastPlayedDate() == null) {
             		hasNotPlayItem = true;
             	}
             }
             if(hasNotPlayItem) {
             	textView.setTextColor(Color.CYAN);
             }
             return textView;
 		}
 		
 		@Override
 		public boolean hasStableIds() {
 			return true;
 		}
 
 		@Override
 		public boolean isChildSelectable(int groupPosition, int childPosition) {
 			// TODO Auto-generated method stub
 			return !(podcastList.get(groupPosition).isEmpty());
 		}
 
 		/**
 		 * TextViewのプロパティ値を設定する。（親要素、子要素共通）
 		 */
         protected void setTextViewParams(TextView t) {
             AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                     ViewGroup.LayoutParams.FILL_PARENT, 64);
 
             t.setLayoutParams(lp);
             t.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
             t.setPadding(36, 0, 0, 0);
         }
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		
 		// loadData= r.doLoad();
 		
 		for(Podcast p:loadData) {
 			p.reCrawl();
 		}
 		try {
 			RoidcastFileIo r = new RoidcastFileIo(getApplicationContext());
 			r.doSave(loadData);
 		} catch (IOException e) {
 			new RoidcatUtil().eLog(e);
 		}
 		doDraw();
 	}
 	
 
 	
 }
