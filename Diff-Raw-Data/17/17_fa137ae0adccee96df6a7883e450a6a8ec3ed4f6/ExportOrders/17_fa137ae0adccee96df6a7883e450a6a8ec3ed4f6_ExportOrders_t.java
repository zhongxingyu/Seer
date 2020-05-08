 package com.order.src;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.SocketException;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.xmlbeans.XmlOptions;
 
 import noNamespace.OrdersDocument;
 import noNamespace.OrdersDocument.Orders;
 import noNamespace.OrdersDocument.Orders.Order;
 import noNamespace.OrdersDocument.Orders.Order.Details;
 import noNamespace.OrdersDocument.Orders.Order.Details.Line;
 import noNamespace.OrdersDocument.Orders.Order.Header.Shipto;
 import noNamespace.OrdersDocument.Orders.Order.Header.Soldto;
 
 import com.order.src.dao.ExportDAO;
 import com.order.src.objects.Header;
 
 public class ExportOrders {
 
 	private static String database = ""; //$NON-NLS-1$
 	private static String database2 = ""; //$NON-NLS-1$
 	
 	private static final String I2I = "I2I";
 	private static final String TOR = "TOR";
 	private static final String S = "*#*#";
 
 	private boolean isTest = true;
 
 	/**
 	 * @param args
 	 */
	@SuppressWarnings("static-access")
 	public static void main(String[] args) {
 		ExportOrders exportOrders = new ExportOrders();
 		database = args[0];
 		database = args[1];
 		
 		exportOrders.database = args[0];
 		exportOrders.database2 = args[1];
 		
 		if (args[2].equalsIgnoreCase("false")) { //$NON-NLS-1$
 			exportOrders.isTest = Boolean.FALSE;
 		} else {
 			exportOrders.isTest = Boolean.TRUE;
 		}
 
 		exportOrders.initialize();
 	}
 	
 	private void initialize(){
 		Vector<Header> headers = getHeaders();
 		createXML(headers);
 	}
 
 	private void createXML(Vector<Header> headers) {
 		OrdersDocument ordsDoc = null;
 
 		ordsDoc = OrdersDocument.Factory.newInstance();
 
 		Orders orders = ordsDoc.addNewOrders();
 
 		Enumeration<Header> e = headers.elements();
 		while (e.hasMoreElements()) {
 			Header header = e.nextElement();
 			
 			Order order = orders.addNewOrder();
 
 			noNamespace.OrdersDocument.Orders.Order.Header xHeader = order.addNewHeader();
 			if (!header.getNo().equalsIgnoreCase("")) { //$NON-NLS-1$
 				xHeader.setNumber(header.getNo());
 			}
 			if (!header.getNumber().equalsIgnoreCase("")) { //$NON-NLS-1$
 				xHeader.setRefNo(header.getNumber());
 			}
 			if (!header.getPoNo().equalsIgnoreCase("")) { //$NON-NLS-1$
 				xHeader.setPoNo(header.getPoNo());
 			}
 			if (!header.getShipOn().equalsIgnoreCase("")) { //$NON-NLS-1$
 				// no shipon data
 			}
 			if (!header.getComment().equalsIgnoreCase("")) { //$NON-NLS-1$
 				xHeader.setComment(header.getComment());
 			}
 			if (!header.getContact().equalsIgnoreCase("")) { //$NON-NLS-1$
 				xHeader.setContact(header.getContact());
 			}
 			if (!header.getShipVia().equalsIgnoreCase("")) { //$NON-NLS-1$
 				xHeader.setShipVia(header.getShipVia());
 			}
 			if (!header.getService().equalsIgnoreCase("")) { //$NON-NLS-1$
 				xHeader.setService(header.getService());
 			}
 			if (!header.getInvoice().equalsIgnoreCase("")) { //$NON-NLS-1$
 				xHeader.setInvoice(header.getInvoice());
 			}
 
 			com.order.src.objects.SoldTo headerSoldTo = getSoldTo(header.getNo());
 			// SoldTo Element
 			if (headerSoldTo != null) {
 				Soldto soldTo = xHeader.addNewSoldto();
 				if (!headerSoldTo.getAddress1().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setAddress1(headerSoldTo.getAddress1());
 				}
 				if (!headerSoldTo.getAddress2().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setAddress2(headerSoldTo.getAddress2());
 				}
 				if (!headerSoldTo.getCity().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setCity(headerSoldTo.getCity());
 				}
 				if (!headerSoldTo.getCode().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setCode(header.getNumber().trim() + "_" + headerSoldTo.getCode()); //$NON-NLS-1$
 				}
 				if (!headerSoldTo.getContact().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setContact(headerSoldTo.getContact());
 				}
 				if (!headerSoldTo.getCountry().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setCountry(headerSoldTo.getCountry());
 				}
 				if (!headerSoldTo.getEmail().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setEmail(headerSoldTo.getEmail());
 				}
 				if (!headerSoldTo.getFax().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setFax(headerSoldTo.getFax());
 				}
 				if (!headerSoldTo.getName().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setName(headerSoldTo.getName());
 				}
 				if (!headerSoldTo.getPhone().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setPhone(headerSoldTo.getPhone());
 				}
 				if (!headerSoldTo.getPostal().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setPostal(headerSoldTo.getPostal());
 				}
 				if (!headerSoldTo.getProvince().equalsIgnoreCase("")) { //$NON-NLS-1$
 					soldTo.setProvince(headerSoldTo.getProvince());
 				} else {
 					soldTo.setProvince("XX"); //$NON-NLS-1$
 				}
 			}
 			
 			com.order.src.objects.ShipTo headerShipTo = getShipTo(header.getNo());
 			// SoldTo Element
 			if (headerShipTo != null) {
 				Shipto shipTo = xHeader.addNewShipto();
 				if (!headerShipTo.getAddress1().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setAddress1(headerShipTo.getAddress1());
 				}
 				if (!headerShipTo.getAddress2().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setAddress2(headerShipTo.getAddress2());
 				}
 				if (!headerShipTo.getCity().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setCity(headerShipTo.getCity());
 				}
 				if (!headerShipTo.getCode().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setCode(header.getNumber().trim() + "_" + headerShipTo.getCode()); //$NON-NLS-1$
 				}
 				if (!headerShipTo.getContact().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setContact(headerShipTo.getContact());
 				}
 				if (!headerShipTo.getCountry().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setCountry(headerShipTo.getCountry());
 				}
 				if (!headerShipTo.getEmail().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setEmail(headerShipTo.getEmail());
 				}
 				if (!headerShipTo.getFax().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setFax(headerShipTo.getFax());
 				}
 				if (!headerShipTo.getName().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setName(headerShipTo.getName());
 				}
 				if (!headerShipTo.getPhone().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setPhone(headerShipTo.getPhone());
 				}
 				if (!headerShipTo.getPostal().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setPostal(headerShipTo.getPostal());
 				}
 				if (!headerShipTo.getProvince().equalsIgnoreCase("")) { //$NON-NLS-1$
 					shipTo.setProvince(headerShipTo.getProvince());
 				} else {
 					shipTo.setProvince("XX"); //$NON-NLS-1$
 				}
 			}
 
 			// Add Details
 			addDetails(header.getNo(), order);
 
 		}
 
 		OrdersDocument ordsDoc_TOR = OrdersDocument.Factory.newInstance();
 		Orders orders_TOR = ordsDoc_TOR.addNewOrders();
 		
 		OrdersDocument ordsDoc_I2I = OrdersDocument.Factory.newInstance();
 		Orders orders_I2I = ordsDoc_I2I.addNewOrders();
 		
 		Order[] ordDoc_Order = ordsDoc.getOrders().getOrderArray();
 		for (int i = 0; i < ordDoc_Order.length; i++)
 		{
 			String item = ordDoc_Order[i].getDetails().getLineArray(0).getItem();
 			System.out.println("item: " + item);
 			if ( item.contains(S+TOR))
 			{
 				//add to TOR orders_TOR
 				Order order = orders_TOR.addNewOrder();
 				order.set(ordDoc_Order[i]);
 				Line[] order_lines = order.getDetails().getLineArray();
 				for ( int j = 0; j < order_lines.length; j++)
 				{
 					String order_item = order_lines[j].getItem();
 					System.out.println("  order_item_tor:before: " + order_item);
 					order_item = order_item.substring(0, order_item.indexOf(S));
 					System.out.println("    order_item_tor:after: " + order_item);
 					order_lines[j].setItem(order_item.trim());
 				}
 			}
 			else
 			{
 				Order order = orders_I2I.addNewOrder();
 				order.set(ordDoc_Order[i]);
 				Line[] order_lines = order.getDetails().getLineArray();
 				for ( int j = 0; j < order_lines.length; j++)
 				{
 					String order_item = order_lines[j].getItem();
 					System.out.println("  order_item_i2i:before: " + order_item);
 					order_item = order_item.substring(0, order_item.indexOf(S));
 					System.out.println("    order_item_i2i:after: " + order_item);
 					order_lines[j].setItem(order_item.trim());
 				}
 			}
 		}
 		System.out.println("orders tor size " + ordsDoc_TOR.getOrders().getOrderArray().length);
 		System.out.println("orders i2i size " + ordsDoc_I2I.getOrders().getOrderArray().length);
 
 		
 		String filename1 = ""; //$NON-NLS-1$
 		String filename2 = "";
 		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmm"); //$NON-NLS-1$
 		java.util.Date date = new java.util.Date();
 		filename1 = "ORD"+ TOR + "_" + dateFormat.format(date) + "_orders.xml"; //$NON-NLS-1$ //$NON-NLS-2$
 		filename2 = "ORD"+ I2I + "_" + dateFormat.format(date) + "_orders.xml"; //$NON-NLS-1$ //$NON-NLS-2$
 
 		// System.out.println(ordsDoc.toString());
 		System.out.println("XML Generated is Valid: " + ordsDoc_TOR.validate()); //$NON-NLS-1$
 		System.out.println("XML Generated is Valid: " + ordsDoc_I2I.validate()); //$NON-NLS-1$
 		System.out.println("Writing to File: " + Messages.getString("directory") + filename1); //$NON-NLS-1$
 		System.out.println("Writing to File: " + Messages.getString("directory") + filename2); //$NON-NLS-1$
 		XmlOptions option = new XmlOptions();
 		option.setSavePrettyPrint();
 		try {
 			ordsDoc_TOR.save(new File(Messages.getString("directory") + filename1), option); //$NON-NLS-1$
 			ordsDoc_I2I.save(new File(Messages.getString("directory") + filename2), option); //$NON-NLS-1$
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		ftpFile(ordsDoc_TOR, filename1, TOR);
 		ftpFile(ordsDoc_I2I, filename2, I2I);
 		
 		System.out.println("The end"); //$NON-NLS-1$
 	}
 
 	private void ftpFile(OrdersDocument ordsDoc, String filename, String warehouse) {
 //		if (ordsDoc.validate()) {   
 			// FTP
 			try {
 				FTPClient ftp = new FTPClient();
 				if (isTest) {
 					ftp.connect(Messages.getString(warehouse+"_testftp")); //$NON-NLS-1$
 					ftp.login(Messages.getString(warehouse+"_testusername"), 
 							Messages.getString(warehouse+"_testpassword")); //$NON-NLS-1$ //$NON-NLS-2$
 				} else {
 					ftp.connect(Messages.getString(warehouse+"_ftp")); //$NON-NLS-1$
 					ftp.login(Messages.getString(warehouse+"_username"), 
 							Messages.getString(warehouse+"_password")); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 
 				System.out.println("Reply Code: " + ftp.getReplyCode()); //$NON-NLS-1$
 				if ( "ACTIVE".equalsIgnoreCase(Messages.getString(warehouse+"_mode")) ){
 					ftp.enterLocalActiveMode();
 				}else{
 					ftp.enterLocalPassiveMode();
 				}
 				
 				ftp.changeWorkingDirectory("ship"); //$NON-NLS-1$
 
 				boolean success = ftp.storeFile(filename, new FileInputStream(
 						Messages.getString("directory") + filename)); //$NON-NLS-1$
 				System.out.println("FTP Success? " + success); //$NON-NLS-1$
 
 				ftp.disconnect();
 				System.out.println("Complete"); //$NON-NLS-1$
 
 			} catch (SocketException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 //		}
 	}
 
 	private void addDetails(String no, Order order) {
 		Details details = order.addNewDetails();
 
 		// query for data and put generate lines in a loop
 		Vector<com.order.src.objects.Line> lines = getDetails(no);
 		Enumeration<com.order.src.objects.Line> e = lines.elements();
 
 		while (e.hasMoreElements()) {
 			com.order.src.objects.Line dLine = e.nextElement();
 			Line line = details.addNewLine();
 
 			if (!dLine.getNo().equalsIgnoreCase("")) { //$NON-NLS-1$
 				line.setNo((short) Float.valueOf(dLine.getNo()).intValue());
 			}
 			if (!dLine.getItem().equalsIgnoreCase("")) { //$NON-NLS-1$
 				if (dLine.getWarehouse().equalsIgnoreCase(TOR)){
 					line.setItem(dLine.getItem()+S+TOR);	
 				}
 				else 
 				{
 					line.setItem(dLine.getItem()+S+I2I);
 				}
 			}
 			if (!dLine.getSerial().equalsIgnoreCase("")) { //$NON-NLS-1$
 				line.setSerial(dLine.getSerial());
 				
 			}
 			if (!dLine.getDescription().equalsIgnoreCase("")) { //$NON-NLS-1$
 				line.setDescription(dLine.getDescription());
 			}
 			if (dLine.getQty() >= 0) {
 				line.setQty(dLine.getQty()+""); //$NON-NLS-1$
 			}
 			if (dLine.getComment() != null
 					&& !dLine.getComment().equalsIgnoreCase("")) { //$NON-NLS-1$
 				line.setComment(dLine.getComment());
 			}
 
 		}
 
 	}
 
 	private com.order.src.objects.SoldTo getSoldTo(String number){
 		ExportDAO dao = new ExportDAO(database, database2);
 		com.order.src.objects.SoldTo soldTo;
 
 		try {
 			soldTo = dao.getSoldToInformation(number);
 			return soldTo;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;		
 		
 	}
 	
 	private com.order.src.objects.ShipTo getShipTo(String number){
 		ExportDAO dao = new ExportDAO(database, database2);
 		com.order.src.objects.ShipTo shipTo;
 
 		try {
 			shipTo = dao.getShipToInformation(number);
 			return shipTo;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;		
 		
 	}
 	
 	private Vector<Header> getHeaders() {
 		ExportDAO dao = new ExportDAO(database, database2);
 		Vector<Header> headers;
 
 		try {
 			headers = dao.getHeaderInformation();
 			return headers;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	private Vector<com.order.src.objects.Line> getDetails(String number) {
 		ExportDAO dao = new ExportDAO(database, database2);
 		Vector<com.order.src.objects.Line> lines;
 
 		try {
 			lines = dao.getDetailLineInformation(number);
 			return lines;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 }
