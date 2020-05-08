 /**
  * Mule Magento Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.magento;
 
 import static org.mule.module.magento.api.catalog.model.ProductIdentifiers.from;
 
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 
 import org.mule.api.lifecycle.Initialisable;
 import org.mule.api.lifecycle.InitialisationException;
 import org.mule.module.magento.api.AxisPortProvider;
 import org.mule.module.magento.api.DefaultAxisPortProvider;
 import org.mule.module.magento.api.MagentoClientAdaptor;
 import org.mule.module.magento.api.MagentoException;
 import org.mule.module.magento.api.catalog.AxisMagentoCatalogClient;
 import org.mule.module.magento.api.catalog.MagentoCatalogClient;
 import org.mule.module.magento.api.catalog.model.MediaMimeType;
 import org.mule.module.magento.api.customer.AxisMagentoInventoryClient;
 import org.mule.module.magento.api.customer.MagentoInventoryClient;
 import org.mule.module.magento.api.directory.AxisMagentoDirectoryClient;
 import org.mule.module.magento.api.directory.MagentoDirectoryClient;
 import org.mule.module.magento.api.inventory.AxisMagentoCustomerClient;
 import org.mule.module.magento.api.inventory.MagentoCustomerClient;
 import org.mule.module.magento.api.order.AxisMagentoOrderClient;
 import org.mule.module.magento.api.order.MagentoOrderClient;
 import org.mule.module.magento.api.order.model.Carrier;
 import org.mule.tools.cloudconnect.annotations.Connector;
 import org.mule.tools.cloudconnect.annotations.Operation;
 import org.mule.tools.cloudconnect.annotations.Parameter;
 import org.mule.tools.cloudconnect.annotations.Property;
 /**
  * A Cloud Connector for the Magento Order Sales API.
  * 
  * @author eberman
  * @author flbulgarelli
  */
 @Connector(namespacePrefix = "magento", namespaceUri = "http://www.mulesoft.org/schema/mule/magento")
 public class MagentoCloudConnector implements Initialisable
 {
     @Property
     private String username;
     @Property
     private String password;
     @Property
     private String address;
 
     private MagentoOrderClient<Map<String, Object>, List<Map<String, Object>>, MagentoException> orderClient;
     private MagentoCustomerClient<Map<String, Object>, List<Map<String, Object>>, MagentoException> customerClient;
     private MagentoInventoryClient<List<Map<String, Object>>, MagentoException> inventoryClient;
     private MagentoDirectoryClient<List<Map<String, Object>>, MagentoException> directoryClient;
     private MagentoCatalogClient<Map<String, Object>, List<Map<String, Object>>, MagentoException> catalogClient;
 
     public String getUsername()
     {
         return username;
     }
 
     public void setUsername(String username)
     {
         this.username = username;
     }
 
     public String getPassword()
     {
         return password;
     }
 
     public void setPassword(String password)
     {
         this.password = password;
     }
 
     public String getAddress()
     {
         return address;
     }
 
     public void setAddress(String address)
     {
         this.address = address;
     }
 
     public void initialise() throws InitialisationException
     {
         PortProviderInitializer initializer = new PortProviderInitializer();
         if (orderClient == null)
         {
             setOrderClient(new AxisMagentoOrderClient(initializer.getPortProvider()));
         }
         if (customerClient == null)
         {
             setCustomerClient(new AxisMagentoCustomerClient(initializer.getPortProvider()));
         }
         if (inventoryClient == null)
         {
             setInventoryClient(new AxisMagentoInventoryClient(initializer.getPortProvider()));
         }
         if (directoryClient == null)
         {
             setDirectoryClient(new AxisMagentoDirectoryClient(initializer.getPortProvider()));
         }
         if (catalogClient == null)
         {
             setCatalogClient(new AxisMagentoCatalogClient(initializer.getPortProvider()));
         }
     }
     /**
      * Adds a comment to the shipment. 
      * 
      * Example:
      * 
      * {@code <magento:add-order-shipment-comment 
      *          shipmentId="#[map-payload:shipmentId]" 
      *          comment="#[map-payload:comment]" 
      *          sendEmail="true" />}
      * 
      * @param shipmentId the shipment's increment id
      * @param comment the comment to add
      * @param sendEmail if an email must be sent after shipment creation
      * @param includeCommentInEmail if the comment must be sent in the email
      */
     @Operation
     public void addOrderShipmentComment(@Parameter String shipmentId,
                                         @Parameter String comment,
                                         @Parameter(optional = true, defaultValue = "false") boolean sendEmail,
                                         @Parameter(optional = true, defaultValue = "false") boolean includeCommentInEmail)
 
     {
         orderClient.addOrderShipmentComment(shipmentId, comment, sendEmail, includeCommentInEmail);
     }
 
     /**
      * Adds a new tracking number
      * 
      * Example:
      * 
      * {@code <magento:add-order-shipment-track
      *          shipmentId="#[map-payload:shipmentId]" 
      *          carrierCode="#[map-payload:carrierCode]"
      *          title="#[map-payload:title]" 
      *          trackId="#[map-payload:trackId]" />}
      * 
      * 
      * @param shipmentId the shipment id
      * @param carrierCode the new track's carrier code
      * @param title the new track's title
      * @param trackNumber the new track's number
      * @return the new track's id
      */
     @Operation
     public int addOrderShipmentTrack(@Parameter String shipmentId,
                                      @Parameter String carrierCode,
                                      @Parameter String title,
                                      @Parameter String trackId)
     {
         return orderClient.addOrderShipmentTrack(shipmentId, carrierCode, title, trackId);
     }
 
     /**
      * Cancels an order
      * 
      * Example:
      * 
      * {@code <magento:cancel-order
      *         orderId="#[map-payload:orderId]"/>}
      * 
      * @param orderId the order to cancel
      */
     @Operation
     public void cancelOrder(@Parameter String orderId)
     {
         orderClient.cancelOrder(orderId);
     }
 
     /**
      * Creates a shipment for order
      * 
      * Example:
      * 
      * {@code <magento:create-order-shipment 
      *          orderId="#[map-payload:orderId]"
      *          comment="#[map-payload:comment]">
      *          <magento:itemsQuantities>
      *              <magento:itemsQuantity key="#[map-payload:orderItemId1]" value="#[map-payload:Quantity1]"/>
      *              <magento:itemsQuantity key="#[map-payload:orderItemId2]" value="#[map-payload:Quantity2]"/>
      *          </magento:itemsQuantities>
      *       </magento:create-order-shipment>}
      * 
      * @param orderId the order increment id
      * @param itemsQuantities a map containing an entry per each (orderItemId,
      *            quantity) pair
      * @param comment an optional comment
      * @param sendEmail if an email must be sent after shipment creation
      * @param includeCommentInEmail if the comment must be sent in the email
      * @return the new shipment's id
      */
     @Operation
     public String createOrderShipment(@Parameter String orderId,
                                       @Parameter Map<Integer, Double> itemsQuantities,
                                       @Parameter(optional = true) String comment,
                                       @Parameter(optional = true, defaultValue = "false") boolean sendEmail,
                                       @Parameter(optional = true, defaultValue = "false") boolean includeCommentInEmail)
     {
         return orderClient.createOrderShipment(orderId, itemsQuantities, comment, sendEmail,
             includeCommentInEmail);
     }
 
     /**
      * Answers the order properties for the given orderId
      * 
      * Example:
      * 
      * {@code  <magento:get-order orderId="#[map-payload:orderId]" />}
      * 
      * @param orderId the order whose properties to fetch
      * @return a string-object map of order properties
      */
     @Operation
     public Map<String, Object> getOrder(@Parameter String orderId)
     {
         return orderClient.getOrder(orderId);
     }
 
     /**
      * Retrieves order invoice information
      * 
      * Example:
      * 
      * {@code       <magento:get-order-invoice invoiceId="#[map-payload:invoiceId]"  />}
      * 
      * @param invoiceId the target invoiceId
      * @return the invoice attributes
      */
     @Operation
     public Map<String, Object> getOrderInvoice(@Parameter String invoiceId)
     {
         return orderClient.getOrderInvoice(invoiceId);
     }
 
     /**
      * Creates an invoice for the given order
      * 
      * Example:
      * 
      * {@code  <magento:get-order-shipment-carriers  orderId="#[map-payload:orderId]"  />}
      * 
      * @param orderId the target order id
      * @param itemsQuantities a map containing an entry per each (orderItemId,
      *            quantity) pair
      * @param comment an optional comment
      * @param sendEmail if an email must be sent after shipment creation
      * @param includeCommentInEmail if the comment must be sent in the email
      * @return the new invoice's id
      */
     @Operation
     public List<Carrier> getOrderShipmentCarriers(@Parameter String orderId)
     {
         return orderClient.getOrderShipmentCarriers(orderId);
     }
 
     /**
      * Adds a comment to the given order's invoice
      * 
      * Example:
      * 
      * {@code  <magento:get-order-shipment 
      *             shipmentId="#[map-payload:orderShipmentId]" /> }
      * 
      * @param invoiceId the invoice id
      * @param comment the comment to add
      * @param sendEmail if an email must be sent after shipment creation
      * @param includeCommentInEmail if the comment must be sent in the email
      */
     @Operation
     public Map<String, Object> getOrderShipment(@Parameter String shipmentId)
     {
         return orderClient.getOrderShipment(shipmentId);
     }
 
     /**
      * Puts order on hold. This operation can be reverted with unholdOrder.
      * 
      * Example:
      * 
      * {@code  <magento:hold-order orderId="#[map-payload:orderId]"/>}
      * 
      * @param orderId the order to put on hold state
      */
     @Operation
     public void holdOrder(@Parameter String orderId)
     {
         orderClient.holdOrder(orderId);
     }
 
     /**
      * Lists order attributes that match the given filtering expression.
      * 
      * Example
      * {@code <magento:list-orders
      * filter="gt(subtotal, #[map-payload:minSubtotal])"/>}
      * 
     * @param filters optional filtering expression - one or more comma-separated
      *            unary or binary predicates, one for each filter, in the form
      *            filterType(attributeName, value), for binary filters or
      *            filterType(attributeName), for unary filters, where filterType is
      *            istrue, isfalse or any of the Magento standard filters. Non-numeric
      *            values need to be escaped using simple quotes.
      * @return a list of string-object maps
      */
     @Operation
     public List<Map<String, Object>> listOrders(@Parameter(optional = true) String filter)
     {
         return orderClient.listOrders(filter);
     }
 
     /**
      * Lists order invoices that match the given filtering expression
      * 
      * Example:
      * 
      * {@code <magento:list-orders-invoices filter="notnull(parent_id)" />}     
      * 
     *  @param filters optional filtering expression - one or more comma-separated
      *            unary or binary predicates, one for each filter, in the form
      *            filterType(attributeName, value), for binary filters or
      *            filterType(attributeName), for unary filters, where filterType is
      *            istrue, isfalse or any of the Magento standard filters. Non-numeric
      *            values need to be escaped using simple quotes.
      * @return list of string-object maps order attributes
      */
     @Operation
     public List<Map<String, Object>> listOrdersInvoices(@Parameter(optional = true) String filter)
     {
         return orderClient.listOrdersInvoices(filter);
     }
 
     /**
      * Lists order shipment atrributes that match the given 
      * optional filtering expression
      * 
      * Example:
      * 
      * {@code <magento:list-orders-shipments filter="null(parent_id)" />}
      * 
     * @param filters optional filtering expression - one or more comma-separated
      *            unary or binary predicates, one for each filter, in the form
      *            filterType(attributeName, value), for binary filters or
      *            filterType(attributeName), for unary filters, where filterType is
      *            istrue, isfalse or any of the Magento standard filters. Non-numeric
      *            values need to be escaped using simple quotes.
      * @return list of string-object map order shipments attributes
      */
     @Operation
     public List<Map<String, Object>> listOrdersShipments(@Parameter(optional = true) String filter)
     {
         return orderClient.listOrdersShipments(filter);
     }
 
     /**
      * Deletes the given track of the given order's shipment
      * 
      * <magento:delete-order-shipment-track
      *       shipmentId="#[map-payload:shipmentId]" trackId="#[map-payload:trackId]" />
      * 
      * @param shipmentId the target shipment id
      * @param trackId the id of the track to delete
      */
     @Operation
     public void deleteOrderShipmentTrack(@Parameter String shipmentId, @Parameter String trackId)
     {
         orderClient.deleteOrderShipmentTrack(shipmentId, trackId);
     }
 
     /**
      * Adds a comment to the given order id
      * 
      * {@code <magento:add-order-comment 
      *               orderId="#[map-payload:orderId]"
      *              status="#[map-payload:status]" 
      *              comment="#[map-payload:comment]" />}     
      * 
      * @param orderId the order id
      * @param status the comment status
      * @param comment the comment
      * @param sendEmail if an email must be sent after shipment creation
      */
     @Operation
     public void addOrderComment(@Parameter String orderId,
                                 @Parameter String status,
                                 @Parameter String comment,
                                 @Parameter(optional = true, defaultValue = "false") boolean sendEmail)
     {
         orderClient.addOrderComment(orderId, status, comment, sendEmail);
     }
 
     /**
      * Releases order
      * 
      * Example:
      * 
      * {@code  <magento:unhold-order orderId="#[map-payload:orderId]" />}
      * 
      * @param orderId the id of the order to remove from hold state
      */
     @Operation
     public void unholdOrder(@Parameter String orderId)
     {
         orderClient.unholdOrder(orderId);
     }
     
     /**
      * Creates an invoice for the given order
      * 
      * @param orderId
      * @param itemsQuantities a map containing an entry per each (orderItemId,
      *            quantity) pair
      * @param comment an optional comment
      * @param sendEmail if an email must be sent after shipment creation
      * @param includeCommentInEmail if the comment must be sent in the email
      * @return the new invoice's id
      */
     @Operation
     public String createOrderInvoice(@Parameter String orderId,
                                      @Parameter Map<Integer, Double> itemsQuantities,
                                      @Parameter(optional = true) String comment,
                                      @Parameter(optional = true, defaultValue = "false") boolean sendEmail,
                                      @Parameter(optional = true, defaultValue = "false") boolean includeCommentInEmail)
     {
         return orderClient.createOrderInvoice(orderId, itemsQuantities, comment, sendEmail,
             includeCommentInEmail);
     }
 
     /**
      * Adds a comment to the given order's invoice
      * 
      * Example: 
      * 
      * {@code  <magento:add-order-comment 
      *             orderId="#[map-payload:orderId]"
      *             status="#[map-payload:status]" 
      *             comment="#[map-payload:comment]" }
      * 
      * @param invoiceId the invoice id
      * @param comment the comment to add
      * @param sendEmail if an email must be sent after shipment creation
      * @param includeCommentInEmail if the comment must be sent in the email
      */
     @Operation
     public void addOrderInvoiceComment(@Parameter String invoiceId,
                                        @Parameter String comment,
                                        @Parameter(optional = true, defaultValue = "false") boolean sendEmail,
                                        @Parameter(optional = true, defaultValue = "false") boolean includeCommentInEmail)
     {
         orderClient.addOrderInvoiceComment(invoiceId, comment, sendEmail, includeCommentInEmail);
     }
 
     /**
      * Captures and invoice
      * 
      * Example: 
      * 
      * {@code  <magento:capture-order-invoice invoiceId="#[map-payload:invoiceId]"  />}
      * 
      * @param invoiceId the invoice to capture
      */
     @Operation
     public void captureOrderInvoice(@Parameter String invoiceId)
     {
         orderClient.captureOrderInvoice(invoiceId);
     }
 
     /**
      * Voids an invoice
      * 
      * Example: 
      * 
      * {@code  <magento:void-order-invoice invoiceId="#[map-payload:invoiceId]"/>}
      * 
      * @param invoiceId the invoice id
      */
     @Operation
     public void voidOrderInvoice(@Parameter String invoiceId)
     {
         orderClient.voidOrderInvoice(invoiceId);
     }
 
     /**
      * Cancels an order's invoice
      * 
      * Example: 
      * 
      * {@code  <magento:cancel-order-invoice invoiceId="#[map-payload:invoiceId]"  />}
      * 
      * @param invoiceId the invoice id
      */
     @Operation
     public void cancelOrderInvoice(@Parameter String invoiceId)
     {
         orderClient.cancelOrderInvoice(invoiceId);
     }
     
     /**
      * Creates a new address for the given customer using the given address
      * attributes
      * 
      * {@code <magento:create-customer-address customerId="#[map-payload:customerId]"  >
      *            <magento:attributes >
      *              <magento:attribute key="city_code" value="#[map-payload:cityCode]"/>
      *            </magento:attributes>
      *          </magento:create-customer-address>}
      * 
      * @param customerId the customer 
      * @param attributes the address attributes
      * @return a new customer address id
      */
     @Operation
     public int createCustomerAddress(@Parameter int customerId, @Parameter Map<String, Object> attributes)
     {
         return customerClient.createCustomerAddress(customerId, attributes);
     }
 
     /**
      * Creates a customer with the given attributes
      * 
      * Example: 
      * 
      * {@code      <magento:create-customer>
      *              <magento:attributes >
      *                <magento:attribute key="email" value="#[map-payload:email]"/>
      *                <magento:attribute key="firstname" value="#[map-payload:firstname]"/>
      *                <magento:attribute key="lastname" value="#[map-payload:lastname]"/>
      *                 <magento:attribute key="password" value="#[map-payload:password]"/>
      *              </magento:attributes>
      *           </magento:create-customer>} 
      * 
      * @param attributes the attributes of the new customer
      * @return the new customer id
      */
     @Operation
     public int createCustomer(@Parameter Map<String, Object> attributes)
     {
         return customerClient.createCustomer(attributes);
     }
 
     /**
      * Deletes a customer given its id
      * 
      * Example:
      * 
      * {@code <magento:delete-customer customerId="#[map-payload:customerId]" />} 
      * 
      * @param customerId the customer to delete
      */
     @Operation
     public void deleteCustomer(@Parameter int customerId)
     {
         customerClient.deleteCustomer(customerId);
     }
 
     /**
      * Deletes a Customer Address
      * 
      * Example:
      * 
      * {@code <magento:delete-customer-address addressId="#[map-payload:addressId]" />}
      * 
      * @param addressId
      */
     @Operation
     public void deleteCustomerAddress(@Parameter int addressId)
     {
         customerClient.deleteCustomerAddress(addressId);
     }
 
     /**
      * Answers customer attributes for the given id. Only the selected attributes are
      * retrieved
      * 
      * Example:
      * 
      * {@code <magento:get-customer  customerId="#[map-payload:customerId]"  >
      *       <magento:attributeNames>
      *            <magento:attributeName>customer_name</magento:attributeName>
      *              <magento:attributeName>customer_last_name </magento:attributeName>
      *            <magento:attributeName>customer_age</magento:attributeName>
      *       </magento:attributeNames>
      *      </magento:get-customer>}
      * 
      * @param customerId
      * @param attributeNames the attributes to retrieve. Not empty
      * @return the attributes map
      * 
      */
     @Operation
     public Map<String, Object> getCustomer(@Parameter int customerId, @Parameter List<String> attributeNames)
 
     {
         return customerClient.getCustomer(customerId, attributeNames);
     }
 
     /**
      * Answers the customer address attributes
      * 
      * Example: 
      * {@code <magento:get-customer-address  addressId="#[map-payload:addressId]"/>}
      * 
      * @param addressId
      * @return the customer address attributes map
      */
     @Operation
     public Map<String, Object> getCustomerAddress(@Parameter int addressId)
     {
         return customerClient.getCustomerAddress(addressId);
     }
 
     /**
      * Lists the customer address for a given customer id
      * 
      * Example: 
      * 
      * {@code  <magento:list-customer-addresses customerId="#[map-payload:customerAddresses]" />}
      * 
      * @param customerId the id of the customer
      * @return a listing of addresses
      */
     @Operation
     public List<Map<String, Object>> listCustomerAddresses(@Parameter int customerId)
     {
         return customerClient.listCustomerAddresses(customerId);
     }
 
     /**
      * Lists all the customer groups
      * 
      * Example: 
      * 
      * {@code <magento:list-customer-groups />}
      * 
      * @return a listing of groups attributes
      */
     @Operation
     public List<Map<String, Object>> listCustomerGroups()
     {
         return customerClient.listCustomerGroups();
     }
 
     /**
      * Answers a list of customer attributes for the given filter expression.
      * 
      * Example:
      * 
     * {@code <magento:list-customers filters="gteq(customer_age, #[map-payload:minCustomerAge])" />}
      * 
     * @param filters optional filtering expression - one or more comma-separated
      *            unary or binary predicates, one for each filter, in the form
      *            filterType(attributeName, value), for binary filters or
      *            filterType(attributeName), for unary filters, where filterType is
      *            istrue, isfalse or any of the Magento standard filters. Non-numeric
      *            values need to be escaped using simple quotes.
      * @return the list of attributes map
      */
     @Operation
    public List<Map<String, Object>> listCustomers(@Parameter(optional = true) String filters)
     {
        return customerClient.listCustomers(filters);
     }
 
     /**
      * Updates the given customer attributes, for the given customer id. Password can
      * not be updated using this method
      * 
      * Example:
      * 
      * {@code <magento:update-customer customerId="#[map-payload:customerId]">
      *            <magento:attributes>
      *               <magento:attribute key="lastname" value="#[map-payload:lastname]"/>
      *            </magento:attributes>}
      * 
      * @param customerId the target customer to update
      * @param attributes the attributes map
      * 
      */
     @Operation
     public void updateCustomer(@Parameter int customerId, @Parameter Map<String, Object> attributes)
     {
         customerClient.updateCustomer(customerId, attributes);
     }
 
     /**
      * Updates the given map of customer address attributes, for the given customer address
      * 
      * Example:
      * 
      * {@code <magento:update-customer-address addressId="#[map-payload:addressId]">
      *          <magento:attributes>
      *               <magento:attribute key="street" value="#[map-payload:street]"/>
      *               <magento:attribute key="region" value="#[map-payload:region]"/>
      *           </magento:attributes>
      *        </magento:update-customer-address>} 
      * 
      * 
      * @param addressId the customer address to update
      * @param attributes  the address attributes to update
      */
     @Operation
     public void updateCustomerAddress(@Parameter int addressId, @Parameter Map<String, Object> attributes)
     {
         customerClient.updateCustomerAddress(addressId, attributes);
     }
 
 
     /**
      * Retrieve stock data by product ids
      * 
      * Example:
      * 
      * {@code <magento:list-stock-items >
      *          <magento:productIdOrSkus>
      *               <magento:productIdOrSku>1560</magento:productIdOrSku>
      *              <magento:productIdOrSku>JJFO986</magento:productIdOrSku>
      *          </magento:productIdOrSkus>
      *     </magento:list-stock-items>}
      * 
      * @param productIdOrSkus a not empty list of product ids or skus whose attributes to list
      * @return a list of stock items attributes
      */
     @Operation
     public List<Map<String, Object>> listStockItems(@Parameter List<String> productIdOrSkus)
     {
         return inventoryClient.listStockItems(productIdOrSkus);
     }
 
     /**
      * Update product stock data given its id or sku
      * 
      * Example:
      * 
      * {@code  <magento:update-stock-item productIdOrSku="#[map-payload:productIdOrSku]">
      *          <magento:attributes>
      *               <magento:attribute key="qty" value="#[map-payload:quantity]"/>
      *          </magento:attributes>
      *     </magento:update-stock-item>} 
      * @param productIdOrSku the product id or sku of the product to update
      * @param the attributes a non empty map of attributes to update 
      */
     @Operation
     public void updateStockItem(@Parameter String productIdOrSku, @Parameter Map<String, Object> attributes)
     {
         inventoryClient.updateStockItem(productIdOrSku, attributes);
     }
 
     /**
      * Answers the list of countries
      * Example:
      * {@code <magento:list-directory-countries"/>}
      * @return a collection of countries attributes
      */
     @Operation
     public List<Map<String, Object>> listDirectoryCountries()
     {
         return directoryClient.listDirectoryCountries();
     }
     
 
     /**
      * Answers a list of regions for the given county
      * Example:
      * {@code <magento:list-directory-regions countryId="#[map-payload:countryId]"/>}
      * 
      * @param countryId the country code, in ISO2 or ISO3 format
      * @return the collection of regions attributes
      */
     @Operation
     public List<Map<String, Object>> listDirectoryRegions(@Parameter String countryId)
     {
         return directoryClient.listDirectoryRegions(countryId);
     }
 
     /**
      * Links two products, given its source and destination productIdOrSku.
      * Example:
      * 
      * {@code  <magento:add-product-link type="#[map-payload:type]"     
      *                          productId="#[map-payload:productId]"
      *                          linkedProductIdOrSku="#[map-payload:linkedProductId]"/>}
      * @param type
      *            the product type
      * @param productId
      *            the id of the source product. Use it instead of productIdOrSku
      *            in case you are sure the source product identifier is a
      *            product id
      * @param productSku
      *            the sku of the source product. Use it instead of productIdOrSku
      *            in case you are sure the source product identifier is a
      *            product sku
      * @param productIdOrSku
      *            the id or sku of the source product.
      * @param linkedProductIdOrSku
      *            the destination product id or sku.
      * @param attributes
      *            the link attributes
      */
     @Operation
     public void addProductLink(@Parameter String type,
                                @Parameter(optional = true) Integer productId,
                                @Parameter(optional = true) String productSku,
                                @Parameter(optional = true) String productIdOrSku,
                                @Parameter String linkedProductIdOrSku,
                                @Parameter(optional = true) Map<String, Object> attributes)
     {
         catalogClient.addProductLink(type, from(productSku, productId, productIdOrSku), linkedProductIdOrSku,
             attributes);
     }
 
     /**
      * Creates a new product media. See catalog-product-attribute-media-create SOAP
      * method. 
      * Example:
      * 
      * {@code  
      *   <magento:create-product-attribute-media 
      *        content="#[map-payload:content]" 
      *       productId="#[map-payload:productId]"
      *       fileName="#[map-payload:fileName]" 
      *       mimeType="JPEG">
      *       <magento:attributes>
      *           <magento:attribute key="label" value="#[map-payload:label]"/>
      *           <magento:attribute key="position" value="#[map-payload:position]"/>
      *       </magento:attributes>
      *   </magento:create-product-attribute-media>}
      * 
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku
      *            in case you are sure the product identifier is a
      *            product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku
      *            in case you are sure the product identifier is a
      *            product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @param attributes the media attributes
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      * @param content the image to upload
      * @param mimeType the mimetype
      * @param baseFileName the base name of the new remote image
      * @return the new image filename
      */
     @Operation
     public String createProductAttributeMedia(@Parameter(optional = true) Integer productId,
                                               @Parameter(optional = true) String productSku,
                                               @Parameter(optional = true) String productIdOrSku,
                                               @Parameter(optional = true) Map<String, Object> attributes,
                                               @Parameter(optional = true) String storeViewIdOrCode,
                                               @Parameter InputStream content,
                                               @Parameter MediaMimeType mimeType,
                                               @Parameter(optional = true) String baseFileName) 
     {
         return catalogClient.createProductAttributeMedia(from(productSku, productId, productIdOrSku),
             attributes, content, mimeType, baseFileName, storeViewIdOrCode);
     }
 
     /**
      * Removes a product image. See catalog-product-attribute-media-remove SOAP
      * method.
      * Example:
      * 
      * {@code <magento:delete-product-attribute-media 
      *              productSku="#[map-payload:productSku]" 
      *              fileName="#[map-payload:fileName]"/>}
      * 
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku
      *            in case you are sure the product identifier is a
      *            product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku
      *            in case you are sure the product identifier is a
      *            product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @param fileName the remote media file to delete
      */
     @Operation
     public void deleteProductAttributeMedia(@Parameter(optional = true) Integer productId,
                                             @Parameter(optional = true) String productSku,
                                             @Parameter(optional = true) String productIdOrSku,
                                             @Parameter String fileName)
 
     {
         catalogClient.deleteProductAttributeMedia(from(productSku, productId, productIdOrSku), fileName);
     }
 
     /**
      * Deletes a product's link.
      * 
      * Example:
      * 
      * {@code <magento:delete-product-link 
      *              type="#[map-payload:type]"                    
      *              productId="#[map-payload:productId]"
      *              linkedProductIdOrSku="#[map-payload:linkedProductId]"/>}
      * 
      * @param type link type
      * @param productId
      *            the id of the source product. Use it instead of productIdOrSku
      *            in case you are sure the source product identifier is a
      *            product id
      * @param productSku
      *            the sku of the source product. Use it instead of productIdOrSku
      *            in case you are sure the source product identifier is a
      *            product sku
      * @param productIdOrSku
      *            the id or sku of the source product.
      * @param linkedProductIdOrSku
      */
     @Operation
     public void deleteProductLink(@Parameter String type,
                                   @Parameter(optional = true) Integer productId,
                                   @Parameter(optional = true) String productSku,
                                   @Parameter(optional = true) String productIdOrSku,
                                   @Parameter String linkedProductIdOrSku) 
     {
         catalogClient.deleteProductLink(type, from(productSku, productId, productIdOrSku), linkedProductIdOrSku);
     }
 
 
     /**
      * Lists linked products to the given product and for the given link type.
      * Example:
      * 
      * {@code <magento:get-product-attribute-media 
      *              productIdOrSku="#[map-payload:productIdOrSku]"
      *              fileName="#[map-payload:fileName]"
      *              storeViewIdOrCode="#[map-payload:storeViewIdOrCode]"/>} 
      * @param type
      *            the link type
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @return the list of links to the product
      */
     @Operation
     public Map<String, Object> getProductAttributeMedia(@Parameter(optional = true) Integer productId,
                                                         @Parameter(optional = true) String productSku,
                                                         @Parameter(optional = true) String productIdOrSku,
                                                         @Parameter String fileName,
                                                         @Parameter(optional = true) String storeViewIdOrCode)
     {
         return catalogClient.getProductAttributeMedia(from(productSku, productId, productIdOrSku), fileName, storeViewIdOrCode);
     }
     
     /**
      * Answers the current default catalog store view id for this session
      * @return the current default store view id
      */
     @Operation
     public int getCatalogCurrentStoreView() 
     {
         return catalogClient.getCatalogCurrentStoreView();
     }
     
     /**
      * Set the default catalog store view for this session
      * 
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      *            the id or code of the store view to set as default for this
      *            session
      */
     @Operation
     public void updateCategoryAttributeStoreView(@Parameter String storeViewIdOrCode) 
     {
         catalogClient.updateCatalogCurrentStoreView(storeViewIdOrCode);
     }
 
     /**
      * Retrieve product image types. See catalog-product-attribute-media-types SOAP
      * method.
      * 
      * Example: 
      * {@code <magento:list-category-attributes setId="#[map-payload:setId]"/>}
      * 
      * @param setId the target set
      * @return the list of category attributes
      */
     @Operation
     public List<Map<String, Object>> listCategoryAttributes() 
     {
         return catalogClient.listCategoryAttributes();
     }
 
     /**
      * Retrieves attribute options. See catalog-category-attribute-options SOAP
      * method.
      * 
      * Example:
      * {@code <magento:list-category-attributes-options attributeId="#[map-payload:attributeId]"/>}
      * 
      * @param attributeId the target attribute whose options will be retrieved
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store 
      * @return the list of category attribute options
      */
     @Operation
     public List<Map<String, Object>> listCategoryAttributeOptions(@Parameter String attributeId,
                                                                   @Parameter(optional = true) String storeViewIdOrCode)
     {
         return catalogClient.listCategoryAttributeOptions(attributeId, storeViewIdOrCode);
     }
 
     /**
      * Retrieves product image list. See catalog-product-attribute-media-list SOAP
      * method
      * Example:
      * {@code   <magento:list-product-attribute-media
      *                  productId="#[map-payload:productId]"
      *                  storeViewIdOrCode="#[map-payload:storeViewIdOrCode]"/>}
      * 
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      * @return the list of product images attributes
      */
     @Operation
     public List<Map<String, Object>> listProductAttributeMedia(@Parameter(optional = true) Integer productId,
                                                                @Parameter(optional = true) String productSku,
                                                                @Parameter(optional = true) String productIdOrSku,
                                                                @Parameter(optional = true) String storeViewIdOrCode)
     {
         return catalogClient.listProductAttributeMedia(from(productSku, productId, productIdOrSku), storeViewIdOrCode);
     }
 
     /**
      * Retrieve product image types. See catalog-product-attribute-media-types SOAP
      * method.
      * 
      * Example:
      * {@code <magento:list-product-attribute-media-types setId="#[map-payload:setId]"/>}
      * 
      * @param setId the setId
      * @return the list of attribute media types
      */
     @Operation
     public List<Map<String, Object>> listProductAttributeMediaTypes(@Parameter int setId) 
     {
         return catalogClient.listProductAttributeMediaTypes(setId);
     }
     
 
     /**
      * Answers the product attribute options. See catalog-product-attribute-options
      * SOAP method.
      * 
      * Example:
      * {@code <magento:list-product-attribute-options attributeId="#[map-payload:attributeId]"/>}
      * 
      * @param attributeId the target attribute whose options will be listed
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      * @return the attributes list
      */
     @Operation
     public List<Map<String, Object>> listProductAttributeOptions(@Parameter String attributeId, 
                                                                  @Parameter(optional = true) String storeViewIdOrCode)
     {
         return catalogClient.listProductAttributeOptions(attributeId, storeViewIdOrCode);
     }
 
     /**
      * Retrieves product attributes list. See catalog-product-attribute-list SOAP
      * methods
      * 
      * Example:
      * 
      * {@code <magento:list-product-attributes setId="#[map-payload:setId]"/>}
      * 
      * @param setId
      * @return the list of product attributes
      */
     @Operation
     public List<Map<String, Object>> listProductAttributes(@Parameter int setId) 
     {
         return catalogClient.listProductAttributes(setId);
     }
 
     /**
      * Retrieves product attribute sets. See catalog-product-attribute-set-list SOAP
      * method.
      * 
      * Example:
      * 
      * {@code <magento:list-product-attribute-sets/>}
      * 
      * @return The list of product attributes sets
      */
     @Operation
     public List<Map<String, Object>> listProductAttributeSets() 
     {
         return catalogClient.listProductAttributeSets();
     }
 
     /**
      * Retrieve product tier prices. See catalog-product-attribute-tier-price-info
      * SOAP Method.
      * Example:
      * 
      * {@code <magento:list-product-attribute-tier-prices productIdOrSku="#[map-payload:productIdOrSku]"/>}
      * 
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @return the list of product attributes
      */
     @Operation
     public List<Map<String, Object>> listProductAttributeTierPrices(@Parameter(optional = true) Integer productId,
                                                                     @Parameter(optional = true) String productSku,
                                                                     @Parameter(optional = true) String productIdOrSku)
     {
         return catalogClient.listProductAttributeTierPrices(from(productSku, productId, productIdOrSku));
     }
 
 
     /**
      * Lists linked products to the given product and for the given link type
      * 
      * @param type the link type
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @return the list of links to the product
      */
     @Operation
     public List<Map<String, Object>> listProductLink(String type,     
                                                      @Parameter(optional = true) Integer productId,
                                                      @Parameter(optional = true) String productSku,
                                                      @Parameter(optional = true) String productIdOrSku)
     {
         return catalogClient.listProductLink(type, from(productSku, productId, productIdOrSku));
     }
 
     /**
      * Lists all the attributes for the given product link type
      * 
      * Example:
      * 
      * {@code <magento:list-product-link-attributes type="#[map-payload:type]" />}
      * 
      * @param type the product type
      * @return the listing of product attributes
      */
     @Operation
     public List<Map<String, Object>> listProductLinkAttributes(@Parameter String type) 
     {
         return catalogClient.listProductLinkAttributes(type);
     }
 
     /**
      * Answers product link types
      * Example:
      * 
      * {@code <magento:list-product-link-types />}
      * 
      * @return the list of product link types
      */
     @Operation
     public List<String> listProductLinkTypes() 
     {
         return catalogClient.listProductLinkTypes();
     }
 
     /**
      * Answers product types. See catalog-product-type-list SOAP method
      * Example:
      * 
      * {@code <magento:list-product-types />}
      * 
      * @return the list of product types
      */
     @Operation
     public List<Map<String, Object>> listProductTypes() 
     {
         return catalogClient.listProductTypes();
     }
     
     
     /**
      * Updates product media. See catalog-product-attribute-media-update
      * Example:
      * {@code <magento:update-product-attribute-media fileName="#[map-payload:fileName]" productId="#[map-payload:productId]">
      *          <magento:attributes>
      *              <magento:attribute key="label" value="#[map-payload:label]"/>
      *          </magento:attributes>
      *        </magento:update-product-attribute-media>}
      * 
      *  @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @param fileName the name of the remote media file to update 
      * @param attributes
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      */
     @Operation
     public void updateProductAttributeMedia(@Parameter(optional = true) Integer productId,
                                             @Parameter(optional = true) String productSku,
                                             @Parameter(optional = true) String productIdOrSku,
                                             @Parameter String fileName,
                                             @Parameter Map<String, Object> attributes,
                                             @Parameter(optional = true) String storeViewIdOrCode)
     {
         catalogClient.updateProductAttributeMedia(from(productSku, productId, productIdOrSku), fileName,
             attributes, storeViewIdOrCode);
     }
 
     
     /**
      * Updates a single product tier price. See catalog-product-attribute-tier-price-update
      * SOAP method.
      * 
      * Example:
      * 
      * {@code <magento:update-product-attribute-tier-price 
      *          productSku="#[map-payload:productSku]"> 
      *         <magento:attributes>
      *          <magento:attribute key="price" value="#[map-payload:price]"/>
      *          <magento:attribute key="qty" value="#[map-payload:quantity]"/>
      *          <magento:attribute key="website" value="#[map-payload:website]"/>
      *          <magento:attribute key="customer_group_id" value="#[map-payload:customerGroupId]"/>
      *         </magento:attributes>
      *        <magento:update-product-attribute-tier-price/>} 
      * 
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @param attributes the tier price to update.
      */
     @Operation
     public void updateProductAttributeTierPrice(@Parameter(optional = true) Integer productId,
                                                 @Parameter(optional = true) String productSku,
                                                 @Parameter(optional = true) String productIdOrSku,
                                                 @Parameter Map<String, Object> attributes)
     {
         catalogClient.updateProductAttributeTierPrice(from(productSku, productId, productIdOrSku), attributes);
     }
 
     /**
      * Update product link
      * Example:
      * 
      * {@code <magento:update-product-link type="#[map-payload:type]" productId="#[map-payload:productId]"
      *                 linkedProductIdOrSku="#[map-payload:linkedProductId]">
      *          <magento:attributes>
      *              <magento:attribute key="qty" value="#[map-payload:qty]"/>
      *          </magento:attributes>                   
      *        </magento:update-product-link>}
      * 
      * @param type the link type
       *@param productId
      *            the id of the source product. Use it instead of productIdOrSku
      *            in case you are sure the source product identifier is a
      *            product id
      * @param productSku
      *            the sku of the source product. Use it instead of productIdOrSku
      *            in case you are sure the source product identifier is a
      *            product sku
      * @param productIdOrSku
      *            the id or sku of the source product.
      * @param linkedProductIdOrSku
      *            the destination product id or sku.
      * @param attributes the link attributes
      */
     @Operation
     public void updateProductLink(@Parameter String type,
                                   @Parameter(optional = true) Integer productId,
                                   @Parameter(optional = true) String productSku,
                                   @Parameter(optional = true) String productIdOrSku,
                                   @Parameter String linkedProductIdOrSku,
                                   @Parameter Map<String, Object> attributes)
     {
         catalogClient.updateProductLink(type, from(productSku, productId, productIdOrSku), linkedProductIdOrSku,
             attributes);
     }
     
     /**
      * Lists product of a given category. See  catalog-category-assignedProducts SOAP method.   
      * Example:
      * 
      * {@code <magento:list-category-products  categoryId="#[map-payload:categoryId]"/>} 
      * @param categoryId the category 
      * @return the listing of category products
      */
     @Operation
     public List<Map<String, Object>> listCategoryProducts(int categoryId) 
     {
         return catalogClient.listCategoryProducts(categoryId);
     }
 
     /**
      * Assign product to category. See catalog-category-assignProduct SOAP method
      * 
      * @param categoryId the category where the given product will be added 
      * @param productId the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku the id or sku of the product.
      * @param position 
      */
     @Operation
     public void addCategoryProduct(int categoryId,
                                    @Parameter(optional = true) Integer productId,
                                    @Parameter(optional = true) String productSku,
                                    @Parameter(optional = true) String productIdOrSku,
                                    String position) 
     {
         catalogClient.addCategoryProduct(categoryId, from(productSku, productId, productIdOrSku), position);
     }
      
     /**
      * Creates a new category. See catalog-category-create SOAP method.
      * 
      * @param parentId the parent category id
      * @param attributes the new category attributes
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      * @return the new category id
      */
     @Operation
     public int createCategory(int parentId,
                               Map<String, Object> attributes,
                               @Parameter(optional = true) String storeViewIdOrCode) 
     {
         return catalogClient.createCategory(parentId, attributes, storeViewIdOrCode);
     }
 
     /**
      * Deletes a category. See  catalog-category-delete SOAP method
      * Example:
      * {@code <magento:delete-category categoryId="#[map-payload:categoryId]"/>}
      *  
      * @param categoryId the category to delete
      */
      @Operation
      public void deleteCategory(int categoryId) 
      {
           catalogClient.deleteCategory(categoryId);
      }
 
     /**
      * Answers category attributes. See catalog-category-info SOAP method. 
      * Example:
      * {@code <magento:get-category categoryId="#[map-payload:categoryId]"/>}
      * 
      * @param categoryId the category whose attributes will be retrieved
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified
      *            for using current store
      * @param attributeNames the category attributes that will be retrieved  
      * @return the category attributes
      */
     @Operation
     public Map<String, Object> getCategory(@Parameter int categoryId,
                                            @Parameter(optional = true) String storeViewIdOrCode,
                                            @Parameter List<String> attributeNames) 
     {
         return catalogClient.getCategory(categoryId, storeViewIdOrCode, attributeNames);
     }
 
     /** 
      * Answers levels of categories for a website, store view and parent category
      * Example:
      * 
      * {@code <magento:list-category-levels/> }
      * 
      * @param website
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      * @param parentCategoryId the parent category of the categories that will be listed
      * @return the list of categories attributes
      */
     @Operation
     public List<Map<String, Object>> listCategoryLevels(@Parameter(optional = true) String website,
                                                         @Parameter(optional = true) String storeViewIdOrCode,
                                                         @Parameter(optional = true) String parentCategoryId)
         
     {
         return catalogClient.listCategoryLevels(website, storeViewIdOrCode, parentCategoryId);
     }
 
     /**
      * Move category in tree. See  catalog-category-move SOAP method. Please make sure that you are not 
      * moving category to any of its own children. There are no
      * extra checks to prevent doing it through webservices API, and you won’t be
      * able to fix this from admin interface then .
      * 
      * Example:
      * 
      * {@code <magento:move-category categoryId="#[map-payload:categoryId]" parentId="#[map-payload:afterId]"/> }
      *  
      * @param categoryId the id of the category to be moved 
      * @param parentId the new parent category id
      * @param afterId an optional category id for use as reference in the positioning of the moved category  
      */
     @Operation
     public void moveCategory(@Parameter int categoryId,
                              @Parameter int parentId,
                              @Parameter(optional = true) String afterId) 
     {
         catalogClient.moveCategory(categoryId, parentId, afterId);
     }
      
     /**
      * Remove a product assignment. See catalog-category-removeProduct SOAP method. 
      * Example:
      * 
      * {@code <magento:delete-category-product 
      *              categoryId="#[map-payload:categoryId]"
      *              productSku="#[map-payload:productSku]"/>}
      * @param categoryId the category to delete
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      */
      @Operation
     public void deleteCategoryProduct(@Parameter int categoryId,
                                       @Parameter(optional = true) Integer productId,
                                       @Parameter(optional = true) String productSku,
                                       @Parameter(optional = true) String productIdOrSku)
      {
           catalogClient.deleteCategoryProduct(categoryId, from(productSku, productId, productIdOrSku));
      }
      
     /**
      * Answers the category tree. 
      * See  catalog-category-tree SOAP method. 
      * @param parentId
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      * @return a category tree attributes
      */
      @Operation
     public Map<String, Object> getCategoryTree(@Parameter String parentId,
                                                @Parameter(optional = true) String storeViewIdOrCode)
                
      {
           return catalogClient.getCategoryTree(parentId, storeViewIdOrCode);
      }
      
      /**
      * Updates a category. See catalog-category-update SOAP method
      * 
      * Example:
      * 
      * {@code <magento:update-category categoryId="#[map-payload:categoryId]" />  
      *          <magento:attributes>
      *              <magento:attribute key="name" value="#[map-payload:categoryName]"/>
      *          </magento:attributes>
      *       </magento:update-category>} 
      * 
      * @param categoryId the category to update
      * @param attributes the category new attributes
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      */
      @Operation
     public void updateCategory(@Parameter int categoryId,
                                @Parameter Map<String, Object> attributes,
                                @Parameter(optional = true) String storeViewIdOrCode) 
      {
           catalogClient.updateCategory(categoryId, attributes, storeViewIdOrCode);
      }
 
     /**
      * Updates a category product 
      * 
      * Example:
      * 
      * {@code <magento:update-category-product 
      *              categoryId="#[header:categoryId]" 
      *              position="#[header:position]"
      *              productSku="#[header:productSku]"/>} 
      * @param categoryId the category id
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @param position the category position for ordering the category inside its level 
      */
     @Operation
     public void updateCategoryProduct(@Parameter int categoryId,
                                       @Parameter(optional = true) Integer productId,
                                       @Parameter(optional = true) String productSku,
                                       @Parameter(optional = true) String productIdOrSku,
                                       @Parameter String position)
      {
           catalogClient.updateCategoryProduct(categoryId, from(productSku, productId, productIdOrSku), position);
      }
 
     /**
      * Lists inventory stock items.
      *  Example: 
      *  {@code <magento:list-inventory-stock-items >
      *          <magento:productIdOrSkus>
      *              <magento:productIdOrSku>1560</magento:productIdOrSku>
      *              <magento:productIdOrSku>10036</magento:productIdOrSku>
      *              <magento:productIdOrSku>9868</magento:productIdOrSku>
      *          </magento:productIdOrSkus>
      *         </magento:list-inventory-stock-items>}
      * 
      * @param products the list of product ids and/or skus whose attributes will be retrieved
      * @return the list of attributes
      */
      @Operation
      public List<Map<String, Object>> listInventoryStockItems(@Parameter List<String> productIdOrSkus)
      {
           return catalogClient.listInventoryStockItems(productIdOrSkus);
      }
 
     /**
      * Updates an stock inventory item
      * 
      * {@code  <magento:update-product-inventory-stock-item  productIdOrSku="#[map-payload:productIdOrSku]">
      *            <magento:attributes>
      *              <magento:attribute key="qty" value="#[map-payload:quantity]"/>
      *            </magento:attributes>
      *         </magento:update-product-inventory-stock-item>}
      *         
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @param attributes the new attributes of the stock item
      */
     @Operation
     public void updateInventoryStockItem(@Parameter(optional = true) Integer productId,
                                          @Parameter(optional = true) String productSku,
                                          @Parameter(optional = true) String productIdOrSku,
                                          @Parameter Map<String, Object> attributes) 
      {
           catalogClient.updateInventoryStockItem(from(productSku, productId, productIdOrSku), attributes);
      }
      
        
     /**
      * Creates a new product
      * 
      * Example:
      * {@code <magento:create-product set="4" sku="78962" type="simple"/>}
      * 
      * @param type the new product's type
      * @param set the new product's set
      * @param sku the new product's sku
      * @param attributes the attributes of the new product
      * @return the new product's id
      */
     @Operation
     public int createProduct(@Parameter String type,
                              @Parameter int set,
                              @Parameter String sku,
                              @Parameter(optional = true) Map<String, Object> attributes)
     {
         return catalogClient.createProduct(type, set, sku, attributes);
     }
 
      
      /**
      * Deletes a product. See catalog-product-delete SOAP method.
      * 
      * Example:
      * 
      * {@code <magento:delete-product productId="#[map-payload:productId]" />}
      *  
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      */
     @Operation
     public void deleteProduct(@Parameter(optional = true) Integer productId,
                               @Parameter(optional = true) String productSku,
                               @Parameter(optional = true) String productIdOrSku)
     {
         catalogClient.deleteProduct(from(productSku, productId, productIdOrSku));
     }
     
     /**
      * Answers a product special price. See catalog-product-getSpecialPrice SOAP method.
      * 
      * Example: 
      * 
      * {@code <magento:get-product-special-price productId="#[map-payload:productId]"/>}
      * 
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      * @param productId.getIdentifierType()
      * @return the product special price attributes
      */
     @Operation
     public Map<String, Object> getProductSpecialPrice(@Parameter(optional = true) Integer productId,
                                                       @Parameter(optional = true) String productSku,
                                                       @Parameter(optional = true) String productIdOrSku,
                                                       @Parameter(optional = true) String storeViewIdOrCode)
     {
         return catalogClient.getProductSpecialPrice(from(productSku, productId, productIdOrSku),
             storeViewIdOrCode);
      }
      
      /**
      * Answers a product's specified attributes. At least one of attributNames or
      * additionalAttributeNames must be non null and non empty. See
      * catalog-product-info SOAP method
      * 
      * {@code   <magento:get-product  productIdOrSku="#[map-payload:productIdOrSku]" storeViewIdOrCode="#[map-payload:storeViewIdOrCode]">
      *              <magento:additionalAttributeNames>
      *                  <magento:additionalAttributeName>keyboard_distribution_type</magento:additionalAttributeName>
      *              </magento:additionalAttributeNames>
      *          </magento:get-product>}    
      * 
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      * @param attributeNames the list of standard attributes to be returned
      * @param additionalAttributeNames the list of non standard attributes to be returned in the additionalAttributes attribute 
      * @return the attributes
      */
     @Operation
     public Map<String, Object> getProduct(@Parameter(optional = true) Integer productId,
                                           @Parameter(optional = true) String productSku,
                                           @Parameter(optional = true) String productIdOrSku,
                                           @Parameter(optional = true) String storeViewIdOrCode,
                                           @Parameter(optional = true) List<String> attributesNames,
                                           @Parameter(optional = true) List<String> additionalAttributeNames)
     {
           return catalogClient.getProduct(from(productSku, productId, productIdOrSku), storeViewIdOrCode, attributesNames, additionalAttributeNames);
      }
      
      
      /**
      * Retrieve products list by filters. See catalog-product-list SOAP method.
      * 
      * Example:
      * 
      * {@code <magento:list-products/>}
      *    
     * @param filters optional filtering expression - one or more comma-separated
      *            unary or binary predicates, one for each filter, in the form
      *            filterType(attributeName, value), for binary filters or
      *            filterType(attributeName), for unary filters, where filterType is
      *            istrue, isfalse or any of the Magento standard filters. Non-numeric
      *            values need to be escaped using simple quotes.
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      * @return the list of product attributes that match the given optional filtering expression
      */
     @Operation
    public List<Map<String, Object>> listProducts(@Parameter(optional = true) String filters,
                                                   @Parameter(optional = true) String storeViewIdOrCode)
     {
          return catalogClient.listProducts(filters, storeViewIdOrCode);
      }
 
     /**
      * Sets a product special price. See catalog-product-setSpecialPrice SOAP method
      * 
      * Example:
      * 
      * {@code <magento:update-product-special-price specialPrice="#[variable:session:specialPrice]" productId="#[variable:session:productId]"/>} 
      * 
      * @param productId the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku the id or sku of the product.
      * @param specialPrice the special price to set
      * @param fromDate optional start date of the special price period 
      * @param toDate optional end date of the special price period
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      */
     @Operation
     public void updateProductSpecialPrice(@Parameter(optional = true) Integer productId,
                                           @Parameter(optional = true) String productSku,
                                           @Parameter(optional = true) String productIdOrSku,
                                           @Parameter String specialPrice,
                                           @Parameter(optional = true) String fromDate,
                                           @Parameter(optional = true) String toDate,
                                           @Parameter(optional = true) String storeViewIdOrCode)
     {
         catalogClient.updateProductSpecialPrice(from(productSku, productId, productIdOrSku), specialPrice,
             fromDate, toDate, storeViewIdOrCode);
     }
 
      /**
      * Updates a product. See catalog-category-updateProduct SOAP method 
      * Example:
      * 
      * {@code <magento:update-product productIdOrSku="#[map-payload:productIdOrSku]">
      *          <magento:attributes>
      *              <magento:attribute key="name" value="#[map-payload:name]"/>
      *          </magento:attributes>
      *        </magento:update-product>}
      *        
      * @param productId
      *            the id of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product id
      * @param productSku
      *            the sku of the product. Use it instead of productIdOrSku in
      *            case you are sure the product identifier is a product sku
      * @param productIdOrSku
      *            the id or sku of the product.       
      * @param attributes the not empty map of product attributes to update 
      * @param storeViewIdOrCode the id or code of the target store. Left unspecified for using current store
      */
     @Operation
     public void updateProduct(@Parameter(optional = true) Integer productId,
                               @Parameter(optional = true) String productSku,
                               @Parameter(optional = true) String productIdOrSku,
                               @Parameter Map<String, Object> attributes,
                               @Parameter(optional = true) String storeViewIdOrCode) 
      {
           catalogClient.updateProduct(from(productSku, productId, productIdOrSku), attributes, storeViewIdOrCode);
      }
 
     @SuppressWarnings("unchecked")
     public void setOrderClient(MagentoOrderClient<?, ?, ?> magentoOrderClient)
     {
         this.orderClient = MagentoClientAdaptor.adapt(MagentoOrderClient.class, magentoOrderClient);
     }
 
     @SuppressWarnings("unchecked")
     public void setCustomerClient(MagentoCustomerClient<?, ?, ?> customerClient)
     {
         this.customerClient = MagentoClientAdaptor.adapt(MagentoCustomerClient.class, customerClient);
     }
 
     @SuppressWarnings("unchecked")
     public void setInventoryClient(MagentoInventoryClient<?, ?> inventoryClient)
     {
         this.inventoryClient = MagentoClientAdaptor.adapt(MagentoInventoryClient.class, inventoryClient);
     }
 
     @SuppressWarnings("unchecked")
     public void setDirectoryClient(MagentoDirectoryClient<?, ?> directoryClient)
     {
         this.directoryClient = MagentoClientAdaptor.adapt(MagentoDirectoryClient.class, directoryClient);
     }
 
     @SuppressWarnings("unchecked")
     public void setCatalogClient(MagentoCatalogClient<?, ?, ?> catalogClient)
     {
         this.catalogClient = MagentoClientAdaptor.adapt(MagentoCatalogClient.class, catalogClient);
     }
 
     private class PortProviderInitializer
     {
         private DefaultAxisPortProvider provider;
 
         public AxisPortProvider getPortProvider()
         {
             if (provider == null)
             {
                 provider = new DefaultAxisPortProvider(username, password, address);
             }
             return provider;
         }
     }
 }
