 package com.wubinben.kata.pottercucumberjvm;
 
 import java.util.ArrayDeque;
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ben
  * Date: 13-8-1
  * Time: 下午5:45
  * To change this template use File | Settings | File Templates.
  */
 public class ShoppingBasket {
     private  ArrayDeque[] basket = {new ArrayDeque(), new ArrayDeque(), new ArrayDeque(), new ArrayDeque(), new ArrayDeque()};
     private static final Logger LOGGER = Logger.getLogger(ShoppingBasket.class.getName());
     private int[] seriesBox = {0, 0, 0, 0, 0};
 
     public ShoppingBasket() {
         // To turn on logging, set level to be Level.INFO.
         LOGGER.setLevel(Level.OFF);
     }
 
     public void initializeBasket() {
         for(ArrayDeque series : basket) {
             series.clear();
         }
     }
 
     void sortBooksInBasket(int numberOfBook, int seriesNumberOfBook) {
         for (int i = 0; i < numberOfBook; i++) {
             basket[seriesNumberOfBook - 1].push(PotterBook.newInstance());
         }
     }
 
     boolean areThereAnyBooksLeft() {
         for(ArrayDeque series : basket) {
             if (!series.isEmpty()) {
                 return true;
             }
         }
         return false;
     }
 
     void printBasketTwoDArray(int[][] basketTwoDArray) {
         StringBuilder output = new StringBuilder("");
         output.append("**basketTwoDArray: \n[\n");
         for (int[] row : basketTwoDArray) {
             output.append("[");
             for (int cell : row) {
                 output.append(cell);
                 output.append(", ");
             }
             output.append("]");
         }
         output.append("]");
         LOGGER.info(output.toString());
     }
 
     void printBasket() {
         StringBuilder output = new StringBuilder("==basket: [");
         for (int i = 0; i < basket.length; i++) {
             output.append(basket[i].size());
             output.append(",");
         }
         output.append("]");
         LOGGER.info(output.toString());
     }
 
     public int[][] convertBasketToTwoDArray() {
        int[][] twoDArray = new int[DiscountStrategy.MAX_SERIES_NUMBER][DiscountStrategy.MAX_NUMBER_OF_COPIES_FOR_EACH_SERIES];
         for (int i = 0; i < basket.length; i++) {
             for (int j = 0; j < basket[i].size(); j++) {
                 if (i >= DiscountStrategy.MAX_SERIES_NUMBER && j >= DiscountStrategy.MAX_NUMBER_OF_COPIES_FOR_EACH_SERIES) {
                     throw new IllegalStateException("the 2-d array is only 5x10.");
                 }
                 twoDArray[i][j] = 1;
             }
         }
         printBasketTwoDArray(twoDArray);
         return twoDArray.clone();
     }
     public int[] generateSeriesBox() {
         clearSeriesBox(seriesBox);
         for (int i = 0; i < basket.length; i++) {
             if (!basket[i].isEmpty()) {
                 basket[i].pop();
                 seriesBox[i] = 1;
             }
         }
         return seriesBox.clone();
     }
     void clearSeriesBox(int[] seriesBox) {
         for (int i = 0; i < seriesBox.length; i++) {
             seriesBox[i] = 0;
         }
         printSeriesBox(seriesBox);
     }
     void printSeriesBox(int[] seriesBox) {
         LOGGER.info("--seriesBox: " + Arrays.toString(seriesBox));
     }
 
 }
