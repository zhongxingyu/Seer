 /*
  * Copyright (C) 2013 asksven
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
 package com.asksven.mytrack;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.asksven.mytrack.utils.GooglePlayCard;
 import com.fima.cardsui.views.CardUI;
 
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 
 
 public class CreditsActivity extends SherlockActivity
 {
 
     private static final String TAG = "CreditsActivity";
     
 	private CardUI m_cardView;
 
 	private final String[] m_libs = new String[]
 	{ "ActionBarSherlock", "CardUI", "Google GSON" };
 	
 	private final String[] m_authors = new String[]
	{ "Jake Wharton", "Google" };
 	
 	private final String[] m_licenses = new String[]
 	{ "Apache 2.0", "Apache 2.0", "Apache 2.0" };
 	
 	private final String[] m_urls = new String[]
 	{ "", "", "http://code.google.com/p/google-gson/downloads"};
 
 	private final String[] m_colors = new String[]
 	{ "#33b6ea", "#e00707",
 			"#f2a400", "#9d36d0",
 			"#4ac925", "#222222",
 			"#33b6ea", "#e00707"};
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.credits);
         setTitle("Credits");
         
 		// init CardView
 		m_cardView = (CardUI) findViewById(R.id.cardsview);
 		m_cardView.setSwipeable(false);
 
 		for (int i = 0; i < m_libs.length; i++)
 		{
 			m_cardView.addCard(new GooglePlayCard(m_libs[i],
 			m_authors[i], m_colors[i],
 			m_colors[i], false, false));
 		}
 
 		// draw cards
 		m_cardView.refresh();
 
     }   
     
 }
