 package controllers;
 
 import models.*;
 import play.Play;
 import play.mvc.Before;
 import play.mvc.Controller;
 import play.mvc.Http;
 
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 public class Experiences extends Controller {
 
     static List<String> qs = Arrays.asList("Your opportunities for professional growth at ThoughtWorks?",
             "Your opportunities for career advancement within ThoughtWorks?",
             "Your compensation compared with similar jobs elsewhere?",
             "Your benefits package compared with similar employers?",
             "Information and knowledge sharing within ThoughtWorks?",
             "Communications from management about important issues and changes?",
             "ThoughtWorks as a place you would recommend to others to work?",
             "ThoughtWorks as a place you are proud to work?",
             "Feedback you receive about your job performance?",
             "Recognition and praise you receive when you do a good job?",
             "Leadership abilities of Senior management?",
             "Competence of Senior management?",
             "Management support in permitting time off when you think it's necessary?",
             "Employer support in balancing between work life and personal life?",
             "Fairness in how promotions are given and people are treated?",
             "The level of respect shown by management toward employees?",
             "Overall, how satisfied are you with ThoughtWorks as a place to work?");
 
     @Before
     static void addDefaults(){
         renderArgs.put("blogTitle", Play.configuration.getProperty("blog.title"));
         renderArgs.put("blogBaseline", Play.configuration.getProperty("blog.baseline"));
     }
 
     public static void index() {
         List<Company> companies = Company.findAll();
         List<Company> topRated = getTopRated(companies);
         List<Article> topViewed = Application.getTopViewedArticles();
         render(companies, topRated, topViewed);
     }
 
     private static List<Company> getTopRated(List<Company> companies) {
         double ratingSum = 0;
         Iterator<Company> companyIterator = companies.iterator();
         while(companyIterator.hasNext()){
             Company company = (Company)companyIterator.next();
             for (Integer i=0; i < company.companyRatings.size(); i++){
                 ratingSum += company.companyRatings.get(i).rating;
             }
             DecimalFormat twoDecimal = new DecimalFormat("#.##");
             if(ratingSum > 0 && company.companyRatings.size() > 0){
                double ratingPecentage = Double.valueOf(twoDecimal.format(ratingSum/company.companyRatings.size()));
             }
         }
 
         return null;
     }
 
     public static void postExperience(String orgName) {
         List<Company> companies = Company.findAll();
         List<Company> topRated = getTopRated(companies);
         List<Article> topViewed = Application.getTopViewedArticles();
         List<CubeQuestion> cubeQuestions = CubeQuestion.find("order by id asc").fetch();
         Company company = Company.find("byOrgName", orgName).first();
         render(cubeQuestions, company, topRated, topViewed);
     }
 
     public static void yourSay(String orgName){
         Company company = Company.find("byOrgName", orgName).first();
         render("/Experiences/yoursay.html",company);
     }
 
     public static void saveUserInfo(){
         System.out.println(Http.Request.current().params.get("jobStatus"));
         System.out.println(Http.Request.current().params.get("jobTitle"));
         Company company = Company.findById(Long.parseLong(Http.Request.current().params.get("employerId")));
         getUser();
         render("/Experiences/review.html",company,session.get("anonymousUserId"));
     }
 
     private static void review() {
     }
 
     private static void getUser() {
         AnonymousUser anonymousUser = null;
         System.out.println("In GetUser");
 //        if(!session.contains("loggedIn") || !session.contains("anonymousUserId")){
         if(!session.contains("loggedIn")){
 //        if(!session.contains("anonymousUserId")){
             System.out.println("checking for session");
             System.out.println(session.get("loggedIn"));
             System.out.println(session.get("anonymousUserId"));
             if(!session.contains("anonymoususerId")){
                 System.out.println("Job Status = " + request.params.get("jobStatus"));
                 System.out.println("jobTitle = " + request.params.get("jobTitle"));
                 System.out.println("jobEndingYear = " + request.params.get("jobEndingYear"));
                 System.out.println("employerCountryName = " + request.params.get("employerCountryName"));
                 System.out.println("employerCityName = " + request.params.get("employerCityName"));
 
                 anonymousUser = new AnonymousUser(request.params.get("employerCityName"),
                         request.params.get("employerCountryName"),
                         request.params.get("jobStatus"),
                         request.params.get("jobTitle"),
                         request.params.get("jobEndingYear"),
                         null).save();
 
                 session.put("anonymousUserId", anonymousUser.id);
             }
 
         }
         else if(session.contains("loggedIn")){
             System.out.println("Checking for already loggedIn User");
             System.out.println(session.get("loggedIn"));
             User user = User.find("userEmail", session.get("userEmail")).first();
             anonymousUser = new AnonymousUser(request.params.get("employerCityName"),
                     request.params.get("employerCountryName"),
                     request.params.get("jobStatus"),
                     request.params.get("jobTitle"),
                     request.params.get("jobEndingYear"),
                     user).save();
 
             session.put("anonymousUserId", anonymousUser.id);
         }
 //        else if(session.contains("anonymousUserId")){
 //            System.out.println("Here I am");
 //            System.out.println(session.get("anonymousUserId"));
 //            System.out.println(session.get("loggedIn"));
 //        }
     }
 
     public static void saveReview(){
         Company company = Company.findById(Long.parseLong(Http.Request.current().params.get("employerId")));
         AnonymousUser anonymousUser = AnonymousUser.find("id", Long.parseLong(session.get("anonymousUserId"))).first();
         List<CubeQuestion> cubeQuestions = CubeQuestion.find("order by id asc").fetch();
         CubeReview cubeReview = new CubeReview(Http.Request.current().params.get("reviewType"),company, Http.Request.current().params.get("headlineAnswer"),
                 Http.Request.current().params.get("proAnswer"), Http.Request.current().params.get("conAnswer"),
                 Http.Request.current().params.get("adviceAnswer"), anonymousUser, true).save();
 
        render("/Experiences/rating.html",company, anonymousUser, cubeQuestions);
 
     }
 
     public static void saveRating(){
         Company company = Company.find("Id", Long.parseLong(request.params.get("companyId"))).first();
         AnonymousUser anonymousUser = AnonymousUser.find("id", Long.parseLong(session.get("anonymousUserId"))).first();
         for (int i=1; i<11;i++){
             if(request.params.get("a"+i)!=null){
                 new CubeRating(anonymousUser, company, Integer.parseInt(request.params.get("quest"+i)), Integer.parseInt(request.params.get("a"+i))).save();
             }
         }
 
         flash.put("reviewStatus","Success");
 
         render("/Experiences/saved.html");
 
     }
 
     public static void showpage(){
         render("/Experiences/saved.html");
     }
 }
