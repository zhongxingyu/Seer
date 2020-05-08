 package com.suyuxin.suvermemo;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 import com.evernote.client.android.EvernoteSession;
 import com.evernote.edam.notestore.NoteFilter;
 import com.evernote.edam.notestore.NoteList;
 import com.evernote.edam.notestore.NoteStore;
 import com.evernote.edam.type.Note;
 import com.evernote.edam.type.Notebook;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends DataActivity{
 	
 	private ListView view_notebook_list;
 	private boolean in_sync = false;
     private static final String TASK_NOTEBOOK_NAME = "任务列表";
 	
 	private void BeginDownloadSound()
 	{
 		Iterator<String> iter = getWordsToDownloadSound().iterator();
 		int notification_id = 0;
 		while(iter.hasNext())
 		{
 			Intent intent = new Intent(this, ServiceDownloadSound.class);
 			intent.putExtra("word", iter.next());
 			intent.putExtra("id", notification_id);
 			startService(intent);
 			notification_id++;
 		}
 	}
 
     private void BeginDownloadNote(String notebook_guid)
     {
         Intent intent = new Intent(this, ServiceDownloadNote.class);
         intent.putExtra("notebook_guid", notebook_guid);
         startService(intent);
     }
 
     private void BeginTaskNavigation(String notebook_guid)
     {
         Intent intent = new Intent(this, TaskPanel.class);
         intent.putExtra("notebook_guid", notebook_guid);
         startActivity(intent);
     }
 
     private void BeginDownloadTask(String notebook_guid)
     {
         Intent intent = new Intent(this, ServiceDownloadTask.class);
         intent.putExtra("notebook_guid", notebook_guid);
         startService(intent);
     }
 
     private void UpdateNotebookList()
     {
        Intent intent = new Intent(this, ServiceUpdateNotebookList.class);
        startService(intent);
     }
 
 	private class NotebookListAdapter extends ArrayAdapter<String> {
 
 		public NotebookListAdapter(Context context, int resource,
 				int textViewResourceId, String[] objects) {
 			super(context, resource, textViewResourceId, objects);
 			// TODO Auto-generated constructor stub
 		}
 
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			// TODO Auto-generated method stub
 			if(convertView == null)
 			{
 				convertView = LayoutInflater.from(getContext()).inflate(R.layout.notebook_row,
 						parent, false);
 			}
 			//set notebook name
 			TextView text = (TextView)convertView.findViewById(R.id.text_notebook_name);
 			text.setText(getItem(position));
 			//set sync button of each notebook in list
 			Button sync = (Button)convertView.findViewById(R.id.button_notebook_sync);
 			sync.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View arg0) {
 					// TODO Auto-generated method stub
 					if(in_sync)
 					{
 						Toast.makeText(getBaseContext(), R.string.text_wait_for_download, Toast.LENGTH_LONG).show();
 						return;
 					}
 					if(getItem(position).equals(getResources().getString(R.string.text_empty_notebook_list)))
 					{
 						if(!evernote_session.isLoggedIn())
 						{
 							evernote_session.authenticate(getBaseContext());
 						}
                        // UpdateNotebookList();
 					}
                     else if(getItem(position).startsWith(TASK_NOTEBOOK_NAME))
                     {
                         BeginDownloadTask(list_notebook_guid[position]);
                     }
 					else if(getItem(position).equals(getResources().getString(R.string.text_sync_sound_file)))
 					{//download pronunciation file
 						BeginDownloadSound();
 					}
 					else
 					{//normally update content of a notebook
                         UpdateNotebookList();
                         BeginDownloadNote(list_notebook_guid[position]);
 					}
 				}
 			});
 
             Button enter = (Button)convertView.findViewById(R.id.button_notebook_enter);
             enter.setVisibility(Button.INVISIBLE);
 
 			if(list_notebook_guid == null || position >= list_notebook_guid.length)
 				return convertView;
 
 			enter.setOnClickListener(new View.OnClickListener() {
 				
 				@Override
 				public void onClick(View v) {
 					// TODO Auto-generated method stub
 					if(in_sync)
 					{
 						Toast.makeText(getBaseContext(), R.string.text_wait_for_download, Toast.LENGTH_LONG).show();
 						return;
 					}
                     if(getItem(position).startsWith(TASK_NOTEBOOK_NAME))
                     {
                         BeginTaskNavigation(list_notebook_guid[position]);
                     }
                     else
                     {
 					    Intent intent = new Intent(getContext(), NoteActivity.class);
 					    intent.putExtra("notebook_guid", list_notebook_guid[position]);
 					    intent.putExtra("notebook_count",
 						    notebook_info.get(list_notebook_guid[position]).note_number);
 					    startActivity(intent);
                     }
 				}
 			});
 			//test whether to show "enter" button
 			if(list_notebook_guid != null)
 			{
 				if(notebooks_having_local_contents.contains(list_notebook_guid[position]))
 					enter.setVisibility(Button.VISIBLE);
 				else
 					enter.setVisibility(Button.INVISIBLE);
 			}
 			return convertView;
 		}
 		
 	}
 	
 	@Override
 	protected void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		//store the status
 	}
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		//
 		view_notebook_list = (ListView)findViewById(R.id.listView_notebook);
 		NotebookListAdapter adapter = new NotebookListAdapter(this,
 				R.layout.notebook_row,
 				R.id.text_notebook_name,
 				getNotebookNames());
 		view_notebook_list.setAdapter(adapter);
 	}
 
     @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
 
 
     }
 
     @Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		//set adapter of notebook list
 		setContentView(R.layout.activity_main);
 		view_notebook_list = (ListView)findViewById(R.id.listView_notebook);
 		NotebookListAdapter adapter = new NotebookListAdapter(this,
 				R.layout.notebook_row,
 				R.id.text_notebook_name,
 				getNotebookNames());
 		view_notebook_list.setAdapter(adapter);
 	}
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		switch (requestCode) {
 	      case EvernoteSession.REQUEST_CODE_OAUTH:
 	    	// update notebook list when oauth activity returns result
 	        if (resultCode == Activity.RESULT_OK) {
                 UpdateNotebookList();
 	        }
 	        break;
 	    }
 	}
 	
 }
