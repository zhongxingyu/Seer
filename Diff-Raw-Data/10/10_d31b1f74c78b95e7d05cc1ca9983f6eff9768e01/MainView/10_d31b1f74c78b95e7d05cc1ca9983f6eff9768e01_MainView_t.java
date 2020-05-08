 package budgetapp.views;
 
 /**
  * The View for the MainActivity, observes the model
  */
 import java.util.ArrayList;
 import java.util.List;
 
 import budgetapp.main.R;
 import budgetapp.models.BudgetModel;
 import budgetapp.util.BudgetAdapter;
 import budgetapp.util.BudgetEntry;
 import budgetapp.util.BudgetFunctions;
 import budgetapp.util.CategoryEntry;
 import budgetapp.util.DayEntry;
 import budgetapp.util.IBudgetObserver;
 import budgetapp.util.Money;
 import budgetapp.util.database.BudgetDataSource;
 import budgetapp.viewholders.TransactionViewHolder;
 import android.content.Context;
 import android.graphics.Color;
 import android.text.Html;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class MainView extends LinearLayout implements IBudgetObserver{
 	
 	public static interface ViewListener
 	{
 		public void chooseCategory();
 		public void favButtClick(Button id);
 		public void favButtLongClick(Button id);
 	}
 	
 	private Button chooseCategoryButton;
 	private Button favButt1,favButt2,favButt3;
 	private ViewListener viewListener;
 	private BudgetModel model;
 	
 	public void setViewListener(ViewListener viewListener)
 	{
 		this.viewListener = viewListener;
 	}
 	
 	public MainView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	public void setModel(BudgetModel model)
 	{
 		this.model = model;
 		model.addObserver(this);
 	}
 	
 	@Override
     protected void onFinishInflate()
     {
     	super.onFinishInflate();
     	
     	chooseCategoryButton = (Button)findViewById(R.id.button_choose_category);
     	favButt1 = (Button)findViewById(R.id.topCategoryButton1);
     	favButt2 = (Button)findViewById(R.id.topCategoryButton2);
     	favButt3 = (Button)findViewById(R.id.topCategoryButton3);
     	
     	setUpListeners();
     	
     	
     }
 
 	@Override
 	public void update() {
 		updateLog();
 		updateColor();
 		updateCurrentBudget();
 		updateButtons();
 	}
 	
 	
 	/**
 	 * Gets the autocomplete values and sets them for the editTextSubtract
 	 */
 	public void setUpAutocompleteValues()
 	{
 
     	// Get a reference to the AutoCompleteTextView in the layout
     	AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.editTextSubtract);
     	
     	ArrayList<Double> values = (ArrayList<Double>) model.getAutocompleteValues();
     	ArrayList<String> stringValues = new ArrayList<String>();
     	
     	for(int i = 0; i < values.size(); i++)
     	{
     		stringValues.add(""+values.get(i)*-1); // Flip the value, transactions are stored as negative
     		
     	}
     	// Create the adapter and set it to the AutoCompleteTextView 
     	
     	ArrayAdapter<String> adapter = 
         new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, stringValues);
     	textView.setAdapter(adapter);
 		
 	}
 	
 	public void setUpEmptyAutocompleteValues()
 	{
 		AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.editTextSubtract);
 	
 		ArrayAdapter<String> adapter = 
 		        new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_list_item_1, new ArrayList<String>());
 		    	textView.setAdapter(adapter);
 	
 	}
 	
 	private void updateCurrentBudget()
     {
     	TextView curr = (TextView)findViewById(R.id.textViewCurrentBudget);
     	curr.setText(""+model.getCurrentBudget());
     }
 	
 	/**
 	 * Update the favButtz to show top categories
 	 */
 	private void updateButtons()
     {
     	int numButtons = 3;
     	ArrayList<Button> theButtons = new ArrayList<Button>();
     	theButtons.add((Button)findViewById(R.id.topCategoryButton1));
     	theButtons.add((Button)findViewById(R.id.topCategoryButton2));
     	theButtons.add((Button)findViewById(R.id.topCategoryButton3));
     	List<CategoryEntry> categories = model.getCategoriesSortedByNum();
     	//Remove categories with positive total
     	int i;
 		for(i=0;i<categories.size();i++)
 		{
 			if(categories.get(i).getValue().get()>0 || categories.get(i).getNum()<2)
 			{	
 				categories.remove(i);
 				i--;
 			}
 		}
     	int index = categories.size()-1; // Start at the last entry
     	
     	for(i = 0;i<BudgetFunctions.min(numButtons,categories.size());i++)
     	{
     		theButtons.get(i).setText(categories.get(index).getCategory());
     		theButtons.get(i).setVisibility(View.VISIBLE);
     		index--;
     	}
     	
     	for(;i<numButtons;i++)
     	{
     		theButtons.get(i).setVisibility(View.GONE);
     	}
     	
     }
 	
 	private void updateLog()
     {
     	List<BudgetEntry> entries = model.getSomeTransactions(5,BudgetDataSource.DESCENDING);
     	BudgetAdapter transactionAdapter = new BudgetAdapter(this.getContext(), R.layout.listitem_transaction);
     	
         for(int i = 0; i < entries.size(); i++)
         {
         	transactionAdapter.add(new TransactionViewHolder(entries.get(i)));
         }
         TextView left = (TextView)findViewById(R.id.textViewLogLeft);
         TextView right = (TextView)findViewById(R.id.textViewLogRight);
         left.setText(Html.fromHtml("<b>Latest transactions</b>"));
         right.setText(Html.fromHtml("<b>Category</>"));
         /*
         for(int i=0;i<entries.size();i++)
         {	
         		left.append(entries.get(i).getDate() + ":    " + entries.get(i).getValue() + "\n");
         		right.append(entries.get(i).getCategory() + "\n");
         		
         }
         */
         ListView transactionListView = (ListView)findViewById(R.id.listViewLatestTransactions);
         transactionListView.setAdapter(transactionAdapter);
         
         
         List<DayEntry> days = model.getSomeDays(7,BudgetDataSource.DESCENDING);
         left = (TextView)findViewById(R.id.textViewDailyCashFlow);
         left.setText(Html.fromHtml("<b>Daily cash flow</b><br />"));
         for(int i=0;i<days.size();i++) 
         {	
         	left.append(days.get(i).getDate()+ ": ");
     		left.append(days.get(i).getTotal()+"\n");
         }
         
     }
 	
 	// Colors the currentBudget text depending on the size of the current budget
     private void updateColor()
     {
     	int numEntries = 30;
     	List<DayEntry> days = model.getSomeDays(numEntries,BudgetDataSource.DESCENDING);
     	
    	if(days.size()==0)
    		return;
     	Money tempValue = days.get(0).getValue();
     	String tempDate = days.get(0).getDate();
     	double hoursWeight = (double)(BudgetFunctions.getHours() * 60);
     	double dayWeight = (double)(hoursWeight + BudgetFunctions.getMinutes());
     	double totalWeight = dayWeight / (24.0 * 60.0);
     	
     	tempValue = tempValue.multiply(totalWeight);
     	days.set(0, new DayEntry(tempDate, tempValue));
     	
     	Money derivative = (BudgetFunctions.getWeightedMean(days,numEntries));
     	TextView newBudget = (TextView)findViewById(R.id.textViewCurrentBudget);
     	
     	double maxValue = (double)model.getDailyBudget().get();
     	double floatDerivative = derivative.get() / maxValue;
     	// Choose transfer function, sqrt if negative for fast red value,
     	// x^2 if positive for slow green value
     	if(floatDerivative<0)
     		floatDerivative=-Math.sqrt(Math.abs(floatDerivative));
     	else
     		floatDerivative=floatDerivative*floatDerivative;
     	
     	floatDerivative = floatDerivative * 255;
     	
     	int coloringFactor = BudgetFunctions.min(255,Math.abs((int)floatDerivative));
     	int start = 255;
     	
     	if(derivative.get()<0)
     		newBudget.setTextColor(Color.rgb(start,start-coloringFactor,start-coloringFactor));
     	else
     		newBudget.setTextColor(Color.rgb(start-coloringFactor,start,start-coloringFactor));
     	
     }
 
     private void setUpListeners()
 	{
 		
 		chooseCategoryButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				viewListener.chooseCategory();
 			}
 		});
     	
     	favButt1.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				viewListener.favButtClick(favButt1);
 			}
 		});
     	favButt1.setOnLongClickListener(new View.OnLongClickListener() {
 			
 			@Override
 			public boolean onLongClick(View v) {
 				viewListener.favButtLongClick(favButt1);
 				return true;
 			}
 		});
     	
     	favButt2.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				viewListener.favButtClick(favButt2);
 			}
 		});
     	favButt2.setOnLongClickListener(new View.OnLongClickListener() {
 			
 			@Override
 			public boolean onLongClick(
 					View v) {
 				viewListener.favButtLongClick(favButt2);
 				return true;
 			}
 		});
     	
     	favButt3.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				viewListener.favButtClick(favButt3);
 			}
 		});
     	favButt3.setOnLongClickListener(new View.OnLongClickListener() {
 			
 			@Override
 			public boolean onLongClick(View v) {
 				viewListener.favButtLongClick(favButt3);
 				return true;
 			}
 		});
 	}
 }
