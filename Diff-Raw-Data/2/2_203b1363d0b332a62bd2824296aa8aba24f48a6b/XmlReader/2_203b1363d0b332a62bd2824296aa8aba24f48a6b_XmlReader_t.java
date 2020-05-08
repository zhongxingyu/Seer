 package com.example.xpSearchLiang.utils;
 
 import com.example.xpSearchLiang.DBManager;
 import com.google.common.base.Strings;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 import java.util.Set;
 
 @Singleton
 public final class XmlReader {
     @Inject
     private DBManager dbManager;
 
     public void importXml(File xml) {
         FileInputStream input = null;
         Connection conn=null;
         try {
             conn = dbManager.getConnection();
             input = new FileInputStream(xml);
             SAXParserFactory spf = SAXParserFactory.newInstance();
             SAXParser saxParser = spf.newSAXParser();
             MyDefaultHandle myDefaultHandle = new MyDefaultHandle(conn);
             saxParser.parse(input, myDefaultHandle);
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             if (input != null) {
                 try {
                     input.close();
                 } catch (IOException e) {
                     //
                 }
             }
             if (conn != null) {
                 try {
                     conn.close();
                 } catch (SQLException e) {
                     //
                 }
             }
         }
 
 
     }
 
     private class MyDefaultHandle extends DefaultHandler {
         private final Connection conn;
         private Set<String> postSet = new HashSet<String>();
         private Set<String> commentSet = new HashSet<String>();
         private Set<String> userSet = new HashSet<String>();
         private Set<String> voteSet = new HashSet<String>();
         private Set<String> posthistorySet = new HashSet<String>();
         String tableName = null;
         String sql = "insert into xpsearchliang_schema.%s (%s) values (%s)";
         StringBuilder fields = new StringBuilder();
         StringBuilder values = new StringBuilder();
         private MyDefaultHandle(Connection conn) {
             //post
             postSet.add("Id");
             postSet.add("Title");
             postSet.add("Body");
             postSet.add("Tags");
             postSet.add("OwnerUserId");
             //comment
             commentSet.add("Id");
             commentSet.add("PostId");
             commentSet.add("Text");
             commentSet.add("Score");
             commentSet.add("UserId");
             //user
             userSet.add("Id");
             userSet.add("DisplayName");
             //vote
             voteSet.add("Id");
             voteSet.add("VoteTypeId");
             voteSet.add("PostId");
             //posthistory
             posthistorySet.add("Id");
             posthistorySet.add("PostHistoryTypeId");
             posthistorySet.add("PostId");
             posthistorySet.add("UserId");
             posthistorySet.add("Text");
             this.conn = conn;
         }
 
         @Override
         public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
             String lname;
             String value;
             if (qName.equals("posts")) {
                 tableName = "post";
             } else if (qName.equals("comments")) {
                 tableName = "comment";
             } else if (qName.equals("votes")) {
                 tableName = "vote";
             } else if (qName.equals("users")) {
                 tableName = "users";
            } else if (qName.equals("posthistory")) {
                 tableName = "posthistory";
             } else if (qName.equals("row")) {
                 for (int i = 0; i <= attributes.getLength(); i++) {
                     lname = attributes.getLocalName(i);
                     value = attributes.getValue(i);
                     if ((tableName.equals("post") && (postSet.contains(lname)))
                             || (tableName.equals("comment") && (commentSet.contains(lname)))
                             || (tableName.equals("users") && (userSet.contains(lname)))
                             || (tableName.equals("vote") && (voteSet.contains(lname)))
                             || (tableName.equals("posthistory") && (posthistorySet.contains(lname)))
                             ) {
                         if (i != 0) {
                             fields.append(",");
                             values.append(",");
                         }
 
                         fields.append(lname);
                         if (Strings.isNullOrEmpty(value)) {
                             values.append("Null");
                         } else {
                             if(lname.contains("Id")||lname.equals("Score")){
                                 values.append(value);
                             }else{
                                 values.append("'").append(value.replace("'","''")).append("'");
                             }
                         }
 
                     }
                 }
                 Statement st = null;
                 try {
                     //System.out.println(String.format(sql, tableName, fields, values));
                     st = conn.createStatement();
                     st.executeUpdate(String.format(sql, tableName, fields, values));
                 } catch (SQLException e) {
                     System.out.println(String.format(sql, tableName, fields, values));
 
                 } finally {
                     fields = new StringBuilder();
                     values = new StringBuilder();
                     if (st != null) {
                         try {
                             st.close();
                         } catch (SQLException e) {
                             //
                         }
                     }
                 }
 
             }
         }
     }
 }
