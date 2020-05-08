package com.aethermade.shoplist;
 
 public class grocerItem {
 	String name;	
 	double price;
 	int quanity;
 	boolean hasCoupon;
 	double couponDiscount;
 	double finalPrice;
 	
 	public void updateFinalPrice(){
 		finalPrice = price - couponDiscount;
 	}
 	
 	public boolean iHaveACoupon(float discount){
 		hasCoupon = true;
 		couponDiscount = discount;
 		updateFinalPrice();
 		return true;
 	}
 	
 	public void groceryItem(String a, double b,int c){
 		name = a;
 		price = b;
 		quanity = c;
 		
 	}
 }
