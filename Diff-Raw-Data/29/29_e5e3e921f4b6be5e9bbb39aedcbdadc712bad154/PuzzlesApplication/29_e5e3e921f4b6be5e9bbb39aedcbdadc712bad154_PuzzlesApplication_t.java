 package de.puzzles.webapp;
 
 import de.puzzles.webapp.page.LoginPage;
 import org.apache.wicket.Page;
 import org.apache.wicket.RuntimeConfigurationType;
 import org.apache.wicket.core.util.file.WebApplicationPath;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.request.resource.caching.FilenameWithVersionResourceCachingStrategy;
 import org.apache.wicket.request.resource.caching.version.LastModifiedResourceVersion;
 import org.apache.wicket.resource.NoOpTextCompressor;
 import org.apache.wicket.util.string.Strings;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author Patrick Groß-Holtwick
  *         Date: 25.01.13
  *         Time: 14:08
  *         To change this template use File | Settings | File Templates.
  */
 public class PuzzlesApplication extends WebApplication {
 
     @Override
     protected void init() {
         super.init();
         if (RuntimeConfigurationType.DEVELOPMENT.equals(getConfigurationType())) {
             getDebugSettings().setDevelopmentUtilitiesEnabled(true);
             getDebugSettings().setAjaxDebugModeEnabled(true);
             getDebugSettings().setOutputComponentPath(true);
             String resourceFolder = System.getProperty("application.resource.src");
             String webResourceFolder = System.getProperty("application.webresource.src");
             if (!Strings.isEmpty(resourceFolder)) {
                 getResourceSettings().getResourceFinders().add(new WebApplicationPath(getServletContext(), resourceFolder));
             }
             if (!Strings.isEmpty(webResourceFolder)) {
                 getResourceSettings().getResourceFinders().add(new WebApplicationPath(getServletContext(), webResourceFolder));
             }
         }
         else {
             getResourceSettings().setCachingStrategy(new FilenameWithVersionResourceCachingStrategy(new LastModifiedResourceVersion()));
             getResourceSettings().setJavaScriptCompressor(new NoOpTextCompressor());
             getMarkupSettings().setStripComments(true);
             getMarkupSettings().setCompressWhitespace(true);
         }
         getMarkupSettings().setDefaultMarkupEncoding("utf-8");

     }
 
     @Override
     public Class<? extends Page> getHomePage() {
         return LoginPage.class;
     }
 
}
