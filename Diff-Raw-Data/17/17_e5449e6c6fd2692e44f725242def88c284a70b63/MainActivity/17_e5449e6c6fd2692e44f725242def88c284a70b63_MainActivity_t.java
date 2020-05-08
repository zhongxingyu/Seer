 package com.pas.main;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
 import com.j256.ormlite.dao.RuntimeExceptionDao;
 import com.pas.bean.Expenses;
 import com.pas.databases.DatabaseHelper;
 
 public class MainActivity extends OrmLiteBaseActivity<DatabaseHelper> {
 
 	private Button btnSimpan;
 	private Button btnReset;
 	private Spinner iSpinner;
 	private EditText iEditCost;
 	private EditText iEditDesc;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		Spinner spinner = (Spinner) findViewById(R.id.exp_spinner);
 		ArrayAdapter<CharSequence> adapter = 
 				ArrayAdapter.createFromResource(this, 
 						R.array.expenses_array, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinner.setAdapter(adapter);
 
 		iSpinner = (Spinner) findViewById(R.id.exp_spinner);
 		iEditCost = (EditText) findViewById(R.id.edit_cost);
 		iEditDesc = (EditText) findViewById(R.id.edit_desc);
 
 		addListnerButton();
 	}
 
 	public void addListnerButton(){
 		
 		btnSimpan = (Button) findViewById(R.id.btn_simpan);
 		btnReset = (Button) findViewById(R.id.btn_reset);
 
 		btnSimpan.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				final String expCode = iSpinner.getSelectedItem().toString();
 				final String expDesc = iEditDesc.getText().toString();
 				final String expCost = iEditCost.getText().toString();
 				final BigDecimal exptCostInt = new BigDecimal(expCost);
 				
 				RuntimeExceptionDao<Expenses, Integer> expensesDao =
 						getHelper().getExpensesDao();
 				
 				Expenses exp = new Expenses(expCode, expDesc, new Date(), exptCostInt);
 				expensesDao.create(exp);
 				
 				Toast.makeText(getApplicationContext(), "Simpan Berhasil", 1).show();
 			}
 		});
 		
 		btnReset.setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menuBtnReport:
 			Intent intent = new Intent(this, ReportActivity.class);
 			startActivity(intent);
 			break;
 		}
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	public void testDatabaseStuff(){
 		RuntimeExceptionDao<Expenses, Integer> expensesDao = getHelper().getExpensesDao();
 		List<Expenses> expList = expensesDao.queryForAll();
 
 	}
 }
