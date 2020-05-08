 package controllers;
 
import com.innoq.ldap.connector.LdapHelper;
import com.innoq.ldap.connector.LdapUser;
 import java.io.File;
 import java.util.List;
 import models.Account;
 import models.Error;
 import models.Project;
 import play.Logger;
 import play.Play;
 import play.libs.IO;
 import play.mvc.Before;
 import play.mvc.Controller;
import play.mvc.Http.Header;
 
 public class Application extends Controller {
 
     // TODO replace with LDAP FOO
     public static String defaultUser = Play.configuration.getProperty("flowcontrol.default_user");
     public static String[] admins = Play.configuration.getProperty("flowcontrol.admins").split(",");
     public static Account currentAccount;
 
     @Before
    public static void debugRequest(){
        StringBuilder sb = new StringBuilder("\n");
        Header h;
        sb.append("url: ").append(request.url).append("\n");
        sb.append("host: ").append(request.host).append("\n");
        if(request.secure){
            sb.append("secure Request\n");
        }
        for( String key : request.headers.keySet()){
            h = request.headers.get(key);
            sb.append("\t").append(key).append(":").append(h.toString()).append("\n");
        }
        Logger.info("headers: "+sb.toString());
    }

    @Before
    public static void login() {
         if (request.user == null) {
             unauthorized("LiQID");
         }
         if (!LdapHelper.getInstance().checkCredentials(request.user, request.password)) {
             unauthorized("LiQID");
         }
         String uid = request.user != null ? request.user : "gast";
         LdapUser user = (LdapUser) LdapHelper.getInstance().getUser(uid);
         Application.currentAccount = Account.findOrCreate(user.getUid(), user.get("mail"));
     }
 
     public static void index() {
         List<Error> errorList = Error.findErrorsByAccount(currentAccount);
         List<Project> projects = currentAccount.getProjects();
         Account account = currentAccount;
         render(projects, errorList, account);
     }
 
     public static void about() {
        boolean secure = request.secure;
        render(secure);
     }
 
     public static String loadExample(String example) {
         Logger.info("loading example " + example);
         return IO.readContentAsString(new File(Play.applicationPath + "/test/" + example)).trim();
     }
 }
