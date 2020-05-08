 package com.your.worth.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /** 
  * @author catalinbora
  *
  *	singleton class that holds the data of the application
  *	the singleton part can be argued is useless but it seemed a nice exercise 
  */
 
 public class AppModel {
 
 	/**
 	 * singleton instance
 	 */
 	private static AppModel INSTANCE = null;
 	
 	/**
 	 * list of the income records
 	 */
 	private List<Record> incomeList = null;
 
     /**
      * size of the income list
      */
     private int incomeListSize = 0;
 
 	/**
 	 * list of the spending records
 	 */
 	private List<Record> spendingList = null;
 
     /**
      *  size of the spending list
      */
     private int spendingListSize = 0;
 
 	/**
 	 * just initialize the 2 lists
 	 */
 	private AppModel() {
 		incomeList = new ArrayList<Record>();
 		spendingList = new ArrayList<Record>();
 	}
 	
 	public static AppModel getInstance() {
         if (INSTANCE == null) {
         	INSTANCE = new AppModel();
         }
         return INSTANCE;
 	}
 
     // This feels like a duplicate :(. I don't like that there are 2 lists and both have similar methods.
 
     /**
      * Method that adds an <B>Income</B> record to the income list of the model
      *
      * @param value value of the income
      * @param description description of the income
      */
	public void setIncomeValueAndDescription( int value, String  description){
 		Record record = new Record(value,description);
         incomeList.add(record);
         incomeListSize++;
 	}
 
     /**
      * Method that returns the value of an income record given by an index
      * @param index the index of the record
      * @return the value of the record
      */
     public int getIncomeRecordValue(int index) {
        /* this method will never fail :D ( throw exception )
         * 0 values are not relevant therefore ignored  */
        if ( !incomeList.isEmpty() || index < incomeListSize) {
            return incomeList.get(index).getValue();
        }
        return 0;
     }
 
     /**
      * Method that returns the description of an income record given by an index
      * @param index the index of the record
      * @return the description of the record
      */
     public String getIncomeRecordDescription(int index) {
        /* this method will never fail :D ( throw exception ) */
         if ( !incomeList.isEmpty() || index < incomeListSize) {
             return incomeList.get(index).getDescription();
         }
         return null;
     }
 
     /**
      * Method that adds an <B>Spending</B> record to the spending list of the model
      *
      * @param value value of the spending
      * @param description description of the spending
      */
 	public void setSpendingValueAndDescription( int value, String  description){
 		Record record = new Record(value,description);
 		spendingList.add(record);
 	}
 
 }
 	
 	
