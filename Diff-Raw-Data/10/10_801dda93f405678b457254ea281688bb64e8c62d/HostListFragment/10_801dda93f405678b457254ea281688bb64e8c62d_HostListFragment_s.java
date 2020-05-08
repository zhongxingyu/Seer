 package org.connectbot;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Fragment;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.ColorStateList;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.*;
 import android.widget.*;
 import org.connectbot.bean.HostBean;
 import org.connectbot.service.TerminalBridge;
 import org.connectbot.service.TerminalManager;
 import org.connectbot.transport.TransportFactory;
 import org.connectbot.util.HostDatabase;
 import org.connectbot.util.PreferenceConstants;
 
 import java.util.List;
 
 public class HostListFragment extends Fragment {
     public final static int REQUEST_EDIT = 1;
 	public final static int REQUEST_EULA = 2;
 
     protected HostDatabase hostdb;
 	private List<HostBean> hosts;
 
     protected boolean sortedByColor = false;
 	private MenuItem sortcolor;
 	private MenuItem sortlast;
 
     protected boolean makingShortcut = false;
 
 	private SharedPreferences prefs = null;
 
     private boolean mDualPane;
     
     private ListView lv;
     private Spinner transportSpinner;
 	private TextView quickconnect;
 
     private int mCurCheckPosition = 0;
     private int mShownCheckPosition = -1;
 
 	protected LayoutInflater inflater = null;
     private HostListFragmentContainer mListener;
 
     public interface HostListFragmentContainer {
         public TerminalManager getTerminalManager();
 
         public boolean startConsoleActivity(Uri uri);
     }
 
     /**
      * Create a new instance of HostListFragment
      */
     static HostListFragment newInstance() {
         HostListFragment f = new HostListFragment();
 
         // Supply num input as an argument.
         /*Bundle args = new Bundle();
         args.putInt("num", num);
         f.setArguments(args);*/
 
         return f;
     }
 
     protected Handler updateHandler = new Handler() {
 		@Override
 		public void handleMessage(Message msg) {
 			updateList();
 		}
 	};
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         try {
             mListener = (HostListFragmentContainer) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString() + " must implement HostListFragmentContainer");
         }
     }
 
     /**
      * When creating, retrieve this instance's number from its arguments.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // check for eula agreement
 		this.prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
 
 		boolean agreed = prefs.getBoolean(PreferenceConstants.EULA, false);
 		if(!agreed) {
 			this.startActivityForResult(new Intent(getActivity(), WizardActivity.class), REQUEST_EULA);
 		}
 
         this.makingShortcut = Intent.ACTION_CREATE_SHORTCUT.equals(getActivity().getIntent().getAction())
 								|| Intent.ACTION_PICK.equals(getActivity().getIntent().getAction());
 
         // connect with hosts database and populate list
 		this.hostdb = new HostDatabase(getActivity());
 
         this.sortedByColor = prefs.getBoolean(PreferenceConstants.SORT_BY_COLOR, false);
 
         /*View v = getFragmentManager().findFragmentById(R.id.consoleFrame);
         if (v == null) mDualPane = false;
         else mDualPane = true;*/
         mDualPane = false;
 
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.frg_hostlist, container, false);
 
         lv = (ListView) v.findViewById(R.id.list);
         //this.list.setSelector(R.drawable.highlight_disabled_pressed);
 
 		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public synchronized void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
 				// launch off to console details
 				HostBean host = (HostBean) parent.getAdapter().getItem(position);
 				Uri uri = host.getUri();
 
 				Intent contents = new Intent(Intent.ACTION_VIEW, uri);
 				contents.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
 				if (makingShortcut) {
 					// create shortcut if requested
 					Intent.ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(getActivity(), R.drawable.icon);
 
 					Intent intent = new Intent();
 					intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, contents);
 					intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, host.getNickname());
 					intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
 
 					getActivity().setResult(Activity.RESULT_OK, intent);
 					getActivity().finish();
 
 				} else {
                     mCurCheckPosition = position;
 
                     if (mDualPane && mShownCheckPosition == mCurCheckPosition) {
                         return;
                     } else {
                         if (mDualPane) {
                             // We can display everything in-place with fragments, so update
                             // the list to highlight the selected item and show the data.
                             lv.setItemChecked(position, true);
                             Log.d("ConnectBotTablet", "Item checked at "+lv.getCheckedItemPosition());
                             if (mShownCheckPosition != mCurCheckPosition) mShownCheckPosition = position;
                         }
 
                        startConsoleActivity();
                     }
 				}
 			}
 		});
 
 		this.registerForContextMenu(lv);
 
         quickconnect = (TextView) v.findViewById(R.id.front_quickconnect);
 		quickconnect.setVisibility(makingShortcut ? View.GONE : View.VISIBLE);
 		quickconnect.setOnKeyListener(new View.OnKeyListener() {
 
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 
 				if(event.getAction() == KeyEvent.ACTION_UP) return false;
 				if(keyCode != KeyEvent.KEYCODE_ENTER) return false;
 
 				return startConsoleActivity();
 			}
 		});
 
 		transportSpinner = (Spinner) v.findViewById(R.id.transport_selection);
 		transportSpinner.setVisibility(makingShortcut ? View.GONE : View.VISIBLE);
 		ArrayAdapter<String> transportSelection = new ArrayAdapter<String>(getActivity(),
 				android.R.layout.simple_spinner_item, TransportFactory.getTransportNames());
 		transportSelection.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		transportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
 			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
 				String formatHint = TransportFactory.getFormatHint(
 						(String) transportSpinner.getSelectedItem(),
 						getActivity());
 
 				quickconnect.setHint(formatHint);
 				quickconnect.setError(null);
 				quickconnect.requestFocus();
 			}
 			public void onNothingSelected(AdapterView<?> arg0) { }
 		});
 		transportSpinner.setAdapter(transportSelection);
 
         this.inflater = inflater;
 
         return v;
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putInt("curChoice", mCurCheckPosition);
         outState.putInt("shownChoice", mShownCheckPosition);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         if (savedInstanceState != null) {
             // Restore last state for checked position.
             mCurCheckPosition = savedInstanceState.getInt("curChoice", 0);
             mShownCheckPosition = savedInstanceState.getInt("shownChoice", -1);
         }
 
         //this.inflater = LayoutInflater.from(getActivity());
     }
 
     @Override
     public void onStart() {
         super.onStart();
 
         if(this.hostdb == null)
 			this.hostdb = new HostDatabase(getActivity());
     }
 
     @Override
     public void onStop() {
         super.onStop();
 
         if(this.hostdb != null) {
 			this.hostdb.close();
 			this.hostdb = null;
 		}
     }
 
     @Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == REQUEST_EULA) {
 			if(resultCode == Activity.RESULT_OK) {
 				// yay they agreed, so store that info
 				SharedPreferences.Editor edit = prefs.edit();
 				edit.putBoolean(PreferenceConstants.EULA, true);
 				edit.commit();
 			} else {
 				// user didnt agree, so close
 				getActivity().finish();
 			}
 		} else if (requestCode == REQUEST_EDIT) {
 			this.updateList();
 		}
 	}
 
     @Override
 	public void onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 
 		// don't offer menus when creating shortcut
 		if (makingShortcut) return;
 
 		sortcolor.setVisible(!sortedByColor);
 		sortlast.setVisible(sortedByColor);
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 
 		// don't offer menus when creating shortcut
 		if(makingShortcut) return;
 
 		// add host, ssh keys, about
 		sortcolor = menu.add(R.string.list_menu_sortcolor);
 		sortcolor.setIcon(android.R.drawable.ic_menu_share);
 		sortcolor.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 			public boolean onMenuItemClick(MenuItem item) {
 				sortedByColor = true;
 				updateList();
 				return true;
 			}
 		});
 
 		sortlast = menu.add(R.string.list_menu_sortname);
 		sortlast.setIcon(android.R.drawable.ic_menu_share);
 		sortlast.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 			public boolean onMenuItemClick(MenuItem item) {
 				sortedByColor = false;
 				updateList();
 				return true;
 			}
 		});
 
 		MenuItem keys = menu.add(R.string.list_menu_pubkeys);
 		keys.setIcon(android.R.drawable.ic_lock_lock);
 		keys.setIntent(new Intent(getActivity(), PubkeyListActivity.class));
 
 		MenuItem colors = menu.add("Colors");
 		colors.setIcon(android.R.drawable.ic_menu_slideshow);
 		colors.setIntent(new Intent(getActivity(), ColorsActivity.class));
 
 		MenuItem settings = menu.add(R.string.list_menu_settings);
 		settings.setIcon(android.R.drawable.ic_menu_preferences);
 		settings.setIntent(new Intent(getActivity(), SettingsActivity.class));
 
 		MenuItem help = menu.add(R.string.title_help);
 		help.setIcon(android.R.drawable.ic_menu_help);
 		help.setIntent(new Intent(getActivity(), HelpActivity.class));
 
 		return;
 
 	}
 
     @Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
 
 		// create menu to handle hosts
 
 		// create menu to handle deleting and sharing lists
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		final HostBean host = (HostBean) lv.getItemAtPosition(info.position);
 
 		menu.setHeaderTitle(host.getNickname());
 
 		// edit, disconnect, delete
 		MenuItem connect = menu.add(R.string.list_host_disconnect);
 		final TerminalBridge bridge = mListener.getTerminalManager().getConnectedBridge(host);
 		connect.setEnabled((bridge != null));
 		connect.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 			public boolean onMenuItemClick(MenuItem item) {
 				bridge.dispatchDisconnect(true);
 				updateHandler.sendEmptyMessage(-1);
 				return true;
 			}
 		});
 
 		MenuItem edit = menu.add(R.string.list_host_edit);
 		edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 			public boolean onMenuItemClick(MenuItem item) {
 				Intent intent = new Intent(getActivity(), HostEditorActivity.class);
 				intent.putExtra(Intent.EXTRA_TITLE, host.getId());
 				getActivity().startActivityForResult(intent, REQUEST_EDIT);
 				return true;
 			}
 		});
 
 		MenuItem portForwards = menu.add(R.string.list_host_portforwards);
 		portForwards.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 			public boolean onMenuItemClick(MenuItem item) {
 				Intent intent = new Intent(getActivity(), PortForwardListActivity.class);
 				intent.putExtra(Intent.EXTRA_TITLE, host.getId());
 				getActivity().startActivityForResult(intent, REQUEST_EDIT);
 				return true;
 			}
 		});
 		if (!TransportFactory.canForwardPorts(host.getProtocol()))
 			portForwards.setEnabled(false);
 
 		MenuItem delete = menu.add(R.string.list_host_delete);
 		delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
 			public boolean onMenuItemClick(MenuItem item) {
 				// prompt user to make sure they really want this
 				new AlertDialog.Builder(getActivity())
 					.setMessage(getString(R.string.delete_message, host.getNickname()))
 					.setPositiveButton(R.string.delete_pos, new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 						// make sure we disconnect
 							if(bridge != null)
 								bridge.dispatchDisconnect(true);
 
 							hostdb.deleteHost(host);
 							updateHandler.sendEmptyMessage(-1);
 						}
 						})
 					.setNegativeButton(R.string.delete_neg, null).create().show();
 
 				return true;
 			}
 		});
 	}
 
     public boolean startConsoleActivity() {
         Uri uri = TransportFactory.getUri((String) transportSpinner
                 .getSelectedItem(), quickconnect.getText().toString());
 
 		if (uri == null) {
 			quickconnect.setError(getString(R.string.list_format_error,
 					TransportFactory.getFormatHint(
 							(String) transportSpinner.getSelectedItem(),
 							getActivity())));
 			return false;
 		}
 
        return mListener.startConsoleActivity(uri);
     }
 
 	protected void updateList() {
 		if (prefs.getBoolean(PreferenceConstants.SORT_BY_COLOR, false) != sortedByColor) {
 			SharedPreferences.Editor edit = prefs.edit();
 			edit.putBoolean(PreferenceConstants.SORT_BY_COLOR, sortedByColor);
 			edit.commit();
 		}
 
 		if (hostdb == null)
 			hostdb = new HostDatabase(getActivity());
 
 		hosts = hostdb.getHosts(sortedByColor);
 
 		// Don't lose hosts that are connected via shortcuts but not in the database.
         TerminalManager bound = mListener.getTerminalManager();
 		if (bound != null) {
 			for (TerminalBridge bridge : bound.bridges) {
 				if (!hosts.contains(bridge.host))
 					hosts.add(0, bridge.host);
 			}
 		}
 
 		HostAdapter adapter = new HostAdapter(getActivity(), hosts, bound);
 
 		this.lv.setAdapter(adapter);
 	}
 
     class HostAdapter extends ArrayAdapter<HostBean> {
 		private List<HostBean> hosts;
 		private final TerminalManager manager;
 		private final ColorStateList  red, green, blue;
 
 		public final static int STATE_UNKNOWN = 1, STATE_CONNECTED = 2, STATE_DISCONNECTED = 3;
 
 		class ViewHolder {
 			public TextView  nickname;
 			public TextView caption;
 			public ImageView icon;
 		}
 
 		public HostAdapter(Context context, List<HostBean> hosts, TerminalManager manager) {
 			super(context, R.layout.item_host, hosts);
 
 			this.hosts = hosts;
 			this.manager = manager;
 
 			red = context.getResources().getColorStateList(R.color.red);
 			green = context.getResources().getColorStateList(R.color.green);
 			blue = context.getResources().getColorStateList(R.color.blue);
 		}
 
 		/**
 		 * Check if we're connected to a terminal with the given host.
 		 */
 		private int getConnectedState(HostBean host) {
 			// always disconnected if we dont have backend service
 			if (this.manager == null)
 				return STATE_UNKNOWN;
 
 			if (manager.getConnectedBridge(host) != null)
 				return STATE_CONNECTED;
 
 			if (manager.disconnected.contains(host))
 				return STATE_DISCONNECTED;
 
 			return STATE_UNKNOWN;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder holder;
 
 			if (convertView == null) {
 				convertView = inflater.inflate(R.layout.item_host, null, false);
 
 				holder = new ViewHolder();
 
 				holder.nickname = (TextView)convertView.findViewById(android.R.id.text1);
 				holder.caption = (TextView)convertView.findViewById(android.R.id.text2);
 				holder.icon = (ImageView)convertView.findViewById(android.R.id.icon);
 
 				convertView.setTag(holder);
 			} else
 				holder = (ViewHolder) convertView.getTag();
 
 			HostBean host = hosts.get(position);
 			if (host == null) {
 				// Well, something bad happened. We can't continue.
 				Log.e("HostAdapter", "Host bean is null!");
 
 				holder.nickname.setText("Error during lookup");
 				holder.caption.setText("see 'adb logcat' for more");
 				return convertView;
 			}
 
 			holder.nickname.setText(host.getNickname());
 
 			switch (this.getConnectedState(host)) {
 			case STATE_UNKNOWN:
 				holder.icon.setImageState(new int[] { }, true);
 				break;
 			case STATE_CONNECTED:
 				holder.icon.setImageState(new int[] { android.R.attr.state_checked }, true);
 				break;
 			case STATE_DISCONNECTED:
 				holder.icon.setImageState(new int[] { android.R.attr.state_expanded }, true);
 				break;
 			}
 
 			ColorStateList chosen = null;
 			if (HostDatabase.COLOR_RED.equals(host.getColor()))
 				chosen = this.red;
 			else if (HostDatabase.COLOR_GREEN.equals(host.getColor()))
 				chosen = this.green;
 			else if (HostDatabase.COLOR_BLUE.equals(host.getColor()))
 				chosen = this.blue;
 
 			Context context = convertView.getContext();
 
 			if (chosen != null) {
 				// set color normally if not selected
 				holder.nickname.setTextColor(chosen);
 				holder.caption.setTextColor(chosen);
 			} else {
 				// selected, so revert back to default black text
 				holder.nickname.setTextAppearance(context, android.R.attr.textAppearanceLarge);
 				holder.caption.setTextAppearance(context, android.R.attr.textAppearanceSmall);
 			}
 
 			long now = System.currentTimeMillis() / 1000;
 
 			String nice = context.getString(R.string.bind_never);
 			if (host.getLastConnect() > 0) {
 				int minutes = (int)((now - host.getLastConnect()) / 60);
 				if (minutes >= 60) {
 					int hours = (minutes / 60);
 					if (hours >= 24) {
 						int days = (hours / 24);
 						nice = context.getString(R.string.bind_days, days);
 					} else
 						nice = context.getString(R.string.bind_hours, hours);
 				} else
 					nice = context.getString(R.string.bind_minutes, minutes);
 			}
 
 			holder.caption.setText(nice);
 
 			return convertView;
 		}
 	}
 }
