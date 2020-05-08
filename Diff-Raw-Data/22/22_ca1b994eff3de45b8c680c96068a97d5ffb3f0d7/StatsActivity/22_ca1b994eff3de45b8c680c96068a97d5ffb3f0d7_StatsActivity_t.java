 package budgetapp.main;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import budgetapp.util.BudgetAdapter;
 import budgetapp.util.BudgetEntry;
 import budgetapp.util.CategoryEntry;
 import budgetapp.util.DayEntry;
 import budgetapp.util.Money;
 import budgetapp.util.ViewHolder;
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.text.method.ScrollingMovementMethod;
 import android.text.util.Linkify;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnLongClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemSelectedListener;
 import budgetapp.util.BudgetFunctions;
 import budgetapp.util.database.BudgetDataSource;
 import budgetapp.util.stats.*;
 public class StatsActivity extends FragmentActivity implements OnItemSelectedListener, OnLongClickListener{
 	
 	BudgetDataSource datasource = MainActivity.datasource;
 	List<DayEntry> days;
 	List<DayEntry> dayFlow;
 	List<BudgetEntry> entries;
 	List<CategoryEntry> categories;
 	List<CategoryEntry> categoryStats; // Contains stats for categories
 	List<String> categoryNames;
 	ListView top;
 	BudgetAdapter ad;
 	ArrayList<BudgetEntry> allEntries = new ArrayList<BudgetEntry>();
 	ArrayList<CompositeStats> years;
 	int selectedYear=-1;
 	int selectedMonth=-1;
 	String selectedCategory = "";
 	int numDaysForDerivative = 30;
 	int numTransactionsForDerivative = 10;
 	ViewHolder selectedViewHolder;
 	int selectedViewHolderIndex;
 	
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_stats);
         top = (ListView)findViewById(R.id.ListViewLogTop);
        // ad = new ArrayAdapter<BudgetEntry>(top.getContext(), R.layout.stats_listitem);
         ad = new BudgetAdapter(this);
         // Read in all the data
         entries = datasource.getAllTransactions(BudgetDataSource.DESCENDING);
         days = datasource.getAllDaysTotal(BudgetDataSource.DESCENDING);
         dayFlow = datasource.getAllDays(BudgetDataSource.DESCENDING);
         categories = datasource.getCategoriesSorted();
         
         
         categoryNames = new ArrayList<String>();
         for(int i=0;i<categories.size();i++)
         	categoryNames.add(categories.get(i).getCategory());
         
         
 
         int size = entries.size();
         
         if(size>0)
         {
 	        
 	        readEntries();
 	        
 	        Spinner spinner = (Spinner) findViewById(R.id.spinnerMonth);
 	        spinner.setOnItemSelectedListener(this);
 	        spinner = (Spinner) findViewById(R.id.spinnerCategory);
 	        spinner.setOnItemSelectedListener(this);
 	        spinner = (Spinner) findViewById(R.id.spinnerYear);
 	        spinner.setOnItemSelectedListener(this);
 	
 	       	updateSpinners();
 	       	spinner = (Spinner) findViewById(R.id.spinnerMonth);
 	       	
 	        updateLog();
         }
         
         
 	}
 	
 	public void readEntries()
 	{
 		years = new ArrayList<CompositeStats>();
         BudgetEntry entry;
         int entryIndex = 0;
         int yearIndex = -1;
 		// Set up the composite
         while(entryIndex<entries.size())
         {
         	entry = entries.get(entryIndex);
 
 
         	String year = entry.getYear();
         	if(years.isEmpty() || !years.get(yearIndex).getName().equalsIgnoreCase(year))
         	{
         		
 	        	//Add a year
 	        	years.add(new CompositeStats(year));
 	        	yearIndex++;
         	}
         	years.get(yearIndex).addEntry(entry,CompositeStats.MONTH);
         	
         	entryIndex++;
         }
 	}
 	/**
 	 * Updates the stats TextView
 	 */
 	public void updateStats()
 	{
 		TextView stats = (TextView)findViewById(R.id.textViewLogStats);
 		
 		stats.setMovementMethod(new ScrollingMovementMethod());
 		if(days.size()<2)
 		{
 			stats.setText("Not enough days for mean derivative\n");
 		}
 		else
 		{
 			stats.setText("Mean derivative (" + BudgetFunctions.min(numDaysForDerivative,dayFlow.size()) + ") days: ");
 			Money dayDerivative = BudgetFunctions.getWeightedMeanDerivative(dayFlow,numDaysForDerivative);
 			stats.append(""+dayDerivative + "\n");
 		}
 		stats.append(Html.fromHtml("<b>Category statistics</b><br />"));
 		CategoryEntry entry;
 		int length = 0;
 		for(int i=0;i<categoryStats.size();i++) // Get longest name
 		{
 			entry = categoryStats.get(i);
 			if(entry.getCategory().length()>length)
 			{
 				length = entry.getCategory().length();
 			}
 		}
 		length++;
 		Money sum = new Money(); // Calculate the total cash flow for the selected time span
 		for(int i=0;i<categoryStats.size();i++)
 		{
 			entry = categoryStats.get(i);
 			
 			stats.append(entry.getCategory()+":");
 			for(int j=0;j<Math.floor((length-entry.getCategory().length()+1)/3)+1;j++) // Fancy pancy formatting
 				stats.append("\t");
 			stats.append(""+entry.getNum());
 			stats.append("\t");
 			if(entry.getNum()<10)
 				stats.append("\t");
 			stats.append(" Total: " + entry.getTotal() + "\n");
 			sum = sum.add(entry.getTotal());
 			
 		}
 		
 		stats.append("Total cash flow: " + sum);
 		
 	}
 	
 	
 	public void updateSpinners()
 	{
		categories = datasource.getCategoriesSorted();
        
		categoryNames = new ArrayList<String>();
        for(int i=0;i<categories.size();i++)
        	categoryNames.add(categories.get(i).getCategory());
        
 		 //Set up the year spinner
 		ArrayList<String> yearStartValues = new ArrayList<String>();
 		
         ArrayList<String> categoryStartValues = new ArrayList<String>();
         
 		//yearStartValues.add(getString(R.string.all_years));
         for(int i=0;i<years.size();i++)
         {
         	yearStartValues.add(years.get(i).getName());
         }
         Spinner spinner = (Spinner) findViewById(R.id.spinnerYear);
 		 // Create an ArrayAdapter using the string array and a default spinner layout
          ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, yearStartValues);
 	   
 	     // Specify the layout to use when the list of choices appears
 		 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		 // Apply the adapter to the spinner
 		 spinner.setAdapter(adapter);
 		 
 		 
 		 updateMonthSpinner();
 		 // Set up the category spinner
 		 categoryStartValues.add(getString(R.string.all_categories));
 	     for(int i=0;i<categoryNames.size();i++)
 	     {
 	    	 categoryStartValues.add(categoryNames.get(i));
 	     }
          spinner = (Spinner) findViewById(R.id.spinnerCategory);
 		 // Create an ArrayAdapter using the string array and a default spinner layout
          adapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, categoryStartValues);
 	   
 	     // Specify the layout to use when the list of choices appears
 		 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		 // Apply the adapter to the spinner
 		 spinner.setAdapter(adapter);
 			 
 			 
 	}
 	
 	public void updateMonthSpinner()
 	{
 		//Set up the month spinner
 		ArrayList<String> monthStartValues = new ArrayList<String>();
         monthStartValues.add(getString(R.string.all_months));
         
        
         if(selectedYear>-1)
         {
         	ArrayList<Stats> months = new ArrayList<Stats>();
         	months = (ArrayList<Stats>) years.get(selectedYear).getChildren();
         	
         	for(int i=0;i<months.size();i++)
         	{
         		monthStartValues.add(monthToString(months.get(i).getName()));
         	}
         }
         else
         {
         	monthStartValues.clear();
         }
         	
         
         Spinner spinner = (Spinner) findViewById(R.id.spinnerMonth);
 		 // Create an ArrayAdapter using the string array and a default spinner layout
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, monthStartValues);
 	   
 	     // Specify the layout to use when the list of choices appears
 		 adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		 // Apply the adapter to the spinner
 		 spinner.setAdapter(adapter);
 		 
 	}
 	/**
 	 * Update the stats for a category
 	 * @param entry The transaction with the data to get stats from
 	 */
 	public void addStats(BudgetEntry entry)
 	{
 		boolean added = false;
 		for(int i=0;i<categoryStats.size();i++)
 		{
 			if(categoryStats.get(i).getCategory().equalsIgnoreCase(entry.getCategory()))
 			{
 				categoryStats.get(i).addToNum(1);
 				categoryStats.get(i).addToTotal(entry.getValue());
 				added = true;
 				break;
 			}
 		}
 		if(!added) // No entry for this category yet, add a new one
 		{
 			CategoryEntry newEntry = new CategoryEntry(0, entry.getCategory(),1,entry.getValue());
 			categoryStats.add(newEntry);
 		}
 	}
 	/**
 	 * Appends a BudgetEntry to a specific TextView
 	 * @param view The view to append to
 	 * @param entry The BudgetEntry to append
 	 */
 	public void printEntry(ArrayList<String> list,BudgetEntry entry)
 	{
 		String temp = "Date: " + entry.getDate().substring(8) + "\t\t" + entry.getValue();
 		
 		if(entry.getValue().get()>-100 && entry.getValue().get()<1000)
 			temp+="\t";
 		if(entry.getValue().get()<=-1000)
 			temp+="\t";
 		//view.append("\t" + entry.getCategory());
 		temp+="\t"+entry.getCategory();
 		// Add comment if there is one
 		// But only print max 20 characters
 		String comment = entry.getComment();
 		if(comment!=null && !comment.equalsIgnoreCase(""))
 		{
 			temp+=" *";
 		}
 		
 		ad.add(new ViewHolder(entry));
 		
 	}
 	
 	
 	/**
 	 * Appends a months of BudgetEntrys to a TextView, only appends those chosen by selectedCategory, 
 	 * or if selectedCategory is set to All categories, appends all
 	 * @param view The TextView to append to
 	 * @param months An ArrayList<Stats> containing the months
 	 * @param index The index of the month
 	 */
 	public void printMonth(ArrayList<String> list, ArrayList<Stats> months, int index)
 	{
 		boolean monthPrinted = false;
 		if(index<months.size())
 		{
 			for(int k=0;k<months.get(index).getTransactions().size();k++)
 			{
 				BudgetEntry entry = months.get(index).getTransactions().get(k);
 				// Print the entry if it has the correct category
 				// or if all categories are set to be printed
 				if(selectedCategory.equalsIgnoreCase(entry.getCategory()) || selectedCategory.equalsIgnoreCase(getResources().getString(R.string.all_categories)))
 				{
 					if(!monthPrinted)
 					{
 						list.add(monthToString(months.get(index).getName()));
 						ad.add(new ViewHolder(monthToString(months.get(index).getName()),ViewHolder.MONTH));
 						monthPrinted = true;
 					}
 					printEntry(list,entry);
 					addStats(entry); // Add the stats to categoryStats
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Appends a year worth of BudgetEntrys to a TextView, using printMonth
 	 * @param view The TextView to append to
 	 * @param index The index of the year
 	 */
 	public void printYear(ArrayList<String> list, int index)
 	{
 		if(entries.size()>0)
 		{
 			ArrayList<Stats> months = (ArrayList<Stats>) years.get(index).getChildren();
 			list.add(years.get(index).getName());
 	    	if(selectedMonth>-1) // A specific month is chosen
 	    	{
 	    		printMonth(list,months,selectedMonth);
 	    	}
 	    	else // Print all transactions this year
 	    	{
 	        	for(int j=0;j<months.size();j++)
 	        	{
 	        		printMonth(list,months,j);
 	        	}
 	    	}
 		}
 	}
 	/**
 	 * Updates the whole log
 	 */
 	public void updateLog()
 	{
 		categoryStats = new ArrayList<CategoryEntry>();
 		ArrayAdapter<String> listViewAdapter;
 		ArrayList<String> allTransactionsList = new ArrayList<String>();
         allTransactionsList.clear();
         ad = new BudgetAdapter(this);
 
         if(selectedYear>-1) // A specific year is chosen
         {
 	        printYear(allTransactionsList,selectedYear);
 	        
         }
         else // Print all years
         {
         	//for(int i=0;i<years.size();i++)
         	//	printYear(top,i);
         }
         
         // Apply the adapter to the ListView
         top.setAdapter(ad);
         
         top.setOnItemClickListener(new AdapterView.OnItemClickListener() {
  		   @Override
  		   public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
  		      ViewHolder listItem = (ViewHolder)top.getItemAtPosition(position);
  		      if(!listItem.entry.getComment().equalsIgnoreCase(""))
  		      {	
  		    	  Toast.makeText(view.getContext(), listItem.entry.getComment(), Toast.LENGTH_LONG).show();
  		      }
  		   }
  		 });
         
         top.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapter, View view,
 					int position, long arg) {
 				 ViewHolder listItem = (ViewHolder)top.getItemAtPosition(position);
 	 		     selectedViewHolder = listItem;
 	 		    selectedViewHolderIndex = position;
 				 if(listItem.flag == ViewHolder.ENTRY)
 				 {
 			    	 DialogFragment newFragment = new EditTransactionDialogFragment();
 			    	 Bundle bundle = new Bundle();
 			    	 bundle.putParcelable("entry", (Parcelable) listItem.entry);
 			    	 
 			    	 newFragment.setArguments(bundle);
 			    	
 			    	 newFragment.show(getSupportFragmentManager(), "edit_transaction");
 			    	  //  Toast.makeText(view.getContext(), listItem.entry.getComment(), Toast.LENGTH_LONG).show();
 				 }
 	 		      return true;
 			}
 			
 		});
         updateStats();
 	}
 	
 	public void updateSelectedEntry(BudgetEntry newEntry)
 	{
 		if(newEntry!=null)
 		{
 			selectedViewHolder.entry.setCategory(newEntry.getCategory());
 			selectedViewHolder.entry.setComment(newEntry.getComment());
 			selectedViewHolder.entry.setValue(newEntry.getValue());
			updateSpinners();
			updateMonthSpinner();
 		}
 		top.invalidate();
 	}
 	
 	public void removeSelectedEntry()
 	{
 
         entries = datasource.getAllTransactions(BudgetDataSource.DESCENDING);
 		
 		if(entries.size()>0)
 		{
 			readEntries();
 		//	updateLog();
 			top.invalidate();
			updateSpinners();
			updateMonthSpinner();
 		}
 		//
 		//ad.remove(selectedViewHolderIndex);
 		//ad.notifyDataSetChanged();
 		//
 		//top.setAdapter(ad);
 		//top.invalidate();
 	}
 	/***
 	 * Parses a string and gets the correct month from Resources
 	 * @param in The string to parse
 	 * @return The month as a string
 	 */
 	public String monthToString(String in)
 	{
 		String[] months = getResources().getStringArray(R.array.months_array);
 		int month = Integer.parseInt(in) - 1;
 		return months[month];
 	}
 	
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
 		
 		Spinner yearSpinner =  (Spinner)findViewById(R.id.spinnerYear);
 		Spinner monthSpinner =  (Spinner)findViewById(R.id.spinnerMonth);
 		Spinner categorySpinner = (Spinner)findViewById(R.id.spinnerCategory);
 		int oldYear = selectedYear;
 		// Find what year, month and category that is chosen
 		selectedYear = yearSpinner.getSelectedItemPosition();
 		selectedMonth = monthSpinner.getSelectedItemPosition()-1; // When there is a "All categories" entry. -1 is needed to get correct month
 		selectedCategory = (String) categorySpinner.getSelectedItem();
 		if(oldYear!=selectedYear)
 			updateMonthSpinner();
 		updateLog();
 		ListView listView = (ListView) findViewById(R.id.ListViewLogTop);
 		listView.scrollTo(0,0);
 		TextView textView = (TextView)findViewById(R.id.textViewLogStats);
 		textView.scrollTo(0,0);
 		
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean onLongClick(View v) {
 		// TODO Auto-generated method stub
 		return true;
 	}
 	
 }
