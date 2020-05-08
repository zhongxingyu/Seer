 package com.scotbaldry.msmoneytools.parsers;
 
 import com.scotbaldry.msmoneytools.SecurityPrice;
 import com.scotbaldry.msmoneytools.parsers.MapperParser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Class that is able to parse the CSV files produced by the Holdings & Transactions link within the Fidelity My Accounts page
  */
 public class FidelityHoldingsCSVParser implements IParser {
     private static String[] _headerFormat = {"Provider", "Holding", "Income status", "Price per unit", "Date", "Units", "Holding valuation", "Holding currency code", "Reporting valuation", "Reporting currency code"};
    private static String[] _columns = {"", "", "", "", ""};
 
     private MapperParser _mapper;
     private Date _valuationDate;
     private Map<String, String[]> _prices = new HashMap<>();
 
     public FidelityHoldingsCSVParser(MapperParser mapper) {
         _mapper = mapper;
     }
 
     public String[] getColumns() {
        return null;
     }
 
     public String[] getHeader() {
         return _headerFormat;
     }
 
     public void parse(File filename) throws Exception {
         String line = "";
         String cvsSplitBy = ",";
         boolean processingBody = false;
         int headerRow = 0;
         int bodyRow = 0;
 
         try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
             while ((line = br.readLine()) != null) {
                 // use comma as separator
                 String[] columns = line.split(cvsSplitBy);
 
                 if (!processingBody) {
                     // Extract statement date from first role in the file
                     if (headerRow == 0) {
                         SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm");
                         _valuationDate = dateFormat.parse(columns[0]);
                         headerRow++;
                         continue;
                     }
                     else if (columns.length < 1 || !"Provider".equals(columns[0])) {
                         headerRow++;
                         continue;
                     }
                 }
 
                 processingBody = true;
 
                 if (bodyRow == 0) {
                     validateHeader(columns);
                 }
                 else {
                     _prices.put(columns[1], columns);
                 }
 
                 bodyRow++;
             }
         }
     }
 
     /**
      * Method to return all parsed data in a simple 2 dimensional array suitable for
      * rendering in a table or similar data component.
      *
      * @return a 2 dimensional array of Objects (columns x rows)
      */
     public Object[][] getData() throws ParseException {
         List<SecurityPrice> securityPrices = getSecurityPrices();
         Object[][] data = new Object[securityPrices.size()][5];
 
         int i = 0;
         for (SecurityPrice price : securityPrices) {
             data[i][0] = price.getSymbol();
             data[i][1] = price.getSecurityName();
             data[i][2] = price.getPrice();
             data[i][3] = price.getCurrency();
             data[i][4] = price.getDate();
             i++;
         }
 
         return data;
     }
 
     public List<SecurityPrice> getSecurityPrices() throws ParseException {
         List<SecurityPrice> securityPriceList = new ArrayList<>();
 
         for (String s : _prices.keySet()) {
             SecurityPrice securityPrice;
             String[] columns = _prices.get(s);
 
             String mappedSymbol = _mapper.getSecurityNameIndex().get(columns[1]).getToSymbol();
             String mappedName = _mapper.getSecurityNameIndex().get(columns[1]).getToSecurityName();
 
             securityPrice = new SecurityPrice(mappedSymbol,                  // Symbol
                                               mappedName,                    // Security Name
                                               columns[3],                    // Price
                                               columns[7],                    // Currency
                                               convertDate(columns[4]),       // Date
                                               "Price as of close of business");
 
             securityPriceList.add(securityPrice);
         }
 
         return securityPriceList;
     }
 
     public Date getValuationDate() {
         return _valuationDate;
     }
 
     public int getRowCount() {
         return _prices.keySet().size();
     }
 
     private boolean validateHeader(String[] columns) {
         if (_headerFormat.length != columns.length) {
             return false;
         }
 
         for (int i = 0; i < _headerFormat.length; i++) {
             if (!_headerFormat[i].equals(columns[i])) {
                 return false;
             }
         }
 
         return true;
     }
 
     private Date convertDate(String input) throws ParseException {
         // Dates are stored as DD/MM/YYYY in the incoming file
         SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
         return formatter.parse(input);
     }
 }
