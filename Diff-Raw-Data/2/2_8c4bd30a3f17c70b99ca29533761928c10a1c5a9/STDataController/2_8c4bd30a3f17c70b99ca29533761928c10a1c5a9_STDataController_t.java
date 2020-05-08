 package com.scarredions.stab;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class STDataController implements OnClickListener {
     
     // TODO: get this from location?
     private double tax = 0.08;
     private double tip = 0.2;
     
     private ArrayList<String> personNames;
     private ArrayList<Bitmap> personPhotos;
     private HashMap<Integer, HashSet<Integer>> personToSelections;
     private int currentPersonId;
     private int nextPersonId = 0;
     
     private ArrayList<String> menuItemNames;
     private ArrayList<Double> menuItemPrices;
     
     private LinearLayout menuListFooter;
     
     private String autoCompletedContactId;
     
     public STDataController() {
         personNames = new ArrayList<String>();
         personPhotos = new ArrayList<Bitmap>();
         personToSelections = new HashMap<Integer, HashSet<Integer>>();
         
         menuItemNames = new ArrayList<String>();
         menuItemPrices = new ArrayList<Double>();        
     }
     
     public boolean currentPersonHasSelected(int menuListPosition) {
         Set<Integer> selections = getCurrentPersonsSelections();
         return selections.contains(Integer.valueOf(menuListPosition));
     }
     
     public HashSet<Integer> getPersonsSelections(int personId) {
         return personToSelections.get(Integer.valueOf(personId));
     }
     
     public HashSet<Integer> getCurrentPersonsSelections() {
         return getPersonsSelections(currentPersonId);
     }
     
     public int getCurrentPersonId() {
         return currentPersonId;
     }
     
     public int getNumberOfPeopleWithSelection(int menuListPosition) {
         int counter = 0;
         for(HashSet<Integer> selections : personToSelections.values()) {
             for(Integer integer : selections) {
                 if (integer.intValue() == menuListPosition) {
                     counter++;
                 }
             }
         }        
         return counter;
     }
     
     public double getTaxPercentage() {
         return tax;
     }
     
     public double getTipPercentage() {
         return tip;
     }
     
     public static String getFormattedPercentage(double value) {
         return NumberFormat.getPercentInstance().format(value);
     }
     
     public String getFormattedTaxPercentage() {
         return getFormattedPercentage(getTaxPercentage());
     }
     
     public String getFormattedTipPercentage() {
         return getFormattedPercentage(getTipPercentage());
     }
     
     public Double getTax() {
         return getTotal() * tax;
     }
     
     public Double getTip() {
         return (getTotal() + getTax()) * tip;
     }
     
     public Double getTotal() {
         double total = 0;
         int menuItemPricesLen = menuItemPrices.size();
 
         for(int i = 0; i < menuItemPricesLen; i++) {
             total += menuItemPrices.get(i).doubleValue();
         }
         
         return Double.valueOf(total);
     }
     
     public Double getPersonTax() {
         return getTax() / personToSelections.size();
     }
     
     public Double getPersonTip() {
         return getTip() / personToSelections.size();
     }
     
     public Double getPersonTotal() {
         double total = 0;
         for (Integer selection : getCurrentPersonsSelections()) {
             total += getMenuItemPrice(selection.intValue()).doubleValue() /
                 getNumberOfPeopleWithSelection(selection.intValue());
         }
         return total;
     }
         
     public String getMenuItemName(int position) {
         return menuItemNames.get(position);
     }
     
     public Double getMenuItemPrice(int position) {
         return menuItemPrices.get(position);
     }
     
     public static String getFormattedPrice(Double price) {
         return NumberFormat.getCurrencyInstance().format(price.doubleValue());
     }
     
     public int getMenuItemCount() {
         return menuItemNames.size();
     }
     
     public View getMenuListFooter() {
         return menuListFooter;
     }
     
     public View getTaxView() {
         return menuListFooter.getChildAt(STConstants.MENU_LIST_FOOTER_TAX_POSITION);
     }
     
     public View getTipView() {
         return menuListFooter.getChildAt(STConstants.MENU_LIST_FOOTER_TIP_POSITION);
     }
     
     public View getTotalView() {
         return menuListFooter.getChildAt(STConstants.MENU_LIST_FOOTER_TOTAL_POSITION);
     }
     
     public int getPersonCount() {
         return personNames.size();
     }
     
     public String getPersonName(int position) {
         return personNames.get(position);
     }
     
     public Bitmap getPersonPhoto(int position) {
         return personPhotos.get(position);        
     }
     
     public void addPerson(String name, Bitmap photo) {
         personNames.add(name);
         personPhotos.add(photo);
         personToSelections.put(Integer.valueOf(nextPersonId), new HashSet<Integer>());
         nextPersonId++;
         updateMenuListFooter();
     }
     
     public void addMenuItem(String name, Double price) {
         menuItemNames.add(name);
         menuItemPrices.add(price);
     }
 
     public void setMenuListFooter(LinearLayout footerView) {
         menuListFooter = footerView;
     }
         
     public void setCurrentPersonId(int personId) {
         currentPersonId = personId;
     }
     
     public void setAutoCompletedContactId(String contactId) {
         this.autoCompletedContactId = contactId;
     }
     
     public String getAndClearAutoCompletedContactId() {
         String id = autoCompletedContactId;
         autoCompletedContactId = null;
         return id;
     }
     
     public void setTaxPercentage(double newTax) {
         tax = newTax;
         TextView taxView = (TextView) ((LinearLayout) getTaxView()).findViewById(R.id.list_footer_text);
         taxView.setText(STConstants.TAX + " (" + getFormattedTaxPercentage() + ")");
         updateTax();
     }
     
     public void setTipPercentage(double newTip) {
         tip = newTip;
        TextView tipView = (TextView) ((LinearLayout) getTipView()).findViewById(R.id.list_footer_text);
         tipView.setText(STConstants.TIP + " (" + getFormattedTipPercentage() + ")");
         updateTip();
     }
     
     public void setSelection(int menuListPosition, boolean checked) {
         HashSet<Integer> selections = getCurrentPersonsSelections();
         if (checked) {
             selections.add(Integer.valueOf(menuListPosition));
         } else {
             selections.remove(Integer.valueOf(menuListPosition));
         }
     }
 
     public void updateMenuListFooter() {
         updateTax();
     }
     
     public void updateTax() {
         TextView tax = (TextView) ((LinearLayout) getTaxView()).findViewById(R.id.list_footer_value);
         tax.setText(getFormattedPrice(getPersonTax()));
         updateTip();
     }
     
     public void updateTip() {
         TextView tip = (TextView) ((LinearLayout) getTipView()).findViewById(R.id.list_footer_value);
         tip.setText(getFormattedPrice(getPersonTip()));
         updateTotal();
     }
     
     public void updateTotal() {
         TextView total = (TextView) ((LinearLayout) getTotalView()).findViewById(R.id.list_footer_value);
         total.setText(getFormattedPrice(getPersonTotal() + getPersonTax() + getPersonTip()));
     }
     
     public void editTaxByDialog() {
         editByDialog(STConstants.TAX, Double.valueOf(getTaxPercentage()));
     }
     
     public void editTipByDialog() {
         editByDialog(STConstants.TIP, Double.valueOf(getTipPercentage()));
     }
 
     public void onClick(DialogInterface dialog, int whichButton) {
         if (whichButton == DialogInterface.BUTTON_POSITIVE) {
             AlertDialog d = (AlertDialog) dialog;
             String type = ((TextView) d.findViewById(R.id.value_view)).getText().toString();
             String value = ((EditText) d.findViewById(R.id.value_edit)).getText().toString();
             if (type.equals("Tax")) {
                 setTaxPercentage(Double.valueOf(value));
             } else if (type.equals("Tip")) {
                 setTipPercentage(Double.valueOf(value));
             }
         }
     }
     
     private void editByDialog(String type, Double value) {
         LayoutInflater factory = LayoutInflater.from(menuListFooter.getContext());
         LinearLayout dialogLayout = (LinearLayout) factory.inflate(R.layout.dialog_tax_or_tip_entry, null);
         ((TextView) dialogLayout.findViewById(R.id.value_view)).setText(type);
         ((EditText) dialogLayout.findViewById(R.id.value_edit)).setText(value.toString());
         
         new AlertDialog.Builder(menuListFooter.getContext())
             .setView(dialogLayout)
             .setPositiveButton("OK", this)
             .setNegativeButton("Cancel", this)
             .create().show();
     }
 }
