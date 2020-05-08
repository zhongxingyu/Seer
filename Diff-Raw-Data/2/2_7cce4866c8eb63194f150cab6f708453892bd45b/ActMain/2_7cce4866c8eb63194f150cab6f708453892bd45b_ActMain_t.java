 package com.nakedferret.simplepass.ui;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.StrictMode;
 import android.util.Log;
 import android.widget.Button;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.Background;
 import com.googlecode.androidannotations.annotations.Click;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.ViewById;
 import com.nakedferret.simplepass.PasswordStorageContract.Account;
 import com.nakedferret.simplepass.PasswordStorageContract.Group;
 import com.nakedferret.simplepass.PasswordStorageContract.Vault;
 import com.nakedferret.simplepass.PasswordStorageDbHelper;
 import com.nakedferret.simplepass.R;
 import com.nakedferret.simplepass.Utils;
 
 @EActivity(R.layout.act_main)
 public class ActMain extends SherlockActivity {
 
 	@ViewById
 	Button insertButton;
 
 	@ViewById
 	Button testDataButton;
 
 	@AfterViews
 	void initialize() {
 		Log.d("SimplePass", "Strict mode disabled...");
 		Log.d("SimplePass", "Thread Policy: " + StrictMode.getThreadPolicy());
 		Log.d("SimplePass", "VM Policy: " + StrictMode.getVmPolicy());
 
 		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
 				.detectAll().penaltyLog().build());
 
 		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll()
 				.penaltyLog().build());
 
 		Log.d("SimplePass", "Strict mode enabled...");
 		Log.d("SimplePass", "Thread Policy: " + StrictMode.getThreadPolicy());
 		Log.d("SimplePass", "VM Policy: " + StrictMode.getVmPolicy());
 		// Creates the database if the app is opened for the first time
 		initDB();
 	}
 
 	@Background
 	void initDB() {
 		// Upgrades or creates the database if needed
 		new PasswordStorageDbHelper(this).getWritableDatabase().close();
 	}
 
 	@Click(R.id.testDataButton)
 	void onCreateTestData() {
 		clearAndInsertTestData();
 	}
 
 	@Background
 	void clearAndInsertTestData() {
 		PasswordStorageDbHelper helper = new PasswordStorageDbHelper(this);
 		helper.clearData(null);
 		helper.close();
 
 		ContentResolver r = getContentResolver();
 
 		ContentValues vault = Utils.createVault("Personal", "secret", 2500);
 		Uri rowUri = r.insert(Utils.buildContentUri(Vault.TABLE_NAME), vault);
 		int vaultId = Integer.parseInt(rowUri.getLastPathSegment());
 		vault.put(Vault._ID, vaultId);
 
 		insertAccount("Reddit", "Entertainment", vault);
 		insertAccount("Xda-Developers", "Development", vault);
 		insertAccount("Twitter", "Social", vault);
 		insertAccount("Bank", "Financial", vault);
 		insertAccount("Amazon", "Retail", vault);
 	}
 
 	private void insertAccount(String name, String groupName,
 			ContentValues vault) {
 		ContentResolver r = getContentResolver();
 
 		Uri groupUri = Utils.buildContentUri(Group.TABLE_NAME);
 		Uri accountUri = Utils.buildContentUri(Account.TABLE_NAME);
 
 		ContentValues group = Utils.createGroup(groupName);
 		Uri rowUri = r.insert(groupUri, group);
 		group.put(Group._ID, rowUri.getLastPathSegment());
 
 		final String username = "naked_ferret";
 		final String pass = "super_secret";
 		final Long groupId = group.getAsLong(Group._ID);
 
		ContentValues account = Utils.createAccount(vault, name, username,
 				pass, groupId, "secret");
 		r.insert(accountUri, account);
 	}
 
 	@Click(R.id.testFrags)
 	void testGenericFrag() {
 		startActivity(new Intent(this, ActFragTest_.class));
 	}
 
 }
