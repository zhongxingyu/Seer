 package de.electricdynamite.pasty;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.text.ClipboardManager;
 import android.text.Layout;
 import android.text.Selection;
 import android.text.Spannable;
 import android.text.Spanned;
 import android.text.method.LinkMovementMethod;
 import android.text.style.ClickableSpan;
 import android.text.style.URLSpan;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.actionbarsherlock.view.Menu;
 
 import de.electricdynamite.pasty.PastyLoader.PastyResponse;
 
 public class ClipboardFragment extends SherlockListFragment implements LoaderCallbacks<PastyLoader.PastyResponse> {
 	private static final String TAG = ClipboardFragment.class.toString();
 	private LayoutInflater mInflater;
 	private Resources mRes;
 	private ClipboardItemListAdapter mAdapter;
 	private ArrayList<ClipboardItem> mItems;
 
 	private boolean mFirstRun = true;
 	private final Handler mHandler = new Handler();
 	
 	private PastyClipboardFragmentListener activity;
 
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
 
 		//LoaderManager.enableDebugLogging(true);
 
 		mRes = getResources();
 		mInflater = LayoutInflater.from(getSherlockActivity());
 		activity = (PastyClipboardFragmentListener) getSherlockActivity();
 
 
 		// you only need to instantiate these the first time your fragment is
 		// created; then, the method above will do the rest
 		if (mAdapter == null) {
 			mItems = new ArrayList<ClipboardItem>();
 			mAdapter = new ClipboardItemListAdapter(getSherlockActivity(), mItems);
 		}
 		getListView().setAdapter(mAdapter);
 
 		// ---- magic lines starting here -----
 		// call this to re-connect with an existing
 		// loader (after screen configuration changes for e.g!)
 		LoaderManager lm = getLoaderManager();
 		if (lm.getLoader(PastyLoader.TASK_CLIPBOARD_FETCH) != null) {
 			Log.d(TAG, "onActivityCreated(): Loader already exists, reconnecting");
 			lm.initLoader(PastyLoader.TASK_CLIPBOARD_FETCH, null, this);
 		} else { 
 			Log.d(TAG, "onActivityCreated(): No PastyLoader found");
 			startLoading();
 		}
 		// ----- end magic lines -----
 
 		if(mFirstRun) {
 			startLoading();
 			mFirstRun = false;
 		}
 	}
 
 	protected void startLoading() {
 		Log.d(TAG,"startLoading()");
 		//showDialog();
 		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
 		TextView mHelpTextBig			= (TextView) getSherlockActivity().findViewById(R.id.tvHelpTextBig);
 		ProgressBar pbLoading			= (ProgressBar) getSherlockActivity().findViewById(R.id.progressbar_downloading);
 		mHelpTextBig.setText(R.string.helptext_PastyActivity_loading);
 		pbLoading.setVisibility(View.VISIBLE);
 		mHelpTextBig = null;
 		pbLoading = null;
 		Bundle b = new Bundle();
 		
 		// first time we call this loader, so we need to create a new one
 		getLoaderManager().initLoader(PastyLoader.TASK_CLIPBOARD_FETCH, b, this);
 		b = null;
 		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
 	}
 	
 	protected void restartLoading() {
 		//showDialog();
 
 
 		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
 		// --------- the other magic lines ----------
 		// call restart because we want the background work to be executed
 		// again
 		Log.d(TAG, "restartLoading(): re-starting loader");
 		Bundle b = new Bundle();
 		// TODO Make sure this does not get called before startLoading was called, or NULL PE
 		getLoaderManager().restartLoader(PastyLoader.TASK_CLIPBOARD_FETCH, b, this);
 		b = null;
 		// --------- end the other magic lines --------
 	}
 
 	
 	@Override
 	public Loader<PastyLoader.PastyResponse> onCreateLoader(int id, Bundle args) {
 		Log.d(TAG, "onCreateLoader(): New PastyLoader created");
 		return new PastyLoader(getSherlockActivity(), id);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<PastyLoader.PastyResponse> loader, PastyLoader.PastyResponse response) {
 		ProgressBar pbLoading			= (ProgressBar) getSherlockActivity().findViewById(R.id.progressbar_downloading);
 		pbLoading.setVisibility(View.GONE);
 		pbLoading = null;
 
     	TextView mHelpTextBig = (TextView) getSherlockActivity().findViewById(R.id.tvHelpTextBig);
     	TextView mHelpTextSmall = (TextView) getSherlockActivity().findViewById(R.id.tvHelpTextSmall);
 		mHelpTextBig.setTextColor(getResources().getColor(R.color.abs__primary_text_holo_light));
 		mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.abs__background_holo_light));
 		
 		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(Boolean.FALSE); 
 	    if(response.hasException) {
 	    	Log.d(TAG, "onLoadFinished(): Loader delivered exception; calling handleException()");
 	    	// an error occured
 	    	PastyException mException = response.getException();
 	    	handleException(mException);
 	    } else {
 	    	switch(loader.getId()) {
 	    	case PastyLoader.TASK_CLIPBOARD_FETCH:
 	    		Log.d(TAG, "Loader delivered TASK_CLIPBOARD_FETCH without exception");
 	    		JSONArray Clipboard = response.getClipboard();
 	    		mItems.clear();
 	    		mAdapter.notifyDataSetChanged();
 	    		getListView().invalidateViews();
 	    		try {
 	    		    if(Clipboard.length() == 0) {
 	    		       //Clipboard is empty
 	    	        	mHelpTextBig.setText(R.string.helptext_PastyActivity_clipboard_empty);
 	    	        	mHelpTextBig = null;
 	    	        	mHelpTextSmall.setText(R.string.helptext_PastyActivity_how_to_add);
 	    	        	mHelpTextSmall = null;
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
 	    				mHelpTextBig = null;
 	    	        	mHelpTextSmall = null;
 	    			
 	    				//Assign adapter to ListView
 	    				ListView listView = (ListView) getSherlockActivity().findViewById(R.id.listItems);
 	    				listView.setAdapter(mAdapter);
 	    				listView.setOnItemClickListener(new OnItemClickListener() { 
 	    					@Override
 	    					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 	    				    	ClipboardItem Item = mItems.get(position); // get a ClipboardItem from the clicked position
 	    				    	if(Item.isLinkified()) {
 	    				    		/* If the clicked item was originally linkified, we manually 
 	    				    		 * fire an ACTION_VIEW intent to simulate Linkify() behavior
 	    				    		 */
 	    				    		String url = Item.getText();
 	    				    		Intent i = new Intent(Intent.ACTION_VIEW);
 	    				    		i.setData(Uri.parse(url));
 	    				    		startActivity(i);
 	    				    	} else {
 	    				    		/* Else we copy the item to the systems clipboard,
 	    				    		 * show a Toast and finish() the activity
 	    				    		 */
 		    						ClipboardManager sysClipboard = (ClipboardManager) getSherlockActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 		    						Item.copyToClipboard(sysClipboard);
 		    				    	Context context = getSherlockActivity().getApplicationContext();
 		    				    	CharSequence text = getString(R.string.item_copied);
 		    				    	int duration = Toast.LENGTH_LONG;
 		    				    	Toast toast = Toast.makeText(context, text, duration);
 		    				    	toast.show();
 		    				    	toast = null;
 		    				    	context = null;
 		    				    	sysClipboard = null;
 		    				    	text = null;
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
 	        private Context context;
 	     
 	        public ClipboardItemListAdapter(Context context, List<ClipboardItem> itemList) {
 	            this.itemList = itemList;
 	            this.context = context;
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
 	        	this.itemList.get(position).linkfied();
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
 				Wrapper wrapper;
 
 				if (view == null) {
 					view = mInflater.inflate(R.layout.listitem, parent, false);
 					wrapper = new Wrapper(view);
 					view.setTag(wrapper);
 				} else {
 					wrapper = (Wrapper) view.getTag();
 				}
 	            // get the item associated with this position
 	            ClipboardItem Item = itemList.get(position);
 	            
 	            // Select our text view from our view row
 	            TextView tvListItem = (TextView) view.findViewById(R.id.myListitem);
 	            tvListItem.setText(Item.getText());
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
 				context = getSherlockActivity().getBaseContext();
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
 	    	int duration = Toast.LENGTH_LONG;
 	   		CharSequence text = getString(R.string.item_deleted);
 	   		Toast toast = Toast.makeText(context, text, duration);
 	   		toast.show();
 	   		toast = null;
 	   		context = null;
 	       }
 	    }
 	}
 	
 		// use an wrapper (or view holder) object to limit calling the
 		// findViewById() method, which parses the entire structure of your
 		// XML in search for the ID of your view
 		private static class Wrapper {
 			private final View mRoot;
 			private TextView tvHelpTextBig;
 			private TextView tvHelpTextSmall;
 			private ProgressBar pbLoading;
 			
 			public static final int VIEW_HELPTEXT_BIG = 0x1;
 			public static final int VIEW_HELPTEXT_SMALL = 0x2;
 			public static final int VIEW_PROGRESS_LOADING = 0x3;
 
 			public Wrapper(View root) {
 				mRoot = root;
 			}
 
 			public TextView getTextView(int tv) {
 				switch (tv) {
 				case VIEW_HELPTEXT_BIG:
 					if (tvHelpTextBig == null) {
 						tvHelpTextBig = (TextView) mRoot.findViewById(R.id.tvHelpTextBig);
 					}
 					return tvHelpTextBig;
 				case VIEW_HELPTEXT_SMALL:
 					if (tvHelpTextSmall == null) {
 						tvHelpTextSmall = (TextView) mRoot.findViewById(R.id.tvHelpTextSmall);
 					}
 					return tvHelpTextBig;
 				default:
 					return null;
 				}
 			}
 
 			public View getBar() {
 				if (pbLoading == null) {
 					pbLoading = (ProgressBar) mRoot.findViewById(R.id.progressbar_downloading);
 				}
 				return pbLoading;
 			}
 		}
 		
 		@Override
 	    public void onCreateContextMenu(ContextMenu menu, View v,
 	        ContextMenuInfo menuInfo) {
 	      if (v.getId()==R.id.listItems || v.getId() == R.id.myListimage) {
 	        menu.setHeaderTitle(getString(R.string.itemContextMenuTitle));
 	        String[] menuItems = getResources().getStringArray(R.array.itemContextMenu);
 	        for (int i = 0; i<menuItems.length; i++) {
 	          menu.add(Menu.NONE, i, i, menuItems[i]);
 	        }
 	      }      
 	    }
 	    
 	    public boolean onContextItemSelected(android.view.MenuItem item) {
 	      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 	      int menuItemIndex = item.getItemId();
 	      ClipboardItem Item = mItems.get(info.position);
 	      switch (menuItemIndex) {
 	      	case PastySharedStatics.ITEM_CONTEXTMENU_COPY_ID:
 	      		// Copy without exit selected
 	      		ClipboardManager clipboard = (ClipboardManager) getSherlockActivity().getSystemService(Context.CLIPBOARD_SERVICE);
 				Item.copyToClipboard(clipboard);
 		    	Context context = getSherlockActivity().getApplicationContext();
 		    	CharSequence text = getString(R.string.item_copied);
 		    	Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
 		    	toast.show();
 		    	toast = null;
 		    	context = null;
 		    	clipboard = null;
 		    	text = null;
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
 	      }
 	      return true;
 	    }
 	    
 	    /** handles PastyExceptions within this Fragment
 	     */
 	    protected void handleException(PastyException mException) {
 	    	TextView mHelpTextBig = (TextView) getSherlockActivity().findViewById(R.id.tvHelpTextBig);
 	    	TextView mHelpTextSmall = (TextView) getSherlockActivity().findViewById(R.id.tvHelpTextSmall);
 	    	switch(mException.errorId) {
     		case PastyException.ERROR_AUTHORIZATION_FAILED:
     			Log.d(TAG, "ERROR_AUTHORIZATION_FAILED EXCEPTION");
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_login_failed_title);
 				mHelpTextSmall.setText(R.string.error_login_failed);
 				return;
 			case PastyException.ERROR_IO_EXCEPTION:
     			Log.d(TAG, "ERROR_IO_EXCEPTION");
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_io_title);
 				mHelpTextSmall.setText(R.string.error_io);
 			case PastyException.ERROR_ILLEGAL_RESPONSE:
 				Log.d(TAG, "ERROR_ILLEGAL_RESPONSE EXCEPTION");
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_badanswer_title);
 				mHelpTextSmall.setText(R.string.error_badanswer);
 				return;
 			case PastyException.ERROR_UNKNOWN:
 				mHelpTextBig.setTextColor(getResources().getColor(R.color.white));
 				mHelpTextBig.setBackgroundColor(getResources().getColor(R.color.red));
 				mHelpTextBig.setText(R.string.error_unknown_title);
 				mHelpTextSmall.setText(R.string.error_unknown);
 				return;
 			default:
 				break;
 			}
 
 			mHelpTextBig = null;
 			mHelpTextSmall = null;
 	    }
 	
 	    public class LinkTextView extends TextView {
 
 			public LinkTextView(Context context) {
 				super(context);
 			}
 	    	
 			@Override
 			public boolean onTouchEvent(MotionEvent event) {
 			        TextView widget = (TextView) this;
 			        Object text = widget.getText();
 			        if (text instanceof Spanned) {
 			            Spannable buffer = (Spannable) text;
 
 			            int action = event.getAction();
 
 			            if (action == MotionEvent.ACTION_UP
 			                    || action == MotionEvent.ACTION_DOWN) {
 			                int x = (int) event.getX();
 			                int y = (int) event.getY();
 
 			                x -= widget.getTotalPaddingLeft();
 			                y -= widget.getTotalPaddingTop();
 
 			                x += widget.getScrollX();
 			                y += widget.getScrollY();
 
 			                Layout layout = widget.getLayout();
 			                int line = layout.getLineForVertical(y);
 			                int off = layout.getOffsetForHorizontal(line, x);
 
 			                ClickableSpan[] link = buffer.getSpans(off, off,
 			                        ClickableSpan.class);
 
 			                if (link.length != 0) {
 			                    if (action == MotionEvent.ACTION_UP) {
 			                        link[0].onClick(widget);
 			                    } else if (action == MotionEvent.ACTION_DOWN) {
 			                         Selection.setSelection(buffer,
 			                                 buffer.getSpanStart(link[0]),
 			                                 buffer.getSpanEnd(link[0]));
 			                    }
 			                    return true;
 			                }
 			            }
 
 			        }
 
 			        return false;
 			    }
 			
 	    }
 }
