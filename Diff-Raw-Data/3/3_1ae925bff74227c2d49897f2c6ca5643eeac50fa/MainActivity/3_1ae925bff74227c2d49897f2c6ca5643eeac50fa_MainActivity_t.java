 package com.conanyuan.papertelephone;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 
 public class MainActivity extends FragmentActivity {
 	private MyAdapter mAdapter;
     private ViewPager mPager;
     private ArrayList<IGame> mLocalGames;
     private ArrayList<IGame> mNetworkGames;
     private ArrayList<IGame> mCompletedGames;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main_layout);
         
         mLocalGames = new ArrayList<IGame>();
         mNetworkGames = new ArrayList<IGame>();
         mCompletedGames = new ArrayList<IGame>();
 
         List<ArrayList<IGame>> gameList = new ArrayList<ArrayList<IGame>>();
         gameList.add(mLocalGames);
         gameList.add(mNetworkGames);
         gameList.add(mCompletedGames);
         List<String> names = new ArrayList<String>();
         names.add(Page.LOCAL_GAMES.getName());
         names.add(Page.NETWORK_GAMES.getName());
         names.add(Page.COMPLETED_GAMES.getName());
         mAdapter = new MyAdapter(getSupportFragmentManager(),
         		gameList, names);
 
         mPager = (ViewPager)findViewById(R.id.pager);
         mPager.setAdapter(mAdapter);
 
         // Watch for button clicks.
         Button button = (Button)findViewById(R.id.goto_first);
         button.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 mPager.setCurrentItem(0);
             }
         });
         button = (Button)findViewById(R.id.goto_last);
         button.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
                 mPager.setCurrentItem(2);
             }
         });
     }
 
     public static enum Page {
     	LOCAL_GAMES     ("Local Games"),
     	NETWORK_GAMES   ("Network Games"),
    	COMPLETED_GAMES ("Completed Games");
 
     	private final String name;
     	
     	Page(String name) {
     		this.name = name;
     	}
 
     	public String getName() {
     		return name;
     	}
     	
     	private static final int size = Page.values().length;
     	public static int size() {
     		return size;
     	}
     }
     
     public static class MyAdapter extends FragmentPagerAdapter {
     	private List<ArrayList<IGame>> mGameLists;
     	private List<String> mNames;
 
     	public MyAdapter(FragmentManager fragmentManager,
     			List<ArrayList<IGame>> gameLists,
     			List<String> names) {
             super(fragmentManager);
     		mGameLists = gameLists;
     		mNames = names;
     	}
 
         @Override
         public int getCount() {
             return Page.size();
         }
 
         @Override
         public Fragment getItem(int position) {
         	return ArrayListFragment.newInstance(mGameLists.get(position),
         			mNames.get(position));
         }
     }
 
     public static class ArrayListFragment extends ListFragment {
         private ArrayList<IGame> mGames;
         private String mName;
         
         /**
          * Create a new instance of CountingFragment, providing "games"
          * as an argument.
          */
         static ArrayListFragment newInstance(ArrayList<IGame> games,
         		String name) {
         	ArrayListFragment f = new ArrayListFragment();
         	
             Bundle args = new Bundle();
             args.putParcelableArrayList("games", games);
             args.putString("name", name);
             f.setArguments(args);
 
             return f;
         }
 
         /**
          * When creating, retrieve this instance's number from its arguments.
          */
         @Override
         public void onCreate(Bundle savedInstanceState) {
             super.onCreate(savedInstanceState);
             if (getArguments() != null) {
             	mGames = getArguments().getParcelableArrayList("games");
             	mName = getArguments().getString("name");
             } else {
             	mGames = new ArrayList<IGame>();
             	mName = "";
             }
         }
 
         /**
          * The Fragment's UI is just a simple text view showing its
          * instance number.
          */
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {
             View v = inflater.inflate(R.layout.fragment_pager_list, container, false);
             View tv = v.findViewById(R.id.text);
             ((TextView)tv).setText(mName);
             return v;
         }
 
         @Override
         public void onActivityCreated(Bundle savedInstanceState) {
         	super.onActivityCreated(savedInstanceState);
         	setListAdapter(new ArrayAdapter<IGame>(getActivity(),
         			android.R.layout.simple_list_item_1,
         			mGames));
         }
 
         @Override
         public void onListItemClick(ListView l, View v, int position, long id) {
             Log.i("FragmentList", "Item clicked: " + id);
         }
     }
 }
