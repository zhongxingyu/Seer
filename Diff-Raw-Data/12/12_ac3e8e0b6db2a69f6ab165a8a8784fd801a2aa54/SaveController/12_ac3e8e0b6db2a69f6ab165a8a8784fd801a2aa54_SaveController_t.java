 package com.ids.controllers;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.seleniumhq.jetty7.util.log.Log;
 import org.slf4j.LoggerFactory;
 import org.apache.commons.fileupload.FileItemIterator;
 import org.apache.commons.fileupload.FileItemStream;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.fileupload.util.Streams;
 import org.apache.commons.lang3.text.WordUtils;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.google.appengine.api.rdbms.AppEngineDriver;
 import com.google.cloud.sql.jdbc.PreparedStatement;
 import com.ids.businessLogic.DownloadExcel;
 import com.ids.businessLogic.DropdownInterface;
 import com.ids.businessLogic.FirstTimeQuery;
 import com.ids.businessLogic.StoreRequestParameters;
 import com.ids.context.GetBeansFromContext;
 import com.ids.json.ColumnModel;
 import com.ids.json.ColumnNameArray;
 import com.ids.json.ColumnSummaryNameArray;
 import com.ids.json.JsonGroupSummaryWithinLoop;
 import com.ids.json.JsonPackaging;
 import com.ids.json.JsonSummaryWithinLoop;
 import com.ids.json.JsonWithInLoop;
 import com.ids.sql.SQL1;
 import com.ids.sql.SQL1GrpSummary;
 import com.ids.sql.SQL1Summary;
 import com.ids.sql.SQL2;
 import com.ids.sql.SQL3;
 import com.ids.sql.SQL4;
 import com.ids.sql.SQL5;
 import com.ids.sql.SQL6;
 import com.ids.user.User;
 
 @Controller
 @RequestMapping(value="/saverow")
 public class SaveController implements DropdownInterface {
 
 	private Connection con;
 
    	private final static Logger logger = Logger.getLogger(SaveController.class.getName()); 
        
     //   HashMap<Integer,Integer> totalLine = null;
        HashMap<String,Integer> totalLine2 = null;
    //    HashMap<Integer,Integer> otherLine = null;
 
 	 
 	 @RequestMapping( method = RequestMethod.GET)
 	public String getMethodOne(
             HttpServletResponse response,
 			HttpServletRequest request,
 			ModelMap model)  {	   
 
 		 
 		 GetBeansFromContext gcfc =null;
 		 try{
 		 
 		 logger.warning("Entering application via GEt");
 		  gcfc = new GetBeansFromContext();
 		 con = gcfc.myConnection();
 	      Statement statement = con.createStatement();
 
 	      ResultSet resultSet = null;
 	      HttpSession session = request.getSession(true);
 	      User user = (User) session.getAttribute("myUser");
 	 		 if (user==null ) {
 	   		      model.addAttribute("errortext","You must logon before you can access IDS");
 	   		   con.close();
 	   		   	  return "login";
 	   		 }
 	      
 	 		 if (request.getParameter("exit")!= null ){
 	 			 if (session.getAttribute("myUser") != null) {
 	 			    session.setAttribute("myUser",null);
 	 			 }
 	 			 con.close();
 	 			return "login"; 
 	 		 }
 	 		String      query = " select 'found' as found from ids_users where userId = '"+user.getUserName()
 					  +"' and passwordId = '"+user.getPassword()+"'";
 
 			 resultSet = statement.executeQuery(query);
 
 	           boolean found = false;
 			   while (resultSet.next()) {
 				   if (resultSet.getString("found").equals("found")) {
 					   found = true;
 					   break;
 				   }
 				   break;
 			   }
 			   if (!found) {
 		   		      model.addAttribute("errortext","Invalid user credentials");
 		   		   con.close();
 		   		   	  return "login";
 			   }
 			   
 			   model.addAttribute("openOrClose","close");
 			   
 			    String access = request.getParameter("access");
 			    logger.warning("access: "+access);
 				   
 			   
 			   if (request.getParameter("save")!= null ) {
 				  // PreparedStatement statement2 = (PreparedStatement) con.prepareStatement("Delete from Facts_"+access);
 				 //  statement2.executeUpdate();
 				   
 				   
 				   PreparedStatement statement2 = (PreparedStatement) con.prepareStatement("DELETE from FactsEdit_"+access+"  Where companyId < 0 ");  // That removes all the current OTHER rows
 				   statement2.executeUpdate();
 				   con.commit();
 				   String multiplier="";
 				   String allCompanies="11";
 			    	if (access.equals("c")) {
 			    		multiplier="*10000";
 			    		allCompanies="11"+multiplier;
 			    	}
 			    	if (access.equals("i")) {
 			    		multiplier="*20000";
 			    		allCompanies="200600000";
 			    	}
 
 				    statement2 = (PreparedStatement) con.prepareStatement(" INSERT INTO FactsEdit_"+access+" (id, companyId, countryid, productid, year, sales_production, quantity, flag, access)  "+
 				    		" select 0, -1"+multiplier+", a.countryId,a.productid,a.year,a.sales_production, " +
 			    			 "  (b.quantity -a.quantity) as quantity ,'I' , '"+access+"' " +
 			    			 " from  FactsEdit_"+access+" b, " + 
 				    		 " (  select productId, countryId, sales_production, 'I' flag, year, sum(quantity) quantity  "+
 				    		 " from FactsEdit_"+access+"  where companyID != "+allCompanies+" " +
 				    		 "  and quantity > 0  	  "+  
                              "  and flag != 'X' "  +
 				    		 "  group by productId, countryId, year, sales_production,  "+
 				    		 "  year  "+
 				    		 " )  a  "+
 				    		 "  where a.productid = b.productid "+
 				    		 " and a.countryId = b.countryId   "+
 				    		 "  and a.sales_production = b.sales_production "+
 				    		// " and a.flag = b.flag " +
 				    		 " and a.year = b.year "+
 				    		 " and b.flag != 'X' " +
 				    		 "  and b.companyid = "+allCompanies+" ");
 				    	//	 " and b.quantity - a.quantity <> 0  ");
 				    
 				    logger.warning(" INSERT INTO FactsEdit_"+access+" (id, companyId, countryid, productid, year, sales_production, quantity, flag, access)  "+
 				    		" select 0, -1"+multiplier+", a.countryId,a.productid,a.year,a.sales_production, " +
 			    			 "  (b.quantity -a.quantity) as quantity ,'I', '"+access+"' " +
 			    			 " from  FactsEdit_"+access+" b, " + 
 				    		 " (  select productId, countryId, sales_production, 'I' flag, year, sum(quantity) quantity  "+
 				    		 " from FactsEdit_"+access+"  where companyID != "+allCompanies+" " +
 				    		  "  and flag != 'X' "  +
 				    		 "  and quantity > 0  	  "+     
 				    		 "  group by productId, countryId, year, sales_production,  "+
 				    		 "  year  "+
 				    		 " )  a  "+
 				    		 "  where a.productid = b.productid "+
 				    		 " and a.countryId = b.countryId   "+
 				    		 "  and a.sales_production = b.sales_production "+
 				    		 "  and a.year = b.year "+
 				    		 " and b.flag != 'X' " +
 				    		 "  and b.companyid = "+allCompanies+" ");
 				    		// " and b.quantity - a.quantity <> 0  ");
 				    
 				    
 				    statement2.executeUpdate();
 				    con.commit();
 					
 				    
 	     if (request.getParameter("other")== null ) {  
 				    statement2 = (PreparedStatement) con.prepareStatement("Delete from Facts_"+access+"  Where companyId < 0 " );
 				  statement2.executeUpdate();
 
 					   con.commit();
 				   statement2 = (PreparedStatement) con.prepareStatement("insert into Facts_"+access+" " +
 				   		" select * from FactsEdit_"+access + " WHERE flag ='I' ");
 				   statement2.executeUpdate();
 				   con.commit();
 				   
 				   
 				   statement2 = (PreparedStatement) con.prepareStatement("delete from Facts_"+access+"  " +
 					   		" where exists (select 1 from  FactsEdit_"+access+" cc " +
 					   		 " where  cc.year= Facts_"+access+".year and cc.countryId = Facts_"+access+".countryId and cc.companyId = Facts_"+access+".companyId " +
 					   		" and cc.sales_production = Facts_"+access+".sales_production and Facts_"+access+".productId = cc.productId  " +
 					   		" and cc.flag = 'X' ) ");
 				
 				   logger.warning("delete from Facts_"+access+"  " +
 					   		" where exists (select 1 from  FactsEdit_"+access+" cc " +
 					   		 " where  cc.year= Facts_"+access+".year and cc.countryId = Facts_"+access+".countryId and cc.companyId = Facts_"+access+".companyId " +
 					   		" and cc.sales_production = Facts_"+access+".sales_production and Facts_"+access+".productId = cc.productId " +
 					   		" and cc.flag = 'X') ");
 				   
 					   statement2.executeUpdate();
 					   con.commit();
 					   
 					   statement2 = (PreparedStatement) con.prepareStatement("update Facts_"+access +" aa INNER JOIN " +
 					      " FactsEdit_"+access+" bb ON ( aa.year= bb.year and aa.countryId = bb.countryId and aa.companyId = bb.companyId " +
 					   		" and aa.sales_production = bb.sales_production and bb.productId = aa.productId) " +
 					   		" set aa.quantity = bb.quantity " +
 				            " WHERE bb.flag = 'U'");  
 					   
 	
 					   statement2.executeUpdate();
 					   con.commit();
 					   statement2 = (PreparedStatement) con.prepareStatement("delete from FactsEdit_"+access+" where flag = 'X' ");
 						   statement2.executeUpdate();
 						  
 						   statement2 = (PreparedStatement) con.prepareStatement("update FactsEdit_"+access+" set flag = '' where flag in ( 'I','D','U' )");
 						   statement2.executeUpdate();   
 						 
 					   
 				   statement2 = (PreparedStatement) con.prepareStatement("update editing set flag = '0' ");
 				   statement2.executeUpdate();
 				   logger.warning("all the updates/deletes and inserts done");
 				   con.commit();  
 				   
 	     }
 				   logger.warning(".....and also commit");
 				   model.addAttribute("saveBut","none");
 				   model.addAttribute("openOrClose","open");
 				   
 				   con.close();
 				   return "redirect:editor";
  
 			   }
 			   
 			   String year = "";
 			   String productId = "";
 			   String countryId = "";
 			   String companyId = "";
 			   String PorS ="";
 			   String SQL = "";
 			   
 			   String value = request.getParameter("dimension1Name");
 			   value = WordUtils.capitalize(value); 
                if (value.equals("Year")) {
             	   year =  request.getParameter("dimension1Val");
                } else {
             	   
             	   String convertIt = request.getParameter("dimension1Val");
             	   convertIt = convertIt.replace("&amp;", "&");
             	   if (convertIt.equals(" ALL COMPANIES")) {
             		   convertIt = "ALL COMPANIES";
             	   }
             	  SQL = " select id from "+value+ " where name = '" +convertIt +"' and access= '"+access+"' ";
             	  logger.warning("SQL1: "+SQL);
             	  logger.warning("SQL4: "+SQL);
             	  resultSet = statement.executeQuery(SQL);
             	  while (resultSet.next()) {
             		  if (value.equals("Country")) {
             			  countryId= resultSet.getString("id");
             			  break;
             		  }
             		  if (value.equals("Company")) {
             			  companyId= resultSet.getString("id");
             			  break;
             		  }
             		  if (value.equals("Product")) {
             			  productId= resultSet.getString("id");
             			  logger.warning("set but input: "+SQL);
             			  break;
             		  }
             	  }
 
                }
                
                
                value = request.getParameter("dimension2Name");
                value =WordUtils.capitalize(value) ;
                if (value.equals("Year")) {
             	   year =  request.getParameter("dimension2Val");
                } else {
             	   if (request.getParameter("dimension2Name").trim().equals("Country")){
             		   SQL = " select id from "+value+ " where country = '" +request.getParameter("dimension2Val").trim() +"'" +
             		   		" and access= '"+access+"' ";
             	   } else {
              	      SQL = " select id from "+value+ " where name = '" +request.getParameter("dimension2Val").trim() +"'" +
              	    		 " and access= '"+access+"' ";
             	   }
              	 logger.warning("SQL2: "+SQL);
              	logger.warning("value2: "+value);
              	if (resultSet != null) {
              		resultSet.close();
              	}
              	 logger.warning("SQL3: "+SQL);
              	  resultSet = statement.executeQuery(SQL);
              	  while (resultSet.next()) {
              		  logger.warning("got in here ok");
              		  if (value.trim().equals("Country")) {
              			  logger.warning("come one we must have got in here");
              			  countryId= Integer.toString(resultSet.getInt("id"));
              			  break;
              		  }
              		  if (value.equals("Company")) {
              			  companyId=Integer.toString(resultSet.getInt("id"));
              			  break;
              		  }
              		  if (value.equals("Product")) {
              			  productId= Integer.toString(resultSet.getInt("id"));
              			  break;
              		  }
              	  }
                }
                
                
                value = request.getParameter("dimension3Name");
                value =WordUtils.capitalize(value) ;
                if (value.equals("Year")) {
             	   year =  request.getParameter("dimension3Val");
                } else {
             	   if (value.equals("Country")) {
             	       SQL = " select id from "+value+ " where country = '" +request.getParameter("dimension3Val").trim() +"' " +
             	    			 " and access= '"+access+"' ";
             	   }else{
             		   SQL = " select id from "+value+ " where name = '" +request.getParameter("dimension3Val").trim() +"' " +
             					 " and access= '"+access+"' ";
             	   }
               	  resultSet = statement.executeQuery(SQL);
               	 logger.warning("SQL2: "+SQL);
               	  while (resultSet.next()) {
               		  if (value.equals("Country")) {
               			  countryId= Integer.toString(resultSet.getInt("id"));
               			  break;
               		  }
               		  if (value.equals("Company")) {
               			  companyId= Integer.toString(resultSet.getInt("id"));
               			  break;
               		  }
               		  if (value.equals("Product")) {
               			  productId= Integer.toString(resultSet.getInt("id"));
               			  break;
               		  }
               	  } 
             	   
                }
                
                if (request.getParameter("dimension4Val").equals("Sales") ) {
             	   PorS= "1"; 
                } else {
             	   PorS = "2";
                }
                
                value = request.getParameter("dimension5Name");
                value =WordUtils.capitalize(value) ;
                
                if (value.equals("Year")) {
             	   year =  request.getParameter("dimension5Val");
                } else { 
 
             	   value = value.substring(0,value.indexOf(" "));
             	   SQL = " select id from "+value+ " where shortname = '" +request.getParameter("dimension5Val") +"'" +
             				 " and access= '"+access+"' ";
                	  resultSet = statement.executeQuery(SQL);
                	  
                	  logger.warning("SQL1: "+SQL);
                	  while (resultSet.next()) {
                		  if (value.equals("Country")) {
                			  countryId= resultSet.getString("id");
                			  break;
                		  }
                		  if (value.equals("Product")) {
                			  productId= resultSet.getString("id");
                			  break;
                		  }
                	  } 
             	   
                }
                
 
                logger.warning("year: "+year);
                logger.warning("productId: "+productId);
                logger.warning("countryId: "+countryId);
                logger.warning("companyId: "+companyId);
                logger.warning("PorS: "+PorS);
                
 
                int quant =0;
                if (!request.getParameter("FactVal").trim().equals("")) { 
                    quant = Integer.parseInt(request.getParameter("FactVal"));
                }
                if (quant==0) {
             	   
            	   PreparedStatement statement2 = (PreparedStatement) con.prepareStatement("Update FactsEdit_"+access+" set quantity=0, flag='X' where "+
                 		    " productId = "+productId + " and year = "+year+ " and access = '"+access+"' "+
                 		   " and companyId = "+companyId + " and countryId = "+countryId+ " and sales_production = "+PorS);
            	   
            	   logger.warning("ZERO QUANT: "+"Update FactsEdit_"+access+" set quantity=0, flag='X' where "+
                		    " productId = "+productId + " and year = "+year+ " and access = '"+access+"' "+
                		   " and companyId = "+companyId + " and countryId = "+countryId+ " and sales_production = "+PorS);
            	   
                    int retval = statement2.executeUpdate();
             	   
                }else {
 			    
                PreparedStatement statement2 = (PreparedStatement) con.prepareStatement("Update FactsEdit_"+access+" set quantity = "+
             		   request.getParameter("FactVal")+ ",flag='U' where productId = "+productId + " and access= '"+access+"' and year = "+year+
             		   " and companyId = "+companyId + " and countryId = "+countryId+ " and sales_production = "+PorS);
                logger.warning("SQL: "+"Update FactsEdit_"+access+" set quantity = "+
             		   request.getParameter("FactVal")+ " where productId = "+productId + " and access= '"+access+"' and year = "+year+
             		   " and companyId = "+companyId + " and countryId = "+countryId+ " and sales_production = "+PorS);

        	   
        	   
                int retval = statement2.executeUpdate();
                
                if (retval==0) {
             	   String newSQL = "Insert into FactsEdit_"+access+" (quantity, productId, year, companyId, countryId," +
             	   		" sales_production, access,flag) values ("+request.getParameter("FactVal")+","+productId
             	   		+","+year+","+companyId+","+countryId+","+PorS+",'"+access+"','I')";
             	   logger.warning("InsertSQL: "+newSQL);
             	   statement2 = (PreparedStatement) con.prepareStatement(newSQL);
             	   retval = statement2.executeUpdate();
             	   
                }
                }
                
         	   String newSQL = "update editing set flag = '1' ";
         	   PreparedStatement statement3 = (PreparedStatement) con.prepareStatement(newSQL);
            	   statement3.executeUpdate();
                
                 con.commit();
                 con.close();
 		 }
                 catch(Exception e) {
                 	logger.log(Level.SEVERE,"error",e);
 
 		 
 	 } finally {
 			logger.warning("just before ending");
 			   gcfc.closeCon();
 			if (con  != null) {
 				try{
 		        con.close();
 
 				}
 		        catch(Exception e) {
 		        	logger.warning("weird error");
 		        }
 			}
 	 }
 
 			   
 			   return "login";
 	 }
 	 
 	 @Transactional
 	 @RequestMapping( method = RequestMethod.POST)
 	public String postMethodOne(
             HttpServletResponse response,
 			HttpServletRequest request,
 			ModelMap model)  {
 		 
 		 logger.warning("Entering application via POST");
 
  
 	   
 	   
 	
 		 return getMethodOne(
 		            response,
 					request,
 					model);
 
 	 }
 	 
 	
 }
