 /*
  * Copyright 2010 Wyona
  */
 
 package org.wyona.yanel.resources.konakart.overview;
 
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
  * Show overview
  */
 public class KonakartOverviewSOAPInfResource extends BasicXMLResource {
     
     private static Logger log = Logger.getLogger(KonakartOverviewSOAPInfResource.class);
     private static String KONAKART_NAMESPACE = "http://www.konakart.com/1.0";
 
     protected InputStream getContentXML(String viewId) throws Exception {
         if (log.isDebugEnabled()) {
             log.debug("requested viewId: " + viewId);
         }
 
         SharedResource shared = new SharedResource();
         KKEngIf kkEngine = shared.getKonakartEngineImpl();
         String sessionId = shared.getSessionId(getEnvironment().getRequest().getSession(true));
         int customerId = shared.getCustomerId(getEnvironment().getRequest().getSession(true));
         int languageId = shared.getLanguageId(getContentLanguage());
         boolean process = getEnvironment().getRequest().getParameter("process") != null;
         OrderIf order = null;
 
         // Build document
         org.w3c.dom.Document doc = null;
         try {
             doc = org.wyona.commons.xml.XMLHelper.createDocument(KONAKART_NAMESPACE, "overview");
         } catch (Exception e) {
             throw new Exception(e.getMessage(), e);
         }
 
         // Root element
         Element rootElement = doc.getDocumentElement();
 
         // Place order?
         if(process) { 
             // If process is true, we're creating and submitting this order
             // at the same time as we are displaying the information. So first,
             // we need the items in the basket to create an order object.
 
             try {
                 int tmpCustomerId = shared.getTemporaryCustomerId(getEnvironment().getRequest().getSession(true));
                 BasketIf[] items = kkEngine.getBasketItemsPerCustomer(null, tmpCustomerId, languageId);
                 order = kkEngine.createOrder(sessionId, items, languageId);
                 ShippingQuoteIf shipping = shared.getShippingCost(items, sessionId, languageId);
                 order.setShippingQuote(shipping);
                 order = kkEngine.getOrderTotals(order, languageId);
                 // Add custom discount to order object
                 // This is a bit difficult because of Konakart...
                 // Here, we edit the totalIncTax/totalExTax fields, those
                 // are used for displaying the order total in e.g. a overview
                 order.setTotalExTax(order.getTotalExTax().add(shipping.getTotalIncTax()));
                 order.setTotalIncTax(order.getTotalExTax().add(shipping.getTotalIncTax()));
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
                 custom_totals[totals.length] = new OrderTotal();
                 custom_totals[totals.length].setTitle("Mengenrabatt Wein");
                 custom_totals[totals.length].setValue(shipping.getTotalIncTax());
                 custom_totals[totals.length].setText("-CHF" + shipping.getTotalIncTax().multiply(new BigDecimal("-1")).setScale(2, BigDecimal.ROUND_HALF_EVEN).toString());
                 custom_totals[totals.length].setClassName("ot_custom");
                 custom_totals[totals.length].setOrderId(order.getId());
                 custom_totals[totals.length].setSortOrder(3);
                 // And finally...
                 order.setOrderTotals(custom_totals);
 
                 // Store cost in custom fields for use later on
                 order.setCustom1(shipping.getCustom1());
                 order.setCustom2(shipping.getCustom2());
                 order.setCustom3(shipping.getCustom3());
             } catch(Exception e) {
                 process = false;
                 log.error(e, e);
                 Element perrElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "process-error"));
                 if(e.getMessage() != null) perrElem.appendChild(doc.createTextNode(e.getMessage()));
             }
         }
 
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
 
         if(process) {
             try {
                 order.setCustomerAddrId(defaddr.getId());
                 order.setBillingAddrId(defaddr.getId());
             } catch(Exception e) {
                 process = false; 
                 log.error(e, e);
                 Element perrElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "process-error"));
                 if(e.getMessage() != null) perrElem.appendChild(doc.createTextNode(e.getMessage()));
             }
         }
 
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
 
         if(process) {
             try { 
                 order.setDeliveryAddrId(devaddr.getId());
                 order.setDeliveryCity(devaddr.getCity());
                 order.setDeliveryName(devaddr.getFirstName() + " " + devaddr.getLastName());
                 order.setDeliveryCountry(devaddr.getCountryName());
                 order.setDeliveryStreetAddress(devaddr.getStreetAddress());
                 order.setDeliveryPostcode(devaddr.getPostcode());
                 if(devaddr.getCompany() != null) order.setDeliveryCompany(devaddr.getCompany());
                 // Status trail
                 OrderStatusHistoryIf[] trail = new OrderStatusHistoryIf[1];
                 trail[0] = new OrderStatusHistory();
                 trail[0].setOrderStatus("New order.");
                 trail[0].setCustomerNotified(true);
                 order.setStatusTrail(trail);
             } catch(Exception e) {
                 process = false;
                 log.error(e, e);
                 Element perrElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "process-error"));
                 if(e.getMessage() != null) perrElem.appendChild(doc.createTextNode(e.getMessage()));
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
                     order.setPaymentMethod("Pluscard");
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
                     order.setPaymentMethod("Creditcard");
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
         }
 
         if(process) {
             try {
                 int id = kkEngine.saveOrder(sessionId, order, languageId);
                 Element prElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "process"));
                 prElem.setAttribute("orderid", "" + id);
 
                 // Print date
                 Date today = new Date();
                 SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                 String formattedDate = formatter.format(today);
                 Element dateElem = (Element) rootElement.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "date"));
                 dateElem.appendChild(doc.createTextNode(formattedDate));
 
                 sendMailToCustomer(id, order, customer);
                 sendMailToOwner(id, order, customer, payment_info_mail);
                 // Let Konakart send the order confirmation...
                 // kkEngine.sendOrderConfirmationEmail(sessionId, id, "Order #" + id, languageId);
 
                 // Status updates
                 kkEngine.changeOrderStatus(sessionId, id, order.getStatus(), true, "New order."); 
                 if(remarks != null) {
                     try {
                         kkEngine.changeOrderStatus(sessionId, id, order.getStatus(), true, "Remarks: " + remarks); 
                     } catch(Exception e) { 
                         log.error(e, e);
                     }
                 }
                 if(payment_info_kk != null) {
                    kkEngine.changeOrderStatus(sessionId, id, order.getStatus(), false, "Payment details: " + payment_info_kk);
                 }
 
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
         content.append("<br/>" + customer.getEmailAddr());
 
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
             content.append("<br/><br/><strong>Bemerkungen / Remarques</strong><br/>");
             content.append(remarks);
         }
     }
 
     /**
      * Send mail.
      */
     public void sendMail(String sender, String recipient, String subject, String content) {
         try {
             String smtpHost = getResourceConfigProperty("email-smtp-host");
             int smtpPort;
             try { 
                 smtpPort = Integer.parseInt(getResourceConfigProperty("email-smtp-port"));
             } catch(Exception e) {
                 smtpPort = -1;
             }
             // TODO: sender is also used as reply-to
             //org.wyona.yanel.core.util.MailUtil.send(smtpHost, smtpPort, sender, sender, recipient, subject, content);
             org.wyona.yanel.core.util.MailUtil.send(smtpHost, smtpPort, sender, sender, recipient, subject, content, "utf-8", "html");
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
             String branch = getBranchEmail(order.getDeliveryPostcode());
             String sendbranch = getResourceConfigProperty("send-branch-emails");
 
 
             if(branch != null) {
                 if("true".equals(sendbranch)) {
                     // Send mail to branch
                     sendMail(sender, branch, subject, content.toString());
                 } else {
                     content.append("Filialleiter: " + branch + "<br/>");
                 }
             }
 
             // Send mail to owner
             sendMail(sender, recipient, subject, content.toString());
         } catch(Exception e) {
             log.error(e, e);
         }
     }
 
     /**
      * Get branch for zip code
      */
     public String getBranchEmail(String zipcode) throws Exception {
         String branchlist = getResourceConfigProperty("email-branches");
 
         if(branchlist != null) {
             InputSource source = new InputSource(getRealm().getRepository().getNode(branchlist).getInputStream());
         
             XMLReader parser = XMLReaderFactory.createXMLReader();
             BranchListHandler handler = new BranchListHandler(zipcode);
             parser.setContentHandler(handler);
             parser.parse(source);
     
             return handler.recipient;
         }
             
         return null;
     }
     
     /**
      * Append error element for given field.
      */
     public void appendErr(String field, Element elem, org.w3c.dom.Document doc) {
         Element err = (Element) elem.appendChild(doc.createElementNS(KONAKART_NAMESPACE, "error"));
         err.setAttribute("id", field);
     }
 
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
 
 }
