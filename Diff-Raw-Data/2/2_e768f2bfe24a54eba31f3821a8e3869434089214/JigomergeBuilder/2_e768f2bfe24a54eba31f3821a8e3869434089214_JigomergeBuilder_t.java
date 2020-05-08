 package hudson.plugins.jigomerge;
 
 import groovy.lang.GroovyClassLoader;
 import groovy.lang.GroovyObject;
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.XmlFile;
 import hudson.model.Action;
 import hudson.model.BuildListener;
 import hudson.model.AbstractBuild;
 import hudson.model.Descriptor;
 import hudson.tasks.Builder;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 public class JigomergeBuilder extends Builder {
 
 	@Extension
 	public static final JigomergeBuildDescriptor DESCRIPTOR = new JigomergeBuildDescriptor();
 
	private static final String JIGOMERGE_VERSION = "2.2.2";
 
 	private final String source;
 	private final String username;
 	private final String password;
 	private final boolean oneByOne;
 	private final boolean eager;
 
 	private String validationScript = null;
 
 	private boolean dryRun = false;
 	private boolean verbose = true;
 
 	@DataBoundConstructor
 	public JigomergeBuilder(String source, String username, String password, boolean oneByOne, boolean eager) {
 		this.source = source;
 		this.username = username;
 		this.password = password;
 		this.oneByOne = oneByOne;
 		this.eager = eager;
 	}
 
 	@Override
 	public Descriptor<Builder> getDescriptor() {
 		return DESCRIPTOR;
 	}
 
 	@Override
 	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
 	        throws InterruptedException, IOException {
 		String workingDirectory = build.getModuleRoot().toURI().getPath();
 
 		MergeResult result = new MergeResult();
 		result.setStatus(false);
 
 		try {
 			InputStream scriptResource = this.getClass().getResourceAsStream(
 			        "/scripts/jigomerge-" + JIGOMERGE_VERSION + ".groovy");
 			GroovyClassLoader gcl = new GroovyClassLoader();
 			Class<?> clazz = gcl.parseClass(scriptResource);
 			Constructor<?>[] constructors = clazz.getConstructors();
 			GroovyObject instance = (GroovyObject) constructors[0].newInstance(dryRun, Collections.EMPTY_LIST, oneByOne, eager,
 			        verbose, username, password, listener.getLogger());
 
 			Object[] mergeArgs = { source, validationScript, workingDirectory };
 			Map returnedObject = (Map) instance.invokeMethod("launchSvnMerge", mergeArgs);
 			listener.getLogger().println("return : " + returnedObject);
 
 			// fill merge result
 			result.setStatus((Boolean) returnedObject.get("status"));
 			List<String> conflictingRevisions = (List<String>) returnedObject.get("conflictingRevisions");
 			if (conflictingRevisions != null) {
 				result.getConflictingRevisions().addAll(conflictingRevisions);
 			}
 
 		} catch (Exception e) {
 			listener.getLogger().println(e.getClass() + " # " + e.getMessage());
 		}
 
 		Action action = new JigomergeBuildAction(build, result, listener);
 		build.addAction(action);
 
 		return result.isStatus();
 	}
 
 	public String getSource() {
 		return source;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public boolean isOneByOne() {
 		return oneByOne;
 	}
 
 	public boolean isEager() {
 		return eager;
 	}
 }
