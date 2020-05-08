 package com.dnsalias.sanja.simplecarbocalc;
 
 import java.io.File;
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Environment;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 import android.widget.SimpleCursorAdapter;
 
 public class SimpleCarboCalcExport extends Activity
 {
 	private static final String LOGTAG= "SimpleCarboCalcExport";
 	
 	RadioGroup mType;
 	EditText mFile;
 	CheckBox mUnits;
 	CheckBox mLangs;
 	CheckBox mProds;
 	CheckBox mAllProds;
 	ListView mExportProds;
 	
 	CompoundButton.OnCheckedChangeListener allCangeListener= new CompoundButton.OnCheckedChangeListener()
 	{
 		public void	 onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 		{
 			if (isChecked)
 			{
 				mExportProds.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, new String[0]));
				mExportProds.setFocusable(false);
 			}
 			else
 			{
 				mExportProds.setAdapter(new SimpleCursorAdapter(getBaseContext(),
 						android.R.layout.simple_list_item_multiple_choice,
 						ProdList.getInstance().getCoursorForRequest(null, -1),
 						new String[] {ProdList.PROD_NAME },
 						new int[] { android.R.id.text1 }));
				mExportProds.setFocusable(true);
 			}
 		}
 	};
 
     RadioGroup.OnCheckedChangeListener typeChangeListener= new RadioGroup.OnCheckedChangeListener()
     {
     	public void onCheckedChanged(RadioGroup group, int checkedId)
         {
         	File dir= new File(Environment.getExternalStorageDirectory(), "SimpleCarboCalc");
         	if (checkedId == R.id.backup)
         	{
         		File file= new File(dir, "backup.txt");
         		mFile.setText(file.toString());
         		mFile.setEnabled(false);
         		mUnits.setChecked(true); mUnits.setEnabled(false);
         		mLangs.setChecked(true); mLangs.setEnabled(false);
         		mProds.setChecked(true); mProds.setEnabled(false);
         		mAllProds.setChecked(true); mAllProds.setEnabled(false);
         	}
         	else
         	{
         		
         		Calendar cl= Calendar.getInstance();
         		File file= new File(dir, String.format("backup%04d%02d%02d%02d%02d.txt",
         				cl.get(Calendar.YEAR), cl.get(Calendar.MONTH) + 1, cl.get(Calendar.DAY_OF_MONTH),
         				cl.get(Calendar.HOUR), cl.get(Calendar.MINUTE)));
         		mFile.setText(file.toString());
         		mFile.setEnabled(true);
         		mUnits.setChecked(false); mUnits.setEnabled(true);
         		mLangs.setChecked(false); mLangs.setEnabled(true);
         		mProds.setChecked(true); mProds.setEnabled(true);
         		mAllProds.setChecked(true); mAllProds.setEnabled(true);
         	}
         }
     };
 
 	@Override
     protected void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.export);
         setTitle(R.string.export_name);
         
         mType= (RadioGroup) findViewById(R.id.radioExportType);
         mFile= (EditText) findViewById(R.id.exportFile);
         mUnits= (CheckBox) findViewById(R.id.exportUnits);
         mLangs= (CheckBox) findViewById(R.id.exportLanguages);
         mProds= (CheckBox) findViewById(R.id.exportProducts);
         mAllProds= (CheckBox) findViewById(R.id.allProds);
         mExportProds= (ListView) findViewById(R.id.exportProdsList);
         mExportProds.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item, new String[0]));
         
         mAllProds.setOnCheckedChangeListener(allCangeListener);
         mType.setOnCheckedChangeListener(typeChangeListener);
         mType.check(R.id.backup);
 	}
 }
