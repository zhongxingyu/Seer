 package pro.trousev.cleer.android.userInterface;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import pro.trousev.cleer.Item;
 import pro.trousev.cleer.Messaging;
 import pro.trousev.cleer.Playlist;
 import pro.trousev.cleer.android.AndroidMessages.Action;
 import pro.trousev.cleer.android.AndroidMessages.ServiceRequestMessage;
 import pro.trousev.cleer.android.AndroidMessages.ServiceTaskMessage;
 import pro.trousev.cleer.android.AndroidMessages.TypeOfResult;
 import pro.trousev.cleer.android.Constants;
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 
 public class ListOfCompositions extends ListFragment {
 	String data[] = new String[] { "one", "two", "three", "four", "one", "two",
 			"three", "four", "one", "two", "three", "four" };
 	List<Item> list;
 
 	public ListOfCompositions(List<Item> arg) {
 		list = arg;
 	}
 
 	public ListOfCompositions(Playlist playlist) {
 		list = playlist.contents();
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		ListOfCompAdapter adapter = new ListOfCompAdapter(getActivity(), list);
 		this.setHasOptionsMenu(true);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public void onAttach(Activity activcity) {
 		super.onAttach(activcity);
 		Log.d(Constants.LOG_TAG, "ListOfComp.onAttach()");
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		getListView().setOnCreateContextMenuListener(this);
 		Log.d(Constants.LOG_TAG, "ListOfComp.onActivityCreated()");
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view,
 			ContextMenuInfo menuInfo) {
 		Log.d(Constants.LOG_TAG, "ListOfComp.onCreateCotextMenu()");
 		super.onCreateContextMenu(menu, view, menuInfo);
 		MenuInflater inflater = getActivity().getMenuInflater();
 		inflater.inflate(R.menu.compositionlist_context_menu, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		ServiceTaskMessage message = ((MainActivity) getActivity()).taskMessage;
 		Item track;
 		switch (item.getItemId()) {
 		case R.id.play:
 			message.action = Action.setToQueue;
 			track = list.get(acmi.position);
 			message.list = new ArrayList<Item>();
 			message.list.add(track);
 			message.position = list.indexOf(track);
 			Messaging.fire(message);
 			break;
 		case R.id.addToQueue:
 			message.action = Action.addToQueue;
 			track = list.get(acmi.position);
 			message.list = new ArrayList<Item>();
 			message.list.add(track);
 			Messaging.fire(message);
 			break;
 		case R.id.addToList:
 			ServiceRequestMessage msg = ((MainActivity) getActivity()).requestMessage;
 			msg.type = TypeOfResult.PlaylistsInDialog;
 			Messaging.fire(msg);
 			// TODO What to do?
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public void onListItemClick(ListView listView, View view, int position,
 			long id) {
 		ServiceTaskMessage message = ((MainActivity) getActivity()).taskMessage;
 		message.action = Action.setToQueue;
 		message.list = list;
 		message.position = position;
 		Messaging.fire(message);
 	}
 }
