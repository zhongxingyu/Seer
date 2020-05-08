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
 
 import com.magento.api.AssociativeEntity;
 import com.magento.api.ComplexFilter;
 import com.magento.api.Filters;
 import com.magento.api.Mage_Api_Model_Server_V2_HandlerPortType;
 import com.magento.api.OrderItemIdQty;
 import com.magento.api.SalesOrderEntity;
 import com.magento.api.SalesOrderListEntity;
 import com.magento.api.SalesOrderShipmentEntity;
 import edu.emory.mathcs.backport.java.util.Collections;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.mule.module.magento.api.AxisPortProvider;
 import org.mule.module.magento.api.catalog.AxisMagentoCatalogClient;
 import org.mule.module.magento.api.customer.AxisMagentoInventoryClient;
 import org.mule.module.magento.api.directory.AxisMagentoDirectoryClient;
 import org.mule.module.magento.api.inventory.AxisMagentoCustomerClient;
 import org.mule.module.magento.api.order.AxisMagentoOrderClient;
 import org.mule.module.magento.api.order.model.Carrier;
import org.mule.module.magento.api.shoppingCart.AxisMagentoShoppingCartClient;
 
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 @SuppressWarnings("serial")
 public class MagentoCloudConnectorUnitTest
 {
     private static final String ORDER_ID = "10001";
     private MagentoCloudConnector connector;
     private AxisPortProvider portProvider;
     private Mage_Api_Model_Server_V2_HandlerPortType port;
 
     @Before
     public void setup() throws Exception
     {
         connector = new MagentoCloudConnector();
         portProvider = mock(AxisPortProvider.class);
         port = mock(Mage_Api_Model_Server_V2_HandlerPortType.class);
         connector.setOrderClient(new AxisMagentoOrderClient(portProvider));
         connector.setCatalogClient(new AxisMagentoCatalogClient(portProvider));
         connector.setCustomerClient(new AxisMagentoCustomerClient(portProvider));
         connector.setDirectoryClient(new AxisMagentoDirectoryClient(portProvider));
         connector.setInventoryClient(new AxisMagentoInventoryClient(portProvider));
        connector.setShoppingCartClient(new AxisMagentoShoppingCartClient(portProvider));
         connector.initialise();
         when(portProvider.getPort()).thenReturn(port);
     }
 
     @Test
     public void testSalesOrdersListNoFilters() throws Exception
     {
         when(port.salesOrderList(anyString(), eq(new Filters()))).thenReturn(
             new SalesOrderListEntity[]{new SalesOrderListEntity()});
         assertEquals(1, connector.listOrders(null).size());
     }
 
     @Test
     public void testSalesOrdersList() throws Exception
     {
         when(port.salesOrderList(anyString(), // 
             eq(new Filters(null, new ComplexFilter[]{//
                 new ComplexFilter("customer_id", new AssociativeEntity("eq", "500"))})))) //
         .thenReturn(new SalesOrderListEntity[]{new SalesOrderListEntity()});
         assertEquals(1, connector.listOrders("eq(customer_id, 500)").size());
     }
 
     @Test
     public void testSalesOrderInfo() throws Exception
     {
         when(port.salesOrderInfo(anyString(), eq(ORDER_ID))).thenReturn(new SalesOrderEntity());
         connector.getOrder(ORDER_ID);
     }
 
     @Test
     public void testSalesOrderHold() throws Exception
     {
         connector.holdOrder(ORDER_ID);
         verify(port).salesOrderHold(anyString(), eq(ORDER_ID));
     }
 
     @Test
     public void testSalesOrderUnhold() throws Exception
     {
         connector.unholdOrder(ORDER_ID);
         verify(port).salesOrderUnhold(anyString(), eq(ORDER_ID));
     }
 
     @Test
     public void testSalesOrderCancel() throws Exception
     {
         connector.cancelOrder(ORDER_ID);
         verify(port).salesOrderCancel(anyString(), eq(ORDER_ID));
     }
 
     @Test
     public void testSalesOrderAddComment() throws RemoteException
     {
         connector.addOrderComment(ORDER_ID, "status", "A comment", false);
         verify(port).salesOrderAddComment(anyString(), eq(ORDER_ID), eq("status"), eq("A comment"), eq("0"));
     }
 
     @Test
     public void testSalesOrderShipmentsList() throws RemoteException
     {
         SalesOrderShipmentEntity shipment = new SalesOrderShipmentEntity();
         shipment.setIs_active("1");
         when(port.salesOrderShipmentList(anyString(), eq(new Filters()))).thenReturn(
             new SalesOrderShipmentEntity[]{shipment});
         assertEquals(1, connector.listOrdersShipments("").size());
     }
 
     @Ignore
     @Test
     public void testSalesOrderShipmentInfo()
     {
         fail("Not yet implemented");
     }
 
     @Ignore
     @Test
     public void testSalesOrderShipmentComment()
     {
         fail("Not yet implemented");
     }
 
     @Test
     public void testSalesOrderShipmentGetCarriers() throws RemoteException
     {
         when(port.salesOrderShipmentGetCarriers(anyString(), eq(ORDER_ID))) //
         .thenReturn(new AssociativeEntity[]{new AssociativeEntity("FDX", "Fedex Express")});
         assertEquals(Collections.singletonList(new Carrier("FDX", "Fedex Express")),
             connector.getOrderShipmentCarriers(ORDER_ID));
     }
 
 
     @Test
     public void testSalesOrderShipmentAddTrack() throws RemoteException
     {
         connector.addOrderShipmentTrack("1", "carrier", "title", "track");
         verify(port).salesOrderShipmentAddTrack(anyString(), eq("1"), eq("carrier"), eq("title"), eq("track"));
     }
 
 
     @Test
     public void testSalesOrderShipmentRemoveTrack() throws RemoteException
     {
         connector.deleteOrderShipmentTrack("1", "id");
         verify(port).salesOrderShipmentRemoveTrack(anyString(), eq("1"), eq("id"));
     }
 
     @Test
     public void testSalesOrderShipmentCreate() throws RemoteException
     {
         connector.createOrderShipment("foo", new HashMap<Integer, Double>()
         {
             {
                 put(100, 10.0);
             }
         }, "comment", true, false);
         verify(port).salesOrderShipmentCreate(anyString(), eq("foo"),
             eq(new OrderItemIdQty[]{new OrderItemIdQty(100, 10)}), eq("comment"), eq(1), eq(0));
     }
 
     @Test
     public void testSalesOrderInvoicesList() throws RemoteException
     {
         connector.listOrdersInvoices("");
         verify(port).salesOrderInvoiceList(anyString(), eq(new Filters()));
     }
 
     @Test
     public void testSalesOrderInvoiceInfo() throws RemoteException
     {
         connector.getOrderInvoice("invoiceId");
         verify(port).salesOrderInvoiceInfo(anyString(), eq("invoiceId"));
     }
 
     @Test
     public void testSalesOrderInvoiceComment() throws RemoteException
     {
         connector.addOrderInvoiceComment("invoiceId", "comment", false, true);
         verify(port).salesOrderInvoiceAddComment(anyString(), eq("invoiceId"), eq("comment"), eq("0"), eq("1"));
     }
     
     @Test
     public void testListInventoryStockItems() throws Exception
     {
         String[] idsOrSkus = new String[]{"SK100", "155600", "7896"};
         connector.listInventoryStockItems(Arrays.asList(idsOrSkus));
         verify(port).catalogInventoryStockItemList(anyString(), eq(idsOrSkus));
     }
 
 
 }
