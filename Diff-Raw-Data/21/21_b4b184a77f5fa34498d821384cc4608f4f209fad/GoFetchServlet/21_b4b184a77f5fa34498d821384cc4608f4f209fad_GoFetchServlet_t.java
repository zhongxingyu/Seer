 package com.gofetch.controllers;
 
 import com.gofetch.GoFetchConstants;
 import com.gofetch.beans.GoFetchRequestBean;
 import com.gofetch.entities.*;
 import com.gofetch.seomoz.Constants;
 import com.gofetch.utils.DateUtil;
import com.gofetch.utils.TextUtil;
 
 import com.google.appengine.api.rdbms.AppEngineDriver;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.logging.Logger;
 
 import java.io.IOException;
 import java.sql.*;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 
 @SuppressWarnings("serial")
 public class GoFetchServlet extends HttpServlet {
 
 	private static Logger log = Logger.getLogger(GoFetchServlet.class.getName());
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
 		resp.setContentType("text/plain");
 		
 		URLDBService urls = new URLDBService();
 
 		//////////
 		// get data from page and fill bean....
 		GoFetchRequestBean goFetchBean = new GoFetchRequestBean();
 
 		goFetchBean.setUrl(req.getParameter("target_url"));
		
		//add trailing slash to all urls in DB...
		goFetchBean.setUrl(TextUtil.addSlashToEndOfString(goFetchBean.getUrl()));
 
 		// initial error checking, in case JS is disabled in browser:
 		if((goFetchBean.getUrl().length() < GoFetchConstants.MIN_VALID_URL_LENGTH) || 
 				(!goFetchBean.getUrl().startsWith("http://"))){
 
 			//TODO: add feedback to user here in case of error...
 
 			try {
 				getServletContext().getRequestDispatcher("/index.html").forward(req,resp);
 
 				return;
 			} catch (ServletException e) {
 				// TODO Auto-generated catch block  - do something else here....
 				e.printStackTrace();
 			}
 
 		}
 
 		
 		///////////////////////
 		// check that url not already in DB....
 		if(urls.urlInDB(goFetchBean.getUrl())){
 
 			//TODO: add feedback to user here...
 			try {
 				getServletContext().getRequestDispatcher("/index.html").forward(req,resp);
 
 				return;
 			} catch (ServletException e) {
 				// TODO Auto-generated catch block - do something else here....
 				e.printStackTrace();
 			}
 		}
 		//
 		////////////////////////////
 
 		//////////////////////
 		// url entered is valid and does not already exist in the DB - continue with retrieving rest of data...
 		goFetchBean.setUser_id(req.getParameter("user_id"));
 
 		String backlink_data = req.getParameter("backlink_data");
 		String social_data = req.getParameter("social_data");
 
 		if(null == backlink_data){
 			goFetchBean.setBackLinkData(false);
 		} else{
 			goFetchBean.setBackLinkData(true);
 		} 
 
 		if(null == social_data){
 			goFetchBean.setSocialData(false);
 		} else{
 			goFetchBean.setSocialData(true);
 		} 
 
 		//
 		//////////////
 
 		/////////////
 		// write url to DB.
 		URL url = new URL();
 
 		url.setDate(DateUtil.getTodaysDate());
 		url.setUrl_address(goFetchBean.getUrl());
 		url.setUser_id(goFetchBean.getUser_id());
 
 		url.setGet_backlinks(goFetchBean.isBackLinkData());
 		url.setGet_social_data(goFetchBean.isSocialData());
 
 		URLDBService urlDBUnit = new URLDBService();
 		urlDBUnit.createURL(url);
 
 
 		// end JPA persistence...
 		///////////////
 
 
 		try {
 			getServletContext().getRequestDispatcher("/index.html").forward(req,resp);
 		} catch (ServletException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
 		doGet(req, resp); // just direct to doGet(...)
 	}
 
 }
