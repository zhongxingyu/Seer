 package nl.digitalica.skydivekompasroos;
 
 import java.util.HashMap;
 import java.util.List;
 
 import android.accounts.NetworkErrorException;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class CanopyDetailsActivity extends KompasroosBaseActivity {
 
 	static Canopy currentCanopy;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_canopydetails);
 
 		Bundle extras = getIntent().getExtras();
 		String canopyKey = extras.getString(CANOPYKEYEXTRA);
 		List<Canopy> thisCanopy = Canopy.getCanopiesInList(canopyKey, this);
 		HashMap<String, Manufacturer> manufacturers = Manufacturer
 				.getManufacturerHash(CanopyDetailsActivity.this);
 
 		if (thisCanopy.size() != 1)
 			Log.e(LOG_TAG, "Onjuist aantal op basis van key " + canopyKey);
 
 		currentCanopy = thisCanopy.get(0);
 		Manufacturer manufacturer = manufacturers
 				.get(currentCanopy.manufacturer);
 
 		String url = manufacturer.url;
 		if (currentCanopy.url != null && !currentCanopy.url.equals(""))
 			url = currentCanopy.url; // if we have a canopy url use that.
 
 		TextView tvName = (TextView) findViewById(R.id.textViewNameText);
 		TextView tvCategory = (TextView) findViewById(R.id.textViewCategoryText);
 		TextView tvAdvise = (TextView) findViewById(R.id.textViewAdviseText);
 		TextView tvExperience = (TextView) findViewById(R.id.textViewExperienceText);
 		TextView tvCells = (TextView) findViewById(R.id.textViewCellsText);
 		TextView tvSizes = (TextView) findViewById(R.id.textViewSizesText);
 		TextView tvUrl = (TextView) findViewById(R.id.textViewUrlText);
 		TextView tvManufacturer = (TextView) findViewById(R.id.textViewManufacturerText);
 		TextView tvManufacturerCountry = (TextView) findViewById(R.id.textViewManufacturerCountryText);
 		TextView tvProduction = (TextView) findViewById(R.id.textViewProductionText);
 		TextView tvDropzoneId = (TextView) findViewById(R.id.textViewDropzoneIdText);
 		TextView tvRemarks = (TextView) findViewById(R.id.textViewRemarksText);
 
 		int acceptability = currentCanopy.acceptablility(currentMaxCategory,
 				currentWeight);
 
 		tvName.setText(currentCanopy.name);
 		tvName.setBackgroundDrawable(backgroundDrawableForAcceptance(acceptability));
 
 		tvCategory.setText(Integer.toString(currentCanopy.category));
 		String advice = "";
 		switch (acceptability) {
 		case Canopy.ACCEPTABLE:
 			advice = String.format(getString(R.string.canopyAdviseAcceptable),
 					currentMinArea);
 			break;
 		case Canopy.NEEDEDSIZENOTAVAILABLE:
 			advice = String.format(
 					getString(R.string.canopyAdviseNeededSizeNotAvailable),
 					currentMinArea);
 			break;
 		case Canopy.CATEGORYTOOHIGH:
 			int extraNeededJumsThis12Months = Calculation.MINIMUMJUMPSLAST12MONTHS[currentCanopy.category]
 					- currentJumpsLast12Months;
 			int extraNeededTotalJumps = Calculation.MINIMUMTOTALJUMPS[currentCanopy.category]
 					- currentTotalJumps;
 			int minimalExtraNeededJumps = Math.max(extraNeededJumsThis12Months,
 					extraNeededTotalJumps);
			advice = String.format(
					getString(R.string.canopyAdviseCategoryTooHigh),
					minimalExtraNeededJumps);
 			break;
 		}
 		tvAdvise.setText(advice);
 		String[] neededExperience = getResources().getStringArray(
 				R.array.neededExperience);
 		tvExperience.setText(neededExperience[currentCanopy.category]);
 		tvCells.setText(currentCanopy.cells);
 		String sizes = "";
 		if (currentCanopy.minSize != null && !currentCanopy.minSize.equals(""))
 			if (currentCanopy.maxSize != null
 					&& !currentCanopy.maxSize.equals("")) {
 				// TODO: change to format string in strings for translation
				sizes = String.format(getString(R.string.detailsSizesRange),
						currentCanopy.minSize, currentCanopy.maxSize);
 			}
 		tvSizes.setText(sizes);
 		tvUrl.setText(url);
 		tvManufacturer.setText(currentCanopy.manufacturer);
 		tvManufacturerCountry.setText(manufacturer.countryFullName());
 		tvDropzoneId.setText(currentCanopy.dropZoneUrl());
 		StringBuilder remarks = new StringBuilder();
 		if (currentCanopy.remarks() != null
 				&& !currentCanopy.remarks().equals("")) {
 			remarks.append(currentCanopy.remarks());
 			remarks.append(System.getProperty("line.separator"));
 		}
 		if (manufacturer.remarks() != null
 				&& !manufacturer.remarks().equals("")) {
 			remarks.append(manufacturer.remarks());
 			remarks.append(System.getProperty("line.separator"));
 		}
 		if (currentCanopy.addtionalInformationNeeded()) {
 			remarks.append(getString(R.string.detailsAdditionalInformationWelcome));
 			remarks.append(System.getProperty("line.separator"));
 		}
 		tvRemarks.setText(remarks.toString());
 
 		// TODO: move strings below to resource file (using string format?)
 		String geproduceerd = "";
 		if (currentCanopy.firstYearOfProduction != null
 				&& !currentCanopy.firstYearOfProduction.equals("")) {
 			geproduceerd = "vanaf " + currentCanopy.firstYearOfProduction;
 			if (currentCanopy.lastYearOfProduction != null
 					&& !currentCanopy.lastYearOfProduction.equals(""))
 				geproduceerd += " tot en met "
 						+ currentCanopy.lastYearOfProduction;
 		}
 		tvProduction.setText(geproduceerd);
 
 		// add on click handler to share button
 		ImageButton shareResultButton = (ImageButton) findViewById(R.id.buttonShareResult);
 		shareResultButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				shareDetails();
 			}
 
 		});
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_canopydetails, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_shareDetails:
 			shareDetails();
 			return true;
 		case R.id.menu_about:
 			startActivity(new Intent(this, AboutActivity.class));
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	private void shareDetails() {
 		Intent sendIntent = new Intent();
 		sendIntent.setAction(Intent.ACTION_SEND);
 		sendIntent.putExtra(Intent.EXTRA_TEXT,
 				CanopyDetailsActivity.skydiveKompasroosDetails(this));
 		sendIntent.putExtra(Intent.EXTRA_SUBJECT,
 				getString(R.string.sharedetailssubject));
 		sendIntent.putExtra(Intent.EXTRA_TITLE,
 				getString(R.string.sharedetailssubject));
 		sendIntent.setType("text/plain");
 		startActivity(sendIntent);
 
 	}
 
 	/***
 	 * Return the full string for the details to share.
 	 * 
 	 * @param context
 	 * @return
 	 */
 	static String skydiveKompasroosDetails(Context context) {
 		String nl = System.getProperty("line.separator");
 
 		StringBuilder details = new StringBuilder();
 
 		// first line: name (manufacturer, cat X)
 		details.append(currentCanopy.name);
 		details.append(" (");
 		details.append(currentCanopy.manufacturer + ", ");
 		details.append("categorie: " + Integer.toString(currentCanopy.category)
 				+ ")");
 		details.append(nl);
 
 		// second line: needed experience
 		details.append(context.getString(R.string.detailsExperience));
 		String[] neededExperience = context.getResources().getStringArray(
 				R.array.neededExperience);
 		details.append(" " + neededExperience[currentCanopy.category]);
 		details.append(nl);
 
 		// last line, url if available
 		HashMap<String, Manufacturer> manufacturers = Manufacturer
 				.getManufacturerHash(context);
 		Manufacturer manufacturer = manufacturers
 				.get(currentCanopy.manufacturer);
 		String url = manufacturer.url;
 		if (currentCanopy.url != null && !currentCanopy.url.equals(""))
 			url = currentCanopy.url; // if we have a canopy url use that.
 		if (url != null && !url.equals("")) {
 			details.append(url);
 			details.append(nl);
 		}
 
 		details.append(nl);
 		details.append(context.getString(R.string.shareresultfooter));
 
 		// return the result
 		return details.toString();
 	}
 
 }
