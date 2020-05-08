 /**
  * Mule Magento Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.magento.api.shoppingCart;
 
 import com.magento.api.*;
 import org.apache.commons.lang.Validate;
 import org.mule.module.magento.api.AbstractMagentoClient;
 import org.mule.module.magento.api.AxisPortProvider;
 
 import java.rmi.RemoteException;
 import java.util.List;
 import java.util.Map;
 
 import static org.mule.module.magento.api.util.MagentoObject.fromMap;
 import static org.mule.module.magento.api.util.MagentoObject.removeNullValues;
 
 public class AxisMagentoShoppingCartClient extends AbstractMagentoClient
     implements MagentoShoppingCartClient<Object, Object[], RemoteException>
 {
     public AxisMagentoShoppingCartClient(AxisPortProvider provider)
     {
         super(provider);
     }
 
     @Override
     public int createShoppingCart(String storeId) throws RemoteException
     {
         return getPort().shoppingCartCreate(getSessionId(), storeId);
     }
 
     @Override
     public Object getShoppingCartInfo(int quoteId, String storeId) throws RemoteException
     {
         return getPort().shoppingCartInfo(getSessionId(), quoteId, storeId);
     }
 
     @Override
     public Object[] listShoppingCartTotals(int quoteId, String storeId) throws RemoteException
     {
         return getPort().shoppingCartTotals(getSessionId(), quoteId, storeId);
     }
 
     @Override
     public String createShoppingCartOrder(int quoteId, String storeId, List<String> licenses) throws RemoteException
     {
        return getPort().shoppingCartOrder(getSessionId(), quoteId, storeId, licenses == null ? null : toArray(licenses, String.class));
     }
 
     @Override
     public Object[] listShoppingCartLicenses(int quoteId, String storeId) throws RemoteException
     {
         return getPort().shoppingCartLicense(getSessionId(), quoteId, storeId);
     }
 
     private ShoppingCartProductEntity[] buildProductsArrayWithOptions(List<Map<String, Object>> productsAttributes,
                                                                       List<Map<String, Object>> productsOptions,
                                                                       List<Map<String, Object>> productsBundleOptions,
                                                                       List<Map<String, Object>> productsBundleOptionsQty)
     {
         if (productsOptions != null)
         {
             Validate.isTrue(productsAttributes.size() == productsOptions.size(), "Options size must be equal to products size");
         }
         if (productsBundleOptions != null)
         {
             Validate.isTrue(productsAttributes.size() == productsBundleOptions.size(), "Bundle options size must be equal to products size");
         }
         if (productsBundleOptionsQty != null)
         {
             Validate.isTrue(productsAttributes.size() == productsBundleOptionsQty.size(), "Bundle options quantity size must be equal to products size");
         }
 
         ShoppingCartProductEntity[] products = fromMap(ShoppingCartProductEntity.class, productsAttributes);
 
         for (int i = 0; i < productsAttributes.size(); i++)
         {
             if (productsOptions != null)
             {
                 Map<String, Object> productOptions = productsOptions.get(i);
                 removeNullValues(productOptions);
                 if (productOptions != null && !productOptions.isEmpty())
                 {
                     products[i].setOptions(fromMap(productOptions));
                 }
             }
             if (productsBundleOptions != null)
             {
                 Map<String, Object> productBundleOption = productsBundleOptions.get(i);
                 removeNullValues(productBundleOption);
                 if (productBundleOption != null && !productBundleOption.isEmpty())
                 {
                     products[i].setBundle_option(fromMap(productBundleOption));
                 }
             }
             if (productsBundleOptionsQty != null)
             {
                 Map<String, Object> productBundleOptionQty = productsBundleOptionsQty.get(i);
                 removeNullValues(productBundleOptionQty);
                 if (productBundleOptionQty != null && !productBundleOptionQty.isEmpty())
                 {
                     products[i].setBundle_option_qty(fromMap(productBundleOptionQty));
                 }
             }
         }
 
         return products;
     }
 
     @Override
     public boolean addShoppingCartProduct(int quoteId,
                                           List<Map<String, Object>> productsAttributes,
                                           List<Map<String, Object>> productsOptions,
                                           List<Map<String, Object>> productsBundleOptions,
                                           List<Map<String, Object>> productsBundleOptionsQty,
                                           String storeId) throws RemoteException
     {
         ShoppingCartProductEntity[] products = buildProductsArrayWithOptions(productsAttributes,
                 productsOptions, productsBundleOptions, productsBundleOptionsQty);
         return getPort().shoppingCartProductAdd(getSessionId(), quoteId, products, storeId);
     }
 
     @Override
     public boolean updateShoppingCartProduct(int quoteId,
                                              List<Map<String, Object>> productsAttributes,
                                              List<Map<String, Object>> productsOptions,
                                              List<Map<String, Object>> productsBundleOptions,
                                              List<Map<String, Object>> productsBundleOptionsQty,
                                              String storeId) throws RemoteException
     {
 
         ShoppingCartProductEntity[] products = buildProductsArrayWithOptions(productsAttributes,
                 productsOptions, productsBundleOptions, productsBundleOptionsQty);
         return getPort().shoppingCartProductUpdate(getSessionId(), quoteId, products, storeId);
     }
 
     @Override
     public boolean removeShoppingCartProduct(int quoteId,
                                              List<Map<String, Object>> productsAttributes,
                                              List<Map<String, Object>> productsOptions,
                                              List<Map<String, Object>> productsBundleOptions,
                                              List<Map<String, Object>> productsBundleOptionsQty,
                                              String storeId) throws RemoteException
     {
         ShoppingCartProductEntity[] products = buildProductsArrayWithOptions(productsAttributes,
                 productsOptions, productsBundleOptions, productsBundleOptionsQty);
         return getPort().shoppingCartProductRemove(getSessionId(), quoteId, products, storeId);
     }
 
     @Override
     public Object[] listShoppingCartProducts(int quoteId, String storeId) throws RemoteException
     {
         return getPort().shoppingCartProductList(getSessionId(), quoteId, storeId);
     }
 
     @Override
     public boolean moveShoppingCartProductToCustomerQuote(int quoteId,
                                                           List<Map<String, Object>> productsAttributes,
                                                           List<Map<String, Object>> productsOptions,
                                                           List<Map<String, Object>> productsBundleOptions,
                                                           List<Map<String, Object>> productsBundleOptionsQty,
                                                           String storeId) throws RemoteException
     {
         ShoppingCartProductEntity[] products = buildProductsArrayWithOptions(productsAttributes,
                 productsOptions, productsBundleOptions, productsBundleOptionsQty);
         return getPort().shoppingCartProductMoveToCustomerQuote(getSessionId(), quoteId, products, storeId);
     }
 
     @Override
     public boolean setShoppingCartCustomer(int quoteId, Map<String, Object> customer, String storeId) throws RemoteException
     {
         return getPort().shoppingCartCustomerSet(getSessionId(), quoteId, fromMap(ShoppingCartCustomerEntity.class, customer), storeId);
     }
 
     @Override
     public boolean setShoppingCartCustomerAddresses(int quoteId, List<Map<String, Object>> addresses, String storeId) throws RemoteException
     {
         return getPort().shoppingCartCustomerAddresses(getSessionId(), quoteId, fromMap(ShoppingCartCustomerAddressEntity.class, addresses), storeId);
     }
 
     @Override
     public boolean setShoppingCartShippingMethod(int quoteId, String method, String storeId) throws RemoteException
     {
         return getPort().shoppingCartShippingMethod(getSessionId(), quoteId, method, storeId);
     }
 
     @Override
     public Object[] listShoppingCartShippingMethods(int quoteId, String storeId) throws RemoteException
     {
         return getPort().shoppingCartShippingList(getSessionId(), quoteId, storeId);
     }
 
     @Override
     public boolean setShoppingCartPaymentMethod(int quoteId, Map<String, Object> method, String storeId) throws RemoteException
     {
         return getPort().shoppingCartPaymentMethod(getSessionId(), quoteId, fromMap(ShoppingCartPaymentMethodEntity.class, method), storeId);
     }
 
     @Override
     public Object listShoppingCartPaymentMethods(int quoteId, String storeId) throws RemoteException
     {
         return getPort().shoppingCartPaymentList(getSessionId(), quoteId, storeId);
     }
 
     @Override
     public boolean addShoppingCartCoupon(int quoteId, String couponCode, String storeId) throws RemoteException
     {
         return getPort().shoppingCartCouponAdd(getSessionId(), quoteId, couponCode, storeId);
     }
 
     @Override
     public boolean removeShoppingCartCoupon(int quoteId, String storeId) throws RemoteException
     {
         return getPort().shoppingCartCouponRemove(getSessionId(), quoteId, storeId);
     }
 }
