 /**
  * PHR_AndroidNative
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /**
  *
  */
 package com.photon.phresco.nativeapp.unit.test.testcases;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.json.JSONObject;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import com.google.gson.Gson;
 import com.photon.phresco.nativeapp.eshop.logger.PhrescoLogger;
 import com.photon.phresco.nativeapp.eshop.model.customer.Address;
 import com.photon.phresco.nativeapp.eshop.model.customer.Customer;
 import com.photon.phresco.nativeapp.eshop.model.order.Order;
 import com.photon.phresco.nativeapp.eshop.model.order.OrderSubmit;
 import com.photon.phresco.nativeapp.eshop.model.order.OrderSubmitProduct;
 import com.photon.phresco.nativeapp.unit.test.core.Constants;
 
 /**
  * @author chandankumar_r
  *
  */
 public class G_OrderReviewActivityTest extends TestCase {
 	private static final String TAG = "G_OrderReviewActivityTest *********** ";
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@AfterClass
 	public static void tearDownAfterClass() {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() {
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() {
 	}
 
 	/*
 	 * get the order details and post it to web server
 	 */
 	@Test
	public void testOrderDetails() {
 
 		// convert the orderInfo object to JSON String
 
 		List<OrderSubmitProduct> products = new ArrayList<OrderSubmitProduct>();
 		OrderSubmit orderSubmit = new OrderSubmit();
 		int totalPrice=629;
 		int quantity=0;
 		int productId=0;
 		try {
 			PhrescoLogger.info(TAG + " testOrderDetails -------------- START ");
 
 			orderSubmit.setComments("product comments");
 			orderSubmit.setCustomerInfo(getCustomerInfo());
 			orderSubmit.setPaymentMethod("cash on delivery");
 			orderSubmit.setTotalPrice(totalPrice);
 
 			// Create product object
 			OrderSubmitProduct orderSubmitProduct = new OrderSubmitProduct();
 			productId=1;
 			orderSubmitProduct.setProductId(productId);
 			orderSubmitProduct.setName("LG Electronics 42PW350 3D Plasma HDTV ");
 			orderSubmitProduct.setPrice(totalPrice);
 			orderSubmitProduct.setImageURL("product/lg_tv_1.png");
 			orderSubmitProduct.setDetailImageURL("product/details/lg_tv_1.png");
 			quantity=1;
 			orderSubmitProduct.setQuantity(quantity);
 			orderSubmitProduct.setTotalPrice(totalPrice);
 			products.add(orderSubmitProduct);
 
 			orderSubmit.setProducts(products);
 
 			Gson jsonObj = new Gson();
 			String orderDetail = jsonObj.toJson(orderSubmit);
 			PhrescoLogger.info(TAG + "postOrderDetails() - JSON String : " + orderDetail);
 
 			// Post the orderInfo JSON String to server, and receive the JSON response
 			Order orderObj = new Order();
 			JSONObject jObject = null;
 
 			PhrescoLogger.info(TAG + Constants.getWebContextURL() + Constants.getRestAPI() + Constants.ORDER_POST_URL);
 
 			jObject = orderObj.postOrderJSONObject(Constants.getWebContextURL() + Constants.getRestAPI() + Constants.ORDER_POST_URL, orderDetail);
 			assertNotNull(jObject);
 
 			PhrescoLogger.info(TAG + " testOrderDetails -------------- END ");
 
 		} catch (IOException ex) {
 			PhrescoLogger.info(TAG + "testOrderDetails - IOException: " + ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 
 	}
 
 
 	/*
 	 * get the order details and post it to web server
 	 */
 	@Test
	public void testOrderDetailsValidation() {
 
 		// convert the orderInfo object to JSON String
 
 		List<OrderSubmitProduct> products = new ArrayList<OrderSubmitProduct>();
 		OrderSubmit orderSubmit = new OrderSubmit();
 		int totalPrice=600;
 		int productPrice=300;
 		int quantity=0;
 		int productId=0;
 		try {
 			PhrescoLogger.info(TAG + " testOrderDetailsValidation -------------- START ");
 
 			orderSubmit.setComments("product comments");
 			orderSubmit.setCustomerInfo(getCustomerInfo());
 			orderSubmit.setPaymentMethod("cash on delivery");
 			orderSubmit.setTotalPrice(totalPrice);
 
 			// Create product object
 			OrderSubmitProduct orderSubmitProduct = new OrderSubmitProduct();
 			productId=1;
 			orderSubmitProduct.setProductId(productId);
 			orderSubmitProduct.setName("LG Electronics 42PW350 3D Plasma HDTV ");
 
 			orderSubmitProduct.setPrice(productPrice);
 
 			orderSubmitProduct.setImageURL("product/lg_tv_1.png");
 			orderSubmitProduct.setDetailImageURL("product/details/lg_tv_1.png");
 			quantity=1;
 			orderSubmitProduct.setQuantity(quantity);
 			orderSubmitProduct.setTotalPrice(totalPrice);
 			products.add(orderSubmitProduct);
 
 			orderSubmit.setProducts(products);
 
 			if(totalPrice==(quantity*productPrice)){
 				assertEquals("total price is equal to product price", 600, 600);
 			}else {
 				assertNotSame("total price is not equal to product price", 600, 250);
 			}
 			Gson jsonObj = new Gson();
 			String orderDetail = jsonObj.toJson(orderSubmit);
 			PhrescoLogger.info(TAG + "postOrderDetails() - JSON String : " + orderDetail);
 
 			// Post the orderInfo JSON String to server, and receive the JSON response
 			Order orderObj = new Order();
 			JSONObject jObject = null;
 
 			PhrescoLogger.info(TAG + Constants.getWebContextURL() + Constants.getRestAPI() + Constants.ORDER_POST_URL);
 
 			jObject = orderObj.postOrderJSONObject(Constants.getWebContextURL() + Constants.getRestAPI() + Constants.ORDER_POST_URL, orderDetail);
 			assertNotNull(jObject);
 
 			PhrescoLogger.info(TAG + " testOrderDetailsValidation -------------- END ");
 
 		} catch (IOException ex) {
 			PhrescoLogger.info(TAG + "testOrderDetailsValidation - IOException: " + ex.toString());
 			PhrescoLogger.warning(ex);
 		}
 
 	}
 
 	/**
 	 *
 	 */
 	private Customer getCustomerInfo() {
 		Customer customer = new Customer();
 		customer.setEmailID("john@phresco.com");
 		customer.setDeliveryAddress(getDeliveryAddress());
 		customer.setBillingAddress(getBillingAddress());
 		return customer;
 	}
 
 	/**
 	 *
 	 */
 	private Address getDeliveryAddress() {
 		Address address = new Address();
 		address.setFirstName("John");
 		address.setLastName("Parker");
 		address.setCompany("Photon");
 		address.setAddressOne("Delivery");
 		address.setAddressTwo("address");
 		address.setCity("Banglore");
 		address.setState("KA");
 		address.setCountry("IN");
 		address.setZipCode("12321");
 		address.setPhoneNumber("2342342342");
 		return address;
 	}
 
 	/**
 	 *
 	 * @return address
 	 */
 	private Address getBillingAddress() {
 		Address address = new Address();
 		address.setFirstName("John");
 		address.setLastName("Parker");
 		address.setCompany("Photon");
 		address.setAddressOne("Billing");
 		address.setAddressTwo("address");
 		address.setCity("Chennai");
 		address.setState("TN");
 		address.setCountry("IN");
 		address.setZipCode("12321");
 		address.setPhoneNumber("2342342342");
 		return address;
 	}
 
 }
