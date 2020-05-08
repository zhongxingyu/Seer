 package de.electricdynamite.pasty;
 
 /*
  *  Copyright 2012-2013 Philipp Geschke
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.text.ClipboardManager;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.webkit.URLUtil;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 
 import de.electricdynamite.pasty.PastyLoader.PastyResponse;
 
 @SuppressWarnings("deprecation")
 public class ClipboardFragment extends SherlockListFragment implements LoaderCallbacks<PastyLoader.PastyResponse> {
 	private static final String TAG = ClipboardFragment.class.toString();
 	private boolean LOCAL_LOG = false;
 	protected Drawable mBackground;
 	protected TextView mHelpTextBig;
 	protected TextView mHelpTextSmall;
 	private LayoutInflater mInflater;
 	private ClipboardItemListAdapter mAdapter;
 	private ArrayList<ClipboardItem> mItems;
 	
 	private PastyClipboardFragmentListener activity;
 	private PastyPreferencesProvider prefs;
 
 	public interface PastyClipboardFragmentListener {
         void onPastyClipboardFragmentSignal(int signal);
         void onPastyClipboardFragmentSignal(int signal, int dialogId);
     }
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		// this is really important in order to save the state across screen
 		// configuration changes for example
 		setRetainInstance(true);
 
         if(PastySharedStatics.LOCAL_LOG == true) this.LOCAL_LOG = true; 
 		
 		//LoaderManager.enableDebugLogging(true);
 	
 		mInflater = LayoutInflater.from(getSherlockActivity());
 		activity = (PastyClipboardFragmentListener) getSherlockActivity();
 
 		mHelpTextBig = (TextView) getActivity().findViewById(R.id.tvHelpTextBig);
 		mHelpTextSmall = (TextView) getActivity().findViewById(R.id.tvHelpTextSmall);
 
 		mBackground = mHelpTextSmall.getBackground();
 
 		if (mAdapter == null) {
 			// Set up our ArrayList and ListAdapter
 			mItems = new ArrayList<ClipboardItem>();
 			mAdapter = new ClipboardItemListAdapter(getSherlockActivity(), mItems);
 		}
 		getListView().setAdapter(mAdapter);
 
 		if(this.prefs == null) {
 			this.prefs = new PastyPreferencesProvider(getSherlockActivity().getApplication());
 		} else {
 			prefs.reload();
 		}
 		
 		// Try to reconnect with an existing loader
 		if (getSherlockActivity().getSupportLoaderManager().getLoader(PastyLoader.TASK_CLIPBOARD_FETCH) != null) {
 			if(LOCAL_LOG) Log.v(TAG, "onActivityCreated(): Loader already exists, reconnecting");
 			getSherlockActivity().getSupportLoaderManager().initLoader(PastyLoader.TASK_CLIPBOARD_FETCH, null, this);
 		} else { 
 			if(LOCAL_LOG) Log.v(TAG, "onActivityCreated(): No PastyLoader found");
 			startLoading();
 		}
 		// ----- end magic lines -----
 
 	}
 
 	protected void startLoading() {
 		//Log.d(TAG,"startLoading()");
 		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
 		ProgressBar pbLoading			= (ProgressBar) getSherlockActivity().findViewById(R.id.progressbar_downloading);
 		mHelpTextBig.setText(R.string.helptext_PastyActivity_loading);
 		pbLoading.setVisibility(View.VISIBLE);
 		pbLoading = null;
 		Bundle b = new Bundle();
 		
 		// first time we call this loader, so we need to create a new one
 		getSherlockActivity().getSupportLoaderManager().initLoader(PastyLoader.TASK_CLIPBOARD_FETCH, b, this);
 		b = null;
 	}
 	
 	protected void restartLoading() {
 		Bundle args = new Bundle();
 		restartLoading(args);
 		args = null;
 	}
 	
 	protected void restartLoading(Boolean permitCache) {
 		Bundle args = new Bundle();
 		args.putBoolean("permitCache", permitCache);
 		restartLoading(args);
 		args = null;
 	}
 	
 	protected void restartLoading(Bundle args) {
 		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
 		// --------- the other magic lines ----------
 		// call restart because we want the background work to be executed
 		// again
 		if(LOCAL_LOG) Log.v(TAG, "restartLoading(): re-starting loader");
 		// TODO Make sure this does not get called before startLoading was called, or NULL PE
 		getSherlockActivity().getSupportLoaderManager().restartLoader(PastyLoader.TASK_CLIPBOARD_FETCH, args, this);
 		args = null;
 		// --------- end the other magic lines --------
 	}
 
 	
 	@Override
 	public Loader<PastyLoader.PastyResponse> onCreateLoader(int id, Bundle args) {
 		if(LOCAL_LOG) Log.v(TAG, "onCreateLoader(): New PastyLoader created");
 		return new PastyLoader(getSherlockActivity().getApplicationContext(), id, args);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<PastyLoader.PastyResponse> loader, PastyLoader.PastyResponse response) {
 		
 		ProgressBar pbLoading			= (ProgressBar) getSherlockActivity().findViewById(R.id.progressbar_downloading);
 		pbLoading.setVisibility(View.GONE);
 		pbLoading = null;
 		
     	mHelpTextBig.setTextColor(getResources().getColor(R.color.abs__primary_text_holo_light));
 		mHelpTextBig.setBackgroundDrawable(mBackground);
 		
 		if(response.isFinal) {
 			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
 			if(response.getResultSource() == PastyResponse.SOURCE_CACHE) {
 		    	Toast.makeText(getSherlockActivity().getApplicationContext(), getString(R.string.warning_no_network_short), Toast.LENGTH_SHORT).show();
 			}
 		}
 		if(response.hasException) {
 	    	if(LOCAL_LOG) Log.v(TAG, "onLoadFinished(): Loader delivered exception; calling handleException()");
 	    	// an error occured
 
 			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
 	    	PastyException mException = response.getException();
 	    	handleException(mException);
 	    } else {
 	    	switch(loader.getId()) {
 	    	case PastyLoader.TASK_CLIPBOARD_FETCH:
 	    		JSONArray Clipboard = response.getClipboard();
 	    		mItems.clear();
 	    		mAdapter.notifyDataSetChanged();
 	    		getListView().invalidateViews();
 	    		try {
 	    		    if(Clipboard.length() == 0) {
 	    		       //Clipboard is empty
 	    	        	mHelpTextBig.setText(R.string.helptext_PastyActivity_clipboard_empty);
 	    	        	mHelpTextSmall.setText(R.string.helptext_PastyActivity_how_to_add);
 	    	        	} else {
 	    				if(Clipboard.length() > 15) {
 	    					throw new Exception();
 	    				}
 	    				for (int i = 0; i < Clipboard.length(); i++) {
 	    					JSONObject Item = Clipboard.getJSONObject(i);
 	    					ClipboardItem cbItem = new ClipboardItem(Item.getString("_id"), Item.getString("item"));
 	    					this.mItems.add(cbItem);
 	    				}
 	    			
 	    				mHelpTextBig.setText(R.string.helptext_PastyActivity_copy);
 	    	        	mHelpTextSmall.setText(R.string.helptext_PastyActivity_options);
 	    				
 	    				//Assign adapter to ListView
 	    				ListView listView = (ListView) getSherlockActivity().findViewById(R.id.listItems);
 	    				listView.setAdapter(mAdapter);
 	    				listView.setOnItemClickListener(new OnItemClickListener() { 
 	    					@SuppressLint("NewApi")
 							@Override
 	    					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 	    				    	ClipboardItem Item = mItems.get(position); // get a ClipboardItem from the clicked position
 	    				    	if(Item.isLinkified() && prefs.getClickableLinks()) {
 	    				    		/* If the clicked item was originally linkified and prefs.getClickableLinks() is true, we manually 
 	    				    		 * fire an ACTION_VIEW intent to simulate Linkify() behavior
 	    				    		 */
 	    				    		String url = Item.getText();
 	    				    		if(!URLUtil.isValidUrl(url)) url = "http://"+url;
 	    				    		Intent i = new Intent(Intent.ACTION_VIEW);
 	    				    		i.setData(Uri.parse(url));
 	    				    		startActivity(i);
 	    				    	} else {
 	    				    		/* Else we copy the item to the systems clipboard,
 	    				    		 * show a Toast and finish() the activity
 	    				    		 */
 	    				    		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 	    				    			android.content.ClipboardManager sysClipboard = (android.content.ClipboardManager) getSherlockActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 	    				    			Item.copyToClipboard(sysClipboard);
 	    				    			sysClipboard = null;
 	    				    		} else {
 	    				    			ClipboardManager sysClipboard = (ClipboardManager) getSherlockActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 	    				    			Item.copyToClipboard(sysClipboard);
 	    				    			sysClipboard = null;
 	    				    		}
 		    				    	Toast.makeText(getSherlockActivity().getApplicationContext(), getString(R.string.item_copied), Toast.LENGTH_LONG).show();
 		    					    getSherlockActivity().finish();
 	    				    	}
 	    					}
 	    				});
 	    				registerForContextMenu(listView);
 	    		    }
 	    		} catch (Exception e) {
 	    			e.printStackTrace();
 	    		}
 	    		break;
 	    	default:
 	    		break;
 	    	}
 	    }
 	}
 
 	@Override
 	public void onLoaderReset(Loader<PastyResponse> arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public class ClipboardItemListAdapter extends BaseAdapter {
 			// List of stored ClipboardItems
 	        private List<ClipboardItem> itemList;
 	     
 	        public ClipboardItemListAdapter(Context context, List<ClipboardItem> itemList) {
 	            this.itemList = itemList;
 	        }
 	     
 	        public int getCount() {
 	            return itemList.size();
 	        }
 	     
 	        public ClipboardItem getItem(int position) {
 	            return itemList.get(position);
 	        }
 	        
 	        public long getItemId(int position) {
 	            return position;
 	        }
 	        
 	        public String getClipboardItemId(int position) {
 	        	return itemList.get(position).getId();
 	        }
 	        
 	        public void linkified(int position) {
 	        	this.itemList.get(position).setLinkfied(Boolean.TRUE);
 	        }
 	        
 	        public Boolean isLinkified(int position) {
 	        	return this.itemList.get(position).isLinkified();
 	        }
 	        
 	        public void remove(int position) {
 	        	this.itemList.remove(position);
 	        	this.notifyDataSetChanged();
 	        }
 	        
 	        public void removeAll() {
 	        	this.itemList.clear();
 	        }
 	        
 	        public void delete(int position) {
 	        	ClipboardItem mItem = getItem(position);
             	new ItemDeleteTask().execute(mItem);
             	remove(position); // TODO Implement some kind of callback to remove only upon successful deletion.
 	        }
 
 			@Override
 	        public View getView(int position, View convertView, ViewGroup parent) {
 				
 				View view = convertView;
 
 				if (view == null) {
 					view = mInflater.inflate(R.layout.listitem, parent, false);
 				}
 	            // get the item associated with this position
 	            ClipboardItem Item = itemList.get(position);
 	            
 	            // Select our text view from our view row
 	            TextView tvListItem = (TextView) view.findViewById(R.id.myListitem);
 	            tvListItem.setText(Item.getText());
 	            
 	            if(prefs.getClickableLinks()) {
 		            /* Linkify/ListView / JB problem work around:
 		             * 1. Linkify the item
 		             * 2. If the item was linkified, write it into the ClipboardItem
 		             * 3. Delete the MovementMethod
 		             * 4. (in the onClick callback) check if the clicked ClipboardItem was linkified
 		             * 5. (in the onClick callback) if it was, fire a manual ACTIEN_VIEW intent
 		             * 6. ????
 		             * 7. PROFIT!!!11 (and dirty, dirty code!)
 		             * (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN)
 		             */
 		            Boolean linkified = Linkify.addLinks(tvListItem, Linkify.WEB_URLS);
 		            if(linkified) {
 		    	        linkified(position); // Tell the ClipboardItem that it was linkified.
 		            } 
 		            /* Delete the MovementMethod to prevent linkified items from firing an intent
 		             * at onItemClick() or onItemLongClick()
 		             */
 	            	tvListItem.setMovementMethod(null); 
 		            linkified = null;
 	            } else {
 	            	if(URLUtil.isValidUrl(Item.getText())) {
 	            		linkified(position);
 	            	}
 	            		
 	            }
 	            
 	            return view;
 	        }
 	    }
 		
 
 	private class ItemDeleteTask extends AsyncTask<ClipboardItem, Void, PastyResponse > {
 		
 		private Context context;
 		
 	    /** The system calls this to perform work in a worker thread and
 	      * delivers it the parameters given to AsyncTask.execute() */
 		@Override
 		protected PastyResponse doInBackground(ClipboardItem... item) {
 			if(context == null) {
 				context = getSherlockActivity().getApplicationContext();
 			}
 			PastyPreferencesProvider prefs = new PastyPreferencesProvider(context);
 			PastyClient client = new PastyClient(prefs.getRESTBaseURL(), true);
 			client.setUsername(prefs.getUsername());
 			client.setPassword(prefs.getPassword());
 			PastyResponse result;
 			try {
 				client.deleteItem(item[0]);
 				result = new PastyResponse();
 			} catch (PastyException e) {
 				result = new PastyResponse(e);
 			}
 			return result;
 		}
 	    
 	    /** The system calls this to perform work in the UI thread and delivers
 	      * the result from doInBackground() */
 	    protected void onPostExecute(PastyResponse result) {
 	       if(result.hasException) {
 	    	   handleException(result.getException());
 	       } else {
 	    	   Toast.makeText(context, getString(R.string.item_deleted), Toast.LENGTH_LONG).show();
 	    	   restartLoading(PastyLoader.CACHE_DENIED);
 	       }
 	    }
 	}
 	
 		@Override
 	    public void onCreateContextMenu(ContextMenu menu, View v,
 	        ContextMenuInfo menuInfo) {
 		  AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 	      if (v.getId()==R.id.listItems || v.getId() == R.id.myListimage) {
 	        menu.setHeaderTitle(getString(R.string.itemContextMenuTitle));
 	        String[] menuItems = getResources().getStringArray(R.array.itemContextMenu);
 	        ClipboardItem mItem = mAdapter.getItem(info.position);
 	        if(mItem.isLinkified()) {
 	        	/* Item was Linkified.
 	        	 * Let's add a "open in browser" menuItem
 	        	 */
 	        	menu.add(Menu.NONE,	PastySharedStatics.ITEM_CONTEXTMENU_OPEN_ID,
 	        			0, R.string.itemContextMenu_open);
 	        }
 	        for (int i = 0; i<menuItems.length; i++) {
 	          menu.add(Menu.NONE, i, i, menuItems[i]);
 	        }
 	      }      
 	    }
 	    
 	    @SuppressLint("NewApi")
 		public boolean onContextItemSelected(android.view.MenuItem item) {
 	      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 	      int menuItemIndex = item.getItemId();
 	      ClipboardItem Item = mItems.get(info.position);
 	      switch (menuItemIndex) {
 	      	case PastySharedStatics.ITEM_CONTEXTMENU_COPY_ID:
 	      		// Copy without exit selected
 	    		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 	    			android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSherlockActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 	    			Item.copyToClipboard(clipboard);
 	    			clipboard = null;
 	    		} else {
 	    			ClipboardManager clipboard = (ClipboardManager) getSherlockActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 	    			Item.copyToClipboard(clipboard);
 			    	clipboard = null;
 	    		}
 		    	Toast.makeText(getSherlockActivity().getApplicationContext(), getString(R.string.item_copied), Toast.LENGTH_LONG).show();
 
 	      		break;
 	      	case PastySharedStatics.ITEM_CONTEXTMENU_SHARE_ID:
 	      		// Share to another app
 	      		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
 	      		shareIntent.setType("text/plain");
 	      		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, Item.getText());
 	      		startActivity(Intent.createChooser(shareIntent, getString(R.string.app_share_from_pasty)));
 	      		break;
 	      	case PastySharedStatics.ITEM_CONTEXTMENU_DELETE_ID:
 	      		// Delete selected
 	      		mAdapter.delete(info.position);
 	      		break;
 	      	case PastySharedStatics.ITEM_CONTEXTMENU_OPEN_ID:
 	      		/* If the clicked item was originally linkified, we
 	    		 * fire an ACTION_VIEW intent.
 	    		 */
 	    		String url = Item.getText();
 	    		if(!URLUtil.isValidUrl(url)) url = "http://"+url;
 	    		Intent i = new Intent(Intent.ACTION_VIEW);
 	    		i.setData(Uri.parse(url));
 	    		startActivity(i);
 	    		break;
 	      }
 	      return true;
 	    }
 	    
 	    /** handles PastyExceptions within this Fragment
 	     */
 	    protected void handleException(PastyException mException) {
 	    	switch(mException.errorId) {
     		case PastyException.ERROR_AUTHORIZATION_FAILED:
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_login_failed_title);
 				mHelpTextSmall.setText(R.string.error_login_failed);
 				return;
 			case PastyException.ERROR_IO_EXCEPTION:
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_io_title);
 				mHelpTextSmall.setText(R.string.error_io);
 			case PastyException.ERROR_ILLEGAL_RESPONSE:
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_badanswer_title);
 				mHelpTextSmall.setText(R.string.error_badanswer);
 				return;
 			case PastyException.ERROR_NO_CACHE_EXCEPTION:
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_no_network_title);
 				mHelpTextSmall.setText(R.string.error_no_network);
 				return;
 			case PastyException.ERROR_UNKNOWN:
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_unknown_title);
 				mHelpTextSmall.setText(R.string.error_unknown);
 				Log.e(TAG,mException.getMessage());
 				return;
 			default:
 				break;
 			}
 	    }
 }
