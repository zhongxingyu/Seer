 package com.edsdev.jconvert.common;
 
 import java.io.IOException;
 import java.net.URLConnection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.edsdev.jconvert.domain.Conversion;
 import com.edsdev.jconvert.domain.ConversionType;
 import com.edsdev.jconvert.logic.ConversionGapBuilder;
 import com.edsdev.jconvert.persistence.DataLoader;
 import com.edsdev.jconvert.util.Http;
 import com.edsdev.jconvert.util.Logger;
 
 /**
  * This class implements the CustomConversionDataInterface and is responsible for gathering custom conversions. In
  * particular, this class will download the latest currency conversions from an external website. Use this class as an
  * example if you want to create your own interface to the application.
  * 
  * @author Ed Sarrazin Created on Nov 2, 2007 4:00:57 PM
  */
 public class CustomConversionCurrency extends CustomConversionImpl implements CustomConversionDataInterface {
 
     private static final String theUrl = "http://moneycentral.msn.com/investor/market/exchangerates.aspx";
 
     private static final String TAB_NAME = "Currency";
 
     private static final Logger log = Logger.getInstance(CustomConversionCurrency.class);
 
     public CustomConversionCurrency() {
         super();
         Runnable timer = new Runnable() {
             public void run() {
                 while (true) {
                     try {
                         Thread.sleep(30 * 60 * 1000);
                     } catch (Exception e) {
 
                     }
                     CustomConversionCurrency.this.fireDataUpdatedEvent();
                 }
             }
         };
         Thread runIt = new Thread(timer);
         runIt.start();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.edsdev.jconvert.common.CustomConversionDataInterface#getConversions()
      */
     public ConversionType getConversions() {
         ConversionType rv = updateData();
         rv.setTypeName(TAB_NAME);
         System.out.println(rv.getTypeName());
         ConversionGapBuilder.createOneToOneConversions(rv);
         ConversionGapBuilder.createMissingConversions(rv);
         DataLoader dataLoader = new DataLoader();
         ArrayList list = new ArrayList();
         list.add(rv);
         dataLoader.unloadData(list, "MSNCurrency.dat");
         return rv;
     }
 
     private ConversionType updateData() {
         StringBuffer loadedUpdate = null;
         ConversionType ct = new ConversionType();
         try {
             URLConnection uc = Http.getPage(theUrl);
             loadedUpdate = Http.receivePage(uc, "UTF-8");
         } catch (IOException e) {
             log.info("Unable to connect to the internet.  Loading currency data from file.");
             DataLoader dataLoader = new DataLoader();
             List conversionTypes = dataLoader.loadData("MSNCurrency.dat", false, null);
             if (conversionTypes.size() == 0) {
                 log.error("Could not find file to load previous currency data and cannot connect to the internet, hence cannot display currency data.");
                 ct.setTypeName(TAB_NAME);
                 return ct;
             }
             ct = (ConversionType) conversionTypes.get(0);
             setLastUpdated(new Date());
             loadedUpdate = null;
             e.printStackTrace();
             return ct;
         }
 
         ArrayList conversionUnits = new ArrayList();
         ArrayList factor = new ArrayList();
 
         conversionUnits.add("US Dollar");
         factor.add("1");
 
        String startLookingHere = ">Per  US Dollar</th>";
         int start = loadedUpdate.indexOf(startLookingHere) + startLookingHere.length();
 
         String headerDivider = "</tr><tr><td>";
        String colDivider = " class=\"c2\">";
        start = loadedUpdate.indexOf(headerDivider, start) + headerDivider.length();
         int lastStart = start;
         while (start > 0) {
             String data = getNextPlainData(loadedUpdate, start);
             conversionUnits.add(htmlToUnicode(data));
 
             start = loadedUpdate.indexOf(colDivider, start) + colDivider.length();
             //grab second column
             start = loadedUpdate.indexOf(colDivider, start) + colDivider.length();
             String fctr = getNextPlainData(loadedUpdate, start);
             factor.add(fctr);
 
             lastStart = start;
             start = loadedUpdate.indexOf(headerDivider, start);
             if (start > 0) {
                 start += headerDivider.length();
             }
         }
         String timeIdentifier = "<div>Page generated";
         start = loadedUpdate.indexOf(timeIdentifier, lastStart) + timeIdentifier.length();
         String time = getNextPlainData(loadedUpdate, start);
 
         try {
             for (int i = 0; i < conversionUnits.size(); i++) {
                 ct.addConversion(Conversion.createInstance(conversionUnits.get(0).toString(), "",
                     conversionUnits.get(i).toString(), "", factor.get(i).toString().replaceAll(",", ""), 0));
                 //                System.out.println(conversionUnits.get(0) + " * " + factor.get(i) + " = " + conversionUnits.get(i));
             }
 
             SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
             setLastUpdated(dateFormat.parse(time));
         } catch (Exception e) {
             e.printStackTrace();
         }
         return ct;
     }
 
 }
