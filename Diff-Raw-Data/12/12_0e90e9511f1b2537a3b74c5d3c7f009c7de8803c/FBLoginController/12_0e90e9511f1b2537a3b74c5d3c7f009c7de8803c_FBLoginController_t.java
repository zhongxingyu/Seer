 package edu.unsw.cse.comp9323.group1.controllers;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URISyntaxException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.http.HttpException;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.restfb.Connection;
 import com.restfb.DefaultFacebookClient;
 import com.restfb.Facebook;
 import com.restfb.FacebookClient;
 import com.restfb.types.NamedFacebookType;
 import com.restfb.types.Page;
 import com.restfb.types.User;
 import com.restfb.types.User.Education;
 
 import edu.unsw.cse.comp9323.group1.DAOs.CourseDAO;
 import edu.unsw.cse.comp9323.group1.DAOs.RestClient;
 import edu.unsw.cse.comp9323.group1.Tools.InitializeREST;
 import edu.unsw.cse.comp9323.group1.Tools.RestDelete;
 import edu.unsw.cse.comp9323.group1.Tools.RestGet;
 import edu.unsw.cse.comp9323.group1.Tools.RestPost;
 import edu.unsw.cse.comp9323.group1.models.Course;
 import edu.unsw.cse.comp9323.group1.models.StudentConcentrationModel;
 import edu.unsw.cse.comp9323.group1.models.StudentEduHistoryModel;
 import edu.unsw.cse.comp9323.group1.models.StudentModel;
 import edu.unsw.cse.comp9323.group1.models.fqlUser;
 
 @Controller
 @RequestMapping("/FBLogin")
 @SessionAttributes({"review", "externalReview"})
 public class FBLoginController {
 	@RequestMapping(value = "/{accesstoken}", method = RequestMethod.GET)
 	public String getStudentDetail(@PathVariable String accesstoken, ModelMap model) {
  
 		
 		//model.addAttribute("accesstoken", accesstoken);
 		
 		FacebookClient facebookClient = new DefaultFacebookClient(accesstoken);
 		System.out.println(accesstoken);
 		User user = facebookClient.fetchObject("me", User.class);
 		
 		//Alternative by using Facebook Query Language (FQL)
 		
 		/*String query = "SELECT uid,username,first_name,middle_name,last_name,sex,interests FROM user WHERE uid = me()";
 		System.out.println(query);
 		List<fqlUser> users = facebookClient.executeFqlQuery(query, fqlUser.class);
 		fqlUser user = users.get(0);*/
 		
 		//Another alternative is using fetchConnection
 		/* Connection<Education> myEducations = facebookClient.fetchConnection("me?fields=education", Education.class); 
 		List<Education> lstEdux = myEducations.getData();*/
 		
 		//System.out.println("User name: " + user.getFirstName());
 		//model.addAttribute("Uname", user.getName());
 		
 		
 		
 		InitializeREST initREST = new InitializeREST();
 		try {
 			initREST.Init();
 			
 			
 			//space character translated into %20
 			String newRestUri = initREST.getRestUri() + "/query/?q=" + URLEncoder.encode("SELECT ID__c, FIrstName__c, MiddleName__c, LastName__c, Gender__c FROM StudentAccount__c WHERE ID__c = ","UTF-8") + "\'"+user.getId()+"\'";//"/sobjects/"+tableName+"/describe/";
 			System.out.println(newRestUri);
 			
 			
     	    
     	    RestGet restGet = new RestGet();
 			String result = restGet.getUsingQuery(newRestUri, initREST.getOauthHeader());
 			System.out.println(result);
 			JsonParser parser = new JsonParser();
 			JsonElement jsonElement = parser.parse(result);
 			JsonObject  jobject = jsonElement.getAsJsonObject();
 			int total = jobject.get("totalSize").getAsInt();
     	    System.out.println("number of records Found :" + total);
     	    
     	    //if no records found then insert new record
     	   
     	    StudentModel student = new StudentModel();
     	    
     	    //for(String X : user.getInterestedIn()){
 	    		//System.out.println("interest : "+X);
 	    	//}
     	    if(total == 0){
     	    	
     	    	
     	    	//load to studentModel
     	    	student.setFirstName(user.getFirstName());
     	    	student.setMiddleName(user.getMiddleName());
     	    	student.setLastName(user.getLastName());
     		
     	    	student.setGender(user.getGender());
     	    	student.setId(user.getId());
     	    	student.setInterests(user.getInterestedIn());
     		
     	    	
     		
     	    	List<Education> lstEdu = user.getEducation();
     		
     	    	System.out.println("Type Of Education : " + lstEdu.get(0).getType());
     	
     		
     	    	List<StudentEduHistoryModel> lstStuHistory = new ArrayList<StudentEduHistoryModel>();
     		
     	    	for(Education edu : lstEdu){
     	    		StudentEduHistoryModel StuHistory = new StudentEduHistoryModel();
     	    		
     	    		if(edu.getDegree()!=null){
     	    			StuHistory.setDegree(edu.getDegree().getName());}
     	    			StuHistory.setSchool(edu.getSchool().getName());
     	    			StuHistory.setType(edu.getType());
     	    		
     	    		if(edu.getYear()!=null){
     	    			StuHistory.setYear(edu.getYear().getName());
     				}
     	    		
     	    		List<StudentConcentrationModel> lstConcentration = new ArrayList<StudentConcentrationModel>();
     	    		
     	    		for(NamedFacebookType x : edu.getConcentration()){
     	    			StudentConcentrationModel concentration = new StudentConcentrationModel();
     	    			concentration.setId(x.getId());
     	    			concentration.setName(x.getName());
     	    			lstConcentration.add(concentration);
     	    		}
     			
     	    		StuHistory.setLstConcentration(lstConcentration);
     	    		lstStuHistory.add(StuHistory);
     			
     	    	}
     	    	
     	    	student.setEducations(lstStuHistory);
     	    	model.addAttribute("student", student);
     	    	
     	    	//insert to database.com
     	    	JsonObject newStudentJSONObj = new JsonObject();
     	    	newStudentJSONObj.addProperty("ID__c", student.getId());
     	    	newStudentJSONObj.addProperty("FIrstName__c", student.getFirstName());
     	    	newStudentJSONObj.addProperty("Gender__c", student.getGender());
     	    	newStudentJSONObj.addProperty("LastName__c", student.getLastName());
     	    	newStudentJSONObj.addProperty("MiddleName__c", student.getMiddleName());
     	    	//student.getInterests();
     	    	//student.getEducations();
     	    	
 //    	    	newRestUri = initREST.getRestUri() +"/sobjects/StudentAccount__c/";
 //    	    	RestPost restPost = new RestPost();
 //    	    	result = restPost.post(newRestUri,initREST.getOauthHeader(), newStudentJSONObj.toString());//  restPost(restUri + "/sobjects/course_detail__c/", newCourse.toString());
 //    	    	System.out.println("Insert Result : \n" + result);
     	    	
     	    	RestClient client = new RestClient();
     	    	client.oauth2Login( client.getUserCredentials());
     	    	String response = client.restPost("/sobjects/StudentAccount__c/", newStudentJSONObj.toString());
     	    	
     		
     	    }
     	    else if(total > 0){
     	    	/*System.out.println("Number of record before deletion : " + total);
     	    	JsonArray jarray = jobject.getAsJsonArray("records");
     	    	JsonObject jobjectFirstRecord = jarray.get(0).getAsJsonObject();
     	    	String url = jobjectFirstRecord.getAsJsonObject("attributes").get("url").getAsString();
          	    System.out.println("url : " + url);
          	    String[] urlArr = url.split("/");
          	    
          	   String id = Arrays.asList(urlArr).get(urlArr.length-1);
          	   System.out.println("id : " + id); 		
          	   System.out.println("rest url:" + initREST.getRestUri());
     	    	
          	   newRestUri = initREST.getRestUri() +"/sobjects/StudentAccount__c/" +id;
          	   System.out.println(newRestUri);
     	    	
          	   RestDelete restDelete = new RestDelete();
     	    	
          	   restDelete.delete(newRestUri,initREST.getOauthHeader());
     	    	
          	   System.out.println("Delete Success !!");*/
     	    	
     	    	
     	    	//load data from Database.com to studentModel
     	    	
     	    	JsonArray jarray = jobject.getAsJsonArray("records");
     	    	System.out.println("size of Array :" + jarray.size());
     	    	//System.out.println(jarray.get(0).toString());
         	    JsonObject jobjectFirstRecord = jarray.get(0).getAsJsonObject();
         	    
        	    student.setId(jobjectFirstRecord.get("ID__c").toString().replaceAll("\"", ""));
         	  
         	    student.setFirstName(jobjectFirstRecord.get("FIrstName__c").toString());
         	    student.setMiddleName(jobjectFirstRecord.get("MiddleName__c").toString());
     	    	student.setLastName(jobjectFirstRecord.get("LastName__c").toString());
     	    	student.setGender(jobjectFirstRecord.get("Gender__c").toString());
     	    	//student.setInterests(user.getInterestedIn());
     	    	System.out.println("Data Load Success !!");
     	    	model.addAttribute("student", student);
     	    	
     	    	CourseDAO crsDAO = new CourseDAO();
     	    	ArrayList<Course>  x = new ArrayList<Course>(crsDAO.getAllIDNameCourses());
     	    	model.addAttribute("courses", crsDAO.getAllIDNameCourses());
     	    	
     	    }
     	    
 			//System.out.println(result);
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (HttpException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "studentHome";
 		
 		
  
 	}
  
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String getDefaultLoginPage(ModelMap model) {
 		return "FBLogin";
  
 	}
 	
 	@RequestMapping(value = "/FBLoginTest", method = RequestMethod.GET)
 	public String FBtest(ModelMap model) {
 		//System.out.println(surveyId);
 		return "FBLoginTest";
  
 	}
 	
 	
 }
 
 
