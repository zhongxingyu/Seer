 package uk.org.sappho.code.heatmap.engine;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.google.inject.Inject;
 import com.thoughtworks.xstream.XStream;
 
 import uk.org.sappho.code.heatmap.config.Configuration;
 import uk.org.sappho.code.heatmap.config.ConfigurationException;
 import uk.org.sappho.code.heatmap.issues.IssueManagementException;
 
 public class Releases {
 
     private final List<String> releaseNames;
     private final String storeFilename;
     private Map<String, HeatMaps> releases = new HashMap<String, HeatMaps>();
     private static final Logger LOG = Logger.getLogger(Releases.class);
 
     @Inject
     public Releases(Configuration config) throws ConfigurationException {
 
         releaseNames = config.getPropertyList("releases");
         storeFilename = config.getProperty("releases.store.filename", "heatmap-data.xml");
     }
 
     public void update(ChangeSet change) throws IssueManagementException {
 
         List<String> issueReleases = change.getIssue().getReleases();
         for (String issueRelease : issueReleases) {
             HeatMaps heatMaps = releases.get(issueRelease);
             if (heatMaps == null) {
                 heatMaps = new HeatMaps();
                 releases.put(issueRelease, heatMaps);
             }
             heatMaps.update(change);
         }
     }
 
    public final List<String> getReleaseNames() {
 
        return releaseNames;
     }
 
     public final HeatMaps getHeatMaps(String releaseName) {
 
         return releases.get(releaseName);
     }
 
     @SuppressWarnings("unchecked")
     public void load() throws IOException {
 
         LOG.info("Loading data from " + storeFilename);
         XStream xstream = new XStream();
         Reader reader = new FileReader(storeFilename);
         releases = (Map<String, HeatMaps>) xstream.fromXML(reader);
         reader.close();
     }
 
     public void save() throws IOException {
 
         LOG.info("Saving data to " + storeFilename);
         XStream xstream = new XStream();
         Writer writer = new FileWriter(storeFilename);
         xstream.toXML(releases, writer);
         writer.close();
     }
 }
