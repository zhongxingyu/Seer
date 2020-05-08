 /**
  * Mule Magento Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.magento.api.order;
 
 import com.magento.api.AssociativeEntity;
 import com.magento.api.OrderItemIdQty;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Transformer;
 import org.apache.commons.lang.BooleanUtils;
 import org.apache.commons.lang.Validate;
 import org.mule.module.magento.api.AbstractMagentoClient;
 import org.mule.module.magento.api.AxisPortProvider;
 import org.mule.module.magento.api.order.model.Carrier;
 import org.mule.module.magento.filters.FiltersParser;
 
 import javax.validation.constraints.NotNull;
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import static org.apache.commons.lang.BooleanUtils.toInteger;
 
 public class AxisMagentoOrderClient extends AbstractMagentoClient
     implements MagentoOrderClient<Object, Object[], RemoteException>
 {
 
     public AxisMagentoOrderClient(AxisPortProvider provider)
     {
         super(provider);
     }
 
     public Object[] listOrders(String filter) throws RemoteException
     {
         return getPort().salesOrderList(getSessionId(), FiltersParser.parse(filter));
     }
 
     public Object getOrder(String orderId) throws RemoteException
     {
         return getPort().salesOrderInfo(getSessionId(), orderId);
     }
 
     public void holdOrder(String orderId) throws RemoteException
     {
         getPort().salesOrderHold(getSessionId(), orderId);
     }
 
     public void unholdOrder(@NotNull String orderId) throws RemoteException
     {
         Validate.notNull(orderId);
         BooleanUtils.toBoolean(getPort().salesOrderUnhold(getSessionId(), orderId));
     }
 
     public void cancelOrder(@NotNull String orderId) throws RemoteException
     {
         Validate.notNull(orderId);
         getPort().salesOrderCancel(getSessionId(), orderId);
     }
 
     public void addOrderComment(@NotNull String orderId,
                                 @NotNull String status,
                                 @NotNull String comment,
                                 boolean sendEmail) throws RemoteException
     {
         Validate.notNull(orderId);
         Validate.notNull(status);
         Validate.notNull(comment);
         getPort().salesOrderAddComment(getSessionId(), orderId, status, comment, toIntegerString(sendEmail));
     }
 
     @NotNull
     public Object[] listOrdersShipments(String filter) throws RemoteException
     {
         return getPort().salesOrderShipmentList(getSessionId(), FiltersParser.parse(filter));
     }
 
     public Object getOrderShipment(String shipmentId) throws RemoteException
     {
         return getPort().salesOrderShipmentInfo(getSessionId(), shipmentId);
     }
 
     public void addOrderShipmentComment(@NotNull String shipmentId,
                                         String comment,
                                         boolean sendEmail,
                                         boolean includeCommentInEmail) throws RemoteException
     {
         Validate.notNull(shipmentId);
         Validate.notNull(comment);
         getPort().salesOrderShipmentAddComment(getSessionId(), shipmentId, comment,
             toIntegerString(sendEmail), toIntegerString(includeCommentInEmail));
     }
 
     @SuppressWarnings("unchecked")
     @NotNull
     public List<Carrier> getOrderShipmentCarriers(@NotNull String orderId) throws RemoteException
     {
         Validate.notNull(orderId);
         return (List<Carrier>) CollectionUtils.collect(Arrays.asList(getPort().salesOrderShipmentGetCarriers(
             getSessionId(), orderId)), new Transformer()
         {
             public Object transform(Object input)
             {
                 AssociativeEntity entity = (AssociativeEntity) input;
                 return new Carrier(entity.getKey(), entity.getValue());
             }
         });
     }
 
     public int addOrderShipmentTrack(@NotNull String shipmentId,
                                      @NotNull String carrier,
                                      @NotNull String title,
                                      @NotNull String trackNumber) throws RemoteException
     {
         Validate.notNull(shipmentId);
         Validate.notNull(carrier);
         Validate.notNull(title);
         Validate.notNull(trackNumber);
         return getPort().salesOrderShipmentAddTrack(getSessionId(), shipmentId, carrier, title, trackNumber);
     }
 
     public void deleteOrderShipmentTrack(@NotNull String shipmentId, @NotNull String trackId)
         throws RemoteException
     {
         Validate.notNull(shipmentId);
         Validate.notNull(trackId);
         getPort().salesOrderShipmentRemoveTrack(getSessionId(), shipmentId, trackId);
     }
 
     public String createOrderShipment(@NotNull String orderId,
                                      @NotNull Map<Integer, Double> itemsQuantities,
                                       String comment,
                                       boolean sendEmail,
                                       boolean includeCommentInEmail) throws RemoteException
     {
        return getPort().salesOrderShipmentCreate(getSessionId(), orderId, fromMap(itemsQuantities), comment,
             toInteger(sendEmail), toInteger(includeCommentInEmail));
     }
 
     public Object[] listOrdersInvoices(String filter) throws RemoteException
     {
         return getPort().salesOrderInvoiceList(getSessionId(), FiltersParser.parse(filter));
     }
 
     public Object getOrderInvoice(@NotNull String invoiceId) throws RemoteException
     {
         Validate.notNull(invoiceId);
         return getPort().salesOrderInvoiceInfo(getSessionId(), invoiceId);
     }
 
     public String createOrderInvoice(@NotNull String orderId,
                                      @NotNull Map<Integer, Double> itemsQuantities,
                                      String comment,
                                      boolean sendEmail,
                                      boolean includeCommentInEmail) throws RemoteException
     {
         Validate.notNull(orderId);
         return getPort().salesOrderInvoiceCreate(getSessionId(), orderId, fromMap(itemsQuantities), comment,
             toIntegerString(sendEmail), toIntegerString(includeCommentInEmail));
     }
 
     public void addOrderInvoiceComment(@NotNull String invoiceId,
                                        @NotNull String comment,
                                        boolean sendEmail,
                                        boolean includeCommentInEmail) throws RemoteException
     {
         Validate.notNull(invoiceId);
         Validate.notNull(comment);
         getPort().salesOrderInvoiceAddComment(getSessionId(), invoiceId, comment, toIntegerString(sendEmail),
             toIntegerString(includeCommentInEmail));
     }
 
     public void captureOrderInvoice(@NotNull String invoiceId) throws RemoteException
     {
         Validate.notNull(invoiceId);
         getPort().salesOrderInvoiceCapture(getSessionId(), invoiceId);
     }
 
     public void voidOrderInvoice(@NotNull String invoiceId) throws RemoteException
     {
         Validate.notNull(invoiceId);
         getPort().salesOrderInvoiceVoid(getSessionId(), invoiceId);
     }
 
     public void cancelOrderInvoice(@NotNull String invoiceId) throws RemoteException
     {
         Validate.notNull(invoiceId);
         getPort().salesOrderInvoiceCancel(getSessionId(), invoiceId);
     }
 
     private OrderItemIdQty[] fromMap(Map<Integer, Double> map)
     {
         OrderItemIdQty[] quantities = new OrderItemIdQty[map.size()];
         int i = 0;
         for (Entry<Integer, Double> entry : map.entrySet())
         {
             quantities[i] = new OrderItemIdQty(entry.getKey(), entry.getValue());
             i++;
         }
         return quantities;
     }
 
 }
