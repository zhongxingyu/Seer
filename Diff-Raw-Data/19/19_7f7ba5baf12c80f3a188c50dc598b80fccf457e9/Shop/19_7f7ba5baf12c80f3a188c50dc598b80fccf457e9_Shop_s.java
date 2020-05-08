 /******************************************************
  * Copyright (C) 2012 Felix Wiemuth                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 package shoptool;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import static shoptool.Util.*;
 
 /**
  *
  * @author Felix Wiemuth
  */
 public class Shop implements ShopInterface {
 
     private final static String extension = "shop";
     private final static String NAME = "NAME";
     private final static String STARTCOORD = "STARTCOORD";
     private final static String ENDCOORD = "ENDCOORD";
     private final static String BOTCOORD = "BOTCOORD";
     private final static String VENDORNAME = "VENDORNAME";
     private final static String INTEREST_BUY = "INTEREST_BUY";
     private final static String INTEREST_SELL = "INTEREST_SELL";
     private final static String PRICELIST = "PRICELIST";
     private String name;
     private String startCoord;
     private String endCoord;
     private String botCoord;
     private String vendorName;
     private PriceList pricelist;
 
     public Shop(File file) {
         if (!checkFile(file, extension)) {
             return;
         }
         BufferedReader reader = getReader(file);
         String line;
         try {
             //TODO handle exceptions
             //set properties line by line
             setName(extract(reader.readLine(), NAME));
             setStartCoord(extract(reader.readLine(), STARTCOORD));
             setEndCoord(extract(reader.readLine(), ENDCOORD));
             setBotCoord(extract(reader.readLine(), BOTCOORD));
             setVendorName(extract(reader.readLine(), VENDORNAME));
 
             //TODO handle exceptions...
             //log("Will load: " + extract(reader.readLine(), PRICELIST) + '.' + PriceList.extension());
            setPricelist(new PriceList(new File(extract(reader.readLine(), PRICELIST) + '.' + PriceList.extension())));
         } catch (IOException ex) {
             Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
         }
         closeReader(reader);
     }
 
     //TODO implement
     private boolean revise() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String name() {
         return name;
     }
 
     @Override
     public String botCoord() {
         return botCoord;
     }
 
     @Override
     public String endCoord() {
         return endCoord;
     }
 
     @Override
     public String startCoord() {
         return startCoord;
     }
 
     @Override
     public String vendorName() {
         return vendorName;
     }
 
     @Override
     public PriceList pricelist() {
         return pricelist;
     }
 
     public static String extension() {
         return extension;
     }
 
     private void setName(String name) {
         this.name = name;
     }
 
     private void setEndCoord(String endCoord) {
         this.endCoord = endCoord;
     }
 
     private void setStartCoord(String startCoord) {
         this.startCoord = startCoord;
     }
 
     private void setBotCoord(String vendorCoord) {
         this.botCoord = vendorCoord;
     }
 
     private void setVendorName(String vendorName) {
         this.vendorName = vendorName;
     }
 
     private void setPricelist(PriceList p) {
         pricelist = p;
     }
 }
