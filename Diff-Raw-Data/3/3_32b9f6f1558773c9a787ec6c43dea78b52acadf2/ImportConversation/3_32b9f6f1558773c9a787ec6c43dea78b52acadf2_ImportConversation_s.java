 package org.jenkinsci.plugins.recipe;
 
 import org.kohsuke.stapler.HttpResponse;
 import org.kohsuke.stapler.HttpResponses;
 import org.kohsuke.stapler.Stapler;
 import org.kohsuke.stapler.StaplerRequest;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 /**
  * Conversation-scoped object that guides the user through the importing process.
  *
  * Tied to {@link HttpSession}.
  *
  * @author Kohsuke Kawaguchi
  */
 public class ImportConversation {
     public final Recipe recipe;
 
     /**
      * If this conversation resulted in any error, put it here
      * and the user will see it.
      */
     public Exception error;
 
     public ImportConversation(Recipe recipe) {
         this.recipe = recipe;
         Stapler.getCurrentRequest().getSession().setAttribute(SESSION_KEY, this);
     }
 
     public static ImportConversation getCurrent() {
         return (ImportConversation)Stapler.getCurrentRequest().getSession().getAttribute(SESSION_KEY);
     }
 
     public HttpResponse doCook(StaplerRequest req) throws ServletException {
         recipe.apply(req);
 
         // permission checked by individual Ingredients
         try {
             recipe.cook();
             return HttpResponses.redirectToContextRoot();
         } catch (IOException e) {
             error = e;
             return HttpResponses.redirectToDot();
         }
     }
 
     private static final String SESSION_KEY = ImportConversation.class.getName();
 }
