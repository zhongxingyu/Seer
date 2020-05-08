 package gov.usgs.cida.jenkins;
 
 import hudson.Extension;
 import hudson.model.ParameterDefinition;
 import hudson.model.ParameterValue;
 import java.io.IOException;
 import java.io.InputStream;
import java.math.BigDecimal;
 import java.util.ArrayList;
import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.UUID;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import net.sf.json.JSONObject;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.log4j.Logger;
 import org.apache.maven.artifact.versioning.ComparableVersion;
 import org.jvnet.localizer.ResourceBundleHolder;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  *
  * @author dmsibley
  */
 public class ListNexusVersionsParameterDefinition extends ParameterDefinition implements Comparable<ListNexusVersionsParameterDefinition> {
 	/** SUID. */
 	private static final long serialVersionUID = -3502280860537289034L;
 
 	private static final Logger LOGGER = Logger.getLogger(ListNexusVersionsParameterDefinition.class);
 	
 	private final UUID uuid;
 	private final String projId;
 	private final String repo;
 
 	@DataBoundConstructor
 	public ListNexusVersionsParameterDefinition(String name, String repo, String projId, String uuid) {
 		super(name, ResourceBundleHolder.get(ListNexusVersionsParameterDefinition.class).format("TagDescription"));
 		
 		this.repo = repo;
 		this.projId = projId;
 		
 		if (uuid == null || uuid.length() == 0) {
 			this.uuid = UUID.randomUUID();
 		} else {
 			this.uuid = UUID.fromString(uuid);
 		}
 	}
 
 	@Override
 	public ParameterValue createValue(StaplerRequest sr) {
 		String[] values = sr.getParameterValues(getName());
 		if (values == null || values.length != 1) {
 			return this.getDefaultParameterValue();
 		}
 		return new ListNexusVersionsParameterValue(getName(), values[0]);
 	}
 
 	@Override
 	public ParameterValue createValue(StaplerRequest sr, JSONObject jsono) {
 		ListNexusVersionsParameterValue result = sr.bindJSON(ListNexusVersionsParameterValue.class, jsono);
 		return result;
 	}
 
 	@Override
 	public ParameterValue getDefaultParameterValue() {
 		String result = "";
 		List<String> versions = getVersions();
 		
 		if (0 < versions.size()) {
 			result = versions.get(0);
 		}
 		
 		return new ListNexusVersionsParameterValue(getName(), result);
 	}
 
 	@Override
 	public DescriptorImpl getDescriptor() {
 		return (DescriptorImpl) super.getDescriptor();
 	}
 
 	public String getRepo() {
 		return this.repo;
 	}
 	
 	public String getProjId() {
 		return this.projId;
 	}
 	
 	/**
 	 * Returns a list of artifact versions to be displayed in
 	 * {@code ListNexusVersionsParameterDefinition/index.jelly}.
 	 *
 	 * @return
 	 */
 	public List<String> getVersions() {
 		Map<ComparableVersion, String> result = new TreeMap<ComparableVersion, String>();
 		
 		String[] artifactName = this.projId.split(":");
 		String uri = this.repo + "/service/local/lucene/search?g=" + artifactName[0] + "&a=" + artifactName[1];
 		
 		GetMethod GET = new GetMethod(uri);
 		HttpClient httpClient = new HttpClient();
 		try {
 			GET.setFollowRedirects(true);
 			int code = httpClient.executeMethod(GET);
 			if (200 == code) {
 				InputStream in = GET.getResponseBodyAsStream();
 				XMLStreamReader xml = null;
 				try {
 					xml = XMLInputFactory.newInstance().createXMLStreamReader(in);
 
 					while (xml.hasNext()) {
 						int tag = xml.next();
 						if (XMLStreamReader.START_ELEMENT == tag && "artifact".equals(xml.getLocalName())) {
 							while (xml.hasNext()) {
 								int inner = xml.next();
 								if (XMLStreamReader.START_ELEMENT == inner && "version".equals(xml.getLocalName())) {
 									String text = xml.getElementText();
 									result.put(new ComparableVersion(text), text);
 								}
 							}
 						}
 					}
 
 				} finally {
 					if (null != xml) {
 						xml.close();
 					}
 				}
 			}
 		} catch (IOException e) {
 			LOGGER.error(null, e);
 		} catch (XMLStreamException e) {
 			LOGGER.error(null, e);
 		} finally {
 			GET.releaseConnection();
 		}
 		List<String> vals = new ArrayList<String>(result.values()); 
 		Collections.reverse(vals);
 		return vals;
 	}
 	
 	public static class VersionSorter implements Comparator<String>{
 
 		public int compare(String version1, String version2) {
 			String[] version1Splits = version1.split("-");
 			String[] version2Splits = version2.split("-");
 			if (version1Splits[0].equals(version2Splits[0])){
 				//the released version (the one without the -SNAPSHOT) is newer
 				return version1Splits.length > 1 ? -1 : 1; 
 			} else{
 				return simpleNumericCompare(version1Splits, version2Splits);
 			}
 		}
 
 		private int simpleNumericCompare(String[] version1Splits,
 				String[] version2Splits) {
 			//clean up the version numbers in case there are multiple decimals
 			String v1Clean = version1Splits[0].replace(".", ",");
 			String v2Clean = version2Splits[0].replace(".", ",");
 			return Double.valueOf(v1Clean).compareTo(Double.valueOf(v2Clean));
 		}
 	}
 
 	public int compareTo(ListNexusVersionsParameterDefinition o) {
 		int result = -1;
 		if (null != o && o.uuid.equals(this.uuid)) {
 			result = 0;
 		}
 		return result;
 	}
 
 	@Extension
 	public static class DescriptorImpl extends ParameterDescriptor {
 
 		@Override
 		public String getDisplayName() {
 			return ResourceBundleHolder.get(ListNexusVersionsParameterDefinition.class).format("DisplayName");
 		}
 	}
 }
