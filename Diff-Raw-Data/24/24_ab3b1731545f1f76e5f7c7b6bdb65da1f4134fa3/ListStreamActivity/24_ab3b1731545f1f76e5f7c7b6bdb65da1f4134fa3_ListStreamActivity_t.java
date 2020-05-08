 package org.fourdnest.androidclient.ui;
 
 import org.fourdnest.androidclient.R;
 
 import android.content.Intent;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 
 /**
  * The starting activity of the application. Displays a list of eggs present on
  * the server. Also provides functionality to access the create view and for
  * toggling route tracking.
  */
 public class ListStreamActivity extends NestSpecificActivity {
 
 	@Override
 	public View getContentLayout(View view) {

 		Button createButton = (Button) view.findViewById(R.id.create_button);
 		createButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), NewEggActivity.class);
 				v.getContext().startActivity(intent);
 
 			}
 		});
 
 		ListView streamList = (ListView) view.findViewById(R.id.egg_list);
 		EggReaderAdapter adapter = new EggReaderAdapter(streamList);
 		adapter.setEggs(null);
 		streamList.setAdapter(adapter);
		streamList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(arg1.getContext(),
						ViewEggActivity.class);
				arg0.getContext().startActivity(intent);

			}
		});

 		return view;
 
 	}
 
 	@Override
 	public int getLayoutId() {
 		return R.layout.list_stream_view;
 	}
 
 }
