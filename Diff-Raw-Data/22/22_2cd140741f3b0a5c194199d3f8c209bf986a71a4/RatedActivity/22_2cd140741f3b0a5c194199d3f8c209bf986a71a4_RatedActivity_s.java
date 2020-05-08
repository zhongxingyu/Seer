 /*  
  *  Codebits - Comment the talk proposals.
  *  Copyright (C) 2011 Henrique Rocha <hmrocha@gmail.com>
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.androidappdev.codebits;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
import android.widget.TextView;
 
 public class RatedActivity extends ListActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (CodebitsActivity.ratedTalks.size() == 0) {
			TextView textview = new TextView(this);
			textview.setText("No talks rated yet or not logged in.");
			setContentView(textview);
 		} else {
			setContentView(R.layout.tab_layout);
 			setListAdapter(new TalkArrayAdapter(this,
 					CodebitsActivity.ratedTalks));
 		}
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Intent intent = new Intent(getApplicationContext(),
 				TalkViewerActivity.class);
 		intent.putExtra(CodebitsActivity.TALK_ID, 
 				CodebitsActivity.ratedTalks.get(position).getId());
 		startActivity(intent);
 	}
 }
