 package com.example.delteutgifter;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import java.util.ArrayList;
 
 /**
  * Created by:
  * User: Einar
  * Date: 13.03.13
  * Time: 16:36
  */
 public class ExpenseLogItemAdapter extends ArrayAdapter<Expense> {
     private ArrayList<Expense> expenseList;
 
     public ExpenseLogItemAdapter(Context context, int textViewResourceId, ArrayList<Expense> expenseList) {
         super(context, textViewResourceId, expenseList);
         this.expenseList = expenseList;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View v = convertView;
         if (v == null) {
             LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.listitem, null);
         }
 
         Expense expense = expenseList.get(position);
         if (expense != null) {
             TextView name = (TextView) v.findViewById(R.id.name);
             TextView amount = (TextView) v.findViewById(R.id.amount);
             TextView date = (TextView) v.findViewById(R.id.date);
             TextView month = (TextView) v.findViewById(R.id.month);
             if (name != null) {
                 name.setText(expense.getName());
             }
             if (amount != null) {
                 amount.setText(expense.getAmountString());
             }
             if (date != null) {
                 date.setText(expense.getDateString());
             }
             if (month != null) {
                 month.setText(expense.getMonthString());
             }
         }
         return v;
     }
 
 
 }
