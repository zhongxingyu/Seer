 /*
  * JBILLING CONFIDENTIAL
  * _____________________
  *
  * [2003] - [2012] Enterprise jBilling Software Ltd.
  * All Rights Reserved.
  *
  * NOTICE:  All information contained herein is, and remains
  * the property of Enterprise jBilling Software.
  * The intellectual and technical concepts contained
  * herein are proprietary to Enterprise jBilling Software
  * and are protected by trade secret or copyright law.
  * Dissemination of this information or reproduction of this material
  * is strictly forbidden.
  */
 
 package com.sapienter.jbilling.server.process.task;
 
 import com.sapienter.jbilling.server.invoice.NewInvoiceDTO;
 import com.sapienter.jbilling.server.invoice.db.InvoiceLineDTO;
 import com.sapienter.jbilling.server.item.ItemBL;
 import com.sapienter.jbilling.server.item.db.ItemDAS;
 import com.sapienter.jbilling.server.item.db.ItemDTO;
 import com.sapienter.jbilling.server.item.db.ItemTypeDTO;
 import com.sapienter.jbilling.server.metafields.db.MetaFieldValue;
 import com.sapienter.jbilling.server.pluggableTask.InvoiceCompositionTask;
 import com.sapienter.jbilling.server.pluggableTask.PluggableTask;
 import com.sapienter.jbilling.server.pluggableTask.TaskException;
 import com.sapienter.jbilling.server.pluggableTask.admin.ParameterDescription;
 import com.sapienter.jbilling.server.process.PeriodOfTime;
 import com.sapienter.jbilling.server.user.db.CustomerDTO;
 import com.sapienter.jbilling.server.user.db.UserDAS;
 import com.sapienter.jbilling.server.util.Constants;
 import org.apache.log4j.Logger;
 
 import java.math.BigDecimal;
 import java.util.Set;
 
 /**
  * This plug-in calculates taxes for invoice.
  *
  * Plug-in parameters:
  *
  *      tax_item_id                 (required) The item that will be added to an invoice with the taxes
  *
  *      custom_contact_field_id     (optional) The id of CCF that if its value is 'true' or 'yes' for a customer,
  *                                  then the customer is considered exempt. Exempt customers do not get the tax
  *                                  added to their invoices.
  *      item_exempt_category_id     (optional) The id of an item category that, if the item belongs to, it is
  *                                  exempt from taxes
  *
  * @author Alexander Aksenov
  * @since 30.04.11
  */
public class SimpleTaxCompositionTask extends PluggableTask
        implements InvoiceCompositionTask {
 
     private static final Logger LOG = Logger.getLogger(SimpleTaxCompositionTask.class);
 
     // plug-in parameters
    //mandatory
    public static final ParameterDescription PARAM_TAX_ITEM_ID =
        new ParameterDescription("tax_item_id", true, ParameterDescription.Type.STR);

     // optional, may be empty
     public static final ParameterDescription PARAM_CUSTOM_CONTACT_FIELD_ID =
         new ParameterDescription("custom_contact_field_id", false, ParameterDescription.Type.STR);
     public static final ParameterDescription PARAM_ITEM_EXEMPT_CATEGORY_ID = new ParameterDescription("item_exempt_category_id", false, ParameterDescription.Type.STR);
 
     //initializer for pluggable params
     {
        descriptions.add(PARAM_TAX_ITEM_ID);
         descriptions.add(PARAM_CUSTOM_CONTACT_FIELD_ID);
         descriptions.add(PARAM_ITEM_EXEMPT_CATEGORY_ID);
     }
 
     @Override
     public void apply(NewInvoiceDTO invoice, Integer userId) throws TaskException {
         ItemDTO taxItem;
         Integer itemExemptCategoryId = null;
         Integer customContactFieldId = null;
         try {
             String paramValue = getParameter(PARAM_TAX_ITEM_ID.getName(), "");
             if (paramValue == null || "".equals(paramValue.trim())) {
                 throw new TaskException("Tax item id is not defined!");
             }
             taxItem = new ItemDAS().find(new Integer(paramValue));
             if (taxItem == null) {
                 throw new TaskException("Tax item not found!");
             }
             paramValue = getParameter(PARAM_ITEM_EXEMPT_CATEGORY_ID.getName(), "");
             if (paramValue != null && !"".equals(paramValue.trim())) {
                 itemExemptCategoryId = new Integer(paramValue);
             }
             paramValue = getParameter(PARAM_CUSTOM_CONTACT_FIELD_ID.getName(), "");
             if (paramValue != null && !"".equals(paramValue.trim())) {
                 customContactFieldId = new Integer(paramValue);
             }
         } catch (NumberFormatException e) {
             LOG.error("Incorrect plugin configuration", e);
             throw new TaskException(e);
         }
 
         if (!isTaxCalculationNeeded(userId, customContactFieldId)) {
             return;
         }
 
         if (taxItem.getPercentage() != null) {
             // tax calculation as percentage of full cost
             //calculate total to include result lines
             invoice.calculateTotal();
             BigDecimal invoiceAmountSum = invoice.getTotal();
 
             //remove carried balance from tax calculation
             //to avoid double taxation
             LOG.debug("Percentage Price. Carried balance is " + invoice.getCarriedBalance());
             if ( null != invoice.getCarriedBalance() ){
                 invoiceAmountSum = invoiceAmountSum.subtract(invoice.getCarriedBalance());
             }
 
             LOG.debug("Exempt Category " + itemExemptCategoryId);
             if (itemExemptCategoryId != null) {
                 // find exemp items and subtract price
                 for (int i = 0; i < invoice.getResultLines().size(); i++) {
                     InvoiceLineDTO invoiceLine = (InvoiceLineDTO) invoice.getResultLines().get(i);
                     ItemDTO item = invoiceLine.getItem();
 
                     if (item != null) {
                         Set<ItemTypeDTO> itemTypes = new ItemDAS().find(item.getId()).getItemTypes();
                         for (ItemTypeDTO itemType : itemTypes) {
                             if (itemType.getId() == itemExemptCategoryId) {
                                 LOG.debug("Item " + item.getDescription() + " is Exempt. Category " + itemType.getId());
                                 invoiceAmountSum = invoiceAmountSum.subtract(invoiceLine.getAmount());
                                 break;
                             }
                         }
                     }
                 }
             }
 
             LOG.debug("Calculating tax on = " + invoiceAmountSum);
 
             BigDecimal taxValue = invoiceAmountSum.multiply(taxItem.getPercentage()).divide(
                     BigDecimal.valueOf(100L), Constants.BIGDECIMAL_SCALE, Constants.BIGDECIMAL_ROUND
             );
             //if (taxValue.compareTo(BigDecimal.ZERO) > 0) {
             InvoiceLineDTO invoiceLine = new InvoiceLineDTO(null, "Tax line for percentage tax item " + taxItem.getId(),
                     taxValue, taxValue, BigDecimal.ONE, Constants.INVOICE_LINE_TYPE_TAX, 0,
                     taxItem.getId(), userId, null);
             invoice.addResultLine(invoiceLine);
             //}
         } else {
             LOG.debug("Flat Price.");
             ItemBL itemBL = new ItemBL(taxItem);
             BigDecimal price = itemBL.getPriceByCurrency(invoice.getCreateDatetime(), taxItem, userId, invoice.getCurrency().getId());
 
             if (price != null && price.compareTo(BigDecimal.ZERO) != 0) {
                 // tax as additional invoiceLine
                 InvoiceLineDTO invoiceLine = new InvoiceLineDTO(null, "Tax line with flat price for tax item " + taxItem.getId(),
                         price, price, BigDecimal.ONE, Constants.INVOICE_LINE_TYPE_TAX, 0,
                         taxItem.getId(), userId, null);
                 invoice.addResultLine(invoiceLine);
             }
         }
     }
 
     private boolean isTaxCalculationNeeded(Integer userId, Integer customFieldId) {
         if (customFieldId == null) {
             return true;
         }
         CustomerDTO customer = new UserDAS().find(userId).getCustomer();
         if (customer == null) {
             return true;
         }
 
         MetaFieldValue customField = customer.getMetaField(customFieldId);
         String value = (String) customField.getValue();
         if ("yes".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) {
             return false;
         }
 
         return true;
     }
 
     @Override
     public BigDecimal calculatePeriodAmount(BigDecimal fullPrice, PeriodOfTime period) {
         throw new UnsupportedOperationException("Can't call this method");
     }
 }
