 /*
  * Copyright 2012 Oracle and/or its affiliates.
  * All rights reserved.  You may not modify, use,
  * reproduce, or distribute this software except in
  * compliance with  the terms of the License at:
  * http://developers.sun.com/license/berkeley_license.html
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package order.ejb;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
 import javax.ejb.EJB;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 
 /**
  * 
  * @author ian
  */
@DependsOn({ "RequestBean" })
 @Singleton
 @Startup
 public class ConfigBean {
 	@EJB
 	private RequestBean request;
 
 	@PostConstruct
 	public void createData() {
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 1");
 
 		request.createPart("1234-5678-01", 1, "ABC PART", new java.util.Date(),
 				"PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 1a");
 
 		request.createPart("9876-4321-02", 2, "DEF PART", new java.util.Date(),
 				"PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
 		request.createPart("5456-6789-03", 3, "GHI PART", new java.util.Date(),
 				"PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
 		request.createPart("ABCD-XYZW-FF", 5, "XYZ PART", new java.util.Date(),
 				"PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
 		request.createPart("SDFG-ERTY-BN", 7, "BOM PART", new java.util.Date(),
 				"PARTQWERTYUIOPASXDCFVGBHNJMKL", null);
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 2");
 
 		request.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "1234-5678-01", 1);
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 2a");
 
 		request.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "9876-4321-02", 2);
 		request.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "5456-6789-03", 3);
 		request.addPartToBillOfMaterial("SDFG-ERTY-BN", 7, "ABCD-XYZW-FF", 5);
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 3");
 
 		request.createVendor(100, "WidgetCorp",
 				"111 Main St., Anytown, KY 99999", "Mr. Jones", "888-777-9999");
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 3a");
 
 		request.createVendor(200, "Gadget, Inc.",
 				"123 State St., Sometown, MI 88888", "Mrs. Smith",
 				"866-345-6789");
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 4");
 
 		request.createVendorPart("1234-5678-01", 1, "PART1", 100.00, 100);
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 4a");
 
 		request.createVendorPart("9876-4321-02", 2, "PART2", 10.44, 200);
 		request.createVendorPart("5456-6789-03", 3, "PART3", 76.23, 200);
 		request.createVendorPart("ABCD-XYZW-FF", 5, "PART4", 55.19, 100);
 		request.createVendorPart("SDFG-ERTY-BN", 7, "PART5", 345.87, 100);
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 5");
 
 		Integer orderId = new Integer(1111);
 		request.createOrder(orderId, 'N', 10,
 				"333 New Court, New City, CA 90000");
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 5a");
 
 		request.addLineItem(orderId, "1234-5678-01", 1, 3);
 		request.addLineItem(orderId, "9876-4321-02", 2, 5);
 		request.addLineItem(orderId, "ABCD-XYZW-FF", 5, 7);
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 6");
 
 		orderId = new Integer(4312);
 		request.createOrder(orderId, 'N', 0,
 				"333 New Court, New City, CA 90000");
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 6a");
 
 		request.addLineItem(orderId, "SDFG-ERTY-BN", 7, 1);
 		request.addLineItem(orderId, "ABCD-XYZW-FF", 5, 3);
 		request.addLineItem(orderId, "1234-5678-01", 1, 15);
 
 		System.out.println("[ConfigBean] TEST LOG OUTPUT 7");
 
 	}
 
 	@PreDestroy
 	public void deleteData() {
 	}
 }
