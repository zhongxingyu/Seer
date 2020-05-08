 package jenkins.plugin.assembla.api;
 
 import hudson.model.AbstractProject;
 import jenkins.plugin.assembla.AssemblaProjectProperty;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 public class AssemblaSite {
 
 	private String username;
 	private String password;
 	private String url;
 	private String pattern;
 
 	private final static String DEFAULT_PATTERN = "(#[0-9]+)( .*)"; // default
 
 	private transient String space;
 
 	private transient boolean backtrackEnabled;
 
 	private transient boolean pluginEnabled;
 
 	/**
 	 * @stapler-constructor
 	 */
 	@DataBoundConstructor
 	public AssemblaSite(String username, String password, String url,
 			String pattern) {
 
 		this.password = password;
 		this.username = username;
 		this.url = url;
 		this.pattern = pattern;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public String getSpace() {
 		return space;
 	}
 
 	public void setSpace(String space) {
 		this.space = space;
 	}
 
 	public String getName() {
 
 		return url;
 	}
 
 	public boolean isBacktrackEnabled() {
 		return backtrackEnabled;
 	}
 
 	public void setBacktrackEnabled(boolean backtrackEnabled) {
 		this.backtrackEnabled = backtrackEnabled;
 	}
 
 	public boolean isPluginEnabled() {
 		return pluginEnabled;
 	}
 
 	public void setPluginEnabled(boolean pluginEnabled) {
 		this.pluginEnabled = pluginEnabled;
 	}
 
 	public String getPattern() {

		return pattern;
	}

	public String getPatternInternal() {

		if (pattern == null || pattern.length() == 0) {

 			return DEFAULT_PATTERN;
 		}
 		
 		return pattern;
 	}
 
 	public void setPattern(String pattern) {
 		this.pattern = pattern;
 	}
 
 	public static AssemblaSite get(AbstractProject<?, ?> p) {
 		AssemblaProjectProperty jpp = p
 				.getProperty(AssemblaProjectProperty.class);
 		if (jpp != null) {
 			AssemblaSite site = jpp.getSite();
 			if (site != null)
 				return site;
 		}
 
 		// none is explicitly configured. try the default ---
 		// if only one is configured, that must be it.
 		AssemblaSite[] sites = AssemblaProjectProperty.DESCRIPTOR.getSites();
 		if (sites.length == 1)
 			return sites[0];
 
 		return null;
 	}
 
 	@Override
 	public String toString() {
 
 		return "URL='" + url + "' - Space='" + space + "' Username='"
 				+ username + "' Password='xxxxxx'";
 	}
 }
