 package com.cs174.starrus.controller;
 import com.cs174.starrus.view.IView;
 import com.cs174.starrus.model.SysDate;
 import com.cs174.starrus.view.SetNewDateView;
 
 
 import java.text.SimpleDateFormat;
 import java.text.DateFormat;
 import java.util.Date;
 
 import java.text.DecimalFormat;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 
 
 public class SetNewDateSubmitController implements IController{
 	private boolean 	DEBUG		= true;
 	private SysDate		sD			= SysDate.getSysDate();
 	private SetNewDateView	sndV	= SetNewDateView.getView();
 	@Override
 	public void setView(IView view) {
 		// TODO Auto-generated method stub
 	}
 	@Override
 	public void process(String model) {
 		SetNewDateView snpV	= SetNewDateView.getView();
 
 		try{
 			String pattern 	= "\\d\\d-(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)-\\d\\d";
 			
 			if(DEBUG == true){
 				System.out.println(sndV.getTxtDate().getText().toUpperCase());
 				System.out.println(sndV.getTxtDate().getText().toUpperCase().matches(pattern));
 
 			}
 			
 			if(sndV.getTxtDate().getText().toUpperCase().matches(pattern)){
 				if( DEBUG == true){
 					System.out.println("SETTING DATE TO: " + sndV.getTxtDate().getText());
 				}
 
 				SysDate.setSysDate(new SysDate( sndV.getTxtDate().getText()));
 			}else{
 				sndV.setLblWarning("Please enter a valid date: \n The correct format is DD-MMM-YY");	
 			}
 			
 			

 									
 		}catch (Exception e){
 			System.out.println("SQLException in SetPriceController");
 			e.printStackTrace();
 		}
 	}
 }
 
