 package com.shivaraj.training.exception;
 
 public class exce {
 
 
 	public void test(){
 			test();
 	}
 	public static void main(String[] args) {
 		new exce().test();
		int[] a = null;

		try{
			System.out.println(a.length);
		}catch(NullPointerException e){
			a=new int[5];
			System.out.println(a.length);
		}

		try{
			System.out.println(a[5]);
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("can access only till "+(a.length-1));
		}
 	}
 
 }
