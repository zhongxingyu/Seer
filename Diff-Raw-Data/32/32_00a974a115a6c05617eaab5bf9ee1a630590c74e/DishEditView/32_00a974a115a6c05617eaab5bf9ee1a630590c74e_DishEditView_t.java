 package be.uclouvain.sinf1225.gourmet;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import be.uclouvain.sinf1225.gourmet.models.Dish;
 import be.uclouvain.sinf1225.gourmet.utils.GourmetUtils;
 import android.widget.CheckBox;
 
 /**
  * 
  * @author qeggerickx
  * 
  */
 public class DishEditView extends Activity
 {
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 	    GourmetUtils.createMenu(menu, this, R.id.search);
 	    return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		return GourmetUtils.onMenuItemSelected(item, this);
 	}
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_dish_edit);
 
 		int dishId = getIntent().getExtras().getInt("dishId");		
 		final Dish dish = Dish.getDish(dishId);
 		// button's creation
 		final EditText EditName = (EditText) findViewById(R.id.EditDishName);
 		final Spinner EditCategory = (Spinner) findViewById(R.id.dish_category_edit);
 		final EditText EditPrice = (EditText) findViewById(R.id.EditDishPrice);
 		final EditText EditAvailable = (EditText) findViewById(R.id.EditDishAvailable);
 		final EditText EditDescription = (EditText) findViewById(R.id.EditDishDescription);
 
 		final CheckBox EditSpicy = (CheckBox) findViewById(R.id.EditDishSpicy);
 		final CheckBox EditVegan = (CheckBox) findViewById(R.id.EditDishVegan);
 		final CheckBox EditAllergen = (CheckBox) findViewById(R.id.EditDishAllergen);
 
 		final Button ApplyButton = (Button) findViewById(R.id.buttonDishApply);
 		final Button DeleteButton = (Button) findViewById(R.id.buttonDishDelete);
 		final Button imageButton = (Button) findViewById(R.id.dishImageButton);
 
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 		        R.array.dish_category, android.R.layout.simple_spinner_item);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		EditCategory.setAdapter(adapter);
 		int position = adapter.getPosition(dish.getCategory());
 		
 		// field's init
 		EditName.setText(dish.getName());
 		EditPrice.setText("" + dish.getPrice());
 		EditAvailable.setText(""+dish.getAvailable());
 		EditDescription.setText(dish.getDescription());
 		EditCategory.setSelection(position);
 
 		if (dish.getSpicy() == 1)
 			EditSpicy.setChecked(true); // to be checked, true equals 1 or 0
 		else
 			EditSpicy.setChecked(false);
 
 		if (dish.getVegan() == 1)
 			EditVegan.setChecked(true);
 		else
 			EditVegan.setChecked(false);
 
 		if (dish.getAllergen() == 1)
 			EditAllergen.setChecked(true);
 		else
 			EditAllergen.setChecked(false);
 
 		ApplyButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View v)
 			{	
 				dish.setName(EditName.getText().toString());
 				dish.setCategory(EditCategory.getSelectedItem().toString());
 				dish.setPrice(Double.parseDouble(EditPrice.getText().toString()));
 				dish.setAvailable(Integer.parseInt(EditAvailable.getText().toString()));
 				dish.setDescription(EditDescription.getText().toString());
 
 				if (EditSpicy.isChecked())
 					dish.setSpicy(1);
 				else
 					dish.setSpicy(0);
 
 				if (EditVegan.isChecked())
 					dish.setVegan(1);
 				else
 					dish.setVegan(0);
 
 				if (EditAllergen.isChecked())
 					dish.setAllergen(1);
 				else
 					dish.setAllergen(0);
 
 				dish.updateDish();
 				Intent returnIntent = new Intent();
 				returnIntent.putExtra("deleted","notdone");
 				setResult(RESULT_OK,returnIntent);
 				finish();
 			}
 		});
 		DeleteButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				dish.deleteDish();
 				Intent returnIntent = new Intent();
 				returnIntent.putExtra("deleted","done");
 				setResult(RESULT_OK,returnIntent);
 				finish();
 			}
 		});
 		imageButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				Intent intent = new Intent(DishEditView.this, ViewDishImage.class);
 				intent.putExtra("dishId", dish.getDishId());
 				startActivity(intent);
 			}
 		});
 	}
 
 	public void onPause()
 	{
 		super.onStop();
 	}
 
 	public void onResume()
 	{
 		super.onResume();
 	}
	public void onBackPressed() {
		Intent returnIntent = new Intent();
		returnIntent.putExtra("deleted","notdone");
		setResult(RESULT_OK,returnIntent);
		finish();
	 }
 
 }
