 /*
  *  Copyright 2010 Jeffrey Hoyt.  All rights reserved.
  */
 
 package org.mitre.medj.servlets;
 
 import com.google.gson.*;
 
 import java.io.*;
 import java.util.logging.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.xml.bind.*;
 import javax.xml.transform.stream.*;
 
 import org.json.JSONObject;
 import org.mitre.medj.*;
 import org.mitre.medj.jaxb.*;
 
 /**
  *
  *
  * @author     jchoyt
  * @created
  */
 public class Convert extends HttpServlet
 {
 
     public final static String KEY = Convert.class.getName();
     public final static Logger log = Logger.getLogger( KEY );
     // static{log.setLevel(Level.FINER);}
 
     /**
      *  Empty constuctor must call super()
      */
      public Convert()
     {
         super();
     }
 
     /**
      *  Description of the Method
      *
      */
     @Override
     public void doGet( HttpServletRequest request, HttpServletResponse response )
         throws IOException, ServletException
     {
         response.sendRedirect("http://medcafe.org");
     }
 
 
     /**
      *  Description of the Method
      *
      */
     @Override
     public void doPost( HttpServletRequest request, HttpServletResponse response )
         throws IOException, ServletException
     {
     	
 		String dirPath = getServletContext().getRealPath("/");
         PrintWriter out = response.getWriter();
         try
         {
         	ContinuityOfCareRecord p = WebUtils.convert(request, dirPath);
         	Gson gson = new Gson();
             // Gson gson = new GsonBuilder().setPrettyPrinting().create();
             if ( p!= null)
             {
             	String jsonString = gson.toJson(p);
             	//Makes it consistent with the JSONRepresentation on Restlet side of things
             	JSONObject obj = new JSONObject(jsonString);
                response.sendRedirect("patientList.jsp");
             }
             else
             {
             	out.write("There was an error parsing your XML or creating the JSON.  Please double check your input and ensure it is a valid CCR document.");
                 
             }
         }
         catch (Exception e) {
             out.write("There was an error parsing your XML or creating the JSON.  Please double check your input and ensure it is a valid CCR document.");
             log.log(Level.SEVERE, "Error retrieving ContinuityOfCareRecord ", e);
         }
     }
 
 }
 
 
