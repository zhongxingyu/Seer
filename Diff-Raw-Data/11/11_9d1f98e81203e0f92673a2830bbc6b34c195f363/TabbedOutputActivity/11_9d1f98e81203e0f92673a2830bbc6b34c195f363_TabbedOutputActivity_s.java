 package au.edu.qut.inn372.greenhat.activity;
 
 import java.io.FileOutputStream;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.Phrase;
 import com.itextpdf.text.pdf.PdfPCell;
 import com.itextpdf.text.pdf.PdfPTable;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.TabActivity;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import au.edu.qut.inn372.greenhat.bean.Calculation;
 import au.edu.qut.inn372.greenhat.bean.Calculator;
 import au.edu.qut.inn372.greenhat.mediator.CalculatorMediator;
 
 public class TabbedOutputActivity extends TabActivity {
 	
 	private TabHost tabHost;
 	public static final int SUMMARY_ID = 0;
 	public static final int POWER_GEN_ID = 1;
 	public static final int FINANCIAL_ID = 2;
 	public static final int SAVINGS_GRAPH_ID = 3;
 	public static final int COST_GRAPH_ID = 4;
 	public static final int MAX_TAB = COST_GRAPH_ID;
 	public static final int MIN_TAB = SUMMARY_ID;
 	
 	private int currentTab;
 	DecimalFormat df = new DecimalFormat("#.##");
 	// Constant for identifying the dialog
 	private static final int DIALOG_ALERT = 10;
 	private static final int DIALOG_FAILED = 11;
 	private static final int DIALOG_GENERATE_PDF = 12;
 	
 	private CalculatorMediator calcMediator; //only needed for saving when displaying a single calculator
 	private List<Calculator> calculatorList;
 	
 	/**
 	 * Constructor - sets up tabs
 	 */
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_tabbed_output);
 
         calculatorList = (ArrayList<Calculator>)getIntent().getSerializableExtra("Calculators");
         
         calcMediator = new CalculatorMediator(calculatorList.get(0)); //if there is one calcalator then the user is allowed to save the result
  
         tabHost = getTabHost();
         
         addTab("Summary", this, OutputSummaryActivity.class);
         addTab("Power Generation", this, PowerGeneration.class);
         addTab("Financial", this, FinancialOutputActivity.class);
         addTab("Savings Chart", this, SavingsGraphActivity.class);
         addTab("Cost Chart", this, CostGraphActivity.class);
         
         currentTab = MIN_TAB;
     }
 	
 	/**
 	 * Creates a new tab
 	 * @param tabName The name to be displayed for the tab
 	 * @param context The context (should be 'this')
 	 * @param newActivity The activity class to start (eg 'LocationActivity.class')
 	 */
 	private void addTab(String tabName, Context context, Class<?> newActivity) {
 		TabSpec newSpec = tabHost.newTabSpec(tabName);
 		newSpec.setIndicator(tabName);
 		Intent newIntent = new Intent(context, newActivity);
 		newSpec.setContent(newIntent);
 		tabHost.addTab(newSpec);
 	}
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_tabbed_output_menu, menu);
         if(calculatorList.size() > 1) {
         	//can't save if comparing two calculators
         	menu.removeItem(R.id.menu_tabbed_output_save);
         	menu.removeItem(R.id.menu_tabbed_output_save_template);
         }
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch (item.getItemId()) {
         	case R.id.menu_tabbed_output_save:
         		saveCalculations();
         		return true;
         	case R.id.menu_tabbed_output_save_template:
         		String oldName = calcMediator.getCalculator().getName();
         		String oldKey = calcMediator.getCalculator().getKey();
         		int oldStatus = calcMediator.getCalculator().getStatus();
         		calcMediator.getCalculator().setName(null);
         		calcMediator.getCalculator().setKey(null);
         		calcMediator.getCalculator().setStatus(2);//template status
         		saveCalculations();
         		calcMediator.getCalculator().setName(oldName);
         		calcMediator.getCalculator().setKey(oldKey);
         		calcMediator.getCalculator().setStatus(oldStatus);
         		return true;
         	case R.id.menu_tabbed_output_generate_pdf:
         		generatePDF();
         		return true;
         	default:
         		return super.onOptionsItemSelected(item);
     	}
     }
     
     private void saveCalculations() {
 		String result = calcMediator.saveCalculation();
 		if(result.equals("ok")) {
 			showDialog(DIALOG_ALERT);
 		}
 		else {
 			showDialog(DIALOG_FAILED);
 		}
     }
     
     private void generatePDF() {
     	exportPDF();
     }
     
 	/**
 	 * Customises the dialogs used in the Activity
 	 */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Builder builder;
 		AlertDialog dialog;
 		switch (id) {
 		case DIALOG_ALERT:
 			// Create out AlterDialog
 			builder = new AlertDialog.Builder(this);
 			builder.setMessage("Calculation Saved.");
 			builder.setCancelable(false);
 			builder.setPositiveButton("OK", new OkOnClickListener());
 			dialog = builder.create();
 			dialog.show();
 			break;
 		case DIALOG_FAILED:
 			builder = new AlertDialog.Builder(this);
 			builder.setMessage("Error saving calculation");
 			builder.setCancelable(false);
 			builder.setPositiveButton("OK", new OkOnClickListener());
 			dialog = builder.create();
 			dialog.show();
 			break;
 		case DIALOG_GENERATE_PDF:
 			builder = new AlertDialog.Builder(this);
 			builder.setMessage("SolarPowerReport.pdf created");
 			builder.setCancelable(false);
 			builder.setPositiveButton("OK", new OkOnClickListener());
 			dialog = builder.create();
 			dialog.show();
 			break;
 		}
 		return super.onCreateDialog(id);
 	}
 	
 	/**
 	 * handle on click button in the dialog
 	 * 
 	 */
 	private final class OkOnClickListener implements
 			DialogInterface.OnClickListener {
 		public void onClick(DialogInterface dialog, int which) {
 			dialog.dismiss();
 		}
 	}
 	
 	/**
 	 * Exports the financial calculation to a PDF
 	 * 
 	 * TODO: talk to customer to check which information he needs
 	 * in the report
 	 * 
 	 * @param view
 	 */
 	public void exportPDF(){
		/*
 		String FILE = "/SolarPowerReport.pdf";
 		try {
 			Document document = new Document();
 			boolean mExternalStorageAvailable = false;
 			boolean mExternalStorageWriteable = false;
 			String state = Environment.getExternalStorageState();
 			if (Environment.MEDIA_MOUNTED.equals(state)) {
 				// We can read and write the media
 				mExternalStorageAvailable = mExternalStorageWriteable = true;
 			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 				// We can only read the media
 				mExternalStorageAvailable = true;
 				mExternalStorageWriteable = false;
 			} else {
 				// Something else is wrong. It may be one of many other states, but all we need
 				//  to know is we can neither read nor write
 				mExternalStorageAvailable = mExternalStorageWriteable = false;
 			}
 			String file = null;
 			if(mExternalStorageWriteable) {
 				file = Environment.getExternalStorageDirectory().getPath() + FILE;
 			}
 
 			PdfWriter.getInstance(document,new FileOutputStream(file));
 			document.open();
 			Paragraph p = new Paragraph("Calculation Report");
 			document.add(p);
 			p = new Paragraph("  ");
 			document.add(p);
 			PdfPTable table = new PdfPTable(3);
 		    PdfPCell c1 = new PdfPCell(new Phrase("Year"));
 		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
 		    table.addCell(c1);
 
 		    c1 = new PdfPCell(new Phrase("Cumulative Savings ($)"));
 		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
 		    table.addCell(c1);
 
 		    c1 = new PdfPCell(new Phrase("ROI"));
 		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
 		    table.addCell(c1);
 		    table.setHeaderRows(1);
			for (Calculation curCalculation : calculator.getCalculations()) {
 			    table.addCell("" + df.format(curCalculation.getYear() + 1));
 			    table.addCell("" + df.format(curCalculation.getCumulativeSaving()));
			    table.addCell("" + df.format(curCalculation.getCumulativeSaving() / calculator.getEquipment().getCost()));
 			}
 			document.add(table);
 			
 			document.close();
 
 			showDialog(DIALOG_GENERATE_PDF);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
		*/
 	}
 	
 	/**
 	 * Returns the list of calculators being compared
 	 * @return
 	 */
 	public List<Calculator> getCalculators() {
 		return calculatorList;
 	}
 	
 	/**
 	 * Switches to the specified tab
 	 * @param tabID ID of the tab to be switched to - ID's are public fields for this class
 	 */
 	public void switchTab(int tabID) {
 		tabHost.setCurrentTab(tabID);
 		setTabId(tabID);
 	}
 	
 	public void setTabId(int tabID) {
 		currentTab = tabID;
 		correctButtonLabels();
 	}
 	
 	public void clickTabLeft(View view) {
 		if(currentTab > MIN_TAB) {
 			switchTab(currentTab-1);
 		}
 	}
 	
 	public void clickTabRight(View view) {
 		if(currentTab < MAX_TAB) {
 			switchTab(currentTab+1);
 		}
 	}
 	
 	private void correctButtonLabels() {
 		Button leftButton = (Button)findViewById(R.id.outputButtonLeft);
 		Button rightButton = (Button)findViewById(R.id.outputButtonRight);
 		if(currentTab>MIN_TAB) {
 			leftButton.setText(R.string.tab_left);
 		}
 		else {
 			leftButton.setText("");
 		}
 		if(currentTab<MAX_TAB) {
 			rightButton.setText(R.string.tab_right);
 		}
 		else {
 			rightButton.setText("");
 		}
 	}
 	
 	@Override
 	public void onBackPressed() {
 		//if the user has saved a key, send it back to the input activity
 		Intent returnIntent = new Intent();
 		if(calcMediator.getCalculator().getKey() != null) {
 			returnIntent.putExtra("key", calcMediator.getCalculator().getKey());
 			setResult(1, returnIntent);
 		}
 		else {
 			setResult(0, returnIntent);
 		}
 		finish();
 	}
 }
