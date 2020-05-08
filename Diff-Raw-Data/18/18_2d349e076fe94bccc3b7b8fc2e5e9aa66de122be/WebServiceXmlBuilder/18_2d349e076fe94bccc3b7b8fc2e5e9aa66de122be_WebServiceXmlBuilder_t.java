 package se.sveaekonomi.webpay.integration.webservice.helper;
 
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 
 import se.sveaekonomi.webpay.integration.exception.SveaWebPayException;
 import se.sveaekonomi.webpay.integration.util.xml.XMLBuilder;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaAuth;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaCloseOrder;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaCreateOrder;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaDeliverOrder;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaGetAddresses;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaGetPaymentPlanParams;
 import se.sveaekonomi.webpay.integration.webservice.svea_soap.SveaOrderRow;
 
 public class WebServiceXmlBuilder extends XMLBuilder {
 
     public static final String prefix = "web:";
     
     public String getCreateOrderEuXml(SveaCreateOrder order) {
         XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         
         try {
             xmlw = xmlof.createXMLStreamWriter(os, "UTF-8");
             xmlw.writeStartElement(prefix+"request");
             getAuth(order.Auth);
             
             if (order.CreateOrderInformation != null) {
                 xmlw.writeStartElement(prefix+"CreateOrderInformation");
                 writeSimpleElement(prefix+"ClientOrderNumber", order.CreateOrderInformation.ClientOrderNumber);
                serializeOrderRows(order.CreateOrderInformation.OrderRows);
                 
                 if (order.CreateOrderInformation.CustomerIdentity != null) {
                     xmlw.writeStartElement(prefix+"CustomerIdentity");
                     writeSimpleElement(prefix+"NationalIdNumber", order.CreateOrderInformation.CustomerIdentity.NationalIdNumber);
                     writeSimpleElement(prefix+"Email", order.CreateOrderInformation.CustomerIdentity.Email);
                     writeSimpleElement(prefix+"PhoneNumber", order.CreateOrderInformation.CustomerIdentity.PhoneNumber);
                     writeSimpleElement(prefix+"IpAddress", order.CreateOrderInformation.CustomerIdentity.IpAddress);
                     writeSimpleElement(prefix+"FullName", order.CreateOrderInformation.CustomerIdentity.FullName);
                     writeSimpleElement(prefix+"Street", order.CreateOrderInformation.CustomerIdentity.Street);
                     writeSimpleElement(prefix+"CoAddress", order.CreateOrderInformation.CustomerIdentity.CoAddress);
                     writeSimpleElement(prefix+"ZipCode", order.CreateOrderInformation.CustomerIdentity.ZipCode);
                     writeSimpleElement(prefix+"HouseNumber", order.CreateOrderInformation.CustomerIdentity.HouseNumber);
                     writeSimpleElement(prefix+"Locality", order.CreateOrderInformation.CustomerIdentity.Locality);
                     writeSimpleElement(prefix+"CountryCode", order.CreateOrderInformation.CustomerIdentity.CountryCode.toString());
                     writeSimpleElement(prefix+"CustomerType", order.CreateOrderInformation.CustomerIdentity.CustomerType);
                     
                     if (order.CreateOrderInformation.CustomerIdentity.IndividualIdentity != null) {
                         xmlw.writeStartElement(prefix+"IndividualIdentity");
                         writeSimpleElement(prefix+"FirstName", order.CreateOrderInformation.CustomerIdentity.IndividualIdentity.FirstName);
                         writeSimpleElement(prefix+"LastName", order.CreateOrderInformation.CustomerIdentity.IndividualIdentity.LastName);
                         writeSimpleElement(prefix+"Initials", order.CreateOrderInformation.CustomerIdentity.IndividualIdentity.Initials);
                         writeSimpleElement(prefix+"BirthDate", order.CreateOrderInformation.CustomerIdentity.IndividualIdentity.BirthDate);            
                         xmlw.writeEndElement();
                     }
                     
                     if (order.CreateOrderInformation.CustomerIdentity.CompanyIdentity != null) {
                         xmlw.writeStartElement(prefix+"CompanyIdentity");
                         writeSimpleElement(prefix+"CompanyIdentification", order.CreateOrderInformation.CustomerIdentity.CompanyIdentity.OrgNumber);
                         writeSimpleElement(prefix+"CompanyVatNumber", order.CreateOrderInformation.CustomerIdentity.CompanyIdentity.CompanyVatNumber);
                         xmlw.writeEndElement();
                     }
                     
                     xmlw.writeEndElement();
                 }
                 
                 writeSimpleElement(prefix+"OrderDate", new SimpleDateFormat("yyyy-MM-dd").format(order.CreateOrderInformation.OrderDate));
                 writeSimpleElement(prefix+"AddressSelector", order.CreateOrderInformation.AddressSelector);
                 writeSimpleElement(prefix+"CustomerReference", order.CreateOrderInformation.CustomerReference);
                 
                 writeSimpleElement(prefix+"OrderType", order.CreateOrderInformation.OrderType);
                 
                 if (order.CreateOrderInformation.CreatePaymentPlanDetails != null && !order.CreateOrderInformation.OrderType.equals("Invoice")) {
                     xmlw.writeStartElement(prefix+"CreatePaymentPlanDetails");
                     String code = (String)order.CreateOrderInformation.CreatePaymentPlanDetails.get("CampaignCode");
                     writeSimpleElement(prefix+"CampaignCode", code);
                     writeSimpleElement("SendAutomaticGiroPaymentForm", String.valueOf((boolean)order.CreateOrderInformation.CreatePaymentPlanDetails.get("SendAutomaticGiroPaymentForm")));
                     
                     // ?? CoCustomerIdentity ???
                     //?? writeSimpleElement("FixedMonthlyAmount", order.CreateOrderInformation.CreatePaymentPlanDetails.)
                 }
                 
                 xmlw.writeEndElement();
             }
             
             xmlw.writeEndElement();
             xmlw.writeEndDocument();
             
             return new String(os.toByteArray(), "UTF-8");
         } catch (XMLStreamException e) {
             throw new SveaWebPayException("Error while building XML", e);
         } catch (UnsupportedEncodingException e) {
             throw new SveaWebPayException("Unsupported encoding UTF-8", e);
         }
     }
     
     public String getDeliverOrderEuXml(SveaDeliverOrder request) {
         XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         
         try {
             xmlw = xmlof.createXMLStreamWriter(os, "UTF-8");
             xmlw.writeStartElement(prefix+"request");
             getAuth(request.auth);
             
             if (request.deliverOrderInformation != null) {
                 xmlw.writeStartElement(prefix+"DeliverOrderInformation");
                 writeSimpleElement(prefix+"SveaOrderId", request.deliverOrderInformation.sveaOrderId);
                 writeSimpleElement(prefix+"OrderType", request.deliverOrderInformation.orderType);
                 
                 if (request.deliverOrderInformation.deliverInvoiceDetails != null) {
                     xmlw.writeStartElement(prefix+"DeliverInvoiceDetails");
                     writeSimpleElement(prefix+"NumberOfCreditDays", String.valueOf(request.deliverOrderInformation.deliverInvoiceDetails.NumberofCreditDays));
                     writeSimpleElement(prefix+"InvoiceDistributionType", request.deliverOrderInformation.deliverInvoiceDetails.InvoiceDistributionType);
                     writeSimpleElement(prefix+"IsCreditInvoice", String.valueOf(request.deliverOrderInformation.deliverInvoiceDetails.IsCreditInvoice));
                     writeSimpleElement(prefix+"InvoiceIdToCredit", request.deliverOrderInformation.deliverInvoiceDetails.InvoiceIdToCredit);
                     ArrayList<SveaOrderRow> rows = request.deliverOrderInformation.getDeliverInvoiceDetails().OrderRows;
                     serializeOrderRows(rows);
                     xmlw.writeEndElement();
                 }
                 xmlw.writeEndElement();
             }
             xmlw.writeEndElement();
             xmlw.writeEndDocument();
             
             return new String(os.toByteArray(), "UTF-8");
         } catch (XMLStreamException e) {
             throw new SveaWebPayException("Error while building XML", e);
         } catch (UnsupportedEncodingException e) {
             throw new SveaWebPayException("Unsupported encoding UTF-8", e);
         }
     }
     
     public String getCloseOrderEuXml(SveaCloseOrder order) {
         XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         
         try {
             xmlw = xmlof.createXMLStreamWriter(os, "UTF-8");
             xmlw.writeStartElement(prefix+"request");
             getAuth(order.Auth);
             
             if (order.CloseOrderInformation != null) {
                 xmlw.writeStartElement(prefix+"CloseOrderInformation");
                 writeSimpleElement(prefix+"SveaOrderId", order.CloseOrderInformation.SveaOrderId.toString());
                 xmlw.writeEndElement();
             }
             
             xmlw.writeEndElement();
             xmlw.writeEndDocument();
             
             return new String(os.toByteArray(), "UTF-8");
         } catch (XMLStreamException e) {
             throw new SveaWebPayException("Error while building XML", e);
         } catch (UnsupportedEncodingException e) {
             throw new SveaWebPayException("Unsupported encoding UTF-8", e);
         }
     }
     
     public String getGetAddressesXml(SveaGetAddresses order) {
         XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         
         try {
             xmlw = xmlof.createXMLStreamWriter(os, "UTF-8");
             xmlw.writeStartElement(prefix+"request");
             
             getAuth(order.Auth);
             
             writeSimpleElement(prefix + "IsCompany", String.valueOf(order.IsCompany));
             writeSimpleElement(prefix + "CountryCode", order.CountryCode);
             writeSimpleElement(prefix + "SecurityNumber", order.SecurityNumber);
             
             xmlw.writeEndElement();
             xmlw.writeEndDocument();
             
             return new String(os.toByteArray(), "UTF-8");
         } catch (XMLStreamException e) {
             throw new SveaWebPayException("Error while building XML", e);
         } catch (UnsupportedEncodingException e) {
             throw new SveaWebPayException("Unsupported encoding UTF-8", e);
         }
     }
     
     public String getGetPaymentPlanParamsXml(SveaGetPaymentPlanParams order) {
         XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         
         try {
             xmlw = xmlof.createXMLStreamWriter(os, "UTF-8");
             xmlw.writeStartElement(prefix+"request");
             
             getAuth(order.Auth);
             
             xmlw.writeEndElement();
             xmlw.writeEndDocument();
             
             return new String(os.toByteArray(), "UTF-8");
         } catch (XMLStreamException e) {
             throw new SveaWebPayException("Error while building XML", e);
         } catch (UnsupportedEncodingException e) {
             throw new SveaWebPayException("Unsupported encoding UTF-8", e);
         }
     }
     
     private void getAuth(SveaAuth auth) throws XMLStreamException {
         if (auth == null) {
             return;
         }
         
         xmlw.writeStartElement(prefix+"Auth");
         writeSimpleElement(prefix+"ClientNumber", auth.ClientNumber.toString());
         writeSimpleElement(prefix+"Username", auth.Username.toString());
         writeSimpleElement(prefix+"Password", auth.Password.toString());
         xmlw.writeEndElement();
     }
     
     private void serializeOrderRows(ArrayList<SveaOrderRow> rows) throws XMLStreamException {
         if (rows == null || rows.size() == 0) {
             return;
         }
         
         xmlw.writeStartElement(prefix+"OrderRows");
         
         for (SveaOrderRow row : rows) {
             serializeOrderRow(row);
         }
         
         xmlw.writeEndElement();
     }
     
     private void serializeOrderRow(SveaOrderRow row) throws XMLStreamException {
         xmlw.writeStartElement(prefix+"OrderRow");
         writeSimpleElement(prefix+"ArticleNumber", row.ArticleNumber.toString());
         writeSimpleElement(prefix+"Description", row.Description);
         writeSimpleElement(prefix+"PricePerUnit", String.valueOf(row.PricePerUnit));
         writeSimpleElement(prefix+"NumberOfUnits", String.valueOf(row.NumberOfUnits));
         writeSimpleElement(prefix+"Unit", row.Unit);
         writeSimpleElement(prefix+"VatPercent", String.valueOf(row.VatPercent));
         writeSimpleElement(prefix+"DiscountPercent", String.valueOf(row.DiscountPercent));
         xmlw.writeEndElement();
     }
 }
