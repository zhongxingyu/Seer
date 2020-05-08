 package swp.web;
 
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import swp.model.AuctionPaymentRequest;
 import swp.service.factory.ServiceFactory;
 import dk.brics.jwig.BadRequestException;
 import dk.brics.jwig.URLPattern;
 import dk.brics.jwig.WebApp;
 import dk.brics.xact.XML;
 
 @URLPattern("pay")
 public class PayApp extends WebApp{
     
     
     // TODO add access control
     @URLPattern("")
     public XML execute(String auctionserver, String item, String returnurl){
         try {
             URL host = new URL(auctionserver);
             URI id = new URI(item);
             AuctionPaymentRequest paymentRequest = ServiceFactory.getInstance().getPaymentRequestService().load(host, id);
             XML result = createPaymentXMLType();
             result = result.plug("ITEM_NAME", paymentRequest.getItemName());
             result = result.plug("ITEM_PRICE", paymentRequest.getPrice());
             result = result.plug("BUYER", paymentRequest.getBuyer());
             result = result.plug("AUCTION_SERVER", auctionserver);
             result = result.plug("ITEM_ID", item);
             result = result.plug("RETURN_URL", returnurl);
             result = result.plug("TITLE", "Payment Request for " + paymentRequest.getItemName());
             return result.close();
         } catch (MalformedURLException e) {
             throw new BadRequestException(e.getMessage());
         } catch (URISyntaxException e) {
             throw new BadRequestException(e.getMessage());
         }
     }
     
     private XML makePaymentSummary(AuctionPaymentRequest item){
         return null;
     }
     
     private XML testMakePaymentSummary(){
         XML result = createPaymentXMLType();
         result = result.plug("ITEM_NAME", "Bar");
         result = result.plug("ITEM_PRICE", "100");
         return result.plug("BUYER", "HANS");
     }
     
     private XML createPaymentXMLType(){
         XML result = getWrapper();
         result = result.plug("BODY", XML.parseTemplate(
                 "You are the winner of the auction for <[ITEM_NAME]>" +
                 "<xhtml:p>The price for the item is <xhtml:b><[ITEM_PRICE]></xhtml:b></xhtml:p>" +
                 "<xhtml:p>Press the button below to complete the payment. " +
                 "The money will be withdrawn from your account</xhtml:p>" +
                 "<xhtml:form action=\"payItem\" method=\"POST\">" +
                     "<xhtml:input type=\"hidden\" name=\"item\" value=[ITEM_ID]/>" +
                     "<xhtml:input type=\"hidden\" name=\"auctionserver\" value=[AUCTION_SERVER]/>" +
                     "<xhtml:input type=\"hidden\" name=\"returnurl\" value=[RETURN_URL]/>" +
                     "<xhtml:input type=\"hidden\" name=\"buyer\" value=[BUYER]/>" +
                     "<xhtml:input  type=\"submit\" value=\"Accept purchase\"/>" +
                 "</xhtml:form>"));
         return result;
     }
     
     private XML getWrapper(){
         XML.getNamespaceMap().put("xhtml", "http://www.w3.org/1999/xhtml");
         return XML.parseTemplate(
                 "<xhtml:html>" +
                     "<xhtml:head>" +
                         "<xhtml:title>" +
                             "<[TITLE]>" +
                         "</xhtml:title>" +
                     "</xhtml:head>" +
                     "<xhtml:body>" +
                         "<[BODY]>" +
                     "</xhtml:body>" +
                 "</xhtml:html>");
     }
     
 
 }
