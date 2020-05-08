 package com.pas.main;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.GenericRawResults;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.pas.bean.Expenses;
 import com.pas.databases.DatabaseHelper;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.TextView;
 
 public class ReportActivity extends OrmLiteBaseActivity<DatabaseHelper>{
 
 	private TextView tvTotal;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_report);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
 		populateFields();
 	}
 	
 	public void populateFields(){
 		String sum = "";
 		tvTotal = (TextView) findViewById(R.id.text_total);
 		
 		RuntimeExceptionDao<Expenses, Integer> expensesDao =
 				getHelper().getExpensesDao();
 		GenericRawResults<String[]> rawResults =
 				expensesDao.queryRaw("select sum(expensesAmount) from Expenses");
 		try {
 			List<String[]> results = rawResults.getResults();
 			for(String[] resultArray : results){
 				sum = resultArray[0];
 			}
 			tvTotal.setText(sum);
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 }
