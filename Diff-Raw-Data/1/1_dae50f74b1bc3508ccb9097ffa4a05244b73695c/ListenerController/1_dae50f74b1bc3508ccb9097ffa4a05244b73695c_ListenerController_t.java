 /*
 *
 * Author: Rob Shan Lone
 * Copyright (c) 2012 The Old County Limited.
 *
 * All rights reserved.
 */
 
 package net.oldcounty.controller;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.oldcounty.dao.ImageDao;
 import net.oldcounty.dao.InterestDao;
 import net.oldcounty.dao.PersonDao;
 import net.oldcounty.dao.PrivateMessageDao;
 import net.oldcounty.manager.PersonManager;
 import net.oldcounty.model.Interests;
 import net.oldcounty.model.Person;
 
 import net.oldcounty.model.PrivateMessage;
 
 import org.bson.types.ObjectId;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.MongoException;
 
 @Controller
 public class ListenerController{
 
 	private PersonManager personManager;
 
     public void setPersonManager(PersonManager personManager){
     	this.personManager = personManager;
     }
         
     /**
      * Edit Chart
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/edit_chart")
     public ModelAndView editDisplay(
     		HttpServletRequest request,
     		@RequestParam(value="userId", required=false) String userId,
     		@RequestParam(value="interests", required=false) String[] interests,
     		@RequestParam(value="interestsknobs", required=false) Integer[] interestsknobs,
     		@RequestParam(value="chart", required=false) String chart,
     		@RequestParam(value="chartId", required=false) String chartId,
     		@RequestParam(value="submitted", required=false) String submitted
     		) throws UnknownHostException, MongoException
     {    	
     	/*_interests chart*/
 	    Map<String,Integer> interestData = new LinkedHashMap<String,Integer>();
     	if(interests!=null){
     		int index = 0;
     		for(String interest : interests)
 	    	{
     			interestData.put(interest,interestsknobs[index]);
 	    		index++;
 	    	}
     	}
     	
     	Interests interest = new Interests();
     		interest.setCid(chartId);
 	    	interest.setUserId(userId);
 	    	interest.setName(chart);//type of chart/name of chart
 	    	interest.setResults(interestData);
     	
     	if(submitted == null){
     		//__if not yet added a chart return html form	    	
 			return new ModelAndView("jsp/user/chart_handler");   	
     	}else{
     		return new ModelAndView("jsp/json/response", "json", InterestDao.updateInterest(interest));  	
     	}       	
     }
     
     
        
     
     
     /**
      * Edit User
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/edit_user")
     public ModelAndView editUser(
     		HttpServletRequest request,
     		@RequestParam(value="userId", required=false) String userId,
     		@RequestParam(value="section", required=false) String section, 
     		@RequestParam(value="realname", required=false) String realname,
     		@RequestParam(value="username", required=false) String username,
     		@RequestParam(value="emailaddress", required=false) String emailaddress,
     		@RequestParam(value="confirmemailaddress", required=false) String confirmemailaddress,
     		@RequestParam(value="password", required=false) String password,
     		@RequestParam(value="confirmpassword", required=false) String confirmpassword,
     		@RequestParam(value="whichscreenname", required=false) String whichscreenname,    
     		@RequestParam(value="birthyear", required=false) String birthyear,
     		@RequestParam(value="birthmonth", required=false) String birthmonth,
     		@RequestParam(value="birthday", required=false) String birthday,
     		@RequestParam(value="about", required=false) String about,
     		@RequestParam(value="ethnicity", required=false) String ethnicity,
     		@RequestParam(value="country", required=false) String country,
     		@RequestParam(value="lookingfor", required=false) String lookingfor,
     		@RequestParam(value="kindofrelationship", required=false) String kindofrelationship,
     		@RequestParam(value="languages", required=false) String[] languages,
     		@RequestParam(value="bodytype", required=false) String bodytype,
     		@RequestParam(value="haircolor", required=false) String haircolor,
     		@RequestParam(value="eyecolor", required=false) String eyecolor,
     		@RequestParam(value="children", required=false) String children,
     		@RequestParam(value="occupation", required=false) String occupation,
     		@RequestParam(value="education", required=false) String education,
     		@RequestParam(value="gender", required=false) String gender,
     		@RequestParam(value="seeking", required=false) String seeking,    		 		
     		@RequestParam(value="personality", required=false) Integer[] personality,
     		@RequestParam(value="submitted", required=false) String submitted
     		) throws UnknownHostException, MongoException
     {    	
     	//_new person
     	Person person = new Person();
     	
     	person.setRealname(realname);
     	person.setUsername(username);
     	person.setEmailaddress(emailaddress);
     	person.setConfirmemailaddress(confirmemailaddress);
     	person.setPassword(password);
     	person.setConfirmpassword(confirmpassword);
     	person.setWhichscreenname(whichscreenname);
     	person.setBirthyear(birthyear);
     	person.setBirthmonth(birthmonth);
     	person.setBirthday(birthday);
     	
     	person.setUid(userId);
     	person.setAbout(about);
     	
     	person.setEthnicity(ethnicity);
     	person.setCountry(country);
     	person.setKindofrelationship(kindofrelationship);
     	person.setLanguages(languages);
     	person.setBodytype(bodytype);
     	person.setHaircolor(haircolor);
     	person.setEyecolor(eyecolor);
     	person.setChildren(children);
     	person.setEducation(education);
     	person.setOccupation(occupation);    	
     	
     	BasicDBObject personaltraits = new BasicDBObject();
 
     	if(personality != null){
 		    /*add to new collection personality*/
 			String[] personalTypes ={"confidence","reasoning","emotion","daring","attachment","sensitivity","comedy"};
 			Integer j = 0;
 			for(Integer traits : personality)
 	    	{
 	    		//System.out.println("traits "+traits);
 	    		personaltraits.put(personalTypes[j], traits);
 	    		j++;
 	    	}	
     	}
     	person.setPersonality(personaltraits);
     	
     	/*    	
     	person.setGender(gender);    	
 
     	person.setLongitude(longitude);
     	person.setLatitude(latitude);
     	
     	person.setInterests(interests);
     	person.setInterestknobs(interestknobs);
     	person.setSeekings(seekings);
     	person.setSeekingknobs(seekingknobs);
     	person.setVisitings(visitings);
     	person.setVisitingknobs(visitingknobs);
     	person.setGoal1(goal1);
     	person.setGoal2(goal2);
     	person.setGoal3(goal3);
     	*/
     	
     	if(submitted == null){
     		//__if not yet added a chart return html form	    	
 			return new ModelAndView("jsp/user/user_handler");   	
     	}else{
     		//_ register the user into the database and return a json response
         	return new ModelAndView("jsp/json/response", "json", personManager.editUser(person, section));
         	
     		//return new ModelAndView("jsp/json/response", "json", InterestDao.updateInterest(interest, chartId));  	
     	}       	
     }
     
     /**
      * get Followers
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/viewFollowers")
     public ModelAndView viewFollowers(
 	    		HttpServletRequest request
     		) throws UnknownHostException, MongoException
     {	
 	 	//_ get followers the user into the database and return a json response
 		return new ModelAndView("jsp/user/view_followers");
     } 
     
     /**
      * add Followers
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/addFollowers")
     public ModelAndView addFollowers(
 	    		HttpServletRequest request,
 	    		@RequestParam(value="submitted", required=false) String submitted
     		) throws UnknownHostException, MongoException
     {	
     	if(submitted == null){
     		//__show add follower form    	
 			return new ModelAndView("jsp/user/add_followers");   	
     	}else{
     		//_ json response add follower
     		String json = null;
         	return new ModelAndView("jsp/json/response", "json", json);  	
     	}	
     }
     
     
 
     /**
      * Forgot Password
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/forgotpassword")
     public ModelAndView forgotPasswordDisplay(
 	    		HttpServletRequest request,
 	    		@RequestParam(value="emailaddress", required=false) String emailaddress,    		
 	    		@RequestParam(value="submitted", required=false) String submitted
     		) throws UnknownHostException, MongoException
     {	
     	if(submitted == null){
      		//__if not yet added a chart return html form	    	
 			return new ModelAndView("jsp/user/forgotpassword");   	
     	}else{
     		return new ModelAndView("jsp/json/response", "json", personManager.forgotPassword(emailaddress));  	
     	}
     }       
     
     /**
      * Login
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/login")
     public ModelAndView loginDisplay(
 	    		HttpServletRequest request,
 	    		@RequestParam(value="emailaddress", required=false) String emailaddress,
 	    		@RequestParam(value="password", required=false) String password,	    		
 	    		@RequestParam(value="submitted", required=false) String submitted
     		) throws UnknownHostException, MongoException
     {	
     	if(submitted == null){
     		//__if not yet loggedin return html form	    	
 			return new ModelAndView("jsp/user/login");   	
     	}else{
     		//_ loggedin the user into the database and return a json response
     		return new ModelAndView("jsp/json/response", "json", personManager.login(emailaddress, password, request));
     	}
     }   
           
     @SuppressWarnings("unchecked")
 	@RequestMapping("/logout")
     public ModelAndView logoutDisplay(
 	    		HttpServletRequest request
     		) throws UnknownHostException, MongoException
     {	
     	List<DBObject> result = personManager.logout(request);//logged out user result
     		
     	List<DBObject> oldLoggedUser = (List<DBObject>) result.get(0).get("oldLoggedUser");//old user object    	
     	String oldLoggedUserName = (String) oldLoggedUser.get(0).get("username").toString();//old user name
 
     	return new ModelAndView("jsp/user/logout", "personName", oldLoggedUserName);
     }
     
     /**
      * Register
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/register")
     public ModelAndView registerDisplay(
     		HttpServletRequest request,
     		@RequestParam(value="realname", required=false) String realname,
     		@RequestParam(value="username", required=false) String username,
     		@RequestParam(value="emailaddress", required=false) String emailaddress,
     		@RequestParam(value="confirmemailaddress", required=false) String confirmemailaddress,
     		@RequestParam(value="password", required=false) String password,
     		@RequestParam(value="confirmpassword", required=false) String confirmpassword,
 
     		@RequestParam(value="birthyear", required=false) String birthyear,
     		@RequestParam(value="birthmonth", required=false) String birthmonth,
     		@RequestParam(value="birthday", required=false) String birthday,
 
     		@RequestParam(value="country", required=false) String country,
     		@RequestParam(value="whichscreenname", required=false) String whichscreenname,
     		@RequestParam(value="about", required=false) String about,
     		@RequestParam(value="submit", required=false) String submit,
 
     		@RequestParam(value="gender", required=false) String gender,
     		@RequestParam(value="ethnicity", required=false) String ethnicity,
     		@RequestParam(value="kindofrelationship", required=false) String kindofrelationship,
     		@RequestParam(value="bodytype", required=false) String bodytype,
     		@RequestParam(value="haircolor", required=false) String haircolor,
     		@RequestParam(value="eyecolor", required=false) String eyecolor,
     		@RequestParam(value="children", required=false) String children,
     		@RequestParam(value="education", required=false) String education,
     		@RequestParam(value="occupation", required=false) String occupation,
     		@RequestParam(value="languages", required=false) String[] languages,
 
     		@RequestParam(value="interests", required=false) String[] interests,
     		@RequestParam(value="interestknobs", required=false) Integer[] interestknobs,
 
     		@RequestParam(value="seekings", required=false) String[] seekings,
     		@RequestParam(value="seekingknobs", required=false) Integer[] seekingknobs,
 
     		@RequestParam(value="visitings", required=false) String[] visitings,
     		@RequestParam(value="visitingknobs", required=false) Integer[] visitingknobs,
 
     		@RequestParam(value="personality", required=false) Integer[] personality,
 
     		@RequestParam(value="latitude", required=false) String latitude,
     		@RequestParam(value="longitude", required=false) String longitude,
 
     		@RequestParam(value="goal1", required=false) String goal1,
     		@RequestParam(value="goal2", required=false) String goal2,
     		@RequestParam(value="goal3", required=false) String goal3,
 
     		//@RequestParam(value="lookingfor", required=false) String lookingfor,
 
     		@RequestParam(value="file", required=false) MultipartFile[] file,
 
     		@RequestParam(value="submitted", required=false) String submitted
     		) throws UnknownHostException, MongoException
     {	
     	//_new person
     	Person person = new Person();
 
     	person.setRealname(realname);
     	person.setUsername(username);
     	person.setEmailaddress(emailaddress);
     	person.setConfirmemailaddress(confirmemailaddress);
     	person.setPassword(password);
     	person.setConfirmpassword(confirmpassword);
     	person.setWhichscreenname(whichscreenname);
     	person.setBirthyear(birthyear);
     	person.setBirthmonth(birthmonth);
     	person.setBirthday(birthday);
     	person.setAbout(about);
     	person.setCountry(country);
     	person.setGender(gender);
     	person.setEthnicity(ethnicity);
     	person.setKindofrelationship(kindofrelationship);
     	person.setBodytype(bodytype);
     	person.setHaircolor(haircolor);
     	person.setEyecolor(eyecolor);
     	person.setChildren(children);
     	person.setEducation(education);
     	person.setOccupation(occupation);
     	person.setLongitude(longitude);
     	person.setLatitude(latitude);
     	person.setLanguages(languages);
     	person.setInterests(interests);
     	person.setInterestknobs(interestknobs);
     	person.setSeekings(seekings);
     	person.setSeekingknobs(seekingknobs);
     	person.setVisitings(visitings);
     	person.setVisitingknobs(visitingknobs);
     	person.setGoal1(goal1);
     	person.setGoal2(goal2);
     	person.setGoal3(goal3);
     	
     	BasicDBObject personaltraits = new BasicDBObject();
 
     	if(personality != null){
 		    /*add to new collection personality*/
 			String[] personalTypes ={"confidence","reasoning","emotion","daring","attachment","sensitivity","comedy"};
 			Integer j = 0;
 			for(Integer traits : personality)
 	    	{
 	    		personaltraits.put(personalTypes[j], traits);
 	    		j++;
 	    	}	
     	}
     	person.setPersonality(personaltraits);
     	
     	if(submitted == null){
     		//__if not yet registered return html form
 	    	Map<String,String> countryList = CommonUtils.getCountries();
 	    	String countriesCommonKey = "GBR"; //United Kingdom
 	
 	    	Map<String,String> ethnicityList = CommonUtils.getEthnicity();
 	    	String ethnicityCommonKey = "0"; //Caucasian
 	
 	    	request.setAttribute("countryCommonKey", countriesCommonKey);
 	    	request.setAttribute("countryList", countryList);
 	
 	    	request.setAttribute("ethnicityCommonKey", ethnicityCommonKey);
 	    	request.setAttribute("ethnicityList", ethnicityList);
 	    	
 			return new ModelAndView("jsp/user/register");   	
     	}else{
     		//_ register the user into the database and return a json response
         	return new ModelAndView("jsp/json/response", "json", personManager.registerUser(person));  	
     	}    	
     }
     
     
     /**
      * Send Private Message
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/sendPrivateMessages")
     public ModelAndView sendPrivateMessages(
     		HttpServletRequest request,
     		@RequestParam(value="senderUid", required=false) String senderUid,
     		@RequestParam(value="recepientUid", required=false) String recepientUid,
     		@RequestParam(value="message", required=false) String message,
     		@RequestParam(value="submitted", required=false) String submitted
     		) throws UnknownHostException, MongoException
     {    	
     	
     	PrivateMessage privatemessage = new PrivateMessage();
 			privatemessage.setRecepientUserId(recepientUid);
 			privatemessage.setSenderUserId(senderUid);
 			privatemessage.setMessage(message);	
 			
     	if(submitted == null){
     		//__if not yet added a chart return html form	    	
 			return new ModelAndView("jsp/user/send_private_message");   	
     	}else{
     		return new ModelAndView("jsp/json/response", "json", PrivateMessageDao.sendPrivateMessage(privatemessage));  	
     	}       	
     }
     
     /**
      * View Private Message
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/viewPrivateMessages")
     public ModelAndView viewPrivateMessages(
     		HttpServletRequest request
     		) throws UnknownHostException, MongoException
     {    	
     	String loggedUser = "1";//logged in user
     	
 		PrivateMessage privatemessage = new PrivateMessage();		
 			
 			privatemessage.setRecepientUserId(loggedUser);
 		List<DBObject> inboxMessages = PrivateMessageDao.getInboxPrivateMessage(privatemessage);
 			
 			privatemessage.setSenderUserId(loggedUser);
 		List<DBObject> sentMessages = PrivateMessageDao.getSentPrivateMessage(privatemessage);
 
 	    BasicDBObject privates = new BasicDBObject();
 			privates.put("inbox", inboxMessages.get(0).get("results"));
 			privates.put("sent", sentMessages.get(0).get("results"));
 	    	
     	return new ModelAndView("jsp/user/view_private_message", "privatemessages", privates);      	
     }
     
 	/**
 	 * getPrivateMessages
 	 * @return 
 	 * @throws MongoException
 	 * @throws UnknownHostException
 	*/
     /*
 	@RequestMapping("/getPrivateMessages")
 	public ModelAndView getPrivateMessages(
 			HttpServletRequest request,
 			@RequestParam(value="recepientUid", required=false) String recepientUid
 			) throws UnknownHostException, MongoException
 	{    	
 		PrivateMessage privatemessage = new PrivateMessage();
 			privatemessage.setRecepientUserId(recepientUid);
 		
 		return new ModelAndView("jsp/json/response", "json", PrivateMessageDao.getPrivateMessage(privatemessage));  	
 	}
     */
         
     
     /*
      * Member List
      * Displays All Users
     */
     @RequestMapping("/members")
     public ModelAndView memberList(
 	    		HttpServletRequest request
     		) throws UnknownHostException, MongoException
     {
     	//ServiceSerlvet.appendSesssion(request);
     	//get search ALL users
     	BasicDBObject searchQuery = new BasicDBObject();
     	//skip 0
     	//limit 130
     	List<DBObject> dataresponse = PersonDao.searchUsers(searchQuery, 0, 130, "myCollection");
     	
     	request.setAttribute("page", "members");
     	return new ModelAndView("jsp/memberlist", "response", dataresponse);
     }
     
     /*
      * Venue Form
     */
     @RequestMapping("/venueform")
     public ModelAndView venueForm(
 	    		HttpServletRequest request
     		) throws UnknownHostException, MongoException
     {
     	return new ModelAndView("jsp/venueform");
     }    
     
     //common site pages
  
     /**
      * Home
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */  
     @RequestMapping(value = "/", method = RequestMethod.GET)
     public ModelAndView home(
     		HttpServletRequest request
 		) throws UnknownHostException, MongoException
 	{
 		SessionController.isSession(request);//_check if in session and append isSession boolean flag
		request.setAttribute("page", "home");
 		return new ModelAndView("welcome");
 	}
     
     /**
      * Privacy
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/privacy")
     public ModelAndView privacy(
 	    		HttpServletRequest request
     	) throws UnknownHostException, MongoException
     {	
     	SessionController.isSession(request);//_check if in session and append isSession boolean flag
     	
 		return new ModelAndView("jsp/privacy");
     }
    
     /**
      * Who We Are
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/whoWeAre")
     public ModelAndView whoWeAre(
 	    		HttpServletRequest request
     		) throws UnknownHostException, MongoException
     {	
     	SessionController.isSession(request);//_check if in session and append isSession boolean flag
     	
 		return new ModelAndView("jsp/who_we_are");
     }
 
     /**
      * Instructions
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping("/instructions")
     public ModelAndView instructions(
 	    		HttpServletRequest request
     	) throws UnknownHostException, MongoException
     {	
     	SessionController.isSession(request);//_check if in session and append isSession boolean flag
     	
     	return new ModelAndView("jsp/instructions");
     }
     
     /**
      * User
      * @return 
      * @throws MongoException
      * @throws UnknownHostException
     */
     @RequestMapping(method=RequestMethod.GET, value={"/user","/user/{id}"})
     public ModelAndView profileDisplay(
     		HttpServletRequest request,
     		@RequestParam(value="id", required=false) String id
     ) throws UnknownHostException, MongoException {
     	SessionController.isSession(request);//_check if in session and append isSession boolean flag
     	
     	request.setAttribute("page", "user");
     	
     	//get search ALL users
     	BasicDBObject searchQuery = new BasicDBObject();
     		searchQuery.put("_id", new ObjectId(id));
     	
     	//skip 0
     	//limit 1   		
     	List<DBObject> searchResponse = PersonDao.searchUsers(searchQuery, 0, 1, "myCollection");
 
     	//append actual age to the returned user object.
     	DBObject newInformation = new BasicDBObject();
 
     	String birthdate = (String) searchResponse.get(0).get("birthdate");
 
     	Integer ageInYears = PersonDao.getAge(birthdate);
     	newInformation.put("ageInYears", ageInYears);
 
     	/*
     	HashMap<Integer,Object> gallery = PersonController.getGallery(
     			id,
     			false
     	);
 
     	newInformation.put("gallery", gallery);
 
     	Integer countGallery = gallery.size();
     	newInformation.put("countGallery", countGallery);
 		*/
 
     	List<DBObject> galleryResponse = ImageDao.getUsersImages(id);
 	   	newInformation.put("galleryResponse", galleryResponse); 	
     	
     	searchResponse.add(newInformation);
 
     	Map<String,String> countryList = CommonUtils.getCountries();
     	Map<String,String> genderList = CommonUtils.getGender();
     	Map<String,String> ethnicityList = CommonUtils.getEthnicity();
 
     	request.setAttribute("countryList", countryList);
     	request.setAttribute("genderList", genderList);
     	request.setAttribute("ethnicityList", ethnicityList);
 
 		return new ModelAndView("jsp/user", "people", searchResponse);
     }
     
     /*
      * Schedule a date
     */
     @RequestMapping("/scheduledate")
     public ModelAndView scheduleDateDisplay(
 	    		HttpServletRequest request
     		) throws UnknownHostException, MongoException
     {
     	request.setAttribute("page", "scheduledate");
     	
     	String json = null;
     	return new ModelAndView("jsp/scheduledate", "response", json);
     }
   
 
     /*
      * Api
     */
     @RequestMapping(method=RequestMethod.GET, value={"/api","/api/{servicerequest}/{id}/{chartname}/{skips}/{limits}"})
     public ModelAndView apiService(
     		HttpServletRequest request,
     		@RequestParam(value="id", required=false) String id,
     		@RequestParam(value="servicerequest", required=false) String servicerequest,
     		@RequestParam(value="chartname", required=false) String chartname,
     		@RequestParam(value="skips", required=false) Integer skips,
     		@RequestParam(value="limits", required=false) Integer limits,
     		@RequestParam(value="bodytype", required=false) String bodytype,
     		@RequestParam(value="haircolor", required=false) String haircolor,
     		@RequestParam(value="eyecolor", required=false) String eyecolor,
     		@RequestParam(value="ethnicity", required=false) String ethnicity,
     		@RequestParam(value="gender", required=false) String gender
     ) throws UnknownHostException, MongoException {
     	//ServiceSerlvet.appendSesssion(request);
        	
     	List<DBObject> json = null;
     	
     	if(servicerequest.equals("getMembers")){
     		//request personality data    	
     		//id
     		//Integer skips = 0;
     		//Integer limits = 20;
     		BasicDBObject filter = new BasicDBObject();  		
     			//filter.put("isloggedon", "1");
     		
     			if(id != null){
     				filter.put("_id", new ObjectId(id));
     			}
 
     			if(bodytype != null){
     				filter.put("bodytype", bodytype);
     			}
     			if(haircolor != null){
     				filter.put("haircolor", haircolor);
     			}
     			if(eyecolor != null){
     				filter.put("eyecolor", eyecolor);
     			}    			
     			if(ethnicity != null){
     				filter.put("ethnicity", ethnicity);
     			}
     			if(gender != null){
     				filter.put("gender", gender);
     			}
     			
     		json = PersonDao.getMembers(skips, limits, filter);
     	}
     	else if(servicerequest.equals("getPersonality")){
     		//request personality data    	
     		//id
     		json = PersonDao.getPersonality(id);
     	}
     	else if(servicerequest.equals("getInterests")){
 			//request interest data 
     		//chartname, visiting, interests
     		
     		System.out.println(id);
     		System.out.println(chartname);
     		//id
 		    	Interests obj = new Interests();
 			    	obj.setUserId(id);
 			    	obj.setName(chartname);
     		json = InterestDao.getInterest(obj);
 		}
     	
 		return new ModelAndView("jsp/json/response", "json", json);
     }
     
    
     /*
      * getInterestJson
     */
     @RequestMapping("/getInterestJson")
     public ModelAndView getInterestJson(
 	    		HttpServletRequest request
     		) throws UnknownHostException, MongoException
     {
     	String json = null;
     	return new ModelAndView("jsp/json/interest", "response", json);
     }
     
     
     
     /*
      * getPlaceJson
     */
     @RequestMapping("/getPlaceJson")
     public ModelAndView getPlaceJson(
 	    		HttpServletRequest request
     		) throws UnknownHostException, MongoException
     {
     	String json = null;
     	return new ModelAndView("jsp/json/places", "response", json);
     }
 
     /*
      * getSeekingJson
     */
     @RequestMapping("/getSeekingJson")
     public ModelAndView getSeekingJson(
 	    		HttpServletRequest request
     		) throws UnknownHostException, MongoException
     {
     	String json = null;
     	return new ModelAndView("jsp/json/seeking", "response", json);
     }
 }
