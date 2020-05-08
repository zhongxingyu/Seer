 package edu.rit.csh.androidwebnews;
 
 import java.util.ArrayList;
 
 import android.app.Fragment;
 import android.app.ProgressDialog;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class DisplayThreadsFragment extends Fragment {
 	String newsgroupName;
 	ArrayList<Thread> threads;
 	boolean[] threadStatus;
 	ArrayList<String> displayedStrings;
 	WebnewsListAdapter<String> listAdapter;
 	int[] extraEntries;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 
 		Log.d("MyDebugging", "Starting ThreadsListFragment constructor");
 		newsgroupName = ((DisplayThreadsActivity)getActivity()).newsgroupName;
 		
 		ListView mainListView = new ListView(getActivity());
 		
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
 	    String apiKey = sharedPref.getString("api_key", "");
 		HttpsConnector hc = new HttpsConnector(apiKey, getActivity());
 	    
 	    threads = hc.getNewsgroupThreads(newsgroupName, 20);
 	    
 	    for(Thread thread : threads)
 	    {
 	    	((DisplayThreadsActivity)getActivity()).threadsDirectMap.add(thread);
 	    }
 	    
 	    displayedStrings = new ArrayList<String>();
	    threadStatus = new boolean[20];
	    extraEntries = new int[20];
 	    
	    for(int x = 0; x < 20; x++)
 	    {
 	    	displayedStrings.add(threads.get(x).toString());
 	    	threadStatus[x] = false;
 	    	extraEntries[x] = 0;
 	    }
 		Log.d("MyDebugging", "displayedStrings populated");
 	    
 	    listAdapter = new WebnewsListAdapter<String>(getActivity(), R.layout.threadlayout, displayedStrings);
 		Log.d("MyDebugging", "list adapter made");
 	    
 	    mainListView.setAdapter(listAdapter);
 		Log.d("MyDebugging", "listadapter set");
 		mainListView.setOnItemClickListener(new OnItemClickListener()
 		{
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View arg1, int position,
 					long id) {
 				int originalPos = findOriginalPos(((DisplayThreadsActivity)getActivity()).threadsDirectMap.get(position));
 				/*Log.d("MyDebugging", "item " + position + " clicked on");
 				Log.d("MyDebugging", "original position is " + originalPos);
 				Log.d("MyDebugging", "threadStatus[originalPos] = " + threadStatus[originalPos]);
 				Log.d("MyDebugging", "extraEntries[originalPos] = " + extraEntries[originalPos]);
 				Log.d("MyDebugging", "sub threads of threads.get(originalPosition) = " + threads.get(originalPos).getSubThreadCount());*/
 				if(originalPos > -1)
 				{
 					if(threadStatus[originalPos])
 					{
 						for(int x = 0; x < extraEntries[originalPos]; x++)
 						{
 							displayedStrings.remove(position + 1);
 							((DisplayThreadsActivity)getActivity()).threadsDirectMap.remove(position + 1);
 						}
 						extraEntries[originalPos] = 0;
 						listAdapter.notifyDataSetChanged();
 						threadStatus[originalPos] = false;					
 					}
 					else
 					{
 						expandThread(threads.get(originalPos), position);
 						listAdapter.notifyDataSetChanged();
 						threadStatus[originalPos] = true;					
 					}
 				}
 			}
 			
 		});
 		Log.d("MyDebugging", "ThreadsListFragment made");
 	    
 	    return mainListView;
 	}
 	
 	public void expandThread(Thread thread, int pos)
 	{
 		for(int x = thread.children.size() - 1; x > -1; x--)
 		{
 			Thread childThread = thread.children.get(x);
 			int originalPos = findOriginalPos(thread);
 			if(originalPos > -1)
 			{
 				if(childThread.children.size() != 0)
 					expandThread(childThread, originalPos, pos, 2);
 				displayedStrings.add(pos + 1, "||" + childThread.toString());
 				extraEntries[originalPos] += 1;
 				((DisplayThreadsActivity)getActivity()).threadsDirectMap.add(pos+1, childThread);
 				//pos += thread.children.size();
 			}
 		}
 	}
 	
 	private void expandThread(Thread thread, int originalPos, int pos, int level)
 	{
 		String temp = "";
 		for(int i = 0; i < level; i++)
 			temp += "||";
 		for(int x = thread.children.size() - 1; x > -1; x--)
 		{
 			Thread childThread = thread.children.get(x);
 			if(childThread.children.size() != 0)
 				expandThread(childThread, originalPos, pos, level + 1);
 			displayedStrings.add(pos + 1, temp + childThread.toString());
 			extraEntries[originalPos] += 1;
 			((DisplayThreadsActivity)getActivity()).threadsDirectMap.add(pos+1, childThread);
 			//pos += thread.children.size();
 		}
 	}
 	
 	private int findOriginalPos(Thread thread)
 	{
 		for(int x = 0; x < threads.size(); x++)
 			if(threads.get(x).Equals(thread))
 				return x;
 		return -1;
 	}
 }
