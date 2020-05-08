 /*
 	Copyright (C) 2010 Ben Van Daele (vandaeleben@gmail.com)
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package be.benvd.mvforandroid;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import be.benvd.mvforandroid.data.DatabaseHelper;
 import be.benvd.mvforandroid.data.MVDataService;
 
 import com.commonsware.cwac.wakeful.WakefulIntentService;
 
 public class TopupsActivity extends Activity {
 
 	public DatabaseHelper helper;
 	private Cursor model;
 
 	private BroadcastReceiver updatedReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			model.requery();
 			TopupsActivity.this.getParent().setProgressBarIndeterminateVisibility(false);
 		}
 	};
 
 	private BroadcastReceiver exceptionReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, getString(R.string.exception_message), Toast.LENGTH_SHORT).show();
 			TopupsActivity.this.getParent().setProgressBarIndeterminateVisibility(false);
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.topups);
 
 		helper = new DatabaseHelper(this);
 		model = helper.topups.getAll();
 
 		updateView();
 	}
 
 	private void updateView() {
 		Button updateButton = (Button) findViewById(R.id.update_button);
 		updateButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				TopupsActivity.this.getParent().setProgressBarIndeterminateVisibility(true);
 				Intent i = new Intent(TopupsActivity.this, MVDataService.class);
 				i.setAction(MVDataService.UPDATE_TOPUPS);
 				WakefulIntentService.sendWakefulWork(TopupsActivity.this, i);
 			}
 		});
 
 		ListView topupsList = (ListView) findViewById(R.id.topups_list);
 		topupsList.setAdapter(new TopupsAdapter(this, model));
 		topupsList.setFocusable(false);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(updatedReceiver, new IntentFilter(MVDataService.TOPUPS_UPDATED));
 		registerReceiver(exceptionReceiver, new IntentFilter(MVDataService.EXCEPTION));
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(updatedReceiver);
 		unregisterReceiver(exceptionReceiver);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		helper.close();
 	}
 
 	private static SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
 
 	static class TopupsHolder {
 		private TextView amount = null, method = null, date = null;
 
 		TopupsHolder(View listItem) {
 			amount = (TextView) listItem.findViewById(R.id.topup_amount);
 			method = (TextView) listItem.findViewById(R.id.topup_method);
 			date = (TextView) listItem.findViewById(R.id.topup_date);
 		}
 
 		public void populateFrom(Cursor c, DatabaseHelper helper) {
 			amount.setText((int) helper.topups.getAmount(c) + "€");
 			method.setText(helper.topups.getMethod(c));
 			date.setText(formatDate.format(new Date(helper.topups.getExecutedOn(c))));
 		}
 
 	}
 
 	class TopupsAdapter extends CursorAdapter {
 
 		public TopupsAdapter(Context context, Cursor c) {
 			super(context, c);
 		}
 
 		@Override
 		public void bindView(View view, Context context, Cursor cursor) {
 			TopupsHolder holder = (TopupsHolder) view.getTag();
 			holder.populateFrom(cursor, helper);
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			LayoutInflater inflater = getLayoutInflater();
 			View listItem = inflater.inflate(R.layout.topups_list_item, parent, false);
 			TopupsHolder holder = new TopupsHolder(listItem);
 			listItem.setTag(holder);
 			return listItem;
 		}
 
 		@Override
 		public boolean isEnabled(int position) {
 			return false;
 		}
 
 	}
 
 }
