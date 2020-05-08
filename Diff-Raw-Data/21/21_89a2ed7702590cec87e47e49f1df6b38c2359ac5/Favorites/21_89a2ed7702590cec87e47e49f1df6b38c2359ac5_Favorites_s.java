 package fi.mikuz.boarder.gui.internet;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import fi.mikuz.boarder.R;
 import fi.mikuz.boarder.app.BoarderListActivity;
 import fi.mikuz.boarder.component.internet.InternetBoard;
 import fi.mikuz.boarder.component.internet.InternetVersionBoard;
 import fi.mikuz.boarder.connection.ConnectionErrorResponse;
 import fi.mikuz.boarder.connection.ConnectionListener;
 import fi.mikuz.boarder.connection.ConnectionManager;
 import fi.mikuz.boarder.connection.ConnectionSuccessfulResponse;
 import fi.mikuz.boarder.connection.ConnectionUtils;
 import fi.mikuz.boarder.util.TimeoutProgressDialog;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 public class Favorites extends BoarderListActivity implements ConnectionListener, OnScrollListener {
	private static final String TAG = "InternetFavorites";
 	
 	private ArrayList<InternetVersionBoard> mList;
 	private ListView mListView;
 	
 	private int mMaxResults;
 	private int mListRequestCount;
 	private int mListServerRequestCount;
 	
 	final Handler mHandler = new Handler();
 	TimeoutProgressDialog mWaitDialog;
 	
 	private String mUserId;
 	private String mSessionToken;
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setTitle("Internet Uploads");
 		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		setContentView(R.layout.internet_uploads_list);
 		
 		mList = new ArrayList<InternetVersionBoard>();
 		mListView = (ListView)findViewById(android.R.id.list);
 		mMaxResults = 40;
 		
 		mWaitDialog = new TimeoutProgressDialog(Favorites.this, "Waiting for response", TAG, true);
 		
 		Bundle extras = getIntent().getExtras();
 		@SuppressWarnings("unchecked")
 		HashMap<String,String> lastSession = (HashMap<String,String>) extras.getSerializable(InternetMenu.LOGIN_KEY);
 		
 		try {
 			mUserId = lastSession.get(InternetMenu.USER_ID_KEY);
 			mSessionToken = lastSession.get(InternetMenu.SESSION_TOKEN_KEY);
 			
 			refreshList();
 			getListView().setOnScrollListener(this);
 		} catch (NullPointerException e) {
 			Toast.makeText(Favorites.this, "Please login", Toast.LENGTH_LONG).show();
 			Favorites.this.finish();
 		}
 		
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		InternetBoard board = ((BoardListAdapter)mListView.getAdapter()).getItem(position);
 		if (!board.getUploaderUsername().equals("")) {
 			Intent i = new Intent(Favorites.this, DownloadBoard.class);
 			i.putExtra(DownloadBoard.SHOW_KEY, DownloadBoard.SHOW_INTERNET_BOARD);
 	    	i.putExtra(InternetMenu.BOARD_ID_KEY, board.getBoardId());
 	    	i.putExtra(DownloadBoardList.LOGGED_IN_KEY, true);
 	    	i.putExtra(InternetMenu.USER_ID_KEY, mUserId);
 	    	i.putExtra(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
 	    	
 	    	startActivity(i);
 		}
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.internet_uploads_bottom, menu);
 	    return true;
     }
     
     private void populateList() {
     	if (mListView.getAdapter() == null) {
     		Log.d(TAG, "Creating adapter");
     		BoardListAdapter adapter = new BoardListAdapter(mList, this);
     		mListView.setAdapter(adapter);
     		Log.d(TAG, "count is " + adapter.getCount());
     	} else {
     		Log.d(TAG, "Refreshing list, sendin " + mList.size() + " elements");
     		((BoardListAdapter)mListView.getAdapter()).refill(mList);
     		Log.d(TAG, "count is " + ((BoardListAdapter)mListView.getAdapter()).getCount());
     	}
     }
 	
     private void getBoards() {
     	HashMap<String, String> sendList = new HashMap<String, String>();
     	sendList.put(InternetMenu.USER_ID_KEY, mUserId);
     	sendList.put(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
     	sendList.put(InternetMenu.REQUEST_COUNT_KEY, Integer.toString(mListServerRequestCount));
     	sendList.put(InternetMenu.MAX_RESULTS_KEY, Integer.toString(mMaxResults));
     	new ConnectionManager(Favorites.this, InternetMenu.mFavoriteListURL, sendList);
     }
     
     private void refreshList() {
 		mList.clear();
 		mListRequestCount = 0;
 		mListServerRequestCount = 0;
 		getBoards();
 		mListRequestCount++;
 	}
 	
     public class BoardListAdapter extends BaseAdapter {
 
 		private List<InternetVersionBoard> boardList;
      
         private Context context;
      
         public BoardListAdapter(List<InternetVersionBoard> boards, Context context) {
         	boardList = new ArrayList<InternetVersionBoard>();
             boardList.addAll(boards);
             this.context = context;
         }
         
         public void add(InternetVersionBoard internetBoard) {
         	this.boardList.add(internetBoard);
         }
      
         public int getCount() {
             return boardList.size();
         }
      
         public InternetVersionBoard getItem(int position) {
             return boardList.get(position);
         }
      
         public long getItemId(int position) {
             return boardList.get(position).getId();
         }
      
         public View getView(int position, View convertView, ViewGroup parent) {
         	RelativeLayout itemLayout;
         	InternetVersionBoard internetBoard = getItem(position);
      
             itemLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.internet_uploads_row, parent, false);
      
             TextView uploaderUsername = (TextView) itemLayout.findViewById(R.id.uploaderUsername);
             uploaderUsername.setText(internetBoard.getUploaderUsername());
      
             TextView boardName = (TextView) itemLayout.findViewById(R.id.boardName);
             boardName.setText(internetBoard.getBoardName());
             
             TextView uploaderRating = (TextView) itemLayout.findViewById(R.id.uploaderRating);
             uploaderRating.setText(internetBoard.getRating());
             
             if (internetBoard.getBoardVersion() > internetBoard.getFavoriteBoardVersion()) {
             	uploaderUsername.setTextColor(Color.YELLOW);
             	boardName.setTextColor(Color.YELLOW);
             	uploaderRating.setTextColor(Color.YELLOW);
             }
      
             return itemLayout;
         }
         
         public void refill(final List<InternetVersionBoard> boards) {
         	boardList.clear();
         	boardList.addAll(boards);
             notifyDataSetChanged();
         }
      
     }
 
 	@Override
 	public void onConnectionSuccessful(ConnectionSuccessfulResponse connectionSuccessfulResponse) throws JSONException {
 		ConnectionUtils.connectionSuccessful(Favorites.this, connectionSuccessfulResponse);
 		mWaitDialog.dismiss();
 		
 		if (ConnectionUtils.checkConnectionId(connectionSuccessfulResponse, InternetMenu.mFavoriteListURL)) {
 			if (connectionSuccessfulResponse.getJSONObject().isNull("data")) {
 				Log.d(TAG, "Got null");
 				setListEmpty();
 			} else {
 				JSONArray jBoards = connectionSuccessfulResponse.getJSONObject().getJSONArray("data");
 				removeSpecialObjects();
 
 				int i;
 				for(i = 0;i < jBoards.length();i++){						
 					mList.add(new InternetVersionBoard(jBoards.getJSONObject(i), true));
 				}
 				mListServerRequestCount++;
 				if (i >= 40) addListLoading();
 			}
 			populateList();
 		}
 	}
 	
 	private void setListEmpty() {
 		InternetVersionBoard board = new InternetVersionBoard();
 		
 		board.setBoardName("Nothing here!");
 		board.setUploaderUsername("");
 		board.setRating("");
 		
 		mList.add(board);
 	}
 	
 	private void addListLoading() {
 		InternetVersionBoard board = new InternetVersionBoard();
 		board.setUploaderUsername("");
 		board.setBoardName("   - Loading -");
 		
 		mList.add(board);
 	}
 	
 	private void removeSpecialObjects() {
 		for (InternetBoard board : mList) {
 			if (board.getBoardName().equals("   - Loading -") && board.getUploaderUsername().equals("")) mList.remove(board);
 		}
 	}
 
 	@Override
 	public void onConnectionError(ConnectionErrorResponse connectionErrorResponse) {
 		ConnectionUtils.connectionError(this, connectionErrorResponse, TAG);
 		mWaitDialog.dismiss();
 	}
 
 	@Override
 	public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
 		if(firstVisible + visibleCount >= totalCount-11 && totalCount >= (mListRequestCount)*mMaxResults) {
 			getBoards();
 			mListRequestCount++;
 		}
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {}
 
 }
