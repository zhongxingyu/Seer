 package za.ac.sun.cs.hons.minke.gui;
 
 import za.ac.sun.cs.hons.minke.MinkeApplication;
 import za.ac.sun.cs.hons.minke.R;
 import za.ac.sun.cs.hons.minke.assets.IntentIntegrator;
 import za.ac.sun.cs.hons.minke.assets.IntentResult;
 import za.ac.sun.cs.hons.minke.entities.product.BranchProduct;
 import za.ac.sun.cs.hons.minke.entities.product.Product;
 import za.ac.sun.cs.hons.minke.entities.store.Branch;
 import za.ac.sun.cs.hons.minke.gui.utils.DialogUtils;
 import za.ac.sun.cs.hons.minke.gui.utils.TabInfo;
 import za.ac.sun.cs.hons.minke.gui.utils.TabsAdapter;
 import za.ac.sun.cs.hons.minke.gui.utils.TextErrorWatcher;
 import za.ac.sun.cs.hons.minke.tasks.LoadDataFGTask;
 import za.ac.sun.cs.hons.minke.tasks.ProgressTask;
 import za.ac.sun.cs.hons.minke.tasks.UpdateDataFGTask;
 import za.ac.sun.cs.hons.minke.utils.BrowseUtils;
 import za.ac.sun.cs.hons.minke.utils.EntityUtils;
 import za.ac.sun.cs.hons.minke.utils.IntentUtils;
 import za.ac.sun.cs.hons.minke.utils.MapUtils;
 import za.ac.sun.cs.hons.minke.utils.PreferencesUtils;
 import za.ac.sun.cs.hons.minke.utils.RPCUtils;
 import za.ac.sun.cs.hons.minke.utils.ScanUtils;
 import za.ac.sun.cs.hons.minke.utils.SearchUtils;
 import za.ac.sun.cs.hons.minke.utils.ShopUtils;
 import za.ac.sun.cs.hons.minke.utils.constants.Constants;
 import za.ac.sun.cs.hons.minke.utils.constants.Debug;
 import za.ac.sun.cs.hons.minke.utils.constants.ERROR;
 import za.ac.sun.cs.hons.minke.utils.constants.NAMES;
 import za.ac.sun.cs.hons.minke.utils.constants.TAGS;
 import za.ac.sun.cs.hons.minke.utils.constants.VIEW;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.StrictMode;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 public class HomeActivity extends SherlockFragmentActivity {
 	TabHost mTabHost;
 	TabsAdapter mTabManager;
 	TabInfo browse, shop, scan;
 	protected boolean downloading;
 	protected int selectedBranch;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if (Debug.ON) {
 			Log.v(TAGS.STATE, "started");
 			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
 					.detectDiskReads().detectDiskWrites().detectNetwork()
 					.penaltyLog().build());
 			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
 					.detectAll().build());
 		}
 		setContentView(R.layout.activity_home);
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
 		mTabHost.setup();
 
 		mTabManager = new TabsAdapter(this, mTabHost, R.id.tab_real);
 
 		mTabManager.addTab(
 				mTabHost.newTabSpec(VIEW.SCAN).setIndicator(
 						getString(R.string.scan)), NAMES.SCAN);
 		mTabManager.addTab(
 				mTabHost.newTabSpec(VIEW.BROWSE).setIndicator(
 						getString(R.string.browse)), NAMES.LOCATION);
 		mTabManager.addTab(
 				mTabHost.newTabSpec(VIEW.SHOP).setIndicator(
 						getString(R.string.shop)), NAMES.SHOP);
 		if (savedInstanceState != null) {
 			mTabHost.setCurrentTabByTag(savedInstanceState.getString(VIEW.SCAN));
 		}
 		new WaitTask().execute();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.menu_home, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case R.id.menu_item_info:
 			Builder dlg = DialogUtils.getInfoDialog(this);
 			dlg.show();
 			return true;
 		case R.id.menu_item_settings:
 			startActivity(IntentUtils.getSettingsIntent(this));
 			return true;
 		case R.id.menu_item_refresh:
 			update();
 			return true;
 		case android.R.id.home:
 			mTabManager.showParentFragment();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void startActivity(Intent intent) {
 		super.startActivity(intent);
 		if (PreferencesUtils.getAnimationLevel() != Constants.NONE) {
 			overridePendingTransition(R.anim.fadein, R.anim.fadeout);
 		}
 	}
 
 	public void changeTab(String tag, String className) {
 		mTabManager.changeTab(tag, className);
 	}
 
 	public void getProductSearch(View view) {
 		changeTab(VIEW.BROWSE, NAMES.PRODUCT);
 	}
 
 	public void getProducts(View view) {
 		final SearchTask task = new SearchTask();
 		task.execute();
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
 					task.cancel(true);
 					Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 							ERROR.TIME_OUT);
 					dlg.setPositiveButton(getString(R.string.retry),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									getProducts(null);
 									dialog.cancel();
 								}
 							});
 					dlg.show();
 				}
 			}
 		}, 5000);
 	}
 
 	public void showHistories(View view) {
 		startActivity(IntentUtils.getGraphIntent(this));
 	}
 
 	public void setBranch(View view) {
 		if (MapUtils.getUserBranch() != null) {
 			changeTab(VIEW.SCAN, NAMES.SCAN);
 			scan(null);
 		} else {
 			DialogUtils.getErrorDialog(this, ERROR.INPUT).show();
 		}
 	}
 
 	public void findStores(View view) {
 		final FindBranchesTask task = new FindBranchesTask();
 		task.execute();
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
 					task.cancel(true);
 					Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 							ERROR.TIME_OUT);
 					dlg.setPositiveButton(getString(R.string.retry),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									findStores(null);
 									dialog.cancel();
 								}
 							});
 					dlg.show();
 				}
 			}
 		}, 5000);
 	}
 
 	public void scan(View view) {
		if (view != null || MapUtils.getUserBranch() == null) {
 			confirmLocation();
 		} else {
 			if (Debug.EMULATOR) {
 				Intent intent = IntentUtils.getEmulatorIntent();
 				onActivityResult(IntentIntegrator.REQUEST_CODE,
 						Activity.RESULT_OK, intent);
 			} else {
 				final AlertDialog dialog;
 				downloading = false;
 				if ((dialog = IntentIntegrator.initiateScan(this)) != null) {
 					downloading = true;
 					dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
 							.setOnClickListener(new View.OnClickListener() {
 
 								@Override
 								public void onClick(View arg0) {
 									downloading = false;
 									onActivityResult(Activity.RESULT_CANCELED,
 											0, null);
 									dialog.cancel();
 								}
 
 							});
 
 				}
 			}
 		}
 	}
 
 	public void showDirections(View view) {
 		String[] names = new String[ShopUtils.getBranches().size()];
 		MapUtils.setBranches(ShopUtils.getBranches());
 		MapUtils.setDestination(0);
 		int i = 0;
 		for (Branch b : ShopUtils.getBranches()) {
 			names[i++] = b.toString();
 		}
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(getString(R.string.str_directions));
 		builder.setSingleChoiceItems(names, 0, new OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface arg0, int position) {
 				MapUtils.setDestination(position);
 			}
 		});
 		builder.setPositiveButton(getString(R.string.view_map),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						startActivity(IntentUtils
 								.getMapIntent(HomeActivity.this));
 					}
 				});
 		builder.setNegativeButton(getString(R.string.cancel),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 		builder.create().show();
 	}
 
 	private void loadData() {
 		final LoadDataFGTask local = new LoadDataFGTask(this);
 		local.execute();
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (local.getStatus().equals(AsyncTask.Status.RUNNING)) {
 					local.cancel(true);
 					Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 							ERROR.TIME_OUT);
 					dlg.setPositiveButton(getString(R.string.retry),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									loadData();
 									dialog.cancel();
 								}
 							});
 					dlg.show();
 				}
 			}
 		}, 5000);
 	}
 
 	private void findProduct() {
 		final FindProductTask task = new FindProductTask();
 		task.execute();
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
 					task.cancel(true);
 					Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 							ERROR.TIME_OUT);
 					dlg.setPositiveButton(getString(R.string.retry),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									findProduct();
 									dialog.cancel();
 								}
 							});
 					dlg.show();
 				}
 			}
 		}, 5000);
 	}
 
 	private void updateProduct(final BranchProduct found, final int price) {
 		final UpdateProductTask task = new UpdateProductTask(found, price);
 		task.execute();
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (task.getStatus().equals(AsyncTask.Status.RUNNING)) {
 					task.cancel(true);
 					Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 							ERROR.TIME_OUT);
 					dlg.setPositiveButton(getString(R.string.retry),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									updateProduct(found, price);
 									dialog.cancel();
 								}
 							});
 					dlg.show();
 				}
 			}
 		}, 10000);
 	}
 
 	private void update() {
 		final Updater updater = new Updater();
 		updater.execute();
 		Handler handler = new Handler();
 		handler.postDelayed(new Runnable() {
 			@Override
 			public void run() {
 				if (updater.getStatus().equals(AsyncTask.Status.RUNNING)) {
 					updater.cancel(true);
 					Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 							ERROR.TIME_OUT);
 					dlg.setPositiveButton(getString(R.string.retry),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									update();
 									dialog.cancel();
 								}
 							});
 					dlg.show();
 				}
 			}
 		}, 30000);
 	}
 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == IntentIntegrator.REQUEST_CODE
 				&& resultCode != Activity.RESULT_CANCELED) {
 			IntentResult scanResult = IntentIntegrator.parseActivityResult(
 					requestCode, resultCode, data);
 			if (scanResult != null) {
 				try {
 					ScanUtils.setBarCode(Long.parseLong(scanResult
 							.getContents()));
 					findProduct();
 					return;
 				} catch (NumberFormatException nfe) {
 					nfe.printStackTrace();
 				}
 			}
 		} else if (!downloading && resultCode == Activity.RESULT_CANCELED) {
 			AlertDialog.Builder failed = DialogUtils.getErrorDialog(this,
 					ERROR.SCAN);
 			failed.setPositiveButton(getString(R.string.retry),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							scan(null);
 							dialog.cancel();
 						}
 					});
 			failed.show();
 		}
 	}
 
 	public void confirmLocation() {
 		if (MapUtils.getBranches() == null) {
 			return;
 		}
 		final int size = Math.min(MapUtils.getBranches().size(), 5);
 		String[] names = new String[size + 1];
 		int i = 0;
 		for (Branch b : MapUtils.getBranches()) {
 			if (i == size) {
 				break;
 			}
 			names[i++] = b.toString();
 		}
 		names[size] = getString(R.string.other);
 		AlertDialog.Builder location = new AlertDialog.Builder(this);
 		location.setTitle(getString(R.string.confirm_location));
 		location.setSingleChoiceItems(names, 0, new OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface arg0, int position) {
 				selectedBranch = position;
 			}
 		});
 		location.setPositiveButton(getString(R.string.get_product),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 						if (selectedBranch == size) {
 							MapUtils.setUserBranch(null);
 							editLocation();
 						} else {
 							MapUtils.setUserBranch(MapUtils.getBranches().get(
 									selectedBranch));
 							scan(null);
 						}
 					}
 				});
 		location.setNegativeButton(getString(R.string.cancel),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 		location.show();
 
 	}
 
 	protected void editLocation() {
 		if (Debug.ON) {
 			Log.v("LOCATION", MapUtils.getLocation().toString());
 		}
 		changeTab(VIEW.SCAN, NAMES.BRANCH);
 	}
 
 	private void updatePrice(final BranchProduct found, final Product product) {
 
 		LayoutInflater factory = LayoutInflater.from(this);
 		final View updateView = factory.inflate(R.layout.dialog_update, null);
 		final TextView productText = (TextView) updateView
 				.findViewById(R.id.lbl_product);
 		final EditText updatePriceText = (EditText) updateView
 				.findViewById(R.id.text_price);
 		updatePriceText.addTextChangedListener(new TextErrorWatcher(this,
 				updatePriceText, true));
 		productText.setText(product.toString());
 		AlertDialog.Builder success = new AlertDialog.Builder(this);
 		success.setTitle(getString(R.string.product_found));
 		success.setView(updateView);
 		success.setPositiveButton(getString(R.string.update),
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						if (updatePriceText.getError() == null) {
 							int price = (int) (Double
 									.parseDouble(updatePriceText.getText()
 											.toString()) * 100);
 							updateProduct(found, price);
 							dialog.cancel();
 						}
 
 					}
 				});
 		success.show();
 
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (Debug.ON) {
 			Log.v(TAGS.KEY_PRESS, "BACK PRESSED");
 		}
 		if (mTabManager == null) {
 			super.onBackPressed();
 		} else {
 			mTabManager.setBackPress(true);
 			if (!mTabManager.isStackEmpty()) {
 				mTabManager.goBack();
 			} else {
 				super.onBackPressed();
 			}
 		}
 
 	}
 
 	class SearchTask extends ProgressTask {
 
 		public SearchTask() {
 			super(HomeActivity.this, getString(R.string.searching) + "...",
 					getString(R.string.searching_product_msg));
 		}
 
 		@Override
 		protected void success() {
 			BrowseUtils.setBranchProducts(SearchUtils.getSearched());
 			BrowseUtils.setStoreBrowse(false);
 			changeTab(VIEW.BROWSE, NAMES.BROWSE);
 
 		}
 
 		@Override
 		protected void failure(ERROR error_code) {
 			Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 					error_code);
 			dlg.setPositiveButton(getString(R.string.retry),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							getProducts(null);
 							dialog.cancel();
 						}
 					});
 			dlg.show();
 
 		}
 
 		@Override
 		protected ERROR retrieve() {
 			return EntityUtils.retrieveBranchProducts(SearchUtils
 					.isProductsActive());
 		}
 	}
 
 	class FindBranchesTask extends ProgressTask {
 
 		public FindBranchesTask() {
 			super(HomeActivity.this, getString(R.string.searching) + "...",
 					getString(R.string.searching_branch_msg));
 		}
 
 		@Override
 		protected void success() {
 			changeTab(VIEW.SHOP, NAMES.STORE);
 
 		}
 
 		@Override
 		protected void failure(ERROR error_code) {
 			Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 					error_code);
 			dlg.setPositiveButton(getString(R.string.retry),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							findStores(null);
 							dialog.cancel();
 						}
 					});
 			dlg.show();
 
 		}
 
 		@Override
 		protected ERROR retrieve() {
 			return EntityUtils.retrieveBranches(ShopUtils
 					.getAddedProducts(false));
 		}
 	}
 
 	class Updater extends UpdateDataFGTask {
 		public Updater() {
 			super(HomeActivity.this);
 		}
 
 		@Override
 		protected void success() {
 			super.success();
 			if (PreferencesUtils.isFirstTime()) {
 				PreferencesUtils.changeFirstTime(context, false);
 			}
 			loadData();
 		}
 
 		@Override
 		protected void failure(ERROR error_code) {
 			Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 					error_code);
 			dlg.setPositiveButton(getString(R.string.retry),
 					new OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dlg, int arg1) {
 							update();
 							dlg.cancel();
 						}
 
 					});
 			dlg.show();
 
 		}
 	}
 
 	class WaitTask extends ProgressTask {
 		public WaitTask() {
 			super(HomeActivity.this, getString(R.string.initialising) + "...",
 					getString(R.string.initialising_msg));
 		}
 
 		@Override
 		protected ERROR retrieve() {
 			while (!((MinkeApplication) getApplication()).loaded()) {
 				try {
 					Thread.sleep(100, 10);
 				} catch (InterruptedException e) {
 					return ERROR.PARSE;
 				}
 			}
 			return ERROR.SUCCESS;
 		}
 
 		@Override
 		protected void success() {
 			if (PreferencesUtils.isFirstTime()
 					|| PreferencesUtils.getUpdateFrequency() == Constants.STARTUP) {
 				update();
 			} else {
 				loadData();
 			}
 		}
 
 		@Override
 		protected void failure(ERROR error_code) {
 			DialogUtils.getErrorDialog(HomeActivity.this, error_code).show();
 		}
 
 	}
 
 	class FindProductTask extends ProgressTask {
 
 		public FindProductTask() {
 			super(HomeActivity.this, getString(R.string.searching) + "...",
 					getString(R.string.searching_product_msg));
 		}
 
 		@Override
 		protected void success() {
 			updatePrice(ScanUtils.getBranchProduct(), ScanUtils.getProduct());
 		}
 
 		@Override
 		protected void failure(ERROR code) {
 			if (code == ERROR.NOT_FOUND) {
 				if (ScanUtils.getProduct() != null) {
 					ScanUtils.setBranchProduct(new BranchProduct(0L, ScanUtils
 							.getProduct().getId(), MapUtils.getUserBranch()
 							.getId(), 0L));
 					updatePrice(ScanUtils.getBranchProduct(),
 							ScanUtils.getProduct());
 				} else {
 					Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 							code);
 					dlg.setPositiveButton(getString(R.string.create),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog,
 										int id) {
 									changeTab(VIEW.SCAN, NAMES.BRANCHPRODUCT);
 									dialog.cancel();
 								}
 							});
 					dlg.show();
 				}
 
 			} else {
 				Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 						code);
 				dlg.setPositiveButton(getString(R.string.ok),
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								dialog.cancel();
 							}
 						});
 				dlg.show();
 
 			}
 		}
 
 		@Override
 		protected ERROR retrieve() {
 			ScanUtils.setProduct(null);
 			ScanUtils.setBranchProduct(null);
 			Product p = EntityUtils.retrieveProduct(ScanUtils.getBarCode());
 			if (p == null) {
 				if (!PreferencesUtils.checkServer()) {
 					return ERROR.NOT_FOUND;
 				}
 				if (!isNetworkAvailable()) {
 					return ERROR.NOT_FOUND;
 				}
 				p = EntityUtils.retrieveProductServer(ScanUtils.getBarCode());
 				if (p == null) {
 					return ERROR.NOT_FOUND;
 				}
 			}
 			ScanUtils.setProduct(p);
 			BranchProduct bp = EntityUtils.retrieveBranchProduct(
 					ScanUtils.getBarCode(), MapUtils.getUserBranch().getId());
 			if (bp == null) {
 				if (!PreferencesUtils.checkServer()) {
 					return ERROR.NOT_FOUND;
 				}
 				if (!isNetworkAvailable()) {
 					return ERROR.NOT_FOUND;
 				}
 				bp = EntityUtils.retrieveBranchProductServer(ScanUtils
 						.getBarCode(), MapUtils.getUserBranch().getId());
 				if (bp == null) {
 					return ERROR.NOT_FOUND;
 				}
 			}
 			ScanUtils.setBranchProduct(bp);
 			return ERROR.SUCCESS;
 		}
 
 	}
 
 	class UpdateProductTask extends ProgressTask {
 		BranchProduct bp;
 		private int price;
 
 		public UpdateProductTask(BranchProduct bp, int price) {
 			super(HomeActivity.this, getString(R.string.updating) + "...",
 					getString(R.string.updating_product_msg));
 			this.bp = bp;
 			this.price = price;
 		}
 
 		@Override
 		protected ERROR retrieve() {
 			if (!isNetworkAvailable()) {
 				return ERROR.CLIENT;
 			}
 			if (RPCUtils.startServer() == ERROR.SERVER) {
 				return ERROR.SERVER;
 			}
 			return RPCUtils.updateBranchProduct(bp, price);
 		}
 
 		@Override
 		protected void success() {
 			changeTab(VIEW.SCAN, NAMES.BROWSE);
 		}
 
 		@Override
 		protected void failure(ERROR error_code) {
 			Builder dlg = DialogUtils.getErrorDialog(HomeActivity.this,
 					error_code);
 			dlg.setPositiveButton(getString(R.string.retry),
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int id) {
 							updatePrice(bp, ScanUtils.getProduct());
 							dialog.cancel();
 						}
 					});
 			dlg.show();
 
 		}
 	}
 
 }
