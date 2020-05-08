 package br.mello.arthur.correcuritiba;
 
 import java.util.Arrays;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 
 public class EventsFragment extends SherlockListFragment {
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.events_fragment, container, false);    	
 	}
 
 	public void setEvents(Event[] events) {
 		Arrays.sort(events);
 		setListAdapter(new EventAdapter(getActivity(), R.layout.event_list_item, events));
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		DetailFragment fragment = (DetailFragment)getFragmentManager().findFragmentById(R.id.detail_fragment);
 		if (fragment != null && fragment.isInLayout()) {
			v.setActivated(true);
 			fragment.displayEvent((Event)l.getItemAtPosition(position));
 		} else {			
 			Intent intent = new Intent(getActivity(), DetailActivity.class);
 			intent.putExtra("event", ((Event)l.getItemAtPosition(position)));
 			startActivity(intent);
 		}	
 	}
 
 	// Menu
 	
 	@Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.events_menu, menu);
 	}
 
 }
