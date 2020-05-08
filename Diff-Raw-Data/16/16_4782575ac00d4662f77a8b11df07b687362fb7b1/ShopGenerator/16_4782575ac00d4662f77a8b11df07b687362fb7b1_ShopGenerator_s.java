 /******************************************************
  * Copyright (C) 2012 Felix Wiemuth                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 
 package shoptool;
 
 import java.io.*;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Felix Wiemuth
  */
 public class ShopGenerator {
     
     private final static String extension = "pricelist";
     private final static String ID = "ID";
     private final static String NAME = "NAME";
     private final static String INTEREST_BUY = "INTEREST_BUY";
     private final static String INTEREST_SELL = "INTEREST_SELL";
     private final static String PRICE = "PRICE";
     private final static String NORMAL = "NORMAL";
     private final static String MAX = "MAX";
     private final static String TIME = "TIME";
     private final static String seperator = " ";
     private final static String assign = "=";
     
     private final static int DEFAULT_ID = 0;
     private final static double DEFAULT_INTEREST_BUY = 0;
     private final static double DEFAULT_INTEREST_SELL = 0;
     private final static int DEFAULT_MAXSTOCK = 0;
     private final static int DEFAULT_NORMALSTOCK = 0;
     private final static int DEFAULT_PRICE_BUY = 0;
     private final static int DEFAULT_PRICE_SELL = 0;
     private final static int DEFAULT_TIME = 0;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Shop shop = getShopFromFile(new File("ShopListExample2"));
         log(extract("NAME=Test SELL=5 PRICE=120", PRICE));
         log(extract("NAME=Test SELL=5 PRICE=120", NAME));
         log(extract("NAME=Test SELL=5 PRICE=120", "SELL"));
     }
     
     public static LinkedList<Shop> getShops(File dir) {
         LinkedList<Shop> shops = new LinkedList<Shop>();
         
         File[] files = dir.listFiles(new FileFilter() {
             @Override
             public boolean accept(File file) {
                 return file.isFile() &&
                  file.getPath().substring(
                        file.getPath().lastIndexOf('.')
                  ).equals(extension);
             }
         });
         
         for (File file : files) {
             shops.add(getShopFromFile(file));
             if (shops.peekFirst() == null) //file was no correct shop file
                 shops.removeFirst();
         }
         
         return shops;
     }
     
     private static PriceList loadBasePriceList(File file) {
         BufferedReader reader = getReader(file);
         return getPriceListFromReader(reader);
     }
     
     private static Shop getShopFromFile(File file) {
         BufferedReader reader;
         Shop shop = null;
         try {
             reader = getReader(file);
             shop = new Shop();
             //first line: name
             shop.setName(extract(reader.readLine(), NAME));
             //second line: interest buy
             try {shop.setInterestBuy(extractDouble(reader.readLine(), INTEREST_BUY));}
                 catch(Exception e) {shop.setInterestBuy(DEFAULT_INTEREST_BUY);}
             //third line: interest sell
             try {shop.setInterestSell(extractDouble(reader.readLine(), INTEREST_SELL));}
                 catch(Exception e) {shop.setInterestSell(DEFAULT_INTEREST_SELL);}
             //rest is the pricelist
             shop.setPricelist(getPriceListFromReader(reader));
         } catch (IOException ex) {
             Logger.getLogger(ShopGenerator.class.getName()).log(Level.SEVERE, null, ex);
         }
         shop.recalcPrices();
         return shop;
     }
     
     private static PriceList getPriceListFromReader(BufferedReader reader) {
         PriceList pricelist = new PriceList();
         String line = null;
         while(true) {
             try {
                 line = reader.readLine();
             } catch (IOException ex) {
                 Logger.getLogger(ShopGenerator.class.getName()).log(Level.SEVERE, null, ex);
             }
             if (line == null)
                 break;
             try {
                 pricelist.addItem(parseLine(line));
             } catch (NullPointerException e) {
                 err("Line '" + line + "' could not be parsed!");
             }
         }
         return pricelist;
     }
     
     private static PriceList getPriceListFromFile(File file) {
         return getPriceListFromReader(getReader(file));
     }
     
     private static PriceListItem parseLine(String line) {
         PriceListItem item = new PriceListItem();
         item.setName(extract(line, NAME));
         item.setId(extract(line, ID));
         
         try {item.setMaxStock(Integer.parseInt(extract(line, MAX)));}
             catch(Exception e) {item.setMaxStock(DEFAULT_MAXSTOCK);}
         
         try {item.setNormalStock(Integer.parseInt(extract(line, NORMAL)));}
             catch(Exception e) {item.setNormalStock(DEFAULT_NORMALSTOCK);}
         
         try {item.setPriceBuy(Integer.parseInt(extract(line, PRICE)));}
             catch(Exception e) {item.setPriceBuy(DEFAULT_PRICE_BUY);}
         
         try {item.setPriceSell(Integer.parseInt(extract(line, PRICE)));}
             catch(Exception e) {item.setPriceSell(DEFAULT_PRICE_SELL);}
         
         try {item.setStockUpdateTime(Integer.parseInt(extract(line, TIME)));}
             catch(Exception e) {item.setStockUpdateTime(DEFAULT_TIME);}
         
         return item;
     }
     
     private static String extract(String line, String key) {
         int i = line.indexOf(key);
         int end = line.indexOf(seperator, i);
         if (end == -1)
             end = line.length();
         return line.substring(line.indexOf(assign, i)+1, end);
     }
     
     private static int extractInt(String line, String key) throws Exception {
         try {
             return Integer.parseInt(extract(line, key));
         } catch(NumberFormatException e) {
             warn("Value of key " + key + " in " + line
                     + " should be Integer - using default value.");
             throw new Exception();
         }
     }
     
     private static double extractDouble(String line, String key) throws Exception {
         try {
             return Double.parseDouble(extract(line, key));
         } catch(NumberFormatException e) {
             warn("Value of key " + key + " in " + line
                     + " should be Double - using default value.");
             throw new Exception();
         }
     }
     
     private static BufferedReader getReader(File file) {
         FileReader input = null;
         try {
             input = new FileReader(file);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(ShopGenerator.class.getName()).log(Level.SEVERE, null, ex);
         }
         BufferedReader bufRead = new BufferedReader(input);
         return bufRead;
     }
     
     public static void log(Object o) {
         System.out.println(o);
     }
     
     public static void warn(Object o) {
         log("[WARNING] " + o);
     }
     
     public static void err(Object o) {
         log("[ERROR] " + o);
     }
 }
