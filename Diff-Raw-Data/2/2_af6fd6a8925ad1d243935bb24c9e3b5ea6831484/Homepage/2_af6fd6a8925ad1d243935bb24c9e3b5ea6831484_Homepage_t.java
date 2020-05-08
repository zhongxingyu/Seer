 package org.freenetproject.plugin.infocalypse_webui.ui.web;
 
 import freenet.client.HighLevelSimpleClient;
 import freenet.support.SimpleFieldSet;
 import org.apache.velocity.VelocityContext;
 import org.freenetproject.plugin.infocalypse_webui.main.InfocalypseL10n;
 import org.freenetproject.plugin.infocalypse_webui.ui.fcp.FCPHandler;
 import org.freenetproject.plugin.infocalypse_webui.ui.fcp.InfocalypseQuery;
 import org.freenetproject.plugin.infocalypse_webui.ui.fcp.InfocalypseResponseHandler;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Plugin homepage / dashboard.
  */
 public class Homepage extends VelocityToadlet {
 
     private final FCPHandler fcpHandler;
     // TODO: Assuming this needs to be private (and not local) to stay around and not get GC'd?
     private final ScheduledThreadPoolExecutor executor;
     // TODO: Would it be more appropriate to hold Files for paths?
     private final ArrayList<String> repoPaths;
 
     public Homepage(HighLevelSimpleClient client, InfocalypseL10n l10n, FCPHandler handler) {
         super(client, l10n, "homepage.vm", "/infocalypse/", "Infocalypse.Menu");
         fcpHandler = handler;
 
         repoPaths = new ArrayList<String>();
         executor = new ScheduledThreadPoolExecutor(1);
         // Query local repo information immediately and every 5 seconds thereafter.
        executor.scheduleWithFixedDelay(new RepoListQuery(), 0, 5, TimeUnit.SECONDS);
     }
 
     @Override
     void updateContext(VelocityContext context) {
         context.put("greetings", new String[] { "Hello", "Hi", "Howdy", "How'da do", "What's up", "How's it hanging"});
         context.put("identifier", fcpHandler.getConnectedIdentifier());
         synchronized (repoPaths) {
             // TODO: Does this copy it or can it be changed after this lock but before the template renders?
             context.put("paths", repoPaths);
         }
     }
 
     private class RepoListQuery implements Runnable {
         private final SimpleFieldSet sfs;
         private final InfocalypseResponseHandler handler;
 
         public RepoListQuery() {
             sfs = new SimpleFieldSet(false);
             sfs.putOverwrite("Message", "ListLocalRepos");
             handler = new Homepage.RepoListHandler();
         }
 
         @Override
         public void run() {
             System.err.println("Querying local repos.");
             fcpHandler.pushQuery(new InfocalypseQuery(sfs, handler));
         }
     }
 
     private class RepoListHandler extends InfocalypseResponseHandler {
 
         @Override
         public SimpleFieldSet handle(SimpleFieldSet params) {
             assert params.get("Message").equals("RepoList");
             synchronized (repoPaths) {
                 repoPaths.clear();
 
                 Iterator<String> it = params.keyIterator();
                 while (it.hasNext()) {
                     final String key = it.next();
                     if (key.startsWith("Repo")) {
                         // TODO: Check that the part after "Repo" is a number?
                         repoPaths.add(params.get(key));
                     }
                 }
 
                 // No fields to add to response.
                 return new SimpleFieldSet(true);
             }
         }
     }
 }
