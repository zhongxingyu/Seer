 package com.shepherd;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.Request.Method;
 import com.android.volley.Response.ErrorListener;
 import com.android.volley.Response.Listener;
 import com.android.volley.VolleyError;
 import com.android.volley.toolbox.JsonObjectRequest;
 import com.android.volley.toolbox.JsonRequest;
 import com.android.volley.toolbox.Volley;
 import com.shepherd.api.Person;
 import com.shepherd.utils.NetUtils;
 import com.shepherd.utils.PersonUtils;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.ListFragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * Fragment that appears in the "content_frame", shows a planet
  */
public class PersonSearchFragment extends ListFragment implements Listener<JSONObject>, ErrorListener{
 
 	protected View mFormView, mStatusView;
 	private RequestQueue volleyQueue;
 	private List<Person> people;
 	
 	
 	public PersonSearchFragment() {
         // Empty constructor required for fragment subclasses
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_person_search, container, false);
         
         mFormView = rootView.findViewById(R.id.form);
 		mStatusView = rootView.findViewById(R.id.status);
 
 		mStatusView.setVisibility(View.VISIBLE);
 		mFormView.setVisibility(View.GONE);
         
 		
 		volleyQueue = Volley.newRequestQueue(this.getActivity());
		volleyQueue.add(new JsonObjectRequest(Method.GET, NetUtils.MissingPeopleURL+".json", null, this, this));
         
         
         String page = getResources().getStringArray(R.array.pages_array)[0];
         
         getActivity().setTitle(page);
         return rootView;
     }
     
     
     @Override
	public void onResponse(JSONObject response) {
    	Log.e("sheperd","json response");
     	
     	mStatusView.setVisibility(View.GONE);
 		mFormView.setVisibility(View.VISIBLE);
     	
 		
 		
 		PersonAdapter adapter = new PersonAdapter(this.getActivity(), new ArrayList<Person>());
 		setListAdapter(adapter);
 		
 		people = PersonUtils.getMissingPersons(response.toString(), null);
		
		adapter.add(new Person());
		people.add(new Person());
		people.add(new Person());
     }
     
     @Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
     	MissingPersonDetail newFrag = new MissingPersonDetail();
     	newFrag.setPersonDetail(people.get(position));
 
 		this.getActivity().getSupportFragmentManager()
 				.beginTransaction().replace(R.id.content_frame, newFrag)
 				// TODO Add this transaction to the back stack
 				// .addToBackStack("placeholder")
 				.commit();
     }
 
 	@Override
 	public void onErrorResponse(VolleyError arg0) {
		Log.e("sheperd","volley error");
 		//Toast.makeText(context, text, duration)
 	}
     
 }
