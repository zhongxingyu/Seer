 package com.pk.personalcalculator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 public class FragmentManageList extends Fragment
 {
 	List<String> listOfItems;
 	ManageAdapter adapter;
 	ListView list;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		View view = inflater.inflate(R.layout.fragment_managelist, container, false);
 		
 		list = (ListView) view.findViewById(R.id.ListView);
 		
 		listOfItems = new ArrayList<String>();
 		listOfItems.add("Themes");
 		listOfItems.add("Lockdown");
 		listOfItems.add("Widget");
 		listOfItems.add("Pop Up");
 		// listOfItems.add("Graphing"); Implement later
 		
 		adapter = new ManageAdapter(getActivity().getBaseContext(), listOfItems);
 		
 		list.setAdapter(adapter);
 		
 		return view;
 	}
 	
 	@Override
 	public void onStart()
 	{
 		super.onStart();
 		list.setDividerHeight(0);
 		list.setOnItemClickListener(new OnItemClickListener()
 		{
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view, int position, long index)
 			{
 				String ID = listOfItems.get(position);
 				ActivityManage.switchFragment(ID, getActivity());
 			}
 		});
 	}
 	
 	// Inner class.... adapter.. o.e
 	public class ManageAdapter extends BaseAdapter implements OnClickListener
 	{
 		private Context context;
 		
 		private List<String> listItem;
 		
 		public ManageAdapter(Context context, List<String> listItem)
 		{
 			this.context = context;
 			this.listItem = listItem;
 		}
 		
 		@Override
 		public int getCount()
 		{
 			return listItem.size();
 		}
 		
 		@Override
 		public Object getItem(int position)
 		{
 			return listItem.get(position);
 		}
 		
 		@Override
 		public long getItemId(int position)
 		{
 			return position;
 		}
 		
 		@Override
 		public View getView(int position, View view, ViewGroup viewGroup)
 		{
 			String entry = listItem.get(position);
 			if (view == null)
 			{
 				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				view = inflater.inflate(R.layout.manage_item, null);
 			}
 			
 			Resources res = context.getResources();
 			
 			// Types
 			TextView Title = (TextView) view.findViewById(R.id.Title);
 			ImageView Image = (ImageView) view.findViewById(R.id.Image);
 			TextView Description = (TextView) view.findViewById(R.id.Description);
 			
 			Title.setText(entry);
 			
 			if (entry.equals("Themes"))
			{
 				Description.setText(res.getString(R.string.themes_description));
				Image.setImageResource(R.drawable.themes_icon);
			}
 			else if (entry.equals("Lockdown"))
			{
 				Description.setText(res.getString(R.string.lockdown_description));
				Image.setImageResource(R.drawable.lockdown_icon);
			}
 			else if (entry.equals("Widget"))
 				Description.setText(res.getString(R.string.widget_description));
 			else if (entry.equals("Pop Up"))
 				Description.setText(res.getString(R.string.popup_description));
 			else if (entry.equals("Graphing"))
 				Description.setText(res.getString(R.string.graphing_description));
 			
 			return view;
 		}
 		
 		@Override
 		public void onClick(View view)
 		{
 			String entry = (String) view.getTag();
 			listItem.remove(entry);
 			notifyDataSetChanged();
 			
 		}
 	}
 }
