 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Fragments;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits.*;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 import de.uni.stuttgart.informatik.ToureNPlaner.R;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities.EditNodeScreen;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Adapters.NodeListAdapter;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.DragDrop.DragNDropListView;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Listener.DragListener;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Listener.DropListener;
 
 import java.io.Serializable;
 
 public class NodeListFragment extends SherlockListFragment implements Session.Listener {
 	private NodeListAdapter adapter;
 	private Session session;
 	private boolean dirty;
 
 	public boolean isDirty() {
 		return dirty;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		outState.putSerializable(Session.IDENTIFIER, session);
 		outState.putBoolean("dirty", dirty);
 		super.onSaveInstanceState(outState);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		// If we get created for the first time we get our data from the intent
 		Bundle data = savedInstanceState != null ? savedInstanceState : getActivity().getIntent().getExtras();
 		session = (Session) data.getSerializable(Session.IDENTIFIER);
 
 		if (savedInstanceState != null) {
 			dirty = savedInstanceState.getBoolean("dirty");
 		}
 
 		session.registerListener(NodeListFragment.class, this);
 
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		adapter = new NodeListAdapter(getActivity(), session.getNodeModel().getNodeVector(), session.getSelectedAlgorithm().sourceIsTarget());
 		setListAdapter(adapter);
 		ListView listView = getListView();
 		registerForContextMenu(listView);
 
 
 		if (listView instanceof DragNDropListView) {
 			((DragNDropListView) listView).setDropListener(mDropListener);
 			((DragNDropListView) listView).setDragListener(mDragListener);
 
 			//---------ContextMenu-----------------
 			listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
 				@Override
 				public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
 					AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;
 					contextMenu.setHeaderTitle(adapter.getItem(info.position).getName());
 					String[] menuItems = {getResources().getString(R.string.edit), getResources().getString(R.string.delete)};
 					for (int i = 0; i < menuItems.length; i++) {
 						contextMenu.add(Menu.NONE, i, i, menuItems[i]);
 					}
 				}
 			}
 			);
 
 			listView.setOnItemClickListener(new OnItemClickListener() {
 				@Override
 				public void onItemClick(AdapterView<?> adapter, View view,
 				                        final int pos, long arg3) {
					editNode((Node) adapter.getItemAtPosition(pos), pos);
 				}
 			});
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.nodelist, null);
 	}
 
 	private DropListener mDropListener =
 			new DropListener() {
 				public void onDrop(int from, int to) {
 					Edit edit = new SwapNodesEdit(session, from, to);
 					edit.perform();
 				}
 			};
 
 	private DragListener mDragListener =
 			new DragListener() {
 
 				int backgroundColor = 0xe0103010;
 				int defaultBackgroundColor;
 
 				public void onDrag(int x, int y, ListView listView) {
 				}
 
 				public void onStartDrag(View itemView) {
 					itemView.setVisibility(View.INVISIBLE);
 					defaultBackgroundColor = itemView.getDrawingCacheBackgroundColor();
 					itemView.setBackgroundColor(backgroundColor);
 					ImageView iv = (ImageView) itemView.findViewById(R.id.nodelisticon);
 					if (iv != null) iv.setVisibility(View.INVISIBLE);
 				}
 
 				public void onStopDrag(View itemView) {
 					itemView.setVisibility(View.VISIBLE);
 					itemView.setBackgroundColor(defaultBackgroundColor);
 					ImageView iv = (ImageView) itemView.findViewById(R.id.nodelisticon);
 					if (iv != null) iv.setVisibility(View.VISIBLE);
 				}
 
 			};
 
	private void editNode(Node node, int index) {
		Intent myIntent = new Intent(getActivity(), EditNodeScreen.class);
		myIntent.putExtra("node", node);
		myIntent.putExtra(Session.IDENTIFIER, session);
		myIntent.putExtra("index", index);
		startActivityForResult(myIntent, 0);
	}

 	@Override
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 			case 0: // edit
				editNode(adapter.getItem(info.position), info.position);
 				break;
 			case 1: // delete
 				Edit edit = new RemoveNodeEdit(session, info.position);
 				edit.perform();
 				break;
 		}
 		return true;
 	}
 
 
 	@Override
 	public void onPrepareOptionsMenu(Menu menu) {
 		menu.findItem(R.id.revertNodes).setEnabled(session.getNodeModel().size() >= 2);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.nodelistmenu, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 			case R.id.revertNodes:
 				Edit edit = new ReverseNodesEdit(session);
 				edit.perform();
 				break;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 		return true;
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Edit edit;
 		switch (resultCode) {
 			case Activity.RESULT_OK:
 				edit = new UpdateNodeEdit(session, data.getExtras().getInt("index"), (Node) data.getSerializableExtra("node"));
 				edit.perform();
 				break;
 			case EditNodeScreen.RESULT_DELETE:
 				edit = new RemoveNodeEdit(session, data.getExtras().getInt("index"));
 				edit.perform();
 				break;
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		session.removeListener(NodeListFragment.class);
 		super.onDestroy();
 	}
 
 	@Override
 	public void onChange(Session.Change change) {
 		dirty = true;
 		adapter.notifyDataSetChanged();
 	}
 }
