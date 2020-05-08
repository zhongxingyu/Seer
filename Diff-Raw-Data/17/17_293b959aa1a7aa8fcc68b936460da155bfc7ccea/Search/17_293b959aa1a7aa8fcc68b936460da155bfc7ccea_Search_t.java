 package controllers;
 import static play.data.Form.form;
 
 import java.util.Collections;
 import java.util.List;
 
 import models.Student;
 import models.Tutor;
 import play.data.DynamicForm;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.search;
 
 public class Search extends Controller {
 
   public static Result search() {
     List<Tutor> emptyList = Collections.<Tutor>emptyList();
     return ok(search.render(emptyList));
   }
 
   public static Result submit() {
     DynamicForm requestData = form().bindFromRequest();
    String subject = requestData.get("subject");
    double minCost = Double.parseDouble(requestData.get("minCost"));
    double maxCost = Double.parseDouble(requestData.get("maxCost"));
    double minRating = Double.parseDouble(requestData.get("minRating"));
    List<Tutor> tutors = Student.searchForTutors(subject, minCost, maxCost,
        minRating);
     return ok(search.render(tutors));
   }
 
 }
