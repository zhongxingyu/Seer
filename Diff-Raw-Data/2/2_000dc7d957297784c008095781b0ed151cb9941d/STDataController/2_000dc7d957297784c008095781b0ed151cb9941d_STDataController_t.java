 package com.scarredions.stab;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 
 /**
  * Data controller for the whole application. 
  * Keeps people, menu items, tax percentage, and tip percentage.
  * 
  * @author ianyh
  *
  */
 public class STDataController {
     
     /**
      * Static method for formatting a percentage as a String.
      * @param value
      * @return Percentage-formatted String of value.
      */
     public static String getFormattedPercentage(double value) {
         return NumberFormat.getPercentInstance().format(value);
     }
     
     private final STContactAccessor contactsAccessor = STContactAccessor.getInstance();
     
     // TODO: get this from location?
     private double tax = 0.08;
     private double tip = 0.2;
     private ArrayList<String> personNames;
     private ArrayList<String> personIds;
     private ArrayList<Bitmap> personPhotos;
     
     private ArrayList<HashSet<Integer>> personSelections;
     private int currentPersonId;
     
     private ArrayList<String> menuItemNames;
     
     private ArrayList<Double> menuItemPrices;
     
     private String autoCompletedContactId;
     
     private Context context;
     
     /**
      * Initializes all of the people/menu item parallel arrays.
      */
     public STDataController(Context context) {
         personNames = new ArrayList<String>();
         personIds = new ArrayList<String>();
         personPhotos = new ArrayList<Bitmap>();
         personSelections = new ArrayList<HashSet<Integer>>();
         
         menuItemNames = new ArrayList<String>();
         menuItemPrices = new ArrayList<Double>();
         
         this.context = context;
     }
     
     /**
      * Initialize from a savedInstanceState. If there is no savedInstanceState 
      * make sure that there is at least one person in the gallery data set otherwise
      * it doesn't display.
      * @param context
      * @param savedInstanceState
      */
     public STDataController(Context context, Bundle savedInstanceState) {
         this(context);
         if (savedInstanceState != null) {
             personNames = savedInstanceState.getStringArrayList("personNames");
             personIds = savedInstanceState.getStringArrayList("personIds");
             for (String id : personIds) {
                 personPhotos.add(getBitmapFromId(context, id));
             }
             
             ArrayList<String> selectionsStrings = savedInstanceState.getStringArrayList("personSelections");
             for (String selectionsString : selectionsStrings) {
                 HashSet<Integer> selections = new HashSet<Integer>();
                 if (!selectionsString.equals("")) {
                     for (String selection : selectionsString.split(",")) {
                         selections.add(Integer.valueOf(selection));
                     }
                 }
                 personSelections.add(selections);
             }
             
             menuItemNames = savedInstanceState.getStringArrayList("menuItemNames");
             double[] prices = savedInstanceState.getDoubleArray("menuItemPrices");
             for (double price : prices) {
                 menuItemPrices.add(Double.valueOf(price));
             }
             
             tax = savedInstanceState.getDouble("tax");
             tip = savedInstanceState.getDouble("tip");
         } else {
             this.addDefaultPerson();
         }
     }
     
     /**
      * Helper method for adding the "default person." Mostly to ensure that the
      * Gallery actually displays.
      */
     private void addDefaultPerson() {
         personNames.add("you");
         personIds.add(STConstants.PERSON_NULL_ID);
         personPhotos.add(null);
         personSelections.add(new HashSet<Integer>());        
     }
     
     /**
      * Adds a menu item.
      * @param name
      * @param price
      */
     public void addMenuItem(String name, Double price) {
         menuItemNames.add(name);
         menuItemPrices.add(price);
     }
     
     /**
      * Adds a person and loads their photo if available.
      * @param name
      * @param id _ID from Contacts if exists.
      */
     public void addPerson(String name, String id) {
        if (!id.equals(STConstants.PERSON_NULL_ID) && personIds.contains(id)) {
             return;
         }
         personNames.add(name);
         personIds.add(id);
         personPhotos.add(getBitmapFromId(context, id));
         personSelections.add(new HashSet<Integer>());
     }
     
     /**
      * Clears all menu item state.
      */
     public void clearMenuItems() {
         menuItemNames.clear();
         menuItemPrices.clear();
     }
 
     /**
      * Clears all person state, ensuring that the Gallery still displays.
      */
     public void clearPeople() {
         personNames.clear();
         personIds.clear();
         personPhotos.clear();
         personSelections.clear();
         addDefaultPerson();
     }
     
     /**
      * 
      * @param menuListPosition
      * @return true if the currently selected person has the menu item identified by
      * menuListPosition selected, false otherwise.
      */
     public boolean currentPersonHasSelected(int menuListPosition) {
         Set<Integer> selections = getCurrentPersonsSelections();
         return selections.contains(Integer.valueOf(menuListPosition));
     }
         
     /**
      * As a side effect, nulls out the autoCompletedContactId to prevent accidental
      * repeated contact IDs. 
      * @return most recent ID selected through auto complete in the add person dialog.
      */
     public String getAndClearAutoCompletedContactId() {
         String id = autoCompletedContactId;
         autoCompletedContactId = null;
         return id;
     }
     
     /**
      * If the contactId is valid, loads and returns the appropriate Bitmap, otherwise
      * returns null.
      * @param context
      * @param contactId
      * @return Contact photo Bitmap if exists.
      */
     public Bitmap getBitmapFromId(Context context, String contactId) {
         if (contactId == null || contactId.equals(STConstants.PERSON_NULL_ID)) {
             return null;
         }
         
         return contactsAccessor.loadContactPhotoFromId(context, contactId);
     }
     
     /**
      * 
      * @return currently selected persons hash of selected menu item ids.
      */
     public HashSet<Integer> getCurrentPersonsSelections() {
         return personSelections.get(currentPersonId);
     }
     
     public String getFormattedTaxPercentage() {
         return getFormattedPercentage(tax);
     }
     
     public String getFormattedTipPercentage() {
         return getFormattedPercentage(tip);
     }
         
     public int getMenuItemCount() {
         return menuItemNames.size();
     }
     
     public String getMenuItemName(int position) {
         return menuItemNames.get(position);
     }
     
     public Double getMenuItemPrice(int position) {
         return menuItemPrices.get(position);
     }
     
     /**
      * Returns a primitive array of price doubles for serializing into
      * a saved state Bundle.
      * @return menu item prices as a primitive array of doubles.
      */
     private double[] getMenuItemPrices() {
         double[] prices = new double[menuItemPrices.size()];
         for (int i = 0; i < menuItemPrices.size(); i ++) {
             prices[i] = menuItemPrices.get(i).doubleValue();
         }
         return prices;
     }
     
     /**
      * 
      * @param menuListPosition
      * @return the number of people who have selected the menu at menuListPosition.
      */
     public int getNumberOfPeopleWithSelection(int menuListPosition) {
         int counter = 0;
         for(HashSet<Integer> selections : personSelections) {
             for(Integer integer : selections) {
                 if (integer.intValue() == menuListPosition) {
                     counter++;
                 }
             }
         }
         return counter;
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
     
     /**
      * 
      * @return per person share of tax
      */
     public Double getPersonTax() {
         return getTax() / personSelections.size();
     }
     
     /**
      * 
      * @return per person share of tip
      */
     public Double getPersonTip() {
         return getTip() / personSelections.size();
     }
     
     /**
      * 
      * @return total amount of owed by currently selected person
      */
     public Double getPersonTotal() {
         double total = 0;
         for (Integer selection : getCurrentPersonsSelections()) {
             total += getMenuItemPrice(selection.intValue()).doubleValue() /
                 getNumberOfPeopleWithSelection(selection.intValue());
         }
         return total;
     }
     
     /**
      * 
      * @return tax owed on the entire bill
      */
     public Double getTax() {
         return getTotal() * tax;
     }
 
     /**
      * 
      * @return the percentage of tax
      */
     public double getTaxPercentage() {
         return tax;
     }
          
     /**
      * 
      * @return tip owed on the entire bill
      */
     public Double getTip() {
         return (getTotal() + getTax()) * tip;
     }
     
     /**
      * 
      * @return the percentage of tip
      */
     public double getTipPercentage() {
         return tip;
     }
     
     /**
      * 
      * @return total owed on the bill before tax and tip
      */
     public Double getTotal() {
         double total = 0;
         int menuItemPricesLen = menuItemPrices.size();
 
         for(int i = 0; i < menuItemPricesLen; i++) {
             total += menuItemPrices.get(i).doubleValue();
         }
         
         return Double.valueOf(total);
     }
     
     /**
      * Serializes all necessary state into the given Bundle.
      * Does not serialize contact photos. Those are reloaded from IDs on restore.
      * @param bundle
      */
     public void saveInstanceState(Bundle bundle) {
         /**
          * Save person data
          */
         bundle.putStringArrayList("personNames", personNames);
         bundle.putStringArrayList("personIds", personIds);
         ArrayList<String> selectionsStrings = new ArrayList<String>();
         for (HashSet<Integer> selections : personSelections) {
             String selectionsString = "";
             for (Integer s : selections) {
                 selectionsString += s.toString() + ",";
             }
             
             if (!selectionsString.equals("")) {
                 selectionsString = selectionsString.substring(0, selectionsString.length() - 1);
             }
             
             selectionsStrings.add(selectionsString);
         }
         bundle.putStringArrayList("personSelections", selectionsStrings);
         
         /**
          * Save menu list data
          */
         bundle.putStringArrayList("menuItemNames", menuItemNames);
         bundle.putDoubleArray("menuItemPrices", getMenuItemPrices());
         
         bundle.putDouble("tax", tax);
         bundle.putDouble("tip", tip);
         
     }
 
     /**
      * Used to hold the contact id of a selection from the auto complete
      * in the add person dialog.
      * @param contactId
      */
     public void setAutoCompletedContactId(String contactId) {
         this.autoCompletedContactId = contactId;
     }
     
     /**
      * Sets currently selected id so we can keep track of who is selected.
      * @param personId
      */
     public void setCurrentPersonId(int personId) {
         currentPersonId = personId;
     }
         
     /**
      * Toggles the selection of the menu item identified by menuListPosition for
      * the currently selected person.
      * @param menuListPosition
      * @param checked
      */
     public void setSelection(int menuListPosition, boolean checked) {
         HashSet<Integer> selections = getCurrentPersonsSelections();
         if (checked) {
             selections.add(Integer.valueOf(menuListPosition));
         } else {
             selections.remove(Integer.valueOf(menuListPosition));
         }
     }
     
     /**
      * Sets a new tax percentage.
      * @param newTax
      */
     public void setTaxPercentage(double newTax) {
         tax = newTax;
     }
     
     /**
      * Sets a new tip percentage.
      * @param newTip
      */
     public void setTipPercentage(double newTip) {
         tip = newTip;
     }
 
 }
