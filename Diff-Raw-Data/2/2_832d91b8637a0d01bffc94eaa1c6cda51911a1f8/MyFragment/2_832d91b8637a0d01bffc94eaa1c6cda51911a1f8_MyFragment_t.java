 package com.mustangexchange.polymeal;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.NumberPicker;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jon
  * Date: 9/1/13
  * Time: 10:22 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MyFragment extends Fragment {
 
     public static final String ARG_POSITION = "position";
    public int position;
 
     public MyFragment()
     {
         //empty contructor required for fragment subclasses
     }
 
     public MyFragment(int position) {
         this.position = position;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState)
     {
         View rootView = inflater.inflate(R.layout.main_fragment, container, false);
         //Actual Fragment inflating happens in the next line.
         ListView lv = ((ListView) rootView.findViewById(R.id.list));
         lv.setAdapter(PagerAdapter.foodAdapterList.get(position));
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
                 final int fPos = pos;
                 if(PagerAdapter.foodAdapterList.get(position).getPrices().size()>0)
                 {
                     final AlertDialog.Builder onListClick= new AlertDialog.Builder(PagerAdapter.activity);
                     onListClick.setCancelable(false);
                     onListClick.setTitle("Add to Cart?");
                     onListClick.setMessage("Would you like to add " + PagerAdapter.foodAdapterList.get(position).getNames().get(pos).replace("@#$", "") + " to your cart? Price: " + "$" + PagerAdapter.foodAdapterList.get(position).getPrices().get(pos));
                     onListClick.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int button) {
                             //money = boundPrices.get(tempIndex);
                             if (PagerAdapter.foodAdapterList.get(position).getNames().get(fPos).contains("@#$")) {
                                 AlertDialog.Builder onYes = new AlertDialog.Builder(PagerAdapter.activity);
                                 onYes.setTitle("How much?");
                                 onYes.setMessage("Estimated Number of Ounces: ");
                                 LayoutInflater inflater = PagerAdapter.activity.getLayoutInflater();
                                 View DialogView = inflater.inflate(R.layout.number_picker, null);
                                 final NumberPicker np = (NumberPicker) DialogView.findViewById(R.id.numberPicker);
                                 np.setMinValue(1);
                                 np.setMaxValue(50);
                                 np.setWrapSelectorWheel(false);
                                 np.setValue(1);
                                 onYes.setView(DialogView);
                                 onYes.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog, int button) {
                                         Cart.add(PagerAdapter.foodAdapterList.get(position).getNames().get(fPos), Double.toString(np.getValue() * new Double(PagerAdapter.foodAdapterList.get(position).getPrices().get(fPos))));
                                         PagerAdapter.updateBalance();
                                     }
                                 });
                                 onYes.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialog, int button) {
                                     }
                                 });
                                 onYes.show();
                             } else {
                                 Cart.add(PagerAdapter.foodAdapterList.get(position).getNames().get(position), PagerAdapter.foodAdapterList.get(position).getPrices().get(fPos));
                                 PagerAdapter.updateBalance();
                             }
                         }
                     });
                     onListClick.setNegativeButton("No", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int button) {
                         }
                     });
                     onListClick.setNeutralButton("Description", new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int button) {
                             AlertDialog.Builder onDialogClick = new AlertDialog.Builder(PagerAdapter.activity);
                             onDialogClick.setTitle("Description");
                             onDialogClick.setMessage(PagerAdapter.foodAdapterList.get(position).getDesc().get(fPos));
                             onDialogClick.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int button) {
 
                                 }
                             });
                             onDialogClick.setNegativeButton("Back", new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int button) {
                                     onListClick.show();
                                 }
                             });
                             onDialogClick.show();
                         }
                     });
                     onListClick.show();
                 }
                 else
                 {
                     AlertDialog.Builder invalidItem = new AlertDialog.Builder(PagerAdapter.activity);
                     invalidItem.setTitle("Invalid Item!");
                     invalidItem.setMessage("No price data was found for this item. It was not added to your cart.");
                     invalidItem.setNeutralButton("OK",new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int button){
 
                         }
                     });
                     invalidItem.show();
                 }
             }
         });
         return rootView;
     }
 }
