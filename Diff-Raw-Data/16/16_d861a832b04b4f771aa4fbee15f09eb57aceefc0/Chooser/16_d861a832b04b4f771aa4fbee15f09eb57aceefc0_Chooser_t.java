 package org.inodes.gus.demo;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Random;
 
 import android.app.ProgressDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.speech.tts.TextToSpeech;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Chooser extends ListFragment {
 	private final static String TAG = "CocktailFactoryChooser";
 	
 	private final static boolean ENABLE_SPEECH = true;
 
 	final private Random mRandom = new Random();
 	private ArrayAdapter<Drink> mAdapter;
 	private Messenger mDeviceService = null;
 	private TextToSpeech mTts = null;
 	private String[] mSpeechAccept;
 	private String[] mSpeechDone;
 	
 	final private ServiceConnection mConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName name, IBinder service) {
 			mDeviceService = new Messenger(service);
 		}
 
 		public void onServiceDisconnected(ComponentName name) {
 		}
 	};
 
 	final private Messenger mMessenger = new Messenger(new MessageHandler());
 	private class MessageHandler extends Handler {
 		@Override
 		public void handleMessage(Message msg) {
 			Log.d(TAG, "handleMessage: " + msg);
 			switch (msg.what) {
 			case DeviceInterface.MSG_DONE: {
 				onDrinkCompleted();
 				break;
 			}
 			case DeviceInterface.MSG_STATUS: {
 				onDrinkProgress(msg.arg1, msg.arg2);
 				break;
 			}
 			case DeviceInterface.MSG_ERROR: {
 				onDrinkError((String)msg.obj);
 				break;
 			}
 			default:
 				super.handleMessage(msg);
 				break;
 			}
 		}
 	}
 
 	private class GoButtonClickListener implements View.OnClickListener {
 		private final Drink mDrink;
 		public GoButtonClickListener(Drink drink) {
 			mDrink = drink;
 		}
 		@Override
 		public void onClick(View v) {
 			makeDrink(mDrink);
 		}
 	}
 
 	private class DrinkAdapter extends ArrayAdapter<Drink> {
 		public DrinkAdapter(Context context, Collection<Drink> collection) {
 			super(context, R.layout.drink_view, 0);
 			// addAll(Collection<>) doesn't exist until API 11
 			for (Drink d : collection)
 				add(d);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			final Drink drink = getItem(position);
 			
 			View v = convertView;
 			if (v == null)
 				v = View.inflate(getContext(), R.layout.drink_view, null);
 
 			ImageView img = (ImageView)v.findViewById(R.id.drink_image);
 			img.setImageResource(drink.getImageResource());
 
 			TextView drink_name = (TextView)v.findViewById(R.id.drink_name);
 			drink_name.setText(drink.getName());
 
 			TextView desc = (TextView)v.findViewById(R.id.drink_desc);
 			desc.setText(drink.getDescription());
 
 			Button go = (Button)v.findViewById(R.id.go_button);
 			go.setOnClickListener(new GoButtonClickListener(drink));
 
 			return v;
 		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
        }

		@Override
        public boolean isEnabled(int position) {
			// "selection" is handled via an explicit "go" button in the item view
			return false;
        }
 	}
 
 	private class RandomDrink extends Drink {
 		final private static int NUM_INGREDIENTS = 3;
 		final private static int TOTAL_WEIGHT = 60;
		final Bottle[] allBottles = Bottle.getBottles().toArray(new Bottle[0]);
 		public RandomDrink(String name, String desc, int imgResource) {
 			super(name, desc, null, imgResource);
 		}
 		@Override
 		public Collection<Ingredient> getIngredients() {
 			List<Ingredient> ingredients = new ArrayList<Ingredient>();
 			for (int i = 0; i < NUM_INGREDIENTS; i++) {
 				int bottle_num = mRandom.nextInt(allBottles.length);
 				ingredients.add(new Drink.Ingredient(allBottles[bottle_num],
 						TOTAL_WEIGHT / NUM_INGREDIENTS));
 			}
 			return ingredients;
 		}
 	}
 
 	protected void onDrinkChosen(Drink drink) {
 		ProgressDialogFragment f = ProgressDialogFragment.newSpinnerInstance(
 				"Place your glass on the scales");
 		f.show(getFragmentManager(), "dialog");
 
 
 		if (mTts != null) {
 			speakRandomPhrase(mSpeechAccept, TextToSpeech.QUEUE_FLUSH);
 			mTts.speak("Just place your glass on the scales.", TextToSpeech.QUEUE_ADD, null);
 		}
 	}
 	
 	protected void onDrinkProgress(int progress, int max) {
 		Log.d(TAG, "onDrinkProgress(" + progress + "/" + max + ")");
 		ProgressDialogFragment f = (ProgressDialogFragment)getFragmentManager().findFragmentByTag("dialog");
 		if (f != null) {
 			ProgressDialog d = (ProgressDialog)f.getDialog();
 			if (d.isIndeterminate()) {
 				// currently showing spinner - time to switch to horizontal
 				Log.d(TAG, "dialog is indeterminate - switching to horizontal");
 				FragmentTransaction ft = getFragmentManager().beginTransaction();
 				f.dismiss();
 				f = ProgressDialogFragment.newHorizInstance();
 				ft.add(f, "dialog");
 				ft.commit();
 			}
 			f.updateProgress(progress, max);		
 		}
 	}
 
 	protected void onDrinkCompleted() {
 		ProgressDialogFragment f = (ProgressDialogFragment)getFragmentManager().findFragmentByTag("dialog");
 		if (f != null)
 			f.dismiss();
 
 		if (mTts != null)
 			speakRandomPhrase(mSpeechDone, TextToSpeech.QUEUE_ADD);
 	}
 
 	protected void onDrinkError(String err) {
 		ProgressDialogFragment f = (ProgressDialogFragment)getFragmentManager().findFragmentByTag("dialog");
 		if (f != null)
 			f.dismiss();
 
 		if (mTts != null)
 			mTts.speak("Oops, sorry about that!", TextToSpeech.QUEUE_FLUSH, null);
 		
 		Toast.makeText(getActivity(), "Error: " + err, Toast.LENGTH_SHORT).show();
 	}
 
 	private void speakRandomPhrase(String[] phrases, int queueMode) {
 		if (mTts != null) {
 			final String phrase = phrases[mRandom.nextInt(phrases.length)];
 			mTts.speak(phrase, queueMode, null);
 		}
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Resources res = getResources();
 
 		mAdapter = new DrinkAdapter(getActivity(), Drink.getDrinks(getActivity()));
 		mAdapter.add(new RandomDrink("I'm Feeling Lucky",
 				"Let the bartender mix a drink just for you", R.drawable.drink_placeholder));
 		setListAdapter(mAdapter);
 		Log.d(TAG, "xml files parsed");
 
 		mSpeechAccept = res.getStringArray(R.array.speech_accept);
 		mSpeechDone = res.getStringArray(R.array.speech_done);
 		if (ENABLE_SPEECH) {
 			mTts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
 				public void onInit(int status) {
 					if (status == TextToSpeech.ERROR) {
 						Log.e(TAG, "Error initialising text-to-speech - disabling");
 						mTts = null;
 					}
 				}
 			});
 		}
 	}
 
 	@Override
 	public void onDestroy() {
 		if (mTts != null) mTts.shutdown();
 		super.onDestroy();
 	}
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 
 		ListView lv = getListView();
 		lv.setTextFilterEnabled(true);
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		Log.d(TAG, "binding to service");
 		getActivity().bindService(new Intent(getActivity(), DeviceInterface.class),
 				mConnection, Context.BIND_AUTO_CREATE);
 		Log.d(TAG, "bound to service");
 	}
 
 	@Override
 	public void onStop() {
 		getActivity().unbindService(mConnection);
 		super.onStop();
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		Drink drink = (Drink)mAdapter.getItem(position);
 		makeDrink(drink);
 	}
 
 	private void makeDrink(Drink drink) {
 		CocktailFactoryActivity activity = (CocktailFactoryActivity)getActivity();
 		try {
 			Message msg = Message.obtain(null, DeviceInterface.MSG_MAKE_DRINK,
 					drink.getName());
 			msg.replyTo = mMessenger;
 			Bundle bundle = new Bundle();
 			bundle.putParcelable(DeviceInterface.DATA_ACCESSORYFD, activity.getAccessoryFd());
 			msg.setData(bundle);
 			mDeviceService.send(msg);
 
 			onDrinkChosen(drink);
 		} catch (RemoteException e) {
 			Toast.makeText(activity, R.string.deviceservice_disconnected,
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 }
