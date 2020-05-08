 /*
  * Copyright (C) 2011 asksven
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.asksven.commandcenter.valueobjects;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.asksven.commandcenter.R;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.preference.PreferenceManager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 
 
 public class CommandListAdapter extends BaseAdapter
 {
 
 	private List m_myItems;
 	private Context m_context;
 	private int m_selectedPosition;
 	private boolean m_showResult;
 	private String m_resultText;
 	
 	/** those widgets are populated asxnchronously */
 //	private ToggleButton m_commandStatus; 
 //	private TextView m_commandState;
 
 	public CommandListAdapter(Context context, List items)
 	{
 		m_myItems = items;
 		m_context = context;
 //		new RefreshCommandsCacheTask().execute("");
 	}
 	
 	
 	public void setSelection(int position)
 	{
 		m_selectedPosition = position;
 	}
 	
 	public void showResult(String resultText)
 	{
 		m_showResult = true;
 		m_resultText = resultText;
 		notifyDataSetChanged();
 	}
 	
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
         if (convertView == null)
         {
             LayoutInflater inflater = (LayoutInflater) m_context
                     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = inflater.inflate(R.layout.row_command, null);
         }
 		
 		
 		final Command myCommand = (Command) m_myItems.get(position);
 		
 		TextView myCommandText=(TextView)convertView.findViewById(R.id.TextViewCommand);
 		myCommandText.setText(myCommand.getName());
 
 
 		TextView myCommandStateCmd=(TextView)convertView.findViewById(R.id.TextViewStateCommand);
 		myCommandStateCmd.setText(myCommand.getCommandStatus());
 		
 		TextView myCommandState=(TextView)convertView.findViewById(R.id.TextViewState);
 		myCommandState.setText(myCommand.getStatusCached());
 		
 		// determine status based on state and regex
 		ToggleButton myCommandStatus=(ToggleButton)convertView.findViewById(R.id.ToggleButton);
 		myCommandStatus.setClickable(false);
 		
     	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(m_context);
     	boolean bUpdateCache = preferences.getBoolean("autoRunStatus", true);
 
     	// if no status commands are to be executed hide the toogle button
     	if (bUpdateCache)
     	{
 	   		// use state and regex to determine status
 			myCommandStatus.setChecked(myCommand.isOnCached());
     	}
     	else
     	{
     		myCommandStatus.setVisibility(View.GONE);
    		// hide status command and status as well
    		convertView.findViewById(R.id.TextViewStateCommand).setVisibility(View.GONE);
    		convertView.findViewById(R.id.TextViewState).setVisibility(View.GONE);
     	}
 		
     	// expand element where result is to be shown
     	LinearLayout resultLayout = (LinearLayout) convertView.findViewById(R.id.linearLayoutResult);
 		
     	if ((m_selectedPosition == position) && (m_showResult))
     	{
     		m_showResult = false;
     		resultLayout.setVisibility(View.VISIBLE);
     		TextView resultText=(TextView)convertView.findViewById(R.id.editTextResult);
     		resultText.setText(m_resultText);
     	}
     	else
     	{
     		resultLayout.setVisibility(View.GONE);
     	}
     	
 		myCommandText.setClickable(false);
 		myCommandText.setEnabled(true);
 		myCommandText.setFocusable(false);		
 		
 		return(convertView);
 	}
 	
     public int getCount()
     {
     	int ret = 0;
     	if (m_myItems != null)
     	{
     		ret = m_myItems.size();
     	}
     	return ret;
     }
     
     public Object getItem(int position)
     {
         return m_myItems.get(position);
     }
 
     public long getItemId(int position)
     {
         return position;
     }
     
     public void deleteItem(int iKey)
     {
 		CommandDBHelper myDB = new CommandDBHelper(m_context);
 		myDB.deleteCommand(iKey);
 		m_myItems = myDB.getCommandCollection().getEntries();
 		this.notifyDataSetChanged();
     }
     
     public void reloadFromDatabase()
     {
 		CommandDBHelper myDB = new CommandDBHelper(m_context);
 		m_myItems = myDB.getCommandCollection().getEntries();
 		this.notifyDataSetChanged();
     }            
 }
 
 
