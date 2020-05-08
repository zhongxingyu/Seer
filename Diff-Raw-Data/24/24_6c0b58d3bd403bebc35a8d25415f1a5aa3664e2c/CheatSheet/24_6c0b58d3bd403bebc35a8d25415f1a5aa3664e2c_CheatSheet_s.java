 package controllers;
 
 import java.util.Map;
 
 import models.Author;
 import models.Repo;
 import play.data.validation.Required;
 import play.modules.gae.GAE;
 import play.modules.twig.Twig;
 import client.GithubClient;
 import client.GithubException;
 
 import com.google.common.base.Strings;
 
 public class CheatSheet extends Application {
 
     public static void list() {
         Map<String, Repo> storedRepos = Repo.findAllWithKeys();
         render(storedRepos);
     }
 
     public static void show(String authorName, String githubUser, String repoName) {
         Repo repo = Repo.findByAuthorNameAndRepo(authorName, githubUser, repoName);
         notFoundIfNull(repo);
 
         if (!repo.visible) {
            if (!GAE.isLoggedIn() || !repo.author.email.equals(GAE.getUser().getEmail()) || !GAE.isAdmin()) {
                 forbidden();
             }
         }
 
         render(repo);
     }
 
     @Secure
     public static void create() {
         boolean isNew = true;
         renderTemplate("CheatSheet/edit.html", isNew);
     }
 
     @Secure
     public static void edit(String githubUser, String repoName) {
         boolean isNew = true;
 
         if (!Strings.isNullOrEmpty(githubUser) && !Strings.isNullOrEmpty(repoName)) {
             Repo repo = getAccessibleRepo(githubUser, repoName);
             isNew = repo == null;
             if (!isNew) {
                 flash.put("title", repo.title);
                 flash.put("githubUser", repo.githubUser);
                 flash.put("repoName", repo.repoName);
                 flash.put("visible", repo.visible);
             }
         }
         render(isNew);
     }
 
     @Secure
     public static void save(
             @Required String title,
             @Required String githubUser,
             @Required String repoName,
             Boolean visible) {
         if (validation.hasErrors()) {
             params.flash();
             validation.keep();
             edit(null, null);
         }
 
         try {
             Repo repo = getAccessibleRepo(githubUser, repoName);
 
             if (repo == null) {
                 repo = new Repo();
                 repo.author = Author.findByEmail(GAE.getUser().getEmail());
                 repo.githubUser = githubUser;
                 repo.repoName = repoName;
                 repo.sheets = new GithubClient(githubUser, repoName).getSheets();
 
                 if (repo.sheets.size() == 0) {
                     flash.put("generalError", "Sorry, no cheat sheets were found at given repository.");
                     params.flash();
                     edit(null, null);
                 }
             }
 
             repo.title = title;
             repo.visible = visible == null ? false : true;
             repo.storeOrUpdate();
 
             list();
         } catch (GithubException e) {
             flash.put("generalError", e.getMessage());
             params.flash();
             edit(null, null);
         }
     }
 
     @Secure
     public static void delete(String githubUser, String repoName) {
         Repo repo = getAccessibleRepo(githubUser, repoName);
         notFoundIfNull(repo);
 
         Twig.delete(repo);
         list();
     }
 
     private static Repo getAccessibleRepo(String githubUser, String repoName) {
         Repo repo = Repo.findByAuthorEmailAndRepo(GAE.getUser().getEmail(), githubUser, repoName);
         return repo;
     }
 }
