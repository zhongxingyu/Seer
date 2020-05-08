 /*
  This file is part of QuizReader.
 
  QuizReader is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  QuizReader is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with QuizReader.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.quizreader.android;
 
 import java.util.List;
 
 import org.quizreader.android.database.Title;
 import org.quizreader.android.database.TitleDao;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class TitleListActivity extends ListActivity {
 
 	private static final int REQUEST_ADD_TITLE = 0;
 	private List<Title> titles;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.title_list);
 
 		loadTitles();
 	}
 
 	private void loadTitles() {
 		TitleDao titleDao = new TitleDao(this);
 		titleDao.open();
 		titles = titleDao.getAllTitles();
 		titleDao.close();
 
		setListAdapter(new ArrayAdapter<Title>(this, R.layout.title_add_row, titles)); // fix this
 	}
 
 	public void addTitle(View view) {
 		Intent addTitleIntent = new Intent(this, TitleAddActivity.class);
 		startActivityForResult(addTitleIntent, REQUEST_ADD_TITLE);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Intent titleReadIntent = new Intent(this, TitleReadActivity.class);
 		titleReadIntent.putExtra("titleId", titles.get(position).getId());
 		startActivity(titleReadIntent);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == REQUEST_ADD_TITLE) {
 			loadTitles();
 		}
 	}
 
	// class TitleAdapter extends ArrayAdapter<Title> {
	// TitleAdapter() {
	// super(TitleListActivity.this, android.R.layout.simple_list_item_1, titles);
	// }
	//
	// public View getView(int position, View convertView, ViewGroup parent) {
	//
	// }
	// }
 }
