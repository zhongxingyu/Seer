 package edu.mit.moneyManager.listUtils;
 
 import java.util.ArrayList;
 
 import edu.mit.moneyManager.R;
 import edu.mit.moneyManager.model.Category;
 import edu.mit.moneyManager.view.ViewCategoryActivity;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.TabHost;
 import android.widget.TextView;
 
 public class SummaryCategoryListAdapter extends ArrayAdapter<Category>{
 
     private Context context;
     private ArrayList<Category> categories;
     private LayoutInflater inflator;
     private TabHost tabhost;
     
     public SummaryCategoryListAdapter(Context context, ArrayList<Category> categories, TabHost tabhost) {
         super(context, 0, categories);
         this.categories = categories;
         this.context = context;
         this.tabhost = tabhost;
         inflator = (LayoutInflater) context
                 .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 
     }
     
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View view = convertView;
         final Category category = categories.get(position);
         if (category != null) {
             view = inflator.inflate(R.layout.list_entry_category_summary, null);
             
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
            view.setLongClickable(false);
             
             TextView categoryName = (TextView) view.findViewById(R.id.view_summary_category);            
             TextView total = (TextView) view.findViewById(R.id.category_total);
             TextView remaining = (TextView) view.findViewById(R.id.category_remaining);
             
             categoryName.setText(category.getName());
             total.setText(new Double(category.getTotal()).toString());
             remaining.setText(new Double(category.getRemaining()).toString());
             
             ImageView detailBtn = (ImageView) view.findViewById(R.id.category_detail_button);
            detailBtn.setOnClickListener(new View.OnClickListener() {
                 
                 @Override
                 public void onClick(View v) {
                     // TODO Auto-generated method stub
 //                    tabhost.setCurrentTab(index)
                     Intent intent = new Intent(v.getContext(), ViewCategoryActivity.class);
                     intent.putExtra(ViewCategoryActivity.INTENT_KEY_CATEGORY, category.getName());
                     context.startActivity(intent);
                 }
             });
 
             
         }
         return view;
     }
 
 }
