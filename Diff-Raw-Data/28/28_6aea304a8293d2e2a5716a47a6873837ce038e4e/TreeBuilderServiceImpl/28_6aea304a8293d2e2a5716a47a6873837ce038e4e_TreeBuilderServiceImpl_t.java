 package com.follett.mywebapp.server;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.follett.mywebapp.util.ValidationTreeDataItem;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * The server side implementation of the RPC service.
  */
 public class TreeBuilderServiceImpl extends RemoteServiceServlet implements TreeBuilderService {
 
   private static final long serialVersionUID = 1L;
 
   /**
    * Implement the connection to the server and gather the necessary data
    * */
   @Override
   public HashMap<String, ArrayList<ValidationTreeDataItem>> getTreeItems() {
 	  HashMap<String, ArrayList<ValidationTreeDataItem>> returnable = new HashMap<String, ArrayList<ValidationTreeDataItem>>();
	  ArrayList<ValidationTreeDataItem> exception = new ArrayList<ValidationTreeDataItem>();
	  exception.add(new ValidationTreeDataItem("","root","Exception", Integer.valueOf(0)));
 	  try {
 		  Class.forName(DatabaseParameters.getTDSDriver()).newInstance();
 		  String url = DatabaseParameters.getDatabaseURL();
 		  Connection conn = DriverManager.getConnection(url, DatabaseParameters.getConnectionProperties());
 		  Statement stmt = conn.createStatement();
 		  ResultSet rs = stmt.executeQuery("SELECT * FROM dbo.TreeItems ORDER BY parentTagID");
 		  String tagID;
 		  String parentTagID;
 		  String description;
 		  Integer fields;
 		  String fieldDescriptions;
 		  while(rs.next()) {
 			  tagID = rs.getString("tagID");
 			  parentTagID = rs.getString("parentTagID");
 			  description = rs.getString("label");
 			  fields = Integer.valueOf(rs.getInt("textfields"));
 			  fieldDescriptions = rs.getString("fieldDescriptions");
 			  ValidationTreeDataItem item = new ValidationTreeDataItem(tagID, parentTagID, description, fields);
 			  item.addDescriptions(fieldDescriptions);
 			  ArrayList<ValidationTreeDataItem> currentList;
 			  if(returnable.containsKey(parentTagID)) {
 				  currentList = returnable.get(parentTagID);
 			  } else {
 				  currentList = new ArrayList<ValidationTreeDataItem>();
 			  }
 			  currentList.add(item);
 			  returnable.put(parentTagID, currentList);
 		  }
 		  conn.close();
 	  } catch (SQLException e) {
		  exception.get(0).setDescription(e.toString());
		  returnable.put("root", exception);
 	  } catch (ClassNotFoundException e) {
		  exception.get(0).setDescription(e.toString());
		  returnable.put("root", exception);
	  } catch (InstantiationException e) {
		  exception.get(0).setDescription(e.toString());
		  returnable.put("root", exception);
	  } catch (IllegalAccessException e) {
		  exception.get(0).setDescription(e.toString());
		  returnable.put("root", exception);
	  }
 	  return returnable;
   }
 
   public Boolean saveTreeItems(ArrayList<ValidationTreeDataItem> nodes) {
 	  Boolean exception = Boolean.FALSE;
 	  try {
 		  Class.forName(DatabaseParameters.getTDSDriver()).newInstance();
 		  String url = DatabaseParameters.getDatabaseURL();
 		  Connection conn = DriverManager.getConnection(url, DatabaseParameters.getConnectionProperties());
 		  Statement stmt = conn.createStatement();
 		  String sql = "";
 		  for (ValidationTreeDataItem item : nodes) {
 			  sql = "UPDATE dbo.TreeItems SET " +
 			  "parentTagID = '" + item.getParentTagID() + "', " +
 			  "description = '" + item.getDescription() + "', " +
 			  "fields = '" + item.getFields() + "', " +
 			  "fieldDescriptions = '" + item.getDescriptionsToString() + "' " +
 			  "WHERE tagID = '" + item.getTagID() + "'";
 			  int success = stmt.executeUpdate(sql);
 			  if(success == 0) {
 				  sql = "INSERT INTO dbo.TreeItems " +
 				  "(tagID, parentTagID, description, fields, fieldDescriptions) values (" +
 				  "'" + item.getTagID() + "', " +
 				  "'" + item.getParentTagID() + "', " +
 				  "'" + item.getDescription() + "', " +
 				  "'" + item.getFields() + "', " +
 				  "'" + item.getDescriptionsToString() + "') ";
 				  success = stmt.executeUpdate(sql);
 			  }
 		  }
 		  conn.close();
 	  } catch (InstantiationException e) {
 		  exception = Boolean.TRUE;
 	  } catch (IllegalAccessException e) {
 		  exception = Boolean.TRUE;
 	  } catch (ClassNotFoundException e) {
 		  exception = Boolean.TRUE;
 	  } catch (SQLException e) {
 		  exception = Boolean.TRUE;
 	  }
 	  return exception;
   }
 }
