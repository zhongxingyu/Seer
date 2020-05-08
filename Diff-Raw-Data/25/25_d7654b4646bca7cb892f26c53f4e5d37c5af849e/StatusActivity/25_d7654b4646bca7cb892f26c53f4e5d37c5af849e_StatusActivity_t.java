 package com.four_envelope.android.activity;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.GridView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.four_envelope.android.R;
 import com.four_envelope.android.adapter.AccountAdapter;
 import com.four_envelope.android.adapter.PersonAdapter;
 import com.four_envelope.android.budget.BudgetWork;
 import com.four_envelope.android.operation.RequestStatusOperation;
 import com.four_envelope.android.store.StoreClient;
 
 /**
  * Show client status page, user properties, accounts
  * @author VMaximenko
  *
  */
 public class StatusActivity extends BaseActivity {
 
 	private TextView mCountry;
 	private TextView mCurrency;
 	private GridView mPersons;
 	private ListView mAccounts;
 	private View mAccountsEmpty;
 	private TextView mAccountLogin;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		mContentView = R.layout.status_view;
 		mMenuRes = R.menu.status_menu;
 		mProgressRes = R.string.progress_status_msg;
 		
 		super.onCreate(savedInstanceState);
         
         mCountry = (TextView) findViewById(R.id.country);
         mCurrency = (TextView) findViewById(R.id.currency);
         mPersons = (GridView) findViewById(R.id.persons);
         mAccounts = (ListView) findViewById(R.id.accounts);
         mAccountsEmpty = (View) findViewById(R.id.accounts_empty);
         mAccountLogin = (TextView) findViewById(R.id.account_login);
 	}
 
     protected void requestPageContent() {
     	super.requestPageContent();
     	
     	new RequestStatusOperation(this).execute();
 	}
     
 	void fillPageContent() {
 		mAccountLogin.setText( StoreClient.getLogin() );
 		
         mCountry.setText( BudgetWork.userData.getCountry().getValue() );
         mCurrency.setText( BudgetWork.userData.getCurrency().getValue() );
         
         mPersons.setAdapter(
         		new PersonAdapter( 
         				this, 
         				BudgetWork.userData.getPersons() ) );  
         
 		mAccounts.setEmptyView(mAccountsEmpty);
 		mAccounts.setAdapter(
 				new AccountAdapter(
 						this, 
 						BudgetWork.userData.getAccounts() ) );
 	}
 
 }
