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
 
 package com.asksven.commandcenter;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.EditText;
 
 import com.asksven.commandcenter.valueobjects.CollectionManager;
 import com.asksven.commandcenter.valueobjects.Command;
 import com.asksven.commandcenter.valueobjects.CommandCollection;
 //import com.asksven.commandcenter.valueobjects.CommandDBHelper;
 import com.asksven.commandcenter.R;
 
 /**
  * @author sven
  *
  */
 /**
  * This is the secondary fragment, displaying the details of a particular
  * item.
  */
 
 public class BasicDetailsFragment extends Fragment
 {
     /**
      * Create a new instance of DetailsFragment, initialized to
      * show the text at 'index'.
      */
     public static BasicDetailsFragment newInstance(int index)
     {
         BasicDetailsFragment f = new BasicDetailsFragment();
 
         // Supply index input as an argument.
         Bundle args = new Bundle();
         args.putInt("index", index);
         f.setArguments(args);
 
         return f;
     }
 
     public int getShownKey()
     {
         return getArguments().getInt("index", -1);
     }
     
     public String getCollectionName()
     {
    	return getArguments().getString("collection", "none");
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState)
     {
         if (container == null)
         {
             // We have different layouts, and in one of them this
             // fragment's containing frame doesn't exist.  The fragment
             // may still be created from its saved state, but there is
             // no reason to try to create its view hierarchy because it
             // won't be displayed.  Note this is not needed -- we could
             // just run the code below, where we would create and return
             // the view hierarchy; it would just never be used.
             return null;
         }
 
         View v = inflater.inflate(R.layout.dlg_command, container, false);
 
         EditText myId = (EditText) v.findViewById(R.id.EditId);
         EditText myName = (EditText) v.findViewById(R.id.EditName);
         EditText myCommand = (EditText) v.findViewById(R.id.EditCommand);
         EditText myCommandValues = (EditText) v.findViewById(R.id.EditCommandValues);
         EditText myStatus = (EditText) v.findViewById(R.id.EditStatus);
 //        CheckBox myFavorite = (CheckBox) v.findViewById(R.id.CheckBoxFavorite);
         EditText myStatusRegex = (EditText) v.findViewById(R.id.EditStatusRegex);
         CheckBox myRegexIsOn = (CheckBox) v.findViewById(R.id.CheckBoxRegexIsOn);
         
         
         if (getShownKey() != -1)
         {
         	//CommandDBHelper myDB = new CommandDBHelper(getActivity());
         	CommandCollection commands =
         			CollectionManager.
         			getInstance(getActivity()).
         			getCollectionByName(getCollectionName());
         	Command myRecord = commands.findById(getShownKey());
 //        	  myRecord = myDB.fetchCommandByKey(getShownKey());
         	
         	if (myRecord != null)
         	{
 	        	myId.setText(String.valueOf(myRecord.getId()));
 	        	myName.setText(myRecord.getName());
 	        	myCommand.setText(myRecord.getCommand());
 	        	myCommandValues.setText(myRecord.getCommandValues());
 	        	myStatus.setText(myRecord.getCommandStatus());
 //	        	myFavorite.setChecked(myRecord.getFavorite()==1);
 	        	myStatusRegex.setText(myRecord.getRegexStatus());
 	        	myRegexIsOn.setChecked(myRecord.getMatchRegexOn()==1);
         	}
         }
         
         // @todo add button handlers here
         return v;
         
     }
 }
