 package com.quanturium.androcloud2.fragments;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.ListFragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import com.cloudapp.api.CloudAppException;
 import com.cloudapp.impl.model.CloudAppAccountImpl;
 import com.quanturium.androcloud2.R;
 import com.quanturium.androcloud2.activities.MainActivity;
 import com.quanturium.androcloud2.adapters.UserAdapter;
 import com.quanturium.androcloud2.listeners.FragmentListener;
 import com.quanturium.androcloud2.tools.Prefs;
 
 public class UserFragment extends ListFragment implements OnItemClickListener
 {
 	private FragmentListener	mCallbacks	= null;
 	private final static String	TAG			= "UserFragment";
 
 	private UserAdapter	adapter;
 
 	@Override
 	public void onAttach(Activity activity)
 	{
 		super.onAttach(activity);
 
 		if (!(activity instanceof FragmentListener))
 			throw new IllegalStateException("Activity must implement fragment's callbacks.");
 
 		this.mCallbacks = (FragmentListener) activity;
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		return inflater.inflate(R.layout.fragment_user, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 		((MainActivity) getActivity()).setActionBarNavigationModeList(false);		
 		
 		adapter = new UserAdapter(getActivity(), getUserData());
 		configureListview(getListView());
 		setListAdapter(adapter);
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public void onDetach()
 	{
 		this.mCallbacks = null;
 		super.onDetach();
 	}
 	
 	public Map<Integer,String> getUserData()
 	{
 		Map<Integer,String> datas = new HashMap<Integer, String>();
 		
 		try
 		{
 			CloudAppAccountImpl account = new CloudAppAccountImpl(new JSONObject(Prefs.getPreferences(getActivity()).getString(Prefs.USER_INFOS, null)));			
 			datas.put(UserAdapter.ITEM_SUBSCRIBED, account.isSubscribed() ? "Yes" : "No");
 			datas.put(UserAdapter.ITEM_SUBSCRIBED_EXPIRES,  "n/a");
 			datas.put(UserAdapter.ITEM_DOMAIN, (account.getDomain() == null || account.getDomain().equals("") || account.getDomain().equals("null")) ? "n/a" : account.getDomain());
 		} catch (JSONException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CloudAppException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return datas;
 	}
 
 	private void configureListview(ListView listView)
 	{
 		listView.setOnItemClickListener(this);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3)
 	{
 		switch(position)
 		{
			case 0 : 
 				
 				Prefs.getPreferences(getActivity()).edit().remove(Prefs.USER_INFOS).commit();
 				Prefs.getPreferences(getActivity()).edit().remove(Prefs.EMAIL).commit();
 				Prefs.getPreferences(getActivity()).edit().remove(Prefs.PASSWORD).commit();
 				Prefs.getPreferences(getActivity()).edit().remove(Prefs.HASH).commit();
 				Prefs.getPreferences(getActivity()).edit().putBoolean(Prefs.LOGGED_IN, false).commit();
 				
 				this.mCallbacks.onUserLoggedOut();
 				
 				break;
 		}
 	}
 }
