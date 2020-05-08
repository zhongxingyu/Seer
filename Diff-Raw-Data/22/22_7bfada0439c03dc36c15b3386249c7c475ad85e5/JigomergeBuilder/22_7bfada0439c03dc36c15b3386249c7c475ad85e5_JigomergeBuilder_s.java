 package hudson.plugins.jigomerge;
 
 import groovy.lang.GroovyClassLoader;
 import groovy.lang.GroovyObject;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.tasks.Builder;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.Collections;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 public class JigomergeBuilder extends Builder {
 
 	private final String source;
 	private final String username;
 	private final String password;
 	private final boolean eager;
 
 	@DataBoundConstructor
 	public JigomergeBuilder(String source, String username, String password,
 			boolean eager) {
 		this.source = source;
 		this.username = username;
 		this.password = password;
 		this.eager = eager;
 	}
 
 	@Override
 	public boolean perform(final AbstractBuild<?, ?> build,
 			final Launcher launcher, final BuildListener listener)
 			throws InterruptedException, IOException {
 		boolean status = false;
 
 		try {
 			String fileName = "/work/sources/jigomerge/trunk2/jigomerge/scripts/jigomerge.groovy";
 			GroovyClassLoader gcl = new GroovyClassLoader();
 			Class clazz = gcl.parseClass(new File(fileName));
 			Constructor[] cs = clazz.getConstructors();
		    GroovyObject instance =(GroovyObject) cs[0].newInstance(true, Collections.EMPTY_LIST, false,
					false, true, "toto", "toto");
 
 			Object[] argsM = { "http://toto", null };
			Object returnedObject = instance.invokeMethod("launchSvnMerge", argsM);
 		} catch (Exception e) {
 
 		}
 		return status;
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
 
 	public boolean isEager() {
 		return eager;
 	}
 }
