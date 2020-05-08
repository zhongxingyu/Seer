 package info.seltenheim.homepage.controllers;
 
 import info.seltenheim.homepage.controllers.secured.OnlyLoggedIn;
 import info.seltenheim.homepage.services.positions.Education;
 import info.seltenheim.homepage.services.positions.Employment;
 import info.seltenheim.homepage.services.positions.PositionsService;
 import info.seltenheim.homepage.services.positions.formdata.EmploymentData;
 import info.seltenheim.homepage.services.positions.formdata.EducationData;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 
 @Component
 public class ProfileController extends Controller {
 
     @Autowired
     private PositionsService positionsService;
 
     public Result index(String langKey, String positionId) throws IOException {
 
         final List<Employment> empl = positionsService.findAllEmployments();
         final List<Education> edus = positionsService.findAllEducations();
 
         Collections.sort(empl, new Comparator<Employment>() {
             @Override
             public int compare(Employment o1, Employment o2) {
                 final DateTime end1 = o1.getToDate();
                 final DateTime end2 = o2.getToDate();
                 if (end1 == null && end2 == null) {
                     return 0;
                 } else if (end1 == null) {
                     return -1;
                 } else if (end2 == null) {
                     return 1;
                 } else {
                     return -end1.compareTo(end2);
                 }
             }
         });
 
         return ok(info.seltenheim.homepage.views.html.profile.render(empl, edus, positionId));
     }
 
     @Security.Authenticated(OnlyLoggedIn.class)
     public Result upsertEmployment(String langKey) throws IOException {
         Form<EmploymentData> filledForm = Form.form(EmploymentData.class).bindFromRequest();
 
         if (filledForm.hasErrors()) {
             return badRequest(filledForm.errorsAsJson());
         } else {
             final EmploymentData data = filledForm.get();
             final String employmentId = data.getId();
            final Employment employment = employmentId != "-1" ? positionsService.findEmploymentById(employmentId) : new Employment();
 
             employment.setFromDate(data.getFromDateObject());
             employment.setToDate(data.getToDateObject());
             employment.setPlace(data.getPlace());
             employment.setTitle("de", data.getTitleDe());
             employment.setTitle("en", data.getTitleEn());
             employment.setWebsite(data.getWebsite());
             employment.setTasks("de", data.getTasksDeList());
             employment.setTasks("en", data.getTasksEnList());
             employment.setTechnologies(data.getTechnologiesList());
 
             positionsService.upsertPosition(employment);
             return redirect(routes.ProfileController.index(langKey, ""));
         }
     }
 
     @Security.Authenticated(OnlyLoggedIn.class)
     public Result upsertEducation(String langKey) throws IOException {
         Form<EducationData> filledForm = Form.form(EducationData.class).bindFromRequest();
 
         if (filledForm.hasErrors()) {
             return badRequest(filledForm.errorsAsJson());
         } else {
             final EducationData data = filledForm.get();
             final String educationId = data.getId();
 
             final Education education = educationId != "-1" ? positionsService.findEducationById(educationId) : new Education();
 
             education.setFromDate(data.getFromDateObject());
             education.setToDate(data.getToDateObject());
             education.setPlace(data.getPlace());
             education.setTitle("de", data.getTitleDe());
             education.setTitle("en", data.getTitleEn());
             education.setWebsite(data.getWebsite());
             education.setDegree("de", data.getDegreeDe());
             education.setDegree("en", data.getDegreeEn());
             education.setScore(data.getScore());
 
             positionsService.upsertPosition(education);
             return redirect(routes.ProfileController.index(langKey, ""));
         }
     }
 
 }
