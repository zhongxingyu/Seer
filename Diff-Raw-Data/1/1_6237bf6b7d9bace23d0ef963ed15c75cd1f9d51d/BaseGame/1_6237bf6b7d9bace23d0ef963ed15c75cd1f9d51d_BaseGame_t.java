 package uk.co.thomasc.wordmaster;
 
 import java.util.ArrayList;
 
 import uk.co.thomasc.wordmaster.api.ServerAPI;
 import uk.co.thomasc.wordmaster.gcm.RegisterThread;
 import uk.co.thomasc.wordmaster.gcm.TurnReceiver;
 import uk.co.thomasc.wordmaster.iab.IabHelper;
 import uk.co.thomasc.wordmaster.iab.IabHelper.OnIabPurchaseFinishedListener;
 import uk.co.thomasc.wordmaster.iab.IabHelper.OnIabSetupFinishedListener;
 import uk.co.thomasc.wordmaster.iab.IabHelper.QueryInventoryFinishedListener;
 import uk.co.thomasc.wordmaster.iab.IabResult;
 import uk.co.thomasc.wordmaster.iab.Inventory;
 import uk.co.thomasc.wordmaster.iab.Purchase;
 import uk.co.thomasc.wordmaster.objects.Game;
 import uk.co.thomasc.wordmaster.objects.Turn;
 import uk.co.thomasc.wordmaster.objects.User;
 import uk.co.thomasc.wordmaster.util.BaseGameActivity;
 import uk.co.thomasc.wordmaster.view.create.PersonAdapter;
 import uk.co.thomasc.wordmaster.view.game.GameAdapter;
 import uk.co.thomasc.wordmaster.view.menu.MenuDetailFragment;
 import uk.co.thomasc.wordmaster.view.menu.MenuListFragment;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.widget.LinearLayout;
 
 /**
  * An example full-screen activity that shows and hides the system UI (i.e.
  * status bar and navigation/system bar) with user interaction.
  *
  * @see SystemUiHider
  */
 public class BaseGame extends BaseGameActivity implements OnIabPurchaseFinishedListener {
 
 	public static Typeface russo;
 
 	public MenuListFragment menuFragment;
 	public MenuDetailFragment menuDetail;
 	public GameAdapter gameAdapter;
 	public boolean wideLayout = false;
 	
 	private String userId = "";
 	public String goToGameId = "";
 	
 	public static IabHelper mHelper;
 	public static String testSKU = "android.test.purchased";
 	public static String upgradeSKU = "wordmaster_upgrade";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.empty_screen);
 		
 		int screenLayoutSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
 		wideLayout = screenLayoutSize > 2;
 		if (!wideLayout) {
 			((LinearLayout) findViewById(R.id.empty)).setWeightSum(1F);
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		}
 
 		BaseGame.russo = Typeface.createFromAsset(getAssets(), "fonts/Russo_One.ttf");
 
 		if (savedInstanceState != null) {
 			Game.restoreState(savedInstanceState, this);
 		} else {
 			getSupportFragmentManager().beginTransaction().add(R.id.empty, new MenuListFragment()).addToBackStack("top").commit();
 		}
 		
 		checkIntent(getIntent());
 		
 		String base64PublicKey = Game.keySegment + Turn.keySegment + User.keySegment + PersonAdapter.keySegment;
 		mHelper = new IabHelper(this, base64PublicKey);
 		mHelper.startSetup(new OnIabSetupFinishedListener() {
 			@Override
 			public void onIabSetupFinished(IabResult result) {
 				if (! result.isSuccess()) {
 					// TODO: IAB is broken, do something
 				} else {
 					// for testing purposes, tell google play we've consumed the upgrade SKUs
 					consumeUpgrades();
 					// TODO: Get rid of this before release!
 				}
 			}
 		});
 	}
 	
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         // Pass on the activity result to the helper for handling
         if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
             super.onActivityResult(requestCode, resultCode, data);
         } else {
             
         }
     }
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		checkIntent(intent);
 	}
 	
 	private void checkIntent(Intent intent) {
 		TurnReceiver.resetNotifications(this);
 		
 		Bundle extras = intent.getExtras();
 		if (extras != null && extras.containsKey("gameid")) {
 			String gameid = extras.getString("gameid");
 			if (Game.getGame(gameid) != null) {
 				menuFragment.goToGame(gameid);
 			} else {
 				goToGameId = gameid;
 			}
 		}
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		if (mHelper != null) {
 			mHelper.dispose();
 		}
 		mHelper = null;
 	}
 	
 	public void buyUpgrade() {
 		mHelper.launchPurchaseFlow(this, upgradeSKU, 1902, this, userId);
 	}
 	
 	public void consumeUpgrades() {
 		mHelper.queryInventoryAsync(new QueryInventoryFinishedListener() {
 			@Override
 			public void onQueryInventoryFinished(IabResult result, Inventory inv) {
 				System.out.println("Got the inventory");
 				ArrayList<Purchase> purchases = new ArrayList<Purchase>();
 				if (inv.hasPurchase(testSKU)) {
 					purchases.add(inv.getPurchase(testSKU));
 				}
 				if (inv.hasPurchase(upgradeSKU)) {
 					purchases.add(inv.getPurchase(upgradeSKU));
 				}
 				mHelper.consumeAsync(purchases, null);
 			}
 		});
 	}
 	
 	@Override
 	public void onIabPurchaseFinished(IabResult result, Purchase info) {
 		if (result.isFailure()) {
 			if (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED) {
 				System.out.println("User cancelled purchase");
 			} else {
 				System.out.println("IAB error " + result.getResponse());
 				// TODO: Purchase failed, show an error?
 			}
 		} else {
 			System.out.println("Upgrade purchased! Token: " + info.getToken());
 			getSupportFragmentManager().popBackStack("upgrade", 1);
 			ServerAPI.upgradePurchased(info.getToken());
 		}
 	}
 	
 	public void queryInventory(QueryInventoryFinishedListener listener) {
 		ArrayList<String> additionalSkuList = new ArrayList<String>();
 		additionalSkuList.add(upgradeSKU);
 		mHelper.queryInventoryAsync(true, additionalSkuList, listener);
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		Game.saveState(outState);
 		Game.saveState(this);
 	}
 
 	@Override
 	public void onSignInFailed() {
 		getSupportFragmentManager().popBackStack("top", 0); // Close any open games
 		menuFragment.onSignInFailed();
 		System.out.println("oh noes!");
 	}
 
 	@Override
 	public void onSignInSucceeded() {
 		userId = getGamesClient().getCurrentPlayer().getPlayerId();
 		User.onPlusConnected(this);
 		User.getUser(getPlusClient().getCurrentPerson(), this); // Load local user into cache
 		menuFragment.onSignInSucceeded();
 		if (gameAdapter != null) {
 			menuDetail.loadTurns();
 			gameAdapter.notifyDataSetChanged();
 		}
 		new RegisterThread(this).start();
 	}
 
 	@Override
 	public void onBackPressed() {
 		String topId = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
 		if (menuDetail != null && topId.equals("game")) {
 			menuDetail.hideKeyboard();
 			menuDetail = null;
 			menuFragment.loadGames();
 		}
 		if (topId.equals("top")) {
 			finish();
 		} else {
 			super.onBackPressed();
 		}
 	}
 	
 	public String getUserId() {
 		return userId;
 	}
 
 }
