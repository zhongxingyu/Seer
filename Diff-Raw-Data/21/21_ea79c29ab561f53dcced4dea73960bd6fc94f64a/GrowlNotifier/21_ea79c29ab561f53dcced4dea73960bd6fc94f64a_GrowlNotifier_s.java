 package net.sourcewalker.svnnotify.notifier;
 
 import java.io.IOException;
 import java.io.PrintStream;
import java.util.ArrayList;
 import java.util.List;
 
 import net.sourcewalker.svnnotify.data.interfaces.INotifier;
 import net.sourcewalker.svnnotify.data.interfaces.IRepository;
 import net.sourcewalker.svnnotify.data.interfaces.IRevision;
 
 /**
  * Display a Growl notification for the received Subversion commits. This
  * notifier currently uses the "growlnotify" command line client to display the
  * notification. Growl is only available on Mac OS X, so this notifier only
  * works on that OS.
 * 
  * @author xperimental
  */
 public class GrowlNotifier implements INotifier {
 
     @Override
    public void reportUpdates(IRepository repository, List<IRevision> revisions) {
         StringBuilder sb = new StringBuilder();
         sb.append("Repository " + repository.getName() + " has "
                 + revisions.size() + " new commits:\n");
         for (IRevision rev : revisions) {
             sb.append("  " + rev.getRevision() + " by " + rev.getAuthor()
                     + " (" + rev.getTimestamp().toString() + ")\n");
         }
         String message = sb.toString();
 
        List<String> params = new ArrayList<String>();
        // params.add("-t");
        // params.add("\"Subversion\"");

         try {
            Process growlProc = Runtime.getRuntime().exec("growlnotify",
                    params.toArray(new String[0]));
             PrintStream growlOut = new PrintStream(growlProc.getOutputStream());
             growlOut.print(message);
             growlOut.close();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
     }
 
 }
