 package org.madbit.drugbox;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class DrugDetailsActivity extends Activity {
 
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.drug_details);
 		
 		TextView name = (TextView) findViewById(R.id.name);
 		TextView type = (TextView) findViewById(R.id.type);
 		TextView brand = (TextView) findViewById(R.id.brand);
 		TextView purchase = (TextView) findViewById(R.id.purchase);
 		TextView expire = (TextView) findViewById(R.id.expire);
 		TextView pathology = (TextView) findViewById(R.id.pathology);
 		TextView minAge = (TextView) findViewById(R.id.minAge);
		TextView category = (TextView) findViewById(R.id.category);
 		
 		String nameExtra = (String) this.getIntent().getSerializableExtra("name");
 		String typeExtra = (String) this.getIntent().getSerializableExtra("type");
 		String brandExtra = (String) this.getIntent().getSerializableExtra("brand");
 		String purchaseExtra = (String) this.getIntent().getSerializableExtra("purchase");
 		String expireExtra = (String) this.getIntent().getSerializableExtra("expire");
 		String pathologyExtra = (String) this.getIntent().getSerializableExtra("pathology");
 		Integer minAgeExtra = (Integer) this.getIntent().getSerializableExtra("minAge");
		String categoryExtra = (String) this.getIntent().getSerializableExtra("category");
 		
 		name.setText(nameExtra);
 		type.setText(typeExtra);
 		brand.setText(brandExtra);
 		purchase.setText(purchaseExtra);
 		expire.setText(expireExtra);
 		pathology.setText(pathologyExtra);
 		//minAge.setText(minAgeExtra);
		category.setText(categoryExtra);
 	}
 }
