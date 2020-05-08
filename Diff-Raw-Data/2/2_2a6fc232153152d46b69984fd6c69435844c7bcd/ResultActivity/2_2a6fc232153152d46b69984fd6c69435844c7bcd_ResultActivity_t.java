 package uk.co.huydinh.apps.incomecalculator;
 
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.TabActivity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.content.res.XmlResourceParser;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class ResultActivity extends TabActivity {
 	public static final String PAYMENT_TERM = "payment_term";
 	public static final String AMOUNT = "annual_salary";
 	public static final String DAYS_PER_WEEK = "days_per_weeki";
 	public static final String HOURS_PER_DAY = "hours_per_day";
 	public static final String HOLIDAYS = "holidays";
 	public static final String REPAY_STUDENT_LOAN = "repay_student_loan";
 
 	private Locale locale;
 	private ArrayList<TaxData> taxDatas;
 	private String defaultYear;
 	private TabHost tabHost;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		final Resources resources = getBaseContext().getResources();
 		SharedPreferences appPreferences = PreferenceManager
 				.getDefaultSharedPreferences(this);
 		String language = appPreferences.getString("language",
 				resources.getStringArray(R.array.languages_values)[0]);
 		Locale locale;
 		if (language.length() == 2) {
 			locale = new Locale(language);
 		} else {
 			locale = new Locale(language.substring(0, 2), language.substring(3));
 		}
 		Configuration config = getBaseContext().getResources().getConfiguration();
 		if (!config.locale.getLanguage().equals(locale.getLanguage())
 				|| !config.locale.getCountry().equals(locale.getCountry())) {
 			Locale.setDefault(locale);
 			config.locale = locale;
 			resources
 					.updateConfiguration(config, resources.getDisplayMetrics());
 			this.locale = locale;
 		}
 
 		setContentView(R.layout.result);
 
 		this.taxDatas = new ArrayList<TaxData>();
 
 		XmlResourceParser p = getResources().getXml(R.xml.tax_data);
 
 		try {
 			p.next();
 			int eventType = p.getEventType();
 
 			while (eventType != XmlPullParser.END_DOCUMENT) {
 				p.next();
 
 				eventType = p.getEventType();
 				if (eventType == XmlPullParser.START_TAG) {
 					String name = p.getName();
 					if (name.equalsIgnoreCase("default")) {
 						if (defaultYear == null) {
 							defaultYear = p.getAttributeValue(null, "year");
 						}
 					} else if (name.equalsIgnoreCase("year")) {
 						this.taxDatas.add(parseTaxData(p));
 					}
 				}
 			}
 		} catch (XmlPullParserException e) {
 			Log.i("XmlPullParserException", e.toString());
 		} catch (IOException e) {
 			Log.i("IOException", e.toString());
 		} finally {
 			p.close();
 		}
 
 		Bundle extras = getIntent().getExtras();
 		if (extras != null) {
 			tabHost = getTabHost();
 
 			// Retrieves data from extras bundle
 			String paymentTerm = extras.getString(PAYMENT_TERM);
 			float amount = extras.getFloat(AMOUNT);
 			int daysPerWeek = extras.getInt(DAYS_PER_WEEK);
 			float hoursPerDay = extras.getFloat(HOURS_PER_DAY);
			float holidays = extras.getFloat(HOLIDAYS);
 			boolean repayStudentLoan = extras.getBoolean(REPAY_STUDENT_LOAN);
 
 			// prepares resources
 			// final Resources resources = getResources();
 			final String[] paymentTerms = resources
 					.getStringArray(R.array.payment_terms);
 
 			// calculate time worked
 			final float workingDaysPerYear = (daysPerWeek * IncomeService.WEEKS_PER_YEAR)
 					- holidays;
 			final float workingHoursPerYear = workingDaysPerYear * hoursPerDay;
 
 			// calculate annual salary
 			float annualSalary;
 			if (paymentTerm.equals(paymentTerms[1])) {
 				annualSalary = amount * IncomeService.MONTHS_PER_YEAR;
 			} else if (paymentTerm.equals(paymentTerms[2])) {
 				annualSalary = amount * IncomeService.WEEKS_PER_YEAR;
 			} else if (paymentTerm.equals(paymentTerms[3])) {
 				annualSalary = amount * workingDaysPerYear;
 			} else if (paymentTerm.equals(paymentTerms[4])) {
 				annualSalary = amount * workingHoursPerYear;
 			} else {
 				annualSalary = amount;
 			}
 
 			// calculate taxes
 			for (TaxData td : this.taxDatas) {
 				// feed data into the calculator
 				final IncomeService is = new IncomeService(td, annualSalary,
 						repayStudentLoan);
 
 				// create a tab for each tax data (each tax data is associated
 				// with a period - usually a year)
 				TabSpec tab = tabHost.newTabSpec(td.label)
 						.setIndicator(td.label)
 						.setContent(new TabHost.TabContentFactory() {
 
 							// retrieves the formatting for currency
 							private NumberFormat formatter = new DecimalFormat(
 									resources
 											.getString(R.string.currency_format));
 
 							/**
 							 * Creates a TextView to be the table cell
 							 * 
 							 * @param label
 							 *            Text for the cell
 							 * @return TextView
 							 */
 							private TextView createTableCell(String label) {
 								TextView tv = new TextView(ResultActivity.this);
 								tv.setText(label);
 								return tv;
 							}
 
 							/**
 							 * Cerates a TextView to be the table cell with text
 							 * alignment
 							 * 
 							 * @param label
 							 *            Text for the cell
 							 * @param gravity
 							 *            Text alignment gravity
 							 * @return TextView
 							 */
 							private TextView createTextView(String label,
 									int gravity) {
 								TextView tv = createTableCell(label);
 								tv.setGravity(gravity);
 								return tv;
 							}
 
 							/**
 							 * Creates a TextView to be the table cell for a
 							 * monetary value - the text is right aligned
 							 * 
 							 * @param amount
 							 *            Number to display
 							 * @return TextView
 							 */
 							private TextView createTextView(float amount) {
 								return this.createTextView(
 										formatter.format(amount), Gravity.RIGHT);
 							}
 
 							/**
 							 * Creates a TableRow with predetermined number of
 							 * columns
 							 * 
 							 * @param label
 							 *            Heading columm's label
 							 * @param amount
 							 *            Annual amount
 							 * @return TableRow
 							 */
 							private TableRow createRow(String label,
 									float amount) {
 								TableRow row = new TableRow(ResultActivity.this);
 								row.addView(createTableCell(label));
 								row.addView(createTextView(amount));
 								row.addView(createTextView(amount
 										/ IncomeService.MONTHS_PER_YEAR));
 								row.addView(createTextView(amount
 										/ IncomeService.WEEKS_PER_YEAR));
 								row.addView(createTextView(amount
 										/ workingDaysPerYear));
 								row.addView(createTextView(amount
 										/ workingHoursPerYear));
 								return row;
 							}
 
 							/**
 							 * Create the tab's content
 							 * 
 							 * @param tag
 							 *            Tab's label
 							 * @return View
 							 */
 							public View createTabContent(String tag) {
 								// Inflates the view
 								LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 								View view = inflater.inflate(
 										R.layout.result_table, null);
 
 								// Find the TableLayout
 								TableLayout table = (TableLayout) view
 										.findViewById(R.id.result_table);
 								table.setStretchAllColumns(true);
 
 								// Create a header row
 								TableRow row = new TableRow(ResultActivity.this);
 								row.addView(createTableCell(""));
 								for (int i = 0, j = paymentTerms.length; i < j; i++) {
 									row.addView(createTextView(paymentTerms[i],
 											Gravity.RIGHT));
 								}
 								table.addView(row);
 
 								// Create the rest of the rows
 								table.addView(createRow(resources
 										.getString(R.string.gross_income),
 										is.grossSalary));
 								table.addView(createRow(resources
 										.getString(R.string.income_tax), is
 										.getIncomeTax()));
 								table.addView(createRow(
 										resources
 												.getString(R.string.national_insurance),
 										is.getNationalInsurance()));
 								if (is.repayStudentLoan) {
 									table.addView(createRow(resources
 											.getString(R.string.student_loan),
 											is.getStudentLoan()));
 								}
 								table.addView(createRow(resources
 										.getString(R.string.total_deductions),
 										is.getTotalDeductions()));
 								table.addView(createRow(resources
 										.getString(R.string.net_income), is
 										.getNetSalary()));
 
 								return view;
 							}
 						});
 				tabHost.addTab(tab);
 			}
 		}
 
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		if (locale != null) {
 			newConfig.locale = locale;
 			Locale.setDefault(locale);
 			getBaseContext().getResources().updateConfiguration(newConfig,
 					getBaseContext().getResources().getDisplayMetrics());
 		}
 	}
 
 	/**
 	 * Parses tax data from xml resource
 	 * 
 	 * @param p
 	 *            An XmlResourceParser object
 	 * @return TaxData object
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private TaxData parseTaxData(XmlResourceParser p)
 			throws XmlPullParserException, IOException {
 		int depth = p.getDepth();
 		String label = p.getAttributeValue(null, "label");
 		p.next();
 		ArrayList<TaxBand> incomeTaxBands = new ArrayList<TaxBand>();
 		ArrayList<TaxBand> niBands = new ArrayList<TaxBand>();
 		while (depth < p.getDepth()) {
 			String name = p.getName();
 			if (name.equalsIgnoreCase("incometax")) {
 				incomeTaxBands = parseTaxBands(p);
 			} else if (name.equalsIgnoreCase("ni")) {
 				niBands = parseTaxBands(p);
 			}
 			p.next();
 		}
 
 		TaxBand[] it = new TaxBand[incomeTaxBands.size()];
 		it = incomeTaxBands.toArray(it);
 		TaxBand[] ni = new TaxBand[niBands.size()];
 		ni = niBands.toArray(ni);
 		return new TaxData(label, it, ni);
 	}
 
 	/**
 	 * Parses tax band datas from xml resource (to be stored in the TaxData
 	 * object)
 	 * 
 	 * @param p
 	 *            An XmlResourceParser object
 	 * @return All TaxBands in an ArrayList
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private ArrayList<TaxBand> parseTaxBands(XmlResourceParser p)
 			throws XmlPullParserException, IOException {
 		ArrayList<TaxBand> taxBands = new ArrayList<TaxBand>();
 		int depth = p.getDepth();
 		p.next();
 		while (depth < p.getDepth()) {
 			taxBands.add(parseTaxBand(p));
 			p.next();
 		}
 		return taxBands;
 	}
 
 	/**
 	 * Parses a single tax band data from xml resource (to be stored in an
 	 * ArrayList)
 	 * 
 	 * @param p
 	 *            An XmlResourceParser object
 	 * @return TaxBand object
 	 * @throws XmlPullParserException
 	 * @throws IOException
 	 */
 	private TaxBand parseTaxBand(XmlResourceParser p)
 			throws XmlPullParserException, IOException {
 		// Parse a float (or an int as a float)
 		float rate;
 		try {
 			rate = p.getAttributeFloatValue(null, "rate", 0.0f);
 		} catch (RuntimeException e) {
 			rate = p.getAttributeIntValue(null, "rate", 0);
 		}
 		int limit = p.getAttributeIntValue(null, "limit", 0);
 
 		// Advances to the end tag before returning
 		p.next();
 
 		return new TaxBand(rate, limit);
 	}
 }
