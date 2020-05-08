 package com.mymed.controller.core.services.tests;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.cassandra.thrift.Cassandra;
 import org.apache.cassandra.thrift.Column;
 import org.apache.cassandra.thrift.ColumnOrSuperColumn;
 import org.apache.cassandra.thrift.ColumnParent;
 import org.apache.cassandra.thrift.ColumnPath;
 import org.apache.cassandra.thrift.ConsistencyLevel;
 import org.apache.cassandra.thrift.InvalidRequestException;
 import org.apache.cassandra.thrift.NotFoundException;
 import org.apache.cassandra.thrift.SlicePredicate;
 import org.apache.cassandra.thrift.SliceRange;
 import org.apache.cassandra.thrift.TimedOutException;
 import org.apache.cassandra.thrift.UnavailableException;
 import org.apache.cassandra.thrift.Cassandra.Client;
 import org.apache.thrift.TException;
 import org.apache.thrift.protocol.TBinaryProtocol;
 import org.apache.thrift.protocol.TProtocol;
 import org.apache.thrift.transport.TSocket;
 import org.apache.thrift.transport.TTransport;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.mymed.controller.core.services.requesthandler.AbstractRequestHandler;
 
 /**
  * Servlet implementation class SDKRequestHandler
  */
 public class SDKRequestHandler extends AbstractRequestHandler implements IRequestHandler {
 
 	/* --------------------------------------------------------- */
 	/* Attributes */
 	/* --------------------------------------------------------- */
 	private static final long serialVersionUID = 1L;
 
 	private TTransport tr;
 	private TProtocol proto;
 	private Client client;
 	private String keyspace = "Testing";
 	private String columnFamily = "Services";
 	private String address;
 	private int port;
 
 	/* --------------------------------------------------------- */
 	/* Constructors */
 	/* --------------------------------------------------------- */
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public SDKRequestHandler() {
 		super();
 
 		/** Default value */
 		this.address = "138.96.242.22";
 		this.port = 4201;
 
 		this.tr = new TSocket(address, port);
 		this.proto = new TBinaryProtocol(tr);
 		this.client = new Cassandra.Client(proto);
 	}
 
 	/* --------------------------------------------------------- */
 	/* public methods */
 	/* --------------------------------------------------------- */
 	/**
 	 * Register a new myMed application
 	 * 
 	 * @param request
 	 * @param response
 	 * @param parameters
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public void register(HttpServletRequest request,
 			HttpServletResponse response, Map<String, String> parameters)
 	throws ServletException, IOException {
 		try {
 			tr.open();
 			ColumnPath colPathName = new ColumnPath(columnFamily);
 			long timestamp = System.currentTimeMillis();
 
 			/*
 			 * PARAMETERS: serviceID, name, description, background, keyNumber,
 			 * key[1..keyNumber], valueNumber, value[1..valueNumber]
 			 */
 			System.out.println("\n\nPARAMETERS" + "\n" + "\t- id = my"
 					+ parameters.get("name") + "\n" + "\t- name = "
 					+ parameters.get("name") + "\n" + "\t- description = "
 					+ parameters.get("description") + "\n"
 					+ "\t- background = " + parameters.get("background") + "\n"
 					+ "\t- icon = " + parameters.get("icon") + "\n"
 					+ "\t- keyNumber = " + parameters.get("keyNumber") + "\n"
 					+ "\t- valueNumber = " + parameters.get("valueNumber")
 					+ "\n");
 
 			String id = parameters.get("name"); // "name" == "serviceID"
 			String name = parameters.get("name");
 			String description = parameters.get("description");
 			String background = parameters.get("background");
 			String icon = parameters.get("icon");
 
 			// ID
 			colPathName.setColumn("id".getBytes("UTF8"));
 			client.insert(keyspace, id, colPathName, id.getBytes("UTF8"),
 					timestamp, ConsistencyLevel.ONE);
 			// NAME
 			colPathName.setColumn("name".getBytes("UTF8"));
 			client.insert(keyspace, id, colPathName, name.getBytes("UTF8"),
 					timestamp, ConsistencyLevel.ONE);
 			// DESCRIPTION
 			colPathName.setColumn("description".getBytes("UTF8"));
 			client.insert(keyspace, id, colPathName, description
 					.getBytes("UTF8"), timestamp, ConsistencyLevel.ONE);
 			// BACKGROUND
 			colPathName.setColumn("background".getBytes("UTF8"));
 			client.insert(keyspace, id, colPathName, background
 					.getBytes("UTF8"), timestamp, ConsistencyLevel.ONE);
 			// ICON
 			colPathName.setColumn("icon".getBytes("UTF8"));
 			client.insert(keyspace, id, colPathName, icon.getBytes("UTF8"),
 					timestamp, ConsistencyLevel.ONE);
 			// KEY DEFINITION
 			int keyNumber = Integer.parseInt(parameters.get("keyNumber"));
 			String keys = "[{\"name\" : \"" + parameters.get("key0name")
 			+ "\"," + "\"type\" : \"" + parameters.get("key0type")
 			+ "\"," + "\"description\" : \""
 			+ parameters.get("key0description") + "\"}";
 			for (int i = 1; i < keyNumber; i++) {
 				keys += "," + "{\"name\" : \""
 				+ parameters.get("key" + i + "name") + "\","
 				+ "\"type\" : \"" + parameters.get("key" + i + "type")
 				+ "\"," + "\"description\" : \""
 				+ parameters.get("key" + i + "description") + "\"}";
 			}
 			keys += "]";
 			colPathName.setColumn(("keys").getBytes("UTF8"));
 			client.insert(keyspace, id, colPathName, keys.getBytes("UTF8"),
 					timestamp, ConsistencyLevel.ONE);
 
 			// VALUE DEFINITION
 			int valueNumber = Integer.parseInt(parameters.get("valueNumber"));
 			String values = "[{\"name\" : \"" + parameters.get("value0name")
 			+ "\"," + "\"type\" : \"" + parameters.get("value0type")
 			+ "\"," + "\"description\" : \""
 			+ parameters.get("value0description") + "\"}";
 			for (int i = 1; i < valueNumber; i++) {
 				values += "," + "{\"name\" : \""
 				+ parameters.get("value" + i + "name") + "\","
 				+ "\"type\" : \""
 				+ parameters.get("value" + i + "type") + "\","
 				+ "\"description\" : \""
 				+ parameters.get("value" + i + "description") + "\"}";
 			}
 			values += "]";
 			colPathName.setColumn(("values").getBytes("UTF8"));
 			client.insert(keyspace, id, colPathName, values.getBytes("UTF8"),
 					timestamp, ConsistencyLevel.ONE);
 
 			try {
 				// THEN REGISTER THE NEW APPLICATION
 				colPathName.setColumn(("list").getBytes("UTF8"));
 				Column col = client.get(keyspace, "registred", colPathName,
 						ConsistencyLevel.ONE).getColumn();
 				String list = new String(col.value, "UTF8");
 				String items[] = list.split(",");
 				Set<String> listSet = new HashSet<String>();
 				for (String item : items) {
 					if (!item.equals("")) {
 						listSet.add(item);
 					}
 				}
 				listSet.add(id); // new Entry
 				Iterator<String> i = listSet.iterator();
 				list = "";
 				String item;
 				while (i.hasNext()) {
 					item = i.next();
 					list += i.hasNext() ? item + "," : item;
 				}
 				// store this new list
 				client.insert(keyspace, "registred", colPathName, list
 						.getBytes("UTF8"), timestamp, ConsistencyLevel.ONE);
 			} catch (NotFoundException e) { // For the bootstrap
 				client.insert(keyspace, "registred", colPathName, id
 						.getBytes("UTF8"), timestamp, ConsistencyLevel.ONE);
 			}
 		} catch (TException e) {
 			e.printStackTrace();
 		} catch (InvalidRequestException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnavailableException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TimedOutException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			tr.close();
 			processRegisterRequest(request, response);
 		}
 	}
 
 	/**
 	 * Return the list of the registred myMed applications
 	 * 
 	 * @param request
 	 * @param response
 	 * @param parameters
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public void getApplicationList(HttpServletRequest request,
 			HttpServletResponse response, Map<String, String> parameters)
 	throws ServletException, IOException {
 		String list = "null";
 		try {
 			tr.open();
 			ColumnPath colPathName = new ColumnPath(columnFamily);
 			colPathName.setColumn(("list").getBytes("UTF8"));
 			Column col = client.get(keyspace, "registred", colPathName,
 					ConsistencyLevel.ONE).getColumn();
 			System.out.println(col);
 			list = new String(col.value, "UTF8");
 			System.out.println("list = " + list);
 		} catch (UnavailableException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidRequestException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TimedOutException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			tr.close();
 			PrintWriter out = response.getWriter();
 			out.println(list);
 			out.close();
 		}
 	}
 
 	/**
 	 * Return an application with the json format
 	 * 
 	 * @param request
 	 * @param response
 	 * @param parameters
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public void getJSONApplication(HttpServletRequest request,
 			HttpServletResponse response, Map<String, String> parameters)
 	throws ServletException, IOException {
 		PrintWriter out = response.getWriter();
 		String application = "\n{ \"App\" : \"Err\"";
 		try {
 			tr.open();
 			String applicationID = parameters.get("id"); // id == name
 
 			// read entire row
 			SlicePredicate predicate = new SlicePredicate();
 			SliceRange sliceRange = new SliceRange();
 			sliceRange.setStart(new byte[0]);
 			sliceRange.setFinish(new byte[0]);
 			predicate.setSlice_range(sliceRange);
 
 			application = "{ \"App\" : {";
 			ColumnParent parent = new ColumnParent(columnFamily);
 			List<ColumnOrSuperColumn> results = client.get_slice(keyspace,
 					applicationID, parent, predicate, ConsistencyLevel.ONE);
 			for (ColumnOrSuperColumn result : results) {
 				Column column = result.column;
 				if (!new String(column.name, "UTF8").contains("key")
 						&& !new String(column.name, "UTF8").contains("value")) {
 					application += "\"" + new String(column.name, "UTF8")
 					+ "\" : " + "\"" + new String(column.value, "UTF8")
 					+ "\",";
 				} else {
 					application += "\"" + new String(column.name, "UTF8")
 					+ "\" : " + "" + new String(column.value, "UTF8")
 					+ ",";
 				}
 			}
 			application = application.substring(0, application.length() - 1);
 			application += "}";
 
 		} catch (TException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidRequestException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnavailableException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TimedOutException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			tr.close();
 			application += "}";
 			out.println(application);
 			out.close();
 		}
 	}
 
 	/**
 	 * Return the Cassandra representation of the application
 	 * @param applicationID 
 	 * 			the ID of the application
 	 * @return List<ColumnOrSuperColumn>
 	 * 		the application defined by applicationID
 	 */
 	public List<ColumnOrSuperColumn> getApplication(String applicationID){
 		try {
 			tr.open();
 			// Read entire row
 			SlicePredicate predicate = new SlicePredicate();
 			SliceRange sliceRange = new SliceRange();
 			sliceRange.setStart(new byte[0]);
 			sliceRange.setFinish(new byte[0]);
 			predicate.setSlice_range(sliceRange);
 			ColumnParent parent = new ColumnParent(columnFamily);
 			return client.get_slice(keyspace,
 					applicationID, parent, predicate, ConsistencyLevel.ONE);
 		} catch (TException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidRequestException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnavailableException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TimedOutException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			tr.close();
 		}
 		return null;
 	}
 
 	/**
 	 * Execute a Publish request for a specific application
 	 * @param request
 	 * @param response
 	 * @param parameters
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public void publish(HttpServletRequest request,
 			HttpServletResponse response, Map<String, String> parameters)
 	throws ServletException, IOException {
 		try {
 			// Retrieve the application responsible for the request
 			List<ColumnOrSuperColumn> application = getApplication(parameters.get("id"));
 
			tr.open();
 			// Reconstruct the API
 			List<Map<String, String>> keyList = null;
 			List<Map<String, String>> valueList = null;
 			for (ColumnOrSuperColumn label : application) {
 				Column column = label.column;
 				String jsonName = new String(column.name, "UTF8");
 				String jsonValue = new String(column.value, "UTF8");
 				if (jsonName.contains("keys")) {
 					keyList = new Gson().fromJson(
 							jsonValue,
 							new TypeToken<List<Map<String, String>>>() {
 							}.getType());
 				} else if (jsonName.contains("values")) {
 					valueList = new Gson().fromJson(
 							jsonValue,
 							new TypeToken<List<Map<String, String>>>() {
 							}.getType());
 				}
 			}
 
 			// Get the request parameters and format the values
 			Map<String, String> keys = new HashMap<String, String>();
 			List<Map<String, String>> values = new ArrayList<Map<String,String>>();
 			if(keyList != null && valueList != null){
 				for(Map<String, String> key : keyList){
 					String name = key.get("name");
 					keys.put(name, parameters.get(name));
 				} 
 				for(Map<String, String> value : valueList){
 					String name = value.get("name");
 					value.put(name, parameters.get(name));
 					values.add(value);
 				}
 			}
 
 			// Execute the  publish request
 			for(String key : keys.keySet()){
 				System.out.println(new Gson().toJson(values));
 			}
 
 
 		} catch (TException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 //		} catch (InvalidRequestException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} catch (UnavailableException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 //		} catch (TimedOutException e) {
 //			// TODO Auto-generated catch block
 //			e.printStackTrace();
 		} finally {
 			tr.close();
 		}
 	}
 
 	/**
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void processRegisterRequest(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		PrintWriter out = response.getWriter();
 		out.println("<html>" + "<head>" + "<title>Redirection en htm</title>"
 				+ "<meta http-equiv=\"refresh\" content=\"3; URL=http://"
 				+ InetAddress.getLocalHost().getHostAddress()
 				+ "/sdk?applications=true\"" + "</head>" + "<body>"
 				+ "Application en construction..." + "</body>" + "</html>");
 		out.close();
 	}
 
 	/* --------------------------------------------------------- */
 	/* extends HttpServlet == REST API */
 	/* --------------------------------------------------------- */
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		/** Get the parameters */
 		Map<String, String> parameters = getParameters(request);
 
 		if (parameters.containsKey("act")) {
 			int chx = Integer.parseInt(parameters.get("act"));
 			switch (chx) {
 			case REGISTER:
 				register(request, response, parameters);
 				break;
 			default:
 				break;
 			}
 		}
 
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		/** Get the parameters */
 		Map<String, String> parameters = getParameters(request);
 
 		if (parameters.containsKey("act")) {
 			int chx = Integer.parseInt(parameters.get("act"));
 			switch (chx) {
 			case GETAPPLIST:
 				getApplicationList(request, response, parameters);
 				break;
 			case GETAPPLIACTION:
 				getJSONApplication(request, response, parameters);
 				break;
 			case PUBLISH:
 				publish(request, response, parameters);
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 }
