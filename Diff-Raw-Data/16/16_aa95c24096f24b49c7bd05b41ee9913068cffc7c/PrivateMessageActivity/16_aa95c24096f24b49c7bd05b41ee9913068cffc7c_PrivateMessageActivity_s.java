 package com.podevs.android.pokemononline.pms;
 
 import java.util.Iterator;
 
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.podevs.android.pokemononline.NetworkService;
 import com.podevs.android.pokemononline.R;
 import com.podevs.android.pokemononline.pms.PrivateMessageList.PrivateMessageListListener;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class PrivateMessageActivity extends Activity {
 	protected PrivateMessageList pms;
 	protected MyAdapter adapter = new MyAdapter();
 	protected NetworkService netServ;
 	protected ViewPager vp;
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         bindService(new Intent(this, NetworkService.class), connection,
         		Context.BIND_AUTO_CREATE);
         
         setContentView(R.layout.pm_activity);
         vp = (ViewPager) findViewById(R.id.viewPager);
         vp.setAdapter(adapter);
         
         TitlePageIndicator titleIndicator = (TitlePageIndicator)findViewById(R.id.titles);
         titleIndicator.setViewPager(vp);
         
         final EditText send = (EditText) findViewById(R.id.pmInput);
         send.setOnKeyListener(new OnKeyListener() {
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 // If the event is a key-down event on the "enter" button
             	// and the socket is connected
                 if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                     (keyCode == KeyEvent.KEYCODE_ENTER) && netServ != null) {
                 	PrivateMessage pm = adapter.getItemAt(vp.getCurrentItem()); 
                 	int id = pm.other.id;
                 	
                 	if (netServ.players.get(id) == null) {
                 		Toast.makeText(PrivateMessageActivity.this, "This player is offline", Toast.LENGTH_SHORT).show();
                 		return true;
                 	}
                 	
                 	// Perform action on key press
                 	netServ.sendPM(id, send.getText().toString());
                 	pm.addMessage(pm.me, send.getText().toString());
                 	send.getText().clear();
                   return true;
                 }
                 return false;
             }
 		});
     }
     
     @Override
 	protected void onResume() {
     	super.onResume();
     	
     	/* Removes the PM notification */
     	((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel("pm", 0);
     	
     	/* Switches to the appropriate page */
     	int position = adapter.getIdPosition(getIntent().getIntExtra("playerId", PagerAdapter.POSITION_NONE));
     	
     	if (position != PagerAdapter.POSITION_NONE) {
     		vp.setCurrentItem(position, true);
     	}
 	}
     
     @Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		
 		setIntent(intent);
 	}
     
     @Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		
 		if (item.getItemId() == R.id.closePM) {
 			try {
 				int id = adapter.getItemAt(vp.getCurrentItem()).other.id;
 				pms.removePM(id);
 			} catch (NullPointerException ex) {
 				
 			}
 			
 			if (pms.count() == 0) {
 				finish();
 			}
 			return true;
 		}
 		
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		
 		MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.pmoptions, menu);
         
 		return true;
 	}
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		
 		if (adapter.getCount() <= 0) {
 			menu.removeItem(R.id.closePM);
 		}
 		return true;
 	}
 
 	private ServiceConnection connection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			netServ = ((NetworkService.LocalBinder)service).getService();
 			pms = netServ.pms;
 			pms.listener = adapter;
 			adapter.notifyDataSetChanged();
 			
 			/* Switches to the appropriate page */
 	    	int position = adapter.getIdPosition(getIntent().getIntExtra("playerId", PagerAdapter.POSITION_NONE));
 	    	
 	    	if (position != PagerAdapter.POSITION_NONE) {
 	    		vp.setCurrentItem(position, true);
 	    	}
 		}
 		
 		public void onServiceDisconnected(ComponentName className) {
 			netServ = null;
 		}
 	};
 	
 	private class MyAdapter extends PagerAdapter implements PrivateMessageListListener
 	{
 		@Override
 		public CharSequence getPageTitle(int position) {
 			return getItemAt(position).other.nick();
 		}
 
 		@Override
 		public int getCount() {
 			if (pms == null) {
 				return 0;
 			} else {
 				return pms.count();
 			}
 		}
 		
 		PrivateMessage getItemAt(int position) {
 			if (pms == null) {
 				return null;
 			}
			Iterator<PrivateMessage> it = pms.privateMessages.values().iterator();
			for (int i = 0; i < position; i++) {
				it.next();
 			}
			return it.next();
 		}
 		
 		@Override
 		public Object instantiateItem(ViewGroup container, int position) {
 			PrivateMessage pm = getItemAt(position);
 			
 			if (pm == null) {
 				return null;
 			}
 			
 			ListView lv = new ListView(PrivateMessageActivity.this);
 			container.addView(lv);
 			
 			lv.setAdapter(new PrivateMessageAdapter(PrivateMessageActivity.this, pm));
 			lv.setTag(R.id.associated_pm, pm);
 			lv.setTag(R.id.position, position);
 
 			return lv;
 		}
 		
 		@Override
 		public int getItemPosition(Object object) {
 			if (pms == null) {
 				return POSITION_UNCHANGED;
 			}
 
 			View v = (View) object;
 			PrivateMessage pm = (PrivateMessage) v.getTag(R.id.associated_pm);
 			int lastPos = (Integer) v.getTag(R.id.position);
 			
 			Iterator<PrivateMessage> it = pms.privateMessages.values().iterator();
 			for (int i = 0; i < pms.count(); i++) {
 				if (it.next() == pm) {
 					if (lastPos != i) {
 						v.setTag(R.id.position, i);
 						return i;
 					}
 					return POSITION_UNCHANGED;
 				}
 			}
 			return POSITION_NONE;
 		}
 		
 		public int getIdPosition(int id) {
 			if (pms == null) {
 				return POSITION_NONE;
 			}
 			Iterator<PrivateMessage> it = pms.privateMessages.values().iterator();
 			for (int i = 0; i < pms.count(); i++) {
 				if (it.next().other.id == id) {
 					return i;
 				}
 			}
 			return POSITION_NONE;
 		}
 
 		@Override
 		public boolean isViewFromObject(View v, Object o) {
 			return v == o;
 		}
 
 		@Override
 		public void destroyItem(ViewGroup container, int position, Object object) {
 			container.removeView((View)object);
 		}
 
 		public void onNewPM(PrivateMessage privateMessage) {
 			runOnUiThread(new Runnable() {
 				public void run() {
 					notifyDataSetChanged();
 				}
 			});
 		}
 
 		public void onRemovePM(int id) {
 			runOnUiThread(new Runnable() {
 				public void run() {
 					notifyDataSetChanged();
 				}
 			});
 		}
 	}
 }
