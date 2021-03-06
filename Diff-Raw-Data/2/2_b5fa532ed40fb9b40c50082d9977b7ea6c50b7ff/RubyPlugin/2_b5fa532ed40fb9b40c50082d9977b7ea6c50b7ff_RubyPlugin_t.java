 package ruby;
 
 import hudson.ExtensionComponent;
 import hudson.Plugin;
 import hudson.model.Items;
 import hudson.util.IOUtils;
 import hudson.util.XStream2;
 import jenkins.model.Jenkins;
 import org.jenkinsci.jruby.JRubyMapper;
 import org.jenkinsci.jruby.JRubyXStream;
 import org.jruby.embed.ScriptingContainer;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * The primary Java interface to a plugin which is implemented in Ruby.
  * One instance is created per each Ruby plugin.
  * 
  * <p>
  * When this plugin initializes, it will instantiate a Jenkins::Plugin
  * object which acts as the gateway for Ruby to interact with the java
  * side.
  * <p>
  * When the RubyPlugin is loaded, it will discover, load and provide
  * a mechanism for extensions written in Ruby that it contains to register
  * themselves.
  * <p>
  * These Extensions are presented to Jenkins via the {@link RubyExtensionFinder}
  * <p>
  * Each plugin has its own JRuby environment
  */
 @SuppressWarnings({"UnusedDeclaration"})
 public class RubyPlugin extends Plugin {
 	/**
 	 * The unique JRuby environment used by this plugin and all the objects
 	 * and classes that it contains.
 	 */
 	private ScriptingContainer ruby;
 
     /**
      * Kinda acts like the "agent" of this ruby plugin in the Ruby world.
      * This is the object that the internals of the ruby side talk to when
      * then want to talk back to Java.
      *
      * @return an instance of Jenkins::Plugin
      */
 	private Object plugin;
 
 	private ArrayList<ExtensionComponent> extensions;
 
 	/**
 	 * invokes a Ruby method on the specified object in the context of this plugin's
 	 * {@link ScriptingContainer}
 	 *
 	 * @param object     <b>JRuby</b> object to use as invocant
 	 * @param methodName the method to end
 	 * @param args       arguments to the method
 	 * @return the return value of the method call.
 	 */
 	public Object callMethod(Object object, String methodName, Object... args) {
 		return ruby.callMethod(object, methodName, args);
 	}
 
 	/**
 	 * Registers an extension with this Ruby plugin so that it will be found later on
 	 * <p/>
 	 * This method is generally called from inside Ruby, as objects that implement
 	 * extension points register themselves.
 	 *
 	 * @param extension
 	 */
 	public void addExtension(Object extension) {
 		extensions.add(new ExtensionComponent(extension));
 	}
 
 	/**
 	 * @return the list of extensions registered with this Plugin. this is used by
 	 *         the {@link RubyExtensionFinder} to present extension points to Jenkins
 	 */
 	public Collection<ExtensionComponent> getExtensions() {
 		return extensions;
 	}
 
 	/**
 	 * Reads a resource relative to this plugin's Java class using a formatted string
 	 *
 	 * @param resource the string template specifying the resource
 	 * @param args     format arguments
 	 * @return the content of the resource
 	 */
 	public String readf(String resource, Object... args) {
 		return read(String.format(resource, args));
 	}
 
 	public RubyPlugin() {
 	}
 
 	/**
 	 * Read a resource relative to this plugin clas
 	 *
 	 * @param resource the name of the resource to be read
 	 * @return the content of the resource
 	 */
 	public String read(String resource) {
 		InputStream stream = this.getClass().getResourceAsStream(resource);
 		try {
 			if (stream == null) {
 				throw new RuntimeException("no such resource: " + resource);
 			}
 			StringBuffer buffer = new StringBuffer();
 			for (int c = stream.read(); c > 0; c = stream.read()) {
 				buffer.append((char) c);
 			}
 			return buffer.toString();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Jenkins will call this method whenever the plugin is initialized
 	 * The plugin will in turn delegate to its instance of Jenkins::Plugin
 	 * which can take action on the Ruby side.
 	 *
 	 * @throws Exception
 	 */
 	@Override
 	public void start() throws Exception {
         if (getWrapper().getShortName().equals("ruby-runtime")) {
             ruby = new ScriptingContainerHolder().ruby;
 
             // Kohsuke: these 3 lines aren't really working. it still requires me to locally install json
             Map<String, String> env = new HashMap<String,String>(this.ruby.getEnvironment());
             env.put("GEM_PATH", this.getClass().getClassLoader().getResource("ruby/vendor/gems/jruby/1.8").getPath());
             this.ruby.setEnvironment(env);
 
 //		this.ruby.getLoadPaths().add(this.getClass().getResource("jenkins-plugins/lib").getPath());
             this.ruby.getLoadPaths().add(this.getClass().getResource(".").getPath());
             this.ruby.runScriptlet("require 'rubygems'");
             this.ruby.runScriptlet("require 'jenkins/plugins'");
 
             register(Jenkins.XSTREAM2, ruby);
             register(Items.XSTREAM2, ruby);
         } else {
             // pick up existing scripting container instead of creating a new one
             // at least for now. TODO: eventually think about isolated multiple scripting container
             ruby = ((RubyPlugin)Jenkins.getInstance().getPlugin("ruby-runtime")).ruby;
         }
         
         this.extensions = new ArrayList<ExtensionComponent>();
         Object pluginClass = this.ruby.runScriptlet("Jenkins::Plugin");
         this.plugin = this.ruby.callMethod(pluginClass, "new", this);
 
 		this.ruby.callMethod(plugin, "start");
 	}
 
     private void register(XStream2 xs, ScriptingContainer ruby) {
         JRubyXStream.register(xs, ruby);
         synchronized (xs) {
             xs.setMapper(new JRubyMapper(xs.getMapperInjectionPoint()));
         }
     }
 
 	/**
 	 * Jenkins will call this method whenever the plugin is shut down
 	 * The plugin will in turn delegate to its instance of Jenkins::Plugin
 	 * which can take action on the Ruby side
 	 *
 	 * @throws Exception
 	 */
 	@Override
 	public void stop() throws Exception {
 		this.ruby.callMethod(plugin, "stop");
 	}
 
 	public String getResourceURI(String relativePathFormat, Object... args) {
 		return getClass().getResource(String.format(relativePathFormat, args)).getPath();
 	}
 
     /**
      * Returns a directory that stores all the Ruby scripts.
      */
     public File getScriptDir() {
         URL url = getWrapper().baseResourceURL;
         // we assume url to be file:// path because we later need to be able to enumerate them
         // to lift this limitation, we need build-time processing to enumerate all the rb files.
         if (!url.getProtocol().equals("file"))
             throw new IllegalStateException("Unexpected base resource URL: "+url);
 
        return new File(new File(url.getPath()),"WEB-INF/classes");
     }
 }
