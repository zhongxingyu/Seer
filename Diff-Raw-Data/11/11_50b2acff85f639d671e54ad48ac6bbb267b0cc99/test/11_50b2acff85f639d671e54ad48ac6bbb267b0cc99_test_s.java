 package org.ndaguan.micromanager.mmtracker;
 
 class test {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		double[][] md1 = new double[512][512];
 		int kk = 3;
 		for(int i=0;i<kk;i++){
 			for(int j=0;j<kk;j++){
 				md1[i][j] = (i+1)*10+(j+i);
 			}
 		}
 		double[][] md2 = new double[512][512];
 		for(int i=0;i<kk;i++){
 			for(int j=0;j<kk;j++){
 				md2[i][j] = 1;
 			}
 		}
  
 
 	}
 	private static void p(String str){
 		System.out.print(str);
 	}
 
 	private static void p(double[][] data){
 		for (int i = 0; i < data.length; i++) {
 			for (int j = 0; j < data[0].length; j++) {
 				System.out.print(String.format("%.0f",data[i][j]));
 				System.out.print("\t");
 			}
 			System.out.print("\n");
 
 
 		}
 	}
 	private static void p(double[][] data,int rowB,int rowE,int cloumnB,int cloumnE){
 		for (int i = rowB; i < rowE; i++) {
 			for (int j = cloumnB; j < cloumnE; j++) {
 				System.out.print(String.format("%.0f",data[i][j]));
 				System.out.print("\t");
 			}
 			System.out.print("\n");
 
 
 		}
 	}
 	private void p(double[] data){
 		for (int i = 0; i < data.length; i++) {
 			System.out.print(String.format("%.0f",data[i]));
 			System.out.print("\t");
 		}
 	}
 
 }
