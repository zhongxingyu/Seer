 package com.mawape.aimant.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.TextView;
import android.widget.Toast;
 
 import com.mawape.aimant.R;
 import com.mawape.aimant.entities.Categoria;
 
 public abstract class BaseActivity extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 	}
 
 	protected void configureMenuBar(Categoria categoria, boolean canGoBack) {
 		if (categoria != null) {// configure color and title
 			TextView txtTitle = (TextView) findViewById(R.id.commonMenuTitle);
 			txtTitle.setText(categoria.getNombre());
 
 			View view = findViewById(R.id.menu_bar);
 			view.setBackgroundColor(Color.parseColor("#" + categoria.getColor()));
 		}
 		if (canGoBack) {
 			ImageView imgGoBack = (ImageView) findViewById(R.id.commonMenuBack);
 			imgGoBack.setVisibility(View.VISIBLE);
 			imgGoBack.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					onBackPressed();
 				}
 			});
 		}
 
 		// handling menu info click
 		ImageView infoImg = (ImageView) findViewById(R.id.commonMenuInformation);
 		infoImg.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				showSplashAimant();
 			}
 		});

		final EditText searchField = (EditText) findViewById(R.id.commonMenuSearchField);
		searchField.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				String currentText = searchField.getText().toString();
				if (hasFocus) {
					searchField.setText("");
				} else if (!hasFocus
						&& (currentText == null || currentText.length() == 0)) {
					searchField.setText(R.string.search_msg);
				}
			}
		});

		// handling menu info click
		ImageView searchImg = (ImageView) findViewById(R.id.commonMenuSearchIcon);
		searchImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchField.requestFocus();
			}
		});

 	}
 
 	protected void configureMenuBar() {
 		configureMenuBar(null, false);
 	}
 
 	private void showSplashAimant() {
 		Intent intent = new Intent(getApplicationContext(),
 				InformationActivity.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		getApplicationContext().startActivity(intent);
 	}
 
 	@Override
 	public void onBackPressed() {
 		super.onBackPressed();
 	}
 }
