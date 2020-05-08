 /*
  * Copyright 2012 Oracle and/or its affiliates.
  * All rights reserved.  You may not modify, use,
  * reproduce, or distribute this software except in
  * compliance with  the terms of the License at:
  * http://developers.sun.com/license/berkeley_license.html
  */
 
 package order.ejb;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.ejb.EJBException;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
 import javax.ejb.Stateful;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import order.entity.LineItem;
 import order.entity.Order;
 import order.entity.Part;
 import order.entity.PartKey;
 import order.entity.Vendor;
 import order.entity.VendorPart;
 
@Singleton
@Lock(LockType.READ)
 public class RequestBean {
 	private static Logger logger = Logger.getLogger("order.ejb.RequestBean");
 	@PersistenceContext
 	private EntityManager em;
 
 	public void createPart(String partNumber, int revision, String description,
 			java.util.Date revisionDate, String specification,
 			Serializable drawing) {
 
 		System.out.println("[RequestBean] OUT 1");
 
 		try {
 			Part part = new Part(partNumber, revision, description,
 					revisionDate, specification, drawing);
 
 			System.out.println("[RequestBean] OUT 2");
 
 			logger.info("Created part " + partNumber + "-" + revision);
 
 			System.out.println("[RequestBean] OUT 3");
 
 			em.persist(part);
 
 			System.out.println("[RequestBean] OUT 4");
 
 			logger.info("Persisted part " + partNumber + "-" + revision);
 
 			System.out.println("[RequestBean] OUT 5");
 
 		} catch (Exception ex) {
 			throw new EJBException(ex.getMessage());
 		}
 	}
 
 	public List<Part> getAllParts() {
 		List<Part> parts = (List<Part>) em.createNamedQuery("findAllParts")
 				.getResultList();
 
 		return parts;
 	}
 
 	public void addPartToBillOfMaterial(String bomPartNumber, int bomRevision,
 			String partNumber, int revision) {
 		logger.info("BOM part number: " + bomPartNumber);
 		logger.info("BOM revision: " + bomRevision);
 		logger.info("Part number: " + partNumber);
 		logger.info("Part revision: " + revision);
 
 		try {
 			PartKey bomKey = new PartKey();
 			bomKey.setPartNumber(bomPartNumber);
 			bomKey.setRevision(bomRevision);
 
 			Part bom = em.find(Part.class, bomKey);
 			logger.info("BOM Part found: " + bom.getPartNumber());
 
 			PartKey partKey = new PartKey();
 			partKey.setPartNumber(partNumber);
 			partKey.setRevision(revision);
 
 			Part part = em.find(Part.class, partKey);
 			logger.info("Part found: " + part.getPartNumber());
 			bom.getParts().add(part);
 			part.setBomPart(bom);
 		} catch (EJBException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void createVendor(int vendorId, String name, String address,
 			String contact, String phone) {
 		try {
 			Vendor vendor = new Vendor(vendorId, name, address, contact, phone);
 			em.persist(vendor);
 		} catch (Exception e) {
 			throw new EJBException(e);
 		}
 	}
 
 	public void createVendorPart(String partNumber, int revision,
 			String description, double price, int vendorId) {
 		try {
 			PartKey pkey = new PartKey();
 			pkey.setPartNumber(partNumber);
 			pkey.setRevision(revision);
 
 			Part part = em.find(Part.class, pkey);
 
 			VendorPart vendorPart = new VendorPart(description, price, part);
 			em.persist(vendorPart);
 
 			Vendor vendor = em.find(Vendor.class, vendorId);
 			vendor.addVendorPart(vendorPart);
 			vendorPart.setVendor(vendor);
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 
 	public void createOrder(Integer orderId, char status, int discount,
 			String shipmentInfo) {
 		try {
 			Order order = new Order(orderId, status, discount, shipmentInfo);
 			em.persist(order);
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 
 	public List<Order> getOrders() {
 		try {
 			return (List<Order>) em.createNamedQuery("findAllOrders")
 					.getResultList();
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 
 	public void addLineItem(Integer orderId, String partNumber, int revision,
 			int quantity) {
 		try {
 			Order order = em.find(Order.class, orderId);
 			logger.info("Found order ID " + orderId);
 
 			PartKey pkey = new PartKey();
 			pkey.setPartNumber(partNumber);
 			pkey.setRevision(revision);
 
 			Part part = em.find(Part.class, pkey);
 
 			LineItem lineItem = new LineItem(order, quantity,
 					part.getVendorPart());
 			order.addLineItem(lineItem);
 		} catch (Exception e) {
 			logger.warning("Couldn't add " + partNumber + " to order ID "
 					+ orderId + ".");
 			throw new EJBException(e.getMessage());
 		}
 	}
 
 	public double getBillOfMaterialPrice(String bomPartNumber, int bomRevision,
 			String partNumber, int revision) {
 		double price = 0.0;
 
 		try {
 			PartKey bomkey = new PartKey();
 			bomkey.setPartNumber(bomPartNumber);
 			bomkey.setRevision(bomRevision);
 
 			Part bom = em.find(Part.class, bomkey);
 			Collection<Part> parts = bom.getParts();
 
 			for (Iterator iterator = parts.iterator(); iterator.hasNext();) {
 				Part part = (Part) iterator.next();
 				VendorPart vendorPart = part.getVendorPart();
 				price += vendorPart.getPrice();
 			}
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 
 		return price;
 	}
 
 	public double getOrderPrice(Integer orderId) {
 		double price = 0.0;
 
 		try {
 			Order order = em.find(Order.class, orderId);
 			price = order.calculateAmmount();
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 
 		return price;
 	}
 
 	public void adjustOrderDiscount(int adjustment) {
 		try {
 			List orders = em.createNamedQuery("findAllOrders").getResultList();
 
 			for (Iterator it = orders.iterator(); it.hasNext();) {
 				Order order = (Order) it.next();
 				int newDiscount = order.getDiscount() + adjustment;
 				order.setDiscount((newDiscount > 0) ? newDiscount : 0);
 			}
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 
 	public Double getAvgPrice() {
 		try {
 			return (Double) em.createNamedQuery("findAverageVendorPartPrice")
 					.getSingleResult();
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 
 	public Double getTotalPricePerVendor(int vendorId) {
 		try {
 			return (Double) em
 					.createNamedQuery("findTotalVendorPartPricePerVendor")
 					.setParameter("id", vendorId).getSingleResult();
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 
 	public List<String> locateVendorsByPartialName(String name) {
 		List<String> names = new ArrayList<String>();
 
 		try {
 			List vendors = em.createNamedQuery("findVendorsByPartialName")
 					.setParameter("name", name).getResultList();
 
 			for (Iterator iterator = vendors.iterator(); iterator.hasNext();) {
 				Vendor vendor = (Vendor) iterator.next();
 				names.add(vendor.getName());
 			}
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 
 		return names;
 	}
 
 	public int countAllItems() {
 		int count = 0;
 
 		try {
 			count = em.createNamedQuery("findAllLineItems").getResultList()
 					.size();
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 
 		return count;
 	}
 
 	public List<LineItem> getLineItems(int orderId) {
 		try {
 			return em.createNamedQuery("findLineItemsByOrderId")
 					.setParameter("orderId", orderId).getResultList();
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 
 	public void removeOrder(Integer orderId) {
 		try {
 			Order order = em.find(Order.class, orderId);
 			em.remove(order);
 		} catch (Exception e) {
 			throw new EJBException(e.getMessage());
 		}
 	}
 
 	public String reportVendorsByOrder(Integer orderId) {
 		StringBuffer report = new StringBuffer();
 
 		try {
 			List vendors = em.createNamedQuery("findVendorByOrder")
 					.setParameter("id", orderId).getResultList();
 
 			for (Iterator iterator = vendors.iterator(); iterator.hasNext();) {
 				Vendor vendor = (Vendor) iterator.next();
 				report.append(vendor.getVendorId()).append(' ')
 						.append(vendor.getName()).append(' ')
 						.append(vendor.getContact()).append('\n');
 			}
 		} catch (Exception e) {
 			throw new EJBException(e);
 		}
 
 		return report.toString();
 	}
 }
