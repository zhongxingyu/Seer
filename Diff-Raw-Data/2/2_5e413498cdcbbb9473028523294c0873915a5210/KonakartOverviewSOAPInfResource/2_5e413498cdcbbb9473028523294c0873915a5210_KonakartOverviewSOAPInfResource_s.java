 /*
  * Copyright 2010 Wyona
  */
 
 package org.wyona.yanel.resources.konakart.overview;
 
 import org.wyona.yanel.core.api.attributes.AnnotatableV1;
 import org.wyona.yanel.core.api.attributes.TrackableV1;
 import org.wyona.yanel.core.attributes.tracking.TrackingInformationV1;
 import org.wyona.yanel.impl.resources.BasicXMLResource;
 import org.wyona.yanel.resources.konakart.shared.SharedResource;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 import org.apache.log4j.Logger;
 
 import com.konakart.appif.KKEngIf;
 import com.konakart.appif.CustomerIf;
 import com.konakart.appif.AddressIf;
 import com.konakart.appif.BasketIf;
 import com.konakart.appif.OrderIf;
 import com.konakart.appif.OrderTotalIf;
 import com.konakart.appif.ShippingQuoteIf;
 import com.konakart.appif.OrderStatusHistoryIf;
 import com.konakart.appif.OrderProductIf;
 import com.konakart.appif.OrderTotalIf;
 import com.konakart.app.OrderStatusHistory;
 import com.konakart.app.OrderTotal;
 
 import org.w3c.dom.Element;
 
 import org.wyona.yanel.core.attributes.viewable.View;
 import javax.servlet.http.HttpServletResponse;
 
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import java.util.Arrays;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import java.math.BigDecimal;
 import java.net.URLDecoder;
 
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 import org.xml.sax.SAXException;
 import org.xml.sax.InputSource;
 
 /**
  * Show overview before buying products and also the actual order process
  */
 public class KonakartOverviewSOAPInfResource extends BasicXMLResource implements AnnotatableV1, TrackableV1 {
     
     private static Logger log = Logger.getLogger(KonakartOverviewSOAPInfResource.class);
     private static String KONAKART_NAMESPACE = "http://www.konakart.com/1.0";
 
     private BranchListHandler handler; // Info: For parsing branches xml file.
 
     private TrackingInformationV1 trackInfo;
 
     /**
      * @see org.wyona.yanel.impl.resources.BasicXMLResource#getContentXML(String)
      */
     @Override
     protected InputStream getContentXML(String viewId) throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("requested viewId: " + viewId);
         }
 
         if (trackInfo != null) {
             String[] annotations = getAnnotations();
             if (annotations != null) {
                 for (int i = 0; i < annotations.length; i++) {
                     trackInfo.addTag(annotations[i]);
                 }
             } else {
                 log.error("No annotations!");
             }
             trackInfo.setPageType("konakart-order-overview");
         } else {
             log.warn("Tracking information bean is null! Check life cycle of resource!");
         }
 
         SharedResource shared = new SharedResource();
         KKEngIf kkEngine = shared.getKonakartEngineImpl();
         KKEngIf kkEngineBranch = null; // Will be initialized later (if in multi-store mode)
         //boolean multistore = false; // Will have to be checked after reading delivery address
         String sessionId = shared.getSessionId(getEnvironment().getRequest().getSession(true));
         int customerId = shared.getCustomerId(getEnvironment().getRequest().getSession(true));
         int languageId = shared.getLanguageId(getContentLanguage());
         OrderIf orderDefault = null;         // Order object which will be filled with totals
 
         // Build document
         org.w3c.dom.Document doc = null;
         try {
             doc = org.wyona.commons.xml.XMLHelper.createDocument(KONAKART_NAMESPACE, "overview");
         } catch (Exception e) {
             throw new Exception(e.getMessage(), e);
         }
 
         // Root element
         Element rootElement = doc.getDocumentElement();
 
         // We'll need these later
         BasketIf[] items = null;
         ShippingQuoteIf shipping = null;
 
 
         // Get customer
         CustomerIf customer = shared.getCustomer(sessionId, customerId); 
 
         if(customer == null) {
             Element exception = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "not-logged-in-yet"));
             java.io.ByteArrayOutputStream baout = new java.io.ByteArrayOutputStream();
             org.wyona.commons.xml.XMLHelper.writeDocument(doc, baout);
             return new java.io.ByteArrayInputStream(baout.toByteArray());
         } 
         
         Element mailElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "email"));
         mailElem.appendChild(doc.createTextNode("" + customer.getEmailAddr())); 
 
         // Get payment (=default) address
         AddressIf defaddr = kkEngine.getDefaultAddressPerCustomer(sessionId);
     
         if(defaddr == null) {
             // Well, there's no default address - just make the first address
             // we have in the database the default address from now on (if there's
             // no default address this is most likely a newly registered user).
             AddressIf[] addrs = customer.getAddresses();
             // There has to be at least one address, Konakart doesn't let you register otherwise.
             defaddr = addrs[0];
             kkEngine.setDefaultAddressPerCustomer(sessionId, defaddr.getId());
         }
     
         // Now print that address information
         Element defAddrElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "payment-address"));
         Element defAddrNameElem = (Element) defAddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "last-name"));
         defAddrNameElem.appendChild(doc.createTextNode("" + defaddr.getLastName())); 
         Element defAddrfNameElem = (Element) defAddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "first-name"));
         defAddrfNameElem.appendChild(doc.createTextNode("" + defaddr.getFirstName())); 
         Element defAddrCompanyElem = (Element) defAddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "company"));
         defAddrCompanyElem.appendChild(doc.createTextNode("" + defaddr.getCompany())); 
         Element defAddrCityElem = (Element) defAddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "city"));
         defAddrCityElem.appendChild(doc.createTextNode("" + defaddr.getCity())); 
         Element defAddrCountryElem = (Element) defAddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "country"));
         defAddrCountryElem.appendChild(doc.createTextNode("" + defaddr.getCountryName())); 
         Element defAddrStreetElem = (Element) defAddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "street"));
         defAddrStreetElem.appendChild(doc.createTextNode("" + defaddr.getStreetAddress())); 
         Element defAddrGenderElem = (Element) defAddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "gender"));
         defAddrGenderElem.appendChild(doc.createTextNode("" + defaddr.getGender())); 
         Element defAddrPostcodeElem = (Element) defAddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "postcode"));
         defAddrPostcodeElem.appendChild(doc.createTextNode("" + defaddr.getPostcode())); 
         Element defAddrPhoneElem = (Element) defAddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "telephone"));
         defAddrPhoneElem.appendChild(doc.createTextNode("" + customer.getTelephoneNumber())); 
 
         // Get delivery (=default) address
         AddressIf[] addrs = kkEngine.getAddressesPerCustomer(sessionId);
         int devaddrid = Integer.parseInt(customer.getCustom1());
         AddressIf devaddr = null;
     
         for(AddressIf a : addrs) {
             if(a.getId() == devaddrid) {
                 devaddr = a;
             }
         }
 
         if(devaddr == null) devaddr = defaddr;
 
         // Now print that address information
         Element devaddrElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "delivery-address"));
         Element devaddrNameElem = (Element) devaddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "last-name"));
         devaddrNameElem.appendChild(doc.createTextNode("" + devaddr.getLastName())); 
         Element devaddrfNameElem = (Element) devaddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "first-name"));
         devaddrfNameElem.appendChild(doc.createTextNode("" + devaddr.getFirstName())); 
         Element devaddrCompanyElem = (Element) devaddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "company"));
         devaddrCompanyElem.appendChild(doc.createTextNode("" + devaddr.getCompany())); 
         Element devaddrCityElem = (Element) devaddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "city"));
         devaddrCityElem.appendChild(doc.createTextNode("" + devaddr.getCity())); 
         Element devaddrCountryElem = (Element) devaddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "country"));
         devaddrCountryElem.appendChild(doc.createTextNode("" + devaddr.getCountryName())); 
         Element devaddrStreetElem = (Element) devaddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "street"));
         devaddrStreetElem.appendChild(doc.createTextNode("" + devaddr.getStreetAddress())); 
         Element devaddrGenderElem = (Element) devaddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "gender"));
         devaddrGenderElem.appendChild(doc.createTextNode("" + devaddr.getGender())); 
         Element devaddrPostcodeElem = (Element) devaddrElem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "postcode"));
         devaddrPostcodeElem.appendChild(doc.createTextNode("" + devaddr.getPostcode())); 
 
         String remarks = (String) getEnvironment().getRequest().getSession(true).getAttribute("checkout-data-remarks");
         if(remarks != null) appendField("remarks", remarks, rootElement, doc);
 
 
         boolean process = getEnvironment().getRequest().getParameter("process") != null;
         String confirmParam = getEnvironment().getRequest().getParameter("confirm");
         boolean confirmGeneralTermsConditions = false;
         if (confirmParam != null) {
             log.warn("DEBUG: Confirm general terms and conditions: " + confirmParam);
             confirmGeneralTermsConditions = true;
         }
         if(process && !confirmGeneralTermsConditions) {
             log.warn("General terms and conditions have not been accepted, hence do not process order...");
             process = false;
             Element perrElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "terms-not-accepted-yet"));
             perrElem.appendChild(doc.createTextNode("General terms and conditions have not been accepted!"));
         }
 
 
         if(process) { // INFO: Check whether order should be processed ...
             // If process is true, we're creating and submitting this order
             // at the same time as we are displaying the information. So first,
             // we need the items in the basket to create an order object.
 
             try {
                 int tmpCustomerId = shared.getTemporaryCustomerId(getEnvironment().getRequest().getSession(true));
                 items = kkEngine.getBasketItemsPerCustomer(null, tmpCustomerId, languageId);
 
                 // Create orders
                 log.warn("DEBUG: Create default order ...");
                 orderDefault = kkEngine.createOrder(sessionId, items, languageId);
                 //OrderIf orderDefault = kkEngine.createOrder(sessionId, items, languageId);
                 shipping = shared.getShippingCost(items, sessionId, languageId, getEnvironment().getRequest().getSession(true));
                 orderDefault.setShippingQuote(shipping);
                 orderDefault = kkEngine.getOrderTotals(orderDefault, languageId);
 
                 fixOrderTotals(orderDefault, shipping);
                 setOrderAddressFields(orderDefault, shipping, devaddr, defaddr);
 
                 // Status trail
                 OrderStatusHistoryIf[] trail = new OrderStatusHistoryIf[1];
                 trail[0] = new OrderStatusHistory();
                 trail[0].setOrderStatus("New order.");
                 trail[0].setCustomerNotified(true);
 
                 orderDefault.setStatusTrail(trail);
             } catch(Exception e) {
                 process = false;
                 log.error(e, e);
                 Element perrElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "process-error"));
                 if(e.getMessage() != null) perrElem.appendChild(doc.createTextNode(e.getMessage()));
             }
             if (trackInfo != null) {
                 trackInfo.setRequestAction("order-processed");
             } else {
                 log.warn("Tracking information bean is null! Check life cycle of resource!");
             }
         } else {
             log.warn("DEBUG: Order either not submitted or processed yet. Continue ...");
             if (trackInfo != null) {
                 trackInfo.setRequestAction("review-order");
             } else {
                 log.warn("Tracking information bean is null! Check life cycle of resource!");
             }
         }
 
         // Payment info
         javax.servlet.http.HttpSession session = getEnvironment().getRequest().getSession(true);
         String type = (String) session.getAttribute("checkout-card-data-type");
         String payment_info_kk = null;
         String payment_info_mail = null;
 
         if("Pluscard".equals(type)) {
             // Pluscard
             Element pc = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "payment-method-pluscard"));
             Element numberElem = (Element) pc.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "number"));
             String number = (String) session.getAttribute("checkout-card-data-number");
             number = number.replace("[^0-9]+", "");
             String num_prefix, num_suffix;
 
             String suffix = number.substring(number.length()-4);
             num_suffix = "..." + suffix;
             num_prefix = number.substring(0, number.length()-4) + "****";
 
             try {
                 // It's +7 because pluscards have a 7 digit prefix
                 char[] asterisks = new char[number.length()-4+7];
                 Arrays.fill(asterisks, '*');
                 String asterisks_str = new String(asterisks);
                 numberElem.appendChild(doc.createTextNode(asterisks_str + suffix));
             } catch(Exception e) { }
 
             String name = (String) session.getAttribute("checkout-card-data-name");
             Element nameElem = (Element) pc.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "name"));
             nameElem.appendChild(doc.createTextNode(name));
             String valid = (String) session.getAttribute("checkout-card-data-valid");
             Element validElem = (Element) pc.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "valid"));
             validElem.appendChild(doc.createTextNode(valid));
 
             if(process) {
                 try {
                     orderDefault.setPaymentMethod("Pluscard");
                     payment_info_kk = num_suffix + " (" + valid + ")";
                     payment_info_mail = "6004512-" + num_prefix;
                 } catch(Exception e) {
                     process = false;
                     log.error(e, e);
                     Element perrElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "process-error"));
                     if(e.getMessage() != null) perrElem.appendChild(doc.createTextNode(e.getMessage()));
                 } 
             }
         } else {
             // Credit card
             Element cc = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "payment-method-creditcard"));
             Element numberElem = (Element) cc.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "number"));
             String number = (String) session.getAttribute("checkout-card-data-number");
             number = number.replace("[^0-9]+", "");
             String num_prefix, num_suffix, suffix;
 
             suffix = number.substring(number.length()-4);
             num_suffix = "..." + suffix;
             num_prefix = number.substring(0, number.length()-4) + "****";
 
             try {
                 char[] asterisks = new char[number.length()-4];
                 Arrays.fill(asterisks, '*');
                 String asterisks_str = new String(asterisks);
                 numberElem.appendChild(doc.createTextNode(asterisks_str + suffix));
             } catch(Exception e) { }
             
             String name = (String) session.getAttribute("checkout-card-data-name");
             Element nameElem = (Element) cc.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "name"));
             nameElem.appendChild(doc.createTextNode(name));
             String valid = (String) session.getAttribute("checkout-card-data-valid");
             Element validElem = (Element) cc.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "valid"));
             validElem.appendChild(doc.createTextNode(valid));
             Element typeElem = (Element) cc.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "type"));
             typeElem.appendChild(doc.createTextNode(type));
 
             if(process) {
                 try {
                     orderDefault.setPaymentMethod("Creditcard");
                     String snum = number.replace("[^0-9]+", "");
                     payment_info_kk = type + ": " + num_suffix + " (" + valid + ")";
                     String cvc = (String) session.getAttribute("checkout-card-data-cvc");
                     payment_info_mail = type + ": " + num_prefix + "/" + cvc;
                 } catch(Exception e) {
                     process = false;
                     log.error(e, e);
                     Element perrElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "process-error"));
                     if(e.getMessage() != null) perrElem.appendChild(doc.createTextNode(e.getMessage()));
                 } 
             }
 
             String globusCardNumber = (String) session.getAttribute("checkout-globuscard-number");
             if (globusCardNumber != null) {
                Element globusCardNumberElem = (Element) cc.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "globucard-number"));
                 globusCardNumberElem.appendChild(doc.createTextNode(globusCardNumber));
             }
         }
 
         if(process) {
             try {
                 int orderId = kkEngine.saveOrder(sessionId, orderDefault, languageId);
 
                 // Status updates
                 // TODO: Needs to be "moved" to branch store if multistore is true
                 kkEngine.changeOrderStatus(sessionId, orderId, orderDefault.getStatus(), true, "New order."); 
                 if(remarks != null) {
                     try {
                         kkEngine.changeOrderStatus(sessionId, orderId, orderDefault.getStatus(), true, "Remarks: " + remarks); 
                     } catch(Exception e) { 
                         log.error(e, e);
                     }
                 }
                 if(payment_info_kk != null) {
                    kkEngine.changeOrderStatus(sessionId, orderId, orderDefault.getStatus(), false, "Payment details: " + payment_info_kk);
                 }
 
                 // INFO: Check whether ZIP corresponds to a specific store id and hence enable Multi-Store
                 String storeId = getBranchStoreId(devaddr.getPostcode());
                 boolean multistore = storeId != null;
                 if(multistore) {
                     log.warn("DEBUG: Multi-store seems to be enabled: " + devaddr.getPostcode() + ", " + storeId);
                     kkEngineBranch = shared.getKonakartEngineImpl(storeId);
                 } else {
                     log.warn("DEBUG: Multi-store seems to be disabled.");
                 }
                 if(multistore) {
                     OrderIf orderBranch = null; // INFO: Branch order object for multi-store mode
                     log.warn("DEBUG: Trying to create branch order ...");
 
                     log.warn("DEBUG: Realm: " + getRealm().getName() + ", " + getRealm().getConfigFile());
                     log.warn("DEBUG: Konakart repository: " + getRealm().getRepository("konakart-repository").getConfigFile());
                     org.wyona.yarep.core.Repository konakartRepo = getRealm().getRepository("konakart-repository");
                     org.wyona.yarep.core.Node ordersNode = konakartRepo.getNode("/orders/");
                     String language = "en"; // TODO: Not used actually (see implementation ...)
                     //String orderId = "" + orderDefault.getId();
                     //String orderId = orderDefault.getOrderNumber();
                     if (ordersNode.hasNode(orderId + "_" + language)) {
                         org.wyona.yarep.core.Node orderNode = ordersNode.getNode(orderId + "_" + language);
                         log.warn("DEBUG: Switch store ID '" + storeId + "' of order: " + orderId);
                         orderNode.setProperty("store-id", storeId);
                     } else {
                         log.error("No such order with ID: " + orderId);
                     }
 
                     // TODO: Add products to this store and append to category
                     for (OrderProductIf orderProduct : orderDefault.getOrderProducts()) {
                         com.konakart.appif.ProductIf productDefault = orderProduct.getProduct();
                         // INFO: Please note that the ID of an order-product is not the same as the ID of an actual product
                         log.warn("DEBUG: Order product ID: " + orderProduct.getId() + ", Product ID: " + productDefault.getId());
                         log.warn("DEBUG: Category ID of product: " + productDefault.getCategoryId());
 /*
                         int language = 2;
                         com.konakart.appif.CategoryIf[] categories = kkEngine.getCategoriesPerProduct(productDefault.getId(), language);
 */
                         for (String shopId : kkEngine.getStoreIds()) {
                             log.warn("DEBUG: Store ID: " + shopId);
                         }
 /*
                         AdminStore[] = KKAdminIf.getStores();
                         AdminStore = ...
                         AdminProducts = kkAdminIf.searchForProducts(...);
                         AdminProduct = ...
                         //kkEngineBranch
 */
                     }
 
 /*
                     orderBranch = kkEngineBranch.createOrder(sessionId, items, languageId);
                     if (orderBranch != null) {
                         orderBranch.setShippingQuote(shipping);
                         // TODO: Something goes wrong here ...
                         orderBranch = kkEngineBranch.getOrderTotals(orderBranch, languageId);
 
                         fixOrderTotals(orderBranch, shipping);
                         setOrderAddressFields(orderBranch, shipping, devaddr, defaddr);
                         orderBranch.setStatusTrail(trail); // TODO: Add a branch specific trail
 
                         orderBranch.setPaymentMethod("Pluscard");
 
                         int idBranch = kkEngineBranch.saveOrder(sessionId, orderBranch, languageId);
                         log.warn("DEBUG: Branch order has been created: " + kkEngineBranch.getStore().getStoreId());
 
                         // TODO: ...
                         //kkEngine.changeOrderStatus(sessionId, idBranch, orderBranch.getStatus(), true, "New order.");
                         //kkEngine.changeOrderStatus(sessionId, idBranch, orderBranch.getStatus(), false, "Payment details: " + payment_info_kk);
                     } else {
                         log.error("Was not able to create order to branch store: " + storeId);
                     }
 */
                 } // INFO: End multistore
                 
                 Element prElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "process"));
                 prElem.setAttribute("orderid", "" + orderId);
 
                 // Print date
                 Date today = new Date();
                 SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                 String formattedDate = formatter.format(today);
                 Element dateElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "date"));
                 dateElem.appendChild(doc.createTextNode(formattedDate));
 
                 sendMailToCustomer(orderId, orderDefault, customer);
                 sendMailToOwner(orderId, orderDefault, customer, payment_info_mail);
                 // Let Konakart send the order confirmation...
                 // kkEngineBranch.sendOrderConfirmationEmail(sessionId, id, "Order #" + id, languageId);
 
             } catch(Exception e) {
                 process = false;
                 log.error(e, e);
                 Element perrElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "process-error"));
                 if(e.getMessage() != null) perrElem.appendChild(doc.createTextNode(e.getMessage()));
             }    
         }
 
         // Output
         java.io.ByteArrayOutputStream baout = new java.io.ByteArrayOutputStream();
         org.wyona.commons.xml.XMLHelper.writeDocument(doc, baout);
         return new java.io.ByteArrayInputStream(baout.toByteArray());
     }
 
     /**
      * Generate mail body/content.
      */
     private void getMailContent(StringBuilder content, int id, OrderIf order, CustomerIf customer) { 
         // Order info
         // Note: I would have liked to use order.getDatePurchased here, 
         // but it always gives me a null pointer exception...
         // And yes, I know, embedded HTML is scary :-(
         SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
         Date today = new Date();
         String formattedDate = formatter.format(today);
         content.append("<br/><br/><strong>Bestellung vom / Commande du: " + formattedDate);
         content.append("</strong><br/>Bestellung Nr. / Commande n°: " + id);
         content.append("<br/><br/><hr/><br/><table>");
 
         // Items
         OrderProductIf[] items = order.getOrderProducts();
         for(OrderProductIf item : items) {
             String quantity = Integer.toString(item.getQuantity());
             String name = item.getName();
             String total_price = item.getFinalPriceExTax().setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
             String single_price = item.getPrice().setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
 
             content.append("<tr><td style=\"text-align:right;width:1%;\">");
             content.append(quantity);
             content.append("</td><td>");
             content.append(name + " (à " + single_price + ")");
             content.append("</td><td style=\"text-align:right;\">");
             content.append(total_price); 
             content.append("</td></tr>");
         }
 
         // Totals
         String subtotal = order.getSubTotalExTax().setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
         content.append("<tr><td colspan=\"2\">Warenwert / Valeur:</td>");
         content.append("<td style=\"text-align:right;\">");
         content.append(subtotal);
         content.append("</td></tr>");
 
         String shw = order.getCustom1();
         if(!"".equals(shw)) {
             content.append("<tr><td colspan=\"2\">Lieferkosten Wein / Frais de port vins:</td>");
             content.append("<td style=\"text-align:right;\">");
             content.append(shw);
             content.append("</td></tr>");
         }
 
         String shg = order.getCustom2();
         if(!"".equals(shg)) {
             content.append("<tr><td colspan=\"2\">Lieferkosten Geschenk / Frais de port cadeaux:</td>");
             content.append("<td style=\"text-align:right;\">");
             content.append(shg);
             content.append("</td></tr>");
         }
 
         String rew = order.getCustom3();
         if(!"0.00".equals(rew)) {
             content.append("<tr><td colspan=\"2\">Mengenrabatt Wein / Rabais de quantité vins:</td>");
             content.append("<td style=\"text-align:right;\">");
             content.append(rew);
             content.append("</td></tr>");
         }
 
         String total = order.getTotalExTax().setScale(2, BigDecimal.ROUND_HALF_EVEN).toString();
         content.append("<tr><td colspan=\"2\" style=\"font-weight:bold;\">Total CHF (inkl. MwSt. / TVA incl.):</td>");
         content.append("<td style=\"text-align:right;font-weight:bold;\">");
         content.append(total);
         content.append("</td></tr>");
     
         content.append("</table>");
 
         // Payment info
         String lang;
         try {
             lang = getContentLanguage();
         } catch(Exception e) {
             lang = "de";
         }
 
         javax.servlet.http.HttpSession session = getEnvironment().getRequest().getSession(true);
         if (session.getAttribute("coupon") != null) {
             content.append("<br/><br/>Gutschein: Sie bezahlen keine Lieferkosten auf dieser Bestellung / Bon: les frais de livraison vous sont offerts pour cette commande<br/>");
         }
 
         content.append("<br/><br/><strong>Rechnungsadresse / Adresse de facturation</strong><br/>");
         if(lang.equals("fr")) {
             if(customer.getGender().equals("f")) {
                 content.append("Madame");
             } else {
                 content.append("Monsieur");
             }
         } else {
             if(customer.getGender().equals("f")) {
                 content.append("Frau");
             } else {
                 content.append("Herr");
             }
         }
         content.append("<br/>" + order.getBillingName());
         String company = order.getBillingCompany();
         if(company != null && company.length() > 0) {
             content.append("<br/>" + company);
         }
         content.append("<br/>" + order.getBillingStreetAddress());
         content.append("<br/>" + order.getBillingPostcode() + " " + order.getBillingCity());
         content.append("<br/>" + customer.getTelephoneNumber());
         content.append("<br/><a href=\"mailto:" + customer.getEmailAddr() + "\">" + customer.getEmailAddr() + "</a>");
 
         // Delivery info
         content.append("<br/><br/><strong>Lieferadresse / Adresse de livraison</strong>");
         content.append("<br/>" + order.getDeliveryName());
         company = order.getDeliveryCompany();
         if(company != null && company.length() > 0) {
             content.append("<br/>" + company);
         }
         content.append("<br/>" + order.getDeliveryStreetAddress());
         content.append("<br/>" + order.getDeliveryPostcode() + " " + order.getDeliveryCity());
 
         // Remarks
         String remarks = (String) getEnvironment().getRequest().getSession(true).getAttribute("checkout-data-remarks");
         if(remarks != null) {
             content.append("<br/><br/><strong>Grusstext, Mitteilung an Globus / Texte de salutation, message à Globus</strong><br/>");
             content.append(remarks);
         }
     }
 
     /**
      * Fix order totals.
      */
     public void fixOrderTotals(OrderIf order, ShippingQuoteIf shipping) {
          // Then we need to edit the OrderTotal objects, those
          // are used in the "detailed" view, to show e.g. shipping and such
          OrderTotalIf[] totals = order.getOrderTotals();
 
          for(OrderTotalIf t : totals) {
              if(t.getClassName().equals("ot_total")) {
                  // If it's the total, edit it...
                  // Not only do we need to change the value, 
                  // we also have to change the text that is displayed
                  // manually ourselves because if we don't the value
                  // will be correct in the database but Konakart will
                  // display something diffrent!
                  t.setValue(t.getValue().add(shipping.getTotalIncTax()));
                  t.setText("<b>CHF" + t.getValue().setScale(2, BigDecimal.ROUND_HALF_EVEN) + "</b>");
              }
          }
 
          // And now we add a OrderTotal object for our "Mengenrabatt",
          // otherwise Konakart will completely ignore it which sucks.
          OrderTotalIf[] custom_totals = new OrderTotal[totals.length+1];
          System.arraycopy(totals, 0, custom_totals, 0, totals.length);
 
          // Yes, all those fields are necessary
          // TODO: Make this more generic than it currently is.
          custom_totals[totals.length] = new OrderTotal();
          custom_totals[totals.length].setTitle("Mengenrabatt Wein");
          custom_totals[totals.length].setValue(shipping.getTotalIncTax());
          custom_totals[totals.length].setText("-CHF" + shipping.getTotalIncTax().multiply(new BigDecimal("-1")).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
          custom_totals[totals.length].setClassName("ot_custom");
          custom_totals[totals.length].setOrderId(order.getId());
          custom_totals[totals.length].setSortOrder(3);
 
          // And finally...
          order.setOrderTotals(custom_totals);
     }
 
 
     /**
      * Set custom fields, addrs.
      */
     public void setOrderAddressFields(OrderIf order, ShippingQuoteIf shipping, AddressIf devaddr, AddressIf defaddr) {
          // Add custom discount to order object
          // This is a bit difficult because of Konakart...
          // Here, we edit the totalIncTax/totalExTax fields, those
          // are used for displaying the order total in e.g. a overview
          order.setTotalExTax(order.getTotalExTax().add(shipping.getTotalIncTax()));
          order.setTotalIncTax(order.getTotalExTax().add(shipping.getTotalIncTax()));
 
          // Store cost in custom fields for use later on
          order.setCustom1(shipping.getCustom1());
          order.setCustom2(shipping.getCustom2());
          order.setCustom3(shipping.getCustom3());
 
          // Adresses
          order.setCustomerAddrId(defaddr.getId());
          order.setBillingAddrId(defaddr.getId());
          order.setDeliveryAddrId(devaddr.getId());
          order.setDeliveryCity(devaddr.getCity());
          order.setDeliveryName(devaddr.getFirstName() + " " + devaddr.getLastName());
          order.setDeliveryCountry(devaddr.getCountryName());
          order.setDeliveryStreetAddress(devaddr.getStreetAddress());
          order.setDeliveryPostcode(devaddr.getPostcode());
          if(devaddr.getCompany() != null) order.setDeliveryCompany(devaddr.getCompany());
     }
 
     /**
      * Send mail.
      */
     public void sendMail(String sender, String recipient, String subject, String content) {
         log.warn("DEBUG: Send e-mail to: " + recipient);
         try {
             // TODO: sender is also used as reply-to
             // INFO: Use smtp host and port from Yanel global configuration
             org.wyona.yanel.core.util.MailUtil.send(null, -1, sender, sender, recipient, subject, content, "utf-8", "html");
         } catch(Exception e) {
             log.error(e, e);
         }
 
     }
 
     /**
      * Send email to customer.
      */
     public void sendMailToCustomer(int id, OrderIf order, CustomerIf customer) {
         // Content
         StringBuilder content = new StringBuilder();
         String lang;
 
         try {
             lang = getContentLanguage();
         } catch(Exception e) {
             lang = "de";
         }
 
         // Salutation
         if(lang.equals("fr")) {
             if(customer.getGender().equals("f")) {
                 content.append("Madame, ");
             } else {
                 content.append("Monsieur, ");
             }
         } else {
             if(customer.getGender().equals("f")) {
                 content.append("Sehr geehrte Frau ");
             } else {
                 content.append("Sehr geehrter Herr ");
             }
             content.append(customer.getLastName());
         }
 
         try {
             String header = getResourceConfigProperty("header");
             if(header != null) content.append(URLDecoder.decode(header));
         } catch(Exception e) { /* If there's no header defined, we don't do anything */ }
 
         // Bulk content
         getMailContent(content, id, order, customer);
 
         // Payment
         content.append("<br/><br/><b>Zahlungsart / Mode de paiement</b><br/>");
         javax.servlet.http.HttpSession session = getEnvironment().getRequest().getSession(true);
         String type = (String) session.getAttribute("checkout-card-data-type");
 
         if("Pluscard".equals(type)) {
             content.append("Pluscard");
         } else {
             content.append("Kreditkarte / Carte de crédit");
         }
 
         content.append("<br/><br/>");
         try {
             String footer = getResourceConfigProperty("footer");
             if(footer != null) content.append(URLDecoder.decode(footer));
         } catch(Exception e) { /* If there's no footer defined, we don't do anything */ }
 
         // Send
         try {
             String recipient = customer.getEmailAddr();
             String sender = getResourceConfigProperty("email-from");
             String subject = getResourceConfigProperty("email-subject") + id;
             sendMail(sender, recipient, subject, content.toString());
         } catch(Exception e) {
             log.error(e, e);
         }
     }
 
     /**
      * Send email to shop owner and general administrator.
      */
     public void sendMailToOwner(int id, OrderIf order, CustomerIf customer, String payment) {
         // Content
         StringBuilder content = new StringBuilder();
         content.append("Eine neue Bestellung ist eingetroffen.");
         content.append("<br/>Vous avez reçu une nouvelle commande.");
 
         try {
             String adminurl = getResourceConfigProperty("admin-url");
             if(adminurl != null) {
                 content.append("<br/><br/>Details und Status ändern / Details et changer le status:<br/>");
                 content.append("<a href=\"" + adminurl + "\">" + adminurl + "</a>");
             }
         } catch(Exception e) { /* If there's no admin-url defined, we don't do anything */ }
 
         getMailContent(content, id, order, customer);
 
         // Payment details
         javax.servlet.http.HttpSession session = getEnvironment().getRequest().getSession(true);
         String name = (String) session.getAttribute("checkout-card-data-name");
         content.append("<br/><br/><b>Zahlungsart / Mode de paiement</b><br/>");
         content.append(order.getPaymentMethod());
         content.append("<br/>" + name); 
         content.append("<br/>" + payment);
 
         content.append("<br/><br/><hr/><br/>");
 
         // Time to send that email now
         // Settings
         try {
             String recipient = getResourceConfigProperty("email-to");
             String sender = getResourceConfigProperty("email-from");
             String subject = getResourceConfigProperty("email-subject") + id;
             String branchTo = getBranchEmail(order.getDeliveryPostcode());
             String sendbranch = getResourceConfigProperty("send-branch-emails");
 
             content.append("Filiale: " + branchTo + "<br/>");
 
             if(branchTo != null) {
                 if("true".equals(sendbranch)) {
                     // Send mail to branch
                     sendMail(sender, branchTo, subject, content.toString());
                 }
             }
 
             // Send mail to owner
             sendMail(sender, recipient, subject, content.toString());
         } catch(Exception e) {
             log.error(e, e);
         }
     }
 
     /**
      * Get branch email which corresponds to ZIP code of customer.
      * @param zipcode ZIP code of customer
      */
     public String getBranchEmail(String zipcode) throws Exception {
         String branchlist = getResourceConfigProperty("email-branches");
 
         if(branchlist != null) {
             if(handler == null) {
                 this.handler = new BranchListHandler(zipcode);
 
                 InputSource source = new InputSource(getRealm().getRepository().getNode(branchlist).getInputStream());
                 XMLReader parser = XMLReaderFactory.createXMLReader();
                 parser.setContentHandler(handler);
                 parser.parse(source);
             }
     
             return handler.recipient;
         }
             
         return null;
     }
 
     /**
      * Get branch store Id which corresponds to ZIP code of customer.
      * @param zipcode ZIP code of customer
      */
     public String getBranchStoreId(String zipcode) throws Exception {
         String branchlist = getResourceConfigProperty("email-branches");
 
         if(branchlist != null) {
             if(handler == null) {
                 this.handler = new BranchListHandler(zipcode);
 
                 InputSource source = new InputSource(getRealm().getRepository().getNode(branchlist).getInputStream());
                 XMLReader parser = XMLReaderFactory.createXMLReader();
                 parser.setContentHandler(handler);
                 parser.parse(source);
             }
     
             return handler.storeId;
         }
             
         return null;
     }
     
     /**
      * Append error element for given field.
      */
 /*
     public void appendErr(String field, Element elem, org.w3c.dom.Document doc) {
         Element err = (Element) elem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "error"));
         err.setAttribute("id", field);
     }
 */
 
     /**
      * Append field to XML output.
      */
     public void appendField(String field, String val, Element elem, org.w3c.dom.Document doc) {
         Element err = (Element) elem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "field"));
         err.setAttribute("id", field);
         err.appendChild(doc.createTextNode(val));
     }
 
     /**
      * Exists?
      */
     public boolean exists() {
         try {
             SharedResource shared = new SharedResource();
             KKEngIf kkEngine = shared.getKonakartEngineImpl();
             String sessionId = shared.getSessionId(getEnvironment().getRequest().getSession(true));
             int customerId = shared.getCustomerId(getEnvironment().getRequest().getSession(true));
             int languageId = shared.getLanguageId(getContentLanguage());
 
             // Get customer
             CustomerIf customer = shared.getCustomer(sessionId, customerId); 
 
             if(customer == null) {
                 return false;
             } 
         } catch(Exception e) {
             return false;
         }
 
         return true;
     }
 
     /**
      * Check whether order should be processed finally
      */
     private boolean readyToProcess() {
         boolean process = getEnvironment().getRequest().getParameter("process") != null;
         String confirmParam = getEnvironment().getRequest().getParameter("confirm");
         boolean confirmGeneralTermsConditions = false;
         if (confirmParam != null) {
             log.debug("Confirm general terms and conditions: " + confirmParam);
             confirmGeneralTermsConditions = true;
         }
         if(process && !confirmGeneralTermsConditions) {
             log.warn("General terms and conditions have not been accepted, hence do not process order...");
             process = false;
         }
         return process;
     }
 
     /**
      * @see org.wyona.yanel.core.api.attributes.AnnotatableV1#getAnnotations()
      */
     public String[] getAnnotations() throws Exception {
         if (true) {
         //if (readyToProcess()) {
             SharedResource shared = new SharedResource();
             KKEngIf kkEngine = shared.getKonakartEngineImpl();
             int tmpCustomerId = shared.getTemporaryCustomerId(getEnvironment().getRequest().getSession(true));
             int languageId = shared.getLanguageId(getContentLanguage());
             BasketIf[] items = kkEngine.getBasketItemsPerCustomer(null, tmpCustomerId, languageId); // TODO: It seems like the basket has already been deleted!?
 
             java.util.List<String> annotations = new java.util.ArrayList();
             for (int i = 0; i < items.length; i++) {
                 annotations.add("" + items[i].getProductId());
                 annotations.add(items[i].getProduct().getName());
             }
 
             return annotations.toArray(new String[annotations.size()]);
         } else {
             log.warn("No annotations!");
             return null;
         }
     }
 
     /**
      * @see org.wyona.yanel.core.api.attributes.AnnotatableV1#clearAllAnnotations()
      */
     public void clearAllAnnotations() throws Exception {
         log.warn("No implemented yet!");
     }
 
     /**
      * @see org.wyona.yanel.core.api.attributes.AnnotatableV1#removeAnnotation(String)
      */
     public void removeAnnotation(String name) throws Exception {
         log.warn("No implemented yet!");
     }
 
     /**
      * @see org.wyona.yanel.core.api.attributes.AnnotatableV1#setAnnotation(String)
      */
     public void setAnnotation(String name) throws Exception {
         log.warn("No implemented yet!");
     }
 
     /**
      * @see org.wyona.yanel.core.api.attributes.TrackableV1#doTrack(TrackingInformationV1)
      */
     public void doTrack(org.wyona.yanel.core.attributes.tracking.TrackingInformationV1 trackInfo) {
         this.trackInfo = trackInfo;
     }
 }
