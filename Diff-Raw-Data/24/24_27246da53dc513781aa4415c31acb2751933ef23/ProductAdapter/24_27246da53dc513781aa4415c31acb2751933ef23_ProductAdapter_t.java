 package no.poltilbud.feed;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 class ProductAdapter extends ArrayAdapter<Product> {
 
     private List<Product> items;
 
     public ProductAdapter(Context context, int textViewResourceId, List<Product> products) {
             super(context, textViewResourceId, products);
             this.items = products;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
             View v = convertView;
             if (v == null) {
                 LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 v = vi.inflate(R.layout.row, null);
             }
             Product o = items.get(position);
             if (o != null) {
                     TextView tt = (TextView) v.findViewById(R.id.toptext);
                     TextView bt = (TextView) v.findViewById(R.id.bottomtext);
                     if (tt != null) {
                           tt.setText(o.getName());                            }
                     if(bt != null){
                          bt.setText(o.getType() + ", N " + o.getNewPrice() + ". Fr " 
                         		  + o.getOldPrice() + ". Ned " + o.getDifferance() + " (" 
                        		  + o.getDifferancePerc() + "%)");                          
                     }
             }
             return v;
     }
 }
