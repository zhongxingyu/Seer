 package com.wubinben.kata.pottercucumberjvm;
 
 import java.util.ArrayDeque;
 import java.util.Arrays;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ben
  * Date: 13-8-1
  * Time: 下午12:15
  * To change this template use File | Settings | File Templates.
  */
 public class PotterBookClient {
     private static ArrayDeque[] basket = {new ArrayDeque(), new ArrayDeque(), new ArrayDeque(), new ArrayDeque(), new ArrayDeque()};
     private static int calculatedPrice = 0;
 
     public static int getCalculatedPrice() {
         return calculatedPrice;
     }
 
     public static void setCalculatedPrice(int calculatedPrice) {
         PotterBookClient.calculatedPrice = calculatedPrice;
     }
 
     public static void initializeBasket() {
         for(ArrayDeque series : basket) {
             series.clear();
         }
     }
 
     public static void putIntoBasket(int numberOfBook, int seriesNumberOfBook) {
        if (seriesNumberOfBook <=0 || seriesNumberOfBook >=6) {
            throw new IllegalStateException("series number of book should be between 1 and 5.");
         }
         for (int i = 0; i < numberOfBook; i++) {
             basket[seriesNumberOfBook - 1].push(PotterBook.newInstance());
         }
     }
 
     static void calculatePrice() {
         int[] seriesBox = {0, 0, 0, 0, 0};
         setCalculatedPrice(0);
         if (hasPatternFiveThree()) {
             setCalculatedPrice(getCalculatedPrice() - 40);
         }
         while (areThereAnyBooksLeft()) {
             fillSeriesBoxAndCalculatePrice(seriesBox);
         }
     }
     private static boolean hasPatternFiveThree() {
         int[][] basketTwoDArray = convertBasketToTwoDArray();
         int[] differentSeriesCount = countDifferentSeries(basketTwoDArray);
         for (int i = 0; i < differentSeriesCount.length; i++) {
             if (differentSeriesCount[i] == 5) {
                 if (differentSeriesCount[i+1] == 3) {
                     return true;
                 }
             }
         }
         return false;
     }
 
    private void printBasketTwoDArray(int[][] basketTwoDArray) {
         System.out.print("basketTwoDArray: \n[\n");
         for (int[] row : basketTwoDArray) {
             System.out.print("[");
             for (int cell : row) {
                 System.out.print(cell + ", ");
             }
             System.out.println("]");
         }
         System.out.println("]");
     }
 
     private static int[] countDifferentSeries(int[][] basketTwoDArray) {
         int[] differentSeriesCount = new int[10];
         int count = 0;
         for (int i = 0; i < differentSeriesCount.length; i++) {
             for (int j = 0; j < basketTwoDArray.length; j++) {
                 count += basketTwoDArray[j][i];
             }
             differentSeriesCount[i] = count;
             count = 0;
         }
         return differentSeriesCount;
     }
 
     private static int[][] convertBasketToTwoDArray() {
         int[][] twoDArray = new int[5][10];
         for (int i = 0; i < basket.length; i++) {
             for (int j = 0; j < basket[i].size(); j++) {
                 if (i > 4 && j > 9) {
                     throw new IllegalStateException("the 2-d array is only 5x10.");
                 }
                 twoDArray[i][j] = 1;
             }
         }
         return twoDArray;
     }
 
     private static void clearSeriesBox(int[] seriesBox) {
         for (int i = 0; i < seriesBox.length; i++) {
             seriesBox[i] = 0;
         }
     }
 
     private static void fillSeriesBoxAndCalculatePrice(int[] seriesBox) {
         clearSeriesBox(seriesBox);
         for (int i = 0; i < basket.length; i++) {
             if (!basket[i].isEmpty()) {
                 basket[i].pop();
                 seriesBox[i] = 1;
             }
         }
         calculatePrice(seriesBox);
     }
 
    private void printBasket() {
         for (int i = 0; i < basket.length; i++) {
             System.out.println("==index: " + i + "; size of ArrayDeque:" + basket[i].size());
         }
     }
 
     private static void calculatePrice(int[] seriesBox) {
         int bookCount = 0;
         for (int number : seriesBox) {
             bookCount += number;
         }
         switch (bookCount) {
             case 1:
                 calculatedPrice += 800;
                 break;
             case 2:
                 calculatedPrice += 800 * 2 * 0.95;
                 break;
             case 3:
                 calculatedPrice += 800 * 3 * 0.9;
                 break;
             case 4:
                 calculatedPrice += 800 * 4 * 0.8;
                 break;
             case 5:
                 calculatedPrice += 800 * 5 * 0.75;
                 break;
         }
     }
 
    private void printSeriesBox(int[] seriesBox) {
         System.out.println("---seriesBox: " + Arrays.toString(seriesBox));
     }
 
     private static boolean areThereAnyBooksLeft() {
         for(ArrayDeque series : basket) {
             if (!series.isEmpty()) {
                 return true;
             }
         }
         return false;
     }
 
 }
