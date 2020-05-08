 package com.four_envelope.android.adapter;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.graphics.Color;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import com.four_envelope.android.R;
 import com.four_envelope.android.budget.BudgetWork;
 import com.four_envelope.android.budget.DailyBudget;
 import com.four_envelope.android.model.DailyExpense;
 import com.four_envelope.android.model.Person;
 
 /**
  * Show person list with it daily expenses 
  * @author VMaximenko
  *
  */
 public class ExecutionPersonAdapter extends ArrayAdapter<Person> {
 	
 	private ArrayList<Person> items;
 	private String mExecutionDate;
 	private final static int viewResourceId = R.layout.envelope_persons_list_item;
 
 	protected class ViewHolder {
 		TextView mName;
 		TextView mSum;
 		
 		Integer mPersonId;
 		String mPersonName;
 		String mDate;
 	}
 	
 	public ExecutionPersonAdapter(Context context, DailyBudget budget) {
 		super(context, viewResourceId, budget.execution.getEnvelope().getPersons());
 		
 		this.mExecutionDate = budget.date;
 		this.items = budget.execution.getEnvelope().getPersons();
 	}
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder = null;
 
 		if (convertView == null) {
 			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(	Context.LAYOUT_INFLATER_SERVICE);
 			convertView = vi.inflate(viewResourceId, null);
 
 			holder = new ViewHolder();
 
 			holder.mName = (TextView) convertView.findViewById(R.id.envelope_person_name);
 			holder.mSum = (TextView) convertView.findViewById(R.id.envelope_person_sum);
 
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
 		final Person o = items.get(position);
 		if (o != null) {
 // one day person sum expense	
 			final DailyExpense personDailyExpense = BudgetWork.getPersonDailyExpense( o, mExecutionDate );
 			
 			holder.mSum.setText( BudgetWork.getPersonDailyExpenseSum(personDailyExpense) );
 
 			holder.mName.setText( o.getName() );
 			if ( BudgetWork.checkPersonHasntSetDailyExpense(personDailyExpense) )
 				holder.mName.setTextColor(Color.RED);
 			
 			holder.mPersonId = o.getId();
 			holder.mPersonName = o.getName();
 			
 			holder.mDate = mExecutionDate;
 		}
 
 		return convertView;
     }	
 
 }
