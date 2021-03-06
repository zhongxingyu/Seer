 package hudson.plugins.jira;
 
 import hudson.MarkupText;
 import hudson.MarkupText.SubText;
 import hudson.model.AbstractBuild;
 import hudson.scm.ChangeLogAnnotator;
 import hudson.scm.ChangeLogSet.Entry;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * {@link ChangeLogAnnotator} that picks up JIRA issue IDs.
  * @author Kohsuke Kawaguchi
  */
 public class JiraChangeLogAnnotator extends ChangeLogAnnotator {
 
     public void annotate(AbstractBuild<?,?> build, Entry change, MarkupText text) {
         JiraSite site = JiraSite.get(build.getProject());
         if(site==null)      return;    // not configured with JIRA
 
         // if there's any recorded detail information, try to use that, too.
         JiraBuildAction a = build.getAction(JiraBuildAction.class);
 
         for(SubText token : text.findTokens(JiraIssueUpdater.ISSUE_PATTERN)) {
             try {
                 String id = token.group(0);
                 URL url = site.getUrl(id);
                 if(url==null)   continue;
 
                 JiraIssue issue = a!=null ? a.getIssue(id) : null;
 
                 if(issue==null) {
                     token.surroundWith("<a href='"+url+"'>","</a>");
                 } else {
                     token.surroundWith(
                        String.format("<a href='%s' id='JIRA-%s'>",url,issue.id),
                        String.format("</a><script>makeTooltip('JIRA-%2$s','%1$s');</script>",
                             issue.title,issue.id));
                 }
             } catch (MalformedURLException e) {
                 // impossible
             }
         }
     }
 }
