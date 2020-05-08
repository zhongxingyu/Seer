 package nl.digitalica.skydivekompasroos;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.UUID;
 
 import nl.digitalica.skydivekompasroos.CanopyTypeListActivity.SortingEnum;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.Spinner;
 import android.support.v4.app.NavUtils;
 
 public class SpecificCanopyEdit extends KompasroosBaseActivity {
 
 	private String typesSpinner[];
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_specific_canopy_edit);
 
 		List<CanopyType> canopyTypesWithSpecialType = CanopyType
 				.getAllCanopyTypesInList(SpecificCanopyEdit.this);
 		List<CanopyType> canopyTypes = new ArrayList<CanopyType>();
 		for (CanopyType type : canopyTypesWithSpecialType)
 			if (!type.isSpecialCatchAllCanopy)
 				canopyTypes.add(type);
 
 		Comparator<CanopyType> canopyComparator = new CanopyType.ComparatorByNameManufacturer();
 		Collections.sort(canopyTypes, canopyComparator);
 
 		typesSpinner = new String[canopyTypes.size()];
 		int i = 0;
 		for (CanopyType type : canopyTypes)
 			typesSpinner[i++] = type.specificName();
 
 		Spinner types = (Spinner) findViewById(R.id.spinnerType);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, typesSpinner);
 		types.setAdapter(adapter);
 
 		// get the id of the specific canopy we're working on, and tag it on
 		// delete and save buttons
 		Bundle b = getIntent().getExtras();
 		int specificCanopyId = b.getInt(SPECIFICCANOPYID_KEY);
 		ImageButton saveSpecificCanopyButton = (ImageButton) findViewById(R.id.buttonSave);
 		ImageButton deleteButton = (ImageButton) findViewById(R.id.buttonDelete);
 		saveSpecificCanopyButton.setTag(specificCanopyId);
 		deleteButton.setTag(specificCanopyId);
 
 		// check if we're adding new specific canopy or editing an existing one
 		if (specificCanopyId == 0) {
 			deleteButton.setEnabled(false); // we can't delete new addition
 			int newSpecificCanopyId = SpecificCanopy.getSpecificCanopiesInList(
 					SpecificCanopyEdit.this).size() + 1;
 			saveSpecificCanopyButton.setTag(newSpecificCanopyId);
 		} else {
 			saveSpecificCanopyButton.setTag(specificCanopyId);
 			EditText etSize = (EditText) findViewById(R.id.editTextSize);
 			Spinner spType = (Spinner) findViewById(R.id.spinnerType);
 			EditText etRemarks = (EditText) findViewById(R.id.editTextRemarks);
 			SpecificCanopy spc = SpecificCanopy.getSpecificCanopy(
 					SpecificCanopyEdit.this, specificCanopyId);
 			CanopyType ct = CanopyType.getCanopy(spc.typeId,
 					SpecificCanopyEdit.this);
 			etSize.setText(Integer.toString(spc.size));
 			int position = adapter.getPosition(ct.specificName());
 			spType.setSelection(position);
 			etRemarks.setText(spc.remarks);
 
 			// add click handler to delete button.
 			deleteButton.setOnClickListener(new View.OnClickListener() {
 
 				public void onClick(View v) {
 					int specificCanopyId = (Integer) v.getTag();
 					SpecificCanopy.delete(SpecificCanopyEdit.this,
 							specificCanopyId);
 					SpecificCanopyEdit.this.finish();
 				}
 			});
 
 		}
 
 		// add click handler to save button.
 		saveSpecificCanopyButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				int specificCanopyId = (Integer) v.getTag();
 				EditText etSize = (EditText) findViewById(R.id.editTextSize);
 				Spinner spType = (Spinner) findViewById(R.id.spinnerType);
 				EditText etRemarks = (EditText) findViewById(R.id.editTextRemarks);
 
 				int size = Integer.parseInt(etSize.getText().toString());
 				String typeSpecificName = (String) spType.getSelectedItem();
 				List<CanopyType> canopyTypes = CanopyType
 						.getAllCanopyTypesInList(SpecificCanopyEdit.this);
 				UUID typeId = null;
 				for (CanopyType ct : canopyTypes)
 					if (typeSpecificName.equals(ct.specificName()))
 						typeId = ct.id;
				String remarks = etRemarks.getText().toString();
 				SpecificCanopy.save(SpecificCanopyEdit.this, specificCanopyId,
 						size, typeId, remarks);
 				SpecificCanopyEdit.this.finish();
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_specific_list_edit, menu);
 		return true;
 	}
 
 }
