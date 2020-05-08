 /**
  * 
  */
 package stockwatch;
 
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 import java.util.Vector;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import ParserExceptions.ParserException;
 
 public class WarsawStockExchangeParser implements QuotesParser {
 
     WSEInternalMarkets wseInternalMarkets;
    private boolean parseSecuritiesNames;
 
     public WarsawStockExchangeParser() {
         wseInternalMarkets = new WSEInternalMarkets();
        parseSecuritiesNames = true;
     }
 
     void checkConsistency(Elements parsedElems, Vector<Security> companies) throws ParserException {
         if (parsedElems.size() != companies.size()) {
             throw new ParserException("Difference beetween parsed elements size and companies number");
         }
     }
 
     private void parseCompanies(Document parsedDocument, Vector<Security> market) {
         Elements companies = parsedDocument.getElementsByClass("nazwa");
         for (Element company : companies) {
             Shares newCompany = new Shares();
             newCompany.setSecurityName(company.text());
             market.addElement(newCompany);
         }
     }
 
     private void parseComapaniesIds(Document parsedDocument, Vector<Security> market) {
         Elements l_params = parsedDocument.getElementsByAttribute("onClick");
         int i = 0;
         for (Element src : l_params) {
             String[] l_id = src.attributes().get("onClick").split("'");
             if (l_id.length > 4) {
                 market.elementAt(i).setSecurityId(l_id[5]);
             }
             i++;
         }
     }
 
     private void parseLastTransactionPrice(Document parsedDocument, Vector<Security> market) throws ParserException {
         Elements closePrices = parsedDocument.getElementsByClass("c");
         checkConsistency(closePrices, market);
 
         int i = 0;
         for (Element closePrice : closePrices) {
             market.elementAt(i).setLastTransactionPrice(closePrice.text());
             i++;
         }
     }
 
     private void parseOpenPrice(Document parsedDocument, Vector<Security> market) throws ParserException {
         Elements openPrices = parsedDocument.getElementsByClass("o");
         checkConsistency(openPrices, market);
 
         int i = 0;
         for (Element openPrice : openPrices) {
             market.elementAt(i).setOpenPrice(openPrice.text());
             i++;
         }
     }
 
     private void parsePercentageChange(Document parsedDocument, Vector<Security> market) throws ParserException {
         Elements percentageChanges = parsedDocument.getElementsByClass("zmiana");
         checkConsistency(percentageChanges, market);
 
         int i = 0;
         for (Element change : percentageChanges) {
             market.elementAt(i).setPercentageChange(change.text());
             i++;
         }
     }
 
     private void parseLastTransactionTime(Document parsedDocument, Vector<Security> market) throws ParserException {
         Elements lastTimeTransaction = parsedDocument.getElementsByClass("czas");
         checkConsistency(lastTimeTransaction, market);
 
         int i = 0;
         for (Element time : lastTimeTransaction) {
             market.elementAt(i).setLastChangedTime(time.ownText());
             i++;
         }
     }
 
     private void parse(Vector<Security> market, String pageAddr) {
         try {
             Document sourceDocument = Jsoup.connect(pageAddr).get();
 
            if (parseSecuritiesNames) {
                parseCompanies(sourceDocument, market);
                parseComapaniesIds(sourceDocument, market);
            }

             parseOpenPrice(sourceDocument, market);
             parseLastTransactionPrice(sourceDocument, market);
             parsePercentageChange(sourceDocument, market);
             parseLastTransactionTime(sourceDocument, market);
 
         } catch (SocketTimeoutException e) {
             System.out.print(e.getMessage());
             e.printStackTrace();
         } catch (ParserException e) {
             System.out.print(e.getMessage());
             e.printStackTrace();
         } catch (IOException e) {
             System.out.print(e.getMessage());
             e.printStackTrace();
         }
     }
 
     @Override
     public InternalMarkets parseQuotes() {
         
         // Iterate over all internal markets of WSE and parse it's quotes.
         WseMarketTypes allMarkets[] = WseMarketTypes.values();
         for (WseMarketTypes market : allMarkets) {
             parse(wseInternalMarkets.getQuotes().get(market.name()), market.getAddress());
         }
        parseSecuritiesNames = false;
         return wseInternalMarkets;
     }
 
     @Override
     public void parseInfo() {
         // TODO Auto-generated method stub
 
     }
 
 }
