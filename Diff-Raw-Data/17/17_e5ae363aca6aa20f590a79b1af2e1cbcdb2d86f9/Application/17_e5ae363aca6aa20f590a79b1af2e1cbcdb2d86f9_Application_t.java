 /**
  * comment
  */
 package controllers;
 
 import java.util.List;
 import com.avaje.ebean.Page;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Security;
 import views.formdata.CountryType;
 import views.formdata.LoginFormData;
 import views.formdata.RatingFormData;
 import views.formdata.SearchFormData;
 import views.formdata.SignupFormData;
 import views.formdata.SurferFormData;
 import views.formdata.SurferTypes;
 import views.html.Index;
 import views.html.Login;
 import views.html.ShowSurfer;
 import views.html.ShowUser;
 import views.html.ManageSurfer;
 import views.html.Updates;
 import views.html.Search;
 import models.FavoriteDB;
 import models.RatingDB;
 import models.Surfer;
 import models.SurferDB;
 import models.SurferUpdateDB;
 import models.UserInfo;
 import models.UserInfoDB;
 
 /**
  * Implements the controllers for this application.
  */
 public class Application extends Controller {
 
   
   /**
    * Returns the home page. 
    * @return The resulting home page. 
    */
   public static Result index() {
     CountryType.getSearchCountries();
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     return ok(Index.render("Index", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()),
                            searchForm, SurferTypes.getTypes(), CountryType.getSearchCountries()));
   }
   /**
    * Create new Surfer.
    * @return The new Surfer form page.
    */
   @Security.Authenticated(Secured.class)
   public static Result newSurfer() {
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     String user = userInfo.getEmail();
     SurferFormData surferFD = new SurferFormData();
     List<String> foot = SurferDB.getFootstyleList();
     Form<SurferFormData> formData = Form.form(SurferFormData.class).fill(surferFD);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     return ok(ManageSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData, 
         SurferTypes.getTypes(), foot, searchForm, CountryType.getSearchCountries()));
   }
   
   /**
    * Handles post form submission from the Surfer form.
    * @return The Surfer that was created/edited if the request was valid. The form page with error messages otherwise.
    */
   @Security.Authenticated(Secured.class)
  public static Result postSurfer() {
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     String user = userInfo.getEmail();
     Form<SurferFormData> formData = Form.form(SurferFormData.class).bindFromRequest();
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     RatingFormData ratingFormData = new RatingFormData();
     Form<RatingFormData> ratingForm = Form.form(RatingFormData.class).fill(ratingFormData);
     if (formData.hasErrors()) {
       flash("error", "Please correct the form below.");
       List<String> foot = SurferDB.getFootstyleList();
       return badRequest(ManageSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), 
           formData, SurferTypes.getTypes(), foot, searchForm, CountryType.getSearchCountries()));
     }
     else {
       SurferFormData data = formData.get();
       flash("success", String.format("Successfully added %s", data.name));
       SurferDB.add(data.slug, data);
       return ok(ShowSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), SurferDB.getSurfer(data.slug),
           searchForm, SurferTypes.getTypes(), CountryType.getSearchCountries(), SurferDB.getRatingList(), ratingForm));
     }
   }
   
   /**
    * Delete a Surfer.
    * @param slug The slug of the Surfer to delete.
    * @return The Index page.
    */
   @Security.Authenticated(Secured.class)
  public static Result deleteSurfer(String slug) {
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     String user = userInfo.getEmail();
     RatingDB.deleteRating(SurferDB.getSurfer(slug).getRating());
     SurferDB.deleteSurfer(slug);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     return ok(Index.render("", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), searchForm, 
                            SurferTypes.getTypes(), CountryType.getSearchCountries()));
   }
   
   /**
    * Edit a Surfer.
    * @param slug The slug of the Surfer to edit.
    * @return The Surfer form page.
    */
   @Security.Authenticated(Secured.class)
   public static Result manageSurfer(String slug) {
     UserInfo userInfo = UserInfoDB.getUser(request().username());
     String user = userInfo.getEmail();
     SurferFormData surferFD = new SurferFormData(SurferDB.getSurfer(slug));
     List<String> foot = SurferDB.getFootstyleList();
     Form<SurferFormData> formData = Form.form(SurferFormData.class).fill(surferFD);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     return ok(ManageSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData,
         SurferTypes.getTypes(SurferDB.getSurfer(slug).getType()), foot, searchForm, CountryType.getSearchCountries()));
   }
   
   /**
    * Get a Surfer.
    * @param slug The slug of the Surfer.
    * @return The Surfer's personal page if slug is valid else the Index page.
    */
   public static Result getSurfer(String slug) {
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     RatingFormData ratingFormData = new RatingFormData();
     Form<RatingFormData> ratingForm = Form.form(RatingFormData.class).fill(ratingFormData);
    if (SurferDB.getSurfer(slug) != null) {
       return ok(ShowSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), SurferDB.getSurfer(slug),
                 searchForm, SurferTypes.getTypes(), CountryType.getSearchCountries(), SurferDB.getRatingList(), 
                 ratingForm));
     }
     else { 
       return ok(Index.render("", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), searchForm,
                 SurferTypes.getTypes(), CountryType.getSearchCountries()));
     }
   }
   
  /**
   * Posts a rating to a specified surfer.
   * @param slug the slug of the surfer to post the rating too
   * @return ShowSurfer page
   */
   public static Result postRating(String slug) {
     Form<RatingFormData> formData = Form.form(RatingFormData.class).bindFromRequest();
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     RatingFormData data = formData.get();
     Surfer surfer = SurferDB.getSurfer(slug);
     RatingDB.addRating(surfer, Secured.getUserInfo(ctx()), data.rating);
     return ok(ShowSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), SurferDB.getSurfer(slug),
         searchForm, SurferTypes.getTypes(), CountryType.getSearchCountries(), SurferDB.getRatingList(), formData));
   }
   
   /**
    * Return the update page.
    * @return The update page. 
    */
  public static Result getUpdates() {
       SearchFormData searchFormData = new SearchFormData();
       Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
       return ok(Updates.render("", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), SurferUpdateDB.getUpdates(),
                 searchForm, SurferTypes.getTypes(), CountryType.getSearchCountries()));
   }
   
   /**
    * Provides the Login page (only to unauthenticated users). 
    * @return The Login page. 
    */
   public static Result login() {
     Form<LoginFormData> formData = Form.form(LoginFormData.class);
     Form<SignupFormData> signupData = Form.form(SignupFormData.class);
     //Form<RegistrationFormData> regFormData = Form.form(RegistrationFormData.class);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     return ok(Login.render("Login", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData, searchForm,
               signupData, SurferTypes.getTypes(), CountryType.getSearchCountries()));
   }
   
   /**
    * Creates a new login session.
    * @return A redirect to the index page on success or the login page on failure
    */
   public static Result postLogin() {
     // Get the submitted form data from the request object, and run validation.
     Form<LoginFormData> formData = Form.form(LoginFormData.class).bindFromRequest();
     //Form<RegistrationFormData> regFormData = Form.form(RegistrationFormData.class);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     Form<SignupFormData> signupData = Form.form(SignupFormData.class);
     if (formData.hasErrors()) {
       flash("loginError", "Login credentials not valid.");
       return badRequest(Login.render("Login", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData,
                         searchForm, signupData, SurferTypes.getTypes(), CountryType.getSearchCountries()));
     }
     else {
       // email/password OK, so now we set the session variable and only go to authenticated pages.
       session().clear();
       session("email", formData.get().email);
       return redirect(routes.Application.index());
     }
   }
   
   /**
    * Logs out (only for authenticated users) and returns them to the Index page. 
    * @return A redirect to the Index page. 
    */
   @Security.Authenticated(Secured.class)
   public static Result logout() {
     session().clear();
     return redirect(routes.Application.index());
   }
   
   /**
    * Result page from the Search widget.
    * @param page The page to retrieve.
    * @return The search results.
    */
   public static Result search(int page) {
     Form<SearchFormData> formData = Form.form(SearchFormData.class).bindFromRequest();
     SearchFormData data = formData.get(); 
     Page<Surfer> results = SurferDB.search(data.searchTerm, data.type, data.country, page);
     return ok(Search.render("Search", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()),
         formData, SurferTypes.getTypes(), CountryType.getSearchCountries(), data.searchTerm, data.type, data.country,
         results));
   }
   
   /**
    * Result page for pagination.
    * @param searchTerm The search term.
    * @param type The type of Surfer.
    * @param country The country of the Surfer.
    * @param page The page to retrieve.
    * @return The search result page. 
    */
   public static Result pageSearch(String searchTerm, String type, String country, int page) {
     Form<SearchFormData> formData = Form.form(SearchFormData.class).bindFromRequest();
     Page<Surfer> results = SurferDB.search(searchTerm, type, country, page - 1);
     return ok(Search.render("Search", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()),
         formData, SurferTypes.getTypes(), CountryType.getSearchCountries(), searchTerm, type, country,
         results));
   }
   
   /**
    * Result page after signing up as a new user.
    * @return The Index page.
    */
   public static Result newUser() {
     // Get the submitted form data from the request object, and run validation.
     Form<LoginFormData> formData = Form.form(LoginFormData.class);
     //Form<RegistrationFormData> regFormData = Form.form(RegistrationFormData.class);
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     Form<SignupFormData> signupData = Form.form(SignupFormData.class).bindFromRequest();
     if (signupData.hasErrors()) {
       flash("signupError", "Form input not valid.");
       return badRequest(Login.render("Login", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), formData,
                         searchForm, signupData, SurferTypes.getTypes(), CountryType.getSearchCountries()));
     }
     else {
       SignupFormData data = signupData.get();
       UserInfoDB.addUserInfo(data.signupName, data.signupEmail, data.signupPassword);
       CountryType.getSearchCountries();
       return ok(Index.render("Index", Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()),
                              searchForm, SurferTypes.getTypes(), CountryType.getSearchCountries()));
     }
   }
   
   /**
    * Result page for adding a Favorite.
    * @param slug The slug of the Surfer that is to be a Favorite.
    * @return The Surfer with the matching slug.
    */
   @Security.Authenticated(Secured.class)
   public static Result addFavorite(String slug) {
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     RatingFormData ratingFormData = new RatingFormData();
     Form<RatingFormData> ratingForm = Form.form(RatingFormData.class).fill(ratingFormData);
     FavoriteDB.add(SurferDB.getSurfer(slug), Secured.getUserInfo(ctx()));
     return ok(ShowSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), SurferDB.getSurfer(slug),
         searchForm, SurferTypes.getTypes(), CountryType.getSearchCountries(), SurferDB.getRatingList(), ratingForm));
   }
   
   /**
    * Result page for adding a Favorite.
    * @param slug The slug of the Surfer that is to be a Favorite.
    * @return The Surfer with the matching slug.
    */
   @Security.Authenticated(Secured.class)
   public static Result deleteFavorite(String slug) {
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     RatingFormData ratingFormData = new RatingFormData();
     Form<RatingFormData> ratingForm = Form.form(RatingFormData.class).fill(ratingFormData);
     FavoriteDB.delete(SurferDB.getSurfer(slug), Secured.getUserInfo(ctx()));
     return ok(ShowSurfer.render(Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()), SurferDB.getSurfer(slug),
         searchForm, SurferTypes.getTypes(), CountryType.getSearchCountries(), SurferDB.getRatingList(), ratingForm));
   }
   
   /**
    * Result page for UserInfo information.
    * @param id The ID of the UserInfo.
    * @return A page about a UserInfo.
    */
   @Security.Authenticated(Secured.class)  
   public static Result showUser(long id) {
     SearchFormData searchFormData = new SearchFormData();
     Form<SearchFormData> searchForm = Form.form(SearchFormData.class).fill(searchFormData);
     UserInfo user = UserInfoDB.getUser(id);
    if (user == null) {
      return redirect(routes.Application.index());
    }
    else {
      return ok(ShowUser.render(user, Secured.isLoggedIn(ctx()), Secured.getUserInfo(ctx()),
          searchForm, SurferTypes.getTypes(), CountryType.getSearchCountries()));
    }
   }
 }
