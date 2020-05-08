 package com.conanyuan.papertelephone;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 
 public class MainActivity extends FragmentActivity {
 	private MyAdapter mAdapter;
 	private ViewPager mPager;
 	private ArrayList<IGame> mLocalGames = new ArrayList<IGame>();
 	//private ArrayList<IGame> mNetworkGames;
 	private ArrayList<IGame> mCompletedGames = new ArrayList<IGame>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_layout);
 
 		mAdapter = new MyAdapter(getSupportFragmentManager(), mLocalGames, mCompletedGames);
 		mPager = (ViewPager) findViewById(R.id.pager);
 		mPager.setAdapter(mAdapter);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.FragmentActivity#onPause()
 	 */
 	@Override
 	protected void onPause() {
 		super.onPause();
 		File rootdir = getFilesDir();
 		// TODO: Rewriting all data to disk is not terribly fast, and this
 		// gets called any time we edit a turn.  We should probably only 
 		// write new data, not delete everything and rewrite it.
 		try {
 			GameImpl.deleteFileRecursively(rootdir);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		for (IGame game : mLocalGames) {
 			try {
 				game.toDisk();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		for (IGame game : mCompletedGames) {
 			try {
 				game.toDisk();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.support.v4.app.FragmentActivity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 
 		// TODO: Do we have to handle the case where the activity was garbage
 		// collected, mLocalGames is empty, but we can't read the files because
 		// we used it to serialize a turn (which has content but no data)?
 		if (mLocalGames.size() != 0) {
 			return;
 		}
 		
 		File rootdir = getFilesDir();
 		File[] files = rootdir.listFiles();
 		Arrays.sort(files);
 		for (File gameDir : files) {
 			int gameId = GameImpl.parseGameDir(gameDir.getName());
 			DrawGame g = new DrawGame(gameId, rootdir.toString());
 			try {
 				if (g.fromDisk(gameDir)) {
 					if (g.isCompleted()) {
 						mCompletedGames.add(g);
 					} else {
 						mLocalGames.add(g);
 					}
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				try {
 					GameImpl.deleteFileRecursively(gameDir);
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 		Collections.sort(mLocalGames, new GameImpl.ByTimestamp());
 	}
 
 	public int nextGameId() {
 		for (int ii = 0; ; ii++) {
 			boolean found = false;
 			for (IGame g : mLocalGames) {
 				if (g.getGameId() == ii) {
 					found = true;
 					break;
 				}
 			}
 			if (found) {
 				continue;
 			}
 			for (IGame g : mCompletedGames) {
 				if (g.getGameId() == ii) {
 					found = true;
 					break;
 				}
 			}
 			if (!found) {
 				return ii;
 			}
 		}
 	}
 
 	public void addCompletedGame(IGame game) {
 		mCompletedGames.add(game);
 		mAdapter.notifyDataSetChanged();
 	}
 
 	public static class MyAdapter extends FragmentPagerAdapter {
 		private ArrayList<IGame> myLocalGames;
 		private ArrayList<IGame> myCompletedGames;
 
 		private LocalGameListFragment myLocalFragment;
 		private CompletedGameListFragment myCompletedFragment;
 
 		public MyAdapter(FragmentManager fragmentManager,
 				ArrayList<IGame> local, ArrayList<IGame> completed) {
 			super(fragmentManager);
 			myLocalGames = local;
 			myCompletedGames = completed;
 		}
 
 		@Override
 		public int getCount() {
 			return 3;
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			switch (position) {
 			case 0:
 				myLocalFragment = LocalGameListFragment.newInstance(myLocalGames);
 				return myLocalFragment;
 			case 1:
 				myCompletedFragment = CompletedGameListFragment.newInstance(myCompletedGames);
 				return myCompletedFragment;
 			case 2:
 				return FileListFragment.newInstance();
 			default:
 				return null;
 			}
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			switch (position) {
 			case 0:
 				return "Local Games";
 			case 1:
 				return "Completed Games";
 			case 2:
 				return "Files";
 			default:
 				return "Unknown";
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see android.support.v4.view.PagerAdapter#notifyDataSetChanged()
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public void notifyDataSetChanged() {
 			super.notifyDataSetChanged();
 			if (myLocalFragment != null) {
 				((ArrayAdapter<IGame>) myLocalFragment.getListAdapter())
 				.notifyDataSetChanged();
 			}
 			if (myCompletedFragment != null) {
 				((ArrayAdapter<IGame>) myCompletedFragment.getListAdapter())
 				.notifyDataSetChanged();
 			}
 		}
 	}
 
 	public static class LocalGameListFragment extends ListFragment {
 		static final String GAME_MESSAGE = "com.conanyuan.papertelephone.GAME";
 		static final int MENU_GROUP_ID = 1;
 		protected ArrayList<IGame> mGames;
 
 		/**
 		 * Create a new instance of LocalGameListFragment, providing "games" as an
 		 * argument.
 		 */
 		static LocalGameListFragment newInstance(ArrayList<IGame> games) {
 			LocalGameListFragment f = new LocalGameListFragment();
 
 			Bundle args = new Bundle();
 			args.putParcelableArrayList("games", games);
 			f.setArguments(args);
 
 			return f;
 		}
 
 		/**
 		 * When creating, retrieve the games from its arguments.
 		 */
 		@Override
 		public void onCreate(Bundle savedInstanceState) {
 			super.onCreate(savedInstanceState);
 			if (getArguments() != null) {
 				mGames = getArguments().getParcelableArrayList("games");
 			}
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View v = inflater.inflate(R.layout.fragment_gamelist, container,
 					false);
 
 			// Watch for requests to add a new game.
 			Button button = (Button) v.findViewById(R.id.new_game);
 			button.setOnClickListener(new OnClickListener() {
 				@SuppressWarnings("unchecked")
 				public void onClick(View v) {
 					mGames.add(new DrawGame(((MainActivity)getActivity()).nextGameId(), 
 							getActivity().getFilesDir().toString()));
 					// Have to cast to an ArrayAdapter in order to call
 					// notifyDataSetChanged.
 					// And if we don't call notifyDataSetChanged, then the view
 					// won't know the array changed until we move to a different
 					// tab and move back. We know we will get an ArrayAdapter
 					// because that's what we set it to in onActivityCreated.
 					((ArrayAdapter<IGame>) getListAdapter())
 							.notifyDataSetChanged();
 				}
 			});
 
 			return v;
 		}
 
 		@Override
 		public void onActivityCreated(Bundle savedInstanceState) {
 			super.onActivityCreated(savedInstanceState);
 			setListAdapter(new ArrayAdapter<IGame>(getActivity(),
 					android.R.layout.simple_list_item_1, mGames));
 			registerForContextMenu(getListView());
 		}
 
 		@Override
 		public void onListItemClick(ListView l, View v, int position, long id) {
 			Log.i("FragmentList", "Item clicked: " + id);
 			Intent intent = new Intent(getActivity(), DrawGameActivity.class);
 		    intent.putExtra(GAME_MESSAGE, mGames.get(position));
 		    startActivityForResult(intent, position);
 		}
 
 		/* (non-Javadoc)
 		 * @see android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem)
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public boolean onContextItemSelected(MenuItem item) {
 			// Need a group id because the fragment pager calls
 			// onContextItemSelected on more than just the fragment 
 			// you expect, for some reason
 			// http://stackoverflow.com/questions/5297842/how-to-handle-oncontextitemselected-in-a-multi-fragment-activity
 			if (item.getGroupId() != MENU_GROUP_ID) {
 				return super.onContextItemSelected(item);
 			}
 			
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 			switch (item.getItemId()) {
 			case R.id.delete:
 				mGames.remove(info.position);
 				((ArrayAdapter<IGame>) getListAdapter()).notifyDataSetChanged();
 				return true;
 			case R.id.complete:
 				((MainActivity)getActivity()).addCompletedGame(mGames.get(info.position));
 				mGames.get(info.position).markCompleted();
 				mGames.remove(info.position);
 				((ArrayAdapter<IGame>) getListAdapter()).notifyDataSetChanged();
 				return true;
 			default:
 				return super.onContextItemSelected(item);
 			}
 		}
 
 		/* (non-Javadoc)
 		 * @see android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
 		 */
 		@Override
 		public void onCreateContextMenu(ContextMenu menu, View v,
 				ContextMenuInfo menuInfo) {
 			super.onCreateContextMenu(menu, v, menuInfo);
 		    menu.add(MENU_GROUP_ID, R.id.delete, Menu.NONE, "Delete Game");
 		    menu.add(MENU_GROUP_ID, R.id.complete, Menu.NONE, "Mark Game Completed");
 		}
 
 		/* (non-Javadoc)
 		 * @see android.support.v4.app.Fragment#onActivityResult(int, int, android.content.Intent)
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public void onActivityResult(int requestCode, int resultCode,
 				Intent data) {
 			if (data == null) {
 				return;
 			}
 			mGames.get(requestCode).addTurn((ITurn)data.getParcelableExtra(GAME_MESSAGE));
 			((ArrayAdapter<IGame>) getListAdapter()).notifyDataSetChanged();
 		}
 	}
 
 	public static class CompletedGameListFragment extends LocalGameListFragment {
 		static final int MENU_GROUP_ID = 2;
 		/**
 		 * Create a new instance of CompletedGameListFragment, providing "games" as an
 		 * argument.
 		 */
 		static CompletedGameListFragment newInstance(ArrayList<IGame> games) {
 			CompletedGameListFragment f = new CompletedGameListFragment();
 
 			Bundle args = new Bundle();
 			args.putParcelableArrayList("games", games);
 			f.setArguments(args);
 
 			return f;
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View v = super.onCreateView(inflater, container, savedInstanceState);
 			// Hide the new_game button
 			v.findViewById(R.id.new_game).setVisibility(View.GONE);
 			return v;
 		}
 
 		@Override
 		public void onListItemClick(ListView l, View v, int position, long id) {
 			Log.i("FragmentList", "Item clicked: " + id);
 			// TODO should use a different activity
 			Intent intent = new Intent(getActivity(), CompletedActivity.class);
 			intent.putExtra(GAME_MESSAGE, mGames.get(position));
 		    startActivity(intent);
 		}
 
 		/* (non-Javadoc)
 		 * @see android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
 		 */
 		@Override
 		public void onCreateContextMenu(ContextMenu menu, View v,
 				ContextMenuInfo menuInfo) {
 		    menu.add(MENU_GROUP_ID, R.id.delete, Menu.NONE, "Delete Game");
 		}
 
 		/* (non-Javadoc)
 		 * @see android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem)
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public boolean onContextItemSelected(MenuItem item) {
 			if (item.getGroupId() != MENU_GROUP_ID) {
 				return false;
 			}
 			
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 			switch (item.getItemId()) {
 			case R.id.delete:
 				mGames.remove(info.position);
 				((ArrayAdapter<IGame>) getListAdapter()).notifyDataSetChanged();
 				return true;
 			default:
 				return super.onContextItemSelected(item);
 			}
 		}
 	}
 
 	public static class FileListFragment extends ListFragment {
 		private ArrayList<String> mFilenames = new ArrayList<String>();
 
 		/**
		 * Create a new instance of FileListFragment
 		 */
 		static FileListFragment newInstance() {
 			FileListFragment fragment = new FileListFragment();
 			//fragment.mFilenames = new ArrayList<String>();
 			return fragment;
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View v = inflater.inflate(R.layout.fragment_list, container,
 					false);
 			return v;
 		}
 
 		@Override
 		public void onActivityCreated(Bundle savedInstanceState) {
 			super.onActivityCreated(savedInstanceState);
 			setListAdapter(new ArrayAdapter<String>(getActivity(),
 					android.R.layout.simple_list_item_1, mFilenames));
 		}
 
 		/* (non-Javadoc)
 		 * @see android.support.v4.app.Fragment#onResume()
 		 */
 		@Override
 		public void onResume() {
 			setFileList();
 			super.onResume();
 		}
 
 		private ArrayList<String> traverse(File f) {
 			Log.i("traverse", "file " + f.toString());
 			ArrayList<String> list = new ArrayList<String>();
 			if (f.isDirectory()) {
 				for (File content : f.listFiles()) {
 					list.addAll(traverse(content));
 				}
 			} else {
 				list.add(f.toString());
 			}
 			return list;
 		}
 
 		@SuppressWarnings("unchecked")
 		private void setFileList() {
 			Log.i("setFileList", "called");
 			mFilenames.clear();
 			mFilenames.addAll(traverse(getActivity().getFilesDir()));
 			((ArrayAdapter<String>) getListAdapter()).notifyDataSetChanged();
 		}
 	}
 }
