 package org.netvogue.server.webmvc.controllers;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.netvogue.server.aws.core.UploadManager;
 import org.netvogue.server.neo4japi.common.Constants;
 import org.netvogue.server.neo4japi.common.ProductLines;
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.domain.Category;
 import org.netvogue.server.neo4japi.domain.Style;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.service.BoutiqueService;
 import org.netvogue.server.neo4japi.service.LinesheetData;
 import org.netvogue.server.neo4japi.service.LinesheetService;
 import org.netvogue.server.neo4japi.service.StyleData;
 import org.netvogue.server.neo4japi.service.StylesheetService;
 import org.netvogue.server.neo4japi.service.UserService;
 import org.netvogue.server.webmvc.domain.ImageURLsResponse;
 import org.netvogue.server.webmvc.domain.JsonRequest;
 import org.netvogue.server.webmvc.domain.JsonResponse;
 import org.netvogue.server.webmvc.domain.Linesheet;
 import org.netvogue.server.webmvc.domain.LinesheetJSONRequest;
 import org.netvogue.server.webmvc.domain.Linesheets;
 import org.netvogue.server.webmvc.domain.StyleJSONResponse;
 import org.netvogue.server.webmvc.domain.StyleResponse;
 import org.netvogue.server.webmvc.domain.Styles;
 import org.netvogue.server.webmvc.security.NetvogueUserDetailsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.convert.ConversionService;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class LinesheetController {
 	@Autowired NetvogueUserDetailsService 	userDetailsService;
 	@Autowired BoutiqueService  			boutiqueService;
 	@Autowired UserService 					userService;
 	@Autowired LinesheetService				linesheetService;
 	@Autowired StylesheetService			stylesheetService;
 	@Autowired ConversionService			conversionService;
 
 	@Autowired
 	private UploadManager uploadManager;
 
 	@RequestMapping(value={"/getlinesheets", "/getlinesheets/{profileid}"}, method=RequestMethod.GET)
 	public @ResponseBody Linesheets GetLinesheets(@ModelAttribute("profileid") String profileid, 
 											@RequestParam(value="brandname", required=false) String brandname,
 											@RequestParam(value="category", required=false) String categories,
 											@RequestParam(value="fromdate", required=false) String fromDate,
 											@RequestParam(value="linesheetname", required=false) String linesheetname,
 											@RequestParam(value="todate", required=false) String toDate,
 											@RequestParam(value="fromprice", required=false, defaultValue="0") long fromPrice,
 											@RequestParam(value="toprice", required=false, defaultValue="0") long toPrice) {
 		System.out.println("Get Linesheets: " + linesheetname +
 				"\nCategory: " + categories +
 				"\nFrom Date: " + fromDate +
 				"\nTo Date: " + toDate +
 				"\nFrom Price: " + fromPrice +
 				"\nTo Price: " + toPrice);
 		Linesheets linesheets = new Linesheets();
 		
 		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy"); 
 		Date todate = null;
 		Date fromdate = null;
 		try {
 			//From Date
 			if(null == fromDate || fromDate.equals("0") || fromDate.isEmpty() || fromDate.equals(Constants.IMMEDIATE))
 				fromdate = new Date(0);
 			else 
 				fromdate = format.parse(fromDate);
 			
 			//To date
 			if(null == toDate || toDate.equals("0") || toDate.isEmpty())
 				todate = new Date(Long.MAX_VALUE);
 			else if(toDate.equals(Constants.IMMEDIATE))
 				todate = new Date(0);
 			else
 				todate = format.parse(toDate);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		User user;
 		if(!profileid.isEmpty()) {
 			user = userService.getUserByUsername(profileid);
 			if(user == null || USER_TYPE.BOUTIQUE == user.getUserType()) {
 				return linesheets;
 			}
 		} else {
 			 user = userDetailsService.getUserFromSession();
 		}
 		
 		linesheets.setName(user.getName());
 		linesheets.setProfilepic(conversionService.convert(user.getProfilePicLink(), ImageURLsResponse.class));
 		Set<Linesheet> linesheetTemp = new LinkedHashSet<Linesheet>();
 		Iterable<LinesheetData> dbLinesheets;
 		if( (null == linesheetname || linesheetname.isEmpty()) && 
 			(null == brandname || brandname.isEmpty()) && 
 			(null == categories || categories.isEmpty()) && 
 			(null == fromDate || fromDate.isEmpty() || fromDate.equals("0")) && 
 			(null == toDate || toDate.isEmpty() || toDate.equals("0")) && 
 			(0 == fromPrice && 0 == toPrice)
 		) {
 			dbLinesheets = userService.getLinesheets(user);
 		} else {
 			Set<String> productlines = new HashSet<String>();
 			if(null != categories && !categories.isEmpty()) {
 				List<String> categoriesafter =  Arrays.asList(categories.split(","));
 				for(String productline: categoriesafter) {
 					ProductLines productLine = ProductLines.getValueOf(productline);
 					if(null != productLine) {
 						System.out.println("product line is:" + productLine.toString());
 						productlines.add(productLine.toString());
 					}
 					else
 						System.out.println("product line is null");
 				}
 			}
 			dbLinesheets = userService.searchLinesheets(user, linesheetname, productlines,
 															fromdate, todate, fromPrice, toPrice,
 															brandname);
 		}
 		if(null == dbLinesheets) {
 			return linesheets;
 		}
 		Iterator<LinesheetData> first = dbLinesheets.iterator();
 		while ( first.hasNext() ){
 			LinesheetData dbSheet = first.next();
 			
 			Linesheet sheet = conversionService.convert(dbSheet.getLinesheet(), Linesheet.class);
 			sheet.setBrandname(dbSheet.getName());
 			linesheetTemp.add(sheet);
 		}
 		linesheets.setLinesheets(linesheetTemp);
 		
 		return linesheets;
 	}
 	
 	@RequestMapping(value={"/linesheet/getstyles", "/linesheet/getstyles/{profileid}"}, method=RequestMethod.GET)
 	public @ResponseBody Styles GetStyles(@ModelAttribute("profileid") String profileid, 
										  @RequestParam("linesheetid") String linesheetid,
										  @RequestParam(value="styleno", required=false) String styleno,
										  @RequestParam(value="fromprice", required=false) long fromPrice,
										  @RequestParam(value="toprice", required=false) long toPrice
 											 ) {
 		System.out.println("Get Styles: " + linesheetid +
 							"\n Styleno:" + styleno +
 							"\n fromprice" + fromPrice +
 							"\n toprice" + toPrice);
 		Styles styles = new Styles();
 		if(linesheetid.isEmpty()) {
 			return styles;
 		}
 		
 		User user;
 		if(!profileid.isEmpty()) {
 			user = userService.getUserByUsername(profileid);
 			if(user == null || USER_TYPE.BOUTIQUE == user.getUserType()) {
 				return styles;
 			}
 		} else {
 			 user = userDetailsService.getUserFromSession();
 		}
 		
 		styles.setName(user.getName());
 		styles.setProfilepic(conversionService.convert(user.getProfilePicLink(), ImageURLsResponse.class));
 		//This must be stored in session attributes from last query..shoudn't get it from database every time - Azeez
 		org.netvogue.server.neo4japi.domain.Linesheet s = linesheetService.getLinesheet(linesheetid);
 		if(null == s)
 			return styles;
 		styles.setStylesheetname(s.getLinesheetname());
 		
 		Set<StyleResponse> stylesTemp = new LinkedHashSet<StyleResponse>();
 		Iterable<StyleData> dbStyles;
 		
 		if((null == styleno || styleno.isEmpty()) &&
 				(0 == fromPrice) && (0 == toPrice)) {
 			dbStyles = linesheetService.getStyles(linesheetid);
 		} else {
 			//Change this after implementing query
 			dbStyles = linesheetService.searchStyles(linesheetid, styleno, fromPrice, toPrice);
 		}
 		if(null == dbStyles) {
 			return styles;
 		}
 		Iterator<StyleData> first = dbStyles.iterator();
 		while ( first.hasNext() ){
 			StyleData dbStyle = first.next() ;
 			
 			StyleResponse newResponse = conversionService.convert(dbStyle.getStyle(), StyleResponse.class);
 			styles.setBrandname(dbStyle.getName()); //See for better way of implementation
 			stylesTemp.add(newResponse);
 		}
 		styles.setStyles(stylesTemp);
 		
 		return styles;
 	}
 	
 	@RequestMapping(value="linesheet/create", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse CreateLinesheet(@RequestBody LinesheetJSONRequest request) {
 		System.out.println("Create Linesheet:" + request.toString());
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		
 		User loggedinUser = userDetailsService.getUserFromSession();
 		if(loggedinUser.getUserType() != USER_TYPE.BRAND) {
 			response.setError("Only brands can create Linesheets");
 			return response;
 		}
 		org.netvogue.server.neo4japi.domain.Linesheet newLinesheet = 
 				new org.netvogue.server.neo4japi.domain.Linesheet(request.getName(), loggedinUser, request.getDeliverydate()); 
 		
 		if(null != request.getCategory()) {
 			ProductLines productLine = ProductLines.getValueOf(request.getCategory());
 			Category cat = boutiqueService.getOrCreateCategory(productLine);
 			newLinesheet.setProductcategory(cat);
 		}
 		if(ResultStatus.SUCCESS == linesheetService.SaveLinesheet(newLinesheet, error)) {  
 			response.setStatus(true);
 			response.setIdcreated(newLinesheet.getLinesheetid());
 		}
 		else
 			response.setError(error);
 		
 		return response;
 	}
 	
 	@RequestMapping(value="linesheet/edit", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse EditLinesheet(@RequestBody LinesheetJSONRequest request) {
 		System.out.println("Edit Linesheet");
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		
 		if(null == request.getId() || request.getId().isEmpty()) {
 			response.setError("linesheet Id is empty");
 			return response;
 		} else if(null == request.getName()) {
 			response.setError("new name is empty");
 			return response;
 		}
 		
 		if(ResultStatus.SUCCESS == linesheetService.editLinesheet(request.getId(), request.getName(), request.getDeliverydate(), error))   
 			response.setStatus(true);
 		else
 				response.setError(error);	
 		return response;
 	}
 	
 	//Think about the categories as well
 	@RequestMapping(value="linesheet/delete", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse DeleteLinesheet(@RequestBody String linesheetId) {
 		System.out.println("Delete Linesheet:"+ linesheetId);
 		String error = "";
 		JsonResponse response = new JsonResponse();
 		
 		if(null == linesheetId || linesheetId.isEmpty()) {
 			response.setError("linesheetId is empty");
 			return response;
 		}
 		if(ResultStatus.SUCCESS == linesheetService.deleteLinesheet(linesheetId, error)) {  
 			response.setStatus(true);
 		}
 		else
 			response.setError(error);
 		
 		return response;
 	}
 	
 	@RequestMapping(value ="linesheet/addstyle",method=RequestMethod.POST)
 	public @ResponseBody StyleJSONResponse AddStyle(@RequestBody JsonRequest request) throws Exception {
 		
 			System.out.println("Add new Style" + request.getValue());
 			String error = "";
 			StyleJSONResponse response = new StyleJSONResponse();
 			
 			User loggedinUser = userDetailsService.getUserFromSession();
 			if(loggedinUser.getUserType() != USER_TYPE.BRAND) {
 				response.setError("Only brands can create styles");
 				return response;
 			}
 			org.netvogue.server.neo4japi.domain.Linesheet linesheet = linesheetService.getLinesheet(request.getId());
 			if(null == linesheet) {
 				response.setError("No linesheet present with this id");
 				return response;
 			}
 			
 			Style styleToAdd = stylesheetService.getStyle(request.getValue(), request.getId());
 			if(null == styleToAdd) {
 				response.setError("No style available with this style id");
 				System.out.println("No style available with this style id");
 				return response;
 			}
 			linesheet.addStyles(styleToAdd);
 			if(ResultStatus.SUCCESS == linesheetService.SaveLinesheet(linesheet, error)) {  
 				response.setStatus(true);
 			}
 			else
 				response.setError(error);
 			
 			return response;
 	}
 	
 	//Change these things once the whole application is completed
 	//All these queries must be changed, as anyone can delete these things if they just have userid Azeez
 	@RequestMapping(value="linesheet/deletestyle", method=RequestMethod.POST)
 	public @ResponseBody JsonResponse DeleteStyle(@RequestBody String styleid) {
 		System.out.println("Delete Style:" + styleid);
 		String error = "";
 		
 		JsonResponse response = new JsonResponse();
 		if(!styleid.isEmpty()) {
 			if(ResultStatus.SUCCESS == linesheetService.deleteStyle(styleid, error)) {  
 				response.setStatus(true);
 			}
 			else
 				response.setError(error);
 		} else {
 			response.setError("styleid is empty");
 		}
 		
 		return response;
 	}
 }
