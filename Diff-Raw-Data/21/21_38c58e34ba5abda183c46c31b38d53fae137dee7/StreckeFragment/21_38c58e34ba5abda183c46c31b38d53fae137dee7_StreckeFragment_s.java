 package com.mintplex.oeffioptimizer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mintplex.oeffioptimizer.model.Haltestellen;
 import com.mintplex.oeffioptimizer.model.Steige;
 import com.orm.query.Condition;
 import com.orm.query.Select;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class StreckeFragment extends AbstractFragment {
 
 	public static StreckeFragment create(long id) {
 		StreckeFragment f = new StreckeFragment();
 		Bundle b = new Bundle();
 		b.putLong("id", id);
 		f.setArguments(b);
 		return f;
 	}
 	
 	long id;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		id = getArguments().getLong("id");
 	}
 
 	ListView list;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_strecke, container, false);
 		list = (ListView) view.findViewById(R.id.fragment_strecke_list);
 
 		Steige steig = Steige.findById(Steige.class, id);
 		long line = steig.fkLinienId;
 
		List<Steige> nextSteige = Select.
 				from(Steige.class).
 				where(Condition.prop("fk_linien_id").eq(steig.fkLinienId), Condition.prop("reihenfolge").gt(steig.reihenfolge)).list();
 		
 		list.setAdapter(new ArrayAdapter<Steige>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, nextSteige));
 		list.setOnItemClickListener(new OnItemClickListener() {
 			
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				Steige s = (Steige) arg0.getItemAtPosition(arg2);
 				SteigInfoActivity.start(getActivity(), s.id);
 			}
 		});
 		return view;
 	}
 	
 }
