 package hudson.plugins.emma;
 
 import hudson.model.Action;
 import hudson.model.Build;
 import hudson.model.Result;
 import hudson.util.IOException2;
 import hudson.util.StreamTaskListener;
 import hudson.util.NullStream;
 import org.kohsuke.stapler.StaplerProxy;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.ref.WeakReference;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Build view extension by Emma plugin.
  *
  * As {@link CoverageObject}, it retains the overall coverage report.
  *
  * @author Kohsuke Kawaguchi
  */
 public final class EmmaBuildAction extends CoverageObject<EmmaBuildAction> implements Action, StaplerProxy {
     public final Build owner;
 
     private transient WeakReference<CoverageReport> report;
 
     /**
      * Non-null if the coverage has pass/fail rules.
      */
     private final Rule rule;
 
     public EmmaBuildAction(Build owner, Rule rule, Ratio classCoverage, Ratio methodCoverage, Ratio blockCoverage, Ratio lineCoverage) {
         this.owner = owner;
         this.rule = rule;
         this.clazz = classCoverage;
         this.method = methodCoverage;
         this.block = blockCoverage;
         this.line = lineCoverage;
     }
 
     public String getDisplayName() {
         return "Coverage Report";
     }
 
     public String getIconFileName() {
         return "graph.gif";
     }
 
     public String getUrlName() {
         return "emma";
     }
 
     public Object getTarget() {
         return getResult();
     }
 
     @Override
     public Build getBuild() {
         return owner;
     }
 
     /**
      * Obtains the detailed {@link CoverageReport} instance.
      */
     public synchronized CoverageReport getResult() {
         if(report!=null) {
             CoverageReport r = report.get();
             if(r!=null)     return r;
         }
 
         File reportFile = EmmaPublisher.getEmmaReport(owner);
         try {
             CoverageReport r = new CoverageReport(this,reportFile);
 
             if(rule!=null) {
                 // we change the report so that the FAILED flag is set correctly
                 logger.info("calculating failed packages based on " + rule);
                 rule.enforce(r,new StreamTaskListener(new NullStream()));
             }
 
             report = new WeakReference<CoverageReport>(r);
             return r;
         } catch (IOException e) {
             logger.log(Level.WARNING, "Failed to load "+reportFile,e);
             return null;
         }
     }
 
     @Override
     public EmmaBuildAction getPreviousResult() {
         return getPreviousResult(owner);
     }
 
     /**
      * Gets the previous {@link EmmaBuildAction} of the given build.
      */
     /*package*/ static EmmaBuildAction getPreviousResult(Build start) {
        Build b = start;
         while(true) {
             b = b.getPreviousBuild();
             if(b==null)
                 return null;
             if(b.getResult()== Result.FAILURE)
                 continue;
             EmmaBuildAction r = b.getAction(EmmaBuildAction.class);
             if(r!=null)
                 return r;
         }
     }
 
     /**
      * Constructs the object from emma XML report file.
      * See <a href="http://emma.sourceforge.net/coverage_sample_c/coverage.xml">an example XML file</a>.
      *
      * @throws IOException
      *      if failed to parse the file.
      */
     public static EmmaBuildAction load(Build owner, Rule rule, File f) throws IOException {
         FileInputStream in = new FileInputStream(f);
         try {
             return load(owner,rule,in);
         } catch (XmlPullParserException e) {
             throw new IOException2("Failed to parse "+f,e);
         } finally {
             in.close();
         }
     }
 
     public static EmmaBuildAction load(Build owner, Rule rule, InputStream in) throws IOException, XmlPullParserException {
         XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
         factory.setNamespaceAware(true);
         XmlPullParser parser = factory.newPullParser();
 
         parser.setInput(in,null);
         while(true) {
             if(parser.nextTag()!=XmlPullParser.START_TAG)
                 continue;
             if(!parser.getName().equals("coverage"))
                 continue;
             break;
         }
 
         // head for the first <coverage> tag.
         Ratio[] r = new Ratio[4];
         for( int i=0; i<r.length; i++ ) {
             if(!parser.getName().equals("coverage"))
                 break;  // line coverage is optional
             parser.require(XmlPullParser.START_TAG,"","coverage");
             r[i] = readCoverageTag(parser);
         }
 
         return new EmmaBuildAction(owner,rule,r[0],r[1],r[2],r[3]);
     }
 
     private static Ratio readCoverageTag(XmlPullParser parser) throws IOException, XmlPullParserException {
         String v = parser.getAttributeValue("", "value");
         Ratio r = Ratio.parseValue(v);
 
         // move to the next coverage tag.
         parser.nextTag();
         parser.nextTag();
 
         return r;
     }
 
     private static final Logger logger = Logger.getLogger(EmmaBuildAction.class.getName());
 }
