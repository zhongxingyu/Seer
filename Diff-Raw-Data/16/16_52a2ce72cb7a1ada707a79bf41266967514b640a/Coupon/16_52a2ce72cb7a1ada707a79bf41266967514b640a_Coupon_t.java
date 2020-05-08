package com.aethermade.shoplist;
 
 public class Coupon {
 	float value;
 	float totalValue;
 	int multiplier;
 	String type;
 	int requiredAmount;
 	int requiredSize;
 	
 	public void updateFinalValue (){
 		totalValue = value * multiplier;
 	}
 	public void addNumNeeded (int amount){
 		requiredAmount = amount;
 	}
 	
 	public void addReqSize (int size){
 		requiredSize = size;
 	}
 	
 	public Coupon (float val,int n,String typ){
 		value = val;
 		multiplier = n;
 		type = typ;
 		
 		
 	}
 }
