 package com.ids.businessLogic;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.springframework.ui.ModelMap;
 
 import com.ids.controllers.MainController;
 import com.ids.entities.Country;
 
 public class ReplaceDropsWithFilterValues {
 
 	private Connection con;
 
    	private final static Logger logger = Logger.getLogger(ReplaceDropsWithFilterValues.class.getName()); 
 	private ModelMap model;
 	private boolean countryFilter = false;
 	public ReplaceDropsWithFilterValues ( Connection con, String countries, String access, ModelMap model, int countryId ) throws SQLException {
 
 		this.model = model;
 		ResultSet resultSet = null;
 		Statement statement = con.createStatement();
 		 
 		countryFilter=false;
 
 		if (countries==null || countries.length()<3) {
 			countries="";
 		}
 			 model.addAttribute("drop11","drop11");
 		
 	         String  query = "select c.id as id , c.country as country from Country c where c.id != 0 and c.access = '"+access+"' " +
 	         		countries +" order by c.country asc " ;
 	      
 	         logger.warning(query);
 
 		      resultSet = statement.executeQuery(query);
 		      StringBuilder sb = new StringBuilder();
 		      while (resultSet.next()) {
 		    	  sb.append("<option value="+resultSet.getString("id")+">"+resultSet.getString("country")+" </option>");
 		          if (Integer.parseInt(resultSet.getString("id"))==countryId ){
 		        	  countryFilter=true;
 		        	  logger.warning("found ID when should not");
 		          }
 		      }
 
 		      model.addAttribute("countries",sb.toString());


		
 	}
 	
 	public ModelMap getModel() {
 		return model;
 	}
 	public boolean getCountryFilter() {
 		return countryFilter;
 	}
 }
