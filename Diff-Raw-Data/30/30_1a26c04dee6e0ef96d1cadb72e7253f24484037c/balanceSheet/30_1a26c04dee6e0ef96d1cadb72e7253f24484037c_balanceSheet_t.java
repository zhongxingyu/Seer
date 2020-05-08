 package com.example.gkaakash;
 
 import java.util.ArrayList;
 
 import com.gkaakash.controller.Report;
 import com.gkaakash.controller.Startup;
 
 import android.app.ActionBar.LayoutParams;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.text.SpannableString;
 import android.view.Gravity;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class balanceSheet extends Activity{
    
     static Integer client_id;
 	private Report report;
 	private Object[] balancesheetresult;
 	String financialFromDate,financialToDate,accountName,projectName,fromDate,toDate,balancetype;
 	TableRow tr;
 	private ArrayList<ArrayList<String>> BalanceSheetGrid;
 	private ArrayList<String> balancesheetresultList;
 	private TableLayout cashFlowtable1;
 	private TableLayout cashFlowtable2;
 	private View label;
 	private ArrayList<String> TotalAmountList;
 	private TextView balDiff;
 	String balanceToDateString;
	String getSelectedOrgType;
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        balancetype=reportMenu.balancetype;
       
        System.out.println("bal tyep"+balancetype);
        if(balancetype.equals("Conventional Balance Sheet"))
        {
     	   System.out.println("we are horizontall");
     	   setContentView(R.layout.balance_sheet_table);
        }
        else
        {
     	   System.out.println("we are vertical");
     	   setContentView(R.layout.vertical_balance_sheet_table);
        }
       
        report = new Report();
        client_id= Startup.getClient_id();
        financialFromDate =Startup.getfinancialFromDate();
        financialToDate=Startup.getFinancialToDate();
        //fromDate = reportMenu.givenfromDateString;
        balanceToDateString = reportMenu.givenToDateString;
        	cashFlowtable1 = (TableLayout)findViewById(R.id.col1);
    		cashFlowtable2 = (TableLayout)findViewById(R.id.col2);
    		balDiff = (TextView) findViewById(R.id.tvdifference);
    		System.out.println("balacesheet diff :"+balDiff.getText());
        balancetype=reportMenu.balancetype;
       getSelectedOrgType = reportMenu.orgtype;
        /*
 	     * set financial from date and to date in textview
 	     */
 	    TextView tvfinancialFromDate = (TextView) findViewById( R.id.tvTfinancialFromDate );
 	    TextView tvfinancialToDate = (TextView) findViewById( R.id.tvTfinancialToDate );
 	      
 	    tvfinancialFromDate.setText("Financial from : " +financialFromDate);
 	    tvfinancialToDate.setText("Financial to : " +financialToDate);
       Object[] params = new Object[]{financialFromDate,financialFromDate,balanceToDateString,"balancesheet",getSelectedOrgType,balancetype};
        balancesheetresult = (Object[]) report.getBalancesheetDisplay(params,client_id);
        System.out.println("balacesheet report :"+balancesheetresult);
        //balancesheetresult is 3 dimensional list 
        int count = 0;
   
        try {
        for(Object tb : balancesheetresult){
 
            Object[] t = (Object[]) tb;
            count = count + 1;
            BalanceSheetGrid = new ArrayList<ArrayList<String>>();
            if(count !=3)   
            {
 	           for(Object tb1 : t){
 	        
 	           	Object[] t1 = (Object[]) tb1;
 	           
 	           	balancesheetresultList = new ArrayList<String>();
 	
 	           	for(int j=0;j<t1.length;j++){
 	           		//System.out.println("item :"+t1[j].toString());
 	           		balancesheetresultList.add((String) t1[j].toString());
 	           	}
 	        System.out.println("balancesheetresultList "+balancesheetresultList); 	
            	BalanceSheetGrid.add(balancesheetresultList);
             
            } 
            }
          
            if (count == 3)
            {
         	   System.out.println("Total :"+t[0]);
         	   balDiff.setText(t[0].toString());
            }
            if(count == 1){
         	   System.out.println("im in count 1 table");
            	addTable(cashFlowtable1);
            }
            else if(count == 2){
            	addTable(cashFlowtable2);
            }
            
        }
    } catch (Exception e) {
    	System.out.println("I am an error"+e);
    	AlertDialog.Builder builder = new AlertDialog.Builder(balanceSheet.this);
 
           builder.setMessage("Please try again")
                   .setCancelable(false)
                   .setPositiveButton("Ok",
                           new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
 
                               }
                           });
 
           AlertDialog alert = builder.create();
           alert.show();		}
 }
 private void addTable(TableLayout tableID) {
    /** Create a TableRow dynamically **/
    for(int i=0;i<BalanceSheetGrid.size();i++){
    	
        ArrayList<String> columnValue = new ArrayList<String>();
        columnValue.addAll(BalanceSheetGrid.get(i));
        System.out.println("columnValue:"+columnValue);
        //create new row
        tr = new TableRow(this);
       
        if(columnValue.get(1).equalsIgnoreCase("Amount")){
        	//for heading pass green color code
     	  // System.out.println("iam in chaninging color "+columnValue.get(1));
        	setRowColorSymbolGravity(columnValue, Color.parseColor("#348017"));
        }
        else{
        	//for remaining rows pass black color code
        	setRowColorSymbolGravity(columnValue, Color.BLACK);
        }
        
        // Add the TableRow to the TableLayout
        tableID.addView(tr, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
    }
    
    
 }/*
  * 1. set the green background color for heading and black for remaining rows
  * 2. set right aligned gravity for amount and center aligned for other values
  * 3. set rupee symbol for amount
  */
 private void setRowColorSymbolGravity(ArrayList<String> columnValue, int color) {
 	for(int j=0;j<columnValue.size();j++){
         /** Creating a TextView to add to the row **/
         addRow(columnValue.get(j));   
         label.setBackgroundColor(color);
         String name = columnValue.get(0).toString();
         String amount = columnValue.get(1).toString();
         String total_amount = columnValue.get(2).toString();
         if(j==1)
         {//for amount coloumn
     		
     		if(!amount.equalsIgnoreCase("Total"))
     		{
     			if(!amount.equalsIgnoreCase("Amount")&&!amount.equalsIgnoreCase("Net Surplus"))
     			{
     				((TextView)label).setGravity(Gravity.RIGHT);
         			//For adding rupee symbol
                     if(columnValue.get(j).length() > 0)
                     {
                     
                         final SpannableString rsSymbol = new SpannableString(balanceSheet.this.getText(R.string.Rs)); 
                         ((TextView) label).setText(rsSymbol+" "+columnValue.get(j).toString());
                     }
     			}
                 
                 if(amount.equalsIgnoreCase("Net Surplus")
                 		||amount.equalsIgnoreCase("Net Loss")
                 		||amount.equalsIgnoreCase("Net DEFICIT")
                 		||amount.equalsIgnoreCase("NET PROFIT"))
             	{
                 	Toast.makeText(this, "Net Sr", Toast.LENGTH_SHORT).show();
         			((TextView)label).setGravity(Gravity.CENTER);
         			
             	}
                
                 if(amount.equalsIgnoreCase("Amount"))
                 {
                 	((TextView)label).setGravity(Gravity.CENTER);
         			//For adding rupee symbol
                     if(columnValue.get(j).length() > 0)
                     {
                     
                         final SpannableString rsSymbol = new SpannableString(balanceSheet.this.getText(R.string.Rs)); 
                         ((TextView) label).setText(rsSymbol+" "+columnValue.get(j).toString());
                     }
                 }
     		}else
     		{
     			
     			((TextView)label).setGravity(Gravity.CENTER);
     		} 
         }
     	if(j==2){//for amount coloumn
 
             if(total_amount.equalsIgnoreCase("Total Amount")||total_amount.equalsIgnoreCase("Amount"))
             {
     			((TextView)label).setGravity(Gravity.CENTER);
     			//For adding rupee symbol
                 if(columnValue.get(j).length() > 0)
                 {
                 
                     final SpannableString rsSymbol = new SpannableString(balanceSheet.this.getText(R.string.Rs)); 
                     ((TextView) label).setText(rsSymbol+" "+columnValue.get(j).toString());
                 }
             }else
             {
             	((TextView)label).setGravity(Gravity.RIGHT);
 	            //For adding rupee symbol
 	            if(columnValue.get(j).length() > 0)
 	            {
 	            
 	                final SpannableString rsSymbol = new SpannableString(balanceSheet.this.getText(R.string.Rs)); 
 	                ((TextView) label).setText(rsSymbol+" "+columnValue.get(j).toString());
 	            }
             }
         }
         if(j==0) {
         	if(balancetype.equals("Conventional Balance Sheet"))
         	{
 	        	if(name.equals("CORPUS")
 	        			||name.equals("CAPITAL") 
 	        			||name.equals("RESERVES")
 	        			||name.equals("LOANS")
 	        			||name.equals("CURRENT LIABILITIES")
 	        			||name.equals("FIXED ASSETS")
 	        			||name.equals("CURRENT ASSETS")
 	        			||name.equals("LOANS")
 	        			||name.equals("INVESTMENTS")
 	        			||name.equals("MISCELLANEOUS EXPENSES(ASSET)"))
 	        	{
 	        		((TextView)label).setGravity(Gravity.LEFT);
 	        	}
 	        	else
 	        	{
 	        		//((TextView)label).setGravity(Gravity.CENTER);
 	        	}
         	}
         	else{
         		if(name.equals("TOTAL CAPITAL")
 	        			||name.equals("TOTAL RESERVES & SURPLUS") 
 	        			||name.equals("TOTAL MISCELLANEOUS EXPENSES(ASSET)")
 	        			||name.equals("TOTAL OF OWNER'S FUNDS")
 	        			||name.equals("TOTAL BORROWED FUNDS")
 	        			||name.equals("TOTAL FUNDS AVAILABLE / CAPITAL EMPLOYED")
 	        			||name.equals("TOTAL FIXED ASSETS(NET)")
 	        			||name.equals("TOTAL LONG TERM INVESTMENTS")
 	        			||name.equals("TOTAL LOANS(ASSETS)")
 	        			||name.equals("TOTAL CURRENT ASSETS")
 	        			||name.equals("TOTAL CURRENT LIABILITIES")
 	        			||name.equals("NET CURRENT ASSETS OR WORKING CAPITAL")
         				){
         			((TextView)label).setGravity(Gravity.RIGHT);
         		}
         		if(name.equalsIgnoreCase("Particulars"))
         		{
         			((TextView)label).setGravity(Gravity.CENTER);
         		}
         		
         	}
         }
         if(!balancetype.equals("Conventional Balance Sheet"))
         {
         	if(j==3)
         	{//for amount coloumn
         		if(amount.equalsIgnoreCase("Amount"))
                 {
         			((TextView)label).setGravity(Gravity.CENTER);
         			//For adding rupee symbol
                     if(columnValue.get(j).length() > 0)
                     {
                     
                         final SpannableString rsSymbol = new SpannableString(balanceSheet.this.getText(R.string.Rs)); 
                         ((TextView) label).setText(rsSymbol+" "+columnValue.get(j).toString());
                     }
                 }
         		else
         		{
     			((TextView)label).setGravity(Gravity.RIGHT);
                 //For adding rupee symbol
                 if(columnValue.get(j).length() > 0){
                 
                     final SpannableString rsSymbol = new SpannableString(balanceSheet.this.getText(R.string.Rs)); 
                     ((TextView) label).setText(rsSymbol+" "+columnValue.get(j).toString());
                 }
         	}
            }
         }
     }
 	}
 
 	/*
  * this function add the value to the row
  */
 void addRow(String param){
     label = new TextView(this);
     ((TextView) label).setText(param);
     ((TextView) label).setTextColor(Color.WHITE);
     label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
             LayoutParams.WRAP_CONTENT));
     label.setPadding(2, 2, 2, 2);
     LinearLayout Ll = new LinearLayout(this);
     LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
             LayoutParams.WRAP_CONTENT);
     params.setMargins(1, 1, 1, 1);
     Ll.addView(label,params);
     tr.addView((View)Ll); // Adding textView to tablerow.
 }
    
 }
