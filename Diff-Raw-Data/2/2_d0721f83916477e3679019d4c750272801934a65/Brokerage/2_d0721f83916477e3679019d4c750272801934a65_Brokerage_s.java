 /**
  * Copyright 2012 Winflow Financial Group Corporation.
  *      http://www.winflowfinancial.com
  */
 package parsers;
 
 import com.avaje.ebean.text.csv.CsvCallback;
 import com.avaje.ebean.text.csv.CsvReader;
 import models.ActiveTrade;
 import models.TradeProperty;
 import utils.WinFlowUtil;
 
 import java.util.List;
 
 /**
  * @author Jatinder Singh on 2012-11-02 at 9:00 PM
  */
 public class Brokerage {
 
     /**
     * Adds broker fields to save into db
      *
      * @param csvReader
      * @param brokerage
      * @return
      */
     public static CsvReader<ActiveTrade> addBrokerProperties(CsvReader<ActiveTrade> csvReader, String brokerage) {
         List<TradeProperty> tradeProperties = null;
         switch (BrokerageType.valueOf(brokerage)) {
             case FIDELITY:
                 csvReader.setIgnoreHeader();
                 tradeProperties = TradeProperty.findByBrokerageType(BrokerageType.FIDELITY);
                 for (TradeProperty tradeProperty : tradeProperties) {
                     if(tradeProperty.ignore) {
                         csvReader.addIgnore();
                     } else if(tradeProperty.isDate) {
                         csvReader.addDateTime(tradeProperty.name, WinFlowUtil.getStringDateFormat());
                     } else {
                         csvReader.addProperty(tradeProperty.name);
                     }
                 }
                 break;
             case IB:
                 csvReader.setIgnoreHeader();
                 tradeProperties = TradeProperty.findByBrokerageType(BrokerageType.IB);
                 for (TradeProperty tradeProperty : tradeProperties) {
                     if (tradeProperty.ignore) {
                         csvReader.addIgnore();
                     } else if (tradeProperty.isDate) {
                         csvReader.addDateTime(tradeProperty.name, WinFlowUtil.getStringDateFormat());
                     } else {
                         csvReader.addProperty(tradeProperty.name);
                     }
                 }
                 break;
             case MERRILL:
                 csvReader.setIgnoreHeader();
                 tradeProperties = TradeProperty.findByBrokerageType(BrokerageType.IB);
                 for (TradeProperty tradeProperty : tradeProperties) {
                     if (tradeProperty.ignore) {
                         csvReader.addIgnore();
                     } else if (tradeProperty.isDate) {
                         csvReader.addDateTime(tradeProperty.name, WinFlowUtil.getStringDateFormat());
                     } else {
                         csvReader.addProperty(tradeProperty.name);
                     }
                 }
                 break;
             default:
                 break;
         }
 
         return csvReader;
     }
 
     /**
      * Returns a concrete parser based on brokerage type
      *
      * @param brokerage
      * @return
      */
     public static CsvCallback<ActiveTrade> getParser(String brokerage) {
         switch (BrokerageType.valueOf(brokerage)) {
             case FIDELITY:
                 return new FidelityCsvParser<ActiveTrade>();
             case IB:
                 return new InteractiveBrokerCsvParser<ActiveTrade>();
             case MERRILL:
                 return new MerrillCsvParser<ActiveTrade>();
             default:
                 break;
         }
 
         return null;
     }
 }
