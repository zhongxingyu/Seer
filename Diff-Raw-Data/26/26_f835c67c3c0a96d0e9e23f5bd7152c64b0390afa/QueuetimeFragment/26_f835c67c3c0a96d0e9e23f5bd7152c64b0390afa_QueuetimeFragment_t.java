 package se.chalmers.krogkollen.list;
 
 import android.support.v4.app.ListFragment;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import se.chalmers.krogkollen.R;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import se.chalmers.krogkollen.search.SortByQueueTime;
 
 public class QueuetimeFragment extends ListFragment {
 
     private PubListAdapter adapter;
     private ListModel model;
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
         model = new ListModel();
         View rootView = inflater.inflate(R.layout.fragment_queue_list, container, false);
 
         adapter = new PubListAdapter(getActivity(), R.layout.listview_item, model.getSortedArray(new SortByQueueTime()));
 
         setListAdapter(adapter);
 
         return rootView;
     }

 }
 
