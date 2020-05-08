 /******************************************************
  * Copyright (C) 2012 Felix Wiemuth                   *
  * Licensed under the GNU GENERAL PUBLIC LICENSE      *
  * See LICENSE or http://www.gnu.org/licenses/gpl.txt *
  ******************************************************/
 package shoptool;
 
 import java.io.*;
 import java.util.LinkedList;
 import static shoptool.Util.*;
 
 /**
  *
  * @author Felix Wiemuth
  */
 public class ShopTool {
 
     public static void main(String[] args) {
         execute(args);
         //run();
     }
     private static final String CMD_HELP = "help";
     //private static final String CMD_EXIT = "exit";
     private static final String CMD_PRICELIST = "pl";
     private static final String CMD_PRICELIST_SYNTAX = CMD_PRICELIST
             + " [factor] [interest buy] [interest sell]"
             + " [[-n][-N]] normalStock"
             + " [[-max][-MAX]] maxStock"
             + " [[-t][-T]] stockUpdateTime"
             + " [dest] [file 1] [file 2] ...";
     private static final String CMD_PRICELIST_HELP = "Command '" + CMD_PRICELIST_SYNTAX + "':\n"
             + "Generate a new pricelist based on the pricelists given in file1, file 2, ... .\n"
             + "Pricelists are revised before being processed.\n"
             + "factor: factor all prices with this constant\n"
             + "interest buy: add an interest to all buy prices\n"
             + "interest sell: subtract an interest from all sell prices\n"
             + "- switches: -x set value if not set, -X force value\n"
             + "dest: path to the file the new pricelist should be saved to\n"
             + "file n: a correct pricelist file";
     private static final String CMD_SHOP = "shp";
     private static final String CMD_SHOP_SYNTAX = "";
     private static final String CMD_SHOP_HELP = "Usage: ";
     private static final String CMD_EVENT = "ev";
     private static final String CMD_EVENT_SYNTAX = CMD_EVENT //TODO optional: give pricelist in command!
             + " [-s] [dest] [file 1] [file 2] ...";
     private static final String CMD_EVENT_HELP = "Command '" + CMD_EVENT_SYNTAX + "':\n"
             + "Generate the event code for shops given in files file1, file 2, ... .\n"
             + "Shops are revised before being processed.\n"
             + "dest: path to the file the event code should be saved to\n"
             + "-s: with static (file 'static.xml' needed)\n"
             + "file n: a correct shop file";
     private static final String CMD_REVISE_PRICELIST = "rv";
     private static final String CMD_REVISE_PRICELIST_SYNTAX = CMD_REVISE_PRICELIST
             + " [file]";
 
     public static void run() {
         log("Welcome to ShopTool for Minecraft/SimpleServer!\n"
                 + "Use \"help\" for help.");
         BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
         String line;
         while (true) {
             System.out.print("> ");
             try {
                 line = in.readLine();
                 if (!execute(line.split(" "))) {
                     return;
                 }
             } catch (IOException e) {
                 err("I/O error: " + e.getMessage());
             }
         }
 
     }
 
     private static boolean execute(String[] args) {
         if (args.length == 0) {
             return true;
         }
         if (args[0].equals(CMD_PRICELIST)) {
             if (args.length < 5) {
                 argErr(5);
             }
             int normal = -1;
             boolean fNormal = false;
             int max = -1;
             boolean fMax = false;
             int time = -1;
             boolean fTime = false;
             int i = 4; //position of first optional parameter
             if (args[i].equals("-n")) {
                 try {normal = parseNat(args[i + 1]);}
                 catch (Exception e) {return false;}
                 i+=2;
             }
             else if (args[i].equals("-N")) {
                 try {normal = parseNat(args[i + 1]);}
                 catch (Exception e) {return false;}
                 fNormal = true;
                 i+=2;
             }
             if (args[i].equals("-max")) {
                 try {max = parseNat(args[i + 1]);}
                 catch (Exception e) {return false;}
                 i+=2;
             }
             else if (args[i].equals("-MAX")) {
                 try {max = parseNat(args[i + 1]);}
                 catch (Exception e) {return false;}
                 fMax = true;
                 i+=2;
             }
             if (args[i].equals("-t")) {
                 try {time = parseNat(args[i + 1]);}
                 catch (Exception e) {return false;}
                 i+=2;
             }
             else if (args[i].equals("-T")) {
                 try {time = parseNat(args[i + 1]);}
                 catch (Exception e) {return false;}
                 fTime = true;
                 i+=2;
             }
             try {
                 cmdPricelist(parseDouble(args[1]), parseDouble(args[2]), parseDouble(args[3]),
                         normal, fNormal, max, fMax, time, fTime,
                         args[i], i+1, args);
             } catch (Exception e) {
             }
         } else if (args[0].equals(CMD_SHOP)) {
         } else if (args[0].equals(CMD_EVENT)) {
             boolean withstatic = false;
             int i = 1;
             try {
                 if (args[1].equals("-s")) {
                     withstatic = true;
                     i = 2;
                 }
                 cmdEvent(args, i, withstatic);
             } catch (IndexOutOfBoundsException e) {
                 argErr(2);
             }
         } else if (args[0].equals(CMD_HELP)) {
             if (args.length == 1) {
                 logCommands();
             } else {
                 if (args[1].equals(CMD_PRICELIST)) {
                     log(CMD_PRICELIST_HELP);
                 } else if (args[1].equals(CMD_SHOP)) {
                     log(CMD_SHOP_HELP);
                 } else if (args[1].equals(CMD_EVENT)) {
                     log(CMD_EVENT_HELP);
                 } else {
                     log("The command '" + args[1] + "' does not exist!");
                 }
             }
 //        } else if (args[0].equals(CMD_EXIT)) {
 //            return false;
         } else {
             log("The command '" + args[0] + "' does not exist!");
         }
         return true;
     }
 
     private static void argErr(int a) {
         log("Incorrect number of arguments! Sould be at least " + a + ".");
     }
 
     private static void logCommands() {
         log("Available commands:");
         log(CMD_PRICELIST_SYNTAX);
         //log(CMD_SHOP_SYNTAX);
         log(CMD_EVENT_SYNTAX);
         //log("exit to quit");
         log(CMD_HELP + " [command] for help on specific command");
 
     }
 
     private static void cmdPricelist(double factor, double interestBuy, double interestSell,
             int normal, boolean forceNormal, int max, boolean forceMax, int time, boolean forceTime,
             String dest, int i, String[] files) {
         LinkedList<PriceList> pricelists = new LinkedList<PriceList>();
         for (; i < files.length; i++) {
             try {
                 pricelists.add(new PriceList(new File(files[i])));
             } catch (Exception e) {
                 //do not add pricelist - it cannot be loaded correctly from args[i]
             }
         }
         PriceList p = new PriceList(pricelists, factor, interestBuy, interestSell,
                 normal, forceNormal, max, forceMax, time, forceTime);
         p.save(dest);
     }
 
     //Generate the shop events of shop files given in 'files'
     private static void cmdEvent(String[] args, int pos, boolean withstatic) {
         File file = new File(args[pos]);
         LinkedList<ShopInterface> shops = new LinkedList<ShopInterface>();
         for (pos++; pos < args.length; pos++) {
             try {
                 shops.add(new Shop(new File(args[pos])));
             } catch (Exception e) {
                 //do not add shop - it cannot be loaded correctly from files[i]
             }
         }
         
         if (shops.isEmpty()) {
             warn("None of the specified shops could be created - nothing to do!");
             return;
         }
         
         String events = EventGenerator.generateShops(shops, withstatic);
         info("Successfully generated events!");
         //save events to file
         FileWriter out;
         try {
             out = new FileWriter(file);
             out.write(events);
             out.close();
             info("Events saved to \"" + file.toString() + "\"!");
         } catch (IOException e) {
             err("I/O error: " + e.getMessage());
         }
     }
 
     private static double parseDouble(String in) throws Exception {
         try {
             return Double.parseDouble(in);
         } catch (NumberFormatException e) {
             err("Value '" + in + "' should be a floating-point number!");
             throw new Exception();
         }
     }
 
     private static int parseNat(String in) throws Exception {
         try {
             int i = Integer.parseInt(in);
             if (i < 0) {
                 throw new Exception();
             }
             return i;
         } catch (NumberFormatException e) {
             err("Value '" + in + "' should be a natural number!");
             throw new Exception();
         }
     }
 }
