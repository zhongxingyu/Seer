 /******************************************************
  * Copyright (C) 2012 Felix Wiemuth                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 package shoptool;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import static shoptool.Util.*;
 
 /**
  * 
  * @author Felix Wiemuth
  */
 public class PriceList implements PriceListInterface {

     private final static String extension = "pricelist";
     private LinkedList<PriceListItem> items;
 
    //TODO preserve order!
     /**
      * Create a new pricelist from existing pricelists.
      * @param base
      * @param factor
      * @param interestBuy
      * @param interestSell 
      */
     public PriceList(LinkedList<PriceList> base, double factor, double interestBuy, double interestSell) {
         HashMap<String, PriceListItem> map = new HashMap<String, PriceListItem>();
         //add all items from pricelists in 'base', if items occur more than once
         //only take first one
         for (PriceList p : base) {
             for (PriceListItem i : p.items()) {
                 if (!map.containsKey(i.id())) {
                     //copy item
                     PriceListItem iNew = new PriceListItem(i);
                     iNew.changePrices(factor * (1 + interestBuy), factor * (1 - interestSell));
                     map.put(iNew.id(), iNew);
                 }
             }
         }
         items = new LinkedList<PriceListItem>(map.values());
     }
 
     public PriceList(File file) throws Exception {
         if (!checkFile(file, extension)) {
             throw new Exception();
         }
         items = new LinkedList<PriceListItem>();
         BufferedReader reader = getReader(file);
         String line;
         while (true) {
             try {
                 line = reader.readLine();
             } catch (IOException e) {
                 err("I/O error: " + e.getMessage());
                 break;
             }
             if (line == null) {
                 break;
             }
             addItem(line);
         }
         closeReader(reader);
        info("Loaded pricelist " + file.getName());
     }
 
     //TODO align keys when saving!
    public void save(String path) {
        File file = new File(path);
         if (file.isFile()) {
             err("Cannot write pricelist: file \"" + file.toString() + "\" already exists!");
             return;
         }
         FileWriter out;
         try {
             out = new FileWriter(file);
             for (PriceListItem item : items) {
                 out.write(item.toStr() + '\n');
             }
             out.close();
             info("Successfully created \"" + file.toString() + "\"!");
         } catch (IOException e) {
             err("I/O error: " + e.getMessage());
         }
     }
 
     private void addItem(String raw) {
         String line = extractCode(raw);
         if (line.equals("")) {
             return; //no code in this line
         }
         try {
             addItem(new PriceListItem(line));
         } catch (Exception e) {
             //do not add item - it cannot be build from 'line'
         }
     }
 
     private void addItem(PriceListItem item) {
         items.add(item);
     }
 
     /**
      * Call 'revise()' on all items in the pricelist and remove uncorrectables.
      */
     public void revise() {
         info("Revising pricelist:");
         Iterator<PriceListItem> it = items.iterator();
         while (it.hasNext()) {
             PriceListItem item = it.next();
             if (!item.revise()) {
                 err("Could not revise item '" + item.toStr() + "' - removing from pricelist!");
                 it.remove();
             }
 
         }
         info("Finished revising pricelist.");
     }
 
     public static LinkedList<PriceList> getPriceLists(File dir) {
         LinkedList<PriceList> lists = new LinkedList<PriceList>();
 
         File[] files = dir.listFiles(new FileFilter() {
 
             @Override
             public boolean accept(File file) {
                 return checkFile(file, extension);
             }
         });
 
         for (File file : files) {
             try {
                 lists.add(new PriceList(file));
             } catch (Exception e) {
                 //do not add pricelist - it cannot be loaded correctly from 'file'
             }
         }
 
         return lists;
     }
 
     public static String extension() {
         return extension;
     }
 
     @Override
     public LinkedList<PriceListItem> items() {
         return items;
     }
 }
