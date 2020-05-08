 package fi.mikuz.boarder.gui.internet;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.thoughtworks.xstream.XStream;
 
 import fi.mikuz.boarder.R;
 import fi.mikuz.boarder.app.BoarderListActivity;
 import fi.mikuz.boarder.component.internet.InternetFullBoard;
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
 public class Uploads extends BoarderListActivity implements ConnectionListener, OnScrollListener {
	private static final String TAG = Uploads.class.getSimpleName();
 	
 	private ArrayList<InternetFullBoard> mList;
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
 		setContentView(R.layout.internet_uploads_list);
 		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
 		
 		mList = new ArrayList<InternetFullBoard>();
 		mListView = (ListView)findViewById(android.R.id.list);
 		mMaxResults = 40;
 		
 		mWaitDialog = new TimeoutProgressDialog(Uploads.this, "Waiting for response", TAG, true);
 		
 		Bundle extras = getIntent().getExtras();
 		@SuppressWarnings("unchecked")
 		HashMap<String,String> lastSession = (HashMap<String,String>) extras.getSerializable(InternetMenu.LOGIN_KEY);
 		
 		try {
 			mUserId = lastSession.get(InternetMenu.USER_ID_KEY);
 			mSessionToken = lastSession.get(InternetMenu.SESSION_TOKEN_KEY);
 			
 			refreshList();
 			getListView().setOnScrollListener(this);
 		} catch (NullPointerException e) {
 			Toast.makeText(Uploads.this, "Please login", Toast.LENGTH_LONG).show();
 			Uploads.this.finish();
 		}
 		
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		
 		final InternetFullBoard board = ((BoardListAdapter)mListView.getAdapter()).getItem(position);
 		
 		if (!board.getUploaderUsername().equals("")) {
 			final CharSequence[] items = {"Edit", "Delete"};
 			
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(board.getBoardName());
 			builder.setItems(items, new DialogInterface.OnClickListener() {
 	    	    public void onClick(DialogInterface dialog, int item) {
 	    	    	if (item == 0) {
 	    	    		boardUploader(board, InternetMenu.PHP_OPERATION_EDIT);
 	    	    	} else if (item == 1) {
 	    	    		AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(Uploads.this);
 	    	    		deleteBuilder.setTitle("Deleting upload");
 	    	    		deleteBuilder.setMessage("Are you sure you want to permanently delete upload '" + board.getBoardName() + "'?");
 	    	    		
 	    	    		deleteBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 	    			  		public void onClick(DialogInterface dialog, int whichButton) {
 	    			  			HashMap<String, String> sendList = new HashMap<String, String>();
 	    	                	sendList.put(InternetMenu.UPLOADER_ID_KEY, mUserId);
 	    	                	sendList.put(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
 	    	                	sendList.put(InternetMenu.BOARD_ID_KEY, Integer.toString(board.getBoardId()));
 	    	    	    		new ConnectionManager(Uploads.this, InternetMenu.mDeleteUploadedBoardURL, sendList);
 	    			  		}
 	    	    		});
 	    	    		deleteBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
 	    			  		public void onClick(DialogInterface dialog, int whichButton) {
 	    			  			dialog.dismiss();
 	    			  		}
 	    	    		});
 	    	    		AlertDialog deleteAlert = deleteBuilder.create();
 	    	    		deleteAlert.show();
 	    	    	}
 	    	    }
 	    	});
 	    	AlertDialog alert = builder.create();
 	    	alert.show();
 		}
 	}
 	
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.internet_uploads_bottom, menu);
 	    return true;
     }
 	
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch(item.getItemId()) {
 		case R.id.menu_upload_board:
 			boardUploader(null, InternetMenu.PHP_OPERATION_ADD);
 			return true;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 	
 	private void boardUploader(final InternetFullBoard board, final int phpOperation) {
 		LayoutInflater inflater = (LayoutInflater) Uploads.this.
 				getSystemService(LAYOUT_INFLATER_SERVICE);
 		final View layout = inflater.inflate(R.layout.internet_uploads_alert_upload_board,
 				(ViewGroup) findViewById(R.id.alert_settings_root));
 		
 		
 		TextView wikiLink = (TextView) layout.findViewById(R.id.wikiLink);
		wikiLink.setText("\n  Guide here!\n\n");
 		
 		wikiLink.setOnClickListener(new OnClickListener() {
             public void onClick(View v) {
             	Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
						Uri.parse("https://github.com/Mikuz/Boarder/wiki/Uploading-guide"));
 				startActivity(browserIntent);
             }
         });
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(Uploads.this);
 		builder.setView(layout);
 		builder.setTitle("Upload board");
 		
 		refreshUploadAlertBoard(layout, board);
 
 		final Button sendButton = (Button) layout.findViewById(R.id.sendButton);
 		sendButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				mWaitDialog = new TimeoutProgressDialog(Uploads.this, "Waiting for response", TAG, false);
 				HashMap<String, String> sendList = getSendList(layout, board, phpOperation);
 				new ConnectionManager(Uploads.this, InternetMenu.mUploadBoardURL, sendList);
 			}
 		});
 		
 		final Button previewButton = (Button) layout.findViewById(R.id.previewButton);
 		previewButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				HashMap<String, String> sendList = getSendList(layout, board, phpOperation);
 				
 				// Simulate a board from web
 				sendList.put(InternetMenu.RATING_KEY, "0");
 				sendList.put(InternetMenu.BOARD_VERSION_KEY, "0");
 				sendList.put(InternetMenu.USERNAME_KEY, "Preview");
 				
 				JSONObject databaseValues = new JSONObject();
 				if (sendList != null) {
 					for (String key : sendList.keySet()) {
 						try {
 							databaseValues.put(key, sendList.get(key));
 						} catch (JSONException e) {
 							Log.e(TAG, "Error putting '" + key + "' to JSONObject as database value");
 						}
 					}
 				}
 				
 				JSONArray jArray = new JSONArray();
 				jArray.put(databaseValues);
 				JSONObject fakeMessage = null;
 				try {
 					fakeMessage = new JSONObject();
 					fakeMessage.put(ConnectionUtils.returnData, jArray);
 				} catch (JSONException e) {
 					Log.e(TAG, "Error constructing fake json message", e);
 				}
 				
 				Intent i = new Intent(Uploads.this, DownloadBoard.class);
 				XStream xstream = new XStream();
 				i.putExtra(DownloadBoard.SHOW_KEY, DownloadBoard.SHOW_PREVIEW_BOARD);
 				i.putExtra(DownloadBoard.JSON_KEY, xstream.toXML(fakeMessage));
 				startActivity(i);
 			}
 		});
 
 		builder.show();
 	}
 	
 	private void refreshUploadAlertBoard(final View layout, final InternetFullBoard board) {
 		
 		final EditText boardNameInput = (EditText) layout.findViewById(R.id.boardNameInput);
 		final EditText boardVersionInput = (EditText) layout.findViewById(R.id.boardVersionInput);
 		final EditText boardDescriptionInput = (EditText) layout.findViewById(R.id.boardDescriptionInput);
 		final EditText boardURL0Input = (EditText) layout.findViewById(R.id.boardUrl0Input);
 		final EditText boardURL1Input = (EditText) layout.findViewById(R.id.boardUrl1Input);
 		final EditText boardURL2Input = (EditText) layout.findViewById(R.id.boardUrl2Input);
 		final EditText boardURL3Input = (EditText) layout.findViewById(R.id.boardUrl3Input);
 		final EditText boardURL4Input = (EditText) layout.findViewById(R.id.boardUrl4Input);
 		final EditText boardScreenshotURL0Input = (EditText) layout.findViewById(R.id.boardScreenshotUrl0Input);
 		
 		if (board != null) {
 			boardNameInput.setText(board.getBoardName());
 			boardVersionInput.setText(Integer.toString(board.getBoardVersion()));
 			boardDescriptionInput.setText(board.getDescription());
 			boardScreenshotURL0Input.setText(board.getScreenshot0Url());
 			
 			int i = 0;
 			for (String boardUrl : board.getUrlList()) {
 				switch (i) {
 					case 0:
 						boardURL0Input.setText(boardUrl);
 						break;
 					case 1:
 						boardURL1Input.setText(boardUrl);
 						break;
 					case 2:
 						boardURL2Input.setText(boardUrl);
 						break;
 					case 3:
 						boardURL3Input.setText(boardUrl);
 						break;
 					case 4:
 						boardURL4Input.setText(boardUrl);
 						break;
 				}
 				i++;
 			}
 		}
 	}
 	
 	private HashMap<String, String> getSendList(final View layout, final InternetFullBoard board, final int phpOperation) {
 		
 		final EditText boardNameInput = (EditText) layout.findViewById(R.id.boardNameInput);
 		final EditText boardVersionInput = (EditText) layout.findViewById(R.id.boardVersionInput);
 		final EditText boardDescriptionInput = (EditText) layout.findViewById(R.id.boardDescriptionInput);
 		final EditText boardURL0Input = (EditText) layout.findViewById(R.id.boardUrl0Input);
 		final EditText boardURL1Input = (EditText) layout.findViewById(R.id.boardUrl1Input);
 		final EditText boardURL2Input = (EditText) layout.findViewById(R.id.boardUrl2Input);
 		final EditText boardURL3Input = (EditText) layout.findViewById(R.id.boardUrl3Input);
 		final EditText boardURL4Input = (EditText) layout.findViewById(R.id.boardUrl4Input);
 		final EditText boardScreenshotURL0Input = (EditText) layout.findViewById(R.id.boardScreenshotUrl0Input);
 		
 		HashMap<String, String> sendList = new HashMap<String, String>();
 
 		HashMap<String, String> urlMap = new HashMap<String, String>();
 		urlMap.put(InternetMenu.BOARD_URL_0_KEY, boardURL0Input.getText().toString());
 		urlMap.put(InternetMenu.BOARD_URL_1_KEY, boardURL1Input.getText().toString());
 		urlMap.put(InternetMenu.BOARD_URL_2_KEY, boardURL2Input.getText().toString());
 		urlMap.put(InternetMenu.BOARD_URL_3_KEY, boardURL3Input.getText().toString());
 		urlMap.put(InternetMenu.BOARD_URL_4_KEY, boardURL4Input.getText().toString());
 
 		for (String key : urlMap.keySet()) {
 			String value = urlMap.get(key);
 			sendList.put(key, value);
 		}
 		
 		if (phpOperation == InternetMenu.PHP_OPERATION_EDIT) {
 			sendList.put(InternetMenu.BOARD_ID_KEY, Long.toString(board.getBoardId()));
 		}
 
 		sendList.put(InternetMenu.USER_ID_KEY, mUserId);
 		sendList.put(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
 		sendList.put(InternetMenu.BOARD_NAME_KEY, boardNameInput.getText().toString());
 		sendList.put(InternetMenu.BOARD_VERSION_KEY, boardVersionInput.getText().toString());
 		sendList.put(InternetMenu.BOARD_DESCRIPTION_KEY, boardDescriptionInput.getText().toString());
 		sendList.put(InternetMenu.BOARD_SCREENSHOT_0_URL_KEY, boardScreenshotURL0Input.getText().toString());
 		sendList.put(InternetMenu.PHP_OPERATION_KEY, Integer.toString(phpOperation));
 		
 		return sendList;
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
 	
     private void getUploadedBoards() {
     	HashMap<String, String> sendList = new HashMap<String, String>();
     	sendList.put(InternetMenu.USER_ID_KEY, mUserId);
     	sendList.put(InternetMenu.SESSION_TOKEN_KEY, mSessionToken);
     	sendList.put(InternetMenu.REQUEST_COUNT_KEY, Integer.toString(mListServerRequestCount));
     	sendList.put(InternetMenu.MAX_RESULTS_KEY, Integer.toString(mMaxResults));
     	new ConnectionManager(Uploads.this, InternetMenu.mUserUploadListURL, sendList);
     }
     
     private void refreshList() {
 		mList.clear();
 		mListRequestCount = 0;
 		mListServerRequestCount = 0;
 		getUploadedBoards();
 		mListRequestCount++;
 	}
 	
     public class BoardListAdapter extends BaseAdapter {
 
 		private List<InternetFullBoard> boardList;
      
         private Context context;
      
         public BoardListAdapter(List<InternetFullBoard> boards, Context context) {
         	boardList = new ArrayList<InternetFullBoard>();
             boardList.addAll(boards);
             this.context = context;
         }
         
         public void add(InternetFullBoard internetFullBoard) {
         	this.boardList.add(internetFullBoard);
         }
      
         public int getCount() {
             return boardList.size();
         }
      
         public InternetFullBoard getItem(int position) {
             return boardList.get(position);
         }
      
         public long getItemId(int position) {
             return boardList.get(position).getId();
         }
      
         public View getView(int position, View convertView, ViewGroup parent) {
         	RelativeLayout itemLayout;
         	InternetFullBoard internetFullBoard = getItem(position);
      
             itemLayout = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.internet_uploads_row, parent, false);
      
             TextView uploaderUsername = (TextView) itemLayout.findViewById(R.id.uploaderUsername);
             uploaderUsername.setText(internetFullBoard.getUploaderUsername());
      
             TextView boardName = (TextView) itemLayout.findViewById(R.id.boardName);
             boardName.setText(internetFullBoard.getBoardName());
             
             TextView uploaderRating = (TextView) itemLayout.findViewById(R.id.uploaderRating);
             uploaderRating.setText(internetFullBoard.getRating());
      
             return itemLayout;
         }
         
         public void refill(final List<InternetFullBoard> boards) {
         	boardList.clear();
         	boardList.addAll(boards);
             notifyDataSetChanged();
         }
      
     }
 
 	@Override
 	public void onConnectionSuccessful(ConnectionSuccessfulResponse connectionSuccessfulResponse) throws JSONException {
 		ConnectionUtils.connectionSuccessful(Uploads.this, connectionSuccessfulResponse);
 		mWaitDialog.dismiss();
 		
 		if (ConnectionUtils.checkConnectionId(connectionSuccessfulResponse, InternetMenu.mDeleteUploadedBoardURL)) {
 			refreshList();
 		} else if (ConnectionUtils.checkConnectionId(connectionSuccessfulResponse, InternetMenu.mUploadBoardURL)) {
 			refreshList();
 		} else if (ConnectionUtils.checkConnectionId(connectionSuccessfulResponse, InternetMenu.mUserUploadListURL)) {
 			if (connectionSuccessfulResponse.getJSONObject().isNull("data")) {
 				Log.d(TAG, "Got null");
 				setListEmpty();
 			} else {
 				JSONArray jBoards = connectionSuccessfulResponse.getJSONObject().getJSONArray("data");
 				removeSpecialObjects();
 				
 				int i;
 				for(i = 0;i < jBoards.length();i++){						
 					mList.add(new InternetFullBoard(jBoards.getJSONObject(i)));
 				}
 				mListServerRequestCount++;
 				if (i >= 40) addListLoading();
 			}
 			populateList();
 		}
 	}
 	
 	private void setListEmpty() {
 		InternetFullBoard board = new InternetFullBoard();
 		
		board.setBoardName("Use the menu to upload");
 		board.setUploaderUsername("");
 		board.setRating("");
 		
 		mList.add(board);
 	}
 	
 	private void addListLoading() {
 		InternetFullBoard board = new InternetFullBoard();
 		board.setUploaderUsername("");
 		board.setBoardName("   - Loading -");
 		
 		mList.add(board);
 	}
 	
 	private void removeSpecialObjects() {
 		for (InternetFullBoard board : mList) {
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
 			getUploadedBoards();
 			mListRequestCount++;
 		}
 	}
 
 	@Override
 	public void onScrollStateChanged(AbsListView view, int scrollState) {}
 
 }
