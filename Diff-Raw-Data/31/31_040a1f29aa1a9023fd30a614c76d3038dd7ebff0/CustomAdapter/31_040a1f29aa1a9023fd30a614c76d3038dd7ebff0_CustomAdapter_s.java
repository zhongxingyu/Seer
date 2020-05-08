 package com.tedebold.data;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import com.tedebold.Model.Product;
 import com.tedebold.buylist.R;
 import com.tedebold.provider.helpers.ProductHelper;
 
 import java.util.ArrayList;
 
 public class CustomAdapter extends ArrayAdapter<Product> {
   private String moving = null;
   public ArrayList<Product> products = new ArrayList<Product>();
   private float x = 0;
   private float previousX = 0;
 
   public CustomAdapter(Context context, int layout, ArrayList<Product> prod) {
     super(context, layout, prod);
     this.products = prod;
   }
 
   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
     View v = convertView;
     if (v == null) {
       LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
       v = vi.inflate(R.layout.row, null);
     }
     Product o = products.get(position);
     if (o != null) {
       TextView tt = (TextView) v.findViewById(R.id.productName);
       if (tt != null) {
         tt.setText(o.getName());
       };
       TextView tb = (TextView) v.findViewById(R.id.bottomtext);
       if (tb != null) {
         tb.setText(o.getBarcode());
       }
     }
     v.setOnTouchListener(new View.OnTouchListener() {
       @Override
       public boolean onTouch(View view, MotionEvent motionEvent) {
         TextView barcode = ((TextView) view.findViewById(R.id.bottomtext));
         if (barcode == null) return false;
         switch (motionEvent.getAction()) {
           case MotionEvent.ACTION_DOWN:
             x = motionEvent.getX();
             previousX = motionEvent.getX();
             break;
           case MotionEvent.ACTION_MOVE:
             if (motionEvent.getX() >= view.getWidth() / 2) {
               view.setBackgroundColor(Color.RED);
             } else if (motionEvent.getX() - previousX > 20 ) {
               moving = barcode.getText().toString();
               int newX = (int)(motionEvent.getX() - x);
               view.setLeft(newX);
               previousX = newX;
             }
             break;
           case MotionEvent.ACTION_UP:
             if (motionEvent.getX() >= view.getWidth() / 2  && barcode.getText().toString().equals(moving)) {
               remove(barcode.getText().toString());
             }
             clearValues(view);
             break;
           case MotionEvent.ACTION_CANCEL:
             clearValues(view);
         }
        return true;
       };
     });
 
     return v;
   }
 
   public void remove(String barcode){
     for (int i = 0; i < products.size(); i++)
     {
       if (products.get(i).getBarcode().equals(barcode)) {
         super.remove(products.get(i));
         this.notifyDataSetChanged();
         String where = ProductHelper.Products.BAR_CODE + "=" + barcode;
         ContentValues values = new ContentValues();
         values.put(ProductHelper.Products.IN_LIST.toString(), "false");
         getContext().getContentResolver().update(ProductHelper.Products.CONTENT_URI, values, where, null);
       }
     }
   }
 
   private void clearValues(View view){
     view.setLeft(0);
     view.setBackgroundColor(Color.WHITE);
     moving = null;
     x = 0;
     previousX = x;
   }
 }
