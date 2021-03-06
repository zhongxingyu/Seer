 package hudson.scm.browsers;
 
 import hudson.model.Descriptor;
 import hudson.scm.ChangeLogSet.Entry;
 import hudson.scm.RepositoryBrowser;
 import hudson.scm.SubversionChangeLogSet;
 import hudson.scm.SubversionChangeLogSet.Path;
 import hudson.scm.SubversionRepositoryBrowser;
 import hudson.scm.EditType;
 import org.kohsuke.stapler.StaplerRequest;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * {@link RepositoryBrowser} for Subversion.
  * 
  * @author Kohsuke Kawaguchi
  * @since 1.90
  */
 public class ViewSVN extends SubversionRepositoryBrowser {
     /**
      * The URL of the top of the site.
      *
      * Normalized to ends with '/', like <tt>http://svn.apache.org/viewvc/</tt>
      * It may contain a query parameter like <tt>?root=foobar</tt>, so relative URL
      * construction needs to be done with care.
      */
     public final URL url;
 
     /**
      * @stapler-constructor
      */
     public ViewSVN(URL url) throws MalformedURLException {
         this.url = normalizeToEndWithSlash(url);
     }
 
     @Override
     public URL getDiffLink(Path path) throws IOException {
         if(path.getEditType()!= EditType.EDIT)
             return null;    // no diff if this is not an edit change
         int r = path.getLogEntry().getRevision();
         return new URL(url,trimHeadSlash(path.getValue())+param().add("r1="+(r-1)).add("r2="+r));
     }
 
     @Override
     public URL getFileLink(Path path) throws IOException {
         return new URL(url,trimHeadSlash(path.getValue())+param());
     }
 
     @Override
     public URL getChangeSetLink(SubversionChangeLogSet.LogEntry changeSet) throws IOException {
        return new URL(url,"."+param().add("view=rev").add("rev="+changeSet.getRevision()));
     }
 
     private QueryBuilder param() {
         return new QueryBuilder(url.getQuery());
     }
 
     public Descriptor<RepositoryBrowser<?>> getDescriptor() {
         return DESCRIPTOR;
     }
 
     public static final Descriptor<RepositoryBrowser<?>> DESCRIPTOR = new Descriptor<RepositoryBrowser<?>>(ViewSVN.class) {
         public String getDisplayName() {
             return "ViewSVN";
         }
 
         public ViewSVN newInstance(StaplerRequest req) throws FormException {
             return req.bindParameters(ViewSVN.class,"viewsvn.");
         }
     };
 
     private static final long serialVersionUID = 1L;
 }
