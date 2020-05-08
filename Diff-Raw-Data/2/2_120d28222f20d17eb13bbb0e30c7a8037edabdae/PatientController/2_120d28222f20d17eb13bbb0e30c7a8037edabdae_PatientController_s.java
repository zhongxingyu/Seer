 package com.secondopinion.common.controller;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletRequest;
 
 import net.tanesha.recaptcha.ReCaptchaImpl;
 import net.tanesha.recaptcha.ReCaptchaResponse;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.secondopinion.common.model.FileUpload;
 import com.secondopinion.common.model.Patient;
 import com.secondopinion.common.model.PatientAllergy;
 import com.secondopinion.common.model.PatientFile;
 import com.secondopinion.common.model.PatientMedication;
 import com.secondopinion.common.model.PatientProcedure;
 import com.secondopinion.common.model.PatientSymptom;
 import com.secondopinion.common.model.User;
 import com.secondopinion.common.service.PatientService;
 import com.secondopinion.common.service.UserService;
 
 
 /**
  * This is the core of the TravelLog functionality.  It's a Spring controller implemented
  * using annotations.  Most methods for loading and storing journals, entries, comments and photos
  * are initiated in this class.
  */
 @Controller
 public class PatientController {
 
     private static final Logger logger=Logger.getLogger(PatientController.class.getName());
     
     @Autowired
     UserService userService;
     
     @Autowired
     PatientService patientService;
     
     @RequestMapping(value = "/patientsignup.do", method = RequestMethod.POST)
     public ModelAndView doPatientAccount (ModelMap map,
       @ModelAttribute("addPatientForm") Patient patient,			
 	  BindingResult result,
       ServletRequest request,
       Model model,
       @RequestParam("email") String email,
       @RequestParam("pwd") String password,
       @RequestParam("fullname") String fullName,
       @RequestParam("dateofbirth") String dateOfBirth,
       @RequestParam("gender") String gender,
       @RequestParam("location") String location,
       @RequestParam("recaptcha_challenge_field") String challenge,
       @RequestParam("recaptcha_response_field") String response) throws ParseException {
     	
     	String remoteAddr = request.getRemoteAddr();
         ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
         reCaptcha.setPrivateKey("6LfARuoSAAAAAKoszbmVYYkidNNvv-3kWQhcghpd");
         
         ReCaptchaResponse reCaptchaResponse = reCaptcha.checkAnswer(remoteAddr, challenge, response);
 
         if (!reCaptchaResponse.isValid()) {
         	System.out.println("Captcha Answer is wrong");
         	//result.rejectValue("invalidRecaptcha", "invalid.captcha");
 			model.addAttribute("invalidRecaptcha", true);
 			ModelAndView modelView = new ModelAndView("redirect:patientregistration.do");
 			return modelView;
         	//FieldError fieldError = new FieldError(objectName, field, defaultMessage)
         } 
         
         System.out.println("Captcha Answer is correct");
         //System.out.println("Captcha Answer is correct");
         User user = new User( -1, email, password, true);
         user = userService.createUser(user);
             
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
         Date dob = dateFormatter.parse(dateOfBirth);
         patient = new Patient(-1, -1, fullName, dob, gender, location);
         patientService.createPatient(user, patient);
         return new ModelAndView("redirect:welcome.do");        	
         
     }
     
     @RequestMapping(value="/patientprofile.do", method={RequestMethod.GET})
     public void doPatientProfile (ModelMap model) {
     	
 		Patient patient = patientService.getCurrentPatient();
 		model.addAttribute("patient", patient);
     }
     
     @RequestMapping(value="/patientbasicinfo.do", method={RequestMethod.GET})
     public void doPatientBasicInfo (ModelMap model) {
     	
 		Patient patient = patientService.getCurrentPatient();
 		model.addAttribute("patient", patient);
     }
     
     @RequestMapping(value="/updatepatientbasicinfo.do", method=RequestMethod.POST)
     public ModelAndView doUpdatePatientBasicInfo (ModelMap model,
     	  @RequestParam("fullname") String fullName,
 	      @RequestParam("dateofbirth") String dateOfBirth,
 	      @RequestParam("gender") String gender,
 	      @RequestParam("location") String location) throws ParseException {
     	
 		Patient patient = patientService.getCurrentPatient();
 		
 		SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
         Date dob = dateFormatter.parse(dateOfBirth);
         
 		patient.setName(fullName);
 		patient.setDateOfBirth(dob);
 		patient.setGender(gender);
 		patient.setLocation(location);
 		
 		patientService.updatePatient(patient);
 		
 		return new ModelAndView("redirect:patientbasicinfo.do");
     }
     
     @RequestMapping(value="/patientfileupload.do", method={RequestMethod.GET})
 	public void doPatientFileUpload(ModelMap model) {
     	Patient patient = patientService.getCurrentPatient();
     	List<PatientFile> patientFiles = patientService.getPatientFiles(patient);
 		model.addAttribute("patientFiles", patientFiles);
 		model.addAttribute("patient", patient);
     }
 
 	@RequestMapping(value="/patientnewfileupload.do", method={RequestMethod.POST})
 	public ModelAndView doPatientNewFileUpload(ModelMap model, @RequestParam("file_description") String fileDescription,
 									   @RequestParam("file") MultipartFile file) throws IOException {
 		Patient patient = patientService.getCurrentPatient();
 		System.out.println("Patient: " + patient.getName() + " uploaded a file.");
 		System.out.println("Description: " + fileDescription);
 		System.out.println("Original file name: " + file.getOriginalFilename());
 		
 		FileUpload fileUpload = new FileUpload(file, fileDescription);
 		patientService.addPatientFile(patient, fileUpload);
 		return new ModelAndView("redirect:patientfileupload.do");
 	}
 	
 	@RequestMapping(value="/removepatientfile.do", method=RequestMethod.POST)
     public ModelAndView doRemovePatientFile (ModelMap model,
     	  @RequestParam("fileId") int fileId) throws ParseException {
 
 		patientService.removePatientFile(fileId);
 		
 		return new ModelAndView("redirect:patientfileupload.do");
     }
     
     @RequestMapping(value="/patientmedication.do", method={RequestMethod.GET})
     public void doPatientMedication (ModelMap model) {
     	Patient patient = patientService.getCurrentPatient();
     	List<PatientMedication> patientMedications = patientService.getPatientMedations(patient);
     	
 		model.addAttribute("patient", patient);
 		model.addAttribute("patientMedications", patientMedications);
     }
     
     @RequestMapping(value="/addpatientmedication.do", method=RequestMethod.POST)
     public ModelAndView doAddPatientMedication (ModelMap model,
     	  @RequestParam("medication") String medicationName,
 	      @RequestParam("notes") String notes) throws ParseException {
     	
 		Patient patient = patientService.getCurrentPatient();
 		
 		PatientMedication patientMedication = new PatientMedication(-1, -1, medicationName, notes);
 		
 		patientService.addPatientMedication(patient, patientMedication);
 		
 		return new ModelAndView("redirect:patientmedication.do");
     }
     
     @RequestMapping(value="/removepatientmedication.do", method=RequestMethod.POST)
     public ModelAndView doRemovePatientMedication (ModelMap model,
     	  @RequestParam("medicationId") int medicationId) throws ParseException {
 
 		patientService.removePatientMedication(medicationId);
 		
 		return new ModelAndView("redirect:patientmedication.do");
     }
     
     @RequestMapping(value="/patientsymptoms.do", method={RequestMethod.GET})
     public void doPatientSymptoms (ModelMap model) {
     	Patient patient = patientService.getCurrentPatient();
     	List<PatientSymptom> patientSymptoms = patientService.getPatientSymptoms(patient);
     	
 		model.addAttribute("patient", patient);
 		model.addAttribute("patientSymptoms", patientSymptoms);
 		
     }
     
     @RequestMapping(value="/addpatientsymptom.do", method=RequestMethod.POST)
     public ModelAndView doAddPatientSymptom (ModelMap model,
     	  @RequestParam("symptom") String symptomName,
 	      @RequestParam("notes") String notes) throws ParseException {
     	
 		Patient patient = patientService.getCurrentPatient();
 		
 		PatientSymptom patientSymptom = new PatientSymptom(-1, -1, symptomName, notes);
 		
 		patientService.addPatientSymptom(patient, patientSymptom);
 		
 		return new ModelAndView("redirect:patientsymptoms.do"); 
     }
     
     @RequestMapping(value="/removepatientsymptom.do", method=RequestMethod.POST)
     public ModelAndView doRemovePatientSymptoms (ModelMap model,
     	  @RequestParam("symptomId") int symptomId) throws ParseException {
 
 		patientService.removePatientSymptom(symptomId);
 		
 		return new ModelAndView("redirect:patientsymptoms.do");
     }
     
     
     @RequestMapping(value="/patientallergies.do", method={RequestMethod.GET})
     public void doPatientAllergies (ModelMap model) {
     	Patient patient = patientService.getCurrentPatient();
     	List<PatientAllergy> patientAllergies = patientService.getPatientAllergies(patient);
     	
 		model.addAttribute("patient", patient);
 		model.addAttribute("patientAllergies", patientAllergies);
     }
     
     @RequestMapping(value="/addpatientallergy.do", method=RequestMethod.POST)
     public ModelAndView doAddPatientAllergy (ModelMap model,
     	  @RequestParam("allergy") String allergyName,
 	      @RequestParam("notes") String notes) throws ParseException {
     	
 		Patient patient = patientService.getCurrentPatient();
 		
 		PatientAllergy patientAllergy = new PatientAllergy(-1, -1, allergyName, notes);
 		
 		patientService.addPatientAllergy(patient, patientAllergy);
 		
 		return new ModelAndView("redirect:patientallergies.do"); 
     }
     
     @RequestMapping(value="/removepatientallergy.do", method=RequestMethod.POST)
     public ModelAndView doRemovePatientAllergy (ModelMap model,
     	  @RequestParam("allergyId") int allergyId) throws ParseException {
     	
 		patientService.removePatientAllergy(allergyId);
 		return new ModelAndView("redirect:patientallergies.do");
     }
     
     
     @RequestMapping(value="/patientprocedures.do", method={RequestMethod.GET})
     public void doPatientProcedures (ModelMap model) {
     	Patient patient = patientService.getCurrentPatient();
     	List<PatientProcedure> patientProcedures = patientService.getPatientProcedures(patient);
     	
 		model.addAttribute("patient", patient);
 		model.addAttribute("patientProcedures", patientProcedures);
     }
     
     @RequestMapping(value="/addpatientprocedure.do", method=RequestMethod.POST)
     public ModelAndView doAddPatientProcedure(ModelMap model,
     	  @RequestParam("procedure") String procedureName,
 	      @RequestParam("notes") String notes) throws ParseException {
     	
 		Patient patient = patientService.getCurrentPatient();
 		
 		PatientProcedure patientProcedure = new PatientProcedure(-1, -1, procedureName, notes);
 		
 		patientService.addPatientProcedure(patient, patientProcedure);
 		
 		return new ModelAndView("redirect:patientprocedures.do"); 
     }
     
     @RequestMapping(value="/removepatientprocedure.do", method=RequestMethod.POST)
     public ModelAndView doRemovePatientProcedure(ModelMap model,
     	  @RequestParam("procedureId") int procedureId) throws ParseException {
 
 		patientService.removePatientProcedure(procedureId);
 		
 		return new ModelAndView("redirect:patientprocedures.do");
     }
     
     
     @RequestMapping(value="/patientaddmedication.do", method={RequestMethod.GET})
     public void doPatientAddMedication (ModelMap model) {
     	Patient patient = patientService.getCurrentPatient();
 		model.addAttribute("patient", patient);
     }
     
     @RequestMapping(value="/patientaddsymptoms.do", method={RequestMethod.GET})
     public void doPatientAddSymptoms (ModelMap model) {
     	Patient patient = patientService.getCurrentPatient();
 		model.addAttribute("patient", patient);
     }
     
     @RequestMapping(value="/patientaddallergy.do", method={RequestMethod.GET})
     public void doPatientAddAllergies (ModelMap model) {
     	Patient patient = patientService.getCurrentPatient();
 		model.addAttribute("patient", patient);
     }
     
     @RequestMapping(value="/patientaddprocedures.do", method={RequestMethod.GET})
     public void doPatientAddProcedures (ModelMap model) {
     	Patient patient = patientService.getCurrentPatient();
 		model.addAttribute("patient", patient);
     }
 }
