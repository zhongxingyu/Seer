 package friendzone.elec3609.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import friendzone.elec3609.service.DatabaseHandler;
 import friendzone.elec3609.model.Role;
 import friendzone.elec3609.model.Student;
 import friendzone.elec3609.model.StudyLevel;
 import friendzone.elec3609.model.ProgrammingLanguage;
 
 @Controller
 public class ProfileController {
 
 	@Autowired
 	DatabaseHandler dbHandler;
 	
 	//New Dummy data just for testing
 	Student student = new Student("123456789", "abcd1234", "aFirstName", "aLastname", "abcd1234@uni.sydney.edu.au", "0412345678", StudyLevel.UNDERGRADUATE, true);
 	
 
 	
 	@RequestMapping("/profile")
 	public String getEnums(Map<String, Object> map){
 		//More Dummy Data
 		ProgrammingLanguage[] planguages = new ProgrammingLanguage[]{ProgrammingLanguage.C, ProgrammingLanguage.CPLUSPLUS};
 		student.setLanguages( planguages );
 		student.setPreferredRole(Role.PROGRAMMER);
 		
		//Converts the array of Prorgramming Language array to a 
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
 		
 		return "profile";
 	}
 	
 
 }
