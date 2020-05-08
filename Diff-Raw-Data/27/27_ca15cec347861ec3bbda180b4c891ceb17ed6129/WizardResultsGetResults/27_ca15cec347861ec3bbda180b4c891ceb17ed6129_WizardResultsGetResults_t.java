 package com.anonymous.solar.android;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.text.Html;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TabHost;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TableRow.LayoutParams;
 import android.widget.TextView;
 
 import com.anonymous.solar.client.SolarCalculatorService;
 import com.anonymous.solar.shared.ResultsDetails;
 import com.anonymous.solar.shared.SolarResult;
 import com.anonymous.solar.shared.SolarSetup;
 
 public class WizardResultsGetResults extends AsyncTask<SolarSetup, Void, SolarResult> {
 
 	private MainActivity parent;
 	private ProgressDialog progressDialog;
 	private ArrayList<View> tabs;
 	final private static int resultTableHeaderColor = 0xffcccccc;
 
 	public WizardResultsGetResults(MainActivity parent, ArrayList<View> tabs, ProgressDialog progressDialog) {
 		this.parent = parent;
 		// progressDialog = new ProgressDialog(parent);
 		this.progressDialog = progressDialog;
 		this.tabs = tabs;
 	}
 
 	protected void onPreExecute() {
 	}
 
 	@Override
 	protected SolarResult doInBackground(SolarSetup... params) {
 		return new SolarCalculatorService().calculateAllResults(parent, params[0]);
 	}
 
 	protected void onPostExecute(SolarResult soapResult) {
 		if (progressDialog.isShowing()) {
 			progressDialog.dismiss();
 		}
 		parent.setSolarResult(soapResult);
 		displayResults(soapResult, true);
 	}
 
 	/**
 	 * Update the results display area.
 	 * 
 	 * @param soapResult
 	 * @param storeResults
 	 */
 	public void displayResults(SolarResult soapResult, boolean storeResults) {
 		TextView summary = (TextView) parent.findViewById(R.id.textViewResultsSummaryText);
 
 		if (soapResult == null) {
 			summary.setText("Error occurred with result service. Please try again.");
 			return;
 		} else {
 			// Add our data to the summary view
 			summary.setText(Html.fromHtml(soapResult.toString2() + "<br />" + soapResult.getSolarSetup().toString()));
 			// Add our data to the graph
 			LinearLayout tabGraph = (LinearLayout) tabs.get(1).findViewById(R.id.layoutResultsGraph);
 			tabGraph.removeAllViews(); // remove old graphs
 			ArrayList<List<Double>> list = new ArrayList<List<Double>>();
 			ArrayList<Double> ROI = new ArrayList<Double>();
 			List<ResultsDetails> resultSet = soapResult.getResultsDetailsList();
 			int numberOfYears = resultSet.size() / 12;
 			for(int i = 0 ; i< numberOfYears; i++){
 				ROI.add(resultSet.get(i * 12).getROI());
 			}
 			list.add(ROI);
 			ArrayList<String> columns = new ArrayList<String>();
 			columns.add("ROI");
 			tabGraph.addView(new WizardResultGraph(parent, "Return on Investment", "Year", list, columns));
 			// Add our data to the table.
 			TableLayout tabResults = (TableLayout) tabs.get(2).findViewById(R.id.tableResults);
 			createTable(tabResults, soapResult.getResultsDetailsList(), soapResult.getSolarSetup());
 
 			// Store our result set.
 			if (storeResults) {
 				DeviceLocalStorage database = new DeviceLocalStorage(parent, parent);
 				database.open();
 				database.createResult(soapResult);
 				database.close();
 			}
 
 			// Add our data to the comparison view.
 			LinearLayout tabComparisonGraph = (LinearLayout) tabs.get(3).findViewById(R.id.layoutResultsComparison);
 			tabComparisonGraph.removeAllViews(); // remove old comparisons
 			tabComparisonGraph.addView(new WizardResultsComparison(parent, soapResult, tabComparisonGraph));
 
 		}
 		TabHost resultsTabHost = (TabHost) parent.findViewById(R.id.tabHostResults);
 		resultsTabHost.setEnabled(true);
 	}
 
 	/**
 	 * Create a table in the specified table layout.
 	 * 
 	 * @param tabResults
 	 * @param list
 	 */
 	private void createTable(TableLayout tabResults, List<ResultsDetails> resultSet, SolarSetup setup) {
 		// Clear the table.
 		tabResults.removeAllViews();
 
 		// Generate the header row.
 		TableRow row = new TableRow(parent);
 		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		params.setMargins(1, 1, 1, 1);
 
 		// Add our header columns (Year)
 		int numberOfYears = resultSet.size() / 12;
 
 		row.addView(getHeader(""));
 		for (int i = 0; i < numberOfYears; i++) {
			row.addView(getHeader(String.format("Year %d", i)));
 		}
 		// Add our row to the table.
 		// row.setPadding(3, 3, 3, 3);
 		tabResults.addView(row);
 
 		// Fill in the data row.
 		TableRow row8 = new TableRow(parent);
 
 		// Add our data
 		row8.addView(getHeader("Old Bill (p/Mth)"));
 		for (int i = 0; i < numberOfYears; i++) {
 			TextView header = new TextView(parent);
 			// Determine the ROI over
 			header.setText(String.format("$%,.2f", setup.getCustomerData().getMonthlyAverageUsage() * setup.getCustomerData().getTariff11Fee()/100.00));
 			header.setGravity(Gravity.CENTER);
 			header.setTextAppearance(parent, android.R.style.TextAppearance_Medium);
 			header.setPadding(10, 10, 10, 10);
 			row8.addView(header);
 		}
 		// Add our row to the table.
 		// row2.setPadding(3, 3, 3, 3);
 		tabResults.addView(row8);
 
 		// Fill in the data row.
 		TableRow row9 = new TableRow(parent);
 
 		// Add our data
 		row9.addView(getHeader("Expected Bill (p/Mth)"));
 		for (int i = 0; i < numberOfYears; i++) {
 			TextView header = new TextView(parent);
 			// Determine the ROI over
 			header.setText(String.format("$%,.2f", resultSet.get(i * 12).getExpectedUtilityBill()));
 			header.setGravity(Gravity.CENTER);
 			header.setTextAppearance(parent, android.R.style.TextAppearance_Medium);
 			header.setPadding(10, 10, 10, 10);
 			row9.addView(header);
 		}
 		// Add our row to the table.
 		// row2.setPadding(3, 3, 3, 3);
 		tabResults.addView(row9);
 		
 		// Fill in the data row.
 				TableRow row10 = new TableRow(parent);
 
 				// Add our data
 				row10.addView(getHeader("Savings (p/Mth)"));
 				for (int i = 0; i < numberOfYears; i++) {
 					TextView header = new TextView(parent);
 					// Determine the ROI over
 					header.setText(String.format("$%,.2f",  (setup.getCustomerData().getMonthlyAverageUsage() * setup.getCustomerData().getTariff11Fee()/100.00) - resultSet.get(i * 12).getExpectedUtilityBill()));
 					header.setGravity(Gravity.CENTER);
 					header.setTextAppearance(parent, android.R.style.TextAppearance_Medium);
 					header.setPadding(10, 10, 10, 10);
 					row10.addView(header);
 				}
 				// Add our row to the table.
 				// row2.setPadding(3, 3, 3, 3);
 				tabResults.addView(row10);
 
 		// Fill in the data row.
 		TableRow row2 = new TableRow(parent);
 
 		// Add our data
 		row2.addView(getHeader("ROI"));
 		for (int i = 0; i < numberOfYears; i++) {
 			TextView header = new TextView(parent);
 			// Determine the ROI over
 			header.setText(String.format("$%,.2f", resultSet.get(i * 12).getROI()));
 			header.setGravity(Gravity.CENTER);
 			header.setTextAppearance(parent, android.R.style.TextAppearance_Medium);
 			header.setPadding(10, 10, 10, 10);
 			row2.addView(header);
 		}
 		// Add our row to the table.
 		// row2.setPadding(3, 3, 3, 3);
 		tabResults.addView(row2);
 
 		// Fill in the data row.
 		TableRow row7 = new TableRow(parent);
 
 		// Add our data
 		row7.addView(getHeader("Income Generated (p/Mth)"));
 		for (int i = 0; i < numberOfYears; i++) {
 			TextView header = new TextView(parent);
 			// Determine the ROI over
 			header.setText(String.format("$%,.2f", resultSet.get(i * 12).getIncome()));
 			header.setGravity(Gravity.CENTER);
 			header.setTextAppearance(parent, android.R.style.TextAppearance_Medium);
 			header.setPadding(10, 10, 10, 10);
 			row7.addView(header);
 		}
 		// Add our row to the table.
 		// row2.setPadding(3, 3, 3, 3);
 		tabResults.addView(row7);
 
 		// Fill in the data row.
 		TableRow row3 = new TableRow(parent);
 		row3.addView(getHeader("Power Generated (p/Yr)"));
 		for (int i = 0; i < numberOfYears; i++) {
 			TextView header = new TextView(parent);
 			// Determine the ROI over
 			header.setText(String.format("%,.2fkW", resultSet.get(i * 12).getPowerGenerated() / 1000.00 * 12.00));
 			header.setGravity(Gravity.CENTER);
 			header.setTextAppearance(parent, android.R.style.TextAppearance_Medium);
 			header.setPadding(10, 10, 10, 10);
 			row3.addView(header);
 		}
 		// Add our row to the table.
 		// row3.setPadding(3, 3, 3, 3);
 
 		tabResults.addView(row3);
 
 		// Fill in the data row.
 		TableRow row4 = new TableRow(parent);
 		row4.addView(getHeader("Panel Efficiency"));
 		for (int i = 0; i < numberOfYears; i++) {
 			TextView header = new TextView(parent);
 			header.setText(String.format("%,.2f%%", resultSet.get(i * 12).getSolarBanksEfficencyList().get(0)));
 			header.setGravity(Gravity.CENTER);
 			header.setTextAppearance(parent, android.R.style.TextAppearance_Medium);
 			header.setPadding(10, 10, 10, 10);
 			row4.addView(header);
 		}
 		// Add our row to the table.
 		// row4.setPadding(3, 3, 3, 3);
 
 		tabResults.addView(row4);
 
 		// Fill in the data row.
 		TableRow row5 = new TableRow(parent);
 		row5.addView(getHeader("Inverter Output"));
 		for (int i = 0; i < numberOfYears; i++) {
 			TextView header = new TextView(parent);
 			header.setText(String.format("%,.2fW", resultSet.get(i * 12).getinverterOutput()));
 			header.setGravity(Gravity.CENTER);
 			header.setTextAppearance(parent, android.R.style.TextAppearance_Medium);
 			header.setPadding(10, 10, 10, 10);
 			row5.addView(header);
 		}
 		// Add our row to the table.
 		// row5.setPadding(3, 3, 3, 3);
 
 		tabResults.addView(row5);
 
 		// Fill in the data row.
 		TableRow row6 = new TableRow(parent);
 		row6.addView(getHeader("Inverter Efficiency"));
 		for (int i = 0; i < numberOfYears; i++) {
 			TextView header = new TextView(parent);
 			header.setText(String.format("%,.2f%%", resultSet.get(i * 12).getInverterEfficiency()));
 			header.setGravity(Gravity.CENTER);
 			header.setTextAppearance(parent, android.R.style.TextAppearance_Medium);
 			header.setPadding(10, 10, 10, 10);
 			row6.addView(header);
 		}
 		// Add our row to the table.
 		// row6.setPadding(3, 3, 3, 3);
 
 		tabResults.addView(row6);
 	}
 
 	/**
 	 * Create a header cell, with correct highlights
 	 * 
 	 * @param text
 	 * @return
 	 */
 	private TextView getHeader(String text) {
 		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 		params.setMargins(1, 1, 1, 1);
 		TextView rowHeader = new TextView(parent);
 		rowHeader.setText(text);
 		rowHeader.setGravity(Gravity.CENTER);
 		rowHeader.setTextAppearance(parent, android.R.style.TextAppearance_Medium);
 		rowHeader.setPadding(5, 1, 5, 1);
 		rowHeader.setLayoutParams(params);
 		rowHeader.setBackgroundColor(resultTableHeaderColor);
 		rowHeader.setTextColor(parent.getResources().getColor(android.R.color.black));
 		return rowHeader;
 	}
 
 }
