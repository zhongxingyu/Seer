 package cmput301.f13t01.createyourownadventure;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.ListFragment;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 
 public class StoryFragmentListFragment extends ListFragment {
 
 	private StoryFragmentListListener listener;
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			this.listener = (StoryFragmentListListener) activity;
 		} catch (final ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ " must implement StoryFragmentListListener");
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		GlobalManager app = (GlobalManager) getActivity().getApplication();
		ArrayList<StoryFragmentInfo> info = app.getEditManager()
 				.getFragmentInfoList();
 
 		FragmentListAdapter adapter = new FragmentListAdapter(getActivity(),
 				info);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		FragmentListAdapter adapter = (FragmentListAdapter) getListAdapter();
 		int storyId = adapter.getIdAtPosition(position);
 		listener.onStoryFragmentSelected(storyId);
 	}
 }
