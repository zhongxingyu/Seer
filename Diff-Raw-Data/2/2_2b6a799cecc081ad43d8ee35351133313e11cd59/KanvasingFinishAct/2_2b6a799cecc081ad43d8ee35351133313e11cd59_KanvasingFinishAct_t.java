 package dp.mobile.store;
 
 import java.text.NumberFormat;
 import java.util.Date;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 import dp.mobile.store.helper.DatabaseAdapter;
 import dp.mobile.store.helper.Utilities;
 import dp.mobile.store.helper.tables.Counter;
 import dp.mobile.store.helper.tables.DtlSales;
 import dp.mobile.store.helper.tables.TrnRoute;
 import dp.mobile.store.helper.tables.TrnSales;
 import dp.mobile.store.helper.tables.User;
 
 public class KanvasingFinishAct extends Activity implements OnClickListener {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.kanvasing_finish);
 		mStoreID = getIntent().getExtras().getString(Utilities.INTENT_STORE_ID);
 		
 		initComp();
 	}
 	
 	private void initComp(){
 		mConfirmButton = (Button) findViewById(R.id.confirm);
 		mConfirmButton.setOnClickListener(this);
 		
 		mTotal 			= (TextView)findViewById(R.id.info1);
 		mDiscount		= (TextView)findViewById(R.id.info2);
 		mNetto			= (TextView)findViewById(R.id.info3);
 		mPayment		= (TextView)findViewById(R.id.info4);
 		mCreditLimit	= (TextView)findViewById(R.id.info5);
 		mReceivable		= (TextView)findViewById(R.id.info6);
 		
 		//Decode JSON to get finished transaction price list
 		mFinishTransPL = decodeJSON();
 		
 		//Calculate total cost
 		mTotalNum = calculateTotal(mFinishTransPL);
 		mTotal.setText("Rp " + NumberFormat.getIntegerInstance().format(mTotalNum));
 		
 		//Set Receivable and CreditLimit (from TrnRoute where customer_code = mStoreID
 		mCurrTrnRoute = getCurrTrnRoute(mStoreID);
 		mReceivable.setText("Rp " + NumberFormat.getIntegerInstance().format(mCurrTrnRoute.mReceivable));
 		mCreditLimit.setText("Rp " + NumberFormat.getIntegerInstance().format(mCurrTrnRoute.mCreditLimit));
 		
 		/// TODO : Where is 'discount' ?
 		mDiscNum = 0;
 		mDiscount.setText("Rp " + NumberFormat.getIntegerInstance().format(mDiscNum));
 		
 		calculateOthers();
 	}
 	
 	private TrnRoute getCurrTrnRoute(String storeID){
 		Cursor cursor = DatabaseAdapter.instance(getBaseContext()).rawQuery("SELECT * " +
 				"FROM mobile_trnroute " +
 				"WHERE customer_code =?", new String[]{storeID});
 		
 		cursor.moveToFirst();
 		TrnRoute retval = new TrnRoute(cursor.getString(0), Utilities.formatStr(cursor.getString(1)),
 				cursor.getString(2), cursor.getString(3), cursor.getLong(4),
 				cursor.getString(5), cursor.getString(6), cursor.getString(7),
 				cursor.getString(8), cursor.getString(9), cursor.getString(10),
 				cursor.getString(11), cursor.getLong(12), cursor.getLong(13),
 				Utilities.formatStr(cursor.getString(14)), cursor.getString(15),
 				cursor.getString(16)); 
 		cursor.close();
 		
 		return retval;
 	}
 	
 	//Decode JSON to FinishTransactionPriceList array
 	private FinishTransactionPriceList[] decodeJSON(){
 		String jsonStr = getIntent().getExtras().getString(Utilities.INTENT_TRANSACTION_DETAILS);
 		JSONArray jsonArr = null; 
 		try {
 			jsonArr = new JSONArray(jsonStr);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
 		FinishTransactionPriceList[] retval = new FinishTransactionPriceList[jsonArr.length()];
 		for(int i=0; i<retval.length; ++i){
 			JSONObject json;
 			try {
 				json = jsonArr.getJSONObject(i);
 				retval[i] = new FinishTransactionPriceList(json.getString("product_code"), 
 						json.getLong("quantity"), json.getLong("price"), json.getLong("bonus"));
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return retval;
 	}
 	
 	private long calculateTotal(FinishTransactionPriceList[] finishTransPL){
 		long cost = 0;		
 		for(int i=0; i<finishTransPL.length; ++i){
 			cost += finishTransPL[i].mPrice * finishTransPL[i].mQuantity;
 		}
 		
 		return cost;
 	}
 	
 	private void calculateOthers() {
 		mNettoNum = mTotalNum - mDiscNum;
 		mPaymentNum = mNettoNum + mCurrTrnRoute.mReceivable - mCurrTrnRoute.mCreditLimit;
 		
 		mNetto.setText("Rp " + NumberFormat.getIntegerInstance().format(mNettoNum));
 		mPayment.setText("Rp " + NumberFormat.getIntegerInstance().format(mPaymentNum));
 	}
 	
 	private void updateDatabase(){
 		//Get last record of mobile_trnsales
 		int lastTrnSalesID = 0;
 		Cursor cursor = DatabaseAdapter.instance(getBaseContext()).rawQuery("SELECT id " +
 				"FROM mobile_trnsales ", null);
 		cursor.moveToFirst();
 		do{
 			int temp = Integer.valueOf(cursor.getString(0));
 			if(temp > lastTrnSalesID)
 				lastTrnSalesID = temp;
 		}while(cursor.moveToNext());
 		cursor.close();
 		
 		//Get mobile_user.unitcompany_code
 		cursor = DatabaseAdapter.instance(getBaseContext()).rawQuery("SELECT unitcompany_code " +
 				"FROM mobile_user " +
 				"LIMIT 1", null);
 		cursor.moveToFirst();
 		User user = new User("dummy ID"); 
 		user.mUnitCompanyCode = cursor.getString(0);
 		cursor.close();
 		
 		//Get mobile_counter.code
 		cursor = DatabaseAdapter.instance(getBaseContext()).rawQuery("SELECT code, counter " +
 				"FROM mobile_counter " +
 				"LIMIT 1", null);
 		cursor.moveToFirst();
 		Counter counter = new Counter("dummy ID"); 
 		counter.mCode = cursor.getString(0);
 		counter.mCounter = cursor.getLong(1);
 		cursor.close();
 		
 		//INSERT into mobile_trnsales
 		String refno = counter.mCode+"/"+Utilities.formatDate(new Date()).substring(0, 2)+"/"+String.valueOf(counter.mCounter); 
 		TrnSales newTrnSales = new TrnSales(String.valueOf(++lastTrnSalesID),
 				user.mUnitCompanyCode, "01", new Date(), refno, "ar01", "sales kanvas "+mCurrTrnRoute.mCustomerName, 
 				"checkNo01", new Date(), mCurrTrnRoute.mCustomerCode, "40115", "SAT01", mNettoNum, mNettoNum, 0, 0);
 		DatabaseAdapter.instance(getBaseContext()).insert(TrnSales.getTableName(), newTrnSales);
 		
 		//Get last record of mobile_dtlsales
 		int lastDtlSalesID = 0;
 		cursor = DatabaseAdapter.instance(getBaseContext()).rawQuery("SELECT id " +
 				"FROM mobile_dtlsales ", null);
 		cursor.moveToFirst();
 		do{
 			int temp = Integer.valueOf(cursor.getString(0));
			if(temp > lastDtlSalesID)
 				lastDtlSalesID = temp;
 		}while(cursor.moveToNext()); 
 		cursor.close();
 
 		//INSERT into mobile_dtlsales
 		for(int i=0; i<mFinishTransPL.length; ++i){
 			DtlSales newDtlSales = new DtlSales(String.valueOf(++lastDtlSalesID), String.valueOf(lastTrnSalesID),
 					mFinishTransPL[i].mProductCode, mFinishTransPL[i].mQuantity, 0,
 					mFinishTransPL[i].mQuantity, mFinishTransPL[i].mPrice, mFinishTransPL[i].mQuantity*mFinishTransPL[i].mPrice, 
 					mFinishTransPL[i].mQuantity*mFinishTransPL[i].mPrice, 0, 
 					mFinishTransPL[i].mQuantity*mFinishTransPL[i].mPrice, 0);
 			DatabaseAdapter.instance(getBaseContext()).insert(DtlSales.getTableName(), newDtlSales);
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		if (v == mConfirmButton) {
 			updateDatabase();
 			
 			setResult(RESULT_OK);
 			Toast.makeText(this, "Transaction Complete", 1000).show();
 			
 			finish();
 		}
 	}
 	
 	private TextView	mTotal, mDiscount, mNetto, mPayment, mCreditLimit, mReceivable;
 	private Button		mConfirmButton;
 	private String		mStoreID;
 	private long		mTotalNum, mDiscNum, mNettoNum, mPaymentNum;
 	
 	private TrnRoute	mCurrTrnRoute;
 	private FinishTransactionPriceList[] mFinishTransPL;
 	
 	private class FinishTransactionPriceList{
 		public FinishTransactionPriceList(String productCode, long productQuantity, long price, long bonus) {
 			mProductCode	= productCode;
 			mQuantity		= productQuantity;
 			mPrice			= price;
 			mBonus			= bonus;
 		}
 		
 		public String	mProductCode;
 		public long		mQuantity, mPrice, mBonus;
 	}
 }
