 package budgetapp.fragments;
 /**
  * Dialog Fragment for adding a new category
  * 
  */
 
 import java.util.List;
 
 import budgetapp.activities.MainActivity;
 import budgetapp.main.R;
 import budgetapp.util.Money;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ChoosePriceFragment extends DialogFragment {
 	
 	private ListView theList;
 	private View view;
 	private String category;
 	
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
 	    LayoutInflater inflater = getActivity().getLayoutInflater();
 	    view = inflater.inflate(R.layout.dialog_choose_price, null);
 	    category = getArguments().getString("category");
 	    
 	    builder.setView(view);
 	    theList = (ListView) view.findViewById(R.id.dialog_choose_price_listview);
 	    
 	    TextView textView = (TextView)view.findViewById(R.id.dialog_choose_price_textview);
 	    textView.append(" '" + category + "'");
 	    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
 	    {
 	        public void onClick(DialogInterface dialog, int id) {
 	    	    ChoosePriceFragment.this.getDialog().cancel(); 
 	        }
         });   
 	   
 	    updatePrices();
 	    setUpListeners();
 	    return builder.create();
 	}
 	
 	/**
 	 * Adds all categories to the ListView and the "Other..." option
 	 */
 	private void updatePrices()
     {
 		List<Double> prices = ((MainActivity)getActivity()).getAutoCompleteValues(category);
 		
 		
         String temp[] = new String[prices.size()];
         for(int i=0;i<prices.size();i++)
         {
         	temp[i] = ""+Math.abs(prices.get(i));
         }
        
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
         		  android.R.layout.simple_list_item_1, android.R.id.text1, temp);
         
 		 theList.setAdapter(adapter);
     }
 	
 	/**
 	 * Set up the listeners for the ListView, click and longclick listeners
 	 */
 	private void setUpListeners()
 	{
 		theList.setOnItemClickListener(new AdapterView.OnItemClickListener() 
 		{
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
 				Object listItem = theList.getItemAtPosition(position);
 				
				String valueWithoutCurrency = listItem.toString().substring(0, listItem.toString().length() - Money.getCurrency().length());
 				((MainActivity)getActivity()).subtractFromBudget(listItem.toString(), category, null);
 				ChoosePriceFragment.this.getDialog().cancel();
 				
 			}
 	 	});
 	    
 		theList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
 		{
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long arg) {
 				if(position!=adapter.getCount()-1)
 			    {   
 					Object listItem = theList.getItemAtPosition(position);
 					((MainActivity)getActivity()).setChosenCategory(listItem.toString());
 					DialogFragment newFragment = new OtherCategoryDialogFragment();
 		            newFragment.show(((MainActivity)getActivity()).getSupportFragmentManager(), "other_category");
 		            ChoosePriceFragment.this.getDialog().cancel();
 			    }
 				
 				return true;
 		   }
 		});
 	}
 	
     
 	
 }
