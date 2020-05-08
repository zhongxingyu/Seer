 package com.selesse.android.winedb.activity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ScrollView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.selesse.android.winedb.R;
 import com.selesse.android.winedb.database.Wine;
 import com.selesse.android.winedb.database.WineDatabaseHandler;
 import com.selesse.android.winedb.model.WineColor;
 
 public class CreateOrEditWineActivity extends SherlockActivity {
     private Wine wine = new Wine();
     private Wine originalWine = new Wine();
     private EditText barcodeText, nameText, countryText, yearText, descText, ratingText, priceText,
             commentText, imageText;
     private Spinner spinner;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         Bundle bundle = this.getIntent().getExtras();
 
         if (savedInstanceState != null) {
             wine.setId(savedInstanceState.getLong("id"));
         }
         if (bundle != null) {
             wine.setId(bundle.getLong("id"));
             if (wine.getId() <= 0) {
                 wine = (Wine) bundle.getSerializable("wine");
             } else {
                 wine = WineDatabaseHandler.getInstance(this).getWine(wine.getId());
             }
         }
         originalWine = new Wine(wine);
 
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edit_wine);
 
         barcodeText = (EditText) findViewById(R.id.barcodeEditText);
         nameText = (EditText) findViewById(R.id.nameEditText);
         countryText = (EditText) findViewById(R.id.countryEditText);
         yearText = (EditText) findViewById(R.id.yearEditText);
         descText = (EditText) findViewById(R.id.descEditText);
         ratingText = (EditText) findViewById(R.id.ratingEditText);
         priceText = (EditText) findViewById(R.id.priceEditText);
         commentText = (EditText) findViewById(R.id.commentEditText);
         imageText = (EditText) findViewById(R.id.imageEditText);
 
         spinner = (Spinner) findViewById(R.id.wineColorSpinner);
 
         ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
                 android.R.layout.simple_spinner_item, WineColor.getLocalizedStrings(getApplicationContext()
                 .getResources()));
         spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         spinner.setAdapter(spinnerArrayAdapter);
 
         if (!wine.getBarcode().equals("")) {
             barcodeText.setText(wine.getBarcode());
         }
         if (!wine.getName().equals("")) {
             nameText.setText(wine.getName());
         }
         if (!wine.getCountry().equals("")) {
             countryText.setText(wine.getCountry());
         }
         // TODO input validation (not here)
         if (wine.getYear() > 0 && wine.getYear() < 2500) {
             yearText.setText(String.valueOf(wine.getYear()));
         }
         if (!wine.getDescription().equals("")) {
             descText.setText(wine.getDescription());
         }
         // TODO input validation (not here)
         if (wine.getRating() > 0 && wine.getRating() < 11) {
             ratingText.setText(String.valueOf(wine.getRating()));
         }
         if (!wine.getPrice().equals("")) {
             priceText.setText(wine.getPrice());
         }
         if (!wine.getComment().equals("")) {
             commentText.setText(wine.getComment());
         }
         if (!wine.getImageURL().equals("")) {
             imageText.setText(wine.getImageURL());
         }
         if (wine.getColor() != WineColor.UNKNOWN) {
             spinner.setSelection(wine.getColor().ordinal());
         }
 
         // remove the icon from the ActionBar
         getSupportActionBar().setIcon(new ColorDrawable(android.R.color.transparent));
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.edit_wine, menu);
         return true;
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (wineHasBeenModified()) {
                 new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                         .setMessage(R.string.save_before_exit)
                         .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 validateAndSave();
                             }
                         }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         finish();
                     }
                 }).show();
             }
         }
         return super.onKeyDown(keyCode, event);
     }
 
     private boolean wineHasBeenModified() {
         saveFields(wine);
         return !wine.equals(originalWine);
     }
 
     @Override
     public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
         switch (item.getItemId()) {
             case R.id.save_wine_button:
                 validateAndSave();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     private void validateAndSave() {
         // make sure that if you're pressing save, you save a name
         if (nameText.getText().toString().trim().length() == 0) {
             Toast.makeText(getApplicationContext(), R.string.empty_name, Toast.LENGTH_SHORT).show();
             nameText.setFocusableInTouchMode(true);
             nameText.requestFocus();
 
             // scroll back up to highlight the field in question
             ScrollView scrollView = (ScrollView) findViewById(R.id.edit_wine_scrollview);
             scrollView.scrollTo(0, 0);
             return;
         }
 
         saveFields(wine);
 
         WineDatabaseHandler.getInstance(getApplicationContext()).putWine(wine);
         Intent data = new Intent();
         data.putExtra("id", wine.getId());
         setResult(RESULT_OK, data);
         finish();
     }
 
     private void saveFields(Wine wine) {
         wine.setBarcode(barcodeText.getText().toString());
         wine.setName(nameText.getText().toString());
         wine.setCountry(countryText.getText().toString());
         if (yearText.getText().toString().trim().length() > 0) {
             wine.setYear(Integer.parseInt(yearText.getText().toString()));
         } else {
             wine.setYear(-1);
         }
         wine.setDescription(descText.getText().toString());
         if (ratingText.getText().toString().trim().length() > 0) {
             wine.setRating(Integer.parseInt(ratingText.getText().toString()));
         } else {
             wine.setRating(-1);
         }
         wine.setPrice(priceText.getText().toString());
         wine.setComment(commentText.getText().toString());
         wine.setImageURL(imageText.getText().toString());
 
         if (spinner.getSelectedItemPosition() > 0) {
             wine.setColor(WineColor.values()[spinner.getSelectedItemPosition()]);
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         outState.putLong("id", wine.getId());
     }
 }
