 package friendzone.elec3609.controller;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import friendzone.elec3609.service.DatabaseHandler;
 import friendzone.elec3609.model.Role;
 import friendzone.elec3609.model.SocialMedia;
 import friendzone.elec3609.model.Student;
 import friendzone.elec3609.model.StudyLevel;
 import friendzone.elec3609.model.ProgrammingLanguage;
 
 @Controller
 @SessionAttributes({"student", "dbHandler"})
 public class ProfileController {
 
 	final static DatabaseHandler dbHandler = DatabaseHandler.getInstance();
 	
 	//New Dummy data just for testing
 
 //Student student = new Student("123456789", "abcd1234", "password", "aFirstName", "aLastname", "abcd1234@uni.sydney.edu.au", "0412345678", StudyLevel.UNDERGRADUATE, true, new ProgrammingLanguage[]{ProgrammingLanguage.C, ProgrammingLanguage.CPLUSPLUS});
 	
 	//Student student = new Student("123456789", "abcd1234", "password", "aFirstName", "aLastname", "abcd1234@uni.sydney.edu.au", "0412345678", StudyLevel.UNDERGRADUATE, true, new ProgrammingLanguage[]{ProgrammingLanguage.C, ProgrammingLanguage.CPLUSPLUS});
 	
	//Student student = dbHandler.getStudent("123456789");
 	
 	@RequestMapping("/profile")
 	public String getEnums(Map<String, Object> map, @ModelAttribute("student") Student student){
 		//More Dummy Data
 //
 //		student.setPreferredRole(Role.PROGRAMMER);
 //		student.setExperience("I have experience in a lot of useful things");
 //		student.setFirstSocialMedia(SocialMedia.Provider.FACEBOOK, "http://www.facebook.com/abcd1234");
 //		student.setSecondSocialMedia(SocialMedia.Provider.GOOGLEPLUS, "http://plus.google.com/abcd1234");
 		
 		//Converts the array of Programming Language array to a 
 		//List of strings
 		ProgrammingLanguage[] languages = student.getLanguages();
 		List<String> langStrings = new ArrayList<String>();
 		for (ProgrammingLanguage language : languages){
 			langStrings.add(language.toString());
 		}
 		
 		//Data to be put on the profile page
 		map.put("SID", student.getSID());
 		map.put("unikey", student.getUnikey());
 		map.put("firstName", student.getFirstName());
 		map.put("lastName", student.getLastName());
 		map.put("primaryEmail", student.getPrimaryEmail());
 		map.put("secondaryEmail", student.getSecondaryEmail());
 		map.put("mobile", student.getMobile());
 		map.put("studyLevels", student.getStudyLevel());
 		map.put("ESL", student.getESL());
 		map.put("languages", langStrings);
 		map.put("preferredRole", student.getPreferredRole());
 		map.put("experience", student.getExperience());
 		map.put("firstSocialMedia", student.getFirstSocialMedia());
 		map.put("secondSocialMedia", student.getSecondSocialMedia());
 		map.put("degree", student.getCourse());
 
 
 		
 		return "profile";
 		
 		
 	}
 	
 	//
 	@RequestMapping("/profile/edit")
	public String editProfile(Map<String, Object> map, @ModelAttribute("student") Student student){
 		map.put("programmingLanguages", ProgrammingLanguage.getNames());
 		map.put("roles", Role.getNames());	
 		map.put("socialMediaProviders", SocialMedia.Provider.getNames());
 		map.put("studyLevels", StudyLevel.getNames());
 		return "edit";
 	}
 	
 	@RequestMapping("/profile/editComplete")
	public String changeProfile(HttpServletRequest request, @ModelAttribute("student") Student student){
 		
 		//Converts the string of languages back to a programming language
 		//And then sets it
 		//Works :D
 		if(request.getParameterMap().containsKey("languages")){
 			String[] languageStrings = request.getParameterValues("languages");
 			ProgrammingLanguage[] languages = new ProgrammingLanguage[languageStrings.length];
 			for (int i = 0; i != languageStrings.length; ++i){
 				languages[i] = ProgrammingLanguage.findMatch(languageStrings[i]);
 			}
 			student.setProgrammingLanguages(languages);
 		}
 
 		
 		//Sets the primary email
 		//Works :D
 		if(request.getParameterMap().containsKey("primaryEmail"))
 		{
 			//checks it is not empty
 			if(request.getParameter("primaryEmail") != "")
 				student.setPrimaryEmail(request.getParameter("primaryEmail"));
 		}
 		
 		//Sets the preferred Role
 		//Works :D
 		if(request.getParameterMap().containsKey("preferredRole"))
 		{
 			//checks it is not empty
 			//if(request.getParameter("preferredRole") != "")
 				student.setPreferredRole(Role.findMatch(request.getParameter("preferredRole")));
 		}
 		
 		//Sets the mobile
 		//Works :D
 		if(request.getParameterMap().containsKey("mobile"))
 		{
 			//checks it is not empty
 			if(request.getParameter("mobile") != "")
 				student.setMobile(request.getParameter("mobile"));
 		}
 		
 		//Sets the StudyLevel
 		//Works :D
 		if(request.getParameterMap().containsKey("studyLevel"))
 		{
 			student.setStudyLevel( StudyLevel.findMatch(request.getParameter("studyLevel")));
 		}
 		
 		//sets the ESL is exists
 		if(request.getParameterMap().containsKey("esl"))
 		{
 			student.setESL(Boolean.parseBoolean(request.getParameter("esl")));
 		}
 		
 		//Sets the experience
 		if(request.getParameterMap().containsKey("experience"))
 		{
 			//checks the box was edited
 			if(request.getParameter("experience") != "Enter a brief description of your work and project experience (up to 200 characters).")
 				student.setExperience(request.getParameter("experience"));
 		}
 		
 		//Sets the course (degree)
 		if(request.getParameterMap().containsKey("course"))
 		{
 			//checks it is not empty
 			if(request.getParameter("course") != "")
 				student.setCourse(request.getParameter("course"));
 		}
 		
 		//Sets the secondaryEmail
 		if(request.getParameterMap().containsKey("secondaryEmail"))
 		{
 			//checks it is not empty
 			if(request.getParameter("secondaryEmail") != "")
 				student.setSecondaryEmail(request.getParameter("secondaryEmail"));
 		}
 		
 		//Sets the first social media
 		if(request.getParameterMap().containsKey("socialMedia1Value") && request.getParameterMap().containsKey("socialMedia1Provider"))
 		{
 			student.setFirstSocialMedia(SocialMedia.Provider.findMatch(request.getParameter("socialMedia1Provider")), request.getParameter("socialMedia1Value"));
 		}
 		
 		//Sets the second social media
 		if(request.getParameterMap().containsKey("socialMedia2Value") && request.getParameterMap().containsKey("socialMedia2Provider"))
 		{
 			student.setSecondSocialMedia(SocialMedia.Provider.findMatch(request.getParameter("socialMedia2Provider")), request.getParameter("socialMedia2Value"));
 		}
 		
 		//redirect to profile
 		return "redirect:/profile/";
 	}
 
 }
