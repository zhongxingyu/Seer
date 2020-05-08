 package serversrc.resImpl;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.rmi.RemoteException;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import serversrc.resInterface.*;
 
 @SuppressWarnings("rawtypes")
 public class TCPMiddleware implements Runnable {
 	// TODO: Put the middleware, customer, flight, hotel, cars, and customer on
 	// different ports
 
 	Socket flightSocket;
 	Socket carsSocket;
 	Socket hotelSocket;
 	Socket customerSocket;
 	Socket clientSocket;
 
 	ObjectInputStream clientIn;
 	ObjectOutputStream clientOut;
 
 	ObjectInputStream flightIn;
 	ObjectOutputStream flightOut;
 
 	ObjectInputStream carsIn;
 	ObjectOutputStream carsOut;
 
 	ObjectInputStream hotelIn;
 	ObjectOutputStream hotelOut;
 
 	ObjectInputStream customersIn;
 	ObjectOutputStream customersOut;
 
 	String server;
 	int port;
 
 	private TCPMiddleware(Socket clientSocket, String server, int port) {
 		this.server = server;
 		this.port = port;
 		this.clientSocket = clientSocket;
 	}
 
 	public static void main(String args[]) {
 		// Figure out where server is running
 		String server = "localhost";
 		int port = 1099;
 
 		if (args.length == 5) {
 			server = server + ":" + args[4];
 			port = Integer.parseInt(args[4]);
 		} else {
 			System.err.println("Wrong usage");
 			System.out
 					.println("Usage: java ResImpl.Middleware rmCar rmFlight rmHotel [port]");
 			System.exit(1);
 		}
 		try {
 			Socket clientSocket;
 			ServerSocket connection = new ServerSocket(port);
 
 			while (true) {
 				clientSocket = connection.accept();
 
 				TCPMiddleware obj = new TCPMiddleware(clientSocket, server,
 						port);
 				Thread t = new Thread(obj);
 				t.start();
 
 			}
 		} catch (Exception e) {
 			System.err.println("Connection issue");
 		}
 	}
 
 	@Override
 	public void run() {
 		try {
 
 			flightSocket = new Socket(server, port);
 			carsSocket = new Socket(server, port);
 			hotelSocket = new Socket(server, port);
 			customerSocket = new Socket(server, port);
 
 			clientIn = new ObjectInputStream(clientSocket.getInputStream());
 			clientOut = new ObjectOutputStream(clientSocket.getOutputStream());
 
 			flightIn = new ObjectInputStream(flightSocket.getInputStream());
 			flightOut = new ObjectOutputStream(flightSocket.getOutputStream());
 
 			carsIn = new ObjectInputStream(carsSocket.getInputStream());
 			carsOut = new ObjectOutputStream(carsSocket.getOutputStream());
 
 			hotelIn = new ObjectInputStream(hotelSocket.getInputStream());
 			hotelOut = new ObjectOutputStream(hotelSocket.getOutputStream());
 
 			customersIn = new ObjectInputStream(customerSocket.getInputStream());
 			customersOut = new ObjectOutputStream(
 					customerSocket.getOutputStream());
 
 			Vector methodInvocation;
 			while ((methodInvocation = (Vector) clientIn.readObject()) != null) {
 				methodSelector(methodInvocation);
 			}
 		}
 
 		catch (Exception e) {
 			Trace.error("Server connection failed");
 
 		}
 	}
 
 	/**
 	 * parses a string into a method invocation
 	 * 
 	 * @param methodInvocation
 	 *            the string to be parsed
 	 * @throws RemoteException
 	 * @throws NumberFormatException
 	 */
 	@SuppressWarnings("unchecked")
 	private void methodSelector(Vector methodInvocation) throws Exception {
 		Vector args = methodInvocation;
 		String method = getString(args.elementAt(0));
 		if (method.contains("Flight")) {
 			// send method to flight manager and get response
 			try {
 				flightOut.writeObject(methodInvocation);
 				hotelOut.writeObject(methodInvocation);
 				if (method.contains("Add") || method.contains("Delete"))
 					clientOut.writeBoolean(flightIn.readBoolean());
 				else
 					clientOut.writeInt(flightIn.readInt());
 			} catch (IOException e) {
 				Trace.error("IOException in method invocation: "
 						+ getString(method));
 				return;
 			}
 
 			// return flight manager's response return;
 		}
 		if (!method.contains("reserve")) {  
 			if (method.contains("Cars")) {
 				try {
 					carsOut.writeObject(methodInvocation);
 					if (method.contains("Add") || method.contains("Delete"))
 						clientOut.writeBoolean(carsIn.readBoolean());
 					else
 						clientOut.writeInt(carsIn.readInt());
 				} catch (IOException e) {
 					Trace.error("IOException in method invocation: "
 							+ getString(method));
 				}
 			}
 			if (method.contains("Rooms")) {
 				try {
 					hotelOut.writeObject(methodInvocation);
 					if (method.contains("Add") || method.contains("Delete"))
 						clientOut.writeBoolean(hotelIn.readBoolean());
 					else
 						clientOut.writeInt(hotelIn.readInt());
 				} catch (IOException e) {
 					Trace.error("IOException in method invocation: "
 							+ getString(method));
 				}
 			}
 			if (method.contains("Customer")) {
 
 				try {
 					customersOut.writeObject(methodInvocation);
 					if (method.equalsIgnoreCase("deleteCustomer")) {
 						RMHashtable reservationHT = (RMHashtable) customersIn
 								.readObject();
 						for (Enumeration e = reservationHT.keys(); e
 								.hasMoreElements();) {
 							String reservedkey = (String) (e.nextElement());
 							ReservedItem reserveditem = (ReservedItem) reservationHT
 									.get(reservedkey);
 							Vector unreserve = new Vector();
 							unreserve.add("unreserveItem");
 							unreserve.add(args.elementAt(1));
 							unreserve.add(reserveditem);
 							boolean success = false;
 							if (reserveditem.getrType() == ReservedItem.rType.FLIGHT) {
 								flightOut.writeObject(unreserve);
 								success = flightIn.readBoolean();
 							} else if (reserveditem.getrType() == ReservedItem.rType.CAR) {
 								carsOut.writeObject(unreserve);
 								success = flightIn.readBoolean();
 							} else if (reserveditem.getrType() == ReservedItem.rType.ROOM) {
 								hotelOut.writeObject(unreserve);
 								success = flightIn.readBoolean();
 							}
 							
 							clientOut.writeBoolean(success);
 							return;
 						}
 					}
 					else if (method.equalsIgnoreCase("newCustomer")){
 						if (args.size() == 3) 
 							clientOut.writeInt(clientIn.readInt());
 						else
 							clientOut.writeBoolean(clientIn.readBoolean());
 						return;
 					}
 					else if (method.equalsIgnoreCase("getCustomer")){
 						clientOut.writeObject(customersIn.readObject());
 						return;
 					}
 					else
 						clientOut.writeObject(customersIn.readObject());
 					
 				} catch (IOException e) {
 					Trace.error("IOException in method invocation: "
 							+ getString(method));
 				}
 			}
 		} else {
 			if (method.equalsIgnoreCase("reserveFlight")) {
 				clientOut.writeBoolean(reserveFlight(getInt(args.elementAt(1)),
 						getInt(args.elementAt(2)), getInt(args.elementAt(3))));
 			}
 			if (method.equalsIgnoreCase("reserveCar")) {
 				clientOut
 						.writeBoolean(reserveCar(getInt(args.elementAt(1)),
 								getInt(args.elementAt(2)),
 								getString(args.elementAt(3))));
 			}
 			if (method.equalsIgnoreCase("reserveRoom")) {
 				clientOut
 						.writeBoolean(reserveRoom(getInt(args.elementAt(1)),
 								getInt(args.elementAt(2)),
 								getString(args.elementAt(3))));
 			}
 		}
 	}
 
 	public boolean reserveFlight(int id, int customer, int flightNum)
 			throws IOException {
 
 		 ReservedItem item = reserveItem(id, customer, Flight.getKey(flightNum),
 				String.valueOf(flightNum), ReservedItem.rType.FLIGHT);
 		 return (item != null);
 	}
 
 	public boolean reserveCar(int id, int customer, String location)
 			throws IOException {
 
 		ReservedItem item = reserveItem(id, customer, Car.getKey(location), location,
 				ReservedItem.rType.CAR);
 		return (item != null);
 	}
 
 	public boolean reserveRoom(int id, int customer, String location)
 			throws IOException {
 
 		ReservedItem item = reserveItem(id, customer, Hotel.getKey(location), location,
 				ReservedItem.rType.ROOM);
 		return (item != null);
 	}
 
 	public boolean itinerary(int id, int customer, Vector flightNumbers,
 			String location, boolean car, boolean room) throws IOException {
 		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", "
 				+ flightNumbers + ", " + location + ", " + car + ", " + room
 				+ " ) called");
 		// Read customer object if it exists (and read lock it)
         Customer cust = null;
         Vector<Object> args = new Vector<Object>();
 		args.add("getCustomer");
 		args.add(id);
 		args.add(customer);
 		
 		customersOut.writeObject(args);
         try {
 			cust = (Customer) customersIn.readObject();
 		} catch (ClassNotFoundException e) {
 			Trace.error("Object returned was not a Customer ");
 			return false;
 		}
         if ( cust == null ) {
         	Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
 					", " + car + ", " + room + " ) -- Customer non existent." );
         	return false;
         }
         ReservedItem reservedCar = null;
         ReservedItem reservedRoom = null;
         if (car){
         	reservedCar = reserveItem(id, customer, Car.getKey(location) ,location, ReservedItem.rType.CAR);
         	if (reservedCar == null) {
         		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
     					", " + car + ", " + room + " ) -- Car could not have been reserved." );
         		return false;
         	}
         }
         if (room){
         	reservedRoom = reserveItem(id, customer, Hotel.getKey(location) ,location, ReservedItem.rType.ROOM);
         	if (reservedRoom == null) {
         		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
     					", " + car + ", " + room + " ) -- Room could not have been reserved." );
         		unreserveItem(id, customer, reservedCar, ReservedItem.rType.CAR);
         		return false;
         	}
         }
         Vector flightsDone = new Vector();
         for (Enumeration e = flightNumbers.elements(); e.hasMoreElements();) {
         	int flightnum = 0;
         	try {
         		flightnum = getInt(e.nextElement());
         	} catch(Exception ex) {
         		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightNumbers+ ", "+location+
     					", " + car + ", " + room + " ) -- Expected FlightNumber was not a valid integer. Exception "
     					+ ex + " cached");
         		unreserveItem(id, customer, reservedCar, ReservedItem.rType.CAR);
         		unreserveItem(id, customer, reservedRoom, ReservedItem.rType.ROOM);
         		for (Enumeration f = flightsDone.elements(); f.hasMoreElements();) {
         			unreserveItem(id, customer, (ReservedItem) f.nextElement(), ReservedItem.rType.ROOM);
         		}
         		return false;
         	}
         	ReservedItem reservedFlight = reserveItem(id, customer, Flight.getKey(flightnum), String.valueOf(flightnum), ReservedItem.rType.FLIGHT);
         	if (reservedFlight == null){
         		Trace.info("RM::itinerary( " + id + ", customer=" + customer + ", " +flightnum+ ", "+location+
     					", " + car + ", " + room + " ) -- flight could not have been reserved." );
         		unreserveItem(id, customer, reservedCar, ReservedItem.rType.CAR);
         		unreserveItem(id, customer, reservedRoom, ReservedItem.rType.ROOM);
         		for (Enumeration f = flightsDone.elements(); f.hasMoreElements();) {
         			unreserveItem(id, customer, (ReservedItem) f.nextElement(), ReservedItem.rType.ROOM);
         		}
         		return false;
         	}
         	flightsDone.add(reservedFlight);	
         }
         	
 		return true;
 	}
 	
 	/*
 	 * Call RMCust to obtain customer, if it exists. Verify if item exists and
 	 * is available. (Call RM*obj*) Reserve with RMCustomer Tell RM*obj* to
 	 * reduce the number of available
 	 */
 	protected ReservedItem reserveItem(int id, int customerID, String key,
 			String location, ReservedItem.rType rtype) throws IOException {
 		Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", "
 				+ key + ", " + location + " ) called");
 		Vector<Object> args = new Vector<Object>();
 		args.add("getCustomer");
 		args.add(id);
 		args.add(customerID);
 		args.add(key);
 		args.add(location);
 		args.add(rtype);
 		customersOut.writeObject(args);
 		Customer cust;
 		try {
 			cust = (Customer) customersIn.readObject();
 		} catch (ClassNotFoundException e) {
 			Trace.error("Object returned was not a Customer ");
 			return null;
 		}
 		if (cust == null) {
 			Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key
 					+ ", " + location + ")  failed--customer doesn't exist");
 			return null;
 		}
 
 		RMInteger price = null;
 		// check if the item is available
		args.set(0, "reserveItem");
 		try {
 			if (rtype == ReservedItem.rType.CAR) {
 				carsOut.writeObject(args);
 				price = (RMInteger) carsIn.readObject();
 			} else if (rtype == ReservedItem.rType.FLIGHT) {
 				flightOut.writeObject(args);
 				price = (RMInteger) flightIn.readObject();
 			} else if (rtype == ReservedItem.rType.ROOM) {
 				hotelOut.writeObject(args);
 				price = (RMInteger) hotelIn.readObject();
 			}
 		} catch (ClassNotFoundException e) {
 			Trace.error("Expected an RMInteger, In TCPMiddleware reserveItem");
 
 		}
 		if (price == null) {
 			Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", "
 					+ key + ", " + location
 					+ ") failed-- Object RM returned false.");
 			return null;
 		} else {
 			args.set(0, "reserve");
 			args.add(5, price.getValue());
 			customersOut.writeObject(args);
 			try {
 				ReservedItem result = (ReservedItem) customersIn.readObject();
 				if (result == null){
 					args.set(0, "unreserveKey");
 					args.set(2, key);
 					if (rtype == ReservedItem.rType.CAR) {
 						carsOut.writeObject(args);
 						carsIn.readBoolean();
 					} else if (rtype == ReservedItem.rType.FLIGHT) {
 						flightOut.writeObject(args);
 						flightIn.readBoolean();
 					} else if (rtype == ReservedItem.rType.ROOM) {
 						hotelOut.writeObject(args);
 						hotelIn.readBoolean();
 					}
 					return null;
 				}
 				return result;
 			} catch (Exception e) {
 				Trace.error("Something wrong happened in reserve");
 				return null;
 			}
 		}
 	}
 	
 	/*
 	 * unreserveItem is used by the itinerary class to cancel a reserved item when the whole reservation failed.
 	 */
 	protected boolean unreserveItem(int id, int customerID, ReservedItem item, ReservedItem.rType rtype) {
 		Trace.info("RM::unreserveItem( " + id + ", customer=" + customerID + ", " +item+ " ) called" );        
 		// Verifies if customer exists
 		Vector<Object> args = new Vector<Object>();
 		args.add("unreserve");
 		args.add(id);
 		args.add(customerID);
 		args.add(item);
 		try {
 			customersOut.writeObject(args);
 			Customer cust = (Customer) customersIn.readObject() ;
 			if(cust == null){
 				Trace.warn("RM::unreserveItem( " + id + ", " + customerID + ", " +item +" ) failed -- Customer has been deleted." );
 				return false;
 			}
 		} catch (Exception e){
 			Trace.error("RM::unreserveItem( " + id + ", " + customerID + ", " +item +" ) failed -- IOException." );
 			return false;
 		}
 
 		boolean done = false;
 		try {
 		if (rtype == ReservedItem.rType.CAR) {
 			carsOut.writeObject(args);
 			done = carsIn.readBoolean();
 		} else if (rtype == ReservedItem.rType.FLIGHT) {
 			flightOut.writeObject(args);
 			done = flightIn.readBoolean();
 		} else if (rtype == ReservedItem.rType.ROOM) {
 			hotelOut.writeObject(args);
 			done = hotelIn.readBoolean();
 		}
 		} catch (IOException e) {
 			Trace.error("RM::unreserveItem( " + id + ", " + customerID + ", " +item +" ) failed -- IOException." );
 			return false;
 		}
 		if (!done) {
 			Trace.warn("RM::unreserveItem( " + id + ", " + customerID + ", " +item +" ) failed-- Object RM returned false." );
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * Since the client sends a Vector of objects, we need this unsafe function
 	 * that retrieves the int from the vector.
 	 */
 	public static int getInt(Object temp) throws Exception {
 		try {
 			return (new Integer((String) temp)).intValue();
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 
 	public boolean getBoolean(Object temp) throws Exception {
 		try {
 			return (new Boolean((String) temp)).booleanValue();
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 
 	public String getString(Object temp) throws Exception {
 		try {
 			return (String) temp;
 		} catch (Exception e) {
 			throw e;
 		}
 	}
 
 }
