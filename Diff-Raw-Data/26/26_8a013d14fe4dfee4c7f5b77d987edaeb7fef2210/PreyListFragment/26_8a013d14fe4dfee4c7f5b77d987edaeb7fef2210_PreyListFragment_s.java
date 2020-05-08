 package com.example.snms;
 
 
 import com.android.volley.Request.Method;
 import com.android.volley.RequestQueue;
 import com.android.volley.Response;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.Volley;
 import com.example.snms.domain.PreyItem;
 import com.example.snms.domain.PreyItemList;
 
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class PreyListFragment extends ListFragment {
 	
 	private RequestQueue requestQueue;
 	
 	private PreyListAdapter adapter;
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		this.requestQueue = Volley.newRequestQueue(this.getActivity());
 		putPreyItemsOnRequestQueue();
 		return inflater.inflate(R.layout.list, null);
 	}
 	
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		adapter = new PreyListAdapter(getActivity());
 		this.requestQueue = Volley.newRequestQueue(this.getActivity());
 		putPreyItemsOnRequestQueue();
 		setListAdapter(adapter);
 	}
 	
 	private void putPreyItemsOnRequestQueue() {
 	    GsonRequest<PreyItemList> jsObjRequest = new GsonRequest<PreyItemList>(
 	        Method.GET,
	        "http://192.168.0.108:3000/api/prayer/1010",
 	        PreyItemList.class,
 	        this.createSuccessListener(),
 	        this.createErrorListener());
 	    this.requestQueue.add(jsObjRequest);
 	}
 	
 	
 	private Response.Listener<PreyItemList> createSuccessListener() {
 	    return new Response.Listener<PreyItemList>() {
 	       
 	    	@Override
 			public void onResponse(PreyItemList response) {
 	    		adapter.clear();
 	    		for(PreyItem preyItem : response.getPreylist()) {
 	    			adapter.add(preyItem);
 	    		}
 				// TODO Auto-generated method stub
 				
 			}
 	    };
 	}
 	
 	private Response.ErrorListener createErrorListener() {
 	    return new Response.ErrorListener() {
 	        @Override
 	        public void onErrorResponse(VolleyError error) {
 	        	//TODO : Log error and get prey times from local storage
 	            error.getStackTrace();
 	        	Log.e("error", error.getMessage());
 	        }
 	    };
 	}
 	
 	public class PreyListAdapter extends ArrayAdapter<PreyItem> {
 
 		public PreyListAdapter(Context context) {
 			super(context, 0);
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			if (convertView == null) {
 				convertView = LayoutInflater.from(getContext()).inflate(R.layout.prey_row, null);
 			}
 			
 			TextView title = (TextView) convertView.findViewById(R.id.row_title);
 			title.setText(getItem(position).getName());
 			TextView time = (TextView) convertView.findViewById(R.id.row_time);
 			time.setText(getItem(position).getTime().toGMTString());
 			return convertView;
 		}
 
 	}
 
 }
