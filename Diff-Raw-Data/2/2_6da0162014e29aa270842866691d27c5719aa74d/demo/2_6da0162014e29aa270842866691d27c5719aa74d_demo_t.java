 package com.bread.demo;
 
 import java.util.Iterator;
 import java.util.List;
 
 public class demo {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		System.out.println("This is demo app");
 		
 		System.out.println("Product version 1.0 - ");
 		
 		List<String> productList = product.getProductList();
 		
 		for(Iterator<String> i = productList.iterator(); i.hasNext();)
 		{
 			String product = i.next();
 			System.out.println("Product Name : " + product);
 		}
		
		System.out.println("Enhancement for version 1.0");
 	}
 
 }
