 package nl.digitalica.skydivekompasroos;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import nl.digitalica.skydivekompasroos.CanopyBase.AcceptabilityEnum;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class CanopyDetailsActivity extends KompasroosBaseActivity {
 
 	static CanopyType currentCanopy;
 	static Manufacturer currentManufacturer;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_canopydetails);
 
 		Bundle extras = getIntent().getExtras();
 		UUID canopyId = UUID.fromString(extras.getString(CANOPYIDEXTRA));
 		HashMap<UUID, Manufacturer> manufacturers = Manufacturer
 				.getManufacturerHash(CanopyDetailsActivity.this);
 
 		currentCanopy = CanopyType.getCanopy(canopyId, this);
 		currentManufacturer = manufacturers.get(currentCanopy.manufacturerId);
 
 		String url = currentManufacturer.url;
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
 
 		AcceptabilityEnum acceptability = currentCanopy.acceptablility(
 				currentMaxCategory, currentWeight);
 
 		tvName.setText(currentCanopy.name);
 		tvName.setBackgroundDrawable(backgroundDrawableForAcceptance(acceptability));
 
		String categoryText = Integer.toString(currentCanopy
				.calculationCategory());
 		if (currentCanopy.isCategoryUnknown())
			categoryText = String.format(getString(R.string.categoryUnknown),
					currentCanopy.calculationCategory());
 		tvCategory.setText(categoryText);
 		String advice = "";
 		int category = currentCanopy.calculationCategory();
 		switch (acceptability) {
 		case ACCEPTABLE:
			if (currentMinArea == 0)
				advice = getString(R.string.canopyAdviseAcceptable);
			else
				advice = String.format(
						getString(R.string.canopyAdviseAcceptableWithMinSize),
						currentMinArea);
 			break;
 		case NEEDEDSIZENOTAVAILABLE:
 			advice = String.format(
 					getString(R.string.canopyAdviseNeededSizeNotAvailable),
 					currentMinArea);
 			break;
 		case CATEGORYTOOHIGH:
 			int extraNeededJumsThis12Months = Calculation.MINIMUMJUMPSLAST12MONTHS[category]
 					- currentJumpsLast12Months;
 			int extraNeededTotalJumps = Calculation.MINIMUMTOTALJUMPS[category]
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
 		tvExperience.setText(neededExperience[category]);
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
 		tvManufacturer.setText(currentManufacturer.name);
 		tvManufacturerCountry.setText(currentManufacturer.countryFullName());
 		tvDropzoneId.setText(currentCanopy.dropZoneUrl());
 		StringBuilder remarks = new StringBuilder();
 		if (currentCanopy.remarks() != null
 				&& !currentCanopy.remarks().equals("")) {
 			remarks.append(currentCanopy.remarks());
 			remarks.append(System.getProperty("line.separator"));
 		}
 		if (currentManufacturer.remarks() != null
 				&& !currentManufacturer.remarks().equals("")) {
 			remarks.append(currentManufacturer.remarks());
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
 	 * @param c
 	 * @return
 	 */
 	static String skydiveKompasroosDetails(Context c) {
 		String nl = System.getProperty("line.separator");
 
 		StringBuilder details = new StringBuilder();
 
 		// first line: name (manufacturer, cat X)
 		details.append(currentCanopy.name);
 		details.append(" (");
 		details.append(currentManufacturer.name + ", ");
		details.append("categorie: "
				+ Integer.toString(currentCanopy.calculationCategory()) + ")");
 		details.append(nl);
 
 		// second line: needed experience
 		details.append(c.getString(R.string.detailsExperience));
 		String[] neededExperience = c.getResources().getStringArray(
 				R.array.neededExperience);
		details.append(" "
				+ neededExperience[currentCanopy.calculationCategory()]);
 		details.append(nl);
 
 		// last line, url if available
 		// TODO: refactor. this code is in the details textview as well
 		String url = currentManufacturer.url;
 		if (currentCanopy.url != null && !currentCanopy.url.equals(""))
 			url = currentCanopy.url; // if we have a canopy url use that.
 		if (url != null && !url.equals("")) {
 			details.append(url);
 			details.append(nl);
 		}
 
 		details.append(nl);
 		details.append(c.getString(R.string.shareresultfooter));
 
 		// return the result
 		return details.toString();
 	}
 
 }
